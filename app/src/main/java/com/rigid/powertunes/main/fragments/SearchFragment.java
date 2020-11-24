package com.rigid.powertunes.main.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.rigid.powertunes.R;
import com.rigid.powertunes.main.fragments.fragmentadapters.SearchAdapter;
import com.rigid.powertunes.viewmodels.SearchDataViewModel;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Transition;
import androidx.transition.TransitionInflater;

public class SearchFragment extends Fragment {

    private EditText searchBar;
    private RecyclerView rv;
    private SearchAdapter mAdapter;
    private SearchDataViewModel searchDataViewModel;

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
        View v =inflater.inflate(R.layout.search_layout,container,false);
        searchBar=v.findViewById(R.id.editTextSearch);
        rv=v.findViewById(R.id.searchRv);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        searchBar.addTextChangedListener(textWatcher());
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new SearchAdapter(getActivity(),this, Collections.emptyList());
        rv.setAdapter(mAdapter);

        searchDataViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())
                .create(SearchDataViewModel.class);
    }
    //retrieve search objects
    private Observer<List<Object>> observer(){
        return objects -> mAdapter.swapData(objects);
    }

    private TextWatcher textWatcher(){
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(String.valueOf(s));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }
    private void performSearch(String s){
        if(!TextUtils.isEmpty(s)) {
            searchDataViewModel.mediatorLiveData(s).observe(SearchFragment.this,observer());
        }else{
            mAdapter.swapData(Collections.emptyList());
        }
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
