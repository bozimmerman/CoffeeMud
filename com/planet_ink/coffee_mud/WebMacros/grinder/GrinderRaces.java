package com.planet_ink.coffee_mud.WebMacros.grinder;
import com.planet_ink.coffee_mud.WebMacros.RoomData;
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
public class GrinderRaces
{
    public String name()    {return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

    public static String getEStats(char c, ExternalHTTPRequests httpReq)
    {
        boolean changes = false;
        EnvStats adjEStats=(EnvStats)CMClass.getCommon("DefaultEnvStats");
        adjEStats.setAllValues(0);
        if(httpReq.isRequestParameter(c+"ESTATS1"))
        {
            int num=1;
            String behav=httpReq.getRequestParameter(c+"ESTATS"+num);
            while(behav!=null)
            {
                if((behav.length()>0) && (CMParms.makeVector(adjEStats.getStatCodes()).contains(behav.toUpperCase().trim())))
                {
                    String prof=httpReq.getRequestParameter(c+"ESTATSV"+num);
                    if(prof==null) prof="0";
                    if(CMath.s_int(prof)!=0)
                    {
                        adjEStats.setStat(behav.toUpperCase().trim(), prof);
                        changes = true;
                    }
                }
                num++;
                behav=httpReq.getRequestParameter(c+"ESTATS"+num);
            }
        }
        if(!changes)
            return "";
        return CMLib.coffeeMaker().getEnvStatsStr(adjEStats);
    }

    public static String getCStats(char c, ExternalHTTPRequests httpReq)
    {
        boolean changes = false;
        CharStats adjCStats=(CharStats)CMClass.getCommon("DefaultCharStats");
        adjCStats.setAllValues(0);
        if(httpReq.isRequestParameter(c+"CSTATS1"))
        {
            int num=1;
            String behav=httpReq.getRequestParameter(c+"CSTATS"+num);
            while(behav!=null)
            {
                if((behav.length()>0) && (CMParms.makeVector(CharStats.CODES.NAMES()).contains(behav.toUpperCase().trim())))
                {
                    int val=CMath.s_int(httpReq.getRequestParameter(c+"CSTATSV"+num));
                    if(val!=0)
                    {
                        adjCStats.setStat(CMParms.indexOf(CharStats.CODES.NAMES(),behav.toUpperCase().trim()), val);
                        changes = true;
                    }
                }
                num++;
                behav=httpReq.getRequestParameter(c+"CSTATS"+num);
            }
        }
        if(!changes)
            return "";
        return CMLib.coffeeMaker().getCharStatsStr(adjCStats);
    }

    public static String getCState(char c, ExternalHTTPRequests httpReq)
    {
        boolean changes = false;
        CharState adjCState=(CharState)CMClass.getCommon("DefaultCharState");
        adjCState.setAllValues(0);
        if(httpReq.isRequestParameter(c+"CSTATE1"))
        {
            int num=1;
            String behav=httpReq.getRequestParameter(c+"CSTATE"+num);
            while(behav!=null)
            {
                if((behav.length()>0) && (CMParms.makeVector(adjCState.getStatCodes()).contains(behav.toUpperCase().trim())))
                {
                    String prof=httpReq.getRequestParameter(c+"CSTATEV"+num);
                    if(prof==null) prof="0";
                    if(CMath.s_int(prof)!=0)
                    {
                        adjCState.setStat(behav.toUpperCase().trim(), prof);
                        changes = true;
                    }
                }
                num++;
                behav=httpReq.getRequestParameter(c+"CSTATE"+num);
            }
        }
        if(!changes)
            return "";
        return CMLib.coffeeMaker().getCharStateStr(adjCState);
    }


    public static Vector itemList(Vector items, char c, ExternalHTTPRequests httpReq, boolean one)
    {
        if(items==null) items=new Vector();
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
        return classes;
    }

    public static DVector rabilities(ExternalHTTPRequests httpReq)
    {
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
        return theclasses;
    }


    public static DVector cabilities(ExternalHTTPRequests httpReq)
    {
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
        return theclasses;
    }

    public static String modifyRace(ExternalHTTPRequests httpReq, Hashtable parms, Race oldR, Race R)
    {
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
        String old;

        old=httpReq.getRequestParameter("NAME");
        R.setStat("NAME",(old==null)?"NAME":old);
        old=httpReq.getRequestParameter("CAT");
        R.setStat("CAT",(old==null)?"CAT":old);
        old=httpReq.getRequestParameter("VWEIGHT");
        R.setStat("VWEIGHT",(old==null)?"VWEIGHT":old);
        old=httpReq.getRequestParameter("BWEIGHT");
        R.setStat("BWEIGHT",(old==null)?"BWEIGHT":old);
        old=httpReq.getRequestParameter("VHEIGHT");
        R.setStat("VHEIGHT",(old==null)?"VHEIGHT":old);
        old=httpReq.getRequestParameter("MHEIGHT");
        R.setStat("MHEIGHT",(old==null)?"MHEIGHT":old);
        old=httpReq.getRequestParameter("FHEIGHT");
        R.setStat("FHEIGHT",(old==null)?"FHEIGHT":old);
        old=httpReq.getRequestParameter("LEAVESTR");
        R.setStat("LEAVE",(old==null)?"LEAVESTR":old);
        old=httpReq.getRequestParameter("ARRIVESTR");
        R.setStat("ARRIVE",(old==null)?"ARRIVESTR":old);
        old=httpReq.getRequestParameter("HEALTHRACE");
        R.setStat("HEALTHRACE",(old==null)?"HEALTHRACE":old);
        old=httpReq.getRequestParameter("WEAPONRACE");
        R.setStat("WEAPONRACE",(old==null)?"WEAPONRACE":old);
        old=httpReq.getRequestParameter("EVENTRACE");
        R.setStat("EVENTRACE",(old==null)?"EVENTRACE":old);
        old=httpReq.getRequestParameter("GENHELP");
        R.setStat("HELP", ((old==null)?"":old));
        StringBuffer bodyOld=new StringBuffer("");
        for(int i=0;i<Race.BODYPARTSTR.length;i++)
        {
            old=httpReq.getRequestParameter("BODYPART"+i);
            bodyOld.append((old==null)?"":old).append(";");
        }
        R.setStat("BODY",bodyOld.toString());
        old=httpReq.getRequestParameter("WEARID");
        long mask=0;
        if(old!=null)
        {
            mask|=CMath.s_long(old);
            for(int i=1;;i++)
                if(httpReq.isRequestParameter("WEARID"+(Integer.toString(i))))
                    mask|=CMath.s_long(httpReq.getRequestParameter("WEARID"+(Integer.toString(i))));
                else
                    break;
        }
        R.setStat("WEAR",""+mask);
        R.setStat("AVAIL",""+CMath.s_long(httpReq.getRequestParameter("PLAYABLEID")));
        R.setStat("BODYKILL",""+CMath.s_bool(httpReq.getRequestParameter("BODYKILL")));
        R.setStat("DISFLAGS",""+CMath.s_long(httpReq.getRequestParameter("DISFLAGS")));
        R.setStat("ESTATS",getEStats('E',httpReq));
        R.setStat("CSTATS",getCStats('S',httpReq));
        R.setStat("ASTATS",getCStats('A',httpReq));
        R.setStat("ASTATE",getCState('A',httpReq));
        R.setStat("STARTASTATE",getCState('S',httpReq));
        StringBuffer commaList = new StringBuffer("");
        int val=0;
        for(int i=0;i<Race.AGE_DESCS.length;i++)
        {
            int lastVal=val;
            val=CMath.s_int((String)httpReq.getRequestParameter("AGE"+i));
            if(val<lastVal) val=lastVal;
            if(i>0) commaList.append(",");
            commaList.append(val);
        }
        R.setStat("AGING",commaList.toString());
        Vector V=itemList(oldR.myResources(),'R',httpReq,false);
        R.setStat("NUMRSC",""+V.size());
        for(int l=0;l<V.size();l++)
        {
            R.setStat("GETRSCID"+l,((Environmental)V.elementAt(l)).ID());
            R.setStat("GETRSCPARM"+l,((Environmental)V.elementAt(l)).text());
        }
        V=itemList(oldR.outfit(null),'O',httpReq,false);
        R.setStat("NUMOFT",""+V.size());
        for(int l=0;l<V.size();l++)
        {
            R.setStat("GETOFTID"+l,((Environmental)V.elementAt(l)).ID());
            R.setStat("GETOFTPARM"+l,((Environmental)V.elementAt(l)).text());
        }
        V=itemList(CMParms.makeVector(oldR.myNaturalWeapon()),'W',httpReq,true);
        if(V.size()==0)
            R.setStat("WEAPONCLASS","StdWeapon");
        else
        {
            R.setStat("WEAPONCLASS",((Environmental)V.firstElement()).ID());
            R.setStat("WEAPONXML",((Environmental)V.firstElement()).text());
        }
        DVector DV=rabilities(httpReq);
        R.setStat("NUMRABLE", ""+DV.size());
        for(int i=0;i<DV.size();i++)
        {
            R.setStat("GETRABLE"+i, (String)DV.elementAt(i,1));
            R.setStat("GETRABLEPROF"+i, (String)DV.elementAt(i,2));
            R.setStat("GETRABLEQUAL"+i, ((String)DV.elementAt(i,3)).equalsIgnoreCase("on")?"true":"false");
            R.setStat("GETRABLELVL"+i, (String)DV.elementAt(i,4));
        }
        DV=cabilities(httpReq);
        R.setStat("NUMCABLE", ""+DV.size());
        for(int i=0;i<DV.size();i++)
        {
            R.setStat("GETCABLE"+i, (String)DV.elementAt(i,1));
            R.setStat("GETCABLEPROF"+i, (String)DV.elementAt(i,2));
        }
        return "";
    }
}
