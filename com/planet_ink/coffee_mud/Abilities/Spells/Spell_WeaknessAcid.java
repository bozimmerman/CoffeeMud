package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Spell_WeaknessAcid extends Spell
{
	public String ID() { return "Spell_WeaknessAcid"; }
	public String name(){return "Weakness to Acid";}
	public String displayText(){return "(Weakness to Acid)";}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_WeaknessAcid();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))&&(Util.bset(msg.targetCode(),CMMsg.MASK_HURT))
		   &&(msg.sourceMinor()==CMMsg.TYP_ACID))
		{
			int recovery=(int)Math.round(Util.mul((msg.targetCode()-CMMsg.MASK_HURT),1.5));
			SaucerSupport.adjustDamageMessage(msg,recovery);
		}
		return true;
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your acidic weakness is now gone.");

		super.unInvoke();

	}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.SAVE_ACID,affectedStats.getStat(CharStats.SAVE_ACID)-100);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"A shimmering absorbing field appears around <T-NAMESELF>.":"^S<S-NAME> invoke(s) a shimmering absorbing field of weakness around <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				success=maliciousAffect(mob,target,0,-1);
			}
		}
		else
			maliciousFizzle(mob,target,"<S-NAME> attempt(s) to invoke weakness to acid, but fail(s).");

		return success;
	}
}
