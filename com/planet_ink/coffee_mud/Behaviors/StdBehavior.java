package com.planet_ink.coffee_mud.Behaviors;
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

import java.util.*;

/*
   Copyright 2001-2020 Bo Zimmerman

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
public class StdBehavior implements Behavior
{
	@Override
	public String ID()
	{
		return "StdBehavior";
	}

	@Override
	public String name()
	{
		return ID();
	}

	protected int canImproveCode()
	{
		return Behavior.CAN_MOBS;
	}

	@Override
	public long flags()
	{
		return 0;
	}

	@Override
	public boolean grantsAggressivenessTo(final MOB M)
	{
		return false;
	}

	@Override
	public int getTickStatus()
	{
		return Tickable.STATUS_NOT;
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public String accountForYourself()
	{
		return "";
	}

	public StdBehavior()
	{
		super();
		//CMClass.bumpCounter(this,CMClass.CMObjectType.BEHAVIOR);//removed for perf
	}

	protected String	parms				= "";

	protected boolean	isSavableBehavior	= true;

	/** return a new instance of the object*/
	@Override
	public CMObject newInstance()
	{
		try
		{
			return this.getClass().newInstance();
		}
		catch(final Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdBehavior();
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final Behavior B=(Behavior)this.clone();
			//CMClass.bumpCounter(B,CMClass.CMObjectType.BEHAVIOR);//removed for perf
			B.setParms(getParms());
			return B;
		}
		catch(final CloneNotSupportedException e)
		{
			return new StdBehavior();
		}
	}

	@Override
	public void registerDefaultQuest(final String questName)
	{
	}

	@Override
	public void startBehavior(final PhysicalAgent forMe)
	{
	}

	@Override
	public void endBehavior(final PhysicalAgent forMe)
	{
	}

	/*
	protected void finalize()
	{
		CMClass.unbumpCounter(this, CMClass.CMObjectType.BEHAVIOR);
	}// removed for perf
	*/

	@Override
	public void setSavable(final boolean truefalse)
	{
		isSavableBehavior = truefalse;
	}

	@Override
	public boolean isSavable()
	{
		return isSavableBehavior;
	}

	@Override
	public boolean amDestroyed()
	{
		return false;
	}

	@Override
	public void destroy()
	{
		parms = "";
	}

	protected MOB getBehaversMOB(final Tickable ticking)
	{
		if(ticking==null)
			return null;

		if(ticking instanceof MOB)
			return (MOB)ticking;
		else
		if(ticking instanceof Item)
		{
			if(((Item)ticking).owner() != null)
			{
				if(((Item)ticking).owner() instanceof MOB)
					return (MOB)((Item)ticking).owner();
			}
		}

		return null;
	}

	protected Room getBehaversRoom(final Tickable ticking)
	{
		if(ticking==null)
			return null;

		if(ticking instanceof Room)
			return (Room)ticking;

		final MOB mob=getBehaversMOB(ticking);
		if(mob!=null)
			return mob.location();

		if(ticking instanceof Item)
		{
			if(((Item)ticking).owner() != null)
			{
				if(((Item)ticking).owner() instanceof Room)
					return (Room)((Item)ticking).owner();
			}
		}
		return null;
	}

	@Override
	public String getParms()
	{
		return parms;
	}

	@Override
	public void setParms(final String parameters)
	{
		parms = parameters;
	}

	@Override
	public String parmsFormat()
	{
		return CMParms.FORMAT_UNDEFINED;
	}

	@Override
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public List<String> externalFiles()
	{
		return null;
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		return;
	}

	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		return true;
	}

	@Override
	public boolean canImprove(final int can_code)
	{
		return CMath.bset(canImproveCode(), can_code);
	}

	@Override
	public boolean canImprove(final PhysicalAgent E)
	{
		if((E==null)&&(canImproveCode()==0))
			return true;
		if(E==null)
			return false;
		if((E instanceof MOB)&&((canImproveCode()&Ability.CAN_MOBS)>0))
			return true;
		if((E instanceof Item)&&((canImproveCode()&Ability.CAN_ITEMS)>0))
			return true;
		if((E instanceof Exit)&&((canImproveCode()&Ability.CAN_EXITS)>0))
			return true;
		if((E instanceof Room)&&((canImproveCode()&Ability.CAN_ROOMS)>0))
			return true;
		if((E instanceof Area)&&((canImproveCode()&Ability.CAN_AREAS)>0))
			return true;
		return false;
	}

	protected static String I(final String str)
	{
		return CMLib.lang().commandWordTranslation(str);
	}

	public static boolean canActAtAll(final Tickable affecting)
	{
		return CMLib.flags().canActAtAll(affecting);
	}

	public static boolean canFreelyBehaveNormal(final Tickable affecting)
	{
		return CMLib.flags().canFreelyBehaveNormal(affecting);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((ticking instanceof Environmental) && (((Environmental)ticking).amDestroyed()))
			return false;
		return true;
	}

	/**
	 * Localize an internal string -- shortcut. Same as calling:
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.LanguageLibrary#fullSessionTranslation(String, String...)
	 * Call with the string to translate, which may contain variables of the form @x1, @x2, etc. The array in xs
	 * is then used to replace the variables AFTER the string is translated.
	 * @param str the string to translate
	 * @param xs the array of variables to replace
	 * @return the translated string, with all variables in place
	 */
	public String L(final String str, final String ... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	protected static final String[]	CODES	= { "CLASS", "TEXT" };
	private static final String[]	INTERNAL_CODES	= { "NAME" };

	@Override
	public String[] getStatCodes()
	{
		return CODES;
	}

	protected int getCodeNum(final String code)
	{
		for(int i=0;i<CODES.length;i++)
		{
			if(code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	protected int getInternalCodeNum(final String code)
	{
		for(int i=0;i<INTERNAL_CODES.length;i++)
		{
			if(code.equalsIgnoreCase(INTERNAL_CODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public String getStat(final String code)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return ID();
		case 1:
			return getParms();
		default:
			switch(getInternalCodeNum(code))
			{
			case 0:
				return name();
			}
			break;
		}
		return "";
	}

	@Override
	public void setStat(final String code, final String val)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return;
		case 1:
			setParms(val);
			break;
		default:
			switch(getInternalCodeNum(code))
			{
			case 0:
				break;
			}
			break;
		}
	}

	@Override
	public int getSaveStatIndex()
	{
		return getStatCodes().length;
	}

	@Override
	public boolean isStat(final String code)
	{
		return CMParms.indexOf(getStatCodes(), code.toUpperCase().trim()) >= 0;
	}

	public boolean sameAs(final Behavior E)
	{
		if(!(E instanceof StdBehavior))
			return false;
		for(int i=0;i<CODES.length;i++)
		{
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		}
		return true;
	}
}
