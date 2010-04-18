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
public class RaceData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	// valid parms include HELP, STATS, SENSES, TRAINS, PRACS, ABILITIES,
	// HEALTHTEXTS, NATURALWEAPON, PLAYABLE, DISPOSITIONS, STARTINGEQ,
	// CLASSES, LANGS, EFFECTS

    private String raceDropDown(String old)
    {
        StringBuffer str=new StringBuffer("");
        str.append("<OPTION VALUE=\"\" "+((old.length()==0)?"SELECTED":"")+">None");
        Race R2=null;
        String R2ID=null;
        for(Enumeration e=CMClass.races();e.hasMoreElements();)
        {
            R2=(Race)e.nextElement();
            R2ID="com.planet_ink.coffee_mud.Races."+R2.ID();
            if(R2.isGeneric() && CMClass.checkForCMClass("RACE",R2ID))
            {
                str.append("<OPTION VALUE=\""+R2.ID()+"\" "+((old.equalsIgnoreCase(R2.ID()))?"SELECTED":"")+">"+R2.ID()+" (Generic)");
                str.append("<OPTION VALUE=\""+R2ID+"\" "+((old.equalsIgnoreCase(R2ID))?"SELECTED":"")+">"+R2ID);
            }
            else
            if(R2.isGeneric())
                str.append("<OPTION VALUE=\""+R2.ID()+"\" "+((old.equalsIgnoreCase(R2.ID())||old.equalsIgnoreCase(R2ID))?"SELECTED":"")+">"+R2.ID()+" (Generic)");
            else
                str.append("<OPTION VALUE=\""+R2ID+"\" "+((old.equalsIgnoreCase(R2.ID())||old.equalsIgnoreCase(R2ID))?"SELECTED":"")+">"+R2ID);
        }
        return str.toString();
    }

    public static StringBuffer estats(EnvStats E, char c, ExternalHTTPRequests httpReq, Hashtable parms, int borderSize)
    {
        StringBuffer str=new StringBuffer("");
        DVector theclasses=new DVector(2);
        if(httpReq.isRequestParameter(c+"ESTATS1"))
        {
            int num=1;
            String behav=httpReq.getRequestParameter(c+"ESTATS"+num);
            while(behav!=null)
            {
                if(behav.length()>0)
                {
                    String prof=httpReq.getRequestParameter(c+"ESTATSV"+num);
                    if(prof==null) prof="0";
                    prof=""+CMath.s_int(prof);
                    theclasses.addElement(behav,prof);
                }
                num++;
                behav=httpReq.getRequestParameter(c+"ESTATS"+num);
            }
        }
        else
        {
            for(int i=0;i<E.getStatCodes().length;i++)
                if(CMath.s_int(E.getStat(E.getStatCodes()[i]))!=0)
                    theclasses.addElement(E.getStatCodes()[i],Integer.toString(CMath.s_int(E.getStat(E.getStatCodes()[i]))));
        }
        str.append("<TABLE WIDTH=100% BORDER="+borderSize+" CELLSPACING=0 CELLPADDING=0>");
        for(int i=0;i<theclasses.size();i++)
        {
            String theclass=(String)theclasses.elementAt(i,1);
            str.append("<TR><TD WIDTH=35%>");
            str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME="+c+"ESTATS"+(i+1)+">");
            str.append("<OPTION VALUE=\"\">Delete!");
            str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
            str.append("</SELECT>");
            str.append("</TD>");
            str.append("<TD WIDTH=65%>");
            str.append("<INPUT TYPE=TEXT NAME="+c+"ESTATSV"+(i+1)+" VALUE=\""+((String)theclasses.elementAt(i,2))+"\" SIZE=3 MAXLENGTH=3>");
            str.append("</TD>");
            str.append("</TR>");
        }
        str.append("<TR><TD WIDTH=35%>");
        str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME="+c+"ESTATS"+(theclasses.size()+1)+">");
        str.append("<OPTION SELECTED VALUE=\"\">Select a stat");
        for(int i=0;i<E.getStatCodes().length;i++)
            if((CMath.isNumber(E.getStat(E.getStatCodes()[i])))&&(!theclasses.contains(E.getStatCodes()[i])))
                str.append("<OPTION VALUE=\""+E.getStatCodes()[i]+"\">"+E.getStatCodes()[i]);
        str.append("</SELECT>");
        str.append("</TD>");
        str.append("<TD WIDTH=65%>");
        str.append("<INPUT TYPE=TEXT NAME="+c+"ESTATSV"+(theclasses.size()+1)+" VALUE=\"\" SIZE=3 MAXLENGTH=3>");
        str.append("</TD>");
        str.append("</TR>");
        str.append("</TABLE>");
        return str;
    }

    public static StringBuffer cstats(CharStats E, char c, ExternalHTTPRequests httpReq, Hashtable parms, int borderSize)
    {
        StringBuffer str=new StringBuffer("");
        DVector theclasses=new DVector(2);
        if(httpReq.isRequestParameter(c+"CSTATS1"))
        {
            int num=1;
            String behav=httpReq.getRequestParameter(c+"CSTATS"+num);
            while(behav!=null)
            {
                if(behav.length()>0)
                {
                    String prof=httpReq.getRequestParameter(c+"CSTATSV"+num);
                    if(prof==null) prof="0";
                    prof=""+CMath.s_int(prof);
                    theclasses.addElement(behav,prof);
                }
                num++;
                behav=httpReq.getRequestParameter(c+"CSTATS"+num);
            }
        }
        else
        {
            for(int i : CharStats.CODES.ALL())
                if(CMath.s_int(E.getStat(CharStats.CODES.NAME(i)))!=0)
                    theclasses.addElement(CharStats.CODES.NAME(i),E.getStat(CharStats.CODES.NAME(i)));
        }
        str.append("<TABLE WIDTH=100% BORDER="+borderSize+" CELLSPACING=0 CELLPADDING=0>");
        for(int i=0;i<theclasses.size();i++)
        {
            String theclass=(String)theclasses.elementAt(i,1);
            str.append("<TR><TD WIDTH=35%>");
            str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME="+c+"CSTATS"+(i+1)+">");
            str.append("<OPTION VALUE=\"\">Delete!");
            str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
            str.append("</SELECT>");
            str.append("</TD>");
            str.append("<TD WIDTH=65%>");
            str.append("<INPUT TYPE=TEXT NAME="+c+"CSTATSV"+(i+1)+" VALUE=\""+((String)theclasses.elementAt(i,2))+"\" SIZE=3 MAXLENGTH=3>");
            str.append("</TD>");
            str.append("</TR>");
        }
        str.append("<TR><TD WIDTH=35%>");
        str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME="+c+"CSTATS"+(theclasses.size()+1)+">");
        str.append("<OPTION SELECTED VALUE=\"\">Select a stat");
        for(int i : CharStats.CODES.ALL())
        	if(!theclasses.contains(CharStats.CODES.NAME(i)))
	            str.append("<OPTION VALUE=\""+CharStats.CODES.NAME(i)+"\">"+CharStats.CODES.DESC(i));
        str.append("</SELECT>");
        str.append("</TD>");
        str.append("<TD WIDTH=65%>");
        str.append("<INPUT TYPE=TEXT NAME="+c+"CSTATSV"+(theclasses.size()+1)+" VALUE=\"\" SIZE=3 MAXLENGTH=3>");
        str.append("</TD>");
        str.append("</TR>");
        str.append("</TABLE>");
        return str;
    }

    public static StringBuffer cstate(CharState E, char c, ExternalHTTPRequests httpReq, Hashtable parms, int borderSize)
    {
        StringBuffer str=new StringBuffer("");
        DVector theclasses=new DVector(2);
        if(httpReq.isRequestParameter(c+"CSTATE1"))
        {
            int num=1;
            String behav=httpReq.getRequestParameter(c+"CSTATE"+num);
            while(behav!=null)
            {
                if(behav.length()>0)
                {
                    String prof=httpReq.getRequestParameter(c+"CSTATEV"+num);
                    if(prof==null) prof="0";
                    prof=""+CMath.s_int(prof);
                    theclasses.addElement(behav,prof);
                }
                num++;
                behav=httpReq.getRequestParameter(c+"CSTATE"+num);
            }
        }
        else
        {
            for(int i=0;i<E.getStatCodes().length;i++)
                if(CMath.s_int(E.getStat(E.getStatCodes()[i]))!=0)
                    theclasses.addElement(E.getStatCodes()[i],Integer.valueOf(E.getStat(E.getStatCodes()[i])).toString());
        }
        str.append("<TABLE WIDTH=100% BORDER="+borderSize+" CELLSPACING=0 CELLPADDING=0>");
        for(int i=0;i<theclasses.size();i++)
        {
            String theclass=(String)theclasses.elementAt(i,1);
            str.append("<TR><TD WIDTH=35%>");
            str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME="+c+"CSTATE"+(i+1)+">");
            str.append("<OPTION VALUE=\"\">Delete!");
            str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
            str.append("</SELECT>");
            str.append("</TD>");
            str.append("<TD WIDTH=65%>");
            str.append("<INPUT TYPE=TEXT NAME="+c+"CSTATEV"+(i+1)+" VALUE=\""+((String)theclasses.elementAt(i,2))+"\" SIZE=3 MAXLENGTH=3>");
            str.append("</TD>");
            str.append("</TR>");
        }
        str.append("<TR><TD WIDTH=35%>");
        str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME="+c+"CSTATE"+(theclasses.size()+1)+">");
        str.append("<OPTION SELECTED VALUE=\"\">Select a stat");
        for(int i=0;i<E.getStatCodes().length;i++)
            if(CMath.isNumber(E.getStat(E.getStatCodes()[i])))
            	if(!theclasses.contains(E.getStatCodes()[i]))
	                str.append("<OPTION VALUE=\""+E.getStatCodes()[i]+"\">"+E.getStatCodes()[i]);
        str.append("</SELECT>");
        str.append("</TD>");
        str.append("<TD WIDTH=65%>");
        str.append("<INPUT TYPE=TEXT NAME="+c+"CSTATEV"+(theclasses.size()+1)+" VALUE=\"\" SIZE=3 MAXLENGTH=3>");
        str.append("</TD>");
        str.append("</TR>");
        str.append("</TABLE>");
        return str;
    }

    public static StringBuffer itemList(Vector items, char c, ExternalHTTPRequests httpReq, Hashtable parms, int borderSize, boolean one)
    {
    	if(items==null) items=new Vector();
        StringBuffer str=new StringBuffer("");
        Vector classes=new Vector();
        Vector itemlist=null;
        if(httpReq.isRequestParameter(c+"ITEM1"))
        {
            itemlist=RoomData.items;
            for(int i=1;;i++)
            {
                String MATCHING=httpReq.getRequestParameter(c+"ITEM"+i);
                if(MATCHING==null)
                    break;
                Item I2=RoomData.getItemFromAnywhere(itemlist,MATCHING);
                if(I2==null)
                {
                    I2=RoomData.getItemFromAnywhere(items,MATCHING);
                    if(I2!=null)
                        RoomData.contributeItems(CMParms.makeVector(I2));
                }
                if(I2!=null)
                    classes.addElement(I2);
                if(one) break;
            }
        }
        else
        {
            for(int m=0;m<items.size();m++)
            {
                Item I2=(Item)items.elementAt(m);
                classes.addElement(I2);
            }
            itemlist=RoomData.contributeItems(classes);
        }
        str.append("<TABLE WIDTH=100% BORDER=\""+borderSize+"\" CELLSPACING=0 CELLPADDING=0>");
        int numItems=0;
        if(!one)
        for(int i=0;i<classes.size();i++)
        {
            numItems++;
            Item I=(Item)classes.elementAt(i);
            str.append("<TR>");
            str.append("<TD WIDTH=90%>");
            str.append("<SELECT NAME="+c+"ITEM"+(numItems)+">");
            if(!one) str.append("<OPTION VALUE=\"\">Delete!");
            if(items.contains(I))
                str.append("<OPTION SELECTED VALUE=\""+RoomData.getItemCode(classes,I)+"\">"+I.Name()+" ("+I.ID()+")");
            else
            if(itemlist.contains(I))
                str.append("<OPTION SELECTED VALUE=\""+I+"\">"+I.Name()+" ("+I.ID()+")");
            else
                str.append("<OPTION SELECTED VALUE=\""+I.ID()+"\">"+I.Name()+" ("+I.ID()+")");
            str.append("</SELECT>");
            str.append("</TD>");
            str.append("<TD WIDTH=10%>");
            str.append("<INPUT TYPE=BUTTON NAME="+c+"EDITITEM"+(i+1)+" VALUE=EDIT ONCLICK=\"EditItem('"+RoomData.getItemCode(classes,I)+"','"+c+"ITEM"+(numItems)+"');\">");
            str.append("</TD></TR>");
        }
        str.append("<TR><TD WIDTH=90%>");
        str.append("<SELECT ONCHANGE=\"AddItem(this);\" NAME="+c+"ITEM"+(numItems+1)+">");
        if(!one) str.append("<OPTION SELECTED VALUE=\"\">Select a new Item");
        for(int i=0;i<itemlist.size();i++)
        {
            Item I=(Item)itemlist.elementAt(i);
            if(one&&(classes.contains(I)))
            {
                if(items.contains(I))
                    str.append("<OPTION SELECTED VALUE=\""+RoomData.getItemCode(classes,I)+"\">"+I.Name()+" ("+I.ID()+")");
                else
                    str.append("<OPTION SELECTED VALUE=\""+I+"\">"+I.Name()+" ("+I.ID()+")");
            }
            else
                str.append("<OPTION VALUE=\""+I+"\">"+I.Name()+" ("+I.ID()+")");
        }
        if(one)
        {
            Vector sortMe=new Vector();
            CMClass.addAllItemClassNames(sortMe,true,true,false);
            Object[] sorted=(new TreeSet(sortMe)).toArray();
            for(int i=0;i<sorted.length;i++)
            {
                boolean selected=false;
                for(int x=0;x<classes.size();x++)
                    if(((Item)classes.elementAt(x)).ID().equals(sorted[i]))
                    { selected=true; break;}
                str.append("<OPTION "+(selected?"SELECTED":"")+" VALUE=\""+(String)sorted[i]+"\">"+(String)sorted[i]);
            }
        }
        else
        {
            StringBuffer mposs=(StringBuffer)Resources.getResource("MUDGRINDER-OTHERPOSS");
            if(mposs==null)
            {
                mposs=new StringBuffer("");
                Vector sortMe=new Vector();
                CMClass.addAllItemClassNames(sortMe,true,true,false);
                Object[] sorted=(new TreeSet(sortMe)).toArray();
                for(int i=0;i<sorted.length;i++)
                    mposs.append("<OPTION VALUE=\""+(String)sorted[i]+"\">"+(String)sorted[i]);
                Resources.submitResource("MUDGRINDER-OTHERPOSS",mposs);
            }
            str.append(mposs);
        }
        str.append("</SELECT>");
        str.append("</TD>");
        str.append("<TD WIDTH=10%>");
        str.append("<INPUT TYPE=BUTTON NAME="+c+"ADDITEM VALUE=\"NEW\" ONCLICK=\"AddNewItem('"+c+"ITEM"+(numItems+1)+"');\">");
        str.append("</TD></TR></TABLE>");
        return str;
    }

    public static StringBuffer rabilities(Race E, ExternalHTTPRequests httpReq, Hashtable parms, int borderSize, String font)
    {
        StringBuffer str=new StringBuffer("");
        DVector theclasses=new DVector(4);
        if(httpReq.isRequestParameter("RABLES1"))
        {
            int num=1;
            String behav=httpReq.getRequestParameter("RABLES"+num);
            while(behav!=null)
            {
                if(behav.length()>0)
                {
                    String prof=httpReq.getRequestParameter("RABPOF"+num);
                    if(prof==null) prof="0";
                    String qual=httpReq.getRequestParameter("RABQUA"+num);
                    if(qual==null) qual="";
                    String levl=httpReq.getRequestParameter("RABLVL"+num);
                    if(levl==null) levl="0";
                    theclasses.addElement(behav,prof,qual,levl);
                }
                num++;
                behav=httpReq.getRequestParameter("RABLES"+num);
            }
        }
        else
        {
            Vector ables=E.racialAbilities(null);
            DVector cables=E.culturalAbilities();
            for(int i=0;i<ables.size();i++)
            {
                Ability Able=(Ability)ables.elementAt(i);
                if((Able!=null)&&(!cables.contains(Able.ID())))
                    theclasses.addElement(Able.ID(),Able.proficiency()+"",CMLib.ableMapper().getDefaultGain(E.ID(),false,Able.ID())?"":"on",CMLib.ableMapper().getQualifyingLevel(E.ID(),false,Able.ID())+"");
            }
        }
        if(font==null) font="<FONT COLOR=WHITE><B>";
        str.append("<TABLE WIDTH=100% BORDER="+borderSize+" CELLSPACING=0 CELLPADDING=0>");
        for(int i=0;i<theclasses.size();i++)
        {
            String theclass=(String)theclasses.elementAt(i,1);
            str.append("<TR><TD WIDTH=35%>");
            str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=RABLES"+(i+1)+">");
            str.append("<OPTION VALUE=\"\">Delete!");
            str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
            str.append("</SELECT>");
            str.append("</TD>");
            str.append("<TD WIDTH=25%>");
            str.append(font+"Lvl:</B></FONT> <INPUT TYPE=TEXT NAME=RABLVL"+(i+1)+" VALUE=\""+((String)theclasses.elementAt(i,4))+"\" SIZE=3 MAXLENGTH=3>");
            str.append("</TD>");
            str.append("<TD WIDTH=10%>");
            str.append("<INPUT TYPE=TEXT NAME=RABPOF"+(i+1)+" VALUE=\""+((String)theclasses.elementAt(i,2))+"\" SIZE=3 MAXLENGTH=3>"+font+"%</B></I></FONT>");
            str.append("</TD>");
            str.append("<TD WIDTH=30%>");
            str.append("<INPUT TYPE=CHECKBOX NAME=RABQUA"+(i+1)+" "+(((String)theclasses.elementAt(i,3)).equalsIgnoreCase("on")?"CHECKED":"")+">"+font+"Qualify Only</B></FONT></I>");
            str.append("</TD>");
            str.append("</TR>");
        }
        str.append("<TR><TD WIDTH=35%>");
        str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=RABLES"+(theclasses.size()+1)+">");
        str.append("<OPTION SELECTED VALUE=\"\">Select an Ability");
        for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
        {
            String cnam=((Ability)a.nextElement()).ID();
            str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
        }
        str.append("</SELECT>");
        str.append("</TD>");
        str.append("<TD WIDTH=25%>");
        str.append(font+"Lvl:</B></I></FONT> <INPUT TYPE=TEXT NAME=RABLVL"+(theclasses.size()+1)+" VALUE=\"\" SIZE=3 MAXLENGTH=3>");
        str.append("</TD>");
        str.append("<TD WIDTH=10%>");
        str.append("<INPUT TYPE=TEXT NAME=RABPOF"+(theclasses.size()+1)+" VALUE=\"\" SIZE=3 MAXLENGTH=3>"+font+"%</B></I></FONT>");
        str.append("</TD>");
        str.append("<TD WIDTH=30%>");
        str.append("<INPUT TYPE=CHECKBOX NAME=RABQUA"+(theclasses.size()+1)+" >"+font+"Qualify Only</B></I></FONT>");
        str.append("</TD>");
        str.append("</TR>");
        str.append("</TABLE>");
        return str;
    }


    public static StringBuffer cabilities(Race E, ExternalHTTPRequests httpReq, Hashtable parms, int borderSize, String font)
    {
        StringBuffer str=new StringBuffer("");
        DVector theclasses=new DVector(2);
        if(httpReq.isRequestParameter("CABLES1"))
        {
            int num=1;
            String behav=httpReq.getRequestParameter("CABLES"+num);
            while(behav!=null)
            {
                if(behav.length()>0)
                {
                    String prof=httpReq.getRequestParameter("CABPOF"+num);
                    if(prof==null) prof="0";
                    theclasses.addElement(behav,prof);
                }
                num++;
                behav=httpReq.getRequestParameter("CABLES"+num);
            }
        }
        else
        {
            DVector ables=E.culturalAbilities();
            for(int i=0;i<ables.size();i++)
                theclasses.addElement(ables.elementAt(i,1),ables.elementAt(i,2).toString());
        }
        if(font==null) font="<FONT COLOR=WHITE><B>";
        str.append("<TABLE WIDTH=100% BORDER="+borderSize+" CELLSPACING=0 CELLPADDING=0>");
        for(int i=0;i<theclasses.size();i++)
        {
            String theclass=(String)theclasses.elementAt(i,1);
            str.append("<TR><TD WIDTH=35%>");
            str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=CABLES"+(i+1)+">");
            str.append("<OPTION VALUE=\"\">Delete!");
            str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
            str.append("</SELECT>");
            str.append("</TD>");
            str.append("<TD WIDTH=65%>");
            str.append("<INPUT TYPE=TEXT NAME=CABPOF"+(i+1)+" VALUE=\""+((String)theclasses.elementAt(i,2))+"\" SIZE=3 MAXLENGTH=3>"+font+"%</B></I></FONT>");
            str.append("</TD>");
            str.append("</TR>");
        }
        str.append("<TR><TD WIDTH=35%>");
        str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=CABLES"+(theclasses.size()+1)+">");
        str.append("<OPTION SELECTED VALUE=\"\">Select an Ability");
        for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
        {
            String cnam=((Ability)a.nextElement()).ID();
            str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
        }
        str.append("</SELECT>");
        str.append("</TD>");
        str.append("<TD WIDTH=65%>");
        str.append("<INPUT TYPE=TEXT NAME=CABPOF"+(theclasses.size()+1)+" VALUE=\"\" SIZE=3 MAXLENGTH=3>"+font+"%</B></I></FONT>");
        str.append("</TD>");
        str.append("</TR>");
        str.append("</TABLE>");
        return str;
    }

    private MOB makeMOB(Race R)
    {
		MOB mob=CMClass.getMOB("StdMOB");
		mob.setSession((Session)CMClass.getCommon("DefaultSession"));
		mob.baseCharStats().setMyRace(R);
		R.startRacing(mob,false);
		mob.recoverCharStats();
		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.recoverMaxState();
		mob.setSession(null);
		return mob;
    }

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);

		String replaceCommand=httpReq.getRequestParameter("REPLACE");
		if((replaceCommand != null)
		&& (replaceCommand.length()>0)
        && (replaceCommand.indexOf('=')>0))
		{
		    int eq=replaceCommand.indexOf('=');
		    String field=replaceCommand.substring(0,eq);
		    String value=replaceCommand.substring(eq+1);
		    httpReq.addRequestParameters(field, value);
            httpReq.addRequestParameters("REPLACE","");
		}

		String last=httpReq.getRequestParameter("RACE");
		if(last==null) return " @break@";
		if(last.length()>0)
		{
            if(parms.containsKey("ISGENERIC"))
            {
                Race R2=CMClass.getRace(last);
                return ""+((R2!=null)&&(R2.isGeneric()));
            }

            String newRaceID=httpReq.getRequestParameter("NEWRACE");
            Race R = null;
            if(R==null)
                R=(Race)httpReq.getRequestObjects().get("RACE-"+last);
            if((R==null)
            &&(newRaceID!=null)
            &&(newRaceID.length()>0)
            &&(CMClass.getRace(newRaceID)==null))
            {
                R=(Race)CMClass.getRace("GenRace").copyOf();
                R.setRacialParms("<RACE><ID>"+newRaceID+"</ID><NAME>"+newRaceID+"</NAME></RACE>");
                last=newRaceID;
                httpReq.addRequestParameters("RACE",newRaceID);
            }
            if(R==null)
                R=CMClass.getRace(last);
            if(parms.containsKey("ISNEWRACE"))
                return ""+(CMClass.getRace(last)==null);

			if(R!=null)
			{
				StringBuffer str=new StringBuffer("");
				if(parms.containsKey("HELP"))
				{
					StringBuilder s=CMLib.help().getHelpText(R.ID(),null,false,true);
					if(s==null)
						s=CMLib.help().getHelpText(R.name(),null,false,true);
					if(s!=null)
					{
						if(s.toString().startsWith("<RACE>"))
							s=new StringBuilder(s.toString().substring(6));
						int limit=70;
						if(parms.containsKey("LIMIT")) limit=CMath.s_int((String)parms.get("LIMIT"));
						str.append(helpHelp(s,limit));
					}
				}
                if(parms.containsKey("NAME"))
                {
                    String old=httpReq.getRequestParameter("NAME");
                    if(old==null) old=R.name();
                    str.append(old+", ");
                }
                if(parms.containsKey("CAT"))
                {
                    String old=httpReq.getRequestParameter("CAT");
                    if(old==null) old=R.racialCategory();
                    str.append(old+", ");
                }
                if(parms.containsKey("VWEIGHT"))
                {
                    String old=httpReq.getRequestParameter("VWEIGHT");
                    if(old==null) old=""+R.weightVariance();
                    str.append(old+", ");
                }
                if(parms.containsKey("BWEIGHT"))
                {
                    String old=httpReq.getRequestParameter("BWEIGHT");
                    if(old==null) old=""+R.lightestWeight();
                    str.append(old+", ");
                }
                if(parms.containsKey("WEIGHT"))
                {
                    str.append(""+(R.lightestWeight()+(R.weightVariance()/2))+", ");
                }
                if(parms.containsKey("HEIGHT"))
                {
                    int m=(R.shortestMale()+R.shortestFemale())/2;
                    m+=(R.heightVariance()/2);
                    str.append(m+", ");
                }
                if(parms.containsKey("VHEIGHT"))
                {
                    String old=httpReq.getRequestParameter("VHEIGHT");
                    if(old==null) old=""+R.heightVariance();
                    str.append(old+", ");
                }
                if(parms.containsKey("MHEIGHT"))
                {
                    String old=httpReq.getRequestParameter("MHEIGHT");
                    if(old==null) old=""+R.shortestMale();
                    str.append(old+", ");
                }
                if(parms.containsKey("FHEIGHT"))
                {
                    String old=httpReq.getRequestParameter("FHEIGHT");
                    if(old==null) old=""+R.shortestFemale();
                    str.append(old+", ");
                }
                if(parms.containsKey("LEAVESTR"))
                {
                    String old=httpReq.getRequestParameter("LEAVESTR");
                    if(old==null) old=""+R.leaveStr();
                    str.append(old+", ");
                }
                if(parms.containsKey("ARRIVESTR"))
                {
                    String old=httpReq.getRequestParameter("ARRIVESTR");
                    if(old==null) old=""+R.arriveStr();
                    str.append(old+", ");
                }
                if(parms.containsKey("GENHELP"))
                {
                    String old=httpReq.getRequestParameter("GENHELP");
                    if(old==null){
                        R=R.makeGenRace();
                        old=R.getStat("HELP");
                    }
                    str.append(old+", ");
                }
                if(parms.containsKey("HEALTHRACE"))
                {
                    String old=httpReq.getRequestParameter("HEALTHRACE");
                    if(old==null){
                        R=R.makeGenRace();
                        old=""+R.getStat("HEALTHRACE");
                    }
                    str.append(raceDropDown(old));
                }
                if(parms.containsKey("WEAPONRACE"))
                {
                    String old=httpReq.getRequestParameter("WEAPONRACE");
                    if(old==null){
                        R=R.makeGenRace();
                        old=""+R.getStat("WEAPONRACE");
                    }
                    str.append(raceDropDown(old));
                }
                if(parms.containsKey("EVENTRACE"))
                {
                    String old=httpReq.getRequestParameter("EVENTRACE");
                    if(old==null){
                        R=R.makeGenRace();
                        old=""+R.getStat("EVENTRACE");
                    }
                    str.append(raceDropDown(old));
                }
                if(parms.containsKey("BODY"))
                {
                    str.append("<TABLE WIDTH=100% BORDER=0><TR>");
                    String font=(String)parms.get("FONT");
                    if(font==null) font="";
                    int col=-1;
                    for(int i=0;i<Race.BODYPARTSTR.length;i++)
                    {
                        String old=httpReq.getRequestParameter("BODYPART"+i);
                        if(old==null) old=""+R.bodyMask()[i];
                        if((++col)==4)
                        {
                        	col=0;
                        	str.append("</TR><TR>");
                        }
                        str.append("<TD WIDTH=1%>"+font+Race.BODYPARTSTR[i]+"</B></I></FONT></TD><TD><INPUT TYPE=TEXT NAME=BODYPART"+i+" VALUE=\""+old+"\" SIZE=3></TD>");

                    }
                    for(int i=col;i<4;i++)
                        str.append("<TD></TD><TD></TD>");
                    str.append("</TR></TABLE>, ");
                }
                if(parms.containsKey("WEAR"))
                {
            		Wearable.CODES codes = Wearable.CODES.instance();
                    for(int b=0;b<codes.total();b++)
                        if(CMath.bset(R.forbiddenWornBits(),codes.get(b)))
                            str.append(codes.name(b)+", ");
                }
                if(parms.containsKey("RABLE"))
                    str.append(rabilities(R,httpReq,parms,0,(String)parms.get("FONT"))+", ");
                if(parms.containsKey("CABLE"))
                    str.append(cabilities(R,httpReq,parms,0,(String)parms.get("FONT"))+", ");
                if(parms.containsKey("WEARID"))
                {
                    String old=httpReq.getRequestParameter("WEARID");
                    long mask=0;
                    if(old==null)
                        mask=R.forbiddenWornBits();
                    else
                    {
                        mask|=CMath.s_long(old);
                        for(int i=1;;i++)
                            if(httpReq.isRequestParameter("WEARID"+(Integer.toString(i))))
                                mask|=CMath.s_long(httpReq.getRequestParameter("WEARID"+(Integer.toString(i))));
                            else
                                break;
                    }
            		Wearable.CODES codes = Wearable.CODES.instance();
                    for(int i=1;i<codes.total();i++)
                    {
                        str.append("<OPTION VALUE="+codes.get(i)+" ");
                        if(CMath.bset(mask,codes.get(i)))
                            str.append("SELECTED");
                        str.append(">"+codes.name(i));
                    }
                    str.append(", ");
                }
                if(parms.containsKey("PLAYABLEID"))
                {
                    String old=httpReq.getRequestParameter("PLAYABLEID");
                    long mask=0;
                    if(old==null)
                        mask=R.availabilityCode();
                    else
                        mask|=CMath.s_long(old);
                    for(int i=0;i<Area.THEME_PHRASE_EXT.length;i++)
                        str.append("<OPTION VALUE="+i+" "+((i==mask)?"SELECTED":"")+">"+Area.THEME_PHRASE_EXT[i]);
                    str.append(", ");
                }


				if(parms.containsKey("PLAYABLE"))
					str.append(Area.THEME_PHRASE_EXT[R.availabilityCode()]+", ");
				if(parms.containsKey("NATURALWEAPON"))
					str.append(R.myNaturalWeapon().name()+", ");

				MOB mob=null;
                if(parms.containsKey("STATS"))
                	str.append(R.getStatAdjDesc()+", ");

                if(parms.containsKey("ESTATS")||parms.containsKey("CSTATS")||parms.containsKey("ASTATS")||parms.containsKey("ASTATE")||parms.containsKey("STARTASTATE"))
                {
                    R=R.makeGenRace();

                    if(parms.containsKey("ESTATS"))
                    {
                        String eStats=R.getStat("ESTATS");
                        EnvStats adjEStats=(EnvStats)CMClass.getCommon("DefaultEnvStats"); adjEStats.setAllValues(0);
                        if(eStats.length()>0){ CMLib.coffeeMaker().setEnvStats(adjEStats,eStats);}
                        str.append(estats(adjEStats,'E',httpReq,parms,0)+", ");
                    }
                    if(parms.containsKey("CSTATS"))
                    {
                        CharStats setStats=(CharStats)CMClass.getCommon("DefaultCharStats"); setStats.setAllValues(0);
                        String cStats=R.getStat("CSTATS");
                        if(cStats.length()>0){  CMLib.coffeeMaker().setCharStats(setStats,cStats);}
                        str.append(cstats(setStats,'S',httpReq,parms,0)+", ");
                    }
                    if(parms.containsKey("ASTATS"))
                    {
                        CharStats adjStats=(CharStats)CMClass.getCommon("DefaultCharStats"); adjStats.setAllValues(0);
                        String cStats=R.getStat("ASTATS");
                        if(cStats.length()>0){  CMLib.coffeeMaker().setCharStats(adjStats,cStats);}
                        str.append(cstats(adjStats,'A',httpReq,parms,0)+", ");
                    }
                    if(parms.containsKey("ASTATE"))
                    {
                        CharState adjState=(CharState)CMClass.getCommon("DefaultCharState"); adjState.setAllValues(0);
                        String aState=R.getStat("ASTATE");
                        if(aState.length()>0){  CMLib.coffeeMaker().setCharState(adjState,aState);}
                        str.append(cstate(adjState,'A',httpReq,parms,0)+", ");
                    }
                    if(parms.containsKey("STARTASTATE"))
                    {
                        CharState startAdjState=(CharState)CMClass.getCommon("DefaultCharState"); startAdjState.setAllValues(0);
                        String saState=R.getStat("STARTASTATE");
                        if(saState.length()>0){ CMLib.coffeeMaker().setCharState(startAdjState,saState);}
                        str.append(cstate(startAdjState,'S',httpReq,parms,0)+", ");
                    }
                }

                if(parms.containsKey("OUTFIT"))
                    str.append(itemList(R.outfit(null),'O',httpReq,parms,0,false)+", ");
                if(parms.containsKey("WEAPON"))
                {
                    Vector V=CMParms.makeVector(R.myNaturalWeapon());
                    str.append(itemList(V,'W',httpReq,parms,0,true)+", ");
                }
                if(parms.containsKey("RESOURCES"))
                    str.append(itemList(R.myResources(),'R',httpReq,parms,0,false)+", ");
                if(parms.containsKey("BODYKILL"))
                {
                    String old=httpReq.getRequestParameter("BODYKILL");
                    boolean bodyKill=false;
                    if(old==null)
                        bodyKill=CMath.s_bool(R.makeGenRace().getStat("BODYKILL"));
                    else
                        bodyKill=old.equalsIgnoreCase("on");
                    if(bodyKill) str.append(" CHECKED , ");
                }
                if(parms.containsKey("DISFLAGS"))
                {
                	if(!httpReq.isRequestParameter("DISFLAGS"))
                    {
                        R=R.makeGenRace();
	                    httpReq.addRequestParameters("DISFLAGS",R.getStat("DISFLAGS"));
                    }
                    int flags=CMath.s_int(httpReq.getRequestParameter("DISFLAGS"));
                    for(int i=0;i<Race.GENFLAG_DESCS.length;i++)
                    {
                    	str.append("<OPTION VALUE="+CMath.pow(2,i));
                    	if(CMath.bset(flags,CMath.pow(2,i)))
                    		str.append(" SELECTED");
                    	str.append(">"+Race.GENFLAG_DESCS[i]);
                    }
                }
                if(parms.containsKey("AGING"))
                {
                	int[] ageChart=R.getAgingChart();
                	if(!httpReq.isRequestParameter("AGE0"))
	                	for(int i=0;i<Race.AGE_DESCS.length;i++)
	                		httpReq.addRequestParameters("AGE"+i,""+ageChart[i]);
            		int val=-1;
                	for(int i=0;i<Race.AGE_DESCS.length;i++)
                	{
                		int lastVal=val;
                		val=CMath.s_int((String)httpReq.getRequestParameter("AGE"+i));
                		if(val<lastVal){ val=lastVal; httpReq.addRequestParameters("AGE"+i,""+val);}
                		str.append("<INPUT TYPE=TEXT SIZE=4 NAME=AGE"+i+" VALUE="+val+">"+Race.AGE_DESCS[i]+"<BR>");
                	}
					str.append(", ");
                }

				if(parms.containsKey("SENSES"))
					if(R.getSensesChgDesc().length()>0)
						str.append(R.getSensesChgDesc()+", ");
				if(parms.containsKey("DISPOSITIONS"))
					if(R.getDispositionChgDesc().length()>0)
						str.append(R.getDispositionChgDesc()+", ");
				if(parms.containsKey("TRAINS"))
					if(R.getTrainAdjDesc().length()>0)
						str.append(R.getTrainAdjDesc()+", ");
				if(parms.containsKey("EXPECTANCY"))
					str.append(""+R.getAgingChart()[Race.AGE_ANCIENT]+", ");
				if(parms.containsKey("PRACS"))
					if(R.getPracAdjDesc().length()>0)
						str.append(R.getPracAdjDesc()+", ");
				if(parms.containsKey("ABILITIES"))
					if(R.getAbilitiesDesc().length()>0)
						str.append(R.getAbilitiesDesc()+", ");
				if(parms.containsKey("EFFECTS"))
				{
					Vector ables=R.racialEffects(null);
					for(int i=0;i<ables.size();i++)
					{
						Ability A=(Ability)ables.elementAt(i);
						if(A!=null)
							str.append(A.Name()+", ");
					}
				}
                if(parms.containsKey("LANGS"))
					if(R.getLanguagesDesc().length()>0)
						str.append(R.getLanguagesDesc()+", ");
                
				if(parms.containsKey("STARTINGEQ"))
				{
					if(R.outfit(null)!=null)
					for(int i=0;i<R.outfit(null).size();i++)
					{
						Item I=(Item)R.outfit(null).elementAt(i);
						if(I!=null)
							str.append(I.Name()+", ");
					}
				}
				if(parms.containsKey("CLASSES"))
				{
					if(mob==null) mob=makeMOB(R);
					for(int i: CharStats.CODES.BASE())
						mob.baseCharStats().setStat(i,25);
					mob.recoverCharStats();
					for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
					{
						CharClass C=(CharClass)c.nextElement();
						if((C!=null)
						&&(CMProps.isTheme(C.availabilityCode()))
						&&(C.qualifiesForThisClass(mob,true)))
							str.append(C.name()+", ");
					}
				}
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
                if(mob!=null) mob.destroy();
                httpReq.getRequestObjects().put("RACE-"+last,R);
                return clearWebMacros(strstr);
			}
		}
		return "";
	}
}
