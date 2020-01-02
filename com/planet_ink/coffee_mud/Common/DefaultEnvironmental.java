package com.planet_ink.coffee_mud.Common;

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
import com.planet_ink.coffee_mud.Libraries.interfaces.CatalogLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2018-2020 Bo Zimmerman

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
public class DefaultEnvironmental extends DefaultModifiable implements Environmental
{
	@Override
	public String ID()
	{
		if(fields.containsKey("ID"))
			return fields.get("ID");
		else
			return "DefaultEnvironmental";
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
	public CMObject newInstance()
	{
		return new DefaultEnvironmental();
	}

	@Override
	public void destroy()
	{
		fields.clear();
	}

	@Override
	public boolean isSavable()
	{
		return CMath.s_bool(getStat("IS_SAVABLE"));
	}

	@Override
	public boolean amDestroyed()
	{
		return fields.size()==0;
	}

	@Override
	public void setSavable(final boolean truefalse)
	{
		setStat("IS_SAVABLE",""+truefalse);
	}

	@Override
	public String Name()
	{
		return name();
	}

	@Override
	public void setName(final String newName)
	{
		setStat(GenericBuilder.GenMOBCode.NAME.name(),newName);
	}

	@Override
	public String displayText()
	{
		return getStat(GenericBuilder.GenMOBCode.DISPLAY.name());
	}

	@Override
	public void setDisplayText(final String newDisplayText)
	{
		setStat(GenericBuilder.GenMOBCode.DISPLAY.name(),newDisplayText);
	}

	@Override
	public String description()
	{
		return getStat(GenericBuilder.GenMOBCode.DESCRIPTION.name());
	}

	@Override
	public void setDescription(final String newDescription)
	{
		setStat(GenericBuilder.GenMOBCode.DESCRIPTION.name(), newDescription);
	}

	@Override
	public String image()
	{
		return getStat(GenericBuilder.GenMOBCode.IMG.name());
	}

	@Override
	public String rawImage()
	{
		return getStat(GenericBuilder.GenMOBCode.IMG.name());
	}

	@Override
	public void setImage(final String newImage)
	{
		setStat(GenericBuilder.GenMOBCode.IMG.name(), newImage);
	}

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		if((newMiscText!=null)
		&&(newMiscText.startsWith("<")))
		{
			fields.clear();
			fields.putAll(CMLib.xml().fromXML(newMiscText));
		}
	}

	@Override
	public String text()
	{
		return CMLib.xml().toXML(fields);
	}

	@Override
	public String miscTextFormat()
	{
		return null;
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof DefaultEnvironmental))
			return false;
		final DefaultEnvironmental otherE=(DefaultEnvironmental)E;
		if(otherE.fields==null)
			return fields==null;
		if(fields==null)
			return false;
		if(otherE.fields.size()!=fields.size())
			return false;
		for(final String key : fields.keySet())
		{
			if(!otherE.fields.containsKey(key))
				return false;
			if(!fields.get(key).equals(otherE.fields.get(key)))
				return false;
		}
		return true;
	}

	@Override
	public long expirationDate()
	{
		return CMath.s_long(getStat("EXPIRATION_DATE"));
	}

	@Override
	public void setExpirationDate(final long dateTime)
	{
		setStat("EXPIRATION_DATE",""+dateTime);
	}

	@Override
	public int maxRange()
	{
		return CMath.s_int(getStat("MAX_RANGE"));
	}

	@Override
	public int minRange()
	{
		return CMath.s_int(getStat("MIN_RANGE"));
	}

	@Override
	public String L(final String str, final String... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

}
