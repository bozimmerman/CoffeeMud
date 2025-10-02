package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.http.MultiPartData;
import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.HTTPRedirectException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.Command;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.grinder.GrinderRooms;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;

/*
   Copyright 2002-2025 Bo Zimmerman

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
public class RoomData extends StdWebMacro
{
	@Override
	public String name()
	{
		return "RoomData";
	}
	static final String[][] STAT_CHECKS={
		{"DISPLAY","NAME"},
		{"CLASS","CLASSES"},
		{"DESCRIPTION","DESCRIPTION"},
		{"XSIZE","XGRID"},
		{"YSIZE","XGRID"},
		{"IMAGE","IMAGE"}
	};


	private static class RoomStuff
	{
		public Vector<MOB> inhabs=new Vector<MOB>();
		public Vector<Item> items=new Vector<Item>();
		public Vector<Ability> affects=new Vector<Ability>();
		public Vector<Behavior> behavs=new Vector<Behavior>();
		public RoomStuff()
		{
		}

		public RoomStuff(final Room R)
		{
			for(final Enumeration<MOB> a =R.inhabitants();a.hasMoreElements();)
			{
				final MOB M=a.nextElement();
				if(M!=null)
				{
					CMLib.catalog().updateCatalogIntegrity(M);
					inhabs.add(M);
				}
			}
			for(final Enumeration<Item> a =R.items();a.hasMoreElements();)
			{
				final Item I=a.nextElement();
				if(I!=null)
				{
					CMLib.catalog().updateCatalogIntegrity(I);
					items.add(I);
				}
			}
			for(final Enumeration<Ability> a =R.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if(A!=null)
					affects.add(A);
			}
			for(final Enumeration<Behavior> b=R.behaviors();b.hasMoreElements();)
			{
				final Behavior B=b.nextElement();
				if(B!=null)
					behavs.add(B);
			}
		}
	}

	public static int getNumFromWordNum(final String var)
	{
		int x=0;
		int l=var.length()-1;
		while((l>=0)&&(Character.isDigit(var.charAt(l))))
		{
			x=(x*10)+(var.charAt(l)-'0');
			l--;
		}
		return x;
	}

	public static Pair<String,String> findPair(final List<Pair<String,String>> fixtures, final String varStart, final String value)
	{
		Pair<String,String> p;
		for(int x=1; (p=getPair(fixtures,varStart+x))!=null;x++)
		{
			if((p.second==value)||((p.second!=null)&&(p.second.equalsIgnoreCase(value))))
				return p;
		}
		return null;
	}

	public static List<Pair<String,String>> findPairs(final List<Pair<String,String>> fixtures, final String varStart, final String value)
	{
		final List<Pair<String,String>> pairs=new LinkedList<Pair<String,String>>();
		Pair<String,String> p;
		for(int x=1; (p=getPair(fixtures,varStart+x))!=null;x++)
		{
			if((p.second==value)||((p.second!=null)&&(p.second.equalsIgnoreCase(value))))
				pairs.add(p);
		}
		return pairs;
	}

	public static Pair<String,String> getPair(final List<Pair<String,String>> fixtures, final String var)
	{
		for(final Pair<String,String> p : fixtures)
		{
			if(p.first.equalsIgnoreCase(var))
				return p;
		}
		return null;
	}

	public static String getPairValue(final List<Pair<String,String>> fixtures, final String var)
	{
		final Pair<String,String> p = getPair(fixtures,var);
		if(p==null)
			return null;
		return p.second;
	}

	public static List<Pair<String,String>> toPairs(final Map<String,String> map)
	{
		final LinkedList<Pair<String,String>> pairList=new LinkedList<Pair<String,String>>();
		for(final String key : map.keySet())
			pairList.add(new Pair<String,String>(key.trim(),map.get(key)));
		return pairList;

	}

	public RoomStuff makeRoomStuff(final Room R, final List<Pair<String,String>> fixtures)
	{
		final RoomStuff stuff=new RoomStuff();
		int x=1;
		String s=getPairValue(fixtures, "ITEM"+x);
		while(s!=null)
		{
			if(s.length()>0)
			{
				final Item I=CMLib.webMacroFilter().getItemFromWebCache(R, s);
				stuff.items.add(I);
			}
			x++;
			s=getPairValue(fixtures, "ITEM"+x);
		}
		s=getPairValue(fixtures, "MOB"+x);
		while(s!=null)
		{
			if(s.length()>0)
			{
				final MOB M=CMLib.webMacroFilter().getMOBFromWebCache(R, s);
				stuff.inhabs.add(M);
			}
			x++;
			s=getPairValue(fixtures, "MOB"+x);
		}
		s=getPairValue(fixtures, "AFFECT"+x);
		while(s!=null)
		{
			if(s.length()>0)
			{
				final Ability A=CMClass.getAbility(s);
				A.setMiscText(getPairValue(fixtures, "ADATA"+x));
				stuff.affects.add(A);
			}
			x++;
			s=getPairValue(fixtures, "AFFECT"+x);
		}
		s=getPairValue(fixtures, "BEHAV"+x);
		while(s!=null)
		{
			if(s.length()>0)
			{
				final Behavior B=CMClass.getBehavior(s);
				B.setParms(getPairValue(fixtures, "BDATA"+x));
				stuff.behavs.add(B);
			}
			x++;
			s=getPairValue(fixtures, "BEHAV"+x);
		}
		return stuff;
	}

	@SuppressWarnings("unchecked")
	public static Pair<String,String>[] makePairs(final RoomStuff stuff, final List<Pair<String,String>> fixtures)
	{
		CMLib.webMacroFilter().contributeItemsToWebCache(stuff.items);
		for(int i=0;i<stuff.items.size();i++)
		{
			final Item I=stuff.items.get(i);
			final Item I2=CMLib.webMacroFilter().findItemMatchInWebCache(I);
			final String code=""+I2;
			fixtures.add(new Pair<String,String>("ITEM"+(i+1), code));
			fixtures.add(new Pair<String,String>("ITEMWORN"+(i+1),""));
			String containerName="";
			if(I.container()!=null)
				containerName=CMLib.english().getContextName(stuff.items,I.container());
			fixtures.add(new Pair<String,String>("ITEMCONT"+(i+1),containerName));
		}
		fixtures.add(new Pair<String,String>("ITEM"+(stuff.items.size()+1),null));
		CMLib.webMacroFilter().contributeMOBsToWebCache(stuff.inhabs);
		for(int m=0;m<stuff.inhabs.size();m++)
		{
			final MOB M=stuff.inhabs.get(m);
			final MOB M2=CMLib.webMacroFilter().findMOBMatchInWebCache(M);
			final String code=""+M2;
			fixtures.add(new Pair<String,String>("MOB"+(m+1),code));
		}
		fixtures.add(new Pair<String,String>("MOB"+(stuff.inhabs.size()+1),null));
		for(int a=0;a<stuff.affects.size();a++)
		{
			final Ability A=stuff.affects.get(a);
			fixtures.add(new Pair<String,String>("AFFECT"+(a+1),A.ID()));
			fixtures.add(new Pair<String,String>("ADATA"+(a+1),A.text()));
		}
		fixtures.add(new Pair<String,String>("AFFECT"+(stuff.affects.size()+1),null));
		fixtures.add(new Pair<String,String>("ADATA"+(stuff.affects.size()+1),null));
		for(int b=0;b<stuff.behavs.size();b++)
		{
			final Behavior B=stuff.behavs.get(b);
			fixtures.add(new Pair<String,String>("BEHAV"+(b+1),B.ID()));
			fixtures.add(new Pair<String,String>("BDATA"+(b+1),B.getParms()));
		}
		fixtures.add(new Pair<String,String>("BEHAV"+(stuff.behavs.size()+1),null));
		fixtures.add(new Pair<String,String>("BDATA"+(stuff.behavs.size()+1),null));
		return fixtures.toArray(new Pair[0]);
	}

	public static Pair<String,String>[] makeMergableRoomFields(final HTTPRequest httpReq, Room R, final List<String> multiRoomList)
	{
		final List<Pair<String,String>> fixtures=new Vector<Pair<String,String>>();
		R=(Room)R.copyOf();
		final RoomStuff stuff=new RoomStuff(R);
		for(final String roomID : multiRoomList)
		{
			if(!roomID.equalsIgnoreCase(R.roomID()))
			{
				final Room R2=MUDGrinder.getRoomObject(httpReq, roomID);
				if(R2!=null)
				{
					CMLib.map().resetRoom(R2);
					for(final String[] set : STAT_CHECKS)
					{
						if(!R.getStat(set[0]).equalsIgnoreCase(R2.getStat(set[0])))
							fixtures.add(new Pair<String,String>(set[1].trim(), ""));
					}
					for(final Iterator<Ability> a=stuff.affects.iterator();a.hasNext();)
					{
						final Ability A=a.next();
						if((R2.fetchEffect(A.ID())==null)
						||(!R2.fetchEffect(A.ID()).text().equalsIgnoreCase(A.text())))
							a.remove();
					}
					for(final Iterator<Behavior> b=stuff.behavs.iterator();b.hasNext();)
					{
						final Behavior B=b.next();
						if((R2.fetchBehavior(B.ID())==null)
						||(!R2.fetchBehavior(B.ID()).getParms().equalsIgnoreCase(B.getParms())))
							b.remove();
					}
					final HashSet<MOB> checkedMobs=new HashSet<MOB>();
					for(final Iterator<MOB> m=stuff.inhabs.iterator();m.hasNext();)
					{
						final MOB M=m.next();
						boolean found=false;
						if((M!=null)
						&&(M.isSavable()||(!R2.isSavable())))
						{
							for(final Enumeration<MOB> m2 =R2.inhabitants();m2.hasMoreElements();)
							{
								final MOB M2=m2.nextElement();
								if(M2!=null)
								{
									if(!checkedMobs.contains(M2))
									{
										CMLib.catalog().updateCatalogIntegrity(M2);
										if((M2.isSavable()||(!R2.isSavable()))
										&&(M2.sameAs(M)))
										{
											found=true;
											checkedMobs.add(M2);
											break;
										}
									}
								}
							}
						}
						if((M!=null)&&(!found))
							m.remove();
					}
					final HashSet<Item> checkedItems=new HashSet<Item>();
					for(final Iterator<Item> i=stuff.items.iterator();i.hasNext();)
					{
						final Item I=i.next();
						boolean found=false;
						if((I!=null)
						&&(I.isSavable()||(!R2.isSavable())))
						{
							for(final Enumeration<Item> i2 =R2.items();i2.hasMoreElements();)
							{
								final Item I2=i2.nextElement();
								if(I2!=null)
								{
									if(!checkedItems.contains(I2))
									{
										CMLib.catalog().updateCatalogIntegrity(I2);
										if((I2.isSavable()||(!R2.isSavable()))
										&&(I2.sameAs(I)))
										{
											found=true;
											checkedItems.add(I2);
											break;
										}
									}
								}
							}
						}
						if((I!=null)&&(!found))
							i.remove();
					}
				}
			}
		}
		return makePairs(stuff,fixtures);
	}

	public static void mergeRoomField(final List<Pair<String,String>> currentRoomPairsList,
									  final List<Pair<String,String>> commonRoomsPairsList,
									  final List<Pair<String,String>> submittedRoomPairsList,
									  final String[] vars)
	{
		final HashSet<Pair<String,String>> foundSubmittedRoomPairsList=new HashSet<Pair<String,String>>();
		final HashSet<Pair<String,String>> foundCurrentRoomPairsList=new HashSet<Pair<String,String>>();
		for(final Pair<String,String> p : commonRoomsPairsList)
		{
			if(p.first.startsWith(vars[0]) && (p.first.length()>vars[0].length()) && Character.isDigit(p.first.charAt(vars[0].length())))
			{
				if(p.second==null)
					continue;
				final List<Pair<String,String>> mPs=findPairs(submittedRoomPairsList,vars[0],p.second);
				boolean found=false;
				for(final Pair<String,String> mP : mPs)
				{
					if(!foundSubmittedRoomPairsList.contains(mP))
					{
						if(vars.length==1)
						{
							found=true;
							foundSubmittedRoomPairsList.add(mP);
							break;
						}
						else
						for(int i=1;i<vars.length;i++)
						{
							final String setAData=getPairValue(commonRoomsPairsList,vars[i]+getNumFromWordNum(p.first));
							final String mergeAData=getPairValue(submittedRoomPairsList,vars[i]+getNumFromWordNum(mP.first));
							if(((setAData==null)&&(mergeAData==null))
							||((setAData!=null)&&(mergeAData!=null)&&(setAData.equalsIgnoreCase(mergeAData))))
							{
								found=true;
								foundSubmittedRoomPairsList.add(mP);
								break;
							}
						}
					}
					if(found)
					{
						break;
					}
				}
				Pair<String,String> foundActiveP=null;
				final List<Pair<String,String>> mP2s=findPairs(currentRoomPairsList,vars[0],p.second);
				for(final Pair<String,String> p2 : mP2s)
				{
					if(!foundCurrentRoomPairsList.contains(p2))
					{
						if(vars.length==1)
						{
							foundActiveP=p2;
							break;
						}
						else
						for(int i=1;i<vars.length;i++)
						{
							final String setAData=getPairValue(commonRoomsPairsList,vars[i]+getNumFromWordNum(p.first));
							final String activeAData=getPairValue(currentRoomPairsList,vars[i]+getNumFromWordNum(p2.first));
							if(((setAData==null)&&(activeAData==null))
							||((setAData!=null)&&(activeAData!=null)&&(setAData.equalsIgnoreCase(activeAData))))
							{
								foundActiveP=p2;
								break;
							}
						}
					}
					if(foundActiveP!=null)
						break;
				}
				if(foundActiveP!=null)
					foundCurrentRoomPairsList.add(foundActiveP);
				if(!found)
				{
					if(foundActiveP!=null)
					{
						foundActiveP.second=""; // effectively erases it.
					}
				}
			}
		}
		for(final Pair<String,String> p : submittedRoomPairsList)
		{
			if((!foundSubmittedRoomPairsList.contains(p)) && p.first.startsWith(vars[0]) && (p.first.length()>vars[0].length()) && Character.isDigit(p.first.charAt(vars[0].length())))
			{
				if((p.second==null)||(p.second.length()==0))
					continue;
				final List<Pair<String,String>> sPs=findPairs(currentRoomPairsList,vars[0],p.second);
				boolean found=false;
				for(final Pair<String,String> sP : sPs)
				{
					if(!foundCurrentRoomPairsList.contains(sP))
					{
						if(vars.length==1)
						{
							found=true;
							foundCurrentRoomPairsList.add(sP);
							break;
						}
						else
						for(int i=1;i<vars.length;i++)
						{
							final String setAData=getPairValue(currentRoomPairsList,vars[i]+getNumFromWordNum(sP.first));
							final String mergeAData=getPairValue(submittedRoomPairsList,vars[i]+getNumFromWordNum(p.first));
							if(((setAData==null)&&(mergeAData==null))
							||((setAData!=null)&&(mergeAData!=null)&&(!setAData.equalsIgnoreCase(mergeAData))))
							{
								found=true;
								foundCurrentRoomPairsList.add(sP);
								break;
							}
						}
					}
					if(found)
					{
						break;
					}
				}
				if(!found)
				{
					for(int x=1;;x++)
					{
						final Pair<String,String> editablePairHead=getPair(currentRoomPairsList,vars[0]+x);
						if(editablePairHead==null)
						{
							currentRoomPairsList.add(new Pair<String,String>(vars[0]+x,p.second));
							for(int i=1;i<vars.length;i++)
								currentRoomPairsList.add(new Pair<String,String>(vars[i]+x,getPairValue(submittedRoomPairsList,vars[i]+getNumFromWordNum(p.first))));
							break;
						}
						else
						if((editablePairHead.second==null)||(editablePairHead.second.trim().length()==0))
						{
							editablePairHead.second=p.second;
							for(int i=1;i<vars.length;i++)
							{
								final Pair<String,String> editableField=getPair(currentRoomPairsList,vars[i]+x);
								if(editableField==null)
									currentRoomPairsList.add(new Pair<String,String>(vars[i]+x,getPairValue(submittedRoomPairsList,vars[i]+getNumFromWordNum(p.first))));
								else
									editableField.second=getPairValue(submittedRoomPairsList,vars[i]+getNumFromWordNum(p.first));
							}
							break;
						}
					}
				}
			}
		}
	}

	public static HTTPRequest mergeRoomFields(final HTTPRequest httpReq, final Pair<String,String> setPairs[], Room R)
	{
		final Hashtable<String,String> mergeParams=new XHashtable<String,String>(httpReq.getUrlParametersCopy());
		final HTTPRequest mergeReq=new HTTPRequest()
		{
			public final Hashtable<String,String> params=mergeParams;

			@Override
			public String getHost()
			{
				return httpReq.getHost();
			}

			@Override
			public String getUrlPath()
			{
				return httpReq.getUrlPath();
			}

			@Override
			public String getFullRequest()
			{
				return httpReq.getFullRequest();
			}

			@Override
			public Map<String,String> getUrlParametersCopy()
			{
				return new XHashtable<String,String>(params);
			}

			@Override
			public String getUrlParameter(final String name)
			{
				return params.get(name.toLowerCase());
			}

			@Override
			public boolean isUrlParameter(final String name)
			{
				return params.containsKey(name.toLowerCase());
			}

			@Override
			public Set<String> getUrlParameters()
			{
				return params.keySet();
			}

			@Override
			public HTTPMethod getMethod()
			{
				return httpReq.getMethod();
			}

			@Override
			public String getHeader(final String name)
			{
				return httpReq.getHeader(name);
			}

			@Override
			public InetAddress getClientAddress()
			{
				return httpReq.getClientAddress();
			}

			@Override
			public int getClientPort()
			{
				return httpReq.getClientPort();
			}

			@Override
			public InputStream getBody()
			{
				return httpReq.getBody();
			}

			@Override
			public String getCookie(final String name)
			{
				return httpReq.getCookie(name);
			}

			@Override
			public Set<String> getCookieNames()
			{
				return httpReq.getCookieNames();
			}

			@Override
			public List<MultiPartData> getMultiParts()
			{
				return httpReq.getMultiParts();
			}

			@Override
			public double getSpecialEncodingAcceptability(final String type)
			{
				return httpReq.getSpecialEncodingAcceptability(type);
			}

			@Override
			public String getFullHost()
			{
				return httpReq.getFullHost();
			}

			@Override
			public List<long[]> getRangeAZ()
			{
				return httpReq.getRangeAZ();
			}

			@Override
			public void addFakeUrlParameter(final String name, final String value)
			{
				params.put(name.toLowerCase(), value);
			}

			@Override
			public void removeUrlParameter(final String name)
			{
				params.remove(name.toLowerCase());
			}

			@Override
			public Map<String,Object> getRequestObjects()
			{
				return httpReq.getRequestObjects();
			}

			@Override
			public float getHttpVer()
			{
				return httpReq.getHttpVer();
			}

			@Override
			public String getQueryString()
			{
				return httpReq.getQueryString();
			}
		};
		for(final String[] pair : STAT_CHECKS)
		{
			if(mergeReq.isUrlParameter(pair[1]) && (mergeReq.getUrlParameter(pair[1]).length()==0))
				mergeReq.addFakeUrlParameter(pair[1].toLowerCase(), R.getStat(pair[0]));
		}
		CMLib.map().resetRoom(R);
		R=(Room)R.copyOf();
		final RoomStuff stuff=new RoomStuff(R);
		final Pair<String,String>[] activePairs = makePairs(stuff,new Vector<Pair<String,String>>());
		final List<Pair<String,String>> submittedRoomPairsList = toPairs(mergeParams);
		final List<Pair<String,String>> commonRoomsPairsList=Arrays.asList(setPairs);
		final List<Pair<String,String>> currentRoomPairsList=new XVector<Pair<String,String>>(Arrays.asList(activePairs));
		RoomData.mergeRoomField(currentRoomPairsList,commonRoomsPairsList,submittedRoomPairsList,new String[]{"AFFECT","ADATA"});
		RoomData.mergeRoomField(currentRoomPairsList,commonRoomsPairsList,submittedRoomPairsList,new String[]{"BEHAV","BDATA"});
		RoomData.mergeRoomField(currentRoomPairsList,commonRoomsPairsList,submittedRoomPairsList,new String[]{"MOB"});
		RoomData.mergeRoomField(currentRoomPairsList,commonRoomsPairsList,submittedRoomPairsList,new String[]{"ITEM","ITEMWORN","ITEMCONT"});
		for(final Pair<String,String> p : currentRoomPairsList)
		{
			if(p.second==null)
				mergeParams.remove(p.first);
			else
				mergeParams.put(p.first, p.second);
		}
		if(!mergeParams.containsKey("AFFECT1"))
			mergeParams.put("AFFECT1", "");
		if(!mergeParams.containsKey("BEHAV1"))
			mergeParams.put("BEHAV1", "");
		if(!mergeParams.containsKey("MOB1"))
			mergeParams.put("MOB1", "");
		if(!mergeParams.containsKey("ITEM1"))
			mergeParams.put("ITEM1", "");
		return mergeReq;
	}

	public static String getTags(final CMObject o, final HTTPRequest httpReq, final java.util.Map<String,String> parms)
	{
		if((parms.containsKey("TAGS"))&&(o instanceof Taggable))
		{
			String tags=httpReq.getUrlParameter("TAGS");
			if(tags==null)
				tags = CMParms.toSemicolonListString(((Taggable)o).tags());
			return tags;
		}
		return "";
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("ROOM");
		if(last==null)
			return " @break@";
		if(last.length()==0)
			return "";

		if(!CMProps.isState(CMProps.HostState.RUNNING))
			return CMProps.getVar(CMProps.Str.MUDSTATUS);

		final String multiFlagStr=httpReq.getUrlParameter("MULTIROOMFLAG");
		final boolean multiFlag=(multiFlagStr!=null)&& multiFlagStr.equalsIgnoreCase("on");
		final List<String> multiRoomList=CMParms.parseSemicolons(httpReq.getUrlParameter("MULTIROOMLIST"),false);
		Room R=(Room)httpReq.getRequestObjects().get(last);
		boolean useRoomItems=true;
		if(R==null)
		{
			R=MUDGrinder.getRoomObject(httpReq, last);
			if(R==null)
				return "No Room?!";
			CMLib.map().resetRoom(R);
			if(multiFlag
			&&(multiRoomList.size()>1)
			&&(httpReq.getUrlParameter("MOB1")==null)
			&&(httpReq.getUrlParameter("ITEM1")==null))
			{
				final Pair<String,String> pairs[]=makeMergableRoomFields(httpReq, R,multiRoomList);
				if(pairs!=null)
				{
					for(final Pair<String,String> p : pairs)
					{
						if(p.second==null)
							httpReq.removeUrlParameter(p.first);
						else
							httpReq.addFakeUrlParameter(p.first,p.second);
					}
				}
				useRoomItems=false;
			}
			httpReq.getRequestObjects().put(last,R);
		}
		synchronized(CMClass.getSync("SYNC"+R.roomID()))
		{
			R=CMLib.map().getRoom(R);
			if(R==null)
				return "@break@";
			final int theme = R.getArea().getTheme();

			final StringBuffer str=new StringBuffer("");
			if(parms.containsKey("NAME"))
			{
				String name=httpReq.getUrlParameter("NAME");
				if((name==null)||((name.length()==0)&&(!multiFlag)))
					name=R.displayText();
				str.append(name);
			}
			if(parms.containsKey("CLASSES"))
			{
				String className=httpReq.getUrlParameter("CLASSES");
				if((className==null)||((className.length()==0)&&(!multiFlag)))
					className=CMClass.classID(R);
				Object[] sorted=(Object[])Resources.getResource("MUDGRINDER-LOCALES");
				if(sorted==null)
				{
					final Vector<String> sortMe=new Vector<String>();
					for(final Enumeration<Room> l=CMClass.locales();l.hasMoreElements();)
						sortMe.addElement(CMClass.classID(l.nextElement()));
					sorted=(new TreeSet<String>(sortMe)).toArray();
					Resources.submitResource("MUDGRINDER-LOCALES",sorted);
				}
				if(multiFlag)
				{
					str.append("<OPTION VALUE=\"\"");
					if(className.length()==0)
						str.append(" SELECTED");
					str.append(">&nbsp;&nbsp;");
				}
				for (final Object element : sorted)
				{
					final String cnam=(String)element;
					str.append("<OPTION VALUE=\""+cnam+"\"");
					if(className.equals(cnam))
						str.append(" SELECTED");
					str.append(">"+cnam);
				}
			}

			str.append(AreaData.affects(R,httpReq,parms,1));
			str.append(AreaData.behaves(R,httpReq,parms,1));
			if(parms.containsKey("IMAGE"))
			{
				String name=httpReq.getUrlParameter("IMAGE");
				if((name==null)||((name.length()==0)&&(!multiFlag)))
					name=R.rawImage();
				str.append(name);
			}
			str.append(getTags(R,httpReq,parms));
			if(parms.containsKey("DESCRIPTION"))
			{
				String desc=httpReq.getUrlParameter("DESCRIPTION");
				if((desc==null)||((desc.length()==0)&&(!multiFlag)))
					desc=R.description();
				str.append(desc);
			}
			if(parms.containsKey("DEVIATIONS"))
			{
				final Command C=CMClass.getCommand("Deviations");
				if(C!=null)
				{
					final MOB mob=CMClass.getFactoryMOB();
					StringBuffer str2;
					try
					{
						mob.setPlayerStats((PlayerStats)CMClass.getCommon("DefaultPlayerStats"));
						int ct=0;
						while((mob.numItems()>0)&&(++ct<1000))
							mob.delItem(mob.getItem(0));
						str2 = new StringBuffer((String)C.executeInternal(mob, 0, R));
					}
					catch (final IOException e)
					{
						str2=new StringBuffer("");
					}
					str2=super.colorwebifyOnly(str2);
					str.append(str2.toString()+"  ");
				}
			}
			if(parms.containsKey("ATMOSPHERE"))
			{
				String atmoVal=httpReq.getUrlParameter("ATMOSPHERE");
				if((atmoVal==null)||((!CMath.isNumber(atmoVal))&&(!multiFlag)))
					atmoVal=""+R.getAtmosphereCode();
				final int atmo=CMath.s_int(atmoVal);
				str.append("<OPTION VALUE=\"-1\"").append((atmo<0)?"SELECTED":"").append(">Inherited");
				for(final int r : RawMaterial.CODES.ALL_SBN())
				{
					str.append("<OPTION VALUE=\""+r+"\"");
					if(r==atmo)
						str.append(" SELECTED");
					str.append(">"+RawMaterial.CODES.NAME(r));
				}
			}
			if(parms.containsKey("CLIMATES"))
			{
				int climate=R.getClimateTypeCode();
				if(httpReq.isUrlParameter("CLIMATE"))
				{
					climate=CMath.s_int(httpReq.getUrlParameter("CLIMATE"));
					if(climate>=0)
					for(int i=1;;i++)
					{
						if(httpReq.isUrlParameter("CLIMATE"+(Integer.toString(i))))
						{
							final int newVal=CMath.s_int(httpReq.getUrlParameter("CLIMATE"+(Integer.toString(i))));
							if(newVal<0)
							{
								climate=-1;
								break;
							}
							climate=climate|newVal;
						}
						else
							break;
					}
				}
				str.append("<OPTION VALUE=-1 "+((climate<0)?"SELECTED":"")+">Inherited");
				for(int i=1;i<Places.NUM_CLIMATES;i++)
				{
					final String climstr=Places.CLIMATE_DESCS[i];
					final int mask=(int)CMath.pow(2,i-1);
					str.append("<OPTION VALUE="+mask);
					if((climate>=0)&&((climate&mask)>0))
						str.append(" SELECTED");
					str.append(">"+climstr);
				}
			}
			if((parms.containsKey("XGRID"))&&(R instanceof GridLocale))
			{
				String size=httpReq.getUrlParameter("XGRID");
				if((size==null)||((size.length()==0)&&(!multiFlag)))
					size=((GridLocale)R).xGridSize()+"";
				str.append(size);
			}
			if((parms.containsKey("YGRID"))&&(R instanceof GridLocale))
			{
				String size=httpReq.getUrlParameter("YGRID");
				if((size==null)||((size.length()==0)&&(!multiFlag)))
					size=((GridLocale)R).yGridSize()+"";
				str.append(size);
			}
			if(R instanceof AutoGenArea)
			{
				final AutoGenArea AG=(AutoGenArea)R;

				if(parms.containsKey("AGAUTOVAR"))
				{
					String value=httpReq.getUrlParameter("AGAUTOVAR");
					if((value==null)||(value.length()==0))
						value=CMParms.toEqListString(AG.getAutoGenVariables());
					str.append(value);
				}
				if(parms.containsKey("AGXMLPATH"))
				{
					String value=httpReq.getUrlParameter("AGXMLPATH");
					if((value==null)||(value.length()==0))
						value=AG.getGeneratorXmlPath();
					str.append(value);
				}
			}
			if(parms.containsKey("ISGRID"))
			{
				if(R instanceof GridLocale)
					return "true";
				return "false";
			}
			if(parms.containsKey("MOBLIST"))
			{
				final List<MOB> classes=new ArrayList<MOB>();
				if(httpReq.isUrlParameter("MOB1"))
				{
					for(int i=1;;i++)
					{
						final String MATCHING=httpReq.getUrlParameter("MOB"+i);
						if(MATCHING==null)
							break;
						else
						if(CMLib.webMacroFilter().isAllNum(MATCHING))
						{
							final MOB M2=CMLib.webMacroFilter().getMOBFromWebCache(R,MATCHING);
							if(M2!=null)
								classes.add(M2);
						}
						else
						if(MATCHING.startsWith("CATALOG-"))
						{
							MOB M=CMLib.catalog().getCatalogMob(MATCHING.substring(8));
							if(M!=null)
							{
								M=(MOB)M.copyOf();
								CMLib.catalog().changeCatalogUsage(M,true);
								classes.add(M);
							}
						}
						else
						if(MATCHING.indexOf('@')>0)
						{
							final MOB M2=CMLib.webMacroFilter().getMOBFromAnywhere(MATCHING);
							if(M2 != null)
								classes.add(M2);
						}
						else
						for(final Enumeration<MOB> m=CMClass.mobTypes();m.hasMoreElements();)
						{
							final MOB M2=m.nextElement();
							if(CMClass.classID(M2).equals(MATCHING)
							&&(!M2.isGeneric()))
							{
								classes.add((MOB)M2.copyOf());
								break;
							}
						}
					}
				}
				else
				{
					for(int m=0;m<R.numInhabitants();m++)
					{
						final MOB M=R.fetchInhabitant(m);
						if(M!=null)
						{
							CMLib.catalog().updateCatalogIntegrity(M);
							if(M.isSavable()||(!R.isSavable()))
								classes.add(M);
						}
					}
					CMLib.webMacroFilter().contributeMOBsToWebCache(classes);
				}
				str.append("<TABLE WIDTH=100% BORDER=1 CELLSPACING=0 CELLPADDING=0>");
				for(int i=0;i<classes.size();i++)
				{
					final MOB M=classes.get(i);
					str.append("<TR>");
					str.append("<TD WIDTH=90%>");
					str.append("<SELECT ONCHANGE=\"DelMOB(this);\" NAME=MOB"+(i+1)+">");
					str.append("<OPTION VALUE=\"\">Delete!");
					final String code=CMLib.webMacroFilter().getAppropriateCode(M,R,useRoomItems?classes:new ArrayList<MOB>());
					str.append("<OPTION SELECTED VALUE=\""+code+"\">"+M.Name()+" ("+M.ID()+")");
					str.append("</SELECT>");
					str.append("</TD>");
					str.append("<TD WIDTH=10%>");
					if(!CMLib.flags().isCataloged(M))
						str.append("<INPUT TYPE=BUTTON NAME=EDITMOB"+(i+1)+" VALUE=EDIT "
								+ "ONCLICK=\"EditMOB('"+CMLib.webMacroFilter().findMOBWebCacheCode(classes,M)+"');\">");
					str.append("</TD></TR>");
				}
				str.append("<TR><TD WIDTH=90% ALIGN=CENTER>");
				str.append("<SELECT ONCHANGE=\"AddMOB(this);\" NAME=MOB"+(classes.size()+1)+">");
				str.append("<OPTION SELECTED VALUE=\"\">Select a new MOB");
				for(final Iterator<MOB> m=CMLib.webMacroFilter().getMOBWebCacheIterable().iterator(); m.hasNext();)
				{
					final MOB M=m.next();
					str.append("<OPTION VALUE=\""+M+"\">"+M.Name()+CMLib.webMacroFilter().getWebCacheSuffix(M));
				}
				StringBuffer mlist=(StringBuffer)Resources.getResource("MUDGRINDER-MOBLIST");
				if(mlist==null)
				{
					mlist=new StringBuffer("");
					for(final Enumeration<MOB> m=CMClass.mobTypes();m.hasMoreElements();)
					{
						final MOB M=m.nextElement();
						if(!M.isGeneric())
							mlist.append("<OPTION VALUE=\""+M.ID()+"\">"+M.Name()+" ("+M.ID()+")");
					}
					Resources.submitResource("MUDGRINDER-MOBLIST",mlist);
				}
				str.append(mlist);
				str.append("<OPTION VALUE=\"\">------ CATALOGED -------");
				final String[] names=CMLib.catalog().getCatalogMobNames();
				for (final String name : names)
					str.append("<OPTION VALUE=\"CATALOG-"+name+"\">"+name);
				str.append("</SELECT>");
				str.append("</TD>");
				str.append("<TD WIDTH=10%>");
				str.append("<INPUT TYPE=BUTTON NAME=ADDMOB VALUE=\"NEW\" ONCLICK=\"AddNewMOB();\">");
				str.append("</TD></TR></TABLE>");
			}

			if(parms.containsKey("ITEMLIST"))
			{
				final List<Item> classes=new ArrayList<Item>();
				final List<Object> containers=new ArrayList<Object>();
				final List<Boolean> beingWorn=new ArrayList<Boolean>();
				if(httpReq.isUrlParameter("ITEM1"))
				{
					final Vector<String> cstrings=new Vector<String>();
					for(int i=1;;i++)
					{
						final String MATCHING=httpReq.getUrlParameter("ITEM"+i);
						final String WORN=httpReq.getUrlParameter("ITEMWORN"+i);
						if(MATCHING==null)
							break;
						final Item I2=CMLib.webMacroFilter().findItemInAnything(R,MATCHING);
						if(I2!=null)
						{
							classes.add(I2);
							beingWorn.add(Boolean.valueOf((WORN!=null)&&(WORN.equalsIgnoreCase("on"))));
							final String CONTAINER=httpReq.getUrlParameter("ITEMCONT"+i);
							cstrings.addElement((CONTAINER==null)?"":CONTAINER);
						}
					}
					for(int i=0;i<cstrings.size();i++)
					{
						final String CONTAINER=cstrings.elementAt(i);
						Item C2=null;
						if(CONTAINER.length()>0)
							C2=(Item)CMLib.english().fetchEnvironmental(classes,CONTAINER,true);
						containers.add((C2!=null)?(Object)C2:"");
					}
				}
				else
				{
					for(int m=0;m<R.numItems();m++)
					{
						final Item I2=R.getItem(m);
						if(I2!=null)
						{
							CMLib.catalog().updateCatalogIntegrity(I2);
							classes.add(I2);
							containers.add((I2.container()==null)?"":(Object)I2.container());
							beingWorn.add(Boolean.valueOf(!I2.amWearingAt(Wearable.IN_INVENTORY)));
						}
					}
					CMLib.webMacroFilter().contributeItemsToWebCache(classes);
				}
				str.append("<TABLE WIDTH=100% BORDER=1 CELLSPACING=0 CELLPADDING=0>");
				final Map<Container,String> classesContainers = new HashMap<Container,String>();
				{
					final List<String> allContextNames=CMLib.english().getAllContextNames(classes, new Filterer<Environmental>()
					{
						@Override
						public boolean passesFilter(final Environmental obj)
						{
							return obj instanceof Container;
						}
					});
					for(int i2=0;i2<classes.size();i2++)
					{
						final Item I=classes.get(i2);
						if(I instanceof Container)
							classesContainers.put((Container)I,allContextNames.get(i2));
					}
				}
				for(int i=0;i<classes.size();i++)
				{
					final Item I=classes.get(i);
					final Item C=(classes.contains(containers.get(i))?(Item)containers.get(i):null);
					//Boolean W=(Boolean)beingWorn.elementAt(i);
					str.append("<TR>");
					str.append("<TD WIDTH=90%>");
					str.append("<SELECT ONCHANGE=\"DelItem(this);\" NAME=ITEM"+(i+1)+">");
					str.append("<OPTION VALUE=\"\">Delete!");
					final String code=CMLib.webMacroFilter().getAppropriateCode(I,R,useRoomItems?classes:new ArrayList<Item>());
					str.append("<OPTION SELECTED VALUE=\""+code+"\">"+
							CMStrings.limit(CMStrings.removeColors(I.Name()),40)
							+" ("+I.ID()+")");
					str.append("</SELECT><BR>");
					str.append("<FONT COLOR=WHITE SIZE=-1>");
					str.append("Container: ");
					str.append("<SELECT NAME=ITEMCONT"+(i+1)+">");
					str.append("<OPTION VALUE=\"\" "+((C==null)?"SELECTED":"")+">On the ground");
					for(final Container C2 : classesContainers.keySet())
					{
						if(C2 != I)
						{
							final String name=classesContainers.get(C2);
							str.append("<OPTION "+((C2==C)?"SELECTED":"")+" VALUE=\""+name+"\">"+name+" ("+C2.ID()+")");
						}
					}
					str.append("</SELECT>&nbsp;&nbsp;");
					//str.append("<INPUT TYPE=CHECKBOX NAME=ITEMWORN"+(i+1)+" "+(W.booleanValue()?"CHECKED":"")+">Worn/Wielded");
					str.append("</FONT></TD>");
					str.append("<TD WIDTH=10%>");
					if(!CMLib.flags().isCataloged(I))
					{
						str.append("<INPUT TYPE=BUTTON NAME=EDITITEM"+(i+1)+" VALUE=EDIT "
								+ "ONCLICK=\"EditItem('"+CMLib.webMacroFilter().findItemWebCacheCode(classes,I)+"');\">");
					}
					str.append("</TD></TR>");
				}
				str.append("<TR><TD WIDTH=90% ALIGN=CENTER>");
				str.append("<SELECT ONCHANGE=\"AddItem(this);\" NAME=ITEM"+(classes.size()+1)+">");
				str.append("<OPTION SELECTED VALUE=\"\">Select a new Item");
				for (final Item I : CMLib.webMacroFilter().getItemWebCacheIterable())
					str.append("<OPTION VALUE=\""+I+"\">"+I.Name()+CMLib.webMacroFilter().getWebCacheSuffix(I));
				StringBuffer ilist=(StringBuffer)Resources.getResource("MUDGRINDER-ITEMLIST"+theme);
				if(ilist==null)
				{
					ilist=new StringBuffer("");
					final List<String> sortMe=new Vector<String>();
					CMClass.addAllItemClassNames(sortMe,true,true,false,theme);
					Collections.sort(sortMe);
					for (final Object element : sortMe)
						ilist.append("<OPTION VALUE=\""+(String)element+"\">"+(String)element);
					Resources.submitResource("MUDGRINDER-ITEMLIST"+theme,ilist);
				}
				str.append(ilist);
				str.append("<OPTION VALUE=\"\">------ CATALOGED -------");
				final String[] names=CMLib.catalog().getCatalogItemNames();
				for (final String name : names)
					str.append("<OPTION VALUE=\"CATALOG-"+name+"\">"+name);
				str.append("</SELECT>");
				str.append("</TD>");
				str.append("<TD WIDTH=10%>");
				str.append("<INPUT TYPE=BUTTON NAME=ADDITEM VALUE=\"NEW\" ONCLICK=\"AddNewItem();\">");
				str.append("</TD></TR></TABLE>");
			}

			String strstr=str.toString();
			if(strstr.endsWith(", "))
				strstr=strstr.substring(0,strstr.length()-2);
			return clearWebMacros(strstr);
		}
	}
}
