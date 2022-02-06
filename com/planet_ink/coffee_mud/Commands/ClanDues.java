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
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2022-2022 Bo Zimmerman

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
public class ClanDues extends StdCommand
{
	public ClanDues()
	{
	}

	private final String[]	access	= I(new String[] { "CLANDUES" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
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

		Clan chkC=null;
		final boolean skipChecks=mob.getClanRole(mob.Name())!=null;
		if(skipChecks)
			chkC=mob.getClanRole(mob.Name()).first;

		if(chkC==null)
		{
			for(final Pair<Clan,Integer> c : mob.clans())
			{
				if((clanName.length()==0)||(CMLib.english().containsString(c.first.getName(), clanName))
				&&(c.first.getAuthority(c.second.intValue(), Clan.Function.TAX)!=Authority.CAN_NOT_DO))
				{
					chkC = c.first;
					break;
				}
			}
		}

		commands.set(0,getAccessWords()[0]);

		final Clan C=chkC;
		if(C==null)
		{
			mob.tell(L("You aren't allowed to require dues from anyone from @x1.",((clanName.length()==0)?"anything":clanName)));
			return false;
		}
		if((!skipChecks)&&(!CMLib.clans().goForward(mob,chkC,commands,Clan.Function.TAX,false)))
		{
			mob.tell(L("You aren't in the right position to set the amount of dues for your @x1.",C.getGovernmentName()));
			return false;
		}
		final Session S=mob.session();
		if((skipChecks)&&(commands.size()>1))
			setClanDues(mob,chkC,skipChecks,commands,CMath.div(CMath.s_int(CMParms.combine(commands,1)),100));
		else
		if(S!=null)
		{
			if((taxStr.length()==0)||(!CMath.isNumber(taxStr)))
			{
				final Pair<String,String> info = C.getPreferredBanking();
				List<String> badMembers = new ArrayList<String>();
				final String curr=(info==null)?CMLib.beanCounter().getCurrency(mob):info.second;
				for(final Clan.MemberRecord rec : C.getFullMemberList())
				{
					if(rec.dues > 0.0)
						badMembers.add(rec.name+"("+CMLib.beanCounter().nameCurrencyShort(curr, rec.dues));
				}
				final String currentDues;
				if(C.getDues()==0.0)
				{
					S.println(L("There are presently no dues."));
					currentDues = "0";
				}
				else
				{
					final double bestDenom=CMLib.beanCounter().getBestDenomination(curr, C.getDues());
					currentDues = (C.getDues()/bestDenom)+" "+CMLib.beanCounter().getDenominationName(curr, bestDenom);
					S.println(L("Dues are presently set at @x1 per year.",CMLib.beanCounter().nameCurrencyLong(curr, C.getDues())));
				}
				if(badMembers.size()==0)
					S.println(L("All members are current and paid-up on their dues."));
				else
					S.println(L("The following members are behind in their dues: @x1.",CMLib.english().toEnglishStringList(badMembers)));
				S.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
				{
					@Override
					public void showPrompt()
					{
						S.promptPrint(L("Enter your @x1's new amount of dues per year ("+currentDues+")\n\r: ", C.getGovernmentName()));
					}

					@Override
					public void timedOut()
					{
					}

					@Override
					public void callBack()
					{
						possiblySetClanDues(mob,C,skipChecks,this.input);
					}
				});
			}
			else
				possiblySetClanDues(mob,chkC,skipChecks,taxStr);
		}
		return false;
	}

	public void possiblySetClanDues(final MOB mob, final Clan C, final boolean skipChecks, String t)
	{
		t=t.trim();
		if(t.length()==0)
			return;
		double amt;
		if(CMath.isNumber(t))
			amt=CMath.s_double(t);
		else
		{
			final Pair<String,String> info = C.getPreferredBanking();
			String curr = CMLib.english().parseNumPossibleGoldCurrency(null, t);
			if(curr == null)
			{
				if(info != null)
					curr=info.second;
				else
					curr="";
			}
			String currName = (curr.length()==0)?"default (gold)":curr.toLowerCase();
			if((info!=null)&&(!curr.equalsIgnoreCase(info.second)))
			{
				if(mob.session()!=null)
					mob.session().println(L("'@x1' must be in the @x2 currency.",t,currName));
				return;
			}
			final Triad<String,Double,Long> triad =  CMLib.english().parseMoneyStringSDL(mob, t, curr);
			if(triad == null)
			{
				if(mob.session()!=null)
					mob.session().println(L("'@x1' is not a valid amount of @x2 currency.",t,currName));
				return;
			}
			amt = triad.second.doubleValue() * triad.third.longValue();
		}
		final Vector<String> commands=new Vector<String>();
		commands.add(getAccessWords()[0]);
		commands.add(t);
		setClanDues(mob, C, skipChecks,commands,amt);
	}

	public void setClanDues(final MOB mob, final Clan C, final boolean skipChecks, final List<String> commands, final double newRate)
	{
		if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.TAX,true))
		{
			final String duesDesc;
			final Pair<String,String> info = C.getPreferredBanking();
			if(info != null)
				duesDesc = CMLib.beanCounter().nameCurrencyLong(info.second, newRate);
			else
				duesDesc = CMLib.beanCounter().nameCurrencyLong(mob, newRate);
			C.setDues(newRate);
			C.update();
			CMLib.clans().clanAnnounce(mob,L("The dues/year amount for @x1 @x2 has been changed to @x3",C.getGovernmentName(),C.clanID(),duesDesc));
		}
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

}
