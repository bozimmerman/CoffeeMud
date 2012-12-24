package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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

import java.util.*;


/* 
   Copyright 2000-2012 Bo Zimmerman

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
public class CMLister extends StdLibrary implements ListingLibrary
{
	public String ID(){return "CMLister";}
	public String itemSeenString(MOB viewerM, 
								 Environmental item, 
								 boolean useName, 
								 boolean longLook,
								 boolean sysmsgs)
	{
		if(useName)
			return CMStrings.capitalizeFirstLetter(item.name())+(sysmsgs?" ^H("+CMClass.classID(item)+")^N":"");
		else
		if((longLook)&&(item instanceof Item)&&(((Item)item).container()!=null))
			return CMStrings.capitalizeFirstLetter("     "+item.name())+(sysmsgs?" ^H("+CMClass.classID(item)+")^N":"");
		else
		if(!item.name().equals(item.Name()))
			return CMStrings.capitalizeFirstLetter(item.name()+" is here.")+(sysmsgs?" ^H("+CMClass.classID(item)+")^N":"");
		else
		if(item instanceof MOB)
			return CMStrings.capitalizeFirstLetter(((MOB)item).displayText(viewerM))+(sysmsgs?" ^H("+CMClass.classID(item)+")^N":"");
		else
		if(item.displayText().length()>0)
			return CMStrings.capitalizeFirstLetter(item.displayText())+(sysmsgs?" ^H("+CMClass.classID(item)+")^N":"");
		else
			return CMStrings.capitalizeFirstLetter(item.name())+(sysmsgs?" ^H("+CMClass.classID(item)+")^N":"");
	}
	
	public int getReps(MOB viewerM, 
					   Environmental item, 
					   List<? extends Environmental> theRest, 
					   boolean useName, 
					   boolean longLook)
	{
		String str=itemSeenString(viewerM,item,useName,longLook,false);
		String str2=null;
		int reps=0;
		int here=0;
		Environmental item2=null;
		while(here<theRest.size())
		{
			item2=(Environmental)theRest.get(here);
			str2=itemSeenString(viewerM,item2,useName,longLook,false);
			if(str2.length()==0)
				theRest.remove(item2);
			else
			if((str.equals(str2))
			&&(item instanceof Physical)
			&&(item2 instanceof Physical)
			&&(CMLib.flags().seenTheSameWay(viewerM,(Physical)item,(Physical)item2)))
			{
				reps++;
				theRest.remove(item2);
			}
			else
				here++;
		}
		return reps;
	}
	
	public void appendReps(int reps, StringBuilder say, boolean compress)
	{
		if(compress)
		{
			if(reps>0) 
				say.append("("+(reps+1)+") ");
		}
		else
		if(reps==0) say.append("      ");
		else
		if(reps>=99)
			say.append("("+CMStrings.padLeftPreserve(""+(reps+1),3)+") ");
		else
		if(reps>0)
			say.append(" ("+CMStrings.padLeftPreserve(""+(reps+1),2)+") ");
	}
	
	public String summarizeTheRest(MOB viewerM, List<? extends Environmental> things, boolean compress) 
	{
		Vector<String> restV=new Vector<String>();
		Item I=null;
		String name="";
		boolean otherItemsHere=false;
		for(int v=0;v<things.size();v++)
		{
			I=(Item)things.get(v);
			if(CMLib.flags().canBeSeenBy(I,viewerM)&&(I.displayText().length()>0))
			{
				name=CMLib.materials().genericType(I).toLowerCase();
				if(name.startsWith("item"))
				{
					if(!otherItemsHere)
						otherItemsHere=true;
				}
				else
				if(!restV.contains(name))
					restV.addElement(name);
			}
		}
		if((restV.size()==0)&&(!otherItemsHere)) return "";
		if(otherItemsHere) restV.addElement("other");
		StringBuilder theRest=new StringBuilder("");
		for(int o=0;o<restV.size();o++)
		{
			theRest.append(restV.elementAt(o));
			if(o<restV.size()-1)
				theRest.append(", ");
			if((restV.size()>1)&&(o==(restV.size()-2)))
				theRest.append("and ");
		}
		return "^IThere are also "+theRest.toString()+" items here.^N"+(compress?"":"\n\r");
	}
	
	public StringBuilder lister(MOB viewerM, 
								List<? extends Environmental> items,
								boolean useName, 
								String tag,
								String tagParm,
								boolean longLook,
								boolean compress)
	{
		boolean nameTagParm=((tagParm!=null)&&(tagParm.indexOf('*')>=0));
		StringBuilder say=new StringBuilder("");
		Environmental item=null;
		boolean sysmsgs=(viewerM!=null)?CMath.bset(viewerM.getBitmap(),MOB.ATT_SYSOPMSGS):false;
		int numShown=0;
		int maxToShow=CMProps.getIntVar(CMProps.SYSTEMI_MAXITEMSHOWN);
		while(items.size()>0)
		{
			if((maxToShow>0)&&(!longLook)&&(!sysmsgs)&&(!useName)&&(numShown>=maxToShow))
			{
				say.append(summarizeTheRest(viewerM,items,compress));
				items.clear();
				break;
			}
			item=items.get(0);
			items.remove(item);
			int reps=getReps(viewerM,item,items,useName,longLook);
			if(CMLib.flags().canBeSeenBy(item,viewerM)
			&&((item.displayText().length()>0)
				||sysmsgs
				||useName))
			{
				numShown++;
				appendReps(reps,say,compress);
				if((!compress)&&(viewerM!=null)&&(!viewerM.isMonster())&&(viewerM.session().clientTelnetMode(Session.TELNET_MXP)))
					say.append(CMProps.mxpImage(item," H=10 W=10",""," "));
				say.append("^I");
				
				if(tag!=null)
				{
					if(nameTagParm)
						say.append("^<"+tag+CMStrings.replaceAll(tagParm,"*",CMStrings.removeColors(item.name()))+"^>");
					else
						say.append("^<"+tag+tagParm+"^>");
				}
				if((compress)&&(item instanceof Physical)) 
					say.append(CMLib.flags().colorCodes((Physical)item,viewerM)+"^I");
				say.append(itemSeenString(viewerM,item,useName,longLook,sysmsgs));
				if(tag!=null)
					say.append("^</"+tag+"^>");
				if((!compress)&&(item instanceof Physical)) 
					say.append(CMLib.flags().colorCodes((Physical)item,viewerM)+"^N\n\r");
				else 
					say.append("^N");
				
				if((longLook)
				&&(item instanceof Container)
				&&(((Container)item).container()==null)
				&&(((Container)item).isOpen())
				&&(!((Container)item).hasALid())
				&&(!CMLib.flags().canBarelyBeSeenBy(item,viewerM)))
				{
					List<Item> V=new Vector<Item>();
					V.addAll(((Container)item).getContents());
					Item item2=null;
					if(compress&&V.size()>0) say.append("{");
					while(V.size()>0)
					{
						item2=(Item)V.get(0);
						V.remove(0);
						int reps2=getReps(viewerM,item2,V,useName,false);
						if(CMLib.flags().canBeSeenBy(item2,viewerM)
						&&((item2.displayText().length()>0)
							||sysmsgs
							||(useName)))
						{
							if(!compress) say.append("      ");
							appendReps(reps2,say,compress);
							if((!compress)&&(viewerM!=null)&&(!viewerM.isMonster())&&(viewerM.session().clientTelnetMode(Session.TELNET_MXP)))
								say.append(CMProps.mxpImage(item," H=10 W=10",""," "));
							say.append("^I");
							if(compress)say.append(CMLib.flags().colorCodes(item2,viewerM)+"^I");
							say.append(CMStrings.endWithAPeriod(itemSeenString(viewerM,item2,useName,longLook,sysmsgs)));
							if(!compress) 
								say.append(CMLib.flags().colorCodes(item2,viewerM)+"^N\n\r");
							else
								say.append("^N");
						}
						if(compress&&(V.size()==0)) say.append("} ");
					}
				}
			}
		}
		return say;
	}
	
	public StringBuilder reallyList(MOB viewerM, Map<String,? extends Object> these, int ofType)
	{
		return reallyList(viewerM,these,ofType,null);
	}
	public StringBuilder reallyList(MOB viewerM, Map<String,? extends Object> these)
	{
		return reallyList(viewerM,these,-1,null);
	}
	public StringBuilder reallyList(MOB viewerM, Map<String,? extends Object> these, Room likeRoom)
	{
		return reallyList(viewerM,these,-1,likeRoom);
	}
	public StringBuilder reallyList(MOB viewerM, Vector<? extends Object> these, int ofType)
	{
		return reallyList(viewerM,these.elements(),ofType,null);
	}
	public StringBuilder reallyList(MOB viewerM, Enumeration<? extends Object> these, int ofType)
	{
		return reallyList(viewerM,these,ofType,null);
	}
	public StringBuilder reallyList(MOB viewerM, Vector<? extends Object> these)
	{
		return reallyList(viewerM,these.elements(),-1,null);
	}
	public StringBuilder reallyList(MOB viewerM, Enumeration<? extends Object> these)
	{
		return reallyList(viewerM,these,-1,null);
	}
	public StringBuilder reallyList(MOB viewerM, Vector<? extends Object> these, Room likeRoom)
	{
		return reallyList(viewerM,these.elements(),-1,likeRoom);
	}
	public StringBuilder reallyList(MOB viewerM, Map<String,? extends Object> these, int ofType, Room likeRoom)
	{
		StringBuilder lines=new StringBuilder("");
		if(these.size()==0) return lines;
		int column=0;
		int COL_LEN=ListingLibrary.ColFixer.fixColWidth(24.0, viewerM);
		for(String key : these.keySet())
		{
			Object thisThang=these.get(key);
			String list=null;
			if(thisThang instanceof String)
				list=(String)thisThang;
			else
			if(thisThang instanceof Ability)
				list=((Ability)thisThang).ID()+(((Ability)thisThang).isGeneric()?"*":"");
			else
			if(thisThang instanceof CharClass)
				list=((CharClass)thisThang).ID()+(((CharClass)thisThang).isGeneric()?"*":"");
			else
			if(thisThang instanceof Race)
				list=((Race)thisThang).ID()+(((Race)thisThang).isGeneric()?"*":"");
			else
				list=CMClass.classID(thisThang);
			if(ofType>=0)
			{
				if((thisThang!=null)&&(thisThang instanceof Ability))
				{
					if((((Ability)thisThang).classificationCode()&Ability.ALL_ACODES)!=ofType)
						list=null;
				}
			}
			if((likeRoom!=null)&&(thisThang instanceof Room))
			{
				if((((Room)thisThang).roomID().length()>0)&&(!((Room)thisThang).getArea().Name().equals(likeRoom.getArea().Name())))
				   list=null;
			}
			if(list!=null)
			{
				if(++column>3)
				{
					lines.append("\n\r");
					column=1;
				}
				lines.append(CMStrings.padRight(list,COL_LEN)+" ");
			}
		}
		lines.append("\n\r");
		return lines;
	}

	public StringBuilder reallyList(MOB viewerM, Vector<? extends Object> these, int ofType, Room likeRoom)
	{ return reallyList(viewerM,these.elements(),ofType,likeRoom);}
	public StringBuilder reallyList(MOB viewerM, Enumeration<? extends Object> these, Room likeRoom)
	{ return reallyList(viewerM,these,-1,likeRoom);}
	public StringBuilder reallyList(MOB viewerM, Enumeration<? extends Object> these, int ofType, Room likeRoom)
	{
		StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements()) return lines;
		int column=0;
		int COL_LEN=ListingLibrary.ColFixer.fixColWidth(24.0, viewerM);
		for(Enumeration<? extends Object> e=these;e.hasMoreElements();)
		{
			Object thisThang=e.nextElement();
			String list=null;
			if(thisThang instanceof String)
				list=(String)thisThang;
			else
			if(thisThang instanceof Ability)
				list=((Ability)thisThang).ID()+(((Ability)thisThang).isGeneric()?"*":"");
			else
			if(thisThang instanceof CharClass)
				list=((CharClass)thisThang).ID()+(((CharClass)thisThang).isGeneric()?"*":"");
			else
			if(thisThang instanceof Race)
				list=((Race)thisThang).ID()+(((Race)thisThang).isGeneric()?"*":"");
			else
				list=CMClass.classID(thisThang);
			if(ofType>=0)
			{
				if((thisThang!=null)&&(thisThang instanceof Ability))
				{
					if((((Ability)thisThang).classificationCode()&Ability.ALL_ACODES)!=ofType)
						list=null;
				}
			}
			if((likeRoom!=null)&&(thisThang instanceof Room))
			{
				if((((Room)thisThang).roomID().length()>0)&&(!((Room)thisThang).getArea().Name().equals(likeRoom.getArea().Name())))
				   list=null;
			}
			if(list!=null)
			{
				if(++column>3)
				{
					lines.append("\n\r");
					column=1;
				}
				lines.append(CMStrings.padRight(list,COL_LEN)+" ");
			}
		}
		lines.append("\n\r");
		return lines;
	}
	public StringBuilder reallyList2Cols(MOB viewerM, Enumeration<? extends Object> these, int ofType, Room likeRoom)
	{
		StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements()) return lines;
		int column=0;
		int COL_LEN=ListingLibrary.ColFixer.fixColWidth(37.0, viewerM);
		for(Enumeration<? extends Object> e=these;e.hasMoreElements();)
		{
			Object thisThang=e.nextElement();
			String list=null;
			if(thisThang instanceof String)
				list=(String)thisThang;
			else
			if(thisThang instanceof Ability)
				list=((Ability)thisThang).ID()+(((Ability)thisThang).isGeneric()?"*":"");
			else
			if(thisThang instanceof CharClass)
				list=((CharClass)thisThang).ID()+(((CharClass)thisThang).isGeneric()?"*":"");
			else
			if(thisThang instanceof Race)
				list=((Race)thisThang).ID()+(((Race)thisThang).isGeneric()?"*":"");
			else
				list=CMClass.classID(thisThang);
			if(ofType>=0)
			{
				if((thisThang!=null)&&(thisThang instanceof Ability))
				{
					if((((Ability)thisThang).classificationCode()&Ability.ALL_ACODES)!=ofType)
						list=null;
				}
			}
			if((likeRoom!=null)&&(thisThang instanceof Room))
			{
				if((((Room)thisThang).roomID().length()>0)&&(!((Room)thisThang).getArea().Name().equals(likeRoom.getArea().Name())))
				   list=null;
			}
			if(list!=null)
			{
				if(++column>2)
				{
					lines.append("\n\r");
					column=1;
				}
				lines.append(CMStrings.padRight(list,COL_LEN)+" ");
			}
		}
		lines.append("\n\r");
		return lines;
	}
	
	public StringBuilder fourColumns(MOB viewerM, List<String> reverseList)
	{ return fourColumns(viewerM,reverseList,null);}
	public StringBuilder fourColumns(MOB viewerM, List<String> reverseList, String tag)
	{ return makeColumns(viewerM,reverseList,tag,4);}
	public StringBuilder makeColumns(MOB viewerM, List<String> reverseList, String tag, int numCols)
	{
		StringBuilder topicBuffer=new StringBuilder("");
		int col=0;
		String s=null;
		int colSize = ListingLibrary.ColFixer.fixColWidth(72.0,viewerM) / numCols;
		for(int i=0;i<reverseList.size();i++)
		{
			if((++col)>numCols)
			{
				topicBuffer.append("\n\r");
				col=1;
			}
			s=(String)reverseList.get(i);
			if((tag!=null)&&(tag.length()>0))
				s="^<"+tag+"^>"+s+"^</"+tag+"^>";
			if(s.length()>colSize)
			{
				if(col == numCols) topicBuffer.append("\n\r");
				topicBuffer.append(CMStrings.padRight(s,(colSize*2)+1)+" ");
				++col;
			}
			else
				topicBuffer.append(CMStrings.padRight(s,colSize)+" ");
		}
		return topicBuffer;
	}
}
