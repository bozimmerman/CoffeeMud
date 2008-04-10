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
public class AutoTitleData extends StdWebMacro
{
    public String name()    {return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

    public String deleteTitle(String title)
    {
        CMLib.login().dispossesTitle(title);
        CMFile F=new CMFile(Resources.makeFileResourceName("titles.txt"),null,true);
        if((F!=null) && (F.exists()))
        {
            boolean removed=Resources.findRemoveProperty(F, title);
            if(removed)
            {
                Resources.removeResource("titles.txt");
                CMLib.login().reloadAutoTitles();
                return null;
            }
        	return "Unable to delete title!";
        }
    	return "Unable to open titles.txt!";
	}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("AUTOTITLE");
		if((last==null)&&(!parms.containsKey("EDIT"))) return " @break@";

        if(parms.containsKey("EDIT"))
        {
            MOB M=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
            if(M==null) return "[authentication error]";
            if(!CMSecurity.isAllowed(M,M.location(),"TITLES")) return "[authentication error]";
            String newTitle=httpReq.getRequestParameter("TITLE");
            String newMask=httpReq.getRequestParameter("MASK");
            if((newTitle==null)||(newMask==null)||(newTitle.length()==0))
                return "[missing data error]";
            String error=CMLib.login().evaluateAutoTitle(newTitle+"="+newMask,false);
            if(error!=null) return "[error: "+error+"]";

            if((last!=null)&&(CMLib.login().isExistingAutoTitle(last)))
            {
                String err=deleteTitle(last);
                if(err!=null) return err;
            }
            if(CMLib.login().isExistingAutoTitle(newTitle))
                return "[new title already exists!]";
            CMFile F=new CMFile(Resources.makeFileResourceName("titles.txt"),null,true);
            F.saveText("\n"+newTitle+"="+newMask,true);
            Resources.removeResource("titles.txt");
            CMLib.login().reloadAutoTitles();
        }
        else
        if(parms.containsKey("DELETE"))
        {
            MOB M=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
            if(M==null) return "[authentication error]";
            if(!CMSecurity.isAllowed(M,M.location(),"TITLES")) return "[authentication error]";
            if(last==null) return " @break@";
            if(!CMLib.login().isExistingAutoTitle(last))
                return "Unknown title!";
            String err=deleteTitle(last);
            if(err==null) return "Auto-Title deleted.";
            return err;
        }
        else
        if(last==null) return " @break@";
        else
        if(last.length()>0)
        {
            StringBuffer str=new StringBuffer("");

            if(parms.containsKey("MASK"))
            {
                String mask=CMLib.login().getAutoTitleMask(last);
                str.append(mask+", ");
            }
            if(parms.containsKey("MASKDESC"))
            {
                String mask=CMLib.login().getAutoTitleMask(last);
                str.append(CMLib.masking().maskDesc(mask)+", ");
            }
            String strstr=str.toString();
            if(strstr.endsWith(", "))
                strstr=strstr.substring(0,strstr.length()-2);
            return clearWebMacros(strstr);
        }

        return "";
    }
}
