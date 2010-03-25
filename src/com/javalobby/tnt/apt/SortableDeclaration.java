package com.javalobby.tnt.apt;

import com.sun.mirror.declaration.Declaration;

public class SortableDeclaration implements Comparable {
	Declaration myDec = null;
	
	public int compareTo(Object x) {
		if (x instanceof Declaration) {
			Declaration m = (Declaration)x;
			return m.getPosition().toString().compareTo(myDec.getPosition().toString());
		}
		return -1;
	}
	public void setDeclaration(Declaration m) {
		myDec = m;
	}
}
