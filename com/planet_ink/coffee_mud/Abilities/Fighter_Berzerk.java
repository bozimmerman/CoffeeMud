package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Fighter_Berzerk extends StdAbility
{
	public int hpAdjustment=0;

	public Fighter_Berzerk()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Berzerk";
		displayText="(Berzerk)";
		miscText="";

		triggerStrings.addElement("BERZERK");

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(20);

		addQualifyingClass(new Fighter().ID(),20);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Fighter_Berzerk();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDamage(affectableStats.damage()+invoker.envStats().level()+invoker.envStats().level());
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+invoker.envStats().level()+invoker.envStats().level());
		affectableStats.setArmor(affectableStats.armor()+invoker.envStats().level());
	}

	public void unInvoke()
	{
		if(affecting() instanceof MOB)
		{
			MOB mob=(MOB)affecting();

			super.unInvoke();

			if(mob.curState().getHitPoints()<=hpAdjustment)
				mob.curState().setHitPoints(1);
			else
				mob.curState().adjHitPoints(-hpAdjustment,mob.maxState());
			mob.tell(mob,null,"You fell calmer.");
		}
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already berzerk.");
			return false;
		}

		if(!mob.isInCombat())
		{
			mob.tell("You aren't in combat!");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,null,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,"<S-NAME> get(s) a wild look in <S-HIS-HER> eyes!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				hpAdjustment=(int)Math.round(Util.div(mob.maxState().getHitPoints(),5.0));
				beneficialAffect(mob,mob,0);
				mob.curState().setHitPoints(mob.curState().getHitPoints()+hpAdjustment);
				hpAdjustment=0;
			}
		}
		else
			beneficialFizzle(mob,null,"<S-NAME> huff(s) and grunt(s), but can't get angry.");
		return success;
	}
}
