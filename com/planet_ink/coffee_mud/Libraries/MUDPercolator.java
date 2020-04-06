package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.exceptions.CMDataException;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.core.exceptions.MQLException;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.Area.State;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutNode;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.io.*;
/*
   Copyright 2008-2020 Bo Zimmerman

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
public class MUDPercolator extends StdLibrary implements AreaGenerationLibrary
{
	@Override
	public String ID()
	{
		return "MUDPercolator";
	}

	protected final static char[] splitters=new char[]{'<','>','='};
	protected final static Triad<Integer,Integer,Class<?>[]> emptyMetacraftFilter = new Triad<Integer,Integer,Class<?>[]>(Integer.valueOf(-1),Integer.valueOf(-1),new Class<?>[0]);
	protected final static String POST_PROCESSING_STAT_SETS="___POST_PROCESSING_SETS___";
	protected final static Set<String> UPPER_REQUIRES_KEYWORDS=new XHashSet<String>(new String[]{"INT","INTEGER","$","STRING","ANY","DOUBLE","#","NUMBER"});
	protected final static CMParms.DelimiterChecker REQUIRES_DELIMITERS=CMParms.createDelimiter(new char[]{' ','\t',',','\r','\n'});
	protected final static List<String> ITEM_IGNORE_STATS = Arrays.asList(GenericBuilder.GenItemCode.getAllCodeNames());
	protected final static List<String> MOB_IGNORE_STATS =new XVector<String>(Arrays.asList(GenericBuilder.GenMOBCode.getAllCodeNames())).append("GENDER");

	protected static enum MQLSpecialFromSet
	{
		AREAS,
		AREA,
		ROOMS,
		ROOM,
		EXITS,
		PLAYERS,
		PLAYER,
		MOBS,
		MOB,
		NPCS,
		NPC,
		ITEMS,
		ITEM,
		EQUIPMENT,
		OWNER,
		RESOURCES,
		FACTIONS,
		ABILITIES,
		PROPERTIES,
		EFFECTS,
		BEHAVIORS,
		RACES,
		SHOP,
		SHOPS,
		SHOPITEMS
	}

	private final SHashtable<String,Class<LayoutManager>> mgrs = new SHashtable<String,Class<LayoutManager>>();

	private interface BuildCallback
	{
		public void willBuild(Environmental E, XMLTag XMLTag);
	}

	private static final Filterer<MOB> noMobFilter = new Filterer<MOB>()
	{

		@Override
		public boolean passesFilter(final MOB obj)
		{
			return (obj != null) && (CMLib.flags().isInTheGame(obj, true));
		}

	};

	private static final Filterer<MOB> npcFilter = new Filterer<MOB>()
	{

		@Override
		public boolean passesFilter(final MOB obj)
		{
			return (obj != null)
				&& (!obj.isPlayer())
				&&((obj.amFollowing()==null)||(!obj.amUltimatelyFollowing().isPlayer()));
		}

	};

	private static final Filterer<Environmental> shopFilter = new Filterer<Environmental>()
	{

		@Override
		public boolean passesFilter(final Environmental obj)
		{
			return (obj != null)
				&& CMLib.coffeeShops().getShopKeeper(obj) != null;
		}

	};

	private static final Converter<Session, MOB> sessionToMobConvereter= new Converter<Session, MOB>()
	{
		@Override
		public MOB convert(final Session obj)
		{
			if(obj==null)
				return null;
			return obj.mob();
		}
	};

	private static final Comparator<Object> objComparator = new Comparator<Object>()
	{
		@Override
		public int compare(final Object o1, final Object o2)
		{
			final String s1=(o1 instanceof Environmental)?((Environmental)o1).Name():(o1==null?"":o1.toString());
			final String s2=(o1 instanceof Environmental)?((Environmental)o2).Name():(o2==null?"":o2.toString());
			if(CMath.isNumber(s1) && CMath.isNumber(s2))
			{
				final double d1=CMath.s_double(s1);
				final double d2=CMath.s_double(s2);
				return (d1==d2)?0:(d1>d2?1:-1);
			}
			return s1.compareToIgnoreCase(s2);
		}
	};


	@Override
	public LayoutManager getLayoutManager(final String named)
	{
		final Class<LayoutManager> mgr = mgrs.get(named.toUpperCase().trim());
		if(mgr != null)
		{
			try
			{
				return mgr.newInstance();
			}
			catch(final Exception e)
			{
				return null;
			}
		}
		return null;
	}

	@Override
	public void buildDefinedIDSet(final List<XMLTag> xmlRoot, final Map<String,Object> defined, final Set<String> overrideIds)
	{
		if(xmlRoot==null)
			return;
		for(int v=0;v<xmlRoot.size();v++)
		{
			final XMLLibrary.XMLTag piece = xmlRoot.get(v);
			final String id = piece.getParmValue("ID");
			if((id!=null)&&(id.length()>0))
			{
				final Object o=defined.get(id.toUpperCase());
				if(o != null)
				{
					if(!(o instanceof XMLTag))
					{
						if(!overrideIds.contains(id.toUpperCase()))
							Log.errOut("Duplicate ID: "+id+" (first tag did not resolve to a complex piece -- it wins.)");
					}
					else
					{
						final Boolean pMergeVal;
						boolean pMergeOver=false;
						String psMergeVal = null;
						if((piece.parms()==null)||(!piece.parms().containsKey("MERGE")))
							pMergeVal = null;
						else
						{
							psMergeVal = piece.parms().get("MERGE").toUpperCase().trim();
							pMergeVal = Boolean.valueOf(CMath.s_bool(psMergeVal));
							pMergeOver=psMergeVal.startsWith("OVER");
						}
						final Boolean oMergeVal;
						String osMergeVal = null;
						boolean oMergeOver=false;
						if((((XMLTag)o).parms()==null)||(!((XMLTag)o).parms().containsKey("MERGE")))
							oMergeVal = null;
						else
						{
							osMergeVal = ((XMLTag)o).parms().get("MERGE");
							oMergeVal = Boolean.valueOf(CMath.s_bool(osMergeVal));
							oMergeOver=osMergeVal.startsWith("OVER");
						}
						if((oMergeOver)&&(!pMergeOver))
						{	/* do nothing, it's already good */ }
						else
						if((!oMergeOver) && (pMergeOver))
							defined.put(id.toUpperCase().trim(),piece);
						else
						if((pMergeVal == null)||(oMergeVal == null))
						{
							if(!overrideIds.contains(id.toUpperCase()))
								Log.errOut("Duplicate ID: "+id+" (no MERGE tag found to permit this operation -- first tag wins.)");
						}
						else
						if((oMergeOver && pMergeOver)
						&&(osMergeVal != null)
						&&(psMergeVal != null))
						{
							final int onum=CMath.s_int(osMergeVal.substring(4));
							final int pnum=CMath.s_int(psMergeVal.substring(4));
							if(onum < pnum)
								defined.put(id.toUpperCase().trim(),piece);
						}
						else
						if(pMergeVal.booleanValue() && oMergeVal.booleanValue())
						{
							final XMLTag src=piece;
							final XMLTag tgt=(XMLTag)o;
							if(!src.tag().equalsIgnoreCase(tgt.tag()))
								Log.errOut("Unable to merge tags with ID: "+id+", they are of different types.");
							else
							for(final String parm : src.parms().keySet())
							{
								final String srcParmVal=src.parms().get(parm);
								final String tgtParmVal=tgt.parms().get(parm);
								if(tgtParmVal==null)
									tgt.parms().put(parm,srcParmVal);
								else
								if(tgtParmVal.equalsIgnoreCase(srcParmVal)||parm.equalsIgnoreCase("ID"))
								{
									/* do nothing -- nothing to do */
								}
								else
								if(parm.equalsIgnoreCase("REQUIRES"))
								{
									final Map<String,String> srcParms=CMParms.parseEQParms(srcParmVal, REQUIRES_DELIMITERS, true);
									final Map<String,String> tgtParms=CMParms.parseEQParms(tgtParmVal, REQUIRES_DELIMITERS, true);
									for(final String srcKey : srcParms.keySet())
									{
										final String srcVal=srcParms.get(srcKey);
										final String tgtVal=tgtParms.get(srcKey);
										if(tgtVal == null)
											tgtParms.put(srcKey, srcVal);
										else
										if(UPPER_REQUIRES_KEYWORDS.contains(srcVal.toUpperCase()))
										{
											if(!srcVal.equalsIgnoreCase(tgtVal))
												Log.errOut("Unable to merge REQUIRES parm on tags with ID: "+id+", mismatch in requirements for '"+srcKey+"'.");
										}
										else
										if(UPPER_REQUIRES_KEYWORDS.contains(tgtVal.toUpperCase()))
											Log.errOut("Unable to merge REQUIRES parm on tags with ID: "+id+", mismatch in requirements for '"+srcKey+"'.");
										else
											tgtParms.put(srcKey,srcVal+";"+tgtVal);
									}
									tgt.parms().put(parm, CMParms.combineEQParms(tgtParms, ','));
								}
								else
								if(parm.equalsIgnoreCase("CONDITION")||parm.equalsIgnoreCase("VALIDATE"))
									tgt.parms().put(parm, tgtParmVal+" and "+srcParmVal);
								else
								if(parm.equalsIgnoreCase("INSERT")||parm.equalsIgnoreCase("DEFINE")||parm.equalsIgnoreCase("PREDEFINE"))
									tgt.parms().put(parm, tgtParmVal+","+srcParmVal);
								else
									Log.errOut("Unable to merge SELECT parm on tags with ID: "+id+".");
							}
							for(final XMLLibrary.XMLTag subPiece : src.contents())
							{
								final Boolean subMergeVal;
								if((subPiece.parms()==null)||(!subPiece.parms().containsKey("MERGE")))
									subMergeVal = null;
								else
									subMergeVal = Boolean.valueOf(CMath.s_bool(subPiece.parms().get("MERGE")));
								if((subMergeVal == null)||(subMergeVal.booleanValue()))
									tgt.contents().add(subPiece);
							}
						}
					}
				}
				else
					defined.put(id.toUpperCase().trim(),piece);
			}

			final String load = piece.getParmValue("LOAD");
			final String from = piece.getParmValue("FROM");
			if((load!=null)&&(load.length()>0))
			{
				piece.parms().remove("LOAD");
				final String loadcondition=piece.getParmValue("CONDITION");
				boolean proceedWithLoad=true;
				if((loadcondition != null)&&(loadcondition.length()>0))
				{
					try
					{
						if(!testCondition(null,null,null,CMLib.xml().restoreAngleBrackets(loadcondition),piece,defined))
							proceedWithLoad=false;
					}
					catch (final PostProcessException e)
					{
					}
				}
				if(proceedWithLoad)
				{
					XMLTag loadedPiece=(XMLTag)defined.get("SYSTEM_LOADED_XML_FILES");
					if(loadedPiece==null)
					{
						loadedPiece=CMLib.xml().createNewTag("SYSTEM_LOADED_XML_FILES","");
						defined.put("SYSTEM_LOADED_XML_FILES", loadedPiece);
					}
					final CMFile file = new CMFile(load,null,CMFile.FLAG_LOGERRORS|CMFile.FLAG_FORCEALLOW);
					if(loadedPiece.getPieceFromPieces( file.getAbsolutePath().toUpperCase())==null)
					{
						loadedPiece.contents().add(CMLib.xml().createNewTag(file.getAbsolutePath().toUpperCase(),"true"));
						if(file.exists() && file.canRead())
						{
							if(CMSecurity.isDebugging(DbgFlag.MUDPERCOLATOR))
								Log.debugOut("MUDPercolator", "Loading XML file "+load);
							final List<XMLTag> addPieces=CMLib.xml().parseAllXML(file.text());
							piece.contents().addAll(addPieces);
						}
						else
							Log.errOut("MUDPercolator", "Failed loading XML file "+load);
					}
				}
			}
			else
			if((from!=null)&&(from.length()>0))
			{
				final String localid=piece.getParmValue("ID");
				if(localid == null)
				{
					Log.errOut("Invalid FROM parm on a tag missing an ID.: "+from+".");
					continue;
				}
				piece.parms().remove("FROM");
				final String loadcondition=piece.getParmValue("CONDITION");
				if((loadcondition != null)&&(loadcondition.length()>0))
				{
					try
					{
						if(!testCondition(null,null,null,CMLib.xml().restoreAngleBrackets(loadcondition),piece,defined))
						{
							continue;
						}
					}
					catch (final PostProcessException e)
					{
					}
				}
				if(CMSecurity.isDebugging(DbgFlag.MUDPERCOLATOR))
					Log.debugOut("MUDPercolator", "Loading '"+localid+"' from file "+from);
				final CMFile file = new CMFile(load,null,CMFile.FLAG_LOGERRORS|CMFile.FLAG_FORCEALLOW);
				if(file.exists() && file.canRead())
				{
					final List<XMLTag> addPieces=CMLib.xml().parseAllXML(file.text());
					final Map<String,Object> localDefined = new Hashtable<String,Object>();
					buildDefinedIDSet(addPieces, localDefined, overrideIds);
					final Object o=localDefined.get(localid.toUpperCase().trim());
					if((o == null)
					||(!(o instanceof XMLTag)))
					{
						Log.errOut("Invalid FROM parm.  ID '"+localid+"' not found in: "+from+".");
						continue;
					}
					final XMLTag newTag=(XMLTag)o;
					piece.parms().putAll(newTag.parms());
					piece.contents().addAll(newTag.contents());
				}
			}
			buildDefinedIDSet(piece.contents(),defined, overrideIds);
		}
	}

	//@Override public
	void testMQLParsing()
	{
		final String[] testMQLs = new String[] {
			"SELECT: x from y where xxx > ydd and ( yyy<=djj ) or zzz > jkkk",
			"SELECT: x from y where xxx > ydd and ttt>3 or ( yyy<=djj ) and zzz > jkkk or 6>7",
			"SELECT: x from y",
			"SELECT: x, XX from y",
			"SELECT: x,xx,xxx from y",
			"SELECT: x, xx , xxx from y",
			"SELECT: x, xx , xxx from (select: x from y)",
			"SELECT: x from y where x>y",
			"SELECT: x from y where x >y",
			"SELECT: x from y where x> y",
			"SELECT: x from y where xxx> ydd and yyy<=djj",
			"SELECT: x from y where xxx> ydd and yyy<=djj or zzz > jkkk and rrr>ddd",
			"SELECT: x from (select: x from y) where (xxx > ydd) and ( yyy<=djj ) or (zzz > jkkk)and(rrr>ddd)",
			"SELECT: x from y where ((xxx > ydd) and ( yyy<=djj )) or((zzz > jkkk)and ((rrr>ddd) or (ddd>888)))",
		};
		for(int i=0;i<testMQLs.length;i++)
		{
			final String mqlWSelect=testMQLs[i];
			final int x=mqlWSelect.indexOf(':');
			final String mql=mqlWSelect.substring(x+1).toUpperCase().trim();
			try
			{
				MQLClause.parseMQL(testMQLs[i], mql);
			}
			catch(final Exception e)
			{
				Log.errOut(e.getMessage());
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean activate()
	{
		final String filePath="com/planet_ink/coffee_mud/Libraries/layouts";
		final CMProps page = CMProps.instance();
		final Vector<Object> layouts=CMClass.loadClassList(filePath,page.getStr("LIBRARY"),"/layouts",LayoutManager.class,true);
		for(int f=0;f<layouts.size();f++)
		{
			final LayoutManager lmgr= (LayoutManager)layouts.elementAt(f);
			final Class<LayoutManager> lmgrClass=(Class<LayoutManager>)lmgr.getClass();
			mgrs.put(lmgr.name().toUpperCase().trim(),lmgrClass);
		}
		return true;
	}

	@Override
	public boolean shutdown()
	{
		mgrs.clear();
		return true;
	}

	private final static class PostProcessException extends Exception
	{
		private static final long serialVersionUID = -8797769166795010761L;
		public PostProcessException(final String s)
		{
			super(s);
		}
	}

	protected abstract class PostProcessAttempt
	{
		protected Map<String,Object> defined=null;
		protected boolean firstRun=true;
		public abstract String attempt() throws CMException,PostProcessException;
	}

	@SuppressWarnings("unchecked")
	protected final String PostProcessAttempter(final Map<String,Object> defined, final PostProcessAttempt attempter) throws CMException
	{
		try
		{
			attempter.defined=defined;
			final String result = attempter.attempt();
			return result;
		}
		catch(final PostProcessException pe)
		{
			List<PostProcessAttempt> posties=(List<PostProcessAttempt>)defined.get(MUDPercolator.POST_PROCESSING_STAT_SETS);
			if(posties==null)
			{
				posties=new Vector<PostProcessAttempt>();
				defined.put(MUDPercolator.POST_PROCESSING_STAT_SETS, posties);
			}
			attempter.firstRun=false;
			final Map<String,Object> definedCopy;
			if(defined instanceof Hashtable)
				definedCopy=(Map<String,Object>)((Hashtable<String,Object>)defined).clone();
			else
				definedCopy=new Hashtable<String,Object>(defined);
			attempter.defined=definedCopy;
			posties.add(attempter);
			return null;
		}
	}

	protected void fillOutRequiredStatCodeSafe(final Modifiable E, final List<String> ignoreStats, final String defPrefix,
			final String tagName, final String statName, final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		PostProcessAttempter(defined,new PostProcessAttempt()
		{
			@Override
			public String attempt() throws CMException, PostProcessException
			{
				final String val = findString(E,ignoreStats,defPrefix,tagName,piece,this.defined);
				E.setStat(statName, val);
				if((defPrefix!=null)&&(defPrefix.length()>0))
					addDefinition(defPrefix+tagName,val,defined);
				return val;
			}
		});
	}

	// vars created: ROOM_CLASS, ROOM_TITLE, ROOM_DESCRIPTION, ROOM_CLASSES, ROOM_TITLES, ROOM_DESCRIPTIONS
	@Override
	public Room buildRoom(final XMLTag piece, final Map<String,Object> defined, final Exit[] exits, final int direction) throws CMException
	{
		addDefinition("DIRECTION",CMLib.directions().getDirectionName(direction).toLowerCase(),defined);

		final String classID = findStringNow("class",piece,defined);
		final Room R = CMClass.getLocale(classID);
		if(R == null)
			throw new CMException("Unable to build room on classID '"+classID+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
		addDefinition("ROOM_CLASS",classID,defined);
		final List<String> ignoreStats=new XArrayList<String>(new String[]{"CLASS","DISPLAY","DESCRIPTION"});
		fillOutRequiredStatCodeSafe(R, ignoreStats, "ROOM_", "TITLE", "DISPLAY", piece, defined);
		fillOutRequiredStatCodeSafe(R, ignoreStats, "ROOM_", "DESCRIPTION", "DESCRIPTION", piece, defined);
		fillOutCopyCodes(R, ignoreStats, "ROOM_", piece, defined);
		fillOutStatCodes(R, ignoreStats, "ROOM_", piece, defined);
		ignoreStats.addAll(Arrays.asList(R.getStatCodes()));
		fillOutStatCodes(R.basePhyStats(),ignoreStats,"ROOM_",piece,defined);
		final List<MOB> mV = findMobs(piece,defined);
		for(int i=0;i<mV.size();i++)
		{
			final MOB M=mV.get(i);
			M.setSavable(true);
			M.bringToLife(R,true);
		}
		final List<Item> iV = findItems(piece,defined);
		for(int i=0;i<iV.size();i++)
		{
			final Item I=iV.get(i);
			R.addItem(I);
			I.setSavable(true);
			I.setExpirationDate(0);
		}
		final List<Ability> aV = findAffects(R,piece,defined,null);
		for(int i=0;i<aV.size();i++)
		{
			final Ability A=aV.get(i);
			A.setSavable(true);
			R.addNonUninvokableEffect(A);
		}
		final List<Behavior> bV = findBehaviors(R,piece,defined);
		for(int i=0;i<bV.size();i++)
		{
			final Behavior B=bV.get(i);
			B.setSavable(true);
			R.addBehavior(B);
		}
		for(int dir=0;dir<Directions.NUM_DIRECTIONS();dir++)
		{
			Exit E=exits[dir];
			if((E==null)&&(defined.containsKey("ROOMLINK_"+CMLib.directions().getDirectionChar(dir).toUpperCase())))
			{
				defined.put("ROOMLINK_DIR",CMLib.directions().getDirectionChar(dir).toUpperCase());
				final Exit E2=findExit(R,piece, defined);
				if(E2!=null)
					E=E2;
				defined.remove("ROOMLINK_DIR");
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
					Log.debugOut("MUDPercolator","EXIT:NEW:"+((E==null)?"null":E.ID())+":DIR="+CMLib.directions().getDirectionChar(dir).toUpperCase()+":ROOM="+R.getStat("DISPLAY"));
			}
			else
			if((CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))&&defined.containsKey("ROOMLINK_"+CMLib.directions().getDirectionChar(dir).toUpperCase()))
				Log.debugOut("MUDPercolator","EXIT:OLD:"+((E==null)?"null":E.ID())+":DIR="+CMLib.directions().getDirectionChar(dir).toUpperCase()+":ROOM="+R.getStat("DISPLAY"));
			R.setRawExit(dir, E);
			R.startItemRejuv();
		}
		return R;
	}

	@SuppressWarnings("unchecked")

	@Override
	public void postProcess(final Map<String,Object> defined) throws CMException
	{
		final List<PostProcessAttempt> posties=(List<PostProcessAttempt>)defined.get(MUDPercolator.POST_PROCESSING_STAT_SETS);
		if(posties == null)
			return;
		try
		{
			for(final PostProcessAttempt stat : posties)
			{
				try
				{
					stat.attempt();
				}
				catch(final PostProcessException pe)
				{
					throw pe;
				}
			}
		}
		catch(final PostProcessException pe)
		{
			throw new CMException("Unsatisfied Post Process Exception: "+pe.getMessage(),pe);
		}
	}

	protected void layoutRecursiveFill(final LayoutNode n, final HashSet<LayoutNode> nodesDone, final Vector<LayoutNode> group, final LayoutTypes type)
	{
		if(n != null)
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
		{
			if(n.links().containsKey(Integer.valueOf(d)))
			{
				final LayoutNode offN=n.links().get(Integer.valueOf(d));
				if((offN.type()==type)
				&&(!group.contains(offN))
				&&(!nodesDone.contains(offN)))
				{
					group.addElement(offN);
					nodesDone.add(offN);
					layoutRecursiveFill(offN,nodesDone,group,type);
				}
			}
		}
	}

	protected void layoutFollow(LayoutNode n, final LayoutTypes type, final int direction, final HashSet<LayoutNode> nodesAlreadyGrouped, final List<LayoutNode> group)
	{
		n=n.links().get(Integer.valueOf(direction));
		while((n != null) &&(n.type()==LayoutTypes.street) &&(!group.contains(n) &&(!nodesAlreadyGrouped.contains(n))))
		{
			nodesAlreadyGrouped.add(n);
			group.add(n);
			n=n.links().get(Integer.valueOf(direction));
		}
	}

	@Override
	public Area findArea(final XMLTag piece, final Map<String,Object> defined, final int directions) throws CMException
	{
		try
		{
			final String tagName="AREA";
			final List<XMLLibrary.XMLTag> choices = getAllChoices(null,null,null,tagName, piece, defined,true);
			if((choices==null)||(choices.size()==0))
			{
				return null;
			}
			while(choices.size()>0)
			{
				final XMLLibrary.XMLTag valPiece = choices.get(CMLib.dice().roll(1,choices.size(),-1));
				choices.remove(valPiece);
				if(valPiece.parms().containsKey("VALIDATE") && !testCondition(null,null,null,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
					continue;
				final Map<String,Object> rDefined=new Hashtable<String,Object>();
				rDefined.putAll(defined);
				defineReward(null,null,null,valPiece.getParmValue("DEFINE"),valPiece,null,rDefined,true);
				final Area A=buildArea(valPiece,rDefined,directions);
				for (final String key : rDefined.keySet())
				{
					if(key.startsWith("_"))
						defined.put(key,rDefined.get(key));
				}
				return A;
			}
		}
		catch(final PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
		return null;
	}

	protected Area buildArea(final XMLTag piece, final Map<String,Object> defined, final int direction) throws CMException
	{
		defined.put("DIRECTION",CMLib.directions().getDirectionName(direction).toLowerCase());

		final String classID = findStringNow("class",piece,defined);
		final Area A = CMClass.getAreaType(classID);
		if(A == null)
			throw new CMException("Unable to build area on classID '"+classID+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
		defined.put("AREA_CLASS",classID);
		final String name = findStringNow(A,null,"AREA_","NAME",piece,defined);
		if(CMLib.map().getArea(name)!=null)
		{
			A.destroy();
			throw new CMException("Unable to create area '"+name+"', you must destroy the old one first.");
		}
		A.setName(name);
		defined.put("AREA_NAME",name);

		PostProcessAttempter(defined,new PostProcessAttempt()
		{
			@Override
			public String attempt() throws CMException, PostProcessException
			{
				final String author = findOptionalString(A,null,"AREA_","author",piece,this.defined, false);
				if(author != null)
				{
					A.setAuthorID(author);
					defined.put("AREA_AUTHOR",author);
				}
				return author;
			}
		});
		PostProcessAttempter(defined,new PostProcessAttempt()
		{
			@Override
			public String attempt() throws CMException, PostProcessException
			{
				final String description = findOptionalString(A,null,"AREA_","description",piece,this.defined, false);
				if(description != null)
				{
					A.setDescription(description);
					defined.put("AREA_DESCRIPTION",description);
				}
				return description;
			}
		});

		if(fillInArea(piece, defined, A, direction))
			return A;
		throw new CMException("Unable to build area for some reason.");
	}

	protected void updateLayoutDefinitions(final Map<String,Object> defined, final Map<String,Object> groupDefined,
										   final Map<List<LayoutNode>,Map<String,Object>> groupDefinitions, final List<List<LayoutNode>> roomGroups)
	{
		for (final String key : groupDefined.keySet())
		{
			if(key.startsWith("__"))
			{
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
					Log.debugOut("AREADEF:"+key+"="+CMStrings.limit(groupDefined.get(key).toString(), 10));
				defined.put(key, groupDefined.get(key));
				for(final List<LayoutNode> group2 : roomGroups)
				{
					final Map<String,Object> groupDefined2 = groupDefinitions.get(group2);
					if(groupDefined2!=groupDefined)
						groupDefined2.put(key, groupDefined.get(key));
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected Room layOutRooms(final Area A, final LayoutManager layoutManager, final int size, final int direction, final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
			Log.debugOut("MudPercolator","Using LayoutManager:"+layoutManager.name());
		final Random random=new Random(System.currentTimeMillis());
		final List<LayoutNode> roomsToLayOut = layoutManager.generate(size,direction);
		if((roomsToLayOut==null)||(roomsToLayOut.size()==0))
			throw new CMException("Unable to fill area of size "+size+" off layout "+layoutManager.name());
		int numLeafs=0;
		for(int i=0;i<roomsToLayOut.size();i++)
		{
			final LayoutNode node=roomsToLayOut.get(i);
			if(node.type()==LayoutTypes.leaf)
				numLeafs++;
			if(node.links().size()==0)
				throw new CMException("Created linkless node with "+layoutManager.name());
		}
		defined.put("AREA_NUMLEAFS", ""+numLeafs);

		// now break our rooms into logical groups, generate those rooms.
		final List<List<LayoutNode>> roomGroups = new Vector<List<LayoutNode>>();
		final LayoutNode magicRoomNode = roomsToLayOut.get(0);
		final HashSet<LayoutNode> nodesAlreadyGrouped=new HashSet<LayoutNode>();
		boolean keepLooking=true;
		while(keepLooking)
		{
			keepLooking=false;
			for(int i=0;i<roomsToLayOut.size();i++)
			{
				final LayoutNode node=roomsToLayOut.get(i);
				if(node.type()==LayoutTypes.leaf)
				{
					final Vector<LayoutNode> group=new Vector<LayoutNode>();
					group.add(node);
					nodesAlreadyGrouped.add(node);
					for(final Integer linkDir : node.links().keySet())
					{
						final LayoutNode dirNode=node.links().get(linkDir);
						if(!nodesAlreadyGrouped.contains(dirNode))
						{
							if((dirNode.type()==LayoutTypes.leaf)
							||(dirNode.type()==LayoutTypes.interior)&&(node.isFlagged(LayoutFlags.offleaf)))
							{
								group.addElement(dirNode);
								nodesAlreadyGrouped.add(dirNode);
							}
						}
					}
					for(final LayoutNode n : group)
						roomsToLayOut.remove(n);
					if(group.size()>0)
					{
						// randomize the leafs a bit
						if(roomGroups.size()==0)
							roomGroups.add(group);
						else
							roomGroups.add(random.nextInt(roomGroups.size()),group);
					}
					keepLooking=true;
					break;
				}
			}
		}
		keepLooking=true;
		while(keepLooking)
		{
			keepLooking=false;
			for(int i=0;i<roomsToLayOut.size();i++)
			{
				final LayoutNode node=roomsToLayOut.get(i);
				if(node.type()==LayoutTypes.street)
				{
					final List<LayoutNode> group=new Vector<LayoutNode>();
					group.add(node);
					nodesAlreadyGrouped.add(node);
					final LayoutRuns run=node.getFlagRuns();
					if(run==LayoutRuns.ns)
					{
						layoutFollow(node,LayoutTypes.street,Directions.NORTH,nodesAlreadyGrouped,group);
						layoutFollow(node,LayoutTypes.street,Directions.SOUTH,nodesAlreadyGrouped,group);
					}
					else
					if(run==LayoutRuns.ew)
					{
						layoutFollow(node,LayoutTypes.street,Directions.EAST,nodesAlreadyGrouped,group);
						layoutFollow(node,LayoutTypes.street,Directions.WEST,nodesAlreadyGrouped,group);
					}
					else
					if(run==LayoutRuns.nesw)
					{
						layoutFollow(node,LayoutTypes.street,Directions.NORTHEAST,nodesAlreadyGrouped,group);
						layoutFollow(node,LayoutTypes.street,Directions.SOUTHWEST,nodesAlreadyGrouped,group);
					}
					else
					if(run==LayoutRuns.nwse)
					{
						layoutFollow(node,LayoutTypes.street,Directions.NORTHWEST,nodesAlreadyGrouped,group);
						layoutFollow(node,LayoutTypes.street,Directions.SOUTHEAST,nodesAlreadyGrouped,group);
					}
					else
					{
						int topDir=-1;
						int topSize=-1;
						for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
						{
							if(node.links().get(Integer.valueOf(d))!=null)
							{
								final List<LayoutNode> grpCopy=new XArrayList<LayoutNode>(group);
								final HashSet<LayoutNode> nodesAlreadyGroupedCopy=(HashSet<LayoutNode>)nodesAlreadyGrouped.clone();
								layoutFollow(node,LayoutTypes.street,d,nodesAlreadyGroupedCopy,grpCopy);
								if(node.links().get(Integer.valueOf(Directions.getOpDirectionCode(d)))!=null)
									layoutFollow(node,LayoutTypes.street,Directions.getOpDirectionCode(d),nodesAlreadyGroupedCopy,grpCopy);
								if(grpCopy.size()>topSize)
								{
									topSize=grpCopy.size();
									topDir=d;
								}
							}
						}
						if(topDir>=0)
						{
							layoutFollow(node,LayoutTypes.street,topDir,nodesAlreadyGrouped,group);
							if(node.links().get(Integer.valueOf(Directions.getOpDirectionCode(topDir)))!=null)
								layoutFollow(node,LayoutTypes.street,Directions.getOpDirectionCode(topDir),nodesAlreadyGrouped,group);
						}
					}
					for(final LayoutNode n : group)
						roomsToLayOut.remove(n);
					if(group.size()>0)
						roomGroups.add(group);
					keepLooking=true;
				}
			}
		}

		while(roomsToLayOut.size() >0)
		{
			final LayoutNode node=roomsToLayOut.get(0);
			final Vector<LayoutNode> group=new Vector<LayoutNode>();
			group.add(node);
			nodesAlreadyGrouped.add(node);
			layoutRecursiveFill(node,nodesAlreadyGrouped,group,node.type());
			for(final LayoutNode n : group)
				roomsToLayOut.remove(n);
			roomGroups.add(group);
		}

		// make CERTAIN that the magic first room in the layout always
		// gets ID#0.
		List<LayoutNode> magicGroup=null;
		for(int g=0;g<roomGroups.size();g++)
		{
			final List<LayoutNode> group=roomGroups.get(g);
			if(group.contains(magicRoomNode))
			{
				magicGroup=group;
				break;
			}
		}
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
		for(int g=0;g<roomGroups.size();g++)
		{
			final Vector<LayoutNode> group=(Vector<LayoutNode>)roomGroups.get(g);
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
				Log.debugOut("MudPercolator","GROUP:"+A.Name()+": "+group.firstElement().type().toString()+": "+group.size());
		}
		final Map<List<LayoutNode>,Map<String,Object>> groupDefinitions=new Hashtable<List<LayoutNode>,Map<String,Object>>();
		for(final List<LayoutNode> group : roomGroups)
		{
			final Map<String,Object> newDefined=new Hashtable<String,Object>();
			newDefined.putAll(defined);
			groupDefinitions.put(group, newDefined);
		}

		Map<String,Object> groupDefined = groupDefinitions.get(magicGroup);
		final Room magicRoom = processRoom(A,direction,piece,magicRoomNode,groupDefined);
		for(final Map<String,Object> otherDefineds : groupDefinitions.values())
		{
			otherDefineds.remove("ROOMTAG_NODEGATEEXIT");
			otherDefineds.remove("ROOMTAG_GATEEXITROOM");
		}
		updateLayoutDefinitions(defined,groupDefined,groupDefinitions,roomGroups);

		//now generate the rooms and add them to the area
		for(final List<LayoutNode> group : roomGroups)
		{
			groupDefined = groupDefinitions.get(group);

			for(final LayoutNode node : group)
			{
				if(node!=magicRoomNode)
					processRoom(A,direction,piece,node,groupDefined);
			}
			updateLayoutDefinitions(defined,groupDefined,groupDefinitions,roomGroups);
		}

		for(final Integer linkDir : magicRoomNode.links().keySet())
		{
			if(linkDir.intValue() == Directions.getOpDirectionCode(direction))
				Log.errOut("MUDPercolator","Generated an override exit for "+magicRoom.roomID()+", direction="+direction+", layout="+layoutManager.name());
			else
			{
				final LayoutNode linkNode=magicRoomNode.getLink(linkDir.intValue());
				if((magicRoom.getRawExit(linkDir.intValue())==null) || (linkNode.room() == null))
					Log.errOut("MUDPercolator","Generated an unpaired node for "+magicRoom.roomID());
				else
					magicRoom.rawDoors()[linkDir.intValue()]=linkNode.room();
			}
		}

		//now do a final-link on the rooms
		for(final List<LayoutNode> group : roomGroups)
		{
			for(final LayoutNode node : group)
			{
				final Room R=node.room();
				if(node != magicRoomNode)
				{
					if((R==null)||(node.links().keySet().size()==0))
						Log.errOut("MUDPercolator",layoutManager.name()+" generated a linkless node: "+node.toString());
					else
					for(final Integer linkDir : node.links().keySet())
					{
						final LayoutNode linkNode=node.getLink(linkDir.intValue());
						if((R.getRawExit(linkDir.intValue())==null) || (linkNode.room() == null))
							Log.errOut("MUDPercolator","Generated an unpaired node for "+R.roomID());
						else
							R.rawDoors()[linkDir.intValue()]=linkNode.room();
					}
				}
			}
		}
		return magicRoom;
	}

	// vars created: LINK_DIRECTION, AREA_CLASS, AREA_NAME, AREA_DESCRIPTION, AREA_LAYOUT, AREA_SIZE
	@Override
	public boolean fillInArea(final XMLTag piece, final Map<String,Object> defined, final Area A, final int direction) throws CMException
	{
		final String layoutType = findStringNow("layout",piece,defined);
		if((layoutType==null)||(layoutType.trim().length()==0))
			throw new CMException("Unable to build area without defined layout");
		final LayoutManager layoutManager = getLayoutManager(layoutType);
		if(layoutManager == null)
			throw new CMException("Undefined Layout "+layoutType);
		defined.put("AREA_LAYOUT",layoutManager.name());
		String size = findStringNow("size",piece,defined);
		if(CMath.isMathExpression(size))
			size=Integer.toString(CMath.parseIntExpression(size));
		if((!CMath.isInteger(size))||(CMath.s_int(size)<=0))
			throw new CMException("Unable to build area of size "+size);
		defined.put("AREA_SIZE",size);
		final List<String> ignoreStats=new XArrayList<String>(new String[]{"CLASS","NAME","DESCRIPTION","LAYOUT","SIZE"});
		fillOutStatCodes(A, ignoreStats,"AREA_",piece,defined);

		final List<Ability> aV = findAffects(A,piece,defined,null);
		for(int i=0;i<aV.size();i++)
		{
			final Ability AB=aV.get(i);
			A.setSavable(true);
			A.addNonUninvokableEffect(AB);
		}
		final List<Behavior> bV = findBehaviors(A,piece,defined);
		for(int i=0;i<bV.size();i++)
		{
			final Behavior B=bV.get(i);
			B.setSavable(true);
			A.addBehavior(B);
		}

		CMLib.map().addArea(A); // necessary for proper naming.

		try
		{
			layOutRooms(A, layoutManager, CMath.s_int(size), direction, piece, defined);
			CMLib.map().delArea(A); // we added it for id assignment, now we are done.
		}
		catch(final Exception t)
		{
			CMLib.map().delArea(A);
			CMLib.map().emptyAreaAndDestroyRooms(A);
			A.destroy();
			if(t instanceof CMException)
				throw (CMException)t;
			throw new CMException(t.getMessage(),t);
		}
		return true;
	}

	protected Room processRoom(final Area A, final int direction, final XMLTag piece, final LayoutNode node, final Map<String,Object> groupDefined)
		throws CMException
	{
		for(final LayoutTags key : node.tags().keySet())
			groupDefined.put("ROOMTAG_"+key.toString().toUpperCase(),node.tags().get(key));
		final Exit[] exits=new Exit[Directions.NUM_DIRECTIONS()];
		for(final Integer linkDir : node.links().keySet())
		{
			final LayoutNode linkNode = node.links().get(linkDir);
			if(linkNode.room() != null)
			{
				final int opDir=Directions.getOpDirectionCode(linkDir.intValue());
				exits[linkDir.intValue()]=linkNode.room().getExitInDir(opDir);
				groupDefined.put("ROOMTITLE_"+CMLib.directions().getDirectionChar(linkDir.intValue()).toUpperCase(),linkNode.room().displayText(null));
			}
			groupDefined.put("NODETYPE_"+CMLib.directions().getDirectionChar(linkDir.intValue()).toUpperCase(),linkNode.type().name());
			//else groupDefined.put("ROOMTITLE_"+CMLib.directions().getDirectionChar(linkDir.intValue()).toUpperCase(),"");
			groupDefined.put("ROOMLINK_"+CMLib.directions().getDirectionChar(linkDir.intValue()).toUpperCase(),"true");
		}
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
				Log.debugOut("MUDPercolator",A.Name()+": type: "+node.type().toString());
			final StringBuffer defs=new StringBuffer("");
			for (final String key : groupDefined.keySet())
			{
				defs.append(key+"="+CMStrings.limit(groupDefined.get(key).toString(),10)+",");
			}
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
				Log.debugOut("MUDPercolator","DEFS: "+defs.toString());
		}
		final Room R=findRoom(A,piece, groupDefined, exits, direction);
		if(R==null)
			throw new CMException("Failure to generate room from "+piece.value());
		R.setRoomID(A.getNewRoomID(null,-1));
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
			Log.debugOut("MUDPercolator","ROOMID: "+R.roomID());
		R.setArea(A);
		A.addProperRoom(R);
		node.setRoom(R);
		groupDefined.remove("ROOMTAG_NODEGATEEXIT");
		groupDefined.remove("ROOMTAG_GATEEXITROOM");
		for(final LayoutTags key : node.tags().keySet())
			groupDefined.remove("ROOMTAG_"+key.toString().toUpperCase());
		for(final Integer linkDir : node.links().keySet())
		{
			groupDefined.remove("NODETYPE_"+CMLib.directions().getDirectionChar(linkDir.intValue()).toUpperCase());
			groupDefined.remove("ROOMLINK_"+CMLib.directions().getDirectionChar(linkDir.intValue()).toUpperCase());
			groupDefined.remove("ROOMTITLE_"+CMLib.directions().getDirectionChar(linkDir.intValue()).toUpperCase());
		}
		return R;
	}

	@Override
	public List<MOB> findMobs(final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		return findMobs(null,piece, defined, null);
	}

	protected List<MOB> findMobs(final Modifiable E,final XMLTag piece, final Map<String,Object> defined, final BuildCallback callBack) throws CMException
	{
		try
		{
			final List<MOB> V = new Vector<MOB>();
			final String tagName="MOB";
			final List<XMLLibrary.XMLTag> choices = getAllChoices(E,null,null,tagName, piece, defined,true);
			if((choices==null)||(choices.size()==0))
				return V;
			for(int c=0;c<choices.size();c++)
			{
				final XMLTag valPiece = choices.get(c);
				if(valPiece.parms().containsKey("VALIDATE") && !testCondition(E,null,null,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
					continue;
				defineReward(E,null,null,valPiece.getParmValue("DEFINE"),valPiece,null,defined,true);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
					Log.debugOut("MUDPercolator","Build Mob: "+CMStrings.limit(CMStrings.deleteCRLFTAB(valPiece.value()),80)+"...");
				final Set<String> definedSet=getPrevouslyDefined(defined,tagName+"_");
				final MOB M=buildMob(valPiece,defined);
				if(callBack != null)
					callBack.willBuild(M, valPiece);
				V.add(M);
				clearNewlyDefined(defined, definedSet, tagName+"_");
			}
			return V;
		}
		catch(final PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
	}

	protected Room findRoom(final Area A, final XMLTag piece, final Map<String,Object> defined, final Exit[] exits, final int directions) throws CMException
	{
		try
		{
			final String tagName="ROOM";
			final List<XMLLibrary.XMLTag> choices = getAllChoices(null,null,null,tagName, piece, defined,true);
			if((choices==null)||(choices.size()==0))
			{
				return null;
			}
			while(choices.size()>0)
			{
				final XMLLibrary.XMLTag valPiece = choices.get(CMLib.dice().roll(1,choices.size(),-1));
				choices.remove(valPiece);
				if(valPiece.parms().containsKey("VALIDATE") && !testCondition(null,null,null,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
					continue;
				final Map<String,Object> rDefined=new Hashtable<String,Object>();
				rDefined.putAll(defined);
				defineReward(null,null,null,valPiece.getParmValue("DEFINE"),valPiece,null,rDefined,true);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
					Log.debugOut("MUDPercolator","Build Room: "+CMStrings.limit(CMStrings.deleteCRLFTAB(valPiece.value()),80)+"...");
				Room R;
				final String layoutType=valPiece.parms().get("LAYOUT");
				if((layoutType!=null)&&(layoutType.length()>0))
				{
					final LayoutManager layoutManager = getLayoutManager(layoutType);
					if(layoutManager == null)
						throw new CMException("Undefined room Layout "+layoutType);
					rDefined.put("ROOM_LAYOUT",layoutManager.name());
					final String size = findStringNow("size",valPiece,rDefined);
					if((!CMath.isInteger(size))||(CMath.s_int(size)<=0))
						throw new CMException("Unable to build room layout of size "+size);
					defined.put("ROOM_SIZE",size);
					R=layOutRooms(A, layoutManager, CMath.s_int(size), directions, valPiece, rDefined);
				}
				else
				{
					final Exit[] rExits=exits.clone();
					R=buildRoom(valPiece,rDefined,rExits,directions);
					for(int e=0;e<rExits.length;e++)
						exits[e]=rExits[e];
				}
				for (final String key : rDefined.keySet())
				{
					if(key.startsWith("_"))
					{
						if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
							Log.debugOut("RGDEF:"+key+"="+CMStrings.limit(rDefined.get(key).toString(), 10));
						defined.put(key,rDefined.get(key));
					}
				}
				return R;
			}
		}
		catch(final PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
		return null;
	}

	protected PairVector<Room,Exit[]> findRooms(final XMLTag piece, final Map<String,Object> defined, final Exit[] exits, final int direction) throws CMException
	{
		try
		{
			final PairVector<Room,Exit[]> DV = new PairVector<Room,Exit[]>();
			final String tagName="ROOM";
			final List<XMLLibrary.XMLTag> choices = getAllChoices(null,null,null,tagName, piece, defined,true);
			if((choices==null)||(choices.size()==0))
				return DV;
			for(int c=0;c<choices.size();c++)
			{
				final XMLTag valPiece = choices.get(c);
				if(valPiece.parms().containsKey("VALIDATE") && !testCondition(null,null,null,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
					continue;
				defineReward(null,null,null,valPiece.getParmValue("DEFINE"),valPiece,null,defined,true);
				final Exit[] theseExits=exits.clone();
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
					Log.debugOut("MUDPercolator","Build Room: "+CMStrings.limit(CMStrings.deleteCRLFTAB(valPiece.value()),80)+"...");
				final Room R=buildRoom(valPiece,defined,theseExits,direction);
				DV.addElement(R,theseExits);
			}
			return DV;
		}
		catch(final PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
	}

	protected Exit findExit(final Modifiable M, final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		try
		{
			final String tagName="EXIT";
			final List<XMLLibrary.XMLTag> choices = getAllChoices(M,null,null,tagName, piece, defined,true);
			if((choices==null)||(choices.size()==0))
				return null;
			final List<Exit> exitChoices = new Vector<Exit>();
			for(int c=0;c<choices.size();c++)
			{
				final XMLTag valPiece = choices.get(c);
				if(valPiece.parms().containsKey("VALIDATE") && !testCondition(M,null,null,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
					continue;
				defineReward(M,null,null,valPiece.getParmValue("DEFINE"),valPiece,null,defined,true);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
					Log.debugOut("MUDPercolator","Build Exit: "+CMStrings.limit(CMStrings.deleteCRLFTAB(valPiece.value()),80)+"...");
				final Exit E=buildExit(valPiece,defined);
				if(E!=null)
					exitChoices.add(E);
			}
			if(exitChoices.size()==0)
				return null;
			return exitChoices.get(CMLib.dice().roll(1,exitChoices.size(),-1));
		}
		catch(final PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
	}

	private static class Varidentifier
	{
		public int outerStart=-1;
		public int outerEnd=-1;
		public String var=null;
		public boolean toLowerCase=false;
		public boolean toUpperCase=false;
		public boolean toJavascript=false;
		public boolean toCapitalized=false;
		public boolean toPlural=false;
		public boolean isMathExpression=false;
		public boolean toOneWord=false;
		public boolean toOneLine=false;
	}

	protected List<Varidentifier> parseVariables(final String str)
	{
		int x=str.indexOf('$');
		final List<Varidentifier> list=new XVector<Varidentifier>();
		while((x>=0)&&(x<str.length()-1))
		{
			final Varidentifier var = new Varidentifier();
			var.outerStart=x;
			x++;
			if((x<str.length())&&(str.charAt(x)=='{'))
			{
				int varstart=var.outerStart;
				x++;
				while((x<str.length()-2)&&(str.charAt(x+1)==':'))
				{
					switch(str.charAt(x))
					{
					case 'l': case 'L':
						var.toLowerCase=true;
						break;
					case 'u': case 'U':
						var.toUpperCase=true;
						break;
					case 'j': case 'J':
						var.toJavascript=true;
						break;
					case '1':
						var.toOneLine=true;
						break;
					case 'p': case 'P':
						var.toPlural=true;
						break;
					case 'c': case 'C':
						var.toCapitalized=true;
						break;
					case '_':
						var.toOneWord=true;
						break;
					}
					x+=2;
					varstart+=2;
				}
				int depth=0;
				while((x<str.length())
				&&((str.charAt(x)!='}')||(depth>0)))
				{
					if(str.charAt(x)=='{')
						depth++;
					else
					if(str.charAt(x)=='}')
						depth--;
					x++;
				}
				var.var = str.substring(varstart+2,x);
				if(x<str.length())
					x++;
				var.outerEnd=x;
			}
			else
			if((x<str.length())&&(str.charAt(x)=='['))
			{
				final int varstart=var.outerStart;
				x++;
				int depth=0;
				while((x<str.length())
				&&((str.charAt(x)!=']')||(depth>0)))
				{
					if(str.charAt(x)=='[')
						depth++;
					else
					if(str.charAt(x)==']')
						depth--;
					x++;
				}
				var.var = str.substring(varstart+2,x);
				var.isMathExpression=true;
				if(x<str.length())
					x++;
				var.outerEnd=x;
			}
			else
			{
				while((x<str.length())&&((str.charAt(x)=='_')||Character.isLetterOrDigit(str.charAt(x))))
					x++;
				var.var = str.substring(var.outerStart+1,x);
				var.outerEnd=x;
				if((var.var.length()==0)
				&&(x<str.length())
				&&(str.charAt(x)=='$'))
				{
					x=str.indexOf('$',x+1);
					continue;
				}
			}
			list.add(var);
			x=str.indexOf('$',var.outerEnd);
		}
		return list;
	}

	protected String fillOutStatCode(final Modifiable E, final List<String> ignoreStats, final String defPrefix, final String stat, final XMLTag piece, final Map<String,Object> defined, final boolean debug)
	{
		if(!ignoreStats.contains(stat.toUpperCase().trim()))
		{
			try
			{
				return PostProcessAttempter(defined,new PostProcessAttempt()
				{
					@Override
					public String attempt() throws CMException, PostProcessException
					{
						String value;
						if(stat.equals("ABILITY"))
						{
							if(E instanceof MOB)
								value = findOptionalString(E,ignoreStats,defPrefix,"HPMOD",piece,this.defined, debug);
							else
								value = findOptionalString(E,ignoreStats,defPrefix,"MAGICABILITY",piece,this.defined, debug);
						}
						else
							value = findOptionalString(E,ignoreStats,defPrefix,stat,piece,this.defined, debug);
						if(value != null)
						{
							E.setStat(stat, value);
							if((defPrefix!=null)&&(defPrefix.length()>0))
								addDefinition(defPrefix+stat,value,this.defined);
						}
						return value;
					}
				});
			}
			catch(final CMException e)
			{
				Log.errOut(e);
				//should never happen
			}
		}
		return null;
	}

	protected void fillOutStatCodes(final Modifiable E, final List<String> ignoreStats, final String defPrefix, final XMLTag piece, final Map<String,Object> defined)
	{
		final String[] statCodes = E.getStatCodes();
		for (final String stat : statCodes)
		{
			fillOutStatCode(E,ignoreStats,defPrefix,stat,piece,defined, false);
		}
	}

	protected void fillOutCopyStats(final Modifiable E, final Modifiable E2)
	{
		if((E2 instanceof MOB)&&(!((MOB)E2).isGeneric()))
		{
			for(final GenericBuilder.GenMOBCode stat : GenericBuilder.GenMOBCode.values())
			{
				if(stat != GenericBuilder.GenMOBCode.ABILITY) // because this screws up gen hit points
				{
					E.setStat(stat.name(), CMLib.coffeeMaker().getGenMobStat((MOB)E2,stat.name()));
				}
			}
		}
		else
		if((E2 instanceof Item)&&(!((Item)E2).isGeneric()))
		{
			for(final GenericBuilder.GenItemCode stat : GenericBuilder.GenItemCode.values())
			{
				E.setStat(stat.name(),CMLib.coffeeMaker().getGenItemStat((Item)E2,stat.name()));
			}
		}
		else
		for(final String stat : E2.getStatCodes())
		{
			E.setStat(stat, E2.getStat(stat));
		}
	}

	protected boolean fillOutCopyCodes(final Modifiable E, final List<String> ignoreStats, final String defPrefix, final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		final String copyStatID = findOptionalStringNow(E,ignoreStats,defPrefix,"COPYOF",piece,defined, false);
		if(copyStatID!=null)
		{
			final List<String> V=CMParms.parseCommas(copyStatID,true);
			for(int v=0;v<V.size();v++)
			{
				String s = V.get(v);
				if(s.startsWith("$"))
					s=s.substring(1).trim();
				final XMLTag statPiece =(XMLTag)defined.get(s.toUpperCase().trim());
				if(statPiece == null)
				{
					Object o=CMClass.getMOBPrototype(s);
					if(o==null)
						o=CMClass.getItemPrototype(s);
					if(o==null)
						o=CMClass.getObjectOrPrototype(s);
					if(!(o instanceof Modifiable))
						throw new CMException("Invalid copystat: '"+s+"' on piece '"+piece.tag()+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
					final Modifiable E2=(Modifiable)o;
					if(o instanceof MOB)
					{
						((MOB)E2).setBaseCharStats((CharStats)((MOB)o).baseCharStats().copyOf());
						((MOB)E2).setBasePhyStats((PhyStats)((MOB)o).basePhyStats().copyOf());
						((MOB)E2).setBaseState((CharState)((MOB)o).baseState().copyOf());
					}
					fillOutCopyStats(E,E2);
				}
				else
				{
					final XMLTag likePiece =(XMLTag)defined.get(s.toUpperCase().trim());
					if(likePiece == null)
						throw new CMException("Invalid copystat: '"+s+"' on piece '"+piece.tag()+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
					final BuildCallback callBack=new BuildCallback()
					{
						@Override
						public void willBuild(final Environmental E2, final XMLTag XMLTag)
						{
							fillOutCopyStats(E,E2);
						}
					};
					findItems(E,likePiece,defined,callBack);
					findMobs(E,likePiece,defined,callBack);
					findAbilities(E,likePiece,defined,callBack);
					findExits(E,likePiece,defined,callBack);
				}
			}
			return V.size()>0;
		}
		return false;
	}

	protected MOB buildMob(final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		final String classID = findStringNow("class",piece,defined);
		MOB M = null;
		final List<String> ignoreStats=new XArrayList<String>();
		boolean copyFilled = false;
		if(classID.equalsIgnoreCase("catalog"))
		{
			final String name = findStringNow("NAME",piece,defined);
			if((name == null)||(name.length()==0))
				throw new CMException("Unable to build a catalog mob without a name, Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			M = CMLib.catalog().getCatalogMob(name);
			if(M==null)
				throw new CMException("Unable to find cataloged mob called '"+name+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			M=(MOB)M.copyOf();
			CMLib.catalog().changeCatalogUsage(M,true);
			addDefinition("MOB_CLASS",M.ID(),defined);
			copyFilled=true;
		}
		else
		{
			M = CMClass.getMOB(classID);
			if(M == null)
				throw new CMException("Unable to build mob on classID '"+classID+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			addDefinition("MOB_CLASS",classID,defined);

			if(M.isGeneric())
			{
				copyFilled = fillOutCopyCodes(M,ignoreStats,"MOB_",piece,defined);
				String name = fillOutStatCode(M,ignoreStats,"MOB_","NAME",piece,defined, false);
				if((!copyFilled) && ((name == null)||(name.length()==0)))
					name = fillOutStatCode(M,ignoreStats,"MOB_","NAME",piece,defined, false);
				if((!copyFilled) && ((name == null)||(name.length()==0)))
				{
					name = fillOutStatCode(M,ignoreStats,"MOB_","NAME",piece,defined, true);
					if((!copyFilled) && ((name == null)||(name.length()==0)))
						throw new CMException("Unable to build a mob without a name, Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
				}
				if((name != null)&&(name.length()>0))
					M.setName(name);
			}
		}
		final MOB mob=M;
		addDefinition("MOB_NAME",M.Name(),defined);
		if(!copyFilled)
			M.baseCharStats().setMyRace(CMClass.getRace("StdRace"));

		PostProcessAttempter(defined,new PostProcessAttempt()
		{
			@Override
			public String attempt() throws CMException, PostProcessException
			{
				final String value = findOptionalString(mob,ignoreStats,"MOB_","LEVEL",piece,this.defined, false);
				if((value != null)&&(value.length()>0))
				{
					mob.setStat("LEVEL",value);
					addDefinition("MOB_LEVEL",value,this.defined);
					CMLib.leveler().fillOutMOB(mob,mob.basePhyStats().level());
					CMLib.leveler().fillOutMOB(mob,mob.basePhyStats().level());
				}
				return value;
			}
		});

		PostProcessAttempter(defined,new PostProcessAttempt()
		{
			@Override
			public String attempt() throws CMException, PostProcessException
			{
				final String value = findOptionalString(mob,ignoreStats,"MOB_","GENDER",piece,this.defined, false);
				if((value != null)&&(value.length()>0))
				{
					mob.baseCharStats().setStat(CharStats.STAT_GENDER,value.charAt(0));
					addDefinition("MOB_GENDER",value,this.defined);
				}
				else
					mob.baseCharStats().setStat(CharStats.STAT_GENDER,CMLib.dice().rollPercentage()>50?'M':'F');
				PostProcessAttempter(this.defined,new PostProcessAttempt()
				{
					@Override
					public String attempt() throws CMException, PostProcessException
					{
						final String value = findOptionalString(mob,ignoreStats,"MOB_","RACE",piece,this.defined, false);
						if((value != null)&&(value.length()>0))
						{
							mob.setStat("RACE",value);
							addDefinition("MOB_RACE",value,this.defined);
							Race R=CMClass.getRace(value);
							if(R==null)
							{
								final List<Race> races=findRaces(mob,piece, this.defined);
								if(races.size()>0)
									R=races.get(CMLib.dice().roll(1, races.size(), -1));
							}
							if(R!=null)
								R.setHeightWeight(mob.basePhyStats(),(char)mob.baseCharStats().getStat(CharStats.STAT_GENDER));
						}
						return value;
					}
				});
				return value;
			}
		});

		PostProcessAttempter(defined, new PostProcessAttempt()
		{
			@Override
			public String attempt() throws CMException, PostProcessException
			{
				final List<XMLLibrary.XMLTag> choices = getAllChoices(mob,ignoreStats,"MOB_","FACTION", piece, this.defined, true);
				if((choices!=null)&&(choices.size()>0))
				{
					for(int c=0;c<choices.size();c++)
					{
						final XMLTag valPiece = choices.get(c);
						final String f = valPiece.getParmValue("ID");
						final String v = valPiece.getParmValue("VALUE");
						mob.addFaction(f, Integer.parseInt(v));
					}
				}
				return "";
			}
		});
		ignoreStats.addAll(Arrays.asList(new String[]{"CLASS","NAME","LEVEL","GENDER","RACE"}));
		fillOutStatCodes(M,ignoreStats,"MOB_",piece,defined);
		fillOutStatCodes(M.baseCharStats(),MOB_IGNORE_STATS,"MOB_",piece,defined);
		fillOutStatCodes(M.basePhyStats(),MOB_IGNORE_STATS,"MOB_",piece,defined);
		fillOutStatCodes(M.baseState(),MOB_IGNORE_STATS,"MOB_",piece,defined);
		M.recoverCharStats();
		M.recoverPhyStats();
		M.recoverMaxState();

		final List<Item> items = findItems(piece,defined);
		for(int i=0;i<items.size();i++)
		{
			final Item I=items.get(i);
			final boolean wearable = (I.phyStats().sensesMask()&PhyStats.SENSE_ITEMNOAUTOWEAR)==0;
			M.addItem(I);
			I.setSavable(true);
			if(wearable)
				I.wearIfPossible(M);
		}
		final List<Ability> aV = findAffects(M,piece,defined,null);
		for(int i=0;i<aV.size();i++)
		{
			final Ability A=aV.get(i);
			A.setSavable(true);
			M.addNonUninvokableEffect(A);
		}
		final List<Behavior> bV= findBehaviors(M,piece,defined);
		for(int i=0;i<bV.size();i++)
		{
			final Behavior B=bV.get(i);
			B.setSavable(true);
			M.addBehavior(B);
		}
		final List<Ability> abV = findAbilities(M,piece,defined,null);
		for(int i=0;i<abV.size();i++)
		{
			final Ability A=abV.get(i);
			A.setSavable(true);
			M.addAbility(A);
		}
		final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
		if(SK!=null)
		{
			final CoffeeShop shop=(SK instanceof Librarian)?((Librarian)SK).getBaseLibrary():SK.getShop();
			final List<Triad<Environmental,Integer,Long>> iV = findShopInventory(M,piece,defined);
			if(iV.size()>0)
				shop.emptyAllShelves();
			for(int i=0;i<iV.size();i++)
				shop.addStoreInventory(iV.get(i).first,iV.get(i).second.intValue(),iV.get(i).third.intValue());
		}

		M.recoverCharStats();
		M.recoverPhyStats();
		M.recoverMaxState();
		M.text();
		M.setMiscText(M.text());
		M.recoverCharStats();
		M.recoverPhyStats();
		M.recoverMaxState();
		return M;
	}

	protected List<Exit> findExits(final Modifiable M,final XMLTag piece, final Map<String,Object> defined, final BuildCallback callBack) throws CMException
	{
		try
		{
			final List<Exit> V = new Vector<Exit>();
			final String tagName="EXIT";
			final List<XMLLibrary.XMLTag> choices = getAllChoices(null,null,null,tagName, piece, defined,true);
			if((choices==null)||(choices.size()==0))
				return V;
			for(int c=0;c<choices.size();c++)
			{
				final XMLTag valPiece = choices.get(c);
				if(valPiece.parms().containsKey("VALIDATE") && !testCondition(M,null,null,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
					continue;
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
					Log.debugOut("MUDPercolator","Build Exit: "+CMStrings.limit(CMStrings.deleteCRLFTAB(valPiece.value()),80)+"...");
				defineReward(M,null,null,valPiece.getParmValue("DEFINE"),valPiece,null,defined,true);
				final Set<String> definedSet=getPrevouslyDefined(defined,tagName+"_");
				final Exit E=buildExit(valPiece,defined);
				if(callBack != null)
					callBack.willBuild(E, valPiece);
				V.add(E);
				clearNewlyDefined(defined, definedSet, tagName+"_");
			}
			return V;
		}
		catch(final PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
	}

	// remember to check ROOMLINK_DIR for N,S,E,W,U,D,etc..
	protected Exit buildExit(final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		final List<String> ignoreStats=new XArrayList<String>();
		final String classID = findStringNow("class",piece,defined);
		final Exit E = CMClass.getExit(classID);
		if(E == null)
			throw new CMException("Unable to build exit on classID '"+classID+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
		addDefinition("EXIT_CLASS",classID,defined);
		ignoreStats.add("CLASS");
		fillOutCopyCodes(E,ignoreStats,"EXIT_",piece,defined);
		fillOutStatCodes(E,ignoreStats,"EXIT_",piece,defined);
		ignoreStats.addAll(Arrays.asList(E.getStatCodes()));
		fillOutStatCodes(E.basePhyStats(),ignoreStats,"EXIT_",piece,defined);
		final List<Ability> aV = findAffects(E,piece,defined,null);
		for(int i=0;i<aV.size();i++)
		{
			final Ability A=aV.get(i);
			A.setSavable(true);
			E.addNonUninvokableEffect(A);
		}
		final List<Behavior> bV= findBehaviors(E,piece,defined);
		for(int i=0;i<bV.size();i++)
		{
			final Behavior B=bV.get(i);
			B.setSavable(true);
			E.addBehavior(B);
		}
		E.text();
		E.setMiscText(E.text());
		E.recoverPhyStats();
		return E;
	}

	protected List<Triad<Environmental,Integer,Long>> findShopInventory(final Modifiable E,final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		try
		{
			final List<Triad<Environmental,Integer,Long>> V = new Vector<Triad<Environmental,Integer,Long>>();
			final List<XMLLibrary.XMLTag> choices = getAllChoices(E,null,null,"SHOPINVENTORY", piece, defined,true);
			if((choices==null)||(choices.size()==0))
				return V;
			for(int c=0;c<choices.size();c++)
			{
				final XMLTag shopPiece = choices.get(c);
				if(shopPiece.parms().containsKey("VALIDATE") && !testCondition(E,null,null,CMLib.xml().restoreAngleBrackets(shopPiece.getParmValue("VALIDATE")),shopPiece, defined))
					continue;
				final String baseNumber[] = { "1" };
				final String basePrice[] = { "-1" };
				try
				{
					final String baseStr=shopPiece.getParmValue("NUMBER");
					if(baseStr != null)
						baseNumber[0]=baseStr;
				}
				catch (final Exception e)
				{
				}
				try
				{
					final String baseStr=shopPiece.getParmValue("PRICE");
					if(baseStr != null)
						basePrice[0]=baseStr;
				}
				catch (final Exception e)
				{
				}
				final BuildCallback callBack=new BuildCallback()
				{
					@Override
					public void willBuild(final Environmental E, final XMLTag XMLTag)
					{
						String numbStr=XMLTag.getParmValue("NUMBER");
						if(numbStr == null)
							numbStr=baseNumber[0];
						int number;
						try
						{
							number = CMath.parseIntExpression(strFilter(E, null, null, numbStr.trim(), piece, defined));
						}
						catch (final Exception e)
						{
							number = 1;
						}
						numbStr=XMLTag.getParmValue("PRICE");
						if(numbStr == null)
							numbStr=basePrice[0];
						long price;
						try
						{
							price = CMath.parseLongExpression(strFilter(E, null, null, numbStr.trim(), piece, defined));
						}
						catch (final Exception e)
						{
							price = -1;
						}
						V.add(new Triad<Environmental,Integer,Long>(E,Integer.valueOf(number),Long.valueOf(price)));
					}
				};
				findItems(E,shopPiece,defined,callBack);
				findMobs(E,shopPiece,defined,callBack);
				findAbilities(E,shopPiece,defined,callBack);
			}
			return V;
		}
		catch(final PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
	}

	protected Set<String> getPrevouslyDefined(final Map<String,Object> defined, final String prefix)
	{
		final Set<String> prevSet=new HashSet<String>();
		for(final String key : defined.keySet())
		{
			if(key.toUpperCase().startsWith(prefix.toUpperCase()))
				prevSet.add(key.toUpperCase());
		}
		return prevSet;
	}

	protected void clearNewlyDefined(final Map<String,Object> defined, final Set<String> exceptSet, final String prefix)
	{
		final Set<String> clearSet=new HashSet<String>();
		for(final String key : defined.keySet())
			if(key.toUpperCase().startsWith(prefix.toUpperCase())
			&& (!exceptSet.contains(key.toUpperCase()))
			&& (!key.startsWith("_")))
				clearSet.add(key);
		for(final String key : clearSet)
			defined.remove(key);
	}

	@Override
	public List<Item> findItems(final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		return findItems(null,piece, defined, null);
	}

	protected List<Item> findItems(final Modifiable E,final XMLTag piece, final Map<String,Object> defined, final BuildCallback callBack) throws CMException
	{
		try
		{
			final List<Item> V = new Vector<Item>();
			final String tagName="ITEM";
			final List<XMLLibrary.XMLTag> choices = getAllChoices(E,null,null,tagName, piece, defined,true);
			if((choices==null)||(choices.size()==0))
				return V;
			for(int c=0;c<choices.size();c++)
			{
				final XMLTag valPiece = choices.get(c);
				if(valPiece.parms().containsKey("VALIDATE") && !testCondition(E,null,null,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
					continue;
				defineReward(E,null,null,valPiece.getParmValue("DEFINE"),valPiece,null,defined,true);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
					Log.debugOut("MUDPercolator","Build Item: "+CMStrings.limit(CMStrings.deleteCRLFTAB(valPiece.value()),80)+"...");
				final Set<String> definedSet=getPrevouslyDefined(defined,tagName+"_");
				try
				{
					for(final Item I : buildItem(valPiece,defined))
					{
						if(callBack != null)
							callBack.willBuild(I, valPiece);
						V.add(I);
					}
				}
				catch(final CMException e)
				{
					throw e;
				}
				clearNewlyDefined(defined, definedSet, tagName+"_");
			}
			return V;
		}
		catch(final PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
	}

	protected List<Item> findContents(final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		try
		{
			final List<Item> V = new Vector<Item>();
			final String tagName="CONTENT";
			final List<XMLLibrary.XMLTag> choices = getAllChoices(null,null,null,tagName, piece, defined,true);
			if((choices==null)||(choices.size()==0))
				return V;
			for(int c=0;c<choices.size();c++)
			{
				final XMLTag valPiece = choices.get(c);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
					Log.debugOut("MUDPercolator","Found Content: "+valPiece.value());
				V.addAll(findItems(valPiece,defined));
			}
			return V;
		}
		catch(final PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
	}

	protected String getMetacraftFilter(String recipe, final XMLTag piece, final Map<String,Object> defined, final Triad<Integer,Integer,Class<?>[]> filter) throws CMException
	{
		int levelLimit=-1;
		int levelFloor=-1;
		Class<?>[] deriveClasses=new Class[0];
		final Map.Entry<Character, String>[] otherParms=CMStrings.splitMulti(recipe, splitters);
		recipe=otherParms[0].getValue();
		if(otherParms.length==1)
			return recipe;
		for(int i=1;i<otherParms.length;i++)
		{
			switch(otherParms[i].getKey().charValue())
			{
			case '<':
			{
				final String lvlStr=strFilterNow(null,null,null,otherParms[i].getValue().trim(),piece, defined);
				if(CMath.isMathExpression(lvlStr))
				{
					levelLimit=CMath.parseIntExpression(lvlStr);
					if((levelLimit==0)||(levelLimit<levelFloor))
						levelLimit=-1;
				}
				break;
			}
			case '>':
			{
				final String lvlStr=strFilterNow(null,null,null,otherParms[i].getValue().trim(),piece, defined);
				if(CMath.isMathExpression(lvlStr))
				{
					levelFloor=CMath.parseIntExpression(lvlStr);
					if((levelFloor==0)||((levelFloor>levelLimit)&&(levelLimit>0)))
						levelFloor=-1;
				}
				break;
			}
			case '=':
			{
				final String classStr=strFilterNow(null,null,null,otherParms[i].getValue().trim(),piece, defined);
				final Object O=CMClass.getItemPrototype(classStr);
				if(O!=null)
				{
					deriveClasses=Arrays.copyOf(deriveClasses, deriveClasses.length+1);
					deriveClasses[deriveClasses.length-1]=O.getClass();
				}
				else
					throw new CMException("Unknown metacraft class= "+classStr);
				break;
			}
			default:
				break;
			}
		}
		if(levelLimit>0)
			filter.first=Integer.valueOf(levelLimit);
		if(levelFloor>0)
			filter.second=Integer.valueOf(levelFloor);
		if(deriveClasses.length>0)
			filter.third=deriveClasses;
		return recipe;
	}

	@SuppressWarnings("unchecked")
	protected List<ItemCraftor.ItemKeyPair> craftAllOfThisRecipe(final ItemCraftor skill, final int material, final Map<String,Object> defined)
	{
		List<ItemCraftor.ItemKeyPair> skillContents;
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.ITEMGENCACHE))
			skillContents=(List<ItemCraftor.ItemKeyPair>)defined.get("____COFFEEMUD_"+skill.ID()+"_"+material+"_true");
		else
			skillContents=(List<ItemCraftor.ItemKeyPair>)Resources.getResource("SYSTEM_ITEMGEN_"+skill.ID()+"_"+material+"_true");
		if(skillContents==null)
		{
			if(material>=0)
				skillContents=skill.craftAllItemSets(material, true);
			else
				skillContents=skill.craftAllItemSets(true);
			if(skillContents==null)
				return null;
			if(CMSecurity.isDisabled(CMSecurity.DisFlag.ITEMGENCACHE))
				defined.put("____COFFEEMUD_"+skill.ID()+"_"+material+"_true",skillContents);
			else
				Resources.submitResource("SYSTEM_ITEMGEN_"+skill.ID()+"_"+material+"_true", skillContents);
		}
		final List<ItemCraftor.ItemKeyPair> skillContentsCopy=new Vector<ItemCraftor.ItemKeyPair>(skillContents.size());
		skillContentsCopy.addAll(skillContents);
		return skillContentsCopy;
	}

	protected boolean checkMetacraftItem(final Item I, final Triad<Integer,Integer,Class<?>[]> filter)
	{
		final int levelLimit=filter.first.intValue();
		final int levelFloor=filter.second.intValue();
		final Class<?>[] deriveClasses=filter.third;
		if(((levelLimit>0) && (I.basePhyStats().level() > levelLimit))
		||((levelFloor>0) && (I.basePhyStats().level() <= levelFloor)))
			return false;
		if(deriveClasses.length==0)
			return true;
		for(final Class<?> C : deriveClasses)
		{
			if(C.isAssignableFrom(I.getClass()))
				return true;
		}
		return false;
	}

	protected List<Item> buildItem(final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		final SHashtable<String,Object> preContentDefined = new SHashtable<String,Object>(defined);
		final String classID = findStringNow("class",piece,defined);
		final List<Item> contents = new Vector<Item>();
		final List<String> ignoreStats=new XArrayList<String>();
		final int senseFlag = CMath.s_bool(findOptionalStringNow(null,null,null,"nowear",piece,defined,false)) ? PhyStats.SENSE_ITEMNOAUTOWEAR : 0;
		if(classID.toLowerCase().startsWith("metacraft"))
		{
			final String classRest=classID.substring(9).toLowerCase().trim();
			final Triad<Integer,Integer,Class<?>[]> filter = new Triad<Integer,Integer,Class<?>[]>(Integer.valueOf(-1),Integer.valueOf(-1),new Class<?>[0]);
			String recipe="anything";
			if(classRest.startsWith(":"))
			{
				recipe=getMetacraftFilter(classRest.substring(1).trim(), piece, defined, filter);
			}
			else
			{
				recipe = findStringNow("NAME",piece,defined);
				if((recipe == null)||(recipe.length()==0))
					throw new CMException("Unable to metacraft with malformed class Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			}

			final String materialStr = findOptionalStringNow(null,null,null,"material",piece,defined, false);
			int material=-1;
			if(materialStr!=null)
				 material = RawMaterial.CODES.FIND_IgnoreCase(materialStr);
			@SuppressWarnings("unchecked")
			List<ItemCraftor> craftorPrototypes = (List<ItemCraftor>)defined.get("____SYSTEM_FILTERED_ITEM_CRAFTORS");
			if(craftorPrototypes == null)
			{
				craftorPrototypes=new Vector<ItemCraftor>();
				for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
				{
					final Ability A=e.nextElement();
					if(A instanceof ItemCraftor)
						craftorPrototypes.add((ItemCraftor)A);
				}
				defined.put("____SYSTEM_FILTERED_ITEM_CRAFTORS", craftorPrototypes);
			}
			final List<ItemCraftor> craftors = new ArrayList<ItemCraftor>(craftorPrototypes.size());
			for(final ItemCraftor I : craftorPrototypes)
				craftors.add((ItemCraftor)I.copyOf());

			if(recipe.startsWith("any"))
			{
				if(recipe.equalsIgnoreCase("anything"))
				{
					final long startTime=System.currentTimeMillis();
					while((contents.size()==0)&&((System.currentTimeMillis()-startTime)<1000))
					{
						final ItemCraftor skill=craftors.get(CMLib.dice().roll(1,craftors.size(),-1));
						if(skill.fetchRecipes().size()>0)
						{
							final List<ItemCraftor.ItemKeyPair> skillContents=craftAllOfThisRecipe(skill,material,defined);
							if(skillContents.size()==0) // preliminary error messaging, just for the craft skills themselves
								Log.errOut("MUDPercolator","Tried metacrafting anything, got "+Integer.toString(skillContents.size())+" from "+skill.ID());
							else
							for(int i=skillContents.size()-1;i>=0;i--)
							{
								final Item I=skillContents.get(i).item;
								if(!checkMetacraftItem(I, filter))
									skillContents.remove(i);
							}
							if(skillContents.size()>0)
							{
								final Item I=(Item)skillContents.get(CMLib.dice().roll(1,skillContents.size(),-1)).item.copyOf();
								contents.add(I);
							}
						}
					}
				}
				else
				if(recipe.toLowerCase().startsWith("any-"))
				{
					recipe=recipe.substring(4).toLowerCase().trim();
					if("rawmaterials".startsWith(recipe)||"resources".startsWith(recipe))
					{
						final int resourceCode;
						if(material > 0)
						{
							final List<Integer> availCodes = RawMaterial.CODES.COMPOSE_RESOURCES(material);
							resourceCode = availCodes.get(CMLib.dice().roll(1, availCodes.size(), -1)).intValue();
						}
						else
 							resourceCode=RawMaterial.CODES.ALL()[CMLib.dice().roll(1, RawMaterial.CODES.ALL().length, -1)];
						if(material != 0)
						{
							final Item I=CMLib.materials().makeItemResource(resourceCode);
							if(I.numBehaviors()>0 || I.numScripts()>0)
								CMLib.threads().deleteAllTicks(I);
							contents.add(I);
						}
					}
					else
					if("farmables".equals(recipe))
					{
						final List<Item> coll=CMLib.materials().getAllFarmables(material & RawMaterial.MATERIAL_MASK);
						if(coll.size()>0)
						{
							final Item I=(Item)coll.get(CMLib.dice().roll(1, coll.size(), -1)).copyOf();
							if(I.numBehaviors()>0 || I.numScripts()>0)
								CMLib.threads().deleteAllTicks(I);
							contents.add(I);
						}
					}
					else
					{
						List<ItemCraftor.ItemKeyPair> skillContents=null;
						for(final ItemCraftor skill : craftors)
						{
							if(skill.ID().equalsIgnoreCase(recipe))
							{
								skillContents=craftAllOfThisRecipe(skill,material,defined);
								if((skillContents==null)||(skillContents.size()==0)) // this is just for checking the skills themselves
									Log.errOut("MUDPercolator","Tried metacrafting any-"+recipe+", got "+Integer.toString(contents.size())+" from "+skill.ID());
								else
								for(int i=skillContents.size()-1;i>=0;i--)
								{
									final Item I=skillContents.get(i).item;
									if(!checkMetacraftItem(I, filter))
										skillContents.remove(i);
								}
								break;
							}
						}
						if((skillContents!=null)&&(skillContents.size()>0))
						{
							final Item I=(Item)skillContents.get(CMLib.dice().roll(1,skillContents.size(),-1)).item.copyOf();
							contents.add(I);
						}
					}
				}
				else
					throw new CMException("Unable to metacraft an item called '"+recipe+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			}
			else
			if(recipe.toLowerCase().startsWith("all"))
			{
				List<ItemCraftor.ItemKeyPair> skillContents=null;
				recipe=recipe.substring(3).startsWith("-")?recipe.substring(4).toLowerCase().trim():"";
				if("rawmaterials".startsWith(recipe)||"resources".startsWith(recipe))
				{
					if(material != 0)
					{
						if(material > 0)
						{
							for(final Integer rsc : RawMaterial.CODES.COMPOSE_RESOURCES(material))
							{
								final Item I=CMLib.materials().makeItemResource(rsc.intValue());
								if(I.numBehaviors()>0 || I.numScripts()>0)
									CMLib.threads().deleteAllTicks(I);
								contents.add(I);
							}
						}
						else
						{
							for(final int rsc : RawMaterial.CODES.ALL())
							{
								if(rsc != 0)
								{
									final Item I=CMLib.materials().makeItemResource(rsc);
									if(I.numBehaviors()>0 || I.numScripts()>0)
										CMLib.threads().deleteAllTicks(I);
									contents.add(I);
								}
							}
						}
					}
				}
				else
				if("farmables".equals(recipe))
				{
					for(final Item I : CMLib.materials().getAllFarmables(material))
					{
						final Item I2=(Item)I.copyOf();
						if(I2.numBehaviors()>0 || I2.numScripts()>0)
							CMLib.threads().deleteAllTicks(I2);
						contents.add(I2);
					}
				}
				else
				{
					for(final ItemCraftor skill : craftors)
					{
						if(skill.ID().equalsIgnoreCase(recipe)||(recipe.length()==0))
						{
							skillContents=craftAllOfThisRecipe(skill,material,defined);
							if((skillContents==null)||(skillContents.size()==0)) // this is just for checking the skills themselves
								Log.errOut("MUDPercolator","Tried metacrafting any-"+recipe+", got "+Integer.toString(contents.size())+" from "+skill.ID());
							else
							for(int i=skillContents.size()-1;i>=0;i--)
							{
								final Item I=skillContents.get(i).item;
								if(!checkMetacraftItem(I, filter))
									skillContents.remove(i);
							}
							while((skillContents!=null)&&(skillContents.size()>0))
							{
								final Item I=(Item)skillContents.remove(0).item.copyOf();
								contents.add(I);
							}
							if(recipe.length()>0)
								break;
						}
					}
				}
			}
			else
			{
				for(final ItemCraftor skill : craftors)
				{
					final List<List<String>> V=skill.matchingRecipeNames(recipe,false);
					if((V!=null)&&(V.size()>0))
					{
						ItemCraftor.ItemKeyPair pair;
						if(material>=0)
							pair=skill.craftItem(recipe,material,true, false);
						else
							pair=skill.craftItem(recipe,-1,true, false);
						if(pair!=null)
						{
							contents.add(pair.item);
							break;
						}
					}
				}
				for(int i=contents.size()-1;i>=0;i--)
				{
					final Item I=contents.get(i);
					if(!checkMetacraftItem(I, filter))
						contents.remove(i);
				}
				if(contents.size()==0)
				{
					for(final ItemCraftor skill : craftors)
					{
						final List<List<String>> V=skill.matchingRecipeNames(recipe,true);
						if((V!=null)&&(V.size()>0))
						{
							ItemCraftor.ItemKeyPair pair;
							if(material>=0)
								pair=skill.craftItem(recipe,material,true, false);
							else
								pair=skill.craftItem(recipe,0,true, false);
							if(pair!=null)
							{
								contents.add(pair.item);
								break;
							}
						}
					}
				}
				for(int i=contents.size()-1;i>=0;i--)
				{
					final Item I=contents.get(i);
					if(!checkMetacraftItem(I, filter))
						contents.remove(i);
				}
			}
			if(contents.size()==0)
			{
				if(filter.equals(emptyMetacraftFilter))
					throw new CMException("Unable to metacraft an item called '"+recipe+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
				else
					return new ArrayList<Item>(0);
			}
			for(final Item I : contents)
			{
				addDefinition("ITEM_CLASS",I.ID(),defined);
				addDefinition("ITEM_NAME",I.Name(),defined); // define so we can mess with it
				addDefinition("ITEM_LEVEL",""+I.basePhyStats().level(),defined); // define so we can mess with it
				fillOutStatCode(I,ignoreStats,"ITEM_","NAME",piece,defined, false);
				fillOutStatCode(I,ignoreStats,"ITEM_","LEVEL",piece,defined, false);
			}
			ignoreStats.addAll(Arrays.asList(new String[]{"CLASS","MATERIAL","NAME","LEVEL"}));
		}
		else
		if(classID.equalsIgnoreCase("catalog"))
		{
			final String name = findStringNow("NAME",piece,defined);
			if((name == null)||(name.length()==0))
				throw new CMException("Unable to build a catalog item without a name, Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			Item I = CMLib.catalog().getCatalogItem(name);
			if(I==null)
				throw new CMException("Unable to find cataloged item called '"+name+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			I=(Item)I.copyOf();
			CMLib.catalog().changeCatalogUsage(I,true);
			contents.add(I);
			addDefinition("ITEM_CLASS",I.ID(),defined);
			addDefinition("ITEM_NAME",I.Name(),defined); // define so we can mess with it
			ignoreStats.addAll(Arrays.asList(new String[]{"CLASS","NAME"}));
		}
		else
		{
			final Item I = CMClass.getItem(classID);
			if(I == null)
				throw new CMException("Unable to build item on classID '"+classID+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			contents.add(I);
			addDefinition("ITEM_CLASS",classID,defined);

			if(I.isGeneric())
			{
				final boolean filledOut = fillOutCopyCodes(I,ignoreStats,"ITEM_",piece,defined);
				final String name = fillOutStatCode(I,ignoreStats,"ITEM_","NAME",piece,defined, false);
				if((!filledOut) && ((name == null)||(name.length()==0)))
					throw new CMException("Unable to build an item without a name, Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
				if((name != null)&&(name.length()>0))
					I.setName(name);
			}
			ignoreStats.addAll(Arrays.asList(new String[]{"CLASS","NAME"}));
			addDefinition("ITEM_NAME",I.Name(),defined); // define so we can mess with it
		}

		final int contentSize=contents.size();
		SHashtable<String,Object> definedCopy = null;
		for(int it=0;it<contentSize;it++) // no iterator, please!!
		{
			final Item I=contents.get(it);
			fillOutStatCodes(I,ignoreStats,"ITEM_",piece,defined);
			I.recoverPhyStats();
			CMLib.itemBuilder().balanceItemByLevel(I);
			I.recoverPhyStats();
			fillOutStatCodes(I,ignoreStats,"ITEM_",piece,defined);
			fillOutStatCodes(I.basePhyStats(),ITEM_IGNORE_STATS,"ITEM_",piece,defined);
			I.recoverPhyStats();

			if(I instanceof Container)
			{
				if((definedCopy == null)||(definedCopy.isDirty()))
					definedCopy=preContentDefined.copyOf();
				final List<Item> V= findContents(piece,preContentDefined);
				for(int i=0;i<V.size();i++)
				{
					final Item I2=V.get(i);
					I2.setContainer((Container)I);
					contents.add(I2);
				}
			}
			{
				final List<Ability> V= findAffects(I,piece,defined,null);
				for(int i=0;i<V.size();i++)
				{
					final Ability A=V.get(i);
					A.setSavable(true);
					I.addNonUninvokableEffect(A);
				}
			}
			final List<Behavior> V = findBehaviors(I,piece,defined);
			for(int i=0;i<V.size();i++)
			{
				final Behavior B=V.get(i);
				B.setSavable(true);
				I.addBehavior(B);
			}
			I.recoverPhyStats();
			I.text();
			I.setMiscText(I.text());
			I.recoverPhyStats();
			I.phyStats().setSensesMask(I.phyStats().sensesMask()|senseFlag);
		}
		return contents;
	}

	protected List<Ability> findAffects(final Modifiable E, final XMLTag piece, final Map<String,Object> defined, final BuildCallback callBack) throws CMException
	{
		return findAbilities(E,"AFFECT",piece,defined,callBack);
	}

	protected List<Ability> findAbilities(final Modifiable E, final XMLTag piece, final Map<String,Object> defined, final BuildCallback callBack) throws CMException
	{
		return findAbilities(E,"ABILITY",piece,defined,callBack);
	}

	protected List<Ability> findAbilities(final Modifiable E, final String tagName, final XMLTag piece, final Map<String,Object> defined, final BuildCallback callBack) throws CMException
	{
		try
		{
			final List<Ability> V = new Vector<Ability>();
			final List<XMLLibrary.XMLTag> choices = getAllChoices(E,null,null,tagName, piece, defined,true);
			if((choices==null)||(choices.size()==0))
				return V;
			for(int c=0;c<choices.size();c++)
			{
				final XMLTag valPiece = choices.get(c);
				if(valPiece.parms().containsKey("VALIDATE")
				&& !testCondition(E,null,null,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
					continue;
				defineReward(E,null,null,valPiece.getParmValue("DEFINE"),valPiece,null,defined,true);
				final Set<String> definedSet=getPrevouslyDefined(defined,tagName+"_");
				final Ability A=buildAbility(E,valPiece,defined);
				if(callBack != null)
					callBack.willBuild(A, valPiece);
				V.add(A);
				clearNewlyDefined(defined, definedSet, tagName+"_");
			}
			return V;
		}
		catch(final PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
	}

	protected List<Behavior> findBehaviors(final Modifiable E,final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		try
		{
			final List<Behavior> V = new Vector<Behavior>();
			final String tagName="BEHAVIOR";
			final List<XMLLibrary.XMLTag> choices = getAllChoices(E,null,null,tagName, piece, defined,true);
			if((choices==null)||(choices.size()==0))
				return V;
			for(int c=0;c<choices.size();c++)
			{
				final XMLTag valPiece = choices.get(c);
				if(valPiece.parms().containsKey("VALIDATE") && !testCondition(E,null,null,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
					continue;
				defineReward(E,null,null,valPiece.getParmValue("DEFINE"),valPiece,null,defined,true);
				final Set<String> definedSet=getPrevouslyDefined(defined,tagName+"_");
				final Behavior B=buildBehavior(E,valPiece,defined);
				V.add(B);
				clearNewlyDefined(defined, definedSet, tagName+"_");
			}
			return V;
		}
		catch(final PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
	}

	protected List<Race> findRaces(final Modifiable E, final XMLTag piece, final Map<String,Object> defined) throws CMException, PostProcessException
	{
		final List<Race> V = new Vector<Race>();
		final String tagName="RACE";
		final List<XMLLibrary.XMLTag> choices = getAllChoices(E,null,null,tagName, piece, defined,true);
		if((choices==null)||(choices.size()==0))
			return V;
		for(int c=0;c<choices.size();c++)
		{
			final XMLTag valPiece = choices.get(c);
			if(valPiece.parms().containsKey("VALIDATE") && !testCondition(E,null,null,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
				continue;
			defineReward(E,null,null,valPiece.getParmValue("DEFINE"),valPiece,null,defined,true);
			final Set<String> definedSet=getPrevouslyDefined(defined,tagName+"_");
			final Race R=buildGenRace(E,valPiece,defined);
			V.add(R);
			clearNewlyDefined(defined, definedSet, tagName+"_");
		}
		return V;
	}

	protected Ability buildAbility(final Modifiable E, final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		final String classID = findStringNow("class",piece,defined);
		Ability A=CMClass.getAbility(classID);
		if(A == null)
			A=CMClass.findAbility(classID);
		if(A == null)
			throw new CMException("Unable to build ability on classID '"+classID+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
		final Ability aA=A;
		PostProcessAttempter(defined,new PostProcessAttempt()
		{
			@Override
			public String attempt() throws CMException, PostProcessException
			{
				final String value = findOptionalString(E,null,null,"PARMS",piece,this.defined, false);
				if(value != null)
					aA.setMiscText(value);
				return value;
			}
		});
		return A;
	}

	protected Behavior buildBehavior(final Modifiable E, final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		final String classID = findStringNow("class",piece,defined);
		Behavior B=CMClass.getBehavior(classID);
		if(B == null)
			B=CMClass.findBehavior(classID);
		if(B == null)
			throw new CMException("Unable to build behavior on classID '"+classID+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
		final Behavior bB=B;
		PostProcessAttempter(defined,new PostProcessAttempt()
		{
			@Override
			public String attempt() throws CMException, PostProcessException
			{
				final String value = findOptionalString(E,null,null,"PARMS",piece,this.defined, false);
				if(value != null)
					bB.setParms(value);
				return value;
			}
		});
		return B;
	}

	protected List<AbilityMapping> findRaceAbles(final Modifiable E, final String tagName, final String prefix, final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		try
		{
			final List<AbilityMapping> V = new Vector<AbilityMapping>();
			final List<XMLLibrary.XMLTag> choices = getAllChoices(E,null,prefix,tagName, piece, defined,true);
			if((choices==null)||(choices.size()==0))
				return V;
			for(int c=0;c<choices.size();c++)
			{
				final XMLTag valPiece = choices.get(c);
				if(valPiece.parms().containsKey("VALIDATE") && !testCondition(E,null,prefix,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
					continue;
				defineReward(E,null,prefix,valPiece.getParmValue("DEFINE"),valPiece,null,defined,true);
				final Set<String> definedSet=getPrevouslyDefined(defined,tagName+"_");
				final String classID = findStringNow("CLASS",valPiece,defined);
				Ability A=CMClass.getAbility(classID);
				if(A == null)
					A=CMClass.findAbility(classID);
				if(A == null)
					throw new CMException("Unable to build ability on classID '"+classID+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
				defined.put(prefix+"CLASS", classID);
				final AbilityMapping mapA=CMLib.ableMapper().newAbilityMapping().ID(classID);
				String value;
				value=findOptionalStringNow(E, null, prefix, "PARMS", valPiece, defined, false);
				if(value != null)
				{
					mapA.defaultParm(value);
					defined.put(prefix+"PARMS", value);
				}
				value=findOptionalStringNow(E, null, prefix, "PROFF", valPiece, defined, false);
				if(value != null)
				{
					mapA.defaultProficiency(CMath.parseIntExpression(value));
					defined.put(prefix+"PROFF", Integer.valueOf(mapA.defaultProficiency()));
				}
				value=findOptionalStringNow(E, null, prefix, "LEVEL", valPiece, defined, false);
				if(value != null)
				{
					mapA.qualLevel(CMath.parseIntExpression(value));
					defined.put(prefix+"LEVEL", Integer.valueOf(mapA.qualLevel()));
				}
				value=findOptionalStringNow(E, null, prefix, "QUALIFY", valPiece, defined, false);
				if(value != null)
				{
					mapA.autoGain(!CMath.s_bool(value));
					defined.put(prefix+"QUALIFY", value);
				}
				V.add(mapA);
				clearNewlyDefined(defined, definedSet, tagName+"_");
			}
			return V;
		}
		catch(final PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
	}

	protected List<Item> getRaceItems(final Modifiable E, final String tagName, final String prefix, final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		try
		{
			final List<Item> V = new Vector<Item>();
			final List<XMLLibrary.XMLTag> choices = getAllChoices(E,null,prefix,tagName, piece, defined,true);
			if((choices==null)||(choices.size()==0))
				return V;
			for(int c=0;c<choices.size();c++)
			{
				final XMLTag valPiece = choices.get(c);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
					Log.debugOut("MUDPercolator","Found Race Item: "+valPiece.value());
				V.addAll(findItems(valPiece,defined));
			}
			return V;
		}
		catch(final PostProcessException pe)
		{
			throw new CMException("Unable to post process this object type: "+pe.getMessage(),pe);
		}
	}

	protected Race buildGenRace(final Modifiable E, final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		final String classID = findStringNow("class",piece,defined);
		Race R=CMClass.getRace(classID);
		if( R != null)
			return R;
		R=(Race)CMClass.getRace("GenRace").copyOf();
		int numStatsFound=0;
		for(final String stat : R.getStatCodes())
		{
			try
			{
				if(findOptionalString(null, null, null, stat, piece, defined, false)!=null)
					numStatsFound++;
			}
			catch(final PostProcessException pe)
			{
				numStatsFound++;
			}
		}
		if(numStatsFound<5)
			throw new CMException("Too few fields to build race on classID '"+classID+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
		R.setRacialParms("<RACE><ID>"+CMStrings.capitalizeAndLower(classID)+"</ID><NAME>"+CMStrings.capitalizeAndLower(classID)+"</NAME></RACE>");
		CMClass.addRace(R);
		addDefinition("RACE_CLASS",R.ID(),defined); // define so we can mess with it
		R.setStat("NAME", findStringNow("name",piece,defined));
		addDefinition("RACE_NAME",R.name(),defined); // define so we can mess with it
		final List<String> ignoreStats=new XArrayList<String>(new String[]{"CLASS","NAME"});

		final List<Item> raceWeapons=getRaceItems(E,"WEAPON","RACE_WEAPON_",piece,defined);
		if(raceWeapons.size()>0)
		{
			final Item I=raceWeapons.get(CMLib.dice().roll(1, raceWeapons.size(), -1));
			R.setStat("WEAPONCLASS", I.ID());
			R.setStat("WEAPONXML", I.text());
		}
		ignoreStats.addAll(Arrays.asList(new String[]{"WEAPONCLASS","WEAPONXML"}));

		final List<Item> raceResources=getRaceItems(E,"RESOURCES","RACE_RESOURCE_",piece,defined);
		R.setStat("NUMRSC", ""+raceResources.size());
		for(int i=0;i<raceResources.size();i++)
		{
			final Item I=raceResources.get(i);
			R.setStat("GETRSCID"+i, I.ID());
			R.setStat("GETRSCPARM"+i, ""+I.text());
		}
		ignoreStats.addAll(Arrays.asList(new String[]{"NUMRSC","GETRSCID","GETRSCPARM"}));
		final List<Item> raceOutfit=getRaceItems(E,"OUTFIT","RACE_RESOURCE_",piece,defined);
		R.setStat("NUMOFT", ""+raceOutfit.size());
		for(int i=0;i<raceOutfit.size();i++)
		{
			final Item I=raceOutfit.get(i);
			R.setStat("GETOFTID"+i, I.ID());
			R.setStat("GETOFTPARM"+i, ""+I.text());
		}
		ignoreStats.addAll(Arrays.asList(new String[]{"NUMOFT","GETOFTID","GETOFTPARM"}));
		final List<AbilityMapping> rables = findRaceAbles(E,"ABILITY","RACE_ABLE_",piece,defined);
		R.setStat("NUMRABLE", ""+rables.size());
		for(int i=0;i<rables.size();i++)
		{
			final AbilityMapping ableMap=rables.get(i);
			R.setStat("GETRABLE"+i, ableMap.abilityID());
			R.setStat("GETRABLEPROF"+i, ""+ableMap.defaultProficiency());
			R.setStat("GETRABLEQUAL"+i, ""+(!ableMap.autoGain()));
			R.setStat("GETRABLELVL"+i, ""+ableMap.qualLevel());
			R.setStat("GETRABLEPARM"+i, ableMap.defaultParm());
		}
		ignoreStats.addAll(Arrays.asList(new String[]{"NUMRABLE","GETRABLE","GETRABLEPROF","GETRABLEQUAL","GETRABLELVL","GETRABLEPARM"}));
		final List<AbilityMapping> cables = findRaceAbles(E,"CULTUREABILITY","RACE_CULT_ABLE_",piece,defined);
		R.setStat("NUMCABLE", ""+cables.size());
		for(int i=0;i<cables.size();i++)
		{
			final AbilityMapping ableMap=cables.get(i);
			R.setStat("GETCABLE"+i, ableMap.abilityID());
			R.setStat("GETCABLEPROF"+i, ""+ableMap.defaultProficiency());
		}
		ignoreStats.addAll(Arrays.asList(new String[]{"NUMCABLE","GETCABLE","GETCABLEPROF"}));
		final List<AbilityMapping> reffs = findRaceAbles(E,"AFFECT","RACE_EFFECT_",piece,defined);
		R.setStat("NUMREFF", ""+reffs.size());
		for(int i=0;i<reffs.size();i++)
		{
			final AbilityMapping ableMap=reffs.get(i);
			R.setStat("GETREFF"+i, ableMap.abilityID());
			R.setStat("GETREFFPARM"+i, ""+ableMap.defaultParm());
			R.setStat("GETREFFLVL"+i, ""+ableMap.qualLevel());
		}
		ignoreStats.addAll(Arrays.asList(new String[]{"NUMREFF","GETREFF","GETREFFPARM","GETREFFLVL"}));

		final List<AbilityMapping> iables = findRaceAbles(E,"IMMUNITY","RACE_IMMUNITY_",piece,defined);
		R.setStat("NUMIABLE", ""+reffs.size());
		for(int i=0;i<iables.size();i++)
		{
			final AbilityMapping ableMap=reffs.get(i);
			R.setStat("GETIABLE"+i, ableMap.abilityID());
		}
		ignoreStats.addAll(Arrays.asList(new String[]{"NUMIABLE","GETIABLE"}));

		fillOutStatCodes(R,ignoreStats,"RACE_",piece,defined);
		CMLib.database().DBCreateRace(R.ID(),R.racialParms());
		return R;
	}

	protected void addDefinition(String definition, final String value, final Map<String,Object> defined)
	{
		definition=definition.toUpperCase().trim();
		defined.put(definition, value);
		if(definition.toUpperCase().endsWith("S"))
			definition+="ES";
		else
			definition+="S";
		final String def = (String)defined.get(definition);
		if(def==null)
			defined.put(definition, value);
		else defined.put(definition, def+","+value);
	}

	protected String findOptionalStringNow(final Modifiable E, final List<String> ignoreStats, final String defPrefix, final String tagName, final XMLTag piece, final Map<String,Object> defined, final boolean debug)
	{
		try
		{
			return findOptionalString(E,ignoreStats,defPrefix,tagName, piece, defined, false);
		}
		catch(final PostProcessException x)
		{
			if(debug)
				Log.errOut(x);
			return null;
		}
	}

	protected String findOptionalString(final Modifiable E, final List<String> ignoreStats, final String defPrefix, final String tagName, final XMLTag piece, final Map<String,Object> defined, final boolean debug) throws PostProcessException
	{
		try
		{
			return findString(E,ignoreStats,defPrefix,tagName, piece, defined);
		}
		catch(final CMException x)
		{
			if(debug)
				Log.errOut(x);
			return null;
		}
	}

	@Override
	public void defineReward(final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		defineReward(null, null, null,piece,piece.value(),defined);
	}

	protected void defineReward(final Modifiable E, final List<String> ignoreStats, final String defPrefix, final XMLTag piece, final String value, final Map<String,Object> defined) throws CMException
	{
		try
		{
			defineReward(E,ignoreStats,defPrefix,piece.getParmValue("DEFINE"),piece,value,defined,true);
		}
		catch(final PostProcessException pe)
		{
			throw new CMException("Post-processing not permitted: "+pe.getMessage(),pe);
		}
	}

	@Override
	public void preDefineReward(final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		preDefineReward(null,null,null,piece,defined);
	}

	protected void preDefineReward(final Modifiable E, final List<String> ignoreStats, final String defPrefix, final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		try
		{
			defineReward(E,ignoreStats,defPrefix,piece.getParmValue("PREDEFINE"),piece,piece.value(),defined,false);
		}
		catch(final PostProcessException pe)
		{
			throw new CMException("Post-processing not permitted: "+pe.getMessage(),pe);
		}
	}

	protected void defineReward(final Modifiable E, final List<String> ignoreStats, final String defPrefix, final String defineString, final XMLTag piece, final Object value, final Map<String,Object> defined, final boolean recurseAllowed) throws CMException, PostProcessException
	{
		if((defineString!=null)&&(defineString.trim().length()>0))
		{
			final List<String> V=CMParms.parseCommas(defineString,true);
			for (String defVar : V)
			{
				Object definition=value;
				final int x=defVar.indexOf('=');
				if(x==0)
					continue;
				if(x>0)
				{
					definition=defVar.substring(x+1).trim();
					defVar=defVar.substring(0,x).toUpperCase().trim();
					switch(defVar.charAt(defVar.length()-1))
					{
					case '+':
					case '-':
					case '*':
					case '/':
						{
							final char plusMinus=defVar.charAt(defVar.length()-1);
							defVar=defVar.substring(0,defVar.length()-1).trim();
							String oldVal=(String)defined.get(defVar.toUpperCase().trim());
							if((oldVal==null)||(oldVal.trim().length()==0))
								oldVal="0";
							definition=oldVal+plusMinus+definition;
							break;
						}
					}
				}
				if(definition==null)
					definition="!";
				if(definition instanceof String)
				{
					definition=strFilter(E,ignoreStats,defPrefix,(String)definition,piece, defined);
					if(CMath.isMathExpression((String)definition))
						definition=Integer.toString(CMath.s_parseIntExpression((String)definition));
				}
				if(defVar.trim().length()>0)
					defined.put(defVar.toUpperCase().trim(), definition);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
					Log.debugOut("MudPercolator","DEFINE:"+defVar.toUpperCase().trim()+"="+definition);
			}
		}
		final XMLTag parentPiece = piece.parent();
		if((parentPiece!=null)&&(parentPiece.tag().equalsIgnoreCase(piece.tag()))&&(recurseAllowed))
			defineReward(E,ignoreStats,defPrefix,parentPiece.getParmValue("DEFINE"),parentPiece,value,defined,recurseAllowed);
	}

	protected String findStringNow(final Modifiable E, final List<String> ignoreStats, final String defPrefix, final String tagName, final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		try
		{
			return findString(E,ignoreStats,defPrefix,tagName,piece,defined);
		}
		catch(final PostProcessException pe)
		{
			throw new CMException("Post processing not permitted",pe);
		}
	}

	protected String replaceLineStartsWithIgnoreCase(final String wholeText, final String lineStarter, final String fullNewLine)
	{
		final int x=wholeText.toLowerCase().indexOf(lineStarter.toLowerCase());
		if(x>0)
		{
			int y=wholeText.indexOf('\n',x+1);
			final int z=wholeText.indexOf('\r',x+1);
			if((y<x)||((z>x)&&(z<y)))
				y=z;
			if(y>x)
				return wholeText.substring(0,x)+fullNewLine+wholeText.substring(y);
		}
		return wholeText;
	}

	protected String buildQuestTemplate(final Modifiable E, final List<String> ignoreStats, final String defPrefix, final String tagName, final XMLTag valPiece, final Map<String,Object> defined) throws CMException,PostProcessException
	{
		String value=null;
		String questTemplateLoad = valPiece.tag().equals("QUEST")?valPiece.getParmValue("QUEST_TEMPLATE_ID"):null;
		if((questTemplateLoad!=null)
		&&(questTemplateLoad.length()>0))
		{
			//valPiece.parms().remove("QUEST_TEMPLATE_ID"); // once only, please
			questTemplateLoad = strFilter(E,ignoreStats,defPrefix,questTemplateLoad,valPiece, defined);
			CMFile file = new CMFile(Resources.makeFileResourceName(questTemplateLoad),null);
			if(!file.exists() || !file.canRead())
				file = new CMFile(Resources.makeFileResourceName("quests/templates/"+questTemplateLoad.trim()+".quest"),null,CMFile.FLAG_LOGERRORS|CMFile.FLAG_FORCEALLOW);
			if(file.exists() && file.canRead())
			{
				final String rawFileText = file.text().toString();
				final int endX=rawFileText.lastIndexOf("#!QUESTMAKER_END_SCRIPT");
				if(endX > 0)
				{
					final int lastCR = rawFileText.indexOf('\n', endX);
					final int lastEOF = rawFileText.indexOf('\r', endX);
					final int endScript = lastCR > endX ? (lastCR < lastEOF ? lastCR : lastEOF): lastEOF;
					final List<String> wizList = Resources.getFileLineVector(new StringBuffer(rawFileText.substring(0, endScript).trim()));
					String cleanedFileText = rawFileText.substring(endScript).trim();
					cleanedFileText = CMStrings.replaceAll(cleanedFileText, "$#AUTHOR", "CoffeeMud");
					final String duration=this.findOptionalString(E, ignoreStats, defPrefix, "DURATION", valPiece, defined, false);
					if((duration != null) && (duration.trim().length()>0))
						cleanedFileText = this.replaceLineStartsWithIgnoreCase(cleanedFileText, "set duration", "SET DURATION "+duration);
					final String expiration=this.findOptionalString(E, ignoreStats, defPrefix, "EXPIRATION", valPiece, defined, false);
					if((expiration != null)  && (expiration.trim().length()>0))
						cleanedFileText = this.replaceLineStartsWithIgnoreCase(cleanedFileText, "set duration", "SET EXPIRATION "+expiration);
					for(final String wiz : wizList)
					{
						if(wiz.startsWith("#$"))
						{
							final int x=wiz.indexOf('=');
							if(x>0)
							{
								final String var=wiz.substring(1,x);
								if(cleanedFileText.indexOf(var)>0)
								{
									final String findVar=wiz.substring(2,x);
									final String val=this.findStringNow(E, ignoreStats, defPrefix, findVar, valPiece, defined);
									if(val == null)
										throw new CMException("Unable to generate quest.  Required variable $"+findVar+" not found.");
									cleanedFileText=CMStrings.replaceAll(cleanedFileText,var,CMStrings.replaceAll(val, "$$", "$"));
								}
							}
						}
					}
					value=cleanedFileText;
				}
				else
					throw new CMException("Corrupt quest_template in '"+tagName+"' on piece '"+valPiece.tag()+"', Data: "+CMParms.toKeyValueSlashListString(valPiece.parms())+":"+CMStrings.limit(valPiece.value(),100));
			}
			else
				throw new CMException("Bad quest_template_id in '"+tagName+"' on piece '"+valPiece.tag()+"', Data: "+CMParms.toKeyValueSlashListString(valPiece.parms())+":"+CMStrings.limit(valPiece.value(),100));
		}
		return value;
	}

	protected String findString(final Modifiable E, final List<String> ignoreStats, final String defPrefix, String tagName, XMLTag piece, final Map<String,Object> defined) throws CMException,PostProcessException
	{
		tagName=tagName.toUpperCase().trim();

		if(tagName.startsWith("SYSTEM_RANDOM_NAME:"))
		{
			final String[] split=tagName.substring(19).split("-");
			if((split.length==2)&&(CMath.isInteger(split[0]))&&(CMath.isInteger(split[1])))
				return CMLib.login().generateRandomName(CMath.s_int(split[0]), CMath.s_int(split[1]));
			throw new CMException("Bad random name range in '"+tagName+"' on piece '"+piece.tag()+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
		}
		else
		if(tagName.equals("ROOM_AREAGATE"))
		{
			if(E instanceof Environmental)
			{
				final Room R=CMLib.map().roomLocation((Environmental)E);
				if(R!=null)
				{
					boolean foundOne=false;
					for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
					{
						final Room R2=R.getRoomInDir(d);
						foundOne=(R2!=null) || foundOne;
						if((R2!=null) && (R2.roomID().length()>0) && (R.getArea()!=R2.getArea()))
							return CMLib.directions().getDirectionName(d);
					}
					if(!foundOne)
						throw new PostProcessException("No exits at all on on object "+R.roomID()+" in variable '"+tagName+"'");
				}
			}
			return "";
		}
		if(defPrefix != null)
		{
			final Object asPreviouslyDefined = defined.get((defPrefix+tagName).toUpperCase());
			if(asPreviouslyDefined instanceof String)
				return strFilter(E,ignoreStats,defPrefix,(String)asPreviouslyDefined,piece, defined);
		}

		final String asParm = piece.getParmValue(tagName);
		if(asParm != null)
			return strFilter(E,ignoreStats,defPrefix,asParm,piece, defined);

		final Object asDefined = defined.get(tagName);
		if(asDefined instanceof String)
			return (String)asDefined;

		final String contentload = piece.getParmValue("CONTENT_LOAD");
		if((contentload!=null)
		&&(contentload.length()>0))
		{
			final CMFile file = new CMFile(contentload,null,CMFile.FLAG_LOGERRORS|CMFile.FLAG_FORCEALLOW);
			if(file.exists() && file.canRead())
				return strFilter(E, ignoreStats, defPrefix,file.text().toString(), piece, defined);
			else
				throw new CMException("Bad content_load filename in '"+tagName+"' on piece '"+piece.tag()+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
		}

		XMLTag processDefined=null;
		if(asDefined instanceof XMLTag)
		{
			piece=(XMLTag)asDefined;
			processDefined=piece;
			tagName=piece.tag();
		}
		final List<XMLLibrary.XMLTag> choices = getAllChoices(E,ignoreStats,defPrefix,tagName, piece, defined,true);
		if((choices==null)||(choices.size()==0))
			throw new CMDataException("Unable to find tag '"+tagName+"' on piece '"+piece.tag()+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
		StringBuffer finalValue = new StringBuffer("");

		for(int c=0;c<choices.size();c++)
		{
			final XMLTag valPiece = choices.get(c);
			if(valPiece.parms().containsKey("VALIDATE") && !testCondition(E,null,null,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
				continue;

			String value=this.buildQuestTemplate(E, ignoreStats, defPrefix, tagName, valPiece, defined);
			if(value == null)
			{
				try
				{
					value=strFilter(E,ignoreStats,defPrefix,valPiece.value(),valPiece, defined);
				}
				catch(final java.lang.StackOverflowError e)
				{
					final String id=(piece.getParmValue("ID")!=null)?piece.getParmValue("ID"):"null";
					Log.errOut("Stack overflow trying to filter "+valPiece.value()+" on "+piece.tag()+" id '"+id+"'");
					throw new CMException("Ended because of a stack overflow.  See the log.");
				}
			}

			if(processDefined!=valPiece)
				defineReward(E,ignoreStats,defPrefix,valPiece.getParmValue("DEFINE"),valPiece,value,defined,true);

			String action = valPiece.getParmValue("ACTION");
			if(action==null)
				finalValue.append(" ").append(value);
			else
			{
				action=action.toUpperCase().trim();
				if((action.length()==0)||(action.equals("APPEND")))
					finalValue.append(" ").append(value);
				else
				if(action.equals("REPLACE"))
					finalValue = new StringBuffer(value);
				else
				if(action.equals("PREPEND"))
					finalValue.insert(0,' ').insert(0,value);
				else
					throw new CMException("Unknown action '"+action+" on subPiece "+valPiece.tag()+" on piece '"+piece.tag()+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			}
		}
		final String finalFinalValue=finalValue.toString().trim();
		if(processDefined!=null)
			defineReward(E,ignoreStats,defPrefix,piece.getParmValue("DEFINE"),processDefined,finalFinalValue,defined,true);
		return finalFinalValue;
	}

	@Override
	public String buildQuestScript(final XMLTag piece, final Map<String,Object> defined, final Modifiable E) throws CMException
	{
		final List<String> ignore=new ArrayList<String>();
		try
		{
			return CMStrings.replaceAll(this.findString(E, ignore, null, "QUEST", piece, defined),"%0D","\n\r");
		}
		catch(final PostProcessException pe)
		{
			throw new CMException("Post processing not permitted",pe);
		}
	}

	protected Object findObject(final Modifiable E, final List<String> ignoreStats, final String defPrefix, String tagName, XMLTag piece, final Map<String,Object> defined) throws CMException,PostProcessException
	{
		tagName=tagName.toUpperCase().trim();

		if(defPrefix != null)
		{
			final Object asPreviouslyDefined = defined.get((defPrefix+tagName).toUpperCase());
			if(asPreviouslyDefined instanceof String)
				return strFilter(E,ignoreStats,defPrefix,(String)asPreviouslyDefined,piece, defined);
			if(!(asPreviouslyDefined instanceof XMLTag))
				return asPreviouslyDefined;
		}

		final String asParm = piece.getParmValue(tagName);
		if(asParm != null)
			return strFilter(E,ignoreStats,defPrefix,asParm,piece, defined);

		final Object asDefined = defined.get(tagName);
		if((!(asDefined instanceof XMLTag))
		&&(!(asDefined instanceof String))
		&&(asDefined != null))
			return asDefined;

		XMLTag processDefined=null;
		if(asDefined instanceof XMLTag)
		{
			piece=(XMLTag)asDefined;
			processDefined=piece;
			tagName=piece.tag();
		}
		if(asDefined == null)
			processDefined=piece;
		final List<XMLLibrary.XMLTag> choices =new ArrayList<XMLLibrary.XMLTag>();
		final Set<String> done=new HashSet<String>();
		for(final XMLLibrary.XMLTag subTag : piece.contents())
		{
			if(done.contains(subTag.tag()))
				continue;
			done.add(subTag.tag());
			choices.addAll(getAllChoices(E,ignoreStats,defPrefix,subTag.tag(), piece, defined,true));
		}
		if(choices.size()==0)
			choices.addAll(getAllChoices(E,ignoreStats,defPrefix,tagName, piece, defined,true));
		if(choices.size()==0)
			throw new CMDataException("Unable to find tag '"+tagName+"' on piece '"+piece.tag()+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
		final List<Object> finalValues = new ArrayList<Object>();

		for(int c=0;c<choices.size();c++)
		{
			final XMLTag valPiece = choices.get(c);
			if(valPiece.parms().containsKey("VALIDATE") && !testCondition(E,null,null,CMLib.xml().restoreAngleBrackets(valPiece.getParmValue("VALIDATE")),valPiece, defined))
				continue;

			final String valueStr=valPiece.value().toUpperCase().trim();
			final Object value;
			if(valueStr.startsWith("SELECT:"))
			{
				final List<Map<String,Object>> sel=this.doMQLSelectObjs(E, ignoreStats, defPrefix, CMLib.xml().restoreAngleBrackets(valueStr), valPiece, defined);
				value=sel;
				finalValues.addAll(sel);
			}
			else
			if(valPiece.tag().equals("MOB"))
			{
				final List<MOB> objs = this.findMobs(E, valPiece, defined, null);
				for(final MOB M : objs)
				{
					CMLib.threads().deleteAllTicks(M);
					M.setSavable(false);
					M.setDatabaseID("DELETE");
				}
				value=objs;
				finalValues.addAll(objs);
			}
			else
			if(valPiece.tag().equals("ITEM"))
			{
				final List<Item> objs = this.findItems(E, valPiece, defined, null);
				for(final Item I : objs)
				{
					CMLib.threads().deleteAllTicks(I);
					I.setSavable(false);
					I.setDatabaseID("DELETE");
				}
				value=objs;
				finalValues.addAll(objs);
			}
			else
			if(valPiece.tag().equals("ABILITY"))
			{
				final List<Ability> objs = this.findAbilities(E, valPiece, defined, null);
				value=objs;
				finalValues.addAll(objs);
			}
			else
			{
				try
				{
					final List<Object> objs = parseMQLFrom(new String[] {valueStr}, valueStr, E, ignoreStats, defPrefix, valPiece, defined, false);
					value=objs;
					finalValues.addAll(objs);
				}
				catch(final CMException e)
				{
					throw new CMException("Unable to produce '"+tagName+"' on piece '"+piece.tag()+"', Data: "+CMStrings.limit(piece.value(),100)+":"+e.getMessage());
				}
			}
			if(processDefined!=valPiece)
				defineReward(E,ignoreStats,defPrefix,valPiece.getParmValue("DEFINE"),valPiece,value,defined,true);
		}
		if(processDefined!=null)
			defineReward(E,ignoreStats,defPrefix,piece.getParmValue("DEFINE"),processDefined,finalValues.size()==1?finalValues.get(0):finalValues,defined,true);
		return finalValues.size()==1?finalValues.get(0):finalValues;
	}

	protected XMLTag processLikeParm(final String tagName, XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		final String like = piece.getParmValue("LIKE");
		if(like!=null)
		{
			final List<String> V=CMParms.parseCommas(like,true);
			final XMLTag origPiece = piece;
			piece=piece.copyOf();
			for(int v=0;v<V.size();v++)
			{
				String s = V.get(v);
				if(s.startsWith("$"))
					s=s.substring(1).trim();
				final XMLTag likePiece =(XMLTag)defined.get(s.toUpperCase().trim());
				if((likePiece == null)||(!likePiece.tag().equalsIgnoreCase(tagName)))
					throw new CMException("Invalid like: '"+s+"' on piece '"+piece.tag()+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
				piece.contents().addAll(likePiece.contents());
				piece.parms().putAll(likePiece.parms());
				piece.parms().putAll(origPiece.parms());
			}
		}
		return piece;
	}

	@Override
	public List<XMLTag> getAllChoices(final String tagName, final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		try
		{
			return getAllChoices(null,null,null,tagName,piece,defined,true);
		}
		catch(final PostProcessException pe)
		{
			throw new CMException("Unable to post process this object: "+pe.getMessage(),pe);
		}
	}

	protected List<XMLTag> getAllChoices(final Modifiable E, final List<String> ignoreStats, final String defPrefix, final String tagName, final XMLTag piece, final Map<String,Object> defined, final boolean skipTest) throws CMException, PostProcessException
	{
		if((!skipTest)
		&&(!testCondition(E,ignoreStats,defPrefix,CMLib.xml().restoreAngleBrackets(piece.getParmValue("CONDITION")),piece,defined)))
			return new Vector<XMLTag>(1);

		defineReward(E,ignoreStats,defPrefix,piece.getParmValue("PREDEFINE"),piece,piece.value(),defined,false); // does pre-define

		final List<XMLTag> choices = new Vector<XMLTag>();
		final String inserter = piece.getParmValue("INSERT");
		if(inserter != null)
		{
			final String upperInserter = inserter.toUpperCase().trim();
			if(upperInserter.startsWith("SELECT:"))
			{
				final List<Map<String,Object>> objs=this.doMQLSelectObjs(E, ignoreStats, defPrefix, CMLib.xml().restoreAngleBrackets(upperInserter), piece, defined);
				for(final Map<String,Object> m : objs)
				{
					for(final String key : m.keySet())
					{
						if(key.equalsIgnoreCase(tagName))
							choices.add(CMLib.xml().createNewTag(tagName, this.convertMQLObjectToString(m.get(key))));
					}
				}
			}
			else
			{
				final List<String> V=CMParms.parseCommas(upperInserter,true);
				for(int v=0;v<V.size();v++)
				{
					String s = V.get(v);
					if(s.startsWith("$"))
						s=s.substring(1).trim();
					final XMLTag insertPiece =(XMLTag)defined.get(s.trim());
					if(insertPiece == null)
						throw new CMException("Undefined insert: '"+s+"' on piece '"+piece.tag()+"', Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
					if(insertPiece.tag().equalsIgnoreCase(tagName))
						choices.addAll(getAllChoices(E,ignoreStats,defPrefix,tagName,insertPiece,defined,false));
				}
			}
		}
		else
		if(piece.tag().equalsIgnoreCase(tagName))
		{
			if((piece.parms().containsKey("LAYOUT")) && (piece.tag().equalsIgnoreCase("ROOM")) && (!defined.containsKey("ROOM_LAYOUT")))
				return new XVector<XMLTag>(processLikeParm(tagName,piece,defined));

			boolean container=false;
			for(int p=0;p<piece.contents().size();p++)
			{
				final XMLTag ppiece=piece.contents().get(p);
				if(ppiece.tag().equalsIgnoreCase(tagName))
				{
					container=true;
					break;
				}
			}
			if(!container)
				return new XVector<XMLTag>(processLikeParm(tagName,piece,defined));
		}

		for(int p=0;p<piece.contents().size();p++)
		{
			final XMLTag subPiece = piece.contents().get(p);
			if(subPiece.tag().equalsIgnoreCase(tagName))
				choices.addAll(getAllChoices(E,ignoreStats,defPrefix,tagName,subPiece,defined,false));
		}
		final List<XMLTag> finalChoices = selectChoices(E,tagName,ignoreStats,defPrefix,choices,piece,defined);
		if(piece.getParmValue("TAGDEFINE")!=null)
		{
			final XMLTag copyTag=piece.copyOf();
			copyTag.parms().remove("INSERT");
			copyTag.parms().remove("TAGDEFINE");
			copyTag.parms().remove("PREDEFINE");
			if(copyTag.parms().containsKey("SELECT"))
				copyTag.parms().put("SELECT", "ALL");
			copyTag.contents().clear();
			copyTag.contents().addAll(finalChoices);
			defineReward(E,ignoreStats,defPrefix,piece.getParmValue("TAGDEFINE"),piece,copyTag,defined,false); // does contents-define
		}
		return finalChoices;
	}

	protected boolean testCondition(final Modifiable E, final List<String> ignoreStats, final String defPrefix, final String condition, final XMLTag piece, final Map<String,Object> defined) throws PostProcessException
	{
		final Map<String,Object> fixed=new HashMap<String,Object>();
		try
		{
			if(condition == null)
				return true;
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
					Log.debugOut("MudPercolator","START-TEST "+piece.tag()+": "+condition);
			final List<Varidentifier> ids=parseVariables(condition);
			for(final Varidentifier id : ids)
			{
				try
				{
					final String upperId=id.var.toUpperCase();
					if(upperId.length()==0)
						continue;
					final boolean missingMobVarCondition;
					if(defPrefix != null)
					{
						missingMobVarCondition = (defined!=null)
						&&(upperId.startsWith(defPrefix))
						&&(!defined.containsKey(upperId));
						if(missingMobVarCondition)
						{
							XMLTag newPiece=piece;
							while((newPiece.parent()!=null)&&(newPiece.tag().equals(piece.tag())))
								newPiece=newPiece.parent();
							fillOutStatCode(E, ignoreStats, defPrefix, id.var.substring(defPrefix.length()), newPiece, defined, false);
						}
					}
					else
						missingMobVarCondition = false;
					String value;
					if(upperId.startsWith("SELECT:"))
					{
						try
						{
							value=doMQLSelectString(E,null,null,CMLib.xml().restoreAngleBrackets(id.var),piece,defined);
						}
						catch(final MQLException e)
						{
							value="";
						}
						return this.testCondition(E, ignoreStats, defPrefix, CMStrings.replaceAll(condition, condition.substring(id.outerStart,id.outerEnd), value), piece, defined);
					}
					else
						value=findString(E,ignoreStats,defPrefix,id.var, piece, defined);
					if(CMath.isMathExpression(value))
					{
						final String origValue = value;
						final double val=CMath.parseMathExpression(value);
						if(Math.round(val)==val)
							value=""+Math.round(val);
						else
							value=""+val;
						if((origValue.indexOf('?')>0) // random levels need to be chosen ONCE, esp when name is involved.
						&&(missingMobVarCondition)
						&&(defined != null))
							defined.put(upperId,value);
					}
					fixed.put(id.var.toUpperCase(),value);
				}
				catch(final CMDataException e)
				{
				}
				catch(final MQLException e)
				{
					Log.errOut("Error processing condition "+condition+": "+e.getMessage());
				}
				catch(final CMException e)
				{
					//Log.errOut("Failure processing condition "+condition+": "+e.getMessage());
				}
			}
			final Map<String,Object> finalDefined = new HashMap<String,Object>();
			finalDefined.putAll(defined);
			finalDefined.putAll(fixed);
			final boolean test= CMStrings.parseStringExpression(condition.toUpperCase(),finalDefined, true);
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
				Log.debugOut("MudPercolator","TEST "+piece.tag()+": "+condition+"="+test);
			return test;
		}
		catch(final Exception e)
		{
			if(e instanceof PostProcessException)
				throw (PostProcessException)e;
			Log.errOut("Generate","Condition procesing failure: "+e.getMessage()+": "+condition);
			try
			{
				final Map<String,Object> finalDefined = new HashMap<String,Object>();
				finalDefined.putAll(defined);
				finalDefined.putAll(fixed);
				if(condition != null)
					CMStrings.parseStringExpression(condition.toUpperCase(),finalDefined, true);
			}
			catch(final Exception e1)
			{
			}
			return false;
		}
	}

	protected String getRequirementsDescription(final String values)
	{
		if(values==null)
			return "";
		if(values.equalsIgnoreCase("integer")||values.equalsIgnoreCase("int"))
			return " as an integer or integer expression";
		else
		if(values.equalsIgnoreCase("double")||values.equalsIgnoreCase("#")||values.equalsIgnoreCase("number"))
			return " as a number or numeric expression";
		else
		if(values.equalsIgnoreCase("string")||values.equalsIgnoreCase("$"))
			return " as an open string";
		else
		if(values.trim().length()>0)
			return " as one of the following values: "+values;
		return "";
	}

	protected boolean checkRequirementsValue(final String validValue, final String value)
	{
		if(validValue==null)
			return value != null;
		if(validValue.equalsIgnoreCase("integer")||validValue.equalsIgnoreCase("int"))
			return CMath.isMathExpression(value);
		else
		if(validValue.equalsIgnoreCase("double")||validValue.equalsIgnoreCase("#")||validValue.equalsIgnoreCase("number"))
			return CMath.isMathExpression(value);
		else
		if(validValue.equalsIgnoreCase("string")||validValue.equalsIgnoreCase("$"))
			return value.length()>0;
		else
		if(validValue.trim().length()>0)
			return CMParms.containsIgnoreCase(CMParms.toStringArray(CMParms.parseSemicolons(validValue,true)),value);
		return value.length()==0;
	}

	protected String cleanRequirementsValue(final String values, final String value)
	{
		if(values==null)
			return value;
		if(values.equalsIgnoreCase("integer")||values.equalsIgnoreCase("int"))
			return Integer.toString(CMath.s_parseIntExpression(value));
		else
		if(values.equalsIgnoreCase("double")||values.equalsIgnoreCase("#")||values.equalsIgnoreCase("number"))
			return Double.toString(CMath.s_parseMathExpression(value));
		else
		if(values.equalsIgnoreCase("string")||values.equalsIgnoreCase("$"))
			return value;
		else
		if(values.trim().length()>0)
		{
			final String[] arrayStr=CMParms.toStringArray(CMParms.parseSemicolons(values,true));
			int x=CMParms.indexOfIgnoreCase(arrayStr,value);
			if(x<0)
				x=0;
			return arrayStr[x];
		}
		return value;
	}

	@Override
	public Map<String,String> getUnfilledRequirements(final Map<String,Object> defined, final XMLTag piece)
	{
		String requirements = piece.getParmValue("REQUIRES");
		final Map<String,String> set=new Hashtable<String,String>();
		if(requirements==null)
			return set;
		requirements = requirements.trim();
		final List<String> reqs = CMParms.parseCommas(requirements,true);
		for(int r=0;r<reqs.size();r++)
		{
			String reqVariable=reqs.get(r);
			if(reqVariable.startsWith("$"))
				reqVariable=reqVariable.substring(1).trim();
			String validValues=null;
			final int x=reqVariable.indexOf('=');
			if(x>=0)
			{
				validValues=reqVariable.substring(x+1).trim();
				reqVariable=reqVariable.substring(0,x).trim();
			}
			if((!defined.containsKey(reqVariable.toUpperCase()))
			||(!checkRequirementsValue(validValues, defined.get(reqVariable.toUpperCase()).toString())))
			{
				if(validValues==null)
					set.put(reqVariable.toUpperCase(), "any");
				else
				if(validValues.equalsIgnoreCase("integer")||validValues.equalsIgnoreCase("int"))
					set.put(reqVariable.toUpperCase(), "int");
				else
				if(validValues.equalsIgnoreCase("double")||validValues.equalsIgnoreCase("#")||validValues.equalsIgnoreCase("number"))
					set.put(reqVariable.toUpperCase(), "double");
				else
				if(validValues.equalsIgnoreCase("string")||validValues.equalsIgnoreCase("$"))
					set.put(reqVariable.toUpperCase(), "string");
				else
				if(validValues.trim().length()>0)
					set.put(reqVariable.toUpperCase(), CMParms.toListString(CMParms.parseSemicolons(validValues,true)));
			}
		}
		return set;
	}

	protected void checkRequirements(final Map<String,Object> defined, String requirements) throws CMException
	{
		if(requirements==null)
			return;
		requirements = requirements.trim();
		final List<String> reqs = CMParms.parseCommas(requirements,true);
		for(int r=0;r<reqs.size();r++)
		{
			String reqVariable=reqs.get(r);
			if(reqVariable.startsWith("$"))
				reqVariable=reqVariable.substring(1).trim();
			String validValues=null;
			final int x=reqVariable.indexOf('=');
			if(x>=0)
			{
				validValues=reqVariable.substring(x+1).trim();
				reqVariable=reqVariable.substring(0,x).trim();
			}
			if(!defined.containsKey(reqVariable.toUpperCase()))
				throw new CMException("Required variable not defined: '"+reqVariable+"'.  Please define this variable"+getRequirementsDescription(validValues)+".");
			if(!checkRequirementsValue(validValues, defined.get(reqVariable.toUpperCase()).toString()))
				throw new CMException("The required variable '"+reqVariable+"' is not properly defined.  Please define this variable"+getRequirementsDescription(validValues)+".");
		}
	}

	@Override
	public void checkRequirements(final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		checkRequirements(defined,piece.getParmValue("REQUIRES"));
	}

	protected List<XMLTag> selectChoices(final Modifiable E, final String tagName, final List<String> ignoreStats, final String defPrefix, final List<XMLTag> choices, final XMLTag piece, final Map<String,Object> defined) throws CMException, PostProcessException
	{
		String selection = piece.getParmValue("SELECT");
		if(selection == null)
			return choices;
		selection=selection.toUpperCase().trim();
		List<XMLLibrary.XMLTag> selectedChoicesV=null;
		if(selection.equals("NONE"))
			selectedChoicesV= new Vector<XMLTag>();
		else
		if(selection.equals("ALL"))
			selectedChoicesV=choices;
		else
		if((choices.size()==0)
		&&(!selection.startsWith("ANY-0"))
		&&(!selection.startsWith("FIRST-0"))
		&&(!selection.startsWith("LAST-0"))
		&&(!selection.startsWith("PICK-0"))
		&&(!selection.startsWith("LIMIT-")))
		{
			throw new CMException("Can't make selection among NONE: on piece '"+piece.tag()+"', Tag: "+tagName+", Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
		}
		else
		if(selection.equals("FIRST"))
			selectedChoicesV= new XVector<XMLTag>(choices.get(0));
		else
		if(selection.startsWith("FIRST-"))
		{
			final int num=CMath.parseIntExpression(strFilterNow(E,ignoreStats,defPrefix,selection.substring(selection.indexOf('-')+1),piece, defined));
			if((num<0)||(num>choices.size()))
				throw new CMException("Can't pick first "+num+" of "+choices.size()+" on piece '"+piece.tag()+"', Tag: "+tagName+", Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			selectedChoicesV=new Vector<XMLTag>();
			for(int v=0;v<num;v++)
				selectedChoicesV.add(choices.get(v));
		}
		else
		if(selection.startsWith("LIMIT-"))
		{
			final int num=CMath.parseIntExpression(strFilterNow(E,ignoreStats,defPrefix,selection.substring(selection.indexOf('-')+1),piece, defined));
			if(num<0)
				throw new CMException("Can't pick limit "+num+" of "+choices.size()+" on piece '"+piece.tag()+"', Tag: "+tagName+", Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			selectedChoicesV=new Vector<XMLTag>();
			if(choices.size()<=num)
				selectedChoicesV.addAll(choices);
			else
			while(selectedChoicesV.size()<num)
				selectedChoicesV.add(choices.remove(CMLib.dice().roll(1, choices.size(), -1)));
		}
		else
		if(selection.equals("LAST"))
			selectedChoicesV=new XVector<XMLTag>(choices.get(choices.size()-1));
		else
		if(selection.startsWith("LAST-"))
		{
			final int num=CMath.parseIntExpression(strFilterNow(E,ignoreStats,defPrefix,selection.substring(selection.indexOf('-')+1),piece, defined));
			if((num<0)||(num>choices.size()))
				throw new CMException("Can't pick last "+num+" of "+choices.size()+" on piece '"+piece.tag()+"', Tag: "+tagName+", Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			selectedChoicesV=new Vector<XMLTag>();
			for(int v=choices.size()-num;v<choices.size();v++)
				selectedChoicesV.add(choices.get(v));
		}
		else
		if(selection.startsWith("PICK-"))
		{
			final int num=CMath.parseIntExpression(strFilterNow(E,ignoreStats,defPrefix,selection.substring(selection.indexOf('-')+1),piece, defined));
			if((num<0)||(num>choices.size()))
				throw new CMException("Can't pick "+num+" of "+choices.size()+" on piece '"+piece.tag()+"', Tag: "+tagName+", Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			selectedChoicesV=new Vector<XMLTag>();
			final List<XMLLibrary.XMLTag> cV=new XArrayList<XMLLibrary.XMLTag>(choices);
			final List<Integer> wV=new XArrayList<Integer>(choices.size(),true);
			for(int c=0;c<cV.size();c++)
			{
				final XMLTag lilP=cV.get(c);
				final String pickWeight=lilP.getParmValue("PICKWEIGHT");
				final int weight;
				if(pickWeight==null)
					weight=0;
				else
				{
					final String weightValue=strFilterNow(E,ignoreStats,defPrefix,pickWeight,piece, defined);
					weight=CMath.s_parseIntExpression(weightValue);
				}
				if(weight < 0)
				{
					cV.remove(c);
					c--;
				}
				else
				{
					wV.add(Integer.valueOf(weight));
				}
			}
			for(int v=0;v<num;v++)
			{
				int total=0;
				for(int c=0;c<cV.size();c++)
					total += wV.get(c).intValue();
				if(total==0)
				{
					if(cV.size()>0)
					{
						final int c=CMLib.dice().roll(1,cV.size(),-1);
						selectedChoicesV.add(cV.get(c));
						cV.remove(c);
						wV.remove(c);
					}
				}
				else
				{
					int choice=CMLib.dice().roll(1,total,0);
					int c=-1;
					while(choice>0)
					{
						c++;
						choice-=wV.get(c).intValue();
					}
					if((c>=0)&&(c<cV.size()))
					{
						selectedChoicesV.add(cV.get(c));
						cV.remove(c);
						wV.remove(c);
					}
				}
			}
		}
		else
		if(selection.equals("ANY"))
			selectedChoicesV=new XVector<XMLTag>(choices.get(CMLib.dice().roll(1,choices.size(),-1)));
		else
		if(selection.startsWith("ANY-"))
		{
			final int num=CMath.parseIntExpression(strFilterNow(E,ignoreStats,defPrefix,selection.substring(selection.indexOf('-')+1),piece, defined));
			if((num<0)||(num>choices.size()))
				throw new CMException("Can't pick last "+num+" of "+choices.size()+" on piece '"+piece.tag()+"', Tag: "+tagName+", Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			selectedChoicesV=new Vector<XMLTag>();
			final List<XMLLibrary.XMLTag> cV=new XArrayList<XMLLibrary.XMLTag>(choices);
			for(int v=0;v<num;v++)
			{
				final int x=CMLib.dice().roll(1,cV.size(),-1);
				selectedChoicesV.add(cV.get(x));
				cV.remove(x);
			}
		}
		else
		if(selection.startsWith("REPEAT-"))
		{
			final int num=CMath.parseIntExpression(strFilterNow(E,ignoreStats,defPrefix,selection.substring(selection.indexOf('-')+1),piece, defined));
			if(num<0)
				throw new CMException("Can't pick last "+num+" of "+choices.size()+" on piece '"+piece.tag()+"', Tag: "+tagName+", Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			selectedChoicesV=new Vector<XMLTag>();
			final List<XMLLibrary.XMLTag> cV=new XArrayList<XMLLibrary.XMLTag>(choices);
			for(int v=0;v<num;v++)
			{
				final int x=CMLib.dice().roll(1,cV.size(),-1);
				selectedChoicesV.add(cV.get(x));
			}
		}
		else
		if((selection.trim().length()>0)&&CMath.isMathExpression(selection))
		{
			final int num=CMath.parseIntExpression(strFilterNow(E,ignoreStats,defPrefix,selection.substring(selection.indexOf('-')+1),piece, defined));
			if((num<0)||(num>choices.size()))
				throw new CMException("Can't pick any "+num+" of "+choices.size()+" on piece '"+piece.tag()+"', Tag: "+tagName+", Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
			selectedChoicesV=new Vector<XMLTag>();
			final List<XMLLibrary.XMLTag> cV=new XArrayList<XMLLibrary.XMLTag>(choices);
			for(int v=0;v<num;v++)
			{
				final int x=CMLib.dice().roll(1,cV.size(),-1);
				selectedChoicesV.add(cV.get(x));
				cV.remove(x);
			}
		}
		else
			throw new CMException("Illegal select type '"+selection+"' on piece '"+piece.tag()+"', Tag: "+tagName+", Data: "+CMParms.toKeyValueSlashListString(piece.parms())+":"+CMStrings.limit(piece.value(),100));
		return selectedChoicesV;
	}

	protected String strFilterNow(final Modifiable E, final List<String> ignoreStats, final String defPrefix, final String str, final XMLTag piece, final Map<String,Object> defined) throws CMException
	{
		try
		{
			return strFilter(E,ignoreStats,defPrefix,str,piece,defined);
		}
		catch(final PostProcessException pe)
		{
			throw new CMException("Post processing not permitted",pe);
		}

	}

	private static enum SelectMQLState
	{
		STATE_SELECT0, // name
		STATE_SELECT1, // as or from or ,
		STATE_AS0, // as
		STATE_AS1, // expect from or , ONLY
		STATE_FROM0, // loc
		STATE_FROM1, // paren
		STATE_EXPECTWHEREOREND, // expect where
		STATE_WHERE0, // object
		STATE_WHERE1, // comparator
		STATE_WHERE2, // object rhs
		STATE_EXPECTCONNOREND, // expect connector or end of clause
		STATE_WHEREEMBEDLEFT0, // got ( on left of comparator
		STATE_WHEREEMBEDRIGHT0, // got ( on right of comparator
		STATE_EXPECTNOTHING // end of where clause
	}


	/**
	 * Class for semi-parsed MQLClause, including method
	 * to do the parsig to fill out this object
	 *
	 * @author Bo Zimmerman
	 *
	 */
	private static class MQLClause
	{
		/**
		 * Connector descriptors for connecting mql where clauses together
		 * @author Bo Zimmerman
		 *
		 */
		private static enum WhereConnector { ENDCLAUSE, AND, OR }

		/**
		 * Connector descriptors for connecting mql where clauses together
		 * @author Bo Zimmerman
		 *
		 */
		private static enum WhereComparator { EQ, NEQ, GT, LT, GTEQ, LTEQ, LIKE, IN, NOTLIKE, NOTIN }

		/** An abstract Where Clause
		 * @author Bo Zimmerman
		 *
		 */
		private static class WhereComp
		{
			private String[]		lhs		= null;
			private WhereComparator	comp	= null;
			private String[]		rhs		= null;

			private static String[] parseHS(final String parts)
			{
				if(parts.startsWith("SELECT:"))
					return new String[] {parts};
				else
					return CMStrings.replaceAll(parts,"\\\\","\\").split("\\\\");
			}
		}

		private static class WhereClause
		{
			private WhereClause		prev	= null;
			private WhereClause		parent	= null;
			private WhereClause		child	= null;
			private WhereComp		lhs		= null;
			private WhereConnector	conn	= null;
			private WhereClause		next	= null;
		}

		private enum AggregatorFunctions
		{
			COUNT,
			MEDIAN,
			MEAN,
			UNIQUE,
			FIRST,
			ANY
		}

		private static class WhatBit extends Pair<String[],String>
		{
			public AggregatorFunctions aggregator = null;

			private WhatBit(final String what[], final String as)
			{
				super(what,as);
			}

			private String[] what()
			{
				return first;
			}

			private String as()
			{
				return second;
			}

			@Override
			public String toString()
			{
				if(first[first.length-1].equals(second))
					return second;
				return first+" as "+second;
			}
		}

		private String				mql		= "";
		private final List<WhatBit>	what	= new ArrayList<WhatBit>(1);
		private final List<String[]>froms	= new ArrayList<String[]>(1);
		private WhereClause			wheres	= null;

		protected final static PrioritizingLimitedMap<String,MQLClause> cachedClauses = new PrioritizingLimitedMap<String,MQLClause>(1000,60000,600000,0);

		private boolean isTermProperlyEnded(final StringBuilder curr)
		{
			if(curr.length()<2)
				return true;
			if((curr.charAt(0)=='\"')
			||(curr.charAt(0)=='\''))
			{
				final int endDex=curr.length()-1;
				if(curr.charAt(endDex) != curr.charAt(0))
					return false;
				if((curr.length()>2)&&(curr.charAt(endDex-1)=='\\'))
					return false;
			}
			return true;
		}

		/**
		 * parse the mql statement into this object
		 *
		 * @param str the original mql statement
		 * @param mqlbits mql statement, minus select: must be ALL UPPERCASE
		 * @return the parsed MQL clause
		 * @throws CMException
		 */
		protected static MQLClause parseMQL(final String str, final String mqlbits) throws MQLException
		{
			if(cachedClauses.containsKey(mqlbits))
				return cachedClauses.get(mqlbits);
			final MQLClause clause=new MQLClause();
			clause.parseInternalMQL(str, mqlbits);
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.MQLCACHE))
				cachedClauses.put(mqlbits, clause);
			return clause;
		}


		/**
		 * parse the mql statement into this object
		 *
		 * @param str the original mql statement
		 * @param mqlbits mql statement, minus select: must be ALL UPPERCASE
		 * @throws CMException
		 */
		private void parseInternalMQL(final String str, final String mqlbits) throws MQLException
		{
			this.mql=str;
			final StringBuilder curr=new StringBuilder("");
			int pdepth=0;
			WhereClause wheres = new WhereClause();
			this.wheres=wheres;
			WhereComp	wcomp  = new WhereComp();
			SelectMQLState state=SelectMQLState.STATE_SELECT0;
			for(int i=0;i<=mqlbits.length();i++)
			{
				final char c=(i==mqlbits.length())?' ':mqlbits.charAt(i);
				switch(state)
				{
				case STATE_SELECT0: // select state
				{
					if(Character.isWhitespace(c))
					{
						if(curr.length()>0)
						{
							if(isTermProperlyEnded(curr))
							{
								// we just got a name symbol, so go to state 1 and expect AS or FROM or ,
								what.add(new WhatBit(WhereComp.parseHS(curr.toString()),curr.toString()));
								state=SelectMQLState.STATE_SELECT1;
								curr.setLength(0);
							}
							else
								curr.append(c);
						}
					}
					else
					if(c==',')
					{
						if(curr.length()>0)
						{
							if(isTermProperlyEnded(curr))
							{
								what.add(new WhatBit(WhereComp.parseHS(curr.toString()),curr.toString()));
								curr.setLength(0);
							}
							else
								curr.append(c);
						}
						else
							throw new MQLException("Unexpected , in Malformed mql: "+str);
					}
					else
						curr.append(c);
					break;
				}
				case STATE_SELECT1: // expect AS or FROM or ,
				{
					if(Character.isWhitespace(c))
					{
						if(curr.length()>0)
						{
							if(curr.toString().equals("FROM"))
							{
								curr.setLength(0);
								state=SelectMQLState.STATE_FROM0;
							}
							else
							if(curr.toString().equals("AS"))
							{
								curr.setLength(0);
								state=SelectMQLState.STATE_AS0;
							}
							else
								throw new MQLException("Unexpected select string in Malformed mql: "+str);
						}
					}
					else
					if(c==',')
					{
						if(curr.length()==0)
							state=SelectMQLState.STATE_SELECT0;
						else
							throw new MQLException("Unexpected , in Malformed mql: "+str);
					}
					else
						curr.append(c);
					break;
				}
				case STATE_AS0: // as name
				{
					if(Character.isWhitespace(c)
					||(c==','))
					{
						if(curr.length()>0)
						{
							if(isTermProperlyEnded(curr))
							{
								if(curr.toString().equals("FROM"))
									throw new MQLException("Unexpected FROM in Malformed mql: "+str);
								else
								if(curr.toString().equals("AS"))
									throw new MQLException("Unexpected AS in Malformed mql: "+str);
								else
								{
									state=SelectMQLState.STATE_AS1; // expect from or , ONLY
									what.get(what.size()-1).second = curr.toString();
									curr.setLength(0);
								}
							}
							else
								curr.append(c);
						}
						else
						if(c==',')
							throw new MQLException("Unexpected , in Malformed mql: "+str);
					}
					else
						curr.append(c);
					break;
				}
				case STATE_AS1: // expect FROM or , only
				{
					if(Character.isWhitespace(c))
					{
						if(curr.length()>0)
						{
							if(curr.toString().equals("FROM"))
							{
								curr.setLength(0);
								state=SelectMQLState.STATE_FROM0;
							}
							else
								throw new MQLException("Unexpected name string in Malformed mql: "+str);
						}
					}
					else
					if(c==',')
					{
						if(curr.length()==0)
							state=SelectMQLState.STATE_SELECT0;
						else
							throw new MQLException("Unexpected , in Malformed mql: "+str);
					}
					else
						curr.append(c);
					break;
				}
				case STATE_FROM0: // from state
				{
					if(Character.isWhitespace(c)||(c==','))
					{
						if(curr.length()>0)
						{
							if(isTermProperlyEnded(curr))
							{
								if(curr.toString().equals("WHERE"))
									throw new MQLException("Unexpected WHERE in Malformed mql: "+str);
								else
								{
									froms.add(WhereComp.parseHS(curr.toString()));
									curr.setLength(0);
									if(c!=',')
										state=SelectMQLState.STATE_EXPECTWHEREOREND; // now expect where
								}
							}
							else
								curr.append(c);
						}
					}
					else
					if(c=='(')
					{
						if(curr.length()==0)
							state=SelectMQLState.STATE_FROM1;
						else
							throw new MQLException("Unexpected ( in Malformed mql: "+str);
					}
					else
						curr.append(c);
					break;
				}
				case STATE_FROM1: // from () state
				{
					if(c=='(')
					{
						curr.append(c);
						pdepth++;
					}
					else
					if(c==')')
					{
						if(pdepth==0)
						{
							froms.add(WhereComp.parseHS(curr.toString()));
							state=SelectMQLState.STATE_EXPECTWHEREOREND; // expect where
							curr.setLength(0);
						}
						else
						{
							curr.append(c);
							pdepth--;
						}
					}
					else
						curr.append(c);
					break;
				}
				case STATE_EXPECTWHEREOREND: // expect where clause
				{
					if(Character.isWhitespace(c)||(c==','))
					{
						if(curr.length()>0)
						{
							if(c==',')
								throw new MQLException("Expected '"+curr.toString()+"' in Malformed mql: "+str);
							else
							if(curr.toString().equals(";"))
							{
								curr.setLength(0);
								state=SelectMQLState.STATE_EXPECTNOTHING;
							}
							else
							if(!curr.toString().equals("WHERE"))
								throw new MQLException("Expected WHERE in Malformed mql: "+str);
							else
							{
								curr.setLength(0);
								state=SelectMQLState.STATE_WHERE0;
							}
						}
						else
						if(c==',')
							state=SelectMQLState.STATE_FROM0;
					}
					else
						curr.append(c);
					break;
				}
				case STATE_WHERE0: // initial where state
				{
					if(Character.isWhitespace(c)
					||("<>!=".indexOf(c)>=0))
					{
						if(curr.length()>0)
						{
							if(isTermProperlyEnded(curr))
							{
								wcomp=new WhereComp();
								if((wheres.lhs!=null)
								||(wheres.parent!=null))
								{
									final WhereClause newClause = new WhereClause();
									newClause.prev=wheres;
									wheres.next=newClause;
									wheres=newClause;
								}
								wheres.lhs=wcomp;
								wcomp.lhs = WhereComp.parseHS(curr.toString());
								curr.setLength(0);
								if(!Character.isWhitespace(c))
									curr.append(c);
								state=SelectMQLState.STATE_WHERE1; // now expect comparator
							}
							else
								curr.append(c);
						}
					}
					else
					if(c=='(')
					{
						if(curr.length()==0)
							state=SelectMQLState.STATE_WHEREEMBEDLEFT0;
						else
							throw new MQLException("Unexpected ( in Malformed mql: "+str);
					}
					else
					if(c==')')
						throw new MQLException("Unexpected ) in Malformed mql: "+str);
					else
						curr.append(c);
					break;
				}
				case STATE_WHEREEMBEDLEFT0:
				{
					if(c=='(')
					{
						if(curr.length()==0)
						{
							final WhereClause priorityClause = new WhereClause();
							if((wheres.lhs!=null)
							||(wheres.parent!=null)
							||(wheres.conn!=null))
							{
								final WhereClause dummyClause = new WhereClause();
								wheres.next=dummyClause;
								dummyClause.prev=wheres;
								wheres=dummyClause;
							}
							priorityClause.child=wheres;
							wheres.parent=priorityClause;
							wheres=priorityClause;
							i--;
							state=SelectMQLState.STATE_WHERE0; // expect lhs of a comp
						}
						else
						{
							curr.append(c);
							pdepth++;
						}
					}
					else
					if(c==')')
					{
						if(pdepth==0)
						{
							wcomp=new WhereComp();
							if((wheres.lhs!=null)
							||(wheres.parent!=null)
							||(wheres.conn!=null))
							{
								final WhereClause newClause = new WhereClause();
								wheres.next=newClause;
								newClause.prev=wheres;
								wheres=newClause;
							}
							wheres.lhs=wcomp;
							wcomp.lhs = WhereComp.parseHS(curr.toString());
							state=SelectMQLState.STATE_WHERE1; // expect connector or endofclause
							curr.setLength(0);
						}
						else
						{
							curr.append(c);
							pdepth--;
						}
					}
					else
					if(Character.isWhitespace(c) && (curr.length()==0))
					{}
					else
					{
						curr.append(c);
						if((curr.length()<8)
						&&(!"SELECT:".startsWith(curr.toString())))
						{
							final WhereClause priorityClause = new WhereClause();
							if((wheres.lhs!=null)
							||(wheres.parent!=null)
							||(wheres.conn!=null))
							{
								final WhereClause dummyClause = new WhereClause();
								wheres.next=dummyClause;
								dummyClause.prev=wheres;
								wheres=dummyClause;
							}
							priorityClause.child=wheres;
							wheres.parent=priorityClause;
							wheres=priorityClause;
							i=mqlbits.lastIndexOf('(',i);
							curr.setLength(0);
							state=SelectMQLState.STATE_WHERE0; // expect lhs of a comp
						}
					}
					break;
				}
				case STATE_WHERE1: // expect comparator
				{
					if(curr.length()==0)
					{
						if(!Character.isWhitespace(c))
							curr.append(c);
					}
					else
					{
						boolean saveC = false;
						boolean done=false;
						if("<>!=".indexOf(c)>=0)
						{
							if("<>!=".indexOf(curr.charAt(0))>=0)
							{
								curr.append(c);
								done=curr.length()>=2;
							}
							else
								throw new MQLException("Unexpected '"+c+"' in Malformed mql: "+str);
						}
						else
						if(!Character.isWhitespace(c))
						{
							if("<>!=".indexOf(curr.charAt(0))>=0)
							{
								saveC=true;
								done=true;
							}
							else
								curr.append(c);
						}
						else
							done=true;
						if(done)
						{
							final String fcurr=curr.toString();
							if(fcurr.equals("="))
							{
								wcomp.comp=WhereComparator.EQ;
								curr.setLength(0);
								state=SelectMQLState.STATE_WHERE2; // now expect RHS
							}
							else
							if(fcurr.equals("!=")||fcurr.equals("<>"))
							{
								wcomp.comp=WhereComparator.NEQ;
								curr.setLength(0);
								state=SelectMQLState.STATE_WHERE2; // now expect RHS
							}
							else
							if(fcurr.equals(">"))
							{
								wcomp.comp=WhereComparator.GT;
								curr.setLength(0);
								state=SelectMQLState.STATE_WHERE2; // now expect RHS
							}
							else
							if(fcurr.equals("<"))
							{
								wcomp.comp=WhereComparator.LT;
								curr.setLength(0);
								state=SelectMQLState.STATE_WHERE2; // now expect RHS
							}
							else
							if(fcurr.equals(">=")||fcurr.equals("=>"))
							{
								wcomp.comp=WhereComparator.GTEQ;
								curr.setLength(0);
								state=SelectMQLState.STATE_WHERE2; // now expect RHS
							}
							else
							if(fcurr.equals("<=")||fcurr.equals("<="))
							{
								wcomp.comp=WhereComparator.LTEQ;
								curr.setLength(0);
								state=SelectMQLState.STATE_WHERE2; // now expect RHS
							}
							else
							if(fcurr.equals("IN"))
							{
								wcomp.comp=WhereComparator.IN;
								curr.setLength(0);
								state=SelectMQLState.STATE_WHERE2; // now expect RHS
							}
							else
							if(fcurr.equals("LIKE"))
							{
								wcomp.comp=WhereComparator.LIKE;
								curr.setLength(0);
								state=SelectMQLState.STATE_WHERE2; // now expect RHS
							}
							else
							if(fcurr.equals("NOTIN"))
							{
								wcomp.comp=WhereComparator.NOTIN;
								curr.setLength(0);
								state=SelectMQLState.STATE_WHERE2; // now expect RHS
							}
							else
							if(fcurr.equals("NOTLIKE"))
							{
								wcomp.comp=WhereComparator.NOTLIKE;
								curr.setLength(0);
								state=SelectMQLState.STATE_WHERE2; // now expect RHS
							}
							else
								throw new MQLException("Unexpected '"+fcurr+"' in Malformed mql: "+str);
							if(saveC)
								curr.append(c);
						}
					}
					break;
				}
				case STATE_WHERE2: // where rhs of clause
				{
					if(Character.isWhitespace(c))
					{
						if(curr.length()>0)
						{
							if(isTermProperlyEnded(curr))
							{
								wcomp.rhs = WhereComp.parseHS(curr.toString());
								curr.setLength(0);
								state=SelectMQLState.STATE_EXPECTCONNOREND;
							}
							else
								curr.append(c);
						}
					}
					else
					if(c==')')
					{
						if(curr.length()==0)
							throw new MQLException("Unexpected ): Malformed mql: "+str);
						wcomp.rhs = WhereComp.parseHS(curr.toString());
						curr.setLength(0);
						while(wheres.prev!=null)
							wheres=wheres.prev;
						if(wheres.child==null)
							throw new MQLException("Unexpected ): Malformed mql: "+str);
						wheres=wheres.child;
						state=SelectMQLState.STATE_EXPECTCONNOREND;
					}
					else
					if(c==';')
					{
						if(curr.length()==0)
							throw new MQLException("Unexpected ; in Malformed mql: "+str);
						else
						{
							wcomp.rhs = WhereComp.parseHS(curr.toString());
							curr.setLength(0);
							state=SelectMQLState.STATE_EXPECTNOTHING;
						}
					}
					else
					if(c=='(')
					{
						if(curr.length()==0)
							state=SelectMQLState.STATE_WHEREEMBEDRIGHT0;
						else
							throw new MQLException("Unexpected ( in Malformed mql: "+str);
					}
					else
						curr.append(c);
					break;
				}
				case STATE_WHEREEMBEDRIGHT0:
				{
					if(c=='(')
					{
						curr.append(c);
						pdepth++;
					}
					else
					if(c==')')
					{
						if(pdepth==0)
						{
							wcomp.rhs = WhereComp.parseHS(curr.toString());
							state=SelectMQLState.STATE_EXPECTCONNOREND; // expect connector or endofclause
							curr.setLength(0);
						}
						else
						{
							curr.append(c);
							pdepth--;
						}
					}
					else
						curr.append(c);
					break;
				}
				case STATE_EXPECTCONNOREND: // expect connector or endofclause
				{
					if(c==';')
					{
						state=SelectMQLState.STATE_EXPECTNOTHING;
					}
					else
					if(Character.isWhitespace(c) || (c=='('))
					{
						if(curr.length()>0)
						{
							if(curr.toString().equals("AND"))
							{
								wheres.conn = WhereConnector.AND;
								state=SelectMQLState.STATE_WHERE0;
								curr.setLength(0);
								if(c=='(')
									i--;
							}
							else
							if(curr.toString().equals("OR"))
							{
								wheres.conn = WhereConnector.OR;
								state=SelectMQLState.STATE_WHERE0;
								curr.setLength(0);
								if(c=='(')
									i--;
							}
							else
								throw new MQLException("Unexpected '"+curr.toString()+"': Malformed mql: "+str);
						}
						else
						if(c=='(')
							throw new MQLException("Unexpected ): Malformed mql: "+str);
					}
					else
					if((c==')') && (curr.length()==0))
					{
						while(wheres.prev!=null)
							wheres=wheres.prev;
						if(wheres.child==null)
							throw new MQLException("Unexpected ): Malformed mql: "+str);
						wheres=wheres.child;
					}
					else
						curr.append(c);
					break;
				}
				default:
					if(!Character.isWhitespace(c))
						throw new MQLException("Unexpected '"+c+"': Malformed mql: "+str);
					break;
				}
			}
			if((state != SelectMQLState.STATE_EXPECTNOTHING)
			&&(state != SelectMQLState.STATE_EXPECTWHEREOREND)
			&&(state != SelectMQLState.STATE_EXPECTCONNOREND))
				throw new MQLException("Unpected end of clause in state "+state.toString()+" in mql: "+str);
			// finally, parse the aggregators
			for(final WhatBit W : this.what)
			{
				final String[] w=W.what();
				if(w.length<2)
					continue;
				W.aggregator=(MQLClause.AggregatorFunctions)CMath.s_valueOf(MQLClause.AggregatorFunctions.class, w[0]);
				if(W.aggregator != null)
					W.first=Arrays.copyOfRange(W.first, 1, W.first.length);
			}
		}
	}

	protected void doneWithMQLObject(final Object o)
	{
		if(o instanceof Physical)
		{
			final Physical P=(Physical)o;
			if(!P.isSavable())
			{
				if((P instanceof DBIdentifiable)
				&&(((DBIdentifiable)P).databaseID().equals("DELETE")))
					P.destroy();
				else
				if((P instanceof Area)
				&&(((Area)P).getAreaState()==Area.State.STOPPED))
					P.destroy();
				else
				if((P instanceof Room)
				&&(((Room)P).getArea().amDestroyed()))
					P.destroy();
			}
		}
	}

	protected List<Object> parseMQLCMFile(final CMFile F, final String mql) throws MQLException
	{
		final List<Object> from=new LinkedList<Object>();
		String str=F.text().toString().trim();
		// normalize singletons
		if(str.startsWith("<MOB>"))
			str="<MOBS>"+str+"</MOB>";
		else
		if(str.startsWith("<ITEM>"))
			str="<ITEMS>"+str+"</ITEMS>";
		else
		if(str.startsWith("<AREA>"))
			str="<AREAS>"+str+"</AREAS>";

		if(str.startsWith("<MOBS>"))
		{
			final List<MOB> mobList=new LinkedList<MOB>();
			final String err = CMLib.coffeeMaker().addMOBsFromXML(str, mobList, null);
			if((err!=null)&&(err.length()>0))
				throw new MQLException("CMFile "+F.getAbsolutePath()+" failed mob parsing '"+err+"' in "+mql);
			for(final MOB M : mobList)
			{
				CMLib.threads().deleteAllTicks(M);
				M.setSavable(false);
				M.setDatabaseID("DELETE");
			}
			from.addAll(mobList);
		}
		else
		if(str.startsWith("<ITEMS>"))
		{
			final List<Item> itemList=new LinkedList<Item>();
			final String err = CMLib.coffeeMaker().addItemsFromXML(str, itemList, null);
			if((err!=null)&&(err.length()>0))
				throw new MQLException("CMFile "+F.getAbsolutePath()+" failed item parsing '"+err+"' in "+mql);
			for(final Item I : itemList)
			{
				CMLib.threads().deleteAllTicks(I);
				I.setSavable(false);
				I.setDatabaseID("DELETE");
			}
			from.addAll(itemList);
		}
		else
		if(str.startsWith("<AREAS>"))
		{
			final List<Area> areaList=new LinkedList<Area>();
			final List<List<XMLLibrary.XMLTag>> areas=new ArrayList<List<XMLLibrary.XMLTag>>();
			String err=CMLib.coffeeMaker().fillAreasVectorFromXML(str,areas,null,null);
			if((err!=null)&&(err.length()>0))
				throw new MQLException("CMFile "+F.getAbsolutePath()+" failed area parsing '"+err+"' in "+mql);
			for(final List<XMLLibrary.XMLTag> area : areas)
				err=CMLib.coffeeMaker().unpackAreaFromXML(area, null, null, true, false);
			for(final Area A : areaList)
			{
				CMLib.threads().deleteAllTicks(A);
				A.setSavable(false);
				A.setAreaState(State.STOPPED);
			}
			from.addAll(areaList);
		}
		else
		if(str.startsWith("<AROOM>"))
		{
			final List<Room> roomList=new LinkedList<Room>();
			final Area dumbArea=CMClass.getAreaType("StdArea");
			CMLib.flags().setSavable(dumbArea, false);
			final List<XMLLibrary.XMLTag> tags=CMLib.xml().parseAllXML(str);
			final String err=CMLib.coffeeMaker().unpackRoomFromXML(dumbArea, tags, true, false);
			if((err!=null)&&(err.length()>0)||(!dumbArea.getProperMap().hasMoreElements()))
				throw new MQLException("CMFile "+F.getAbsolutePath()+" failed room parsing '"+err+"' in "+mql);
			roomList.add(dumbArea.getProperMap().nextElement());
			for(final Room R : roomList)
			{
				CMLib.threads().deleteAllTicks(R);
				dumbArea.delProperRoom(R);
				R.setSavable(false);
			}
			dumbArea.destroy();
			from.addAll(roomList);
		}
		else
			throw new MQLException("CMFile "+F.getAbsolutePath()+" not selectable from in "+mql);
		return from;
	}

	protected List<Object> flattenMQLObjectList(final Collection<Object> from)
	{
		final List<Object> flat=new LinkedList<Object>();
		for(final Object o1 : from)
		{
			if((o1 instanceof CMObject)
			||(o1 instanceof String))
				flat.add(o1);
			else
			if(o1 instanceof List)
			{
				@SuppressWarnings("unchecked")
				final List<Object> nl = (List<Object>)o1;
				flat.addAll(flattenMQLObjectList(nl));
			}
			else
			if(o1 instanceof Map)
			{
				@SuppressWarnings("unchecked")
				final Map<Object,Object> m=(Map<Object,Object>)o1;
				flat.addAll(flattenMQLObjectList(m.values()));
			}
			else
				flat.add(o1);
		}
		return flat;
	}

	protected List<Object> parseMQLFrom(final String[] fromClause, final String mql, final Modifiable E, final List<String> ignoreStats,
			final String defPrefix, final XMLTag piece, final Map<String,Object> defined, final boolean literalsOK)
			throws MQLException,PostProcessException
	{
		final List<Object> from=new LinkedList<Object>();
		if((fromClause.length==1)
		&&(fromClause[0].startsWith("SELECT:")))
		{
			from.addAll(doMQLSelectObjs(E, ignoreStats, defPrefix, fromClause[0], piece, defined));
			return from;
		}
		for(final String f : fromClause)
		{
			final MQLSpecialFromSet set=(MQLSpecialFromSet)CMath.s_valueOf(MQLSpecialFromSet.class, f);
			if(set != null)
			{
				switch(set)
				{
				case AREA:
					if(from.size()==0)
					{
						final Area A=(E instanceof Environmental) ? CMLib.map().areaLocation(E) : null;
						if(A==null)
							throw new MQLException("Unknown sub-from "+f+" on "+(""+E)+" in "+mql);
						else
						if(!from.contains(A))
							from.add(A);
						break;
					}
					//$FALL-THROUGH$
				case AREAS:
					{
						if(from.size()==0)
							from.addAll(new XVector<Area>(CMLib.map().areas()));
						else
						{
							final List<Object> oldFrom=flattenMQLObjectList(from);
							from.clear();
							for(final Object o : oldFrom)
							{
								final Area A;
								if (o instanceof CMObject)
									A=CMLib.map().areaLocation((CMObject)o);
								else
									A=null;
								if(A==null)
									throw new MQLException("Unknown sub-from "+f+" on "+o.toString()+" in "+mql);
								else
								if(!from.contains(A))
									from.add(A);
							}
						}
					}
					break;
				case ROOM:
					if(from.size()==0)
					{
						final Room R=(E instanceof Environmental) ? CMLib.map().roomLocation((Environmental)E) : null;
						if(R==null)
							throw new MQLException("Unknown sub-from "+f+" on "+(""+E)+" in "+mql);
						else
						if(!from.contains(R))
							from.add(R);
						break;
					}
					//$FALL-THROUGH$
				case ROOMS:
					{
						if((from.size()==0)
						&&(E instanceof Area))
							from.add(E);
						if(from.size()==0)
							from.addAll(new XVector<Room>(CMLib.map().rooms()));
						else
						{
							final List<Object> oldFrom=flattenMQLObjectList(from);
							from.clear();
							for(final Object o : oldFrom)
							{
								final Room R;
								if(o instanceof Area)
								{
									from.addAll(new XVector<Room>(((Area)o).getProperMap()));
									continue;
								}
								if (o instanceof Environmental)
									R=CMLib.map().roomLocation((Environmental)o);
								else
									R=null;
								if(R==null)
									throw new MQLException("Unknown sub-from "+f+" on "+o.toString()+" in "+mql);
								else
								if(!from.contains(R))
									from.add(R);
							}
						}
					}
					break;
				case EXITS:
					{
						final List<Object> oldFrom;
						if(from.size()==0)
						{
							final Room R=(E instanceof Environmental) ? CMLib.map().roomLocation((Environmental)E) : null;
							if(R==null)
								throw new MQLException("Unknown sub-from "+f+" on "+(""+E)+" in "+mql);
							oldFrom=new XVector<Object>(R);
						}
						else
							oldFrom=flattenMQLObjectList(from);
						from.clear();
						for(final Object o : oldFrom)
						{
							if(o instanceof Area)
							{
								for(final Enumeration<Room> r=((Area)o).getProperMap();r.hasMoreElements();)
								{
									final Room R=r.nextElement();
									if(R!=null)
									{
										for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
										{
											final Room R2=R.rawDoors()[d];
											if((R2!=null)&&(R2.roomID().length()>0))
											{
												final Exit E2=R.getExitInDir(d);
												if((E2!=null)&&(!from.contains(E2)))
													from.add(E2);
											}
										}
									}
								}
							}
							if(o instanceof Environmental)
							{
								final Room R=CMLib.map().roomLocation((Environmental)o);
								if(R!=null)
								{
									for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
									{
										final Room R2=R.rawDoors()[d];
										if((R2!=null)&&(R2.roomID().length()>0))
										{
											final Exit E2=R.getExitInDir(d);
											if((E2!=null)&&(!from.contains(E2)))
												from.add(E2);
										}
									}
								}
							}
							else
								throw new MQLException("Unknown sub-from "+f+" on "+o.toString()+" in "+mql);
						}
					}
					break;
				case PLAYER:
					if(from.size()==0)
					{
						final MOB oE=(E instanceof MOB) ? (MOB)E : null;
						if((oE==null)||(!oE.isPlayer()))
							throw new MQLException("Unknown sub-from "+f+" on "+(""+E)+" in "+mql);
						else
						if(!from.contains(oE))
							from.add(oE);
						break;
					}
					//$FALL-THROUGH$
				case PLAYERS:
					if((f.equals("PLAYERS"))
					||((from.size()>0)&&(f.equals("PLAYER"))))
					{
						if((from.size()==0)
						&&((E instanceof Area)||(E instanceof Room)||(E instanceof MOB)))
							from.add(E);
						if(from.size()==0)
						{
							final Enumeration<Session> sesss = new IteratorEnumeration<Session>(CMLib.sessions().allIterableAllHosts().iterator());
							final Enumeration<MOB> m=new FilteredEnumeration<MOB>(new ConvertingEnumeration<Session, MOB>(sesss, sessionToMobConvereter), noMobFilter);
							for(;m.hasMoreElements();)
								from.add(m.nextElement());
						}
						else
						{
							final List<Object> oldFrom=flattenMQLObjectList(from);
							from.clear();
							for(final Object o : oldFrom)
							{
								if(o instanceof Area)
								{
									final Enumeration<Session> sesss = new IteratorEnumeration<Session>(CMLib.sessions().allIterableAllHosts().iterator());
									final Enumeration<MOB> m=new FilteredEnumeration<MOB>(new ConvertingEnumeration<Session, MOB>(sesss, sessionToMobConvereter), noMobFilter);
									for(;m.hasMoreElements();)
									{
										final MOB M=m.nextElement();
										if(CMLib.map().areaLocation(M) == o)
											from.add(M);
									}
								}
								else
								if(o instanceof Room)
								{
									for(final Enumeration<MOB> m=((Room)o).inhabitants();m.hasMoreElements();)
									{
										final MOB M=m.nextElement();
										if((M!=null)
										&&(M.isPlayer()))
											from.add(M);
									}
								}
								if(o instanceof MOB)
								{
									if(((MOB)o).isPlayer())
										from.add(o);
								}
								else
								if(o instanceof Item)
								{
									final Item I=(Item)o;
									if((I.owner() instanceof MOB)
									&&(((MOB)I.owner())).isPlayer())
										from.add(I.owner());
								}
								else
									throw new MQLException("Unknown sub-from "+f+" on "+o.toString()+" in "+mql);
							}
						}
					}
					break;
				case MOB:
					if(from.size()==0)
					{
						final Environmental oE=(E instanceof MOB) ? (Environmental)E : null;
						if(oE==null)
							throw new MQLException("Unknown sub-from "+f+" on "+(""+E)+" in "+mql);
						else
						if(!from.contains(oE))
							from.add(oE);
						break;
					}
					//$FALL-THROUGH$
				case MOBS:
					{
						if((from.size()==0)
						&&((E instanceof Area)||(E instanceof Room)||(E instanceof MOB)))
							from.add(E);
						if(from.size()==0)
							from.addAll(new XVector<MOB>(CMLib.map().worldMobs()));
						else
						{
							final List<Object> oldFrom=flattenMQLObjectList(from);
							from.clear();
							for(final Object o : oldFrom)
							{
								if(o instanceof Area)
								{
									for(final Enumeration<Room> r=((Area)o).getFilledCompleteMap();r.hasMoreElements();)
									{
										final Room R=r.nextElement();
										if(R.numInhabitants()>0)
										{
											for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
											{
												final MOB M=m.nextElement();
												if(!from.contains(M))
													from.add(M);
											}
										}
									}
								}
								else
								if((o instanceof MOB)&&(f.equals("MOB")))
									from.add(o);
								else
								{
									final Room R;
									if (o instanceof Environmental)
										R=CMLib.map().roomLocation((Environmental)o);
									else
										R=null;
									if(R==null)
										throw new MQLException("Unknown sub-from "+f+" on "+o.toString()+" in "+mql);
									else
									if(R.numInhabitants()>0)
									{
										for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
										{
											final MOB M=m.nextElement();
											if(!from.contains(M))
												from.add(M);
										}
									}
								}
							}
						}
					}
					break;
				case NPC:
					if(from.size()==0)
					{
						final MOB oE=(E instanceof MOB) ? (MOB)E : null;
						if((oE==null) || (oE.isPlayer()))
							throw new MQLException("Unknown sub-from "+f+" on "+(""+E)+" in "+mql);
						else
						if(!from.contains(oE))
							from.add(oE);
						break;
					}
					//$FALL-THROUGH$
				case NPCS:
					{
						if((from.size()==0)
						&&((E instanceof Area)||(E instanceof Room)||(E instanceof MOB)))
							from.add(E);
						if(from.size()==0)
							from.addAll(new XVector<MOB>(new FilteredEnumeration<MOB>(CMLib.map().worldMobs(),npcFilter)));
						else
						{
							final List<Object> oldFrom=flattenMQLObjectList(from);
							from.clear();
							for(final Object o : oldFrom)
							{
								if(o instanceof Area)
								{
									for(final Enumeration<Room> r=((Area)o).getFilledCompleteMap();r.hasMoreElements();)
									{
										final Room R=r.nextElement();
										if(R.numInhabitants()>0)
										{
											for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
											{
												final MOB M=m.nextElement();
												if(npcFilter.passesFilter(M) && (!from.contains(M)))
													from.add(M);
											}
										}
									}
								}
								else
								if((o instanceof MOB)
								&&(f.equals("NPC"))
								&&(npcFilter.passesFilter((MOB)o)))
									from.add(o);
								else
								{
									final Room R;
									if (o instanceof Environmental)
										R=CMLib.map().roomLocation((Environmental)o);
									else
										R=null;
									if(R==null)
										throw new MQLException("Unknown sub-from "+f+" on "+(""+o)+" in "+mql);
									else
									if(R.numInhabitants()>0)
									{
										for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
										{
											final MOB M=m.nextElement();
											if(npcFilter.passesFilter(M) && (!from.contains(M)))
												from.add(M);
										}
									}
								}
							}
						}
					}
					break;
				case SHOP:
					if(from.size()==0)
					{
						final Environmental oE=((E instanceof Environmental) && (CMLib.coffeeShops().getShopKeeper((Environmental)E) != null))
								? (Environmental)E : null;
						if(oE==null)
							throw new MQLException("Unknown sub-from "+f+" on "+(""+E)+" in "+mql);
						else
						if(!from.contains(oE))
							from.add(oE);
						break;
					}
					//$FALL-THROUGH$
				case SHOPS:
					{
						final ShoppingLibrary shopper=CMLib.coffeeShops();
						if((from.size()==0)
						&&((E instanceof Area)||(E instanceof Room)||(E instanceof MOB)||(E instanceof Item)))
							from.add(E);
						if(from.size()==0)
							from.addAll(new XVector<Room>(CMLib.map().rooms()));
						final List<Object> oldFrom=flattenMQLObjectList(from);
						from.clear();
						for(final Object o : oldFrom)
						{
							if(o instanceof Area)
							{
								for(final Enumeration<Room> r=((Area)o).getFilledCompleteMap();r.hasMoreElements();)
								{
									final Room R=r.nextElement();
									final List<Environmental> shops=shopper.getAllShopkeepers(R, null);
									if(shops.size()>0)
										from.addAll(shops);
								}
							}
							else
							if(o instanceof Room)
							{
								final Room R=(Room)o;
								final List<Environmental> shops=shopper.getAllShopkeepers(R, null);
								if(shops.size()>0)
									from.addAll(shops);
							}
							else
							if((o instanceof Environmental)
							&&(shopFilter.passesFilter((Environmental)o)))
								from.add(o);
						}
					}
					break;
				case ITEM:
					if(from.size()==0)
					{
						final Environmental oE=(E instanceof Item) ? (Environmental)E : null;
						if(oE==null)
							throw new MQLException("Unknown sub-from "+f+" on "+(""+E)+" in "+mql);
						else
						if(!from.contains(oE))
							from.add(oE);
						break;
					}
					//$FALL-THROUGH$
				case ITEMS:
					{
						if((from.size()==0)
						&&((E instanceof Area)||(E instanceof Room)))
							from.add(E);
						if(from.size()==0)
							from.addAll(new XVector<Item>(CMLib.map().worldEveryItems()));
						else
						{
							final List<Object> oldFrom=flattenMQLObjectList(from);
							from.clear();
							for(final Object o : oldFrom)
							{
								if(o instanceof Area)
								{
									for(final Enumeration<Room> r=((Area)o).getFilledCompleteMap();r.hasMoreElements();)
										from.addAll(new XVector<Item>(r.nextElement().itemsRecursive()));
								}
								else
								if((o instanceof Item)
								&&(f.equals("ITEM")))
									from.add(o);
								else
								{
									final Room R;
									if (o instanceof Environmental)
										R=CMLib.map().roomLocation((Environmental)o);
									else
										R=null;
									if(R==null)
										throw new MQLException("Unknown sub-from "+f+" on "+o.toString()+" in "+mql);
									else
										from.addAll(new XVector<Item>(R.itemsRecursive()));
								}
							}
						}
					}
					break;
				case EQUIPMENT:
					{
						if((from.size()==0)
						&&((E instanceof Area)||(E instanceof Room)||(E instanceof MOB)||(E instanceof Item)))
							from.add(E);
						if(from.size()==0)
						{
							final Environmental oE=(E instanceof MOB) ? (Environmental)E : null;
							if(oE==null)
								throw new MQLException("Unknown sub-from "+f+" on "+(""+E)+" in "+mql);
							else
							{
								for(final Enumeration<Item> i=((MOB)oE).items();i.hasMoreElements();)
								{
									final Item I=i.nextElement();
									if((I!=null)
									&&(I.amBeingWornProperly()))
										from.add(I);
								}
							}
						}
						else
						{
							final List<Object> oldFrom=flattenMQLObjectList(from);
							from.clear();
							for(final Object o : oldFrom)
							{
								if(o instanceof Area)
								{
									for(final Enumeration<Room> r=((Area)o).getFilledCompleteMap();r.hasMoreElements();)
									{
										for(final Enumeration<MOB> m=r.nextElement().inhabitants();m.hasMoreElements();)
										{
											final MOB M=m.nextElement();
											for(final Enumeration<Item> i=M.items();i.hasMoreElements();)
											{
												final Item I=i.nextElement();
												if((I!=null)
												&&(I.amBeingWornProperly()))
													from.add(I);
											}
										}
									}
								}
								else
								if(o instanceof MOB)
								{
									for(final Enumeration<Item> i=((MOB)o).items();i.hasMoreElements();)
									{
										final Item I=i.nextElement();
										if((I!=null)
										&&(I.amBeingWornProperly()))
											from.add(I);
									}
								}
								else
								if(o instanceof Room)
								{
									for(final Enumeration<MOB> m=((Room)o).inhabitants();m.hasMoreElements();)
									{
										final MOB M=m.nextElement();
										for(final Enumeration<Item> i=M.items();i.hasMoreElements();)
										{
											final Item I=i.nextElement();
											if((I!=null)
											&&(I.amBeingWornProperly()))
												from.add(I);
										}
									}
								}
								else
								if(o instanceof Item)
								{
									if(((Item)o).amBeingWornProperly())
										from.add(o);
								}
								else
									throw new MQLException("Unknown sub-from "+f+" on "+o.toString()+" in "+mql);
							}
						}
					}
					break;
				case SHOPITEMS:
					{
						if((from.size()==0)
						&&((E instanceof Area)||(E instanceof Room)||(E instanceof MOB)))
							from.add(E);
						if(from.size()==0)
						{
							final ShopKeeper oE=(E instanceof ShopKeeper) ? (ShopKeeper)E : null;
							if(oE==null)
								throw new MQLException("Unknown sub-from "+f+" on "+(""+E)+" in "+mql);
							else
							{
								for(final Iterator<Environmental> i=oE.getShop().getStoreInventory();i.hasNext();)
								{
									final Environmental I=i.next();
									if(I!=null)
										from.add(I);
								}
							}
						}
						else
						{
							final List<Object> oldFrom=flattenMQLObjectList(from);
							from.clear();
							for(final Object o : oldFrom)
							{
								if(o instanceof Area)
								{
									final ShoppingLibrary shopper=CMLib.coffeeShops();
									for(final Enumeration<Room> r=((Area)o).getFilledCompleteMap();r.hasMoreElements();)
									{
										for(final Environmental E2 : shopper.getAllShopkeepers(r.nextElement(), null))
										{
											final ShopKeeper SK = shopper.getShopKeeper(E2);
											if(SK != null)
											{
												for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
												{
													final Environmental I=i.next();
													if(I!=null)
														from.add(I);
												}
											}
										}
									}
								}
								else
								if(o instanceof MOB)
								{
									final ShopKeeper SK = CMLib.coffeeShops().getShopKeeper((MOB)o);
									if(SK != null)
									{
										for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
										{
											final Environmental I=i.next();
											if(I!=null)
												from.add(I);
										}
									}
								}
								else
								if(o instanceof Room)
								{
									final ShoppingLibrary shopper=CMLib.coffeeShops();
									for(final Environmental E2 : shopper.getAllShopkeepers((Room)o, null))
									{
										final ShopKeeper SK = shopper.getShopKeeper(E2);
										if(SK != null)
										{
											for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
											{
												final Environmental I=i.next();
												if(I!=null)
													from.add(I);
											}
										}
									}
								}
								else
								if(o instanceof Item)
								{
									final ShopKeeper SK = CMLib.coffeeShops().getShopKeeper((Item)o);
									if(SK != null)
									{
										for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
										{
											final Environmental I=i.next();
											if(I!=null)
												from.add(I);
										}
									}
								}
								else
									throw new MQLException("Unknown sub-from "+f+" on "+o.toString()+" in "+mql);
							}
						}
					}
					break;
				case OWNER:
					{
						if(from.size()==0)
						{
							final Environmental oE=(E instanceof Item) ? (Environmental)E : null;
							if(oE==null)
								throw new MQLException("Unknown sub-from "+f+" on "+(""+E)+" in "+mql);
							else
							if(((Item)E).owner()!=null)
								from.add(((Item)E).owner());
						}
						else
						{
							if((from.size()==0)
							&&((E instanceof Area)||(E instanceof Room)||(E instanceof Item)))
								from.add(E);
							final List<Object> oldFrom=flattenMQLObjectList(from);
							from.clear();
							for(final Object o : oldFrom)
							{
								if(o instanceof Area)
								{
									for(final Enumeration<Room> r=((Area)o).getFilledCompleteMap();r.hasMoreElements();)
									{
										for(final Enumeration<MOB> m=r.nextElement().inhabitants();m.hasMoreElements();)
										{
											final MOB M=m.nextElement();
											for(final Enumeration<Item> i=M.items();i.hasMoreElements();)
											{
												final Item I=i.nextElement();
												if((I!=null)
												&&(I.owner()!=null)
												&&(!from.contains(I.owner())))
													from.add(I.owner());
											}
										}
									}
								}
								else
								if(o instanceof MOB)
								{
									for(final Enumeration<Item> i=((MOB)o).items();i.hasMoreElements();)
									{
										final Item I=i.nextElement();
										if((I!=null)
										&&(I.owner()!=null)
										&&(!from.contains(I.owner())))
											from.add(I.owner());
									}
								}
								else
								if(o instanceof Room)
								{
									for(final Enumeration<MOB> m=((Room)o).inhabitants();m.hasMoreElements();)
									{
										final MOB M=m.nextElement();
										for(final Enumeration<Item> i=M.items();i.hasMoreElements();)
										{
											final Item I=i.nextElement();
											if((I!=null)
											&&(I.owner()!=null)
											&&(!from.contains(I.owner())))
												from.add(I.owner());
										}
									}
								}
								else
								if(o instanceof Item)
								{
									if(((Item)o).amBeingWornProperly())
										from.add(o);
								}
								else
									throw new MQLException("Unknown sub-from "+f+" on "+o.toString()+" in "+mql);
							}
						}
					}
					break;
				case RESOURCES:
					{
						final List<Object> oldFrom=new ArrayList<Object>();
						if(from.size()>0)
							oldFrom.addAll(flattenMQLObjectList(from));
						else
						{
							final Object oE;
							if((E instanceof MOB)
							||(E instanceof Item)
							||(E instanceof Room)
							||(E instanceof Area))
								oE=E;
							else
								throw new MQLException("Unknown sub-from "+f+" on "+(""+E)+" in "+mql);
							oldFrom.add(oE);
						}
						from.clear();
						for(final Object o : oldFrom)
						{
							if((o instanceof Area)
							||(o instanceof Room))
							{
								final Enumeration<Room> r;
								if(o instanceof Area)
									r=((Area)o).getFilledCompleteMap();
								else
									r=new XVector<Room>((Room)o).elements();
								for(;r.hasMoreElements();)
								{
									final Room R=r.nextElement();
									final int resource=R.myResource()&RawMaterial.RESOURCE_MASK;
									if(RawMaterial.CODES.IS_VALID(resource))
									{
										final Item I=CMLib.materials().makeItemResource(resource);
										CMLib.threads().deleteAllTicks(I);
										I.setSavable(false);
										I.setDatabaseID("DELETE");
										from.add(I);
									}
								}
							}
							else
							if(o instanceof MOB)
							{
								final Race R=((MOB)o).charStats().getMyRace();
								for(final Item I : R.myResources())
								{
									final Item I2=(Item)I.copyOf();
									CMLib.threads().deleteAllTicks(I2);
									I2.setSavable(false);
									I2.setDatabaseID("DELETE");
									from.add(I2);
								}
							}
							else
							if(o instanceof Item)
							{
								final int resource=((Item)o).material()&RawMaterial.RESOURCE_MASK;
								if(RawMaterial.CODES.IS_VALID(resource))
								{
									final Item I=CMLib.materials().makeItemResource(resource);
									CMLib.threads().deleteAllTicks(I);
									I.setSavable(false);
									I.setDatabaseID("DELETE");
									from.add(I);
								}
							}
							else
								throw new MQLException("Unknown sub-from "+f+" on "+o.toString()+" in "+mql);
						}
					}
					break;
				case FACTIONS:
					{
						final List<Object> oldFrom=new ArrayList<Object>();
						if(from.size()>0)
							oldFrom.addAll(flattenMQLObjectList(from));
						else
						{
							final Object oE;
							if(E instanceof MOB)
								oE=E;
							else
								throw new MQLException("Unknown sub-from "+f+" on "+(""+E)+" in "+mql);
							oldFrom.add(oE);
						}
						from.clear();
						for(final Object o : oldFrom)
						{
							if(o instanceof MOB)
							{
								for(final Enumeration<String> fenum=((MOB)o).factions();fenum.hasMoreElements();)
								{
									final String fstr=fenum.nextElement();
									final int val=((MOB)o).fetchFaction(fstr);
									final Map<String,Object> m=new TreeMap<String,Object>();
									m.put("ID", fstr);
									m.put("VALUE", ""+val);
									from.add(m);

								}
							}
							else
								throw new MQLException("Unknown sub-from "+f+" on "+o.toString()+" in "+mql);
						}
					}
					break;
				case PROPERTIES:
					{
						if((from.size()==0)
						&&((E instanceof Affectable)||(E instanceof Ability)))
							from.add(E);
						if(from.size()==0)
							from.addAll(new XVector<Ability>(CMClass.abilities()));
						else
						{
							final List<Object> oldFrom=flattenMQLObjectList(from);
							from.clear();
							for(final Object o : oldFrom)
							{
								if(o instanceof Affectable)
								{
									for(final Enumeration<Ability> r=((Affectable)o).effects();r.hasMoreElements();)
									{
										final Ability A=r.nextElement();
										if((A!=null)
										&&(!A.canBeUninvoked()))
											from.add(A);
									}
								}
								else
								if(o instanceof Ability)
								{
									if(!((Ability)o).canBeUninvoked())
										from.add(o);
								}
								else
									throw new MQLException("Unknown sub-from "+f+" on "+o.toString()+" in "+mql);
							}
						}
					}
					break;
				case EFFECTS:
					{
						if(from.size()==0)
							from.addAll(new XVector<Ability>(CMClass.abilities()));
						else
						{
							final List<Object> oldFrom=flattenMQLObjectList(from);
							from.clear();
							for(final Object o : oldFrom)
							{
								if(o instanceof Affectable)
								{
									for(final Enumeration<Ability> r=((Affectable)o).effects();r.hasMoreElements();)
									{
										final Ability A=r.nextElement();
										if(A!=null)
										from.add(A);
									}
								}
								else
								if(o instanceof Ability)
									from.add(o);
								else
									throw new MQLException("Unknown sub-from "+f+" on "+o.toString()+" in "+mql);
							}
						}
					}
					break;
				case BEHAVIORS:
					{
						if((from.size()==0)
						&&((E instanceof Behavable)))
							from.add(E);
						if(from.size()==0)
							from.addAll(new XVector<Behavior>(CMClass.behaviors()));
						else
						{
							final List<Object> oldFrom=flattenMQLObjectList(from);
							from.clear();
							for(final Object o : oldFrom)
							{
								if(o instanceof Behavable)
								{
									for(final Enumeration<Behavior> r=((Behavable)o).behaviors();r.hasMoreElements();)
									{
										final Behavior A=r.nextElement();
										if(A!=null)
											from.add(A);
									}
								}
								else
								if(o instanceof Behavior)
									from.add(o);
								else
									throw new MQLException("Unknown sub-from "+f+" on "+o.toString()+" in "+mql);
							}
						}
					}
					break;
				case RACES:
					{
						if((from.size()==0)
						&&((E instanceof MOB)||(E instanceof Race)))
							from.add(E);
						if(from.size()==0)
							from.addAll(new XVector<Race>(CMClass.races()));
						else
						{
							final List<Object> oldFrom=flattenMQLObjectList(from);
							from.clear();
							for(final Object o : oldFrom)
							{
								if(o instanceof MOB)
									from.add(((MOB)o).charStats().getMyRace());
								else
								if(o instanceof Race)
									from.add(o);
								else
									throw new MQLException("Unknown sub-from "+f+" on "+o.toString()+" in "+mql);
							}
						}
					}
					break;
				case ABILITIES:
					{
						if((from.size()==0)
						&&((E instanceof MOB)||(E instanceof Ability)))
							from.add(E);
						if(from.size()==0)
							from.addAll(new XVector<Ability>(CMClass.abilities()));
						else
						{
							final List<Object> oldFrom=flattenMQLObjectList(from);
							from.clear();
							for(final Object o : oldFrom)
							{
								if(o instanceof MOB)
								{
									for(final Enumeration<Ability> r=((MOB)o).allAbilities();r.hasMoreElements();)
										from.add(r.nextElement());
								}
								else
								if(o instanceof Ability)
									from.add(o);
								else
										throw new MQLException("Unknown sub-from "+f+" on "+o.toString()+" in "+mql);
							}
						}
					}
					break;
				default:
					throw new MQLException("Unknown sub-from "+f+" in "+mql);
				}
			}
			else
			if(f.startsWith("/")
			|| f.startsWith("::")
			|| f.startsWith("//"))
			{
				final CMFile F=new CMFile(f,null);
				if(!F.exists())
					throw new MQLException("CMFile "+f+" not found in "+mql);
				if(F.isDirectory())
				{
					for(final CMFile F2 : F.listFiles())
					{
						if(!F2.isDirectory())
							from.addAll(this.parseMQLCMFile(F, mql));
					}
				}
				else
					from.addAll(this.parseMQLCMFile(F, mql));
			}
			else
			if(f.startsWith("$"))
			{
				final Object val = defined.get(f.substring(1));
				if(val == null)
					throw new MQLException("Unknown from clause selector '"+f+"' in "+mql);
				if(val instanceof XMLTag)
				{
					final XMLTag tag=(XMLTag)val;
					try
					{
						if(tag.tag().equalsIgnoreCase("STRING"))
							from.add(findString(E,ignoreStats,defPrefix,"STRING",(XMLTag)val,defined));
						else
						{
							final Object o =findObject(E,ignoreStats,defPrefix,"OBJECT",tag,defined);
							if(o instanceof List)
							{
								@SuppressWarnings({ "unchecked" })
								final List<Object> l=(List<Object>)o;
								from.addAll(l);
							}
							else
								from.add(o);
						}
					}
					catch(final CMException e)
					{
						throw new MQLException(e.getMessage(),e);
					}
				}
				else
				if(val instanceof List)
				{
					@SuppressWarnings("unchecked")
					final List<Object> l=(List<Object>)val;
					from.addAll(l);
				}
				else
					from.add(val);
			}
			else
			{
				final Object asDefined = defined.get(f);
				if(asDefined == null)
				{
					if(literalsOK)
						from.add(f);
					else
						throw new MQLException("Unknown from clause selector '"+f+"' in "+mql);
				}
				else
				if(asDefined instanceof List)
					from.addAll((from));
				else
					from.add(asDefined);
			}
		}
		return from;
	}

	protected Object getSimpleMQLValue(final String valueName, final Object from)
	{
		if(valueName.equals(".")||valueName.equals("*"))
			return from;
		if(from instanceof Map)
		{
			@SuppressWarnings({ "unchecked" })
			final Map<String,Object> m=(Map<String,Object>)from;
			if(m.containsKey(valueName))
				return m.get(valueName);
			for(final String key : m.keySet())
			{
				final Object o=m.get(key);
				if(o instanceof CMObject)
					return getSimpleMQLValue(valueName,o);
			}
		}
		if(from instanceof List)
		{
			@SuppressWarnings("rawtypes")
			final List l=(List)from;
			if(l.size()>0)
				return getSimpleMQLValue(valueName,l.get(0));
		}
		if(from instanceof MOB)
		{
			if(CMLib.coffeeMaker().isAnyGenStat((Physical)from,valueName))
				return CMLib.coffeeMaker().getAnyGenStat((Physical)from,valueName);
		}
		else
		if(from instanceof Item)
		{
			if(CMLib.coffeeMaker().isAnyGenStat((Physical)from,valueName))
				return CMLib.coffeeMaker().getAnyGenStat((Physical)from,valueName);
		}
		else
		if(from instanceof Room)
		{
			if(CMLib.coffeeMaker().isAnyGenStat((Physical)from,valueName))
				return CMLib.coffeeMaker().getAnyGenStat((Physical)from,valueName);
		}
		else
		if(from instanceof Area)
		{
			if(CMLib.coffeeMaker().isAnyGenStat((Physical)from,valueName))
				return CMLib.coffeeMaker().getAnyGenStat((Physical)from,valueName);
		}
		else
		if(from instanceof Modifiable)
		{
			if(((Modifiable)from).isStat(valueName))
			{
				final String str=((Modifiable)from).getStat(valueName);
				return str;
			}
			else
			if((from instanceof Ability)||(from instanceof Behavior))
			{
				final String str=((Modifiable)from).getStat(valueName);
				if(str.length()>0)
					return str;
			}
		}
		return null;
	}

	protected Object getFinalMQLValue(final String[] strpath, final List<Object> allFrom, final Object from, final Map<String,Object> cache, final String mql,
			final Modifiable E, final List<String> ignoreStats, final String defPrefix, final XMLTag piece, final Map<String,Object> defined, final boolean literalsOK)
					throws MQLException,PostProcessException
	{
		if((strpath.length==1)
		&&(strpath[0].startsWith("SELECT:")))
		{
			Modifiable selectFrom=(from instanceof Modifiable)?(Modifiable)from:null;
			if(from instanceof Map)
			{
				@SuppressWarnings("rawtypes")
				final Map m=(Map)from;
				if((m.size()>0)
				&&(m.values().iterator().next() instanceof Modifiable))
					selectFrom=(Modifiable)m.values().iterator().next();
			}
			return doMQLSelectObjs(selectFrom,ignoreStats,defPrefix,strpath[0],piece,defined);
		}
		Object finalO=null;
		try
		{
			int index=0;
			for(final String str : strpath)
			{
				index++;
				if(str.length()==0)
				{
				}
				else
				switch(str.charAt(0))
				{
				case '*':
				case '.':
					if(finalO==null)
						finalO=from;
					else
						finalO=str.trim();
					break;
				case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
				case '-':
					finalO=str.trim();
					break;
				case '\"':
					if((str.endsWith("\""))&&(str.length()>1))
						finalO=this.strFilter(E, ignoreStats, defPrefix, CMStrings.replaceAll(str.substring(1,str.length()-1),"\\\"","\""), piece, defined);
					else
						finalO=str.trim();
					break;
				case '\'':
					if((str.endsWith("\'"))&&(str.length()>1))
						finalO=this.strFilter(E, ignoreStats, defPrefix, CMStrings.replaceAll(str.substring(1,str.length()-1),"\\'","'"), piece, defined);
					else
						finalO=str.trim();
					break;
				case '`':
					if((str.endsWith("`"))&&(str.length()>1))
						finalO=this.strFilter(E, ignoreStats, defPrefix, CMStrings.replaceAll(str.substring(1,str.length()-1),"\\`","`"), piece, defined);
					else
						finalO=str.trim();
					break;
				case '$':
					{
						if(str.length()==1)
							break;
						final String key;
						if((str.charAt(1)=='{')&&(str.endsWith("}")))
						{
							final int x=str.lastIndexOf(':');
							if(x<0)
								key=str.substring(2,str.length()-1);
							else
								key=str.substring(x+1,str.length()-1);
						}
						else
							key=str.substring(1);
						Object val = defined.get(key);
						if(val instanceof XMLTag)
						{
							Modifiable chkO=E;
							if(finalO instanceof Modifiable)
								chkO=(Modifiable)finalO;
							else
							if(from instanceof Modifiable)
								chkO=(Modifiable)from;
							final XMLTag tag=(XMLTag)val;
							if(tag.tag().equalsIgnoreCase("STRING"))
							{
								final String s=findString(chkO,ignoreStats,defPrefix,"STRING",(XMLTag)val,defined);
								if(s.toUpperCase().trim().startsWith("SELECT:"))
									val=doMQLSelectObjects(chkO, s);
								else
									val=s;
							}
							else
							{
								final Object o =findObject(E,ignoreStats,defPrefix,"OBJECT",tag,defined);
								if(o instanceof Tickable)
									CMLib.threads().deleteAllTicks((Tickable)o);
								if(o instanceof List)
								{
									@SuppressWarnings({ "unchecked" })
									final List<Object> l=(List<Object>)o;
									for(final Object o2 : l)
									{
										if(o2 instanceof Tickable)
											CMLib.threads().deleteAllTicks((Tickable)o2);
									}
								}
								val=o;
							}
						}
						if(val == null)
							throw new MQLException("Unknown variable '$"+str+"' in str '"+str+"' in '"+mql+"'",new CMException("$"+str));
						finalO=val;
					}
					break;
				case 'C':
					if(str.startsWith("COUNT"))
					{
						Object chkO=finalO;
						if(chkO==null)
							chkO=from;
						if(chkO==null)
							throw new MQLException("Can not count instances of null in '"+strpath+"' in '"+mql+"'");
						else
						if(chkO instanceof List)
						{
							@SuppressWarnings("rawtypes")
							final List l=(List)chkO;
							finalO=""+l.size();
						}
						else
						if((chkO instanceof String)
						||(chkO instanceof Environmental))
						{
							if(index>1)
							{
								final String[] newWhat = Arrays.copyOfRange(strpath, 0, index-1);
								final StringBuilder newWhatStr=new StringBuilder("");
								for(final String s : newWhat)
									newWhatStr.append(s+"_");
								int count=0;
								final String cacheKey = "COUNT_CACHE_"+newWhatStr.toString()+"FROM_"+allFrom.hashCode();
								@SuppressWarnings("unchecked")
								Map<Object,Object> cachedValues =  (Map<Object,Object>)cache.get(cacheKey);
								if(cachedValues == null)
								{
									cachedValues = new HashMap<Object,Object>();
									for(final Object o : allFrom)
									{
										if(o == null)
											cachedValues.put(o, chkO);
										else
											cachedValues.put(o, getFinalMQLValue(newWhat, allFrom, o, cache, mql, E, ignoreStats, defPrefix, piece, defined, literalsOK));
									}
									cache.put(cacheKey, cachedValues);
								}
								for(final Object o : allFrom)
								{
									if(o==from)
										count++;
									else
									if(chkO.equals(cachedValues.get(o)))
										count++;
								}
								finalO=""+count;
							}
							else
								finalO=""+allFrom.size();
						}
						else
							finalO="1";
						break;
					}
					//$FALL-THROUGH$
				default:
					{
						final Object fromO=(finalO==null)?((from==null)?E:from):finalO;
						Object newObj=getSimpleMQLValue(str,fromO);
						if(newObj == null)
						{
							if(fromO instanceof Modifiable)
								newObj=parseMQLFrom(new String[] {str}, mql, (Modifiable)fromO, ignoreStats, defPrefix, piece, defined, literalsOK);
							if(newObj == null)
								throw new MQLException("Unknown variable '"+str+"' on '"+fromO+"' in '"+mql+"'",new CMException("$"+str));
						}
						finalO=newObj;
					}
					break;
				}
			}
		}
		catch(final CMException e)
		{
			if(e instanceof MQLException)
				throw (MQLException)e;
			else
				throw new MQLException("MQL failure on $"+strpath,e);
		}
		return finalO;
	}

	protected boolean doMQLComparison(final Object lhso, MQLClause.WhereComparator comp, final Object rhso, final List<Object> allFrom, final Object from,
			final Modifiable E, final List<String> ignoreStats, final String defPrefix, final XMLTag piece, final Map<String,Object> defined) throws MQLException,PostProcessException
	{
		final String lhstr=(lhso==null)?"":lhso.toString();
		final String rhstr=(rhso==null)?"":rhso.toString();
		if(lhso instanceof List)
		{
			if(rhso instanceof List)
			{
				@SuppressWarnings("rawtypes")
				final List llhso=(List)lhso;
				@SuppressWarnings("rawtypes")
				final List lrhso=(List)rhso;
				switch(comp)
				{
				case GT:
					return llhso.size()>lrhso.size();
				case LT:
					return llhso.size()<lrhso.size();
				case GTEQ:
					if(llhso.size()>lrhso.size())
						return true;
					comp=MQLClause.WhereComparator.EQ;
					break;
				case LTEQ:
					if(llhso.size()<lrhso.size())
						return true;
					comp=MQLClause.WhereComparator.EQ;
					break;
				default:
					// see below;
					break;
				}
				switch(comp)
				{
				case NEQ:
				case EQ:
				{
					if(llhso.size()!=lrhso.size())
						return (comp==MQLClause.WhereComparator.NEQ);
					boolean allSame=true;
					for(final Object o1 : llhso)
					{
						for(final Object o2 : lrhso)
							allSame = allSame || doMQLComparison(o1, MQLClause.WhereComparator.EQ, o2, allFrom, from,E,ignoreStats,defPrefix,piece,defined);
					}
					if(allSame)
						return (comp==MQLClause.WhereComparator.EQ);
					return (comp==MQLClause.WhereComparator.NEQ);
				}
				case LIKE:
				case NOTLIKE:
				{
					if(llhso.size()!=lrhso.size())
						return (comp==MQLClause.WhereComparator.NOTLIKE);
					boolean allSame=true;
					for(final Object o1 : llhso)
					{
						for(final Object o2 : lrhso)
							allSame = allSame || doMQLComparison(o1, MQLClause.WhereComparator.LIKE, o2, allFrom, from,E,ignoreStats,defPrefix,piece,defined);
					}
					if(allSame)
						return (comp==MQLClause.WhereComparator.LIKE);
					return (comp==MQLClause.WhereComparator.NOTLIKE);
				}
				case IN:
				case NOTIN:
				{
					boolean allIn=true;
					for(final Object o1 : llhso)
						allIn = allIn || doMQLComparison(o1, MQLClause.WhereComparator.IN, lrhso, allFrom, from,E,ignoreStats,defPrefix,piece,defined);
					if(allIn)
						return (comp==MQLClause.WhereComparator.IN);
					return (comp==MQLClause.WhereComparator.NOTIN);
				}
				default:
					// see above:
					break;
				}
			}
			else
			{
				@SuppressWarnings("unchecked")
				final List<Object> l=(List<Object>)lhso;
				boolean allIn=true;
				for(final Object o1 : l)
					allIn = allIn && doMQLComparison(o1, comp, rhso, allFrom, from,E,ignoreStats,defPrefix,piece,defined);
				return allIn;
			}
		}
		if((comp != MQLClause.WhereComparator.IN)
		&&(comp != MQLClause.WhereComparator.NOTIN))
		{
			if(rhso instanceof List)
			{
				@SuppressWarnings("rawtypes")
				final List rL=(List)rhso;
				if(rL.size()>1)
				{
					return comp==MQLClause.WhereComparator.NEQ
					|| comp==MQLClause.WhereComparator.NOTLIKE
					|| comp==MQLClause.WhereComparator.LT
					|| comp==MQLClause.WhereComparator.LTEQ;
				}
				return doMQLComparison(lhso, comp, rL.get(0), allFrom, from,E,ignoreStats,defPrefix,piece,defined);
			}
			if(lhso instanceof Map)
			{
				@SuppressWarnings("rawtypes")
				final Map mlhso=(Map)lhso;
				boolean allSame=true;
				for(final Object o1 : mlhso.keySet())
					allSame = allSame && doMQLComparison(mlhso.get(o1), comp, rhso, allFrom, from,E,ignoreStats,defPrefix,piece,defined);
				return allSame;
			}
			else
			if(rhso instanceof Map)
			{
				@SuppressWarnings("rawtypes")
				final Map mrhso=(Map)rhso;
				boolean allSame=true;
				for(final Object o1 : mrhso.keySet())
					allSame = allSame && doMQLComparison(lhso, comp, mrhso.get(o1), allFrom, from,E,ignoreStats,defPrefix,piece,defined);
				return allSame;
			}
		}
		switch(comp)
		{
		case NEQ:
		case EQ:
		{
			final boolean eq=(comp==MQLClause.WhereComparator.EQ);
			if(CMath.isNumber(lhstr) && CMath.isNumber(rhstr))
				return (CMath.s_double(lhstr) == CMath.s_double(rhstr)) == eq;
			if(lhso instanceof String)
			{
				if(rhso instanceof String)
					return lhstr.equalsIgnoreCase(rhstr) == eq;
				if(rhso instanceof Environmental)
					return ((Environmental)rhso).Name().equalsIgnoreCase(lhstr) == eq;
				else
					return lhstr.equalsIgnoreCase(rhstr) == eq;
			}
			else
			if(rhso instanceof String)
			{
				if(lhso instanceof Environmental)
					return ((Environmental)lhso).Name().equalsIgnoreCase(rhstr) == eq;
				else
					return rhstr.equalsIgnoreCase(lhstr) == eq;
			}
			if(lhso instanceof Environmental)
			{
				if(rhso instanceof Environmental)
					return ((Environmental)lhso).sameAs((Environmental)rhso) == eq;
				throw new MQLException("'"+rhstr+"' can't be compared to '"+lhstr+"'");
			}
			else
			if(rhso instanceof Environmental)
				throw new MQLException("'"+rhstr+"' can't be compared to '"+lhstr+"'");
			if((lhso != null)&&(rhso != null))
				return (rhso.equals(rhso)) == eq;
			else
				return (lhso == rhso) == eq;
		}
		case GT:
			if(CMath.isNumber(lhstr) && CMath.isNumber(rhstr))
				return CMath.s_double(lhstr) > CMath.s_double(rhstr);
			if((lhstr instanceof String)||(rhstr instanceof String))
				return lhstr.compareToIgnoreCase(rhstr) > 0;
			return false; // objects can't be > than each other
		case GTEQ:
			if(CMath.isNumber(lhstr) && CMath.isNumber(rhstr))
				return CMath.s_double(lhstr) >= CMath.s_double(rhstr);
			if((lhstr instanceof String)||(rhstr instanceof String))
				return lhstr.compareToIgnoreCase(rhstr) >= 0;
			return doMQLComparison(lhso, MQLClause.WhereComparator.EQ, rhso, allFrom, from,E,ignoreStats,defPrefix,piece,defined);
		case NOTIN:
		case IN:
			if(rhso instanceof List)
			{
				@SuppressWarnings("rawtypes")
				final List lrhso=(List)rhso;
				for(final Object o2 : lrhso)
				{
					if(doMQLComparison(lhso, MQLClause.WhereComparator.EQ, o2, allFrom, from,E,ignoreStats,defPrefix,piece,defined))
						return comp==(MQLClause.WhereComparator.IN);
				}
				return comp==(MQLClause.WhereComparator.NOTIN);
			}
			if(rhso instanceof String)
			{
				if(lhso instanceof String)
					return (rhstr.toUpperCase().indexOf(lhstr.toUpperCase()) >= 0) == (comp==MQLClause.WhereComparator.IN);
				else
					throw new MQLException("'"+lhstr+"' can't be in a string");
			}
			if(rhso instanceof Map)
			{
				@SuppressWarnings("rawtypes")
				final Map m=(Map)rhso;
				if(m.containsKey(lhstr.toUpperCase()))
					return (comp==MQLClause.WhereComparator.IN);
				for(final Object key : m.keySet())
				{
					if(doMQLComparison(lhso, MQLClause.WhereComparator.EQ, m.get(key), allFrom, from,E,ignoreStats,defPrefix,piece,defined))
						return comp==(MQLClause.WhereComparator.IN);
				}
			}
			if(rhso instanceof Environmental)
			{
				return doMQLComparison(lhso, MQLClause.WhereComparator.EQ, rhso, allFrom, from,E,ignoreStats,defPrefix,piece,defined)
					== (comp==(MQLClause.WhereComparator.IN));
			}
			throw new MQLException("'"+rhstr+"' can't contain anything");
		case NOTLIKE:
		case LIKE:
			if(!(rhso instanceof String))
				throw new MQLException("Nothing can ever be LIKE '"+rhstr+"'");
			if(lhso instanceof Environmental)
			{
				return CMLib.masking().maskCheck(rhstr, (Environmental)lhso, true)
						== (comp==MQLClause.WhereComparator.LIKE);
			}
			if(lhso instanceof Map)
			{
				@SuppressWarnings("rawtypes")
				final Map m=(Map)lhso;
				Object o=m.get("CLASS");
				if(!(o instanceof String))
					throw new MQLException("'"+lhstr+"' can ever be LIKE anything.");
				o=CMClass.getObjectOrPrototype((String)o);
				if(o == null)
					throw new MQLException("'"+lhstr+"' can ever be LIKE anything but nothing.");
				if(!(o instanceof Modifiable))
					throw new MQLException("'"+lhstr+"' can ever be LIKE anything modifiable.");
				if(!(o instanceof Environmental))
					throw new MQLException("'"+lhstr+"' can ever be LIKE anything envronmental.");
				final Modifiable mo=(Modifiable)((Modifiable)o).newInstance();
				for(final Object key : m.keySet())
				{
					if((key instanceof String)
					&&(m.get(key) instanceof String))
						mo.setStat((String)key, (String)m.get(key));
				}
				if(mo instanceof Environmental)
				{
					final boolean rv = CMLib.masking().maskCheck(rhstr, (Environmental)lhso, true)
							== (comp==MQLClause.WhereComparator.LIKE);
					((Environmental)mo).destroy();
					return rv;
				}
			}
			else
				throw new MQLException("'"+lhstr+"' can ever be LIKE anything at all.");
			break;
		case LT:
			if(CMath.isNumber(lhstr) && CMath.isNumber(rhstr))
				return CMath.s_double(lhstr) < CMath.s_double(rhstr);
			if((lhstr instanceof String)||(rhstr instanceof String))
				return lhstr.compareToIgnoreCase(rhstr) < 0;
			return false; // objects can't be < than each other
		case LTEQ:
			if(CMath.isNumber(lhstr) && CMath.isNumber(rhstr))
				return CMath.s_double(lhstr) <= CMath.s_double(rhstr);
			if((lhstr instanceof String)||(rhstr instanceof String))
				return lhstr.compareToIgnoreCase(rhstr) <= 0;
			return doMQLComparison(lhso, MQLClause.WhereComparator.EQ, rhso, allFrom, from,E,ignoreStats,defPrefix,piece,defined);
		default:
			break;

		}
		return true;
	}

	protected boolean doMQLComparison(final MQLClause.WhereComp comp, final List<Object> allFrom, final Object from, final Map<String, Object> cache, final String mql,
			final Modifiable E, final List<String> ignoreStats, final String defPrefix, final XMLTag piece, final Map<String,Object> defined) throws MQLException,PostProcessException
	{
		final Object lhso=getFinalMQLValue(comp.lhs, allFrom, from,cache,mql,E,ignoreStats,defPrefix,piece,defined,true);
		final Object rhso=getFinalMQLValue(comp.rhs, allFrom, from,cache,mql,E,ignoreStats,defPrefix,piece,defined,true);
		return doMQLComparison(lhso, comp.comp, rhso, allFrom, from,E,ignoreStats,defPrefix,piece,defined);
	}

	protected boolean doMQLWhereClauseFilter(final MQLClause.WhereClause whereClause, final List<Object> allFrom, final Object from, final Map<String, Object> cache, final String mql,
			final Modifiable E, final List<String> ignoreStats, final String defPrefix, final XMLTag piece, final Map<String,Object> defined) throws MQLException,PostProcessException
	{
		MQLClause.WhereConnector lastConn=null;
		MQLClause.WhereClause clause=whereClause;
		boolean result=true;
		while(clause != null)
		{
			boolean thisResult;
			if(clause.parent != null)
			{
				if(clause.lhs != null)
					throw new MQLException("Parent & lhs error ");
				thisResult=doMQLWhereClauseFilter(clause.parent,allFrom,from,cache,mql,E,ignoreStats,defPrefix,piece,defined);
			}
			else
			if(clause.lhs != null)
				thisResult=doMQLComparison(clause.lhs, allFrom, from,cache,mql,E,ignoreStats,defPrefix,piece,defined);
			else
			if(clause.next != null)
			{
				if(clause.conn != null)
					lastConn=clause.conn;
				clause=clause.next;
				continue;
			}
			else
				return result;
			if(lastConn != null)
			{
				switch(lastConn)
				{
				default:
				case ENDCLAUSE:
				case AND:
					result=result&&thisResult;
					break;
				case OR:
					result=result||thisResult;
					break;
				}
			}
			else
				result=result&&thisResult;
			if(clause.conn != null)
			{
				lastConn=clause.conn;
				if(clause.next != null)
				{
					switch(lastConn)
					{
					case AND:
						if(!result)
							return false;
						break;
					case OR:
						if(result)
							return true;
						break;
					default:
					case ENDCLAUSE:
						break;
					}
				}
			}
			clause=clause.next;
		}
		return result;
	}

	protected List<Map<String,Object>> doSubObjSelect(final Modifiable E, final List<String> ignoreStats, final String defPrefix,
													  final MQLClause clause, final String mql, final XMLTag piece, final Map<String,Object> defined)
															  throws MQLException,PostProcessException
	{
		final List<Map<String,Object>> results=new ArrayList<Map<String,Object>>();
		// first estalish the from object6
		if(clause.froms.size()==0)
			throw new MQLException("No FROM clause in "+clause.mql);
		// froms can have any environmental, or tags
		final List<Object> froms=new LinkedList<Object>();
		for(final String[] from : clause.froms)
		{
			if((from.length==0)||(from[0].length()==0))
				throw new MQLException("Empty FROM clause in "+clause.mql);
			froms.addAll(parseMQLFrom(from, clause.mql, E, ignoreStats, defPrefix, piece, defined,false));
		}
		boolean aggregate=false;
		for(int i=0;i<clause.what.size();i++)
		{
			if(clause.what.get(i).aggregator != null)
			{
				aggregate=true;
				break;
			}
		}
		final Map<String,Object> cache=new HashMap<String,Object>();
		for(final Object o : froms)
		{
			if(doMQLWhereClauseFilter(clause.wheres, froms, o,cache,mql,E,ignoreStats,defPrefix,piece,defined))
			{
				final Map<String,Object> m=new TreeMap<String,Object>();
				for(final MQLClause.WhatBit bit : clause.what)
				{
					final Object o1 = this.getFinalMQLValue(bit.what(), froms, o, cache, mql,E, ignoreStats, defPrefix, piece, defined,true);
					if(o1 instanceof Map)
					{
						@SuppressWarnings("unchecked")
						final Map<String,Object> m2=(Map<String, Object>)o1;
						m.putAll(m2);
					}
					else
					if(o1 instanceof List)
					{
						@SuppressWarnings("rawtypes")
						final Iterator iter = ((List)o1).iterator();
						if(iter.hasNext())
						{
							final Object obj = iter.next();
							if(obj instanceof Map)
							{
								@SuppressWarnings("unchecked")
								final List<Map<String,Object>> list=(List<Map<String,Object>>)o1;
								for(final Map<String,Object> m2 : list)
									m.putAll(m2);
							}
							else
								m.put(".", o1);
						}
					}
					else
						m.put(bit.as(), o1);
				}
				results.add(m);
			}
		}
		if(aggregate)
		{
			for(final MQLClause.WhatBit W : clause.what)
			{
				if(W.aggregator == null)
					continue;

				final String whatName=W.as();
				switch(W.aggregator)
				{
				case COUNT:
				{
					final String sz=""+results.size();
					results.clear();
					final Map<String,Object> oneRow=new TreeMap<String,Object>();
					results.add(oneRow);
					oneRow.put(whatName, sz);
					break;
				}
				case FIRST:
				{
					if(results.size()>0)
					{
						final Map<String,Object> first=results.get(0);
						results.clear();
						results.add(first);
					}
					break;
				}
				case ANY:
				{
					if(results.size()>0)
					{
						final Map<String,Object> any=results.get(CMLib.dice().roll(1, results.size(), -1));
						results.clear();
						results.add(any);
					}
					break;
				}
				case UNIQUE:
				{
					final List<Map<String,Object>> nonresults=new ArrayList<Map<String,Object>>(results.size());
					nonresults.addAll(results);
					results.clear();
					final TreeSet<Object> done=new TreeSet<Object>(objComparator);
					for(final Map<String,Object> r : nonresults)
					{
						if(r.containsKey(whatName)
						&&(r.get(whatName)!=null)
						&&(!done.contains(r.get(whatName))))
						{
							done.add(r.get(whatName));
							results.add(r);
						}
					}
					break;
				}
				case MEDIAN:
				{
					final List<Object> allValues=new ArrayList<Object>(results.size());
					for(final Map<String,Object> r : results)
					{
						if(r.containsKey(whatName)&&(r.get(whatName)!=null))
							allValues.add(r.get(whatName));
					}
					Collections.sort(allValues,objComparator);
					if(allValues.size()>0)
					{
						results.clear();
						final Map<String,Object> oneRow=new TreeMap<String,Object>();
						results.add(oneRow);
						oneRow.put(W.as(), allValues.get((int)Math.round(Math.floor(CMath.div(allValues.size(), 2)))));
					}
					break;
				}
				case MEAN:
				{
					double totalValue=0.0;
					for(final Map<String,Object> r : results)
					{
						if(r.containsKey(whatName)&&(r.get(whatName)!=null))
							totalValue += CMath.s_double(r.get(whatName).toString());
					}
					if(results.size()>0)
					{
						final String mean=""+(totalValue/results.size());
						results.clear();
						final Map<String,Object> oneRow=new TreeMap<String,Object>();
						results.add(oneRow);
						oneRow.put(W.as(), mean);
					}
					break;
				}
				}
			}
		}
		return results;
	}

	protected String convertMQLObjectToString(final Object o1)
	{
		if(o1 instanceof List)
		{
			@SuppressWarnings("unchecked")
			final List<Object> oldL=(List<Object>)o1;
			final StringBuilder str=new StringBuilder("");
			for(final Object o : oldL)
				str.append(convertMQLObjectToString(o)).append(" ");
			return str.toString();
		}
		else
		if(o1 instanceof Map)
		{
			@SuppressWarnings("unchecked")
			final Map<String,Object> oldL=(Map<String,Object>)o1;
			final StringBuilder str=new StringBuilder("");
			for(final Object o : oldL.values())
				str.append(convertMQLObjectToString(o)).append(" ");
			return str.toString();
		}
		else
		if(o1 instanceof MOB)
			return CMLib.coffeeMaker().getMobXML((MOB)o1).toString();
		else
		if(o1 instanceof Item)
			return CMLib.coffeeMaker().getItemXML((Item)o1).toString();
		else
		if(o1 instanceof Ability)
			return ((Ability)o1).ID();
		else
		if(o1 instanceof Room)
			return  CMLib.map().getExtendedRoomID((Room)o1);
		else
		if(o1 instanceof Area)
			return ((Area)o1).Name();
		else
		if(o1 instanceof Behavior)
			return ((Behavior)o1).ID();
		else
		if(o1 != null)
			return  o1.toString();
		else
			return "";
	}

	protected List<Map<String,String>> doSubSelectStr(final Modifiable E, final List<String> ignoreStats, final String defPrefix, final MQLClause clause, final String mql,
													  final XMLTag piece, final Map<String,Object> defined) throws MQLException,PostProcessException
	{
		final List<Map<String,String>> results=new ArrayList<Map<String,String>>();
		final List<Map<String, Object>> objs=this.doSubObjSelect(E, ignoreStats, defPrefix, clause, mql, piece, defined);
		for(final Map<String,Object> o : objs)
		{
			final Map<String,String> n=new TreeMap<String,String>();
			results.add(n);
			for(final String key : o.keySet())
			{
				final Object o1=o.get(key);
				n.put(key, this.convertMQLObjectToString(o1));
			}
		}
		return results;
	}

	protected List<Map<String,String>> doMQLSelectStrs(final Modifiable E, final List<String> ignoreStats, final String defPrefix, final String str, final XMLTag piece, final Map<String,Object> defined) throws MQLException,PostProcessException
	{
		final int x=str.indexOf(':');
		if(x<0)
			throw new MQLException("Malformed mql: "+str);
		final String mqlbits=str.substring(x+1).toUpperCase();
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
			Log.debugOut("Starting MQL: "+CMStrings.deleteCRLFTAB(mqlbits) +" on "+((E==null)?"null":E.name()));
		final MQLClause clause = MQLClause.parseMQL(str, mqlbits);
		final List<Map<String,String>> results = this.doSubSelectStr(E, ignoreStats, defPrefix, clause, str, piece, defined);
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
			Log.debugOut("Finished MQL: "+results.size()+" results");
		return results;
	}

	protected List<Map<String,Object>> doMQLSelectObjs(final Modifiable E, final List<String> ignoreStats, final String defPrefix, final String str, final XMLTag piece, final Map<String,Object> defined) throws MQLException,PostProcessException
	{
		final int x=str.indexOf(':');
		if(x<0)
			throw new MQLException("Malformed mql: "+str);
		final String mqlbits=str.substring(x+1).toUpperCase();
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
			Log.debugOut("Starting MQL: "+CMStrings.deleteCRLFTAB(mqlbits)+" on "+((E==null)?"null":((E instanceof Room)?((Room)E).roomID():E.name())));

		final MQLClause clause = MQLClause.parseMQL(str, mqlbits);
		final List<Map<String,Object>> results = this.doSubObjSelect(E, ignoreStats, defPrefix, clause, str, piece, defined);
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.MUDPERCOLATOR))
		{
			if((results.size()==1)&&(results.get(0).size()==1))
				Log.debugOut("Finished MQL: "+results.size()+" results: "+results.get(0).keySet().iterator().next()+"="+results.get(0).values().iterator().next());
			else
				Log.debugOut("Finished MQL: "+results.size()+" results");
		}
		return results;
	}

	protected String doMQLSelectString(final Modifiable E, final List<String> ignoreStats, final String defPrefix, final String str, final XMLTag piece, final Map<String,Object> defined) throws MQLException,PostProcessException
	{
		final List<Map<String,String>> res=doMQLSelectStrs(E,ignoreStats,defPrefix,str,piece,defined);
		final StringBuilder finalStr = new StringBuilder("");
		for(final Map<String,String> map : res)
		{
			for(final String key : map.keySet())
				finalStr.append(map.get(key)).append(" ");
		}
		return finalStr.toString().trim();
	}

	@Override
	public String doMQLSelectString(final Modifiable E, final String mql)
	{
		final Map<String,Object> defined=new TreeMap<String,Object>();
		final XMLTag piece=CMLib.xml().createNewTag("tag", "value");
		try
		{
			return doMQLSelectString(E,null,null,mql,piece,defined);
		}
		catch(final Exception e)
		{
			final ByteArrayOutputStream bout=new ByteArrayOutputStream();
			final PrintStream pw=new PrintStream(bout);
			e.printStackTrace(pw);
			pw.flush();
			return e.getMessage()+"\n\r"+bout.toString();
		}
	}

	@Override
	public List<Map<String,Object>> doMQLSelectObjects(final Modifiable E, final String mql) throws MQLException
	{
		final Map<String,Object> defined=new TreeMap<String,Object>();
		final XMLTag piece=CMLib.xml().createNewTag("tag", "value");
		try
		{
			return doMQLSelectObjs(E,null,null,mql,piece,defined);
		}
		catch(final PostProcessException e)
		{
			throw new MQLException("Cannot post-process MQL.", e);
		}
	}

	protected String strFilter(Modifiable E, final List<String> ignoreStats, final String defPrefix, String str, final XMLTag piece, final Map<String,Object> defined) throws CMException,PostProcessException
	{
		List<Varidentifier> vars=parseVariables(str);
		final boolean killArticles;
		if((str.length()>6)&&(str.substring(0,6).equalsIgnoreCase("(a(n))")))
			killArticles=true;
		else
			killArticles=false;
		while(vars.size()>0)
		{
			final Varidentifier V=vars.remove(vars.size()-1);
			Object val;
			if(V.isMathExpression)
			{
				final String expression=strFilter(E,ignoreStats,defPrefix,V.var,piece, defined);
				if(CMath.isMathExpression(expression))
				{
					if(expression.indexOf('.')>0)
						val=""+CMath.parseMathExpression(expression);
					else
						val=""+CMath.parseLongExpression(expression);
				}
				else
					throw new CMException("Invalid math expression '$"+expression+"' in str '"+str+"'");
			}
			else
			if(V.var.toUpperCase().startsWith("SELECT:"))
			{
				val=doMQLSelectString(E,ignoreStats,defPrefix,CMLib.xml().restoreAngleBrackets(V.var),piece, defined);
			}
			else
			if(V.var.toUpperCase().startsWith("STAT:") && (E!=null))
			{
				final String[] parts=V.var.toUpperCase().split(":");
				if(E instanceof Environmental)
				{
					Environmental E2=(Environmental)E;
					for(int p=1;p<parts.length-1;p++)
					{
						final Room R=CMLib.map().roomLocation(E2);
						Environmental E3;
						if(parts[p].equals("ROOM"))
							E3=CMLib.map().roomLocation(E2);
						else
						if(parts[p].equals("AREA"))
							E3=CMLib.map().areaLocation(E2);
						else
						if(CMLib.directions().getDirectionCode(parts[p])>=0)
						{
							if(R==null)
								throw new PostProcessException("Unknown room on object "+E2.ID()+" in variable '"+V.var+"'");
							final int dir=CMLib.directions().getDirectionCode(parts[p]);
							E3=R.getRoomInDir(dir);
						}
						else
						if(parts[p].equals("ANYROOM"))
						{
							if(R==null)
								throw new PostProcessException("Unknown room on object "+E2.ID()+" in variable '"+V.var+"'");
							final List<Room> dirs=new ArrayList<Room>();
							for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
							{
								final Room R2=R.getRoomInDir(d);
								if((R2!=null)
								&&((R2.roomID().length()>0)
									||((R.rawDoors()[d] instanceof GridLocale)
									   &&(R.rawDoors()[d].roomID().length()>0))))
									dirs.add(R2);
							}
							if(dirs.size()==0)
								throw new PostProcessException("No anyrooms on object "+E2.ID()+" ("+R.roomID()+"/"+R+") in variable '"+V.var+"' ");
							E3=dirs.get(CMLib.dice().roll(1, dirs.size(), -1));
						}
						else
						if(parts[p].equals("MOB"))
						{
							if(R==null)
								throw new PostProcessException("Unknown room on object "+E2.ID()+" in variable '"+V.var+"'");
							if((R.numInhabitants()==0)||((E2 instanceof MOB)&&(R.numInhabitants()==1)))
								throw new PostProcessException("No mobs in room for "+E2.ID()+" in variable '"+V.var+"'");
							E3=R.fetchInhabitant(CMLib.dice().roll(1, R.numInhabitants(), -1));
						}
						else
						if(parts[p].equals("ITEM"))
						{
							if(R==null)
								throw new PostProcessException("Unknown room on object "+E2.ID()+" in variable '"+V.var+"'");
							if((R.numItems()==0)||((E2 instanceof Item)&&(R.numItems()==1)))
								throw new PostProcessException("No items in room for "+E2.ID()+" in variable '"+V.var+"'");
							E3=R.getItem(CMLib.dice().roll(1, R.numItems(), -1));
						}
						else
						if(parts[p].equals("AREAGATE"))
						{
							if(R==null)
								throw new PostProcessException("Unknown room on object "+E2.ID()+" in variable '"+V.var+"'");
							final List<Room> dirs=new ArrayList<Room>();
							for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
							{
								final Room R2=R.getRoomInDir(d);
								if((R2!=null) && (R2.roomID().length()>0) && (R.getArea()!=R2.getArea()))
									dirs.add(R.getRoomInDir(d));
							}
							if(dirs.size()==0)
							{
								if(defined.get("ROOMTAG_GATEEXITROOM") instanceof Room)
									dirs.add((Room)defined.get("ROOMTAG_GATEEXITROOM"));
								if(dirs.size()==0)
									throw new PostProcessException("No areagates on object "+E2.ID()+" in variable '"+V.var+"'");
							}
							E3=dirs.get(CMLib.dice().roll(1, dirs.size(), -1));
						}
						else
							throw new PostProcessException("Unknown stat code '"+parts[p]+"' on object "+E2.ID()+" in variable '"+V.var+"'");
						if(E3==null)
							throw new PostProcessException("Unknown '"+parts[p]+"' on object "+E2.ID()+" in variable '"+V.var+"'");
						else
							E2=E3;
					}
					E=E2;
				}
				if (E.isStat(parts[parts.length-1]))
					val=E.getStat(parts[parts.length-1]);
				else
					throw new CMException("Unknown stat code '"+parts[parts.length-1]+"' on object "+E+" in variable '"+V.var+"'");
			}
			else
			if(V.var.length()==0)
				continue;
			else
				val = defined.get(V.var.toUpperCase().trim());
			if(val instanceof XMLTag)
			{
				val = findString(E,ignoreStats,defPrefix,"STRING",(XMLTag)val,defined);
			}
			if((val == null)&&(defPrefix!=null)&&(defPrefix.length()>0)&&(E!=null))
			{
				String preValue=V.var;
				if(preValue.toUpperCase().startsWith(defPrefix.toUpperCase()))
				{
					preValue=preValue.toUpperCase().substring(defPrefix.length());
					if((E.isStat(preValue))
					&&((ignoreStats==null)||(!ignoreStats.contains(preValue.toUpperCase()))))
					{
						val=fillOutStatCode(E,ignoreStats,defPrefix,preValue,piece,defined, false);
						XMLTag statPiece=piece;
						while((val == null)
						&&(statPiece.parent()!=null)
						&&(!(defPrefix.startsWith(statPiece.tag())&&(!defPrefix.startsWith(statPiece.parent().tag())))))
						{
							statPiece=statPiece.parent();
							val=fillOutStatCode(E,ignoreStats,defPrefix,preValue,statPiece,defined, false);
						}
						if((ignoreStats!=null)&&(val!=null))
							ignoreStats.add(preValue.toUpperCase());
					}
				}
			}
			if(val == null)
				throw new CMException("Unknown variable '$"+V.var+"' in str '"+str+"'",new CMException("$"+V.var));
			if(V.toUpperCase)
				val=val.toString().toUpperCase();
			if(V.toLowerCase)
				val=val.toString().toLowerCase();
			if(V.toPlural)
				val=CMLib.english().removeArticleLead(CMLib.english().makePlural(val.toString()));
			if(V.toCapitalized)
				val=CMStrings.capitalizeAndLower(val.toString());
			if(V.toOneWord)
				val=CMStrings.removePunctuation(val.toString().replace(' ', '_'));
			if(V.toOneLine)
				val=CMStrings.deleteAllofAny(val.toString().replace('\n',' '), new char[] {'\t','\r'});
			if(V.toJavascript)
				val=MiniJSON.toJSONString(val.toString());
			if(killArticles)
				val=CMLib.english().removeArticleLead(val.toString());
			str=str.substring(0,V.outerStart)+val.toString()+str.substring(V.outerEnd);
			if(vars.size()==0)
				vars=parseVariables(str);
		}
		int x=str.toLowerCase().indexOf("(a(n))");
		while((x>=0)&&(x<str.length()-8))
		{
			if((Character.isWhitespace(str.charAt(x+6)))
			&&(Character.isLetter(str.charAt(x+7))))
			{
				if(CMStrings.isVowel(str.charAt(x+7)))
					str=str.substring(0,x)+"an"+str.substring(x+6);
				else
					str=str.substring(0,x)+"a"+str.substring(x+6);
			}
			else
				str=str.substring(0,x)+"a"+str.substring(x+6);
			x=str.toLowerCase().indexOf("(a(n))");
		}
		return CMLib.xml().restoreAngleBrackets(str).replace('\'','`');
	}

	@Override
	public String findString(final String tagName, final XMLTag piece, final Map<String, Object> defined) throws CMException
	{
		return findStringNow(null,null,null,tagName,piece,defined);
	}

	protected String findStringNow(final String tagName, final XMLTag piece, final Map<String, Object> defined) throws CMException
	{
		return findStringNow(null,null,null,tagName,piece,defined);
	}

	protected int makeNewLevel(final int level, final int oldMin, final int oldMax, final int newMin, final int newMax)
	{
		final double oldRange = oldMax-oldMin;
		final double myOldRange = level - oldMin;
		final double pctOfOldRange = myOldRange/oldRange;
		final double newRange = newMax-newMin;
		return newMin + (int)Math.round(pctOfOldRange * newRange);
	}

	@Override
	public boolean relevelRoom(final Room room, final int oldMin, final int oldMax, final int newMin, final int newMax)
	{
		boolean changeMade=false;
		try
		{
			room.toggleMobility(false);
			CMLib.threads().suspendResumeRecurse(room, false, true);
			if(CMLib.law().getLandTitle(room)!=null)
				return false;
			for(final Enumeration<Item> i=room.items();i.hasMoreElements();)
			{
				final Item I=i.nextElement();
				if(I==null)
					continue;
				if((I instanceof Weapon)||(I instanceof Armor))
				{
					int newILevel=makeNewLevel(I.phyStats().level(),oldMin,oldMax,newMin,newMax);
					if(newILevel <= 0)
						newILevel = 1;
					if(newILevel != I.phyStats().level())
					{
						final int levelDiff = newILevel - I.phyStats().level();
						changeMade=true;
						final int effectiveLevel =CMLib.itemBuilder().timsLevelCalculator(I) + levelDiff;
						I.basePhyStats().setLevel(effectiveLevel);
						I.phyStats().setLevel(effectiveLevel);
						CMLib.itemBuilder().itemFix(I, effectiveLevel, null);
						I.basePhyStats().setLevel(newILevel);
						I.phyStats().setLevel(newILevel);
						I.text();
					}
				}
			}
			for(final Enumeration<MOB> m=room.inhabitants();m.hasMoreElements();)
			{
				final MOB M=m.nextElement();
				if((M!=null)
				&&(M.isMonster())
				&&(M.getStartRoom()==room))
				{
					int newLevel=makeNewLevel(M.phyStats().level(),oldMin,oldMax,newMin,newMax);
					if(newLevel <= 0)
						newLevel = 1;
					if(newLevel != M.phyStats().level())
					{
						changeMade=true;
						final double spdDif = M.basePhyStats().speed() - CMLib.leveler().getLevelMOBSpeed(M);
						final int armDif = M.basePhyStats().armor()-CMLib.leveler().getLevelMOBArmor(M);
						final int dmgDif = M.basePhyStats().damage()-CMLib.leveler().getLevelMOBDamage(M);
						final int attDif = M.basePhyStats().attackAdjustment()-CMLib.leveler().getLevelAttack(M);
						M.basePhyStats().setLevel(newLevel);
						M.phyStats().setLevel(newLevel);
						CMLib.leveler().fillOutMOB(M,M.basePhyStats().level());
						M.basePhyStats().setSpeed(M.basePhyStats().speed()+spdDif);
						M.basePhyStats().setArmor(M.basePhyStats().armor()+armDif);
						M.basePhyStats().setDamage(M.basePhyStats().damage()+dmgDif);
						M.basePhyStats().setAttackAdjustment(M.basePhyStats().attackAdjustment()+attDif);
					}
					for(final Enumeration<Item> mi=M.items();mi.hasMoreElements();)
					{
						final Item mI=mi.nextElement();
						if(mI!=null)
						{
							int newILevel=makeNewLevel(mI.phyStats().level(),oldMin,oldMax,newMin,newMax);
							if(newILevel <= 0)
								newILevel = 1;
							if(newILevel != mI.phyStats().level())
							{
								final int levelDiff = newILevel - mI.phyStats().level();
								changeMade=true;
								final int effectiveLevel =CMLib.itemBuilder().timsLevelCalculator(mI) + levelDiff;
								mI.basePhyStats().setLevel(effectiveLevel);
								mI.phyStats().setLevel(effectiveLevel);
								CMLib.itemBuilder().itemFix(mI, effectiveLevel, null);
								mI.basePhyStats().setLevel(newILevel);
								mI.phyStats().setLevel(newILevel);
								mI.text();
							}
						}
					}
					final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
					if(SK!=null)
					{
						for(final Iterator<CoffeeShop.ShelfProduct> i=SK.getShop().getStoreShelves();i.hasNext();)
						{
							final CoffeeShop.ShelfProduct P=i.next();
							final Environmental E2=P.product;
							if((E2 instanceof Item)||(E2 instanceof MOB))
							{
								final Physical P2=(Physical)E2;
								newLevel=makeNewLevel(P2.phyStats().level(),oldMin,oldMax,newMin,newMax);
								if(newLevel <= 0)
									newLevel = 1;
								if(newLevel != P2.phyStats().level())
								{
									changeMade=true;
									if(E2 instanceof Item)
									{
										final Item I2=(Item)E2;
										final int levelDiff = newLevel - I2.phyStats().level();
										final int effectiveLevel =CMLib.itemBuilder().timsLevelCalculator(I2) + levelDiff;
										I2.basePhyStats().setLevel(effectiveLevel);
										I2.phyStats().setLevel(effectiveLevel);
										CMLib.itemBuilder().itemFix(I2, effectiveLevel, null);
									}
									P2.basePhyStats().setLevel(newLevel);
									P2.phyStats().setLevel(newLevel);
									if(E2 instanceof MOB)
										CMLib.leveler().fillOutMOB((MOB)E2,P2.basePhyStats().level());
									E2.text();
								}
							}
						}
					}
					M.text();
					M.recoverCharStats();
					M.recoverMaxState();
					M.recoverPhyStats();
					M.recoverCharStats();
					M.recoverMaxState();
					M.recoverPhyStats();
					M.resetToMaxState();
				}
			}
		}
		catch(final Exception e)
		{
			Log.errOut(e);
			changeMade=false;
		}
		finally
		{
			room.recoverRoomStats();
			CMLib.threads().suspendResumeRecurse(room, false, false);
			room.toggleMobility(true);
			room.recoverRoomStats();
		}
		return changeMade;
	}

}
