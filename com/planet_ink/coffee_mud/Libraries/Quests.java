package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
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
            if(Q.isObjectInUse(E)) return Q;
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

    public Object getHolidayFile()
    {
        Quest Q=fetchQuest("holidays");
        if((Q==null)
        ||(!Q.script().toUpperCase().trim().equalsIgnoreCase(holidayDefinition)))
        {
            Q=null;
            CMFile lF=new CMFile("//"+Resources.makeFileResourceName(holidayFilename),null,false);
            CMFile vF=new CMFile("::"+Resources.makeFileResourceName(holidayFilename),null,false);
            if((lF.exists())&&(!vF.exists())&&(lF.canRead())&&(vF.canWrite()))
            {
                byte[] O=lF.raw();
                vF.saveRaw(O);
            }
            if(Q==null) 
            {
                Q=(Quest)CMClass.getCommon("DefaultQuest");
                Q.setScript(holidayDefinition);
                addQuest(Q);
                CMLib.database().DBUpdateQuest(Q);
                Q=fetchQuest("holidays");
            }
            if(Q==null)
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
                    if((!contains) && (areaName != null))
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

    public String createHoliday(String named, String areaName, boolean save)
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
        if(save)
        {
            CMFile F=new CMFile(Resources.makeFileResourceName(holidayFilename),null,false);
            F.saveText(getDefaultHoliData(named,areaName),true);
            Quest Q=fetchQuest("holidays");
            if(Q!=null) Q.setScript(holidayDefinition);
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
    
    public String deleteHoliday(int holidayNumber)
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
                        settings.addElement(var,str,Integer.valueOf(v));
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
                    behaviors.addElement(var,CMParms.combineWithQuotes(lineV,3),Integer.valueOf(v));
                }
                if(cmd.equals("GIVE")&&(var.equalsIgnoreCase("AFFECT"))&&(lineV.size()>2)&&(pricingMobIndex<0))
                {
                    var=((String)lineV.elementAt(2)).toUpperCase();
                    properties.addElement(var,CMParms.combineWithQuotes(lineV,3),Integer.valueOf(v));
                }
                if(cmd.equals("GIVE")&&(var.equalsIgnoreCase("STAT"))&&(lineV.size()>2))
                {
                    var=((String)lineV.elementAt(2)).toUpperCase();
                    if((pricingMobIndex<0)||(var.equals("PRICEMASKS")))
                        stats.addElement(var,CMParms.combineWithQuotes(lineV,3),Integer.valueOf(v));
                }
            }
        }
        encodedData.addElement(Integer.valueOf(pricingMobIndex));
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
            String err=alterHoliday(oldName, encodedData);
            if(err.length()==0)
                mob.tell("Holiday modified.");
            else
                mob.tell(err);
        }
    }

    public String alterHoliday(String oldName, Vector newData)
    {
        DVector settings=(DVector)newData.elementAt(0);
        DVector behaviors=(DVector)newData.elementAt(1);
        DVector properties=(DVector)newData.elementAt(2);
        DVector stats=(DVector)newData.elementAt(3);
        //Vector stepV=(Vector)data.elementAt(4);
        int pricingMobIndex=((Integer)newData.elementAt(5)).intValue();
        
        int holidayNumber=getHolidayIndex(oldName);
        Object resp=getHolidayFile();
        if(resp instanceof String) return (String)resp;
        Vector steps=null;
        if(resp instanceof Vector)
            steps=(Vector)resp;
        else
            return "Unknown error.";
        
        String step = null;
        Vector stepV = null;
        Vector encodedData = null;
        StringBuffer buf=new StringBuffer("");
        for(int v=0;v<steps.size();v++)
        {
            step=(String)steps.elementAt(v);
            if(v==holidayNumber)
            {
                encodedData=getEncodedHolidayData(step);
                DVector oldBehaviors=((DVector)encodedData.elementAt(1)).copyOf();
                DVector oldProperties=((DVector)encodedData.elementAt(2)).copyOf();
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
                while(newVal.equals("?")&&((mob.session()!=null)&&(!mob.session().killFlag())))
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
                if((mob.session()!=null)&&(!mob.session().killFlag()))
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
                if((mob.session()!=null)&&(!mob.session().killFlag()))
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
                    if(((showFlag<=-999)&&CMLib.genEd().prompt(mob,false,showNumber,showFlag,"Add new price factor"))
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
                priceFactor=CMLib.genEd().prompt(mob,priceFactor,showNumber,showFlag,"Price Factor (enter 0 to delete)");
                if(priceFactor==0.0)
                {
                    priceV.removeElementAt(v);
                    v--;
                    if((showFlag==showNumber)) break;
                    showNumber--;
                    continue;
                }
                mob.tell(showNumber+". Price Factor: "+Math.round(priceFactor*100.0)+"%: "+mask);
                mask=CMLib.genEd().prompt(mob,mask,showNumber,showFlag,"Item mask for this price",CMLib.masking().maskHelp("\n\r","disallow"));
                priceV.setElementAt(priceFactor+" "+mask,v);
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

    public String breakOutMaskString(String s, Vector p)
    {
        String mask="";
        int x=s.toUpperCase().lastIndexOf("MASK=");
        if(x>=0)
        {
            mask=s.substring(x+5).trim();
            int i=0;
            while((i<p.size())&&(((String)p.elementAt(i)).toUpperCase().indexOf("MASK=")<0))i++;
            if(i<=p.size())
            {
                String pp=(String)p.elementAt(i);
                x=pp.toUpperCase().indexOf("MASK=");
                if((x>0)&&(pp.substring(0,x).trim().length()>0))
                {
                    p.setElementAt(pp.substring(0,x).trim(),i);
                    i++;
                }
                while(i<p.size()) p.removeElementAt(i);
            }
        }
        return mask.trim();
    }
    
    public Vector breakOutMudChatVs(String MUDCHAT, DVector behaviors) {
        int mndex=behaviors.indexOf(MUDCHAT);
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
        return mudChatV;
    }
    
    protected int genMudChat(MOB mob, String var, DVector behaviors, int showNumber, int showFlag)
    throws IOException
    {
        int mndex=behaviors.indexOf(var);
        Vector mudChatV = breakOutMudChatVs(var,behaviors);
        Vector V = null;
        String s=null;
        for(int v=0;v<=mudChatV.size();v++)
        {
            if((showFlag>0)&&(showFlag!=showNumber)){ if(v<mudChatV.size())showNumber++; continue;}
            if(v==mudChatV.size())
            {
                if((mob.session()!=null)&&(!mob.session().killFlag()))
                {
                    if(((showFlag<=-999)&&CMLib.genEd().prompt(mob,false,showNumber,showFlag,"Add new mud chat"))
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
                words=CMLib.genEd().prompt(mob,words,showNumber,showFlag,"Enter matching words (| delimited, NULL to delete)\n\r",true);
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
            behaviors.addElement(var,(finalVal.toString().trim().length()==0)?"":("+"+finalVal.toString()),Integer.valueOf(behaviors.size()));
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
        String lineStr=null;
        for(int v=startLine;v<script.size();v++)
        {
            lineStr=((String)script.elementAt(v)).trim();
            if(lineStr.trim().equalsIgnoreCase(XMLLibrary.FILE_XML_BOUNDARY))
            {
                if(scr.toString().trim().length()>0)
                    parsed.addElement(scr.toString());
                scr=new StringBuffer(((String)script.elementAt(v))+"\n\r");
                while((++v)<script.size())
                    scr.append(((String)script.elementAt(v))+"\n\r");
                break;
            }
            line=CMParms.parse(lineStr.toUpperCase());
            if(line.size()==0) continue;
            cmd=((String)line.firstElement()).trim();
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
 
    public DVector getQuestTemplate(MOB mob, String fileToGet)
    {
        CMFile tempF=new CMFile(Resources.makeFileResourceName("quests/templates"),mob,true,false);
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
                Vector V=Resources.getFileLineVector(files[f].text());
                String s=null;
                boolean foundStart=false;
                boolean foundQuestScript=false;
                DVector pageDV=null;
                StringBuffer script=null;
                for(int v=0;v<V.size();v++)
                {
                    s=((String)V.elementAt(v)).trim();
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
                                templatesDV.addElement(name,"",files[f].getName(),new Vector(),script);
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
                                ((Vector)templatesDV.elementAt(templatesDV.size()-1,4)).addElement(pageDV);
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
                                int x=s.indexOf("=");
                                if(x<0)
                                    Log.errOut("Quests","Illegal QuestMaker variable syntax: "+s);
                                else
                                if(pageDV==null)
                                    Log.errOut("Quests","QuestMaker syntax error, QUESTMAKER_PAGE not yet designated: "+s);
                                else
                                {
                                    int y=s.indexOf("=",x+1);
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
        Vector choices=new Vector();
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
            if((M!=null)&&(M.savable())) 
            {
                choices.addElement(M);
                choiceDescs.append(M.name()+", ");
            }
        }
        Vector newMobs=new Vector();
        for(Enumeration e=CMClass.mobTypes();e.hasMoreElements();)
        {
            M=(MOB)e.nextElement();
            if(M.isGeneric())
            {
                M=(MOB)M.copyOf();
                newMobs.addElement(M);
                M.setName("A NEW "+M.ID().toUpperCase());
                choices.addElement(M);
                choiceDescs.append(M.name()+", ");
            }
        }
        MOB canMOB=CMClass.getMOB("StdMOB");
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
            if(C!=null) C.execute(mob,CMParms.makeVector("MODIFY",M),0);
            // modify it!
        }
        String newValue=(M!=null)?CMLib.coffeeMaker().getMobXML(M).toString():showValue;
        for(int n=0;n<newMobs.size();n++) ((MOB)newMobs.elementAt(n)).destroy();
        return newValue.trim();
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
        Vector choices=new Vector();
        Item baseI=((showValue!=null)?baseI=CMLib.coffeeMaker().getItemFromXML(showValue):null);
        StringBuffer choiceDescs=new StringBuffer("");
        if(baseI!=null){
            choices.addElement(baseI);
            choiceDescs.append(baseI.name()+", ");
        }
        Room R=mob.location();
        if(R!=null)
        for(int i=0;i<R.numItems();i++)
        {
            I=R.fetchItem(i);
            if((I!=null)&&(I.container()==null)&&(I.savable())) 
            {
                choices.addElement(I);
                choiceDescs.append(I.name()+", ");
            }
        }
        Vector allItemNames=new Vector();
        CMClass.addAllItemClassNames(allItemNames,true,false,false);
        Vector newItems=new Vector();
        for(int a=0;a<allItemNames.size();a++)
        {
            I=CMClass.getItem((String)allItemNames.elementAt(a));
            if((I!=null)&&(I.isGeneric()))
            {
                newItems.addElement(I);
                I.setName("A NEW "+I.ID().toUpperCase());
                choices.addElement(I);
                choiceDescs.append(I.name()+", ");
            }
        }
        choiceDescs.append("CANCEL");
        Item canItem=CMClass.getItem("StdItem");
        canItem.setName("CANCEL");
        choices.addElement(canItem);
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
            if(C!=null) C.execute(mob,CMParms.makeVector("MODIFY",I),0);
            // modify it!
        }
        String newValue=(I!=null)?CMLib.coffeeMaker().getItemXML(I).toString():showValue;
        for(int n=0;n<newItems.size();n++) ((Item)newItems.elementAt(n)).destroy();
        return newValue.trim();
    }
    
    public Vector<Quest> getPlayerPersistantQuests(MOB player)
    {
        Vector qVec=new Vector();
        for(int q=0;q<CMLib.quests().numQuests();q++)
        {
            Quest Q=CMLib.quests().fetchQuest(q);
            if(Q==null) continue;
            for(int s=0;s<player.numScripts();s++)
            {
                ScriptingEngine S=player.fetchScript(s);
                if(S==null) continue;
                if((S.defaultQuestName().length()>0)
                &&(S.defaultQuestName().equalsIgnoreCase(Q.name()))
                &&(!qVec.contains(Q)))
                    qVec.addElement(Q);
            }
        }
        return qVec;
    }
    
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
            while((questIndex<0)&&(mob.session()!=null)&&(!mob.session().killFlag()))
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
            if((mob.session()==null)||(mob.session().killFlag())||(questIndex<0))
                return null;
            DVector qFDV=getQuestTemplate(mob,(String)questTemplates.elementAt(questIndex,3));
            if((qFDV==null)||(qFDV.size()==0)) return null;
            String questTemplateName=(String)qFDV.elementAt(0,1);
            Vector qPages=(Vector)qFDV.elementAt(0,4);
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
                while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
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
                            for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
                                label.append("\""+((Area)e.nextElement()).name()+"\" ");
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
                            for(Enumeration e=CMClass.abilities();e.hasMoreElements();)
                                label.append(((Ability)e.nextElement()).ID()+" ");
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
                            Vector itemXMLs=new Vector();
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
                            Vector mobXMLs=new Vector();
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
                            for(Enumeration f=CMLib.factions().factions();f.hasMoreElements();)
                                label.append("\""+((Faction)f.nextElement()).name()+"\" ");
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
                if((mob.session()==null)||(mob.session().killFlag()))
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
            if((mob.session()!=null)&&(!mob.session().killFlag())
            &&(mob.session().confirm("Create the new quest: "+name+" (y/N)? ","N")))
            {
                Quest Q=(Quest)CMClass.getCommon("DefaultQuest");
                CMFile newQF=new CMFile(Resources.makeFileResourceName("quests/"+name+".quest"),mob,true,false);
                newQF.saveText(script);
                Q.setScript("LOAD=quests/"+name+".quest");
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
