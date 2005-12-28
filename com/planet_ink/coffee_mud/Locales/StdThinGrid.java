package com.planet_ink.coffee_mud.Locales;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.smtp.SMTPserver;
import com.planet_ink.coffee_mud.core.*;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2006 Bo Zimmerman

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
public class StdThinGrid extends StdRoom implements GridLocale
{
	public String ID(){return "StdThinGrid";}
	
	public final static long EXPIRATION=1800000;
	
	protected Vector descriptions=new Vector();
	protected Vector displayTexts=new Vector();
	protected Vector gridexits=new Vector();
	
	protected int xsize=5;
	protected int ysize=5;
	protected Exit ox=null;
	
	protected final DVector rooms=new DVector(4);
	protected static boolean tickStarted=false;
    
	public StdThinGrid()
	{
		super();
		myID=getClass().getName().substring(getClass().getName().lastIndexOf('.')+1);
	}

    protected void cloneFix(Room E)
    {
        super.cloneFix(E);
        if(E instanceof StdThinGrid)
        {
            descriptions=(Vector)((StdThinGrid)E).descriptions.clone();
            displayTexts=(Vector)((StdThinGrid)E).displayTexts.clone();
            gridexits=(Vector)((StdThinGrid)E).gridexits.clone();
        }
    }
	public String getChildLocaleID(){return "StdRoom";}

	public int xSize(){return xsize;}
	public int ySize(){return ysize;}
	public void setXSize(int x){ if(x>0)xsize=x; }
	public void setYSize(int y){ if(y>0)ysize=y; }

	
	public void setDescription(String newDescription)
	{
		super.setDescription(newDescription);
		descriptions=new Vector();
		int x=newDescription.indexOf("<P>");
		while(x>=0)
		{
			String s=newDescription.substring(0,x).trim();
			if(s.length()>0) descriptions.addElement(s);
			newDescription=newDescription.substring(x+3).trim();
			x=newDescription.indexOf("<P>");
		}
		if(newDescription.length()>0)
			descriptions.addElement(newDescription);
	}

	public void setDisplayText(String newDisplayText)
	{
		super.setDisplayText(newDisplayText);
		displayTexts=new Vector();
		int x=newDisplayText.indexOf("<P>");
		while(x>=0)
		{
			String s=newDisplayText.substring(0,x).trim();
			if(s.length()>0) displayTexts.addElement(s);
			newDisplayText=newDisplayText.substring(x+3).trim();
			x=newDisplayText.indexOf("<P>");
		}
		if(newDisplayText.length()>0)
			displayTexts.addElement(newDisplayText);
	}

	protected Room getGridRoomIfExists(int x, int y)
	{
		try
		{
			for(int i=0;i<rooms.size();i++)
			{
				if((((Integer)rooms.elementAt(i,2)).intValue()==x)
				&&(((Integer)rooms.elementAt(i,3)).intValue()==y))
				{
				    rooms.setElementAt(i,4,new Long(System.currentTimeMillis()));
					return (Room)rooms.elementAt(i,1);
				}
			}
		}
        catch(Exception e){Log.debugOut("StdThinGrid",e);}
        return null;
	}
	
	protected static boolean cleanRoom(Room R)
	{
		if(R.getGridParent()==null) return true;
		if(R.numInhabitants()>0) return false;
		if(R.numItems()>0) return false;
		for(int a=0;a<R.numEffects();a++)
		{
			Ability A=R.fetchEffect(a);
			if((A!=null)&&(!A.savable()))
				return false;
		}
		return true;
	}
	
	protected Room getMakeSingleGridRoom(int x, int y)
	{
		if((x<0)||(y<0)||(y>=ySize())||(x>=xSize())) 
			return null;
		
		Room R=getGridRoomIfExists(x,y);
		if(R==null)
		{
			R=CMClass.getLocale(getChildLocaleID());
			if(R==null) return null;
			R.setGridParent(this);
			R.setArea(getArea());
			R.setRoomID("");
			R.setDisplayText(displayText());
			R.setDescription(description());
			int c=-1;
			if(displayTexts!=null)
			if(displayTexts.size()>0)
			{
				c=CMLib.dice().roll(1,displayTexts.size(),-1);
				R.setDisplayText((String)displayTexts.elementAt(c));
			}
			if(descriptions!=null)
			if(descriptions.size()>0)
			{
				if((c<0)||(c>descriptions.size())||(descriptions.size()!=displayTexts.size()))
					c=CMLib.dice().roll(1,descriptions.size(),-1);
				R.setDescription((String)descriptions.elementAt(c));
			}

			for(int a=0;a<numEffects();a++)
				R.addEffect((Ability)fetchEffect(a).copyOf());
			for(int b=0;b<numBehaviors();b++)
				R.addBehavior((Behavior)fetchBehavior(b).copyOf());
			rooms.addElement(R,new Integer(x),new Integer(y),new Long(System.currentTimeMillis()));
		}
		return R;
	}
	
	protected void fillExitsOfGridRoom(Room R, int x, int y)
	{
		if((x<0)||(y<0)||(y>=ySize())||(x>=xSize())) 
			return;
        synchronized(R.baseEnvStats())
        {
            int mask=R.baseEnvStats().sensesMask();
            if(CMath.bset(mask,EnvStats.SENSE_ROOMSYNC))
                return;
            R.baseEnvStats().setSensesMask(mask|EnvStats.SENSE_ROOMSYNC);
        }
		
		// the adjacent rooms created by this method should also take
		// into account the possibility that they are on the edge.
		// it does NOT
		if(ox==null) ox=CMClass.getExit("Open");
		Room R2=null;
		if(y>0)
		{
			R2=getMakeSingleGridRoom(x,y-1);
			if(R2!=null)
				linkRoom(R,R2,Directions.NORTH,ox,ox);
		}
		else
		if((rawDoors()[Directions.NORTH]!=null)&&(rawExits()[Directions.NORTH]!=null))
			linkRoom(R,rawDoors()[Directions.NORTH],Directions.NORTH,rawExits()[Directions.NORTH],rawExits()[Directions.NORTH]);
		
		if(x>0)
		{
			R2=getMakeSingleGridRoom(x-1,y);
			if(R2!=null) 
				linkRoom(R,R2,Directions.WEST,ox,ox);
		}
		else
		if((rawDoors()[Directions.WEST]!=null)&&(rawExits()[Directions.WEST]!=null))
			linkRoom(R,rawDoors()[Directions.WEST],Directions.WEST,rawExits()[Directions.WEST],rawExits()[Directions.WEST]);
		if(y<(ySize()-1))
		{
			R2=getMakeSingleGridRoom(x,y+1);
			if(R2!=null) 
				linkRoom(R,R2,Directions.SOUTH,ox,ox);
		}
		else
		if((rawDoors()[Directions.SOUTH]!=null)&&(rawExits()[Directions.SOUTH]!=null))
			linkRoom(R,rawDoors()[Directions.SOUTH],Directions.SOUTH,rawExits()[Directions.SOUTH],rawExits()[Directions.SOUTH]);
		if(x<(xSize()-1))
		{
			R2=getMakeSingleGridRoom(x+1,y);
			if(R2!=null) 
				linkRoom(R,R2,Directions.EAST,ox,ox);
		}
		else
		if((rawDoors()[Directions.EAST]!=null)&&(rawExits()[Directions.EAST]!=null))
			linkRoom(R,rawDoors()[Directions.EAST],Directions.EAST,rawExits()[Directions.EAST],rawExits()[Directions.EAST]);
		
		if(Directions.NORTHEAST<Directions.NUM_DIRECTIONS)
		{
			if((y>0)&&(x>0))
			{
				R2=getMakeSingleGridRoom(x-1,y-1);
				if(R2!=null)
					linkRoom(R,R2,Directions.NORTHWEST,ox,ox);
			}
			else
			if((rawDoors()[Directions.NORTHWEST]!=null)&&(rawExits()[Directions.NORTHWEST]!=null))
				linkRoom(R,rawDoors()[Directions.NORTHWEST],Directions.NORTHWEST,rawExits()[Directions.NORTHWEST],rawExits()[Directions.NORTHWEST]);
			
			if((x>0)&&(y<(ySize()-1)))
			{
				R2=getMakeSingleGridRoom(x-1,y+1);
				if(R2!=null) 
					linkRoom(R,R2,Directions.SOUTHWEST,ox,ox);
			}
			else
			if((rawDoors()[Directions.SOUTHWEST]!=null)&&(rawExits()[Directions.SOUTHWEST]!=null))
				linkRoom(R,rawDoors()[Directions.SOUTHWEST],Directions.SOUTHWEST,rawExits()[Directions.SOUTHWEST],rawExits()[Directions.SOUTHWEST]);
			
			if((x<(xSize()-1))&&(y>0))
			{
				R2=getMakeSingleGridRoom(x+1,y-1);
				if(R2!=null) 
					linkRoom(R,R2,Directions.NORTHEAST,ox,ox);
			}
			else
			if((rawDoors()[Directions.NORTHEAST]!=null)&&(rawExits()[Directions.NORTHEAST]!=null))
				linkRoom(R,rawDoors()[Directions.NORTHEAST],Directions.NORTHEAST,rawExits()[Directions.NORTHEAST],rawExits()[Directions.NORTHEAST]);
			if((x<(xSize()-1))&&(y<(ySize()-1)))
			{
				R2=getMakeSingleGridRoom(x+1,y+1);
				if(R2!=null) 
					linkRoom(R,R2,Directions.SOUTHEAST,ox,ox);
			}
			else
			if((rawDoors()[Directions.SOUTHEAST]!=null)&&(rawExits()[Directions.SOUTHEAST]!=null))
				linkRoom(R,rawDoors()[Directions.SOUTHEAST],Directions.SOUTHEAST,rawExits()[Directions.SOUTHEAST],rawExits()[Directions.SOUTHEAST]);
		}
		
		for(int d=0;d<gridexits.size();d++)
		{
			WorldMap.CrossExit EX=(WorldMap.CrossExit)gridexits.elementAt(d);
			try{
				if((EX.out)&&(EX.x==x)&&(EX.y==y))
					switch(EX.dir)
					{
					case Directions.NORTH:
						if(EX.y==0)
							tryFillInExtraneousExternal(EX,ox,R);
						break;
					case Directions.SOUTH:
						if(EX.y==ySize()-1)
							tryFillInExtraneousExternal(EX,ox,R);
						break;
					case Directions.EAST:
						if(EX.x==xSize()-1)
							tryFillInExtraneousExternal(EX,ox,R);
						break;
					case Directions.WEST:
						if(EX.x==0)
							tryFillInExtraneousExternal(EX,ox,R);
						break;
					case Directions.NORTHEAST:
						if((EX.y==0)&&(EX.x==xSize()-1))
							tryFillInExtraneousExternal(EX,ox,R);
						break;
					case Directions.SOUTHWEST:
						if((EX.y==ySize()-1)&&(EX.x==0))
							tryFillInExtraneousExternal(EX,ox,R);
						break;
					case Directions.NORTHWEST:
						if((EX.y==0)&&(EX.x==0))
							tryFillInExtraneousExternal(EX,ox,R);
						break;
					case Directions.SOUTHEAST:
						if((EX.y==ySize()-1)&&(EX.x==xSize()-1))
							tryFillInExtraneousExternal(EX,ox,R);
						break;
					}
			}catch(Exception e){}
		}
        R.baseEnvStats().setSensesMask(CMath.unsetb(R.baseEnvStats().sensesMask(),EnvStats.SENSE_ROOMSYNC));
	}
	
	public void tryFillInExtraneousExternal(WorldMap.CrossExit EX, Exit ox, Room linkFrom)
	{
		if(EX==null) return;
		Room linkTo=CMLib.map().getRoom(EX.destRoomID);
		if((linkTo!=null)&&(linkTo.getGridParent()!=null)) 
			linkTo=linkTo.getGridParent();
		if((linkTo!=null)&&(linkFrom.rawDoors()[EX.dir]!=linkTo))
		{
			if(ox==null) ox=CMClass.getExit("Open");
			linkFrom.rawDoors()[EX.dir]=linkTo;
			linkFrom.rawExits()[EX.dir]=ox;
		}
	}
	
	protected Room getMakeGridRoom(int x, int y)
	{
		if((x<0)||(y<0)||(y>=ySize())||(x>=xSize())) 
			return null;
		
		startThinTick();
		Room R=getMakeSingleGridRoom(x,y);
		if(R==null) return null;
		fillExitsOfGridRoom(R,x,y);
		return R;
	}
	
	public Vector outerExits(){return (Vector)gridexits.clone();}
	public void delOuterExit(WorldMap.CrossExit x){gridexits.remove(x);}
	public void addOuterExit(WorldMap.CrossExit x){gridexits.addElement(x);}
	
	public Room getAltRoomFrom(Room loc, int direction)
	{
		if((loc==null)||(direction<0))
			return null;
		int opDirection=Directions.getOpDirectionCode(direction);
		
		String roomID=CMLib.map().getExtendedRoomID(loc);
		for(int d=0;d<gridexits.size();d++)
		{
			WorldMap.CrossExit EX=(WorldMap.CrossExit)gridexits.elementAt(d);
			if((!EX.out)
			&&(EX.destRoomID.equalsIgnoreCase(roomID))
			&&(EX.dir==direction)
			&&(EX.x>=0)&&(EX.y>=0)&&(EX.x<ySize())&&(EX.y<ySize()))
				return getMakeGridRoom(EX.x,EX.y);
		}
		
		Room oldLoc=loc;
		if(loc.getGridParent()!=null)
			loc=loc.getGridParent();
		if((oldLoc!=loc)&&(loc instanceof GridLocale))
		{
			int y=((GridLocale)loc).getChildY(oldLoc);
			int x=((GridLocale)loc).getChildX(oldLoc);
			
			if((x>=0)&&(y>=0))
			switch(opDirection)
			{
			case Directions.EAST:
				if((((GridLocale)loc).ySize()==ySize()))
					return getMakeGridRoom(xSize()-1,y);
				break;
			case Directions.WEST:
				if((((GridLocale)loc).ySize()==ySize()))
					return getMakeGridRoom(0,y);
				break;
			case Directions.NORTH:
				if((((GridLocale)loc).xSize()==xSize()))
					return getMakeGridRoom(x,0);
				break;
			case Directions.NORTHWEST:
				return getMakeGridRoom(0,0);
			case Directions.SOUTHEAST:
				return getMakeGridRoom(xSize()-1,ySize()-1);
			case Directions.NORTHEAST:
				return getMakeGridRoom(xSize()-1,0);
			case Directions.SOUTHWEST:
				return getMakeGridRoom(0,ySize()-1);
			case Directions.SOUTH:
				if((((GridLocale)loc).xSize()==xSize()))
					return getMakeGridRoom(x,ySize()-1);
				break;
			}
		}
		int x=0;
		int y=0;
		switch(opDirection)
		{
		case Directions.NORTH:
			x=xSize()/2;
			break;
		case Directions.SOUTH:
			x=xSize()/2;
			y=ySize()-1;
			break;
		case Directions.EAST:
			x=xSize()-1;
			y=ySize()/2;
			break;
		case Directions.WEST:
			y=ySize()/2;
			break;
		case Directions.NORTHWEST:
			x=0;
			y=0;
			break;
		case Directions.NORTHEAST:
			x=xSize()-1;
			y=0;
			break;
		case Directions.SOUTHWEST:
			x=0;
			y=ySize()-1;
			break;
		case Directions.SOUTHEAST:
			x=xSize()-1;
			y=ySize()-1;
			break;
		case Directions.UP:
		case Directions.DOWN:
			x=xSize()/2;
			y=ySize()/2;
			break;
		}
		return getMakeGridRoom(x,y);
	}

	public Vector getAllRooms()
	{
		Vector V=new Vector();
		getRandomChild();
		try
		{
			for(int i=0;i<rooms.size();i++)
				V.addElement(rooms.elementAt(i,1));
		}
        catch(Exception e){Log.debugOut("StdThinGrid",e);}
		return V;
	}
	
	protected Room alternativeLink(Room room, Room defaultRoom, int dir)
	{
		if(room.getGridParent()==this)
		for(int d=0;d<gridexits.size();d++)
		{
			WorldMap.CrossExit EX=(WorldMap.CrossExit)gridexits.elementAt(d);
			try{
				if((EX.out)&&(EX.dir==dir)
				&&(getGridRoomIfExists(EX.x,EX.y)==room))
				{
					Room R=CMLib.map().getRoom(EX.destRoomID);
					if(R!=null)
					{
						if(R.getGridParent()!=null)
							return R.getGridParent();
						return R;
					}
				}
			}catch(Exception e){}
		}
		return defaultRoom;
	}
	
	protected void halfLink(Room room, Room loc, int dirCode, Exit o)
	{
		if(room==null) return;
		if(loc==null) return;
		if(room.rawDoors()[dirCode]!=null)
		{
			if(room.rawDoors()[dirCode].getGridParent()==null)
				return;
			if(room.rawDoors()[dirCode].getGridParent().isMyChild(room.rawDoors()[dirCode]))
				return;
			room.rawDoors()[dirCode]=null;
		}
		if(o==null) o=CMClass.getExit("Open");
		room.rawDoors()[dirCode]=alternativeLink(room,loc,dirCode);
		room.rawExits()[dirCode]=o;
	}

	protected void linkRoom(Room room, Room loc, int dirCode, Exit o, Exit ao)
	{
		if(loc==null) return;
		if(room==null) return;
		int opCode=Directions.getOpDirectionCode(dirCode);
		if(room.rawDoors()[dirCode]!=null)
		{
			if(room.rawDoors()[dirCode].getGridParent()==null)
				return;
			if(room.rawDoors()[dirCode].getGridParent().isMyChild(room.rawDoors()[dirCode]))
				return;
			room.rawDoors()[dirCode]=null;
		}
		if(o==null) o=CMClass.getExit("Open");
		room.rawDoors()[dirCode]=alternativeLink(room,loc,dirCode);
		room.rawExits()[dirCode]=o;
		if(loc.rawDoors()[opCode]!=null)
		{
			if(loc.rawDoors()[opCode].getGridParent()==null)
				return;
			if(loc.rawDoors()[opCode].getGridParent().isMyChild(loc.rawDoors()[opCode]))
				return;
			loc.rawDoors()[opCode]=null;
		}
		if(ao==null) ao=CMClass.getExit("Open");
		loc.rawDoors()[opCode]=alternativeLink(loc,room,opCode);
		loc.rawExits()[opCode]=ao;
	}

	public void buildGrid()
	{
		clearGrid(null);
	}
	
	public boolean isMyChild(Room loc)
	{
        try{return rooms.contains(loc);}catch(Exception e){} // optomization
	    DVector myRooms=rooms.copyOf();
		for(int i=0;i<myRooms.size();i++)
			if(loc==myRooms.elementAt(i,1))
				return true;
		return false;
	}

	protected static void clearRoom(Room room, Room bringBackHere, ThinGridVacuum cleaner)
	{
		room.setGridParent(null);
		if(cleaner!=null) cleaner.tickStatus=Tickable.STATUS_MISC+7;
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
			{
			    if(cleaner!=null) cleaner.tickStatus=Tickable.STATUS_MISC+8;
				bringBackHere.bringMobHere(M,false);
			}
			else
			if((M.getStartRoom()==null)
			||(M.getStartRoom()==room)
			||(M.getStartRoom().ID().length()==0))
			{
			    if(cleaner!=null) cleaner.tickStatus=Tickable.STATUS_MISC+9;
				M.destroy();
			}
			else
			{
			    if(cleaner!=null) cleaner.tickStatus=Tickable.STATUS_MISC+10;
				M.getStartRoom().bringMobHere(M,false);
			}
			if(cleaner!=null) cleaner.tickStatus=Tickable.STATUS_MISC+11;
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
			{
			    if(cleaner!=null) cleaner.tickStatus=Tickable.STATUS_MISC+12;
				bringBackHere.bringItemHere(I,Item.REFUSE_PLAYER_DROP);
			}
			else
			{
			    if(cleaner!=null) cleaner.tickStatus=Tickable.STATUS_MISC+13;
				I.destroy();
			}
			if(cleaner!=null) cleaner.tickStatus=Tickable.STATUS_MISC+14;
		}
		if(cleaner!=null) cleaner.tickStatus=Tickable.STATUS_MISC+15;
		room.clearSky();
		if(cleaner!=null) cleaner.tickStatus=Tickable.STATUS_MISC+16;
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
            if(room.rawDoors()[d]!=null)
            for(int d2=0;d2<Directions.NUM_DIRECTIONS;d2++)
                if(room.rawDoors()[d].rawDoors()[d2]==room)
                {
                    room.rawDoors()[d].rawDoors()[d2]=null;
                    room.rawDoors()[d].rawExits()[d2]=null;
                }
			room.rawDoors()[d]=null;
			room.rawExits()[d]=null;
		}
		if(cleaner!=null) cleaner.tickStatus=Tickable.STATUS_MISC+17;
	}
    
	public void clearGrid(Room bringBackHere)
	{
		try
		{
		    for(int r=0;r<rooms.size();r++)
			{
				Room room=(Room)rooms.elementAt(r,1);
				clearRoom(room,bringBackHere,null);
			}
		    while(rooms.size()>0)
		    {
				Room room=(Room)rooms.elementAt(0,1);
				room.destroy();
                rooms.removeElementAt(0);
		    }
		}
        catch(Exception e){Log.debugOut("StdThinGrid",e);}
	}

	public String getChildCode(Room loc)
	{
		if(roomID().length()==0) return "";
        try{
            int x=rooms.indexOf(loc);
            return roomID()+"#("+((Integer)rooms.elementAt(x,2)).intValue()+","+((Integer)rooms.elementAt(x,3)).intValue()+")";
        }catch(Exception x){}
        DVector rs=rooms.copyOf();
        for(int i=0;i<rs.size();i++)
            if(rs.elementAt(i,1)==loc)
                return roomID()+"#("+((Integer)rs.elementAt(i,2)).intValue()+","+((Integer)rs.elementAt(i,3)).intValue()+")";
        return "";
	}
	public int getChildX(Room loc)
	{
        try{return ((Integer)rooms.elementAt(rooms.indexOf(loc),2)).intValue();}catch(Exception x){}
        DVector rs=rooms.copyOf();
        for(int i=0;i<rs.size();i++)
            if(rs.elementAt(i,1)==loc)
                return ((Integer)rs.elementAt(i,2)).intValue();
        return -1;
	}
	
	public Room getRandomChild()
	{
		int x=CMLib.dice().roll(1,xSize(),-1);
		int y=CMLib.dice().roll(1,ySize(),-1);
		Room R=getMakeGridRoom(x,y);
		if(R==null)
			Log.errOut("StdThinGrid",roomID()+" failed to get a random child!");
		return R;
	}
	
	public int getChildY(Room loc)
	{
        try{return ((Integer)rooms.elementAt(rooms.indexOf(loc),3)).intValue();}catch(Exception x){}
        DVector rs=rooms.copyOf();
        for(int i=0;i<rs.size();i++)
            if(rs.elementAt(i,1)==loc)
                return ((Integer)rs.elementAt(i,3)).intValue();
        return -1;
	}
	
	public Room getChild(String childCode)
	{
		if(childCode.equals(roomID()))
			return this;
		if(!childCode.startsWith(roomID()+"#("))
			return null;
		int len=roomID().length()+2;
		int comma=childCode.indexOf(',',len);
		if(comma<0) return null;
		int x=CMath.s_int(childCode.substring(len,comma));
		int y=CMath.s_int(childCode.substring(comma+1,childCode.length()-1));
		return getMakeGridRoom(x,y);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if(msg.targetMinor()==CMMsg.TYP_ENTER)
		{
			if(msg.target()==this)
			{
				MOB mob=msg.source();
				if((mob.location()!=null)&&(mob.location().roomID().length()>0))
				{
					int direction=-1;
					for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
					{
						if(mob.location().getRoomInDir(d)==this)
							direction=d;
					}
					if(direction<0)
					{
						mob.tell("Some great evil is preventing your movement that way.");
						return false;
					}
					msg.modify(msg.source(),
							  getAltRoomFrom(mob.location(),direction),
							  msg.tool(),
							  msg.sourceCode(),
							  msg.sourceMessage(),
							  msg.targetCode(),
							  msg.targetMessage(),
							  msg.othersCode(),
							  msg.othersMessage());
				}
			}
		}
		return true;
	}
	
	public static synchronized void startThinTick()
	{
		if(tickStarted) 
			return;
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return;
		tickStarted=true;
        
		//please Disable until the roomdestroy problem can be fixed.
		ThinGridVacuum TGV=new ThinGridVacuum();
		CMLib.threads().startTickDown(TGV,Tickable.TICKID_ROOM_BEHAVIOR,450);
	}
	
	protected static class ThinGridVacuum implements Tickable
	{
		public String ID(){return "ThinGridVacuum";}
		public String name(){return ID();}
        public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new ThinGridVacuum();}}
        public CMObject copyOf(){try{return (CMObject)this.clone();}catch(Exception e){return newInstance();}}
        public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
		public long tickStatus=Tickable.STATUS_NOT;
		public long getTickStatus(){return tickStatus;}
		public boolean tick(Tickable ticking, int tickID)
		{
		    tickStatus=Tickable.STATUS_START;
            Vector thinGrids=new Vector();
            Room R=null;
			try
			{
				for(Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
				{
					R=(Room)e.nextElement();
					if(R instanceof StdThinGrid)
                        thinGrids.addElement(R);
                }
            }
            catch(java.util.NoSuchElementException  nse){}
            StdThinGrid STG=null;
            DVector thisGridRooms=null;
            for(Enumeration e=thinGrids.elements();e.hasMoreElements();)
            {
			    STG=(StdThinGrid)e.nextElement();
                thisGridRooms=STG.rooms;
				if(thisGridRooms.size()==0) continue;
			    tickStatus=Tickable.STATUS_ALIVE;
                try
				{
                    Vector roomsToClear=new Vector();
					long time=System.currentTimeMillis()-EXPIRATION;
					for(int r=thisGridRooms.size()-1;r>=0;r--)
                    {
						if(((Long)thisGridRooms.elementAt(r,4)).longValue()<time)
						{
						    tickStatus=Tickable.STATUS_MISC;
							R=(Room)thisGridRooms.elementAt(r,1);
							if(!cleanRoom(R)) continue;
						    tickStatus=Tickable.STATUS_MISC+1;
							boolean cleanOne=true;
							for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
							{
								Room R2=R.rawDoors()[d];
								if((R2!=null)&&(!cleanRoom(R2)))
								{ cleanOne=false; break;}
							}
						    tickStatus=Tickable.STATUS_MISC+2;
							if(cleanOne)
							{
                                thisGridRooms.removeElementAt(r);
                                roomsToClear.addElement(R);
							}
						    tickStatus=Tickable.STATUS_MISC+3;
						}
                    }
                    tickStatus=Tickable.STATUS_MISC+4;
                    Room R2=null;
                    for(Enumeration i=roomsToClear.elements();i.hasMoreElements();)
                    {
                        R=(Room)i.nextElement();
                        tickStatus=Tickable.STATUS_MISC+5;
                        for(int r=thisGridRooms.size()-1;r>=0;r--)
                        {
                            R2=(Room)thisGridRooms.elementAt(r,1);
                            for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
                                if(R2.rawDoors()[d]==R)
                                {
                                    R2.rawDoors()[d]=null;
                                    R2.rawExits()[d]=null;
                                }
                        }
                        tickStatus=Tickable.STATUS_MISC+6;
                        clearRoom(R,null,this);
                        tickStatus=Tickable.STATUS_MISC+7;
                    }
                    tickStatus=Tickable.STATUS_MISC+8;
                    for(Enumeration i=roomsToClear.elements();i.hasMoreElements();)
                    {
                        R=(Room)i.nextElement();
                        tickStatus=Tickable.STATUS_MISC+9;
                        // all of the below is contained in R.destroy()
                        // it was replaced with content for debugging purposes
                        // and will be replaced later.
                        //R.destroy();
                        try{
                            for(int a=R.numEffects()-1;a>=0;a--)
                                R.fetchEffect(a).unInvoke();
                        }catch(Exception e2){}
                        tickStatus=Tickable.STATUS_MISC+10;
                        while(R.numEffects()>0)
                            R.delEffect(R.fetchEffect(0));
                        tickStatus=Tickable.STATUS_MISC+11;
                        try{
                        for(int a=R.numInhabitants()-1;a>=0;a--)
                            R.fetchInhabitant(a).destroy();
                        }catch(Exception e2){}
                        tickStatus=Tickable.STATUS_MISC+12;
                        while(R.numInhabitants()>0)
                            R.delInhabitant(R.fetchInhabitant(0));
                        tickStatus=Tickable.STATUS_MISC+13;
                        while(R.numBehaviors()>0)
                            R.delBehavior(R.fetchBehavior(0));
                        tickStatus=Tickable.STATUS_MISC+13;
                        try{
                        while(R.numItems()>0)
                            R.fetchItem(0).destroy();
                        }catch(Exception e2){}
                        tickStatus=Tickable.STATUS_MISC+13;
                        while(R.numItems()>0)
                            R.delItem(R.fetchItem(0));
                        tickStatus=Tickable.STATUS_MISC+14;
                        if(R instanceof GridLocale)
                            ((GridLocale)R).clearGrid(null);
                        tickStatus=Tickable.STATUS_MISC+15;
                        R.clearSky();
                        tickStatus=Tickable.STATUS_MISC+16; // once locked here
                        CMLib.threads().deleteTick(R,-1);
                        tickStatus=Tickable.STATUS_MISC+17; // show lock here
                        ((StdRoom)R).imageName=null;
                        R.setArea(null);
                        tickStatus=Tickable.STATUS_MISC+18;  
                        ((StdRoom)R).baseEnvStats=(EnvStats)CMClass.getCommon("DefaultEnvStats");
                        ((StdRoom)R).envStats=((StdRoom)R).baseEnvStats;
                        ((StdRoom)R).exits=new Exit[Directions.NUM_DIRECTIONS];
                        ((StdRoom)R).doors=new Room[Directions.NUM_DIRECTIONS];
                        ((StdRoom)R).affects=null;
                        ((StdRoom)R).behaviors=null;
                        ((StdRoom)R).contents=new Vector();
                        ((StdRoom)R).inhabitants=new Vector();
                        ((StdRoom)R).gridParent=null;
                        tickStatus=Tickable.STATUS_MISC+19;
                    }
                    tickStatus=Tickable.STATUS_MISC+20;
				}
                catch(Exception e2){Log.debugOut("StdThinGrid",e2);}
				tickStatus=Tickable.STATUS_MISC+21;
			}
            tickStatus=Tickable.STATUS_MISC+22;
			tickStatus=Tickable.STATUS_NOT;
			return true;
		}
	}
}
