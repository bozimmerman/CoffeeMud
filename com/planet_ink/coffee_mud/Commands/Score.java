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
public class Score extends Affect
{
	public Score(){}

	private String[] access={"SCORE","SC"};
	public String[] getAccessWords(){return access;}

	public StringBuffer getScore(MOB mob)
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
				levelStr="level "+mob.envStats().level()+" "+mob.charStats().getCurrentClass().name();
			else
				levelStr=mob.charStats().getCurrentClass().name()+" "+classLevel+"/"+mob.envStats().level();
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
			msg.append("You are ^H"+mob.Name()+"^? the ^H"+mob.charStats().getCurrentClass().name()+"^?.\n\r");
		else
			msg.append("You are ^H"+mob.Name()+"^?.\n\r");

		//if(mob.image().length()>0) msg.append("^<IMAGE '"+mob.image()+"' URL=\""+CommonStrings.getVar(CommonStrings.SYSTEM_IMAGEURL)+"\" ALIGN=RIGHT H=70 W=70^>^N\n\r");
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
					classList.append(C.name()+" ("+mob.charStats().getClassLevel(C)+") ");
				}
			}
			msg.append(classList.toString()+".\n\r");
		}

		if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_ACCOUNTEXPIRATION)&&(mob.playerStats()!=null))
            msg.append("Your account is Registered and Active until: "+new IQCalendar(mob.playerStats().getAccountExpiration()).d2String()+"!\n\r");

		String genderName="neuter";
		if(mob.charStats().getStat(CharStats.GENDER)=='M') genderName="male";
		else
		if(mob.charStats().getStat(CharStats.GENDER)=='F') genderName="female";
		msg.append("You are a ");
		if(mob.baseCharStats().getStat(CharStats.AGE)>0)
		    msg.append("^!"+mob.baseCharStats().getStat(CharStats.AGE)+"^? year old ");
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
			Clan C=Clans.getClan(mob.getClanID());
			if(C!=null)
			{
				String role=Clans.getRoleName(C.getGovernment(),mob.getClanRole(),true,false);
				role=Util.startWithAorAn(role);
				msg.append("You are "+role+" of the ^H"+mob.getClanID()+"^?^. Clan.\n\r");
			}
		}
		msg.append("\n\r^NYour stats are: \n\r^!"+mob.charStats().getStats()+"^?\n\r");
		msg.append("You have ^H"+mob.curState().getHitPoints()+"/"+mob.maxState().getHitPoints()+"^? hit points, ^H");
		msg.append(mob.curState().getMana()+"/"+mob.maxState().getMana()+"^? mana, and ^H");
		msg.append(mob.curState().getMovement()+"/"+mob.maxState().getMovement()+"^? movement.\n\r");
		if(mob.envStats().height()<0)
			msg.append("You are incorporeal, but still weigh ^!"+mob.baseWeight()+"^? pounds.\n\r");
		else
			msg.append("You are ^!"+mob.envStats().height()+"^? inches tall and weigh ^!"+mob.baseWeight()+"^? pounds.\n\r");
        if(CMSecurity.isAllowed(mob,mob.location(),"CARRYALL"))
            msg.append("You are carrying ^!"+mob.inventorySize()+"^? items weighing ^!"+mob.envStats().weight()+"^? pounds.\n\r");
        else
    		msg.append("You are carrying ^!"+mob.inventorySize()+"^? items weighing ^!"+mob.envStats().weight()+"^?/^!"+mob.maxCarry()+"^? pounds.\n\r");
		msg.append("You have ^!"+mob.getPractices()+"^? practices, ^!"+mob.getTrains()+"^? training sessions, and ^!"+mob.getQuestPoint()+"^? quest points.\n\r");
		if((!CMSecurity.isDisabled("EXPERIENCE"))
		&&!mob.charStats().getCurrentClass().expless()
		&&!mob.charStats().getMyRace().expless())
		{
			msg.append("You have scored ^!"+mob.getExperience()+"^? experience points, and have been online for ^!"+Math.round(Util.div(mob.getAgeHours(),60.0))+"^? hours.\n\r");
			if((!CMSecurity.isDisabled("LEVELS"))
			&&(!mob.charStats().getCurrentClass().leveless())
			&&(!mob.charStats().getMyRace().leveless()))
			{
				if((CommonStrings.getIntVar(CommonStrings.SYSTEMI_LASTPLAYERLEVEL)>0)
				&&(mob.baseEnvStats().level()>CommonStrings.getIntVar(CommonStrings.SYSTEMI_LASTPLAYERLEVEL)))
					msg.append("You will not gain further levels through experience.\n\r");
				else
				if(mob.getExpNeededLevel()==Integer.MAX_VALUE)
					msg.append("You will not gain further levels through experience.\n\r");
				else
					msg.append("You need ^!"+(mob.getExpNeededLevel())+"^? experience points to advance to the next level.\n\r");
			}
		}
		for(Enumeration e=mob.fetchFactions();e.hasMoreElements();)
		{
		    String factionID=(String)e.nextElement();
		    Faction.FactionRange FR=Factions.getRange(factionID,mob.fetchFaction(factionID));
		    if((FR!=null)&&(FR.myFaction.showinscore))
	            msg.append("Your "+Util.padRight(FR.myFaction.name+" is",18)+": ^H"+FR.Name+"^?.\n\r");
		}
		msg.append("Your armored defense is: ^H"+CommonStrings.armorStr(adjustedArmor)+"^?.\n\r");
		msg.append("Your combat prowess is : ^H"+CommonStrings.fightingProwessStr(adjustedAttack)+"^?.\n\r");
		msg.append("Wimpy is set to ^!"+mob.getWimpHitPoint()+"^? hit points.\n\r");
	    if((mob.playerStats()!=null)&&(mob.soulMate()==null)&&(mob.playerStats().getHygiene()>=PlayerStats.HYGIENE_DELIMIT))
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

		if(Sense.isBound(mob))
			msg.append("^!You are bound.^?\n\r");

		if(Sense.isFalling(mob))
			msg.append("^!You are falling!!!^?\n\r");
		else
		if(Sense.isSleeping(mob))
			msg.append("^!You are sleeping.^?\n\r");
		else
		if(Sense.isSitting(mob))
			msg.append("^!You are resting.^?\n\r");
		else
		if(Sense.isSwimming(mob))
			msg.append("^!You are swimming.^?\n\r");
		else
		if(Sense.isClimbing(mob))
			msg.append("^!You are climbing.^?\n\r");
		else
		if(Sense.isFlying(mob))
			msg.append("^!You are flying.^?\n\r");
		else
			msg.append("^!You are standing.^?\n\r");

		if(mob.riding()!=null)
			msg.append("^!You are "+mob.riding().stateString(mob)+" "+mob.riding().name()+".^?\n\r");

		if(Sense.isInvisible(mob))
			msg.append("^!You are invisible.^?\n\r");
		if(Sense.isHidden(mob))
			msg.append("^!You are hidden.^?\n\r");
		if(Sense.isSneaking(mob))
			msg.append("^!You are sneaking.^?\n\r");
		if(Util.bset(mob.getBitmap(),MOB.ATT_QUIET))
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
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
