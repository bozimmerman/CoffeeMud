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
        // qmerrors
        // qmpagefields
        String qTemplate=httpReq.getRequestParameter("QMTEMPLATE");
        String qPageStr=httpReq.getRequestParameter("QMPAGE");
        String qPageErrors=httpReq.getRequestParameter("QMPAGEERRORS");
        
        if(parms.containsKey("QMPAGETITLE"))
        {
            // should load the template, and page
            // then return the pages title, if there is one
            return "not yet implemented";
        }
        else
        if(parms.containsKey("QMPAGEINSTR"))
        {
            // should load the template, and page
            // and return the pages instructions, if there is any
            return "not yet implemented";
        }
        else
        if(parms.containsKey("QMPAGEFIELDS"))
        {
            // should load the template, and page
            // and build the fields, labels, and so forth
            // for each input field. this will be BIG
            return "not yet implemented";
        }
        else
        if(parms.containsKey("QMLASTPAGE"))
        {
            // should load the template, and compare QMPAGE to
            // # of pages to return true or false
            return "false";
        }
        else
        if(parms.containsKey("QMPAGEERRORS")) return (qPageErrors==null)?"":qPageErrors;
        else
        if(parms.containsKey("QMTEMPLATE")) return qTemplate;
        else
        if(parms.containsKey("QMPAGE")) return qPageStr;
        else
        if(parms.containsKey("QMSTATE")) return qState;
        else
        if(parms.containsKey("NEXT"))
        {
            Vector V=httpReq.getAllRequestParameterKeys("^AT_(.+)");
            // this should EVALUATE the data submitted and populate QMERRORS
            // **HERE***
            // have it update all the above fields (esp qmpage, qmpageerrors
            // including the NEXT pages default data
            StringBuffer newState=new StringBuffer("<STATE>");
            for(int v=0;v<V.size();v++)
            {
                String key=(String)V.elementAt(v);
                if((!key.startsWith("AT_")))
                    continue;
                key=key.substring(3);
                newState.append("<"+key.toUpperCase()+">");
                newState.append(CMLib.xml().parseOutAngleBrackets(httpReq.getRequestParameter(key)));
                newState.append("</"+key.toUpperCase()+">");
            }
            newState.append("</STATE>");
            httpReq.addRequestParameters("QMSTATE",qState+newState.toString());
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
                    String tagName="AT_"+tag.tag;
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
