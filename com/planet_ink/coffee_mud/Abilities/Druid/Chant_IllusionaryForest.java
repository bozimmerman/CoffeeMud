package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_IllusionaryForest extends Chant
{
	public String ID() { return "Chant_IllusionaryForest"; }
	public String name(){ return "Illusionary Forest";}
	public String displayText(){return "(Illusionary Fores)";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return CAN_ROOMS;}
	Room newRoom=null;
	public Environmental newInstance(){	return new Chant_IllusionaryForest();}

	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		Room room=(Room)affected;
		if(canBeUninvoked())
			room.showHappens(CMMsg.MSG_OK_VISUAL, "The appearance of this place changes...");
		super.unInvoke();
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof Room)
		&&(msg.amITarget(affected))
		&&(newRoom().fetchEffect(ID())==null)
		&&(msg.targetMinor()==CMMsg.TYP_EXAMINESOMETHING))
		{
			FullMsg msg2=new FullMsg(msg.source(),newRoom(),msg.tool(),
						  msg.sourceCode(),msg.sourceMessage(),
						  msg.targetCode(),msg.targetMessage(),
						  msg.othersCode(),msg.othersMessage());
			if(newRoom().okMessage(myHost,msg2))
			{
				newRoom().executeMsg(myHost,msg2);
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public Room newRoom()
	{
		if(newRoom!=null) return newRoom;
		newRoom=CMClass.getLocale("Woods");
		switch(Dice.roll(1,10,0))
		{
		case 1:
			newRoom.setDisplayText("Forest glade");
			newRoom.setDescription("This quaint forest glade is surrounded by tall oak trees.  A gentle breeze tosses leaves up into the air.");
			break;
		case 2:
			newRoom.setDisplayText("Dark Forest");
			newRoom.setDescription("The forest is dark and thick here.  Ominous looking trees seem to block every path, and the air is perfectly still.");
			break;
		case 3:
			newRoom.setDisplayText("Light Forest");
			newRoom.setDescription("A light growth of tall evergreens surrounds you on all sides.  There are no apparant paths, but you can still see the sky through the leaves.");
			break;
		case 4:
			newRoom.setDisplayText("Forest by the stream");
			newRoom.setDescription("A light growth of tall evergreens surrounds you on all sides.  You can hear the sound of a running brook, but can't tell which direction its coming from.");
			break;
		case 5:
			newRoom.setDisplayText("Dark Forest");
			newRoom.setDescription("The trees around you are dark and old, their branches seeming to reach towards you.  In the distance, a wolfs howl can be heard.");
			break;
		case 6:
			newRoom.setDisplayText("End of the path");
			newRoom.setDescription("The forest path seems to end at the base of a copse of tall evergreens.  Behind you, the path has mysteriously vanished.");
			break;
		case 7:
			newRoom.setDisplayText("Forest");
			newRoom.setDescription("You are standing in the middle of a light forest.  How you got here, you can't really say.");
			break;
		case 8:
			newRoom.setDisplayText("Dark Forest");
			newRoom.setDescription("You are standing in the middle of a thick dark forest.  You wish you knew how you got here.");
			break;
		case 9:
			newRoom.setDisplayText("Dark Forest");
			newRoom.setDescription("The trees here seem to tower endlessly into the sky.  Their branches blocking out all but the smallest glimpses of the sky.");
			break;
		case 10:
			newRoom.setDisplayText("Druidic Forest");
			newRoom.setDescription("A forest seems to have grown up all around you.  The strange magical nature of the mushroom like trees makes you think you've entered a druidic grove.");
			break;
		}
		return newRoom;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		Environmental target = mob.location();
		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			newRoom();
			FullMsg msg = new FullMsg(mob, target, this, affectType(auto), auto?"":"^S<S-NAME> chant(s) dramatically!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"The appearance of this place changes...");
				if(CoffeeUtensils.doesOwnThisProperty(mob,mob.location()))
				{
					mob.location().addNonUninvokableEffect((Ability)copyOf());
					CMClass.DBEngine().DBUpdateRoom(mob.location());
				}
				else
					beneficialAffect(mob,mob.location(),0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) dramatically, but the magic fades.");

		// return whether it worked
		return success;
	}
}
