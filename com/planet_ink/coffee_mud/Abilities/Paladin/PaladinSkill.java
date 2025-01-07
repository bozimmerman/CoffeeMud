package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2010-2024 Bo Zimmerman

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
public class PaladinSkill extends StdAbility
{
	@Override
	public String ID()
	{
		return "PaladinSkill";
	}

	private final static String	localizedName	= CMLib.lang().L("Paladin Skill");

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
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
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
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	protected Set<MOB>			paladinsGroup	= null;
	protected final Set<MOB> 	buildGrp		= new HashSet<MOB>(); // can be hashset because its temp use
	protected volatile boolean	rebuildGroup	= true;

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL;
	}

	@Override
	public CMObject copyOf()
	{
		if(paladinsGroup==null)
			return super.copyOf();
		final PaladinSkill P=(PaladinSkill)super.copyOf();
		if(P==null)
			return super.copyOf();
		P.paladinsGroup=new SHashSet<MOB>(paladinsGroup);
		return P;
	}

	protected static boolean paladinAlignmentCheck(final StdAbility A, final MOB mob, final boolean auto)
	{
		if((!auto)
		&&(!mob.isMonster())
		&&(!A.disregardsArmorCheck(mob))
		&&(mob.isMine(A))
		&&(!A.appropriateToMyFactions(mob)))
		{
			mob.tell(CMLib.lang().L("You don't feel worthy enough to @x1.",A.name()));
			return false;
		}
		return true;
	}

	protected static boolean isPaladinGoodSide(final MOB mob)
	{
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.GOOD))
		{
			if(CMLib.flags().isGood(mob))
			{
				if(CMLib.factions().isAlignmentLoaded(Faction.Align.LAWFUL))
					return CMLib.flags().isLawful(mob);
				return true;
			}
			else
				return false;
		}
		else
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.LAWFUL))
			return CMLib.flags().isLawful(mob);
		return false;
	}

	protected static boolean isPaladinAntiSide(final MOB mob)
	{
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.GOOD))
		{
			if(CMLib.flags().isEvil(mob))
			{
				if(CMLib.factions().isAlignmentLoaded(Faction.Align.LAWFUL))
					return CMLib.flags().isChaotic(mob);
				return true;
			}
			else
				return false;
		}
		else
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.LAWFUL))
			return CMLib.flags().isChaotic(mob);
		return false;
	}

	@Override
	public boolean canBeTaughtBy(final MOB teacher, final MOB student)
	{
		if(!super.canBeTaughtBy(teacher, student))
			return false;
		if(student != null)
		{
			final Faction F = super.getInappropriateFaction(student);
			if(F!=null)
			{
				teacher.tell(L("@x1 lacks the correct moral @x2 to learn '@x3'.",student.name(),F.name().toLowerCase(),name()));
				student.tell(L("You lack the correct moral @x1 to learn '@x2'.",F.name(), name()));
				return false;
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_ENTER:
		case CMMsg.TYP_LEAVE:
		case CMMsg.TYP_RECALL:
		case CMMsg.TYP_FOLLOW:
		case CMMsg.TYP_REBUKE:
		case CMMsg.TYP_LIFE:
			rebuildGroup=true;
			break;
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(!(affected instanceof MOB))
			return false;
		final MOB paladinMob=(invoker == null) ? (MOB)affected : invoker;
		if(paladinMob==null)
			return false;
		if(!appropriateToMyFactions(paladinMob))
			return false;
		final Set<MOB> paladinsGroup=this.paladinsGroup;
		if((paladinsGroup!=null)
		&&(rebuildGroup))
		{
			synchronized(buildGrp)
			{
				rebuildGroup=false;
				buildGrp.clear();
				paladinMob.getGroupMembers(buildGrp);
				for(final Iterator<MOB> i = buildGrp.iterator(); i.hasNext(); )
				{
					final MOB M=i.next();
					if(M.location()!=paladinMob.location())
						i.remove();
				}
				paladinsGroup.clear();
				paladinsGroup.addAll(buildGrp);
			}
		}
		if((CMLib.dice().rollPercentage()==1)
		&&(CMLib.dice().rollPercentage()<10))
			helpProficiency(paladinMob, 0);
		return true;
	}

	@Override
	public boolean autoInvocation(final MOB mob, final boolean force)
	{
		return super.autoInvocation(mob, force);
	}
}
