package com.planet_ink.coffee_mud.Exits;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenExit extends StdExit
{
	protected String name="a walkway";
	protected String description="Looks like an ordinary path from here to there.";
	protected String displayText="";
	protected String closedText="A barrier blocks the way.";

	protected String doorName="door";
	protected String closeName="close";
	protected String openName="open";

	protected boolean hasADoor=false;
	protected boolean doorDefaultsClosed=true;
	protected boolean hasALock=false;
	protected boolean doorDefaultsLocked=false;
	protected boolean isReadable=false;
	protected int openDelayTicks=45;

	public String ID(){	return "GenExit";}
	protected String keyName="";
	public GenExit()
	{
		super();
		name="a walkway";
		description="An ordinary looking way from here to there.";
		displayText="";
		closedText="a closed exit";
		doorName="exit";
		openName="open";
		closeName="close";
		keyName="";
		hasADoor=false;
		isOpen=true;
		hasALock=false;
		isLocked=false;
		doorDefaultsClosed=false;
		doorDefaultsLocked=false;

		openDelayTicks=45;
	}

	public Environmental newInstance()
	{
		return new GenExit();
	}
	public boolean isGeneric(){return true;}
	public String text()
	{
		return Generic.getPropertiesStr(this,false);
	}

	public void setMiscText(String newText)
	{
		miscText="";
		Generic.setPropertiesStr(this,newText,false);
		recoverEnvStats();
		isOpen=!doorDefaultsClosed;
		isLocked=doorDefaultsLocked;
	}

	public String Name(){ return name;}
	public void setName(String newName){name=newName;}
	public String displayText(){ return displayText;}
	public void setDisplayText(String newDisplayText){ displayText=newDisplayText;}
	public String description(){ return description;}
	public void setDescription(String newDescription){ description=newDescription;}
	public boolean hasADoor(){return hasADoor;}
	public boolean hasALock(){return hasALock;}
	public boolean defaultsLocked(){return doorDefaultsLocked;}
	public boolean defaultsClosed(){return doorDefaultsClosed;}
	public void setDoorsNLocks(boolean newHasADoor,
								  boolean newIsOpen,
								  boolean newDefaultsClosed,
								  boolean newHasALock,
								  boolean newIsLocked,
								  boolean newDefaultsLocked)
	{
		isOpen=newIsOpen;
		isLocked=newIsLocked;
		hasADoor=newHasADoor;
		hasALock=newHasALock;
		doorDefaultsClosed=newDefaultsClosed;
		doorDefaultsLocked=newDefaultsLocked;
	}

	public boolean isReadable(){ return isReadable;}

	public String doorName(){return doorName;}
	public String closeWord(){return closeName;}
	public String openWord(){return openName;}
	public String closedText(){return closedText;}
	public void setExitParams(String newDoorName,
							  String newCloseWord,
							  String newOpenWord,
							  String newClosedText)
	{
		doorName=newDoorName;
		closeName=newCloseWord;
		openName=newOpenWord;
		closedText=newClosedText;
	}
	
	public String readableText(){ return (isReadable?keyName:"");}
	public void setReadable(boolean isTrue){isReadable=isTrue;}
	public void setReadableText(String text) { keyName=text; }

	public String keyName()	{ return keyName; }
	public void setKeyName(String newKeyName){keyName=newKeyName;}

	public int openDelayTicks()	{ return openDelayTicks;}
	public void setOpenDelayTicks(int numTicks){openDelayTicks=numTicks;}
}
