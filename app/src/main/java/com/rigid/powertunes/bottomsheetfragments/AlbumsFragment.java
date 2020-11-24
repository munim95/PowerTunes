package com.rigid.powertunes.bottomsheetfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rigid.powertunes.bottomsheetfragments.bottomsheetadapters.AlbumsAdapter;
import com.rigid.powertunes.R;
import com.rigid.powertunes.viewmodels.SongDataViewModel;
import com.rigid.powertunes.songmodels.Album;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.Pivot;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.Transition;
import androidx.transition.TransitionInflater;

public class AlbumsFragment extends Fragment {
    private final String TAG =AlbumsFragment.class.getSimpleName();

    private AlbumsAdapter mAdapter;
    private DiscreteScrollView rv;
    private List<Album> albumList;

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
        mAdapter = new AlbumsAdapter(getActivity(), v);
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
        final SongDataViewModel songDataViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())
                .create(SongDataViewModel.class);
        songDataViewModel.getAlbums().observe(this, AlbumDataObserver());

    }

    /**LIVEDATA OBSERVERS */
    //song data observer
    private Observer<List<Album>> AlbumDataObserver(){
        return albums ->{
            mAdapter.swapAlbums(albums);
            albumList = albums;
        };
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
