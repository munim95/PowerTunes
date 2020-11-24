package com.rigid.powertunes.bottomsheetfragments.bottomsheetadapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.rigid.powertunes.misc.GlideViewTargetSub;
import com.rigid.powertunes.misc.GlobalSelectionTracker;
import com.rigid.powertunes.R;
import com.rigid.powertunes.songmodels.Playlist;
import com.rigid.powertunes.songmodels.Song;
import com.rigid.powertunes.util.PlaylistUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistDisplaySheetAdapter extends RecyclerView.Adapter<PlaylistDisplaySheetAdapter.ViewHolder> {
    //show number of songs in playlist
    private List<Playlist> playlistsList;
    private Context ctx;
    private ArrayList<Song> songs;

    public PlaylistDisplaySheetAdapter(Context context) {
        ctx=context;
    }


    public void swapPlaylist(List<Playlist> playlists) {
        playlistsList= playlists;
        notifyDataSetChanged();
    }
    public void swapFolderSongs(ArrayList<Song> songs){
        this.songs=songs;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bottomsheet_folder_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindTo(position);
    }

    @Override
    public int getItemCount() {
        return playlistsList!=null?playlistsList.size():0;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView playlistImg;
        private TextView playlistName;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            playlistImg=itemView.findViewById(R.id.bottomsheetItemImage);
            playlistName=itemView.findViewById(R.id.bottomsheetItemName);
        }
        private void bindTo(int position){
            Playlist playlist=playlistsList.get(position);
            playlistName.setText(playlist.name);

            Glide.with(itemView)
                    .load(playlist.getChildList().size()>0?playlist.getChildList().get(0).imageBytes:null)
                    .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(ctx.getResources().getDimensionPixelSize(R.dimen.round_edges_dimen)))
                            .placeholder(R.mipmap.ic_launcher).skipMemoryCache(true))
                    .into(new GlideViewTargetSub(playlistImg));
        }

        @Override
        public void onClick(View v) {
            //add to selected playlist functionality
            final ArrayList<String> selectedSongs = new ArrayList<>();
            if (GlobalSelectionTracker.getMySelection().hasSelection()) {
                    if(songs!=null && songs.size()>0){
                        for(Song song:songs)
                        selectedSongs.add(song.data);
                }else {
                        for (Uri uri : GlobalSelectionTracker.getMySelection().getSelection()) {
                        selectedSongs.add(uri.getPath());
                    }
                }
                if (PlaylistUtil.addSongsToPlaylist(ctx, playlistsList.get(getAdapterPosition()).id, selectedSongs)) {
                    Toast.makeText(ctx, String.format("Added %s songs to playlist %s", selectedSongs.size(), playlistsList.get(getAdapterPosition()).name), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ctx, "Unexpected Error: Could not add songs to playlist.", Toast.LENGTH_SHORT).show();
                }
                GlobalSelectionTracker.getMySelection().clearSelection();
            }
        }
    }
}
