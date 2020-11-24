package com.rigid.powertunes.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rigid.powertunes.misc.GlobalSelectionTracker;
import com.rigid.powertunes.R;
import com.rigid.powertunes.util.PlaylistUtil;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class RenamePlaylistDialog extends DialogFragment {
    private static final String PLAYLIST= "playlist";

    @NonNull
    public static RenamePlaylistDialog create() {
        return create(-1);
    }
    @NonNull
    public static RenamePlaylistDialog create(int playlistId) {
        RenamePlaylistDialog dialog = new RenamePlaylistDialog();
        Bundle args = new Bundle();
        args.putInt(PLAYLIST, playlistId);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialDialog.Builder(getActivity())
                .title("Rename Playlist")
                .positiveText(R.string.create_action)
                .negativeText(android.R.string.cancel)
                .inputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                .input(R.string.playlist_name_empty, 0, false, (dialog, input) -> {
                    final String name = input.toString().trim();
                    if (!name.isEmpty()) {
                        int playlistId = getArguments().getInt(PLAYLIST);
                        if (PlaylistUtil.renamePlaylist(getActivity(), playlistId,name)) {
                            Toast.makeText(getActivity(), String.format("Renamed playlist to %s.",name), Toast.LENGTH_SHORT).show();

                            GlobalSelectionTracker.getMySelection().clearSelection();
                        }else{
                            Toast.makeText(getActivity(),String.format("Playlist name %s already exists. Try Another.",name), Toast.LENGTH_SHORT).show();

                            GlobalSelectionTracker.getMySelection().clearSelection();
                        }
                    }
                }).build();
    }
}
