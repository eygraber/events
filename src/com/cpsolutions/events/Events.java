package com.cpsolutions.events;

import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public final class Events {
    /**
     * The default instance
     */
    private static class Singleton {
        public static final Events instance = new Events();
    }

    private final Map<Class<? extends Event>, Set<MethodRegistrationWrapper>> map;
    private final Object mapLock = new Object();
    private final Map<Object, Set<Class<? extends Event>>> reverseLookup;
    private final Object reverseLookupLock = new Object();
    private final Handler mainPoster;

    /**
     * Create a new {@code Events} instance.
     */
    public Events() {
        map = new HashMap<Class<? extends Event>, Set<MethodRegistrationWrapper>>();
        reverseLookup = new HashMap<Object, Set<Class<? extends Event>>>();
        mainPoster = new Handler(Looper.getMainLooper());
    }

    /**
     *
     * @return the default instance
     */
    public static Events getDefault() {
        return Singleton.instance;
    }

    /**
     * Subscribe the {@code subscriber} for events of type {@code event} using {@code methodName} as the
     * event handler. Will use {@link com.cpsolutions.events.RunType#DEFAULT}.
     * @param subscriber an {@code Object} to subscribe for notifications
     * @param event the type of {@code Event} the {@code subscriber} will subscribe to
     * @param methodName the name of the method that will be the event handler
     */
    public <T extends Event> void subscribe(Object subscriber, Class<T> event, String methodName) {
        subscribe(subscriber, new MethodRegistration<T>(event, methodName));
    }

    /**
     * Subscribe the {@code subscriber} for events of type {@code event} using {@code methodName} as the
     * event handler.
     * @param subscriber an {@code Object} to subscribe for notifications
     * @param event the type of {@code Event} the {@code subscriber} will subscribe to
     * @param methodName the name of the method that will be the event handler
     * @param runType defines what {@code Thread} the event handler will be called on when an {@code Event}
     *                is posted.
     */
    public <T extends Event> void subscribe(Object subscriber, Class<T> event, String methodName, RunType runType) {
        subscribe(subscriber, new MethodRegistration<T>(event, methodName, runType));
    }

    /**
     * Subscribe the {@code subscriber} for events using event handlers described in {@code methodRegistrations}.
     * @param subscriber an {@code Object} to subscribe for notifications
     * @param methodRegistrations an array of {@link com.cpsolutions.events.MethodRegistration} that holds the
     *                            event and event handler information
     */
    public void subscribe(Object subscriber, MethodRegistration<?>... methodRegistrations) {
        Class<?> subscriberClass = subscriber.getClass();
        for(MethodRegistration<?> methodRegistration : methodRegistrations) {
            Class<? extends Event> event = methodRegistration.getEvent();
            String methodName = methodRegistration.getMethodName();

            Method methodToInvoke;
            try {
                methodToInvoke = subscriberClass.getMethod(methodName, event);
            } catch (NoSuchMethodException e) {
                throw new NoSuchMethodError(String.format("No method %s in class %s that takes %s",
                        methodName, subscriberClass.getSimpleName(), event.getSimpleName()));
            }

            synchronized (mapLock) {
                Set<MethodRegistrationWrapper> registrations = map.get(event);
                if(registrations == null) {
                    registrations = new HashSet<MethodRegistrationWrapper>();
                }
                registrations.add(new MethodRegistrationWrapper(subscriberClass, methodRegistration, methodToInvoke));
                map.put(event, registrations);
            }

            synchronized (reverseLookupLock) {
                Set<Class<? extends Event>> reverseLookups = reverseLookup.get(subscriber);
                if(reverseLookups == null) {
                    reverseLookups = new HashSet<Class<? extends Event>>();
                }
                reverseLookups.add(event);
                reverseLookup.put(subscriber, reverseLookups);
            }
        }
    }

    /**
     * Unsubscribes an {@code Object} from all {@code Events}.
     * @param subscriber the {@code Object} to unsubscribe
     */
    public void unsubscribe(Object subscriber) {
        synchronized (reverseLookupLock) {
            Set<Class<? extends Event>> reverseLookups = reverseLookup.get(subscriber);
            if(reverseLookups == null) {
                return;
            }

            for(Class<? extends Event> event : reverseLookups) {
                synchronized (mapLock) {
                    Set<MethodRegistrationWrapper> registrations = map.get(event);
                    if(registrations != null) {
                        Iterator<MethodRegistrationWrapper> it = registrations.iterator();
                        while(it.hasNext()) {
                            MethodRegistrationWrapper registration = it.next();
                            if(registration.getSubscriber() == subscriber) {
                                it.remove();
                            }
                        }
                    }
                }
            }

            reverseLookup.remove(subscriber);
        }
    }

    /**
     * Posts an {@link com.cpsolutions.events.Event} to its subscribers
     * @param event the {@code Event} to post
     */
    public <T extends Event> void post(T event) {
        List<MethodRegistrationWrapper> registrations;
        Set<MethodRegistrationWrapper> registrationsSet = map.get((event.getClass()));;
        if(registrationsSet != null) {
            registrations = new ArrayList<MethodRegistrationWrapper>(registrationsSet);
        }
        else {
            return;
        }

        for(MethodRegistrationWrapper registration : registrations) {
            if(registration.getRunType() == RunType.DEFAULT) {
                invoke(event, registration);
            }
            else if(registration.getRunType() == RunType.MAIN) {
                mainPoster.post(new InvokeRunnable<T>(event, registration));
            }
        }
    }

    private static <T extends Event> void invoke(T event, MethodRegistrationWrapper registration) {
        try {
            registration.getMethodToInvoke().invoke(registration.getSubscriber(), event);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(String.format("IllegalAccessError - " +
                            "Couldn't invoke %s on instance of %s for %s",
                    registration.getMethodName(), registration.getSubscriberClass().getSimpleName(),
                    event.getClass().getSimpleName()));
        } catch (InvocationTargetException e) {
            throw new RuntimeException(String.format("InvocationTargetException - " +
                            "Couldn't invoke %s on instance of %s for %s",
                    registration.getMethodName(), registration.getSubscriberClass().getSimpleName(),
                    event.getClass().getSimpleName()));
        }
    }

    private final static class InvokeRunnable<T extends Event> implements Runnable {
        private T event;
        private MethodRegistrationWrapper registration;

        public InvokeRunnable(T event, MethodRegistrationWrapper registration) {
            this.event = event;
            this.registration = registration;
        }

        @Override
        public void run() {
            Events.invoke(event, registration);
        }
    }

    private final static class MethodRegistrationWrapper {
        private Object subscriber;
        private MethodRegistration<?> methodRegistration;
        private Method methodToInvoke;

        public MethodRegistrationWrapper(Object subscriber, MethodRegistration<?> methodRegistration, Method methodToInvoke) {
            this.subscriber = subscriber;
            this.methodRegistration = methodRegistration;
            this.methodToInvoke = methodToInvoke;
        }

        public Object getSubscriber() {
            return subscriber;
        }

        public Class<?> getSubscriberClass() {
            return subscriber.getClass();
        }

        public String getMethodName() {
            return methodRegistration.getMethodName();
        }

        public RunType getRunType() {
            return methodRegistration.getRunType();
        }

        public Method getMethodToInvoke() {
            return methodToInvoke;
        }

        public int hashCode() {
            return methodRegistration.hashCode();
        }

        public boolean equals(MethodRegistrationWrapper other) {
            return methodRegistration.equals(other.methodRegistration);
        }
    }
}