package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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

import java.io.IOException;
import java.util.*;

/**
 * <p>Portions Copyright (c) 2003 Jeremy Vyska</p>
 * <p>Portions Copyright (c) 2004 Bo Zimmerman</p>
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * <p>you may not use this file except in compliance with the License.
 * <p>You may obtain a copy of the License at
 *
 * <p>       http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * <p>distributed under the License is distributed on an "AS IS" BASIS,
 * <p>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>See the License for the specific language governing permissions and
 * <p>limitations under the License.
 */

public class Factions extends StdLibrary implements FactionManager
{
    public String ID(){return "Factions";}
	public Hashtable factionSet = new Hashtable();
	public Hashtable hashedFactionRanges=new Hashtable();
	
    public Hashtable factionSet(){return factionSet;}
	public void clearFactions()
	{
	    factionSet.clear();
	    hashedFactionRanges.clear();
	}
    public void reloadFactions(String factionList)
    {
        Vector preLoadFactions=CMParms.parseSemicolons(factionList,true);
        clearFactions();
        for(int i=0;i<preLoadFactions.size();i++)
            getFaction((String)preLoadFactions.elementAt(i));
    }
	
	public Hashtable rangeCodeNames(){ return hashedFactionRanges; }
	public boolean isRangeCodeName(String key){ return rangeCodeNames().containsKey(key.toUpperCase());}
	public boolean isFactionedThisWay(MOB mob, String rangeCodeName)
	{
	    Faction.FactionRange FR=(Faction.FactionRange)rangeCodeNames().get(rangeCodeName.toUpperCase());
	    if(FR==null) return false;
	    Faction.FactionRange FR2=FR.myFaction().fetchRange(mob.fetchFaction(FR.myFaction().factionID()));
	    if(FR2==null) return false;
	    return FR2.codeName().equalsIgnoreCase(FR.codeName());
	}
	public String rangeDescription(String rangeCodeName, String andOr)
	{
	    Faction.FactionRange FR=(Faction.FactionRange)rangeCodeNames().get(rangeCodeName.toUpperCase());
	    if((FR==null)||(FR.myFaction()==null)||(FR.myFaction().ranges().size()<=0))
	        return "";
	    Vector relevantFactions=new Vector();
	    for(int r=0;r<FR.myFaction().ranges().size();r++)
	    {
	        if((((Faction.FactionRange)FR.myFaction().ranges().elementAt(r)).codeName().equalsIgnoreCase(FR.codeName())))
	            relevantFactions.addElement(FR.myFaction().ranges().elementAt(r));
	    }
	    if(relevantFactions.size()==0) return "";
	    if(relevantFactions.size()==1)
	        return FR.myFaction().name()+" of "+((Faction.FactionRange)relevantFactions.firstElement()).name();
	    StringBuffer buf=new StringBuffer(FR.myFaction().name()+" of ");
	    for(int i=0;i<relevantFactions.size()-1;i++)
	        buf.append(((Faction.FactionRange)relevantFactions.elementAt(i)).name()+", ");
        buf.append(andOr+((Faction.FactionRange)relevantFactions.lastElement()).name());
        return buf.toString();
	}
	    
	
	
	public Faction getFaction(String factionID) 
	{
	    if(factionID==null) return null;
		Faction F=(Faction)factionSet.get(factionID.toUpperCase());
		if(F!=null) return F;
        StringBuffer buf=new CMFile(Resources.makeFileResourceName(factionID),null,true).text();
	    if((buf!=null)&&(buf.length()>0))
	    {
            F=(Faction)CMClass.getCommon("DefaultFaction");
            F.initializeFaction(buf,factionID);
            for(int r=0;r<F.ranges().size();r++)
            {
                Faction.FactionRange FR=(Faction.FactionRange)F.ranges().elementAt(r);
                String CodeName=(FR.codeName().length()>0)?FR.codeName().toUpperCase():FR.name().toUpperCase();
                if(!hashedFactionRanges.containsKey(CodeName))
                    hashedFactionRanges.put(CodeName,FR);
            }
            factionSet.put(factionID.toUpperCase(),F);
            return F;
	    }
        return null;
	}
	
	public Faction getFactionByRangeCodeName(String rangeCodeName)
	{
	    if(hashedFactionRanges.containsKey(rangeCodeName.toUpperCase()))
	        return (Faction)hashedFactionRanges.get(rangeCodeName.toUpperCase());
	    return null;
	}
	
	public Faction getFactionByName(String factionNamed) 
	{
	    for(Enumeration e=factionSet.keys();e.hasMoreElements();) 
	    {
	        Faction f=(Faction)factionSet.get(e.nextElement());
	        if(f.name().equalsIgnoreCase(factionNamed)) return f;
	    }
	    return null;
	}
	
	public boolean removeFaction(String factionID) 
	{
	    if(factionID==null) 
	    {
	        for(Enumeration e=factionSet.keys();e.hasMoreElements();) 
	        {
	            Faction f=(Faction)factionSet.get(e.nextElement());
	            removeFaction(f.factionID());
	        }
	        return true;
	    }
        Faction F=getFactionByName(factionID);
        if(F==null) F=getFaction(factionID);
        if(F==null) return false;
        Resources.removeResource(F.factionID());
        factionSet.remove(F.factionID().toUpperCase());
        return true;
	}
	
	public String listFactions() 
	{
	    StringBuffer msg=new StringBuffer();
	    msg.append("\n\r^.^N");
	    msg.append("+--------------------------------+-----------------------------------------+\n\r");
	    msg.append("| ^HFaction Name^N                   | ^HFaction INI Source File (Faction ID)^N    |\n\r");
	    msg.append("+--------------------------------+-----------------------------------------+\n\r");
	    for(Enumeration e=factionSet.keys();e.hasMoreElements();) 
	    {
	        Faction f=(Faction)factionSet.get(e.nextElement());
	        msg.append("| ");
	        msg.append(CMStrings.padRight(f.name(),30));
	        msg.append(" | ");
	        msg.append(CMStrings.padRight(f.factionID(),39));
	        msg.append(" |\n\r");
	    }
	    msg.append("+--------------------------------+-----------------------------------------+\n\r");
	    msg.append("\n\r");
	    return msg.toString();
	}
	
	public String name(){return "Factions";}
	public long getTickStatus(){ return Tickable.STATUS_NOT;}
	public String getName(String factionID) {  Faction f=getFaction(factionID); if(f!=null) return f.name(); return ""; }
	public int getMinimum(String factionID) {  Faction f=getFaction(factionID); if(f!=null) return f.minimum(); return 0; }
	public int getMaximum(String factionID) {  Faction f=getFaction(factionID); if(f!=null) return f.maximum(); return 0; }
	public int getPercent(String factionID, int faction) { Faction f=getFaction(factionID); if(f!=null) return f.asPercent(faction); return 0; }
	public int getPercentFromAvg(String factionID, int faction) { Faction f=getFaction(factionID); if(f!=null) return f.asPercentFromAvg(faction); return 0; }
	public Faction.FactionRange getRange(String factionID, int faction) { Faction f=getFaction(factionID); if(f!=null) return f.fetchRange(faction); return null; }
	public Vector getRanges(String factionID) { Faction f=getFaction(factionID); if(f!=null) return f.ranges(); return null; }
	public double getRangePercent(String factionID, int faction) 
    { 
        Faction F=getFaction(factionID); 
        if(F==null) return 0.0;
        return CMath.div((int)Math.round(CMath.div((faction - F.minimum()),(F.maximum() - F.minimum())) * 10000.0),100.0);
    }
	public double getRateModifier(String factionID) {  Faction f=getFaction(factionID); if(f!=null) return f.rateModifier(); return 0; }
	public int getTotal(String factionID) {  Faction f=getFaction(factionID); if(f!=null) return (f.maximum()-f.minimum()); return 0; }
	public int getRandom(String factionID) {  Faction f=getFaction(factionID); if(f!=null) return f.randomFaction(); return 0; }
	
	public String AlignID() { return "alignment.ini"; }
	public void setAlignment(MOB mob, int newAlignment)
	{
	    if(getFaction(AlignID())!=null) 
	        mob.addFaction(AlignID(),getAlignThingie(newAlignment));
	}
	
	public void setAlignmentOldRange(MOB mob, int oldRange)
	{
		if(getFaction(AlignID())!=null)
		{
			if(oldRange>=650)
				setAlignment(mob,Faction.ALIGN_GOOD);
			else
			if(oldRange>=350) 
			    setAlignment(mob,Faction.ALIGN_NEUTRAL);
			else
			if(oldRange>=0) 
			    setAlignment(mob,Faction.ALIGN_EVIL);
			else{ /* a -1 value is the new norm */}
		}
	}
	public boolean tick(Tickable ticking, int tickID)
	{
        if(CMLib.sessions().size()==0) 
            return true;
	    try
	    {
		    Session S=null;
		    MOB mob=null;
		    Faction F=null;
		    Faction.FactionChangeEvent CE=null; 
            DVector outSiders=new DVector(2);
            DVector timers=new DVector(2);
            for(Enumeration e=factionSet.elements();e.hasMoreElements();)
            {
                F=(Faction)e.nextElement();
                CE=F.findChangeEvent("ADDOUTSIDER");
                if(CE!=null) outSiders.addElement(CE,F);
                CE=F.findChangeEvent("TIME");
                if(CE!=null) timers.addElement(CE,F);
            }
            if((outSiders.size()==0)&&(timers.size()==0)) 
                return true;
		    for(int s=0;s<CMLib.sessions().size();s++)
		    {
		        S=CMLib.sessions().elementAt(s);
		        mob=(!S.killFlag())?S.mob():null;
		        if(mob!=null)
		        {
                    for(int o=0;o<outSiders.size();o++)
		            {
		                CE=(Faction.FactionChangeEvent)outSiders.elementAt(o,1);
                        F=(Faction)outSiders.elementAt(o,2);
		                if((CE.applies(mob))&&(!F.hasFaction(mob)))
		                    F.executeChange(mob,mob,CE);
		            }
                    for(int o=0;o<timers.size();o++)
                    {
                        CE=(Faction.FactionChangeEvent)timers.elementAt(o,1);
                        F=(Faction)timers.elementAt(o,2);
		                if((CE.applies(mob))&&(F.hasFaction(mob)))
		                    F.executeChange(mob,mob,CE);
		            }
		        }
		    }
	    }catch(Exception e){}
	    return true;
	}
	
	public int getAlignPurity(int faction, int AlignEq) 
	{
		int bottom=Integer.MAX_VALUE;
		int top=Integer.MIN_VALUE;
        int pct=getPercent(AlignID(),faction);
		Vector ranges = getRanges(AlignID());
		for(int i=0;i<ranges.size();i++) 
        {
			Faction.FactionRange R=(Faction.FactionRange)ranges.elementAt(i);
			if(R.alignEquiv()==AlignEq) 
            {
				if(R.low()<bottom) bottom=R.low();
				if(R.high()>top) top=R.high();
			}
		}
		switch(AlignEq) 
        {
			case Faction.ALIGN_GOOD:
				return Math.abs(pct - getPercent(AlignID(),top));
			case Faction.ALIGN_EVIL:
				return Math.abs(getPercent(AlignID(),bottom) - pct);
			case Faction.ALIGN_NEUTRAL:
				return Math.abs(getPercent(AlignID(),(int)Math.round(CMath.div((top+bottom),2))) - pct);
			default:
				return 0;
		}
	}
	
	// Please don't mock the name, I couldn't think of a better one.  Sadly.
	public int getAlignThingie(int AlignEq) 
	{
        int bottom=Integer.MAX_VALUE;
        int top=Integer.MIN_VALUE;
		Vector ranges = getRanges(AlignID());
	    if(ranges==null) return 0;
		for(int i=0;i<ranges.size();i++) 
        {
			Faction.FactionRange R=(Faction.FactionRange)ranges.elementAt(i);
			if(R.alignEquiv()==AlignEq) {
				if(R.low()<bottom) bottom=R.low();
				if(R.high()>top) top=R.high();
			}
		}
		switch(AlignEq) 
		{
			case Faction.ALIGN_GOOD:
				return top;
			case Faction.ALIGN_EVIL:
				return bottom;
			case Faction.ALIGN_NEUTRAL:
				return (int)Math.round(CMath.div((top+bottom),2));
			default:
				return 0;
		}
	}
    public int isFactionTag(String tag)
    {
        for(int i=0;i<Faction.ALL_TAGS.length;i++)
            if(tag.equalsIgnoreCase(Faction.ALL_TAGS[i]))
                return i;
            else
            if(Faction.ALL_TAGS[i].endsWith("*")&&tag.startsWith(Faction.ALL_TAGS[i].substring(0,Faction.ALL_TAGS[i].length()-1)))
                return i;
        return -1;
    }
    public int getAlignEquiv(String str)
    {
        if(str.equalsIgnoreCase(Faction.ALIGN_NAMES[Faction.ALIGN_GOOD])) 
            return Faction.ALIGN_GOOD;
        else 
        if(str.equalsIgnoreCase(Faction.ALIGN_NAMES[Faction.ALIGN_NEUTRAL])) 
            return Faction.ALIGN_NEUTRAL;
        else 
        if(str.equalsIgnoreCase(Faction.ALIGN_NAMES[Faction.ALIGN_EVIL])) 
            return  Faction.ALIGN_EVIL;
        else 
            return  Faction.ALIGN_INDIFF;
    }
    
    
    public void modifyFaction(MOB mob, Faction me) throws IOException
    {
        if(mob.isMonster())
            return;
        boolean ok=false;
        int showFlag=-1;
        if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
            showFlag=-999;
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
        {
            int showNumber=0;
            // name
            me.setName(CMLib.english().prompt(mob,me.name(),++showNumber,showFlag,"Name"));

            // ranges
            ++showNumber;
            if(me.ranges().size()==0)
                me.ranges().addElement(me.newRange("0;100;Sample Range;SAMPLE;"));
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                StringBuffer list=new StringBuffer(showNumber+". Faction Division/Ranges List:\n\r");
                list.append(CMStrings.padRight("   Name",21)+CMStrings.padRight("Min",11)+CMStrings.padRight("Max",11)+CMStrings.padRight("Code",16)+CMStrings.padRight("Align",6)+"\n\r");
                for(int r=0;r<me.ranges().size();r++)
                {
                    Faction.FactionRange FR=(Faction.FactionRange)me.ranges().elementAt(r);
                    list.append(CMStrings.padRight("   "+FR.name(),20)+" ");
                    list.append(CMStrings.padRight(""+FR.low(),10)+" ");
                    list.append(CMStrings.padRight(""+FR.high(),10)+" ");
                    list.append(CMStrings.padRight(FR.codeName(),15)+" ");
                    list.append(CMStrings.padRight(Faction.ALIGN_NAMES[FR.alignEquiv()],5)+"\n\r");
                }
                mob.tell(list.toString());
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                String which=mob.session().prompt("Enter a name to add, remove, or modify:","");
                if(which.length()==0)
                    break;
                Faction.FactionRange FR=null;
                for(int r=0;r<me.ranges().size();r++)
                {
                    if(((Faction.FactionRange)me.ranges().elementAt(r)).name().equalsIgnoreCase(which))
                        FR=(Faction.FactionRange)me.ranges().elementAt(r);
                }
                if(FR==null)
                {
                    if(mob.session().confirm("Create a new range called '"+which+"' (y/N): ","N"))
                    {
                        FR=me.newRange("0;100;"+which+";CHANGEMYCODENAME;");
                        me.ranges().addElement(FR);
                    }
                }
                else
                if(mob.session().choose("Would you like to M)odify or D)elete this range (M/d): ","MD","M").toUpperCase().startsWith("D"))
                {
                    me.ranges().remove(FR);
                    mob.tell("Range deleted.");
                    FR=null;
                }
                if(FR!=null)
                {
                    String newName=mob.session().prompt("Enter a new name ("+FR.name()+")\n\r: "+FR.name());
                    boolean error99=false;
                    if(newName.length()==0)
                        error99=true;
                    else
                    for(int r=0;r<me.ranges().size();r++)
                    {
                        Faction.FactionRange FR3=(Faction.FactionRange)me.ranges().elementAt(r);
                        if(FR3.name().equalsIgnoreCase(FR.name())&&(FR3!=FR))
                        { mob.tell("A range already exists with that name!"); error99=true; break;}
                    }
                    if(error99)
                        mob.tell("(no change)");
                    else
                        FR.setName(newName);
                    newName=mob.session().prompt("Enter the low end of the range ("+FR.low()+")\n\r: ",""+FR.low());
                    if(!CMath.isInteger(newName))
                        mob.tell("(no change)");
                    else
                        FR.setLow(CMath.s_int(newName));
                    newName=mob.session().prompt("Enter the high end of the range ("+FR.high()+")\n\r: ",""+FR.high());
                    if((!CMath.isInteger(newName))||(CMath.s_int(newName)<FR.low()))
                        mob.tell("(no change)");
                    else
                        FR.setHigh(CMath.s_int(newName));
                    newName=mob.session().prompt("Enter a code-name ("+FR.codeName()+")\n\r: ",""+FR.codeName());
                    if(newName.trim().length()==0)
                        mob.tell("(no change)");
                    else
                    {
                        Faction FC=CMLib.factions().getFactionByRangeCodeName(newName.toUpperCase().trim());
                        if((FC!=null)&&(FC!=me))
                            mob.tell("That code name is already being used in another faction.  Value not accepted.");
                        else
                            FR.setCodeName(newName.toUpperCase().trim());
                    }
                    StringBuffer prompt=new StringBuffer("Select the 'virtue' (if any) of this range:\n\r");
                    StringBuffer choices=new StringBuffer("");
                    for(int r=0;r<Faction.ALIGN_NAMES.length;r++)
                    {
                        choices.append(""+r);
                        if(r==Faction.ALIGN_INDIFF)
                            prompt.append(r+") Not applicable\n\r");
                        else
                            prompt.append(r+") "+Faction.ALIGN_NAMES[r].toLowerCase()+"\n\r");
                    }
                    FR.setAlignEquiv(CMath.s_int(mob.session().choose(prompt.toString()+"Enter alignment equivalency or 0: ",choices.toString(),""+FR.alignEquiv())));
                }
            }


            // show in score
            me.setShowinscore(CMLib.english().prompt(mob,me.showinscore(),++showNumber,showFlag,"Show in 'Score'"));

            // show in factions
            me.setShowinfactionscommand(CMLib.english().prompt(mob,me.showinfactionscommand(),++showNumber,showFlag,"Show in 'Factions' command"));

            // show in special reports
            boolean alreadyReporter=false;
            for(Enumeration e=CMLib.factions().factionSet().elements();e.hasMoreElements();)
            {
                Faction F2=(Faction)e.nextElement();
                if(F2.showinspecialreported()) alreadyReporter=true;
            }
            if(!alreadyReporter)
                me.setShowinspecialreported(CMLib.english().prompt(mob,me.showinspecialreported(),++showNumber,showFlag,"Show in Reports"));

            // show in editor
            me.setShowineditor(CMLib.english().prompt(mob,me.showineditor(),++showNumber,showFlag,"Show in MOB Editor"));

            // auto defaults
            boolean error=true;
            me.setAutoDefaults(CMParms.parseSemicolons(CMLib.english().prompt(mob,CMParms.toSemicolonList(me.autoDefaults()),++showNumber,showFlag,"Optional automatic assigned values with zapper masks (semicolon delimited).\n\r    "),true));

            // non-auto defaults
            error=true;
            if(me.defaults().size()==0)
                me.defaults().addElement("0");
            ++showNumber;
            while(error&&(mob.session()!=null)&&(!mob.session().killFlag()))
            {
                error=false;
                String newDefaults=CMLib.english().prompt(mob,CMParms.toSemicolonList(me.defaults()),showNumber,showFlag,"Other default values with zapper masks (semicolon delimited).\n\r    ");
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                Vector V=CMParms.parseSemicolons(newDefaults,true);
                if(V.size()==0)
                {
                    mob.tell("This field may not be empty.");
                    error=true;
                }
                me.setDefaults(CMParms.parseSemicolons(newDefaults,true));
            }

            // choices and choice intro
            me.setChoices(CMParms.parseSemicolons(CMLib.english().prompt(mob,CMParms.toSemicolonList(me.choices()),++showNumber,showFlag,"Optional new player value choices (semicolon-delimited).\n\r    "),true));
            if(me.choices().size()>0)
                me.setChoiceIntro(CMLib.english().prompt(mob,me.choiceIntro(),++showNumber,showFlag,"Optional choices introduction text. Filename"));

            // rate modifier
            String newModifier=CMLib.english().prompt(mob,Math.round(me.rateModifier()*100.0)+"%",++showNumber,showFlag,"Rate modifier");
            if(newModifier.endsWith("%"))
                newModifier=newModifier.substring(0,newModifier.length()-1);
            if(CMath.isNumber(newModifier))
                me.setRateModifier(CMath.s_double(newModifier)/100.0);

            // experience flag
            boolean error2=true;
            ++showNumber;
            while(error2&&(mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                error2=false;
                StringBuffer nextPrompt=new StringBuffer("\n\r");
                int myval=-1;
                for(int i=0;i<Faction.EXPAFFECT_NAMES.length;i++)
                {
                    if(me.experienceFlag().equalsIgnoreCase(Faction.EXPAFFECT_NAMES[i]))
                        myval=i;
                    nextPrompt.append("  "+(i+1)+") "+CMStrings.capitalizeAndLower(Faction.EXPAFFECT_NAMES[i].toLowerCase())+"\n\r");
                }
                if(myval<0){ me.setExperienceFlag("NONE"); myval=0;}
                if((showFlag!=showNumber)&&(showFlag>-999))
                {
                    mob.tell(showNumber+". Affect on experience: "+Faction.EXPAFFECT_NAMES[myval]);
                    break;
                }
                String prompt="Affect on experience:  "+Faction.EXPAFFECT_NAMES[myval]+nextPrompt.toString()+"\n\rSelect a value: ";
                int mynewval=CMLib.english().prompt(mob,myval+1,showNumber,showFlag,prompt);
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                if((mynewval<=0)||(mynewval>Faction.EXPAFFECT_NAMES.length))
                {
                    mob.tell("That value is not valid.");
                    error2=true;
                }
                else
                    me.setExperienceFlag(Faction.EXPAFFECT_NAMES[mynewval-1]);
            }

            // factors by mask
            ++showNumber;
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                StringBuffer list=new StringBuffer(showNumber+". Faction change adjustment Factors with Zapper Masks:\n\r");
                list.append("    #) "+CMStrings.padRight("Zapper Mask",31)+CMStrings.padRight("Gain",6)+CMStrings.padRight("Loss",6)+"\n\r");
                StringBuffer choices=new StringBuffer("");
                for(int r=0;r<me.factors().size();r++)
                {
                    Vector factor=(Vector)me.factors().elementAt(r);
                    if(factor.size()!=3)
                        me.factors().removeElement(factor);
                    else
                    {
                        choices.append(((char)('A'+r)));
                        list.append("    "+(((char)('A'+r))+") "));
                        list.append(CMStrings.padRight((String)factor.elementAt(2),30)+" ");
                        list.append(CMStrings.padRight(""+Math.round(CMath.s_double((String)factor.elementAt(0))*100.0)+"%",5)+" ");
                        list.append(CMStrings.padRight(""+Math.round(CMath.s_double((String)factor.elementAt(1))*100.0)+"%",5)+"\n\r");
                    }
                }
                mob.tell(list.toString());
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                String which=mob.session().choose("Enter a # to remove, or modify, or enter 0 to Add:","0"+choices.toString(),"").trim().toUpperCase();
                int factorNum=choices.toString().indexOf(which);
                if((which.length()!=1)
                ||((!which.equalsIgnoreCase("0"))
                    &&((factorNum<0)||(factorNum>=me.factors().size()))))
                    break;
                Vector factor=null;
                if(!which.equalsIgnoreCase("0"))
                {
                    factor=(Vector)me.factors().elementAt(factorNum);
                    if(factor!=null)
                        if(mob.session().choose("Would you like to M)odify or D)elete this range (M/d): ","MD","M").toUpperCase().startsWith("D"))
                        {
                            me.factors().remove(factor);
                            mob.tell("Factor deleted.");
                            factor=null;
                        }
                }
                else
                {
                    factor=new Vector();
                    factor.addElement("1.0");
                    factor.addElement("1.0");
                    factor.addElement("");
                    me.factors().addElement(factor);
                }
                if(factor!=null)
                {
                    String mask=mob.session().prompt("Enter a new zapper mask ("+((String)factor.elementAt(2))+")\n\r: "+((String)factor.elementAt(2)));
                    double newHigh=CMath.s_double((String)factor.elementAt(0));
                    String newName=mob.session().prompt("Enter gain adjustment ("+Math.round(newHigh*100)+"%): "+Math.round(newHigh*100)+"".trim()+"%");
                    if(newName.endsWith("%"))
                        newName=newName.substring(0,newName.length()-1);
                    if(!CMath.isNumber(newName))
                        mob.tell("(no change)");
                    else
                        newHigh=CMath.s_double(newName)/100.0;

                    double newLow=CMath.s_double((String)factor.elementAt(1));
                    newName=mob.session().prompt("Enter loss adjustment ("+Math.round(newLow*100)+"%): "+Math.round(newLow*100)+"".trim()+"%");
                    if(newName.endsWith("%"))
                        newName=newName.substring(0,newName.length()-1);
                    if(!CMath.isNumber(newName))
                        mob.tell("(no change)");
                    else
                        newLow=CMath.s_double(newName)/100.0;
                    me.factors().removeElement(factor);
                    factor=new Vector();
                    factor.addElement(""+newHigh);
                    factor.addElement(""+newLow);
                    factor.addElement(""+mask);
                    me.factors().addElement(factor);
                }
            }

            // relations between factions
            ++showNumber;
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                StringBuffer list=new StringBuffer(showNumber+". Cross-Faction Relations:\n\r");
                list.append("    Faction"+CMStrings.padRight("",25)+"Percentage change\n\r");
                for(Enumeration e=me.relations().keys();e.hasMoreElements();)
                {
                    String key=(String)e.nextElement();
                    Double value=(Double)me.relations().get(key);
                    Faction F=CMLib.factions().getFaction(key);
                    if(F!=null)
                    {
                        list.append("    "+CMStrings.padRight(F.name(),31)+" ");
                        long lval=Math.round(value.doubleValue()*100.0);
                        list.append(lval+"%");
                        list.append("\n\r");
                    }
                }
                mob.tell(list.toString());
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                String which=mob.session().prompt("Enter a faction to add, remove, or modify relations:","");
                if(which.length()==0)
                    break;
                Faction theF=null;
                for(Enumeration e=me.relations().keys();e.hasMoreElements();)
                {
                    String key=(String)e.nextElement();
                    Faction F=CMLib.factions().getFaction(key);
                    if((F!=null)&&(F.name().equalsIgnoreCase(which)))
                        theF=F;
                }
                if(theF==null)
                {
                    Faction possibleF=CMLib.factions().getFaction(which);
                    if(possibleF==null) possibleF=CMLib.factions().getFactionByName(which);
                    if(possibleF==null)
                        mob.tell("'"+which+"' is not a valid faction.");
                    else
                    if(mob.session().confirm("Create a new relation for faction  '"+possibleF.name()+"' (y/N):","N"))
                    {
                        theF=possibleF;
                        me.relations().put(theF.factionID(),new Double(1.0));
                    }
                }
                else
                if(mob.session().choose("Would you like to M)odify or D)elete this relation (M/d): ","MD","M").toUpperCase().startsWith("D"))
                {
                    me.relations().remove(theF.factionID());
                    mob.tell("Relation deleted.");
                    theF=null;
                }
                if(theF!=null)
                {
                    long amount=Math.round(((Double)me.relations().get(theF.factionID())).doubleValue()*100.0);
                    String newName=mob.session().prompt("Enter a relation amount ("+amount+"%): ",""+amount+"%");
                    if(newName.endsWith("%")) newName=newName.substring(0,newName.length()-1);
                    if(!CMath.isInteger(newName))
                        mob.tell("(no change)");
                    else
                        amount=CMath.s_long(newName);
                    me.relations().remove(theF.factionID());
                    me.relations().put(theF.factionID(),new Double(amount/100.0));
                }
            }

            // faction change triggers
            ++showNumber;
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                StringBuffer list=new StringBuffer(showNumber+". Faction Change Triggers:\n\r");
                list.append("    "+CMStrings.padRight("Type",15)
                        +" "+CMStrings.padRight("Direction",10)
                        +" "+CMStrings.padRight("Factor",10)
                        +" "+CMStrings.padRight("Flags",20)
                        +" Mask\n\r");
                for(Enumeration e=me.Changes().elements();e.hasMoreElements();)
                {
                    Faction.FactionChangeEvent CE=(Faction.FactionChangeEvent)e.nextElement();
                    if(CE!=null)
                    {
                        list.append("    ");
                        list.append(CMStrings.padRight(CE.eventID(),15)+" ");
                        list.append(CMStrings.padRight(Faction.FactionChangeEvent.FACTION_DIRECTIONS[CE.direction()],10)+" ");
                        list.append(CMStrings.padRight(Math.round(CE.factor()*100.0)+"%",10)+" ");
                        list.append(CMStrings.padRight(CE.flagCache(),20)+" ");
                        list.append(CE.zapper()+"\n\r");
                    }
                }
                mob.tell(list.toString());
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                String which=mob.session().prompt("Select a trigger ID to add, remove, or modify (?):","");
                which=which.toUpperCase().trim();
                if(which.length()==0) break;
                if(which.equalsIgnoreCase("?"))
                {
                    mob.tell("Valid triggers: \n\r"+me.ALL_CHANGE_EVENT_TYPES());
                    continue;
                }
                Faction.FactionChangeEvent CE=(Faction.FactionChangeEvent)me.Changes().get(which);
                if(CE==null)
                {
                    CE=me.newChangeEvent();
                    if(!CE.setFilterID(which))
                    {
                        mob.tell("That ID is invalid.  Try '?'.");
                        continue;
                    }
                    else
                    if(!mob.session().confirm("Create a new trigger using ID '"+which+"' (y/N): ","N"))
                    {
                        CE=null;
                        break;
                    }
                    else
                        me.Changes().put(CE.eventID().toUpperCase(),CE);
                }
                else
                if(mob.session().choose("Would you like to M)odify or D)elete this trigger (M/d): ","MD","M").toUpperCase().startsWith("D"))
                {
                    me.Changes().remove(CE.eventID());
                    mob.tell("Trigger deleted.");
                    CE=null;
                }

                if(CE!=null)
                {
                    StringBuffer directions=new StringBuffer("Valid directions:\n\r");
                    StringBuffer cmds=new StringBuffer("");
                    for(int i=0;i<Faction.FactionChangeEvent.FACTION_DIRECTIONS.length;i++)
                    {
                        directions.append(((char)('A'+i))+") "+Faction.FactionChangeEvent.FACTION_DIRECTIONS[i]+"\n\r");
                        cmds.append((char)('A'+i));
                    }
                    String str=mob.session().choose(directions+"\n\rSelect a new direction ("+Faction.FactionChangeEvent.FACTION_DIRECTIONS[CE.direction()]+"): ",cmds.toString()+"\n\r","");
                    if((str.length()==0)||str.equals("\n")||str.equals("\r")||(cmds.toString().indexOf(str.charAt(0))<0))
                        mob.tell("(no change)");
                    else
                        CE.setDirection((cmds.toString().indexOf(str.charAt(0))));
                }
                if(CE!=null)
                {
                    if(CE.factor()==0.0) CE.setFactor(1.0);
                    int amount=(int)Math.round(CE.factor()*100.0);
                    String newName=mob.session().prompt("Enter the amount factor ("+amount+"%): ",""+amount+"%");
                    if(newName.endsWith("%")) newName=newName.substring(0,newName.length()-1);
                    if(!CMath.isInteger(newName))
                        mob.tell("(no change)");
                    else
                        CE.setFactor(new Double(CMath.s_int(newName)/100.0).doubleValue());
                }
                if(CE!=null)
                {
                    mob.tell("Valid flags include: "+CMParms.toStringList(Faction.FactionChangeEvent.VALID_FLAGS)+"\n\r");
                    String newFlags=mob.session().prompt("Enter new flag(s) ("+CE.flagCache()+"): "+CE.flagCache());
                    if((newFlags.length()==0)||(newFlags.equals(CE.flagCache())))
                        mob.tell("(no change)");
                    else
                        CE.setFlags(newFlags);
                }
                if(CE!=null)
                {
                    String newFlags=mob.session().prompt("Zapper mask ("+CE.zapper()+"): "+CE.zapper());
                    if((newFlags.length()==0)||(newFlags.equals(CE.zapper())))
                        mob.tell("(no change)");
                    else
                        CE.setZapper(newFlags);
                }
            }

            // Ability allowances
            ++showNumber;
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                if((showFlag>0)&&(showFlag!=showNumber)) break;
                StringBuffer list=new StringBuffer(showNumber+". Ability allowances:\n\r");
                list.append("    #) "
                        +CMStrings.padRight("Ability masks",40)
                        +" "+CMStrings.padRight("Low value",10)
                        +" "+CMStrings.padRight("High value",10)
                        +"\n\r");
                int num=0;
                StringBuffer choices=new StringBuffer("0\n\r");
                for(Enumeration e=me.abilityUsages().elements();e.hasMoreElements();)
                {
                    Faction.FactionAbilityUsage CA=(Faction.FactionAbilityUsage)e.nextElement();
                    if(CA!=null)
                    {
                        list.append("    "+((char)('A'+num)+") "));
                        list.append(CMStrings.padRight(CA.usageID(),40)+" ");
                        list.append(CMStrings.padRight(CA.low()+"",10)+" ");
                        list.append(CMStrings.padRight(CA.high()+"",10)+" ");
                        list.append("\n\r");
                        choices.append((char)('A'+num));
                        num++;
                    }
                }
                mob.tell(list.toString());
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                String which=mob.session().choose("Select an allowance to remove or modify, or enter 0 to Add:",choices.toString(),"");
                if(which.length()!=1)
                    break;
                which=which.toUpperCase().trim();
                Faction.FactionAbilityUsage CA=null;
                if(!which.equalsIgnoreCase("0"))
                {
                    num=(which.charAt(0)-'A');
                    if((num<0)||(num>=me.abilityUsages().size()))
                        break;
                    CA=(Faction.FactionAbilityUsage)me.abilityUsages().elementAt(num);
                    if(CA==null)
                    {
                        mob.tell("That allowance is invalid..");
                        continue;
                    }
                    if(mob.session().choose("Would you like to M)odify or D)elete this allowance (M/d): ","MD","M").toUpperCase().startsWith("D"))
                    {
                        me.abilityUsages().remove(CA);
                        mob.tell("Allowance deleted.");
                        CA=null;
                    }
                }
                else
                if(!mob.session().confirm("Create a new allowance (y/N): ","N"))
                {
                    CA=null;
                    continue;
                }
                else
                {
                    CA=me.newAbilityUsage();
                    me.abilityUsages().addElement(CA);
                }
                if(CA!=null)
                {
                    boolean cont=false;
                    while((!cont)&&(!mob.session().killFlag()))
                    {
                        String newFlags=mob.session().prompt("Ability determinate masks or ? ("+CA.usageID()+"): "+CA.usageID());
                        if(newFlags.equalsIgnoreCase("?"))
                        {
                            StringBuffer vals=new StringBuffer("Valid masks: \n\r");
                            for(int i=0;i<Ability.ACODE_DESCS.length;i++)
                                vals.append(Ability.ACODE_DESCS[i]+", ");
                            for(int i=0;i<Ability.DOMAIN_DESCS.length;i++)
                                vals.append(Ability.DOMAIN_DESCS[i]+", ");
                            for(int i=0;i< Ability.FLAG_DESCS.length;i++)
                                vals.append(Ability.FLAG_DESCS[i]+", ");
                            vals.append(" * Any ABILITY ID (skill/prayer/spell/etc)");
                            mob.tell(vals.toString());
                            cont=false;
                        }
                        else
                        {
                            cont=true;
                            if((newFlags.length()==0)||(newFlags.equals(CA.usageID())))
                                mob.tell("(no change)");
                            else
                            {
                                Vector unknowns=CA.setAbilityFlag(newFlags);
                                if(unknowns.size()>0)
                                    for(int i=unknowns.size()-1;i>=0;i--)
                                        if(CMClass.getAbility((String)unknowns.elementAt(i))!=null)
                                            unknowns.removeElementAt(i);
                                if(unknowns.size()>0)
                                {
                                    mob.tell("The following are unknown masks: '"+CMParms.toStringList(unknowns)+"'.  Please correct them.");
                                    cont=false;
                                }
                            }
                        }
                    }
                    String newName=mob.session().prompt("Enter the minimum value to use the ability ("+CA.low()+"): ",""+CA.low());
                    if((!CMath.isInteger(newName))||(CA.low()==CMath.s_int(newName)))
                        mob.tell("(no change)");
                    else
                        CA.setLow(CMath.s_int(newName));
                    newName=mob.session().prompt("Enter the maximum value to use the ability ("+CA.high()+"): ",""+CA.high());
                    if((!CMath.isInteger(newName))||(CA.high()==CMath.s_int(newName)))
                        mob.tell("(no change)");
                    else
                        CA.setHigh(CMath.s_int(newName));
                    if(CA.high()<CA.low()) CA.setHigh(CA.low());
                }
            }

            // calculate new max/min
            me.setMinimum(Integer.MAX_VALUE);
            me.setMaximum(Integer.MIN_VALUE);
            for(int r=0;r<me.ranges().size();r++)
            {
                Faction.FactionRange FR=(Faction.FactionRange)me.ranges().elementAt(r);
                if(FR.high()>me.maximum()) me.setMaximum(FR.high());
                if(FR.low()<me.minimum()) me.setMinimum(FR.low());
            }
            if(me.minimum()==Integer.MAX_VALUE) me.setMinimum(Integer.MIN_VALUE);
            if(me.maximum()==Integer.MIN_VALUE) me.setMaximum(Integer.MAX_VALUE);
            if(me.maximum()<me.minimum())
            {
                int oldMin=me.minimum();
                me.setMinimum(me.maximum());
                me.setMaximum(oldMin);
            }
            me.setMiddle(me.minimum()+(int)Math.round(CMath.div(me.maximum()-me.minimum(),2.0)));
            me.setDifference(CMath.abs(me.maximum()-me.minimum()));



            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
        if((me.factionID().length()>0)&&(CMLib.factions().getFaction(me.factionID())!=null))
        {
            Vector oldV=Resources.getFileLineVector(Resources.getFileResource(me.factionID(),true));
            if(oldV.size()<10)
            {

            }
            boolean[] defined=new boolean[Faction.ALL_TAGS.length];
            for(int i=0;i<defined.length;i++) defined[i]=false;
            for(int v=0;v<oldV.size();v++)
            {
                String s=(String)oldV.elementAt(v);
                if(!(s.trim().startsWith("#")||s.trim().length()==0||(s.indexOf("=")<0)))
                {
                    String tag=s.substring(0,s.indexOf("=")).trim().toUpperCase();
                    int tagRef=CMLib.factions().isFactionTag(tag);
                    if(tagRef>=0) defined[tagRef]=true;
                }
            }
            boolean[] done=new boolean[Faction.ALL_TAGS.length];
            for(int i=0;i<done.length;i++) done[i]=false;
            int lastCommented=-1;
            String CR="\n\r";
            StringBuffer buf=new StringBuffer("");
            for(int v=0;v<oldV.size();v++)
            {
                String s=(String)oldV.elementAt(v);
                if(s.trim().length()==0)
                {
                    if((lastCommented>=0)&&(!done[lastCommented]))
                    {
                        done[lastCommented]=true;
                        buf.append(me.getINIDef(Faction.ALL_TAGS[lastCommented],CR)+CR);
                        lastCommented=-1;
                    }
                }
                else
                if(s.trim().startsWith("#")||(s.indexOf("=")<0))
                {
                    buf.append(s+CR);
                    int x=s.indexOf("=");
                    if(x>=0)
                    {
                        s=s.substring(0,x).trim();
                        int first=s.length()-1;
                        for(;first>=0;first--)
                            if(!Character.isLetterOrDigit(s.charAt(first)))
                                break;
                        first=CMLib.factions().isFactionTag(s.substring(first).trim().toUpperCase());
                        if(first>=0) lastCommented=first;
                    }
                }
                else
                {
                    String tag=s.substring(0,s.indexOf("=")).trim().toUpperCase();
                    int tagRef=CMLib.factions().isFactionTag(tag);
                    if(tagRef<0)
                        buf.append(s+CR);
                    else
                    if(!done[tagRef])
                    {
                        done[tagRef]=true;
                        buf.append(me.getINIDef(tag,CR)+CR);
                    }
                }
            }
            if((lastCommented>=0)&&(!done[lastCommented]))
                buf.append(me.getINIDef(Faction.ALL_TAGS[lastCommented],CR)+CR);
            Resources.removeResource(me.factionID());
            Resources.submitResource(me.factionID(),buf);
            if(!Resources.saveFileResource(me.factionID()))
                mob.tell("Faction File '"+me.factionID()+"' could not be modified.  Make sure it is not READ-ONLY.");
        }
    }
}
