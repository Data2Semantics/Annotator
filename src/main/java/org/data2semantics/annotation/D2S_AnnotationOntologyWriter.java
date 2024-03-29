package org.data2semantics.annotation;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.turtle.TurtleWriter;




/**
 * This class will be responsible for generating annotation ontology.
 * I am not sure if this is better than the print string version, 
 * maybe I just don't know how to do this properly.
 * 
 * @author wibisono
 *

 */
public class D2S_AnnotationOntologyWriter implements D2S_AnnotationWriter {
	
	
	private Log log = LogFactory.getLog(D2S_AnnotationOntologyWriter.class);

	
	//Namespaces
	 
	public static final String AO = "http://purl.org/ao/";
	
	public static final String AO_CORE = AO + "core#";

	public static final String AO_FOAF = AO + "foaf#";

	public static final String AO_TYPE = AO + "types#";
	
	public static final String AO_SELECTOR = AO + "selectors#";
	
	
	
	public static final String ANNOTEA = "http://www.w3.org/2000/10/annotation-ns#";
	
	public static final String FOAF = "http://xmlns.com/foaf/0.1#";

	public static final String PAV = "http://purl.org/pav/";

	public static final String OBO = "http://purl.obolibrary.org/obo#";
	
	public static final String XSD = "http://www.w3.org/TR/xmlschema-2/#";
	
	public static final String D2S_EXAMPLE = "http://www.data2semantics.org/example/";

	public static final String D2S_SOURCEDOC = D2S_EXAMPLE+"sourcedocs/";
	
	public static final String D2S_PREFIX_SELECTOR = D2S_EXAMPLE+"prefixpostfixtextselector/";
	
	public static final String D2S_IMAGE_SELECTOR = D2S_EXAMPLE+"imageselector/";
	
	public static final String D2S_QUALIFIER = D2S_EXAMPLE+"qualifier/";
	
	public static final String D2S_DOCS = D2S_EXAMPLE+"docs/";
	
	public static final String D2S_ANNOTATOR = D2S_EXAMPLE+"D2SAnnotator/";
	
	// Types & Predicates
	
	public static final URIImpl AO_ANNOTATION 			= new URIImpl(AO_CORE+ "Annotation");
	public static final URIImpl AO_ONSOURCEDOCUMENT 	= new URIImpl(AO_CORE+"onSourceDocument");
	public static final URIImpl AO_HASTOPIC			 	= new URIImpl(AO_CORE+"hasTopic");
	public static final URIImpl AO_CORE_SELECTOR 		= new URIImpl(AO_CORE+"Selector");
	public static final URIImpl AO_CONTEXT		 		= new URIImpl(AO_CORE+"context");
	
	public static final URIImpl AOS_TEXT_SELECTOR 		= new URIImpl(AO_SELECTOR+"TextSelector");
	public static final URIImpl AOS_IMAGE_SELECTOR 		= new URIImpl(AO_SELECTOR+"ImageSelector");
	public static final URIImpl AOS_INITEND_SELECTOR 	= new URIImpl(AO_SELECTOR+"InitEndCornerSelector");
	public static final URIImpl AOS_PP_SELECTOR			= new URIImpl(AO_SELECTOR+"PrefixPostFixSelector");
	
	public static final URIImpl AOS_EXACT				= new URIImpl(AO_SELECTOR+"exact");
	public static final URIImpl AOS_PREFIX				= new URIImpl(AO_SELECTOR+"prefix");
	public static final URIImpl AOS_POSTFIX				= new URIImpl(AO_SELECTOR+"postfix");
	public static final URIImpl AOS_INIT				= new URIImpl(AO_SELECTOR+"init");
	public static final URIImpl AOS_END					= new URIImpl(AO_SELECTOR+"end");
	
	public static final URIImpl AOF_ONDOCUMENT			= new URIImpl(AO_FOAF+"onDocument");
	public static final URIImpl AOF_ANNOTATES_DOCUMENT	= new URIImpl(AO_FOAF+"annotatesDocument");
	
	
	public static final URIImpl AOT_QUALIFIER			= new URIImpl(AO_TYPE+ "Qualifier");
	public static final URIImpl ANN_ANNOTATION 			= new URIImpl(ANNOTEA+ "Annotation");
	
	public static final URIImpl PAV_CREATEDON 			= new URIImpl(PAV+ "createdOn");
	public static final URIImpl PAV_CREATEDBY 			= new URIImpl(PAV+ "createdBy");
	public static final URIImpl PAV_RETRIEVED_FROM		= new URIImpl(PAV+ "retrievedFrom");
	public static final URIImpl PAV_SRCDOC				= new URIImpl(PAV+ "SourceDocument");
	public static final URIImpl PAV_SRCACCESSED_FROM	= new URIImpl(PAV+ "sourceAccessedOn");
	
	public static final URIImpl FOAF_DOCUMENT			= new URIImpl(FOAF+ "Document");



	
	private void setupNameSpace() {
		
		try {
			docWriter.handleNamespace("ao",   D2S_AnnotationOntologyWriter.AO_CORE);
			docWriter.handleNamespace("aof",  D2S_AnnotationOntologyWriter.AO_FOAF);
			docWriter.handleNamespace("aot",  D2S_AnnotationOntologyWriter.AO_TYPE);
			docWriter.handleNamespace("aos",  D2S_AnnotationOntologyWriter.AO_SELECTOR);
			
			docWriter.handleNamespace("pav",  D2S_AnnotationOntologyWriter.PAV);
			docWriter.handleNamespace("ann",  D2S_AnnotationOntologyWriter.ANNOTEA);
			docWriter.handleNamespace("pro",  D2S_AnnotationOntologyWriter.OBO);
			docWriter.handleNamespace("foaf", D2S_AnnotationOntologyWriter.FOAF);
			
				
		} catch (RDFHandlerException e) {
			log.error("Failed to setup namespaces for Annotation Ontology");
		}
	}

	TurtleWriter docWriter;
	FileOutputStream outputStream = null;
	
	public D2S_AnnotationOntologyWriter(String outputFile) {
		try {
			outputStream = new FileOutputStream(new File(outputFile));
		} catch (FileNotFoundException e) {
			log.error("Failed to create output file");
		}
		
		docWriter = new TurtleWriter(outputStream);

		
		setupNameSpace();
		

	}

	public void startWriting() {
		try {
			docWriter.startRDF();
		} catch (RDFHandlerException e) {
			log.error("Failed to start writing document");
		}
	}

	public void stopWriting(){
		try {
			docWriter.endRDF();
			outputStream.close();
		} catch (IOException e) {

			log.error("Failed to stop writing document");
		} catch (RDFHandlerException e) {
			log.error("Failed to write document");
		}
	}
	
	
   public static Literal dateToLiteral(Date date) {

           LiteralImpl timeLiteral = new LiteralImpl(date.toString(), new URIImpl(XSD+ "dateTime"));
           return timeLiteral;
   }
	
	/**
	 * Add file header statements
	 * @param files
	 */
	public void addFiles(List<File> files) {

		for (File curFile : files) {
			String fileName = curFile.getName().replaceAll(" ", "_");
			URIImpl fileSourceDocURI = new URIImpl(D2S_SOURCEDOC + fileName);
			
			addTriple(fileSourceDocURI, RDF.TYPE, PAV_SRCDOC);

			addTriple(fileSourceDocURI, PAV_RETRIEVED_FROM, 
					new URIImpl(D2S_DOCS + fileName));

			addTriple(fileSourceDocURI, PAV_SRCACCESSED_FROM, 
					dateToLiteral(new Date()));
			
			addTriple(new URIImpl(D2S_DOCS+ fileName), RDF.TYPE, FOAF_DOCUMENT);

		}

	}
	
	/**
	 * Add files and original URLS
	 * @param files
	 */
	public void addFileAndURLs(List<File> files, HashMap<String, String> originalURL) {

		for (File curFile : files) {
			String fileName = curFile.getName().replaceAll(" ", "_");
			URIImpl fileSourceDocURI = new URIImpl(D2S_SOURCEDOC + fileName);
			
			addTriple(fileSourceDocURI, RDF.TYPE, PAV_SRCDOC);

			addTriple(fileSourceDocURI, PAV_RETRIEVED_FROM, 
					new URIImpl(originalURL.get(curFile.getName())));

			addTriple(fileSourceDocURI, PAV_SRCACCESSED_FROM, 
					dateToLiteral(new Date()));
			
			addTriple(new URIImpl(D2S_DOCS+  fileName), RDF.TYPE, FOAF_DOCUMENT);

		}

	}
	/**
	 * This function will add annotation ontology found in PDF.
	 * @param mainTerm
	 * @param prefix
	 * @param postfix
	 * @param onDocument
	 * @param annotation
	 * @param position (image coordinates/positions)
	 */
	public void addPDFAnnotation(String mainTerm, String prefix, String postfix,
			String onDocument, String annotation, String position,
			String page_nr, String chunk_nr, String termLocation) {
		String sourceDocument = onDocument;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		 
		String CREATED_ON = sdf.format(new Date());
		
		String selectorID = onDocument + "_" + page_nr + "_" + chunk_nr + "_"
				+ termLocation;

		writePrefixPostfixTextSelector(mainTerm, prefix, postfix, D2S_DOCS+onDocument, sourceDocument,
				selectorID);

		writeImageSelector(onDocument, position, selectorID);

		URIImpl qualifier = new URIImpl(D2S_QUALIFIER+ selectorID);
		
		addTriple(qualifier, RDF.TYPE, AOT_QUALIFIER);
		
		addTriple(qualifier, RDF.TYPE, AO_ANNOTATION);
		
		addTriple(qualifier, RDF.TYPE, ANN_ANNOTATION);
		
		addTriple(qualifier, AOF_ANNOTATES_DOCUMENT, new URIImpl(D2S_DOCS+ onDocument));
		
		addTriple(qualifier, AO_HASTOPIC, new URIImpl(annotation));
		
		addTriple(qualifier, PAV_CREATEDON, new LiteralImpl(CREATED_ON));
		
		addTriple(qualifier, PAV_CREATEDBY, new URIImpl(D2S_ANNOTATOR));
		
		addTriple(qualifier, AO_CONTEXT, new URIImpl(D2S_PREFIX_SELECTOR+ selectorID));
		
		addTriple(qualifier, AO_CONTEXT, new URIImpl(D2S_IMAGE_SELECTOR+ selectorID));
		
	}

	/**
	 * XML document, we don't have page number.
	 * @param curAnnotation
	 */
	public void addAnnotation(D2S_Annotation curAnnotation){
		String mainTerm="", prefix="", postfix="", onDocument="", sourceDocument = "", annotation="";
		mainTerm = curAnnotation.getPreferredName();
		prefix = curAnnotation.getPrefix();
		postfix = curAnnotation.getSuffix();
		onDocument = curAnnotation.getOnDocument();
		sourceDocument = curAnnotation.getSourceDocument();
		annotation = curAnnotation.getTermFound();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		 
		String CREATED_ON = sdf.format(new Date());
		
		
		String selectorID = sourceDocument + "_" + curAnnotation.getFrom() + "_"+curAnnotation.getTo();

		writePrefixPostfixTextSelector(mainTerm, prefix, postfix, onDocument, sourceDocument, selectorID);

		URIImpl qualifier = new URIImpl(D2S_QUALIFIER+ selectorID);
		
		addTriple(qualifier, RDF.TYPE, AOT_QUALIFIER);
		
		addTriple(qualifier, RDF.TYPE, AO_ANNOTATION);
		
		addTriple(qualifier, RDF.TYPE, ANN_ANNOTATION);
		

		addTriple(qualifier, AOF_ANNOTATES_DOCUMENT, new URIImpl(onDocument));
		
		addTriple(qualifier, AO_HASTOPIC, new URIImpl(annotation));
		
		addTriple(qualifier, PAV_CREATEDON, new LiteralImpl(CREATED_ON));
		
		addTriple(qualifier, PAV_CREATEDBY, new URIImpl(D2S_ANNOTATOR));
		
		addTriple(qualifier, AO_CONTEXT, new URIImpl(D2S_PREFIX_SELECTOR+ selectorID));
		
	}


	
	private void writeImageSelector(String fileName, String position,
			String selectorID) {
		URIImpl imageSelector = new URIImpl(D2S_IMAGE_SELECTOR+ selectorID);

		addTriple(imageSelector, RDF.TYPE, AO_CORE_SELECTOR);

		addTriple(imageSelector, RDF.TYPE, AOS_IMAGE_SELECTOR);

		addTriple(imageSelector, RDF.TYPE, AOS_INITEND_SELECTOR);

		addTriple(imageSelector, AOS_INIT, new LiteralImpl(
				position.split("-")[0]));

		addTriple(imageSelector, AOS_END, new LiteralImpl(
				position.split("-")[1]));

		addTriple(imageSelector, AOF_ONDOCUMENT,
				new URIImpl(D2S_DOCS+ fileName));

		addTriple(imageSelector, AO_ONSOURCEDOCUMENT, new URIImpl(D2S_SOURCEDOC+ fileName));
	}

	/**
	 * Write necessary triples for prefix postfix selectors.
	 * @param mainTerm
	 * @param prefix
	 * @param postfix
	 * @param onDocument
	 * @param selectorID
	 */
	private void writePrefixPostfixTextSelector(String mainTerm, String prefix,
			String postfix, String onDocument, String sourceDocument, String selectorID)
			{
		
		URIImpl prefixPostfixSelector = new URIImpl(D2S_PREFIX_SELECTOR+selectorID);
		
		// prefixSelector a ao:Selector
		addTriple( prefixPostfixSelector, RDF.TYPE, AO_CORE_SELECTOR);

		// rdf:type aos:TextSelector
		addTriple(prefixPostfixSelector, RDF.TYPE,  AOS_TEXT_SELECTOR);
		
		// rdf:type aos:PrefixPostfixSelector
		addTriple(prefixPostfixSelector, RDF.TYPE,  AOS_PP_SELECTOR);
		
		//aos:exact mainTerm
		addTriple(prefixPostfixSelector, AOS_EXACT,	new LiteralImpl(mainTerm));
		
		//aos:prefix prefix
		addTriple(prefixPostfixSelector, AOS_PREFIX, new LiteralImpl(prefix));
		
		//aos:postfix postfix
		addTriple(prefixPostfixSelector, AOS_POSTFIX, new LiteralImpl(postfix));
		
		//aof:onDocument 
		addTriple(prefixPostfixSelector, AOF_ONDOCUMENT,  new URIImpl(onDocument));

		//ao:onSourceDocument 
		addTriple(prefixPostfixSelector, AO_ONSOURCEDOCUMENT, new URIImpl(D2S_SOURCEDOC+ sourceDocument));
	}
	
	public void addTriple(Resource subj, URI pred, Value obj ){
		  try {
			Statement s = new StatementImpl(subj,pred,obj);
			docWriter.handleStatement(s);
		} catch (RDFHandlerException e) {
			log.error("Failed to add statement: "+subj+" "+pred+" "+obj);
		}
	}
	
	public OutputStream getOS(){
		return outputStream;
	}

	public String getAnnotationFileName() {
		// TODO Auto-generated method stub
		return null;
	}

	public URI getDocumentURI() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAnnotationSourceLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	public Boolean hasAnnotations() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
