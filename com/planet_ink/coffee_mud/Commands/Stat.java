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
public class Stat extends BaseAbleLister
{
	public Stat(){}

	private String[] access={"STAT"};
	public String[] getAccessWords(){return access;}

	public MOB getTarget(MOB mob, Vector commands, boolean quiet)
	{
		String targetName=Util.combine(commands,0);
		MOB target=null;
		if(targetName.length()>0)
		{
			target=mob.location().fetchInhabitant(targetName);
			if(target==null)
			{
				Environmental t=mob.location().fetchFromRoomFavorItems(null,targetName,Item.WORN_REQ_UNWORNONLY);
				if((t!=null)&&(!(t instanceof MOB)))
				{
					if(!quiet)
						mob.tell(mob,t,null,"You can't do that to <T-NAMESELF>.");
					return null;
				}
			}
		}

		if(target!=null)
			targetName=target.name();

		if((target==null)||((!Sense.canBeSeenBy(target,mob))&&((!Sense.canBeHeardBy(target,mob))||(!target.isInCombat()))))
		{
			if(!quiet)
			{
				if(targetName.trim().length()==0)
					mob.tell("You don't see them here.");
				else
					mob.tell("You don't see '"+targetName+"' here.");
			}
			return null;
		}

		return target;
	}

	public boolean showTableStats(MOB mob, int days, int scale, String rest)
	{
		IQCalendar ENDQ=new IQCalendar(System.currentTimeMillis());
		ENDQ.add(Calendar.DATE,-days);
		ENDQ.set(Calendar.HOUR_OF_DAY,23);
		ENDQ.set(Calendar.MINUTE,59);
		ENDQ.set(Calendar.SECOND,59);
		ENDQ.set(Calendar.MILLISECOND,999);
		CoffeeTables.update();
		Vector V=CMClass.DBEngine().DBReadStats(ENDQ.getTimeInMillis()-1);
		if(V.size()==0){ mob.tell("No Stats?!"); return false;}
		StringBuffer table=new StringBuffer("");
		table.append("^xStatistics since "+ENDQ.d2String()+":^.^N\n");
		table.append(Util.padRight("Date",25)
					 +Util.padRight("CONs",5)
					 +Util.padRight("HIGH",5)
					 +Util.padRight("ONLN",5)
					 +Util.padRight("AVGM",5)
					 +Util.padRight("NEWB",5)
					 +Util.padRight("DTHs",5)
					 +Util.padRight("PKDs",5)
					 +Util.padRight("CLAS",5)
					 +Util.padRight("PURG",5)
					 +Util.padRight("MARR",5)+"\n\r");
		table.append(Util.repeat("-",75)+"\n\r");
		IQCalendar C=new IQCalendar(System.currentTimeMillis());
		C.set(Calendar.HOUR_OF_DAY,23);
		C.set(Calendar.MINUTE,59);
		C.set(Calendar.SECOND,59);
		C.set(Calendar.MILLISECOND,999);
		long curTime=C.getTimeInMillis();
		String code="*";
		if(rest.length()>0) code=""+rest.toUpperCase().charAt(0);
		long lastCur=System.currentTimeMillis();
		while((V.size()>0)&&(curTime>(ENDQ.getTimeInMillis())))
		{
			lastCur=curTime;
			IQCalendar C2=new IQCalendar(curTime);
			C2.add(Calendar.DATE,-(scale));
			curTime=C2.getTimeInMillis();
			C2.set(Calendar.HOUR_OF_DAY,23);
			C2.set(Calendar.MINUTE,59);
			C2.set(Calendar.SECOND,59);
			C2.set(Calendar.MILLISECOND,999);
			curTime=C2.getTimeInMillis();
			Vector set=new Vector();
			for(int v=V.size()-1;v>=0;v--)
			{
				CoffeeTables T=(CoffeeTables)V.elementAt(v);
				if((T.startTime()>curTime)&&(T.endTime()<=lastCur))
				{
					set.addElement(T);
					V.removeElementAt(v);
				}
			}
			long[] totals=new long[CoffeeTables.STAT_TOTAL];
			long highestOnline=0;
			long numberOnlineTotal=0;
			long numberOnlineCounter=0;
			for(int s=0;s<set.size();s++)
			{
				CoffeeTables T=(CoffeeTables)set.elementAt(s);
				T.totalUp(code,totals);
				if(T.highestOnline()>highestOnline) highestOnline=T.highestOnline();
				numberOnlineTotal+=T.numberOnlineTotal();
				numberOnlineCounter+=T.numberOnlineCounter();
			}
			totals[CoffeeTables.STAT_TICKSONLINE]=(totals[CoffeeTables.STAT_TICKSONLINE]*MudHost.TICK_TIME)/scale/(1000*60);
			double avgOnline=(numberOnlineCounter>0)?Util.div(numberOnlineTotal,numberOnlineCounter):0.0;
			avgOnline=Util.div(Math.round(avgOnline*10.0),10.0);
			table.append(Util.padRight(new IQCalendar(curTime+1).d2DString()+" - "+new IQCalendar(lastCur-1).d2DString(),25)
						 +Util.centerPreserve(""+totals[CoffeeTables.STAT_LOGINS],5)
						 +Util.centerPreserve(""+highestOnline,5)
						 +Util.centerPreserve(""+avgOnline,5)
						 +Util.centerPreserve(""+totals[CoffeeTables.STAT_TICKSONLINE],5)
						 +Util.centerPreserve(""+totals[CoffeeTables.STAT_NEWPLAYERS],5)
						 +Util.centerPreserve(""+totals[CoffeeTables.STAT_DEATHS],5)
						 +Util.centerPreserve(""+totals[CoffeeTables.STAT_PKDEATHS],5)
						 +Util.centerPreserve(""+totals[CoffeeTables.STAT_CLASSCHANGE],5)
						 +Util.centerPreserve(""+totals[CoffeeTables.STAT_PURGES],5)
						 +Util.centerPreserve(""+totals[CoffeeTables.STAT_MARRIAGES],5)+"\n\r");
			if(scale==0) break;
		}
		mob.tell(table.toString());
		return false;
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		if(commands.size()==0) commands.addElement("TODAY");
		String s1=(commands.size()>0)?((String)commands.elementAt(0)).toUpperCase():"";
		String s2=(commands.size()>1)?((String)commands.elementAt(1)).toUpperCase():"";
		if(s1.equalsIgnoreCase("TODAY"))
			return showTableStats(mob,0,1,Util.combine(commands,1));
		else
		if(commands.size()>1)
		{
			String rest=(commands.size()>2)?Util.combine(commands,2):"";
			if(s2.equals("DAY")&&(Util.isNumber(s1)))
				return showTableStats(mob,(Util.s_int(s1)),1,rest);
			else
			if(s2.equals("DAYS")&&(Util.isNumber(s1)))
				return showTableStats(mob,(Util.s_int(s1)),1,rest);
			else
			if(s2.equals("WEEK")&&(Util.isNumber(s1)))
				return showTableStats(mob,(Util.s_int(s1)*7),7,rest);
			else
			if(s2.equals("WEEKS")&&(Util.isNumber(s1)))
				return showTableStats(mob,(Util.s_int(s1)*7),7,rest);
			else
			if(s2.equals("MONTH")&&(Util.isNumber(s1)))
				return showTableStats(mob,(Util.s_int(s1)*30),30,rest);
			else
			if(s2.equals("MONTHS")&&(Util.isNumber(s1)))
				return showTableStats(mob,(Util.s_int(s1)*30),30,rest);
			else
			if(s2.equals("YEAR")&&(Util.isNumber(s1)))
				return showTableStats(mob,(Util.s_int(s1)*365),365,rest);
			else
			if(s2.equals("YEARS")&&(Util.isNumber(s1)))
				return showTableStats(mob,(Util.s_int(s1)*365),365,rest);
		}
		
		int ableTypes=-1;
		if(commands.size()>1)
		{
			String s=((String)commands.elementAt(0)).toUpperCase();
			if("TATTOOS".startsWith(s))
			{
				ableTypes=-5;
				commands.removeElementAt(0);
			}
			else
			if("QUESTWINS".startsWith(s)||"QUESTS".startsWith(s))
			{
				ableTypes=-4;
				commands.removeElementAt(0);
			}
			else
			if("EQUIPMENT".startsWith(s))
			{
				ableTypes=-2;
				commands.removeElementAt(0);
			}
			else
			if("INVENTORY".startsWith(s))
			{
				ableTypes=-3;
				commands.removeElementAt(0);
			}
			else
			for(int a=0;a<Ability.TYPE_DESCS.length;a++)
			{
				if((Ability.TYPE_DESCS[a]+"S").startsWith(s))
				{
					ableTypes=a;
					commands.removeElementAt(0);
					break;
				}
			}
		}
		String MOBname=Util.combine(commands,0);
		MOB target=getTarget(mob,commands,true);
		if((target==null)||((target!=null)&&(!target.isMonster())))
			target=mob.location().fetchInhabitant(MOBname);
		if((target==null)||((target!=null)&&(!target.isMonster())))
		{
		    try
		    {
				Enumeration r=CMMap.rooms();
				for(;r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if(CMSecurity.isAllowed(mob,R,"STAT"))
					{
						MOB mob2=R.fetchInhabitant(MOBname);
						if(mob2!=null)
						{
							target=mob2;
							break;
						}
					}
				}
		    }catch(NoSuchElementException nse){}
		}
		if(target==null)
			target=CMMap.getLoadPlayer(MOBname);
		if(target==null)
		{
			mob.tell("You can't stat '"+MOBname+"'  -- he doesn't exist.");
			return false;
		}

		StringBuffer str=new StringBuffer("");
		if(ableTypes>=0)
		{
			Vector V=new Vector();
			int mask=Ability.ALL_CODES;
			V.addElement(new Integer(ableTypes));
			str=getAbilities(target,V,mask,false,-1);
		}
		else
		if(ableTypes==-2)
			str=CommonMsgs.getEquipment(mob,target);
		else
		if(ableTypes==-3)
			str=CommonMsgs.getInventory(mob,target);
		else
		if(ableTypes==-4)
		{
			str.append("Quests won:");
			for(int q=0;q<Quests.numQuests();q++)
			{
				Quests Q=Quests.fetchQuest(q);
				if(Q.wasWinner(target.Name()))
					str.append(" "+Q.name()+",");
			}
			str.deleteCharAt(str.length()-1);
			str.append("\n\r");
		}
		else
		if(ableTypes==-5)
		{
			str.append("Tattoos:");
			for(int q=0;q<target.numTattoos();q++)
				str.append(" "+target.fetchTattoo(q)+",");
			str.deleteCharAt(str.length()-1);
			str.append("\n\r");
		}
		else
			str=CommonMsgs.getScore(target);
		if(!mob.isMonster())
			mob.session().wraplessPrintln(str.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"STAT");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
