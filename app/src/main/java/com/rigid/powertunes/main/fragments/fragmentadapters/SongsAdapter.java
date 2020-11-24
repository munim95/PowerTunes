package com.rigid.powertunes.main.fragments.fragmentadapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.media.MediaMetadataCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.rigid.powertunes.GlobalVariables;
import com.rigid.powertunes.ItemTouchHelperAdapter;
import com.rigid.powertunes.OnStartDragListener;
import com.rigid.powertunes.Selection;
import com.rigid.powertunes.bottomsheetbehaviours.ConfirmationBottomSheet;
import com.rigid.powertunes.bottomsheetbehaviours.SelectionBottomSheet;
import com.rigid.powertunes.misc.GlobalSelectionTracker;
import com.rigid.powertunes.main.activities.MainActivity;
import com.rigid.powertunes.R;
import com.rigid.powertunes.misc.ResultReceiverCustom;
import com.rigid.powertunes.bottomsheetbehaviours.SelectionOptionsListener;
import com.rigid.powertunes.songmodels.Song;
import com.rigid.powertunes.util.SongsUtil;
import com.rigid.powertunes.util.Utils;
import com.rigid.powertunes.viewmodels.SharedFragmentViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongsViewHolder> implements ItemTouchHelperAdapter {
    /**
     * Displays songs as a singular songs adapter for all main frags
     * Handles highlighting items on song change and fragment change, selection and drag drop in queues
     **/
    //todo notify item changed DEBUG
    private static final String TAG=SongsAdapter.class.getSimpleName();
    //LiveData...
    private  ArrayList<Song> songList = new ArrayList<>();

    private Context context;
    private RecyclerView currentRv;
    private ResultReceiverCustom resultReceiverCustom = new ResultReceiverCustom(new Handler()),
            adapterReceiver = new ResultReceiverCustom(new Handler());
    private ArrayList<Uri> songUriCollection;
    private BottomSheetBehavior bottomsheetBehaviour,bottomSheetBehaviourConfirmation,bottomSheetPlaylist;

    private TextView numOfSelectionText;
//    private final ViewHolderListener viewHolderListener;
    private int playlistId;
    private int height=-1;
    private int currPos=-1;
    private int lastPos=-1;
    private long currentId=-1;

    private CheckBox checkBox;
    private SharedFragmentViewModel sharedFragmentViewModel;
    private Fragment currFrag;
    private OnStartDragListener mDragStartListener;
    private Selection mySelection;
    private AsyncListDiffer<Song> asyncListDiffer;

    public SongsAdapter(Context ctx, Fragment fragment,int playlistId,SharedFragmentViewModel sharedViewModel){
        super();
        context=ctx;
        setHasStableIds(true);
        asyncListDiffer=new AsyncListDiffer<>(this, new DiffUtil.ItemCallback<Song>() {
            @Override
            public boolean areItemsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
                return Long.parseLong(oldItem.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)) ==
                        Long.parseLong(newItem.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
            }

            @Override
            public boolean areContentsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
                return oldItem.data.equals(newItem.data);
            }
        });

        if(fragment instanceof OnStartDragListener)
        mDragStartListener=(OnStartDragListener)fragment;
//        ((MainActivity)ctx).setMetaDataChangeObserver(metaDataChangeObserver());
//        viewHolderListener = new ViewHolderListenerImpl(fragment);
//        ((MainActivity) context).setPlayStateChangeListener(playStateListener());
        sharedFragmentViewModel = sharedViewModel;
        //being removed as observer in mainactivity when fragments are destroyed
//        sharedFragmentViewModel.getMediaMetaDataCompat().removeObserver(metadataChangeObserver());
//        sharedFragmentViewModel.getMediaMetaDataCompat().observe(fragment,metadataChangeObserver());

        resultReceiverCustom.setReceiver(((MainActivity)ctx));
        adapterReceiver.setReceiver(receiver());

        //selection setters and stuff dependent...
        checkBox=((MainActivity)ctx).findViewById(R.id.allSelect);
        checkBox.setOnCheckedChangeListener(allCheckListener());
        ((MainActivity)ctx).setSelectionOptionsListener(selectionOptionsListener());
        RelativeLayout layout = ((MainActivity)ctx).findViewById(R.id.selection_bottomsheet);
        bottomsheetBehaviour = SelectionBottomSheet.from(layout);
        numOfSelectionText = ((MainActivity)ctx).findViewById(R.id.numberOfSelect);
        RelativeLayout confirmDialog = ((MainActivity)ctx).findViewById(R.id.delConfirmationBottomSheet);
        bottomSheetBehaviourConfirmation = ConfirmationBottomSheet.from(confirmDialog);
        RelativeLayout playlistLayout = ((MainActivity)ctx).findViewById(R.id.new_playlist_sheet);
        bottomSheetPlaylist = ConfirmationBottomSheet.from(playlistLayout);

        this.playlistId=playlistId;
        currFrag=fragment;
    }

    //diffutil
    //refine the method below keeping larger data sets in mind--> threading
    private void updateSongList(ArrayList<Song> songs){
        asyncListDiffer.submitList(songs);
        songList=songs;
//        final RecyclerViewDiffCallback diffCallback = new RecyclerViewDiffCallback(songList,songs);
//        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback,true);
//        diffResult.dispatchUpdatesTo(this);
//        songList.clear();
//        songList.addAll(songs);
    }
    public void swapSongList(ArrayList<Song> songs) {
        //to stop from updating while queue is dragged
        if(justDragged){
            justDragged=false;
            return;
        }
        //get curr id and curr pos from mainactivity
        updateSongList(songs);

        sendUpdateToMainActivity(); //1 - request data from main (adapter -> main -> media browser -> adapter)
//        sharedFragmentViewModel.getMediaMetaDataCompat().observe(currFrag, metadataChangeObserver());
        //get uri for all songs selection
        songUriCollection = new ArrayList<>();
        for (int i = 0; i < songs.size(); i++) {
            Song song = songs.get(i);
            songUriCollection.add(Uri.parse(song.data));
        }
    }
    private void sendUpdateToMainActivity(){
        Bundle bundle = new Bundle();
        bundle.putParcelable("adapterreceiver",adapterReceiver);
        resultReceiverCustom.send(GlobalVariables.ON_ADAPTER_UPDATE,bundle);
    }
    private HandlerThread handlerThread; //to loop through song list in bg
    private Handler backgroundHandler;
    private Handler mainHandler;
    //2-receive data from main
    private ResultReceiverCustom.Receiver receiver() {
        //if current playlist is the same as the rv songs list, then use current position as normal, otherwise use songs id to determine the position
        //current != -1 if not same
        //todo once position is determined based on current id, proceed to create a list of songs using the window pane algorithm
        return (resultCode, resultData) -> {
            if(resultCode==GlobalVariables.ON_ADAPTER_UPDATE) {
                currentId=resultData.getLong("currentid",-1);
                if(currentId!=-1){
                    if(handlerThread==null)
                        handlerThread = new HandlerThread("SongsAdapterThread");
                    if(!handlerThread.isAlive())
                        handlerThread.start();
                    if(backgroundHandler == null)
                        backgroundHandler = new Handler(handlerThread.getLooper());
                    backgroundHandler.post(()-> {
                        for(Song song : asyncListDiffer.getCurrentList()) {
                            if(song._id==currentId) {
                                currPos=asyncListDiffer.getCurrentList().indexOf(song);
                                currentId=-1;
                                break;
                            }
                        }
                        if(mainHandler==null)
                            mainHandler= new Handler(Looper.getMainLooper());
                        mainHandler.post(()->
                                notifyAndAdjustCurrentSong(currPos)
                        );
                    });
                } else {
                    if(handlerThread!=null&&handlerThread.isAlive()) {
                        handlerThread.quit();
                        mainHandler=null;
                        backgroundHandler =null;
                    }
                    currPos = resultData.getInt("currentposition",-1);
                    notifyAndAdjustCurrentSong(currPos);
                }
            }
        };
    }
//    private MetaDataChangeObserver metaDataChangeObserver(){
//        return mediaMetadataCompat -> {
//            notifyAndAdjustCurrentSong(Long.parseLong(mediaMetadataCompat.getDescription().getMediaId()));
//        };
//    }
    private void notifyForSongChange(int curr, int last){
        if (last != -1)
            notifyItemChanged(last);
        notifyItemChanged(curr);
    }
    //for updating UI with current song, not to be confused with current position as this is only for the frag its in rather than currently playing list
    private void notifyAndAdjustCurrentSong(int currPos){
            //CHANGE CONSIDERING LARGE DATASETS - PERFOMANCE AFFECTED
//            currPos = asyncListDiffer.getCurrentList().indexOf(MediaLibrary.getSongIdSparseArray().get(id));
//            sharedFragmentViewModel.setCurrentPosition(currPos);
            if (currPos != -1) {
                notifyForSongChange(currPos,lastPos);
//                currentRv.post(() -> currentRv.smoothScrollToPosition(currPos));
//                if (!currentRv.isComputingLayout()) {
                boolean isInBetween = (currPos <= ((LinearLayoutManager) currentRv.getLayoutManager()).findLastVisibleItemPosition()
                        && currPos >= ((LinearLayoutManager) currentRv.getLayoutManager()).findFirstVisibleItemPosition());
                if (!isInBetween) {
                    if(currPos>Math.round(getItemCount()/4f))
                        currentRv.scrollToPosition(currPos);
                    else
                        currentRv.smoothScrollToPosition(currPos);
                } else {
                    //this is needed since smoothScrollToPosition() glitches for offscreen items interfering with item animations
                    if (currPos > lastPos) {
                        //going down
                        if (currPos
                                >= ((LinearLayoutManager) currentRv.getLayoutManager()).findLastCompletelyVisibleItemPosition()) {
                            currentRv.smoothScrollBy(0, height);
                        }
                    } else {
                        //going up
                        if (currPos
                                <= ((LinearLayoutManager) currentRv.getLayoutManager()).findFirstCompletelyVisibleItemPosition()) {
                            currentRv.smoothScrollBy(0, -height);
                        }
                    }
                }
                lastPos = currPos;
//                }
            }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return asyncListDiffer.getCurrentList().size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        currentRv=recyclerView;
        setUpSelection(context,recyclerView);
//        preLoadImagesGlide(context,recyclerView);
    }
//    private void preLoadImagesGlide(Context context, RecyclerView recyclerView, View image){
//        ListPreloader.PreloadSizeProvider sizeProvider =
//                new ViewPreloadSizeProvider(image);
//        ListPreloader.PreloadModelProvider modelProvider = new MyPreloadModelProvider();
//        RecyclerViewPreloader<byte[]> preloader =
//                new RecyclerViewPreloader<>(Glide.with(context), modelProvider, sizeProvider,5);
//        recyclerView.addOnScrollListener(preloader);
//    }
    private void setUpSelection(Context context,RecyclerView recyclerView){
        //enable selection
        new GlobalSelectionTracker(context,recyclerView,
                () -> {
                    if(mySelection.hasSelection()){
                        Utils.lockScreenOrientation(context,true);
                        if(mySelection.getSelection().size()!=getItemCount()){
                            checkBox.setChecked(false);
                        }else{
                            checkBox.setChecked(true);
                        }
                    }else{
                        Utils.lockScreenOrientation(context,false);
                    }
                    numOfSelectionText.setText(String.format("%s/%s",
                            mySelection.getSelection().size(), getItemCount()));
                    if(bottomsheetBehaviour.getState()!=BottomSheetBehavior.STATE_SETTLING) {
                        ((SelectionBottomSheet) bottomsheetBehaviour)
                                .setOptionsMenuVisible(mySelection.hasSelection(), currentRv,playlistId!=-1);
                        if(!mySelection.hasSelection()) {
                            bottomSheetBehaviourConfirmation.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            bottomSheetPlaylist.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        }
                    }
                });
        mySelection = GlobalSelectionTracker.getMySelection();
    }
    //preloads bitmaps for smoother scrolling experience
//    private class MyPreloadModelProvider implements ListPreloader.PreloadModelProvider<byte[]>{
//        @NonNull
//        @Override
//        public List<byte[]> getPreloadItems(int position) {
//            if(songList.get(position).imageBytes==null){
//                return Collections.emptyList();
//            }
//            return Collections.singletonList(songList.get(position).imageBytes);
//        }
//        @Nullable
//        @Override
//        public RequestBuilder getPreloadRequestBuilder(@NonNull byte[] item) {
//            return Glide.with(context)
//                    .load(item)
//                    .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(10))
//                            .placeholder(R.mipmap.ic_launcher).skipMemoryCache(true));
//        }
//
//    }

    private CompoundButton.OnCheckedChangeListener allCheckListener(){
        return (buttonView, isChecked) -> {
            if(isChecked){
                mySelection.setItemsSelected(songUriCollection, true);
            }else{
                if(mySelection.getSelection().size()==getItemCount()) {
                    mySelection.setItemsSelected(songUriCollection, false);
                }
            }
        };
    }

    @NonNull
    @Override
    public SongsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.songs_item_list, parent, false);
        if(currFrag.getClass().getSimpleName().equalsIgnoreCase("queuefragment")){
            v.findViewById(R.id.songItemDragHandle).setVisibility(View.VISIBLE);
        }else{
            v.findViewById(R.id.songItemDragHandle).setVisibility(View.GONE);
        }
        return new SongsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SongsViewHolder holder, int position) {
        //handles bindings
        height=holder.itemView.getHeight();
        holder.bindTo(position);
    }

    @Override
    public void onBindViewHolder(@NonNull SongsViewHolder holder, int position, @NonNull List<Object> payloads) {
        if(payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        }else {
            Song item = asyncListDiffer.getCurrentList().get(position);
            if ((int)payloads.get(0)==1) {
                //handle for song changed and selection separately
                if (Long.parseLong(item.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)) == currentId) {
                    holder.itemView.setActivated(true);
                } else {
                    holder.itemView.setActivated(false);
                }
            }else{
                if (mySelection.isSelected(item.data)) {
                    holder.itemView.setSelected(true);
                } else {
                    holder.itemView.setSelected(false);
                }
            }
        }
    }
    @Override
    public void onViewRecycled(@NonNull SongsViewHolder holder) {
        Glide.with(context).clear(holder.songItemImage);
    }
    @Override
    public void onViewAttachedToWindow(@NonNull SongsViewHolder holder) {
        //todo seperate thread, set data src causes lag in scrolling
//        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
//        mediaMetadataRetriever.setDataSource(asyncListDiffer.getCurrentList().get(holder.getAdapterPosition()).data);
//        Glide.with(context)
//                .load(mediaMetadataRetriever.getEmbeddedPicture())
////                .listener(new RequestListener<Drawable>() {
////                    @Override
////                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
////                        viewHolderListener.onLoadCompleted(holder.itemDetails.pos);
////                        return false;
////                    }
////                    @Override
////                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
////                        viewHolderListener.onLoadCompleted(holder.itemDetails.pos);
////                        return false;
////                    }
////                })
//                .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(10))
//                        .placeholder(R.mipmap.ic_launcher))
//                .into(holder.songItemImage);
//        mediaMetadataRetriever.release();
    }
    public class SongsViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener {
        private ImageView songItemImage,songItemDragHandle;
        private TextView songItemName, songItemArtistName, songItemAlbumName;

        SongsViewHolder(View v) {
            super(v);
            songItemImage = v.findViewById(R.id.songItemImage);
            songItemArtistName = v.findViewById(R.id.songItemArtistName);
            songItemAlbumName=v.findViewById(R.id.songItemAlbumName);
            songItemName = v.findViewById(R.id.songItemName);
            songItemDragHandle=v.findViewById(R.id.songItemDragHandle);
//            songItemDragHandle.setOnTouchListener(dragTouch());
            v.setOnTouchListener(this);
        }
        private void bindTo(int pos){
            Song songItem = asyncListDiffer.getCurrentList().get(pos);
//            itemDetails.pos=pos;
            //keep state
            if(mySelection.hasSelection()) {
                if (mySelection.isSelected(songItem.data)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
            }
            //if playing
            if (getAdapterPosition() == currPos) {
                itemView.setActivated(true);
            } else {
                itemView.setActivated(false);
            }
            songItemName.setText(songItem.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            String artistName = songItem.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
            String albumName = songItem.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
            songItemArtistName.setText(artistName.equalsIgnoreCase("<unknown>") ||
                    artistName.isEmpty() ?
                    context.getString(R.string.unknown_artist) : artistName);
            songItemAlbumName.setText(albumName.equalsIgnoreCase("<unknown>") ||
                    albumName.equalsIgnoreCase("") ? " - " +
                    context.getString(R.string.unknown_album) : " - " + albumName);

////            songItemImage.setTransitionName(String.valueOf(songItem.id));
        }

        public String getSongUri(){
            return songList.get(getAdapterPosition()).data;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
//                itemDetails.pos=getLayoutPosition();
                if (currFrag.getClass().getSimpleName().equalsIgnoreCase("queuefragment")) {
                    if (event.getX() > songItemDragHandle.getX()) {
                        //disable selection while dragging
                        mDragStartListener.onStartDrag(SongsViewHolder.this);
                    }
                }
            }
            return false;
        }
    }

    public Selection getSelectionTracker(){
        return mySelection;
    }
    @Override
    public long getItemId(int position){
        return asyncListDiffer.getCurrentList().size()!=0?asyncListDiffer.getCurrentList().get(position)._id:-1;
    }
    /**
     * Swipe to dismiss and drag (for queues only)
     **/
    private Bundle bundle = new Bundle();
    private boolean notify = false;

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        //change id array order
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(songList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(songList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition,toPosition);
        notify=true;
        bundle.putBoolean("notifyqueue",true);
        bundle.putInt("from",fromPosition);
        bundle.putInt("to",toPosition);
        if(fromPosition==currPos){
            bundle.putBoolean("isplaying",true);
            resultReceiverCustom.send(1000,bundle);
            sharedFragmentViewModel.setCurrentPosition(currPos);
        } else {
            bundle.putBoolean("isplaying", false);
            resultReceiverCustom.send(1000,bundle);
        }
//        bundle.putInt("from",fromPosition);
//        bundle.putInt("to",toPosition);
//        resultReceiverCustom.send(1000,bundle);
    }
    @Override
    public void onItemDismiss(int position) {
        notify=false;
        songList.remove(position);
        //remove from queue
        notifyItemRemoved(position);
        bundle.putInt("removeposition",position);
        bundle.putBoolean("notifyqueue",false);
        resultReceiverCustom.send(1000,bundle);
    }
    private boolean justDragged = false;
    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder holder,int actionState) { }
    /**
     * Selection options buttons interface
     */
    private SelectionOptionsListener selectionOptionsListener() {
        return new SelectionOptionsListener() {
            @Override
            public void closeSelect() {
                mySelection.clearSelection();
                ((SelectionBottomSheet) bottomsheetBehaviour)
                        .setOptionsMenuVisible(false,currentRv,false);
            }

            //option to remove or delete for playlist
            @Override
            public void deleteSelect(boolean hasRemove) {
//                ArrayList<Song> songsCopy = new ArrayList<>(songList);
//                String[] paths = new String[mySelection.getSelection().size()];
//                ArrayList<PlaylistSong> playlistSongs=new ArrayList<>(mySelection.getSelection().size());
//                int count = 0;
//                for (Uri uri : mySelection.getSelection()) {
//                    for (Song song : songList) {
//                        if ((song.data).equals(uri.getPath())) {
//
//                            paths[count++] = song.data;
//                            //if playlist
//                            if(playlistId!=-1){
//                                playlistSongs.add(new PlaylistSong(song.id,
//                                        song.genre,
//                                        song.title,
//                                        song.artistName,
//                                        song.albumName,
//                                        song.duration,
//                                        song.trackNumber,
//                                        song.dateAdded,
//                                        playlistId,
//                                        song.data,
//                                        song.contentUri,
//                                        song.imageBytes));
//                            }
//                        }
//                    }
//                }
//                if(hasRemove){
//                    //remove from playlist
//                    if(PlaylistUtil.removeSongsInPlaylist(context,playlistSongs,playlistId)){
//                        Toast.makeText(context,"Successfully removed song.",Toast.LENGTH_SHORT).show();
//                    }else{
//                        Toast.makeText(context,"Unexpected Error: Could not remove song.",Toast.LENGTH_SHORT).show();
//                    }
//                    for(Song song : songList) {
//                        for (PlaylistSong playlistSong : playlistSongs) {
//                            if(song.id==playlistSong.id) {
//                                songsCopy.remove(song);
//                            }
//                        }
//                    }
//                    updateSongList(songsCopy);
//                }else {
//                    int rows = context.getContentResolver().delete(MediaStore.Files.getContentUri("external"),
//                                    Utils.makeSQLSelectionArgs("",MediaStore.Files.FileColumns.DATA,paths.length),
//                                    paths);
//
//                    if(rows!=-1 && rows!= 0) {
//                        if(rows == paths.length) {
//                            resultReceiverCustom.send(10,null);
//                            Toast.makeText(context, "Deleted all songs successfully.", Toast.LENGTH_SHORT).show();
//                        } else{
//                            Toast.makeText(context, String.format("Deleted %s/%s songs.",rows,paths.length), Toast.LENGTH_SHORT).show();
//                        }
//                    } else if(rows==0){
//                        Toast.makeText(context, "Could not delete songs.", Toast.LENGTH_SHORT).show();
//                        //todo force delete file.delete()
//                    }
                    //delete
//                //delete and get deleted paths
//                    ArrayList<String> s = FileUtil.deleteSongs(context, paths);
//                    int c = paths.length - s.size(); //0 - all deleted
//                    count = 0;
//                    int[] deletedPositions = new int[s.size()];
//                    if (c == 0) {
//                        //update data normally
//                        for (Song song : songList) {
//                            for (String s1 : s) {
//                                if (song.data.equals(s1)) {
//                                    songsCopy.remove(song);
//                                    deletedPositions[count++] = songList.indexOf(song);
//                                }
//                            }
//                        }
//                    } else if (c > 0 && c < paths.length) {
//                        //some failed, dont update the songs that failed
//                        for (Song song : songList) {
//                            for (String s1 : s) {
//                                if (song.data.equals(s1)) {
//                                    songsCopy.remove(song);
//                                    deletedPositions[count++] = songList.indexOf(song);
//                                }
//                            }
//                        }
//                    }
//                    updateSongList(songsCopy);
                    //inform media service so it can update media player with the next song
//                    Bundle bundle = new Bundle();
//                    bundle.putIntArray("deletedpositions", deletedPositions);
                    //todo delete check
//                    MainActivity.getMediaControllerCompat().sendCommand("updatedeleted", bundle, resultReceiverCustom);
                    //update all view models and components
//                    ((MainActivity) context).swapCardAdapterSongs(songsCopy);
//                }
                mySelection.clearSelection();
            }
            @Override
            public void addToPlaylistSelect() {
                //N/A for playlist
            }

            @Override
            public void addToQueueSelect() {
            }

            @Override
            public void albumArtSelect() {
                mySelection.clearSelection();
            }

            @Override
            public void shareSelect() {
                //open share menu
                ArrayList<Uri> contentUris = new ArrayList<>(mySelection.getSelection().size());
                for(Uri uri:mySelection.getSelection()){
                    for(Song song:songList){
                        if((song.data).equals(uri.getPath())){
//                            contentUris.add(Uri.parse(song.contentUri));
                        }
                    }
                }
                context.startActivity(Intent.createChooser(SongsUtil.createShareSongFileIntent(contentUris,context),null));
                mySelection.clearSelection();
            }

            @Override
            public void editInforSelect() {
            mySelection.clearSelection();
            }
        };
    }

//    private GlobalSelectionTracker.SelectionObserver selectionObserver() {
//        return new GlobalSelectionTracker.SelectionObserver() {
//            @Override
//            public void onItemStateChanged(@NonNull Object key, boolean selected) {
//                if(selectionTracker.hasSelection()){
//                    ((MainActivity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
//                    if(selectionTracker.getSelection().size()!=getItemCount()){
//                        checkBox.setChecked(false);
//                    }else{
//                        checkBox.setChecked(true);
//                    }
//                }else{
//                    ((MainActivity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
//                }
//                numOfSelectionText.setText(String.format("%s/%s",
//                        selectionTracker.getSelection().size(), getItemCount()));
//
//
//            }
//            @Override
//            public void onSelectionChanged() {
//                if(bottomsheetBehaviour.getState()!=BottomSheetBehavior.STATE_SETTLING) {
//                    ((SelectionBottomSheet) bottomsheetBehaviour)
//                            .setOptionsMenuVisible(selectionTracker.hasSelection(), currentRv,playlistId!=-1);
//                    if(!selectionTracker.hasSelection()) {
//                        bottomSheetBehaviourConfirmation.setState(BottomSheetBehavior.STATE_COLLAPSED);
//                        bottomSheetPlaylist.setState(BottomSheetBehavior.STATE_COLLAPSED);
//                    }
//                }
//            }
//            //not used
//            @Override
//            public void onSelectionRefresh() {}
//            @Override
//            public void onSelectionRestored() {}
//        };
//    }



}
