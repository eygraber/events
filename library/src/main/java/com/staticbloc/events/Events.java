package com.staticbloc.events;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Events {
  private static EventDispatcherFactory eventDispatcherFactory = getEventDispatcherFactory();

  private static EventDispatcherFactory getEventDispatcherFactory() {
    try {
      return (EventDispatcherFactory) Class.forName("com.staticbloc.events.EventDispatcherFactoryImpl").getConstructor().newInstance();
    }
    catch (Exception e) {
      Log.wtf("Events", "Couldn't find EventDispatcherFactoryImpl. Please email info@staticbloc.com for assistance", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * The default instance
   */
  private static class Singleton {
    public static final Events instance = new Events();
  }

  private static final Handler mainPoster = new Handler(Looper.getMainLooper());
  private static final ExecutorService defaultEventDispatcher = Executors.newSingleThreadExecutor();

  private final Map<Object, EventDispatcher> objectForwardMap;
  private final Map<Class<? extends Event>, Set<EventDispatcher>> eventForwardMap;

  private final Set<OnEventListenerWrapper> onEventListeners;
  private final Object onEventListenersLock = new Object();

  private final ExecutorService eventExecutor;

  private static class OnEventListenerWrapper extends EventDispatcher {
    private OnEventListener onEventListener;
    private RunType runType;

    public OnEventListenerWrapper(OnEventListener onEventListener, RunType runType) {
      this.onEventListener = onEventListener;
      this.runType = runType;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      OnEventListenerWrapper that = (OnEventListenerWrapper) o;

      return onEventListener == that.onEventListener && runType == that.runType;
    }

    @Override
    public int hashCode() {
      int result = onEventListener != null ? onEventListener.hashCode() : 0;
      result = 31 * result + (runType != null ? runType.hashCode() : 0);
      return result;
    }

    @Override
    public <T extends Event> void dispatchEvent(T event) {
      onEventListener.onEvent(event);
    }

    @Override
    public Set<Class<? extends Event>> getDispatchableEvents() {
      return Collections.emptySet();
    }

    @Override
    public RunType getRunTypeForEvent(Class<? extends Event> eventClass) {
      return runType;
    }
  }

  public interface OnEventListener {
    void onEvent(Event event);
  }

  /**
   * Create a new {@code Events} instance that uses the default {@link java.util.concurrent.ExecutorService}.
   */
  public Events() {
    this(defaultEventDispatcher);
  }

  /**
   * Create a new {@code Events} instance. If {@code useDefaultExecutor} is {@code true} then the
   * default {@link java.util.concurrent.ExecutorService} will be used. Otherwise,
   * {@link java.util.concurrent.Executors#newSingleThreadExecutor()} will be used.
   * @param useDefaultExecutor whether or not to use the default {@code ExecutorService}
   */
  public Events(boolean useDefaultExecutor) {
    this((useDefaultExecutor ? defaultEventDispatcher : Executors.newSingleThreadExecutor()));
  }

  /**
   * Create a new {@code Events} instance.
   * @param eventExecutor the {@link java.util.concurrent.ExecutorService} to use
   */
  public Events(ExecutorService eventExecutor) {
    objectForwardMap = new IdentityHashMap<>();
    eventForwardMap = new HashMap<>();

    onEventListeners = new HashSet<>();

    this.eventExecutor = eventExecutor;
  }

  /**
   * @return the default instance
   */
  public static Events getDefault() {
    return Singleton.instance;
  }

  /**
   * Subscribe the {@code subscriber} to events that were specified with {@link EventHandler} methods.
   */
  public synchronized void subscribe(Object subscriber) {
    if(objectForwardMap.containsKey(subscriber)) {
      throw new IllegalStateException(String.format("%s already subscribed to events", subscriber.toString()));
    }

    EventDispatcher eventDispatcher = eventDispatcherFactory.createDispatcher(subscriber);
    if(eventDispatcher == null) {
      throw new IllegalStateException("You cannot pass an object that has not registered any @EventHandlers to Events.subscribe");
    }

    objectForwardMap.put(subscriber, eventDispatcher);

    for(Class<? extends Event> eventClass : eventDispatcher.getDispatchableEvents()) {
      Set<EventDispatcher> dispatchers = eventForwardMap.get(eventClass);
      if(dispatchers == null) {
        dispatchers = new HashSet<>();
        eventForwardMap.put(eventClass, dispatchers);
      }
      dispatchers.add(eventDispatcher);
    }
  }

  public void subscribe(OnEventListener onEventListener, RunType runType) {
    synchronized (onEventListenersLock) {
      onEventListeners.add(new OnEventListenerWrapper(onEventListener, runType));
    }
  }

  public synchronized void resubscribeToEvent(Object subscriber, Class<? extends Event> eventClass) {
    if(!objectForwardMap.containsKey(subscriber)) {
      throw new IllegalStateException(String.format("%s never subscribed to %s", subscriber.toString(), eventClass.toString()));
    }

    EventDispatcher eventDispatcher = objectForwardMap.get(subscriber);
    eventDispatcher.unblockDispatch(eventClass);
  }

  public synchronized void unsubscribeFromEvent(Object subscriber, Class<? extends Event> eventClass) {
    if(!objectForwardMap.containsKey(subscriber)) {
      throw new IllegalStateException(String.format("%s never subscribed to %s", subscriber.toString(), eventClass.toString()));
    }

    EventDispatcher eventDispatcher = objectForwardMap.get(subscriber);
    eventDispatcher.blockDispatch(eventClass);
  }

  /**
   * Unsubscribes an {@code Object} from all {@code Events}.
   * @param subscriber the {@code Object} to unsubscribe
   */
  public synchronized void unsubscribe(Object subscriber) {
    EventDispatcher eventDispatcher = objectForwardMap.remove(subscriber);

    if(eventDispatcher != null) {
      for(Class<? extends Event> eventClass : eventDispatcher.getDispatchableEvents()) {
        Set<EventDispatcher> dispatchers = eventForwardMap.get(eventClass);
        if(dispatchers != null) {
          dispatchers.remove(eventDispatcher);
        }
      }
    }
  }

  public void unsubscribe(OnEventListener onEventListener, RunType runType) {
    synchronized (onEventListenersLock) {
      onEventListeners.remove(new OnEventListenerWrapper(onEventListener, runType));
    }
  }

  /**
   * Posts an {@link Event} to its subscribers
   * @param event the {@code Event} to post
   */
  public <T extends Event> void post(T event) {
    Set<EventDispatcher> eventDispatchers = eventForwardMap.get((event.getClass()));
    if(eventDispatchers == null) {
      return;
    }

    Set<EventDispatcher> dispatchers = new HashSet<>(eventDispatchers);
    dispatchers.addAll(onEventListeners);

    List<EventDispatcher> backgroundDispatchers = new ArrayList<>();
    List<EventDispatcher> mainThreadDispatchers = new ArrayList<>();
    List<EventDispatcher> postingThreadDispatchers = new ArrayList<>();

    for(EventDispatcher dispatcher : dispatchers) {
      RunType runType = dispatcher.getRunTypeForEvent(event.getClass());
      if(runType == RunType.BACKGROUND) {
        backgroundDispatchers.add(dispatcher);
      }
      else if(runType == RunType.MAIN) {
        mainThreadDispatchers.add(dispatcher);
      }
      else if(runType == RunType.DEFAULT) {
        postingThreadDispatchers.add(dispatcher);
      }
    }

    // send the background dispatchers to be posted on a background thread
    eventExecutor.execute(new EventDispatchRunnable<>(backgroundDispatchers, event));

    // send the main thread dispatchers to be posted on the main thread, or invoke them if we are on the main thread
    if(Looper.myLooper() == Looper.getMainLooper()) {
      for(EventDispatcher eventDispatcher : mainThreadDispatchers) {
        eventDispatcher.dispatchEvent(event);
      }
    }
    else {
      mainPoster.post(new EventDispatchRunnable<>(mainThreadDispatchers, event));
    }

    // invoke the posting thread dispatchers
    for(EventDispatcher eventDispatcher : postingThreadDispatchers) {
      eventDispatcher.dispatchEvent(event);
    }
  }

  private static class EventDispatchRunnable<T extends Event> implements Runnable {
    private T event;
    private List<EventDispatcher> dispatchers;

    public EventDispatchRunnable(List<EventDispatcher> dispatchers, T event) {
      this.dispatchers = dispatchers;
      this.event = event;
    }

    @Override
    public void run() {
      for(EventDispatcher eventDispatcher : dispatchers) {
        eventDispatcher.dispatchEvent(event);
      }
    }
  }
}
