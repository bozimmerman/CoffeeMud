package com.planet_ink.coffee_mud.Abilities.Spells;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
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
public class Spell_FindFamiliar extends Spell
{
	public String ID() { return "Spell_FindFamiliar"; }
	public String name(){return "Find Familiar";}
	public String displayText(){return "(Find Familiar)";}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	public int enchantQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;}
	public long flags(){return Ability.FLAG_NOORDERING;}
	protected int overrideMana(){return Integer.MAX_VALUE;}
	public int castingQuality(MOB mob, MOB target)
	{
		if((target!=null)&&(mob!=target)) return Ability.QUALITY_INDIFFERENT;
		if(mob.numFollowers()>0) return Ability.QUALITY_INDIFFERENT;
		if(mob.isMonster()) return Ability.QUALITY_INDIFFERENT;
		return abstractQuality();
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(target instanceof MOB)
            {
                if((((MOB)target).numFollowers()>0)||(((MOB)target).isMonster()))
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob,target);
    }

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((mob.numFollowers()>0)||(mob.isMonster()))
		{
			mob.tell("You cannot have any followers when casting this spell.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

        int experienceToLose=getXPCOSTAdjustment(mob,100);
		CMLib.leveler().postExperience(mob,null,null,-experienceToLose,false);

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> call(s) for a familiar.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB target = determineMonster(mob, mob.envStats().level());
				if(target.isInCombat()) target.makePeace();
				CMLib.commands().postFollow(target,mob,true);
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
		newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
		int choice=CMLib.dice().roll(1,9,-1);
		switch(choice)
		{
		case 0:
			newMOB.setName("a dog");
			newMOB.setDisplayText("a dog is sniffing around here");
			newMOB.setDescription("She looks like a nice loyal companion.");
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'F');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Dog"));
			break;
		case 1:
			newMOB.setName("a turtle");
			newMOB.setDisplayText("a turtle is crawling around here");
			newMOB.setDescription("Not very fast, but pretty cute.");
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'F');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Turtle"));
			break;
		case 2:
			newMOB.setName("a cat");
			newMOB.setDisplayText("a cat is prowling around here");
			newMOB.setDescription("She looks busy ignoring you.");
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'F');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Cat"));
			break;
		case 3:
			newMOB.setName("a bat");
			newMOB.setDisplayText("a bat is flying around here");
			newMOB.setDescription("The darn thing just won`t stay still!");
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Bat"));
			break;
		case 4:
			newMOB.setName("a rat");
			newMOB.setDisplayText("a rat scurries nearby");
			newMOB.setDescription("Such a cute, furry little guy!");
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Rodent"));
			break;
		case 5:
			newMOB.setName("a snake");
			newMOB.setDisplayText("a snake is slithering around");
			newMOB.setDescription("..red on yellow..., how did that go again?");
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Snake"));
			break;
		case 6:
			newMOB.setName("an owl");
			newMOB.setDisplayText("an owl is flying around here");
			newMOB.setDescription("He looks wise beyond his years.");
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Owl"));
			break;
		case 7:
			newMOB.setName("a rabbit");
			newMOB.setDisplayText("a cute little rabbit is watching you");
			newMOB.setDescription("Don`t blink, or she may twitch her nose.");
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'F');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Rabbit"));
			break;
		case 8:
			newMOB.setName("a raven");
			newMOB.setDisplayText("a raven is pearched nearby");
			newMOB.setDescription("You think he`s watching you.");
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Raven"));
			break;
		}
		newMOB.recoverEnvStats();
		newMOB.recoverCharStats();
		newMOB.baseEnvStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB));
		newMOB.baseEnvStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(newMOB));
		newMOB.baseEnvStats().setDamage(CMLib.leveler().getLevelMOBDamage(newMOB));
		newMOB.baseEnvStats().setSpeed(CMLib.leveler().getLevelMOBSpeed(newMOB));
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		CMLib.factions().setAlignment(newMOB,Faction.ALIGN_GOOD);
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
		CMLib.beanCounter().clearZeroMoney(newMOB,null);
		newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
		caster.location().recoverRoomStats();
		return(newMOB);


	}
}
