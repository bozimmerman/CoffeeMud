package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Attack3 extends StdAbility
{
	public String ID() { return "Skill_Attack3"; }
	public String name(){ return "Third Attack";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	private boolean active=true;
	public Environmental newInstance(){	return new Skill_Attack3();	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		affectableStats.setSpeed(affectableStats.speed()+(1.0*(new Integer(profficiency()).doubleValue()/100.0)));
	}
	
	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;

		if((affect.amISource(mob))
		&&(affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
		&&(Dice.rollPercentage()>97)
		&&(mob.isInCombat())
		&&(!mob.amDead())
		&&(affect.target() instanceof MOB))
			helpProfficiency(mob);
	}
	
	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null) return true;
		if(student.fetchAbility("Skill_Attack2")==null)
		{
			teacher.tell(student.name()+" has not yet learned second attack.");
			student.tell("You need to learn second attack before you can learn "+name()+".");
			return false;
		}

		return true;
	}

}
