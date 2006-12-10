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

    protected Object getHolidayFile()
    {
        Quest Q=fetchQuest("holidays");
        if((Q==null)
        ||(!Q.script().toUpperCase().trim().equalsIgnoreCase(holidayFilename)))
        {
            return "A quest named 'holidays', with the script definition '"+holidayDefinition+"' has not been created.  Enter the following to create this quest:\n\r"
                  +"CREATE QUEST "+holidayDefinition+"\n\r"
                  +"SAVE QUESTS";
        }
        CMFile F=new CMFile(Resources.makeFileResourceName(holidayFilename),null,false);
        if((!F.exists())||(!F.canRead()))
        {
            return "The file '"+Resources.makeFileResourceName(holidayFilename)+"' does not exist, and is required for this feature.";
        }
        Vector V=Resources.getFileLineVector(F.text());
        Vector steps=parseQuestSteps(V,0);
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
                    if(var.equalsIgnoreCase("AREAGROUP"))
                    { areaLine=line;}
                    if(var.equalsIgnoreCase("NAME"))
                    { nameLine=line;}
                }
            }
            if((areaLine!=null)&&(nameLine!=null))
            {
                boolean contains=(areaName==null);
                if(!contains)
                for(int l=2;l<areaLine.size();l++)
                    if(areaName.equalsIgnoreCase((String)areaLine.elementAt(l)))
                    {    contains=true; break;}
                if(contains)
                {
                    String name=CMParms.combine(nameLine,2);
                    str.append(CMStrings.padRight(""+s,3)+CMStrings.padRight(name,20)+CMStrings.padRight(CMParms.combineWithQuotes(areaLine,2),30)+"^?\n\r");
                }
            }
        }
        return str.toString();
    }
    
    
    protected void promptText(MOB mob, DVector sets, String var, int showNumber, int showFlag, String prompt, String help)
    throws java.io.IOException
    {
        int index=sets.indexOf(var);
        String oldVal=index>=0?(String)sets.elementAt(index,2):"";
        while((mob.session()!=null)&&(!mob.session().killFlag()))
        {
            String newVAL=CMLib.english().promptText(mob,oldVal,++showNumber,showFlag,prompt);
            if(newVAL.equals("?"))
            {
                mob.tell(help);
                continue;
            }
            else
            if(index>=0)
                sets.setElementAt(index,2,newVAL);
            else
                sets.addElement(var,newVAL,new Integer(-1));
            break;
        }
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
        Vector stepV=Resources.getFileLineVector(new StringBuffer(step));
        DVector settings=new DVector(3);
        DVector behaviors=new DVector(3);
        DVector properties=new DVector(3);
        DVector stats=new DVector(3);
        Vector lineV=null;
        String line=null;
        String var=null;
        String cmd=null;
        String[] SETTINGS={"AREAGROUP","MOBGROUP","NAME","WAIT","INTERVAL","DURATION","DATE","MUDDAY"};
        for(int v=0;v<stepV.size();v++)
        {
            line=(String)stepV.elementAt(v);
            lineV=CMParms.parse(line);
            if(lineV.size()>1)
            {
                cmd=((String)lineV.elementAt(0)).toUpperCase();
                var=((String)lineV.elementAt(1)).toUpperCase();
                if(cmd.equals("SET")&&(CMParms.indexOf(SETTINGS,var)>=0)&&(!settings.contains(var)))
                    settings.addElement(var,CMParms.combineWithQuotes(lineV,2),new Integer(v));
                if(cmd.equals("GIVE")&&(var.equalsIgnoreCase("BEHAVIOR"))&&(lineV.size()>2))
                {
                    var=((String)lineV.elementAt(2)).toUpperCase();
                    behaviors.addElement(var,CMParms.combineWithQuotes(lineV,3),new Integer(v));
                }
                if(cmd.equals("GIVE")&&(var.equalsIgnoreCase("AFFECT"))&&(lineV.size()>2))
                {
                    var=((String)lineV.elementAt(2)).toUpperCase();
                    properties.addElement(var,CMParms.combineWithQuotes(lineV,3),new Integer(v));
                }
                if(cmd.equals("GIVE")&&(var.equalsIgnoreCase("STAT"))&&(lineV.size()>2))
                {
                    var=((String)lineV.elementAt(2)).toUpperCase();
                    properties.addElement(var,CMParms.combineWithQuotes(lineV,3),new Integer(v));
                }
            }
        }
        
        if(mob.isMonster())
            return;
        boolean ok=false;
        int showFlag=-1;
        if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
            showFlag=-999;
        try{
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
            {
                int showNumber=0;
                promptText(mob,settings,"NAME",++showNumber,showFlag,"Holiday Name","It's, well, a name.");
                // show the current start date system
                
                promptText(mob,settings,"DURATION",++showNumber,showFlag,"Duration of holiday in ticks (?): ","Use a number, or math expression like 1+(5?10) would mean random 5-10 plus 1.");
                promptText(mob,settings,"MOBGROUP",++showNumber,showFlag,"Mask for mobs to apply holiday changes to\n\r",CMLib.masking().maskHelp("\n\r","disallow"));
                promptText(mob,properties,"Mood",++showNumber,showFlag,"Mood setting (?)","NULL/Empty (to not use a Mood), FORMAL, POLITE, HAPPY, SAD, ANGRY, RUDE, MEAN, PROUD, GRUMPY, EXCITED, SCARED, LONELY");
                promptText(mob,properties,"Aggressive",++showNumber,showFlag,"Aggressive setting (?)","\n\r"+CMLib.masking().maskHelp("\n\r","disallow")+"\n\r\n\r** NULL/Empty (to not use Aggressive **");
                
                //"Pricing","Pricing adjustments (?)"
                //mudChat(mob,behaviors,++showNumber,showFlag);\
                //extra behaviors
                //extra effects
                // holy day (no working)
                if(showFlag<-900){ ok=true; break;}
                if(showFlag>0){ showFlag=-1; continue;}
                showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
                if(showFlag<=0)
                {
                    showFlag=-1;
                    ok=true;
                    // save me here
                }
            }
        }catch(java.io.IOException e){return;}
    }
    
    protected void genPricing(MOB mob, DVector properties, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        int moodIndex=properties.indexOf("PRICING");
        String moodSetting=(moodIndex<0)?"":(String)properties.elementAt(moodIndex,2);
        /*mob.tell(showNumber+". Mood setting: '"+((moodSetting.length()==0)?"Not used":moodSetting)+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newValue=mob.session().prompt("Enter a new value (null=dont use) (?)\n\r:",moodSetting);
        if(newValue.equalsIgnoreCase("null"))
            E.setPrejudiceFactors("");
        else
        if(newValue.length()>0)
            E.setPrejudiceFactors(newValue);
        else
            mob.tell("(no change)");
       */
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
    
    public Vector parseQuestSteps(Vector script, int startLine)
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
            scr.append(((String)script.elementAt(v))+"\n\r");
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
                parsed=new Vector();
                scr=new StringBuffer("");
            }
        }
        if(scr.toString().trim().length()>0)
            parsed.addElement(scr);
        return parsed;
    }
    
}
