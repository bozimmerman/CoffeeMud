package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_AnimalSpy extends Chant
{
	public String ID() { return "Chant_AnimalSpy"; }
	public String name(){ return "Animal Spy";}
	public String displayText(){return "(Animal Spy)";}
	public int quality(){return Ability.OK_OTHERS;}
	private MOB spy=null;
	private boolean disable=false;
	public Environmental newInstance()	{	return new Chant_AnimalSpy();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==MudHost.TICK_MOB)
		   &&(affected==spy))
		{
			if(spy.amDead()
			   ||(spy.amFollowing()!=invoker)
			   ||(!Sense.isInTheGame(spy)))
				unInvoke();
		}
		return true;
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		if(canBeUninvoked())
		{
			if(invoker!=null)
			{
				Ability A=invoker.fetchEffect(this.ID());
				if(A!=null)
					invoker.delEffect(A);
				invoker.tell("Your connection with '"+spy.name()+"' fades.");
			}
		}
		super.unInvoke();
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		try
		{
			super.executeMsg(myHost,msg);
			if(spy==null) return;
			if(invoker==null) return;

			if((msg.amISource(spy))
			&&(msg.sourceMinor()==CMMsg.TYP_EXAMINESOMETHING)
			&&(msg.target()!=null)
			&&((((MOB)invoker).location()!=spy.location())||(!(msg.target() instanceof Room))))
			{
				disable=true;
				FullMsg newAffect=new FullMsg(invoker,msg.target(),CMMsg.TYP_EXAMINESOMETHING,null);
				msg.target().executeMsg(invoker,newAffect);
			}
			else
			if((!msg.amISource(invoker))
			&&(((MOB)invoker).location()!=spy.location())
			&&(msg.othersCode()!=CMMsg.NO_EFFECT)
			&&(msg.othersMessage()!=null)
			&&(!disable))
			{
				disable=true;
				((MOB)invoker).executeMsg(invoker,msg);
			}
			else
			if(msg.amISource(invoker)
			&&(!disable)
			&&(msg.sourceMinor()==CMMsg.TYP_SPEAK)
			&&((msg.sourceCode()&CMMsg.MASK_MAGIC)==0))
			{
				int start=msg.sourceMessage().indexOf("\'");
				int end=msg.sourceMessage().lastIndexOf("\'");
				if((start>0)&&(end>start))
				{
					String msg2=msg.sourceMessage().substring(start+1,end).trim();
					if(msg2.length()>0)
						spy.enqueCommand(Util.parse(msg2.trim()),0);
				}
			}
		}
		finally
		{
			disable=false;
			if((spy!=null)&&(spy.amFollowing()!=invoker)&&(!spy.amDead()))
				unInvoke();
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		if(commands.size()<1)
		{
			mob.tell("Chant to whom?");
			return false;
		}
		String mobName=Util.combine(commands,0).trim().toUpperCase();
		MOB target=getTarget(mob,commands,givenTarget);

		Room newRoom=mob.location();
		if(target!=null)
			newRoom=target.location();
		else
		if((target==null)
		||(!Sense.isAnimalIntelligence(target))
		||(target.amFollowing()!=mob))
		{
			mob.tell("You have no animal follower named '"+mobName+"' here.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>, invoking the a mystical connection.^?");
			FullMsg msg2=new FullMsg(mob,target,this,affectType(auto),null);
			if((mob.location().okMessage(mob,msg))&&((newRoom==mob.location())||(newRoom.okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				if(newRoom!=mob.location()) newRoom.send(target,msg2);
				spy=target;
				beneficialAffect(mob,spy,0);
				Ability A=spy.fetchEffect(ID());
				mob.addNonUninvokableEffect((Ability)A.copyOf());
				A.setAffectedOne(spy);
			}

		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades.");


		// return whether it worked
		return success;
	}
}