package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2013 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class ExitData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	private static final String[] okparms={
		"NAME","CLASSES","DISPLAYTEXT","DESCRIPTION",
		"LEVEL","LEVELRESTRICTED","ISTRAPPED","HASADOOR",
		"CLOSEDTEXT","DEFAULTSCLOSED","OPENWORD","CLOSEWORD",
		"HASALOCK","DEFAULTSLOCKED","KEYNAME","ISREADABLE",
		"READABLETEXT","ISCLASSRESTRICTED","RESTRICTEDCLASSES",
		"ISALIGNMENTRESTRICTED","RESTRICTEDALIGNMENTS",
		"MISCTEXT","ISGENERIC","DOORNAME","IMAGE","OPENTICKS"};
	public static String dispositions(Physical P,
									  boolean firstTime,
									  ExternalHTTPRequests httpReq,
									  java.util.Map<String,String> parms)
	{
		StringBuffer str=new StringBuffer("");
		for(int d=0;d<PhyStats.IS_CODES.length;d++)
		{
			if(parms.containsKey(PhyStats.IS_CODES[d]))
			{
				String parm=httpReq.getRequestParameter(PhyStats.IS_CODES[d]);
				if(firstTime)
					parm=(((P.basePhyStats().disposition()&(1<<d))>0)?"on":"");
				if((parm!=null)&&(parm.length()>0))
					str.append("checked");
			}
		}
		return str.toString();
	}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		java.util.Map<String,String> parms=parseParms(parm);

		String last=httpReq.getRequestParameter("ROOM");
		if(last==null) return " @break@";
		Room R=(Room)httpReq.getRequestObjects().get(last);
		if(R==null)
		{
			R=CMLib.map().getRoom(last);
			if(R==null)	return "No Room?!";
			httpReq.getRequestObjects().put(last,R);
		}

		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return CMProps.getVar(CMProps.SYSTEM_MUDSTATUS);

		String linkdir=httpReq.getRequestParameter("LINK");
		if(linkdir==null) return "@break@";
		int link=Directions.getGoodDirectionCode(linkdir);
		if((link<0)||(link>=Directions.NUM_DIRECTIONS())) return " @break@";

		Exit X=R.getRawExit(link);

		// important generic<->non generic swap!
		String newClassID=httpReq.getRequestParameter("CLASSES");
		if((newClassID!=null)&&(!newClassID.equals(CMClass.classID(X))))
				X=CMClass.getExit(newClassID);

		boolean firstTime=(!httpReq.isRequestParameter("ACTION"))
					||(!httpReq.getRequestParameter("ACTION").equals("MODIFYEXIT"))
					||(((httpReq.isRequestParameter("CHANGEDCLASS"))&&(httpReq.getRequestParameter("CHANGEDCLASS")).equals("true")));

		if(X==null) return "@break@";

		StringBuffer str=new StringBuffer("");
		for(int o=0;o<okparms.length;o++)
		if(parms.containsKey(okparms[o]))
		{
			String old=httpReq.getRequestParameter(okparms[o]);
			if(old==null) old="";
			switch(o)
			{
			case 0: // name
				if(firstTime) old=X.Name();
				str.append(old);
				break;
			case 1: // classes
				{
					if(firstTime) old=CMClass.classID(X);
					Object[] sorted=(Object[])Resources.getResource("MUDGRINDER-EXITS");
					if(sorted==null)
					{
						Vector sortMe=new Vector();
						for(Enumeration e=CMClass.exits();e.hasMoreElements();)
							sortMe.addElement(CMClass.classID(e.nextElement()));
						sorted=(new TreeSet(sortMe)).toArray();
						Resources.submitResource("MUDGRINDER-EXITS",sorted);
					}
					for(int r=0;r<sorted.length;r++)
					{
						String cnam=(String)sorted[r];
						str.append("<OPTION VALUE=\""+cnam+"\"");
						if(old.equals(cnam))
							str.append(" SELECTED");
						str.append(">"+cnam);
					}
				}
				break;
			case 2: // displaytext
				if(firstTime) old=X.displayText();
				str.append(old);
				break;
			case 3: // description
				if(firstTime) old=X.description();
				str.append(old);
				break;
			case 4: // level
				if(firstTime) old=""+X.basePhyStats().level();
				str.append(old);
				break;
			case 5: // levelrestricted;
				break;
			case 6: // istrapped
				break;
			case 7: // hasadoor
				if(firstTime)
					old=X.hasADoor()?"checked":"";
				else
				if(old.equals("on"))
					old="checked";
				str.append(old);
				break;
			case 8: // closedtext
				if(firstTime) old=X.closedText();
				if(old.length()==0) old="a closed door";
				str.append(old);
				break;
			case 9: // defaultsclosed
				if(firstTime)
					old=X.defaultsClosed()?"checked":"";
				else
				if(old.equals("on"))
					old="checked";
				str.append(old);
				break;
			case 10: // openword
				if(firstTime) old=X.openWord();
				if(old.length()==0) old="open";
				str.append(old);
				break;
			case 11: // closeword
				if(firstTime) old=X.closeWord();
				if(old.length()==0) old="close";
				str.append(old);
				break;
			case 12: // hasalock
				if(firstTime)
					old=X.hasALock()?"checked":"";
				else
				if(old.equals("on"))
					old="checked";
				str.append(old);
				break;
			case 13: // defaultslocked
				if(firstTime)
					old=X.defaultsLocked()?"checked":"";
				else
				if(old.equals("on"))
					old="checked";
				str.append(old);
				break;
			case 14: // keyname
				if(firstTime) old=X.keyName();
				str.append(old);
				break;
			case 15: // isreadable
				if(firstTime)
					old=X.isReadable()?"checked":"";
				else
				if(old.equals("on"))
					old="checked";
				str.append(old);
				break;
			case 16: // readable text
				if(firstTime) old=X.readableText();
				str.append(old);
				break;
			case 17: // isclassrestricuted
				break;
			case 18: // restrictedclasses
				break;
			case 19: // isalignmentrestricuted
				break;
			case 20: // restrictedalignments
				break;
			case 21: // misc text
				if(firstTime) old=X.text();
				str.append(old);
				break;
			case 22: // is generic
				if(X.isGeneric())
					return "true";
				return "false";
			case 23: // door name
				if(firstTime) old=X.doorName();
				if(old.length()==0) old="door";
				str.append(old);
				break;
			case 24: // image
				if(firstTime) old=X.rawImage();
				str.append(old);
				break;
			case 25: // open ticks
				if((firstTime)||(old==null)||(old.length()==0)) 
					old=Integer.toString(X.openDelayTicks());
				str.append(old);
				break;
			}
			if(firstTime)
				httpReq.addRequestParameters(okparms[o],old.equals("checked")?"on":old);

		}
		str.append(ExitData.dispositions(X,firstTime,httpReq,parms));
		str.append(AreaData.affects(X,httpReq,parms,1));
		str.append(AreaData.behaves(X,httpReq,parms,1));
		X.recoverPhyStats();
		X.text();

		String strstr=str.toString();
		if(strstr.endsWith(", "))
			strstr=strstr.substring(0,strstr.length()-2);
		return clearWebMacros(strstr);
	}
}
