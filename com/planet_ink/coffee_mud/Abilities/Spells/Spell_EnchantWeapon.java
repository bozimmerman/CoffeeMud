package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.Spells.interfaces.*;
import java.util.*;

public class Spell_EnchantWeapon extends Spell
	implements EnchantmentDevotion
{
	public Spell_EnchantWeapon()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Enchant Weapon";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(22);

		addQualifyingClass("Mage",22);
		addQualifyingClass("Ranger",baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_EnchantWeapon();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands);
		if(target==null) return false;

		if(!(target instanceof Weapon))
		{
			mob.tell("You can't enchant that with an Enchant Weapon spell!");
			return false;
		}
		if(mob.curState().getMana()<mob.maxState().getMana())
		{
			mob.tell("You need to be at full mana to cast this.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		mob.setExperience(mob.getExperience()-50);

		mob.curState().setMana(0);

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> hold(s) <T-NAMESELF> and chant(s).");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(target.envStats().ability()>2)
					mob.tell(target.name()+" cannot be enchanted further.");
				else
				{
					mob.location().show(mob,target,Affect.MSG_OK_VISUAL,target.name()+" glows!");
					target.baseEnvStats().setAbility(target.baseEnvStats().ability()+1);
					target.baseEnvStats().setLevel(target.baseEnvStats().level()+3);
					target.recoverEnvStats();
				}
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> hold(s) <T-NAMESELF> and chant(s), looking very frustrated.");


		// return whether it worked
		return success;
	}
}