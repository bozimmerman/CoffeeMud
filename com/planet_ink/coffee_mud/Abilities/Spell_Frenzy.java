package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Spell_Frenzy extends Spell
	implements CharmDevotion
{
	public int hpAdjustment=0;

	public Spell_Frenzy()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Frenzy";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Frenzy spell)";


		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(13);

		addQualifyingClass(new Mage().ID(),13);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Frenzy();
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDamage(affectableStats.damage()+invoker.envStats().level());
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+invoker.envStats().level());
		affectableStats.setArmor(affectableStats.armor()+invoker.envStats().level());
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		if(mob.curState().getHitPoints()<=hpAdjustment)
			mob.curState().setHitPoints(1);
		else
			mob.curState().adjHitPoints(-hpAdjustment,mob.maxState());
		mob.tell(mob,null,"You fell calmer.");
	}



	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> chant(s) to <T-NAME>!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.VISUAL_WNOISE,"<T-NAME> go(es) wild!");
				hpAdjustment=(int)Math.round(Util.div(mob.maxState().getHitPoints(),5.0));
				beneficialAffect(mob,target,0);
				mob.curState().setHitPoints(mob.curState().getHitPoints()+hpAdjustment);
				hpAdjustment=0;
			}
		}
		else
			return beneficialFizzle(mob,target,"<S-NAME> chant(s) wildly to <T-NAME>, but nothing more happens.");

		// return whether it worked
		return success;
	}
}
