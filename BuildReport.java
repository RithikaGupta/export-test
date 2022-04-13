package com;

import util.ConnectToMySQLTestDB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by rgupta on 8/29/2017.
 */
public class BuildReport {

    public static int failCount =0;
    public  static int passCount = 0;


    public static String reportHtml = "<table>\n" +
            "<thead>\n" +
            "<style>\n" +
            "   table {border-collapse:collapse; table-layout:fixed; width:310px;}\n" +
            "   table td {border:solid 1px #096b6b; width:100px; word-wrap:break-word;}"+
            "h1, h2, h3 {\n" +
            "    white-space: nowrap;\n" +
            "}\n" +
            "\n" +
            "h2 {\n" +
            "    font-size: 120%;\n" +
            "}\n" +
            "\n" +
            ".success, .success a {\n" +
            "    color: #008000;\n" +
            "}\n" +
            "\n" +
            ".failures, .failures a {\n" +
            "    color: #b60808;\n" +
            "}\n" +
            "\n" +
            "div.tab th, div.tab table {\n" +
            "    border-bottom: solid #d0d0d0 1px;\n" +
            "}"+
            "div.tab th {\n" +
            "    text-align: middle;\n" +
            //  "    white-space: nowrap;\n" +
            "    padding-left: 10em;\n" +
            "}"+
            "div.tab th:first-child {\n" +
            "    padding-left: 0;\n" +
            "}\n"+
            "div.tab td {\n" +
            // "    white-space: nowrap;\n" +
            "    padding-left: 10em;\n" +
            "    padding-top: 10px;\n" +
            "    padding-bottom: 7px;\n" +
            "}\n"+
            "th:first-child {\n" +
            "    padding-left: 0;\n" +
            "}"+
            "td:first-child {\n" +
            "    padding-left: 0;\n" +
            "    padding-right: 50px;\n" +
            "}"+
            "</style>" +
            "<tr>\n" +
            "<th align=\"left\"> Test</th>\n" +
            "<th align=\"left\"> Result</th>\n" +
            "<th align=\"left\">Region </th>\n" +
            "<th align=\"left\">ExportDuration  </th>\n" +

            "<th align=\"left\"> Export Drive</th>\n" +
            "<th align=\"left\">Windows Export Folder Location</th>\n" +
            "<th align=\"left\">Linux Export Folder Location</th>\n" +
            "</tr>\n" +
            "</thead>" +
            "<tr>";




    public static void  buildReport() throws Exception {
        makeHtml();
        //System.out.println("The HTML : "+ reportHtml);
    }

    public static String getReportHtml(){
        return reportHtml;
    }





    private static void  makeHtml() throws Exception {
        File f = new File("build\\reports\\tests\\test\\classes");
        List<String> fileNames = getAllFilesNames_InDir(f.getAbsoluteFile());
        System.out.println("Files Found in CLASSES "+ fileNames.size());

        for (String file : fileNames) {
            if(file.equals("com.PollForExportFolderTest.html")) {
                try {
                    System.out.println("Exploring File : " + file);
                    BufferedReader input = new BufferedReader(new FileReader(f + "\\" + file));
                    String str;
                    String startPoint = null;
                    String failureOrSuccess = null;
                    String exportType_InHTMLReport = null;
                    String exportRegion_InHTMLReport = null;

                    while ((str = input.readLine()) != null) {

                        if (str.contains("<td class")) {
                            startPoint = str;
                            //get the Class attribute value  - whcih is Failure or Success so that it can be used in manual hmtl report that we r building below
                            failureOrSuccess = startPoint.split("class=\"")[1].split("\"")[0];
                            System.out.println("startPoint : " + startPoint);
                        }

                        // TO get Nightly  or PR export results being parsed
                        if (str.contains("Nightly") || str.contains("NIGHTLY") || str.contains("nightly"))
                            exportType_InHTMLReport = "NIGHTLY";
                        else if (str.contains("PR") || str.contains("pr"))
                            exportType_InHTMLReport = "PR";
                        else if (str.contains("SHAREPOINT") || str.contains("SharePoint"))
                            exportType_InHTMLReport = "SHAREPOINT";
                        //System.out.println("Export TYPE : " + exportType_InHTMLReport);


                        if (str.contains("QA")) {
                            exportRegion_InHTMLReport = "QA";
                        } else if (str.contains("Dev")) {
                            exportRegion_InHTMLReport = "DEV";
                        } else if (str.contains("Stag") || str.contains("STAG")) {
                            exportRegion_InHTMLReport = "STAGING";
                        }
                        else if (str.contains("Prod") || str.contains("PROD")) {
                            exportRegion_InHTMLReport = "PRODUCTION";
                        }
                        else if (str.contains("ProdSharePoint") || str.contains("PRODSHAREPOINT")) {
                            exportRegion_InHTMLReport = "PROD SHAREPOINT";
                        }
                        else if (str.contains("StagingSharePoint") || str.contains("STAGINGSHAREPOINT")) {
                            exportRegion_InHTMLReport = "STAGING SHAREPOINT";
                        }

                        System.out.println("Export Region : " + exportRegion_InHTMLReport);
                        // Now start building the html report for email.
                        if (startPoint != null) {

                            if (str.equals("</tr>")) {
                                reportHtml = reportHtml + "\n<td class=\"" + failureOrSuccess + "\">" + exportRegion_InHTMLReport + "</td>\n";
                                reportHtml = reportHtml + "\n<td class=\"" + failureOrSuccess + "\">" + getExportDuration(exportType_InHTMLReport, exportRegion_InHTMLReport) + "</td>\n";
                                reportHtml = reportHtml + "\n<td class=\"" + failureOrSuccess + "\">" + getExportDriveName(exportType_InHTMLReport, exportRegion_InHTMLReport) + "</td>\n";
                                reportHtml = reportHtml + "\n<td class=\"" + failureOrSuccess + "\">" + getExportFolderLocation(exportType_InHTMLReport, exportRegion_InHTMLReport) + "</td>\n";
                                reportHtml = reportHtml + "\n<td class=\"" + failureOrSuccess + "\">" + getLinuxBoxFolderLocation(exportType_InHTMLReport, exportRegion_InHTMLReport) + "</td>\n";
                                if (failureOrSuccess.equals("failures"))
                                    failCount++;
                                else if (failureOrSuccess.equals("success"))
                                    passCount++;
                            }
                            if(!str.contains("."))
                                reportHtml = reportHtml + str;
                            // str = input.readLine();

                        }
                        if (str.contains("</table>") && startPoint != null) {
                            reportHtml = reportHtml + str;
                            break;
                        }

                    }
                    reportHtml = reportHtml + "</html>";
                    System.out.println("HTML : " + reportHtml);
                    input.close();
                } catch (IOException e) {

                }
            }
        }
    }



    private static List<String> getAllFilesNames_InDir(File folder){

        List<String> fileName = new ArrayList<>();

        for( File fileEntry: folder.listFiles()){
            if(fileEntry.isFile() && !fileEntry.getName().equals("com.EmailTest.html"))
                fileName.add(fileEntry.getName());
        }
        return fileName;
    }

    private static String getExportFolderLocation(String exportType , String env ) throws Exception {
        String value = null;

        Connection mySql = ConnectToMySQLTestDB.getInsstance().getMySqlConnection();
        Statement stmt = mySql.createStatement();
        ResultSet rs = null;

        String query = "Select export_folder  from export_test_tbl where export_region='"+ env
                +"' and export_type ='" + exportType+ "'";
        System.out.println(query);

        rs = stmt.executeQuery(query);

        while(rs.next()){
            value = rs.getString("export_folder");
        }
        mySql.close();
        return value;
    }

    private static String getLinuxBoxFolderLocation(String exportType , String env ) throws Exception {
        String value = null;

        Connection mySql = ConnectToMySQLTestDB.getInsstance().getMySqlConnection();
        Statement stmt = mySql.createStatement();
        ResultSet rs = null;

        String query = "Select linuxBox_export_folder_location  from export_test_tbl where export_region='"+ env
                +"' and export_type ='" + exportType+ "'";
        System.out.println(query);

        rs = stmt.executeQuery(query);

        while(rs.next()){
            value = rs.getString("linuxBox_export_folder_location");
        }
        mySql.close();
        return value;
    }


    private static String getExportDuration(String exportType , String env ) throws Exception {
        String value = null;

        Connection mySql = ConnectToMySQLTestDB.getInsstance().getMySqlConnection();
        Statement stmt = mySql.createStatement();
        ResultSet rs = null;

        String query = "Select export_run_duration  from export_test_tbl where export_region='"+ env
                +"' and export_type ='" + exportType+ "'";
        rs = stmt.executeQuery(query);

        while(rs.next()){
            value = rs.getString("export_run_duration");
        }
        mySql.close();
        return value;
    }

    private static String getExportDriveName(String exportType , String env ) throws Exception {
        String value = null;

        Connection mySql = ConnectToMySQLTestDB.getInsstance().getMySqlConnection();
        Statement stmt = mySql.createStatement();
        ResultSet rs = null;

        String query = "Select export_drive_location  from export_test_tbl where export_region='"+ env
                +"' and export_type ='" + exportType+ "'";
        rs = stmt.executeQuery(query);

        while(rs.next()){
            value = rs.getString("export_drive_location");
        }
        mySql.close();
        return value;
    }

}



