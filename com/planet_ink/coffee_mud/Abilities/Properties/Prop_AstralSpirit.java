package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_AstralSpirit extends Property
{
	public String ID() { return "Prop_AstralSpirit"; }
	public String name(){ return "Astral Spirit";}
	public String displayText(){ return "(Spirit Form)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prop_AstralSpirit();}
	public boolean autoInvocation(MOB mob)
	{
		if((mob!=null)&&(mob.fetchAffect(ID())==null))
		{
			mob.addNonUninvokableAffect(this);
			return true;
		}
		return false;
	}

	public String accountForYourself()
	{ return "an astral spirit";	}

	public void peaceAt(MOB mob)
	{
		Room room=mob.location();
		if(room==null) return;
		for(int m=0;m<room.numInhabitants();m++)
		{
			MOB inhab=room.fetchInhabitant(m);
			if((inhab!=null)&&(inhab.getVictim()==mob))
				inhab.setVictim(null);
		}
	}
	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;
		MOB mob=(MOB)affected;

		if((affect.amISource(mob))&&(!Util.bset(affect.sourceMajor(),Affect.MASK_GENERAL)))
		{
			if((affect.tool()!=null)&&(affect.tool().ID().equalsIgnoreCase("Skill_Revoke")))
			   return super.okAffect(myHost,affect);
			else
			if(affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
			{
				mob.tell("You are unable to attack in this incorporeal form.");
				peaceAt(mob);
				return false;
			}
			else
			if((Util.bset(affect.sourceMajor(),Affect.MASK_HANDS))
			||(Util.bset(affect.sourceMajor(),Affect.MASK_MOUTH)))
			{
				if(Util.bset(affect.sourceMajor(),Affect.MASK_SOUND))
					mob.tell("You are unable to make sounds in this incorporeal form.");
				else
					mob.tell("You are unable to do that this incorporeal form.");
				peaceAt(mob);
				return false;
			}
		}
		else
		if((affect.amITarget(mob))&&(!affect.amISource(mob))
		   &&(!Util.bset(affect.targetMajor(),Affect.MASK_GENERAL)))
		{
			mob.tell(mob.name()+" doesn't seem to be here.");
			return false;
		}
		return true;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setWeight(0);
		affectableStats.setHeight(-1);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GOLEM);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_INVISIBLE);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_NOT_SEEN);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SPEAK);
	}
}
