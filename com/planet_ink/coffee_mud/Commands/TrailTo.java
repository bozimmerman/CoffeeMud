package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
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

import java.util.*;

/*
   Copyright 2000-2008 Bo Zimmerman

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
public class TrailTo extends StdCommand
{
	public TrailTo(){}

	private String[] access={"TRAILTO"};
	public String[] getAccessWords(){return access;}


	public String trailTo(Room R1, Vector commands)
	{
		int radius=Integer.MAX_VALUE;
        HashSet ignoreRooms=null;
        for(int c=0;c<commands.size();c++)
        {
            String s=(String)commands.elementAt(c);
            if(s.toUpperCase().startsWith("RADIUS"))
            {
                s=s.substring(("RADIUS").length()).trim();
                if(!s.startsWith("=")) continue;
                s=s.substring(1);
                commands.removeElementAt(c);
                radius=CMath.s_int(s);
            }
            else
            if(s.toUpperCase().startsWith("IGNOREROOMS"))
            {
                s=s.substring(("IGNOREROOMS").length()).trim();
                if(!s.startsWith("=")) continue;
                s=s.substring(1);
                commands.removeElementAt(c);
                Vector roomList=CMParms.parseCommas(s,true);
                ignoreRooms=new HashSet();
                for(int v=0;v<roomList.size();v++)
                {
                    Room R=CMLib.map().getRoom((String)roomList.elementAt(v));
                    if(R==null){ return "Ignored room "+((String)roomList.elementAt(v))+" is unknown!";}
                    if(!ignoreRooms.contains(R))ignoreRooms.add(R);
                }
            }
        }
		String where=CMParms.combine(commands,1);
		if(where.length()==0) return "Trail to where? Try a Room ID, 'everyroom', or 'everyarea'.  You can also use the 'areanames', 'ignorerooms=', and 'confirm!' flags.";
		if(R1==null) return "Where are you?";
		boolean confirm=false;
        boolean areaNames=false;
        boolean justTheFacts=false;
        if(where.toUpperCase().endsWith(" AREANAMES"))
        {
            where=where.substring(0,where.length()-10).trim();
            areaNames=true;
        }
        if(where.toUpperCase().endsWith(" JUSTTHEFACTS"))
        {
            where=where.substring(0,where.length()-13).trim();
            justTheFacts=true;
        }
		if(where.toUpperCase().endsWith(" CONFIRM!"))
		{
			where=where.substring(0,where.length()-9).trim();
			confirm=true;
		}
		Vector set=new Vector();
		CMLib.tracking().getRadiantRooms(R1,set,false,false,true,false,false,null,radius,ignoreRooms);
		if(where.equalsIgnoreCase("everyarea"))
		{
			StringBuffer str=new StringBuffer("");
			for(Enumeration a=CMLib.map().sortedAreas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				str.append(CMStrings.padRightPreserve(A.name(),30)+": "+trailTo(R1,set,A.name(),areaNames,confirm,radius,ignoreRooms)+"\n\r");
			}
			if(confirm) Log.rawSysOut(str.toString());
			return str.toString();
		}
		else
		if(where.equalsIgnoreCase("everyroom"))
		{
			StringBuffer str=new StringBuffer("");
			try
			{
				for(Enumeration a=CMLib.map().rooms();a.hasMoreElements();)
				{
					Room R=(Room)a.nextElement();
					if((R!=R1)&&(R.roomID().length()>0))
						str.append(CMStrings.padRightPreserve(R.roomID(),30)+": "+trailTo(R1,set,R.roomID(),areaNames,confirm,radius,ignoreRooms)+"\n\r");
				}
		    }catch(NoSuchElementException nse){}
			if(confirm) Log.rawSysOut(str.toString());
			return str.toString();
		}
		else
		{
			String str=trailTo(R1,set,where,areaNames,confirm,radius,ignoreRooms);
			if(!justTheFacts)str=CMStrings.padRightPreserve(where,30)+": "+str;
			if(confirm) Log.rawSysOut(str);
			return str;
		}
	}
	protected int getRoomDirection(Room R, Room toRoom, Vector ignore)
	{
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			if((R.getRoomInDir(d)==toRoom)
			&&(R!=toRoom)
			&&(!ignore.contains(R)))
				return d;
		return -1;
	}
	public String trailTo(Room R1, Vector set, String where, boolean areaNames, boolean confirm, int radius, HashSet ignoreRooms)
	{
		Room R2=CMLib.map().getRoom(where);
		if(R2==null)
			for(Enumeration a=CMLib.map().sortedAreas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				if(A.name().equalsIgnoreCase(where))
				{
					if(set.size()==0)
					{
						int lowest=Integer.MAX_VALUE;
						for(Enumeration r=A.getCompleteMap();r.hasMoreElements();)
						{
							Room R=(Room)r.nextElement();
							int x=R.roomID().indexOf("#");
							if((x>=0)&&(CMath.s_int(R.roomID().substring(x+1))<lowest))
								lowest=CMath.s_int(R.roomID().substring(x+1));
						}
						if(lowest<Integer.MAX_VALUE)
							R2=CMLib.map().getRoom(A.name()+"#"+lowest);
					}
					else
					{
						for(int i=0;i<set.size();i++)
						{
							Room R=(Room)set.elementAt(i);
							if(R.getArea()==A)
							{
								R2=R;
								break;
							}
						}
					}
					break;
				}
			}
		if(R2==null) return "Unable to determine '"+where+"'.";
		if(set.size()==0)
			CMLib.tracking().getRadiantRooms(R1,set,false,false,true,false,false,R2,radius,ignoreRooms);
		int foundAt=-1;
		for(int i=0;i<set.size();i++)
		{
			Room R=(Room)set.elementAt(i);
			if(R==R2){ foundAt=i; break;}
		}
		if(foundAt<0) return "You can't get to '"+R2.roomID()+"' from here.";
		Room checkR=R2;
		Vector trailV=new Vector();
		trailV.addElement(R2);
        HashSet areasDone=new HashSet();
		boolean didSomething=false;
		while(checkR!=R1)
		{
			didSomething=false;
			for(int r=0;r<foundAt;r++)
			{
				Room R=(Room)set.elementAt(r);
				if(getRoomDirection(R,checkR,trailV)>=0)
				{
					trailV.addElement(R);
                    if(!areasDone.contains(R.getArea()))
                        areasDone.add(R.getArea());
					foundAt=r;
					checkR=R;
					didSomething=true;
					break;
				}
			}
			if(!didSomething)
				return "No trail was found?!";
		}
		Vector theDirTrail=new Vector();
		Vector empty=new Vector();
		for(int s=trailV.size()-1;s>=1;s--)
		{
			Room R=(Room)trailV.elementAt(s);
			Room RA=(Room)trailV.elementAt(s-1);
			theDirTrail.addElement(Character.toString(Directions.getDirectionName(getRoomDirection(R,RA,empty)).charAt(0))+" ");
		}
		StringBuffer theTrail=new StringBuffer("");
		if(confirm)	theTrail.append("\n\r"+CMStrings.padRight("Trail",30)+": ");
		char lastDir='\0';
		int lastNum=0;
		while(theDirTrail.size()>0)
		{
			String s=(String)theDirTrail.elementAt(0);
			if(lastNum==0)
			{
				lastDir=s.charAt(0);
				lastNum=1;
			}
			else
			if(s.charAt(0)==lastDir)
				lastNum++;
			else
			{
				if(lastNum==1)
					theTrail.append(Character.toString(lastDir)+" ");
				else
					theTrail.append(Integer.toString(lastNum)+Character.toString(lastDir)+" ");
				lastDir=s.charAt(0);
				lastNum=1;
			}
			theDirTrail.removeElementAt(0);
		}
		if(lastNum==1)
			theTrail.append(Character.toString(lastDir));
		else
		if(lastNum>0)
			theTrail.append(Integer.toString(lastNum)+Character.toString(lastDir));

		if((confirm)&&(trailV.size()>1))
		{
			for(int i=0;i<trailV.size();i++)
			{
				Room R=(Room)trailV.elementAt(i);
				if(R.roomID().length()==0)
				{
					theTrail.append("*");
					break;
				}
			}
			Room R=(Room)trailV.elementAt(1);
			theTrail.append("\n\r"+CMStrings.padRight("From",30)+": "+Directions.getDirectionName(getRoomDirection(R,R2,empty))+" <- "+R.roomID());
			theTrail.append("\n\r"+CMStrings.padRight("Room",30)+": "+R.displayText()+"/"+R.description());
			theTrail.append("\n\r\n\r");
		}
        if((areaNames)&&(areasDone.size()>0))
        {
            theTrail.append("\n\r"+CMStrings.padRight("Areas",30)+":");
            for(Iterator i=areasDone.iterator();i.hasNext();)
            {
                Area A=(Area)i.next();
                theTrail.append(" \""+A.name()+"\",");
            }
        }
		return theTrail.toString();
	}

	public boolean execute(MOB mob, Vector<Object> commands, int metaFlags)
		throws java.io.IOException
	{
		if((commands.size()>0)&&(((String)commands.lastElement()).equalsIgnoreCase("QUIETLY")))
		{
			commands.removeElementAt(commands.size()-1);
			commands.setElementAt(trailTo(mob.location(),commands),0);
		}
		else
		if(!mob.isMonster())
			mob.session().rawPrintln(trailTo(mob.location(),commands));
		return false;
	}

	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"TRAILTO");}


}
