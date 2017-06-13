package com.example.yanghang.clipboard.OthersView.swipebacklayout.lib.app;

import com.example.yanghang.clipboard.OthersView.swipebacklayout.lib.SwipeBackLayout;

/**
 * @author Yrom
 */
public interface SwipeBackActivityBase {
    /**
     * @return the SwipeBackLayout associated with this activity.
     */
    SwipeBackLayout getSwipeBackLayout();

    void setSwipeBackEnable(boolean enable);

    /**
     * Scroll out contentView and finish the activity
     */
    void scrollToFinishActivity();

}
