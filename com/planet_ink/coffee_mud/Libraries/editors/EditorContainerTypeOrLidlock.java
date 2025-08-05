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
public class EditorContainerTypeOrLidlock extends AbilityParmEditorImpl
{
	public EditorContainerTypeOrLidlock()
	{
		super("CONTAINER_TYPE_OR_LIDLOCK",CMLib.lang().L("Con."),ParmType.SPECIAL);
	}

	@Override
	public void createChoices()
	{
		super.choices = new PairVector<String,String>();
		for(final String s : Container.CONTAIN_DESCS)
			choices().add(s.toUpperCase().trim(),s);
		choices().add("LID","Lid");
		choices().add("LOCK","Lid+Lock");
	}

	@Override
	public int appliesToClass(final Object o)
	{
		return (o instanceof Container) ? 1 : -1;
	}

	@Override
	public String defaultValue()
	{
		return "";
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		final StringBuilder str=new StringBuilder("");
		if(I instanceof Container)
		{
			final Container C=(Container)I;
			if(C.hasALock())
				str.append("LOCK");
			if(str.length()>0)
				str.append("|");
			if(C.hasADoor())
				str.append("LID");
			if(str.length()>0)
				str.append("|");
			for(int i=1;i<Container.CONTAIN_DESCS.length;i++)
			{
				if(CMath.isSet(C.containTypes(), i-1))
				{
					if(str.length()>0)
						str.append("|");
					str.append(Container.CONTAIN_DESCS[i]);
				}
			}
		}
		return str.toString();
	}

	@Override
	public String[] fakeUserInput(final String oldVal)
	{
		if(oldVal.trim().length()==0)
			return new String[]{"NULL"};
		return CMParms.parseAny(oldVal,'|',true).toArray(new String[0]);
	}

	@Override
	public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		final String webValue = httpReq.getUrlParameter(fieldName);
		if(webValue == null)
			return oldVal;
		String id="";
		int index=0;
		final StringBuilder str=new StringBuilder("");
		for(;httpReq.isUrlParameter(fieldName+id);id=""+(++index))
		{
			final String newVal = httpReq.getUrlParameter(fieldName+id);
			if((newVal!=null)&&(newVal.length()>0)&&(choices().containsFirst(newVal)))
				str.append(newVal).append("|");
		}
		return str.toString();
	}

	@Override
	public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag)
	throws java.io.IOException
	{
		return CMLib.genEd().promptMultiSelectList(mob,oldVal,"|",++showNumber[0],showFlag,prompt(),choices(),false);
	}

	@Override
	public boolean confirmValue(final String oldVal)
	{
		final List<String> webVals=CMParms.parseAny(oldVal.toUpperCase().trim(), "|", true);
		for(final String s : webVals)
		{
			if(!choices().containsFirst(s))
				return false;
		}
		return true;
	}

	@Override
	public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		final String webValue = webValue(httpReq,parms,oldVal,fieldName);
		final List<String> webVals=CMParms.parseAny(webValue.toUpperCase().trim(), "|", true);
		String onChange = null;
		onChange = " MULTIPLE ";
		if(!parms.containsKey("NOSELECT"))
			onChange+= "ONCHANGE=\"MultiSelect(this);\"";
		final StringBuilder str=new StringBuilder("");
		str.append("\n\r<SELECT NAME="+fieldName+onChange+">");
		for(int i=0;i<choices().size();i++)
		{
			final String option = (choices().get(i).first);
			str.append("<OPTION VALUE=\""+option+"\" ");
			if(webVals.contains(option))
				str.append("SELECTED");
			str.append(">"+(choices().get(i).second));
		}
		return str.toString()+"</SELECT>";
	}
}
