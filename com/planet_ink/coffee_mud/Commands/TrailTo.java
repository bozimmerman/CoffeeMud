package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class TrailTo extends StdCommand
{
	public TrailTo(){}

	private String[] access={"TRAILTO"};
	public String[] getAccessWords(){return access;}


	public String trailTo(Room R1, Vector commands)
	{
		String where=Util.combine(commands,1);
		if(where.length()==0) return "Trail to where? Try a Room ID, 'everyroom', or 'everyarea'.";
		if(R1==null) return "Where are you?";
		boolean confirm=false;
		if(where.endsWith(" CONFIRM!"))
		{
			where=where.substring(0,where.length()-9).trim();
			confirm=true;
		}
		Vector set=new Vector();
		MUDTracker.getRadiantRooms(R1,set,false,false,true,null,Integer.MAX_VALUE);
		if(where.equalsIgnoreCase("everyarea"))
		{
			StringBuffer str=new StringBuffer("");
			for(Enumeration a=CMMap.areas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				str.append(Util.padRightPreserve(A.name(),30)+": "+trailTo(R1,set,A.name(),confirm)+"\n\r");
			}
			if(confirm) Log.rawSysOut(str.toString());
			return str.toString();
		}
		else
		if(where.equalsIgnoreCase("everyroom"))
		{
			StringBuffer str=new StringBuffer("");
			for(Enumeration a=CMMap.rooms();a.hasMoreElements();)
			{
				Room R=(Room)a.nextElement();
				if((R!=R1)&&(R.roomID().length()>0))
					str.append(Util.padRightPreserve(R.roomID(),30)+": "+trailTo(R1,set,R.roomID(),confirm)+"\n\r");
			}
			if(confirm) Log.rawSysOut(str.toString());
			return str.toString();
		}
		else
		{
			String str=Util.padRightPreserve(where,30)+": "+trailTo(R1,set,where,confirm);
			if(confirm) Log.rawSysOut(str);
			return str;
		}
	}
	private int getRoomDirection(Room R, Room toRoom, Vector ignore)
	{
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
			if((R.getRoomInDir(d)==toRoom)
			&&(R!=toRoom)
			&&(!ignore.contains(R)))
				return d;
		return -1;
	}
	public String trailTo(Room R1, Vector set, String where, boolean confirm)
	{
		Room R2=CMMap.getRoom(where);
		if(R2==null)
			for(Enumeration a=CMMap.areas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				if(A.name().equalsIgnoreCase(where))
				{
					if(set.size()==0)
					{
						int lowest=Integer.MAX_VALUE;
						for(Enumeration r=A.getMap();r.hasMoreElements();)
						{
							Room R=(Room)r.nextElement();
							int x=R.roomID().indexOf("#");
							if((x>=0)&&(Util.s_int(R.roomID().substring(x+1))<lowest))
								lowest=Util.s_int(R.roomID().substring(x+1));
						}
						if(lowest<Integer.MAX_VALUE)
							R2=CMMap.getRoom(A.name()+"#"+lowest);
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
			MUDTracker.getRadiantRooms(R1,set,false,false,true,R2,Integer.MAX_VALUE);
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
			theDirTrail.addElement(new Character(Directions.getDirectionName(getRoomDirection(R,RA,empty)).charAt(0)).toString()+" ");
		}
		StringBuffer theTrail=new StringBuffer("");
		if(confirm)	theTrail.append("\n\r"+Util.padRight("Trail",30)+": ");
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
					theTrail.append(new Character(lastDir).toString()+" ");
				else
					theTrail.append(new Integer(lastNum).toString()+new Character(lastDir).toString()+" ");
				lastDir=s.charAt(0);
				lastNum=1;
			}
			theDirTrail.removeElementAt(0);
		}
		if(lastNum==1)
			theTrail.append(new Character(lastDir).toString());
		else
		if(lastNum>0)
			theTrail.append(new Integer(lastNum).toString()+new Character(lastDir).toString());

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
			theTrail.append("\n\r"+Util.padRight("From",30)+": "+Directions.getDirectionName(getRoomDirection(R,R2,empty))+" <- "+R.roomID());
			theTrail.append("\n\r"+Util.padRight("Room",30)+": "+R.displayText()+"/"+R.description());
			theTrail.append("\n\r\n\r");
		}
		return theTrail.toString();
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isMonster())
			mob.session().rawPrintln(trailTo(mob.location(),commands));
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"TRAILTO");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
