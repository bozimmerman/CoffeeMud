package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class StdMap extends StdItem implements com.planet_ink.coffee_mud.interfaces.Map
{
	private StringBuffer myMap=null;
	private int oldLevel=0;

	public StdMap()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a map";
		displayText="a map sits here";
		description="Looks like a map of some place.";
		isReadable=true;
		baseGoldValue=10;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new StdMap();
	}

	private class MapRoom
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
		String newText=getMapArea();
		String newName="";
		while(newText.length()>0)
		{
			int y=newText.indexOf(";");
			String areaName="";
			if(y>=0)
			{
				areaName=newText.substring(0,y).trim();
				newText=newText.substring(y+1);
			}
			else
			{
				areaName=newText;
				newText="";
			}
			if(areaName.length()>0)
			{
				if(newName.length()==0)
					newName="a map of ";
				else
				if(newText.length()==0)
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

    public MapRoom[][] rebuildGrid(Vector areaMap)
    {
	    // build grid!
	    int xoffset=0;
	    int yoffset=0;
	    for(int x=0;x<areaMap.size();x++)
	        if(((MapRoom)areaMap.elementAt(x)).x<xoffset)
	            xoffset=((MapRoom)areaMap.elementAt(x)).x;

	    for(int y=0;y<areaMap.size();y++)
	        if(((MapRoom)areaMap.elementAt(y)).y<yoffset)
	            yoffset=((MapRoom)areaMap.elementAt(y)).y;

	    xoffset=xoffset*-1;
	    yoffset=yoffset*-1;

	    int Xbound=0;
	    int Ybound=0;
	    for(int x=0;x<areaMap.size();x++)
	    {
	        MapRoom room=(MapRoom)areaMap.elementAt(x);
	        room.x=room.x+xoffset;
	        if(room.x>Xbound)
	            Xbound=room.x;
	        room.y=room.y+yoffset;
	        if(room.y>Ybound)
	            Ybound=room.y;
	    }
	    MapRoom[][] grid=new MapRoom[Xbound+1][Ybound+1];
	    for(int y=0;y<areaMap.size();y++)
	    {
	        MapRoom room=(MapRoom)areaMap.elementAt(y);
	        grid[room.x][room.y]=room;
	    }


		int numStragglers=0;
		int somethingsLeft=0;
		// now clear out stragglers.
	    for(int x=0;x<grid.length;x++)
			for(int y=0;y<grid[0].length;y++)
			{
			    MapRoom room=(MapRoom)grid[x][y];
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
						areaMap.remove(room);
						grid[x][y]=null;
						numStragglers++;
					}
				}
			}
		if((numStragglers>0)&&(somethingsLeft>0))
			return rebuildGrid(areaMap);

		return grid;
    }

	public StringBuffer getMyMappedRoom()
	{
		if(oldLevel!=envStats().level())
		{
			myMap=null;
			oldLevel=envStats().level();
		}

		if(myMap!=null)
			return myMap;

		Object o=Resources.getResource("map"+envStats().level()+":"+getMapArea());
		if((o!=null)&&(o instanceof StringBuffer))
			myMap=(StringBuffer)o;

		if(myMap!=null)
			return myMap;

		Vector mapAreas=new Vector();
		myMap=null;
		String newText=getMapArea();
		while(newText.length()>0)
		{
			int y=newText.indexOf(";");
			String areaName="";
			if(y>=0)
			{
				areaName=newText.substring(0,y).trim();
				newText=newText.substring(y+1);
			}
			else
			{
				areaName=newText;
				newText="";
			}
			if(areaName.length()>0)
				mapAreas.addElement(areaName);
		}

		Vector mapRooms=new Vector();
		for(int a=0;a<mapAreas.size();a++)
		{
			String area=(String)mapAreas.elementAt(a);
			for(int m=0;m<CMMap.numRooms();m++)
			{
				Room room=CMMap.getRoom(m);
				if(room.getArea().name().trim().equalsIgnoreCase(area))
				{
					MapRoom mr=new MapRoom();
					mr.r=room;
					mapRooms.addElement(mr);
				}
			}
		}

		StringBuffer map=new StringBuffer("");
		if(mapRooms.size()>0)
		{
			placeRooms(mapRooms);
			MapRoom[][] grid=rebuildGrid(mapRooms);
			if(grid.length>0)
			for(int y=0;y<grid[0].length;y++)
			{
				String line1="";
				String line2="";
				String line3="";
				String line4="";
				String line5="";
				String line6="";
				for(int x=0;x<grid.length;x++)
				{
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
						line1+="---"+dirChar(Directions.NORTH,grid,x,y)+"----";
						line2+="!"+paddedName.substring(0,6)+"!";
						line3+=dirChar(Directions.WEST,grid,x,y)+paddedName.substring(6,12)+dirChar(Directions.EAST,grid,x,y);
						line4+="!"+paddedName.substring(12,18)+"!";
						line5+="!"+paddedName.substring(18,24)+"!";
						line6+="---"+dirChar(Directions.SOUTH,grid,x,y)+"----";
					}
				}
				map.append(line1+"\r\n"+line2+"\r\n"+line3+"\r\n"+line4+"\r\n"+line5+"\r\n"+line6+"\r\n");
			}

		}

		myMap=map;
		Resources.submitResource("map"+envStats().level()+":"+getMapArea(),myMap);
		return myMap;
	}


	public char dirChar(int dirCode, MapRoom[][] grid, int x, int y)
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
		{
			nextRoom=grid[x][y];
			if(nextRoom==null)
				return ' ';
		}
		catch(Throwable t)
		{
			return ' ';
		}
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			if(room.r.rawDoors()[d]==nextRoom.r)
			{
				dirCode=d;
				break;
			}
		}
		if(dirCode<0)
			return ' ';
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

    public MapRoom getRoom(Vector allRooms, String ID)
    {
        for(int r=0;r<allRooms.size();r++)
        {
            MapRoom room=(MapRoom)allRooms.elementAt(r);
            if(room.r.ID().equalsIgnoreCase(ID))
                return room;
        }
        return null;
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
        Room D=room.r.rawDoors()[direction];
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
		for(int e=0;e<Directions.NUM_DIRECTIONS;e++)
		{
			Exit exit=room.r.rawExits()[e];
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

    public void placeRooms(Vector areaMap)
    {
        if(areaMap==null) return;
        if(areaMap.size()==0) return;

        for(int i=0;i<areaMap.size();i++)
        {
            MapRoom room=(MapRoom)areaMap.elementAt(i);
            room.x=0;
            room.y=0;
            for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
            {
                Room dir=room.r.rawDoors()[d];
                if(dir!=null)
				{
					MapRoom rm=getRoom(areaMap,dir.ID());
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
            for(int i=0;i<areaMap.size();i++)
            {
                MapRoom room=(MapRoom)areaMap.elementAt(i);
                if((processed.get(room.r.ID())==null)&&(okToPlace(room)))
                {
                    placeRoom(room,0,0,processed,areaMap,true);
                    doneSomething=true;
                }
            }
        }
        //if(areaMap.size()>processed.size())
        //    Log.errOut("Map","?!"+(areaMap.size()-processed.size())+" room(s) could not be placed.  I recommend restarting your server.");
    }

	public void affect(Affect affect)
	{
		MOB mob=affect.source();
		if(!affect.amITarget(this))
			super.affect(affect);
		else
		switch(affect.targetMinor())
		{
		case Affect.TYP_READSOMETHING:
			if(Sense.canBeSeenBy(this,mob))
			{
				StringBuffer map=this.getMyMappedRoom();

				if((isReadable)&&(map!=null)&&(!mob.isMonster()))
					mob.session().rawPrint(map.toString());
				else
					mob.tell(name()+" appears to be blank.");
			}
			else
				mob.tell("You can't see that!");
			return;
		}
		super.affect(affect);
	}
    public void placeRoom(MapRoom room,
							int favoredX,
							int favoredY,
							Hashtable processed,
							Vector allRooms,
							boolean doNotDefer)
    {
        if(room==null) return;

        MapRoom anythingAt=getProcessedRoomAt(processed,favoredX,favoredY);
        if(anythingAt!=null)
        {
            // maybe someone else will take care of it?
            if(!doNotDefer)
                for(int r=0;r<allRooms.size();r++)
                {
                    MapRoom roomToBlame=(MapRoom)allRooms.elementAt(r);
                    if(roomToBlame!=room)
                        for(int rd=0;rd<Directions.NUM_DIRECTIONS;rd++)
                        {
                            Room RD=roomToBlame.r.rawDoors()[rd];
							if(RD!=null)
							{
								MapRoom MR=getRoom(allRooms,RD.ID());
								if((RD!=null)&&(RD.ID().equals(room.r.ID()))&&(MR!=null)&&(!MR.positionedAlready))
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
        processed.put(room.r.ID(),room);

        for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
        {
            String roomID=null;
			Exit exit=null;
            if(room.r.rawDoors()[d]!=null)
			{
                roomID=room.r.rawDoors()[d].ID();
				exit=room.r.rawExits()[d];
			}

            if((roomID!=null)&&(roomID.length()>0)&&(processed.get(roomID)==null))
            {
                MapRoom nextRoom=getRoom(allRooms,roomID);
                if((nextRoom!=null)&&(okToPlace(nextRoom,exit)))
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
                    placeRoom(nextRoom,newFavoredX,newFavoredY,processed,allRooms,false);
                }
            }
        }
    }
}
