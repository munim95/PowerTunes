package com.rigid.powertunes;

public interface SettingsProgressCallback {
    void onProgressUpdate(String... strs);
    void onComplete();
}
