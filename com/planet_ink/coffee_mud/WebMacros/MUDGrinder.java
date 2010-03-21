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
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			Area pickedA=getLoggedArea(httpReq,mob);
			return GrinderAreas.getAreaList(pickedA,mob);
		}
		else
		if(parms.containsKey("DELAREA"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			Area A=getLoggedArea(httpReq,mob);
			if(A==null) return "@break@";
			CMLib.map().obliterateArea(A.Name());
			Log.sysOut("Grinder",mob.Name()+" obliterated area "+A.Name());
			return "The area "+A.Name()+" has been successfully deleted.";
		}
        else
        if(parms.containsKey("EDITACCOUNT"))
        {
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
            if(mob==null) return "@break@";
            String last=httpReq.getRequestParameter("ACCOUNT");
            if(last==null) return "@break@";
            if(last.length()==0) return "@break@";
            PlayerAccount A=CMLib.players().getLoadAccount(last);
            if(A==null) return "@break@";
            if(!CMSecurity.isAllowedEverywhere(mob,"CMDPLAYERS")) return "@break@";
            String err=new GrinderAccounts().runMacro(httpReq,parm);
            if(err.length()>0) return err;
            Log.sysOut("Grinder",mob.Name()+" modified account "+A.accountName());
            return "";
        }
        else
        if(parms.containsKey("DELACCOUNT"))
        {
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
            if(mob==null) return "@break@";
            String last=httpReq.getRequestParameter("ACCOUNT");
            if(last==null) return "@break@";
            if(last.length()==0) return "@break@";
            PlayerAccount A=CMLib.players().getLoadAccount(last);
            if(A==null) return "@break@";
            if(!CMSecurity.isAllowedEverywhere(mob,"CMDPLAYERS")) return "@break@";
    	    String playerList = CMParms.toStringList(A.getPlayers());
			for(Enumeration<String> p=A.getPlayers();p.hasMoreElements();)
			{
				MOB deadMOB=CMLib.players().getLoadPlayer(p.nextElement());
				CMLib.players().obliteratePlayer(deadMOB,false);
				Log.sysOut("Grinder",mob.Name()+" destroyed user "+deadMOB.Name()+".");
				deadMOB.destroy();
			}
		    CMLib.players().obliterateAccountOnly(A);
			Log.sysOut("Grinder",mob.Name()+" destroyed account "+A.accountName()+" and players '"+playerList+"'.");
            return "";
        }
		else
        if(parms.containsKey("DELCATALOGMOB"))
        {
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
            if(mob==null) return "@break@";
            String last=httpReq.getRequestParameter("DELMOB");
            if(last==null) return "@break@";
            if(last.length()==0) return "@break@";
            if(last.startsWith("CATALOG-")) {
                last=last.substring(8);
                MOB M=CMLib.catalog().getCatalogMob(last);
                if(M==null) return "@break@";
                if(!CMSecurity.isAllowedEverywhere(mob,"CATALOG")) return "@break@";
                CMLib.catalog().delCatalog(M);
                Log.sysOut("Grinder",mob.Name()+" destroyed catalog mob "+last);
                return "The catalog mob "+last+" has been removed.";
            }
            return "@break@";
        }
        else
        if(parms.containsKey("DELCATALOGITEM"))
        {
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
            if(mob==null) return "@break@";
            String last=httpReq.getRequestParameter("DELITEM");
            if(last==null) return "@break@";
            if(last.length()==0) return "@break@";
            if(last.startsWith("CATALOG-")) {
                last=last.substring(8);
                Item I=CMLib.catalog().getCatalogItem(last);
                if(I==null) return "@break@";
                if(!CMSecurity.isAllowedEverywhere(mob,"CATALOG")) return "@break@";
                CMLib.catalog().delCatalog(I);
                Log.sysOut("Grinder",mob.Name()+" destroyed catalog item "+last);
                return "The catalog item "+last+" has been removed.";
            }
            return "@break@";
        }
        else
        if(parms.containsKey("DELCLAN"))
        {
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
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
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
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
            MOB mob = Authenticate.getAuthenticatedMob(httpReq);
            if(mob==null) return "@break@";
            String last=httpReq.getRequestParameter("NEWCLANID");
            if(last==null) return "@break@";
            if(last.length()==0) return "@break@";
            Clan C=CMLib.clans().findClan(last);
            if((C!=null) 
            ||CMLib.players().playerExists(last)
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
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
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
    		try{C.execute(mob,V,0);}catch(Exception e){return e.getMessage();}
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
            MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			String AREA=httpReq.getRequestParameter("AREA");
			if(AREA==null) return "false";
			if(AREA.length()==0) return "false";
			Area A=CMLib.map().getArea(AREA);
			if(A==null)
			{
				String areaClass=httpReq.getRequestParameter("AREATYPE");
				A=CMClass.getAreaType(areaClass);
				A.setName(AREA);
				CMLib.map().addArea(A);
				CMLib.database().DBCreateArea(A);
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
            MOB mob = Authenticate.getAuthenticatedMob(httpReq);
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
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
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
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
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
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
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
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
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
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			Room R=CMLib.map().getRoom(httpReq.getRequestParameter("ROOM"));
			if(R==null) return "@break@";
			String errMsg=GrinderRooms.editRoom(httpReq,parms,mob,R);
			httpReq.addRequestParameters("ERRMSG",errMsg);
		}
        else
        if(parms.contains("DELHOLIDAY"))
        {
            MOB mob = Authenticate.getAuthenticatedMob(httpReq);
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
            MOB mob = Authenticate.getAuthenticatedMob(httpReq);
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
            MOB mob = Authenticate.getAuthenticatedMob(httpReq);
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
            MOB mob = Authenticate.getAuthenticatedMob(httpReq);
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
	        String errMsg=GrinderRaces.modifyRace(httpReq, parms, (oldR==null)?R:oldR, R);
            httpReq.addRequestParameters("ERRMSG",errMsg);
            if(!create)
            {
                CMLib.database().DBDeleteRace(R.ID());
                CMLib.database().DBCreateRace(R.ID(),R.racialParms());
                if((oldR!=null)&&(oldR!=R))
                    CMLib.utensils().swapRaces(R, oldR);
                Log.sysOut("Grinder",mob.name()+" modified race "+R.ID());
                return "Race "+R.ID()+" modified.";
            }
            CMClass.addRace(R);
            CMLib.database().DBCreateRace(R.ID(),R.racialParms());
            if((oldR!=null)&&(oldR!=R))
                CMLib.utensils().swapRaces(R, oldR);
            Log.sysOut("Grinder",mob.name()+" created race "+R.ID());
            if((oldR!=null)&&(!oldR.isGeneric()))
                return "Race "+R.ID()+" replaced with Generic Race " + R.ID()+".";
            return "Race "+R.ID()+" created.";
		}
        else
        if(parms.contains("DELCLASS"))
        {
            MOB mob = Authenticate.getAuthenticatedMob(httpReq);
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
            CMClass.loadClass("CHARCLASS","com/planet_ink/coffee_mud/CharClasses/"+oldCID+".class",true);
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
            MOB mob = Authenticate.getAuthenticatedMob(httpReq);
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
            String errMsg=GrinderClasses.modifyCharClass(httpReq, parms, (oldC==null)?C:oldC, C);
            httpReq.addRequestParameters("ERRMSG",errMsg);
            if(!create)
            {
                CMLib.database().DBDeleteClass(C.ID());
                CMLib.database().DBCreateClass(C.ID(),C.classParms());
                if((oldC!=null)&&(oldC!=C))
                    CMLib.utensils().reloadCharClasses(oldC);
                Log.sysOut("Grinder",mob.name()+" modified class "+C.ID());
                return "Char Class "+C.ID()+" modified.";
            }
            CMClass.addCharClass(C);
            CMLib.database().DBCreateClass(C.ID(),C.classParms());
            if((oldC!=null)&&(oldC!=C))
                CMLib.utensils().reloadCharClasses(oldC);
            Log.sysOut("Grinder",mob.name()+" created class "+C.ID());
            if((oldC!=null)&&(!oldC.isGeneric()))
                return "Char Class "+C.ID()+" replaced with Generic Class " + C.ID()+".";
            return "Char Class "+C.ID()+" created.";
        }
        else
        if(parms.contains("DELFACTION"))
        {
            MOB mob = Authenticate.getAuthenticatedMob(httpReq);
            if(mob==null) return "@break@";
            Faction F=null;
            String last=httpReq.getRequestParameter("FACTION");
            if(last==null) return " @break@";
            F=CMLib.factions().getFaction(last);
            if(F==null) return " @break@";
            java.io.File F2=new java.io.File(Resources.makeFileResourceName(F.factionID()));
            if(F2.exists()) F2.delete();
            Log.sysOut("Grinder",mob.Name()+" destroyed Faction "+F.name()+" ("+F.factionID()+").");
            Resources.removeResource(F.factionID());
            CMLib.factions().removeFaction(F.factionID());
            return "Faction "+F.ID()+" deleted.";
        }
        else
        if(parms.contains("EDITFACTION"))
        {
            MOB mob = Authenticate.getAuthenticatedMob(httpReq);
            if(mob==null) return "@break@";
            Faction F=null;
            String last=httpReq.getRequestParameter("FACTION");
            if(last==null) return " @break@";
            F=CMLib.factions().getFaction(last);
            //boolean create=false;
            if(F==null){
                //create=true;
                StringBuffer template=new CMFile(Resources.buildResourcePath("examples")+"factiontemplate.ini",null,true).text();
                if((template==null)||(template.length()==0))
                    return "The file 'resources/examples/factiontemplate.ini' could not be located and is required for command line faction creation.";
                Resources.submitResource(last,template);
                if(!Resources.saveFileResource("::"+last,mob,template))
                    return "Unable to save "+Resources.buildResourcePath("")+last;
                F=(Faction)CMClass.getCommon("DefaultFaction");
                F.initializeFaction(template,last);
                CMLib.factions().addFaction(F.factionID().toUpperCase(),F);
            }
            if(F==null) return " @break@";
            String errMsg=GrinderFactions.modifyFaction(httpReq, parms, F);
            if(errMsg.length()==0)
                errMsg=CMLib.factions().resaveFaction(F);
            httpReq.addRequestParameters("ERRMSG",errMsg);
            if(errMsg.length()==0)
                return "Faction "+F.ID()+" created/modified";
            return errMsg;
        }
        else
        if(parms.contains("DELABILITY"))
        {
            MOB mob = Authenticate.getAuthenticatedMob(httpReq);
            if(mob==null) return "@break@";
            Ability A=null;
            String last=httpReq.getRequestParameter("ABILITY");
            if(last==null) return " @break@";
            A=CMClass.getAbility(last);
            if((A==null)||(!A.isGeneric()))
                return " @break@";
            Object O=CMClass.getClass(A.ID());
            if(!(O instanceof Ability))
                return " @break@";
            CMClass.delClass("ABILITY",(Ability)O);
            CMLib.database().DBDeleteAbility(A.ID());
            Log.sysOut("Grinder",mob.name()+" deleted Ability "+A.ID());
            return "Ability "+A.ID()+" deleted.";
        }
        else
        if(parms.contains("EDITABILITY"))
        {
            MOB mob = Authenticate.getAuthenticatedMob(httpReq);
            if(mob==null) return "@break@";
            Ability A=null;
            Ability oldA=null;
            String last=httpReq.getRequestParameter("ABILITY");
            if(last==null) return " @break@";
            A=CMClass.getAbility(last);
            boolean create=false;
            if((A!=null)&&(!A.isGeneric()))
                return " @break@";
            if(A==null) {
                create=true;
                if(A!=null) oldA=A;
                A=(Ability)CMClass.getAbility("GenAbility").copyOf();
                A.setStat("CLASS",last);
            }
            if(A==null) return " @break@";
            String errMsg=GrinderAbilities.modifyAbility(httpReq, parms, (oldA==null)?A:oldA, A);
            httpReq.addRequestParameters("ERRMSG",errMsg);
            if(!create)
            {
                CMLib.database().DBDeleteAbility(A.ID());
                CMLib.database().DBCreateAbility(A.ID(),A.getStat("ALLXML"));
                Log.sysOut("Grinder",mob.name()+" modified ability "+A.ID());
                return "Ability "+A.ID()+" modified.";
            }
            CMLib.database().DBCreateAbility(A.ID(),A.getStat("ALLXML"));
            Log.sysOut("Grinder",mob.name()+" created ability "+A.ID());
            return "Ability "+A.ID()+" created.";
        }
		else
		if(parms.containsKey("EDITITEM"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
            String player=httpReq.getRequestParameter("PLAYER");
            MOB playerM=(player!=null)?CMLib.players().getLoadPlayer(player):null;
            String roomID=httpReq.getRequestParameter("ROOM");
			Room R=CMLib.map().getRoom(roomID);
			if((R==null)&&(playerM==null)&&((roomID==null)||(!roomID.equalsIgnoreCase("ANY")))) return "@break@";
			String errMsg=GrinderItems.editItem(httpReq,parms,mob,R,playerM);
			httpReq.addRequestParameters("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("EDITMOB"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
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
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			MOB M=CMLib.players().getLoadPlayer(httpReq.getRequestParameter("PLAYER"));
			if(M==null) return "@break@";
			String errMsg=GrinderPlayers.editPlayer(mob,httpReq,parms,M);
			httpReq.addRequestParameters("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("DELROOM"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
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
		if(parms.containsKey("RESETROOM"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			Room R=CMLib.map().getRoom(httpReq.getRequestParameter("ROOM"));
			if(R==null) return "@break@";
			CMLib.map().resetRoom(R,true);
			httpReq.addRequestParameters("ERRMSG","Room "+R.roomID()+" reset.");
		}
		else
		if(parms.containsKey("ADDROOM"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
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
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
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
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--) likeRoom.rawDoors()[d]=null;
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
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
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
			if(newRoom==null) 
				httpReq.addRequestParameters("ERRMSG","An error occurred trying to create your room.");
			else
			{
				Log.sysOut("Grinder",mob.Name()+" added room "+newRoom.roomID());
				httpReq.addRequestParameters("ROOM",newRoom.roomID());
			}
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
