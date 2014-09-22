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

    public int hashCode() {
        int hash = super.hashCode();
        if(id != null) {
            hash = id.hashCode();
            if(extra != null) {
                hash += extra.hashCode();
            }
        }
        return hash;
    }

    /**
     * Returns true if {@code o} is an {@code Event}, and both
     * {@code Event}'s ids and extras are equal.
     * @param o the {@link Object} that is being compared
     * @return if the events are equal
     */
    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        else if(!(o instanceof Event)) {
            return false;
        }

        Event other = (Event) o;
        if(getId() != null) {
            if(getId().equals(other.getId())) {
                if(getExtra() != null) {
                    return getExtra().equals(other.getExtra());
                }
                else {
                    return other.getExtra() == null;
                }
            }
            else {
                return false;
            }
        }
        else {
            if(other.getId() == null) {
                if(getExtra() != null) {
                    return getExtra().equals(other.getExtra());
                }
                else {
                    return other.getExtra() == null;
                }
            }
            else {
                return false;
            }
        }
    }
}
