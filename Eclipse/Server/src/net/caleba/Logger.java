package net.caleba;

public class Logger {
	
	public void logInfo(String info) {
		System.out.println(info);
	}
	
	public void logError(String error) {
		System.err.println(error);
	}
	
	public void logError(Exception error) {
		error.printStackTrace();
	}

}
