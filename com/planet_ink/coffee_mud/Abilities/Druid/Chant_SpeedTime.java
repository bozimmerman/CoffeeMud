package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_SpeedTime extends Chant
{
	public String ID() { return "Chant_SpeedTime"; }
	public String name(){ return "Speed Time";}
	public String displayText(){return "(Speed Time)";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_SpeedTime();}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"Something is happening!":"^S<S-NAME> begin(s) to chant...^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				for(int i=0;i<(adjustedLevel(mob)/2);i++)
					ExternalPlay.tickAllTickers(mob.location());
				if(CMMap.numAreas()>0) CMMap.getFirstArea().tickTock(1);
				mob.location().show(mob,null,affectType(auto),auto?"It stops.":"^S<S-NAME> stop(s) chanting.^?");
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> chant(s), but nothing happens.");

		return success;
	}
}