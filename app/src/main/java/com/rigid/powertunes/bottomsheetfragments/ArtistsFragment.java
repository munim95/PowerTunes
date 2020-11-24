package com.rigid.powertunes.bottomsheetfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rigid.powertunes.bottomsheetfragments.bottomsheetadapters.ArtistsAdapter;
import com.rigid.powertunes.R;
import com.rigid.powertunes.viewmodels.SongDataViewModel;
import com.rigid.powertunes.songmodels.Artist;
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

public class ArtistsFragment extends Fragment {
    private final String TAG =ArtistsFragment.class.getSimpleName();

    private ArtistsAdapter mAdapter;
    private DiscreteScrollView rv;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottomsheet_folders_layout,container,false);
        initViewModels();
        rv = v.findViewById(R.id.bottomsheetFoldersRv);
        mAdapter = new ArtistsAdapter(getActivity(),v);
        rv.setAdapter(mAdapter);
        rv.setSlideOnFling(true);
        rv.setItemTransformer(new ScaleTransformer.Builder()
                .setMaxScale(1.05f)
                .setMinScale(0.8f)
                .setPivotX(Pivot.X.CENTER) // CENTER is a default one
                .setPivotY(Pivot.Y.BOTTOM) // CENTER is a default one
                .build());
        return v;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void initViewModels(){
        //data viewmodel
        final SongDataViewModel songDataViewModel = ViewModelProviders.of(this)
                .get(SongDataViewModel.class);
        songDataViewModel.getArtists().observe(this, ArtistsObserver());

    }

    /**LIVEDATA OBSERVERS */
    //song data observer
    private Observer<List<Artist>> ArtistsObserver(){
        return artists -> mAdapter.swapArtists(artists);
    }

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
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
