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
	public Environmental newInstance(){	return new Thief_Safecracking();}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	
	
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okAffect(myHost,affect);

		MOB mob=(MOB)affected;
		if((affect.amISource(mob))
		&&(affect.tool()!=null)
		&&(affect.tool().ID().equals("Thief_Pick")))
		{
			helpProfficiency(mob);
			Ability A=mob.fetchAbility("Thief_Pick");
			A.setAbilityCode(10-(profficiency()/10));
			if((affect.target()!=null)&&(Dice.rollPercentage()<profficiency()))
			{
				A=affect.target().fetchAffect("Spell_WizardLock");
				if(A!=null) A.unInvoke();
			}
		}
		return super.okAffect(myHost,affect);
	}
}
