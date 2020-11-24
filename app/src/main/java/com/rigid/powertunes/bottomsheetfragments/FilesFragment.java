package com.rigid.powertunes.bottomsheetfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rigid.powertunes.R;
import com.rigid.powertunes.bottomsheetfragments.bottomsheetadapters.FoldersAdapter;
import com.rigid.powertunes.songmodels.Folder;
import com.rigid.powertunes.viewmodels.SongDataViewModel;
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

/**
 * NOTHING IS POSSIBLE WITHOUT HIS HELP. SUBHAN'ALLAH!
 * */

public class FilesFragment extends Fragment {

    private DiscreteScrollView rv;
    private FoldersAdapter foldersAdapter;
//    private Button testButton;

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
        rv = v.findViewById(R.id.bottomsheetFoldersRv);
        foldersAdapter=new FoldersAdapter(getActivity(),v);
        rv.setAdapter(foldersAdapter);
        rv.setSlideOnFling(true);
        rv.setItemTransformer(new ScaleTransformer.Builder()
                .setMaxScale(1.05f)
                .setMinScale(0.8f)
                .setPivotX(Pivot.X.CENTER) // CENTER is a default one
                .setPivotY(Pivot.Y.BOTTOM) // CENTER is a default one
                .build());
//        testButton=v.findViewById(R.id.testButton);
        SongDataViewModel songDataViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())
                .create(SongDataViewModel.class);
        songDataViewModel.getFolders().observe(this,DataObserver());
        return v;
    }

    private Observer<List<Folder>> DataObserver() {
        return folders -> {
            foldersAdapter.swapFolders(folders);
        };
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
