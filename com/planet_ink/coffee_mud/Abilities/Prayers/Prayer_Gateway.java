package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Gateway extends Prayer
{
	public String ID() { return "Prayer_Gateway"; }
	public String name(){return "Gateway";}
	protected int canTargetCode(){return 0;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY|Ability.FLAG_TRANSPORTING;}

	Room newRoom=null;
	Room oldRoom=null;

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(newRoom!=null)
			{
				newRoom.showHappens(CMMsg.MSG_OK_VISUAL,"The divine gateway closes.");
				newRoom.rawDoors()[Directions.GATE]=null;
				newRoom.rawExits()[Directions.GATE]=null;
			}
			if(oldRoom!=null)
			{
				oldRoom.showHappens(CMMsg.MSG_OK_VISUAL,"The divine gateway closes.");
				oldRoom.rawDoors()[Directions.GATE]=null;
				oldRoom.rawExits()[Directions.GATE]=null;
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((auto||mob.isMonster())&&(commands.size()==0))
			commands.addElement(CMMap.getRandomRoom().displayText());
		if(commands.size()<1)
		{
			mob.tell("Pray for a gateway to where?");
			return false;
		}
		if((mob.location().getRoomInDir(Directions.GATE)!=null)
		||(mob.location().getExitInDir(Directions.GATE)!=null))
		{
			mob.tell("A gateway cannot be created here.");
			return false;
		}
		String areaName=Util.combine(commands,0).trim().toUpperCase();
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if(Sense.canAccess(mob,R))
				if(EnglishParser.containsString(R.displayText(),areaName))
				{
				   newRoom=R;
				   break;
				}
		}

		if(newRoom==null)
		{
			mob.tell("You don't know of an place called '"+Util.combine(commands,0)+"'.");
			return false;
		}

		int profNeg=0;
		for(int i=0;i<newRoom.numInhabitants();i++)
		{
			MOB t=newRoom.fetchInhabitant(i);
			if(t!=null)
			{
				int adjustment=t.envStats().level()-mob.envStats().level();
				if(t.isMonster()) adjustment=adjustment*3;
				profNeg+=adjustment;
			}
		}
		profNeg+=newRoom.numItems()*20;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,-profNeg,auto);

		if((success)
		&&((newRoom.getRoomInDir(Directions.GATE)==null)
		&&(newRoom.getExitInDir(Directions.GATE)==null)))
		{
			FullMsg msg=new FullMsg(mob,mob.location(),this,affectType(auto),"^S<S-NAME> "+prayWord(mob)+" for a blinding, divine gateway here.^?");
			FullMsg msg2=new FullMsg(mob,newRoom,this,affectType(auto),"A blinding, divine gateway appears here.");
			if((mob.location().okMessage(mob,msg))&&(newRoom.okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				newRoom.send(mob,msg2);
				Exit e=CMClass.getExit("GenExit");
				e.setDescription("A divine gateway to somewhere");
				e.setDisplayText("A divine gateway to somewhere");
				e.setDoorsNLocks(false,true,false,false,false,false);
				e.setExitParams("gateway","close","open","closed.");
				e.setName("gateway");
				mob.location().rawDoors()[Directions.GATE]=newRoom;
				newRoom.rawDoors()[Directions.GATE]=mob.location();
				mob.location().rawExits()[Directions.GATE]=e;
				newRoom.rawExits()[Directions.GATE]=e;
				oldRoom=mob.location();
				beneficialAffect(mob,e,5);
			}
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for a gateway, but nothing happens.");


		// return whether it worked
		return success;
	}
}