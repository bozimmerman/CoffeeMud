package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Spell_FleshStone extends Spell
	implements AlterationDevotion
{

	String previousDisplayText="";

	public Spell_FleshStone()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Flesh Stone";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Flesh to Stone)";


		malicious=true;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(19);

		addQualifyingClass(new Mage().ID(),19);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_FleshStone();
	}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(affect.amISource(mob))
		{
			if((affect.sourceType()!=Affect.AREA)
			&&(affect.sourceType()!=Affect.GENERAL)
			&&(affect.sourceType()!=Affect.NO_EFFECT))
			{
				mob.tell("Statues can't do that.");
				return false;
			}
		}
		if(affect.amITarget(mob))
		{
			if((affect.targetCode()==Affect.SOUND_MAGIC)
			   &&(affect.tool()!=null)
			   &&(affect.tool() instanceof Spell_StoneFlesh))
			{
				affect.source().tell("The statue seems to smile.");
			}
			else
			if((affect.targetType()!=Affect.AREA)
			&&(affect.targetType()!=Affect.GENERAL)
			&&(affect.targetCode()!=Affect.VISUAL_LOOK)
			&&(affect.targetCode()!=Affect.VISUAL_READ)
			&&(affect.targetCode()!=Affect.SOUND_WORDS)
			&&(affect.targetCode()!=Affect.SOUND_NOISE)
			&&(affect.targetType()!=Affect.NO_EFFECT))
			{
				affect.source().tell("You can't do that to a statue.");
				return false;
			}
		}

		mob.recoverMaxState();
		mob.curState().setHunger(1000);
		mob.curState().setThirst(1000);
		mob.recoverCharStats();
		mob.recoverEnvStats();
		if(mob.isInCombat())
			mob.makePeace();

		return super.okAffect(affect);
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setSensesMask(affectableStats.sensesMask()|Sense.CAN_MOVE);
		affectableStats.setSensesMask(affectableStats.sensesMask()|Sense.CAN_HEAR);
		affectableStats.setSensesMask(affectableStats.sensesMask()|Sense.CAN_SMELL);
		affectableStats.setSensesMask(affectableStats.sensesMask()|Sense.CAN_SPEAK);
		affectableStats.setSensesMask(affectableStats.sensesMask()|Sense.CAN_TASTE);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		mob.tell("Your flesh returns to normal!");
		mob.curState().setHitPoints(1);
		mob.curState().setMana(0);
		mob.curState().setMovement(0);
		mob.curState().setHunger(0);
		mob.curState().setThirst(0);
		mob.setDisplayText(previousDisplayText);
		Movement.standIfNecessary(mob);
	}



	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

		// if they can't hear the sleep spell, it
		// won't happen
		if(!Sense.canBeHeardBy(mob,target))
		{
			mob.tell(target.charStats().HeShe()+" can't hear your words.");
			return false;
		}


		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands))
			return false;


		boolean success=profficiencyCheck(-(target.envStats().level()));

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.STRIKE_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> encant(s) at <T-NAME>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					int a=0;
					while(a<target.numAffects())
					{
						Ability A=target.fetchAffect(a);
						int s=target.numAffects();
						A.unInvoke();
						if(target.numAffects()==s)
							a++;
					}
					target.makePeace();
					Movement.standIfNecessary(target);
					previousDisplayText=((MOB)target).displayText();
					target.setDisplayText("A statue of "+target.name()+" stands here.");
					success=maliciousAffect(mob,target,mob.envStats().level()*50,-1);
					if(success)
						mob.location().show(mob,target,Affect.VISUAL_WNOISE,"<T-NAME> turn(s) into stone!!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> encant(s) at <T-NAME>, but the spell fades.");

		// return whether it worked
		return success;
	}
}
