package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class PlayerNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		if(!httpReq.getMUD().gameStatusStr().equalsIgnoreCase("OK"))
			return httpReq.getMUD().gameStatusStr();

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
					MOB M=Authenticate.getMOB((String)unV.firstElement());
					if(M==null) return " @break@";
					String loweStr=PlayerData.getBasic(M,code);
					MOB lowestM=M;
					for(int i=1;i<unV.size();i++)
					{
						M=Authenticate.getMOB((String)unV.elementAt(i));
						if(M==null) return " @break@";
						String val=PlayerData.getBasic(M,code);
						if((Util.isNumber(val)&&Util.isNumber(loweStr)))
						{
							if(Util.s_int(val)<Util.s_int(loweStr))
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