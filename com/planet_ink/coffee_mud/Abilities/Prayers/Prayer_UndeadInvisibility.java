package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_UndeadInvisibility extends Prayer
{
	public String ID() { return "Prayer_UndeadInvisibility"; }
	public String name(){ return "Invisibility to Undead";}
	public String displayText(){ return "(Inivisbility to Undead)";}
	public int quality(){ return OK_SELF;}
	public int holyQuality(){ return HOLY_EVIL;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prayer_UndeadInvisibility();}

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
				affectableStats.setArmor(affectableStats.armor()-20);
		}
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(((affect.targetCode()&Affect.MASK_MALICIOUS)>0)
		&&((affect.amITarget(affected))))
		{
			MOB target=(MOB)affect.target();
			if((!target.isInCombat())
			   &&(affect.source().charStats().getMyRace().racialCategory().equals("Undead"))
			   &&(affect.source().getVictim()!=target))
			{
				affect.source().tell("You don't see "+target.displayName());
				if(target.getVictim()==affect.source())
				{
					target.makePeace();
					target.setVictim(null);
					helpProfficiency((MOB)affected);
				}
				return false;
			}
		}
		return super.okAffect(myHost,affect);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your invisibility to undead fades.");
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already affected by "+displayName()+".");
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> become(s) invisible to the undead.":"^S<S-NAME> "+prayWord(mob)+" for invisibility to the undead.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for invisibility to the undead, but there is no answer.");


		// return whether it worked
		return success;
	}
}
