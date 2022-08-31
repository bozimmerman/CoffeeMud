package com.planet_ink.coffee_mud.Abilities.Diseases;
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
   Copyright 2003-2022 Bo Zimmerman

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
public class Disease_Vampirism extends Disease
{
	@Override
	public String ID()
	{
		return "Disease_Vampirism";
	}

	private final static String	localizedName	= CMLib.lang().L("Vampirism");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Vampirism)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
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
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	protected int DISEASE_TICKS()
	{
		return CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY) * 6;
	}

	@Override
	protected int DISEASE_DELAY()
	{
		return CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY);
	}

	@Override
	protected String DISEASE_DONE()
	{
		return L("Your vampirism lifts.");
	}

	@Override
	protected String DISEASE_START()
	{
		return L("^G<S-NAME> seem(s) pale and cold.^?");
	}

	@Override
	protected String DISEASE_AFFECT()
	{
		return "";
	}

	@Override
	public int spreadBitmap()
	{
		return DiseaseAffect.SPREAD_CONSUMPTION;
	}

	@Override
	public int difficultyLevel()
	{
		return 9;
	}

	protected final static String cancelID="Spell_DarkSensitivity";

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(!(affected instanceof MOB))
			return;
		if(((MOB)affected).location()==null)
			return;
		if(CMLib.flags().isInDark(((MOB)affected).location()))
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_DARK);
		else
		{
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-50);
			affectableStats.setArmor(affectableStats.armor()+50);
		}
	}

	protected boolean isLightBlind(final MOB M)
	{
		final Room R=M.location();
		if(R==null)
			return true;
		return !CMLib.flags().isInDark(R);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(msg.source()==affected)
		{
			if((msg.tool() instanceof Ability)
			&&(msg.tool().ID().equals("Skill_Swim")))
			{
				msg.source().tell(L("You can't swim!"));
				return false;
			}
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_EXAMINE:
				if(isLightBlind(msg.source())
				&& (!(msg.target() instanceof Room))
				&&(msg.source().fetchEffect(cancelID)==null))
				{
					msg.source().tell(L("You can't seem to make it out that well in this bright light."));
					return false;
				}
				break;
			case CMMsg.TYP_JUSTICE:
			{
				if(!msg.targetMajor(CMMsg.MASK_DELICATE))
					return true;
			}
				//$FALL-THROUGH$
			case CMMsg.TYP_DELICATE_HANDS_ACT:
			case CMMsg.TYP_CAST_SPELL:
			{
				if((msg.target()!=null)
				&&(msg.target()!=msg.source())
				&&(!(msg.target() instanceof Room))
				&&(isLightBlind(msg.source()))
				&&(msg.source().fetchEffect(cancelID)==null))
				{
					msg.source().tell(msg.source(),msg.target(),null,L("You can't seem to make out <T-NAME> in this bright light."));
					return false;
				}
			}
			}
		}
		return true;
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null)
			return;
		affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)+1);
	}
}
