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
public class AllQualifyData extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()   {return true;}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		java.util.Map<String,String> parms=parseParms(parm);
		String last=httpReq.getRequestParameter("ALLQUALID");
		String which=httpReq.getRequestParameter("ALLQUALWHICH");
		if(parms.containsKey("WHICH"))
			which=parms.get("WHICH");
		String origiWhich=which;
		if((which==null)||(which.length()==0)) which="ALL";
		Map<String,Map<String,AbilityMapper.AbilityMapping>> allQualMap=CMLib.ableMapper().getAllQualifiesMap(httpReq.getRequestObjects());
		Map<String,AbilityMapper.AbilityMapping> map=allQualMap.get(which.toUpperCase().trim());
		if(map==null) return "";
		
		AbilityMapper.AbilityMapping mapped=map.get(last);
		if(mapped==null)
		{
			if((origiWhich==null) && (allQualMap.get("EACH")!=null))
				mapped=allQualMap.get("EACH").get(last);
			if(mapped==null)
				return "";
		}
		StringBuilder str=new StringBuilder("");
		if(parms.containsKey("NAME"))
		{
			Ability A=CMClass.getAbility(last);
			if(A!=null) 
				str.append(A.name()).append(", ");
		}
		
		if(parms.containsKey("LEVEL"))
		{
			String lvl=httpReq.getRequestParameter("LEVEL");
			if(lvl==null) 
				lvl=Integer.toString(mapped.qualLevel);
			else
				lvl=Integer.toString(CMath.s_int(lvl));
			str.append(lvl).append(", ");
		}
		
		if(parms.containsKey("PROF"))
		{
			String prof=httpReq.getRequestParameter("PROF");
			if(prof==null) 
				prof=Integer.toString(mapped.defaultProficiency);
			else
				prof=Integer.toString(CMath.s_int(prof));
			str.append(prof).append(", ");
		}
		
		if(parms.containsKey("MASK"))
		{
			String s=httpReq.getRequestParameter("MASK");
			if(s==null) s=mapped.extraMask;
			str.append(s).append(", ");
		}
		
		if(parms.containsKey("AUTOGAIN"))
		{
			String s=httpReq.getRequestParameter("AUTOGAIN");
			if(s==null) s=mapped.autoGain?"on":"";
			str.append(s.equalsIgnoreCase("on")?"true":"false").append(", ");
		}
		
		if(parms.containsKey("REQUIRES"))
		{
			if(!httpReq.isRequestParameter("REQABLE1"))
			{
				int pnum=1;
				for(String s : CMParms.parseCommas(mapped.originalSkillPreReqList,true))
				{
					String ableID=s;
					String lvl="";
					int x=s.indexOf('(');
					if(s.endsWith(")")&&(x>1))
					{
						ableID=s.substring(0,x);
						lvl=s.substring(x+1,s.length()-1).trim();
					}
					Ability A=CMClass.getAbility(ableID);
					if(A!=null)
					{
						httpReq.addRequestParameters("REQABLE"+pnum, ableID);
						httpReq.addRequestParameters("REQLEVEL"+pnum, lvl);
						pnum++;
					}
				}
				httpReq.addRequestParameters("REQABLE"+pnum, "");
				httpReq.addRequestParameters("REQLEVEL"+pnum, "");
			}
			else
			{
				int curChkNum=1;
				int curWriteNum=1;
				while(httpReq.isRequestParameter("REQABLE"+curChkNum))
				{
					String curVal=httpReq.getRequestParameter("REQABLE"+curChkNum);
					if(curVal.equals("DEL")||curVal.equals("DELETE")||curVal.trim().length()==0)
					{
						curChkNum++;
						continue;
					}
					httpReq.addRequestParameters("REQABLE"+curWriteNum, curVal);
					httpReq.addRequestParameters("REQLEVEL"+curWriteNum, httpReq.getRequestParameter("REQLEVEL"+curChkNum));
					curChkNum++;
					curWriteNum++;
				}
				httpReq.removeRequestParameter("REQABLE"+curWriteNum);
				httpReq.removeRequestParameter("REQLEVEL"+curWriteNum);
			}
			if(parms.containsKey("RESET"))
			{
				httpReq.removeRequestParameter("REQUIRESNUM");
				httpReq.removeRequestParameter("REQUIRESNAME1");
				httpReq.removeRequestParameter("REQUIRESNAME2");
				return "";
			}
			if(parms.containsKey("NEXT"))
			{
				String lastR=httpReq.getRequestParameter("REQUIRESNUM");
				String lastID="";
				int curChkNum=1;
				int curWriteNum=1;
				while(httpReq.isRequestParameter("REQABLE"+curChkNum))
				{
					String thisName=Integer.toString(curChkNum);
					if((lastR==null)||((lastR.length()>0)&&(lastR.equals(lastID))&&(!thisName.equals(lastID))))
					{
						httpReq.addRequestParameters("REQUIRESNUM",thisName);
						lastR=thisName;
						httpReq.addRequestParameters("REQUIRESNAME1","REQABLE"+curWriteNum);
						httpReq.addRequestParameters("REQUIRESNAME2","REQLEVEL"+curWriteNum);
						return "";
					}
					curChkNum++;
					curWriteNum++;
					lastID=thisName;
				}
				httpReq.addRequestParameters("REQUIRESNUM","");
				httpReq.addRequestParameters("REQUIRESNAME1","REQABLE"+curWriteNum);
				httpReq.addRequestParameters("REQUIRESNAME2","REQLEVEL"+curWriteNum);
				if(parms.containsKey("EMPTYOK"))
					return "<!--EMPTY-->";
				return " @break@";
			}
			if(parms.containsKey("ABLEEDIT"))
			{
				String lastR=httpReq.getRequestParameter("REQUIRESNUM");
				String ableID=httpReq.getRequestParameter("REQABLE"+lastR);
				if((ableID!=null)&&(ableID.length()>0))
				{
					str.append("<OPTION VALUE=\"DEL\">Delete!");
					final Ability A=CMClass.getAbility(ableID);
					if(A!=null)
						str.append("<OPTION VALUE=\""+A.ID()+"\" SELECTED>"+A.ID());
				}
				else
				{
					str.append("<OPTION VALUE=\"\">Add New");
					for(Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						if((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON)
							continue;
						str.append("<OPTION VALUE=\""+A.ID()+"\">"+A.ID());
					}
				}
				str.append(", ");
			}
			
		}
		
		String strstr=str.toString();
		if(strstr.endsWith(", "))
			strstr=strstr.substring(0,strstr.length()-2);
		return clearWebMacros(strstr);
	}
}
