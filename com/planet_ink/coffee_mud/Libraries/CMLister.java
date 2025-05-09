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
   Copyright 2004-2025 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "CMLister";
	}

	@SuppressWarnings("unchecked")
	protected static final Filterer<Object>[]	NO_FILTER	= new Filterer[0];

	protected static final ListStringer stringer=new ListStringer()
	{
		@Override
		public String stringify(final Object o)
		{
			if(o instanceof String)
				return (String)o;
			else
			if(o instanceof Ability)
				return ((Ability)o).ID()+(((Ability)o).isGeneric()?"*":"");
			else
			if(o instanceof CharClass)
				return ((CharClass)o).ID()+(((CharClass)o).isGeneric()?"*":"");
			else
			if(o instanceof Race)
				return ((Race)o).ID()+(((Race)o).isGeneric()?"*":"");
			else
				return CMClass.classID(o);
		}
	};

	protected static class LikeRoomFilter implements Filterer<Object>
	{
		private final Room likeRoom;
		public LikeRoomFilter(final Room R)
		{
			likeRoom=R;
		}

		@Override
		public boolean passesFilter(final Object obj)
		{
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
		private final int ofType;
		private final int ofDomain;
		public AbilityTypeFilter(final int typ)
		{
			ofType=(typ<0)?-1:(typ&Ability.ALL_ACODES);
			ofDomain=(typ<=0)?0:(typ&Ability.ALL_DOMAINS);
		}

		@Override
		public boolean passesFilter(final Object obj)
		{
			if((ofType>=0)&&(ofType!=Ability.ALL_ACODES))
			{
				if(obj instanceof Ability)
				{
					if((((Ability)obj).classificationCode()&Ability.ALL_ACODES)!=ofType)
						return false;
				}
			}
			if(ofDomain>0)
			{
				if(obj instanceof Ability)
				{
					if((((Ability)obj).classificationCode()&Ability.ALL_DOMAINS)!=ofDomain)
						return false;
				}
			}
			return true;
		}
	}

	@Override
	public ListStringer getListStringer()
	{
		return CMLister.stringer;
	}

	protected String itemSeenString(final MOB viewerM, final Environmental item, final boolean useName, final boolean longLook,
									final boolean sysmsgs, final boolean compress)
	{
		final String seen;
		if(useName)
		{
			if(item instanceof Physical)
				seen = CMStrings.capitalizeFirstLetter(((Physical)item).name(viewerM))+(sysmsgs?" ^H("+CMClass.classID(item)+")^N":"");
			else
				seen = CMStrings.capitalizeFirstLetter(item.name())+(sysmsgs?" ^H("+CMClass.classID(item)+")^N":"");
		}
		else
		if((longLook)&&(item instanceof Item)&&(((Item)item).container()!=null))
			seen = CMStrings.capitalizeFirstLetter("     "+((Item)item).name(viewerM))+(sysmsgs?" ^H("+CMClass.classID(item)+")^N":"");
		else
		if(!item.name().equals(item.Name()))
		{
			if(item instanceof Physical)
				seen = CMStrings.capitalizeFirstLetter(L("@x1 is here.",((Physical)item).name(viewerM)))+(sysmsgs?" ^H("+CMClass.classID(item)+")^N":"");
			else
				seen = CMStrings.capitalizeFirstLetter(L("@x1 is here.",item.name()))+(sysmsgs?" ^H("+CMClass.classID(item)+")^N":"");
		}
		else
		if(item.displayText().length()>0)
		{
			if(compress)
			{
				if(item instanceof Physical)
					seen = CMStrings.capitalizeFirstLetterAndEndSentence(((Physical)item).displayText(viewerM))+(sysmsgs?" ^H("+CMClass.classID(item)+")^N":"");
				else
					seen = CMStrings.capitalizeFirstLetterAndEndSentence(item.displayText())+(sysmsgs?" ^H("+CMClass.classID(item)+")^N":"");
			}
			else
			{
				if(item instanceof Physical)
					seen = CMStrings.capitalizeFirstLetter(((Physical)item).displayText(viewerM))+(sysmsgs?" ^H("+CMClass.classID(item)+")^N":"");
				else
					seen = CMStrings.capitalizeFirstLetter(item.displayText())+(sysmsgs?" ^H("+CMClass.classID(item)+")^N":"");
			}
		}
		else
			seen = CMStrings.capitalizeFirstLetter(item.name())+(sysmsgs?" ^H("+CMClass.classID(item)+")^N":"");
		return seen;
	}

	protected int getReps(final MOB viewerM,
						  final Environmental item,
						  final List<? extends Environmental> theRest,
						  final boolean useName,
						  final boolean longLook)
	{
		final String str=itemSeenString(viewerM,item,useName,longLook,false, false);
		String str2=null;
		int reps=0;
		int here=0;
		Environmental item2=null;
		while(here<theRest.size())
		{
			item2=theRest.get(here);
			str2=itemSeenString(viewerM,item2,useName,longLook,false, false);
			if(str2.length()==0)
				theRest.remove(item2);
			else
			if((str.equals(str2))
			&&(item instanceof Physical)
			&&(item2 instanceof Physical)
			&&(CMLib.flags().isSeenTheSameWay(viewerM,(Physical)item,(Physical)item2)))
			{
				reps++;
				theRest.remove(item2);
			}
			else
				here++;
		}
		return reps;
	}

	protected void appendReps(final int reps, final StringBuilder say, final boolean compress)
	{
		if(compress)
		{
			if(reps>0)
				say.append("("+(reps+1)+") ");
		}
		else
		if(reps==0)
			say.append("      ");
		else
		if(reps>=99)
			say.append("("+CMStrings.padLeftPreserve(""+(reps+1),3)+") ");
		else
		if(reps>0)
			say.append(" ("+CMStrings.padLeftPreserve(""+(reps+1),2)+") ");
	}

	public String summarizeTheRest(final MOB viewerM, final List<? extends Environmental> things, final boolean compress)
	{
		final Vector<String> restV=new Vector<String>();
		Item I=null;
		String name="";
		boolean otherItemsHere=false;
		for(int v=0;v<things.size();v++)
		{
			I=(Item)things.get(v);
			if(CMLib.flags().canBeSeenBy(I,viewerM)&&(I.displayText(viewerM).length()>0))
			{
				name=CMLib.materials().getGeneralItemType(I).toLowerCase();
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
		if((restV.size()==0)&&(!otherItemsHere))
			return "";
		if(otherItemsHere)
			restV.addElement("other");
		final StringBuilder theRest=new StringBuilder("");
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

	@Override
	public String lister(final MOB viewerM,
						 final List<? extends Environmental> items,
						 final boolean useName,
						 final String tag,
						 final String tagParm,
						 final boolean longLook,
						 final boolean compress)
	{
		final boolean nameTagParm=((tagParm!=null)&&(tagParm.indexOf('*')>=0));
		final StringBuilder say=new StringBuilder("");
		Environmental item=null;
		final boolean sysmsgs=(viewerM!=null)?viewerM.isAttributeSet(MOB.Attrib.SYSOPMSGS):false;
		int numShown=0;
		final int maxToShow=CMProps.getIntVar(CMProps.Int.MAXITEMSHOWN);
		while(items.size()>0)
		{
			if((maxToShow>0)
			&&(!longLook)
			&&(!sysmsgs)
			&&(!useName)
			&&(numShown>=maxToShow))
			{
				say.append(summarizeTheRest(viewerM,items,compress));
				items.clear();
				break;
			}
			item=items.get(0);
			items.remove(item);
			final int reps=getReps(viewerM,item,items,useName,longLook);
			final String displayText=(item instanceof Physical)?((Physical)item).displayText(viewerM):item.displayText();
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
					say.append(CMLib.flags().getDispositionBlurbs((Physical)item,viewerM)+"^I");
				say.append(itemSeenString(viewerM,item,useName,longLook,sysmsgs, compress));
				if(tag!=null)
					say.append("^</"+tag+"^>");
				if((!compress)&&(item instanceof Physical))
					say.append(CMLib.flags().getDispositionBlurbs((Physical)item,viewerM)+"^N\n\r");
				else
				if(useName)
					say.append("^N, ");
				else
					say.append("^N");

				if((longLook)
				&&(item instanceof Container)
				&&(((Container)item).container()==null)
				&&(((Container)item).isOpen())
				&&(!((Container)item).hasADoor())
				&&(!CMLib.flags().canBarelyBeSeenBy(item,viewerM)))
				{
					final List<Item> V=new Vector<Item>();
					V.addAll(((Container)item).getContents());
					Item item2=null;
					if(compress&&V.size()>0)
						say.append("{");
					while(V.size()>0)
					{
						item2=V.get(0);
						V.remove(0);
						final int reps2=getReps(viewerM,item2,V,useName,false);
						if(CMLib.flags().canBeSeenBy(item2,viewerM)
						&&((item2.displayText(viewerM).length()>0)
							||sysmsgs
							||(useName)))
						{
							if(!compress)
								say.append("      ");
							appendReps(reps2,say,compress);
							if((!compress)&&(viewerM!=null)&&(!viewerM.isMonster())&&(viewerM.session().getClientTelnetMode(Session.TELNET_MXP)))
								say.append(CMLib.protocol().mxpImage(item," H=10 W=10",""," "));
							say.append("^I");
							if(compress)
								say.append(CMLib.flags().getDispositionBlurbs(item2,viewerM)+"^I");
							say.append(CMStrings.endWithAPeriod(itemSeenString(viewerM,item2,useName,longLook,sysmsgs, compress)));
							if(!compress)
								say.append(CMLib.flags().getDispositionBlurbs(item2,viewerM)+"^N\n\r");
							else
							if(useName)
								say.append("^N, ");
							else
								say.append("^N");
						}
						if(compress&&(V.size()==0))
							say.append("} ");
					}
				}
			}
		}
		if(useName && compress && say.length()>2 && say.substring(say.length()-2).equals(", "))
			say.delete(say.length()-2, say.length());
		return say.toString();
	}

	@SuppressWarnings("unchecked")
	protected Filterer<Object>[] buildOfTypeFilter(final int ofType)
	{
		return new Filterer[]{new AbilityTypeFilter(ofType)};
	}

	@SuppressWarnings("unchecked")
	protected Filterer<Object>[] buildLikeRoomFilter(final Room R)
	{
		return new Filterer[]{new LikeRoomFilter(R)};
	}

	@Override
	public String build3ColTable(final MOB viewerM, final Map<String, Ability> these, final int ofType)
	{
		return build3ColTable(viewerM,these,buildOfTypeFilter(ofType),stringer);
	}

	@Override
	public String build3ColTable(final MOB viewerM, final Map<String,? extends Object> these)
	{
		return build3ColTable(viewerM,these,NO_FILTER,stringer);
	}

	@Override
	public String build3ColTable(final MOB viewerM, final Map<String, Room> these, final Room likeRoom)
	{
		return build3ColTable(viewerM,these,buildLikeRoomFilter(likeRoom),stringer);
	}

	@Override
	public String build3ColTable(final MOB viewerM, final Vector<Ability> these, final int ofType)
	{
		return build3ColTable(viewerM,these.elements(),buildOfTypeFilter(ofType),stringer);
	}

	@Override
	public String build3ColTable(final MOB viewerM, final Enumeration<Ability> these, final int ofType)
	{
		return build3ColTable(viewerM,these,buildOfTypeFilter(ofType),stringer);
	}

	@Override
	public String build3ColTable(final MOB viewerM, final Vector<? extends Object> these)
	{
		return build3ColTable(viewerM,these.elements(),NO_FILTER,stringer);
	}

	@Override
	public String build3ColTable(final MOB viewerM, final Enumeration<? extends Object> these)
	{
		return build3ColTable(viewerM,these,NO_FILTER,stringer);
	}

	@Override
	public String build3ColTable(final MOB viewerM, final Vector<Room> these, final Room likeRoom)
	{
		return build3ColTable(viewerM,these.elements(),buildLikeRoomFilter(likeRoom),stringer);
	}

	@Override
	public String build3ColTable(final MOB viewerM, final Map<String,? extends Object> these, final Filterer<Object>[] filters, ListStringer stringer)
	{
		if(stringer==null)
			stringer=CMLister.stringer;
		final StringBuilder lines=new StringBuilder("");
		if(these.size()==0)
			return "";
		int column=0;
		final int COL_LEN=fixColWidth(24.0, viewerM);
		for(final String key : these.keySet())
		{
			final Object thisThang=these.get(key);
			String list=stringer.stringify(thisThang);
			if(filters!=null)
			{
				for(final Filterer<Object> F : filters)
				{
					if(!F.passesFilter(thisThang))
						list=null;
				}
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
		return lines.toString();
	}

	@Override
	public String build3ColTable(final MOB viewerM, final Vector<? extends Object> these, final Filterer<Object>[] filters, final ListStringer stringer)
	{
		return build3ColTable(viewerM,these.elements(),filters,stringer);
	}

	@Override
	public String build3ColTable(final MOB viewerM, final Enumeration<Room> these, final Room likeRoom)
	{
		return build3ColTable(viewerM,these,buildLikeRoomFilter(likeRoom),stringer);
	}

	@Override
	public String build3ColTable(final MOB viewerM, final Enumeration<? extends Object> these, final Filterer<Object>[] filters, ListStringer stringer)
	{
		if(stringer==null)
			stringer=CMLister.stringer;
		final StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements())
			return "";
		int column=0;
		final int COL_LEN=fixColWidth(24.0, viewerM);
		for(final Enumeration<? extends Object> e=these;e.hasMoreElements();)
		{
			final Object thisThang=e.nextElement();
			String list=stringer.stringify(thisThang);
			if(filters!=null)
			{
				for(final Filterer<Object> F : filters)
				{
					if(!F.passesFilter(thisThang))
						list=null;
				}
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
		return lines.toString();
	}

	@Override
	public String buildWikiList(final Enumeration<? extends CMObject> these, final int ofType)
	{
		return buildWikiList(these,buildOfTypeFilter(ofType),ofType != Ability.ACODE_PROPERTY);
	}

	@Override
	public String buildWikiList(final Enumeration<? extends CMObject> these, final Filterer<Object>[] filters, final boolean includeName)
	{
		final StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements())
			return "";
		for(final Enumeration<? extends Object> e=these;e.hasMoreElements();)
		{
			final Object thisObj=e.nextElement();
			if(thisObj instanceof CMObject)
			{
				final CMObject thisThang = (CMObject)thisObj;
				if(filters!=null)
				{
					boolean passes=true;
					for(final Filterer<Object> F : filters)
					{
						if(!F.passesFilter(thisThang))
							passes=false;
					}
					if(!passes)
						continue;
				}
				if(!includeName)
					lines.append("*[["+thisThang.ID()+"]]\n\r");
				else
					lines.append("*[["+thisThang.ID()+"|"+thisThang.name()+"]]\n\r");
			}
		}
		return lines.toString();
	}

	@Override
	public String build2ColTable(final MOB viewerM, final Enumeration<? extends Object> these)
	{
		return build2ColTable(viewerM, these, NO_FILTER, stringer);
	}

	@Override
	public String build2ColTable(final MOB viewerM, final Enumeration<? extends Object> these, final Filterer<Object>[] filters, ListStringer stringer)
	{
		if(stringer==null)
			stringer=CMLister.stringer;
		final StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements())
			return "";
		int column=0;
		final int COL_LEN=fixColWidth(37.0, viewerM);
		for(final Enumeration<? extends Object> e=these;e.hasMoreElements();)
		{
			final Object thisThang=e.nextElement();
			String list=stringer.stringify(thisThang);
			if(filters!=null)
			{
				for(final Filterer<Object> F : filters)
				{
					if(!F.passesFilter(thisThang))
						list=null;
				}
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
		return lines.toString();
	}

	@Override
	public String build4ColTable(final MOB viewerM, final List<String> reverseList)
	{
		return build4ColTable(viewerM,reverseList,null);
	}

	@Override
	public String build4ColTable(final MOB viewerM, final List<String> reverseList, final String tag)
	{
		return buildNColTable(viewerM,reverseList,tag,4);
	}

	@Override
	public String build3ColTable(final MOB viewerM, final List<String> reverseList)
	{
		return build3ColTable(viewerM,reverseList,null);
	}

	@Override
	public String build3ColTable(final MOB viewerM, final List<String> reverseList, final String tag)
	{
		return buildNColTable(viewerM,reverseList,tag,3);
	}

	@Override
	public int fixColWidth(final double colWidth, final MOB mob)
	{
		return fixColWidth(colWidth,(mob==null)?null:mob.session());
	}

	@Override
	public int fixColWidth(final double colWidth, final Session session)
	{
		double totalWidth=(session==null)?78.0:(double)session.getWrap();
		if(totalWidth==0.0)
			totalWidth=78.0;
		return (int)Math.round((colWidth/78.0)*totalWidth);
	}

	@Override
	public void fixColWidths(final int[] colWidths, final Session session)
	{
		double totalWidth=(session==null)?78.0:(double)session.getWrap();
		if(totalWidth==0.0)
			totalWidth=78.0;
		for(int i=0;i<colWidths.length;i++)
			colWidths[i] = (int)Math.round((colWidths[i]/78.0)*totalWidth);
	}

	@Override
	public int fixColWidth(final double colWidth, final double totalWidth)
	{
		return (int)Math.round((colWidth/78.0)*totalWidth);
	}

	@Override
	public String buildNColTable(final MOB viewerM, final List<String> reverseList, final String tag, final int numCols)
	{
		final StringBuilder topicBuffer=new StringBuilder("");
		int col=0;
		String s=null;
		final int colSize = fixColWidth(72.0,viewerM) / numCols;
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
				if(CMStrings.removeColors(s).length()>colSize)
				{
					if(col == numCols)
						topicBuffer.append("\n\r");
					topicBuffer.append(CMStrings.padRight(s,(colSize*2)+1)+" ");
					++col;
				}
				else
					topicBuffer.append(CMStrings.padRight(s,colSize)+" ");
			}
		}
		return topicBuffer.toString();
	}
}
