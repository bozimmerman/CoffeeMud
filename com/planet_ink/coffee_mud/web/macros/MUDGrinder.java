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
			String AREA=httpReq.getRequestParameter("AREA");
			if(AREA==null) return "";
			if(AREA.length()==0) return "";
			Area A=CMMap.getArea(AREA);
			if(A==null) return "";
			if(A.mapSize()==0)
			{
				GrinderRooms.createLonelyRoom(A,null,0,false);
				A.clearMap();
			}
			GrinderFlatMap map=null;
			if((httpReq.getRequestParameter("MAPSTYLE")!=null)
			&&(httpReq.getRequestParameter("MAPSTYLE").length()>0))
				map=new GrinderMap(A);
			else
				map=new GrinderFlatMap(A);
			map.rePlaceRooms();
			return map.getHTMLTable(httpReq).toString();
		}
        else
        if(parms.containsKey("AREATHUMBNAIL"))
        {
			String AREA = httpReq.getRequestParameter("AREA");
			if (AREA == null) return "";
			if (AREA.length() == 0) return "";
			Area A = CMMap.getArea(AREA);
			if (A == null)  return "";
			if (A.mapSize() == 0) 
			{
				GrinderRooms.createLonelyRoom(A, null, 0, false);
				A.clearMap();
			}
			GrinderFlatMap map=null;
			if((httpReq.getRequestParameter("MAPSTYLE")!=null)
			&&(httpReq.getRequestParameter("MAPSTYLE").length()>0))
				map=new GrinderMap(A);
			else
				map=new GrinderFlatMap(A);
			map.rePlaceRooms();
			String rS = httpReq.getRequestParameter("ROOMSIZE");
			int roomSize = (rS!=null)?Util.s_int(rS):4;
			if (roomSize <= 4)
				return map.getHTMLMap(httpReq).toString();
			else
				return map.getHTMLMap(httpReq, roomSize).toString();
        }
        else
        if(parms.containsKey("WORLDMAP"))
        {
			GrinderFlatMap map=null;
			if((httpReq.getRequestParameter("MAPSTYLE")!=null)
			&&(httpReq.getRequestParameter("MAPSTYLE").length()>0))
				map=new GrinderMap();
			else
				map=new GrinderFlatMap();
			map.rePlaceRooms();
			return map.getHTMLMap(httpReq).toString();
        }
		else
		if(parms.containsKey("AREALIST"))
		{
			MOB mob=Authenticate.getMOB(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Area pickedA=getLoggedArea(httpReq,mob);
			return GrinderAreas.getAreaList(pickedA,mob);
		}
		else
		if(parms.containsKey("DELAREA"))
		{
			MOB mob=Authenticate.getMOB(Authenticate.getLogin(httpReq));
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
			String AREA=httpReq.getRequestParameter("AREA");
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
			String AREA=httpReq.getRequestParameter("AREA");
			if(AREA==null) return "";
			if(AREA.length()==0) return "";
			Area A=CMMap.getArea(AREA);
			if(A==null) return "";
			String error=GrinderAreas.modifyArea(httpReq,parms);
			AREA=httpReq.getRequestParameter("AREA");
			Log.sysOut("Grinder","Someone edited area "+A.Name());
			if(error==null) error="";
			httpReq.addRequestParameters("ERRMSG",error);
		}
		else
		if(parms.containsKey("DELEXIT"))
		{
			MOB mob=Authenticate.getMOB(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMMap.getRoom(httpReq.getRequestParameter("ROOM"));
			if(R==null) return "@break@";
			int dir=Directions.getGoodDirectionCode(httpReq.getRequestParameter("LINK"));
			if(dir<0) return "@break@";
			Log.sysOut("Grinder",mob.Name()+" deleted exit "+dir+" from "+R.roomID());
			String errMsg=GrinderExits.delExit(R,dir);
			httpReq.addRequestParameters("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("EDITEXIT"))
		{
			MOB mob=Authenticate.getMOB(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMMap.getRoom(httpReq.getRequestParameter("ROOM"));
			if(R==null) return "@break@";
			int dir=Directions.getGoodDirectionCode(httpReq.getRequestParameter("LINK"));
			if(dir<0) return "@break@";
			Log.sysOut("Grinder",mob.Name()+" modified exit "+dir+" from "+R.roomID());
			String errMsg=GrinderExits.editExit(R,dir,httpReq,parms);
			httpReq.addRequestParameters("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("QUICKFIND"))
		{
			String find=httpReq.getRequestParameter("QUICKFIND");
			if(find==null) return "@break@";
			String AREA=httpReq.getRequestParameter("AREA");
			if(AREA==null) return "";
			if(AREA.length()==0) return "";
			Area A=CMMap.getArea(AREA);
			if(A==null) return "";
			for(Enumeration r=A.getMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.roomID().toUpperCase().endsWith(find.toUpperCase()))
				{
					httpReq.addRequestParameters("ROOM",R.roomID());
					return "";
				}
			}
			for(Enumeration r=A.getMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.displayText().toUpperCase().indexOf(find.toUpperCase())>=0)
				{
					httpReq.addRequestParameters("ROOM",R.roomID());
					return "";
				}
			}
			for(Enumeration r=A.getMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.description().toUpperCase().indexOf(find.toUpperCase())>=0)
				{
					httpReq.addRequestParameters("ROOM",R.roomID());
					return "";
				}
			}
			return "";
		}
		else
		if(parms.containsKey("LINKEXIT"))
		{
			MOB mob=Authenticate.getMOB(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMMap.getRoom(httpReq.getRequestParameter("ROOM"));
			if(R==null) return "@break@";
			int dir=Directions.getGoodDirectionCode(httpReq.getRequestParameter("LINK"));
			if(dir<0) return "@break@";
			Room R2=CMMap.getRoom(httpReq.getRequestParameter("OLDROOM"));
			if(R2==null) return "@break@";
			int dir2=Directions.getGoodDirectionCode(httpReq.getRequestParameter("OLDLINK"));
			if(dir2<0) return "@break@";
			Log.sysOut("Grinder",mob.Name()+" linked exit "+dir+" from "+R.roomID());
			String errMsg=GrinderExits.linkRooms(R,R2,dir,dir2);
			httpReq.addRequestParameters("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("LINKAREA"))
		{
			MOB mob=Authenticate.getMOB(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMMap.getRoom(httpReq.getRequestParameter("ROOM"));
			if(R==null) return "@break@";
			int dir=Directions.getGoodDirectionCode(httpReq.getRequestParameter("LINK"));
			if(dir<0) return "@break@";
			String oldroom=httpReq.getRequestParameter("OLDROOM");
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
			httpReq.addRequestParameters("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("EDITROOM"))
		{
			MOB mob=Authenticate.getMOB(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMMap.getRoom(httpReq.getRequestParameter("ROOM"));
			if(R==null) return "@break@";
			String errMsg=GrinderRooms.editRoom(httpReq,parms,R);
			Log.sysOut("Grinder",mob.Name()+" modified room "+R.roomID());
			httpReq.addRequestParameters("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("EDITITEM"))
		{
			MOB mob=Authenticate.getMOB(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMMap.getRoom(httpReq.getRequestParameter("ROOM"));
			if(R==null) return "@break@";
			String errMsg=GrinderItems.editItem(httpReq,parms,R);
			Log.sysOut("Grinder",mob.Name()+" modified item in room "+R.roomID());
			httpReq.addRequestParameters("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("EDITMOB"))
		{
			MOB mob=Authenticate.getMOB(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMMap.getRoom(httpReq.getRequestParameter("ROOM"));
			if(R==null) return "@break@";
			String errMsg=GrinderMobs.editMob(httpReq,parms,R);
			Log.sysOut("Grinder",mob.Name()+" modified mob in room "+R.roomID());
			httpReq.addRequestParameters("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("DELROOM"))
		{
			MOB mob=Authenticate.getMOB(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMMap.getRoom(httpReq.getRequestParameter("ROOM"));
			if(R==null) return "@break@";
			for(int d=0;d<R.rawDoors().length;d++)
				if(R.rawDoors()[d]!=null)
				{
					httpReq.addRequestParameters("ROOM",R.rawDoors()[d].roomID());
					httpReq.addRequestParameters("LINK","");
					break;
				}
			Log.sysOut("Grinder",mob.Name()+" deleted room "+R.roomID());
			String errMsg=GrinderRooms.delRoom(R);
			httpReq.addRequestParameters("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("ADDROOM"))
		{
			MOB mob=Authenticate.getMOB(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMMap.getRoom(httpReq.getRequestParameter("ROOM"));
			if(R==null) return "@break@";
			int dir=Directions.getGoodDirectionCode(httpReq.getRequestParameter("LINK"));
			if(dir<0) return "@break@";
			String copyThisOne=httpReq.getRequestParameter("COPYROOM");
			String errMsg=GrinderRooms.createRoom(R,dir,(copyThisOne!=null)&&(copyThisOne.equalsIgnoreCase("ON")));
			httpReq.addRequestParameters("ERRMSG",errMsg);
			R=R.rawDoors()[dir];
			if(R!=null)
			{
				httpReq.addRequestParameters("ROOM",R.roomID());
				Log.sysOut("Grinder",mob.Name()+" added room "+R.roomID());
			}
			httpReq.addRequestParameters("LINK","");
		}
		return "";
	}

	private Area getLoggedArea(ExternalHTTPRequests httpReq, MOB mob)
	{
		String AREA=httpReq.getRequestParameter("AREA");
		if(AREA==null) return null;
		if(AREA.length()==0) return null;
		Area A=CMMap.getArea(AREA);
		if(A==null) return null;
		if(mob.isASysOp(null)||A.amISubOp(mob.Name()))
			return A;
		return null;
	}
}
