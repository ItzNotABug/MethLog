package com.lazygeniouz.methlog.plugin;

public class MethLogExtension {
    private boolean enabled = true;

    @SuppressWarnings("unused")
    public void setEnabled(boolean value) {
        this.enabled = value;
    }

    public boolean getEnabled() {
        return enabled;
    }
}