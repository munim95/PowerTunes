package com.rigid.powertunes.main.fragments.fragmentadapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.rigid.powertunes.GlobalVariables;
import com.rigid.powertunes.R;
import com.rigid.powertunes.main.activities.MainActivity;
import com.rigid.powertunes.bottomsheetbehaviours.MainBottomSheet;
import com.rigid.powertunes.main.fragments.FoldersSongsFragment;
import com.rigid.powertunes.misc.GlideViewTargetSub;
import com.rigid.powertunes.songmodels.Album;
import com.rigid.powertunes.songmodels.Artist;
import com.rigid.powertunes.songmodels.Folder;
import com.rigid.powertunes.songmodels.Genre;
import com.rigid.powertunes.songmodels.Playlist;
import com.rigid.powertunes.songmodels.Song;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int HEADER = 0;
    private final int ALBUM = 1;
    private final int ARTIST = 2;
    private final int SONG = 3;
    private final int GENRE = 4;
    private final int PLAYLIST = 5;
    private final int FOLDER = 6;

    private Context context;
    private List<Object> dataSet;
    private TextView sheetSelectionText;
    private BottomSheetBehavior bottomsheetBehaviour;
    private long currentId=-1;
    private MediaControllerCompat mediaControllerCompat;

    public SearchAdapter(Context context, Fragment fragment, List<Object> dataSet) {
        this.context=context;
        this.dataSet=dataSet;
        sheetSelectionText=((MainActivity)context).findViewById(R.id.currentSelectionText);
        RelativeLayout layout = ((MainActivity)context).findViewById(R.id.mainBottomsheetLayout);
        bottomsheetBehaviour = MainBottomSheet.from(layout);
        mediaControllerCompat=((MainActivity)context).getMediaControllerCompat();
        if(mediaControllerCompat!=null)
        mediaControllerCompat.registerCallback(callback());
//        SharedFragmentViewModel sharedFragmentViewModel = ViewModelProviders.of((MainActivity) context).get(SharedFragmentViewModel.class);
//        sharedFragmentViewModel.getMediaMetaDataCompat().observe(fragment, mediaMetadataCompat -> {
//            currentId=Long.parseLong(mediaMetadataCompat.getDescription().getMediaId());
//            notifyDataSetChanged();
//        });
    }
    private MediaControllerCompat.Callback callback() {
        return new MediaControllerCompat.Callback() {
            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) {
                super.onMetadataChanged(metadata);
                currentId=Long.parseLong(metadata.getDescription().getMediaId());
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getItemViewType(int position) {
        if (dataSet.get(position) instanceof Album) return ALBUM;
        if (dataSet.get(position) instanceof Artist) return ARTIST;
        if (dataSet.get(position) instanceof Song) return SONG;
        if (dataSet.get(position) instanceof Genre) return GENRE;
        if (dataSet.get(position) instanceof Playlist) return PLAYLIST;
        if (dataSet.get(position) instanceof Folder) return FOLDER;
        return HEADER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType){
            case SONG:
                View v1 =inflater.inflate(R.layout.songs_item_list,parent,false);
                vh= new SongVh(v1);
                break;
            case ALBUM:
                View v2 = inflater.inflate(R.layout.bottomsheet_folder_item,parent,false);
                vh= new SongFoldersVh(v2);
                break;
            case ARTIST:
                View v3 =inflater.inflate(R.layout.bottomsheet_folder_item,parent,false);
                vh= new SongFoldersVh(v3);
                break;
            case GENRE:
                View v4 =inflater.inflate(R.layout.bottomsheet_folder_item,parent,false);
                vh= new SongFoldersVh(v4);
                break;
            case PLAYLIST:
                View v5 =inflater.inflate(R.layout.bottomsheet_folder_item,parent,false);
                vh= new SongFoldersVh(v5);
                break;
            case FOLDER:
                View v6 =inflater.inflate(R.layout.bottomsheet_folder_item,parent,false);
                vh= new SongFoldersVh(v6);
                break;
            default:
                View v0 =inflater.inflate(R.layout.search_header_item,parent,false);
                vh= new HeaderVh(v0);
                break;
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()){
            case SONG:
                ((SongVh)holder).bindTo(dataSet.get(position));
                break;
            case ALBUM:
                ((SongFoldersVh)holder).bindTo(dataSet.get(position));
                break;
            case ARTIST:
                ((SongFoldersVh)holder).bindTo(dataSet.get(position));
                break;
            case GENRE:
                ((SongFoldersVh)holder).bindTo(dataSet.get(position));
                break;
            case PLAYLIST:
                ((SongFoldersVh)holder).bindTo(dataSet.get(position));
                break;
            case FOLDER:
                ((SongFoldersVh)holder).bindTo(dataSet.get(position));
                break;
            default:
                //sub title
                ((HeaderVh)holder).bindTo(dataSet.get(position).toString());
                break;
        }
    }

    public void swapData(List<Object> data){
        dataSet = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if(holder instanceof SongVh) {
            Glide.with(holder.itemView).clear(((SongVh) holder).songItemImage);
            ((SongVh) holder).songItemImage.invalidate();
        }else if(holder instanceof SongFoldersVh){
            Glide.with(holder.itemView).clear(((SongFoldersVh) holder).imageView);
            ((SongFoldersVh) holder).imageView.invalidate();
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if(holder instanceof SongVh){
            Glide.with(holder.itemView)
                    .load(((Song)dataSet.get(holder.getAdapterPosition())).imageBytes)
                    .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(10))
                            .placeholder(R.mipmap.ic_launcher).skipMemoryCache(true))
                    .into(new GlideViewTargetSub(((SongVh) holder).songItemImage));
        }else if(holder instanceof SongFoldersVh){
            RequestOptions requestOptions=new RequestOptions().transforms(new CenterCrop(),
                    new RoundedCorners(context.getResources().getDimensionPixelSize(R.dimen.round_edges_dimen)))
                    .placeholder(R.mipmap.ic_launcher).skipMemoryCache(true);
            if(dataSet.get(holder.getAdapterPosition()) instanceof Album) {
                Glide.with(holder.itemView)
                        .load(((Album)dataSet.get(holder.getAdapterPosition())).getChildList().get(0).imageBytes)
                        .apply(requestOptions)
                        .into(new GlideViewTargetSub(((SongFoldersVh) holder).imageView));
            }
            if(dataSet.get(holder.getAdapterPosition()) instanceof Artist) {
                Glide.with(holder.itemView)
                        .load(((Artist)dataSet.get(holder.getAdapterPosition())).getChildList().get(0).imageBytes)
                        .apply(requestOptions)
                        .into(new GlideViewTargetSub(((SongFoldersVh) holder).imageView));
            }
            if(dataSet.get(holder.getAdapterPosition()) instanceof Genre) {
                Glide.with(holder.itemView)
                        .load(((Genre)dataSet.get(holder.getAdapterPosition())).getChildList().get(0).imageBytes)
                        .apply(requestOptions)
                        .into(new GlideViewTargetSub(((SongFoldersVh) holder).imageView));
            }
            if(dataSet.get(holder.getAdapterPosition()) instanceof Playlist) {
                Glide.with(holder.itemView)
                        .load(((Playlist)dataSet.get(holder.getAdapterPosition())).getChildList().get(0).imageBytes)
                        .apply(requestOptions)
                        .into(new GlideViewTargetSub(((SongFoldersVh) holder).imageView));
            }
            if(dataSet.get(holder.getAdapterPosition()) instanceof Folder) {
                Glide.with(holder.itemView)
                        .load(((Folder)dataSet.get(holder.getAdapterPosition())).getSongsInFolders().get(0).imageBytes)
                        .apply(requestOptions)
                        .into(new GlideViewTargetSub(((SongFoldersVh) holder).imageView));
            }
        }
    }

    class HeaderVh extends RecyclerView.ViewHolder {
        private TextView title;
        HeaderVh(@NonNull View itemView) {
            super(itemView);
            title=itemView.findViewById(R.id.headerTitle);
        }
        private void bindTo(String txt){
            title.setText(txt);
        }
    }
    class SongVh extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView songItemImage;
        private TextView songItemName, songItemArtistName, songItemAlbumName;
        private RelativeLayout relativeLayout;
        SongVh(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            songItemImage = itemView.findViewById(R.id.songItemImage);
            songItemArtistName = itemView.findViewById(R.id.songItemArtistName);
            songItemAlbumName=itemView.findViewById(R.id.songItemAlbumName);
            songItemName = itemView.findViewById(R.id.songItemName);
            relativeLayout=(RelativeLayout)itemView;
        }
        private void bindTo(Object obj){
            songItemName.setText(((Song)obj).metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            String artistName=((Song)obj).metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
            String albumName=((Song)obj).metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
            songItemArtistName.setText(artistName.equalsIgnoreCase("<unknown>")||
                    artistName.isEmpty() ?
                    context.getString(R.string.unknown_artist) :artistName);
            songItemAlbumName.setText(" - "+(albumName.equalsIgnoreCase("<unknown>")||albumName.isEmpty()?
                    context.getString(R.string.unknown_album):
                    albumName));

            if(currentId!=-1) {
                if (Long.parseLong(((Song) obj).metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)) == currentId) {
                    relativeLayout.setActivated(true);
                } else {
                    relativeLayout.setActivated(false);
                }
            }
        }

        @Override
        public void onClick(View v) {
            //set according to all songs order
            long newID = Long.parseLong(((Song)dataSet.get(getAdapterPosition())).metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
//            GlobalVariables.currentPosition=findCurrentSongPositionForId(MediaLibrary.getTotalSongsList(),newID);
            if (newID != GlobalVariables.currentSongId) {
                GlobalVariables.currentSongId=newID;
                GlobalVariables.shouldBePaused=false;

                mediaControllerCompat.getTransportControls().stop();
                mediaControllerCompat.getTransportControls().prepareFromMediaId(newID + "", null);
                mediaControllerCompat.getTransportControls().play();

            } else {
                mediaControllerCompat.getTransportControls().play();
            }
        }
    }
    class SongFoldersVh extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title;
        private ImageView imageView;
        private RequestOptions requestOptions;

        SongFoldersVh(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            title=itemView.findViewById(R.id.bottomsheetItemName);
            imageView=itemView.findViewById(R.id.bottomsheetItemImage);
            requestOptions=new RequestOptions().transforms(new CenterCrop(),
                    new RoundedCorners(context.getResources().getDimensionPixelSize(R.dimen.round_edges_dimen)))
                    .placeholder(R.mipmap.ic_launcher).skipMemoryCache(true);
        }
        private void bindTo(Object obj){
            if(obj instanceof Album) {
                title.setText(((Album) obj).albumName);
            }
            if(obj instanceof Artist) {
                title.setText(((Artist)obj).artist);
            }
            if(obj instanceof Genre) {
                title.setText(((Genre)obj).genreName);
            }
            if(obj instanceof Playlist) {
                title.setText(((Playlist)obj).name);
            }
            if(obj instanceof Folder) {
                title.setText(((Folder)obj).folderName);
            }
        }

        @Override
        public void onClick(View v) {
            final FoldersSongsFragment receivingFrag;
            final Bundle args;
            switch (getItemViewType()){
                case ALBUM:
                    receivingFrag=new FoldersSongsFragment();
                    args=new Bundle();
                    args.putParcelable("foldersongs", ((Album)dataSet.get(getAdapterPosition())));
                    args.putInt("isplaylist",-1);
                    receivingFrag.setArguments(args);
                    ((MainActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.mainFragContainer, receivingFrag).commitNow();
                    doStuffOnTransaction(((Album)dataSet.get(getAdapterPosition())).albumName);
                    break;
                case ARTIST:
                    receivingFrag=new FoldersSongsFragment();
                    args=new Bundle();
                    args.putParcelable("foldersongs", ((Artist)dataSet.get(getAdapterPosition())));
                    args.putInt("isplaylist",-1);
                    receivingFrag.setArguments(args);
                    ((MainActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.mainFragContainer, receivingFrag).commitNow();
                    doStuffOnTransaction(((Artist)dataSet.get(getAdapterPosition())).artist);
                    break;
                case GENRE:
                    receivingFrag=new FoldersSongsFragment();
                    args = new Bundle();
                    args.putParcelable("foldersongs", ((Genre)dataSet.get(getAdapterPosition())));
                    args.putInt("isplaylist",-1);
                    receivingFrag.setArguments(args);
                    ((MainActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.mainFragContainer, receivingFrag).commitNow();
                    doStuffOnTransaction(((Genre)dataSet.get(getAdapterPosition())).genreName);
                    break;
                case PLAYLIST:
                    receivingFrag=new FoldersSongsFragment();
                    args = new Bundle();
                    args.putParcelable("foldersongs", ((Playlist)dataSet.get(getAdapterPosition())));
                    args.putInt("isplaylist",0);
                    receivingFrag.setArguments(args);
                    ((MainActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.mainFragContainer, receivingFrag).commitNow();
                    doStuffOnTransaction(((Playlist)dataSet.get(getAdapterPosition())).name);
                    break;
                case FOLDER:
                    receivingFrag=new FoldersSongsFragment();
                    args = new Bundle();
                    args.putParcelable("foldersongs", ((Folder)dataSet.get(getAdapterPosition())));
                    args.putInt("isplaylist",-1);
                    receivingFrag.setArguments(args);
                    ((MainActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.mainFragContainer, receivingFrag).commitNow();
                    doStuffOnTransaction(((Folder)dataSet.get(getAdapterPosition())).folderName);
            }
        }
    }
    private void doStuffOnTransaction(String text){
        sheetSelectionText.setText(text);
        bottomsheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
}
