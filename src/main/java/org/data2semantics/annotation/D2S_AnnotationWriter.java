package org.data2semantics.annotation;

import org.openrdf.model.URI;

public interface D2S_AnnotationWriter {

	public void addAnnotation(D2S_Annotation curAnnotation);

	public String getAnnotationFileName();
	
	public String getAnnotationSourceLocation();
	
	public URI getDocumentURI();
	
	public Boolean hasAnnotations();
}
