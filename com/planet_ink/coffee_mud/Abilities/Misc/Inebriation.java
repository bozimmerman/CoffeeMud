package com.planet_ink.coffee_mud.Abilities.Misc;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;


public class Inebriation extends StdAbility
{
	public String ID() { return "Inebriation"; }
	public String name(){ return "Inebriation";}
	public String displayText(){ return "(Inebriated)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.MALICIOUS;}
	public Environmental newInstance(){	return new Inebriation();}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected instanceof MOB)
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(int)Math.round(((MOB)affected).envStats().level()));
	}


	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.DEXTERITY,(int)Math.round(affectableStats.getStat(CharStats.DEXTERITY)-3));
	}

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null) return true;

		if((Dice.rollPercentage()<20)&&(Sense.aliveAwakeMobile(mob,true)))
		{
			if(mob.getAlignment()<350)
			switch(Dice.roll(1,9,-1))
			{
			case 0:
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> stagger(s) around making ugly faces.");
				break;
			case 1:
				mob.location().show(mob,null,Affect.MSG_NOISE,"<S-NAME> belch(es) grotesquely.");
				break;
			case 2:
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> spin(s) <S-HIS-HER> head around.");
				break;
			case 3:
				mob.location().show(mob,null,Affect.MSG_NOISE,"<S-NAME> can't stop snarling.");
				break;
			case 4:
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> just fell over!");
				break;
			case 5:
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> look(s) around with glazed over eyes.");
				break;
			case 6:
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> can't seem to focus.");
				break;
			case 7:
				mob.location().showSource(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> are definitely sh** faced!");
				mob.location().showOthers(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> is definitely sh** faced!");
				break;
			case 8:
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> stare(s) blankly at the ground.");
				break;
			}
			else
			if(mob.getAlignment()<650)
			switch(Dice.roll(1,9,-1))
			{
			case 0:
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> stagger(s) around aimlessly.");
				break;
			case 1:
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> burp(s) noncommitally.");
				break;
			case 2:
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> look(s) around with glazed over eyes.");
				break;
			case 3:
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> can't seem to focus.");
				break;
			case 4:
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> almost fell over.");
				break;
			case 5:
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> hiccup(s) and almost smile(s).");
				break;
			case 6:
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> belch(es)!");
				break;
			case 7:
				mob.location().showSource(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> are definitely drunk!");
				mob.location().showOthers(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> is definitely drunk!");
				break;
			case 8:
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> stare(s) blankly ahead.");
				break;
			}
			else
			switch(Dice.roll(1,9,-1))
			{
			case 0:
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> stagger(s) around trying to hug everyone.");
				break;
			case 1:
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> hiccup(s) and smile(s).");
				break;
			case 2:
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> bob(s) <S-HIS-HER> head back and forth.");
				break;
			case 3:
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> can't stop smiling.");
				break;
			case 4:
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> lean(s) slightly to one side.");
				break;
			case 5:
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> look(s) around with glazed over eyes.");
				break;
			case 6:
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> can't seem to focus.");
				break;
			case 7:
				mob.location().showSource(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> are definitely a bit tipsy!");
				mob.location().showOthers(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> is definitely a bit tipsy!");
				break;
			case 8:
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> stare(s) blankly at <S-HIS-HER> eyelids.");
				break;
			}

		}
		return true;
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if(affect.source()!=affected)
			return true;
		if(affect.source().location()==null)
			return true;
		if((!Util.bset(affect.targetMajor(),Affect.MASK_GENERAL))
		&&(affect.targetMajor()>0))
		{
			if((affect.target() !=null)
				&&(affect.target() instanceof MOB))
					affect.modify(affect.source(),affect.source().location().fetchInhabitant(Dice.roll(1,affect.source().location().numInhabitants(),0)-1),affect.tool(),affect.sourceCode(),affect.sourceMessage(),affect.targetCode(),affect.targetMessage(),affect.othersCode(),affect.othersMessage());
		}
		return true;
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked)
			mob.tell(mob,null,"You feel sober now.");
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
			String str=auto?"":"<S-NAME> attempt(s) to inebriate <T-NAMESELF>!";
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_NOISYMOVEMENT,str);
			if(target.location().okAffect(msg))
			{
			    target.location().send(target,msg);
				target.location().show(target,null,Affect.MSG_NOISE,"<S-NAME> burp(s)!");
				success=maliciousAffect(mob,target,0,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to inebriate <T-NAMESELF>, but fail(s).");

        return success;

	}
}