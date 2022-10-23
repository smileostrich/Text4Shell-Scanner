package com.vulcheck.text4shell.entity;

public class VulState {

    private boolean vulnerable = false;

    public boolean isVulnerable() {
        return vulnerable;
    }

    public void setVulnerable() {
        this.vulnerable = true;
    }

}
