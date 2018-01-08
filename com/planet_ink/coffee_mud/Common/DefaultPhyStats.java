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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

/*
   Copyright 2010-2018 Bo Zimmerman

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
public class DefaultPhyStats implements PhyStats
{
	@Override
	public String ID()
	{
		return "DefaultPhyStats";
	}

	@Override
	public String name()
	{
		return ID();
	}

	private final static String[]	empty			= new String[0];
	private final static int[]		DEFAULT_STATS	= {0,0,100,0,0,0,0,0,0,0};
	private final static Comparator<String> ambiComp= new Comparator<String>()
	{
		@Override
		public int compare(String o1, String o2)
		{
			return o1.compareToIgnoreCase(o2);
		}
	};

	protected int[]		stats			= DEFAULT_STATS.clone();
	protected double	speed			= 1.0;			// should be positive
	protected String	replacementName	= null;
	protected String[]	ambiances		= null;

	public DefaultPhyStats()
	{
	}

	@Override
	public void setAllValues(int def)
	{
		for(int i=0;i<NUM_STATS;i++)
			stats[i]=def;
		speed=def;
	}

	@Override
	public void reset()
	{
		for(int i=0;i<DEFAULT_STATS.length;i++)
			stats[i]=DEFAULT_STATS[i];
		speed=1.0;
		replacementName=null;
		ambiances=null;
	}

	@Override
	public int sensesMask()
	{
		return stats[STAT_SENSES];
	}

	@Override
	public int disposition()
	{
		return stats[STAT_DISPOSITION];
	}

	@Override
	public int level()
	{
		return stats[STAT_LEVEL];
	}

	@Override
	public int ability()
	{
		return stats[STAT_ABILITY];
	}

	@Override
	public int rejuv()
	{
		return stats[STAT_REJUV];
	}

	@Override
	public int weight()
	{
		return stats[STAT_WEIGHT];
	}

	@Override
	public int height()
	{
		return stats[STAT_HEIGHT];
	}

	@Override
	public int armor()
	{
		return stats[STAT_ARMOR];
	}

	@Override
	public int damage()
	{
		return stats[STAT_DAMAGE];
	}

	@Override
	public double speed()
	{
		return speed;
	}

	@Override
	public int attackAdjustment()
	{
		return stats[STAT_ATTACK];
	}

	@Override
	public String newName()
	{
		return replacementName;
	}

	@Override
	public String[] ambiances()
	{
		return (ambiances == null) ? empty : ambiances;
	}

	@Override
	public void setRejuv(int newRejuv)
	{
		stats[STAT_REJUV] = newRejuv;
	}

	@Override
	public void setLevel(int newLevel)
	{
		stats[STAT_LEVEL] = newLevel;
	}

	@Override
	public void setArmor(int newArmor)
	{
		stats[STAT_ARMOR] = newArmor;
	}

	@Override
	public void setDamage(int newDamage)
	{
		stats[STAT_DAMAGE] = newDamage;
	}

	@Override
	public void setWeight(int newWeight)
	{
		stats[STAT_WEIGHT] = newWeight;
	}

	@Override
	public void setSpeed(double newSpeed)
	{
		speed = newSpeed;
	}

	@Override
	public void setAttackAdjustment(int newAdjustment)
	{
		stats[STAT_ATTACK] = newAdjustment;
	}

	@Override
	public void setAbility(int newAdjustment)
	{
		stats[STAT_ABILITY] = newAdjustment;
	}

	@Override
	public void setDisposition(int newDisposition)
	{
		stats[STAT_DISPOSITION] = newDisposition;
	}

	@Override
	public void setSensesMask(int newMask)
	{
		stats[STAT_SENSES] = newMask;
	}

	@Override
	public void setHeight(int newHeight)
	{
		stats[STAT_HEIGHT] = newHeight;
	}

	@Override
	public void setName(String newName)
	{
		replacementName = newName;
	}

	@Override
	public String getCombatStats()
	{
		return "L" + stats[STAT_LEVEL] + ":A" + stats[STAT_ARMOR] + ":K" + stats[STAT_ATTACK] + ":D" + stats[STAT_DAMAGE];
	}

	@Override
	public void addAmbiance(String ambiance)
	{
		if(ambiance==null)
			return;
		ambiance=ambiance.trim();
		final String[] ambiances = this.ambiances;
		if(ambiances == null)
		{
			this.ambiances = new String[] { ambiance };
		}
		else
		{
			final String[] newAmbiances = Arrays.copyOf(ambiances, ambiances.length+1); 
			newAmbiances[newAmbiances.length-1]=ambiance;
			Arrays.sort(newAmbiances, ambiComp);
			this.ambiances = newAmbiances;
		}
	}

	@Override
	public void delAmbiance(String ambiance)
	{
		if(ambiance==null)
			return;
		final String[] ambiances = this.ambiances;
		if(ambiances==null)
			return;
		int dex=Arrays.binarySearch(ambiances, ambiance.trim(), ambiComp);
		if(dex<0)
			return;
		if(ambiances.length==1)
		{
			this.ambiances=null;
			return;
		}
		final String[] newAmbiances=Arrays.copyOf(ambiances, ambiances.length-1);
		for(;dex<newAmbiances.length;dex++)
			newAmbiances[dex]=ambiances[dex+1];
		this.ambiances=newAmbiances;
	}

	@Override
	public boolean isAmbiance(String ambiance)
	{
		final String[] ambiances=this.ambiances;
		if((ambiances==null)||(ambiance==null))
			return false;
		return Arrays.binarySearch(ambiances, ambiance.trim(), ambiComp) >=0;
	}

	@Override
	public int movesReqToPull()
	{
		return 1 + (weight() / 3);
	}

	@Override
	public int movesReqToPush()
	{
		return 1 + (weight() / 3);
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
			return new DefaultPhyStats();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public void copyInto(PhyStats intoStats)
	{
		if(intoStats instanceof DefaultPhyStats)
		{
			for(int i=0;i<NUM_STATS;i++)
				((DefaultPhyStats)intoStats).stats[i]=stats[i];
			((DefaultPhyStats)intoStats).speed=speed;
			((DefaultPhyStats)intoStats).ambiances=ambiances;
			((DefaultPhyStats)intoStats).replacementName=replacementName;
		}
		else
		for(int i=0;i<getStatCodes().length;i++)
			intoStats.setStat(getStatCodes()[i],getStat(getStatCodes()[i]));
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final DefaultPhyStats E=(DefaultPhyStats)this.clone();
			E.stats=E.stats.clone();
			return E;
		}
		catch(final java.lang.CloneNotSupportedException e)
		{
			return new DefaultPhyStats();
		}
	}

	private final static String[] CODES=
	{
		"SENSES","DISPOSITION","LEVEL",
		"ABILITY","REJUV","WEIGHT","HEIGHT",
		"ARMOR","DAMAGE","ATTACK", "AMBIANCES"
	};

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
	public boolean sameAs(PhyStats E)
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
			setSensesMask(CMath.s_parseIntExpression(val));
			break;
		case 1:
			setDisposition(CMath.s_parseIntExpression(val));
			break;
		case 2:
			setLevel(CMath.s_parseIntExpression(val));
			break;
		case 3:
			setAbility(CMath.s_parseIntExpression(val));
			break;
		case 4:
			setRejuv(CMath.s_parseIntExpression(val));
			break;
		case 5:
			setWeight(CMath.s_parseIntExpression(val));
			break;
		case 6:
			setHeight(CMath.s_parseIntExpression(val));
			break;
		case 7:
			setArmor(CMath.s_parseIntExpression(val));
			break;
		case 8:
			setDamage(CMath.s_parseIntExpression(val));
			break;
		case 9:
			setAttackAdjustment(CMath.s_parseIntExpression(val));
			break;
		case 10:
		{
			if(val.trim().length()==0)
				ambiances=null;
			else
			{
				ambiances=CMParms.toStringArray(CMParms.parseCommas(val,true));
				Arrays.sort(ambiances);
			}
			break;
		}
		}
	}

	@Override
	public String getStat(String code)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return "" + sensesMask();
		case 1:
			return "" + disposition();
		case 2:
			return "" + level();
		case 3:
			return "" + ability();
		case 4:
			return "" + rejuv();
		case 5:
			return "" + weight();
		case 6:
			return "" + height();
		case 7:
			return "" + armor();
		case 8:
			return "" + damage();
		case 9:
			return "" + attackAdjustment();
		case 10:
			return CMParms.toListString(ambiances());
		default:
			return "";
		}
	}

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}
}
