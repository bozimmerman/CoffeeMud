package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Inebriation extends Song
{
	public String ID() { return "Song_Inebriation"; }
	public String name(){ return "Drunkenness";}
	public int quality(){ return MALICIOUS;}
	protected boolean mindAttack(){return true;}
	public Environmental newInstance(){	return new Song_Inebriation();}
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

		affectableStats.setStat(CharStats.DEXTERITY,(int)Math.round(affectableStats.getStat(CharStats.DEXTERITY)-3));
	}

	public void show(MOB mob, int code, String text)
	{
		FullMsg msg=new FullMsg(mob,null,this,code,code,code,text);
		if((mob.location()!=null)&&(mob.location().okAffect(mob,msg)))
			mob.location().send(mob,msg);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null) return true;
		if(mob==invoker) return true;
		if((Dice.rollPercentage()<25)&&(Sense.canMove(mob)))
		{
			if(mob.getAlignment()<350)
				show(mob,Affect.MSG_QUIETMOVEMENT,"<S-NAME> stagger(s) around making ugly faces.");
			else
			if(mob.getAlignment()<650)
				show(mob,Affect.MSG_QUIETMOVEMENT,"<S-NAME> stagger(s) around aimlessly.");
			else
				show(mob,Affect.MSG_QUIETMOVEMENT,"<S-NAME> stagger(s) around trying to hug everyone.");

		}
		return true;
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;

		if(affect.source()==invoker)
			return true;

		if(affect.source()!=affected)
			return true;

		if(affect.target()==null)
			return true;

		if(!(affect.target() instanceof MOB))
		   return true;

		if((affect.amISource((MOB)affected))
		&&(affect.sourceMessage()!=null)
		&&(affect.tool()==null)
		&&((affect.sourceMinor()==Affect.TYP_SPEAK)
		   ||(affect.sourceMinor()==Affect.TYP_TELL)
		   ||(Util.bset(affect.sourceCode(),Affect.MASK_CHANNEL))))
		{
			Ability A=CMClass.getAbility("Drunken");
			if(A!=null)
			{
				A.setProfficiency(100);
				A.invoke(affect.source(),null,true);
				A.setAffectedOne(affect.source());
				if(!A.okAffect(myHost,affect))
					return false;
			}
		}
		else
		if((!Util.bset(affect.targetMajor(),Affect.MASK_GENERAL))
		&&(affect.targetMajor()>0))
		{
			MOB newTarget=affect.source().location().fetchInhabitant(Dice.roll(1,affect.source().location().numInhabitants(),-1));
			if(newTarget!=null)
				affect.modify(affect.source(),newTarget,affect.tool(),affect.sourceCode(),affect.sourceMessage(),affect.targetCode(),affect.targetMessage(),affect.othersCode(),affect.othersMessage());
		}
		return true;
	}
}
