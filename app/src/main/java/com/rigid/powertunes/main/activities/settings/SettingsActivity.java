package com.rigid.powertunes.main.activities.settings;

import android.os.Bundle;

import com.rigid.powertunes.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        Settings settings = new Settings();
        if(getIntent().getStringExtra("startchooser")!=null) {
            Bundle bundle = new Bundle();
            bundle.putString("startchooser", getIntent().getStringExtra("startchooser"));
            settings.setArguments(bundle);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.settingsContainer,settings).commit();
    }

}
