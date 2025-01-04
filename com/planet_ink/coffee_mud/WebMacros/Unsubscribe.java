package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.http.HTTPException;
import com.planet_ink.coffee_web.http.HTTPStatus;
import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.HTTPServerException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.PlayerCode;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.PlayerAccount.AccountFlag;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2025 Bo Zimmerman

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
public class Unsubscribe extends StdWebMacro
{
	@Override
	public String name()
	{
		return "Unsubscribe";
	}

	@Override
	public boolean isAWebPath()
	{
		return true;
	}

	protected void unsubScribePlayer(final String playerName)
	{
		Long attributes = (Long)CMLib.players().getPlayerValue(playerName, PlayerCode.MATTRIB);
		if(attributes != null)
		{
			attributes = Long.valueOf(attributes.longValue() |  Attrib.AUTOFORWARD.getBitCode());
			CMLib.players().setPlayerValue(playerName, PlayerCode.MATTRIB, attributes);
		}
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp) throws HTTPServerException
	{
		String last=httpReq.getUrlParameter("USER");
		if(last==null)
			throw new HTTPServerException(new HTTPException(HTTPStatus.S404_NOT_FOUND));
		final String key=httpReq.getUrlParameter("UNSUBKEY");
		if(key==null)
			throw new HTTPServerException(new HTTPException(HTTPStatus.S404_NOT_FOUND));
		if((last.length()>0)
		&&(key.length()>0))
		{
			if(CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)>1)
			{
				PlayerAccount account = CMLib.players().getLoadAccount(last);
				if((account == null)&&(CMLib.players().accountExistsAllHosts(last)))
					account = CMLib.players().getAccountAllHosts(last);
				if(account != null)
				{
					final String passwordOfAnyKind = account.getPasswordStr();
					final String b64repeatedHash = CMLib.encoder().makeRepeatableHashString(account.getAccountName()+"_"+passwordOfAnyKind);
					if(!b64repeatedHash.equals(key))
						throw new HTTPServerException(new HTTPException(HTTPStatus.S202_ACCEPTED, L("The link you followed is corrupt, or out of date. Please contact administrators.")));
					if(account.isSet(AccountFlag.NOAUTOFORWARD))
						throw new HTTPServerException(new HTTPException(HTTPStatus.S202_ACCEPTED,L("You are already unsubscribed from future emails.")));
					account.setFlag(AccountFlag.NOAUTOFORWARD, true);
					CMLib.database().DBUpdateAccount(account);
					for(final Enumeration<String> ms=account.getPlayers();ms.hasMoreElements();)
						unsubScribePlayer(ms.nextElement());
					throw new HTTPServerException(new HTTPException(HTTPStatus.S202_ACCEPTED,L("You are now unsubscribed from future emails.")));
				}
			}
			last=CMStrings.capitalizeAndLower(last);
			final Long attrib = (Long)CMLib.players().getPlayerValue(last, PlayerCode.MATTRIB);
			if(attrib == null)
				throw new HTTPServerException(new HTTPException(HTTPStatus.S202_ACCEPTED,L("You are already completely and totally unsubscribed from all emails forever.")));
			if(CMath.bset(attrib.intValue(), Attrib.AUTOFORWARD.getBitCode()))
				throw new HTTPServerException(new HTTPException(HTTPStatus.S202_ACCEPTED,L("You are already unsubscribed from future emails.")));
			final String passwordOfAnyKind = (String)CMLib.players().getPlayerValue(last, PlayerCode.PASSWORD);
			final String b64repeatedHash = CMLib.encoder().makeRepeatableHashString(last+"_"+passwordOfAnyKind);
			if(!b64repeatedHash.equals(key))
				throw new HTTPServerException(new HTTPException(HTTPStatus.S202_ACCEPTED,L("The link you followed is corrupt, or out of date. Please contact administrators.")));
			unsubScribePlayer(last);
			throw new HTTPServerException(new HTTPException(HTTPStatus.S202_ACCEPTED,L("You are now unsubscribed from future emails.")));
		}
		throw new HTTPServerException(new HTTPException(HTTPStatus.S404_NOT_FOUND));
	}
}
