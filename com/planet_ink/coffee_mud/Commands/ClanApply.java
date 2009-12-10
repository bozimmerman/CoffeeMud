package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
public class ClanApply extends StdCommand
{
	public ClanApply(){}

	private String[] access={"CLANAPPLY"};
	public String[] getAccessWords(){return access;}
    
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		commands.setElementAt(getAccessWords()[0],0);
		String qual=CMParms.combine(commands,1).toUpperCase();
		if(mob.isMonster()) return false;
		StringBuffer msg=new StringBuffer("");
		if(qual.length()>0)
		{
			if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
			{
				Clan C=CMLib.clans().findClan(qual);
				if(C!=null)
				{
                    if((C.getGovernment()==Clan.GVT_FAMILY)
                    &&(!CMLib.clans().isFamilyOfMembership(mob,C.getMemberList())))
                    {
                        msg.append("The clan  "+C.clanID()+" is a family.  You can not join a family, you must be born or married into it.");
                        return false;
                    }
					if(CMLib.masking().maskCheck(C.getAcceptanceSettings(),mob,true))
					{
                        if((CMLib.masking().maskCheck("-<"+CMProps.getIntVar(CMProps.SYSTEMI_MINCLANLEVEL),mob,true))
                        ||(C.getGovernment() == Clan.GVT_FAMILY))
                        {
                        	int maxMembers=CMProps.getIntVar(CMProps.SYSTEMI_MAXCLANMEMBERS);
                        	int numMembers=C.getSize();
                        	if((maxMembers<=0)||(numMembers<maxMembers))
                        	{
	                        	int role=C.getAutoPosition();
                                C.addMember(mob,role);
	    						if(mob.getClanRole()==Clan.POS_APPLICANT)
	    						{
	        						CMLib.clans().clanAnnounce(mob,"The "+C.typeName()+" "+C.clanID()+" has a new Applicant: "+mob.Name());
		    						mob.tell("You have successfully applied for membership in clan "+C.clanID()+".  Your application will be reviewed by management.  Use SCORE to check for a change in status.");
	    						}
	    						else
	    						{
	        						CMLib.clans().clanAnnounce(mob,"The "+C.typeName()+" "+C.clanID()+" has a new member: "+mob.Name());
	    							mob.tell("You have successfully joined "+C.clanID()+".  Use CLANDETAILS for information.");
	    						}
                        	}
                        	else
                        	{
                                msg.append("This "+C.typeName()+" already has the maximum number of members ("+numMembers+"/"+maxMembers+") and can not accept new applicants.");
                        	}
                        }
                        else
                        {
                            msg.append("You must be at least level "+CMProps.getIntVar(CMProps.SYSTEMI_MINCLANLEVEL)+" to join a clan.");
                        }
					}
					else
					{
						msg.append("You are not of the right qualities to join "+C.clanID()+". Use CLANDETAILS \""+C.clanID()+"\" for more information.");
					}
				}
				else
				{
					msg.append("There is no clan named "+qual+".");
				}
			}
			else
			{
				msg.append("You are already a member of "+mob.getClanID()+". You need to resign from your before you can apply to another.");
			}
		}
		else
		{
			msg.append("You haven't specified which clan you are applying to.");
		}
		mob.tell(msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return false;}

	
}
