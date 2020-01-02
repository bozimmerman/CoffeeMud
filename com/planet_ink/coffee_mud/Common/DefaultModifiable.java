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
public class DefaultModifiable implements CMCommon, Modifiable
{
	@Override
	public String ID()
	{
		if(fields.containsKey("ID"))
			return fields.get("ID");
		else
			return "DefaultModifiable";
	}

	public final Map<String,String> fields = new Hashtable<String,String>();

	@Override
	public String name()
	{
		return getStat(GenericBuilder.GenMOBCode.NAME.name());
	}

	@Override
	public String toString()
	{
		return CMParms.toKeyValueSlashListString(fields);
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			return (CMObject) this.clone();
		}
		catch (final Exception e)
		{
			return this;
		}
	}

	@Override
	public int compareTo(final CMObject o)
	{
		if (o == null)
			return 1;
		return (this == o) ? 0 : this.ID().compareTo(o.ID());
	}

	@Override
	public CMObject newInstance()
	{
		return new DefaultModifiable();
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public String[] getStatCodes()
	{
		return new XVector<String>(fields.keySet()).toArray(new String[0]);
	}

	@Override
	public int getSaveStatIndex()
	{
		return 0;
	}

	@Override
	public String getStat(String code)
	{
		if(code == null)
			return "";
		code=code.toUpperCase().trim();
		if(fields.containsKey(code))
			return fields.get(code);
		return "";
	}

	@Override
	public boolean isStat(String code)
	{
		if(code == null)
			return false;
		code=code.toUpperCase().trim();

		return fields.containsKey(code);
	}

	@Override
	public void setStat(String code, final String val)
	{
		if((code == null)||(val==null))
			return;
		code=code.toUpperCase().trim();
		fields.put(code, val);
	}

}
