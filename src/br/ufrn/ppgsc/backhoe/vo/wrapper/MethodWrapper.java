package br.ufrn.ppgsc.backhoe.vo.wrapper;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import br.ufrn.ppgsc.backhoe.util.cyclomaticcomplexity.CyclomaticComplexityCalculatorStrategy;
import br.ufrn.ppgsc.backhoe.util.cyclomaticcomplexity.McCabeCalculatorStrategy;

public class MethodWrapper {
	private MethodDeclaration methodDeclaration; // wrapped object
	private CyclomaticComplexityCalculatorStrategy calculatorStrategy;
	private CompilationUnit compilationUnit;
	
	public MethodWrapper(MethodDeclaration methodDeclaration, CompilationUnit compilationUnit) {
		this.methodDeclaration = methodDeclaration;
		this.compilationUnit = compilationUnit;
	}
	
	public int getCyclomaticComplexity() {
		if(methodDeclaration.getBody() == null) { // is an interface method?
			return 0;
		} else {
			this.calculatorStrategy = new McCabeCalculatorStrategy(methodDeclaration.getBody().toString());
			return calculatorStrategy.getCyclomaticComplexity();
		}
	}
	
	public String getName() {
		return methodDeclaration.getName().getFullyQualifiedName();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof MethodWrapper) {
			MethodWrapper method = (MethodWrapper) obj;
			if(this.getName().equals(method.getName())) {
				return true;
			}
		}
		return false;
	}
	
	public Integer getBeginLine() {
		return compilationUnit.getLineNumber(methodDeclaration.getStartPosition());
	}
	
	public Integer getEndLine() {
		return compilationUnit.getLineNumber(methodDeclaration.getStartPosition() + methodDeclaration.getLength());
	}

	@Override
	public String toString() {
		return methodDeclaration.toString();
	}	
}
