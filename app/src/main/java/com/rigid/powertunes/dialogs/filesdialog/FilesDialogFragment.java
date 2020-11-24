package com.rigid.powertunes.dialogs.filesdialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.multilevelview.models.RecyclerViewItem;
import com.rigid.powertunes.GlobalVariables;
import com.rigid.powertunes.MultiLevelRv;
import com.rigid.powertunes.MultiLevelRvItem;
import com.rigid.powertunes.RvItem;
import com.rigid.powertunes.ScanClickCallback;
import com.rigid.powertunes.provider.FetchSongFilesAsync;
import com.rigid.powertunes.R;
import com.rigid.powertunes.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

public class FilesDialogFragment extends DialogFragment {
    /**
     * fetches selected songs from files and plays successfully
     * */

    private static final String TAG = FilesDialogFragment.class.getSimpleName();
//    private final String ROOT_NODE ="/storage/";
//    private final String FILENAMEPREF = "dirprefdata"; //DO NOT ALTER - name in user app data
//    private final String FILENAMESELECTION = "selectiondata"; //DO NOT ALTER - name in user app data

    private MultiLevelRv rv;
//    private List<MultiLevelRvItem> itemModels = (List<MultiLevelRvItem>)getRootFiles(new File(ROOT_NODE),0);
    private HashMap<String,Boolean> checkedOrUnderlinedFilesMap;
    private ScanClickCallback scanClickCallback;
    private Button scan, cancel;

    private static FilesDialogFragment newInstance;
    private FilesDialogFragment() {
        super();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==0 && resultCode== Activity.RESULT_OK){
            Uri uri = data.getData(); // uri that has been allowed by user
            if(uri==null) {
                Toast.makeText(getContext(),"Unexpected error fetching data. Check the Help section.",Toast.LENGTH_LONG)
                        .show();
                return;
            }
            //todo cases where
            // 1. uri not the same as the one stored in shared pref - change it (RARE SINCE DOC URIS ARE MOST LIKELY API INDEPENDENT)
            // 2. user selects folders instead of the whole directory -
            //    check if its part of the parent and list accordingly

            ((FilesDialogAdapter)rv.getAdapter()).notifyChange(uri);
            rv.getAdapter().notifyDataSetChanged(); // temp testing
//            DocumentFile documentFile =DocumentFile.fromTreeUri(getContext(),uris);
        }else
            Toast.makeText(getContext(),"Having trouble? Check the Help section in Settings.", Toast.LENGTH_LONG)
                    .show();
    }

    public static FilesDialogFragment getInstance() {
        if(newInstance==null) {
            newInstance = new FilesDialogFragment();
        }
        return newInstance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        checkedOrUnderlinedFilesMap=readPreferences();
//        if(checkedOrUnderlinedFilesMap !=null&& checkedOrUnderlinedFilesMap.size()!=0) {
//            for(FileItemModel itemModel : itemModels){
//                for(Map.Entry<String,Boolean> entry: checkedOrUnderlinedFilesMap.entrySet()){
//                    if(entry.getValue()){ // true if checked, underlined if false
//                        if(itemModel.getFile().getPath().equals(entry.getKey()))
//                            itemModel.setChecked(true);
//                    }else{
//                        if(itemModel.getFile().getPath().equals(entry.getKey())) {
//                            itemModel.setUnderlined(true);
//                            readFromMapRecursive(itemModel, checkedOrUnderlinedFilesMap);
//                        }
//                    }
//                }
//            }
//        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.files_dialog_layout, container,false);
        rv=v.findViewById(R.id.filesRv);
        scan = v.findViewById(R.id.dialogFilesScanBtn);
        cancel = v.findViewById(R.id.dialogFilesCancelBtn);
        return v;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().setTitle("Select Folders");

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new FilesDialogAdapter(getContext(),getRootFiles(getContext())));
        rv.setToggleItemOnClick(false);

        cancel.setOnClickListener(v0 -> dismiss());
        scan.setOnClickListener(v1 -> {
            if(((FilesDialogAdapter)rv.getAdapter()).getSelectedFiles().size()==0) {
                Toast.makeText(getActivity(),"No Files Selected...",Toast.LENGTH_SHORT).show();
                return;
            }
            scan.setEnabled(false);
            //get audio files and update song list
            FetchSongFilesAsync.getInstance().execute(getActivity(),
                    ((FilesDialogAdapter)rv.getAdapter()).getSelectedFiles(),null,true);
//            savePreferencesToFile();
//            saveFilePathsInDisk();
            scanClickCallback.onScanClicked();
            dismiss();
        });
    }
    /**
     * Get root or starting files e.g. Internal and SD card storage
     * */
    private List<MultiLevelRvItem> getRootFiles(Context context){
        String[] storage = FileUtil.getExternalDocumentContentUris(context);
        List<MultiLevelRvItem> multiLevelRvItems = new ArrayList<>();
        for(String str : storage){
            //always consists of two values, one is null if no sd card present
            if(str!=null){
                DocumentFile documentFile = DocumentFile.fromTreeUri(context, Uri.parse(str));
                if(documentFile!=null) {
                    MultiLevelRvItem multiLevelRvItem = new MultiLevelRvItem(0);
                    multiLevelRvItem.setChildrenAvailable(documentFile.listFiles().length!=0);
                    multiLevelRvItem.addDocumentFile(documentFile);
                    multiLevelRvItems.add(multiLevelRvItem);
                }
            }
        }
        return multiLevelRvItems;
    }
    public void setScanClickCallback(ScanClickCallback scanClickCallback){
      this.scanClickCallback=scanClickCallback;
    }
    /**
     * RECURSIVE
     * if underlined item model exists then check for checked/underlined children
     * */
    private void readFromMapRecursive(RecyclerViewItem item, HashMap<String,Boolean> selectedFiles){
        for(RecyclerViewItem model:item.getChildren()){
            for(Map.Entry<String,Boolean> entry1: selectedFiles.entrySet()) {
                if (entry1.getKey().equals(((FileItemModel)model).getFile().getPath())) {
                    if(entry1.getValue()){
                        ((FileItemModel)model).setChecked(true);
                    }else{
                        ((FileItemModel)model).setUnderlined(true);
                        readFromMapRecursive(model,selectedFiles);
                    }
                }
            }
        }
    }

    private HashMap<String,Boolean> readPreferences(){
        try {
            FileInputStream streamIn = getActivity().openFileInput(GlobalVariables.FILENAMEPREF);
            ObjectInputStream ois = new ObjectInputStream(streamIn);
            HashMap<String,Boolean> files = (HashMap<String,Boolean>) ois.readObject(); //string - path, boolean -true if checked, false if underlined
            ois.close();
            if(files != null) {
//                checkedOrUnderlinedFilesMap = files;
                return files;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    //save user selection in file to maintain dialog fragment config
//    private void generateSelectedFilesMap(){
//        checkedOrUnderlinedFilesMap = new HashMap<>();
//        for(FileItemModel itemModel:itemModels){
//            if(itemModel.isChecked()) {
//                checkedOrUnderlinedFilesMap.put(itemModel.getFile().getPath(),itemModel.isChecked());
//            }else if(itemModel.isUnderlined()){
//                checkedOrUnderlinedFilesMap.put(itemModel.getFile().getPath(),itemModel.isChecked());
//                if(!itemModel.isExpanded()){
//                    writeToMapRecursive(itemModel, checkedOrUnderlinedFilesMap);
//                }
//            }
//        }

//    }
    private void writeToMapRecursive(RecyclerViewItem item, HashMap<String,Boolean> map){
        for(RecyclerViewItem model:item.getChildren()){
            if(((FileItemModel)model).isChecked()){
                map.put(((FileItemModel)model).getFile().getPath(),((FileItemModel)model).isChecked());
            }else if(((FileItemModel)model).isUnderlined()){
                map.put(((FileItemModel)model).getFile().getPath(),((FileItemModel)model).isChecked());
                writeToMapRecursive(model,map);
            }
        }
    }
    private void savePreferencesToFile(){
//        generateSelectedFilesMap();
        FileUtil.writeObjectToFile(getActivity(),GlobalVariables.FILENAMEPREF, checkedOrUnderlinedFilesMap);
    }
    //for use at app startup - load songs
//    private void saveSelectionToFile(){
//        //creates 2d data model objects for fileitemmodel attrs to save in file to recreate later
//        Object[][] modelData = new Object[((FilesDialogAdapter)rv.getAdapter()).getSelectedFiles().size()][4];
//        int r = -1;
//        for(FileItemModel fileItemModel : ((FilesDialogAdapter)rv.getAdapter()).getSelectedFiles()){
//            //each item model has 3 attrs to it thus r - fileItemModel (row), c - its columns
//            r = r+1;
//            int c = -1;
//            while(c<3) {
//                //add 4 columns to the model
//                c = c+1;
//                switch (c){
//                    case 0:
//                        modelData[r][c] = fileItemModel.getFile(); //file object
//                        break;
//                    case 1:
//                        modelData[r][c] = fileItemModel.isUnderlined(); //boolean
//                        break;
//                    case 2:
//                        modelData[r][c] = fileItemModel.isChecked(); //boolean
//                        break;
//                    case 3:
//                        modelData[r][c] = fileItemModel.getLevel(); //int
//                        break;
//                }
//            }
//        }
//        //store data in disk (app data)
//        FileUtil.writeObjectToFile(getActivity(),GlobalVariables.FILENAMESELECTION,modelData);
//    }
//
//    private void saveFilePathsInDisk(){
//        String[] strs = new String[((FilesDialogAdapter)rv.getAdapter()).getSelectedFiles().size()];
//        int i = 0;
//        for(FileItemModel fileItemModel : ((FilesDialogAdapter)rv.getAdapter()).getSelectedFiles()){
//            strs[i++] = fileItemModel.getFile().getPath();
//        }
//        FileUtil.writeObjectToFile(getActivity(),GlobalVariables.FILENAMESELECTION,strs);
//
//    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onResume() {
        // Store access variables for window and blank point
        Window window = getDialog().getWindow();
        Point size = new Point();
        // Store dimensions of the screen in `size`
        window.getWindowManager().getDefaultDisplay().getSize(size);
        // Set the height of the dialog proportional to 80% of the screen width
        if(window.getWindowManager().getDefaultDisplay().getRotation()==Surface.ROTATION_90||
                window.getWindowManager().getDefaultDisplay().getRotation()==Surface.ROTATION_270){
            window.setLayout((int)(size.x*0.70), (int)(size.y * 0.80));
        }
        else
            window.setLayout(size.x, (int)(size.y * 0.80));

        window.setGravity(Gravity.CENTER);
        super.onResume();
    }

    //todo instead of loading all folders on first load, load them on demand
    /**
     * gets all the files in root node (inc. SD Cards) ("/storage")- for listing out all files in dialog
     * @param file root node file
     * @param level for determining levels in multi-recycler view adapter, start with 0 as root
     * */
    private List<?> getRootFiles(File file,int level){
        List<RvItem> itemList = new ArrayList<>();
        File[] files = file.listFiles();
        if(file.exists() && file.isDirectory() && files!=null){
            for (File file1 : files) {
                if (file1.isDirectory()) { //we dont want files just parent folders
                    MultiLevelRvItem itemModel = new MultiLevelRvItem(level);
                    if(level==0 && (file1.getName().equalsIgnoreCase("emulated"))){
                        File file2 = new File(Environment.getExternalStorageDirectory().toURI());
                        itemModel.addFile(file2);
                        itemModel.setChildrenAvailable(true);
//                        itemModel.addChildren((List<RecyclerViewItem>) getRootFiles(file2, level + 1));
                        itemList.add(itemModel);
                    } else {
                        if(!file1.getName().equalsIgnoreCase("self")) { //memory card
                            itemModel.addFile(file1);
                            itemModel.setChildrenAvailable(true);
//                            itemModel.addChildren((List<RecyclerViewItem>) getRootFiles(file1, level + 1));
                            itemList.add(itemModel);
                        }
                    }
                }
            }
        }
        return itemList;
    }


    private boolean externalMemoryAvailable() {
        if (Environment.isExternalStorageRemovable()) {
            //device support sd card. We need to check sd card availability.
            String state = Environment.getExternalStorageState();
            return state.equals(Environment.MEDIA_MOUNTED) ||
                    state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
        } else {
            //device does not support sd card.
            return false;
        }
    }

}
