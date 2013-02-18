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
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Authority;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class ClanTax extends StdCommand
{
	public ClanTax(){}

	private final String[] access={"CLANTAX"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String taxStr=(commands.size()>1)?(String)commands.get(commands.size()-1):"";
		String clanName="";
		if(!CMath.isInteger(taxStr))
		{
			clanName=(commands.size()>2)?CMParms.combine(commands,1,commands.size()):"";
			taxStr="";
		}
		else
			clanName=(commands.size()>2)?CMParms.combine(commands,1,commands.size()-1):"";
		
		Clan C=null;
		boolean skipChecks=mob.getClanRole(mob.Name())!=null;
		if(skipChecks) C=mob.getClanRole(mob.Name()).first;
			
		if(C==null)
		for(Pair<Clan,Integer> c : mob.clans())
			if((clanName.length()==0)||(CMLib.english().containsString(c.first.getName(), clanName))
			&&(c.first.getAuthority(c.second.intValue(), Clan.Function.TAX)!=Authority.CAN_NOT_DO))
			{	C=c.first; break; }

		commands.setElementAt(getAccessWords()[0],0);

		StringBuffer msg=new StringBuffer("");
		if(C==null)
		{
			mob.tell("You aren't allowed to tax anyone from "+((clanName.length()==0)?"anything":clanName)+".");
			return false;
		}
		if((!skipChecks)&&(!CMLib.clans().goForward(mob,C,commands,Clan.Function.TAX,false)))
		{
			msg.append("You aren't in the right position to set the experience tax rate for your "+C.getGovernmentName()+".");
		}
		else
		{
			try
			{
				double newRate=0.0;
				if((skipChecks)&&(commands.size()>1))
					newRate=CMath.div(CMath.s_int(CMParms.combine(commands,1)),100);
				else
				if(mob.session()!=null)
				{
					String t=null;
					if((taxStr.length()==0)||(!CMath.isNumber(taxStr)))
						t=mob.session().prompt("Enter your "+C.getGovernmentName()+"'s new tax rate (0-25)\n\r: ","");
					else
						t=taxStr;
					if(t.length()==0) return false;
					int intt=CMath.s_int(t);
					if((intt<0)||(intt>25)) 
					{
						mob.session().println("'"+t+"' is not a valid value.  Try 0-25.");
						return false;
					}
					commands.clear();
					commands.addElement(getAccessWords()[0]);
					commands.addElement(t);
					newRate=CMath.div(CMath.s_int(t),100);
				}
				if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.TAX,true))
				{
					C.setTaxes(newRate);
					C.update();
					CMLib.clans().clanAnnounce(mob,"The experience tax rate of "+C.getGovernmentName()+" "+C.clanID()+" has been changed to "+((int)Math.round(C.getTaxes()*100.0)+"%."));
					return false;
				}
			}
			catch(java.io.IOException e)
			{
			}
		}
		mob.tell(msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return false;}

	
}
