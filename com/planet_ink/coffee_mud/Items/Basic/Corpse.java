package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2024 Bo Zimmerman

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
public class Corpse extends GenContainer implements DeadBody
{
	@Override
	public String ID()
	{
		return "Corpse";
	}

	protected CharStats 	charStats	= null;
	protected String 		mobName		= "";
	protected String 		mobDesc		= "";
	protected String 		killerName	= "";
	protected boolean 		killerPlayer= false;
	protected String 		lastMessage	= "";
	protected Environmental killingTool	= null;
	protected boolean 		playerCorpse= false;
	protected long 			timeOfDeath	= System.currentTimeMillis();
	protected boolean 		mobPKFlag	= false;
	protected MOB 			savedMOB	= null;
	protected boolean		saveSavedMOB= false;
	protected int 			deadMobHash = 0;
	protected boolean 		lootDestroy = false;

	public Corpse()
	{
		super();

		setName("the body of someone");
		setDisplayText("the body of someone lies here.");
		setDescription("Bloody and bruised, obviously mistreated.");
		properWornBitmap=0;
		basePhyStats.setWeight(150);
		capacity=5;
		baseGoldValue=1;
		recoverPhyStats();
		material=RawMaterial.RESOURCE_MEAT;
	}

	@Override
	public String genericName()
	{
		if(CMLib.english().startsWithAnIndefiniteArticle(name())&&(CMStrings.numWords(name())<4))
			return CMStrings.removeColors(name());
		return L("a corpse");
	}

	@Override
	public void setMiscText(final String newText)
	{
		miscText="";
		if(newText.length()>0)
			super.setMiscText(newText);
	}

	@Override
	public CharStats charStats()
	{
		if(charStats==null)
			charStats=(CharStats)CMClass.getCommon("DefaultCharStats");
		return charStats;
	}

	@Override
	public void setCharStats(final CharStats newStats)
	{
		charStats=newStats;
		if(charStats!=null)
			charStats=(CharStats)charStats.copyOf();
	}

	@Override
	public void setSecretIdentity(final String newIdentity)
	{
		if(newIdentity.indexOf('/')>0)
		{
			playerCorpse=false;
			final int x=newIdentity.indexOf('/');
			if(x>=0)
			{
				mobName=newIdentity.substring(0,x);
				mobDesc=newIdentity.substring(x+1);
				playerCorpse=true;
			}
		}
		else
			super.setSecretIdentity(newIdentity);
	}

	@Override
	public void destroy()
	{
		super.destroy();
		if((savedMOB!=null)
		&&(!saveSavedMOB)
		&&((!savedMOB.isPlayer())
			||(CMLib.players().getPlayerAllHosts(savedMOB.Name())!=savedMOB)))
				savedMOB.destroy();
		savedMOB=null;
	}

	@Override
	public String getMobName()
	{
		return mobName;
	}

	@Override
	public void setMobName(final String newName)
	{
		mobName=newName;
	}

	@Override
	public String getMobDescription()
	{
		return mobDesc;
	}

	@Override
	public void setMobDescription(final String newDescription)
	{
		mobDesc=newDescription;
	}

	@Override
	public boolean getMobPKFlag()
	{
		return mobPKFlag;
	}

	@Override
	public void setMobPKFlag(final boolean truefalse)
	{
		mobPKFlag=truefalse;
	}

	@Override
	public int getMobHash()
	{
		return deadMobHash;
	}

	@Override
	public void setMobHash(final int newHash)
	{
		deadMobHash = newHash;
	}

	@Override
	public String getKillerName()
	{
		return killerName;
	}

	@Override
	public void setKillerName(final String newName)
	{
		killerName=newName;
	}

	@Override
	public boolean isKillerPlayer()
	{
		return killerPlayer;
	}

	@Override
	public void setIsKillerPlayer(final boolean trueFalse)
	{
		killerPlayer=trueFalse;
	}

	@Override
	public boolean isPlayerCorpse()
	{
		return playerCorpse;
	}

	@Override
	public void setIsPlayerCorpse(final boolean truefalse)
	{
		playerCorpse=truefalse;
	}

	@Override
	public String getLastMessage()
	{
		return lastMessage;
	}

	@Override
	public void setLastMessage(final String lastMsg)
	{
		lastMessage=lastMsg;
	}

	@Override
	public Environmental getKillerTool()
	{
		return killingTool;
	}

	@Override
	public void setKillerTool(final Environmental tool)
	{
		killingTool=tool;
	}

	@Override
	public boolean isDestroyedAfterLooting()
	{
		return lootDestroy;
	}

	@Override
	public void setIsDestroyAfterLooting(final boolean truefalse)
	{
		lootDestroy=truefalse;
	}

	@Override
	public long getTimeOfDeath()
	{
		return timeOfDeath;
	}

	@Override
	public void setTimeOfDeath(final long time)
	{
		timeOfDeath=time;
	}

	@Override
	public void setSavedMOB(final MOB mob, final boolean preserve)
	{
		savedMOB=mob;
		saveSavedMOB=preserve;
	}

	@Override
	public MOB getSavedMOB()
	{
		return savedMOB;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_SIT)
		&&(msg.source().Name().equalsIgnoreCase(getMobName()))
		&&(msg.amITarget(this)||(msg.tool()==this))
		&&(CMLib.flags().isGolem(msg.source()))
		&&(msg.source().phyStats().height()<0)
		&&(msg.source().phyStats().weight()<=0)
		&&(isPlayerCorpse())
		&&(getMobName().length()>0))
		{
			CMLib.utensils().resurrect(msg.source(),msg.source().location(),this,-1);
			return;
		}
		if(msg.amITarget(this)&&(msg.targetMinor()==CMMsg.TYP_SNIFF)
		&&((System.currentTimeMillis()-getTimeOfDeath())>(TimeManager.MILI_HOUR/2)))
			msg.source().tell(L("@x1 has definitely started to decay.",name()));
		super.executeMsg(myHost, msg);

	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.amITarget(this)||(msg.tool()==this))
		&&(isPlayerCorpse())
		&&(getMobName().length()>0))
		{
			if((msg.targetMinor()==CMMsg.TYP_SIT)
			&&(msg.source().name().equalsIgnoreCase(getMobName()))
			&&(CMLib.flags().isGolem(msg.source()))
			&&(msg.source().phyStats().height()<0)
			&&(msg.source().phyStats().weight()<=0))
				return true;

			if(!super.okMessage(myHost,msg))
				return false;

			if(((msg.targetMinor()==CMMsg.TYP_GET)
				||((msg.tool() instanceof Ability)
					&&(!msg.tool().ID().equalsIgnoreCase("Prayer_Resurrect"))
					&&(!msg.tool().ID().equalsIgnoreCase("Prayer_PreserveBody"))
					&&(!msg.tool().ID().equalsIgnoreCase("Song_Rebirth"))))
			&&(!msg.targetMajor(CMMsg.MASK_INTERMSG)))
			{
				final String guardPolicy=CMProps.getVar(CMProps.Str.CORPSEGUARD).toUpperCase().trim();
				if(guardPolicy.length()>0)
				{
					if(CMSecurity.isAllowed(msg.source(),msg.source().location(),CMSecurity.SecFlag.CMDITEMS))
						return true;

					if(msg.source().getGroupLeader().isMonster())
						return true;
					if(guardPolicy.equals("ANY"))
						return true;
					if (getMobName().equalsIgnoreCase(msg.source().Name()))
						return true;
					else
					if(guardPolicy.equals("SELFONLY"))
					{
						msg.source().tell(L("You may not loot another players corpse."));
						return false;
					}
					else
					if(guardPolicy.equals("PKONLY"))
					{
						if(!((msg.source()).isAttributeSet(MOB.Attrib.PLAYERKILL)))
						{
							msg.source().tell(L("You can not get that.  You are not a player killer."));
							return false;
						}
						else
						if(!getMobPKFlag())
						{
							msg.source().tell(L("You can not get that.  @x1 was not a player killer.",getMobName()));
							return false;
						}
					}
				}
			}
			return true;
		}
		return super.okMessage(myHost, msg);
	}

	private final static String[] MYCODES;
	static {
		MYCODES= new String[6 + GenericBuilder.GenMOBCode.values().length];
		MYCODES[0] = "KILLERNAME";
		MYCODES[1] = "KILLERPLAYER";
		MYCODES[2] = "KILLLASTMESSAGE";
		MYCODES[3] = "KILLTOOLNAME";
		MYCODES[4] = "MOBPLAYER";
		MYCODES[5] = "MOBPK";
		for (int g=0;g<GenericBuilder.GenMOBCode.values().length;g++)
			MYCODES[6+g] = "MOB" + GenericBuilder.GenMOBCode.values()[g].name();
	}

	private int getInternalCodeNum(final String code)
	{
		for(int i=0;i<MYCODES.length;i++)
		{
			if(code.equalsIgnoreCase(MYCODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public String getStat(final String code)
	{
		final int internalNum = getInternalCodeNum(code);
		if(internalNum<0)
			return super.getStat(code);
		else
		switch(internalNum)
		{
		case 0: return killerName != null ? killerName : "";
		case 1: return Boolean.toString(this.killerPlayer);
		case 2: return lastMessage != null ? lastMessage : "";
		case 3: return (this.killingTool != null) ? this.killingTool.Name() : "";
		case 4: return Boolean.toString(this.playerCorpse);
		case 5: return Boolean.toString(this.mobPKFlag);
		default:
		{
			String ucode = code.toUpperCase().trim();
			if(ucode.startsWith("MOB") && (ucode.length()>3))
			{
				ucode = ucode.substring(3);
				final GenericBuilder.GenMOBCode cd = (GenericBuilder.GenMOBCode)CMath.s_valueOf(GenericBuilder.GenMOBCode.class, code.toUpperCase().trim());
				if(cd == null)
					return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
				else
				if(this.savedMOB != null)
					return CMLib.coffeeMaker().getGenMobStat(savedMOB, ucode);
				else
					return "";
			}
			else
				return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
		}
	}

	@Override
	public void setStat(final String code, final String val)
	{
		final int internalNum = getInternalCodeNum(code);
		if(internalNum<0)
			super.setStat(code, val);
		else
		switch(internalNum)
		{
		case 0:
			killerName = val;
			break;
		case 1:
			this.killerPlayer = CMath.s_bool(val);
			break;

		case 2:
			this.lastMessage = val;
			break;
		case 3:
			break; // nothing to do -- not gonna construct a new item!
		case 4:
			this.playerCorpse = CMath.s_bool(val);
			break;
		case 5:
			this.mobPKFlag = CMath.s_bool(val);
			break;
		default:
		{
			String ucode = code.toUpperCase().trim();
			if(ucode.startsWith("MOB") && (ucode.length()>3))
			{
				ucode = ucode.substring(3);
				final GenericBuilder.GenMOBCode cd = (GenericBuilder.GenMOBCode)CMath.s_valueOf(GenericBuilder.GenMOBCode.class, code.toUpperCase().trim());
				if(cd == null)
					CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
				else
				if(this.savedMOB != null)
					CMLib.coffeeMaker().setGenMobStat(savedMOB, ucode, val);
			}
			else
				CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
		}
	}

	private static String[] codes=null;

	@Override
	public String[] getStatCodes()
	{
		if(codes==null)
		{
			if(codes!=null)
				return codes;
			final String[] MYCODES=CMProps.getStatCodesList(Corpse.MYCODES,this);
			final String[] superCodes=super.getStatCodes();
			codes=new String[superCodes.length+MYCODES.length];
			int i=0;
			for(;i<superCodes.length;i++)
				codes[i]=superCodes[i];
			for(int x=0;x<MYCODES.length;i++,x++)
				codes[i]=MYCODES[x];
		}
		return codes;
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof Corpse))
			return false;
		for(int i=0;i<getStatCodes().length;i++)
		{
			if(!E.getStat(getStatCodes()[i]).equals(getStat(getStatCodes()[i])))
				return false;
		}
		return true;
	}
}
