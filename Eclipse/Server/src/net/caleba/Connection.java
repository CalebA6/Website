package net.caleba;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.caleba.nonstatic_pages.NonstaticPage;

public class Connection extends Thread {
	
	private Socket socket;
	private boolean secure;
	Connection(Socket socket, boolean secure) {
		this.socket = socket;
		this.secure = secure;
	}
	
	private static Set<NonstaticPage> nonstaticPages = new HashSet<>();
	static void addNonstaticPage(NonstaticPage addition) {
		nonstaticPages.add(addition);
	}
	
	public void run() {
		try {
			Request request = new Request(socket);
			byte[] response = getResponse(request);
			
			OutputStream output = socket.getOutputStream();
			output.write(response);
			socket.close();
		} catch(Exception e) {
			System.err.println(e.toString());
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
	
	public static byte[] getResponse(Request request) {
		System.out.println(request.getStart());
		String page = request.getPage();
		for(NonstaticPage potential: nonstaticPages) {
			if(potential.checkAddress(page)) {
				return potential.newThread(request, new AccountServices(request));
			}
		}
		try {
			System.err.println("Responding with File: " + page);
			return respondWithFile(page);
		} catch(IOException e) {
			char[] newLineArray = {13, 10};
			String newLine = new String(newLineArray);
			return ("HTTP/1.1 500 EndOfCode" + newLine + newLine + "<html><head><title>ERROR</title></head>"
					+ "<body>Something went wrong. <br><a href=\"" + Default.getAddress() + "\">&#8592;Home</a></body></html>").getBytes();
		}
	}
	
	public static byte[] respondWithFile(String page) throws IOException {
		EasyByteArray output = new EasyByteArray();
		char[] newLineArray = {13, 10};
		String newLine = new String(newLineArray);
		StringBuilder response = new StringBuilder();
		response.append("HTTP/1.1 200 SendingPage" + newLine + newLine);
		File file = new File(Default.getMainDirectory() + "\\" + page);
		boolean isImage = false;
		if(file.exists()) {
			if(file.isDirectory()) {
				String[] files = file.list();
				boolean main = false;
				boolean index = false;
				for(String f: files) {
					if(f.equals("default.html")) {
						main = true;
					}
					if(f.equals("index.html")) {
						index = true;
					}
				}
				if(main) {
					response.append(fileToString(new File(file.getPath() + "\\default.html")));
				} else if(index) {
					response.append(fileToString(new File(file.getPath() + "\\index.html")));
				} else {
					response.append(index(file, page));
				}
			}
			if(file.isFile()) {
				if(page.substring(page.length()-4, page.length()).equals(".png")) {
					isImage = true;
					response = new StringBuilder("HTTP/1.1 200 SendingImage" + newLine + "Cache-Control: max-age=3600" + newLine + "Content-Type: image/png" + newLine);
					String image = fileToString(file);
					response.append("Content-Length: " + image.length() + newLine + newLine);
					//response.append(image);
				}
				else response.append(fileToString(file));
			}
		} else {
			response = new StringBuilder();
			response.append("HTTP/1.1 404 \"" + page + "\" is not a valid page name. " + newLine + newLine);
			response.append("<html><head><title>HTTP/1.1 404 \"" + page + "\" is not a valid page name. </title></head>"
					+ "<body>The page you tried to access does not exist. <br><a href=\"" + Default.getAddress() + "\">&#8592;Home</a></body></html>");
		}
		output.add(response.toString().getBytes());
		if(isImage) {
			Queue<Byte> image = fileToBytes(file);
			for(byte i: image) {
				output.add(i);
			}
		}
		return output.toArray();
	}
	
	private static String fileToString(File file) throws IOException {
		StringBuilder contents = new StringBuilder();
		InputStream reader = new FileInputStream(file);
		String user = null; // Always site to run while account changes are in progress. 
		while(true) {
			int nextByte = reader.read();
			if(nextByte == -1) break;
			if(nextByte == 96 && user != null) {
				contents.append("/account/" + user);
			} else if(nextByte != 96) {
				contents.append((char)(nextByte));
			}
		}
		reader.close();
		return contents.toString();
	}
	
	//Needs Performance Improvements
	private static Queue<Byte> fileToBytes(File file) throws IOException {
		Queue<Byte> contents = new LinkedList<>();
		InputStream reader = new FileInputStream(file);
		while(true) {
			int nextByte = reader.read();
			if(nextByte == -1) break;
			contents.add((byte)nextByte);
		}
		reader.close();
		return contents;
	}
	
	private static String index(File folder, String page) {
		StringBuilder html = new StringBuilder();
		String[] files = folder.list();
		html.append("<html><head><title>Index</title></head><body><h1>Index of: </h1><br><h2>");
		html.append(page + "</h2><ul>");
		for(String file: files) {
			html.append("<li>" + file);
		}
		html.append("</ul></body></html>");
		return html.toString();
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
