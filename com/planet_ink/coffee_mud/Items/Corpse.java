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
        &&(rawSecretIdentity().indexOf("/")>=0))
        {
            if(((MOB)msg.source()).isASysOp((Room)myHost))
                return true;
            if(msg.source().isMonster())
                return true;
            if(CommonStrings.getVar(CommonStrings.SYSTEM_CORPSEGUARD).equalsIgnoreCase("ANY"))
                return true;
            if(CommonStrings.getVar(CommonStrings.SYSTEM_CORPSEGUARD).equalsIgnoreCase("SELFONLY")) 
			{
                if (rawSecretIdentity().startsWith(msg.source().Name()+"/")) 
					return true;
                else 
				{
                    msg.source().tell("Hey - that's not yours!");
                    return false;
                }
	        }
            if(CommonStrings.getVar(CommonStrings.SYSTEM_CORPSEGUARD).equalsIgnoreCase("PLAYERKILL")) 
			{
                if((((rawSecretIdentity().startsWith(msg.source().Name()+"/")))
                ||(Util.bset(envStats().ability(),64))
                    &&(Util.bset(((MOB)msg.source()).getBitmap(), MOB.ATT_PLAYERKILL))))
				{
                    return true;
                }
                else 
                if(!(Util.bset((msg.source()).getBitmap(), MOB.ATT_PLAYERKILL))) 
				{
                    msg.source().tell("You can not get that.  You are not a player killer.");
                    return false;
                }
				else
                if(!Util.bset(envStats().ability(),64))
				{
					int x=rawSecretIdentity().indexOf("/");
                    msg.source().tell("You can not get that.  "+rawSecretIdentity().substring(0,x)+" is not a player killer.");
                    return false;
                }
			}
        }
        return true;
	}
}
