package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2010 Bo Zimmerman

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
	public String ID(){	return "Corpse";}
	protected CharStats charStats=null;
	protected String mobName="";
	protected String mobDescription="";
	protected String killerName="";
	protected boolean killerPlayer=false;
	protected String lastMessage="";
	protected Environmental killingTool=null;
	protected boolean destroyAfterLooting=false;
	protected boolean playerCorpse=false;
	protected long timeOfDeath=System.currentTimeMillis();
	protected boolean mobPKFlag=false;
	protected MOB savedMOB=null;
    
	public Corpse()
	{
		super();

		setName("the body of someone");
		setDisplayText("the body of someone lies here.");
		setDescription("Bloody and bruised, obviously mistreated.");
		properWornBitmap=0;
		baseEnvStats.setWeight(150);
		capacity=5;
		baseGoldValue=0;
		recoverEnvStats();
		material=RawMaterial.RESOURCE_MEAT;
	}
	public void setMiscText(String newText)
	{
		miscText="";
		if(newText.length()>0)
			super.setMiscText(newText);
	}

	public CharStats charStats()
	{
		if(charStats==null)
			charStats=(CharStats)CMClass.getCommon("DefaultCharStats");
		return charStats;
	}
	public void setCharStats(CharStats newStats){
		charStats=newStats;
		if(charStats!=null) charStats=(CharStats)charStats.copyOf();
	}
	
	public void setSecretIdentity(String newIdentity)
	{
		if(newIdentity.indexOf("/")>0)
		{
			playerCorpse=false;
			int x=newIdentity.indexOf("/");
			if(x>=0)
			{
				mobName=newIdentity.substring(0,x);
				mobDescription=newIdentity.substring(x+1);
				playerCorpse=true;
			}
		}
		else
			super.setSecretIdentity(newIdentity);
	}
    
    public void destroy()
    {
        super.destroy();
        if(savedMOB!=null)
            savedMOB.destroy();
        savedMOB=null;
    }

	public String mobName(){ return mobName;}
	public void setMobName(String newName){mobName=newName;}
	public String mobDescription(){return mobDescription;}
	public void setMobDescription(String newDescription){mobDescription=newDescription;}
	public boolean mobPKFlag(){return mobPKFlag;}
	public void setMobPKFlag(boolean truefalse){mobPKFlag=truefalse;}
	public String killerName(){return killerName;}
	public void setKillerName(String newName){killerName=newName;}
	public boolean killerPlayer(){return killerPlayer;}
	public void setKillerPlayer(boolean trueFalse){killerPlayer=trueFalse;}
	public boolean playerCorpse(){return playerCorpse;}
	public void setPlayerCorpse(boolean truefalse){playerCorpse=truefalse;}
	public String lastMessage(){return lastMessage;}
	public void setLastMessage(String lastMsg){lastMessage=lastMsg;}
	public Environmental killingTool(){return killingTool;}
	public void setKillingTool(Environmental tool){killingTool=tool;}
	public boolean destroyAfterLooting(){return destroyAfterLooting;}
	public void setDestroyAfterLooting(boolean truefalse){destroyAfterLooting=truefalse;}
	public long timeOfDeath(){return timeOfDeath;}
	public void setTimeOfDeath(long time){timeOfDeath=time;}
    public void setSavedMOB(MOB mob){savedMOB=mob;}
    public MOB savedMOB(){return savedMOB;}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_SIT)
		&&(msg.source().Name().equalsIgnoreCase(mobName()))
		&&(msg.amITarget(this)||(msg.tool()==this))
		&&(CMLib.flags().isGolem(msg.source()))
		&&(msg.source().envStats().height()<0)
		&&(msg.source().envStats().weight()<=0)
        &&(playerCorpse())
        &&(mobName().length()>0))
		{
			CMLib.utensils().resurrect(msg.source(),msg.source().location(),this,-1);
			return;
		}
		if(msg.amITarget(this)&&(msg.targetMinor()==CMMsg.TYP_SNIFF)
        &&((System.currentTimeMillis()-timeOfDeath())>(TimeManager.MILI_HOUR/2)))
		    msg.source().tell(name()+" has definitely started to decay.");
		super.executeMsg(myHost, msg);
		
	}
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((msg.amITarget(this)||(msg.tool()==this))
        &&(playerCorpse())
        &&(mobName().length()>0))
		{
			if((msg.targetMinor()==CMMsg.TYP_SIT)
			&&(msg.source().name().equalsIgnoreCase(mobName()))
			&&(CMLib.flags().isGolem(msg.source()))
			&&(msg.source().envStats().height()<0)
			&&(msg.source().envStats().weight()<=0))
				return true;
			
			if(!super.okMessage(myHost,msg))
				return false;
			
			if(((msg.targetMinor()==CMMsg.TYP_GET)
				||((msg.tool() instanceof Ability)
					&&(!msg.tool().ID().equalsIgnoreCase("Prayer_Resurrect"))
					&&(!msg.tool().ID().equalsIgnoreCase("Prayer_PreserveBody"))
					&&(!msg.tool().ID().equalsIgnoreCase("Song_Rebirth"))))
			&&(CMProps.getVar(CMProps.SYSTEM_CORPSEGUARD).length()>0)
	        &&((msg.targetMessage()==null)||(!msg.targetMessage().equalsIgnoreCase("GIVE"))))
	        {
	            if(CMSecurity.isAllowed(msg.source(),msg.source().location(),"CMDITEMS"))
	                return true;
	            
	            MOB ultimateFollowing=msg.source().amUltimatelyFollowing();
	            if((msg.source().isMonster())
	            &&((ultimateFollowing==null)||(ultimateFollowing.isMonster())))
	                return true;
	            if(CMProps.getVar(CMProps.SYSTEM_CORPSEGUARD).equalsIgnoreCase("ANY"))
	                return true;
	            if (mobName().equalsIgnoreCase(msg.source().Name()))
					return true;
	            else
	            if(CMProps.getVar(CMProps.SYSTEM_CORPSEGUARD).equalsIgnoreCase("SELFONLY"))
				{
	                msg.source().tell("You may not loot another players corpse.");
	                return false;
		        }
				else
	            if(CMProps.getVar(CMProps.SYSTEM_CORPSEGUARD).equalsIgnoreCase("PKONLY"))
				{
	                if(!(CMath.bset((msg.source()).getBitmap(), MOB.ATT_PLAYERKILL)))
					{
	                    msg.source().tell("You can not get that.  You are not a player killer.");
	                    return false;
	                }
					else
					if(mobPKFlag())
					{
	                    msg.source().tell("You can not get that.  "+mobName()+" was not a player killer.");
	                    return false;
	                }
				}
	        }
	        return true;
		}
        return super.okMessage(myHost, msg);
	}
}
