package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_SummonFlyer extends Spell
{
	public Spell_SummonFlyer()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Summon Flyer";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Summon Flyer)";

		quality=Ability.OK_SELF;

		canBeUninvoked=true;
		isAutoinvoked=false;

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=0;

		baseEnvStats().setLevel(7);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_SummonFlyer();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_CONJURATION;
	}

	public void unInvoke()
	{
		MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked)&&(mob!=null))
			mob.destroy();
	}
	
	public boolean tick(int tickID)
	{
		if(tickID==Host.MOB_TICK)
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if(((mob.amFollowing()==null)
				||(mob.location()==null)
				||(mob.amDead())
				||(invoker==null)
				||(invoker.location()==null)
				||((invoker!=null)&&(mob.location()!=invoker.location())&&(invoker.riding()!=affected))))
				{
					mob.delAffect(this);
					mob.destroy();
				}
			}
		}
		return super.tick(tickID);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.curState().getMana()<mob.maxState().getMana())
		{
			mob.tell("You need to be at full mana to cast this.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		
		mob.curState().setMana(0);

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"":"^S<S-NAME> magically call(s) for a loyal steed.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				MOB target = determineMonster(mob, mob.envStats().level());
				ExternalPlay.follow(target,mob,true);
				invoker=mob;
				target.addNonUninvokableAffect((Ability)copyOf());
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> call(s) for a steed, but choke(s) on the words.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster, int level)
	{
		
		MOB newMOB=(MOB)CMClass.getMOB("GenRideable");
		Rideable ride=(Rideable)newMOB;
		newMOB.baseEnvStats().setAbility(11);
		newMOB.baseEnvStats().setDisposition(newMOB.baseEnvStats().disposition()|EnvStats.IS_FLYING);
		newMOB.baseEnvStats().setLevel(level);
		newMOB.baseEnvStats().setWeight(500);
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Horse"));
		newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'M');
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		newMOB.setName("a flying warhorse");
		newMOB.setDisplayText("a warhorse with broad powerful wings stands here");
		newMOB.setDescription("A ferocious, fleet of foot, flying friend.");
		ride.setRideBasis(Rideable.RIDEABLE_AIR);
		ride.setMobCapacity(2);
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.setAlignment(500);
		newMOB.resetToMaxState();
		newMOB.text();
		newMOB.bringToLife(caster.location(),true);
		caster.location().recoverRoomStats();
		newMOB.setStartRoom(null);
		return(newMOB);


	}
}
