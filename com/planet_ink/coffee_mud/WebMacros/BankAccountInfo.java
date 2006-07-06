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
   Copyright 2000-2006 Bo Zimmerman

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
public class BankAccountInfo extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("BANKCHAIN");
		if(last==null) return " @break@";
		MOB M=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
		String player=httpReq.getRequestParameter("PLAYER");
		MOB playerM=null;
		if((player!=null)&&(player.length()>0))
		{
			if(((M==null)||(!M.Name().equalsIgnoreCase(player)))
			&&(!CMSecurity.isAllowedEverywhere(M,"CMDPLAYERS)")))
				return "";
			playerM=CMLib.map().getLoadPlayer(player);
			if((playerM==null)||(CMLib.map().getStartArea(playerM)==null)) 
				return "PLAYER not found!";
		}
		else
			return "PLAYER not set!";
		Banker B=CMLib.map().getBank(last,last);
		if(B==null) return "BANKER not found?!";
		double balance=B.getBalance(playerM);
		if(balance<=0.0) return "";
		if(parms.containsKey("BALANCE")) 
			return CMLib.beanCounter().nameCurrencyLong(playerM,balance);
		if(parms.containsKey("DEBTDUE"))
		{
			Vector debtV=B.getDebtInfo(playerM);
			if((debtV==null)||(debtV.size()==0)) return "N/A";
			
		}
		
		return "";
	}
}