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
public class MudInfo extends StdWebMacro
{
    public String name()    {return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
    public boolean isAdminMacro()   {return false;}

    public String runMacro(ExternalHTTPRequests httpReq, String parm)
    {
        Hashtable parms=parseParms(parm);
        if(parms.containsKey("DOMAIN"))
            return CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN);
        if(parms.containsKey("EMAILOK"))
            return ""+(CMProps.getVar(CMProps.SYSTEM_MAILBOX).length()>0);
        if(parms.containsKey("MAILBOX"))
            return CMProps.getVar(CMProps.SYSTEM_MAILBOX);
        if(parms.containsKey("NAME"))
            return CMProps.getVar(CMProps.SYSTEM_MUDNAME);
        if(parms.containsKey("PORT"))
        {
            String ports=CMProps.getVar(CMProps.SYSTEM_MUDPORTS).trim();
            int x=ports.indexOf(" ");
            if(x<0)
                return clearWebMacros(ports);
            return clearWebMacros(ports.substring(0,x));
        }
        return "";
    }
}