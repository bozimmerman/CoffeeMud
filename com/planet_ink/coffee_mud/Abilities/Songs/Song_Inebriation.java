package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Inebriation extends Song
{

	public Song_Inebriation()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Inebriation";
		displayText="(Song of Inebriation)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		quality=Ability.MALICIOUS;
		mindAttack=true;

		baseEnvStats().setLevel(11);

		addQualifyingClass("Bard",11);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Inebriation();
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

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null) return true;
		if(mob==invoker) return true;
		if((Dice.rollPercentage()<25)&&(Sense.canMove(mob)))
		{
			if(mob.getAlignment()<350)
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> stagger(s) around making ugly faces.");
			else
			if(mob.getAlignment()<650)
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> stagger(s) around aimlessly.");
			else
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> stagger(s) around trying to hug everyone.");

		}
		return true;
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if(affect.source()==invoker)
			return true;

		if(affect.source()!=affected)
			return true;

		if(affect.target()==null)
			return true;

		if(!(affect.target() instanceof MOB))
		   return true;

		if((!Util.bset(affect.targetMajor(),Affect.ACT_GENERAL))
		&&(affect.targetMajor()>0))
		{
			int n=affect.source().location().numInhabitants();
			int which=(int)Math.round(Math.random()*n);
			MOB newTarget=null;
			for(int i=0;i<=which;i++)
				newTarget=affect.source().location().fetchInhabitant(i);
			affect.modify(affect.source(),newTarget,affect.tool(),affect.sourceCode(),affect.sourceMessage(),affect.targetCode(),affect.targetMessage(),affect.othersCode(),affect.othersMessage());
		}
		return true;
	}
}
