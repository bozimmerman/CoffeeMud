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

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(invoker==null) return;
		affectableStats.setStat(CharStats.SAVE_ACID,affectableStats.getStat(CharStats.SAVE_ACID)+profficiency());
		affectableStats.setStat(CharStats.SAVE_COLD,affectableStats.getStat(CharStats.SAVE_COLD)+profficiency());
		affectableStats.setStat(CharStats.SAVE_ELECTRIC,affectableStats.getStat(CharStats.SAVE_ELECTRIC)+profficiency());
		affectableStats.setStat(CharStats.SAVE_FIRE,affectableStats.getStat(CharStats.SAVE_FIRE)+profficiency());
		affectableStats.setStat(CharStats.SAVE_GAS,affectableStats.getStat(CharStats.SAVE_GAS)+profficiency());
		affectableStats.setStat(CharStats.SAVE_MIND,affectableStats.getStat(CharStats.SAVE_MIND)+profficiency());
		affectableStats.setStat(CharStats.SAVE_PARALYSIS,affectableStats.getStat(CharStats.SAVE_PARALYSIS)+profficiency());
		affectableStats.setStat(CharStats.SAVE_MAGIC,affectableStats.getStat(CharStats.SAVE_MAGIC)+profficiency());
		affectableStats.setStat(CharStats.SAVE_JUSTICE,affectableStats.getStat(CharStats.SAVE_JUSTICE)+profficiency());
		affectableStats.setStat(CharStats.SAVE_POISON,affectableStats.getStat(CharStats.SAVE_POISON)+profficiency());
		affectableStats.setStat(CharStats.SAVE_WATER,affectableStats.getStat(CharStats.SAVE_WATER)+profficiency());
		affectableStats.setStat(CharStats.SAVE_UNDEAD,affectableStats.getStat(CharStats.SAVE_UNDEAD)+profficiency());
		affectableStats.setStat(CharStats.SAVE_DISEASE,affectableStats.getStat(CharStats.SAVE_DISEASE)+profficiency());
	}
}
