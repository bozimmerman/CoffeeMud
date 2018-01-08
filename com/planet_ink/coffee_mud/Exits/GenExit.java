package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.GenContainer;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2018 Bo Zimmerman

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
public class GenExit extends StdExit implements Modifiable
{
	protected String	name				= "a walkway";
	protected String	description			= "Looks like an ordinary path from here to there.";
	protected String	displayText			= "";
	protected String	closedText			= "A barrier blocks the way.";

	protected String	doorName			= "door";
	protected String	closeName			= "close";
	protected String	openName			= "open";

	protected boolean	hasADoor			= false;
	protected boolean	doorDefaultsClosed	= true;
	protected boolean	hasALock			= false;
	protected boolean	doorDefaultsLocked	= false;
	protected boolean	isReadable			= false;
	protected int		openDelayTicks		= 45;

	protected String	keyName				= "";

	@Override
	public String ID()
	{
		return "GenExit";
	}

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

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	@Override
	public String text()
	{
		return CMLib.coffeeMaker().getPropertiesStr(this,false);
	}

	@Override
	public void setMiscText(String newText)
	{
		CMLib.coffeeMaker().setPropertiesStr(this,newText,false);
		recoverPhyStats();
		isOpen=!doorDefaultsClosed;
		isLocked=doorDefaultsLocked;
	}

	@Override
	public String Name()
	{
		return name;
	}

	@Override
	public void setName(String newName)
	{
		name=newName;
	}

	@Override
	public String displayText()
	{
		return displayText;
	}

	@Override
	public void setDisplayText(String newDisplayText)
	{
		displayText=newDisplayText;
	}

	@Override
	public String description()
	{
		return description;
	}

	@Override
	public void setDescription(String newDescription)
	{
		description=newDescription;
	}

	@Override
	public boolean hasADoor()
	{
		return hasADoor;
	}

	@Override
	public boolean hasALock()
	{
		return hasALock;
	}

	@Override
	public boolean defaultsLocked()
	{
		return doorDefaultsLocked;
	}

	@Override
	public boolean defaultsClosed()
	{
		return doorDefaultsClosed;
	}

	@Override
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

	@Override
	public boolean isReadable()
	{
		return isReadable;
	}

	@Override
	public String doorName()
	{
		return doorName;
	}

	@Override
	public String closeWord()
	{
		return closeName;
	}

	@Override
	public String openWord()
	{
		return openName;
	}

	@Override
	public String closedText()
	{
		return closedText;
	}

	@Override
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

	@Override
	public String readableText()
	{
		return (isReadable?keyName:"");
	}

	@Override
	public void setReadable(boolean isTrue)
	{
		isReadable=isTrue;
	}

	@Override
	public void setReadableText(String text)
	{
		keyName=temporaryDoorLink()+text;
	}

	@Override
	public String keyName()
	{
		return keyName;
	}

	@Override
	public void setKeyName(String newKeyName)
	{
		keyName=temporaryDoorLink()+newKeyName;
	}

	@Override
	public int openDelayTicks()
	{
		return openDelayTicks;
	}

	@Override
	public void setOpenDelayTicks(int numTicks)
	{
		openDelayTicks=numTicks;
	}

	@Override
	public String temporaryDoorLink()
	{
		if(keyName.startsWith("{#"))
		{
			final int x=keyName.indexOf("#}");
			if(x>=0)
				return keyName.substring(2,x);
		}
		return "";
	}

	@Override
	public void setTemporaryDoorLink(String link)
	{
		if(link.startsWith("{{#"))
		{
			super.setTemporaryDoorLink(link);
			return;
		}
		if(keyName.startsWith("{#"))
		{
			final int x=keyName.indexOf("#}");
			if(x>=0)
				keyName=keyName.substring(x+2);
		}
		if(link.length()>0)
			keyName="{#"+link+"#}"+keyName;
	}

	@Override
	public int getSaveStatIndex()
	{
		return getStatCodes().length;
	}

	private static final String[] CODES={
		"CLASS", "NAME", "DISPLAY", "DESCRIPTION", "DOOR",
		"LEVEL", "ABILITY", "ISREADABLE", "AFFBEHAV", "DISPOSITION",
		"READABLETEXT", "HASADOOR", "DEFCLOSED", "HASALOCK", "DEFLOCKED",
		"KEYNAME", "RESETTIME","CLOSEWORD","OPENWORD","CLOSEDTEXT"
	};

	@Override
	public String[] getStatCodes()
	{
		return CODES;
	}

	@Override
	public boolean isStat(String code)
	{
		return CMParms.indexOf(getStatCodes(),code.toUpperCase().trim())>=0;
	}

	@Override
	protected int getCodeNum(String code)
	{
		for(int i=0;i<CODES.length;i++)
		{
			if(code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public String getStat(String code)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return ID(); // class
		case 1:
			return name(); // name
		case 2:
			return displayText(); // display
		case 3:
			return description(); // description
		case 4:
			return doorName(); // door
		case 5:
			return "" + basePhyStats().level(); // level
		case 6:
			return "" + basePhyStats().ability(); // ability
		case 7:
			return "" + isReadable(); // isreadable
		case 8:
			return CMLib.coffeeMaker().getExtraEnvPropertiesStr(this); // affbehav
		case 9:
			return "" + basePhyStats().disposition(); // disposition
		case 10:
			return "" + readableText(); // readabletext
		case 11:
			return "" + hasADoor(); // hasadoor
		case 12:
			return "" + defaultsClosed(); // defclosed
		case 13:
			return "" + hasALock(); // hasalock
		case 14:
			return "" + defaultsLocked(); // deflocked
		case 15:
			return "" + keyName(); // keyname
		case 16:
			return "" + openDelayTicks(); // open ticks
		case 17:
			return this.closeWord();
		case 18:
			return this.openWord();
		case 19:
			return this.closedText();
		}
		return "";
	}

	@Override
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return;
		case 1:
			setName(val);
			break; // name
		case 2:
			setDisplayText(val);
			break; // display
		case 3:
			setDescription(val);
			break; // description
		case 4:
			doorName = val;
			break; // door
		case 5:
			basePhyStats().setLevel(CMath.parseIntExpression(val));
			break; // level
		case 6:
			basePhyStats().setAbility(CMath.parseIntExpression(val));
			break; // ability
		case 7:
			setReadable(CMath.s_bool(val));
			break; // isreadable
		case 8:
		{ // affbehav
			delAllEffects(true);
			delAllBehaviors();
			CMLib.coffeeMaker().setExtraEnvProperties(this, CMLib.xml().parseAllXML(val)); // affbehav
			break;
		} // affbehav
		case 9:
		{ // disposition
			if (CMath.isInteger(val) || (val.trim().length() == 0))
				basePhyStats().setDisposition(CMath.s_parseIntExpression(val)); // disposition
			else
			{
				basePhyStats().setDisposition(0);
				final List<String> V = CMParms.parseCommas(val, true);
				for (final Iterator<String> e = V.iterator(); e.hasNext();)
				{
					val = e.next();
					final int dispIndex = CMParms.indexOfIgnoreCase(PhyStats.IS_CODES, val);
					if (dispIndex >= 0)
						basePhyStats().setDisposition(basePhyStats().disposition() | (int) CMath.pow(2, dispIndex));
				}
			}
			break;
		} // disposition
		case 10:
			setReadableText(val);
			break; // readabletext
		case 11:
			hasADoor = CMath.s_bool(val);
			break; // hasadoor
		case 12:
			doorDefaultsClosed = CMath.s_bool(val);
			break; // defclosed
		case 13:
			hasALock = CMath.s_bool(val);
			break; // hasalock
		case 14:
			doorDefaultsLocked = CMath.s_bool(val);
			break; // deflocked
		case 15:
			setKeyName(val);
			break; // keyname
		case 16:
			setOpenDelayTicks(CMath.s_parseIntExpression(val));
			break; // openticks
		case 17:
			this.closeName = val;
			break;
		case 18:
			this.openName = val;
			break;
		case 19:
			this.closedText = val;
			break;
		}
	}

	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenExit))
			return false;
		final String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
		{
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		}
		return true;
	}
}
