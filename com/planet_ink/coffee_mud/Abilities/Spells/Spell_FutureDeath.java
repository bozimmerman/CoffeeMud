package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_FutureDeath extends Spell
{
	public String ID() { return "Spell_FutureDeath"; }
	public String name(){return "Future Death";}
	public int quality(){return MALICIOUS;};
	protected int canTargetCode(){return CAN_MOBS;}
	protected int canAffectCode(){return 0;}
	public Environmental newInstance(){	return new Spell_FutureDeath();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_DIVINATION;}


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
		boolean success=profficiencyCheck(-((target.charStats().getStat(CharStats.WISDOM)*2)+(levelDiff*5)),auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			String str=auto?"":"^S<S-NAME> incant(s) at <T-NAMESELF>^?";
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),str);
			FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_MIND|(auto?Affect.MASK_GENERAL:0),null);
			if((mob.location().okAffect(msg))&&(mob.location().okAffect(msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((!msg.wasModified())&&(!msg2.wasModified()))
				{
					str=null;
					switch(Dice.roll(1,10,-1))
					{
					case 1:
						str="<S-NAME> grab(s) at <S-HIS-HER> throat and choke(s) to death!";
						break;
					case 2:
						str="<S-NAME> wave(s) <S-HIS-HER> arms and look(s) down as if falling. Then <S-HE-SHE> hit(s).";
						break;
					case 3:
						str="<S-NAME> defend(s) <S-HIM-HERSELF> from unseen blows, then fall(s) dead.";
						break;
					case 4:
						str="<S-NAME> gasp(s) for breathe, as if underwater, and drown(s).";
						break;
					case 5:
						str="<S-NAME> kneel(s) and lower(s) <S-HIS-HER> head, as if on the block.  In one last whimper, <S-HE-SHE> dies.";
						break;
					case 6:
						str="<S-NAME> jerk(s) as if being struck by a thousand arrows, and die(s).";
						break;
					case 7:
						str="<S-NAME> writhe(s) as if being struck by a powerful electric spell, and die(s).";
						break;
					case 8:
						str="<S-NAME> lie(s) on the ground, take(s) on a sickly expression, and die(s).";
						break;
					case 9:
						str="<S-NAME> grab(s) <S-HIS-HER> chest, and then it stops.";
						break;
					case 10:
						str="<S-NAME> run(s) an unseen blade along <S-HIS-HER> wrists, and die(s).";
						break;
					}
					target.location().show(target,null,Affect.MSG_OK_VISUAL,str);
					ExternalPlay.postDeath(mob,target,null);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> incant(s) at <T-NAMESELF>, but nothing happens.");

		// return whether it worked
		return success;
	}
}
