package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2004-2018 Bo Zimmerman

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
public class Skill_Enslave extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Enslave";
	}

	private final static String	localizedName	= CMLib.lang().L("Enslave");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "ENSLAVE" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Enslaved)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_CRIMINAL;
	}

	protected String	masterName		= "";
	protected String	oldLeige		= "";
	protected MOB		masterMOB		= null;
	protected int		masterAnger		= 0;
	protected int		speedDown		= 0;
	protected int		hungerTickDown	= HUNGERTICKMAX;
	protected Room		lastRoom		= null;
	
	protected List<Pair<Clan, Integer>>	oldClans		= null;
	protected SlaveryLibrary.GeasSteps	STEPS			= null;
	protected final static int			HUNGERTICKMAX	= 4;
	protected final static int			SPEEDMAX		= 2;

	@Override
	public void setMiscText(String txt)
	{
		masterMOB=null;
		masterName=txt;
		super.setMiscText(txt);
	}

	public MOB getMaster()
	{
		if(masterMOB==null)
		{
			masterMOB=CMLib.players().getLoadPlayer(masterName);
			if(masterMOB!=null)
			{
				oldLeige=masterMOB.getLiegeID();
				oldClans=new Vector<Pair<Clan,Integer>>();
				for(final Pair<Clan,Integer> p : masterMOB.clans())
					oldClans.add(p);
			}
		}
		return masterMOB;
	}

	public void unMaster(MOB mob)
	{
		if((masterMOB!=null) && (mob!=null))
		{
			mob.setLiegeID(oldLeige);
			mob.setClan("", Integer.MIN_VALUE);
			for(final Pair<Clan,Integer> p : oldClans)
				mob.setClan(p.first.clanID(),p.second.intValue());
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
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
		&&(msg.targetMinor()!=CMMsg.TYP_ORDER)
		&&(msg.sourceMessage()!=null)
		&&(msg.sourceMessage().length()>0))
		{
			if(STEPS!=null)
			{
				if((msg.target()==null)||(msg.target() instanceof MOB))
				{
					final String response=CMStrings.getSayFromMessage(msg.sourceMessage());
					if(response!=null)
					{
						if((msg.target()==mob)
						&&(msg.source().Name().equals(mob.getLiegeID())))
						{
							final Vector<String> V=CMParms.parse(response.toUpperCase());
							if(V.contains("STOP")||V.contains("CANCEL"))
							{
								CMLib.commands().postSay(mob,msg.source(),L("Yes master."),false,false);
								STEPS=null;
								return;
							}
						}
						STEPS.sayResponse(msg.source(),(MOB)msg.target(),response);
					}
				}
			}
			else
			if((msg.amITarget(mob))
			&&(mob.getLiegeID().length()>0))
			{
				if((msg.tool()==null)
				||((msg.tool() instanceof Ability)
					&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE)
					&&(mob.fetchAbility(msg.tool().ID())!=null)))
				{
					if(!msg.source().Name().equals(mob.getLiegeID()))
					{
						final String response=CMStrings.getSayFromMessage(msg.sourceMessage());
						if(response!=null)
						{
							if((response.toUpperCase().startsWith("I COMMAND YOU TO "))
							||(response.toUpperCase().startsWith("I ORDER YOU TO ")))
								CMLib.commands().postSay(mob,msg.source(),L("I don't take orders from you. "),false,false);
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
								CMLib.commands().postSay(mob,msg.source(),L("Master, please begin your instruction with the words 'I command you to '.  You can also tell me to 'stop' or 'cancel' any order you give."),false,false);
								return;
							}
							STEPS=CMLib.slavery().processRequest(msg.source(),mob,response);
							if((STEPS!=null)&&(STEPS.size()>0))
								CMLib.commands().postSay(mob,msg.source(),L("Yes master."),false,false);
							else
							{
								STEPS=null;
								CMLib.commands().postSay(mob,msg.source(),L("Huh? Wuh?"),false,false);
							}
						}
					}
				}
				else
				if((msg.tool() instanceof Ability)
				&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE))
					CMLib.commands().postSay(mob,msg.source(),L("I don't understand your words."),false,false);
			}
		}
		else
		if((mob.location()!=null)&&(getMaster()!=null))
		{
			final Room room=mob.location();
			if((room!=lastRoom)
			&&(CMLib.law().doesHavePriviledgesHere(getMaster(),room))
			&&(room.isInhabitant(mob)))
			{
				lastRoom=room;
				mob.basePhyStats().setRejuv(PhyStats.NO_REJUV);
				mob.setStartRoom(room);
			}
		}
		else
		if((msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
		||((msg.targetMinor()==CMMsg.TYP_EXPIRE)&&((msg.target()==mob.location())||(msg.target()==mob)||(msg.target()==mob.amFollowing())))
		||((msg.sourceMinor()==CMMsg.TYP_QUIT)&&(msg.amISource(mob.amFollowing()))))
			mob.setFollowing(null);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!(affected instanceof MOB))
			return super.tick(ticking,tickID);
		if(tickID==Tickable.TICKID_MOB)
		{
			final MOB mob=(MOB)ticking;
			if((speedDown>-500)&&((--speedDown)>=0))
			{
				for(int a=mob.numEffects()-1;a>=0;a--) // personal
				{
					final Ability A=mob.fetchEffect(a);
					if((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL))
					{
						if(!A.tick(ticking,tickID))
							mob.delEffect(A);
					}
				}
			}
			if((--hungerTickDown)<=0)
			{
				hungerTickDown=HUNGERTICKMAX;
				CMLib.combat().expendEnergy(mob,false);
				if((!mob.isInCombat())&&(CMLib.dice().rollPercentage()==1)&&(CMLib.dice().rollPercentage()<(masterAnger/10)))
				{
					final MOB myMaster=getMaster();
					if((myMaster!=null)&&(mob.location().isInhabitant(myMaster)))
					{
						mob.location().show(mob,myMaster,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> rebel(s) against <T-NAMESELF>!"));
						final MOB master=getMaster();
						unMaster(mob);
						setMiscText("");
						mob.recoverCharStats();
						mob.recoverPhyStats();
						mob.resetToMaxState();
						mob.setFollowing(null);
						CMLib.combat().postAttack(mob,master,mob.fetchWieldedItem());
					}
					else
					if(CMLib.dice().rollPercentage()<50)
					{
						mob.location().show(mob,myMaster,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> escape(s) <T-NAMESELF>!"));
						CMLib.tracking().beMobile(mob,true,true,false,false,null,null);
					}
				}
				if(mob.curState().getHunger()<=0)
				{
					Food f=null;
					for(int i=0;i<mob.numItems();i++)
					{
						final Item I=mob.getItem(i);
						if(I instanceof Food)
						{
							f = (Food) I;
							break;
						}
					}
					if(f==null)
						CMLib.commands().postSay(mob,null,L("I am hungry."),false,false);
					else
					{
						final Command C=CMClass.getCommand("Eat");
						try
						{
							C.execute(mob, CMParms.parse("EAT \"" + f.Name() + "$\""), MUDCmdProcessor.METAFLAG_ORDER);
						}
						catch (final Exception e)
						{
						}
					}
				}
				if(mob.curState().getThirst()<=0)
				{
					Drink d=null;
					for(int i=0;i<mob.numItems();i++)
					{
						final Item I=mob.getItem(i);
						if(I instanceof Drink)
						{
							d = (Drink) I;
							break;
						}
					}
					if(d==null)
						CMLib.commands().postSay(mob,null,L("I am thirsty."),false,false);
					else
					{
						final Command C=CMClass.getCommand("Drink");
						try
						{
							C.execute(mob, CMParms.parse("DRINK \"" + d.Name() + "$\""), MUDCmdProcessor.METAFLAG_ORDER);
						}
						catch (final Exception e)
						{
						}
					}
				}
			}
			if(!mob.getLiegeID().equals(masterName))
			{
				mob.setLiegeID(masterName);
				final MOB myMaster=getMaster();
				if(myMaster!=null)
				{
					for(final Pair<Clan,Integer> p : CMLib.clans().findRivalrousClans(myMaster))
						mob.setClan(p.first.clanID(),p.first.getGovernment().getAcceptPos());
				}
			}
			if(STEPS==null)
			{
				// wait to be told to do something
			}
			else
			if((STEPS.size()==0)||(STEPS.isDone()))
			{
				if(mob.isInCombat())
					return true; // let them finish fighting.
				if((STEPS!=null)&&((STEPS.size()==0)||(STEPS.isDone())))
					mob.tell(L("You have completed your masters task."));
				else
					mob.tell(L("You have been released from your masters task."));
				if((mob.isMonster())
				&&(!mob.amDead())
				&&(mob.location()!=null)
				&&(mob.location()!=mob.getStartRoom()))
					CMLib.tracking().wanderAway(mob,true,true);
				unInvoke();
				STEPS=null;
				return !canBeUninvoked();
			}
			if(STEPS!=null)
			{
				STEPS.step();
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		MOB mob=null;
		if(affected instanceof MOB)
			mob=(MOB)affected;
		super.unInvoke();
		if(this.masterMOB!=null)
			unMaster(mob);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(mob.isMonster())
		{
			mob.location().show(mob,null,CMMsg.MSG_NOISE,L("<S-NAME> sigh(s)."));
			CMLib.commands().postSay(mob,null,L("You know, if I had any ambitions, I would enslave myself so I could do interesting things!"),false,false);
			return false;
		}

		if(commands.size()<1)
		{
			mob.tell(L("You need to specify a target to enslave."));
			return false;
		}
		final MOB target=getTarget(mob,commands,givenTarget,false,true);
		if(target==null)
			return false;
		if(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)<5)
		{
			mob.tell(L("@x1 would be too stupid to understand your instructions!",target.name(mob)));
			return false;
		}

		if((!CMLib.flags().isBoundOrHeld(target))
		&&(target.fetchEffect(ID())==null)
		&&(!CMSecurity.isAllowed(mob,target.location(), CMSecurity.SecFlag.CMDMOBS)))
		{
			mob.tell(L("@x1 must be bound first.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final boolean peace1=!mob.isInCombat();
			final boolean peace2=!target.isInCombat();
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISE|CMMsg.MASK_MALICIOUS,auto?"":L("^S<S-NAME> enslave(s) <T-NAMESELF>!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(peace2)
					target.makePeace(true);
				if(peace1)
					mob.makePeace(true);
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
			return beneficialWordsFizzle(mob,target,L("<S-NAME> attempt(s) to enslave on <T-NAMESELF>, but fails."));

		// return whether it worked
		return success;
	}
}
