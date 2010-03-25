package com.javalobby.tnt.apt;

import java.util.*;
import com.sun.mirror.apt.*;
import com.sun.mirror.declaration.*;
 
public class NoteAnnotationProcessorFactory implements
		AnnotationProcessorFactory {
 
	/**
	 * Returns a note annotation processor.
	 * 
	 * @return An annotation processor for note annotations if requested, 
	 * otherwise, returns the NO_OP annotation processor.
	 */
	public AnnotationProcessor getProcessorFor(
			Set<AnnotationTypeDeclaration> declarations,
			AnnotationProcessorEnvironment env) {
		AnnotationProcessor result;
		if(declarations.isEmpty()) {
			result = AnnotationProcessors.NO_OP;
		}
		else {
			// Next Step - implement this class:
			result = new NoteAnnotationProcessor(env);
		}
		return result;
 
	}
 
	/**
	 * This factory only builds processors for the 
	 * {@link com.javalobby.tnt.annotation.Note} annotation.
	 * @return a collection containing only the note annotation name.
	 */
	public Collection<String> supportedAnnotationTypes() {
		return Collections.singletonList("com.javalobby.tnt.annotation.attribute");
	}
 
	/**
	 * No options are supported by this annotation processor.
	 * @return an empty list.
	 */
	public Collection<String> supportedOptions() {
		return Collections.emptyList();
	}
}
