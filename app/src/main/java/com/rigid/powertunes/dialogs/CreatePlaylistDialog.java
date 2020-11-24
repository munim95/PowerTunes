package com.rigid.powertunes.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rigid.powertunes.misc.GlobalSelectionTracker;
import com.rigid.powertunes.R;
import com.rigid.powertunes.util.PlaylistUtil;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class CreatePlaylistDialog extends DialogFragment {
    private static final String SONGS = "songs";

    @NonNull
    public static CreatePlaylistDialog create() {
        return create((String) null);
    }

    @NonNull
    public static CreatePlaylistDialog create(@Nullable String song) {
        ArrayList<String> list = new ArrayList<>();
        if (song != null)
            list.add(song);
        return create(list);
    }

    @NonNull
    public static CreatePlaylistDialog create(ArrayList<String> songs) {
        CreatePlaylistDialog dialog = new CreatePlaylistDialog();
        Bundle args = new Bundle();
        args.putStringArrayList(SONGS, songs);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialDialog.Builder(getActivity())
                .backgroundColorRes(R.color.blackAlpha)
                .title(R.string.new_playlist_title)
                .positiveText(R.string.create_action)
                .negativeText(android.R.string.cancel)
                .inputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                .input(R.string.playlist_name_empty, 0, false, (dialog, input) -> {
                    final String name = input.toString().trim();
                    if (!name.isEmpty()) {
                        ArrayList<String> songs = getArguments().getStringArrayList(SONGS);
                        if (songs != null && !songs.isEmpty()) {
                            if (PlaylistUtil.createNewPlaylistFile(getActivity(), name, songs)) {
                                Toast.makeText(getActivity(), getActivity().getResources().getString(
                                        R.string.playlist_exists, name), Toast.LENGTH_SHORT).show();
                                GlobalSelectionTracker.getMySelection().clearSelection();
                            }else{
                                Toast.makeText(getActivity(), getActivity().getResources().getString(
                                        R.string.added_x_songs_into_playlist_x, songs.size(),name), Toast.LENGTH_SHORT).show();
                                GlobalSelectionTracker.getMySelection().clearSelection();
                            }
                        }
                    }
                }).build();
    }
}
