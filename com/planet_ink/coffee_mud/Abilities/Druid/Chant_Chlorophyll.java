package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Chlorophyll extends Chant
{
	public String ID() { return "Chant_Chlorophyll"; }
	public String name(){return "Chlorophyll";}
	public String displayText(){return "(Chlorophyll)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){ return new Chant_Chlorophyll();}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> skin returns to a normal color.");
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected==null)||(!(affected instanceof MOB)))
		{
			unInvoke();
			return false;
		}
		else
		{
			MOB mob=(MOB)affected;
			Room R=mob.location();
			if((R!=null)
			&&((R.getArea().getTODCode()==Area.TIME_DAY)||(R.getArea().getTODCode()==Area.TIME_DAWN))
			&&((R.domainType()&Room.INDOORS)==0)
			&&((R.getArea().weatherType(R)==Area.WEATHER_CLEAR)
			   ||(R.getArea().weatherType(R)==Area.WEATHER_DROUGHT)
			   ||(R.getArea().weatherType(R)==Area.WEATHER_WINDY)
			   ||(R.getArea().weatherType(R)==Area.WEATHER_WINTER_COLD)
			   ||(R.getArea().weatherType(R)==Area.WEATHER_HEAT_WAVE)))
			mob.curState().adjHunger(2,mob.maxState());
			return true;
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=super.getTarget(mob,commands,givenTarget);
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
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<S-NAME> gain(s) chlorophyll in <S-HIS-HER> skin!":"^S<S-NAME> chant(s) to <T-NAMESELF>, turning <T-HIM-HER> a light shade of chlorophyll green!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s), but nothing more happens.");

		// return whether it worked
		return success;
	}
}
