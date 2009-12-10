package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/*
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Prop_Artifact extends Property
{
	public String ID() { return "Prop_Artifact"; }
	public String name(){ return "Artifact";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected static final Hashtable registeredArtifacts=new Hashtable();
	private String itemID=null;
	private boolean autodrop=true;
	private boolean nocast=true;
	private boolean nolocate=true;
	private boolean nomobs=true;
	private boolean autoreset=false;
	private long waitToReload=0;

	private String getItemID()
	{
		if(itemID==null)
		{
			int x=miscText.indexOf(";");
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
	
	public String text()
	{
		if((miscText==null)||(miscText.length()==0))
			getItemID();
		return miscText;
	}

	public void setMiscText(String text)
	{
		super.setMiscText(text);
		if(text.startsWith("BOOT;"))
		{
			itemID=text.substring(5);
			waitToReload=System.currentTimeMillis()+5000;
			return;
		}
		int x=text.indexOf(";");
		if(x>=0)
		{
			itemID=text.substring(x+1);
			text=text.substring(0,x);
		}
		else
		if(affected!=null)
		{
			itemID=""+affected+"_"+Math.random();
			super.setMiscText(text()+";"+itemID);
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
			try{
				java.io.ByteArrayOutputStream o=new java.io.ByteArrayOutputStream();
				new Exception().printStackTrace(new java.io.PrintStream(o));
				o.close();
				if(o.toString().indexOf("Commands.Destroy.execute")>=0)
	    			CMLib.database().DBDeleteData(getItemID(),"ARTIFACTS","ARTIFACTS/"+getItemID());
			}catch(Exception e){}
		}
	}
	
	public void unInvoke()
	{
		deleteFromDB();
		super.unInvoke();
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(affected instanceof Item)) return false;
		if((msg.targetMinor()==CMMsg.TYP_EXPIRE)
		&&(msg.target() instanceof Room)
		&&(((Room)msg.target()).isContent((Item)affected)))
			return false;
		if((msg.target()!=null)
		&&((msg.target()==affected)
			||(msg.target()==((Item)affected).container())
			||(msg.target()==((Item)affected).ultimateContainer())))
		{
			if((nomobs)
			&&(msg.targetMinor()==CMMsg.TYP_GET)
			&&(msg.source().isMonster()))
			{
				msg.source().tell("You are not allowed to possess "+affected.Name());
				return false;
			}
			if(nocast&&((CMath.bset(msg.sourceCode(),CMMsg.MASK_MAGIC))
			||(CMath.bset(msg.targetCode(),CMMsg.MASK_MAGIC))
			||(CMath.bset(msg.othersCode(),CMMsg.MASK_MAGIC))))
			{
				Room room=null;
				if(affected instanceof Room)
					room=(Room)affected;
				else
				if((msg.source()!=null)
				&&(msg.source().location()!=null))
					room=msg.source().location();
				else
				if((msg.target()!=null)
				&&(msg.target() instanceof MOB)
				&&(((MOB)msg.target()).location()!=null))
					room=((MOB)msg.target()).location();
				if(room==null) room=CMLib.map().roomLocation(affected);
				if(room!=null)
					room.showHappens(CMMsg.MSG_OK_VISUAL,"Magic energy fizzles around "+affected.Name()+" and is absorbed into the air.");
				return false;
			}
			else
			if(nocast&&(msg.tool() instanceof Ability))
			{
				msg.source().tell("That doesn't appear to work on "+affected.name());
				return false;
			}
		}

		if((msg.sourceMinor()==CMMsg.TYP_QUIT)
		&&(msg.source()==((Item)affected).owner()))
		{
			msg.source().tell("^HYou lose your hold over "+affected.name()+"^?");
			Room R=CMLib.map().roomLocation(msg.source());
			R.bringItemHere((Item)affected,0,false);
			if(autoreset)
			{
				waitToReload=System.currentTimeMillis()+60000;
				if(!CMLib.threads().isTicking(this,Tickable.TICKID_ITEM_BOUNCEBACK))
					CMLib.threads().startTickDown(this, Tickable.TICKID_ITEM_BOUNCEBACK,4);
				Environmental E = affected;
				E.delEffect(this);
				E.destroy();
			}
		}
		return super.okMessage(myHost, msg);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(!(affected instanceof Item)) return;
		if(((msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
			||(msg.sourceMinor()==CMMsg.TYP_ROOMRESET))
		&&(getItemID()!=null))
		{
			Item I=(Item)affected;
			Room R=CMLib.map().roomLocation(I);
			if(msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
			{
				waitToReload=0;
				if(autodrop)
				{
					if((I.owner() instanceof MOB)
					&&((!((MOB)I.owner()).isMonster())||(CMLib.players().getPlayer(((MOB)I.owner()).Name())!=null)))
						R.bringItemHere(I,0,false);
				}
			}
			else
			{
				waitToReload=System.currentTimeMillis()+60000;
				if(!CMLib.threads().isTicking(this,Tickable.TICKID_ITEM_BOUNCEBACK))
					CMLib.threads().startTickDown(this, Tickable.TICKID_ITEM_BOUNCEBACK,4);
			}
			if((R!=null)
			&&(!((Item)I).amDestroyed()))
			{
				if(autoreset)
				{
					Vector itemSet=CMLib.database().DBReadData(getItemID(),"ARTIFACTS","ARTIFACTS/"+getItemID());
					if((itemSet!=null)&&(itemSet.size()>0))
						return;
				}
				StringBuffer data=new StringBuffer("");
				data.append("<ARTITEM>");
				if(((Item)I).owner() instanceof Room)
					data.append(CMLib.xml().convertXMLtoTag("ROOMID",CMLib.map().getExtendedRoomID(R)));
				else
				if(((Item)I).owner() instanceof MOB)
				{
					MOB M=(MOB)((Item)I).owner();
					if(M.getStartRoom()!=null)
					{
						data.append(CMLib.xml().convertXMLtoTag("ROOMID",CMLib.map().getExtendedRoomID(M.getStartRoom())));
						data.append(CMLib.xml().convertXMLtoTag("MOB",((MOB)((Item)I).owner()).Name()));
					}
					else
						data.append(CMLib.xml().convertXMLtoTag("ROOMID",CMLib.map().getExtendedRoomID(R)));
				}
				else
					data.append(CMLib.xml().convertXMLtoTag("ROOMID",CMLib.map().getExtendedRoomID(R)));
				data.append(CMLib.xml().convertXMLtoTag("ICLAS",CMClass.classID(I)));
				data.append(CMLib.xml().convertXMLtoTag("IREJV",I.baseEnvStats().rejuv()));
				data.append(CMLib.xml().convertXMLtoTag("IUSES",((Item)I).usesRemaining()));
				data.append(CMLib.xml().convertXMLtoTag("ILEVL",I.baseEnvStats().level()));
				data.append(CMLib.xml().convertXMLtoTag("IABLE",I.baseEnvStats().ability()));
				data.append(CMLib.xml().convertXMLtoTag("ITEXT",CMLib.xml().parseOutAngleBrackets(I.text())));
				data.append("</ARTITEM>");
                ((Item)I).destroy();
    			CMLib.database().DBReCreateData(getItemID(),"ARTIFACTS","ARTIFACTS/"+getItemID(),data.toString());
			}
		}
	}

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
				&&(CMLib.flags().isInTheGame(affected,true)))
					return false;

				Vector itemSet=CMLib.database().DBReadData(getItemID(),"ARTIFACTS","ARTIFACTS/"+getItemID());
				if((itemSet!=null)&&(itemSet.size()>0))
				{
					// does it already exist?
					if(registeredArtifacts.containsKey(getItemID())) registeredArtifacts.remove(getItemID());
					String data=((DatabaseEngine.PlayerData)itemSet.firstElement()).xml;
					Vector xml=CMLib.xml().parseAllXML(data);
					if(xml!=null)
						for(int c=0;c<xml.size();c++)
						{
							XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)xml.elementAt(c);
							if((iblk.tag.equalsIgnoreCase("ARTITEM"))&&(iblk.contents!=null))
							{
								Vector roomData=iblk.contents;
								String roomID=CMLib.xml().getValFromPieces(roomData,"ROOMID");
								String MOBname=CMLib.xml().getValFromPieces(roomData,"MOB");
								Room R=CMLib.map().getRoom(roomID);
								if(R!=null)
								{
									String iClass=CMLib.xml().getValFromPieces(roomData,"ICLAS");
									Item newItem=CMClass.getItem(iClass);
									HashSet doneMOBs=new HashSet();
									if(newItem!=null)
									{
										newItem.baseEnvStats().setLevel(CMLib.xml().getIntFromPieces(roomData,"ILEVL"));
										newItem.baseEnvStats().setAbility(CMLib.xml().getIntFromPieces(roomData,"IABLE"));
										newItem.baseEnvStats().setRejuv(CMLib.xml().getIntFromPieces(roomData,"IREJV"));
										newItem.setUsesRemaining(CMLib.xml().getIntFromPieces(roomData,"IUSES"));
										newItem.setMiscText(CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(roomData,"ITEXT")));
										newItem.recoverEnvStats();
										if(!registeredArtifacts.containsKey(getItemID()))
											registeredArtifacts.put(getItemID(),newItem);
										else
											Log.errOut("Prop_Artifact","Possible duplicate artifact: "+newItem.name());
										MOB foundMOB=null;
										if(MOBname.length()>0)
											for(int i=0;i<R.numInhabitants();i++)
											{
												MOB M=R.fetchInhabitant(i);
												if((M!=null)
												&&(M.isMonster())
												&&(M.name().equals(MOBname))
												&&(M.getStartRoom()==R)
												&&(!doneMOBs.contains(M)))
												{ foundMOB=M; break;}
											}
										Area A=R.getArea();
										if((foundMOB==null)&&(MOBname.length()>0))
											for(Enumeration e=A.getMetroMap();e.hasMoreElements();)
											{
												Room R2=(Room)e.nextElement();
												for(int i=0;i<R2.numInhabitants();i++)
												{
													MOB M=R2.fetchInhabitant(i);
													if((M!=null)
													&&(M.isMonster())
													&&(M.name().equals(MOBname))
													&&(M.getStartRoom()==R)
													&&(!doneMOBs.contains(M)))
													{ foundMOB=M; break;}
												}
											}
                                        Item newItemMinusArtifact=(Item)newItem.copyOf();
                                        Ability A2=newItemMinusArtifact.fetchEffect(ID());
                                        if(A2!=null) newItemMinusArtifact.delEffect(A2);
                                        Item I=null;
										if(foundMOB!=null)
										{
	                                        for(int i=0;i<foundMOB.inventorySize();i++)
	                                        {
	                                        	I=foundMOB.fetchInventory(i);
	                                        	if(I==null) break;
	                                        	if(I.Name().equals(newItemMinusArtifact.Name()))
	                                        	{
	                                        		I=(Item)I.copyOf();
	                                                A2=I.fetchEffect(ID());
	                                                if(A2!=null) I.delEffect(A2);
		                                            if(newItemMinusArtifact.sameAs(I))
		                                            	I.destroy();
	                                        	}
	                                        }
    										foundMOB.addInventory(newItem);
    										newItem.wearAt(newItem.rawProperLocationBitmap());
										}
										else
										if(MOBname.length()==0)
										{
	                                        for(int i=0;i<R.numItems();i++)
	                                        {
	                                        	I=R.fetchItem(i);
	                                        	if(I==null) break;
	                                        	if(I.Name().equals(newItemMinusArtifact.Name()))
	                                        	{
	                                        		I=(Item)I.copyOf();
	                                                A2=I.fetchEffect(ID());
	                                                if(A2!=null) I.delEffect(A2);
		                                            if(newItemMinusArtifact.sameAs(I))
		                                            	I.destroy();
	                                        	}
	                                        }
											R.addItem(newItem);
										}
										else
										{
                                        	Log.errOut("Prop_Artifact","Unable to reset: "+getItemID()+" to "+MOBname+" in "+CMLib.map().getExtendedRoomID(R));
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
				// my work is done, I can go away.
				return false;
			}
			Log.errOut("Prop_Artifact","No Artifact record for: "+getItemID());
			return true;
		}
		return super.tick(ticking,tickID);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected instanceof Item)
		{
			if(nolocate)
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.SENSE_UNLOCATABLE);
			if(((Item)affected).subjectToWearAndTear()) ((Item)affected).setUsesRemaining(100);
			((Item)affected).setExpirationDate(0);
		}
	}
}