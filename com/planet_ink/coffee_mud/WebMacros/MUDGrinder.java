package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.miniweb.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.grinder.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2013 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class MUDGrinder extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	public String runMacro(HTTPRequest httpReq, String parm)
	{
		java.util.Map<String,String> parms=parseParms(parm);
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return CMProps.getVar(CMProps.SYSTEM_MUDSTATUS);

		if(parms!=null)
		if(parms.containsKey("AREAMAP"))
		{
			String AREA=httpReq.getUrlParameter("AREA");
			if(AREA==null) return "";
			if(AREA.length()==0) return "";
			Area A=CMLib.map().getArea(AREA);
			if(A==null) return "";
			if((A.properSize()==0)&&(A.getRandomProperRoom()==null))
				GrinderRooms.createLonelyRoom(A,null,0,false);
			GrinderFlatMap map=null;
			int[] xyxy=this.getAppropriateXY(A,httpReq.getUrlParameter("MAPSTYLE"));
			if((httpReq.getUrlParameter("MAPSTYLE")!=null)
			&&(httpReq.getUrlParameter("MAPSTYLE").length()>0)
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
				String AREA = httpReq.getUrlParameter("AREA");
				if (AREA == null) return "";
				if (AREA.length() == 0) return "";
				Area A = CMLib.map().getArea(AREA);
				if (A == null)  return "";
				if((A.properSize()==0)&&(A.getRandomProperRoom()==null))
					GrinderRooms.createLonelyRoom(A, null, 0, false);
				GrinderFlatMap map=null;
				if((httpReq.getUrlParameter("MAPSTYLE")!=null)
				&&(httpReq.getUrlParameter("MAPSTYLE").length()>0)
				&&(!(A instanceof GridZones)))
					map=new GrinderMap(A,null);
				else
					map=new GrinderFlatMap(A,null);
				map.rePlaceRooms();
				String rS = httpReq.getUrlParameter("ROOMSIZE");
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
			if((httpReq.getUrlParameter("MAPSTYLE")!=null)
			&&(httpReq.getUrlParameter("MAPSTYLE").length()>0))
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
			boolean noInstances=parms.containsKey("NOINSTANCE");
			return GrinderAreas.getAreaList(pickedA,mob,noInstances);
		}
		else
		if(parms.containsKey("TEMPLATELIST"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			List<String> templateDirs=new LinkedList<String>();
			templateDirs.add("");
			StringBuilder str=new StringBuilder("");
			while(templateDirs.size()>0)
			{
				String templateDirPath=templateDirs.remove(0);
				CMFile templateDir=new CMFile(Resources.buildResourcePath("randareas/"+templateDirPath),mob,false);
				for(CMFile file : templateDir.listFiles())
				{
					if(file.isDirectory() && file.canRead())
						templateDirs.add(templateDirPath+file.getName()+"/");
					else
						str.append("<OPTION VALUE=\""+templateDirPath+file.getName()+"\">"+templateDirPath+file.getName());
				}
			}
			return str.toString();
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
			String last=httpReq.getUrlParameter("ACCOUNT");
			if(last==null) return "@break@";
			if(last.length()==0) return "@break@";
			PlayerAccount A=CMLib.players().getLoadAccount(last);
			if(A==null) return "@break@";
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDPLAYERS)) return "@break@";
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
			String last=httpReq.getUrlParameter("ACCOUNT");
			if(last==null) return "@break@";
			if(last.length()==0) return "@break@";
			PlayerAccount A=CMLib.players().getLoadAccount(last);
			if(A==null) return "@break@";
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDPLAYERS)) return "@break@";
			String playerList = CMParms.toStringList(A.getPlayers());
			for(Enumeration<String> p=A.getPlayers();p.hasMoreElements();)
			{
				MOB deadMOB=CMLib.players().getLoadPlayer(p.nextElement());
				CMLib.players().obliteratePlayer(deadMOB,true,false);
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
			String last=httpReq.getUrlParameter("DELMOB");
			if(last==null) return "@break@";
			if(last.length()==0) return "@break@";
			if(last.startsWith("CATALOG-")) {
				last=last.substring(8);
				MOB M=CMLib.catalog().getCatalogMob(last);
				if(M==null) return "@break@";
				if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CATALOG)) return "@break@";
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
			String last=httpReq.getUrlParameter("DELITEM");
			if(last==null) return "@break@";
			if(last.length()==0) return "@break@";
			if(last.startsWith("CATALOG-")) {
				last=last.substring(8);
				Item I=CMLib.catalog().getCatalogItem(last);
				if(I==null) return "@break@";
				if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CATALOG)) return "@break@";
				CMLib.catalog().delCatalog(I);
				Log.sysOut("Grinder",mob.Name()+" destroyed catalog item "+last);
				return "The catalog item "+last+" has been removed.";
			}
			return "@break@";
		}
		else
		if(parms.containsKey("DELCOMPONENT"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			String last=httpReq.getUrlParameter("COMPONENT");
			if(last==null) return "@break@";
			if(last.length()==0) return "@break@";
			List<AbilityComponent> list = CMLib.ableMapper().getAbilityComponentDVector(last);
			if(list==null) return "@break@";
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.COMPONENTS)) return "@break@";
			CMLib.ableMapper().getAbilityComponentMap().remove(last.toUpperCase().trim());
			CMLib.ableMapper().alterAbilityComponentFile(last, true);
			Log.sysOut("Grinder",mob.Name()+" destroyed component "+last);
			return "The component "+last+" has been successfully destroyed.";
		}
		else
		if(parms.containsKey("EDITCOMPONENT")||parms.containsKey("ADDCOMPONENT"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			String last=httpReq.getUrlParameter("COMPONENT");
			if(last==null) return "@break@";
			if(last.length()==0) return "@break@";
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.COMPONENTS)) return "@break@";
			String err=new GrinderComponent().runMacro(httpReq,parm);
			if(err.length()>0) return err;
			Log.sysOut("Grinder",mob.Name()+" modified component "+last);
			return "The component "+last+" has been successfully modified.";
		}
		else
		if(parms.containsKey("DELALLQUALIFY"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			String last=httpReq.getUrlParameter("ALLQUALID");
			if(last==null) return "@break@";
			if(last.length()==0) return "@break@";
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.ABILITIES)) return "@break@";
			String which=httpReq.getUrlParameter("ALLQUALWHICH");
			if(parms.containsKey("WHICH"))
				which=parms.get("WHICH");	
			if((which==null)||(which.length()==0))
				return " @break@";
			Map<String,Map<String,AbilityMapper.AbilityMapping>> allQualMap=CMLib.ableMapper().getAllQualifiesMap(httpReq.getRequestObjects());
			Map<String,AbilityMapper.AbilityMapping> map=allQualMap.get(which.toUpperCase().trim());
			if(map==null) return " @break@";
			map.remove(last.toUpperCase().trim());
			CMLib.ableMapper().saveAllQualifysFile(allQualMap);
			Log.sysOut("Grinder",mob.Name()+" destroyed all qualify ability "+last);
			return "The all qualify ability "+last+" has been successfully destroyed.";
		}
		else
		if(parms.containsKey("EDITALLQUALIFY")||parms.containsKey("ADDALLQUALIFY"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			String last=httpReq.getUrlParameter("ALLQUALID");
			if(last==null) return "@break@";
			if(last.length()==0) return "@break@";
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.ABILITIES)) return "@break@";
			String err=new GrinderAllQualifys().editAllQualify(httpReq, parms);
			if(err.length()>0) return err;
			Log.sysOut("Grinder",mob.Name()+" modified all qualify ability "+last);
			return "The all qualify ability "+last+" has been successfully modified.";
		}
		else
		if(parms.containsKey("DELCLAN"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			String last=httpReq.getUrlParameter("CLAN");
			if(last==null) return "@break@";
			if(last.length()==0) return "@break@";
			Clan C=CMLib.clans().getClan(last);
			if(C==null) return "@break@";
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDCLANS)) return "@break@";
			C.destroyClan();
			Log.sysOut("Grinder",mob.Name()+" destroyed clan "+C.clanID());
			return "The clan "+C.clanID()+" has been successfully destroyed.";
		}
		else
		if(parms.containsKey("EDITCLAN"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			String last=httpReq.getUrlParameter("CLAN");
			if(last==null) return "@break@";
			if(last.length()==0) return "@break@";
			Clan C=CMLib.clans().getClan(last);
			if(C==null) return "@break@";
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDCLANS)) return "@break@";
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
			String last=httpReq.getUrlParameter("NEWCLANID");
			if(last==null) return "@break@";
			if(last.length()==0) return "@break@";
			Clan C=CMLib.clans().findClan(last);
			if((C!=null) 
			||CMLib.players().playerExists(last)
			||(last.equalsIgnoreCase("All")))
				return "@break@";
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDCLANS)) return "@break@";
			Clan newClan=(Clan)CMClass.getCommon("DefaultClan");
			newClan.setName(last);
			newClan.setGovernmentID(CMLib.clans().getDefaultGovernment().getID());
			newClan.setStatus(Clan.CLANSTATUS_PENDING);
			newClan.create();
			Log.sysOut("Grinder",mob.Name()+" created clan "+newClan.clanID());
			httpReq.addFakeUrlParameter("CLAN",last);
			return "The clan "+newClan.clanID()+" has been successfully created.";
		}
		else
		if(parms.containsKey("DELCLANGOVERNMENT"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			String last=httpReq.getUrlParameter("GOVERNMENT");
			if(last==null) return "@break@";
			if(last.length()==0) return "@break@";
			ClanGovernment G=CMLib.clans().getStockGovernment(CMath.s_int(last));
			if(G==null) return "@break@";
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDCLANS)) return "@break@";
			if(!CMLib.clans().removeGovernment(G)) 
				return "Unable to remove last government.";
			Log.sysOut("Grinder",mob.Name()+" destroyed clan government "+G.getName());
			return "The clan government "+G.getName()+" has been successfully destroyed.";
		}
		else
		if(parms.containsKey("EDITCLANGOVERNMENT"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			String last=httpReq.getUrlParameter("GOVERNMENT");
			if(last==null) return "@break@";
			if(last.length()==0) return "@break@";
			ClanGovernment G=CMLib.clans().getStockGovernment(CMath.s_int(last));
			if(G==null) return "@break@";
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDCLANS)) return "@break@";
			String err=new GrinderClanGovernments().runMacro(httpReq,parm);
			if(err.length()>0) return err;
			CMLib.clans().reSaveGovernmentsXML();
			Log.sysOut("Grinder",mob.Name()+" modified clan government "+G.getName());
			return "The clan government "+G.getName()+" has been successfully modified.";
		}
		else
		if(parms.containsKey("IMPORTAREA"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			String file=httpReq.getUrlParameter("FILE");
			if(file==null) file="";
			byte[] bufBytes=(byte[])httpReq.getRequestObjects().get("FILE");
			if((file.length()==0)||(bufBytes==null)||(bufBytes.length==0))
				return "That file was empty.";
			boolean deleteIfExists=false;
			if(httpReq.getUrlParameter("DELFIRST")!=null)
				deleteIfExists=httpReq.getUrlParameter("DELFIRST").equalsIgnoreCase("ON");
			StringBuffer buf=new StringBuffer(CMStrings.bytesToStr(bufBytes));
			Vector<Object> V=new Vector<Object>();
			V.addAll(CMParms.parse("IMPORT "+(deleteIfExists?"":"NODELETE ")+"NOPROMPT"));
			V.add(buf);
			Command C=CMClass.getCommand("Import");
			if(C==null) return null;
			try{C.execute(mob,V,0);}catch(Exception e){return e.getMessage();}
			if((V.size()==0)||(V.get(V.size()-1) instanceof StringBuffer))
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
			String AREA=httpReq.getUrlParameter("AREA");
			if(AREA==null) return "false";
			if(AREA.length()==0) return "false";
			Area A=CMLib.map().getArea(AREA);
			if(A==null)
			{
				String areaClass=httpReq.getUrlParameter("AREATYPE");
				A=CMClass.getAreaType(areaClass);
				if(A==null) return "false";
				A.setName(AREA);
				CMLib.map().addArea(A);
				CMLib.database().DBCreateArea(A);
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
			String AREA=httpReq.getUrlParameter("AREA");
			if(AREA==null) return "";
			if(AREA.length()==0) return "";
			Area A=CMLib.map().getArea(AREA);
			if(A==null) return "";
			GrinderAreas.modifyArea(httpReq,parms);
			AREA=httpReq.getUrlParameter("AREA");
			Log.sysOut("Grinder",mob.Name()+" edited area "+A.Name());
		}
		else
		if(parms.containsKey("DELEXIT"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			Room R=CMLib.map().getRoom(httpReq.getUrlParameter("ROOM"));
			if(R==null) return "@break@";
			int dir=Directions.getGoodDirectionCode(httpReq.getUrlParameter("LINK"));
			if(dir<0) return "@break@";
			Log.sysOut("Grinder",mob.Name()+" deleted exit "+dir+" from "+R.roomID());
			String errMsg=GrinderExits.delExit(R,dir);
			httpReq.addFakeUrlParameter("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("EDITEXIT"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			Room R=CMLib.map().getRoom(httpReq.getUrlParameter("ROOM"));
			if(R==null) return "@break@";
			int dir=Directions.getGoodDirectionCode(httpReq.getUrlParameter("LINK"));
			if(dir<0) return "@break@";
			Log.sysOut("Grinder",mob.Name()+" modified exit "+dir+" from "+R.roomID());
			String errMsg=GrinderExits.editExit(R,dir,httpReq,parms);
			httpReq.addFakeUrlParameter("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("QUICKFIND"))
		{
			String find=httpReq.getUrlParameter("QUICKFIND");
			if(find==null) return "@break@";
			String AREA=httpReq.getUrlParameter("AREA");
			if(AREA==null) return "";
			if(AREA.length()==0) return "";
			Area A=CMLib.map().getArea(AREA);
			if(A==null) return "";
			String roomID=quickfind(A,find.toUpperCase());
			if(roomID!=null)
				httpReq.addFakeUrlParameter("ROOM",roomID);
			return "";
		}
		else
		if(parms.containsKey("LINKEXIT"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			Room R=CMLib.map().getRoom(httpReq.getUrlParameter("ROOM"));
			if(R==null) return "@break@";
			int dir=Directions.getGoodDirectionCode(httpReq.getUrlParameter("LINK"));
			if(dir<0) return "@break@";
			Room R2=CMLib.map().getRoom(httpReq.getUrlParameter("OLDROOM"));
			if(R2==null) return "@break@";
			int dir2=Directions.getGoodDirectionCode(httpReq.getUrlParameter("OLDLINK"));
			if(dir2<0) return "@break@";
			Log.sysOut("Grinder",mob.Name()+" linked exit "+dir+" from "+R.roomID());
			String errMsg=GrinderExits.linkRooms(R,R2,dir,dir2);
			httpReq.addFakeUrlParameter("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("LINKAREA"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			Room R=CMLib.map().getRoom(httpReq.getUrlParameter("ROOM"));
			if(R==null) return "@break@";
			int dir=Directions.getGoodDirectionCode(httpReq.getUrlParameter("LINK"));
			if(dir<0) return "@break@";
			String oldroom=httpReq.getUrlParameter("OLDROOM");
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
			httpReq.addFakeUrlParameter("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("EDITROOM"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			Room R=CMLib.map().getRoom(httpReq.getUrlParameter("ROOM"));
			if(R==null) return "@break@";
			final String multiFlagStr=httpReq.getUrlParameter("MULTIROOMFLAG");
			final boolean multiFlag=(multiFlagStr!=null)&& multiFlagStr.equalsIgnoreCase("on");
			final Vector<String> multiRoomList=CMParms.parseSemicolons(httpReq.getUrlParameter("MULTIROOMLIST"),false);
			String errMsg;
			if((multiFlag)&&(multiRoomList.size()>0))
			{
				errMsg="";
				Pair<String,String> setPairs[]=RoomData.makeMergableRoomFields(R, multiRoomList);
				for(final String id : multiRoomList)
				{
					if(id.length()==0) continue;
					R=CMLib.map().getRoom(id);
					if(R==null) return "@break@";
					HTTPRequest mergeReq=RoomData.mergeRoomFields(httpReq, setPairs, R);
					errMsg+=GrinderRooms.editRoom(mergeReq,parms,mob,R);
				}
			}
			else
				errMsg=GrinderRooms.editRoom(httpReq,parms,mob,R);
			httpReq.addFakeUrlParameter("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("DELHOLIDAY"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			String last=httpReq.getUrlParameter("HOLIDAY");
			if(last==null) return " @break@";
			int dex=CMLib.quests().getHolidayIndex(last);
			if(dex<0) return "Holiday '" + last +"' does not exist.";
			String err=CMLib.quests().deleteHoliday(dex);
			Log.sysOut("Grinder",mob.name()+" deleted holiday "+last);
			return (err.length()>0)?err:"Holiday '"+last+"' deleted.";
		}
		else
		if(parms.containsKey("EDITHOLIDAY"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			String last=httpReq.getUrlParameter("HOLIDAY");
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
		if(parms.containsKey("DELRACE"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			Race R=null;
			String last=httpReq.getUrlParameter("RACE");
			if(last==null) return " @break@";
			R=CMClass.getRace(last);
			if((R==null)||(!R.isGeneric()))
				return " @break@";
			String oldRID=R.ID();
			CMClass.delRace(R);
			CMLib.database().DBDeleteRace(R.ID());
			CMClass.loadClass(CMObjectType.RACE,"com/planet_ink/coffee_mud/Races/"+oldRID+".class",true);
			Race oldR=CMClass.getRace(oldRID);
			if(oldR==null) oldR=CMClass.getRace("StdRace");
			CMLib.utensils().swapRaces(oldR, R);
			Log.sysOut("Grinder",mob.name()+" deleted race "+R.ID());
			return "Race "+R.ID()+" deleted.";
		}
		else
		if(parms.containsKey("EDITRACE"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			Race R=null;
			Race oldR=null;
			String last=httpReq.getUrlParameter("RACE");
			if(last==null) return " @break@";
			R=CMClass.getRace(last);
			boolean create=false;
			if((R==null)||(!R.isGeneric())) {
				create=true;
				if(R!=null) oldR=R;
				R=(Race)CMClass.getRace("GenRace").copyOf();
				if(R==null) return " @break@";
				R.setRacialParms("<RACE><ID>"+last+"</ID><NAME>"+last+"</NAME></RACE>");
			}
			R=R.makeGenRace();
			String errMsg=GrinderRaces.modifyRace(httpReq, parms, (oldR==null)?R:oldR, R);
			httpReq.addFakeUrlParameter("ERRMSG",errMsg);
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
		if(parms.containsKey("DELCLASS"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			CharClass C=null;
			String last=httpReq.getUrlParameter("CLASS");
			if(last==null) return " @break@";
			C=CMClass.getCharClass(last);
			if((C==null)||(!C.isGeneric()))
				return " @break@";
			String oldCID=C.ID();
			CMClass.delCharClass(C);
			CMLib.database().DBDeleteClass(C.ID());
			CMClass.loadClass(CMObjectType.CHARCLASS,"com/planet_ink/coffee_mud/CharClasses/"+oldCID+".class",true);
			CharClass oldC=CMClass.getCharClass(oldCID);
			if(oldC==null) oldC=CMClass.getCharClass("StdCharClass");
			if((oldC!=null)&&(oldC!=C))
				CMLib.utensils().reloadCharClasses(oldC);
			Log.sysOut("Grinder",mob.name()+" deleted charclass "+C.ID());
			return "CharClass "+C.ID()+" deleted.";
		}
		else
		if(parms.containsKey("EDITCLASS"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			CharClass C=null;
			CharClass oldC=null;
			String last=httpReq.getUrlParameter("CLASS");
			if(last==null) return " @break@";
			C=CMClass.getCharClass(last);
			boolean create=false;
			if((C==null)||(!C.isGeneric())) {
				create=true;
				if(C!=null) oldC=C;
				C=(CharClass)CMClass.getCharClass("GenCharClass").copyOf();
				if(C==null) return " @break@";
				C.setClassParms("<CCLASS><ID>"+last+"</ID><NAME>"+last+"</NAME></CCLASS>");
			}
			C=C.makeGenCharClass();
			String errMsg=GrinderClasses.modifyCharClass(httpReq, parms, (oldC==null)?C:oldC, C);
			httpReq.addFakeUrlParameter("ERRMSG",errMsg);
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
		if(parms.containsKey("DELFACTION"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			Faction F=null;
			String last=httpReq.getUrlParameter("FACTION");
			if(last==null) return " @break@";
			F=CMLib.factions().getFaction(last);
			if(F==null) return " @break@";
			CMFile F2=new CMFile(Resources.makeFileResourceName(CMLib.factions().makeFactionFilename(F.factionID())),null,true);
			if(F2.exists()) F2.deleteAll();
			Log.sysOut("Grinder",mob.Name()+" destroyed Faction "+F.name()+" ("+F.factionID()+").");
			Resources.removeResource(F.factionID());
			F.destroy();
			return "Faction "+F.ID()+" deleted.";
		}
		else
		if(parms.containsKey("EDITFACTION"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			Faction F=null;
			String last=httpReq.getUrlParameter("FACTION");
			if(last==null) return " @break@";
			F=CMLib.factions().getFaction(last);
			//boolean create=false;
			if(F==null){
				//create=true;
				StringBuffer template=new CMFile(Resources.buildResourcePath("examples")+"factiontemplate.ini",null,true).text();
				if((template==null)||(template.length()==0))
					return "The file 'resources/examples/factiontemplate.ini' could not be located and is required for command line faction creation.";
				//Resources.submitResource(last,template);
				if(!Resources.saveFileResource("::"+CMLib.factions().makeFactionFilename(last),mob,template))
					return "Unable to save "+Resources.buildResourcePath("")+CMLib.factions().makeFactionFilename(last);
				F=(Faction)CMClass.getCommon("DefaultFaction");
				if(F==null) return " @break@";
				F.initializeFaction(template,last);
				CMLib.factions().addFaction(F.factionID().toUpperCase(),F);
			}
			String errMsg=GrinderFactions.modifyFaction(httpReq, parms, F);
			if(errMsg.length()==0)
				errMsg=CMLib.factions().resaveFaction(F);
			httpReq.addFakeUrlParameter("ERRMSG",errMsg);
			if(errMsg.length()==0)
				return "Faction "+F.ID()+" created/modified";
			return errMsg;
		}
		else
		if(parms.containsKey("DELABILITY"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			Ability A=null;
			String last=httpReq.getUrlParameter("ABILITY");
			if(last==null) return " @break@";
			A=CMClass.getAbility(last);
			if((A==null)||(!A.isGeneric()))
				return " @break@";
			Object O=CMClass.getObjectOrPrototype(A.ID());
			if(!(O instanceof Ability))
				return " @break@";
			CMClass.delClass(CMObjectType.ABILITY,(Ability)O);
			CMLib.database().DBDeleteAbility(A.ID());
			Log.sysOut("Grinder",mob.name()+" deleted Ability "+A.ID());
			return "Ability "+A.ID()+" deleted.";
		}
		else
		if(parms.containsKey("EDITABILITY"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			Ability A=null;
			Ability oldA=null;
			String last=httpReq.getUrlParameter("ABILITY");
			if(last==null) return " @break@";
			A=CMClass.getAbility(last);
			oldA=A;
			boolean create=false;
			if((A!=null)&&(!A.isGeneric()))
				return " @break@";
			String type="GenAbility";
			int code=CMath.s_int(httpReq.getUrlParameter("CLASSIFICATION_ACODE"));
			if(code==Ability.ACODE_LANGUAGE) type="GenLanguage";
			if(code==Ability.ACODE_COMMON_SKILL) type="GenCraftSkill";
			if(A==null) {
				create=true;
				A=(Ability)CMClass.getAbility(type).copyOf();
				if(A==null) return " @break@";
				A.setStat("CLASS",last);
			}
			String errMsg=GrinderAbilities.modifyAbility(httpReq, parms, (oldA==null)?A:oldA, A);
			httpReq.addFakeUrlParameter("ERRMSG",errMsg);
			if(!create)
			{
				CMLib.database().DBDeleteAbility(A.ID());
				CMLib.database().DBCreateAbility(A.ID(),type,A.getStat("ALLXML"));
				Log.sysOut("Grinder",mob.name()+" modified ability "+A.ID()+" ("+type+")");
				return "Ability "+A.ID()+" modified.";
			}
			CMLib.database().DBCreateAbility(A.ID(),type,A.getStat("ALLXML"));
			Log.sysOut("Grinder",mob.name()+" created ability "+A.ID()+" ("+type+")");
			return type+" "+A.ID()+" created.";
		}
		else
		if(parms.containsKey("EDITITEM"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			String player=httpReq.getUrlParameter("PLAYER");
			MOB playerM=(player!=null)?CMLib.players().getLoadPlayer(player):null;
			String roomID=httpReq.getUrlParameter("ROOM");
			Room R=CMLib.map().getRoom(roomID);
			if((R==null)&&(playerM==null)&&((roomID==null)||(!roomID.equalsIgnoreCase("ANY")))) return "@break@";
			String errMsg=GrinderItems.editItem(httpReq,parms,mob,R,playerM);
			httpReq.addFakeUrlParameter("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("EDITMOB"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			String roomID=httpReq.getUrlParameter("ROOM");
			Room R=CMLib.map().getRoom(roomID);
			if((R==null)&&((roomID==null)||(!roomID.equalsIgnoreCase("ANY")))) return "@break@";
			String errMsg=GrinderMobs.editMob(httpReq,parms,mob,R);
			httpReq.addFakeUrlParameter("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("EDITPLAYER"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			MOB M=CMLib.players().getLoadPlayer(httpReq.getUrlParameter("PLAYER"));
			if(M==null) return "@break@";
			String errMsg=GrinderPlayers.editPlayer(mob,httpReq,parms,M);
			httpReq.addFakeUrlParameter("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("DELROOM"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			final String multiFlagStr=httpReq.getUrlParameter("MULTIROOMFLAG");
			final boolean multiFlag=(multiFlagStr!=null)&& multiFlagStr.equalsIgnoreCase("on");
			final Vector<String> multiRoomList=CMParms.parseSemicolons(httpReq.getUrlParameter("MULTIROOMLIST"),false);
			List<Room> rooms = new LinkedList<Room>();
			if((multiFlag)&&(multiRoomList.size()>0))
			{
				for(final String id : multiRoomList)
				{
					if(id.length()==0) continue;
					Room R=CMLib.map().getRoom(id);
					if(R==null) return "@break@";
					rooms.add(R);
				}
			}
			else
			{
				Room R=CMLib.map().getRoom(httpReq.getUrlParameter("ROOM"));
				if(R==null) return "@break@";
				rooms.add(R);
			}
			String errMsg="";
			for(final Room R : rooms)
			{
				for(int d=0;d<R.rawDoors().length;d++)
					if(R.rawDoors()[d]!=null)
					{
						httpReq.addFakeUrlParameter("ROOM",R.rawDoors()[d].roomID());
						httpReq.addFakeUrlParameter("LINK","");
						break;
					}
				Log.sysOut("Grinder",mob.Name()+" deleted room "+R.roomID());
				errMsg+=GrinderRooms.delRoom(R)+" ";
			}
			httpReq.addFakeUrlParameter("ERRMSG",errMsg);
			httpReq.removeUrlParameter("ROOM");
			httpReq.removeUrlParameter("MULTIROOMLIST");
			httpReq.removeUrlParameter("MULTIROOMFLAG");
		}
		else
		if(parms.containsKey("RESETROOM"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			final String multiFlagStr=httpReq.getUrlParameter("MULTIROOMFLAG");
			final boolean multiFlag=(multiFlagStr!=null)&& multiFlagStr.equalsIgnoreCase("on");
			final Vector<String> multiRoomList=CMParms.parseSemicolons(httpReq.getUrlParameter("MULTIROOMLIST"),false);
			List<Room> rooms = new LinkedList<Room>();
			if((multiFlag)&&(multiRoomList.size()>0))
			{
				for(final String id : multiRoomList)
				{
					if(id.length()==0) continue;
					Room R=CMLib.map().getRoom(id);
					if(R==null) return "@break@";
					rooms.add(R);
				}
			}
			else
			{
				Room R=CMLib.map().getRoom(httpReq.getUrlParameter("ROOM"));
				if(R==null) return "@break@";
				rooms.add(R);
			}
			String errMsg="";
			for(final Room R : rooms)
			{
				CMLib.map().resetRoom(R,true);
				errMsg+="Room "+R.roomID()+" reset.  ";
			}
			httpReq.addFakeUrlParameter("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("ADDROOM"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			String link=httpReq.getUrlParameter("LINK");
			Room R=CMLib.map().getRoom(httpReq.getUrlParameter("ROOM"));
			if(R==null) return "@break@";
			int dir=Directions.getGoodDirectionCode(link);
			if(dir<0) return "@break@";
			String copyThisOne=httpReq.getUrlParameter("COPYROOM");
			String errMsg=GrinderRooms.createRoom(R,dir,(copyThisOne!=null)&&(copyThisOne.equalsIgnoreCase("ON")));
			httpReq.addFakeUrlParameter("ERRMSG",errMsg);
			R=R.rawDoors()[dir];
			if(R!=null)
			{
				httpReq.addFakeUrlParameter("ROOM",R.roomID());
				Log.sysOut("Grinder",mob.Name()+" added room "+R.roomID());
			}
			httpReq.addFakeUrlParameter("LINK","");
		}
		else
		if(parms.containsKey("PAINTROOMS"))
		{
			MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null) return "@break@";
			String AREA=httpReq.getUrlParameter("AREA");
			if(AREA==null) return "@break@";
			Area A=CMLib.map().getArea(AREA);
			if(A==null) return "@break@";
			String like=httpReq.getUrlParameter("ROOM");
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
			boolean overwrite=httpReq.getUrlParameter("NOVERLAP")!=null
								&&(httpReq.getUrlParameter("NOVERLAP").length()>0);
			int brushSize=CMath.s_int(httpReq.getUrlParameter("BRUSHSIZE"));
			if(brushSize<=0) return "Your brush size should be a number greater than 0!";
			int[] coords=getAppropriateXY(A,httpReq.getUrlParameter("PAINTAT"));
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
			String AREA=httpReq.getUrlParameter("AREA");
			if(AREA==null) return "false";
			Area A=CMLib.map().getArea(AREA);
			if(A==null) return "@break@";
			String roomID=httpReq.getUrlParameter("ROOM");
			String link=httpReq.getUrlParameter("AUTOLINK");
			Room R=CMLib.map().getRoom(roomID);
			if((roomID==null)||(R!=null)||(roomID.length()==0)) return "@break@";
			String copyThisOne=httpReq.getUrlParameter("COPYROOM");
			Room copyRoom=CMLib.map().getRoom(copyThisOne);
			Room newRoom=GrinderRooms.createGridRoom(A,roomID,copyRoom,null,((link!=null)&&(link.length()>0)));
			if(newRoom==null) 
				httpReq.addFakeUrlParameter("ERRMSG","An error occurred trying to create your room.");
			else
			{
				Log.sysOut("Grinder",mob.Name()+" added room "+newRoom.roomID());
				httpReq.addFakeUrlParameter("ROOM",newRoom.roomID());
			}
			httpReq.addFakeUrlParameter("LINK","");
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
			int x=mapStyle.indexOf('_');
			int index=0;
			while(x>=0)
			{
				String coord=mapStyle.substring(0,x);
				mapStyle=mapStyle.substring(x+1);
				if((index<xyxy.length)&&(CMath.isInteger(coord)))
					xyxy[index++]=CMath.s_int(coord);
				else
					break;
				x=mapStyle.indexOf('_');
			}
			if((index<xyxy.length)&&(CMath.isInteger(mapStyle)))
				xyxy[index++]=CMath.s_int(mapStyle);
			return xyxy;
		}
		return null;
	}

	protected Area getLoggedArea(HTTPRequest httpReq, MOB mob)
	{
		String AREA=httpReq.getUrlParameter("AREA");
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
		for(Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
		{
			R=r.nextElement();
			if((R.roomID().length()>0)&&(R.roomID().toUpperCase().endsWith(find.toUpperCase())))
				return R.roomID();
		}
		for(Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
		{
			R=r.nextElement();
			if((R.roomID().length()>0)&&(R.displayText().toUpperCase().indexOf(find.toUpperCase())>=0))
				return R.roomID();
		}
		for(Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
		{
			R=r.nextElement();
			if((R.roomID().length()>0)&&(R.description().toUpperCase().indexOf(find.toUpperCase())>=0))
				return R.roomID();
		}
		R=CMLib.map().getRoom(find);
		if((R!=null)&&(R.roomID().length()>0)) return R.roomID();
		return null;
	}
}
