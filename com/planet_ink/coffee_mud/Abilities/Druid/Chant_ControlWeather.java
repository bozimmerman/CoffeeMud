package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_ControlWeather extends Chant
{
	public String ID() { return "Chant_ControlWeather"; }
	public String name(){ return "Control Weather";}
	protected int canAffectCode(){return Ability.CAN_AREAS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_ControlWeather();}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;
		if(!msg.amISource(invoker())
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Ability)
		&&(Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_WEATHERAFFECTING)))
		{
			msg.source().tell("The weather does not heed to your call.");
			return false;
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		Ability A=mob.location().getArea().fetchEffect(ID());
		int size=mob.location().getArea().numberOfIDedRooms();
		size=size/mob.envStats().level();
		if(size<0) size=0;
		if(A!=null) size=size-((A.invoker().envStats().level()-mob.envStats().level())*10);
		boolean success=profficiencyCheck(mob,-size,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,mob.location().getArea(),this,affectType(auto),auto?"The sky changes color!":"^S<S-NAME> chant(s) into the sky for control of the weather!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if((A!=null)&&(A.invoker()!=mob))
					mob.tell("You successfully wrest control of the weather from "+A.invoker().name()+".");
				if(A!=null) A.unInvoke();
				beneficialAffect(mob,mob.location().getArea(),0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) into the sky for control, but the magic fizzles.");

		return success;
	}
}