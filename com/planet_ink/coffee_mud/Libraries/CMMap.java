package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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

import java.io.IOException;
import java.util.*;
/*
   Copyright 2000-2007 Bo Zimmerman

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
public class CMMap extends StdLibrary implements WorldMap
{
    
    public String ID(){return "CMMap";}
	public Vector areasList = new Vector();
	//public Vector roomsList = new Vector();
	public Vector playersList = new Vector();
	public Vector deitiesList = new Vector();
    public Vector postOfficeList=new Vector();
    public Vector auctionHouseList=new Vector();
    public Vector bankList=new Vector();
	public Hashtable startRooms=new Hashtable();
	public Hashtable deathRooms=new Hashtable();
	public Hashtable bodyRooms=new Hashtable();
	public final int QUADRANT_WIDTH=10;
	public Vector space=new Vector();
    public Hashtable globalHandlers=new Hashtable();
    public Vector sortedAreas=null;

	// areas
	public int numAreas() { return areasList.size(); }
	public void addArea(Area newOne)
	{
		sortedAreas=null;
		areasList.addElement(newOne);
	}

	public void delArea(Area oneToDel)
	{
		sortedAreas=null;
		areasList.remove(oneToDel);
	}
	
	public Enumeration sortedAreas()
	{
		if(sortedAreas==null)
		{
			Vector V=new Vector();
			Area A=null;
			for(Enumeration e=areas();e.hasMoreElements();)
			{
				A=(Area)e.nextElement();
				String upperName=A.Name().toUpperCase();
				for(int v=0;v<=V.size();v++)
					if(v==V.size())
					{ V.addElement(A); break;}
					else
					if(upperName.compareTo(((Area)V.elementAt(v)).Name().toUpperCase())<=0)
					{ V.insertElementAt(A,v); break;}
			}
			sortedAreas=V;
		}
		return (sortedAreas==null)?sortedAreas():sortedAreas.elements();
	}
	
	public Area getArea(String calledThis)
	{
		for(Enumeration a=areas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
			if(A.Name().equalsIgnoreCase(calledThis))
				return A;
		}
		return null;
	}
    public Area findArea(String calledThis)
    {
        Area A=getArea(calledThis);
        if(A!=null) return A;
        for(Enumeration a=areas();a.hasMoreElements();)
        {
            A=(Area)a.nextElement();
            if(A.Name().toUpperCase().startsWith(calledThis))
                return A;
        }
        return null;
    }
    
	public Enumeration areas()
    {
		return areasList.elements();
	}
	public Enumeration roomIDs(){ return new WorldMap.CompleteRoomIDEnumerator(this);}
	public Area getFirstArea()
	{
		if (areas().hasMoreElements())
			return (Area) areas().nextElement();
		return null;
	}
	public Area getRandomArea()
	{
		Area A=null;
		while((numAreas()>0)&&(A==null))
		{
			try{
				A=(Area)areasList.elementAt(CMLib.dice().roll(1,numAreas(),-1));
            }catch(ArrayIndexOutOfBoundsException e){}
		}
		return A;
	}

    public void addGlobalHandler(Environmental E, int category)
    {
        Vector V=(Vector)globalHandlers.get(new Integer(category));
        if(V==null)
        {
            V=new Vector();
            globalHandlers.put(new Integer(category),V);
        }
        if(!V.contains(E))
            V.add(E);
    }
    
    public void delGlobalHandler(Environmental E, int category)
    {
        Vector V=(Vector)globalHandlers.get(new Integer(category));
        if(V==null) return;
        V.removeElement(E);
    }
    
    public MOB god(Room R){
        MOB everywhereMOB=CMClass.getMOB("StdMOB");
        everywhereMOB.setName("somebody");
        everywhereMOB.setLocation(R);
        return everywhereMOB;
    }

	public boolean isObjectInSpace(SpaceObject O){return space.contains(O);}
	public void delObjectInSpace(SpaceObject O){	space.removeElement(O);}
	public void addObjectToSpace(SpaceObject O){	space.addElement(O);}
	
	public long getDistanceFrom(SpaceObject O1, SpaceObject O2)
	{
		return Math.round(Math.sqrt(CMath.mul((O1.coordinates()[0]-O2.coordinates()[0]),(O1.coordinates()[0]-O2.coordinates()[0]))
									+CMath.mul((O1.coordinates()[1]-O2.coordinates()[1]),(O1.coordinates()[1]-O2.coordinates()[1]))
									+CMath.mul((O1.coordinates()[2]-O2.coordinates()[2]),(O1.coordinates()[2]-O2.coordinates()[2]))));
	}
	public double[] getDirection(SpaceObject FROM, SpaceObject TO)
	{
		double[] dir=new double[2];
		double x=new Long(TO.coordinates()[0]-FROM.coordinates()[0]).doubleValue();
		double y=new Long(TO.coordinates()[1]-FROM.coordinates()[1]).doubleValue();
		double z=new Long(TO.coordinates()[2]-FROM.coordinates()[2]).doubleValue();
		dir[0]=Math.toDegrees(Math.acos(x/Math.sqrt((x*x)+(y*y))));
		dir[1]=Math.toDegrees(Math.acos(z/Math.sqrt((z*z)+(y*y))));
		return dir;
	}
	
	public void moveSpaceObject(SpaceObject O)
	{
		double x1=Math.cos(Math.toRadians(O.direction()[0]))*Math.sin(Math.toRadians(O.direction()[1]));
		double y1=Math.sin(Math.toRadians(O.direction()[0]))*Math.sin(Math.toRadians(O.direction()[1]));
		double z1=Math.cos(O.direction()[1]);
		O.coordinates()[0]=O.coordinates()[0]+Math.round(CMath.mul(O.velocity(),x1));
		O.coordinates()[1]=O.coordinates()[1]+Math.round(CMath.mul(O.velocity(),y1));
		O.coordinates()[2]=O.coordinates()[2]+Math.round(CMath.mul(O.velocity(),z1));
	}
	
	public long getRelativeVelocity(SpaceObject O1, SpaceObject O2)
	{
		return Math.round(Math.sqrt(new Long(((O1.velocity()*O1.coordinates()[0])-(O2.velocity()*O2.coordinates()[0])*(O1.velocity()*O1.coordinates()[0])-(O2.velocity()*O2.coordinates()[0]))
									+((O1.velocity()*O1.coordinates()[1])-(O2.velocity()*O2.coordinates()[1])*(O1.velocity()*O1.coordinates()[1])-(O2.velocity()*O2.coordinates()[1]))
									+((O1.velocity()*O1.coordinates()[2])-(O2.velocity()*O2.coordinates()[2])*(O1.velocity()*O1.coordinates()[2])-(O2.velocity()*O2.coordinates()[2]))).doubleValue()));
	}
	
	public String createNewExit(Room from, Room room, int direction)
	{
		Room opRoom=from.rawDoors()[direction];
		if((opRoom!=null)&&(opRoom.roomID().length()==0))
			opRoom=null;
		Room reverseRoom=null;
		if(opRoom!=null)
			reverseRoom=opRoom.rawDoors()[Directions.getOpDirectionCode(direction)];

		if((reverseRoom!=null)&&(reverseRoom==from))
			return "Opposite room already exists and heads this way.  One-way link created.";

		Exit thisExit=null;
		synchronized(("SYNC"+from.roomID()).intern())
		{
			from=CMLib.map().getRoom(from);
			if(opRoom!=null)
				from.rawDoors()[direction]=null;
	
			from.rawDoors()[direction]=room;
			thisExit=from.rawExits()[direction];
			if(thisExit==null)
			{
				thisExit=CMClass.getExit("StdOpenDoorway");
				from.rawExits()[direction]=thisExit;
			}
			CMLib.database().DBUpdateExits(from);
		}
		synchronized(("SYNC"+room.roomID()).intern())
		{
			room=CMLib.map().getRoom(room);
			if(room.rawDoors()[Directions.getOpDirectionCode(direction)]==null)
			{
				room.rawDoors()[Directions.getOpDirectionCode(direction)]=from;
				room.rawExits()[Directions.getOpDirectionCode(direction)]=thisExit;
				CMLib.database().DBUpdateExits(room);
			}
		}
		return "";
	}

	public int numRooms()
    {
        int total=0;
        for(Enumeration e=areas();e.hasMoreElements();)
            total+=((Area)e.nextElement()).properSize();
        return total;
    }
    
    public boolean sendGlobalMessage(MOB host, int category, CMMsg msg)
    {
        Vector V=(Vector)globalHandlers.get(new Integer(category));
        if(V==null) return true;
        try{
            Environmental E=null;
            for(int v=V.size()-1;v>=0;v--)
            {
                E=(Environmental)V.elementAt(v);
                if(!CMLib.flags().isInTheGame(E,true))
                {
                    if(!CMLib.flags().isInTheGame(E,false))
                        delGlobalHandler(E,category);
                }
                else
                if(!E.okMessage(host,msg))
                    return false;
            }
            for(int v=V.size()-1;v>=0;v--)
            {
                E=(Environmental)V.elementAt(v);
                E.executeMsg(host,msg);
            }
        }
        catch(ArrayIndexOutOfBoundsException x){}
        return true;
    }

	public String getExtendedRoomID(Room R)
	{
		if(R==null) return "";
		if(R.roomID().length()>0) return R.roomID();
		Area A=R.getArea();
		if(A==null) return "";
		GridLocale GR=R.getGridParent();
		if(GR!=null) return GR.getGridChildCode(R);
		return R.roomID();
	}

    public Room getRoom(Vector roomSet, String calledThis)
    {
        try
        {
            if(calledThis==null) return null;
            if(calledThis.endsWith(")"))
            {
                int child=calledThis.lastIndexOf("#(");
                if(child>1)
                {
                    Room R=getRoom(roomSet,calledThis.substring(0,child));
                    if((R!=null)&&(R instanceof GridLocale))
                    {
                        R=((GridLocale)R).getGridChild(calledThis);
                        if(R!=null) return R;
                    }
                }
            }
            Room R=null;
            if(roomSet==null)
            {
                int x=calledThis.indexOf("#");
                if(x>=0)
                {
                    Area A=getArea(calledThis.substring(0,x));
                    if(A!=null) R=A.getRoom(calledThis);
                    if(R!=null) return R;
                }
                for(Enumeration e=areas();e.hasMoreElements();)
                {
                    R = ((Area)e.nextElement()).getRoom(calledThis);
                    if(R!=null) return R;
                }
            }
            else
            for(Enumeration e=roomSet.elements();e.hasMoreElements();)
            {
                R=(Room)e.nextElement();
                if(R.roomID().equalsIgnoreCase(calledThis))
                    return R;
            }
        }
        catch(java.util.NoSuchElementException x){}
        return null;
    }
    
	public Room getRoom(Hashtable hashedRoomSet, String areaName, String calledThis)
	{
		if(calledThis.startsWith("#"))
		{
			if(hashedRoomSet.containsKey(calledThis.substring(1)))
				return (Room)hashedRoomSet.get(calledThis.substring(1));
		}
		else
		if(calledThis.startsWith(areaName+"#"))
		{
			if(hashedRoomSet.containsKey(calledThis.substring(areaName.length()+1)))
				return (Room)hashedRoomSet.get(calledThis.substring(areaName.length()+1));
		}
		else
		{
			if(hashedRoomSet.containsKey(calledThis))
				return (Room)hashedRoomSet.get(calledThis);
		}
		Room R=getRoom(calledThis);
		if(R!=null) return R;
		return getRoom(areaName+"#"+calledThis);
	}

    public Room getRoom(Room room)
    {
    	if(room==null) 
    		return null;
    	if(room.amDestroyed())
    		return getRoom(CMLib.map().getExtendedRoomID(room));
    	return room;
    }
    
	public Room getRoom(String calledThis){ return getRoom(null,calledThis); }
	public Enumeration rooms(){ return new AreaEnumerator(); }
	public Room getRandomRoom()
	{
		Room R=null;
        int numRooms=-1;
		while((R==null)&&((numRooms=numRooms())>0))
		{
			try
            {
				int which=CMLib.dice().roll(1,numRooms,-1);
                int total=0;
                for(Enumeration e=areas();e.hasMoreElements();)
                {
                    Area A=(Area)e.nextElement();
                    if(which<(total+A.properSize()))
                    { R=A.getRandomProperRoom(); break;}
                    total+=A.properSize();
                }
            }catch(NoSuchElementException e){}
		}
		return R;
	}

	public int numDeities() { return deitiesList.size(); }
	public void addDeity(Deity newOne)
	{
		if (!deitiesList.contains(newOne))
			deitiesList.add(newOne);
	}
	public void delDeity(Deity oneToDel)
	{
		deitiesList.remove(oneToDel);
	}
	public Deity getDeity(String calledThis)
	{
		Deity D = null;
		for (Enumeration i=deities(); i.hasMoreElements();)
		{
			D = (Deity)i.nextElement();
			if (D.Name().equalsIgnoreCase(calledThis))
				return D;
		}
		return null;
	}
	public Enumeration deities() { return deitiesList.elements(); }

    public int numPostOffices() { return postOfficeList.size(); }
    public void addPostOffice(PostOffice newOne)
    {
        if (!postOfficeList.contains(newOne))
            postOfficeList.add(newOne);
    }
    public void delPostOffice(PostOffice oneToDel)
    {
        postOfficeList.remove(oneToDel);
    }
    public PostOffice getPostOffice(String chain, String areaNameOrBranch)
    {
        PostOffice P = null;
        for (Enumeration i=postOffices(); i.hasMoreElements();)
        {
            P = (PostOffice)i.nextElement();
            if((P.postalChain().equalsIgnoreCase(chain))
            &&(P.postalBranch().equalsIgnoreCase(areaNameOrBranch)))
                return P;
        }
        Area A=findArea(areaNameOrBranch);
        if(A==null) return null;
        for (Enumeration i=postOffices(); i.hasMoreElements();)
        {
            P = (PostOffice)i.nextElement();
            if((P.postalChain().equalsIgnoreCase(chain))
            &&(CMLib.map().getStartArea(P)==A))
                return P;
        }
        return null;
    }
    public Enumeration postOffices() { return ((Vector)postOfficeList.clone()).elements(); }
    
    public Enumeration auctionHouses() { return ((Vector)auctionHouseList.clone()).elements(); }
    public int numAuctionHouses() { return auctionHouseList.size(); }
    public void addAuctionHouse(Auctioneer newOne)
    {
        if (!auctionHouseList.contains(newOne))
        	auctionHouseList.add(newOne);
    }
    public void delAuctionHouse(Auctioneer oneToDel)
    {
    	auctionHouseList.remove(oneToDel);
    }
    public Auctioneer getAuctionHouse(String chain, String areaNameOrBranch)
    {
    	Auctioneer C = null;
        for (Enumeration i=auctionHouses(); i.hasMoreElements();)
        {
            C = (Auctioneer)i.nextElement();
            if((C.auctionHouse().equalsIgnoreCase(chain))
            &&(C.auctionHouse().equalsIgnoreCase(areaNameOrBranch)))
                return C;
        }
        Area A=findArea(areaNameOrBranch);
        if(A==null) return null;
        for (Enumeration i=auctionHouses(); i.hasMoreElements();)
        {
            C = (Auctioneer)i.nextElement();
            if((C.auctionHouse().equalsIgnoreCase(chain))
            &&(CMLib.map().getStartArea(C)==A))
                return C;
        }
        return null;
    }
    
    public int numBanks() { return bankList.size(); }
    public void addBank(Banker newOne)
    {
        if (!bankList.contains(newOne))
        	bankList.add(newOne);
    }
    public void delBank(Banker oneToDel)
    {
    	bankList.remove(oneToDel);
    }
    public Banker getBank(String chain, String areaNameOrBranch)
    {
    	Banker B = null;
        for (Enumeration i=banks(); i.hasMoreElements();)
        {
            B = (Banker)i.nextElement();
            if((B.bankChain().equalsIgnoreCase(chain))
            &&(B.bankChain().equalsIgnoreCase(areaNameOrBranch)))
                return B;
        }
        Area A=findArea(areaNameOrBranch);
        if(A==null) return null;
        for (Enumeration i=banks(); i.hasMoreElements();)
        {
            B = (Banker)i.nextElement();
            if((B.bankChain().equalsIgnoreCase(chain))
            &&(CMLib.map().getStartArea(B)==A))
                return B;
        }
        return null;
    }
    public Enumeration banks() { return ((Vector)bankList.clone()).elements(); }
	public Iterator bankChains(Area AreaOrNull)
	{
		HashSet H=new HashSet();
		Banker B=null;
		for(Enumeration e=banks();e.hasMoreElements();)
		{
			B=(Banker)e.nextElement();
			if((!H.contains(B.bankChain())) 
			&&((AreaOrNull==null)
				||(getStartArea(B)==AreaOrNull)
				||(AreaOrNull.isChild(getStartArea(B)))))
					H.add(B.bankChain());
		}
		return H.iterator();
	}
	
    
	public int numPlayers() { return playersList.size(); }
	public void addPlayer(MOB newOne) 
	{ 
		synchronized(playersList)
		{
			if(getPlayer(newOne.Name())!=null) return;
			if(playersList.contains(newOne)) return;
			playersList.add(newOne);
		} 
	}
	public void delPlayer(MOB oneToDel) { synchronized(playersList){playersList.remove(oneToDel);} }
	public MOB getPlayer(String calledThis)
	{
		MOB M = null;
		synchronized(playersList)
		{
			for (Enumeration p=players(); p.hasMoreElements();)
			{
				M = (MOB)p.nextElement();
				if (M.Name().equalsIgnoreCase(calledThis))
					return M;
			}
		}
		return null;
	}

	public MOB getLoadPlayer(String last)
	{
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return null;
		MOB M=null;
		synchronized(playersList)
		{
			M=getPlayer(last);
			if(M!=null) return M;
	
			for(Enumeration p=players();p.hasMoreElements();)
			{
				MOB mob2=(MOB)p.nextElement();
				if(mob2.Name().equalsIgnoreCase(last))
				{ return mob2;}
			}
	
			MOB TM=CMClass.getMOB("StdMOB");
			if(CMLib.database().DBUserSearch(TM,last))
			{
				M=CMClass.getMOB("StdMOB");
				M.setName(TM.Name());
				CMLib.database().DBReadPlayer(M);
				CMLib.database().DBReadFollowers(M,false);
				if(M.playerStats()!=null)
					M.playerStats().setUpdated(M.playerStats().lastDateTime());
				M.recoverEnvStats();
				M.recoverCharStats();
			}
	        TM.destroy();
		}
		return M;
	}

	public Enumeration players() { return ((Vector)playersList.clone()).elements(); }

    
	public Room getDefaultStartRoom(MOB mob)
	{
		String race=mob.baseCharStats().getMyRace().racialCategory().toUpperCase();
		race.replace(' ','_');
		String charClass=mob.baseCharStats().getCurrentClass().ID().toUpperCase();
		charClass.replace(' ','_');
		String realrace=mob.baseCharStats().getMyRace().ID().toUpperCase();
		realrace.replace(' ','_');
		String deity=mob.getWorshipCharID().toUpperCase();
		deity.replace(' ','_');
		String align=CMLib.flags().getAlignmentName(mob);
		String roomID=(String)startRooms.get(race);
		if((roomID==null)||(roomID.length()==0))
			roomID=(String)startRooms.get(realrace);
		if(((roomID==null)||(roomID.length()==0)))
			roomID=(String)startRooms.get(align);
		if(((roomID==null)||(roomID.length()==0)))
			roomID=(String)startRooms.get(charClass);
		if(((roomID==null)||(roomID.length()==0)))
		{
		    Vector V=mob.fetchFactionRanges();
		    for(int v=0;v<V.size();v++)
			    if(startRooms.containsKey(((String)V.elementAt(v)).toUpperCase()))
			    { roomID=(String)startRooms.get(((String)V.elementAt(v)).toUpperCase()); break;}
		}
		if(((roomID==null)||(roomID.length()==0))&&(deity.length()>0))
			roomID=(String)startRooms.get(deity);
		if((roomID==null)||(roomID.length()==0))
			roomID=(String)startRooms.get("ALL");

		Room room=null;
		if((roomID!=null)&&(roomID.length()>0))
			room=getRoom(roomID);
		if(room==null)
			room=getRoom("START");
		if((room==null)&&(numRooms()>0))
			room=(Room)rooms().nextElement();
		return room;
	}

	public Room getDefaultDeathRoom(MOB mob)
	{
		String charClass=mob.baseCharStats().getCurrentClass().ID().toUpperCase();
		charClass.replace(' ','_');
		String race=mob.baseCharStats().getMyRace().racialCategory().toUpperCase();
		race.replace(' ','_');
		String deity=mob.getWorshipCharID().toUpperCase();
		deity.replace(' ','_');
		String align=CMLib.flags().getAlignmentName(mob);
		String roomID=(String)deathRooms.get(race);
		if(((roomID==null)||(roomID.length()==0)))
			roomID=(String)deathRooms.get(align);
		if(((roomID==null)||(roomID.length()==0)))
			roomID=(String)deathRooms.get(charClass);
		if(((roomID==null)||(roomID.length()==0)))
		{
		    Vector V=mob.fetchFactionRanges();
		    for(int v=0;v<V.size();v++)
			    if(deathRooms.containsKey(((String)V.elementAt(v)).toUpperCase()))
			    { roomID=(String)deathRooms.get(((String)V.elementAt(v)).toUpperCase()); break;}
		}
		if(((roomID==null)||(roomID.length()==0))&&(deity.length()>0))
			roomID=(String)deathRooms.get(deity);
		if((roomID==null)||(roomID.length()==0))
			roomID=(String)deathRooms.get("ALL");

		Room room=null;
		if((roomID!=null)&&(roomID.equalsIgnoreCase("START")))
			room=mob.getStartRoom();
		if((room==null)&&(roomID!=null)&&(roomID.length()>0))
			room=getRoom(roomID);
		if(room==null)
			room=mob.getStartRoom();
		if((room==null)&&(numRooms()>0))
			room=(Room)rooms().nextElement();
		return room;
	}

	public Room getDefaultBodyRoom(MOB mob)
	{
	    if((mob.getClanID().length()>0)
	    &&(mob.getClanRole()!=Clan.POS_APPLICANT)
	    &&((!mob.isMonster())||(mob.getStartRoom()==null)))
	    {
	    	Clan C=CMLib.clans().getClan(mob.getClanID());
		    if((C!=null)&&(C.getMorgue().length()>0))
		    {
		        Room room=getRoom(C.getMorgue());
		        if((room!=null)&&(CMLib.law().doesHavePriviledgesHere(mob,room)))
		            return room;
		    }
	    }
		String charClass=mob.baseCharStats().getCurrentClass().ID().toUpperCase();
		charClass.replace(' ','_');
		String race=mob.baseCharStats().getMyRace().racialCategory().toUpperCase();
		race.replace(' ','_');
		String realrace=mob.baseCharStats().getMyRace().ID().toUpperCase();
		realrace.replace(' ','_');
		String deity=mob.getWorshipCharID().toUpperCase();
		deity.replace(' ','_');
		String align=CMLib.flags().getAlignmentName(mob);
		String roomID=(String)bodyRooms.get(race);
		if((roomID==null)||(roomID.length()==0))
			roomID=(String)bodyRooms.get(realrace);
		if(((roomID==null)||(roomID.length()==0)))
			roomID=(String)bodyRooms.get(align);
		if(((roomID==null)||(roomID.length()==0)))
			roomID=(String)bodyRooms.get(charClass);
		if(((roomID==null)||(roomID.length()==0)))
		{
		    Vector V=mob.fetchFactionRanges();
		    for(int v=0;v<V.size();v++)
			    if(bodyRooms.containsKey(((String)V.elementAt(v)).toUpperCase()))
			    { roomID=(String)bodyRooms.get(((String)V.elementAt(v)).toUpperCase()); break;}
		}
		if(((roomID==null)||(roomID.length()==0))&&(deity.length()>0))
			roomID=(String)bodyRooms.get(deity);
		if((roomID==null)||(roomID.length()==0))
			roomID=(String)bodyRooms.get("ALL");

		Room room=null;
		if((roomID!=null)&&(roomID.equalsIgnoreCase("START")))
			room=mob.location();
		if((room==null)&&(roomID!=null)&&(roomID.length()>0))
			room=getRoom(roomID);
		if(room==null)
			room=mob.location();
		if((room==null)&&(numRooms()>0))
			room=(Room)rooms().nextElement();
		return room;
	}

	public void pageRooms(CMProps page, Hashtable table, String start)
	{
		for(Enumeration i=page.keys();i.hasMoreElements();)
		{
			String k=(String)i.nextElement();
			if(k.startsWith(start+"_"))
				table.put(k.substring(start.length()+1),page.getProperty(k));
		}
		String thisOne=page.getProperty(start);
		if((thisOne!=null)&&(thisOne.length()>0))
			table.put("ALL",thisOne);
	}

	public void initStartRooms(CMProps page)
	{
		startRooms=new Hashtable();
		pageRooms(page,startRooms,"START");
	}

	public void initDeathRooms(CMProps page)
	{
		deathRooms=new Hashtable();
		pageRooms(page,deathRooms,"DEATH");
	}

	public void initBodyRooms(CMProps page)
	{
		bodyRooms=new Hashtable();
		pageRooms(page,bodyRooms,"MORGUE");
	}

	public void renameRooms(Area A, String oldName, Vector allMyDamnRooms)
	{
		Vector onesToRenumber=new Vector();
		for(int r=0;r<allMyDamnRooms.size();r++)
		{
			Room R=(Room)allMyDamnRooms.elementAt(r);
			synchronized(("SYNC"+R.roomID()).intern())
			{
				R=CMLib.map().getRoom(R);
				R.setArea(A);
				if(oldName!=null)
				{
					if(R.roomID().toUpperCase().startsWith(oldName.toUpperCase()+"#"))
					{
						Room R2=getRoom(A.Name()+"#"+R.roomID().substring(oldName.length()+1));
						if((R2==null)||(!R2.roomID().startsWith(A.Name()+"#")))
						{
							String oldID=R.roomID();
							R.setRoomID(A.Name()+"#"+R.roomID().substring(oldName.length()+1));
							CMLib.database().DBReCreate(R,oldID);
						}
						else
							onesToRenumber.addElement(R);
					}
					else
						CMLib.database().DBUpdateRoom(R);
				}
			}
		}
		if(oldName!=null)
		{
			for(int r=0;r<onesToRenumber.size();r++)
			{
				Room R=(Room)onesToRenumber.elementAt(r);
				String oldID=R.roomID();
				R.setRoomID(A.getNewRoomID(R,-1));
				CMLib.database().DBReCreate(R,oldID);
			}
		}
	}

	public void unLoad()
	{
		areasList.clear();
		deitiesList.clear();
		playersList.clear();
		space=new Vector();
		bodyRooms=new Hashtable();
		startRooms=new Hashtable();
		deathRooms=new Hashtable();
        globalHandlers.clear();
	}

    public int getRoomDir(Room from, Room to)
    {
    	if((from==null)||(to==null)) return -1;
    	for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
    		if(from.getRoomInDir(d)==to)
    			return d;
    	return -1;
    }
    
    public Room findConnectingRoom(Room room)
    {
    	if(room==null) return null;
    	Room R=null;
    	Vector otherChoices=new Vector();
    	for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
    	{
    		R=room.getRoomInDir(d);
    		if(R!=null)
    	    	for(int d1=0;d1<Directions.NUM_DIRECTIONS;d1++)
    	    		if(R.getRoomInDir(d1)==room)
    	    		{
    	    			if(R.getArea()==room.getArea())
    	    				return R;
    	    			otherChoices.addElement(R);
    	    		}
    	}
    	for(Enumeration e=rooms();e.hasMoreElements();)
    	{
    		R=(Room)e.nextElement();
    		if(R==room) continue;
	    	for(int d1=0;d1<Directions.NUM_DIRECTIONS;d1++)
	    		if(R.getRoomInDir(d1)==room)
	    		{
	    			if(R.getArea()==room.getArea())
	    				return R;
	    			otherChoices.addElement(R);
	    		}
    	}
    	if(otherChoices.size()>0)
    		return (Room)otherChoices.firstElement();
    	return null;
    }
    
	public boolean isClearableRoom(Room R)
	{
		if((R==null)||(R.amDestroyed())) return true;
		MOB M=null;
		Room sR=null;
		for(int i=0;i<R.numInhabitants();i++)
		{
			M=R.fetchInhabitant(i);
			if(M==null) continue;
			sR=M.getStartRoom();
			if((sR!=null)
			&&(!sR.roomID().equals(R.roomID())))
				return false;
		}
		Item I=null;
		for(int i=0;i<R.numItems();i++)
		{
			I=R.fetchItem(i);
			if((I!=null)&&(I.expirationDate()!=0))
				return false;
		}
		for(int a=0;a<R.numEffects();a++)
		{
			Ability A=R.fetchEffect(a);
			if((A!=null)&&(!A.savable()))
				return false;
		}
		return true;
	}
    
    public boolean explored(Room R, Vector areas)
    {
        if((R==null) 
        ||(CMath.bset(R.envStats().sensesMask(),EnvStats.SENSE_ROOMUNEXPLORABLE))
        ||(R.getArea()==null))
            return false;
        return false;
    }
    public static class AreaEnumerator implements Enumeration
    {
        private Enumeration curAreaEnumeration=null;
        private Enumeration curRoomEnumeration=null;
        
        public boolean hasMoreElements()
        {
            if(curAreaEnumeration==null) curAreaEnumeration=CMLib.map().areas();
            while((curRoomEnumeration==null)||(!curRoomEnumeration.hasMoreElements()))
            {
                if(!curAreaEnumeration.hasMoreElements()) return false;
                curRoomEnumeration=((Area)curAreaEnumeration.nextElement()).getProperMap();
            }
            return curRoomEnumeration.hasMoreElements();
        }
        public Object nextElement()
        {
            if(curAreaEnumeration==null) curAreaEnumeration=CMLib.map().areas();
            while((curRoomEnumeration==null)||(!curRoomEnumeration.hasMoreElements()))
            {
                if(!curAreaEnumeration.hasMoreElements()) return null;
                curRoomEnumeration=((Area)curAreaEnumeration.nextElement()).getProperMap();
            }
            return curRoomEnumeration.nextElement();
        }
    }
    
	public void obliterateRoom(Room deadRoom)
	{
		for(int a=deadRoom.numEffects()-1;a>=0;a--)
		{
			Ability A=deadRoom.fetchEffect(a);
			if(A!=null)
			{
				A.unInvoke();
				deadRoom.delEffect(A);
			}
		}
		try
		{
			for(Enumeration r=rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				synchronized(("SYNC"+R.roomID()).intern())
				{
					R=CMLib.map().getRoom(R);
					boolean changes=false;
					for(int dir=0;dir<Directions.NUM_DIRECTIONS;dir++)
					{
						Room thatRoom=R.rawDoors()[dir];
						if(thatRoom==deadRoom)
						{
							R.rawDoors()[dir]=null;
							changes=true;
							if((R.rawExits()[dir]!=null)&&(R.rawExits()[dir].isGeneric()))
							{
								Exit GE=R.rawExits()[dir];
								GE.setTemporaryDoorLink(deadRoom.roomID());
							}
						}
					}
					if(changes)
						CMLib.database().DBUpdateExits(R);
				}
			}
	    }catch(NoSuchElementException e){}
	    for(int m=deadRoom.numInhabitants()-1;m>=0;m--)
	    {
	    	MOB M=deadRoom.fetchInhabitant(m);
	    	if((M!=null)&&(M.playerStats()!=null))
				M.getStartRoom().bringMobHere(M,true);
	    }
		emptyRoom(deadRoom,null);
		deadRoom.destroy();
		if(deadRoom instanceof GridLocale)
			((GridLocale)deadRoom).clearGrid(null);
		CMLib.database().DBDeleteRoom(deadRoom);
	}

	public Room roomLocation(Environmental E)
	{
		if(E==null) return null;
		if(E instanceof Room)
			return (Room)E;
		else
		if(E instanceof MOB)
			return ((MOB)E).location();
		else
		if((E instanceof Item)&&(((Item)E).owner() instanceof Room))
			return (Room)((Item)E).owner();
		else
		if((E instanceof Item)&&(((Item)E).owner() instanceof MOB))
		   return ((MOB)((Item)E).owner()).location();
        else
        if(E instanceof Ability)
            return roomLocation(((Ability)E).affecting());
		return null;
	}
	public Area getStartArea(Environmental E)
	{
		if(E instanceof Area) return (Area)E;
		Room R=getStartRoom(E);
		if(R==null) return null;
		return R.getArea();
	}
	
    public Room getStartRoom(Environmental E)
    {
        if(E ==null) return null;
        if(E instanceof MOB) 
        {
        	return ((MOB)E).getStartRoom();
        }
        if(E instanceof Item)
        {
            if(((Item)E).owner() instanceof MOB)
            	return getStartRoom(((Item)E).owner());
            if(CMLib.flags().isGettable((Item)E))
                return null;
        }
        if(E instanceof Ability)
            return getStartRoom(((Ability)E).affecting());
        if(E instanceof Area) return ((Area)E).getRandomProperRoom();
        return roomLocation(E);
    }

    public Area areaLocation(Object E)
    {
        if(E==null) return null;
        if(E instanceof Area)
            return (Area)E;
        else
        if(E instanceof Room)
            return ((Room)E).getArea();
        else
        if(E instanceof MOB)
            return ((MOB)E).location().getArea();
        else
        if((E instanceof Item)&&(((Item)E).owner() instanceof Room))
            return ((Room)((Item)E).owner()).getArea();
        else
        if((E instanceof Item)&&(((Item)E).owner() instanceof MOB))
           return (((MOB)((Item)E).owner()).location()).getArea();
        return null;
    }
    
	public void emptyRoom(Room room, Room bringBackHere)
	{
		if(room==null) return;
		Vector inhabs=new Vector();
		MOB M=null;
		for(int m=0;m<room.numInhabitants();m++)
		{
		    M=room.fetchInhabitant(m);
		    if(M!=null) inhabs.addElement(M);
		}
		for(int m=0;m<inhabs.size();m++)
		{
			M=(MOB)inhabs.elementAt(m);
			if(bringBackHere!=null)
				bringBackHere.bringMobHere(M,false);
			else
			if(!M.savable())
				continue;
			else
			if((M.getStartRoom()==null)
			||(M.getStartRoom()==room)
			||(M.getStartRoom().ID().length()==0))
				M.destroy();
			else
				M.getStartRoom().bringMobHere(M,false);
		}
		Item I=null;
		inhabs.clear();
		for(int i=0;i<room.numItems();i++)
		{
		    I=room.fetchItem(i);
		    if(I!=null) inhabs.addElement(I);
		}
		for(int i=0;i<inhabs.size();i++)
		{
			I=(Item)inhabs.elementAt(i);
			if(bringBackHere!=null)
				bringBackHere.bringItemHere(I,Item.REFUSE_PLAYER_DROP,false);
			else
				I.destroy();
		}
		room.clearSky();
		CMLib.threads().clearDebri(room,0);
	}


	public void obliterateArea(String areaName)
	{
		Area A=getArea(areaName);
		if(A==null) return;
		Enumeration e=A.getCompleteMap();
		Vector rooms=new Vector();
		Room R=null;
		while(e.hasMoreElements())
		{
			for(int i=0;(i<100)&&e.hasMoreElements();i++)
			{
				R=(Room)e.nextElement();
				rooms.addElement(R);
			}
			while(rooms.size()>0)
			{
				R=(Room)rooms.elementAt(0);
				rooms.removeElementAt(0);
				if((R!=null)&&(R.roomID().length()>0))
					obliterateRoom(R);
			}
			e=A.getCompleteMap();
		}
		CMLib.database().DBDeleteArea(A);
		delArea(A);
	}

	public CMMsg resetMsg=null;
	public void resetRoom(Room room)
	{
		if(room==null) return;
		if(room.roomID().length()==0) return;
		synchronized(("SYNC"+room.roomID()).intern())
		{
			room=getRoom(room);
			boolean mobile=room.getMobility();
			room.toggleMobility(false);
			if(resetMsg==null) resetMsg=CMClass.getMsg(CMClass.sampleMOB(),room,CMMsg.MSG_ROOMRESET,null);
			resetMsg.setTarget(room);
			room.executeMsg(room,resetMsg);
			emptyRoom(room,null);
	        Ability A=null;
	        for(int a=room.numEffects()-1;a>=0;a--)
	        {
	            A=room.fetchEffect(a);
	            if((A!=null)&&(A.canBeUninvoked()))
	                A.unInvoke();
	        }
			CMLib.database().DBReadContent(room,null);
			room.toggleMobility(mobile);
		}
	}
    
    public void resetArea(Area area)
    {
        int oldFlag=area.getAreaFlags();
        area.setAreaFlags(Area.FLAG_FROZEN);
        for(Enumeration r=area.getProperMap();r.hasMoreElements();)
            resetRoom((Room)r.nextElement());
        area.fillInAreaRooms();
        area.setAreaFlags(oldFlag);
    }
    
	public void obliteratePlayer(MOB deadMOB, boolean quiet)
	{
		if(getPlayer(deadMOB.Name())!=null)
		{
		   deadMOB=getPlayer(deadMOB.Name());
		   delPlayer(deadMOB);
		}
		for(int s=0;s<CMLib.sessions().size();s++)
		{
			Session S=CMLib.sessions().elementAt(s);
			if((!S.killFlag())&&(S.mob()!=null)&&(S.mob().Name().equals(deadMOB.Name())))
			   deadMOB=S.mob();
		}
		CMMsg msg=CMClass.getMsg(deadMOB,null,CMMsg.MSG_RETIRE,(quiet)?null:"A horrible death cry is heard throughout the land.");
		if(deadMOB.location()!=null)
			deadMOB.location().send(deadMOB,msg);
		try
		{
			for(Enumeration r=rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if((R!=null)&&(R!=deadMOB.location()))
				{
					if(R.okMessage(deadMOB,msg))
						R.sendOthers(deadMOB,msg);
					else
					{
						addPlayer(deadMOB);
						return;
					}
				}
			}
	    }catch(NoSuchElementException e){}
		StringBuffer newNoPurge=new StringBuffer("");
		Vector protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
        boolean somethingDone=false;
		if((protectedOnes!=null)&&(protectedOnes.size()>0))
		{
			for(int b=0;b<protectedOnes.size();b++)
			{
				String B=(String)protectedOnes.elementAt(b);
				if(!B.equalsIgnoreCase(deadMOB.name()))
					newNoPurge.append(B+"\n");
                else
                    somethingDone=true;
			}
            if(somethingDone)
            {
    			Resources.updateResource("protectedplayers.ini",newNoPurge);
    			Resources.saveFileResource("protectedplayers.ini");		
            }
		}
		
		CMLib.database().DBDeleteMOB(deadMOB);
		if(deadMOB.session()!=null)
			deadMOB.session().setKillFlag(true);
		Log.sysOut("Scoring",deadMOB.name()+" has been deleted.");
        deadMOB.destroy();
	}

	public boolean hasASky(Room room)
	{
		if((room==null)
		||(room.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		||((room.domainType()&Room.INDOORS)>0))
			return false;
		return true;
	}
}
