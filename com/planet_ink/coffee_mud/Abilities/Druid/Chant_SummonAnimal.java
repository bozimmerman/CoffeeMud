package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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

public class Chant_SummonAnimal extends Chant
{
	public String ID() { return "Chant_SummonAnimal"; }
	public String name(){ return "Summon Animal";}
	public String displayText(){return "(Animal Summoning)";}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public long flags(){return Ability.FLAG_SUMMONING;}

	public void unInvoke()
	{
		MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			if(mob.amDead()) mob.setLocation(null);
			mob.destroy();
		}
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing()))
		&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
			unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
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
			Exit opExit=mob.location().getReverseExit(d);
			if((room!=null)
			&&((room.domainType()&Room.INDOORS)==0)
			&&(room.domainType()!=Room.DOMAIN_OUTDOORS_AIR)
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

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) and summon(s) a companion from the Java Plain.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB target = determineMonster(mob, adjustedLevel(mob,asLevel));
				target.bringToLife(newRoom,true);
				BeanCounter.clearZeroMoney(target,null);
				target.location().showOthers(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
				newRoom.recoverRoomStats();
				target.setStartRoom(null);
				if(target.isInCombat()) target.makePeace();
				MUDTracker.move(target,opDir,false,false);
				if(target.location()==mob.location())
				{
					if(target.isInCombat()) target.makePeace();
					CommonMsgs.follow(target,mob,true);
					beneficialAffect(mob,target,asLevel,0);
					if(target.amFollowing()!=mob)
						mob.tell(target.name()+" seems unwilling to follow you.");
				}
				else
				{
					if(target.amDead()) target.setLocation(null);
					target.destroy();
				}
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
		{
			switch(Dice.rollPercentage())
			{
			case 1: newMOB=CMClass.getMOB("BlackBear"); break;
			case 2: newMOB=CMClass.getMOB("BrownBear"); break;
			case 3: newMOB=CMClass.getMOB("Buck"); break;
			case 4: newMOB=CMClass.getMOB("Buffalo"); break;
			case 5: newMOB=CMClass.getMOB("Bull"); break;
			case 6: newMOB=CMClass.getMOB("Cat"); break;
			case 7: newMOB=CMClass.getMOB("Cheetah"); break;
			case 8: newMOB=CMClass.getMOB("Chicken"); break;
			case 9: newMOB=CMClass.getMOB("Cobra"); break;
			case 10: newMOB=CMClass.getMOB("CommonBat"); break;
			case 11: newMOB=CMClass.getMOB("Cow"); break;
			case 12: newMOB=CMClass.getMOB("Deer"); break;
			case 13: newMOB=CMClass.getMOB("Doe"); break;
			case 14: newMOB=CMClass.getMOB("Dog"); break;
			case 15: newMOB=CMClass.getMOB("Falcon"); break;
			case 16: newMOB=CMClass.getMOB("GardenSnake"); break;
			case 17: newMOB=CMClass.getMOB("GiantBat"); break;
			case 18: newMOB=CMClass.getMOB("GiantScorpion"); break;
			case 19: newMOB=CMClass.getMOB("Jaguar"); break;
			case 20: newMOB=CMClass.getMOB("LargeBat"); break;
			case 21: newMOB=CMClass.getMOB("Lizard"); break;
			case 22: newMOB=CMClass.getMOB("Panther"); break;
			case 23: newMOB=CMClass.getMOB("Parakeet"); break;
			case 24: newMOB=CMClass.getMOB("Pig"); break;
			case 25: newMOB=CMClass.getMOB("Python"); break;
			case 26: newMOB=CMClass.getMOB("Rattlesnake"); break;
			case 27: newMOB=CMClass.getMOB("Sheep"); break;
			case 28: newMOB=CMClass.getMOB("Tiger"); break;
			case 29: newMOB=CMClass.getMOB("WildEagle"); break;
			case 30: newMOB=CMClass.getMOB("Wolf"); break;
			case 31: newMOB=CMClass.getMOB("Ape"); break;
			case 32: newMOB=CMClass.getMOB("Chimp"); break;
			case 33: newMOB=CMClass.getMOB("Duck"); break;
			case 34: newMOB=CMClass.getMOB("Kitten"); break;
			case 35: newMOB=CMClass.getMOB("Monkey"); break;
			case 36: newMOB=CMClass.getMOB("Mouse"); break;
			case 37: newMOB=CMClass.getMOB("Puppy"); break;
			case 38: newMOB=CMClass.getMOB("Rabbit"); break;
			case 39: newMOB=CMClass.getMOB("Rat"); break;
			case 40: newMOB=CMClass.getMOB("Turtle"); break;
			case 41: newMOB=CMClass.getMOB("Raven"); break;
			default: break;
			}
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
