package net.caleba;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
	
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
	
	InputStream input;
	private String start;
	private String headers;
	private String request;
	private List<String> data;
	
	public Request(Socket socket) throws IOException {
		this.input = socket.getInputStream();
		
		start = parseStart();
		headers = parseHeaders();
		request = start + "/r/n" + headers;
		
		data = new ArrayList<>();
	}
	
	private Request(InputStream input, String start, String headers, List<String> data) {
		this.input = input;
		this.start = start;
		this.headers = headers;
		this.request = start + "/r/n" + headers;
		
		this.data = data;
	}
	
	private String parseStart() throws IOException {
		StringBuilder header = new StringBuilder();
		long start = System.nanoTime();
		while(true) {
			int in = input.read();
			if(in > -1) {
				header.append((char)in);
			}
			int length = header.length();
			try{
				if(header.charAt(length-1) == 10) {
					break;
				}
			} catch(StringIndexOutOfBoundsException e) {
				
			}
			if(System.nanoTime() - start > 3000000000L) {
				System.err.println("Timed out.");
				break;
			}
		}
		return header.substring(0, header.length()-2);
	}
	
	private String parseHeaders() throws IOException {
		StringBuilder headers = new StringBuilder();
		long start = System.nanoTime();
		while(true) {
			int in = input.read();
			if(in > -1) {
				headers.append((char)in);
			}
			int length = headers.length();
			try{
				if(headers.charAt(length-1) == 10 && headers.charAt(length-2) == 13 && headers.charAt(length-3) == 10 && headers.charAt(length-4) == 13) {
					break;
				}
			} catch(StringIndexOutOfBoundsException e) { }
			if(System.nanoTime() - start > 3000000000L) {
				System.err.println("Timed out.");
				break;
			}
		}
		return headers.toString();
	}
	
	private void parseDataLine() throws IOException {
		StringBuilder line = new StringBuilder();
		long start = System.nanoTime();
		while(true) {
			int in = input.read();
			if(in > -1) {
				line.append((char)in);
			}
			int length = line.length();
			try {
				if(line.charAt(length-1) == 10 && line.charAt(length-2) == 13) {
					data.add(line.substring(0, length-2));
					break;
				}
			} catch(StringIndexOutOfBoundsException e) { }
			if(System.nanoTime() - start > 3000000000L) {
				System.err.println("Timed out.");
				break;
			}
		}
	}
	
	public String getStart() {
		return start;
	}
	
	public String getMethod() {
		return getStart().split(" ")[0];
	}
	
	public String getPage() {
		return decodeURL(getStart().split(" ")[1].substring(1));
	}
	
	public String getHttpVersion() {
		return getStart().split(" ")[2];
	}
	
	public String[] getPath() {
		return getPage().split("/");
	}
	
	// TODO: exception handling
	public String getDataLine(int line) {
		while(data.size() < line) {
			try {
				parseDataLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return data.get(line);
	}
	
	// TODO: Check if a user is logged in
	public Request getUserlessRequest() {
		String[] path = getPath();
		int accountCharacters = path[0].length() + path[1].length() + path[2].length() + 2;
		StringBuilder subRequest = new StringBuilder();
		subRequest.append(getMethod());
		subRequest.append(" ");
		String subPage = getPage().substring(accountCharacters);
		if(subPage.length() < 1 || subPage.charAt(0) != '/') {
			subRequest.append('/');
		}
		subRequest.append(subPage);
		subRequest.append(" ");
		subRequest.append(getHttpVersion());
		return new Request(input, subRequest.toString(), headers, data);
	}
	
	@Override
	public String toString() {
		return request;
	}

}
