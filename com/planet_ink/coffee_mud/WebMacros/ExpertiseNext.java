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
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.QualifyingID;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2006-2018 Bo Zimmerman

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
public class ExpertiseNext extends StdWebMacro
{
	@Override
	public String name()
	{
		return "ExpertiseNext";
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("EXPERTISE");
		if(parms.containsKey("RESET"))
		{
			if(last!=null)
				httpReq.removeUrlParameter("EXPERTISE");
			return "";
		}
		String lastID="";
		DVector experts=(DVector)httpReq.getRequestObjects().get("SORTED_EXPERTISE");
		ExpertiseLibrary.ExpertiseDefinition E=null;
		if(experts==null)
		{
			experts=new DVector(2);
			String Ename=null;
			String Vname=null;
			int x=0;
			for(final Enumeration e=CMLib.expertises().definitions();e.hasMoreElements();)
			{
				E=(ExpertiseLibrary.ExpertiseDefinition)e.nextElement();
				Ename=E.name();
				x=Ename.lastIndexOf(' ');
				if((x>0)&&(CMath.isRomanNumeral(Ename.substring(x).trim())))
					Ename=Ename.substring(0,x)+" "+((char)('a'+CMath.convertFromRoman(Ename.substring(x).trim())));
				boolean added=false;
				for(int v=0;v<experts.size();v++)
				{
					Vname=(String)experts.elementAt(v,1);
					if(Vname.compareTo(Ename)>0)
					{
						experts.insertElementAt(v,Ename,E);
						added=true;
						break;
					}
				}
				if(!added)
					experts.addElement(Ename,E);
			}
			httpReq.getRequestObjects().put("SORTED_EXPERTISE",experts);
		}
		Integer qualLevel=null;
		final String levelName=httpReq.getUrlParameter("LEVEL");
		final int levelCheck=((levelName!=null)&&(levelName.length()>0))?CMath.s_int(levelName):-1;
		final String className=httpReq.getUrlParameter("CLASS");
		Hashtable<String,Integer> expertsAllows=null;
		if((className!=null)&&(className.length()>0))
		{
			expertsAllows=(Hashtable)httpReq.getRequestObjects().get("ALLOWS-"+className.toUpperCase().trim());
			if(expertsAllows==null)
			{
				expertsAllows=new Hashtable<String,Integer>();
				httpReq.getRequestObjects().put("ALLOWS-"+className.toUpperCase().trim(),expertsAllows);
				final List<QualifyingID> DV=CMLib.ableMapper().getClassAllowsList(className);
				if(DV!=null)
				{
					for(final QualifyingID qID : DV)
					{
						final String xpertise=qID.ID();
						E=CMLib.expertises().getDefinition(xpertise);
						if(E!=null)
						{
							qualLevel=Integer.valueOf(qID.qualifyingLevel());
							int minLevel=E.getMinimumLevel();
							if((qualLevel!=null)&&(minLevel<qualLevel.intValue()))
								minLevel=qualLevel.intValue();
							expertsAllows.put(xpertise,Integer.valueOf(minLevel));
						}
					}
				}
			}
		}
		for(final Iterator<Object> e=experts.getDimensionList(2).iterator();e.hasNext();)
		{
			E=(ExpertiseLibrary.ExpertiseDefinition)e.next();
			if(E!=null)
			{
				if(expertsAllows!=null)
				{
					qualLevel=expertsAllows.get(E.ID());
					if(qualLevel==null)
						continue;
					if((levelCheck>=0)&&(levelCheck!=qualLevel.intValue()))
						continue;
				}
				else
				if((levelCheck>=0)&&(levelCheck!=E.getMinimumLevel()))
					continue;
				if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!E.ID().equals(lastID))))
				{
					httpReq.addFakeUrlParameter("EXPERTISE",E.ID());
					return "";
				}
				lastID=E.ID();
			}
		}
		httpReq.addFakeUrlParameter("EXPERTISE","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}

}
