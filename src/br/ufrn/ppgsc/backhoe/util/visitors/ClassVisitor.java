package br.ufrn.ppgsc.backhoe.util.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class ClassVisitor extends ASTVisitor {
	private List<FieldDeclaration> fields;
	private List<MethodDeclaration> methods;
	
	public ClassVisitor() {
		fields = new ArrayList<FieldDeclaration>();
		methods = new ArrayList<MethodDeclaration>();
	}

	@Override
	public void endVisit(FieldDeclaration node) {
		fields.add(node);
	}

	@Override
	public void endVisit(MethodDeclaration node) {
		methods.add(node);
	}

	public List<FieldDeclaration> getFields() {
		return fields;
	}

	public void setFields(List<FieldDeclaration> fields) {
		this.fields = fields;
	}

	public List<MethodDeclaration> getMethods() {
		return methods;
	}

	public void setMethods(List<MethodDeclaration> methods) {
		this.methods = methods;
	}
}
