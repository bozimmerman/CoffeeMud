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
		if((tickID==Host.MOB_TICK)
		   &&(affected==spy))
		{
			if(spy.amDead()
			   ||(spy.amFollowing()!=invoker)
			   ||(spy.location()==null)
			   ||(!spy.location().isInhabitant(spy)))
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
				Ability A=invoker.fetchAffect(this.ID());
				if(A!=null)
					invoker.delAffect(A);
				invoker.tell("Your connection with '"+spy.displayName()+"' fades.");
			}
		}
		super.unInvoke();
	}

	public void affect(Environmental myHost, Affect affect)
	{
		try
		{
			super.affect(myHost,affect);
			if(spy==null) return;
			if(invoker==null) return;

			if((affect.amISource(spy))
			&&(affect.sourceMinor()==Affect.TYP_EXAMINESOMETHING)
			&&(affect.target()!=null)
			&&((((MOB)invoker).location()!=spy.location())||(!(affect.target() instanceof Room))))
			{
				disable=true;
				FullMsg newAffect=new FullMsg(invoker,affect.target(),Affect.TYP_EXAMINESOMETHING,null);
				affect.target().affect(invoker,newAffect);
			}
			else
			if((!affect.amISource(invoker))
			&&(((MOB)invoker).location()!=spy.location())
			&&(affect.othersCode()!=Affect.NO_EFFECT)
			&&(affect.othersMessage()!=null)
			&&(!disable))
			{
				disable=true;
				((MOB)invoker).affect(invoker,affect);
			}
			else
			if(affect.amISource(invoker)
			&&(!disable)
			&&(affect.sourceMinor()==Affect.TYP_SPEAK)
			&&((affect.sourceCode()&Affect.MASK_MAGIC)==0))
			{
				int start=affect.sourceMessage().indexOf("\'");
				int end=affect.sourceMessage().lastIndexOf("\'");
				if((start>0)&&(end>start))
				{
					String msg=affect.sourceMessage().substring(start+1,end).trim();
					if(msg.length()>0)
					{
						try{
							ExternalPlay.doCommand(spy,Util.parse(msg.trim()));
						}
						catch(Exception e)
						{
							invoker.tell("Your command to the spy has failed.");
						}
					}
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

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>, invoking the a mystical connection.^?");
			FullMsg msg2=new FullMsg(mob,target,this,affectType(auto),null);
			if((mob.location().okAffect(mob,msg))&&((newRoom==mob.location())||(newRoom.okAffect(mob,msg2))))
			{
				mob.location().send(mob,msg);
				if(newRoom!=mob.location()) newRoom.send(target,msg2);
				spy=target;
				beneficialAffect(mob,spy,0);
				Ability A=spy.fetchAffect(ID());
				mob.addNonUninvokableAffect((Ability)A.copyOf());
				A.setAffectedOne(spy);
			}

		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades.");


		// return whether it worked
		return success;
	}
}