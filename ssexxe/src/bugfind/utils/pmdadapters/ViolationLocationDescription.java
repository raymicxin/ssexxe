/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bugfind.utils.pmdadapters;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 *
 * @author Mikosh
 */
public class ViolationLocationDescription {
    private String fileFullPath;
    private int startLine;
    private int endLine;
    private String codeExtract;

    public ViolationLocationDescription(String fileFullPath, int startLine, int endLine) throws FileNotFoundException {
        this.fileFullPath = fileFullPath;
        this.startLine = startLine;
        this.endLine = endLine;
        this.codeExtract = extractCodeFromFile(fileFullPath, startLine, endLine);
    }
    
    private String extractCodeFromFile(String filePath, int startLine, int endLine) throws FileNotFoundException {
        File f = new File(filePath);
        Scanner scn = new Scanner(f);
        StringBuilder sb = new StringBuilder();
        int cnt = 0;
        while (scn.hasNext()) {
            ++cnt;
            String s = scn.nextLine();
            if (cnt >= startLine && cnt <= endLine) {
                sb.append(s).append("\n");
            }
        }
        
        return sb.toString().trim();
    }

    public String getFileFullPath() {
        return fileFullPath;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public String getCodeExtract() {
        return codeExtract;
    }
    
    
    
    
}
