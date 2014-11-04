package com.staticbloc.events;

/**
 * Holds the information to route an event to a method.
 * @param <T> The type of {@link Event} that this {@code MethodRegistration} represents.
 * @see Events#subscribe(Object, MethodRegistration[])
 */
public final class MethodRegistration<T extends Event> {
    private Class<T> event;
    private String methodName;
    private RunType runType;

    /**
     * Creates a MethodRegistration meant to be passed to
     * {@link Events#subscribe(Object, MethodRegistration[])}.
     * Will use {@link RunType#DEFAULT}.
     * @param event a class type of an {@link Event} used to match
     *              that {@code Event} to the {@code methodName}
     * @param methodName a method name that will be called when an {@code Event}
     *                   matching {@code event} is posted
     */
    public MethodRegistration(Class<T> event, String methodName) {
        this(event, methodName, RunType.DEFAULT);
    }

    /**
     * Creates a MethodRegistration meant to be passed to
     * {@link Events#subscribe(Object, MethodRegistration[])}
     * @param event a class type of an {@link Event} used to match
     *              that {@code Event} to the {@code methodName}
     * @param methodName a method name that will be called when an {@code Event}
     *                   matching {@code event} is posted
     * @param runType defines what {@link Thread} the method will be called on when an {@code Event}
     *                is posted
     */
    public MethodRegistration(Class<T> event, String methodName, RunType runType) {
        this.event = event;
        this.methodName = methodName;
        this.runType = runType;
    }

    /**
     *
     * @return the event class
     */
    public Class<T> getEvent() {
        return event;
    }

    /**
     *
     * @return the method name
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     *
     * @return the {@link RunType}
     */
    public RunType getRunType() {
        return runType;
    }

    public int hashCode() {
        return getEvent().hashCode() + methodName.hashCode() + runType.hashCode();
    }

    /**
     * Returns true if {@code o} is an {@code MethodRegistrtion}, and both
     * {@code MethodRegistrtion}'s {@link MethodRegistration#getEvent()},
     * {@link MethodRegistration#getMethodName()}, and {@link MethodRegistration#getRunType()} are equal.
     * @param o the {@link Object} that is being compared
     * @return if the {@code MethodRegistration}s are equal
     */
    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        else if(!(o instanceof MethodRegistration)) {
            return false;
        }

        MethodRegistration other = (MethodRegistration) o;
        return getEvent() == other.getEvent() &&
                getMethodName().equals(other.getMethodName()) &&
                getRunType() == other.getRunType();
    }
}
