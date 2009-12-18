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
public class CrossBaseClassAbilities extends StdWebMacro
{
	public String name()	{return "CrossBaseClassAbilities";}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		StringBuffer buf=new StringBuffer("");
		
		Vector baseClasses=new Vector();
		for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
		{
			CharClass C=(CharClass)c.nextElement();
			if(CMProps.isTheme(C.availabilityCode()))
			{
				if(!baseClasses.contains(C.baseClass()))
				   baseClasses.addElement(C.baseClass());
			}
		}
		
		for(int b=0;b<baseClasses.size();b++)
		{
			String baseClass=(String)baseClasses.elementAt(b);
			Vector charClasses=new Vector();
			for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
			{
				CharClass C=(CharClass)c.nextElement();
				if((CMProps.isTheme(C.availabilityCode()))
				&&(C.baseClass().equals(baseClass))
				&&(!charClasses.contains(C.ID())))
					charClasses.addElement(C.ID());
			}
			
			Vector abilities=new Vector();
			Vector levelssum=new Vector();
			Vector numberare=new Vector();
			for(int c=0;c<charClasses.size();c++)
			{
				String className=(String)charClasses.elementAt(c);
				for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
				{
					Ability A=(Ability)a.nextElement();
					int level=CMLib.ableMapper().getQualifyingLevel(className,true,A.ID());
					if(level>=0)
					{
						int dex=abilities.indexOf(A.ID());
						if(dex<0)
						{
							abilities.addElement(A.ID());
							levelssum.addElement(Integer.valueOf(level));
							numberare.addElement(Integer.valueOf(1));
						}
						else
						{
							Integer I=(Integer)levelssum.elementAt(dex);
							levelssum.setElementAt(Integer.valueOf(I.intValue()+level),dex);
							Integer I2=(Integer)numberare.elementAt(dex);
							numberare.setElementAt(Integer.valueOf(I2.intValue()+1),dex);
						}
					}
				}
			}
			
			Vector sortedAbilities=new Vector();
			while(abilities.size()>0)
			{
				double lowAvg=Double.MAX_VALUE;
				int lowDex=-1;
				for(int i=0;i<abilities.size();i++)
				{
					Integer I=(Integer)levelssum.elementAt(i);
					Integer I2=(Integer)numberare.elementAt(i);
					double avg=CMath.div(I.intValue(),I2.intValue());
					if(avg<lowAvg)
					{
						lowAvg=avg;
						lowDex=i;
					}
				}
				if(lowDex>=0)
				{
					sortedAbilities.addElement(abilities.elementAt(lowDex));
					abilities.removeElementAt(lowDex);
					levelssum.removeElementAt(lowDex);
					numberare.removeElementAt(lowDex);
				}
			}
				
			buf.append("<BR><BR><BR><B><H3>"+baseClass+"</H3></B>\n\r");
			buf.append("<TABLE WIDTH=100% CELLSPACING=0 CELLPADDING=0 BORDER=1>\n\r");
			buf.append("<TR>");
			buf.append("<TD><B>Skill</B></TD>");
			for(int c=0;c<charClasses.size();c++)
			{
				String charClass=(String)charClasses.elementAt(c);
				buf.append("<TD><B>"+charClass+"</B></TD>");
			}
			buf.append("</TR>\n\r");
			for(int a=0;a<sortedAbilities.size();a++)
			{
				String able=(String)sortedAbilities.elementAt(a);
				buf.append("<TR><TD><B>"+able+"</B></TD>");
				for(int c=0;c<charClasses.size();c++)
				{
					String charClass=(String)charClasses.elementAt(c);
					int level=CMLib.ableMapper().getQualifyingLevel(charClass,true,able);
					if(level>=0)
						buf.append("<TD>"+level+"</TD>");
					else
						buf.append("<TD><BR></TD>");
				}
				buf.append("</TR>\n\r");
			}
			buf.append("</TABLE>");
		}
        return clearWebMacros(buf);
	}

}
