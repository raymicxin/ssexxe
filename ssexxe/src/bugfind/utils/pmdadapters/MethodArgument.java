/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bugfind.utils.pmdadapters;

import java.util.List;

/**
 *
 * @author Mikosh
 */
public class MethodArgument {
    String argumentValue;
    String argumentType;

    public MethodArgument(String argumentValue, String argumentType) {
        this.argumentValue = argumentValue;
        this.argumentType = argumentType;
    }

    public String getArgumentValue() {
        return argumentValue;
    }

    public String getArgumentType() {
        return argumentType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MethodArgument) {
            MethodArgument ma = (MethodArgument) obj;
            return (this.getArgumentValue().equals(ma.getArgumentValue()) 
                    && this.getArgumentType().equals(ma.getArgumentType()));
        }
        else {
            return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
        }
    }
    
    
    
    public static boolean areArgumentsEqual(List<MethodArgument> lma1, List<MethodArgument> lma2) {
        if (lma1.size() != lma2.size()) return false;
        else {
            for (int i=0; i<lma1.size(); ++i) {
                if (!lma1.get(i).equals(lma2.get(i))) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
}
