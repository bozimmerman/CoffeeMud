package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_ArcaneMark extends Spell
{
	public String ID() { return "Spell_ArcaneMark"; }
	public String name(){return "Arcane Mark";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){ return new Spell_ArcaneMark();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ALTERATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<2)
		{
			mob.tell("You must specify what object you want the spell cast on, and the message you wish the object have marked upon it. ");
			return false;
		}
		Environmental target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,((String)commands.elementAt(0)),Item.WORN_REQ_UNWORNONLY);
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+((String)commands.elementAt(0))+"' here.");
			return false;
		}
		if((!(target instanceof Item))||(!target.isGeneric()))
		{
			mob.tell("You can't can't cast this on "+target.name()+".");
			return false;
		}
		String message=Util.combine(commands,1);

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),"^S<S-NAME> invoke(s) a spell upon <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target.description().indexOf("Some markings on it say")>=0)
					target.setDescription(target.description()+" Some other markings say `"+message+"`.");
				else
					target.setDescription(target.description()+" Some markings on it say `"+message+"`.");
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a spell upon <T-NAMESELF>, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}