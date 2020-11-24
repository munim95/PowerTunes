package com.rigid.powertunes.dialogs;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.rigid.powertunes.R;
import com.rigid.powertunes.songmodels.Song;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class EditTagDialog extends DialogFragment implements TextWatcher {
    private EditText editTitle,editTrack,editYear,editArtist,editAlbum;
    private Spinner editGenre;
    private Button close,save;
    private Song song;

    public static EditTagDialog createEditDialog(Song song){
        EditTagDialog editTagDialog=new EditTagDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelable("currentsong",song);
        editTagDialog.setArguments(bundle);
        return editTagDialog;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        song=getArguments().getParcelable("currentsong");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().setTitle("Edit Tags");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.edit_tag_dialog,container,false);
        editTitle=v.findViewById(R.id.editTagTitle);
        editTrack=v.findViewById(R.id.editTagTrack);
        editYear=v.findViewById(R.id.editTagYear);
        editGenre =v.findViewById(R.id.editTagGenre);
        editArtist=v.findViewById(R.id.editTagArtist);
        editAlbum=v.findViewById(R.id.editTagAlbum);
        close=v.findViewById(R.id.closeEditDialogBtn);
        save=v.findViewById(R.id.saveEditDialogBtn);

        fillInDefaultSongDetails(song);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editTitle.addTextChangedListener(this);
        editTrack.addTextChangedListener(this);
        editYear.addTextChangedListener(this);
        editAlbum.addTextChangedListener(this);
        editArtist.addTextChangedListener(this);

        save.setOnClickListener(handleClick());
        close.setOnClickListener(handleClick());
    }
    private View.OnClickListener handleClick(){
        return v -> {
            if(v.getId()==save.getId()){
                //handle saving values
                //use jaudio tagger
            }else{
                dismiss();
            }
        };
    }
    @Override
    public void onResume() {
        // Store access variables for window and blank point
        Window window = getDialog().getWindow();
        Point size = new Point();
        // Store dimensions of the screen in `size`
        window.getWindowManager().getDefaultDisplay().getSize(size);
        // Set the height of the dialog proportional to 80% of the screen width
        if(window.getWindowManager().getDefaultDisplay().getRotation()== Surface.ROTATION_90||
                window.getWindowManager().getDefaultDisplay().getRotation()==Surface.ROTATION_270){
            window.setLayout((int)(size.x*0.70), (int)(size.y * 0.80));
        }
        else {
            window.setLayout(MATCH_PARENT, WRAP_CONTENT);
        }
        window.setGravity(Gravity.CENTER);
        super.onResume();
    }

    private void fillInDefaultSongDetails(Song song){
        editTitle.setText(song.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        editAlbum.setText(song.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
        editArtist.setText(song.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
