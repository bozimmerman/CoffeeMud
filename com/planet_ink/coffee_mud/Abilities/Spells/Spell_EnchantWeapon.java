package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_EnchantWeapon extends Spell
{
	public String ID() { return "Spell_EnchantWeapon"; }
	public String name(){return "Enchant Weapon";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Spell_EnchantWeapon();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}
	protected int overrideMana(){return Integer.MAX_VALUE;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(!(target instanceof Weapon))
		{
			mob.tell("You can't enchant that with an Enchant Weapon spell!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		MUDFight.postExperience(mob,null,null,-50,false);

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> hold(s) <T-NAMESELF> and cast(s) a spell.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target.envStats().ability()>2)
					mob.tell(target.name()+" cannot be enchanted further.");
				else
				{
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"<T-NAME> glows!");
					target.baseEnvStats().setAbility(target.baseEnvStats().ability()+1);
					target.baseEnvStats().setLevel(target.baseEnvStats().level()+3);
					target.recoverEnvStats();
					mob.recoverEnvStats();
				}
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> hold(s) <T-NAMESELF> tightly and whisper(s), but fail(s) to cast a spell.");


		// return whether it worked
		return success;
	}
}