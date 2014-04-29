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
   Copyright 2000-2014 Bo Zimmerman

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
	public String ID(){return "MUDLaw";}
	
	public Law getTheLaw(Room R, MOB mob)
	{
		LegalBehavior B=getLegalBehavior(R);
		if(B!=null)
		{
			Area A2=getLegalObject(R.getArea());
			return B.legalInfo(A2);
		}
		return null;
	}
	
	public LegalBehavior getLegalBehavior(Area A)
	{
		if(A==null) return null;
		List<Behavior> V=CMLib.flags().flaggedBehaviors(A,Behavior.FLAG_LEGALBEHAVIOR);
		if(V.size()>0) return (LegalBehavior)V.get(0);
		LegalBehavior B=null;
		for(Enumeration<Area> e=A.getParents();e.hasMoreElements();)
		{
			B=getLegalBehavior(e.nextElement());
			if(B!=null) break;
		}
		return B;
	}
	public LegalBehavior getLegalBehavior(Room R)
	{
		if(R==null) return null;
		List<Behavior> V=CMLib.flags().flaggedBehaviors(R,Behavior.FLAG_LEGALBEHAVIOR);
		if(V.size()>0) return (LegalBehavior)V.get(0);
		return getLegalBehavior(R.getArea());
	}
	public Area getLegalObject(Area A)
	{
		if(A==null) return null;
		List<Behavior> V=CMLib.flags().flaggedBehaviors(A,Behavior.FLAG_LEGALBEHAVIOR);
		if(V.size()>0) return A;
		Area A2=null;
		Area A3=null;
		for(Enumeration<Area> e=A.getParents();e.hasMoreElements();)
		{
			A2=e.nextElement();
			A3=getLegalObject(A2);
			if(A3!=null) return A3;
		}
		return A3;
	}
	public Area getLegalObject(Room R)
	{
		if(R==null) return null;
		return getLegalObject(R.getArea());
	}

	public boolean isACity(Area A)
	{
		int other=0;
		int streets=0;
		int buildings=0;
		Room R=null;
		for(Enumeration<Room> e=A.getCompleteMap();e.hasMoreElements();)
		{
			R=e.nextElement();
			if((R==null)||(R.roomID()==null)||(R.roomID().length()==0)) continue;
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
	
	public List<LandTitle> getAllUniqueTitles(Enumeration<Room> e, String owner, boolean includeRentals)
	{
		Vector<LandTitle> V=new Vector<LandTitle>();
		HashSet<Room> roomsDone=new HashSet<Room>();
		Room R=null;
		for(;e.hasMoreElements();)
		{
			R=e.nextElement();
			LandTitle T=getLandTitle(R);
			if((T!=null)
			&&(!V.contains(T))
			&&(includeRentals||(!T.rentalProperty()))
			&&((owner==null)
				||(owner.length()==0)
				||(owner.equals("*")&&(T.getOwnerName().length()>0))
				||(T.getOwnerName().equals(owner))))
			{
				List<Room> V2=T.getAllTitledRooms();
				boolean proceed=true;
				for(int v=0;v<V2.size();v++)
				{
					Room R2=V2.get(v);
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
	
	public LandTitle getLandTitle(Area area)
	{
		if(area==null) return null;
		for(final Enumeration<Ability> a=area.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A instanceof LandTitle))
				return (LandTitle)A;
		}
		return null;
	}
	public LandTitle getLandTitle(Room room)
	{
		if(room==null) return null;
		LandTitle title=getLandTitle(room.getArea());
		if(title!=null) return title;
		for(final Enumeration<Ability> a=room.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A instanceof LandTitle))
				return (LandTitle)A;
		}
		return null;
	}

	public boolean isHomeRoomUpstairs(Room room)
	{
		if(isHomePeerRoom(room.getRoomInDir(Directions.DOWN)))
			return true;
		Set<Room> peerRooms=getHomePeersOnThisFloor(room,new HashSet<Room>());
		for(Room R : peerRooms)
		{
			if(isHomePeerRoom(R.getRoomInDir(Directions.DOWN)))
				return true;
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
			return CMLib.law().getLandTitle(R);
		return null;
	}

	public LandTitle ifLandTitle(Room R)
	{
		if((R!=null)
		&&(R.ID().length()>0))
			return CMLib.law().getLandTitle(R);
		return null;
	}

	public boolean isRoomSimilarlyTitled(LandTitle title, Room R)
	{
		LandTitle ptitle = ifLandTitle(R);
		if(ptitle ==null) return false;
		if(ptitle.getOwnerName().length()==0)
		{
			for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
			{
				Room sideRoom=R.getRoomInDir(d);
				LandTitle psTitle=ifLandTitle(sideRoom);
				if(psTitle.getOwnerName().equals(title.getOwnerName()))
					return true;
			}
			return false;
		}
		else
			return ptitle.getOwnerName().equals(title.getOwnerName());
	}
	
	public Set<Room> getHomePeersOnThisFloor(Room room, Set<Room> doneRooms)
	{
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
		{
			if((d!=Directions.UP)&&(d!=Directions.DOWN)&&(d!=Directions.GATE))
			{
				Room sideRoom=room.getRoomInDir(d);
				if(isHomePeerRoom(sideRoom)  &&(!doneRooms.contains(sideRoom)))
				{
					doneRooms.add(sideRoom);
					doneRooms.addAll(getHomePeersOnThisFloor(sideRoom, doneRooms));
				}
			}
		}
		return doneRooms;
	}
	
	public boolean isHomeRoomDownstairs(Room room)
	{
		if(isHomePeerRoom(room.getRoomInDir(Directions.UP)))
			return true;
		Set<Room> peerRooms=getHomePeersOnThisFloor(room,new HashSet<Room>());
		for(Room R : peerRooms)
		{
			if(isHomePeerRoom(R.getRoomInDir(Directions.UP)))
				return true;
		}
		return false;
	}
	
	public boolean doesHavePriviledgesInThisDirection(MOB mob, Room room, Exit exit)
	{
		final int dirCode=CMLib.map().getExitDir(room,exit);
		if(dirCode<0) return false;
		Room otherRoom=room.getRoomInDir(dirCode);
		if(otherRoom==null) return false;
		return doesHavePriviledgesHere(mob,otherRoom);
	}
	
	public boolean doesHavePriviledgesHere(MOB mob, Room room)
	{
		LandTitle title=getLandTitle(room);
		if(title==null) return false;
		if(title.getOwnerName()==null) return false;
		if(title.getOwnerName().length()==0) return false;
		if(title.getOwnerName().equals(mob.Name())) return true;
		if((title.getOwnerName().equals(mob.getLiegeID())&&(mob.isMarriedToLiege())))
			return true;
		Pair<Clan,Integer> clanRole=mob.getClanRole(title.getOwnerName());
		if((clanRole!=null)&&(clanRole.first.getAuthority(clanRole.second.intValue(), Clan.Function.HOME_PRIVS)!=Clan.Authority.CAN_NOT_DO))
			return true;
		if(mob.amFollowing()!=null) 
			return doesHavePriviledgesHere(mob.amFollowing(),room);
		return false;
	}
	
	public boolean doesAnyoneHavePrivilegesHere(MOB mob, String overrideID, Room R)
	{
		if((CMLib.law().doesHavePriviledgesHere(mob,R))||((overrideID.length()>0)&&(mob.Name().equals(overrideID))))
			return true;
		if(overrideID.length()>0)
		{
			Pair<Clan,Integer> clanPair=mob.getClanRole(overrideID);
			if((clanPair!=null)&&(clanPair.first.getAuthority(clanPair.second.intValue(), Clan.Function.HOME_PRIVS)!=Clan.Authority.CAN_NOT_DO))
				return true;
		}
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M=R.fetchInhabitant(i);
			if(CMLib.law().doesHavePriviledgesHere(M,R))
				return true;
			if(overrideID.length()>0)
			{
				if(M.Name().equals(overrideID))
					return true;
				Pair<Clan,Integer> clanPair=M.getClanRole(overrideID);
				if((clanPair!=null)&&(clanPair.first.getAuthority(clanPair.second.intValue(), Clan.Function.HOME_PRIVS)!=Clan.Authority.CAN_NOT_DO))
					return true;
			}
		}
		return false;
	}
	
	public String getLandOwnerName(Room room)
	{
		LandTitle title=getLandTitle(room);
		if(title==null) return "";
		if(title.getOwnerName()==null) return "";
		return title.getOwnerName();
	}
	
	public boolean doesOwnThisProperty(String name, Room room)
	{
		LandTitle title=getLandTitle(room);
		if(title==null) return false;
		if(title.getOwnerName()==null) return false;
		if(title.getOwnerName().length()==0) return false;
		if(title.getOwnerName().equals(name)) return true;
		return false;
	}
	
	public Ability getClericInfusion(Physical room)
	{
		if(room==null) return null;
		for(final Enumeration<Ability> a=room.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A.ID().startsWith("Prayer_Infuse")))
				return A;
		}
		return null;
	}
	public Deity getClericInfused(Room room)
	{
		Ability A=getClericInfusion(room);
		if(A==null) return null;
		return CMLib.map().getDeity(A.text());
	}
	
	public boolean doesOwnThisProperty(MOB mob, Room room)
	{
		LandTitle title=getLandTitle(room);
		if(title==null) return false;
		if(title.getOwnerName()==null) return false;
		if(title.getOwnerName().length()==0) return false;
		if(title.getOwnerName().equals(mob.Name())) return true;
		if((title.getOwnerName().equals(mob.getLiegeID())&&(mob.isMarriedToLiege())))
			return true;
		Pair<Clan,Integer> clanRole=mob.getClanRole(title.getOwnerName());
		if((clanRole!=null)&&(clanRole.first.getAuthority(clanRole.second.intValue(),Clan.Function.PROPERTY_OWNER)!=Clan.Authority.CAN_NOT_DO))
			return true;
		if(mob.amFollowing()!=null) 
			return doesOwnThisProperty(mob.amFollowing(),room);
		return false;
	}
	
	public boolean isLegalOfficerHere(MOB mob)
	{
		if((mob==null)||(mob.location()==null)) return false;
		Area A=this.getLegalObject(mob.location());
		if(A==null) return false;
		LegalBehavior B=this.getLegalBehavior(A);
		if(B==null) return false;
		return B.isAnyOfficer(A, mob);
	}
	public boolean isLegalJudgeHere(MOB mob)
	{
		if((mob==null)||(mob.location()==null)) return false;
		Area A=this.getLegalObject(mob.location());
		if(A==null) return false;
		LegalBehavior B=this.getLegalBehavior(A);
		if(B==null) return false;
		return B.isJudge(A, mob);
	}
	public boolean isLegalOfficialHere(MOB mob){ return isLegalOfficerHere(mob)||isLegalJudgeHere(mob);}
}
