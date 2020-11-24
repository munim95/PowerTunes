package com.rigid.powertunes.main.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Transition;
import androidx.transition.TransitionInflater;

import com.rigid.powertunes.ItemTouchHelperCallback;
import com.rigid.powertunes.OnStartDragListener;
import com.rigid.powertunes.R;
import com.rigid.powertunes.main.fragments.fragmentadapters.SongsAdapter;
import com.rigid.powertunes.recyclerviewhelpers.CustomLinearLayoutManager;
import com.rigid.powertunes.songmodels.Song;
import com.rigid.powertunes.viewmodels.SharedFragmentViewModel;

import java.util.ArrayList;

public class QueueFragment extends Fragment implements OnStartDragListener {

    //todo save queue in disk and load again on create
    private SongsAdapter mAdapter;
    private RecyclerView recyclerView;
    private SharedFragmentViewModel sharedFragmentViewModel;
    private ItemTouchHelper touchHelper;
    private TextView emptyQueueText;

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
        View v= inflater.inflate(R.layout.queue_fragment,container,false);
        recyclerView=v.findViewById(R.id.queueRv);
        emptyQueueText=v.findViewById(R.id.emptyQueueText);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new SongsAdapter(getActivity(),this,-1,sharedFragmentViewModel);

        recyclerView.setLayoutManager(new CustomLinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemAnimator(null);

        touchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(mAdapter));
        touchHelper.attachToRecyclerView(recyclerView);

        sharedFragmentViewModel= ViewModelProviders.of(getActivity()).get(SharedFragmentViewModel.class);
        sharedFragmentViewModel.getCurrentSongsOrder().observe(this, songs -> {
            //get queue songs
            if(!songs.isEmpty()) {
                emptyQueueText.setVisibility(View.GONE);
                mAdapter.swapSongList((ArrayList<Song>) songs);
                Log.d("QUEUE","size "+songs.size());
            }
            else
                emptyQueueText.setVisibility(View.VISIBLE);
            });
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder holder) {
        touchHelper.startDrag(holder);
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
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
