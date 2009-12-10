package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.SlaveryLibrary;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;



import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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

@SuppressWarnings("unchecked")
public class Skill_Enslave extends StdSkill
{
	public String ID() { return "Skill_Enslave"; }
	public String name(){ return "Enslave";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"ENSLAVE"};
	public String[] triggerStrings(){return triggerStrings;}
	public String displayText(){ return "(Enslaved)";}
    public int classificationCode() {   return Ability.ACODE_SKILL|Ability.DOMAIN_CRIMINAL; }
	
	protected MOB myMaster=null;
    protected SlaveryLibrary.geasSteps STEPS=null;
	protected int masterAnger=0;
	protected int speedDown=0;
    protected final static int HUNGERTICKMAX=4;
    protected final static int SPEEDMAX=2;
	protected int hungerTickDown=HUNGERTICKMAX;
	protected Room lastRoom=null;
	
	public void setMiscText(String txt)
	{
	    myMaster=null;
	    super.setMiscText(txt);
	}
	
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		&&(msg.tool() instanceof Social)
		&&(msg.tool().Name().equals("WHIP <T-NAME>")
			||msg.tool().Name().equals("BEAT <T-NAME>")))
		    speedDown=SPEEDMAX;
		else
		if(msg.amITarget(mob)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&((msg.value())>0))
		{
		    masterAnger+=10;
			CMLib.combat().postPanic(mob,msg);
		}
		else
		if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(msg.sourceMessage()!=null)
		&&(msg.sourceMessage().length()>0))
		{
			if(STEPS!=null)
			{
				if((msg.target()==null)||(msg.target() instanceof MOB))
				{
                    String response=CMStrings.getSayFromMessage(msg.sourceMessage());
                    if(response!=null)
					{
					    if((msg.target()==mob)
					    &&(msg.source().Name().equals(mob.getLiegeID())))
					    {
					        Vector V=CMParms.parse(response.toUpperCase());
					        if(V.contains("STOP")||V.contains("CANCEL"))
					        {
					            CMLib.commands().postSay(mob,msg.source(),"Yes master.",false,false);
					            return;
					        }
					    }
					    STEPS.sayResponse(msg.source(),(MOB)msg.target(),response);
					}
				}
			}
			else
			if((msg.amITarget(mob))&&(mob.getLiegeID().length()>0))
			{
			    if((msg.tool()==null)
			    ||((msg.tool() instanceof Ability)
			    	&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE)
			    	&&(mob.fetchAbility(msg.tool().ID())!=null)))
		    	{
				    if(!msg.source().Name().equals(mob.getLiegeID()))
				    {
                        String response=CMStrings.getSayFromMessage(msg.sourceMessage());
                        if(response!=null)
						{
						    if((response.toUpperCase().startsWith("I COMMAND YOU TO "))
						    ||(response.toUpperCase().startsWith("I ORDER YOU TO ")))
					            CMLib.commands().postSay(mob,msg.source(),"I don't take orders from you. ",false,false);
						}
				    }
				    else
				    {
                        String response=CMStrings.getSayFromMessage(msg.sourceMessage());
                        if(response!=null)
						{
						    if(response.toUpperCase().startsWith("I COMMAND YOU TO "))
						        response=response.substring(("I COMMAND YOU TO ").length());
						    else
						    if(response.toUpperCase().startsWith("I ORDER YOU TO "))
						        response=response.substring(("I ORDER YOU TO ").length());
						    else
						    {
					            CMLib.commands().postSay(mob,msg.source(),"Master, please begin your instruction with the words 'I command you to '.  You can also tell me to 'stop' or 'cancel' any order you give.",false,false);
					            return;
						    }
							STEPS=CMLib.slavery().processRequest(msg.source(),mob,response);
							if((STEPS!=null)&&(STEPS.size()>0))
					            CMLib.commands().postSay(mob,msg.source(),"Yes master.",false,false);
							else
					            CMLib.commands().postSay(mob,msg.source(),"Huh? Wuh?",false,false);
						}
					}
		    	}
			    else
		        if((msg.tool() instanceof Ability)
				&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE))
		            CMLib.commands().postSay(mob,msg.source(),"I don't understand your words.",false,false);
			}
		}
		else
		if((mob.location()!=null)&&(myMaster!=null))
		{
			Room room=mob.location();
			if((room!=lastRoom)
			&&(CMLib.law().doesHavePriviledgesHere(myMaster,room))
			&&(room.isInhabitant(mob)))
			{
				lastRoom=room;
				mob.baseEnvStats().setRejuv(0);
				mob.setStartRoom(room);
			}
		}
		else
		if((msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
		||((msg.targetMinor()==CMMsg.TYP_EXPIRE)&&((msg.target()==mob.location())||(msg.target()==mob)||(msg.target()==mob.amFollowing())))
		||((msg.sourceMinor()==CMMsg.TYP_QUIT)&&(msg.amISource(mob.amFollowing()))))
			mob.setFollowing(null);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);
		if(tickID==Tickable.TICKID_MOB)
		{
			MOB mob=(MOB)ticking;
			if((speedDown>-500)&&((--speedDown)>=0))
			{
			    for(int a=mob.numEffects()-1;a>=0;a--)
			    {
			        Ability A=mob.fetchEffect(a);
			        if((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL))
			            if(!A.tick(ticking,tickID))
			                mob.delEffect(A);
			    }
			}
		    if((--hungerTickDown)<=0)
		    {
		        hungerTickDown=HUNGERTICKMAX;
		        mob.curState().expendEnergy(mob,mob.maxState(),false);
		        if((!mob.isInCombat())&&(CMLib.dice().rollPercentage()==1)&&(CMLib.dice().rollPercentage()<(masterAnger/10)))
		        {
		            if(myMaster==null) myMaster=CMLib.players().getPlayer(text());
		            if((myMaster!=null)&&(mob.location().isInhabitant(myMaster)))
		            {
		                mob.location().show(mob,myMaster,null,CMMsg.MSG_OK_ACTION,"<S-NAME> rebel(s) against <T-NAMESELF>!");
		                setMiscText("");
		                MOB master=myMaster;
		                myMaster=null;
		                mob.setLiegeID("");
		                mob.setClanID("");
		                mob.recoverCharStats();
		                mob.recoverEnvStats();
		                mob.resetToMaxState();
		                mob.setFollowing(null);
		                CMLib.combat().postAttack(mob,master,mob.fetchWieldedItem());
		            }
		            else
		            if(CMLib.dice().rollPercentage()<50)
		            {
		                mob.location().show(mob,myMaster,null,CMMsg.MSG_OK_ACTION,"<S-NAME> escape(s) <T-NAMESELF>!");
		                CMLib.tracking().beMobile(mob,true,true,false,false,null,null);
		            }
		        }
	            if(mob.curState().getHunger()<=0)
	            {
	                Food f=null;
	                for(int i=0;i<mob.inventorySize();i++)
	                {
	                    Item I=mob.fetchInventory(i);
	                    if(I instanceof Food)
	                    { f=(Food)I; break;}
	                }
	                if(f==null)
		                CMLib.commands().postSay(mob,null,"I am hungry.",false,false);  
	                else
	                {
	                    Command C=CMClass.getCommand("Eat");
	                    try{C.execute(mob,CMParms.parse("EAT \""+f.Name()+"$\""),Command.METAFLAG_ORDER);}catch(Exception e){}
	                }
	            }
	            if(mob.curState().getThirst()<=0)
	            {
	                Drink d=null;
	                for(int i=0;i<mob.inventorySize();i++)
	                {
	                    Item I=mob.fetchInventory(i);
	                    if(I instanceof Drink)
	                    { d=(Drink)I; break;}
	                }
	                if(d==null)
	                    CMLib.commands().postSay(mob,null,"I am thirsty.",false,false);
	                else
	                {
	                    Command C=CMClass.getCommand("Drink");
	                    try{C.execute(mob,CMParms.parse("DRINK \""+d.Name()+"$\""),Command.METAFLAG_ORDER);}catch(Exception e){}
	                }
	            }
		    }
			if(!mob.getLiegeID().equals(text()))
			{
			    mob.setLiegeID(text());
	            if(myMaster==null) myMaster=CMLib.players().getPlayer(text());
	            if(myMaster!=null) mob.setClanID(myMaster.getClanID());
			}
		    if(STEPS!=null)
		    {
				if((STEPS==null)||(STEPS.size()==0)||(STEPS.done))
				{
					if(mob.isInCombat())
						return true; // let them finish fighting.
					if((STEPS!=null)&&((STEPS.size()==0)||(STEPS.done)))
						mob.tell("You have completed your masters task.");
					else
						mob.tell("You have been released from your masters task.");
					if((mob.isMonster())
					&&(!mob.amDead())
					&&(mob.location()!=null)
					&&(mob.location()!=mob.getStartRoom()))
						CMLib.tracking().wanderAway(mob,true,true);
					unInvoke();
					return !canBeUninvoked();
				}
			    STEPS.step();
		    }
		}
		return super.tick(ticking,tickID);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob.isMonster())
		{
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> sigh(s).");
			CMLib.commands().postSay(mob,null,"You know, if I had any ambitions, I would enslave myself so I could do interesting things!",false,false);
			return false;
		}

		if(commands.size()<1)
		{
			mob.tell("You need to specify a target to enslave.");
			return false;
		}
		MOB target=getTarget(mob,commands,givenTarget,false,true);
		if(target==null) return false;
		if(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)<5)
		{
			mob.tell(target.name()+" would be too stupid to understand your instructions!");
			return false;
		}
		
		if((!CMLib.flags().isBoundOrHeld(target))&&(target.fetchEffect(ID())==null))
		{
		    mob.tell(target.name()+" must be bound first.");
		    return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISE,auto?"":"^S<S-NAME> enslave(s) <T-NAMESELF>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Ability A=target.fetchEffect(ID());
				if(A==null)
				{
					A=(Ability)copyOf();
					target.addNonUninvokableEffect(A);
				}
				A.setMiscText(mob.Name());
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to enslave on <T-NAMESELF>, but fails.");

		// return whether it worked
		return success;
	}
}
