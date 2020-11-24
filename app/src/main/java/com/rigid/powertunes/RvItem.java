package com.rigid.powertunes;

import java.util.List;

public abstract class RvItem {
    /**
     * Children will be added at run time
     **/
    private List<RvItem> children;

    private int level;

    private int position;

    private boolean expanded=false;

    private boolean childrenAvailable;

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    RvItem(int level){
        this.level = level;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel(){
        return level;
    }

    public List<RvItem> getChildren() {
        return children;
    }

    public void addChildren(List<RvItem> children) {
        this.children = children;
    }

    public void setChildrenAvailable(boolean t){
        childrenAvailable = t;
    }

    //predetermine if it has children before dynamically adding them
    public boolean areChildrenAvailable(){ return childrenAvailable; }

    public boolean areChildrenLoaded(){
        return (children !=null && children.size() > 0);
    }
}
