package com.nedap.university;

/**
 * Created by anne-greeth.vanherwijnen on 16/04/2017.
 */
public class TerminalOutput {

    static void welcomeMSG(){
        System.out.println("Welcome! You can type commands and use them to use file transfer service");
        menuMSG();
    }

    static void menuMSG(){
        System.out.println( "You can type the following commands:" + '\n' +
            "- myFiles :to see your own files" + '\n' +
            "* piFiles :to request the files on the pi" + '\n' +
            "* uploadFile :to upload a file, this will show the files you can choose from to upload" + '\n' +
            "* downloadFile [x] :to download a specific file that is on the pi (x represents the number shown by the command piFiles" + '\n' +
            "- shutdown to close this program" + '\n' +
            "- help :to see all these commands again" + '\n' + '\n' +
            "You can always use the commands marked with the -. The commands marked with the * only once you are connected to the Pi.");//TODO: print that DSN has been resolved
    }

    static void DNSResolved(){
        System.out.println("A connection has been established with the Pi, you can now use the upload, download and file request commands.");
    }

    static void showFiles(String string){ //TODO: Implement that this index can be used to retrieve the file
        String[] allFiles = Utils.splitString(string, " ");
        int index = 0;
        System.out.println("The files are:");
        for (String file : allFiles){
            System.out.println("- " + file + " "  + index);
            index++;
        }
    }


}
