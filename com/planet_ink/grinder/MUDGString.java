package com.planet_ink.grinder;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.net.*;
import java.awt.*;
import java.util.jar.*;
import java.util.zip.*;
import com.planet_ink.coffee_mud.utils.Directions;
import com.planet_ink.coffee_mud.utils.XMLManager;
import javax.swing.*;



public class MUDGString
{
	
	private static Object lastDoubleClicker=null;
	private static Calendar lastDoubleClicked=Calendar.getInstance();
	private static Object selectedThang=null;
	private static JJLabel secondSelectedThang=null;
	private static MUDGrinder grind;
	private static Hashtable panelMap=null;
	private static SymMouseRoom lsymMouse1=new SymMouseRoom();
	private static SymMouse lsymMouse2=new SymMouse();
	private static Hashtable icons=new Hashtable();
	
	public static void clearMap()
	{
	    if(grind==null) return;
	    for(int j=grind.getComponentCount()-1;j>=0;j--)
	    {
	        Component C=grind.getComponent(j);
	        if(C instanceof Panel)
	            grind.remove(C);
	    }
	    panelMap=null;
	    grind.repaint();
	}
	
	public static JJLabel getLabelFrom(Panel newPanel, String ID)
	{
	    JJLabel doorLabel=null;
	    for(int c=0;c<newPanel.getComponentCount();c++)
	    {
	        Component j=newPanel.getComponent(c);
	        if((j instanceof JJLabel)&&(j.getName().equals(ID)))
	        {
	            doorLabel=(JJLabel)j;
	            break;
	        }
	    }
	    return doorLabel;
	}
	
	public static ImageIcon getIcon(String filename)
	{
	    if(icons.get(filename)!=null)
	        return (ImageIcon)icons.get(filename);
	        
	    if(icons.get("grinder"+File.separator+filename)!=null)
	        return (ImageIcon)icons.get("grinder"+File.separator+filename);
	        
	    ImageIcon I=null;
	    try
	    {
		    File MyFile = new File(filename);
		    if (MyFile.canRead()==true)
    	        I=new ImageIcon(filename);
    	}
    	catch(Exception e)
    	{
    	}
    	if(I==null)
    	{
    	    try
    	    {
    	        JarFile J=new JarFile("MUDGrinder.jar");
    	        if(J.entries().hasMoreElements())
    	        {
    	            ZipEntry z=J.getEntry(filename);
    	            if(z!=null)
    	            {
    	                byte[] buf=new byte[J.getInputStream(z).available()+1];
    	                J.getInputStream(z).read(buf,0,J.getInputStream(z).available());
    	                I=new ImageIcon(buf);
    	            }
    	        }
    	    }
    	    catch(Exception e)
    	    {
    	        
    	    }
    	}
    	if(I!=null)
    	{
    	    icons.put(filename,I);
    	    return I;
    	}
    	else
        	return new ImageIcon(filename);
	}
	
	public static void setupPane(JJTextPane pane)
	{
	    pane.removeMouseListener(lsymMouse1);
	    pane.addMouseListener(lsymMouse1);
	}
	
	public static void setupLabel(String iconName, JJLabel label, String type, int dirCode)
	{
	    ImageIcon I=getIcon(iconName);
    	label.setIcon(I);
    	label.dirType=type;
    	label.dirCode=dirCode;
	    label.removeMouseListener(lsymMouse2);
	    label.addMouseListener(lsymMouse2);
	}
	
	private static void unselect(Object selected)
	{
		if(selected!=null)
		{
			if(selected instanceof JJLabel)
			{
			    JJLabel j=(JJLabel)selected;
			    j.setOpaque(false);
			    j.setBackground(Color.white);
			    j.setForeground(Color.white);
			    j.repaint();
			}
			else
			if(selected instanceof JJTextPane)
			{
			    JJTextPane p=(JJTextPane)selected;
			    p.setBackground(Color.lightGray);
			    p.setForeground(Color.black);
			    p.repaint();
			}
		}
	}
	private static void unselect()
	{
	    unselect(selectedThang);
	    unselect(secondSelectedThang);
		selectedThang=null;
		secondSelectedThang=null;
	}
	
	private static class SymMouse extends java.awt.event.MouseAdapter
	{
		public synchronized void mouseClicked(java.awt.event.MouseEvent event)
		{
			JJLabel src=(JJLabel)event.getSource();
			Calendar C=Calendar.getInstance();
			C.add(Calendar.MILLISECOND,-900);
			if((src==lastDoubleClicker)&&(lastDoubleClicked.after(C)))
			{
			    lastDoubleClicker=null;
			    lastDoubleClicked=C;
			    edit();
			    unselect();
			}
			else
			{
			    if((selectedThang!=null)&&(event.isShiftDown()||event.isAltDown()||event.isControlDown()))
			    {
			        if(selectedThang==src)
			        {
			            unselect();
			            return;
			        }
			        else
			        {
			            unselect(secondSelectedThang);
			            secondSelectedThang=null;
			        }
			        secondSelectedThang=src;
			        src.setOpaque(true);
			        src.setBackground(Color.green);
			        src.setForeground(Color.green);
			        src.repaint();
			    }
			    else
			    {
			        if(secondSelectedThang==src)
			        {
			            unselect();
			        }
			        else
			        {
			            unselect(selectedThang);
			            selectedThang=null;
			        }
			        selectedThang=src;
			        src.setOpaque(true);
			        src.setBackground(Color.blue);
			        src.setForeground(Color.blue);
			        src.repaint();
			    }
	            lastDoubleClicker=src;
	            lastDoubleClicked=Calendar.getInstance();
			}
		}
	}

	private static class SymMouseRoom extends java.awt.event.MouseAdapter
	{
		public synchronized void mouseClicked(java.awt.event.MouseEvent event)
		{
			JJTextPane src=(JJTextPane)event.getSource();
			Calendar C=Calendar.getInstance();
			C.add(Calendar.MILLISECOND,-900);
			if((src==lastDoubleClicker)&&(lastDoubleClicked.after(C)))
			{
	            lastDoubleClicker=null;
	            lastDoubleClicked=Calendar.getInstance();
			    edit();
			    unselect();
			}
			else
			{
			    unselect(selectedThang);
			    selectedThang=src;
			    src.setBackground(Color.blue);
			    src.setForeground(Color.white);
			    src.repaint();
	            lastDoubleClicker=src;
	            lastDoubleClicked=Calendar.getInstance();
			}
		}
	}

    public static void paintExits(Panel newPanel, MapGrinder.Room room)
    {
	    MapGrinder.Room[][] grid=MapGrinder.getGrid();
	    if(grid==null) return;
	    
	    for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
	    {
	        JJLabel j=getLabelFrom(newPanel,Integer.toString(d));
	        if(j!=null)
	        {
	            j.setIcon(null);
			    j.setOpaque(false);
			    j.setBackground(Color.white);
			    j.setForeground(Color.white);
			}
	        j=getLabelFrom(newPanel,Integer.toString(d|128));
	        if(j!=null)
	        {
	            j.setIcon(null);
			    j.setOpaque(false);
			    j.setBackground(Color.white);
			    j.setForeground(Color.white);
			}
	    }
	    for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
	    {
	        MapGrinder.Direction dir=(MapGrinder.Direction)room.doors[d];
	        String dirLetter=""+Directions.getDirectionName(d).toUpperCase().charAt(0);
	        if((d==Directions.UP)||(d==Directions.DOWN))
	        {
	            JJLabel doorLabel=null;
	            if(d==Directions.UP)
	            {
    	            doorLabel=getLabelFrom(newPanel,Integer.toString(Directions.NORTH|128));
    	            if(doorLabel.getIcon()!=null)
    	                doorLabel=getLabelFrom(newPanel,Integer.toString(Directions.WEST|128));
    	            else
    	            if(doorLabel.getIcon()!=null)
    	                doorLabel=getLabelFrom(newPanel,Integer.toString(Directions.EAST|128));
    	            else
    	            if(doorLabel.getIcon()!=null)
    	                doorLabel=getLabelFrom(newPanel,Integer.toString(Directions.SOUTH|128));
    	        }
    	        else
	            {
    	            doorLabel=getLabelFrom(newPanel,Integer.toString(Directions.SOUTH|128));
    	            if(doorLabel.getIcon()!=null)
    	                doorLabel=getLabelFrom(newPanel,Integer.toString(Directions.EAST|128));
    	            else
    	            if(doorLabel.getIcon()!=null)
    	                doorLabel=getLabelFrom(newPanel,Integer.toString(Directions.WEST|128));
    	            else
    	            if(doorLabel.getIcon()!=null)
    	                doorLabel=getLabelFrom(newPanel,Integer.toString(Directions.NORTH|128));
    	        }
	            if((dir==null)||((dir!=null)&&(dir.room.length()==0)))
	            {
    	            if(doorLabel!=null)
    	                setupLabel("E"+dirLetter+".gif",doorLabel,"E",d);
	            }
	            else
	            {
	                int actualDirection=-1;
	                MapGrinder.Room roomPointer=null;
	                if((room.y>0)&&(grid[room.x][room.y-1]!=null)&&(grid[room.x][room.y-1].roomID.equals(dir.room)))
	                {
	                    actualDirection=Directions.NORTH|128;
	                    roomPointer=grid[room.x][room.y-1];
	                }
	                if((room.y<MapGrinder.Ybound)&&(grid[room.x][room.y+1]!=null)&&(grid[room.x][room.y+1].roomID.equals(dir.room)))
	                {
	                    actualDirection=Directions.SOUTH|128;
	                    roomPointer=grid[room.x][room.y+1];
	                }
	                if((room.x<MapGrinder.Xbound)&&(grid[room.x+1][room.y]!=null)&&(grid[room.x+1][room.y].roomID.equals(dir.room)))
	                {
	                    actualDirection=Directions.EAST|128;
	                    roomPointer=grid[room.x+1][room.y]; 
	                }
	                if((room.x>0)&&(grid[room.x-1][room.y]!=null)&&(grid[room.x-1][room.y].roomID.equals(dir.room)))
	                {
	                    actualDirection=Directions.WEST|128;
	                    roomPointer=grid[room.x-1][room.y]; 
	                }
	                    
	                if((dir.room.length()>0)&&((roomPointer==null)||((roomPointer!=null)&&(!roomPointer.roomID.equals(dir.room)))))
    	                dirLetter+="R";
    	                    
    	            if(actualDirection>=0)
	                    doorLabel=getLabelFrom(newPanel,Integer.toString(actualDirection));
	                        
    	            MapGrinder.Exit exit=dir.exit;
    	            if((exit==null)||((exit!=null)&&(exit.classID.length()==0)))
    	                setupLabel("U"+dirLetter+".gif",doorLabel,"U",d);
    	            else
    	            if(exit.hasADoor)
    	                setupLabel("D"+dirLetter+".gif",doorLabel,"D",d);
    	            else
    	                setupLabel("O"+dirLetter+".gif",doorLabel,"O",d);
	            }
	        }
	        else
	        {
	            JJLabel doorLabel=getLabelFrom(newPanel,Integer.toString(d));
	            if((dir==null)||((dir!=null)&&(dir.room.length()==0)))
    	            setupLabel("E"+dirLetter+".gif",doorLabel,"E",d);
	            else
	            {
	                MapGrinder.Room roomPointer=null;
	                switch(d)
	                {
	                    case Directions.NORTH:
	                        if(room.y>0)
	                            roomPointer=grid[room.x][room.y-1];
	                        break;
	                    case Directions.SOUTH:
	                        if(room.y<MapGrinder.Ybound)
	                            roomPointer=grid[room.x][room.y+1];
	                        break;
	                    case Directions.EAST:
	                        if(room.x<MapGrinder.Xbound)
	                            roomPointer=grid[room.x+1][room.y]; 
	                        break;
	                    case Directions.WEST:
	                        if(room.x>0)
	                            roomPointer=grid[room.x-1][room.y]; 
	                        break;
	                }
	                if((dir.room.length()>0)&&((roomPointer==null)||((roomPointer!=null)&&(!roomPointer.roomID.equals(dir.room)))))
    	                dirLetter+="R";
    	            MapGrinder.Exit exit=dir.exit;
    	            doorLabel.setToolTipText(Directions.getDirectionName(d)+" to "+dir.room);
    	            if((exit==null)||((exit!=null)&&(exit.classID.length()==0)))
    	                setupLabel("U"+dirLetter+".gif",doorLabel,"U",d);
    	            else
    	            if(exit.hasADoor)
    	                setupLabel("D"+dirLetter+".gif",doorLabel,"D",d);
    	            else
    	                setupLabel("O"+dirLetter+".gif",doorLabel,"O",d);
	            }
	        }
	            
	    }
    }

	public static void buildMap(MUDGrinder M)
	{
	    grind=M;
	    Vector map=MapGrinder.getMap(grind);
	    if(map==null) return;
	    
	    panelMap=new Hashtable();
	    for(int m=0;m<map.size();m++)
	    {
	        MapGrinder.Room room=(MapGrinder.Room)map.elementAt(m);
	        Panel newPanel=gimmiRoom(room);
	        paintExits(newPanel, room);
	        grind.add(newPanel);
	        panelMap.put(room.roomID,newPanel);
	    }
	}
	
    public static void moveMapAround()
    {
	    if(grind==null) return;
	    Hashtable roomMap=MapGrinder.getHashRooms();
	    if(roomMap==null) return;
	    if(panelMap==null) return;
	    
	    for(Enumeration e=panelMap.keys();e.hasMoreElements();)
	    {
	        String roomID=(String)e.nextElement();
	        Panel P=(Panel)panelMap.get(roomID);
	        MapGrinder.Room room=(MapGrinder.Room)roomMap.get(roomID);
	        if((room!=null)&&(P!=null))
	            P.setBounds((5+(room.x*133))-(grind.horizontalScrollbar1.getValue()*133),(50+(room.y*133))-(grind.verticalScrollbar1.getValue()*133),132,132);
	    }
    }

	public static void reBuildMap()
	{
	    if(grind==null)
	        return;
	    MapGrinder.rePlaceRooms(grind);
	    Vector map=MapGrinder.getMap(grind);
	    if(map==null) return;
	    
	    for(int m=0;m<map.size();m++)
	    {
	        MapGrinder.Room room=(MapGrinder.Room)map.elementAt(m);
	        Panel newPanel=(Panel)panelMap.get(room.roomID);
	        if(newPanel==null)
	        {
	            newPanel=gimmiRoom(room);
	            panelMap.put(room.roomID,newPanel);
	            grind.add(newPanel);
	        }
    	        
	        paintExits(newPanel, room);
	        grind.add(newPanel);
	    }
	    moveMapAround();
	}
	
	public final static String SPACES="                                                ";
	public static void textifyPanel(MapGrinder.Room room, JJTextPane pane)
	{
	    String newText=room.roomID;
	    if(newText.indexOf("#")>0)
	        newText=newText.substring(newText.indexOf("#"));
	    newText=(newText+SPACES).substring(0,12)+" "+(room.classID+SPACES).substring(0,12)+" "+room.displayText;
	    pane.setText(newText);
	}
	
	public static Panel gimmiRoom(MapGrinder.Room room)
	{
	    Panel newPanel=gimmiPanel(room);
	    newPanel.setName(room.roomID);
	    textifyPanel(room,((JJTextPane)newPanel.getComponent(0)));
	    return newPanel;
	}
	
	public static void add()
	{
	    if(grind==null) return;
	    MapGrinder.Room connectRoom=null;
	    MapGrinder.Direction connectDirection=null;
	    int dirCode=0;
	    if(selectedThang!=null)
	    {
	        if(selectedThang instanceof JJTextPane)
	        {
	            boolean ok=GrinderOKCancel.askMe(grind,"MUDGrinder -- be careful!","If you wish to create a room connected to this one, you should select a free exit.  Click 'Cancel' to go back and select an exit, or click 'Ok' to create a new free-floating room.");
	            if(!ok) return;
	        }
	        else
	        if(selectedThang instanceof JJLabel)
	        {
	            dirCode=((JJLabel)selectedThang).dirCode;
	            connectRoom=((JJLabel)selectedThang).room;
	            connectDirection=connectRoom.doors[dirCode];
	            if(connectDirection!=null)
	                if((connectDirection.room!=null)&&(connectDirection.room.length()>0))
	                {
	                    GrinderOKBox.okMe(grind,"MUDGrinder -- you did WRONG!","You cannot create a room off an exit that is in use.  Please select an unused exit to link to that room, or select a room to create a free-floating room.");
	                    return;
	                }
	        }
	    }
	    if((selectedThang==null)||((selectedThang!=null)&&(selectedThang instanceof JJLabel)))
	    {
	        boolean ok=false;
	        if(connectRoom!=null)
    	        ok=GrinderOKCancel.askMe(grind,"MUDGrinder -- alrighty then!","Create a new room "+Directions.getDirectionName(dirCode)+" of room "+connectRoom.roomID+"?");
    	    else
    	        ok=GrinderOKCancel.askMe(grind,"MUDGrinder -- alrighty then!","Create a new free floating room?");
	        if(!ok) return;
	    }
	    MapGrinder.Room room=GrinderRoom.newMe(grind,"Create a brand spankin' new room!");
	    if(room!=null)
	    {
	        for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
	        {
	            MapGrinder.Direction D=new MapGrinder.Direction();
	            D.exit=null;
	            D.room="";
	            room.doors[d]=D;
	        }
	        Vector map=MapGrinder.getMap(grind);
	        MapGrinder.Exit exit1=new MapGrinder.Exit();
	        MapGrinder.Exit exit2=new MapGrinder.Exit();
	        exit1.classID="Open";
	        exit1.exitSame=true;
	        exit2.classID="Open";
	        exit2.exitSame=true;
	        if(connectDirection!=null)
	        {
	            MapGrinder.Direction D=room.doors[Directions.getOpDirectionCode(dirCode)];
	            if(D!=null)
	            {
	                D.room=connectRoom.roomID;
	                D.exit=exit1;
	            }

	        }
	        if(MapGrinder.createRoom(room))
	        {
	            if(connectDirection!=null)
	            {
    	            connectDirection.room=room.roomID;
    	            connectDirection.exit=exit2;
    	            MapGrinder.setRoomDirty(connectRoom, false,true);
    	        }
    	        MapGrinder.updateRoomItems(room);
    	        room.dirty=false;
    	        room.dirtyItems=false;
    	        map.addElement(room);
    	        unselect();
	            reBuildMap();
    	    }
	    }
	}
	public static void edit()
	{
	    if(grind==null) return;
	    if(selectedThang==null)
	    {
	        GrinderOKBox.okMe(grind,"MUDGrinder -- you did wrong!","Please select something first.");
	        return;
	    }
	    if(selectedThang instanceof JJLabel)
	    {
	        MapGrinder.Room room1=((JJLabel)selectedThang).room;
	        MapGrinder.Room room2=null;
	        MapGrinder.Direction dir1=room1.doors[((JJLabel)selectedThang).dirCode];
	        MapGrinder.Direction dir2=null;
	        MapGrinder.Exit exit=dir1.exit;
	        if(exit==null)
	        {
	            GrinderOKBox.okMe(grind,"MUDGrinder -- you did wrong!","You can only modify an exit that is in use.");
	            return;
	        }
	        if(exit.exitSame)
	        {
	            room2=(MapGrinder.Room)MapGrinder.getHashRooms().get(dir1.room);
	            if(room2!=null)
    	        {
    	            dir2=room2.doors[Directions.getOpDirectionCode(((JJLabel)selectedThang).dirCode)];
    	            if(!dir2.room.equalsIgnoreCase(room1.roomID))
    	            {
    	                room2=null;
    	                dir2=null;
    	            }
    	        }
	        }
	        MapGrinder.Exit newExit=GrinderExit.oldExit(grind,room1,dir1,exit,"Modify "+Directions.getDirectionName(((JJLabel)selectedThang).dirCode)+" exit from room "+room1.roomID+".");
	        if(newExit!=null)
	        {
	            dir1.exit=newExit;
	            if(dir2!=null)
	            {
	                newExit.exitSame=true;
    	            dir2.exit=newExit;
    	        }
	            if(!newExit.classID.equalsIgnoreCase("genexit"))
	            {
	                StringBuffer buf=TheGrinder.safelyExpect(null,"INFOXML <ID>"+newExit.classID+"</ID>","</OBJECT>");
	                if(buf!=null)
	                    newExit.hasADoor=XMLManager.returnXMLBoolean(buf.toString(),"OBJECTDOOR");
	            }
	            MapGrinder.setRoomDirty(room1, false,true);
	            if(room2!=null)
    	            MapGrinder.setRoomDirty(room2, false,true);
    	        unselect();
    	        Panel roomPanel1=(Panel)panelMap.get(room1.roomID);
    	        Panel roomPanel2=null;
    	        if(roomPanel1!=null)
        	        paintExits(roomPanel1,room1);
        	    if(room2!=null)
        	        roomPanel2=(Panel)panelMap.get(room2.roomID);
    	        if(roomPanel2!=null)
        	        paintExits(roomPanel2,room2);
	        }
	    }
	    else
	    if(selectedThang instanceof JJTextPane)
	    {
	        JJTextPane J=((JJTextPane)selectedThang);
	        MapGrinder.Room room=J.room;
	        MapGrinder.Room eRoom=GrinderRoom.oldMe(grind,room,"Modify room "+room.roomID+".");
	        if(eRoom!=null)
	        {
	            eRoom.copyInto(room);
	            textifyPanel(room,J);
	            if((room.dirty)||(room.dirtyItems))
	                MapGrinder.setRoomDirty(room,room.dirtyItems,room.dirty);
	        }
	    }
	}
	public static void delete()
	{
	    if(grind==null) return;
	    if(selectedThang==null)
	    {
	        GrinderOKBox.okMe(grind,"MUDGrinder -- you did wrong!","Please select something first.");
	        return;
	    }
	    if(selectedThang instanceof JJLabel)
	    {
	        JJLabel J=(JJLabel)selectedThang;
	        MapGrinder.Room room=J.room;
	        int dirCode=J.dirCode;
	        MapGrinder.Direction dir=room.doors[dirCode];
	        String code=J.dirType;
	        
	        // can't delete a nonexistant exit!
	        if((code.equalsIgnoreCase("E"))||(code.trim().length()==0)||(dir==null)||(dir.room.length()==0))
	            return;
	            
	        MapGrinder.Room alsoRoom=(MapGrinder.Room)MapGrinder.getHashRooms().get(dir.room);
	        MapGrinder.Direction alsoDir=null;
	        if((dir.exit!=null)&&(dir.exit.exitSame)&&(alsoRoom!=null))
	        {
    	        alsoDir=alsoRoom.doors[Directions.getOpDirectionCode(dirCode)];
    	        if(!alsoDir.room.equalsIgnoreCase(room.roomID))
    	            alsoDir=null;
    	    }
	        
	        String theMessage="Delete "+Directions.getDirectionName(dirCode)+" exit from "+room.roomID+" to "+dir.room;
	        if((alsoDir!=null)&&(alsoRoom!=null))
    	        theMessage+=" and "+Directions.getDirectionName(Directions.getOpDirectionCode(dirCode))+" exit from "+alsoRoom.roomID+" to "+room.roomID;
	        boolean saidYes=GrinderYesNo.askMe(grind,"Really delete?",theMessage+"?");
	        if(saidYes)
	        {
	            if(alsoDir!=null)
	            {
	                alsoDir.room="";
	                alsoDir.exit=null;
	                MapGrinder.setRoomDirty(alsoRoom,false,true);
	            }
	        }
	        else
	            saidYes=GrinderOKCancel.askMe(grind,"Single de-link?","Delete the exit from "+room.roomID+" to "+dir.room+" only?");
	        if(saidYes)
	        {
	            dir.room="";
	            dir.exit=null;
	            if((alsoDir!=null)&&(alsoDir.exit!=null))
	                alsoDir.exit.exitSame=false;
	            MapGrinder.setRoomDirty(room, false,true);
    	        unselect();
	            reBuildMap();
	        }
	    }
	    else
	    {
	        JJTextPane J=(JJTextPane)selectedThang;
	        MapGrinder.Room room=J.room;
	        if(!room.deleted)
	        {
	            boolean saidYes=GrinderOKCancel.askMe(grind,"OBLITERATE?","Delete the room "+room.roomID+" now and FOREVER?!");
    	        if(saidYes)
    	        {
    	            unselect();
    	            Vector V=MapGrinder.getMap(grind);
    	            for(int m=0;m<V.size();m++)
    	            {
    	                MapGrinder.Room rroom=(MapGrinder.Room)V.elementAt(m);
    	                for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
    	                {
    	                    MapGrinder.Direction dir=(MapGrinder.Direction)rroom.doors[d];
    	                    if((dir!=null)&&(dir.room.equals(room.roomID)))
    	                    {
    	                        dir.room="";
    	                        dir.exit=null;
    	                        MapGrinder.setRoomDirty(rroom,false,true);
    	                    }
    	                }
    	            }
    	            boolean gone=false;
    	            while(!gone)
    	            {
    	                MapGrinder.setRoomDeleted(room);
    	                MapGrinder.saveIfAble(grind);
    	                gone=true;
    	                for(int m=0;m<V.size();m++)
    	                    if(room==V.elementAt(m))
    	                    {
    	                        gone=false;
    	                        break;
    	                    }
    	            }
	                panelMap.remove(room);
	                panelMap.remove(room.roomID);
	                grind.remove(J.getParent());
	                reBuildMap();
    	        }
	        }
	        
	    }
	}
	public static void link()
	{
	    if(grind==null) return;
	    if(selectedThang==null)
	    {
	        GrinderOKBox.okMe(grind,"MUDGrinder -- you did wrong!","Please select something first.");
	        return;
	    }
	    if(!(selectedThang instanceof JJLabel))
	    {
	        GrinderOKBox.okMe(grind,"MUDGrinder -- you did wrong!","You must select two exits to link them.");
	        return;
	    }
	    if(secondSelectedThang==null)
	    {
	        GrinderOKBox.okMe(grind,"MUDGrinder -- you did wrong!","Use the shift key to select a second exit to connect to with your mouse,");
	        return;
	    }
	    MapGrinder.Room room1=((JJLabel)selectedThang).room;
	    MapGrinder.Room room2=secondSelectedThang.room;
	    MapGrinder.Direction dir1=room1.doors[((JJLabel)selectedThang).dirCode];
	    MapGrinder.Direction dir2=room2.doors[secondSelectedThang.dirCode];
	    if(((dir1.room!=null)&&(dir1.room.length()>0))||((dir2.room!=null)&&(dir2.room.length()>0)))
	    {
	        GrinderOKBox.okMe(grind,"MUDGrinder -- you did wrong!","Your selected exits must be unused/deleted before you can link them.");
	        return;
	    }
	    MapGrinder.Exit exit=GrinderExit.newExit(grind,room1.roomID,room2.roomID,"Link two exits", ((JJLabel)selectedThang).dirCode==Directions.getOpDirectionCode(secondSelectedThang.dirCode));
	    if(exit!=null)
	    {
	        exit.exitSame=((JJLabel)selectedThang).dirCode==Directions.getOpDirectionCode(secondSelectedThang.dirCode);
	        dir1.exit=exit;
	        dir2.exit=exit;
	        dir1.room=room2.roomID;
	        dir2.room=room1.roomID;
	        
	        if(!exit.classID.equalsIgnoreCase("genexit"))
	        {
	            StringBuffer buf=TheGrinder.safelyExpect(null,"INFOXML <ID>"+exit.classID+"</ID>","</OBJECT>");
	            if(buf!=null)
	                exit.hasADoor=XMLManager.returnXMLBoolean(buf.toString(),"OBJECTDOOR");
	        }
	        MapGrinder.setRoomDirty(room1, false,true);
	        MapGrinder.setRoomDirty(room2, false,true);
    	    unselect();
	        reBuildMap();
	    }
	}
	
	public static Panel gimmiPanel(MapGrinder.Room room)
	{
	    java.awt.Panel panel1 = new java.awt.Panel();
	    JJTextPane wrappingLabel1 = new JJTextPane();
	    setupPane(wrappingLabel1);
	    wrappingLabel1.room=room;
		panel1.setLayout(null);
		panel1.setBounds(336,84,132,132);
		panel1.add(wrappingLabel1);
		wrappingLabel1.setBackground(java.awt.Color.lightGray);
		wrappingLabel1.setFont(new Font("MonoSpaced", Font.PLAIN, 10));
		wrappingLabel1.setEditable(false);
		wrappingLabel1.setBounds(24,24,84,84);
		
	    for(int j=0;j<8;j++)
	    {
    	    JJLabel JLabel1 = new JJLabel();
			JLabel1.setBackground(Color.white);
			JLabel1.setForeground(Color.white);
		    panel1.add(JLabel1);
		    JLabel1.room=room;
		    switch(j)
		    {
		        case 0:
		            JLabel1.setBounds(36,0,24,24); // first north
		            JLabel1.setName(""+(Directions.NORTH));
		            break;
		        case 1:
		            JLabel1.setBounds(72,0,24,24); // second north
		            JLabel1.setName(""+(Directions.NORTH | 128));
		            break;
		        case 2:
		            JLabel1.setBounds(36,108,24,24); // first south
		            JLabel1.setName(""+(Directions.SOUTH));
		            break;
		        case 3:
            		JLabel1.setBounds(72,108,24,24); // second south
		            JLabel1.setName(""+(Directions.SOUTH | 128));
		            break;
		        case 4:
		            JLabel1.setBounds(108,36,24,24); // first east
		            JLabel1.setName(""+(Directions.EAST));
		            break;
		        case 5:
		            JLabel1.setBounds(108,72,24,24); // second east
		            JLabel1.setName(""+(Directions.EAST | 128));
		            break;
		        case 6:
		            JLabel1.setBounds(0,36,24,24); // first west
		            JLabel1.setName(""+(Directions.WEST));
		            break;
		        case 7:
		            JLabel1.setBounds(0,72,24,24); // second west
		            JLabel1.setName(""+(Directions.WEST | 128));
		            break;
		    }
		    panel1.add(JLabel1);
        }
		return panel1;
	}
	static class JJTextPane extends JTextPane
	{
	    public MapGrinder.Room room=null;
	}
	static class JJLabel extends JLabel
	{
	    public MapGrinder.Room room=null;
	    public String dirType="";
	    public int dirCode=0;
	}
	
}