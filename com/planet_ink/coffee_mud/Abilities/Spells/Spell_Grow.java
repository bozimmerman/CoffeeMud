package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Grow extends Spell
{
	public String ID() { return "Spell_Grow"; }
	public String name(){return "Grow";}
	public String displayText(){return "(Grow)";}
	protected int canTargetCode(){return CAN_MOBS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_Grow();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;}
	private int oldWeight=0;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			double aff=1.0 + Util.mul(0.1,(invoker().envStats().level()));
			affectableStats.setHeight((int)Math.round(Util.mul(affectableStats.height(),aff)));
		}
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.DEXTERITY,affectableStats.getStat(CharStats.DEXTERITY)/2);
		affectableStats.setStat(CharStats.STRENGTH,affectableStats.getStat(CharStats.STRENGTH)+(invoker().envStats().level()/5));
	}

	public void unInvoke()
	{
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if(oldWeight<1)
				mob.baseCharStats().getMyRace().setHeightWeight(mob.baseEnvStats(),(char)mob.baseCharStats().getStat(CharStats.GENDER));
			else
				mob.baseEnvStats().setWeight(oldWeight);
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME> shrink(s) back down to size.");
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(target.fetchAffect(this.ID())!=null)
		{
			mob.tell(target.name()+" is already HUGE!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, encanting.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.MSG_OK_ACTION,"<T-NAME> grow(s) to an enormous size!");
				oldWeight=mob.baseEnvStats().weight();
				double aff=1.0 + Util.mul(0.1,(mob.envStats().level()));
				aff=aff*aff;
				mob.baseEnvStats().setWeight((int)Math.round(Util.mul(mob.baseEnvStats().weight(),aff)));
				beneficialAffect(mob,target,0);
				mob.confirmWearability();
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, encanting but nothing happens.");


		// return whether it worked
		return success;
	}
}