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
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2007-2018 Bo Zimmerman

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

public class Prop_Artifact extends Property
{
	@Override
	public String ID()
	{
		return "Prop_Artifact";
	}

	@Override
	public String name()
	{
		return "Artifact";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	protected static final Map<String, Item>	registeredArtifacts	= new Hashtable<String, Item>();
	
	private String	itemID		= null;
	private boolean	autodrop	= true;
	private boolean	nocast		= true;
	private boolean	nolocate	= true;
	private boolean	nomobs		= true;
	private boolean	autoreset	= false;
	private long	waitToReload= 0;

	private String getItemID()
	{
		if(itemID==null)
		{
			final int x=miscText.indexOf(';');
			if(x>=0)
				itemID=miscText.substring(x+1);
			else
			if(affected!=null)
			{
				itemID = affected+"_"+Math.random();
				miscText += ";" + itemID;
			}
		}
		return itemID;
	}

	@Override
	public String text()
	{
		if((miscText==null)||(miscText.length()==0))
			getItemID();
		return miscText;
	}

	@Override
	public void setMiscText(String text)
	{
		super.setMiscText(text);
		if(text.startsWith("BOOT;"))
		{
			itemID=text.substring(5);
			waitToReload=System.currentTimeMillis()+5000;
			return;
		}
		final int x=text.indexOf(';');
		if(x>=0)
		{
			itemID=text.substring(x+1);
			text=text.substring(0,x);
		}
		else
		if(affected!=null)
		{
			itemID=""+affected+"_"+Math.random();
			super.setMiscText(text+";"+itemID);
		}
		autodrop=CMParms.getParmBool(text,"AUTODROP",true);
		nocast=CMParms.getParmBool(text,"NOCAST",true);
		nolocate=CMParms.getParmBool(text,"NOLOCATE",true);
		nomobs=CMParms.getParmBool(text,"NOMOBS",true);
		autoreset=CMParms.getParmBool(text,"AUTORESET",false);
	}

	/**
	 * the purpose of this is to determine if the
	 * source of the call to this method includes the
	 * Destroy command.  If so, we have a winner.
	 */
	public void deleteFromDB()
	{
		if((affected!=null)&&(getItemID().length()>0))
		{
			try
			{
				final java.io.ByteArrayOutputStream o=new java.io.ByteArrayOutputStream();
				new Exception().printStackTrace(new java.io.PrintStream(o));
				o.close();
				if(o.toString().indexOf("Commands.Destroy.execute")>=0)
					CMLib.database().DBDeletePlayerData(getItemID(),"ARTIFACTS","ARTIFACTS/"+getItemID());
			}
			catch (final Exception e)
			{
			}
		}
	}

	@Override
	public void unInvoke()
	{
		deleteFromDB();
		super.unInvoke();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof Item))
			return false;
		if((msg.targetMinor()==CMMsg.TYP_EXPIRE)
		&&(msg.target() instanceof Room)
		&&(((Room)msg.target()).isContent((Item)affected)))
			return false;
		if((msg.target()!=null)
		&&((msg.target()==affected)
			||(msg.target()==((Item)affected).container())
			||(msg.target()==((Item)affected).ultimateContainer(null))))
		{
			if((nomobs)
			&&(msg.targetMinor()==CMMsg.TYP_GET)
			&&(msg.source().isMonster()))
			{
				msg.source().tell(L("You are not allowed to possess @x1",affected.Name()));
				return false;
			}
			if(nocast&&((CMath.bset(msg.sourceMajor(),CMMsg.MASK_MAGIC))
			||(CMath.bset(msg.targetMajor(),CMMsg.MASK_MAGIC))
			||(CMath.bset(msg.othersMajor(),CMMsg.MASK_MAGIC))))
			{
				Room room=null;
				if(affected instanceof Room)
					room=(Room)affected;
				else
				if(msg.source().location()!=null)
					room=msg.source().location();
				else
				if((msg.target() instanceof MOB)
				&&(((MOB)msg.target()).location()!=null))
					room=((MOB)msg.target()).location();
				if(room==null)
					room=CMLib.map().roomLocation(affected);
				if(room!=null)
					room.showHappens(CMMsg.MSG_OK_VISUAL,L("Magic energy fizzles around @x1 and is absorbed into the air.",affected.Name()));
				return false;
			}
			else
			if(nocast&&(msg.tool() instanceof Ability))
			{
				msg.source().tell(L("That doesn't appear to work on @x1",affected.name()));
				return false;
			}
		}

		if((msg.sourceMinor()==CMMsg.TYP_QUIT)
		&&(msg.source()==((Item)affected).owner())
		&&(autodrop))
		{
			msg.source().tell(L("^HYou lose your hold over @x1^?",affected.name()));
			final Room R=CMLib.map().roomLocation(msg.source());
			R.moveItemTo((Item)affected);
			if(autoreset)
			{
				waitToReload=System.currentTimeMillis()+60000;
				if(!CMLib.threads().isTicking(this,Tickable.TICKID_ITEM_BOUNCEBACK))
					CMLib.threads().startTickDown(this, Tickable.TICKID_ITEM_BOUNCEBACK,4);
				final Physical P = affected;
				P.delEffect(this);
				P.destroy();
			}
		}
		return super.okMessage(myHost, msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(!(affected instanceof Item))
			return;
		if(((msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
			||(msg.sourceMinor()==CMMsg.TYP_ROOMRESET))
		&&(getItemID()!=null))
		{
			final Item I=(Item)affected;
			final Room R=CMLib.map().roomLocation(I);
			if(msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
			{
				waitToReload=0;
				if(autodrop)
				{
					if((I.owner() instanceof MOB)
					&&((!((MOB)I.owner()).isMonster())||(CMLib.players().getPlayer(((MOB)I.owner()).Name())!=null)))
						R.moveItemTo(I);
				}
			}
			else
			{
				waitToReload=System.currentTimeMillis()+60000;
				if(!CMLib.threads().isTicking(this,Tickable.TICKID_ITEM_BOUNCEBACK))
					CMLib.threads().startTickDown(this, Tickable.TICKID_ITEM_BOUNCEBACK,4);
			}
			if((R!=null)
			&&(!I.amDestroyed()))
			{
				if(autoreset)
				{
					final List<PlayerData> itemSet=CMLib.database().DBReadPlayerData(getItemID(),"ARTIFACTS","ARTIFACTS/"+getItemID());
					if((itemSet!=null)&&(itemSet.size()>0))
						return;
				}
				final StringBuffer data=new StringBuffer("");
				data.append("<ARTITEM>");
				if(I.owner() instanceof Room)
					data.append(CMLib.xml().convertXMLtoTag("ROOMID",CMLib.map().getExtendedRoomID(R)));
				else
				if(I.owner() instanceof MOB)
				{
					final MOB M=(MOB)I.owner();
					if(M.getStartRoom()!=null)
					{
						data.append(CMLib.xml().convertXMLtoTag("ROOMID",CMLib.map().getExtendedRoomID(M.getStartRoom())));
						data.append(CMLib.xml().convertXMLtoTag("MOB",((MOB)I.owner()).Name()));
					}
					else
						data.append(CMLib.xml().convertXMLtoTag("ROOMID",CMLib.map().getExtendedRoomID(R)));
				}
				else
					data.append(CMLib.xml().convertXMLtoTag("ROOMID",CMLib.map().getExtendedRoomID(R)));
				data.append(CMLib.xml().convertXMLtoTag("ICLAS",CMClass.classID(I)));
				data.append(CMLib.xml().convertXMLtoTag("IREJV",I.basePhyStats().rejuv()));
				data.append(CMLib.xml().convertXMLtoTag("IUSES",I.usesRemaining()));
				data.append(CMLib.xml().convertXMLtoTag("ILEVL",I.basePhyStats().level()));
				data.append(CMLib.xml().convertXMLtoTag("IABLE",I.basePhyStats().ability()));
				data.append(CMLib.xml().convertXMLtoTag("ITEXT",CMLib.xml().parseOutAngleBrackets(I.text())));
				data.append("</ARTITEM>");
				I.destroy();
				CMLib.database().DBReCreatePlayerData(getItemID(),"ARTIFACTS","ARTIFACTS/"+getItemID(),data.toString());
			}
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((waitToReload>0)&&(tickID==Tickable.TICKID_ITEM_BOUNCEBACK))
		{
			if(System.currentTimeMillis()>waitToReload)
			{
				waitToReload=0;
				if((affected instanceof Item)
				&&(!affected.amDestroyed())
				&&(((Item)affected).owner() instanceof MOB)
				&&(!((MOB)((Item)affected).owner()).isMonster())
				&&(CMLib.flags().isInTheGame((Item)affected,true)))
					return false;

				final List<PlayerData> itemSet=CMLib.database().DBReadPlayerData(getItemID(),"ARTIFACTS","ARTIFACTS/"+getItemID());
				if((itemSet!=null)&&(itemSet.size()>0))
				{
					// does it already exist?
					if(registeredArtifacts.containsKey(getItemID()))
						registeredArtifacts.remove(getItemID());
					final String data=itemSet.get(0).xml();
					final List<XMLLibrary.XMLTag> xml=CMLib.xml().parseAllXML(data);
					if(xml!=null)
					{
						for(int c=0;c<xml.size();c++)
						{
							final XMLTag iblk=xml.get(c);
							if((iblk.tag().equalsIgnoreCase("ARTITEM"))&&(iblk.contents()!=null))
							{
								final List<XMLLibrary.XMLTag> roomData=iblk.contents();
								final String roomID=CMLib.xml().getValFromPieces(roomData,"ROOMID");
								final String MOBname=CMLib.xml().getValFromPieces(roomData,"MOB");
								final Room R=CMLib.map().getRoom(roomID);
								if(R!=null)
								{
									final String iClass=CMLib.xml().getValFromPieces(roomData,"ICLAS");
									final Item newItem=CMClass.getItem(iClass);
									final HashSet<MOB> doneMOBs=new HashSet<MOB>();
									if(newItem!=null)
									{
										newItem.basePhyStats().setLevel(CMLib.xml().getIntFromPieces(roomData,"ILEVL"));
										newItem.basePhyStats().setAbility(CMLib.xml().getIntFromPieces(roomData,"IABLE"));
										newItem.basePhyStats().setRejuv(CMLib.xml().getIntFromPieces(roomData,"IREJV"));
										newItem.setUsesRemaining(CMLib.xml().getIntFromPieces(roomData,"IUSES"));
										newItem.setMiscText(CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(roomData,"ITEXT")));
										newItem.recoverPhyStats();
										if(!registeredArtifacts.containsKey(getItemID()))
											registeredArtifacts.put(getItemID(),newItem);
										else
											Log.errOut("Prop_Artifact","Possible duplicate artifact: "+newItem.name());
										MOB foundMOB=null;
										if(MOBname.length()>0)
										{
											for(int i=0;i<R.numInhabitants();i++)
											{
												final MOB M=R.fetchInhabitant(i);
												if((M!=null)
												&&(M.isMonster())
												&&(M.name().equals(MOBname))
												&&(M.getStartRoom()==R)
												&&(!doneMOBs.contains(M)))
												{
													foundMOB=M;
													break;
												}
											}
										}
										final Area A=R.getArea();
										if((foundMOB==null)&&(MOBname.length()>0))
										{
											for(final Enumeration<Room> e=A.getMetroMap();e.hasMoreElements();)
											{
												final Room R2=e.nextElement();
												for(int i=0;i<R2.numInhabitants();i++)
												{
													final MOB M=R2.fetchInhabitant(i);
													if((M!=null)
													&&(M.isMonster())
													&&(M.name().equals(MOBname))
													&&(M.getStartRoom()==R)
													&&(!doneMOBs.contains(M)))
													{
														foundMOB = M;
														break;
													}
												}
											}
										}
										final Item newItemMinusArtifact=(Item)newItem.copyOf();
										Ability A2=newItemMinusArtifact.fetchEffect(ID());
										if(A2!=null)
											newItemMinusArtifact.delEffect(A2);
										Item I=null;
										if(foundMOB!=null)
										{
											for(int i=0;i<foundMOB.numItems();i++)
											{
												I=foundMOB.getItem(i);
												if(I==null)
													break;
												if(I.Name().equals(newItemMinusArtifact.Name()))
												{
													I=(Item)I.copyOf();
													A2=I.fetchEffect(ID());
													if(A2!=null)
														I.delEffect(A2);
													if(newItemMinusArtifact.sameAs(I))
														I.destroy();
												}
											}
											foundMOB.addItem(newItem);
											newItem.wearAt(newItem.rawProperLocationBitmap());
										}
										else
										if(MOBname.length()==0)
										{
											for(int i=0;i<R.numItems();i++)
											{
												I=R.getItem(i);
												if(I==null)
													break;
												if(I.Name().equals(newItemMinusArtifact.Name()))
												{
													I=(Item)I.copyOf();
													A2=I.fetchEffect(ID());
													if(A2!=null)
														I.delEffect(A2);
													if(newItemMinusArtifact.sameAs(I))
														I.destroy();
												}
											}
											R.addItem(newItem);
										}
										else
										{
											Log.errOut("Prop_Artifact","Unable to reset: "+getItemID()+" to "+MOBname+" in "+CMLib.map().getDescriptiveExtendedRoomID(R));
											waitToReload=System.currentTimeMillis()+10*60000;
											return true;
										}
									}
								}
								else
									Log.errOut("Prop_Artifact","Unknown artifact room artifact: "+roomID+" for "+getItemID());
							}
						}
					}
				}
				// my work is done, I can go away.
				return false;
			}
			Log.errOut("Prop_Artifact","No Artifact record for: "+getItemID());
			return true;
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof Item)
		{
			if(nolocate)
				affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.SENSE_UNLOCATABLE);
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.SENSE_UNDESTROYABLE);
			if(((Item)affected).subjectToWearAndTear())
				((Item)affected).setUsesRemaining(100);
			((Item)affected).setExpirationDate(0);
		}
	}
}
