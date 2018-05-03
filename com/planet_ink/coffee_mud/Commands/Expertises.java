package com.planet_ink.coffee_mud.Commands;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2006-2018 Bo Zimmerman

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

public class Expertises extends StdCommand
{
	public Expertises(){}

	private final String[] access=I(new String[]{"EXPERTISES","EXPS"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	protected void appendExpertise(final MOB mob, final StringBuffer msg)
	{
		int col=0;
		final int COL_LEN=CMLib.lister().fixColWidth(25.0,mob);
		final XVector<String> expers=new XVector<String>();
		for(final Enumeration<String> e=mob.expertises();e.hasMoreElements();)
		{
			final String exper=e.nextElement();
			final ExpertiseLibrary.ExpertiseDefinition def=CMLib.expertises().getDefinition(exper);
			if(def==null)
			{
				final Pair<String,Integer> p=mob.fetchExpertise(exper);
				if(p==null)
					expers.add("?"+CMStrings.capitalizeAllFirstLettersAndLower(exper));
				else
				if(p.first.endsWith("%"))
					expers.add("?"+CMStrings.capitalizeAllFirstLettersAndLower(p.first.substring(0,p.first.length()-1))+" ("+p.second.intValue()+"%)");
				else
					expers.add("?"+CMStrings.capitalizeAllFirstLettersAndLower(p.first)+" "+p.second.intValue());
			}
			else
				expers.add(def.name());
		}
		expers.sort();
		for(final String expName : expers)
		{
			if(expName.startsWith("?"))
			{
				msg.append(CMStrings.padRight(expName.substring(1),COL_LEN));
			}
			else
			if(expName.length()>=COL_LEN)
			{
				if(col>=2)
				{
					msg.append("\n\r");
					col=0;
				}
				msg.append(CMStrings.padRightPreserve("^<HELP^>"+expName+"^</HELP^>",COL_LEN));
				final int spaces=(COL_LEN*2)-expName.length();
				for(int i=0;i<spaces;i++) msg.append(" ");
				col++;
			}
			else
				msg.append(CMStrings.padRight("^<HELP^>"+expName+"^</HELP^>",COL_LEN));
			if((++col)>=3)
			{
				msg.append("\n\r");
				col=0;
			}
		}
	}
	
	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		final StringBuffer msg=new StringBuffer("");
		msg.append(L("\n\r^HYour expertises:^? \n\r"));
		appendExpertise(mob,msg);
		if(!msg.toString().endsWith("\n\r"))
			msg.append("\n\r");
		if(!mob.isMonster())
			mob.session().wraplessPrintln(msg.toString());
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}


	@Override
	public Object executeInternal(MOB mob, int metaFlags, Object... args) throws java.io.IOException
	{
		//if(!super.checkArguments(internalParameters, args)) return Boolean.FALSE.toString();

		MOB target=mob;
		for(final Object o : args)
		{
			if(o instanceof MOB)
				target=(MOB)o;
		}
		StringBuffer msg=new StringBuffer("");
		this.appendExpertise(target, msg);
		return msg;
	}
}
