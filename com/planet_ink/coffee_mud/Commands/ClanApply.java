package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class ClanApply extends BaseClanner
{
	public ClanApply(){}

	private String[] access={"CLANAPPLY"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		commands.setElementAt("clanapply",0);
		String qual=Util.combine(commands,1).toUpperCase();
		if(mob.isMonster()) return false;
		StringBuffer msg=new StringBuffer("");
		if(qual.length()>0)
		{
			if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
			{
				Clan C=Clans.getClan(qual);
				if(C!=null)
				{
					if((MUDZapper.zapperCheck(C.getAcceptanceSettings(),mob))
					&&(MUDZapper.zapperCheck("-<"+CommonStrings.getIntVar(CommonStrings.SYSTEMI_MINCLANLEVEL),mob)))
					{
						CMClass.DBEngine().DBUpdateClanMembership(mob.Name(), C.ID(), Clan.POS_APPLICANT);
						mob.setClanID(C.ID());
						mob.setClanRole(Clan.POS_APPLICANT);
						clanAnnounce(mob,"New Applicant: "+mob.Name());
						mob.tell("You have successfully applied for membership in clan "+C.ID()+".  Your application will be reviewed by management.  Use SCORE to check for a change in status.");
					}
					else
					{
						msg.append("You are not of the right qualities to join "+C.ID()+". Use CLANDETAILS \""+C.ID()+"\" for more information.");
					}
				}
				else
				{
					msg.append("There is no clan named '"+qual+"'.");
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
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
