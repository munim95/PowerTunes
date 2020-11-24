package com.rigid.powertunes.viewpagers;

import android.os.Parcelable;

import com.rigid.powertunes.songmodels.Song;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class NowPlayingPagerAdapter extends FragmentStatePagerAdapter {
    private ArrayList<Song> songs;

    public NowPlayingPagerAdapter(@NonNull FragmentManager fm) {
        super(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return NowPlayingFragment.newInstance(songs.get(position));
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    public void swapData(ArrayList<Song> songs){
        this.songs=songs;
        notifyDataSetChanged();
    }
    @Override
    public Parcelable saveState() {
        return null;
    }
}