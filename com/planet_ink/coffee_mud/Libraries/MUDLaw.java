package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2006-2020 Bo Zimmerman

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
public class MUDLaw extends StdLibrary implements LegalLibrary
{
	@Override
	public String ID()
	{
		return "MUDLaw";
	}

	@Override
	public Law getTheLaw(final Room R, final MOB mob)
	{
		final LegalBehavior B=getLegalBehavior(R);
		if(B!=null)
		{
			final Area A2=getLegalObject(R.getArea());
			return B.legalInfo(A2);
		}
		return null;
	}

	@Override
	public LegalBehavior getLegalBehavior(final Area A)
	{
		if(A==null)
			return null;
		final List<Behavior> V=CMLib.flags().flaggedBehaviors(A,Behavior.FLAG_LEGALBEHAVIOR);
		if(V.size()>0)
			return (LegalBehavior)V.get(0);
		LegalBehavior B=null;
		for(final Enumeration<Area> e=A.getParents();e.hasMoreElements();)
		{
			B=getLegalBehavior(e.nextElement());
			if(B!=null)
				break;
		}
		return B;
	}

	@Override
	public LegalBehavior getLegalBehavior(final Room R)
	{
		if(R==null)
			return null;
		final List<Behavior> V=CMLib.flags().flaggedBehaviors(R,Behavior.FLAG_LEGALBEHAVIOR);
		if(V.size()>0)
			return (LegalBehavior)V.get(0);
		return getLegalBehavior(R.getArea());
	}

	@Override
	public Area getLegalObject(final Area A)
	{
		if(A==null)
			return null;
		final List<Behavior> V=CMLib.flags().flaggedBehaviors(A,Behavior.FLAG_LEGALBEHAVIOR);
		if(V.size()>0)
			return A;
		Area A2=null;
		Area A3=null;
		for(final Enumeration<Area> e=A.getParents();e.hasMoreElements();)
		{
			A2=e.nextElement();
			A3=getLegalObject(A2);
			if(A3!=null)
				return A3;
		}
		return A3;
	}

	@Override
	public Area getLegalObject(final Room R)
	{
		if(R==null)
			return null;
		return getLegalObject(R.getArea());
	}

	@Override
	public boolean isACity(final Area A)
	{
		if((A==null)
		||(A.getAreaIStats()[Area.Stats.COUNTABLE_ROOMS.ordinal()]==0))
			return false;
		return CMath.div(A.getAreaIStats()[Area.Stats.CITY_ROOMS.ordinal()],A.getAreaIStats()[Area.Stats.COUNTABLE_ROOMS.ordinal()]) > .60;
	}

	@Override
	public List<LandTitle> getAllUniqueLandTitles(final Enumeration<Room> e, final String owner, final boolean includeRentals)
	{
		final Vector<LandTitle> V=new Vector<LandTitle>();
		final HashSet<Room> roomsDone=new HashSet<Room>();
		final Set<String> titlesDone = new HashSet<String>();
		Room R=null;
		for(;e.hasMoreElements();)
		{
			R=e.nextElement();
			LandTitle T=getLandTitle(R);
			if((T!=null)
			&&(!V.contains(T))
			&&(!titlesDone.contains(T.getTitleID()))
			&&(includeRentals||(!T.rentalProperty()))
			&&((owner==null)
				||(owner.length()==0)
				||(owner.equals("*")&&(T.getOwnerName().length()>0))
				||(T.getOwnerName().equals(owner))))
			{
				titlesDone.add(T.getTitleID());
				int price = T.getPrice();
				final List<Room> V2=T.getAllTitledRooms();
				for(int v=0;v<V2.size();v++)
				{
					final Room R2=V2.get(v);
					if(R2 != null)
					{
						if(!roomsDone.contains(R2))
						{
							roomsDone.add(R2);
							final LandTitle T2=getLandTitle(R2);
							if((T2 != null)
							&&(T2.getPrice() > price))
							{
								T=T2;
								price=T2.getPrice();
							}
						}
					}
				}
				V.addElement(T); // should be the best taxable title
			}
		}
		return V;
	}

	@Override
	public LandTitle getLandTitle(final Area area)
	{
		if(area==null)
			return null;
		if(area instanceof LandTitle)
			return (LandTitle)area;
		if((area instanceof BoardableShip)
		&&(((BoardableShip)area).getShipItem() instanceof LandTitle))
			return (LandTitle)((BoardableShip)area).getShipItem();
		for(final Enumeration<Ability> a=area.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A instanceof LandTitle))
				return (LandTitle)A;
		}
		return null;
	}

	@Override
	public PrivateProperty getPropertyRecord(final Area area)
	{
		if(area==null)
			return null;
		if(area instanceof PrivateProperty)
			return (PrivateProperty)area;
		for(final Enumeration<Ability> a=area.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A instanceof PrivateProperty))
				return (PrivateProperty)A;
		}
		return null;
	}

	@Override
	public LandTitle getLandTitle(final Room room)
	{
		if(room==null)
			return null;
		final LandTitle title=getLandTitle(room.getArea());
		if(title!=null)
			return title;
		for(final Enumeration<Ability> a=room.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A instanceof LandTitle))
				return (LandTitle)A;
		}
		return null;
	}

	@Override
	public PrivateProperty getPropertyRecord(final Room room)
	{
		if(room==null)
			return null;
		if(room instanceof PrivateProperty)
			return (PrivateProperty)room;
		final PrivateProperty record=getPropertyRecord(room.getArea());
		if(record!=null)
			return record;
		for(final Enumeration<Ability> a=room.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A instanceof PrivateProperty))
				return (PrivateProperty)A;
		}
		return null;
	}

	@Override
	public boolean isHomeRoomUpstairs(final Room room)
	{
		final Room dR=room.getRoomInDir(Directions.DOWN);
		if((dR!=null)
		&&(dR.domainType() != Room.DOMAIN_INDOORS_CAVE)
		&&((dR.getAtmosphere()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_ROCK))
		{
			if(isHomePeerRoom(dR))
				return true;
			final Set<Room> peerRooms=getHomePeersOnThisFloor(room,new HashSet<Room>());
			for(final Room R : peerRooms)
			{
				final Room pdR=R.getRoomInDir(Directions.DOWN);
				if((pdR!=null)
				&&(pdR.domainType() != Room.DOMAIN_INDOORS_CAVE)
				&&((pdR.getAtmosphere()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_ROCK)
				&&(isHomePeerRoom(pdR)))
					return true;
			}
		}
		return false;
	}

	public boolean isHomePeerRoom(final Room R)
	{
		return ifHomePeerLandTitle(R)!=null;
	}

	public LandTitle ifHomePeerLandTitle(final Room R)
	{
		if((R!=null)
		&&(R.ID().length()>0)
		&&(CMath.bset(R.domainType(),Room.INDOORS)))
			return getLandTitle(R);
		return null;
	}

	public LandTitle ifLandTitle(final Room R)
	{
		if((R!=null)
		&&(R.ID().length()>0))
			return getLandTitle(R);
		return null;
	}

	@Override
	public boolean isRoomSimilarlyTitled(final LandTitle title, final Room R)
	{
		final LandTitle ptitle = ifLandTitle(R);
		if(ptitle ==null)
			return false;
		if(ptitle.getOwnerName().length()==0)
		{
			for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
			{
				final Room sideRoom=R.getRoomInDir(d);
				final LandTitle psTitle=ifLandTitle(sideRoom);
				if(psTitle.getOwnerName().equals(title.getOwnerName()))
					return true;
			}
			return false;
		}
		else
			return ptitle.getOwnerName().equals(title.getOwnerName());
	}

	@Override
	public Set<Room> getHomePeersOnThisFloor(final Room room, final Set<Room> doneRooms)
	{
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
		{
			if((d!=Directions.UP)&&(d!=Directions.DOWN)&&(d!=Directions.GATE))
			{
				final Room sideRoom=room.getRoomInDir(d);
				if(isHomePeerRoom(sideRoom)  &&(!doneRooms.contains(sideRoom)))
				{
					doneRooms.add(sideRoom);
					doneRooms.addAll(getHomePeersOnThisFloor(sideRoom, doneRooms));
				}
			}
		}
		return doneRooms;
	}

	@Override
	public boolean isHomeRoomDownstairs(final Room room)
	{
		if(room==null)
			return false;
		if(isHomePeerRoom(room.getRoomInDir(Directions.UP)))
			return true;
		final Set<Room> peerRooms=getHomePeersOnThisFloor(room,new HashSet<Room>());
		for(final Room R : peerRooms)
		{
			if(isHomePeerRoom(R.getRoomInDir(Directions.UP)))
				return true;
		}
		return false;
	}

	@Override
	public boolean doesHavePriviledgesInThisDirection(final MOB mob, final Room room, final Exit exit)
	{
		final int dirCode=CMLib.map().getExitDir(room,exit);
		if(dirCode<0)
			return false;
		final Room otherRoom=room.getRoomInDir(dirCode);
		if(otherRoom==null)
			return false;
		return doesHavePriviledgesHere(mob,otherRoom);
	}

	@Override
	public boolean doesHavePrivilegesWith(final MOB mob, final PrivateProperty record)
	{
		if((record==null)||(mob==null))
			return false;
		if(doesHaveWeakPrivilegesWith(mob,record))
			return true;
		if(doesHaveClanFriendlyPrivilegesHere(mob, record.getOwnerName()))
			return true;
		return false;
	}

	@Override
	public boolean doesHaveWeakPrivilegesWith(final MOB mob, final PrivateProperty record)
	{
		if((record==null)||(mob==null))
			return false;
		if(record.getOwnerName()==null)
			return false;
		if(record.getOwnerName().length()==0)
			return false;
		if(record.getOwnerName().equals(mob.Name()))
			return true;
		if((record.getOwnerName().equals(mob.getLiegeID())&&(mob.isMarriedToLiege())))
			return true;
		final Pair<Clan,Integer> clanRole=mob.getClanRole(record.getOwnerName());
		if(clanRole != null)
			return true;
		return false;
	}

	@Override
	public boolean doesHavePriviledgesHere(final MOB mob, final Room room)
	{
		final PrivateProperty record=getPropertyRecord(room);
		if((record==null)||(mob==null))
			return false;
		if(doesHavePrivilegesWith(mob,record))
			return true;
		if(mob.amFollowing()!=null)
			return doesHavePriviledgesHere(mob.amFollowing(),room);
		return false;
	}

	@Override
	public boolean doesHaveWeakPriviledgesHere(final MOB mob, final Room room)
	{
		final PrivateProperty record=getPropertyRecord(room);
		if((record==null)||(mob==null))
			return false;
		if(doesHaveWeakPrivilegesWith(mob,record))
			return true;
		if(mob.amFollowing()!=null)
			return doesHaveWeakPriviledgesHere(mob.amFollowing(),room);
		return false;
	}

	protected boolean doesHaveClanFriendlyPrivilegesHere(final MOB mob, final String clanID)
	{
		final Clan C=CMLib.clans().getClanExact(clanID);
		if(C!=null)
		{
			final Pair<Clan,Integer> clanRole=mob.getClanRole(C.clanID());
			if((clanRole!=null)&&(clanRole.first.getAuthority(clanRole.second.intValue(), Clan.Function.HOME_PRIVS)!=Clan.Authority.CAN_NOT_DO))
				return true;
			final Clan myC = CMLib.clans().findRivalrousClan(mob);
			if(myC != null)
			{
				final Pair<Clan,Integer> myCRole=mob.getClanRole(myC.clanID());
				if((myCRole!=null)
				&&(myCRole.first.getAuthority(myCRole.second.intValue(), Clan.Function.HOME_PRIVS)!=Clan.Authority.CAN_NOT_DO)
				&&(CMLib.clans().isClanFriendly(mob, C)))
					return true;
			}
		}
		return false;
	}


	@Override
	public boolean doesAnyoneHavePrivilegesHere(final MOB mob, final String overrideID, final Room R)
	{
		if((mob==null)||(R==null))
			return false;
		if((doesHavePriviledgesHere(mob,R))||((overrideID.length()>0)&&(mob.Name().equals(overrideID))))
			return true;
		if(overrideID.length()>0)
		{
			if(doesHaveClanFriendlyPrivilegesHere(mob, overrideID))
				return true;
		}
		for(int i=0;i<R.numInhabitants();i++)
		{
			final MOB M=R.fetchInhabitant(i);
			if(doesHavePriviledgesHere(M,R))
				return true;
			if(overrideID.length()>0)
			{
				if(M.Name().equals(overrideID))
					return true;

				if(M.isMarriedToLiege() && M.getLiegeID().equalsIgnoreCase(overrideID))
					return true;

				final Pair<Clan,Integer> clanPair=M.getClanRole(overrideID);
				if((clanPair!=null)&&(clanPair.first.getAuthority(clanPair.second.intValue(), Clan.Function.HOME_PRIVS)!=Clan.Authority.CAN_NOT_DO))
					return true;
			}
		}
		return false;
	}

	@Override
	public String getPropertyOwnerName(final Room room)
	{
		final PrivateProperty record=getPropertyRecord(room);
		if(record==null)
			return "";
		if(record.getOwnerName()==null)
			return "";
		return record.getOwnerName();
	}

	@Override
	public String getLandOwnerName(final Room room)
	{
		final LandTitle title=getLandTitle(room);
		if(title==null)
			return "";
		if(title.getOwnerName()==null)
			return "";
		return title.getOwnerName();
	}

	@Override
	public boolean isLandOwnable(final Room room)
	{
		final LandTitle title=getLandTitle(room);
		if(title==null)
			return false;
		return true;
	}

	@Override
	public boolean doesOwnThisLand(final String name, final Room room)
	{
		final LandTitle title=getLandTitle(room);
		if(title==null)
			return false;
		if(title.getOwnerName()==null)
			return false;
		if(title.getOwnerName().length()==0)
			return false;
		if(title.getOwnerName().equals(name))
			return true;
		return false;
	}

	@Override
	public boolean doesOwnThisProperty(final String name, final Room room)
	{
		final PrivateProperty record=getPropertyRecord(room);
		if(record==null)
			return false;
		if(record.getOwnerName()==null)
			return false;
		if(record.getOwnerName().length()==0)
			return false;
		if(record.getOwnerName().equals(name))
			return true;
		return false;
	}

	@Override
	public boolean doesOwnThisProperty(final MOB mob, final Room room)
	{
		final PrivateProperty record=getPropertyRecord(room);
		if(record==null)
			return false;
		return doesOwnThisProperty(mob,record);
	}

	@Override
	public boolean doesOwnThisProperty(final MOB mob, final PrivateProperty record)
	{
		if(record.getOwnerName()==null)
			return false;
		if(record.getOwnerName().length()==0)
			return false;
		if(record.getOwnerName().equals(mob.Name()))
			return true;
		if((record.getOwnerName().equals(mob.getLiegeID())&&(mob.isMarriedToLiege())))
			return true;
		final Pair<Clan,Integer> clanRole=mob.getClanRole(record.getOwnerName());
		if((clanRole!=null)&&(clanRole.first.getAuthority(clanRole.second.intValue(),Clan.Function.PROPERTY_OWNER)!=Clan.Authority.CAN_NOT_DO))
			return true;
		if(mob.amFollowing()!=null)
			return doesOwnThisProperty(mob.amFollowing(),record);
		return false;
	}

	@Override
	public MOB getPropertyOwner(final PrivateProperty record)
	{
		if(record.getOwnerName()==null)
			return null;
		if(record.getOwnerName().length()==0)
			return null;
		if(record.getOwnerName().startsWith("#"))
			return null;
		final Clan clan = CMLib.clans().getClanExact(record.getOwnerName());
		if(clan != null)
			return clan.getResponsibleMember();
		final MOB M=CMLib.players().getPlayerAllHosts(record.getOwnerName());
		if(M == null)
			return CMLib.players().getLoadPlayer(record.getOwnerName());
		return M;
	}

	@Override
	public boolean canAttackThisProperty(final MOB mob, final PrivateProperty record)
	{
		if(record.getOwnerName()==null)
			return true;
		if(record.getOwnerName().length()==0)
			return true;
		if(record.getOwnerName().startsWith("#"))
			return true;
		if(record.getOwnerName().equals(mob.Name()))
			return true;
		if((record.getOwnerName().equals(mob.getLiegeID())&&(mob.isMarriedToLiege())))
			return true;
		final Clan clan = CMLib.clans().getClanExact(record.getOwnerName());
		if(clan != null)
		{
			final Pair<Clan,Integer> clanRole=mob.getClanRole(record.getOwnerName());
			if((clanRole!=null)&&(clanRole.first.getAuthority(clanRole.second.intValue(),Clan.Function.PROPERTY_OWNER)!=Clan.Authority.CAN_NOT_DO))
				return true;
			final MOB M=clan.getResponsibleMember();
			if(M!=null)
				return mob.mayIFight(M);
		}
		else
		{
			MOB M=CMLib.players().getPlayerAllHosts(record.getOwnerName());
			if(M == null)
				M=CMLib.players().getLoadPlayer(record.getOwnerName());
			if(M!=null)
				return mob.mayIFight(M);
		}
		return false;
	}

	@Override
	public Ability getClericInfusion(final Physical room)
	{
		if(room==null)
			return null;
		for(final Enumeration<Ability> a=room.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A.ID().startsWith("Prayer_Infuse")))
				return A;
		}
		return null;
	}

	@Override
	public Deity getClericInfused(final Room room)
	{
		final Ability A=getClericInfusion(room);
		if(A==null)
			return null;
		return CMLib.map().getDeity(A.text());
	}

	@Override
	public boolean doesOwnThisLand(final MOB mob, final Room room)
	{
		final PrivateProperty prop=this.getPropertyRecord(room);
		if(prop==null)
			return false;
		final String ownerName = prop.getOwnerName();
		if((ownerName==null)||(ownerName.length()==0))
			return false;
		if(ownerName.equals(mob.Name()))
			return true;
		if((ownerName.equals(mob.getLiegeID())&&(mob.isMarriedToLiege())))
			return true;
		final Pair<Clan,Integer> clanRole=mob.getClanRole(ownerName);
		if((clanRole!=null)&&(clanRole.first.getAuthority(clanRole.second.intValue(),Clan.Function.PROPERTY_OWNER)!=Clan.Authority.CAN_NOT_DO))
			return true;
		if(mob.amFollowing()!=null)
			return doesOwnThisLand(mob.amFollowing(),room);
		return false;
	}

	@Override
	public boolean isLegalOfficerHere(final MOB mob)
	{
		if((mob==null)||(mob.location()==null))
			return false;
		final Area A=this.getLegalObject(mob.location());
		if(A==null)
			return false;
		final LegalBehavior B=this.getLegalBehavior(A);
		if(B==null)
			return false;
		return B.isAnyOfficer(A, mob);
	}

	@Override
	public boolean isLegalJudgeHere(final MOB mob)
	{
		if((mob==null)||(mob.location()==null))
			return false;
		final Area A=this.getLegalObject(mob.location());
		if(A==null)
			return false;
		final LegalBehavior B=this.getLegalBehavior(A);
		if(B==null)
			return false;
		return B.isJudge(A, mob);
	}

	@Override
	public boolean isLegalOfficialHere(final MOB mob)
	{
		return isLegalOfficerHere(mob) || isLegalJudgeHere(mob);
	}

	@Override
	public void colorRoomForSale(Room R, final LandTitle title, final boolean reset)
	{
		synchronized(("SYNC"+R.roomID()).intern())
		{
			R=CMLib.map().getRoom(R);
			final String theStr=CMLib.lang().L(title.rentalProperty()?RENTSTR:SALESTR);
			final String otherStr=CMLib.lang().L(title.rentalProperty()?SALESTR:RENTSTR);
			int x=R.description().indexOf(otherStr);
			while(x>=0)
			{
				R.setDescription(R.description().substring(0,x));
				CMLib.database().DBUpdateRoom(R);
				x=R.description().indexOf(otherStr);
			}
			final String oldDescription=R.description();
			x=R.description().indexOf(theStr.trim());
			final String displayStr =  CMLib.lang().L(CMath.bset(R.domainType(), Room.INDOORS)?INDOORSTR:OUTDOORSTR);
			if((x<0)||(reset&&(!R.displayText().equals(displayStr))))
			{
				if(reset)
				{
					R.setDescription("");
					R.setDisplayText(displayStr);
					x=-1;
				}
				if(x<0)
					R.setDescription(R.description()+theStr);
				else
				if(!reset)
					R.setDescription(R.description().substring(0,x+theStr.trim().length()));
				if(!R.description().equals(oldDescription))
					CMLib.database().DBUpdateRoom(R);
			}
			else
			{
				R.setDescription(R.description().substring(0,x+theStr.trim().length()));
				if(!R.description().equals(oldDescription))
					CMLib.database().DBUpdateRoom(R);
			}
			Item I=R.findItem(null,"$id$");
			if((I==null)||(!I.ID().equals("GenWallpaper")))
			{
				I=CMClass.getItem("GenWallpaper");
				CMLib.flags().setReadable(I,true);
				I.setName(("id"));
				final StringBuilder txt = new StringBuilder("");
				final int size = title.getAllTitledRooms().size();
				txt.append(CMLib.lang().L("This room is @x1.  ",CMLib.map().getExtendedRoomID(R)));
				if(size > 1)
					txt.append(CMLib.lang().L("There are @x1 rooms in this lot.  ",""+size));
				if(!title.allowsExpansionConstruction())
					txt.append(L("The size of this lot is not expandable"));
				else
					txt.append(L("The size of this lot is expandable"));
				if(title.gridLayout())
					txt.append(L(", and rooms are connected in a grid"));
				txt.append(".");
				I.setReadableText(txt.toString());
				I.setDescription(txt.toString());
				R.addItem(I);
				CMLib.database().DBUpdateItems(R);
			}
		}
	}

	protected boolean shopkeeperMobPresent(final Room R)
	{
		if(R==null)
			return false;
		MOB M=null;
		for(int i=0;i<R.numInhabitants();i++)
		{
			M=R.fetchInhabitant(i);
			if((M.getStartRoom()==R)
			&&(M.isMonster())
			&&(CMLib.coffeeShops().getShopKeeper(M)!=null))
				return true;
		}
		return false;
	}

	@Override
	public boolean robberyCheck(final PrivateProperty record, final CMMsg msg, final boolean quiet)
	{
		if(msg.source().isMonster())
			return true;
		if(((msg.targetMinor()==CMMsg.TYP_GET)&&(!msg.isTarget(CMMsg.MASK_INTERMSG)))
		||(msg.targetMinor()==CMMsg.TYP_PUSH)
		||(msg.targetMinor()==CMMsg.TYP_PULL))
		{
			final Room R=msg.source().location();
			if((msg.target() instanceof Item)
			&&(((Item)msg.target()).owner() ==R)
			&&((!(msg.tool() instanceof Item))||(msg.source().isMine(msg.tool())))
			&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
			&&(record.getOwnerName().length()>0)
			&&(R!=null)
			//&&(msg.othersMessage()!=null) // ant train fix. Justify it next time.
			//&&(msg.othersMessage().length()>0)
			&&((msg.tool()==null)||(CMLib.coffeeShops().getShopKeeper(msg.tool()))==null) // getting from a shopkeeper is not stealing...
			&&(!doesHavePriviledgesHere(msg.source(),R)))
			{
				if((!canAttackThisProperty(msg.source(), record))
				&&(!msg.source().isAttributeSet(Attrib.PLAYERKILL)))
				{
					if(!quiet)
						msg.source().tell(L("You either need to turn on your PK flag, or be in a clan war to rob this property."));
					return false;
				}
				final LegalBehavior B=getLegalBehavior(R);
				if(B!=null)
				{
					final MOB D=getPropertyOwner(record);
					if((D!=null)&&(D!=msg.source()))
					{
						// now check for witnesses
						boolean witnessFound=false;
						final List<Room> rooms=new LinkedList<Room>();
						rooms.add(R);
						for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
						{
							final Room R2=R.getRoomInDir(d);
							if(R2!=null)
								rooms.add(R2);
						}
						for(final Room R2 : rooms)
						{
							for(int m=0;m<R2.numInhabitants();m++)
							{
								final MOB M=R2.fetchInhabitant(m);
								if((M!=null)
								&& doesHavePriviledgesHere(M,R) // not a typo
								&&(CMLib.flags().canBeSeenBy(msg.source(), M)))
									witnessFound=true;
							}
						}
						if(witnessFound)
							B.accuse(getLegalObject(R),msg.source(),D,new String[]{"PROPERTYROB","THIEF_ROBBERY"});
					}
				}
				Ability propertyProp=((Item)msg.target()).fetchEffect("Prop_PrivateProperty");
				if(propertyProp==null)
				{
					propertyProp=CMClass.getAbility("Prop_PrivateProperty");
					propertyProp.setMiscText("owner=\""+record.getOwnerName()+"\" expiresec=60");
					((Item)msg.target()).addNonUninvokableEffect(propertyProp);
				}
			}
		}
		return true;
	}

	@Override
	public PrivateProperty getPropertyRecord(final Item item)
	{
		if(item==null)
			return null;
		if(item instanceof PrivateProperty)
			return (PrivateProperty)item;
		if(item.numEffects()==0)
			return null;
		for(final Enumeration<Ability> a=item.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A instanceof PrivateProperty)
				return (PrivateProperty)A;
		}
		return null;
	}

	@Override
	public boolean mayOwnThisItem(final MOB mob, final Item item)
	{
		final PrivateProperty record = getPropertyRecord(item);
		if(record != null)
		{
			if(doesHaveWeakPrivilegesWith(mob,record))
				return true;
			MOB following=mob.amFollowing();
			int ct=100;
			while((following!=null)
			&&(following!=mob)
			&&(--ct>0))
			{
				if(doesHaveWeakPrivilegesWith(following,record))
					return true;
				following=following.amFollowing();
			}
			if(item.owner() instanceof Room)
			{
				final Room R=(Room)item.owner();
				if(doesHavePriviledgesHere(mob,R))
					return true;
			}
			return false;
		}
		if(item.owner() instanceof Room)
		{
			final Room R=(Room)item.owner();
			final PrivateProperty roomRecord = getPropertyRecord(R);
			if(roomRecord != null)
			{
				if(doesHaveWeakPrivilegesWith(mob,roomRecord))
					return true;
				if((roomRecord.getOwnerName()!=null)
				&&(roomRecord.getOwnerName().length()>0))
					return false;
			}
		}
		return true;
	}
}
