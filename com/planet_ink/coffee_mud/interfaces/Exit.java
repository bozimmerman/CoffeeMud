package com.planet_ink.coffee_mud.interfaces;

public interface Exit extends Environmental
{
	public boolean isOpen();
	public boolean isLocked();
	public boolean hasADoor();
	public boolean hasALock();
	public boolean defaultsLocked();
	public boolean defaultsClosed();
	public void setDoorsNLocks(boolean hasADoor,
							   boolean isOpen,
							   boolean defaultsClosed,
							   boolean hasALock,
							   boolean isLocked,
							   boolean defaultsLocked);
	public String keyName();
	public void setKeyName(String keyName);
	 
	public String readableText();
	public boolean isReadable();
	public void setReadable(boolean isTrue);
	public void setReadableText(String text);
	
	public StringBuffer viewableText(MOB mob, Room myRoom);
	
	public String doorName();
	public String closeWord();
	public String openWord();
	public String closedText();
	public void setExitParams(String newDoorName,
							  String newCloseWord,
							  String newOpenWord,
							  String newClosedText);

	
	public int openDelayTicks();
	public void setOpenDelayTicks(int numTicks);
}
