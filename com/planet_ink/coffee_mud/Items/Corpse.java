package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Corpse extends GenContainer implements DeadBody
{
	public String ID(){	return "Corpse";}
	protected Room roomLocation=null;
	protected CharStats charStats=null;
	protected String mobName="";
	protected String mobDescription="";
	protected String killerName="";
	protected boolean killerPlayer=false;
	protected String lastMessage="";
	protected Environmental killingTool=null;
	protected boolean destroyAfterLooting=false;
	protected boolean playerCorpse=false;
	protected boolean mobPKFlag=false;

	public Corpse()
	{
		super();

		setName("the body of someone");
		setDisplayText("the body of someone lies here.");
		setDescription("Bloody and bruised, obviously mistreated.");
		properWornBitmap=0;
		baseEnvStats.setWeight(150);
		baseEnvStats.setRejuv(100);
		capacity=5;
		baseGoldValue=0;
		recoverEnvStats();
		material=EnvResource.RESOURCE_MEAT;
	}
	public void setMiscText(String newText)
	{
		miscText="";
		if(newText.length()>0)
			super.setMiscText(newText);
	}
	public Environmental newInstance()
	{
		return new Corpse();
	}
	public void startTicker(Room thisRoom)
	{
		roomLocation=thisRoom;
		CMClass.ThreadEngine().startTickDown(this,MudHost.TICK_DEADBODY_DECAY,envStats().rejuv());
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==MudHost.TICK_DEADBODY_DECAY)
		{
			destroy();
			if(owner() instanceof Room)
				roomLocation=(Room)owner();
			if(roomLocation!=null)
				roomLocation.recoverRoomStats();
			return false;
		}
		else
			return super.tick(ticking,tickID);
	}
	public CharStats charStats()
	{
		if(charStats==null)
			charStats=new DefaultCharStats();
		return charStats;
	}
	public void setCharStats(CharStats newStats){charStats=newStats;}

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
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((msg.amITarget(this)||(msg.tool()==this))
        &&((msg.targetMinor()==CMMsg.TYP_GET)
			||((msg.tool() instanceof Ability)
				&&(!msg.tool().ID().equalsIgnoreCase("Prayer_Resurrect"))
				&&(!msg.tool().ID().equalsIgnoreCase("Prayer_PreserveBody"))
				&&(!msg.tool().ID().equalsIgnoreCase("Song_Rebirth"))))
        &&((envStats().ability()>10)||(Sense.isABonusItems(this)))
		&&(CommonStrings.getVar(CommonStrings.SYSTEM_CORPSEGUARD).length()>0)
        &&(playerCorpse())
		&&(mobName().length()>0))
        {
            if(CMSecurity.isAllowed(msg.source(),(Room)myHost,"CMDITEMS"))
                return true;
            if(msg.source().isMonster())
                return true;
            if(CommonStrings.getVar(CommonStrings.SYSTEM_CORPSEGUARD).equalsIgnoreCase("ANY"))
                return true;
            if (mobName().equalsIgnoreCase(msg.source().Name())) 
				return true;
            else 
            if(CommonStrings.getVar(CommonStrings.SYSTEM_CORPSEGUARD).equalsIgnoreCase("SELFONLY"))
			{
                msg.source().tell("You may not loot another players corpse.");
                return false;
	        }
			else
            if(CommonStrings.getVar(CommonStrings.SYSTEM_CORPSEGUARD).equalsIgnoreCase("PKONLY"))
			{
                if(!(Util.bset((msg.source()).getBitmap(), MOB.ATT_PLAYERKILL))) 
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
}
