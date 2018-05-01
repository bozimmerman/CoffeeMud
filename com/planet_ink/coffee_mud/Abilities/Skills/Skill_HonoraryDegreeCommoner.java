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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2017-2018 Bo Zimmerman

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
public class Skill_HonoraryDegreeCommoner extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_HonoraryDegreeCommoner";
	}

	private final static String localizedName = CMLib.lang().L("Commoner Honorary Degree");

	@Override
	public String name()
	{
		return localizedName;
	}

	protected String getBaseClassID()
	{
		return "Commoner";
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
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_EDUCATIONLORE;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	protected final static int DEG_CID = 0;
	protected final static int DEG_CNAME = 1;
	protected final static int DEG_TITLE = 2;
	
	private final String[][] getAllDegrees()
	{
		String[][] degrees = (String[][])Resources.getResource("SKILL_"+ID().toUpperCase());
		if(degrees == null)
		{
			final List<CharClass> classes=new ArrayList<CharClass>();
			for(Enumeration<CharClass> c = CMClass.charClasses();c.hasMoreElements();)
			{
				final CharClass C=c.nextElement();
				if(C.baseClass().equals(getBaseClassID())
				&&(C.availabilityCode()!=0)
				&&((C.availabilityCode()&Area.THEME_SKILLONLYMASK)==0))
					classes.add(C);
			}
			final List<String[]> nearFinal = new ArrayList<String[]>();
			for(CharClass c : classes)
				nearFinal.add(new String[]{c.ID(),c.name(),L("*, Honorary @x1",c.name())});
			degrees = nearFinal.toArray(new String[][]{});
			Resources.submitResource("SKILL_"+ID().toUpperCase(), degrees);
		}
		return degrees;
	}

	protected final List<String[]>	myClasses	= new ArrayList<String[]>();
	protected final Set<String>		myTitles	= new HashSet<String>();
	protected volatile int			numSkills	= -1;
	protected volatile CharClass	activatedC	= null;
	protected volatile CharClass	elligibleC	= null;
	protected String				lastTitle	= "";

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			final Physical P=affected;
			if(P instanceof MOB)
			{
				final MOB mob=(MOB)P;
				if(mob.numAbilities()!=numSkills)
				{
					numSkills = mob.numAbilities();
					this.myClasses.clear();
					this.myTitles.clear();
					final String[][] allDegrees = this.getAllDegrees();
					final Ability studyingA=mob.fetchAbility("Studying");
					final AbilityContainer collection=(studyingA==null)?mob:(AbilityContainer)studyingA;
					final int[] counts=new int[allDegrees.length];
					boolean fail=true;
					while(fail)
					{
						fail=false;
						try
						{
							for(Enumeration<Ability> a=collection.abilities();a.hasMoreElements();)
							{
								final Ability A=a.nextElement();
								for(int ci=0;ci<allDegrees.length;ci++)
								{
									if(CMLib.ableMapper().getAbleMap(allDegrees[ci][0], A.ID()) != null)
										counts[ci]++;
								}
							}
						}
						catch(java.util.ConcurrentModificationException e)
						{
							for(int i=0;i<counts.length;i++)
								counts[i]=0;
							fail=true;
						}
					}
					final int threshold = 3;
					synchronized(this.myClasses)
					{
						for(int ci=0;ci<allDegrees.length;ci++)
						{
							if(counts[ci]>threshold)
							{
								final String[] degree = allDegrees[ci];
								this.myClasses.add(degree);
							}
						}
					}
					synchronized(this.myTitles)
					{
						for(int ci=0;ci<allDegrees.length;ci++)
						{
							if(counts[ci]>threshold)
							{
								final String[] degree = allDegrees[ci];
								this.myTitles.add(degree[DEG_TITLE]);
							}
						}
					}
					final PlayerStats pStats = mob.playerStats();
					if((pStats!=null)&&(pStats.getTitles()!=null))
					{
						for(int ci=0;ci<allDegrees.length;ci++)
						{
							final String[] degree = allDegrees[ci];
							if(pStats.getTitles().contains(degree[DEG_TITLE]))
							{
								while(CMParms.numContains(pStats.getTitles(), degree[DEG_TITLE]) > 1)
									pStats.getTitles().remove(degree[DEG_TITLE]);
								if(!this.myClasses.contains(degree))
									pStats.getTitles().remove(degree[DEG_TITLE]);
							}
							else
							if(this.myClasses.contains(degree))
								pStats.getTitles().add(degree[DEG_TITLE]);
						}
					}
					this.lastTitle="";
				}
				final PlayerStats pStats = mob.playerStats();
				if((pStats!=null)
				&&(lastTitle != pStats.getActiveTitle()))
				{
					synchronized(this)
					{
						this.lastTitle=pStats.getActiveTitle();
						this.elligibleC = null;
						if((this.lastTitle!=null)
						&&(myTitles.contains(this.lastTitle)))
						{
							for(final String[] degree : this.myClasses)
							{
								if(degree[DEG_TITLE].equals(this.lastTitle))
								{
									this.elligibleC = CMClass.getCharClass(degree[DEG_CID]);
									this.helpProficiency(mob,0);
									break;
								}
							}
						}
					}
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void affectCharStats(final MOB affectedMob, final CharStats affectableStats)
	{
		super.affectCharStats(affectedMob, affectableStats);
		final CharClass C;
		synchronized(this)
		{
			C=this.activatedC;
		}
		if((C != null)&&(affectableStats.getCurrentClass()!=C))
		{
			final int level = affectableStats.getCurrentClassLevel();
			affectableStats.setCurrentClass(C);
			affectableStats.setCurrentClassLevel(level);
		}
	}

	private void activateDegree(final CMMsg msg)
	{
		synchronized(this)
		{
			if(this.elligibleC != null)
			{
				if(!super.proficiencyCheck(msg.source(), 0, false))
					return;
				
				this.activatedC = this.elligibleC;
				msg.source().recoverCharStats();
			}
			else
				return;
		}
		final Skill_HonoraryDegreeCommoner me = this;
		final MOB mob=msg.source();
		msg.addTrailerRunnable(new Runnable()
		{
			@Override
			public void run()
			{
				synchronized(me)
				{
					me.activatedC = null;
					mob.recoverCharStats();
				}
			}
		});
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if(msg.amISource(mob))
		{
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_WEAR:
			case CMMsg.TYP_WIELD:
			case CMMsg.TYP_GET:
			case CMMsg.TYP_HOLD:
			case CMMsg.TYP_TRAVEL:
			case CMMsg.TYP_ENTER:
			case CMMsg.TYP_LEAVE:
				this.activateDegree(msg);
				break;
			default:
				if(this.activatedC != null)
				{
					synchronized(this)
					{
						this.activatedC = null;
						mob.recoverCharStats();
					}
				}
			}
		}
		return true;
	}
}

