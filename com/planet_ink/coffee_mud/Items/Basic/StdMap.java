package com.planet_ink.coffee_mud.Items.Basic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class StdMap extends StdItem implements com.planet_ink.coffee_mud.interfaces.Map
{
	public String ID(){	return "StdMap";}
	protected StringBuffer[][] myMap=null;
	protected int oldLevel=0;

	public StdMap()
	{
		super();
		setName("a map");
		setDisplayText("a map sits here");
		setDescription("Looks like a map of some place.");
		baseEnvStats().setSensesMask(EnvStats.SENSE_ITEMREADABLE);
		baseGoldValue=10;
		material=EnvResource.RESOURCE_PAPER;
		recoverEnvStats();
	}



	protected class MapRoom
	{
		Room r=null;
		int x=0;
		int y=0;
		boolean positionedAlready=false;
	}

	public String getMapArea(){return miscText;}
	public void setMapArea(String mapName)
	{ super.setMiscText(mapName);	}

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		doMapArea();
	}
	public void doMapArea()
	{
		Vector V=Util.parseSemicolons(getMapArea(),true);
		String newName="";
		for(int v=0;v<V.size();v++)
		{
			String areaName=(String)V.elementAt(v);
			if(areaName.length()>0)
			{
				if(newName.length()==0)
					newName="a map of ";
				else
				if(v==V.size()-1)
					newName+=", and ";
				else
					newName+=", ";
				newName+=areaName.trim();
			}
		}
		this.setName(newName+".");
		this.setDescription("Looks like "+newName+".");
		myMap=null;
	}

    public MapRoom[][] rebuildGrid(Hashtable areaMap)
    {
	    // build grid!
	    int xoffset=0;
	    int yoffset=0;
	    for(Enumeration e=areaMap.elements();e.hasMoreElements();)
		{
			MapRoom mr=(MapRoom)e.nextElement();
	        if(mr.x<xoffset) xoffset=mr.x;
	        if(mr.y<yoffset) yoffset=mr.y;
		}

	    xoffset=xoffset*-1;
	    yoffset=yoffset*-1;

	    int Xbound=0;
	    int Ybound=0;
	    for(Enumeration e=areaMap.elements();e.hasMoreElements();)
		{
			MapRoom room=(MapRoom)e.nextElement();
	        room.x=room.x+xoffset;
	        if(room.x>Xbound)
	            Xbound=room.x;
	        room.y=room.y+yoffset;
	        if(room.y>Ybound)
	            Ybound=room.y;
	    }
	    MapRoom[][] grid=new MapRoom[Xbound+1][Ybound+1];
	    for(Enumeration e=areaMap.elements();e.hasMoreElements();)
		{
			MapRoom room=(MapRoom)e.nextElement();
	        grid[room.x][room.y]=room;
	    }


		int numStragglers=0;
		int somethingsLeft=0;
		// now clear out stragglers.
	    for(int x=0;x<grid.length;x++)
			for(int y=0;y<grid[0].length;y++)
			{
			    MapRoom room=grid[x][y];
				if(room!=null)
				{
					if(
					  ((x>0)&&(grid[x-1][y]!=null))
					||((y>0)&&(grid[x][y-1]!=null))
					||((x>0)&&(y>0)&&(grid[x-1][y-1]!=null))
					||((y<(grid[0].length-1))&&(grid[x][y+1]!=null))
					||((x<(grid.length-1))&&(grid[x+1][y]!=null))
					||((y<(grid[0].length-1))&&(x<(grid.length-1))&&(grid[x+1][y+1]!=null))
					   )
						somethingsLeft++;
					else
					{
						room.x=0;
						room.y=0;
						areaMap.remove(room.r);
						grid[x][y]=null;
						numStragglers++;
					}
				}
			}
		if((numStragglers>0)&&(somethingsLeft>0))
			return rebuildGrid(areaMap);

		return grid;
    }

	public void clearTheSkys(Hashtable mapRooms)
	{
		for(Enumeration e=mapRooms.keys();e.hasMoreElements();)
		{
			Room R=(Room)e.nextElement();
			Room UP=R.rawDoors()[Directions.UP];
			if((UP!=null)
			&&(UP.roomID().length()==0)
			&&(UP instanceof GridLocale))
			{
				Vector V=((GridLocale)UP).getAllRooms();
				for(int v=0;v<V.size();v++)
					mapRooms.remove(V.elementAt(v));
			}
			Room DOWN=R.rawDoors()[Directions.DOWN];
			if((DOWN!=null)
			&&(DOWN.roomID().length()==0)
			&&(DOWN instanceof GridLocale))
			{
				Vector V=((GridLocale)DOWN).getAllRooms();
				for(int v=0;v<V.size();v++)
					mapRooms.remove(V.elementAt(v));
			}
		}
	}

	public Hashtable makeMapRooms()
	{
		Vector mapAreas=Util.parseSemicolons(getMapArea(),true);
		Hashtable mapRooms=new Hashtable();
		for(int a=0;a<mapAreas.size();a++)
		{
			Area A=CMMap.getArea((String)mapAreas.elementAt(a));
			if(A!=null)
			for(Enumeration r=A.getProperMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				MapRoom mr=new MapRoom();
				mr.r=R;
				mapRooms.put(R,mr);
			}
		}
		clearTheSkys(mapRooms);
		return mapRooms;
	}


	public StringBuffer[][] finishMapMaking()
	{
		Hashtable mapRooms=makeMapRooms();
		StringBuffer[][] map=new StringBuffer[0][0];
		if(mapRooms.size()>0)
		{
			placeRooms(mapRooms);
			MapRoom[][] grid=rebuildGrid(mapRooms);
			if((grid.length==0)||(grid[0].length==0))
				return map;
			int ysize=grid[0].length/5;
			int xsize=grid.length/9;
			map=new StringBuffer[xsize+1][ysize+1];
			for(int y=0;y<grid[0].length;y++)
			{
				int ycoord=y/5;
				int lastX=-1;
				String line1="";
				String line2="";
				String line3="";
				String line4="";
				String line5="";
				String line6="";
				int xcoord=-1;
				for(int x=0;x<grid.length;x++)
				{
					xcoord=x/9;
					if(xcoord!=lastX)
					{
						if(lastX>=0)
						{
							if(map[lastX][ycoord]==null)
								map[lastX][ycoord]=new StringBuffer("");
							map[lastX][ycoord].append(line1+"\n\r"+line2+"\n\r"+line3+"\n\r"+line4+"\n\r"+line5+"\n\r"+line6+"\n\r");
						}
						lastX=xcoord;
						line1="";
						line2="";
						line3="";
						line4="";
						line5="";
						line6="";
					}
					MapRoom room=grid[x][y];
					if(room==null)
					{
						line1+="        ";
						line2+="        ";
						line3+="        ";
						line4+="        ";
						line5+="        ";
						line6+="        ";
					}
					else
					{
						String paddedName=Util.padRight(room.r.displayText().trim(),30);
						line1+="---"+dirChar(Directions.NORTH,grid,x,y,'-')+"----";
						line2+="!"+paddedName.substring(0,6)+"!";
						line3+=dirChar(Directions.WEST,grid,x,y,'!')+paddedName.substring(6,12)+dirChar(Directions.EAST,grid,x,y,'!');
						line4+="!"+paddedName.substring(12,18)+"!";
						line5+="!"+paddedName.substring(18,24)+"!";
						line6+="---"+dirChar(Directions.SOUTH,grid,x,y,'-')+"----";
					}
				}
				if(xcoord>=0)
				{
					if(map[xcoord][ycoord]==null)
						map[xcoord][ycoord]=new StringBuffer("");
					map[xcoord][ycoord].append(line1+"\n\r"+line2+"\n\r"+line3+"\n\r"+line4+"\n\r"+line5+"\n\r"+line6+"\n\r");
				}
			}
		}
		for(int x=0;x<map.length;x++)
			for(int y=0;y<map[x].length;y++)
				if(map[x][y]==null) map[x][y]=new StringBuffer("");
		return map;
	}


	public StringBuffer[][] getMyMappedRoom()
	{
		if(oldLevel!=envStats().level())
		{
			myMap=null;
			oldLevel=envStats().level();
		}

		if(myMap!=null)
			return myMap;

		Object o=Resources.getResource("map"+envStats().level()+":"+getMapArea());
		if((o!=null)&&(o instanceof StringBuffer[][]))
			myMap=(StringBuffer[][])o;

		if(myMap!=null)
			return myMap;
		myMap=finishMapMaking();
		Resources.submitResource("map"+envStats().level()+":"+getMapArea(),myMap);
		return myMap;
	}


	public char dirChar(int dirCode, MapRoom[][] grid, int x, int y, char wall)
	{
		MapRoom room=grid[x][y];
		if(room==null)
			return ' ';
		switch(dirCode)
		{
		case Directions.NORTH:
			y=y-1;
			break;
		case Directions.SOUTH:
			y=y+1;
			break;
		case Directions.WEST:
			x=x-1;
			break;
		case Directions.EAST:
			x=x+1;
			break;
		default:
		}
		MapRoom nextRoom=null;
		try
		{	nextRoom=grid[x][y];	}
		catch(Throwable t){}

		if(nextRoom==null)
		{
			if((room.r!=null)&&(room.r.getRoomInDir(dirCode)==null))
				return wall;
			return ' ';
		}
		dirCode=-1;
		for(int d=0;d<Directions.NUM_DIRECTIONS-1;d++)
		{
			if(room.r.getRoomInDir(d)==nextRoom.r)
			{
				dirCode=d;
				break;
			}
		}
		if(dirCode<0)
		{
			if((room.r!=null)&&(room.r.getRoomInDir(dirCode)==null))
				return wall;
			return ' ';
		}
		switch(dirCode)
		{
		case Directions.NORTH:
			return '^';
		case Directions.SOUTH:
			return 'v';
		case Directions.EAST:
			return '>';
		case Directions.WEST:
			return '<';
		case Directions.UP:
			return 'U';
		case Directions.DOWN:
			return 'D';
		}
		return ' ';
	}


    public MapRoom getProcessedRoomAt(Hashtable processed, int x, int y)
    {
        for(Enumeration e=processed.elements();e.hasMoreElements();)
        {
            MapRoom room=(MapRoom)e.nextElement();
            if((room.x==x)&&(room.y==y))
                return room;
        }
        return null;
    }

    public MapRoom getRoom(Hashtable allRooms, Room droom)
    {
		return (MapRoom)allRooms.get(droom);
    }

    public final static int CLUSTERSIZE=3;

    public boolean isEmptyCluster(Hashtable processed, int x, int y)
    {
        for(Enumeration e=processed.elements();e.hasMoreElements();)
        {
            MapRoom room=(MapRoom)e.nextElement();
            if((((room.x>x-CLUSTERSIZE)&&(room.x<x+CLUSTERSIZE))
            &&((room.y>y-CLUSTERSIZE)&&(room.y<y+CLUSTERSIZE)))
            ||((room.x==x)&&(room.y==y)))
                return false;
        }
        return true;
    }

    public void findEmptyCluster(Hashtable processed, Vector XY)
    {
        int x=((Integer)XY.elementAt(0)).intValue();
        int y=((Integer)XY.elementAt(1)).intValue();
        int spacing=CLUSTERSIZE;
        while(true)
        {
            for(int i=0;i<8;i++)
            {
                int yadjust=0;
                int xadjust=0;
                switch(i)
                {
                    case 0: xadjust=1; break;
                    case 1: xadjust=1;yadjust=1; break;
                    case 2: yadjust=1; break;
                    case 3: xadjust=1;xadjust=-1; break;
                    case 4: xadjust=-1; break;
                    case 5: xadjust=-1;yadjust=-1; break;
                    case 6: yadjust=-1; break;
                    case 7: yadjust=-1;xadjust=1; break;
                }
                if(isEmptyCluster(processed,x+(spacing*xadjust),y+(spacing*yadjust)))
                {
                    XY.setElementAt(new Integer(x+(spacing*xadjust)),0);
                    XY.setElementAt(new Integer(y+(spacing*yadjust)),1);
                    return;
                }
            }
            spacing+=1;
        }
    }

    public boolean anythingThatDirection(MapRoom room, int direction)
    {
        Room D=room.r.getRoomInDir(direction);
        if(D==null)
            return false;
        return true;
    }

	public boolean okToPlace(MapRoom room)
	{
		if(room==null) return false;

		if(room.r.domainType()==Room.DOMAIN_OUTDOORS_AIR)
			return false;

		if((envStats().level()<1)&&((room.r.domainType()&Room.INDOORS)==Room.INDOORS))
			return false;

		boolean ok=false;
		for(int e=0;e<Directions.NUM_DIRECTIONS-1;e++)
		{
			Exit exit=room.r.getExitInDir(e);
			if(exit!=null)
			{
				if(envStats().level()<2)
				{
					if((!Sense.isHidden(exit))&&(!Sense.isInvisible(exit))&&(!exit.defaultsLocked()))
						ok=true;
				}
				else
					ok=true;
			}
		}
		return ok;
	}

	public boolean okToPlace(MapRoom room, Exit exit)
	{
		if(!okToPlace(room)) return false;
		if(exit==null) return false;
		if(envStats().level()<2)
		{
			if((Sense.isHidden(exit)||Sense.isInvisible(exit)))
			   return false;
			if(exit.defaultsLocked())
				return false;
		}
		return true;
	}

    public void placeRooms(Hashtable areaMap)
    {
        if(areaMap==null) return;
        if(areaMap.size()==0) return;

        for(Enumeration e=areaMap.elements();e.hasMoreElements();)
        {
            MapRoom room=(MapRoom)e.nextElement();
            room.x=0;
            room.y=0;
            for(int d=0;d<Directions.NUM_DIRECTIONS-1;d++)
            {
                Room dir=room.r.getRoomInDir(d);
                if(dir!=null)
				{
					MapRoom rm=getRoom(areaMap,dir);
					if(rm!=null)
	                    rm.positionedAlready=false;
				}
            }
        }

        Hashtable processed=new Hashtable();
        boolean doneSomething=true;
        while((areaMap.size()>processed.size())&&(doneSomething))
        {
            doneSomething=false;
            for(Enumeration e=areaMap.elements();e.hasMoreElements();)
            {
                MapRoom room=(MapRoom)e.nextElement();
                if((processed.get(room.r)==null)&&(okToPlace(room)))
                {
                    placeRoom(room,areaMap,0,0,processed,true,true,0);
                    doneSomething=true;
                }
            }
        }
    }

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		MOB mob=msg.source();
		if(!msg.amITarget(this))
			super.executeMsg(myHost,msg);
		else
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_READSOMETHING:
			if(Sense.canBeSeenBy(this,mob))
			{
				StringBuffer map[][]=getMyMappedRoom();
				if((Sense.isReadable(this))
				&&(map!=null)
				&&(map.length>0)
				&&(map[0].length>0)
				&&(!mob.isMonster()))
				{
					int x=0;
					int y=0;
					String sec="A0";
					if((msg.targetMessage().length()>0)&&(!msg.targetMessage().startsWith("<S-NAME>")))
					{
						sec="";
						boolean trans=false;
						for(int i=0;i<msg.targetMessage().length();i++)
						{
							char c=msg.targetMessage().charAt(i);
							if(Character.isDigit(c))
							{
								trans=true;
								y=(y*10)+Util.s_int(""+c);
							}
							else
							if(Character.isLetter(c)&&(!trans))
							{
								x=(x*10)+(Character.toUpperCase(c)-'A');
								sec=sec+Character.toUpperCase(c);
							}
						}
						sec+=y;
					}
					if((x>=map.length)||(y>=map[0].length))
					{
						x=0;
						y=0;
						sec="A0";
					}
					if((map.length>1)||(map[0].length>1))
						if(name().length()>0)
							mob.session().rawPrintln("Section: "+sec);
					mob.session().rawPrint(map[x][y].toString());
					if((map.length>1)||(map[0].length>1))
					{
						String letsec="A";
						if(map.length>25)
							for(int l=0;l<map.length/26;l++)
								letsec+='Z';
						letsec=letsec.substring(0,letsec.length()-1)+((char)((('A')+map.length%26)-1));
						if(name().length()>0)
							mob.session().rawPrintln("("+sec+") Use 'READ SEC MAPNAME' to read sections A0 through "+letsec+(map[0].length-1)+" (A-"+letsec+", 0-"+(map[0].length-1)+").");
					}
				}
				else
					mob.tell(name()+" appears to be blank.");
			}
			else
				mob.tell("You can't see that!");
			return;
		}
		super.executeMsg(myHost,msg);
	}

	public void placeRoom(MapRoom room,
						  Hashtable areaMap,
                          int favoredX,
                          int favoredY,
                          Hashtable processed,
                          boolean doNotDefer,
						  boolean passTwo,
						  int depth)
    {
        if(room==null) return;
        if(depth>500) return;
        MapRoom anythingAt=getProcessedRoomAt(processed,favoredX,favoredY);
        if(anythingAt!=null)
        {
            // maybe someone else will take care of it?
            if(!doNotDefer)
                for(Enumeration e=areaMap.elements();e.hasMoreElements();)
                {
                    MapRoom roomToBlame=(MapRoom)e.nextElement();
                    if(roomToBlame!=room)
                        for(int rd=0;rd<Directions.NUM_DIRECTIONS;rd++)
                        {
                            Room RD=roomToBlame.r.getRoomInDir(rd);
                            if((RD!=null)&&(RD==room.r))
							{
								MapRoom MR=getRoom(areaMap,RD);
								if((MR!=null)&&(!MR.positionedAlready))
									return;
							}
                        }
                }
            // nope; nobody can.  It's up to this!
            Vector XY=new Vector();
            XY.addElement(new Integer(0));
            XY.addElement(new Integer(0));
            findEmptyCluster(processed,XY);
            room.x=((Integer)XY.elementAt(0)).intValue();
            room.y=((Integer)XY.elementAt(1)).intValue();
        }
        else
        {
            room.x=favoredX;
            room.y=favoredY;
        }

        // once done, is never undone.  A room is
        // considered processed only once!
        processed.put(room.r,room);

        for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
        {
            MapRoom nextRoom=null;
            if(room.r.getRoomInDir(d)!=null)
                nextRoom=getRoom(areaMap,room.r.getRoomInDir(d));

            if((nextRoom!=null)
			&&(processed.get(nextRoom.r)==null)
            &&(passTwo||((d!=Directions.UP)&&(d!=Directions.DOWN))))
            {
                int newFavoredX=room.x;
                int newFavoredY=room.y;
                switch(d)
                {
                    case Directions.NORTH:
                        newFavoredY--; break;
                    case Directions.SOUTH:
                        newFavoredY++; break;
                    case Directions.EAST:
                        newFavoredX++; break;
                    case Directions.WEST:
                        newFavoredX--; break;
                    case Directions.UP:
                        if(!anythingThatDirection(room,Directions.NORTH))
                            newFavoredY--;
                        else
                        if(!anythingThatDirection(room,Directions.WEST))
                            newFavoredX--;
                        else
                        if(!anythingThatDirection(room,Directions.EAST))
                            newFavoredX++;
                        else
                        if(!anythingThatDirection(room,Directions.SOUTH))
                            newFavoredY++;
                        break;
                    case Directions.DOWN:
                        if(!anythingThatDirection(room,Directions.SOUTH))
                            newFavoredY++;
                        else
                        if(!anythingThatDirection(room,Directions.EAST))
                            newFavoredX++;
                        else
                        if(!anythingThatDirection(room,Directions.WEST))
                            newFavoredX--;
                        else
                        if(!anythingThatDirection(room,Directions.NORTH))
                            newFavoredY--;
                        break;
                }
                nextRoom.positionedAlready=true;
                placeRoom(nextRoom,areaMap,newFavoredX,newFavoredY,processed,false,passTwo,depth+1);
            }
        }
    }
}
