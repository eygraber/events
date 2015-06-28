package com.staticbloc.events;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: eygraber
 * Date: 6/21/2015
 * Time: 5:11 PM
 * To change this template use File | Settings | File Templates.
 */
/*package*/ abstract class EventDispatcher {
  private Set<Class<? extends Event>> dispatchBlocks;

  public EventDispatcher() {
    dispatchBlocks = new HashSet<>();
  }

  public void blockDispatch(Class<? extends Event> eventDispatchToBlock) {
    dispatchBlocks.add(eventDispatchToBlock);
  }

  public void unblockDispatch(Class<? extends Event> eventDispatchToUnblock) {
    dispatchBlocks.remove(eventDispatchToUnblock);
  }

  protected boolean isDispatchBlocked(Class<? extends Event> eventDispatch) {
    return dispatchBlocks.contains(eventDispatch);
  }

  public abstract <T extends Event> void dispatchEvent(T event);

  public abstract Set<Class<? extends Event>> getDispatchableEvents();

  public abstract RunType getRunTypeForEvent(Class<? extends Event> eventClass);
}
