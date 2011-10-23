package org.jenkins.plugins.appaloosa;

import java.io.PrintStream;

public class AppaloosaClient {
	final String organisationToken;
	PrintStream logger = System.out;
	
	public AppaloosaClient(String organisationToken) {
		this.organisationToken = organisationToken;
	}

	/**
	 * @param filePath physical path of the file to upload 
	 * 
	 * */
	public void deployFile(String filePath) throws AppaloosaDeployException{
        logger.print("Uploading file to Appaloosa");
        logger.println(filePath);
        
        // Retrieve details from Appaloosa to do the upload

        // Upload the file on Amazon

        // Notify Appaloosa that the file is available

        // Wait for Appaloosa to process the file
        
	}

	public void useLogger(PrintStream logger) {
		this.logger = logger;
	}
	
}
