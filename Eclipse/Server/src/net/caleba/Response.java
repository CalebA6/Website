package net.caleba;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.caleba.nonstatic_pages.NonstaticPage;

public class Response {
	
	private final int httpMajorVersion = 1;
	private final int httpMinorVersion = 1;
	private int responseCode;
	private String message;
	private LinkedHashMap<String, String> headers;
	private EasyByteArray page;
	
	private Response(int responseCode, String message) {
		this.responseCode = responseCode;
		this.message = message;
		headers = new LinkedHashMap<>();
		page = new EasyByteArray();
	}
	
	private void addHeader(String header, String value) {
		headers.put(header, value);
	}
	
	private void addContent(String content) {
		page.add(content.getBytes());
	}
	
	private void addContent(byte[] content) {
		page.add(content);
	}
	
	public byte[] getBytes() {
		EasyByteArray response = new EasyByteArray();
		byte[] newLine = {13, 10};
		
		response.add("HTTP/".getBytes());
		response.add(Integer.toString(httpMajorVersion).getBytes());
		response.add(".".getBytes());
		response.add(Integer.toString(httpMinorVersion).getBytes());
		response.add(" ".getBytes());
		response.add(Integer.toString(responseCode).getBytes());
		response.add(" ".getBytes());
		response.add(message.getBytes());
		for(Map.Entry<String, String> header: headers.entrySet()) {
			response.add(newLine);
			response.add(header.getKey().getBytes());
			response.add(": ".getBytes());
			response.add(header.getValue().getBytes());
		}
		response.add(newLine);
		response.add(newLine);
		response.add(page.toArray());
		return response.toArray();
	}
	
	
	private static Set<NonstaticPage> nonstaticPages = new HashSet<>();
	static void addNonstaticPage(NonstaticPage addition) {
		nonstaticPages.add(addition);
	}
	
	public static Response getResponse(Request request) {
		System.out.println(request.getStart());
		String page = request.getPage();
		for(NonstaticPage potential: nonstaticPages) {
			if(potential.checkAddress(page)) {
				return potential.newThread(request);
			}
		}
		try {
			return respondWithFile(page);
		} catch(Exception e) {
			Response response = new Response(500, "Server Error");
			response.addContent("<html><head><title>ERROR</title></head><body>Something went wrong. <br><a href=\"");
			response.addContent(Default.getAddress());
			response.addContent("\">&#8592;Home</a></body></html>");
			return response;
		}
	}
	
	public static Response respondWithFile(String page) throws IOException, ImpossibleException {
		Response response = null;
		File file = new File(Default.getMainDirectory() + "/" + page);
		if(file.exists()) {
			if(file.isDirectory()) {
				response = new Response(200, "Sending Page");
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
					response.addContent(fileToString(new File(file.getPath() + "/default.html")));
				} else if(index) {
					response.addContent(fileToString(new File(file.getPath() + "/index.html")));
				} else {
					response.addContent(index(file, page));
				}
			}
			if(file.isFile()) {
				if(page.substring(page.length()-4, page.length()).equals(".png")) {
					response = new Response(200, "Sending Image");
					response.addHeader("Cache-Control", "max-age=3600");
					response.addHeader("Content-Type", "image/png");
					String image = fileToString(file);
					response.addHeader("Content-Length", Integer.toString(image.length()));
					response.addContent(fileToBytes(file));
				}
				else {
					response = new Response(200, "Sending Page");
					response.addContent(fileToBytes(file));
				}
			}
		} else {
			response = new Response(404, "\"" + page + "\" is not a valid page name. ");
			response.addContent("<html><head><title>HTTP/1.1 404 \"");
			response.addContent(page);
			response.addContent("\" is not a valid page name. </title></head><body>The page you tried to access does not exist. <br><a href=\"");
			response.addContent(Default.getAddress());
			response.addContent("\">&#8592;Home</a></body></html>");
		}
		if(response == null) throw new ImpossibleException();
		return response;
	}
	
	public static String fileToString(File file) throws IOException {
		StringBuilder contents = new StringBuilder();
		InputStream reader = new FileInputStream(file); 
		while(true) {
			int nextByte = reader.read();
			if(nextByte == -1) break;
			else contents.append((char)nextByte);
		}
		reader.close();
		return contents.toString();
	}
	
	public static byte[] fileToBytes(File file) throws IOException {
		EasyByteArray contents = new EasyByteArray();
		InputStream reader = new FileInputStream(file);
		while(true) {
			int nextByte = reader.read();
			if(nextByte == -1) break;
			contents.add((byte)nextByte);
		}
		reader.close();
		return contents.toArray();
	}
	
	public static String index(File folder, String page) {
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

}
