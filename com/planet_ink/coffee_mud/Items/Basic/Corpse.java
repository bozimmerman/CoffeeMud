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
		baseGoldValue=0;
		recoverPhyStats();
		material=RawMaterial.RESOURCE_MEAT;
	}

	@Override
	public void setMiscText(String newText)
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
	public void setCharStats(CharStats newStats)
	{
		charStats=newStats;
		if(charStats!=null)
			charStats=(CharStats)charStats.copyOf();
	}

	@Override
	public void setSecretIdentity(String newIdentity)
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
		if((savedMOB!=null)&&(!saveSavedMOB))
			savedMOB.destroy();
		savedMOB=null;
	}

	@Override
	public String getMobName()
	{
		return mobName;
	}

	@Override
	public void setMobName(String newName)
	{
		mobName=newName;
	}

	@Override
	public String getMobDescription()
	{
		return mobDesc;
	}

	@Override
	public void setMobDescription(String newDescription)
	{
		mobDesc=newDescription;
	}

	@Override
	public boolean getMobPKFlag()
	{
		return mobPKFlag;
	}

	@Override
	public void setMobPKFlag(boolean truefalse)
	{
		mobPKFlag=truefalse;
	}

	@Override
	public int getMobHash()
	{
		return deadMobHash;
	}

	@Override
	public void setMobHash(int newHash)
	{
		deadMobHash = newHash;
	}

	@Override
	public String getKillerName()
	{
		return killerName;
	}

	@Override
	public void setKillerName(String newName)
	{
		killerName=newName;
	}

	@Override
	public boolean isKillerPlayer()
	{
		return killerPlayer;
	}

	@Override
	public void setIsKillerPlayer(boolean trueFalse)
	{
		killerPlayer=trueFalse;
	}

	@Override
	public boolean isPlayerCorpse()
	{
		return playerCorpse;
	}

	@Override
	public void setIsPlayerCorpse(boolean truefalse)
	{
		playerCorpse=truefalse;
	}

	@Override
	public String getLastMessage()
	{
		return lastMessage;
	}

	@Override
	public void setLastMessage(String lastMsg)
	{
		lastMessage=lastMsg;
	}

	@Override
	public Environmental getKillerTool()
	{
		return killingTool;
	}

	@Override
	public void setKillerTool(Environmental tool)
	{
		killingTool=tool;
	}

	@Override
	public boolean isDestroyedAfterLooting()
	{
		return lootDestroy;
	}

	@Override
	public void setIsDestroyAfterLooting(boolean truefalse)
	{
		lootDestroy=truefalse;
	}

	@Override
	public long getTimeOfDeath()
	{
		return timeOfDeath;
	}

	@Override
	public void setTimeOfDeath(long time)
	{
		timeOfDeath=time;
	}

	@Override
	public void setSavedMOB(MOB mob, boolean preserve)
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
			&&(CMProps.getVar(CMProps.Str.CORPSEGUARD).length()>0)
			&&(!msg.targetMajor(CMMsg.MASK_INTERMSG)))
			{
				if(CMSecurity.isAllowed(msg.source(),msg.source().location(),CMSecurity.SecFlag.CMDITEMS))
					return true;

				final MOB ultimateFollowing=msg.source().amUltimatelyFollowing();
				if((msg.source().isMonster())
				&&((ultimateFollowing==null)||(ultimateFollowing.isMonster())))
					return true;
				if(CMProps.getVar(CMProps.Str.CORPSEGUARD).equalsIgnoreCase("ANY"))
					return true;
				if (getMobName().equalsIgnoreCase(msg.source().Name()))
					return true;
				else
				if(CMProps.getVar(CMProps.Str.CORPSEGUARD).equalsIgnoreCase("SELFONLY"))
				{
					msg.source().tell(L("You may not loot another players corpse."));
					return false;
				}
				else
				if(CMProps.getVar(CMProps.Str.CORPSEGUARD).equalsIgnoreCase("PKONLY"))
				{
					if(!((msg.source()).isAttributeSet(MOB.Attrib.PLAYERKILL)))
					{
						msg.source().tell(L("You can not get that.  You are not a player killer."));
						return false;
					}
					else
					if(getMobPKFlag())
					{
						msg.source().tell(L("You can not get that.  @x1 was not a player killer.",getMobName()));
						return false;
					}
				}
			}
			return true;
		}
		return super.okMessage(myHost, msg);
	}
}
