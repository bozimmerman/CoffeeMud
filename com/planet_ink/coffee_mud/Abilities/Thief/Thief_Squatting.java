package com.planet_ink.coffee_mud.Abilities.Thief;

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
public class Thief_Squatting extends ThiefSkill
{
	public String ID() { return "Thief_Squatting"; }
	public String name(){ return "Squatting";}
	public String displayText(){return "(Squatting)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"SQUAT","SQUATTING"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	private boolean failed=false;
	private Room room=null;
	private LandTitle title=null;

	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if((msg.source()==mob)
			&&(msg.target()==mob.location())
			&&(msg.targetMinor()==CMMsg.TYP_LEAVE))
			{
				failed=true;
				unInvoke();
			}
			else
			if((!Sense.isSitting(mob))||(mob.location()!=room))
			{
				failed=true;
				unInvoke();
			}
		}
		super.executeMsg(host,msg);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if((canBeUninvoked())&&(mob.location()!=null))
		{
			if((failed)||(!Sense.isSitting(mob))||(room==null)||(title==null)||(mob.location()!=room))
				mob.tell("You are no longer squatting.");
			else
			if(title.landOwner().length()>0)
			{
				mob.tell("Your squat has succeeded.  This property no longer belongs to "+title.landOwner()+".");
				title.setLandOwner("");
				title.updateTitle();
				title.updateLot();
			}
			else
			if(title.landOwner().length()>0)
			{
				mob.tell("Your squat has succeeded.  This property now belongs to you.");
				title.setLandOwner(mob.Name());
				title.updateTitle();
				title.updateLot();
			}
		}
		failed=false;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already squatting.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(CoffeeUtensils.doesOwnThisProperty(mob,mob.location()))
		{
			mob.tell("You already own this property!");
			return false;
		}
		LandTitle T=CoffeeUtensils.getLandTitle(mob.location());
		boolean confirmed=false;
		for(int r=0;r<mob.location().numEffects();r++)
			if(mob.location().fetchEffect(r)==T)
				confirmed=true;
		if(T==null)
		{
			mob.tell("This property is not available for sale, and cannot be squatted upon.");
			return false;
		}
		if(!confirmed)
		{
			mob.tell("You cannot squat on an area for sale.");
			return false;
		}
		if(!Sense.isSitting(mob))
		{
			mob.tell("You must be sitting!");
			return false;
		}

		boolean success=profficiencyCheck(mob,0,auto);

		FullMsg msg=new FullMsg(mob,null,this,auto?CMMsg.MASK_GENERAL:CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,auto?"":"<S-NAME> start(s) squatting.");
		if(!success)
			return beneficialVisualFizzle(mob,null,auto?"":"<S-NAME> can't seem to get comfortable here.");
		else
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			failed=false;
			room=mob.location();
			title=T;
			beneficialAffect(mob,target,asLevel,(CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDMONTH)));
		}
		return success;
	}
}
