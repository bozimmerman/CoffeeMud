package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Where extends StdCommand
{
	public Where(){}

	private String[] access={"WHERE"};
	public String[] getAccessWords(){return access;}

	private void whereAdd(DVector V, String area, int i)
	{
		if(V.contains(area)) return;

		for(int v=0;v<V.size();v++)
		{
			if(((Integer)V.elementAt(v,2)).intValue()>i)
			{
				V.insertElementAt(v,area,new Integer(i));
				return;
			}
		}
		V.addElement(area,new Integer(i));
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(mob.isASysOp(mob.location()))
		{
			StringBuffer lines=new StringBuffer("^x");
			lines.append(Util.padRight("Name",17)+"| ");
			lines.append(Util.padRight("Location",17)+"^.^N\n\r");
			String who=Util.combine(commands,1);
			if(who.length()==0)
			{
				for(int s=0;s<Sessions.size();s++)
				{
					Session thisSession=(Session)Sessions.elementAt(s);
					if(thisSession.mob() != null)
					{
						if(mob.isASysOp(thisSession.mob().location()))
						{
							lines.append("^!"+Util.padRight(thisSession.mob().Name(),17)+"^?| ");
							if(thisSession.mob().location() != null )
							{
								lines.append(thisSession.mob().location().displayText());
								lines.append(" ("+CMMap.getExtendedRoomID(thisSession.mob().location())+")");
							}
							else
								lines.append("^!(no location)^?");
							lines.append("\n\r");
						}
					}
					else
					if(mob.isASysOp(null))
					{
						lines.append(Util.padRight("NAMELESS",17)+"| ");
						lines.append("NOWHERE");
						lines.append("\n\r");
					}
				}
			}
			else
			{
				for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if((R!=null)&&(mob.isASysOp(R)))
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB M=R.fetchInhabitant(m);
						if((EnglishParser.containsString(M.name(),who))
						||(EnglishParser.containsString(M.displayText(),who)))
						{
							lines.append("^!"+Util.padRight(M.name(),17)+"^?| ");
							lines.append(R.roomTitle());
							lines.append(" ("+R.roomID()+")");
							lines.append("\n\r");
						}
					}
				}
			}
			mob.tell(lines.toString()+"^.");
		}
		else
		{
			int adjust=Util.s_int(Util.combine(commands,1));
			DVector levelsVec=new DVector(2);
			DVector mobsVec=new DVector(2);
			DVector alignVec=new DVector(2);
			int moblevel=mob.envStats().level()+adjust;
			for(Enumeration a=CMMap.areas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				if(Sense.canAccess(mob,A))
				{
					int median=A.getAreaIStats()[Area.AREASTAT_MEDLEVEL];
					int medianDiff=0;
					int upperLimit=moblevel/3;
					if((median<(moblevel+upperLimit))
					&&((median>=(moblevel-5))))
					{
						if(mob.envStats().level()>=median)
							medianDiff=(int)Math.round(9.0*Util.div(median,moblevel));
						else
							medianDiff=(int)Math.round(10.0*Util.div(moblevel,median));
					}
					whereAdd(levelsVec,A.name(),medianDiff);

					whereAdd(mobsVec,A.name(),A.getAreaIStats()[Area.AREASTAT_POPULATION]);

					int align=A.getAreaIStats()[Area.AREASTAT_MEDALIGN];
					int alignDiff=((int)Math.abs(new Integer(mob.getAlignment()-align).doubleValue()));
					whereAdd(alignVec,A.name(),alignDiff);
				}
			}
			StringBuffer msg=new StringBuffer("You are currently in: ^H"+mob.location().getArea().name()+"^?\n\r");
			DVector scores=new DVector(2);
			for(Enumeration a=CMMap.areas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				if(Sense.canAccess(mob,A))
				{
					int index=levelsVec.getIndex(A.name());
					if(index>=0)
					{
						Integer I=(Integer)levelsVec.elementAt(index,2);
						if((I!=null)&&(I.intValue()!=0))
						{
							int score=(index+1);
							index=mobsVec.getIndex(A.name());
							if(index>=0)
								score+=(index+1);

							index=alignVec.getIndex(A.name());
							if(index>=0)
								score+=(index+1);
							whereAdd(scores,A.name(),score);
						}
					}
				}
			}
			msg.append("\n\r^HThe best areas for you to try appear to be: ^?\n\r");
			for(int i=scores.size()-1;((i>=0)&&(i>=(scores.size()-10)));i--)
				msg.append(((String)scores.elementAt(i,1))+"\n\r");
			msg.append("\n\r\n\r^HEnter 'HELP (AREA NAME) for more information.^?");
			if(!mob.isMonster())
				mob.session().colorOnlyPrintln(msg.toString());
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
