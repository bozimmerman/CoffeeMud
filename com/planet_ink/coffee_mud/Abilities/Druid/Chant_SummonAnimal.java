package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_SummonAnimal extends Chant
{
	public Chant_SummonAnimal()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Summon Animal";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Animal Summoning)";

		quality=Ability.BENEFICIAL_SELF;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(7);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Chant_SummonAnimal();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		Vector choices=new Vector();
		int fromDir=-1;
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room room=mob.location().getRoomInDir(d);
			Exit exit=mob.location().getExitInDir(d);
			Exit opExit=mob.location().getPairedExit(d);
			if((room!=null)
			&&((room.domainType()&Room.INDOORS)==0)
			&&((exit!=null)&&(exit.isOpen()))
			&&(opExit!=null)&&(opExit.isOpen()))
				choices.addElement(new Integer(d));
		}
		if(choices.size()==0)
		{
			mob.tell("You must be further outdoors to summon an animal.");
			return false;
		}
		fromDir=((Integer)choices.elementAt(Dice.roll(1,choices.size(),-1))).intValue();
		Room newRoom=mob.location().getRoomInDir(fromDir);
		int opDir=Directions.getOpDirectionCode(fromDir);
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"":"<S-NAME> chant(s) and summon(s) a companion from the Java Plain.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				MOB target = determineMonster(mob, adjustedLevel(mob));
				target.bringToLife(newRoom);
				newRoom.recoverRoomStats();
				target.setStartRoom(null);
				ExternalPlay.move(target,opDir,false);
				if(target.location()==mob.location())
					ExternalPlay.follow(target,mob,true);
				else
					target.destroy();
				invoker=mob;
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) and summon(s), but nothing happens.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster, int level)
	{
		MOB newMOB=null;
		
		while(newMOB==null)
		switch(Dice.rollPercentage())
		{
		case 1: newMOB=(MOB)CMClass.getMOB("BlackBear"); break;
		case 2: newMOB=(MOB)CMClass.getMOB("BrownBear"); break;
		case 3: newMOB=(MOB)CMClass.getMOB("Buck"); break;
		case 4: newMOB=(MOB)CMClass.getMOB("Buffalo"); break;
		case 5: newMOB=(MOB)CMClass.getMOB("Bull"); break;
		case 6: newMOB=(MOB)CMClass.getMOB("Cat"); break;
		case 7: newMOB=(MOB)CMClass.getMOB("Cheetah"); break;
		case 8: newMOB=(MOB)CMClass.getMOB("Chicken"); break;
		case 9: newMOB=(MOB)CMClass.getMOB("Cobra"); break;
		case 10: newMOB=(MOB)CMClass.getMOB("CommonBat"); break;
		case 11: newMOB=(MOB)CMClass.getMOB("Cow"); break;
		case 12: newMOB=(MOB)CMClass.getMOB("Deer"); break;
		case 13: newMOB=(MOB)CMClass.getMOB("Doe"); break;
		case 14: newMOB=(MOB)CMClass.getMOB("Dog"); break;
		case 15: newMOB=(MOB)CMClass.getMOB("Falcon"); break;
		case 16: newMOB=(MOB)CMClass.getMOB("GardenSnake"); break;
		case 17: newMOB=(MOB)CMClass.getMOB("GiantBat"); break;
		case 18: newMOB=(MOB)CMClass.getMOB("GiantScorpion"); break;
		case 19: newMOB=(MOB)CMClass.getMOB("Jaguar"); break;
		case 20: newMOB=(MOB)CMClass.getMOB("LargeBat"); break;
		case 21: newMOB=(MOB)CMClass.getMOB("Lizard"); break;
		case 22: newMOB=(MOB)CMClass.getMOB("Panther"); break;
		case 23: newMOB=(MOB)CMClass.getMOB("Parakeet"); break;
		case 24: newMOB=(MOB)CMClass.getMOB("Pig"); break;
		case 25: newMOB=(MOB)CMClass.getMOB("Python"); break;
		case 26: newMOB=(MOB)CMClass.getMOB("Rattlesnake"); break;
		case 27: newMOB=(MOB)CMClass.getMOB("Sheep"); break;
		case 28: newMOB=(MOB)CMClass.getMOB("Tiger"); break;
		case 29: newMOB=(MOB)CMClass.getMOB("WildEagle"); break;
		case 30: newMOB=(MOB)CMClass.getMOB("Wolf"); break;
		case 31: newMOB=(MOB)CMClass.getMOB("Ape"); break;
		case 32: newMOB=(MOB)CMClass.getMOB("Chimp"); break;
		case 33: newMOB=(MOB)CMClass.getMOB("Duck"); break;
		case 34: newMOB=(MOB)CMClass.getMOB("Kitten"); break;
		case 35: newMOB=(MOB)CMClass.getMOB("Monkey"); break;
		case 36: newMOB=(MOB)CMClass.getMOB("Mouse"); break;
		case 37: newMOB=(MOB)CMClass.getMOB("Puppy"); break;
		case 38: newMOB=(MOB)CMClass.getMOB("Rabbit"); break;
		case 39: newMOB=(MOB)CMClass.getMOB("Rat"); break;
		case 40: newMOB=(MOB)CMClass.getMOB("Turtle"); break;
		case 41: newMOB=(MOB)CMClass.getMOB("Raven"); break;
		default: continue;
		}

		newMOB.setLocation(caster.location());
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		return(newMOB);


	}
}
