package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Repulsion extends Spell
{
	public String ID() { return "Spell_Repulsion"; }
	public String name(){return "Repulsion";}
	public String displayText(){return "(Repulsion)";}
	public int maxRange(){return 3;}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_Repulsion();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ABJURATION;}

	public int amountRemaining=0;

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(affect.amISource(mob))
		{
			if(affect.sourceMinor()==Affect.TYP_ADVANCE)
			{
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> struggle(s) against the repulsion field.");
				amountRemaining-=mob.charStats().getStat(CharStats.STRENGTH);
				if(amountRemaining<0)
					unInvoke();
				else
					return false;
			}
		}
		return super.okAffect(affect);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked)
		{
			mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to break <S-HIS-HER> way free of the repulsion field.");
			ExternalPlay.standIfNecessary(mob);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Hashtable h=ExternalPlay.properTargets(this,mob,auto);
		if((h==null)||(h.size()==0))
		{
			mob.tell("There doesn't appear to be anyone here worth repelling.");
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
			mob.location().show(mob,null,affectType(auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> arms and cast(s) a spell.^?");
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
				if((mob.location().okAffect(msg))&&(target.fetchAffect(this.ID())==null))
				{
					mob.location().send(mob,msg);
					if(!msg.wasModified())
					{
						amountRemaining=130;
						if(target.location()==mob.location())
						{
							success=maliciousAffect(mob,target,(mob.envStats().level()*10),-1);
							target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> become(s) repelled!");
							if((target.getVictim()!=null)&&(target.rangeToTarget()>0))
								target.setAtRange(target.rangeToTarget());
							else
							if(target.location().maxRange()<2)
								target.setAtRange(target.location().maxRange());
							else
								target.setAtRange(2);
							if(target.getVictim()!=null)
								target.getVictim().setAtRange(target.rangeToTarget());
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> incant(s), but the spell fizzles.");


		// return whether it worked
		return success;
	}
}