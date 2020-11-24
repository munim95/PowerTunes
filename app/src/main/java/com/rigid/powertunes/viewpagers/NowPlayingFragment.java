package com.rigid.powertunes.viewpagers;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.rigid.powertunes.R;
import com.rigid.powertunes.misc.ResultReceiverCustom;
import com.rigid.powertunes.util.RenderScriptBlurBuilder;
import com.rigid.powertunes.songmodels.Song;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class NowPlayingFragment extends Fragment {
    /**
     * Contains Title and blurred art
     * */

    private TextView nowPlayingTitle, nowPlayingAlbum, nowPlayingArtist;
    private ImageView blurredArt;
    private Song currentSong=null;
    private AsyncTasker asyncTasker;


    public static NowPlayingFragment newInstance(Song song){
        Bundle bundle = new Bundle();
        bundle.putParcelable("currentsong",song);
        final NowPlayingFragment nowPlayingFragment = new NowPlayingFragment();
        nowPlayingFragment.setArguments(bundle);
        return nowPlayingFragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentSong=getArguments().getParcelable("currentsong");
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v =inflater.inflate(R.layout.now_playing_layout,container,false);
        v.setOnClickListener(v1 -> {
            if(isVisible()) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        blurredArt=v.findViewById(R.id.blurredAlbumArt);

        nowPlayingTitle=v.findViewById(R.id.nowPlayingTitle);
        nowPlayingArtist=v.findViewById(R.id.nowPlayingArtist);
        nowPlayingAlbum=v.findViewById(R.id.nowPlayingAlbum);

        nowPlayingTitle.setText(currentSong.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        String artistName = currentSong.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        String albumName = currentSong.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
        nowPlayingArtist.setText(artistName.equalsIgnoreCase("<unknown>")?
                getActivity().getString(R.string.unknown_artist):artistName);
        nowPlayingAlbum.setText(albumName.equalsIgnoreCase("<unknown>")?
                getActivity().getString(R.string.unknown_album):albumName);

        asyncTasker=new AsyncTasker(v);
        asyncTasker.execute(currentSong.imageBytes);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    private static class AsyncTasker extends AsyncTask<byte[], Void, Bitmap> {
        private WeakReference<View> view;
        private Exception e;

        private AsyncTasker(View view) {
            super();
            this.view=new WeakReference<>(view);
        }

        @SuppressLint("WrongThread")
        @Override
        protected Bitmap doInBackground(byte[]... bytes) {
            Bitmap blurryBitmap=null;
            if(bytes[0]!=null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize=4;
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes[0],
                        0,bytes[0].length,options);
                if(!bitmap.isRecycled())
                try {
                   blurryBitmap = RenderScriptBlurBuilder.blur(view.get().getContext(), bitmap);
               }catch (Exception e){
                   this.e=e;
               }
            }
            return blurryBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(e!=null) {
                e.printStackTrace();
            }else{
                Glide.with(view.get().findViewById(R.id.blurredAlbumArt))
                        .load(bitmap)
                        .transition(new DrawableTransitionOptions().crossFade())
                        .apply(new RequestOptions().centerCrop().skipMemoryCache(true))
                        .into((ImageView) view.get().findViewById(R.id.blurredAlbumArt));
            }
        }
    }


    @Override
    public void onStart() { super.onStart(); }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() { super.onStop(); }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(asyncTasker!=null)
            asyncTasker.cancel(true);
        Glide.with(blurredArt).clear(blurredArt);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}

