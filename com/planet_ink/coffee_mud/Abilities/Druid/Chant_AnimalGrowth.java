package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_AnimalGrowth extends Chant
{
	public String ID() { return "Chant_AnimalGrowth"; }
	public String name(){ return "Animal Growth";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public String displayText(){return "(Animal Growth)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Chant_AnimalGrowth();}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if((canBeUninvoked())&&(mob.location()!=null)&&(!mob.amDead()))
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> shrink(s) back down to size.");
	}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.STRENGTH,affectedStats.getStat(CharStats.STRENGTH)+5);
		affectedStats.setStat(CharStats.DEXTERITY,affectedStats.getStat(CharStats.DEXTERITY)-3);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectedStats)
	{
		super.affectEnvStats(affected,affectedStats);
		affectedStats.setWeight(affectedStats.weight()*3);
		affectedStats.setHeight(affectedStats.height()*2);
		String oldName=affected.Name().toUpperCase();
		if(oldName.startsWith("A "))
			oldName=affected.Name().substring(2).trim();
		else
		if(oldName.startsWith("AN "))
			oldName=affected.Name().substring(3).trim();
		else
		if(oldName.startsWith("THE "))
			oldName=affected.Name().substring(4).trim();
		else
			oldName=affected.Name();
		affectedStats.setName("An ENORMOUS "+oldName);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(!Sense.isAnimalIntelligence(target))
		{
			mob.tell("This chant only works on animals.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> grow(s) to an ENORMOUS size!");
				beneficialAffect(mob,target,0);
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}