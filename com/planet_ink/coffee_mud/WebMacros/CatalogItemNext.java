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
public class CatalogItemNext extends StdWebMacro
{
    public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
    public boolean isAdminMacro()   {return true;}

    public String runMacro(ExternalHTTPRequests httpReq, String parm)
    {
        Hashtable parms=parseParms(parm);
        String last=httpReq.getRequestParameter("ITEM");
        if(parms.containsKey("RESET"))
        {   
            if(last!=null) httpReq.removeRequestParameter("ITEM");
            httpReq.removeRequestParameter("CATALOG_ITEM_NAME");
            httpReq.removeRequestParameter("CATALOG_ITEM_USAGE");
            httpReq.removeRequestParameter("CATALOG_ITEM_LEVEL");
            httpReq.removeRequestParameter("CATALOG_ITEM_CLASS");
            httpReq.removeRequestParameter("CATALOG_ITEM_VALUE");
            httpReq.removeRequestParameter("CATALOG_ITEM_RATE");
            httpReq.removeRequestParameter("CATALOG_ITEM_MASK");
            httpReq.removeRequestParameter("CATALOG_ITEM_LIVE");
            return "";
        }
        String lastID="";
        Item I=null;
        String name=null;
        CatalogLibrary.CataData data=null;
        for(int s=0;s<CMLib.catalog().getCatalogItems().size();s++)
        {
            I=CMLib.catalog().getCatalogItem(s);
            data=CMLib.catalog().getCatalogItemData(s);
            if(I==null) continue;
            name="CATALOG-"+I.Name().toUpperCase().trim();
            if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!name.equalsIgnoreCase(lastID))))
            {
                httpReq.addRequestParameters("ITEM",name);
                httpReq.addRequestParameters("CATALOG_ITEM_NAME",""+I.name());
                httpReq.addRequestParameters("CATALOG_ITEM_USAGE",""+CMLib.catalog().getCatalogItemUsage(s)[0]);
                httpReq.addRequestParameters("CATALOG_ITEM_LEVEL",""+I.baseEnvStats().level());
                httpReq.addRequestParameters("CATALOG_ITEM_CLASS",I.ID());
                httpReq.addRequestParameters("CATALOG_ITEM_VALUE",""+I.baseGoldValue());
                if(data.rate>0.0)
                {
                    httpReq.addRequestParameters("CATALOG_ITEM_RATE",CMath.toPct(data.rate));
                    httpReq.addRequestParameters("CATALOG_ITEM_MASK",data.lmaskStr==null?"":data.lmaskStr);
                    httpReq.addRequestParameters("CATALOG_ITEM_LIVE",""+data.live);
                }
                else
                {
                    httpReq.addRequestParameters("CATALOG_ITEM_RATE","");
                    httpReq.addRequestParameters("CATALOG_ITEM_MASK","");
                    httpReq.addRequestParameters("CATALOG_ITEM_LIVE","");
                }
                return "";
            }
            lastID=name;
        }
        httpReq.addRequestParameters("ITEM","");
        httpReq.addRequestParameters("CATALOG_ITEM_NAME","");
        httpReq.addRequestParameters("CATALOG_ITEM_USAGE","");
        httpReq.addRequestParameters("CATALOG_ITEM_LEVEL","");
        httpReq.addRequestParameters("CATALOG_ITEM_CLASS","");
        httpReq.addRequestParameters("CATALOG_ITEM_VALUE","");
        if(parms.containsKey("EMPTYOK"))
            return "<!--EMPTY-->";
        return " @break@";
    }
}