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
			InputStream input = socket.getInputStream();
			OutputStream output = socket.getOutputStream();
			StringBuilder request = new StringBuilder();
			long start = System.nanoTime();
			while(true) {
				int in = input.read();
				if(in > -1) {
					request.append((char)in);
				}
				int length = request.length();
				try{
					if(request.charAt(length-1) == 10) {
						break;
					}
				} catch(StringIndexOutOfBoundsException e) {
					
				}
				if(System.nanoTime() - start > 3000000000L) {
					System.err.println("Timed out.");
					break;
				}
			}
			byte[] response = getResponse(request.toString(), socket, null);
			
//			byte[] requestData = request.toString().getBytes();
//			byte[] logData = new byte[requestData.length + 4 + response.length];
//			for(int i=0; i<requestData.length; ++i) {
//				logData[i] = requestData[i];
//			}
//			logData[requestData.length] = '\r';
//			logData[requestData.length+1] = '\n';
//			logData[requestData.length+2] = '\r';
//			logData[requestData.length+3] = '\n';
//			for(int i=0; i<response.length; ++i) {
//				logData[requestData.length+4+i] = response[i];
//			}
//			log(logData);
			
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
	
	public static byte[] getResponse(String header, Socket connection, String user) {
		char[] newLineArray = {13, 10};
		String newLine = new String(newLineArray);
		String[] headers = header.split(" ");
		System.out.println(header + "   " + user);
		String page = decodeURL(headers[1].substring(1));
		for(NonstaticPage potential: nonstaticPages) {
			if(potential.checkAddress(page)) {
				return potential.newThread(headers[0], page, headers[2], connection, user);
			}
//			System.out.println(page + " " + isStatic);
		}
//		String[] path = page.split("/");
//		for(int i=0;i<path.length;i++) {
//			path[i] = decodeURL(path[i]);
//		}
//		String entrie = "";
//		if(path[0].contains("?")) {
//			entrie = path[0].substring(path[0].indexOf('=')+1, path[0].length());
//			path[0] = path[0].split("\\?")[0];
//		}
		try {
			return respondWithFile(page, user);
		} catch(IOException e) {
			return ("HTTP/1.1 500 EndOfCode" + newLine + newLine + "<html><head><title>ERROR</title></head>"
					+ "<body>Something went wrong. <br><a href=\"" + Default.getAddress() + "\">&#8592;Home</a></body></html>").getBytes();
		}
	}
	
	public static byte[] respondWithFile(String page, String user) throws IOException {
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
					response.append(fileToString(new File(file.getPath() + "\\default.html"), user));
				} else if(index) {
					response.append(fileToString(new File(file.getPath() + "\\index.html"), user));
				} else {
					response.append(index(file, page));
				}
			}
			if(file.isFile()) {
				if(page.substring(page.length()-4, page.length()).equals(".png")) {
					isImage = true;
					response = new StringBuilder("HTTP/1.1 200 SendingImage" + newLine + "Cache-Control: max-age=3600" + newLine + "Content-Type: image/png" + newLine);
					String image = fileToString(file, null);
					response.append("Content-Length: " + image.length() + newLine + newLine);
					//response.append(image);
				}
				else response.append(fileToString(file, user));
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
	
	private static String fileToString(File file, String user) throws IOException {
		StringBuilder contents = new StringBuilder();
		InputStream reader = new FileInputStream(file);
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
	
//	private String decodeURL(String url) {
//		String[][] encodeings = {{"\\+", " "}, {"%24", "$"}, {"%26", "&"}, {"%2B", "+"}, {"%2C", ","}, {"%2F", "/"}, {"%3A", ":"}, 
//				{"%3B", ";"}, {"%3D", "="}, {"%3F", "?"}, {"%40", "@"}, {"%20", " "}, {"%22", "\""}, {"%3C", "<"}, 
//				{"%3E", ">"}, {"%23", "#"}, {"%25", "%"}, {"%7B", "{"}, {"%7D", "}"}, {"%7C", "|"}, {"%7C", "\\"}, 
//				{"%5E", "^"}, {"%7E", "~"}, {"%5E", "["}, {"%5D", "]"}, {"%60", "`"}, {"%21", "."}, {"%29", ")"}, {"%28", "("}};
//		for(String[] encoding: encodeings) {
//			url = url.replaceAll(encoding[0], encoding[1]);
//		}
//		return url;
//	}
	
	private static Map<String, Character> urlEncodings = new HashMap<>();
	
	public static String decodeURL(String url) {
		StringBuilder decoded = new StringBuilder(url.length());
		for(int i=0; i<url.length(); ++i) {
			if((url.charAt(i) == '%') && (i+2 < url.length())) {
				decoded.append(urlEncodings.get(url.substring(i, i+3)));
				i += 2;
			} else {
				decoded.append(url.charAt(i));
			}
		}
		return decoded.toString();
	}
	
	static {
		urlEncodings.put("%20", ' ');
		urlEncodings.put("%21", '!');
		urlEncodings.put("%22", '"');
		urlEncodings.put("%23", '#');
		urlEncodings.put("%24", '$');
		urlEncodings.put("%25", '%');
		urlEncodings.put("%26", '&');
		urlEncodings.put("%27", '\'');
		urlEncodings.put("%28", '(');
		urlEncodings.put("%29", ')');
		urlEncodings.put("%2A", '*');
		urlEncodings.put("%2B", '+');
		urlEncodings.put("%2C", ',');
		urlEncodings.put("%2D", '-');
		urlEncodings.put("%2E", '.');
		urlEncodings.put("%2F", '/');
		urlEncodings.put("%30", '0');
		urlEncodings.put("%31", '1');
		urlEncodings.put("%32", '2');
		urlEncodings.put("%33", '3');
		urlEncodings.put("%34", '4');
		urlEncodings.put("%35", '5');
		urlEncodings.put("%36", '6');
		urlEncodings.put("%37", '7');
		urlEncodings.put("%38", '8');
		urlEncodings.put("%39", '9');
		urlEncodings.put("%3A", ':');
		urlEncodings.put("%3B", ';');
		urlEncodings.put("%3C", '<');
		urlEncodings.put("%3D", '=');
		urlEncodings.put("%3E", '>');
		urlEncodings.put("%3F", '?');
		urlEncodings.put("%40", '@');
		urlEncodings.put("%41", 'A');
		urlEncodings.put("%42", 'B');
		urlEncodings.put("%43", 'C');
		urlEncodings.put("%44", 'D');
		urlEncodings.put("%45", 'E');
		urlEncodings.put("%46", 'F');
		urlEncodings.put("%47", 'G');
		urlEncodings.put("%48", 'H');
		urlEncodings.put("%49", 'I');
		urlEncodings.put("%4A", 'J');
		urlEncodings.put("%4B", 'K');
		urlEncodings.put("%4C", 'L');
		urlEncodings.put("%4D", 'M');
		urlEncodings.put("%4E", 'N');
		urlEncodings.put("%4F", 'O');
		urlEncodings.put("%50", 'P');
		urlEncodings.put("%51", 'Q');
		urlEncodings.put("%52", 'R');
		urlEncodings.put("%53", 'S');
		urlEncodings.put("%54", 'T');
		urlEncodings.put("%55", 'U');
		urlEncodings.put("%56", 'V');
		urlEncodings.put("%57", 'W');
		urlEncodings.put("%58", 'X');
		urlEncodings.put("%59", 'Y');
		urlEncodings.put("%5A", 'Z');
		urlEncodings.put("%5B", '[');
		urlEncodings.put("%5C", '\\');
		urlEncodings.put("%5D", ']');
		urlEncodings.put("%5E", '^');
		urlEncodings.put("%5F", '_');
		urlEncodings.put("%60", '`');
		urlEncodings.put("%61", 'a');
		urlEncodings.put("%62", 'b');
		urlEncodings.put("%63", 'c');
		urlEncodings.put("%64", 'd');
		urlEncodings.put("%65", 'e');
		urlEncodings.put("%66", 'f');
		urlEncodings.put("%67", 'g');
		urlEncodings.put("%68", 'h');
		urlEncodings.put("%69", 'i');
		urlEncodings.put("%6A", 'j');
		urlEncodings.put("%6B", 'k');
		urlEncodings.put("%6C", 'l');
		urlEncodings.put("%6D", 'm');
		urlEncodings.put("%6E", 'n');
		urlEncodings.put("%6F", 'o');
		urlEncodings.put("%70", 'p');
		urlEncodings.put("%71", 'q');
		urlEncodings.put("%72", 'r');
		urlEncodings.put("%73", 's');
		urlEncodings.put("%74", 't');
		urlEncodings.put("%75", 'u');
		urlEncodings.put("%76", 'v');
		urlEncodings.put("%77", 'w');
		urlEncodings.put("%78", 'x');
		urlEncodings.put("%79", 'y');
		urlEncodings.put("%7A", 'z');
		urlEncodings.put("%7B", '{');
		urlEncodings.put("%7C", '|');
		urlEncodings.put("%7D", '}');
		urlEncodings.put("%7E", '~');
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
