package com.vulcheck.text4shell.scanner;

import com.vulcheck.text4shell.utils.CustomUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import static com.vulcheck.text4shell.utils.CustomUtils.isTarget;

public class Scanner {

    public static final String SCANNER_NAME = "Scanner for CVE-2022-42889";

    private Config config;
    private Detector detector;

    public static void main(String[] args) {
        System.out.println(SCANNER_NAME);

        try {
            Scanner scanner = new Scanner();
            int status = scanner.run(args);

            System.exit(status);
        } catch (Throwable t) {
            System.out.printf("Error! MSG : %s\r\n", t.getMessage());

            if (!(t instanceof IllegalArgumentException))
                t.printStackTrace();

            System.exit(-1);
        }
    }

    public int run(String[] configs) {
        if (configs.length < 1) {
            Config.printUsage();

            return 0;
        }

        config = Config.parse(configs);

        return scan();
    }

    public int scan() {
        detector = new Detector(config);

        try {
            for (String targetPath : config.getTargetPaths()) {
                File f = new File(targetPath);

                traverse(f, 0);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("### Result ###");
            System.out.println("Vulnerable Files are " + detector.getVulnerableFileCount());
        }

        if (detector.getVulnerableFileCount() > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    private void traverse(File f, int depth) throws IOException {
        if (depth == 0 && !f.exists()) {
            printSkipped(f);
            return;
        }

        if (!f.canRead()) {
            return;
        }

        String path = f.getAbsolutePath();

        if (f.isDirectory()) {
            if (isExcluded(path))
                return;

            if (isExcludedDirectory(path))
                return;

            DirectoryStream<Path> stream = null;

            try {
                stream = Files.newDirectoryStream(f.toPath());

                for (Path p : stream) {
                    if (!Files.isReadable(p) || Files.isSymbolicLink(p))
                        continue;
                    traverse(p.toFile(), depth + 1);
                }
            } catch (AccessDeniedException e) {
                printSkipped(f, "Access denied", e);
            } catch (DirectoryIteratorException ex) {
                throw ex.getCause();
            } catch (IOException e) {
                String msg = e.getClass().getSimpleName() + " - " + e.getMessage();
                printSkipped(f, msg, e);
            } finally {
                CustomUtils.ensureClose(stream);
            }
        } else {
            if (isTarget(path)) {
                detector.scanFile(f);
            }
        }
    }

    private boolean isExcludedDirectory(String path) {
        return ((path.equals("/dev") || path.startsWith("/dev/")) || path.equals("/proc") || path.startsWith("/proc/"))
                || (path.equals("/run") || path.startsWith("/run/")) || (path.equals("/sys") || path.startsWith("/sys/"))
                || path.startsWith("/var/folders/") || path.equals("/var/folders") || (path.equals("/var/run") || path.startsWith("/var/run/")
        );
    }

    private boolean isExcluded(String path) {
        for (String excludePath : config.getExcludedPathPrefixes()) {
            if (path.startsWith(excludePath))
                return true;
        }

        for (String excludePattern : config.getExcludedPatterns()) {
            if (path.contains(excludePattern))
                return true;
        }

        return false;
    }

    private void printSkipped(File f) {
        printSkipped(f, "File not found", null);
    }

    private void printSkipped(File f, String msg, Throwable t) {
        if (t != null) {
            System.out.printf("Skipped! Path : %s, MSG : %s\r\n", f.getAbsolutePath(), t.getMessage());
            return;
        }

        System.out.printf("Skipped! Path : %s, MSG : %s\r\n%n", f.getAbsolutePath(), msg);
    }

}
