package com.planet_ink.coffee_mud.Abilities.Skills;

import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

public class Skill_Enslave extends StdAbility
{
	public String ID() { return "Skill_Enslave"; }
	public String name(){ return "Enslave";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings = {"ENSLAVE"};
	public String[] triggerStrings(){return triggerStrings;}
	public boolean canBeUninvoked(){return false;}
	public boolean isAutoInvoked(){return false;}
	public int classificationCode(){return Ability.PROPERTY;}
	
	//TODO: add eating drinking hunger, starvation
	//TODO: damage taken by a slave may bring reprisal against master?
	//TODO: whippings cause them to do common skills faster, but may bring reprisal
	

	public MOB master=null;
	public EnglishParser.geasStep STEP=null;
	private int masterAnger=0;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if((STEP!=null)&&(STEP.que!=null)&&(STEP.que.size()==0))
				mob.tell("You have completed your masters task.");
			else
				mob.tell("You have been released from your masters task.");
			if((mob.isMonster())
			&&(!mob.amDead())
			&&(mob.location()!=null)
			&&(mob.location()!=mob.getStartRoom()))
				MUDTracker.wanderAway(mob,true,true);
		}
		STEP=null;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(msg.amITarget(mob)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&((msg.value())>0))
		{
		    masterAnger+=1000;
			MUDFight.postPanic(mob,msg);
		}
		else
		if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(msg.sourceMessage()!=null)
		&&(msg.sourceMessage().length()>0))
		{
			if(STEP!=null)
			{
				if((msg.target()==null)||(msg.target() instanceof MOB))
				{
					int start=msg.sourceMessage().indexOf("'");
					int end=msg.sourceMessage().lastIndexOf("'");
					if((start>0)&&(end>(start+1)))
					{
					    String response=msg.sourceMessage().substring(start+1,end);
					    if((msg.target()==mob)
					    &&(msg.source().Name().equals(mob.getLiegeID())))
					    {
					        Vector V=Util.parse(response.toUpperCase());
					        if(V.contains("STOP")||V.contains("CANCEL"))
					        {
					            CommonMsgs.say(mob,msg.source(),"Yes master.",false,false);
					            return;
					        }
					    }
						STEP.sayResponse(msg.source(),(MOB)msg.target(),response);
					}
				}
			}
			else
			if((msg.amITarget(mob))&&(mob.getLiegeID().length()>0))
			{
			    if((msg.tool()==null)
			    ||((msg.tool() instanceof Ability)
			    	&&(((Ability)msg.tool()).classificationCode()==Ability.LANGUAGE)
			    	&&(mob.fetchAbility(msg.tool().ID())!=null)))
		    	{
				    if(!msg.source().Name().equals(mob.getLiegeID()))
				    {
						int start=msg.sourceMessage().indexOf("'");
						int end=msg.sourceMessage().lastIndexOf("'");
						if((start>0)&&(end>(start+1)))
						{
						    String response=msg.sourceMessage().substring(start+1,end);
						    if((response.toUpperCase().startsWith("I COMMAND YOU TO "))
						    ||(response.toUpperCase().startsWith("I ORDER YOU TO ")))
					            CommonMsgs.say(mob,msg.source(),"I don't take orders from you. ",false,false);
						}
				    }
				    else
				    {
						int start=msg.sourceMessage().indexOf("'");
						int end=msg.sourceMessage().lastIndexOf("'");
						if((start>0)&&(end>(start+1)))
						{
						    String response=msg.sourceMessage().substring(start+1,end);
						    if(response.toUpperCase().startsWith("I COMMAND YOU TO "))
						        response=response.substring(("I COMMAND YOU TO ").length());
						    else
						    if(response.toUpperCase().startsWith("I ORDER YOU TO "))
						        response=response.substring(("I ORDER YOU TO ").length());
						    else
						    {
					            CommonMsgs.say(mob,msg.source(),"Master, please begin your instruction with the words 'I command you to '.  You can also tell me to 'stop' or 'cancel' any order you give.",false,false);
					            return;
						    }
							STEP=EnglishParser.processRequest(msg.source(),mob,response);
				            CommonMsgs.say(mob,msg.source(),"Yes master.",false,false);
						}
					}
		    	}
			    else
		        if((msg.tool() instanceof Ability)
				&&(((Ability)msg.tool()).classificationCode()==Ability.LANGUAGE))
		            CommonMsgs.say(mob,msg.source(),"I don't understand your words.",false,false);
			}
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);
		if(tickID==MudHost.TICK_MOB)
		{
			MOB mob=(MOB)ticking;
			if(!mob.getLiegeID().equals(text()))
			    mob.setLiegeID(text());
		    if(STEP!=null)
		    {
				if((STEP.que!=null)&&(STEP.que.size()==0))
				{
					if(mob.isInCombat())
						return true; // let them finish fighting.
					unInvoke();
					return !canBeUninvoked();
				}
				if((STEP!=null)&&(STEP.que!=null))
				    STEP.step();
		    }
		}
		return super.tick(ticking,tickID);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob.isMonster())
		{
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> sigh(s).");
			CommonMsgs.say(mob,null,"You know, if I had any ambitions, I would enslave myself so I could do interesting things!",false,false);
			return false;
		}

		if(commands.size()<1)
		{
			mob.tell("You need to specify a target to enslave.");
			return false;
		}
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(target.charStats().getStat(CharStats.INTELLIGENCE)<5)
		{
			mob.tell(target.name()+" would be too stupid to understand your instructions!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_NOISE,auto?"":"^S<S-NAME> enslave(s) <T-NAMESELF>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Ability A=(Ability)copyOf();
				A.setMiscText(mob.Name());
				target.addNonUninvokableEffect(A);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to enslave on <T-NAMESELF>, but fails.");

		// return whether it worked
		return success;
	}
}
