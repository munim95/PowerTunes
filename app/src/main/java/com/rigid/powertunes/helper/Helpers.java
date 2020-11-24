package com.rigid.powertunes.helper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by MunimsMac on 12/02/2018.
 */

public class Helpers {
    //views offset from the top bounds of a viewgroup
    public static Rect getOffsetViewBounds(ViewGroup parent, View child) {
        Rect offsetViewBounds = new Rect();
        child.getDrawingRect(offsetViewBounds);
        parent.offsetDescendantRectToMyCoords(child, offsetViewBounds);
        return offsetViewBounds;
    }

//    //true if the particular view is in the visible items list currently on screen
//    public static boolean isItemVisible(LinearLayoutManager lm, View v) {
//        if(v!=null)
//        for (View view : getVisibleItemsList(lm)) {
//            if (view == v)
//                return true;
//            Log.d("is item visible?", "YES");
//        }
//        Log.d("is item visible?", "NO" );
//        return false;
//    }

    public static int[][] getVisibleItemsPositions(LinearLayoutManager lm) {
        int firstVisibleItem = lm.findFirstVisibleItemPosition();
        int lastVisibleItem = lm.findLastVisibleItemPosition();
        int firstCompletelyVisibleItem = lm.findFirstCompletelyVisibleItemPosition();
        int lastCompletelyVisibleItem = lm.findLastCompletelyVisibleItemPosition();
        if(firstVisibleItem == RecyclerView.NO_POSITION || lastVisibleItem == RecyclerView.NO_POSITION){
            Log.d("NO POSITION",firstVisibleItem==RecyclerView.NO_POSITION?"first Item NO POS":
                    lastVisibleItem==RecyclerView.NO_POSITION?"Last No Pos":"NO POSITION");
        }
        int[][] firstandlast = new int[][]{{firstVisibleItem,lastVisibleItem},
                {firstCompletelyVisibleItem,lastCompletelyVisibleItem} };

        Log.d("firstfullvisible?",firstVisibleItem==firstCompletelyVisibleItem?"Yes": "No");
        Log.d("lastfullvisible?",lastVisibleItem==lastCompletelyVisibleItem?"Yes": "No");

        return firstandlast;
    }

    // secs to hour:min:sec format
    public static String timeConversion(int seconds) {

    final int MINUTES_IN_AN_HOUR = 60;
    final int SECONDS_IN_A_MINUTE = 60;

    int minutes = seconds / SECONDS_IN_A_MINUTE;
    seconds -= minutes * SECONDS_IN_A_MINUTE;
    int hours = minutes / MINUTES_IN_AN_HOUR;
    minutes -= hours * MINUTES_IN_AN_HOUR;
        //return only min:seconds(eg. 04:05) if hours is 0
        if(hours==0 )
            return (minutes<10?"0"+minutes:minutes) + ":" + (seconds<10?"0"+seconds:seconds);

    return (hours<10?"0"+hours:hours) +":"+ (minutes<10?"0"+minutes:minutes) +":"+ (seconds<10?"0"+seconds:seconds);
    }

    //converting bytes to a readable format --- FOR LOGGING USE ONLY
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static int[][] getMaxXYForScreen(Context context){
        Display mdisp = ((Activity)context).getWindowManager().getDefaultDisplay();
        Point mdispSize = new Point();
        mdisp.getSize(mdispSize);
        int[][] xy = new int[2][1];
        xy[0][0]=mdispSize.x;
        xy[1][0]=mdispSize.y;
        return xy;
    }
    public static int countChar(String str, char c) {
        int count = 0;
        for(int i=0; i < str.length(); i++)
        {    if(str.charAt(i) == c)
            count++;
        }

        return count;
    }

//
//    private Point getWindowDefaultDimensions(){
//        Display display = getWindowManager().getDefaultDisplay();
//        Point point = new Point();
//        display.getSize(point);
//        return point;
//    }
}
