package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Ventriloquate extends Spell
{
	public Spell_Ventriloquate()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Ventriloquate";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(1);

		canAffectCode=0;
		canTargetCode=Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_EXITS|Ability.CAN_ROOMS;

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Ventriloquate();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ILLUSION;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		if(commands.size()<2)
		{
			mob.tell("You must specify who or what to cast this on, and what you want said.");
			return false;
		}
		Environmental target=mob.location().fetchFromRoomFavorItems(null,(String)commands.elementAt(0),Item.WORN_REQ_UNWORNONLY);
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+((String)commands.elementAt(0))+"' here.");
			return false;
		}
		if(target==mob) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,null);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.MSG_SPEAK,"^T<T-NAME> say(s) '"+Util.combine(commands,1)+"'^?.");
			}

		}


		// return whether it worked
		return success;
	}
}