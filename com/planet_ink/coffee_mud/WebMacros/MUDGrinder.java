package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.WebMacros.grinder.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.Command;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;



/* 
   Copyright 2000-2008 Bo Zimmerman

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
public class MUDGrinder extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return CMProps.getVar(CMProps.SYSTEM_MUDSTATUS);

		if(parms!=null)
		if(parms.containsKey("AREAMAP"))
		{
			String AREA=httpReq.getRequestParameter("AREA");
			if(AREA==null) return "";
			if(AREA.length()==0) return "";
			Area A=CMLib.map().getArea(AREA);
			if(A==null) return "";
			if((A.properSize()==0)&&(A.getRandomProperRoom()==null))
				GrinderRooms.createLonelyRoom(A,null,0,false);
			GrinderFlatMap map=null;
			int[] xyxy=this.getAppropriateXY(A,httpReq.getRequestParameter("MAPSTYLE"));
			if((httpReq.getRequestParameter("MAPSTYLE")!=null)
			&&(httpReq.getRequestParameter("MAPSTYLE").length()>0)
			&&(xyxy==null))
				map=new GrinderMap(A,null);
			else
				map=new GrinderFlatMap(A,xyxy);
			map.rePlaceRooms();
			return map.getHTMLTable(httpReq).toString();
		}
        else
        if(parms.containsKey("AREATHUMBNAIL"))
        {
			try
			{
				String AREA = httpReq.getRequestParameter("AREA");
				if (AREA == null) return "";
				if (AREA.length() == 0) return "";
				Area A = CMLib.map().getArea(AREA);
				if (A == null)  return "";
				if((A.properSize()==0)&&(A.getRandomProperRoom()==null))
					GrinderRooms.createLonelyRoom(A, null, 0, false);
				GrinderFlatMap map=null;
				if((httpReq.getRequestParameter("MAPSTYLE")!=null)
				&&(httpReq.getRequestParameter("MAPSTYLE").length()>0)
				&&(!(A instanceof GridZones)))
					map=new GrinderMap(A,null);
				else
					map=new GrinderFlatMap(A,null);
				map.rePlaceRooms();
				String rS = httpReq.getRequestParameter("ROOMSIZE");
				int roomSize = (rS!=null)?CMath.s_int(rS):4;
				if (roomSize <= 4)
					return map.getHTMLMap(httpReq).toString();
				return map.getHTMLMap(httpReq, roomSize).toString();
			}
			catch(Exception e){e.printStackTrace();}
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
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Area pickedA=getLoggedArea(httpReq,mob);
			return GrinderAreas.getAreaList(pickedA,mob);
		}
		else
		if(parms.containsKey("DELAREA"))
		{
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Area A=getLoggedArea(httpReq,mob);
			if(A==null) return "@break@";
			CMLib.map().obliterateArea(A.Name());
			Log.sysOut("Grinder",mob.Name()+" obliterated area "+A.Name());
			return "The area "+A.Name()+" has been successfully deleted.";
		}
        else
        if(parms.containsKey("DELCLAN"))
        {
            MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
            if(mob==null) return "@break@";
            String last=httpReq.getRequestParameter("CLAN");
            if(last==null) return "@break@";
            if(last.length()==0) return "@break@";
            Clan C=CMLib.clans().getClan(last);
            if(C==null) return "@break@";
            if(!CMSecurity.isAllowedEverywhere(mob,"CMDCLANS")) return "@break@";
            C.destroyClan();
            Log.sysOut("Grinder",mob.Name()+" destroyed clan "+C.clanID());
            return "The clan "+C.clanID()+" has been successfully destroyed.";
        }
        else
        if(parms.containsKey("EDITCLAN"))
        {
            MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
            if(mob==null) return "@break@";
            String last=httpReq.getRequestParameter("CLAN");
            if(last==null) return "@break@";
            if(last.length()==0) return "@break@";
            Clan C=CMLib.clans().getClan(last);
            if(C==null) return "@break@";
            if(!CMSecurity.isAllowedEverywhere(mob,"CMDCLANS")) return "@break@";
            String err=new GrinderClans().runMacro(httpReq,parm);
            if(err.length()>0) return err;
            C.update();
            Log.sysOut("Grinder",mob.Name()+" modified clan "+C.clanID());
            return "The clan "+C.clanID()+" has been successfully modified.";
        }
		else
        if(parms.containsKey("ADDCLAN"))
        {
            MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
            if(mob==null) return "@break@";
            String last=httpReq.getRequestParameter("NEWCLANID");
            if(last==null) return "@break@";
            if(last.length()==0) return "@break@";
            Clan C=CMLib.clans().findClan(last);
            if((C!=null) 
            ||(CMLib.database().DBUserSearch(null,last))
            ||(last.equalsIgnoreCase("All")))
                return "@break@";
            if(!CMSecurity.isAllowedEverywhere(mob,"CMDCLANS")) return "@break@";
            Clan newClan=CMLib.clans().getClanType(Clan.TYPE_CLAN);
            newClan.setName(last);
            newClan.setGovernment(Clan.GVT_DICTATORSHIP);
            newClan.setStatus(Clan.CLANSTATUS_PENDING);
            newClan.create();
            Log.sysOut("Grinder",mob.Name()+" created clan "+newClan.clanID());
            httpReq.addRequestParameters("CLAN",last);
            return "The clan "+newClan.clanID()+" has been successfully created.";
        }
        else
		if(parms.containsKey("IMPORTAREA"))
		{
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
            String file=httpReq.getRequestParameter("FILE");
            if(file==null) file="";
            byte[] bufBytes=(byte[])httpReq.getRequestObjects().get("FILE");
            if((file.length()==0)||(bufBytes==null)||(bufBytes.length==0))
            	return "That file was empty.";
            boolean deleteIfExists=false;
            if(httpReq.getRequestParameter("DELFIRST")!=null)
            	deleteIfExists=httpReq.getRequestParameter("DELFIRST").equalsIgnoreCase("ON");
            StringBuffer buf=new StringBuffer(CMStrings.bytesToStr(bufBytes));
    		Vector V=CMParms.parse("IMPORT "+(deleteIfExists?"":"NODELETE ")+"NOPROMPT");
    		V.addElement(buf);
    		Command C=CMClass.getCommand("Import");
    		if(C==null) return null;
    		try{C.execute(mob,V);}catch(Exception e){return e.getMessage();}
    		if((V.size()==0)||(V.lastElement() instanceof StringBuffer))
    			return "<FONT COLOR=LIGHTGREEN>Your file was successfully imported.</FONT>";
    		StringBuffer error=new StringBuffer("");
    		for(int i=0;i<V.size();i++)
    			if(V.elementAt(i) instanceof String)
    				error.append(((String)V.elementAt(i))+"<BR>");
    		return error.toString();
		}
		else
		if(parms.containsKey("ADDAREA"))
		{
            MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			String AREA=httpReq.getRequestParameter("AREA");
			if(AREA==null) return "false";
			if(AREA.length()==0) return "false";
			Area A=CMLib.map().getArea(AREA);
			if(A==null)
			{
				String areaClass=httpReq.getRequestParameter("AREATYPE");
				A=CMLib.database().DBCreateArea(AREA,areaClass);
				if(A==null) return "false";
				A.setName(AREA);
		        CMLib.coffeeMaker().addAutoPropsToAreaIfNecessary(A);			}
			else
				return "false";
			Log.sysOut("Grinder",mob.Name()+" added area "+A.Name());
			return "true";
		}
		else
		if(parms.containsKey("EDITAREA"))
		{
            MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			String AREA=httpReq.getRequestParameter("AREA");
			if(AREA==null) return "";
			if(AREA.length()==0) return "";
			Area A=CMLib.map().getArea(AREA);
			if(A==null) return "";
			GrinderAreas.modifyArea(httpReq,parms);
			AREA=httpReq.getRequestParameter("AREA");
			Log.sysOut("Grinder",mob.Name()+" edited area "+A.Name());
		}
		else
		if(parms.containsKey("DELEXIT"))
		{
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMLib.map().getRoom(httpReq.getRequestParameter("ROOM"));
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
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMLib.map().getRoom(httpReq.getRequestParameter("ROOM"));
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
			Area A=CMLib.map().getArea(AREA);
			if(A==null) return "";
			String roomID=quickfind(A,find.toUpperCase());
			if(roomID!=null)
				httpReq.addRequestParameters("ROOM",roomID);
			return "";
		}
		else
		if(parms.containsKey("LINKEXIT"))
		{
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMLib.map().getRoom(httpReq.getRequestParameter("ROOM"));
			if(R==null) return "@break@";
			int dir=Directions.getGoodDirectionCode(httpReq.getRequestParameter("LINK"));
			if(dir<0) return "@break@";
			Room R2=CMLib.map().getRoom(httpReq.getRequestParameter("OLDROOM"));
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
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMLib.map().getRoom(httpReq.getRequestParameter("ROOM"));
			if(R==null) return "@break@";
			int dir=Directions.getGoodDirectionCode(httpReq.getRequestParameter("LINK"));
			if(dir<0) return "@break@";
			String oldroom=httpReq.getRequestParameter("OLDROOM");
			if(oldroom==null) oldroom="";
			Room R2=CMLib.map().getRoom(oldroom);
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
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMLib.map().getRoom(httpReq.getRequestParameter("ROOM"));
			if(R==null) return "@break@";
			String errMsg=GrinderRooms.editRoom(httpReq,parms,mob,R);
			httpReq.addRequestParameters("ERRMSG",errMsg);
		}
        else
        if(parms.contains("DELHOLIDAY"))
        {
            MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
            if(mob==null) return "@break@";
            String last=httpReq.getRequestParameter("HOLIDAY");
            if(last==null) return " @break@";
            int dex=CMLib.quests().getHolidayIndex(last);
            if(dex<0) return "Holiday '" + last +"' does not exist.";
            String err=CMLib.quests().deleteHoliday(dex);
            Log.sysOut("Grinder",mob.name()+" deleted holiday "+last);
            return (err.length()>0)?err:"Holiday '"+last+"' deleted.";
        }
        else
        if(parms.contains("EDITHOLIDAY"))
        {
            MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
            if(mob==null) return "@break@";
            String last=httpReq.getRequestParameter("HOLIDAY");
            if(last==null) return " @break@";
            int holidayIndex=CMLib.quests().getHolidayIndex(last);
            String err=GrinderHolidays.createModifyHoliday(httpReq, parms, last);
            if(holidayIndex<0)
                Log.sysOut("Grinder",mob.name()+" created holiday "+last);
            else
                Log.sysOut("Grinder",mob.name()+" modified holiday "+last);
            return (err.length()>0)?err:"";
        }
        else
        if(parms.contains("DELRACE"))
        {
            MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
            if(mob==null) return "@break@";
            Race R=null;
            String last=httpReq.getRequestParameter("RACE");
            if(last==null) return " @break@";
            R=CMClass.getRace(last);
            if((R==null)||(!R.isGeneric()))
                return " @break@";
            String oldRID=R.ID();
            CMClass.delRace(R);
            CMLib.database().DBDeleteRace(R.ID());
            CMClass.loadClass("RACE","com/planet_ink/coffee_mud/Races/"+oldRID+".class",true);
            Race oldR=CMClass.getRace(oldRID);
            if(oldR==null) oldR=CMClass.getRace("StdRace");
            CMLib.utensils().swapRaces(oldR, R);
            Log.sysOut("Grinder",mob.name()+" deleted race "+R.ID());
            return "Race "+R.ID()+" deleted.";
        }
		else
		if(parms.contains("EDITRACE"))
		{
            MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
            if(mob==null) return "@break@";
            Race R=null;
            Race oldR=null;
	        String last=httpReq.getRequestParameter("RACE");
	        if(last==null) return " @break@";
            R=CMClass.getRace(last);
            boolean create=false;
            if((R==null)||(!R.isGeneric())) {
                create=true;
                if(R!=null) oldR=R;
                R=(Race)CMClass.getRace("GenRace").copyOf();
                R.setRacialParms("<RACE><ID>"+last+"</ID><NAME>"+last+"</NAME></RACE>");
            }
	        if(R==null) return " @break@";
	        R=R.makeGenRace();
	        String errMsg=GrinderRaces.modifyRace(httpReq, parms, R);
            httpReq.addRequestParameters("ERRMSG",errMsg);
            if(!create)
            {
                CMLib.database().DBDeleteRace(R.ID());
                CMLib.database().DBCreateRace(R.ID(),R.racialParms());
                if((oldR!=null)&&(oldR!=R))
                    CMLib.utensils().swapRaces(R, oldR);
                return "Race "+R.ID()+" modified.";
            }
            else
            {
                CMClass.addRace(R);
                CMLib.database().DBCreateRace(R.ID(),R.racialParms());
                if((oldR!=null)&&(oldR!=R))
                    CMLib.utensils().swapRaces(R, oldR);
                if((oldR!=null)&&(!oldR.isGeneric()))
                    return "Race "+R.ID()+" replaced with Generic Race " + R.ID()+".";
                else
                    return "Race "+R.ID()+" created.";
            }
		}
        else
        if(parms.contains("DELCLASS"))
        {
            MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
            if(mob==null) return "@break@";
            CharClass C=null;
            String last=httpReq.getRequestParameter("CLASS");
            if(last==null) return " @break@";
            C=CMClass.getCharClass(last);
            if((C==null)||(!C.isGeneric()))
                return " @break@";
            String oldCID=C.ID();
            CMClass.delCharClass(C);
            CMLib.database().DBDeleteClass(C.ID());
            CMClass.loadClass("CHARRCLASS","com/planet_ink/coffee_mud/CharClasses/"+oldCID+".class",true);
            CharClass oldC=CMClass.getCharClass(oldCID);
            if(oldC==null) oldC=CMClass.getCharClass("StdCharClass");
            if((oldC!=null)&&(oldC!=C))
                CMLib.utensils().reloadCharClasses(oldC);
            Log.sysOut("Grinder",mob.name()+" deleted charclass "+C.ID());
            return "CharClass "+C.ID()+" deleted.";
        }
        else
        if(parms.contains("EDITCLASS"))
        {
            MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
            if(mob==null) return "@break@";
            CharClass C=null;
            CharClass oldC=null;
            String last=httpReq.getRequestParameter("CLASS");
            if(last==null) return " @break@";
            C=CMClass.getCharClass(last);
            boolean create=false;
            if((C==null)||(!C.isGeneric())) {
                create=true;
                if(C!=null) oldC=C;
                C=(CharClass)CMClass.getCharClass("GenCharClass").copyOf();
                C.setClassParms("<CCLASS><ID>"+last+"</ID><NAME>"+last+"</NAME></CCLASS>");
            }
            if(C==null) return " @break@";
            C=C.makeGenCharClass();
            String errMsg=GrinderClasses.modifyCharClass(httpReq, parms, C);
            httpReq.addRequestParameters("ERRMSG",errMsg);
            if(!create)
            {
                CMLib.database().DBDeleteClass(C.ID());
                CMLib.database().DBCreateClass(C.ID(),C.classParms());
                if((oldC!=null)&&(oldC!=C))
                    CMLib.utensils().reloadCharClasses(oldC);
                return "Char Class "+C.ID()+" modified.";
            }
            else
            {
                CMClass.addCharClass(C);
                CMLib.database().DBCreateClass(C.ID(),C.classParms());
                if((oldC!=null)&&(oldC!=C))
                    CMLib.utensils().reloadCharClasses(oldC);
                if((oldC!=null)&&(!oldC.isGeneric()))
                    return "Char Class "+C.ID()+" replaced with Generic Class " + C.ID()+".";
                else
                    return "Char Class "+C.ID()+" created.";
            }
        }
		else
		if(parms.containsKey("EDITITEM"))
		{
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
            String player=httpReq.getRequestParameter("PLAYER");
            MOB playerM=(player!=null)?CMLib.map().getLoadPlayer(player):null;
            String roomID=httpReq.getRequestParameter("ROOM");
			Room R=CMLib.map().getRoom(roomID);
			if((R==null)&&(playerM==null)&&((roomID==null)||(!roomID.equalsIgnoreCase("ANY")))) return "@break@";
			String errMsg=GrinderItems.editItem(httpReq,parms,mob,R,playerM);
			httpReq.addRequestParameters("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("EDITMOB"))
		{
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
            String roomID=httpReq.getRequestParameter("ROOM");
			Room R=CMLib.map().getRoom(roomID);
			if((R==null)&&((roomID==null)||(!roomID.equalsIgnoreCase("ANY")))) return "@break@";
			String errMsg=GrinderMobs.editMob(httpReq,parms,mob,R);
			httpReq.addRequestParameters("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("EDITPLAYER"))
		{
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			MOB M=CMLib.map().getLoadPlayer(httpReq.getRequestParameter("PLAYER"));
			if(M==null) return "@break@";
			String errMsg=GrinderPlayers.editPlayer(mob,httpReq,parms,M);
			httpReq.addRequestParameters("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("DELROOM"))
		{
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMLib.map().getRoom(httpReq.getRequestParameter("ROOM"));
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
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			String link=httpReq.getRequestParameter("LINK");
			Room R=CMLib.map().getRoom(httpReq.getRequestParameter("ROOM"));
			if(R==null) return "@break@";
			int dir=Directions.getGoodDirectionCode(link);
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
		else
		if(parms.containsKey("PAINTROOMS"))
		{
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			String AREA=httpReq.getRequestParameter("AREA");
			if(AREA==null) return "@break@";
			Area A=CMLib.map().getArea(AREA);
			if(A==null) return "@break@";
			String like=httpReq.getRequestParameter("ROOM");
			Vector likeList=CMParms.parseCommas(like,true);
			Vector RS=new Vector();
			for(int l=0;l<likeList.size();l++)
			{
				like=(String)likeList.elementAt(l);
				String roomID=quickfind(A,like.toUpperCase());
				Room R=null;
				if(roomID!=null) R=CMLib.map().getRoom(roomID);
				R=CMLib.map().getRoom(R);
				if(R==null) return "The room you entered ("+like+") could not be found.";
				CMLib.map().resetRoom(R);
				Room likeRoom=(Room)CMLib.map().getRoom(R).copyOf();
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++) likeRoom.rawDoors()[d]=null;
				RS.addElement(likeRoom);
			}
			if(RS.size()==0) return "You did not specify a room or room list to paint from!";
			boolean overwrite=httpReq.getRequestParameter("NOVERLAP")!=null
								&&(httpReq.getRequestParameter("NOVERLAP").length()>0);
			int brushSize=CMath.s_int(httpReq.getRequestParameter("BRUSHSIZE"));
			if(brushSize<=0) return "Your brush size should be a number greater than 0!";
			int[] coords=getAppropriateXY(A,httpReq.getRequestParameter("PAINTAT"));
			if(coords==null) return "An error occurred trying to paint.  Relogin and try again.";
			int radius=(int)Math.round(Math.floor(CMath.div(brushSize,2)));
			if(radius<1) radius=1;
			coords[4]-=radius;
			coords[5]-=radius;
			if(coords[4]<0) coords[4]=0;
			if(coords[5]<0) coords[5]=0;
			int endX=coords[4]+brushSize;
			int endY=coords[5]+brushSize;
			if(endX>=((GridZones)A).xGridSize()) endX=((GridZones)A).xGridSize()-1;
			if(endY>=((GridZones)A).yGridSize()) endY=((GridZones)A).yGridSize()-1;
			String roomID=null;
			Room R=null;
			if(overwrite)
			for(int x=coords[4];x<=endX;x++)
				for(int y=coords[5];y<=endY;y++)
				{
					roomID=gridRoomID(A,x,y);
					if(A.getProperRoomnumbers().contains(roomID))
					{
						R=A.getRoom(roomID);
						if(R!=null)
						{
							CMLib.map().obliterateRoom(R);
							Log.sysOut("Grinder",mob.Name()+" obliterated room "+R.roomID());
						}
					}
				}
			RoomnumberSet deferredExitSaves=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
			for(int x=coords[4];x<=endX;x++)
				for(int y=coords[5];y<=endY;y++)
				{
					roomID=gridRoomID(A,x,y);
					if(!A.getProperRoomnumbers().contains(roomID))
					{
						Room likeRoom=(Room)RS.elementAt(CMLib.dice().roll(1,RS.size(),0)-1);
						R=GrinderRooms.createGridRoom(A,roomID,likeRoom,deferredExitSaves,true);
						Log.sysOut("Grinder",mob.Name()+" added room "+R.roomID());
					}
				}
			for(Enumeration e=deferredExitSaves.getRoomIDs();e.hasMoreElements();)
			{
				R=CMLib.map().getRoom((String)e.nextElement());
				CMLib.database().DBUpdateExits(R);
			}
			Log.sysOut("Grinder",mob.name()+" updated "+deferredExitSaves.roomCountAllAreas()+" rooms exits.");
			//String errMsg=GrinderRooms.createRoom(R,dir,(copyThisOne!=null)&&(copyThisOne.equalsIgnoreCase("ON")));
		}
		else
		if(parms.containsKey("ADDGRIDROOM"))
		{
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			String AREA=httpReq.getRequestParameter("AREA");
			if(AREA==null) return "false";
			Area A=CMLib.map().getArea(AREA);
			if(A==null) return "@break@";
			String roomID=httpReq.getRequestParameter("ROOM");
			String link=httpReq.getRequestParameter("AUTOLINK");
			Room R=CMLib.map().getRoom(roomID);
			if((roomID==null)||(R!=null)||(roomID.length()==0)) return "@break@";
			String copyThisOne=httpReq.getRequestParameter("COPYROOM");
			Room copyRoom=CMLib.map().getRoom(copyThisOne);
			Room newRoom=GrinderRooms.createGridRoom(A,roomID,copyRoom,null,((link!=null)&&(link.length()>0)));
			if(newRoom==null) httpReq.addRequestParameters("ERRMSG","An error occurred trying to create your room.");
			Log.sysOut("Grinder",mob.Name()+" added room "+newRoom.roomID());
			httpReq.addRequestParameters("ROOM",newRoom.roomID());
			httpReq.addRequestParameters("LINK","");
		}
		return "";
	}

	protected String gridRoomID(Area A, int x, int y)
	{
		String roomID=A.Name()+"#";
		String xy=""+y;
		if(x>0)
		{ 
			while(xy.length()<(((GridZones)A).yGridSize()+"").length())
				xy="0"+xy;
			xy=x+xy;
		}
		return roomID+xy;
	}
	
	protected int[] getAppropriateXY(Area A, String mapStyle)
	{
		if((mapStyle==null)
		||(mapStyle.length()==0)
		||(!(A instanceof GridZones)))
			return null;
		if(mapStyle.startsWith("G"))
		{
			mapStyle=mapStyle.substring(1);
			int[] xyxy=new int[6];
			int x=mapStyle.indexOf("_");
			int index=0;
			while(x>=0)
			{
				String coord=mapStyle.substring(0,x);
				mapStyle=mapStyle.substring(x+1);
				if((index<xyxy.length)&&(CMath.isInteger(coord)))
					xyxy[index++]=CMath.s_int(coord);
				else
					break;
				x=mapStyle.indexOf("_");
			}
			if((index<xyxy.length)&&(CMath.isInteger(mapStyle)))
				xyxy[index++]=CMath.s_int(mapStyle);
			return xyxy;
		}
		return null;
	}

    protected Area getLoggedArea(ExternalHTTPRequests httpReq, MOB mob)
	{
		String AREA=httpReq.getRequestParameter("AREA");
		if(AREA==null) return null;
		if(AREA.length()==0) return null;
		Area A=CMLib.map().getArea(AREA);
		if(A==null) return null;
		if(CMSecurity.isASysOp(mob)||A.amISubOp(mob.Name()))
			return A;
		return null;
	}
    
    protected String quickfind(Area A, String find)
    {
    	Room R=A.getRoom(find);
    	if((R!=null)&&(R.roomID().length()>0)) return R.roomID();
    	if(find.startsWith("#"))
    	{
    		R=A.getRoom(A.Name()+find);
        	if((R!=null)&&(R.roomID().length()>0)) return R.roomID();
    	}
    	if(CMath.isNumber(find))
    	{
    		R=A.getRoom(A.Name()+"#"+find);
        	if((R!=null)&&(R.roomID().length()>0)) return R.roomID();
    	}
		for(Enumeration r=A.getProperMap();r.hasMoreElements();)
		{
			R=(Room)r.nextElement();
			if((R.roomID().length()>0)&&(R.roomID().toUpperCase().endsWith(find.toUpperCase())))
				return R.roomID();
		}
		for(Enumeration r=A.getProperMap();r.hasMoreElements();)
		{
			R=(Room)r.nextElement();
			if((R.roomID().length()>0)&&(R.displayText().toUpperCase().indexOf(find.toUpperCase())>=0))
				return R.roomID();
		}
		for(Enumeration r=A.getProperMap();r.hasMoreElements();)
		{
			R=(Room)r.nextElement();
			if((R.roomID().length()>0)&&(R.description().toUpperCase().indexOf(find.toUpperCase())>=0))
				return R.roomID();
		}
		R=CMLib.map().getRoom(find);
    	if((R!=null)&&(R.roomID().length()>0)) return R.roomID();
		return null;
    }
}
