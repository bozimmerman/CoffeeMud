package com.planet_ink.coffee_mud.Abilities.Properties;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2018 Bo Zimmerman

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
public class Prop_ReqCapacity extends Property implements TriggeredAffect
{
	@Override
	public String ID()
	{
		return "Prop_ReqCapacity";
	}

	@Override
	public String name()
	{
		return "Capacity Limitations";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS | Ability.CAN_AREAS | Ability.CAN_EXITS;
	}

	public int		peopleCap		= Integer.MAX_VALUE;
	public int		playerCap		= Integer.MAX_VALUE;
	public int		mobCap			= Integer.MAX_VALUE;
	public int		itemCap			= Integer.MAX_VALUE;
	public int		maxWeight		= Integer.MAX_VALUE;
	public int		roomLimit		= Integer.MAX_VALUE;
	protected long	lastCheck		= 0;
	public boolean	indoorOnly		= false;
	public boolean	containersOk	= false;

	@Override
	public long flags()
	{
		return Ability.FLAG_ZAPPER;
	}

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_ENTER;
	}

	@Override
	public String accountForYourself()
	{
		return 
		   ((peopleCap==Integer.MAX_VALUE)?"":L("\n\rPerson limit: @x1",(""+peopleCap)))
		  +((playerCap==Integer.MAX_VALUE)?"":L("\n\rPlayer limit: @x1",(""+playerCap)))
		  +((mobCap==Integer.MAX_VALUE)?"":L("\n\rMOB limit   : @x1",(""+mobCap)))
		  +((itemCap==Integer.MAX_VALUE)?"":L("\n\rItem limit  : @x1",(""+itemCap)))
		  +((roomLimit==Integer.MAX_VALUE)?"":L("\n\rRoom limit  : @x1",(""+roomLimit)))
		  +((maxWeight==Integer.MAX_VALUE)?"":L("\n\rWeight limit: @x1",(""+maxWeight)));
	}

	@Override
	public void setMiscText(String txt)
	{
		super.setMiscText(txt);
		peopleCap=Integer.MAX_VALUE;
		playerCap=Integer.MAX_VALUE;
		mobCap=Integer.MAX_VALUE;
		itemCap=Integer.MAX_VALUE;
		maxWeight=Integer.MAX_VALUE;
		roomLimit=Integer.MAX_VALUE;
		indoorOnly=false;
		if(txt.length()==0)
			peopleCap=2;
		else
		if(CMath.isNumber(txt))
			peopleCap=CMath.s_int(txt);
		else
		{
			peopleCap=CMParms.getParmInt(txt,"people",peopleCap);
			playerCap=CMParms.getParmInt(txt,"players",playerCap);
			mobCap=CMParms.getParmInt(txt,"mobs",mobCap);
			itemCap=CMParms.getParmInt(txt,"items",itemCap);
			roomLimit=CMParms.getParmInt(txt,"rooms",roomLimit);
			maxWeight=CMParms.getParmInt(txt,"weight",maxWeight);
			indoorOnly=CMParms.getParmBool(txt,"indoor",indoorOnly);
			containersOk=CMParms.getParmBool(txt,"droponly",containersOk)||CMParms.getParmBool(txt,"containersok",containersOk);
		}
	}

	protected int getRoomWeight(Room R)
	{
		if(R==null)
			return Integer.MAX_VALUE/2;
		int soFar=0;
		for(int i=0;i<R.numItems();i++)
		{
			final Item I = R.getItem(i);
			if (I != null)
				soFar += I.phyStats().weight();
		}
		return soFar;
	}

	protected int getRoomItemCount(Room R)
	{
		if(R==null)
			return Integer.MAX_VALUE/2;
		int soFar=0;
		for(int i=0;i<R.numItems();i++)
		{
			final Item I=R.getItem(i);
			if((I!=null)&&(I.container()==null))
				soFar++;
		}
		return soFar;
	}
	
	protected void overflowCheck()
	{
		final Physical affected=this.affected;
		if(affected!=null)
		{
			final List<Room> doRooms = new LinkedList<Room>();
			if(affected instanceof Area)
				doRooms.addAll(Collections.list(((Area)affected).getProperMap()));
			else
				doRooms.add(CMLib.map().roomLocation(affected));
			for(Iterator<Room> r=doRooms.iterator();r.hasNext();)
			{
				try
				{
					final Room R=r.next();
					if(R!=null)
					{
						if((peopleCap<Integer.MAX_VALUE)
						&&((!indoorOnly)||((R.domainType()&Room.INDOORS)==Room.INDOORS)))
						{
						}
						if((!indoorOnly)||((R.domainType()&Room.INDOORS)==Room.INDOORS))
						{
							if(itemCap<Integer.MAX_VALUE)
							{
								final int roomItemCount=getRoomItemCount(R);
								if(roomItemCount>itemCap)
								{
									int totOver=roomItemCount-itemCap;
									Room targetRoom = null;
									int smallestCount=Integer.MAX_VALUE/2;
									for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
									{
										final Room R2=R.getRoomInDir(d);
										final Exit E2=R.getExitInDir(d);
										if((R2!=null)&&(E2!=null)
										&&(E2.isOpen())&&(!CMLib.flags().isAiryRoom(R2)))
										{
											int rct=this.getRoomItemCount(R2);
											if(rct < smallestCount)
											{
												targetRoom=R2;
												smallestCount=rct;
											}
										}
									}
									if(targetRoom != null)
									{
										for(int ri=R.numItems()-1;ri>=0 && totOver>0;ri--,totOver--)
										{
											Item I=R.getItem(ri);
											if((I!=null)&&(I.container()==null))
												targetRoom.moveItemTo(I);
										}
									}
								}
							}
							if(maxWeight<Integer.MAX_VALUE)
							{
								final int roomItemWeight=getRoomWeight(R);
								if(roomItemWeight>maxWeight)
								{
									int totOver=roomItemWeight-maxWeight;
									Room targetRoom = null;
									int smallestCount=Integer.MAX_VALUE/2;
									for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
									{
										final Room R2=R.getRoomInDir(d);
										final Exit E2=R.getExitInDir(d);
										if((R2!=null)&&(E2!=null)
										&&(E2.isOpen())&&(!CMLib.flags().isAiryRoom(R2)))
										{
											int rct=this.getRoomWeight(R2);
											if(rct < smallestCount)
											{
												targetRoom=R2;
												smallestCount=rct;
											}
										}
									}
									if(targetRoom != null)
									{
										for(int ri=R.numItems()-1;ri>=0 && totOver>0;ri--,totOver--)
										{
											Item I=R.getItem(ri);
											if((I!=null)&&(I.container()==null))
											{
												targetRoom.moveItemTo(I);
												totOver -= I.phyStats().weight();
											}
										}
									}
								}
							}
						}
					}
				}
				catch(Exception e)
				{
					Log.errOut("Prop_ReqCapacity",e);
				}
			}
		}
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		final Physical affected=this.affected;
		if(affected!=null)
		{
			if(System.currentTimeMillis() > lastCheck)
			{
				this.lastCheck=System.currentTimeMillis() + (30 * 60 * 1000);
				CMLib.threads().executeRunnable(new Runnable()
				{
					@Override
					public void run()
					{
						overflowCheck();
					}
				});
			}
			
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ENTER:
				if((msg.target() instanceof Room)
				&&(peopleCap<Integer.MAX_VALUE)
				&&((!indoorOnly)||((((Room)msg.target()).domainType()&Room.INDOORS)==Room.INDOORS))
				&&((msg.amITarget(affected))||(msg.tool()==affected)||(affected instanceof Area)))
				{
					if(((Room)msg.target()).numInhabitants()>=peopleCap)
					{
						msg.source().tell(L("No more people can fit in there."));
						if(msg.source().isMonster())
						{
							final MOB M=msg.source().amUltimatelyFollowing();
							if((M!=null)&&(!M.isMonster())&&(M.location()==(Room)msg.target()))
								M.tell(L("No more people can fit in here."));
						}
						return false;
					}
					if(((Room)msg.target()).numPCInhabitants()>=playerCap)
					{
						msg.source().tell(L("No more players can fit in there."));
						return false;
					}
					if(msg.source().isMonster()
					&& (((Room)msg.target()).numInhabitants()-((Room)msg.target()).numPCInhabitants())>=mobCap)
					{
						msg.source().tell(L("No more MOBs can fit in there."));
						final MOB M=msg.source().amUltimatelyFollowing();
						if((!M.isMonster())&&(M.location()==(Room)msg.target()))
							M.tell(L("No more people can fit in here."));
						return false;
					}
				}
				break;
			case CMMsg.TYP_PUSH:
			case CMMsg.TYP_PULL:
				if((msg.tool() != affected)
				&&(msg.tool() != myHost))
					break;
				//$FALL-THROUGH$
			case CMMsg.TYP_DROP:
			case CMMsg.TYP_ITEMGENERATED:
				if((msg.target() instanceof Item)
				&&(msg.source().location()!=null)
				&&((!msg.targetMajor(CMMsg.MASK_INTERMSG))||(!containersOk))) // intermsgs are PUTs on the ground
				{
					final Item targetI=(Item)msg.target();
					Room R=null;
					if(affected instanceof Room)
					{
						R=(Room)affected;
						if((msg.source().location() != R)
						&&(msg.targetMinor()!=CMMsg.TYP_PUSH)
						&&(msg.targetMinor()!=CMMsg.TYP_PULL))
							break;
					}
					else
					if(myHost instanceof Room)
					{
						R=(Room)myHost;
						if((msg.source().location() != R)
						&&(msg.targetMinor()!=CMMsg.TYP_PUSH)
						&&(msg.targetMinor()!=CMMsg.TYP_PULL))
							break;
					}
					else
						R=msg.source().location();
					if((!indoorOnly)||((R.domainType()&Room.INDOORS)==Room.INDOORS))
					{
						if(itemCap<Integer.MAX_VALUE)
						{
							int soFar=0;
							int rawResources=0;
							for(int i=0;i<R.numItems();i++)
							{
								final Item I=R.getItem(i);
								if(I instanceof RawMaterial)
									rawResources++;
								if((I!=null)&&(I.container()==null))
									soFar++;
							}
							if(soFar>=itemCap)
							{
								msg.source().tell(L("There is no more room in here to drop @x1.",msg.target().Name()));
								if((rawResources>0)&&(CMath.div(rawResources,itemCap)>0.5))
									msg.source().tell(L("You should consider bundling up some of those resources."));
								return false;
							}
						}
						if(maxWeight<Integer.MAX_VALUE)
						{
							int soFar=getRoomWeight(R);
							if((soFar+targetI.phyStats().weight())>=maxWeight)
							{
								msg.source().tell(L("There is no room in here to put @x1.",targetI.Name()));
								return false;
							}
						}
					}
				}
				break;
			}
		}
		return super.okMessage(myHost,msg);
	}
}
