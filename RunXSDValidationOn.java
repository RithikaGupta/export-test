package com;

/**
 * Created by rgupta on 8/21/2017.
 */
public class RunXSDValidationOn {

/*

    public void runTestR() throws IOException, InterruptedException {
        String[] command =
                {
                        "cmd",
                };
         Process p = Runtime.getRuntime().exec(command);
        new Thread(new SyncPipe_CMDLine(p.getErrorStream(), System.err)).start();
        new Thread(new SyncPipe_CMDLine(p.getInputStream(), System.out)).start();
        PrintWriter stdin = new PrintWriter(p.getOutputStream());
        stdin.println("cd c:\\Users\\RGupta\\accurev\\QA_5.0");
        stdin.println("accurev login -H accurev01p:5050 rithika.gupta 1");
        stdin.println("accurev update");
        stdin.println("cd C:\\Users\\RGupta\\accurev\\QA_5.0\\Build\\Deployment\\TestR\\publish\\win7-x64");

        if(GlobalVariables.environment.equals("QA"))
            stdin.println("TestR.exe -testGroup=TEST1-LABi -buildtype=Nightly -newcontent=\""+ GlobalVariables.nightly_exportFolder +
                    "\" -xsd=\"C:\\Users\\RGupta\\accurev\\QA_5.0\\Source\\UpToDate.Editorial.Export\\Resources\" " +
                            "-log=\"C:\\Temp\\Repo\\export-Test\\src\\test\\resources\\testR_Report\\QA\"");

        if(GlobalVariables.environment.equals("STAGING"))
            stdin.println("TestR.exe -testGroup=TEST1-LABi -buildtype=Nightly -newcontent=\""+ GlobalVariables.nightly_exportFolder +
                    "\" -xsd=\"C:\\Users\\RGupta\\accurev\\QA_5.0\\Source\\UpToDate.Editorial.Export\\Resources\" " +
                    "-log=\"C:\\Temp\\Repo\\export-Test\\src\\test\\resources\\testR_Report\\STAGING\"");

        // write any other commands you want here
        stdin.close();
        int returnCode = p.waitFor();
        System.out.println("Return code = " + returnCode);
   }
*/

}
