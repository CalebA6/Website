package net.caleba.nonstatic_pages;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.caleba.Connection;
import net.caleba.Default;
import net.caleba.Request;

public class Chat implements NonstaticPage {

	public static final String LOCATION = "chat";
	
	public boolean checkAddress(String address) {
		if(address.equals(LOCATION)) return true;
		if(address.length() <= LOCATION.length()) return false;
		System.out.println(address.substring(0, LOCATION.length()));
		if(address.substring(0, LOCATION.length()+1).equals(LOCATION + "/")) return true;
		return false;
	}

	public byte[] newThread(Request request, String user) {
		try {
			if(request.getMethod().equals("GET")) {
				String[] path = request.getPath();
				if(path.length == 1) {
					return Connection.respondWithFile("chat/default.html", user);
				} else if(page.contains("start.html")) {
					if(user == null) {
						return Connection.respondWithFile("chat/startout.html", null);
					} else {
						return Connection.respondWithFile("chat/startin.html", user);
					}
				}
			} else if(request.getMethod().equals("POST")) {
				InputStream conInput = connection.getInputStream();
				StringBuilder request = new StringBuilder();
				long start = System.nanoTime();
				boolean create = false;
				while(true) {
					int in = conInput.read();
					if(in > -1) {
						request.append((char)in);
					}
					int length = request.length();
					System.out.print(request.charAt(length-1));
					try{
						if(create && request.charAt(length-1) == 10 && request.charAt(length-2) == 13) {
							break;
						}
						if(request.charAt(length-1) == 10 && request.charAt(length-2) == 13 && request.charAt(length-3) == 101 && request.charAt(length-4) == 116 && request.charAt(length-5) == 97 && request.charAt(length-6) == 101 && request.charAt(length-7) == 114 && request.charAt(length-8) == 99) {
							create = true;
						}
					} catch(StringIndexOutOfBoundsException e) {
						
					}
					if(System.nanoTime() - start > 3000000000L) {
						System.err.println("Timed out.");
						break;
					}
				}
				String[] input = request.toString().split("\r\n");
				int lineZero = input.length - 3;
				if(input[lineZero+1] == "create") {
					if(user != null) {
						String session = Database.makeSession(4);
						return ("HTTP/1.1 200 SessionID\r\ncreated\r\n" + session + "/" + Database.setupIndividual(session, user)).getBytes();
					} else {
						String username = input[lineZero+2];
						username = username.replaceAll("<", "&#60;").replaceAll(">", "&#62;");
						if(Account.validUsername(username)) {
							if(Account.accountExists(username)) {
								return ("HTTP/1.1 200 Session\r\nusername unavailable").getBytes();
							} else {
								String session = Database.makeSession(4);
								return ("HTTP/1.1 200 SessionID\r\ncreated\r\n" + session + "/" + Database.setupIndividual(session, username)).getBytes();
							}
						} else {
							return ("HTTP/1.1 200 Session\r\nusername unavailable").getBytes();
						}
					}
				}
				
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return ("HTTP/1.1 500 EndOfCode\r\n\r\n<html><head><title>ERROR</title></head>"
				+ "<body>Something went wrong. <br><a href=\"" + Default.getAddress() + "\">&#8592;Home</a></body></html>").getBytes();
	}
	
}


class Database {
	
	private static List<Character> idHeader = new ArrayList<>();
	private static Map<String, Queue<String>> conversationInputs = new ConcurrentHashMap<>();
	private static Map<String, List<String>> individualIds = new ConcurrentHashMap<>();
	private static Map<String[], String> usernames = new ConcurrentHashMap<>();
	private static Map<String[], Queue<String>> conversationOutputs = new ConcurrentHashMap<>();
	private static Map<String, List<Queue<String>>> conversationOutputLoader = new ConcurrentHashMap<>();
	
	public static String makeSession(int idLength) {
		String id = getIdHeader() + getId(idLength);
		conversationInputs.put(id, new ConcurrentLinkedQueue<String>());
		individualIds.put(id, new ArrayList<String>());
		conversationOutputLoader.put(id, Collections.synchronizedList(new ArrayList<>()));
		return id;
	}
	
	public static String setupIndividual(String sessionID, String username) {
		String individualId = makeIndividualId(sessionID);
		usernames.put(new String[] {sessionID, individualId}, username);
		Queue<String> outputQueue = new ConcurrentLinkedQueue<>();
		conversationOutputs.put(new String[] {sessionID, individualId}, outputQueue);
		conversationOutputLoader.get(sessionID).add(outputQueue);
		return individualId;
	}
	
	private static synchronized String makeIndividualId(String sessionID) {
		Random random = new Random();
		while(true) {
			int id = random.nextInt();
			if(!individualIds.get(sessionID).contains(Integer.toHexString(id))) {
				individualIds.get(sessionID).add(Integer.toHexString(id));
				return Integer.toHexString(id);
			}
		}
	}
	
	private static String getId(int idLength) {
		StringBuilder id = new StringBuilder();
		Random random = new Random();
		for(int i=0;i<idLength;i++) { 
			id.append(random.nextInt(10));
		}
		return id.toString();
		 
	}
	private static synchronized String getIdHeader() {
		boolean finished = false;
		for(int i=0;i<idHeader.size();i++) {
			if(idHeader.get(i) < 90) {
				int value = idHeader.remove(i);
				idHeader.add(i, (char)(value+1));
				finished = true;
				break;
			} else {
				idHeader.remove(i);
				idHeader.add(i, (char)65);
			}
		}
		if(!finished) idHeader.add((char)65);
		StringBuilder idHeaderString = new StringBuilder();
		for(int i=0;i<idHeader.size();i++) {
			idHeaderString.append(idHeader.get(i));
		}
		return idHeaderString.toString();
	}
	
}
