package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_UndeadInvisibility extends StdAbility
{
	public Skill_UndeadInvisibility()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Invisibility to the Undead";
		displayText="";
		miscText="";

		quality=Ability.BENEFICIAL_SELF;
		canBeUninvoked=false;
		isAutoinvoked=true;

		baseEnvStats().setLevel(8);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_UndeadInvisibility();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public boolean okAffect(Affect affect)
	{
		if(((affect.targetCode()&Affect.MASK_MALICIOUS)>0)
		&&((affect.amITarget(affected))))
		{
			MOB target=(MOB)affect.target();
			if((!target.isInCombat())
			   &&(affect.source().charStats().getMyClass().ID().equals("Undead"))
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