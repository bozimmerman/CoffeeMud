package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_EnchantWand extends Spell
{
	public String ID() { return "Spell_EnchantWand"; }
	public String name(){return "Enchant Wand";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Spell_EnchantWand();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<2)
		{
			mob.tell("Enchant which spell onto what?");
			return false;
		}
		Environmental target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,(String)commands.lastElement(),Item.WORN_REQ_UNWORNONLY);
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+((String)commands.lastElement())+"' here.");
			return false;
		}
		if(!(target instanceof Wand))
		{
			mob.tell("You can't enchant that.");
			return false;
		}
		if((mob.curState().getMana()<mob.maxState().getMana())&&(!auto))
		{
			mob.tell("You need to be at full mana to cast this.");
			return false;
		}

		commands.removeElementAt(commands.size()-1);
		Wand wand=(Wand)target;

		String spellName=Util.combine(commands,0).trim();
		Spell wandThis=null;
		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if((A!=null)
			&&(A instanceof Spell)
			&&(A.isBorrowed(mob)||(CMAble.qualifiesByLevel(mob,A)))
			&&(A.name().toUpperCase().startsWith(spellName.toUpperCase()))
			&&(!A.ID().equals(this.ID())))
				wandThis=(Spell)A;
		}
		if(wandThis==null)
		{
			mob.tell("You don't know how to enchant anything with '"+spellName+"'.");
			return false;
		}

		if(wand.getSpell()!=null)
		{
			mob.tell("A spell has already been enchanted into '"+wand.name()+"'.");
			return false;
		}
		// lose all the mana!
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if(!auto)mob.curState().setMana(0);
		
		int experienceToLose=10*CMAble.lowestQualifyingLevel(wandThis.ID());
		mob.charStats().getCurrentClass().loseExperience(mob,experienceToLose);
		mob.tell("You lose "+experienceToLose+" experience points for the effort.");

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			setMiscText(wandThis.ID());
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),"^S<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, encanting softly.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				wand.setSpell((Ability)wandThis.copyOf());
				if((wand.usesRemaining()==Integer.MAX_VALUE)||(wand.usesRemaining()<0))
					wand.setUsesRemaining(0);
				wand.setUsesRemaining(wand.usesRemaining()+5);
				wand.text();
				wand.recoverEnvStats();
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, encanting softly, and looking very frustrated.");


		// return whether it worked
		return success;
	}
}
