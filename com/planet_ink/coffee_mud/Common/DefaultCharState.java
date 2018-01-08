package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.database.DBInterface;
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
public class DefaultCharState implements CharState
{
	@Override
	public String ID()
	{
		return "DefaultCharState";
	}

	@Override
	public String name()
	{
		return ID();
	}

	protected final static int[]	DEFAULT_STATES	= { 10, 100, 50, 500, 1000, 0, 0 };
	protected int[]					states			= DEFAULT_STATES.clone();
	protected long					fatigue			= 0;

	public DefaultCharState()
	{
		DEFAULT_STATES[STAT_HUNGER]=CMProps.getIntVar(CMProps.Int.HUNGER_FULL);
		DEFAULT_STATES[STAT_THIRST]=CMProps.getIntVar(CMProps.Int.THIRST_FULL);
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
			return new DefaultCharState();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public void setAllValues(int def)
	{
		for(int i=0;i<states.length;i++)
			states[i]=def;
		fatigue=def;
	}

	@Override
	public void reset()
	{
		for(int i=0;i<DEFAULT_STATES.length;i++)
			states[i]=DEFAULT_STATES[i];
		fatigue=0;
	}

	@Override
	public void copyInto(CharState intoState)
	{
		if(intoState instanceof DefaultCharState)
		{
			for(int i=0;i<states.length;i++)
				((DefaultCharState)intoState).states[i]=states[i];
			((DefaultCharState)intoState).fatigue=fatigue;
		}
		else
		for(int i=0;i<getStatCodes().length;i++)
			intoState.setStat(getStatCodes()[i],getStat(getStatCodes()[i]));
	}

	@Override
	public int getHitPoints()
	{
		return states[STAT_HITPOINTS];
	}

	@Override
	public void setHitPoints(int newVal)
	{
		states[STAT_HITPOINTS]=newVal;
	}

	@Override
	public boolean adjHitPoints(int byThisMuch, CharState max)
	{
		states[STAT_HITPOINTS]+=byThisMuch;
		if(states[STAT_HITPOINTS]<1)
		{
			states[STAT_HITPOINTS]=0;
			return false;
		}
		if(states[STAT_HITPOINTS]>max.getHitPoints())
		{
			states[STAT_HITPOINTS]=max.getHitPoints();
			return false;
		}
		return true;
	}

	@Override
	public long getFatigue()
	{
		return fatigue;
	}

	@Override
	public void setFatigue(long newVal)
	{
		fatigue=newVal;
	}

	@Override
	public boolean adjFatigue(final long byThisMuch, final CharState max)
	{
		fatigue+=byThisMuch;
		if(fatigue<1)
		{
			fatigue=0;
			return false;
		}
		return true;
	}

	@Override
	public int getHunger()
	{
		return states[STAT_HUNGER];
	}

	@Override
	public void setHunger(int newVal)
	{
		states[STAT_HUNGER]=newVal;
		if(states[STAT_HUNGER]>0)
			states[STAT_TICKSHUNGRY]=0;
	}

	@Override
	public int adjTicksHungry(boolean bumpUp)
	{
		if(bumpUp)
			states[STAT_TICKSHUNGRY]++;
		return states[STAT_TICKSHUNGRY];
	}

	@Override
	public boolean adjHunger(int byThisMuch, int max)
	{
		if(byThisMuch>0)
		{
			if(states[STAT_HUNGER]==Integer.MAX_VALUE)
				return false;
			byThisMuch=(int)Math.round(CMProps.getIntVarAsPct(CMProps.Int.HUNGER_GAIN_PCT)*byThisMuch);
		}
		else
			byThisMuch=(int)Math.round(CMProps.getIntVarAsPct(CMProps.Int.HUNGER_LOSS_PCT)*byThisMuch);
			
		states[STAT_HUNGER]+=byThisMuch;
		if(states[STAT_HUNGER]<0)
		{
			states[STAT_HUNGER]=0;
			return false;
		}
		if(states[STAT_HUNGER]>0)
			states[STAT_TICKSHUNGRY]=0;
		if(states[STAT_HUNGER]>max)
		{
			states[STAT_HUNGER]=max;
			return false;
		}
		return true;
	}

	@Override
	public int maxHunger(int baseWeight)
	{
		long factor=baseWeight/250;
		if(factor==0)
			factor=1;
		factor*=getHunger();
		if(factor>Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		return (int)factor;
	}

	@Override
	public int getThirst()
	{
		return states[STAT_THIRST];
	}

	@Override
	public void setThirst(int newVal)
	{
		states[STAT_THIRST]=newVal;
		if(states[STAT_THIRST]>0)
			states[STAT_TICKSTHIRSTY]=0;
	}

	@Override
	public int adjTicksThirsty(boolean bumpUp)
	{
		if(bumpUp)
			states[STAT_TICKSTHIRSTY]++;
		return states[STAT_TICKSTHIRSTY];
	}

	@Override
	public boolean adjThirst(int byThisMuch, int max)
	{
		if(byThisMuch>0)
		{
			if(states[STAT_THIRST]==Integer.MAX_VALUE)
				return false;
			byThisMuch=(int)Math.round(CMProps.getIntVarAsPct(CMProps.Int.THIRST_GAIN_PCT)*byThisMuch);
		}
		else
			byThisMuch=(int)Math.round(CMProps.getIntVarAsPct(CMProps.Int.THIRST_LOSS_PCT)*byThisMuch);
		states[STAT_THIRST]+=byThisMuch;
		if(states[STAT_THIRST]<0)
		{
			states[STAT_THIRST]=0;
			return false;
		}
		if(states[STAT_THIRST]>0)
			states[STAT_TICKSTHIRSTY]=0;
		if(states[STAT_THIRST]>max)
		{
			states[STAT_THIRST]=max;
			return false;
		}
		return true;
	}

	@Override
	public int maxThirst(int baseWeight)
	{
		long factor=baseWeight/250;
		if(factor==0)
			factor=1;
		factor*=getThirst();
		if(factor>Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		return (int)factor;
	}

	@Override
	public String getCombatStats()
	{
		return "H"+states[STAT_HITPOINTS]+":M"+states[STAT_MANA]+":V"+states[STAT_MOVE]+":F"+fatigue;
	}

	@Override
	public int getMana()
	{
		return states[STAT_MANA];
	}

	@Override
	public void setMana(int newVal)
	{
		states[STAT_MANA]=newVal;
	}

	@Override
	public boolean adjMana(int byThisMuch, CharState max)
	{
		states[STAT_MANA]+=byThisMuch;
		if(states[STAT_MANA]<0)
		{
			states[STAT_MANA]=0;
			return false;
		}
		if(states[STAT_MANA]>max.getMana())
		{
			states[STAT_MANA]=max.getMana();
			return false;
		}
		return true;
	}

	@Override
	public int getMovement()
	{
		return states[STAT_MOVE];
	}

	@Override
	public void setMovement(int newVal)
	{
		states[STAT_MOVE]=newVal;
	}

	@Override
	public boolean adjMovement(int byThisMuch, CharState max)
	{
		states[STAT_MOVE]+=byThisMuch;
		if(states[STAT_MOVE]<0)
		{
			states[STAT_MOVE]=0;
			return false;
		}
		if(states[STAT_MOVE]>max.getMovement())
		{
			states[STAT_MOVE]=max.getMovement();
			return false;
		}
		return true;
	}

	private final static String[] CODES={
		"HITS","MANA","MOVE",
		"HUNGER","THIRST","FATIGUE"};

	@Override
	public int getSaveStatIndex()
	{
		return getStatCodes().length;
	}

	@Override
	public String[] getStatCodes()
	{
		return CODES;
	}

	@Override
	public boolean isStat(String code)
	{
		return CMParms.indexOf(getStatCodes(), code.toUpperCase().trim()) >= 0;
	}

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
	public boolean sameAs(CharState E)
	{
		for(int i=0;i<CODES.length;i++)
		{
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		}
		return true;
	}

	@Override
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0:
			setHitPoints(CMath.s_parseIntExpression(val));
			break;
		case 1:
			setMana(CMath.s_parseIntExpression(val));
			break;
		case 2:
			setMovement(CMath.s_parseIntExpression(val));
			break;
		case 3:
			setHunger(CMath.s_parseIntExpression(val));
			break;
		case 4:
			setThirst(CMath.s_parseIntExpression(val));
			break;
		case 5:
			setFatigue(CMath.s_parseIntExpression(val));
			break;
		}
	}

	@Override
	public String getStat(String code)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return "" + getHitPoints();
		case 1:
			return "" + getMana();
		case 2:
			return "" + getMovement();
		case 3:
			return "" + getHunger();
		case 4:
			return "" + getThirst();
		case 5:
			return "" + getFatigue();
		default:
			return "";
		}
	}

	// create a new one of these
	@Override
	public CMObject copyOf()
	{
		try
		{
			final DefaultCharState E=(DefaultCharState)this.clone();
			E.states=E.states.clone();
			return E;
		}
		catch(final CloneNotSupportedException e)
		{
			return new DefaultCharState();
		}
	}

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}
}
