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
public class GrinderClasses 
{
    public String name()    {return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

    public static DVector cabilities(ExternalHTTPRequests httpReq)
    {
        DVector theclasses=new DVector(9);
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
                    String qual=httpReq.getRequestParameter("CABQUA"+num);
                    if(qual==null) qual="";// null means unchecked
                    String levl=httpReq.getRequestParameter("CABLVL"+num);
                    if(levl==null) levl="0";
                    String secr=httpReq.getRequestParameter("CABSCR"+num);
                    if(secr==null) secr="";// null means unchecked
                    Object parm=httpReq.getRequestParameter("CABPRM"+num);
                    if(parm==null) parm="";
                    Object prereq=httpReq.getRequestParameter("CABPRE"+num);
                    if(prereq==null) prereq="";
                    Object mask=httpReq.getRequestParameter("CABMSK"+num);
                    if(mask==null) mask="";
                    String maxp=httpReq.getRequestParameter("CABMPOF"+num);
                    if(maxp==null) maxp="100";
                    theclasses.addElement(behav,levl,prof,qual,secr,parm,prereq,mask,maxp);
                }
                num++;
                behav=httpReq.getRequestParameter("CABLES"+num);
            }
        }
        return theclasses;
    }

    public static String modifyCharClass(ExternalHTTPRequests httpReq, Hashtable parms, CharClass oldC, CharClass C)
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
        // names are numerous
        DVector DV=new DVector(2);
        int num=0;
        while(httpReq.isRequestParameter("NAME"+(++num)))
            if(CMath.isInteger(httpReq.getRequestParameter("NAMELEVEL"+(num))))
            {
                int minLevel = CMath.s_int(httpReq.getRequestParameter("NAMELEVEL"+(num)));
                String name=httpReq.getRequestParameter("NAME"+(num));
                if((name!=null)&&(name.length()>0))
                {
                    if(DV.size()==0)
                        DV.addElement(Integer.valueOf(minLevel),name);
                    else
                    {
                        boolean added=false;
                        for(int n=0;n<DV.size();n++)
                            if(minLevel<((Integer)DV.elementAt(n,1)).intValue())
                            {
                                DV.insertElementAt(n,Integer.valueOf(minLevel),name);
                                added=true;
                                break;
                            }
                            else
                            if(minLevel==((Integer)DV.elementAt(n,1)).intValue())
                            {
                                added=true;
                                break;
                            }
                        if(!added)
                            DV.addElement(Integer.valueOf(minLevel),name);
                    }
                }
            }
        if(DV.size()==0) DV.addElement(Integer.valueOf(0),C.ID());
        C.setStat("NUMNAME",""+DV.size());
        for(int l=0;l<DV.size();l++)
        {
            C.setStat("NAME"+l, (String)DV.elementAt(l,2));
            C.setStat("NAMELEVEL"+l, ((Integer)DV.elementAt(l,1)).toString());
        }
            
        old=httpReq.getRequestParameter("");
        C.setStat("",(old==null)?"":old);
        
        old=httpReq.getRequestParameter("BASE");
        C.setStat("BASE",(old==null)?"BASECLASS":old);
        old=httpReq.getRequestParameter("HPDIV");
        C.setStat("HPDIV",(old==null)?"1":old);
        old=httpReq.getRequestParameter("HPDICE");
        C.setStat("HPDICE",(old==null)?"1":old);
        old=httpReq.getRequestParameter("HPDIE");
        C.setStat("HPDIE",(old==null)?"6":old);
        old=httpReq.getRequestParameter("MANADIV");
        C.setStat("MANADIV",(old==null)?"1":old);
        old=httpReq.getRequestParameter("MANADICE");
        C.setStat("MANADICE",(old==null)?"1":old);
        old=httpReq.getRequestParameter("MANADIE");
        C.setStat("MANADIE",(old==null)?"6":old);
        old=httpReq.getRequestParameter("LVLPRAC");
        C.setStat("LVLPRAC",(old==null)?"0":old);
        old=httpReq.getRequestParameter("LVLATT");
        C.setStat("LVLATT",(old==null)?"0":old);
        old=httpReq.getRequestParameter("FSTTRAN");
        C.setStat("FSTTRAN",(old==null)?"0":old);
        old=httpReq.getRequestParameter("FSTPRAC");
        C.setStat("FSTPRAC",(old==null)?"0":old);
        old=httpReq.getRequestParameter("LVLDAM");
        C.setStat("LVLDAM",(old==null)?"10":old);
        old=httpReq.getRequestParameter("MAXNCS");
        C.setStat("MAXNCS",(old==null)?"0":old);
        old=httpReq.getRequestParameter("MAXCRS");
        C.setStat("MAXCRS",(old==null)?"0":old);
        old=httpReq.getRequestParameter("MAXCMS");
        C.setStat("MAXCMS",(old==null)?"0":old);
        old=httpReq.getRequestParameter("MAXLGS");
        C.setStat("MAXLGS",(old==null)?"0":old);
        old=httpReq.getRequestParameter("LEVELCAP");
        C.setStat("LEVELCAP",(old==null)?"-1":old);
        old=httpReq.getRequestParameter("LVLMOVE");
        C.setStat("LVLMOVE",(old==null)?"1":old);
        old=httpReq.getRequestParameter("ARMOR");
        C.setStat("ARMOR",(old==null)?"0":old);
        old=httpReq.getRequestParameter("STRLMT");
        C.setStat("STRLMT",(old==null)?"STRLMT":old);
        old=httpReq.getRequestParameter("STRBON");
        C.setStat("STRBON",(old==null)?"STRBON":old);
        old=httpReq.getRequestParameter("QUAL");
        C.setStat("QUAL",(old==null)?"":old);
        old=httpReq.getRequestParameter("PLAYER");
        C.setStat("PLAYER",(old==null)?"0":old);
        C.setStat("ESTATS",GrinderRaces.getEStats('E',httpReq));
        C.setStat("CSTATS",GrinderRaces.getCStats('S',httpReq));
        C.setStat("ASTATS",GrinderRaces.getCStats('A',httpReq));
        C.setStat("ASTATE",GrinderRaces.getCState('A',httpReq));
        C.setStat("STARTASTATE",GrinderRaces.getCState('S',httpReq));
        old=httpReq.getRequestParameter("GENHELP");
        C.setStat("HELP", ((old==null)?"":old));
        String id="";
        Vector V=new Vector();
        for(int i=0;httpReq.isRequestParameter("NOWEAPS"+id);id=""+(++i))
            V.addElement(httpReq.getRequestParameter("NOWEAPS"+id));
        C.setStat("GETWEP",CMParms.toStringList(V));
        Vector Ivs=GrinderRaces.itemList(oldC.outfit(null),'O',httpReq,false);
        C.setStat("NUMOFT",""+Ivs.size());
        for(int l=0;l<Ivs.size();l++)
        {
            C.setStat("GETOFTID"+l,((Environmental)Ivs.elementAt(l)).ID());
            C.setStat("GETOFTPARM"+l,((Environmental)Ivs.elementAt(l)).text());
        }
        C.setStat("DISFLAGS",""+CMath.s_long(httpReq.getRequestParameter("DISFLAGS")));
        num=0;
        DV.clear();
        while(httpReq.isRequestParameter("SSET"+(++num)))
            if(CMath.isInteger(httpReq.getRequestParameter("SSETLEVEL"+(num))))
            {
                int minLevel = CMath.s_int(httpReq.getRequestParameter("SSETLEVEL"+(num)));
                String name=httpReq.getRequestParameter("SSET"+(num));
                if((name!=null)&&(name.length()>0))
                {
                    if(DV.size()==0)
                        DV.addElement(Integer.valueOf(minLevel),name);
                    else
                    {
                        boolean added=false;
                        for(int n=0;n<DV.size();n++)
                            if(minLevel<((Integer)DV.elementAt(n,1)).intValue())
                            {
                                DV.insertElementAt(n,Integer.valueOf(minLevel),name);
                                added=true;
                                break;
                            }
                            else
                            if(minLevel==((Integer)DV.elementAt(n,1)).intValue())
                            {
                                added=true;
                                break;
                            }
                        if(!added)
                            DV.addElement(Integer.valueOf(minLevel),name);
                    }
                }
            }
        C.setStat("NUMSSET",""+DV.size());
        for(int l=0;l<DV.size();l++)
        {
            String sec=(String)DV.elementAt(l,2);
            V=CMParms.parseCommas(sec, true);
            C.setStat("SSET"+l, CMParms.combineWithQuotes(V,0));
            C.setStat("SSETLEVEL"+l, ((Integer)DV.elementAt(l,1)).toString());
        }
        id="";
        V=new Vector();
        for(int i=0;httpReq.isRequestParameter("WEAPMATS"+id);id=""+(++i))
            if(CMath.isInteger(httpReq.getRequestParameter("WEAPMATS"+id)))
                V.addElement(httpReq.getRequestParameter("WEAPMATS"+id));
        C.setStat("NUMWMAT",""+V.size());
        C.setStat("GETWMAT",CMParms.toStringList(V));
        old=httpReq.getRequestParameter("ARMORMINOR");
        C.setStat("ARMORMINOR",(old==null)?"-1":old);
        old=httpReq.getRequestParameter("STATCLASS");
        C.setStat("STATCLASS",(old==null)?"":old);
        old=httpReq.getRequestParameter("EVENTCLASS");
        C.setStat("EVENTCLASS",(old==null)?"":old);
        DV=cabilities(httpReq);
        C.setStat("NUMCABLE", ""+DV.size());
        for(int i=0;i<DV.size();i++)
        {
            C.setStat("GETCABLELVL"+i, (String)DV.elementAt(i,2));
            C.setStat("GETCABLEPROF"+i, (String)DV.elementAt(i,3));
            C.setStat("GETCABLEGAIN"+i, ((String)DV.elementAt(i,4)).equalsIgnoreCase("on")?"false":"true");
            C.setStat("GETCABLESECR"+i, ((String)DV.elementAt(i,5)).equalsIgnoreCase("on")?"true":"false");
            if(DV.elementAt(i,6) instanceof String)
                C.setStat("GETCABLEPARM"+i, (String)DV.elementAt(i,6));
            if(DV.elementAt(i,7) instanceof String)
                C.setStat("GETCABLEPREQ"+i, (String)DV.elementAt(i,7));
            if(DV.elementAt(i,8) instanceof String)
                C.setStat("GETCABLEMASK"+i, (String)DV.elementAt(i,8));
            C.setStat("GETCABLEMAXP"+i, (String)DV.elementAt(i,9));
            // CABLE MUST BE LAST
            C.setStat("GETCABLE"+i, (String)DV.elementAt(i,1));
        }
        return "";
    }
}
