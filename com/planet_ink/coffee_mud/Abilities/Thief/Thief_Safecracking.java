package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Safecracking extends ThiefSkill
{
	public String ID() { return "Thief_Safecracking"; }
	public String name(){ return "Safecracking";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}

	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null) return true;
		if(student.fetchAbility("Thief_Pick")==null)
		{
			teacher.tell(student.name()+" has not yet learned to pick locks.");
			student.tell("You need to learn to pick locks first.");
			return false;
		}
		return true;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okMessage(myHost,msg);

		MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(msg.tool()!=null)
		&&(msg.tool().ID().equals("Thief_Pick")))
		{
			helpProfficiency(mob);
			Ability A=mob.fetchAbility("Thief_Pick");
			A.setAbilityCode(profficiency()/5);
			if((msg.target()!=null)&&(Dice.rollPercentage()<profficiency()))
			{
				A=msg.target().fetchEffect("Spell_WizardLock");
				if(A!=null) A.unInvoke();
			}
		}
		return super.okMessage(myHost,msg);
	}
}
