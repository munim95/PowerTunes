package com.rigid.powertunes.main.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.rigid.powertunes.misc.ResultReceiverCustom;
import com.rigid.powertunes.recyclerviewhelpers.CustomLinearLayoutManager;
import com.rigid.powertunes.misc.GlobalSelectionTracker;
import com.rigid.powertunes.GlobalVariables;
import com.rigid.powertunes.recyclerviewhelpers.ListClickListener;
import com.rigid.powertunes.main.activities.MainActivity;
import com.rigid.powertunes.songmodels.Album;
import com.rigid.powertunes.songmodels.Artist;
import com.rigid.powertunes.songmodels.Folder;
import com.rigid.powertunes.songmodels.Genre;
import com.rigid.powertunes.songmodels.Playlist;
import com.rigid.powertunes.songmodels.PlaylistSong;
import com.rigid.powertunes.viewmodels.SharedFragmentViewModel;
import com.rigid.powertunes.viewpagers.NowPlayingHostFragment;
import com.rigid.powertunes.R;
import com.rigid.powertunes.recyclerviewhelpers.RecyclerTouchListener;
import com.rigid.powertunes.recyclerviewhelpers.ScrollerLinearLayout;
import com.rigid.powertunes.main.fragments.fragmentadapters.SongsAdapter;
import com.rigid.powertunes.songmodels.Song;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

public class FoldersSongsFragment extends Fragment {
    /**
     * ARTISTS, ALBUMS, FOLDERS, GENRES SONGS
     * */
    private RecyclerView rv;
    private SongsAdapter mAdapter;
    private Object folder;
    private List<Song> songList;
    private LinearLayout scrollerLayout;
    private SharedFragmentViewModel sharedFragmentViewModel;
    private ResultReceiverCustom resultReceiverCustom = new ResultReceiverCustom(new Handler());
    private boolean songsAreSet=false;
    private Bundle bundle = new Bundle();

    //todo DELETE songs from playlist

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        folder = getArguments().getParcelable("folder");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initSharedViewModel();
        //get the current playlist id passed in from the adapter
        View v = inflater.inflate(R.layout.folders_songs_fragment,container,false);
        rv = v.findViewById(R.id.playlistSongList);
        mAdapter = new SongsAdapter(getActivity(),this,folder instanceof Playlist?((Playlist)folder).id:-1,sharedFragmentViewModel);
        rv.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), rv, listClickListener()));

        rv.setLayoutManager(new CustomLinearLayoutManager(getActivity()));
        rv.setAdapter(mAdapter);
        ((SimpleItemAnimator) rv.getItemAnimator())
                .setSupportsChangeAnimations(false);
        scrollerLayout=v.findViewById(R.id.scrollerLayout);
        ((ScrollerLinearLayout)scrollerLayout).setRecyclerView(rv);
        ((ScrollerLinearLayout)scrollerLayout).setLetterBox(v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        songsAreSet=false;
    }

    private void initSharedViewModel(){
        sharedFragmentViewModel = ViewModelProviders.of(getActivity()).get(SharedFragmentViewModel.class);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        songList=new ArrayList<>();
        if(folder instanceof Artist){
            songList=((Artist)folder).getChildList();
        }else if(folder instanceof Album){
            songList=((Album)folder).getChildList();
        }else if(folder instanceof Genre){
            songList=((Genre)folder).getChildList();
        }else if(folder instanceof Folder){
            songList=((Folder)folder).getSongsInFolders();
        }else if(folder instanceof Playlist){
            for(PlaylistSong song:((Playlist)folder).getChildList()){
                songList.add(new Song(
                        song.data,
                        song.imageBytes, null, -1));
                }
        }
        if(songList !=null && songList.size()!=0){
            mAdapter.swapSongList((ArrayList<Song>) songList);
            ((ScrollerLinearLayout) scrollerLayout).swapData((ArrayList<Song>) songList);
            if(MainActivity.FIRST_LOAD) {
                //if last position exists then load that instead
                bundle.putInt("currentposition", 0);
                bundle.putLong("newsongid", mAdapter.getItemId(0));
                bundle.putBoolean("firsttime",true);
                resultReceiverCustom.send(GlobalVariables.PLAY_UPDATE_CODE, bundle);
                sharedFragmentViewModel.setCurrentSongsOrder(songList);
                MainActivity.FIRST_LOAD =false;
            }
        }
        resultReceiverCustom.setReceiver((MainActivity)getActivity());
    }

    private ListClickListener listClickListener() {
        return new ListClickListener() {
            @Override
            public void click(View view, int position) {
                if(!GlobalSelectionTracker.getMySelection().hasSelection()) {
                    bundle.putInt("currentposition",position);
                    bundle.putLong("newsongid",mAdapter.getItemId(position));
                    bundle.putBoolean("firsttime",false);
                    resultReceiverCustom.send(GlobalVariables.PLAY_UPDATE_CODE,bundle);
                    if(!songsAreSet) {
                        sharedFragmentViewModel.setCurrentSongsOrder(songList);
                        songsAreSet=false;
                    }
                    setNowPlayingTransaction(position);
                }
            }
            @Override
            public void onLongClick(View view, int position) {
            }
        };
    }
    private void setNowPlayingTransaction(int position){
        Bundle bundle = new Bundle();
        bundle.putInt("currentposition",position);
        //let viewpager know of the current fragment
        final NowPlayingHostFragment nowPlayingHostFragment = new NowPlayingHostFragment();
        nowPlayingHostFragment.setArguments(bundle);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFragContainer, nowPlayingHostFragment, NowPlayingHostFragment.class.getSimpleName())
                .addToBackStack(null)
                .commit();
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
