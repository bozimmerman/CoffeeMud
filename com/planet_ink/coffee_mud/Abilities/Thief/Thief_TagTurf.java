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
public class Thief_TagTurf extends ThiefSkill
{
	public String ID() { return "Thief_TagTurf"; }
	public String name(){ return "Tag Turf";}
	public String displayText(){return "(Tagged)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"TURFTAG","TAGTURF"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}

	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(!super.okMessage(host,msg))
			return false;

		if((!msg.source().Name().equals(text()))
		&&(msg.tool() instanceof Ability)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES)==Ability.THIEF_SKILL))
		{
			msg.source().tell("You definitely aren't allowed to do that on "+text()+"'s turf.");
			return false;
		}
		return true;
	}

	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((msg.target()==affected)
		&&(affected instanceof Room)
		&&(msg.targetMinor()==CMMsg.TYP_EXAMINESOMETHING))
		{
			if(msg.source().Name().equals(text()))
				msg.addTrailerMsg(new FullMsg(msg.source(),msg.target(),null,
										CMMsg.MSG_OK_VISUAL,"This is your turf.",
										msg.NO_EFFECT,null,
										msg.NO_EFFECT,null));
			else
				msg.addTrailerMsg(new FullMsg(msg.source(),msg.target(),null,
										CMMsg.MSG_OK_VISUAL,"This turf has been claimed by "+text()+".",
										msg.NO_EFFECT,null,
										msg.NO_EFFECT,null));
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		Room target=mob.location();
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof Room))
			target=(Room)givenTarget;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("This place has already been tagged.");
			return false;
		}
		if((mob.location().domainType()!=Room.DOMAIN_OUTDOORS_CITY)
		   &&(mob.location().domainType()!=Room.DOMAIN_INDOORS_WOOD)
		   &&(mob.location().domainType()!=Room.DOMAIN_INDOORS_STONE))
		{
			mob.tell("A place like this can't get your turf.");
			return false;
		}
		if((!CoffeeUtensils.doesOwnThisProperty(mob,mob.location()))
		&&(CoffeeUtensils.getLandTitle(mob.location())!=null)
		&&(CoffeeUtensils.getLandTitle(mob.location()).landOwner().length()>0))
		{
			mob.tell("You can't tag anothers property!");
			return false;
		}



		boolean success=profficiencyCheck(mob,0,auto);

		FullMsg msg=new FullMsg(mob,null,this,auto?CMMsg.MASK_GENERAL:CMMsg.MSG_DELICATE_HANDS_ACT,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,auto?"":"<S-NAME> tag(s) this place as <S-HIS-HER> turf.");
		if(!success)
			return beneficialVisualFizzle(mob,null,auto?"":"<S-NAME> attempt(s) to tag this place, but can't get into it.");
		else
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			setMiscText(mob.Name());
			beneficialAffect(mob,target,(int)(CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)*5));
		}
		return success;
	}
}
