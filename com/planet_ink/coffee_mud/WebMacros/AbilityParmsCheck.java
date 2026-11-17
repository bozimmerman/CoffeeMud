package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.http.MIMEType;
import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2026-2026 Bo Zimmerman

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
/**
 * A web-path macro for validating misc-text parameters for an ability.
 * Post (or get) two fields: ABILITY (the ability ID) and PARMS (the
 * parameter text to be validated).  The response body is either "Failed"
 * or an empty string.
 *
 * @author Bo Zimmerman
 */
public class AbilityParmsCheck extends StdWebMacro
{
	@Override
	public String name()
	{
		return "AbilityParmsCheck";
	}

	@Override
	public boolean isAWebPath()
	{
		return true;
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final boolean helpMode = httpReq.getUrlParameter("HELP") != null;
		httpResp.setMimeType((helpMode ? MIMEType.All.html : MIMEType.All.txt).getType());
		final String abilityID = httpReq.getUrlParameter("ABILITY");
		if((abilityID == null) || (abilityID.trim().length() == 0))
			return "Failed";
		String text = httpReq.getUrlParameter("PARMS");
		if(text == null)
			text = "";
		final String id = abilityID.trim();
		String name = null;
		String format = null;
		try
		{
			Ability A = CMClass.getAbility(id);
			if(A != null)
			{
				name = A.Name();
				format = A.miscTextFormat();
			}
			else
			{
				final Behavior B = CMClass.getBehavior(id);
				if(B == null)
					return "Failed";
				name = B.name();
				format = B.parmsFormat();
			}
			if(format == null)
				format = "";
			format = format.trim();
			if(format.equalsIgnoreCase(CMParms.FORMAT_UNDEFINED))
				format = "";
			if(helpMode)
				return this.helpEntry(id, name, format);
			// nothing was declared to check against, so anything passes
			if(format.length() == 0)
				return "";
			if(!CMLib.ableParms().isValidMiscText(format, text.trim()))
				return "Failed";
			return "";
		}
		catch(final Exception e)
		{
			Log.errOut("AbilityParmsCheck", "Error checking parms for '"+id+"': "+e.getMessage());
			return "Failed";
		}
	}
	protected String helpEntry(final String id, final String name, final String format)
	{
		final StringBuilder str = new StringBuilder("");
		str.append("<B>").append(name).append("</B> (").append(id).append(")<BR>");
		str.append("<B>Format:</B> ");
		if(format.length() == 0)
			str.append("none declared - there is nothing to check against");
		else
			str.append(CMStrings.replaceAll(CMStrings.replaceAll(format, "&", "&amp;"), "<", "&lt;"));
		str.append("<BR><BR>");
		final String help = CMLib.help().getHelpText(id, null, true);
		if((help == null) || (help.length() == 0))
			str.append("<I>No help text is available for ").append(id).append(".</I>");
		else
			str.append(this.helpHelp(help, 78));
		return this.clearWebMacros(str.toString());
	}
}
