package com.javalobby.tnt.annotation;

@attribute("Call a Java procedure from the interface")
public class MyClass {
 
 public MyClass() {
 
 }	 
 
 @attribute(value = "Set the name of the funtion to be called.",
		 	required = true,
		 	defaultValue = "")	
 public void setFunction() {
 
 }
 @attribute(value = "Set the name of the funtion to be called.  " +
 		"This is another line.",
		 	required = true,
		 	defaultValue = "")	
public void addArg(String x) {

}
}

