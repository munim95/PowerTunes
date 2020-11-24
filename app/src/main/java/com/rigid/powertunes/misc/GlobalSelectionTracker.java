package com.rigid.powertunes.misc;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.rigid.powertunes.Selection;
//import com.rigid.powertunes.activities.MainActivity;
//import com.rigid.powertunes.bottomsheetbehaviours.SelectionBottomSheet;
//import com.rigid.powertunes.selection.CustomSelectionTracker;
//import com.rigid.powertunes.selection.ItemDetailsLookup;
//import com.rigid.powertunes.selection.ItemKeyProvider;
//import com.rigid.powertunes.selection.SelectionPredicates;
//import com.rigid.powertunes.selection.StorageStrategy;

public class GlobalSelectionTracker  {
    private final String TAG ="GLOBALSELECTIONTRACKER";
//    private static CustomSelectionTracker<Uri> selectionTracker;
//    private static SelectionObserver selectionObserver;
    private static Selection mySelection;

//    private static CustomSelectionTracker<Uri> nullifiedTracker=null;
//    private static SelectionObserver nullifiedObserver=null;

//    public GlobalSelectionTracker(String selectionId, RecyclerView rv, ItemDetailsLookup lookup,
//                                  ItemKeyProvider keyProvider, SelectionObserver _selectionObserver){
//       selectionTracker= new CustomSelectionTracker.Builder<>(
//               selectionId,
//               rv,
//               keyProvider,
//               lookup,
//               StorageStrategy.createParcelableStorage(Uri.class))
//               .withSelectionPredicate(SelectionPredicates.<Uri>createSelectAnything())
//               .build();
//        selectionTracker.addObserver(selectionObserver());
//        selectionObserver=_selectionObserver;
//
//    }
    public GlobalSelectionTracker(Context context, RecyclerView recyclerView, Selection.SelectionCallback selectionCallback){
        mySelection=new Selection(context, recyclerView,selectionCallback);
    }
//    public static CustomSelectionTracker<Uri> getSelectionTracker(){
//        return selectionTracker;
//    }
    public static Selection getMySelection(){
        return mySelection;
    }
//    public static void enableSelectionTracker(boolean enable){
//        if(!enable){
//            nullifiedTracker=selectionTracker;
//            nullifiedObserver=selectionObserver;
//
//            selectionTracker = null;
//            selectionObserver = null;
//        }else{
//            selectionTracker= nullifiedTracker;
//            selectionObserver = nullifiedObserver;
//
//            nullifiedTracker=null;
//            nullifiedObserver=null;
//        }
//
//    }
    //selection observer
//    private CustomSelectionTracker.SelectionObserver selectionObserver(){
//        return new CustomSelectionTracker.SelectionObserver() {
//            @Override
//            public void onItemStateChanged(@NonNull Object key, boolean selected) {
//                if(selectionObserver!=null)
//                    selectionObserver.onItemStateChanged(key,selected);
//            }
//
//            @Override
//            public void onSelectionRefresh() {
//                if(selectionObserver!=null)
//                    selectionObserver.onSelectionRefresh();
//
//            }
//
//            @Override
//            public void onSelectionChanged() {
//                if(selectionObserver!=null)
//                    selectionObserver.onSelectionChanged();
//            }
//
//            @Override
//            public void onSelectionRestored() {
//                if(selectionObserver!=null)
//                    selectionObserver.onSelectionRestored();
//
//            }
//        };
//    }

    public interface SelectionObserver{
        void onItemStateChanged(@NonNull Object key, boolean selected);
        void onSelectionRefresh();
        void onSelectionChanged();
        void onSelectionRestored();
    }

}
