package com.planet_ink.coffee_mud.Items.CompTech;
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
import com.planet_ink.coffee_mud.Items.BasicTech.StdElecItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechCommand;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2015-2018 Bo Zimmerman

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
public class StdElecCompSensor extends StdElecCompItem implements TechComponent
{
	@Override
	public String ID()
	{
		return "StdElecCompSensor";
	}

	@Override
	public TechType getTechType()
	{
		return Technical.TechType.SHIP_SENSOR;
	}

	private static final Filterer<SpaceObject> acceptEverythingFilter = new Filterer<SpaceObject>()
	{
		@Override
		public boolean passesFilter(SpaceObject obj)
		{
			return true;
		}
	};
	
	private static final Converter<SpaceObject, Environmental> directConverter = new Converter<SpaceObject, Environmental>()
	{
		@Override
		public Environmental convert(SpaceObject obj)
		{
			return obj;
		}
	};
	
	/**
	 * The maximum range of objects that this sensor can detect
	 * @return maximum range of objects that this sensor can detect
	 */
	protected long getSensorMaxRange()
	{
		return SpaceObject.Distance.Parsec.dm;
	}
	
	/**
	 * Filter to pick out which objects this sensor can actually pick up.
	 * @see com.planet_ink.coffee_mud.core.collections.Filterer
	 * @return a Filterer to pick out which objects this sensor can actually pick up.
	 */
	protected Filterer<SpaceObject> getSensedObjectFilter()
	{
		return acceptEverythingFilter;
	}

	/**
	 * Converter to convert from the actual sensed object, to a CMObject, which may
	 * or may not contain all the information of the actual one.
	 * @see com.planet_ink.coffee_mud.core.collections.Converter
	 * @return  to convert from the actual sensed object, to a CMObject
	 */
	protected Converter<SpaceObject, Environmental> getSensedObjectConverter()
	{
		return directConverter;
	}
	
	protected boolean doSensing(MOB mob, Software controlI)
	{
		final SpaceObject O=CMLib.map().getSpaceObject(this, true);
		if((O!=null)&&(this.powerRemaining() > this.powerNeeds())) 
		{
			final long maxRange = Math.round(getSensorMaxRange() * this.getComputedEfficiency());
			final List<SpaceObject> found = CMLib.map().getSpaceObjectsWithin(O, O.radius()+1, maxRange);
			if(found.size() > 1)
			{
				if(CMLib.dice().rollPercentage() > this.getFinalManufacturer().getReliabilityPct())
				{
					//TODO: better to filter out the most distant!
					int num = found.size() / 10; // failing reliability check always loses 10% of found things
					if(num <= 0)
						num = 1;
					for(int i=0;i<num && (found.size() > 0);i++)
					{
						found.remove(CMLib.dice().roll(1, found.size(), -1));
					}
				}
			}
			if(found.size() > 0)
			{
				final Filterer<SpaceObject> filter = this.getSensedObjectFilter();
				final Converter<SpaceObject, Environmental> converter = this.getSensedObjectConverter();
				for(final SpaceObject obj : found)
				{
					if(filter.passesFilter(obj))
					{
						Environmental sensedObject = converter.convert(obj);
						final String code=Technical.TechCommand.SENSE.makeCommand();
						final CMMsg msg=CMClass.getMsg(mob, controlI, sensedObject, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
						if(controlI.owner() instanceof Room)
						{
							if(((Room)controlI.owner()).okMessage(mob, msg))
								((Room)controlI.owner()).send(mob, msg);
						}
						else
						if(controlI.okMessage(mob, msg))
							controlI.executeMsg(mob, msg);
					}
				}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ACTIVATE:
			{
				final LanguageLibrary lang=CMLib.lang();
				final String[] parts=msg.targetMessage().split(" ");
				final TechCommand command=TechCommand.findCommand(parts);
				final Software controlI=(msg.tool() instanceof Software)?((Software)msg.tool()):null;
				final MOB mob=msg.source();
				if(command==null)
					reportError(this, controlI, mob, lang.L("@x1 does not respond.",me.name(mob)), lang.L("Failure: @x1: control failure.",me.name(mob)));
				else
				{
					final Object[] parms=command.confirmAndTranslate(parts);
					if(parms==null)
						reportError(this, controlI, mob, lang.L("@x1 did not respond.",me.name(mob)), lang.L("Failure: @x1: control syntax failure.",me.name(mob)));
					else
					if(command == TechCommand.SENSE)
					{
						if(doSensing(mob, controlI))
							this.activate(true);
						
					}
					else
						reportError(this, controlI, mob, lang.L("@x1 refused to respond.",me.name(mob)), lang.L("Failure: @x1: control command failure.",me.name(mob)));
				}
				break;
			}
			case CMMsg.TYP_DEACTIVATE:
				this.activate(false);
				//TODO:what does the ship need to know?
				break;
			}
		}
	}

	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdElecCompSensor))
			return false;
		return super.sameAs(E);
	}
}
