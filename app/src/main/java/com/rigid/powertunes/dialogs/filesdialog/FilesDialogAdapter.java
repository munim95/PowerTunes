package com.rigid.powertunes.dialogs.filesdialog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rigid.powertunes.App;
import com.rigid.powertunes.MultiLevelRv;
import com.rigid.powertunes.MultiLevelRvAdapter;
import com.rigid.powertunes.MultiLevelRvItem;
import com.rigid.powertunes.R;
import com.rigid.powertunes.RvItem;
import com.rigid.powertunes.util.FileUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

public class FilesDialogAdapter extends MultiLevelRvAdapter {
//todo come back and have a look at the checkboxes
    private static final String TAG=FilesDialogAdapter.class.getSimpleName();

    private final Context context;
    private final List<MultiLevelRvItem> initialFiles;
    private MultiLevelRv multiLevelRecyclerView;
    private final float density= App.getInstance().getResources().getDisplayMetrics().density;

    public FilesDialogAdapter(Context context, List<MultiLevelRvItem> initialFiles) {
        super(initialFiles);
        this.context=context;
        this.initialFiles = initialFiles;
//        this.multiLevelRecyclerView=multiLevelRecyclerView;
//        density = context.getResources().getDisplayMetrics().density;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FilesViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.files_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        FilesViewHolder holder = (FilesViewHolder) viewHolder;
        holder.bindTo(i);
        //indent child items
        ((ViewGroup.MarginLayoutParams) holder.textLayout.getLayoutParams()).leftMargin =
                (int) ((getItemViewType(i) * 20) * density + 0.5f);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        multiLevelRecyclerView=(MultiLevelRv) recyclerView;
    }
    //get selected user uri here
    public void notifyChange(Uri uri){
        Log.d(TAG,"DATA - "+uri.getAuthority());
        if(uri.getAuthority().equals("")){

        }
    }

    public class FilesViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        ImageView expandBtn;
        CheckBox checkBox;
        LinearLayout textLayout;
        Button permissionsBtn;

        FilesViewHolder(@NonNull View itemView) {
            super(itemView);
            text=itemView.findViewById(R.id.fileTitleText);
            expandBtn=itemView.findViewById(R.id.fileExpandBtn);
            checkBox=itemView.findViewById(R.id.fileDialogCheckBox);
            textLayout=itemView.findViewById(R.id.dialogTextLayout);
            permissionsBtn=itemView.findViewById(R.id.permissionsButton);

            checkBox.setOnClickListener(v -> {
                if(!initialFiles.get(getAdapterPosition()).isChecked()) {
                    initialFiles.get(getAdapterPosition()).setChecked(true);
                }else{
                    initialFiles.get(getAdapterPosition()).setChecked(false);
                }
                initialFiles.get(getAdapterPosition()).setUnderlined(false);
                text.setPaintFlags(0);

                //checks how many of the current dir's children are checked/unchecked it sets the parent dir to checked or underlined accordingly
                checkParentsForChild(getAdapterPosition());
                setChildrenChecked(getAdapterPosition(),checkBox.isChecked());
            });
            //set click listener on LinearLayout because the click area is bigger than the ImageView
            textLayout.setOnClickListener(v -> {
                //check permissions
                if(!initialFiles.get(getAdapterPosition()).getDocumentFile().canRead()){
                    Toast.makeText(context,"Please allow permissions to access.",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!initialFiles.get(getAdapterPosition()).isExpanded() &&
                        !initialFiles.get(getAdapterPosition()).areChildrenLoaded()) {
                    MultiLevelRvItem currentItem = initialFiles.get(getAdapterPosition());
                    List<RvItem> children = new ArrayList<>();
                    for (DocumentFile f : currentItem.getDocumentFile().listFiles()) {
                        if (f.isDirectory()) {
                            MultiLevelRvItem multiLevelRvItem = new MultiLevelRvItem(currentItem.getLevel() + 1);
                            multiLevelRvItem.addDocumentFile(f);
                            DocumentFile[] files = f.listFiles();
                            if (files.length != 0) {
                                for(DocumentFile check : files){
                                    if(check.isDirectory()) {
                                        multiLevelRvItem.setChildrenAvailable(true);
                                        break;
                                    }
                                }
                            }
                            children.add(multiLevelRvItem);
                            //add children to current file
                        }
                    }
                    getRvItemList().get(getAdapterPosition()).addChildren(children);
                    // set click event on expand button here
                    initialFiles.get(getAdapterPosition()).addChildren(children);
                }

                multiLevelRecyclerView.toggleItemsGroup(getAdapterPosition());
                // rotate the icon based on the current state
                expandBtn.animate()
                        .rotation(getRvItemList().get(getAdapterPosition()).isExpanded() ? 90 : 0)
                        .start();

                if(initialFiles.get(getAdapterPosition()).isChecked())
                    setChildrenChecked(getAdapterPosition(),
                            initialFiles.get(getAdapterPosition()).isChecked());
            });
        }
        private void bindTo(int pos){
            MultiLevelRvItem fileItemModel = initialFiles.get(pos);
            if(fileItemModel.getLevel()==0) {
                text.setText(fileItemModel.getDocumentFile().getUri().toString().contains("primary")?
                        Build.MODEL:
                        "SD CARD");
            }else
                text.setText(fileItemModel.getDocumentFile().getName());
            if (fileItemModel.getLevel() == 0 &&
                    !fileItemModel.getDocumentFile().canRead()) {
                if(permissionsBtn.getVisibility()==View.GONE) {
                    checkBox.setVisibility(View.GONE);
                    expandBtn.setVisibility(View.INVISIBLE);
                    permissionsBtn.setVisibility(View.VISIBLE);
                }

                permissionsBtn.setOnClickListener((v -> {
                    String[] strings = FileUtil.getExternalDocumentContentUris(context);
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI,
                            fileItemModel.getDocumentFile()
                                    .getUri().toString().contains("primary")?
                                    Uri.parse(strings[0]) : Uri.parse(strings[1]));
                    FilesDialogFragment.getInstance().startActivityForResult(intent,0);
                }));
            } else {
                if(permissionsBtn.getVisibility()==View.VISIBLE) {
                    permissionsBtn.setVisibility(View.GONE);
                    expandBtn.setVisibility(View.VISIBLE);
                    checkBox.setVisibility(View.VISIBLE);
                }

                checkBox.setChecked(fileItemModel.isChecked());
                text.setPaintFlags(fileItemModel.isUnderlined() ? text.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG : 0);

                if (fileItemModel.areChildrenAvailable()) {
                    expandBtn.setRotation(fileItemModel.isExpanded() ? 90 : 0);
                    expandBtn.setVisibility(View.VISIBLE);
                } else {
                    expandBtn.setVisibility(View.INVISIBLE);
                }
            }
        }
    }
    /**
     * Sets children of respective parent checked recursively
     * @param position position of parent
     * */
    private void setChildrenChecked(int position,boolean checked){
        //check all children
        if(initialFiles.get(position).areChildrenLoaded()) {
            if (checked) {
                for (RvItem item : initialFiles.get(position).getChildren()) {
                    if (initialFiles.get(position).isExpanded()) {
                        RecyclerView.ViewHolder holder = multiLevelRecyclerView
                                .findViewHolderForAdapterPosition(item.getPosition());
                        if (holder != null) {
                            ((FilesViewHolder) holder).checkBox.setChecked(true);
                            ((FilesViewHolder) holder).text.setPaintFlags(0);
                        }
                        ((MultiLevelRvItem) item).setChecked(true);
                        ((MultiLevelRvItem) item).setUnderlined(false);
                        setChildrenChecked(item.getPosition(),true);
                    }
                    ((MultiLevelRvItem) item).setChecked(true);
                    ((MultiLevelRvItem) item).setUnderlined(false);
                }
                //uncheck all children
            } else {
                for (RvItem item : initialFiles.get(position).getChildren()) {
                    if (initialFiles.get(position).isExpanded()) {
                        RecyclerView.ViewHolder holder = multiLevelRecyclerView.findViewHolderForAdapterPosition(item.getPosition());
                        if (holder != null) {
                            ((FilesViewHolder) holder).checkBox.setChecked(false);
                            ((FilesViewHolder) holder).text.setPaintFlags(0);
                        }
                        ((MultiLevelRvItem) item).setChecked(false);
                        ((MultiLevelRvItem) item).setUnderlined(false);
                        setChildrenChecked(item.getPosition(),false);
                    }
                    ((MultiLevelRvItem) item).setChecked(false);
                    ((MultiLevelRvItem) item).setUnderlined(false);

                }
            }
        }
    }

    /**
     * Checks how many of the current dir's children are checked/unchecked then
     * it sets the parent dir to checked or underlined accordingly
     *
     * @param position position of the child to iterate the parents
     * */
    private void checkParentsForChild(int position){
        if(initialFiles.get(position).getLevel()>0){
            int checked =0;
            int unchecked =0;
            //get parents for the item at position
            for(MultiLevelRvItem parent : callRecursiveGetParents(position)){
                //check how many children are checked/unchecked for each parent
                for(RvItem child : parent.getChildren()) {
                    if (!((MultiLevelRvItem) child).isChecked()) {
                        ++unchecked;
                    } else {
                        ++checked;
                    }
                }

                RecyclerView.ViewHolder holder = multiLevelRecyclerView
                        .findViewHolderForAdapterPosition(parent.getPosition());
                if(holder==null) {
                    Log.e(TAG,"checkParentsForChild: ViewHolder is null!!!");
                    return;
                }
                if (checked != 0 && unchecked == 0) {
                    //all checked
                    ((FilesViewHolder) holder).checkBox.setChecked(true);
                    ((FilesViewHolder) holder).text.setPaintFlags(0);
                    parent.setChecked(true);
                    parent.setUnderlined(false);
                } else if (unchecked != 0 && checked == 0) {
                    //all unchecked
                    ((FilesViewHolder) holder).checkBox.setChecked(false);
                    ((FilesViewHolder) holder).text.setPaintFlags(0);
                    parent.setChecked(false);
                    parent.setUnderlined(false);
                } else if (checked > 0 && unchecked > 0) {
                    //some checked and unchecked
                    ((FilesViewHolder) holder).checkBox.setChecked(false);
                    ((FilesViewHolder) holder).text
                            .setPaintFlags(((FilesViewHolder) holder).text.getPaintFlags() |
                                            Paint.UNDERLINE_TEXT_FLAG);
                    parent.setChecked(false);
                    parent.setUnderlined(true);
                }
            }
        }
    }

    /**
     * Gets list of all parents of a child going up the hierarchy
     * @param position position of child
     * @return array of parents, recursive call from {@link #getParents(int, List)}
     * */
    private List<MultiLevelRvItem> callRecursiveGetParents(int position){
        List<MultiLevelRvItem> parents =new ArrayList<>();
        getParents(position,parents);
        return parents;
    }
    /**
     * Not to be called directly
     * Recursive method to be used in conjunction with {@link #callRecursiveGetParents(int)}
     * */
    private void getParents(int position, List<MultiLevelRvItem> parents){
        DocumentFile parentFile = initialFiles.get(position).getDocumentFile().getParentFile();
        if (parentFile != null) {
            for (MultiLevelRvItem item : initialFiles) {
                if (item.getDocumentFile().getUri().equals(parentFile.getUri())) {
                    parents.add(item);
                    getParents(item.getPosition(), parents);
                    break;
                }
            }
        }
    }

    /**
     * Fetches selected files taking checked/underlined files in consideration
     * @return HashSet of selected files to avoid duplicates
     * */
    public Set<MultiLevelRvItem> getSelectedFiles(){
        Set<MultiLevelRvItem> selectedFiles = new HashSet<>();
        getSelectedFilesRecursive(initialFiles,selectedFiles);
        return selectedFiles;
    }
    /**
     * Not to be called directly
     * Recursive method to be used in conjunction with {@link #getSelectedFiles()}
     * */
    //fetch all selected files recursively depending if underlined or not
    private void getSelectedFilesRecursive(List<?> files, Set<MultiLevelRvItem> selectedFiles){
        for(MultiLevelRvItem item:(List<MultiLevelRvItem>)files){
//            Log.d(TAG,"files selection " +
//                    (item.isChecked()?"checked "+item.getFile().getName():item.isUnderlined()?"underlined "+item.getFile().getName():"null"));
            if(item.isChecked()){
                selectedFiles.add(item);
            }else if(item.isUnderlined()){
                //get all child folders
                getSelectedFilesRecursive(item.getChildren(),selectedFiles);
            }
        }
    }



}
