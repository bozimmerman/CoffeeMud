package com.planet_ink.coffee_mud.Abilities.Prayers;
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
   Copyright 2014-2018 Bo Zimmerman

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

public class Prayer_AnimalSpirit extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_AnimalSpirit";
	}

	private final static String	localizedName	= CMLib.lang().L("Animal Spirit");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return getAnimalSpirit().getAffectText();
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER | Ability.DOMAIN_COMMUNING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[]	triggerStrings	= I(new String[] { "ANIMALSPIRIT" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
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

	private AnimalSpirit spirit = AnimalSpirit.None; 
	
	public static enum AnimalSpirit 
	{
		None,
		Mouse, 
		Rat, 
		Cat, 
		Lion, 
		Dog, 
		Wolf, 
		Robin, 
		Owl, 
		Hawk, 
		Eagle, 
		GardenSnake("Garden Snake"), 
		Snake, 
		Python,
		Cobra,
		Grasshopper,
		Centipede,
		Tarantula,
		Monkey,
		Chimp,
		Ape,
		Gorilla,
		Cow,
		Bull,
		Buffalo
		;
		private final String displayName;
		private final String raceID;
		private AnimalSpirit(String displayName, String raceID)
		{
			this.displayName=displayName;
			this.raceID=raceID;
		}

		private AnimalSpirit(String displayName)
		{
			this.displayName=displayName;
			this.raceID=null;
		}

		private AnimalSpirit()
		{
			this.displayName=null;
			this.raceID=null;
		}

		public String getRaceID()
		{
			if(raceID == null)
				return this.name();
			return raceID;
		}

		@Override
		public String toString()
		{
			if(displayName == null)
				return this.name();
			return this.displayName;
		}
		
		public String getDisplayName()
		{
			return toString();
		}
		
		public String getAffectText()
		{
			return CMLib.lang().L("(Spirit of the @x1)",getDisplayName());
		}
	}
	
	public AnimalSpirit getAnimalSpirit()
	{
		if((spirit == AnimalSpirit.None)||(spirit==null))
		{
			spirit = AnimalSpirit.values()[CMLib.dice().roll(1, AnimalSpirit.values().length, -1)];
			if(invoker()!=null)
			{
				Ability A=invoker().fetchAbility(ID());
				if(A!=null)
					A.setMiscText(spirit.name());
			}
		}
		return spirit;
	}
	
	@Override
	public void setMiscText(String newText)
	{
		if(newText.length()>0)
		{
			spirit = AnimalSpirit.valueOf(newText);
			if(spirit == null)
				spirit=AnimalSpirit.None;
		}
		super.setMiscText(spirit.name());
	}
	
	@Override
	public String text()
	{
		return getAnimalSpirit().name();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		boolean success = super.proficiencyCheck(mob, 0, auto);

		if(!super.invoke(mob, commands, givenTarget, auto, asLevel))
			return false;
		
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_OK_ACTION,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
			}
		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> go(s) on a vision quest, but nothing happens."));

		// return whether it worked
		return success;
	}
}
