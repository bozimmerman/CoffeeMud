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

/*
   Copyright 2019-2020 Bo Zimmerman

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
public class Disease_ColorBlindness extends Disease implements Session.SessionFilter
{
	@Override
	public String ID()
	{
		return "Disease_ColorBlindness";
	}

	private final static String localizedName = CMLib.lang().L("Color Blindness");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Color Blindness)");

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
	public int difficultyLevel()
	{
		return 10;
	}

	@Override
	protected int DISEASE_TICKS()
	{
		return 99999;
	}

	@Override
	protected int DISEASE_DELAY()
	{
		return CMProps.getIntVar( CMProps.Int.TICKSPERMUDDAY );
	}

	@Override
	protected String DISEASE_DONE()
	{
		return L("Your color blindness is cured!");
	}

	@Override
	protected String DISEASE_START()
	{
		return L("^G<S-NAME> can't see colors.^?");
	}

	@Override
	protected String DISEASE_AFFECT()
	{
		return "";
	}

	@Override
	public int abilityCode()
	{
		return 0; // no spreading
	}

	@Override
	public int spreadBitmap()
	{
		return 0; // no spreading
	}

	protected volatile boolean checked = false;

	protected void apply(final Physical affected)
	{
		if(!checked)
		{
			if(affected instanceof MOB)
			{
				final Session S=((MOB)affected).session();
				if((S != null)
				&&(affected.fetchEffect(ID())==this))
				{
					S.addSessionFilter(this);
					checked=true;
				}
			}
		}
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		apply(affected);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if((myHost == affected)
		&&(affected instanceof MOB))
			apply(affected);
	}

	@Override
	public String applyFilter(final MOB mob, final Physical source, final Environmental target, final Environmental tool, final String msg)
	{
		if((mob == affected)
		&&(mob.fetchEffect(ID())==this))
		{
			return CMStrings.removeColors(msg);
		}
		return null;
	}
}
