package com.planet_ink.coffee_mud.core.database;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.database.DBConnector.DBPreparedBatchEntry;
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

import java.sql.*;
import java.util.*;

/**
 * Portions Copyright (c) 2003 Jeremy Vyska
 * Portions Copyright (c) 2004-2018 Bo Zimmerman
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ClanLoader
{
	protected DBConnector DB=null;
	public ClanLoader(DBConnector newDB)
	{
		DB=newDB;
	}

	protected int currentRecordPos=1;
	protected int recordCount=0;

	public void updateBootStatus(String loading)
	{
		CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Loading "+loading+" ("+currentRecordPos+" of "+recordCount+")");
	}
	
	public void DBReadClanItems(Map<String,Clan> clans)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCLIT");
			while(R.next())
			{
				final String clanID=DBConnections.getRes(R,"CMCLID");
				final Clan C=clans.get(clanID);
				if(C==null)
				{
					//Log.errOut("Clan","Couldn't find clan '"+clanID+"'");
				}
				else
				{
					final String itemID=DBConnections.getRes(R,"CMITID");
					final Item newItem=CMClass.getItem(itemID);
					if(newItem==null)
						Log.errOut("Clan","Couldn't find item '"+itemID+"'");
					else
					{
						String text=DBConnections.getResQuietly(R,"CMITTX");
						int roomX;
						if(text.startsWith("<ROOM") && ((roomX=text.indexOf("/>"))>=0))
						{
							final String roomXML=text.substring(0,roomX+2);
							text=text.substring(roomX+2);
							newItem.setMiscText(text);
							final List<XMLLibrary.XMLTag> xml=CMLib.xml().parseAllXML(roomXML);
							if((xml!=null)&&(xml.size()>0))
							{
								final String roomID=xml.get(0).parms().get("ID");
								final long expirationDate=CMath.s_long(xml.get(0).parms().get("EXPIRE"));
								if(roomID.startsWith("SPACE.") && (newItem instanceof SpaceObject))
									CMLib.map().addObjectToSpace((SpaceObject)newItem,CMParms.toLongArray(CMParms.parseCommas(roomID.substring(6), true)));
								else
								{
									final Room itemR=CMLib.map().getRoom(roomID);
									if(itemR!=null)
									{
										if(newItem instanceof BoardableShip)
											((BoardableShip)newItem).dockHere(itemR);
										else
											itemR.addItem(newItem);
										newItem.setExpirationDate(expirationDate);
									}
								}
							}
						}
						else
						{
							newItem.setMiscText(text);
						}
						newItem.setUsesRemaining((int)DBConnections.getLongRes(R,"CMITUR"));
						newItem.basePhyStats().setLevel((int)DBConnections.getLongRes(R,"CMITLV"));
						newItem.basePhyStats().setAbility((int)DBConnections.getLongRes(R,"CMITAB"));
						newItem.basePhyStats().setHeight((int)DBConnections.getLongRes(R,"CMHEIT"));
						newItem.recoverPhyStats();
						CMLib.map().registerWorldObjectLoaded(null, null, newItem);
						C.getExtItems().addItem(newItem);
					}
				}
				updateBootStatus("Clan Items");
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("Clan",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public List<Clan> DBRead()
	{
		List<Clan> clanList=new ArrayList<Clan>();
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCLAN");
			recordCount=DB.getRecordCount(D,R);
			final Map<String,Clan> clans=new Hashtable<String,Clan>();
			while(R.next())
			{
				currentRecordPos=R.getRow();
				final String name=DBConnections.getRes(R,"CMCLID");
				final Clan C=(Clan)CMClass.getCommon("DefaultClan");
				C.setName(name);
				C.setPremise(DBConnections.getRes(R,"CMDESC"));
				C.setAcceptanceSettings(DBConnections.getRes(R,"CMACPT"));
				C.setPolitics(DBConnections.getRes(R,"CMPOLI"));
				C.setRecall(DBConnections.getRes(R,"CMRCLL"));
				C.setDonation(DBConnections.getRes(R,"CMDNAT"));
				C.setStatus(CMath.s_int(DBConnections.getRes(R, "CMSTAT")));
				C.setMorgue(DBConnections.getRes(R,"CMMORG"));
				C.setTrophies(CMath.s_int(DBConnections.getRes(R, "CMTROP")));
				//CMLib.clans().addClan(C);
				clanList.add(C);
				clans.put(C.clanID(), C);
				updateBootStatus("Clans");
			}
			R.close();
		}
		catch(final Exception sqle)
		{
			Log.errOut("Clan",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		// log comment
		return clanList;
	}

	public void DBUpdate(Clan C)
	{
		final String sql="UPDATE CMCLAN SET "
				+"CMDESC='"+C.getPremise()+"',"
				+"CMACPT='"+C.getAcceptanceSettings()+"',"
				+"CMPOLI=?,"
				+"CMRCLL='"+C.getRecall()+"',"
				+"CMDNAT='"+C.getDonation()+"',"
				+"CMSTAT="+C.getStatus()+","
				+"CMMORG='"+C.getMorgue()+"',"
				+"CMTROP="+C.getTrophies()+""
				+" WHERE CMCLID='"+C.clanID()+"'";
		DB.updateWithClobs(sql, C.getPolitics());
	}

	protected String getDBItemUpdateString(final Clan C, final Item thisItem)
	{
		CMLib.catalog().updateCatalogIntegrity(thisItem);
		final String container=((thisItem.container()!=null)?(""+thisItem.container()):"");
		return "INSERT INTO CMCLIT (CMCLID, CMITNM, CMITID, CMITTX, CMITLO, CMITWO, "
		+"CMITUR, CMITLV, CMITAB, CMHEIT"
		+") values ('"+C.clanID()+"','"+(thisItem)+"','"+thisItem.ID()+"',?,'"+container+"',"+thisItem.rawWornCode()+","
		+thisItem.usesRemaining()+","+thisItem.basePhyStats().level()+","+thisItem.basePhyStats().ability()+","
		+thisItem.basePhyStats().height()+")";
	}

	private List<DBPreparedBatchEntry> getDBItemUpdateStrings(final Clan C)
	{
		final HashSet<String> done=new HashSet<String>();
		final List<DBPreparedBatchEntry> strings=new LinkedList<DBPreparedBatchEntry>();
		final ItemCollection coll=C.getExtItems();
		final List<Item> finalCollection=new LinkedList<Item>();
		final List<Item> extraItems=new LinkedList<Item>();
		for(int i=coll.numItems()-1;i>=0;i--)
		{
			final Item thisItem=coll.getItem(i);
			if((thisItem!=null)&&(!thisItem.amDestroyed()))
			{
				final Item cont=thisItem.ultimateContainer(null);
				if(cont.owner() instanceof Room)
					finalCollection.add(thisItem);
			}
		}
		for(final Item thisItem : finalCollection)
		{
			if(thisItem instanceof Container)
			{
				final List<Item> contents=((Container)thisItem).getDeepContents();
				for(final Item I : contents)
					if(!finalCollection.contains(I))
						extraItems.add(I);
			}
		}
		finalCollection.addAll(extraItems);
		for(final Item thisItem : finalCollection)
		{
			if(!done.contains(""+thisItem))
			{
				CMLib.catalog().updateCatalogIntegrity(thisItem);
				final Item cont=thisItem.ultimateContainer(null);
				final String sql=getDBItemUpdateString(C,thisItem);
				final String roomID=((cont.owner()==null)&&(thisItem instanceof SpaceObject)&&(CMLib.map().isObjectInSpace((SpaceObject)thisItem)))?
						("SPACE."+CMParms.toListString(((SpaceObject)thisItem).coordinates())):CMLib.map().getExtendedRoomID((Room)cont.owner());
				final String text="<ROOM ID=\""+roomID+"\" EXPIRE="+thisItem.expirationDate()+" />"+thisItem.text();
				strings.add(new DBPreparedBatchEntry(sql,text));
				done.add(""+thisItem);
			}
		}
		return strings;
	}

	public void DBUpdateItems(final Clan C)
	{
		if((C==null)||(C.clanID()==null)||(C.clanID().length()==0))
			return;
		final List<DBPreparedBatchEntry> statements=new LinkedList<DBPreparedBatchEntry>();
		statements.add(new DBPreparedBatchEntry("DELETE FROM CMCLIT WHERE CMCLID='"+C.clanID()+"'"));
		statements.addAll(getDBItemUpdateStrings(C));
		DB.updateWithClobs(statements);
	}

	public void DBCreate(Clan C)
	{
		if(C.clanID().length()==0)
			return;
		final String sql="INSERT INTO CMCLAN ("
			+"CMCLID,"
			+"CMTYPE,"
			+"CMDESC,"
			+"CMACPT,"
			+"CMPOLI,"
			+"CMRCLL,"
			+"CMDNAT,"
			+"CMSTAT,"
			+"CMMORG,"
			+"CMTROP"
			+") values ("
			+"'"+C.clanID()+"',"
			+"0,"
			+"'"+C.getPremise()+"',"
			+"'"+C.getAcceptanceSettings()+"',"
			+"?,"
			+"'"+C.getRecall()+"',"
			+"'"+C.getDonation()+"',"
			+""+C.getStatus()+","
			+"'"+C.getMorgue()+"',"
			+""+C.getTrophies()
			+")";
			DB.updateWithClobs(sql, C.getPolitics());
	}

	public void DBDelete(Clan C)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			D.update("DELETE FROM CMCLAN WHERE CMCLID='"+C.clanID()+"'",0);
			D.update("DELETE FROM CMCLIT WHERE CMCLID='"+C.clanID()+"'",0);
			D.update("DELETE FROM CMCHCL WHERE CMCLAN='"+C.clanID()+"'",0);
		}
		catch(final Exception sqle)
		{
			Log.errOut("Clan",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
	}

}
