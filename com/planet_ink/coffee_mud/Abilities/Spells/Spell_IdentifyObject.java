package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_IdentifyObject extends Spell
{
	public String ID() { return "Spell_IdentifyObject"; }
	public String name(){return "Identify Object";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Spell_IdentifyObject();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_DIVINATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> stud(ys) <T-NAMESELF> very closely.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				String identity=((Item)target).secretIdentity();
				if(mob.isMonster())
					ExternalPlay.quickSay(mob,null,identity,false,false);
				else
					mob.tell(identity);
			}

		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> stud(ys) <T-NAMESELF>, looking more frustrated every second.");


		// return whether it worked
		return success;
	}
}
