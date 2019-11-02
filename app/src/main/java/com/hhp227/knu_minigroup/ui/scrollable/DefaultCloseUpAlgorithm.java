package com.hhp227.knu_minigroup.ui.scrollable;

public class DefaultCloseUpAlgorithm implements CloseUpAlgorithm {

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFlingFinalY(ScrollableLayout layout, boolean isScrollingBottom, int nowY, int suggestedY, int maxY) {
        return isScrollingBottom ? 0 : maxY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIdleFinalY(ScrollableLayout layout, int nowY, int maxY) {
        final boolean shouldScrollToTop = nowY < (maxY / 2);
        return shouldScrollToTop ? 0 : maxY;
    }
}
