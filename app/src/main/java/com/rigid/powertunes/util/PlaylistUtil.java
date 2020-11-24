package com.rigid.powertunes.util;

import android.content.Context;
import android.util.Log;

import com.rigid.powertunes.provider.FetchSongFilesAsync;
import com.rigid.powertunes.mediaservice.MediaLibrary;
import com.rigid.powertunes.songmodels.Playlist;
import com.rigid.powertunes.songmodels.PlaylistSong;
import com.rigid.powertunes.songmodels.Song;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

public class PlaylistUtil {
    /**
     * PLAYLIST NAME IN FILE FORMAT-> 'MyPlaylist.plid0'
     * Where '.plid0' represents playlist id in file
     * The last digit is incremented on new playlist addition and is the true playlist id
     * */
    private final static String TAG = PlaylistUtil.class.getSimpleName();
    private final static String PLAYLISTFILEFORMAT = ".plid";
    private final static String MAINPLAYLISTFILE = "com.rigid.powertunes.playlists"; //CREATED ON PLAYLIST STARTUP. DON'T DELETE ONCE CREATED.


    public static boolean renamePlaylist(Context context, int playlistId, String newName){
        try {
            Log.d(TAG,"start "+ playlistId);
            FileInputStream streamIn = context.openFileInput(MAINPLAYLISTFILE);
            ObjectInputStream ois = new ObjectInputStream(streamIn);
            HashMap<String,ArrayList<String>> files = (HashMap<String, ArrayList<String>>) ois.readObject();
            ois.close();
            if(files != null) {
                Log.d(TAG,"in  "+ playlistId);
                for(Map.Entry<String,ArrayList<String>> entry : files.entrySet()){
                    String sub = entry.getKey().substring(0,entry.getKey().lastIndexOf(PLAYLISTFILEFORMAT));
                    if (sub.equalsIgnoreCase(newName)) {
                        return false;
                    }
                }
                String newKey="";
                String oldKey="";
                for(Map.Entry<String,ArrayList<String>> entry : files.entrySet()){
                    String id = entry.getKey().substring(entry.getKey().lastIndexOf(PLAYLISTFILEFORMAT));
                    if(playlistId==Integer.parseInt(id.substring(5))){
                        oldKey=entry.getKey();
//                        name=entry.getKey().substring(0,entry.getKey().lastIndexOf(PLAYLISTFILEFORMAT));
                        newKey=newName+(PLAYLISTFILEFORMAT+playlistId);
                        break;
                    }
                }
                files.put(newKey,files.get(oldKey));
                files.remove(oldKey);

                FileOutputStream fos = context.openFileOutput(MAINPLAYLISTFILE, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(files);
                oos.close();

                FetchSongFilesAsync.getInstance().getAllPlaylists(context);
                return true;
            }
        }catch (Exception e){
            Log.e(TAG,"renameplaylists");
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Deletes Playlists
     * @param playlistIdsToRemove ids of playlists to remove in arraylist
     * @return true if deleted successfully
     * */
    public static boolean deletePlaylists(Context context, ArrayList<Integer> playlistIdsToRemove){
        try {
            FileInputStream streamIn = context.openFileInput(MAINPLAYLISTFILE);
            ObjectInputStream ois = new ObjectInputStream(streamIn);
            HashMap<String,ArrayList<String>> files = (HashMap<String, ArrayList<String>>) ois.readObject();
            ois.close();
            Log.d(TAG,"getAll");
            if(files != null) {
                ArrayList<String> toremove = new ArrayList<>();

                for(Map.Entry<String,ArrayList<String>> entry : files.entrySet()){
                    String id = entry.getKey().substring(entry.getKey().lastIndexOf(PLAYLISTFILEFORMAT));
                    for(int i : playlistIdsToRemove){
                        if(i==Integer.parseInt(id.substring(5))){
                            toremove.add(entry.getKey());
                        }
                    }
                }
                for(String str : toremove){
                    files.remove(str);
                }
                FileOutputStream fos = context.openFileOutput(MAINPLAYLISTFILE, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(files);
                oos.close();

                FetchSongFilesAsync.getInstance().getAllPlaylists(context);
                return true;
            }
        }catch (Exception e){
            Log.e(TAG,"deleteplaylists");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Remove songs in playlists
     * @param playlistSongsToRemove - songs to remove
     * @param playlistId - id of playlist in which the song is present
     * */
    public static boolean removeSongsInPlaylist(Context context, ArrayList<PlaylistSong> playlistSongsToRemove, int playlistId){
        try {
            FileInputStream streamIn = context.openFileInput(MAINPLAYLISTFILE);
            ObjectInputStream ois = new ObjectInputStream(streamIn);
            HashMap<String,ArrayList<String>> files = (HashMap<String, ArrayList<String>>) ois.readObject();
            ois.close();
            if(files != null) {
                //contain the uris to remove from the playlist
                ArrayList<String> toremove = new ArrayList<>();
                String key="";
                for(Map.Entry<String,ArrayList<String>> entry : files.entrySet()){
                    String id = entry.getKey().substring(entry.getKey().lastIndexOf(PLAYLISTFILEFORMAT));
                    if(playlistId==Integer.parseInt(id.substring(5))){
                        key=entry.getKey();
                        for(PlaylistSong i : playlistSongsToRemove){
                            for(String str : entry.getValue()){
                                if(i.data.equals(str)){
                                    toremove.add(str);
                                }
                            }
                        }
                        break;
                    }
                }
                Log.d(TAG,"delete "+toremove.size());
                ArrayList<String> newUris = new ArrayList<>(files.get(key));
                for(String str : toremove){
                    newUris.remove(str);
                }
                files.put(key,newUris);

                FileOutputStream fos = context.openFileOutput(MAINPLAYLISTFILE, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(files);
                oos.close();

                return true;
            }
        }catch (Exception e){
            Log.e(TAG,"deleteplaylists");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Creates PlaylistSong arraylist for playlist
     * @param playlistId id of the playlist to get the songs for
     * @return arraylist of playlist songs
     * */
    public static ArrayList<PlaylistSong> getSongsForPlaylist(@NonNull Context context,@NonNull int playlistId){
        ArrayList<PlaylistSong> playlistSongs= new ArrayList<>();
        try {
            FileInputStream streamIn = context.openFileInput(MAINPLAYLISTFILE);
            ObjectInputStream ois = new ObjectInputStream(streamIn);
            HashMap<String,ArrayList<String>> files = (HashMap<String, ArrayList<String>>) ois.readObject();
            ois.close();
            Log.d(TAG,"getSongs");
            if(files != null) {
                for(Map.Entry<String,ArrayList<String>> entry : files.entrySet()){
                    String id = entry.getKey().substring(entry.getKey().lastIndexOf(PLAYLISTFILEFORMAT)); //returns .plid#(id)
                    if(playlistId==Integer.parseInt(id.substring(5))) {
                        ArrayList<String> uris = entry.getValue();
//                        for(Song song : MediaLibrary.getTotalSongsList()){
//                            for(String uri : uris) {
//                                if (song.data.equals(uri)){
//                                    playlistSongs.add(new PlaylistSong(
//                                            song.id,
//                                            song.genre,
//                                            song.title,
//                                            song.artistName,
//                                            song.albumName,
//                                            song.duration,
//                                            song.trackNumber,
//                                            song.dateAdded,
//                                            playlistId,
//                                            song.data,
//                                            song.contentUri,
//                                            song.imageBytes));
//                                }
//                            }
//                        }
                        break;
                    }
                }
            }
        }catch (Exception e){
            Log.e(TAG,"get all");
            e.printStackTrace();
        }
        return playlistSongs;
    }
    /**
     * Add Songs to existing playlist
     * @param playlistId id of the playlist to add to
     * @param songsUri uri of songs to add
     * */
    public static boolean addSongsToPlaylist(@NonNull Context context,@NonNull int playlistId, @NonNull ArrayList<String> songsUri){
        try {
            FileInputStream streamIn = context.openFileInput(MAINPLAYLISTFILE);
            ObjectInputStream ois = new ObjectInputStream(streamIn);
            HashMap<String,ArrayList<String>> files = (HashMap<String, ArrayList<String>>) ois.readObject();
            ois.close();
            Log.d(TAG,"getSongs");
            if(files != null) {
                String playlistKey ="";
                ArrayList<String> values=new ArrayList<>();
                for(Map.Entry<String,ArrayList<String>> entry : files.entrySet()){
                    String id = entry.getKey().substring(entry.getKey().lastIndexOf(PLAYLISTFILEFORMAT)); //returns .plid#(id)
                    if(playlistId==Integer.parseInt(id.substring(5))) { //returns the id
                       playlistKey=entry.getKey();
                       values=entry.getValue();
                       break;
                    }
                }
                if(!playlistKey.equals("")) {
                    //write to existing id
                    values.addAll(songsUri);
                    files.put(playlistKey, values);
                    FileOutputStream fos = context.openFileOutput(MAINPLAYLISTFILE, Context.MODE_PRIVATE);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(files);
                    oos.close();
                }else{
                    return false;
                }

            }else{
                return false;
            }
        }catch (Exception e){
            Log.e(TAG,"get all");
            e.printStackTrace();
        }
        return true;
    }
    /**
     * Fetches all playlist present in file
     * @return arraylist of playlist
     * */
    public static ArrayList<Playlist> getAllPlaylistsInFile(@NonNull final Context context){
        ArrayList<Playlist> playlists= new ArrayList<>();
        try {
            FileInputStream streamIn = context.openFileInput(MAINPLAYLISTFILE);
            ObjectInputStream ois = new ObjectInputStream(streamIn);
            HashMap<String,ArrayList<String>> files = (HashMap<String, ArrayList<String>>) ois.readObject();
            ois.close();
            Log.d(TAG,"getAll");
            if(files != null) {
                for(Map.Entry<String,ArrayList<String>> entry : files.entrySet()){
                    String name = entry.getKey().substring(0,entry.getKey().lastIndexOf(PLAYLISTFILEFORMAT));
                    String id = entry.getKey().substring(entry.getKey().lastIndexOf(PLAYLISTFILEFORMAT));

                    playlists.add(new Playlist(Integer.parseInt(id.substring(5)),name));
                }
            }
        }catch (Exception e){
            Log.e(TAG,"get all");
            e.printStackTrace();
        }
        return playlists;
    }
    /**
     * Creates new playlist and adds to the main playlist file in app data.
     * Will be saved in a serializable hashmap in file as name.plid#(key),URIs(value).
     * @param name name of the playlist
     * @param songsUri the uri path of the selected songs
     * @return boolean - true if playlist exists already, false otherwise
     * */
    public static boolean createNewPlaylistFile(@NonNull final Context context,@NonNull String name,@NonNull ArrayList<String> songsUri) {
        try {
            Log.d(TAG,"size "+songsUri.size());
            File file = new File(context.getFilesDir(), MAINPLAYLISTFILE);
            if(file.createNewFile()) {
                    //new playlist file, first load
                    String s = name +               //i.e. name.plid0
                            PLAYLISTFILEFORMAT +
                            0;
                    Log.d(TAG,"new playlist file created: "+s);

                    HashMap<String, ArrayList<String>> hashMap = new HashMap<>();
                    hashMap.put(s, songsUri);

                    FileOutputStream fos = context.openFileOutput(MAINPLAYLISTFILE, Context.MODE_PRIVATE);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(hashMap);
                    oos.close();
            }else {
                if (!doesPlaylistExistInFile(context, name)) {
                    //append the name of playlist in file
                    StringBuilder sb = new StringBuilder();
                    int lastId = findLastPlaylistIdInFile(context); //to increment from
                    if (lastId != -1) {
                        lastId = lastId + 1;
                        sb.append(name);
                        sb.append(PLAYLISTFILEFORMAT);
                        sb.append(lastId);
                    } else { //first playlist
                        lastId = 0;
                        sb.append(name);
                        sb.append(PLAYLISTFILEFORMAT);
                        sb.append(lastId);
                    }
                    Log.d(TAG, name + " create" + Collections.singletonList(songsUri)+" id "+lastId);

                    //get hashmap from file and add to it
                    FileInputStream streamIn = context.openFileInput(MAINPLAYLISTFILE);
                    ObjectInputStream ois = new ObjectInputStream(streamIn);
                    HashMap<String, ArrayList<String>> playlistDataMap = (HashMap<String, ArrayList<String>>) ois.readObject();
                    ois.close();
                    if (playlistDataMap != null) {
//                        Log.d("PLAYLISTUTIL", playlistDataMap.size() + " size of map");
//                        for(Map.Entry<String,ArrayList<String>> entry : playlistDataMap.entrySet()){
//                            Log.d("PLAYLISTUTIL",entry.getKey()+" "+entry.getValue());
//                        }
                        playlistDataMap.put(sb.toString(), songsUri);

                        FileOutputStream fos = context.openFileOutput(MAINPLAYLISTFILE, Context.MODE_PRIVATE);
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                        oos.writeObject(playlistDataMap);
                        oos.close();
                    }
                } else
                    return true;
            }
        } catch (Exception e) {
            Log.e(TAG,"create new");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Finds last playlist id in the main file in order to increment id for new playlist
     * @return last id of playlist in file
     * */
    private static int findLastPlaylistIdInFile(@NonNull final Context context){
        //find last id if exists and increment from there
        int lastId =-1;
        try {
            FileInputStream streamIn = context.openFileInput(MAINPLAYLISTFILE);
            ObjectInputStream ois = new ObjectInputStream(streamIn);
            HashMap<String,ArrayList<String>> files = (HashMap<String, ArrayList<String>>) ois.readObject();
            ois.close();
            if(files != null) {
                //sort first
                ArrayList<Integer> ids = new ArrayList<>();
                for(Map.Entry<String,ArrayList<String>> entry : files.entrySet()){
                    String sub = entry.getKey().substring(entry.getKey().lastIndexOf(PLAYLISTFILEFORMAT));
                    ids.add(Integer.parseInt(sub.substring(5)));
                }
                Collections.sort(ids);
                //find last id
                int count =0;
                for(int i : ids){
                    count = count+1;
                    if(count==ids.size()) {
                        lastId = i;
                    }
                }
                Log.d(TAG,"size: "+files.size()+ " lastId: "+lastId);
            }
        }catch (Exception e){
            Log.e(TAG,"ERROR: 'findlast'");
            e.printStackTrace();
        }
        return lastId;
    }
    /**
     * Checks if playlist exists in file already
     * @param name of the playlist
     * @return true if it does, false otherwise
     * */
    private static boolean doesPlaylistExistInFile(@NonNull final Context context,@NonNull String name){
        try {
            FileInputStream streamIn = context.openFileInput(MAINPLAYLISTFILE);
            ObjectInputStream ois = new ObjectInputStream(streamIn);
            HashMap<String,ArrayList<String>> files = (HashMap<String, ArrayList<String>>) ois.readObject();
            ois.close();
            if(files != null) {
                Log.d(TAG,files.size()+" does exist");
                for(Map.Entry<String,ArrayList<String>> entry : files.entrySet()){
                    String sub = entry.getKey().substring(0,entry.getKey().lastIndexOf(PLAYLISTFILEFORMAT));
                    if (sub.equalsIgnoreCase(name)) {
                        return true;
                    }
                }
            }
        }catch (Exception e){
            Log.e(TAG,"ERROR: 'doesplaylistexist'");
            e.printStackTrace();
        }
        return false;
    }


}
