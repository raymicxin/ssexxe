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
public class MethodCallInfo {
    private String callerName;
    private String methodName;
    private List<MethodArgument> parameterList;

    public MethodCallInfo(String callerName, String methodName, List<MethodArgument> parameterList) {
        this.callerName = callerName;
        this.methodName = methodName;
        this.parameterList = (parameterList == null) ? new ArrayList<MethodArgument>(): parameterList;
    }

    public String getCallerName() {
        return callerName;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<MethodArgument> getParameterList() {
        return parameterList;
    }

    @Override
    public String toString() {
        return callerName + "." + methodName + "("+ parameterList + ")"; //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
}
