package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_AcidRain extends Chant
{
	public String ID() { return "Chant_AcidRain"; }
	public String name(){ return "Acid Rain";}
	public int quality(){return Ability.MALICIOUS;}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return Ability.CAN_ROOMS;}
	public Environmental newInstance(){	return new Chant_AcidRain();}

	public boolean isRaining(Room R)
	{
		if((R.getArea().getClimateObj().weatherType(R)==Climate.WEATHER_RAIN)
		||(R.getArea().getClimateObj().weatherType(R)==Climate.WEATHER_SLEET)
		||(R.getArea().getClimateObj().weatherType(R)==Climate.WEATHER_THUNDERSTORM))
			return true;
		return false;
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof Room))
		{
			Room R=(Room)affected;
			if(isRaining(R))
			for(int i=0;i<R.numInhabitants();i++)
			{
				MOB M=R.fetchInhabitant(i);
				if((M!=null)&&(Dice.rollPercentage()>M.charStats().getSave(CharStats.SAVE_ACID)))
					MUDFight.postDamage(invoker(),M,this,Dice.roll(1,M.envStats().level(),1),CMMsg.MASK_GENERAL|CMMsg.TYP_ACID,Weapon.TYPE_MELTING,"The acid rain <DAMAGE> <T-NAME>!");
			}
		}
		return true;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Room target=mob.location();
		if(target==null) return false;
		if(!isRaining(target))
		{
			mob.tell("This chant requires some rain.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the rain.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					for(int i=0;i<target.numInhabitants();i++)
					{
						MOB M=target.fetchInhabitant(i);
						if((M!=null)&&(mob!=M))
							mob.location().show(mob,M,CMMsg.MASK_MALICIOUS|CMMsg.TYP_OK_VISUAL,null);
					}
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"Acid rain starts pouring from the sky!");
					maliciousAffect(mob,target,0,-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to the rain, but the magic fades.");
		// return whether it worked
		return success;
	}
}
