package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
public class Quests extends StdLibrary implements QuestManager
{
	public String ID(){return "Quests";}
    protected String holidayFilename="quests/holidays/holidays.quest";
    protected String holidayDefinition="LOAD="+holidayFilename;
	protected Vector quests=new Vector();
    
	public Quest objectInUse(Environmental E)
	{
		if(E==null) return null;
		for(int q=0;q<numQuests();q++)
		{
			Quest Q=fetchQuest(q);
			if(Q.isQuestObject(E)) return Q;
		}
		return null;
	}

	public int numQuests(){return quests.size();}
	public Quest fetchQuest(int i){
		try{
			return (Quest)quests.elementAt(i);
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
	public void addQuest(Quest Q)
	{
		if(!quests.contains(Q))
		{
			quests.addElement(Q);
			Q.autostartup();
		}
	}
	public void shutdown()
	{
		for(int i=numQuests();i>=0;i--)
		{
			Quest Q=fetchQuest(i);
			delQuest(Q);
		}
		quests.clear();
	}
	public void delQuest(Quest Q)
	{
		if(quests.contains(Q))
		{
			Q.stopQuest();
			CMLib.threads().deleteTick(Q,Tickable.TICKID_QUEST);
			quests.removeElement(Q);
		}
	}
	public void save()
	{
		CMLib.database().DBUpdateQuests(quests);
	}

    public Object getHolidayFile()
    {
        Quest Q=fetchQuest("holidays");
        if((Q==null)
        ||(!Q.script().toUpperCase().trim().equalsIgnoreCase(holidayDefinition)))
        {
            return "A quest named 'holidays', with the script definition '"+holidayDefinition+"' has not been created.  Enter the following to create this quest:\n\r"
                  +"CREATE QUEST "+holidayDefinition+"\n\r"
                  +"SAVE QUESTS";
        }
        CMFile F=new CMFile(Resources.makeFileResourceName(holidayFilename),null,false);
        if((!F.exists())||(!F.canRead())||(!F.canWrite()))
        {
            return "The file '"+Resources.makeFileResourceName(holidayFilename)+"' does not exist, and is required for this feature.";
        }
        Vector V=Resources.getFileLineVector(F.text());
        Vector steps=parseQuestSteps(V,0,true);
        return steps;
    }
    
    public String listHolidays(Area A, String otherParms)
    {
        Object resp=getHolidayFile();
        if(resp instanceof String)
            return (String)resp;
        Vector steps=null;
        if(resp instanceof Vector)
            steps=(Vector)resp;
        else
            return "Unknown error.";
        String areaName=A.Name().toUpperCase().trim();
        if(otherParms.equalsIgnoreCase("ALL"))
            areaName=null;
        StringBuffer str=new StringBuffer("^xDefined Quest Holidays^?\n\r");
        Vector line=null;
        String var=null;
        Vector V=null;
        str.append("^H#  "+CMStrings.padRight("Holiday Name",20)+CMStrings.padRight("Area Name(s)",50)+"^?\n\r");
        for(int s=1;s<steps.size();s++)
        {
            String step=(String)steps.elementAt(s);
            V=Resources.getFileLineVector(new StringBuffer(step));
            Vector cmds=CMLib.quests().parseQuestCommandLines(V,"SET",0);
            Vector areaLine=null;
            Vector nameLine=null;
            for(int v=0;v<cmds.size();v++)
            {
                line=(Vector)cmds.elementAt(v);
                if(line.size()>1)
                {
                    var=((String)line.elementAt(1)).toUpperCase();
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
                    if(!contains)
                    for(int l=2;l<areaLine.size();l++)
                        if(areaName.equalsIgnoreCase((String)areaLine.elementAt(l)))
                        {    contains=true; break;}
                }
                else
                {
                    areaLine=CMParms.makeVector("","","*special*");
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
        while((mob.session()!=null)&&(!mob.session().killFlag()))
        {
            String newVAL=CMLib.english().promptText(mob,oldVal,showNumber,showFlag,prompt,emptyOK);
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
                sets.addElement(var,newVAL,new Integer(-1));
            break;
        }
    }

    public String createHoliday(Area A, String named)
    {
        Object resp=getHolidayFile();
        if(resp instanceof String)
            return (String)resp;
        Vector steps=null;
        if(resp instanceof Vector)
            steps=(Vector)resp;
        else
            return "Unknown error.";
        if(CMLib.quests().fetchQuest(named)!=null)
            return "A quest called '"+named+"' already exists.  Better to pick a new name.";
        Vector lineV=null;
        String line=null;
        String var=null;
        String cmd=null;
        String step=null;
        for(int v=0;v<steps.size();v++)
        {
            step=(String)steps.elementAt(v);
            Vector stepV=Resources.getFileLineVector(new StringBuffer(step));
            for(int v1=0;v1<stepV.size();v1++)
            {
                line=(String)stepV.elementAt(v1);
                lineV=CMParms.parse(line);
                if(lineV.size()>1)
                {
                    cmd=((String)lineV.elementAt(0)).toUpperCase();
                    var=((String)lineV.elementAt(1)).toUpperCase();
                    if(cmd.equals("SET")&&(var.equalsIgnoreCase("NAME")))
                    {
                        String str=CMParms.combine(lineV,2);
                        if(str.equalsIgnoreCase(named))
                            return "A quest called '"+named+"' already exists.  Better to pick a new name or modify the existing one.";
                    }
                }
            }
        }
        CMFile F=new CMFile(Resources.makeFileResourceName(holidayFilename),null,false);
        StringBuffer newHoliday=new StringBuffer("");
        newHoliday.append("\n\rSET NAME "+named+"\n\r");
        newHoliday.append("SET WAIT 900+(1?100)\n\r");
        newHoliday.append("SET INTERVAL 1\n\r");
        newHoliday.append("SET DURATION 900\n\r");
        newHoliday.append("SET AREAGROUP \""+A.name()+"\"\n\r");
        newHoliday.append("QUIET\n\r");
        newHoliday.append("SET MOBGROUP RESELECT MASK=+INT 3\n\r");
        newHoliday.append("GIVE BEHAVIOR MUDCHAT +(|"+named+")\\;9Happy "+named+" $n!\\;\n\r");
        newHoliday.append("SET MOBGROUP RESELECT MASK=-JAVACLASS +GenShopkeeper +StdShopKeeper\n\r");
        newHoliday.append("GIVE STAT PRICEMASKS 0.75 -MATERIAL +CLOTH\n\r");
        newHoliday.append("STEP BREAK\n\r");
        F.saveText(newHoliday,true);
        Quest Q=fetchQuest("holidays");
        if(Q!=null) Q.setScript(holidayDefinition);
        return "Ok";
    }
    
    public String deleteHoliday(Area A, int holidayNumber)
    {
        Object resp=getHolidayFile();
        if(resp instanceof String)
            return (String)resp;
        Vector steps=null;
        if(resp instanceof Vector)
            steps=(Vector)resp;
        else
            return "Unknown error.";
        
        if((holidayNumber<=0)||(holidayNumber>=steps.size()))
            return holidayNumber+" does not exist as a holiday -- enter LIST HOLIDAYS.";
        
        String step=null;
        StringBuffer buf=new StringBuffer("");
        steps.removeElementAt(holidayNumber);
        for(int v=0;v<steps.size();v++)
        {
            step=(String)steps.elementAt(v);
            buf.append(step+"\n\r");
        }
        CMFile F=new CMFile(Resources.makeFileResourceName(holidayFilename),null,false);
        F.saveText(buf);
        Quest Q=fetchQuest("holidays");
        if(Q!=null) Q.setScript(holidayDefinition);
        return "Holiday deleted.";
    }

    public String getHolidayName(int index)
    {
        Object resp=getHolidayFile();
        if(resp instanceof String){ return "";}
        Vector steps=null;
        if(resp instanceof Vector)
            steps=(Vector)resp;
        else
            return "";
        
        if((index<0)||(index>=steps.size()))
            return "";
        
        Vector lineV=null;
        String line=null;
        String var=null;
        String cmd=null;
        String step=null;
        step=(String)steps.elementAt(index);
        Vector stepV=Resources.getFileLineVector(new StringBuffer(step));
        for(int v1=0;v1<stepV.size();v1++)
        {
            line=(String)stepV.elementAt(v1);
            lineV=CMParms.parse(line);
            if(lineV.size()>1)
            {
                cmd=((String)lineV.elementAt(0)).toUpperCase();
                var=((String)lineV.elementAt(1)).toUpperCase();
                if(cmd.equals("SET")&&(var.equalsIgnoreCase("NAME")))
                    return CMParms.combine(lineV,2);
            }
        }
        return "";
    }
    
    public int getHolidayIndex(String named)
    {
        Object resp=getHolidayFile();
        if(resp instanceof String){ return -1;}
        Vector steps=null;
        if(resp instanceof Vector)
            steps=(Vector)resp;
        else
            return -1;
        
        Vector lineV=null;
        String line=null;
        String var=null;
        String cmd=null;
        String step=null;
        for(int v=1;v<steps.size();v++)
        {
            step=(String)steps.elementAt(v);
            Vector stepV=Resources.getFileLineVector(new StringBuffer(step));
            for(int v1=0;v1<stepV.size();v1++)
            {
                line=(String)stepV.elementAt(v1);
                lineV=CMParms.parse(line);
                if(lineV.size()>1)
                {
                    cmd=((String)lineV.elementAt(0)).toUpperCase();
                    var=((String)lineV.elementAt(1)).toUpperCase();
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
    
    public int startLineIndex(Vector V, String start)
    {
        start=start.toUpperCase().trim();
        for(int v=0;v<V.size();v++)
            if(((String)V.elementAt(v)).toUpperCase().trim().startsWith(start))
                return v;
        return -1;
    }
    
    public Vector getEncodedHolidayData(String dataFromStepsFile)
    {
        Vector stepV=Resources.getFileLineVector(new StringBuffer(dataFromStepsFile));
        for(int v=0;v<stepV.size();v++)
            stepV.setElementAt(CMStrings.replaceAll((String)stepV.elementAt(v),"\\;",";"),v);
        DVector settings=new DVector(3);
        DVector behaviors=new DVector(3);
        DVector properties=new DVector(3);
        DVector stats=new DVector(3);
        Vector encodedData=new Vector();
        encodedData.addElement(settings);
        encodedData.addElement(behaviors);
        encodedData.addElement(properties);
        encodedData.addElement(stats);
        encodedData.addElement(stepV);
        Vector lineV=null;
        String line=null;
        String var=null;
        String cmd=null;
        int pricingMobIndex=-1;
        String[] SETTINGS={"NAME","WAIT","DATE","DURATION","MUDDAY","AREAGROUP","MOBGROUP"};
        for(int v=0;v<stepV.size();v++)
        {
            line=(String)stepV.elementAt(v);
            lineV=CMParms.parse(line);
            if(lineV.size()>1)
            {
                cmd=((String)lineV.elementAt(0)).toUpperCase();
                var=((String)lineV.elementAt(1)).toUpperCase();
                if(cmd.equals("SET")&&(CMParms.indexOf(SETTINGS,var)>=0))
                {
                    if(!settings.contains(var))
                    {
                        String str=CMParms.combineWithQuotes(lineV,2);
                        if(str.toUpperCase().startsWith("ANY ")) str=str.substring(4);
                        if(str.toUpperCase().startsWith("RESELECT MASK=")) str=str.substring(14);
                        if(str.toUpperCase().startsWith("MASK=")) str=str.substring(5);
                        settings.addElement(var,str,new Integer(v));
                    }
                    else
                    if((var.equalsIgnoreCase("MOBGROUP"))
                    &&(pricingMobIndex<0)
                    &&(CMParms.combine(lineV,2).toUpperCase().indexOf("SHOPKEEPER")>0))
                        pricingMobIndex=v;
                }
                if(cmd.equals("GIVE")&&(var.equalsIgnoreCase("BEHAVIOR"))&&(lineV.size()>2)&&(pricingMobIndex<0))
                {
                    var=((String)lineV.elementAt(2)).toUpperCase();
                    behaviors.addElement(var,CMParms.combineWithQuotes(lineV,3),new Integer(v));
                }
                if(cmd.equals("GIVE")&&(var.equalsIgnoreCase("AFFECT"))&&(lineV.size()>2)&&(pricingMobIndex<0))
                {
                    var=((String)lineV.elementAt(2)).toUpperCase();
                    properties.addElement(var,CMParms.combineWithQuotes(lineV,3),new Integer(v));
                }
                if(cmd.equals("GIVE")&&(var.equalsIgnoreCase("STAT"))&&(lineV.size()>2))
                {
                    var=((String)lineV.elementAt(2)).toUpperCase();
                    if((pricingMobIndex<0)||(var.equals("PRICEMASKS")))
                        stats.addElement(var,CMParms.combineWithQuotes(lineV,3),new Integer(v));
                }
            }
        }
        encodedData.addElement(new Integer(pricingMobIndex));
        return encodedData;
    }
    
    public void modifyHoliday(MOB mob, int holidayNumber)
    {
        Object resp=getHolidayFile();
        if(resp instanceof String)
        { mob.tell((String)resp); return;}
        Vector steps=null;
        if(resp instanceof Vector)
            steps=(Vector)resp;
        else
        { mob.tell("Unknown error."); return;}
        if((holidayNumber<=0)||(holidayNumber>=steps.size()))
        { mob.tell(holidayNumber+" does not exist as a holiday -- enter LIST HOLIDAYS."); return;}

        String step=(String)steps.elementAt(holidayNumber);
        Vector encodedData=getEncodedHolidayData(step);
        DVector settings=(DVector)encodedData.elementAt(0);
        DVector behaviors=(DVector)encodedData.elementAt(1);
        DVector properties=(DVector)encodedData.elementAt(2);
        DVector stats=(DVector)encodedData.elementAt(3);
        Vector stepV=(Vector)encodedData.elementAt(4);
        int pricingMobIndex=((Integer)encodedData.elementAt(5)).intValue();
        DVector oldProperties=properties.copyOf();
        DVector oldBehaviors=behaviors.copyOf();
        int oldNameIndex=settings.indexOf("NAME");
        if((mob.isMonster())||(oldNameIndex<0)) 
            return;
        String oldName=(String)settings.elementAt(oldNameIndex,2);
        boolean ok=false;
        int showFlag=-1;
        if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
            showFlag=-999;
        try{
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
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
            int newHolidayIndex=getHolidayIndex(oldName);
            if(newHolidayIndex!=holidayNumber)
                holidayNumber=newHolidayIndex;
            resp=getHolidayFile();
            if(resp instanceof String)
            { mob.tell((String)resp); return;}
            steps=null;
            if(resp instanceof Vector)
                steps=(Vector)resp;
            else
            { mob.tell("Unknown error."); return;}
            
            StringBuffer buf=new StringBuffer("");
            for(int v=0;v<steps.size();v++)
            {
                step=(String)steps.elementAt(v);
                if(v==holidayNumber)
                {
                    encodedData=getEncodedHolidayData(step);
                    stepV=(Vector)encodedData.elementAt(4);
                    int index=startLineIndex(stepV,"SET NAME");
                    stepV.setElementAt("SET NAME "+(String)settings.elementAt(settings.indexOf("NAME"),2),index);
                    index=startLineIndex(stepV,"SET DURATION");
                    stepV.setElementAt("SET DURATION "+(String)settings.elementAt(settings.indexOf("DURATION"),2),index);
                    int intervalLine=startLineIndex(stepV,"SET MUDDAY");
                    if(intervalLine<0) intervalLine=startLineIndex(stepV,"SET DATE");
                    if(intervalLine<0) intervalLine=startLineIndex(stepV,"SET WAIT");
                    int mudDayIndex=settings.indexOf("MUDDAY");
                    int dateIndex=settings.indexOf("DATE");
                    int waitIndex=settings.indexOf("WAIT");
                    if(mudDayIndex>=0)
                        stepV.setElementAt("SET MUDDAY "+((String)settings.elementAt(mudDayIndex,2)),intervalLine);
                    else
                    if(dateIndex>=0)
                        stepV.setElementAt("SET DATE "+((String)settings.elementAt(dateIndex,2)),intervalLine);
                    else
                        stepV.setElementAt("SET WAIT "+((String)settings.elementAt(waitIndex,2)),intervalLine);
                    
                    index=settings.indexOf("AREAGROUP");
                    if(index>=0)
                    {
                        index=startLineIndex(stepV,"SET AREAGROUP");
                        if(index>=0)
                            stepV.setElementAt("SET AREAGROUP "+settings.elementAt(settings.indexOf("AREAGROUP"),2),index);
                    }
                    
                    index=settings.indexOf("MOBGROUP");
                    if(index>=0)
                    {
                        index=startLineIndex(stepV,"SET MOBGROUP");
                        stepV.setElementAt("SET MOBGROUP RESELECT MASK="+settings.elementAt(settings.indexOf("MOBGROUP"),2),index);
                    }
                    if((pricingMobIndex>0)&&(stats.indexOf("PRICEMASKS")>=0))
                    {
                        index=startLineIndex(stepV,"GIVE STAT PRICEMASKS");
                        String s=(String)stats.elementAt(stats.indexOf("PRICEMASKS"),2);
                        if(s.trim().length()==0)
                        {
                            if(index>=0)
                                stepV.removeElementAt(index);
                        }
                        else
                        {
                            if(index>=0)
                                stepV.setElementAt("GIVE STAT PRICEMASKS "+s,index);
                            else
                                stepV.insertElementAt("GIVE STAT PRICEMASKS "+s,pricingMobIndex+1);
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
                                stepV.removeElementAt(index);
                        }
                        else
                        {
                            if(index>=0)
                                stepV.setElementAt("GIVE BEHAVIOR AGGRESSIVE "+s,index);
                            else
                                stepV.insertElementAt("GIVE BEHAVIOR AGGRESSIVE "+s,mobGroupIndex+1);
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
                                stepV.removeElementAt(index);
                        }
                        else
                        {
                            if(index>=0)
                                stepV.setElementAt("GIVE BEHAVIOR MUDCHAT "+s,index);
                            else
                                stepV.insertElementAt("GIVE BEHAVIOR MUDCHAT "+s,mobGroupIndex+1);
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
                                stepV.removeElementAt(index);
                        }
                        else
                        {
                            if(index>=0)
                                stepV.setElementAt("GIVE AFFECT MOOD "+s,index);
                            else
                                stepV.insertElementAt("GIVE AFFECT MOOD "+s,mobGroupIndex+1);
                        }
                    }
                    
                    // look for newly missing stuff
                    for(int p=0;p<oldProperties.size();p++)
                    {
                        String prop=(String)oldProperties.elementAt(p,1);
                        if(properties.indexOf(prop)<0)
                        {
                            index=startLineIndex(stepV,"GIVE AFFECT "+prop);
                            if(index>=0) stepV.removeElementAt(index);
                        }
                    }
                    // look for newly missing stuff
                    for(int p=0;p<oldBehaviors.size();p++)
                    {
                        String behav=(String)oldBehaviors.elementAt(p,1);
                        if(behaviors.indexOf(behav)<0)
                        {
                            index=startLineIndex(stepV,"GIVE BEHAVIOR "+behav);
                            if(index>=0) stepV.removeElementAt(index);
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
                            stepV.setElementAt("GIVE AFFECT "+prop.toUpperCase().trim()+" "+(properties.elementAt(p,2)),index);
                        else
                            stepV.insertElementAt("GIVE AFFECT "+prop.toUpperCase().trim()+" "+(properties.elementAt(p,2)),mobGroupIndex+1);
                    }
                    // now changed/added stuff
                    for(int p=0;p<behaviors.size();p++)
                    {
                        String behav=(String)behaviors.elementAt(p,1);
                        if(behav.equalsIgnoreCase("AGGRESSIVE")||behav.equalsIgnoreCase("MUDCHAT")) continue;
                        mobGroupIndex=startLineIndex(stepV,"SET MOBGROUP");
                        index=startLineIndex(stepV,"GIVE BEHAVIOR "+behav);
                        if(index>=0) 
                            stepV.setElementAt("GIVE BEHAVIOR "+behav.toUpperCase().trim()+" "+(behaviors.elementAt(p,2)),index);
                        else
                            stepV.insertElementAt("GIVE BEHAVIOR "+behav.toUpperCase().trim()+" "+(behaviors.elementAt(p,2)),mobGroupIndex+1);
                    }
                    
                    for(int v1=0;v1<stepV.size();v1++)
                    {
                        if(((String)stepV.elementAt(v1)).trim().length()>0)
                            buf.append(CMStrings.replaceAll((((String)stepV.elementAt(v1))+"\n\r"),";","\\;"));
                    }
                    buf.append("\n\r");
                }
                else
                    buf.append(step+"\n\r");
            }
            CMFile F=new CMFile(Resources.makeFileResourceName(holidayFilename),null,false);
            F.saveText(buf);
            Quest Q=fetchQuest("holidays");
            if(Q!=null) Q.setScript(holidayDefinition);
            mob.tell("Holiday modified.");
        }
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
            settings.addElement("DURATION","900",new Integer(-1));
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
                while(newVal.equals("?")&&((mob.session()!=null)&&(!mob.session().killFlag())))
                {
                    newVal=CMLib.english().promptText(mob,TYPES[typeIndex],showNumber,showFlag,"Schedule type (?)",CMParms.toStringList(TYPES));
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
                        settings.addElement("WAIT","100",new Integer(-1));
                    if((typeIndex==1)&&(mudDayIndex<0))
                        settings.addElement("MUDDAY","1-1",new Integer(-1));
                    if((typeIndex==2)&&(dateIndex<0))
                        settings.addElement("DATE","1-1",new Integer(-1));
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
                if((mob.session()!=null)&&(!mob.session().killFlag()))
                {
                    if(((showFlag<=-999)&&CMLib.english().promptBool(mob,false,showNumber,showFlag,"Add new mob behavior"))
                    ||(showNumber==showFlag))
                    {
                        behaviors.addElement("BehaviorID","",new Integer(behaviors.size()));
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
                behavior=CMLib.english().promptText(mob,behavior,showNumber,showFlag,"Behavior ID (?, NULL to delete)",true,toStringList(CMClass.behaviors()));
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
                StringBuffer help=CMLib.help().getHelpText(behavior,mob,true);
                if(help==null) help=new StringBuffer("No help on '"+behavior+"'");
                parms=CMLib.english().promptText(mob,parms,showNumber,showFlag,"Behavior Parameters (?)",help.toString());
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
                if((mob.session()!=null)&&(!mob.session().killFlag()))
                {
                    if(((showFlag<=-999)&&CMLib.english().promptBool(mob,false,showNumber,showFlag,"Add new mob property"))
                    ||(showNumber==showFlag))
                    {
                        properties.addElement("AbilityID","",new Integer(properties.size()));
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
                propertyID=CMLib.english().promptText(mob,propertyID,showNumber,showFlag,"Ability ID (?, NULL to delete)",true,toStringList(CMClass.abilities()));
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
                StringBuffer help=CMLib.help().getHelpText(propertyID,mob,true);
                if(help==null) help=new StringBuffer("No help on '"+propertyID+"'");
                parms=CMLib.english().promptText(mob,parms,showNumber,showFlag,"Ability Parameters (?)",help.toString());
                properties.setElementAt(p,1,propertyID);
                properties.setElementAt(p,2,parms);
            }
            showNumber++;
        }
        return showNumber;
    }
    
    public static String toStringList(Enumeration e)
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
        Vector priceV=CMParms.parseCommas(priceStr,true);
        for(int v=0;v<=priceV.size();v++)
        {
            if((showFlag>0)&&(showFlag!=showNumber)){ if(v<priceV.size())showNumber++; continue;}
            if(v==priceV.size())
            {
                if((mob.session()!=null)&&(!mob.session().killFlag()))
                {
                    if(((showFlag<=-999)&&CMLib.english().promptBool(mob,false,showNumber,showFlag,"Add new price factor"))
                    ||(showNumber==showFlag))
                    {
                        priceV.addElement("1.0");
                        v-=1;
                    }
                    else
                    if(showFlag==-1)
                        mob.tell(showNumber+". Add new price factor.");
                }
                continue;
            }
            String priceLine=(String)priceV.elementAt(v);
            double priceFactor=0.0;
            String mask="";
            int x=priceLine.indexOf(" ");
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
                priceFactor=CMLib.english().promptDouble(mob,priceFactor,showNumber,showFlag,"Price Factor (enter 0 to delete)");
                if(priceFactor==0.0)
                {
                    priceV.removeElementAt(v);
                    v--;
                    if((showFlag==showNumber)) break;
                    showNumber--;
                    continue;
                }
                mob.tell(showNumber+". Price Factor: "+Math.round(priceFactor*100.0)+"%: "+mask);
                mask=CMLib.english().promptText(mob,mask,showNumber,showFlag,"Item mask for this price (?)",CMLib.masking().maskHelp("\n\r","disallow"));
                priceV.setElementAt(priceFactor+" "+mask,v);
            }
            showNumber++;
        }
        String newVal=CMParms.toStringList(priceV);
        if(pndex>=0)
            stats.setElementAt(pndex,2,newVal);
        else
            stats.addElement("PRICEMASKS",newVal,new Integer(stats.size()));
        return showNumber;
    }
    
    protected int genMudChat(MOB mob, String var, DVector behaviors, int showNumber, int showFlag)
    throws IOException
    {
        int mndex=behaviors.indexOf(var);
        String mudChatStr=(mndex<0)?"":(String)behaviors.elementAt(mndex,2);
        if(mudChatStr.startsWith("+")) mudChatStr=mudChatStr.substring(1);
        Vector rawMCV=CMParms.parseSemicolons(mudChatStr,true);
        Vector mudChatV=new Vector();
        String s=null;
        Vector V=new Vector();
        mudChatV.addElement(V);
        for(int r=0;r<rawMCV.size();r++)
        {
            s=(String)rawMCV.elementAt(r);
            if(s.startsWith("(")&&s.endsWith(")"))
            {
                if(V.size()>0)
                {
                    V=new Vector();
                    mudChatV.addElement(V);
                }
                s=s.substring(1,s.length()-1);
            }
            V.addElement(s);
        }
        if(V.size()==0) mudChatV.removeElement(V);
        for(int v=0;v<=mudChatV.size();v++)
        {
            if((showFlag>0)&&(showFlag!=showNumber)){ if(v<mudChatV.size())showNumber++; continue;}
            if(v==mudChatV.size())
            {
                if((mob.session()!=null)&&(!mob.session().killFlag()))
                {
                    if(((showFlag<=-999)&&CMLib.english().promptBool(mob,false,showNumber,showFlag,"Add new mud chat"))
                    ||(showNumber==showFlag))
                    {
                        V=new Vector();
                        V.addElement("match | these | words or phrases");
                        V.addElement("9say this");
                        mudChatV.addElement(V);
                        v-=1;
                    }
                    else
                    if(showFlag==-1)
                        mob.tell(showNumber+". Add new mud chat.");
                }
                continue;
            }
            V=(Vector)mudChatV.elementAt(v);
            String words=(String)V.firstElement();
            mob.tell(showNumber+". MudChat for words: "+words);
            if((showFlag==showNumber)||(showFlag<=-999))
            {
                words=CMLib.english().promptText(mob,words,showNumber,showFlag,"Enter matching words (| delimited, NULL to delete)\n\r",true);
                if(words.trim().length()==0)
                {
                    mudChatV.removeElementAt(v);
                    if((showFlag==showNumber)) break;
                    showNumber--;
                    continue;
                }
                V.setElementAt(words,0);
                for(int v1=1;v1<=V.size();v1++)
                {
                    if(v1==V.size())
                    {
                        if((mob.session()!=null)&&(!mob.session().killFlag())
                        &&(mob.session().confirm("Add another thing to say (y/N)","NO")))
                        {
                            V.addElement("9say this");
                            v1-=1;
                        }
                        continue;
                    }
                    s=(String)V.elementAt(v1);
                    String newStr="?";
                    while((newStr.equals("?"))&&(mob.session()!=null)&&(!mob.session().killFlag()))
                    {
                        newStr=mob.session().prompt("Enter  # Weight + thing to say (?) '"+s+"'\n\r: ",s);
                        if(newStr.equals("?"))
                            mob.tell("Enter a number followed by a phrase to say like 9thingtosay. Enter NULL to delete this thing to say.");
                        else
                            s=newStr;
                    }
                    if(s.trim().length()==0)
                    {
                        V.removeElementAt(v1);
                        v1--;
                        continue;
                    }
                    if(!Character.isDigit(s.charAt(0)))
                        s="9"+s;
                    V.setElementAt(s,v1);
                }
            }
            showNumber++;
        }
        StringBuffer finalVal=new StringBuffer("");
        for(int v=0;v<mudChatV.size();v++)
        {
            V=(Vector)mudChatV.elementAt(v);
            if(V.size()==0) continue;
            finalVal.append("("+((String)V.firstElement())+");");
            for(int v1=1;v1<V.size();v1++)
                finalVal.append(((String)V.elementAt(v1))+";");
            finalVal.append(";");
        }
        if(mndex>=0)
            behaviors.setElementAt(mndex,2,(finalVal.toString().trim().length()==0)?"":("+"+finalVal.toString()));
        else
            behaviors.addElement(var,(finalVal.toString().trim().length()==0)?"":("+"+finalVal.toString()),new Integer(behaviors.size()));
        return showNumber;
    }
    

    public Vector parseQuestCommandLines(Vector script, String cmdOnly, int startLine)
    {
        Vector line=null;
        String cmd=null;
        boolean inScript=false;
        Vector lines=new Vector();
        if(cmdOnly!=null) cmdOnly=cmdOnly.toUpperCase().trim();
        for(int v=startLine;v<script.size();v++)
        {
            line=CMParms.parse(((String)script.elementAt(v)));
            if(line.size()==0) continue;
            cmd=((String)line.firstElement()).toUpperCase().trim();
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
                lines.addElement(line);
        }
        return lines;
    }
    
    public Vector parseQuestSteps(Vector script, int startLine, boolean rawLineInput)
    {
        Vector line=null;
        String cmd=null;
        Vector parsed=new Vector();
        StringBuffer scr=new StringBuffer("");
        boolean inScript=false;
        for(int v=startLine;v<script.size();v++)
        {
            line=CMParms.parse(((String)script.elementAt(v)).toUpperCase().trim());
            if(line.size()==0) continue;
            cmd=((String)line.firstElement()).toUpperCase().trim();
            if(rawLineInput)
                scr.append(((String)script.elementAt(v))+"\n\r");
            else
                scr.append(CMStrings.replaceAll(((String)script.elementAt(v))+"\n\r",";","\\;"));
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
            parsed.addElement(scr);
        return parsed;
    }
    
}
