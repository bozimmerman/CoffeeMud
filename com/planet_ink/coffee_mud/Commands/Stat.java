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
public class Stat  extends Skills
{
	public Stat(){}

	private final String[] access=_i(new String[]{"STAT"});
	@Override public String[] getAccessWords(){return access;}

	public static final int ABLETYPE_EQUIPMENT=-2;
	public static final int ABLETYPE_INVENTORY=-3;
	public static final int ABLETYPE_QUESTWINS=-4;
	public static final int ABLETYPE_TATTOOS=-5;
	public static final int ABLETYPE_COMBAT=-6;
	public static final int ABLETYPE_SCRIPTS=-7;
	public static final int ABLETYPE_TITLES=-8;
	public static final int ABLETYPE_ROOMSEXPLORED=-9;
	public static final int ABLETYPE_AREASEXPLORED=-10;
	public static final int ABLETYPE_WORLDEXPLORED=-11;
	public static final int ABLETYPE_FACTIONS=-12;

	public static final String[][] ABLETYPE_DESCS={
		{"EQUIPMENT","EQ","EQUIP"},
		{"INVENTORY","INVEN","INV"},
		{"QUESTWINS","QUESTS","QUEST","QUESTWIN"},
		{"TATTOOS","TATTOO","TATT"},
		{"COMBAT"},
		{"SCRIPTS"},
		{"TITLES","TITLE"},
		{"ROOMSEXPLORED"},
		{"AREASEXPLORED"},
		{"WORLDEXPLORED"},
		{"FACTIONS","FACTION"},
	};

	public MOB getTarget(MOB mob, String targetName, boolean quiet)
	{
		MOB target=null;
		if(targetName.length()>0)
		{
			target=mob.location().fetchInhabitant(targetName);
			if(target==null)
			{
				final Environmental t=mob.location().fetchFromRoomFavorItems(null,targetName);
				if((t!=null)&&(!(t instanceof MOB)))
				{
					if(!quiet)
						mob.tell(mob,t,null,_("You can't do that to <T-NAMESELF>."));
					return null;
				}
			}
		}
		return target;
	}

	public boolean showTableStats(MOB mob, int days, int scale, String rest)
	{
		final Calendar ENDQ=Calendar.getInstance();
		ENDQ.add(Calendar.DATE,-days);
		ENDQ.set(Calendar.HOUR_OF_DAY,23);
		ENDQ.set(Calendar.MINUTE,59);
		ENDQ.set(Calendar.SECOND,59);
		ENDQ.set(Calendar.MILLISECOND,999);
		CMLib.coffeeTables().update();
		final List<CoffeeTableRow> V=CMLib.database().DBReadStats(ENDQ.getTimeInMillis()-1);
		if(V.size()==0){ mob.tell(_("No Stats?!")); return false;}
		final StringBuffer table=new StringBuffer("");
		boolean skillUse=false;
		boolean questStats=false;
		if(rest.toUpperCase().trim().startsWith("SKILLUSE"))
		{
			skillUse=true;
			final int x=rest.indexOf(' ');
			if(x>0) rest=rest.substring(x+1).trim();
			else rest="";
		}
		if(rest.toUpperCase().trim().startsWith("QUEST"))
		{
			questStats=true;
			final int x=rest.indexOf(' ');
			if(x>0) rest=rest.substring(x+1).trim();
			else rest="";
		}
		table.append("^xStatistics since "+CMLib.time().date2String(ENDQ.getTimeInMillis())+":^.^N\n\r\n\r");
		if(skillUse)
			table.append(CMStrings.padRight(_("Skill"),25)+CMStrings.padRight(_("Uses"),10)+CMStrings.padRight(_("Skill"),25)+CMStrings.padRight(_("Uses"),10)+"\n\r");
		else
		if(questStats)
			table.append(CMStrings.padRight(_("Quest"),30)
	   					+CMStrings.padRight(_("STRT"),5)
						+CMStrings.padRight(_("TSRT"),5)
						+CMStrings.padRight(_("FLST"),5)
						+CMStrings.padRight(_("ACPT"),5)
						+CMStrings.padRight(_("WINS"),5)
						+CMStrings.padRight(_("FAIL"),5)
						+CMStrings.padRight(_("DROP"),5)
						+CMStrings.padRight(_("TSTP"),5)
						+CMStrings.padRight(_("STOP"),5)
						+"\n\r");
		else
			table.append(CMStrings.padRight(_("Date"),25)
						 +CMStrings.padRight(_("CONs"),5)
						 +CMStrings.padRight(_("HIGH"),5)
						 +CMStrings.padRight(_("ONLN"),5)
						 +CMStrings.padRight(_("AVGM"),5)
						 +CMStrings.padRight(_("NEWB"),5)
						 +CMStrings.padRight(_("DTHs"),5)
						 +CMStrings.padRight(_("PKDs"),5)
						 +CMStrings.padRight(_("CLAS"),5)
						 +CMStrings.padRight(_("PURG"),5)
						 +CMStrings.padRight(_("MARR"),5)+"\n\r");
		table.append(CMStrings.repeat("-",75)+"\n\r");
		final Calendar C=Calendar.getInstance();
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
			final CharClass CharC=CMClass.getCharClass(rest);
			final Vector allSkills=new Vector();
			for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
				allSkills.addElement(e.nextElement());
			final long[][] totals=new long[allSkills.size()][CoffeeTableRow.STAT_TOTAL];
			while((V.size()>0)&&(curTime>(ENDQ.getTimeInMillis())))
			{
				lastCur=curTime;
				final Calendar C2=Calendar.getInstance();
				C.setTimeInMillis(curTime);
				C2.add(Calendar.DATE,-(scale));
				curTime=C2.getTimeInMillis();
				C2.set(Calendar.HOUR_OF_DAY,23);
				C2.set(Calendar.MINUTE,59);
				C2.set(Calendar.SECOND,59);
				C2.set(Calendar.MILLISECOND,999);
				curTime=C2.getTimeInMillis();
				final Vector set=new Vector();
				for(int v=V.size()-1;v>=0;v--)
				{
					final CoffeeTableRow T=V.get(v);
					if((T.startTime()>curTime)&&(T.endTime()<=lastCur))
					{
						set.addElement(T);
						V.remove(v);
					}
				}
				for(int s=0;s<set.size();s++)
				{
					final CoffeeTableRow T=(CoffeeTableRow)set.elementAt(s);
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
		if(questStats)
		{
			final long[][] totals=new long[CMLib.quests().numQuests()][CoffeeTableRow.STAT_TOTAL];
			while((V.size()>0)&&(curTime>(ENDQ.getTimeInMillis())))
			{
				lastCur=curTime;
				final Calendar C2=Calendar.getInstance();
				C.setTimeInMillis(curTime);
				C2.add(Calendar.DATE,-(scale));
				curTime=C2.getTimeInMillis();
				C2.set(Calendar.HOUR_OF_DAY,23);
				C2.set(Calendar.MINUTE,59);
				C2.set(Calendar.SECOND,59);
				C2.set(Calendar.MILLISECOND,999);
				curTime=C2.getTimeInMillis();
				final Vector set=new Vector();
				for(int v=V.size()-1;v>=0;v--)
				{
					final CoffeeTableRow T=V.get(v);
					if((T.startTime()>curTime)&&(T.endTime()<=lastCur))
					{
						set.addElement(T);
						V.remove(v);
					}
				}
				if(set.size()==0){ set.addAll(V); V.clear();}
				for(int s=0;s<set.size();s++)
				{
					final CoffeeTableRow T=(CoffeeTableRow)set.elementAt(s);
					for(int x=0;x<CMLib.quests().numQuests();x++)
						T.totalUp("U"+T.tagFix(CMLib.quests().fetchQuest(x).name()),totals[x]);
				}
				if(scale==0) break;
			}
			for(int x=0;x<CMLib.quests().numQuests();x++)
			{
				final Quest Q=CMLib.quests().fetchQuest(x);
				table.append(
						 CMStrings.padRight(Q.name(),30)
						+CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_QUESTSTARTATTEMPT],5)
						+CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_QUESTTIMESTART],5)
						+CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_QUESTFAILEDSTART],5)
						+CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_QUESTACCEPTED],5)
						+CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_QUESTSUCCESS],5)
						+CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_QUESTFAILED],5)
						+CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_QUESTDROPPED],5)
						+CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_QUESTTIMESTOP],5)
						+CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_QUESTSTOP],5));
				table.append("\n\r");
			}
			table.append("\n\r");
		}
		else
		while((V.size()>0)&&(curTime>(ENDQ.getTimeInMillis())))
		{
			lastCur=curTime;
			final Calendar C2=Calendar.getInstance();
			C2.setTimeInMillis(curTime);
			C2.add(Calendar.DATE,-(scale));
			curTime=C2.getTimeInMillis();
			C2.set(Calendar.HOUR_OF_DAY,23);
			C2.set(Calendar.MINUTE,59);
			C2.set(Calendar.SECOND,59);
			C2.set(Calendar.MILLISECOND,999);
			curTime=C2.getTimeInMillis();
			final Vector set=new Vector();
			for(int v=V.size()-1;v>=0;v--)
			{
				final CoffeeTableRow T=V.get(v);
				if((T.startTime()>curTime)&&(T.endTime()<=lastCur))
				{
					set.addElement(T);
					V.remove(v);
				}
			}
			final long[] totals=new long[CoffeeTableRow.STAT_TOTAL];
			long highestOnline=0;
			long numberOnlineTotal=0;
			long numberOnlineCounter=0;
			for(int s=0;s<set.size();s++)
			{
				final CoffeeTableRow T=(CoffeeTableRow)set.elementAt(s);
				T.totalUp(code,totals);
				if(T.highestOnline()>highestOnline) highestOnline=T.highestOnline();
				numberOnlineTotal+=T.numberOnlineTotal();
				numberOnlineCounter+=T.numberOnlineCounter();
			}
			totals[CoffeeTableRow.STAT_TICKSONLINE]=(totals[CoffeeTableRow.STAT_TICKSONLINE]*CMProps.getTickMillis())/scale/(1000*60);
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

	public int averageDamage(MOB M)
	{
		long total=0;
		for(int i=0;i<1000;i++)
			total+=CMLib.combat().adjustedDamage(M,null,null,0,true);
		return (int)(total / 1000);
	}

	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		if((commands.size()>0)
		&&(commands.firstElement() instanceof String)
		&&((String)commands.firstElement()).equals("?"))
		{
			final StringBuilder msg = new StringBuilder("STAT allows the following options: \n\r");
			msg.append(_("[MOB/PLAYER NAME], [NUMBER] [DAYS/WEEKS/MONTHS], "));
			for (final String[] element : ABLETYPE_DESCS)
				msg.append(element[0]+", ");
			msg.append(CMParms.toStringList(Ability.ACODE_DESCS));
			mob.tell(msg.toString());
			return false;
		}
		if(commands.size()==0) commands.addElement("TODAY");
		final String s1=(commands.size()>0)?((String)commands.elementAt(0)).toUpperCase():"";
		final String s2=(commands.size()>1)?((String)commands.elementAt(1)).toUpperCase():"";
		if(s1.equalsIgnoreCase("TODAY"))
			return showTableStats(mob,1,1,CMParms.combine(commands,1));
		else
		if(commands.size()>1)
		{
			final String rest=(commands.size()>2)?CMParms.combine(commands,2):"";
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
			final String s=((String)commands.elementAt(0)).toUpperCase();
			for(int i=0;i<ABLETYPE_DESCS.length;i++)
				for(int is=0;is<ABLETYPE_DESCS[i].length;is++)
					if(s.equals(ABLETYPE_DESCS[i][is]))
					{
						ableTypes=-2 -i;
						commands.removeElementAt(0);
						break;
					}
			if(ableTypes==-1)
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
		final String MOBname=CMParms.combine(commands,0);
		MOB target=getTarget(mob,MOBname,true);
		if((target==null)||(!target.isMonster()))
			target=mob.location().fetchInhabitant(MOBname);
		if((target==null)||(!target.isMonster()))
		{
			try
			{
				final List<MOB> inhabs=CMLib.map().findInhabitants(CMLib.map().rooms(), mob,MOBname,100);
				for(final MOB mob2 : inhabs)
				{
					final Room R=mob2.location();
					if(CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.STAT))
					{
						target=mob2;
						break;
					}
				}
			}catch(final NoSuchElementException nse){}
		}
		if(target==null)
			target=CMLib.players().getLoadPlayer(MOBname);
		if(target==null)
		{
			mob.tell(_("You can't stat '@x1'  -- he doesn't exist.",MOBname));
			return false;
		}
		if(((target.isMonster())&&(!CMSecurity.isAllowed(mob, target.location(), CMSecurity.SecFlag.CMDMOBS)))
		||((!target.isMonster())&&(!CMSecurity.isAllowed(mob, target.location(), CMSecurity.SecFlag.CMDPLAYERS))))
		{
			mob.tell(_("You aren't allowed to stat '@x1'.",target.Name()));
			return false;
		}

		StringBuilder str=new StringBuilder("");
		if(ableTypes>=0)
		{
			final Vector V=new Vector();
			final int mask=Ability.ALL_ACODES;
			V.addElement(Integer.valueOf(ableTypes));
			str=getAbilities(mob,target,V,mask,false,-1);
		}
		else
		if(ableTypes==ABLETYPE_EQUIPMENT)
			str=CMLib.commands().getEquipment(mob,target);
		else
		if(ableTypes==ABLETYPE_INVENTORY)
			str=CMLib.commands().getInventory(mob,target);
		else
		if(ableTypes==ABLETYPE_QUESTWINS)
		{
			str.append(_("Quests won:"));
			final StringBuffer won=new StringBuffer("");
			for(int q=0;q<CMLib.quests().numQuests();q++)
			{
				final Quest Q=CMLib.quests().fetchQuest(q);
				if(Q.wasWinner(target.Name()))
					won.append(" "+Q.name()+",");
			}
			if(won.length()==0)
				won.append(" None!");
			won.deleteCharAt(won.length()-1);
			str.append(won);
			str.append("\n\r");
		}
		else
		if(ableTypes==ABLETYPE_TITLES)
		{
			str.append(_("Titles:"));
			final StringBuffer ttl=new StringBuffer("");
			if(target.playerStats()!=null)
				for(int t=0;t<target.playerStats().getTitles().size();t++)
				{
					final String title = target.playerStats().getTitles().get(t);
					ttl.append(" "+title+",");
				}
			if(ttl.length()==0)
				ttl.append(" None!");
			ttl.deleteCharAt(ttl.length()-1);
			str.append(ttl);
			str.append("\n\r");
		}
		else
		if(ableTypes==ABLETYPE_SCRIPTS)
		{
			str.append(_("Scripts covered:\n\r"));
			int q=1;
			for(final Enumeration<ScriptingEngine> e=target.scripts();e.hasMoreElements();q++)
			{
				final ScriptingEngine SE=e.nextElement();
				str.append(_("Script #@x1\n\r",""+q));
				str.append(_("Quest: @x1\n\r",SE.defaultQuestName()));
				str.append(_("Savable: @x1\n\r",""+SE.isSavable()));
				str.append(_("Scope: @x1\n\r",SE.getVarScope()));
				str.append(_("Vars: @x1\n\r",SE.getLocalVarXML()));
				str.append(_("Script: @x1\n\r",SE.getScript()));
				str.append("\n\r");
			}
			str.append("\n\r");
		}
		else
		if(ableTypes==ABLETYPE_TATTOOS)
		{
			str.append(_("Tattoos:"));
			for(final Enumeration<MOB.Tattoo> e=target.tattoos();e.hasMoreElements();)
				str.append(" "+e.nextElement().tattooName+",");
			str.deleteCharAt(str.length()-1);
			str.append("\n\r");
		}
		else
		if(ableTypes==ABLETYPE_FACTIONS)
		{
			str.append(_("Factions:\n\r"));
			for(final Enumeration<String> f=target.fetchFactions();f.hasMoreElements();)
			{
				final Faction F=CMLib.factions().getFaction(f.nextElement());
				if(F!=null)
					str.append("^W[^H"+F.name()+"^N("+F.factionID()+"): "+target.fetchFaction(F.factionID())+"^W]^N, ");
			}
			str.append("\n\r");
		}
		else
		if(ableTypes==ABLETYPE_WORLDEXPLORED)
		{
			if(target.playerStats()!=null)
				str.append(_("@x1 has explored @x2% of the world.\n\r",target.name(),""+mob.playerStats().percentVisited(mob,null)));
			else
				str.append(_("Exploration data is not kept on mobs.\n\r"));
		}
		else
		if(ableTypes==ABLETYPE_AREASEXPLORED)
		{
			if(target.playerStats()!=null)
			{
				for(final Enumeration e=CMLib.map().areas();e.hasMoreElements();)
				{
					final Area A=(Area)e.nextElement();
					final int pct=mob.playerStats().percentVisited(target, A);
					if(pct>0) str.append("^H"+A.name()+"^N: "+pct+"%, ");
				}
				str=new StringBuilder(str.toString().substring(0,str.toString().length()-2)+"\n\r");
			}
			else
				str.append(_("Exploration data is not kept on mobs.\n\r"));
		}
		else
		if(ableTypes==ABLETYPE_ROOMSEXPLORED)
		{
			if(target.playerStats()!=null)
			{
				for(final Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
				{
					final Room R=(Room)e.nextElement();
					if((R.roomID().length()>0)&&(mob.playerStats().hasVisited(R)))
						str.append("^H"+R.roomID()+"^N, ");
				}
				str=new StringBuilder(str.toString().substring(0,str.toString().length()-2)+"\n\r");
			}
			else
				str.append(_("Exploration data is not kept on mobs.\n\r"));
		}
		else
		if(ableTypes==ABLETYPE_COMBAT)
		{
			str.append(_("Combat summary:\n\r\n\r"));
			final MOB M=CMClass.getMOB("StdMOB");
			M.setBaseCharStats((CharStats)target.baseCharStats().copyOf());
			M.setBasePhyStats((PhyStats)target.basePhyStats().copyOf());
			M.setBaseState((CharState)target.baseState().copyOf());
			recoverMOB(target);
			recoverMOB(M);
			int base=CMLib.combat().adjustedAttackBonus(M,null);
			str.append("^c"+CMStrings.padRight(_("Base Attack"),40)+": ^W"+base+"\n\r");
			for(int i=0;i<target.numItems();i++)
			{
				final Item I=target.getItem(i);
				if((I!=null)&&(!I.amWearingAt(Wearable.IN_INVENTORY))){ recoverMOB(M); base=CMLib.combat().adjustedAttackBonus(M,null); testMOB(target,M,I); final int diff=CMLib.combat().adjustedAttackBonus(M,null)-base; reportOnDiffMOB(I,diff,str);}
			}
			recoverMOB(M);
			for(final Enumeration<Ability> a=target.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if(A!=null){ recoverMOB(M); base=CMLib.combat().adjustedAttackBonus(M,null); testMOB(target,M,A); final int diff=CMLib.combat().adjustedAttackBonus(M,null)-base; reportOnDiffMOB(A,diff,str);}
			}
			recoverMOB(target);
			recoverMOB(M);
			str.append("^W-------------------------\n\r");
			str.append("^C"+CMStrings.padRight(_("Total"),40)+": ^W"+CMLib.combat().adjustedAttackBonus(target,null)+"\n\r");
			str.append("\n\r");
			base=CMLib.combat().adjustedArmor(M);
			str.append("^C"+CMStrings.padRight(_("Base Armor"),40)+": ^W"+base+"\n\r");
			for(int i=0;i<target.numItems();i++)
			{
				final Item I=target.getItem(i);
				if(I!=null){ recoverMOB(M); base=CMLib.combat().adjustedArmor(M); testMOB(target,M,I); final int diff=CMLib.combat().adjustedArmor(M)-base; reportOnDiffMOB(I,diff,str);}
			}
			recoverMOB(M);
			for(final Enumeration<Ability> a=target.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if(A!=null){ recoverMOB(M); base=CMLib.combat().adjustedArmor(M); testMOB(target,M,A); final int diff=CMLib.combat().adjustedArmor(M)-base; reportOnDiffMOB(A,diff,str);}
			}
			recoverMOB(target);
			recoverMOB(M);
			str.append("^W-------------------------\n\r");
			str.append("^C"+CMStrings.padRight(_("Total"),40)+": ^W"+CMLib.combat().adjustedArmor(target)+"\n\r");
			str.append("\n\r");
			base=CMLib.combat().adjustedDamage(M,null,null,0,false);
			str.append("^C"+CMStrings.padRight(_("Base Damage"),40)+": ^W"+base+"\n\r");
			for(int i=0;i<target.numItems();i++)
			{
				final Item I=target.getItem(i);
				if(I!=null){ recoverMOB(M); base=averageDamage(M); testMOB(target,M,I); final int diff=averageDamage(M)-base; reportOnDiffMOB(I,diff,str);}
			}
			recoverMOB(M);
			for(final Enumeration<Ability> a=target.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if(A!=null){ recoverMOB(M); base=averageDamage(M); testMOB(target,M,A); final int diff=averageDamage(M)-base; reportOnDiffMOB(A,diff,str);}
			}
			recoverMOB(target);
			recoverMOB(M);
			str.append("^W-------------------------\n\r");
			str.append("^C"+CMStrings.padRight(_("Total"),40)+": ^W"+CMLib.combat().adjustedDamage(target,null,null,0,false)+"\n\r");
			str.append("\n\r");
			base=(int)Math.round(M.phyStats().speed()*100);
			str.append("^C"+CMStrings.padRight(_("Base Attacks%"),40)+": ^W"+base+"\n\r");
			for(int i=0;i<target.numItems();i++)
			{
				final Item I=target.getItem(i);
				if(I!=null){ recoverMOB(M); base=(int)Math.round(M.phyStats().speed()*100); testMOB(target,M,I); final int diff=(int)Math.round(M.phyStats().speed()*100)-base; reportOnDiffMOB(I,diff,str);}
			}
			recoverMOB(M);
			for(final Enumeration<Ability> a=target.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if(A!=null){ recoverMOB(M); base=(int)Math.round(M.phyStats().speed()*100); testMOB(target,M,A); final int diff=(int)Math.round(M.phyStats().speed()*100)-base; reportOnDiffMOB(A,diff,str);}
			}
			recoverMOB(target);
			recoverMOB(M);
			str.append("^W-------------------------\n\r");
			str.append("^C"+CMStrings.padRight(_("Total"),40)+": ^W"+(int)Math.round(target.phyStats().speed()*100)+"\n\r");
			str.append("\n\r");
			base=M.maxState().getHitPoints();
			str.append("^C"+CMStrings.padRight(_("Base Hit Points"),40)+": ^W"+base+"\n\r");
			for(int i=0;i<target.numItems();i++)
			{
				final Item I=target.getItem(i);
				if(I!=null){ recoverMOB(M); base=M.maxState().getHitPoints(); testMOB(target,M,I); final int diff=M.maxState().getHitPoints()-base; reportOnDiffMOB(I,diff,str);}
			}
			recoverMOB(M);
			for(int i=0;i<target.numAllEffects();i++)
			{
				final Ability A=target.fetchEffect(i);
				if(A!=null){ recoverMOB(M); base=M.maxState().getHitPoints(); testMOB(target,M,A); final int diff=M.maxState().getHitPoints()-base; reportOnDiffMOB(A,diff,str);}
			}
			recoverMOB(M);
			str.append("^W-------------------------\n\r");
			str.append("^C"+CMStrings.padRight(_("Total"),40)+": ^W"+target.maxState().getHitPoints()+"\n\r");
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
		M.recoverPhyStats();
		M.recoverMaxState();
		M.resetToMaxState();
	}
	public void testMOB(MOB target,MOB M, Environmental test)
	{
		test.affectCharStats(target,M.charStats());
		test.affectPhyStats(target,M.phyStats());
		test.affectCharState(target,M.maxState());
	}
	public void reportOnDiffMOB(Environmental test, int diff, StringBuilder str)
	{
		if(diff>0)
			str.append("^C"+CMStrings.padRight(test.Name(),40)+": ^W+"+diff+"\n\r");
		else
		if(diff<0)
			str.append("^C"+CMStrings.padRight(test.Name(),40)+": ^W"+diff+"\n\r");
	}

	@Override public boolean canBeOrdered(){return true;}
	@Override public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.STAT);}


}
