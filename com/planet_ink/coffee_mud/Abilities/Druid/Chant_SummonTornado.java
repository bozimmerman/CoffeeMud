package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_SummonTornado extends Chant
{
	public String ID() { return "Chant_SummonTornado"; }
	public String name(){return "Summon Tornado";}
	public String displayText(){return "(Inside a Tornado)";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	private Vector childrenAffects=new Vector();
	public Environmental newInstance(){	return new Chant_SummonTornado();}
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_FLYING);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		if((mob.location().getArea().weatherType(mob.location())!=Area.WEATHER_THUNDERSTORM)
		||(mob.location().getArea().weatherType(mob.location())!=Area.WEATHER_WINDY))
		{
			mob.tell("This chant requires a thunderstorm!");
			return false;
		}

		Environmental target = mob.location();

		if(target.fetchAffect(this.ID())!=null)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"A tornado is already here!");
			if(mob.location().okAffect(mob,msg))
				mob.location().send(mob,msg);
			return false;
		}
		
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			FullMsg msg = new FullMsg(mob, target, this, affectType(auto), (auto?"A":"^S<S-NAME> chant(s) to the sky and a")+" tornado appears!^?");
			if(mob.location().okAffect(mob,msg))
			{
				childrenAffects=new Vector();
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob.location(),7);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) into the sky, but nothing happens.");

		// return whether it worked
		return success;
	}
}
