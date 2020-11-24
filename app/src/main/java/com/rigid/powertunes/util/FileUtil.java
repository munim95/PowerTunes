package com.rigid.powertunes.util;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.Toast;

import com.rigid.powertunes.provider.FetchSongFilesAsync;
import com.rigid.powertunes.dialogs.filesdialog.FileItemModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

public class FileUtil {
    private static String TAG = FileUtil.class.getSimpleName();
    public static void writeObjectToFile(Context context,String fileName, Object data){
        try {
            File file = new File(context.getFilesDir(), fileName);
            file.createNewFile();

            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(data);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //*not used* - no need for the whole object
    //reads selection and creates fileitemmodel set for async task
    public static int readTrackSelectionFileAndExecute(@NonNull Context context, @NonNull final String filename,boolean showToast){
        try {
            FileInputStream streamIn = context.openFileInput(filename);
            ObjectInputStream ois = new ObjectInputStream(streamIn);
            Object[][] files = (Object[][]) ois.readObject();
            ois.close();
            if(files != null) {
                Set<FileItemModel> set = new HashSet<>();
                for (Object[] aModelData : files) {
                    FileItemModel fileItemModel = new FileItemModel((int) aModelData[3]); //level
                    for (int j = 0; j < 4; j++) {
                        switch (j) {
                            case 0:
                                fileItemModel.addFile((File) aModelData[j]); //file
                                break;
                            case 1:
                                fileItemModel.setUnderlined((boolean) aModelData[j]); //underlined
                                break;
                            case 2:
                                fileItemModel.setChecked((boolean) aModelData[j]); //checked
                                break;
                            case 3:
                                set.add(fileItemModel); //add to set of user selected folders
                                break;
                        }
                    }
                }
//                FetchSongFilesAsync.getInstance().execute(context,set,null,showToast);
                return 0; //find audio files based on selection
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }
    //replacement for prev method to reduce overhead by only saving string[] to disk
    /**
     * Look for saved user selected file paths for songs
     * @return true if executing
     *         false if files did not exist
     * */
    public static boolean readIt(@NonNull Context context, @NonNull final String filename,boolean showToast){
        try {
            FileInputStream streamIn = context.openFileInput(filename);
            ObjectInputStream ois = new ObjectInputStream(streamIn);
            String[] files = (String[]) ois.readObject();
            ois.close();
            if(files != null) {
                FetchSongFilesAsync.getInstance().execute(context,null,files,showToast);
                return true; //find audio files based on selection
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes Files
     * @param paths paths of files to delete
     * @return ArrayList of paths deleted
     * */
    public static ArrayList<String> deleteSongs(Context context,String[] paths){
        ArrayList<String> successPaths = new ArrayList<>();
        try {
            if(paths.length>0) {
                int leftOver;
                for(String path : paths){
                    if(new File(path).delete()) {
                        successPaths.add(path);
                    }
                }
                leftOver=paths.length-successPaths.size();
                if(leftOver == 0) {
                    Toast.makeText(context, String.format("Deleted %s songs successfully.",successPaths.size()), Toast.LENGTH_SHORT).show();
                }else if(leftOver > 0 && leftOver != paths.length){
                    Toast.makeText(context, String.format("%s files deleted. Could not delete %s songs.",successPaths.size(),leftOver), Toast.LENGTH_SHORT).show();
                }else if(leftOver == paths.length) {
                    Toast.makeText(context, String.format("Could not delete %s songs.",paths.length), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            if(successPaths.size()!=paths.length)
                Toast.makeText(context, String.format("Permission denied while deleting '%s'.",successPaths.get(successPaths.size()-1)), Toast.LENGTH_SHORT).show();
        }

        return successPaths;
    }
    /**
     * Calls recursive method getAudioFilesForFilePath and scans the given file path for audio files
     * @param file file path to scan for audio
     * @return audio files arraylist
     * */

    public static ArrayList<String> getAudioFilesForFile(File file){
        ArrayList<String> audioFiles=new ArrayList<>();
        getAudioFilesForFilePath(file,audioFiles);
        return audioFiles;
    }
    //recursively gets audio files from directories
    private static void getAudioFilesForFilePath(File file, ArrayList<String> audioFiles){
        if(file.exists()){
            File[] files = file.listFiles(audioFilter());
            if(files!=null) {
                for (File file1 : files) {
                    if (!file1.isDirectory()) {
                        audioFiles.add(file1.getPath());
                        continue;
                    }
                    //is a dir and not empty
                    if (file1.list() != null) {
                        getAudioFilesForFilePath(file1, audioFiles);
                    }
                }
            }
        }
//        Log.d("folders",""+audioFiles.size());
    }

    //filter for supported audio codecs
    private static FilenameFilter audioFilter(){
        return new FilenameFilter() {
            File f;
            @Override
            public boolean accept(File dir, String name) {
                if(name.endsWith(".mp3")||name.endsWith(".mp4") ||
                        name.endsWith(".aac") || name.endsWith(".3gp") ||
                        name.endsWith(".ogg") || name.endsWith(".mkv") ||
                        name.endsWith(".wav") || name.endsWith(".flac") ||
                        name.endsWith(".opus")|| name.endsWith(".m4a")){
//                    Log.d("FILEFILTER","name "+name + " dir "+dir.getName());
                    return true;
                }
//                Log.d("FILEFILTER","f "+name + " dir "+dir.getName());
                f=new File(dir.getAbsolutePath()+"/"+name);
                return f.isDirectory();
            }
        };
    }
    public static ArrayList<Uri> getAudioFilesForFile(DocumentFile file){
        ArrayList<Uri> audioFiles=new ArrayList<>();
        getAudioFilesForFilePath(file,audioFiles);
        return audioFiles;
    }
    //recursively gets audio files from directories
    private static void getAudioFilesForFilePath(DocumentFile file, ArrayList<Uri> audioFiles){
        if(file.exists()){
            DocumentFile[] files = file.listFiles();
            if(files.length!=0) {
                for (DocumentFile file1 : files) {
                    if (!file1.isDirectory()) {
                        String lastPath = file1.getUri().getLastPathSegment();
                        if(lastPath.matches(".*(\\.mp3|\\.ogg|\\.aac|\\.flac|\\.wav|\\.opus|\\.mkv|\\.m4a|\\.3gp|\\.mp4)"))
                            audioFiles.add(file1.getUri());
                        continue;
                    }
                    //file is a non empty dir
                    if (file1.listFiles().length != 0) {
                        getAudioFilesForFilePath(file1, audioFiles);
                    }
                }
            }
        }
//        Log.d("folders",""+audioFiles.size());
    }

    //will return primary if sd card not available
    public static String[] getExternalDocumentContentUris(Context context){
        //primary or secondary
        String[] externals = new String[2];
        externals[0] = "content://com.android.externalstorage.documents/tree/primary%3A/document/primary%3A";
        externals[1] = null;
        //check for secondary storage
        File[] ext_dirs = context.getExternalFilesDirs(null);
        if(ext_dirs.length>1){
            for(File file : ext_dirs){
                if(file!=null) {
                    if (!Environment.isExternalStorageEmulated(file)) {
                        int count = 0;
                        String[] strings = file.getPath().split("/");
                        for (String str : strings) {
                            if (str.equalsIgnoreCase("storage")) {
                                externals[1] = "content://com.android.externalstorage.documents/tree/"
                                        +strings[count + 1]+"%3A/document/"+strings[count + 1]+"%3A";
                                break;
                            }
                            ++count;
                        }
                        break;
                    }
                }
            }
        }
        return externals;
    }
}
