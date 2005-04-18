package com.planet_ink.coffee_mud.Abilities.Spells;

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
public class Spell_FindFamiliar extends Spell
{
	public String ID() { return "Spell_FindFamiliar"; }
	public String name(){return "Find Familiar";}
	public String displayText(){return "(Find Familiar)";}
	public int quality(){return BENEFICIAL_SELF;};
	protected int canTargetCode(){return 0;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_CONJURATION;}
	public long flags(){return Ability.FLAG_NOORDERING;}
	protected int overrideMana(){return Integer.MAX_VALUE;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob.numFollowers()>0)
		{
			mob.tell("You cannot have any followers when casting this spell.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		MUDFight.postExperience(mob,null,null,-100,false);

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> call(s) for a familiar.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB target = determineMonster(mob, mob.envStats().level());
				if(target.isInCombat()) target.makePeace();
				CommonMsgs.follow(target,mob,true);
				invoker=mob;
				if(target.amFollowing()!=mob)
					mob.tell(target.name()+" seems unwilling to follow you.");
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> call(s), but choke(s) on the words.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster, int level)
	{

		MOB newMOB=CMClass.getMOB("GenMOB");
		newMOB.baseEnvStats().setAbility(7);
		newMOB.baseEnvStats().setLevel(level);
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.baseCharStats().setStat(CharStats.GENDER,'M');
		int choice=Dice.roll(1,9,-1);
		switch(choice)
		{
		case 0:
			newMOB.setName("a dog");
			newMOB.setDisplayText("a dog is sniffing around here");
			newMOB.setDescription("She looks like a nice loyal companion.");
			newMOB.baseCharStats().setStat(CharStats.GENDER,'F');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Dog"));
			break;
		case 1:
			newMOB.setName("a turtle");
			newMOB.setDisplayText("a turtle is crawling around here");
			newMOB.setDescription("Not very fast, but pretty cute.");
			newMOB.baseCharStats().setStat(CharStats.GENDER,'F');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Turtle"));
			break;
		case 2:
			newMOB.setName("a cat");
			newMOB.setDisplayText("a cat is prowling around here");
			newMOB.setDescription("She looks busy ignoring you.");
			newMOB.baseCharStats().setStat(CharStats.GENDER,'F');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Cat"));
			break;
		case 3:
			newMOB.setName("a bat");
			newMOB.setDisplayText("a bat is flying around here");
			newMOB.setDescription("The darn thing just won`t stay still!");
			newMOB.baseCharStats().setStat(CharStats.GENDER,'M');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Bat"));
			break;
		case 4:
			newMOB.setName("a rat");
			newMOB.setDisplayText("a rat scurries nearby");
			newMOB.setDescription("Such a cute, furry little guy!");
			newMOB.baseCharStats().setStat(CharStats.GENDER,'M');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Rodent"));
			break;
		case 5:
			newMOB.setName("a snake");
			newMOB.setDisplayText("a snake is slithering around");
			newMOB.setDescription("..red on yellow..., how did that go again?");
			newMOB.baseCharStats().setStat(CharStats.GENDER,'M');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Snake"));
			break;
		case 6:
			newMOB.setName("an owl");
			newMOB.setDisplayText("an owl is flying around here");
			newMOB.setDescription("He looks wise beyond his years.");
			newMOB.baseCharStats().setStat(CharStats.GENDER,'M');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Owl"));
			break;
		case 7:
			newMOB.setName("a rabbit");
			newMOB.setDisplayText("a cute little rabbit is watching you");
			newMOB.setDescription("Don`t blink, or she may twitch her nose.");
			newMOB.baseCharStats().setStat(CharStats.GENDER,'F');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Rabbit"));
			break;
		case 8:
			newMOB.setName("a raven");
			newMOB.setDisplayText("a raven is pearched nearby");
			newMOB.setDescription("You think he`s watching you.");
			newMOB.baseCharStats().setStat(CharStats.GENDER,'M');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Raven"));
			break;
		}
		newMOB.recoverEnvStats();
		newMOB.recoverCharStats();
		newMOB.baseEnvStats().setArmor(newMOB.baseCharStats().getCurrentClass().getLevelArmor(newMOB));
		newMOB.baseEnvStats().setAttackAdjustment(newMOB.baseCharStats().getCurrentClass().getLevelAttack(newMOB));
		newMOB.baseEnvStats().setDamage(newMOB.baseCharStats().getCurrentClass().getLevelDamage(newMOB));
		newMOB.baseEnvStats().setSpeed(newMOB.baseCharStats().getCurrentClass().getLevelSpeed(newMOB));
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		Factions.setAlignment(newMOB,Faction.ALIGN_GOOD);
		newMOB.setStartRoom(null);
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		Ability A=CMClass.getAbility("Prop_Familiar");
		A.setMiscText(""+choice);
		newMOB.addNonUninvokableEffect(A);
		newMOB.text();
		newMOB.bringToLife(caster.location(),true);
		BeanCounter.clearZeroMoney(newMOB,null);
		newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
		caster.location().recoverRoomStats();
		return(newMOB);


	}
}
