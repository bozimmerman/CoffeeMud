package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_ProtUndead extends Prayer
{
	public String ID() { return "Prayer_ProtUndead"; }
	public String name(){ return "Protection Undead";}
	public String displayText(){ return "(Protection from Undead)";}
	public int quality(){ return OK_SELF;}
	public int holyQuality(){ return HOLY_NEUTRAL;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prayer_ProtUndead();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return false;

		if(invoker==null)
			return false;

		MOB mob=(MOB)affected;

		if(mob.charStats().getMyRace().racialCategory().equalsIgnoreCase("Undead"))
		{
			int damage=(int)Math.round(Util.div(mob.envStats().level(),3.0));
			ExternalPlay.postDamage(invoker,mob,this,damage,Affect.MASK_GENERAL|Affect.TYP_CAST_SPELL,Weapon.TYPE_BURSTING,"^S<T-HIS-HER> protective aura <DAMAGE> <T-NAME>!^?");
		}
		return super.tick(ticking,tickID);
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
			if(victim.charStats().getMyRace().racialCategory().equalsIgnoreCase("Undead"))
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

		if(canBeUninvoked())
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

		Environmental target=mob;
		if((auto)&&(givenTarget!=null)) target=givenTarget;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> become(s) protected from the undead.":"^S<S-NAME> "+prayWord(mob)+" for protection from the undead.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for protection, but there is no answer.");


		// return whether it worked
		return success;
	}
}
