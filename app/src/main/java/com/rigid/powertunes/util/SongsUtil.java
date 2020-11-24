package com.rigid.powertunes.util;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.rigid.powertunes.songmodels.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

public class SongsUtil {

    @NonNull
    public static Intent createShareSongFileIntent(@NonNull final ArrayList<Uri> uris, Context context) {
        try {
            return new Intent()
                    .setAction(Intent.ACTION_SEND_MULTIPLE)
                    .putParcelableArrayListExtra(Intent.EXTRA_STREAM,uris)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .setType("audio/*");

        } catch (IllegalArgumentException e) {
            // TODO the path is most likely not like /storage/emulated/0/... but something like /storage/28C7-75B0/...
            e.printStackTrace();
            Toast.makeText(context, "Could not share this file. Try moving to different directory.", Toast.LENGTH_SHORT).show();
            return new Intent();
        }
    }

}
