/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bugfind.utils.pmdadapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTArguments;
import net.sourceforge.pmd.lang.java.ast.ASTBooleanLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTPrimarySuffix;
import net.sourceforge.pmd.lang.java.ast.AbstractJavaNode;
import net.sourceforge.pmd.lang.java.symboltable.JavaNameOccurrence;
import net.sourceforge.pmd.lang.java.symboltable.MethodNameDeclaration;
import net.sourceforge.pmd.lang.java.symboltable.VariableNameDeclaration;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;

/**
 *
 * @author Mikosh
 */
public class VariableInfo {
    VariableNameDeclaration variableNameDeclaration;
    List<NameOccurrence> lstNameOccurrence;

    public VariableInfo(VariableNameDeclaration variableNameDeclaration, List<NameOccurrence> lstNameOccurrence) {
        this.variableNameDeclaration = variableNameDeclaration;
        this.lstNameOccurrence = lstNameOccurrence;
    }
    
    public VariableInfo(VariableNameDeclaration variableNameDeclaration) {
        this.variableNameDeclaration = variableNameDeclaration;
        this.lstNameOccurrence = this.variableNameDeclaration.getDeclaratorId().getUsages();
    }

    public VariableNameDeclaration getVariableNameDeclaration() {
        return variableNameDeclaration;
    }
    
    public String getVariableName() {
        return variableNameDeclaration.getImage();
    }
    
    public String getVariableType() {
        return variableNameDeclaration.getTypeImage();
    }
    
    public String getVariableTypeWithFullQualification() {
        throw new UnsupportedOperationException("not supported yet");
    }
    
    public int getNumberOfUsages() {
        return lstNameOccurrence.size();
    }
    
    public boolean isMethodOrConstructorInvocation(int nameOccurrenceIndex) {
        JavaNameOccurrence jno = (JavaNameOccurrence) lstNameOccurrence.get(nameOccurrenceIndex);
        JavaNameOccurrence qualifier = (JavaNameOccurrence) jno.getNameForWhichThisIsAQualifier();
        
        if (qualifier == null) {
            return false;
        }
        else {
            boolean succ1 = qualifier.isMethodOrConstructorInvocation();            
            if (!succ1) return false;
            
            ASTPrimaryExpression pryExpr = getPrimaryExprParent(qualifier);
            if (pryExpr != null && pryExpr.jjtGetNumChildren() > 1 && (pryExpr.jjtGetChild(1) instanceof ASTPrimarySuffix)
                    && qualifier.getLocation().getImage().equalsIgnoreCase(jno.getImage() + "." + qualifier.getImage())) {
                return true;
            }
        }
        
        return false;
    }
    
    public MethodCallInfo getMethodCallInfoAtOccurrence(int nameOccurrenceIndex) {
        if (!isMethodOrConstructorInvocation(nameOccurrenceIndex)) return null;
        
        
        JavaNameOccurrence jno = (JavaNameOccurrence) lstNameOccurrence.get(nameOccurrenceIndex);
        JavaNameOccurrence qualifier = (JavaNameOccurrence) jno.getNameForWhichThisIsAQualifier();
        
        String methodCaller = jno.getImage(), methodName = qualifier.getImage();
        
        ASTPrimaryExpression pryExpr = getPrimaryExprParent(qualifier);
        if (pryExpr != null && pryExpr.jjtGetNumChildren() > 1 && 
                (pryExpr.jjtGetChild(1) instanceof ASTPrimarySuffix)) {
            ASTPrimarySuffix sufx = (ASTPrimarySuffix) pryExpr.jjtGetChild(1);
            ((ASTArguments)sufx.jjtGetChild(0)).getScope().getDeclarations();//jjtGetChild(0).jjtGetChild(0)//getArgumentCount();
            //ASTArguments
           
           return new MethodCallInfo(methodCaller, methodName, getMethodArgumentsInfoAtOccurrence(nameOccurrenceIndex));
        }
        
        return null;
    }
    
    public List<MethodArgument> getMethodArgumentsInfoAtOccurrence(int nameOccurrenceIndex) {
        if (!isMethodOrConstructorInvocation(nameOccurrenceIndex)) return null;
        
        JavaNameOccurrence jno = (JavaNameOccurrence) lstNameOccurrence.get(nameOccurrenceIndex);
        JavaNameOccurrence qualifier = (JavaNameOccurrence) jno.getNameForWhichThisIsAQualifier();
        
        String methodCaller = jno.getImage(), methodName = qualifier.getImage();
        
        ASTPrimaryExpression pryExpr = getPrimaryExprParent(qualifier);        
        
        List<MethodArgument> lst = new ArrayList<>();
        
        if (pryExpr != null && pryExpr.jjtGetNumChildren() > 1 && 
                (pryExpr.jjtGetChild(1) instanceof ASTPrimarySuffix)) {
            ASTPrimarySuffix sufx = (ASTPrimarySuffix) pryExpr.jjtGetChild(1);
            int argCount = sufx.getArgumentCount();
            //((ASTLiteral)sufx.jjtGetChild(0).jjtGetChild(0).jjtGetChild(1).jjtGetChild(0).jjtGetChild(0).jjtGetChild(0)).getType();
            for (int i=0; i<argCount; ++i) {//sufx.jjtGetChild(0).getFirstDescendantOfType(ASTPrimaryPrefix.class).jjtGetChild(0).
                ASTPrimaryPrefix prfx = sufx.jjtGetChild(0).jjtGetChild(0).jjtGetChild(i).getFirstDescendantOfType(ASTPrimaryPrefix.class);
                if (prfx == null) continue;
                String methname= null, methType = null;
                ASTBooleanLiteral bl = getBooleanLiteral(prfx);
                ASTLiteral lit = getLiteral(prfx);
                ASTName nm = getName(prfx);
                if (bl != null) {
                    methname = (bl.isTrue()) ? "true" : "false";//bl.getImage();
                    methType = bl.getType().getName();
                    
                }
                else if (lit != null) {
                    methname = lit.getImage();
                    methType = (lit.getType() == null) ? null : lit.getType().getSimpleName();
                }
                else if (nm != null) {
                    methname = nm.getImage();
                    if (!(nm.getNameDeclaration() instanceof VariableNameDeclaration)) {
                       methType = null;
                    }
                    else {
                        methType = (nm.getType() == null) ? ((VariableNameDeclaration)nm.getNameDeclaration()).getTypeImage() : nm.getType().getName();
                    }
                }
//                if (methType != null && methType.equals("java.lang.String")) {
//                    int t=5;
//                    int g=7;
//                }
                MethodArgument ma = new MethodArgument(methname, methType);
                Map m = prfx.getScope().getDeclarations();
                lst.add(ma);
            }
            
            //((ASTArguments)sufx.jjtGetChild(0)).getScope().getDeclarations();//jjtGetChild(0).jjtGetChild(0)//getArgumentCount();
            //ASTArguments
        }
        
        return lst;
    }
    
    private ASTBooleanLiteral getBooleanLiteral(ASTPrimaryPrefix pfx) {
        return pfx.getFirstDescendantOfType(ASTBooleanLiteral.class);
    }
    
    private ASTLiteral getLiteral(ASTPrimaryPrefix pfx) {
        return pfx.getFirstDescendantOfType(ASTLiteral.class);
    }
    
    private ASTName getName(ASTPrimaryPrefix pfx) {
        return pfx.getFirstDescendantOfType(ASTName.class);
    }
    
    private boolean isCompoundArgument(ASTArguments argument) {
        return (argument.getFirstDescendantOfType(ASTArguments.class) != null);
    }
   
    private ASTPrimaryExpression getPrimaryExprParent(JavaNameOccurrence jno) {
        Node currNode = jno.getLocation();
        
        return currNode.getFirstParentOfType(ASTPrimaryExpression.class);
    }
    
    public NameOccurrence getNameOccurrence(int index) {
        if (this.lstNameOccurrence == null) {
            return null;
        }
        else if (index >= this.lstNameOccurrence.size()) {
            return null;
        }
        else {
            return this.lstNameOccurrence.get(index);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VariableInfo && obj != null){
            VariableInfo vInfo = (VariableInfo) obj;
            if (this.variableNameDeclaration == vInfo.variableNameDeclaration 
                    && this.lstNameOccurrence == vInfo.lstNameOccurrence) {
                return true;
            }
            else {
                return false;
            }
        }
        return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int hashCode() {
        return (this.variableNameDeclaration.hashCode() * this.lstNameOccurrence.hashCode() 
                * this.lstNameOccurrence.hashCode())/(this.variableNameDeclaration.hashCode() + this.lstNameOccurrence.hashCode());
    }
    
    
    
}
