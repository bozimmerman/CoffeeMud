package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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

	public boolean canShowTo(MOB showTo, MOB show)
	{
	    if((show!=null)
	    &&(show.session()!=null)
	    &&(showTo!=null)
		&&((show.envStats().disposition()&EnvStats.IS_CLOAKED)==0)
			||((CMSecurity.isAllowedAnywhere(showTo,"CLOAK")||CMSecurity.isAllowedAnywhere(showTo,"WIZINV"))&&(showTo.envStats().level()>=show.envStats().level())))
			return true;
		return false;
	}
	
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(CMSecurity.isAllowed(mob,mob.location(),"WHERE"))
		{
			StringBuffer lines=new StringBuffer("^x");
			lines.append(Util.padRight("Name",17)+"| ");
			lines.append(Util.padRight("Location",17)+"^.^N\n\r");
			String who=Util.combine(commands,1);
			if(who.length()==0)
			{
				for(int s=0;s<Sessions.size();s++)
				{
					Session thisSession=Sessions.elementAt(s);
					MOB mob2=thisSession.mob();
					if(canShowTo(mob,mob2))
					{
						lines.append("^!"+Util.padRight(mob2.Name(),17)+"^?| ");
						if(thisSession.mob().location() != null )
						{
							lines.append(thisSession.mob().location().displayText());
							lines.append(" ("+CMMap.getExtendedRoomID(thisSession.mob().location())+")");
						}
						else
							lines.append("^!(no location)^?");
						lines.append("\n\r");
					}
					else
					{
						lines.append(Util.padRight("NAMELESS",17)+"| ");
						lines.append("NOWHERE");
						lines.append("\n\r");
					}
				}
			}
			else
			{
				
				Enumeration r=CMMap.rooms();
				if(who.toUpperCase().startsWith("AREA "))
				{
					r=mob.location().getArea().getProperMap();
					who=who.substring(5).trim();
				}
				boolean mobOnly=false;
				boolean itemOnly=false;
				boolean roomOnly=false;
				if((who.toUpperCase().startsWith("ROOM "))
				||(who.toUpperCase().startsWith("ROOMS ")))
				{
					roomOnly=true;
					who=who.substring(5).trim();
				}
				else
				if((who.toUpperCase().startsWith("ITEM "))
				||(who.toUpperCase().startsWith("ITEMS ")))
				{
					itemOnly=true;
					who=who.substring(5).trim();
				}
				else
				if((who.toUpperCase().startsWith("MOB "))
				||(who.toUpperCase().startsWith("MOBS ")))
				{
					mobOnly=true;
					who=who.substring(4).trim();
				}
				for(;r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if((R!=null)&&(CMSecurity.isAllowed(mob,R,"WHERE")))
					{
						if((!mobOnly)&&(!itemOnly))
							if(EnglishParser.containsString(R.displayText(),who)
							||EnglishParser.containsString(R.description(),who))
							{
								lines.append("^!"+Util.padRight("*",17)+"^?| ");
								lines.append(R.roomTitle());
								lines.append(" ("+R.roomID()+")");
								lines.append("\n\r");
							}
						if((!mobOnly)&&(!roomOnly))
							for(int i=0;i<R.numItems();i++)
							{
								Item I=R.fetchItem(i);
								if((EnglishParser.containsString(I.name(),who))
								||(EnglishParser.containsString(I.displayText(),who))
								||(EnglishParser.containsString(I.description(),who)))
								{
									lines.append("^!"+Util.padRight(I.name(),17)+"^?| ");
									lines.append(R.roomTitle());
									lines.append(" ("+R.roomID()+")");
									lines.append("\n\r");
								}
							}
						for(int m=0;m<R.numInhabitants();m++)
						{
							MOB M=R.fetchInhabitant(m);
						    if((M!=null)&&((M.isMonster())||(canShowTo(mob,M))))
						    {
								if((!itemOnly)&&(!roomOnly))
									if((EnglishParser.containsString(M.name(),who))
									||(EnglishParser.containsString(M.displayText(),who))
									||(EnglishParser.containsString(M.description(),who)))
									{
										lines.append("^!"+Util.padRight(M.name(),17)+"^?| ");
										lines.append(R.roomTitle());
										lines.append(" ("+R.roomID()+")");
										lines.append("\n\r");
									}
								if((!mobOnly)&&(!roomOnly))
								{
									for(int i=0;i<M.inventorySize();i++)
									{
										Item I=M.fetchInventory(i);
										if((EnglishParser.containsString(I.name(),who))
										||(EnglishParser.containsString(I.displayText(),who))
										||(EnglishParser.containsString(I.description(),who)))
										{
											lines.append("^!"+Util.padRight(I.name(),17)+"^?| ");
											lines.append("INV: "+M.name());
											lines.append(" ("+R.roomID()+")");
											lines.append("\n\r");
											break;
										}
									}
									ShopKeeper SK=CoffeeUtensils.getShopKeeper(M);
									Vector V=(SK!=null)?SK.getUniqueStoreInventory():null;
									if(V!=null)
									for(int i=0;i<V.size();i++)
									{
										Environmental E=(Environmental)V.elementAt(i);
										if((EnglishParser.containsString(E.name(),who))
										||(EnglishParser.containsString(E.displayText(),who))
										||(EnglishParser.containsString(E.description(),who)))
										{
											lines.append("^!"+Util.padRight(E.name(),17)+"^?| ");
											lines.append("SHOP: "+M.name());
											lines.append(" ("+R.roomID()+")");
											lines.append("\n\r");
											break;
										}
									}
								}
						    }
						}
					}
				}
			}
			mob.tell(lines.toString()+"^.");
		}
		else
		{
			int alignment=mob.getAlignment();
			for(int i=commands.size()-1;i>=0;i--)
			{
				String s=(String)commands.elementAt(i);
				if(s.equalsIgnoreCase("good"))
				{
					alignment=0;
					commands.removeElementAt(i);
				}
				else
				if(s.equalsIgnoreCase("neutral"))
				{
					alignment=500;
					commands.removeElementAt(i);
				}
				else
				if(s.equalsIgnoreCase("evil"))
				{
					alignment=1000;
					commands.removeElementAt(i);
				}
			}
												 
			int adjust=Util.s_int(Util.combine(commands,1));
			DVector levelsVec=new DVector(2);
			DVector mobsVec=new DVector(2);
			DVector alignVec=new DVector(2);
			int moblevel=mob.envStats().level()+adjust;
			for(Enumeration a=CMMap.areas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				if((Sense.canAccess(mob,A))&&(A.getAreaIStats()!=null))
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
					int alignDiff=((int)Math.abs(new Integer(alignment-align).doubleValue()));
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
