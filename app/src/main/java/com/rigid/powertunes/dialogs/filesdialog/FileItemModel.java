package com.rigid.powertunes.dialogs.filesdialog;

import com.multilevelview.models.RecyclerViewItem;

import java.io.File;
import java.io.Serializable;

public class FileItemModel extends RecyclerViewItem {
    private File file;
    private boolean isChecked;
    private boolean isUnderlined;

    public FileItemModel(int level) {
        super(level);
    }

    //file itself
    public void addFile(File file){
        this.file=file;
    }
    public File getFile(){
        return file;
    }
    //is checked in list?
    public void setChecked(boolean checked){
        isChecked=checked;
    }
    public boolean isChecked(){
        return isChecked;
    }
    //is underlined in list?
    public void setUnderlined(boolean underlined) {
        isUnderlined = underlined;
    }
    public boolean isUnderlined(){
        return isUnderlined;
    }

}
