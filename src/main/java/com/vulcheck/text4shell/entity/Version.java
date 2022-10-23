package com.vulcheck.text4shell.entity;

public class Version {

    private final int major;
    private final int minor;

    public Version(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public static Version parse(String input) {
        String version = input;
        int delimiter = input.indexOf('-');

        if (delimiter > 0)
            version = input.substring(0, delimiter);

        String[] splitVersion = version.split("\\.");
        int major = Integer.parseInt(splitVersion[0]);
        int minor = Integer.parseInt(splitVersion[1]);

        return new Version(major, minor);
    }

}
