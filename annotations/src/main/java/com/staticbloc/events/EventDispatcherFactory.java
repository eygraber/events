package com.staticbloc.events;

/**
 * Created with IntelliJ IDEA.
 * User: eygraber
 * Date: 6/21/2015
 * Time: 11:43 PM
 * To change this template use File | Settings | File Templates.
 */
public interface EventDispatcherFactory {
  EventDispatcher createDispatcher(Object o);
}
