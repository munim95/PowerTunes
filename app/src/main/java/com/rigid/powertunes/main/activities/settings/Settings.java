package com.rigid.powertunes.main.activities.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.util.Log;

import com.rigid.powertunes.GlobalVariables;
import com.rigid.powertunes.R;
import com.rigid.powertunes.ScanClickCallback;
import com.rigid.powertunes.SettingsProgressCallback;
import com.rigid.powertunes.dialogs.filesdialog.FilesDialogFragment;
import com.rigid.powertunes.provider.FetchSongFilesAsync;
import com.rigid.powertunes.util.FileUtil;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import static com.rigid.powertunes.GlobalVariables.BOTTOM_CARD_BLUR_SWITCH_KEY;
import static com.rigid.powertunes.GlobalVariables.BOTTOM_CARD_STATIC_SWITCH_KEY;

//SettingsActivity: host
public class Settings extends PreferenceFragmentCompat implements SettingsProgressCallback,ScanClickCallback,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private Preference rescanFolders;
    private final int SCAN_DELAY_MS=1000;
    /**
     * PREFERENCE KEYS
     * */
    private final String RESCAN_FOLDERS_KEY="rescan_folders";
    private final String MUSIC_FOLDERS_KEY="music_folders";
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preferences,rootKey);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        //do not call here
        Preference musicFoldersPref = findPreference(MUSIC_FOLDERS_KEY);
        rescanFolders = findPreference(RESCAN_FOLDERS_KEY);

//        if(getArguments()!=null) {
//            FilesDialogFragment.getInstance().setScanClickCallback(this);
//            if (getArguments().getString("startchooser").equals("dialog")) {
//                FilesDialogFragment.getInstance().show(getActivity().getSupportFragmentManager(),
//                        FilesDialogFragment.getInstance().getClass().getSimpleName());
//            }
//        }
        //open dialog
        musicFoldersPref.setOnPreferenceClickListener(preference -> {
//            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI,
//                    FileUtil.getExternalDocumentContentUris(getContext())[0]);
//            startActivityForResult(intent,0);
            FilesDialogFragment filesDialogFragment = FilesDialogFragment.getInstance();
            filesDialogFragment.setScanClickCallback(this);
            filesDialogFragment.show(getActivity().getSupportFragmentManager(),
                    filesDialogFragment.getClass().getSimpleName());
            return true;
        });
        //rescan
        rescanFolders.setOnPreferenceClickListener(preference -> {
            if(!FileUtil.readIt(getActivity(), GlobalVariables.FILENAMESELECTION,true))
                rescanFolders.setSummary("No Folders Selected.");

            rescanFolders.setEnabled(false);
            return true;
        });
        FetchSongFilesAsync fetchSongFilesAsync = FetchSongFilesAsync.getInstance();
        fetchSongFilesAsync.setSettingsPostExecuteCallback(this);
        rescanFolders.setSummary("Songs: "+fetchSongFilesAsync.getSelectedNumber());
    }

    @Override
    public void onProgressUpdate(String... strs) {
        rescanFolders.setSummary(strs[0]);
    }

    @Override
    public void onComplete() {
        new Handler().postDelayed(()-> {
            rescanFolders.setSummary("Songs: "+FetchSongFilesAsync.getInstance().getSelectedNumber());
            rescanFolders.setEnabled(true);
        },3000);
    }

    @Override
    public void onScanClicked() {
        rescanFolders.setEnabled(false);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key){
            case BOTTOM_CARD_BLUR_SWITCH_KEY:
                break;
            case BOTTOM_CARD_STATIC_SWITCH_KEY:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //check if primary uri is the same as this one else save in data folder
        if(requestCode==0 && resultCode==Activity.RESULT_OK){
            Uri uris = data.getData();
            DocumentFile documentFile =DocumentFile.fromTreeUri(getContext(),uris);
            Log.d("lol","ok "+documentFile.getUri());
        }
    }
}
