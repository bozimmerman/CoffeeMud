package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Paladin_DiseaseImmunity extends Paladin
{
	public Paladin_DiseaseImmunity()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Disease Immunity";
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
		return new Paladin_DiseaseImmunity();
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
		if((affect.amITarget(mob))
		&&(affect.targetMinor()==Affect.TYP_DISEASE)
		&&(!mob.amDead())
		&&(mob.getAlignment()>650)
		&&(profficiencyCheck(0,false)))
			return false;
		return super.okAffect(affect);
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if((affected!=null)&&(affected.getAlignment()>650))
			affectableStats.setStat(CharStats.SAVE_DISEASE,affectableStats.getStat(CharStats.SAVE_DISEASE)+50+profficiency());
	}
}
