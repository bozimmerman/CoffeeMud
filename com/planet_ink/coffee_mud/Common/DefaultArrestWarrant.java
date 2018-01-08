package com.planet_ink.coffee_mud.Common;
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
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

/*
   Copyright 2005-2018 Bo Zimmerman

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
public class DefaultArrestWarrant implements LegalWarrant
{
	@Override
	public String ID()
	{
		return "DefaultArrestWarrant";
	}

	@Override
	public String name()
	{
		return ID();
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return getClass().newInstance();
		}
		catch (final Exception e)
		{
			return new DefaultArrestWarrant();
		}
	}

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			return (DefaultArrestWarrant) this.clone();
		}
		catch (final CloneNotSupportedException e)
		{
			return newInstance();
		}
	}

	private MOB				criminal			= null;
	private MOB				victim				= null;
	private MOB				witness				= null;
	private MOB				arrestingOfficer	= null;
	private Room			jail				= null;
	private Room			releaseRoom			= null;
	private String			crime				= "";
	private final DVector	punishmentParms		= new DVector(2);
	private int				punishment			= -1;
	private int				jailTime			= 0;
	private int				state				= 0;
	private int				offenses			= 0;
	private long			lastOffense			= 0;
	private long			travelAttemptTime	= 0;
	private long			lastStateChangeTime = 0;
	private long			ignoreUntilTime		= 0;
	private String			warnMsg				= null;

	@Override
	public void setArrestingOfficer(Area legalArea, MOB mob)
	{
		if ((arrestingOfficer != null) 
		&& (arrestingOfficer.getStartRoom() != null) 
		&& (arrestingOfficer.location() != null) 
		&& (legalArea != null)
		&& (arrestingOfficer.getStartRoom().getArea() != arrestingOfficer.location().getArea()) 
		&& (!legalArea.inMyMetroArea(arrestingOfficer.location().getArea())))
			CMLib.tracking().wanderAway(arrestingOfficer, true, true);
		
		if ((mob == null) && (arrestingOfficer != null))
			CMLib.tracking().stopTracking(arrestingOfficer);
		arrestingOfficer = mob;
	}

	@Override
	public MOB criminal()
	{
		return criminal;
	}

	@Override
	public MOB victim()
	{
		return victim;
	}

	@Override
	public MOB witness()
	{
		return witness;
	}

	@Override
	public MOB arrestingOfficer()
	{
		return arrestingOfficer;
	}

	@Override
	public Room jail()
	{
		return CMLib.map().getRoom(jail);
	}

	@Override
	public Room releaseRoom()
	{
		return CMLib.map().getRoom(releaseRoom);
	}

	@Override
	public String crime()
	{
		return crime;
	}

	@Override
	public int punishment()
	{
		return punishment;
	}

	@Override
	public String getPunishmentParm(int code)
	{
		final int index = punishmentParms.indexOf(Integer.valueOf(code));
		if (index < 0)
			return "";
		return (String) punishmentParms.elementAt(index, 2);
	}

	@Override
	public void addPunishmentParm(int code, String parm)
	{
		final int index = punishmentParms.indexOf(Integer.valueOf(code));
		if (index >= 0)
			punishmentParms.removeElementAt(index);
		punishmentParms.addElement(Integer.valueOf(code), parm);
	}

	@Override
	public int jailTime()
	{
		return jailTime;
	}

	@Override
	public int state()
	{
		return state;
	}

	@Override
	public int offenses()
	{
		return offenses;
	}

	@Override
	public long lastOffense()
	{
		return lastOffense;
	}

	@Override
	public long travelAttemptTime()
	{
		return travelAttemptTime;
	}

	@Override
	public String warnMsg()
	{
		return warnMsg;
	}

	@Override
	public void setCriminal(MOB mob)
	{
		criminal = mob;
	}

	@Override
	public void setVictim(MOB mob)
	{
		victim = mob;
	}

	@Override
	public void setWitness(MOB mob)
	{
		witness = mob;
	}

	@Override
	public void setJail(Room R)
	{
		jail = R;
	}

	@Override
	public void setReleaseRoom(Room R)
	{
		releaseRoom = R;
	}

	@Override
	public void setCrime(String newcrime)
	{
		crime = newcrime;
	}

	@Override
	public void setPunishment(int code)
	{
		punishment = code;
	}

	@Override
	public void setJailTime(int time)
	{
		jailTime = time;
	}

	@Override
	public void setState(int newstate)
	{
		lastStateChangeTime=System.currentTimeMillis();
		state = newstate;
	}

	@Override
	public long getLastStateChangeTime()
	{
		return lastStateChangeTime;
	}
	
	@Override
	public void setOffenses(int num)
	{
		offenses = num;
	}

	@Override
	public void setLastOffense(long last)
	{
		lastOffense = last;
	}

	@Override
	public void setTravelAttemptTime(long time)
	{
		travelAttemptTime = time;
	}

	@Override
	public void setWarnMsg(String msg)
	{
		warnMsg = msg;
	}

	@Override
	public long getIgnoreUntilTime()
	{
		return ignoreUntilTime;
	}

	@Override
	public void setIgnoreUntilTime(long time)
	{
		this.ignoreUntilTime = time;
	}
}
