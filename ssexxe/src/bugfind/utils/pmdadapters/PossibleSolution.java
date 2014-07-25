/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bugfind.utils.pmdadapters;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mikosh
 */
public class PossibleSolution {
    VulnerabilityMitigationItem vulnerabilityDefinitionItem;
    VariableInfo variableInfo;
    ViolationLocationDescription locationDescription;
    int occurrenceIndex;
    List<MethodArgument> methodArgumentList;
    
    public PossibleSolution(VulnerabilityMitigationItem vulnerabilityDefinitionItem, VariableInfo variableInfo, 
            int occurrenceIndex, List<MethodArgument> args) {
        this.vulnerabilityDefinitionItem = vulnerabilityDefinitionItem;
        this.variableInfo = variableInfo;
        this.occurrenceIndex = occurrenceIndex;
        
        methodArgumentList = new ArrayList<>();
        if (args != null) {
            for (MethodArgument marg : args) {
                methodArgumentList.add(marg);
            }
        }
    }
    
    public PossibleSolution(VulnerabilityMitigationItem vulnerabilityDefinitionItem, VariableInfo variableInfo, 
            int occurrenceIndex) {
        this(vulnerabilityDefinitionItem, variableInfo, occurrenceIndex, null);
    }

    public VulnerabilityMitigationItem getVulnerabilityDefinitionItem() {
        return vulnerabilityDefinitionItem;
    }

    public VariableInfo getVariableInfo() {
        return variableInfo;
    }

    public int getOccurrenceIndex() {
        return occurrenceIndex;
    }

    public List<MethodArgument> getMethodArgumentList() {
        return methodArgumentList;
    }
        
    public ViolationLocationDescription getLocationDescription() {
        return locationDescription;
    }

    public void setLocationDescription(ViolationLocationDescription locationDescription) {
        this.locationDescription = locationDescription;
    }
}
