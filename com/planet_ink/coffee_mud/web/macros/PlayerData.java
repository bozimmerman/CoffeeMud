package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class PlayerData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public final static String[] BASICS={
		"NAME",
		"DESCRIPTION",
		"LASTDATETIME",
		"EMAIL",
		"RACE",
		"CHARCLASS",
		"LEVEL",
		"LEVELSTR",
		"CLASSLEVEL",
		"CLASSES",
		"MAXCARRY",
		"ATTACK",
		"ARMOR",
		"DAMAGE",
		"HOURS",
		"PRACTICES",
		"EXPERIENCE",
		"EXPERIENCELEVEL",
		"TRAINS",
		"MONEY",
		"DEITY",
		"LEIGE",
		"CLAN",
		"CLANROLE",
		"ALIGNMENT",
		"ALIGNMENTSTRING",
		"WIMP",
		"STARTROOM",
		"LOCATION",
		"STARTROOMID",
		"LOCATIONID",
		"INVENTORY",
		"WEIGHT",
		"ENCUMBRANCE",
		"GENDER",
		"LASTDATETIMEMILLIS",
		"HITPOINTS",
		"MANA",
		"MOVEMENT",
		"RIDING",
		"HEIGHT",
		"LASTIP",
		"QUESTPOINTS",
		"BASEHITPOINTS",
		"BASEMANA",
		"BASEMOVEMENT",
	};
	public static int getBasicCode(String val)
	{
		for(int i=0;i<BASICS.length;i++)
			if(val.equalsIgnoreCase(BASICS[i])) 
				return i;
		return -1;
	}
	public static String getBasic(MOB M, int i)
	{
		StringBuffer str=new StringBuffer("");
		switch(i)
		{
		case 0: str.append(M.Name()+", "); break;
		case 1: str.append(M.description()+", "); break;
		case 2:  if(M.playerStats()!=null)
					str.append(IQCalendar.d2String(M.playerStats().lastDateTime())+", "); 
				 break;
		case 3: if(M.playerStats()!=null)
					str.append(M.playerStats().getEmail()+", "); 
				break;
		case 4: str.append(M.baseCharStats().getMyRace().name()+", "); break;
		case 5: str.append(M.baseCharStats().getCurrentClass().name()+", "); break;
		case 6: str.append(M.baseEnvStats().level()+", "); break;
		case 7: str.append(M.baseCharStats().displayClassLevel(M,true)+", "); break;
		case 8: str.append(M.baseCharStats().getClassLevel(M.baseCharStats().getCurrentClass())+", "); break;
		case 9: for(int c=M.charStats().numClasses()-1;c>=0;c--)
				{
					CharClass C=M.charStats().getMyClass(c);
					str.append(C.name()+" ("+M.charStats().getClassLevel(C)+") ");
				}
				str.append(", ");
				break;
		case 10: str.append(M.maxCarry()+", "); break;
		case 11: str.append(Util.capitalize(CommonStrings.fightingProwessStr(M.adjustedAttackBonus()))+", "); break;
		case 12: str.append(Util.capitalize(CommonStrings.armorStr((-M.adjustedArmor())+50))+", "); break;
		case 13: str.append(M.adjustedDamage(null,null)+", "); break;
		case 14: str.append(Math.round(Util.div(M.getAgeHours(),60.0))+", "); break;
		case 15: str.append(M.getPractices()+", "); break;
		case 16: str.append(M.getExperience()+", "); break;
		case 17: str.append(M.getExpNextLevel()+", "); break;
		case 18: str.append(M.getTrains()+", "); break;
		case 19: str.append(M.getMoney()+", "); break;
		case 20: str.append(M.getWorshipCharID()+", "); break;
		case 21: str.append(M.getLeigeID()+", "); break;
		case 22: str.append(M.getClanID()+", "); break;
		case 23: if(M.getClanID().length()>0)
					 str.append(Clans.getRoleName(M.getClanRole(),true,false)+", ");
				 break;
		case 24: str.append(M.getAlignment()+", "); break;
		case 25: str.append(Util.capitalize(CommonStrings.alignmentStr(M.getAlignment()))+", "); break;
		case 26: str.append(M.getWimpHitPoint()+", "); break;
		case 27: if(M.getStartRoom()!=null)
					 str.append(M.getStartRoom().displayText()+", "); 
				 break;
		case 28: if(M.location()!=null)
					 str.append(M.location().displayText()+", "); 
				 break;
		case 29: if(M.getStartRoom()!=null)
					 str.append(M.getStartRoom().roomID()+", "); 
				 break;
		case 30: if(M.location()!=null)
					 str.append(M.location().roomID()+", "); 
				 break;
		case 31:
				for(int inv=0;inv<M.inventorySize();inv++)
				{ 
					Item I=M.fetchInventory(inv);
					if((I!=null)&&(I.container()==null))
						  str.append(I.name()+", ");
				}
			break;
		case 32: str.append(M.baseEnvStats().weight()+", "); break;
		case 33: str.append(M.envStats().weight()+", "); break;
		case 34: str.append(Util.capitalize(M.baseCharStats().genderName())+", "); break;
		case 35: if(M.playerStats()!=null)
					 str.append(M.playerStats().lastDateTime()+", "); 
				 break;
		case 36: str.append(M.curState().getHitPoints()+", "); break;
		case 37: str.append(M.curState().getMana()+", "); break;
		case 38: str.append(M.curState().getMovement()+", "); break;
		case 39: if(M.riding()!=null)
					 str.append(M.riding().name()+", ");
				 break;
		case 40: str.append(M.baseEnvStats().height()+", "); break;
		case 41: if(!M.isMonster())
					 str.append(M.session().getAddress()+", ");
				 else
				 if(M.playerStats()!=null)
					 str.append(M.playerStats().lastIP()+", "); 
				 break;
		case 42:  str.append(M.getQuestPoint()+", "); break;
		case 43: str.append(M.maxState().getHitPoints()+", "); break;
		case 44: str.append(M.maxState().getMana()+", "); break;
		case 45: str.append(M.maxState().getMovement()+", "); break;
		}
		return str.toString();
	}
	public static String getBasic(MOB M, String val)
	{
		for(int i=0;i<BASICS.length;i++)
		{
			if(val.equalsIgnoreCase(BASICS[i]))
				return getBasic(M,i);
		}
		return "";
	}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		if(!httpReq.getMUD().gameStatusStr().equalsIgnoreCase("OK"))
			return httpReq.getMUD().gameStatusStr();

		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("PLAYER");
		if(last==null) return " @break@";
		if(last.length()>0)
		{
			MOB M=Authenticate.getMOB(last);
			if(M==null) return " @break@";
			
			StringBuffer str=new StringBuffer("");
			for(int i=0;i<MOB.AUTODESC.length;i++)
			{
				if(parms.containsKey(MOB.AUTODESC[i]))
				{
					boolean set=Util.isSet(M.getBitmap(),i);
					if(MOB.AUTOREV[i]) set=!set;
					str.append((set?"ON":"OFF")+",");
				}
			}
			for(int i=0;i<CharStats.TRAITS.length;i++)
			{
				String stat=CharStats.TRAITS[i];
				if(!stat.equalsIgnoreCase("GENDER"))
				{
					if(stat.endsWith(" SAVE"))
						stat=((String)Util.parse(stat).firstElement());
					else
					if(stat.startsWith("SAVE "))
						stat=((String)Util.parse(stat).lastElement());
					CharStats C=M.charStats();
					if(parms.containsKey("BASE"+stat))
					{
						stat=stat.substring(4);
						C=M.baseCharStats();
					}
					if(parms.containsKey(stat))
					{
						if(i>CharStats.NUM_BASE_STATS)
							str.append(M.charStats().getSave(i)+", ");
						else
							str.append(M.charStats().getStat(i)+", ");
					}
				}
			}
			for(int i=0;i<BASICS.length;i++)
			{
				if(parms.containsKey(BASICS[i]))
					str.append(getBasic(M,i));
			}
			String strstr=str.toString();
			if(strstr.endsWith(", "))
				strstr=strstr.substring(0,strstr.length()-2);
			return strstr;
		}
		return "";
	}
}
