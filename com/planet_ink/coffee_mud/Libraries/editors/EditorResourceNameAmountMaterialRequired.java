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
public class EditorResourceNameAmountMaterialRequired extends AbilityParmEditorImpl
{
	public EditorResourceNameAmountMaterialRequired()
	{
		super("RESOURCE_NAME_AMOUNT_MATERIAL_REQUIRED",CMLib.lang().L("Resrc/Amt"),ParmType.SPECIAL);
	}

	@Override
	public void createChoices()
	{
		createChoices(RawMaterial.CODES.NAMES());
		choices().add("","");
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		int amt=(int)Math.round(CMath.mul(I.basePhyStats().weight()-1,(A!=null)?A.getItemWeightMultiplier(false):1.0));
		if(amt<1)
			amt=1;
		return RawMaterial.CODES.NAME(I.material())+"/"+amt;
	}

	@Override
	public String defaultValue()
	{
		return "";
	}

	@Override
	public int appliesToClass(final Object o)
	{
		return 0;
	}

	@Override
	public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		if(httpReq.isUrlParameter(fieldName+"_RESOURCE"))
		{
			final String rsc=httpReq.getUrlParameter(fieldName+"_RESOURCE");
			final String amt=httpReq.getUrlParameter(fieldName+"_AMOUNT");
			if((rsc.trim().length()==0)||(rsc.equalsIgnoreCase("NOTHING"))||(CMath.s_int(amt)<=0))
				return "";
			return rsc+"/"+amt;
		}
		return oldVal;
	}

	@Override
	public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		final String value=webValue(httpReq,parms,oldVal,fieldName);
		String rsc = "";
		int amt = 0;
		final int x=value.indexOf('/');
		if(x>0)
		{
			rsc = value.substring(0,x);
			amt = CMath.s_int(value.substring(x+1));
		}
		final StringBuffer str=new StringBuffer("");
		str.append("\n\r<SELECT NAME="+fieldName+"_RESOURCE MULTIPLE>");
		final String[] Ss=RawMaterial.CODES.NAMES().clone();
		Arrays.sort(Ss);
		for(final String S : Ss)
		{
			str.append("<OPTION VALUE=\""+S+"\" "
					+((S.equalsIgnoreCase(rsc))?"SELECTED":"")+">"
					+CMStrings.capitalizeAndLower(S));
		}
		str.append("</SELECT>");
		str.append("&nbsp;&nbsp;Amount: ");
		str.append("<INPUT TYPE=TEXT NAME="+fieldName+"_AMOUNT VALUE="+amt+">");
		return str.toString();
	}

	@Override
	public boolean confirmValue(String oldVal)
	{
		if(oldVal.trim().length()==0)
			return true;
		oldVal=oldVal.trim();
		final int x=oldVal.indexOf('/');
		if(x<0)
			return false;
		if(!CMStrings.contains(choices().toArrayFirst(new String[0]),oldVal.substring(0,x)))
			return false;
		if(!CMath.isInteger(oldVal.substring(x+1)))
			return false;
		return true;
	}

	@Override
	public String[] fakeUserInput(final String oldVal)
	{
		final int x=oldVal.indexOf('/');
		if(x<=0) return new String[]{""};
		return new String[]{oldVal.substring(0,x),oldVal.substring(x+1)};
	}

	@Override
	public String commandLinePrompt(final MOB mob, String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
	{
		oldVal=oldVal.trim();
		final int x=oldVal.indexOf('/');
		String oldRsc = "";
		int oldAmt = 0;
		if(x>0)
		{
			oldRsc = oldVal.substring(0,x);
			oldAmt = CMath.s_int(oldVal.substring(x));
		}
		oldRsc = CMLib.genEd().promptChoice(mob,oldRsc,++showNumber[0],showFlag,prompt(),choices());
		if(oldRsc.length()>0)
			return oldRsc+"/"+CMLib.genEd().prompt(mob,oldAmt,++showNumber[0],showFlag,prompt());
		return "";
	}
}
