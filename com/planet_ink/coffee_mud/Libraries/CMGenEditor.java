package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MoneyLibrary.MoneyDenomination;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.io.IOException;
import java.util.*;
import java.util.regex.*;

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
public class CMGenEditor extends StdLibrary implements GenericEditor
{
    public String ID(){return "CMGenEditor";}
    
    private final long maxLength=Long.MAX_VALUE;
    // showNumber should always be a valid number no less than 1
    // showFlag should be a valid number for editing, or -1 for skipping


    public void promptStatInt(MOB mob, CMModifiable E, int showNumber, int showFlag, String FieldDisp, String Field)
    throws IOException
    { promptStatInt(mob,E,null,showNumber,showFlag,FieldDisp,Field);}
    public void promptStatInt(MOB mob, CMModifiable E, String help, int showNumber, int showFlag, String FieldDisp, String Field)
    throws IOException
    { E.setStat(Field,""+prompt(mob,CMath.s_long(E.getStat(Field)),showNumber,showFlag,FieldDisp,help)); }
    public void promptStatBool(MOB mob, CMModifiable E, int showNumber, int showFlag, String FieldDisp, String Field)
    throws IOException
    { promptStatBool(mob,E,null,showNumber,showFlag,FieldDisp,Field);}
    public void promptStatBool(MOB mob, CMModifiable E, String help, int showNumber, int showFlag, String FieldDisp, String Field)
    throws IOException
    { E.setStat(Field,""+prompt(mob,CMath.s_bool(E.getStat(Field)),showNumber,showFlag,FieldDisp,help)); }
    public void promptStatStr(MOB mob, CMModifiable E, int showNumber, int showFlag, String FieldDisp, String Field)
    throws IOException
    { promptStatStr(mob,E,null,showNumber,showFlag,FieldDisp,Field,true);}
    public void promptStatStr(MOB mob, CMModifiable E, String help, int showNumber, int showFlag, String FieldDisp, String Field, boolean emptyOK)
    throws IOException
    { E.setStat(Field,prompt(mob,E.getStat(Field),showNumber,showFlag,FieldDisp,emptyOK,false,help,null,null)); }
    public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String FieldDisp)
    throws IOException
    { return prompt(mob,oldVal,showNumber,showFlag,FieldDisp,false,false,null,null,null); }
    public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String FieldDisp, String help)
    throws IOException
    { return prompt(mob,oldVal,showNumber,showFlag,FieldDisp,false,false,help,null,null); }
    public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String FieldDisp, boolean emptyOK)
    throws IOException
    {return prompt(mob,oldVal,showNumber,showFlag,FieldDisp,emptyOK,false,null,null,null); }
    public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String FieldDisp, boolean emptyOK, String help)
    throws IOException
    { return prompt(mob,oldVal,showNumber,showFlag,FieldDisp,emptyOK,false,help);}
    public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String FieldDisp, boolean emptyOK, boolean rawPrint)
    throws IOException
    { return prompt(mob,oldVal,showNumber,showFlag,FieldDisp,emptyOK,rawPrint,null,null,null);}
    public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String FieldDisp, boolean emptyOK, boolean rawPrint, String help)
    throws IOException
    { return prompt(mob,oldVal,showNumber,showFlag,FieldDisp,emptyOK,rawPrint,help,null,null);}
    public boolean prompt(MOB mob, boolean oldVal, int showNumber, int showFlag, String FieldDisp)
    throws IOException
    { return prompt(mob,oldVal,showNumber,showFlag,FieldDisp,null); }
    public double prompt(MOB mob, double oldVal, int showNumber, int showFlag, String FieldDisp)
    throws IOException
    { return prompt(mob,oldVal,showNumber,showFlag,FieldDisp,null); }
    public int prompt(MOB mob, int oldVal, int showNumber, int showFlag, String FieldDisp)
    throws IOException
    { return prompt(mob,oldVal,showNumber,showFlag,FieldDisp,null);}
    public long prompt(MOB mob, long oldVal, int showNumber, int showFlag, String FieldDisp)
    throws IOException
    { return prompt(mob,oldVal,showNumber,showFlag,FieldDisp,null);}



    public boolean promptToggle(MOB mob, int showNumber, int showFlag, String FieldDisp)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return false;
        mob.tell(showNumber+". "+FieldDisp);
        if((showFlag!=showNumber)&&(showFlag>-999)) return false;
        if(showFlag!=showNumber)
            return mob.session().confirm("Toggle (y/N)?","N");
        return true;
    }

    public String prompt(MOB mob,
                         String oldVal,
                         int showNumber,
                         int showFlag,
                         String FieldDisp,
                         boolean emptyOK,
                         boolean rawPrint,
                         String help,
                         CMEval eval,
                         Object[] choices)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return oldVal;
        if(rawPrint)
            mob.session().rawPrintln(showNumber+". "+FieldDisp+": '"+oldVal+"'.");
        else
            mob.tell(showNumber+". "+FieldDisp+": '"+oldVal+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return oldVal;
        String newName="?";
        while(newName.equals("?")&&(mob.session()!=null)&&(!mob.session().killFlag()))
        {
            newName=mob.session().prompt("Enter a new value "+(emptyOK?"(or NULL)":"")+(help!=null?" (?)":"")+"\n\r:","");
            if(newName.equals("?")&&(help!=null))
                mob.tell(help);
            else
            {
                boolean noEntry=(newName.trim().length()==0);
                if(noEntry)
                    newName=oldVal;
                else
                if((newName.equalsIgnoreCase("null"))&&(emptyOK))
                    newName="";

                if(eval!=null)
                try
                {
                    Object value=eval.eval(newName,choices,emptyOK);
                    if(value instanceof String)
                        newName=(String)value;
                }
                catch(CMException e)
                {
                    mob.tell(e.getMessage());
                    newName="?";
                    continue;
                }
                if((noEntry)&&(newName.equals(oldVal)))
                    break;
                return newName;
            }
        }
        mob.tell("(no change)");
        return oldVal;
    }

    public boolean prompt(MOB mob, boolean oldVal, int showNumber, int showFlag, String FieldDisp, String help)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return oldVal;
        mob.tell(showNumber+". "+FieldDisp+": '"+oldVal+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return oldVal;
        String newName="?";
        while(newName.equals("?")&&(mob.session()!=null)&&(!mob.session().killFlag()))
        {
            newName=mob.session().prompt("Enter true or false"+(help!=null?" (?)":"")+":","");
            if(newName.equals("?")&&(help!=null))
                mob.tell(help);
            else
            if(newName.toUpperCase().startsWith("T")||newName.toUpperCase().startsWith("F"))
                return newName.toUpperCase().startsWith("T");
            else
            if(newName.toUpperCase().startsWith("Y")||newName.toUpperCase().startsWith("N"))
                return newName.toUpperCase().startsWith("Y");
            else
                break;
        }
        mob.tell("(no change)");
        return oldVal;
    }

    public double prompt(MOB mob, double oldVal, int showNumber, int showFlag, String FieldDisp, String help)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return oldVal;
        mob.tell(showNumber+". "+FieldDisp+": '"+oldVal+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return oldVal;
        String newName="?";
        while(newName.equals("?")&&(mob.session()!=null)&&(!mob.session().killFlag()))
        {
            newName=mob.session().prompt("Enter a new value"+(help!=null?" (?)":"")+":","");
            if(newName.equals("?")&&(help!=null))
                mob.tell(help);
            else
            if(CMath.isNumber(newName))
                return CMath.s_double(newName);
            else
                break;
        }
        mob.tell("(no change)");
        return oldVal;
    }

    public int prompt(MOB mob, int oldVal, int showNumber, int showFlag, String FieldDisp, String help)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return oldVal;
        mob.tell(showNumber+". "+FieldDisp+": '"+oldVal+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return oldVal;
        String newName="?";
        while(newName.equals("?")&&(mob.session()!=null)&&(!mob.session().killFlag()))
        {
            newName=mob.session().prompt("Enter a new value"+(help!=null?" (?)":"")+":","");
            if(newName.equals("?")&&(help!=null))
                mob.tell(help);
            else
            if(CMath.isInteger(newName))
                return CMath.s_int(newName);
            else
                break;
        }
        mob.tell("(no change)");
        return oldVal;
    }

    public long prompt(MOB mob, long oldVal, int showNumber, int showFlag, String FieldDisp, String help)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return oldVal;
        mob.tell(showNumber+". "+FieldDisp+": '"+oldVal+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return oldVal;
        String newName="?";
        while(newName.equals("?")&&(mob.session()!=null)&&(!mob.session().killFlag()))
        {
            newName=mob.session().prompt("Enter a new value"+(help!=null?" (?)":"")+":","");
            if(newName.equals("?")&&(help!=null))
                mob.tell(help);
            else
            if(CMath.isInteger(newName))
                return CMath.s_long(newName);
            else
                break;
        }
        mob.tell("(no change)");
        return oldVal;
    }
    
    public int promptMulti(MOB mob, int oldVal, int showNumber, int showFlag, String FieldDisp, DVector choices) 
    throws IOException
    {
        return CMath.s_int(promptMultiOrExtra(mob,""+oldVal,showNumber,showFlag,FieldDisp,choices));
    }
    
    public String promptMultiOrExtra(MOB mob, String oldVal, int showNumber, int showFlag, String FieldDisp, DVector choices) 
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return oldVal;
        Vector oldVals = new Vector();
        if(CMath.s_int(oldVal) > 0) {
            for(int c=0;c<choices.size();c++)
                if(CMath.bset(CMath.s_int(oldVal),((Integer)choices.elementAt(c,1)).intValue()))
                    oldVals.addElement((String)choices.elementAt(c,2));
        }
        else
        if(choices.contains(oldVal.toUpperCase().trim()))
            oldVals.addElement(oldVal);
        mob.tell(showNumber+". "+FieldDisp+": '"+CMParms.toStringList(oldVals)+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return oldVal;
        String newVal=oldVal;
        String thisVal="?";
        while(thisVal.equals("?")&&(mob.session()!=null)&&(!mob.session().killFlag()))
        {
            thisVal=mob.session().prompt("Enter a new choice to add/remove (?):","").trim();
            if(thisVal.equals("?"))
                mob.tell(CMParms.toStringList(choices.getDimensionVector(2)));
            else
            if(thisVal.length()==0)
                newVal = oldVal;
            else
            if(thisVal.equalsIgnoreCase("NULL")) {
                if(choices.contains(""))
                    newVal = "";
                else
                    newVal = "0";
                oldVals.clear();
                break;
            }
            else
            {
                String foundChoice = null;
                String foundVal = "";
                for(int c=0;c<choices.size();c++)
                    if(((String)choices.elementAt(c,2)).equalsIgnoreCase(thisVal))
                    {
                        foundChoice = (String)choices.elementAt(c,2);
                        foundVal = choices.elementAt(c,1).toString();
                    }
                if(foundChoice == null)
                {
                    mob.tell("'"+newVal+"' is not an available option.  Use ? for a list.");
                    thisVal = "?";
                }
                else
                if(!CMath.isInteger(foundVal))
                {
                    oldVals.clear();
                    newVal = foundVal;
                    oldVals.addElement(foundVal);
                }
                else
                if(foundVal == "0")
                {
                    newVal = "0";
                    oldVals.clear();
                }
                else
                {
                    if(oldVals.contains(foundChoice))
                    {
                        newVal = Integer.toString(CMath.s_int(newVal) - CMath.s_int(foundVal));
                        oldVals.remove(foundChoice);
                        mob.tell("'"+foundChoice+"' removed.");
                        thisVal = "?";
                    } else {
                        oldVals.add(foundChoice);
                        mob.tell("'"+foundChoice+"' added.");
                        thisVal = "?";
                        newVal = Integer.toString(CMath.s_int(newVal) | CMath.s_int(foundVal));
                    }
                }
            }
        }
        if(oldVal.equals(newVal))
            mob.tell("(no change)");
        return newVal;
    }
    
    public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String FieldDisp, DVector choices)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return oldVal;
        String oldShowVal = oldVal;
        for(int c=0;c<choices.size();c++)
            if(((String)choices.elementAt(c,1)).equalsIgnoreCase(oldVal))
                oldShowVal = (String)choices.elementAt(c,2);
        mob.tell(showNumber+". "+FieldDisp+": '"+oldShowVal+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return oldVal;
        String newVal="?";
        while(newVal.equals("?")&&(mob.session()!=null)&&(!mob.session().killFlag()))
        {
            newVal=mob.session().prompt("Enter a new choice (? or NULL):","").trim();
            if(newVal.equals("?"))
                mob.tell(CMParms.toStringList(choices.getDimensionVector(2)));
            else
            if(newVal.length()==0)
                newVal = oldVal;
            else
            {
                if(newVal.equalsIgnoreCase("NULL"))
                    newVal = "";
                String foundChoice = null;
                for(int c=0;c<choices.size();c++)
                    if(((String)choices.elementAt(c,2)).equalsIgnoreCase(newVal))
                        foundChoice = (String)choices.elementAt(c,1);
                if(foundChoice == null)
                    mob.tell("'"+newVal+"' is not an available choice.  Use ? for a list.");
                else
                {
                    newVal = foundChoice;
                    break;
                }
            }
        }
        if(oldVal.equals(newVal))
            mob.tell("(no change)");
        return newVal;
    }
    
    public void genName(MOB mob, Environmental E, int showNumber, int showFlag) throws IOException
    {
        String newName=prompt(mob,E.Name(),showNumber,showFlag,"Name",false,false);
        if(newName.equals(E.Name())) return;
        if((mob.session()==null)
        ||((!(E instanceof MOB))&&(!(E instanceof Item)))
        ||(!CMLib.flags().isCataloged(E)))
        {
            E.setName(newName);
            return;
        }
        Environmental cataE=CMLib.catalog().getCatalogObj(E);
        if(cataE==null) {
            E.setName(newName);
            CMLib.catalog().changeCatalogUsage(E,false);
            return;
        } else
        if(mob.session().confirm("This object is cataloged.  Changing its name will detach it from the cataloged version, are you sure (y/N)?","N"))
        {
            CMLib.catalog().changeCatalogUsage(E,false);
            E.setName(newName);
        }
    }

    protected void catalogCheckUpdate(MOB mob, Environmental E)
        throws IOException
    {
        if((!CMLib.flags().isCataloged(E))
        ||((!(E instanceof MOB))&&(!(E instanceof Item)))
        ||(mob.session()==null))
        {
        	if(E instanceof MOB)
                E.setMiscText(E.text());
            return;
        }

        StringBuffer diffs=CMLib.catalog().checkCatalogIntegrity(E);
        if((diffs!=null)&&(diffs.length()>0))
        {
        	Environmental origCataE = CMLib.catalog().getCatalogObj(E);
        	Environmental cataE=(Environmental)origCataE.copyOf();
        	CMLib.catalog().changeCatalogUsage(cataE,true);
        	StringBuffer detailedDiff=new StringBuffer("");
        	Vector V=CMParms.parseCommas(diffs.toString(),true);
        	
        	for(int v=0;v<V.size();v++)
        	{
        		String stat=(String)V.elementAt(v);
        		detailedDiff.append("CATALOG:"+stat+":'"+cataE.getStat(stat)+"'\n\r");
        		detailedDiff.append("YOURS  :"+stat+":'"+E.getStat(stat)+"'\n\r");
        	}
        	cataE.destroy();
        	mob.tell("You have modified the following fields: \n\r"+detailedDiff.toString());
        	String message = "This object is cataloged.  Enter U to update the cataloged version, or D to detach this object from the catalog, or C to Cancel (u/d/C)?";
        	String choice = mob.session().choose(message, "UDC", "C");
        	if(choice.equalsIgnoreCase("C"))
        	{
        		E.setMiscText(origCataE.text());
        		E.recoverEnvStats();
        		if(E instanceof MOB)
        		{
        			((MOB)E).recoverCharStats();
        			((MOB)E).recoverMaxState();
        		}
        		CMLib.catalog().changeCatalogUsage(E, true);
        	}
        	else
        	if(choice.equalsIgnoreCase("U"))
	        {
	        	CMLib.catalog().updateCatalog(E);
	            mob.tell("Catalog update complete.");
	            Log.infoOut("BaseGenerics",mob.Name()+" updated catalog "+((E instanceof MOB)?"MOB":"ITEM")+" "+E.Name());
	            E.setMiscText(E.text());
	        }
	        else
        	if(choice.equalsIgnoreCase("D"))
        	{
	            CMLib.catalog().changeCatalogUsage(E,false);
	            E.setMiscText(E.text());
        	}
        	else
        		mob.tell("That wasn't a choice?!");
        }
    }

    protected void genImage(MOB mob, Environmental E, int showNumber, int showFlag) throws IOException
    {   E.setImage(prompt(mob,E.rawImage(),showNumber,showFlag,"MXP Image filename",true,false,"This is the path/filename of your MXP image file for this object."));}

    protected void genCorpseData(MOB mob, DeadBody E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Corpse Data: '"+E.mobName()+"/"+E.killerName()+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        mob.tell("Dead MOB name: '"+E.mobName()+"'.");
        String newName=mob.session().prompt("Enter a new name\n\r:","");
        if(newName.length()>0) E.setMobName(newName);
        else mob.tell("(no change)");
        mob.tell("Dead MOB Description: '"+E.mobDescription()+"'.");
        newName=mob.session().prompt("Enter a new description\n\r:","");
        if(newName.length()>0) E.setMobDescription(newName);
        else mob.tell("(no change)");
        mob.tell("Is a Players corpse: "+E.playerCorpse());
        newName=mob.session().prompt("Enter a new true/false\n\r:","");
        if((newName.length()>0)&&(newName.equalsIgnoreCase("true")||newName.equalsIgnoreCase("false")))
            E.setPlayerCorpse(Boolean.valueOf(newName.toLowerCase()).booleanValue());
        else mob.tell("(no change)");
        mob.tell("Dead mobs PK flag: "+E.mobPKFlag());
        newName=mob.session().prompt("Enter a new true/false\n\r:","");
        if((newName.length()>0)&&(newName.equalsIgnoreCase("true")||newName.equalsIgnoreCase("false")))
            E.setMobPKFlag(Boolean.valueOf(newName.toLowerCase()).booleanValue());
        else mob.tell("(no change)");
        genCharStats(mob,E.charStats());
        mob.tell("Killers Name: '"+E.killerName()+"'.");
        newName=mob.session().prompt("Enter a new killer\n\r:","");
        if(newName.length()>0) E.setKillerName(newName);
        else mob.tell("(no change)");
        mob.tell("Killer is a player: "+E.killerPlayer());
        newName=mob.session().prompt("Enter a new true/false\n\r:","");
        if((newName.length()>0)&&(newName.equalsIgnoreCase("true")||newName.equalsIgnoreCase("false")))
            E.setKillerPlayer(Boolean.valueOf(newName.toLowerCase()).booleanValue());
        else mob.tell("(no change)");
        mob.tell("Time of death: "+CMLib.time().date2String(E.timeOfDeath()));
        newName=mob.session().prompt("Enter a new value\n\r:","");
        if(newName.length()>0) E.setTimeOfDeath(CMLib.time().string2Millis(newName));
        else mob.tell("(no change)");
        mob.tell("Last message string: "+E.lastMessage());
        newName=mob.session().prompt("Enter a new value\n\r:","");
        if(newName.length()>0) E.setLastMessage(newName);
        else mob.tell("(no change)");
    }

    protected void genAuthor(MOB mob, Area A, int showNumber, int showFlag) throws IOException
    {   A.setAuthorID(prompt(mob,A.getAuthorID(),showNumber,showFlag,"Author",true,false,"Area Author's Name"));}

    protected void genPanelType(MOB mob, ShipComponent.ShipPanel S, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String componentType=CMStrings.capitalizeAndLower(ShipComponent.ShipPanel.COMPONENT_PANEL_DESC[S.panelType()].toLowerCase());
        mob.tell(showNumber+". Panel Type: '"+componentType+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        boolean continueThis=true;
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(continueThis))
        {
            continueThis=false;
            String newName=mob.session().prompt("Enter a new one (?)\n\r:","");
            if(newName.length()>0)
            {
                if(newName.equalsIgnoreCase("?"))
                {
                    mob.tell("Component Types: "+CMParms.toStringList(ShipComponent.ShipPanel.COMPONENT_PANEL_DESC));
                    continueThis=true;
                }
                else
                {
                    int newType=-1;
                    for(int i=0;i<ShipComponent.ShipPanel.COMPONENT_PANEL_DESC.length;i++)
                        if(ShipComponent.ShipPanel.COMPONENT_PANEL_DESC[i].equalsIgnoreCase(newName))
                            newType=i;
                    if(newType<0)
                    {
                        mob.tell("'"+newName+"' is not recognized.  Try '?' for a list.");
                        continueThis=true;
                    }
                    else
                        S.setPanelType(newType);
                }
            }
            else
                mob.tell("(no change)");
        }
    }

    protected void genCurrency(MOB mob, Area A, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String currencyName=A.getCurrency().length()==0?"Default":A.getCurrency();
        mob.tell(showNumber+". Currency: '"+currencyName+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newName=mob.session().prompt("Enter a new one or 'DEFAULT'\n\r:","");
        if(newName.length()>0)
        {
            if(newName.equalsIgnoreCase("default"))
                A.setCurrency("");
            else
            if((newName.indexOf("=")<0)&&(!CMLib.beanCounter().getAllCurrencies().contains(newName.trim().toUpperCase())))
            {
                Vector V=CMLib.beanCounter().getAllCurrencies();
                mob.tell("'"+newName.trim().toUpperCase()+"' is not a known currency. Existing currencies include: DEFAULT"+CMParms.toStringList(V));
            }
            else
            if(newName.indexOf("=")>=0)
                A.setCurrency(newName.trim());
            else
                A.setCurrency(newName.toUpperCase().trim());
        }
        else
            mob.tell("(no change)");
    }

    protected void genTimeClock(MOB mob, Area A, int showNumber, int showFlag)
    throws IOException
    {

        if((showFlag>0)&&(showFlag!=showNumber)) return;
        TimeClock TC=A.getTimeObj();
        StringBuffer report=new StringBuffer("");
        if(TC==CMLib.time().globalClock())
            report.append("Default -- Can't be changed.");
        else
        {
            report.append(TC.getHoursInDay()+" hrs-day/");
            report.append(TC.getDaysInMonth()+" days-mn/");
            report.append(TC.getMonthsInYear()+" mnths-yr");
        }
        mob.tell(showNumber+". Calendar: '"+report.toString()+"'.");
        if(TC==CMLib.time().globalClock()) return;
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newName="";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(newName.length()==0))
        {
            report=new StringBuffer("\n\rCalendar/Clock settings:\n\r");
            report.append("1. "+TC.getHoursInDay()+" hours per day\n\r");
            report.append("2. Dawn Hour: "+TC.getDawnToDusk()[TimeClock.TIME_DAWN]+"\n\r");
            report.append("3. Day Hour: "+TC.getDawnToDusk()[TimeClock.TIME_DAY]+"\n\r");
            report.append("4. Dusk Hour: "+TC.getDawnToDusk()[TimeClock.TIME_DUSK]+"\n\r");
            report.append("5. Night Hour: "+TC.getDawnToDusk()[TimeClock.TIME_NIGHT]+"\n\r");
            report.append("6. Weekdays: "+CMParms.toStringList(TC.getWeekNames())+"\n\r");
            report.append("7. Months: "+CMParms.toStringList(TC.getMonthNames())+"\n\r");
            report.append("8. Year Title(s): "+CMParms.toStringList(TC.getYearNames()));
            mob.tell(report.toString());
            newName=mob.session().prompt("Enter one to change:","");
            if(newName.length()==0) break;
            int which=CMath.s_int(newName);

            if((which<0)||(which>8))
                mob.tell("Invalid: "+which+"");
            else
            if(which<=5)
            {
                newName="";
                String newNum=mob.session().prompt("Enter a new number:","");
                int val=CMath.s_int(newNum);
                if(newNum.length()==0)
                    mob.tell("(no change)");
                else
                switch(which)
                {
                case 1:
                    TC.setHoursInDay(val);
                    break;
                case 2:
                    TC.getDawnToDusk()[TimeClock.TIME_DAWN]=val;
                    break;
                case 3:
                    if((val>=0)&&(TC.getDawnToDusk()[TimeClock.TIME_DAWN]>=val))
                        mob.tell("That value is before the dawn!");
                    else
                        TC.getDawnToDusk()[TimeClock.TIME_DAY]=val;
                    break;
                case 4:
                    if((val>=0)&&(TC.getDawnToDusk()[TimeClock.TIME_DAWN]>=val))
                        mob.tell("That value is before the dawn!");
                    else
                    if((val>=0)&&(TC.getDawnToDusk()[TimeClock.TIME_DAY]>=val))
                        mob.tell("That value is before the day!");
                    else
                        TC.getDawnToDusk()[TimeClock.TIME_DUSK]=val;
                    break;
                case 5:
                    if((val>=0)&&(TC.getDawnToDusk()[TimeClock.TIME_DAWN]>=val))
                        mob.tell("That value is before the dawn!");
                    else
                    if((val>=0)&&(TC.getDawnToDusk()[TimeClock.TIME_DAY]>=val))
                        mob.tell("That value is before the day!");
                    else
                    if((val>=0)&&(TC.getDawnToDusk()[TimeClock.TIME_DUSK]>=val))
                        mob.tell("That value is before the dusk!");
                    else
                        TC.getDawnToDusk()[TimeClock.TIME_NIGHT]=val;
                    break;
                }
            }
            else
            {
                newName="";
                String newNum=mob.session().prompt("Enter a new list (comma delimited)\n\r:","");
                if(newNum.length()==0)
                    mob.tell("(no change)");
                else
                switch(which)
                {
                case 6:
                    TC.setDaysInWeek(CMParms.toStringArray(CMParms.parseCommas(newNum,true)));
                    break;
                case 7:
                    TC.setMonthsInYear(CMParms.toStringArray(CMParms.parseCommas(newNum,true)));
                    break;
                case 8:
                    TC.setYearNames(CMParms.toStringArray(CMParms.parseCommas(newNum,true)));
                    break;
                }
            }
        }
        TC.save();
    }

    protected void genClan(MOB mob, MOB E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag<=0)||(showFlag==showNumber))
        {
            mob.tell(showNumber+". Clan (ID): '"+E.getClanID()+"'.");
            if((showFlag==showNumber)||(showFlag<=-999))
            {
                String newName=mob.session().prompt("Enter a new one (null)\n\r:","");
                if(newName.equalsIgnoreCase("null"))
                    E.setClanID("");
                else
                if(newName.length()>0)
                {
                    E.setClanID(newName);
                    E.setClanRole(Clan.POS_MEMBER);
                }
                else
                    mob.tell("(no change)");
            }
        }
        if(((showFlag<=0)||(showFlag==showNumber))
           &&(!E.isMonster())
           &&(E.getClanID().length()>0)
           &&(CMLib.clans().getClan(E.getClanID())!=null))
        {

            Clan C=CMLib.clans().getClan(E.getClanID());
            mob.tell(showNumber+". Clan (Role): '"+CMLib.clans().getRoleName(C.getGovernment(),E.getClanRole(),true,false)+"'.");
            if((showFlag==showNumber)||(showFlag<=-999))
            {
                String newName=mob.session().prompt("Enter a new one\n\r:","");
                if(newName.length()>0)
                {
                    int newRole=-1;
                    for(int i=0;i<Clan.ROL_DESCS[C.getGovernment()].length;i++)
                        if(newName.equalsIgnoreCase(Clan.ROL_DESCS[C.getGovernment()][i]))
                        {
                            newRole=(int)CMath.pow(2,i-1);
                            break;
                        }
                    if(newRole<0)
                        mob.tell("That role is invalid. Try: "+CMParms.toStringList(Clan.ROL_DESCS[C.getGovernment()]));
                    else
                        E.setClanRole(newRole);
                }
                else
                    mob.tell("(no change)");
            }
        }
    }

    protected void genDeity(MOB mob, MOB E, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag<=0)||(showFlag==showNumber))
        {
            mob.tell(showNumber+". Deity (ID): '"+E.getWorshipCharID()+"'.");
            if((showFlag==showNumber)||(showFlag<=-999))
            {
                String newName=mob.session().prompt("Enter a new one (null)\n\r:","");
                if(newName.equalsIgnoreCase("null"))
                    E.setWorshipCharID("");
                else
                if(newName.length()>0)
                {
                    if(CMLib.map().getDeity(newName)==null)
                        mob.tell("That deity does not exist.");
                    else
                        E.setWorshipCharID(CMLib.map().getDeity(newName).Name());
                }
                else
                    mob.tell("(no change)");
            }
        }
    }

    protected void genArchivePath(MOB mob, Area E, int showNumber, int showFlag) throws IOException
    {   E.setArchivePath(prompt(mob,E.getArchivePath(),showNumber,showFlag,"Archive Path",true,false,"Path/filename for EXPORT AREA command.  Enter NULL for default."));}

    public Room changeRoomType(Room R, Room newRoom)
    {
        if((R==null)||(newRoom==null)) return R;
        synchronized(("SYNC"+R.roomID()).intern())
        {
            R=CMLib.map().getRoom(R);
            Room oldR=R;
            R=newRoom;
            Vector oldBehavsNEffects=new Vector();
            for(int a=oldR.numEffects()-1;a>=0;a--)
            {
                Ability A=oldR.fetchEffect(a);
                if(A!=null)
                {
                    if(!A.canBeUninvoked())
                    {
                        oldBehavsNEffects.addElement(A);
                        oldR.delEffect(A);
                    }
                    else
                        A.unInvoke();
                }
            }
            for(int b=0;b<oldR.numBehaviors();b++)
            {
                Behavior B=oldR.fetchBehavior(b);
                if(B!=null)
                    oldBehavsNEffects.addElement(B);
            }
            CMLib.threads().deleteTick(oldR,-1);
            R.setRoomID(oldR.roomID());
            Area A=oldR.getArea();
            if(A!=null) A.delProperRoom(oldR);
            R.setArea(A);
            for(int d=0;d<R.rawDoors().length;d++)
                R.rawDoors()[d]=oldR.rawDoors()[d];
            for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
                R.setRawExit(d,oldR);
            R.setDisplayText(oldR.displayText());
            R.setDescription(oldR.description());
            if(R.image().equalsIgnoreCase(CMProps.getDefaultMXPImage(oldR))) R.setImage(null);
            if((R instanceof GridLocale)&&(oldR instanceof GridLocale))
            {
                ((GridLocale)R).setXGridSize(((GridLocale)oldR).xGridSize());
                ((GridLocale)R).setYGridSize(((GridLocale)oldR).yGridSize());
                ((GridLocale)R).clearGrid(null);
            }
            Vector allmobs=new Vector();
            int skip=0;
            while(oldR.numInhabitants()>(skip))
            {
                MOB M=oldR.fetchInhabitant(skip);
                if(M.savable())
                {
                    if(!allmobs.contains(M))
                        allmobs.addElement(M);
                    oldR.delInhabitant(M);
                }
                else
                if(oldR!=R)
                {
                    oldR.delInhabitant(M);
                    R.bringMobHere(M,true);
                }
                else
                    skip++;
            }
            Vector allitems=new Vector();
            while(oldR.numItems()>0)
            {
                Item I=oldR.fetchItem(0);
                if(!allitems.contains(I))
                    allitems.addElement(I);
                oldR.delItem(I);
            }

            for(int i=0;i<allitems.size();i++)
            {
                Item I=(Item)allitems.elementAt(i);
                if(!R.isContent(I))
                {
                    if(I.subjectToWearAndTear())
                        I.setUsesRemaining(100);
                    I.recoverEnvStats();
                    R.addItem(I);
                    R.recoverRoomStats();
                }
            }
            for(int m=0;m<allmobs.size();m++)
            {
                MOB M=(MOB)allmobs.elementAt(m);
                if(!R.isInhabitant(M))
                {
                    MOB M2=(MOB)M.copyOf();
                    M2.setStartRoom(R);
                    M2.setLocation(R);
                    long rejuv=Tickable.TICKS_PER_RLMIN+Tickable.TICKS_PER_RLMIN+(Tickable.TICKS_PER_RLMIN/2);
                    if(rejuv>(Tickable.TICKS_PER_RLMIN*20)) rejuv=(Tickable.TICKS_PER_RLMIN*20);
                    M2.envStats().setRejuv((int)rejuv);
                    M2.recoverCharStats();
                    M2.recoverEnvStats();
                    M2.recoverMaxState();
                    M2.resetToMaxState();
                    M2.bringToLife(R,true);
                    R.recoverRoomStats();
                    M.destroy();
                }
            }

            try
            {
                for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
                {
                    Room R2=(Room)r.nextElement();
                    for(int d=0;d<R2.rawDoors().length;d++)
                        if(R2.rawDoors()[d]==oldR)
                        {
                            R2.rawDoors()[d]=R;
                            if(R2 instanceof GridLocale)
                                ((GridLocale)R2).buildGrid();
                        }
                }
            }catch(NoSuchElementException e){}
            try
            {
                for(Enumeration e=CMLib.players().players();e.hasMoreElements();)
                {
                    MOB M=(MOB)e.nextElement();
                    if(M.getStartRoom()==oldR)
                        M.setStartRoom(R);
                    else
                    if(M.location()==oldR)
                        M.setLocation(R);
                }
            }catch(NoSuchElementException e){}
            R.getArea().fillInAreaRoom(R);
            for(int i=0;i<oldBehavsNEffects.size();i++)
            {
                if(oldBehavsNEffects.elementAt(i) instanceof Behavior)
                    R.addBehavior((Behavior)oldBehavsNEffects.elementAt(i));
                else
                    R.addNonUninvokableEffect((Ability)oldBehavsNEffects.elementAt(i));
            }
            CMLib.database().DBUpdateRoom(R);
            CMLib.database().DBUpdateMOBs(R);
            CMLib.database().DBUpdateItems(R);
            oldR.destroy();
            R.getArea().addProperRoom(R); // necessary because of the destroy
            R.setImage(R.rawImage());
            R.startItemRejuv();
        }
        return R;
    }

    protected Room genRoomType(MOB mob, Room R, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return R;
        mob.tell(showNumber+". Type: '"+CMClass.classID(R)+"'");
        if((showFlag!=showNumber)&&(showFlag>-999)) return R;
        String newName="";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(newName.length()==0))
        {
            newName=mob.session().prompt("Enter a new one (?)\n\r:","");
            if(newName.trim().equals("?"))
            {
                mob.tell(CMLib.lister().reallyList2Cols(CMClass.locales(),-1,null).toString()+"\n\r");
                newName="";
            }
            else
            if(newName.length()>0)
            {
                Room newRoom=CMClass.getLocale(newName);
                if(newRoom==null)
                    mob.tell("'"+newName+"' does not exist. No Change.");
                else
                if(mob.session().confirm("This will change the room type of room "+R.roomID()+". It will automatically save any mobs and items in this room permanently.  Are you absolutely sure (y/N)?","N"))
                    R=changeRoomType(R,newRoom);
                R.recoverRoomStats();
            }
            else
            {
                mob.tell("(no change)");
                break;
            }
        }
        return R;
    }

    public void genDescription(MOB mob, Environmental E, int showNumber, int showFlag) throws IOException
    {   E.setDescription(prompt(mob,E.description(),showNumber,showFlag,"Description",true,true,null));}

    protected void genNotes(MOB mob, MOB E, int showNumber, int showFlag) throws IOException
    {
        if(E.playerStats()!=null)
        E.playerStats().setNotes(prompt(mob,E.playerStats().notes(),showNumber,showFlag,"Private notes",true,false,null));
    }

    protected void genPassword(MOB mob, MOB E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Password: ********.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String str=mob.session().prompt("Enter a new one to reset\n\r:","");
        if((str.length()>0)&&(E.playerStats()!=null))
        {
            E.playerStats().setPassword(str);
            CMLib.database().DBUpdatePassword(E.Name(),str);
        }
        else
            mob.tell("(no change)");
    }

    protected void genEmail(MOB mob, AccountStats A, int showNumber, int showFlag) throws IOException
    {
    	if(A==null) return;
        A.setEmail(prompt(mob,A.getEmail(),showNumber,showFlag,"Email",true,false,null));
    }

    public void genDisplayText(MOB mob, Environmental E, int showNumber, int showFlag) throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        if(mob.session()!=null)
        	mob.session().rawPrintln(showNumber+". Display: '"+E.displayText()+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newName=null;
        if(E instanceof Item)
            newName=mob.session().prompt("Enter something new (null == blended)\n\r:","");
        else
        if(E instanceof Exit)
            newName=mob.session().prompt("Enter something new (null == see-through)\n\r:","");
        else
            newName=mob.session().prompt("Enter something new (null = empty)\n\r:","");
        if(newName.length()>0)
        {
            if(newName.trim().equalsIgnoreCase("null"))
                newName="";
            E.setDisplayText(newName);
        }
        else
            mob.tell("(no change)");
        if((E instanceof Item)&&(E.displayText().length()==0))
            mob.tell("(blended)");
    }

    protected void genClosedText(MOB mob, Exit E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        if(E instanceof Item)
            mob.tell(showNumber+". Exit Closed Text: '"+E.closedText()+"'.");
        else
            mob.tell(showNumber+". Closed Text: '"+E.closedText()+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newName=mob.session().prompt("Enter something new (null=blank)\n\r:","");
        if(newName.equals("null"))
            E.setExitParams(E.doorName(),E.closeWord(),E.openWord(),"");
        else
        if(newName.length()>0)
            E.setExitParams(E.doorName(),E.closeWord(),E.openWord(),newName);
        else
            mob.tell("(no change)");
    }
    protected void genDoorName(MOB mob, Exit E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        if(E instanceof Item)
            mob.tell(showNumber+". Exit Direction: '"+E.doorName()+"'.");
        else
            mob.tell(showNumber+". Door Name: '"+E.doorName()+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newName=mob.session().prompt("Enter something new\n\r:","");
        if(newName.length()>0)
            E.setExitParams(newName,E.closeWord(),E.openWord(),E.closedText());
        else
            mob.tell("(no change)");
    }

    protected void genBurnout(MOB mob, Light E, int showNumber, int showFlag)
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Is destroyed after burnout: '"+E.destroyedWhenBurnedOut()+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        E.setDestroyedWhenBurntOut(!E.destroyedWhenBurnedOut());
    }

    protected void genOpenWord(MOB mob, Exit E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Open Word: '"+E.openWord()+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newName=mob.session().prompt("Enter something new\n\r:","");
        if(newName.length()>0)
            E.setExitParams(E.doorName(),E.closeWord(),newName,E.closedText());
        else
            mob.tell("(no change)");
    }

    protected void genSubOps(MOB mob, Area A, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String str="Q";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(str.length()>0))
        {
            mob.tell(showNumber+". Area staff names: "+A.getSubOpList());
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            str=mob.session().prompt("Enter a name to add or remove\n\r:","");
            if(str.length()>0)
            {
                if(A.amISubOp(str))
                {
                    A.delSubOp(str);
                    mob.tell("Staff removed.");
                }
                else
                if(CMLib.players().playerExists(str))
                {
                    A.addSubOp(str);
                    mob.tell("Staff added.");
                }
                else
                    mob.tell("'"+str+"' is not recognized as a valid user name.");
            }
        }
    }

    protected void genParentAreas(MOB mob, Area A, int showNumber, int showFlag)
            throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String newArea="Q";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(newArea.length()>0))
        {
            mob.tell(showNumber+". Parent Areas: "+A.getParentsList());
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            newArea=mob.session().prompt("Enter an area name to add or remove\n\r:","");
            if(newArea.length()>0)
            {
                Area lookedUp=CMLib.map().getArea(newArea);
                if(lookedUp!=null)
                {
                    if (lookedUp.isChild(A))
                    {
                        // this new area is already a parent to A,
                        // they must want it removed
                        A.removeParent(lookedUp);
                        lookedUp.removeChild(A);
                        mob.tell("Enter an area name to add or remove\n\r:");
                    }
                    else
                    {
                        if(A.canParent(lookedUp))
                        {
                            A.addParent(lookedUp);
                            lookedUp.addChild(A);
                            mob.tell("Area '"+lookedUp.Name()+"' added.");
                        }
                        else
                        {
                            mob.tell("Area '"+lookedUp.Name()+"" +"' cannot be added because this would create a circular reference.");
                        }
                    }
                }
                else
                    mob.tell("'"+newArea+"' is not recognized as a valid area name.");
            }
        }
    }

    protected void genChildAreas(MOB mob, Area A, int showNumber, int showFlag)
            throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String newArea="Q";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(newArea.length()>0))
        {
            mob.tell(showNumber+". Area Children: "+A.getChildrenList());
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            newArea=mob.session().prompt("Enter an area name to add or remove\n\r:","");
            if(newArea.length()>0)
            {
                Area lookedUp=CMLib.map().getArea(newArea);
                if(lookedUp!=null)
                {
                    if (lookedUp.isParent(A))
                    {
                        // this area is already a child to A, they must want it removed
                        A.removeChild(lookedUp);
                        lookedUp.removeParent(A);
                        mob.tell("Enter an area name to add or remove\n\r:");
                    }
                    else
                    {
                        if(A.canChild(lookedUp))
                        {
                            A.addChild(lookedUp);
                            lookedUp.addParent(A);
                            mob.tell("Area '"+ lookedUp.Name()+"' added.");
                        }
                        else
                        {
                            mob.tell("Area '"+ lookedUp.Name()+"" +"' cannot be added because this would create a circular reference.");
                        }
                    }
                }
                else
                    mob.tell("'"+newArea+"' is not recognized as a valid area name.");
            }
        }
    }

    protected void genCloseWord(MOB mob, Exit E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Close Word: '"+E.closeWord()+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newName=mob.session().prompt("Enter something new\n\r:","");
        if(newName.length()>0)
            E.setExitParams(E.doorName(),newName,E.openWord(),E.closedText());
        else
            mob.tell("(no change)");
    }

    protected void genExitMisc(MOB mob, Exit E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        if(E.hasALock())
        {
            E.setReadable(false);
            mob.tell(showNumber+". Assigned Key Item: '"+E.keyName()+"'.");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            String newName=mob.session().prompt("Enter something new (null=blank)\n\r:","");
            if(newName.equalsIgnoreCase("null"))
                E.setKeyName("");
            else
            if(newName.length()>0)
                E.setKeyName(newName);
            else
                mob.tell("(no change)");
        }
        else
        {
            if((showFlag!=showNumber)&&(showFlag>-999))
            {
                if(!E.isReadable())
                    mob.tell(showNumber+". Door not is readable.");
                else
                    mob.tell(showNumber+". Door is readable: "+E.readableText());
                return;
            }
            else
            if(genGenericPrompt(mob,"Is this door ",E.isReadable()))
            {
                E.setReadable(true);
                mob.tell("\n\rText: '"+E.readableText()+"'.");
                String newName=mob.session().prompt("Enter something new (null=blank)\n\r:","");
                if(newName.equalsIgnoreCase("null"))
                    E.setReadableText("");
                else
                if(newName.length()>0)
                    E.setReadableText(newName);
                else
                    mob.tell("(no change)");
            }
            else
                E.setReadable(false);
        }
    }

    protected void genReadable1(MOB mob, Item E, int showNumber, int showFlag)
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;

        if((E instanceof Wand)
         ||(E instanceof SpellHolder)
         ||(E instanceof Light)
         ||(E instanceof Container)
         ||(E instanceof Ammunition)
         ||((E instanceof ClanItem)
             &&((((ClanItem)E).ciType()==ClanItem.CI_GATHERITEM)
                 ||(((ClanItem)E).ciType()==ClanItem.CI_CRAFTITEM)
                 ||(((ClanItem)E).ciType()==ClanItem.CI_SPECIALAPRON)))
         ||(E instanceof Key))
            CMLib.flags().setReadable(E,false);
        else
        if((CMClass.classID(E).endsWith("Readable"))
        ||(E instanceof Recipe)
        ||(E instanceof com.planet_ink.coffee_mud.Items.interfaces.Map))
            CMLib.flags().setReadable(E,true);
        else
        if((showFlag!=showNumber)&&(showFlag>-999))
            mob.tell(showNumber+". Item is readable: "+CMLib.flags().isReadable(E)+"");
        else
            CMLib.flags().setReadable(E,genGenericPrompt(mob,showNumber+". Is this item readable",CMLib.flags().isReadable(E)));
    }

    protected void genReadable2(MOB mob, Item E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;

        if((CMLib.flags().isReadable(E))
         ||(E instanceof SpellHolder)
         ||(E instanceof Ammunition)
         ||(E instanceof Recipe)
         ||(E instanceof Exit)
         ||(E instanceof Wand)
         ||(E instanceof ClanItem)
         ||(E instanceof Light)
         ||(E instanceof Key))
        {
            boolean ok=false;
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
            {
                if(CMClass.classID(E).endsWith("SuperPill"))
                {
                    mob.tell(showNumber+". Assigned Spell or Parameters: '"+E.readableText()+"'.");
                    ok=true;
                }
                else
                if(E instanceof SpellHolder)
                    mob.tell(showNumber+". Assigned Spell(s) ( ';' delimited)\n: '"+E.readableText()+"'.");
                else
                if(E instanceof Ammunition)
                {
                    mob.tell(showNumber+". Ammunition type: '"+E.readableText()+"'.");
                    ok=true;
                }
                else
                if(E instanceof Exit)
                {
                    mob.tell(showNumber+". Assigned Room IDs: '"+E.readableText()+"'.");
                    ok=true;
                }
                else
                if(E instanceof Wand)
                    mob.tell(showNumber+". Assigned Spell Name: '"+E.readableText()+"'.");
                else
                if(E instanceof Key)
                {
                    mob.tell(showNumber+". Assigned Key Code: '"+E.readableText()+"'.");
                    ok=true;
                }
                else
                if(E instanceof com.planet_ink.coffee_mud.Items.interfaces.Map)
                {
                    mob.tell(showNumber+". Assigned Map Area(s): '"+E.readableText()+"'.");
                    ok=true;
                }
                else
                if(E instanceof Light)
                {
                    mob.tell(showNumber+". Light duration (before burn out): '"+CMath.s_int(E.readableText())+"'.");
                    ok=true;
                }
                else
                {
                    mob.tell(showNumber+". Assigned Read Text: '"+E.readableText()+"'.");
                    ok=true;
                }

                if((showFlag!=showNumber)&&(showFlag>-999)) return;
                String newName=null;

                if((E instanceof Wand)
                ||((E instanceof SpellHolder)&&(!(CMClass.classID(E).endsWith("SuperPill")))))
                {
                    newName=mob.session().prompt("Enter something new (?)\n\r:","");
                    if(newName.length()==0)
                        ok=true;
                    else
                    {
                        if(newName.equalsIgnoreCase("?"))
                            mob.tell(CMLib.lister().reallyList(CMClass.abilities(),-1).toString());
                        else
                        if(E instanceof Wand)
                        {
                            if(CMClass.getAbility(newName)!=null)
                                ok=true;
                            else
                                mob.tell("'"+newName+"' is not recognized.  Try '?'.");
                        }
                        else
                        if(E instanceof SpellHolder)
                        {
                            String oldName=newName;
                            if(!newName.endsWith(";")) newName+=";";
                            int x=newName.indexOf(";");
                            while(x>=0)
                            {
                                String spellName=newName.substring(0,x).trim();
                                if(CMClass.getAbility(spellName)!=null)
                                    ok=true;
                                else
                                {
                                    mob.tell("'"+spellName+"' is not recognized.  Try '?'.");
                                    break;
                                }
                                newName=newName.substring(x+1).trim();
                                x=newName.indexOf(";");
                            }
                            newName=oldName;
                        }
                    }
                }
                else
                    newName=mob.session().prompt("Enter something new (null=blank)\n\r:","");

                if(ok)
                {
                    if(newName.equalsIgnoreCase("null"))
                        E.setReadableText("");
                    else
                    if(newName.length()>0)
                        E.setReadableText(newName);
                    else
                        mob.tell("(no change)");
                }
            }
        }
        else
        if(E instanceof Drink)
        {
            mob.session().println(showNumber+". Current liquid type: "+RawMaterial.CODES.NAME(((Drink)E).liquidType()));
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            boolean q=false;
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!q))
            {
                String newType=mob.session().prompt("Enter a new type (?)\n\r:",RawMaterial.CODES.NAME(((Drink)E).liquidType()));
                if(newType.equals("?"))
                {
                    StringBuffer say=new StringBuffer("");
                    List<Integer> liquids = RawMaterial.CODES.COMPOSE_RESOURCES(RawMaterial.MATERIAL_LIQUID);
                    for(Integer code : liquids)
                        say.append(RawMaterial.CODES.NAME(code.intValue())+", ");
                    mob.tell(say.toString().substring(0,say.length()-2));
                    q=false;
                }
                else
                {
                    q=true;
                    int newValue=RawMaterial.CODES.FIND_IgnoreCase(newType);
                    if((newValue&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LIQUID)
                    	newValue=-1;
                    if(newValue>=0)
                        ((Drink)E).setLiquidType(newValue);
                    else
                        mob.tell("(no change)");
                }
            }
        }
    }

    protected void genRecipe(MOB mob, Recipe E, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String prompt="Recipe Data ";
        mob.tell(showNumber+". "+prompt+": "+E.getCommonSkillID()+".");
        mob.tell(CMStrings.padRight(" ",(""+showNumber).length()+2+prompt.length())+": "+CMStrings.replaceAll(E.getRecipeCodeLine(),"\t",",")+".");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        while(!mob.session().killFlag())
        {
            String newName=mob.session().prompt("Enter new skill id (?)\n\r:","");
            if(newName.equalsIgnoreCase("?"))
            {
                StringBuffer str=new StringBuffer("");
                Ability A=null;
                for(Enumeration e=CMClass.abilities();e.hasMoreElements();)
                {
                    A=(Ability)e.nextElement();
                    if(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)
                    &&((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL))
                        str.append(A.ID()+"\n\r");
                }
                mob.tell("\n\rCommon Skills:\n\r"+str.toString()+"\n\r");
            }
            else
            if((newName.length()>0)
            &&(CMClass.getAbility(newName)!=null)
            &&((CMClass.getAbility(newName).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL))
            {
                E.setCommonSkillID(CMClass.getAbility(newName).ID());
                break;
            }
            else
            if(newName.length()>0)
                mob.tell("'"+newName+"' is not a valid common skill.  Try ?.");
            else
            {
                mob.tell("(no change)");
                break;
            }
        }
        String newName=mob.session().prompt("Enter new data line\n\r:","");
        if(newName.length()>0)
            E.setRecipeCodeLine(CMStrings.replaceAll(newName,",","\t"));
        else
            mob.tell("(no change)");
    }

    protected void genGettable(MOB mob, Item E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        if(E instanceof Potion)
            ((Potion)E).setDrunk(false);

        String c="Q";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(!c.equals("\n")))
        {
            mob.session().println(showNumber+". A) Is Gettable   : "+(!CMath.bset(E.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOTGET)));
            mob.session().println("    B) Is Droppable  : "+(!CMath.bset(E.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNODROP)));
            mob.session().println("    C) Is Removable  : "+(!CMath.bset(E.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOREMOVE)));
            mob.session().println("    D) Non-Locatable : "+(((E.baseEnvStats().sensesMask()&EnvStats.SENSE_UNLOCATABLE)>0)?"true":"false"));
            if(E instanceof Weapon)
                mob.session().println("    E) Is Two-Handed : "+E.rawLogicalAnd());
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            c=mob.session().choose("Enter one to change, or ENTER when done:","ABCDE\n","\n").toUpperCase();
            switch(Character.toUpperCase(c.charAt(0)))
            {
            case 'A': CMLib.flags().setGettable(E,(CMath.bset(E.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOTGET))); break;
            case 'B': CMLib.flags().setDroppable(E,(CMath.bset(E.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNODROP))); break;
            case 'C': CMLib.flags().setRemovable(E,(CMath.bset(E.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOREMOVE))); break;
            case 'D': if((E.baseEnvStats().sensesMask()&EnvStats.SENSE_UNLOCATABLE)>0)
                          E.baseEnvStats().setSensesMask(E.baseEnvStats().sensesMask()-EnvStats.SENSE_UNLOCATABLE);
                      else
                          E.baseEnvStats().setSensesMask(E.baseEnvStats().sensesMask()|EnvStats.SENSE_UNLOCATABLE);
                      break;
            case 'E': if(E instanceof Weapon)
                          E.setRawLogicalAnd(!E.rawLogicalAnd());
                      break;
            }
        }
    }

    protected void toggleDispositionMask(EnvStats E, int mask)
    {
        int current=E.disposition();
        if((current&mask)==0)
            E.setDisposition(current|mask);
        else
            E.setDisposition(current&((int)(EnvStats.ALLMASK-mask)));
    }

    protected void genDisposition(MOB mob, EnvStats E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        int[] disps={EnvStats.IS_INVISIBLE,
                     EnvStats.IS_HIDDEN,
                     EnvStats.IS_NOT_SEEN,
                     EnvStats.IS_BONUS,
                     EnvStats.IS_GLOWING,
                     EnvStats.IS_LIGHTSOURCE,
                     EnvStats.IS_FLYING,
                     EnvStats.IS_CLIMBING,
                     EnvStats.IS_SNEAKING,
                     EnvStats.IS_SWIMMING,
                     EnvStats.IS_EVIL,
                     EnvStats.IS_GOOD};
        String[] briefs={"invisible",
                         "hide",
                         "unseen",
                         "magical",
                         "glowing",
                         "lightsrc",
                         "fly",
                         "climb",
                         "sneak",
                         "swimmer",
                         "evil",
                         "good"};
        if((showFlag!=showNumber)&&(showFlag>-999))
        {
            StringBuffer buf=new StringBuffer(showNumber+". Dispositions: ");
            for(int i=0;i<disps.length;i++)
            {
                int mask=disps[i];
                if((E.disposition()&mask)!=0)
                    buf.append(briefs[i]+" ");
            }
            mob.tell(buf.toString());
            return;
        }
        String c="Q";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(!c.equals("\n")))
        {
            char letter='A';
            String letters="";
            for(int i=0;i<disps.length;i++)
            {
                int mask=disps[i];
                for(int num=0;num<EnvStats.IS_DESCS.length;num++)
                    if(mask==CMath.pow(2,num))
                    {
                        mob.session().println("    "+letter+") "+CMStrings.padRight(EnvStats.IS_DESCS[num],20)+":"+((E.disposition()&mask)!=0));
                        letters+=letter;
                        break;
                    }
                letter++;
            }
            c=mob.session().choose("Enter one to change, or ENTER when done: ",letters+"\n","\n").toUpperCase();
            letter='A';
            for(int i=0;i<disps.length;i++)
            {
                int mask=disps[i];
                if(letter==Character.toUpperCase(c.charAt(0)))
                {
                    toggleDispositionMask(E,mask);
                    break;
                }
                letter++;
            }
        }
    }

    public boolean genGenericPrompt(MOB mob, String prompt, boolean val)
    {
        try
        {
            prompt=CMStrings.padRight(prompt,35);
            if(val)
                prompt+="(Y/n): ";
            else
                prompt+="(y/N): ";

            return mob.session().confirm(prompt,val?"Y":"N");
        }
        catch(IOException e)
        {
            return val;
        }
    }

    protected void toggleSensesMask(EnvStats E, int mask)
    {
        int current=E.sensesMask();
        if((current&mask)==0)
            E.setSensesMask(current|mask);
        else
            E.setSensesMask(current&((int)(EnvStats.ALLMASK-mask)));
    }

    protected void toggleClimateMask(Area A, int mask)
    {
        int current=A.climateType();
        if((current&mask)==0)
            A.setClimateType(current|mask);
        else
            A.setClimateType(current&((int)(EnvStats.ALLMASK-mask)));
    }



    protected void genClimateType(MOB mob, Area A, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String c="Q";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(!c.equals("\n")))
        {
            mob.session().println(""+showNumber+". Climate:");
            mob.session().println("    R) Wet and Rainy    : "+((A.climateType()&Area.CLIMASK_WET)>0));
            mob.session().println("    H) Excessively hot  : "+((A.climateType()&Area.CLIMASK_HOT)>0));
            mob.session().println("    C) Excessively cold : "+((A.climateType()&Area.CLIMASK_COLD)>0));
            mob.session().println("    W) Very windy       : "+((A.climateType()&Area.CLIMATE_WINDY)>0));
            mob.session().println("    D) Very dry         : "+((A.climateType()&Area.CLIMASK_DRY)>0));
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            c=mob.session().choose("Enter one to change, or ENTER when done: ","RHCWD\n","\n").toUpperCase();
            switch(c.charAt(0))
            {
            case 'C': toggleClimateMask(A,Area.CLIMASK_COLD); break;
            case 'H': toggleClimateMask(A,Area.CLIMASK_HOT); break;
            case 'R': toggleClimateMask(A,Area.CLIMASK_WET); break;
            case 'W': toggleClimateMask(A,Area.CLIMATE_WINDY); break;
            case 'D': toggleClimateMask(A,Area.CLIMASK_DRY); break;
            }
        }
    }

    protected void genCharStats(MOB mob, CharStats E)
    throws IOException
    {
        String c="Q";
        String commandStr="ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*()=+-";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(!c.equals("\n")))
        {
            for(int i : CharStats.CODES.ALL())
                if(i!=CharStats.STAT_GENDER)
                    mob.session().println("    "+commandStr.charAt(i)+") "+CMStrings.padRight(CharStats.CODES.DESC(i),20)+":"+((E.getStat(i))));
            c=mob.session().choose("Enter one to change, or ENTER when done: ",commandStr.substring(0,CharStats.CODES.TOTAL())+"\n","\n").toUpperCase();
            int num=commandStr.indexOf(c);
            if(num>=0)
            {
                String newVal=mob.session().prompt("Enter a new value:  "+CharStats.CODES.DESC(num)+" ("+E.getStat(num)+"): ","");
                if(((CMath.s_int(newVal)>0)||(newVal.trim().equals("0")))
                &&(num!=CharStats.STAT_GENDER))
                    E.setStat(num,CMath.s_int(newVal));
                else
                    mob.tell("(no change)");
            }
        }
    }


    protected void genCharStats(MOB mob, MOB E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        if((showFlag!=showNumber)&&(showFlag>-999))
        {
            StringBuffer buf=new StringBuffer(showNumber+". Stats: ");
			for(int i : CharStats.CODES.BASE())
                buf.append(CharStats.CODES.ABBR(i)+":"+E.baseCharStats().getStat(i)+" ");
            mob.tell(buf.toString());
            return;
        }
        String c="Q";
        String commandStr="ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*()=+-";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(!c.equals("\n")))
        {
            for(int i : CharStats.CODES.ALL())
                if(i!=CharStats.STAT_GENDER)
                    mob.session().println("    "+commandStr.charAt(i)+") "+CMStrings.padRight(CharStats.CODES.DESC(i),20)+":"+((E.baseCharStats().getStat(i))));
            c=mob.session().choose("Enter one to change, or ENTER when done: ",commandStr.substring(0,CharStats.CODES.TOTAL())+"\n","\n").toUpperCase();
            int num=commandStr.indexOf(c);
            if(num>=0)
            {
                String newVal=mob.session().prompt("Enter a new value:  "+CharStats.CODES.DESC(num)+" ("+E.baseCharStats().getStat(num)+"): ","");
                if(((CMath.s_int(newVal)>0)||(newVal.trim().equals("0")))
                &&(num!=CharStats.STAT_GENDER))
                {
                    E.baseCharStats().setStat(num,CMath.s_int(newVal));
                    if((num==CharStats.STAT_AGE)&&(E.playerStats()!=null)&&(E.playerStats().getBirthday()!=null))
                        E.playerStats().getBirthday()[2]=CMLib.time().globalClock().getYear()-CMath.s_int(newVal);
                }
                else
                    mob.tell("(no change)");
            }
        }
    }

    protected void genSensesMask(MOB mob, EnvStats E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        int[] senses={EnvStats.CAN_SEE_DARK,
                      EnvStats.CAN_SEE_HIDDEN,
                      EnvStats.CAN_SEE_INVISIBLE,
                      EnvStats.CAN_SEE_SNEAKERS,
                      EnvStats.CAN_SEE_INFRARED,
                      EnvStats.CAN_SEE_GOOD,
                      EnvStats.CAN_SEE_EVIL,
                      EnvStats.CAN_SEE_BONUS,
                      EnvStats.CAN_NOT_SPEAK,
                      EnvStats.CAN_NOT_HEAR,
                      EnvStats.CAN_NOT_SEE};
        String[] briefs={"darkvision",
                         "hidden",
                         "invisible",
                         "sneakers",
                         "infrared",
                         "good",
                         "evil",
                         "magic",
                         "MUTE",
                         "DEAF",
                         "BLIND"};
        if((showFlag!=showNumber)&&(showFlag>-999))
        {
            StringBuffer buf=new StringBuffer(showNumber+". Senses: ");
            for(int i=0;i<senses.length;i++)
            {
                int mask=senses[i];
                if((E.sensesMask()&mask)!=0)
                    buf.append(briefs[i]+" ");
            }
            mob.tell(buf.toString());
            return;
        }
        String c="Q";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(!c.equals("\n")))
        {
            char letter='A';
            String letters="";
            for(int i=0;i<senses.length;i++)
            {
                int mask=senses[i];
                for(int num=0;num<EnvStats.CAN_SEE_DESCS.length;num++)
                    if(mask==CMath.pow(2,num))
                    {
                        letters+=letter;
                        mob.session().println("    "+letter+") "+CMStrings.padRight(EnvStats.CAN_SEE_DESCS[num],20)+":"+((E.sensesMask()&mask)!=0));
                        break;
                    }
                letter++;
            }
            c=mob.session().choose("Enter one to change, or ENTER when done: ",letters+"\n","\n").toUpperCase();
            letter='A';
            for(int i=0;i<senses.length;i++)
            {
                int mask=senses[i];
                if(letter==Character.toUpperCase(c.charAt(0)))
                {
                    toggleSensesMask(E,mask);
                    break;
                }
                letter++;
            }
        }
    }

    protected void genDoorsNLocks(MOB mob, Exit E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        boolean HasDoor=E.hasADoor();
        boolean Open=E.isOpen();
        boolean DefaultsClosed=E.defaultsClosed();
        boolean HasLock=E.hasALock();
        boolean Locked=E.isLocked();
        boolean DefaultsLocked=E.defaultsLocked();
        if((showFlag!=showNumber)&&(showFlag>-999)){
            mob.tell(showNumber+". Has a door: "+E.hasADoor()
                    +"\n\r   Has a lock  : "+E.hasALock()
                    +"\n\r   Open ticks: "+E.openDelayTicks());
            return;
        }

        if(genGenericPrompt(mob,"Has a door",E.hasADoor()))
        {
            HasDoor=true;
            DefaultsClosed=genGenericPrompt(mob,"Defaults closed",E.defaultsClosed());
            Open=!DefaultsClosed;
            if(genGenericPrompt(mob,"Has a lock",E.hasALock()))
            {
                HasLock=true;
                DefaultsLocked=genGenericPrompt(mob,"Defaults locked",E.defaultsLocked());
                Locked=DefaultsLocked;
            }
            else
            {
                HasLock=false;
                Locked=false;
                DefaultsLocked=false;
            }
            mob.tell("\n\rReset Delay (# ticks): '"+E.openDelayTicks()+"'.");
            int newLevel=CMath.s_int(mob.session().prompt("Enter a new delay\n\r:",""));
            if(newLevel>0)
                E.setOpenDelayTicks(newLevel);
            else
                mob.tell("(no change)");
        }
        else
        {
            HasDoor=false;
            Open=true;
            DefaultsClosed=false;
            HasLock=false;
            Locked=false;
            DefaultsLocked=false;
        }
        E.setDoorsNLocks(HasDoor,Open,DefaultsClosed,HasLock,Locked,DefaultsLocked);
    }

    public String makeContainerTypes(Container E)
    {
        String canContain=", "+Container.CONTAIN_DESCS[0];
        if(E.containTypes()>0)
        {
            canContain="";
            for(int i=0;i<Container.CONTAIN_DESCS.length-1;i++)
                if(CMath.isSet((int)E.containTypes(),i))
                    canContain+=", "+Container.CONTAIN_DESCS[i+1];
        }
        return canContain.substring(2);
    }


    protected void genLidsNLocks(MOB mob, Container E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        if((showFlag!=showNumber)&&(showFlag>-999)){
            mob.tell(showNumber+". Can contain : "+makeContainerTypes(E)
                    +"\n\r   Has a lid   : "+E.hasALid()
                    +"\n\r   Has a lock  : "+E.hasALock()
                    +"\n\r   Key name    : "+E.keyName());
            return;
        }
        String change="NO";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(change.length()>0))
        {
            mob.tell("\n\rCan only contain: "+makeContainerTypes(E));
            change=mob.session().prompt("Enter a type to add/remove (?)\n\r:","");
            if(change.length()==0) break;
            int found=-1;
            if(change.equalsIgnoreCase("?"))
                for(int i=0;i<Container.CONTAIN_DESCS.length;i++)
                    mob.tell(Container.CONTAIN_DESCS[i]);
            else
            {
                for(int i=0;i<Container.CONTAIN_DESCS.length;i++)
                    if(Container.CONTAIN_DESCS[i].startsWith(change.toUpperCase()))
                        found=i;
                if(found<0)
                    mob.tell("Unknown type.  Try '?'.");
                else
                if(found==0)
                    E.setContainTypes(0);
                else
                if(CMath.isSet((int)E.containTypes(),found-1))
                    E.setContainTypes(E.containTypes()-CMath.pow(2,found-1));
                else
                    E.setContainTypes(E.containTypes()|CMath.pow(2,found-1));
            }
        }

        if(genGenericPrompt(mob,"Has a lid " ,E.hasALid()))
        {
            E.setLidsNLocks(true,false,E.hasALock(),E.isLocked());
            if(genGenericPrompt(mob,"Has a lock",E.hasALock()))
            {
                E.setLidsNLocks(E.hasALid(),E.isOpen(),true,true);
                mob.tell("\n\rKey code: '"+E.keyName()+"'.");
                String newName=mob.session().prompt("Enter something new\n\r:","");
                if(newName.length()>0)
                    E.setKeyName(newName);
                else
                    mob.tell("(no change)");
            }
            else
            {
                E.setKeyName("");
                E.setLidsNLocks(E.hasALid(),E.isOpen(),false,false);
            }
        }
        else
        {
            E.setKeyName("");
            E.setLidsNLocks(false,true,false,false);
        }
    }

    protected void genLevel(MOB mob, Environmental E, int showNumber, int showFlag) throws IOException
    {
        if(E.baseEnvStats().level()<0) E.baseEnvStats().setLevel(1);
        E.baseEnvStats().setLevel(prompt(mob,E.baseEnvStats().level(),showNumber,showFlag,"Level"));
    }

    protected void genRejuv(MOB mob, Environmental E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        if(E instanceof Item)
            mob.tell(showNumber+". Rejuv/Pct: '"+E.baseEnvStats().rejuv()+"' (0=special).");
        else
            mob.tell(showNumber+". Rejuv Ticks: '"+E.baseEnvStats().rejuv()+"' (0=never).");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String rlevel=mob.session().prompt("Enter new amount\n\r:","");
        int newLevel=CMath.s_int(rlevel);
        if((newLevel>0)||(rlevel.trim().equals("0")))
        {
            E.baseEnvStats().setRejuv(newLevel);
            if((E.baseEnvStats().rejuv()==0)&&(E instanceof MOB))
            {
                E.baseEnvStats().setRejuv(Integer.MAX_VALUE);
                mob.tell(E.Name()+" will now never rejuvinate.");
            }
        }
        else
            mob.tell("(no change)");
    }

    protected void genUses(MOB mob, Item E, int showNumber, int showFlag) throws IOException
    { E.setUsesRemaining(prompt(mob,E.usesRemaining(),showNumber,showFlag,"Uses Remaining")); }

    protected void genMaxUses(MOB mob, Wand E, int showNumber, int showFlag) throws IOException
    { E.setMaxUses(prompt(mob,E.maxUses(),showNumber,showFlag,"Maximum Uses")); }

    protected void genCondition(MOB mob, Item E, int showNumber, int showFlag) throws IOException
    { E.setUsesRemaining(prompt(mob,E.usesRemaining(),showNumber,showFlag,"Condition")); }

    public void genMiscSet(MOB mob, Environmental E)
        throws IOException
    {
        if(CMLib.flags().isCataloged(E))
        {
            if(CMLib.catalog().isCatalogObj(E.Name()))
                mob.tell("*** This object is Cataloged **\n\r");
            else
                mob.tell("*** This object WAS cataloged and is still tied **\n\r");
        }

        if(E instanceof ShopKeeper)
            modifyGenShopkeeper(mob,(ShopKeeper)E);
        else
        if(E instanceof MOB)
        {
            if(((MOB)E).isMonster())
                modifyGenMOB(mob,(MOB)E);
            else
                modifyPlayer(mob,(MOB)E);
        }
        else
        if((E instanceof Exit)&&(!(E instanceof Item)))
            modifyGenExit(mob,(Exit)E);
        else
        if(E instanceof com.planet_ink.coffee_mud.Items.interfaces.Map)
            modifyGenMap(mob,(com.planet_ink.coffee_mud.Items.interfaces.Map)E);
        else
        if(E instanceof Armor)
            modifyGenArmor(mob,(Armor)E);
        else
        if(E instanceof MusicalInstrument)
            modifyGenInstrument(mob,(MusicalInstrument)E);
        else
        if(E instanceof Food)
            modifyGenFood(mob,(Food)E);
        else
        if((E instanceof Drink)&&(E instanceof Item))
            modifyGenDrink(mob,(Drink)E);
        else
        if(E instanceof Weapon)
            modifyGenWeapon(mob,(Weapon)E);
        else
        if(E instanceof Container)
            modifyGenContainer(mob,(Container)E);
        else
        if(E instanceof Item)
        {
            if(E.ID().equals("GenWallpaper"))
                modifyGenWallpaper(mob,(Item)E);
            else
                modifyGenItem(mob,(Item)E);
        }
        catalogCheckUpdate(mob, E);
    }

    public void genMiscText(MOB mob, Environmental E, int showNumber, int showFlag)
        throws IOException
    {
        if(E.isGeneric())
            genMiscSet(mob,E);
        else
        {   E.setMiscText(prompt(mob,E.text(),showNumber,showFlag,"Misc Text",true,false));}
    }

    protected void genTitleRoom(MOB mob, LandTitle E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Land plot ID: '"+E.landPropertyID()+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newText="?!?!";
        while((mob.session()!=null)&&(!mob.session().killFlag())
            &&((newText.length()>0)&&(CMLib.map().getRoom(newText)==null)))
        {
            newText=mob.session().prompt("New Property ID:","");
            if((newText.length()==0)
            &&(CMLib.map().getRoom(newText)==null)
            &&(CMLib.map().getArea(newText)==null))
                mob.tell("That property (room ID) doesn't exist!");
        }
        if(newText.length()>0)
            E.setLandPropertyID(newText);
        else
            mob.tell("(no change)");

    }

    public void genAbility(MOB mob, Environmental E, int showNumber, int showFlag) throws IOException
    { E.baseEnvStats().setAbility(prompt(mob,E.baseEnvStats().ability(),showNumber,showFlag,"Magical Ability")); }

    protected void genCoinStuff(MOB mob, Coins E, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Money data: '"+E.getNumberOfCoins()+" x "+CMLib.beanCounter().getDenominationName(E.getCurrency(),E.getDenomination())+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        boolean gocontinue=true;
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(gocontinue))
        {
            gocontinue=false;
            String oldCurrency=E.getCurrency();
            if(oldCurrency.length()==0) oldCurrency="Default";
            oldCurrency=mob.session().prompt("Enter currency code (?):",oldCurrency).trim().toUpperCase();
            if(oldCurrency.equalsIgnoreCase("Default"))
            {
                if(E.getCurrency().length()>0)
                    E.setCurrency("");
                else
                    mob.tell("(no change)");
            }
            else
            if((oldCurrency.length()==0)||(oldCurrency.equalsIgnoreCase(E.getCurrency())))
                mob.tell("(no change)");
            else
            if(!CMLib.beanCounter().getAllCurrencies().contains(oldCurrency))
            {
                Vector V=CMLib.beanCounter().getAllCurrencies();
                for(int v=0;v<V.size();v++)
                    if(((String)V.elementAt(v)).length()==0)
                        V.setElementAt("Default",v);
                mob.tell("'"+oldCurrency+"' is not a known currency. Existing currencies include: DEFAULT"+CMParms.toStringList(V));
                gocontinue=true;
            }
            else
                E.setCurrency(oldCurrency.toUpperCase().trim());
        }
        gocontinue=true;
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(gocontinue))
        {
            gocontinue=false;
            String newDenom=mob.session().prompt("Enter denomination (?):",""+E.getDenomination()).trim().toUpperCase();
            MoneyLibrary.MoneyDenomination[] DV=CMLib.beanCounter().getCurrencySet(E.getCurrency());
            if((newDenom.length()>0)
            &&(!CMath.isDouble(newDenom))
            &&(!newDenom.equalsIgnoreCase("?")))
            {
                double denom=CMLib.english().matchAnyDenomination(E.getCurrency(),newDenom);
                if(denom>0.0) newDenom=""+denom;
            }
            if((newDenom.length()==0)
            ||(CMath.isDouble(newDenom)
                &&(!newDenom.equalsIgnoreCase("?"))
                &&(CMath.s_double(newDenom)==E.getDenomination())))
                    mob.tell("(no change)");
            else
            if((newDenom.equalsIgnoreCase("?"))
            ||(!CMath.isDouble(newDenom))
            ||((DV!=null)&&(CMLib.beanCounter().getDenominationIndex(E.getCurrency(), CMath.s_double(newDenom))<0)))
            {
                StringBuffer allDenoms=new StringBuffer("");
                for(int i=0;i<DV.length;i++)
                    allDenoms.append(DV[i].value+"("+DV[i].name+"), ");
                if(allDenoms.toString().endsWith(", "))
                    allDenoms=new StringBuffer(allDenoms.substring(0,allDenoms.length()-2));
                mob.tell("'"+newDenom+"' is not a defined denomination. Try one of these: "+allDenoms.toString()+".");
                gocontinue=true;
            }
            else
                E.setDenomination(CMath.s_double(newDenom));
        }
        if((mob.session()!=null)&&(!mob.session().killFlag()))
            E.setNumberOfCoins(CMath.s_int(mob.session().prompt("Enter stack size\n\r:",""+E.getNumberOfCoins())));
    }

    protected void genHitPoints(MOB mob, MOB E, int showNumber, int showFlag) throws IOException
    {
        if(E.isMonster())
            E.baseEnvStats().setAbility(prompt(mob,E.baseEnvStats().ability(),showNumber,showFlag,"Hit Points Bonus Modifier","Hit points = (level*level) + (random*level*THIS)"));
        else
            E.baseEnvStats().setAbility(prompt(mob,E.baseEnvStats().ability(),showNumber,showFlag,"Ability -- unused"));
    }

    protected void genValue(MOB mob, Item E, int showNumber, int showFlag) throws IOException
    { E.setBaseValue(prompt(mob,E.baseGoldValue(),showNumber,showFlag,"Base Value")); }

    protected void genWeight(MOB mob, Environmental E, int showNumber, int showFlag) throws IOException
    { E.baseEnvStats().setWeight(prompt(mob,E.baseEnvStats().weight(),showNumber,showFlag,"Weight")); }

    protected void genClanItem(MOB mob, ClanItem E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Clan: '"+E.clanID()+"', Type: "+ClanItem.CI_DESC[E.ciType()]+".");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String clanID=E.clanID();
        E.setClanID(mob.session().prompt("Enter a new clan\n\r:",clanID));
        if(E.clanID().equals(clanID))
            mob.tell("(no change)");
        String clanType=ClanItem.CI_DESC[E.ciType()];
        String s="?";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(s.equals("?")))
        {
            s=mob.session().prompt("Enter a new type (?)\n\r:",clanType);
            if(s.equalsIgnoreCase("?"))
                mob.tell("Types: "+CMParms.toStringList(ClanItem.CI_DESC));
            else
            if(s.equalsIgnoreCase(clanType))
            {
                mob.tell("(no change)");
                break;
            }
            else
            {
                boolean found=false;
                for(int i=0;i<ClanItem.CI_DESC.length;i++)
                    if(ClanItem.CI_DESC[i].equalsIgnoreCase(s))
                    { found=true; E.setCIType(i); break;}
                if(!found)
                {
                    mob.tell("'"+s+"' is unknown.  Try '?'");
                    s="?";
                }
            }
        }
    }

    protected void genHeight(MOB mob, Environmental E, int showNumber, int showFlag) throws IOException
    { E.baseEnvStats().setHeight(prompt(mob,E.baseEnvStats().height(),showNumber,showFlag,"Height")); }


    protected void genSize(MOB mob, Armor E, int showNumber, int showFlag) throws IOException
    { E.baseEnvStats().setHeight(prompt(mob,E.baseEnvStats().height(),showNumber,showFlag,"Size")); }

    public void wornLayer(MOB mob, short[] layerAtt, short[] clothingLayer, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber))  return;
        boolean seeThroughBool=CMath.bset(layerAtt[0],Armor.LAYERMASK_SEETHROUGH);
        boolean multiWearBool=CMath.bset(layerAtt[0],Armor.LAYERMASK_MULTIWEAR);
        String seeThroughStr=(!seeThroughBool)?" (opaque)":" (see-through)";
        String multiWearStr=multiWearBool?" (multi)":"";
        mob.tell(showNumber+". Layer: '"+clothingLayer[0]+"'"+seeThroughStr+""+multiWearStr+".");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        if((mob.session()!=null)&&(!mob.session().killFlag()))
            clothingLayer[0] = CMath.s_short(mob.session().prompt("Enter a new layer\n\r:",""+clothingLayer[0]));
        boolean newSeeThrough=seeThroughBool;
        if((mob.session()!=null)&&(!mob.session().killFlag()))
            newSeeThrough=mob.session().confirm("Is see-through (Y/N)? ",""+seeThroughBool);
        boolean multiWear=multiWearBool;
        if((mob.session()!=null)&&(!mob.session().killFlag()))
            multiWear=mob.session().confirm("Is multi-wear (Y/N)? ",""+multiWearBool);
        layerAtt[0] = (short)0;
        layerAtt[0] = (short)(layerAtt[0]|(newSeeThrough?Armor.LAYERMASK_SEETHROUGH:0));
        layerAtt[0] = (short)(layerAtt[0]|(multiWear?Armor.LAYERMASK_MULTIWEAR:0));
    }

    protected void genLayer(MOB mob, Armor E, int showNumber, int showFlag) throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber))  return;
        short[] layerAtt = new short[]{E.getLayerAttributes()};
        short[] clothingLayer = new short[]{E.getClothingLayer()};
        wornLayer(mob,layerAtt,clothingLayer,showNumber,showFlag);
        E.setClothingLayer(clothingLayer[0]);
        E.setLayerAttributes(layerAtt[0]);
    }

    protected void genCapacity(MOB mob, Container E, int showNumber, int showFlag) throws IOException
    { E.setCapacity(prompt(mob,E.capacity(),showNumber,showFlag,"Capacity")); }

    protected void genAttack(MOB mob, Environmental E, int showNumber, int showFlag) throws IOException
    { E.baseEnvStats().setAttackAdjustment(prompt(mob,E.baseEnvStats().attackAdjustment(),showNumber,showFlag,"Attack Adjustment")); }

    protected void genDamage(MOB mob, Environmental E, int showNumber, int showFlag) throws IOException
    { E.baseEnvStats().setDamage(prompt(mob,E.baseEnvStats().damage(),showNumber,showFlag,"Damage")); }

    protected void genBanker1(MOB mob, Banker E, int showNumber, int showFlag) throws IOException
    { E.setCoinInterest(prompt(mob,E.getCoinInterest(),showNumber,showFlag,"Coin Interest [% per real day]")); }

    protected void genBanker2(MOB mob, Banker E, int showNumber, int showFlag) throws IOException
    { E.setItemInterest(prompt(mob,E.getItemInterest(),showNumber,showFlag,"Item Interest [% per real day]")); }

    protected void genBanker3(MOB mob, Banker E, int showNumber, int showFlag) throws IOException
    { E.setBankChain(prompt(mob,E.bankChain(),showNumber,showFlag,"Bank Chain",false,false)); }

    protected void genBanker4(MOB mob, Banker E, int showNumber, int showFlag) throws IOException
    { E.setLoanInterest(prompt(mob,E.getLoanInterest(),showNumber,showFlag,"Loan Interest [% per mud month]")); }

    protected void genSpeed(MOB mob, Environmental E, int showNumber, int showFlag) throws IOException
    { E.baseEnvStats().setSpeed(prompt(mob,E.baseEnvStats().speed(),showNumber,showFlag,"Actions/Attacks per tick")); }

    protected void genArmor(MOB mob, Environmental E, int showNumber, int showFlag) throws IOException
    {
        if(E instanceof MOB)
            E.baseEnvStats().setArmor(prompt(mob,E.baseEnvStats().armor(),showNumber,showFlag,"Armor (lower-better)"));
        else
            E.baseEnvStats().setArmor(prompt(mob,E.baseEnvStats().armor(),showNumber,showFlag,"Armor (higher-better)"));
    }

    protected void genMoney(MOB mob, MOB E, int showNumber, int showFlag) throws IOException
    {
        if(E.getMoney()==0)
        {
            double d=CMLib.beanCounter().getTotalAbsoluteNativeValue(E);
            CMLib.beanCounter().subtractMoney(E,d);
            E.setMoney((int)Math.round(d));
        }
        CMLib.beanCounter().setMoney(E,prompt(mob,E.getMoney(),showNumber,showFlag,"Money"));
    }

    protected void genWeaponAmmo(MOB mob, Weapon E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String defaultAmmo=(E.requiresAmmunition())?"Y":"N";
        if((showFlag!=showNumber)&&(showFlag>-999))
        {
            mob.tell(showNumber+". Ammo required: "+E.requiresAmmunition()+" ("+E.ammunitionType()+")");
            return;
        }

        if(mob.session().confirm("Does this weapon require ammunition (default="+defaultAmmo+") (Y/N)?",defaultAmmo))
        {
            mob.tell("\n\rAmmo type: '"+E.ammunitionType()+"'.");
            String newName=mob.session().prompt("Enter a new one\n\r:","");
            if(newName.length()>0)
            {
                E.setAmmunitionType(newName);
                mob.tell("(Remember to create a GenAmmunition item with '"+E.ammunitionType()+"' in the secret identity, and the uses remaining above 0!");
            }
            else
                mob.tell("(no change)");
            mob.tell("\n\rAmmo capacity: '"+E.ammunitionCapacity()+"'.)");
            int newValue=CMath.s_int(mob.session().prompt("Enter a new value\n\r:",""));
            if(newValue>0)
                E.setAmmoCapacity(newValue);
            else
                mob.tell("(no change)");
            E.setAmmoRemaining(E.ammunitionCapacity());
        }
        else
        {
            E.setAmmunitionType("");
            E.setAmmoCapacity(0);
        }
    }
    protected void genWeaponRanges(MOB mob, Weapon E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Minimum/Maximum Ranges: "+E.minRange()+"/"+E.maxRange()+".");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newMinStr=mob.session().prompt("Enter a new minimum range\n\r:","");
        String newMaxStr=mob.session().prompt("Enter a new maximum range\n\r:","");
        if((newMinStr.length()==0)&&(newMaxStr.length()==0))
            mob.tell("(no change)");
        else
        {
            E.setRanges(CMath.s_int(newMinStr),CMath.s_int(newMaxStr));
            if((E.minRange()>E.maxRange())||(E.minRange()<0)||(E.maxRange()<0))
            {
                mob.tell("(defective entries.  resetting.)");
                E.setRanges(0,0);
            }
        }
    }

    protected void genWeaponType(MOB mob, Weapon E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Weapon Attack Type: '"+Weapon.TYPE_DESCS[E.weaponType()]+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        boolean q=false;
        String sel="NSPBFMR";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(!q))
        {
            String newType=mob.session().choose("Enter a new value\n\r:",sel+"?","");
            if(newType.equals("?"))
            {
                for(int i=0;i<sel.length();i++)
                    mob.tell(sel.charAt(i)+") "+Weapon.TYPE_DESCS[i]);
                q=false;
            }
            else
            {
                q=true;
                int newValue=-1;
                if(newType.length()>0)
                    newValue=sel.indexOf(newType.toUpperCase());
                if(newValue>=0)
                    E.setWeaponType(newValue);
                else
                    mob.tell("(no change)");
            }
        }
    }

    protected void genTechLevel(MOB mob, Area A, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Theme setting: '"+Area.THEME_PHRASE[A.getTechLevel()]+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        boolean q=false;
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(!q))
        {
            String newType=mob.session().prompt("Enter a new level (?)\n\r",Area.THEME_PHRASE[A.getTechLevel()]);
            if(newType.equals("?"))
            {
                StringBuffer say=new StringBuffer("");
                for(int i=1;i<Area.THEME_PHRASE.length;i++)
                    say.append(i+") "+Area.THEME_PHRASE[i]+"\n\r");
                mob.tell(say.toString());
                q=false;
            }
            else
            {
                q=true;
                int newValue=-1;
                if(CMath.s_int(newType)>0)
                    newValue=CMath.s_int(newType);
                else
                for(int i=0;i<Area.THEME_PHRASE.length;i++)
                    if(Area.THEME_PHRASE[i].toUpperCase().startsWith(newType.toUpperCase()))
                        newValue=i;
                if(newValue>=0)
                    A.setTechLevel(newValue);
                else
                    mob.tell("(no change)");
            }
        }
    }


    protected void genMaterialCode(MOB mob, Item E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Material Type: '"+RawMaterial.CODES.NAME(E.material())+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        boolean q=false;
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(!q))
        {
            String newType=mob.session().prompt("Enter a new material (?)\n\r:",RawMaterial.CODES.NAME(E.material()));
            if(newType.equals("?"))
            {
                StringBuffer say=new StringBuffer("");
                for(String S : RawMaterial.CODES.NAMES())
                    say.append(S+", ");
                mob.tell(say.toString().substring(0,say.length()-2));
                q=false;
            }
            else
            {
                q=true;
                int newValue=RawMaterial.CODES.FIND_IgnoreCase(newType);
                if(newValue>=0)
                    E.setMaterial(newValue);
                else
                    mob.tell("(no change)");
            }
        }
    }

    protected void genInstrumentType(MOB mob, MusicalInstrument E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Instrument Type: '"+MusicalInstrument.TYPE_DESC[E.instrumentType()]+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        boolean q=false;
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(!q))
        {
            String newType=mob.session().prompt("Enter a new type (?)\n\r:",MusicalInstrument.TYPE_DESC[E.instrumentType()]);
            if(newType.equals("?"))
            {
                StringBuffer say=new StringBuffer("");
                for(int i=0;i<MusicalInstrument.TYPE_DESC.length-1;i++)
                    say.append(MusicalInstrument.TYPE_DESC[i]+", ");
                mob.tell(say.toString().substring(0,say.length()-2));
                q=false;
            }
            else
            {
                q=true;
                int newValue=-1;
                for(int i=0;i<MusicalInstrument.TYPE_DESC.length-1;i++)
                    if(newType.equalsIgnoreCase(MusicalInstrument.TYPE_DESC[i]))
                        newValue=i;
                if(newValue>=0)
                    E.setInstrumentType(newValue);
                else
                    mob.tell("(no change)");
            }
        }
    }

    protected void genSpecialFaction(MOB mob, MOB E, int showNumber, int showFlag, Faction F)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        if(F==null) return;
        Faction.FactionRange myFR=CMLib.factions().getRange(F.factionID(),E.fetchFaction(F.factionID()));
        mob.tell(showNumber+". "+F.name()+": "+((myFR!=null)?myFR.name():"UNDEFINED")+" ("+E.fetchFaction(F.factionID())+")");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        for(Enumeration e=F.ranges();e.hasMoreElements();)
        {
            Faction.FactionRange FR=(Faction.FactionRange)e.nextElement();
            mob.tell(CMStrings.padRight(FR.name(),20)+": "+FR.low()+" - "+FR.high()+")");
        }
        String newOne=mob.session().prompt("Enter a new value\n\r:");
        if(CMath.isInteger(newOne))
        {
            E.addFaction(F.factionID(),CMath.s_int(newOne));
            return;
        }
        for(Enumeration e=F.ranges();e.hasMoreElements();)
        {
            Faction.FactionRange FR=(Faction.FactionRange)e.nextElement();
            if(FR.name().toUpperCase().startsWith(newOne.toUpperCase()))
            {
                if(FR.low()==F.minimum())
                    E.addFaction(F.factionID(),FR.low());
                else
                if(FR.high()==F.maximum())
                    E.addFaction(F.factionID(),FR.high());
                else
                    E.addFaction(F.factionID(),FR.low()+((FR.high()-FR.low())/2));
                return;
            }
        }
        mob.tell("(no change)");
    }
    protected void genFaction(MOB mob, MOB E, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String newFact="Q";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(newFact.length()>0))
        {
            mob.tell(showNumber+". Factions: "+E.getFactionListing());
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            newFact=mob.session().prompt("Enter a faction name to add or remove\n\r:","");
            if(newFact.length()>0)
            {
                Faction lookedUp=CMLib.factions().getFactionByName(newFact);
                if(lookedUp==null) lookedUp=CMLib.factions().getFaction(newFact);
                if(lookedUp!=null)
                {
                    if (E.fetchFaction(lookedUp.factionID())!=Integer.MAX_VALUE)
                    {
                        // this mob already has this faction, they must want it removed
                        E.removeFaction(lookedUp.factionID());
                        mob.tell("Faction '"+lookedUp.name()  +"' removed.");
                    }
                    else
                    {
                        String howMuch = mob.session().prompt("How much faction ("+lookedUp.findDefault(E)+")?",
                                   Integer.toString(lookedUp.findDefault(E)));
                        if(CMath.isInteger(howMuch)) {
                            int value =Integer.valueOf(howMuch).intValue();
                            if(value<lookedUp.minimum()) value=lookedUp.minimum();
                            if(value>lookedUp.maximum()) value=lookedUp.maximum();
                            E.addFaction(lookedUp.factionID(),value);
                            mob.tell("Faction '"+lookedUp.name() +"' added.");
                        }
                        else
                            mob.tell("'"+howMuch+"' is not a valid number.");
                    }
                 }
                 else
                    mob.tell("'"+newFact+"' is not recognized as a valid faction name or file.");
            }
        }
    }

    protected void genGender(MOB mob, MOB E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Gender: '"+Character.toUpperCase((char)E.baseCharStats().getStat(CharStats.STAT_GENDER))+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newType=mob.session().choose("Enter a new gender (M/F/N)\n\r:","MFN","");
        int newValue=-1;
        if(newType.length()>0)
            newValue=("MFN").indexOf(newType.trim().toUpperCase());
        if(newValue>=0)
        {
            switch(newValue)
            {
            case 0:
                E.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
                break;
            case 1:
                E.baseCharStats().setStat(CharStats.STAT_GENDER,'F');
                break;
            case 2:
                E.baseCharStats().setStat(CharStats.STAT_GENDER,'N');
                break;
            }
        }
        else
            mob.tell("(no change)");
    }

    protected void genWeaponClassification(MOB mob, Weapon E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Weapon Classification: '"+Weapon.CLASS_DESCS[E.weaponClassification()]+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        boolean q=false;
        String sel=("ABEFHKPRSDTN");
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(!q))
        {
            String newType=mob.session().choose("Enter a new value (?)\n\r:",sel+"?","");
            if(newType.equals("?"))
            {
                for(int i=0;i<sel.length();i++)
                    mob.tell(sel.charAt(i)+") "+Weapon.CLASS_DESCS[i]);
                q=false;
            }
            else
            {
                q=true;
                int newValue=-1;
                if(newType.length()>0)
                    newValue=sel.indexOf(newType.toUpperCase());
                if(newValue>=0)
                    E.setWeaponClassification(newValue);
                else
                    mob.tell("(no change)");
            }
        }
    }

    protected void genSecretIdentity(MOB mob, Item E, int showNumber, int showFlag) throws IOException
    { E.setSecretIdentity(prompt(mob,E.rawSecretIdentity(),showNumber,showFlag,"Secret Identity",true,false)); }

    protected void genNourishment(MOB mob, Food E, int showNumber, int showFlag) throws IOException
    { E.setNourishment(prompt(mob,E.nourishment(),showNumber,showFlag,"Nourishment/Eat")); }

    protected void genBiteSize(MOB mob, Food E, int showNumber, int showFlag) throws IOException
    { E.setBite(prompt(mob,E.bite(),showNumber,showFlag,"Bite/Eat (0=all)")); }

    protected void genRace(MOB mob, MOB E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String raceID="begin!";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(raceID.length()>0))
        {
            mob.tell(showNumber+". Race: '"+E.baseCharStats().getMyRace().ID()+"'.");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            raceID=mob.session().prompt("Enter a new race (?)\n\r:","").trim();
            if(raceID.equalsIgnoreCase("?"))
                mob.tell(CMLib.lister().reallyList(CMClass.races(),-1).toString());
            else
            if(raceID.length()==0)
                mob.tell("(no change)");
            else
            {
                Race R=CMClass.getRace(raceID);
                if(R!=null)
                {
                    E.baseCharStats().setMyRace(R);
                    E.baseCharStats().getMyRace().startRacing(E,false);
                    E.baseCharStats().getMyRace().setHeightWeight(E.baseEnvStats(),(char)E.baseCharStats().getStat(CharStats.STAT_GENDER));
                }
                else
                    mob.tell("Unknown race! Try '?'.");
            }
        }
    }

    protected void genCharClass(MOB mob, MOB E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String classID="begin!";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(classID.length()>0))
        {
            StringBuffer str=new StringBuffer("");
            for(int c=0;c<E.baseCharStats().numClasses();c++)
            {
                CharClass C=E.baseCharStats().getMyClass(c);
                str.append(C.ID()+"("+E.baseCharStats().getClassLevel(C)+") ");
            }
            mob.tell(showNumber+". Class: '"+str.toString()+"'.");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            classID=mob.session().prompt("Enter a class to add/remove(?)\n\r:","").trim();
            if(classID.equalsIgnoreCase("?"))
                mob.tell(CMLib.lister().reallyList(CMClass.charClasses(),-1).toString());
            else
            if(classID.length()==0)
                mob.tell("(no change)");
            else
            {
                CharClass C=CMClass.getCharClass(classID);
                if(C!=null)
                {
                    if(E.baseCharStats().getClassLevel(C)>=0)
                    {
                        if(E.baseCharStats().numClasses()<2)
                            mob.tell("Final class may not be removed.  To change a class, add the new one first.");
                        else
                        {
                            StringBuffer charClasses=new StringBuffer("");
                            StringBuffer classLevels=new StringBuffer("");
                            for(int c=0;c<E.baseCharStats().numClasses();c++)
                            {
                                CharClass C2=E.baseCharStats().getMyClass(c);
                                int L2=E.baseCharStats().getClassLevel(C2);
                                if(C2!=C)
                                {
                                    charClasses.append(";"+C2.ID());
                                    classLevels.append(";"+L2);
                                }
                            }
                            E.baseCharStats().setMyClasses(charClasses.toString());
                            E.baseCharStats().setMyLevels(classLevels.toString());
                        }
                    }
                    else
                    {
                        int highLvl=Integer.MIN_VALUE;
                        CharClass highestC=null;
                        for(int c=0;c<E.baseCharStats().numClasses();c++)
                        {
                            CharClass C2=E.baseCharStats().getMyClass(c);
                            if(E.baseCharStats().getClassLevel(C2)>highLvl)
                            {
                                highestC=C2;
                                highLvl=E.baseCharStats().getClassLevel(C2);
                            }
                        }
                        E.baseCharStats().setCurrentClass(C);
                        int levels=E.baseCharStats().combinedSubLevels();
                        levels=E.baseEnvStats().level()-levels;
                        String lvl=null;
                        if(levels>0)
                        {
                            lvl=mob.session().prompt("Levels to give this class ("+levels+")\n\r:",""+levels).trim();
                            int lvl2=CMath.s_int(lvl);
                            if(lvl2>levels) lvl2=levels;
                            E.baseCharStats().setClassLevel(C,lvl2);
                        }
                        else
                        if(highestC!=null)
                        {
                            lvl=mob.session().prompt("Levels to siphon from "+highestC.ID()+" for this class (0)\n\r:",""+0).trim();
                            int lvl2=CMath.s_int(lvl);
                            if(lvl2>highLvl) lvl2=highLvl;
                            E.baseCharStats().setClassLevel(highestC,highLvl-lvl2);
                            E.baseCharStats().setClassLevel(C,lvl2);
                        }
                    }
                    int levels=E.baseCharStats().combinedSubLevels();
                    levels=E.baseEnvStats().level()-levels;
                    C=E.baseCharStats().getCurrentClass();
                    E.baseCharStats().setClassLevel(C,levels);
                }
                else
                    mob.tell("Unknown character class! Try '?'.");
            }
        }
    }

    protected void genTattoos(MOB mob, MOB E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String behave="NO";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
        {
            String tattoostr="";
            for(int b=0;b<E.numTattoos();b++)
            {
                String B=E.fetchTattoo(b);
                if(B!=null) tattoostr+=B+", ";
            }
            if(tattoostr.length()>0)
                tattoostr=tattoostr.substring(0,tattoostr.length()-2);
            if((tattoostr.length()>60)&&((showFlag!=showNumber)&&(showFlag>-999)))
                tattoostr=tattoostr.substring(0,60)+"...";
            mob.tell(showNumber+". Tattoos: '"+tattoostr+"'.");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            behave=mob.session().prompt("Enter a tattoo to add/remove\n\r:","");
            if(behave.length()>0)
            {
                String tattoo=behave;
                if((tattoo.length()>0)
                &&(Character.isDigit(tattoo.charAt(0)))
                &&(tattoo.indexOf(" ")>0)
                &&(CMath.isNumber(tattoo.substring(0,tattoo.indexOf(" ")))))
                    tattoo=tattoo.substring(tattoo.indexOf(" ")+1).trim();
                if(E.fetchTattoo(tattoo)!=null)
                {
                    mob.tell(tattoo.trim().toUpperCase()+" removed.");
                    E.delTattoo(behave);
                }
                else
                {
                    mob.tell(behave.trim().toUpperCase()+" added.");
                    E.addTattoo(behave);
                }
            }
            else
                mob.tell("(no change)");
        }
    }

    protected void genTitles(MOB mob, MOB E, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        if(E.playerStats()==null) return;
        String behave="NO";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
        {
            String behaviorstr="";
            for(int b=0;b<E.playerStats().getTitles().size();b++)
            {
                String B=(String)E.playerStats().getTitles().elementAt(b);
                if(B!=null) behaviorstr+=B+", ";
            }
            if(behaviorstr.length()>0)
                behaviorstr=behaviorstr.substring(0,behaviorstr.length()-2);
            mob.tell(showNumber+". Titles: '"+behaviorstr+"'.");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            behave=mob.session().prompt("Enter a title to add or a number to remove\n\r:","");
            if(behave.length()>0)
            {
                String tattoo=behave;
                if((tattoo.length()>0)
                &&(CMath.isInteger(tattoo))
                &&(CMath.s_int(tattoo)>0)
                &&(CMath.s_int(tattoo)<=E.playerStats().getTitles().size()))
                    tattoo=(String)E.playerStats().getTitles().elementAt(CMath.s_int(tattoo)-1);
                else
                if((tattoo.length()>0)
                &&(Character.isDigit(tattoo.charAt(0)))
                &&(tattoo.indexOf(" ")>0)
                &&(CMath.isNumber(tattoo.substring(0,tattoo.indexOf(" ")))))
                    tattoo=tattoo.substring(tattoo.indexOf(" ")+1).trim();
                if(E.playerStats().getTitles().contains(tattoo))
                {
                    mob.tell(tattoo.trim().toUpperCase()+" removed.");
                    E.playerStats().getTitles().remove(tattoo);
                }
                else
                {
                    mob.tell(behave.trim().toUpperCase()+" added.");
                    E.playerStats().getTitles().addElement(tattoo);
                }
            }
            else
                mob.tell("(no change)");
        }
    }

    protected void genExpertises(MOB mob, MOB E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String behave="NO";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
        {
            String behaviorstr="";
            for(int b=0;b<E.numExpertises();b++)
            {
                String B=E.fetchExpertise(b);
                if(B!=null) behaviorstr+=B+", ";
            }
            if(behaviorstr.length()>0)
                behaviorstr=behaviorstr.substring(0,behaviorstr.length()-2);
            if((behaviorstr.length()>60)&&((showFlag!=showNumber)&&(showFlag>-999)))
                behaviorstr=behaviorstr.substring(0,60)+"...";
            mob.tell(showNumber+". Expertises: '"+behaviorstr+"'.");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            behave=mob.session().prompt("Enter a lesson to add/remove\n\r:","");
            if(behave.length()>0)
            {
                if(E.fetchExpertise(behave)!=null)
                {
                    mob.tell(behave+" removed.");
                    E.delExpertise(behave);
                }
                else
                {
                    mob.tell(behave+" added.");
                    E.addExpertise(behave);
                }
            }
            else
                mob.tell("(no change)");
        }
    }

    protected void genSecurity(MOB mob, MOB E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        PlayerStats P=E.playerStats();
        if(P==null) return;
        String behave="NO";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
        {
            String behaviorstr="";
            for(int b=0;b<P.getSecurityGroups().size();b++)
            {
                String B=(String)P.getSecurityGroups().elementAt(b);
                if(B!=null) behaviorstr+=B+", ";
            }
            if(behaviorstr.length()>0)
                behaviorstr=behaviorstr.substring(0,behaviorstr.length()-2);
            mob.tell(showNumber+". Security Groups: '"+behaviorstr+"'.");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            behave=mob.session().prompt("Enter a group to add/remove\n\r:","");
            if(behave.length()>0)
            {
                if(P.getSecurityGroups().contains(behave.trim().toUpperCase()))
                {
                    P.getSecurityGroups().remove(behave.trim().toUpperCase());
                    mob.tell(behave+" removed.");
                }
                else
                if((behave.trim().toUpperCase().startsWith("AREA "))
                &&(!CMSecurity.isAllowedAnywhere(mob,behave.trim().toUpperCase().substring(5).trim())))
                    mob.tell("You do not have clearance to add security code '"+behave+"' to this class.");
                else
                if((!behave.trim().toUpperCase().startsWith("AREA "))
                &&(!CMSecurity.isAllowedEverywhere(mob,behave.trim().toUpperCase())))
                    mob.tell("You do not have clearance to add security code '"+behave+"' to this class.");
                else
                {
                    P.getSecurityGroups().addElement(behave.trim().toUpperCase());
                    mob.tell(behave+" added.");
                }
            }
            else
                mob.tell("(no change)");
        }
    }

    public void genBehaviors(MOB mob, Environmental E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String behave="NO";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
        {
            String behaviorstr="";
            for(int b=0;b<E.numBehaviors();b++)
            {
                Behavior B=E.fetchBehavior(b);
                if((B!=null)&&(B.isSavable()))
                {
                    behaviorstr+=B.ID();
                    if(B.getParms().trim().length()>0)
                        behaviorstr+="("+B.getParms().trim()+"), ";
                    else
                        behaviorstr+=", ";
                }
            }
            if(behaviorstr.length()>0)
                behaviorstr=behaviorstr.substring(0,behaviorstr.length()-2);
            mob.tell(showNumber+". Behaviors: '"+behaviorstr+"'.");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            behave=mob.session().prompt("Enter a behavior to add/remove (?)\n\r:","");
            if(behave.length()>0)
            {
                if(behave.equalsIgnoreCase("?"))
                    mob.tell(CMLib.lister().reallyList(CMClass.behaviors(),-1).toString());
                else
                {
                    Behavior chosenOne=null;
                    for(int b=0;b<E.numBehaviors();b++)
                    {
                        Behavior B=E.fetchBehavior(b);
                        if((B!=null)&&(B.ID().equalsIgnoreCase(behave)))
                            chosenOne=B;
                    }
                    if(chosenOne!=null)
                    {
                        mob.tell(chosenOne.ID()+" removed.");
                        E.delBehavior(chosenOne);
                    }
                    else
                    {
                        chosenOne=CMClass.getBehavior(behave);
                        if(chosenOne!=null)
                        {
                            boolean alreadyHasIt=false;
                            for(int b=0;b<E.numBehaviors();b++)
                            {
                                Behavior B=E.fetchBehavior(b);
                                if((B!=null)&&(B.ID().equals(chosenOne.ID())))
                                {
                                    alreadyHasIt=true;
                                    chosenOne=B;
                                }
                            }
                            String parms="?";
                            while(parms.equals("?"))
                            {
                                parms=chosenOne.getParms();
                                parms=mob.session().prompt("Enter any behavior parameters (?)\n\r:"+parms);
                                if(parms.equals("?")){ StringBuilder s2=CMLib.help().getHelpText(chosenOne.ID(),mob,true); if(s2!=null) mob.tell(s2.toString()); else mob.tell("no help!");}
                            }
                            chosenOne.setParms(parms.trim());
                            if(!alreadyHasIt)
                            {
                                mob.tell(chosenOne.ID()+" added.");
                                E.addBehavior(chosenOne);
                            }
                            else
                                mob.tell(chosenOne.ID()+" re-added.");
                        }
                        else
                        {
                            mob.tell("'"+behave+"' is not recognized.  Try '?'.");
                        }
                    }
                }
            }
            else
                mob.tell("(no change)");
        }
    }

    public void genAffects(MOB mob, Environmental E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String behave="NO";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
        {
            String affectstr="";
            for(int b=0;b<E.numEffects();b++)
            {
                Ability A=E.fetchEffect(b);
                if((A!=null)&&(A.savable()))
                {
                    affectstr+=A.ID();
                    if(A.text().trim().length()>0)
                        affectstr+="("+A.text().trim()+"), ";
                    else
                        affectstr+=", ";
                }

            }
            if(affectstr.length()>0)
                affectstr=affectstr.substring(0,affectstr.length()-2);
            mob.tell(showNumber+". Effects: '"+affectstr+"'.");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            behave=mob.session().prompt("Enter an effect to add/remove (?)\n\r:","");
            if(behave.length()>0)
            {
                if(behave.equalsIgnoreCase("?"))
                    mob.tell(CMLib.lister().reallyList(CMClass.abilities(),-1).toString());
                else
                {
                    Ability chosenOne=null;
                    for(int a=0;a<E.numEffects();a++)
                    {
                        Ability A=E.fetchEffect(a);
                        if((A!=null)&&(A.ID().equalsIgnoreCase(behave)))
                            chosenOne=A;
                    }
                    if(chosenOne!=null)
                    {
                        mob.tell(chosenOne.ID()+" removed.");
                        E.delEffect(chosenOne);
                    }
                    else
                    {
                        chosenOne=CMClass.getAbility(behave);
                        if(chosenOne!=null)
                        {
                            String parms="?";
                            while(parms.equals("?"))
                            {
                                parms=chosenOne.text();
                                parms=mob.session().prompt("Enter any effect parameters (?)\n\r:"+parms);
                                if(parms.equals("?")){ StringBuilder s2=CMLib.help().getHelpText(chosenOne.ID(),mob,true); if(s2!=null) mob.tell(s2.toString()); else mob.tell("no help!");}
                            }
                            chosenOne.setMiscText(parms.trim());
                            mob.tell(chosenOne.ID()+" added.");
                            E.addNonUninvokableEffect(chosenOne);
                        }
                        else
                        {
                            mob.tell("'"+behave+"' is not recognized.  Try '?'.");
                        }
                    }
                }
            }
            else
                mob.tell("(no change)");
        }
    }

    protected void genRideable1(MOB mob, Rideable R, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Rideable Type: '"+Rideable.RIDEABLE_DESCS[R.rideBasis()]+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        boolean q=false;
        String sel="LWACBTEDG";
        while(!q)
        {
            String newType=mob.session().choose("Enter a new value (?)\n\r:",sel+"?","");
            if(newType.equals("?"))
            {
                for(int i=0;i<sel.length();i++)
                    mob.tell(sel.charAt(i)+") "+Rideable.RIDEABLE_DESCS[i].toLowerCase());
                q=false;
            }
            else
            {
                q=true;
                int newValue=-1;
                if(newType.length()>0)
                    newValue=sel.indexOf(newType.toUpperCase());
                if(newValue>=0)
                    R.setRideBasis(newValue);
                else
                    mob.tell("(no change)");
            }
        }
    }
    protected void genRideable2(MOB mob, Rideable E, int showNumber, int showFlag) throws IOException
    { E.setRiderCapacity(prompt(mob,E.riderCapacity(),showNumber,showFlag,"Number of MOBs held")); }

    protected void genShopkeeper1(MOB mob, ShopKeeper E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        long oldMask=E.getWhatIsSoldMask();
        while((mob.session()!=null)&&(!mob.session().killFlag()))
        {
	        mob.tell(showNumber+". Shopkeeper type: '"+E.storeKeeperString()+"'.");
	        if((showFlag!=showNumber)&&(showFlag>-999)) return;
	        
	        StringBuffer buf=new StringBuffer("");
	        StringBuffer codes=new StringBuffer("");
	        String codeStr="0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	        if(E instanceof Banker)
	        {
	            int r=ShopKeeper.DEAL_BANKER;
	            char c=codeStr.charAt(r);
	            codes.append(c);
	            buf.append(c+") "+ShopKeeper.DEAL_DESCS[r]+"\n\r");
	            r=ShopKeeper.DEAL_CLANBANKER;
	            c=codeStr.charAt(r);
	            codes.append(c);
	            buf.append(c+") "+ShopKeeper.DEAL_DESCS[r]+"\n\r");
	        }
	        else
	        if(E instanceof PostOffice)
	        {
	            int r=ShopKeeper.DEAL_POSTMAN;
	            char c=codeStr.charAt(r);
	            codes.append(c);
	            buf.append(c+") "+ShopKeeper.DEAL_DESCS[r]+"\n\r");
	            r=ShopKeeper.DEAL_CLANPOSTMAN;
	            c=codeStr.charAt(r);
	            codes.append(c);
	            buf.append(c+") "+ShopKeeper.DEAL_DESCS[r]+"\n\r");
	        }
	        else
	        if(E instanceof Auctioneer)
	        {
	            int r=ShopKeeper.DEAL_AUCTIONEER;
	            char c=codeStr.charAt(r);
	            codes.append(c);
	            buf.append(c+") "+ShopKeeper.DEAL_DESCS[r]+"\n\r");
	            r=ShopKeeper.DEAL_AUCTIONEER;
	            c=codeStr.charAt(r);
	            codes.append(c);
	            buf.append(c+") "+ShopKeeper.DEAL_DESCS[r]+"\n\r");
	        }
	        else
	        for(int r=0;r<ShopKeeper.DEAL_DESCS.length;r++)
	        {
	            if((r!=ShopKeeper.DEAL_CLANBANKER)
	            &&(r!=ShopKeeper.DEAL_BANKER)
	            &&(r!=ShopKeeper.DEAL_CLANPOSTMAN)
	            &&(r!=ShopKeeper.DEAL_POSTMAN))
	            {
	                char c=codeStr.charAt(r);
	                codes.append(c);
	                buf.append(c+") "+ShopKeeper.DEAL_DESCS[r]+"\n\r");
	            }
	        }
	        String newType=mob.session().choose(buf.toString()+"Enter a value to toggle on/off: ",codes.toString(),"");
	        int newValue=-1;
	        if(newType.trim().length()==0)
	        {
	        	if(E.getWhatIsSoldMask()==oldMask)
	        		mob.tell("(no change");
	        	return;
	        }
	        if(newType.length()>0)
	            newValue=codeStr.indexOf(newType.toUpperCase());
	        if(newValue<=0)
	        	E.setWhatIsSoldMask(0);
	        else
	        if(E.isSold(newValue))
	        {
	        	E.addSoldType(-newValue);
	            Vector V=E.getShop().getStoreInventory();
	            for(int b=0;b<V.size();b++)
	                if(!E.doISellThis((Environmental)V.elementAt(b)))
	                    E.getShop().delAllStoreInventory((Environmental)V.elementAt(b));
	        }
	        else
	            E.addSoldType(newValue);
        }
    }

    protected void genShopkeeper2(MOB mob, ShopKeeper E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String itemstr="NO";
        while(itemstr.length()>0)
        {
            String inventorystr="";
            Vector V=E.getShop().getStoreInventory();
            for(int b=0;b<V.size();b++)
            {
                Environmental E2=(Environmental)V.elementAt(b);
                if(E2.isGeneric())
                    inventorystr+=E2.name()+" ("+E.getShop().numberInStock(E2)+"), ";
                else
                    inventorystr+=CMClass.classID(E2)+" ("+E.getShop().numberInStock(E2)+"), ";
            }
            if(inventorystr.length()>0)
                inventorystr=inventorystr.substring(0,inventorystr.length()-2);
            mob.tell(showNumber+". Inventory: '"+inventorystr+"'.");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            itemstr=mob.session().prompt("Enter something to add/remove (?)\n\r:","");
            if(itemstr.length()>0)
            {
                if(itemstr.equalsIgnoreCase("?"))
                {
                    mob.tell(CMLib.lister().reallyList(CMClass.abilities(),-1).toString());
                    mob.tell(CMLib.lister().reallyList(CMClass.armor(),-1).toString());
                    mob.tell(CMLib.lister().reallyList(CMClass.weapons(),-1).toString());
                    mob.tell(CMLib.lister().reallyList(CMClass.miscMagic(),-1).toString());
                    mob.tell(CMLib.lister().reallyList(CMClass.miscTech(),-1).toString());
                    mob.tell(CMLib.lister().reallyList(CMClass.clanItems(),-1).toString());
                    mob.tell(CMLib.lister().reallyList(CMClass.basicItems(),-1).toString());
                    mob.tell(CMLib.lister().reallyList(CMClass.mobTypes(),-1).toString());
                    mob.tell("* Plus! Any items on the ground.");
                    mob.tell("* Plus! Any mobs hanging around in the room.");
                }
                else
                {
                    Environmental item=E.getShop().getStock(itemstr,null);
                    if(item!=null)
                    {
                        mob.tell(item.ID()+" removed.");
                        E.getShop().delAllStoreInventory((Environmental)item.copyOf());
                    }
                    else
                    {
                        item=CMClass.getUnknown(itemstr);
                        if((item==null)&&(mob.location()!=null))
                        {
                            Room R=mob.location();
                            item=R.fetchItem(null,itemstr);
                            if(item==null)
                            {
                                item=R.fetchInhabitant(itemstr);
                                if((item instanceof MOB)&&(!((MOB)item).isMonster()))
                                    item=null;
                            }
                        }
                        if((item!=null)&&((!(item instanceof ArchonOnly))||(CMSecurity.isASysOp(mob))))
                        {
                            item=(Environmental)item.copyOf();
                            item.recoverEnvStats();
                            boolean ok=E.doISellThis(item);
                            if((item instanceof Ability)
                               &&((E.isSold(ShopKeeper.DEAL_TRAINER))||(E.isSold(ShopKeeper.DEAL_CASTER))))
                                ok=true;
                            else
                            if(E.isSold(ShopKeeper.DEAL_INVENTORYONLY))
                                ok=true;
                            if(!ok)
                            {
                                mob.tell("The shopkeeper does not sell that.");
                            }
                            else
                            {
                                boolean alreadyHasIt=false;

                                if(E.getShop().doIHaveThisInStock(item.Name(),null))
                                   alreadyHasIt=true;

                                if(!alreadyHasIt)
                                {
                                    mob.tell(item.ID()+" added.");
                                    int num=1;
                                    if(!(item instanceof Ability))
                                        num=CMath.s_int(mob.session().prompt("How many? :",""));
                                    int price=CMath.s_int(mob.session().prompt("At what price? :",""));
                                    E.getShop().addStoreInventory(item,num,price);
                                }
                            }
                        }
                        else
                        {
                            mob.tell("'"+itemstr+"' is not recognized.  Try '?'.");
                        }
                    }
                }
            }
            else
                mob.tell("(no change)");
        }
    }
    protected void genEconomics1(MOB mob, Economics E, int showNumber, int showFlag) throws IOException
    { E.setPrejudiceFactors(prompt(mob,E.prejudiceFactors(),showNumber,showFlag,"Prejudice",true,false)); }

    protected void genEconomics2(MOB mob, Economics E, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String header=showNumber+". Item Pricing Factors: ";
        String[] prics=E.itemPricingAdjustments();
        if((showFlag!=showNumber)&&(showFlag>-999))
        {
            if(prics.length<1)
                mob.tell(header+"''.");
            else
            if(prics.length==1)
                mob.tell(header+"'"+prics[0]+"'.");
            else
                mob.tell(header+prics.length+" defined..");
            return;
        }
        String behave="NO";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
        {
            mob.tell(header+"\n\r");
            for(int p=0;p<prics.length;p++)
                mob.tell(CMStrings.SPACES.substring(0,header.length()-3)
                        +(p+1)+") "+prics[p]+"\n\r");
            String newValue=mob.session().prompt("Enter # to remove, or A to add:\n\r:","");
            if(CMath.isInteger(newValue))
            {
                int x=CMath.s_int(newValue);
                if((x>0)&&(x<=prics.length))
                {
                    String[] newPrics=new String[prics.length-1];
                    int y=0;
                    for(int i=0;i<prics.length;i++)
                        if(i!=(x-1))
                            newPrics[y++]=prics[i];
                    prics=newPrics;
                }
            }
            else
            if(newValue.toUpperCase().startsWith("A"))
            {
                double dbl=CMath.s_double(mob.session().prompt("Enter a price multiplier between 0.0 and X.Y\n\r: "));
                String mask="?";
                while(mask.equals("?"))
                {
                    mask=mob.session().prompt("Now enter a mask that describes the item (? for syntax)\n\r: ");
                    if(mask.equals("?"))
                        mob.tell(CMLib.masking().maskHelp("\n\r","disallow"));
                }
                String[] newPrics=new String[prics.length+1];
                for(int i=0;i<prics.length;i++)
                    newPrics[i]=prics[i];
                newPrics[prics.length]=dbl+" "+mask;
                prics=newPrics;
            }
            else
            {
                mob.tell("(no change)");
                break;
            }
        }
        E.setItemPricingAdjustments(prics);
    }

    protected void genAreaBlurbs(MOB mob, Area E, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String header=showNumber+". Area Blurb Flags: ";
        if((showFlag!=showNumber)&&(showFlag>-999))
        {
            int numFlags=E.numBlurbFlags();
            if(numFlags<1)
                mob.tell(header+"''.");
            else
            if(numFlags==1)
                mob.tell(header+"'"+E.getBlurbFlag(0)+": "+E.getBlurbFlag(E.getBlurbFlag(0))+"'.");
            else
                mob.tell(header+numFlags+" defined..");
            return;
        }
        String behave="NO";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
        {
            mob.tell(header+"\n\r");
            for(int p=0;p<E.numBlurbFlags();p++)
                mob.tell((E.getBlurbFlag(p))+": "+E.getBlurbFlag(E.getBlurbFlag(p)));
            String newValue=mob.session().prompt("Enter flag to remove, or A to add:\n\r:","");
            if(E.getBlurbFlag(newValue.toUpperCase().trim())!=null)
            {
                E.delBlurbFlag(newValue.toUpperCase().trim());
                mob.tell(newValue.toUpperCase().trim()+" removed");
            }
            else
            if(newValue.toUpperCase().equals("A"))
            {
                String flag=mob.session().prompt("Enter a new flag: ");
                if(flag.trim().length()==0) continue;
                String desc=mob.session().prompt("Enter a flag blurb (or nothing): ");
                E.addBlurbFlag((flag.toUpperCase().trim()+" "+desc).trim());
                mob.tell(flag.toUpperCase().trim()+" added");
            }
            else
            if(newValue.length()==0)
            {
                mob.tell("(no change)");
                break;
            }
        }
    }

    protected void genEconomics3(MOB mob, Economics E, int showNumber, int showFlag) throws IOException
    { E.setBudget(prompt(mob,E.budget(),showNumber,showFlag,"Budget",true,false)); }

    protected void genEconomics4(MOB mob, Economics E, int showNumber, int showFlag) throws IOException
    { E.setDevalueRate(prompt(mob,E.devalueRate(),showNumber,showFlag,"Devaluation rate(s)",true,false)); }

    protected void genEconomics5(MOB mob, Economics E, int showNumber, int showFlag) throws IOException
    { E.setInvResetRate(prompt(mob,E.invResetRate(),showNumber,showFlag,"Inventory reset rate [ticks]")); }

    protected void genEconomics6(MOB mob, Economics E, int showNumber, int showFlag) throws IOException
    { E.setIgnoreMask(prompt(mob,E.ignoreMask(),showNumber,showFlag,"Ignore Mask",true,false)); }

    protected void genAbilities(MOB mob, MOB E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String behave="NO";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
        {
            String abilitiestr="";
            for(int a=0;a<E.numLearnedAbilities();a++)
            {
                Ability A=E.fetchAbility(a);
                if((A!=null)&&(A.savable()))
                    abilitiestr+=A.ID()+", ";
            }
            if(abilitiestr.length()>0)
                abilitiestr=abilitiestr.substring(0,abilitiestr.length()-2);
            if((abilitiestr.length()>60)&&((showFlag!=showNumber)&&(showFlag>-999)))
                abilitiestr=abilitiestr.substring(0,60)+"...";
            mob.tell(showNumber+". Abilities: '"+abilitiestr+"'.");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            behave=mob.session().prompt("Enter an ability to add/remove (?)\n\r:","");
            if(behave.length()>0)
            {
                if(behave.equalsIgnoreCase("?"))
                    mob.tell(CMLib.lister().reallyList(CMClass.abilities(),-1).toString());
                else
                {
                    Ability chosenOne=null;
                    for(int a=0;a<E.numLearnedAbilities();a++)
                    {
                        Ability A=E.fetchAbility(a);
                        if((A!=null)&&(A.ID().equalsIgnoreCase(behave)))
                            chosenOne=A;
                    }
                    if(chosenOne!=null)
                    {
                        mob.tell(chosenOne.ID()+" removed.");
                        E.delAbility(chosenOne);
                        if(E.fetchEffect(chosenOne.ID())!=null)
                            E.delEffect(E.fetchEffect(chosenOne.ID()));
                    }
                    else
                    {
                        chosenOne=CMClass.getAbility(behave);
                        if(chosenOne!=null)
                        {
                            boolean alreadyHasIt=(E.fetchAbility(chosenOne.ID())!=null);
                            if(!alreadyHasIt)
                                mob.tell(chosenOne.ID()+" added.");
                            else
                                mob.tell(chosenOne.ID()+" re-added.");
                            if(!alreadyHasIt)
                            {
                                chosenOne=(Ability)chosenOne.copyOf();
                                E.addAbility(chosenOne);
                                chosenOne.setProficiency(50);
                                chosenOne.autoInvocation(mob);
                            }
                        }
                        else
                        {
                            mob.tell("'"+behave+"' is not recognized.  Try '?'.");
                        }
                    }
                }
            }
            else
                mob.tell("(no change)");
        }
    }

    public void spells(MOB mob, Vector V, int showNumber, int showFlag, boolean inParms) throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String behave="NO";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
        {
            String affectstr="";
            for(int b=0;b<V.size();b++)
            {
                Ability A=(Ability)V.elementAt(b);
                if((A!=null)&&(A.savable()))
                {
                    affectstr+=A.ID();
                    if(A.text().trim().length()>0)
                        affectstr+="("+A.text().trim()+"), ";
                    else
                        affectstr+=", ";
                }

            }
            if(affectstr.length()>0)
                affectstr=affectstr.substring(0,affectstr.length()-2);
            mob.tell(showNumber+". Effects: '"+affectstr+"'.");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            behave=mob.session().prompt("Enter a spell to add/remove (?)\n\r:","");
            if(behave.length()>0)
            {
                if(behave.equalsIgnoreCase("?"))
                    mob.tell(CMLib.lister().reallyList(CMClass.abilities(),-1).toString());
                else
                {
                    Ability chosenOne=null;
                    for(int a=0;a<V.size();a++)
                    {
                        Ability A=(Ability)V.elementAt(a);
                        if((A!=null)&&(A.ID().equalsIgnoreCase(behave)))
                            chosenOne=A;
                    }
                    if(chosenOne!=null)
                    {
                        mob.tell(chosenOne.ID()+" removed.");
                        V.remove(chosenOne);
                    }
                    else
                    {
                        chosenOne=CMClass.getAbility(behave);
                        if(chosenOne!=null)
                        {
                            if(inParms)
                            {
                                String parms="?";
                                while(parms.equals("?"))
                                {
                                    parms=chosenOne.text();
                                    parms=mob.session().prompt("Enter any effect parameters (?)\n\r:"+parms);
                                    if(parms.equals("?")){ StringBuilder s2=CMLib.help().getHelpText(chosenOne.ID(),mob,true); if(s2!=null) mob.tell(s2.toString()); else mob.tell("no help!");}
                                }
                                chosenOne.setMiscText(parms.trim());
                            }
                            mob.tell(chosenOne.ID()+" added.");
                            V.addElement(chosenOne);
                        }
                        else
                        {
                            mob.tell("'"+behave+"' is not recognized.  Try '?'.");
                        }
                    }
                }
            }
            else
                mob.tell("(no change)");
        }
    }
    
    protected void genClanMembers(MOB mob, Clan E, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String behave="NO";
        Vector<MemberRecord> members=E.getMemberList();
        List<MemberRecord> membersCopy=(List<MemberRecord>)members.clone();
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
        {
            String memberStr="";
            for(Clan.MemberRecord member : members)
                memberStr+=member.name+" ("+CMLib.clans().getRoleName(E.getGovernment(),member.role,true,false)+"), ";
            if(memberStr.length()>0)
                memberStr=memberStr.substring(0,memberStr.length()-2);
            mob.tell(showNumber+". Clan Members : '"+memberStr+"'.");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            behave=mob.session().prompt("Enter a name to add/remove\n\r:","");
            if(behave.length()>0)
            {
                int chosenOne=-1;
                for(int m=0;m<members.size();m++)
                    if(behave.equalsIgnoreCase(members.elementAt(m).name))
                        chosenOne=m;
                if(chosenOne>=0)
                {
                    mob.tell(members.elementAt(chosenOne).name+" removed.");
                    members.removeElementAt(chosenOne);
                }
                else
                {
                    MOB M=CMLib.players().getLoadPlayer(behave);
                    if(M!=null)
                    {
                        int oldNum=-1;
                        for(int m=0;m<membersCopy.size();m++)
                            if(behave.equalsIgnoreCase(membersCopy.get(m).name))
                            {
                                oldNum=m;
                                members.addElement(membersCopy.get(m));
                                break;
                            }
                        int index=oldNum;
                        if(index<0)
                        {
                            index=members.size();
                            members.addElement(new MemberRecord(M.name(),Clan.POS_MEMBER,M.playerStats().lastDateTime()));
                        }

                        int newRole=-1;
                        while((mob.session()!=null)&&(!mob.session().killFlag())&&(newRole<0))
                        {
                            String newRoleStr=mob.session().prompt("Enter this members role (?) '"+CMLib.clans().getRoleName(E.getGovernment(),members.get(index).role,true,false)+"': ","");
                            StringBuffer roles=new StringBuffer();
                            for(int i=0;i<Clan.ROL_DESCS[E.getGovernment()].length;i++)
                            {
                                roles.append(Clan.ROL_DESCS[E.getGovernment()][i]+", ");
                                if(newRoleStr.equalsIgnoreCase(Clan.ROL_DESCS[E.getGovernment()][i]))
                                    newRole=Clan.POSORDER[i];
                            }
                            roles=new StringBuffer(roles.substring(0,roles.length()-2));
                            if(newRole<0)
                                mob.tell("That role is invalid.  Valid roles include: "+roles.toString());
                            else
                                break;
                        }
                        if(oldNum<0)
                            mob.tell(M.Name()+" added.");
                        else
                            mob.tell(M.Name()+" re-added.");
                        members.elementAt(index).role=newRole;
                    }
                    else
                    {
                        mob.tell("'"+behave+"' is an unrecognized player name.");
                    }
                }
                // first add missing ones
                for(int m=0;m<members.size();m++)
                {
                    String newName=members.get(m).name;
                    if(!membersCopy.contains(newName))
                    {
                        MOB M=CMLib.players().getLoadPlayer(newName);
                        if(M!=null)
                        {
                            Clan oldC=CMLib.clans().getClan(M.getClanID());
                            if((oldC!=null)
                            &&(!M.getClanID().equalsIgnoreCase(E.clanID())))
                            {
                                M.setClanID("");
                                M.setClanRole(Clan.POS_APPLICANT);
                                oldC.updateClanPrivileges(M);
                            }
                            int role=members.get(m).role;
                            CMLib.database().DBUpdateClanMembership(M.Name(), E.clanID(), role);
                            M.setClanID(E.clanID());
                            M.setClanRole(role);
                            E.updateClanPrivileges(M);
                        }
                    }
                }
                // now adjust changed roles
                for(int m=0;m<members.size();m++)
                {
                    String newName=members.get(m).name;
                    if(membersCopy.contains(newName))
                    {
                        MOB M=CMLib.players().getLoadPlayer(newName);
                        int newRole=members.get(m).role;
                        if((M!=null)&&(newRole!=M.getClanRole()))
                        {
                            CMLib.database().DBUpdateClanMembership(M.Name(), E.clanID(), newRole);
                            M.setClanRole(newRole);
                            E.updateClanPrivileges(M);
                        }
                    }
                }
                // now remove old members
                for(int m=0;m<membersCopy.size();m++)
                {
                    String newName=membersCopy.get(m).name;
                    if(!members.contains(newName))
                    {
                        MOB M=CMLib.players().getLoadPlayer(newName);
                        if(M!=null)
                        {
                            M.setClanID("");
                            M.setClanRole(Clan.POS_APPLICANT);
                            E.updateClanPrivileges(M);
                        }
                    }
                }
            }
            else
                mob.tell("(no change)");
        }
    }

    protected void genDeity1(MOB mob, Deity E, int showNumber, int showFlag) throws IOException
    { E.setClericRequirements(prompt(mob,E.getClericRequirements(),showNumber,showFlag,"Cleric Requirements",false,false)); }

    protected void genDeity2(MOB mob, Deity E, int showNumber, int showFlag) throws IOException
    { E.setClericRitual(prompt(mob,E.getClericRitual(),showNumber,showFlag,"Cleric Ritual",false,false)); }

    protected void genDeity3(MOB mob, Deity E, int showNumber, int showFlag) throws IOException
    { E.setWorshipRequirements(prompt(mob,E.getWorshipRequirements(),showNumber,showFlag,"Worshiper Requirements",false,false)); }

    protected void genDeity4(MOB mob, Deity E, int showNumber, int showFlag) throws IOException
    { E.setWorshipRitual(prompt(mob,E.getWorshipRitual(),showNumber,showFlag,"Worshiper Ritual",false,false)); }

    protected void genDeity5(MOB mob, Deity E, int showNumber, int showFlag) throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String behave="NO";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
        {
            String abilitiestr="";
            for(int a=0;a<E.numBlessings();a++)
            {
                Ability A=E.fetchBlessing(a);
                if((A!=null)&&(A.savable()))
                    abilitiestr+=A.ID()+", ";
            }
            if(abilitiestr.length()>0)
                abilitiestr=abilitiestr.substring(0,abilitiestr.length()-2);
            mob.tell(showNumber+". Blessings: '"+abilitiestr+"'.");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            behave=mob.session().prompt("Enter an ability to add/remove (?)\n\r:","");
            if(behave.length()>0)
            {
                if(behave.equalsIgnoreCase("?"))
                    mob.tell(CMLib.lister().reallyList(CMClass.abilities(),-1).toString());
                else
                {
                    Ability chosenOne=null;
                    for(int a=0;a<E.numBlessings();a++)
                    {
                        Ability A=E.fetchBlessing(a);
                        if((A!=null)&&(A.ID().equalsIgnoreCase(behave)))
                            chosenOne=A;
                    }
                    if(chosenOne!=null)
                    {
                        mob.tell(chosenOne.ID()+" removed.");
                        E.delBlessing(chosenOne);
                    }
                    else
                    {
                        chosenOne=CMClass.getAbility(behave);
                        if(chosenOne!=null)
                        {
                            boolean alreadyHasIt=false;
                            for(int a=0;a<E.numBlessings();a++)
                            {
                                Ability A=E.fetchBlessing(a);
                                if((A!=null)&&(A.ID().equals(chosenOne.ID())))
                                    alreadyHasIt=true;
                            }
                            boolean clericOnly=mob.session().confirm("Is this for clerics only (y/N)?","N");
                            if(!alreadyHasIt)
                                mob.tell(chosenOne.ID()+" added.");
                            else
                                mob.tell(chosenOne.ID()+" re-added.");
                            if(!alreadyHasIt)
                                E.addBlessing((Ability)chosenOne.copyOf(),clericOnly);
                        }
                        else
                        {
                            mob.tell("'"+behave+"' is not recognized.  Try '?'.");
                        }
                    }
                }
            }
            else
                mob.tell("(no change)");
        }
    }

    protected void genDeity6(MOB mob, Deity E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String behave="NO";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
        {
            String abilitiestr="";
            for(int a=0;a<E.numCurses();a++)
            {
                Ability A=E.fetchCurse(a);
                if((A!=null)&&(A.savable()))
                    abilitiestr+=A.ID()+", ";
            }
            if(abilitiestr.length()>0)
                abilitiestr=abilitiestr.substring(0,abilitiestr.length()-2);
            mob.tell(showNumber+". Curses: '"+abilitiestr+"'.");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            behave=mob.session().prompt("Enter an ability to add/remove (?)\n\r:","");
            if(behave.length()>0)
            {
                if(behave.equalsIgnoreCase("?"))
                    mob.tell(CMLib.lister().reallyList(CMClass.abilities(),-1).toString());
                else
                {
                    Ability chosenOne=null;
                    for(int a=0;a<E.numCurses();a++)
                    {
                        Ability A=E.fetchCurse(a);
                        if((A!=null)&&(A.ID().equalsIgnoreCase(behave)))
                            chosenOne=A;
                    }
                    if(chosenOne!=null)
                    {
                        mob.tell(chosenOne.ID()+" removed.");
                        E.delCurse(chosenOne);
                    }
                    else
                    {
                        chosenOne=CMClass.getAbility(behave);
                        if(chosenOne!=null)
                        {
                            boolean alreadyHasIt=false;
                            for(int a=0;a<E.numCurses();a++)
                            {
                                Ability A=E.fetchCurse(a);
                                if((A!=null)&&(A.ID().equals(chosenOne.ID())))
                                    alreadyHasIt=true;
                            }
                            boolean clericOnly=mob.session().confirm("Is this for clerics only (y/N)?","N");
                            if(!alreadyHasIt)
                                mob.tell(chosenOne.ID()+" added.");
                            else
                                mob.tell(chosenOne.ID()+" re-added.");
                            if(!alreadyHasIt)
                                E.addCurse((Ability)chosenOne.copyOf(),clericOnly);
                        }
                        else
                        {
                            mob.tell("'"+behave+"' is not recognized.  Try '?'.");
                        }
                    }
                }
            }
            else
                mob.tell("(no change)");
        }
    }

    protected void genDeity7(MOB mob, Deity E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String behave="NO";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
        {
            String abilitiestr="";
            for(int a=0;a<E.numPowers();a++)
            {
                Ability A=E.fetchPower(a);
                if((A!=null)&&(A.savable()))
                    abilitiestr+=A.ID()+", ";
            }
            if(abilitiestr.length()>0)
                abilitiestr=abilitiestr.substring(0,abilitiestr.length()-2);
            mob.tell(showNumber+". Granted Powers: '"+abilitiestr+"'.");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            behave=mob.session().prompt("Enter an ability to add/remove (?)\n\r:","");
            if(behave.length()>0)
            {
                if(behave.equalsIgnoreCase("?"))
                    mob.tell(CMLib.lister().reallyList(CMClass.abilities(),-1).toString());
                else
                {
                    Ability chosenOne=null;
                    for(int a=0;a<E.numPowers();a++)
                    {
                        Ability A=E.fetchPower(a);
                        if((A!=null)&&(A.ID().equalsIgnoreCase(behave)))
                            chosenOne=A;
                    }
                    if(chosenOne!=null)
                    {
                        mob.tell(chosenOne.ID()+" removed.");
                        E.delPower(chosenOne);
                    }
                    else
                    {
                        chosenOne=CMClass.getAbility(behave);
                        if(chosenOne!=null)
                        {
                            boolean alreadyHasIt=false;
                            for(int a=0;a<E.numPowers();a++)
                            {
                                Ability A=E.fetchPower(a);
                                if((A!=null)&&(A.ID().equals(chosenOne.ID())))
                                    alreadyHasIt=true;
                            }
                            if(!alreadyHasIt)
                                mob.tell(chosenOne.ID()+" added.");
                            else
                                mob.tell(chosenOne.ID()+" re-added.");
                            if(!alreadyHasIt)
                                E.addPower((Ability)chosenOne.copyOf());
                        }
                        else
                        {
                            mob.tell("'"+behave+"' is not recognized.  Try '?'.");
                        }
                    }
                }
            }
            else
                mob.tell("(no change)");
        }
    }
    protected void genDeity8(MOB mob, Deity E, int showNumber, int showFlag) throws IOException
    { E.setClericSin(prompt(mob,E.getClericSin(),showNumber,showFlag,"Cleric Sin",false,false)); }

    protected void genDeity9(MOB mob, Deity E, int showNumber, int showFlag) throws IOException
    { E.setWorshipSin(prompt(mob,E.getWorshipSin(),showNumber,showFlag,"Worshiper Sin",false,false)); }

    protected void genDeity0(MOB mob, Deity E, int showNumber, int showFlag) throws IOException
    { E.setClericPowerup(prompt(mob,E.getClericPowerup(),showNumber,showFlag,"Cleric Power Ritual",false,false)); }

    protected void genDeity11(MOB mob, Deity E, int showNumber, int showFlag) throws IOException
    { E.setServiceRitual(prompt(mob,E.getServiceRitual(),showNumber,showFlag,"Service Ritual",false,false)); }

    protected void genGridLocaleX(MOB mob, GridZones E, int showNumber, int showFlag) throws IOException
    { E.setXGridSize(prompt(mob,E.xGridSize(),showNumber,showFlag,"Size (X)")); }

    protected void genGridLocaleY(MOB mob, GridZones E, int showNumber, int showFlag) throws IOException
    { E.setYGridSize(prompt(mob,E.yGridSize(),showNumber,showFlag,"Size (Y)")); }

    public void wornLocation(MOB mob, long[] oldWornLocation, boolean[] logicalAnd, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        if((showFlag!=showNumber)&&(showFlag>-999))
        {
            StringBuffer buf=new StringBuffer(showNumber+". ");
            if(!logicalAnd[0])
                buf.append("Wear on any one of: ");
            else
                buf.append("Worn on all of: ");
    		Wearable.CODES codes = Wearable.CODES.instance();
            for(int l=1;l<codes.all().length;l++)
            {
                long wornCode=codes.all()[l];
                if((oldWornLocation[0]&wornCode)>0)
                    buf.append(codes.name(l)+", ");
            }
            if(buf.toString().endsWith(", "))
                mob.tell(buf.substring(0,buf.length()-2));
            else
                mob.tell(buf.toString());
            return;
        }
        int codeVal=-1;
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(codeVal!=0))
        {
            mob.tell("Wearing parameters\n\r0: Done");
            if(!logicalAnd[0])
                mob.tell("1: Able to wear on any ONE of these locations:");
            else
                mob.tell("1: Must be worn on ALL of these locations:");
    		Wearable.CODES codes = Wearable.CODES.instance();
            for(int l=0;l<codes.total();l++)
            {
                long wornCode=codes.get(l);
                if(codes.name(wornCode).length()>0)
                {
                    String header=(l+2)+": ("+codes.name(wornCode)+") : "+(((oldWornLocation[0]&wornCode)==wornCode)?"YES":"NO");
                    mob.tell(header);
                }
            }
            codeVal=CMath.s_int(mob.session().prompt("Select an option number above to TOGGLE\n\r: "));
            if(codeVal>0)
            {
                if(codeVal==1)
                    logicalAnd[0]=!logicalAnd[0];
                else
                {
                    long wornCode=codes.get(codeVal-2);
                    if((oldWornLocation[0]&wornCode)==wornCode)
                        oldWornLocation[0]=(oldWornLocation[0]-wornCode);
                    else
                        oldWornLocation[0]=(oldWornLocation[0]|wornCode);
                }
            }
        }
    }
    
    protected void genWornLocation(MOB mob, Item E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        long[] wornLoc = new long[]{E.rawProperLocationBitmap()};
        boolean[] logicalAnd = new boolean[]{E.rawLogicalAnd()};
        wornLocation(mob,wornLoc,logicalAnd,showNumber,showFlag);
        E.setRawProperLocationBitmap(wornLoc[0]);
        E.setRawLogicalAnd(logicalAnd[0]);
    }

    protected void genThirstQuenched(MOB mob, Drink E, int showNumber, int showFlag) throws IOException
    { E.setThirstQuenched(prompt(mob,E.thirstQuenched(),showNumber,showFlag,"Quenched/Drink")); }

    protected void genDrinkHeld(MOB mob, Drink E, int showNumber, int showFlag) throws IOException
    {
        E.setLiquidHeld(prompt(mob,E.liquidHeld(),showNumber,showFlag,"Amount of Drink Held"));
        E.setLiquidRemaining(E.liquidHeld());
    }

    protected void genAttackAttribute(MOB mob, CharClass E, int showNumber, int showFlag, String FieldDisp, String Field)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". "+FieldDisp+": '"+CharStats.CODES.DESC(CMath.s_int(E.getStat(Field)))+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newName=mob.session().prompt("Enter a new one\n\r:","");
        String newStat="";
		for(int i : CharStats.CODES.BASE())
            if(newName.equalsIgnoreCase(CharStats.CODES.DESC(i)))
                newStat=""+i;
        if(newStat.length()>0)
            E.setStat(Field,newStat);
        else
            mob.tell("(no change)");
    }
    protected void genArmorCode(MOB mob, CharClass E, int showNumber, int showFlag, String FieldDisp, String Field)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". "+FieldDisp+": '"+CharClass.ARMOR_LONGDESC[CMath.s_int(E.getStat(Field))]+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newName=mob.session().prompt("Enter ("+CMParms.toStringList(CharClass.ARMOR_DESCS)+")\n\r:","");
        String newStat="";
        for(int i=0;i<CharClass.ARMOR_DESCS.length;i++)
            if(newName.equalsIgnoreCase(CharClass.ARMOR_DESCS[i]))
                newStat=""+i;
        if(newStat.length()>0)
            E.setStat(Field,newStat);
        else
            mob.tell("(no change)");
    }
    protected void genQualifications(MOB mob, CharClass E, int showNumber, int showFlag, String FieldDisp, String Field)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". "+FieldDisp+": '"+CMLib.masking().maskDesc(E.getStat(Field))+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newName="?";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(newName.equals("?")))
        {
            newName=mob.session().prompt("Enter a new mask (?)\n\r:","");
            if(newName.equals("?"))
                mob.tell(CMLib.masking().maskHelp("\n","disallow"));
        }
        if((newName.length()>0)&&(!newName.equals("?")))
            E.setStat(Field,newName);
        else
            mob.tell("(no change)");
    }
    protected void genClanAccept(MOB mob, Clan E, int showNumber, int showFlag) throws IOException
    { E.setAcceptanceSettings(prompt(mob,E.getAcceptanceSettings(),showNumber,showFlag,"Clan Qualifications",false,false,CMLib.masking().maskHelp("\n","disallow"))); }

    protected void genWeaponRestr(MOB mob, CharClass E, int showNumber, int showFlag, String FieldDisp, String FieldNum, String Field)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        Vector set=CMParms.parseCommas(E.getStat(Field),true);
        StringBuffer str=new StringBuffer("");
        for(int v=0;v<set.size();v++)
            str.append(" "+Weapon.CLASS_DESCS[CMath.s_int((String)set.elementAt(v))].toLowerCase());

        mob.tell(showNumber+". "+FieldDisp+": '"+str.toString()+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newName="?";
        boolean setChanged=false;
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(newName.equals("?")))
        {
            newName=mob.session().prompt("Enter a weapon class to add/remove (?)\n\r:","");
            if(newName.equals("?"))
                mob.tell(CMParms.toStringList(Weapon.CLASS_DESCS));
            else
            if(newName.length()>0)
            {
                int foundCode=-1;
                for(int i=0;i<Weapon.CLASS_DESCS.length;i++)
                    if(Weapon.CLASS_DESCS[i].equalsIgnoreCase(newName))
                        foundCode=i;
                if(foundCode<0)
                {
                    mob.tell("'"+newName+"' is not recognized.  Try '?'.");
                    newName="?";
                }
                else
                {
                    int x=set.indexOf(""+foundCode);
                    if(x>=0)
                    {
                        setChanged=true;
                        set.removeElementAt(x);
                        mob.tell("'"+newName+"' removed.");
                        newName="?";
                    }
                    else
                    {
                        set.addElement(""+foundCode);
                        setChanged=true;
                        mob.tell("'"+newName+"' added.");
                        newName="?";
                    }
                }
            }
        }
        if(setChanged)
            E.setStat(Field,CMParms.toStringList(set));
        else
            mob.tell("(no change)");
    }

    protected void genWeaponMaterials(MOB mob, CharClass E, int showNumber, int showFlag, String FieldDisp, String FieldNum, String Field)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        Vector set=CMParms.parseCommas(E.getStat(Field),true);
        StringBuffer str=new StringBuffer("");
        for(int v=0;v<set.size();v++)
            str.append(" "+CMLib.materials().getMaterialDesc(CMath.s_int((String)set.elementAt(v))));

        mob.tell(showNumber+". "+FieldDisp+": '"+str.toString()+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newName="?";
        boolean setChanged=false;
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(newName.equals("?")))
        {
            newName=mob.session().prompt("Enter a material type to add/remove to requirements (?)\n\r:","");
            if(newName.equals("?"))
                mob.tell(CMParms.toStringList(RawMaterial.MATERIAL_DESCS));
            else
            if(newName.length()>0)
            {
                int foundCode=CMLib.materials().getMaterialCode(newName,true);
                if(foundCode<0) foundCode=CMLib.materials().getMaterialCode(newName,false);
                if(foundCode<0)
                {
                    mob.tell("'"+newName+"' is not recognized.  Try '?'.");
                    newName="?";
                }
                else
                {
                    int x=set.indexOf(""+foundCode);
                    if(x>=0)
                    {
                        setChanged=true;
                        set.removeElementAt(x);
                        mob.tell("'"+newName+"' removed.");
                        newName="?";
                    }
                    else
                    {
                        set.addElement(""+foundCode);
                        setChanged=true;
                        mob.tell("'"+newName+"' added.");
                        newName="?";
                    }
                }
            }
        }
        if(setChanged)
            E.setStat(Field,CMParms.toStringList(set));
        else
            mob.tell("(no change)");
    }



    protected void genDisableFlags(MOB mob, Race E, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        int flags=CMath.s_int(E.getStat("DISFLAGS"));
        String newName="?";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(newName.equals("?")))
        {
            StringBuffer disabled=new StringBuffer("");
            for(int i=0;i<Race.GENFLAG_DESCS.length;i++)
                if(CMath.isSet(flags,i))
                    disabled.append(Race.GENFLAG_DESCS[i]);

            mob.tell(showNumber+". Disabled: '"+disabled+"'.");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;

            newName=mob.session().prompt("Enter flag to toggle (?)\n\r:","").toUpperCase();
            if(newName.length()==0)
                mob.tell("(no change)");
            else
            if(CMParms.contains(Race.GENFLAG_DESCS,newName))
            {
                int bit=CMParms.indexOf(Race.GENFLAG_DESCS,newName);
                if(CMath.isSet(flags,bit))
                    flags=flags-(int)CMath.pow(2,bit);
                else
                    flags=flags+(int)CMath.pow(2,bit);
            }
            else
            if(newName.equalsIgnoreCase("?"))
            {
                StringBuffer str=new StringBuffer("Valid values: \n\r");
                for(int i=0;i<Race.GENFLAG_DESCS.length;i++)
                    str.append(Race.GENFLAG_DESCS[i]+"\n\r");
                mob.tell(str.toString());
            }
            else
                mob.tell("(no change)");
        }
        E.setStat("DISFLAGS",""+flags);
    }

    protected void genRaceWearFlags(MOB mob, Race E, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        int flags=CMath.s_int(E.getStat("WEAR"));
        String newName="?";
		Wearable.CODES codes = Wearable.CODES.instance();
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(newName.equals("?")))
        {
            StringBuffer wearable=new StringBuffer("");
            for(int i=1;i<codes.total();i++)
                if(CMath.isSet(flags,i-1))
                    wearable.append(codes.name(i)+" ");

            mob.tell(showNumber+". UNWearable locations: '"+wearable+"'.");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;

            newName=mob.session().prompt("Enter a location to toggle (?)\n\r:","").toUpperCase();
            if(newName.length()==0)
                mob.tell("(no change)");
            else
            if(CMParms.containsIgnoreCase(codes.names(),newName))
            {
                int bit=CMParms.indexOfIgnoreCase(codes.names(),newName)-1;
                if(bit>=0)
                {
                    if(CMath.isSet(flags,bit))
                        flags=flags-(int)CMath.pow(2,bit);
                    else
                        flags=flags+(int)CMath.pow(2,bit);
                }
            }
            else
            if(newName.equalsIgnoreCase("?"))
            {
                StringBuffer str=new StringBuffer("Valid values: \n\r");
                for(String name : codes.names())
                    str.append(name+" ");
                mob.tell(str.toString());
            }
            else
                mob.tell("(no change)");
        }
        E.setStat("WEAR",""+flags);
    }


    protected void genRaceAvailability(MOB mob, Race E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Availability: '"+Area.THEME_PHRASE_EXT[CMath.s_int(E.getStat("AVAIL"))]+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newName="?";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(newName.equals("?")))
        {
            newName=mob.session().prompt("Enter a new value (?)\n\r:","");
            if(newName.length()==0)
                mob.tell("(no change)");
            else
            if((CMath.isNumber(newName))&&(CMath.s_int(newName)<Area.THEME_PHRASE_EXT.length))
                E.setStat("AVAIL",""+CMath.s_int(newName));
            else
            if(newName.equalsIgnoreCase("?"))
            {
                StringBuffer str=new StringBuffer("Valid values: \n\r");
                for(int i=0;i<Area.THEME_PHRASE_EXT.length;i++)
                    str.append(i+") "+Area.THEME_PHRASE_EXT[i]+"\n\r");
                mob.tell(str.toString());
            }
            else
                mob.tell("(no change)");
        }
    }

    protected void genClassAvailability(MOB mob, CharClass E, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Availability: '"+Area.THEME_PHRASE_EXT[CMath.s_int(E.getStat("PLAYER"))]+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newName="?";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(newName.equals("?")))
        {
            newName=mob.session().prompt("Enter a new value (?)\n\r:","");
            if(newName.length()==0)
                mob.tell("(no change)");
            else
            if((CMath.isNumber(newName))&&(CMath.s_int(newName)<Area.THEME_PHRASE_EXT.length))
                E.setStat("PLAYER",""+CMath.s_int(newName));
            else
            if(newName.equalsIgnoreCase("?"))
            {
                StringBuffer str=new StringBuffer("Valid values: \n\r");
                for(int i=0;i<Area.THEME_PHRASE_EXT.length;i++)
                    str.append(i+") "+Area.THEME_PHRASE_EXT[i]+"\n\r");
                mob.tell(str.toString());
            }
            else
                mob.tell("(no change)");
        }
    }

    protected void genCat(MOB mob, Race E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Racial Category: '"+E.racialCategory()+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newName=mob.session().prompt("Enter a new one\n\r:","");
        if(newName.length()>0)
        {
            boolean found=false;
            if(newName.startsWith("new "))
            {
                newName=CMStrings.capitalizeAndLower(newName.substring(4));
                if(newName.length()>0)
                    found=true;
            }
            else
            for(Enumeration r=CMClass.races();r.hasMoreElements();)
            {
                Race R=(Race)r.nextElement();
                if(newName.equalsIgnoreCase(R.racialCategory()))
                {
                    newName=R.racialCategory();
                    found=true;
                    break;
                }
            }
            if(!found)
            {
                StringBuffer str=new StringBuffer("That category does not exist.  Valid categories include: ");
                HashSet H=new HashSet();
                for(Enumeration r=CMClass.races();r.hasMoreElements();)
                {
                    Race R=(Race)r.nextElement();
                    if(!H.contains(R.racialCategory()))
                    {
                        H.add(R.racialCategory());
                        str.append(R.racialCategory()+", ");
                    }
                }
                mob.tell(str.toString().substring(0,str.length()-2)+".");
            }
            else
                E.setStat("CAT",newName);
        }
        else
            mob.tell("(no change)");
    }


    protected void genRaceBuddy(MOB mob, Race E, int showNumber, int showFlag, String prompt, String flag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". "+prompt+": '"+E.getStat(flag)+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newName=mob.session().prompt("Enter a new one\n\r:","");
        if(newName.length()>0)
        {
            Race R2=CMClass.getRace(newName);
            if(R2==null) R2=(Race)CMClass.unsortedLoadClass("RACE",newName,true);
            if((R2!=null)&&(R2.isGeneric()))
                R2=null;
            if(R2==null)
            {
                StringBuffer str=new StringBuffer("That race name is invalid or is generic.  Valid races include: ");
                for(Enumeration r=CMClass.races();r.hasMoreElements();)
                {
                    Race R=(Race)r.nextElement();
                    if(!R.isGeneric())
                        str.append(R.ID()+", ");
                }
                mob.tell(str.toString().substring(0,str.length()-2)+".");
            }
            else
            if(CMClass.getRace(newName)==R2)
                E.setStat(flag,R2.ID());
            else
                E.setStat(flag,R2.getClass().getName());
        }
        else
            mob.tell("(no change)");
    }

    protected void genClassBuddy(MOB mob, CharClass E, int showNumber, int showFlag, String prompt, String flag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". "+prompt+": '"+E.getStat(flag)+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newName=mob.session().prompt("Enter a new one\n\r:","");
        if(newName.length()>0)
        {
            CharClass C2=CMClass.getCharClass(newName);
            if(C2==null) C2=(CharClass)CMClass.unsortedLoadClass("CHARCLASS",newName,true);
            if((C2!=null)&&(C2.isGeneric()))
                C2=null;
            if(C2==null)
            {
                StringBuffer str=new StringBuffer("That char class name is invalid or is generic.  Valid char classes include: ");
                for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
                {
                    CharClass C=(CharClass)c.nextElement();
                    if(!C.isGeneric())
                        str.append(C.ID()+", ");
                }
                mob.tell(str.toString().substring(0,str.length()-2)+".");
            }
            else
            if(CMClass.getCharClass(newName)==C2)
                E.setStat(flag,C2.ID());
            else
                E.setStat(flag,C2.getClass().getName());
        }
        else
            mob.tell("(no change)");
    }


    protected void genBodyParts(MOB mob, Race E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        StringBuffer parts=new StringBuffer("");
        for(int i=0;i<Race.BODYPARTSTR.length;i++)
            if(E.bodyMask()[i]!=0) parts.append(Race.BODYPARTSTR[i].toLowerCase()+"("+E.bodyMask()[i]+") ");
        mob.tell(showNumber+". Body Parts: "+parts.toString()+".");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newName=mob.session().prompt("Enter a body part\n\r:","");
        if(newName.length()>0)
        {
            int partNum=-1;
            for(int i=0;i<Race.BODYPARTSTR.length;i++)
                if(newName.equalsIgnoreCase(Race.BODYPARTSTR[i]))
                { partNum=i; break;}
            if(partNum<0)
            {
                StringBuffer str=new StringBuffer("That body part is invalid.  Valid parts include: ");
                for(int i=0;i<Race.BODYPARTSTR.length;i++)
                    str.append(Race.BODYPARTSTR[i]+", ");
                mob.tell(str.toString().substring(0,str.length()-2)+".");
            }
            else
            {
                newName=mob.session().prompt("Enter new number ("+E.bodyMask()[partNum]+"), 0=none\n\r:",""+E.bodyMask()[partNum]);
                if(newName.length()>0)
                    E.bodyMask()[partNum]=CMath.s_int(newName);
                else
                    mob.tell("(no change)");
            }
        }
        else
            mob.tell("(no change)");
    }
    protected void genEStats(MOB mob, Race R, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        EnvStats S=(EnvStats)CMClass.getCommon("DefaultEnvStats");
        S.setAllValues(0);
        CMLib.coffeeMaker().setEnvStats(S,R.getStat("ESTATS"));
        StringBuffer parts=new StringBuffer("");
        for(int i=0;i<S.getStatCodes().length;i++)
            if(CMath.s_int(S.getStat(S.getStatCodes()[i]))!=0)
                parts.append(CMStrings.capitalizeAndLower(S.getStatCodes()[i])+"("+S.getStat(S.getStatCodes()[i])+") ");
        mob.tell(showNumber+". EStat Adjustments: "+parts.toString()+".");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        boolean done=false;
        while((!done)&&(mob.session()!=null)&&(!mob.session().killFlag()))
        {
            String newName=mob.session().prompt("Enter a stat name\n\r:","");
            if(newName.length()>0)
            {
                String partName=null;
                for(int i=0;i<S.getStatCodes().length;i++)
                    if(newName.equalsIgnoreCase(S.getStatCodes()[i]))
                    { partName=S.getStatCodes()[i]; break;}
                if(partName==null)
                {
                    StringBuffer str=new StringBuffer("That stat is invalid.  Valid stats include: ");
                    for(int i=0;i<S.getStatCodes().length;i++)
                        str.append(S.getStatCodes()[i]+", ");
                    mob.tell(str.toString().substring(0,str.length()-2)+".");
                }
                else
                {
                    boolean checkChange=false;
                    if(partName.equals("DISPOSITION"))
                    {
                        genDisposition(mob,S,0,0);
                        checkChange=true;
                    }
                    else
                    if(partName.equals("SENSES"))
                    {
                        genSensesMask(mob,S,0,0);
                        checkChange=true;
                    }
                    else
                    {
                        newName=mob.session().prompt("Enter a value\n\r:","");
                        if(newName.length()>0)
                        {
                            S.setStat(partName,newName);
                            checkChange=true;
                        }
                        else
                            mob.tell("(no change)");
                    }
                    if(checkChange)
                    {
                        boolean zereoed=true;
                        for(int i=0;i<S.getStatCodes().length;i++)
                        {
                            if(CMath.s_int(S.getStat(S.getStatCodes()[i]))!=0)
                            { zereoed=false; break;}
                        }
                        if(zereoed)
                            R.setStat("ESTATS","");
                        else
                            R.setStat("ESTATS",CMLib.coffeeMaker().getEnvStatsStr(S));
                    }
                }
            }
            else
            {
                mob.tell("(no change)");
                done=true;
            }
        }
    }
    protected void genAState(MOB mob,
                          Race R,
                          String field,
                          String prompt,
                          int showNumber,
                          int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        CharState S=(CharState)CMClass.getCommon("DefaultCharState"); S.setAllValues(0);
        CMLib.coffeeMaker().setCharState(S,R.getStat(field));
        StringBuffer parts=new StringBuffer("");
        for(int i=0;i<S.getStatCodes().length;i++)
            if(CMath.s_int(S.getStat(S.getStatCodes()[i]))!=0)
                parts.append(CMStrings.capitalizeAndLower(S.getStatCodes()[i])+"("+S.getStat(S.getStatCodes()[i])+") ");
        mob.tell(showNumber+". "+prompt+": "+parts.toString()+".");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        boolean done=false;
        while((!done)&&(mob.session()!=null)&&(!mob.session().killFlag()))
        {
            String newName=mob.session().prompt("Enter a stat name\n\r:","");
            if(newName.length()>0)
            {
                String partName=null;
                for(int i=0;i<S.getStatCodes().length;i++)
                    if(newName.equalsIgnoreCase(S.getStatCodes()[i]))
                    { partName=S.getStatCodes()[i]; break;}
                if(partName==null)
                {
                    StringBuffer str=new StringBuffer("That stat is invalid.  Valid stats include: ");
                    for(int i=0;i<S.getStatCodes().length;i++)
                        str.append(S.getStatCodes()[i]+", ");
                    mob.tell(str.toString().substring(0,str.length()-2)+".");
                }
                else
                {
                    newName=mob.session().prompt("Enter a value\n\r:","");
                    if(newName.length()>0)
                    {
                        S.setStat(partName,newName);
                        boolean zereoed=true;
                        for(int i=0;i<S.getStatCodes().length;i++)
                        {
                            if(CMath.s_int(S.getStat(S.getStatCodes()[i]))!=0)
                            { zereoed=false; break;}
                        }
                        if(zereoed)
                            R.setStat(field,"");
                        else
                            R.setStat(field,CMLib.coffeeMaker().getCharStateStr(S));
                    }
                    else
                        mob.tell("(no change)");
                }
            }
            else
            {
                mob.tell("(no change)");
                done=true;
            }
        }
    }
    protected void genAStats(MOB mob, Race R, String Field, String FieldName, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        CharStats S=(CharStats)CMClass.getCommon("DefaultCharStats"); S.setAllValues(0);
        CMLib.coffeeMaker().setCharStats(S,R.getStat(Field));
        StringBuffer parts=new StringBuffer("");
        for(int i : CharStats.CODES.ALL())
            if(S.getStat(i)!=0)
                parts.append(CMStrings.capitalizeAndLower(CharStats.CODES.DESC(i))+"("+S.getStat(i)+") ");
        mob.tell(showNumber+". "+FieldName+": "+parts.toString()+".");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        boolean done=false;
        while((!done)&&(mob.session()!=null)&&(!mob.session().killFlag()))
        {
            String newName=mob.session().prompt("Enter a stat name\n\r:","");
            if(newName.length()>0)
            {
                int partNum=-1;
                for(int i : CharStats.CODES.ALL())
                    if(newName.equalsIgnoreCase(CharStats.CODES.DESC(i)))
                    { partNum=i; break;}
                if(partNum<0)
                {
                    StringBuffer str=new StringBuffer("That stat is invalid.  Valid stats include: ");
                    for(int i : CharStats.CODES.ALL())
                        str.append(CharStats.CODES.DESC(i)+", ");
                    mob.tell(str.toString().substring(0,str.length()-2)+".");
                }
                else
                {
                    newName=mob.session().prompt("Enter a value\n\r:","");
                    if(newName.length()>0)
                    {
                        if(newName.trim().equalsIgnoreCase("0"))
                            S.setStat(partNum,CMath.s_int(newName));
                        else
                        if(partNum==CharStats.STAT_GENDER)
                            S.setStat(partNum,(int)newName.charAt(0));
                        else
                            S.setStat(partNum,CMath.s_int(newName));
                        boolean zereoed=true;
                        for(int i : CharStats.CODES.ALL())
                        {
                            if(S.getStat(i)!=0)
                            { zereoed=false; break;}
                        }
                        if(zereoed)
                            R.setStat(Field,"");
                        else
                            R.setStat(Field,CMLib.coffeeMaker().getCharStatsStr(S));
                    }
                    else
                        mob.tell("(no change)");
                }
            }
            else
            {
                mob.tell("(no change)");
                done=true;
            }
        }
    }

    protected void genEStats(MOB mob, CharClass R, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        EnvStats S=(EnvStats)CMClass.getCommon("DefaultEnvStats");
        S.setAllValues(0);
        CMLib.coffeeMaker().setEnvStats(S,R.getStat("ESTATS"));
        StringBuffer parts=new StringBuffer("");
        for(int i=0;i<S.getStatCodes().length;i++)
            if(CMath.s_int(S.getStat(S.getStatCodes()[i]))!=0)
                parts.append(CMStrings.capitalizeAndLower(S.getStatCodes()[i])+"("+S.getStat(S.getStatCodes()[i])+") ");
        mob.tell(showNumber+". EStat Adjustments: "+parts.toString()+".");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        boolean done=false;
        while((!done)&&(mob.session()!=null)&&(!mob.session().killFlag()))
        {
            String newName=mob.session().prompt("Enter a stat name\n\r:","");
            if(newName.length()>0)
            {
                String partName=null;
                for(int i=0;i<S.getStatCodes().length;i++)
                    if(newName.equalsIgnoreCase(S.getStatCodes()[i]))
                    { partName=S.getStatCodes()[i]; break;}
                if(partName==null)
                {
                    StringBuffer str=new StringBuffer("That stat is invalid.  Valid stats include: ");
                    for(int i=0;i<S.getStatCodes().length;i++)
                        str.append(S.getStatCodes()[i]+", ");
                    mob.tell(str.toString().substring(0,str.length()-2)+".");
                }
                else
                {
                    boolean checkChange=false;
                    if(partName.equals("DISPOSITION"))
                    {
                        genDisposition(mob,S,0,0);
                        checkChange=true;
                    }
                    else
                    if(partName.equals("SENSES"))
                    {
                        genSensesMask(mob,S,0,0);
                        checkChange=true;
                    }
                    else
                    {
                        newName=mob.session().prompt("Enter a value\n\r:","");
                        if(newName.length()>0)
                        {
                            S.setStat(partName,newName);
                            checkChange=true;
                        }
                        else
                            mob.tell("(no change)");
                    }
                    if(checkChange)
                    {
                        boolean zereoed=true;
                        for(int i=0;i<S.getStatCodes().length;i++)
                        {
                            if(CMath.s_int(S.getStat(S.getStatCodes()[i]))!=0)
                            { zereoed=false; break;}
                        }
                        if(zereoed)
                            R.setStat("ESTATS","");
                        else
                            R.setStat("ESTATS",CMLib.coffeeMaker().getEnvStatsStr(S));
                    }
                }
            }
            else
            {
                mob.tell("(no change)");
                done=true;
            }
        }
    }
    protected void genAState(MOB mob,
                          CharClass R,
                          String field,
                          String prompt,
                          int showNumber,
                          int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        CharState S=(CharState)CMClass.getCommon("DefaultCharState"); S.setAllValues(0);
        CMLib.coffeeMaker().setCharState(S,R.getStat(field));
        StringBuffer parts=new StringBuffer("");
        for(int i=0;i<S.getStatCodes().length;i++)
            if(CMath.s_int(S.getStat(S.getStatCodes()[i]))!=0)
                parts.append(CMStrings.capitalizeAndLower(S.getStatCodes()[i])+"("+S.getStat(S.getStatCodes()[i])+") ");
        mob.tell(showNumber+". "+prompt+": "+parts.toString()+".");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        boolean done=false;
        while((!done)&&(mob.session()!=null)&&(!mob.session().killFlag()))
        {
            String newName=mob.session().prompt("Enter a stat name\n\r:","");
            if(newName.length()>0)
            {
                String partName=null;
                for(int i=0;i<S.getStatCodes().length;i++)
                    if(newName.equalsIgnoreCase(S.getStatCodes()[i]))
                    { partName=S.getStatCodes()[i]; break;}
                if(partName==null)
                {
                    StringBuffer str=new StringBuffer("That stat is invalid.  Valid stats include: ");
                    for(int i=0;i<S.getStatCodes().length;i++)
                        str.append(S.getStatCodes()[i]+", ");
                    mob.tell(str.toString().substring(0,str.length()-2)+".");
                }
                else
                {
                    newName=mob.session().prompt("Enter a value\n\r:","");
                    if(newName.length()>0)
                    {
                        S.setStat(partName,newName);
                        boolean zereoed=true;
                        for(int i=0;i<S.getStatCodes().length;i++)
                        {
                            if(CMath.s_int(S.getStat(S.getStatCodes()[i]))!=0)
                            { zereoed=false; break;}
                        }
                        if(zereoed)
                            R.setStat(field,"");
                        else
                            R.setStat(field,CMLib.coffeeMaker().getCharStateStr(S));
                    }
                    else
                        mob.tell("(no change)");
                }
            }
            else
            {
                mob.tell("(no change)");
                done=true;
            }
        }
    }
    protected void genAStats(MOB mob, CharClass R, String Field, String FieldName, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        CharStats S=(CharStats)CMClass.getCommon("DefaultCharStats"); S.setAllValues(0);
        CMLib.coffeeMaker().setCharStats(S,R.getStat(Field));
        StringBuffer parts=new StringBuffer("");
        for(int i : CharStats.CODES.ALL())
            if(S.getStat(i)!=0)
                parts.append(CMStrings.capitalizeAndLower(CharStats.CODES.DESC(i))+"("+S.getStat(i)+") ");
        mob.tell(showNumber+". "+FieldName+": "+parts.toString()+".");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        boolean done=false;
        while((!done)&&(mob.session()!=null)&&(!mob.session().killFlag()))
        {
            String newName=mob.session().prompt("Enter a stat name\n\r:","");
            if(newName.length()>0)
            {
                int partNum=-1;
                for(int i : CharStats.CODES.ALL())
                    if(newName.equalsIgnoreCase(CharStats.CODES.DESC(i)))
                    { partNum=i; break;}
                if(partNum<0)
                {
                    StringBuffer str=new StringBuffer("That stat is invalid.  Valid stats include: ");
                    for(int i : CharStats.CODES.ALL())
                        str.append(CharStats.CODES.DESC(i)+", ");
                    mob.tell(str.toString().substring(0,str.length()-2)+".");
                }
                else
                {
                    newName=mob.session().prompt("Enter a value\n\r:","");
                    if(newName.length()>0)
                    {
                        S.setStat(partNum,CMath.s_int(newName));
                        boolean zereoed=true;
                        for(int i : CharStats.CODES.ALL())
                        {
                            if(S.getStat(i)!=0)
                            { zereoed=false; break;}
                        }
                        if(zereoed)
                            R.setStat(Field,"");
                        else
                            R.setStat(Field,CMLib.coffeeMaker().getCharStatsStr(S));
                    }
                    else
                        mob.tell("(no change)");
                }
            }
            else
            {
                mob.tell("(no change)");
                done=true;
            }
        }
    }
    protected void genResources(MOB mob, Race E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(true))
        {
            StringBuffer parts=new StringBuffer("");
            int numResources=CMath.s_int(E.getStat("NUMRSC"));
            DVector DV=new DVector(2);
            for(int r=0;r<numResources;r++)
            {
                Item I=CMClass.getItem(E.getStat("GETRSCID"+r));
                if(I!=null)
                {
                    I.setMiscText(E.getStat("GETRSCPARM"+r));
                    I.recoverEnvStats();
                    boolean done=false;
                    for(int v=0;v<DV.size();v++)
                        if(I.sameAs((Environmental)DV.elementAt(v,1)))
                        { DV.setElementAt(v,2,Integer.valueOf(((Integer)DV.elementAt(v,2)).intValue()+1)); done=true; break;}
                    if(!done)
                        DV.addElement(I,Integer.valueOf(1));
                }
                else
                    parts.append("Unknown: "+E.getStat("GETRSCID"+r)+", ");
            }
            for(int v=0;v<DV.size();v++)
            {
                Item I=(Item)DV.elementAt(v,1);
                int i=((Integer)DV.elementAt(v,2)).intValue();
                if(i<2)
                    parts.append(I.name()+", ");
                else
                    parts.append(I.name()+" ("+i+"), ");
            }
            if(parts.toString().endsWith(", "))
            {parts.deleteCharAt(parts.length()-1);parts.deleteCharAt(parts.length()-1);}
            mob.tell(showNumber+". Resources: "+parts.toString()+".");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            String newName=mob.session().prompt("Enter a resource name to remove or\n\rthe word new and an item name to add from your inventory\n\r:","");
            if(newName.length()>0)
            {
                int partNum=-1;
                for(int i=0;i<DV.size();i++)
                    if(CMLib.english().containsString(((Item)DV.elementAt(i,1)).name(),newName))
                    { partNum=i; break;}
                boolean updateList=false;
                if(partNum<0)
                {
                    if(!newName.toLowerCase().startsWith("new "))
                        mob.tell("That is neither an existing resource name, or the word new followed by a valid item name.");
                    else
                    {
                        Item I=mob.fetchCarried(null,newName.substring(4).trim());
                        if(I!=null)
                        {
                            I=(Item)I.copyOf();
                            boolean done=false;
                            for(int v=0;v<DV.size();v++)
                                if(I.sameAs((Environmental)DV.elementAt(v,1)))
                                { DV.setElementAt(v,2,Integer.valueOf(((Integer)DV.elementAt(v,2)).intValue()+1)); done=true; break;}
                            if(!done)
                                DV.addElement(I,Integer.valueOf(1));
                            else
                            	I.destroy();
                            mob.tell(I.name()+" added.");
                            updateList=true;
                        }

                    }
                }
                else
                {
                    Item I=(Item)DV.elementAt(partNum,1);
                    int i=((Integer)DV.elementAt(partNum,2)).intValue();
                    if(i<2)
                        DV.removeElementAt(partNum);
                    else
                        DV.setElementAt(partNum,2,Integer.valueOf(i-1));
                    mob.tell(I.name()+" removed.");
                    updateList=true;
                }
                if(updateList)
                {
                    int dex=0;
                    for(int i=0;i<DV.size();i++)
                        dex+=((Integer)DV.elementAt(i,2)).intValue();
                    E.setStat("NUMRSC",""+dex);
                    dex=0;
                    Item I=null;
                    Integer N=null;
                    for(int i=0;i<DV.size();i++)
                    {
                        I=(Item)DV.elementAt(i,1);
                        N=(Integer)DV.elementAt(i,2);
                        for(int n=0;n<N.intValue();n++)
                            E.setStat("GETRSCID"+(dex++),I.ID());
                    }
                    dex=0;
                    for(int i=0;i<DV.size();i++)
                    {
                        I=(Item)DV.elementAt(i,1);
                        N=(Integer)DV.elementAt(i,2);
                        for(int n=0;n<N.intValue();n++)
                            E.setStat("GETRSCPARM"+(dex++),I.text());
                    }
                }
            }
            else
            {
                mob.tell("(no change)");
                return;
            }
        }
    }
    protected void genOutfit(MOB mob, Race E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(true))
        {
            StringBuffer parts=new StringBuffer("");
            int numResources=CMath.s_int(E.getStat("NUMOFT"));
            Vector V=new Vector();
            for(int v=0;v<numResources;v++)
            {
                Item I=CMClass.getItem(E.getStat("GETOFTID"+v));
                if(I!=null)
                {
                    I.setMiscText(E.getStat("GETOFTPARM"+v));
                    I.recoverEnvStats();
                    parts.append(I.name()+", ");
                    V.addElement(I);
                }
            }
            if(parts.toString().endsWith(", "))
            {parts.deleteCharAt(parts.length()-1);parts.deleteCharAt(parts.length()-1);}
            mob.tell(showNumber+". Outfit: "+parts.toString()+".");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            String newName=mob.session().prompt("Enter an item name to remove or\n\rthe word new and an item name to add from your inventory\n\r:","");
            if(newName.length()>0)
            {
                int partNum=-1;
                for(int i=0;i<V.size();i++)
                    if(CMLib.english().containsString(((Item)V.elementAt(i)).name(),newName))
                    { partNum=i; break;}
                boolean updateList=false;
                if(partNum<0)
                {
                    if(!newName.toLowerCase().startsWith("new "))
                        mob.tell("That is neither an existing item name, or the word new followed by a valid item name.");
                    else
                    {
                        Item I=mob.fetchCarried(null,newName.substring(4).trim());
                        if(I!=null)
                        {
                            I=(Item)I.copyOf();
                            V.addElement(I);
                            mob.tell(I.name()+" added.");
                            updateList=true;
                        }

                    }
                }
                else
                {
                    Item I=(Item)V.elementAt(partNum);
                    V.removeElementAt(partNum);
                    mob.tell(I.name()+" removed.");
                    updateList=true;
                }
                if(updateList)
                {
                    E.setStat("NUMOFT","");
                    for(int i=0;i<V.size();i++)
                        E.setStat("GETOFTID"+i,((Item)V.elementAt(i)).ID());
                    for(int i=0;i<V.size();i++)
                        E.setStat("GETOFTPARM"+i,((Item)V.elementAt(i)).text());
                }
            }
            else
            {
                mob.tell("(no change)");
                return;
            }
        }
    }
    protected void genOutfit(MOB mob, CharClass E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(true))
        {
            StringBuffer parts=new StringBuffer("");
            int numResources=CMath.s_int(E.getStat("NUMOFT"));
            Vector V=new Vector();
            for(int v=0;v<numResources;v++)
            {
                Item I=CMClass.getItem(E.getStat("GETOFTID"+v));
                if(I!=null)
                {
                    I.setMiscText(E.getStat("GETOFTPARM"+v));
                    I.recoverEnvStats();
                    parts.append(I.name()+", ");
                    V.addElement(I);
                }
            }
            if(parts.toString().endsWith(", "))
            {parts.deleteCharAt(parts.length()-1);parts.deleteCharAt(parts.length()-1);}
            mob.tell(showNumber+". Outfit: "+parts.toString()+".");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            String newName=mob.session().prompt("Enter an item name to remove or\n\rthe word new and an item name to add from your inventory\n\r:","");
            if(newName.length()>0)
            {
                int partNum=-1;
                for(int i=0;i<V.size();i++)
                    if(CMLib.english().containsString(((Item)V.elementAt(i)).name(),newName))
                    { partNum=i; break;}
                boolean updateList=false;
                if(partNum<0)
                {
                    if(!newName.toLowerCase().startsWith("new "))
                        mob.tell("That is neither an existing item name, or the word new followed by a valid item name.");
                    else
                    {
                        Item I=mob.fetchCarried(null,newName.substring(4).trim());
                        if(I!=null)
                        {
                            I=(Item)I.copyOf();
                            V.addElement(I);
                            mob.tell(I.name()+" added.");
                            updateList=true;
                        }

                    }
                }
                else
                {
                    Item I=(Item)V.elementAt(partNum);
                    V.removeElementAt(partNum);
                    mob.tell(I.name()+" removed.");
                    updateList=true;
                }
                if(updateList)
                {
                    E.setStat("NUMOFT","");
                    for(int i=0;i<V.size();i++)
                        E.setStat("GETOFTID"+i,((Item)V.elementAt(i)).ID());
                    for(int i=0;i<V.size();i++)
                        E.setStat("GETOFTPARM"+i,((Item)V.elementAt(i)).text());
                }
            }
            else
            {
                mob.tell("(no change)");
                return;
            }
        }
    }
    protected void genWeapon(MOB mob, Race E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        StringBuffer parts=new StringBuffer("");
        Item I=CMClass.getItem(E.getStat("WEAPONCLASS"));
        if(I!=null)
        {
            I.setMiscText(E.getStat("WEAPONXML"));
            I.recoverEnvStats();
            parts.append(I.name());
        }
        mob.tell(showNumber+". Natural Weapon: "+parts.toString()+".");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String newName=mob.session().prompt("Enter a weapon name from your inventory to change, or 'null' for human\n\r:","");
        if(newName.equalsIgnoreCase("null"))
        {
            E.setStat("WEAPONCLASS","");
            mob.tell("Human weapons set.");
        }
        else
        if(newName.length()>0)
        {
            I=mob.fetchCarried(null,newName);
            if(I==null)
            {
                mob.tell("'"+newName+"' is not in your inventory.");
                mob.tell("(no change)");
                return;
            }
            I=(Item)I.copyOf();
            E.setStat("WEAPONCLASS",I.ID());
            E.setStat("WEAPONXML",I.text());
            I.destroy();
        }
        else
        {
            mob.tell("(no change)");
            return;
        }
    }

    protected void modifyDField(DVector fields, String fieldName, String value)
    {
        int x=fields.indexOf(fieldName.toUpperCase());
        if(x<0) return;
        fields.setElementAt(x,2,value);
    }

    protected void genAgingChart(MOB mob, Race E, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;

        mob.tell(showNumber+". Aging Chart: "+CMParms.toStringList(E.getAgingChart())+".");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(true))
        {
            String newName=mob.session().prompt("Enter a comma-delimited list of 9 numbers, running from infant -> ancient\n\r:","");
            if(newName.length()==0)
            {
                mob.tell("(no change)");
                return;
            }
            Vector V=CMParms.parseCommas(newName,true);
            if(V.size()==9)
            {
                int highest=-1;
                boolean cont=false;
                for(int i=0;i<V.size();i++)
                {
                    if(CMath.s_int((String)V.elementAt(i))<highest)
                    {
                        mob.tell("Entry "+((String)V.elementAt(i))+" is out of place.");
                        cont=true;
                        break;
                    }
                    highest=CMath.s_int((String)V.elementAt(i));
                }
                if(cont) continue;
                E.setStat("AGING",newName);
                break;
            }
        }
    }

    protected void genClassFlags(MOB mob, CharClass E, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber))
            return;

        int flags=CMath.s_int(E.getStat("DISFLAGS"));
        StringBuffer sets=new StringBuffer("");
        if(CMath.bset(flags,CharClass.GENFLAG_NORACE))
            sets.append("Raceless ");
        if(CMath.bset(flags,CharClass.GENFLAG_NOLEVELS))
            sets.append("Leveless ");
        if(CMath.bset(flags,CharClass.GENFLAG_NOEXP))
            sets.append("Expless ");

        mob.tell(showNumber+". Extra CharClass Flags: "+sets.toString()+".");
        if((showFlag!=showNumber)&&(showFlag>-999))
            return;
        String newName=mob.session().prompt("Enter: 1) Classless, 2) Leveless, 3) Expless\n\r:","");
        switch(CMath.s_int(newName))
        {
        case 1:
            if(CMath.bset(flags,CharClass.GENFLAG_NORACE))
                flags=CMath.unsetb(flags,CharClass.GENFLAG_NORACE);
            else
                flags=flags|CharClass.GENFLAG_NORACE;
            break;
        case 2:
            if(CMath.bset(flags,CharClass.GENFLAG_NOLEVELS))
                flags=CMath.unsetb(flags,CharClass.GENFLAG_NOLEVELS);
            else
                flags=flags|CharClass.GENFLAG_NOLEVELS;
            break;
        case 3:
            if(CMath.bset(flags,CharClass.GENFLAG_NOEXP))
                flags=CMath.unsetb(flags,CharClass.GENFLAG_NOEXP);
            else
                flags=flags|CharClass.GENFLAG_NOEXP;
            break;
        default:
            mob.tell("(no change)");
            break;
        }
        E.setStat("DISFLAGS",""+flags);
    }

    protected void genRacialAbilities(MOB mob, Race E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(true))
        {
            StringBuffer parts=new StringBuffer("");
            int numResources=CMath.s_int(E.getStat("NUMRABLE"));
            Vector ables=new Vector();
            Vector data=new Vector();
            for(int v=0;v<numResources;v++)
            {
                Ability A=CMClass.getAbility(E.getStat("GETRABLE"+v));
                if(A!=null)
                {
                    parts.append("("+A.ID()+"/"+E.getStat("GETRABLELVL"+v)+"/"+E.getStat("GETRABLEQUAL"+v)+"/"+E.getStat("GETRABLEPROF"+v)+"), ");
                    ables.addElement(A);
                    data.addElement(A.ID()+";"+E.getStat("GETRABLELVL"+v)+";"+E.getStat("GETRABLEQUAL"+v)+";"+E.getStat("GETRABLEPROF"+v));
                }
            }
            if(parts.toString().endsWith(", "))
            {parts.deleteCharAt(parts.length()-1);parts.deleteCharAt(parts.length()-1);}
            mob.tell(showNumber+". Racial Abilities: "+parts.toString()+".");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            String newName=mob.session().prompt("Enter an ability name to add or remove (?)\n\r:","");
            if(newName.equalsIgnoreCase("?"))
                mob.tell(CMLib.lister().reallyList(CMClass.abilities(),-1).toString());
            else
            if(newName.length()>0)
            {
                int partNum=-1;
                for(int i=0;i<ables.size();i++)
                    if(CMLib.english().containsString(((Ability)ables.elementAt(i)).ID(),newName))
                    { partNum=i; break;}
                boolean updateList=false;
                if(partNum<0)
                {
                    Ability A=CMClass.getAbility(newName);
                    if(A==null)
                        mob.tell("That is neither an existing ability name, nor a valid one to add.  Use ? for a list.");
                    else
                    if(A.isAutoInvoked())
                        mob.tell("'"+A.name()+"' cannot be named, as it is autoinvoked.");
                    else
                    if((A.triggerStrings()==null)||(A.triggerStrings().length==0))
                        mob.tell("'"+A.name()+"' cannot be named, as it has no trigger/command words.");
                    else
                    {
                        StringBuffer str=new StringBuffer(A.ID()+";");
                        String level=mob.session().prompt("Enter the level of this skill (1): ","1");
                        str.append((""+CMath.s_int(level))+";");
                        if(mob.session().confirm("Is this skill automatically gained (Y/n)?","Y"))
                            str.append("false;");
                        else
                            str.append("true;");
                        String prof=mob.session().prompt("Enter the (perm) proficiency level (100): ","100");
                        str.append((""+CMath.s_int(prof)));
                        data.addElement(str.toString());
                        ables.addElement(A);
                        mob.tell(A.name()+" added.");
                        updateList=true;
                    }
                }
                else
                {
                    Ability A=(Ability)ables.elementAt(partNum);
                    ables.removeElementAt(partNum);
                    data.removeElementAt(partNum);
                    updateList=true;
                    mob.tell(A.name()+" removed.");
                }
                if(updateList)
                {
                    if(data.size()>0)
                        E.setStat("NUMRABLE",""+data.size());
                    else
                        E.setStat("NUMRABLE","");
                    for(int i=0;i<data.size();i++)
                    {
                        Vector V=CMParms.parseSemicolons((String)data.elementAt(i),false);
                        E.setStat("GETRABLE"+i,((String)V.elementAt(0)));
                        E.setStat("GETRABLELVL"+i,((String)V.elementAt(1)));
                        E.setStat("GETRABLEQUAL"+i,((String)V.elementAt(2)));
                        E.setStat("GETRABLEPROF"+i,((String)V.elementAt(3)));
                    }
                }
            }
            else
            {
                mob.tell("(no change)");
                return;
            }
        }
    }
    protected void genRacialEffects(MOB mob, Race E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(true))
        {
            StringBuffer parts=new StringBuffer("");
            int numResources=CMath.s_int(E.getStat("NUMREFF"));
            Vector ables=new Vector();
            Vector data=new Vector();
            for(int v=0;v<numResources;v++)
            {
                Ability A=CMClass.getAbility(E.getStat("GETREFF"+v));
                if(A!=null)
                {
                    parts.append("("+A.ID()+"/"+E.getStat("GETREFFLVL"+v)+"/"+E.getStat("GETREFFPARM"+v)+"), ");
                    ables.addElement(A);
                    data.addElement(A.ID()+"~"+E.getStat("GETREFFLVL"+v)+"~"+E.getStat("GETREFFPARM"+v));
                }
            }
            if(parts.toString().endsWith(", "))
            {parts.deleteCharAt(parts.length()-1);parts.deleteCharAt(parts.length()-1);}
            mob.tell(showNumber+". Racial Effects: "+parts.toString()+".");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            String newName=mob.session().prompt("Enter an effect name to add or remove\n\r:","");
            if(newName.equalsIgnoreCase("?"))
                mob.tell(CMLib.lister().reallyList(CMClass.abilities(),-1).toString());
            else
            if(newName.length()>0)
            {
                int partNum=-1;
                for(int i=0;i<ables.size();i++)
                    if(CMLib.english().containsString(((Ability)ables.elementAt(i)).ID(),newName))
                    { partNum=i; break;}
                boolean updateList=false;
                if(partNum<0)
                {
                    Ability A=CMClass.getAbility(newName);
                    if(A==null)
                        mob.tell("That is neither an existing effect name, nor a valid one to add.  Use ? for a list.");
                    else
                    {
                        StringBuffer str=new StringBuffer(A.ID()+"~");
                        String level=mob.session().prompt("Enter the level to gain this effect (1): ","1");
                        str.append((""+CMath.s_int(level))+"~");
                        String prof=mob.session().prompt("Enter any parameters: ","");
                        str.append(""+prof);
                        data.addElement(str.toString());
                        ables.addElement(A);
                        mob.tell(A.name()+" added.");
                        updateList=true;
                    }
                }
                else
                {
                    Ability A=(Ability)ables.elementAt(partNum);
                    ables.removeElementAt(partNum);
                    data.removeElementAt(partNum);
                    updateList=true;
                    mob.tell(A.name()+" removed.");
                }
                if(updateList)
                {
                    if(data.size()>0)
                        E.setStat("NUMREFF",""+data.size());
                    else
                        E.setStat("NUMREFF","");
                    for(int i=0;i<data.size();i++)
                    {
                        Vector V=CMParms.parseSquiggleDelimited((String)data.elementAt(i),false);
                        E.setStat("GETREFF"+i,((String)V.elementAt(0)));
                        E.setStat("GETREFFLVL"+i,((String)V.elementAt(1)));
                        E.setStat("GETREFFPARM"+i,((String)V.elementAt(2)));
                    }
                }
            }
            else
            {
                mob.tell("(no change)");
                return;
            }
        }
    }

    protected DVector genClassAbleMod(MOB mob, DVector sets, String ableID, int origLevelIndex, int origAbleIndex)
    throws IOException
    {

        Integer level=null;
        if(origLevelIndex>=0)
        {
            if(mob.session().confirm("Enter Y to DELETE, or N to modify (y/N)?","N"))
            {
                Vector set=(Vector)sets.elementAt(origLevelIndex,2);
                set.removeElementAt(origAbleIndex);
                return null;
            }
            level=(Integer)sets.elementAt(origLevelIndex,1);
        }
        else
            level=Integer.valueOf(1);
        level=Integer.valueOf(CMath.s_int(mob.session().prompt("Enter the level of this skill ("+level+"): ",""+level)));
        if(level.intValue()<=0)
        {
            mob.tell("Aborted.");
            return null;
        }

    	AbilityMapper.AbilityMapping aMAP=new AbilityMapper.AbilityMapping();
        if(origLevelIndex<0)
        {
        	aMAP.abilityName=ableID;
        	aMAP.defaultProficiency=0;
        	aMAP.maxProficiency=100;
        	aMAP.defaultParm="";
        	aMAP.originalSkillPreReqList="";
        	aMAP.extraMask="";
        	aMAP.autoGain=false;
        	aMAP.isSecret=false;
        }
        else
        {
            Vector levelSet=(Vector)sets.elementAt(origLevelIndex,2);
            aMAP=(AbilityMapper.AbilityMapping)levelSet.elementAt(origAbleIndex);
            levelSet.removeElementAt(origAbleIndex);
            origAbleIndex=-1;
        }

        int newlevelIndex=sets.indexOf(level);
        Vector levelSet=null;
        if(newlevelIndex<0)
        {
            newlevelIndex=sets.size();
            levelSet=new Vector();
            sets.addElement(level,levelSet);
        }
        else
            levelSet=(Vector)sets.elementAt(newlevelIndex,2);
        aMAP.defaultProficiency=CMath.s_int(mob.session().prompt("Enter the (default) proficiency level ("+aMAP.defaultProficiency+"): ",aMAP.defaultProficiency+""));
        aMAP.maxProficiency=CMath.s_int(mob.session().prompt("Enter the (maximum) proficiency level ("+aMAP.maxProficiency+"): ",aMAP.maxProficiency+""));
        aMAP.autoGain=mob.session().confirm("Is this skill automatically gained"+(aMAP.autoGain?"(Y/n)":"(y/N)")+"?",""+aMAP.autoGain);
        aMAP.isSecret=mob.session().confirm("Is this skill secret "+(aMAP.isSecret?"(Y/n)":"(y/N)")+"?",""+aMAP.isSecret);
        aMAP.defaultParm=mob.session().prompt("Enter any properties ("+aMAP.defaultParm+")\n\r: ",aMAP.defaultParm);
        String s="?";
        while(s.equalsIgnoreCase("?"))
        {
            s=mob.session().prompt("Enter any pre-requisites ("+aMAP.originalSkillPreReqList+")\n\r(?) : ",aMAP.originalSkillPreReqList);
            if(s.equalsIgnoreCase("?"))
                mob.tell(""+CMLib.help().getHelpText("ABILITY_PREREQS",mob,true));
            else
            	aMAP.originalSkillPreReqList=s;
        }
        s="?";
        while(s.equalsIgnoreCase("?"))
        {
            s=mob.session().prompt("Enter any requirement mask ("+aMAP.extraMask+")\n\r(?) : ",aMAP.extraMask);
            if(s.equalsIgnoreCase("?"))
                mob.tell(""+CMLib.help().getHelpText("MASKS",mob,true));
            else
            	aMAP.extraMask=s;
        }
        levelSet.addElement(aMAP);
        return sets;
    }

    protected void genClassAbilities(MOB mob, CharClass E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        if((showFlag!=showNumber)&&(showFlag>-999))
        {
            mob.tell(showNumber+". Class Abilities: [...].");
            return;
        }
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(true))
        {
            StringBuffer parts=new StringBuffer("");
            int numAbles=CMath.s_int(E.getStat("NUMCABLE"));
            DVector levelSets=new DVector(2);
            int maxAbledLevel=Integer.MIN_VALUE;
            for(int v=0;v<numAbles;v++)
            {
                Ability A=CMClass.getAbility(E.getStat("GETCABLE"+v));
                if(A!=null)
                {
                	AbilityMapper.AbilityMapping aMAP=new AbilityMapper.AbilityMapping();
                	aMAP.abilityName=A.ID();
                	aMAP.autoGain=CMath.s_bool(E.getStat("GETCABLEGAIN"+v));
                    aMAP.defaultProficiency=CMath.s_int(E.getStat("GETCABLEPROF"+v));
                    aMAP.qualLevel=CMath.s_int(E.getStat("GETCABLELVL"+v));
                    aMAP.isSecret=CMath.s_bool(E.getStat("GETCABLESECR"+v));
                    aMAP.maxProficiency=CMath.s_int(E.getStat("GETCABLEMAXP"+v));
                    aMAP.defaultParm=E.getStat("GETCABLEPARM"+v);
                    aMAP.originalSkillPreReqList=E.getStat("GETCABLEPREQ"+v);
                    aMAP.extraMask=E.getStat("GETCABLEMASK"+v);
                    int lvlIndex=levelSets.indexOf(Integer.valueOf(aMAP.qualLevel));
                	Vector set=null;
                    if(lvlIndex<0)
                    {
                        set=new Vector();
                        levelSets.addElement(Integer.valueOf(aMAP.qualLevel),set);
                        if(aMAP.qualLevel>maxAbledLevel)
                            maxAbledLevel=aMAP.qualLevel;
                    }
                    else
                        set=(Vector)levelSets.elementAt(lvlIndex,2);
                    set.addElement(aMAP);
                }
            }
            String header=showNumber+". Class Abilities: ";
            String spaces=CMStrings.repeat(" ",2+(""+showNumber).length());
            parts.append("\n\r");
            parts.append(spaces+CMStrings.padRight("Lvl",3)+" "
                               +CMStrings.padRight("Skill",25)+" "
                               +CMStrings.padRight("Proff",5)+" "
                               +CMStrings.padRight("Gain",5)+" "
                               +CMStrings.padRight("Secret",6)+" "
                               +CMStrings.padRight("Parm",7)+" "
                               +CMStrings.padRight("Preq",7)+" "
                               +CMStrings.padRight("Mask",6)+"\n\r"
                               );
            for(int i=0;i<=maxAbledLevel;i++)
            {
                int index=levelSets.indexOf(Integer.valueOf(i));
                if(index<0) continue;
                Vector set=(Vector)levelSets.elementAt(index,2);
                for(int s=0;s<set.size();s++)
                {
                	AbilityMapper.AbilityMapping aMAP=(AbilityMapper.AbilityMapping)set.elementAt(s);
                    parts.append(spaces+CMStrings.padRight(""+i,3)+" "
                                       +CMStrings.padRight(""+aMAP.abilityName,25)+" "
                                       +CMStrings.padRight(""+aMAP.defaultProficiency,5)+" "
                                       +CMStrings.padRight(""+aMAP.autoGain,5)+" "
                                       +CMStrings.padRight(""+aMAP.isSecret,6)+" "
                                       +CMStrings.padRight(""+aMAP.defaultParm,7)+" "
                                       +CMStrings.padRight(""+aMAP.originalSkillPreReqList,7)+" "
                                       +CMStrings.padRight(""+aMAP.extraMask,6)+"\n\r"
                                       );
                }
            }

            mob.session().wraplessPrintln(header+parts.toString());
            String newName=mob.session().prompt("Enter an ability name to add or remove (?)\n\r:","");
            if(newName.equalsIgnoreCase("?"))
                mob.tell(CMLib.lister().reallyList(CMClass.abilities(),-1).toString());
            else
            if(newName.length()>0)
            {
                int lvlIndex=-1;
                int ableIndex=-1;
                Vector myLevelSet=null;
                for(int s=0;s<levelSets.size();s++)
                {
                    Vector lvls=(Vector)levelSets.elementAt(s,2);
                    for(int l=0;l<lvls.size();l++)
                        if(CMLib.english().containsString(((AbilityMapper.AbilityMapping)lvls.elementAt(l)).abilityName,newName))
                        {
                            lvlIndex=s;
                            ableIndex=l;
                            myLevelSet=lvls;
                            break;
                        }
                    if(lvlIndex>=0) break;
                }
                boolean updateList=false;
                if(ableIndex<0)
                {
                    Ability A=CMClass.getAbility(newName);
                    if(A==null)
                        mob.tell("That is neither an existing ability name, nor a valid one to add.  Use ? for a list.");
                    else
                    {
                        // add new one here
                        if(genClassAbleMod(mob,levelSets,A.ID(),-1,-1)!=null)
                        {
                            mob.tell(A.ID()+" added.");
                            updateList=true;
                            numAbles++;
                        }
                    }
                }
                else
                if(myLevelSet!=null)
                {
                    String aID=((AbilityMapper.AbilityMapping)myLevelSet.elementAt(ableIndex)).abilityName;
                    if(genClassAbleMod(mob,levelSets,aID,lvlIndex,ableIndex)!=null)
                        mob.tell(aID+" modified.");
                    else
                    {
                        mob.tell(aID+" removed.");
                        numAbles--;
                    }

                    updateList=true;
                }
                if(updateList)
                {
                    if(numAbles>0)
                        E.setStat("NUMCABLE",""+numAbles);
                    else
                        E.setStat("NUMCABLE","");
                    int dex=0;
                    for(int s=0;s<levelSets.size();s++)
                    {
                        Integer lvl=(Integer)levelSets.elementAt(s,1);
                        Vector lvls=(Vector)levelSets.elementAt(s,2);
                        for(int l=0;l<lvls.size();l++)
                        {
                        	AbilityMapper.AbilityMapping aMAP=(AbilityMapper.AbilityMapping)lvls.elementAt(l);
                            E.setStat("GETCABLELVL"+dex,lvl.toString());
                            E.setStat("GETCABLEGAIN"+dex,""+aMAP.autoGain);
                            E.setStat("GETCABLEPROF"+dex,""+aMAP.defaultProficiency);
                            E.setStat("GETCABLESECR"+dex,""+aMAP.isSecret);
                            E.setStat("GETCABLEPARM"+dex,""+aMAP.defaultParm);
                            E.setStat("GETCABLEPREQ"+dex,aMAP.originalSkillPreReqList);
                            E.setStat("GETCABLEMASK"+dex,aMAP.extraMask);
                            E.setStat("GETCABLEMAXP"+dex,""+aMAP.maxProficiency);
                            // CABLE MUST BE LAST
                            E.setStat("GETCABLE"+dex,aMAP.abilityName);
                            dex++;
                        }
                    }
                }
            }
            else
            {
                mob.tell("(no change)");
                return;
            }
        }
    }

    protected void genCulturalAbilities(MOB mob, Race E, int showNumber, int showFlag)
        throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(true))
        {
            StringBuffer parts=new StringBuffer("");
            int numResources=CMath.s_int(E.getStat("NUMCABLE"));
            Vector ables=new Vector();
            Vector data=new Vector();
            for(int v=0;v<numResources;v++)
            {
                Ability A=CMClass.getAbility(E.getStat("GETCABLE"+v));
                if(A!=null)
                {
                    parts.append("("+A.ID()+"/"+E.getStat("GETCABLEPROF"+v)+"), ");
                    ables.addElement(A);
                    data.addElement(A.ID()+";"+E.getStat("GETCABLEPROF"+v));
                }
            }
            if(parts.toString().endsWith(", "))
            {parts.deleteCharAt(parts.length()-1);parts.deleteCharAt(parts.length()-1);}
            mob.tell(showNumber+". Cultural Abilities: "+parts.toString()+".");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            String newName=mob.session().prompt("Enter an ability name to add or remove (?)\n\r:","");
            if(newName.equalsIgnoreCase("?"))
                mob.tell(CMLib.lister().reallyList(CMClass.abilities(),-1).toString());
            else
            if(newName.length()>0)
            {
                int partNum=-1;
                for(int i=0;i<ables.size();i++)
                    if(CMLib.english().containsString(((Ability)ables.elementAt(i)).ID(),newName))
                    { partNum=i; break;}
                boolean updateList=false;
                if(partNum<0)
                {
                    Ability A=CMClass.getAbility(newName);
                    if(A==null)
                        mob.tell("That is neither an existing ability name, nor a valid one to add.  Use ? for a list.");
                    else
                    {
                        StringBuffer str=new StringBuffer(A.ID()+";");
                        String prof=mob.session().prompt("Enter the default proficiency level (100): ","100");
                        str.append((""+CMath.s_int(prof)));
                        data.addElement(str.toString());
                        ables.addElement(A);
                        mob.tell(A.name()+" added.");
                        updateList=true;
                    }
                }
                else
                {
                    Ability A=(Ability)ables.elementAt(partNum);
                    ables.removeElementAt(partNum);
                    data.removeElementAt(partNum);
                    updateList=true;
                    mob.tell(A.name()+" removed.");
                }
                if(updateList)
                {
                    if(data.size()>0)
                        E.setStat("NUMCABLE",""+data.size());
                    else
                        E.setStat("NUMCABLE","");
                    for(int i=0;i<data.size();i++)
                    {
                        Vector V=CMParms.parseSemicolons((String)data.elementAt(i),false);
                        E.setStat("GETCABLE"+i,((String)V.elementAt(0)));
                        E.setStat("GETCABLEPROF"+i,((String)V.elementAt(1)));
                    }
                }
            }
            else
            {
                mob.tell("(no change)");
                return;
            }
        }
    }

    public void modifyGenClass(MOB mob, CharClass me)
        throws IOException
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

            promptStatInt(mob,me,++showNumber,showFlag,"Number of Class Names: ","NUMNAME");
            int numNames=CMath.s_int(me.getStat("NUMNAME"));
            if(numNames<=1)
                promptStatStr(mob,me,++showNumber,showFlag,"Class Name","NAME0");
            else
            for(int i=0;i<numNames;i++)
            {
                promptStatStr(mob,me,++showNumber,showFlag,"Class Name #"+i+": ","NAME"+i);
                if(i>0)
                while(!mob.session().killFlag())
                {
                    int oldNameLevel=CMath.s_int(me.getStat("NAMELEVEL"+i));
                    promptStatInt(mob,me,++showNumber,showFlag,"Class Name #"+i+" class level: ","NAMELEVEL"+i);
                    int previousNameLevel=CMath.s_int(me.getStat("NAMELEVEL"+(i-1)));
                    int newNameLevel=CMath.s_int(me.getStat("NAMELEVEL"+i));
                    if((oldNameLevel!=newNameLevel)&&(newNameLevel<(previousNameLevel+1)))
                    {
                        mob.tell("This level may not be less than "+(previousNameLevel+1)+".");
                        me.setStat("NAMELEVEL"+i,""+(previousNameLevel+1));
                        showNumber--;
                    }
                    else
                        break;
                }
            }
            promptStatInt(mob,me,"Use -1 to disable a class Level Cap",++showNumber,showFlag,"Level Cap (?)","LEVELCAP");
            promptStatStr(mob,me,++showNumber,showFlag,"Base Class","BASE");
            genClassAvailability(mob,me,++showNumber,showFlag);
            promptStatInt(mob,me,++showNumber,showFlag,"HP Con Divisor","HPDIV");
            promptStatInt(mob,me,++showNumber,showFlag,"HP Die","HPDICE");
            promptStatInt(mob,me,++showNumber,showFlag,"HP #Dice","HPDIE");
            promptStatInt(mob,me,++showNumber,showFlag,"Mana Divisor","MANADIV");
            promptStatInt(mob,me,++showNumber,showFlag,"Mana #Dice","MANADICE");
            promptStatInt(mob,me,++showNumber,showFlag,"Mana Die","MANADIE");
            promptStatInt(mob,me,++showNumber,showFlag,"Prac/Level","LVLPRAC");
            promptStatInt(mob,me,++showNumber,showFlag,"Attack/Level","LVLATT");
            genAttackAttribute(mob,me,++showNumber,showFlag,"Attack Attribute","ATTATT");
            promptStatInt(mob,me,++showNumber,showFlag,"Practices/1stLvl","FSTPRAC");
            promptStatInt(mob,me,++showNumber,showFlag,"Trains/1stLvl","FSTTRAN");
            promptStatInt(mob,me,++showNumber,showFlag,"Levels/Dmg Pt","LVLDAM");
            promptStatInt(mob,me,++showNumber,showFlag,"Moves/Level","LVLMOVE");
            genArmorCode(mob,me,++showNumber,showFlag,"Armor Restr.","ARMOR");
    		
            int armorMinorCode=CMath.s_int(me.getStat("ARMORMINOR"));
            boolean newSpells=prompt(mob,armorMinorCode>0,++showNumber,showFlag,"Armor restricts only spells");
            me.setStat("ARMORMINOR",""+(newSpells?CMMsg.TYP_CAST_SPELL:-1));

            promptStatStr(mob,me,++showNumber,showFlag,"Limitations","STRLMT");
            promptStatStr(mob,me,++showNumber,showFlag,"Bonuses","STRBON");
            genQualifications(mob,me,++showNumber,showFlag,"Qualifications","QUAL");
            genEStats(mob,me,++showNumber,showFlag);
            genAStats(mob,me,"ASTATS","CharStat Adjustments",++showNumber,showFlag);
            genAStats(mob,me,"CSTATS","CharStat Settings",++showNumber,showFlag);
            genAState(mob,me,"ASTATE","CharState Adjustments",++showNumber,showFlag);
            genAState(mob,me,"STARTASTATE","New Player CharState Adj.",++showNumber,showFlag);
            genClassFlags(mob,me,++showNumber,showFlag);
            genWeaponRestr(mob,me,++showNumber,showFlag,"Weapon Restr.","NUMWEP","GETWEP");
            genWeaponMaterials(mob,me,++showNumber,showFlag,"Weapon Materials","NUMWMAT","GETWMAT");
            genOutfit(mob,me,++showNumber,showFlag);
            genClassBuddy(mob,me,++showNumber,showFlag,"Stat-Modifying Class","STATCLASS");
            genClassBuddy(mob,me,++showNumber,showFlag,"Special Events Class","EVENTCLASS");
            promptStatInt(mob,me,++showNumber,showFlag,"Max Non-Crafting Skills","MAXNCS");
            promptStatInt(mob,me,++showNumber,showFlag,"Max Crafting Skills","MAXCRS");
            promptStatInt(mob,me,++showNumber,showFlag,"Max All-Common Skills","MAXCMS");
            promptStatInt(mob,me,++showNumber,showFlag,"Max Languages","MAXLGS");
            genClassAbilities(mob,me,++showNumber,showFlag);
            promptStatInt(mob,me,++showNumber,showFlag,"Number of Security Code Sets: ","NUMSSET");
            int numGroups=CMath.s_int(me.getStat("NUMSSET"));
            for(int i=0;i<numGroups;i++)
            {
                promptStatStr(mob,me,++showNumber,showFlag,"Security Codes in Set #"+i,"SSET"+i);
                while(!mob.session().killFlag())
                {
                    int oldGroupLevel=CMath.s_int(me.getStat("SSETLEVEL"+i));
                    promptStatInt(mob,me,++showNumber,showFlag,"Class Level for Security Set #"+i+": ","SSETLEVEL"+i);
                    int previousGroupLevel=CMath.s_int(me.getStat("SSETLEVEL"+(i-1)));
                    int newGroupLevel=CMath.s_int(me.getStat("SSETLEVEL"+i));
                    if((oldGroupLevel!=newGroupLevel)
                    &&(i>0)
                    &&(newGroupLevel<(previousGroupLevel+1)))
                    {
                        mob.tell("This level may not be less than "+(previousGroupLevel+1)+".");
                        me.setStat("SSETLEVEL"+i,""+(previousGroupLevel+1));
                        showNumber--;
                    }
                    else
                        break;
                }
            }

            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
    }

    public void modifyGenAbility(MOB mob, Ability me) throws IOException
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
            // id is bad to change.. make them delete it.
            //genText(mob,me,null,++showNumber,showFlag,"Enter the class","CLASS");
            promptStatStr(mob,me,null,++showNumber,showFlag,"Enter an ability name to add or remove","NAME",false);
            promptStatStr(mob,me,CMParms.toStringList(Ability.ACODE_DESCS)+","+CMParms.toStringList(Ability.DOMAIN_DESCS),++showNumber,showFlag,"Type, Domain","CLASSIFICATION",false);
            promptStatStr(mob,me,null,++showNumber,showFlag,"Command Words (comma sep)","TRIGSTR",false);
            promptStatStr(mob,me,CMParms.toStringList(Ability.RANGE_CHOICES),++showNumber,showFlag,"Minimum Range","MINRANGE",false);
            promptStatStr(mob,me,CMParms.toStringList(Ability.RANGE_CHOICES),++showNumber,showFlag,"Maximum Range","MAXRANGE",false);
            promptStatStr(mob,me,null,++showNumber,showFlag,"Ticks Between Casts","TICKSBETWEENCASTS",false);
            promptStatStr(mob,me,null,++showNumber,showFlag,"Affect String","DISPLAY",true);
            promptStatBool(mob,me,++showNumber,showFlag,"Is Auto-invoking","AUTOINVOKE");
            promptStatStr(mob,me,"0,"+CMParms.toStringList(Ability.FLAG_DESCS),++showNumber,showFlag,"Skill Flags (comma sep)","FLAGS",true);
            promptStatInt(mob,me,"-1,x,"+Integer.MAX_VALUE+","+Integer.MAX_VALUE+"-(1 to 100)",++showNumber,showFlag,"Override Cost","OVERRIDEMANA");
            promptStatStr(mob,me,CMParms.toStringList(Ability.USAGE_DESCS),++showNumber,showFlag,"Cost Type","USAGEMASK",false);
            promptStatStr(mob,me,"0,"+CMParms.toStringList(Ability.CAN_DESCS),++showNumber,showFlag,"Can Affect","CANAFFECTMASK",true);
            promptStatStr(mob,me,"0,"+CMParms.toStringList(Ability.CAN_DESCS),++showNumber,showFlag,"Can Target","CANTARGETMASK",true);
            promptStatStr(mob,me,CMParms.toStringList(Ability.QUALITY_DESCS),++showNumber,showFlag,"Quality Code","QUALITY",true);
            promptStatStr(mob,me,"The parameters for this field are LIKE the parameters for this property:\n\r\n\r"+
                    CMLib.help().getHelpText("Prop_HereAdjuster",mob,true).toString(),++showNumber,showFlag,"Affect Adjustments","HERESTATS",true);
            promptStatStr(mob,me,CMLib.masking().maskHelp("\n","disallow"),++showNumber,showFlag,"Caster Mask","CASTMASK",true);
            promptStatStr(mob,me,CMLib.help().getHelpText("Scriptable",mob,true).toString(),++showNumber,showFlag,"Scriptable Parm","SCRIPT",true);
            promptStatStr(mob,me,CMLib.masking().maskHelp("\n","disallow"),++showNumber,showFlag,"Target Mask","TARGETMASK",true);
            promptStatStr(mob,me,null,++showNumber,showFlag,"Fizzle Message","FIZZLEMSG",true);
            promptStatStr(mob,me,null,++showNumber,showFlag,"Auto-Cast Message","AUTOCASTMSG",true);
            promptStatStr(mob,me,null,++showNumber,showFlag,"Normal-Cast Message","CASTMSG",true);
            promptStatStr(mob,me,null,++showNumber,showFlag,"Post-Cast Message","POSTCASTMSG",true);
            promptStatStr(mob,me,CMParms.toStringList(CMMsg.TYPE_DESCS),++showNumber,showFlag,"Attack-Type","ATTACKCODE",true);
            promptStatStr(mob,me,"The parameters for this field are LIKE the parameters for this property:\n\r\n\r"+
                    CMLib.help().getHelpText("Prop_HereSpellCast",mob,true).toString(),++showNumber,showFlag,"Silent affects","POSTCASTAFFECT",true);
            promptStatStr(mob,me,"The parameters for this field are LIKE the parameters for this property:\n\r\n\r"+
                    CMLib.help().getHelpText("Prop_HereSpellCast",mob,true).toString(),++showNumber,showFlag,"Extra castings","POSTCASTABILITY",true);
            promptStatStr(mob,me,"Enter a damage or healing formula. Use +-*/()?. @x1=caster level, @x2=target level.  Formula evaluates >0 for damage, <0 for healing. Requires Can Target!",++showNumber,showFlag,"Damage/Healing Formula","POSTCASTDAMAGE",true);

            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
    }

    protected boolean genText(MOB mob, DVector set, String[] choices, String help, int showNumber, int showFlag, String FieldDisp, String Field)
    throws IOException
    {
        int setDex=set.indexOf(Field);
        if(((showFlag>0)&&(showFlag!=showNumber))||(setDex<0)) return true;
        mob.tell(showNumber+". "+FieldDisp+": '"+((String)set.elementAt(setDex,2)+"'."));
        if((showFlag!=showNumber)&&(showFlag>-999)) return true;
        String newName=mob.session().prompt("Enter a new one\n\r:","");
        if(newName.trim().length()==0)
        {
            mob.tell("(no change)");
            return false;
        }
        if((newName.equalsIgnoreCase("?"))&&(help!=null))
        {
            if((mob.session()==null)||(mob.session().killFlag()))
                return false;
            mob.tell(help);
            return genText(mob,set,choices,help,showNumber,showFlag,FieldDisp,Field);
        }
        if(newName.equalsIgnoreCase("null")) newName="";
        if((choices==null)||(choices.length==0))
        {
            set.setElementAt(setDex,2,newName);
            return true;
        }
        boolean found=false;
        for(int s=0;s<choices.length;s++)
        {
            if(newName.equalsIgnoreCase(choices[s]))
            { newName=choices[s]; found=true; break;}
        }
        if(!found)
        {
            if((mob.session()==null)||(mob.session().killFlag()))
                return false;
            mob.tell(help);
            return genText(mob,set,choices,help,showNumber,showFlag,FieldDisp,Field);
        }
        set.setElementAt(setDex,2,newName);
        return true;
    }

    protected boolean modifyComponent(MOB mob, Vector<AbilityComponent> components, int componentIndex)
    throws IOException
    {
        DVector decoded=CMLib.ableMapper().getAbilityComponentDecodedDVector(components,componentIndex);
        if(mob.isMonster()) return true;
        boolean ok=false;
        int showFlag=-1;
        if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
            showFlag=-999;
        String choices="Your choices are: ";
        String allComponents=CMParms.toStringList(RawMaterial.MATERIAL_DESCS)+","+CMParms.toStringList(RawMaterial.CODES.NAMES());
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
        {
            int showNumber=0;
            genText(mob,decoded,(new String[]{"&&","||","X"}),choices+" &&, ||, X",++showNumber,showFlag,"Conjunction (X Deletes) (?)","ANDOR");
            if(((String)decoded.elementAt(0,2)).equalsIgnoreCase("X")) return false;
            genText(mob,decoded,(new String[]{"INVENTORY","HELD","WORN"}),choices+" INVENTORY, HELD, WORN",++showNumber,showFlag,"Component position (?)","DISPOSITION");
            genText(mob,decoded,(new String[]{"KEPT","CONSUMED"}),choices+" KEPT, CONSUMED",++showNumber,showFlag,"Component fate (?)","FATE");
            genText(mob,decoded,null,null,++showNumber,showFlag,"Amount of component","AMOUNT");
            genText(mob,decoded,null,allComponents,++showNumber,showFlag,"Type of component (?)","COMPONENTID");
            genText(mob,decoded,null,CMLib.masking().maskHelp("\n","disallow"),++showNumber,showFlag,"Component applies-to mask (?)","MASK");
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
        CMLib.ableMapper().setAbilityComponentCodedFromDecodedDVector(decoded,components,componentIndex);
        return true;
    }

    public void modifyComponents(MOB mob, String componentID) throws IOException
    {
        if(mob.isMonster())
            return;
        boolean ok=false;
        int showFlag=-1;
        if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
            showFlag=-999;
        Vector<AbilityComponent> codedDV=CMLib.ableMapper().getAbilityComponentDVector(componentID);
        if(codedDV!=null)
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
        {
            int showNumber=0;
            for(int v=0;v<codedDV.size();v++)
                if((mob.session()!=null)&&(!mob.session().killFlag()))
                {
                    showNumber++;
                    if((showFlag>0)&&(showFlag!=showNumber)) continue;
                    mob.tell(showNumber+": '"+CMLib.ableMapper().getAbilityComponentDesc(null,codedDV,v)+"'.");
                    if((showFlag!=showNumber)&&(showFlag>-999)) continue;
                    if(!modifyComponent(mob,codedDV,v))
                    {
                        codedDV.removeElementAt(v);
                        v--;
                    }
                }
            while((mob.session()!=null)&&(!mob.session().killFlag()))
            {
                showNumber++;
                mob.tell(showNumber+". Add new component requirement.");
                if((showFlag==showNumber)||(showFlag<=-999))
                {
                    CMLib.ableMapper().addBlankAbilityComponent(codedDV);
                    boolean success=modifyComponent(mob,codedDV,codedDV.size()-1);
                    if(!success)
                        codedDV.removeElementAt(codedDV.size()-1);
                    else
                    if(showFlag<=-999)
                        continue;
                }
                break;
            }
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
    }

    public void modifyGenRace(MOB mob, Race me) throws IOException
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
            promptStatStr(mob,me,++showNumber,showFlag,"Name","NAME");
            genCat(mob,me,++showNumber,showFlag);
            promptStatInt(mob,me,++showNumber,showFlag,"Base Weight","BWEIGHT");
            promptStatInt(mob,me,++showNumber,showFlag,"Weight Variance","VWEIGHT");
            promptStatInt(mob,me,++showNumber,showFlag,"Base Male Height","MHEIGHT");
            promptStatInt(mob,me,++showNumber,showFlag,"Base Female Height","FHEIGHT");
            promptStatInt(mob,me,++showNumber,showFlag,"Height Variance","VHEIGHT");
            genRaceAvailability(mob,me,++showNumber,showFlag);
            genDisableFlags(mob,me,++showNumber,showFlag);
            promptStatStr(mob,me,++showNumber,showFlag,"Leaving text","LEAVE");
            promptStatStr(mob,me,++showNumber,showFlag,"Arriving text","ARRIVE");
            genRaceBuddy(mob,me,++showNumber,showFlag,"Health Race","HEALTHRACE");
            genRaceBuddy(mob,me,++showNumber,showFlag,"Event Race","EVENTRACE");
            genBodyParts(mob,me,++showNumber,showFlag);
            genRaceWearFlags(mob,me,++showNumber,showFlag);
            genAgingChart(mob,me,++showNumber,showFlag);
            promptStatBool(mob,me,++showNumber,showFlag,"Never create corpse","BODYKILL");
            genEStats(mob,me,++showNumber,showFlag);
            genAStats(mob,me,"ASTATS","CharStat Adjustments",++showNumber,showFlag);
            genAStats(mob,me,"CSTATS","CharStat Settings",++showNumber,showFlag);
            genAState(mob,me,"ASTATE","CharState Adjustments",++showNumber,showFlag);
            genAState(mob,me,"STARTASTATE","New Player CharState Adj.",++showNumber,showFlag);
            genResources(mob,me,++showNumber,showFlag);
            genOutfit(mob,me,++showNumber,showFlag);
            genWeapon(mob,me,++showNumber,showFlag);
            genRaceBuddy(mob,me,++showNumber,showFlag,"Weapons Race","WEAPONRACE");
            genRacialAbilities(mob,me,++showNumber,showFlag);
            genCulturalAbilities(mob,me,++showNumber,showFlag);
            //genRacialEffects(mob,me,++showNumber,showFlag);
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
    }

    protected void modifyGenItem(MOB mob, Item me)
        throws IOException
    {
        boolean ok=false;
        int showFlag=-1;
        if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
            showFlag=-999;
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
        {
            if(mob.isMonster()) return;
            int showNumber=0;
            genName(mob,me,++showNumber,showFlag);
            genDisplayText(mob,me,++showNumber,showFlag);
            genDescription(mob,me,++showNumber,showFlag);
            genLevel(mob,me,++showNumber,showFlag);
            genSecretIdentity(mob,me,++showNumber,showFlag);
            genMaterialCode(mob,me,++showNumber,showFlag);
            if(me instanceof ClanItem)
                genClanItem(mob,(ClanItem)me,++showNumber,showFlag);
            if(me instanceof ShipComponent)
            {
                if(me instanceof ShipComponent.ShipPanel)
                    genPanelType(mob,(ShipComponent.ShipPanel)me,++showNumber,showFlag);
            }
            if(me instanceof PackagedItems)
                ((PackagedItems)me).setNumberOfItemsInPackage(prompt(mob,((PackagedItems)me).numberOfItemsInPackage(),++showNumber,showFlag,"Number of items in the package"));
            genGettable(mob,me,++showNumber,showFlag);
            genReadable1(mob,me,++showNumber,showFlag);
            genReadable2(mob,me,++showNumber,showFlag);
            if(me instanceof Recipe) genRecipe(mob,(Recipe)me,++showNumber,showFlag);
            if(me instanceof Light) genBurnout(mob,(Light)me,++showNumber,showFlag);
            genRejuv(mob,me,++showNumber,showFlag);
            if(me instanceof Coins)
                genCoinStuff(mob,(Coins)me,++showNumber,showFlag);
            else
                genAbility(mob,me,++showNumber,showFlag);
            genUses(mob,me,++showNumber,showFlag);
            if(me instanceof Wand)
                genMaxUses(mob,(Wand)me,++showNumber,showFlag);
            genValue(mob,me,++showNumber,showFlag);
            genWeight(mob,me,++showNumber,showFlag);
            genDisposition(mob,me.baseEnvStats(),++showNumber,showFlag);
            genBehaviors(mob,me,++showNumber,showFlag);
            genAffects(mob,me,++showNumber,showFlag);
            if(me instanceof LandTitle)
                genTitleRoom(mob,(LandTitle)me,++showNumber,showFlag);
            genImage(mob,me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
        me.recoverEnvStats();
        if(me.text().length()>=maxLength)
            mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.");
    }

    protected void modifyGenFood(MOB mob, Food me)
        throws IOException
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
            genName(mob,me,++showNumber,showFlag);
            genDisplayText(mob,me,++showNumber,showFlag);
            genDescription(mob,me,++showNumber,showFlag);
            genSecretIdentity(mob,me,++showNumber,showFlag);
            genLevel(mob,me,++showNumber,showFlag);
            genValue(mob,me,++showNumber,showFlag);
            genRejuv(mob,me,++showNumber,showFlag);
            genWeight(mob,me,++showNumber,showFlag);
            genMaterialCode(mob,me,++showNumber,showFlag);
            genNourishment(mob,me,++showNumber,showFlag);
            genBiteSize(mob,me,++showNumber,showFlag);
            genDisposition(mob,me.baseEnvStats(),++showNumber,showFlag);
            genGettable(mob,me,++showNumber,showFlag);
            genReadable1(mob,me,++showNumber,showFlag);
            genReadable2(mob,me,++showNumber,showFlag);
            if(me instanceof Light) genBurnout(mob,(Light)me,++showNumber,showFlag);
            genBehaviors(mob,me,++showNumber,showFlag);
            genAffects(mob,me,++showNumber,showFlag);
            genImage(mob,me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
        me.recoverEnvStats();
        if(me.text().length()>=maxLength)
            mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.");
    }

    protected void genScripts(MOB mob, MOB E, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        String behave="NO";
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(behave.length()>0))
        {
            String behaviorstr="";
            for(int b=0;b<E.numScripts();b++)
            {
                ScriptingEngine B=E.fetchScript(b);
                if(B!=null) behaviorstr+=(b+1)+":"+B.defaultQuestName()+", ";
            }
            if(behaviorstr.length()>0)
                behaviorstr=behaviorstr.substring(0,behaviorstr.length()-2);
            mob.tell(showNumber+". Scripts: '"+behaviorstr+"'.");
            if((showFlag!=showNumber)&&(showFlag>-999)) return;
            behave=mob.session().prompt("Enter a script number to remove\n\r:","");
            if(behave.length()>0)
            {
                String tattoo=behave;
                if((tattoo.length()>0)
                &&(CMath.isInteger(tattoo))
                &&(CMath.s_int(tattoo)>0)
                &&(CMath.s_int(tattoo)<=E.numScripts()))
                {
                	int x=CMath.s_int(tattoo);
                    mob.tell("Script #"+x+" removed.");
                    E.delScript(E.fetchScript(x-1));
                }
            }
            else
                mob.tell("(no change)");
        }
    }
    
    protected void modifyGenDrink(MOB mob, Drink me)
        throws IOException
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
            genName(mob,me,++showNumber,showFlag);
            genDisplayText(mob,me,++showNumber,showFlag);
            genDescription(mob,me,++showNumber,showFlag);
            genSecretIdentity(mob,(Item)me,++showNumber,showFlag);
            genValue(mob,(Item)me,++showNumber,showFlag);
            genLevel(mob,me,++showNumber,showFlag);
            genWeight(mob,me,++showNumber,showFlag);
            genRejuv(mob,me,++showNumber,showFlag);
            genThirstQuenched(mob,me,++showNumber,showFlag);
            genMaterialCode(mob,(Item)me,++showNumber,showFlag);
            genDrinkHeld(mob,me,++showNumber,showFlag);
            genGettable(mob,(Item)me,++showNumber,showFlag);
            genReadable1(mob,(Item)me,++showNumber,showFlag);
            genReadable2(mob,(Item)me,++showNumber,showFlag);
            if(me instanceof Light) genBurnout(mob,(Light)me,++showNumber,showFlag);
            genBehaviors(mob,me,++showNumber,showFlag);
            genAffects(mob,me,++showNumber,showFlag);
            genDisposition(mob,me.baseEnvStats(),++showNumber,showFlag);
            if(me instanceof Container)
                genCapacity(mob,(Container)me,++showNumber,showFlag);
            if(me instanceof Perfume)
                ((Perfume)me).setSmellList(prompt(mob,((Perfume)me).getSmellList(),++showNumber,showFlag,"Smells list (; delimited)"));
            genImage(mob,me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
        me.recoverEnvStats();
        if(me.text().length()>=maxLength)
            mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.");
    }

    protected void modifyGenWallpaper(MOB mob, Item me)
        throws IOException
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
            genName(mob,me,++showNumber,showFlag);
            genDescription(mob,me,++showNumber,showFlag);
            genReadable1(mob,me,++showNumber,showFlag);
            genReadable2(mob,me,++showNumber,showFlag);
            if(me instanceof Light) genBurnout(mob,(Light)me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
        me.recoverEnvStats();
        if(me.text().length()>=maxLength)
            mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.");
    }

    protected void modifyGenMap(MOB mob, com.planet_ink.coffee_mud.Items.interfaces.Map me)
        throws IOException
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
            genName(mob,me,++showNumber,showFlag);
            genDisplayText(mob,me,++showNumber,showFlag);
            genDescription(mob,me,++showNumber,showFlag);
            genLevel(mob,me,++showNumber,showFlag);
            genSecretIdentity(mob,me,++showNumber,showFlag);
            genGettable(mob,me,++showNumber,showFlag);
            genReadable1(mob,me,++showNumber,showFlag);
            genReadable2(mob,me,++showNumber,showFlag);
            genValue(mob,me,++showNumber,showFlag);
            genWeight(mob,me,++showNumber,showFlag);
            genRejuv(mob,me,++showNumber,showFlag);
            genMaterialCode(mob,me,++showNumber,showFlag);
            genDisposition(mob,me.baseEnvStats(),++showNumber,showFlag);
            genBehaviors(mob,me,++showNumber,showFlag);
            genAffects(mob,me,++showNumber,showFlag);
            genImage(mob,me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
        me.recoverEnvStats();
        if(me.text().length()>=maxLength)
            mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.");
    }

    protected void modifyGenContainer(MOB mob, Container me)
        throws IOException
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
            genName(mob,me,++showNumber,showFlag);
            genDisplayText(mob,me,++showNumber,showFlag);
            genDescription(mob,me,++showNumber,showFlag);
            genLevel(mob,me,++showNumber,showFlag);
            genRejuv(mob,me,++showNumber,showFlag);
            genCapacity(mob,me,++showNumber,showFlag);
            if(me instanceof ShipComponent)
            {
                if(me instanceof ShipComponent.ShipPanel)
                    genPanelType(mob,(ShipComponent.ShipPanel)me,++showNumber,showFlag);
            }
            genLidsNLocks(mob,me,++showNumber,showFlag);
            genMaterialCode(mob,me,++showNumber,showFlag);
            genSecretIdentity(mob,me,++showNumber,showFlag);
            genValue(mob,me,++showNumber,showFlag);
            genUses(mob,me,++showNumber,showFlag);
            genWeight(mob,me,++showNumber,showFlag);
            genDisposition(mob,me.baseEnvStats(),++showNumber,showFlag);
            if(me instanceof DeadBody)
                genCorpseData(mob,(DeadBody)me,++showNumber,showFlag);
            if(me instanceof ClanItem)
                genClanItem(mob,(ClanItem)me,++showNumber,showFlag);
            genGettable(mob,me,++showNumber,showFlag);
            genReadable1(mob,me,++showNumber,showFlag);
            genReadable2(mob,me,++showNumber,showFlag);
            if(me instanceof Light) genBurnout(mob,(Light)me,++showNumber,showFlag);
            genBehaviors(mob,me,++showNumber,showFlag);
            genAffects(mob,me,++showNumber,showFlag);
            if(me instanceof Rideable)
            {
                genRideable1(mob,(Rideable)me,++showNumber,showFlag);
                genRideable2(mob,(Rideable)me,++showNumber,showFlag);
            }
            if(me instanceof Exit)
            {
                genDoorName(mob,(Exit)me,++showNumber,showFlag);
                genClosedText(mob,(Exit)me,++showNumber,showFlag);
            }
            genImage(mob,me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
        me.recoverEnvStats();
        if(me.text().length()>=maxLength)
            mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.");
    }

    protected void modifyGenWeapon(MOB mob, Weapon me)
        throws IOException
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
            genName(mob,me,++showNumber,showFlag);
            genDisplayText(mob,me,++showNumber,showFlag);
            genDescription(mob,me,++showNumber,showFlag);
            int oldLevel = me.baseEnvStats().level();
            genLevel(mob,me,++showNumber,showFlag);
            if(me.baseEnvStats().level() != oldLevel)
            	CMLib.itemBuilder().balanceItemByLevel(me);
            genAttack(mob,me,++showNumber,showFlag);
            genDamage(mob,me,++showNumber,showFlag);
            genMaterialCode(mob,me,++showNumber,showFlag);
            genWeaponType(mob,me,++showNumber,showFlag);
            genWeaponClassification(mob,me,++showNumber,showFlag);
            genWeaponRanges(mob,me,++showNumber,showFlag);
            if(me instanceof Wand)
            {
                genReadable1(mob,me,++showNumber,showFlag);
                genReadable2(mob,me,++showNumber,showFlag);
                genUses(mob,me,++showNumber,showFlag);
                genMaxUses(mob,(Wand)me,++showNumber,showFlag);
                if(me instanceof Light) genBurnout(mob,(Light)me,++showNumber,showFlag);
            }
            else
                genWeaponAmmo(mob,me,++showNumber,showFlag);
            genRejuv(mob,me,++showNumber,showFlag);
            if((!me.requiresAmmunition())&&(!(me instanceof Wand)))
                genCondition(mob,me,++showNumber,showFlag);
            genAbility(mob,me,++showNumber,showFlag);
            genSecretIdentity(mob,me,++showNumber,showFlag);
            if(me instanceof ClanItem)
                genClanItem(mob,(ClanItem)me,++showNumber,showFlag);
            genGettable(mob,me,++showNumber,showFlag);
            genValue(mob,me,++showNumber,showFlag);
            genWeight(mob,me,++showNumber,showFlag);
            genDisposition(mob,me.baseEnvStats(),++showNumber,showFlag);
            genBehaviors(mob,me,++showNumber,showFlag);
            genAffects(mob,me,++showNumber,showFlag);
            genImage(mob,me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
        me.recoverEnvStats();
        if(me.text().length()>=maxLength)
            mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.");
    }

    protected void modifyGenArmor(MOB mob, Armor me)
        throws IOException
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
            genName(mob,me,++showNumber,showFlag);
            genDisplayText(mob,me,++showNumber,showFlag);
            genDescription(mob,me,++showNumber,showFlag);
            int oldLevel = me.baseEnvStats().level();
            genLevel(mob,me,++showNumber,showFlag);
            if(me.baseEnvStats().level() != oldLevel)
            	CMLib.itemBuilder().balanceItemByLevel(me);
            genMaterialCode(mob,me,++showNumber,showFlag);
            genWornLocation(mob,me,++showNumber,showFlag);
            genLayer(mob,me,++showNumber,showFlag);
            genRejuv(mob,me,++showNumber,showFlag);
            genArmor(mob,me,++showNumber,showFlag);
            genCondition(mob,me,++showNumber,showFlag);
            genAbility(mob,me,++showNumber,showFlag);
            genSecretIdentity(mob,me,++showNumber,showFlag);
            if(me instanceof ClanItem)
                genClanItem(mob,(ClanItem)me,++showNumber,showFlag);
            genGettable(mob,me,++showNumber,showFlag);
            genCapacity(mob,me,++showNumber,showFlag);
            genLidsNLocks(mob,me,++showNumber,showFlag);
            //genReadable1(mob,me,++showNumber,showFlag); // since they can have keys, no readability for you.
            //genReadable2(mob,me,++showNumber,showFlag);
            if(me instanceof Light) genBurnout(mob,(Light)me,++showNumber,showFlag);
            genValue(mob,me,++showNumber,showFlag);
            genWeight(mob,me,++showNumber,showFlag);
            genSize(mob,me,++showNumber,showFlag);
            genDisposition(mob,me.baseEnvStats(),++showNumber,showFlag);
            genBehaviors(mob,me,++showNumber,showFlag);
            genAffects(mob,me,++showNumber,showFlag);
            genImage(mob,me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
        me.recoverEnvStats();
        if(me.text().length()>=maxLength)
            mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.");
    }


    protected void modifyGenInstrument(MOB mob, MusicalInstrument me)
        throws IOException
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
            genName(mob,me,++showNumber,showFlag);
            genDisplayText(mob,me,++showNumber,showFlag);
            genDescription(mob,me,++showNumber,showFlag);
            genLevel(mob,me,++showNumber,showFlag);
            genMaterialCode(mob,me,++showNumber,showFlag);
            genWornLocation(mob,me,++showNumber,showFlag);
            genRejuv(mob,me,++showNumber,showFlag);
            genAbility(mob,me,++showNumber,showFlag);
            genSecretIdentity(mob,me,++showNumber,showFlag);
            genGettable(mob,me,++showNumber,showFlag);
            genInstrumentType(mob,me,++showNumber,showFlag);
            genValue(mob,me,++showNumber,showFlag);
            genWeight(mob,me,++showNumber,showFlag);
            genDisposition(mob,me.baseEnvStats(),++showNumber,showFlag);
            genBehaviors(mob,me,++showNumber,showFlag);
            genAffects(mob,me,++showNumber,showFlag);
            genImage(mob,me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
        me.recoverEnvStats();
        if(me.text().length()>=maxLength)
            mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.");
    }


    public void modifyGenExit(MOB mob, Exit me)
        throws IOException
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
            genName(mob,me,++showNumber,showFlag);
            genDisplayText(mob,me,++showNumber,showFlag);
            genDescription(mob,me,++showNumber,showFlag);
            genLevel(mob,me,++showNumber,showFlag);
            genDoorsNLocks(mob,me,++showNumber,showFlag);
            if(me.hasADoor())
            {
                genClosedText(mob,me,++showNumber,showFlag);
                genDoorName(mob,me,++showNumber,showFlag);
                genOpenWord(mob,me,++showNumber,showFlag);
                genCloseWord(mob,me,++showNumber,showFlag);
            }
            genExitMisc(mob,me,++showNumber,showFlag);
            genDisposition(mob,me.baseEnvStats(),++showNumber,showFlag);
            genBehaviors(mob,me,++showNumber,showFlag);
            genAffects(mob,me,++showNumber,showFlag);
            genImage(mob,me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
        me.recoverEnvStats();
        if(me.text().length()>=maxLength)
            mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.");
    }


    protected void modifyGenMOB(MOB mob, MOB me)
        throws IOException
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
            genName(mob,me,++showNumber,showFlag);
            genDisplayText(mob,me,++showNumber,showFlag);
            genDescription(mob,me,++showNumber,showFlag);
            int oldLevel=me.baseEnvStats().level();
            genLevel(mob,me,++showNumber,showFlag);
            if((oldLevel<2)&&(me.baseEnvStats().level()>1))
                CMLib.leveler().fillOutMOB(me,me.baseEnvStats().level());
            genRejuv(mob,me,++showNumber,showFlag);
            genRace(mob,me,++showNumber,showFlag);
        	CMLib.factions().updatePlayerFactions(me,me.location());
            Faction F=null;
            for(Enumeration e=CMLib.factions().factions();e.hasMoreElements();)
            {
                F=(Faction)e.nextElement();
                if(F.showInEditor())
                    genSpecialFaction(mob,me,++showNumber,showFlag,F);
            }
            genGender(mob,me,++showNumber,showFlag);
            genHeight(mob,me,++showNumber,showFlag);
            genWeight(mob,me,++showNumber,showFlag);
            genClan(mob,me,++showNumber,showFlag);
            genSpeed(mob,me,++showNumber,showFlag);
            if((oldLevel<2)&&(me.baseEnvStats().level()>1))
                me.baseEnvStats().setDamage((int)Math.round(CMath.div(me.baseEnvStats().damage(),me.baseEnvStats().speed())));
            genAttack(mob,me,++showNumber,showFlag);
            genDamage(mob,me,++showNumber,showFlag);
            genArmor(mob,me,++showNumber,showFlag);
            genHitPoints(mob,me,++showNumber,showFlag);
            genMoney(mob,me,++showNumber,showFlag);
            me.setMoneyVariation(CMath.s_double(prompt(mob,""+me.getMoneyVariation(),++showNumber,showFlag,"Money Variation")));
            genAbilities(mob,me,++showNumber,showFlag);
            genBehaviors(mob,me,++showNumber,showFlag);
            genAffects(mob,me,++showNumber,showFlag);
            genDisposition(mob,me.baseEnvStats(),++showNumber,showFlag);
            genSensesMask(mob,me.baseEnvStats(),++showNumber,showFlag);
            if(me instanceof Rideable)
            {
                genRideable1(mob,(Rideable)me,++showNumber,showFlag);
                genRideable2(mob,(Rideable)me,++showNumber,showFlag);
            }
            if(me instanceof Deity)
            {
                genDeity1(mob,(Deity)me,++showNumber,showFlag);
                genDeity2(mob,(Deity)me,++showNumber,showFlag);
                genDeity3(mob,(Deity)me,++showNumber,showFlag);
                genDeity4(mob,(Deity)me,++showNumber,showFlag);
                genDeity5(mob,(Deity)me,++showNumber,showFlag);
                genDeity8(mob,(Deity)me,++showNumber,showFlag);
                genDeity9(mob,(Deity)me,++showNumber,showFlag);
                genDeity6(mob,(Deity)me,++showNumber,showFlag);
                genDeity0(mob,(Deity)me,++showNumber,showFlag);
                genDeity7(mob,(Deity)me,++showNumber,showFlag);
                genDeity11(mob,(Deity)me,++showNumber,showFlag);
            }
            genFaction(mob,me,++showNumber,showFlag);
            genTattoos(mob,me,++showNumber,showFlag);
            genExpertises(mob,me,++showNumber,showFlag);
            genImage(mob,me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
        me.recoverCharStats();
        me.recoverMaxState();
        me.recoverEnvStats();
        me.resetToMaxState();
        if(me.text().length()>=maxLength)
            mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.");
    }

    public void modifyPlayer(MOB mob, MOB me) throws IOException
    {
        if(mob.isMonster())
            return;
        boolean ok=false;
        int showFlag=-1;
        if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
            showFlag=-999;
        String oldName=me.Name();
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
        {
            int showNumber=0;
            genName(mob,me,++showNumber,showFlag);
            while((!me.Name().equals(oldName))&&(CMLib.players().playerExists(me.Name()))
            &&(mob.session()!=null)&&(!mob.session().killFlag()))
            {
                mob.tell("The name given cannot be chosen, as it is already being used.");
                genName(mob,me,showNumber,showFlag);
            }
            if(CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)>1)
            {
            	String oldAccountName = ((me.playerStats()!=null)&&(me.playerStats().getAccount()!=null))?me.playerStats().getAccount().accountName():"";
            	String accountName =CMStrings.capitalizeAndLower(prompt(mob,oldAccountName,++showNumber,showFlag,"Account",true,false,null));
	            while((!accountName.equals(oldAccountName))&&(CMLib.players().getLoadAccount(accountName)==null)
	            &&(mob.session()!=null)&&(!mob.session().killFlag()))
	            {
	                mob.tell("The account can not be used, as it does not exist.");
	                accountName =CMStrings.capitalizeAndLower(prompt(mob,oldAccountName,showNumber,showFlag,"Account",true,false,null));
	            }
	            if(!oldAccountName.equals(accountName))
	            {
	            	PlayerAccount newAccount = CMLib.players().getLoadAccount(accountName);
	            	me.playerStats().setAccount(newAccount);
	            	newAccount.addNewPlayer(me);
	            	PlayerAccount oldAccount = CMLib.players().getLoadAccount(oldAccountName);
	            	if(oldAccount!=null)
	            	{
	            		oldAccount.delPlayer(me);
	            		CMLib.database().DBUpdateAccount(oldAccount);
	            	}
	            }
	            if(CMProps.getBoolVar(CMProps.SYSTEMB_ACCOUNTEXPIRATION))
	            	genAccountExpiration(mob,me.playerStats().getAccount(),++showNumber,showFlag);
            }
            else
            if(CMProps.getBoolVar(CMProps.SYSTEMB_ACCOUNTEXPIRATION))
            	genAccountExpiration(mob,me.playerStats(),++showNumber,showFlag);
            genPassword(mob,me,++showNumber,showFlag);

            genDescription(mob,me,++showNumber,showFlag);
            genLevel(mob,me,++showNumber,showFlag);
            genRace(mob,me,++showNumber,showFlag);
            genCharClass(mob,me,++showNumber,showFlag);
            genCharStats(mob,me,++showNumber,showFlag);
        	CMLib.factions().updatePlayerFactions(me,me.location());
            Faction F=null;
            for(Enumeration e=CMLib.factions().factions();e.hasMoreElements();)
            {
                F=(Faction)e.nextElement();
                if(F.showInEditor())
                    genSpecialFaction(mob,me,++showNumber,showFlag,F);
            }
            genGender(mob,me,++showNumber,showFlag);
            genHeight(mob,me,++showNumber,showFlag);
            genWeight(mob,me,++showNumber,showFlag);
            genClan(mob,me,++showNumber,showFlag);
            genDeity(mob,me,++showNumber,showFlag);
            genSpeed(mob,me,++showNumber,showFlag);
            genAttack(mob,me,++showNumber,showFlag);
            genDamage(mob,me,++showNumber,showFlag);
            genArmor(mob,me,++showNumber,showFlag);
            genHitPoints(mob,me,++showNumber,showFlag);
            genMoney(mob,me,++showNumber,showFlag);
            me.setTrains(prompt(mob,me.getTrains(),++showNumber,showFlag,"Training Points"));
            me.setPractices(prompt(mob,me.getPractices(),++showNumber,showFlag,"Practice Points"));
            me.setQuestPoint(prompt(mob,me.getQuestPoint(),++showNumber,showFlag,"Quest Points"));
            genAbilities(mob,me,++showNumber,showFlag);
            genAffects(mob,me,++showNumber,showFlag);
            genBehaviors(mob,me,++showNumber,showFlag);
            genDisposition(mob,me.baseEnvStats(),++showNumber,showFlag);
            genSensesMask(mob,me.baseEnvStats(),++showNumber,showFlag);
            if(me instanceof Rideable)
            {
                genRideable1(mob,(Rideable)me,++showNumber,showFlag);
                genRideable2(mob,(Rideable)me,++showNumber,showFlag);
            }
            genFaction(mob,me,++showNumber,showFlag);
            genTattoos(mob,me,++showNumber,showFlag);
            genExpertises(mob,me,++showNumber,showFlag);
            genTitles(mob,me,++showNumber,showFlag);
            genEmail(mob,me.playerStats(),++showNumber,showFlag);
            genSecurity(mob,me,++showNumber,showFlag);
            genImage(mob,me,++showNumber,showFlag);
            genScripts(mob,me,++showNumber,showFlag);
            genNotes(mob,me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
            if(me.playerStats()!=null)
            for(int x=me.playerStats().getSaveStatIndex();x<me.playerStats().getStatCodes().length;x++)
                me.playerStats().setStat(me.playerStats().getStatCodes()[x],prompt(mob,me.playerStats().getStat(me.playerStats().getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.playerStats().getStatCodes()[x])));
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
        me.recoverCharStats();
        me.recoverMaxState();
        me.recoverEnvStats();
        me.resetToMaxState();
        if(!oldName.equals(me.Name()))
        {
            MOB fakeMe=(MOB)me.copyOf();
            fakeMe.setName(oldName);
            CMLib.database().DBDeleteMOB(fakeMe);
            CMLib.database().DBCreateCharacter(me);
            PlayerStats pstats = me.playerStats();
            if(pstats != null)
            {
            	PlayerAccount account = pstats.getAccount();
            	if(account != null)
            	{
            		account.delPlayer(fakeMe);
            		account.addNewPlayer(me);
            	}
            }
        }
        CMLib.database().DBUpdatePlayer(me);
        CMLib.database().DBUpdateFollowers(me);
    }


    protected void genClanStatus(MOB mob, Clan C, int showNumber, int showFlag)
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Clan Status: "+Clan.CLANSTATUS_DESC[C.getStatus()]);
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        switch(C.getStatus())
        {
        case Clan.CLANSTATUS_ACTIVE:
            C.setStatus(Clan.CLANSTATUS_PENDING);
            mob.tell("Clan '"+C.name()+"' has been changed from active to pending!");
            break;
        case Clan.CLANSTATUS_PENDING:
            C.setStatus(Clan.CLANSTATUS_ACTIVE);
            mob.tell("Clan '"+C.name()+"' has been changed from pending to active!");
            break;
        case Clan.CLANSTATUS_FADING:
            C.setStatus(Clan.CLANSTATUS_ACTIVE);
            mob.tell("Clan '"+C.name()+"' has been changed from fading to active!");
            break;
        default:
            mob.tell("Clan '"+C.name()+"' has not been changed!");
            break;
        }
    }

    protected void genClanGovt(MOB mob, Clan C, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Government type: '"+C.typeName()+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        while((mob.session()!=null)&&(!mob.session().killFlag()))
        {
            String newName=mob.session().prompt("Enter a new one (?)\n\r:","");
            if(newName.trim().length()==0)
            {
                mob.tell("(no change)");
                return;
            }
            int newGovt=-1;
            StringBuffer gvts=new StringBuffer();
            for(int i=0;i<Clan.GVT_DESCS.length;i++)
            {
                gvts.append(Clan.GVT_DESCS[i]+", ");
                if(newName.equalsIgnoreCase(Clan.GVT_DESCS[i]))
                    newGovt=i;
            }
            gvts=new StringBuffer(gvts.substring(0,gvts.length()-2));
            if(newGovt<0)
                mob.tell("That government type is invalid.  Valid types include: "+gvts.toString());
            else
            {
                C.setGovernment(newGovt);
                break;
            }
        }
    }

    protected double genAuctionPrompt(MOB mob, double oldVal, int showNumber, int showFlag, String msg, boolean pct)
    throws IOException
    {
        String oldStr=(oldVal<0)?"":(pct?""+(oldVal*100.0)+"%":""+oldVal);
        String newStr=prompt(mob,oldStr,showNumber,showFlag,msg);
        if(newStr.trim().length()==0)
            return -1.0;
        if((pct)&&(!CMath.isPct(newStr))&&(!CMath.isNumber(newStr)))
            return -1.0;
        else
        if((!pct)&&(!CMath.isNumber(newStr)))
            return -1.0;
        if(pct) return CMath.s_pct(newStr);
        return CMath.s_double(newStr);
    }

    protected int genAuctionPrompt(MOB mob, int oldVal, int showNumber, int showFlag, String msg)
    throws IOException
    {
        String oldStr=(oldVal<0)?"":""+oldVal;
        String newStr=prompt(mob,oldStr,showNumber,showFlag,msg);
        if(newStr.trim().length()==0)
            return -1;
        if(!CMath.isNumber(newStr))
            return -1;
        return CMath.s_int(newStr);
    }


    protected void genClanRole(MOB mob, Clan C, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Clan (Role): '"+CMLib.clans().getRoleName(C.getGovernment(),C.getAutoPosition(),true,false)+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        while((mob.session()!=null)&&(!mob.session().killFlag()))
        {
            String newName=mob.session().prompt("Enter a new one (?)\n\r:","");
            if(newName.trim().length()==0)
            {
                mob.tell("(no change)");
                return;
            }
            int newRole=-1;
            StringBuffer roles=new StringBuffer();
            for(int i=0;i<Clan.ROL_DESCS[C.getGovernment()].length;i++)
            {
                roles.append(Clan.ROL_DESCS[C.getGovernment()][i]+", ");
                if(newName.equalsIgnoreCase(Clan.ROL_DESCS[C.getGovernment()][i]))
                    newRole=Clan.POSORDER[i];
            }
            roles=new StringBuffer(roles.substring(0,roles.length()-2));
            if(newRole<0)
                mob.tell("That role is invalid.  Valid roles include: "+roles.toString());
            else
            {
                C.setAutoPosition(newRole);
                break;
            }
        }
    }

    protected void genClanClass(MOB mob, Clan C, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        CharClass CC=CMClass.getCharClass(C.getClanClass());
        if(CC==null)CC=CMClass.findCharClass(C.getClanClass());
        String clasName=(CC==null)?"NONE":CC.name();
        mob.tell(showNumber+". Clan Auto-Class: '"+clasName+"'.");
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        while((mob.session()!=null)&&(!mob.session().killFlag()))
        {
            String newName=mob.session().prompt("Enter a new one (?)\n\r:","");
            if(newName.trim().equalsIgnoreCase("none"))
            {
                C.setClanClass("");
                return;
            }
            else
            if(newName.trim().length()==0)
            {
                mob.tell("(no change)");
                return;
            }
            CharClass newC=null;
            StringBuffer clss=new StringBuffer();
            for(Enumeration e=CMClass.charClasses();e.hasMoreElements();)
            {
                CC=(CharClass)e.nextElement();
                clss.append(CC.name()+", ");
                if(newName.equalsIgnoreCase(CC.name())||(newName.equalsIgnoreCase(CC.ID())))
                    newC=CC;
            }
            clss=new StringBuffer(clss.substring(0,clss.length()-2));
            if(newC==null)
                mob.tell("That class name is invalid.  Valid names include: "+clss.toString());
            else
            {
                C.setClanClass(newC.ID());
                break;
            }
        }
    }

    protected String genClanRoom(MOB mob, Clan C, String oldRoomID, String promptCode, int showNumber, int showFlag)
    throws IOException
    {
        if((showFlag>0)&&(showFlag!=showNumber)) return oldRoomID;
        mob.tell(showNumber+CMStrings.replaceAll(promptCode,"@x1",oldRoomID));
        if((showFlag!=showNumber)&&(showFlag>-999)) return oldRoomID;
        while((mob.session()!=null)&&(!mob.session().killFlag()))
        {
            String newName=mob.session().prompt("Enter a new one (null)\n\r:","");
            if(newName.trim().equalsIgnoreCase("null"))
                return "";
            else
            if(newName.trim().length()==0)
            {
                mob.tell("(no change)");
                return oldRoomID;
            }
            Room newRoom=CMLib.map().getRoom(newName);
            if((newRoom==null)
            ||(CMLib.map().getExtendedRoomID(newRoom).length()==0)
            ||(!CMLib.law().doesOwnThisProperty(C.clanID(),newRoom)))
                mob.tell("That is either not a valid room id, or that room is not owned by the clan.");
            else
                return CMLib.map().getExtendedRoomID(newRoom);
        }
        return oldRoomID;
    }

    public void modifyClan(MOB mob, Clan C)
    throws IOException
    {
        if(mob.isMonster())
            return;
        boolean ok=false;
        int showFlag=-1;
        if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
            showFlag=-999;
        String oldName=C.ID();
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
        {
            mob.tell("*. Name: '"+C.name()+"'.");
            int showNumber=0;
            genClanGovt(mob,C,++showNumber,showFlag);
            C.setPremise(prompt(mob,C.getPremise(),++showNumber,showFlag,"Clan Premise: ",true));
            C.setExp(prompt(mob,C.getExp(),++showNumber,showFlag,"Clan Experience: "));
            C.setTaxes(prompt(mob,C.getTaxes(),++showNumber,showFlag,"Clan Tax Rate (X 100%): "));
            C.setMorgue(genClanRoom(mob,C,C.getMorgue(),". Morgue RoomID: '@x1'.",++showNumber,showFlag));
            C.setRecall(genClanRoom(mob,C,C.getRecall(),". Clan Home RoomID: '@x1'.",++showNumber,showFlag));
            C.setDonation(genClanRoom(mob,C,C.getDonation(),". Clan Donate RoomID: '@x1'.",++showNumber,showFlag));
            genClanAccept(mob,C,++showNumber,showFlag);
            genClanClass(mob,C,++showNumber,showFlag);
            genClanRole(mob,C,++showNumber,showFlag);
            genClanStatus(mob,C,++showNumber,showFlag);
            genClanMembers(mob,C,++showNumber,showFlag);
            /*setClanRelations, votes?*/
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
        if(!oldName.equals(C.ID()))
        {
            //cycle through everything changing the name
            CMLib.database().DBDeleteClan(C);
            CMLib.database().DBCreateClan(C);
        }
        C.update();
    }

    protected void modifyGenShopkeeper(MOB mob, ShopKeeper me)
        throws IOException
    {
        if(mob.isMonster())
            return;
        if(!(me instanceof MOB))
            return;
        MOB mme=(MOB)me;
        boolean ok=false;
        int showFlag=-1;
        if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
            showFlag=-999;
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
        {
            int showNumber=0;
            genName(mob,me,++showNumber,showFlag);
            genDisplayText(mob,me,++showNumber,showFlag);
            genDescription(mob,me,++showNumber,showFlag);
            int oldLevel=me.baseEnvStats().level();
            genLevel(mob,me,++showNumber,showFlag);
            if((oldLevel<2)&&(me.baseEnvStats().level()>1))
                CMLib.leveler().fillOutMOB(mme,me.baseEnvStats().level());
            genRejuv(mob,me,++showNumber,showFlag);
            genRace(mob,mme,++showNumber,showFlag);
            genHeight(mob,me,++showNumber,showFlag);
            genWeight(mob,me,++showNumber,showFlag);
        	CMLib.factions().updatePlayerFactions((MOB)me,((MOB)me).location());
            Faction F=null;
            for(Enumeration e=CMLib.factions().factions();e.hasMoreElements();)
            {
                F=(Faction)e.nextElement();
                if(F.showInEditor())
                    genSpecialFaction(mob,(MOB)me,++showNumber,showFlag,F);
            }
            genGender(mob,mme,++showNumber,showFlag);
            genClan(mob,mme,++showNumber,showFlag);
            genSpeed(mob,me,++showNumber,showFlag);
            if((oldLevel<2)&&(me.baseEnvStats().level()>1))
                me.baseEnvStats().setDamage((int)Math.round(CMath.div(me.baseEnvStats().damage(),me.baseEnvStats().speed())));
            genAttack(mob,me,++showNumber,showFlag);
            genDamage(mob,me,++showNumber,showFlag);
            genArmor(mob,me,++showNumber,showFlag);
            if(me instanceof MOB)
                genHitPoints(mob,(MOB)me,++showNumber,showFlag);
            genMoney(mob,mme,++showNumber,showFlag);
            mme.setMoneyVariation(CMath.s_double(prompt(mob,""+mme.getMoneyVariation(),++showNumber,showFlag,"Money Variation")));
            genAbilities(mob,mme,++showNumber,showFlag);
            genBehaviors(mob,me,++showNumber,showFlag);
            genAffects(mob,me,++showNumber,showFlag);
            if(!(me instanceof Auctioneer))
            {
                genShopkeeper1(mob,me,++showNumber,showFlag);
                genShopkeeper2(mob,me,++showNumber,showFlag);
                genEconomics1(mob,me,++showNumber,showFlag);
                genEconomics5(mob,me,++showNumber,showFlag);
            }
            genEconomics6(mob,me,++showNumber,showFlag);
            if(me instanceof Banker)
            {
                genBanker1(mob,(Banker)me,++showNumber,showFlag);
                genBanker2(mob,(Banker)me,++showNumber,showFlag);
                genBanker3(mob,(Banker)me,++showNumber,showFlag);
                genBanker4(mob,(Banker)me,++showNumber,showFlag);
            }
            else
            if(me instanceof PostOffice)
            {
                ((PostOffice)me).setPostalChain(prompt(mob,((PostOffice)me).postalChain(),++showNumber,showFlag,"Postal chain"));
                ((PostOffice)me).setFeeForNewBox(prompt(mob,((PostOffice)me).feeForNewBox(),++showNumber,showFlag,"Fee to open a new box"));
                ((PostOffice)me).setMinimumPostage(prompt(mob,((PostOffice)me).minimumPostage(),++showNumber,showFlag,"Minimum postage cost"));
                ((PostOffice)me).setPostagePerPound(prompt(mob,((PostOffice)me).postagePerPound(),++showNumber,showFlag,"Postage cost per pound after 1st pound"));
                ((PostOffice)me).setHoldFeePerPound(prompt(mob,((PostOffice)me).holdFeePerPound(),++showNumber,showFlag,"Holding fee per pound per month"));
                ((PostOffice)me).setMaxMudMonthsHeld(prompt(mob,((PostOffice)me).maxMudMonthsHeld(),++showNumber,showFlag,"Maximum number of months held"));
            }
            else
            if(me instanceof Auctioneer)
            {
                ((Auctioneer)me).setAuctionHouse(prompt(mob,((Auctioneer)me).auctionHouse(),++showNumber,showFlag,"Auction house"));
                ((Auctioneer)me).setTimedListingPrice(genAuctionPrompt(mob,((Auctioneer)me).timedListingPrice(),++showNumber,showFlag,"Flat fee per auction",false));
                ((Auctioneer)me).setTimedListingPct(genAuctionPrompt(mob,((Auctioneer)me).timedListingPct(),++showNumber,showFlag,"Listing Cut/%Pct per day",true));
                ((Auctioneer)me).setTimedFinalCutPct(genAuctionPrompt(mob,((Auctioneer)me).timedFinalCutPct(),++showNumber,showFlag,"Cut/%Pct of final price",true));
                ((Auctioneer)me).setMaxTimedAuctionDays(genAuctionPrompt(mob,((Auctioneer)me).maxTimedAuctionDays(),++showNumber,showFlag,"Maximum number of auction mud-days"));
                ((Auctioneer)me).setMinTimedAuctionDays(genAuctionPrompt(mob,((Auctioneer)me).minTimedAuctionDays(),++showNumber,showFlag,"Minimum number of auction mud-days"));
            }
            else
            {
                genEconomics2(mob,me,++showNumber,showFlag);
                genEconomics3(mob,me,++showNumber,showFlag);
                genEconomics4(mob,me,++showNumber,showFlag);
            }
            genDisposition(mob,me.baseEnvStats(),++showNumber,showFlag);
            genSensesMask(mob,me.baseEnvStats(),++showNumber,showFlag);
            genFaction(mob,mme,++showNumber,showFlag);
            genTattoos(mob,(MOB)me,++showNumber,showFlag);
            genExpertises(mob,(MOB)me,++showNumber,showFlag);
            genImage(mob,me,++showNumber,showFlag);
            for(int x=me.getSaveStatIndex();x<me.getStatCodes().length;x++)
                me.setStat(me.getStatCodes()[x],prompt(mob,me.getStat(me.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(me.getStatCodes()[x])));
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
        mme.recoverCharStats();
        mme.recoverMaxState();
        me.recoverEnvStats();
        mme.resetToMaxState();
        if(me.text().length()>=maxLength)
            mob.tell("\n\rThe data entered exceeds the string limit of "+maxLength+" characters.");
    }
    
    public void modifyRoom(MOB mob, Room R) throws IOException
    {
        int showFlag=-1;
        if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
            showFlag=-999;
        boolean ok=false;
        while(!ok)
        {
            int showNumber=0;
            genRoomType(mob,R,++showNumber,showFlag);
            genDisplayText(mob,R,++showNumber,showFlag);
            genDescription(mob,R,++showNumber,showFlag);
            if(R instanceof GridZones)
            {
                genGridLocaleX(mob,(GridZones)R,++showNumber,showFlag);
                genGridLocaleY(mob,(GridZones)R,++showNumber,showFlag);
                //((GridLocale)mob.location()).buildGrid();
            }
            genBehaviors(mob,R,++showNumber,showFlag);
            genAffects(mob,R,++showNumber,showFlag);
            for(int x=R.getSaveStatIndex();x<R.getStatCodes().length;x++)
                R.setStat(R.getStatCodes()[x],prompt(mob,R.getStat(R.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(R.getStatCodes()[x])));
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
    }
    
    protected void genAccountExpiration(MOB mob, AccountStats A, int showNumber, int showFlag) throws IOException
    { 
        if((showFlag>0)&&(showFlag!=showNumber)) return;
        mob.tell(showNumber+". Expires: "+CMLib.time().date2String(A.getAccountExpiration()));
        if((showFlag!=showNumber)&&(showFlag>-999)) return;
        String s=mob.session().prompt("Enter a new value\n\r:","");
        if(s.length()>0) 
        	A.setAccountExpiration(CMLib.time().string2Millis(s));
        else 
        	mob.tell("(no change)");
    }

    public void modifyAccount(MOB mob, PlayerAccount A) throws IOException
    {
        int showFlag=-1;
        if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
            showFlag=-999;
        boolean ok=false;
        while(!ok)
        {
            int showNumber=0;
            String acctName=CMStrings.capitalizeAndLower(prompt(mob,A.accountName(),++showNumber,showFlag,"Name",true,false,null));
            while((!acctName.equals(A.accountName()))
            &&(CMLib.players().getLoadAccount(acctName)!=null)
            &&(mob.session()!=null)&&(!mob.session().killFlag()))
            {
                mob.tell("The name given cannot be chosen, as it is already being used.");
                acctName=CMStrings.capitalizeAndLower(prompt(mob,acctName,showNumber,showFlag,"Name",true,false,null));
            }
            A.setAccountName(acctName);
        	genEmail(mob, A, ++showNumber, showFlag);
            if(CMProps.getBoolVar(CMProps.SYSTEMB_ACCOUNTEXPIRATION))
            	genAccountExpiration(mob,A,++showNumber,showFlag);
            promptStatStr(mob,A,CMParms.toStringList(PlayerAccount.FLAG_DESCS),++showNumber,showFlag,"Flags (?)","FLAGS",true);
            promptStatStr(mob,A,++showNumber,showFlag,"Notes: ","NOTES");
            for(int x=A.getSaveStatIndex();x<A.getStatCodes().length;x++)
                A.setStat(A.getStatCodes()[x],prompt(mob,A.getStat(A.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(A.getStatCodes()[x])));
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
    }

    public void modifyStdMob(MOB mob, MOB thang) throws IOException
    {
        int showFlag=-1;
        if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
            showFlag=-999;
        boolean ok=false;
        while(!ok)
        {
            int showNumber=0;
            genLevel(mob,thang,++showNumber,showFlag);
            genAbility(mob,thang,++showNumber,showFlag);
            genRejuv(mob,thang,++showNumber,showFlag);
            genMiscText(mob,thang,++showNumber,showFlag);
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
    }
    
    public void modifyStdItem(MOB mob, Item thang) throws IOException
    {
        int showFlag=-1;
        if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
            showFlag=-999;
        boolean ok=false;
        while(!ok)
        {
            int showNumber=0;
            genLevel(mob,thang,++showNumber,showFlag);
            genAbility(mob,thang,++showNumber,showFlag);
            genRejuv(mob,thang,++showNumber,showFlag);
            genUses(mob,thang,++showNumber,showFlag);
            genMiscText(mob,thang,++showNumber,showFlag);
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
    }
    
    public void modifyGenArea(MOB mob, Area myArea) throws IOException
    {
        int showFlag=-1;
        if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
            showFlag=-999;
        boolean ok=false;
        while(!ok)
        {
            int showNumber=0;
            mob.tell("*. Class: "+myArea.ID());
            genName(mob,myArea,++showNumber,showFlag);
            genDescription(mob,myArea,++showNumber,showFlag);
            genAuthor(mob,myArea,++showNumber,showFlag);
            genTechLevel(mob,myArea,++showNumber,showFlag);
            genClimateType(mob,myArea,++showNumber,showFlag);
            genTimeClock(mob,myArea,++showNumber,showFlag);
            genArchivePath(mob,myArea,++showNumber,showFlag);
            genParentAreas(mob,myArea,++showNumber,showFlag);
            genChildAreas(mob,myArea,++showNumber,showFlag);
            genSubOps(mob,myArea,++showNumber,showFlag);
            genAreaBlurbs(mob,myArea,++showNumber,showFlag);
            if(myArea instanceof GridZones)
            {
                genGridLocaleX(mob,(GridZones)myArea,++showNumber,showFlag);
                genGridLocaleY(mob,(GridZones)myArea,++showNumber,showFlag);
            }
            genBehaviors(mob,myArea,++showNumber,showFlag);
            genAffects(mob,myArea,++showNumber,showFlag);
            genImage(mob,myArea,++showNumber,showFlag);
            for(int x=myArea.getSaveStatIndex();x<myArea.getStatCodes().length;x++)
                myArea.setStat(myArea.getStatCodes()[x],prompt(mob,myArea.getStat(myArea.getStatCodes()[x]),++showNumber,showFlag,CMStrings.capitalizeAndLower(myArea.getStatCodes()[x])));
            if((showFlag<=0)||((showFlag>=showNumber)&&(showFlag<=showNumber+7)))
                mob.tell("*** Area Economics settings: ");
                genCurrency(mob,myArea,++showNumber,showFlag);
                genEconomics1(mob,myArea,++showNumber,showFlag);
                genEconomics2(mob,myArea,++showNumber,showFlag);
                genEconomics3(mob,myArea,++showNumber,showFlag);
                genEconomics4(mob,myArea,++showNumber,showFlag);
                genEconomics5(mob,myArea,++showNumber,showFlag);
                genEconomics6(mob,myArea,++showNumber,showFlag);
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
    }
}
