package com.staticbloc.events;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

/**
 * Created with IntelliJ IDEA.
 * User: eygraber
 * Date: 6/21/2015
 * Time: 4:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventDispatcherFactoryCreator {
  private final Map<String, Set<EventHandlerRegistration>> map;
  private final Elements elementUtils;

  public EventDispatcherFactoryCreator(Map<String, Set<EventHandlerRegistration>> map, Elements elementUtils) {
    this.map = map;
    this.elementUtils = elementUtils;
  }

  public JavaFile createDispatcherFactory() {
    return JavaFile.builder("com.staticbloc.events", createFactoryClass().build()).build();
  }

  private TypeSpec.Builder createFactoryClass() {
    MethodSpec factoryConstructor = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .build();

    MethodSpec.Builder createDispatcherMethod = MethodSpec.methodBuilder("createDispatcher")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override.class)
            .addParameter(Object.class, "o", Modifier.FINAL)
            .returns(EventDispatcher.class);

    for(String enclosingClassFQN : map.keySet()) {
      Set<EventHandlerRegistration> registrations = map.get(enclosingClassFQN);
      createDispatcherMethod = createDispatcherMethod
              .beginControlFlow("if(o.getClass().equals($L))", enclosingClassFQN + ".class")
              .addStatement("return $L", createEventDispatcherImplementation(enclosingClassFQN, registrations))
              .endControlFlow();
    }

    createDispatcherMethod = createDispatcherMethod.addStatement("return null");

    return TypeSpec.classBuilder("EventDispatcherFactoryImpl")
            .addSuperinterface(EventDispatcherFactory.class)
            .addMethod(factoryConstructor)
            .addMethod(createDispatcherMethod.build());

  }

  private TypeSpec createEventDispatcherImplementation(String enclosingClassFQN, Set<EventHandlerRegistration> registrations) {
    TypeName enclosingClassTypeName = TypeName.get(getEnclosingClassTypeMirror(enclosingClassFQN));

    ParameterizedTypeName classOfEvent = ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Event.class));

    TypeSpec.Builder eventDispatcherBuilder = TypeSpec.anonymousClassBuilder("")
            .addSuperinterface(EventDispatcher.class)
            .addField(FieldSpec.builder(enclosingClassTypeName, "subject", Modifier.PRIVATE)
                    .initializer("$L", String.format("(%s) o", enclosingClassFQN))
                    .build())
            .addMethod(createEventDispatchMethod(registrations))
            .addMethod(createGetDispatchableEventsMethod(classOfEvent))
            .addMethod(createGetRunTypeForEventMethod(classOfEvent))
            .addMethod(createEventDispatcherEqualsMethod())
            .addMethod(createEventDispatcherHashCodeMethod());

    eventDispatcherBuilder = createEventsToRunTypeMap(eventDispatcherBuilder, classOfEvent, registrations);

    return eventDispatcherBuilder.build();
  }

  private static TypeSpec.Builder createEventsToRunTypeMap(TypeSpec.Builder builder, ParameterizedTypeName classOfEvent, Set<EventHandlerRegistration> registrations) {
    return builder.addField(FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class), classOfEvent, TypeName.get(RunType.class)),
            "eventsToRunTypeMap", Modifier.PRIVATE, Modifier.FINAL)
            .initializer("createEventsToRunTypeMap()")
            .build())
            .addMethod(createEventsToRunTypeMapMethod(registrations, classOfEvent));
  }

  private static MethodSpec createEventsToRunTypeMapMethod(Set<EventHandlerRegistration> registrations, ParameterizedTypeName classOfEvent) {
    MethodSpec.Builder registrationInfoMethodBuilder = MethodSpec.methodBuilder("createEventsToRunTypeMap")
            .addModifiers(Modifier.PRIVATE)
            .returns(ParameterizedTypeName.get(ClassName.get(Map.class), classOfEvent, TypeName.get(RunType.class)));

    registrationInfoMethodBuilder.addStatement("$T<$T, $T> eventsToRunTypeMap = new $T<$T, $T>()", Map.class, classOfEvent, RunType.class,
            HashMap.class, classOfEvent, RunType.class);
    for(EventHandlerRegistration registration : registrations) {
      registrationInfoMethodBuilder.addStatement("eventsToRunTypeMap.put($L, $L.$L)",
              String.format("%s.class", registration.getEventParameterFQN()), "RunType", registration.getRunType());
    }

    registrationInfoMethodBuilder.addStatement("return eventsToRunTypeMap");

    return registrationInfoMethodBuilder.build();
  }

  private static MethodSpec createEventDispatchMethod(Set<EventHandlerRegistration> registrations) {
    TypeVariableName eventType = TypeVariableName.get("T", Event.class);

    MethodSpec.Builder dispatchMethodBuilder = MethodSpec.methodBuilder("dispatchEvent")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(eventType)
            .addParameter(eventType, "event");

    for(EventHandlerRegistration registration : registrations) {
      dispatchMethodBuilder = addEventDispatchStatement(dispatchMethodBuilder, registration);
    }

    return dispatchMethodBuilder.build();
  }

  private static MethodSpec createGetDispatchableEventsMethod(ParameterizedTypeName classOfEvent) {
    return MethodSpec.methodBuilder("getDispatchableEvents")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override.class)
            .returns(ParameterizedTypeName.get(ClassName.get(Set.class), classOfEvent))
            .addStatement("return eventsToRunTypeMap.keySet()")
            .build();
  }

  private static MethodSpec createGetRunTypeForEventMethod(ParameterizedTypeName classOfEvent) {
    return MethodSpec.methodBuilder("getRunTypeForEvent")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override.class)
            .returns(RunType.class)
            .addParameter(classOfEvent, "event")
            .addStatement("return eventsToRunTypeMap.get(event)")
            .build();
  }

  private static MethodSpec createEventDispatcherEqualsMethod() {
    return MethodSpec.methodBuilder("equals")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(Object.class, "o")
            .returns(TypeName.BOOLEAN)
            .beginControlFlow("if(this == o)")
            .addStatement("return true")
            .endControlFlow()
            .addStatement("return o != null && getClass() == o.getClass()")
            .build();
  }

  private static MethodSpec createEventDispatcherHashCodeMethod() {
    return MethodSpec.methodBuilder("hashCode")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(TypeName.INT)
            .addStatement("return subject.hashCode()")
            .build();
  }

  private static MethodSpec.Builder addEventDispatchStatement(MethodSpec.Builder builder, EventHandlerRegistration registration) {
    return builder
        .beginControlFlow("if(event instanceof $L && !isDispatchBlocked($L.class))", registration.getEventParameterFQN(), registration.getEventParameterFQN())
        .addStatement("subject.$L(($L)event)", registration.getMethodName(), registration.getEventParameterFQN())
        .addStatement("return")
        .endControlFlow();
  }

  private TypeMirror getEnclosingClassTypeMirror(String enclosingClassFQN) {
    return elementUtils.getTypeElement(enclosingClassFQN).asType();
  }
}
