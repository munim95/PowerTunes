package com.rigid.powertunes.bottomsheetfragments.bottomsheetadapters;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.rigid.powertunes.GlobalVariables;
import com.rigid.powertunes.Selection;
import com.rigid.powertunes.bottomsheetbehaviours.ConfirmationBottomSheet;
import com.rigid.powertunes.bottomsheetbehaviours.MainBottomSheet;
import com.rigid.powertunes.misc.GlobalSelectionTracker;
import com.rigid.powertunes.main.activities.MainActivity;
import com.rigid.powertunes.R;
import com.rigid.powertunes.dialogs.RenamePlaylistDialog;
import com.rigid.powertunes.main.fragments.FoldersSongsFragment;
import com.rigid.powertunes.recyclerviewhelpers.RecyclerViewFoldersDiffCallback;
import com.rigid.powertunes.songmodels.Playlist;
import com.rigid.powertunes.songmodels.PlaylistSong;
import com.rigid.powertunes.songmodels.Song;
import com.rigid.powertunes.util.PlaylistUtil;
import com.rigid.powertunes.util.SongsUtil;
import com.rigid.powertunes.viewmodels.SharedFragmentViewModel;
import com.yarolegovich.discretescrollview.DiscreteScrollView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>{
    private List<Playlist> playlistsList= new ArrayList<>();
    private Context ctx;
    private TextView sheetSelectionText;
    private BottomSheetBehavior bottomsheetBehaviour,bottomsheetConfirmationBehaviour;
    private RecyclerView rv;
    private Selection mySelection;
    private LinearLayout selectionLayout;
    private TextView numOfSelectionText;
    private ArrayList<Uri> songUriCollection;
    private CheckBox checkBox;
    private ImageView editImage;
    private int lastPos=-1;
    private boolean stopped;
    private SharedFragmentViewModel sharedFragmentViewModel;
    private MediaControllerCompat mediaControllerCompat;

    public PlaylistAdapter(Context context,View v) {
        ctx=context;
        initBottomSheetOptionsButtons(v);
        sheetSelectionText=((MainActivity)context).findViewById(R.id.currentSelectionText);
        RelativeLayout layout = ((MainActivity)context).findViewById(R.id.mainBottomsheetLayout);
        bottomsheetBehaviour = MainBottomSheet.from(layout);
        RelativeLayout layout1 =  ((MainActivity)context).findViewById(R.id.delConfirmationBottomSheet);
        bottomsheetConfirmationBehaviour = ConfirmationBottomSheet.from(layout1);
        mediaControllerCompat=((MainActivity)context).getMediaControllerCompat();
        mediaControllerCompat.registerCallback(callback());
        sharedFragmentViewModel=ViewModelProviders.of(((MainActivity)context)).get(SharedFragmentViewModel.class);
    }
    private MediaControllerCompat.Callback callback(){
        return new MediaControllerCompat.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                super.onPlaybackStateChanged(state);
                if(lastPos!=-1) {
                    if (state.getState() == PlaybackStateCompat.STATE_PAUSED) {
                        playlistsList.get(lastPos).setIsPlaying(false);
                        notifyItemChanged(lastPos);
                    } else {
                        playlistsList.get(lastPos).setIsPlaying(true);
                        notifyItemChanged(lastPos);
                    }
                }
            }
        };
    }
    //diffutil
    private void updateSongList(List<Playlist> _playlists) {
        final RecyclerViewFoldersDiffCallback diffCallback = new RecyclerViewFoldersDiffCallback(playlistsList, _playlists);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback, true);
        diffResult.dispatchUpdatesTo(this);
        playlistsList.clear();
        playlistsList.addAll(_playlists);
    }
    public void swapPlaylist(List<Playlist> playlists) {
        updateSongList(playlists);
        songUriCollection=new ArrayList<>();
        for (int i = 0; i <playlists.size(); i++) {
            songUriCollection.add(Uri.parse(playlists.get(i).name+i));
        }
        for(Playlist playlist:playlists){
            if(playlist.isCurrentSelectedFolder()){
                lastPos=playlists.indexOf(playlist);
                rv.scrollToPosition(lastPos);
                break;
            }
        }
    }
    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bottomsheet_folder_item, parent, false);
        return new PlaylistViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        holder.bindTo(position);
    }

    @Override
    public int getItemCount() {
        return playlistsList!=null?playlistsList.size():0;
    }

    @Override
    public void onViewRecycled(@NonNull PlaylistViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.with(holder.itemView).clear(holder.playlistImg);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull PlaylistViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        Glide.with(holder.itemView)
                .load(playlistsList.get(holder.getAdapterPosition()).getChildList().size()>0?playlistsList.get(holder.getAdapterPosition()).getChildList().get(0).imageBytes:null)
                .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(ctx.getResources().getDimensionPixelSize(R.dimen.round_edges_dimen)))
                        .placeholder(R.mipmap.ic_launcher).skipMemoryCache(true))
                .into(holder.playlistImg);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        rv=recyclerView;
        new GlobalSelectionTracker(ctx,recyclerView,()->{
            if(mySelection.hasSelection()){
                selectionLayout.setVisibility(View.VISIBLE);
                selectionLayout.animate().alpha(0.9f).setDuration(150L);
                numOfSelectionText.setText(String.format("%s/%s",
                        mySelection.getSelection().size(), getItemCount()));
                if(mySelection.getSelection().size()!=getItemCount()){
                    checkBox.setChecked(false);
                }else{
                    checkBox.setChecked(true);
                }
            }else{
                checkBox.setChecked(false);
                selectionLayout.animate().alpha(0).setDuration(150L).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if(!mySelection.hasSelection())
                            selectionLayout.setVisibility(View.GONE);
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }
                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }
            if(!mySelection.hasSelection()) {
                bottomsheetConfirmationBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        mySelection=GlobalSelectionTracker.getMySelection();
//        new GlobalSelectionTracker("PlaylistAdapter", recyclerView,new MyDetailsLookup(recyclerView),
//                new MyItemKeyProvider(ItemKeyProvider.SCOPE_MAPPED), selectionObserver());
//        selectionTracker = GlobalSelectionTracker.getSelectionTracker();
        ((DiscreteScrollView)recyclerView).addScrollStateChangeListener(new DiscreteScrollView.ScrollStateChangeListener<RecyclerView.ViewHolder>() {
            @Override
            public void onScrollStart(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                if(((PlaylistViewHolder) viewHolder)!=null) {
                    if (!GlobalVariables.currentSelectedFolder.equals("")) {
                        if (!playlistsList.get(i).isCurrentPlayingFolder()) {
                            TransitionManager.beginDelayedTransition(((PlaylistViewHolder) viewHolder).controlsHolder);
                            ((PlaylistViewHolder) viewHolder).controlsHolder.setVisibility(View.GONE);
                        }
                    } else {
                        TransitionManager.beginDelayedTransition(((PlaylistViewHolder) viewHolder).controlsHolder);
                        ((PlaylistViewHolder) viewHolder).controlsHolder.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onScrollEnd(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

            }

            @Override
            public void onScroll(float v, int i, int i1, @Nullable RecyclerView.ViewHolder viewHolder, @Nullable RecyclerView.ViewHolder t1) {

            }
        });
        ((DiscreteScrollView)recyclerView).addOnItemChangedListener((viewHolder, i) ->{
            if(((PlaylistViewHolder)viewHolder)!=null)
                ((PlaylistViewHolder)viewHolder).controlsHolder.setVisibility(View.VISIBLE);
        });
    }

    public class PlaylistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView playlistImg;
        private TextView playlistName;
        private RelativeLayout relativeLayout;
        private FrameLayout controlsHolder;

        PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            relativeLayout=(RelativeLayout) itemView;
            playlistImg=itemView.findViewById(R.id.bottomsheetItemImage);
            playlistName=itemView.findViewById(R.id.bottomsheetItemName);
            controlsHolder=itemView.findViewById(R.id.bottomsheet_folders_controls_holder);
            controlsHolder.findViewById(R.id.bottomsheet_folders_play).setOnClickListener(playClickListener());
            controlsHolder.findViewById(R.id.bottomsheet_folders_pause).setOnClickListener(playClickListener());
        }
        private void bindTo(int position){
            Playlist playlist=playlistsList.get(position);
            playlistName.setText(playlist.name);
            if(mySelection.isSelected(playlistsList.get(position).name+String.valueOf(position))) {
                relativeLayout.setSelected(true);
            } else{
                relativeLayout.setSelected(false);
            }
            if(playlist.isCurrentSelectedFolder()) {
                controlsHolder.setVisibility(View.VISIBLE);
                if (playlist.isCurrentPlayingFolder()) {
                    controlsHolder.findViewById(R.id.bottomsheet_folders_play).setVisibility(View.GONE);
                    controlsHolder.findViewById(R.id.bottomsheet_folders_pause).setVisibility(View.VISIBLE);
                } else {
                    controlsHolder.findViewById(R.id.bottomsheet_folders_pause).setVisibility(View.GONE);
                    controlsHolder.findViewById(R.id.bottomsheet_folders_play).setVisibility(View.VISIBLE);
                }
            }else{
                controlsHolder.findViewById(R.id.bottomsheet_folders_pause).setVisibility(View.GONE);
                controlsHolder.findViewById(R.id.bottomsheet_folders_play).setVisibility(View.VISIBLE);
                controlsHolder.setVisibility(View.GONE);
            }
        }
        public String getUri(){
            return playlistsList.get(getAdapterPosition()).name+String.valueOf(getAdapterPosition());
        }
        @Override
        public void onClick(View v) {
            if (playlistsList.size() != 0 && !mySelection.hasSelection()) {
                rv.scrollToPosition(getLayoutPosition());
                FoldersSongsFragment receivingFrag=new FoldersSongsFragment();
                Bundle args = new Bundle();
                args.putParcelable("folder", playlistsList.get(getLayoutPosition()));
                receivingFrag.setArguments(args);
                ((MainActivity) ctx).getSupportFragmentManager().beginTransaction().replace(R.id.mainFragContainer, receivingFrag,receivingFrag.getClass().getSimpleName()).commit();
                ((MainActivity) ctx).getSupportFragmentManager().executePendingTransactions();
                doStuffOnTransaction(playlistsList.get(getLayoutPosition()).name);
            }
        }
        private View.OnClickListener playClickListener() {
            return v -> {
                if (playlistsList.get(getAdapterPosition()).getChildList().size() > 0) {
                    if (lastPos != getAdapterPosition()) {
                        if (lastPos != -1)
                            notifyItemChanged(lastPos);
                        lastPos = getAdapterPosition();
                        playlistsList.get(getAdapterPosition()).setIsPlaying(true);
                    }
                    if (v.getId() == R.id.bottomsheet_folders_play) {
                        controlsHolder.findViewById(R.id.bottomsheet_folders_play).setVisibility(View.GONE);
                        controlsHolder.findViewById(R.id.bottomsheet_folders_pause).setVisibility(View.VISIBLE);

                        long newID =Long.parseLong(playlistsList.get(getAdapterPosition()).getChildList().get(0).metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
                            GlobalVariables.currentPosition=0;
                            GlobalVariables.currentSongId = newID;
                            GlobalVariables.shouldBePaused = false;
//                            long[] songIdArray = new long[playlistsList.get(getAdapterPosition()).getChildList().size()];
//                            for (int i0 = 0; i0 < playlistsList.get(getAdapterPosition()).getChildList().size(); i0++) {
//                                Song song = playlistsList.get(getAdapterPosition()).getChildList().get(i0);
//                                songIdArray[i0] = song.id;
//                            }
//                            GlobalVariables.idArray = songIdArray;
                            ArrayList<Song> songList = new ArrayList<>();
                            for (PlaylistSong song : (playlistsList.get(getAdapterPosition()).getChildList())) {
                                songList.add(new Song(
                                        song.data,
                                        song.imageBytes, null, -1));
                            }
                            sharedFragmentViewModel.setCurrentSongsOrder(songList);
//                            ((MainActivity) ctx).swapCardAdapterSongs(songList);
                            mediaControllerCompat.getTransportControls().stop();
                            mediaControllerCompat.getTransportControls().prepareFromMediaId(newID + "", null);
                            mediaControllerCompat.getTransportControls().play();
                        if(!stopped) {
                            makeToast(String.format("Started queue from '%s'", GlobalVariables.currentSelectedFolder));
                        }else{
                            stopped=false;
                            makeToast(String.format("Restarted queue from '%s'", GlobalVariables.currentSelectedFolder));
                        }
                        playlistsList.get(getAdapterPosition()).setIsPlaying(true);
                    } else {
                        stopped=true;
                        controlsHolder.findViewById(R.id.bottomsheet_folders_pause).setVisibility(View.GONE);
                        controlsHolder.findViewById(R.id.bottomsheet_folders_play).setVisibility(View.VISIBLE);
                        mediaControllerCompat.getTransportControls().pause();
                        makeToast(String.format("Stopped queue from '%s'", GlobalVariables.currentSelectedFolder));
                        playlistsList.get(getAdapterPosition()).setIsPlaying(false);
                    }
                }else{
                    makeToast("Folder is empty...");
                }
            };
        }
        private void makeToast(String text){
            Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show();
        }
    }
    private void doStuffOnTransaction(String text) {
        sheetSelectionText.setText(text);
        bottomsheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
    /**
     * Selection options buttons interface
     */
    private void initBottomSheetOptionsButtons(View v){
        selectionLayout=v.findViewById(R.id.bottomsheetSelectionLayout);
        numOfSelectionText=v.findViewById(R.id.bottomsheetNumOfSelection);
        editImage = v.findViewById(R.id.bottomsheetEditSelection);
        editImage.setOnClickListener(bottomsheetSelectionListener());
        checkBox=v.findViewById(R.id.bottomsheetAllSelect);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                mySelection.setItemsSelected(songUriCollection, true);
            }else{
                if(mySelection.getSelection().size()==getItemCount()) {
                    mySelection.setItemsSelected(songUriCollection, false);
                }
            }
        });
        v.findViewById(R.id.bottomsheetDeleteSelection).setOnClickListener(bottomsheetSelectionListener());
        v.findViewById(R.id.bottomsheetShareSelection).setOnClickListener(bottomsheetSelectionListener());
        v.findViewById(R.id.bottomsheetAddPlaylistSelection).setVisibility(View.GONE);
        ((MainActivity)ctx).findViewById(R.id.bottomDialogDelete).setOnClickListener(bottomsheetSelectionListener());
    }
    private View.OnClickListener bottomsheetSelectionListener(){
        return (v -> {
            switch (v.getId()){
                case R.id.bottomsheetDeleteSelection:
                    ((ConfirmationBottomSheet)bottomsheetConfirmationBehaviour).showConfirmationDialog(true,true,false);
                    break;
                case R.id.bottomDialogDelete:
                    ArrayList<Integer> toremove = new ArrayList<>();
                    for(Uri uri : mySelection.getSelection()){
                        for(Playlist playlist : playlistsList){
                            if(playlist.name.equals(uri.toString().replaceAll("\\d*$", ""))){
                                toremove.add(playlist.id);
                            }
                        }
                    }
                    PlaylistUtil.deletePlaylists(ctx,toremove);
                    mySelection.clearSelection();
                    break;
                case R.id.bottomsheetShareSelection:
                    //share all songs in playlist
                    ArrayList<Uri> contentUris = new ArrayList<>(mySelection.getSelection().size());
                    if(mySelection.getSelection().size()<=30) {
                        for (Uri uri : mySelection.getSelection()) {
                            for (Playlist playlist : playlistsList) {
                                if (playlist.name.equals(uri.toString().replaceAll("\\d*$", ""))) {
                                    for(PlaylistSong song : playlist.getChildList()){
                                        contentUris.add(Uri.parse(song.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)));
                                    }
                                }
                            }
                        }
                        ctx.startActivity(Intent.createChooser(SongsUtil.createShareSongFileIntent(contentUris, ctx), null));
                    }else{
                        Toast.makeText(ctx,"Exceeded max share limit of 30.",Toast.LENGTH_SHORT).show();
                    }
                    mySelection.clearSelection();
                    break;
                case R.id.bottomsheetAddPlaylistSelection:
                    //disable
                    ((MainActivity)ctx).findViewById(R.id.bottomsheetAddPlaylistSelection).setVisibility(View.GONE);
                    break;
                case R.id.bottomsheetEditSelection:
                    //rename playlist
                    int playlistId=-1;
                    if(mySelection.getSelection().size()==1) {
                        for (Uri uri : mySelection.getSelection()) {
                            for (Playlist playlist : playlistsList) {
                                if (playlist.name.equals(uri.toString().replaceAll("\\d*$", ""))) {
                                    playlistId = playlist.id;
                                    Log.d("playlists", "id " + playlist.id);
                                }
                            }
                        }
                        RenamePlaylistDialog.create(playlistId).show(((MainActivity)ctx).getSupportFragmentManager(),"renameplaylistdialog");
                    }
                    break;
            }
        });
    }

}
