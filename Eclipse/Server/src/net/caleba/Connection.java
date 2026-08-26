package net.caleba;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Date;

public class Connection extends Thread {
	
	private Socket socket;
	private boolean secure;
	Connection(Socket socket, boolean secure) {
		this.socket = socket;
		this.secure = secure;
	}
	
	public void run() {
		Logger logger = new Logger();
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
	
	private static File logs = new File(Default.getMainDirectory().getParent() + "/log");
	private static DateFormat formatDate = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
	
	// Maybe create a seperate class with a seperate thread and a queue
	private synchronized static void log(byte[] data) throws IOException {    // Maybe only make file selection and creation synchronized
		String time = formatDate.format(new Date()) + " ";
		time = time.replace(':', '.');
//		File timeFolder = new File(logs.getPath() + "/" + time);
//		String logNameRoot = socket.getInetAddress().toString() + ":" + socket.getPort() + " ";
//		if(timeFolder.exists()) {
//			String[] logs = timeFolder.list();
//			for(int i=0; true; ++i) {
//				String potentialLogName = logNameRoot + i + ".txt";
//				boolean unique = true;
//				for(String logName: logs) {                            // Could maybe be improved by finding the largest number instead of checking one by one
//					if(logName.equals(potentialLogName)) {
//						unique = false;
//						break;
//					}
//				}
//				if(unique) {
//					File log = new File(timeFolder.getPath() + "/" + potentialLogName);
//					log.createNewFile();
//					OutputStream logWriter = new FileOutputStream(log);
//					logWriter.write(data);
//					logWriter.close();
//					break;
//				}
//			}
//		} else {
//			timeFolder.mkdir();
//			System.out.println(timeFolder.getPath() + "/" + logNameRoot + "0.txt");
//			File log = new File(timeFolder.getPath() + "/" + logNameRoot + "0.txt");
//			log.createNewFile();
//			OutputStream logWriter = new FileOutputStream(log);
//			logWriter.write(data);
//			logWriter.close();
//		}
		String[] logNames = logs.list();
		for(int i=0; true; ++i) {
			String potentialLogName = time + i + ".txt";
			boolean unique = true;
			for(String logName: logNames) {                            // Could maybe be improved by finding the largest number instead of checking one by one
				if(logName.equals(potentialLogName)) {
					unique = false;
					break;
				}
			}
			if(unique) {
				File log = new File(logs.getPath() + "/" + potentialLogName);
				log.createNewFile();
				OutputStream logWriter = new FileOutputStream(log);
				logWriter.write(data);
				logWriter.close();
				break;
			}
		}
	}
	
}
