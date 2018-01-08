package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.http.MultiPartData;
import com.planet_ink.coffee_web.interfaces.*;
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
   Copyright 2002-2018 Bo Zimmerman

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
	@Override
	public String name()
	{
		return "MUDGrinder";
	}

	@Override
	public boolean isAdminMacro()
	{
		return true;
	}

	public static Area getAreaObject(String ID)
	{
		if(ID==null)
			return null;
		if(ID.length()==0)
			return null;
		if(ID.startsWith("ITEM#<")&&(ID.endsWith(">")))
		{
			String xmlIsh=ID.substring(5);
			String playerCode=null;
			String roomCode=null;
			String mobCode=null;
			String itemCode=null;
			int x=xmlIsh.indexOf(">");
			while(x>=0)
			{
				xmlIsh=xmlIsh.substring(1);
				x--;
				while((x<xmlIsh.length()-1)&&(xmlIsh.charAt(x+1)!='<'))
					x=xmlIsh.indexOf(">",x+1);
				if(x>=0)
				{
					String thing=xmlIsh.substring(0,x);
					if(thing.startsWith("P=")&&(x>2))
						playerCode=thing.substring(2);
					else
					if(thing.startsWith("I=")&&(x>2))
						itemCode=thing.substring(2);
					else
					if(thing.startsWith("R=")&&(x>2))
						roomCode=thing.substring(2);
					else
					if(thing.startsWith("M=")&&(x>2))
						mobCode=thing.substring(2);
				}
				if((x>=0)&&(x<xmlIsh.length()-1))
					xmlIsh=xmlIsh.substring(x+1);
				else
					break;
				x=xmlIsh.indexOf(">");
			}
			if(itemCode == null)
			{
				return null;
			}
			MOB playerM=null;
			Room R=null;
			if(playerCode!=null)
				playerM=CMLib.players().getLoadPlayer(playerCode);
			if((playerCode==null)&&(roomCode!=null))
			{
				if(!roomCode.equalsIgnoreCase("ANY"))
					R=getRoomObject((HTTPRequest)null,roomCode);
			}
			Item I=null;
			MOB M=null;
			final String sync=("SYNC"+((R!=null)?R.roomID():playerCode));
			synchronized(sync.intern())
			{
				if(R!=null)
					R=CMLib.map().getRoom(R);
				
				if((playerM!=null)&&(R==null))
				{
					I=RoomData.getItemFromCode(playerM,itemCode);
					M=playerM;
				}
				else
				if((mobCode!=null)&&(mobCode.length()>0))
				{
					if(R!=null)
						M=RoomData.getMOBFromCode(R,mobCode);
					else
						M=RoomData.getMOBFromCode(RoomData.getMOBCache(),mobCode);
					if(M!=null)
					{
						I=RoomData.getItemFromCode(M,itemCode);
						if(I==null)
							I=RoomData.getItemFromCode((MOB)null,itemCode);
					}
				}
				else
				if(R!=null)
				{
					I=RoomData.getItemFromCode(R,itemCode);
					if(I==null)
						I=RoomData.getItemFromCode((Room)null,itemCode);
				}
				if(I==null)
				{
					if(itemCode.startsWith("CATALOG-")||itemCode.startsWith("NEWCATA-"))
					{
						I=CMLib.catalog().getCatalogItem(itemCode.substring(8));
						if(I==null)
							I=CMClass.getItem("GenItem");
						else
							I=(Item)I.copyOf();
					}
					else
						I=RoomData.getItemFromAnywhere(RoomData.getItemCache(),itemCode);
				}
			}
			if(I instanceof BoardableShip)
			{
				return ((BoardableShip)I).getShipArea();
			}
		}
		else
		{
			return CMLib.map().getArea(ID);
		}
		return null;
	}
	
	public static Room getRoomObject(String AREA, String ID)
	{
		if(ID==null)
			return null;
		if(ID.length()==0)
			return null;
		if((AREA!=null)&&(AREA.startsWith("ITEM#<"))&&(AREA.endsWith(">")))
		{
			final Area A=getAreaObject(AREA);
			if(A!=null)
				return A.getRoom(ID);
		}
		final Room R=CMLib.map().getRoom(ID);
		if(R!=null)
			return R;
		final Area A=getAreaObject(AREA);
		if(A!=null)
			return A.getRoom(ID);
		return null;
	}
	
	public static Room getRoomObject(HTTPRequest req, String ID)
	{
		if(ID==null)
			return null;
		if(ID.length()==0)
			return null;
		if(req != null)
		{
			String areaID=req.getUrlParameter("AREA");
			if((areaID!=null)&&(areaID.startsWith("ITEM#<"))&&(areaID.endsWith(">")))
				return getRoomObject(areaID, ID);
		}
		final Room R=CMLib.map().getRoom(ID);
		if(R!=null)
			return R;
		return (req==null) ? null : getRoomObject(req.getUrlParameter("AREA"), ID);
	}
	
	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return CMProps.getVar(CMProps.Str.MUDSTATUS);

		if(parms!=null)
		if(parms.containsKey("AREAMAP"))
		{
			final String AREA=httpReq.getUrlParameter("AREA");
			final Area A=getAreaObject(AREA);
			if(A==null)
				return "";
			if((A.properSize()==0)&&(A.getRandomProperRoom()==null))
				GrinderRooms.createLonelyRoom(A,null,0,false);
			GrinderFlatMap map=null;
			final int[] xyxy=this.getAppropriateXY(A,httpReq.getUrlParameter("MAPSTYLE"));
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
				final String AREA = httpReq.getUrlParameter("AREA");
				final Area A=getAreaObject(AREA);
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
				final String rS = httpReq.getUrlParameter("ROOMSIZE");
				final int roomSize = (rS!=null)?CMath.s_int(rS):4;
				if (roomSize <= 4)
					return map.getHTMLMap(httpReq).toString();
				return map.getHTMLMap(httpReq, roomSize).toString();
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
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
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final Area pickedA=getLoggedArea(httpReq,mob);
			final boolean noInstances=parms.containsKey("NOINSTANCE");
			return GrinderAreas.getAreaList(CMLib.map().mundaneAreas(),pickedA,mob,noInstances);
		}
		else
		if(parms.containsKey("PLANETLIST"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final Area pickedA=getLoggedArea(httpReq,mob);
			final boolean noInstances=parms.containsKey("NOINSTANCE");
			return GrinderAreas.getAreaList(CMLib.map().spaceAreas(),pickedA,mob,noInstances);
		}
		else
		if(parms.containsKey("ISSPACE"))
		{
			return ""+CMLib.map().spaceAreas().hasMoreElements();
		}
		else
		if(parms.containsKey("DELAREA"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final Area A=getLoggedArea(httpReq,mob);
			if(A==null)
				return "@break@";
			CMLib.map().obliterateArea(A);
			Log.sysOut("Grinder",mob.Name()+" obliterated area "+A.Name());
			return "The area "+A.Name()+" has been successfully deleted.";
		}
		else
		if(parms.containsKey("EDITACCOUNT"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final String last=httpReq.getUrlParameter("ACCOUNT");
			if(last==null)
				return "@break@";
			if(last.length()==0)
				return "@break@";
			final PlayerAccount A=CMLib.players().getLoadAccount(last);
			if(A==null)
				return "@break@";
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDPLAYERS))
				return "@break@";
			final String err=new GrinderAccounts().runMacro(httpReq,parm);
			if(err.length()>0)
				return err;
			Log.sysOut("Grinder",mob.Name()+" modified account "+A.getAccountName());
			return "";
		}
		else
		if(parms.containsKey("DELACCOUNT"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final String last=httpReq.getUrlParameter("ACCOUNT");
			if(last==null)
				return "@break@";
			if(last.length()==0)
				return "@break@";
			final PlayerAccount A=CMLib.players().getLoadAccount(last);
			if(A==null)
				return "@break@";
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDPLAYERS))
				return "@break@";
			final String playerList = CMParms.toListString(A.getPlayers());
			for(final Enumeration<String> p=A.getPlayers();p.hasMoreElements();)
			{
				final MOB deadMOB=CMLib.players().getLoadPlayer(p.nextElement());
				CMLib.players().obliteratePlayer(deadMOB,true,false);
				Log.sysOut("Grinder",mob.Name()+" destroyed user "+deadMOB.Name()+".");
				deadMOB.destroy();
			}
			CMLib.players().obliterateAccountOnly(A);
			Log.sysOut("Grinder",mob.Name()+" destroyed account "+A.getAccountName()+" and players '"+playerList+"'.");
			return "";
		}
		else
		if(parms.containsKey("DELCATALOGMOB"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			String last=httpReq.getUrlParameter("DELMOB");
			if(last==null)
				return "@break@";
			if(last.length()==0)
				return "@break@";
			if(last.startsWith("CATALOG-"))
			{
				last=last.substring(8);
				final MOB M=CMLib.catalog().getCatalogMob(last);
				if(M==null)
					return "@break@";
				if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CATALOG))
					return "@break@";
				CMLib.catalog().delCatalog(M);
				Log.sysOut("Grinder",mob.Name()+" destroyed catalog mob "+last);
				return "The catalog mob "+last+" has been removed.";
			}
			return "@break@";
		}
		else
		if(parms.containsKey("DELCATALOGITEM"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			String last=httpReq.getUrlParameter("DELITEM");
			if(last==null)
				return "@break@";
			if(last.length()==0)
				return "@break@";
			if(last.startsWith("CATALOG-"))
			{
				last=last.substring(8);
				final Item I=CMLib.catalog().getCatalogItem(last);
				if(I==null)
					return "@break@";
				if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CATALOG))
					return "@break@";
				CMLib.catalog().delCatalog(I);
				Log.sysOut("Grinder",mob.Name()+" destroyed catalog item "+last);
				return "The catalog item "+last+" has been removed.";
			}
			return "@break@";
		}
		else
		if(parms.containsKey("DELCOMPONENT"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final String last=httpReq.getUrlParameter("COMPONENT");
			if(last==null)
				return "@break@";
			if(last.length()==0)
				return "@break@";
			final List<AbilityComponent> list = CMLib.ableComponents().getAbilityComponents(last);
			if(list==null)
				return "@break@";
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.COMPONENTS))
				return "@break@";
			CMLib.ableComponents().getAbilityComponentMap().remove(last.toUpperCase().trim());
			CMLib.ableComponents().alterAbilityComponentFile(last, true);
			Log.sysOut("Grinder",mob.Name()+" destroyed component "+last);
			return "The component "+last+" has been successfully destroyed.";
		}
		else
		if(parms.containsKey("EDITCOMPONENT")||parms.containsKey("ADDCOMPONENT"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final String last=httpReq.getUrlParameter("COMPONENT");
			if(last==null)
				return "@break@";
			if(last.length()==0)
				return "@break@";
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.COMPONENTS))
				return "@break@";
			final String err=new GrinderComponent().runMacro(httpReq,parm);
			if(err.length()>0)
				return err;
			Log.sysOut("Grinder",mob.Name()+" modified component "+last);
			return "The component "+last+" has been successfully modified.";
		}
		else
		if(parms.containsKey("DELALLQUALIFY"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final String last=httpReq.getUrlParameter("ALLQUALID");
			if(last==null)
				return "@break@";
			if(last.length()==0)
				return "@break@";
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.ABILITIES))
				return "@break@";
			String which=httpReq.getUrlParameter("ALLQUALWHICH");
			if(parms.containsKey("WHICH"))
				which=parms.get("WHICH");
			if((which==null)||(which.length()==0))
				return " @break@";
			final Map<String,Map<String,AbilityMapper.AbilityMapping>> allQualMap=CMLib.ableMapper().getAllQualifiesMap(httpReq.getRequestObjects());
			final Map<String,AbilityMapper.AbilityMapping> map=allQualMap.get(which.toUpperCase().trim());
			if(map==null)
				return " @break@";
			map.remove(last.toUpperCase().trim());
			CMLib.ableMapper().saveAllQualifysFile(allQualMap);
			Log.sysOut("Grinder",mob.Name()+" destroyed all qualify ability "+last);
			return "The all qualify ability "+last+" has been successfully destroyed.";
		}
		else
		if(parms.containsKey("EDITALLQUALIFY")||parms.containsKey("ADDALLQUALIFY"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final String last=httpReq.getUrlParameter("ALLQUALID");
			if(last==null)
				return "@break@";
			if(last.length()==0)
				return "@break@";
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.ABILITIES))
				return "@break@";
			final String err=new GrinderAllQualifys().editAllQualify(httpReq, parms);
			if(err.length()>0)
				return err;
			Log.sysOut("Grinder",mob.Name()+" modified all qualify ability "+last);
			return "The all qualify ability "+last+" has been successfully modified.";
		}
		else
		if(parms.containsKey("DELCLAN"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final String last=httpReq.getUrlParameter("CLAN");
			if(last==null)
				return "@break@";
			if(last.length()==0)
				return "@break@";
			final Clan C=CMLib.clans().getClan(last);
			if(C==null)
				return "@break@";
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDCLANS))
				return "@break@";
			C.destroyClan();
			Log.sysOut("Grinder",mob.Name()+" destroyed clan "+C.clanID());
			return "The clan "+C.clanID()+" has been successfully destroyed.";
		}
		else
		if(parms.containsKey("EDITCLAN"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final String last=httpReq.getUrlParameter("CLAN");
			if(last==null)
				return "@break@";
			if(last.length()==0)
				return "@break@";
			final Clan C=CMLib.clans().getClan(last);
			if(C==null)
				return "@break@";
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDCLANS))
				return "@break@";
			final String err=new GrinderClans().runMacro(httpReq,parm);
			if(err.length()>0)
				return err;
			C.update();
			Log.sysOut("Grinder",mob.Name()+" modified clan "+C.clanID());
			return "The clan "+C.clanID()+" has been successfully modified.";
		}
		else
		if(parms.containsKey("ADDCLAN"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final String last=httpReq.getUrlParameter("NEWCLANID");
			if(last==null)
				return "@break@";
			if(last.length()==0)
				return "@break@";
			final Clan C=CMLib.clans().findClan(last);
			if((C!=null)
			||CMLib.players().playerExists(last)
			||(last.equalsIgnoreCase("All")))
				return "@break@";
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDCLANS))
				return "@break@";
			final Clan newClan=(Clan)CMClass.getCommon("DefaultClan");
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
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final String last=httpReq.getUrlParameter("GOVERNMENT");
			if(last==null)
				return "@break@";
			if(last.length()==0)
				return "@break@";
			final ClanGovernment G=CMLib.clans().getStockGovernment(CMath.s_int(last));
			if(G==null)
				return "@break@";
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDCLANS))
				return "@break@";
			if(!CMLib.clans().removeGovernment(G))
				return "Unable to remove last government.";
			Log.sysOut("Grinder",mob.Name()+" destroyed clan government "+G.getName());
			return "The clan government "+G.getName()+" has been successfully destroyed.";
		}
		else
		if(parms.containsKey("EDITCLANGOVERNMENT"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final String last=httpReq.getUrlParameter("GOVERNMENT");
			if(last==null)
				return "@break@";
			if(last.length()==0)
				return "@break@";
			final ClanGovernment G=CMLib.clans().getStockGovernment(CMath.s_int(last));
			if(G==null)
				return "@break@";
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDCLANS))
				return "@break@";
			final String err=new GrinderClanGovernments().runMacro(httpReq,parm);
			if(err.length()>0)
				return err;
			CMLib.clans().reSaveGovernmentsXML();
			Log.sysOut("Grinder",mob.Name()+" modified clan government "+G.getName());
			return "The clan government "+G.getName()+" has been successfully modified.";
		}
		else
		if(parms.containsKey("IMPORTAREA"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			byte[] bufBytes=null;
			String file="";
			for(final MultiPartData d : httpReq.getMultiParts())
			{
				if((d.getVariables().containsKey("filename"))&&(d.getData()!=null))
				{
					bufBytes=d.getData();
					file=d.getVariables().get("filename");
					break;
				}
			}
			if((file.length()==0)||(bufBytes==null)||(bufBytes.length==0))
				return "That file was empty.";
			boolean deleteIfExists=false;
			if(httpReq.getUrlParameter("DELFIRST")!=null)
				deleteIfExists=httpReq.getUrlParameter("DELFIRST").equalsIgnoreCase("ON");
			final StringBuffer buf=new StringBuffer(CMStrings.bytesToStr(bufBytes));
			Vector<Object> V=new Vector<Object>();
			V.addAll(CMParms.parse("IMPORT "+(deleteIfExists?"":"NODELETE ")+"NOPROMPT"));
			V.add(buf);
			final Command C=CMClass.getCommand("Import");
			if(C==null)
				return null;
			try
			{
				V = (Vector<Object>)C.executeInternal(mob, 0, V.toArray(new Object[0]));
			}
			catch (final Exception e)
			{
				return e.getMessage();
			}
			if((V.size()==0)||(V.get(V.size()-1) instanceof StringBuffer))
				return "<FONT COLOR=LIGHTGREEN>Your file was successfully imported.</FONT>";
			final StringBuffer error=new StringBuffer("");
			for(int i=0;i<V.size();i++)
			{
				if(V.elementAt(i) instanceof String)
					error.append(((String)V.elementAt(i))+"<BR>");
			}
			return error.toString();
		}
		else
		if(parms.containsKey("ADDAREA"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			String AREA=httpReq.getUrlParameter("AREA");
			if(AREA==null)
				return "false";
			if(AREA.length()==0)
				return "false";
			Area A=getAreaObject(AREA);
			if(A==null)
			{
				final String areaClass=httpReq.getUrlParameter("AREATYPE");
				A=CMClass.getAreaType(areaClass);
				if(A==null)
					return "false";
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
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			String AREA=httpReq.getUrlParameter("AREA");
			final Area A=getAreaObject(AREA);
			if(A==null)
				return "";
			GrinderAreas.modifyArea(httpReq,parms);
			AREA=httpReq.getUrlParameter("AREA");
			Log.sysOut("Grinder",mob.Name()+" edited area "+A.Name());
		}
		else
		if(parms.containsKey("DELEXIT"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final Room R=getRoomObject(httpReq,httpReq.getUrlParameter("ROOM"));
			if(R==null)
				return "@break@";
			final int dir=CMLib.directions().getGoodDirectionCode(httpReq.getUrlParameter("LINK"));
			if(dir<0)
				return "@break@";
			Log.sysOut("Grinder",mob.Name()+" deleted exit "+dir+" from "+R.roomID());
			final String errMsg=GrinderExits.delExit(R,dir);
			httpReq.addFakeUrlParameter("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("EDITEXIT"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final Room R=getRoomObject(httpReq,httpReq.getUrlParameter("ROOM"));
			if(R==null)
				return "@break@";
			final int dir=CMLib.directions().getGoodDirectionCode(httpReq.getUrlParameter("LINK"));
			if(dir<0)
				return "@break@";
			Log.sysOut("Grinder",mob.Name()+" modified exit "+dir+" from "+R.roomID());
			final String errMsg=GrinderExits.editExit(R,dir,httpReq,parms);
			httpReq.addFakeUrlParameter("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("QUICKFIND"))
		{
			final String find=httpReq.getUrlParameter("QUICKFIND");
			if(find==null)
				return "@break@";
			final String AREA=httpReq.getUrlParameter("AREA");
			final Area A=getAreaObject(AREA);
			if(A==null)
				return "";
			final String roomID=quickfind(A,find.toUpperCase());
			if(roomID!=null)
				httpReq.addFakeUrlParameter("ROOM",roomID);
			return "";
		}
		else
		if(parms.containsKey("LINKEXIT"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final Room R=getRoomObject(httpReq,httpReq.getUrlParameter("ROOM"));
			if(R==null)
				return "@break@";
			final int dir=CMLib.directions().getGoodDirectionCode(httpReq.getUrlParameter("LINK"));
			if(dir<0)
				return "@break@";
			final Room R2=getRoomObject(httpReq,httpReq.getUrlParameter("OLDROOM"));
			if(R2==null)
				return "@break@";
			final int dir2=CMLib.directions().getGoodDirectionCode(httpReq.getUrlParameter("OLDLINK"));
			if(dir2<0)
				return "@break@";
			Log.sysOut("Grinder",mob.Name()+" linked exit "+dir+" from "+R.roomID());
			final String errMsg=GrinderExits.linkRooms(R,R2,dir,dir2);
			httpReq.addFakeUrlParameter("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("CREATEEXIT"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final Room R=getRoomObject(httpReq,httpReq.getUrlParameter("ROOM"));
			if(R==null)
				return "@break@";
			final int dir=CMLib.directions().getGoodDirectionCode(httpReq.getUrlParameter("LINK"));
			if(dir<0)
				return "@break@";
			Log.sysOut("Grinder",mob.Name()+" created exit for "+dir+" from "+R.roomID());
			final String errMsg=GrinderExits.createExitForRoom(R,dir);
			httpReq.addFakeUrlParameter("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("LINKAREA"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final Room R=getRoomObject(httpReq,httpReq.getUrlParameter("ROOM"));
			if(R==null)
				return "@break@";
			final int dir=CMLib.directions().getGoodDirectionCode(httpReq.getUrlParameter("LINK"));
			if(dir<0)
				return "@break@";
			String oldroom=httpReq.getUrlParameter("OLDROOM");
			if(oldroom==null)
				oldroom="";
			final Room R2=CMLib.map().getRoom(oldroom);
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
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			Room R=getRoomObject(httpReq,httpReq.getUrlParameter("ROOM"));
			if(R==null)
				return "@break@";
			final String multiFlagStr=httpReq.getUrlParameter("MULTIROOMFLAG");
			final boolean multiFlag=(multiFlagStr!=null)&& multiFlagStr.equalsIgnoreCase("on");
			final List<String> multiRoomList=CMParms.parseSemicolons(httpReq.getUrlParameter("MULTIROOMLIST"),false);
			String errMsg;
			if((multiFlag)&&(multiRoomList.size()>0))
			{
				errMsg="";
				final Pair<String,String> setPairs[]=RoomData.makeMergableRoomFields(httpReq, R, multiRoomList);
				for(final String id : multiRoomList)
				{
					if(id.length()==0)
						continue;
					R=getRoomObject(httpReq,id);
					if(R==null)
						return "@break@";
					final HTTPRequest mergeReq=RoomData.mergeRoomFields(httpReq, setPairs, R);
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
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final String last=httpReq.getUrlParameter("HOLIDAY");
			if(last==null)
				return " @break@";
			final int dex=CMLib.quests().getHolidayIndex(last);
			if(dex<0)
				return "Holiday '" + last +"' does not exist.";
			final String err=CMLib.quests().deleteHoliday(dex);
			Log.sysOut("Grinder",mob.name()+" deleted holiday "+last);
			return (err.length()>0)?err:"Holiday '"+last+"' deleted.";
		}
		else
		if(parms.containsKey("EDITHOLIDAY"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final String last=httpReq.getUrlParameter("HOLIDAY");
			if(last==null)
				return " @break@";
			final int holidayIndex=CMLib.quests().getHolidayIndex(last);
			final String err=GrinderHolidays.createModifyHoliday(httpReq, parms, last);
			if(holidayIndex<0)
				Log.sysOut("Grinder",mob.name()+" created holiday "+last);
			else
				Log.sysOut("Grinder",mob.name()+" modified holiday "+last);
			return (err.length()>0)?err:"";
		}
		else
		if(parms.containsKey("DELRACE"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			Race R=null;
			final String last=httpReq.getUrlParameter("RACE");
			if(last==null)
				return " @break@";
			R=CMClass.getRace(last);
			if((R==null)||(!R.isGeneric()))
				return " @break@";
			final String oldRID=R.ID();
			CMClass.delRace(R);
			CMLib.database().DBDeleteRace(R.ID());
			CMClass.loadClass(CMObjectType.RACE,"com/planet_ink/coffee_mud/Races/"+oldRID+".class",true);
			Race oldR=CMClass.getRace(oldRID);
			if(oldR==null)
				oldR=CMClass.getRace("StdRace");
			CMLib.utensils().swapRaces(oldR, R);
			Log.sysOut("Grinder",mob.name()+" deleted race "+R.ID());
			return "Race "+R.ID()+" deleted.";
		}
		else
		if(parms.containsKey("EDITRACE"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			Race R=null;
			Race oldR=null;
			final String last=httpReq.getUrlParameter("RACE");
			if(last==null)
				return " @break@";
			R=CMClass.getRace(last);
			boolean create=false;
			if((R==null)||(!R.isGeneric()))
			{
				create=true;
				if(R!=null)
					oldR=R;
				R=(Race)CMClass.getRace("GenRace").copyOf();
				if(R==null)
					return " @break@";
				R.setRacialParms("<RACE><ID>"+last+"</ID><NAME>"+last+"</NAME></RACE>");
			}
			R=R.makeGenRace();
			final String errMsg=GrinderRaces.modifyRace(httpReq, parms, (oldR==null)?R:oldR, R);
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
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			CharClass C=null;
			final String last=httpReq.getUrlParameter("CLASS");
			if(last==null)
				return " @break@";
			C=CMClass.getCharClass(last);
			if((C==null)||(!C.isGeneric()))
				return " @break@";
			final String oldCID=C.ID();
			CMClass.delCharClass(C);
			CMLib.database().DBDeleteClass(C.ID());
			CMClass.loadClass(CMObjectType.CHARCLASS,"com/planet_ink/coffee_mud/CharClasses/"+oldCID+".class",true);
			CharClass oldC=CMClass.getCharClass(oldCID);
			if(oldC==null)
				oldC=CMClass.getCharClass("StdCharClass");
			if((oldC!=null)&&(oldC!=C))
				CMLib.utensils().reloadCharClasses(oldC);
			Log.sysOut("Grinder",mob.name()+" deleted charclass "+C.ID());
			return "CharClass "+C.ID()+" deleted.";
		}
		else
		if(parms.containsKey("EDITCLASS"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			CharClass C=null;
			CharClass oldC=null;
			final String last=httpReq.getUrlParameter("CLASS");
			if(last==null)
				return " @break@";
			C=CMClass.getCharClass(last);
			boolean create=false;
			if((C==null)||(!C.isGeneric()))
			{
				create=true;
				if(C!=null)
					oldC=C;
				C=(CharClass)CMClass.getCharClass("GenCharClass").copyOf();
				if(C==null)
					return " @break@";
				C.setClassParms("<CCLASS><ID>"+last+"</ID><NAME>"+last+"</NAME></CCLASS>");
			}
			C=C.makeGenCharClass();
			final String errMsg=GrinderClasses.modifyCharClass(httpReq, parms, (oldC==null)?C:oldC, C);
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
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			Faction F=null;
			final String last=httpReq.getUrlParameter("FACTION");
			if(last==null)
				return " @break@";
			F=CMLib.factions().getFaction(last);
			if(F==null)
				return " @break@";
			final CMFile F2=new CMFile(Resources.makeFileResourceName(CMLib.factions().makeFactionFilename(F.factionID())),null,CMFile.FLAG_LOGERRORS);
			if(F2.exists())
				F2.deleteAll();
			Log.sysOut("Grinder",mob.Name()+" destroyed Faction "+F.name()+" ("+F.factionID()+").");
			Resources.removeResource(F.factionID());
			F.destroy();
			return "Faction "+F.ID()+" deleted.";
		}
		else
		if(parms.containsKey("EDITFACTION"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			Faction F=null;
			final String last=httpReq.getUrlParameter("FACTION");
			if(last==null)
				return " @break@";
			F=CMLib.factions().getFaction(last);
			//boolean create=false;
			if(F==null)
			{
				//create=true;
				final StringBuffer template=new CMFile(Resources.buildResourcePath("examples")+"factiontemplate.ini",null,CMFile.FLAG_LOGERRORS).text();
				if((template==null)||(template.length()==0))
					return "The file 'resources/examples/factiontemplate.ini' could not be located and is required for command line faction creation.";
				//Resources.submitResource(last,template);
				if(!Resources.saveFileResource("::"+CMLib.factions().makeFactionFilename(last),mob,template))
					return "Unable to save "+Resources.buildResourcePath("")+CMLib.factions().makeFactionFilename(last);
				F=(Faction)CMClass.getCommon("DefaultFaction");
				if(F==null)
					return " @break@";
				F.initializeFaction(template,last);
				CMLib.factions().addFaction(F);
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
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			Ability A=null;
			final String last=httpReq.getUrlParameter("ABILITY");
			if(last==null)
				return " @break@";
			A=CMClass.getAbility(last);
			if((A==null)||(!A.isGeneric()))
				return " @break@";
			final Object O=CMClass.getObjectOrPrototype(A.ID());
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
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			Ability A=null;
			Ability oldA=null;
			final String last=httpReq.getUrlParameter("ABILITY");
			if(last==null)
				return " @break@";
			A=CMClass.getAbility(last);
			oldA=A;
			boolean create=false;
			if((A!=null)&&(!A.isGeneric()))
				return " @break@";
			String type="GenAbility";
			final int code=CMath.s_int(httpReq.getUrlParameter("CLASSIFICATION_ACODE"));
			if(code==Ability.ACODE_LANGUAGE)
				type="GenLanguage";
			if(code==Ability.ACODE_COMMON_SKILL)
				type="GenCraftSkill";
			if(A==null)
			{
				create=true;
				A=(Ability)CMClass.getAbility(type).copyOf();
				if(A==null)
					return " @break@";
				A.setStat("CLASS",last);
			}
			final String errMsg=GrinderAbilities.modifyAbility(httpReq, parms, (oldA==null)?A:oldA, A);
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
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final String player=httpReq.getUrlParameter("PLAYER");
			final MOB playerM=(player!=null)?CMLib.players().getLoadPlayer(player):null;
			final String roomID=httpReq.getUrlParameter("ROOM");
			final Room R=getRoomObject(httpReq,roomID);
			if((R==null)&&(playerM==null)&&((roomID==null)||(!roomID.equalsIgnoreCase("ANY"))))
				return "@break@";
			final String errMsg=GrinderItems.editItem(httpReq,parms,mob,R,playerM);
			httpReq.addFakeUrlParameter("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("EDITMOB"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final String roomID=httpReq.getUrlParameter("ROOM");
			final Room R=getRoomObject(httpReq,roomID);
			if((R==null)&&((roomID==null)||(!roomID.equalsIgnoreCase("ANY"))))
				return "@break@";
			final String errMsg=GrinderMobs.editMob(httpReq,parms,mob,R);
			httpReq.addFakeUrlParameter("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("EDITPLAYER"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final MOB M=CMLib.players().getLoadPlayer(httpReq.getUrlParameter("PLAYER"));
			if(M==null)
				return "@break@";
			final String errMsg=GrinderPlayers.editPlayer(mob,httpReq,parms,M);
			httpReq.addFakeUrlParameter("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("DELROOM"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final String multiFlagStr=httpReq.getUrlParameter("MULTIROOMFLAG");
			final boolean multiFlag=(multiFlagStr!=null)&& multiFlagStr.equalsIgnoreCase("on");
			final List<String> multiRoomList=CMParms.parseSemicolons(httpReq.getUrlParameter("MULTIROOMLIST"),false);
			final List<Room> rooms = new LinkedList<Room>();
			if((multiFlag)&&(multiRoomList.size()>0))
			{
				for(final String id : multiRoomList)
				{
					if(id.length()==0)
						continue;
					final Room R=getRoomObject(httpReq,id);
					if(R==null)
						return "@break@";
					rooms.add(R);
				}
			}
			else
			{
				final Room R=getRoomObject(httpReq,httpReq.getUrlParameter("ROOM"));
				if(R==null)
					return "@break@";
				rooms.add(R);
			}
			String errMsg="";
			for(final Room R : rooms)
			{
				for(int d=0;d<R.rawDoors().length;d++)
				{
					if(R.rawDoors()[d]!=null)
					{
						httpReq.addFakeUrlParameter("ROOM",R.rawDoors()[d].roomID());
						httpReq.addFakeUrlParameter("LINK","");
						break;
					}
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
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final String multiFlagStr=httpReq.getUrlParameter("MULTIROOMFLAG");
			final boolean multiFlag=(multiFlagStr!=null)&& multiFlagStr.equalsIgnoreCase("on");
			final List<String> multiRoomList=CMParms.parseSemicolons(httpReq.getUrlParameter("MULTIROOMLIST"),false);
			final List<Room> rooms = new LinkedList<Room>();
			if((multiFlag)&&(multiRoomList.size()>0))
			{
				for(final String id : multiRoomList)
				{
					if(id.length()==0)
						continue;
					final Room R=getRoomObject(httpReq,id);
					if(R==null)
						return "@break@";
					rooms.add(R);
				}
			}
			else
			{
				final Room R=getRoomObject(httpReq,httpReq.getUrlParameter("ROOM"));
				if(R==null)
					return "@break@";
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
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final String link=httpReq.getUrlParameter("LINK");
			Room R=getRoomObject(httpReq,httpReq.getUrlParameter("ROOM"));
			if(R==null)
				return "@break@";
			final int dir=CMLib.directions().getGoodDirectionCode(link);
			if(dir<0)
				return "@break@";
			final String copyThisOne=httpReq.getUrlParameter("COPYROOM");
			final String errMsg=GrinderRooms.createRoom(R,dir,(copyThisOne!=null)&&(copyThisOne.equalsIgnoreCase("ON")));
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
		if(parms.containsKey("MASSACCOUNTCREATE"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final String dataStr=httpReq.getUrlParameter("ACCOUNTNAMES");
			if((dataStr==null)||(dataStr.length()==0))
				return "No accounts created.";
			final List<String> list = Resources.getFileLineVector(new StringBuffer(dataStr));
			final StringBuffer response=new StringBuffer("");
			int created=0;
			int size=list.size();
			for(final String line : list)
			{
				final List<String> set=CMParms.parseSpaces(line,true);
				if(set.size()!=0)
				{
					final String accountName = CMStrings.capitalizeAndLower(set.get(0));
					if(set.size()>3)
					{
						response.append(L("Error: '@x1' has too much data (extra spaces somewhere). Not created.\n\r<BR>",accountName));
						continue;
					}
					if(CMLib.players().accountExists(accountName))
					{
						response.append(L("Error: '@x1' already exists.\n\r<BR>",accountName));
						continue;
					}
					if(!CMLib.login().isOkName(accountName,false))
					{
						response.append(L("Error: '@x1' is not a valid name.\n\r<BR>",accountName));
						continue;
					}
					final String password;
					final String email;
					if(set.size()==3)
					{
						password=set.get(1);
						email=set.get(2);
					}
					else
					if(set.size()==2)
					{
						if(CMLib.smtp().isValidEmailAddress(set.get(1)))
						{
							email=set.get(1);
							password=CMLib.encoder().generateRandomPassword();
						}
						else
						{
							email="";
							password=set.get(1);
						}
						
					}
					else
					if(set.size()==1)
					{
						email="";
						password=CMLib.encoder().generateRandomPassword();
					}
					else
					{
						response.append(L("Error: '@x1' has too much data (extra spaces somewhere). Not created.\n\r<BR>",accountName));
						continue;
					}
					if((email.length()>0)&&(!CMLib.smtp().isValidEmailAddress(email)))
					{
						response.append(L("Error: '@x1' for account '@x2' is not a valid address. Not created.\n\r<BR>",email,accountName));
						continue;
					}
					
					PlayerAccount thisAcct=(PlayerAccount)CMClass.getCommon("DefaultPlayerAccount");
					thisAcct.setAccountName(accountName);
					thisAcct.setAccountExpiration(0);
					thisAcct.setEmail(email);
					if(CMProps.getBoolVar(CMProps.Bool.ACCOUNTEXPIRATION))
						thisAcct.setAccountExpiration(System.currentTimeMillis()+(1000l*60l*60l*24l*(CMProps.getIntVar(CMProps.Int.TRIALDAYS))));
					thisAcct.setLastDateTime(System.currentTimeMillis());
					thisAcct.setLastUpdated(System.currentTimeMillis());
					thisAcct.setPassword(password);
					CMLib.database().DBCreateAccount(thisAcct);
					CMLib.players().addAccount(thisAcct);
					Log.sysOut("Create",mob.Name()+" mass created account "+thisAcct.getAccountName()+".");
					if(email.length()>0)
					{
						CMLib.smtp().emailOrJournal(CMProps.getVar(CMProps.Str.SMTPSERVERNAME), accountName, "noreply@"+CMProps.getVar(CMProps.Str.MUDDOMAIN).toLowerCase(), email,
								"Password for "+accountName,
								"Your password for "+accountName+" at "+CMProps.getVar(CMProps.Str.MUDDOMAIN)+" is: '"+password+"'.");
						response.append(L("Created: '@x1'\n\r<BR>",accountName));
					}
					else
						response.append(L("Created: '@x1' with password '@x2'\n\r<BR>",accountName,password));
					created++;
				}
			}
			return response.toString()+"\n\r<BR>Created "+created+"/"+size+" accounts.\n\r<BR>";
		}
		else
		if(parms.containsKey("PAINTROOMS"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final String AREA=httpReq.getUrlParameter("AREA");
			if(AREA==null)
				return "@break@";
			final Area A=getAreaObject(AREA);
			if(A==null)
				return "@break@";
			String like=httpReq.getUrlParameter("ROOM");
			final List<String> likeList=CMParms.parseCommas(like,true);
			final Vector<Room> RS=new Vector<Room>();
			for(int l=0;l<likeList.size();l++)
			{
				like=likeList.get(l);
				final String roomID=quickfind(A,like.toUpperCase());
				Room R=null;
				if(roomID!=null)
					R=getRoomObject(httpReq,roomID);
				R=CMLib.map().getRoom(R);
				if(R==null)
					return "The room you entered ("+like+") could not be found.";
				CMLib.map().resetRoom(R);
				final Room likeRoom=(Room)CMLib.map().getRoom(R).copyOf();
				for (int d = Directions.NUM_DIRECTIONS() - 1; d >= 0; d--)
					likeRoom.rawDoors()[d] = null;
				RS.addElement(likeRoom);
			}
			if(RS.size()==0)
				return "You did not specify a room or room list to paint from!";
			final boolean overwrite=httpReq.getUrlParameter("NOVERLAP")!=null
								&&(httpReq.getUrlParameter("NOVERLAP").length()>0);
			final int brushSize=CMath.s_int(httpReq.getUrlParameter("BRUSHSIZE"));
			if(brushSize<=0)
				return "Your brush size should be a number greater than 0!";
			final int[] coords=getAppropriateXY(A,httpReq.getUrlParameter("PAINTAT"));
			if(coords==null)
				return "An error occurred trying to paint.  Relogin and try again.";
			int radius=(int)Math.round(Math.floor(CMath.div(brushSize,2)));
			if(radius<1)
				radius=1;
			coords[4]-=radius;
			coords[5]-=radius;
			if(coords[4]<0)
				coords[4]=0;
			if(coords[5]<0)
				coords[5]=0;
			int endX=coords[4]+brushSize;
			int endY=coords[5]+brushSize;
			if(endX>=((GridZones)A).xGridSize())
				endX=((GridZones)A).xGridSize()-1;
			if(endY>=((GridZones)A).yGridSize())
				endY=((GridZones)A).yGridSize()-1;
			String roomID=null;
			Room R=null;
			if(overwrite)
			for(int x=coords[4];x<=endX;x++)
			{
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
			}
			final RoomnumberSet deferredExitSaves=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
			for(int x=coords[4];x<=endX;x++)
			{
				for(int y=coords[5];y<=endY;y++)
				{
					roomID=gridRoomID(A,x,y);
					if(!A.getProperRoomnumbers().contains(roomID))
					{
						final Room likeRoom=RS.elementAt(CMLib.dice().roll(1,RS.size(),0)-1);
						R=GrinderRooms.createGridRoom(A,roomID,likeRoom,deferredExitSaves,true);
						Log.sysOut("Grinder",mob.Name()+" added room "+R.roomID());
					}
				}
			}
			for(final Enumeration e=deferredExitSaves.getRoomIDs();e.hasMoreElements();)
			{
				R=getRoomObject(httpReq,(String)e.nextElement());
				CMLib.database().DBUpdateExits(R);
			}
			Log.sysOut("Grinder",mob.name()+" updated "+deferredExitSaves.roomCountAllAreas()+" rooms exits.");
			//String errMsg=GrinderRooms.createRoom(R,dir,(copyThisOne!=null)&&(copyThisOne.equalsIgnoreCase("ON")));
		}
		else
		if(parms.containsKey("ADDGRIDROOM"))
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob==null)
				return "@break@";
			final String AREA=httpReq.getUrlParameter("AREA");
			if(AREA==null)
				return "false";
			final Area A=getAreaObject(AREA);
			if(A==null)
				return "@break@";
			final String roomID=httpReq.getUrlParameter("ROOM");
			final String link=httpReq.getUrlParameter("AUTOLINK");
			final Room R=getRoomObject(httpReq,roomID);
			if((roomID==null)||(R!=null)||(roomID.length()==0))
				return "@break@";
			final String copyThisOne=httpReq.getUrlParameter("COPYROOM");
			final Room copyRoom=getRoomObject(httpReq,copyThisOne);
			final Room newRoom=GrinderRooms.createGridRoom(A,roomID,copyRoom,null,((link!=null)&&(link.length()>0)));
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
		final String roomID=A.Name()+"#";
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
			final int[] xyxy=new int[6];
			int x=mapStyle.indexOf('_');
			int index=0;
			while(x>=0)
			{
				final String coord=mapStyle.substring(0,x);
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
		final String AREA=httpReq.getUrlParameter("AREA");
		final Area A=getAreaObject(AREA);
		if(A==null)
			return null;
		if(CMSecurity.isASysOp(mob)||A.amISubOp(mob.Name()))
			return A;
		return null;
	}

	protected String quickfind(Area A, String find)
	{
		Room R=A.getRoom(find);
		if((R!=null)&&(R.roomID().length()>0))
			return R.roomID();
		if(find.startsWith("#"))
		{
			R=A.getRoom(A.Name()+find);
			if((R!=null)&&(R.roomID().length()>0))
				return R.roomID();
		}
		if(CMath.isNumber(find))
		{
			R=A.getRoom(A.Name()+"#"+find);
			if((R!=null)&&(R.roomID().length()>0))
				return R.roomID();
		}
		for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
		{
			R=r.nextElement();
			if((R.roomID().length()>0)&&(R.roomID().toUpperCase().endsWith(find.toUpperCase())))
				return R.roomID();
		}
		for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
		{
			R=r.nextElement();
			if((R.roomID().length()>0)&&(R.displayText().toUpperCase().indexOf(find.toUpperCase())>=0))
				return R.roomID();
		}
		for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
		{
			R=r.nextElement();
			if((R.roomID().length()>0)&&(R.description().toUpperCase().indexOf(find.toUpperCase())>=0))
				return R.roomID();
		}
		R=CMLib.map().getRoom(find);
		if((R!=null)&&(R.roomID().length()>0))
			return R.roomID();
		return null;
	}
}
