package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class PlayerNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public static MOB getMOB(String last)
	{
		MOB M=CMMap.getPlayer(last);
		if((M==null)&&(ExternalPlay.DBUserSearch(null,last)))
		{
			M=CMClass.getMOB("StdMOB");
			M.setName(last);
			ExternalPlay.DBReadMOB(M);
			ExternalPlay.DBReadFollowers(M,false);
			M.setUpdated(M.lastDateTime());
		}
		return M;
	}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("PLAYER");
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.removeRequestParameter("PLAYER");
			return "";
		}
		String lastID="";
		String sort=httpReq.getRequestParameter("SORTBY");
		if(sort==null) sort="";
		Vector V=(Vector)httpReq.getRequestObjects().get("PLAYERLISTVECTOR"+sort);
		if(V==null)
		{
			V=ExternalPlay.userList();
			int code=PlayerData.getBasicCode(sort);
			if((sort.length()>0)
			&&(code>=0)
			&&(V.size()>1))
			{
				Vector unV=V;
				V=new Vector();
				while(unV.size()>0)
				{
					MOB M=PlayerNext.getMOB((String)unV.firstElement());
					if(M==null) return " @break@";
					String loweStr=PlayerData.getBasic(M,code);
					MOB lowestM=M;
					for(int i=1;i<unV.size();i++)
					{
						M=PlayerNext.getMOB((String)unV.elementAt(i));
						if(M==null) return " @break@";
						String val=PlayerData.getBasic(M,code);
						if(val.compareTo(loweStr)<0)
						{
							loweStr=val;
							lowestM=M;
						}
					}
					unV.removeElement(lowestM.Name());
					V.addElement(lowestM.Name());
				}
			}
			httpReq.getRequestObjects().put("PLAYERLISTVECTOR"+sort,V);
		}
		
		for(int i=0;i<V.size();i++)
		{
			String user=(String)V.elementAt(i);
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!user.equals(lastID))))
			{
				httpReq.addRequestParameters("PLAYER",user);
				return "";
			}
			lastID=user;
		}
		httpReq.addRequestParameters("PLAYER","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		else
			return " @break@";
	}

}