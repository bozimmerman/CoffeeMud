package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_EnchantArmor extends Spell
{
	public String ID() { return "Spell_EnchantArmor"; }
	public String name(){return "Enchant Armor";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Spell_EnchantArmor();}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(!(target instanceof Armor))
		{
			mob.tell("You can't enchant that with an Enchant Armor spell!");
			return false;
		}
		if((mob.curState().getMana()<mob.maxState().getMana())&&(!auto))
		{
			mob.tell("You need to be at full mana to cast this.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		mob.charStats().getCurrentClass().loseExperience(mob,50);
		
		if(!auto)mob.curState().setMana(0);

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> hold(s) <T-NAMESELF> and cast(s) a spell.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target.envStats().ability()>2)
					mob.tell(target.name()+" cannot be enchanted further.");
				else
				{
					mob.location().show(mob,target,Affect.MSG_OK_VISUAL,"<T-NAME> glows!");
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