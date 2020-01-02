package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Function;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.PostOffice.MailPiece;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2019-2020 Bo Zimmerman

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
public class PostalBoxInfo extends StdWebMacro
{
	@Override
	public String name()
	{
		return "PostalBoxInfo";
	}

	@SuppressWarnings("unchecked")
	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String chain=httpReq.getUrlParameter("POSTCHAIN");
		if(chain==null)
			return " @break@";
		final MOB M = Authenticate.getAuthenticatedMob(httpReq);
		if(M==null)
			return " @break@";

		final String box=httpReq.getUrlParameter("POSTBOX");
		if((box==null)||(box.length()==0))
			return " @break@";

		String last=httpReq.getUrlParameter("POSTPIECE");
		if(parms.containsKey("RESET"))
		{
			if(last!=null)
				httpReq.removeUrlParameter("POSTPIECE");
			return "";
		}

		final List<MailPiece> mailPieces;
		if(httpReq.getRequestObjects().containsKey("MAIL_PIECES_"+chain+"_"+box))
			mailPieces = (List<MailPiece>)httpReq.getRequestObjects().get("MAIL_PIECES_"+chain+"_"+box);
		else
		{
			final PostOffice P=CMLib.map().getPostOffice(chain, "*");
			if(P!=null)
			{
				final List<DatabaseEngine.PlayerData> data = CMLib.database().DBReadPlayerData(box, chain);
				mailPieces=new ArrayList<MailPiece>(data.size());
				for(final DatabaseEngine.PlayerData PD : data)
				{
					if((PD!=null)
					&&(PD.key().indexOf(';')>0))
					{
						final MailPiece MP=P.parsePostalItemData(PD.xml());
						if(MP != null)
							mailPieces.add(MP);
					}
				}
			}
			else
				mailPieces=new ArrayList<MailPiece>(1);
			httpReq.getRequestObjects().put("MAIL_PIECES_"+chain+"_"+box,mailPieces);
		}

		if(parms.containsKey("NEXT"))
		{
			String lastID="";
			for(final MailPiece MP : mailPieces)
			{
				final String mpStr = MP.toString();
				if((last==null)
				||((last.length()>0)
					&&(last.equals(lastID))
					&&(!mpStr.equals(lastID))))
				{
					httpReq.addFakeUrlParameter("POSTPIECE",mpStr);
					last=mpStr;
					return "";
				}
				lastID=mpStr;
			}
			httpReq.addFakeUrlParameter("POSTPIECE","");
			if(parms.containsKey("EMPTYOK"))
				return "<!--EMPTY-->";
			return " @break@";
		}

		if(parms.containsKey("TOTMAIL"))
		{
			return ""+ mailPieces.size();
		}

		if(last != null)
		{
			for(final MailPiece MP : mailPieces)
			{
				final String mpStr = MP.toString();
				if(mpStr.equals(last))
				{
					if(parms.containsKey("FROM"))
						return MP.from;
					if(parms.containsKey("TO"))
						return MP.to;
					if(parms.containsKey("DATE")||parms.containsKey("TIME"))
						return CMLib.time().date2String(CMath.s_long(MP.time));
					if(parms.containsKey("CLASS"))
						return MP.classID;
					if(parms.containsKey("NAME"))
						return CMLib.xml().getValFromPieces(CMLib.xml().parseAllXML(MP.xml), "NAME");
					break;
				}
			}
		}

		return "";
	}
}
