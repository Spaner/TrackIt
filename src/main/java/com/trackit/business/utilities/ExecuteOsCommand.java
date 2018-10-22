package com.trackit.business.utilities;

import java.io.*;

public class ExecuteOsCommand {
	
	private boolean silent;
	private String  commandOutput;
	private String  errorMessage;
	
	public ExecuteOsCommand() {
		// TODO Auto-generated constructor stub
		silent = false;
	}
	
	public ExecuteOsCommand( boolean silent) {
		this.silent = silent;
	}
	
	public String execute( String command) {
        String s;
        
        try {
             
        // run the Unix "ps -ef" command
            // using the Runtime exec method:
            Process p = Runtime.getRuntime().exec(command);
             
            BufferedReader stdInput = new BufferedReader(new
                 InputStreamReader(p.getInputStream()));
 
            BufferedReader stdError = new BufferedReader(new
                 InputStreamReader(p.getErrorStream()));
 
            // read the output from the command
        	commandOutput = "";
            while ( (s = stdInput.readLine()) != null )
            	commandOutput += s + '\n';
//            System.out.println( "'" + commandOutput.charAt(0) + "'" + commandOutput.charAt(1));
            if ( ! silent ) {
                System.out.println("Here is the standard output of the command:\n");
                System.out.println( commandOutput);           	
            }
            
            // read any errors from the attempted command
        	errorMessage  = "";
            while ((s = stdError.readLine()) != null) 
            	errorMessage += s + '\n';
            if ( !silent ) {
                System.out.println("Here is the standard error of the command (if any):\n");
                System.out.println(errorMessage);
           }

        }
        catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();            System.exit(-1);
        }
 		return commandOutput;
	}
	
	public String getOutput() {
		return commandOutput;
	}
	
	public String getError() {
		return errorMessage;
	}
}
