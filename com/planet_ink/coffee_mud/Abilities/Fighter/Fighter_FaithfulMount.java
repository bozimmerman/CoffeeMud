package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
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
   Copyright 2023-2024 Bo Zimmerman

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
public class Fighter_FaithfulMount extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_FaithfulMount";
	}

	private final static String localizedName = CMLib.lang().L("Faithful Mount");

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

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.sourceMinor()==CMMsg.TYP_DEATH)
		&&(msg.source()==affected)
		&&(super.proficiencyCheck(msg.source(), 0, false)))
		{
			Log.debugOut("Fighter_FaithfulMount", "Death event triggered for " + msg.source().Name());
			super.helpProficiency(msg.source(), 0);
			MOB riddenM = null;
			final Set<? extends Rider> grp = msg.source().getGroupMembersAndRideables(new XTreeSet<Rider>());
			for(final Rider R : grp)
			{
				if(R instanceof MOB)
				{
					final MOB M = (MOB)R;
					if((M.isMonster())
					&&(CMLib.flags().isAnimalIntelligence(M))
					&&(M instanceof Rideable)
					&&(M.location()==msg.source().location())
					&&(CMLib.flags().isAliveAwakeMobileUnbound(M, true)))
					{
						final PairList<String, Race> choices = CMLib.utensils().getFavoredMounts(msg.source());
						if((choices.containsSecond(M.baseCharStats().getMyRace()))
							||(choices.containsFirst(M.baseCharStats().getMyRace().racialCategory())))
						{
							if(msg.source().riding()==M)
							{
								riddenM = M;
								Log.debugOut("Fighter_FaithfulMount", "Found ridden mount: " + M.Name());
								break;
							}
							else
								riddenM = M;
						}
					}
				}
			}
			if(riddenM != null)
			{
				final MOB rM = riddenM;
				final Fighter_FaithfulMount almostMeA = this;
				msg.addTrailerRunnable(new Runnable() {
					final MOB deadM = msg.source();
					final MOB riddenM = rM;
					final Fighter_FaithfulMount meA = almostMeA;
					@Override
					public void run()
					{
						Log.debugOut("Fighter_FaithfulMount", "Trailer runnable started for " + deadM.Name());
						final Room R = riddenM.location();
						if((deadM.amDead()
							||(deadM.location() != riddenM.location())
							||(deadM.fetchEffect("Prop_AstralSpirit")!=null))
						&&(CMLib.flags().isAliveAwakeMobileUnbound(riddenM, true))
						&&(R!=null)
						&&(deadM.location() != null)
						&&(deadM.location() != R))
						{
							Log.debugOut("Fighter_FaithfulMount", "Conditions met for corpse retrieval");
							DeadBody dbI = null;
							for(final Enumeration<Item> i = R.items();i.hasMoreElements();)
							{
								final Item I = i.nextElement();
								if((I instanceof DeadBody)
								&&(((DeadBody)I).getMobName().equals(deadM.Name())))
									dbI = (DeadBody)I;
							}
							if(dbI != null)
							{
								Log.debugOut("Fighter_FaithfulMount", "Corpse found: " + dbI.Name());
								if(riddenM.isInCombat())
									riddenM.makePeace(false);
								riddenM.moveItemTo(dbI);
								Log.debugOut("Fighter_FaithfulMount", "Corpse moved to mount: " + riddenM.Name());
								CMLib.tracking().wanderAway(riddenM, false, false);
								if(riddenM.location() != R)
								{
									Log.debugOut("Fighter_FaithfulMount", "Mount moved to new location");
									final Room tempR = CMClass.getLocale("StdRoom");
									tempR.setDisplayText(L("The trail"));
									tempR.setArea(CMClass.getAreaType("StdArea"));
									tempR.setDescription(L("You are on your way from where you were, to where you need to be."));
									riddenM.location().delInhabitant(riddenM);
									tempR.addInhabitant(riddenM);
									long delayTime = 60000
											- (2000 * meA.getXLEVELLevel(deadM))
											- (4000 * meA.getXTIMELevel(deadM));
									if(delayTime <= 0)
										delayTime = 1000;
									Log.debugOut("Fighter_FaithfulMount", "Calculated delay time: " + delayTime + "ms");
									deadM.tell(L("Your faithful mount is on the way with your corpse!"));
									final MOB deadM2 = deadM;
									final MOB riddenM2 = riddenM;
									final DeadBody dbI2 = dbI;
									final Runnable[] reEnter = new Runnable[1];
									reEnter[0] =new Runnable() {
										final MOB deadM = deadM2;
										final MOB riddenM = riddenM2;
										final DeadBody bodyI = dbI2;
										final Runnable meRun = reEnter[0];
										int attempts = 500;
										@Override
										public void run()
										{
											Log.debugOut("Fighter_FaithfulMount", "Re-enter runnable started, attempt: " + (501 - attempts));
											final Room tR = deadM.location();
											if(CMLib.flags().isInTheGame(deadM, true))
											{
												CMLib.tracking().wanderIn(riddenM, tR);
												if(riddenM.location() == tR)
												{
													Log.debugOut("Fighter_FaithfulMount", "Mount arrived at target room");
													tR.show(riddenM, bodyI, CMMsg.MSG_DROP,L("<S-NAME> arrive(s) and allow(s) <T-NAME> to gently slide to the ground."));
													if(bodyI.owner() != tR)
														tR.moveItemTo(bodyI, Expire.Player_Body);
													Log.debugOut("Fighter_FaithfulMount", "Corpse delivered successfully");
												}
												return;
											}
											if(--attempts>0)
											{
												Log.debugOut("Fighter_FaithfulMount", "Rescheduling re-enter runnable, attempts left: " + attempts);
												CMLib.threads().scheduleRunnable(meRun, 10000);
											}
											else
											{
												Log.debugOut("Fighter_FaithfulMount", "Re-enter attempts exhausted");
											}
										}
									};
									CMLib.threads().scheduleRunnable(reEnter[0], delayTime);
								}
							}
							else
							{
								Log.debugOut("Fighter_FaithfulMount", "Corpse not found for " + deadM.Name());
							}
						}
						else
						{
							Log.debugOut("Fighter_FaithfulMount", "Conditions not met for corpse retrieval");
						}
					}
				});
			}
			else
			{
				Log.debugOut("Fighter_FaithfulMount", "No suitable mount found for " + msg.source().Name());
			}
		}
		return true;
	}
}
