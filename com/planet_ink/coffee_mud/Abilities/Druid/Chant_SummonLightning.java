package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_SummonLightning extends Chant
{
	public String ID() { return "Chant_SummonLightning"; }
	public String name(){ return "Summon Lightning";}
	public int quality(){return Ability.MALICIOUS;}
	public int maxRange(){return 2;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Chant_SummonLightning();}
	public long flags(){return Ability.FLAG_WEATHERAFFECTING;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		if(mob.location().getArea().getClimateObj().weatherType(mob.location())!=Climate.WEATHER_THUNDERSTORM)
		{
			mob.tell("This chant requires a thunderstorm!");
			return false;
		}
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"A lightning bolt streaks out of the sky!":"^S<S-NAME> chant(s) to <T-NAMESELF>.  Suddenly a lightning bolt streaks from the sky!^?")+CommonStrings.msp("lightning.wav",40));
			FullMsg msg2=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_ELECTRIC|(auto?CMMsg.MASK_GENERAL:0),null);
			if((mob.location().okMessage(mob,msg))&&((mob.location().okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				int maxDie =  (int)Math.round(new Integer(adjustedLevel(mob)).doubleValue());
				int damage = Dice.roll(maxDie,8,maxDie);
				if((msg.value()>0)||(msg2.value()>0))
					damage = (int)Math.round(Util.div(damage,2.0));
				if(target.location()==mob.location())
					MUDFight.postDamage(mob,target,this,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_ELECTRIC,Weapon.TYPE_STRIKING,"The bolt <DAMAGE> <T-NAME>!");
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAMESELF>, but the magic fades.");


		// return whether it worked
		return success;
	}
}
