package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.Spells.interfaces.*;
import java.util.*;

public class Spell_DistantVision extends Spell
	implements DivinationDevotion
{
	public Spell_DistantVision()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Distant Vision";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(13);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_DistantVision();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<1)
		{
			mob.tell("Divine a vision of where?");
			return false;
		}
		String areaName=Util.combine(commands,0).trim().toUpperCase();
		Room thisRoom=null;
		for(int m=0;m<CMMap.map.size();m++)
		{
			Room room=(Room)CMMap.map.elementAt(m);
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
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"":"<S-NAME> close(s) <S-HIS-HER> eyes, chant(s), and invoke(s) a vision.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.tell("\n\r\n\r");
				thisRoom.look(mob);
			}

		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> close(s) <S-HIS-HER> eyes, chanting, but then open(s) them in frustration.");


		// return whether it worked
		return success;
	}
}
