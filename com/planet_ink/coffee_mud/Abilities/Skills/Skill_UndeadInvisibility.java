package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_UndeadInvisibility extends StdAbility
{
	public String ID() { return "Skill_UndeadInvisibility"; }
	public String name(){ return "Undead Invisibility";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Skill_UndeadInvisibility();	}

	public boolean okAffect(Affect affect)
	{
		if(((affect.targetCode()&Affect.MASK_MALICIOUS)>0)
		&&((affect.amITarget(affected))))
		{
			MOB target=(MOB)affect.target();
			if((!target.isInCombat())
			   &&(affect.source().charStats().getMyRace().ID().equals("Undead"))
			   &&(affect.source().getVictim()!=target))
			{
				affect.source().tell("You don't see "+target.name());
				if(target.getVictim()==affect.source())
				{
					target.makePeace();
					target.setVictim(null);
					helpProfficiency((MOB)affected);
				}
				return false;
			}
		}
		return super.okAffect(affect);
	}
}