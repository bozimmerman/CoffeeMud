package com.planet_ink.coffee_mud.interfaces;

/* 
   Copyright 2000-2004 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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
	public String temporaryDoorLink();
	public void setTemporaryDoorLink(String link);
}
