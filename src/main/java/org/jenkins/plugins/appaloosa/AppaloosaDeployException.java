package org.jenkins.plugins.appaloosa;

public class AppaloosaDeployException extends Exception {
	
	private static final long serialVersionUID = 8058314925397045494L;

	public AppaloosaDeployException() {
		super();
	}

	public AppaloosaDeployException(String message, Throwable t) {
		super(message, t);
	}

	public AppaloosaDeployException(String message) {
		super(message);
	}

	public AppaloosaDeployException(Throwable t) {
		super(t);
	}
}
