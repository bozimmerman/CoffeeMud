package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_CombatPrecognition extends Spell
{
	public String ID() { return "Spell_CombatPrecognition"; }
	public String name(){return "Combat Precognition";}
	public String displayText(){return "(Combat Precognition)";}
	public int quality(){return BENEFICIAL_SELF;};
	protected int canAffectCode(){return CAN_MOBS;}
	boolean lastTime=false;
	public Environmental newInstance(){	return new Spell_CombatPrecognition();}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_DIVINATION;}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(affect.amITarget(mob)
		   &&(mob.location()!=null)
		   &&(Sense.aliveAwakeMobile(mob,true)))
		{
			if(affect.targetMinor()==Affect.TYP_WEAPONATTACK)
			{
				FullMsg msg=new FullMsg(mob,affect.source(),null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> avoid(s) the attack by <T-NAME>!");
				if((profficiencyCheck(mob.charStats().getStat(CharStats.DEXTERITY)-60,false))
				&&(!lastTime)
				&&(affect.source().getVictim()==mob)
				&&(affect.source().rangeToTarget()==0)
				&&(mob.location().okAffect(msg)))
				{
					lastTime=true;
					mob.location().send(mob,msg);
					helpProfficiency(mob);
					return false;
				}
				else
					lastTime=false;
			}
			else
			if((!affect.wasModified())
			   &&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
			   &&(profficiencyCheck(mob.charStats().getStat(CharStats.DEXTERITY)-50,false)))
			{
				String tool=null;
				if((affect.tool()!=null)&&(affect.tool() instanceof Ability))
					tool=((Ability)affect.tool()).name();
				FullMsg msg2=null;
				switch(affect.targetMinor())
				{
				case Affect.TYP_GAS:
					msg2=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,"<T-NAME> avoid(s) the "+((tool==null)?"noxious fumes":tool)+" from <S-NAME>.");
					break;
				case Affect.TYP_COLD:
					msg2=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,"<T-NAME> avoid(s) the "+((tool==null)?"cold blast":tool)+" from <S-NAME>.");
					break;
				case Affect.TYP_ELECTRIC:
					msg2=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,"<T-NAME> avoid(s) the "+((tool==null)?"electrical attack":tool)+" from <S-NAME>.");
					break;
				case Affect.TYP_FIRE:
					msg2=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,"<T-NAME> avoid(s) the "+((tool==null)?"blast of heat":tool)+" from <S-NAME>.");
					break;
				case Affect.TYP_WATER:
					msg2=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,"<T-NAME> avoid(s) the "+((tool==null)?"weat blast":tool)+" from <S-NAME>.");
					break;
				case Affect.TYP_ACID:
					msg2=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,"<T-NAME> avoid(s) the "+((tool==null)?"acid attack":tool)+" from <S-NAME>.");
					break;
				}
				if((msg2!=null)&&(mob.location()!=null)&&(mob.location().okAffect(msg2)))
				{
					mob.location().send(mob,msg2);
					return false;
				}
			}
		}
		return true;
	}
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		mob.tell("Your combat precognition fades away.");
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if(target.fetchAffect(ID())!=null)
		{
			mob.tell("You already have the sight.");
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
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"<S-NAME> shout(s) combatively!":"^S<S-NAME> shout(s) a combative spell!^?"));
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> shout(s) combatively, but nothing more happens.");
		// return whether it worked
		return success;
	}
}