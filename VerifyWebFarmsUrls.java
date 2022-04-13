package com;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import util.GetSelectQueryResultSet;
import util.ObjectRepository;
import util.SaveTheDataforHashmap;
import util.UpdateQuery;

import java.util.HashMap;
import java.util.List;

import static util.ObjectRepository.version_xpath;

public class VerifyWebFarmsUrls {
    public static HashMap<String, String> log = new HashMap<String, String>();
    public static String webPageMsg=null;

    public boolean verifyWebFarmPages(String env, String type) throws Exception{

        String notInSyncWebFarms="";
        String server = "WebFarms";
       // List<String> notInSyncWebFarms =new ArrayList<>();
        String query ="Select Url from "+ObjectRepository.url_table + " Where Environment = '"+
                        env.toLowerCase() + "' and Type = 'RepliwebFarms' ";
        List<String> url = GetSelectQueryResultSet.getColumnValueInAList(query,"MySQL");
        System.out.println("The numbers of server found = "+url.size());
        String status = null;

        boolean res=false;
       /* String query2 = "Select Manifest_id from "+ ObjectRepository.Export_test_table +
                        " Where Export_Region = 'Production' and Export_Type = 'PR'";
        String manifestID =  GetSelectQueryResultSet.getSingleColumnValue(query2,"MySQL");
        manifestID = manifestID+".0";
        String xpath = version_xpath.replace("Version ","Version "+manifestID);*/
        int count =1;
        String xpath = null;
        String manifestID =null;
        PollForExportFolder pollForExportFolder = new PollForExportFolder();
        String sequence = pollForExportFolder.getSquenceNumberForExportTable("PRODUCTION",server);
        String query3 = "Delete from "+ ObjectRepository.Export_test_table +
                " Where SequenceNo = '"+sequence+"'";
        UpdateQuery.SQLUpDateQuery(query3);



        String chromeDriverPath = 	ObjectRepository.chromeDriverPath;
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);

        ChromeOptions options = new ChromeOptions();
        //options.addArguments("--headless");
        options.addArguments("window-size=1920,1080");

        log.put("SequenceNo",sequence);
        WebDriver driver = new ChromeDriver(options);
        for (String page : url){
            driver.get(page);
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
                if(count<2) { // get the manifest id from first URL that is opened.
                    manifestID = driver.findElement(By.id("topicVersionRevision")).getText();
                    System.out.println("Expected manifestID - "  + manifestID);
                    xpath = version_xpath.replace("@var",manifestID);
                    manifestID = manifestID.split("Version ")[1];
                    count++;
                }

                res = (driver.findElement(By.xpath(xpath)).isDisplayed());

                //if(actualManifestID.trim().equals(manifestID)){
                    webPageMsg = "Web farms status: All webfarms servers are in sync.";

                System.out.println(page + "  "+  webPageMsg);
            }catch ( Exception e){
                System.out.println(e);

                webPageMsg = "Web farms status: FAILURE: some webfarms are not in sync for manifest "+manifestID +" See report below.";
                System.out.println(page + webPageMsg);
                res = false;
                notInSyncWebFarms = notInSyncWebFarms + "\n"+page;
                continue;
            }

        }
        log.clear();
        if(res){
            status = "pass";
            log.put("Server","-");
       }
        else {
            status = "fail";
            log.put("Server", notInSyncWebFarms);
        }
            log.put("SequenceNo",sequence); // ADD THE SEQUENCE NUMBER
            log.put("export_region",env);
            log.put("export_type",type);
            log.put("Export_Folder_(Windows)"," ");

            log.put("Publish_Folder_(Linux)","-");
            log.put("Run_Duration","-");
            //log.put("Drive",exportDriveName);
            log.put("Manifest_Id",manifestID);
            log.put("Published", "-");
            log.put("Message", webPageMsg);
            log.put("status",status);
            SaveTheDataforHashmap.run(ObjectRepository.Export_test_table ,log);

        driver.quit();
        return res;

    }

}
