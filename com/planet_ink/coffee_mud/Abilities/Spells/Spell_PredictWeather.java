package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Spell_PredictWeather extends Spell
{
	public String ID() { return "Spell_PredictWeather"; }
	public String name(){return "Forecast Weather";}
	public String displayText(){return "(Forecast Weather)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_DIVINATION;}

	String lastPrediction="";

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			lastPrediction="";
		super.unInvoke();
		if(canBeUninvoked())
			mob.tell("Your senses are no longer sensitive to the weather.");
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==MudHost.TICK_MOB)
		   &&(affected!=null)
		   &&(affected instanceof MOB)
		   &&(((MOB)affected).location()!=null)
		   &&((((MOB)affected).location().domainType()&Room.INDOORS)==0))
		{
		   String prediction=(((MOB)affected).location().getArea().getClimateObj().nextWeatherDescription(((MOB)affected).location()));
		   if(!prediction.equals(lastPrediction))
		   {
			   lastPrediction=prediction;
			   ((MOB)affected).tell("Your weather senses gaze into the future, you see: \n\r"+prediction);
		   }
		}
		return true;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already detecting weather.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> gain(s) sensitivity to the weather!":"^S<S-NAME> invoke(s) weather sensitivity!^?");
			if(mob.location().okMessage(mob,msg))
			{
				lastPrediction="";
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> incant(s) into the sky, but the spell fizzles.");

		return success;
	}
}