package com.staticbloc.events;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes("com.staticbloc.events.EventHandler")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class EventHandlerProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for(TypeElement t : annotations) {
            for(Element e : roundEnv.getElementsAnnotatedWith(t)) {
                if(e.getKind() != ElementKind.METHOD) {
                    printError(e);
                }

                Set<Modifier> modifiers = e.getModifiers();
                if(!modifiers.contains(Modifier.PUBLIC)) {
                    printError(e);
                }
            }
        }
        return true;
    }

    private void printError(Element e) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                "@EventHandler should only be applied to public methods", e);
    }
}
