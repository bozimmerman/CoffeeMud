package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


/* 
   Copyright 2000-2004 Bo Zimmerman

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
		int scale=Util.s_int(httpReq.getRequestParameter("SCALE"));
		if(scale<=0) scale=1;
		int days=Util.s_int(httpReq.getRequestParameter("DAYS"));
		days=days*scale;
		if(days<=0) days=0;
		String code=httpReq.getRequestParameter("CODE");
		if((code==null)||(code.length()==0)) code="*";
		
		IQCalendar ENDQ=new IQCalendar(System.currentTimeMillis());
		ENDQ.add(Calendar.DATE,-days);
		ENDQ.set(Calendar.HOUR_OF_DAY,23);
		ENDQ.set(Calendar.MINUTE,59);
		ENDQ.set(Calendar.SECOND,59);
		ENDQ.set(Calendar.MILLISECOND,000);
		CoffeeTables.update();
		Vector V=CMClass.DBEngine().DBReadStats(ENDQ.getTimeInMillis()-1);
		if(V.size()==0){return "";}
		StringBuffer table=new StringBuffer("");
		IQCalendar C=new IQCalendar(System.currentTimeMillis());
		C.set(Calendar.HOUR_OF_DAY,23);
		C.set(Calendar.MINUTE,59);
		C.set(Calendar.SECOND,59);
		C.set(Calendar.MILLISECOND,999);
		long curTime=C.getTimeInMillis();
		long lastCur=0;
		while((V.size()>0)&&(curTime>(ENDQ.getTimeInMillis())))
		{
			lastCur=curTime;
			IQCalendar C2=new IQCalendar(curTime);
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
				CoffeeTables T=(CoffeeTables)V.elementAt(v);
				if((T.startTime()>curTime)&&(T.endTime()<=lastCur))
				{
					set.addElement(T);
					V.removeElementAt(v);
				}
			}
			long[] totals=new long[CoffeeTables.STAT_TOTAL];
			long highestOnline=0;
			long numberOnlineTotal=0;
			long numberOnlineCounter=0;
			for(int s=0;s<set.size();s++)
			{
				CoffeeTables T=(CoffeeTables)set.elementAt(s);
				T.totalUp(code,totals);
				if(T.highestOnline()>highestOnline) highestOnline=T.highestOnline();
				numberOnlineTotal+=T.numberOnlineTotal();
				numberOnlineCounter+=T.numberOnlineCounter();
			}
			long minsOnline=(totals[CoffeeTables.STAT_TICKSONLINE]*MudHost.TICK_TIME)/(1000*60);
			totals[CoffeeTables.STAT_TICKSONLINE]=(totals[CoffeeTables.STAT_TICKSONLINE]*MudHost.TICK_TIME)/(1000*60*60);
			double avgOnline=(numberOnlineCounter>0)?Util.div(numberOnlineTotal,numberOnlineCounter):0.0;
			avgOnline=Util.div(Math.round(avgOnline*10.0),10.0);
			table.append("<TR>");
			for(int i=0;i<orderedParms.size();i++)
			{
				String key=(String)orderedParms.elementAt(i,1);
				if(key.equalsIgnoreCase("DATERANGE")) table.append("<TD>"+header+new IQCalendar(curTime+1).d2DString()+" - "+new IQCalendar(lastCur-1).d2DString()+footer+"</TD>");
				else if(key.equalsIgnoreCase("DATESTART")) table.append("<TD>"+header+new IQCalendar(curTime+1).d2DString()+footer+"</TD>");
				else if(key.equalsIgnoreCase("DATEEND")) table.append("<TD>"+header+new IQCalendar(lastCur).d2DString()+footer+"</TD>");
				else if(key.equalsIgnoreCase("LOGINS")) table.append("<TD>"+header+totals[CoffeeTables.STAT_LOGINS]+footer+"</TD>");
				else if(key.equalsIgnoreCase("MOSTONLINE")) table.append("<TD>"+header+highestOnline+footer+"</TD>");
				else if(key.equalsIgnoreCase("AVERAGEONLINE")) table.append("<TD>"+header+avgOnline+footer+"</TD>");
				else if(key.equalsIgnoreCase("AVERAGETICKS")) table.append("<TD>"+header+((totals[CoffeeTables.STAT_LOGINS]>0)?(minsOnline/totals[CoffeeTables.STAT_LOGINS]):0)+"</TD>");
				else if(key.equalsIgnoreCase("TOTALHOURS")) table.append("<TD>"+header+totals[CoffeeTables.STAT_TICKSONLINE]+footer+"</TD>");
				else if(key.equalsIgnoreCase("NEWPLAYERS")) table.append("<TD>"+header+totals[CoffeeTables.STAT_NEWPLAYERS]+footer+"</TD>");
				else if(key.equalsIgnoreCase("DEATHS")) table.append("<TD>"+header+totals[CoffeeTables.STAT_DEATHS]+footer+"</TD>");
				else if(key.equalsIgnoreCase("PKDEATHS")) table.append("<TD>"+header+totals[CoffeeTables.STAT_PKDEATHS]+footer+"</TD>");
				else if(key.equalsIgnoreCase("CLASSCHANGES")) table.append("<TD>"+header+totals[CoffeeTables.STAT_CLASSCHANGE]+footer+"</TD>");
				else if(key.equalsIgnoreCase("PURGES")) table.append("<TD>"+header+totals[CoffeeTables.STAT_PURGES]+footer+"</TD>");
				else if(key.equalsIgnoreCase("MARRIAGES")) table.append("<TD>"+header+totals[CoffeeTables.STAT_MARRIAGES]+footer+"</TD>");
				else if(key.equalsIgnoreCase("BIRTHS")) table.append("<TD>"+header+totals[CoffeeTables.STAT_BIRTHS]+footer+"</TD>");
				else if(key.equalsIgnoreCase("DIVORCES")) table.append("<TD>"+header+totals[CoffeeTables.STAT_DIVORCES]+footer+"</TD>");
				else if(key.equalsIgnoreCase("LEVELSUP")) table.append("<TD>"+header+totals[CoffeeTables.STAT_LEVELSGAINED]+footer+"</TD>");
			}
			table.append("</TR>");
			if(scale==0) break;
		}
		return table.toString();
	}
}
