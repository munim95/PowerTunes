package com.rigid.powertunes.main.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rigid.powertunes.misc.GlobalSelectionTracker;
import com.rigid.powertunes.misc.ResultReceiverCustom;
import com.rigid.powertunes.recyclerviewhelpers.CustomLinearLayoutManager;
import com.rigid.powertunes.GlobalVariables;
import com.rigid.powertunes.recyclerviewhelpers.CustomRecyclerView;
import com.rigid.powertunes.recyclerviewhelpers.ListClickListener;
import com.rigid.powertunes.main.activities.MainActivity;
import com.rigid.powertunes.viewmodels.SharedFragmentViewModel;
import com.rigid.powertunes.viewpagers.NowPlayingHostFragment;
import com.rigid.powertunes.R;
import com.rigid.powertunes.recyclerviewhelpers.RecyclerTouchListener;
import com.rigid.powertunes.viewmodels.SongDataViewModel;
import com.rigid.powertunes.main.fragments.fragmentadapters.SongsAdapter;
import com.rigid.powertunes.songmodels.Song;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class LastAddedFragment extends Fragment {
    private static final String TAG =LastAddedFragment.class.getSimpleName();

    private SongsAdapter mAdapter;
    private CustomRecyclerView rv;
    private List<Song> songs;
    public SongDataViewModel songDataViewModel;
    private SharedFragmentViewModel sharedFragmentViewModel;
    private ResultReceiverCustom resultReceiverCustom = new ResultReceiverCustom(new Handler());
    private Bundle bundle = new Bundle();
    private boolean songsAreSet=false;
    private int currPos;
    private long currentId=-1;
    private boolean launchNowPlaying = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            currPos=getArguments().getInt("currentposition");
            if(getArguments().getInt("launchnowplaying")==1){
                launchNowPlaying=true;
                GlobalVariables.firstTime=false;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        resultReceiverCustom.setReceiver((MainActivity)getActivity());
        return inflater.inflate(R.layout.last_added_layout, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        songsAreSet=false;
        initViewModels();
        initRecyclerViewObjects(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    private void initViewModels() {
        //data viewmodel
        songDataViewModel = ViewModelProviders.of(this).get(SongDataViewModel.class);
        songDataViewModel.getRecentlyAdded().observe(this, DataObserver());

        sharedFragmentViewModel=ViewModelProviders.of(getActivity()).get(SharedFragmentViewModel.class);
    }
    /**LIVEDATA OBSERVERS */
    //song data observer
    private Observer<List<Song>> DataObserver(){
        return new Observer<List<Song>>() {
            @Override
            public void onChanged(List<Song> songs) {
                LastAddedFragment.this.songs=songs;
                mAdapter.swapSongList((ArrayList<Song>) songs);

               launchNowPlayingOnNotificationClick(launchNowPlaying);
//               initIfFirstTime();
            }
        };
    }
    private void launchNowPlayingOnNotificationClick(boolean launchNowPlaying){
        if(launchNowPlaying){
            sharedFragmentViewModel.setCurrentSongsOrder(songs);
            songsAreSet=true;
            setNowPlayingTransaction(currPos);
            this.launchNowPlaying=false;
        }
    }

    private void initRecyclerViewObjects(View v) {
        rv = v.findViewById(R.id.recentlyAddedRv);
        rv.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), rv, listClickListener()));

        CustomLinearLayoutManager layoutManager = new CustomLinearLayoutManager(getActivity());
        rv.setLayoutManager(layoutManager);
        mAdapter = new SongsAdapter(getContext(), this,-1,sharedFragmentViewModel);
        rv.setAdapter(mAdapter);
//        rv.setItemAnimator(null);
    }

    private ListClickListener listClickListener(){
        return new ListClickListener() {
            @Override
            public void click(View view, int position) {
                if(isVisible()) {
                    //handle clicks
                    if (!GlobalSelectionTracker.getMySelection().hasSelection()) {
                        bundle.putInt("currentposition", position);
//                        bundle.putParcelable("songmetadata", songs.get(position).metadataCompat);
                        resultReceiverCustom.send(GlobalVariables.PLAY_UPDATE_CODE, bundle);
//                        setNowPlayingTransaction(position);
//                        Log.d(TAG, "boolean " + songsAreSet);
//                        if (!songsAreSet) {
//                            sharedFragmentViewModel.setCurrentSongsOrder(songs);
//                            songsAreSet = true;
//                        }
//                        sharedFragmentViewModel.setCurrentPosition(position);
                    }
                }
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        };
    }
    private void setNowPlayingTransaction(int position){
        rv.shouldDisableTouch(true);
        //let viewpager know of the current fragment
        final NowPlayingHostFragment nowPlayingHostFragment = new NowPlayingHostFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("currentposition",position);
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
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
