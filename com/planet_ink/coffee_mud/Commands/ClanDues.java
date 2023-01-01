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
   Copyright 2022-2023 Bo Zimmerman

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
		if(commands.size()<=1)
		{
			final Session S=mob.session();
			if(S==null)
				return false;
			boolean found=false;
			for(final Pair<Clan,Integer> c : mob.clans())
			{
				if(c.first.getAuthority(c.second.intValue(), Clan.Function.TAX)!=Authority.CAN_NOT_DO)
				{
					final Clan C=c.first;
					found=true;
					if(C.getDues()==0.0)
						S.println(L("There are presently no dues for @x1.",C.name()));
					else
					{
						final Pair<String,String> info = C.getPreferredBanking();
						final List<String> badMembers = new ArrayList<String>();
						final String curr=(info==null)?CMLib.beanCounter().getCurrency(mob):info.second;
						for(final Clan.MemberRecord rec : C.getFullMemberList())
						{
							if(rec.dues > 0.0)
								badMembers.add("^H"+rec.name+"^N("+CMLib.beanCounter().nameCurrencyShort(curr, rec.dues)+")");
						}
						S.println(L("Dues are presently set at @x1 per year for @x2.",CMLib.beanCounter().nameCurrencyLong(curr, C.getDues()),C.name()));
						if(badMembers.size()==0)
							S.println(L("All members are current and paid-up on their dues."));
						else
							S.println(L("The following members are behind in their dues: @x1.",CMLib.english().toEnglishStringList(badMembers)));
					}
				}
			}
			if(!found)
				return CMLib.commands().postCommandFail(mob,commands,L("How much in dues?"));
			return true;
		}
		String taxStr=(commands.size()>1)?(String)commands.get(commands.size()-1):"";
		String clanName="";
		String forgiveWhom=null;
		if(commands.size()>2)
		{
			if(CMath.isNumber(commands.get(2)))
			{
				clanName=commands.get(1);
				taxStr=CMParms.combine(commands,2);
			}
			else
			if("FORGIVE".startsWith(commands.get(1).toUpperCase().trim()))
			{
				final String s=CMStrings.capitalizeAndLower(commands.get(2));
				if(CMLib.players().playerExists(s))
				{
					forgiveWhom=s;
					taxStr="";
					if(commands.size()>3)
						clanName=CMParms.combine(commands,3);
				}
			}
			else
			if(CMath.isNumber(commands.get(1)))
				taxStr=CMParms.combine(commands,1);
		}

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
			if(forgiveWhom != null)
				mob.tell(L("You aren't allowed to forgive dues from anyone from @x1.",((clanName.length()==0)?L("anything"):clanName)));
			else
				mob.tell(L("You aren't allowed to require dues from anyone from @x1.",((clanName.length()==0)?L("anything"):clanName)));
			return false;
		}
		final Pair<String,String> info = C.getPreferredBanking();
		if((!skipChecks) &&(info == null))
		{
			mob.tell(L("The @x1 @x2 requires a clan bank account to collect dues.",C.getGovernmentName(),C.name()));
			return false;
		}
		if((!skipChecks)
		&&(!CMLib.clans().goForward(mob,chkC,commands,Clan.Function.TAX,false)))
		{
			if(forgiveWhom != null)
				mob.tell(L("You aren't in the right position to forgive dues for your @x1.",C.getGovernmentName()));
			else
				mob.tell(L("You aren't in the right position to set the amount of dues for your @x1.",C.getGovernmentName()));
			return false;
		}

		if((!skipChecks)
		&&(forgiveWhom!=null))
		{
			final Clan.MemberRecord m = C.getMember(forgiveWhom);
			if(m==null)
			{
				mob.tell(L("'@x1' is not a member of @x2 @x3.",forgiveWhom,C.getGovernmentName(),C.name()));
				return false;
			}
			if(m.dues<=0.0)
			{
				mob.tell(L("'@x1' does not owe any dues."));
				return false;
			}
		}

		final Session S=mob.session();
		if(forgiveWhom != null)
		{
			final String curr=(info==null)?CMLib.beanCounter().getCurrency(mob):info.second;
			final Clan.MemberRecord m = C.getMember(forgiveWhom);
			CMLib.database().DBUpdateClanDonates(C.clanID(), forgiveWhom, 0, 0, -m.dues);
			mob.tell(L("@x1 has been forgiven @x2 in dues.",forgiveWhom,CMLib.beanCounter().nameCurrencyLong(curr, m.dues)));
		}
		else
		if((skipChecks)&&(taxStr.length()>0))
			possiblySetClanDues(mob,chkC,skipChecks,taxStr);
		else
		if(S!=null)
		{
			if(taxStr.length()==0)
			{
				final List<String> badMembers = new ArrayList<String>();
				final String curr=(info==null)?CMLib.beanCounter().getCurrency(mob):info.second;
				for(final Clan.MemberRecord rec : C.getFullMemberList())
				{
					if(rec.dues > 0.0)
						badMembers.add("^H"+rec.name+"^N("+CMLib.beanCounter().nameCurrencyShort(curr, rec.dues)+")");
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
			final String currName = (curr.length()==0)?"default (gold)":curr.toLowerCase();
			if((info!=null)
			&&(!curr.equalsIgnoreCase(info.second))
			&&(!skipChecks))
			{
				if(mob.session()!=null)
					mob.session().println(L("'@x1' must be in the @x2 currency.",t,currName));
				return;
			}
			final Triad<String,Double,Long> triad =  CMLib.english().parseMoneyStringSDL(mob, t, curr);
			if(triad == null)
			{
				if((mob.session()!=null)&&(!skipChecks))
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
