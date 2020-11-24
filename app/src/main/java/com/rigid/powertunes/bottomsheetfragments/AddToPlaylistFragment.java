package com.rigid.powertunes.bottomsheetfragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rigid.powertunes.dialogs.CreatePlaylistDialog;
import com.rigid.powertunes.misc.GlobalSelectionTracker;
import com.rigid.powertunes.R;
import com.rigid.powertunes.bottomsheetfragments.bottomsheetadapters.PlaylistDisplaySheetAdapter;
import com.rigid.powertunes.songmodels.Playlist;
import com.rigid.powertunes.songmodels.Song;
import com.rigid.powertunes.viewmodels.PlaylistDataViewModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AddToPlaylistFragment extends Fragment {
    private final static String TAG=AddToPlaylistFragment.class.getSimpleName();

    private RecyclerView rv;
    private PlaylistDisplaySheetAdapter mAdapter;
    private TextView noPlaylistText;
    private ImageView addNewPlaylist;
    private PlaylistDataViewModel playlistDataViewModel;
    private ArrayList<Song> folderSongs;


    public AddToPlaylistFragment() {
        super();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(folderSongs!=null)
            folderSongs.clear();
        if(getArguments()!=null) {
            folderSongs = (ArrayList<Song>) getArguments().getSerializable("songs");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        initViewModels();
        View v = inflater.inflate(R.layout.add_new_playlist_sheet,container,false);
        noPlaylistText=v.findViewById(R.id.addNewPlaylistText);
        addNewPlaylist=v.findViewById(R.id.addNewPlaylistImage);
        addNewPlaylist.setOnClickListener(clickListener());
        initRecyclerViewObjects(v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    private void initViewModels() {
        //data viewmodel
        playlistDataViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())
                .create(PlaylistDataViewModel.class);
        playlistDataViewModel.getAllPlaylists().observe(this,DataObserver());

    }
    private void initRecyclerViewObjects(View v) {
        rv = v.findViewById(R.id.addPlaylistList);
        mAdapter=new PlaylistDisplaySheetAdapter(getActivity());
        mAdapter.swapFolderSongs(folderSongs);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(),3,RecyclerView.HORIZONTAL,false);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(mAdapter);

    }

    /**
     * CURSOR LIVEDATA OBSERVER
     */
    private Observer<List<Playlist>> DataObserver(){
        return playlists -> {
            if(playlists.size()!=0){
                noPlaylistText.setVisibility(View.GONE);
                mAdapter.swapPlaylist(playlists);
            }else {
                noPlaylistText.setVisibility(View.VISIBLE);
            }
        };
    }

    //add new playlist
    private View.OnClickListener clickListener(){
        return v -> {
            final ArrayList<String> selectedSongs = new ArrayList<>();
            if (GlobalSelectionTracker.getMySelection().hasSelection()) {
                if (folderSongs != null && folderSongs.size() > 0) {
                    for(Song song : folderSongs) {
                        selectedSongs.add(song.data);
                    }
                    if (folderSongs.size() > 1) {
                        CreatePlaylistDialog.create(selectedSongs).show(getChildFragmentManager(), "CREATE NEW PLAYLIST");
                    } else {
                        CreatePlaylistDialog.create(selectedSongs.get(0)).show(getChildFragmentManager(), "CREATE NEW PLAYLIST");
                    }
            } else {
                    for (Uri uri : GlobalSelectionTracker.getMySelection().getSelection()) {
                        selectedSongs.add(uri.getPath());
                        Log.d(TAG, uri.getPath());
                    }
                    if (GlobalSelectionTracker.getMySelection().getSelection().size() > 1) {
                        CreatePlaylistDialog.create(selectedSongs).show(getChildFragmentManager(), "CREATE NEW PLAYLIST");
                    } else {
                        CreatePlaylistDialog.create(selectedSongs.get(0)).show(getChildFragmentManager(), "CREATE NEW PLAYLIST");
                    }
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
