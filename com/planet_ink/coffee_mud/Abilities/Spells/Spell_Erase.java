package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Erase extends Spell
{
	public Spell_Erase()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Erase Scroll";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(1);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Erase();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ALTERATION;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell("Erase what?.");
			return false;
		}
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if((target==null)||((target!=null)&&(!(target instanceof Scroll))&&(!target.isReadable())))
		{
			mob.tell("You can't erase that.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"The words on <T-NAME> fade.":"<S-NAME> whisper(s), and then rub(s) on <T-NAMESELF>, making the words fade.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(target instanceof Scroll)
					((Scroll)target).setScrollText("");
				else
					target.setReadableText("");
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> whisper(s), and then rub(s) on <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
