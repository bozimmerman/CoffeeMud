package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;

import com.planet_ink.coffee_mud.core.exceptions.HTTPServerException;
import com.planet_ink.siplet.applet.*;

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
public class SipletInterface extends StdWebMacro
{
    public String name()    {return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

    public boolean isAWebPath(){return true;}
    public static final SHashtable<String,Pair<long[],Siplet>> siplets = new SHashtable<String,Pair<long[],Siplet>>(); 
	public static final LinkedList<String> removables=new LinkedList<String>();
    
    public void cleanOutTrash()
    {
		synchronized(siplets)
		{
			for(final String key : siplets.keySet())
			{
				Pair<long[],Siplet> p = siplets.get(key);
				if((p!=null)&&((System.currentTimeMillis()-p.first[0])>(2 * 60 * 1000)))
				{
					p.second.disconnectFromURL();
					removables.addLast(key);
				}
			}
			if(removables.size()>0)
			{
				for(final String remme : removables)
					siplets.remove(remme);
				removables.clear();
			}
		}
    }
    
    public String runMacro(ExternalHTTPRequests httpReq, String parm) throws HTTPServerException
    {
		cleanOutTrash();
		if(httpReq.isRequestParameter("CONNECT"))
		{
			String url=httpReq.getRequestParameter("URL");
			int port=CMath.s_int(httpReq.getRequestParameter("PORT"));
			String hex="";
			Siplet sip = new Siplet();
			boolean success=false;
			if(url!=null)
			{
				sip.init();
				success=sip.connectToURL(url, port);
				if(success)
				{
					synchronized(siplets)
					{
						int tokenNum=0;
						int tries=1000;
						while((tokenNum==0)&&((--tries)>0))
						{
							tokenNum = new Random().nextInt();
							if(tokenNum<0) tokenNum = tokenNum * -1;
							hex=Integer.toHexString(tokenNum);
							if(httpReq.isRequestParameter(hex))
								tokenNum=0;
						}
						siplets.put(hex, new Pair<long[],Siplet>(new long[]{System.currentTimeMillis()},sip));
					}
				}
			}
			return Boolean.toString(success)+';'+hex+';'+sip.info()+hex+';';
		}
		else
		if(httpReq.isRequestParameter("DISCONNECT"))
		{
			String token=httpReq.getRequestParameter("TOKEN");
			boolean success = false;
			if(token != null)
			{
				Pair<long[],Siplet> p = siplets.get(token);
				if(p!=null)
				{
					siplets.remove(token);
					p.second.disconnectFromURL();
					success=true;
				}
			}
			return Boolean.toString(success)+';';
		}
		else
		if(httpReq.isRequestParameter("SENDDATA"))
		{
			String token=httpReq.getRequestParameter("TOKEN");
			boolean success = false;
			if(token != null)
			{
				Pair<long[],Siplet> p = siplets.get(token);
				if(p!=null)
				{
					String data=httpReq.getRequestParameter("DATA");
					if(data!=null)
					{
						p.first[0]=System.currentTimeMillis();
						p.second.sendData(data);
						success=p.second.isConnectedToURL();
					}
				}
			}
			return Boolean.toString(success)+token+';';
		}
		else
		if(httpReq.isRequestParameter("POLL"))
		{
			String token=httpReq.getRequestParameter("TOKEN");
			boolean success = false;
			String data="";
			String jscript="";
			if(token != null)
			{
				Pair<long[],Siplet> p = siplets.get(token);
				if(p!=null)
				{
					if(p.second.isConnectedToURL())
					{
						p.first[0]=System.currentTimeMillis();
						p.second.readURLData();
						data = p.second.getURLData();
						jscript = p.second.getJScriptCommands();
						success=p.second.isConnectedToURL();
					}
				}
			}
			return Boolean.toString(success)+';'+data+token+';'+jscript+token+';';
		}
        return "false;";
    }
}
