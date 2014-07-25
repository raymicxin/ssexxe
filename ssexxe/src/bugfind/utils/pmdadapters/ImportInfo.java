/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bugfind.utils.pmdadapters;

import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import static net.sourceforge.pmd.lang.java.rule.AbstractJavaRule.isQualifiedName;
import net.sourceforge.pmd.lang.java.rule.ImportWrapper;

/**
 *
 * @author Mikosh
 */
public class ImportInfo {
    private ASTImportDeclaration node;

    public ImportInfo(ASTImportDeclaration node) {
        this.node = node;
        
//        if (!node.isImportOnDemand()) { node.getImportedNameNode().getImage()
//            ASTName importedType = (ASTName) node.jjtGetChild(0);
//            String className;
//            if (isQualifiedName(importedType)) {
//                int lastDot = importedType.getImage().lastIndexOf('.') + 1;
//                className = importedType.getImage().substring(lastDot);
//            } else {
//                className = importedType.getImage();
//            }
//            imports.add(new ImportWrapper(importedType.getImage(), className, node));
//        }
    }
    
    public String getFullQualifiedName() {
        if (node.isImportOnDemand()) {
            return node.getImportedName()+".*";
        }
        else {
            return node.getImportedName();
        }
    }
    
    public String getShortName() {
        if (node.isImportOnDemand()) {
            ASTName importedType = node.getImportedNameNode();
            String className;
            if (isQualifiedName(importedType)) {
                int lastDot = importedType.getImage().lastIndexOf('.') + 1;
                className = importedType.getImage().substring(lastDot);
            } else {
                className = importedType.getImage();
            }

            return className;
        }
        else {
            return "*";
        }
    }
    
    String getPackageName() {
        return node.getPackageName();
    }

    public ASTImportDeclaration getNode() {
        return node;
    }
    
    
}
