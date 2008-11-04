package com.planet_ink.coffee_mud.WebMacros;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.planet_ink.coffee_mud.Common.interfaces.CharStats;
import com.planet_ink.coffee_mud.Common.interfaces.Clan;
import com.planet_ink.coffee_mud.Common.interfaces.Faction;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExternalHTTPRequests;
import com.planet_ink.coffee_mud.MOBS.interfaces.Deity;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.Races.interfaces.Race;
import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMParms;
import com.planet_ink.coffee_mud.core.CMProps;
import com.planet_ink.coffee_mud.core.CMStrings;
import com.planet_ink.coffee_mud.core.CMath;

@SuppressWarnings("unchecked")
public class ThinPlayerData extends StdWebMacro {
	
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	
	public static Vector getSortedThinPlayerData(ExternalHTTPRequests httpReq,  String sort) {
		Vector V=(Vector)httpReq.getRequestObjects().get("PLAYERLISTVECTOR"+sort);
		if(V==null)
		{
			V=CMLib.database().getExtendedUserList();
			int code=DatabaseEngine.ThinPlayer.getSortCode(sort);
			if((sort.length()>0)
			&&(code>=0)
			&&(V.size()>1))
			{
				Vector unV=V;
				V=new Vector();
				while(unV.size()>0)
				{
					DatabaseEngine.ThinPlayer M=(DatabaseEngine.ThinPlayer)unV.firstElement();
					String loweStr=M.getSortValue(code);
					DatabaseEngine.ThinPlayer lowestM=M;
					for(int i=1;i<unV.size();i++)
					{
						M=(DatabaseEngine.ThinPlayer)unV.elementAt(i);
						String val=M.getSortValue(code);
						if((CMath.isNumber(val)&&CMath.isNumber(loweStr)))
						{
							if(CMath.s_long(val)<CMath.s_long(loweStr))
							{
								loweStr=val;
								lowestM=M;
							}
						}
						else
						if(val.compareTo(loweStr)<0)
						{
							loweStr=val;
							lowestM=M;
						}
					}
					unV.removeElement(lowestM);
					V.addElement(lowestM);
				}
			}
			httpReq.getRequestObjects().put("PLAYERLISTVECTOR"+sort,V);
		}
		return V;
	}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return CMProps.getVar(CMProps.SYSTEM_MUDSTATUS);

		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("PLAYER");
		if(last==null) return " @break@";
		StringBuffer str=new StringBuffer("");
		if(last.length()>0)
		{
			String sort=httpReq.getRequestParameter("SORTBY");
			if(sort==null) sort="";
			Vector V=ThinPlayerData.getSortedThinPlayerData(httpReq,sort);
			DatabaseEngine.ThinPlayer player = null;
			for(int v=0;v<V.size();v++)
				if(((DatabaseEngine.ThinPlayer)V.elementAt(v)).name.equalsIgnoreCase(last))
				{
					player = (DatabaseEngine.ThinPlayer)V.elementAt(v); 
					break;
				}
			if(player == null) return " @break@";
			for(Enumeration e=parms.keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				int x=DatabaseEngine.ThinPlayer.getSortCode(key.toUpperCase().trim());
				if(x>=0)
				{
					String value = player.getSortValue(x);
					if(DatabaseEngine.ThinPlayer.SORTCODES[x].equals("LAST"))
						value=CMLib.time().date2String(CMath.s_long(value));
					str.append(value+", ");
				}
			}
		}
		String strstr=str.toString();
		if(strstr.endsWith(", "))
			strstr=strstr.substring(0,strstr.length()-2);
        return clearWebMacros(strstr);
	}

}
