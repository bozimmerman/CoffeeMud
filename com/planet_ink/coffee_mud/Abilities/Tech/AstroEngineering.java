package com.planet_ink.coffee_mud.Abilities.Tech;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.Abilities.Common.CommonSkill;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2014-2018 Bo Zimmerman

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

public class AstroEngineering extends TechSkill
{
	@Override
	public String ID()
	{
		return "AstroEngineering";
	}

	private final static String	localizedName	= CMLib.lang().L("Astro Engineering");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "ASTROENGINEER", "ASTROENGINEERING", "ENGINEER", "AE" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	protected volatile int			baseTickSpan	= Integer.MAX_VALUE;
	protected volatile boolean		aborted			= false;
	protected volatile boolean		failure			= false;
	protected volatile Item			targetItem		= null;
	protected volatile Item			targetPanel		= null;
	protected volatile Room			targetRoom		= null;
	protected volatile Operation	op				= Operation.REPAIR;
	protected volatile String		altverb			= "";

	protected static enum Operation
	{
		INSTALL("installing"),
		REPAIR("repairing"),
		ENHANCE("enhancing");
		public String verb="";
		private Operation(String verb)
		{
			this.verb=verb;
		}
	}

	public int getBonus(MOB mob, Electronics item, int multiplyBy)
	{
		if((mob==null)||(item==null))
			return 0;
		double score = 0.0;
		final Manufacturer m=item.getFinalManufacturer();
		if(m==null)
			score+=0.5;
		else
		{
			final Pair<String,Integer> manuExpertise=mob.fetchExpertise(m.name()+"%");
			if(manuExpertise!=null)
				score += (0.5 * CMath.div(manuExpertise.second.intValue(), 100.0));
		}
		final Technical.TechType ttype = item.getTechType();
		if(ttype==null)
			score+=0.5;
		else
		{
			final Pair<String,Integer> techTypeExpertise=mob.fetchExpertise(ttype.getDisplayName()+"%");
			if(techTypeExpertise!=null)
				score += (0.5 * CMath.div(techTypeExpertise.second.intValue(), 100.0));
		}
		return (int)Math.round(CMath.mul(multiplyBy, score));
	}

	public void giveBonus(MOB mob, Electronics item)
	{
		if((mob==null)||(item==null))
			return;

		if((System.currentTimeMillis()-lastCastHelp)<300000)
			return;

		String experName;
		if(CMLib.dice().rollPercentage()>50)
		{
			final Manufacturer m=item.getFinalManufacturer();
			if(m!=null)
			{
				experName=m.name();
			}
			else
				return;
		}
		else
		{
			final Technical.TechType ttype = item.getTechType();
			if(ttype!=null)
			{
				experName=ttype.getDisplayName();
			}
			else
				return;
		}
		Pair<String,Integer> expertise=mob.fetchExpertise(experName+"%");
		final int currentExpertise=(expertise!=null)?expertise.second.intValue():0;
		if(((int)Math.round(Math.sqrt((mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)))*34.0*Math.random()))>=currentExpertise)
		{
			mob.addExpertise(experName+"%"+(currentExpertise+1));
			lastCastHelp=System.currentTimeMillis();
			AstroEngineering A=(AstroEngineering)mob.fetchAbility(ID());
			if(A!=null)
				A.lastCastHelp=System.currentTimeMillis();
			mob.tell(mob,null,null,L("You gain some new insights about @x1.",CMLib.english().makePlural(experName.toLowerCase())));
		}
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected instanceof MOB)
			&&(((MOB)affected).location()!=null))
			{
				final MOB mob=(MOB)affected;
				final Room room=mob.location();
				if((aborted)||(room==null))
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> stop(s) @x1.",op.verb));
				else
				if(failure)
				{
					mob.location().show(mob,targetItem,CMMsg.MSG_OK_VISUAL,L("<S-NAME> mess(es) up @x1 <T-NAME>.",op.verb));
				}
				else
				if(op==Operation.INSTALL)
				{
					final CMMsg msg=CMClass.getMsg(mob,targetPanel,targetItem,CMMsg.MSG_INSTALL,L("<S-NAME> install(s) <O-NAME> into <T-NAME>."));
					msg.setValue(50+getBonus(mob,(Electronics)targetItem,50));
					if(room.okMessage(msg.source(), msg))
					{
						room.send(msg.source(), msg);
						giveBonus(mob,(Electronics)targetItem);
					}
				}
				else
				if(op==Operation.REPAIR)
				{
					final CMMsg msg=CMClass.getMsg(mob,targetItem,this,CMMsg.MSG_REPAIR,L("<S-NAME> <S-IS-ARE> done trying to repair <T-NAME>."));
					msg.setValue(CMLib.dice().roll(1, proficiency()/2, getBonus(mob,(Electronics)targetItem,50)));
					if(room.okMessage(msg.source(), msg))
					{
						room.send(msg.source(), msg);
						giveBonus(mob,(Electronics)targetItem);
					}
				}
				else
				{
					String verb=altverb;
					final CMMsg msg=CMClass.getMsg(mob,targetItem,this,CMMsg.MSG_ENHANCE,L("<S-NAME> <S-IS-ARE> done @x1 <T-NAME>.",verb));
					msg.setValue((proficiency()/2)+getBonus(mob,(Electronics)targetItem,50));
					if(room.okMessage(msg.source(), msg))
					{
						room.send(msg.source(), msg);
						giveBonus(mob,(Electronics)targetItem);
					}
				}
			}
			aborted = false;
			targetItem = null;
			targetPanel = null;
			targetRoom = null;
		}
		super.unInvoke();
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if((mob.isInCombat())
			||(mob.location()!=targetRoom)
			||(!CMLib.flags().isAliveAwakeMobileUnbound(mob,true)))
			{
				aborted=true;
				unInvoke();
				return false;
			}
			if(tickDown==4)
			{
				String verb=op.verb;
				if(op==Operation.ENHANCE)
					verb=altverb;
				mob.location().show(mob,targetItem,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> <S-IS-ARE> almost done @x1 <T-NAME>.",verb));
			}
			else
			if(((baseTickSpan-tickDown)%4)==0)
			{
				final int total=baseTickSpan;
				final int tickUp=(baseTickSpan-tickDown);
				final int pct=(int)Math.round(CMath.div(tickUp,total)*100.0);
				String verb=op.verb;
				if(op==Operation.ENHANCE)
					verb=altverb;
				mob.location().show(mob,targetItem,this,CMMsg.MSG_NOISYMOVEMENT,
						L("<S-NAME> continue(s) @x1 <T-NAME> (@x2% completed).",verb,""+pct),
						null,L("<S-NAME> continue(s) @x1 <T-NAME>.",verb));
			}
			if((mob.soulMate()==null)
			&&(mob.playerStats()!=null)
			&&(mob.location()!=null)
			&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.HYGIENE)))
				mob.playerStats().adjHygiene(PlayerStats.HYGIENE_COMMONDIRTY);
		}

		if(!super.tick(ticking,tickID))
			return false;
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((myHost instanceof MOB)&&(myHost == this.affected)&&(((MOB)myHost).location()!=null))
		{
			if((msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
			||((msg.sourceMinor()==CMMsg.TYP_QUIT)&&(msg.amISource((MOB)myHost))))
			{
				aborted=true;
				unInvoke();
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell(L("What would you like to install, repair, or enhance?"));
			return false;
		}
		aborted = false;
		failure = false;
		targetItem = null;
		targetPanel = null;
		targetRoom = mob.location();
		op=Operation.REPAIR;
		int minTicks=8;
		if("INSTALL".startsWith((commands.get(0)).toUpperCase()))
		{
			op=Operation.INSTALL;
			commands.remove(0);
			if(givenTarget instanceof Item)
			{
				targetPanel=(Item)givenTarget;
			}
			else
			if(commands.size()<2)
			{
				mob.tell(L("You need to specify an item to install and a panel to install it into."));
				return false;
			}
			else
			{
				final String panelName=commands.get(commands.size()-1);
				targetPanel=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,panelName);
				if(targetPanel==null)
				{
					targetPanel=targetRoom.findItem(null,panelName);
				}
				if((targetPanel==null)||(!CMLib.flags().canBeSeenBy(targetPanel,mob)))
				{
					mob.tell(L("You don't see '@x1' here.",panelName));
					return false;
				}
				if(!(targetPanel instanceof ElecPanel))
				{
					mob.tell(L("That's not an electronics panel."));
					return false;
				}
				commands.remove(commands.size()-1);
			}
		}
		else
		if("REPAIR".startsWith((commands.get(0)).toUpperCase()))
		{
			op=Operation.REPAIR;
			commands.remove(0);
		}
		else
		if("ENHANCE".startsWith((commands.get(0)).toUpperCase()))
		{
			op=Operation.ENHANCE;
			final String[] verbs=CMProps.getListFileStringList(CMProps.ListFile.TECH_BABBLE_VERBS);
			final String[] adjs1=CMProps.getListFileStringList(CMProps.ListFile.TECH_BABBLE_ADJ1);
			final String[] adjs2=CMProps.getListFileStringList(CMProps.ListFile.TECH_BABBLE_ADJ2);
			final String[] adjs12=new String[adjs1.length+adjs2.length];
			System.arraycopy(adjs1, 0, adjs12, 0, adjs1.length);
			System.arraycopy(adjs2, 0, adjs12, adjs1.length, adjs2.length);
			final String[] nouns=CMProps.getListFileStringList(CMProps.ListFile.TECH_BABBLE_NOUN);
			altverb=verbs[CMLib.dice().roll(1, verbs.length, -1)].trim()+" "
					+adjs12[CMLib.dice().roll(1, adjs12.length, -1)].trim()+" "
					+adjs2[CMLib.dice().roll(1, adjs2.length, -1)].trim()+" "
					+nouns[CMLib.dice().roll(1, nouns.length, -1)].trim()
					;
			commands.remove(0);
		}
		final String itemName=CMParms.combine(commands,0);
		if(targetItem == null)
			targetItem=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,itemName);
		if(targetItem==null)
			targetItem=targetRoom.findItem(null,itemName);
		if((targetItem==null)&&(targetPanel==null))
		{
			final CMFlagLibrary flagLib=CMLib.flags();
			for(int i=0;i<targetRoom.numItems();i++)
			{
				final Item I=targetRoom.getItem(i);
				if((flagLib.isOpenAccessibleContainer(I))
				&&(flagLib.canBeSeenBy(I, mob)))
				{
					targetItem=targetRoom.findItem(I,itemName);
					if(targetItem!=null)
						break;
				}
			}
		}
		if((targetItem==null)||(!CMLib.flags().canBeSeenBy(targetItem,mob)))
		{
			mob.tell(L("You don't see '@x1' here.",itemName));
			return false;
		}
		if(!(targetItem instanceof Electronics))
		{
			mob.tell(L("That's not an electronics item."));
			return false;
		}
		if(!(targetItem instanceof TechComponent))
		{
			mob.tell(L("That's not a space ship component."));
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell(mob,null,null,L("<S-NAME> <S-IS-ARE> in combat!"));
			return false;
		}
		if(!CMLib.flags().canBeSeenBy(targetRoom,mob))
		{
			mob.tell(mob,null,null,L("<S-NAME> can't see to do that!"));
			return false;
		}
		if(CMLib.flags().isSitting(mob)||CMLib.flags().isSleeping(mob))
		{
			mob.tell(mob,null,null,L("You need to stand up!"));
			return false;
		}
		if(op==Operation.REPAIR)
		{
			if(!targetItem.subjectToWearAndTear())
			{
				mob.tell(mob,targetItem,null,L("<T-NAME> can't be repaired!"));
				return false;
			}
		}
		else
		if(op==Operation.ENHANCE)
		{
			if((targetItem.subjectToWearAndTear())&&(targetItem.usesRemaining()<100))
			{
				mob.tell(mob,targetItem,null,L("<T-NAME> must be repaired first!"));
				return false;
			}
			if(targetItem instanceof TechComponent)
			{
				if(((TechComponent)targetItem).getInstalledFactor()>1.0)
					minTicks+=(int)Math.round((((TechComponent)targetItem).getInstalledFactor()-1.0)*0.01);
			}
		}
		for(final Enumeration<Ability> a=mob.personalEffects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)||(A.ID().equalsIgnoreCase("AstroEngineering"))))
			{
				if(A instanceof AstroEngineering)
					((AstroEngineering)A).aborted=true;
				A.unInvoke();
			}
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		failure=!proficiencyCheck(mob,0,auto);

		baseTickSpan = targetItem.basePhyStats().weight()/4;
		if(failure)
			baseTickSpan=baseTickSpan/2;
		if(baseTickSpan<minTicks)
			baseTickSpan=minTicks;
		mob.location().show(mob,targetItem,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> start(s) @x1 <T-NAME>@x2.",op.verb,((targetPanel!=null)?" into "+targetPanel.name():"")));
		beneficialAffect(mob, mob, asLevel, baseTickSpan);

		return !failure;
	}
}
