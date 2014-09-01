package com.cpsolutions.events;

public enum RunType {
    /**
     * The event handler will be called on the same thread that {@code post} was called on.
     * @see com.cpsolutions.events.Events#post(Event)
     */
    DEFAULT,
    /**
     * The even handler will be called from the main thread.
     */
    MAIN
}
