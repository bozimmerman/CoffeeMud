package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.web.macros.grinder.*;
import com.planet_ink.coffee_mud.utils.*;


public class MUDGrinder extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		if(parms!=null)
		if(parms.containsKey("AREALIST"))
		{
			MOB mob=Authenticate.getAuthenticatedMOB(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Area pickedA=getLoggedArea(httpReq,mob);
			StringBuffer AreaList=new StringBuffer("");
			for(int a=0;a<CMMap.numAreas();a++)
			{
				Area A=(Area)CMMap.getArea(a);
				if((A.amISubOp(mob.name()))||(mob.isASysOp(null)))
					if((pickedA!=null)&&(pickedA==A))
						AreaList.append("<OPTION SELECTED VALUE=\""+A.name()+"\">"+A.name());
					else
						AreaList.append("<OPTION VALUE=\""+A.name()+"\">"+A.name());
			}
			return AreaList.toString();
		}
		else
		if(parms.containsKey("DELAREA"))
		{
			MOB mob=Authenticate.getAuthenticatedMOB(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Area A=getLoggedArea(httpReq,mob);
			if(A==null) return "@break@";
			ExternalPlay.obliterateArea(A.name());
			return "The area "+A.name()+" has been successfully deleted.";
		}
		else
		if(parms.containsKey("ADDAREA"))
		{
			String AREA=(String)httpReq.getRequestParameters().get("AREA");
			if(AREA==null) return "false";
			if(AREA.length()==0) return "false";
			Area A=CMMap.getArea(AREA);
			if(A==null)
				A=ExternalPlay.DBCreateArea(AREA,"StdArea");
			else
				return "false";
			return "true";
		}
		else
		if(parms.containsKey("AREAMAP"))
		{
			
			String AREA=(String)httpReq.getRequestParameters().get("AREA");
			if(AREA==null) return "";
			if(AREA.length()==0) return "";
			Area A=CMMap.getArea(AREA);
			if(A==null) return "";
			GrinderMap map=new GrinderMap(A);
			map.rePlaceRooms();
			return map.getHTMLTable(httpReq).toString();
		}
		else
		if(parms.containsKey("EDITAREA"))
		{
			String AREA=(String)httpReq.getRequestParameters().get("AREA");
			if(AREA==null) return "";
			if(AREA.length()==0) return "";
			Area A=CMMap.getArea(AREA);
			if(A==null) return "";
			String error=GrinderArea.modifyArea(httpReq,parms);
			AREA=(String)httpReq.getRequestParameters().get("AREA");
			if((error!=null)&&(error.length()>0))
				httpReq.getRequestParameters().put("ERRMSG",error);
			A=CMMap.getArea(AREA);
			if(A==null) return "";
			ExternalPlay.DBUpdateArea(A);
		}
		return "";
	}
	
	private Area getLoggedArea(ExternalHTTPRequests httpReq, MOB mob)
	{
		String AREA=(String)httpReq.getRequestParameters().get("AREA");
		if(AREA==null) return null;
		if(AREA.length()==0) return null;
		Area A=CMMap.getArea(AREA);
		if(A==null) return null;
		if(mob.isASysOp(null)||(A.amISubOp(mob.name())))
			return A;
		return null;
	}
}
