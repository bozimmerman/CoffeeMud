package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.Spells.interfaces.*;
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

		addQualifyingClass("Mage",13);
		addQualifyingClass("Ranger",baseEnvStats().level()+4);

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

	public void peaceAt(MOB mob)
	{
		Room room=mob.location();
		if(room==null) return;
		for(int m=0;m<room.numInhabitants();m++)
		{
			MOB inhab=room.fetchInhabitant(m);
			if((inhab!=null)&&(inhab.getVictim()==mob))
				inhab.setVictim(null);
		}
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

		if((affect.amISource(mob))&&(!Util.bset(affect.sourceMajor(),Affect.ACT_GENERAL)))
		{
			if(affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
			{
				mob.tell("You are unable to attack in this semi-incorporeal form.");
				peaceAt(mob);
				return false;
			}
			else
			if((Util.bset(affect.sourceMajor(),Affect.ACT_HANDS))
			||(Util.bset(affect.sourceMajor(),Affect.ACT_EARS))
			||(Util.bset(affect.sourceMajor(),Affect.ACT_MOUTH)))
			{
				if(Util.bset(affect.sourceMajor(),Affect.ACT_SOUND))
					mob.tell("You are unable to make sounds in this semi-incorporeal form.");
				else
					mob.tell("You are unable to do that this semi-incorporeal form.");
				peaceAt(mob);
				return false;
			}
		}
		else
		if((affect.amITarget(mob))&&(!affect.amISource(mob))&&(!Util.bset(affect.targetMajor(),Affect.ACT_GENERAL)))
		{
			mob.tell(mob.name()+" doesn't seem to be here.");
			return false;
		}
		return true;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_INVISIBLE);
		affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_SEEN);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> point(s) to <T-NAMESELF> and yell(s) for death!");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			target.makePeace();
			peaceAt(target);
			deathRoom=mob.location();
			Body=(DeadBody)CMClass.getItem("Corpse");
			beneficialAffect(mob,target,0);

			while(target.numFollowers()>0)
			{
				MOB follower=target.fetchFollower(0);
				if(follower!=null)
					follower.setFollowing(null);
			}
			deathRoom.show(target,null,Affect.MSG_OK_ACTION,"^Z"+target.name()+" is DEAD!!!\n\r");
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