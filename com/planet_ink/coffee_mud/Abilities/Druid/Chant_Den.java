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

public class Chant_Den extends Chant
{
	public String ID() { return "Chant_Den"; }
	public String name(){ return "Den";}
	public String displayText(){return "(Den)";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return CAN_ROOMS;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		Room room=(Room)affected;
		if(canBeUninvoked())
		{
			Room R=room.getRoomInDir(Directions.UP);
			if((R!=null)&&(R.roomID().equalsIgnoreCase("")))
			{
				R.showHappens(CMMsg.MSG_OK_VISUAL,"The den fades away...");
				while(R.numInhabitants()>0)
				{
					MOB M=R.fetchInhabitant(0);
					if(M!=null)	room.bringMobHere(M,false);
				}
				while(R.numItems()>0)
				{
					Item I=R.fetchItem(0);
					if(I!=null) room.bringItemHere(I,-1);
				}
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					R.rawDoors()[d]=null;
					R.rawExits()[d]=null;
				}
				CMMap.delRoom(R);
				room.rawDoors()[Directions.UP]=null;
				room.rawExits()[Directions.UP]=null;
			}
			room.clearSky();
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target = mob.location();
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("There is already a den here!");
			return false;
		}
		if(mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE)
		{
			mob.tell("This magic will only work in a cave.");
			return false;
		}
		if(mob.location().roomID().length()==0)
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		Vector dirChoices=new Vector();
		for(int d=0;d<4;d++)
		{
			if(mob.location().getRoomInDir(d)==null)
				dirChoices.addElement(new Integer(d));
		}
		if(dirChoices.size()==0)
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		int d=((Integer)dirChoices.elementAt(Dice.roll(1,dirChoices.size(),-1))).intValue();

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

			FullMsg msg = new FullMsg(mob, null, this, affectType(auto), auto?"":"^S<S-NAME> chant(s) for a den!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"Your den, carefully covered, appears to the "+Directions.getDirectionName(d)+"!");
				Room newRoom=CMClass.getLocale("CaveRoom");
				newRoom.setDisplayText("A musty den");
				newRoom.setDescription("You are in a dark rocky den!");
				newRoom.setArea(mob.location().getArea());
				mob.location().rawDoors()[d]=newRoom;
				mob.location().rawExits()[d]=CMClass.getExit("HiddenWalkway");
				newRoom.rawDoors()[Directions.getOpDirectionCode(d)]=mob.location();
				Ability A=CMClass.getAbility("Prop_RoomView");
				A.setMiscText(CMMap.getExtendedRoomID(mob.location()));
				Exit E=CMClass.getExit("Open");
				E.addNonUninvokableEffect(A);
				A=CMClass.getAbility("Prop_PeaceMaker");
				if(A!=null) newRoom.addEffect(A);
				A=CMClass.getAbility("Prop_NoRecall");
				if(A!=null) newRoom.addEffect(A);
				A=CMClass.getAbility("Prop_NoSummon");
				if(A!=null) newRoom.addEffect(A);
				A=CMClass.getAbility("Prop_NoTeleport");
				if(A!=null) newRoom.addEffect(A);
				A=CMClass.getAbility("Prop_NoTeleportOut");
				if(A!=null) newRoom.addEffect(A);

				newRoom.rawExits()[Directions.getOpDirectionCode(d)]=E;
				newRoom.getArea().fillInAreaRoom(newRoom);
				beneficialAffect(mob,mob.location(),(int)CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY));
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) for a den, but the magic fades.");

		// return whether it worked
		return success;
	}
}