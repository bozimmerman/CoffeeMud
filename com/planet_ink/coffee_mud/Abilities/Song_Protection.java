package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;


public class Song_Protection extends Song
{

	public Song_Protection()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Protection";
		displayText="(Song of Protection)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(15);

		addQualifyingClass(new Bard().ID(),15);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Protection();
	}
	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;
		if(affected==invoker) return;

		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(int)Math.round(invoker.envStats().level()));
	}


	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(invoker==null) return;
		if(affected==invoker) return;

		affectableStats.setDexterity((int)Math.round(affectableStats.getDexterity()-3));
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if((affect.targetCode()!=Affect.STRIKE_ACID)
		&&(affect.targetCode()!=Affect.STRIKE_COLD)
		&&(affect.targetCode()!=Affect.STRIKE_ELECTRIC)
		&&(affect.targetCode()!=Affect.STRIKE_FIRE)
		&&(affect.targetCode()!=Affect.STRIKE_GAS)
		&&(affect.targetCode()!=Affect.STRIKE_WATER))
			return true;

		if(affect.target()==null)
			return true;
		if(!(affect.target() instanceof MOB))
			return true;

		if(((MOB)affect.target()).fetchAffect(this.ID())==null)
			return true;

		if(Dice.rollPercentage()<(((MOB)affect.target()).charStats().getWisdom()*3))
		{
			affect.source().location().show(affect.source(),affect.target(),Affect.VISUAL_WNOISE,"The musical aura around <T-NAME> protects "+((MOB)affect.target()).charStats().himher()+".");
			return false;
		}

		return true;
	}
}
