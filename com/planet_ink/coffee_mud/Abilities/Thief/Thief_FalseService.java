package com.planet_ink.coffee_mud.Abilities.Thief;
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
   Copyright 2020-2020 Bo Zimmerman

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
public class Thief_FalseService extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_FalseService";
	}

	private final static String localizedName = CMLib.lang().L("False Service");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedDisplay = CMLib.lang().L("(False Service)");

	@Override
	public String displayText()
	{
		return localizedDisplay;
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
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_DECEPTIVE;
	}

	private static final String[] triggerStrings =I(new String[] {"FALSESERVICE"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final Physical affected=this.affected;
		if(!(affected instanceof MOB))
			return;
		if((msg.sourceMinor()==CMMsg.TYP_HOLYEVENT)
		&&(msg.source() instanceof Deity)
		&&(msg.othersMessage()!=null)
		&&(msg.target()==affected)
		&&(msg.othersMessage().equalsIgnoreCase("SERVICE")||msg.othersMessage().equalsIgnoreCase("SERVICE-CANCEL")))
		{
			if((msg.value()>0)
			&&(msg.othersMessage().equalsIgnoreCase("SERVICE")))
			{
				final MOB mob=(MOB)affected;
				CMLib.leveler().postExperience(mob,null,null,250+((50 + (10+super.getXLEVELLevel(mob)))*msg.value()),false);
				if(room != null)
				{
					final Deity trueD=mob.baseCharStats().getMyDeity();
					final String trueDeity=mob.baseCharStats().getWorshipCharID();
					final String fakeDeity=msg.source().Name();
					if((trueDeity.length()>0)&&(trueD!=null))
					{
						double totalGold = 0;
						final String nameCode = trueD.Name().toUpperCase().trim().replace(' ', '_');
						final Faction F=CMLib.factions().getFaction("DEITY_"+nameCode);
						for(final Enumeration<MOB> m=room.inhabitants();m.hasMoreElements();)
						{
							final MOB M=m.nextElement();
							if((M.baseCharStats().getWorshipCharID().equalsIgnoreCase(fakeDeity))
							||(M.charStats().getWorshipCharID().equals(fakeDeity)))
							{
								if(mob.mayIFight(M) && (M!=mob))
								{
									final double amt=CMLib.beanCounter().getTotalAbsoluteNativeValue(M);
									totalGold += (amt * 0.05);
									CMLib.beanCounter().subtractMoney(M, amt);
								}
							}
							if((M.baseCharStats().getWorshipCharID().equalsIgnoreCase(trueDeity))
							||(M.charStats().getWorshipCharID().equals(trueDeity)))
							{
								if(F!=null)
								{
									if(CMLib.factions().postFactionChange(M,this, F.factionID(), 25))
										M.tell(L("You receive @x1 faction with @x2.",""+25,F.name()));
								}
							}
						}
						if(totalGold > 0)
						{
							final String gStr=CMLib.beanCounter().nameCurrencyShort(CMLib.beanCounter().getCurrency(mob), totalGold);
							mob.tell(L("You manage to filch @x1 from the donation plate.",gStr));
							CMLib.beanCounter().addMoney(mob, totalGold);
						}
					}
				}
			}
			unInvoke();
		}
	}

	protected long timeToNextCast = 0;
	protected volatile boolean activated = false;

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(activated)
			affectableStats.setStat(CharStats.STAT_FAITH, affectableStats.getStat(CharStats.STAT_FAITH)+1000);
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
	}

	@Override
	protected int getTicksBetweenCasts()
	{
		return (int)(CMProps.getTicksPerMudHour() * 5);
	}

	@Override
	protected long getTimeOfNextCast()
	{
		return timeToNextCast;
	}

	@Override
	protected void setTimeOfNextCast(final long absoluteTime)
	{
		timeToNextCast=absoluteTime;
	}

	protected Room room = null;

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell(L("Your false service ends."));

		super.unInvoke();

	}
	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		final Physical affected=this.affected;
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if(room == null)
				room=mob.location();
			final Room R=mob.location();
			if((room != R)
			||(!CMLib.flags().isAliveAwakeMobile(mob, false))
			||(!CMLib.flags().isInTheGame(mob,true))
			||(mob.charStats().getMyDeity()==null))
			{
				final CMMsg msg2=CMClass.getMsg(mob,mob.charStats().getMyDeity(),this,CMMsg.MSG_HOLYEVENT,null,CMMsg.MSG_HOLYEVENT,null,CMMsg.NO_EFFECT,"SERVICE-CANCEL");
				if(R.okMessage(mob,msg2))
					R.send(mob,msg2);
				unInvoke();
			}
			else
			{
				try
				{
					activated=true;
					mob.recoverCharStats();
					final CMMsg msg2=CMClass.getMsg(mob,mob.charStats().getMyDeity(),this,CMMsg.MSG_HOLYEVENT,null,CMMsg.MSG_HOLYEVENT,null,CMMsg.NO_EFFECT,"SERVICE");
					if(R.okMessage(mob,msg2))
						R.send(mob,msg2);
				}
				finally
				{
					activated=false;
					mob.recoverCharStats();
				}
			}
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		room=mob.location();
		if((mob.fetchEffect(this.ID())!=null)||(room==null))
		{
			mob.tell(L("You are already conducting a false service."));
			return false;
		}

		if(mob.isInCombat())
		{
			mob.tell(L("Not while in combat!"));
			return false;
		}

		if(!CMLib.flags().isAliveAwakeMobile(mob, false))
			return false;

		if(!auto)
		{
			if(mob.baseCharStats().getWorshipCharID().length()==0)
			{
				mob.tell(L("You lack the faith to do that."));
				return false;
			}

			if(mob.charStats().getWorshipCharID().equalsIgnoreCase(mob.baseCharStats().getWorshipCharID()))
			{
				mob.tell(L("@x1 would not be amused by you conducting a service.  You need false faith in some other deity first.",mob.baseCharStats().deityName()));
				return false;
			}
			if(!mob.charStats().deityName().equals(mob.charStats().getWorshipCharID()))
			{
				mob.tell(L("You need to un-mask your faith to perform a service."));
				return false;
			}
		}

		if(mob.charStats().getMyDeity()==null)
		{
			mob.tell(L("You need to false faith in a deity to do that."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final MOB highestMOB=getHighestLevelMOB(mob,null);
		final int levelDiff=(mob.phyStats().level()+(2*getXLEVELLevel(mob)))-getMOBLevel(highestMOB);

		final boolean success=(highestMOB==null)||proficiencyCheck(mob,levelDiff*10,auto);

		if(success)
		{
			final String deityName = mob.charStats().getWorshipCharID().length()==0?L("the gods"):mob.charStats().getWorshipCharID();
			final String str=L("<S-NAME> make(s) preparations to perform a religious service for @x1.",deityName);

			final CMMsg msg=CMClass.getMsg(mob,null,this,auto?CMMsg.MSG_OK_ACTION:(CMMsg.MSG_DELICATE_HANDS_ACT|CMMsg.MASK_MOVE),str,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
			if(room.okMessage(mob,msg))
			{
				room.send(mob,msg);
				final CMMsg msg2=CMClass.getMsg(mob,mob.charStats().getMyDeity(),this,CMMsg.MSG_HOLYEVENT,null,CMMsg.MSG_HOLYEVENT,null,CMMsg.NO_EFFECT,"SERVICE");
				if(room.okMessage(mob,msg2))
				{
					final Thief_FalseService svA=(Thief_FalseService)beneficialAffect(mob,mob,Integer.MAX_VALUE/2,asLevel);
					if(svA!=null)
					{
						svA.room=mob.location();
						setTimeOfNextCast(mob);
					}
					mob.recoverCharStats();
				}
				else
					beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to begin a false religious service and fail(s)."));
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to begin a false religious service and fail(s)."));
		return success;
	}
}
