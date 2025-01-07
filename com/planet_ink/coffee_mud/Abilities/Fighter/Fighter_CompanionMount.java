package com.planet_ink.coffee_mud.Abilities.Fighter;
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
   Copyright 2024 github.com/toasted323

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

   CHANGES:
   2024-11 toasted323: reset ticks required to make a loyal companion to 1 rl hour
*/
public class Fighter_CompanionMount extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_CompanionMount";
	}

	private final static String localizedName = CMLib.lang().L("Companion Mount");

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
	public String displayText()
	{
		if((affected instanceof MOB)
		&& (mountMob != null)
		&& (mountTicks < Integer.MAX_VALUE/2))
		{
			final MOB mob = (MOB)affected;
			final MOB mn = mountMob;
			if((mn != null) && (mob != null))
			{
				final TimeClock C = CMLib.time().homeClock(mob);
				if(C != null)
				{
					return L("(Building loyalty with @x1, @x2 remain)",mn.name(),
							C.deriveEllapsedTimeString(mountTicks * CMProps.getTickMillis()));
				}
			}
		}
		return "";
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_ANIMALAFFINITY;
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

	protected volatile int mountTicks = Integer.MAX_VALUE;
	protected volatile MOB mountMob   = null;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(affected instanceof MOB)
		{
			final MOB mob = (MOB)affected;
			final Rideable riding = mob.riding();
			if(riding instanceof MOB)
			{
				MOB target = mountMob;
				if(riding == mountMob)
				{
					if((mountTicks%5==0)&&(CMLib.dice().rollPercentage()<10))
						super.helpProficiency(mob, 0);
					if(!proficiencyCheck(mob,0,false))
						return false;
					mountTicks--;
					if(mountTicks <= 0 )
					{
						if(target.fetchEffect("Loyalty")==null)
						{
							if(mob.location()!=null)
							{
								int numLoyal = 0;
								for(int f=0;f<mob.numFollowers();f++)
								{
									final MOB M=mob.fetchFollower(f);
									if((M!=mob)
									&&(CMLib.flags().isAnAnimal(M))
									&&(M.fetchEffect("Loyalty")!=null))
										numLoyal++;
								}
								if(numLoyal > this.getXLEVELLevel(mob))
								{
									mob.tell(L("You lack the expertise to gain another companion."));
									mountTicks=Integer.MAX_VALUE/2;
									return true;
								}
								final List<Ability> affects = new LinkedList<Ability>();
								for(final Enumeration<Ability> a=target.personalEffects();a.hasMoreElements();)
								{
									final Ability A=a.nextElement();
									affects.add(A);
									target.delEffect(A);
								}
								final MOB targetCopy = (MOB)target.copyOf();
								for(final Ability A : affects)
									target.addEffect(A);
								for(final Enumeration<Ability> a=target.personalEffects();a.hasMoreElements();)
								{
									final Ability A=a.nextElement();
									if((A!=null)
									&&((A.flags()&Ability.FLAG_CHARMING)!=0)
									&&(A.canBeUninvoked()))
									{
										affects.remove(A);
										// in case there is wandering off...
										final Room oldR = target.location();
										oldR.delInhabitant(target);
										target.setLocation(null);
										A.unInvoke();
										oldR.addInhabitant(target);
										target.setLocation(oldR);
										mob.makePeace(true);
										target.makePeace(true);
										if((target.amFollowing()!=mob)
										&&(!target.amDead())
										&&(!target.amDestroyed()))
											target.setFollowing(mob);
									}
								}
								try
								{
									for (Ability A : affects)
									{
										A = (Ability) A.copyOf();
										targetCopy.addEffect(A);
									}
								}
								catch(final Throwable t)
								{
								}

								if(target.amDestroyed() || target.amDead())
								{
									target=targetCopy;
									target.basePhyStats().setRejuv(PhyStats.NO_REJUV);
									target.phyStats().setRejuv(PhyStats.NO_REJUV);
									target.text();
									target.bringToLife(mob.location(), false);
								}
								else
								if(target.location() != mob.location())
								{
									mob.location().bringMobHere(target, true);
									targetCopy.destroy();
								}
								else
									targetCopy.destroy();
								mob.makePeace(true);
								target.makePeace(true);
								if((target.basePhyStats().rejuv()>0)
								&&(target.basePhyStats().rejuv()!=PhyStats.NO_REJUV)
								&&(target.getStartRoom()!=null))
								{
									final MOB oldTarget=target;
									mob.setRiding(null);
									target = (MOB) target.copyOf();
									target.basePhyStats().setRejuv(PhyStats.NO_REJUV);
									target.phyStats().setRejuv(PhyStats.NO_REJUV);
									target.text();
									oldTarget.killMeDead(false);
									target.bringToLife(mob.location(), false);
									mob.setRiding((Rideable)target);
								}
								if(target.amFollowing()!=mob)
									target.setFollowing(mob);
								Ability A=target.fetchEffect("Loyalty");
								if(A==null)
								{
									A=CMClass.getAbility("Loyalty");
									A.setMiscText("NAME="+mob.Name());
									A.setSavable(true);
									target.addNonUninvokableEffect(A);
									mob.tell(mob,target,null,L("<T-NAME> is now a loyal companion to you."));
									mob.location().recoverRoomStats();
								}
							}
						}
						else
							mountTicks = Integer.MAX_VALUE;
					}
				}
				else
				{
					mountMob = (MOB)riding;
					if((mountMob.fetchEffect("Loyalty")==null)
					&&(mountMob.isMonster())
					&&(CMLib.flags().isAnAnimal(mountMob)))
					{
						target = mountMob;
						final PairList<String, Race> choices = CMLib.utensils().getFavoredMounts(mob);
						if((choices.containsSecond(target.baseCharStats().getMyRace()))
							||(choices.containsFirst(target.baseCharStats().getMyRace().racialCategory())))
						{
							final TimeClock C = CMLib.time().homeClock(mob);
							mountTicks = (int)(CMProps.getTicksPerMudHour());
							mountTicks -= (int)(super.getXTIMELevel(mob) * CMProps.getTicksPerMudHour());
							if(mountTicks < 10)
								mountTicks = 10;
						}
						else
							mountTicks = Integer.MAX_VALUE;
					}
					else
						mountTicks = Integer.MAX_VALUE;
				}
			}
		}
		return true;
	}
}
