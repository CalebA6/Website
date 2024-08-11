package net.caleba.nonstatic_pages;

import net.caleba.AccountServices;
import net.caleba.Request;

public interface NonstaticPage {
	public boolean checkAddress(String address);
	public byte[] newThread(Request request, AccountServices user);
}
