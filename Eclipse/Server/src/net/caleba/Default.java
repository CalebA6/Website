package net.caleba;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class Default {
	
	private static int port = 80;
	private static String serverAddress = "http://localhost/";
	private static File mainDirectory = new File("C:\\Users\\Caleb\\OneDrive\\Documents\\Projects\\WebsiteSSL\\www");
	private static File keyStoreLocation = new File("C:\\Users\\Caleb\\OneDrive\\Documents\\Projects\\WebsiteSSL\\keystore");
	private static String keyStorePassword = null;
	
	public static void main(String[] args) throws IOException, UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
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
		if(args.length > 3) {
			keyStoreLocation = new File(args[3]);
		}
		if(args.length > 4) {
			keyStorePassword = args[4];
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
	
	public static File getKeyStoreLocation() {
		return keyStoreLocation;
	}
	
	public static String getKeyStorePassword() {
		return keyStorePassword;
	}
	
}