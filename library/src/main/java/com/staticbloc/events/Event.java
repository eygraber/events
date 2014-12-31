package com.staticbloc.events;

/**
 * The base class for all events.
 */
public class Event<T> {
    private String id;
    private T extra;

    /**
     * Creates an {@code Event} without an {@code id} or {@code extra}.
     */
    public Event() {
        this(null, null);
    }

    /**
     * Creates an {@code Event} with an {@code id} and no {@code extra}.
     *
     * @param id the id of this event.
     */
    public Event(String id) {
        this(id, null);
    }

    /**
     * Creates an {@code Event} with an {@code id} and {@code extra}.
     *
     * @param id the id of this event.
     * @param extra the extra for this event.
     */
    public Event(String id, T extra) {
        this.id = id;
        this.extra = extra;
    }

    /**
     * @return the id for this event.
     */
    public String getId() {
        return id;
    }

    /**
     * @return the extra for this event.
     */
    public T getExtra() {
        return extra;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (extra != null ? !extra.equals(event.extra) : event.extra != null) return false;
        if (id != null ? !id.equals(event.id) : event.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (extra != null ? extra.hashCode() : 0);
        return result;
    }
}
