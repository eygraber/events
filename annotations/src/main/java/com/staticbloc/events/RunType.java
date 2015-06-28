package com.staticbloc.events;

public enum RunType {
    /**
     * The event handlers will be called on the same thread that {@code post} was called on.
     * <br/>
     * <br/>
     * <b>Note:</b> This will block the posting thread. {@link EventHandler}s that perform long running
     * operations should use {@link RunType#BACKGROUND}
     */
    DEFAULT,
    /**
     * The event handlers will be called from the main thread.
     * <br/>
     * <br/>
     * <b>Note:</b> This will block the posting thread. {@link EventHandler}s that perform long running
     * operations should use {@link RunType#BACKGROUND}
     */
    MAIN,
    /**
     * The event handlers will be called on a background thread.
     */
    BACKGROUND
}
