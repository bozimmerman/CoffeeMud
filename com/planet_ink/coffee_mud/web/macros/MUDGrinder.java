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
			StringBuffer buf=new StringBuffer("");
			GrinderMap map=new GrinderMap(A);
			map.rebuildGrid();
			buf.append("<TABLE WIDTH="+(map.Xbound*20)+" BORDER=0 CELLSPACING=0 CELLPADDING=0>");
			for(int y=0;y<=map.Ybound;y++)
			{
				for(int l=0;l<4;l++)
				{
					buf.append("<TR>");
					for(int x=0;x<=map.Xbound;x++)
					{
						GrinderRoom GR=map.grid[x][y];
						if(GR==null)
							buf.append("<TD COLSPAN=4 WIDTH=20><BR></TD>");
						else
						switch(l)
						{
						case 0:
							buf.append("<TD WIDTH=5><BR></TD>");
							buf.append("<TD WIDTH=5 BGCOLOR=RED>U1</TD>");
							buf.append("<TD WIDTH=5 BGCOLOR=RED>U2</TD>");
							buf.append("<TD WIDTH=5><BR></TD>");
							break;
						case 1:
							buf.append("<TD WIDTH=5 BGCOLOR=GREEN>L1</TD>");
							buf.append("<TD WIDTH=5 COLSPAN=2 BGCOLOR=YELLOW ALIGN=CENTER>"+GR.roomID+"</TD>");
							buf.append("<TD WIDTH=5 BGCOLOR=BLUE>R1</TD>");
							break;
						case 2:
							buf.append("<TD WIDTH=5 BGCOLOR=GREEN>L2</TD>");
							buf.append("<TD WIDTH=5 COLSPAN=2 BGCOLOR=YELLOW ALIGN=CENTER>*</TD>");
							buf.append("<TD WIDTH=5 BGCOLOR=BLUE>R2</TD>");
							break;
						case 3:
							buf.append("<TD WIDTH=5><BR></TD>");
							buf.append("<TD WIDTH=5 BGCOLOR=BLACK>D1</TD>");
							buf.append("<TD WIDTH=5 BGCOLOR=BLACK>D2</TD>");
							buf.append("<TD WIDTH=5><BR></TD>");
							break;
						}
					}
					buf.append("</TR>");
				}
			}
			buf.append("</TABLE>");
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
