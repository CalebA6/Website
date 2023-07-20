package net.caleba;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Random;

import javax.swing.Timer;

public class Session implements ActionListener, Comparable<Session> {
	
	private Timer timer = new Timer(1_800_000 /* 30min */, this);
	private Map<String, Session> sessionsSet;
	private String id;
	private Session replacement = null;
	
	public Session(Map<String, Session> sessionsSet) {
		this.sessionsSet = sessionsSet;
		
		boolean created = false;
		StringBuilder sessionID = new StringBuilder();
		Random random = new Random();
		char next;
		while(!created) {
			for(int i=0; i<12; ++i) {
				do {
					next = (char)(random.nextInt(95)+32);
				} while(next == '/' || next == '%' || next == '`' || next == '"' || next == '\'' || next == '\\' || next == '#');
				sessionID.append(next);
			}
			if(addID(sessionID.toString(), false)) {
				created = true;
			}
		}
		
		timer.start();
	}
	
	// Ensures that no two session end up with the same id
	private synchronized boolean addID(String id, boolean change) {
		if(sessionsSet.containsKey(id)) {
			return false;
		} else {
			if(!change) sessionsSet.put(id, this);
			this.id = id;
			return true;
		}
	}

	// Handles session time out
	@Override
	public void actionPerformed(ActionEvent e) {
		sessionsSet.remove(id);
		timer.stop();
		System.out.println("Session timed out");
	}
	
	public String getID() {
		return id;
	}

	@Override
	public int compareTo(Session o) {
		return id.compareTo(o.id);
//		try {
//			return id.compareTo(((Session) o).id);
//		} catch(ClassCastException e) {
//			return id.compareTo((String) o);
//		}
	}
	
	@Override
	public boolean equals(Object o) {
		return id.equals(((Session) o).id);
//		try {
//			return id.equals(((Session) o).id);
//		} catch(ClassCastException e) {
//			return id.equals((String) o);
//		}
	}
	
	// Replaces session and causes current session to expire much sooner
	public String replace() {
		if(replacement == null) {
			replacement = new Session(sessionsSet);
			timer.stop();
			timer.setDelay(7000);
			timer.start();
		}
		return replacement.getID();
	}
	
}
