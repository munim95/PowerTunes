package com.rigid.powertunes.misc;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class GlideViewTargetSub extends CustomViewTarget<ImageView,Drawable> {
    private ImageView view;

    public GlideViewTargetSub(@NonNull ImageView view) {
        super(view);
        this.view=view;
    }

    @Override
    protected void onResourceCleared(@Nullable Drawable placeholder) {
    }
    @Override
    public void onLoadFailed(@Nullable Drawable errorDrawable) {
        view.setImageDrawable(errorDrawable);
    }

    @Override
    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
        view.setImageDrawable(resource);
    }


}
