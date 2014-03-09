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
public class Quests extends StdLibrary implements QuestManager
{
	public String ID(){return "Quests";}
	protected String holidayFilename="quests/holidays/holidays.quest";
	protected String holidayDefinition="LOAD="+holidayFilename;
	protected SVector<Quest> quests=new SVector<Quest>();
	
	public Quest objectInUse(Environmental E)
	{
		if(E==null) return null;
		for(int q=0;q<numQuests();q++)
		{
			Quest Q=fetchQuest(q);
			if(Q.isObjectInUse(E)) return Q;
		}
		return null;
	}

	public int numQuests(){return quests.size();}
	public Quest fetchQuest(int i){
		try{
			return quests.elementAt(i);
		}catch(Exception e){}
		return null;
	}
	public Quest fetchQuest(String qname)
	{
		for(int i=0;i<numQuests();i++)
		{
			Quest Q=fetchQuest(i);
			if(Q.name().equalsIgnoreCase(qname))
				return Q;
		}
		return null;
	}
	public Quest findQuest(String qname)
	{
		Quest Q=fetchQuest(qname);
		if(Q!=null) return Q;
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
	
	public void addQuest(Quest Q)
	{
		if(!quests.contains(Q))
		{
			quests.addElement(Q);
			Q.autostartup();
		}
	}
	public boolean shutdown()
	{
		for(int i=numQuests();i>=0;i--)
		{
			Quest Q=fetchQuest(i);
			delQuest(Q);
		}
		quests.clear();
		return true;
	}
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
	public void save()
	{
		CMLib.database().DBUpdateQuests(quests);
	}

	public Enumeration<Quest> enumQuests()
	{
		return quests.elements();
	}
	
	public Object getHolidayFile()
	{
		Quest Q=fetchQuest("holidays");
		if((Q==null)
		||(!Q.script().toUpperCase().trim().equalsIgnoreCase(holidayDefinition)))
		{
			Q=null;
			CMFile lF=new CMFile("//"+Resources.makeFileResourceName(holidayFilename),null);
			CMFile vF=new CMFile("::"+Resources.makeFileResourceName(holidayFilename),null);
			if((lF.exists())&&(!vF.exists())&&(lF.canRead())&&(vF.canWrite()))
			{
				byte[] O=lF.raw();
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
		CMFile F=new CMFile(Resources.makeFileResourceName(holidayFilename),null);
		if((!F.exists())||(!F.canRead())||(!F.canWrite()))
		{
			return "The file '"+Resources.makeFileResourceName(holidayFilename)+"' does not exist, and is required for this feature.";
		}
		List<String> V=Resources.getFileLineVector(F.text());
		List<String> steps=parseQuestSteps(V,0,true);
		return steps;
	}
	
	@SuppressWarnings("unchecked")
	public String listHolidays(Area A, String otherParms)
	{
		Object resp=getHolidayFile();
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
		StringBuffer str=new StringBuffer("^xDefined Quest Holidays^?\n\r");
		List<String> line=null;
		String var=null;
		List<String> V=null;
		str.append("^H#  "+CMStrings.padRight("Holiday Name",20)+CMStrings.padRight("Area Name(s)",50)+"^?\n\r");
		for(int s=1;s<steps.size();s++)
		{
			String step=steps.get(s);
			V=Resources.getFileLineVector(new StringBuffer(step));
			List<List<String>> cmds=CMLib.quests().parseQuestCommandLines(V,"SET",0);
			List<String> areaLine=null;
			List<String> nameLine=null;
			for(int v=0;v<cmds.size();v++)
			{
				line=cmds.get(v);
				if(line.size()>1)
				{
					var=line.get(1).toUpperCase();
					if(var.equals("AREAGROUP"))
					{ areaLine=line;}
					if(var.equals("NAME"))
					{ nameLine=line;}
				}
			}
			if(nameLine!=null)
			{
				boolean contains=true;//(areaName==null);
				if(areaLine!=null)
				{
					if((!contains) && (areaName != null))
					for(int l=2;l<areaLine.size();l++)
						if(areaName.equalsIgnoreCase(areaLine.get(l)))
						{	contains=true; break;}
				}
				else
				{
					areaLine=new XVector<String>("","","*special*");
					contains=true;
				}
				if(contains)
				{
					String name=CMParms.combine(nameLine,2);
					str.append(CMStrings.padRight(""+s,3)+CMStrings.padRight(name,20)+CMStrings.padRight(CMParms.combineWithQuotes(areaLine,2),30)+"\n\r");
				}
			}
		}
		return str.toString();
	}
	
	
	protected void promptText(MOB mob, DVector sets, String var, int showNumber, int showFlag, String prompt, String help, boolean emptyOK)
	throws java.io.IOException
	{
		int index=sets.indexOf(var);
		String oldVal=index>=0?(String)sets.elementAt(index,2):"";
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			String newVAL=CMLib.genEd().prompt(mob,oldVal,showNumber,showFlag,prompt,emptyOK);
			if(newVAL.equals("?"))
			{
				mob.tell(help);
				continue;
			}
			else
			if(index>=0)
				sets.setElementAt(index,2,newVAL);
			else
			if(!newVAL.equals(oldVal))
				sets.addElement(var,newVAL,Integer.valueOf(-1));
			break;
		}
	}

	@SuppressWarnings("unchecked")
	public String createHoliday(String named, String areaName, boolean save)
	{
		Object resp=getHolidayFile();
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
			List<String> stepV=Resources.getFileLineVector(new StringBuffer(step));
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
						String str=CMParms.combine(lineV,2);
						if(str.equalsIgnoreCase(named))
							return "A quest called '"+named+"' already exists.  Better to pick a new name or modify the existing one.";
					}
				}
			}
		}
		if(save)
		{
			CMFile F=new CMFile(Resources.makeFileResourceName(holidayFilename),null);
			F.saveText(getDefaultHoliData(named,areaName),true);
			Quest Q=fetchQuest("holidays");
			if(Q!=null) Q.setScript(holidayDefinition,true);
		}
		return "";
	}
	
	public StringBuffer getDefaultHoliData(String named, String area)
	{
		StringBuffer newHoliday=new StringBuffer("");
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
	
	@SuppressWarnings("unchecked")
	public String deleteHoliday(int holidayNumber)
	{
		Object resp=getHolidayFile();
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
		StringBuffer buf=new StringBuffer("");
		steps.remove(holidayNumber);
		for(int v=0;v<steps.size();v++)
		{
			step=steps.get(v);
			buf.append(step+"\n\r");
		}
		CMFile F=new CMFile(Resources.makeFileResourceName(holidayFilename),null);
		F.saveText(buf);
		Quest Q=fetchQuest("holidays");
		if(Q!=null) Q.setScript(holidayDefinition,true);
		return "Holiday deleted.";
	}

	@SuppressWarnings("unchecked")
	public String getHolidayName(int index)
	{
		Object resp=getHolidayFile();
		if(resp instanceof String){ return "";}
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
		List<String> stepV=Resources.getFileLineVector(new StringBuffer(step));
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
	
	@SuppressWarnings("unchecked")
	public int getHolidayIndex(String named)
	{
		Object resp=getHolidayFile();
		if(resp instanceof String){ return -1;}
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
			List<String> stepV=Resources.getFileLineVector(new StringBuffer(step));
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
						String str=CMParms.combine(lineV,2);
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
			if(V.get(v).toUpperCase().trim().startsWith(start))
				return v;
		return -1;
	}
	
	public RawHolidayData getEncodedHolidayData(String dataFromStepsFile)
	{
		List<String> stepV=Resources.getFileLineVector(new StringBuffer(dataFromStepsFile));
		for(int v=0;v<stepV.size();v++)
			stepV.set(v,CMStrings.replaceAll(stepV.get(v),"\\;",";"));
		DVector settings=new DVector(3);
		DVector behaviors=new DVector(3);
		DVector properties=new DVector(3);
		DVector stats=new DVector(3);
		RawHolidayData encodedData=new RawHolidayData();
		encodedData.settings=settings;
		encodedData.behaviors=behaviors;
		encodedData.properties=properties;
		encodedData.stats=stats;
		encodedData.stepV=stepV;
		Vector<String> lineV=null;
		String line=null;
		String var=null;
		String cmd=null;
		int pricingMobIndex=-1;
		String[] SETTINGS={"NAME","WAIT","DATE","DURATION","MUDDAY","AREAGROUP","MOBGROUP"};
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
					if(!settings.contains(var))
					{
						String str=CMParms.combineWithQuotes(lineV,2);
						if(str.toUpperCase().startsWith("ANY ")) str=str.substring(4);
						if(str.toUpperCase().startsWith("RESELECT MASK=")) str=str.substring(14);
						if(str.toUpperCase().startsWith("MASK=")) str=str.substring(5);
						settings.addElement(var,str,Integer.valueOf(v));
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
					behaviors.addElement(var,CMParms.combineWithQuotes(lineV,3),Integer.valueOf(v));
				}
				if(cmd.equals("GIVE")&&("AFFECT".equalsIgnoreCase(var))&&(lineV.size()>2)&&(pricingMobIndex<0))
				{
					var=lineV.elementAt(2).toUpperCase();
					properties.addElement(var,CMParms.combineWithQuotes(lineV,3),Integer.valueOf(v));
				}
				if(cmd.equals("GIVE")&&("STAT".equalsIgnoreCase(var))&&(lineV.size()>2))
				{
					var=lineV.elementAt(2).toUpperCase();
					if((pricingMobIndex<0)||(var.equals("PRICEMASKS")))
						stats.addElement(var,CMParms.combineWithQuotes(lineV,3),Integer.valueOf(v));
				}
			}
		}
		encodedData.pricingMobIndex=Integer.valueOf(pricingMobIndex);
		return encodedData;
	}
	
	@SuppressWarnings("unchecked")
	public void modifyHoliday(MOB mob, int holidayNumber)
	{
		Object resp=getHolidayFile();
		if(resp instanceof String)
		{ mob.tell((String)resp); return;}
		List<String> steps=null;
		if(resp instanceof List)
			steps=(List<String>)resp;
		else
		{ mob.tell("Unknown error."); return;}
		if((holidayNumber<=0)||(holidayNumber>=steps.size()))
		{ mob.tell(holidayNumber+" does not exist as a holiday -- enter LIST HOLIDAYS."); return;}

		String step=steps.get(holidayNumber);
		RawHolidayData encodedData=getEncodedHolidayData(step);
		DVector settings=encodedData.settings;
		DVector behaviors=encodedData.behaviors;
		DVector properties=encodedData.properties;
		DVector stats=encodedData.stats;
		
		int oldNameIndex=settings.indexOf("NAME");
		if((mob.isMonster())||(oldNameIndex<0)) 
			return;
		String oldName=(String)settings.elementAt(oldNameIndex,2);
		boolean ok=false;
		int showFlag=-1;
		if(CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0)
			showFlag=-999;
		try{
			while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
			{
				int showNumber=0;
				promptText(mob,settings,"NAME",++showNumber,showFlag,"Holiday Name","It's, well, a name.",false);
				showNumber=promptDuration(mob,settings,showNumber,showFlag);
				if(settings.indexOf("AREAGROUP")>=0)
					promptText(mob,settings,"AREAGROUP",++showNumber,showFlag,"Areas List (?)","Area names are space separated, and words grouped using double-quotes",false);
				if(settings.indexOf("MOBGROUP")>=0)
					promptText(mob,settings,"MOBGROUP",++showNumber,showFlag,"Mask for mobs that apply (?)",CMLib.masking().maskHelp("\n\r","disallow"),false);
				promptText(mob,properties,"MOOD",++showNumber,showFlag,"Mood setting (?)","NULL/Empty (to not use a Mood), or one of: FORMAL, POLITE, HAPPY, SAD, ANGRY, RUDE, MEAN, PROUD, GRUMPY, EXCITED, SCARED, LONELY",true);
				promptText(mob,behaviors,"AGGRESSIVE",++showNumber,showFlag,"Aggressive setting (?)",CMLib.help().getHelpText("Aggressive",mob,true)+"\n\r\n\r** NULL/Empty (to not use Aggressive **",true);
				showNumber=genPricing(mob,stats,++showNumber,showFlag);
				showNumber=genMudChat(mob,"MUDCHAT",behaviors,++showNumber,showFlag);
				showNumber=genBehaviors(mob,behaviors,++showNumber,showFlag);
				showNumber=genProperties(mob,properties,++showNumber,showFlag);
				if(showFlag<-900){ ok=true; break;}
				if(showFlag>0){ showFlag=-1; continue;}
				showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
				if(showFlag<=0)
				{
					showFlag=-1;
					ok=true;
				}
			}
		}catch(java.io.IOException e){return;}
		if(ok)
		{
			String err=alterHoliday(oldName, encodedData);
			if(err.length()==0)
				mob.tell("Holiday modified.");
			else
				mob.tell(err);
		}
	}

	@SuppressWarnings("unchecked")
	public String alterHoliday(String oldName, RawHolidayData newData)
	{
		DVector settings=newData.settings;
		DVector behaviors=newData.behaviors;
		DVector properties=newData.properties;
		DVector stats=newData.stats;
		//List stepV=(List)data.elementAt(4);
		int pricingMobIndex=newData.pricingMobIndex.intValue();
		
		int holidayNumber=getHolidayIndex(oldName);
		Object resp=getHolidayFile();
		if(resp instanceof String) return (String)resp;
		List<String> steps=null;
		if(resp instanceof List)
			steps=(List<String>)resp;
		else
			return "Unknown error.";
		
		String step = null;
		List<String> stepV = null;
		RawHolidayData encodedData = null;
		StringBuffer buf=new StringBuffer("");
		for(int v=0;v<steps.size();v++)
		{
			step=steps.get(v);
			if(v==holidayNumber)
			{
				encodedData=getEncodedHolidayData(step);
				DVector oldBehaviors=encodedData.behaviors.copyOf();
				DVector oldProperties=encodedData.properties.copyOf();
				stepV=encodedData.stepV;
				
				int index=startLineIndex(stepV,"SET NAME");
				stepV.set(index,"SET NAME "+(String)settings.elementAt(settings.indexOf("NAME"),2));
				index=startLineIndex(stepV,"SET DURATION");
				stepV.set(index,"SET DURATION "+(String)settings.elementAt(settings.indexOf("DURATION"),2));
				int intervalLine=startLineIndex(stepV,"SET MUDDAY");
				if(intervalLine<0) intervalLine=startLineIndex(stepV,"SET DATE");
				if(intervalLine<0) intervalLine=startLineIndex(stepV,"SET WAIT");
				int mudDayIndex=settings.indexOf("MUDDAY");
				int dateIndex=settings.indexOf("DATE");
				int waitIndex=settings.indexOf("WAIT");
				if(mudDayIndex>=0)
					stepV.set(intervalLine,"SET MUDDAY "+((String)settings.elementAt(mudDayIndex,2)));
				else
				if(dateIndex>=0)
					stepV.set(intervalLine,"SET DATE "+((String)settings.elementAt(dateIndex,2)));
				else
					stepV.set(intervalLine,"SET WAIT "+((String)settings.elementAt(waitIndex,2)));
				
				index=settings.indexOf("AREAGROUP");
				if(index>=0)
				{
					index=startLineIndex(stepV,"SET AREAGROUP");
					if(index>=0)
						stepV.set(index,"SET AREAGROUP "+settings.elementAt(settings.indexOf("AREAGROUP"),2));
				}
				
				index=settings.indexOf("MOBGROUP");
				if(index>=0)
				{
					index=startLineIndex(stepV,"SET MOBGROUP");
					stepV.set(index,"SET MOBGROUP RESELECT MASK="+settings.elementAt(settings.indexOf("MOBGROUP"),2));
				}
				if((pricingMobIndex>0)&&(stats.indexOf("PRICEMASKS")>=0))
				{
					index=startLineIndex(stepV,"GIVE STAT PRICEMASKS");
					String s=(String)stats.elementAt(stats.indexOf("PRICEMASKS"),2);
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
				index=behaviors.indexOf("AGGRESSIVE");
				if(index>=0)
				{
					index=startLineIndex(stepV,"GIVE BEHAVIOR AGGRESSIVE");
					String s=(String)behaviors.elementAt(behaviors.indexOf("AGGRESSIVE"),2);
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
				index=behaviors.indexOf("MUDCHAT");
				if(index>=0)
				{
					index=startLineIndex(stepV,"GIVE BEHAVIOR MUDCHAT");
					String s=(String)behaviors.elementAt(behaviors.indexOf("MUDCHAT"),2);
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
				index=properties.indexOf("MOOD");
				if(index>=0)
				{
					index=startLineIndex(stepV,"GIVE AFFECT MOOD");
					String s=(String)properties.elementAt(properties.indexOf("MOOD"),2);
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
					String prop=(String)oldProperties.elementAt(p,1);
					if(properties.indexOf(prop)<0)
					{
						index=startLineIndex(stepV,"GIVE AFFECT "+prop);
						if(index>=0) stepV.remove(index);
					}
				}
				// look for newly missing stuff
				for(int p=0;p<oldBehaviors.size();p++)
				{
					String behav=(String)oldBehaviors.elementAt(p,1);
					if(behaviors.indexOf(behav)<0)
					{
						index=startLineIndex(stepV,"GIVE BEHAVIOR "+behav);
						if(index>=0) stepV.remove(index);
					}
				}
				// now changed/added stuff
				for(int p=0;p<properties.size();p++)
				{
					String prop=(String)properties.elementAt(p,1);
					if(prop.equalsIgnoreCase("MOOD")) continue;
					mobGroupIndex=startLineIndex(stepV,"SET MOBGROUP");
					index=startLineIndex(stepV,"GIVE AFFECT "+prop);
					if(index>=0) 
						stepV.set(index,"GIVE AFFECT "+prop.toUpperCase().trim()+" "+(properties.elementAt(p,2)));
					else
						stepV.add(mobGroupIndex+1,"GIVE AFFECT "+prop.toUpperCase().trim()+" "+(properties.elementAt(p,2)));
				}
				// now changed/added stuff
				for(int p=0;p<behaviors.size();p++)
				{
					String behav=(String)behaviors.elementAt(p,1);
					if(behav.equalsIgnoreCase("AGGRESSIVE")||behav.equalsIgnoreCase("MUDCHAT")) continue;
					mobGroupIndex=startLineIndex(stepV,"SET MOBGROUP");
					index=startLineIndex(stepV,"GIVE BEHAVIOR "+behav);
					if(index>=0) 
						stepV.set(index,"GIVE BEHAVIOR "+behav.toUpperCase().trim()+" "+(behaviors.elementAt(p,2)));
					else
						stepV.add(mobGroupIndex+1,"GIVE BEHAVIOR "+behav.toUpperCase().trim()+" "+(behaviors.elementAt(p,2)));
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
		CMFile F=new CMFile(Resources.makeFileResourceName(holidayFilename),null);
		F.saveText(buf);
		Quest Q=fetchQuest("holidays");
		if(Q!=null) Q.setScript(holidayDefinition,true);
		return "";
	}
	
	protected int promptDuration(MOB mob, DVector settings, int showNumber,int showFlag)
		throws IOException
	{
		int mudDayIndex=settings.indexOf("MUDDAY");
		int dateIndex=settings.indexOf("DATE");
		int waitIndex=settings.indexOf("WAIT");
		int durationIndex=settings.indexOf("DURATION");
		if(durationIndex<0)
		{
			settings.addElement("DURATION","900",Integer.valueOf(-1));
			durationIndex=settings.indexOf("DURATION");
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
					newVal=CMLib.genEd().prompt(mob,TYPES[typeIndex],showNumber,showFlag,"Schedule type",CMParms.toStringList(TYPES));
					if(CMParms.indexOf(TYPES,newVal.toUpperCase().trim())<0)
					{
						newVal="?";
						mob.tell("Not a valid entry.  Try ?");
						continue;
					}
					typeIndex=CMParms.indexOf(TYPES,newVal.toUpperCase().trim());
					if((typeIndex!=0)&&(waitIndex>=0))
						settings.removeElement("WAIT");
					if((typeIndex!=1)&&(mudDayIndex>=0))
						settings.removeElement("MUDDAY");
					if((typeIndex!=2)&&(dateIndex>=0))
						settings.removeElement("DATE");
					if((typeIndex==0)&&(waitIndex<0))
						settings.addElement("WAIT","100",Integer.valueOf(-1));
					if((typeIndex==1)&&(mudDayIndex<0))
						settings.addElement("MUDDAY","1-1",Integer.valueOf(-1));
					if((typeIndex==2)&&(dateIndex<0))
						settings.addElement("DATE","1-1",Integer.valueOf(-1));
					if(showFlag==showNumber)
						return showNumber;
					break;
				}
				mudDayIndex=settings.indexOf("MUDDAY");
				dateIndex=settings.indexOf("DATE");
				waitIndex=settings.indexOf("WAIT");
				durationIndex=settings.indexOf("DURATION");
			}
			else
				mob.tell(showNumber+". Schedule type: "+TYPES[typeIndex]);
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
	
	protected int genBehaviors(MOB mob, DVector behaviors, int showNumber, int showFlag)
	throws IOException
	{
		for(int b=0;b<=behaviors.size();b++)
		{
			if((b<behaviors.size())
			&&(((String)behaviors.elementAt(b,1)).equalsIgnoreCase("MUDCHAT")
				||((String)behaviors.elementAt(b,1)).equalsIgnoreCase("AGGRESSIVE")))
				continue;
			if((showFlag>0)&&(showFlag!=showNumber)){ if(b<behaviors.size()) showNumber++; continue;}
			if(b==behaviors.size())
			{
				if((mob.session()!=null)&&(!mob.session().isStopped()))
				{
					if(((showFlag<=-999)&&CMLib.genEd().prompt(mob,false,showNumber,showFlag,"Add new mob behavior"))
					||(showNumber==showFlag))
					{
						behaviors.addElement("BehaviorID","",Integer.valueOf(behaviors.size()));
						b-=1;
					}
					else
					if(showFlag==-1)
						mob.tell(showNumber+". Add new mob behavior");
				}
				continue;
			}
			String behavior=(String)behaviors.elementAt(b,1);
			String parms=(String)behaviors.elementAt(b,2);
			
			mob.tell(showNumber+". Behavior: "+behavior+": "+parms);
			if((showFlag==showNumber)||(showFlag<=-999))
			{
				behavior=CMLib.genEd().prompt(mob,behavior,showNumber,showFlag,"Behavior ID (NULL to delete)",true,toStringList(CMClass.behaviors()));
				if(behavior.length()==0)
				{
					behaviors.removeElementAt(b);
					b--;
					if((showFlag==showNumber)) break;
					showNumber--;
					continue;
				}
				if(CMClass.getBehavior(behavior)==null)
				{
					mob.tell("Behavior '"+behavior+"' does not exist.  Use ? for a list.");
					b--;
					showNumber--;
					continue;
				}
				StringBuilder help=CMLib.help().getHelpText(behavior,mob,true);
				if(help==null) help=new StringBuilder("No help on '"+behavior+"'");
				parms=CMLib.genEd().prompt(mob,parms,showNumber,showFlag,"Behavior Parameters",help.toString());
				behaviors.setElementAt(b,1,behavior);
				behaviors.setElementAt(b,2,parms);
			}
			showNumber++;
		}
		return showNumber;
	}
	
	protected int genProperties(MOB mob, DVector properties, int showNumber, int showFlag)
	throws IOException
	{
		for(int p=0;p<=properties.size();p++)
		{
			if((p<properties.size())
			&&(((String)properties.elementAt(p,1)).equalsIgnoreCase("MOOD")))
				continue;
			if((showFlag>0)&&(showFlag!=showNumber)){ if(p<properties.size()) showNumber++; continue;}
			if(p==properties.size())
			{
				if((mob.session()!=null)&&(!mob.session().isStopped()))
				{
					if(((showFlag<=-999)&&CMLib.genEd().prompt(mob,false,showNumber,showFlag,"Add new mob property"))
					||(showNumber==showFlag))
					{
						properties.addElement("AbilityID","",Integer.valueOf(properties.size()));
						p-=1;
					}
					else
					if(showFlag==-1)
						mob.tell(showNumber+". Add new mob property");
				}
				continue;
			}
			String propertyID=(String)properties.elementAt(p,1);
			String parms=(String)properties.elementAt(p,2);
			
			mob.tell(showNumber+". Effect: "+propertyID+": "+parms);
			if((showFlag==showNumber)||(showFlag<=-999))
			{
				propertyID=CMLib.genEd().prompt(mob,propertyID,showNumber,showFlag,"Ability ID (NULL to delete)",true,toStringList(CMClass.abilities()));
				if(propertyID.length()==0)
				{
					properties.removeElementAt(p);
					p--;
					if((showFlag==showNumber)) break;
					showNumber--;
					continue;
				}
				if(CMClass.getAbility(propertyID)==null)
				{
					mob.tell("Ability '"+propertyID+"' does not exist.  Use ? for a list.");
					p--;
					showNumber--;
					continue;
				}
				StringBuilder help=CMLib.help().getHelpText(propertyID,mob,true);
				if(help==null) help=new StringBuilder("No help on '"+propertyID+"'");
				parms=CMLib.genEd().prompt(mob,parms,showNumber,showFlag,"Ability Parameters",help.toString());
				properties.setElementAt(p,1,propertyID);
				properties.setElementAt(p,2,parms);
			}
			showNumber++;
		}
		return showNumber;
	}
	
	public static String toStringList(Enumeration<?> e)
	{
		if(!e.hasMoreElements()) return "";
		StringBuffer s=new StringBuffer("");
		Object o=null;
		for(;e.hasMoreElements();)
		{
			o=e.nextElement();
			if(o instanceof CMObject)
				s.append(", "+((CMObject)o).ID());
			else
				s.append(", "+o);
		}
		if(s.length()==0) return "";
		return s.toString().substring(2);
	}

	protected int genPricing(MOB mob, DVector stats, int showNumber, int showFlag)
	throws IOException
	{
		int pndex=stats.indexOf("PRICEMASKS");
		String priceStr=(pndex<0)?"":(String)stats.elementAt(pndex,2);
		List<String> priceV=CMParms.parseCommas(priceStr,true);
		for(int v=0;v<=priceV.size();v++)
		{
			if((showFlag>0)&&(showFlag!=showNumber)){ if(v<priceV.size())showNumber++; continue;}
			if(v==priceV.size())
			{
				if((mob.session()!=null)&&(!mob.session().isStopped()))
				{
					if(((showFlag<=-999)&&CMLib.genEd().prompt(mob,false,showNumber,showFlag,"Add new price factor"))
					||(showNumber==showFlag))
					{
						priceV.add("1.0");
						v-=1;
					}
					else
					if(showFlag==-1)
						mob.tell(showNumber+". Add new price factor.");
				}
				continue;
			}
			String priceLine=priceV.get(v);
			double priceFactor=0.0;
			String mask="";
			int x=priceLine.indexOf(' ');
			if(x<0)
				priceFactor=CMath.s_double(priceLine);
			else
			{
				priceFactor=CMath.s_double(priceLine.substring(0,x));
				mask=priceLine.substring(x+1).trim();
			}
			mob.tell(showNumber+". Price Factor: "+Math.round(priceFactor*100.0)+"%: "+mask);
			if((showFlag==showNumber)||(showFlag<=-999))
			{
				priceFactor=CMLib.genEd().prompt(mob,priceFactor,showNumber,showFlag,"Price Factor (enter 0 to delete)");
				if(priceFactor==0.0)
				{
					priceV.remove(v);
					v--;
					if((showFlag==showNumber)) break;
					showNumber--;
					continue;
				}
				mob.tell(showNumber+". Price Factor: "+Math.round(priceFactor*100.0)+"%: "+mask);
				mask=CMLib.genEd().prompt(mob,mask,showNumber,showFlag,"Item mask for this price",CMLib.masking().maskHelp("\n\r","disallow"));
				priceV.set(v,priceFactor+" "+mask);
			}
			showNumber++;
		}
		String newVal=CMParms.toStringList(priceV);
		if(pndex>=0)
			stats.setElementAt(pndex,2,newVal);
		else
			stats.addElement("PRICEMASKS",newVal,Integer.valueOf(stats.size()));
		return showNumber;
	}

	public String breakOutMaskString(String s, List<String> p)
	{
		String mask="";
		int x=s.toUpperCase().lastIndexOf("MASK=");
		if(x>=0)
		{
			mask=s.substring(x+5).trim();
			int i=0;
			while((i<p.size())&&(p.get(i).toUpperCase().indexOf("MASK=")<0))i++;
			if(i<=p.size())
			{
				String pp=p.get(i);
				x=pp.toUpperCase().indexOf("MASK=");
				if((x>0)&&(pp.substring(0,x).trim().length()>0))
				{
					p.set(i,pp.substring(0,x).trim());
					i++;
				}
				while(i<p.size()) p.remove(i);
			}
		}
		return mask.trim();
	}
	
	public List<List<String>> breakOutMudChatVs(String MUDCHAT, DVector behaviors) 
	{
		int mndex=behaviors.indexOf(MUDCHAT);
		String mudChatStr=(mndex<0)?"":(String)behaviors.elementAt(mndex,2);
		if(mudChatStr.startsWith("+")) mudChatStr=mudChatStr.substring(1);
		List<String> rawMCV=CMParms.parseSemicolons(mudChatStr,true);
		List<List<String>> mudChatV=new Vector<List<String>>();
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
		if(V.size()==0) mudChatV.remove(V);
		return mudChatV;
	}
	
	protected int genMudChat(MOB mob, String var, DVector behaviors, int showNumber, int showFlag)
	throws IOException
	{
		int mndex=behaviors.indexOf(var);
		List<List<String>> mudChatV = breakOutMudChatVs(var,behaviors);
		List<String> V = null;
		String s=null;
		for(int v=0;v<=mudChatV.size();v++)
		{
			if((showFlag>0)&&(showFlag!=showNumber)){ if(v<mudChatV.size())showNumber++; continue;}
			if(v==mudChatV.size())
			{
				if((mob.session()!=null)&&(!mob.session().isStopped()))
				{
					if(((showFlag<=-999)&&CMLib.genEd().prompt(mob,false,showNumber,showFlag,"Add new mud chat"))
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
						mob.tell(showNumber+". Add new mud chat.");
				}
				continue;
			}
			V=mudChatV.get(v);
			String words=V.get(0);
			mob.tell(showNumber+". MudChat for words: "+words);
			if((showFlag==showNumber)||(showFlag<=-999))
			{
				words=CMLib.genEd().prompt(mob,words,showNumber,showFlag,"Enter matching words (| delimited, NULL to delete)\n\r",true);
				if(words.trim().length()==0)
				{
					mudChatV.remove(v);
					if((showFlag==showNumber)) break;
					showNumber--;
					continue;
				}
				V.set(0,words);
				for(int v1=1;v1<=V.size();v1++)
				{
					if(v1==V.size())
					{
						if((mob.session()!=null)&&(!mob.session().isStopped())
						&&(mob.session().confirm("Add another thing to say (y/N)","NO")))
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
						newStr=mob.session().prompt("Enter  # Weight + thing to say (?) '"+s+"'\n\r: ",s);
						if(newStr.equals("?"))
							mob.tell("Enter a number followed by a phrase to say like 9thingtosay. Enter NULL to delete this thing to say.");
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
		StringBuffer finalVal=new StringBuffer("");
		for(int v=0;v<mudChatV.size();v++)
		{
			V=mudChatV.get(v);
			if(V.size()==0) continue;
			finalVal.append("("+(V.get(0))+");");
			for(int v1=1;v1<V.size();v1++)
				finalVal.append((V.get(v1))+";");
			finalVal.append(";");
		}
		if(mndex>=0)
			behaviors.setElementAt(mndex,2,(finalVal.toString().trim().length()==0)?"":("+"+finalVal.toString()));
		else
			behaviors.addElement(var,(finalVal.toString().trim().length()==0)?"":("+"+finalVal.toString()),Integer.valueOf(behaviors.size()));
		return showNumber;
	}
	

	public List<List<String>> parseQuestCommandLines(List<?> script, String cmdOnly, int startLine)
	{
		Vector<String> line=null;
		String cmd=null;
		boolean inScript=false;
		List<List<String>> lines=new Vector<List<String>>();
		if(cmdOnly!=null) cmdOnly=cmdOnly.toUpperCase().trim();
		for(int v=startLine;v<script.size();v++)
		{
			line=CMParms.parse(((String)script.get(v)));
			if(line.size()==0) continue;
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
	
	public List<String> parseQuestSteps(List<String> script, int startLine, boolean rawLineInput)
	{
		Vector<String> line=null;
		String cmd=null;
		Vector<String> parsed=new Vector<String>();
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
			if(line.size()==0) continue;
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
 
	@SuppressWarnings("unchecked")
	public DVector getQuestTemplate(MOB mob, String fileToGet)
	{
		// user security doesn't matter, because this is read-only & system files.
		final int fileOpenFlag=CMFile.FLAG_LOGERRORS|(CMSecurity.isAllowedAnywhere(mob, CMSecurity.SecFlag.CMDQUESTS)?CMFile.FLAG_FORCEALLOW:0);
		CMFile tempF=new CMFile(Resources.makeFileResourceName("quests/templates"),null,fileOpenFlag);
		if((!tempF.exists())||(!tempF.isDirectory()))
			return null;
		CMFile[] files=tempF.listFiles();
		DVector templatesDV=new DVector(5);
		boolean parsePages=(fileToGet!=null)&&(!fileToGet.endsWith("*"));
		if((fileToGet!=null)&&(fileToGet.endsWith("*")))
			fileToGet=fileToGet.substring(0,fileToGet.length()-1);
		if(files.length==0) return null;
		for(int f=0;f<files.length;f++)
		{
			if((files[f].getName().toUpperCase().endsWith(".QUEST"))
			&&((fileToGet==null)||(files[f].getName().toUpperCase().startsWith(fileToGet.toUpperCase().trim()))))
			{
				List<String> V=Resources.getFileLineVector(files[f].text());
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
								String name=s.substring(23).trim();
								foundStart=true;
								script=new StringBuffer("");
								templatesDV.addElement(name,"",files[f].getName(),new Vector<Object>(),script);
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
								if(!parsePages) break;
								String name=s.substring(15).trim();
								pageDV=new DVector(4);
								pageDV.addElement(Integer.valueOf(QuestManager.QM_COMMAND_$TITLE),name,"","");
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
								int x=s.indexOf('=');
								if(x<0)
									Log.errOut("Quests","Illegal QuestMaker variable syntax: "+s);
								else
								if(pageDV==null)
									Log.errOut("Quests","QuestMaker syntax error, QUESTMAKER_PAGE not yet designated: "+s);
								else
								{
									int y=s.indexOf('=',x+1);
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
									int code=CMParms.indexOf(QuestManager.QM_COMMAND_TYPES,cmd);
									if(code<0)
									{
										Log.errOut("Quests","QuestMaker syntax error, '"+cmd+"' is an unknown command");
										pageDV.removeElementsAt(pageDV.size()-1);
									}
									else
										pageDV.setElementAt(pageDV.size()-1,1,Integer.valueOf(code|mask));
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
								||(((Integer)pageDV.elementAt(pageDV.size()-1,1)).intValue()==QuestManager.QM_COMMAND_$TITLE)
								||(((Integer)pageDV.elementAt(pageDV.size()-1,1)).intValue()==QuestManager.QM_COMMAND_$LABEL))
								{
									if(s.length()==0)
									{
										if(((Integer)pageDV.elementAt(pageDV.size()-1,1)).intValue()==QuestManager.QM_COMMAND_$TITLE)
											pageDV.addElement(Integer.valueOf(QuestManager.QM_COMMAND_$LABEL),"",s,"");
										else
											pageDV.setElementAt(pageDV.size()-1,3,((String)pageDV.elementAt(pageDV.size()-1,3))+"\n\r\n\r");
									}
									else
										pageDV.setElementAt(pageDV.size()-1,3,((String)pageDV.elementAt(pageDV.size()-1,3))+s+" ");
								}
								else
									pageDV.addElement(Integer.valueOf(QuestManager.QM_COMMAND_$LABEL),"",s,"");
							}
						}
					}
				}
				
			}
		}
		if(templatesDV.size()==0)
			return null;
		DVector sortedTemplatesDV=new DVector(5);
		while(templatesDV.size()>0)
		{
			int maxRow=0;
			for(int t=1;t<templatesDV.size();t++)
				if(((String)templatesDV.elementAt(t,1)).compareTo((String)templatesDV.elementAt(maxRow,1))<0)
					maxRow=t;
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
		Vector<MOB> choices=new Vector<MOB>();
		MOB baseM=((showValue!=null)?baseM=CMLib.coffeeMaker().getMobFromXML(showValue):null);
		StringBuffer choiceDescs=new StringBuffer("");
		if(baseM!=null){
			choices.addElement(baseM);
			choiceDescs.append(baseM.name()+", ");
		}
		Room R=mob.location();
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
		Vector<MOB> newMobs=new Vector<MOB>();
		for(Enumeration<MOB> e=CMClass.mobTypes();e.hasMoreElements();)
		{
			M=e.nextElement();
			if(M.isGeneric())
			{
				M=(MOB)M.copyOf();
				newMobs.add(M);
				M.setName("A NEW "+M.ID().toUpperCase());
				choices.add(M);
				choiceDescs.append(M.name()+", ");
			}
		}
		MOB canMOB=CMClass.getFactoryMOB();
		canMOB.setName("CANCEL");
		choiceDescs.append("CANCEL");
		choices.addElement(canMOB);
		String showName=showValue;
		if(baseM!=null) showName=CMLib.english().getContextName(choices,baseM);
		lastLabel=((lastLabel==null)?"":lastLabel)+"\n\rChoices: "+choiceDescs.toString();
		String s=CMLib.genEd().prompt(mob,showName,showNumber,showFlag,parm1Fixed,optionalEntry,false,lastLabel,
										QuestManager.QM_COMMAND_TESTS[QuestManager.QM_COMMAND_$MOBXML],
										choices.toArray());
		canMOB.destroy();
		if(s.equalsIgnoreCase("CANCEL")) return null;
		M=(MOB)CMLib.english().fetchEnvironmental(choices,s,false);
		if((M!=null)&&(newMobs.contains(M)))
		{
			Command C=CMClass.getCommand("Modify");
			if(C!=null) C.execute(mob,new XVector<Object>("MODIFY",M),0);
			// modify it!
		}
		String newValue=(M!=null)?CMLib.coffeeMaker().getMobXML(M).toString():showValue;
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
		List<Item> choices=new Vector<Item>();
		Item baseI=((showValue!=null)?baseI=CMLib.coffeeMaker().getItemFromXML(showValue):null);
		StringBuffer choiceDescs=new StringBuffer("");
		if(baseI!=null){
			choices.add(baseI);
			choiceDescs.append(baseI.name()+", ");
		}
		Room R=mob.location();
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
		List<String> allItemNames=new Vector<String>();
		CMClass.addAllItemClassNames(allItemNames,true,false,false);
		List<Item> newItems=new Vector<Item>();
		for(int a=0;a<allItemNames.size();a++)
		{
			I=CMClass.getItem(allItemNames.get(a));
			if((I!=null)&&(I.isGeneric()))
			{
				newItems.add(I);
				I.setName("A NEW "+I.ID().toUpperCase());
				choices.add(I);
				choiceDescs.append(I.name()+", ");
			}
		}
		choiceDescs.append("CANCEL");
		Item canItem=CMClass.getItem("StdItem");
		canItem.setName("CANCEL");
		choices.add(canItem);
		String showName=showValue;
		if(baseI!=null) showName=CMLib.english().getContextName(choices,baseI);
		lastLabel=((lastLabel==null)?"":lastLabel)+"\n\rChoices: "+choiceDescs.toString();
		String s=CMLib.genEd().prompt(mob,showName,showNumber,showFlag,parm1Fixed,optionalEntry,false,lastLabel,
										QuestManager.QM_COMMAND_TESTS[QuestManager.QM_COMMAND_$ITEMXML],
										choices.toArray());
		canItem.destroy();
		if(s.equalsIgnoreCase("CANCEL")) return null;
		I=(Item)CMLib.english().fetchEnvironmental(choices,s,false);
		if((I!=null)&&(newItems.contains(I)))
		{
			Command C=CMClass.getCommand("Modify");
			if(C!=null) C.execute(mob,new XVector<Object>("MODIFY",I),0);
			// modify it!
		}
		String newValue=(I!=null)?CMLib.coffeeMaker().getItemXML(I).toString():showValue;
		for(int n=0;n<newItems.size();n++) newItems.get(n).destroy();
		return (newValue==null)?"":newValue.trim();
	}
	
	public List<Quest> getPlayerPersistantQuests(MOB player)
	{
		Vector<Quest> qVec=new Vector<Quest>();
		for(int q=0;q<CMLib.quests().numQuests();q++)
		{
			Quest Q=CMLib.quests().fetchQuest(q);
			if(Q==null) continue;
			for(Enumeration<ScriptingEngine> e=player.scripts();e.hasMoreElements();)
			{
				ScriptingEngine SE=e.nextElement();
				if(SE==null) continue;
				if((SE.defaultQuestName().length()>0)
				&&(SE.defaultQuestName().equalsIgnoreCase(Q.name()))
				&&(!qVec.contains(Q)))
					qVec.addElement(Q);
			}
		}
		return qVec;
	}
	
	@SuppressWarnings("unchecked")
	public Quest questMaker(MOB mob)
	{
		if(mob.isMonster()) return null;
		DVector questTemplates=getQuestTemplate(mob,null);
		try
		{
			if((questTemplates==null)||(questTemplates.size()==0))
			{
				mob.tell("No valid quest templates found in resources/quests/templates!");
				return null;
			}
			int questIndex=-1;
			while((questIndex<0)&&(mob.session()!=null)&&(!mob.session().isStopped()))
			{
				String choice=mob.session().prompt("Select a quest template (?): ","");
				if(choice.equals("?"))
				{
					StringBuffer fullList=new StringBuffer("\n\r^HCANCEL^N -- to cancel.\n\r");
					for(int t=0;t<questTemplates.size();t++)
						fullList.append("^H"+(String)questTemplates.elementAt(t,1)+"^N\n\r"+(String)questTemplates.elementAt(t,2)+"\n\r");
					mob.tell(fullList.toString());
				}
				else
				if((choice.length()==0)||(choice.equalsIgnoreCase("CANCEL")))
					return null;
				else
				{
					StringBuffer list=new StringBuffer("");
					for(int t=0;t<questTemplates.size();t++)
						if(choice.equalsIgnoreCase((String)questTemplates.elementAt(t,1)))
							questIndex=t;
						else
							list.append(((String)questTemplates.elementAt(t,1))+", ");
					if(questIndex<0)
						mob.tell("'"+choice+"' is not a valid quest name, use ? for a list, or select from the following: "+list.toString().substring(0,list.length()-2));
				}
			}
			if((mob.session()==null)||(mob.session().isStopped())||(questIndex<0))
				return null;
			DVector qFDV=getQuestTemplate(mob,(String)questTemplates.elementAt(questIndex,3));
			if((qFDV==null)||(qFDV.size()==0)) return null;
			String questTemplateName=(String)qFDV.elementAt(0,1);
			Vector<Object> qPages=(Vector<Object>)qFDV.elementAt(0,4);
			mob.tell("^Z"+questTemplateName+"^.^N");
			mob.tell((String)qFDV.elementAt(0,2));
			for(int page=0;page<qPages.size();page++)
			{
				DVector pageDV=(DVector)qPages.elementAt(page);
				String pageName=(String)pageDV.elementAt(0,2);
				String pageInstructions=(String)pageDV.elementAt(0,3);
				mob.tell("\n\r\n\r^HPage #"+(page+1)+": ^N"+pageName);
				mob.tell("^N"+pageInstructions);
				boolean ok=false;
				int showFlag=-999;
				while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
				{
					int showNumber=0;
					String lastLabel=null;
					for(int step=1;step<pageDV.size();step++)
					{
						Integer stepType=(Integer)pageDV.elementAt(step,1);
						String keyName=(String)pageDV.elementAt(step,2);
						String defValue=(String)pageDV.elementAt(step,3);
						String parm1Fixed=CMStrings.capitalizeAndLower(keyName.replace('_',' '));
						if(parm1Fixed.startsWith("$")) parm1Fixed=parm1Fixed.substring(1);
						
						boolean optionalEntry=CMath.bset(stepType.intValue(),QuestManager.QM_COMMAND_OPTIONAL);
						int inputCode=stepType.intValue()&QuestManager.QM_COMMAND_MASK;
						switch(inputCode)
						{
						case QM_COMMAND_$TITLE: break;
						case QM_COMMAND_$HIDDEN: 
							pageDV.setElementAt(step,4,defValue==null?"":defValue);
							break;
						case QM_COMMAND_$LABEL: lastLabel=defValue; break;
						case QM_COMMAND_$EXPRESSION:
						case QM_COMMAND_$TIMEEXPRESSION:
						case QM_COMMAND_$UNIQUE_QUEST_NAME:
						case QM_COMMAND_$STRING:
						case QM_COMMAND_$LONG_STRING:
						case QM_COMMAND_$NAME:
						case QM_COMMAND_$ZAPPERMASK:
						case QM_COMMAND_$ROOMID:
						{
							String showValue=(showFlag<-900)?defValue:(String)pageDV.elementAt(step,4);
							if(inputCode==QM_COMMAND_$ZAPPERMASK)
								lastLabel=(lastLabel==null?"":lastLabel)+"\n\r"+CMLib.masking().maskHelp("\n\r","disallows");
							String s=CMLib.genEd().prompt(mob,showValue,++showNumber,showFlag,parm1Fixed,optionalEntry,false,lastLabel,
															QuestManager.QM_COMMAND_TESTS[inputCode],null);
							pageDV.setElementAt(step,4,s);
							break;
						}
						case QM_COMMAND_$AREA:
						{
							String showValue=(showFlag<-900)?"":(String)pageDV.elementAt(step,4);
							StringBuffer label=new StringBuffer(((lastLabel==null)?"":lastLabel)+"\n\rChoices: ");
							for(Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
								label.append("\""+e.nextElement().name()+"\" ");
							String s=CMLib.genEd().prompt(mob,showValue,++showNumber,showFlag,parm1Fixed,optionalEntry,false,label.toString(),
															QuestManager.QM_COMMAND_TESTS[inputCode],
															null);
							pageDV.setElementAt(step,4,s);
							break;
						}
						case QM_COMMAND_$EXISTING_QUEST_NAME:
						{
							String showValue=(showFlag<-900)?"":(String)pageDV.elementAt(step,4);
							StringBuffer label=new StringBuffer(((lastLabel==null)?"":lastLabel)+"\n\rChoices: ");
							for(int q=0;q<CMLib.quests().numQuests();q++)
								label.append("\""+CMLib.quests().fetchQuest(q).name()+"\" ");
							String s=CMLib.genEd().prompt(mob,showValue,++showNumber,showFlag,parm1Fixed,optionalEntry,false,label.toString(),
															QuestManager.QM_COMMAND_TESTS[inputCode],
															null);
							pageDV.setElementAt(step,4,s);
							break;
						}
						case QM_COMMAND_$ABILITY:
						{
							String showValue=(showFlag<-900)?"":(String)pageDV.elementAt(step,4);
							StringBuffer label=new StringBuffer(((lastLabel==null)?"":lastLabel)+"\n\rChoices: ");
							for(Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
								label.append(e.nextElement().ID()+" ");
							String s=CMLib.genEd().prompt(mob,showValue,++showNumber,showFlag,parm1Fixed,optionalEntry,false,label.toString(),
															QuestManager.QM_COMMAND_TESTS[inputCode],
															null);
							pageDV.setElementAt(step,4,s);
							break;
						}
						case QM_COMMAND_$CHOOSE:
						{
							String showValue=(showFlag<-900)?"":(String)pageDV.elementAt(step,4);
							String label=((lastLabel==null)?"":lastLabel)+"\n\rChoices: "+defValue;
							String s=CMLib.genEd().prompt(mob,showValue,++showNumber,showFlag,parm1Fixed,optionalEntry,false,label,
															QuestManager.QM_COMMAND_TESTS[inputCode],
															CMParms.toStringArray(CMParms.parseCommas(defValue.toUpperCase(),true)));
							pageDV.setElementAt(step,4,s);
							break;
						}
						case QM_COMMAND_$ITEMXML:
						{
							String showValue=(showFlag<-900)?"":(String)pageDV.elementAt(step,4);
							String newValue=addXMLQuestItem(mob, showFlag, pageDV, showValue, parm1Fixed, lastLabel, optionalEntry, step, ++showNumber);
							if(newValue!=null) pageDV.setElementAt(step,4,newValue);
							break;
						}
						case QM_COMMAND_$ITEMXML_ONEORMORE:
						{
							String showValue=(showFlag<-900)?"":(String)pageDV.elementAt(step,4);
							Vector<String> itemXMLs=new Vector<String>();
							int x=showValue.indexOf("</ITEM><ITEM>");
							while(x>=0)
							{
								String xml=showValue.substring(0,x+7).trim();
								if(xml.length()>0) itemXMLs.addElement(xml);
								showValue=showValue.substring(x+7);
								x=showValue.indexOf("</ITEM><ITEM>");
							}
							if(showValue.trim().length()>0)
								itemXMLs.addElement(showValue.trim());
							String newValue=null;
							for(int i=0;i<=itemXMLs.size();i++)
							{
								showValue=(i<itemXMLs.size())?(String)itemXMLs.elementAt(i):"";
								boolean optional=(i==0)?optionalEntry:true;
								String thisValue=addXMLQuestItem(mob, showFlag, pageDV, showValue, parm1Fixed, lastLabel, optional, step, ++showNumber);
								if(thisValue!=null)
								{
									if(newValue==null) newValue="";
									newValue+=thisValue;
									if((thisValue.length()>0)
									&&(i==itemXMLs.size()))
										itemXMLs.addElement(thisValue);
								}
							}
							if(newValue!=null) pageDV.setElementAt(step,4,newValue);
							break;
						}
						case QM_COMMAND_$MOBXML:
						{
							String showValue=(showFlag<-900)?"":(String)pageDV.elementAt(step,4);
							String newValue=addXMLQuestMob(mob, showFlag, pageDV, showValue, parm1Fixed, lastLabel, optionalEntry, step, ++showNumber);
							if(newValue!=null) pageDV.setElementAt(step,4,newValue);
							break;
						}
						case QM_COMMAND_$MOBXML_ONEORMORE:
						{
							String showValue=(showFlag<-900)?"":(String)pageDV.elementAt(step,4);
							Vector<String> mobXMLs=new Vector<String>();
							int x=showValue.indexOf("</MOB><MOB>");
							while(x>=0)
							{
								String xml=showValue.substring(0,x+6).trim();
								if(xml.length()>0) mobXMLs.addElement(xml);
								showValue=showValue.substring(x+6);
								x=showValue.indexOf("</MOB><MOB>");
							}
							if(showValue.trim().length()>0)
								mobXMLs.addElement(showValue.trim());
								
							String newValue=null;
							for(int i=0;i<=mobXMLs.size();i++)
							{
								showValue=(i<mobXMLs.size())?(String)mobXMLs.elementAt(i):"";
								boolean optional=(i==0)?optionalEntry:true;
								String thisValue=addXMLQuestMob(mob, showFlag, pageDV, showValue, parm1Fixed, lastLabel, optional, step, ++showNumber);
								if(thisValue!=null)
								{
									if(newValue==null) newValue="";
									newValue+=thisValue;
									if((thisValue.length()>0)
									&&(i==mobXMLs.size()))
										mobXMLs.addElement(thisValue);
								}
							}
							if(newValue!=null) pageDV.setElementAt(step,4,newValue);
							break;
						}
						case QM_COMMAND_$FACTION:
						{
							String showValue=(showFlag<-900)?"":(String)pageDV.elementAt(step,4);
							StringBuffer label=new StringBuffer(((lastLabel==null)?"":lastLabel)+"\n\rChoices: ");
							for(Enumeration<Faction> f=CMLib.factions().factions();f.hasMoreElements();)
								label.append("\""+f.nextElement().name()+"\" ");
							String s=CMLib.genEd().prompt(mob,showValue,++showNumber,showFlag,parm1Fixed,optionalEntry,false,label.toString(),
															QuestManager.QM_COMMAND_TESTS[inputCode],
															null);
							pageDV.setElementAt(step,4,s);
							break;
						}
						}
					}
					if(showFlag<-900){ ok=false; showFlag=0; mob.tell("\n\r^HNow verify this page's selections:^.^N"); continue;}
					if(showFlag>0){ showFlag=-1; continue;}
					String what=mob.session().prompt("Edit which (enter 0 to cancel)? ","");
					if(what.trim().equals("0"))
					{
						if(mob.session().confirm("Are you sure you want to abort (y/N)? ","N"))
						{
							mob.tell("Aborted.");
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
				DVector pageDV=(DVector)qPages.elementAt(page);
				for(int v=0;v<pageDV.size();v++)
				{
					var=(String)pageDV.elementAt(v,2);
					val=(String)pageDV.elementAt(v,4);
					if(((Integer)pageDV.elementAt(v,1)).intValue()==QM_COMMAND_$UNIQUE_QUEST_NAME)
						name=val;
					script=CMStrings.replaceAll(script,var,val);
				}
			}
			script=CMStrings.replaceAll(script,"$#AUTHOR",mob.Name());
			if((mob.session()!=null)&&(!mob.session().isStopped())
			&&(mob.session().confirm("Create the new quest: "+name+" (y/N)? ","N")))
			{
				Quest Q=(Quest)CMClass.getCommon("DefaultQuest");
				CMFile newQF=new CMFile(Resources.makeFileResourceName("quests/"+name+".quest"),mob,CMFile.FLAG_LOGERRORS);
				newQF.saveText(script);
				Q.setScript("LOAD=quests/"+name+".quest",true);
				if((Q.name().trim().length()==0)||(Q.duration()<0))
				{
					mob.tell("You must specify a VALID quest string.  This one contained errors.  Try AHELP QUESTS.");
					return null;
				}
				CMLib.quests().addQuest(Q);
				CMLib.quests().save();
				return Q;
			}
			return null;
		}
		catch(java.io.IOException e)
		{
			return null;
		}
	}
}
