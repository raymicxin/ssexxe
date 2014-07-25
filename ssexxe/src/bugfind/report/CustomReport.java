/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bugfind.report;

/**
 *
 * @author Mikosh
 */
public class CustomReport {    
    int sn=0;
    String appTitle;
    String reportTitle;
    StringBuffer reportString;

    public CustomReport(String appTitle, String reportTitle) {
        this.appTitle = appTitle;
        this.reportTitle = reportTitle;
        reportString = new StringBuffer();
        
        reportString.append("<html><head><title>").append(appTitle).append("</title></head><body>\n")
                .append("<center><h3>").append(appTitle).append("</h3></center><center><h3>")
                .append(reportTitle).append("</h3></center>")
                .append("<table align=\"center\" cellspacing=\"0\" cellpadding=\"3\"><tr>\n")
                .append("<th>#</th><th>File</th><th>Line</th><th>Code Extract</th><th>Problem</th></tr>");
    }
    
    
    
    public void addViolation(String fileName, int line, String codeExtract, String problemDescription) {
        ++sn;
        String altColor = (sn % 2 == 0) ? "" : " bgcolor=\"lightgrey\"";
        reportString.append("<tr").append(altColor).append(">")
                .append("<td align=\"center\">").append(sn).append("</td>")
                .append("<td width=\"*%\"><a href=\"file:///").append(fileName).append("\">").append(fileName).append("</a>").append("</td>")
                .append("<td align=\"center\" width=\"*%\">").append(line).append("</td>")
                .append("<td width=\"*%\">").append(codeExtract).append("</td>")
                .append("<td width=\"*\">").append(problemDescription).append("</td>")
                .append("<tr>");
    }
   
    public String getReportAsString() {
        return reportString.toString() + "</body></html";
    }
    
}
