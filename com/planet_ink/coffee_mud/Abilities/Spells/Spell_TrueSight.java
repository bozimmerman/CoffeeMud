package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Spell_TrueSight extends Spell
{
	public String ID() { return "Spell_TrueSight"; }
	public String name(){return "True Sight";}
	public String displayText(){return "(Spell_TrueSight)";}
	public int quality(){ return OK_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_TrueSight();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_DIVINATION;	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			mob.tell(mob,null,"Your no longer have true sight.");
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_HIDDEN);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_INVISIBLE);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_SNEAKERS);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You already have true sight.");
			return false;
		}

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"<S-NAME> gain(s) true sight!":"^S<S-NAME> incant(s) softly, and gain(s) true sight!^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> incant(s) and open(s) <S-HIS-HER> eyes, but the spell fizzles.");

		return success;
	}
}
