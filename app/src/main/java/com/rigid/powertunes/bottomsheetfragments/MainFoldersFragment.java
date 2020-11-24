package com.rigid.powertunes.bottomsheetfragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rigid.powertunes.R;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.transition.Transition;
import androidx.transition.TransitionInflater;
import androidx.viewpager.widget.ViewPager;
/**
 * Initializes the viewpager menu
 * */
public class MainFoldersFragment extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(getContext());
        Transition transition1 = inflater.inflateTransition(R.transition.grid_exit_transition);
        transition1.setDuration(150);
        setEnterTransition(transition1);
        setExitTransition(transition1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.main_folders_viewpager,container,false);
        ViewPager viewPager = v.findViewById(R.id.mainFoldersViewPager);
        WormDotsIndicator dotsIndicator = v.findViewById(R.id.tabDots);
        viewPager.setAdapter(new FoldersViewPagerAdapter(getChildFragmentManager()));
        dotsIndicator.setViewPager(viewPager);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public void onResume() {
        super.onResume();
    }


    class FoldersViewPagerAdapter extends FragmentStatePagerAdapter{
        FoldersViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return FoldersViewPagerFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 2;
        }
        @Override
        public Parcelable saveState() {
            return null;
        }
    }
}
