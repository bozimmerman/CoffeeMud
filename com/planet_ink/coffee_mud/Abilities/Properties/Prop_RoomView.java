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
   Copyright 2003-2025 Bo Zimmerman

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
public class Prop_RoomView extends Property
{
	@Override
	public String ID()
	{
		return "Prop_RoomView";
	}

	@Override
	public String name()
	{
		return "Different Room View";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS|Ability.CAN_ITEMS|Ability.CAN_EXITS;
	}

	protected Room newRoom=null;
	protected boolean longlook = false;
	protected boolean attack = false;
	protected String uviewedRoomID = "";
	protected int uviewedRoomDir = -1;
	//protected Integer[] dirAdj = new Integer[0];

	@Override
	public void setMiscText(final String text)
	{
		super.setMiscText(text);
		final int x=text.indexOf(';');
		longlook=false;
		attack=false;
		newRoom=null;
		uviewedRoomDir=-1;
		if(x>=0)
		{
			final String parms=text.substring(0,x);
			for(final String str : CMParms.parse(parms))
				if(str.equalsIgnoreCase("LONGLOOK"))
					longlook=true;
				else
				if(str.equalsIgnoreCase("ATTACK"))
					longlook=true;
			uviewedRoomID=text.substring(x+1).trim().toUpperCase();
		}
		else
			uviewedRoomID=text.trim();
		if (uviewedRoomID.endsWith("NOUT"))
		{
			uviewedRoomDir = CMLib.directions().getGoodDirectionCode(uviewedRoomID.substring(0,uviewedRoomID.length()-4));
			if(uviewedRoomDir < 0)
				Log.errOut("Unknown direction in room code: "+uviewedRoomID);
			else
				uviewedRoomID = "NOUT";
		}
		else
			uviewedRoomDir = CMLib.directions().getGoodDirectionCode(uviewedRoomID);
	}

	@Override
	public String accountForYourself()
	{
		return "Different View of "+uviewedRoomID;
	}

	protected Room getNewRoom()
	{
		if(uviewedRoomID.equals("OUT")||uviewedRoomID.equals("NOUT"))
		{
			Room newRoom = CMLib.map().roomLocation(affected);
			if((newRoom != null)&&(newRoom.getArea() instanceof Boardable))
			{
				final Room thereR=CMLib.map().roomLocation(((Boardable)newRoom.getArea()).getBoardableItem());
				if(thereR!=null)
				{
					newRoom=thereR;
					if (uviewedRoomID.endsWith("NOUT"))
					{
						final int dir = this.uviewedRoomDir;
						Room R = newRoom.getRoomInDir(dir);
						Exit E = newRoom.getExitInDir(dir);
						if ((R != null)
						&& (E != null)
						&& (E.isOpen())
						&& (!CMLib.flags().isHidden(E)))
						{
							newRoom = R;
							while((newRoom.roomID().length()==0)
							&&((newRoom.domainType()==Room.DOMAIN_OUTDOORS_AIR)
								||(newRoom.domainType()==Room.DOMAIN_INDOORS_AIR)
								||(newRoom.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
								||(newRoom.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)))
							{
								if(newRoom.numInhabitants()>0)
									break;
								boolean foundBoardable = false;
								for (final Enumeration<Item> i = newRoom.items(); i.hasMoreElements();)
								{
									final Item I = i.nextElement();
									if ((I != null)
									&& (I.container() == null)
									&& (I instanceof Rideable))
									{
										foundBoardable=true;
										break;
									}
								}
								if (foundBoardable)
									break;
								R = newRoom.getRoomInDir(dir);
								E = newRoom.getExitInDir(dir);
								if ((R == null)
								|| (E == null)
								|| (!E.isOpen())
								|| (CMLib.flags().isHidden(E)))
									break;
								newRoom = R;
							}
						}
					}
				}
			}
			this.newRoom = newRoom;
		}
		else
		if((newRoom==null)
		||(newRoom.amDestroyed()))
		{
			if(uviewedRoomDir < 0)
				newRoom=CMLib.map().getRoom(uviewedRoomID);
			else
			{
				final Room hereR=CMLib.map().roomLocation(affected);
				if(hereR != null)
					newRoom=hereR.getRoomInDir(uviewedRoomDir);
			}
		}
		return newRoom;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)
		&&((affected instanceof Room)||(affected instanceof Exit)||(affected instanceof Item)))
		{
			if((msg.amITarget(affected))
			&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE)))
			{
				final Room newRoom=getNewRoom();
				if((newRoom==null)
				||(newRoom.fetchEffect(ID())!=null))
					return super.okMessage(myHost,msg);
				if(longlook)
				{
					if(msg.targetMinor()!=CMMsg.TYP_EXAMINE)
						return super.okMessage(myHost,msg);
					msg.addTrailerRunnable(new Runnable()
					{
						final Room R=newRoom;
						final CMMsg mmsg=msg;
						@Override
						public void run()
						{
							if(CMLib.flags().canBeSeenBy(R, mmsg.source()) && (mmsg.source().session()!=null))
								mmsg.source().session().print(L("In @x1 you can see:",R.displayText(mmsg.source())));
							final CMMsg msg2=CMClass.getMsg(mmsg.source(), R, mmsg.tool(), mmsg.sourceCode(), null, mmsg.targetCode(), null, mmsg.othersCode(), null);
							if((mmsg.source().isAttributeSet(MOB.Attrib.AUTOEXITS))
							&&(CMProps.getIntVar(CMProps.Int.EXVIEW)!=CMProps.Int.EXVIEW_PARAGRAPH))
								msg2.addTrailerMsg(CMClass.getMsg(mmsg.source(),R,null,CMMsg.MSG_LOOK_EXITS,null));
							if(R.okMessage(mmsg.source(), mmsg))
								R.send(mmsg.source(),msg2);
						}
					});
				}
				else
				{
					final CMMsg msg2=CMClass.getMsg(msg.source(),newRoom,msg.tool(),
								  msg.sourceCode(),msg.sourceMessage(),
								  msg.targetCode(),msg.targetMessage(),
								  msg.othersCode(),msg.othersMessage());
					if(newRoom.okMessage(msg.source(),msg2))
					{
						newRoom.executeMsg(msg.source(),msg2);
						return false;
					}
				}
			}
			else
			if((attack)
			&&(msg.sourceMinor()==CMMsg.TYP_COMMANDFAIL)
			&&(msg.source().location()==affected)
			&&(msg.targetMessage()!=null)
			&&(msg.targetMessage().length()>0))
			{
				switch(Character.toUpperCase(msg.targetMessage().charAt(0)))
				{
				case 'A':
				case 'K':
				{
					final List<String> parsedFail = CMParms.parse(msg.targetMessage());
					final String cmd=parsedFail.get(0).toUpperCase();
					if(("ATTACK".startsWith(cmd)||("KILL".startsWith(cmd)))
					&&(affected instanceof Room))
					{
						final String rest = CMParms.combine(parsedFail,1);
						final MOB sourceM=msg.source();
						final Room wasR=(Room)affected;
						final Room newRoom=getNewRoom();
						if((newRoom==null)
						||(newRoom.fetchEffect(ID())!=null))
							return super.okMessage(myHost,msg);
						final MOB M=newRoom.fetchInhabitant(rest);
						if((M!=null)
						&&(CMLib.flags().canBeSeenBy(M, sourceM))
						&&(wasR!=null))
						{
							if(!sourceM.mayIFight(M))
							{
								sourceM.tell(L("You are not permitted to attack @x1",M.name()));
								return false;
							}
							final Item I=sourceM.fetchWieldedItem();
							if((!(I instanceof Weapon))
							||((((Weapon)I).weaponClassification()!=Weapon.CLASS_RANGED)
								&&(((Weapon)I).weaponClassification()!=Weapon.CLASS_THROWN)))
							{
								sourceM.tell(L("You can't attack @x1 with @x2 from here.",M.name(sourceM),I.name(sourceM)));
								return false;
							}
							final Command C=CMClass.getCommand("Kill");
							final double actionCost = (C==null)?0:C.actionsCost(sourceM, new XVector<String>("Kill",rest));
							if((C==null)||(sourceM.actions()<=actionCost))
							{
								sourceM.tell(L("You aren't quite ready to attack just this second."));
								return false;
							}
							if(sourceM.isInCombat())
							{
								sourceM.tell(L("You are already in combat!"));
								return false;
							}
							try
							{
								newRoom.bringMobHere(sourceM, false);
								CMLib.combat().postAttack(sourceM, M, I);
								sourceM.setActions(sourceM.actions()-actionCost);
							}
							finally
							{
								wasR.bringMobHere(sourceM, false);
								sourceM.makePeace(true);
							}
							return false;
						}
					}
					break;
				}
				default:
					break;
				}
			}
		}
		return super.okMessage(myHost,msg);
	}
}
