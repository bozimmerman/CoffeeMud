package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;


public class Spell_FeignDeath extends Spell
	implements IllusionistDevotion
{

	public DeadBody Body=null;
	public Room deathRoom=null;

	public Spell_FeignDeath()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Feign Death";
		displayText="(Feign Death)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(13);

		addQualifyingClass(new Mage().ID(),13);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_FeignDeath();
	}
	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		mob.tell(mob,null,"Your death is no longer feigned.");
		if((Body!=null)&&(deathRoom!=null)&&(deathRoom.isContent(Body)))
		{
			Body.destroyThis();
			deathRoom.recoverRoomStats();
		}
		super.unInvoke();
	}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;
		MOB mob=(MOB)affected;

		if(affect.amISource(mob))
		{
			switch(affect.sourceType())
			{
			case Affect.STRIKE:
				mob.tell("You are unable to attack in this semi-incorporeal form.");
				return false;
			case Affect.HANDS:
				mob.tell("You are unable to do that this semi-incorporeal form.");
				return false;
			case Affect.TASTE:
				mob.tell("You are unable to do that in this semi-incorporeal form.");
				return false;
			case Affect.SOUND:
				mob.tell("You are unable to make sounds in this semi-incorporeal form.");
				return false;
			default:
				break;
			}
		}
		else
		if(affect.amITarget(mob))
		{
			switch(affect.targetType())
			{
			case Affect.STRIKE:
				mob.tell("That doesn't seem to be here.");
				return false;
			case Affect.HANDS:
				mob.tell("That doesn't seem to be here.");
				return false;
			case Affect.TASTE:
				mob.tell("That doesn't seem to be here.");
				return false;
			default:
				break;
			}
		}
		return true;
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_INVISIBLE);
		affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_SEEN);
	}


	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,"<S-NAME> point(s) to <T-NAME> and yell(s) for death!");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			target.makePeace();
			deathRoom=mob.location();
			Body=new DeadBody();
			beneficialAffect(mob,mob,0);

			while(target.numFollowers()>0)
				target.fetchFollower(0).setFollowing(null);
			deathRoom.show(target,null,Affect.VISUAL_WNOISE,target.name()+" is DEAD!!!\n\r");
			Body.setName("the body of "+target.name());
			Body.setDisplayText("the body of "+target.name()+" lies here.");
			Body.baseEnvStats().setWeight(target.envStats().weight()+100);
			deathRoom.addItem(Body);
			Body.recoverEnvStats();
			Body.startTicker(deathRoom);
			deathRoom.recoverRoomStats();
		}

		return success;
	}
}