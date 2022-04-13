package com;

import util.ConnectToMySQLTestDB;
import util.GlobalVariables;

import javax.swing.text.html.HTML;
import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rgupta on 2/26/2019.
 */
public class BuildReportFromDB {
    public  static int passCount = 0;
    public  static int failCount = 0;
    public static String reportHtml = "";

   public static String css = "<!DOCTYPE html>\n" +
           "<html>\n" +
           "<table>\n" +
           "<thead> </thead>\n" +
           "\t\t\t<style>\n" +
           "\t\t\ttable, th, td {\n" +
           "\t\t\t\tborder: 1px solid #BABABA;\n" +
           "\t\t\t\tborder-collapse: collapse;\n" +
           "\t\t\t}\n" +
           "            table {\n" +
           "\t\t\ttable-layout:auto; \n" +
           "\t\t\tmax-width:767px;\n" +
           "\t\t\tfont-family: arial, sans-serif;\n" +
           "\t\t\t}\n" +
           "            th {\n" +
           "\t\t\tstyle =\"color:blue\";\n" +
           "\t\t\ttext-align: left;\n" +
           "\t\t\tpadding: 8px;\n" +
           "\t\t\tword-wrap:break-word;\n" +
           "\t\t\t}\n" +
           "            td {\n" +
           "\t\t\tmax-width:767px;\n" +
           "\t\t\ttext-align: left;\n" +
           "\t\t\tpadding: 8px;\n" +
           "\t\t\t}\n" +
           "\t\t\ttr:nth-child(even){background-color: #f2f2f2;}\n" +
           "           \n" +
           "            .pass, .pass a {\n" +
           "                color: #04853A;\n" +
           "            }\n" +
           "   \n" +
           "            .fail, .fail a {\n" +
           "             color: #b60808;\n" +
           "            }\n" +
           "            \n" +
           "           </style>";


    public static void  buildReport(String tableName, ArrayList<String> tableHeadersToBeIgnored, String orderby) throws Exception {
        makeHtml(tableName,tableHeadersToBeIgnored, orderby);
        //System.out.println("The HTML : "+ reportHtml);
    }

    public static String getReportHtml(){
        return reportHtml;
    }

    private static void  makeHtml(String tableName, ArrayList<String> tableHeadersToBeIgnored,String orderby) throws Exception {
        ArrayList<String> tableHeaderNames = new ArrayList<String>();


        //ignoreTableHeaderNames.add("Status");
        Connection con = ConnectToMySQLTestDB.getInsstance().getMySqlConnection();
        Statement stmt = con.createStatement();
        ResultSet rs = null;
        String name = null;

        String query = "Select *  from "+tableName +" order by "+orderby;
        System.out.println(query);
        rs = stmt.executeQuery(query);

        /* NOW CREATE TABLE HEADING AS HTML HEADERS IN THE REPORT*/
        ResultSetMetaData rsmd = rs.getMetaData();

        String startTableRowHtml = "<tr>\n";
        String TableHeadinghtml = "";
        String endTableRowHtml = "</tr>\n"; //</thead>


        //this is loop to check if user asked any table headers not to be included
        if (tableHeadersToBeIgnored!= null){
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                 name = rsmd.getColumnName(i);
                boolean found = false;
                for(int j=0; j<tableHeadersToBeIgnored.size();j++){
                    String s = tableHeadersToBeIgnored.get(j);
                    if(name.equals(s)){
                        System.out.println("I found what to take out.");
                        found = true;
                    }
                }
                if(found)
                    continue;
                tableHeaderNames.add(name);
            }
        }
        else{
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                 name = rsmd.getColumnName(i);

                tableHeaderNames.add(name);
            }
        }



        /* NOW CREATE TABLE HEADING VALUES AS HTML IN THE REPORT*/
        String tableDataHTML = "";
        while (rs.next()){
            tableDataHTML =tableDataHTML + "<tr>";
            for ( int i =0 ; i<tableHeaderNames.size(); i++)
                tableDataHTML = tableDataHTML + "\n<td class=\"" + rs.getString("status").toLowerCase() + "\">"
                        + rs.getString(tableHeaderNames.get(i)) + "</td>\n";

            if (rs.getString("status").toLowerCase().equalsIgnoreCase("pass"))
                passCount++;
            else if (rs.getString("status").toLowerCase().equalsIgnoreCase("fail"))
                failCount++;

            tableDataHTML = tableDataHTML + "</tr>\n";
        }//DONE CREEATING CELL VALUES



        /* Now replace the header name with underscore to space then form the header "TH" HTML*/
        for (int i = 0; i < tableHeaderNames.size(); i++) {
            name = tableHeaderNames.get(i);
            name = name.replace("_", " ");
            TableHeadinghtml = TableHeadinghtml + "<th align=\"center\"; bgcolor=\"#C2DDF6\">" + name + "</th>\n";
        }
        String tableHeaderHTML = startTableRowHtml + TableHeadinghtml + endTableRowHtml;
        //DONE CREEATING HEADERS



        con.close();
        reportHtml = reportHtml + css  + tableHeaderHTML + tableDataHTML + "</table>\n</table>\n</html>";
        System.out.println("*************************HTML Report *********************");
        System.out.println(reportHtml);
    }

    public static void addMessage(String message){
        reportHtml = reportHtml + "\n"+message;
    }
}
