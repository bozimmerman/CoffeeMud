package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Duplicate extends Spell
{
	public String ID() { return "Spell_Duplicate"; }
	public String name(){return "Duplicate";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Spell_Duplicate();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ALTERATION;}
	protected int overrideMana(){return Integer.MAX_VALUE;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;

		if(!mob.isMine(target))
		{
			mob.tell("You'll need to pick it up first.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		mob.tell("You lose "+(target.envStats().level()*5)+" experience points.");
		ExternalPlay.postExperience(mob,null,null,-target.envStats().level()*5,false);

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> hold(s) <T-NAMESELF> and cast(s) a spell.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.MSG_OK_VISUAL,target.name()+" blurs and divides into two!");
				Item newTarget=(Item)target.copyOf();
				newTarget.recoverEnvStats();
				if(target.owner() instanceof MOB)
					((MOB)target.owner()).addInventory(newTarget);
				else
				if(target.owner() instanceof Room)
					((Room)target.owner()).addItemRefuse(newTarget,Item.REFUSE_PLAYER_DROP);
				else
					mob.addInventory(newTarget);
				target.recoverEnvStats();
				mob.recoverEnvStats();
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> hold(s) <T-NAMESELF> tightly and incant(s), the spell fizzles.");


		// return whether it worked
		return success;
	}
}