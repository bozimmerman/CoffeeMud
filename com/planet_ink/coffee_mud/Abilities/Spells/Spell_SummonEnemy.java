package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_SummonEnemy extends Spell
{
	public String ID() { return "Spell_SummonEnemy"; }
	public String name(){return "Summon Enemy";}
	public String displayText(){return "(Enemy Summoning)";}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Spell_SummonEnemy();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}

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

	public void affect(Environmental myHost, Affect msg)
	{
		super.affect(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing()))
		&&(msg.sourceMinor()==Affect.TYP_QUIT))
			unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> conjur(s) the dark shadow of a living creature...^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB target = determineMonster(mob, mob.envStats().level());
				if(target!=null)
				{
					beneficialAffect(mob,target,0);
					target.setVictim(mob);
				}
				else
					mob.tell("Your equal could not be summoned.");
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> conjur(s), but nothing happens.");

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
			Room room=CMMap.getRandomRoom();
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
		monster.recoverCharStats();
		monster.recoverEnvStats();
		monster.recoverMaxState();
		monster.resetToMaxState();
		monster.text();
		monster.bringToLife(caster.location(),true);
		caster.location().recoverRoomStats();
		monster.setStartRoom(null);
		return(monster);
	}
}
