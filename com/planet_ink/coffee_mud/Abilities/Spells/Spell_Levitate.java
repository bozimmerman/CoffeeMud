package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Levitate extends Spell
{
	public String ID() { return "Spell_Levitate"; }
	public String name(){return "Levitate";}
	public String displayText(){return "(Levitated)";}
	public int maxRange(){return 5;}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS|CAN_ITEMS;}
	public Environmental newInstance(){	return new Spell_Levitate();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_EVOCATION;}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(affect.amISource(mob))
		{
			if((affect.sourceMinor()==Affect.TYP_ADVANCE)
			||(affect.sourceMinor()==Affect.TYP_RETREAT)
			||(affect.sourceMinor()==Affect.TYP_LEAVE)
			||(affect.sourceMinor()==Affect.TYP_ENTER)
			||(affect.sourceMinor()==Affect.TYP_RETREAT))
			{
				mob.tell("You can't seem to go anywhere!");
				return false;
			}
		}
		return super.okAffect(myHost,affect);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_FLYING);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
		{
			super.unInvoke();
			return;
		}
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> float(s) back down.");
			ExternalPlay.standIfNecessary(mob);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=super.getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;
		if(target instanceof Item)
		{
			if(mob.isMine(target))
			{
				mob.tell("You'd better set it down first!");
				return false;
			}
		}
		else
		if(target instanceof MOB)
		{
		}
		else
		{
			mob.tell("You can't levitate "+target.displayName()+"!");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> arms and cast(s) a spell.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					success=maliciousAffect(mob,target,0,-1);
					if(target instanceof MOB)
						((MOB)target).location().show((MOB)target,null,Affect.MSG_OK_ACTION,"<S-NAME> float(s) straight up!");
					else
						mob.location().showHappens(Affect.MSG_OK_ACTION,target.displayName()+" float(s) straight up!");
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> incant(s), but the spell fizzles.");
		// return whether it worked
		return success;
	}
}