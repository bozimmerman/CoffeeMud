package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Spell_LowerResists extends Spell
{
	public String ID() { return "Spell_LowerResists"; }
	public String name(){return "Lower Resistance";}
	public String displayText(){return "(Lowered Resistances)";}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_LowerResists();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}

	int amount=0;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked)
			mob.tell("Your cold weakness is now gone.");

		super.unInvoke();

	}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.SAVE_ACID,affectedStats.getStat(CharStats.SAVE_ACID)-amount);
		affectedStats.setStat(CharStats.SAVE_COLD,affectedStats.getStat(CharStats.SAVE_COLD)-amount);
		affectedStats.setStat(CharStats.SAVE_DISEASE,affectedStats.getStat(CharStats.SAVE_DISEASE)-amount);
		affectedStats.setStat(CharStats.SAVE_ELECTRIC,affectedStats.getStat(CharStats.SAVE_ELECTRIC)-amount);
		affectedStats.setStat(CharStats.SAVE_FIRE,affectedStats.getStat(CharStats.SAVE_FIRE)-amount);
		affectedStats.setStat(CharStats.SAVE_GAS,affectedStats.getStat(CharStats.SAVE_GAS)-amount);
		affectedStats.setStat(CharStats.SAVE_JUSTICE,affectedStats.getStat(CharStats.SAVE_JUSTICE)-amount);
		affectedStats.setStat(CharStats.SAVE_MAGIC,affectedStats.getStat(CharStats.SAVE_MAGIC)-amount);
		affectedStats.setStat(CharStats.SAVE_MIND,affectedStats.getStat(CharStats.SAVE_MIND)-amount);
		affectedStats.setStat(CharStats.SAVE_PARALYSIS,affectedStats.getStat(CharStats.SAVE_PARALYSIS)-amount);
		affectedStats.setStat(CharStats.SAVE_POISON,affectedStats.getStat(CharStats.SAVE_POISON)-amount);
		affectedStats.setStat(CharStats.SAVE_UNDEAD,affectedStats.getStat(CharStats.SAVE_UNDEAD)-amount);
		affectedStats.setStat(CharStats.SAVE_WATER,affectedStats.getStat(CharStats.SAVE_WATER)-amount);
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"A shimmering unresistable field appears around <T-NAMESELF>.":"^S<S-NAME> invoke(s) a shimmering unresistable field around <T-NAMESELF>.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				for(int a=0;a<target.numAffects();a++)
				{
					Ability A=target.fetchAffect(a);
					if((!A.isAutoInvoked())
					&&(A.canBeUninvoked())
					&&((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ABJURATION))
					{
						int x=target.numAffects();
						A.unInvoke();
						if(x>target.numAffects())
							a--;
					}
				}
				amount=(mob.envStats().level()-target.envStats().level())*5;
				if(amount<5) amount=5;
				success=maliciousAffect(mob,target,0,-1);
			}
		}
		else
			maliciousFizzle(mob,target,"<S-NAME> attempt(s) to invoke lowered resistances on <T-NAMESELF>, but fail(s).");

		return success;
	}
}
