package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_MummyRot extends StdAbility
{
	public String ID() { return "Disease_MummyRot"; }
	public String name(){ return "Mummy Rot";}
	public String displayText(){ return "(Mummy Rot)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings = {"MUMMYROT"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Disease_MummyRot();}
	public int classificationCode(){return Ability.SKILL;}

	int conDown=1;
	int diseaseTick=0;
	
	public String text(){return "DISEASE";}
	
	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))	return false;
		if((affected==null)||(invoker==null)) return false;

		MOB mob=(MOB)affected;
		if((--diseaseTick)<=0)
		{
			diseaseTick=10;
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> rotting away...");
			conDown--;
			return true;
		}
		return true;
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			mob.tell(mob,null,"The rot is cured.");
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		if(conDown<0) return;
		affectableStats.setStat(CharStats.CONSTITUTION,affectableStats.getStat(CharStats.CONSTITUTION)-conDown);
		if(affectableStats.getStat(CharStats.CONSTITUTION)<=0)
		{
			conDown=-1;
			ExternalPlay.postDeath(invoker(),affected,null);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to touch!");
			return false;
		}
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		String str=null;
		if(success)
		{
			str=auto?"":"^S<S-NAME> extend(s) a rotting hand to <T-NAMESELF>!^?";
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_UNDEAD|(auto?Affect.MASK_GENERAL:0),str);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> turn(s) grey!");
					conDown=1;
					success=maliciousAffect(mob,target,0,-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> extend(s) a rotting hand to <T-NAMESELF>, but fail(s).");

		return success;
	}
}