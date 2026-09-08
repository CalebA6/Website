package net.caleba;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;

public class Server{

	public Server() throws IOException, UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
		new SecureServer().start();
		ServerSocket server = new ServerSocket(Default.getPort());
		while(true) {
			new Connection(server.accept(), false).start();
		}
	}
	
}

class SecureServer extends Thread {
	
	private SSLServerSocket server;
	
	public SecureServer() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException, KeyManagementException {
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		char[] password = getKeyStorePassword();
		keyStore.load(new FileInputStream(Default.getKeyStoreLocation()), password);
		keyManagerFactory.init(keyStore, password);
		Arrays.fill(password, '\0');
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
		server = (SSLServerSocket) sslContext.getServerSocketFactory().createServerSocket(443);
		server.setEnabledProtocols(new String[] {"TLSv1.3"});
		server.setEnabledCipherSuites(new String[] {"TLS_AES_128_GCM_SHA256"});
	}
	
	public void run() {
		try {
			while(true) {
				new Connection(server.accept(), true).start();
			}
		} catch(Exception e) {
			Logger.logGeneralError("Secure Server Failed: " + e.toString());
			System.exit(1);
		}
	}
	
	private char[] getKeyStorePassword() {
		String password = Default.getKeyStorePassword();
		if(password != null) return password.toCharArray();
		System.out.print("Enter key store password: ");
		return System.console().readPassword();
	}
	
}