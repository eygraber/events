package com.cpsolutions.events;

/**
 * The base class for all events.
 */
public class Event {
    private String id;
    private Object extra;

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
    public Event(String id, Object extra) {
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
     *
     * @return the extra for this event.
     */
    public Object getExtra() {
        return extra;
    }

    public int hashCode() {
        int hash = 0;
        if(id != null) {
            hash = id.hashCode();
            if(extra != null) {
                hash += extra.hashCode();
            }
        }
        return hash;
    }

    /**
     * Returns true if both {@code Event}'s ids and extras are equal.
     * @param other the {@code Event} that is being compared.
     * @return if the events are equal.
     */
    public boolean equals(Event other) {
        if(getId() != null) {
            if(getId().equals(other.getId())) {
                if(getExtra() != null) {
                    return equals(other.getExtra());
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
                    return equals(other.getExtra());
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
