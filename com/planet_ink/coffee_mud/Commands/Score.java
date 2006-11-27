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
public class Score extends Affect
{
	public Score(){}

	private String[] access={"SCORE","SC"};
	public String[] getAccessWords(){return access;}

    public StringBuffer getScore(MOB mob){return getScore(mob,"");}
	public StringBuffer getScore(MOB mob, String parm)
	{
		int adjustedAttack=mob.adjustedAttackBonus(null);
		int adjustedArmor=(-mob.adjustedArmor())+50;

		StringBuffer msg=new StringBuffer("^N");

		int classLevel=mob.charStats().getClassLevel(mob.charStats().getCurrentClass());
		if((!CMSecurity.isDisabled("CLASSES"))
		&&(!mob.charStats().getMyRace().classless())
		&&(!CMSecurity.isDisabled("LEVELS"))
		&&(!mob.charStats().getMyRace().leveless())
		&&(!mob.charStats().getCurrentClass().leveless()))
		{
			String levelStr=null;
			if(classLevel>=mob.envStats().level())
				levelStr="level "+mob.envStats().level()+" "+mob.charStats().getCurrentClass().name(mob.charStats().getCurrentClassLevel());
			else
				levelStr=mob.charStats().getCurrentClass().name(mob.charStats().getCurrentClassLevel())+" "+classLevel+"/"+mob.envStats().level();
			msg.append("You are ^H"+mob.Name()+"^? the ^H"+levelStr+"^?.\n\r");
		}
		else
		if((!CMSecurity.isDisabled("LEVELS"))
		&&(!mob.charStats().getCurrentClass().leveless())
		&&(!mob.charStats().getMyRace().leveless()))
		{
			String levelStr=null;
			if(classLevel>=mob.envStats().level())
				levelStr=", level "+mob.envStats().level();
			else
				levelStr=", level "+classLevel+"/"+mob.envStats().level();
			msg.append("You are ^H"+mob.Name()+"^?^H"+levelStr+"^?.\n\r");
		}
		else
		if((!CMSecurity.isDisabled("CLASSES"))
		&&(!mob.charStats().getMyRace().classless()))
			msg.append("You are ^H"+mob.Name()+"^? the ^H"+mob.charStats().getCurrentClass().name(mob.charStats().getCurrentClassLevel())+"^?.\n\r");
		else
			msg.append("You are ^H"+mob.Name()+"^?.\n\r");

		if((!CMSecurity.isDisabled("CLASSES"))
		&&(classLevel<mob.envStats().level()))
		{
			msg.append("You also have levels in: ");
			StringBuffer classList=new StringBuffer("");
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
		if((!CMSecurity.isDisabled("RACES"))
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
		if((mob.getClanID()!=null)&&(mob.getClanID().length()>0))
		{
			Clan C=CMLib.clans().getClan(mob.getClanID());
			if(C!=null)
			{
				String role=CMLib.clans().getRoleName(C.getGovernment(),mob.getClanRole(),true,false);
				role=CMStrings.startWithAorAn(role);
				msg.append("You are "+role+" of the ^H"+mob.getClanID()+"^?^. Clan.\n\r");
			}
		}
        msg.append("\n\r^NYour stats are: \n\r^!");
        int max=CMProps.getIntVar(CMProps.SYSTEMI_BASEMAXSTAT);
        CharStats CT=mob.charStats();
        if(parm.equalsIgnoreCase("BASE")) CT=mob.baseCharStats();
        msg.append(CMProps.mxpImage(mob," ALIGN=RIGHT H=70 W=70"));
        msg.append("^N^!");
        msg.append(CMStrings.padRight("^<HELP^>Strength^</HELP^>",15)+": "+CMStrings.padRight(Integer.toString(CT.getStat(CharStats.STAT_STRENGTH)),2)+"/"+(max+CT.getStat(CharStats.STAT_MAX_STRENGTH_ADJ))+"\n\r");
        msg.append(CMStrings.padRight("^<HELP^>Intelligence^</HELP^>",15)+": "+CMStrings.padRight(Integer.toString(CT.getStat(CharStats.STAT_INTELLIGENCE)),2)+"/"+(max+CT.getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ))+"\n\r");
        msg.append(CMStrings.padRight("^<HELP^>Dexterity^</HELP^>",15)+": "+CMStrings.padRight(Integer.toString(CT.getStat(CharStats.STAT_DEXTERITY)),2)+"/"+(max+CT.getStat(CharStats.STAT_MAX_DEXTERITY_ADJ))+"\n\r");
        msg.append(CMStrings.padRight("^<HELP^>Wisdom^</HELP^>",15)+": "+CMStrings.padRight(Integer.toString(CT.getStat(CharStats.STAT_WISDOM)),2)+"/"+(max+CT.getStat(CharStats.STAT_MAX_WISDOM_ADJ))+"\n\r");
        msg.append(CMStrings.padRight("^<HELP^>Constitution^</HELP^>",15)+": "+CMStrings.padRight(Integer.toString(CT.getStat(CharStats.STAT_CONSTITUTION)),2)+"/"+(max+CT.getStat(CharStats.STAT_MAX_CONSTITUTION_ADJ))+"\n\r");
        msg.append(CMStrings.padRight("^<HELP^>Charisma^</HELP^>",15)+": "+CMStrings.padRight(Integer.toString(CT.getStat(CharStats.STAT_CHARISMA)),2)+"/"+(max+CT.getStat(CharStats.STAT_MAX_CHARISMA_ADJ))+"\n\r");
        msg.append("^?\n\r");
		msg.append("You have ^H"+mob.curState().getHitPoints()+"/"+mob.maxState().getHitPoints()+"^? ^<HELP^>hit points^</HELP^>, ^H");
		msg.append(mob.curState().getMana()+"/"+mob.maxState().getMana()+"^? ^<HELP^>mana^</HELP^>, and ^H");
		msg.append(mob.curState().getMovement()+"/"+mob.maxState().getMovement()+"^? ^<HELP^>movement^</HELP^>.\n\r");
		if(mob.envStats().height()<0)
			msg.append("You are incorporeal, but still weigh ^!"+mob.baseWeight()+"^? pounds.\n\r");
		else
			msg.append("You are ^!"+mob.envStats().height()+"^? inches tall and weigh ^!"+mob.baseWeight()+"^? pounds.\n\r");
        if(CMSecurity.isAllowed(mob,mob.location(),"CARRYALL"))
            msg.append("You are carrying ^!"+mob.inventorySize()+"^? items weighing ^!"+mob.envStats().weight()+"^? pounds.\n\r");
        else
    		msg.append("You are carrying ^!"+mob.inventorySize()+"^?/^!"+mob.maxItems()+"^? items weighing ^!"+mob.envStats().weight()+"^?/^!"+mob.maxCarry()+"^? pounds.\n\r");
		msg.append("You have ^!"+mob.getPractices()+"^? ^<HELP^>practices^</HELP^>, ^!"+mob.getTrains()+"^? ^<HELP^>training sessions^</HELP^>, and ^!"+mob.getQuestPoint()+"^? ^<HELP^>quest points^</HELP^>.\n\r");
		if((!CMSecurity.isDisabled("EXPERIENCE"))
		&&!mob.charStats().getCurrentClass().expless()
		&&!mob.charStats().getMyRace().expless())
		{
			if((!CMSecurity.isDisabled("LEVELS"))
			&&(!mob.charStats().getCurrentClass().leveless())
			&&(!mob.charStats().getMyRace().leveless()))
			{
				if(((CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL)>0)
					&&(mob.baseEnvStats().level()>CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL)))
				||(mob.getExpNeededLevel()==Integer.MAX_VALUE))
					msg.append("You have scored ^!"+mob.getExperience()+"^? ^<HELP^>experience points^</HELP^>, "+(mob.getExpNeededDelevel())+" over your last level.\n\r");
				else
					msg.append("You have scored ^!"+mob.getExperience()+"^? ^<HELP^>experience points^</HELP^>, and need "+(mob.getExpNeededLevel())+" to advance.\n\r");
			}
			else
				msg.append("You have scored ^!"+mob.getExperience()+"^? ^<HELP^>experience points^</HELP^>.\n\r");
		}
		msg.append("You have been online for ^!"+Math.round(CMath.div(mob.getAgeHours(),60.0))+"^? hours.\n\r");
		for(Enumeration e=mob.fetchFactions();e.hasMoreElements();)
		{
		    String factionID=(String)e.nextElement();
		    Faction.FactionRange FR=CMLib.factions().getRange(factionID,mob.fetchFaction(factionID));
		    if((FR!=null)&&(FR.myFaction().showinscore()))
	            msg.append("Your "+CMStrings.padRight("^<HELP^>"+FR.myFaction().name()+"^</HELP^> is",18)+": ^H"+FR.name()+"^?.\n\r");
		}
		msg.append("Your ^<HELP^>armored defence^</HELP^> is: ^H"+CMLib.combat().armorStr(adjustedArmor)+"^?.\n\r");
		msg.append("Your ^<HELP^>combat prowess^</HELP^> is : ^H"+CMLib.combat().fightingProwessStr(adjustedAttack)+"^?.\n\r");
		//if(CMLib.flags().canSeeHidden(mob))
		//	msg.append("Your ^<HELP^>observation score^</HELP^> : ^H"+CMLib.flags().getDetectScore(mob)+"^?.\n\r");
		msg.append("Wimpy is set to ^!"+mob.getWimpHitPoint()+"^? hit points.\n\r");
	    if((mob.playerStats()!=null)&&(mob.soulMate()==null)&&(mob.playerStats().getHygiene()>=PlayerStats.HYGIENE_DELIMIT))
	    {
	    	if(CMSecurity.isASysOp(mob)) 
	    		mob.playerStats().setHygiene(0);
	    	else
	    	{
		        int x=(int)(mob.playerStats().getHygiene()/PlayerStats.HYGIENE_DELIMIT);
		        if(x<=1) msg.append("^!You could use a bath.^?\n\r"); 
		        else
		        if(x<=3) msg.append("^!You could really use a bath.^?\n\r"); 
		        else
		        if(x<=7) msg.append("^!You need to bathe, soon.^?\n\r");
		        else
		        if(x<15) msg.append("^!You desperately need to bathe.^?\n\r");
		        else msg.append("^!Your stench is horrendous! Bathe dammit!.^?\n\r");
	    	}
	    }

		if(CMLib.flags().isBound(mob))
			msg.append("^!You are bound.^?\n\r");

		// dont do falling -- the flag doubles for drowning/treading water anyway.
		//if(CMLib.flags().isFalling(mob))
		//	msg.append("^!You are falling!!!^?\n\r");
		//else
		if(CMLib.flags().isSleeping(mob))
			msg.append("^!You are sleeping.^?\n\r");
		else
		if(CMLib.flags().isSitting(mob))
			msg.append("^!You are resting.^?\n\r");
		else
		if(CMLib.flags().isSwimmingInWater(mob))
			msg.append("^!You are swimming.^?\n\r");
		else
		if(CMLib.flags().isClimbing(mob))
			msg.append("^!You are climbing.^?\n\r");
		else
		if(CMLib.flags().isFlying(mob))
			msg.append("^!You are flying.^?\n\r");
		else
			msg.append("^!You are standing.^?\n\r");

		if(mob.riding()!=null)
			msg.append("^!You are "+mob.riding().stateString(mob)+" "+mob.riding().name()+".^?\n\r");
		
		if(CMath.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL))
			msg.append("^!Your playerkill flag is on.^?\n\r");

		if(CMLib.flags().isInvisible(mob))
			msg.append("^!You are invisible.^?\n\r");
		if(CMLib.flags().isHidden(mob))
			msg.append("^!You are hidden.^?\n\r");// ("+CMLib.flags().getHideScore(mob)+").^?\n\r");
		if(CMLib.flags().isSneaking(mob))
			msg.append("^!You are sneaking.^?\n\r");
		if(CMath.bset(mob.getBitmap(),MOB.ATT_QUIET))
			msg.append("^!You are in QUIET mode.^?\n\r");
		
		if(mob.curState().getFatigue()>CharState.FATIGUED_MILLIS)
			msg.append("^!You are fatigued.^?\n\r");
		if(mob.curState().getHunger()<1)
			msg.append("^!You are hungry.^?\n\r");
		if(mob.curState().getThirst()<1)
			msg.append("^!You are thirsty.^?\n\r");
		msg.append(getAffects(mob));
		return msg;
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		StringBuffer msg=getScore(mob);
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
