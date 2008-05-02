package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class HolidayData extends StdWebMacro
{
    public String name()    {return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

    public String runMacro(ExternalHTTPRequests httpReq, String parm)
    {
        Hashtable parms=parseParms(parm);
        String last=httpReq.getRequestParameter("HOLIDAY");
        if(last==null) return " @break@";
        if(last.length()>0)
        {
            int index=CMLib.quests().getHolidayIndex(last);
            Vector encodedData=null;
            Object resp=CMLib.quests().getHolidayFile();
            Vector steps=null;
            if(resp instanceof Vector)
                steps=(Vector)resp;
            if((index>=0)&&(steps!=null)) 
                encodedData=CMLib.quests().getEncodedHolidayData((String)steps.elementAt(index));
            if(encodedData!=null)
            {
                DVector settings=(DVector)encodedData.elementAt(0);
                DVector behaviors=(DVector)encodedData.elementAt(1);
                DVector properties=(DVector)encodedData.elementAt(2);
                DVector stats=(DVector)encodedData.elementAt(3);
                Vector stepV=(Vector)encodedData.elementAt(4);
                int pricingMobIndex=((Integer)encodedData.elementAt(5)).intValue();
                
                StringBuffer str=new StringBuffer("");
                if(parms.containsKey("NAME"))
                {
                    String old=httpReq.getRequestParameter("NAME");
                    if(old==null)
                    {
                        int dex=settings.indexOf("NAME");
                        if(dex>=0)
                            old=(String)settings.elementAt(dex,2);
                        else
                            old="Unknown";
                    }
                    str.append(old+", ");
                }
                if(parms.containsKey("DURATION"))
                {
                    String old=httpReq.getRequestParameter("DURATION");
                    if(old==null)
                    {
                        int dex=settings.indexOf("DURATION");
                        if(dex>=0)
                            old=(String)settings.elementAt(dex,2);
                        else
                            old="900";
                    }
                    str.append(old+", ");
                }
                if(parms.containsKey("AREAGROUP"))
                {
                    // any, all, "name" "name" "name" "name"
                    String old=httpReq.getRequestParameter("AREAGROUP");
                    if(old==null)
                    {
                        int dex=settings.indexOf("AREAGROUP");
                        if(dex>=0)
                            old=(String)settings.elementAt(dex,2);
                        else
                            old="Unknown";
                    }
                    /*else
                    if((!old.equalsIgnoreCase("ALL"))&&(old.trim().length()>0))
                    {
                        Vector areas=new Vector();
                        Vector chkV=CMParms.parse(old);
                        for(int a=0;a<chkV.size();a++)
                            if(CMLib.map().findArea((String)chkV.elementAt(a))!=null)
                                areas.addElement(CMLib.map().findArea((String)chkV.elementAt(a)).name());
                        if(areas.size()==0)
                        {
                            chkV=CMParms.parseCommas(old,false);
                            for(int a=0;a<chkV.size();a++)
                                if(CMLib.map().findArea((String)chkV.elementAt(a))!=null)
                                    areas.addElement(CMLib.map().findArea((String)chkV.elementAt(a)));
                        }
                        old=CMParms.combineWithQuotes(areas,0);
                    }*/
                    str.append(old+", ");
                }
                if(parms.containsKey("MOBGROUP"))
                {
                    // zappermask only
                    String old=httpReq.getRequestParameter("MOBGROUP");
                    if(old==null)
                    {
                        int dex=settings.indexOf("MOBGROUP");
                        if(dex>=0)
                            old=(String)settings.elementAt(dex,2);
                        else
                            old="Unknown";
                    }
                    str.append(old+", ");
                }
                if(parms.containsKey("MOOD"))
                {
                    String old=httpReq.getRequestParameter("MOOD");
                    if(old==null)
                    {
                        int dex=properties.indexOf("MOOD");
                        if(dex>=0)
                            old=(String)properties.elementAt(dex,2);
                        else
                            old="Unknown";
                    }
                    /*else
                    if(old.length()>0)
                    {
                        Vector V=getMoodList();
                        if(!V.contains(old.toUpperCase().trim()))
                            old="";
                    }*/
                    str.append(old+", ");
                }
                if(parms.containsKey("AGGRESSIVE"))
                {
                    String old=httpReq.getRequestParameter("AGGRESSIVE");
                    if(old==null)
                    {
                        int dex=behaviors.indexOf("AGGRESSIVE");
                        if(dex>=0)
                            old=(String)behaviors.elementAt(dex,2);
                        else
                            old="Unknown";
                    }
                    str.append(old+", ");
                }
                if(parms.containsKey("SCHEDULETYPE")||parms.containsKey("SCHEDULETYPEID"))
                {
                    String old=httpReq.getRequestParameter("SCHEDULETYPE");
                    if(old==null) old=httpReq.getRequestParameter("SCHEDULETYPEID");
                    final String[] TYPES={"RANDOM INTERVAL","MUD-DAY","RL-DAY"};
                    if(old==null)
                    {
                        int mudDayIndex=settings.indexOf("MUDDAY");
                        int dateIndex=settings.indexOf("DATE");
                        if(mudDayIndex>=0) 
                            old=TYPES[1];
                        else
                        if(dateIndex>=0) 
                            old=TYPES[2];
                        else
                            old=TYPES[0];
                    }
                    if(parms.containsKey("SCHEDULETYPEID"))
                    for(int i=0;i<TYPES.length;i++)
                        str.append("<OPTION VALUE="+i+" "+(old.equalsIgnoreCase(TYPES[i])?"SELECTED":"")+">"+TYPES[i]);
                    else
                        str.append(old);
                    str.append(", ");
                }
                if(parms.containsKey("MUDDAY"))
                {
                    String old=httpReq.getRequestParameter("MUDDAY");
                    if(old==null)
                    {
                        int dex=settings.indexOf("MUDDAY");
                        if(dex>=0)
                            old=(String)settings.elementAt(dex,2);
                        else
                            old="1-1";
                    }
                    str.append(old+", ");
                }
                if(parms.containsKey("DATE"))
                {
                    String old=httpReq.getRequestParameter("DATE");
                    if(old==null)
                    {
                        int dex=settings.indexOf("DATE");
                        if(dex>=0)
                            old=(String)settings.elementAt(dex,2);
                        else
                            old="1-1";
                    }
                    str.append(old+", ");
                }
                if(parms.containsKey("WAIT"))
                {
                    String old=httpReq.getRequestParameter("WAIT");
                    if(old==null)
                    {
                        int dex=settings.indexOf("WAIT");
                        if(dex>=0)
                            old=(String)settings.elementAt(dex,2);
                        else
                            old="100";
                    }
                    str.append(old+", ");
                }
                str.append(HolidayData.behaviors(behaviors,httpReq,parms,1));
                str.append(HolidayData.properties(properties,httpReq,parms,1));
                str.append(HolidayData.priceFactors(stats, httpReq, parms, 1));
                //showNumber=genMudChat(mob,"MUDCHAT",behaviors,++showNumber,showFlag);
                String strstr=str.toString();
                if(strstr.endsWith(", "))
                    strstr=strstr.substring(0,strstr.length()-2);
                return clearWebMacros(strstr);
            }
        }
        return "";
    }
    
    public static StringBuffer behaviors(DVector behaviors, ExternalHTTPRequests httpReq, Hashtable parms, int borderSize)
    {
        StringBuffer str=new StringBuffer("");
        if(parms.containsKey("BEHAVIORS"))
        {
            Vector theclasses=new Vector();
            Vector theparms=new Vector();
            if(httpReq.isRequestParameter("BEHAV1"))
            {
                int num=1;
                String behav=httpReq.getRequestParameter("BEHAV"+num);
                String theparm=httpReq.getRequestParameter("BDATA"+num);
                while((behav!=null)&&(theparm!=null))
                {
                    if(behav.length()>0)
                    {
                        theclasses.addElement(behav);
                        String t=theparm;
                        t=CMStrings.replaceAll(t,"\"","&quot;");
                        theparms.addElement(t);
                    }
                    num++;
                    behav=httpReq.getRequestParameter("BEHAV"+num);
                    theparm=httpReq.getRequestParameter("BDATA"+num);
                }
            }
            else
            for(int b=0;b<behaviors.size();b++)
            {
                Behavior B=CMClass.getBehavior((String)behaviors.elementAt(b,1));
                if((B!=null)
                &&(!B.ID().equalsIgnoreCase("MUDCHAT"))
                &&(!B.ID().equalsIgnoreCase("AGGRESSIVE")))
                {
                    theclasses.addElement(CMClass.classID(B));
                    String t=(String)behaviors.elementAt(b,2);
                    t=CMStrings.replaceAll(t,"\"","&quot;");
                    theparms.addElement(t);
                }
            }
            str.append("<TABLE WIDTH=100% BORDER=\""+borderSize+"\" CELLSPACING=0 CELLPADDING=0>");
            for(int i=0;i<theclasses.size();i++)
            {
                String theclass=(String)theclasses.elementAt(i);
                String theparm=(String)theparms.elementAt(i);
                str.append("<TR><TD WIDTH=50%>");
                str.append("<SELECT ONCHANGE=\"EditBehavior(this);\" NAME=BEHAV"+(i+1)+">");
                str.append("<OPTION VALUE=\"\">Delete!");
                str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
                str.append("</SELECT>");
                str.append("</TD><TD WIDTH=50%>");
                str.append("<INPUT TYPE=TEXT SIZE=30 NAME=BDATA"+(i+1)+" VALUE=\""+theparm+"\">");
                str.append("</TD></TR>");
            }
            str.append("<TR><TD WIDTH=50%>");
            str.append("<SELECT ONCHANGE=\"AddBehavior(this);\" NAME=BEHAV"+(theclasses.size()+1)+">");
            str.append("<OPTION SELECTED VALUE=\"\">Select a Behavior");

            Object[] sortedB=null;
            Vector sortMeB=new Vector();
            for(Enumeration b=CMClass.behaviors();b.hasMoreElements();)
            {
                Behavior B=(Behavior)b.nextElement();
                sortMeB.addElement(CMClass.classID(B));
            }
            sortedB=(new TreeSet(sortMeB)).toArray();
            for(int r=0;r<sortedB.length;r++)
            {
                String cnam=(String)sortedB[r];
                str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
            }
            str.append("</SELECT>");
            str.append("</TD><TD WIDTH=50%>");
            str.append("<INPUT TYPE=TEXT SIZE=30 NAME=BDATA"+(theclasses.size()+1)+" VALUE=\"\">");
            str.append("</TD></TR>");
            str.append("</TABLE>");
        }
        return str;
    }
    
    public static StringBuffer properties(DVector properties, ExternalHTTPRequests httpReq, Hashtable parms, int borderSize)
    {
        StringBuffer str=new StringBuffer("");
        if(parms.containsKey("AFFECTS"))
        {
            Vector theclasses=new Vector();
            Vector theparms=new Vector();
            if(httpReq.isRequestParameter("AFFECT1"))
            {
                int num=1;
                String behav=httpReq.getRequestParameter("AFFECT"+num);
                String theparm=httpReq.getRequestParameter("ADATA"+num);
                while((behav!=null)&&(theparm!=null))
                {
                    if(behav.length()>0)
                    {
                        theclasses.addElement(behav);
                        String t=theparm;
                        t=CMStrings.replaceAll(t,"\"","&quot;");
                        theparms.addElement(t);
                    }
                    num++;
                    behav=httpReq.getRequestParameter("AFFECT"+num);
                    theparm=httpReq.getRequestParameter("ADATA"+num);
                }
            }
            else
            for(int b=0;b<properties.size();b++)
            {
                Ability A=CMClass.getAbility((String)properties.elementAt(b,1));
                if((A!=null)&&(!A.ID().equalsIgnoreCase("MOOD")))
                {
                    theclasses.addElement(CMClass.classID(A));
                    String t=(String)properties.elementAt(b,2);
                    t=CMStrings.replaceAll(t,"\"","&quot;");
                    theparms.addElement(t);
                }
            }
            str.append("<TABLE WIDTH=100% BORDER=\""+borderSize+"\" CELLSPACING=0 CELLPADDING=0>");
            for(int i=0;i<theclasses.size();i++)
            {
                String theclass=(String)theclasses.elementAt(i);
                String theparm=(String)theparms.elementAt(i);
                str.append("<TR><TD WIDTH=50%>");
                str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=AFFECT"+(i+1)+">");
                str.append("<OPTION VALUE=\"\">Delete!");
                str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
                str.append("</SELECT>");
                str.append("</TD><TD WIDTH=50%>");
                str.append("<INPUT TYPE=TEXT SIZE=30 NAME=ADATA"+(i+1)+" VALUE=\""+theparm+"\">");
                str.append("</TD></TR>");
            }
            str.append("<TR><TD WIDTH=50%>");
            str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=AFFECT"+(theclasses.size()+1)+">");
            str.append("<OPTION SELECTED VALUE=\"\">Select an Effect");
            for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
            {
                Ability A=(Ability)a.nextElement();
                String cnam=A.ID();
                str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
            }
            str.append("</SELECT>");
            str.append("</TD><TD WIDTH=50%>");
            str.append("<INPUT TYPE=TEXT SIZE=30 NAME=ADATA"+(theclasses.size()+1)+" VALUE=\"\">");
            str.append("</TD></TR>");
            str.append("</TABLE>");
        }
        return str;
    }
    
    public static StringBuffer priceFactors(DVector stats, ExternalHTTPRequests httpReq, Hashtable parms, int borderSize)
    {
        StringBuffer str=new StringBuffer("");
        if(parms.containsKey("PRICEFACTORS"))
        {
            Vector theclasses=new Vector();
            Vector theparms=new Vector();
            if(httpReq.isRequestParameter("PRCFAC1"))
            {
                int num=1;
                String behav=httpReq.getRequestParameter("PRCFAC"+num);
                String theparm=httpReq.getRequestParameter("PMASK"+num);
                while((behav!=null)&&(theparm!=null))
                {
                    if(!behav.trim().endsWith("%")) behav=behav.trim()+"%";
                    if((behav.length()>0)&&(CMath.isPct(behav)))
                    {
                        theclasses.addElement(behav);
                        String t=theparm;
                        t=CMStrings.replaceAll(t,"\"","&quot;");
                        theparms.addElement(t);
                    }
                    num++;
                    behav=httpReq.getRequestParameter("PRCFAC"+num);
                    theparm=httpReq.getRequestParameter("PMASK"+num);
                }
            }
            else
            {
                int pndex=stats.indexOf("PRICEMASKS");
                String priceStr=(pndex<0)?"":(String)stats.elementAt(pndex,2);
                Vector priceV=CMParms.parseCommas(priceStr,true);
                for(int v=0;v<priceV.size();v++)
                {
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
                    theclasses.addElement((priceFactor*100.0)+"%");
                    mask=CMStrings.replaceAll(mask,"\"","&quot;");
                    theparms.addElement(mask);
                }
            }
            str.append("<TABLE WIDTH=100% BORDER=\""+borderSize+"\" CELLSPACING=0 CELLPADDING=0>");
            for(int i=0;i<theclasses.size();i++)
            {
                String theclass=(String)theclasses.elementAt(i);
                String theparm=(String)theparms.elementAt(i);
                str.append("<TR><TD WIDTH=25%>");
                str.append("<INPUT TYPE=TEXT SIZE=5 NAME=PRCFAC"+(i+1)+" VALUE=\""+theclass+"\">");
                str.append("</TD><TD WIDTH=75%>");
                str.append("<INPUT TYPE=TEXT SIZE=30 NAME=PMASK"+(i+1)+" VALUE=\""+theparm+"\">");
                str.append("</TD></TR>");
            }
            str.append("<TR><TD WIDTH=25%>");
            str.append("<INPUT TYPE=TEXT SIZE=5 NAME=PRCFAC"+(theclasses.size()+1)+" VALUE=\"\">");
            str.append("</TD><TD WIDTH=50%>");
            str.append("<INPUT TYPE=TEXT SIZE=40 NAME=PMASK"+(theclasses.size()+1)+" VALUE=\"\">");
            str.append("</TD></TR>");
            str.append("</TABLE>");
        }
        return str;
    }
    
    protected Vector getMoodList()
    {
        Vector V=new Vector();
        Ability A=CMClass.getAbility("Mood");
        if(A==null) return V;
        int x=0;
        A.setMiscText(""+x);
        while((A.text().length()>0)&&(!V.contains(A.text())))
        {
            V.addElement(A.text().toUpperCase().trim());
            x++;
            A.setMiscText(""+x);
        }
        return V;
    }
}