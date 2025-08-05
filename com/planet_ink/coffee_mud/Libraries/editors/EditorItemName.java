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
public class EditorItemName extends AbilityParmEditorImpl
{
	public EditorItemName()
	{
		super("ITEM_NAME",CMLib.lang().L("Item Final Name"),ParmType.STRING);
	}

	@Override
	public void createChoices()
	{
	}

	@Override
	public String defaultValue()
	{
		return "Item Name";
	}

	@Override
	public int minColWidth()
	{
		return 10;
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		final String oldName=CMStrings.removeColors(I.Name());
		if(I.material()==RawMaterial.RESOURCE_GLASS)
			return CMLib.english().removeArticleLead(oldName);

		String newName=oldName;
		final List<String> V=CMParms.parseSpaces(oldName,true);
		for(int i=0;i<V.size();i++)
		{
			final String s=V.get(i);
			final int code=RawMaterial.CODES.FIND_IgnoreCase(s);
			if((code>0)&&(code==I.material()))
			{
				V.set(i, "%");
				if((i>0)&&(CMLib.english().isAnArticle(V.get(i-1))))
					V.remove(i-1);
				newName=CMParms.combine(V);
				break;
			}
		}
		if(oldName.equals(newName))
		{
			for(int i=0;i<V.size();i++)
			{
				final String s=V.get(i);
				final int code=RawMaterial.CODES.FIND_IgnoreCase(s);
				if(code>0)
				{
					V.set(i, "%");
					if((i>0)&&(CMLib.english().isAnArticle(V.get(i-1))))
						V.remove(i-1);
					newName=CMParms.combine(V);
					break;
				}
			}
		}
		if(newName.indexOf( '%' )<0)
		{
			for(int i=0;i<V.size()-1;i++)
			{
				if(CMLib.english().isAnArticle( V.get( i ) ))
				{
					if(i==0)
						V.set( i, "%" );
					else
						V.add(i+1, "%");
					break;
				}
			}
			newName=CMParms.combine( V );
		}
		if(newName.indexOf( '%' )<0)
		{
			newName="% "+newName;
		}
		return newName;
	}
}
