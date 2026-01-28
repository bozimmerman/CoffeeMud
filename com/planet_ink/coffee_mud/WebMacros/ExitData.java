package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.interfaces.*;
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
import com.planet_ink.coffee_mud.WebMacros.grinder.GrinderExits.ExitDataField;

import java.util.*;

/*
   Copyright 2005-2026 Bo Zimmerman

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
public class ExitData extends StdWebMacro
{
	@Override
	public String name()
	{
		return "ExitData";
	}

	public static String dispositions(final Physical P,
									  final boolean firstTime,
									  final HTTPRequest httpReq,
									  final java.util.Map<String,String> parms)
	{
		final StringBuffer str=new StringBuffer("");
		for(int d=0;d<PhyStats.IS_CODES.length;d++)
		{
			if(parms.containsKey(PhyStats.IS_CODES[d]))
			{
				String parm=httpReq.getUrlParameter(PhyStats.IS_CODES[d]);
				if(firstTime)
					parm=(((P.basePhyStats().disposition()&(1<<d))>0)?"on":"");
				if((parm!=null)&&(parm.length()>0))
					str.append("checked");
			}
		}
		return str.toString();
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);

		final String last=httpReq.getUrlParameter("ROOM");
		if(last==null)
			return " @break@";
		Room R=(Room)httpReq.getRequestObjects().get(last);
		if(R==null)
		{
			R=MUDGrinder.getRoomObject(httpReq, last);
			if(R==null)
				return "No Room?!";
			httpReq.getRequestObjects().put(last,R);
		}

		if(!CMProps.isState(CMProps.HostState.RUNNING))
			return CMProps.getVar(CMProps.Str.MUDSTATUS);

		final String linkdir=httpReq.getUrlParameter("LINK");
		if(linkdir==null)
			return "@break@";
		final int link=CMLib.directions().getGoodDirectionCode(linkdir);
		if((link<0)||(link>=Directions.NUM_DIRECTIONS()))
			return " @break@";

		Exit X=R.getRawExit(link);

		// important generic<->non generic swap!
		final String newClassID=httpReq.getUrlParameter("CLASSES");
		if((newClassID!=null)&&(!newClassID.equals(CMClass.classID(X))))
				X=CMClass.getExit(newClassID);

		final boolean firstTime=(!httpReq.isUrlParameter("ACTION"))
					||(!httpReq.getUrlParameter("ACTION").equals("MODIFYEXIT"))
					||(((httpReq.isUrlParameter("CHANGEDCLASS"))&&(httpReq.getUrlParameter("CHANGEDCLASS")).equals("true")));

		if(X==null)
			return "@break@";

		final StringBuffer str=new StringBuffer("");
		for(final ExitDataField field : ExitDataField.values())
		if(parms.containsKey(field.name()))
		{
			String old=httpReq.getUrlParameter(field.name());
			if(old==null)
				old="";
			switch(field)
			{
			case NAME: // name
				if(firstTime)
					old=X.Name();
				str.append(old);
				break;
			case CLASSES: // classes
				{
					if(firstTime)
						old=CMClass.classID(X);
					Object[] sorted=(Object[])Resources.getResource("MUDGRINDER-EXITS");
					if(sorted==null)
					{
						final Vector<String> sortMe=new Vector<String>();
						for(final Enumeration<Exit> e=CMClass.exits();e.hasMoreElements();)
							sortMe.addElement(CMClass.classID(e.nextElement()));
						sorted=(new TreeSet<String>(sortMe)).toArray();
						Resources.submitResource("MUDGRINDER-EXITS",sorted);
					}
					for (final Object element : sorted)
					{
						final String cnam=(String)element;
						str.append("<OPTION VALUE=\""+cnam+"\"");
						if(old.equals(cnam))
							str.append(" SELECTED");
						str.append(">"+cnam);
					}
				}
				break;
			case DISPLAYTEXT: // displaytext
				if(firstTime)
					old=X.displayText();
				str.append(old);
				break;
			case DESCRIPTION: // description
				if(firstTime)
					old=X.description();
				str.append(old);
				break;
			case LEVEL: // level
				if(firstTime)
					old=""+X.basePhyStats().level();
				str.append(old);
				break;
			case LEVELRESTRICTED: // levelrestricted;
				break;
			case ISTRAPPED: // istrapped
				break;
			case HASADOOR: // hasadoor
				if(firstTime)
					old=X.hasADoor()?"checked":"";
				else
				if(old.equals("on"))
					old="checked";
				str.append(old);
				break;
			case CLOSEDTEXT: // closedtext
				if(firstTime)
					old=X.closedText();
				str.append(old);
				break;
			case DEFAULTSCLOSED: // defaultsclosed
				if(firstTime)
					old=X.defaultsClosed()?"checked":"";
				else
				if(old.equals("on"))
					old="checked";
				str.append(old);
				break;
			case OPENWORD: // openword
				if(firstTime)
					old=X.openWord();
				if(old.length()==0)
					old="open";
				str.append(old);
				break;
			case CLOSEWORD: // closeword
				if(firstTime)
					old=X.closeWord();
				if(old.length()==0)
					old="close";
				str.append(old);
				break;
			case HASALOCK: // hasalock
				if(firstTime)
					old=X.hasALock()?"checked":"";
				else
				if(old.equals("on"))
					old="checked";
				str.append(old);
				break;
			case DEFAULTSLOCKED: // defaultslocked
				if(firstTime)
					old=X.defaultsLocked()?"checked":"";
				else
				if(old.equals("on"))
					old="checked";
				str.append(old);
				break;
			case KEYNAME: // keyname
				if(firstTime)
					old=X.keyName();
				str.append(old);
				break;
			case ISREADABLE: // isreadable
				if(firstTime)
					old=X.isReadable()?"checked":"";
				else
				if(old.equals("on"))
					old="checked";
				str.append(old);
				break;
			case READABLETEXT: // readable text
				if(firstTime)
					old=X.readableText();
				str.append(old);
				break;
			case ISCLASSRESTRICTED: // isclassrestricuted
				break;
			case RESTRICTEDCLASSES: // restrictedclasses
				break;
			case ISALIGNMENTRESTRICTED: // isalignmentrestricuted
				break;
			case RESTRICTEDALIGNMENTS: // restrictedalignments
				break;
			case MISCTEXT: // misc text
				if(firstTime)
					old=X.text();
				str.append(old);
				break;
			case ISGENERIC: // is generic
				if(X.isGeneric())
					return "true";
				return "false";
			case TAGS: // tags
				if(firstTime)
				{
					old="";
					for(final Enumeration<String> e=X.tags();e.hasMoreElements();)
						str.append(e.nextElement()).append(";");
				}
				else
					str.append(old);
				break;
			case DOORNAME: // door name
				if(firstTime)
					old=X.doorName();
				if(old.length()==0)
					old="door";
				str.append(old);
				break;
			case IMAGE: // image
				if(firstTime)
					old=X.rawImage();
				str.append(old);
				break;
			case OPENTICKS: // open ticks
				if((firstTime)||(old.length()==0))
					old=Integer.toString(X.openDelayTicks());
				str.append(old);
				break;
			}
			if(firstTime)
				httpReq.addFakeUrlParameter(field.name(),old.equals("checked")?"on":old);

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
