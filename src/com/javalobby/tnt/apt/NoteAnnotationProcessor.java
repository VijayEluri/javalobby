package com.javalobby.tnt.apt;

import static com.javalobby.tnt.apt.DOMUtil.printDOM;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.util.SourcePosition;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;

public class NoteAnnotationProcessor implements AnnotationProcessor {
 
	enum type {NONE, CLASS, ATT, NESTED};
	
	static Document document = null;
	static Element description;
	static Element name;
	static Element parameters;
	static Element nested;
	static Element examples;
	static Element extend;
	
	File lastFile = null;
	
	private AnnotationProcessorEnvironment environment;
 
	private AnnotationTypeDeclaration attributeDec;
	
	static final Comparator<Declaration> DECLARATION_ORDER = 
	    new Comparator<Declaration>() {
	public int compare(Declaration e1, Declaration e2) {
		SourcePosition p1 = e1.getPosition();
		SourcePosition p2 = e2.getPosition();
		String file1 = p1.file().getAbsolutePath();
		String file2 = p2.file().getAbsolutePath();
		int line1 = p1.line();
		int line2 = p2.line();
		
		if (file2.equals(file1))
			return line1 - line2;
	return file2.compareTo(file1);
	}
	};
 
	public NoteAnnotationProcessor(AnnotationProcessorEnvironment env) {
		environment = env;
		
		// get the annotation type declaration for our 'Note' annotation.
		// Note, this is also passed in to our annotation factory - this 
		// is just an alternate way to do it.
		attributeDec = (AnnotationTypeDeclaration) environment
		.getTypeDeclaration("com.javalobby.tnt.annotation.attribute");
		System.out.println("qn = " + attributeDec.getQualifiedName());
		System.out.println("position = " + attributeDec.getPosition());
	}
 
	public void buildDocument() {
		DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.newDocument();  
			   Element root = 
	                  (Element) document.createElement("object"); 
	       
	          root.appendChild( name = document.createElement("name"));
	          root.appendChild( description = document.createElement("description"));
	          root.appendChild(parameters=document.createElement("parameters"));
	          root.appendChild(nested=document.createElement("nested"));
	          root.appendChild(examples=document.createElement("examples"));
	          root.appendChild(extend=document.createElement("extend"));
	          document.appendChild(root);
		}

		catch (ParserConfigurationException  e) {
			e.printStackTrace();
			return;
		}
	}
	
	public void process() {
		// Get all declarations that use the note annotation.
		Collection<Declaration> declarations = environment
				.getDeclarationsAnnotatedWith(attributeDec);
		List<Declaration>sortDec = new ArrayList<Declaration>(declarations);

		Collections.sort(sortDec, DECLARATION_ORDER);
		
		for (Declaration declaration : sortDec) {
			processNoteAnnotations(declaration);
		}
		FindExampleFileAndPrint();
		
	}
	
	private void processNoteAnnotations(Declaration declaration) {
		// Get all of the annotation usage for this declaration.
		// the annotation mirror is a reflection of what is in the source.
		Collection<AnnotationMirror> annotations = declaration
				.getAnnotationMirrors();
		
		//System.err.println("annotations = " + annotations);
		// iterate over the mirrors.
		for (AnnotationMirror mirror : annotations) {
			Map<AnnotationTypeElementDeclaration, AnnotationValue> 
				values = mirror.getElementValues();
			SourcePosition position = mirror.getPosition();
			File file = position.file();
			if (lastFile == null || !lastFile.equals(file)) {
				
				if (lastFile != null) {
					FindExampleFileAndPrint();
				}
				else {
					System.err.println("No 'examples' file");
				}
				buildDocument();
				lastFile = file;
			}
			// if the mirror in this iteration is for our note declaration...
			type t = type.NONE;
			if(mirror.getAnnotationType().getDeclaration().equals(
					attributeDec)) {
				
				String dec = declaration.getSimpleName();
				//System.out.println("modifiers = " + declaration.getModifiers());
				
				String name = "";
				
				if (declaration.toString().endsWith(dec)) {
					t = type.CLASS;
					name = dec;
					this.name.appendChild(document.createTextNode(name));
				}
				else {
					if (dec.startsWith("add")) {
						t = type.NESTED;
						//System.out.println("nested");
					}
					else if (dec.startsWith("set")) {
						t = type.ATT;
						//System.out.println("attribute");
					}
					else {
						System.err.println("Function name doesn't begin with 'set' or 'add'.");
					}
					name = dec.substring(3,4).toLowerCase() + dec.substring(4);
				}
				//System.out.println("name = " + name);
				//System.out.println("Declaration: " + declaration.toString());
				//System.out.println("Position: " + position);
				//System.out.println("Values:");
				String key = null;
				String value = null;
				HashMap<String, String> x = new HashMap<String, String>();
				for (Map.Entry<AnnotationTypeElementDeclaration, AnnotationValue> entry : values
						.entrySet()) {
					AnnotationTypeElementDeclaration elemDecl = entry.getKey();
					key = elemDecl.toString();
					value = entry.getValue().toString();
					value = value.replace("\"", "");
					x.put(key, value);
					AnnotationValue avalue = entry.getValue();
					//System.out.println("    " + elemDecl + "=" + avalue);
				}
				String comment = declaration.getDocComment();
				if (comment == null)
					comment = "NO COMMENT";
				switch (t) {
				case CLASS:
					try {
							Class c = null;
							try {
								c = Class.forName(declaration.toString());
							} catch (java.lang.NoClassDefFoundError e) {								
								e.printStackTrace();
								extend.appendChild(document.createTextNode("Error: couldn't load " + declaration.toString() + " because " + e.getMessage() + " not found."));
							}
							if (c != null) {
								extend.appendChild(document.createTextNode(c.getSuperclass().getName()));
							}
						} catch (Exception e) {
							e.printStackTrace();
						} 
					this.description.appendChild(document.createTextNode(comment));
					break;
				case ATT:
					/*
					<required>no; defaults to value</required>
					*/
					Element param = document.createElement("parameter");
					Element att = document.createElement("attribute");
					att.appendChild(document.createTextNode(name));
					param.appendChild(att);
					Element description = document.createElement("description");			
					description.appendChild(document.createTextNode(comment/*x.get("value()")*/));
					param.appendChild(description);
					Element required = document.createElement("required");
					String defaultValue = x.get("defaultValue()");
					String req = x.get("required()");
					if ("true".equals(req))
						req = "yes";
					else
						req = "no";
							
					if (defaultValue != null && defaultValue.trim().length() > 1) {
						req = req + "; " + defaultValue;
					}
					required.appendChild(document.createTextNode(req));
					param.appendChild(required);
					parameters.appendChild(param);
					break;
				case NESTED:
					Element paramN = document.createElement("parameter");
					Element nameN = document.createElement("name");
					nameN.appendChild(document.createTextNode(name));
					paramN.appendChild(nameN);
					Element descriptionN = document.createElement("description");			
					descriptionN.appendChild(document.createTextNode(comment/*x.get("value()")*/));
					paramN.appendChild(descriptionN);
					nested.appendChild(paramN);
					break;
				}
			}
		}
	}
    
    private Document getXML(String src) 
    		throws IOException, SAXException, FileNotFoundException {
        DOMParser parser = new DOMParser();
        InputStream in = new FileInputStream(src);
        InputSource source = new InputSource(in);
        parser.parse(source);
        in.close();
        
        return parser.getDocument();
    }
 
    private void printXML(Document doc) 
    		throws TransformerException, TransformerConfigurationException {
        DOMSource domSource = new DOMSource(doc);
        StreamResult streamResult = new StreamResult(System.out);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer serializer = tf.newTransformer();
        serializer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
        //serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"users.dtd");
        serializer.setOutputProperty(OutputKeys.INDENT,"yes");
        serializer.transform(domSource, streamResult); 
    }
    
    void FindExampleFileAndPrint() {
    	String name = lastFile.getAbsolutePath().replace("src","src/examples");
    	name = name.replace(".java",".xml");
        ProcessingInstruction pi = document.createProcessingInstruction("insert", name);
        examples.appendChild(pi);

        String outFile = lastFile.getAbsolutePath().replace("src", "build/gensrc");
        outFile = outFile.replace(".java", ".xml");
        new File(outFile).getParentFile().mkdirs();
        try {
        	System.setOut(new PrintStream(outFile));
        	//System.err.println("output goes to " + outFile);
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
        printDOM(document);
    }
}




