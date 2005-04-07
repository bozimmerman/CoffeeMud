package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Snoop extends StdCommand
{
	public Snoop(){}

	private String[] access={"SNOOP"};
	public String[] getAccessWords(){return access;}
	
	private Vector snoopingOn(Session S)
	{
		Vector V=new Vector();
		for(int s=0;s<Sessions.size();s++)
			if(Sessions.elementAt(s).amBeingSnoopedBy(S))
				V.addElement(Sessions.elementAt(s));
		return V;
	}
	
	
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		if(mob.session()==null) return false;
		boolean doneSomething=false;
		for(int s=0;s<Sessions.size();s++)
		{
			Session S=Sessions.elementAt(s);
			if(S.amBeingSnoopedBy(mob.session()))
			{
				if(S.mob()!=null)
					mob.tell("You stop snooping on "+S.mob().name()+".");
				else
					mob.tell("You stop snooping on someone.");
				doneSomething=true;
				S.stopBeingSnoopedBy(mob.session());
			}
		}
		if(commands.size()==0)
		{
			if(!doneSomething)
				mob.tell("Snoop on whom?");
			return false;
		}
		String whom=Util.combine(commands,0);
		Session SnoopOn=null;
		for(int s=0;s<Sessions.size();s++)
		{
			Session S=Sessions.elementAt(s);
			if((S.mob()!=null)&&(EnglishParser.containsString(S.mob().name(),whom)))
			{
				if(S==mob.session())
				{
					mob.tell("no.");
					return false;
				}
				else
				if(CMSecurity.isAllowed(mob,S.mob().location(),"SNOOP"))
					SnoopOn=S;
			}
		}
		if(SnoopOn==null)
			mob.tell("You can't find anyone to snoop on by that name.");
		else
		if(CMSecurity.isASysOp(SnoopOn.mob())&&(!CMSecurity.isASysOp(mob)))
		    mob.tell("Only another Archon can snoop on "+mob.name()+".");
		else
		{
			Vector snoop=new Vector();
			snoop.addElement(SnoopOn);
			for(int v=0;v<snoop.size();v++)
			{
				if(snoop.elementAt(v)==mob.session())
				{
					mob.tell("This would create a snoop loop!");
					return false;
				}
				Vector V=snoopingOn((Session)snoop.elementAt(v));
				for(int v2=0;v2<V.size();v2++)
				{
					Session S2=(Session)V.elementAt(v2);
					if(!snoop.contains(S2))
						snoop.addElement(S2);
				}
			}
			mob.tell("You start snooping on "+SnoopOn.mob().name()+".");
			SnoopOn.startBeingSnoopedBy(mob.session());
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"SNOOP");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
