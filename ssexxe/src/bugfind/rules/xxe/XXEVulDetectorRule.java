/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bugfind.rules.xxe;

import bugfind.report.CustomReport;
import bugfind.utils.pmdadapters.ImportInfo;
import bugfind.utils.pmdadapters.MethodArgument;
import bugfind.utils.pmdadapters.MethodCallInfo;
import bugfind.utils.pmdadapters.PossibleSolution;
import bugfind.utils.pmdadapters.PossibleVulnerability;
import bugfind.utils.pmdadapters.VariableInfo;
import bugfind.utils.pmdadapters.ViolationLocationDescription;
import bugfind.utils.pmdadapters.VulnerabilityDefinitionItem;
import bugfind.utils.pmdadapters.VulnerabilityMitigationItem;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceBodyDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTStatement;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.symboltable.JavaNameOccurrence;
import net.sourceforge.pmd.lang.java.symboltable.VariableNameDeclaration;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;

/**
 *
 * @author Mikosh
 */
public class XXEVulDetectorRule extends AbstractJavaRule {
    static protected List<ImportInfo> listImportInfo = new ArrayList<ImportInfo>();//protected Set<ImportWrapper> imports = new HashSet<ImportWrapper>();
    static protected List<PossibleVulnerability> listPossibleVulnerability = new ArrayList<>();
    static protected List<PossibleSolution> listPossibleSolution = new ArrayList<>();
    static protected List<VulnerabilityDefinitionItem> listDefaultVDI = getDefaultVulnerabilityDefinitionList();    
    static protected List<VulnerabilityMitigationItem> listDefaultVMI = getDefaultVulnerabilityMitigationList();
    static protected Map<VariableInfo, Node> vInfoNodeMap = new HashMap<>();
    
    @Override
    public void start(RuleContext ctx) {
        listPossibleVulnerability.clear();
        listPossibleSolution.clear();
        super.start(ctx); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object visit(ASTCompilationUnit node, Object data) {
        listImportInfo.clear();
        return super.visit(node, data); //To change body of generated methods, choose Tools | Templates.
    }

    public Object visit(ASTImportDeclaration node, Object data) {
        listImportInfo.add(new ImportInfo(node));
        //addViolation(data, node, node.getNameDeclaration().getImage());
        return data;
    }   

    @Override
    public Object visit(ASTClassOrInterfaceBodyDeclaration node, Object data) {
      
        RuleContext rctx = (RuleContext) data;

        Map<VariableNameDeclaration, List<NameOccurrence>> vnd = getVariablesWithinScope(node,
                node.getScope().getDeclarations(VariableNameDeclaration.class));

        //  first check for possible vulnerabilities
        for (Map.Entry<VariableNameDeclaration, List<NameOccurrence>> entry : vnd.entrySet()) {
            VariableInfo vInfo = new VariableInfo(entry.getKey());
            List<PossibleVulnerability> listPV = getPossibleVulnerabilities(vInfo, listDefaultVDI);
            
            for(PossibleVulnerability pi : listPV) {
                int startLine = pi.getVariableInfo().getNameOccurrence(pi.getOccurrenceIndex()).getLocation().getBeginLine();
                int endLine = pi.getVariableInfo().getNameOccurrence(pi.getOccurrenceIndex()).getLocation().getEndLine();
                try {
                    pi.setLocationDescription(new ViolationLocationDescription(rctx.getSourceCodeFilename(), startLine, endLine));
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(XXEVulDetectorRule.class.getName()).log(Level.SEVERE, null, ex);
                    throw new RuntimeException("A runtime error has occurred: " + ex);
                }
            }
            
            listPossibleVulnerability.addAll(listPV);
        }
        
        // next check for possible solutions in any of the variables
        for (Map.Entry<VariableNameDeclaration, List<NameOccurrence>> entry : vnd.entrySet()) {
            VariableInfo vInfo = new VariableInfo(entry.getKey());
            List<PossibleSolution> listPS = getPossibleSolutions(vInfo, listDefaultVMI);
            
            for(PossibleSolution pi : listPS) {
                int startLine = pi.getVariableInfo().getNameOccurrence(pi.getOccurrenceIndex()).getLocation().getBeginLine();
                int endLine = pi.getVariableInfo().getNameOccurrence(pi.getOccurrenceIndex()).getLocation().getEndLine();
                try {
                    pi.setLocationDescription(new ViolationLocationDescription(rctx.getSourceCodeFilename(), startLine, endLine));
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(XXEVulDetectorRule.class.getName()).log(Level.SEVERE, null, ex);
                    throw new RuntimeException("A runtime error has occurred: " + ex);
                }
            }
            
            listPossibleSolution.addAll(listPS);
        }
        
        //System.out.println("listPV- size: " + listPossibleVulnerability);

        
        return super.visit(node, data); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void end(RuleContext ctx) { 
        //ctx.setLanguageVersion(LanguageVersion.JAVA_18);
    
        CustomReport creport = new CustomReport("SSE XXE", "Exploitable Vulnerabilities Found");
       
        for (PossibleVulnerability pv : listPossibleVulnerability) {
            boolean isResolved = false;
            for (PossibleSolution ps : listPossibleSolution) {
                isResolved = isResolvedByPossibleSolution(pv, ps);
                if (isResolved) {//System.out.println("resolved pv " + pv);
                    break;
                }
            }
            
            if (!isResolved) {            
                creport.addViolation(pv.getLocationDescription().getFileFullPath(), pv.getLocationDescription().getStartLine(),
                        pv.getLocationDescription().getCodeExtract(), pv.getVulnerabilityDefinitionItem().getReason());
            }
        }
        System.out.println(creport.getReportAsString());
        
        listPossibleVulnerability.clear();
        super.end(ctx); //To change body of generated methods, choose Tools | Templates.
    }
    
    private boolean isResolvedByPossibleSolution(PossibleVulnerability pv, PossibleSolution ps) {
        VariableInfo pvVarInfo = pv.getVariableInfo();
        VariableInfo psVarInfo = ps.getVariableInfo();
        MethodCallInfo pvMCI = pvVarInfo.getMethodCallInfoAtOccurrence(pv.getOccurrenceIndex());
        MethodCallInfo psMCI = psVarInfo.getMethodCallInfoAtOccurrence(ps.getOccurrenceIndex());
        
        if (pvVarInfo.getVariableName().equals(psVarInfo.getVariableName()) 
                && pvVarInfo.getVariableType().equals(pvVarInfo.getVariableType())
                && pvMCI.getCallerName().equals(psMCI.getCallerName())
                //&& pvMCI.getMethodName().equals(psMCI.getMethodName())
                //&& MethodArgument.areArgumentsEqual(pvMCI.getParameterList(), psMCI.getParameterList())
                && areWithinSameScope(pvVarInfo, pv.getOccurrenceIndex(), psVarInfo, ps.getOccurrenceIndex())) {
            if (compareWithinSameScope(pvVarInfo, pv.getOccurrenceIndex(), psVarInfo, ps.getOccurrenceIndex()) > 0) {
                return true;
            }
        }
        
        return false;
        
            
    }
    
    private boolean areWithinSameScope(VariableInfo vi1, int occurrenceIndex1, VariableInfo vi2, int occurrenceIndex2) {
        JavaNameOccurrence no1 = (JavaNameOccurrence) vi1.getNameOccurrence(occurrenceIndex1);
        JavaNameOccurrence no2 = (JavaNameOccurrence) vi2.getNameOccurrence(occurrenceIndex2);
        JavaNode jn1 = no1.getLocation();
        JavaNode jn2 = no2.getLocation();
      
        if ((jn1.getScope().contains(no2) && jn2.getScope().contains(no1)) 
                && jn1.getScope().getParent().equals(jn2.getScope().getParent())) {
            return true;
        }
        else {
            return false;
        }
    }
    
    
    
    private int compareWithinSameScope(VariableInfo vi1, int occurrenceIndex1, VariableInfo vi2, int occurrenceIndex2) {
        JavaNameOccurrence no1 = (JavaNameOccurrence) vi1.getNameOccurrence(occurrenceIndex1);
        JavaNameOccurrence no2 = (JavaNameOccurrence) vi2.getNameOccurrence(occurrenceIndex2);
        JavaNode jn1 = no1.getLocation();
        JavaNode jn2 = no2.getLocation();
        
        if (!(jn1.getScope().contains(no2) && jn2.getScope().contains(no1)) 
                && jn1.getScope().getParent().equals(jn2.getScope().getParent())) {
            throw new RuntimeException("Not within the same scope");
        }
        
        int line1 = jn1.getBeginLine(), col1 = jn1.getBeginColumn(), line2 = jn2.getBeginLine(), col2 = jn2.getBeginColumn();
        if (line1 == line2 && col1 == col2) {
            return 0;
        }
        else if (line1 < line2){
            return -1;
        }
        else if (line2 < line1) {
            return 1;
        }
        else if (line1 == line2 && col1 < col2) {
            return -1;
        }
        else {
            return 1;
        }
    }
    
    private void printNode(Node n) {        
        System.out.println("node: " + n + " expr: " +n.getImage());
        System.out.println("doc: " +n.getAsDocument().getTextContent());
        for (int i=0; i<n.jjtGetNumChildren(); ++i) {
            printNode(n.jjtGetChild(i));
        }
    }
    
    
    private Map<VariableNameDeclaration, List<NameOccurrence>> getVariablesWithinScope(Node parentNode, Map<VariableNameDeclaration, List<NameOccurrence>> mapObj) {
        if (parentNode.jjtGetNumChildren() > 0) {
            for (int i=0; i<parentNode.jjtGetNumChildren(); ++i) {
                Node aChild = parentNode.jjtGetChild(i);
                if (aChild instanceof ASTStatement ) {
                    Map<VariableNameDeclaration, List<NameOccurrence>> aMap = ((ASTStatement)aChild).getScope().getDeclarations (VariableNameDeclaration.class);
                    mapObj.putAll(aMap);
                    for (Map.Entry<VariableNameDeclaration, List<NameOccurrence>> entry : aMap.entrySet()) {
                        vInfoNodeMap.put(new VariableInfo(entry.getKey()), aChild);
                    }
                    if (aChild.jjtGetNumChildren() > 0) {
                        getVariablesWithinScope(aChild, mapObj);
                    }
                }
                else {
                    getVariablesWithinScope(aChild, mapObj);
                }
            }
        }
        
        return mapObj;
    }   
    
    public static List<VulnerabilityDefinitionItem> getDefaultVulnerabilityDefinitionList() {
        List<VulnerabilityDefinitionItem> list = new ArrayList<>();
        
        VulnerabilityDefinitionItem item =  new VulnerabilityDefinitionItem("JDOMXXESAXBUILDER","org.jdom2.input.SAXBuilder", 
                "org.jdom2.input", "build", "Using the JDOM library SAXBuilder build() method is vulnerable to XXE attack. "
                        + "Consider turing off DTD declarations by using the setFeature() method");
        list.add(item);
        item = new VulnerabilityDefinitionItem("DOMDOCUMENTBUILDER","javax.xml.parsers.DocumentBuilder", "javax.xml.parsers", "parse", "Using the "
                + "DocumentBuilder's parse() method at default XML settings is vulnerable to XXE Attack. Consider turning off XML external entity processing by calling the "
                + " method DocumentBuilder.setFeature(\"http://xml.org/sax/features/external-general-entities\", false);");
        list.add(item);
        item = new VulnerabilityDefinitionItem("SAXSAXPARSER", "javax.xml.parsers.SAXParser", "javax.xml.parsers", "parse", "Using the "
                + "SAXParser's parse() method at default XML settings is vulnerable to XXE Attack. Consider turning off XML external entity processing by calling the "
                + " method SAXParserFactory.setFeature(\"http://xml.org/sax/features/external-general-entities\", false);");
        list.add(item);
        
        return list;
    }
    
    public static List<VulnerabilityMitigationItem> getDefaultVulnerabilityMitigationList() {
        List<VulnerabilityMitigationItem> list = new ArrayList<>();
        
        VulnerabilityMitigationItem item =  new VulnerabilityMitigationItem("org.jdom2.input.SAXBuilder", 
                "org.jdom2.input", "setFeature", 
                new MethodArgument("\"http://apache.org/xml/features/disallow-doctype-decl\"", "String")//"java.lang.String")
                , new MethodArgument("true", "boolean"));
        list.add(item);
        item =  new VulnerabilityMitigationItem("javax.xml.parsers.DocumentBuilder", 
                "javax.xml.parsers", "setFeature", 
                new MethodArgument("\"http://xml.org/sax/features/external-general-entities\"", "String")//"java.lang.String")
                , new MethodArgument("false", "boolean"));
        list.add(item);
        item =  new VulnerabilityMitigationItem("javax.xml.parsers.DocumentBuilder", 
                "javax.xml.parsers", "setFeature", 
                new MethodArgument("\"http://xml.org/sax/features/external-parameter-entities\"", "String")//"java.lang.String")
                , new MethodArgument("false", "boolean"));        
        list.add(item);
        item =  new VulnerabilityMitigationItem("javax.xml.parsers.SAXParserFactory", 
                "javax.xml.parsers", "setFeature", 
                new MethodArgument("\"http://xml.org/sax/features/external-general-entities\"", "String")//"java.lang.String")
                , new MethodArgument("false", "boolean"));
        list.add(item);
        item =  new VulnerabilityMitigationItem("javax.xml.parsers.SAXParserFactory", 
                "javax.xml.parsers", "setFeature", 
                new MethodArgument("\"http://xml.org/sax/features/external-parameter-entities\"", "String")//"java.lang.String")
                , new MethodArgument("false", "boolean"));        
        list.add(item);
        
        return list;
    }
 
    private List<PossibleVulnerability> getPossibleVulnerabilities(VariableInfo vinfo, 
            List<VulnerabilityDefinitionItem> lstDefs) {
        List<PossibleVulnerability> listPV = new ArrayList<>();
        
        int numUsages = vinfo.getNumberOfUsages();
        
        MethodCallInfo mci;
        for (int i=0; i<numUsages; ++i) {
            mci = vinfo.getMethodCallInfoAtOccurrence(i);
            for (VulnerabilityDefinitionItem vdi : lstDefs) {
                if (isMatchVulnerability(vinfo.getVariableType(), mci, vdi)) {
                    listPV.add(new PossibleVulnerability(vdi, vinfo, i));
                }
            }
        }
        
        return listPV;
    }
    
    private List<PossibleSolution> getPossibleSolutions(VariableInfo vinfo, 
            List<VulnerabilityMitigationItem> lstStops) {
        //throw new UnsupportedOperationException("not supported yet");
        List<PossibleSolution> listPS = new ArrayList<>();
        
        int numUsages = vinfo.getNumberOfUsages();
        
        MethodCallInfo mci;
        for (int i=0; i<numUsages; ++i) {
            mci = vinfo.getMethodCallInfoAtOccurrence(i);
            for (VulnerabilityMitigationItem vsi : lstStops) {
                if (isMatchStop(vinfo.getVariableType(), mci, vsi)) {
                    listPS.add(new PossibleSolution(vsi, vinfo, i, vinfo.getMethodArgumentsInfoAtOccurrence(i)));
                }
            }
        }
        
        return listPS;
    }
    
    private boolean isMatchVulnerability(String callerClassName, MethodCallInfo mci, VulnerabilityDefinitionItem vdi) {
        if (vdi.getFullQualifiedClassName().endsWith(callerClassName) && mci != null
                && mci.getMethodName().equals(vdi.getMethodName())) {
            return true;
        }

        return false;
    }
    
    private boolean isMatchStop(String callerClassName, MethodCallInfo mci, VulnerabilityMitigationItem vsi) {
        if (vsi.getFullyQualifiedClassName().endsWith(callerClassName) && mci != null
                && mci.getMethodName().equals(vsi.getMethodName())
                && MethodArgument.areArgumentsEqual(mci.getParameterList(), vsi.getArgumentList())) {
            return true;
        }

        return false;
    }
}
