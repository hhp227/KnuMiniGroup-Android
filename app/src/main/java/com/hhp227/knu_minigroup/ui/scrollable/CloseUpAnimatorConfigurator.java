package com.hhp227.knu_minigroup.ui.scrollable;

import android.animation.ObjectAnimator;

public interface CloseUpAnimatorConfigurator {

    /**
     * Note that {@link ObjectAnimator#setDuration(long)} would erase current value set by {@link CloseUpIdleAnimationTime} if any present
     * @param animator current {@link ObjectAnimator} object to animate close-up animation of a {@link ScrollableLayout}
     */
    void configure(ObjectAnimator animator);
}
