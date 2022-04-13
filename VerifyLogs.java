package com;
import com.mysql.jdbc.exceptions.MySQLDataException;
import getterClasses.GetCalenderDate;
import getterClasses.GetLastModifiedDateOfAFolder;
import getterClasses.GetterClass;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.json.JsonOutput;
import util.*;

import javax.mail.MessagingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import java.util.Calendar;


public class VerifyLogs {


    public static void verifyFileExist(String env) throws MessagingException {

        String returnValue = null;


            if (env.equalsIgnoreCase("DEV")) {
                    returnValue = "edweb01d";
            }
            else if(env.equalsIgnoreCase("QA")) {
                returnValue = "edweb01q";
            }
            else if(env.equalsIgnoreCase("STAGING")) {
                returnValue = "edweb0503s";
            }
            else if(env.equalsIgnoreCase("PRODUCTION")) {
                returnValue = "edweb05p";
            }



            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);

            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String strDate = dateFormat.format(new Date(cal.getTimeInMillis()));
            System.out.println("log date is " +strDate);

            String logName = "Utd.log"+(strDate);
            System.out.println(logName);

            String folder = "//"+returnValue+"//Editorial_Log//"+logName;
            System.out.println(folder);
            File file = new File(folder);
            if(file.exists()){
                System.out.println("UTD Log file is available for " +logName);
            }else
            {
                System.out.println(logName+" log file not available");
                Email.sendEmailTo("dhilip.balasuriyan@wolterskluwer.com");
                assert file.exists();
            }
    }
}
