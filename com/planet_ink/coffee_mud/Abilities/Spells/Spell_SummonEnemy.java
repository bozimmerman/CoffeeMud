package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_SummonEnemy extends Spell
{
	MOB theMonster=null;
	
	public Spell_SummonEnemy()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Summon Enemy";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Enemy Summoning)";

		quality=Ability.INDIFFERENT;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(7);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_SummonMonster();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_CONJURATION;
	}
	
	public void unInvoke()
	{
		super.unInvoke();
		if(theMonster!=null)
		{
			MOB monster=theMonster;
			theMonster=null;
			monster.destroy();
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"":"<S-NAME> chant(s) and conjur(s) the dark shadow of a living creature...");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				MOB target = determineMonster(mob, mob.envStats().level());
				if(target!=null)
				{
					theMonster=target;
					beneficialAffect(mob,target,0);
					theMonster.setVictim(mob);
				}
				else
					mob.tell("Your equal could not be summoned.");
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) and conjur(s), but nothing happens.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster, int level)
	{
		if(caster==null) return null;
		if(caster.location()==null) return null;
		if(caster.location().getArea()==null) return null;
		MOB monster=null;
		int tries=10000000;
		while((monster==null)&&((--tries)>0))
		{
			Room room=CMMap.getRoom(Dice.roll(1,CMMap.numRooms(),-1));
			if((room!=null)&&(room.numInhabitants()>0))
			{
				MOB mob=room.fetchInhabitant(Dice.roll(1,room.numInhabitants(),-1));
				if((mob!=null)
				&&(mob.envStats().level()>=level)
				&&(mob.charStats()!=null)
				&&(mob.charStats().getMyRace()!=null)
				&&(!mob.charStats().getMyRace().playerSelectable())
				&&(Math.abs(new Integer(mob.getAlignment()-caster.getAlignment()).doubleValue())>350.0))
					monster=mob;
			}
		}
		if(monster==null) return null;
		monster=(MOB)monster.copyOf();
		monster.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		monster.setStartRoom(null);
		monster.recoverCharStats();
		monster.recoverEnvStats();
		monster.recoverMaxState();
		monster.resetToMaxState();
		monster.text();
		monster.bringToLife(caster.location());
		caster.location().recoverRoomStats();
		return(monster);
	}
}
