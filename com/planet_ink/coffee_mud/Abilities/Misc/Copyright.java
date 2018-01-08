package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2005-2018 Bo Zimmerman

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

public class Copyright extends StdAbility
{
	@Override
	public String ID()
	{
		return "Copyright";
	}

	private final static String	localizedName	= CMLib.lang().L("Copyright");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return (text().length()>0) ? "(Copyrighted by " + text() + ")" : "";
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
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
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		return true;
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(msg.target()==affected)
		{
			if(msg.targetMinor()==CMMsg.TYP_WRITE)
			{
				if(text().length()>0)
					msg.source().tell(L("This work is copyrighted by @x1 and cannot be modified.",text()));
				else
					msg.source().tell(L("This cannot be modified."));
				return false;
			}
			if(msg.targetMinor()==CMMsg.TYP_REWRITE)
			{
				if(text().length()>0)
					msg.source().tell(L("This work is copyrighted by @x1 and cannot be modified.",text()));
				else
					msg.source().tell(L("This cannot be modified."));
				return false;
			}
			if(msg.tool() instanceof Ability)
			//&&(!msg.source().Name().equals(text()))
			{
				if(msg.tool().ID().equalsIgnoreCase("Transcribing") //BZ: I hate this crap.
				||msg.tool().ID().equalsIgnoreCase("Spell_Duplicate"))
				{
					if(text().length()>0)
						msg.source().tell(L("This work is copyrighted by @x1 and cannot be duplicated.",text()));
					else
						msg.source().tell(L("This cannot be duplicated."));
					return false;
				}
				if(msg.tool().ID().equalsIgnoreCase("BookEditing")) //BZ: I hate this crap.
				{
					if(text().length()>0)
						msg.source().tell(L("This work is copyrighted by @x1 and cannot be edited.",text()));
					else
						msg.source().tell(L("This cannot be edited."));
					return false;
				}
				if(msg.tool().ID().equalsIgnoreCase("BookNaming")
				||msg.tool().ID().equalsIgnoreCase("Titling")) //BZ: I hate this crap.
				{
					if(text().length()>0)
						msg.source().tell(L("This work is copyrighted by @x1 and cannot be re-named.",text()));
					else
						msg.source().tell(L("This cannot be re-named."));
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Physical target=givenTarget;
		if(target==null)
			return false;
		if(target.fetchEffect(ID())!=null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
		}
		return success;
	}
}
