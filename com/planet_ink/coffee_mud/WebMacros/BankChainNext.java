package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;



/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class BankChainNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		MOB playerM=null;
		boolean destroyPlayer=false;
		try{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("BANKCHAIN");
		String player=httpReq.getRequestParameter("PLAYER");
		if((player==null)||(player.length()==0)) player=httpReq.getRequestParameter("CLAN");
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.removeRequestParameter("BANKCHAIN");
			return "";
		}
		String lastID="";
		MOB M = Authenticate.getAuthenticatedMob(httpReq);
		if(M==null) return " @break@";
		if((player!=null)&&(player.length()>0))
		{
			if(((!M.Name().equalsIgnoreCase(player)))
			&&(!CMSecurity.isAllowedEverywhere(M,"CMDPLAYERS)")))
				return "";
			Clan C=CMLib.clans().getClan(player);
			if(C!=null)
			{
				playerM=CMClass.getMOB("StdMOB");
				playerM.setName(C.clanID());
				playerM.setLocation(M.location());
				playerM.setStartRoom(M.getStartRoom());
				playerM.setClanID(C.clanID());
				playerM.setClanRole(Clan.POS_BOSS);
				destroyPlayer=true;
			}
			else
			{
				playerM=CMLib.players().getPlayer(player);
				if(playerM==null)
				{
					playerM=CMClass.getMOB("StdMOB");
					playerM.setName(CMStrings.capitalizeAndLower(player));
					playerM.setLocation(M.location());
					playerM.setStartRoom(M.getStartRoom());
					destroyPlayer=true;
				}
			}
			if(playerM==null) return "";
		}
		else
		if(!CMSecurity.isAllowedEverywhere(M,"CMDPLAYERS)"))
			return "";
		
		for(Iterator j=CMLib.map().bankChains(null);j.hasNext();)
		{
			String bankChain=(String)j.next();
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!bankChain.equals(lastID))))
			{
				httpReq.addRequestParameters("BANKCHAIN",bankChain);
				last=bankChain;
				if(playerM!=null)
				{
					Banker bankerM=CMLib.map().getBank(bankChain,bankChain);
					if((bankerM==null)
					||((!playerM.getClanID().equals(playerM.Name()))&&(bankerM.isSold(Banker.DEAL_CLANBANKER)))
					||(BankAccountInfo.getMakeAccountInfo(httpReq,bankerM,playerM).balance<=0.0))
					{
						lastID=bankChain;
						continue;
					}
				}
				return "";
			}
			lastID=bankChain;
		}
		httpReq.addRequestParameters("BANKCHAIN","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
		}
		finally
		{
			if((destroyPlayer)&&(playerM!=null))
			{
				playerM.setLocation(null); 
				playerM.destroy();
			}
		}
	}

}