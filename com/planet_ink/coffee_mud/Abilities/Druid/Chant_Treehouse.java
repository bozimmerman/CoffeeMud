package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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

@SuppressWarnings("unchecked")
public class Chant_Treehouse extends Chant
{
	public String ID() { return "Chant_Treehouse"; }
	public String name(){ return "Treehouse";}
	public String displayText(){return "(Treehouse)";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTCONTROL;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
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
				R.showHappens(CMMsg.MSG_OK_VISUAL,"The treehouse fades away...");
				while(R.numInhabitants()>0)
				{
					MOB M=R.fetchInhabitant(0);
					if(M!=null)	room.bringMobHere(M,false);
				}
				while(R.numItems()>0)
				{
					Item I=R.fetchItem(0);
					if(I!=null) room.bringItemHere(I,-1,false);
				}
				R.destroy();
				room.rawDoors()[Directions.UP]=null;
                room.setRawExit(Directions.UP,null);
			}
			room.clearSky();
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental target = mob.location();
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("There is already a treehouse above here!");
			return false;
		}
		boolean isATree=((mob.location().domainType()==Room.DOMAIN_OUTDOORS_WOODS)
					   ||(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_JUNGLE));
		Item myPlant=Druid_MyPlants.myPlant(mob.location(),mob,0);
		if((myPlant != null) &&((myPlant.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN))
			isATree=true;
		if((mob.location().myResource()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN)
			isATree=true;
		if(!isATree)
		{
			mob.tell("There really aren't enough trees here to chant to.");
			return false;
		}
		if(mob.location().roomID().length()==0)
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		if((mob.location().getRoomInDir(Directions.UP)!=null)
		&&(mob.location().getRoomInDir(Directions.UP).roomID().length()>0))
		{
			mob.tell("You can't create a treehouse here!");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			CMMsg msg = CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto), auto?"":"^S<S-NAME> chant(s) for a treehouse!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"A treehouse appears up in a nearby tree!");
				mob.location().clearSky();
				Room newRoom=CMClass.getLocale("WoodRoom");
				newRoom.setDisplayText("A treehouse");
				newRoom.setDescription("You are up in the treehouse. The view is great from up here!");
				newRoom.setArea(mob.location().getArea());
				mob.location().rawDoors()[Directions.UP]=newRoom;
				mob.location().setRawExit(Directions.UP,CMClass.getExit("ClimbableExit"));
				newRoom.rawDoors()[Directions.DOWN]=mob.location();
				Ability A=CMClass.getAbility("Prop_RoomView");
				A.setMiscText(CMLib.map().getExtendedRoomID(mob.location()));
				Exit E=CMClass.getExit("ClimbableExit");
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

				newRoom.setRawExit(Directions.DOWN,E);
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					Room R=mob.location().rawDoors()[d];
					if((R!=null)
					   &&(d!=Directions.DOWN)
					   &&(d!=Directions.UP)
					   &&(R.roomID().length()>0)
					   &&((R.domainType()&Room.INDOORS)==0))
					{
						newRoom.rawDoors()[d]=R;
						A=CMClass.getAbility("Prop_RoomView");
						A.setMiscText(CMLib.map().getExtendedRoomID(R));
						E=CMClass.getExit("Impassable");
						E.addNonUninvokableEffect(A);
						newRoom.setRawExit(d,E);
					}
				}
				newRoom.getArea().fillInAreaRoom(newRoom);
				beneficialAffect(mob,mob.location(),asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) for a treehouse, but the magic fades.");

		// return whether it worked
		return success;
	}
}
