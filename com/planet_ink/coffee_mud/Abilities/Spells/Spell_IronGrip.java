package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Spell_IronGrip extends Spell
{
	public String ID() { return "Spell_IronGrip"; }
	public String name(){return "Iron Grip";}
	public String displayText(){return "(Iron Grip)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_IronGrip();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> weapon hand becomes flesh again.");

		super.unInvoke();

	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if((msg.amITarget(mob))
			&&(msg.tool()!=null)
			&&(msg.tool().ID().toUpperCase().indexOf("DISARM")>=0))
			{
				mob.location().show(msg.source(),mob,CMMsg.MSG_OK_ACTION,"<S-NAME> attempt(s) to disarm <T-NAME>, but the grip is too strong!");
				return false;
			}
			else
			if((msg.amISource(mob))
			&&((msg.targetMinor()==CMMsg.TYP_DROP)
				||(msg.targetMinor()==CMMsg.TYP_THROW)
				||(msg.targetMinor()==CMMsg.TYP_GET)
			    ||(msg.targetMinor()==CMMsg.TYP_REMOVE))
			&&(msg.target()!=null)
			&&(msg.target() instanceof Item)
			&&(mob.isMine((Item)msg.target()))
			&&(((Item)msg.target()).amWearingAt(Item.WIELD)))
			{
				mob.location().show(mob,null,msg.target(),CMMsg.MSG_OK_ACTION,"<S-NAME> attempt(s) to let go of <O-NAME>, but <S-HIS-HER> grip is too strong!");
				return false;
			}
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> watch(es) <T-HIS-HER> weapon hand turn to iron!":"^S<S-NAME> invoke(s) a spell on <T-NAMESELF> and <T-HIS-HER> weapon hand turns into iron!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a spell, but fail(s).");

		return success;
	}
}