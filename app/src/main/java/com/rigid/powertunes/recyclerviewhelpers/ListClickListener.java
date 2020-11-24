package com.rigid.powertunes.recyclerviewhelpers;

import android.view.View;

/**
 * Created by MunimsMac on 16/01/2018.
 */

public interface ListClickListener {

    void click(View view, int position);

    void onLongClick(View view, int position);

}
