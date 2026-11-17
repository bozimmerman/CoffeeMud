package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.http.MIMEType;
import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
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
 * or an empty string, so a client can do:
 *
 * <pre>
 *   (await (await fetch("AbilityParmsCheck", {method:"POST",
 *       body:new URLSearchParams({ABILITY:"Prop_LimitedEquip",PARMS:"2"})})).text()) == ""
 * </pre>
 *
 * No authentication is required, and no game state is changed.
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
		httpResp.setMimeType(MIMEType.All.txt.getType());
		final String abilityID = httpReq.getUrlParameter("ABILITY");
		if((abilityID == null) || (abilityID.trim().length() == 0))
			return "Failed";
		final Ability A = CMClass.getAbility(abilityID.trim());
		if(A == null)
			return "Failed";
		String format = null;
		try
		{
			format = A.miscTextFormat();
			if(format == null)
				format = "";
			format = format.trim();
			// nothing was declared to check against, so anything passes
			if((format.length() == 0) || (format.equalsIgnoreCase(CMParms.FORMAT_UNDEFINED)))
				return "";
			String text = httpReq.getUrlParameter("PARMS");
			if(text == null)
				text = "";
			if(!CMLib.ableParms().isValidMiscText(format, text.trim()))
				return "Failed";
			return "";
		}
		catch(final Exception e)
		{
			Log.errOut("AbilityParmsCheck", "Error checking parms for '"+abilityID+"': "+e.getMessage());
			return "Failed";
		}
	}
}
