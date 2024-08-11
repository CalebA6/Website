package net.caleba.nonstatic_pages;

import java.io.IOException;

import net.caleba.AccountServices;
import net.caleba.Connection;
import net.caleba.Default;
import net.caleba.Request;

public class TicTacToe implements NonstaticPage {
	
	public static final String LOCATION = "games/tictactoe";
	
	public boolean checkAddress(String address) {
		if(address.equals(LOCATION)) return true;
		if(address.length() <= LOCATION.length()) return false;
		if(address.substring(0, LOCATION.length()+1).equals(LOCATION + "/")) return true;
		return false;
	}
	
	public byte[] newThread(Request request, AccountServices user) {
		char[] newLineArray = {13, 10};
		String newLine = new String(newLineArray);
		String[] path = request.getPath();
		String board = "";
		if(path.length > 2) board = path[2];
		else {
			try {
				return Connection.respondWithFile("games/tictactoe/default.html");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		boolean mainPage = false;
		boolean play = false;
		if(path.length > 3) {
			if(path[3].equals("board.html")) {
				play = true;
			} else if(path[3].equals("main.html")) mainPage = true;
		} else if(!(board.equals("main.html") || board.equals("board.html"))){
			try {
				return Connection.respondWithFile("games/tictactoe/default.html");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		String addressHead;
		if(user == null) {
			addressHead = Default.getAddress();
		} else {
			addressHead = "/account/" + user + "/";
		}
		
		if(play || board.equals("board.html")) {
			try {
				return ("HTTP/1.1 200 SendingPage" + newLine + newLine + getHTML(board, addressHead)).getBytes();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(mainPage || board.equals("main.html")) {
			try {
				return Connection.respondWithFile("games/tictactoe/main.html");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
//		if(path.length > 2) board = path[2]; else board = "";
//		try {
//			socket.getOutputStream().write(("HTTP/1.1 200 SendingPage" + newLine + newLine + getHTML(board)).getBytes());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return ("HTTP/1.1 500 EndOfCode" + newLine + newLine + "<html><head><title>ERROR</title></head>"
				+ "<body>Something went wrong. <br><a href=\"" + addressHead + "\">&#8592;Home</a></body></html>").getBytes();
	}

	private String getHTML(String board, String homeAddress) throws Exception {
		StringBuilder Html = new StringBuilder();
		Html.append("<html><head><title>Tic Tac Toe</title></head><body>");
		char myLetter = 'X';
		char enemieLetter = 'O';
		boolean win = false;
		/*if(board.length() < 9 || board.length() == 0) {
			throw new Exception("Incompatable page requested: " + board);
		}*/
		if(board.equals("board.html")) {
			Html.append("Are you ready to play Tic Tac Toe? <a href=\"" + homeAddress + LOCATION + "/EEEEEEEEE\" target=\"_top\">Click here. </a>");
		} else {
			if(win(board) != -1) {
				win = true;
			} else {
				//Computer
				int[] plays = {4, 0, 8, 6, 2, 1, 7, 5, 3};
				StringBuilder newBoard = new StringBuilder();
				int change = -1;
				for(int i=0;i<9;i++) {
					if(board.charAt(plays[i]) == 'E') {
						change = plays[i];
						break;
					}
				}
				for(int i=0;i<9;i++) {
					if(i == change) {
						newBoard.append(enemieLetter);
					} else {
						newBoard.append(board.charAt(i));
					}
				}
				board = newBoard.toString();
				if(win(board) != -1) {
					win = true;
				}
			}
			//Player
			Html.append("<table border>");
			for(int i=0;i<3;i++) {
				Html.append("<tr>");
				for(int it=0;it<3;it++) {
					Html.append("<td>");
					int position = i*3 + it;
					char space = board.charAt(position);
					if(space == 'E') {
						if(!win) {
							Html.append("<a href=\"" + homeAddress + LOCATION + "/");
							for(int j=0;j<9;j++) {
								if(j != position) {
									Html.append(board.charAt(j));
								} else {
									Html.append(myLetter);
								}
							}
							Html.append("\" target=\"_top\">");
						}
						Html.append("<font color=\"#555555\" size=\"30\">E</font>");
						if(!win) {
							Html.append("</a>");
						}
					} else {
						Html.append("<font size=\"30\">" + space + "</font>");
					}
					Html.append("</td>");
				}
				Html.append("</tr>");
			}
			Html.append("</table>");
			if(draw(board) && !win) {
				Html.append("<p>There is a draw. <a href=\"" + homeAddress + LOCATION + "/EEEEEEEEE\" target=\"_top\">Play again?</a></p>");
			} else if(win) {
				Html.append("<p>");
				if(win(board) == 0) {
					Html.append("O");
				} else {
					Html.append("X");
				}
				Html.append(" has won! <a href=\"" + homeAddress + LOCATION + "/EEEEEEEEE\" target=\"_top\">Play again?</a></p>");
			}
		}
		Html.append("<br><a href=\"" + homeAddress + "\" target=\"_top\">&#8592;Home</a></body></html></body></html>");
		//System.out.println(Html.toString());
		return Html.toString();
	}

	private byte win(String board) throws Exception {
		char[] p = new char[9];
		for(int i=0;i<9;i++) {
			p[i] = board.charAt(i);
		}
		int[][] wins = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6}};
		for(int i=0;i<8;i++) {
			if(p[wins[i][0]] == p[wins[i][1]] && p[wins[i][1]] == p[wins[i][2]]) {
				switch(p[wins[i][0]]) {
				case 'O': 
					return 0;
				case 'X': 
					return 1;
				}
			}
		}
		return -1;
	}
	
	private boolean draw(String board) throws Exception {
		for(int i=0;i<9;i++) {
			if(board.charAt(i) == 'E') {
				return false;
			}
		}
		return true;
	}
	
}
