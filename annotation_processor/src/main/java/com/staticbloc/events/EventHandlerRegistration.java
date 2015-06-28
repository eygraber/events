package com.staticbloc.events;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Created with IntelliJ IDEA.
 * User: eygraber
 * Date: 6/21/2015
 * Time: 3:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class EventHandlerRegistration {
  private String methodName;
  private TypeMirror enclosingClass;
  private String enclosingClassFQN;
  private TypeMirror eventParameter;
  private String eventParameterFQN;
  private RunType runType;

  public EventHandlerRegistration(EventHandler eventHandler, String methodName, TypeElement enclosingClass, TypeElement eventParameter) {
    this.methodName = methodName;
    runType = eventHandler.runType();

    this.enclosingClass = enclosingClass.asType();
    this.eventParameter = eventParameter.asType();

    enclosingClassFQN = enclosingClass.getQualifiedName().toString();
    eventParameterFQN = eventParameter.getQualifiedName().toString();
  }

  public String getMethodName() {
    return methodName;
  }

  public TypeMirror getEnclosingClass() {
    return enclosingClass;
  }

  public String getEnclosingClassFQN() {
    return enclosingClassFQN;
  }

  public TypeMirror getEventParameter() {
    return eventParameter;
  }

  public String getEventParameterFQN() {
    return eventParameterFQN;
  }

  public RunType getRunType() {
    return runType;
  }

  @Override
  public String toString() {
    return String.format("%s#%s(%s) - %s", enclosingClass.toString(), methodName, eventParameter.toString(), runType.name());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    EventHandlerRegistration that = (EventHandlerRegistration) o;

    if (!enclosingClassFQN.equals(that.enclosingClassFQN)) {
      return false;
    }
    return eventParameterFQN.equals(that.eventParameterFQN);

  }

  @Override
  public int hashCode() {
    int result = enclosingClassFQN.hashCode();
    result = 31 * result + eventParameterFQN.hashCode();
    return result;
  }
}
