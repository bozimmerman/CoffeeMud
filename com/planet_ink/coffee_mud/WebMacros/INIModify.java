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
public class INIModify extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}
	
	public void updateINIFile(Vector page)
	{
		StringBuffer buf=new StringBuffer("");
		for(int p=0;p<page.size();p++)
			buf.append(((String)page.elementAt(p))+"\r\n");
        new CMFile(CMProps.getVar(CMProps.SYSTEM_INIPATH),null,false,true).saveText(buf);
	}

	public boolean modified(HashSet H, String s)
	{
		if(s.endsWith("*"))
			for(Iterator i=H.iterator();i.hasNext();)
			{
				if(((String)i.next()).startsWith(s.substring(0,s.length()-1)))
				   return true;
			}
		return H.contains(s);
	}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		if(parms==null) return "";
		Vector page=CMProps.loadEnumerablePage(CMProps.getVar(CMProps.SYSTEM_INIPATH));
		if(parms.containsKey("ADDKEY"))
		{
			String key=(String)parms.get("KEY");
			if((key==null)||(key.trim().length()==0)) return "";
			key=key.trim().toUpperCase();
            CMProps ipage=CMProps.loadPropPage(CMProps.getVar(CMProps.SYSTEM_INIPATH));
			if((ipage==null)||(!ipage.loaded)) return "";
			if(ipage.containsKey(key)) return "";
			int where=0;
			if(parms.containsKey("NEAR"))
			{
				boolean found=false;
				String near=(String)parms.get("NEAR");
				if(near.endsWith("*")) near=near.substring(0,near.length()-1);
				for(int p=0;p<page.size();p++)
				{
					String s=((String)page.elementAt(p)).trim();
					int x=s.indexOf(near);
					if(x==0) 
						found=true;
					else
					if((x>0)&&(!Character.isLetter(s.charAt(x-1))))
						found=true;
					if((!s.startsWith("#"))&&(!s.startsWith("!"))&&(found))
					{ where=p; break;}
				}
			}
			if(where>=0)
				page.insertElementAt(key+"=",where);
			else
				page.addElement(key+"=");
			Log.sysOut("INIModify","Key '"+key+"' added.");
			updateINIFile(page);
			return "";
		}
		else
		if(parms.containsKey("DELKEY"))
		{
			String key=(String)parms.get("KEY");
			if((key==null)||(key.trim().length()==0)) return "";
			key=key.trim().toUpperCase();
			for(int p=0;p<page.size();p++)
			{
				String s=((String)page.elementAt(p)).trim();
				if(s.startsWith("!")||s.startsWith("#")) continue;
				int x=s.indexOf("=");
				if(x<0) x=s.indexOf(":");
				if(x<0) continue;
				String thisKey=s.substring(0,x).trim().toUpperCase();
				if(thisKey.equals(key))
				{
					page.removeElementAt(p);
					Log.sysOut("INIModify","Key '"+thisKey+"' removed.");
					updateINIFile(page);
					break;
				}
			}
			return "";
		}
		else
		if(parms.containsKey("UPDATE"))
		{
			HashSet modified=new HashSet();
            CMProps ipage=CMProps.loadPropPage(CMProps.getVar(CMProps.SYSTEM_INIPATH));
			if((ipage==null)||(!ipage.loaded)) return "";
			for(int p=0;p<page.size();p++)
			{
				String s=((String)page.elementAt(p)).trim();
				if(s.startsWith("!")||s.startsWith("#")) continue;
				int x=s.indexOf("=");
				if(x<0) x=s.indexOf(":");
				if(x<0) continue;
				String thisKey=s.substring(0,x).trim().toUpperCase();
                
				if(httpReq.isRequestParameter(thisKey)
				&&(ipage.containsKey(thisKey))
				&&(!modified.contains(thisKey))
				&&(!httpReq.getRequestParameter(thisKey).equals(ipage.getStr(thisKey))))
				{
					modified.add(thisKey);
					Log.sysOut("INIModify","Key '"+thisKey+"' modified.");
					page.setElementAt(thisKey+"="+httpReq.getRequestParameter(thisKey),p);
				}
			}
			if(modified.size()>0)
			{
                if(modified.contains("JSCRIPTS")) return ""; // never modified through this
				updateINIFile(page);
				ipage=CMProps.loadPropPage(CMProps.getVar(CMProps.SYSTEM_INIPATH));
				if((ipage==null)||(!ipage.loaded)) return "";
				ipage.resetSystemVars();
				if(modified(modified,"SYSOPMASK"))
					CMSecurity.setSysOp(ipage.getStr("SYSOPMASK"));
				if(modified(modified,"GROUP_*"))
					CMSecurity.parseGroups(ipage);
				if(modified(modified,"START")||(modified(modified,"START_*")))
					CMLib.login().initStartRooms(ipage);
				if(modified(modified,"DEATH")||(modified(modified,"DEATH_*")))
					CMLib.login().initDeathRooms(ipage);
				if(modified(modified,"MORGUE")||(modified(modified,"MORGUE_*")))
					CMLib.login().initBodyRooms(ipage);
                if(modified(modified,"FACTIONS"))
                    CMLib.factions().reloadFactions(CMProps.getVar(CMProps.SYSTEM_PREFACTIONS));
				if(modified(modified,"CHANNELS")
				||(modified(modified,"ICHANNELS"))
                ||(modified(modified,"COMMANDJOURNALS"))
                ||(modified(modified,"FORUMJOURNALS"))
				||(modified(modified,"IMC2CHANNELS")))
				{
					CMLib.channels().loadChannels(ipage.getStr("CHANNELS"),ipage.getStr("ICHANNELS"),ipage.getStr("IMC2CHANNELS"));
                    CMLib.journals().loadCommandJournals(ipage.getStr("COMMANDJOURNALS"));
                    CMLib.journals().loadForumJournals(ipage.getStr("FORUMJOURNALS"));
				}
                CMLib.time().globalClock().initializeINIClock(ipage);
			}
			return "";
		}
		return "";
	}
}
