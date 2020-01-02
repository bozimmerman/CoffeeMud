package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.ThinAbility;
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

import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/*
   Copyright 2001-2020 Bo Zimmerman

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
public class Property extends ThinAbility
{
	public Property()
	{
		super();
		// CMClass.bumpCounter(this,CMClass.CMObjectType.ABILITY);//removed for
		// mem & perf
	}

	@Override
	public String ID()
	{
		return "Property";
	}

	@Override
	public String name()
	{
		return "a Property";
	}


	/**
	 * Designates whether, when used as a property/effect, what sort of objects
	 * this ability can affect. Uses the Ability.CAN_* constants.
	 *
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @return a mask showing the type of objects this ability can affect
	 */
	protected int canAffectCode()
	{
		return 0;
	}

	/**
	 * Designates whether, when invoked as a skill, what sort of objects this
	 * ability can effectively target. Uses the Ability.CAN_* constants.
	 *
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @return a mask showing the type of objects this ability can target
	 */
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public boolean canTarget(final int can_code)
	{
		return CMath.bset(canTargetCode(), can_code);
	}

	@Override
	public boolean canAffect(final int can_code)
	{
		return CMath.bset(canAffectCode(), can_code);
	}

	@Override
	public void setDescription(final String newDescription)
	{
	}

	@Override
	public void setDisplayText(final String newDisplayText)
	{
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PROPERTY;
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return this.getClass().newInstance();
		}
		catch (final Exception e)
		{
			Log.errOut(ID(), e);
		}
		return new Property();
	}

	private void cloneFix(final Ability E)
	{
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final Property E=(Property)this.clone();
			//CMClass.bumpCounter(E,CMClass.CMObjectType.ABILITY);//removed for mem & perf
			E.cloneFix(this);
			return E;

		}
		catch(final CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	@Override
	public boolean canAffect(final Physical P)
	{
		if((P==null)&&(canAffectCode()==0))
			return true;
		if(P==null)
			return false;
		if((P instanceof MOB)&&((canAffectCode()&Ability.CAN_MOBS)>0))
			return true;
		if((P instanceof Item)&&((canAffectCode()&Ability.CAN_ITEMS)>0))
			return true;
		if((P instanceof Exit)&&((canAffectCode()&Ability.CAN_EXITS)>0))
			return true;
		if((P instanceof Room)&&((canAffectCode()&Ability.CAN_ROOMS)>0))
			return true;
		if((P instanceof Area)&&((canAffectCode()&Ability.CAN_AREAS)>0))
			return true;
		return false;
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof ThinAbility))
			return false;
		return super.sameAs(E);
	}
}
