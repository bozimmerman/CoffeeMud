package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Dance_Polka extends Dance
{
	public String ID() { return "Dance_Polka"; }
	public String name(){ return "Polka";}
	public int quality(){ return MALICIOUS;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;

		if(affected==invoker) return;

		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-invoker.envStats().level());
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
		if((mob.location()!=null)&&(mob.location().okMessage(mob,msg)))
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
				show(mob,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> stagger(s) around making ugly faces.");
			else
			if(mob.getAlignment()<650)
				show(mob,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> stagger(s) around aimlessly.");
			else
				show(mob,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> stagger(s) around trying to hug everyone.");

		}
		return true;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if(msg.source()==invoker)
			return true;

		if(msg.source()!=affected)
			return true;

		if(msg.target()==null)
			return true;

		if(!(msg.target() instanceof MOB))
		   return true;

		if((msg.amISource((MOB)affected))
		&&(msg.sourceMessage()!=null)
		&&(msg.tool()==null)
		&&((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
		   ||(Util.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL))))
		{
			Ability A=CMClass.getAbility("Drunken");
			if(A!=null)
			{
				A.setProfficiency(100);
				A.invoke(msg.source(),null,true);
				A.setAffectedOne(msg.source());
				if(!A.okMessage(myHost,msg))
					return false;
			}
		}
		else
		if((!Util.bset(msg.targetMajor(),CMMsg.MASK_GENERAL))
		&&(msg.targetMajor()>0))
		{
			MOB newTarget=msg.source().location().fetchInhabitant(Dice.roll(1,msg.source().location().numInhabitants(),-1));
			if(newTarget!=null)
				msg.modify(msg.source(),newTarget,msg.tool(),msg.sourceCode(),msg.sourceMessage(),msg.targetCode(),msg.targetMessage(),msg.othersCode(),msg.othersMessage());
		}
		return true;
	}
}
