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
   Copyright 2000-2011 Bo Zimmerman

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
        
        String which=parms.get("WHICH");
        if((which==null)||(which.length()==0)) which="ALL";
        Map<String,Map<String,AbilityMapper.AbilityMapping>> allQualMap=CMLib.ableMapper().getAllQualifiesMap(httpReq.getRequestObjects());
        Map<String,AbilityMapper.AbilityMapping> map=allQualMap.get(which.toUpperCase().trim());
        if(map==null) return "";
        
        
        AbilityMapper.AbilityMapping mapped=map.get(last);
        if(mapped==null)
        {
        	if((which==null) && (allQualMap.get("EACH")!=null))
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
        	String s=httpReq.getRequestParameter("AUTOGAIN");
        	if(s==null) s=mapped.autoGain?"on":"";
        	str.append(s.equalsIgnoreCase("on")?"true":"false").append(", ");
        }
        
		if((mapped.originalSkillPreReqList!=null)&&(mapped.originalSkillPreReqList.trim().length()>0))
			str.append("REQUIRES=").append(CMParms.combineWith(CMParms.parseCommas(mapped.originalSkillPreReqList,true), ' ')).append(" ");

        
		String strstr=str.toString();
		if(strstr.endsWith(", "))
			strstr=strstr.substring(0,strstr.length()-2);
        return clearWebMacros(strstr);
    }
}
