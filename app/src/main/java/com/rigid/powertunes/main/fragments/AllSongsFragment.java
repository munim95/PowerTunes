package com.rigid.powertunes.main.fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

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
import com.rigid.powertunes.recyclerviewhelpers.ScrollerLinearLayout;
import com.rigid.powertunes.viewmodels.SongDataViewModel;
import com.rigid.powertunes.main.fragments.fragmentadapters.SongsAdapter;
import com.rigid.powertunes.songmodels.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MunimsMac on 04/01/2018.
 */

public class AllSongsFragment extends Fragment  {
    // TODO: 25/01/2019 SAVED INSTANCE STATE WITH VIEWMODELS AND HANDLE BACK BUTTON
    private static final String TAG = AllSongsFragment.class.getSimpleName();

    private Context ctx;
    private SongsAdapter mAdapter;

    private CustomRecyclerView rv;

    private LinearLayout scrollerLayout;
    public SongDataViewModel songDataViewModel;
    private SharedFragmentViewModel sharedFragmentViewModel;
    private ResultReceiverCustom resultReceiverCustom = new ResultReceiverCustom(new Handler());
    private Bundle bundle = new Bundle();
    private List<Song> songs;
    private int currPos=0;
//    private boolean songsAreSetSharedVM =false;
    private boolean launchNowPlaying = false;

    public AllSongsFragment() {
        super();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ctx = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            currPos=getArguments().getInt("currentposition");
            if(getArguments().getInt("launchnowplaying")==1){
                launchNowPlaying=true;
                MainActivity.FIRST_LOAD =false;
            }else{
                launchNowPlaying=false;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        resultReceiverCustom.setReceiver((MainActivity)getActivity());
        return inflater.inflate(R.layout.all_songs_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        songsAreSetSharedVM=false;
        view.requestFocusFromTouch();
        initViewModels();
        initRecyclerViewObjects(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void initViewModels() {
        //viewmodel from main activity
        sharedFragmentViewModel= new ViewModelProvider(this)
                .get(SharedFragmentViewModel.class);
        //data viewmodel
        songDataViewModel = new ViewModelProvider(this)
                .get(SongDataViewModel.class);
        songDataViewModel.getSongs().removeObservers(this); //to prevent observer being called twice
        songDataViewModel.getSongs().observe(getViewLifecycleOwner(), DataObserver());
    }

    private void initRecyclerViewObjects(View v) {
        mAdapter = new SongsAdapter(getContext(), this,-1,sharedFragmentViewModel);
        rv = v.findViewById(R.id.fragmentList);
        rv.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), rv, listClickListener()));
//        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setLayoutManager(new CustomLinearLayoutManager(getActivity()));
        rv.setAdapter(mAdapter);
        scrollerLayout=v.findViewById(R.id.scrollerLayout);
        ((ScrollerLinearLayout)scrollerLayout).setRecyclerView(rv);
        ((ScrollerLinearLayout)scrollerLayout).setLetterBox(v);
    }

    /**
     * LIVEDATA OBSERVER
     */
    private Observer<List<Song>> DataObserver() {
        return songs -> {
            AllSongsFragment.this.songs=songs;
            ((ScrollerLinearLayout) scrollerLayout).swapData((ArrayList<Song>) songs);
            mAdapter.swapSongList((ArrayList<Song>) songs);

            if(launchNowPlaying) {
                sharedFragmentViewModel.setCurrentSongsOrder(songs);
                setNowPlayingTransaction(currPos);
//                songsAreSetSharedVM =true;
                launchNowPlaying=false;
            }

//            if(MainActivity.FIRST_LOAD){
//                resultReceiverCustom.send(GlobalVariables.PLAY_UPDATE_CODE,
//                        makeBundle(bundle,currPos,mAdapter.getItemId(currPos),true));
                sharedFragmentViewModel.setCurrentSongsOrder(songs);
//                sharedFragmentViewModel.setCurrentPosition(currPos);
//                songsAreSetSharedVM =true;
//                MainActivity.FIRST_LOAD=false;
//            }
        };
    }
    private Bundle makeBundle(Bundle bundle, int position){
        bundle.putInt("currentposition", position);//if last position exists then load that instead
//        bundle.putLong("songid", id);
//        bundle.putParcelable("songmetadata", songs.get(position).metadataCompat);
//        bundle.putBoolean("firsttime",firstTime);
        return bundle;
    }

    private ListClickListener listClickListener() {
        return new ListClickListener() {
            @Override
            public void click(View view, int position) {
                if(isVisible()) {
                    if (!GlobalSelectionTracker.getMySelection().hasSelection()) {
                        currPos=position;
                        //now handled in main activity to make a singleton source and in an effort to reduce static usage
                        resultReceiverCustom.send(GlobalVariables.PLAY_UPDATE_CODE,
                                makeBundle(bundle,position)); //send to main activity
//                        setNowPlayingTransaction(position);
//                        if(!songsAreSetSharedVM) {
//                            sharedFragmentViewModel.setCurrentSongsOrder(songs);
//                            songsAreSetSharedVM =true;
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
//        rv.shouldDisableTouch(true);
        final NowPlayingHostFragment nowPlayingHostFragment = new NowPlayingHostFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("currentposition",position);
        nowPlayingHostFragment.setArguments(bundle);

        getFragmentManager()
                .beginTransaction()
                .add(R.id.mainFragContainer, nowPlayingHostFragment,
                        NowPlayingHostFragment.class.getSimpleName())
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
        Window window = getActivity().getWindow();
        Point size = new Point();
        // Store dimensions of the screen in `size`
        window.getWindowManager().getDefaultDisplay().getSize(size);
        ViewGroup.LayoutParams layoutParams = scrollerLayout.getLayoutParams();
        // Set the height proportional to 50% of the screen width
        if(window.getWindowManager().getDefaultDisplay().getRotation()==Surface.ROTATION_90||
                window.getWindowManager().getDefaultDisplay().getRotation()==Surface.ROTATION_270){
            layoutParams.height=(int)(size.y * 0.50);
        }
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
