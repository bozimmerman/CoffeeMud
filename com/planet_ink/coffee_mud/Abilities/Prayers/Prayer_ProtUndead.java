package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_ProtUndead extends Prayer
{
	public Prayer_ProtUndead()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Protection Undead";
		displayText="(Protection from Undead)";

		quality=Ability.OK_SELF;
		holyQuality=Prayer.HOLY_NEUTRAL;
		baseEnvStats().setLevel(4);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_ProtUndead();
	}

	public boolean tick(int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return false;

		if(invoker==null)
			return false;

		MOB mob=(MOB)affected;

		if(mob.charStats().getMyRace().name().equalsIgnoreCase("Undead"))
		{
			int damage=(int)Math.round(Util.div(mob.envStats().level(),3.0));
			ExternalPlay.postDamage(invoker,mob,this,damage,Affect.ACT_GENERAL|Affect.TYP_CAST_SPELL,Weapon.TYPE_BURSTING,"<T-HIS-HER> protective aura <DAMAGE> <T-NAME>!");
		}
		return super.tick(tickID);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		MOB mob=(MOB)affected;

		if(mob.isInCombat())
		{
			MOB victim=mob.getVictim();
			if(victim.charStats().getMyRace().name().equalsIgnoreCase("Undead"))
				affectableStats.setArmor(affectableStats.armor()-50);
		}
	}



	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		mob.tell("Your protection from undead fades.");
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already affected by "+name()+".");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"<S-NAME> become(s) protected from the undead.":"<S-NAME> call(s) upon the protection of <S-HIS-HER> god from the undead.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> call(s) upon the protection of <S-HIS-HER> god, but there is no answer.");


		// return whether it worked
		return success;
	}
}
