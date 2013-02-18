package com.planet_ink.coffee_mud.WebMacros.grinder;
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
public class GrinderAbilities {
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public static String modifyAbility(ExternalHTTPRequests httpReq, java.util.Map<String,String> parms, Ability oldA, Ability A)
	{
		String replaceCommand=httpReq.getRequestParameter("REPLACE");
		if((replaceCommand != null) 
		&& (replaceCommand.length()>0)
		&& (replaceCommand.indexOf('=')>0))
		{
			int eq=replaceCommand.indexOf('=');
			String field=replaceCommand.substring(0,eq);
			String value=replaceCommand.substring(eq+1);
			httpReq.addRequestParameters(field, value);
			httpReq.addRequestParameters("REPLACE","");
		}
		String old;
		old=httpReq.getRequestParameter("NAME");
		A.setStat("NAME",(old==null)?"NAME":old);
		int x1=CMath.s_int(httpReq.getRequestParameter("CLASSIFICATION_ACODE"));
		int x2=CMath.s_int(httpReq.getRequestParameter("CLASSIFICATION_DOMAIN"));
		A.setStat("CLASSIFICATION",""+((x2<<5)+x1));
		old=httpReq.getRequestParameter("TRIGSTR");
		A.setStat("TRIGSTR",(old==null)?"TRIGSTR":old.toUpperCase().trim());
		old=httpReq.getRequestParameter("MINRANGE");
		A.setStat("MINRANGE",(old==null)?"0":old);
		old=httpReq.getRequestParameter("MAXRANGE");
		A.setStat("MAXRANGE",(old==null)?"":old);
		old=httpReq.getRequestParameter("TICKSBETWEENCASTS");
		A.setStat("TICKSBETWEENCASTS",(old==null)?"0":old);
		old=httpReq.getRequestParameter("DISPLAY");
		A.setStat("DISPLAY",(old==null)?"DISPLAY":old);
		old=httpReq.getRequestParameter("AUTOINVOKE");
		A.setStat("AUTOINVOKE",(old==null)?"":""+old.equalsIgnoreCase("on"));
		Vector V=new Vector();
		if(httpReq.isRequestParameter("ABILITY_FLAGS"))
		{
			String id="";
			int num=0;
			for(;httpReq.isRequestParameter("ABILITY_FLAGS"+id);id=""+(++num))
				V.addElement(httpReq.getRequestParameter("ABILITY_FLAGS"+id));
		} 
		A.setStat("FLAGS",CMParms.toStringList(V));
		old=httpReq.getRequestParameter("GENHELP");
		A.setStat("HELP", old==null?"":old);
		old=httpReq.getRequestParameter("OVERRIDEMANA");
		x1=CMath.s_int(old);
		if(((x1>0)&&(x1<Ability.COST_PCT))) 
			old=httpReq.getRequestParameter("CUSTOMOVERRIDEMANA");
		A.setStat("OVERRIDEMANA",(old==null)?"-1":old);
		V.clear();
		if(httpReq.isRequestParameter("USAGEMASK"))
		{
			String id="";
			int num=0;
			for(;httpReq.isRequestParameter("USAGEMASK"+id);id=""+(++num))
				V.addElement(httpReq.getRequestParameter("USAGEMASK"+id));
		} 
		A.setStat("USAGEMASK",CMParms.toStringList(V));
		V.clear();
		if(httpReq.isRequestParameter("MATLIST"))
		{
			String id="";
			int num=0;
			for(;httpReq.isRequestParameter("MATLIST"+id);id=""+(++num))
				V.addElement(httpReq.getRequestParameter("MATLIST"+id));
		} 
		A.setStat("MATLIST",CMParms.toStringList(V));
		V.clear();
		if(httpReq.isRequestParameter("CANAFFECTMASK"))
		{
			String id="";
			int num=0;
			for(;httpReq.isRequestParameter("CANAFFECTMASK"+id);id=""+(++num))
				V.addElement(httpReq.getRequestParameter("CANAFFECTMASK"+id));
		} 
		A.setStat("CANAFFECTMASK",CMParms.toStringList(V));
		V.clear();
		if(httpReq.isRequestParameter("CANTARGETMASK"))
		{
			String id="";
			int num=0;
			for(;httpReq.isRequestParameter("CANTARGETMASK"+id);id=""+(++num))
				V.addElement(httpReq.getRequestParameter("CANTARGETMASK"+id));
		} 
		A.setStat("CANTARGETMASK",CMParms.toStringList(V));
		old=httpReq.getRequestParameter("CANMEND");
		A.setStat("CANMEND",(old==null)?"false":Boolean.toString(old.equalsIgnoreCase("on")));
		old=httpReq.getRequestParameter("CANREFIT");
		A.setStat("CANREFIT",(old==null)?"false":Boolean.toString(old.equalsIgnoreCase("on")));
		old=httpReq.getRequestParameter("CANBUNDLE");
		A.setStat("CANBUNDLE",(old==null)?"false":Boolean.toString(old.equalsIgnoreCase("on")));
		old=httpReq.getRequestParameter("CANSIT");
		A.setStat("CANSIT",(old==null)?"false":Boolean.toString(old.equalsIgnoreCase("on")));
		old=httpReq.getRequestParameter("SOUND");
		A.setStat("SOUND",(old==null)?"":old);
		old=httpReq.getRequestParameter("VERB");
		A.setStat("VERB",(old==null)?"":old);
		old=httpReq.getRequestParameter("FILENAME");
		A.setStat("FILENAME",(old==null)?"":old);
		old=httpReq.getRequestParameter("QUALITY");
		A.setStat("QUALITY",(old==null)?"":old);
		old=httpReq.getRequestParameter("HERESTATS");
		A.setStat("HERESTATS",(old==null)?"":old);
		old=httpReq.getRequestParameter("SCRIPT");
		A.setStat("SCRIPT",(old==null)?"":old);
		old=httpReq.getRequestParameter("CASTMASK");
		A.setStat("CASTMASK",(old==null)?"":old);
		old=httpReq.getRequestParameter("TARGETMASK");
		A.setStat("TARGETMASK",(old==null)?"":old);
		old=httpReq.getRequestParameter("FIZZLEMSG");
		A.setStat("FIZZLEMSG",(old==null)?"":old);
		old=httpReq.getRequestParameter("AUTOCASTMSG");
		A.setStat("AUTOCASTMSG",(old==null)?"":old);
		old=httpReq.getRequestParameter("CASTMSG");
		A.setStat("CASTMSG",(old==null)?"":old);
		old=httpReq.getRequestParameter("POSTCASTMSG");
		A.setStat("POSTCASTMSG",(old==null)?"":old);
		old=httpReq.getRequestParameter("ATTACKCODE");
		A.setStat("ATTACKCODE",(old==null)?"0":old);
		old=httpReq.getRequestParameter("POSTCASTDAMAGE");
		A.setStat("POSTCASTDAMAGE",(old==null)?"":old);
		V.clear();
		if(httpReq.isRequestParameter("POSTCASTAFFECT"))
		{
			String id="";
			int num=0;
			for(;httpReq.isRequestParameter("POSTCASTAFFECT"+id);id=""+(++num))
				V.addElement(httpReq.getRequestParameter("POSTCASTAFFECT"+id));
		} 
		A.setStat("POSTCASTAFFECT",CMParms.toSemicolonList(V));
		V.clear();
		if(httpReq.isRequestParameter("POSTCASTABILITY"))
		{
			String id="";
			int num=0;
			for(;httpReq.isRequestParameter("POSTCASTABILITY"+id);id=""+(++num))
				V.addElement(httpReq.getRequestParameter("POSTCASTABILITY"+id));
		} 
		A.setStat("POSTCASTABILITY",CMParms.toSemicolonList(V));
		if(A instanceof Language)
		{
			((Language)A).translationVector(A.ID()).clear();
			if(httpReq.isRequestParameter("WORDLIST1"))
			{
				int x=1;
				while(httpReq.isRequestParameter("WORDLIST"+x))
				{
					((Language)A).translationVector(A.ID())
					.add(CMParms.parseCommas(httpReq.getRequestParameter("WORDLIST"+x), true).toArray(new String[0]));
					x++;
				}
			}
			for(int i=((Language)A).translationVector(A.ID()).size()-1;i>=0;i--)
				if(((Language)A).translationVector(A.ID()).get(i).length==0)
					((Language)A).translationVector(A.ID()).remove(i);
				else
					break;
			((Language)A).translationHash(A.ID()).clear();
			if(httpReq.isRequestParameter("HASHWORD1"))
			{
				int x=1;
				while(httpReq.isRequestParameter("HASHWORD"+x))
				{
					String word=httpReq.getRequestParameter("HASHWORD"+x).toUpperCase().trim();
					String def=httpReq.getRequestParameter("HASHWORDDEF"+x);
					if((def!=null)&&(def.length()>0)&&(word.length()>0))
						((Language)A).translationHash(A.ID()).put(word,def);
					x++;
				}
			}
		}
		return "";
	}
}
