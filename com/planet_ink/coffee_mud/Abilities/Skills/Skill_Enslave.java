package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.GenLantern;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder;
import com.planet_ink.coffee_mud.Libraries.interfaces.SlaveryLibrary;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2024 Bo Zimmerman

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
public class Skill_Enslave extends StdSkill implements PrivateProperty
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
	protected int		price			= 0;
	protected int		hungerTickDown	= HUNGERTICKMAX;
	protected Room		lastRoom		= null;

	protected Pair<MOB,Long>			pimpedInfo 		= null;
	protected List<Pair<Clan, Integer>>	oldClans		= null;
	protected SlaveryLibrary.GeasSteps	steps			= null;

	protected final static int			HUNGERTICKMAX	= 4;
	protected final static int			SPEEDMAX		= 2;

	@Override
	public void setMiscText(final String txt)
	{
		masterMOB=null;
		if(txt.indexOf('=') < 0)
		{
			masterName=txt.trim();
			price = 0;
		}
		else
		{
			masterName = CMParms.getParmStr(txt, "MASTER", "");
			price = CMParms.getParmInt(txt, "PRICE", 0);
		}
		super.setMiscText(txt);
	}

	protected String getObeyName()
	{
		final Pair<MOB,Long> pimp = this.pimpedInfo;
		if(pimp != null)
			return pimp.first.Name();
		return getOwnerName();
	}

	protected MOB getMaster()
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

	public void unMaster(final MOB mob)
	{
		this.pimpedInfo = null;
		if((masterMOB!=null)
		&& (mob!=null))
		{
			mob.setLiegeID(oldLeige);
			mob.setClan("", Integer.MIN_VALUE);
			for(final Pair<Clan,Integer> p : oldClans)
				mob.setClan(p.first.clanID(),p.second.intValue());
		}
	}

	@Override
	public int getPrice()
	{
		if(price <= 0)
		{
			if(affected != null)
				price = 100 * affected.phyStats().level();
		}
		return price;
	}

	@Override
	public void setPrice(final int price)
	{
		this.price = price;
		super.setMiscText("MASTER=\""+masterName+"\" PRICE="+price);
	}

	@Override
	public String getOwnerName()
	{
		if(masterName == null)
			return "";
		return masterName;
	}

	@Override
	public void setOwnerName(final String owner)
	{
		masterName = owner;
		super.setMiscText("MASTER=\""+masterName+"\" PRICE="+price);
	}

	@Override
	public boolean isProperlyOwned()
	{
		final String owner=getOwnerName();
		if(owner.length()==0)
			return false;
		final Clan C=CMLib.clans().fetchClanAnyHost(owner);
		if(C!=null)
			return true;
		return CMLib.players().playerExistsAllHosts(owner);
	}

	@Override
	public String getTitleID()
	{
		return "SLAVE:"+affected;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
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
			if(steps!=null)
			{
				if((msg.target()==null)||(msg.target() instanceof MOB))
				{
					final String response=CMStrings.getSayFromMessage(msg.sourceMessage());
					if(response!=null)
					{
						if((msg.target()==mob)
						&&(msg.source().Name().equals(mob.getLiegeID())
							||msg.source().Name().equals(getObeyName())))
						{
							final Vector<String> V=CMParms.parse(response.toUpperCase());
							if(V.contains("STOP")||V.contains("CANCEL"))
							{
								CMLib.commands().postSay(mob,msg.source(),L("Yes master."),false,false);
								steps=null;
								return;
							}
						}
						steps.sayResponse(msg.source(),(MOB)msg.target(),response);
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
					&&(mob.fetchAbility(msg.tool().ID())!=null)
					&&(msg.sourceMinor()!=CMMsg.TYP_TEACH)))
				{
					if(!msg.source().Name().equals(mob.getLiegeID())
					&&(!msg.source().Name().equals(getObeyName())))
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
							steps=CMLib.slavery().processRequest(msg.source(),mob,response);
							if((steps!=null)&&(steps.size()>0))
								CMLib.commands().postSay(mob,msg.source(),L("Yes master."),false,false);
							else
							{
								steps=null;
								CMLib.commands().postSay(mob,msg.source(),L("Huh? Wuh?"),false,false);
							}
						}
					}
				}
				else
				if((msg.tool() instanceof Ability)
				&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE)
				&&(msg.sourceMinor()!=CMMsg.TYP_TEACH))
					CMLib.commands().postSay(mob,msg.source(),L("I don't understand your words."),false,false);
			}
		}
		else
		if((mob.location()!=null)
		&&(getMaster()!=null))
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
		||((msg.targetMinor()==CMMsg.TYP_EXPIRE)
			&&((msg.target()==mob.location())||(msg.target()==mob)||(msg.target()==mob.amFollowing())))
		||((msg.sourceMinor()==CMMsg.TYP_QUIT)&&(msg.amISource(mob.amFollowing()))))
		{
			mob.setFollowing(null);
			wanderBack(mob);
		}
	}

	protected boolean wanderBackTo(final MOB mob, final MOB M)
	{
		if(M!=null)
		{
			if(CMLib.flags().isInTheGame(M, true))
			{
				if(M.location()==mob.location())
					return true;
				CMLib.tracking().wanderFromTo(mob, M.location(), false);
				if(M.location() == mob.location())
				{
					if(mob.amFollowing() != M)
						CMLib.commands().postFollow(mob, M, true);
					return true;
				}
			}
		}
		return false;
	}

	protected void wanderBack(final MOB mob)
	{
		final MOB M = mob.amFollowing();
		if(wanderBackTo(mob, M))
			return;
		final Pair<MOB, Long> pimp = this.pimpedInfo;
		if((pimp != null)
		&&(wanderBackTo(mob,pimp.first)))
			return;
		final MOB mM = getMaster();
		if(wanderBackTo(mob,mM))
			return;
		CMLib.tracking().wanderAway(mob,true,true);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
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
					if((A!=null)
					&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL))
					{
						if(!A.tick(ticking,tickID))
							mob.delEffect(A);
					}
				}
			}
			final Pair<MOB,Long> pimpInfo = this.pimpedInfo;
			if((pimpInfo != null)
			&&(System.currentTimeMillis() > pimpInfo.second.longValue()))
			{
				this.pimpedInfo = null;
				mob.setFollowing(null);
				wanderBack(mob);
			}
			if((getObeyName().equals(mob.getLiegeID()))
			&&((--hungerTickDown)<=0))
			{
				hungerTickDown=HUNGERTICKMAX;
				CMLib.combat().expendEnergy(mob,false);
				if((!mob.isInCombat())
				&&(CMLib.dice().rollPercentage()==1)
				&&(CMLib.dice().rollPercentage()<(masterAnger/10)))
				{
					final MOB myMaster=getMaster();
					if((myMaster!=null)
					&&(mob.location().isInhabitant(myMaster)))
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
			if(steps==null)
			{
				// wait to be told to do something
			}
			else
			if((steps.size()==0)||(steps.isDone()))
			{
				if(mob.isInCombat())
					return true; // let them finish fighting.
				if((steps!=null)&&((steps.size()==0)||(steps.isDone())))
					mob.tell(L("You have completed your masters task."));
				else
					mob.tell(L("You have been released from your masters task."));
				if((mob.isMonster())
				&&(!mob.amDead())
				&&(mob.location()!=null)
				&&(!canBeUninvoked()))
					wanderBack(mob);
				unInvoke();
				steps=null;
				return !canBeUninvoked();
			}
			if(steps!=null)
			{
				steps.step();
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
		if((this.masterMOB!=null)
		&&(this.canBeUninvoked))
			unMaster(mob);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
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
		if(CMLib.flags().isUndead(target))
		{
			mob.tell(L("@x1 would not follow your instructions!",target.name(mob)));
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
				((PrivateProperty)A).setOwnerName(mob.Name());
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> attempt(s) to enslave on <T-NAMESELF>, but fail(s)."));

		// return whether it worked
		return success;
	}

	private static String[]			CODES			= null;
	private static final String[]	INTERNAL_CODES	= { "MASTER", "PRICE", "PIMP", "PIMPEXPIRE" };

	@Override
	public String[] getStatCodes()
	{
		if(CODES!=null)
			return CODES;
		final String[] MYCODES=CMProps.getStatCodesList(Skill_Enslave.INTERNAL_CODES,this);
		final String[] superCodes=super.getStatCodes();
		CODES=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			CODES[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			CODES[i]=MYCODES[x];
		return CODES;
	}

	@Override
	protected int getInternalCodeNum(final String code)
	{
		for(int i=0;i<INTERNAL_CODES.length;i++)
		{
			if(code.equalsIgnoreCase(INTERNAL_CODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public String getStat(final String code)
	{
		switch(getInternalCodeNum(code))
		{
		case 0:
			return getOwnerName();
		case 1:
			return ""+getPrice();
		case 2:
		{
			final Pair<MOB, Long> pimpInfo = this.pimpedInfo;
			if(pimpInfo != null)
				return pimpInfo.first.Name();
			return "";
		}
		case 3:
		{
			final Pair<MOB, Long> pimpInfo = this.pimpedInfo;
			if(pimpInfo != null)
				return pimpInfo.second.toString();
			return "";
		}
		default:
			return super.getStat(code);
		}
	}

	@Override
	public void setStat(final String code, final String val)
	{
		switch(getInternalCodeNum(code))
		{
		case 0:
			setOwnerName(val);
			break;
		case 1:
			setPrice(CMath.s_int(val));
			break;
		case 2:
			if(val.trim().length()==0)
				this.pimpedInfo = null;
			else
			{
				MOB M = CMLib.players().getPlayerAllHosts(val.trim());
				if((M==null)
				&&(affected instanceof MOB)
				&&(((MOB)affected).location()!=null))
					M = ((MOB)affected).location().fetchInhabitant(val.trim());
				if(M != null)
				{
					if(this.pimpedInfo == null)
						this.pimpedInfo = new Pair<MOB,Long>(M,Long.valueOf(Long.MAX_VALUE));
					this.pimpedInfo.first = M;
				}
			}
			break;
		case 3:
			if(this.pimpedInfo != null)
				this.pimpedInfo.second=Long.valueOf(CMath.s_long(val));
			break;
		default:
			super.setStat(code, val);
			break;
		}
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof Skill_Enslave))
			return false;
		return super.sameAs(E);
	}
}
