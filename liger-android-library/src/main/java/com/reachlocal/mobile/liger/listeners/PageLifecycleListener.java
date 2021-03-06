package com.reachlocal.mobile.liger.listeners;

import com.reachlocal.mobile.liger.ui.PageFragment;

/**
 * Interface that objects that need to listen for page lifecycle events.
 */
public interface PageLifecycleListener {

    public void onPageClosed(PageFragment page);

    public void onHiddenChanged(PageFragment page, boolean hidden);

    public void onPageFinished(PageFragment page);
}
