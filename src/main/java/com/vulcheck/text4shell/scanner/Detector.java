package com.vulcheck.text4shell.scanner;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import com.vulcheck.text4shell.entity.VulState;
import com.vulcheck.text4shell.entity.Version;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import com.vulcheck.text4shell.utils.DummyInputStream;
import com.vulcheck.text4shell.utils.CustomUtils;
import com.vulcheck.text4shell.utils.ZipFileParser;

import static com.vulcheck.text4shell.utils.CustomUtils.isTarget;

public class Detector {

    private final Config config;

    private int vulnerableFileCount = 0;

    private static final String POM_PROPERTIES_PATH = "META-INF/maven/org.apache.commons/commons-text/pom.properties";

    public Detector(Config config) {
        this.config = config;
    }

    protected int getVulnerableFileCount() {
        return vulnerableFileCount;
    }

    protected void scanFile(File jarFile) {
        scanFile(jarFile, StandardCharsets.UTF_8);
    }

    protected void scanFile(File jarFile, Charset charset) {
        InputStream inputStream = null;
        ZipFileParser it = null;
        Charset altCharset;

        try {
            inputStream = new BufferedInputStream(new FileInputStream(jarFile));
            VulState result;

            try {
                it = parseZipIterator(jarFile, inputStream, charset, 0);
                result = scanStream(jarFile, it, new ArrayList<>(), StandardCharsets.UTF_8, 0);
            } catch (IllegalArgumentException e) {
                // when the first try fails, try again with the system encoding or alternative encoding
                altCharset = Charset.defaultCharset();

                if (config.getCharset() != null)
                    altCharset = config.getCharset();

                CustomUtils.ensureClose(it);
                CustomUtils.ensureClose(inputStream);
                inputStream = new BufferedInputStream(new FileInputStream(jarFile));

                it = parseZipIterator(jarFile, inputStream, charset, 0);
                result = scanStream(jarFile, it, new ArrayList<>(), altCharset, 0);
            }

            if (result.isVulnerable())
                vulnerableFileCount++;

        } catch (ZipException e) {
            System.err.printf("Skipped! File : %s, MSG : %s", jarFile, e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.printf("Skipped! File : %s, MSG : %s", jarFile, e.getMessage());
        } catch (Throwable t) {
            System.err.printf("Error! File : %s, MSG : %s", jarFile, t.getMessage());
        } finally {
            CustomUtils.ensureClose(it);
            CustomUtils.ensureClose(inputStream);
        }
    }

    private VulState scanStream(File jarFile, ZipFileParser it, List<String> pathList, Charset charset, int depth) throws IOException {
        VulState result = new VulState();
        String version = null;

        while (true) {
            ZipEntry entry = it.getNextEntry();

            if (entry == null)
                break;

            InputStream is = it.getNextInputStream();

            if (entry.getName().equals(POM_PROPERTIES_PATH))
                version = parseVersion(is);

            if (isTarget(entry.getName())) {
                ZipFileParser nestedIt = null;
                try {
                    nestedIt = parseZipIterator(jarFile, is, charset, depth + 1);
                    pathList.add(entry.getName());

                    VulState nestedResult = scanStream(jarFile, nestedIt, pathList, charset, depth + 1);

                    if (!result.isVulnerable() && nestedResult.isVulnerable())
                        result.setVulnerable();

                    pathList.remove(pathList.size() - 1);
                } finally {
                    CustomUtils.ensureClose(nestedIt);
                }
            }
        }

        if (version != null && !version.equals("")) {
            if (isVulnerableCommonsText(Version.parse(version))) {
                printDetection(jarFile, pathList, version);

                result.setVulnerable();
            } else {
                System.out.printf("commons-text version (%s) is not vulnerable", version);
            }
        }

        return result;
    }

    private ZipFileParser parseZipIterator(File jarFile, InputStream is, Charset charset, int depth) throws IOException {
        try {
            return new ZipFileParser(new ZipArchiveInputStream(new DummyInputStream(is)));
        } catch (Exception e) {
            if (depth == 0)
                return new ZipFileParser(jarFile, charset);

            return new ZipFileParser(new ZipInputStream(new DummyInputStream(is), charset));
        }
    }

    private String parseVersion(InputStream is) throws IOException {
        Properties props = new Properties();
        props.load(is);

        String groupId = props.getProperty("groupId");
        String artifactId = props.getProperty("artifactId");
        String version = props.getProperty("version");

        if (groupId.equals("org.apache.commons") && artifactId.equals("commons-text")) {
            return version;
        }

        return null;
    }

    private boolean isVulnerableCommonsText(Version version) {
        if (version.getMajor() > 1)
            return false;

        if (version.getMajor() == 0)
            return true;

        return version.getMinor() < 10;
    }

    private void printDetection(File jarFile, List<String> pathList, String version) {
        String path = getPath(jarFile, pathList);

        System.out.printf(String.format("Critical! Found vulnerability(CVE-2022-42889)! Path : %s, Version : %s\r\n", path, version));
    }

    private String getPath(File jarFile, List<String> pathList) {
        String path = jarFile.getAbsolutePath();

        if (pathList != null && !pathList.isEmpty())
            path += " (" + concat(pathList) + ")";

        return path;
    }

    private String concat(List<String> pathList) {
        if (pathList == null)
            return "";

        StringBuilder sb = new StringBuilder();
        int i = 0;

        for (String path : pathList) {
            if (i++ != 0)
                sb.append(" > ");
            sb.append(path);
        }

        return sb.toString();
    }

}
