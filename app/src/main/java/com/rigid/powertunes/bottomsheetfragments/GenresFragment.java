package com.rigid.powertunes.bottomsheetfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rigid.powertunes.R;
import com.rigid.powertunes.bottomsheetfragments.bottomsheetadapters.GenresAdapter;
import com.rigid.powertunes.viewmodels.SongDataViewModel;
import com.rigid.powertunes.songmodels.Genre;
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

public class GenresFragment extends Fragment {
    private final String TAG=GenresFragment.class.getSimpleName();

    private DiscreteScrollView rv;
    private GenresAdapter mAdapter;

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
        mAdapter = new GenresAdapter(getActivity(),v);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private void initViewModels(){
        final SongDataViewModel songDataViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())
                .create(SongDataViewModel.class);
        songDataViewModel.getGenres().observe(this, GenresDataObserver());
    }

    /**LIVEDATA OBSERVERS */
    //song data observer
    private Observer<List<Genre>> GenresDataObserver(){
        return new Observer<List<Genre>>() {
            @Override
            public void onChanged(List<Genre> genres) {
                mAdapter.swapGenres(genres);
            }
        };
    }



}
