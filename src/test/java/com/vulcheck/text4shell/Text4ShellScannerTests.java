package com.vulcheck.text4shell;

import com.vulcheck.text4shell.scanner.Scanner;
import org.junit.jupiter.api.Test;

class Text4ShellScannerTests {

    @Test
    void simpleTest() {
        Scanner scanner = new Scanner();
        scanner.run(new String[] { "/" });
    }

    @Test
    void withPath() {
        Scanner scanner = new Scanner();
        scanner.run(new String[] { "/Users" });
    }

}
