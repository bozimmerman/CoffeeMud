package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Flameshield extends Spell
{
	public Spell_Flameshield()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Flameshield";
		displayText="(Flameshield)";

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;
		
		baseEnvStats().setLevel(9);
		quality=Ability.BENEFICIAL_OTHERS;

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Flameshield();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_EVOCATION;
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked)
			mob.tell("Your flame shield disappears.");
	}

	public void affect(Affect affect)
	{
		super.affect(affect);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		MOB mob=(MOB)affected;
		if(affect.target()==null) return;
		if(affect.source()==null) return;
		MOB source=affect.source();
		if(source.location()==null) return;
		

		if(affect.amITarget(mob))
		{
			if(Util.bset(affect.targetCode(),Affect.AFF_TOUCHED)
			   &&(affect.targetMessage()!=null)
			   &&(affect.source().rangeToTarget()==0)
			   &&(affect.targetMessage().length()>0))
			{
				if((Dice.rollPercentage()>(source.charStats().getStat(CharStats.DEXTERITY)*3)))
				{
					FullMsg msg=new FullMsg(source,mob,this,affectType,null);
					if(source.location().okAffect(msg))
					{
						source.location().send(source,msg);
						if(invoker==null) invoker=source;
						if(!msg.wasModified())
						{
							int damage = Dice.roll(1,(int)Math.round(new Integer(invoker.envStats().level()).doubleValue()/3.0),1);
							ExternalPlay.postDamage(mob,source,this,damage,Affect.ACT_GENERAL|Affect.TYP_FIRE,Weapon.TYPE_BURNING,"The flame shield around <S-NAME> flares and <DAMAGE> <T-NAME>!");
						}
					}
				}
			}

		}
		return;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		MOB mob=(MOB)affected;

		affectableStats.setArmor(affectableStats.armor()-mob.envStats().level());
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType,(auto?"":"<S-NAME> incant(s) and wave(s) <S-HIS-HER> arms.  ")+"A field of flames erupt(s) around <T-NAME>!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> incant(s) and wave(s) <S-HIS-HER> arms, but only sparks emerge.");


		// return whether it worked
		return success;
	}
}