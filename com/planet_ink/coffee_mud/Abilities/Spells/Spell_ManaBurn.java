package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_ManaBurn extends Spell
{
	int curMana=0;
	public Spell_ManaBurn()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Mana Burn";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Mana Burn)";

		quality=Ability.MALICIOUS;


		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;

		baseEnvStats().setLevel(6);

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_ManaBurn();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;
	}

	public boolean okAffect(Affect affect)
	{
		adjustMana();
		return super.okAffect(affect);
	}

	public void affect(Affect affect)
	{
		adjustMana();
		super.affect(affect);
	}

	public boolean tick(int tickID)
	{
		adjustMana();
		return super.tick(tickID);
	}
	
	public void adjustMana()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked)
		{
			if(mob.curState().getMana()<curMana)
				mob.curState().adjMana(mob.curState().getMana()-curMana,mob.maxState());
			curMana=mob.curState().getMana();
		}
		
	}
	
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		mob.tell("You feel less drained.");
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		int levelDiff=target.envStats().level()-mob.envStats().level();
		if((!target.mayIFight(mob))||(levelDiff>=10))
		{
			mob.tell(target.charStats().HeShe()+" looks too powerful.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(-((target.charStats().getStat(CharStats.INTELLIGENCE))+(levelDiff*5)),auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened. 
			String str=auto?"":"<S-NAME> incant(s) hotly at <T-NAMESELF>";
			FullMsg msg=new FullMsg(mob,target,this,affectType,str);
			FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_MIND|(auto?Affect.ACT_GENERAL:0),null);
			if((mob.location().okAffect(msg))&&(mob.location().okAffect(msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((!msg.wasModified())&&(!msg2.wasModified()))
				{
					target.curState().adjMana(-50,target.maxState());
					curMana=target.curState().getMana();
					success=maliciousAffect(mob,target,0,-1);
					if(success)
						mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> seem(s) drained!");
				}
			}
		}
		if(!success)
			return maliciousFizzle(mob,target,"<S-NAME> incant(s) hotly at <T-NAMESELF>, but nothing happens.");

		// return whether it worked
		return success;
	}
}
