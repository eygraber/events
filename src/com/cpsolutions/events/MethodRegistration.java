package com.cpsolutions.events;

/**
 * Holds the information to route an event to a method.
 * @param <T> The type of {@link com.cpsolutions.events.Event} that this {@code MethodRegistration} represents.
 * @see com.cpsolutions.events.Events#subscribe(Object, MethodRegistration[])
 */
public final class MethodRegistration<T extends Event> {
    private Class<T> event;
    private String methodName;
    private RunType runType;

    /**
     * Creates a MethodRegistration meant to be passed to
     * {@link com.cpsolutions.events.Events#subscribe(Object, MethodRegistration[])}.
     * Will use {@link com.cpsolutions.events.RunType#DEFAULT}.
     * @param event a class type of an {@link com.cpsolutions.events.Event} used to match
     *              that {@code Event} to the {@code methodName}.
     * @param methodName a method name that will be called when an {@code Event}
     *                   matching {@code event} is posted.
     */
    public MethodRegistration(Class<T> event, String methodName) {
        this(event, methodName, RunType.DEFAULT);
    }

    /**
     * Creates a MethodRegistration meant to be passed to
     * {@link com.cpsolutions.events.Events#subscribe(Object, MethodRegistration[])}
     * @param event a class type of an {@link com.cpsolutions.events.Event} used to match
     *              that {@code Event} to the {@code methodName}.
     * @param methodName a method name that will be called when an {@code Event}
     *                   matching {@code event} is posted.
     * @param runType defines what {@code Thread} the method will be called on when an {@code Event}
     *                is posted.
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
     * @return the {@link com.cpsolutions.events.RunType}
     */
    public RunType getRunType() {
        return runType;
    }

    public int hashCode() {
        return getEvent().hashCode() + methodName.hashCode();
    }

    /**
     * Returns {@code true} if the {@code events} and {@code methodNames} are equal.
     * @param other the {@code Event} that is being compared.
     * @return if the {@code MethodRegistrations}s are equal
     */
    public boolean equals(MethodRegistration other) {
        return getEvent() == other.getEvent() &&
                getMethodName().equals(other.getMethodName());
    }
}
