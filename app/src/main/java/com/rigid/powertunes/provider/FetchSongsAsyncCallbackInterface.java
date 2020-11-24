package com.rigid.powertunes.provider;

import com.rigid.powertunes.songmodels.Song;

import java.util.ArrayList;

public interface FetchSongsAsyncCallbackInterface {
    void onSongsLoaded(ArrayList<Song> songs);
}
