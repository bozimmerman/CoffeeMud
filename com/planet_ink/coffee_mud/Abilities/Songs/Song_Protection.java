package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
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
		quality=Ability.BENEFICIAL_OTHERS;

		addQualifyingClass("Bard",15);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Protection();
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
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

		if(!Util.bset(affect.targetMajor(),Affect.MASK_MALICIOUS))
			return true;
		switch(affect.targetMinor())
		{
		case Affect.TYP_ACID:
		case Affect.TYP_COLD:
		case Affect.TYP_ELECTRIC:
		case Affect.TYP_FIRE:
		case Affect.TYP_GAS:
		case Affect.TYP_WATER:
			break;
		default:
			return true;
		}

		if(affect.target()==null)
			return true;
		if(!(affect.target() instanceof MOB))
			return true;

		if(invoker==null) return true;

		if(((MOB)affect.target()).fetchAffect(this.ID())==null)
			return true;

		if(Dice.rollPercentage()<(invoker.charStats().getCharisma()*4))
		{
			affect.source().location().show(affect.source(),affect.target(),Affect.MSG_OK_ACTION,"The musical aura around <T-NAME> protects "+((MOB)affect.target()).charStats().himher()+".");
			return false;
		}

		return true;
	}
}
