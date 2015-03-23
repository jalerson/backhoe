package br.ufrn.ppgsc.backhoe.util.cyclomaticcomplexity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.WhileStatement;

public class McCabeVisitor extends ASTVisitor {
	private Integer cyclomaticComplexity;
	
	public McCabeVisitor() {
		this.cyclomaticComplexity = 1;
	}
	
	@Override
	public boolean visit(ForStatement stm) {
		cyclomaticComplexity++;
		return true;
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		cyclomaticComplexity++;
		return true;
	}

	@Override
	public boolean visit(IfStatement stm) {
		cyclomaticComplexity++;
		return true;
	}
	
	@Override
	public boolean visit(WhileStatement stm) {
		cyclomaticComplexity++;
		Expression exp = stm.getExpression();
		if(exp != null) {
			cyclomaticComplexity += countOccurrences("\\&\\&", exp.toString());
			cyclomaticComplexity += countOccurrences("\\|\\|", exp.toString());
		}
		return true;
	}

	@Override
	public boolean visit(SwitchCase stm) {
		cyclomaticComplexity++;
		return true;
	}
	
	@Override
	public boolean visit(CatchClause catchClause) {
		cyclomaticComplexity++;
		return true;
	}

	@Override
	public boolean visit(ExpressionStatement stm) {
		Expression exp = stm.getExpression();
		cyclomaticComplexity += countOccurrences("\\?", exp.toString());
		cyclomaticComplexity += countOccurrences("\\&\\&", exp.toString());
		cyclomaticComplexity += countOccurrences("\\|\\|", exp.toString());
		return true;
	}

	@Override
	public boolean visit(ConditionalExpression exp) {
		Expression condition = exp.getExpression();
		cyclomaticComplexity += countOccurrences("\\&\\&", condition.toString());
		cyclomaticComplexity += countOccurrences("\\|\\|", condition.toString());
		return true;
	}

	private int countOccurrences(String findStr, String str) {
		Pattern p = Pattern.compile(findStr);
		Matcher m = p.matcher(str);
		int count = 0;
		while (m.find()){
			count +=1;
		}
		return count;
	} 
	
	public int getCyclomaticComplexity() {
		return cyclomaticComplexity;
	}
}