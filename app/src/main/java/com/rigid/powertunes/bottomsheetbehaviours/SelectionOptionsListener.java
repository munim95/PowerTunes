package com.rigid.powertunes.bottomsheetbehaviours;

public interface SelectionOptionsListener {
    void closeSelect();
    void deleteSelect(boolean hasRemove);
    void addToPlaylistSelect();
    void addToQueueSelect();
    void albumArtSelect();
    void shareSelect();
    void editInforSelect();
}
