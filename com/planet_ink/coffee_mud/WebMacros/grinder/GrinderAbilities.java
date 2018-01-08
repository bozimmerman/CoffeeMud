package com.planet_ink.coffee_mud.WebMacros.grinder;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.RoomData;
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
   Copyright 2008-2018 Bo Zimmerman

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

public class GrinderAbilities 
{
	public String name()
	{
		return "GrinderAbilities";
	}

	public static String modifyAbility(HTTPRequest httpReq, java.util.Map<String,String> parms, Ability oldA, Ability A)
	{
		final String replaceCommand=httpReq.getUrlParameter("REPLACE");
		if((replaceCommand != null)
		&& (replaceCommand.length()>0)
		&& (replaceCommand.indexOf('=')>0))
		{
			final int eq=replaceCommand.indexOf('=');
			final String field=replaceCommand.substring(0,eq);
			final String value=replaceCommand.substring(eq+1);
			httpReq.addFakeUrlParameter(field, value);
			httpReq.addFakeUrlParameter("REPLACE","");
		}
		String old;
		old=httpReq.getUrlParameter("NAME");
		A.setStat("NAME",(old==null)?"NAME":old);
		int x1=CMath.s_int(httpReq.getUrlParameter("CLASSIFICATION_ACODE"));
		final int x2=CMath.s_int(httpReq.getUrlParameter("CLASSIFICATION_DOMAIN"));
		A.setStat("CLASSIFICATION",""+((x2<<5)+x1));
		old=httpReq.getUrlParameter("TRIGSTR");
		A.setStat("TRIGSTR",(old==null)?"TRIGSTR":old.toUpperCase().trim());
		old=httpReq.getUrlParameter("MINRANGE");
		A.setStat("MINRANGE",(old==null)?"0":old);
		old=httpReq.getUrlParameter("MAXRANGE");
		A.setStat("MAXRANGE",(old==null)?"":old);
		old=httpReq.getUrlParameter("TICKSBETWEENCASTS");
		A.setStat("TICKSBETWEENCASTS",(old==null)?"0":old);
		old=httpReq.getUrlParameter("TICKSOVERRIDE");
		A.setStat("TICKSOVERRIDE",(old==null)?"0":old);
		old=httpReq.getUrlParameter("DISPLAY");
		A.setStat("DISPLAY",(old==null)?"DISPLAY":old);
		old=httpReq.getUrlParameter("TICKAFFECTS");
		A.setStat("TICKAFFECTS",(old==null)?"":""+old.equalsIgnoreCase("on"));
		old=httpReq.getUrlParameter("AUTOINVOKE");
		A.setStat("AUTOINVOKE",(old==null)?"":""+old.equalsIgnoreCase("on"));
		final Vector<String> V=new Vector<String>();
		if(httpReq.isUrlParameter("ABILITY_FLAGS"))
		{
			String id="";
			int num=0;
			for(;httpReq.isUrlParameter("ABILITY_FLAGS"+id);id=""+(++num))
				V.addElement(httpReq.getUrlParameter("ABILITY_FLAGS"+id));
		}
		A.setStat("FLAGS",CMParms.toListString(V));
		old=httpReq.getUrlParameter("GENHELP");
		A.setStat("HELP", old==null?"":old);
		old=httpReq.getUrlParameter("OVERRIDEMANA");
		x1=CMath.s_int(old);
		if(((x1>0)&&(x1<Ability.COST_PCT)))
			old=httpReq.getUrlParameter("CUSTOMOVERRIDEMANA");
		A.setStat("OVERRIDEMANA",(old==null)?"-1":old);
		V.clear();
		if(httpReq.isUrlParameter("USAGEMASK"))
		{
			String id="";
			int num=0;
			for(;httpReq.isUrlParameter("USAGEMASK"+id);id=""+(++num))
				V.addElement(httpReq.getUrlParameter("USAGEMASK"+id));
		}
		A.setStat("USAGEMASK",CMParms.toListString(V));
		V.clear();
		if(httpReq.isUrlParameter("MATLIST"))
		{
			String id="";
			int num=0;
			for(;httpReq.isUrlParameter("MATLIST"+id);id=""+(++num))
				V.addElement(httpReq.getUrlParameter("MATLIST"+id));
		}
		A.setStat("MATLIST",CMParms.toListString(V));
		V.clear();
		if(httpReq.isUrlParameter("CANAFFECTMASK"))
		{
			String id="";
			int num=0;
			for(;httpReq.isUrlParameter("CANAFFECTMASK"+id);id=""+(++num))
				V.addElement(httpReq.getUrlParameter("CANAFFECTMASK"+id));
		}
		A.setStat("CANAFFECTMASK",CMParms.toListString(V));
		V.clear();
		if(httpReq.isUrlParameter("CANTARGETMASK"))
		{
			String id="";
			int num=0;
			for(;httpReq.isUrlParameter("CANTARGETMASK"+id);id=""+(++num))
				V.addElement(httpReq.getUrlParameter("CANTARGETMASK"+id));
		}
		A.setStat("CANTARGETMASK",CMParms.toListString(V));
		old=httpReq.getUrlParameter("CANMEND");
		A.setStat("CANMEND",(old==null)?"false":Boolean.toString(old.equalsIgnoreCase("on")));
		old=httpReq.getUrlParameter("CANREFIT");
		A.setStat("CANREFIT",(old==null)?"false":Boolean.toString(old.equalsIgnoreCase("on")));
		old=httpReq.getUrlParameter("CANBUNDLE");
		A.setStat("CANBUNDLE",(old==null)?"false":Boolean.toString(old.equalsIgnoreCase("on")));
		old=httpReq.getUrlParameter("CANSIT");
		A.setStat("CANSIT",(old==null)?"false":Boolean.toString(old.equalsIgnoreCase("on")));
		old=httpReq.getUrlParameter("SOUND");
		A.setStat("SOUND",(old==null)?"":old);
		old=httpReq.getUrlParameter("VERB");
		A.setStat("VERB",(old==null)?"":old);
		old=httpReq.getUrlParameter("FILENAME");
		A.setStat("FILENAME",(old==null)?"":old);
		old=httpReq.getUrlParameter("VQUALITY");
		A.setStat("QUALITY",(old==null)?"":old);
		old=httpReq.getUrlParameter("HERESTATS");
		A.setStat("HERESTATS",(old==null)?"":old);
		old=httpReq.getUrlParameter("SCRIPT");
		A.setStat("SCRIPT",(old==null)?"":old);
		old=httpReq.getUrlParameter("CASTMASK");
		A.setStat("CASTMASK",(old==null)?"":old);
		old=httpReq.getUrlParameter("TARGETMASK");
		A.setStat("TARGETMASK",(old==null)?"":old);
		old=httpReq.getUrlParameter("FIZZLEMSG");
		A.setStat("FIZZLEMSG",(old==null)?"":old);
		old=httpReq.getUrlParameter("AUTOCASTMSG");
		A.setStat("AUTOCASTMSG",(old==null)?"":old);
		old=httpReq.getUrlParameter("CASTMSG");
		A.setStat("CASTMSG",(old==null)?"":old);
		old=httpReq.getUrlParameter("POSTCASTMSG");
		A.setStat("POSTCASTMSG",(old==null)?"":old);
		old=httpReq.getUrlParameter("ATTACKCODE");
		A.setStat("ATTACKCODE",(old==null)?"0":old);
		old=httpReq.getUrlParameter("POSTCASTDAMAGE");
		A.setStat("POSTCASTDAMAGE",(old==null)?"":old);
		V.clear();
		if(httpReq.isUrlParameter("POSTCASTAFFECT"))
		{
			String id="";
			int num=0;
			for(;httpReq.isUrlParameter("POSTCASTAFFECT"+id);id=""+(++num))
				V.addElement(httpReq.getUrlParameter("POSTCASTAFFECT"+id));
		}
		A.setStat("POSTCASTAFFECT",CMParms.toSemicolonListString(V));
		V.clear();
		if(httpReq.isUrlParameter("POSTCASTABILITY"))
		{
			String id="";
			int num=0;
			for(;httpReq.isUrlParameter("POSTCASTABILITY"+id);id=""+(++num))
				V.addElement(httpReq.getUrlParameter("POSTCASTABILITY"+id));
		}
		A.setStat("POSTCASTABILITY",CMParms.toSemicolonListString(V));
		if(A instanceof Language)
		{
			((Language)A).translationVector(A.ID()).clear();
			if(httpReq.isUrlParameter("WORDLIST1"))
			{
				int x=1;
				while(httpReq.isUrlParameter("WORDLIST"+x))
				{
					((Language)A).translationVector(A.ID())
					.add(CMParms.parseCommas(httpReq.getUrlParameter("WORDLIST"+x), true).toArray(new String[0]));
					x++;
				}
			}
			for(int i=((Language)A).translationVector(A.ID()).size()-1;i>=0;i--)
				if(((Language)A).translationVector(A.ID()).get(i).length==0)
					((Language)A).translationVector(A.ID()).remove(i);
				else
					break;
			((Language)A).translationHash(A.ID()).clear();
			if(httpReq.isUrlParameter("HASHWORD1"))
			{
				int x=1;
				while(httpReq.isUrlParameter("HASHWORD"+x))
				{
					final String word=httpReq.getUrlParameter("HASHWORD"+x).toUpperCase().trim();
					final String def=httpReq.getUrlParameter("HASHWORDDEF"+x);
					if((def!=null)&&(def.length()>0)&&(word.length()>0))
						((Language)A).translationHash(A.ID()).put(word,def);
					x++;
				}
			}
		}
		return "";
	}
}
