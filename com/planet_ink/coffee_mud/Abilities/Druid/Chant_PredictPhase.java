package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_PredictPhase extends Chant
{
	public String ID() { return "Chant_PredictPhase"; }
	public String name(){ return "Predict Phase";}
	public int quality(){return Ability.OK_SELF;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_PredictPhase();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) and gaze(s) toward the sky.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.tell(Area.MOON_PHASES[mob.location().getArea().getMoonPhase()]);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) and gaze(s) toward the sky, but the magic fizzles.");

		return success;
	}
}