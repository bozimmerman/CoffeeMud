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
   Copyright 2002-2020 Bo Zimmerman

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

	protected void unsubScribePlayer(final MOB M, final boolean unload)
	{
		M.setAttribute(Attrib.AUTOFORWARD, true);
		CMLib.database().DBUpdatePlayerMOBOnly(M);
		if(unload)
			CMLib.players().unloadOfflinePlayer(M);
	}

	protected void unsubScribePlayer(final String playerName)
	{
		if(playerName != null)
		{
			{
				final MOB M=CMLib.players().getPlayer(playerName);
				if(M != null)
				{
					unsubScribePlayer(M, false);
					return;
				}
			}
			{
				final MOB M=CMLib.players().getLoadPlayer(playerName);
				if(M != null) // load, set flag, unload, thank you.
					unsubScribePlayer(M, true);
			}
		}
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp) throws HTTPServerException
	{
		final String last=httpReq.getUrlParameter("USER");
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
			final boolean unloadPlayer = CMLib.players().getPlayer(last) == null;
			MOB M=CMLib.players().getLoadPlayer(last);
			if((M == null)&&(CMLib.players().playerExistsAllHosts(last)))
				M=CMLib.players().getPlayerAllHosts(last);
			if((M == null) || (M.playerStats()==null))
				throw new HTTPServerException(new HTTPException(HTTPStatus.S202_ACCEPTED,L("You are already completely and totally unsubscribed from all emails forever.")));
			if(M.isAttributeSet(Attrib.AUTOFORWARD))
				throw new HTTPServerException(new HTTPException(HTTPStatus.S202_ACCEPTED,L("You are already unsubscribed from future emails.")));
			final String passwordOfAnyKind = M.playerStats().getPasswordStr();
			final String b64repeatedHash = CMLib.encoder().makeRepeatableHashString(M.Name()+"_"+passwordOfAnyKind);
			if(!b64repeatedHash.equals(key))
				throw new HTTPServerException(new HTTPException(HTTPStatus.S202_ACCEPTED,L("The link you followed is corrupt, or out of date. Please contact administrators.")));
			unsubScribePlayer(M, unloadPlayer);
			throw new HTTPServerException(new HTTPException(HTTPStatus.S202_ACCEPTED,L("You are now unsubscribed from future emails.")));
		}
		throw new HTTPServerException(new HTTPException(HTTPStatus.S404_NOT_FOUND));
	}
}
