package com.example.spotifywrappedapplication;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class WrappedHelper {
    public static Animation animation(Context context, String anim) {
        anim = anim.toLowerCase().trim();
        if (anim.equals("fly in")) {
            return AnimationUtils.loadAnimation(context, R.anim.fly_in_top_animation);
        } else if (anim.equals("fly out")) {
            return AnimationUtils.loadAnimation(context, R.anim.fly_out_animation);
        } else if (anim.equals("fade in")) {
            return AnimationUtils.loadAnimation(context, R.anim.fade_in_animation);
        } else if (anim.equals("fade out")) {
            return AnimationUtils.loadAnimation(context, R.anim.fade_out_animation);
        } else if (anim.equals("flip in")) {
            return AnimationUtils.loadAnimation(context, R.anim.flip_in_animation);
        } else if (anim.equals("flip out")) {
            return AnimationUtils.loadAnimation(context, R.anim.flip_out_animation);
        } else if (anim.equals("fly in bottom")) {
            return AnimationUtils.loadAnimation(context, R.anim.fly_in_bottom_animation);
        } else if (anim.equals("float")) {
            return AnimationUtils.loadAnimation(context, R.anim.float_animation);
        }

        throw new IllegalArgumentException("animation not available.");
    }

    /**
     * flips the card
     */
    public static void flipCard(Context context, View frontCard, View backCard) {
        if (backCard.getVisibility() == View.INVISIBLE) {
            frontCard.startAnimation(animation(context, "fade out"));
            frontCard.setVisibility(View.INVISIBLE);
            backCard.setVisibility(View.VISIBLE);
            backCard.startAnimation(animation(context, "fade in"));
        } else {
            backCard.startAnimation(animation(context, "fade out"));
            backCard.setVisibility(View.INVISIBLE);
            frontCard.setVisibility(View.VISIBLE);
            frontCard.startAnimation(animation(context, "fade in"));
        }
    }

}
