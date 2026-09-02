package net.caleba;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class Connection extends Thread {
	
	private Socket socket;
	private boolean secure;
	Connection(Socket socket, boolean secure) {
		this.socket = socket;
		this.secure = secure;
	}
	
	public void run() {
		Logger logger = new Logger(socket.getInetAddress(), socket.getPort());
		try {
			Request request;
			Response response;
			try {
				request = new Request(socket, logger);
				response = Response.getResponse(request, logger);
			} catch (IncompleteHeadLineException e) {
				if (e.isTimeout()) {
					response = Response.getTimeoutResponse();
				} else {
					throw e;
				}
			}
			
			OutputStream output = socket.getOutputStream();
			output.write(response.getBytes());
			socket.close();
		} catch(Exception e) {
			logger.logError(e);
			try {
				socket.close();
			} catch (IOException e1) {
				logger.logError(e1);
			}
		}
	}
	
}
