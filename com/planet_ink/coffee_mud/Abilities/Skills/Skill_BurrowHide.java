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
   Copyright 2016-2018 Bo Zimmerman

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
public class Skill_BurrowHide extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_BurrowHide";
	}

	private final static String	localizedName	= CMLib.lang().L("Burrow Hide");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return isBuried?L("(Hidden in a Burrow)"):"";
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
		return Ability.ACODE_SKILL | Ability.DOMAIN_STEALTHY;
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
	
	protected volatile boolean isBuried = false;

	protected boolean isDiggableRoom(final Room R)
	{
		if(R==null)
			return false;
		if(((R.domainType()&Room.INDOORS)>0)
		&&(R.domainType()!=Room.DOMAIN_INDOORS_CAVE)
		&&((R.getAtmosphere()&RawMaterial.MATERIAL_ROCK)==0))
			return false;
		if((R.domainType()==Room.DOMAIN_OUTDOORS_CITY)
		   ||(R.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
		   ||(R.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		   ||(R.domainType()==Room.DOMAIN_OUTDOORS_AIR)
		   ||(R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
			return false;
		return true;
	}
	
	protected boolean isRodentHere(final Room R, final MOB notM)
	{
		if(R==null)
			return false;
		for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
		{
			final MOB M=m.nextElement();
			if((M!=null)&&(M!=notM)&&(M.isMonster()))
			{
				final Race mR=M.charStats().getMyRace();
				if(mR.racialCategory().equals("Rodent")
				||mR.racialCategory().equals("Worm")
				||mR.racialCategory().equals("Ophidian"))
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		return super.tick(ticking,tickID);
	}

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(isBuried)
		{
			affectableStats.setStat(CharStats.STAT_SAVE_DETECTION,proficiency()+affectableStats.getStat(CharStats.STAT_SAVE_DETECTION));
		}
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(isBuried)
		{
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_HIDDEN);
			if(CMLib.flags().isSneaking(affected))
				affectableStats.setDisposition(affectableStats.disposition()-PhyStats.IS_SNEAKING);
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return;

		final MOB mob=(MOB)affected;

		if(msg.amISource(mob) && (isBuried))
		{
			if(((msg.sourceMinor()==CMMsg.TYP_ENTER)
				||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
				||(msg.sourceMinor()==CMMsg.TYP_FLEE)
				||(msg.sourceMinor()==CMMsg.TYP_RECALL))
			&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
			&&(msg.sourceMajor()>0))
			{
				isBuried=false;
				msg.source().tell("You climb out of the burrow.");
				mob.recoverPhyStats();
				mob.recoverCharStats();
			}
			else
			if((abilityCode()==0)
			&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
			&&(msg.othersMinor()!=CMMsg.TYP_LOOK)
			&&(msg.othersMinor()!=CMMsg.TYP_EXAMINE)
			&&(msg.othersMajor()>0))
			{
				if(msg.othersMajor(CMMsg.MASK_SOUND))
				{
					isBuried=false;
					msg.source().tell("You climb out of the burrow.");
					mob.recoverPhyStats();
					mob.recoverCharStats();
				}
				else
				switch(msg.othersMinor())
				{
				case CMMsg.TYP_SPEAK:
				case CMMsg.TYP_CAST_SPELL:
					{
						isBuried=false;
						msg.source().tell("You climb out of the burrow.");
						mob.recoverPhyStats();
						mob.recoverCharStats();
					}
					break;
				case CMMsg.TYP_OPEN:
				case CMMsg.TYP_CLOSE:
				case CMMsg.TYP_LOCK:
				case CMMsg.TYP_UNLOCK:
				case CMMsg.TYP_PUSH:
				case CMMsg.TYP_PULL:
					if(((msg.target() instanceof Exit)
						||((msg.target() instanceof Item)
						   &&(!msg.source().isMine(msg.target())))))
					{
						isBuried=false;
						msg.source().tell("You climb out of the burrow.");
						mob.recoverPhyStats();
						mob.recoverCharStats();
					}
					break;
				}
			}
		}
		return;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		final Room R=mob.location();
		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.source()!=mob)
		&&(msg.source().isMonster())
		&&(!isBuried)
		&&(msg.target()==R)
		&&(isDiggableRoom(R))
		&&(isRodentHere(R,mob))
		&&(CMLib.flags().isAliveAwakeMobileUnbound(mob, true))
		&&(CMLib.flags().isAggressiveTo(msg.source(), mob))
		&&(msg.source().mayIFight(mob))
		&&(super.proficiencyCheck(mob, 5*(adjustedLevel(mob,0)-msg.source().phyStats().level()), false)))
		{
			mob.tell("Your friends alert you to incoming danger! You quickly burrow out of sight.");
			isBuried=true;
			mob.recoverPhyStats();
			mob.recoverCharStats();
		}
		return true;
	}
}

