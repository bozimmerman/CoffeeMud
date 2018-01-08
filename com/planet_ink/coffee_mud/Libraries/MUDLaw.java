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
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2006-2018 Bo Zimmerman

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
	public Law getTheLaw(Room R, MOB mob)
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
	public LegalBehavior getLegalBehavior(Area A)
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
	public LegalBehavior getLegalBehavior(Room R)
	{
		if(R==null)
			return null;
		final List<Behavior> V=CMLib.flags().flaggedBehaviors(R,Behavior.FLAG_LEGALBEHAVIOR);
		if(V.size()>0)
			return (LegalBehavior)V.get(0);
		return getLegalBehavior(R.getArea());
	}

	@Override
	public Area getLegalObject(Area A)
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
	public Area getLegalObject(Room R)
	{
		if(R==null)
			return null;
		return getLegalObject(R.getArea());
	}

	@Override
	public boolean isACity(Area A)
	{
		int other=0;
		int streets=0;
		int buildings=0;
		Room R=null;
		for(final Enumeration<Room> e=A.getCompleteMap();e.hasMoreElements();)
		{
			R=e.nextElement();
			if((R==null)||(R.roomID()==null)||(R.roomID().length()==0))
				continue;
			if(R.domainType()==Room.DOMAIN_OUTDOORS_CITY)
				streets++;
			else
			if((R.domainType()==Room.DOMAIN_INDOORS_METAL)
			||(R.domainType()==Room.DOMAIN_INDOORS_STONE)
			||(R.domainType()==Room.DOMAIN_INDOORS_WOOD))
				buildings++;
			else
				other++;
		}
		if((streets<(other/2))||((streets+buildings)<other))
			return false;
		return true;
	}

	@Override
	public List<LandTitle> getAllUniqueLandTitles(Enumeration<Room> e, String owner, boolean includeRentals)
	{
		final Vector<LandTitle> V=new Vector<LandTitle>();
		final HashSet<Room> roomsDone=new HashSet<Room>();
		Room R=null;
		for(;e.hasMoreElements();)
		{
			R=e.nextElement();
			final LandTitle T=getLandTitle(R);
			if((T!=null)
			&&(!V.contains(T))
			&&(includeRentals||(!T.rentalProperty()))
			&&((owner==null)
				||(owner.length()==0)
				||(owner.equals("*")&&(T.getOwnerName().length()>0))
				||(T.getOwnerName().equals(owner))))
			{
				final List<Room> V2=T.getAllTitledRooms();
				boolean proceed=true;
				for(int v=0;v<V2.size();v++)
				{
					final Room R2=V2.get(v);
					if(!roomsDone.contains(R2))
						roomsDone.add(R2);
					else
						proceed=false;
				}
				if(proceed)
					V.addElement(T);

			}
		}
		return V;
	}

	@Override
	public LandTitle getLandTitle(Area area)
	{
		if(area==null)
			return null;
		if(area instanceof LandTitle)
			return (LandTitle)area;
		for(final Enumeration<Ability> a=area.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A instanceof LandTitle))
				return (LandTitle)A;
		}
		return null;
	}

	@Override
	public PrivateProperty getPropertyRecord(Area area)
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
	public LandTitle getLandTitle(Room room)
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
	public PrivateProperty getPropertyRecord(Room room)
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
	public boolean isHomeRoomUpstairs(Room room)
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
				&&((pdR.getAtmosphere()&RawMaterial.MATERIAL_ROCK)==0)
				&&(isHomePeerRoom(pdR)))
					return true;
			}
		}
		return false;
	}

	public boolean isHomePeerRoom(Room R)
	{
		return ifHomePeerLandTitle(R)!=null;
	}

	public LandTitle ifHomePeerLandTitle(Room R)
	{
		if((R!=null)
		&&(R.ID().length()>0)
		&&(CMath.bset(R.domainType(),Room.INDOORS)))
			return getLandTitle(R);
		return null;
	}

	public LandTitle ifLandTitle(Room R)
	{
		if((R!=null)
		&&(R.ID().length()>0))
			return getLandTitle(R);
		return null;
	}

	@Override
	public boolean isRoomSimilarlyTitled(LandTitle title, Room R)
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
	public Set<Room> getHomePeersOnThisFloor(Room room, Set<Room> doneRooms)
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
	public boolean isHomeRoomDownstairs(Room room)
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
	public boolean doesHavePriviledgesInThisDirection(MOB mob, Room room, Exit exit)
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
		final Pair<Clan,Integer> clanRole=mob.getClanRole(record.getOwnerName());
		if((clanRole!=null)&&(clanRole.first.getAuthority(clanRole.second.intValue(), Clan.Function.HOME_PRIVS)!=Clan.Authority.CAN_NOT_DO))
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
	public boolean doesHavePriviledgesHere(MOB mob, Room room)
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
	public boolean doesHaveWeakPriviledgesHere(MOB mob, Room room)
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

	@Override
	public boolean doesAnyoneHavePrivilegesHere(MOB mob, String overrideID, Room R)
	{
		if((mob==null)||(R==null))
			return false;
		if((doesHavePriviledgesHere(mob,R))||((overrideID.length()>0)&&(mob.Name().equals(overrideID))))
			return true;
		if(overrideID.length()>0)
		{
			final Pair<Clan,Integer> clanPair=mob.getClanRole(overrideID);
			if((clanPair!=null)&&(clanPair.first.getAuthority(clanPair.second.intValue(), Clan.Function.HOME_PRIVS)!=Clan.Authority.CAN_NOT_DO))
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
				final Pair<Clan,Integer> clanPair=M.getClanRole(overrideID);
				if((clanPair!=null)&&(clanPair.first.getAuthority(clanPair.second.intValue(), Clan.Function.HOME_PRIVS)!=Clan.Authority.CAN_NOT_DO))
					return true;
			}
		}
		return false;
	}

	@Override
	public String getPropertyOwnerName(Room room)
	{
		final PrivateProperty record=getPropertyRecord(room);
		if(record==null)
			return "";
		if(record.getOwnerName()==null)
			return "";
		return record.getOwnerName();
	}

	@Override
	public String getLandOwnerName(Room room)
	{
		final LandTitle title=getLandTitle(room);
		if(title==null)
			return "";
		if(title.getOwnerName()==null)
			return "";
		return title.getOwnerName();
	}

	@Override
	public boolean isLandOwnable(Room room)
	{
		final LandTitle title=getLandTitle(room);
		if(title==null)
			return false;
		return true;
	}

	@Override
	public boolean doesOwnThisLand(String name, Room room)
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
	public boolean doesOwnThisProperty(String name, Room room)
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
	public boolean doesOwnThisProperty(MOB mob, Room room)
	{
		final PrivateProperty record=getPropertyRecord(room);
		if(record==null)
			return false;
		return doesOwnThisProperty(mob,record);
	}

	@Override
	public boolean doesOwnThisProperty(MOB mob, PrivateProperty record)
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
	public MOB getPropertyOwner(PrivateProperty record)
	{
		if(record.getOwnerName()==null)
			return null;
		if(record.getOwnerName().length()==0)
			return null;
		if(record.getOwnerName().startsWith("#"))
			return null;
		final Clan clan = CMLib.clans().getClan(record.getOwnerName());
		if(clan != null)
			return clan.getResponsibleMember();
		return CMLib.players().getLoadPlayer(record.getOwnerName());
	}
	
	@Override
	public boolean canAttackThisProperty(MOB mob, PrivateProperty record)
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
		final Clan clan = CMLib.clans().getClan(record.getOwnerName());
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
			final MOB M=CMLib.players().getLoadPlayer(record.getOwnerName());
			if(M!=null)
				return mob.mayIFight(M);
		}
		return false;
	}

	@Override
	public Ability getClericInfusion(Physical room)
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
	public Deity getClericInfused(Room room)
	{
		final Ability A=getClericInfusion(room);
		if(A==null)
			return null;
		return CMLib.map().getDeity(A.text());
	}

	@Override
	public boolean doesOwnThisLand(MOB mob, Room room)
	{
		final LandTitle title=getLandTitle(room);
		if(title==null)
			return false;
		if(title.getOwnerName()==null)
			return false;
		if(title.getOwnerName().length()==0)
			return false;
		if(title.getOwnerName().equals(mob.Name()))
			return true;
		if((title.getOwnerName().equals(mob.getLiegeID())&&(mob.isMarriedToLiege())))
			return true;
		final Pair<Clan,Integer> clanRole=mob.getClanRole(title.getOwnerName());
		if((clanRole!=null)&&(clanRole.first.getAuthority(clanRole.second.intValue(),Clan.Function.PROPERTY_OWNER)!=Clan.Authority.CAN_NOT_DO))
			return true;
		if(mob.amFollowing()!=null)
			return doesOwnThisLand(mob.amFollowing(),room);
		return false;
	}

	@Override
	public boolean isLegalOfficerHere(MOB mob)
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
	public boolean isLegalJudgeHere(MOB mob)
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
	public boolean isLegalOfficialHere(MOB mob)
	{
		return isLegalOfficerHere(mob) || isLegalJudgeHere(mob);
	}
	
	@Override
	public void colorRoomForSale(Room R, LandTitle title, boolean reset)
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
				StringBuilder txt = new StringBuilder("");
				int size = title.getAllTitledRooms().size();
				txt.append(CMLib.lang().L("This room is @x1.  ",CMLib.map().getExtendedRoomID(R)));
				if(size > 1)
					txt.append(CMLib.lang().L("There are @x1 rooms in this lot.  ",""+size));
				if(!title.allowsExpansionConstruction())
					txt.append(L("The size of this lot is not expandable."));
				I.setReadableText(txt.toString());
				I.setDescription(txt.toString());
				R.addItem(I);
				CMLib.database().DBUpdateItems(R);
			}
		}
	}

	protected boolean shopkeeperMobPresent(Room R)
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
	public boolean robberyCheck(PrivateProperty record, CMMsg msg)
	{
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
			&&(msg.othersMessage()!=null)
			&&(msg.othersMessage().length()>0)
			&&(!shopkeeperMobPresent(R))
			&&(!doesHavePriviledgesHere(msg.source(),R)))
			{
				final LegalBehavior B=getLegalBehavior(R);
				if(B!=null)
				{
					for(int m=0;m<R.numInhabitants();m++)
					{
						final MOB M=R.fetchInhabitant(m);
						if(doesHavePriviledgesHere(M,R))
							return false;
					}
					MOB D=null;
					if(!record.getOwnerName().startsWith("#"))
					{
						final Clan C=CMLib.clans().getClan(record.getOwnerName());
						if(C!=null)
							D=C.getResponsibleMember();
						else
							D=CMLib.players().getLoadPlayer(record.getOwnerName());
					}
					if((D!=null)&&(D!=msg.source()))
						B.accuse(getLegalObject(R),msg.source(),D,new String[]{"PROPERTYROB","THIEF_ROBBERY"});
				}
				Ability propertyProp=((Item)msg.target()).fetchEffect("Prop_PrivateProperty");
				if(propertyProp==null)
				{
					propertyProp=CMClass.getAbility("Prop_PrivateProperty");
					propertyProp.setMiscText("owner=\""+record.getOwnerName()+"\" expiresec=60");
					((Item)msg.target()).addNonUninvokableEffect(propertyProp);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public PrivateProperty getPropertyRecord(final Item item)
	{
		if(item==null)
			return null;
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
			while((following!=null)&&(following!=mob))
			{
				if(doesHaveWeakPrivilegesWith(following,record))
					return true;
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
