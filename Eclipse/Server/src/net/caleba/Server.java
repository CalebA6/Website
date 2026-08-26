package net.caleba;
import java.io.IOException;
import java.net.ServerSocket;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class Server{

	public Server() throws IOException {
		new SecureServer().start();
		ServerSocket server = new ServerSocket(Default.getPort());
		while(true) {
			Logger logger = new Logger();
			new Connection(server.accept(), logger, false).start();
			logger.logInfo("Page loaded. ");
		}
	}
	
}

class SecureServer extends Thread {
	
	public void run() {
		try {
			SSLServerSocket server = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(443);
			server.setEnabledProtocols(new String[] {"TLSv1.3"});
			server.setEnabledCipherSuites(new String[] {"TLS_AES_128_GCM_SHA256"});
			while(true) {
				Logger logger = new Logger();
				new Connection(server.accept(), logger, true).start();
				logger.logInfo("Page loaded securely. ");
			}
		} catch(Exception e) {
			new Logger().logError("Secure Server Failed: " + e.toString());
			System.exit(1);
		}
	}
	
}