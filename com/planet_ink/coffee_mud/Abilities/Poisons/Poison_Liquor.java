package com.planet_ink.coffee_mud.Abilities.Poisons;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;


public class Poison_Liquor extends Poison_Alcohol
{
	public String ID() { return "Poison_Liquor"; }
	public String name(){ return "Liquor";}
	private static final String[] triggerStrings = {"LIQUORUP"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Poison_Liquor();}
	public int classificationCode(){return Ability.POISON;}
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(affected instanceof MOB)
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(int)Math.round(((MOB)affected).envStats().level()*4));
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.DEXTERITY,(int)Math.round(affectableStats.getStat(CharStats.DEXTERITY)-10));
		if(affectableStats.getStat(CharStats.DEXTERITY)<=0)
			affectableStats.setStat(CharStats.DEXTERITY,1);
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if((affected==null)||(invoker==null)) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		if((getTickDownRemaining()==1)
		&&(Dice.rollPercentage()>mob.charStats().getSave(CharStats.SAVE_DISEASE)))
		{
			unInvoke();
			mob.delAffect(this);
			Ability A=CMClass.getAbility("Disease_Migraines");
			A.invoke(invoker,mob,true);
		}
		return true;
	}
	public void unInvoke()
	{
		MOB mob=null;
		if((affected!=null)&&(affected instanceof MOB))
			mob=(MOB)affected;
		super.unInvoke();
		if((mob!=null)&&(!mob.isInCombat()))
			mob.location().show(mob,null,Affect.MSG_SLEEP,"<S-NAME> curl(s) up on the ground and fall(s) asleep.");
	}
}