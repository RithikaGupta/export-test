package com;

import com.jcraft.jsch.ChannelSftp;
import getterClasses.GetterClass;
import util.ConnectToLinuxBox;
import util.XMLParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by RGupta on 3/19/2018.
 */
public class MedicalTopic {


    private static String exportPath = null;
    private static  String env = null;

    public static  MedicalTopic  getinstanceOFMedicalTopicOfExportType(String environment,String exportType) throws Exception{
        env = environment;
        exportPath =   GetterClass.getLinuxExportFolderPath(environment, exportType);
        return new MedicalTopic();
    }

    public boolean validateforGraphihcRefrencesPresentInMeta(String exportPath) throws Exception{
        String path =exportPath ;
        ArrayList<String> errorList = new ArrayList<String>();
        ConnectToLinuxBox linuxConnect = ConnectToLinuxBox.connect(env);
        ArrayList<String> listOfGraphicIDs =  linuxConnect.getAllGraphicIDs(path+"graphics");
        ArrayList<String> listOfMedicalSpecialty =  linuxConnect.getListOfAllSpecialitiesPResentInTopic(path+"topics");

        ArrayList<String> values = new ArrayList<String>();
        String topicXML = null;
        int counter =0;
        // Now iterate through each topic to read cml anf validate for graphic reference
        for (String specialty: listOfMedicalSpecialty) {
            if(!specialty.startsWith("DRUG")) {
                ArrayList<String> listOfMedicalFileInSpecialty = linuxConnect.getListOfAllFIlesPresentInTopic(path + "topics/" + specialty);

                for (String fileName : listOfMedicalFileInSpecialty) {

                    if (fileName.contains("_meta")) {
                         topicXML = linuxConnect.getXMls(path+"topics/"+specialty+"/"+fileName);
                        //topicXML = linuxConnect.getXMls(path + "topics/" + "SURG/109437_meta.xml");
                        //System.out.println(topicXML);
                        counter++;
                        values = XMLParser.getListOfTheAttributeValuesInTag(topicXML, "RelatedGraphic", "GraphicId");
                        for (String s : values) {
                            if (!listOfGraphicIDs.contains(s))
                            // do nothing
                            //System.out.println("Graphic ID link  "+s +" Found verfified the expected graphic is present in the build.");
                            {
                                errorList.add(" TEST FAILED : Graphic ID link  " + s + " In Topic " + fileName + " under specailty " + specialty + " NOT FOUND ");
                            } else
                                System.out.println("Graphic ID link  " + s + " Found verfified the expected graphic is present in the build.");
                        }
                    }
                }
            }
        }
        System.out.println("\n\n\nVALIDATED "+counter+" Files : \n");
        for(String s : errorList)
            System.out.println(s);
        ConnectToLinuxBox.disconnect();
        if(errorList.size()>1)
            return false;
        else
            return true;
    }


}
