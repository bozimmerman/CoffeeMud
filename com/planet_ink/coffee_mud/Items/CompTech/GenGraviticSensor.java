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
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2015-2020 Bo Zimmerman

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
public class GenGraviticSensor extends GenElecCompSensor
{
	@Override
	public String ID()
	{
		return "GenGraviticSensor";
	}

	public GenGraviticSensor()
	{
		super();
		setName("a gravitic sensor");
		setDisplayText("a gravitic sensor sits here.");
		setDescription("");
	}

	@Override
	protected long getSensorMaxRange()
	{
		return SpaceObject.Distance.Parsec.dm;
	}

	@Override
	protected Converter<SpaceObject, Environmental> getSensedObjectConverter()
	{
		return new Converter<SpaceObject, Environmental>()
		{
			@Override
			public Environmental convert(final SpaceObject obj)
			{
				return new SpaceObject()
				{
					final String name = L("Unknown");
					final String displayText = "";
					final String description = "";

					@Override
					public String ID()
					{
						return ""+obj;
					}

					@Override
					public String Name()
					{
						return name;
					}

					@Override
					public void setName(final String newName)
					{
					}

					@Override
					public String displayText()
					{
						return displayText;
					}

					@Override
					public void setDisplayText(final String newDisplayText)
					{
					}

					@Override
					public String description()
					{
						return description;
					}

					@Override
					public void setDescription(final String newDescription)
					{
					}

					@Override
					public String image()
					{
						return null;
					}

					@Override
					public String rawImage()
					{
						return null;
					}

					@Override
					public void setImage(final String newImage)
					{
					}

					@Override
					public boolean isGeneric()
					{
						return false;
					}

					@Override
					public void setMiscText(final String newMiscText)
					{
					}

					@Override
					public String text()
					{
						return "";
					}

					@Override
					public String miscTextFormat()
					{
						return null;
					}

					@Override
					public boolean sameAs(final Environmental E)
					{
						return E==this || E==obj;
					}

					@Override
					public long expirationDate()
					{
						return 0;
					}

					@Override
					public void setExpirationDate(final long dateTime)
					{
					}

					@Override
					public int maxRange()
					{
						return 0;
					}

					@Override
					public int minRange()
					{
						return 0;
					}

					@Override
					public String L(final String str, final String... xs)
					{
						return str;
					}

					@Override
					public String name()
					{
						return Name();
					}

					@Override
					public int getTickStatus()
					{
						return 0;
					}

					@Override
					public boolean tick(final Tickable ticking, final int tickID)
					{
						return false;
					}

					@Override
					public CMObject newInstance()
					{
						return obj.newInstance();
					}

					@Override
					public CMObject copyOf()
					{
						return obj.copyOf();
					}

					@Override
					public void initializeClass()
					{
					}

					@Override
					public int compareTo(final CMObject o)
					{
						return obj.compareTo(o);
					}

					@Override
					public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
					{
					}

					@Override
					public void affectCharStats(final MOB affectedMob, final CharStats affectableStats)
					{
					}

					@Override
					public void affectCharState(final MOB affectedMob, final CharState affectableMaxState)
					{
					}

					@Override
					public void executeMsg(final Environmental myHost, final CMMsg msg)
					{
					}

					@Override
					public boolean okMessage(final Environmental myHost, final CMMsg msg)
					{
						return true;
					}

					@Override
					public void destroy()
					{
						// Nope!
					}

					@Override
					public boolean isSavable()
					{
						return false;
					}

					@Override
					public boolean amDestroyed()
					{
						return obj.amDestroyed();
					}

					@Override
					public void setSavable(final boolean truefalse)
					{
					}

					@Override
					public String[] getStatCodes()
					{
						return new String[0];
					}

					@Override
					public int getSaveStatIndex()
					{
						return 0;
					}

					@Override
					public String getStat(final String code)
					{
						return "";
					}

					@Override
					public boolean isStat(final String code)
					{
						return false;
					}

					@Override
					public void setStat(final String code, final String val)
					{
					}

					@Override
					public BoundedCube getBounds()
					{
						return obj.getBounds();
					}

					@Override
					public long[] coordinates()
					{
						return Arrays.copyOf(obj.coordinates(), obj.coordinates().length);
					}

					@Override
					public void setCoords(final long[] coords)
					{
					}

					@Override
					public long radius()
					{
						return obj.radius();
					}

					@Override
					public void setRadius(final long radius)
					{
					}

					@Override
					public double[] direction()
					{
						return new double[]{0,0};
					}

					@Override
					public void setDirection(final double[] dir)
					{
					}

					@Override
					public double speed()
					{
						return 0;
					}

					@Override
					public void setSpeed(final double v)
					{
					}

					@Override
					public SpaceObject knownTarget()
					{
						return null;
					}

					@Override
					public void setKnownTarget(final SpaceObject O)
					{
					}

					@Override
					public SpaceObject knownSource()
					{
						return obj;
					}

					@Override
					public void setKnownSource(final SpaceObject O)
					{
					}

					@Override
					public long getMass()
					{
						return obj.getMass();
					}
				};
			}
		};
	}

	protected static SpaceObject star = null;

	protected long getMassDetected()
	{
		if(star == null)
		{
			star = (SpaceObject)CMClass.getBasicItem("Star");
		}
		return star.getMass();
	}

	@Override
	protected Filterer<SpaceObject> getSensedObjectFilter()
	{
		return new Filterer<SpaceObject>()
		{
			final SpaceObject spaceMe = CMLib.map().getSpaceObject(me, true);

			@Override
			public boolean passesFilter(final SpaceObject obj)
			{
				if((spaceMe == null)||(me == obj)||(spaceMe == obj))
					return false;
				final long distance = CMLib.map().getDistanceFrom(spaceMe.coordinates(), obj.coordinates());
				final long adjustedMax = Math.round(obj.getMass() * (1.0 - CMath.div(distance, getSensorMaxRange())));
				// tiny objects are not detected, nor ships at great distance, nor things inside us
				return (adjustedMax > 100) && (distance > obj.radius() + spaceMe.radius());
			}
		};
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenGraviticSensor))
			return false;
		return super.sameAs(E);
	}
}
