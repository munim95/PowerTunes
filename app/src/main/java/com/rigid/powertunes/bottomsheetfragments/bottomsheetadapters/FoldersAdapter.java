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
import com.rigid.powertunes.bottomsheetfragments.AddToPlaylistFragment;
import com.rigid.powertunes.misc.GlobalSelectionTracker;
import com.rigid.powertunes.main.activities.MainActivity;
import com.rigid.powertunes.R;
import com.rigid.powertunes.main.fragments.FoldersSongsFragment;
import com.rigid.powertunes.recyclerviewhelpers.RecyclerViewFoldersDiffCallback;
import com.rigid.powertunes.songmodels.Folder;
import com.rigid.powertunes.songmodels.Song;
import com.rigid.powertunes.util.FileUtil;
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

public class FoldersAdapter extends RecyclerView.Adapter<FoldersAdapter.FoldersViewHolder> {
    private List<Folder> folders=new ArrayList<>();
    private Context context;
    private TextView sheetSelectionText;
    private BottomSheetBehavior bottomsheetBehaviour,bottomsheetPlaylistBehaviour,bottomsheetConfirmationBehaviour;
    private RecyclerView rv;
//    private CustomSelectionTracker<Uri> selectionTracker;
    private Selection mySelection;
    private LinearLayout selectionLayout;
    private TextView numOfSelectionText;
    private CheckBox checkBox;
    private ArrayList<Uri> songUriCollection;
    private int lastPos=-1;
    private boolean stopped;
    private SharedFragmentViewModel sharedFragmentViewModel;
    private MediaControllerCompat mediaControllerCompat;

    public FoldersAdapter(Context context,View v) {
        this.context =context;
        initBottomSheetOptionsButtons(v);
        sheetSelectionText=((MainActivity)context).findViewById(R.id.currentSelectionText);
        RelativeLayout layout = ((MainActivity)context).findViewById(R.id.mainBottomsheetLayout);
        bottomsheetBehaviour = MainBottomSheet.from(layout);
        RelativeLayout playlistLayout = ((MainActivity)context).findViewById(R.id.new_playlist_sheet);
        bottomsheetPlaylistBehaviour = ConfirmationBottomSheet.from(playlistLayout);
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
                        folders.get(lastPos).setIsPlaying(false);
                        notifyItemChanged(lastPos);
                    } else {
                        folders.get(lastPos).setIsPlaying(true);
                        notifyItemChanged(lastPos);
                    }
                }
            }
        };
    }
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        rv=recyclerView;
        new GlobalSelectionTracker(context,recyclerView,()->{
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
//        new GlobalSelectionTracker("FoldersAdapter", recyclerView,new MyDetailsLookup(recyclerView),
//                new MyItemKeyProvider(ItemKeyProvider.SCOPE_MAPPED), selectionObserver());
//        selectionTracker = GlobalSelectionTracker.getSelectionTracker();
        ((DiscreteScrollView)recyclerView).addScrollStateChangeListener(new DiscreteScrollView.ScrollStateChangeListener<RecyclerView.ViewHolder>() {
            @Override
            public void onScrollStart(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                if(!GlobalVariables.currentSelectedFolder.equals("")) {
                    if (!folders.get(i).isCurrentPlayingFolder()) {
                        TransitionManager.beginDelayedTransition(((FoldersViewHolder) viewHolder).controlsHolder);
                        ((FoldersViewHolder) viewHolder).controlsHolder.setVisibility(View.GONE);
                    }
                }else {
                    TransitionManager.beginDelayedTransition(((FoldersViewHolder) viewHolder).controlsHolder);
                    ((FoldersViewHolder) viewHolder).controlsHolder.setVisibility(View.GONE);
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
            if(viewHolder!=null)
                ((FoldersViewHolder)viewHolder).controlsHolder.setVisibility(View.VISIBLE);
        });
    }


    @NonNull
    @Override
    public FoldersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bottomsheet_folder_item, parent, false);
        return new FoldersViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FoldersViewHolder holder, int position) {
        holder.bindTo(position);
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    @Override
    public void onViewRecycled(@NonNull FoldersViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.with(holder.itemView).clear(holder.imageView);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull FoldersViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        Glide.with(holder.itemView)
                .load(folders.get(holder.getAdapterPosition()).getSongsInFolders().size()>0?folders.get(holder.getAdapterPosition()).getSongsInFolders().get(0).imageBytes:null)
                .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(context.getResources().getDimensionPixelSize(R.dimen.round_edges_dimen)))
                        .placeholder(R.mipmap.ic_launcher).skipMemoryCache(true))
                .into(holder.imageView);
    }

    //diffutil
    private void updateSongList(List<Folder> _folders){
        final RecyclerViewFoldersDiffCallback diffCallback = new RecyclerViewFoldersDiffCallback(folders,_folders);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback,true);
        diffResult.dispatchUpdatesTo(this);
        folders.clear();
        folders.addAll(_folders);
    }
    public void swapFolders(List<Folder> folders){
        updateSongList(folders);
        songUriCollection=new ArrayList<>();
        for (int i = 0; i <folders.size(); i++) {
            songUriCollection.add(Uri.parse(folders.get(i).folderName+i));
        }
        for(Folder folder:folders){
            if(folder.isCurrentSelectedFolder()){
                lastPos=folders.indexOf(folder);
                rv.scrollToPosition(lastPos);
                break;
            }
        }
    }

    public class FoldersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView name;
        private RelativeLayout relativeLayout;
        private ImageView imageView;
        private FrameLayout controlsHolder;

        FoldersViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            relativeLayout=(RelativeLayout) itemView;
            name=itemView.findViewById(R.id.bottomsheetItemName);
            imageView=itemView.findViewById(R.id.bottomsheetItemImage);
            controlsHolder=itemView.findViewById(R.id.bottomsheet_folders_controls_holder);
            controlsHolder.findViewById(R.id.bottomsheet_folders_play).setOnClickListener(playClickListener());
            controlsHolder.findViewById(R.id.bottomsheet_folders_pause).setOnClickListener(playClickListener());
        }
        private void bindTo(int position){
            Folder folder=folders.get(position);
            name.setText(folder.folderName);
            if(mySelection!=null) {
                if(mySelection.isSelected(folders.get(position).folderName+String.valueOf(position))) {
                    relativeLayout.setSelected(true);
                } else{
                    relativeLayout.setSelected(false); }
            }
            if(folder.isCurrentSelectedFolder()) {
                controlsHolder.setVisibility(View.VISIBLE);
                if (folder.isCurrentPlayingFolder()) {
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
            return folders.get(getAdapterPosition()).folderName+String.valueOf(getAdapterPosition());
        }
        @Override
        public void onClick(View v) {
            if (folders.size() != 0 && !mySelection.hasSelection()) {
                rv.scrollToPosition(getLayoutPosition());
                FoldersSongsFragment receivingFrag=new FoldersSongsFragment();
                //launch another frag containing playlist songs
                Bundle args = new Bundle();
                args.putParcelable("folder", folders.get(getLayoutPosition()));
                receivingFrag.setArguments(args);
                ((MainActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.mainFragContainer, receivingFrag,receivingFrag.getClass().getSimpleName()).commit();
                ((MainActivity) context).getSupportFragmentManager().executePendingTransactions();
                doStuffOnTransaction(folders.get(getLayoutPosition()).folderName);
            }
        }
        private View.OnClickListener playClickListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (folders.get(getAdapterPosition()).getSongsInFolders().size() > 0) {
                        if (lastPos != getAdapterPosition()) {
                            if (lastPos != -1)
                                notifyItemChanged(lastPos);
                            lastPos = getAdapterPosition();
                            folders.get(getAdapterPosition()).setIsPlaying(true);
                        }
                        if (v.getId() == R.id.bottomsheet_folders_play) {
                            controlsHolder.findViewById(R.id.bottomsheet_folders_play).setVisibility(View.GONE);
                            controlsHolder.findViewById(R.id.bottomsheet_folders_pause).setVisibility(View.VISIBLE);

                            long newID = Long.parseLong(folders.get(getAdapterPosition()).getSongsInFolders().get(0).metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
                                GlobalVariables.currentPosition=0;
                                GlobalVariables.currentSongId = newID;
                                GlobalVariables.shouldBePaused = false;
//                                long[] songIdArray = new long[folders.get(getAdapterPosition()).getSongsInFolders().size()];
//                                for (int i0 = 0; i0 < folders.get(getAdapterPosition()).getSongsInFolders().size(); i0++) {
//                                    Song song = folders.get(getAdapterPosition()).getSongsInFolders().get(i0);
//                                    songIdArray[i0] = song.id;
//                                }
//                                GlobalVariables.idArray = songIdArray;
//
//                                ((MainActivity) context).swapCardAdapterSongs((ArrayList<Song>) folders.get(getAdapterPosition()).getSongsInFolders());
                                sharedFragmentViewModel.setCurrentSongsOrder(folders.get(getAdapterPosition()).getSongsInFolders());
                                mediaControllerCompat.getTransportControls().stop();
                                mediaControllerCompat.getTransportControls().prepareFromMediaId(newID + "", null);
                                mediaControllerCompat.getTransportControls().play();
                            if(!stopped) {
                                makeToast(String.format("Started queue from '%s'", GlobalVariables.currentSelectedFolder));
                            }else{
                                stopped=false;
                                makeToast(String.format("Restarted queue from '%s'", GlobalVariables.currentSelectedFolder));
                            }
                            folders.get(getAdapterPosition()).setIsPlaying(true);
                        } else {
                            controlsHolder.findViewById(R.id.bottomsheet_folders_pause).setVisibility(View.GONE);
                            controlsHolder.findViewById(R.id.bottomsheet_folders_play).setVisibility(View.VISIBLE);
                            mediaControllerCompat.getTransportControls().pause();
                            makeToast(String.format("Stopped queue from '%s'", GlobalVariables.currentSelectedFolder));
                            folders.get(getAdapterPosition()).setIsPlaying(false);
                        }
                    }else{
                        makeToast("Folder is empty...");
                    }
                }
            };
        }
        private void makeToast(String text){
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
    }
    private void doStuffOnTransaction(String text){
        sheetSelectionText.setText(text);
        bottomsheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    /**
     * Selection options buttons interface
     */
    private void initBottomSheetOptionsButtons(View v){
        selectionLayout=v.findViewById(R.id.bottomsheetSelectionLayout);
        numOfSelectionText=v.findViewById(R.id.bottomsheetNumOfSelection);
        checkBox=v.findViewById(R.id.bottomsheetAllSelect);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                mySelection.setItemsSelected(songUriCollection, true);
            }else{
                if(mySelection.getSelection().size()==getItemCount())
                    mySelection.setItemsSelected(songUriCollection, false);
            }
        });
        v.findViewById(R.id.bottomsheetDeleteSelection).setOnClickListener(bottomsheetSelectionListener());
        v.findViewById(R.id.bottomsheetEditSelection).setOnClickListener(bottomsheetSelectionListener());
        v.findViewById(R.id.bottomsheetShareSelection).setOnClickListener(bottomsheetSelectionListener());
        v.findViewById(R.id.bottomsheetAddPlaylistSelection).setOnClickListener(bottomsheetSelectionListener());
        ((MainActivity)context).findViewById(R.id.bottomDialogDelete).setOnClickListener(bottomsheetSelectionListener());
    }
    private View.OnClickListener bottomsheetSelectionListener(){
        return (v -> {
            switch (v.getId()){
                case R.id.bottomsheetDeleteSelection:
                    ((ConfirmationBottomSheet)bottomsheetConfirmationBehaviour).showConfirmationDialog(true,false,false);
                    break;
                case R.id.bottomDialogDelete:
                    String[] paths = new String[mySelection.getSelection().size()];
                    int count = 0;
                    for(Uri uri : mySelection.getSelection()){
                        for(Folder folder : folders){
                            if(folder.folderName.equals(uri.toString().replaceAll("\\d*$", ""))){
                                for(Song song : folder.getSongsInFolders()){
                                    paths[count++]=song.data;
                                }
                            }
                        }
                        //delete and get deleted paths
                        FileUtil.deleteSongs(context,paths);
                        //update all view models and components
                        FileUtil.readTrackSelectionFileAndExecute(context,GlobalVariables.FILENAMESELECTION,false);
                    }
                    mySelection.clearSelection();
                    break;
                case R.id.bottomsheetShareSelection:
                    ArrayList<Uri> contentUris = new ArrayList<>(mySelection.getSelection().size());
                    if(mySelection.getSelection().size()<=30) {
                        for (Uri uri : mySelection.getSelection()) {
                            for (Folder folder : folders) {
                                if (folder.folderName.equals(uri.toString().replaceAll("\\d*$", ""))) {
                                    for (Song song : folder.getSongsInFolders()) {
                                        contentUris.add(Uri.parse(song.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)));
                                    }
                                }
                            }
                        }
                    }else{
                        Toast.makeText(context,"Exceeded max share limit of 30.",Toast.LENGTH_SHORT).show();
                    }
                    context.startActivity(Intent.createChooser(SongsUtil.createShareSongFileIntent(contentUris,context),null));
                    mySelection.clearSelection();
                    break;
                case R.id.bottomsheetAddPlaylistSelection:
                    ArrayList<Song> songsToAdd = new ArrayList<>();
                    for(Uri uri : mySelection.getSelection()) {
                        for (Folder folder : folders) {
                            if (folder.folderName.equals(uri.toString().replaceAll("\\d*$", ""))) {
                                songsToAdd.addAll(folder.getSongsInFolders());
                            }
                        }
                    }
                    final AddToPlaylistFragment addToPlaylistFragment = new AddToPlaylistFragment();
                    Bundle args = new Bundle();
                    args.putSerializable("songs", songsToAdd);
                    addToPlaylistFragment.setArguments(args);
                    ((MainActivity)context).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.playlist_frag_container, addToPlaylistFragment,addToPlaylistFragment.getClass().getSimpleName())
                            .commit();
                    bottomsheetPlaylistBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
                    break;
                case R.id.bottomsheetEditSelection:
                    ((MainActivity)context).findViewById(R.id.bottomsheetEditSelection).setVisibility(View.GONE);
                    break;
            }
        });
    }
}
