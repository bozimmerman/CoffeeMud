package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Spell_WaterBreathing extends Spell
{

	public Spell_WaterBreathing()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Water Breathing";
		displayText="(Water Breathing)";
		miscText="";

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;

		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.OK_OTHERS;

		baseEnvStats().setLevel(4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_WaterBreathing();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		mob.tell(mob,null,"Your gills disappear.");
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if((mob.location()!=null)
		&&((mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)||(mob.location().domainType()==Room.DOMAIN_INDOORS_UNDERWATER)))
			if((mob.envStats().sensesMask()&EnvStats.CAN_BREATHE)==EnvStats.CAN_BREATHE)
				affectableStats.setSensesMask(affectableStats.sensesMask()-EnvStats.CAN_BREATHE);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> whistle(s) to <T-NAMESELF>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> grow(s) a pair of gills!");
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> whistle(s) to <T-NAMESELF>, but nothing happens.");

		return success;
	}
}
