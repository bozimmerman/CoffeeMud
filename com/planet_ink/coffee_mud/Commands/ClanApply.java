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
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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

public class ClanApply extends StdCommand
{
	public ClanApply()
	{
	}

	private final String[]	access	= I(new String[] { "CLANAPPLY" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		commands.set(0,getAccessWords()[0]);
		final String clanName=CMParms.combine(commands,1);
		if(mob.isMonster())
			return false;
		final StringBuffer msg=new StringBuffer("");
		if(clanName.length()>0)
		{
			final Clan C=CMLib.clans().findClan(clanName);
			if(C!=null)
			{
				if(C.isOnlyFamilyApplicants()
				&&(!CMLib.clans().isFamilyOfMembership(mob,C.getMemberList())))
				{
					mob.tell(L("The clan  @x1 is a family.  You can not join a family, you must be born or married into it.",C.clanID()));
					return false;
				}

				final List<Pair<Clan,Integer>> oldList=CMLib.clans().getClansByCategory(mob, C.getCategory());
				if(oldList.size()>=CMProps.getMaxClansThisCategory(C.getCategory()))
				{
					if(oldList.size()>0)
					{
						final Pair<Clan,Integer> p=oldList.get(0);
						mob.tell(L("You are already a member of @x1. You need to resign before you can apply to another.",p.first.getName()));
					}
					else
						mob.tell(L("You are not elligible to apply to this clan."));
					return false;
				}

				if(!CMLib.masking().maskCheck(C.getBasicRequirementMask(), mob, true))
				{
					mob.tell(L("You are not of the right qualities to join @x1. Use CLANDETAILS \"@x2\" for more information.",C.clanID(),C.clanID()));
					return false;
				}

				final CharClass CC = CMClass.getCharClass(C.getClanClass());
				if((CC!=null) && (mob.charStats().getClassLevel(CC)<0) && (!CC.qualifiesForThisClass(mob, false)))
					return false;

				if(CMLib.masking().maskCheck(C.getAcceptanceSettings(),mob,true))
				{
					final List<Clan.MemberRecord> members=C.getMemberList();
					if((CMLib.masking().maskCheck("-<"+CMProps.getIntVar(CMProps.Int.MINCLANLEVEL),mob,true))
					||(CMLib.clans().isFamilyOfMembership(mob,members)))
					{
						final int maxMembers=CMProps.getIntVar(CMProps.Int.MAXCLANMEMBERS);
						final int numMembers=members.size();
						if((maxMembers<=0)||(numMembers<maxMembers))
						{
							final int role=C.getAutoPosition();
							C.addMember(mob,role);
							final Pair<Clan,Integer> newRole=mob.getClanRole(C.clanID());
							if((newRole.second.intValue()!=C.getGovernment().getAcceptPos())
							&&(newRole.second.intValue()==C.getGovernment().getAutoRole()))
							{
								CMLib.clans().clanAnnounce(mob,L("The @x1 @x2 has a new Applicant: @x3",C.getGovernmentName(),C.clanID(),mob.Name()));
								mob.tell(L("You have successfully applied for membership in clan @x1.  Your application will be reviewed by management.  Use SCORE to check for a change in status.",C.clanID()));
							}
							else
							{
								if(C.getGovernment().getEntryScript().trim().length()>0)
								{
									final ScriptingEngine S=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
									S.setSavable(false);
									S.setVarScope("*");
									S.setScript(C.getGovernment().getEntryScript());
									final CMMsg msg2=CMClass.getMsg(mob,mob,null,CMMsg.MSG_OK_VISUAL,null,null,L("CLANENTRY"));
									S.executeMsg(mob, msg2);
									S.dequeResponses();
									S.tick(mob,Tickable.TICKID_MOB);
								}
								CMLib.clans().clanAnnounce(mob,L("The @x1 @x2 has a new member: @x3",C.getGovernmentName(),C.clanID(),mob.Name()));
								mob.tell(L("You have successfully joined @x1.  Use CLANDETAILS for information.",C.clanID()));
							}
						}
						else
						{
							msg.append(L("This @x1 already has the maximum number of members (@x2/@x3) and can not accept new applicants.",C.getGovernmentName(),""+numMembers,""+maxMembers));
						}
					}
					else
					{
						msg.append(L("You must be at least level @x1 to join a clan.",""+CMProps.getIntVar(CMProps.Int.MINCLANLEVEL)));
					}
				}
				else
				{
					msg.append(L("You are not of the right qualities to join @x1. Use CLANDETAILS \"@x2\" for more information.",C.clanID(),C.clanID()));
				}
			}
			else
			{
				msg.append(L("There is no clan named @x1.",clanName));
			}
		}
		else
		{
			msg.append(L("You haven't specified which clan you are applying to."));
		}
		mob.tell(msg.toString());
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

}
