package com.staticbloc.events;

import java.util.Comparator;

import javax.lang.model.util.Types;

/**
 * Created by eliezer on 6/27/15.
 */
public class EventComparator implements Comparator<EventHandlerRegistration> {
  private Types typeUtils;

  public EventComparator(Types typeUtils) {
    this.typeUtils = typeUtils;
  }

  @Override
  public int compare(EventHandlerRegistration lhs, EventHandlerRegistration rhs) {
    if(typeUtils.isSubtype(lhs.getEventParameter(), rhs.getEventParameter())) {
      return -1;
    }
    else if(typeUtils.isSubtype(rhs.getEventParameter(), lhs.getEventParameter())) {
      return 1;
    }
    else {
      return lhs.getEventParameterFQN().compareTo(rhs.getEventParameterFQN());
    }
  }
}
