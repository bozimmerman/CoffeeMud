package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_DistantVision extends Spell
{
	public String ID() { return "Spell_DistantVision"; }
	public String name(){return "Distant Vision";}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Spell_DistantVision();	}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_DIVINATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<1)
		{
			mob.tell("Divine a vision of where?");
			return false;
		}
		String areaName=Util.combine(commands,0).trim().toUpperCase();
		Room thisRoom=null;
		for(int m=0;m<CMMap.numRooms();m++)
		{
			Room room=CMMap.getRoom(m);
			if(CoffeeUtensils.containsString(room.displayText().toUpperCase(),areaName))
			{
				thisRoom=room;
				break;
			}
		}

		if(thisRoom==null)
		{
			mob.tell("You can't seem to fixate on a place called '"+Util.combine(commands,0)+"'.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> close(s) <S-HIS-HER> eyes, and invoke(s) a vision.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.tell("\n\r\n\r");
				FullMsg msg2=new FullMsg(mob,thisRoom,Affect.MSG_EXAMINESOMETHING,null);
				thisRoom.affect(msg2);
			}

		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> close(s) <S-HIS-HER> eyes, encanting, but then open(s) them in frustration.");


		// return whether it worked
		return success;
	}
}
