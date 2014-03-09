package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary.ListStringer;
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
   Copyright 2000-2014 Bo Zimmerman

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
	protected static final ListStringer stringer=new ListStringer();
	@SuppressWarnings("unchecked")
	protected static final Filterer<Object>[] NO_FILTER=new Filterer[0];
	protected static class LikeRoomFilter implements Filterer<Object>
	{
		private Room likeRoom;
		public LikeRoomFilter(Room R)
		{
			likeRoom=R;
		}
		@Override
		public boolean passesFilter(Object obj) {
			if((likeRoom!=null)&&(obj instanceof Room))
			{
				if((((Room)obj).roomID().length()>0)&&(!((Room)obj).getArea().Name().equals(likeRoom.getArea().Name())))
					return false;
			}
			return true;
		}
	}

	protected static class AbilityTypeFilter implements Filterer<Object>
	{
		private int ofType;
		public AbilityTypeFilter(int typ)
		{
			ofType=typ;
		}
		@Override
		public boolean passesFilter(Object obj) {
			if(ofType>=0)
			{
				if(obj instanceof Ability)
				{
					if((((Ability)obj).classificationCode()&Ability.ALL_ACODES)!=ofType)
						return false;
				}
			}
			return true;
		}
	}

	public String itemSeenString(MOB viewerM, Environmental item, boolean useName, boolean longLook, boolean sysmsgs)
	{
		if(useName)
		{
			if(item instanceof Physical)
				return CMStrings.capitalizeFirstLetter(((Physical)item).name(viewerM))+(sysmsgs?" ^H("+CMClass.classID(item)+")^N":"");
			else
				return CMStrings.capitalizeFirstLetter(item.name())+(sysmsgs?" ^H("+CMClass.classID(item)+")^N":"");
		}
		else
		if((longLook)&&(item instanceof Item)&&(((Item)item).container()!=null))
			return CMStrings.capitalizeFirstLetter("     "+((Item)item).name(viewerM))+(sysmsgs?" ^H("+CMClass.classID(item)+")^N":"");
		else
		if(!item.name().equals(item.Name()))
		{
			if(item instanceof Physical)
				return CMStrings.capitalizeFirstLetter(((Physical)item).name(viewerM)+" is here.")+(sysmsgs?" ^H("+CMClass.classID(item)+")^N":"");
			else
				return CMStrings.capitalizeFirstLetter(item.name()+" is here.")+(sysmsgs?" ^H("+CMClass.classID(item)+")^N":"");
		}
		else
		if(item.displayText().length()>0)
		{
			if(item instanceof Physical)
				return CMStrings.capitalizeFirstLetter(((Physical)item).displayText(viewerM))+(sysmsgs?" ^H("+CMClass.classID(item)+")^N":"");
			else
				return CMStrings.capitalizeFirstLetter(item.displayText())+(sysmsgs?" ^H("+CMClass.classID(item)+")^N":"");
		}
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
			item2=theRest.get(here);
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
			if(CMLib.flags().canBeSeenBy(I,viewerM)&&(I.displayText(viewerM).length()>0))
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
		int maxToShow=CMProps.getIntVar(CMProps.Int.MAXITEMSHOWN);
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
			String displayText=(item instanceof Physical)?((Physical)item).displayText(viewerM):item.displayText();
			if(CMLib.flags().canBeSeenBy(item,viewerM)
			&&((displayText.length()>0)
				||sysmsgs
				||useName))
			{
				numShown++;
				appendReps(reps,say,compress);
				if((!compress)&&(viewerM!=null)&&(!viewerM.isMonster())&&(viewerM.session().getClientTelnetMode(Session.TELNET_MXP)))
					say.append(CMLib.protocol().mxpImage(item," H=10 W=10",""," "));
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
						item2=V.get(0);
						V.remove(0);
						int reps2=getReps(viewerM,item2,V,useName,false);
						if(CMLib.flags().canBeSeenBy(item2,viewerM)
						&&((item2.displayText(viewerM).length()>0)
							||sysmsgs
							||(useName)))
						{
							if(!compress) say.append("      ");
							appendReps(reps2,say,compress);
							if((!compress)&&(viewerM!=null)&&(!viewerM.isMonster())&&(viewerM.session().getClientTelnetMode(Session.TELNET_MXP)))
								say.append(CMLib.protocol().mxpImage(item," H=10 W=10",""," "));
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
	
	@SuppressWarnings("unchecked")
	protected Filterer<Object>[] buildOfTypeFilter(int ofType)
	{
		return new Filterer[]{new AbilityTypeFilter(ofType)};
	}
	
	@SuppressWarnings("unchecked")
	protected Filterer<Object>[] buildLikeRoomFilter(Room R)
	{
		return new Filterer[]{new LikeRoomFilter(R)};
	}
	
	public StringBuilder reallyList(MOB viewerM, Map<String,? extends Object> these, int ofType)
	{
		return reallyList(viewerM,these,buildOfTypeFilter(ofType),stringer);
	}
	public StringBuilder reallyList(MOB viewerM, Map<String,? extends Object> these)
	{
		return reallyList(viewerM,these,NO_FILTER,stringer);
	}
	public StringBuilder reallyList(MOB viewerM, Map<String,? extends Object> these, Room likeRoom)
	{
		return reallyList(viewerM,these,buildLikeRoomFilter(likeRoom),stringer);
	}
	public StringBuilder reallyList(MOB viewerM, Vector<? extends Object> these, int ofType)
	{
		return reallyList(viewerM,these.elements(),buildOfTypeFilter(ofType),stringer);
	}
	public StringBuilder reallyList(MOB viewerM, Enumeration<? extends Object> these, int ofType)
	{
		return reallyList(viewerM,these,buildOfTypeFilter(ofType),stringer);
	}
	public StringBuilder reallyList(MOB viewerM, Vector<? extends Object> these)
	{
		return reallyList(viewerM,these.elements(),NO_FILTER,stringer);
	}
	public StringBuilder reallyList(MOB viewerM, Enumeration<? extends Object> these)
	{
		return reallyList(viewerM,these,NO_FILTER,stringer);
	}
	public StringBuilder reallyList(MOB viewerM, Vector<? extends Object> these, Room likeRoom)
	{
		return reallyList(viewerM,these.elements(),buildLikeRoomFilter(likeRoom),stringer);
	}
	public StringBuilder reallyList(MOB viewerM, Map<String,? extends Object> these, Filterer<Object>[] filters, ListStringer stringer)
	{
		if(stringer==null) stringer=CMLister.stringer;
		StringBuilder lines=new StringBuilder("");
		if(these.size()==0) return lines;
		int column=0;
		int COL_LEN=ListingLibrary.ColFixer.fixColWidth(24.0, viewerM);
		for(String key : these.keySet())
		{
			Object thisThang=these.get(key);
			String list=stringer.stringify(thisThang);
			if(filters!=null)
				for(Filterer<Object> F : filters)
					if(!F.passesFilter(thisThang))
						list=null;
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

	public StringBuilder reallyList(MOB viewerM, Vector<? extends Object> these, Filterer<Object>[] filters, ListStringer stringer)
	{ 
		return reallyList(viewerM,these.elements(),filters,stringer);
	}
	public StringBuilder reallyList(MOB viewerM, Enumeration<? extends Object> these, Room likeRoom)
	{ 
		return reallyList(viewerM,these,buildLikeRoomFilter(likeRoom),stringer);
	}
	public StringBuilder reallyList(MOB viewerM, Enumeration<? extends Object> these, Filterer<Object>[] filters, ListStringer stringer)
	{
		if(stringer==null) stringer=CMLister.stringer;
		StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements()) return lines;
		int column=0;
		int COL_LEN=ListingLibrary.ColFixer.fixColWidth(24.0, viewerM);
		for(Enumeration<? extends Object> e=these;e.hasMoreElements();)
		{
			Object thisThang=e.nextElement();
			String list=stringer.stringify(thisThang);
			if(filters!=null)
				for(Filterer<Object> F : filters)
					if(!F.passesFilter(thisThang))
						list=null;
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
	
	public StringBuilder reallyList2Cols(MOB viewerM, Enumeration<? extends Object> these)
	{
		return reallyList2Cols(viewerM, these, NO_FILTER, stringer);
	}
	
	public StringBuilder reallyList2Cols(MOB viewerM, Enumeration<? extends Object> these, Filterer<Object>[] filters, ListStringer stringer)
	{
		if(stringer==null) stringer=CMLister.stringer;
		StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements()) return lines;
		int column=0;
		int COL_LEN=ListingLibrary.ColFixer.fixColWidth(37.0, viewerM);
		for(Enumeration<? extends Object> e=these;e.hasMoreElements();)
		{
			Object thisThang=e.nextElement();
			String list=stringer.stringify(thisThang);
			if(filters!=null)
				for(Filterer<Object> F : filters)
					if(!F.passesFilter(thisThang))
						list=null;
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
	{ 
		return fourColumns(viewerM,reverseList,null);
	}
	
	public StringBuilder fourColumns(MOB viewerM, List<String> reverseList, String tag)
	{ 
		return makeColumns(viewerM,reverseList,tag,4);
	}
	
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
			s=reverseList.get(i);
			if((tag!=null)&&(tag.length()>0))
				s="^<"+tag+"^>"+s+"^</"+tag+"^>";
			if(s!=null)
			{
				if(s.length()>colSize)
				{
					if(col == numCols) topicBuffer.append("\n\r");
					topicBuffer.append(CMStrings.padRight(s,(colSize*2)+1)+" ");
					++col;
				}
				else
					topicBuffer.append(CMStrings.padRight(s,colSize)+" ");
			}
		}
		return topicBuffer;
	}
}
