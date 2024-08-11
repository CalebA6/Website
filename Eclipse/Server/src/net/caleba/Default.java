package net.caleba;
import java.io.File;
import java.io.IOException;

public class Default {
	
	private static int port = 80;
	private static String serverAddress = "http://localhost/";
	private static File mainDirectory = new File("C:\\Users\\Caleb\\OneDrive\\Documents\\Projects\\WebsiteSSL\\www");
	
	public static void main(String[] args) throws IOException {
		//Handles Program Settings
		if(args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		if(args.length > 1) {
			serverAddress = args[1];
		}
		if(args.length > 2) {
			mainDirectory = new File(args[2]);
		}
		//Starts Server
		new Server();
	}
	
	public static int getPort() {
		return port;
	}
	
	public static String getAddress() {
		return serverAddress;
	}
	
	public static File getMainDirectory() {
		return mainDirectory;
	}
	
}