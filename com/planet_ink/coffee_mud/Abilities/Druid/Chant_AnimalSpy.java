package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_AnimalSpy extends Chant
{
	private MOB spy=null;
	private boolean disable=false;
	public Chant_AnimalSpy()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Animal Spy";
		displayText="(Animal Spy)";

		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.OK_OTHERS;

		baseEnvStats().setLevel(13);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Chant_AnimalSpy();
	}
	
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		if(invoker!=null)
		{
			invoker.delAffect(this);
			invoker.tell("Your connection with '"+spy.name()+"' fades.");
		}
		super.unInvoke();
	}

	public void affect(Affect affect)
	{
		try
		{
			super.affect(affect);
			if(spy==null) return;
			if(invoker==null) return;

			if((affect.amISource(spy))
			&&(affect.sourceMinor()==Affect.TYP_EXAMINESOMETHING)
			&&(affect.target()!=null)
			&&((((MOB)invoker).location()!=spy.location())||(!(affect.target() instanceof Room))))
			{
				disable=true;
				FullMsg newAffect=new FullMsg(invoker,affect.target(),Affect.TYP_EXAMINESOMETHING,null);
				affect.target().affect(newAffect);
			}
			else
			if((!affect.amISource(invoker))
			&&(((MOB)invoker).location()!=spy.location())
			&&(affect.othersCode()!=Affect.NO_EFFECT)
			&&(affect.othersMessage()!=null)
			&&(!disable))
			{
				disable=true;
				((MOB)invoker).affect(affect);
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
		||(target.charStats().getStat(CharStats.INTELLIGENCE)>1)
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
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> chants(s) to <T-NAMESELF>, invoking the a mystical connection.");
			FullMsg msg2=new FullMsg(mob,target,this,affectType,null);
			if((mob.location().okAffect(msg))&&((newRoom==mob.location())||(newRoom.okAffect(msg2))))
			{
				mob.location().send(mob,msg);
				if(newRoom!=mob.location()) newRoom.send(target,msg2);
				spy=target;
				beneficialAffect(mob,spy,0);
				Ability A=spy.fetchAffect(ID());
				mob.addNonUninvokableAffect(A);
			}

		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades.");


		// return whether it worked
		return success;
	}
}