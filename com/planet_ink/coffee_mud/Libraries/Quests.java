package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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

import java.io.IOException;
import java.util.*;

import org.mozilla.javascript.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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
public class Quests extends StdLibrary implements QuestManager
{
	@Override
	public String ID()
	{
		return "Quests";
	}

	protected String holidayFilename="quests/holidays/holidays.quest";
	protected String holidayDefinition="LOAD="+holidayFilename;
	protected SVector<Quest> quests=new SVector<Quest>();

	@Override
	public Quest objectInUse(Environmental E)
	{
		if(E==null)
			return null;
		for(int q=0;q<numQuests();q++)
		{
			final Quest Q=fetchQuest(q);
			if(Q!=null)
			{
				if(Q.isObjectInUse(E))
					return Q;
			}
		}
		return null;
	}

	@Override
	public int numQuests()
	{
		return quests.size();
	}

	@Override
	public Quest fetchQuest(int i)
	{
		try
		{
			return quests.elementAt(i);
		}
		catch (final Exception e)
		{
		}
		return null;
	}

	@Override
	public Quest fetchQuest(String qname)
	{
		for(int i=0;i<numQuests();i++)
		{
			final Quest Q=fetchQuest(i);
			if((Q!=null)&&(Q.name().equalsIgnoreCase(qname)))
				return Q;
		}
		return null;
	}

	@Override
	public Quest findQuest(String qname)
	{
		Quest Q=fetchQuest(qname);
		if(Q!=null)
			return Q;
		for(int i=0;i<numQuests();i++)
		{
			Q=fetchQuest(i);
			if((Q.displayName().trim().length()>0)
			&&(Q.displayName().equalsIgnoreCase(qname)))
				return Q;
		}
		for(int i=0;i<numQuests();i++)
		{
			Q=fetchQuest(i);
			if((Q.displayName().trim().length()>0)
			&&(CMLib.english().containsString(Q.displayName(),qname)))
				return Q;
		}
		return null;
	}

	@Override
	public void addQuest(Quest Q)
	{
		if(!quests.contains(Q))
		{
			quests.addElement(Q);
			Q.autostartup();
		}
	}

	@Override
	public boolean shutdown()
	{
		for(int i=numQuests();i>=0;i--)
		{
			final Quest Q=fetchQuest(i);
			delQuest(Q);
		}
		quests.clear();
		return true;
	}

	@Override
	public void delQuest(Quest Q)
	{
		if(quests.contains(Q))
		{
			Q.stopQuest();
			CMLib.threads().deleteTick(Q,Tickable.TICKID_QUEST);
			Q.internalQuestDelete();
			quests.removeElement(Q);
		}
	}

	@Override
	public void save()
	{
		CMLib.database().DBUpdateQuests(quests);
	}

	@Override
	public Enumeration<Quest> enumQuests()
	{
		return quests.elements();
	}

	@Override
	public Object getHolidayFile()
	{
		Quest Q=fetchQuest("holidays");
		if((Q==null)
		||(!Q.script().toUpperCase().trim().equalsIgnoreCase(holidayDefinition)))
		{
			Q=null;
			final CMFile lF=new CMFile("//"+Resources.makeFileResourceName(holidayFilename),null);
			final CMFile vF=new CMFile("::"+Resources.makeFileResourceName(holidayFilename),null);
			if((lF.exists())&&(!vF.exists())&&(lF.canRead())&&(vF.canWrite()))
			{
				final byte[] O=lF.raw();
				vF.saveRaw(O);
			}
			Q=(Quest)CMClass.getCommon("DefaultQuest");
			Q.setScript(holidayDefinition,true);
			addQuest(Q);
			CMLib.database().DBUpdateQuest(Q);
			Q=fetchQuest("holidays");
			if(Q==null)
				return "A quest named 'holidays', with the script definition '"+holidayDefinition+"' has not been created.  Enter the following to create this quest:\n\r"
					  +"CREATE QUEST "+holidayDefinition+"\n\r"
					  +"SAVE QUESTS";
		}
		final CMFile F=new CMFile(Resources.makeFileResourceName(holidayFilename),null);
		if((!F.exists())||(!F.canRead())||(!F.canWrite()))
		{
			return "The file '"+Resources.makeFileResourceName(holidayFilename)+"' does not exist, and is required for this feature.";
		}
		final List<String> V=Resources.getFileLineVector(F.text());
		final List<String> steps=parseQuestSteps(V,0,true);
		return steps;
	}

	@Override
	@SuppressWarnings("unchecked")
	public String listHolidays(Area A, String otherParms)
	{
		final Object resp=getHolidayFile();
		if(resp instanceof String)
			return (String)resp;
		List<String> steps=null;
		if(resp instanceof List)
			steps=(List<String>)resp;
		else
			return "Unknown error.";
		String areaName=A.Name().toUpperCase().trim();
		if(otherParms.equalsIgnoreCase("ALL"))
			areaName=null;
		final StringBuffer str=new StringBuffer(L("^xDefined Quest Holidays^?\n\r"));
		List<String> line=null;
		String var=null;
		List<String> V=null;
		str.append("^H#  "+CMStrings.padRight(L("Holiday Name"),20)+CMStrings.padRight(L("Area Name(s)"),50)+"^?\n\r");
		for(int s=1;s<steps.size();s++)
		{
			final String step=steps.get(s);
			V=Resources.getFileLineVector(new StringBuffer(step));
			final List<List<String>> cmds=CMLib.quests().parseQuestCommandLines(V,"SET",0);
			List<String> areaLine=null;
			List<String> nameLine=null;
			for(int v=0;v<cmds.size();v++)
			{
				line=cmds.get(v);
				if(line.size()>1)
				{
					var=line.get(1).toUpperCase();
					if (var.equals("AREAGROUP"))
					{
						areaLine = line;
					}
					if (var.equals("NAME"))
					{
						nameLine = line;
					}
				}
			}
			if(nameLine!=null)
			{
				boolean contains=true;//(areaName==null);
				if(areaLine!=null)
				{
					if((!contains) && (areaName != null))
					for(int l=2;l<areaLine.size();l++)
					{
						if(areaName.equalsIgnoreCase(areaLine.get(l)))
							{
								contains = true;
								break;
							}
					}
				}
				else
				{
					areaLine=new XVector<String>("","","*special*");
					contains=true;
				}
				if(contains)
				{
					final String name=CMParms.combine(nameLine,2);
					str.append(CMStrings.padRight(""+s,3)+CMStrings.padRight(name,20)+CMStrings.padRight(CMParms.combineQuoted(areaLine,2),30)+"\n\r");
				}
			}
		}
		return str.toString();
	}

	protected void promptText(MOB mob, TriadList<String,String,Integer> sets, String var, int showNumber, int showFlag, String prompt, String help, boolean emptyOK)
	throws java.io.IOException
	{
		final int index=sets.indexOfFirst(var);
		final String oldVal=index>=0?(String)sets.get(index).second:"";
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			final String newVAL=CMLib.genEd().prompt(mob,oldVal,showNumber,showFlag,prompt,emptyOK);
			if(newVAL.equals("?"))
			{
				mob.tell(help);
				continue;
			}
			else
			if(index>=0)
				sets.get(index).second = newVAL;
			else
			if(!newVAL.equals(oldVal))
				sets.add(var,newVAL,Integer.valueOf(-1));
			break;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public String createHoliday(String named, String areaName, boolean save)
	{
		final Object resp=getHolidayFile();
		if(resp instanceof String)
			return (String)resp;
		List<String> steps=null;
		if(resp instanceof List)
			steps=(List<String>)resp;
		else
			return "Unknown error.";
		if(CMLib.quests().fetchQuest(named)!=null)
			return "A quest called '"+named+"' already exists.  Better to pick a new name.";
		Vector<String> lineV=null;
		String line=null;
		String var=null;
		String cmd=null;
		String step=null;
		for(int v=0;v<steps.size();v++)
		{
			step=steps.get(v);
			final List<String> stepV=Resources.getFileLineVector(new StringBuffer(step));
			for(int v1=0;v1<stepV.size();v1++)
			{
				line=stepV.get(v1);
				lineV=CMParms.parse(line);
				if(lineV.size()>1)
				{
					cmd=lineV.elementAt(0).toUpperCase();
					var=lineV.elementAt(1).toUpperCase();
					if(cmd.equals("SET")&&(var.equalsIgnoreCase("NAME")))
					{
						final String str=CMParms.combine(lineV,2);
						if(str.equalsIgnoreCase(named))
							return "A quest called '"+named+"' already exists.  Better to pick a new name or modify the existing one.";
					}
				}
			}
		}
		if(save)
		{
			final CMFile F=new CMFile(Resources.makeFileResourceName(holidayFilename),null);
			F.saveText(getDefaultHoliData(named,areaName),true);
			final Quest Q=fetchQuest("holidays");
			if(Q!=null)
				Q.setScript(holidayDefinition,true);
		}
		return "";
	}

	@Override
	public StringBuffer getDefaultHoliData(String named, String area)
	{
		final StringBuffer newHoliday=new StringBuffer("");
		newHoliday.append("\n\rSET NAME "+named+"\n\r");
		newHoliday.append("SET WAIT 900+(1?100)\n\r");
		newHoliday.append("SET INTERVAL 1\n\r");
		newHoliday.append("SET DURATION 900\n\r");
		newHoliday.append("SET PERSISTANCE TRUE\n\r");
		newHoliday.append("SET AREAGROUP \""+area+"\"\n\r");
		newHoliday.append("QUIET\n\r");
		newHoliday.append("SET MOBGROUP RESELECT MASK=+INT 3\n\r");
		newHoliday.append("GIVE BEHAVIOR MUDCHAT +(|"+named+")\\;9Happy "+named+" $n!\\;\n\r");
		newHoliday.append("SET MOBGROUP RESELECT MASK=-JAVACLASS +GenShopkeeper +StdShopKeeper\n\r");
		newHoliday.append("GIVE STAT PRICEMASKS 0.75 -MATERIAL +CLOTH\n\r");
		newHoliday.append("STEP BREAK\n\r");
		return newHoliday;
	}

	@Override
	@SuppressWarnings("unchecked")
	public String deleteHoliday(int holidayNumber)
	{
		final Object resp=getHolidayFile();
		if(resp instanceof String)
			return (String)resp;
		List<String> steps=null;
		if(resp instanceof List)
			steps=(List<String>)resp;
		else
			return "Unknown error.";

		if((holidayNumber<=0)||(holidayNumber>=steps.size()))
			return holidayNumber+" does not exist as a holiday -- enter LIST HOLIDAYS.";

		String step=null;
		final StringBuffer buf=new StringBuffer("");
		steps.remove(holidayNumber);
		for(int v=0;v<steps.size();v++)
		{
			step=steps.get(v);
			buf.append(step+"\n\r");
		}
		final CMFile F=new CMFile(Resources.makeFileResourceName(holidayFilename),null);
		F.saveText(buf);
		final Quest Q=fetchQuest("holidays");
		if(Q!=null)
			Q.setScript(holidayDefinition,true);
		return "Holiday deleted.";
	}

	@Override
	@SuppressWarnings("unchecked")
	public String getHolidayName(int index)
	{
		final Object resp=getHolidayFile();
		if(resp instanceof String)
		{
			return "";
		}
		List<String> steps=null;
		if(resp instanceof List)
			steps=(List<String>)resp;
		else
			return "";

		if((index<0)||(index>=steps.size()))
			return "";

		Vector<String> lineV=null;
		String line=null;
		String var=null;
		String cmd=null;
		String step=null;
		step=steps.get(index);
		final List<String> stepV=Resources.getFileLineVector(new StringBuffer(step));
		for(int v1=0;v1<stepV.size();v1++)
		{
			line=stepV.get(v1);
			lineV=CMParms.parse(line);
			if(lineV.size()>1)
			{
				cmd=lineV.elementAt(0).toUpperCase();
				var=lineV.elementAt(1).toUpperCase();
				if(cmd.equals("SET")&&(var.equalsIgnoreCase("NAME")))
					return CMParms.combine(lineV,2);
			}
		}
		return "";
	}

	@Override
	@SuppressWarnings("unchecked")
	public int getHolidayIndex(String named)
	{
		final Object resp=getHolidayFile();
		if(resp instanceof String)
		{
			return -1;
		}
		List<String> steps=null;
		if(resp instanceof List)
			steps=(List<String>)resp;
		else
			return -1;

		Vector<String> lineV=null;
		String line=null;
		String var=null;
		String cmd=null;
		String step=null;
		for(int v=1;v<steps.size();v++)
		{
			step=steps.get(v);
			final List<String> stepV=Resources.getFileLineVector(new StringBuffer(step));
			for(int v1=0;v1<stepV.size();v1++)
			{
				line=stepV.get(v1);
				lineV=CMParms.parse(line);
				if(lineV.size()>1)
				{
					cmd=lineV.elementAt(0).toUpperCase();
					var=lineV.elementAt(1).toUpperCase();
					if(cmd.equals("SET")&&(var.equalsIgnoreCase("NAME")))
					{
						final String str=CMParms.combine(lineV,2);
						if(str.equalsIgnoreCase(named))
							return v;
					}
				}
			}
		}
		return -1;
	}

	public int startLineIndex(List<String> V, String start)
	{
		start=start.toUpperCase().trim();
		for(int v=0;v<V.size();v++)
		{
			if(V.get(v).toUpperCase().trim().startsWith(start))
				return v;
		}
		return -1;
	}

	@Override
	public HolidayData getEncodedHolidayData(String dataFromStepsFile)
	{
		final List<String> stepV=Resources.getFileLineVector(new StringBuffer(dataFromStepsFile));
		for(int v=0;v<stepV.size();v++)
			stepV.set(v,CMStrings.replaceAll(stepV.get(v),"\\;",";"));
		final TriadList<String,String,Integer> settings=new TriadVector<String,String,Integer>();
		final TriadList<String,String,Integer> behaviors=new TriadVector<String,String,Integer>();
		final TriadList<String,String,Integer> properties=new TriadVector<String,String,Integer>();
		final TriadList<String,String,Integer> stats=new TriadVector<String,String,Integer>();
		Vector<String> lineV=null;
		String line=null;
		String var=null;
		String cmd=null;
		int pricingMobIndex=-1;
		final String[] SETTINGS={"NAME","WAIT","DATE","DURATION","MUDDAY","AREAGROUP","MOBGROUP"};
		for(int v=0;v<stepV.size();v++)
		{
			line=stepV.get(v);
			lineV=CMParms.parse(line);
			if(lineV.size()>1)
			{
				cmd=lineV.elementAt(0).toUpperCase();
				var=lineV.elementAt(1).toUpperCase();
				if(cmd.equals("SET")&&(CMParms.indexOf(SETTINGS,var)>=0))
				{
					if(!settings.containsFirst(var))
					{
						String str=CMParms.combineQuoted(lineV,2);
						if(str.toUpperCase().startsWith("ANY "))
							str=str.substring(4);
						if(str.toUpperCase().startsWith("RESELECT MASK="))
							str=str.substring(14);
						if(str.toUpperCase().startsWith("MASK="))
							str=str.substring(5);
						settings.add(var,str,Integer.valueOf(v));
					}
					else
					if((var.equalsIgnoreCase("MOBGROUP"))
					&&(pricingMobIndex<0)
					&&(CMParms.combine(lineV,2).toUpperCase().indexOf("SHOPKEEPER")>0))
						pricingMobIndex=v;
				}
				if(cmd.equals("GIVE")&&("BEHAVIOR".equalsIgnoreCase(var))&&(lineV.size()>2)&&(pricingMobIndex<0))
				{
					var=lineV.elementAt(2).toUpperCase();
					behaviors.add(var,CMParms.combineQuoted(lineV,3),Integer.valueOf(v));
				}
				if(cmd.equals("GIVE")&&("AFFECT".equalsIgnoreCase(var))&&(lineV.size()>2)&&(pricingMobIndex<0))
				{
					var=lineV.elementAt(2).toUpperCase();
					properties.add(var,CMParms.combineQuoted(lineV,3),Integer.valueOf(v));
				}
				if(cmd.equals("GIVE")&&("STAT".equalsIgnoreCase(var))&&(lineV.size()>2))
				{
					var=lineV.elementAt(2).toUpperCase();
					if((pricingMobIndex<0)||(var.equals("PRICEMASKS")))
						stats.add(var,CMParms.combineQuoted(lineV,3),Integer.valueOf(v));
				}
			}
		}
		final Integer pricingMobIndexI = Integer.valueOf(pricingMobIndex);
		return new HolidayData()
		{
			@Override
			public TriadList<String, String, Integer> settings()
			{
				return settings;
			}

			@Override
			public TriadList<String, String, Integer> behaviors()
			{
				return behaviors;
			}

			@Override
			public TriadList<String, String, Integer> properties()
			{
				return properties;
			}

			@Override
			public TriadList<String, String, Integer> stats()
			{
				return stats;
			}

			@Override
			public List<String> stepV()
			{
				return stepV;
			}

			@Override
			public Integer pricingMobIndex()
			{
				return pricingMobIndexI;
			}
		};
	}

	@Override
	@SuppressWarnings("unchecked")
	public void modifyHoliday(MOB mob, int holidayNumber)
	{
		final Object resp=getHolidayFile();
		if(resp instanceof String)
		{
			mob.tell((String)resp);
			return;
		}
		List<String> steps=null;
		if(resp instanceof List)
			steps=(List<String>)resp;
		else
		{
			mob.tell(L("Unknown error."));
			return;
		}
		if((holidayNumber<=0)||(holidayNumber>=steps.size()))
		{
			mob.tell(L("@x1 does not exist as a holiday -- enter LIST HOLIDAYS.",""+holidayNumber));
			return;
		}

		final String step=steps.get(holidayNumber);
		final HolidayData encodedData=getEncodedHolidayData(step);
		final TriadList<String,String,Integer> settings=encodedData.settings();
		final TriadList<String,String,Integer> behaviors=encodedData.behaviors();
		final TriadList<String,String,Integer> properties=encodedData.properties();
		final TriadList<String,String,Integer> stats=encodedData.stats();

		final int oldNameIndex=settings.indexOfFirst("NAME");
		if((mob.isMonster())||(oldNameIndex<0))
			return;
		final String oldName=settings.get(oldNameIndex).second;
		boolean ok=false;
		int showFlag=-1;
		if(CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0)
			showFlag=-999;
		try
		{
			while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
			{
				int showNumber=0;
				promptText(mob,settings,"NAME",++showNumber,showFlag,"Holiday Name","It's, well, a name.",false);
				showNumber=promptDuration(mob,settings,showNumber,showFlag);
				if(settings.indexOfFirst("AREAGROUP")>=0)
					promptText(mob,settings,"AREAGROUP",++showNumber,showFlag,"Areas List (?)","Area names are space separated, and words grouped using double-quotes",false);
				if(settings.indexOfFirst("MOBGROUP")>=0)
					promptText(mob,settings,"MOBGROUP",++showNumber,showFlag,"Mask for mobs that apply (?)",CMLib.masking().maskHelp("\n\r","disallow"),false);
				promptText(mob,properties,"MOOD",++showNumber,showFlag,"Mood setting (?)","NULL/Empty (to not use a Mood), or one of: FORMAL, POLITE, HAPPY, SAD, ANGRY, RUDE, MEAN, PROUD, GRUMPY, EXCITED, SCARED, LONELY",true);
				promptText(mob,behaviors,"AGGRESSIVE",++showNumber,showFlag,"Aggressive setting (?)",CMLib.help().getHelpText("Aggressive",mob,true)+"\n\r\n\r** NULL/Empty (to not use Aggressive **",true);
				showNumber=genPricing(mob,stats,++showNumber,showFlag);
				showNumber=genMudChat(mob,"MUDCHAT",behaviors,++showNumber,showFlag);
				showNumber=genBehaviors(mob,behaviors,++showNumber,showFlag);
				showNumber=genProperties(mob,properties,++showNumber,showFlag);
				if(showFlag<-900)
				{
					ok=true;
					break;
				}
				if(showFlag>0)
				{
					showFlag=-1;
					continue;
				}
				showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
				if(showFlag<=0)
				{
					showFlag=-1;
					ok=true;
				}
			}
		}
		catch (final java.io.IOException e)
		{
			return;
		}
		if(ok)
		{
			final String err=alterHoliday(oldName, encodedData);
			if(err.length()==0)
				mob.tell(L("Holiday modified."));
			else
				mob.tell(err);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public String alterHoliday(String oldName, HolidayData newData)
	{
		final TriadList<String,String,Integer> settings=newData.settings();
		final TriadList<String,String,Integer> behaviors=newData.behaviors();
		final TriadList<String,String,Integer> properties=newData.properties();
		final TriadList<String,String,Integer> stats=newData.stats();
		//List stepV=(List)data.elementAt(4);
		final int pricingMobIndex=newData.pricingMobIndex().intValue();

		final int holidayNumber=getHolidayIndex(oldName);
		final Object resp=getHolidayFile();
		if(resp instanceof String)
			return (String)resp;
		List<String> steps=null;
		if(resp instanceof List)
			steps=(List<String>)resp;
		else
			return "Unknown error.";

		String step = null;
		List<String> stepV = null;
		HolidayData encodedData = null;
		final StringBuffer buf=new StringBuffer("");
		for(int v=0;v<steps.size();v++)
		{
			step=steps.get(v);
			if(v==holidayNumber)
			{
				encodedData=getEncodedHolidayData(step);
				final TriadList<String,String,Integer> oldBehaviors=new TriadVector<String,String,Integer>(encodedData.behaviors());
				final TriadList<String,String,Integer> oldProperties=new TriadVector<String,String,Integer>(encodedData.properties());
				stepV=encodedData.stepV();

				int index=startLineIndex(stepV,"SET NAME");
				stepV.set(index,"SET NAME "+settings.get(settings.indexOfFirst("NAME")).second);
				index=startLineIndex(stepV,"SET DURATION");
				stepV.set(index,"SET DURATION "+settings.get(settings.indexOfFirst("DURATION")).second);
				int intervalLine=startLineIndex(stepV,"SET MUDDAY");
				if(intervalLine<0)
					intervalLine=startLineIndex(stepV,"SET DATE");
				if(intervalLine<0)
					intervalLine=startLineIndex(stepV,"SET WAIT");
				final int mudDayIndex=settings.indexOfFirst("MUDDAY");
				final int dateIndex=settings.indexOfFirst("DATE");
				final int waitIndex=settings.indexOfFirst("WAIT");
				if(mudDayIndex>=0)
					stepV.set(intervalLine,"SET MUDDAY "+(settings.get(mudDayIndex).second));
				else
				if(dateIndex>=0)
					stepV.set(intervalLine,"SET DATE "+(settings.get(dateIndex).second));
				else
					stepV.set(intervalLine,"SET WAIT "+(settings.get(waitIndex).second));

				index=settings.indexOfFirst("AREAGROUP");
				if(index>=0)
				{
					index=startLineIndex(stepV,"SET AREAGROUP");
					if(index>=0)
						stepV.set(index,"SET AREAGROUP "+settings.get(settings.indexOfFirst("AREAGROUP")).second);
				}

				index=settings.indexOfFirst("MOBGROUP");
				if(index>=0)
				{
					index=startLineIndex(stepV,"SET MOBGROUP");
					stepV.set(index,"SET MOBGROUP RESELECT MASK="+settings.get(settings.indexOfFirst("MOBGROUP")).second);
				}
				if((pricingMobIndex>0)&&(stats.indexOfFirst("PRICEMASKS")>=0))
				{
					index=startLineIndex(stepV,"GIVE STAT PRICEMASKS");
					final String s=stats.get(stats.indexOfFirst("PRICEMASKS")).second;
					if(s.trim().length()==0)
					{
						if(index>=0)
							stepV.remove(index);
					}
					else
					{
						if(index>=0)
							stepV.set(index,"GIVE STAT PRICEMASKS "+s);
						else
							stepV.add(pricingMobIndex+1,"GIVE STAT PRICEMASKS "+s);
					}
				}
				int mobGroupIndex=startLineIndex(stepV,"SET MOBGROUP");
				index=behaviors.indexOfFirst("AGGRESSIVE");
				if(index>=0)
				{
					index=startLineIndex(stepV,"GIVE BEHAVIOR AGGRESSIVE");
					final String s=behaviors.get(behaviors.indexOfFirst("AGGRESSIVE")).second;
					if(s.trim().length()==0)
					{
						if(index>=0)
							stepV.remove(index);
					}
					else
					{
						if(index>=0)
							stepV.set(index,"GIVE BEHAVIOR AGGRESSIVE "+s);
						else
							stepV.add(mobGroupIndex+1,"GIVE BEHAVIOR AGGRESSIVE "+s);
					}
				}

				mobGroupIndex=startLineIndex(stepV,"SET MOBGROUP");
				index=behaviors.indexOfFirst("MUDCHAT");
				if(index>=0)
				{
					index=startLineIndex(stepV,"GIVE BEHAVIOR MUDCHAT");
					final String s=behaviors.get(behaviors.indexOfFirst("MUDCHAT")).second;
					if(s.trim().length()<2)
					{
						if(index>=0)
							stepV.remove(index);
					}
					else
					{
						if(index>=0)
							stepV.set(index,"GIVE BEHAVIOR MUDCHAT "+s);
						else
							stepV.add(mobGroupIndex+1,"GIVE BEHAVIOR MUDCHAT "+s);
					}
				}

				mobGroupIndex=startLineIndex(stepV,"SET MOBGROUP");
				index=properties.indexOfFirst("MOOD");
				if(index>=0)
				{
					index=startLineIndex(stepV,"GIVE AFFECT MOOD");
					final String s=properties.get(properties.indexOfFirst("MOOD")).second;
					if(s.trim().length()==0)
					{
						if(index>=0)
							stepV.remove(index);
					}
					else
					{
						if(index>=0)
							stepV.set(index,"GIVE AFFECT MOOD "+s);
						else
							stepV.add(mobGroupIndex+1,"GIVE AFFECT MOOD "+s);
					}
				}

				// look for newly missing stuff
				for(int p=0;p<oldProperties.size();p++)
				{
					final String prop=oldProperties.get(p).first;
					if(properties.indexOfFirst(prop)<0)
					{
						index=startLineIndex(stepV,"GIVE AFFECT "+prop);
						if(index>=0)
							stepV.remove(index);
					}
				}
				// look for newly missing stuff
				for(int p=0;p<oldBehaviors.size();p++)
				{
					final String behav=oldBehaviors.get(p).first;
					if(behaviors.indexOfFirst(behav)<0)
					{
						index=startLineIndex(stepV,"GIVE BEHAVIOR "+behav);
						if(index>=0)
							stepV.remove(index);
					}
				}
				// now changed/added stuff
				for(int p=0;p<properties.size();p++)
				{
					final String prop=properties.get(p).first;
					if(prop.equalsIgnoreCase("MOOD"))
						continue;
					mobGroupIndex=startLineIndex(stepV,"SET MOBGROUP");
					index=startLineIndex(stepV,"GIVE AFFECT "+prop);
					if(index>=0)
						stepV.set(index,"GIVE AFFECT "+prop.toUpperCase().trim()+" "+(properties.get(p).second));
					else
						stepV.add(mobGroupIndex+1,"GIVE AFFECT "+prop.toUpperCase().trim()+" "+(properties.get(p).second));
				}
				// now changed/added stuff
				for(int p=0;p<behaviors.size();p++)
				{
					final String behav=behaviors.get(p).first;
					if(behav.equalsIgnoreCase("AGGRESSIVE")||behav.equalsIgnoreCase("MUDCHAT"))
						continue;
					mobGroupIndex=startLineIndex(stepV,"SET MOBGROUP");
					index=startLineIndex(stepV,"GIVE BEHAVIOR "+behav);
					if(index>=0)
						stepV.set(index,"GIVE BEHAVIOR "+behav.toUpperCase().trim()+" "+(behaviors.get(p).second));
					else
						stepV.add(mobGroupIndex+1,"GIVE BEHAVIOR "+behav.toUpperCase().trim()+" "+(behaviors.get(p).second));
				}

				for(int v1=0;v1<stepV.size();v1++)
				{
					if(stepV.get(v1).trim().length()>0)
						buf.append(CMStrings.replaceAll((stepV.get(v1)+"\n\r"),";","\\;"));
				}
				buf.append("\n\r");
			}
			else
				buf.append(step+"\n\r");
		}
		final CMFile F=new CMFile(Resources.makeFileResourceName(holidayFilename),null);
		F.saveText(buf);
		final Quest Q=fetchQuest("holidays");
		if(Q!=null)
			Q.setScript(holidayDefinition,true);
		return "";
	}

	protected int promptDuration(MOB mob, TriadList<String,String,Integer> settings, int showNumber,int showFlag)
		throws IOException
	{
		int mudDayIndex=settings.indexOfFirst("MUDDAY");
		int dateIndex=settings.indexOfFirst("DATE");
		int waitIndex=settings.indexOfFirst("WAIT");
		int durationIndex=settings.indexOfFirst("DURATION");
		if(durationIndex<0)
		{
			settings.add("DURATION","900",Integer.valueOf(-1));
			durationIndex=settings.indexOfFirst("DURATION");
		}
		++showNumber;
		if((showFlag<0)||(showFlag==showNumber))
		{
			final String[] TYPES={"RANDOM INTERVAL","MUD-DAY","RL-DAY"};
			int typeIndex=0;
			if(mudDayIndex>=0)
				typeIndex=1;
			else
			if(dateIndex>=0)
				typeIndex=2;

			if((showFlag<=-999)||(showFlag==showNumber))
			{
				String newVal="?";
				while(newVal.equals("?")&&((mob.session()!=null)&&(!mob.session().isStopped())))
				{
					newVal=CMLib.genEd().prompt(mob,TYPES[typeIndex],showNumber,showFlag,L("Schedule type"),CMParms.toListString(TYPES));
					if(CMParms.indexOf(TYPES,newVal.toUpperCase().trim())<0)
					{
						newVal="?";
						mob.tell(L("Not a valid entry.  Try ?"));
						continue;
					}
					typeIndex=CMParms.indexOf(TYPES,newVal.toUpperCase().trim());
					if((typeIndex!=0)&&(waitIndex>=0))
						settings.removeFirst("WAIT");
					if((typeIndex!=1)&&(mudDayIndex>=0))
						settings.removeFirst("MUDDAY");
					if((typeIndex!=2)&&(dateIndex>=0))
						settings.removeFirst("DATE");
					if((typeIndex==0)&&(waitIndex<0))
						settings.add("WAIT","100",Integer.valueOf(-1));
					if((typeIndex==1)&&(mudDayIndex<0))
						settings.add("MUDDAY","1-1",Integer.valueOf(-1));
					if((typeIndex==2)&&(dateIndex<0))
						settings.add("DATE","1-1",Integer.valueOf(-1));
					if(showFlag==showNumber)
						return showNumber;
					break;
				}
				mudDayIndex=settings.indexOfFirst("MUDDAY");
				dateIndex=settings.indexOfFirst("DATE");
				waitIndex=settings.indexOfFirst("WAIT");
				durationIndex=settings.indexOfFirst("DURATION");
			}
			else
				mob.tell(L("@x1. Schedule type: @x2",""+showNumber,TYPES[typeIndex]));
		}

		if(mudDayIndex>=0)
			promptText(mob,settings,"MUDDAY",++showNumber,showFlag,"Mud-Day (MONTH-DAY)","It's, well, a date in form month-day.",false);
		else
		if(dateIndex>=0)
			promptText(mob,settings,"DATE",++showNumber,showFlag,"Real Life Date (MONTH-DAY)","It's, well, a date in form month-day.",false);
		else
			promptText(mob,settings,"WAIT",++showNumber,showFlag,"Interval Ticks (?): ","It's in ticks; where 150 ticks=1 mud hour.  You can also make random interval by using ? operator where 1?5 means random number from 1-5.  Make expressions like 100+((1?5)*10)",false);
		promptText(mob,settings,"DURATION",++showNumber,showFlag,"Duration Ticks (?): ","It's in ticks; where 150 ticks=1 mud hour.  You can also make random durations by using ? operator where 1?5 means random number from 1-5.  Make expressions like 100+((1?5)*10)",false);
		return showNumber;
	}

	protected int genBehaviors(MOB mob, TriadList<String,String,Integer> behaviors, int showNumber, int showFlag)
	throws IOException
	{
		for(int b=0;b<=behaviors.size();b++)
		{
			if((b<behaviors.size())
			&&(behaviors.get(b).first.equalsIgnoreCase("MUDCHAT")
				||behaviors.get(b).first.equalsIgnoreCase("AGGRESSIVE")))
				continue;
			if((showFlag>0)&&(showFlag!=showNumber))
			{
				if(b<behaviors.size()) 
					showNumber++;
				continue;
			}
			if(b==behaviors.size())
			{
				if((mob.session()!=null)&&(!mob.session().isStopped()))
				{
					if(((showFlag<=-999)&&CMLib.genEd().prompt(mob,false,showNumber,showFlag,L("Add new mob behavior")))
					||(showNumber==showFlag))
					{
						behaviors.add("BehaviorID","",Integer.valueOf(behaviors.size()));
						b-=1;
					}
					else
					if(showFlag==-1)
						mob.tell(L("@x1. Add new mob behavior",""+showNumber));
				}
				continue;
			}
			String behavior=behaviors.get(b).first;
			String parms=behaviors.get(b).second;

			mob.tell(L("@x1. Behavior: @x2: @x3",""+showNumber,behavior,parms));
			if((showFlag==showNumber)||(showFlag<=-999))
			{
				behavior=CMLib.genEd().prompt(mob,behavior,showNumber,showFlag,L("Behavior ID (NULL to delete)"),true,toStringList(CMClass.behaviors()));
				if(behavior.length()==0)
				{
					behaviors.remove(b);
					b--;
					if((showFlag==showNumber))
						break;
					showNumber--;
					continue;
				}
				if(CMClass.getBehavior(behavior)==null)
				{
					mob.tell(L("Behavior '@x1' does not exist.  Use ? for a list.",behavior));
					b--;
					showNumber--;
					continue;
				}
				StringBuilder help=CMLib.help().getHelpText(behavior,mob,true);
				if(help==null)
					help=new StringBuilder("No help on '"+behavior+"'");
				parms=CMLib.genEd().prompt(mob,parms,showNumber,showFlag,L("Behavior Parameters"),help.toString());
				behaviors.get(b).first = behavior;
				behaviors.get(b).second = parms;
			}
			showNumber++;
		}
		return showNumber;
	}

	protected int genProperties(MOB mob, TriadList<String,String,Integer> properties, int showNumber, int showFlag)
	throws IOException
	{
		for(int p=0;p<=properties.size();p++)
		{
			if((p<properties.size())
			&&(properties.get(p).first.equalsIgnoreCase("MOOD")))
				continue;
			if((showFlag>0)&&(showFlag!=showNumber))
			{
				if(p<properties.size()) 
					showNumber++;
				continue;
			}
			if(p==properties.size())
			{
				if((mob.session()!=null)&&(!mob.session().isStopped()))
				{
					if(((showFlag<=-999)&&CMLib.genEd().prompt(mob,false,showNumber,showFlag,L("Add new mob property")))
					||(showNumber==showFlag))
					{
						properties.add("AbilityID","",Integer.valueOf(properties.size()));
						p-=1;
					}
					else
					if(showFlag==-1)
						mob.tell(L("@x1. Add new mob property",""+showNumber));
				}
				continue;
			}
			String propertyID=properties.get(p).first;
			String parms=properties.get(p).second;

			mob.tell(L("@x1. Effect: @x2: @x3",""+showNumber,propertyID,parms));
			if((showFlag==showNumber)||(showFlag<=-999))
			{
				propertyID=CMLib.genEd().prompt(mob,propertyID,showNumber,showFlag,L("Ability ID (NULL to delete)"),true,toStringList(CMClass.abilities()));
				if(propertyID.length()==0)
				{
					properties.remove(p);
					p--;
					if((showFlag==showNumber))
						break;
					showNumber--;
					continue;
				}
				if(CMClass.getAbility(propertyID)==null)
				{
					mob.tell(L("Ability '@x1' does not exist.  Use ? for a list.",propertyID));
					p--;
					showNumber--;
					continue;
				}
				StringBuilder help=CMLib.help().getHelpText(propertyID,mob,true);
				if(help==null)
					help=new StringBuilder("No help on '"+propertyID+"'");
				parms=CMLib.genEd().prompt(mob,parms,showNumber,showFlag,L("Ability Parameters"),help.toString());
				properties.get(p).first =propertyID;
				properties.get(p).second = parms;
			}
			showNumber++;
		}
		return showNumber;
	}

	public static String toStringList(Enumeration<?> e)
	{
		if(!e.hasMoreElements())
			return "";
		final StringBuffer s=new StringBuffer("");
		Object o=null;
		for(;e.hasMoreElements();)
		{
			o=e.nextElement();
			if(o instanceof CMObject)
				s.append(", "+((CMObject)o).ID());
			else
				s.append(", "+o);
		}
		if(s.length()==0)
			return "";
		return s.toString().substring(2);
	}

	protected int genPricing(MOB mob, TriadList<String,String,Integer> stats, int showNumber, int showFlag)
	throws IOException
	{
		final int pndex=stats.indexOfFirst("PRICEMASKS");
		final String priceStr=(pndex<0)?"":(String)stats.get(pndex).second;
		final List<String> priceV=CMParms.parseCommas(priceStr,true);
		for(int v=0;v<=priceV.size();v++)
		{
			if((showFlag>0)&&(showFlag!=showNumber))
			{
				if(v<priceV.size())
					showNumber++;
				continue;
			}
			if(v==priceV.size())
			{
				if((mob.session()!=null)&&(!mob.session().isStopped()))
				{
					if(((showFlag<=-999)&&CMLib.genEd().prompt(mob,false,showNumber,showFlag,L("Add new price factor")))
					||(showNumber==showFlag))
					{
						priceV.add("1.0");
						v-=1;
					}
					else
					if(showFlag==-1)
						mob.tell(L("@x1. Add new price factor.",""+showNumber));
				}
				continue;
			}
			final String priceLine=priceV.get(v);
			double priceFactor=0.0;
			String mask="";
			final int x=priceLine.indexOf(' ');
			if(x<0)
				priceFactor=CMath.s_double(priceLine);
			else
			{
				priceFactor=CMath.s_double(priceLine.substring(0,x));
				mask=priceLine.substring(x+1).trim();
			}
			mob.tell(L("@x1. Price Factor: @x2%: @x3",""+showNumber,""+Math.round(priceFactor*100.0),mask));
			if((showFlag==showNumber)||(showFlag<=-999))
			{
				priceFactor=CMLib.genEd().prompt(mob,priceFactor,showNumber,showFlag,L("Price Factor (enter 0 to delete)"));
				if(priceFactor==0.0)
				{
					priceV.remove(v);
					v--;
					if((showFlag==showNumber))
						break;
					showNumber--;
					continue;
				}
				mob.tell(L("@x1. Price Factor: @x2%: @x3",""+showNumber,""+Math.round(priceFactor*100.0),mask));
				mask=CMLib.genEd().prompt(mob,mask,showNumber,showFlag,L("Item mask for this price"),CMLib.masking().maskHelp("\n\r","disallow"));
				priceV.set(v,priceFactor+" "+mask);
			}
			showNumber++;
		}
		final String newVal=CMParms.toListString(priceV);
		if(pndex>=0)
			stats.get(pndex).second = newVal;
		else
			stats.add("PRICEMASKS",newVal,Integer.valueOf(stats.size()));
		return showNumber;
	}

	@Override
	public String breakOutMaskString(String s, List<String> p)
	{
		String mask="";
		int x=s.toUpperCase().lastIndexOf("MASK=");
		if(x>=0)
		{
			mask=s.substring(x+5).trim();
			int i=0;
			while((i<p.size())&&(p.get(i).toUpperCase().indexOf("MASK=")<0))
				i++;
			if(i<=p.size())
			{
				final String pp=p.get(i);
				x=pp.toUpperCase().indexOf("MASK=");
				if((x>0)&&(pp.substring(0,x).trim().length()>0))
				{
					p.set(i,pp.substring(0,x).trim());
					i++;
				}
				while(i<p.size())
					p.remove(i);
			}
		}
		return mask.trim();
	}

	@Override
	public List<List<String>> breakOutMudChatVs(String MUDCHAT, TriadList<String,String,Integer> behaviors)
	{
		final int mndex=behaviors.indexOfFirst(MUDCHAT);
		String mudChatStr=(mndex<0)?"":(String)behaviors.get(mndex).second;
		if(mudChatStr.startsWith("+"))
			mudChatStr=mudChatStr.substring(1);
		final List<String> rawMCV=CMParms.parseSemicolons(mudChatStr,true);
		final List<List<String>> mudChatV=new Vector<List<String>>();
		String s=null;
		List<String> V=new Vector<String>();
		mudChatV.add(V);
		for(int r=0;r<rawMCV.size();r++)
		{
			s=rawMCV.get(r);
			if(s.startsWith("(")&&s.endsWith(")"))
			{
				if(V.size()>0)
				{
					V=new Vector<String>();
					mudChatV.add(V);
				}
				s=s.substring(1,s.length()-1);
			}
			V.add(s);
		}
		if(V.size()==0)
			mudChatV.remove(V);
		return mudChatV;
	}

	protected int genMudChat(MOB mob, String var, TriadList<String,String,Integer> behaviors, int showNumber, int showFlag)
	throws IOException
	{
		final int mndex=behaviors.indexOfFirst(var);
		final List<List<String>> mudChatV = breakOutMudChatVs(var,behaviors);
		List<String> V = null;
		String s=null;
		for(int v=0;v<=mudChatV.size();v++)
		{
			if((showFlag>0)&&(showFlag!=showNumber))
			{
				if(v<mudChatV.size())
					showNumber++;
				continue;
			}
			if(v==mudChatV.size())
			{
				if((mob.session()!=null)&&(!mob.session().isStopped()))
				{
					if(((showFlag<=-999)&&CMLib.genEd().prompt(mob,false,showNumber,showFlag,L("Add new mud chat")))
					||(showNumber==showFlag))
					{
						V=new Vector<String>();
						V.add("match | these | words or phrases");
						V.add("9say this");
						mudChatV.add(V);
						v-=1;
					}
					else
					if(showFlag==-1)
						mob.tell(L("@x1. Add new mud chat.",""+showNumber));
				}
				continue;
			}
			V=mudChatV.get(v);
			String words=V.get(0);
			mob.tell(L("@x1. MudChat for words: @x2",""+showNumber,words));
			if((showFlag==showNumber)||(showFlag<=-999))
			{
				words=CMLib.genEd().prompt(mob,words,showNumber,showFlag,L("Enter matching words (| delimited, NULL to delete)\n\r"),true);
				if(words.trim().length()==0)
				{
					mudChatV.remove(v);
					if((showFlag==showNumber))
						break;
					showNumber--;
					continue;
				}
				V.set(0,words);
				for(int v1=1;v1<=V.size();v1++)
				{
					if(v1==V.size())
					{
						if((mob.session()!=null)&&(!mob.session().isStopped())
						&&(mob.session().confirm(L("Add another thing to say (y/N)"),L("NO"))))
						{
							V.add("9say this");
							v1-=1;
						}
						continue;
					}
					s=V.get(v1);
					String newStr="?";
					while((newStr.equals("?"))&&(mob.session()!=null)&&(!mob.session().isStopped()))
					{
						newStr=mob.session().prompt(L("Enter  # Weight + thing to say (?) '@x1'\n\r: ",s),s);
						if(newStr.equals("?"))
							mob.tell(L("Enter a number followed by a phrase to say like 9thingtosay. Enter NULL to delete this thing to say."));
						else
							s=newStr;
					}
					if(s.trim().length()==0)
					{
						V.remove(v1);
						v1--;
						continue;
					}
					if(!Character.isDigit(s.charAt(0)))
						s="9"+s;
					V.set(v1,s);
				}
			}
			showNumber++;
		}
		final StringBuffer finalVal=new StringBuffer("");
		for(int v=0;v<mudChatV.size();v++)
		{
			V=mudChatV.get(v);
			if(V.size()==0)
				continue;
			finalVal.append("("+(V.get(0))+");");
			for(int v1=1;v1<V.size();v1++)
				finalVal.append((V.get(v1))+";");
			finalVal.append(";");
		}
		if(mndex>=0)
			behaviors.get(mndex).second  =(finalVal.toString().trim().length()==0)?"":("+"+finalVal.toString());
		else
			behaviors.add(var,(finalVal.toString().trim().length()==0)?"":("+"+finalVal.toString()),Integer.valueOf(behaviors.size()));
		return showNumber;
	}

	@Override
	public List<List<String>> parseQuestCommandLines(List<?> script, String cmdOnly, int startLine)
	{
		Vector<String> line=null;
		String cmd=null;
		boolean inScript=false;
		final List<List<String>> lines=new Vector<List<String>>();
		if(cmdOnly!=null)
			cmdOnly=cmdOnly.toUpperCase().trim();
		for(int v=startLine;v<script.size();v++)
		{
			line=CMParms.parse(((String)script.get(v)));
			if(line.size()==0)
				continue;
			cmd=line.firstElement().toUpperCase().trim();
			if(cmd.equals("</SCRIPT>")&&(inScript))
			{
				inScript=false;
				continue;
			}
			if(cmd.equals("<SCRIPT>"))
			{
				inScript=true;
				continue;
			}
			if(cmd.equals("STEP"))
				return lines;
			if((cmdOnly==null)||(cmdOnly.equalsIgnoreCase(cmd)))
				lines.add(line);
		}
		return lines;
	}

	@Override
	public List<String> parseQuestSteps(List<String> script, int startLine, boolean rawLineInput)
	{
		Vector<String> line=null;
		String cmd=null;
		final Vector<String> parsed=new Vector<String>();
		StringBuffer scr=new StringBuffer("");
		boolean inScript=false;
		String lineStr=null;
		for(int v=startLine;v<script.size();v++)
		{
			lineStr=script.get(v).trim();
			if(lineStr.trim().equalsIgnoreCase(XMLLibrary.FILE_XML_BOUNDARY))
			{
				if(scr.toString().trim().length()>0)
					parsed.addElement(scr.toString());
				scr=new StringBuffer((script.get(v))+"\n\r");
				while((++v)<script.size())
					scr.append((script.get(v))+"\n\r");
				break;
			}
			line=CMParms.parse(lineStr.toUpperCase());
			if(line.size()==0)
				continue;
			cmd=line.firstElement().trim();
			if(rawLineInput)
				scr.append((script.get(v))+"\n\r");
			else
				scr.append(CMStrings.replaceAll((script.get(v))+"\n\r",";","\\;"));
			if(cmd.equals("</SCRIPT>")&&(inScript))
			{
				inScript=false;
				continue;
			}
			if(cmd.equals("<SCRIPT>"))
			{
				inScript=true;
				continue;
			}
			if(cmd.equals("STEP"))
			{
				parsed.addElement(scr.toString());
				scr=new StringBuffer("");
			}
		}
		if(scr.toString().trim().length()>0)
			parsed.add(scr.toString().trim());
		return parsed;
	}

	@Override
	@SuppressWarnings("unchecked")
	public DVector getQuestTemplate(MOB mob, String fileToGet)
	{
		// user security doesn't matter, because this is read-only & system files.
		final int fileOpenFlag=CMFile.FLAG_LOGERRORS|(CMSecurity.isAllowedAnywhere(mob, CMSecurity.SecFlag.CMDQUESTS)?CMFile.FLAG_FORCEALLOW:0);
		final CMFile tempF=new CMFile(Resources.makeFileResourceName("quests/templates"),null,fileOpenFlag);
		if((!tempF.exists())||(!tempF.isDirectory()))
			return null;
		final CMFile[] files=tempF.listFiles();
		final DVector templatesDV=new DVector(5);
		final boolean parsePages=(fileToGet!=null)&&(!fileToGet.endsWith("*"));
		if((fileToGet!=null)&&(fileToGet.endsWith("*")))
			fileToGet=fileToGet.substring(0,fileToGet.length()-1);
		if(files.length==0)
			return null;
		for (final CMFile file : files)
		{
			if((file.getName().toUpperCase().endsWith(".QUEST"))
			&&((fileToGet==null)||(file.getName().toUpperCase().startsWith(fileToGet.toUpperCase().trim()))))
			{
				final List<String> V=Resources.getFileLineVector(file.text());
				String s=null;
				boolean foundStart=false;
				boolean foundQuestScript=false;
				DVector pageDV=null;
				StringBuffer script=null;
				for(int v=0;v<V.size();v++)
				{
					s=V.get(v).trim();
					if((foundQuestScript)&&(script!=null))
						script.append(s+"\n\r");
					else
					if(s.startsWith("#"))
					{
						s=s.substring(1).trim();
						if(s.startsWith("!"))
						{
							s=s.substring(1).trim();
							if(s.startsWith("QUESTMAKER_START_SCRIPT"))
							{
								final String name=s.substring(23).trim();
								foundStart=true;
								script=new StringBuffer("");
								templatesDV.addElement(name,"",file.getName(),new Vector<Object>(),script);
							}
							else
							if(s.startsWith("QUESTMAKER_END_SCRIPT")&&(foundStart))
							{
								foundStart=false;
								foundQuestScript=true;
								continue;
							}
							else
							if(s.startsWith("QUESTMAKER_PAGE")&&(foundStart))
							{
								if(!parsePages)
									break;
								final String name=s.substring(15).trim();
								pageDV=new DVector(4);
								pageDV.addElement(Integer.valueOf(QuestManager.QMCommand.$TITLE.ordinal()),name,"","");
								((Vector<Object>)templatesDV.elementAt(templatesDV.size()-1,4)).addElement(pageDV);
							}
							else
							if(s.trim().length()>0)
								Log.errOut("Quests","Unrecognized meta-questmaker command: "+s);
						}
						else
						if(s.startsWith("$")&&(foundStart))
						{
							if(parsePages)
							{
								final int x=s.indexOf('=');
								if(x<0)
									Log.errOut("Quests","Illegal QuestMaker variable syntax: "+s);
								else
								if(pageDV==null)
									Log.errOut("Quests","QuestMaker syntax error, QUESTMAKER_PAGE not yet designated: "+s);
								else
								{
									final int y=s.indexOf('=',x+1);
									if(y>=0)
										pageDV.addElement(s.substring(x+1,y).trim(),s.substring(0,x),s.substring(y+1),"");
									else
										pageDV.addElement(s.substring(x+1).trim(),s.substring(0,x),"","");
									String cmd=(String)pageDV.elementAt(pageDV.size()-1,1);
									int mask=0;
									if(cmd.startsWith("(")&&cmd.endsWith(")"))
									{
										mask=mask|QuestManager.QM_COMMAND_OPTIONAL;
										cmd=cmd.substring(1,cmd.length()-1);
									}
									final QMCommand command = (QMCommand)CMath.s_valueOf(QMCommand.class, cmd);
									if(command == null)
									{
										Log.errOut("Quests","QuestMaker syntax error, '"+cmd+"' is an unknown command");
										pageDV.removeElementsAt(pageDV.size()-1);
									}
									else
										pageDV.setElementAt(pageDV.size()-1,1,Integer.valueOf(command.ordinal()|mask));
								}
							}
						}
						else
						if(foundStart)
						{
							if(pageDV==null)
							{
								if(s.length()==0)
									templatesDV.setElementAt(templatesDV.size()-1,2,((String)templatesDV.elementAt(templatesDV.size()-1,2))+"\n\r\n\r");
								else
									templatesDV.setElementAt(templatesDV.size()-1,2,((String)templatesDV.elementAt(templatesDV.size()-1,2))+s+" ");
							}
							else
							if(parsePages)
							{
								if((pageDV.size()<2)
								||(((Integer)pageDV.elementAt(pageDV.size()-1,1)).intValue()==QuestManager.QMCommand.$TITLE.ordinal())
								||(((Integer)pageDV.elementAt(pageDV.size()-1,1)).intValue()==QuestManager.QMCommand.$LABEL.ordinal()))
								{
									if(s.length()==0)
									{
										if(((Integer)pageDV.elementAt(pageDV.size()-1,1)).intValue()==QuestManager.QMCommand.$TITLE.ordinal())
											pageDV.addElement(Integer.valueOf(QuestManager.QMCommand.$LABEL.ordinal()),"",s,"");
										else
											pageDV.setElementAt(pageDV.size()-1,3,((String)pageDV.elementAt(pageDV.size()-1,3))+"\n\r\n\r");
									}
									else
										pageDV.setElementAt(pageDV.size()-1,3,((String)pageDV.elementAt(pageDV.size()-1,3))+s+" ");
								}
								else
									pageDV.addElement(Integer.valueOf(QuestManager.QMCommand.$LABEL.ordinal()),"",s,"");
							}
						}
					}
				}

			}
		}
		if(templatesDV.size()==0)
			return null;
		final DVector sortedTemplatesDV=new DVector(5);
		while(templatesDV.size()>0)
		{
			int maxRow=0;
			for(int t=1;t<templatesDV.size();t++)
			{
				if(((String)templatesDV.elementAt(t,1)).compareTo((String)templatesDV.elementAt(maxRow,1))<0)
					maxRow=t;
			}
			sortedTemplatesDV.addElement(templatesDV.elementAt(maxRow,1),
										 templatesDV.elementAt(maxRow,2),
										 templatesDV.elementAt(maxRow,3),
										 templatesDV.elementAt(maxRow,4),
										 templatesDV.elementAt(maxRow,5));
			templatesDV.removeElementsAt(maxRow);
		}
		return sortedTemplatesDV;
	}

	protected String addXMLQuestMob(MOB mob,
									int showFlag,
									DVector pageDV,
									String showValue,
									String parm1Fixed,
									String lastLabel,
									boolean optionalEntry,
									int step,
									int showNumber)
	throws IOException
	{
		MOB M=null;
		final Vector<MOB> choices=new Vector<MOB>();
		MOB baseM=((showValue!=null)?baseM=CMLib.coffeeMaker().getMobFromXML(showValue):null);
		final StringBuffer choiceDescs=new StringBuffer("");
		if(baseM!=null)
		{
			choices.addElement(baseM);
			choiceDescs.append(baseM.name()+", ");
		}
		final Room R=mob.location();
		if(R!=null)
		for(int i=0;i<R.numInhabitants();i++)
		{
			M=R.fetchInhabitant(i);
			if((M!=null)&&(M.isSavable()))
			{
				choices.addElement(M);
				choiceDescs.append(M.name()+", ");
			}
		}
		final Vector<MOB> newMobs=new Vector<MOB>();
		for(final Enumeration<MOB> e=CMClass.mobTypes();e.hasMoreElements();)
		{
			M=e.nextElement();
			if(M.isGeneric())
			{
				M=(MOB)M.copyOf();
				newMobs.add(M);
				M.setName(L("A NEW @x1",M.ID().toUpperCase()));
				choices.add(M);
				choiceDescs.append(M.name()+", ");
			}
		}
		final MOB canMOB=CMClass.getFactoryMOB();
		canMOB.setName(L("CANCEL"));
		choiceDescs.append("CANCEL");
		choices.addElement(canMOB);
		String showName=showValue;
		if(baseM!=null)
			showName=CMLib.english().getContextName(choices,baseM);
		lastLabel=((lastLabel==null)?"":lastLabel)+"\n\rChoices: "+choiceDescs.toString();
		final GenericEditor.CMEval evaler = getQuestCommandEval(QMCommand.$MOBXML);
		final String s=CMLib.genEd().prompt(mob,showName,showNumber,showFlag,parm1Fixed,optionalEntry,false,lastLabel,
										evaler, choices.toArray());
		canMOB.destroy();
		if(s.equalsIgnoreCase("CANCEL"))
			return null;
		M=(MOB)CMLib.english().fetchEnvironmental(choices,s,false);
		if((M!=null)&&(newMobs.contains(M)))
		{
			final Command C=CMClass.getCommand("Modify");
			if(C!=null)
				C.executeInternal(mob,0,M);
			// modify it!
		}
		final String newValue=(M!=null)?CMLib.coffeeMaker().getMobXML(M).toString():showValue;
		for(int n=0;n<newMobs.size();n++) newMobs.elementAt(n).destroy();
		return newValue==null?"":newValue.trim();
	}

	protected String addXMLQuestItem(MOB mob,
									 int showFlag,
									 DVector pageDV,
									 String showValue,
									 String parm1Fixed,
									 String lastLabel,
									 boolean optionalEntry,
									 int step,
									 int showNumber)
	throws IOException
	{
		Item I=null;
		final List<Item> choices=new Vector<Item>();
		Item baseI=((showValue!=null)?baseI=CMLib.coffeeMaker().getItemFromXML(showValue):null);
		final StringBuffer choiceDescs=new StringBuffer("");
		if(baseI!=null)
		{
			choices.add(baseI);
			choiceDescs.append(baseI.name()+", ");
		}
		final Room R=mob.location();
		if(R!=null)
		for(int i=0;i<R.numItems();i++)
		{
			I=R.getItem(i);
			if((I!=null)&&(I.container()==null)&&(I.isSavable()))
			{
				choices.add(I);
				choiceDescs.append(I.name()+", ");
			}
		}
		final List<String> allItemNames=new Vector<String>();
		CMClass.addAllItemClassNames(allItemNames,true,false,false,CMProps.getIntVar(CMProps.Int.MUDTHEME));
		final List<Item> newItems=new Vector<Item>();
		for(int a=0;a<allItemNames.size();a++)
		{
			I=CMClass.getItem(allItemNames.get(a));
			if((I!=null)&&(I.isGeneric()))
			{
				newItems.add(I);
				I.setName(L("A NEW @x1",I.ID().toUpperCase()));
				choices.add(I);
				choiceDescs.append(I.name()+", ");
			}
		}
		choiceDescs.append("CANCEL");
		final Item canItem=CMClass.getItem("StdItem");
		canItem.setName(L("CANCEL"));
		choices.add(canItem);
		String showName=showValue;
		if(baseI!=null)
			showName=CMLib.english().getContextName(choices,baseI);
		lastLabel=((lastLabel==null)?"":lastLabel)+"\n\rChoices: "+choiceDescs.toString();
		final GenericEditor.CMEval evaler = getQuestCommandEval(QMCommand.$ITEMXML);
		final String s=CMLib.genEd().prompt(mob,showName,showNumber,showFlag,parm1Fixed,optionalEntry,false,lastLabel,
										evaler, choices.toArray());
		canItem.destroy();
		if(s.equalsIgnoreCase("CANCEL"))
			return null;
		I=(Item)CMLib.english().fetchEnvironmental(choices,s,false);
		if((I!=null)&&(newItems.contains(I)))
		{
			final Command C=CMClass.getCommand("Modify");
			if(C!=null)
				C.executeInternal(mob,0,I);
			// modify it!
		}
		final String newValue=(I!=null)?CMLib.coffeeMaker().getItemXML(I).toString():showValue;
		for(int n=0;n<newItems.size();n++) newItems.get(n).destroy();
		return (newValue==null)?"":newValue.trim();
	}

	@Override
	public List<Quest> getPlayerPersistentQuests(MOB player)
	{
		final Vector<Quest> qVec=new Vector<Quest>();
		for(int q=0;q<CMLib.quests().numQuests();q++)
		{
			final Quest Q=CMLib.quests().fetchQuest(q);
			if(Q==null)
				continue;
			for(final Enumeration<ScriptingEngine> e=player.scripts();e.hasMoreElements();)
			{
				final ScriptingEngine SE=e.nextElement();
				if(SE==null)
					continue;
				if((SE.defaultQuestName().length()>0)
				&&(SE.defaultQuestName().equalsIgnoreCase(Q.name()))
				&&(!qVec.contains(Q)))
					qVec.addElement(Q);
			}
		}
		return qVec;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Quest questMaker(MOB mob)
	{
		if(mob.isMonster())
			return null;
		final DVector questTemplates=getQuestTemplate(mob,null);
		try
		{
			if((questTemplates==null)||(questTemplates.size()==0))
			{
				mob.tell(L("No valid quest templates found in resources/quests/templates!"));
				return null;
			}
			int questIndex=-1;
			while((questIndex<0)&&(mob.session()!=null)&&(!mob.session().isStopped()))
			{
				final String choice=mob.session().prompt(L("Select a quest template (?): "),"");
				if(choice.equals("?"))
				{
					final StringBuffer fullList=new StringBuffer("\n\r^HCANCEL^N -- to cancel.\n\r");
					for(int t=0;t<questTemplates.size();t++)
						fullList.append("^H"+(String)questTemplates.elementAt(t,1)+"^N\n\r"+(String)questTemplates.elementAt(t,2)+"\n\r");
					mob.tell(fullList.toString());
				}
				else
				if((choice.length()==0)||(choice.equalsIgnoreCase("CANCEL")))
					return null;
				else
				{
					final StringBuffer list=new StringBuffer("");
					for(int t=0;t<questTemplates.size();t++)
						if(choice.equalsIgnoreCase((String)questTemplates.elementAt(t,1)))
							questIndex=t;
						else
							list.append(((String)questTemplates.elementAt(t,1))+", ");
					if(questIndex<0)
						mob.tell(L("'@x1' is not a valid quest name, use ? for a list, or select from the following: @x2",choice,list.toString().substring(0,list.length()-2)));
				}
			}
			if((mob.session()==null)||(mob.session().isStopped())||(questIndex<0))
				return null;
			final DVector qFDV=getQuestTemplate(mob,(String)questTemplates.elementAt(questIndex,3));
			if((qFDV==null)||(qFDV.size()==0))
				return null;
			final String questTemplateName=(String)qFDV.elementAt(0,1);
			final Vector<Object> qPages=(Vector<Object>)qFDV.elementAt(0,4);
			mob.tell("^Z"+questTemplateName+"^.^N");
			mob.tell((String)qFDV.elementAt(0,2));
			for(int page=0;page<qPages.size();page++)
			{
				final DVector pageDV=(DVector)qPages.elementAt(page);
				final String pageName=(String)pageDV.elementAt(0,2);
				final String pageInstructions=(String)pageDV.elementAt(0,3);
				mob.tell(L("\n\r\n\r^HPage #@x1: ^N@x2",""+(page+1),pageName));
				mob.tell("^N"+pageInstructions);
				boolean ok=false;
				int showFlag=-999;
				while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
				{
					int showNumber=0;
					String lastLabel=null;
					for(int step=1;step<pageDV.size();step++)
					{
						final Integer stepType=(Integer)pageDV.elementAt(step,1);
						final String keyName=(String)pageDV.elementAt(step,2);
						final String defValue=(String)pageDV.elementAt(step,3);
						String parm1Fixed=CMStrings.capitalizeAndLower(keyName.replace('_',' '));
						if(parm1Fixed.startsWith("$"))
							parm1Fixed=parm1Fixed.substring(1);

						final boolean optionalEntry=CMath.bset(stepType.intValue(),QuestManager.QM_COMMAND_OPTIONAL);
						final int inputCode=stepType.intValue()&QuestManager.QM_COMMAND_MASK;
						final QMCommand inputCommand = QMCommand.values()[inputCode];
						switch(inputCommand)
						{
						case $TITLE: break;
						case $HIDDEN:
							pageDV.setElementAt(step,4,defValue==null?"":defValue);
							break;
						case $LABEL: lastLabel=defValue; break;
						case $EXPRESSION:
						case $TIMEEXPRESSION:
						case $UNIQUE_QUEST_NAME:
						case $STRING:
						case $LONG_STRING:
						case $NAME:
						case $ZAPPERMASK:
						case $ROOMID:
						{
							final String showValue=(showFlag<-900)?defValue:(String)pageDV.elementAt(step,4);
							final GenericEditor.CMEval evaler = getQuestCommandEval(inputCommand);
							if(inputCommand==QMCommand.$ZAPPERMASK)
								lastLabel=(lastLabel==null?"":lastLabel)+"\n\r"+CMLib.masking().maskHelp("\n\r","disallows");
							final String s=CMLib.genEd().prompt(mob,showValue,++showNumber,showFlag,parm1Fixed,optionalEntry,false,lastLabel,
															evaler,null);
							pageDV.setElementAt(step,4,s);
							break;
						}
						case $AREA:
						{
							final String showValue=(showFlag<-900)?"":(String)pageDV.elementAt(step,4);
							final StringBuffer label=new StringBuffer(((lastLabel==null)?"":lastLabel)+"\n\rChoices: ");
							for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
								label.append("\""+e.nextElement().name()+"\" ");
							final GenericEditor.CMEval evaler = getQuestCommandEval(inputCommand);
							final String s=CMLib.genEd().prompt(mob,showValue,++showNumber,showFlag,parm1Fixed,optionalEntry,false,label.toString(),
															evaler,
															null);
							pageDV.setElementAt(step,4,s);
							break;
						}
						case $EXISTING_QUEST_NAME:
						{
							final String showValue=(showFlag<-900)?"":(String)pageDV.elementAt(step,4);
							final StringBuffer label=new StringBuffer(((lastLabel==null)?"":lastLabel)+"\n\rChoices: ");
							for(int q=0;q<CMLib.quests().numQuests();q++)
								label.append("\""+CMLib.quests().fetchQuest(q).name()+"\" ");
							final GenericEditor.CMEval evaler = getQuestCommandEval(inputCommand);
							final String s=CMLib.genEd().prompt(mob,showValue,++showNumber,showFlag,parm1Fixed,optionalEntry,false,label.toString(),
															evaler, null);
							pageDV.setElementAt(step,4,s);
							break;
						}
						case $ABILITY:
						{
							final String showValue=(showFlag<-900)?"":(String)pageDV.elementAt(step,4);
							final StringBuffer label=new StringBuffer(((lastLabel==null)?"":lastLabel)+"\n\rChoices: ");
							for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
								label.append(e.nextElement().ID()+" ");
							final GenericEditor.CMEval evaler = getQuestCommandEval(inputCommand);
							final String s=CMLib.genEd().prompt(mob,showValue,++showNumber,showFlag,parm1Fixed,optionalEntry,false,label.toString(),
															evaler, null);
							pageDV.setElementAt(step,4,s);
							break;
						}
						case $CHOOSE:
						{
							final String showValue=(showFlag<-900)?"":(String)pageDV.elementAt(step,4);
							final String label=((lastLabel==null)?"":lastLabel)+"\n\rChoices: "+defValue;
							final GenericEditor.CMEval evaler = getQuestCommandEval(inputCommand);
							final String s=CMLib.genEd().prompt(mob,showValue,++showNumber,showFlag,parm1Fixed,optionalEntry,false,label,
															evaler,
															CMParms.toStringArray(CMParms.parseCommas(defValue.toUpperCase(),true)));
							pageDV.setElementAt(step,4,s);
							break;
						}
						case $ITEMXML:
						{
							final String showValue=(showFlag<-900)?"":(String)pageDV.elementAt(step,4);
							final String newValue=addXMLQuestItem(mob, showFlag, pageDV, showValue, parm1Fixed, lastLabel, optionalEntry, step, ++showNumber);
							if(newValue!=null)
								pageDV.setElementAt(step,4,newValue);
							break;
						}
						case $ITEMXML_ONEORMORE:
						{
							String showValue=(showFlag<-900)?"":(String)pageDV.elementAt(step,4);
							final Vector<String> itemXMLs=new Vector<String>();
							int x=showValue.indexOf("</ITEM><ITEM>");
							while(x>=0)
							{
								final String xml=showValue.substring(0,x+7).trim();
								if(xml.length()>0)
									itemXMLs.addElement(xml);
								showValue=showValue.substring(x+7);
								x=showValue.indexOf("</ITEM><ITEM>");
							}
							if(showValue.trim().length()>0)
								itemXMLs.addElement(showValue.trim());
							String newValue=null;
							for(int i=0;i<=itemXMLs.size();i++)
							{
								showValue=(i<itemXMLs.size())?(String)itemXMLs.elementAt(i):"";
								final boolean optional=(i==0)?optionalEntry:true;
								final String thisValue=addXMLQuestItem(mob, showFlag, pageDV, showValue, parm1Fixed, lastLabel, optional, step, ++showNumber);
								if(thisValue!=null)
								{
									if(newValue==null)
										newValue="";
									newValue+=thisValue;
									if((thisValue.length()>0)
									&&(i==itemXMLs.size()))
										itemXMLs.addElement(thisValue);
								}
							}
							if(newValue!=null)
								pageDV.setElementAt(step,4,newValue);
							break;
						}
						case $MOBXML:
						{
							final String showValue=(showFlag<-900)?"":(String)pageDV.elementAt(step,4);
							final String newValue=addXMLQuestMob(mob, showFlag, pageDV, showValue, parm1Fixed, lastLabel, optionalEntry, step, ++showNumber);
							if(newValue!=null)
								pageDV.setElementAt(step,4,newValue);
							break;
						}
						case $MOBXML_ONEORMORE:
						{
							String showValue=(showFlag<-900)?"":(String)pageDV.elementAt(step,4);
							final Vector<String> mobXMLs=new Vector<String>();
							int x=showValue.indexOf("</MOB><MOB>");
							while(x>=0)
							{
								final String xml=showValue.substring(0,x+6).trim();
								if(xml.length()>0)
									mobXMLs.addElement(xml);
								showValue=showValue.substring(x+6);
								x=showValue.indexOf("</MOB><MOB>");
							}
							if(showValue.trim().length()>0)
								mobXMLs.addElement(showValue.trim());

							String newValue=null;
							for(int i=0;i<=mobXMLs.size();i++)
							{
								showValue=(i<mobXMLs.size())?(String)mobXMLs.elementAt(i):"";
								final boolean optional=(i==0)?optionalEntry:true;
								final String thisValue=addXMLQuestMob(mob, showFlag, pageDV, showValue, parm1Fixed, lastLabel, optional, step, ++showNumber);
								if(thisValue!=null)
								{
									if(newValue==null)
										newValue="";
									newValue+=thisValue;
									if((thisValue.length()>0)
									&&(i==mobXMLs.size()))
										mobXMLs.addElement(thisValue);
								}
							}
							if(newValue!=null)
								pageDV.setElementAt(step,4,newValue);
							break;
						}
						case $FACTION:
						{
							final String showValue=(showFlag<-900)?"":(String)pageDV.elementAt(step,4);
							final StringBuffer label=new StringBuffer(((lastLabel==null)?"":lastLabel)+"\n\rChoices: ");
							for(final Enumeration<Faction> f=CMLib.factions().factions();f.hasMoreElements();)
								label.append("\""+f.nextElement().name()+"\" ");
							final GenericEditor.CMEval evaler = getQuestCommandEval(inputCommand);
							final String s=CMLib.genEd().prompt(mob,showValue,++showNumber,showFlag,parm1Fixed,optionalEntry,false,label.toString(),
															evaler, null);
							pageDV.setElementAt(step,4,s);
							break;
						}
						}
					}
					if(showFlag<-900)
					{
						ok=false;
						showFlag=0;
						mob.tell(L("\n\r^HNow verify this page's selections:^.^N"));
						continue;
					}
					if(showFlag>0)
					{
						showFlag=-1;
						continue;
					}
					final String what=mob.session().prompt(L("Edit which (enter 0 to cancel)? "),"");
					if(what.trim().equals("0"))
					{
						if(mob.session().confirm(L("Are you sure you want to abort (y/N)? "),"N"))
						{
							mob.tell(L("Aborted."));
							return null;
						}
					}
					showFlag=CMath.s_int(what);
					if(showFlag<=0)
					{
						showFlag=-1;
						ok=true;
						// all done
					}
				}
				if((mob.session()==null)||(mob.session().isStopped()))
					return null;
			}
			String name="";
			String script=((StringBuffer)qFDV.elementAt(0,5)).toString();
			String var=null;
			String val=null;
			for(int page=0;page<qPages.size();page++)
			{
				final DVector pageDV=(DVector)qPages.elementAt(page);
				for(int v=0;v<pageDV.size();v++)
				{
					var=(String)pageDV.elementAt(v,2);
					val=(String)pageDV.elementAt(v,4);
					if(((Integer)pageDV.elementAt(v,1)).intValue()==QMCommand.$UNIQUE_QUEST_NAME.ordinal())
						name=val;
					script=CMStrings.replaceAll(script,var,val);
				}
			}
			script=CMStrings.replaceAll(script,"$#AUTHOR",mob.Name());
			if((mob.session()!=null)&&(!mob.session().isStopped())
			&&(mob.session().confirm(L("Create the new quest: @x1 (y/N)? ",name),"N")))
			{
				final Quest Q=(Quest)CMClass.getCommon("DefaultQuest");
				final CMFile newQF=new CMFile(Resources.makeFileResourceName("quests/"+name+".quest"),mob,CMFile.FLAG_LOGERRORS);
				newQF.saveText(script);
				Q.setScript("LOAD=quests/"+name+".quest",true);
				if((Q.name().trim().length()==0)||(Q.duration()<0))
				{
					mob.tell(L("You must specify a VALID quest string.  This one contained errors.  Try AHELP QUESTS."));
					return null;
				}
				CMLib.quests().addQuest(Q);
				CMLib.quests().save();
				return Q;
			}
			return null;
		}
		catch(final java.io.IOException e)
		{
			return null;
		}
	}
	
	@Override
	public GenericEditor.CMEval getQuestCommandEval(QMCommand command)
	{
		switch(command)
		{
		case $TITLE:
			return new GenericEditor.CMEval()
			{
				@Override
				public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException
				{ // title
					return str;
				}
			};
		case $LABEL:
			return new GenericEditor.CMEval()
			{
				@Override
				public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException
				{ // label
					return str;
				}
			};
		case $EXPRESSION:
			return new GenericEditor.CMEval()
			{
				@Override
				public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException
				{ // expression
					if (!(str instanceof String))
						throw new CMException("Bad type: " + ((str == null) ? "null" : str.getClass().getName()));
					if (((String) str).trim().length() == 0)
					{
						if (emptyOK)
							return "";
						throw new CMException("You must enter an expression!");
					}
					if (!CMath.isMathExpression((String) str))
						throw new CMException("Invalid mathematical expression.  Use numbers,+,-,*,/,(), and ? only.");
					return str;
				}
			};
		case $UNIQUE_QUEST_NAME:
			return new GenericEditor.CMEval()
			{
				@Override
				public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException
				{ // quest name
					if (!(str instanceof String))
						throw new CMException("Bad type: " + ((str == null) ? "null" : str.getClass().getName()));
					if (((String) str).trim().length() == 0)
					{
						if (emptyOK)
							return "";
						throw new CMException("You must enter a quest name!");
					}
					for (int i = 0; i < ((String) str).length(); i++)
					{
						if ((!Character.isLetterOrDigit(((String) str).charAt(i))) && (((String) str).charAt(i) != '_'))
							throw new CMException("Quest names may only contain letters, digits, or _ -- no spaces or special characters.");
					}

					if (CMLib.quests().fetchQuest(((String) str).trim()) != null)
						throw new CMException("A quest of that name already exists.  Enter another.");
					return ((String) str).trim();
				}
			};
		case $CHOOSE:
			return new GenericEditor.CMEval()
			{
				@Override
				public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException
				{ // choose
					if ((choices == null) || (choices.length == 0))
						throw new CMException("NO choices?!");
					if (!(str instanceof String))
						throw new CMException("Bad type: " + ((str == null) ? "null" : str.getClass().getName()));
					if (((String) str).trim().length() == 0)
					{
						if (emptyOK)
							return "";
						throw new CMException("You must enter a value!");
					}
					final int x = CMParms.indexOf(choices, ((String) str).toUpperCase().trim());
					if (x < 0)
						throw new CMException("That is not a valid option.  Choices include: " + CMParms.toListString(choices));
					return choices[x];
				}
			};
		case $ITEMXML:
			return new GenericEditor.CMEval()
			{
				@Override
				public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException
				{ // itemxml
					if ((choices == null) || (choices.length == 0))
						throw new CMException("NO choices?!");
					if (!(str instanceof String))
						throw new CMException("Bad type: " + ((str == null) ? "null" : str.getClass().getName()));
					StringBuffer choiceNames = new StringBuffer("");
					for (final Object choice : choices)
						choiceNames.append(((Environmental) choice).Name() + ", ");
					if (choiceNames.toString().endsWith(", "))
						choiceNames = new StringBuffer(choiceNames.substring(0, choiceNames.length() - 2));
					if (((String) str).trim().length() == 0)
					{
						if (emptyOK)
							return "";
						throw new CMException("You must enter one of the following: " + choiceNames.toString());
					}
					final Environmental[] ES = new Environmental[choices.length];
					for (int e = 0; e < choices.length; e++)
						ES[e] = (Environmental) choices[e];
					final Environmental E = CMLib.english().fetchEnvironmental(Arrays.asList(ES), (String) str, false);
					if (E == null)
						throw new CMException("'" + str + "' was not found.  You must enter one of the following: " + choiceNames.toString());
					return CMLib.english().getContextName(ES, E);
				}
			};
		case $STRING:
			return new GenericEditor.CMEval()
			{
				@Override
				public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException
				{ // string
					if (!(str instanceof String))
						throw new CMException("Bad type: " + ((str == null) ? "null" : str.getClass().getName()));
					if (((String) str).trim().length() == 0)
					{
						if (emptyOK)
							return "";
						throw new CMException("You must enter a value!");
					}
					return str;
				}
			};
		case $ROOMID:
			return new GenericEditor.CMEval()
			{
				@Override
				public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException
				{ // roomid
					if (!(str instanceof String))
						throw new CMException("Bad type: " + ((str == null) ? "null" : str.getClass().getName()));
					if (((String) str).trim().length() == 0)
					{
						if (emptyOK)
							return "";
						throw new CMException("You must enter an room id(s), name(s), keyword ANY, or ANY MASK=...");
					}
					if (((String) str).trim().equalsIgnoreCase("ANY"))
						return ((String) str).trim();
					if (((String) str).trim().toUpperCase().startsWith("ANY MASK="))
						return str;
					if (CMStrings.contains(Quest.ROOM_REFERENCE_QCODES, ((String) str).toUpperCase().trim()))
						return ((String) str).toUpperCase().trim();
					if ((((String) str).indexOf(' ') > 0) && (((String) str).indexOf('\"') < 0))
						throw new CMException(
								"Multiple-word room names/ids must be grouped with double-quotes.  If this represents several names, put each name in double-quotes as so: \"name1\" \"name2\" \"multi word name\".");
					final Vector<String> V = CMParms.parse((String) str);
					if (V.size() == 0)
					{
						if (emptyOK)
							return "";
						throw new CMException("You must enter an room id(s), name(s), keyword ANY, or ANY MASK=...");
					}
					String s = null;
					for (int v = 0; v < V.size(); v++)
					{
						s = V.elementAt(v);
						boolean found = false;
						final Room R = CMLib.map().getRoom(s);
						if (R != null)
							found = true;
						if (!found)
							found = CMLib.map().findWorldRoomLiberally(null, s, "R", 50, 30000) != null;
						if (!found)
							throw new CMException("'" + (V.elementAt(v)) + "' is not a valid room name, id, or description.");
					}
					return str;
				}
			};
		case $AREA:
			return new GenericEditor.CMEval()
			{
				@Override
				public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException
				{ // area
					if (!(str instanceof String))
						throw new CMException("Bad type: " + ((str == null) ? "null" : str.getClass().getName()));
					if (((String) str).trim().length() == 0)
					{
						if (emptyOK)
							return "";
						throw new CMException("You must enter an area name(s), keyword ANY, or ANY MASK=...");
					}
					if (((String) str).trim().equalsIgnoreCase("ANY"))
						return ((String) str).trim();
					if (((String) str).trim().toUpperCase().startsWith("ANY MASK="))
						return str;
					if ((((String) str).indexOf(' ') > 0) && (((String) str).indexOf('\"') < 0))
						throw new CMException(
								"Multiple-word area names/ids must be grouped with double-quotes.  If this represents several names, put each name in double-quotes as so: \"name1\" \"name2\" \"multi word name\".");
					final Vector<String> V = CMParms.parse((String) str);
					if (V.size() == 0)
					{
						if (emptyOK)
							return "";
						throw new CMException("You must enter an area name(s), keyword ANY, or ANY MASK=...");
					}
					final StringBuffer returnStr = new StringBuffer("");
					for (int v = 0; v < V.size(); v++)
					{
						final Area A = CMLib.map().findArea(V.elementAt(v));
						if (A == null)
							throw new CMException("'" + (V.elementAt(v)) + "' is not a valid area name.");
						returnStr.append("\"" + A.name() + "\" ");
					}
					return returnStr.toString().trim();
				}
			};
		case $MOBXML:
			return new GenericEditor.CMEval()
			{
				@Override
				public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException
				{ // mobxml
					if ((choices == null) || (choices.length == 0))
						throw new CMException("NO choices?!");
					if (!(str instanceof String))
						throw new CMException("Bad type: " + ((str == null) ? "null" : str.getClass().getName()));
					StringBuffer choiceNames = new StringBuffer("");
					for (final Object choice : choices)
						choiceNames.append(((Environmental) choice).Name() + ", ");
					if (choiceNames.toString().endsWith(", "))
						choiceNames = new StringBuffer(choiceNames.substring(0, choiceNames.length() - 2));
					if (((String) str).trim().length() == 0)
					{
						if (emptyOK)
							return "";
						throw new CMException("You must enter one of the following: " + choiceNames.toString());
					}
					final Environmental[] ES = new Environmental[choices.length];
					for (int e = 0; e < choices.length; e++)
						ES[e] = (Environmental) choices[e];
					final Environmental E = CMLib.english().fetchEnvironmental(Arrays.asList(ES), (String) str, false);
					if (E == null)
						throw new CMException("'" + str + "' was not found.  You must enter one of the following: " + choiceNames.toString());
					return CMLib.english().getContextName(ES, E);
				}
			};
		case $NAME:
			return new GenericEditor.CMEval()
			{
				@Override
				public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException
				{ // designame
					if (!(str instanceof String))
						throw new CMException("Bad type: " + ((str == null) ? "null" : str.getClass().getName()));
					if (((String) str).trim().length() == 0)
					{
						if (emptyOK)
							return "";
						throw new CMException("You must enter a value!");
					}
					if (((String) str).trim().equalsIgnoreCase("ANY"))
						return ((String) str).trim();
					if (((String) str).trim().toUpperCase().startsWith("ANY MASK="))
						return str;
					if ((((String) str).indexOf(' ') > 0) && (((String) str).indexOf('\"') < 0))
						throw new CMException("Multiple-word names must be grouped with double-quotes.  If this represents several names, put each name in double-quotes as so: \"name1\" \"name2\" \"multi word name\".");
					return str;
				}
			};
		case $LONG_STRING:
			return new GenericEditor.CMEval()
			{
				@Override
				public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException
				{ // string
					if (!(str instanceof String))
						throw new CMException("Bad type: " + ((str == null) ? "null" : str.getClass().getName()));
					if (((String) str).trim().length() == 0)
					{
						if (emptyOK)
							return "";
						throw new CMException("You must enter a value!");
					}
					str = CMStrings.replaceAll((String) str, "\n\r", " ");
					str = CMStrings.replaceAll((String) str, "\r\n", " ");
					str = CMStrings.replaceAll((String) str, "\n", " ");
					str = CMStrings.replaceAll((String) str, "\r", " ");
					return str;
				}
			};
		case $MOBXML_ONEORMORE:
			return new GenericEditor.CMEval()
			{
				@Override
				public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException
				{ // mobxml_1ormore
					final GenericEditor.CMEval evaler = getQuestCommandEval(QMCommand.$MOBXML);
					return evaler.eval(str, choices, emptyOK);
				}
			};
		case $ITEMXML_ONEORMORE:
			return new GenericEditor.CMEval()
			{
				@Override
				public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException
				{ // itemxml_1ormore
					final GenericEditor.CMEval evaler = getQuestCommandEval(QMCommand.$ITEMXML);
					return evaler.eval(str, choices, emptyOK);
				}
			};
		case $ZAPPERMASK:
			return new GenericEditor.CMEval()
			{
				@Override
				public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException
				{ // zappermask
					if (!(str instanceof String))
						throw new CMException("Bad type: " + ((str == null) ? "null" : str.getClass().getName()));
					final Vector<String> errors = new Vector<String>(1);
					if (!CMLib.masking().syntaxCheck((String) str, errors))
						throw new CMException("Mask Error: " + CMParms.toListString(errors));
					return str;
				}
			};
		case $ABILITY:
			return new GenericEditor.CMEval()
			{
				@Override
				public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException
				{ // ability
					if (!(str instanceof String))
						throw new CMException("Bad type: " + ((str == null) ? "null" : str.getClass().getName()));
					final StringBuffer list = new StringBuffer("");
					for (final Enumeration<Ability> e = CMClass.abilities(); e.hasMoreElements();)
						list.append(e.nextElement().ID() + ", ");
					if (((String) str).trim().length() == 0)
					{
						if (emptyOK)
							return "";
						throw new CMException("You must enter an ability ID, choose from the following: " + list.toString());
					}
					Ability A = CMClass.getAbility((String) str);
					if (A == null)
						A = CMClass.findAbility((String) str);
					if ((A.classificationCode() & Ability.ALL_DOMAINS) == Ability.DOMAIN_ARCHON)
						A = null;
					if (A == null)
						throw new CMException("Invalid ability id, choose from the following: " + list.toString());
					return A.ID();
				}
			};
		case $EXISTING_QUEST_NAME:
			return new GenericEditor.CMEval()
			{
				@Override
				public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException
				{ // existing quest name
					if (!(str instanceof String))
						throw new CMException("Bad type: " + ((str == null) ? "null" : str.getClass().getName()));
					if (((String) str).trim().length() == 0)
					{
						if (emptyOK)
							return "";
						throw new CMException("You must enter a quest name!");
					}
					final Quest Q = CMLib.quests().fetchQuest(((String) str).trim());
					if (Q == null)
						throw new CMException("A quest of the name '" + ((String) str).trim() + "' does not exist.  Enter another.");
					return Q.name();
				}
			};
		case $HIDDEN:
			return new GenericEditor.CMEval()
			{
				@Override
				public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException
				{ // hidden
					return str;
				}
			};
		case $FACTION:
			return new GenericEditor.CMEval()
			{
				@Override
				public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException
				{ // faction
					if (!(str instanceof String))
						throw new CMException("Bad type: " + ((str == null) ? "null" : str.getClass().getName()));
					if (((String) str).trim().length() == 0)
					{
						if (emptyOK)
							return "";
						throw new CMException("You must enter a faction id!");
					}
					final Faction F = CMLib.factions().getFaction((String) str);
					if (F == null)
						throw new CMException("A faction of the name '" + ((String) str).trim() + "' does not exist.  Enter another.");
					return F.factionID();
				}
			};
		case $TIMEEXPRESSION:
			return new GenericEditor.CMEval()
			{
				@Override
				public Object eval(Object str, Object[] choices, boolean emptyOK) throws CMException
				{ // timeexpression
					if (!(str instanceof String))
						throw new CMException("Bad type: " + ((str == null) ? "null" : str.getClass().getName()));
					if (((String) str).trim().length() == 0)
					{
						if (emptyOK)
							return "";
						throw new CMException("You must enter an expression!");
					}
					if (!CMLib.time().isTickExpression((String) str))
						throw new CMException("Invalid time mathematical expression.  Use numbers,+,-,*,/,(), and ? only.  You may add ticks, minutes, hours, days, mudhours, muddays, mudweeks, mudmonths, mudyears.");
					return str;
				}
			};
		}
		return null;
	}
}
