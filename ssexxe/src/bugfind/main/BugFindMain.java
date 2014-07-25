/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bugfind.main;

import java.util.Arrays;
import net.sourceforge.pmd.PMD;

/**
 *
 * @author Mikosh
 */
public class BugFindMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        // TODO code application logic here
        System.out.println("started...");
        
        String[] args2 = (args.length > 0) ? Arrays.copyOf(args, args.length + 4) : args;
        args2[args.length] = "-f";
        args2[args.length+1] = "text";
        args2[args.length+2] = "-R";
        args2[args.length+3] = "xxevulfind.xml";
        
        PMD.main(args2);
        
//        args = new String[]{"-dir", "C:\\Users\\Mikosh\\Documents\\NetbeansProjects\\MyXMLTest\\src",
//                "-f", "text", "-R",  
//                //"C:\\Users\\Mikosh\\Documents\\NetbeansProjects\\PMDLibGen\\src\\rulesets\\java\\imports.xml"};
//                "C:\\Users\\Mikosh\\Documents\\NetbeansProjects\\BugFindPMDTest\\src\\bugfind\\rules\\xxevulfind.xml"};
//        
//        //System.out.println(Arrays.asList(args2));
//        PMD.main(args);
    }
    
}
