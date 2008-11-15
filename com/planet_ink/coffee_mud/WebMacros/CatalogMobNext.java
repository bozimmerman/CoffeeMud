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
   Copyright 2000-2008 Bo Zimmerman

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
public class CatalogMobNext extends StdWebMacro
{
    public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
    public boolean isAdminMacro()   {return true;}

    public String runMacro(ExternalHTTPRequests httpReq, String parm)
    {
        Hashtable parms=parseParms(parm);
        String last=httpReq.getRequestParameter("MOB");
        if(parms.containsKey("RESET"))
        {   
            if(last!=null)
                httpReq.removeRequestParameter("MOB");
            httpReq.removeRequestParameter("CATALOG_MOB_USAGE");
            httpReq.removeRequestParameter("CATALOG_MOB_NAME");
            httpReq.removeRequestParameter("CATALOG_MOB_RACE");
            httpReq.removeRequestParameter("CATALOG_MOB_GENDER");
            httpReq.removeRequestParameter("CATALOG_MOB_LEVEL");
            httpReq.removeRequestParameter("CATALOG_MOB_CLASS");
            return "";
        }
        String lastID="";
        MOB M=null;
        String name=null;
        String[] names=CMLib.catalog().getCatalogMobNames();
        for(int s=0;s<names.length;s++)
        {
            name="CATALOG-"+names[s].toUpperCase().trim();
            if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!name.equalsIgnoreCase(lastID))))
            {
                //int[] usage=CMLib.catalog().getCatalogMobUsage(names[s]);
                M=CMLib.catalog().getCatalogMob(names[s]);
                if(M==null) continue;
                httpReq.addRequestParameters("MOB",name);
                httpReq.addRequestParameters("CATALOG_MOB_NAME",M.name());
                //httpReq.addRequestParameters("CATALOG_MOB_USAGE",""+usage[0]);
                httpReq.addRequestParameters("CATALOG_MOB_RACE",M.baseCharStats().raceName());
                httpReq.addRequestParameters("CATALOG_MOB_GENDER",M.baseCharStats().genderName());
                httpReq.addRequestParameters("CATALOG_MOB_LEVEL",""+M.baseEnvStats().level());
                httpReq.addRequestParameters("CATALOG_MOB_CLASS",M.ID());
                return "";
            }
            lastID=name;
        }
        httpReq.addRequestParameters("MOB","");
        httpReq.addRequestParameters("CATALOG_MOB_NAME","");
        httpReq.addRequestParameters("CATALOG_MOB_USAGE","");
        httpReq.addRequestParameters("CATALOG_MOB_RACE","");
        httpReq.addRequestParameters("CATALOG_MOB_GENDER","");
        httpReq.addRequestParameters("CATALOG_MOB_LEVEL","");
        httpReq.addRequestParameters("CATALOG_MOB_CLASS","");
        if(parms.containsKey("EMPTYOK"))
            return "<!--EMPTY-->";
        return " @break@";
    }
}