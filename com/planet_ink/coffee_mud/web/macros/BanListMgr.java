package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class BanListMgr extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("BANNEDONE");
		if(parms.containsKey("RESET"))
		{
			if(last!=null) httpReq.removeRequestParameter("BANNEDONE");
			return "";
		}
		else
		if(parms.containsKey("NEXT"))
		{
			String lastID="";
			Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
			for(int i=0;i<banned.size();i++)
			{
				String key=(String)banned.elementAt(i);
				if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!key.equals(lastID))))
				{
					httpReq.addRequestParameters("BANNEDONE",key);
					return "";
				}
				lastID=key;
			}
			httpReq.addRequestParameters("BANNEDONE","");
			if(parms.containsKey("EMPTYOK"))
				return "<!--EMPTY-->";
			else
				return " @break@";
		}
		else
		if(parms.containsKey("DELETE"))
		{
			StringBuffer newBanned=new StringBuffer("");
			Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
			String key=httpReq.getRequestParameter("BANNEDONE");
			if((key!=null)&&(key.length()>0)&&(banned!=null)&&(banned.size()>0))
			{
				for(int b=0;b<banned.size();b++)
				{
					String B=(String)banned.elementAt(b);
					if(!B.equals(key))
						newBanned.append(B+"\n\r");
				}
				Resources.updateResource("banned.ini",newBanned);
				Resources.saveFileResource("banned.ini");
				return "'"+key+"' no longer banned.";
			}
			return "<!--EMPTY-->";
		}
		else
		if(parms.containsKey("ADD"))
		{
			StringBuffer newBanned=Resources.getFileResource("banned.ini",false);
			if(newBanned==null) newBanned=new StringBuffer("");
			Vector banned=Resources.getFileLineVector(newBanned);
			if(banned==null) banned=new Vector();
			String key=httpReq.getRequestParameter("NEWBANNEDONE");
			if((key!=null)&&(key.length()>0))
			{
				boolean found=false;
				for(int b=0;b<banned.size();b++)
				{
					String B=(String)banned.elementAt(b);
					if(B.equalsIgnoreCase(key)){ found=true; break;}
				}
				if(!found)
				{
					newBanned.append(key+"\n\r");
					Resources.updateResource("banned.ini",newBanned);
					Resources.saveFileResource("banned.ini");
				}
				return "'"+key+"' is now banned.";
			}
			return "<!--EMPTY-->";
		}
		else
		if(last!=null)
			return last;
		return "<!--EMPTY-->";
	}
	
}
