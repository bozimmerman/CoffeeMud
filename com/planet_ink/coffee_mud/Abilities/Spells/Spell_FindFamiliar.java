package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_FindFamiliar extends Spell
{
	public Spell_FindFamiliar()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Find Familiar";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Find Familiar)";

		quality=Ability.BENEFICIAL_SELF;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(7);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_FindFamiliar();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_CONJURATION;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.numFollowers()>0)
		{
			mob.tell("You cannot have any followers when casting this spell.");
			return false;
		}
		if(mob.curState().getMana()<mob.maxState().getMana())
		{
			mob.tell("You need to be at full mana to cast this.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		mob.charStats().getMyClass().loseExperience(mob,100);
		mob.curState().setMana(0);
		
		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"":"<S-NAME> chant(s) and call(s) for a familiar.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				MOB target = determineMonster(mob, mob.envStats().level());
				ExternalPlay.follow(target,mob,true);
				invoker=mob;
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) and call(s), but choke(s) on the words.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster, int level)
	{
		
		MOB newMOB=(MOB)CMClass.getMOB("GenMOB");
		newMOB.baseEnvStats().setAbility(7);
		newMOB.baseEnvStats().setLevel(level);
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'M');
		int choice=Dice.roll(1,9,-1);
		switch(choice)
		{
		case 0:
			newMOB.setName("a dog");
			newMOB.setDisplayText("a dog is sniffing around here");
			newMOB.setDescription("She looks like a nice loyal companion.");
			newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'F');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Dog"));
			break;
		case 1:
			newMOB.setName("a turtle");
			newMOB.setDisplayText("a turtle is crawling around here");
			newMOB.setDescription("Not very fast, but pretty cute.");
			newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'F');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Turtle"));
			break;
		case 2:
			newMOB.setName("a cat");
			newMOB.setDisplayText("a cat is prowling around here");
			newMOB.setDescription("She looks busy ignoring you.");
			newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'F');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Cat"));
			break;
		case 3:
			newMOB.setName("a bat");
			newMOB.setDisplayText("a bat is flying around here");
			newMOB.setDescription("The darn thing just won`t stay still!");
			newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'M');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Bat"));
			break;
		case 4:
			newMOB.setName("a rat");
			newMOB.setDisplayText("a rat scurries nearby");
			newMOB.setDescription("Such a cute, furry little guy!");
			newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'M');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Rodent"));
			break;
		case 5:
			newMOB.setName("a snake");
			newMOB.setDisplayText("a snake is slithering around");
			newMOB.setDescription("..red on yellow..., how did that go again?");
			newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'M');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Snake"));
			break;
		case 6:
			newMOB.setName("an owl");
			newMOB.setDisplayText("a own is flying around here");
			newMOB.setDescription("He looks wise beyond his years.");
			newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'M');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Owl"));
			break;
		case 7:
			newMOB.setName("a rabbit");
			newMOB.setDisplayText("a cute little rabbit is watching you");
			newMOB.setDescription("Don`t blink, or she may twitch her nose.");
			newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'F');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Rabbit"));
			break;
		case 8:
			newMOB.setName("a raven");
			newMOB.setDisplayText("a raven is pearched nearby");
			newMOB.setDescription("You think he`s watching you.");
			newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'M');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Raven"));
			break;
		}
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		newMOB.setAlignment(1000);
		newMOB.setStartRoom(null);
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		Ability A=CMClass.getAbility("Prop_Familiar");
		A.setMiscText(""+choice);
		newMOB.addNonUninvokableAffect(A);
		newMOB.text();
		newMOB.bringToLife(caster.location());
		caster.location().recoverRoomStats();
		return(newMOB);


	}
}
