package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


/* 
   Copyright 2000-2004 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

public class Chant_PredictWeather extends Chant
{
	public String ID() { return "Chant_PredictWeather"; }
	public String name(){ return "Predict Weather";}
	public String displayText(){return "(Predict Weather)";}
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


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already detecting weather.");
			return false;
		}

		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> gain(s) sensitivity to the weather!":"^S<S-NAME> chant(s) for weather sensitivity!^?");
			if(mob.location().okMessage(mob,msg))
			{
				lastPrediction="";
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) into the sky, but the magic fizzles.");

		return success;
	}
}
