package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

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
