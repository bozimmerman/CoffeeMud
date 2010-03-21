package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Poll.PollResult;
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
public class PollData extends StdWebMacro
{
    public String name()    {return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

    public String runMacro(ExternalHTTPRequests httpReq, String parm)
    {
        Hashtable parms=parseParms(parm);
        String last=httpReq.getRequestParameter("POLL");
        if((last==null)&&(!parms.containsKey("EDIT"))) return " @break@";


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

        if(parms.containsKey("EDIT"))
        {
			MOB M = Authenticate.getAuthenticatedMob(httpReq);
            if(M==null) return "[authentication error]";
            if(!CMSecurity.isAllowed(M,M.location(),"POLLS")) return "[authentication error]";
            String newTitle=httpReq.getRequestParameter("TITLE");
            if((newTitle==null)||(newTitle.length()==0))
                return "[missing data error]";

            if((last!=null)&&(last.length()==0)&&(CMLib.polls().getPoll(newTitle)!=null))
                return "[new title already exists!]";
            Poll P=CMLib.polls().getPoll(last);
            boolean create=false;
            if(P==null)
            {
                P=(Poll)CMClass.getCommon("DefaultPoll");
                create=true;
            }

            String old=httpReq.getRequestParameter("TITLE");
            if(old!=null) P.setName(old);
            old=httpReq.getRequestParameter("SUBJECT");
            if(old!=null) P.setSubject(old);
            old=httpReq.getRequestParameter("DESCRIPTION");
            if(old!=null) P.setDescription(old);
            old=httpReq.getRequestParameter("QUALZAPPER");
            if(old!=null) P.setQualZapper(old);
            long flag=0;
            old=httpReq.getRequestParameter("ISACTIVE");
            if((old!=null)&&(old.equalsIgnoreCase("on")))
                flag|=Poll.FLAG_ACTIVE;
            old=httpReq.getRequestParameter("ISPREVIEWRESULTS");
            if((old!=null)&&(old.equalsIgnoreCase("on")))
                flag|=Poll.FLAG_PREVIEWRESULTS;
            old=httpReq.getRequestParameter("ISABSTAIN");
            if((old!=null)&&(old.equalsIgnoreCase("on")))
                flag|=Poll.FLAG_ABSTAIN;
            old=httpReq.getRequestParameter("ISVOTEBYIP");
            if((old!=null)&&(old.equalsIgnoreCase("on")))
                flag|=Poll.FLAG_VOTEBYIP;
            old=httpReq.getRequestParameter("ISHIDERESULTS");
            if((old!=null)&&(old.equalsIgnoreCase("on")))
                flag|=Poll.FLAG_HIDERESULTS;
            old=httpReq.getRequestParameter("ISNOTATLOGIN");
            if((old!=null)&&(old.equalsIgnoreCase("on")))
                flag|=Poll.FLAG_NOTATLOGIN;
            P.setFlags(flag);
            old=httpReq.getRequestParameter("DOESEXPIRE");
            if((old==null)||(!old.equalsIgnoreCase("on")))
                P.setExpiration(0);
            else
            {
                String AP="AM";
                int hr=CMath.s_int(httpReq.getRequestParameter("HOUR"));
                if(hr>12){
                    hr-=12;
                    AP="PM";
                }
                P.setExpiration(CMLib.time().string2Date(
                   (CMath.s_int(httpReq.getRequestParameter("MONTH"))+1)
                   +"/"+httpReq.getRequestParameter("DAY")
                   +"/"+httpReq.getRequestParameter("YEAR")
                   +" "+hr
                   +":"+httpReq.getRequestParameter("MINUTE")
                   +" "+AP).getTimeInMillis());
            }
            P.getOptions().clear();
            int num=0;
            while(httpReq.isRequestParameter("OPTION"+(++num)))
            {
                old=httpReq.getRequestParameter("OPTION"+num);
                if((old!=null)&&(old.trim().length()>0))
                    P.getOptions().addElement(new Poll.PollOption(old));
            }
            if(create)
            {
                Log.sysOut(M.name()+" created poll "+P.getName());
                CMLib.polls().createPoll(P);
                return "Poll "+P.getName()+" created";
            }
            CMLib.polls().updatePoll(last, P);
            Log.sysOut(M.name()+" updated poll "+P.getName());
            return "Poll "+last+" updated";
        }
        else
        if(parms.containsKey("DELETE"))
        {
			MOB M = Authenticate.getAuthenticatedMob(httpReq);
            if(M==null) return "[authentication error]";
            if(!CMSecurity.isAllowed(M,M.location(),"POLLS")) return "[authentication error]";
            if(last==null) return " @break@";
            Poll P=CMLib.polls().getPoll(last);
            if(P==null)
                return "Unknown poll!";
            CMLib.polls().deletePoll(P);
            Log.sysOut(M.name()+" deleted poll "+last);
            return "Poll deleted.";
        }
        else
        {
            if(last==null) return " @break@";
            if(last.length()>0)
            {
                Poll P=null;
                String newPollID=httpReq.getRequestParameter("NEWPOLL");
                if(P==null)
                    P=(Poll)httpReq.getRequestObjects().get("POLL-"+last);
                if((P==null)
                &&(newPollID!=null)
                &&(newPollID.length()>0)
                &&(CMLib.polls().getPoll(newPollID)==null))
                {
                    P=(Poll)CMClass.getCommon("DefaultPoll");
                    P.setName(newPollID);
                    last=newPollID;
                    httpReq.addRequestParameters("POLL",newPollID);
                }
                if(P==null)
                {
                    P=CMLib.polls().getPoll(last);
                    if(P!=null)
                        CMLib.polls().loadPollIfNecessary(P);
                }
                if(parms.containsKey("ISNEWPOLL"))
                    return ""+(CMLib.polls().loadPollByName(last)==null);
                if(P!=null)
                {
                    StringBuffer str=new StringBuffer("");
                    boolean input=parms.containsKey("INPUT");
                    if(parms.containsKey("TITLE"))
                    {
                        String old=httpReq.getRequestParameter("TITLE");
                        if(old==null)
                            old=P.getName();
                        if(old!=null)
                            str.append(old+", ");
                    }
                    if(parms.containsKey("SUBJECT"))
                    {
                        String old=httpReq.getRequestParameter("SUBJECT");
                        if(old==null)
                            old=P.getSubject();
                        if(old!=null)
                            str.append(old+", ");
                    }
                    if(parms.containsKey("DESCRIPTION"))
                    {
                        String old=httpReq.getRequestParameter("DESCRIPTION");
                        if(old==null)
                            old=P.getDescription();
                        if(old!=null)
                            str.append(old+", ");
                    }
                    if(parms.containsKey("QUALZAPPER"))
                    {
                        String old=httpReq.getRequestParameter("QUALZAPPER");
                        if(old==null)
                            old=P.getQualZapper();
                        if(old!=null)
                            str.append(old+", ");
                    }
                    if(parms.containsKey("ISACTIVE"))
                    {
                        String old=httpReq.getRequestParameter("ISACTIVE");
                        if(old==null)
                            old=(CMath.bset(P.getFlags(),Poll.FLAG_ACTIVE)?"on":"");
                        if(old!=null)
                            str.append((old.equalsIgnoreCase("on")?"CHECKED":"")+", ");
                    }
                    if(parms.containsKey("ISPREVIEWRESULTS"))
                    {
                        String old=httpReq.getRequestParameter("ISPREVIEWRESULTS");
                        if(old==null)
                            old=(CMath.bset(P.getFlags(),Poll.FLAG_PREVIEWRESULTS)?"on":"");
                        if(old!=null)
                            str.append((old.equalsIgnoreCase("on")?"CHECKED":"")+", ");
                    }
                    if(parms.containsKey("ISABSTAIN"))
                    {
                        String old=httpReq.getRequestParameter("ISABSTAIN");
                        if(old==null)
                            old=(CMath.bset(P.getFlags(),Poll.FLAG_ABSTAIN)?"on":"");
                        if(old!=null)
                            str.append((old.equalsIgnoreCase("on")?"CHECKED":"")+", ");
                    }
                    if(parms.containsKey("ISVOTEBYIP"))
                    {
                        String old=httpReq.getRequestParameter("ISVOTEBYIP");
                        if(old==null)
                            old=(CMath.bset(P.getFlags(),Poll.FLAG_VOTEBYIP)?"on":"");
                        if(old!=null)
                            str.append((old.equalsIgnoreCase("on")?"CHECKED":"")+", ");
                    }
                    if(parms.containsKey("ISHIDERESULTS"))
                    {
                        String old=httpReq.getRequestParameter("ISHIDERESULTS");
                        if(old==null)
                            old=(CMath.bset(P.getFlags(),Poll.FLAG_HIDERESULTS)?"on":"");
                        if(old!=null)
                            str.append((old.equalsIgnoreCase("on")?"CHECKED":"")+", ");
                    }
                    if(parms.containsKey("ISNOTATLOGIN"))
                    {
                        String old=httpReq.getRequestParameter("ISNOTATLOGIN");
                        if(old==null)
                            old=(CMath.bset(P.getFlags(),Poll.FLAG_NOTATLOGIN)?"on":"");
                        if(old!=null)
                            str.append((old.equalsIgnoreCase("on")?"CHECKED":"")+", ");
                    }
                    if(parms.containsKey("DOESEXPIRE"))
                    {
                        String old=httpReq.getRequestParameter("DOESEXPIRE");
                        if(old==null)
                            old=(P.getExpiration()!=0)?"on":"";
                        str.append(""+old.equalsIgnoreCase("on"));
                    }
                    if(parms.containsKey("EXPIRATION")) // req input
                    {
                        Calendar exp=Calendar.getInstance();
                        if(httpReq.isRequestParameter("MONTH"))
                        {
                            String AP="AM";
                            int hr=CMath.s_int(httpReq.getRequestParameter("HOUR"));
                            if(hr>12){
                                hr-=12;
                                AP="PM";
                            }

                            String date=(CMath.s_int(httpReq.getRequestParameter("MONTH"))+1)
                                       +"/"+httpReq.getRequestParameter("DAY")
                                       +"/"+httpReq.getRequestParameter("YEAR")
                                       +" "+hr
                                       +":"+httpReq.getRequestParameter("MINUTE")
                                       +" "+AP;

                            exp=CMLib.time().string2Date(date);
                        }
                        else
                        {
                            long time=P.getExpiration();
                            if(time<1000) time=System.currentTimeMillis();
                            exp.setTimeInMillis(time);
                        }
                        if(!input)
                            str.append(CMLib.time().date2String(exp));
                        else
                        {
                            str.append("<SELECT NAME=MONTH>");
                            for(int m=0;m<12;m++)
                            {
                                str.append("<OPTION VALUE=\""+m+"\"");
                                if(m==exp.get(Calendar.MONTH))
                                    str.append(" SELECTED");
                                str.append(">"+CMLib.time().getMonthName(m+1,false));
                            }
                            str.append("</SELECT>");
                            str.append("/");
                            str.append("<SELECT NAME=DAY>");
                            int days = exp.getActualMaximum(Calendar.DAY_OF_MONTH); // 28
                            for(int d=1;d<=days;d++)
                            {
                                str.append("<OPTION VALUE=\""+d+"\"");
                                if(d==exp.get(Calendar.DAY_OF_MONTH))
                                    str.append(" SELECTED");
                                str.append(">"+d);
                            }
                            str.append("</SELECT>");
                            str.append("/");
                            str.append("<SELECT NAME=YEAR>");
                            int year=Calendar.getInstance().get(Calendar.YEAR);
                            if((exp.get(Calendar.YEAR)>1900)&&(exp.get(Calendar.YEAR)<year))
                                year=exp.get(Calendar.YEAR);
                            int doneYear=Calendar.getInstance().get(Calendar.YEAR)+10;
                            for(int y=year;y<=doneYear;y++)
                            {
                                str.append("<OPTION VALUE=\""+y+"\"");
                                if(y==exp.get(Calendar.YEAR))
                                    str.append(" SELECTED");
                                str.append(">"+y);
                            }
                            str.append("</SELECT>");
                            str.append("&nbsp;&nbsp;");
                            str.append("<SELECT NAME=HOUR>");
                            for(int h=1;h<=24;h++)
                            {
                                str.append("<OPTION VALUE=\""+h+"\"");
                                if(h==exp.get(Calendar.HOUR_OF_DAY))
                                    str.append(" SELECTED");
                                str.append(">"+h);
                            }
                            str.append("</SELECT>");
                            str.append(":");
                            str.append("<SELECT NAME=MINUTE>");
                            for(int m=0;m<=59;m++)
                            {
                                str.append("<OPTION VALUE=\""+m+"\"");
                                if(m==exp.get(Calendar.MINUTE))
                                    str.append(" SELECTED");
                                str.append(">"+CMLib.time().twoDigits(m));
                            }
                            str.append("</SELECT>");
                        }
                    }
                    if(parms.containsKey("NUMOPTIONS"))
                        str.append(""+P.getOptions().size()+", ");
                    if(parms.containsKey("NUMRESULTS"))
                        str.append(""+P.getResults().size()+", ");
                    if(parms.containsKey("OPTIONS"))
                    {
                        int which=-1;
                        if(parms.containsKey("NUM"))
                            which=CMath.s_int((String)parms.get("NUM"));
                        if(!httpReq.isRequestParameter("OPTION1"))
                        {
                            for(int v=0;v<P.getOptions().size();v++)
                            {
                                Poll.PollOption O=(Poll.PollOption)P.getOptions().elementAt(v);
                                httpReq.addRequestParameters("OPTION"+(v+1),O.text);
                            }
                        }
                        int num=0;
                        int showNum=0;
                        String sfont=httpReq.getRequestParameter("FONT");
                        String efont="";
                        if(sfont==null)
                            sfont="<BR>";
                        else
                        {
                            String s=sfont.toUpperCase().trim();
                            if(s.indexOf("<I>")>=0) efont+="</I>";
                            if(s.indexOf("<B>")>=0) efont+="</B>";
                            if(s.indexOf("<FONT")>=0) efont+="</FONT>";
                            if(s.indexOf("<P")>=0) efont+="</P>";
                            if(s.indexOf("<TD")>=0) efont+="</TD>";
                            if(s.indexOf("<TR")>=0) efont+="</TR>";
                        }

                        while(httpReq.isRequestParameter("OPTION"+(++num)))
                        {
                            String option=httpReq.getRequestParameter("OPTION"+num);
                            if((option.length()>0)
                            &&((which<0)||(which==showNum)))
                            {
                                ++showNum;
                                if(!input)
                                    str.append(sfont + option + efont);
                                else
                                    str.append(sfont + "<INPUT TYPE=TEXT NAME=OPTION"+(showNum)+" SIZE=60 MAXLENGTH=255 VALUE=\""+option+"\">" + efont);
                            }
                        }
                        if(input)
                        {
                            ++showNum;
                            str.append(sfont + "<INPUT TYPE=TEXT NAME=OPTION"+(showNum)+" SIZE=60 MAXLENGTH=255 VALUE=\"\">" + efont);
                        }
                    }
                    if(parms.containsKey("RESULTS"))
                    {
                        Vector options=P.getOptions();
                        Vector results=P.getResults();
                        int which=-1;
                        if(parms.containsKey("NUM"))
                            which=CMath.s_int((String)parms.get("NUM"));
                        int[] votes=new int[options.size()+(CMath.bset(P.getFlags(),Poll.FLAG_ABSTAIN)?1:0)];
                        PollResult R=null;
                        int choice=0;
                        int total=0;
                        for(int r=0;r<P.getResults().size();r++)
                        {
                            R=(PollResult)results.elementAt(r);
                            choice=CMath.s_int(R.answer);
                            if(((choice<=0)&&CMath.bset(P.getFlags(),Poll.FLAG_ABSTAIN))
                            ||((choice>=0)&&(choice<=options.size())))
                            {
                                total++;
                                if(choice<=0)
                                    votes[votes.length-1]++;
                                else
                                    votes[choice-1]++;
                            }
                        }
                        String sfont=httpReq.getRequestParameter("FONT");
                        String efont="";
                        if(sfont==null)
                            sfont="<BR>";
                        else
                        {
                            String s=sfont.toUpperCase().trim();
                            if(s.indexOf("<I>")>=0) efont+="</I>";
                            if(s.indexOf("<B>")>=0) efont+="</B>";
                            if(s.indexOf("<FONT")>=0) efont+="</FONT>";
                            if(s.indexOf("<P")>=0) efont+="</P>";
                            if(s.indexOf("<TD")>=0) efont+="</TD>";
                            if(s.indexOf("<TR")>=0) efont+="</TR>";
                        }
                        boolean pct=parms.containsKey("PCT")||parms.containsKey("PERCENT");
                        boolean showNum=(!pct)||parms.containsKey("VOTES")||parms.containsKey("VOTE")||parms.containsKey("COUNT");
                        for(int o=0;o<options.size();o++)
                        {
                            if((which<0)||(which==(o+1)))
                            {
                                str.append(sfont);
                                if(showNum)
                                    str.append(votes[o]);
                                if(pct)
                                {
                                    if(showNum)
                                        str.append(" (" + CMath.toPct(((double)votes[o])/((double)total))+")");
                                    else
                                        str.append(CMath.toPct(((double)votes[o])/((double)total)));
                                }
                                str.append(efont+", ");
                            }

                        }
                    }
                    if(parms.containsKey("MASKDESC"))
                    {
                        String mask=httpReq.getRequestParameter("MASK");
                        if((mask==null)&&(last.length()>0))
                            mask=CMLib.titles().getAutoTitleMask(last);
                        if(mask!=null)
                            str.append(CMLib.masking().maskDesc(mask)+", ");
                    }
                    String strstr=str.toString();
                    if(strstr.endsWith(", "))
                        strstr=strstr.substring(0,strstr.length()-2);
                    return clearWebMacros(strstr);
                }
            }
        }
        return " @break@";
    }
}
