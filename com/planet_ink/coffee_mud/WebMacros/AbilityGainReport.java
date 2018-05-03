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
import com.planet_ink.coffee_web.interfaces.*;

import java.util.*;

/*
   Copyright 2005-2018 Bo Zimmerman

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

public class AbilityGainReport extends StdWebMacro
{
	@Override
	public String name()
	{
		return "AbilityGainReport";
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return CMProps.getVar(CMProps.Str.MUDSTATUS);
		String className=httpReq.getUrlParameter("CLASS");
		if(className==null)
			className="";
		final List<String> players=CMLib.database().getUserList();
		final HashSet<String> trainedFor=new HashSet<String>();
		final Hashtable<String,long[]> profSpent=new Hashtable<String,long[]>();
		for(final String playerName : players)
		{
			final MOB player=CMLib.players().getLoadPlayer(playerName);
			for(int a=0;a<player.numAbilities();a++)
			{
				final Ability A=player.fetchAbility(a);
				if(A==null)
					continue;
				boolean autogains=false;
				boolean qualifiesFor=false;
				int bestProf=-1;
				for(int c=0;c<player.baseCharStats().numClasses();c++)
				{
					final CharClass C=player.baseCharStats().getMyClass(c);
					if((className.length()==0)||(C.ID().equalsIgnoreCase(className)))
					{
						final int qlevel=CMLib.ableMapper().getQualifyingLevel(C.ID(),true,A.ID());
						if(qlevel>=player.baseCharStats().getClassLevel(C.ID()))
						{
							if(CMLib.ableMapper().getDefaultGain(C.ID(),true,A.ID()))
								autogains=true;
							qualifiesFor=true;
							if(CMLib.ableMapper().getDefaultProficiency(C.ID(),true,A.ID())>bestProf)
								bestProf=CMLib.ableMapper().getDefaultProficiency(C.ID(),true,A.ID());
						}
					}
				}
				if((!autogains)&&(qualifiesFor)&&(!trainedFor.contains(A.ID())))
					trainedFor.add(A.ID());
				if((qualifiesFor)&&(A.proficiency()>bestProf))
				{
					long[] stats=profSpent.get(A.ID());
					if(stats==null)
					{
						stats=new long[3];
						profSpent.put(A.ID(),stats);
					}
					stats[0]+=(A.proficiency()-bestProf);
					stats[1]++;
					stats[2]=stats[0]/stats[1];
				}
			}
		}
		final DVector sorted=new DVector(2);
		while(profSpent.size()>0)
		{
			final Enumeration<String> e=profSpent.keys();
			String bestKey=e.nextElement();
			long[] bestStat=profSpent.get(bestKey);
			for(;e.hasMoreElements();)
			{
				final String key=e.nextElement();
				final long[] stat=profSpent.get(key);
				if(stat[2]>bestStat[2])
				{
					bestKey=key;
					bestStat=stat;
				}
			}
			profSpent.remove(bestKey);
			sorted.addElement(bestKey,bestStat);
		}
		final StringBuffer buf=new StringBuffer("<TABLE WIDTH=100% BORDER=1>");
		buf.append("<TR><TD WIDTH=40%>Trained Skill</TD><TD WIDTH=30%>Avg. Prof. Gained</TD><TD WIDTH=30%>Instances</TD></TR>");
		buf.append("<TR><TD WIDTH=40%><BR></TD><TD WIDTH=30%><BR></TD><TD WIDTH=30%><BR></TD></TR>");
		for(int s=0;s<sorted.size();s++)
		{
			final String able=(String)sorted.elementAt(s,1);
			final long[] stats=(long[])sorted.elementAt(s,2);
			if(trainedFor.contains(able))
				buf.append("<TR><TD>"+able+"</TD><TD>"+stats[2]+"</TD><TD>"+stats[1]+"</TD></TR>");
		}
		for(final Iterator<String> i=trainedFor.iterator();i.hasNext();)
		{
			final String able=i.next();
			if(!sorted.contains(able))
				buf.append("<TR><TD>"+able+"</TD><TD>N/A</TD><TD>N/A</TD></TR>");
		}
		buf.append("</TABLE><P><BR><TABLE WIDTH=100% BORDER=1>");
		buf.append("<TR><TD WIDTH=40%>Possessed Skill</TD><TD WIDTH=30%>Avg. Prof. Gained</TD><TD WIDTH=30%>Instances</TD></TR>");
		buf.append("<TR><TD WIDTH=40%><BR></TD><TD WIDTH=30%><BR></TD><TD WIDTH=30%><BR></TD></TR>");
		for(int s=0;s<sorted.size();s++)
		{
			final String able=(String)sorted.elementAt(s,1);
			final long[] stats=(long[])sorted.elementAt(s,2);
			buf.append("<TR><TD>"+able+"</TD><TD>"+stats[2]+"</TD><TD>"+stats[1]+"</TD></TR>");
		}
		buf.append("</TABLE>");
		return clearWebMacros(buf);
	}

}
