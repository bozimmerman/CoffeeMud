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
public class CrossClassAbilities extends StdWebMacro
{
	public String name()	{return "CrossClassAbilities";}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Vector rowsFavoring=new Vector();
		Vector allOtherRows=new Vector();
		String sort=httpReq.getRequestParameter("SORTBY");
		int sortByClassNum=-1;
		if((sort!=null)&&(sort.length()>0))
		{
			int cnum=0;
			for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
			{
				CharClass C=(CharClass)c.nextElement();
				if((C.ID().equals(sort))&&(C.playerSelectable()))
					sortByClassNum=cnum;
				cnum++;
			}
		}
		for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
		{
			Ability A=(Ability)a.nextElement();
			StringBuffer buf=new StringBuffer("");
			int numFound=0;
			for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
			{
				CharClass C=(CharClass)c.nextElement();
				if(C.playerSelectable()
				   &&(CMAble.getQualifyingLevel(C.ID(),true,A.ID())>=0))
					if((++numFound)>0) break;
			}
			if(numFound>0)
			{
				buf.append("<TR><TD><B>"+A.name()+"</B></TD>");
				int cnum=0;
				for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
				{
					CharClass C=(CharClass)c.nextElement();
					if(C.playerSelectable())
					{
						int qual=CMAble.getQualifyingLevel(C.ID(),true,A.ID());
						if(qual>=0)
						{
							buf.append("<TD>"+qual+"</TD>");
							if((cnum==sortByClassNum)&&(!rowsFavoring.contains(buf)))
								rowsFavoring.addElement(buf);
						}
						else
							buf.append("<TD><BR></TD>");
					}
					cnum++;
				}
				if(!rowsFavoring.contains(buf))
					allOtherRows.addElement(buf);
				buf.append("</TR>");
			}
		}
		StringBuffer buf=new StringBuffer("");
		for(int i=0;i<rowsFavoring.size();i++)
			buf.append((StringBuffer)rowsFavoring.elementAt(i));
		for(int i=0;i<allOtherRows.size();i++)
			buf.append((StringBuffer)allOtherRows.elementAt(i));
		return buf.toString();
	}

}
