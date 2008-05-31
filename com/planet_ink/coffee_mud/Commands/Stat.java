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
   Copyright 2000-2008 Bo Zimmerman

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

	public MOB getTarget(MOB mob, String targetName, boolean quiet)
	{
		MOB target=null;
		if(targetName.length()>0)
		{
			target=mob.location().fetchInhabitant(targetName);
			if(target==null)
			{
				Environmental t=mob.location().fetchFromRoomFavorItems(null,targetName,Item.WORNREQ_UNWORNONLY);
				if((t!=null)&&(!(t instanceof MOB)))
				{
					if(!quiet)
						mob.tell(mob,t,null,"You can't do that to <T-NAMESELF>.");
					return null;
				}
			}
		}
		return target;
	}

	public boolean showTableStats(MOB mob, int days, int scale, String rest)
	{
		Calendar ENDQ=Calendar.getInstance();
		ENDQ.add(Calendar.DATE,-days);
		ENDQ.set(Calendar.HOUR_OF_DAY,23);
		ENDQ.set(Calendar.MINUTE,59);
		ENDQ.set(Calendar.SECOND,59);
		ENDQ.set(Calendar.MILLISECOND,999);
		CMLib.coffeeTables().update();
		Vector V=CMLib.database().DBReadStats(ENDQ.getTimeInMillis()-1);
		if(V.size()==0){ mob.tell("No Stats?!"); return false;}
		StringBuffer table=new StringBuffer("");
        boolean skillUse=false;
        if(rest.toUpperCase().trim().startsWith("SKILLUSE"))
        {
            skillUse=true;
            rest=rest.substring("SKILLUSE".length()).trim();
        }
		table.append("^xStatistics since "+CMLib.time().date2String(ENDQ.getTimeInMillis())+":^.^N\n\r\n\r");
        if(skillUse)
            table.append(CMStrings.padRight("Skill",25)+CMStrings.padRight("Uses",10)+CMStrings.padRight("Skill",25)+CMStrings.padRight("Uses",10)+"\n\r");
        else
    		table.append(CMStrings.padRight("Date",25)
    					 +CMStrings.padRight("CONs",5)
    					 +CMStrings.padRight("HIGH",5)
    					 +CMStrings.padRight("ONLN",5)
    					 +CMStrings.padRight("AVGM",5)
    					 +CMStrings.padRight("NEWB",5)
    					 +CMStrings.padRight("DTHs",5)
    					 +CMStrings.padRight("PKDs",5)
    					 +CMStrings.padRight("CLAS",5)
    					 +CMStrings.padRight("PURG",5)
    					 +CMStrings.padRight("MARR",5)+"\n\r");
		table.append(CMStrings.repeat("-",75)+"\n\r");
		Calendar C=Calendar.getInstance();
		C.set(Calendar.HOUR_OF_DAY,23);
		C.set(Calendar.MINUTE,59);
		C.set(Calendar.SECOND,59);
		C.set(Calendar.MILLISECOND,999);
		long curTime=C.getTimeInMillis();
		String code="*";
		if(rest.length()>0) code=""+rest.toUpperCase().charAt(0);
		long lastCur=System.currentTimeMillis();
        if(skillUse)
        {
            CharClass CharC=CMClass.getCharClass(rest);
            Vector allSkills=new Vector();
            for(Enumeration e=CMClass.abilities();e.hasMoreElements();)
                allSkills.addElement(e.nextElement());
            long[][] totals=new long[allSkills.size()][CoffeeTableRow.STAT_TOTAL];
            while((V.size()>0)&&(curTime>(ENDQ.getTimeInMillis())))
            {
                lastCur=curTime;
                Calendar C2=Calendar.getInstance();
                C.setTimeInMillis(curTime);
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
                    CoffeeTableRow T=(CoffeeTableRow)V.elementAt(v);
                    if((T.startTime()>curTime)&&(T.endTime()<=lastCur))
                    {
                        set.addElement(T);
                        V.removeElementAt(v);
                    }
                }
                for(int s=0;s<set.size();s++)
                {
                    CoffeeTableRow T=(CoffeeTableRow)set.elementAt(s);
                    for(int x=0;x<allSkills.size();x++)
                        T.totalUp("A"+((Ability)allSkills.elementAt(x)).ID().toUpperCase(),totals[x]);
                }
                if(scale==0) break;
            }
            boolean cr=false;
            for(int x=0;x<allSkills.size();x++)
            {
                Ability A=(Ability)allSkills.elementAt(x);
                if((CharC==null)||(CMLib.ableMapper().getQualifyingLevel(CharC.ID(),true,A.ID())<0))
                    continue;
                if(totals[x][CoffeeTableRow.STAT_SKILLUSE]>0)
                {
                    table.append(CMStrings.padRight(""+A.ID(),25)
                            +CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_SKILLUSE],10));
                    if(cr) table.append("\n\r");
                    cr=!cr;
                }
                x++;
                if(x<allSkills.size())
                {
                    A=(Ability)allSkills.elementAt(x);
                    if(totals[x][CoffeeTableRow.STAT_SKILLUSE]>0)
                    {
                        
                        table.append(CMStrings.padRight(""+A.ID(),25)
                                +CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_SKILLUSE],10));
                        if(cr) table.append("\n\r");
                        cr=!cr;
                    }
                }
            }
            if(cr)table.append("\n\r");
        }
        else
		while((V.size()>0)&&(curTime>(ENDQ.getTimeInMillis())))
		{
			lastCur=curTime;
            Calendar C2=Calendar.getInstance();
            C2.setTimeInMillis(curTime);
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
				CoffeeTableRow T=(CoffeeTableRow)V.elementAt(v);
				if((T.startTime()>curTime)&&(T.endTime()<=lastCur))
				{
					set.addElement(T);
					V.removeElementAt(v);
				}
			}
			long[] totals=new long[CoffeeTableRow.STAT_TOTAL];
			long highestOnline=0;
			long numberOnlineTotal=0;
			long numberOnlineCounter=0;
			for(int s=0;s<set.size();s++)
			{
				CoffeeTableRow T=(CoffeeTableRow)set.elementAt(s);
				T.totalUp(code,totals);
				if(T.highestOnline()>highestOnline) highestOnline=T.highestOnline();
				numberOnlineTotal+=T.numberOnlineTotal();
				numberOnlineCounter+=T.numberOnlineCounter();
			}
			totals[CoffeeTableRow.STAT_TICKSONLINE]=(totals[CoffeeTableRow.STAT_TICKSONLINE]*Tickable.TIME_TICK)/scale/(1000*60);
			double avgOnline=(numberOnlineCounter>0)?CMath.div(numberOnlineTotal,numberOnlineCounter):0.0;
			avgOnline=CMath.div(Math.round(avgOnline*10.0),10.0);
			table.append(CMStrings.padRight(CMLib.time().date2DateString(curTime+1)+" - "+CMLib.time().date2DateString(lastCur-1),25)
						 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_LOGINS],5)
						 +CMStrings.centerPreserve(""+highestOnline,5)
						 +CMStrings.centerPreserve(""+avgOnline,5)
						 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_TICKSONLINE],5)
						 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_NEWPLAYERS],5)
						 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_DEATHS],5)
						 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_PKDEATHS],5)
						 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_CLASSCHANGE],5)
						 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_PURGES],5)
						 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_MARRIAGES],5)+"\n\r");
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
			return showTableStats(mob,1,1,CMParms.combine(commands,1));
		else
		if(commands.size()>1)
		{
			String rest=(commands.size()>2)?CMParms.combine(commands,2):"";
			if(s2.equals("DAY")&&(CMath.isNumber(s1)))
				return showTableStats(mob,(CMath.s_int(s1)),1,rest);
			else
			if(s2.equals("DAYS")&&(CMath.isNumber(s1)))
				return showTableStats(mob,(CMath.s_int(s1)),1,rest);
			else
			if(s2.equals("WEEK")&&(CMath.isNumber(s1)))
				return showTableStats(mob,(CMath.s_int(s1)*7),7,rest);
			else
			if(s2.equals("WEEKS")&&(CMath.isNumber(s1)))
				return showTableStats(mob,(CMath.s_int(s1)*7),7,rest);
			else
			if(s2.equals("MONTH")&&(CMath.isNumber(s1)))
				return showTableStats(mob,(CMath.s_int(s1)*30),30,rest);
			else
			if(s2.equals("MONTHS")&&(CMath.isNumber(s1)))
				return showTableStats(mob,(CMath.s_int(s1)*30),30,rest);
			else
			if(s2.equals("YEAR")&&(CMath.isNumber(s1)))
				return showTableStats(mob,(CMath.s_int(s1)*365),365,rest);
			else
			if(s2.equals("YEARS")&&(CMath.isNumber(s1)))
				return showTableStats(mob,(CMath.s_int(s1)*365),365,rest);
		}
		
		int ableTypes=-1;
		if(commands.size()>1)
		{
			String s=((String)commands.elementAt(0)).toUpperCase();
			if("TATTOOS".equals(s)||"TATTOO".equals(s)||"TATT".equals(s))
			{
				ableTypes=-5;
				commands.removeElementAt(0);
			}
			else
			if("QUESTWINS".equals(s)||"QUESTS".equals(s)||"QUEST".equals(s)||"QUESTWIN".equals(s))
			{
				ableTypes=-4;
				commands.removeElementAt(0);
			}
			else
			if("EQUIPMENT".equals(s)||"EQ".equals(s)||"EQUIP".equals(s))
			{
				ableTypes=-2;
				commands.removeElementAt(0);
			}
			else
			if("COMBAT".equals(s))
			{
				ableTypes=-6;
				commands.removeElementAt(0);
			}
            else
            if("SCRIPTS".equals(s))
            {
                ableTypes=-7;
                commands.removeElementAt(0);
            }
			else
			if("INVENTORY".equals(s)||"INVEN".equals(s)||"INV".equals(s))
			{
				ableTypes=-3;
				commands.removeElementAt(0);
			}
			else
			for(int a=0;a<Ability.ACODE_DESCS.length;a++)
			{
				if((Ability.ACODE_DESCS[a]+"S").equals(s)||(Ability.ACODE_DESCS[a]).equals(s))
				{
					ableTypes=a;
					commands.removeElementAt(0);
					break;
				}
			}
		}
		String MOBname=CMParms.combine(commands,0);
		MOB target=getTarget(mob,MOBname,true);
		if((target==null)||(!target.isMonster()))
			target=mob.location().fetchInhabitant(MOBname);
		if((target==null)||(!target.isMonster()))
		{
		    try
		    {
				Enumeration r=CMLib.map().rooms();
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
			target=CMLib.map().getLoadPlayer(MOBname);
		if(target==null)
		{
			mob.tell("You can't stat '"+MOBname+"'  -- he doesn't exist.");
			return false;
		}

		StringBuffer str=new StringBuffer("");
		if(ableTypes>=0)
		{
			Vector V=new Vector();
			int mask=Ability.ALL_ACODES;
			V.addElement(new Integer(ableTypes));
			str=getAbilities(target,V,mask,false,-1);
		}
		else
		if(ableTypes==-2)
			str=CMLib.commands().getEquipment(mob,target);
		else
		if(ableTypes==-3)
			str=CMLib.commands().getInventory(mob,target);
		else
		if(ableTypes==-4)
		{
			str.append("Quests won:");
			for(int q=0;q<CMLib.quests().numQuests();q++)
			{
				Quest Q=CMLib.quests().fetchQuest(q);
				if(Q.wasWinner(target.Name()))
					str.append(" "+Q.name()+",");
			}
			str.deleteCharAt(str.length()-1);
			str.append("\n\r");
		}
        if(ableTypes==-7)
        {
            str.append("Scripts covered:\n\r");
            for(int q=0;q<target.numScripts();q++)
            {
                ScriptingEngine E=target.fetchScript(q);
                str.append("Script #"+q+"\n\r");
                str.append("Quest: "+E.defaultQuestName()+"\n\r");
                str.append("Savable: "+E.isSavable()+"\n\r");
                str.append("Scope: "+E.getVarScope()+"\n\r");
                str.append("Vars: "+E.getScopeValues()+"\n\r");
                str.append("Script: "+E.getScript()+"\n\r");
                str.append("\n\r");
            }
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
		if(ableTypes==-6)
		{
			str.append("Combat summary:\n\r\n\r");
			MOB M=CMClass.getMOB("StdMOB");
			M.setBaseCharStats((CharStats)target.baseCharStats().copyOf());
			M.setBaseEnvStats((EnvStats)target.baseEnvStats().copyOf());
			M.setBaseState((CharState)target.baseState().copyOf());
			recoverMOB(target);
			recoverMOB(M);
			int base=M.adjustedAttackBonus(null);
			str.append("^c"+CMStrings.padRight("Base Attack",40)+": ^W"+base+"\n\r");
			for(int i=0;i<target.inventorySize();i++)
			{
				Item I=target.fetchInventory(i);
				if(I!=null){ recoverMOB(M); testMOB(target,M,I); int diff=M.adjustedAttackBonus(null)-base; reportOnDiffMOB(I,diff,str);}
			}
			recoverMOB(M);
			for(int i=0;i<target.numAllEffects();i++)
			{
				Ability A=target.fetchEffect(i);
				if(A!=null){ recoverMOB(M); testMOB(target,M,A); int diff=M.adjustedAttackBonus(null)-base; reportOnDiffMOB(A,diff,str);}
			}
			recoverMOB(target);
			recoverMOB(M);
			str.append("^W-------------------------\n\r");
			str.append("^C"+CMStrings.padRight("Total",40)+": ^W"+target.adjustedAttackBonus(null)+"\n\r");
			str.append("\n\r");
			base=M.adjustedArmor();
			str.append("^C"+CMStrings.padRight("Base Armor",40)+": ^W"+base+"\n\r");
			for(int i=0;i<target.inventorySize();i++)
			{
				Item I=target.fetchInventory(i);
				if(I!=null){ recoverMOB(M); testMOB(target,M,I); int diff=M.adjustedArmor()-base; reportOnDiffMOB(I,diff,str);}
			}
			recoverMOB(M);
			for(int i=0;i<target.numAllEffects();i++)
			{
				Ability A=target.fetchEffect(i);
				if(A!=null){ recoverMOB(M); testMOB(target,M,A); int diff=M.adjustedArmor()-base; reportOnDiffMOB(A,diff,str);}
			}
			recoverMOB(target);
			recoverMOB(M);
			str.append("^W-------------------------\n\r");
			str.append("^C"+CMStrings.padRight("Total",40)+": ^W"+target.adjustedArmor()+"\n\r");
			str.append("\n\r");
			base=M.adjustedDamage(null,null);
			str.append("^C"+CMStrings.padRight("Base Damage",40)+": ^W"+base+"\n\r");
			for(int i=0;i<target.inventorySize();i++)
			{
				Item I=target.fetchInventory(i);
				if(I!=null){ recoverMOB(M); testMOB(target,M,I); int diff=M.adjustedDamage(null,null)-base; reportOnDiffMOB(I,diff,str);}
			}
			recoverMOB(M);
			for(int i=0;i<target.numAllEffects();i++)
			{
				Ability A=target.fetchEffect(i);
				if(A!=null){ recoverMOB(M); testMOB(target,M,A); int diff=M.adjustedDamage(null,null)-base; reportOnDiffMOB(A,diff,str);}
			}
			recoverMOB(target);
			recoverMOB(M);
			str.append("^W-------------------------\n\r");
			str.append("^C"+CMStrings.padRight("Total",40)+": ^W"+target.adjustedDamage(null,null)+"\n\r");
			str.append("\n\r");
			base=(int)Math.round(M.envStats().speed()*100);
			str.append("^C"+CMStrings.padRight("Base Attacks%",40)+": ^W"+base+"\n\r");
			for(int i=0;i<target.inventorySize();i++)
			{
				Item I=target.fetchInventory(i);
				if(I!=null){ recoverMOB(M); testMOB(target,M,I); int diff=(int)Math.round(M.envStats().speed()*100)-base; reportOnDiffMOB(I,diff,str);}
			}
			recoverMOB(M);
			for(int i=0;i<target.numAllEffects();i++)
			{
				Ability A=target.fetchEffect(i);
				if(A!=null){ recoverMOB(M); testMOB(target,M,A); int diff=(int)Math.round(M.envStats().speed()*100)-base; reportOnDiffMOB(A,diff,str);}
			}
			recoverMOB(target);
			recoverMOB(M);
			str.append("^W-------------------------\n\r");
			str.append("^C"+CMStrings.padRight("Total",40)+": ^W"+(int)Math.round(target.envStats().speed()*100)+"\n\r");
			str.append("\n\r");
			base=M.maxState().getHitPoints();
			str.append("^C"+CMStrings.padRight("Base Hit Points",40)+": ^W"+base+"\n\r");
			for(int i=0;i<target.inventorySize();i++)
			{
				Item I=target.fetchInventory(i);
				if(I!=null){ recoverMOB(M); testMOB(target,M,I); int diff=M.maxState().getHitPoints()-base; reportOnDiffMOB(I,diff,str);}
			}
			recoverMOB(M);
			for(int i=0;i<target.numAllEffects();i++)
			{
				Ability A=target.fetchEffect(i);
				if(A!=null){ recoverMOB(M); testMOB(target,M,A); int diff=M.maxState().getHitPoints()-base; reportOnDiffMOB(A,diff,str);}
			}
			recoverMOB(M);
			str.append("^W-------------------------\n\r");
			str.append("^C"+CMStrings.padRight("Total",40)+": ^W"+target.maxState().getHitPoints()+"\n\r");
			recoverMOB(target);
		}
		else
			str=CMLib.commands().getScore(target);
		if(!mob.isMonster())
			mob.session().wraplessPrintln(str.toString());
		return false;
	}

	public void recoverMOB(MOB M)
	{
		M.recoverCharStats();
		M.recoverEnvStats();
		M.recoverMaxState();
		M.resetToMaxState();
	}
	public void testMOB(MOB target,MOB M, Environmental test)
	{
		test.affectCharStats(target,M.charStats());
		test.affectEnvStats(target,M.envStats());
		test.affectCharState(target,M.maxState());
	}
	public void reportOnDiffMOB(Environmental test, int diff, StringBuffer str)
	{
		if(diff>0)
			str.append("^C"+CMStrings.padRight(test.Name(),40)+": ^W+"+diff+"\n\r");
		else
		if(diff<0)
			str.append("^C"+CMStrings.padRight(test.Name(),40)+": ^W"+diff+"\n\r");
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"STAT");}

	
}
