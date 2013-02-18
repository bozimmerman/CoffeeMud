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
public class Score extends Affect
{
	public Score(){}

	private final String[] access={"SCORE","SC"};
	public String[] getAccessWords(){return access;}

	public StringBuilder getScore(MOB mob){return getScore(mob,"");}
	public StringBuilder getScore(MOB mob, String parm)
	{
		StringBuilder msg=new StringBuilder("^N");

		int classLevel=mob.charStats().getClassLevel(mob.charStats().getCurrentClass());
		if((!CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES))
		&&(!mob.charStats().getMyRace().classless())
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS))
		&&(!mob.charStats().getMyRace().leveless())
		&&(!mob.charStats().getCurrentClass().leveless()))
		{
			String levelStr=null;
			if(classLevel>=mob.phyStats().level())
				levelStr="level "+mob.phyStats().level()+" "+mob.charStats().getCurrentClass().name(mob.charStats().getCurrentClassLevel());
			else
				levelStr=mob.charStats().getCurrentClass().name(mob.charStats().getCurrentClassLevel())+" "+classLevel+"/"+mob.phyStats().level();
			msg.append("You are ^H"+mob.Name()+"^? the ^H"+levelStr+"^?.\n\r");
		}
		else
		if((!CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS))
		&&(!mob.charStats().getCurrentClass().leveless())
		&&(!mob.charStats().getMyRace().leveless()))
		{
			String levelStr=null;
			if(classLevel>=mob.phyStats().level())
				levelStr=", level "+mob.phyStats().level();
			else
				levelStr=", level "+classLevel+"/"+mob.phyStats().level();
			msg.append("You are ^H"+mob.Name()+"^?^H"+levelStr+"^?.\n\r");
		}
		else
		if((!CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES))
		&&(!mob.charStats().getMyRace().classless()))
			msg.append("You are ^H"+mob.Name()+"^? the ^H"+mob.charStats().getCurrentClass().name(mob.charStats().getCurrentClassLevel())+"^?.\n\r");
		else
			msg.append("You are ^H"+mob.Name()+"^?.\n\r");

		if((!CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES))
		&&(classLevel<mob.phyStats().level()))
		{
			msg.append("You also have levels in: ");
			StringBuilder classList=new StringBuilder("");
			for(int c=0;c<mob.charStats().numClasses()-1;c++)
			{
				CharClass C=mob.charStats().getMyClass(c);
				if(C!=mob.charStats().getCurrentClass())
				{
					if(classList.length()>0)
						if(c==mob.charStats().numClasses()-2)
							classList.append(", and ");
						else
							classList.append(", ");
					classList.append(C.name(mob.charStats().getClassLevel(C))+" ("+mob.charStats().getClassLevel(C)+") ");
				}
			}
			msg.append(classList.toString()+".\n\r");
		}

		if(CMProps.getBoolVar(CMProps.SYSTEMB_ACCOUNTEXPIRATION)&&(mob.playerStats()!=null))
			msg.append("Your account is Registered and Active until: "+CMLib.time().date2String(mob.playerStats().getAccountExpiration())+"!\n\r");

		String genderName="neuter";
		if(mob.charStats().getStat(CharStats.STAT_GENDER)=='M') genderName="male";
		else
		if(mob.charStats().getStat(CharStats.STAT_GENDER)=='F') genderName="female";
		msg.append("You are a ");
		if(mob.baseCharStats().getStat(CharStats.STAT_AGE)>0)
			msg.append("^!"+mob.baseCharStats().getStat(CharStats.STAT_AGE)+"^? year old ");
		msg.append("^!"+genderName);
		if((!CMSecurity.isDisabled(CMSecurity.DisFlag.RACES))
		&&(!mob.charStats().getCurrentClass().raceless()))
			msg.append(" "+mob.charStats().getMyRace().name() + "^?");
		else
			msg.append("^?");
		if(mob.getLiegeID().length()>0)
		{
			if(mob.isMarriedToLiege())
				msg.append(" who is married to ^H"+mob.getLiegeID()+"^?");
			else
				msg.append(" who serves ^H"+mob.getLiegeID()+"^?");
		}
		if(mob.getWorshipCharID().length()>0)
			msg.append(" worshipping ^H"+mob.getWorshipCharID()+"^?");
		msg.append(".\n\r");
		if(mob.clans().iterator().hasNext())
		{
			msg.append("You are ");
			for(Iterator<Pair<Clan,Integer>> c = mob.clans().iterator();c.hasNext();)
			{
				Pair<Clan,Integer> p=c.next();
				Clan C=p.first;
				String role=C.getRoleName(p.second.intValue(),true,false);
				role=CMLib.english().startWithAorAn(role);
				msg.append(role+" of ^H"+C.getName()+"^?^.");
				if(c.hasNext())
					msg.append(", ");
			}
			msg.append("\n\r");
		}
		msg.append("\n\r^NYour stats are: ");
		msg.append(CMProps.mxpImage(mob," ALIGN=RIGHT H=70 W=70"));
		msg.append("\n\r");
		CharStats CT=mob.charStats();
		if(parm.equalsIgnoreCase("BASE")) CT=mob.baseCharStats();
		msg.append("^N^!");
		for(int i : CharStats.CODES.BASE())
			msg.append(CMStrings.padRight("^<HELP^>" + CMStrings.capitalizeAndLower(CharStats.CODES.NAME(i))+"^</HELP^>",15)
					+": "
					+CMStrings.padRight(Integer.toString(CT.getStat(i)),2)
					+"/"
					+(CT.getMaxStat(i))+"\n\r");
		msg.append("^?\n\r");
		msg.append("You have ^H"+mob.curState().getHitPoints()+"/"+mob.maxState().getHitPoints()+"^? ^<HELP^>hit points^</HELP^>, ^H");
		msg.append(mob.curState().getMana()+"/"+mob.maxState().getMana()+"^? ^<HELP^>mana^</HELP^>, and ^H");
		msg.append(mob.curState().getMovement()+"/"+mob.maxState().getMovement()+"^? ^<HELP^>movement^</HELP^>.\n\r");
		if(mob.phyStats().height()<0)
			msg.append("You are incorporeal, but still weigh ^!"+mob.baseWeight()+"^? pounds.\n\r");
		else
			msg.append("You are ^!"+mob.phyStats().height()+"^? inches tall and weigh ^!"+mob.baseWeight()+"^? pounds.\n\r");
		if(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CARRYALL))
			msg.append("You are carrying ^!"+mob.numItems()+"^? items weighing ^!"+mob.phyStats().weight()+"^? pounds.\n\r");
		else
			msg.append("You are carrying ^!"+mob.numItems()+"^?/^!"+mob.maxItems()+"^? items weighing ^!"+mob.phyStats().weight()+"^?/^!"+mob.maxCarry()+"^? pounds.\n\r");
		msg.append("You have ^!"+mob.getPractices()+"^? ^<HELP^>practices^</HELP^>, ^!"+mob.getTrains()+"^? ^<HELP^>training sessions^</HELP^>, and ^!"+mob.getQuestPoint()+"^? ^<HELP^>quest points^</HELP^>.\n\r");
		if((!CMSecurity.isDisabled(CMSecurity.DisFlag.EXPERIENCE))
		&&!mob.charStats().getCurrentClass().expless()
		&&!mob.charStats().getMyRace().expless())
		{
			if((!CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS))
			&&(!mob.charStats().getCurrentClass().leveless())
			&&(!mob.charStats().getMyRace().leveless()))
			{
				if(((CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL)>0)
					&&(mob.basePhyStats().level()>CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL)))
				||(mob.getExpNeededLevel()==Integer.MAX_VALUE)
				||(mob.charStats().isLevelCapped(mob.charStats().getCurrentClass())))
					msg.append("You have scored ^!"+mob.getExperience()+"^? ^<HELP^>experience points^</HELP^>, ^!"+(mob.getExpNeededDelevel())+"^? over your last level.\n\r");
				else
					msg.append("You have scored ^!"+mob.getExperience()+"^? ^<HELP^>experience points^</HELP^>, and need ^!"+(mob.getExpNeededLevel())+"^? to advance.\n\r");
			}
			else
				msg.append("You have scored ^!"+mob.getExperience()+"^? ^<HELP^>experience points^</HELP^>.\n\r");
		}
		msg.append("You have been online for ^!"+Math.round(CMath.div(mob.getAgeMinutes(),60.0))+"^? hours.\n\r");
		for(Enumeration e=mob.fetchFactions();e.hasMoreElements();)
		{
			String factionID=(String)e.nextElement();
			Faction F=CMLib.factions().getFaction(factionID);
			if(F!=null)
			{
				int factionAmt=mob.fetchFaction(factionID);
				Faction.FRange FR=CMLib.factions().getRange(factionID,factionAmt);
				if((FR!=null)&&(F.showInScore()))
					msg.append("Your "+CMStrings.padRight("^<HELP^>"+F.name()+"^</HELP^> is",18)+": ^H"+FR.name()+" ^.("+factionAmt+").\n\r");
			}
		}
		msg.append("Your ^<HELP^>armored defence^</HELP^> is: ^H"+CMLib.combat().armorStr(mob)+"^..\n\r");
		msg.append("Your ^<HELP^>combat prowess^</HELP^> is : ^H"+CMLib.combat().fightingProwessStr(mob)+"^..\n\r");
		//if(CMLib.flags().canSeeHidden(mob))
		//    msg.append("Your ^<HELP^>observation score^</HELP^> : ^H"+CMLib.flags().getDetectScore(mob)+"^?.\n\r");
		msg.append("Wimpy is set to ^!"+mob.getWimpHitPoint()+"^? hit points.\n\r");
		
		msg.append(getMOBState(mob));
		msg.append(getAffects(mob.session(),mob,false,false));
		return msg;
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String parm="";
		if(commands.size()>1)
			parm=CMParms.combine(commands,1);
		StringBuilder msg=getScore(mob,parm);
		if(commands.size()==0)
		{
			commands.addElement(msg);
			return false;
		}
		if(!mob.isMonster())
			mob.session().wraplessPrintln(msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
