package com.rigid.powertunes;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;

public class MultiLevelRvItem extends RvItem{
    private File file;
    private DocumentFile documentFile;
    private boolean isChecked;
    private boolean isUnderlined;

    public MultiLevelRvItem(int level) {
        super(level);
    }

    //file itself * remove *
    public void addFile(File file){
        this.file=file;
    }
    public File getFile(){ return file;}

    public void addDocumentFile(DocumentFile file){
        this.documentFile=file;
    }
    public DocumentFile getDocumentFile(){
        return documentFile;
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


    @Override
    public int hashCode() {
        return getDocumentFile().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        MultiLevelRvItem item = (MultiLevelRvItem) obj;
        return getDocumentFile().equals(item.getDocumentFile());
    }
}
