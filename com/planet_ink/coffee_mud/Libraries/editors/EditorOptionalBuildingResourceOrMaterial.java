package com.planet_ink.coffee_mud.Libraries.editors;
import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.RawMaterial.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
   Copyright 2008-2025 Bo Zimmerman

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
public class EditorOptionalBuildingResourceOrMaterial extends AbilityParmEditorImpl
{
	public EditorOptionalBuildingResourceOrMaterial()
	{
		super("OPTIONAL_BUILDING_RESOURCE_OR_MATERIAL",CMLib.lang().L("Rsc/ Mat"),ParmType.CHOICES);
	}

	@Override
	public void createChoices()
	{
		final XVector<String> V=new XVector<String>(RawMaterial.CODES.NAMES());
		Collections.sort(V);
		final XVector<String> V2=new XVector<String>(RawMaterial.Material.names());
		Collections.sort(V2);
		V.addAll(V2);
		V.addElement("VALUE");
		V.addElement("MONEY");
		V.addElement("");
		createChoices(V);
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		final List<String> words=CMParms.parse(I.name());
		for(int i=words.size()-1;i>=0;i--)
		{
			final String s=words.get(i);
			final int y=s.indexOf('-');
			if(y>=0)
			{
				words.add(s.substring(0, y));
				words.add(s.substring(0, y+1));
			}
		}
		for(final String word : words)
		{
			if(word.length()>0)
			{
				final int rsc=RawMaterial.CODES.FIND_IgnoreCase(word);
				if((rsc > 0)&&(rsc != I.material()))
					return RawMaterial.CODES.NAME(rsc);
			}
		}
		return "";
	}

	@Override
	public String defaultValue()
	{
		return "";
	}
}
