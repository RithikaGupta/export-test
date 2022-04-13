package com;

import getterClasses.GetterClass;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by RGupta on 12/13/2017.
 */
public class ValidateFileOrFolder {

    public static boolean validateExistenceOfFolder(File folderPath, boolean isEmpty) {

        boolean result = false;
        if (isEmpty) {
            if (folderPath.isDirectory()) {
                if (folderPath.list().length > 0) {
                    result = true;
                }
            }
        }
        else {
            result = folderPath.isDirectory();
        }
        return result;
    }

    public static boolean validateExistenceOfFile (File filePath) {

        return filePath.isFile();
    }





    public  static  boolean validateNumberOfFilesOrFoldersInDirectory (File path, int numberOfFileOrFolderExpected){
        File[] allFilesOrFoldersPresent = GetterClass.getListOfSubDirectories(path);
        return allFilesOrFoldersPresent.length == numberOfFileOrFolderExpected;
    }





    /*****
     *  The below method "valaidateAbstractFolderStructure" will validated abstract folder structure ,
     *  i.e. it shoudl contain folders from 00 to 99
    incase any folder not found will be added to list "notFoundDirectories"
     */
     public static boolean valaidateAbstractFolderStructure(File filePath){
        int k=0;
        ArrayList<String> notFoundDirectories = new ArrayList<String>();
       File[] allSubDirectories = filePath.listFiles(new FileFilter() {
           @Override
           public boolean accept(File filePath) {
               return filePath.isDirectory();
           }
       });
     System.out.println(allSubDirectories.length);
       for (int i=0;i<10;i++){
           for (int j=0;j<10;j++){
               String name = Integer.toString(i)+ Integer.toString(j);
               //System.out.println("Expected name "+ name + " and actual name is "+ allSubDirectories[k].getName());
               try{
               if (!allSubDirectories[k].getName().equals(name)){
                   notFoundDirectories.add(name);
               }
               else{
                   k++;
               }
               }catch (ArrayIndexOutOfBoundsException e){
                   notFoundDirectories.add("99");
                   // do nothing
               }


       }
       }
       for (String s : notFoundDirectories)
                System.out.println("\n Did not find Citation ending with "+s);
         return notFoundDirectories.size() == 0;
    }





    /**********
     * Validate XML against XSDs
     */

    public static  boolean  validateXMLSchema(String xsdPath, String xmlPath){

        try{
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(xsdPath));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new File(xmlPath)));
        }
        catch (SAXException | IOException e){
            System.out.println("EXCEPTION AT VALIDATION \n"+ e);
            return  false;
        }
        return  true;

    }



}
