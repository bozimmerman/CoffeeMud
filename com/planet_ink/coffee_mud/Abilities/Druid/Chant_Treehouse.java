package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Treehouse extends Chant
{
	public String ID() { return "Chant_Treehouse"; }
	public String name(){ return "Treehouse";}
	public String displayText(){return "(Treehouse)";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return CAN_ROOMS;}
	public Environmental newInstance(){	return new Chant_Treehouse();}
	
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
			if((R!=null)&&(R.ID().equalsIgnoreCase("")))
			{
				R.showHappens(Affect.MSG_OK_VISUAL,"The treehouse fades away...");
				while(R.numInhabitants()>0)
				{
					MOB M=R.fetchInhabitant(0);
					if(M!=null)	room.bringMobHere(M,false);
				}
				while(R.numItems()>0)
				{
					Item I=R.fetchItem(0);
					if(I!=null) room.bringItemHere(I);
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
		if(target.fetchAffect(ID())!=null)
		{
			mob.tell("There is already a treehouse above here!");
			return false;
		}
		if((mob.location().domainType()!=Room.DOMAIN_OUTDOORS_WOODS)
		&&((mob.location().myResource()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_WOODEN)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_JUNGLE))
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		if(mob.location().ID().length()==0)
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		if((mob.location().getRoomInDir(Directions.UP)!=null)
		&&(mob.location().getRoomInDir(Directions.UP).ID().length()>0))
		{
			mob.tell("You can't create a treehouse here!");
			return false;
		}
		
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			FullMsg msg = new FullMsg(mob, null, this, affectType(auto), auto?"":"^S<S-NAME> chant(s) for a treehouse!^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().showHappens(Affect.MSG_OK_VISUAL,"A treehouse appears up in a nearby tree!");
				mob.location().clearSky();
				Room newRoom=CMClass.getLocale("WoodRoom");
				newRoom.setDisplayText("A treehouse");
				newRoom.setDescription("You are up in the treehouse. The view is great from up here!");
				newRoom.setArea(mob.location().getArea());
				mob.location().rawDoors()[Directions.UP]=newRoom;
				mob.location().rawExits()[Directions.UP]=CMClass.getExit("ClimbableExit");
				newRoom.rawDoors()[Directions.DOWN]=mob.location();
				Ability A=CMClass.getAbility("Prop_RoomView");
				A.setMiscText(mob.location().ID());
				Exit E=CMClass.getExit("ClimbableExit");
				E.addNonUninvokableAffect(A);
				newRoom.rawExits()[Directions.DOWN]=E;
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					Room R=mob.location().rawDoors()[d];
					if((R!=null)
					   &&(d!=Directions.DOWN)
					   &&(d!=Directions.UP)
					   &&(R.ID().length()>0)
					   &&((R.domainType()&Room.INDOORS)==0))
					{
						newRoom.rawDoors()[d]=R;
						A=CMClass.getAbility("Prop_RoomView");
						A.setMiscText(R.ID());
						E=CMClass.getExit("Impassable");
						E.addNonUninvokableAffect(A);
						newRoom.rawExits()[d]=E;
					}
				}
				newRoom.getArea().fillInAreaRoom(newRoom);
				beneficialAffect(mob,mob.location(),0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) for a treehouse, but the magic fades.");

		// return whether it worked
		return success;
	}
}