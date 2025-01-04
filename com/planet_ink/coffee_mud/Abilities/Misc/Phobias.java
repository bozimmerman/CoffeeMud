package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.collections.*;
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

import java.io.IOException;
import java.util.*;

/*
   Copyright 2020-2025 Bo Zimmerman

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
public class Phobias extends StdAbility implements HealthCondition
{
	@Override
	public String ID()
	{
		return "Phobias";
	}

	private final static String	localizedName	= CMLib.lang().L("Phobias");

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
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PROPERTY;
	}

	//TODO: phobias against flying, riding?
	protected final static int	CHECK_DOWN		= 3;
	protected List<String>		objectPhobias	= new LinkedList<String>();
	protected Set<String>		racePhobias		= new HashSet<String>();
	protected int				phobicCheckDown	= 0;
	protected volatile String	oldMood			= null;

	protected static int numRaces = 0;
	protected final static Map<String,String>	raceCatMap	= new Hashtable<String,String>();

	protected final static synchronized Map<String,String> getRaceCatMap()
	{
		if(numRaces != CMClass.numPrototypes(CMObjectType.RACE))
		{
			raceCatMap.clear();
			for(final Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
			{
				final Race R=r.nextElement();
				if(!raceCatMap.containsKey(R.racialCategory().toUpperCase()))
					raceCatMap.put(R.racialCategory().toUpperCase(), R.racialCategory());
			}
			numRaces = CMClass.numPrototypes(CMObjectType.RACE);
		}
		return raceCatMap;
	}

	@Override
	public String getHealthConditionDesc()
	{
		final List<String> list=new ArrayList<String>();
		for(final String obj : objectPhobias)
			list.add(CMLib.english().makePlural(obj.toLowerCase()));
		for(final String strr : racePhobias)
			list.add(CMLib.english().makePlural(strr));
		if(list.size()==0)
			return "";
		return "Afraid of "+CMLib.english().toEnglishStringList(list)+".";
	}

	public boolean isScared()
	{
		final Physical P=affected;
		if(P instanceof MOB)
		{
			final Ability moodA=P.fetchEffect("Mood");
			if(moodA==null)
				return false;
			if(moodA.text().equalsIgnoreCase("SCARED"))
				return true;
		}
		return false;
	}

	@Override
	public void setMiscText(final String newText)
	{
		super.setMiscText(newText);
		objectPhobias.clear();
		racePhobias.clear();
		final List<String> V=CMParms.parseCommas(newText.toUpperCase().trim(),true);
		final Map<String,String> allRacialCats = getRaceCatMap();
		for(final String str : V)
		{
			final Race R=CMClass.findRace(str);
			if(R!=null)
				racePhobias.add(R.name());
			else
			if(allRacialCats.containsKey(str))
				racePhobias.add(allRacialCats.get(str));
			else
				objectPhobias.add(str.toLowerCase());
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(((++phobicCheckDown)>CHECK_DOWN)
		&&(affected instanceof MOB))
		{
			phobicCheckDown=0;
			boolean didSomething=false;
			final MOB mob=(MOB)affected;
			if((CMLib.flags().isAliveAwakeMobile(mob,true))
			&&(CMLib.flags().isInTheGame(mob,true)))
			{
				final Room R=CMLib.map().roomLocation(mob);
				if(racePhobias.size()>0)
				{
					MOB M=null;
					for(int i=0;i<R.numInhabitants();i++)
					{
						M=R.fetchInhabitant(i);
						if((M!=null)
						&&(M!=mob)
						&&(this.isPhobic(M))
						&&(CMLib.flags().canBeSeenBy(M, mob))
						&&(CMLib.dice().rollPercentage()<50))
						{
							reactToPhobia(mob,M);
							didSomething=true;
							break;
						}
					}
				}
				if((objectPhobias.size()>0)
				&&(R.numItems()<20))
				{
					Item I=null;
					for(int i=0;i<R.numItems();i++)
					{
						I=R.getItem(i);
						if((I!=null)
						&&(I.container()==null)
						&&(this.isPhobic(I))
						&&(CMLib.flags().canBeSeenBy(I, mob))
						&&(CMLib.dice().rollPercentage()<50))
						{
							reactToPhobia(mob,I);
							didSomething=true;
							break;
						}
					}
				}
			}
			if((!didSomething)&&(this.oldMood!=null))
			{
				final Ability moodA=affected.fetchEffect("Mood");
				if((moodA!=null)
				&&(moodA.text().equalsIgnoreCase("SCARED")))
				{
					final Command C=CMClass.getCommand("Mood");
					if(C!=null)
					{
						try
						{
							if(oldMood.length()==0)
								oldMood="NORMAL";
							C.execute(mob, new XVector<String>("MOOD",oldMood), 0);
							oldMood=null;
						}
						catch (final IOException e)
						{
						}
					}
				}
			}
		}
		return true;
	}

	protected boolean isObjectPhobic(final Environmental E)
	{
		if((E!=null)&&(this.objectPhobias.size()>0))
			return CMLib.english().containsOneOfString(E.Name(), this.objectPhobias);
		return false;
	}

	protected boolean isPhobic(final Environmental P)
	{
		if(P instanceof MOB)
			return isPhobic((MOB)P);
		return isObjectPhobic(P);
	}

	protected boolean isPhobic(final MOB M)
	{
		if(M!=null)
		{
			if(isRacePhobic(M.charStats().getMyRace()))
				return true;
			return isObjectPhobic(M);
		}
		return false;
	}

	protected boolean isRacePhobic(final Race R)
	{
		if(R!=null)
		{
			if(racePhobias.contains(R.name()))
				return true;
			if(racePhobias.contains(R.racialCategory()))
				return true;
		}
		return false;
	}

	public void reactToPhobia(final MOB mob, final Physical fromP)
	{
		final Room R=mob.location();
		if(R==null)
			return;
		if((mob.fetchEffect("Cowering")!=null)||(mob.fetchEffect("Fighter_Whomp")!=null))
			return;
		if(this.oldMood==null)
		{
			final Command C=CMClass.getCommand("Mood");
			if(C!=null)
			{
				try
				{
					final Ability moodA=mob.fetchEffect("Mood");
					this.oldMood=(moodA!=null)?moodA.text():"NORMAL";
					C.execute(mob, new XVector<String>("MOOD","SCARED"), 0);
				}
				catch (final IOException e)
				{
				}
			}
		}
		if(CMLib.dice().rollPercentage()<30)
		{
			final Ability cowerA=CMClass.getAbility("Cowering");
			if(cowerA!=null)
				cowerA.invoke(mob, fromP, true, 0);
			return;
		}
		if(mob.isInCombat())
		{
			final Command fleeC=CMClass.getCommand("Flee");
			final List<String> commands=new XVector<String>("FLEE");
			try
			{
				fleeC.execute(mob, commands, 0);
			}
			catch (final IOException e)
			{
			}
		}
		else
		{
			if(CMLib.dice().rollPercentage()<40)
			{
				if(R.show(mob, fromP, CMMsg.MSG_NOISYMOVEMENT, L("Shaking in fear of <T-NAME>, <S-NAME> vomit(s).")))
					mob.curState().adjHunger(-500, mob.maxState().getHunger());
				return;
			}
			else
			if(CMLib.dice().rollPercentage()<40)
			{
				if(R.show(mob, fromP, CMMsg.MSG_NOISYMOVEMENT, L("Shaking in fear of <T-NAME>, <S-NAME> pass(es) out.")))
				{
					final Ability A=CMClass.findAbility("Fighter_Whomp");
					if(A!=null)
						A.startTickDown(mob, mob, 3);
				}
				return;
			}
			CMLib.tracking().beMobile(mob, true, true, false, false, null, null);
		}
		if(R != mob.location())
			R.show(mob, R, CMMsg.MASK_ALWAYS|CMMsg.MSG_FLEE, L("<S-NAME> flee(s) in fear from @x1.",fromP.Name()));
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((affected!=null)
		&&(affected instanceof MOB))
		{
			if((msg.source()==affected))
			{
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_EAT:
					if(isPhobic(msg.target()))
					{
						msg.source().tell(L("Yea, you aren't going to eat that, ever."));
						return false;
					}
					break;
				case CMMsg.TYP_DRINK:
					if(isPhobic(msg.target()))
					{
						msg.source().tell(L("Yea, you aren't going to drink that, ever."));
						return false;
					}
					break;
				case CMMsg.TYP_GET:
				case CMMsg.TYP_PUSH:
				case CMMsg.TYP_PULL:
					if(isPhobic(msg.target()))
					{
						msg.source().tell(L("You are too scared to do that."));
						return false;
					}
					break;
				}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			if((msg.target()==affected)
			&&((msg.targetMajor(CMMsg.MASK_HANDS))
			   ||(msg.targetMajor(CMMsg.MASK_MOVE)))
			&&(isPhobic(msg.source())))
			{
				final MOB mob=(MOB)affected;
				if((mob.location()!=null)
				&&(mob.location().isInhabitant(msg.source()))
				&&(CMLib.dice().rollPercentage()<20))
				{
					reactToPhobia(mob, msg.source());
				}
			}
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		String choice="";
		if(givenTarget!=null)
		{
			if((commands.size()>0)&&((commands.get(0)).equals(givenTarget.name())))
				commands.remove(0);
			choice=CMParms.combine(commands,0);
			commands.clear();
		}
		else
		if(commands.size()>1)
		{
			choice=CMParms.combine(commands,1);
			while(commands.size()>1)
				commands.remove(1);
		}
		final MOB target=getTarget(mob,commands,givenTarget);

		if(target==null)
			return false;
		if(target.fetchEffect(ID())!=null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final ArrayList<String> allChoices=new ArrayList<String>();
			Race R=null;
			for(final Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
			{
				R=r.nextElement();
				allChoices.add(R.ID().toUpperCase());
			}
			String phobias="";
			if((choice.length()>0)&&(allChoices.contains(choice.toUpperCase())))
				phobias=choice.toUpperCase();
			else
			for(int i=0;i<allChoices.size();i++)
			{
				if((CMLib.dice().roll(1,allChoices.size(),0)==1)
				&&(!(allChoices.get(i).equalsIgnoreCase(mob.charStats().getMyRace().ID().toUpperCase()))))
					phobias+=" "+allChoices.get(i);
			}
			if(phobias.length()==0)
				return false;

			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_OK_VISUAL,"");
			if(target.location()!=null)
			{
				if(target.location().okMessage(target,msg))
				{
					target.location().send(target,msg);
					final Ability A=(Ability)copyOf();
					A.setMiscText(phobias.trim());
					target.addNonUninvokableEffect(A);
				}
			}
			else
			{
				final Ability A=(Ability)copyOf();
				A.setMiscText(phobias.trim());
				target.addNonUninvokableEffect(A);
			}
		}
		return success;
	}
}
