package com.vulcheck.text4shell.scanner;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Config {

    private final List<String> targetPaths = new LinkedList<>();
    private final List<String> excludedPathPrefixes = new ArrayList<>();
    private final List<String> excludedPatterns = new ArrayList<>();

    private Charset charset = StandardCharsets.UTF_8;

    public static void printUsage() {
        System.out.println("Usage: text4shell-scan target_path1 target_path2\r\n");

        System.out.println("--charset");
        System.out.println("\t(Default is utf-8)");

        System.out.println("--exclude-prefix [prefix]");

        System.out.println("--exclude-pattern [pattern]");

        System.out.println("--help");
    }

    public static Config parse(String[] args) {
        Config config = new Config();

        for (int i=0; i < args.length; i++) {
            switch (args[i]) {
                case "--help":
                    printUsage();

                    System.exit(-1);
                case "--charset":
                    validate(args, i, "Charset", "Charset is not specified");

                    config.charset = Charset.forName(args[i++ + 1]);

                    break;
                case "--exclude-prefix":
                    validate(args, i, "Exclude path", "Exclude prefix is not specified");

                    String path = args[1 + i++];

                    config.excludedPathPrefixes.add(path);

                    break;
                case "--exclude-pattern":
                    validate(args, i, "Pattern", "Exclude pattern is not specified");

                    String pattern = args[1 + i++];

                    config.excludedPatterns.add(pattern);

                    break;
                default:
                    if (args[i].startsWith("-"))
                        throw new IllegalArgumentException("Unknown option: " + args[i]);

                    String targetPath = args[i];
                    File file = new File(targetPath);

                    if (!file.exists())
                        throw new IllegalArgumentException("path not found: " + file.getAbsolutePath());

                    if (!file.canRead())
                        throw new IllegalArgumentException("no permission for " + file.getAbsolutePath());

                    config.targetPaths.add(targetPath);

                    break;
            }
        }

        String osName = System.getProperty("os.name");

        if (osName != null && osName.toLowerCase().startsWith("mac")) {
            config.excludedPatterns.add("/.Trash");
            config.excludedPatterns.add("/Dropbox");
            config.excludedPatterns.add("/Library");
        }

        System.out.println("UserName :" + System.getProperty("user.name"));
        System.out.println("OS name : " + osName);
        System.out.println("Target paths : " + config.targetPaths);
        System.out.println("Exclude paths : " + config.excludedPatterns);

        return config;
    }

    private static void validate(String[] configs, int i, String name, String msg) {
        if (configs.length <= i + 1)
            throw new IllegalArgumentException(msg);

        if (configs[i + 1].startsWith("--"))
            throw new IllegalArgumentException("Error! Arguments should start with \"--\" (" + name + ")");
    }

    public List<String> getTargetPaths() {
        return targetPaths;
    }

    public List<String> getExcludedPathPrefixes() {
        return excludedPathPrefixes;
    }

    public List<String> getExcludedPatterns() {
        return excludedPatterns;
    }

    public Charset getCharset() {
        return charset;
    }

}
