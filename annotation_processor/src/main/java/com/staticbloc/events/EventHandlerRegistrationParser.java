package com.staticbloc.events;

import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created with IntelliJ IDEA.
 * User: eygraber
 * Date: 6/21/2015
 * Time: 4:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class EventHandlerRegistrationParser {
  public static class EventHandlerParseException extends Exception {
    public EventHandlerParseException(String message) {
      super(message);
    }
  }

  private final Types typeUtils;
  private final Elements elementUtils;

  public EventHandlerRegistrationParser(ProcessingEnvironment processingEnv) {
    typeUtils = processingEnv.getTypeUtils();
    elementUtils = processingEnv.getElementUtils();
  }

  public EventHandlerRegistration parse(Element methodElement) throws EventHandlerParseException {
    EventHandler handler = methodElement.getAnnotation(EventHandler.class);
    if(handler == null) {
      return null;
    }

    checkMethodModifiers(methodElement);

    ExecutableElement eventHandlerMethod = checkMethodForParameterizedType(methodElement);

    VariableElement eventParameterElement = checkMethodParameter(eventHandlerMethod);

    return createEventHandlerMethodInfo(handler, eventHandlerMethod, eventParameterElement);
  }

  private void checkMethodModifiers(Element methodElement) throws EventHandlerParseException {
    Set<Modifier> modifiers = methodElement.getModifiers();
    if(!modifiers.contains(Modifier.PUBLIC)) {
      throw new EventHandlerParseException("@EventHandler can only be applied to a public method");
    }
    else if(modifiers.contains(Modifier.STATIC)) {
      throw new EventHandlerParseException("@EventHandler cannot be applied to a static method");
    }
    else if(modifiers.contains(Modifier.ABSTRACT)) {
      throw new EventHandlerParseException("@EventHandler cannot be applied to an abstract method");
    }

    if(methodElement.getKind() != ElementKind.METHOD) {
      throw new EventHandlerParseException("@EventHandler can only be applied to methods");
    }
  }

  private ExecutableElement checkMethodForParameterizedType(Element methodElement) throws EventHandlerParseException {
    ExecutableElement eventHandlerMethod = (ExecutableElement) methodElement;

    List<? extends TypeParameterElement> parameterizedTypes = eventHandlerMethod.getTypeParameters();
    if(parameterizedTypes != null && !parameterizedTypes.isEmpty()) {
      throw new EventHandlerParseException("@EventHandler must take a single non-parameterized parameter that implements Event");
    }

    return eventHandlerMethod;
  }

  private VariableElement checkMethodParameter(ExecutableElement eventHandlerMethod) throws EventHandlerParseException {
    List<? extends VariableElement> parameters = eventHandlerMethod.getParameters();
    if(parameters != null && parameters.size() == 1) {
      VariableElement eventParameterElement = parameters.get(0);

      if(!typeUtils.isAssignable(eventParameterElement.asType(), elementUtils.getTypeElement("com.staticbloc.events.Event").asType())) {
        throw new EventHandlerParseException("@EventHandler must take a single parameter that implements Event");
      }

      return eventParameterElement;

    }
    else {
      throw new EventHandlerParseException("@EventHandler must take a single parameter that implements Event");
    }
  }

  private EventHandlerRegistration createEventHandlerMethodInfo(EventHandler handler, ExecutableElement eventHandlerMethod,
                                                              VariableElement eventParameterElement) throws EventHandlerParseException {

    TypeElement enclosingClass = (TypeElement) eventHandlerMethod.getEnclosingElement();
    if(!enclosingClass.getModifiers().contains(Modifier.PUBLIC)) {
      throw new EventHandlerParseException("The enclosing class of the @EventHandler must be public");
    }
    else {
      Element enclosingElement = enclosingClass.getEnclosingElement();
      do {
        if(enclosingElement != null && enclosingElement instanceof TypeElement && !enclosingElement.getModifiers().contains(Modifier.PUBLIC)) {
          throw new EventHandlerParseException("All of the classes enclosing the @EventHandler must be public");
        }
        if(enclosingElement != null) {
          enclosingElement = enclosingElement.getEnclosingElement();
        }
      } while (enclosingElement != null && enclosingElement instanceof TypeElement);
    }
    TypeElement eventType = ((TypeElement) typeUtils.asElement(eventParameterElement.asType()));
    return new EventHandlerRegistration(handler, eventHandlerMethod.getSimpleName().toString(), enclosingClass, eventType);
  }
}
