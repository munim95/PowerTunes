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
//import com.rigid.powertunes.selection.CustomSelectionTracker;
//import com.rigid.powertunes.selection.ItemDetailsLookup;
//import com.rigid.powertunes.selection.ItemKeyProvider;
import com.rigid.powertunes.songmodels.Album;
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

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.AlbumsViewHolder> {
    private List<Album> albumsList=new ArrayList<>();
    private Context context;
    private TextView sheetSelectionText;
    private BottomSheetBehavior bottomsheetBehaviour,bottomsheetPlaylistBehaviour,bottomsheetConfirmationBehaviour;
    private RecyclerView rv;
//    private CustomSelectionTracker<Uri> selectionTracker;
    private Selection mySelection;
    private TextView numOfSelection;
    private LinearLayout selectionLayout;
    private CheckBox checkBox;
    private ArrayList<Uri> songUriCollection;
    private int lastPos=-1;
    private boolean stopped;
    private SharedFragmentViewModel sharedFragmentViewModel;
    private MediaControllerCompat mediaControllerCompat;


    public AlbumsAdapter(Context context,View v) {
        this.context=context;
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
        sharedFragmentViewModel = ViewModelProviders.of((MainActivity)context).get(SharedFragmentViewModel.class);
    }
    private MediaControllerCompat.Callback callback(){
        return new MediaControllerCompat.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                super.onPlaybackStateChanged(state);
                if(lastPos!=-1) {
                    if (state.getState() == PlaybackStateCompat.STATE_PAUSED) {
                        albumsList.get(lastPos).setIsPlaying(false);
                        notifyItemChanged(lastPos);
                    } else {
                        albumsList.get(lastPos).setIsPlaying(true);
                        notifyItemChanged(lastPos);
                    }
                }
            }
        };
    }
    @NonNull
    @Override
    public AlbumsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bottomsheet_folder_item, parent, false);
        return new AlbumsViewHolder(v);
    }

    //diffutil
    private void updateSongList(List<Album> _albums) {
        final RecyclerViewFoldersDiffCallback diffCallback = new RecyclerViewFoldersDiffCallback(albumsList, _albums);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback, true);
        diffResult.dispatchUpdatesTo(this);
        albumsList.clear();
        albumsList.addAll(_albums);
    }
    public void swapAlbums(List<Album> albums) {
        updateSongList(albums);
        songUriCollection=new ArrayList<>();
        for (int i = 0; i <albums.size(); i++) {
            songUriCollection.add(Uri.parse(albums.get(i).albumName+i));
        }
        for(Album album:albums){
            if(album.isCurrentSelectedFolder()){
                lastPos=albums.indexOf(album);
                rv.scrollToPosition(lastPos);
                break;
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumsViewHolder holder, int position) {
        holder.bindTo(position);
    }

    @Override
    public int getItemCount() {
        return albumsList!=null?albumsList.size():0;
    }

    @Override
    public void onViewRecycled(@NonNull AlbumsViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.with(holder.itemView).clear(holder.imageView);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull AlbumsViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        Glide.with(holder.itemView)
                .load(albumsList.get(holder.getAdapterPosition()).getChildList().size()>0?albumsList.get(holder.getAdapterPosition()).getChildList().get(0).imageBytes:null)
                .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(context.getResources().getDimensionPixelSize(R.dimen.round_edges_dimen)))
                        .placeholder(R.mipmap.ic_launcher).skipMemoryCache(true))
                .into(holder.imageView);
    }

    //stuff to do when selection state changes
    private void onSelectionChangedTasks(){
        if(mySelection.hasSelection()){
            selectionLayout.setVisibility(View.VISIBLE);
            selectionLayout.animate().alpha(0.9f).setDuration(150L);
            numOfSelection.setText(String.format("%s/%s",
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
    }
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        rv=recyclerView;
        new GlobalSelectionTracker(context,recyclerView, this::onSelectionChangedTasks);
        mySelection=GlobalSelectionTracker.getMySelection();
//        new GlobalSelectionTracker("AlbumsAdapter", recyclerView,new MyDetailsLookup(recyclerView),
//                new MyItemKeyProvider(ItemKeyProvider.SCOPE_MAPPED), selectionObserver());
//        selectionTracker = GlobalSelectionTracker.getSelectionTracker();
        ((DiscreteScrollView)recyclerView).addScrollStateChangeListener(new DiscreteScrollView.ScrollStateChangeListener<RecyclerView.ViewHolder>() {
            @Override
            public void onScrollStart(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                if(!GlobalVariables.currentSelectedFolder.equals("")) {
                    if (!albumsList.get(i).isCurrentPlayingFolder()) {
                        TransitionManager.beginDelayedTransition(((AlbumsViewHolder) viewHolder).controlsHolder);
                        ((AlbumsViewHolder) viewHolder).controlsHolder.setVisibility(View.GONE);
                    }
                }else {
                    TransitionManager.beginDelayedTransition(((AlbumsViewHolder) viewHolder).controlsHolder);
                    ((AlbumsViewHolder) viewHolder).controlsHolder.setVisibility(View.GONE);
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
                if(((AlbumsViewHolder)viewHolder)!=null)
                    ((AlbumsViewHolder)viewHolder).controlsHolder.setVisibility(View.VISIBLE);
            });
    }

    public class AlbumsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView albumName;
//        private ItemDetails itemDetails;
        private RelativeLayout relativeLayout;
        private ImageView imageView;
        private FrameLayout controlsHolder;
        AlbumsViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
//            itemDetails=new ItemDetails();
            albumName=itemView.findViewById(R.id.bottomsheetItemName);
            imageView=itemView.findViewById(R.id.bottomsheetItemImage);
            relativeLayout=(RelativeLayout)itemView;
            controlsHolder=itemView.findViewById(R.id.bottomsheet_folders_controls_holder);
            controlsHolder.findViewById(R.id.bottomsheet_folders_play).setOnClickListener(playClickListener());
            controlsHolder.findViewById(R.id.bottomsheet_folders_pause).setOnClickListener(playClickListener());
        }
        private void bindTo(int position){
//            itemDetails.pos=position;
            Album album=albumsList.get(position);
            albumName.setText(album.albumName);
            if(mySelection.isSelected(albumsList.get(position).albumName+String.valueOf(position))) {
                relativeLayout.setSelected(true);
            } else{
                relativeLayout.setSelected(false);
            }
//            if(selectionTracker!=null) {
//                if(selectionTracker.isSelected(itemDetails.getSelectionKey())) {
//                    relativeLayout.setSelected(true);
//                } else{
//                    relativeLayout.setSelected(false);
//                }
//            }
            if(album.isCurrentSelectedFolder()) {
                controlsHolder.setVisibility(View.VISIBLE);
                if (album.isCurrentPlayingFolder()) {
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
            return albumsList.get(getAdapterPosition()).albumName+String.valueOf(getAdapterPosition());
        }
        @Override
        public void onClick(View v) {
                if (albumsList.size() != 0 && !mySelection.hasSelection()) {
                    //launch another frag containing playlist songs
                    rv.scrollToPosition(getLayoutPosition());
                    FoldersSongsFragment receivingFrag=new FoldersSongsFragment();
                    Bundle args = new Bundle();
                    args.putParcelable("folder", albumsList.get(getLayoutPosition()));
                    receivingFrag.setArguments(args);
                    ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainFragContainer, receivingFrag,receivingFrag.getClass().getSimpleName())
                            .commit();
                    ((MainActivity) context).getSupportFragmentManager().executePendingTransactions();
                    doStuffOnTransaction(albumsList.get(getLayoutPosition()).albumName);
                }
        }
        private View.OnClickListener playClickListener(){
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (albumsList.get(getAdapterPosition()).getChildList().size() > 0) {
                        if (lastPos != getAdapterPosition()) {
                            if (lastPos != -1)
                                notifyItemChanged(lastPos);
                            lastPos = getAdapterPosition();
                            albumsList.get(getAdapterPosition()).setIsPlaying(true);
                        }
                        if (v.getId() == R.id.bottomsheet_folders_play) {
                            controlsHolder.findViewById(R.id.bottomsheet_folders_play).setVisibility(View.GONE);
                            controlsHolder.findViewById(R.id.bottomsheet_folders_pause).setVisibility(View.VISIBLE);

                            long newID = Long.parseLong(albumsList.get(getAdapterPosition()).getChildList().get(0).metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
                            GlobalVariables.currentPosition=0;
                            GlobalVariables.currentSongId = newID;
                            GlobalVariables.shouldBePaused = false;
//                            long[] songIdArray = new long[albumsList.get(getAdapterPosition()).getChildList().size()];
//                            for (int i0 = 0; i0 < albumsList.get(getAdapterPosition()).getChildList().size(); i0++) {
//                                Song song = albumsList.get(getAdapterPosition()).getChildList().get(i0);
//                                songIdArray[i0] = song.id;
//                            }
//                            GlobalVariables.idArray = songIdArray;

//                            ((MainActivity) context).swapCardAdapterSongs((ArrayList<Song>) albumsList.get(getAdapterPosition()).getChildList());
                            sharedFragmentViewModel.setCurrentSongsOrder(albumsList.get(getAdapterPosition()).getChildList());
                            mediaControllerCompat.getTransportControls().stop();
                            mediaControllerCompat.getTransportControls().prepareFromMediaId(newID + "", null);
                            mediaControllerCompat.getTransportControls().play();
                            if(!stopped) {
                                makeToast(String.format("Started queue from '%s'", GlobalVariables.currentSelectedFolder));
                            }else{
                                stopped=false;
                                makeToast(String.format("Restarted queue from '%s'", GlobalVariables.currentSelectedFolder));
                            }
                            albumsList.get(getAdapterPosition()).setIsPlaying(true);
                        } else {
                            stopped=true;
                            controlsHolder.findViewById(R.id.bottomsheet_folders_pause).setVisibility(View.GONE);
                            controlsHolder.findViewById(R.id.bottomsheet_folders_play).setVisibility(View.VISIBLE);
                            mediaControllerCompat.getTransportControls().pause();
                            makeToast(String.format("Stopped queue from '%s'", GlobalVariables.currentSelectedFolder));
                            albumsList.get(getAdapterPosition()).setIsPlaying(false);
                        }
                    } else {
                        makeToast("Folder is empty...");
                    }
                }
            };
        }
        private void makeToast(String text){
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
//        private ItemDetails getItemDetails(){
//            return itemDetails;
//        }
    }
    private void doStuffOnTransaction(String text) {
        sheetSelectionText.setText(text);
        bottomsheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
    /**
     * selection tracker provider classes...
     * */
//    private class MyItemKeyProvider extends ItemKeyProvider<Uri> {
//
//        MyItemKeyProvider(int scope) {
//            super(scope);
//        }
//
//        @Nullable
//        @Override
//        public Uri getKey(int position) {
//            return Uri.parse(albumsList.get(position).albumName+String.valueOf(position));
//        }
//        @Override
//        public int getPosition(@NonNull Uri key) {
//            int pos = -1;
//            for(Album album : albumsList){
//                pos = pos+1;
//                if(album.albumName.equals(key.toString().replaceAll("\\d*$", ""))){
//                    break;
//                }
//            }
//            return pos;
//        }
//    }
//    class ItemDetails extends ItemDetailsLookup.ItemDetails<Uri> {
//        int pos;
//
//        ItemDetails(){}
//
//        @Override
//        public int getPosition() {
//            return pos;
//        }
//
//        @Nullable
//        @Override
//        public Uri getSelectionKey() {
//            return Uri.parse(albumsList.get(pos).albumName+String.valueOf(pos));
//        }
//    }
//    class MyDetailsLookup extends ItemDetailsLookup {
//        private RecyclerView mRecyclerView;
//
//        MyDetailsLookup(RecyclerView recyclerView) {
//            mRecyclerView = recyclerView;
//        }
//
//        public ItemDetails getItemDetails(MotionEvent e) {
//            View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
//            if (view != null) {
//                RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(view);
//                if (holder instanceof AlbumsViewHolder) {
//                    return ((AlbumsViewHolder) holder).getItemDetails();
//                }
//            }
//            return null;
//        }
//    }
//    private GlobalSelectionTracker.SelectionObserver selectionObserver() {
//        return new GlobalSelectionTracker.SelectionObserver() {
//            @Override
//            public void onItemStateChanged(@NonNull Object key, boolean selected) {
//                if(selectionTracker.hasSelection()){
//                    selectionLayout.setVisibility(View.VISIBLE);
//                    selectionLayout.animate().alpha(0.9f).setDuration(150L);
//                    numOfSelection.setText(String.format("%s/%s",
//                            selectionTracker.getSelection().size(), getItemCount()));
//                    if(selectionTracker.getSelection().size()!=getItemCount()){
//                        checkBox.setChecked(false);
//                    }else{
//                        checkBox.setChecked(true);
//                    }
//                }else{
//                    checkBox.setChecked(false);
//                    selectionLayout.animate().alpha(0).setDuration(150L).setListener(new Animator.AnimatorListener() {
//                        @Override
//                        public void onAnimationStart(Animator animation) {
//
//                        }
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            if(!selectionTracker.hasSelection())
//                                selectionLayout.setVisibility(View.GONE);
//                        }
//                        @Override
//                        public void onAnimationCancel(Animator animation) {
//
//                        }
//                        @Override
//                        public void onAnimationRepeat(Animator animation) {
//
//                        }
//                    });
//                }
//            }
//            @Override
//            public void onSelectionRefresh() {
//            }
//
//            @Override
//            public void onSelectionChanged() {
//                if(!selectionTracker.hasSelection()) {
//                    bottomsheetConfirmationBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
//                }
//            }
//
//            @Override
//            public void onSelectionRestored() {
//            }
//        };
//    }
    /**
     * Selection options buttons interface
     */
    private void initBottomSheetOptionsButtons(View v){
        selectionLayout=v.findViewById(R.id.bottomsheetSelectionLayout);
        numOfSelection=v.findViewById(R.id.bottomsheetNumOfSelection);
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
                        for(Album album : albumsList){
                            if(album.albumName.equals(uri.toString().replaceAll("\\d*$", ""))){
                                for(Song song : album.getChildList()){
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
                        for(Uri uri:mySelection.getSelection()) {
                            for (Album album : albumsList) {
                                if (album.albumName.equals(uri.toString().replaceAll("\\d*$", ""))) {
                                    for (Song song : album.getChildList()) {
                                        contentUris.add(Uri.parse(song.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)));
                                    }
                                }
                            }
                        }
                    }else {
                        Toast.makeText(context,"Exceeded max share limit of 30.",Toast.LENGTH_SHORT).show();
                    }
                    context.startActivity(Intent.createChooser(SongsUtil.createShareSongFileIntent(contentUris,context),null));
                    mySelection.clearSelection();
                    break;
                case R.id.bottomsheetAddPlaylistSelection:
                    ArrayList<Song> songsToAdd = new ArrayList<>();
                    for(Uri uri : mySelection.getSelection()) {
                        for (Album album : albumsList) {
                            if (album.albumName.equals(uri.toString().replaceAll("\\d*$", ""))) {
                                songsToAdd.addAll(album.getChildList());
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
