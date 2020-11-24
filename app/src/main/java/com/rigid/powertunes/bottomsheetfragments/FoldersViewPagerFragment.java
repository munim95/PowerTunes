package com.rigid.powertunes.bottomsheetfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.rigid.powertunes.bottomsheetbehaviours.MainBottomSheet;
import com.rigid.powertunes.R;
import com.rigid.powertunes.main.fragments.AllSongsFragment;
import com.rigid.powertunes.main.fragments.LastAddedFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class FoldersViewPagerFragment extends Fragment {
    public static FoldersViewPagerFragment newInstance(int pos) {
        Bundle args = new Bundle();
        args.putInt("pagerposition",pos);
        FoldersViewPagerFragment fragment = new FoldersViewPagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private int position =-1;
    private TextView txt1,txt2,txt3,txt4;
    private final AllSongsFragment allSongsFragment = new AllSongsFragment();
    private final LastAddedFragment lastAddedFragment =new LastAddedFragment();
    private final AlbumsFragment albumsFragment =new AlbumsFragment();
    private final ArtistsFragment artistsFragment =new ArtistsFragment();
    private final GenresFragment genresFragment =new GenresFragment();
    private final FilesFragment filesFragment =new FilesFragment();
    private final PlaylistFragment playlistFragment = new PlaylistFragment();
    private final AudioSheetFragment audioSheetFragment = new AudioSheetFragment();
    private TextView titleText;
    private BottomSheetBehavior bottomsheetBehaviour;

    private String[] makeFolderItems(){
        return new String[]
                {
                        "All Songs",//1
                        "Last Added",//2
                        "Artists",//3
                        "Albums",//4
                        "Genres", //1
                        "Folders",//2
                        "Playlists",//3
                        "Equalizer"//4
                };
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position=getArguments().getInt("pagerposition");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.folders_viewpager_frag,container,false);
        txt1=v.findViewById(R.id.foldersTxt1);
        txt2=v.findViewById(R.id.foldersTxt2);
        txt3=v.findViewById(R.id.foldersTxt3);
        txt4=v.findViewById(R.id.foldersTxt4);
        v.findViewById(R.id.foldersTxt1Holder).setOnClickListener(clickListener());
        v.findViewById(R.id.foldersTxt2Holder).setOnClickListener(clickListener());
        v.findViewById(R.id.foldersTxt3Holder).setOnClickListener(clickListener());
        v.findViewById(R.id.foldersTxt4Holder).setOnClickListener(clickListener());

        RelativeLayout layout = getActivity().findViewById(R.id.mainBottomsheetLayout);
        bottomsheetBehaviour = MainBottomSheet.from(layout);
        titleText=getActivity().findViewById(R.id.foldersMenuTxt);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(position == 0){
            txt1.setText(makeFolderItems()[0]);
            ((ImageView)view.findViewById(R.id.foldersTxt1Image)).setImageDrawable(getResources().getDrawable(R.drawable.ic_music_note_black_24dp,null));
            txt2.setText(makeFolderItems()[1]);
            ((ImageView)view.findViewById(R.id.foldersTxt2Image)).setImageDrawable(getResources().getDrawable(R.drawable.ic_add_24dp,null));
            txt3.setText(makeFolderItems()[2]);
            ((ImageView)view.findViewById(R.id.foldersTxt3Image)).setImageDrawable(getResources().getDrawable(R.drawable.ic_mic_24dp,null));
            txt4.setText(makeFolderItems()[3]);
            ((ImageView)view.findViewById(R.id.foldersTxt4Image)).setImageDrawable(getResources().getDrawable(R.drawable.ic_album_black_24dp,null));
        }else if(position==1){
            txt1.setText(makeFolderItems()[4]);
            ((ImageView)view.findViewById(R.id.foldersTxt1Image)).setImageDrawable(getResources().getDrawable(R.drawable.ic_library_music_24dp,null));
            txt2.setText(makeFolderItems()[5]);
            ((ImageView)view.findViewById(R.id.foldersTxt2Image)).setImageDrawable(getResources().getDrawable(R.drawable.ic_folder_24dp,null));
            txt3.setText(makeFolderItems()[6]);
            ((ImageView)view.findViewById(R.id.foldersTxt3Image)).setImageDrawable(getResources().getDrawable(R.drawable.ic_list_24dp,null));
            txt4.setText(makeFolderItems()[7]);
            ((ImageView)view.findViewById(R.id.foldersTxt4Image)).setImageDrawable(getResources().getDrawable(R.drawable.ic_equalizer_24dp,null));
        }
    }

    private View.OnClickListener clickListener(){
        return v -> {
            final FragmentTransaction transaction= getActivity().getSupportFragmentManager().beginTransaction();
            switch(v.getId()){
                case R.id.foldersTxt1Holder:
                    if(txt1.getText().equals(makeFolderItems()[0])){
                        //all songs
                        transaction
                                .replace(R.id.mainFragContainer,allSongsFragment,allSongsFragment.getClass().getSimpleName());
                        doStuffOnTransaction("All Songs",true);
                    }else{
                        //genres
                        transaction
                                .replace(R.id.bottomsheet_frag_container,genresFragment,genresFragment.getClass().getSimpleName());
                        doStuffOnTransaction("Genres",false);
                    }
                    break;
                case R.id.foldersTxt2Holder:
                    if(txt2.getText().equals(makeFolderItems()[1])){
                        //last added
                        transaction
                                .replace(R.id.mainFragContainer, lastAddedFragment,lastAddedFragment.getClass().getSimpleName());
                        doStuffOnTransaction("Last Added",true);
                    }else{
                        //folders
                        transaction
                                .replace(R.id.bottomsheet_frag_container,filesFragment,filesFragment.getClass().getSimpleName());
                        doStuffOnTransaction("Folders",false);
                    }
                    break;
                case R.id.foldersTxt3Holder:
                    if(txt3.getText().equals(makeFolderItems()[2])){
                        //artists
                        transaction
                                .replace(R.id.bottomsheet_frag_container,artistsFragment,artistsFragment.getClass().getSimpleName());
                        doStuffOnTransaction("Artists",false);

                    }else{
                        //playlists
                        transaction
                                .replace(R.id.bottomsheet_frag_container, playlistFragment);
                        doStuffOnTransaction("Playlists",false);
                    }
                    break;
                case R.id.foldersTxt4Holder:
                    if(txt4.getText().equals(makeFolderItems()[3])){
                        //albums
                        transaction
                                .replace(R.id.bottomsheet_frag_container,albumsFragment,albumsFragment.getClass().getSimpleName());
                        doStuffOnTransaction("Albums",false);
                    }else{
                        //equalizer
                        transaction
                                .replace(R.id.bottomsheet_frag_container,audioSheetFragment);
                        doStuffOnTransaction("Equalizer",false);
                    }
                    break;
            }
            transaction.commit();
        };
    }
    private void doStuffOnTransaction(String text,boolean main){
        if(main) {
            bottomsheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }else {
            titleText.setText(text);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
