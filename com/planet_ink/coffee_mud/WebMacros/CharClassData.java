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
public class CharClassData extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	
	
    private String classDropDown(String old)
    {
        StringBuffer str=new StringBuffer("");
        str.append("<OPTION VALUE=\"\" "+((old.length()==0)?"SELECTED":"")+">None");
        CharClass C2=null;
        String C2ID=null;
        for(Enumeration e=CMClass.charClasses();e.hasMoreElements();)
        {
            C2=(CharClass)e.nextElement();
            C2ID="com.planet_ink.coffee_mud.CharClasses."+C2.ID();
            if(C2.isGeneric() && CMClass.checkForCMClass("CHARCLASS",C2ID))
            {
                str.append("<OPTION VALUE=\""+C2.ID()+"\" "+((old.equalsIgnoreCase(C2.ID()))?"SELECTED":"")+">"+C2.ID()+" (Generic)");
                str.append("<OPTION VALUE=\""+C2ID+"\" "+((old.equalsIgnoreCase(C2ID))?"SELECTED":"")+">"+C2ID);
            }
            else
            if(C2.isGeneric())
                str.append("<OPTION VALUE=\""+C2.ID()+"\" "+((old.equalsIgnoreCase(C2.ID())||old.equalsIgnoreCase(C2ID))?"SELECTED":"")+">"+C2.ID()+" (Generic)");
            else
                str.append("<OPTION VALUE=\""+C2ID+"\" "+((old.equalsIgnoreCase(C2.ID())||old.equalsIgnoreCase(C2ID))?"SELECTED":"")+">"+C2ID);
        }
        return str.toString();
    }
    
    
    public static StringBuffer cabilities(CharClass E, ExternalHTTPRequests httpReq, Hashtable parms, int borderSize, String font)
    {
        StringBuffer str=new StringBuffer("");
        DVector theclasses=new DVector(6);
        if(httpReq.isRequestParameter("CABLES1"))
        {
            int num=1;
            String behav=httpReq.getRequestParameter("CABLES"+num);
            while(behav!=null)
            {
                if(behav.length()>0)
                {
                    String prof=httpReq.getRequestParameter("CABPOF"+num);
                    if((prof==null)||(!CMath.isInteger(prof))) prof="0";
                    String qual=httpReq.getRequestParameter("CABQUA"+num);
                    if(qual==null) qual=""; // null means unchecked
                    String levl=httpReq.getRequestParameter("CABLVL"+num);
                    if((levl==null)||(!CMath.isInteger(levl))) levl="0";
                    String secr=httpReq.getRequestParameter("CABSCR"+num);
                    if(secr==null) secr=""; // null means unchecked
                    String parm=httpReq.getRequestParameter("CABPRM"+num);
                    if(parm==null) parm="";
                    theclasses.addElement(behav,levl,prof,qual,secr,parm);
                }
                num++;
                behav=httpReq.getRequestParameter("CABLES"+num);
            }
        }
        else
        {
            DVector data1=CMLib.ableMapper().getUpToLevelListings(E.ID(),Integer.MAX_VALUE,true,false);
            DVector sortedData1=new DVector(2);
            String aID=null;
            int minLvl=Integer.MAX_VALUE;
            int maxLvl=Integer.MIN_VALUE;
            for(int i=0;i<data1.size();i++)
            {
                aID=(String)data1.elementAt(i,1);
                int qlvl=CMLib.ableMapper().getQualifyingLevel(E.ID(), false, aID);
                if(qlvl>maxLvl) maxLvl=qlvl;
                if(qlvl<minLvl) minLvl=qlvl;
                sortedData1.addElement(aID,new Integer(qlvl));
            }
            Integer qLvl=null;
            for(int lvl=minLvl;lvl<=maxLvl;lvl++)
            {
                for(int i=0;i<sortedData1.size();i++)
                {
                    qLvl=(Integer)sortedData1.elementAt(i,2);
                    if(qLvl.intValue()==lvl)
                    {
                        aID=(String)sortedData1.elementAt(i,1);
                        theclasses.addElement(aID,
                                               qLvl.toString(),
                                               new Integer(CMLib.ableMapper().getDefaultProficiency(E.ID(),false,aID)).toString(),
                                               CMLib.ableMapper().getDefaultGain(E.ID(),false,aID)?"":"on",
                                               CMLib.ableMapper().getSecretSkill(E.ID(),false,aID)?"on":"",
                                               CMLib.ableMapper().getDefaultParm(E.ID(),false,aID));
                        DVector preReqs=CMLib.ableMapper().getPreReqs(E.ID(), false, aID);
                    }
                }
            }
        }
        if(font==null) font="<FONT COLOR=WHITE><B>";
        str.append("<TABLE WIDTH=100% BORDER="+borderSize+" CELLSPACING=0 CELLPADDING=0>");
        if(parms.containsKey("HEADERCOL1")
        ||parms.containsKey("HEADERCOL2")
        ||parms.containsKey("HEADERCOL3")
        ||parms.containsKey("HEADERCOL4"))
        {
            String sfont=(parms.containsKey("FONT"))?("<FONT "+((String)parms.get("FONT"))+">"):"";
            String efont=(parms.containsKey("FONT"))?"</FONT>":"";
            str.append("<TR><TD WIDTH=50%>");
            if(parms.containsKey("HEADERCOL1"))
                str.append(sfont + ((String)parms.get("HEADERCOL1")) + efont);
            str.append("</TD><TD WIDTH=10%>");
            if(parms.containsKey("HEADERCOL2"))
                str.append(sfont + ((String)parms.get("HEADERCOL2")) + efont);
            str.append("</TD><TD WIDTH=10%>");
            if(parms.containsKey("HEADERCOL3"))
                str.append(sfont + ((String)parms.get("HEADERCOL3")) + efont);
            str.append("</TD><TD WIDTH=30%>");
            if(parms.containsKey("HEADERCOL4"))
                str.append(sfont + ((String)parms.get("HEADERCOL4")) + efont);
            str.append("</TD></TR>");
        }
        HashSet used=new HashSet();
        for(int i=0;i<theclasses.size();i++)
        {
            String theclass=(String)theclasses.elementAt(i,1);
            used.add(theclass);
            str.append("<TR><TD WIDTH=50%>");
            str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=CABLES"+(i+1)+">");
            str.append("<OPTION VALUE=\"\">Delete!");
            str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
            str.append("</SELECT><BR>");
            str.append("<INPUT TYPE=TEXT NAME=CABPRM"+(i+1)+" VALUE=\""+((String)theclasses.elementAt(i,6))+"\" SIZE=50 MAXLENGTH=255>");
            str.append("</TD>");
            str.append("<TD WIDTH=10%>");
            str.append("<INPUT TYPE=TEXT NAME=CABLVL"+(i+1)+" VALUE=\""+((String)theclasses.elementAt(i,2))+"\" SIZE=2 MAXLENGTH=3>");
            str.append("</TD>");
            str.append("<TD WIDTH=10%>");
            str.append("<INPUT TYPE=TEXT NAME=CABPOF"+(i+1)+" VALUE=\""+((String)theclasses.elementAt(i,3))+"\" SIZE=2 MAXLENGTH=3>"+font+"%</B></I></FONT>");
            str.append("</TD>");
            str.append("<TD WIDTH=30%>");
            str.append("<INPUT TYPE=CHECKBOX NAME=CABQUA"+(i+1)+" "+(((String)theclasses.elementAt(i,4)).equalsIgnoreCase("on")?"CHECKED":"")+">"+font+"Qualify Only</B></FONT></I>&nbsp;");
            str.append("<INPUT TYPE=CHECKBOX NAME=CABSCR"+(i+1)+" "+(((String)theclasses.elementAt(i,5)).equalsIgnoreCase("on")?"CHECKED":"")+">"+font+"Secret</B></FONT></I>");
            str.append("</TD>");
            str.append("</TR>");
        }
        str.append("<TR><TD WIDTH=50%>");
        str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=CABLES"+(theclasses.size()+1)+">");
        str.append("<OPTION SELECTED VALUE=\"\">Select an Ability");
        for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
        {
            String cnam=((Ability)a.nextElement()).ID();
            if(!used.contains(cnam))
                str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
        }
        str.append("</SELECT><BR>");
        str.append("<INPUT TYPE=TEXT NAME=CABPRM"+(theclasses.size()+1)+" VALUE=\"\" SIZE=50 MAXLENGTH=255>");
        str.append("</TD>");
        str.append("<TD WIDTH=10%>");
        str.append("<INPUT TYPE=TEXT NAME=CABLVL"+(theclasses.size()+1)+" VALUE=\"\" SIZE=2 MAXLENGTH=3>");
        str.append("</TD>");
        str.append("<TD WIDTH=10%>");
        str.append("<INPUT TYPE=TEXT NAME=CABPOF"+(theclasses.size()+1)+" VALUE=\"\" SIZE=2 MAXLENGTH=3>"+font+"%</B></I></FONT>");
        str.append("</TD>");
        str.append("<TD WIDTH=30%>");
        str.append("<INPUT TYPE=CHECKBOX NAME=CABQUA"+(theclasses.size()+1)+" >"+font+"Qualify Only</B></I></FONT>&nbsp;");
        str.append("<INPUT TYPE=CHECKBOX NAME=CABSCR"+(theclasses.size()+1)+" >"+font+"Secret</B></I></FONT>");
        str.append("</TD>");
        str.append("</TR>");
        str.append("</TABLE>");
        return str;
    }

	// parameters include help, playable, max stats, pracs, trains, hitpoints,
	// mana, movement, attack, weapons, armorlimits, limits, bonuses,
	// prime, quals, startingeq
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
        
        
		String last=httpReq.getRequestParameter("CLASS");
		if(last==null) return " @break@";
		if(last.length()>0)
		{
			CharClass C=null;
            String newClassID=httpReq.getRequestParameter("NEWCLASS");
            if(C==null)
                C=(CharClass)httpReq.getRequestObjects().get("CLASS-"+last);
            if((C==null)
            &&(newClassID!=null)
            &&(newClassID.length()>0)
            &&(CMClass.getCharClass(newClassID)==null))
            {
                C=(CharClass)CMClass.getCharClass("GenCharClass").copyOf();
                C.setClassParms("<CCLASS><ID>"+newClassID+"</ID><NAME>"+CMStrings.capitalizeAndLower(newClassID)+"</NAME></CCLASS>");
                last=newClassID;
                httpReq.addRequestParameters("CLASS",newClassID);
            }
            if(C==null)
                C=CMClass.getCharClass(last);
			if(C!=null)
			{
				StringBuffer str=new StringBuffer("");
                if(parms.containsKey("NEWCLASS"))
                {
                    CharClass C2=CMClass.getCharClass(last);
                    return ""+(C2==null);
                }
                if(parms.containsKey("ISGENERIC"))
                {
                    CharClass C2=CMClass.getCharClass(C.ID());
                    return ""+((C2!=null)&&(C2.isGeneric()));
                }
                if(parms.containsKey("NAME"))
                {
                    String old=httpReq.getRequestParameter("NAME");
                    if(old==null) old=C.name();
                    str.append(old+", ");
                }
                if(parms.containsKey("NAMES"))
                {
                    String old=httpReq.getRequestParameter("NAME1");
                    DVector nameSet=new DVector(2);
                    int numNames=0;
                    boolean cSrc=false;
                    if(old==null)
                    {
                        C=C.makeGenCharClass();
                        numNames=CMath.s_int(C.getStat("NUMNAME"));
                        cSrc=true;
                    }
                    else
                    {
                        while(httpReq.isRequestParameter("NAME"+(numNames+1)))
                            numNames++;
                        
                    }
                    if(numNames<=0)
                        nameSet.addElement(new Integer(0),C.name());
                    else
                    for(int i=0;i<numNames;i++)
                    {
                        String lvlStr=cSrc?C.getStat("NAMELEVEL"+i):httpReq.getRequestParameter("NAMELEVEL"+(i+1));
                        if(CMath.isInteger(lvlStr))
                        {
                            int minLevel = CMath.s_int(lvlStr);
                            String name=cSrc?C.getStat("NAME"+i):httpReq.getRequestParameter("NAME"+(i+1));
                            if((name!=null)&&(name.length()>0))
                            {
                                if(nameSet.size()==0)
                                    nameSet.addElement(new Integer(minLevel),name);
                                else
                                {
                                    boolean added=false;
                                    for(int n=0;n<nameSet.size();n++)
                                        if(minLevel<((Integer)nameSet.elementAt(n,1)).intValue())
                                        {
                                            nameSet.insertElementAt(n,new Integer(minLevel),name);
                                            added=true;
                                            break;
                                        }
                                        else
                                        if(minLevel==((Integer)nameSet.elementAt(n,1)).intValue())
                                        {
                                            added=true;
                                            break;
                                        }
                                    if(!added)
                                        nameSet.addElement(new Integer(minLevel),name);
                                }
                            }
                        }
                    }
                    if(nameSet.size()==0)
                        nameSet.addElement(new Integer(0),C.name());
                    else
                        nameSet.setElementAt(0,1,new Integer(0));
                    int borderSize=1;
                    str.append("<TABLE WIDTH=100% BORDER=\""+borderSize+"\" CELLSPACING=0 CELLPADDING=0>");
                    String sfont=(parms.containsKey("FONT"))?("<FONT "+((String)parms.get("FONT"))+">"):"";
                    String efont=(parms.containsKey("FONT"))?"</FONT>":"";
                    if(parms.containsKey("HEADERCOL1")||parms.containsKey("HEADERCOL2"))
                    {
                        str.append("<TR><TD WIDTH=20%>");
                        if(parms.containsKey("HEADERCOL1"))
                            str.append(sfont + ((String)parms.get("HEADERCOL1")) + efont);
                        str.append("</TD><TD WIDTH=80%>");
                        if(parms.containsKey("HEADERCOL2"))
                            str.append(sfont + ((String)parms.get("HEADERCOL2")) + efont);
                        str.append("</TD></TR>");
                    }
                    for(int i=0;i<nameSet.size();i++)
                    {
                        Integer lvl=(Integer)nameSet.elementAt(i,1);
                        String name=(String)nameSet.elementAt(i,2);
                        str.append("<TR><TD WIDTH=20%>");
                        str.append("<INPUT TYPE=TEXT SIZE=5 NAME=NAMELEVEL"+(i+1)+" VALUE=\""+lvl.toString()+"\">");
                        str.append("</TD><TD WIDTH=80%>");
                        str.append("<INPUT TYPE=TEXT SIZE=30 NAME=NAME"+(i+1)+" VALUE=\""+name+"\">");
                        str.append("</TD></TR>");
                    }
                    str.append("<TR><TD WIDTH=25%>");
                    str.append("<INPUT TYPE=TEXT SIZE=5 NAME=NAMELEVEL"+(nameSet.size()+1)+" VALUE=\"\">");
                    str.append("</TD><TD WIDTH=50%>");
                    str.append("<INPUT TYPE=TEXT SIZE=30 NAME=NAME"+(nameSet.size()+1)+" VALUE=\"\">");
                    str.append("</TD></TR>");
                    str.append("</TABLE>");
                    str.append(", ");
                }
                if(parms.containsKey("BASE"))
                {
                    String old=httpReq.getRequestParameter("BASE");
                    if(old==null) 
                        old=C.baseClass();
                    else
                    {
                        CharClass pC=CMClass.getCharClass(old);
                        if(pC==null) pC=CMClass.findCharClass(old);
                        if(pC!=null) old=pC.ID();
                    }
                    str.append(old+", ");
                }
                if(parms.containsKey("HPDIV"))
                {
                    String old=httpReq.getRequestParameter("HPDIV");
                    if(old==null) old=""+C.getHPDivisor();
                    if(CMath.s_int(old)<=0)
                        old="1";
                    str.append(old+", ");
                }
                if(parms.containsKey("HPDICE"))
                {
                    String old=httpReq.getRequestParameter("HPDICE");
                    if(old==null) old=""+C.getHPDice();
                    if(CMath.s_int(old)<=0)
                        old="1";
                    str.append(old+", ");
                }
                if(parms.containsKey("HPDIE"))
                {
                    String old=httpReq.getRequestParameter("HPDIE");
                    if(old==null) old=""+C.getHPDie();
                    if(CMath.s_int(old)<=0)
                        old="1";
                    str.append(old+", ");
                }
                if(parms.containsKey("MANADIV"))
                {
                    String old=httpReq.getRequestParameter("MANADIV");
                    if(old==null) old=""+C.getManaDivisor();
                    if(CMath.s_int(old)<=0)
                        old="1";
                    str.append(old+", ");
                }
                if(parms.containsKey("MANADICE"))
                {
                    String old=httpReq.getRequestParameter("MANADICE");
                    if(old==null) old=""+C.getManaDice();
                    if(CMath.s_int(old)<=0)
                        old="1";
                    str.append(old+", ");
                }
                if(parms.containsKey("MANADIE"))
                {
                    String old=httpReq.getRequestParameter("MANADIE");
                    if(old==null) old=""+C.getManaDie();
                    if(CMath.s_int(old)<=0)
                        old="1";
                    str.append(old+", ");
                }
                if(parms.containsKey("LVLPRAC"))
                {
                    String old=httpReq.getRequestParameter("LVLPRAC");
                    if(old==null) old=""+C.getBonusPracLevel();
                    if(CMath.s_int(old)<=0) old="0";
                    str.append(old+", ");
                }
                if(parms.containsKey("LVLATT"))
                {
                    String old=httpReq.getRequestParameter("LVLATT");
                    if(old==null) old=""+C.getBonusAttackLevel();
                    if(CMath.s_int(old)<0) old="0";
                    str.append(old+", ");
                }
                if(parms.containsKey("ATTATT"))
                {
                    String old=httpReq.getRequestParameter("ATTATT");
                    if(old==null) old=""+C.getAttackAttribute();
                    if(CMath.s_int(old)<0) old="0";
                    for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
                        str.append("<OPTION VALUE=\""+i+"\""+((CMath.s_int(old)==i)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(CharStats.STAT_NAMES[i]));
                    str.append(", ");
                }
                if(parms.containsKey("FSTTRAN"))
                {
                    String old=httpReq.getRequestParameter("FSTTRAN");
                    if(old==null) old=""+C.getTrainsFirstLevel();
                    if(CMath.s_int(old)<0) old="0";
                    str.append(old+", ");
                }
                if(parms.containsKey("FSTPRAC"))
                {
                    String old=httpReq.getRequestParameter("FSTPRAC");
                    if(old==null) old=""+C.getPracsFirstLevel();
                    if(CMath.s_int(old)<0) old="0";
                    str.append(old+", ");
                }
                if(parms.containsKey("LVLDAM"))
                {
                    String old=httpReq.getRequestParameter("LVLDAM");
                    if(old==null) old=""+C.getLevelsPerBonusDamage();
                    if(CMath.s_int(old)<=0) old="1";
                    str.append(old+", ");
                }
                if(parms.containsKey("LVLMOVE"))
                { // movement multiplier?
                    String old=httpReq.getRequestParameter("LVLMOVE");
                    if(old==null) old=""+C.getMovementMultiplier();
                    if(CMath.s_int(old)<=0) old="0";
                    str.append(old+", ");
                }
                if(parms.containsKey("ARMOR"))
                { 
                    String old=httpReq.getRequestParameter("ARMOR");
                    if(old==null) {
                        C=C.makeGenCharClass();
                        old=""+C.getStat("ARMOR");
                    }
                    if(CMath.s_int(old)<=0) old="0";
                    for(int i=0;i<CharClass.ARMOR_LONGDESC.length;i++)
                        str.append("<OPTION VALUE=\""+i+"\""+((CMath.s_int(old)==i)?" SELECTED":"")+">"+CMStrings.capitalizeAndLower(CharClass.ARMOR_LONGDESC[i]));
                    str.append(", ");
                }
                if(parms.containsKey("STRLMT"))
                {
                    String old=httpReq.getRequestParameter("STRLMT");
                    if(old==null) old=""+C.otherLimitations();
                    str.append(old+", ");
                }
                if(parms.containsKey("STRBON"))
                {
                    String old=httpReq.getRequestParameter("STRBON");
                    if(old==null) old=""+C.otherBonuses();
                    str.append(old+", ");
                }
                if(parms.containsKey("QUAL"))
                {
                    String old=httpReq.getRequestParameter("QUAL");
                    if(old==null){
                        C=C.makeGenCharClass();
                        old=""+C.getStat("QUAL");
                    }
                    str.append(old+", ");
                }
                if(parms.containsKey("PLAYER"))
                {
                    String old=httpReq.getRequestParameter("PLAYER");
                    long mask=0;
                    if(old==null) 
                        mask=C.availabilityCode();
                    else
                        mask|=CMath.s_long(old);
                    for(int i=0;i<Area.THEME_DESCS_EXT.length;i++)
                        str.append("<OPTION VALUE="+i+" "+((i==mask)?"SELECTED":"")+">"+Area.THEME_DESCS_EXT[i]);
                    str.append(", ");
                }
                if(parms.containsKey("ESTATS")||parms.containsKey("CSTATS")||parms.containsKey("ASTATS")||parms.containsKey("ASTATE")||parms.containsKey("STARTASTATE"))
                {
                    C=C.makeGenCharClass();
                    
                    if(parms.containsKey("ESTATS"))
                    {
                        String eStats=C.getStat("ESTATS");
                        EnvStats adjEStats=(EnvStats)CMClass.getCommon("DefaultEnvStats"); adjEStats.setAllValues(0);
                        if(eStats.length()>0){ CMLib.coffeeMaker().setEnvStats(adjEStats,eStats);}
                        str.append(RaceData.estats(adjEStats,'E',httpReq,parms,0)+", ");
                    }
                    if(parms.containsKey("CSTATS"))
                    {
                        CharStats setStats=(CharStats)CMClass.getCommon("DefaultCharStats"); setStats.setAllValues(0);
                        String cStats=C.getStat("CSTATS");
                        if(cStats.length()>0){  CMLib.coffeeMaker().setCharStats(setStats,cStats);}
                        str.append(RaceData.cstats(setStats,'S',httpReq,parms,0)+", ");
                    }
                    if(parms.containsKey("ASTATS"))
                    {
                        CharStats adjStats=(CharStats)CMClass.getCommon("DefaultCharStats"); adjStats.setAllValues(0);
                        String cStats=C.getStat("ASTATS");
                        if(cStats.length()>0){  CMLib.coffeeMaker().setCharStats(adjStats,cStats);}
                        str.append(RaceData.cstats(adjStats,'A',httpReq,parms,0)+", ");
                    }
                    if(parms.containsKey("ASTATE"))
                    {
                        CharState adjState=(CharState)CMClass.getCommon("DefaultCharState"); adjState.setAllValues(0);
                        String aState=C.getStat("ASTATE");
                        if(aState.length()>0){  CMLib.coffeeMaker().setCharState(adjState,aState);}
                        str.append(RaceData.cstate(adjState,'A',httpReq,parms,0)+", ");
                    }
                    if(parms.containsKey("STARTASTATE"))
                    {
                        CharState startAdjState=(CharState)CMClass.getCommon("DefaultCharState"); startAdjState.setAllValues(0);
                        String saState=C.getStat("STARTASTATE");
                        if(saState.length()>0){ CMLib.coffeeMaker().setCharState(startAdjState,saState);}
                        str.append(RaceData.cstate(startAdjState,'S',httpReq,parms,0)+", ");
                    }
                }
                if(parms.containsKey("NOWEAPS"))
                {
                    String old=httpReq.getRequestParameter("NOWEAPS");
                    Vector set=null;
                    if(old==null) 
                    {
                        C=C.makeGenCharClass();
                        String weapList=C.getStat("GETWEP");
                        set=CMParms.parseCommas(weapList,true);
                    }
                    else
                    {
                        String id="";
                        set=new Vector();
                        for(int i=0;httpReq.isRequestParameter("NOWEAPS"+id);id=""+(++i))
                            set.addElement(httpReq.getRequestParameter("NOWEAPS"+id));
                    }
                    for(int i=0;i<Weapon.classifictionDescription.length;i++)
                    {
                        str.append("<OPTION VALUE="+i);
                        if(set.contains(""+i)) str.append(" SELECTED");
                        str.append(">"+Weapon.classifictionDescription[i]);
                    }
                    str.append(", ");
                }
                if(parms.containsKey("OUTFIT"))
                    str.append(RaceData.itemList(C.outfit(null),'O',httpReq,parms,0,false)+", ");
                if(parms.containsKey("DISFLAGS"))
                {
                    if(!httpReq.isRequestParameter("DISFLAGS"))
                    {
                        C=C.makeGenCharClass();
                        httpReq.addRequestParameters("DISFLAGS",C.getStat("DISFLAGS"));
                    }
                    int flags=CMath.s_int(httpReq.getRequestParameter("DISFLAGS"));
                    for(int i=0;i<CharClass.GENFLAG_DESCS.length;i++)
                    {
                        str.append("<OPTION VALUE="+CMath.pow(2,i));
                        if(CMath.bset(flags,CMath.pow(2,i)))
                            str.append(" SELECTED");
                        str.append(">"+CharClass.GENFLAG_DESCS[i]);
                    }
                    str.append(", ");
                }
                if(parms.containsKey("SECURITYSETS"))
                {
                    String old=httpReq.getRequestParameter("SSET1");
                    DVector sSet=new DVector(2);
                    int numSSet=0;
                    boolean cSrc=false;
                    if(old==null)
                    {
                        C=C.makeGenCharClass();
                        numSSet=CMath.s_int(C.getStat("NUMSSET"));
                        cSrc=true;
                    }
                    else
                    {
                        while(httpReq.isRequestParameter("SSET"+(numSSet+1)))
                            numSSet++;
                        
                    }
                    for(int i=0;i<numSSet;i++)
                    {
                        String lvlStr=cSrc?C.getStat("SSETLEVEL"+i):httpReq.getRequestParameter("SSETLEVEL"+(i+1));
                        if(CMath.isInteger(lvlStr))
                        {
                            int minLevel = CMath.s_int(lvlStr);
                            String sec = null;
                            if(cSrc)
                            {
                                sec=C.getStat("SSET"+i);
                                Vector V=CMParms.parse(sec);
                                sec=CMParms.combineWithX(V,",",0);
                            }
                            else
                                sec=httpReq.getRequestParameter("SSET"+(i+1));
                            if((sec!=null)&&(sec.trim().length()>0)&&(CMParms.parseCommas(sec,true).size()>0))
                            {
                                sec=CMParms.combineWithX(CMParms.parseCommas(sec.toUpperCase().trim(),true),",",0);
                                if(sSet.size()==0)
                                    sSet.addElement(new Integer(minLevel),sec);
                                else
                                {
                                    boolean added=false;
                                    for(int n=0;n<sSet.size();n++)
                                        if(minLevel<((Integer)sSet.elementAt(n,1)).intValue())
                                        {
                                            sSet.insertElementAt(n,new Integer(minLevel),sec);
                                            added=true;
                                            break;
                                        }
                                        else
                                        if(minLevel==((Integer)sSet.elementAt(n,1)).intValue())
                                        {
                                            added=true;
                                            break;
                                        }
                                    if(!added)
                                        sSet.addElement(new Integer(minLevel),sec);
                                }
                            }
                        }
                    }
                    int borderSize=1;
                    str.append("<TABLE WIDTH=100% BORDER=\""+borderSize+"\" CELLSPACING=0 CELLPADDING=0>");
                    String sfont=(parms.containsKey("FONT"))?("<FONT "+((String)parms.get("FONT"))+">"):"";
                    String efont=(parms.containsKey("FONT"))?"</FONT>":"";
                    if(parms.containsKey("HEADERCOL1")||parms.containsKey("HEADERCOL2"))
                    {
                        str.append("<TR><TD WIDTH=20%>");
                        if(parms.containsKey("HEADERCOL1"))
                            str.append(sfont + ((String)parms.get("HEADERCOL1")) + efont);
                        str.append("</TD><TD WIDTH=80%>");
                        if(parms.containsKey("HEADERCOL2"))
                            str.append(sfont + ((String)parms.get("HEADERCOL2")) + efont);
                        str.append("</TD></TR>");
                    }
                    for(int i=0;i<sSet.size();i++)
                    {
                        Integer lvl=(Integer)sSet.elementAt(i,1);
                        String sec=(String)sSet.elementAt(i,2);
                        str.append("<TR><TD WIDTH=20%>");
                        str.append("<INPUT TYPE=TEXT SIZE=5 NAME=SSETLEVEL"+(i+1)+" VALUE=\""+lvl.toString()+"\">");
                        str.append("</TD><TD WIDTH=80%>");
                        str.append("<INPUT TYPE=TEXT SIZE=60 NAME=SSET"+(i+1)+" VALUE=\""+sec+"\">");
                        str.append("</TD></TR>");
                    }
                    str.append("<TR><TD WIDTH=25%>");
                    str.append("<INPUT TYPE=TEXT SIZE=5 NAME=SSETLEVEL"+(sSet.size()+1)+" VALUE=\"\">");
                    str.append("</TD><TD WIDTH=50%>");
                    str.append("<INPUT TYPE=TEXT SIZE=60 NAME=SSET"+(sSet.size()+1)+" VALUE=\"\">");
                    str.append("</TD></TR>");
                    str.append("</TABLE>");
                    str.append(", ");
                }
                
                if(parms.containsKey("WEAPMATS"))
                {
                    String old=httpReq.getRequestParameter("WEAPMATS");
                    Vector set=null;
                    if(old==null) 
                    {
                        C=C.makeGenCharClass();
                        String matList=C.getStat("GETWMAT");
                        set=CMParms.parseCommas(matList,true);
                    }
                    else
                    {
                        String id="";
                        set=new Vector();
                        for(int i=0;httpReq.isRequestParameter("WEAPMATS"+id);id=""+(++i))
                            if(CMath.isInteger(httpReq.getRequestParameter("WEAPMATS"+id)))
                                set.addElement(httpReq.getRequestParameter("WEAPMATS"+id));
                    }
                    str.append("<OPTION VALUE=\"*\"");
                    if(set.size()==0) str.append(" SELECTED");
                    str.append(">ANY");
                    for(int i=0;i<RawMaterial.MATERIAL_DESCS.length;i++)
                    {
                        str.append("<OPTION VALUE="+(i<<8));
                        if(set.contains(""+(i<<8))) str.append(" SELECTED");
                        str.append(">"+RawMaterial.MATERIAL_DESCS[i]);
                    }
                    str.append(", ");
                }
                if(parms.containsKey("ARMORMINOR"))
                {
                    String old=httpReq.getRequestParameter("ARMORMINOR");
                    int armorMinor=-1;
                    if(old==null) 
                    {
                        C=C.makeGenCharClass();
                        armorMinor=CMath.s_int(C.getStat("ARMORMINOR"));
                    }
                    else
                        armorMinor=CMath.s_int(old);
                    str.append("<OPTION VALUE=-1");
                    if(armorMinor<0) str.append(" SELECTED");
                    str.append(">N/A");
                    for(int i=0;i<CMMsg.TYPE_DESCS.length;i++)
                    {
                        str.append("<OPTION VALUE="+i);
                        if(i==armorMinor) str.append(" SELECTED");
                        str.append(">"+CMMsg.TYPE_DESCS[i]);
                    }
                    str.append(", ");
                }
                if(parms.containsKey("STATCLASS"))
                {
                    String old=httpReq.getRequestParameter("STATCLASS");
                    if(old==null){
                        C=C.makeGenCharClass();
                        old=""+C.getStat("STATCLASS");
                    }
                    str.append(classDropDown(old));
                }
                if(parms.containsKey("EVENTCLASS"))
                {
                    String old=httpReq.getRequestParameter("EVENTCLASS");
                    if(old==null){
                        C=C.makeGenCharClass();
                        old=""+C.getStat("EVENTCLASS");
                    }
                    str.append(classDropDown(old));
                }
                if(parms.containsKey("CABILITIES"))
                    str.append(cabilities(C,httpReq,parms,1,"<FONT SIZE=-1 COLOR=WHITE>"));
                /******************************************************/
                // Here begins the displayable only fields.
                /******************************************************/
				if(parms.containsKey("HELP"))
				{
					StringBuffer s=CMLib.help().getHelpText(C.ID(),null,false);
					if(s==null)
						s=CMLib.help().getHelpText(C.name(),null,false);
					if(s!=null)
					{
						int limit=70;
						if(parms.containsKey("LIMIT")) limit=CMath.s_int((String)parms.get("LIMIT"));
						str.append(helpHelp(s,limit));
					}
				}
				if(parms.containsKey("PLAYABLE"))
					str.append(Area.THEME_DESCS_EXT[C.availabilityCode()]+", ");

				if(parms.containsKey("BASECLASS"))
					str.append(C.baseClass()+", ");

				if(parms.containsKey("MAXSTATS"))
					for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
						if(C.maxStatAdjustments()[i]!=0)
							str.append(CMStrings.capitalizeAndLower(CharStats.STAT_DESCS[i])+" ("+(CMProps.getIntVar(CMProps.SYSTEMI_BASEMAXSTAT)+C.maxStatAdjustments()[i])+"), ");
				if(parms.containsKey("PRACS"))
				{
					str.append(C.getPracsFirstLevel()+" plus (Wisdom/4)");
					if(C.getBonusPracLevel()>0)
						str.append("+"+C.getBonusPracLevel());
					else
					if(C.getBonusPracLevel()<0)
						str.append(""+C.getBonusPracLevel());
					str.append(" per level after first, ");
				}
				if(parms.containsKey("TRAINS"))
					str.append(C.getTrainsFirstLevel()+" plus 1 per level after first, ");
				if(parms.containsKey("DAMAGE"))
					str.append("An extra point of damage per "+C.getLevelsPerBonusDamage()+" level(s), ");
				if(parms.containsKey("QUALDOMAINLIST"))
				{
					Hashtable domains=new Hashtable();
					Ability A=null;
					String domain=null;
					for(Enumeration e=CMClass.abilities();e.hasMoreElements();)
					{
						A=(Ability)e.nextElement();
						if(CMLib.ableMapper().getQualifyingLevel(C.ID(),true,A.ID())>0)
						{
							if((A.classificationCode()&Ability.ALL_DOMAINS)==0)
								domain=Ability.ACODE_DESCS[A.classificationCode()];
							else
								domain=Ability.DOMAIN_DESCS[(A.classificationCode()&Ability.ALL_DOMAINS)>>5];
							Integer I=(Integer)domains.get(domain);
							if(I==null)I=new Integer(0);
							I=new Integer(I.intValue()+1);
							domains.remove(domain);
							domains.put(domain,I);
						}
					}
					Integer I=null;
					String winner=null;
					Integer winnerI=null;
					while(domains.size()>0)
					{
						winner=null;
						winnerI=null;
						for(Enumeration e=domains.keys();e.hasMoreElements();)
						{
							domain=(String)e.nextElement();
							I=(Integer)domains.get(domain);
							if((winnerI==null)||(I.intValue()>winnerI.intValue()))
							{
								winner=domain;
								winnerI=I;
							}
						}
						str.append(winner+"("+winnerI.intValue()+"), ");
						domains.remove(winner);
					}
				}
				
				if(parms.containsKey("HITPOINTS"))
					str.append("20 at first, plus (Constitution/"+C.getHPDivisor()+")+"+C.getHPDice()+"d"+C.getHPDie()+" per level thereafter, ");
				if(parms.containsKey("MANA"))
					str.append("100 plus (Intelligence/"+C.getManaDivisor()+")+"+C.getManaDice()+"d"+C.getManaDie()+" per level after first, ");
				if(parms.containsKey("MOVEMENT"))
					str.append("100 plus ((Strength/18)*"+C.getMovementMultiplier()+") per level after first, ");
				
				if(parms.containsKey("AVGHITPOINTS"))
				{
					int maxCon=18+C.maxStatAdjustments()[CharStats.STAT_CONSTITUTION];
					str.append("("+avgMath2(10,20,10,C.getHPDivisor(),C.getHPDice())+"/"+avgMath2(10,20,18,C.getHPDivisor(),C.getHPDice())+"/"+avgMath2(10,20,maxCon,C.getHPDivisor(),C.getHPDice())+") ");
					str.append("("+avgMath2(50,20,10,C.getHPDivisor(),C.getHPDice())+"/"+avgMath2(50,20,18,C.getHPDivisor(),C.getHPDice())+"/"+avgMath2(50,20,maxCon,C.getHPDivisor(),C.getHPDice())+") ");
					str.append("("+avgMath2(90,20,10,C.getHPDivisor(),C.getHPDice())+"/"+avgMath2(90,20,18,C.getHPDivisor(),C.getHPDice())+"/"+avgMath2(90,20,maxCon,C.getHPDivisor(),C.getHPDice())+") ");
				}
					
				if(parms.containsKey("AVGMANA"))
				{
					int maxInt=18+C.maxStatAdjustments()[CharStats.STAT_INTELLIGENCE];
					str.append("("+avgMath2(10,100,10,C.getManaDivisor(),C.getManaDice())+"/"+avgMath2(10,100,18,C.getManaDivisor(),C.getManaDice())+"/"+avgMath2(10,100,maxInt,C.getManaDivisor(),C.getManaDice())+") ");
					str.append("("+avgMath2(50,100,10,C.getManaDivisor(),C.getManaDice())+"/"+avgMath2(50,100,18,C.getManaDivisor(),C.getManaDice())+"/"+avgMath2(50,100,maxInt,C.getManaDivisor(),C.getManaDice())+") ");
					str.append("("+avgMath2(90,100,10,C.getManaDivisor(),C.getManaDice())+"/"+avgMath2(90,100,18,C.getManaDivisor(),C.getManaDice())+"/"+avgMath2(90,100,maxInt,C.getManaDivisor(),C.getManaDice())+") ");
				}
				if(parms.containsKey("AVGMOVEMENT"))
				{
					int ah=C.getMovementMultiplier();
					int maxStrength=18+C.maxStatAdjustments()[CharStats.STAT_STRENGTH];
					str.append("("+avgMath(10,ah,10,100)+"/"+avgMath(18,ah,10,100)+"/"+avgMath(maxStrength,ah,10,100)+") ");
					str.append("("+avgMath(10,ah,50,100)+"/"+avgMath(18,ah,50,100)+"/"+avgMath(maxStrength,ah,50,100)+") ");
					str.append("("+avgMath(10,ah,90,100)+"/"+avgMath(18,ah,90,100)+"/"+avgMath(maxStrength,ah,90,100)+") ");
				}
				
				StringBuffer preReqName=new StringBuffer(CharStats.STAT_DESCS[C.getAttackAttribute()].toLowerCase());
				preReqName.setCharAt(0,Character.toUpperCase(preReqName.charAt(0)));
				if(parms.containsKey("PRIME"))
					str.append(preReqName+", ");
				if(parms.containsKey("ATTACK"))
				{
					str.append("("+preReqName+"/6)");
					if(C.getBonusAttackLevel()>0)
						str.append("+"+C.getBonusAttackLevel());
					else
					if(C.getBonusAttackLevel()<0)
						str.append(""+C.getBonusAttackLevel());
					str.append(" per level after first, ");
				}
				if(parms.containsKey("WEAPONS"))
					if(C.weaponLimitations().length()>0)
						str.append(C.weaponLimitations()+", ");
					else
						str.append("Any, ");
				if(parms.containsKey("ARMORLIMITS"))
					if(C.armorLimitations().length()>0)
						str.append(C.armorLimitations()+", ");
					else
						str.append("Any, ");
				if(parms.containsKey("LIMITS"))
					if(C.otherLimitations().length()>0)
						str.append(C.otherLimitations()+", ");
					else
						str.append("None, ");
				if(parms.containsKey("BONUSES"))
					if(C.otherBonuses().length()>0)
						str.append(C.otherBonuses()+", ");
					else
						str.append("None, ");
				if(parms.containsKey("QUALS"))
					if(C.statQualifications().length()>0)
						str.append(C.statQualifications()+", ");
				if(parms.containsKey("STARTINGEQ"))
				{
					if(C.outfit(null)!=null)
					for(int i=0;i<C.outfit(null).size();i++)
					{
						Item I=(Item)C.outfit(null).elementAt(i);
						if(I!=null)
							str.append(I.name()+", ");
					}
				}
				if(parms.containsKey("BALANCE"))
					str.append(balanceChart(C));
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
                return clearWebMacros(strstr);
			}
		}
		return "";
	}
	
	public String balanceChart(CharClass C)
	{
		MOB M=CMClass.getMOB("StdMOB");
		M.baseEnvStats().setLevel(1);
		M.baseCharStats().setCurrentClass(C);
		M.recoverCharStats();
		C.startCharacter(M,false,false);
		HashSet seenBefore=new HashSet();
		int totalgained=0;
		int totalqualified=0;
		int uniqueClassSkills=0;
		int uniqueClassSkillsGained=0;
		int uncommonClassSkills=0;
		int uncommonClassSkillsGained=0;
		int totalCrossClassSkills=0;
		int totalCrossClassLevelDiffs=0;
		int maliciousSkills=0;
		int maliciousSkillsGained=0;
		int beneficialSkills=0;
		int beneficialSkillsGained=0;
		for(int l=1;l<=30;l++)
		{
			Vector set=CMLib.ableMapper().getLevelListings(C.ID(),true,l);
			for(int s=0;s<set.size();s++)
			{
				String able=(String)set.elementAt(s);
				if(able.equalsIgnoreCase("Skill_Recall")) continue;
				if(able.equalsIgnoreCase("Skill_Write")) continue;
				if(able.equalsIgnoreCase("Skill_Swim")) continue;
				if(CMLib.ableMapper().getQualifyingLevel("All",true,able)==l) continue;
				if(seenBefore.contains(able)) continue;
				seenBefore.add(able);
				int numOthers=0;
				int thisCrossClassLevelDiffs=0;
				for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
				{
					CharClass C2=(CharClass)c.nextElement();
					if(C2==C) continue;
					if(!CMProps.isTheme(C2.availabilityCode())) continue;
					if(C2.baseClass().equals(C.baseClass()))
					{
						int tlvl=CMLib.ableMapper().getQualifyingLevel(C2.ID(),true,able);
						if(tlvl>0)
						{
							if(tlvl>l)
								thisCrossClassLevelDiffs+=(tlvl-l);
							else
								thisCrossClassLevelDiffs+=(l-tlvl);
							numOthers++;
						}
					}
				}
				if(numOthers==0)
				{ 
					uniqueClassSkills++; 
					uncommonClassSkills++;
				}
				else
				{
					totalCrossClassLevelDiffs+=(thisCrossClassLevelDiffs/numOthers);
					totalCrossClassSkills++;
				}
				if(numOthers==1) 
					uncommonClassSkills++;
				boolean gained=(M.fetchAbility(able)!=null);
				if(gained)
				{
					totalgained++;
					if(numOthers==0){ uniqueClassSkillsGained++; uncommonClassSkillsGained++;}
					if(numOthers==1) uncommonClassSkillsGained++;
				}
				else
					totalqualified++;
				Ability A=CMClass.getAbility(able);
				if(A==null) continue;
				if((A.abstractQuality()==Ability.QUALITY_BENEFICIAL_OTHERS)
				   ||(A.abstractQuality()==Ability.QUALITY_BENEFICIAL_SELF))
				{
					beneficialSkills++;
					if(gained) beneficialSkillsGained++;
				}
				if(A.abstractQuality()==Ability.QUALITY_MALICIOUS)
				{
					maliciousSkills++;
					if(gained) maliciousSkillsGained++;
				}
			}
			CMLib.leveler().level(M);
		}
		StringBuffer str=new StringBuffer("");
		str.append("<BR>Rule#1: Avg gained skill/level: "+CMath.div(Math.round(100.0*CMath.div(totalgained,30)),(long)100));
		str.append("<BR>Rule#2: Avg qualified skill/level: "+CMath.div(Math.round(100.0*CMath.div(totalqualified,30)),(long)100));
		str.append("<BR>Rule#4: Unique class skills gained: "+uniqueClassSkillsGained+"/"+uniqueClassSkills);
		str.append("<BR>Rule#4: Uncommon class skills gained: "+uncommonClassSkillsGained+"/"+uncommonClassSkills);
		str.append("<BR>Rule#5: Combat skills gained: "+(maliciousSkillsGained+beneficialSkillsGained)+"/"+(maliciousSkills+beneficialSkills));
		str.append("<BR>Rule#6: Avg Unique class skill/level: "+CMath.div(Math.round(100.0*CMath.div(uniqueClassSkills,30)),(long)100));
		str.append("<BR>Rule#8: Avg Cross class skill/level: "+CMath.div(Math.round(100.0*CMath.div(totalCrossClassSkills,30)),(long)100));
        M.destroy();
		return str.toString();
	}
	
	public int avgMath2(int level, int add, int stat, int divisor, int hpdice )
	{
		return add+(level*((int)Math.round(CMath.div(stat,divisor)))+(hpdice*(hpdice+1)/2));
	}
	public int avgMath(int stat, int avg, int lvl, int add)
	{
		return add+(int)Math.round(CMath.mul(CMath.div(stat,18),avg)*lvl);
	}
	
}
