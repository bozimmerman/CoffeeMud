package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.Properties.Prop_HaveResister;
import java.util.*;

public class Skill_Resistance extends StdAbility
{
	public int oldHP=-1;
	public Skill_Resistance()
	{
		super();
		Skill_Resistance_Setup("");
	}

	private void Skill_Resistance_Setup(String resistanceMask)
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Resistance";
		displayText="";
		miscText=resistanceMask;

		canBeUninvoked=false;
		isAutoinvoked=true;
		quality=Ability.BENEFICIAL_SELF;

		baseEnvStats().setLevel(1);

		recoverEnvStats();
	}

	public Skill_Resistance(String resistanceMask)
	{
		Skill_Resistance_Setup(resistanceMask);
	}

	public Environmental newInstance()
	{
		return new Skill_Resistance();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if((affect.amITarget(mob))&&(!affect.wasModified())&&(mob.location()!=null))
		{
			if(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
				switch(affect.targetMinor())
				{
				case Affect.TYP_GAS:
				case Affect.TYP_FIRE:
				case Affect.TYP_ELECTRIC:
				case Affect.TYP_MIND:
				case Affect.TYP_PARALYZE:
				case Affect.TYP_CAST_SPELL:
				case Affect.TYP_JUSTICE:
				case Affect.TYP_COLD:
				case Affect.TYP_ACID:
				case Affect.TYP_POISON:
				case Affect.TYP_WATER:
				case Affect.TYP_UNDEAD:
					if(profficiencyCheck(0,false))
					{
						ExternalPlay.resistanceMsgs(affect,affect.source(),mob);
						if(affect.wasModified())
							helpProfficiency(mob);
					}
				default:
					break;
				}
		}
		return true;
	}
}
