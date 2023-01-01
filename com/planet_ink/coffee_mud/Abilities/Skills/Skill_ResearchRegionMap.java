package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.TimeManager;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.RFilter;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2020-2023 Bo Zimmerman

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
public class Skill_ResearchRegionMap extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_ResearchRegionMap";
	}

	private final static String	localizedName	= CMLib.lang().L("Research Region Map");

	@Override
	public String name()
	{
		return localizedName;
	}

	protected String	displayText	= L("(Researching)");

	@Override
	public String displayText()
	{
		return displayText;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] {"RESEARCHREGIONS", "RESEARCHREGIONMAP" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	@Override
	public void unInvoke()
	{
		if(!unInvoked)
		{
			final Physical affected=this.affected;
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if(finalMapI==null)
					mob.tell(L("Your research fails to find anything on '@x1'.",what));
				else
				{
					mob.tell(L("Your research uncovers '@x1'.",finalMapI.name(mob)));
					mob.addItem(finalMapI);
				}
			}
		}
		super.unInvoke();
		setMiscText("");
	}

	protected Room theRoom = null;
	protected Item finalMapI = null;
	protected List<Room> checkSet = null;
	protected String what = "";
	protected int ticksToRemain = 0;
	protected int numBooksInRoom = 1;
	protected final Room[] targetRoom=new Room[1];

	protected final LimitedTreeSet<String> recent=new LimitedTreeSet<String>(TimeManager.MILI_HOUR,50,false);

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		finalMapI = null;
		targetRoom[0]=null;
		checkSet = null;
		numBooksInRoom = 1;
		what = newMiscText;
		ticksToRemain = 0;
		theRoom=null;
		final Physical affected = this.affected;
		if(affected instanceof MOB)
			numBooksInLibrary((MOB)affected); // sets the appropriate variables
	}

	protected TrackingLibrary.TrackingFlags getTrackingFlags()
	{
		TrackingLibrary.TrackingFlags flags;
		flags = CMLib.tracking().newFlags()
				.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
				.plus(TrackingLibrary.TrackingFlag.NOAIR)
				.plus(TrackingLibrary.TrackingFlag.NOWATER);
		return flags;
	}

	protected Area[][] insertAreaColumn(final Area[][] areaDrawing, final int x, final int dupAllButDex)
	{
		final int height=areaDrawing[0].length;
		final Area[][] newArea=new Area[areaDrawing.length+1][height];
		for(int i=0;i<x;i++)
			newArea[i]=areaDrawing[i];
		for(int i=x;i<areaDrawing.length;i++)
			newArea[i+1]=areaDrawing[i];
		if(dupAllButDex >= 0)
		{
			newArea[x]=Arrays.copyOf(newArea[x+1], newArea[x+1].length);
			newArea[x][dupAllButDex]=null;
		}
		else
			newArea[x]=new Area[newArea[0].length];
		return newArea;
	}

	protected Area[][] insertAreaRow(final Area[][] areaDrawing, final int y, final int dupAllButDex)
	{
		final int oldHeight=areaDrawing[0].length;
		final Area[][] newArea=areaDrawing;
		for(int x=0;x<areaDrawing.length;x++)
		{
			final Area[] oldCol=areaDrawing[x];
			final Area[] newCol=new Area[oldHeight+1];
			newArea[x]=newCol;
			for(int i=0;i<y;i++)
				newCol[i]=oldCol[i];
			for(int i=y;i<oldCol.length;i++)
				newCol[i+1]=oldCol[i];
			if((dupAllButDex >= 0) && (dupAllButDex != x))
				newCol[y]=newCol[y+1];
		}
		return newArea;
	}

	protected char pickMapChar(final char c1, final char c2, final Area A1, final Area A2, final Map<Area,Map<Area,int[]>> connections)
	{
		if(A1==A2)
			return ' ';
		if((A2!=null)
		&&( ((connections.containsKey(A1))&&(connections.get(A1).containsKey(A2)))
			||((connections.containsKey(A2))&&(connections.get(A2).containsKey(A1)))))
			return c2;
		return c1;
	}

	protected char[] pickMapChars(final char c1, final char c2, final Area A1, final Area A2, final Map<Area,Map<Area,int[]>> connections)
	{
		final char c=pickMapChar(c1,c2,A1,A2,connections);
		if(c==' ')
			return new char[]{c,c};
		else
			return new char[]{c1,c};
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID==Tickable.TICKID_MOB)
		{
			final Physical affected=this.affected;
			if(!(affected instanceof MOB))
				return true;
			final MOB mob=(MOB)affected;
			Room R;
			synchronized(this)
			{
				R=theRoom;
			}
			if(R == null)
			{
				if(numBooksInLibrary(mob)==0)
				{
					mob.tell(L("You fail researching."));
					unInvoke();
					return false;
				}
				R=theRoom;
				if(R==null)
				{
					unInvoke();
					return false;
				}
			}
			if(!R.isInhabitant(mob)
			||(mob.isInCombat())
			||(!CMLib.flags().canBeSeenBy(R, mob))
			||(!CMLib.flags().isAliveAwakeMobileUnbound(mob,true)))
			{
				finalMapI=null;
				mob.tell(L("You stop researching."));
				unInvoke();
				return false;
			}
			if((tickDown==4)&&(checkSet != null))
			{
				if(!R.show(mob,null,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> <S-IS-ARE> almost done researching '@x1'",what)))
				{
					unInvoke();
					return false;
				}
			}
			else
			if((tickDown%4)==0)
			{
				if(!R.show(mob,null,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> continue(s) researching '@x1'",what)))
				{
					unInvoke();
					return false;
				}
			}
			if(checkSet==null)
			{
				final HashSet<Area> areas=new HashSet<Area>();
				Area A=null;
				final HashSet<Area> areasTried=new HashSet<Area>();
				int numAreas = 0;
				numAreas=(int)Math.round(CMath.mul(CMLib.map().numAreas(),0.90))+1;
				if(numAreas>CMLib.map().numAreas())
					numAreas=CMLib.map().numAreas();
				int tries=numAreas*numAreas;
				while((areas.size()<numAreas)&&(((--tries)>0)))
				{
					A=CMLib.map().getRandomArea();
					if((A!=null)&&(!areasTried.contains(A)))
					{
						areasTried.add(A);
						if((CMLib.flags().canAccess(mob,A))
						&&(!CMath.bset(A.flags(),Area.FLAG_INSTANCE_CHILD)))
							areas.add(A);
						else
							numAreas--;
					}
				}
				final int range=45 + (2*super.getXLEVELLevel(mob))+(10*super.getXMAXRANGELevel(mob));
				this.finalMapI=null;
				final Area targetA=CMLib.map().findArea(what);
				final Room startRoom=mob.location();
				final Area startArea=(startRoom==null)?null:startRoom.getArea();
				if((targetA==null)
				||(startRoom==null)
				||(startArea==null)
				||(startRoom!=this.theRoom)
				||(recent.contains(startArea.Name()+"->"+targetA.Name())))
				{
					checkSet=new ArrayList<Room>();
					return true;
				}
				recent.add(startArea.Name()+"->"+targetA.Name());
				this.checkSet=new Vector<Room>(range*10);
				targetRoom[0]=null;
				if(!CMLib.tracking().getRadiantRoomsToTarget(mob.location(), checkSet, getTrackingFlags(), new RFilter() {
					@Override
					public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
					{
						if((R!=null)
						&&(R.roomID().length()>0)
						&&(R.getArea()==targetA)
						&&(CMLib.flags().canAccess(mob, R)))
						{
							targetRoom[0]=R;
							return false;
						}
						return true;
					}
				},range))
				{
					unInvoke();
					return false;
				}
				else
				if(targetRoom[0]!=null)
				{
					final List<List<Integer>> trails = CMLib.tracking().findAllTrails(mob.location(), targetRoom[0], this.checkSet);
					this.checkSet.clear(); // don't need it any longer
					final OrderedMap<Area,Map<Area,int[]>> connections = new OrderedMap<Area,Map<Area,int[]>>();
					for(final List<Integer> trail : trails)
					{
						Room curRoom=startRoom;
						for(final Integer dir : trail)
						{
							final Room nextR=curRoom.getRoomInDir(dir.intValue());
							if(nextR==null)
								Log.errOut("Regional Map fail!");
							else
							{
								if(nextR.getArea()!=curRoom.getArea())
								{
									if(!connections.containsKey(curRoom.getArea()))
										connections.put(curRoom.getArea(), new HashMap<Area,int[]>());

									if(!connections.get(curRoom.getArea()).containsKey(nextR.getArea()))
										connections.get(curRoom.getArea()).put(nextR.getArea(),new int[] {1});
									else
										connections.get(curRoom.getArea()).get(nextR.getArea())[0]++;
								}
								curRoom=nextR;
							}
						}
					}
					final List<Quad<Area,Area,long[],long[]>> coords=new ArrayList<Quad<Area,Area,long[],long[]>>();
					for(final Iterator<Area> sa = connections.keyIterator();sa.hasNext();)
					{
						final Area sA=sa.next();
						final Map<Area,int[]> connectAs=connections.get(sA);
						final Area fA;
						if(connectAs.size()<2)
							fA=connectAs.keySet().iterator().next();
						else
						{
							Area bestA=null;
							int highestCount=0;
							for(final Area bA : connectAs.keySet())
							{
								if(connectAs.get(bA)[0]>highestCount)
								{
									bestA=bA;
									highestCount=connectAs.get(bA)[0];
								}
							}
							fA=bestA;
						}
						if(fA==null)
							continue;
						final Quad<Area,Area,long[],long[]> quad=new Quad<Area,Area,long[],long[]>(sA,fA,new long[1],new long[1]);
						coords.add(quad);
						for(final List<Integer> trail : trails)
						{
							Room curRoom=startRoom;
							boolean started=(curRoom.getArea()==sA);
							for(final Integer dir : trail)
							{
								final Room nextR=curRoom.getRoomInDir(dir.intValue());
								if(nextR==null)
									Log.errOut("Regional Map fail!");
								else
								{
									if(started)
									{
										final int[] delta = Directions.adjustXYByDirections(0,0,dir.intValue());
										if((curRoom.domainType()&Room.INDOORS)==0)
										{
											delta[0]*=2;
											delta[1]*=2;
										}
										quad.third[0]+=delta[0];
										quad.fourth[0]+=delta[1];
										if(nextR.getArea()==fA)
											break;
									}
									else
									if(nextR.getArea()==sA)
										started=true;
									curRoom=nextR;
								}
							}
						}
					}
					//connections.clear(); -- needed below
					final int trailSize=trails.size();
					trails.clear();
					final List<Triad<Area,Area,Integer>> finalDirs = new ArrayList<Triad<Area,Area,Integer>>();
					for(final Quad<Area,Area,long[],long[]> quad : coords)
					{
						final int fXMove = (int)Math.round(CMath.div((double)quad.third[0],trailSize));
						final int fYMove = (int)Math.round(CMath.div((double)quad.fourth[0],trailSize));
						final int finalDir;
						if(fYMove == 0)
							finalDir = (fXMove<0)?Directions.WEST:Directions.EAST;
						else
						if(fXMove == 0)
							finalDir = (fYMove<0)?Directions.NORTH:Directions.SOUTH;
						else
						{
							final double afXMove=Math.abs(fXMove);
							final double afYMove=Math.abs(fYMove);
							//final int thirdSide=(int)Math.round(Math.sqrt((fXMove*fXMove)+(fYMove*fYMove)));
							final double atan = Math.atan(CMath.div(afXMove,afYMove));
							if((fXMove>0)&&(fYMove<0))
								finalDir=(atan<.392)?Directions.NORTH:((atan>1.18)?Directions.EAST:Directions.NORTHEAST);
							else
							if((fXMove<0)&&(fYMove<0))
								finalDir=(atan<.392)?Directions.NORTH:((atan>1.18)?Directions.WEST:Directions.NORTHWEST);
							else
							if((fXMove<0)&&(fYMove>0))
								finalDir=(atan<.392)?Directions.SOUTH:((atan>1.18)?Directions.WEST:Directions.SOUTHWEST);
							else
							if((fXMove>0)&&(fYMove>0))
								finalDir=(atan<.392)?Directions.SOUTH:((atan>1.18)?Directions.EAST:Directions.SOUTHEAST);
							else
							{
								Log.errOut("Regional Map fail! II");
								continue;
							}
						}
						finalDirs.add(new Triad<Area,Area,Integer>(quad.first,quad.second,Integer.valueOf(finalDir)));
					}
					coords.clear();
					Area[][] areaDrawing = new Area[1][1];
					areaDrawing[0][0]=startRoom.getArea();

					for(final Triad<Area,Area,Integer> ta : finalDirs)
					{
						final Area sA=ta.first;
						final Area tA=ta.second;
						final Integer dir=ta.third;
						int x=0;
						int y=0;
						while((x<areaDrawing.length)&&(y<areaDrawing[x].length))
						{
							if(areaDrawing[x][y]==sA)
							{
								final int[] nextDir=Directions.adjustXYByDirections(x, y, dir.intValue());
								if(nextDir[0]<0)
								{
									areaDrawing=this.insertAreaColumn(areaDrawing, 0,-1);
									nextDir[0]=0;
								}
								else
								if(nextDir[0]>=areaDrawing.length)
								{
									areaDrawing=this.insertAreaColumn(areaDrawing, areaDrawing.length,-1);
									nextDir[0]=areaDrawing.length-1;
								}
								if(nextDir[1]<0)
								{
									areaDrawing=this.insertAreaRow(areaDrawing, 0,-1);
									nextDir[1]=0;
								}
								else
								if(nextDir[1]>=areaDrawing[x].length)
								{
									areaDrawing=this.insertAreaRow(areaDrawing, areaDrawing[x].length,-1);
									nextDir[1]=areaDrawing[x].length-1;
								}
								if(areaDrawing[nextDir[0]][nextDir[1]]==null)
									areaDrawing[nextDir[0]][nextDir[1]]=tA;
								else
								{
									if(((dir.intValue()==Directions.NORTH)||(dir.intValue()==Directions.NORTHEAST)||(dir.intValue()==Directions.NORTHWEST))
									&&(areaDrawing[nextDir[0]][nextDir[1]]==null))
										areaDrawing=this.insertAreaRow(areaDrawing, nextDir[1],nextDir[0]);
									if(((dir.intValue()==Directions.SOUTH)||(dir.intValue()==Directions.SOUTHEAST)||(dir.intValue()==Directions.SOUTHWEST))
									&&(areaDrawing[nextDir[0]][nextDir[1]]==null))
										areaDrawing=this.insertAreaRow(areaDrawing, nextDir[1],nextDir[0]);
									if(((dir.intValue()==Directions.WEST)||(dir.intValue()==Directions.NORTHWEST)||(dir.intValue()==Directions.SOUTHWEST))
									&&(areaDrawing[nextDir[0]][nextDir[1]]==null))
										areaDrawing=this.insertAreaColumn(areaDrawing, nextDir[0],nextDir[1]);
									if(((dir.intValue()==Directions.EAST)||(dir.intValue()==Directions.NORTHEAST)||(dir.intValue()==Directions.SOUTHEAST))
									&&(areaDrawing[nextDir[0]][nextDir[1]]==null))
										areaDrawing=this.insertAreaColumn(areaDrawing, nextDir[0],nextDir[1]);
									areaDrawing[nextDir[0]][nextDir[1]]=tA;
								}
								break;
							}
							y++;
							if(y>=areaDrawing[x].length)
							{
								y=0;
								x++;
							}
						}
					}
					finalDirs.clear();
					final StringBuilder map=new StringBuilder("");
					for(int y=0;y<areaDrawing[0].length;y++)
					{
						final StringBuilder phatRows[] = new StringBuilder[5];
						for(int i=0;i<phatRows.length;i++)
							phatRows[i]=new StringBuilder("");
						for(int x=0;x<areaDrawing.length;x++)
						{
							if(areaDrawing[x][y]==null)
							{
								phatRows[0].append("          ");
								phatRows[1].append("          ");
								phatRows[2].append("          ");
								phatRows[3].append("          ");
								phatRows[4].append("          ");
							}
							else
							{
								final Area A1=areaDrawing[x][y];
								Area A2;
								A2=(x>0)?areaDrawing[x-1][y]:null;
								final char leftChar[]=pickMapChars('!','-',A1,A2,connections);
								A2=(x<areaDrawing.length-1)?areaDrawing[x+1][y]:null;
								final char rightChar[]=pickMapChars('!','-',A1,A2,connections);
								A2=(y>0)?areaDrawing[x][y-1]:null;
								final char topChar[]=pickMapChars('-','!',A1,A2,connections);
								A2=(y<areaDrawing[0].length-1)?areaDrawing[x][y+1]:null;
								final char botChar[]=pickMapChars('-','!',A1,A2,connections);

								A2=((y>0)&&(x>0))?areaDrawing[x-1][y-1]:null;
								final char diagCharTL=pickMapChar('+','\\',A1,A2,connections);
								A2=((y>0)&&(x<areaDrawing.length-1))?areaDrawing[x+1][y-1]:null;
								final char diagCharTR=pickMapChar('+','/',A1,A2,connections);
								A2=((x>0)&&(y<areaDrawing[0].length-1))?areaDrawing[x-1][y+1]:null;
								final char diagCharBL=pickMapChar('+','/',A1,A2,connections);
								A2=((x<areaDrawing.length-1)&&(y<areaDrawing[0].length-1))?areaDrawing[x+1][y+1]:null;
								final char diagCharBR=pickMapChar('+','\\',A1,A2,connections);
								final String areaName=CMStrings.limit(CMStrings.padRight(A1.name(), 24),24);
								phatRows[0].append(diagCharTL+CMStrings.repeat(topChar[0], 3)+topChar[1]+topChar[1]+CMStrings.repeat(topChar[0], 3)+diagCharTR);
								phatRows[1].append(leftChar[0]+areaName.substring(0,8)+rightChar[0]);
								phatRows[2].append(leftChar[1]+areaName.substring(8,16)+rightChar[1]);
								phatRows[3].append(leftChar[0]+areaName.substring(16,24)+rightChar[0]);
								phatRows[4].append(diagCharBL+CMStrings.repeat(botChar[0], 3)+botChar[1]+botChar[1]+CMStrings.repeat(botChar[0], 3)+diagCharBR);
							}
						}
						for(final StringBuilder str : phatRows)
							map.append(str).append("\n\r");
					}
					this.finalMapI=CMClass.getBasicItem("GenReadable");
					this.finalMapI.setName(L("A region map from @x1 to @x2",this.theRoom.getArea().Name(),what));
					this.finalMapI.setDisplayText(L("A rolled up map to @x1 lies here.",what));
					this.finalMapI.setReadable(true);
					this.finalMapI.setReadableText("\n\r"+map.toString());
					this.finalMapI.recoverPhyStats();
					this.finalMapI.text();
					this.tickDown += (5*connections.size());
					connections.clear();
				}
				return true;
			}
		}
		return true;
	}

	protected int numBooksInLibrary(final MOB mob)
	{
		if(mob==null)
			return 0;
		final Room R=mob.location();
		if(R==null)
			return 0;
		if(theRoom == null)
		{
			numBooksInRoom = 0;
			theRoom=R;
			if(CMLib.english().containsString(R.displayText(), "library"))
				numBooksInRoom += 10;
			for(final Enumeration<Item> i=R.items();i.hasMoreElements();)
			{
				final Item I=i.nextElement();
				if((I instanceof Book)
				&&(((Book)I).getUsedPages()>0))
					numBooksInRoom++;
			}
			for(final Enumeration<Item> i=mob.items();i.hasMoreElements();)
			{
				final Item I=i.nextElement();
				if((I instanceof Book)
				&&(((Book)I).getUsedPages()>0))
					numBooksInRoom++;
			}
			for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
			{
				final MOB M=m.nextElement();
				if(M==null)
					continue;
				final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
				if(SK==null)
					continue;
				final CoffeeShop shop=SK.getShop();
				if(shop!=null)
				{
					for(final Iterator<Environmental> i=shop.getStoreInventory();i.hasNext();)
					{
						final Environmental I=i.next();
						if((I instanceof Book)
						&&(((Book)I).getUsedPages()>0))
							numBooksInRoom++;
					}
				}
			}
		}
		return numBooksInRoom;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		String areaName=CMParms.combine(commands);
		if(areaName.trim().length()==0)
		{
			mob.tell(L("Research which area?"));
			return false;
		}
		final Room R=mob.location();
		if(R==null)
			return false;

		if(this.numBooksInLibrary(mob)==0)
		{
			mob.tell(L("I don't think you'll get much research done here."));
			return false;
		}

		if(mob.isInCombat())
		{
			mob.tell(L("You are too busy to do that right now."));
			return false;
		}

		if(!CMLib.flags().canBeSeenBy(R, mob))
		{
			mob.tell(L("You need to be able to see to do that."));
			return false;
		}
		if(!CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
		{
			mob.tell(L("You can't do that right now."));
			return false;
		}
		final Area A=CMLib.map().findArea(areaName);
		if((A==null)||(!CMLib.flags().canAccess(mob, A)))
		{
			mob.tell(L("You don't know of a place called '@x1'",areaName));
			return false;
		}
		if((A==mob.location().getArea())
		||(A.inMyMetroArea(mob.location().getArea()))
		||(mob.location().getArea().inMyMetroArea(A)))
		{
			mob.tell(L("That would be a pretty silly map to research from here."));
			return false;
		}
		areaName=A.Name();

		if(!super.invoke(mob, commands, givenTarget, auto, asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,mob.isMonster()?null:L("<S-NAME> begin(s) to research the paths to '@x1'.",areaName));
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				final Skill_ResearchRegionMap researchA = (Skill_ResearchRegionMap)beneficialAffect(mob,mob,asLevel,10);
				if(researchA != null)
				{
					researchA.tickDown=10; // override any expertise!
					researchA.setMiscText(areaName);
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to engage in research, but can't get started."));
		// return whether it worked
		return success;
	}
}
