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
		if(!httpReq.getMUD().gameStatusStr().equalsIgnoreCase("OK"))
			return httpReq.getMUD().gameStatusStr();

		if(parms!=null)
		if(parms.containsKey("AREAMAP"))
		{
			String AREA=(String)httpReq.getRequestParameters().get("AREA");
			if(AREA==null) return "";
			if(AREA.length()==0) return "";
			Area A=CMMap.getArea(AREA);
			if(A==null) return "";
			if(A.mapSize()==0)
			{
				GrinderRooms.createLonelyRoom(A,null,0,false);
				A.clearMap();
			}
			GrinderMap map=new GrinderMap(A);
			map.rePlaceRooms();
			return map.getHTMLTable(httpReq).toString();
		}
		else
		if(parms.containsKey("AREALIST"))
		{
			MOB mob=Authenticate.getAuthenticatedMOB(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Area pickedA=getLoggedArea(httpReq,mob);
			return GrinderAreas.getAreaList(pickedA,mob);
		}
		else
		if(parms.containsKey("DELAREA"))
		{
			MOB mob=Authenticate.getAuthenticatedMOB(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Area A=getLoggedArea(httpReq,mob);
			if(A==null) return "@break@";
			ExternalPlay.obliterateArea(A.Name());
			Log.sysOut("Grinder",mob.Name()+" obliterated area "+A.Name());
			return "The area "+A.Name()+" has been successfully deleted.";
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
			Log.sysOut("Grinder","Someone added area "+A.Name());
			return "true";
		}
		else
		if(parms.containsKey("EDITAREA"))
		{
			String AREA=(String)httpReq.getRequestParameters().get("AREA");
			if(AREA==null) return "";
			if(AREA.length()==0) return "";
			Area A=CMMap.getArea(AREA);
			if(A==null) return "";
			String error=GrinderAreas.modifyArea(httpReq,parms);
			AREA=(String)httpReq.getRequestParameters().get("AREA");
			Log.sysOut("Grinder","Someone edited area "+A.Name());
			if(error==null) error="";
			httpReq.getRequestParameters().put("ERRMSG",error);
			httpReq.resetRequestEncodedParameters();
		}
		else
		if(parms.containsKey("DELEXIT"))
		{
			MOB mob=Authenticate.getAuthenticatedMOB(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMMap.getRoom((String)httpReq.getRequestParameters().get("ROOM"));
			if(R==null) return "@break@";
			int dir=Directions.getGoodDirectionCode((String)httpReq.getRequestParameters().get("LINK"));
			if(dir<0) return "@break@";
			Log.sysOut("Grinder",mob.Name()+" deleted exit "+dir+" from "+R.roomID());
			String errMsg=GrinderExits.delExit(R,dir);
			httpReq.getRequestParameters().put("ERRMSG",errMsg);
			httpReq.resetRequestEncodedParameters();
		}
		else
		if(parms.containsKey("EDITEXIT"))
		{
			MOB mob=Authenticate.getAuthenticatedMOB(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMMap.getRoom((String)httpReq.getRequestParameters().get("ROOM"));
			if(R==null) return "@break@";
			int dir=Directions.getGoodDirectionCode((String)httpReq.getRequestParameters().get("LINK"));
			if(dir<0) return "@break@";
			Log.sysOut("Grinder",mob.Name()+" modified exit "+dir+" from "+R.roomID());
			String errMsg=GrinderExits.editExit(R,dir,httpReq,parms);
			httpReq.getRequestParameters().put("ERRMSG",errMsg);
			httpReq.resetRequestEncodedParameters();
		}
		else
		if(parms.containsKey("QUICKFIND"))
		{
			String find=(String)httpReq.getRequestParameters().get("QUICKFIND");
			if(find==null) return "@break@";
			String AREA=(String)httpReq.getRequestParameters().get("AREA");
			if(AREA==null) return "";
			if(AREA.length()==0) return "";
			Area A=CMMap.getArea(AREA);
			if(A==null) return "";
			for(Enumeration r=A.getMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.roomID().toUpperCase().endsWith(find.toUpperCase()))
				{
					httpReq.getRequestParameters().put("ROOM",R.roomID());
					httpReq.resetRequestEncodedParameters();
					return "";
				}
			}
			for(Enumeration r=A.getMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.displayText().toUpperCase().indexOf(find.toUpperCase())>=0)
				{
					httpReq.getRequestParameters().put("ROOM",R.roomID());
					httpReq.resetRequestEncodedParameters();
					return "";
				}
			}
			for(Enumeration r=A.getMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.description().toUpperCase().indexOf(find.toUpperCase())>=0)
				{
					httpReq.getRequestParameters().put("ROOM",R.roomID());
					httpReq.resetRequestEncodedParameters();
					return "";
				}
			}
			return "";
		}
		else
		if(parms.containsKey("LINKEXIT"))
		{
			MOB mob=Authenticate.getAuthenticatedMOB(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMMap.getRoom((String)httpReq.getRequestParameters().get("ROOM"));
			if(R==null) return "@break@";
			int dir=Directions.getGoodDirectionCode((String)httpReq.getRequestParameters().get("LINK"));
			if(dir<0) return "@break@";
			Room R2=CMMap.getRoom((String)httpReq.getRequestParameters().get("OLDROOM"));
			if(R2==null) return "@break@";
			int dir2=Directions.getGoodDirectionCode((String)httpReq.getRequestParameters().get("OLDLINK"));
			if(dir2<0) return "@break@";
			Log.sysOut("Grinder",mob.Name()+" linked exit "+dir+" from "+R.roomID());
			String errMsg=GrinderExits.linkRooms(R,R2,dir,dir2);
			httpReq.getRequestParameters().put("ERRMSG",errMsg);
			httpReq.resetRequestEncodedParameters();
		}
		else
		if(parms.containsKey("LINKAREA"))
		{
			MOB mob=Authenticate.getAuthenticatedMOB(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMMap.getRoom((String)httpReq.getRequestParameters().get("ROOM"));
			if(R==null) return "@break@";
			int dir=Directions.getGoodDirectionCode((String)httpReq.getRequestParameters().get("LINK"));
			if(dir<0) return "@break@";
			String oldroom=(String)httpReq.getRequestParameters().get("OLDROOM");
			if(oldroom==null) oldroom="";
			Room R2=CMMap.getRoom(oldroom);
			String errMsg="";
			if(R2==null)
				errMsg="No external room with ID '"+oldroom+"' found.";
			else
			{
				errMsg=GrinderExits.linkRooms(R,R2,dir,Directions.getOpDirectionCode(dir));
				Log.sysOut("Grinder",mob.Name()+" linked area "+R.roomID()+" to "+R2.roomID());
			}
			httpReq.getRequestParameters().put("ERRMSG",errMsg);
			httpReq.resetRequestEncodedParameters();
		}
		else
		if(parms.containsKey("EDITROOM"))
		{
			MOB mob=Authenticate.getAuthenticatedMOB(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMMap.getRoom((String)httpReq.getRequestParameters().get("ROOM"));
			if(R==null) return "@break@";
			String errMsg=GrinderRooms.editRoom(httpReq,parms,R);
			Log.sysOut("Grinder",mob.Name()+" modified room "+R.roomID());
			httpReq.getRequestParameters().put("ERRMSG",errMsg);
			httpReq.resetRequestEncodedParameters();
		}
		else
		if(parms.containsKey("EDITITEM"))
		{
			MOB mob=Authenticate.getAuthenticatedMOB(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMMap.getRoom((String)httpReq.getRequestParameters().get("ROOM"));
			if(R==null) return "@break@";
			String errMsg=GrinderItems.editItem(httpReq,parms,R);
			Log.sysOut("Grinder",mob.Name()+" modified item in room "+R.roomID());
			httpReq.getRequestParameters().put("ERRMSG",errMsg);
			httpReq.resetRequestEncodedParameters();
		}
		else
		if(parms.containsKey("EDITMOB"))
		{
			MOB mob=Authenticate.getAuthenticatedMOB(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMMap.getRoom((String)httpReq.getRequestParameters().get("ROOM"));
			if(R==null) return "@break@";
			String errMsg=GrinderMobs.editMob(httpReq,parms,R);
			Log.sysOut("Grinder",mob.Name()+" modified mob in room "+R.roomID());
			httpReq.getRequestParameters().put("ERRMSG",errMsg);
			httpReq.resetRequestEncodedParameters();
		}
		else
		if(parms.containsKey("DELROOM"))
		{
			MOB mob=Authenticate.getAuthenticatedMOB(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMMap.getRoom((String)httpReq.getRequestParameters().get("ROOM"));
			if(R==null) return "@break@";
			for(int d=0;d<R.rawDoors().length;d++)
				if(R.rawDoors()[d]!=null)
				{
					httpReq.getRequestParameters().put("ROOM",R.rawDoors()[d].roomID());
					httpReq.getRequestParameters().put("LINK","");
					break;
				}
			Log.sysOut("Grinder",mob.Name()+" deleted room "+R.roomID());
			String errMsg=GrinderRooms.delRoom(R);
			httpReq.getRequestParameters().put("ERRMSG",errMsg);
			httpReq.resetRequestEncodedParameters();
		}
		else
		if(parms.containsKey("ADDROOM"))
		{
			MOB mob=Authenticate.getAuthenticatedMOB(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMMap.getRoom((String)httpReq.getRequestParameters().get("ROOM"));
			if(R==null) return "@break@";
			int dir=Directions.getGoodDirectionCode((String)httpReq.getRequestParameters().get("LINK"));
			if(dir<0) return "@break@";
			String copyThisOne=(String)httpReq.getRequestParameters().get("COPYROOM");
			String errMsg=GrinderRooms.createRoom(R,dir,(copyThisOne!=null)&&(copyThisOne.equalsIgnoreCase("ON")));
			httpReq.getRequestParameters().put("ERRMSG",errMsg);
			R=R.rawDoors()[dir];
			if(R!=null)
			{
				httpReq.getRequestParameters().put("ROOM",R.roomID());
				Log.sysOut("Grinder",mob.Name()+" added room "+R.roomID());
			}
			httpReq.getRequestParameters().put("LINK","");
			httpReq.resetRequestEncodedParameters();
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
		if(mob.isASysOp(null)||A.amISubOp(mob.Name()))
			return A;
		return null;
	}
}
