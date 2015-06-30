package com.staticbloc.events;

import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("com.staticbloc.events.EventHandler")
public class EventHandlerProcessor extends AbstractProcessor {
  private EventHandlerRegistrationParser eventHandlerRegistrationParser;
  private EventComparator eventComparator;
  private Map<String, Set<EventHandlerRegistration>> map;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    eventHandlerRegistrationParser = new EventHandlerRegistrationParser(processingEnv);
    eventComparator = new EventComparator(processingEnv.getTypeUtils());
    map  = new HashMap<>();
  }

  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    boolean generateEventDispatcherFactoryImplementation = false;

    for(TypeElement t : annotations) {
      for(Element element : roundEnv.getElementsAnnotatedWith(t)) {
        EventHandlerRegistration eventHandlerRegistration;
        try {
          eventHandlerRegistration = eventHandlerRegistrationParser.parse(element);
        } catch (EventHandlerRegistrationParser.EventHandlerParseException e) {
          printError(element, e.getMessage());
          continue;
        }

        Set<EventHandlerRegistration> eventHandlerRegistrations = map.get(eventHandlerRegistration.getEnclosingClassFQN());
        if(eventHandlerRegistrations == null) {
          eventHandlerRegistrations = new TreeSet<>(eventComparator);
          map.put(eventHandlerRegistration.getEnclosingClassFQN(), eventHandlerRegistrations);
        }

        if(eventHandlerRegistrations.contains(eventHandlerRegistration)) {
          printError(element, String.format("There can only be one @EventHandler method per event type per class (found duplicate entry for %s)", eventHandlerRegistration.toString()));
          continue;
        }

        eventHandlerRegistrations.add(eventHandlerRegistration);
        if(!generateEventDispatcherFactoryImplementation) {
          generateEventDispatcherFactoryImplementation = true;
        }
      }
    }

    if(generateEventDispatcherFactoryImplementation) {
      EventDispatcherFactoryCreator creator = new EventDispatcherFactoryCreator(map, processingEnv.getElementUtils());
      JavaFile file = creator.createDispatcherFactory();
      try {
        file.writeTo(processingEnv.getFiler());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return true;
  }

  private void printError(Element element, String message) {
    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
  }
}