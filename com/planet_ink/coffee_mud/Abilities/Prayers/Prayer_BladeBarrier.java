package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_BladeBarrier extends Prayer
{
	public String ID() { return "Prayer_BladeBarrier"; }
	public String name(){ return "Blade Barrier";}
	public String displayText(){ return "(Blade Barrier)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){ return BENEFICIAL_SELF;}
	public int holyQuality(){ return HOLY_GOOD;}
	String lastMessage=null;
	public Environmental newInstance(){	return new Prayer_BladeBarrier();}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your blade barrier disappears.");
	}

	public void affect(Affect affect)
	{
		super.affect(affect);
		if((invoker==null)
		||(affected==null)
		||(!(affected instanceof MOB)))
			return;
		if(affect.target()==invoker)
		{
			if((Dice.rollPercentage()>60+affect.source().charStats().getStat(CharStats.DEXTERITY))
			&&(affect.source().rangeToTarget()==0)
			&&((lastMessage==null)||(!lastMessage.startsWith("The blade barrier around")))
			&&((Util.bset(affect.targetMajor(),Affect.MASK_HANDS))
			   ||(Util.bset(affect.targetMajor(),Affect.MASK_MOVE))))
			{
				int level=(int)Math.round(Util.div(invoker.envStats().level(),6.0));
				if(level>5) level=5;
				int damage=Dice.roll(2,level,0);
				StringBuffer hitWord=new StringBuffer(CommonStrings.standardHitWord(-1,damage));
				if(hitWord.charAt(hitWord.length()-1)==')')
					hitWord.deleteCharAt(hitWord.length()-1);
				if(hitWord.charAt(hitWord.length()-2)=='(')
					hitWord.deleteCharAt(hitWord.length()-2);
				if(hitWord.charAt(hitWord.length()-3)=='(')
					hitWord.deleteCharAt(hitWord.length()-3);
				ExternalPlay.postDamage((MOB)affect.target(),affect.source(),this,damage,Affect.MSG_OK_ACTION,Weapon.TYPE_SLASHING,"The blade barrier around <S-NAME> slices and <DAMAGE> <T-NAME>.");
				lastMessage="The blade barrier around";
			}
			else
				lastMessage=affect.othersMessage();
		}
		else
			lastMessage=affect.othersMessage();
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
		MOB target=mob;
		if(target==null) return false;
		if(target.fetchAffect(ID())!=null)
		{
			mob.tell("You already have the blade barrier.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"":"^S<S-NAME> "+prayWord(mob)+" for divine protection!  ")+"A barrier of blades begin to spin around <T-NAME>!^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for divine protection, but nothing happens.");


		// return whether it worked
		return success;
	}
}
