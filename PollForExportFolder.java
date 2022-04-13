package com;

import com.mysql.jdbc.exceptions.MySQLDataException;
import getterClasses.GetCalenderDate;
import getterClasses.GetLastModifiedDateOfAFolder;
import getterClasses.GetterClass;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import util.*;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static util.ObjectRepository.version_xpath;

/**
 * Created by rgupta on 8/21/2017.
 *
 */
public class PollForExportFolder {

    public static HashMap<String, String> log = new HashMap<String, String>();
    boolean result = true;
    String exportFolder_pr = null;
    String exportDrive =null; // added to get the drive in which exported content is put.
    String zipFolder_pr = null;
    String exportFolder = null;
    String zipFolder = null;
    String exportBenginDate_pr = null;
    String exportEndDate_pr = null;
    String exportBenginDate = null;
    String exportEndDate = null;
    String message = null;
    String manifestID = null;

    private String env =null;
    private String exportType = null;
    private String exportRegion = null;
    String url =  null;

    public  void setEnvironment(String environment) throws Exception {
        env = environment;

        GlobalVariables.environment = environment;
        if (env.equals("QA"))
            GlobalVariables.exportRegion = "\\\\edweb01q\\";
        if (env.equals("Dev"))
            GlobalVariables.exportRegion = "\\\\edweb01d\\";
        if (env.equals("Staging"))
            GlobalVariables.exportRegion = "\\\\edapp0403s\\";
        if (env.equals("Production"))
            GlobalVariables.exportRegion = "\\\\edapp05p\\";
        if (env.equals("Prod Sharepoint")) {
            env = "Production";
            GlobalVariables.exportRegion = "\\\\edsp02p\\";
        }
        if (env.equals("Staging Sharepoint")){
            env = "Staging";
            GlobalVariables.exportRegion = "\\\\edsp0203s\\";
        }
        if (env.equals("Prod Report")){
            env = "Prod Report";
            GlobalVariables.exportRegion = "\\\\edrpapp01p\\";
        }
        exportRegion = GlobalVariables.exportRegion;
    }

    private void  findLatestManifest(String type) throws Exception {
        result = true; // doing true coz this is global variable and by setting to true it is being rest at the start of every export test.
        Connection con = ConnectToEditorialDB.getInstance(env).getConnection();
        Statement stmt = con.createStatement();
        String query;
        manifestID = "";
        message = null;
        deleteExportFolderLocation (type);

        switch (type){
            case "PR":
                exportFolder_pr = null;
                zipFolder_pr = null;
                exportBenginDate_pr = null;
                exportEndDate_pr = null;
                exportDrive =null;

                query = "SELECT TOP 1 * FROM ED_ADMIN.BUILD_MANIFEST\n" +
                        "WHERE  BUILD_MANIFEST_ID =\n" +
                        "(SELECT MAX(BUILD_MANIFEST_ID) FROM ED_ADMIN.BUILD_MANIFEST \n" +
                        "WHERE BUILD_NAME LIKE '%PUBLISH READY%' AND BUILD_NAME not like '%ADHOC%' AND BUILD_NAME not like '%CONPORT%'  )\n" +
                        "AND BUILD_BEGIN_DATE > DATEADD(DAY,DATEDIFF(DAY,1,GETDATE()),0)\n " +
                         "ORDER BY LAST_MOD_DATE DESC";
                ResultSet res = stmt.executeQuery(query);

                if(!res.isBeforeFirst()){
                    System.out.println("There was no export for publish ready yesterday which ran on "+env);
                    message ="Not Found";
                    exportDrive = " ";
                }
                else {
                    try {
                        while (res.next()) {
                            manifestID = res.getString("BUILD_MANIFEST_ID");
                            exportBenginDate_pr = res.getString("BUILD_BEGIN_DATE");
                            exportFolder_pr = res.getString("EXPORT_FOLDER").split(":")[1].replace("\\", "\\\\");
                            exportDrive = res.getString("EXPORT_FOLDER").split(":")[0];
                            zipFolder_pr = res.getString(("ZIP_FILE_PATH")).split(":")[1].replace("\\", "\\\\");
                            exportEndDate_pr = res.getString("BUILD_END_DATE");

                        }
                    } catch (ArrayIndexOutOfBoundsException |NullPointerException e) {
                        System.out.println("Exception thrown - please check the field might be empty .... " + e);
                        // we are here as there is export running which has no completion date  hence we are going to caluculate the time for how long it has been running.
                        Date now = new Date();
                        Date currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now)));
                        Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(exportBenginDate_pr);
                        System.out.println("Current Time =  "+ currentTime);
                        System.out.println("Start Time =  "+ startTime);
                        long diff = currentTime.getTime() - startTime.getTime();
                        long diffMinutes = diff / (60 * 1000) % 60;
                        long diffHours = diff / (60 * 60 * 1000);
                        if (diffHours > 6)
                            message = "Manifest ID " + manifestID + " - it has been running for "+ diffHours + " hours.";
                        else
                            message = "Manifest ID " + manifestID + " - it has been running for last " + diffHours +
                                " hours and "+diffMinutes +".";

                        /**
                         * Since the export hasn't finished - exportdrive will be put to BLANK instead of null else it will throw exception when inserting in export_tys_tbl in mysql
                         */
                        exportDrive = " ";
                    }
                }
                break;

            case "Nightly":

                exportFolder = null;
                zipFolder = null;
                exportBenginDate = null;
                exportEndDate =null;
                exportDrive = null;

                query = "SELECT TOP 1 * FROM ED_ADMIN.BUILD_MANIFEST\n" +
                        "WHERE  BUILD_MANIFEST_ID =\n" +
                        "(SELECT MAX(BUILD_MANIFEST_ID) FROM ED_ADMIN.BUILD_MANIFEST \n" +
                        "WHERE BUILD_NAME NOT LIKE '%PUBLISH READY%' AND BUILD_NAME not like '%ADHOC%' )\n" +
                        "AND BUILD_BEGIN_DATE > DATEADD(DAY,DATEDIFF(DAY,1,GETDATE()),0)\n" +
                       // "and BUILD_END_DATE is not null\n"+
                        "ORDER BY LAST_MOD_DATE DESC";
                res = stmt.executeQuery(query);

                if(!res.isBeforeFirst()){
                    System.out.println("There was no export for all site yesterday which ran on "+env);
                    message ="Not Found";
                    exportDrive = " ";
                }
                try{
                    while(res.next()) {
                        manifestID = res.getString("BUILD_MANIFEST_ID");
                        exportBenginDate = res.getString("BUILD_BEGIN_DATE");
                        exportFolder = res.getString("EXPORT_FOLDER").split(":")[1].replace("\\", "\\\\");
                        System.out.println();
                        exportDrive = res.getString("EXPORT_FOLDER").split(":")[0];
                        System.out.println(exportDrive);
                        zipFolder = res.getString(("ZIP_FILE_PATH")).split(":")[1].replace("\\", "\\\\");
                       exportEndDate = res.getString("BUILD_END_DATE");
                    }
                }
                catch (ArrayIndexOutOfBoundsException | NullPointerException e){
                    System.out.println("Exception thrown - please check the field might be empty .... "+e);

                  // we are here as there is export running which has no completion date  hence we are going to caluculate the time for how long it has been running.
                    Date now = new Date();
                    Date currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now)));
                    Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(exportBenginDate);
                    System.out.println("Current Time =  "+ currentTime);
                    System.out.println("Start Time =  "+ startTime);
                    long diff = currentTime.getTime() - startTime.getTime();
                    long diffMinutes = diff / (60 * 1000) % 60;
                    long diffHours = diff / (60 * 60 * 1000);
                    if (diffHours > 6)
                        message = "Found Export Manifest ID " + manifestID + " - it has been running for "+ diffHours +" hours.";
                    else if(exportFolder!=null && zipFolder==null)
                        message= exportFolder +" Found but no zip found";
                    else
                        message = "Found Export Manifest ID " + manifestID + " - it has been running for last " + diffHours +
                                " hours and "+diffMinutes +" mins .";
                    //throw e;
                    /**
                     * Since the export hasn't finished - exportdrive will be put to BLANK instead of null else it will throw exception when inserting in export_tys_tbl in mysql
                     */
                    exportDrive = " ";
                }

        }
         con.close();

    }

    public boolean didExportRun_pr(String type) throws Exception {
        exportType = type;
        boolean res=false;
    	findLatestManifest(exportType);
    	File file = new File(exportRegion+exportFolder_pr);
    	System.out.println("Export Folder: " + file);
    	System.out.println("PR Export Message: " + message);

    	if (!file.isDirectory() && message != null) {
    		System.out.println(file + " :File is not directory and message is not null " + message);
    		GlobalVariables.pr_exportFolder = message;
    	}
    	else if (!file.isDirectory() && message == null) {
    		System.out.println(file + " :File is not directory");
    		if(file.toString().contains("\\utd_export") )
    			// ||  file.toString().contains("\\edapp05p\\utd_export")   ) //eapp05p
    			/***
    			 * We are here because code is not able to access staging server - and so file.isDirectory() check will return false and raise false alarm.
    			 */
    			GlobalVariables.pr_exportFolder = file.toString();
    		else
    			GlobalVariables.pr_exportFolder = " There was no Export Run for last Night Or Export Directory is not present/accessible.";
    	}
    	else {
    		System.out.println("Found export");
    		GlobalVariables.pr_exportFolder = file.toString();
    	}

    	//manifestID =  manifestID+ "-"+ GlobalVariables.pr_exportFolder.split("-")[1];

    	setExportFolderLocation(GlobalVariables.pr_exportFolder, exportType);

    	if(file.toString().contains("\\utd_export"))
    		res = true;
    	else
    		res = file.isDirectory();

    	if(res & result)
    	    result=true;
    	else
    	    result=false;
        return result;
    }

    public boolean didZipFileGotCreated_pr() throws Exception {

    	File file = new File(exportRegion+zipFolder_pr);
        boolean res =false;
    	if(file.toString().contains("\\utd_export")){
    		res =  true;
    	}
    	else{
    		if(!file.isDirectory() && message!=null){
    			System.out.println(file+ " :File is not directory and message is not null "+message);
    			GlobalVariables.pr_exportFolder = message;
    		}
    		res= file.exists();
    	}
    	System.out.println("Zip Folder: " +file);
        if(res & result)
            result=true;
        else
            result=false;
    	return result;
    }

    public boolean didExportRun(String type) throws Exception {
        exportType = type;
    	findLatestManifest(type);
        boolean res=false;
    	if (GlobalVariables.environment.equals("DEV"))
    		exportRegion = exportRegion +"d\\";
    	else if (GlobalVariables.environment.equals("PRODUCTION"))
    		exportRegion = "\\\\edapp06p\\";

    	File file = new File(exportRegion+exportFolder);
    	GlobalVariables.nightly_exportFolder = file.toString();
    	System.out.println("Export Folder: " + file);
    	System.out.println("Nightly Export Message: " + message);

    	if(!file.isDirectory() && message!=null)
    		GlobalVariables.nightly_exportFolder = message;
    	else if(!file.isDirectory() && message==null) {

    		if(file.toString().contains("\\utd_export") )
    			//   file.toString().contains("\\edapp06p\\utd_export")   )
    			/***
    			 * We are here because code is not able to access staging server - and so file.isdirectory() check will return false and raise false alarm.
    			 */
    			GlobalVariables.nightly_exportFolder = file.toString();
    		else
    			GlobalVariables.nightly_exportFolder = " There was no Export Run for last Night Or Export Directory is not present/accessible.";
    	}
    	setExportFolderLocation( GlobalVariables.nightly_exportFolder,exportType);


    	if(file.toString().contains("\\utd_export"))
    	    res =true;
    	else
    		res = file.isDirectory();
        if(res & result)
            result=true;
        else
            result=false;
        return result;

    }

    public boolean didZipFileGotCreated() throws Exception {

        File file = new File(exportRegion+zipFolder);
        boolean res=false;
        if(file.toString().contains("\\utd_export")){
            res =  true;
        }
        else{
            if(!file.isDirectory() && message!=null){
                System.out.println(file+ " :File is not directory and message is not null "+message);
                GlobalVariables.nightly_exportFolder = message;
            }
            res= file.exists();
        }
        System.out.println("Zip Folder: " +file);
        if(res & result)
            result=true;
        else
            result=false;
        return result;
    }


    public boolean verifyIfExportWasProcessedOnConbld(String env, String exportType) throws Exception{

        this.env = env;
        String status = "fail";
        boolean result = false;
        String message ="";
        String exportFolderPathOnLinuxBox = GetterClass.getLinuxExportFolderPath(env, exportType);
        System.out.println("Received export Folder Path name  :  "+exportFolderPathOnLinuxBox);

        ConnectToLinuxBox connectToLinuxBox=ConnectToLinuxBox.connect(env);
        String currentDirectoryPath = connectToLinuxBox.changeDirectory(exportFolderPathOnLinuxBox);

        System.out.println("exportFolderPathOnLinuxBox :  "+exportFolderPathOnLinuxBox);
        try {
            if (currentDirectoryPath.equalsIgnoreCase(exportFolderPathOnLinuxBox))
                result = true;
                status = "pass";
        }catch (NullPointerException e){
            if(currentDirectoryPath==null) {
                message = "Could not find today manifest on " + GlobalVariables.conbld_serverName + ".";
                currentDirectoryPath ="-";
            }
            System.out.println("currentDirectoryPath :  "+"Could not find today manifest on "+GlobalVariables.conbld_serverName+".");
            status="fail";
        }
        SaveTheDataforHashmap.runQuery("Update "+ ObjectRepository.Export_test_table +" set `Publish_Folder_(Linux)` = '"
                +currentDirectoryPath  + "' where SequenceNo <> 'O15'and export_region = '" + env
                + "' and export_type = '" + exportType + "'",true );
        SaveTheDataforHashmap.runQuery("Update "+ ObjectRepository.Export_test_table +" set status ='"
                + status + "' where SequenceNo <> 'O15' and export_region = '" + env
                + "' and export_type = '" + exportType + "'",true );
        SaveTheDataforHashmap.runQuery("Update "+ ObjectRepository.Export_test_table +" set Message ='"
                + message + "' where SequenceNo <> 'O15' and export_region = '" + env
                + "' and export_type = '" + exportType + "'",true );
        SaveTheDataforHashmap.runQuery("Update "+ ObjectRepository.Export_test_table +" set Server ='"
                + GlobalVariables.conbld_serverName + "'  where SequenceNo <> 'O15' and export_region = '" + env
                + "' and export_type = '" + exportType + "'",true );
         return result;
    }



//VERIFY SHAREPONIT SITES
    public boolean verifyProdSharepointSite() throws Exception {
        exportDrive =" ";
        manifestID = " ";
        boolean res=false;
        String message = null;
        String status = null;
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy");

        String sharePointFolder = GlobalVariables.exportRegion+"\\utd_export";
        File file = new File(sharePointFolder);
        //File file = new File("Y:");
        GlobalVariables.nightly_exportFolder = sharePointFolder;

        long date = GetLastModifiedDateOfAFolder.getLatestModifiedDate(file);
        String folder_LastModifiedDate =dateFormatter.format(new Date(date));

        String today  = dateFormatter.format(new Date()); // get current date
        String yesterday  = dateFormatter.format(GetCalenderDate.yesterday()); // get current date
        String pastDate = "12-31-1969";
        System.out.println("Date Fetched = "+ folder_LastModifiedDate);
        System.out.println("Yesterday date Fetched = "+ yesterday);

        if(folder_LastModifiedDate.equals(yesterday)){
            //GlobalVariables.nightly_exportFolder = GlobalVariables.nightly_exportFolder + "\n The folder was last modified yesterday.";
            message = "Updated on "+folder_LastModifiedDate;
            res = true;
        }
        else if(folder_LastModifiedDate.equals(today)){
            //GlobalVariables.nightly_exportFolder = GlobalVariables.nightly_exportFolder + "\n The folder was modified today today .";
            message = "Updated on "+folder_LastModifiedDate;
            res = true;
        }
        else if(folder_LastModifiedDate.equalsIgnoreCase(pastDate)) {
            //GlobalVariables.nightly_exportFolder = GlobalVariables.nightly_exportFolder + "\n is not accessible";
            message = " Folder is not accessible.";
        }
        else{
            //GlobalVariables.nightly_exportFolder = GlobalVariables.nightly_exportFolder + "\n The folder has not been modified since "+folder_LastModifiedDate;
            message = "Updated on "+folder_LastModifiedDate;
            res = false;
        }
       // setExportFolderLocation(GlobalVariables.nightly_exportFolder, "SHAREPOINT");
        //log.put("Publish_Folder_(Linux)","-");
        if(res ) {
            result = true;
            status = "pass";
        }
        else {
            result = false;
            status = "fail";
        }

        // NOW ADD THIS TO TABLE export_fileTest_tbl
        sharePointFolder = sharePointFolder.replace("\\", "\\\\");

        System.out.println("sharePointFolder = "+sharePointFolder);
        String sequence = getSquenceNumberForExportTable(env,"Sharepoint");
        String query = "Delete from "+ ObjectRepository.Export_test_table +
                " Where Export_Region like '%"+ env+"%'  and Export_Type ='Sharepoint'";
        UpdateQuery.SQLUpDateQuery(query);

        log.clear();
        log.put("Export_Folder_(Windows)",sharePointFolder);
        log.put("Message", message);
        log.put("SequenceNo",sequence); // ADD THE SEQUENCE NUMBER
        log.put("export_Region",env);
        log.put("export_Type","Sharepoint");

        log.put("run_duration","-");
        log.put("Server","-");
        log.put("Publish_Folder_(Linux)","-");
        log.put("Published","-");
        log.put("Manifest_Id","-");

        log.put("status",status);
        SaveTheDataforHashmap.run(ObjectRepository.Export_test_table,log);
        return result;
    }

// Verify production report server is updated.
    public boolean verifyIfEditorialReportServerFolderUpdated() throws Exception{
        boolean result = false;
        String status =null;
        String message =null;
        log.clear();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy");
        String reportServer_FolderPath = ObjectRepository.editorialReportServer_FolderPath;
        String today  = dateFormatter.format(new Date()); // get current date
        String yesterday  = dateFormatter.format(GetCalenderDate.yesterday());
        System.out.println("Date yesterday = "+yesterday + " and today = "+today);
        ConnectToLinuxBox connectToLinuxBox=ConnectToLinuxBox.connect(env);
        String lastModDateOfSourceFOlder= connectToLinuxBox.runCommand("date +\"%m-%d-%Y\" -r "+reportServer_FolderPath);
        if (lastModDateOfSourceFOlder.trim().equals(yesterday) || lastModDateOfSourceFOlder.trim().equals(today)) { //
            result = true;

            status = "pass";
        }
        else {
            System.out.println(lastModDateOfSourceFOlder.trim() + "!="+yesterday);
            status ="fail";
        }
        message = "Updated on " + lastModDateOfSourceFOlder;
        // NOW ADD THIS TO TABLE export_fileTest_tbl
        String sequence = getSquenceNumberForExportTable(env,GlobalVariables.conbld_serverName);
        String query = "Delete from "+ ObjectRepository.Export_test_table +
                " Where SequenceNo = '"+sequence+"'";
        UpdateQuery.SQLUpDateQuery(query);
        log.clear();
         log.put("Export_Folder_(Windows)","-");
         log.put("Message", message);
        log.put("SequenceNo",sequence); // ADD THE SEQUENCE NUMBER
        log.put("Export_Region","Production");
        log.put("Export_Type","-");

        log.put("Run_Duration","-");
        log.put("Server",GlobalVariables.conbld_serverName);
        log.put("Publish_Folder_(Linux)",ObjectRepository.editorialSource_FolderPath );
        log.put("Published","-");
        log.put("Manifest_Id","-");

        log.put("Status",status);
        SaveTheDataforHashmap.run(ObjectRepository.Export_test_table,log);
        return result;
    }

    /*
  *
   * Verify if editorial source folder was updated BELOW
   *
  *
*/
    public boolean verifyIfEditorialSourceFolderUpdated() throws Exception{
        boolean result = false;
        String sequence = "0";
        String status =null;
        String message =null;
        log.clear();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy");
        String editorialSourceFolder = ObjectRepository.editorialSource_FolderPath;
        String today  = dateFormatter.format(new Date()); // get current date
        String yesterday  = dateFormatter.format(GetCalenderDate.yesterday());
        System.out.println("Date yesterday = "+yesterday + " and today = "+today);
        ConnectToLinuxBox connectToLinuxBox=ConnectToLinuxBox.connect(env);
        String lastModDateOfSourceFOlder= connectToLinuxBox.runCommand("date +\"%m-%d-%Y\" -r "+editorialSourceFolder);
        if (lastModDateOfSourceFOlder.trim().equals(yesterday) || lastModDateOfSourceFOlder.trim().equals(today)) { //
            result = true;
            status = "pass";
        }
        else {

            System.out.println(lastModDateOfSourceFOlder.trim() + "!="+yesterday);
            status ="fail";
        }
        message = "Updated on " + lastModDateOfSourceFOlder;
        // NOW ADD THIS TO TABLE export_fileTest_tbl
        switch (env){
            case "PRODUCTION":

        }
         sequence = getSquenceNumberForExportTable(env, GlobalVariables.conbld_serverName);
        String query = "Delete from "+ ObjectRepository.Export_test_table +
                " Where SequenceNo = '"+sequence+"'";
        UpdateQuery.SQLUpDateQuery(query);
        log.clear();
            log.put("Export_Folder_(Windows)","-");

            log.put("Message", message);
        log.put("SequenceNo",sequence); // ADD THE SEQUENCE NUMBER
        log.put("export_region",env);
        log.put("export_type","-");

        log.put("run_duration","-");
        log.put("Server",GlobalVariables.conbld_serverName);
        log.put("Publish_Folder_(Linux)",ObjectRepository.editorialSource_FolderPath);
        log.put("Published","-");
        log.put("Manifest_id","-");

        log.put("status",status);
        SaveTheDataforHashmap.run(ObjectRepository.Export_test_table,log);
        return result;
    }


//verify if LOCK file is present
        public boolean verifyIfLockFileIsPresent() throws Exception{
            String message =null;
            String status = null;
            boolean result = true;
            String lastModDateOflockFIle = "";
            String editorialSourceFolder = ObjectRepository.editorialSource_FolderPath;
            String lockFile = ConnectToLinuxBox.connect(env).getPathLikeAutoTabComplete(editorialSourceFolder,"lock.txt");

            if(lockFile.contains("lock")) {
                try {
                    ConnectToLinuxBox connectToLinuxBox = ConnectToLinuxBox.connect(env);
                     lastModDateOflockFIle = connectToLinuxBox.runCommand("date +\"%m-%d-%Y\" -r " + lockFile);
                }catch(Exception e){
                    //do nothing
                    System.out.println("There was exception in getting last mod date of the lock file.");
                }
                result = false;
                message = " \n The lock-file is present since "+lastModDateOflockFIle;
                status = "fail";
            }
            else{
                result= true;
                message = " \n No lock-file found";
                status = "pass";
            }
            //NOW I NEED TO APPEND THE LOCK FILE CHECK STATUS TO THE TABLE SO I AM GOING TO APPEND TO THE MESSAGE
            if(status.equals("fail")) {
                String query = "Update " + ObjectRepository.Export_test_table + " set Message =concat( Message ,'" + message + "') where Export_Region = '" +
                        env + "' and Export_Folder_(Windows) like '%" +GlobalVariables.conbld_serverName+": "+ObjectRepository.editorialSource_FolderPath+ "%'";
                System.out.println("FAIL TEST: LOCK FILE "+query);
                UpdateQuery.SQLUpDateQuery(query);
            }
            return result;
        }
    private  void setExportFolderLocation (String folderName, String type) throws Exception {

        String status="";
        String message="";
        String sequence ="0";
        Connection mySql =  ConnectToMySQLTestDB.getInsstance().getMySqlConnection();
        Statement stmt = mySql.createStatement();
        log.clear();



        long diff , diffHours, diffMinutes;
        try {
             diff = getExportDuration(type);
             diffMinutes = diff / (60 * 1000) % 60;
             diffHours = diff / (60 * 60 * 1000);
        }
        catch(NullPointerException e){
            diffHours = 0;
            diffMinutes = 0;
        }

        if(result)
            status = "pass";
        else
            status = "fail";

        sequence = getSquenceNumberForExportTable(env, type);
        String mySqlQuery =  "delete from "+ ObjectRepository.Export_test_table +" where SequenceNo = '"+
                sequence+"'";
        System.out.println(mySqlQuery);
        try {
            stmt.executeUpdate(mySqlQuery);
        }catch (NullPointerException e){

        }


        folderName = folderName.replace("\\", "\\\\"); // Now replace single slash with double so that when inserting into DB  - mySQL does not trim it.

        log.put("SequenceNo",sequence); // ADD THE SEQUENCE NUMBER
        log.put("export_region",env);
        log.put("export_type",type);
        log.put("Export_Folder_(Windows)",folderName.trim());
        log.put("run_duration",diffHours + "hrs & " + diffMinutes +"min");
        log.put("Server","");
        //log.put("Drive",exportDriveName);
        log.put("Manifest_id",manifestID);
        log.put("Message", message);
        log.put("status",status);
        SaveTheDataforHashmap.run(ObjectRepository.Export_test_table ,log);
        mySql.close();
    }



    private  void deleteExportFolderLocation (String type) throws Exception {

        Connection mySql = ConnectToMySQLTestDB.getInsstance().getMySqlConnection();
        Statement stmt = mySql.createStatement();

        String mySqlQuery = "delete from "+ ObjectRepository.Export_test_table +" where export_region like '%" +
                env + "%' and  SequenceNo <> 'O15'  and export_type = '" + type + "'";
        System.out.println(mySqlQuery);
        stmt.executeUpdate(mySqlQuery);
        mySql.close();
    }

    private long getExportDuration(String exportType) throws ParseException {

        Date start = null;
        Date stop = null;
        switch (exportType){
            case "PR":
                start = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(exportBenginDate_pr);
                stop    =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse((exportEndDate_pr));
                break;
            case "Nightly":
                start = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(exportBenginDate);
                stop    =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse((exportEndDate));
                break;
        }

        long diff = stop.getTime() - start.getTime();

        return diff;
    }

    private String getExportDrive(){
                return exportDrive.toUpperCase();
    }
    
//verify the UptoDate all_and_PR Site
    public boolean verifyWebPage(String env,String exportType) throws Exception{
    	String webPageMsg;
    	String exportRegionUpdate=null;
    	url = getEnvironmentSite(env,exportType);
        String status = null;
        //make xpath what needs to be verified
        String query2 = "Select Manifest_id from "+ ObjectRepository.Export_test_table +
                " Where Export_Region = '"+env +"' and Export_Type = '"+ exportType+"'";
        String manifest =  GetSelectQueryResultSet.getSingleColumnValue(query2,"MySQL");
        manifest = manifest+".0";
        String xpath = version_xpath.replace("@var","Version "+manifest);
        System.out.println("XPATH to find= "+xpath);
        boolean res=false;
    	String chromeDriverPath = 	ObjectRepository.chromeDriverPath;
    	System.setProperty("webdriver.chrome.driver", chromeDriverPath);
    	ChromeOptions options = new ChromeOptions();
    	options.addArguments("--headless");
    	options.addArguments("window-size=1920,1080");
    	WebDriver driver = new ChromeDriver(options);
    	driver.get(url);
        try{
            Thread.sleep(3000);
            driver.findElement(By.id("details-button")).click(); //proceed-link
            Thread.sleep(500);
            driver.findElement(By.id("proceed-link")).click(); //
            Thread.sleep(3000);
        }catch (Exception e){
            // do nothing
        }

    	try{
    		Thread.sleep(1000);
            res = (driver.findElement(By.xpath(xpath)).isDisplayed());
    		driver.quit();
            webPageMsg = " Yes " ;
    		System.out.println(webPageMsg);
    	}catch ( Exception e){
            webPageMsg = " No ";
    		System.out.println(webPageMsg);
    		res = false;
    	}
        exportRegionUpdate = "<a style=\"color:blue\" href=\""+url+"\">"+env+"-"+exportType+"</a>";
        if(res & result){
            result=true;
            status = "pass";
        }
        else{
            result=false;
            status = "fail";
        }
        driver.quit();
        if(exportType.equalsIgnoreCase("Uptodate") || exportType.equalsIgnoreCase("PR")){
            exportType = "PR";
        }
        SaveTheDataforHashmap.runQuery("Update "+ ObjectRepository.Export_test_table +" set Published = '"+webPageMsg
                + "' where SequenceNo <> 'O15' and export_region = '" + env
                + "' and export_type = '" + exportType + "'",true );
        SaveTheDataforHashmap.runQuery("Update "+ ObjectRepository.Export_test_table +" set status ='"
                + status + "' where SequenceNo <> 'O15' and export_region = '" + env
                + "' and export_type = '" + exportType + "'",true );
        SaveTheDataforHashmap.runQuery("Update "+ ObjectRepository.Export_test_table +" set Export_Region ='"
                + exportRegionUpdate + "' where SequenceNo <> 'O15' and export_region = '" + env
                + "' and export_type = '" + exportType + "'",true );
    	return result;

    }
	/*
    public static String  getXMLvalue() {
    	
		try {

    		File fXML = new File("//edweb01q//utd_export//current_nightly_pr//8757-20190616//version.xml");
    		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    		Document doc = dBuilder.parse(fXML);
    		doc.getDocumentElement().normalize();

    		System.out.println("Root element :" +doc.getDocumentElement().getNodeName());
    		NodeList nList = doc.getElementsByTagName("Version");
    		System.out.println("----------------------");

    		for (int temp = 0; temp < nList.getLength(); temp++) {

    			Node nNode = nList.item(temp);

    			System.out.println("\nCurrent Element :" + nNode.getNodeName());

    			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

    				Element eElement = (Element) nNode;
    				contentVers = eElement.getAttribute("ContentVersion");
    				System.out.println("Content Version is : " + contentVers);

    			}
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
		return contentVers;
    }*/

//Get  environment details from DB --
    public String getEnvironmentSite(String env, String exportType) throws Exception {

    	String resultValue = "";
    	Connection mySql =  ConnectToMySQLTestDB.getInsstance().getMySqlConnection();
    	Statement stmt = mySql.createStatement();

    	String mySqlQuery = "Select Url from "+ ObjectRepository.url_table+
    			     " where Environment = '"+env+"' AND Type = '"+exportType+"'";

    	System.out.println(mySqlQuery);
    	ResultSet res = stmt.executeQuery(mySqlQuery);

    	try {
    		while(res.next()){
    			resultValue = res.getString(1).toString();
    		}
    	} catch (MySQLDataException e) {
    		e.printStackTrace();
    	}
    	catch (NullPointerException err) {
    		System.out.println("No Records obtained for this specific query");
    		err.printStackTrace();
    	}

    	System.out.println(resultValue);
    	return resultValue;
   }

   public String  getSquenceNumberForExportTable(String env, String type){
        env =env.toUpperCase();
        String sequence="0";
        System.out.println("ENV :  TYPE - "+env +" : "+type);
        switch (env){
            case "PRODUCTION":
                if(type.equals("PR"))
                    sequence = "A1";
                else if (type.equals("Nightly"))
                    sequence = "B2";
                else if (type.equals("Sharepoint"))
                    sequence="I9";
                else if (type.equals("conbld03p"))
                    sequence="K11";
                else if (type.equals("WebFarms"))
                    sequence="O15";
                break;
            case "PROD REPORT":
                  if (type.equals("edrpapp01p"))
                    sequence="N14";
                  break;
            case "STAGING":
                if(type.equals("PR"))
                    sequence = "C3";
                else if (type.equals("Nightly"))
                    sequence = "D4";
                else if (type.equals("Sharepoint"))
                    sequence="J10";
                else if (type.equals("conbld0103s"))
                    sequence="L12";
                break;
            case "QA":
                if(type.equals("PR"))
                    sequence = "E5";
                else if (type.equals("Nightly"))
                    sequence = "F6";
                else if (type.equals("conbld01q"))
                    sequence="M13";
                break;
            case "DEV":
                if(type.equals("PR"))
                    sequence = "G7";
                else if (type.equals("Nightly"))
                    sequence = "H8";
                break;
        }
        return sequence;
    }
}
