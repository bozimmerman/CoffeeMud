package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_AutoBash extends StdAbility
{
	public String ID() { return "Fighter_AutoBash"; }
	public String name(){ return "AutoBash";}
	public String displayText(){return "";};
	private static final String[] triggerStrings = {"AUTOBASH"};
	public String[] triggerStrings(){return triggerStrings;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Fighter_AutoBash();}
	public int classificationCode(){ return Ability.SKILL; }

	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null) return true;
		if(student.fetchAbility("Skill_Bash")==null)
		{
			teacher.tell(student.name()+" has not yet learned to shield bash.");
			student.tell("You need to learn to shield bash first.");
			return false;
		}

		return true;
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);
		if(!super.tick(ticking,tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob.isInCombat()
		&&(mob.rangeToTarget()==0)
		&&(Sense.aliveAwakeMobile(mob,true))
		&&(mob.fetchFirstWornItem(Item.HELD) instanceof Shield)
		&&(profficiencyCheck(null,0,false)))
		{
			Ability A=mob.fetchAbility("Skill_Bash");
			if(A!=null) A.invoke(mob,mob.getVictim(),false);
			if(Dice.rollPercentage()<10)
				helpProfficiency(mob);
		}
		return true;
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.fetchEffect(ID())!=null))
		{
			mob.tell("You are no longer automatically bashing opponents.");
			mob.delEffect(mob.fetchEffect(ID()));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			mob.tell("You will now automatically bash opponents when you fight.");
			beneficialAffect(mob,mob,0);
			Ability A=mob.fetchEffect(ID());
			if(A!=null) A.makeLongLasting();
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to get into <S-HIS-HER> bashing mood, but fail(s).");
		return success;
	}
}