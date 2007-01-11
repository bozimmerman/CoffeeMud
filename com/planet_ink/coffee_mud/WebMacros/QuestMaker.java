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
   Copyright 2000-2007 Bo Zimmerman

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
public class QuestMaker extends StdWebMacro
{
    public String name()    {return "QuestMaker";}

    public String runMacro(ExternalHTTPRequests httpReq, String parm)
    {
        Hashtable parms=parseParms(parm);
        if((parms==null)||(parms.size()==0)) return "";
        String qState=httpReq.getRequestParameter("QMSTATE");
        if((qState==null)||(qState.length()==0)) qState="";
        if(parms.containsKey("QMSTATE")) return qState;
        else
        if(parms.containsKey("NEXT"))
        {
            Vector V=httpReq.getAllRequestParameterKeys("^QM(.+)");
            StringBuffer newState=new StringBuffer("<STATE>");
            for(int v=0;v<V.size();v++)
            {
                String key=(String)V.elementAt(v);
                if((key.equalsIgnoreCase("QMSTATE"))
                ||(key.equalsIgnoreCase("QMPROCESS"))
                ||(key.equalsIgnoreCase("QMNEXT"))
                ||(key.equalsIgnoreCase("QMEVAL")))
                    continue;
                newState.append("<"+key.toUpperCase()+">");
                newState.append(CMLib.xml().parseOutAngleBrackets(httpReq.getRequestParameter(key)));
                newState.append("</"+key.toUpperCase()+">");
            }
            newState.append("</STATE>");
            httpReq.addRequestParameters("QMSTATE",qState+newState.toString());
            httpReq.addRequestParameters("QMDISPLAY",httpReq.getRequestParameter("QMNEXT"));
            // this should add new data to the qmstate from the page and
            // proceed on the assumption that the page knows its next display state
            // from QMDISPLAY
        }
        else
        if(parms.containsKey("BACK"))
        {
            Vector V=CMLib.xml().parseAllXML(qState);
            if(V.size()>0)
            {
                StringBuffer newBuf=new StringBuffer("");
                XMLLibrary.XMLpiece tagsFrom=(XMLLibrary.XMLpiece)V.lastElement();
                for(int v=0;v<V.size()-1;v++)
                    newBuf.append("<STATE>"+((XMLLibrary.XMLpiece)V.elementAt(v)).value+"</STATE>");
                httpReq.addRequestParameters("QMSTATE",qState+newBuf.toString());
                if(tagsFrom.contents!=null)
                for(int t=0;t<tagsFrom.contents.size();t++)
                {
                    XMLLibrary.XMLpiece tag=(XMLLibrary.XMLpiece)tagsFrom.contents.elementAt(t);
                    String tagName=tag.tag;
                    String tagValue=CMLib.xml().restoreAngleBrackets(tag.value);
                    httpReq.addRequestParameters(tagName,tagValue);
                }
                // this should remove the previous data from the qmstate, re-set the
                // QMDISPLAY parm to its previous value, and return.
            }
        }
        return "";
    }
}
