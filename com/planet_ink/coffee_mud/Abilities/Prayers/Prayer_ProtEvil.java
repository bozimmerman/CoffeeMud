package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_ProtEvil extends Prayer
{
	public String ID() { return "Prayer_ProtEvil"; }
	public String name(){ return "Protection Evil";}
	public String displayText(){ return "(Protection from evil)";}
	public int quality(){ return OK_SELF;}
	public int holyQuality(){ return HOLY_GOOD;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prayer_ProtEvil();}

	public boolean tick(int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return false;

		if(invoker==null)
			return false;

		MOB mob=(MOB)affected;

		if(mob.getAlignment()<350)
		{
			int damage=(int)Math.round(Util.div(mob.envStats().level(),3.0));
			ExternalPlay.postDamage(invoker,mob,this,damage,Affect.MASK_GENERAL|Affect.TYP_CAST_SPELL,Weapon.TYPE_BURSTING,"^S<T-HIS-HER> protective aura <DAMAGE> <T-NAME>!^?");
		}
		return super.tick(tickID);
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;
		if(invoker==null) return true;
		if(affected==null) return true;
		if(!(affected instanceof MOB)) return true;

		if((affect.target()==invoker)&&(affect.source()!=invoker))
		{
			if((Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
			&&(affect.targetMinor()==Affect.TYP_CAST_SPELL)
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Prayer))
			{
				Prayer bob=(Prayer)affect.tool();
				if(bob.holyQuality()==Prayer.HOLY_EVIL)
				{
					affect.source().location().show(invoker,null,Affect.MSG_OK_VISUAL,"The holy field around <S-NAME> protect(s) <S-HIM-HER> from the evil magic attack of "+affect.source().name()+".");
					return false;
				}
			}

		}
		return true;
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
			if(victim.getAlignment()<350)
				affectableStats.setArmor(affectableStats.armor()-10);
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
			mob.tell("Your protection from evil fades.");
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> become(s) protected from evil.":"^S<S-NAME> "+prayWord(mob)+" for protection from evil.^?");
			if(mob.location().okAffect(msg))
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
