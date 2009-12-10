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
public class CoffeeTableRows extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	//HEADER, FOOTER, DATERANGE, DATESTART, DATEEND, LEVELSUP, DIVORCES, BIRTHS, MARRIAGES, PURGES, CLASSCHANGES, PKDEATHS, DEATHS, NEWPLAYERS, TOTALHOURS, AVERAGETICKS, AVERAGEONLINE, MOSTONLINE, LOGINS, 
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		if(parm.length()==0) parm="DATERANGE&LOGINS&MOSTONLINE&AVERAGEONLINE&TOTALHOURS&NEWPLAYERS&DEATHS&PKDEATHS&CLASSCHANGES&PURGES&MARRIAGES&BIRTHS&DIVORCES";
		Hashtable parms=parseParms(parm);
		DVector orderedParms=parseOrderedParms(parm);
		String header=(String)parms.get("HEADER");
		if(header==null) header="";
		String footer=(String)parms.get("FOOTER");
		if(footer==null) footer="";
		int scale=CMath.s_int(httpReq.getRequestParameter("SCALE"));
		if(scale<=0) scale=1;
		int days=CMath.s_int(httpReq.getRequestParameter("DAYS"));
		days=days*scale;
		if(days<=0) days=0;
		String code=httpReq.getRequestParameter("CODE");
		if((code==null)||(code.length()==0)) code="*";
		
        Calendar ENDQ=Calendar.getInstance();
		ENDQ.add(Calendar.DATE,-days);
		ENDQ.set(Calendar.HOUR_OF_DAY,23);
		ENDQ.set(Calendar.MINUTE,59);
		ENDQ.set(Calendar.SECOND,59);
		ENDQ.set(Calendar.MILLISECOND,000);
		CMLib.coffeeTables().update();
		Vector V=CMLib.database().DBReadStats(ENDQ.getTimeInMillis()-1);
		if(V.size()==0){return "";}
		StringBuffer table=new StringBuffer("");
        Calendar C=Calendar.getInstance();
		C.set(Calendar.HOUR_OF_DAY,23);
		C.set(Calendar.MINUTE,59);
		C.set(Calendar.SECOND,59);
		C.set(Calendar.MILLISECOND,999);
		long curTime=C.getTimeInMillis();
		long lastCur=0;
        String colspan="";
        if(orderedParms.contains("SKILLUSE"))
        {
            CharClass CharC=null;
            if(code.length()>1)
                CharC=CMClass.getCharClass(code.substring(1));
            Vector allSkills=new Vector();
            for(Enumeration e=CMClass.abilities();e.hasMoreElements();)
            {
                Ability A=(Ability)e.nextElement();
                if((CharC==null)||(CMLib.ableMapper().getQualifyingLevel(CharC.ID(),true,A.ID())>=0))
                    allSkills.addElement(A);
            }
            long[][] totals=new long[allSkills.size()][CoffeeTableRow.STAT_TOTAL];
            while((V.size()>0)&&(curTime>(ENDQ.getTimeInMillis())))
            {
                lastCur=curTime;
                Calendar C2=Calendar.getInstance();
                C.setTimeInMillis(curTime);
                C2.add(Calendar.DATE,-(scale));
                curTime=C2.getTimeInMillis();
                C2.set(Calendar.HOUR_OF_DAY,23);
                C2.set(Calendar.MINUTE,59);
                C2.set(Calendar.SECOND,59);
                C2.set(Calendar.MILLISECOND,999);
                curTime=C2.getTimeInMillis();
                Vector set=new Vector();
                if(V.size()==1)
                {
                    CoffeeTableRow T=(CoffeeTableRow)V.elementAt(0);
                    set.addElement(T);
                    V.removeElementAt(0);
                }
                else
                for(int v=V.size()-1;v>=0;v--)
                {
                    CoffeeTableRow T=(CoffeeTableRow)V.elementAt(v);
                    if((T.startTime()>curTime)&&(T.endTime()<=lastCur))
                    {
                        set.addElement(T);
                        V.removeElementAt(v);
                    }
                }
                for(int s=0;s<set.size();s++)
                {
                    CoffeeTableRow T=(CoffeeTableRow)set.elementAt(s);
                    for(int x=0;x<allSkills.size();x++)
                        T.totalUp("A"+((Ability)allSkills.elementAt(x)).ID().toUpperCase(),totals[x]);
                }
                if(scale==0) break;
            }
            int x=-1;
            Ability A=null;
            while(x<allSkills.size())
            {
                table.append("<TR>");
                for(int i=0;i<orderedParms.size();i++)
                {
                    String key=(String)orderedParms.elementAt(i,1);
                    if(key.equals("COLSPAN"))
                        colspan=" COLSPAN="+orderedParms.elementAt(i,2);
                    else
                    if(key.equalsIgnoreCase("NEXTSKILLID"))
                    {
                        x++;
                        if(x>=allSkills.size())
                            A=null;
                        else
                        {
                            A=(Ability)allSkills.elementAt(x);
                            table.append("<TD"+colspan+">"+header+A.ID()+footer+"</TD>");
                        }
                    }
                    else
                    if(key.equalsIgnoreCase("SKILLUSE"))
                    {
                        if(A!=null)
                            table.append("<TD"+colspan+">"+header+totals[x][CoffeeTableRow.STAT_SKILLUSE]+footer+"</TD>");
                    }
                }
                table.append("</TR>");
            }
        }
        else
        if(orderedParms.contains("QUESTNAME")||orderedParms.contains("QUESTRPT"))
        {
            long[][] totals=new long[CMLib.quests().numQuests()][CoffeeTableRow.STAT_TOTAL];
            while((V.size()>0)&&(curTime>(ENDQ.getTimeInMillis())))
            {
                lastCur=curTime;
                Calendar C2=Calendar.getInstance();
                C.setTimeInMillis(curTime);
                C2.add(Calendar.DATE,-(scale));
                curTime=C2.getTimeInMillis();
                C2.set(Calendar.HOUR_OF_DAY,23);
                C2.set(Calendar.MINUTE,59);
                C2.set(Calendar.SECOND,59);
                C2.set(Calendar.MILLISECOND,999);
                curTime=C2.getTimeInMillis();
                Vector set=new Vector();
                if(V.size()==1)
                {
                    CoffeeTableRow T=(CoffeeTableRow)V.elementAt(0);
                    set.addElement(T);
                    V.removeElementAt(0);
                }
                else
                for(int v=V.size()-1;v>=0;v--)
                {
                    CoffeeTableRow T=(CoffeeTableRow)V.elementAt(v);
                    if((T.startTime()>curTime)&&(T.endTime()<=lastCur))
                    {
                        set.addElement(T);
                        V.removeElementAt(v);
                    }
                }
                if(set.size()==0){ set.addAll(V); V.clear();}
                for(int s=0;s<set.size();s++)
                {
                    CoffeeTableRow T=(CoffeeTableRow)set.elementAt(s);
                    for(int x=0;x<CMLib.quests().numQuests();x++)
                        T.totalUp("U"+T.tagFix(CMLib.quests().fetchQuest(x).name()),totals[x]);
                }
                if(scale==0) break;
            }
            Quest Q=null;
            for(int x=0;x<CMLib.quests().numQuests();x++)
            {
            	Q=CMLib.quests().fetchQuest(x);
                table.append("<TR>");
                for(int i=0;i<orderedParms.size();i++)
                {
                    String key=(String)orderedParms.elementAt(i,1);
                    if(key.equals("COLSPAN"))
                        colspan=" COLSPAN="+orderedParms.elementAt(i,2);
                    else if(key.equalsIgnoreCase("QUESTNAME")) table.append("<TD"+colspan+">"+header+Q.name()+footer+"</TD>");
                    else if(key.equalsIgnoreCase("DATERANGE")) table.append("<TD"+colspan+">"+header+CMLib.time().date2DateString(curTime+1)+" - "+CMLib.time().date2DateString(lastCur-1)+footer+"</TD>");
    				else if(key.equalsIgnoreCase("DATESTART")) table.append("<TD"+colspan+">"+header+CMLib.time().date2DateString(curTime+1)+footer+"</TD>");
    				else if(key.equalsIgnoreCase("DATEEND")) table.append("<TD"+colspan+">"+header+CMLib.time().date2DateString(lastCur)+footer+"</TD>");
                    else if(key.equalsIgnoreCase("FAILEDSTART")) table.append("<TD"+colspan+">"+header+totals[x][CoffeeTableRow.STAT_QUESTFAILEDSTART]+footer+"</TD>");
                    else if(key.equalsIgnoreCase("TIMESTART")) table.append("<TD"+colspan+">"+header+totals[x][CoffeeTableRow.STAT_QUESTTIMESTART]+footer+"</TD>");
                    else if(key.equalsIgnoreCase("TIMESTOP")) table.append("<TD"+colspan+">"+header+totals[x][CoffeeTableRow.STAT_QUESTTIMESTOP]+footer+"</TD>");
                    else if(key.equalsIgnoreCase("STOP")) table.append("<TD"+colspan+">"+header+totals[x][CoffeeTableRow.STAT_QUESTSTOP]+footer+"</TD>");
                    else if(key.equalsIgnoreCase("ACCEPTED")) table.append("<TD"+colspan+">"+header+totals[x][CoffeeTableRow.STAT_QUESTACCEPTED]+footer+"</TD>");
                    else if(key.equalsIgnoreCase("FAILED")) table.append("<TD"+colspan+">"+header+totals[x][CoffeeTableRow.STAT_QUESTFAILED]+footer+"</TD>");
                    else if(key.equalsIgnoreCase("SUCCESS")) table.append("<TD"+colspan+">"+header+totals[x][CoffeeTableRow.STAT_QUESTSUCCESS]+footer+"</TD>");
                    else if(key.equalsIgnoreCase("DROPPED")) table.append("<TD"+colspan+">"+header+totals[x][CoffeeTableRow.STAT_QUESTDROPPED]+footer+"</TD>");
                    else if(key.equalsIgnoreCase("STARTATTEMPT")) table.append("<TD"+colspan+">"+header+totals[x][CoffeeTableRow.STAT_QUESTSTARTATTEMPT]+footer+"</TD>");
                }
                table.append("</TR>");
            }
        }
        else
		while((V.size()>0)&&(curTime>(ENDQ.getTimeInMillis())))
		{
			lastCur=curTime;
            Calendar C2=Calendar.getInstance();
            C2.setTimeInMillis(curTime);
			C2.add(Calendar.DATE,-scale);
			curTime=C2.getTimeInMillis();
			C2.set(Calendar.HOUR_OF_DAY,23);
			C2.set(Calendar.MINUTE,59);
			C2.set(Calendar.SECOND,59);
			C2.set(Calendar.MILLISECOND,999);
			curTime=C2.getTimeInMillis();
			Vector set=new Vector();
			for(int v=V.size()-1;v>=0;v--)
			{
				CoffeeTableRow T=(CoffeeTableRow)V.elementAt(v);
				if((T.startTime()>curTime)&&(T.endTime()<=lastCur))
				{
					set.addElement(T);
					V.removeElementAt(v);
				}
			}
			long[] totals=new long[CoffeeTableRow.STAT_TOTAL];
			long highestOnline=0;
			long numberOnlineTotal=0;
			long numberOnlineCounter=0;
			for(int s=0;s<set.size();s++)
			{
				CoffeeTableRow T=(CoffeeTableRow)set.elementAt(s);
				T.totalUp(code,totals);
				if(T.highestOnline()>highestOnline) highestOnline=T.highestOnline();
				numberOnlineTotal+=T.numberOnlineTotal();
				numberOnlineCounter+=T.numberOnlineCounter();
			}
			long minsOnline=(totals[CoffeeTableRow.STAT_TICKSONLINE]*Tickable.TIME_TICK)/(1000*60);
			totals[CoffeeTableRow.STAT_TICKSONLINE]=(totals[CoffeeTableRow.STAT_TICKSONLINE]*Tickable.TIME_TICK)/(1000*60*60);
			double avgOnline=(numberOnlineCounter>0)?CMath.div(numberOnlineTotal,numberOnlineCounter):0.0;
			avgOnline=CMath.div(Math.round(avgOnline*10.0),10.0);
			table.append("<TR>");
			for(int i=0;i<orderedParms.size();i++)
			{
				String key=(String)orderedParms.elementAt(i,1);
                if(key.equals("COLSPAN")) colspan=" COLSPAN="+orderedParms.elementAt(i,2);
                else if(key.equalsIgnoreCase("DATERANGE")) table.append("<TD"+colspan+">"+header+CMLib.time().date2DateString(curTime+1)+" - "+CMLib.time().date2DateString(lastCur-1)+footer+"</TD>");
				else if(key.equalsIgnoreCase("DATESTART")) table.append("<TD"+colspan+">"+header+CMLib.time().date2DateString(curTime+1)+footer+"</TD>");
				else if(key.equalsIgnoreCase("DATEEND")) table.append("<TD"+colspan+">"+header+CMLib.time().date2DateString(lastCur)+footer+"</TD>");
				else if(key.equalsIgnoreCase("LOGINS")) table.append("<TD"+colspan+">"+header+totals[CoffeeTableRow.STAT_LOGINS]+footer+"</TD>");
				else if(key.equalsIgnoreCase("MOSTONLINE")) table.append("<TD"+colspan+">"+header+highestOnline+footer+"</TD>");
				else if(key.equalsIgnoreCase("AVERAGEONLINE")) table.append("<TD"+colspan+">"+header+avgOnline+footer+"</TD>");
				else if(key.equalsIgnoreCase("AVERAGETICKS")) table.append("<TD"+colspan+">"+header+((totals[CoffeeTableRow.STAT_LOGINS]>0)?(minsOnline/totals[CoffeeTableRow.STAT_LOGINS]):0)+"</TD>");
				else if(key.equalsIgnoreCase("TOTALHOURS")) table.append("<TD"+colspan+">"+header+totals[CoffeeTableRow.STAT_TICKSONLINE]+footer+"</TD>");
				else if(key.equalsIgnoreCase("NEWPLAYERS")) table.append("<TD"+colspan+">"+header+totals[CoffeeTableRow.STAT_NEWPLAYERS]+footer+"</TD>");
				else if(key.equalsIgnoreCase("DEATHS")) table.append("<TD"+colspan+">"+header+totals[CoffeeTableRow.STAT_DEATHS]+footer+"</TD>");
				else if(key.equalsIgnoreCase("PKDEATHS")) table.append("<TD"+colspan+">"+header+totals[CoffeeTableRow.STAT_PKDEATHS]+footer+"</TD>");
				else if(key.equalsIgnoreCase("CLASSCHANGES")) table.append("<TD"+colspan+">"+header+totals[CoffeeTableRow.STAT_CLASSCHANGE]+footer+"</TD>");
				else if(key.equalsIgnoreCase("PURGES")) table.append("<TD"+colspan+">"+header+totals[CoffeeTableRow.STAT_PURGES]+footer+"</TD>");
				else if(key.equalsIgnoreCase("MARRIAGES")) table.append("<TD"+colspan+">"+header+totals[CoffeeTableRow.STAT_MARRIAGES]+footer+"</TD>");
				else if(key.equalsIgnoreCase("BIRTHS")) table.append("<TD"+colspan+">"+header+totals[CoffeeTableRow.STAT_BIRTHS]+footer+"</TD>");
				else if(key.equalsIgnoreCase("DIVORCES")) table.append("<TD"+colspan+">"+header+totals[CoffeeTableRow.STAT_DIVORCES]+footer+"</TD>");
				else if(key.equalsIgnoreCase("LEVELSUP")) table.append("<TD"+colspan+">"+header+totals[CoffeeTableRow.STAT_LEVELSGAINED]+footer+"</TD>");
			}
			table.append("</TR>");
			if(scale==0) break;
		}
        return clearWebMacros(table);
	}
}
