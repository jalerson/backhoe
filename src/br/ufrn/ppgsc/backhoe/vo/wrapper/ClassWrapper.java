package br.ufrn.ppgsc.backhoe.vo.wrapper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import br.ufrn.ppgsc.backhoe.util.visitors.ClassVisitor;

public class ClassWrapper {
	private CompilationUnit compilationUnit; // wrapped object
	private ASTParser parser;
	private ClassVisitor classVisitor;

	public ClassWrapper(String input) {
		parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(input.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		compilationUnit = (CompilationUnit) parser.createAST(null);
		classVisitor = new ClassVisitor();
		compilationUnit.accept(classVisitor);
	}

	public List<MethodWrapper> getMethods() {
		ArrayList<MethodWrapper> methods = new ArrayList<MethodWrapper>();
		for (MethodDeclaration methodDeclaration : classVisitor.getMethods()) {
			methods.add(new MethodWrapper(methodDeclaration, compilationUnit));
		}
		return methods;
	}
}
