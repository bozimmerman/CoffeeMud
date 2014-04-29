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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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
public class ClanApply extends StdCommand
{
	public ClanApply(){}

	private final String[] access={"CLANAPPLY"};
	@Override public String[] getAccessWords(){return access;}

	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		commands.setElementAt(getAccessWords()[0],0);
		String clanName=CMParms.combine(commands,1);
		if(mob.isMonster()) return false;
		StringBuffer msg=new StringBuffer("");
		if(clanName.length()>0)
		{
			Clan C=CMLib.clans().findClan(clanName);
			if(C!=null)
			{
				if(C.isOnlyFamilyApplicants()
				&&(!CMLib.clans().isFamilyOfMembership(mob,C.getMemberList())))
				{
					mob.tell("The clan  "+C.clanID()+" is a family.  You can not join a family, you must be born or married into it.");
					return false;
				}

				List<Pair<Clan,Integer>> oldList=CMLib.clans().getClansByCategory(mob, C.getCategory());
				if(oldList.size()>=CMProps.getMaxClansThisCategory(C.getCategory()))
				{
					if(oldList.size()>0)
					{
						Pair<Clan,Integer> p=oldList.get(0);
						mob.tell("You are already a member of "+p.first.getName()+". You need to resign before you can apply to another.");
					}
					else
						mob.tell("You are not elligible to apply to this clan.");
					return false;
				}

				if(!CMLib.masking().maskCheck(C.getBasicRequirementMask(), mob, true))
				{
					mob.tell("You are not of the right qualities to join "+C.clanID()+". Use CLANDETAILS \""+C.clanID()+"\" for more information.");
					return false;
				}

				CharClass CC = CMClass.getCharClass(C.getClanClass());
				if((CC!=null) && (mob.charStats().getClassLevel(CC)<0) && (!CC.qualifiesForThisClass(mob, false)))
					return false;

				if(CMLib.masking().maskCheck(C.getAcceptanceSettings(),mob,true))
				{
					List<Clan.MemberRecord> members=C.getMemberList();
					if((CMLib.masking().maskCheck("-<"+CMProps.getIntVar(CMProps.Int.MINCLANLEVEL),mob,true))
					||(CMLib.clans().isFamilyOfMembership(mob,members)))
					{
						int maxMembers=CMProps.getIntVar(CMProps.Int.MAXCLANMEMBERS);
						int numMembers=members.size();
						if((maxMembers<=0)||(numMembers<maxMembers))
						{
							int role=C.getAutoPosition();
							C.addMember(mob,role);
							Pair<Clan,Integer> newRole=mob.getClanRole(C.clanID());
							if((newRole.second.intValue()!=C.getGovernment().getAcceptPos())
							&&(newRole.second.intValue()==C.getGovernment().getAutoRole()))
							{
								CMLib.clans().clanAnnounce(mob,"The "+C.getGovernmentName()+" "+C.clanID()+" has a new Applicant: "+mob.Name());
								mob.tell("You have successfully applied for membership in clan "+C.clanID()+".  Your application will be reviewed by management.  Use SCORE to check for a change in status.");
							}
							else
							{
								if(C.getGovernment().getEntryScript().trim().length()>0)
								{
									ScriptingEngine S=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
									S.setSavable(false);
									S.setVarScope("*");
									S.setScript(C.getGovernment().getEntryScript());
									CMMsg msg2=CMClass.getMsg(mob,mob,null,CMMsg.MSG_OK_VISUAL,null,null,"CLANENTRY");
									S.executeMsg(mob, msg2);
									S.dequeResponses();
									S.tick(mob,Tickable.TICKID_MOB);
								}
								CMLib.clans().clanAnnounce(mob,"The "+C.getGovernmentName()+" "+C.clanID()+" has a new member: "+mob.Name());
								mob.tell("You have successfully joined "+C.clanID()+".  Use CLANDETAILS for information.");
							}
						}
						else
						{
							msg.append("This "+C.getGovernmentName()+" already has the maximum number of members ("+numMembers+"/"+maxMembers+") and can not accept new applicants.");
						}
					}
					else
					{
						msg.append("You must be at least level "+CMProps.getIntVar(CMProps.Int.MINCLANLEVEL)+" to join a clan.");
					}
				}
				else
				{
					msg.append("You are not of the right qualities to join "+C.clanID()+". Use CLANDETAILS \""+C.clanID()+"\" for more information.");
				}
			}
			else
			{
				msg.append("There is no clan named "+clanName+".");
			}
		}
		else
		{
			msg.append("You haven't specified which clan you are applying to.");
		}
		mob.tell(msg.toString());
		return false;
	}

	@Override public boolean canBeOrdered(){return false;}


}
