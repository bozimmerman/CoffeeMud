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
	protected int overrideMana(){return 100;}
	boolean lastTime=false;
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_DIVINATION;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(msg.amITarget(mob)
		   &&(mob.location()!=null)
		   &&(Sense.aliveAwakeMobile(mob,true)))
		{
			if(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
			{
				FullMsg msg2=new FullMsg(mob,msg.source(),null,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> avoid(s) the attack by <T-NAME>!");
				if((profficiencyCheck(null,mob.charStats().getStat(CharStats.DEXTERITY)-60,false))
				&&(!lastTime)
				&&(msg.source().getVictim()==mob)
				&&(msg.source().rangeToTarget()==0)
				&&(mob.location().okMessage(mob,msg2)))
				{
					lastTime=true;
					mob.location().send(mob,msg2);
					helpProfficiency(mob);
					return false;
				}
				else
					lastTime=false;
			}
			else
			if((msg.value()<=0)
			   &&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
			   &&((mob.fetchAbility(ID())==null)||profficiencyCheck(null,mob.charStats().getStat(CharStats.DEXTERITY)-50,false)))
			{
				String tool=null;
				if((msg.tool()!=null)&&(msg.tool() instanceof Ability))
					tool=((Ability)msg.tool()).name();
				FullMsg msg2=null;
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_JUSTICE:
					if((Util.bset(msg.targetCode(),CMMsg.MASK_MOVE))
					&&(tool!=null))
						msg2=new FullMsg(mob,msg.source(),CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> avoid(s) the "+((tool==null)?"physical":tool)+" from <T-NAME>.");
					break;
				case CMMsg.TYP_GAS:
					msg2=new FullMsg(mob,msg.source(),CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> avoid(s) the "+((tool==null)?"noxious fumes":tool)+" from <T-NAME>.");
					break;
				case CMMsg.TYP_COLD:
					msg2=new FullMsg(mob,msg.source(),CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> avoid(s) the "+((tool==null)?"cold blast":tool)+" from <T-NAME>.");
					break;
				case CMMsg.TYP_ELECTRIC:
					msg2=new FullMsg(mob,msg.source(),CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> avoid(s) the "+((tool==null)?"electrical attack":tool)+" from <T-NAME>.");
					break;
				case CMMsg.TYP_FIRE:
					msg2=new FullMsg(mob,msg.source(),CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> avoid(s) the "+((tool==null)?"blast of heat":tool)+" from <T-NAME>.");
					break;
				case CMMsg.TYP_WATER:
					msg2=new FullMsg(mob,msg.source(),CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> avoid(s) the "+((tool==null)?"weat blast":tool)+" from <T-NAME>.");
					break;
				case CMMsg.TYP_ACID:
					msg2=new FullMsg(mob,msg.source(),CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> avoid(s) the "+((tool==null)?"acid attack":tool)+" from <T-NAME>.");
					break;
				}
				if((msg2!=null)&&(mob.location()!=null)&&(mob.location().okMessage(mob,msg2)))
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
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> already <S-HAS-HAVE> the sight.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"<T-NAME> shout(s) combatively!":"^S<S-NAME> shout(s) a combative spell!^?"));
			if(mob.location().okMessage(mob,msg))
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