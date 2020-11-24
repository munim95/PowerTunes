package com.rigid.powertunes.bottomsheetfragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rigid.powertunes.R;
import com.rigid.powertunes.bottomsheetfragments.bottomsheetadapters.PlaylistAdapter;
import com.rigid.powertunes.songmodels.Playlist;
import com.rigid.powertunes.viewmodels.PlaylistDataViewModel;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.Pivot;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.transition.Transition;
import androidx.transition.TransitionInflater;

public class PlaylistFragment extends Fragment {
    private final static String TAG=PlaylistFragment.class.getSimpleName();

    private DiscreteScrollView rv;
    private PlaylistAdapter mAdapter;

    private PlaylistDataViewModel playlistDataViewModel;


    public PlaylistFragment() {
        super();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(getContext());
        Transition transition1 = inflater.inflateTransition(R.transition.folder_items_transition);
        transition1.setDuration(200);
        setEnterTransition(transition1);
        setExitTransition(transition1);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottomsheet_folders_layout,container,false);
        initViewModels();
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
        playlistDataViewModel = ViewModelProviders.of(this).get(PlaylistDataViewModel.class);
        playlistDataViewModel.getAllPlaylists().observe(this,DataObserver());

    }
    private void initRecyclerViewObjects(View v) {
        rv = v.findViewById(R.id.bottomsheetFoldersRv);
        mAdapter= new PlaylistAdapter(getActivity(), v);
        rv.setAdapter(mAdapter);
        rv.setSlideOnFling(true);
        rv.setItemTransformer(new ScaleTransformer.Builder()
                .setMaxScale(1.05f)
                .setMinScale(0.8f)
                .setPivotX(Pivot.X.CENTER) // CENTER is a default one
                .setPivotY(Pivot.Y.BOTTOM) // CENTER is a default one
                .build());
    }

    /**
     * CURSOR LIVEDATA OBSERVER
     */
    private Observer<List<Playlist>> DataObserver(){
        return new Observer<List<Playlist>>() {
            @Override
            public void onChanged(List<Playlist> playlists) {
                Log.d(TAG,"size"+playlists.size());
                mAdapter.swapPlaylist(playlists);
                if(playlists.size()==0){
//                    noPlaylistText.setVisibility(View.VISIBLE);
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
