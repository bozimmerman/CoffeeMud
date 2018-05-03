package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.database.DBConnector.DBPreparedBatchEntry;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.TechComponent.ShipDir;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.CatalogLibrary.CataData;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;

/*
   Copyright 2004-2018 Bo Zimmerman

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
public class CoffeeMaker extends StdLibrary implements GenericBuilder
{
	@Override
	public String ID()
	{
		return "CoffeeMaker";
	}
	
	@Override
	public String getGenMOBTextUnpacked(MOB mob, String newText)
	{
		if((newText!=null)&&((newText.length()>10)||newText.startsWith("%DBID>")))
		{
			if(newText.startsWith("%DBID>"))
			{
				int x=newText.indexOf('@');
				final String dbstr=CMLib.database().DBReadRoomMOBMiscText(newText.substring(6,x),
																  ((Object)mob).getClass().getName()+newText.substring(x).trim());
				if(dbstr!=null)
					return dbstr;
				Log.errOut("Unable to re-read mob data: "+newText);
				return null;
			}
			return newText;
		}
		return null;
	}

	@Override
	public void resetGenMOB(MOB mob, String newText)
	{
		newText=getGenMOBTextUnpacked(mob,newText);
		if(newText!=null)
			setPropertiesStr(mob,newText,false);

		mob.recoverPhyStats();
		mob.recoverCharStats();
		mob.baseState().setHitPoints(CMLib.dice().rollHP(mob.basePhyStats().level(),mob.basePhyStats().ability()));
		mob.baseState().setMana(CMLib.leveler().getLevelMana(mob));
		mob.baseState().setMovement(CMLib.leveler().getLevelMove(mob));
		mob.recoverMaxState();
		mob.resetToMaxState();
		if(mob.getWimpHitPoint()>0)
			mob.setWimpHitPoint((int)Math.round(CMath.mul(mob.curState().getHitPoints(),.10)));
		mob.setExperience(CMLib.leveler().getLevelExperience(mob.phyStats().level()-1)+500);
	}

	@Override
	public int envFlags(Environmental E)
	{
		int f=0;
		if(E instanceof Item)
		{
			final Item item=(Item)E;
			if(!CMath.bset(item.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMNODROP))
				f=f|1;
			if(!CMath.bset(item.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMNOTGET))
				f=f|2;
			if(CMath.bset(item.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMREADABLE))
				f=f|4;
			if(!CMath.bset(item.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMNOREMOVE))
				f=f|8;
		}
		else
		if(E instanceof Exit)
		{
			final Exit exit=(Exit)E;
			if(exit.isReadable())
				f=f|4;
		}
		if(E instanceof CloseableLockable)
		{
			f = f | 16; // new style flag
			final CloseableLockable container=(CloseableLockable)E;
			if(container.hasADoor())
				f=f|32;
			if(container.hasALock())
				f=f|64;
			if(container.defaultsClosed())
				f=f|128;
			if(container.defaultsLocked())
				f=f|256;
		}
		return f;
	}

	@Override
	public void setEnvFlags(Environmental E, int f)
	{
		if(E instanceof Item)
		{
			final Item item=(Item)E;
			// deprecated, but unfortunately, its here to stay.
			CMLib.flags().setDroppable(item,CMath.bset(f,1));
			CMLib.flags().setGettable(item,CMath.bset(f,2));
			CMLib.flags().setReadable(item,CMath.bset(f,4));
			CMLib.flags().setRemovable(item,CMath.bset(f,8));
		}
		else
		if(E instanceof Exit)
		{
			final Exit exit=(Exit)E;
			exit.setReadable(CMath.bset(f,4));
		}
		
		if(E instanceof CloseableLockable)
		{
			final CloseableLockable container=(CloseableLockable)E;
			if((CMath.bset(f, 16))||(E instanceof Exit)) // this will be a 'new method' flag
			{
				final boolean hasDoor=CMath.bset(f,32);
				final boolean hasLock=CMath.bset(f,64);
				final boolean defaultsClosed=CMath.bset(f,128);
				final boolean defaultsLocked=CMath.bset(f,256);
				container.setDoorsNLocks(hasDoor,(!hasDoor)||(!defaultsClosed),defaultsClosed,hasLock,hasLock&&defaultsLocked,defaultsLocked);
			}
			else
			{
				final boolean hasDoor=CMath.bset(f,32);
				final boolean hasLock=CMath.bset(f,64);
				container.setDoorsNLocks(hasDoor,!hasDoor,hasDoor,hasLock,hasLock,hasLock);
			}
		}
	}

	@Override
	public String getGenAbilityXML(Ability A)
	{
		return new StringBuilder("<ABILITY ID=\"").append(A.ID()).append("\" TYPE=\"").append(CMClass.getSimpleClassName(A)).append("\">")
		   .append(A.getStat("ALLXML"))
		   .append("</ABILITY>").toString();
	}

	@Override
	public String getPropertiesStr(Environmental E, boolean fromTop)
	{
		if(E==null)
		{
			Log.errOut("CoffeeMaker","getPropertiesStr: null 'E'");
			return "";
		}
		if(E instanceof SpaceObject)
		{
			final StringBuilder str=new StringBuilder("");
			str.append(CMLib.xml().convertXMLtoTag("SSRADIUS",((SpaceObject)E).radius()));
			str.append(CMLib.xml().convertXMLtoTag("SSCOORDS",CMParms.toListString(((SpaceObject)E).coordinates())));
			str.append(CMLib.xml().convertXMLtoTag("SSDIR",CMParms.toListString(((SpaceObject)E).direction())));
			str.append(CMLib.xml().convertXMLtoTag("SSSPEED",Math.round(((SpaceObject)E).speed())));
			return str.append((E.isGeneric()?getGenPropertiesStr(E):"") + (fromTop?getOrdPropertiesStr(E):"")).toString();
		}
		else
			return (E.isGeneric()?getGenPropertiesStr(E):"") + (fromTop?getOrdPropertiesStr(E):"");
	}

	@Override
	public void doGenPropertiesCopy(Environmental fromE, Environmental toE)
	{
		final String xml = getGenPropertiesStr(fromE) + getOrdPropertiesStr(fromE);
		final List<XMLLibrary.XMLTag> xmlV = CMLib.xml().parseAllXML(xml);
		this.setGenPropertiesStr(toE, xmlV);
		this.setOrdPropertiesStr(toE, xmlV);
		if(toE instanceof Physical)
			recoverPhysical((Physical)toE);
	}
	
	protected String getOrdPropertiesStr(Environmental E)
	{
		final StringBuilder str=new StringBuilder("");
		if(E instanceof Room)
		{
			str.append(CMLib.xml().convertXMLtoTag("RCLIM", ((Room)E).getClimateTypeCode()));
			str.append(CMLib.xml().convertXMLtoTag("RATMO", ((Room)E).getAtmosphereCode()));
			if(E instanceof GridLocale)
			{
				str.append(CMLib.xml().convertXMLtoTag("XGRID",((GridLocale)E).xGridSize()));
				str.append(CMLib.xml().convertXMLtoTag("YGRID",((GridLocale)E).yGridSize()));
			}
			if(E instanceof LocationRoom)
				str.append(CMLib.xml().convertXMLtoTag("COREDIR",CMParms.toListString(((LocationRoom)E).getDirectionFromCore())));
			str.append(getExtraEnvPropertiesStr(E));
			str.append(getGenScripts((Room)E,false));
		}
		else
		if(E instanceof Area)
		{
			final Area myArea=(Area)E;
			final StringBuilder parentstr = new StringBuilder();
			final StringBuilder childrenstr = new StringBuilder();
			str.append(CMLib.xml().convertXMLtoTag("ARCHP",myArea.getArchivePath()));
			final Area defaultParentArea=CMLib.map().getDefaultParentArea();
			for(final Enumeration<Area> e=myArea.getParents(); e.hasMoreElements();)
			{
				final Area A=e.nextElement();
				if((A!=defaultParentArea)||(A.getTimeObj()!=CMLib.time().globalClock()))
				{
					parentstr.append("<PARENT>");
					parentstr.append(CMLib.xml().convertXMLtoTag("PARENTNAMED", A.name()));
					parentstr.append("</PARENT>");
				}
			}
			str.append(CMLib.xml().convertXMLtoTag("PARENTS",parentstr.toString()));
			for(final Enumeration<Area> e=myArea.getChildren(); e.hasMoreElements();)
			{
				final Area A=e.nextElement();
				childrenstr.append("<CHILD>");
				childrenstr.append(CMLib.xml().convertXMLtoTag("CHILDNAMED", A.name()));
				childrenstr.append("</CHILD>");
			}
			str.append(CMLib.xml().convertXMLtoTag("CHILDREN",childrenstr.toString()));
			str.append(getExtraEnvPropertiesStr(E));
			str.append(getGenScripts((Area)E,false));
			str.append(CMLib.xml().convertXMLtoTag("AUTHOR",myArea.getAuthorID()));
			str.append(CMLib.xml().convertXMLtoTag("CURRENCY",myArea.getCurrency()));
			if(myArea instanceof BoardableShip)
				str.append(CMLib.xml().convertXMLtoTag("DISP",CMLib.xml().parseOutAngleBrackets(myArea.displayText())));
			final Vector<String> V=new Vector<String>();
			String flag=null;
			for(final Enumeration<String> f=myArea.areaBlurbFlags();f.hasMoreElements();)
			{
				flag=f.nextElement();
				V.addElement((flag+" "+myArea.getBlurbFlag(flag)).trim());
			}
			str.append(CMLib.xml().convertXMLtoTag("BLURBS",CMLib.xml().getXMLList(V)));
			str.append(CMLib.xml().convertXMLtoTag("AATMO",((Area)E).getAtmosphereCode()));
			if(E instanceof GridZones)
				str.append(CMLib.xml().convertXMLtoTag("XGRID",((GridZones)E).xGridSize())
						  +CMLib.xml().convertXMLtoTag("YGRID",((GridZones)E).yGridSize()));
			if(E instanceof AutoGenArea)
			{
				str.append(CMLib.xml().convertXMLtoTag("AGXMLPATH",CMLib.xml().parseOutAngleBrackets(((AutoGenArea)E).getGeneratorXmlPath())));
				str.append(CMLib.xml().convertXMLtoTag("AGAUTOVAR",CMLib.xml().parseOutAngleBrackets(CMParms.toEqListString(((AutoGenArea)E).getAutoGenVariables()))));
			}
		}
		else
		if(E instanceof Ability)
			str.append(CMLib.xml().convertXMLtoTag("AWRAP",E.text()));
		else
		if(E instanceof Item)
		{
			final Item I=(Item)E;
			str.append((((I instanceof Container)&&(((Container)I).capacity()>0))
				?CMLib.xml().convertXMLtoTag("IID",""+I):""));
			str.append(CMLib.xml().convertXMLtoTag("IWORN",""+I.rawWornCode()));
			str.append(CMLib.xml().convertXMLtoTag("ILOC",""+((I.container()!=null)?(""+I.container()):"")));
			str.append(CMLib.xml().convertXMLtoTag("IUSES",""+I.usesRemaining()));
			str.append(CMLib.xml().convertXMLtoTag("ILEVL",""+I.basePhyStats().level()));
			str.append(CMLib.xml().convertXMLtoTag("IABLE",""+I.basePhyStats().ability()));
			str.append((E.isGeneric()?"":CMLib.xml().convertXMLtoTag("ITEXT",""+I.text())));
		}
		else
		if(E instanceof MOB)
		{
			final MOB M=(MOB)E;
			str.append(CMLib.xml().convertXMLtoTag("MLEVL",""+M.basePhyStats().level()));
			str.append(CMLib.xml().convertXMLtoTag("MABLE",""+M.basePhyStats().ability()));
			str.append(CMLib.xml().convertXMLtoTag("MREJV",""+M.basePhyStats().rejuv()));
			str.append(((E.isGeneric()?"":CMLib.xml().convertXMLtoTag("MTEXT",""+M.text()))));
		}
		return str.toString();
	}

	protected String getGenMobAbilities(MOB M)
	{
		final StringBuilder abilitystr=new StringBuilder("");
		for(int b=0;b<M.numAbilities();b++)
		{
			final Ability A=M.fetchAbility(b);
			if((A!=null)&&(A.isSavable()))
			{
				abilitystr.append("<ABLTY>");
				abilitystr.append(CMLib.xml().convertXMLtoTag("ACLASS",CMClass.classID(A)));
				abilitystr.append(CMLib.xml().convertXMLtoTag("APROF",""+A.proficiency()));
				abilitystr.append(CMLib.xml().convertXMLtoTag("ADATA",getPropertiesStr(A,true)));
				abilitystr.append("</ABLTY>");
			}
		}
		return (CMLib.xml().convertXMLtoTag("ABLTYS",abilitystr.toString()));
	}

	@Override
	public String getGenScripts(PhysicalAgent E, boolean includeVars)
	{
		final StringBuilder scriptstr=new StringBuilder("");
		for(final Enumeration<ScriptingEngine> e=E.scripts();e.hasMoreElements();)
		{
			final ScriptingEngine SE=e.nextElement();
			if((SE!=null)&&(SE.isSavable()))
			{
				scriptstr.append("<SCRPT>");
				scriptstr.append(CMLib.xml().convertXMLtoTag("SCRIPT",CMLib.xml().parseOutAngleBrackets(SE.getScript())));
				scriptstr.append(CMLib.xml().convertXMLtoTag("SQN",""+SE.defaultQuestName()));
				scriptstr.append(CMLib.xml().convertXMLtoTag("SSCOP",SE.getVarScope()));
				if((includeVars)&&(SE.getVarScope().equals("*")))
					scriptstr.append(CMLib.xml().convertXMLtoTag("SSVAR",SE.getLocalVarXML()));
				scriptstr.append("</SCRPT>");
			}
		}
		if(scriptstr.length()>0)
			return (CMLib.xml().convertXMLtoTag("SCRPTS",scriptstr.toString()));
		return "";
	}

	protected void possibleAddElectronicsManufacturers(Item I, Set<CMObject> custom)
	{
		if((I instanceof Electronics)
		&&(custom!=null)
		&&(!((Electronics)I).getManufacturerName().equalsIgnoreCase("RANDOM"))
		&&(!custom.contains(((Electronics)I).getFinalManufacturer())))
			custom.add(((Electronics)I).getFinalManufacturer());
	}

	protected void possibleAddElectronicsManufacturers(MOB M, Set<CMObject> custom)
	{
		for(int i=0;i<M.numItems();i++)
			possibleAddElectronicsManufacturers(M.getItem(i),custom);
	}

	@Override
	public String getGenMobInventory(MOB M)
	{
		final StringBuilder itemstr=new StringBuilder("");
		for(int b=0;b<M.numItems();b++)
		{
			final Item I=M.getItem(b);
			if((I!=null)&&(I.isSavable()))
			{
				itemstr.append("<ITEM>");
				itemstr.append(CMLib.xml().convertXMLtoTag("ICLASS",CMClass.classID(I)));
				itemstr.append(CMLib.xml().convertXMLtoTag("IDATA",getPropertiesStr(I,true)));
				itemstr.append("</ITEM>");
			}
		}
		return (CMLib.xml().convertXMLtoTag("INVEN",itemstr.toString()));
	}

	protected String getPlayerExtraInventory(MOB M)
	{
		final StringBuilder itemstr=new StringBuilder("");
		final ItemCollection coll=M.playerStats().getExtItems();
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
		HashSet<String> done=new HashSet<String>();
		for(final Item thisItem : finalCollection)
		{
			if(!done.contains(""+thisItem))
			{
				CMLib.catalog().updateCatalogIntegrity(thisItem);
				final Item cont=thisItem.ultimateContainer(null);
				final String roomID=((cont.owner()==null)&&(thisItem instanceof SpaceObject)&&(CMLib.map().isObjectInSpace((SpaceObject)thisItem)))?
						("SPACE."+CMParms.toListString(((SpaceObject)thisItem).coordinates())):CMLib.map().getExtendedRoomID((Room)cont.owner());
				itemstr.append("<ITEM>");
				itemstr.append(CMLib.xml().convertXMLtoTag("ICLASS",CMClass.classID(thisItem)));
				itemstr.append((thisItem instanceof Container)
						?CMLib.xml().convertXMLtoTag("IID",""+thisItem):"");
				itemstr.append(CMLib.xml().convertXMLtoTag("IDATA",getPropertiesStr(thisItem,true)));
				itemstr.append("<IROOM ID=\""+roomID+"\" EXPIRE="+thisItem.expirationDate()+" />");
				itemstr.append("</ITEM>");
				done.add(""+thisItem);
			}
		}
		return (CMLib.xml().convertXMLtoTag("EXTRAINV",itemstr.toString()));
	}

	protected void setPlayerExtraInventory(MOB M, List<XMLTag> buf)
	{
		final ItemCollection coll=M.playerStats().getExtItems();
		final List<XMLLibrary.XMLTag> V=CMLib.xml().getContentsFromPieces(buf,"EXTRAINV");
		if(V==null)
		{
			Log.errOut("CoffeeMaker","Error parsing 'EXTRAINV' of "+identifier(M,null)+".  Load aborted");
			return;
		}
		final Hashtable<String,Container> IIDmap=new Hashtable<String,Container>();
		final Hashtable<Item,String> LOCmap=new Hashtable<Item,String>();
		for(int i=0;i<V.size();i++)
		{
			final XMLTag iblk=V.get(i);
			if((!iblk.tag().equalsIgnoreCase("ITEM"))||(iblk.contents()==null))
			{
				Log.errOut("CoffeeMaker","Error parsing 'ITEM' of "+identifier(M,null)+".  Load aborted");
				return;
			}
			final Item newOne=CMClass.getItem(iblk.getValFromPieces("ICLASS"));
			if(newOne instanceof ArchonOnly)
				continue;
			if(newOne==null)
			{
				Log.errOut("CoffeeMaker","Unknown item "+iblk.getValFromPieces("ICLASS")+" on "+identifier(M,null)+", skipping.");
				continue;
			}
			final List<XMLLibrary.XMLTag> idat=iblk.getContentsFromPieces("IDATA");
			if(idat==null)
			{
				Log.errOut("CoffeeMaker","Error parsing 'ITEM DATA' of "+identifier(M,null)+".  Load aborted");
				return;
			}
			final String ILOC=CMLib.xml().getValFromPieces(idat,"ILOC");
			coll.addItem(newOne);
			
			final XMLLibrary.XMLTag irm=iblk.getPieceFromPieces("IROOM");
			if(irm ==null)
			{
				Log.errOut("CoffeeMaker","Error parsing 'ITEM IROOM' of "+identifier(M,null)+".  Load aborted");
				return;
			}
			final String roomID=irm.parms().get("ID");
			final long expirationDate=CMath.s_long(irm.parms().get("EXPIRE"));
			if(roomID.startsWith("SPACE.") && (newOne instanceof SpaceObject))
				CMLib.map().addObjectToSpace((SpaceObject)newOne,CMParms.toLongArray(CMParms.parseCommas(roomID.substring(6), true)));
			else
			{
				final Room itemR=CMLib.map().getRoom(roomID);
				if(itemR!=null)
				{
					if(newOne instanceof BoardableShip)
						((BoardableShip)newOne).dockHere(itemR);
					else
						itemR.addItem(newOne);
					newOne.setExpirationDate(expirationDate);
				}
			}
			if(ILOC.length()>0)
				LOCmap.put(newOne,ILOC);
			setPropertiesStr(newOne,idat,true);
			if(newOne instanceof Container)
				IIDmap.put(CMLib.xml().getValFromPieces(iblk.contents(),"IID"),(Container)newOne);
		}
		for(Enumeration<Item> i=coll.items();i.hasMoreElements();)
		{
			final Item item=i.nextElement();
			if(item!=null)
			{
				final String ILOC=LOCmap.get(item);
				if(ILOC!=null)
					item.setContainer(IIDmap.get(ILOC));
			}
		}
		
	}

	protected String getGenPropertiesStr(Environmental E)
	{
		final StringBuilder text=new StringBuilder("");
		text.append(getEnvPropertiesStr(E));

		text.append(CMLib.xml().convertXMLtoTag("FLAG",envFlags(E)));

		if(E instanceof Exit)
		{
			final Exit exit=(Exit)E;
			text.append(
			 CMLib.xml().convertXMLtoTag("CLOSTX",exit.closedText())
			+CMLib.xml().convertXMLtoTag("DOORNM",exit.doorName())
			+CMLib.xml().convertXMLtoTag("OPENNM",exit.openWord())
			+CMLib.xml().convertXMLtoTag("CLOSNM",exit.closeWord())
			+CMLib.xml().convertXMLtoTag("KEYNM",exit.keyName())
			+CMLib.xml().convertXMLtoTag("OPENTK",exit.openDelayTicks()));
		}

		if(E instanceof ClanItem)
		{
			text.append(CMLib.xml().convertXMLtoTag("CLANID",""+((ClanItem)E).clanID()));
			text.append(CMLib.xml().convertXMLtoTag("CITYPE",""+((ClanItem)E).getClanItemType().ordinal()));
		}

		if(E instanceof Item)
		{
			final Item item=(Item)E;
			text.append(
			 CMLib.xml().convertXMLtoTag("IDENT",item.rawSecretIdentity())
			+CMLib.xml().convertXMLtoTag("VALUE",item.baseGoldValue())
			//+CMLib.xml().convertXMLtoTag("USES",item.usesRemaining()) // handled 'from top' & in db
			+CMLib.xml().convertXMLtoTag("MTRAL",item.material())
			+CMLib.xml().convertXMLtoTag("READ",item.readableText())
			+CMLib.xml().convertXMLtoTag("WORNL",item.rawLogicalAnd())
			+CMLib.xml().convertXMLtoTag("WORNB",item.rawProperLocationBitmap()));
			if(E instanceof Container)
			{
				text.append(CMLib.xml().convertXMLtoTag("CAPA",((Container)item).capacity()));
				text.append(CMLib.xml().convertXMLtoTag("CONT",((Container)item).containTypes()));
				text.append(CMLib.xml().convertXMLtoTag("OPENTK",((Container)item).openDelayTicks()));
			}
			if(E instanceof AmmunitionWeapon)
				text.append(CMLib.xml().convertXMLtoTag("ACAPA",((AmmunitionWeapon)item).ammunitionCapacity()));

			if(E instanceof BoardableShip)
			{
				text.append(CMLib.xml().convertXMLtoTag("SSAREA",CMLib.xml().parseOutAngleBrackets(getAreaObjectXML(((BoardableShip)item).getShipArea(), null, null, null, true).toString())));
				text.append(CMLib.xml().convertXMLtoTag("PORTID",((BoardableShip)item).getHomePortID()));
			}
			if(E instanceof SpaceShip)
			{
				text.append(CMLib.xml().convertXMLtoTag("SSOML",((SpaceShip)item).getOMLCoeff()+""));
				text.append(CMLib.xml().convertXMLtoTag("SSFACE", CMParms.toListString(((SpaceShip)item).facing())));
			}
		}

		if(E instanceof Coins)
		{
			text.append(CMLib.xml().convertXMLtoTag("CRNC",((Coins)E).getCurrency()));
			text.append(CMLib.xml().convertXMLtoTag("DENOM",""+((Coins)E).getDenomination()));
		}
		if(E instanceof Electronics)
		{
			text.append(CMLib.xml().convertXMLtoTag("POWC",""+((Electronics)E).powerCapacity()));
			text.append(CMLib.xml().convertXMLtoTag("POWR",""+((Electronics)E).powerRemaining()));
			text.append(CMLib.xml().convertXMLtoTag("EACT", ""+((Electronics)E).activated()));
			text.append(CMLib.xml().convertXMLtoTag("MANUFACT", ((Electronics)E).getManufacturerName()));

		}
		if(E instanceof ElecPanel)
		{
			if(((ElecPanel)E).panelType()!=null)
				text.append(CMLib.xml().convertXMLtoTag("SSPANELT",""+((ElecPanel)E).panelType().name()));
		}
		if(E instanceof TechComponent)
		{
			text.append(CMLib.xml().convertXMLtoTag("INSTF",""+((TechComponent)E).getInstalledFactor()));
			text.append(CMLib.xml().convertXMLtoTag("RECHRATE",""+((TechComponent)E).getRechargeRate()));
		}
		if(E instanceof ShipEngine)
		{
			text.append(CMLib.xml().convertXMLtoTag("SSTHRUST",""+((ShipEngine)E).getMaxThrust()));
			text.append(CMLib.xml().convertXMLtoTag("SSIMPL",""+((ShipEngine)E).getSpecificImpulse()));
			text.append(CMLib.xml().convertXMLtoTag("SSFEFF",""+((ShipEngine)E).getFuelEfficiency()));
			text.append(CMLib.xml().convertXMLtoTag("SSNTHRUST",""+((ShipEngine)E).getMinThrust()));
			text.append(CMLib.xml().convertXMLtoTag("SSCONST",""+((ShipEngine)E).isConstantThruster()));
			text.append(CMLib.xml().convertXMLtoTag("SSAPORTS",CMParms.toListString(((ShipEngine)E).getAvailPorts())));
		}
		if(E instanceof ShipWarComponent)
		{
			text.append(CMLib.xml().convertXMLtoTag("SSPDIRS",""+((ShipWarComponent)E).getPermittedNumDirections()));
			text.append(CMLib.xml().convertXMLtoTag("SSAPORTS",""+CMParms.toListString(((ShipWarComponent)E).getPermittedDirections())));
			text.append(CMLib.xml().convertXMLtoTag("SSMTYPES",""+CMParms.toListString(((ShipWarComponent)E).getDamageMsgTypes())));
		}
		if(E instanceof PowerGenerator)
		{
			text.append(CMLib.xml().convertXMLtoTag("EGENAMT",""+((PowerGenerator)E).getGeneratedAmountPerTick()));
		}
		if(E instanceof FuelConsumer)
		{
			text.append(CMLib.xml().convertXMLtoTag("ECONSTYP",CMParms.toListString(((FuelConsumer)E).getConsumedFuelTypes())));
		}
		if(E instanceof Recipe)
		{
			text.append(CMLib.xml().convertXMLtoTag("SKILLID",((Recipe)E).getCommonSkillID()));
			final String[] recipes = ((Recipe)E).getRecipeCodeLines();
			for(final String recipe : recipes)
				text.append(CMLib.xml().convertXMLtoTag("RECIPE",recipe));
		}

		if(E instanceof Light)
			text.append(CMLib.xml().convertXMLtoTag("BURNOUT",((Light)E).destroyedWhenBurnedOut()));

		if(E instanceof Wand)
			text.append(CMLib.xml().convertXMLtoTag("MAXUSE",((Wand)E).maxUses()));

		if(E instanceof Book)
		{
			text.append(CMLib.xml().convertXMLtoTag("MAXPG",((Book)E).getMaxPages()));
			text.append(CMLib.xml().convertXMLtoTag("MAXCHPG",((Book)E).getMaxCharsPerPage()));
		}
		
		if(E instanceof Rideable)
		{
			text.append(CMLib.xml().convertXMLtoTag("RIDET",((Rideable)E).rideBasis()));
			text.append(CMLib.xml().convertXMLtoTag("RIDEC",((Rideable)E).riderCapacity()));
			text.append(CMLib.xml().convertXMLtoTag("PUTSTR",((Rideable)E).getPutString()));
			text.append(CMLib.xml().convertXMLtoTag("MOUNTSTR",((Rideable)E).getMountString()));
			text.append(CMLib.xml().convertXMLtoTag("DISMOUNTSTR",((Rideable)E).getDismountString()));
			text.append(CMLib.xml().convertXMLtoTag("STATESTR",((Rideable)E).getStateString()));
			text.append(CMLib.xml().convertXMLtoTag("STATESUBJSTR",((Rideable)E).getStateStringSubject()));
			text.append(CMLib.xml().convertXMLtoTag("RIDERSTR",((Rideable)E).getRideString()));
		}

		if(E instanceof RawMaterial)
			text.append(CMLib.xml().convertXMLtoTag("DOMN",((RawMaterial)E).domainSource()+""));

		if(E instanceof Food)
		{
			text.append(CMLib.xml().convertXMLtoTag("CAPA2",((Food)E).nourishment()));
			text.append(CMLib.xml().convertXMLtoTag("BITE",((Food)E).bite()));
		}

		if(E instanceof Drink)
		{
			text.append(CMLib.xml().convertXMLtoTag("CAPA2",((Drink)E).liquidHeld()));
			text.append(CMLib.xml().convertXMLtoTag("REMAN",((Drink)E).liquidRemaining()));
			text.append(CMLib.xml().convertXMLtoTag("LTYPE",((Drink)E).liquidType()));
			text.append(CMLib.xml().convertXMLtoTag("DRINK",((Drink)E).thirstQuenched()));
		}

		if(E instanceof Weapon)
		{
			text.append(CMLib.xml().convertXMLtoTag("TYPE",((Weapon)E).weaponDamageType()));
			text.append(CMLib.xml().convertXMLtoTag("CLASS",((Weapon)E).weaponClassification()));
			text.append(CMLib.xml().convertXMLtoTag("MINR",((Weapon)E).minRange()));
			text.append(CMLib.xml().convertXMLtoTag("MAXR",((Weapon)E).maxRange()));
		}

		if(E instanceof Armor)
		{
			text.append(CMLib.xml().convertXMLtoTag("LAYR",((Armor)E).getClothingLayer()));
			text.append(CMLib.xml().convertXMLtoTag("LAYA",((Armor)E).getLayerAttributes()));
		}

		if(E instanceof LandTitle)
			text.append(CMLib.xml().convertXMLtoTag("LANDID",((LandTitle)E).landPropertyID()));
		else
		if(E instanceof PrivateProperty)
		{
			text.append(CMLib.xml().convertXMLtoTag("OWNERID",((PrivateProperty)E).getOwnerName()));
			text.append(CMLib.xml().convertXMLtoTag("PRICE",((PrivateProperty)E).getPrice()));
		}

		if(E instanceof Perfume)
			text.append(CMLib.xml().convertXMLtoTag("SMELLLST",((Perfume)E).getSmellList()));

		if(E instanceof DeadBody)
		{
			if(((DeadBody)E).charStats()!=null)
			{
				text.append(CMLib.xml().convertXMLtoTag("GENDER",""+(char)((DeadBody)E).charStats().getStat(CharStats.STAT_GENDER)));
				text.append(CMLib.xml().convertXMLtoTag("MRACE",""+((DeadBody)E).charStats().getMyRace().ID()));
				text.append(CMLib.xml().convertXMLtoTag("MDNAME",""+((DeadBody)E).getMobName()));
				text.append(CMLib.xml().convertXMLtoTag("MDDESC",""+((DeadBody)E).getMobDescription()));
				text.append(CMLib.xml().convertXMLtoTag("MKNAME",""+((DeadBody)E).getKillerName()));
				text.append(CMLib.xml().convertXMLtoTag("MTOD",""+((DeadBody)E).getTimeOfDeath()));
				text.append(CMLib.xml().convertXMLtoTag("MKPLAY",""+((DeadBody)E).isKillerPlayer()));
				text.append(CMLib.xml().convertXMLtoTag("MHASH",""+((DeadBody)E).getMobHash()));
				text.append(CMLib.xml().convertXMLtoTag("MDLMSG",""+((DeadBody)E).getLastMessage()));
				text.append(CMLib.xml().convertXMLtoTag("MBREAL",""+((DeadBody)E).isDestroyedAfterLooting()));
				text.append(CMLib.xml().convertXMLtoTag("MPLAYR",""+((DeadBody)E).isPlayerCorpse()));
				text.append(CMLib.xml().convertXMLtoTag("MPKILL",""+((DeadBody)E).getMobPKFlag()));
				if(((DeadBody)E).getSavedMOB()!=null)
					text.append("<MOBS>"+getMobXML(((DeadBody)E).getSavedMOB())+"</MOBS>");
				if(((DeadBody)E).getKillerTool()==null) 
					text.append("<KLTOOL />");
				else
				{
					text.append("<KLTOOL>");
					text.append(CMLib.xml().convertXMLtoTag("KLCLASS",CMClass.classID(((DeadBody)E).getKillerTool())));
					text.append(CMLib.xml().convertXMLtoTag("KLDATA",getPropertiesStr(((DeadBody)E).getKillerTool(),true)));
					text.append("</KLTOOL>");
				}
			}
			else
			{
				text.append(CMLib.xml().convertXMLtoTag("GENDER","M"));
				text.append(CMLib.xml().convertXMLtoTag("MRACE","Human"));
				text.append(CMLib.xml().convertXMLtoTag("MPLAYR","false"));
			}
		}

		if(E instanceof MOB)
		{
			final int money = CMLib.beanCounter().getMoney((MOB)E);
			text.append(CMLib.xml().convertXMLtoTag("MONEY",money));
			text.append(CMLib.xml().convertXMLtoTag("VARMONEY",""+((MOB)E).getMoneyVariation()));
			CMLib.beanCounter().clearInventoryMoney((MOB)E,null);
			((MOB)E).setMoney(money);
			for(final Pair<Clan,Integer> p : ((MOB)E).clans())
				text.append("<CLAN ROLE=").append(p.second.toString()).append(">").append(p.first.clanID()).append("</CLAN>");
			text.append(CMLib.xml().convertXMLtoTag("GENDER",""+(char)((MOB)E).baseCharStats().getStat(CharStats.STAT_GENDER)));
			text.append(CMLib.xml().convertXMLtoTag("MRACE",""+((MOB)E).baseCharStats().getMyRace().ID()));
			text.append(getFactionXML((MOB)E));
			text.append(getGenMobInventory((MOB)E));
			text.append(getGenMobAbilities((MOB)E));

			if(E instanceof Banker)
			{
				text.append(CMLib.xml().convertXMLtoTag("BANK",""+((Banker)E).bankChain()));
				text.append(CMLib.xml().convertXMLtoTag("COININT",""+((Banker)E).getCoinInterest()));
				text.append(CMLib.xml().convertXMLtoTag("ITEMINT",""+((Banker)E).getItemInterest()));
				text.append(CMLib.xml().convertXMLtoTag("LOANINT",""+((Banker)E).getLoanInterest()));
			}
			if(E instanceof PostOffice)
			{
				text.append(CMLib.xml().convertXMLtoTag("POSTCHAIN",""+((PostOffice)E).postalChain()));
				text.append(CMLib.xml().convertXMLtoTag("POSTMIN",""+((PostOffice)E).minimumPostage()));
				text.append(CMLib.xml().convertXMLtoTag("POSTLBS",""+((PostOffice)E).postagePerPound()));
				text.append(CMLib.xml().convertXMLtoTag("POSTHOLD",""+((PostOffice)E).holdFeePerPound()));
				text.append(CMLib.xml().convertXMLtoTag("POSTNEW",""+((PostOffice)E).feeForNewBox()));
				text.append(CMLib.xml().convertXMLtoTag("POSTHELD",""+((PostOffice)E).maxMudMonthsHeld()));
			}
			if(E instanceof Librarian)
			{
				text.append(CMLib.xml().convertXMLtoTag("LIBRCHAIN",""+((Librarian)E).libraryChain()));
				text.append(CMLib.xml().convertXMLtoTag("LIBROVERCHG",""+((Librarian)E).getOverdueCharge()));
				text.append(CMLib.xml().convertXMLtoTag("LIBRDAYCHG",""+((Librarian)E).getDailyOverdueCharge()));
				text.append(CMLib.xml().convertXMLtoTag("LIBROVERPCT",""+((Librarian)E).getOverdueChargePct()));
				text.append(CMLib.xml().convertXMLtoTag("LIBDAYPCT",""+((Librarian)E).getDailyOverdueChargePct()));
				text.append(CMLib.xml().convertXMLtoTag("LIBMINDAYS",""+((Librarian)E).getMinOverdueDays()));
				text.append(CMLib.xml().convertXMLtoTag("LIBMAXDAYS",""+((Librarian)E).getMaxOverdueDays()));
				text.append(CMLib.xml().convertXMLtoTag("LIBMAXBORROW",""+((Librarian)E).getMaxBorrowed()));
				text.append(CMLib.xml().convertXMLtoTag("POSTCMASK",""+((Librarian)E).contributorMask()));
			}
			if(E instanceof Auctioneer)
			{
				text.append(CMLib.xml().convertXMLtoTag("AUCHOUSE",""+((Auctioneer)E).auctionHouse()));
				//text.append(CMLib.xml().convertXMLtoTag("LIVEPRICE",""+((Auctioneer)E).liveListingPrice()));
				text.append(CMLib.xml().convertXMLtoTag("TIMEPRICE",""+((Auctioneer)E).timedListingPrice()));
				text.append(CMLib.xml().convertXMLtoTag("TIMEPCT",""+((Auctioneer)E).timedListingPct()));
				//text.append(CMLib.xml().convertXMLtoTag("LIVECUT",""+((Auctioneer)E).liveFinalCutPct()));
				text.append(CMLib.xml().convertXMLtoTag("TIMECUT",""+((Auctioneer)E).timedFinalCutPct()));
				text.append(CMLib.xml().convertXMLtoTag("MAXADAYS",""+((Auctioneer)E).maxTimedAuctionDays()));
				text.append(CMLib.xml().convertXMLtoTag("MINADAYS",""+((Auctioneer)E).minTimedAuctionDays()));
			}
			if(E instanceof Deity)
			{
				text.append(CMLib.xml().convertXMLtoTag("CLEREQ",((Deity)E).getClericRequirements()));
				text.append(CMLib.xml().convertXMLtoTag("WORREQ",((Deity)E).getWorshipRequirements()));
				text.append(CMLib.xml().convertXMLtoTag("CLERIT",((Deity)E).getClericRitual()));
				text.append(CMLib.xml().convertXMLtoTag("WORRIT",((Deity)E).getWorshipRitual()));
				text.append(CMLib.xml().convertXMLtoTag("CLERSIT",((Deity)E).getClericSin()));
				text.append(CMLib.xml().convertXMLtoTag("WORRSIT",((Deity)E).getWorshipSin()));
				text.append(CMLib.xml().convertXMLtoTag("CLERPOW",((Deity)E).getClericPowerup()));
				text.append(CMLib.xml().convertXMLtoTag("SVCRIT",((Deity)E).getServiceRitual()));

				StringBuilder itemstr=new StringBuilder("");
				for(int b=0;b<((Deity)E).numBlessings();b++)
				{
					final Ability A=((Deity)E).fetchBlessing(b);
					if(A==null)
						continue;
					itemstr.append("<BLESS>");
					itemstr.append(CMLib.xml().convertXMLtoTag("BLCLASS",CMClass.classID(A)));
					itemstr.append(CMLib.xml().convertXMLtoTag("BLONLY",""+((Deity)E).fetchBlessingCleric(b)));
					itemstr.append(CMLib.xml().convertXMLtoTag("BLDATA",getPropertiesStr(A,true)));
					itemstr.append("</BLESS>");
				}
				text.append(CMLib.xml().convertXMLtoTag("BLESSINGS",itemstr.toString()));

				itemstr=new StringBuilder("");
				for(int b=0;b<((Deity)E).numCurses();b++)
				{
					final Ability A=((Deity)E).fetchCurse(b);
					if(A==null)
						continue;
					itemstr.append("<CURSE>");
					itemstr.append(CMLib.xml().convertXMLtoTag("CUCLASS",CMClass.classID(A)));
					itemstr.append(CMLib.xml().convertXMLtoTag("CUONLY",""+((Deity)E).fetchCurseCleric(b)));
					itemstr.append(CMLib.xml().convertXMLtoTag("CUDATA",getPropertiesStr(A,true)));
					itemstr.append("</CURSE>");
				}
				text.append(CMLib.xml().convertXMLtoTag("CURSES",itemstr.toString()));

				itemstr=new StringBuilder("");
				for(int b=0;b<((Deity)E).numPowers();b++)
				{
					final Ability A=((Deity)E).fetchPower(b);
					if(A==null)
						continue;
					itemstr.append("<POWER>");
					itemstr.append(CMLib.xml().convertXMLtoTag("POCLASS",CMClass.classID(A)));
					itemstr.append(CMLib.xml().convertXMLtoTag("PODATA",getPropertiesStr(A,true)));
					itemstr.append("</POWER>");
				}
				text.append(CMLib.xml().convertXMLtoTag("POWERS",itemstr.toString()));
			}
			if(E instanceof ShopKeeper)
			{
				text.append(CMLib.xml().convertXMLtoTag("SELLCD",((ShopKeeper)E).getWhatIsSoldMask()));
				text.append(CMLib.xml().convertXMLtoTag("SELLIMSK",CMLib.xml().parseOutAngleBrackets(((ShopKeeper)E).getWhatIsSoldZappermask())));
				final StringBuilder itemstr=new StringBuilder("");
				for(final Iterator<Environmental> i=((ShopKeeper)E).getShop().getStoreInventory();i.hasNext();)
				{
					final Environmental E2=i.next();
					itemstr.append("<SHITEM>");
					itemstr.append(CMLib.xml().convertXMLtoTag("SICLASS",CMClass.classID(E2)));
					itemstr.append(CMLib.xml().convertXMLtoTag("SITYPE",CMClass.getType(E2).toString()));
					itemstr.append(CMLib.xml().convertXMLtoTag("SISTOCK",((ShopKeeper)E).getShop().numberInStock(E2)));
					itemstr.append(CMLib.xml().convertXMLtoTag("SIPRICE",((ShopKeeper)E).getShop().stockPrice(E2)));
					itemstr.append(CMLib.xml().convertXMLtoTag("SIDATA",getPropertiesStr(E2,true)));
					itemstr.append("</SHITEM>");
				}
				text.append(CMLib.xml().convertXMLtoTag("STORE",itemstr.toString()));
			}
			if(((MOB)E).tattoos().hasMoreElements())
			{
				text.append("<TATTS>");
				for(final Enumeration<Tattoo> e=((MOB)E).tattoos();e.hasMoreElements();)
					text.append(e.nextElement().toString()+";");
				text.append("</TATTS>");
			}
			if(((MOB)E).expertises().hasMoreElements())
			{
				text.append("<EDUS>");
				for(final Enumeration<String> x=((MOB)E).expertises();x.hasMoreElements();)
					text.append(x.nextElement()).append(';');
				text.append("</EDUS>");
			}
		}
		return text.toString();
	}

	protected String unpackErr(String where, String msg)
	{
		Log.errOut("CoffeeMaker","unpack"+where+"FromXML: "+msg);
		return msg;
	}

	protected String unpackErr(String where, String msg, XMLTag piece)
	{
		if(piece == null)
			return unpackErr(where, msg);
		Log.errOut("CoffeeMaker","unpack"+where+"FromXML: "+msg+" in piece "+piece.toString());
		return msg;
	}

	protected String unpackErr(String where, String msg, List<XMLLibrary.XMLTag> list)
	{
		if(list == null)
			return unpackErr(where, msg);
		if(list.size()>0)
			return unpackErr(where, msg, list.get(0));
		Log.errOut("CoffeeMaker","unpack"+where+"FromXML: "+msg+" in empty pieces list");
		return msg;
	}

	@Override
	public String unpackRoomFromXML(String buf, boolean andContent)
	{
		final List<XMLLibrary.XMLTag> xml=CMLib.xml().parseAllXML(buf);
		if(xml==null)
			return unpackErr("Room","null 'xml'");
		final List<XMLLibrary.XMLTag> roomData=CMLib.xml().getContentsFromPieces(xml,"AROOM");
		if(roomData==null)
			return unpackErr("Room","null 'roomData'",xml);
		return unpackRoomFromXML(roomData,andContent);
	}

	@Override
	public String unpackRoomFromXML(List<XMLTag> xml, boolean andContent)
	{
		return unpackRoomFromXML(null, xml, andContent, true);
	}

	protected String unpackRoomFromXML(Area forceArea, List<XMLTag> xml, boolean andContent, boolean andSave)
	{
		Area myArea;
		String areaName;
		if(forceArea!=null)
		{
			myArea=forceArea;
			areaName = forceArea.Name();
		}
		else
		{
			areaName = CMLib.xml().getValFromPieces(xml,"RAREA");
			myArea=CMLib.map().getArea(areaName);
		}
		if(myArea==null)
			return unpackErr("Room","null RAREA '"+areaName+"'",xml);
		final String roomClass=CMLib.xml().getValFromPieces(xml,"RCLAS");
		final Room newRoom=CMClass.getLocale(roomClass);
		if(newRoom==null)
			return unpackErr("Room","null RCLAS '"+roomClass+"'",xml);
		newRoom.setRoomID(CMLib.xml().getValFromPieces(xml,"ROOMID"));
		if(newRoom.roomID().equals("NEW"))
			newRoom.setRoomID(myArea.getNewRoomID(newRoom,-1));
		if((forceArea==null) && CMLib.map().getRoom(newRoom.roomID())!=null)
			return "Room Exists: "+newRoom.roomID();
		newRoom.setArea(myArea);
		if(andSave)
			CMLib.database().DBCreateRoom(newRoom);
		newRoom.setDisplayText(CMLib.xml().getValFromPieces(xml,"RDISP"));
		newRoom.setDescription(CMLib.xml().getValFromPieces(xml,"RDESC"));
		newRoom.setMiscText(CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(xml,"RTEXT")));

		// now EXITS!
		final List<XMLLibrary.XMLTag> xV=CMLib.xml().getContentsFromPieces(xml,"ROOMEXITS");
		if(xV==null)
			return unpackErr("Room","null 'ROOMEXITS' in room "+newRoom.roomID(),xml);
		for(int x=0;x<xV.size();x++)
		{
			final XMLTag xblk=xV.get(x);
			if((!xblk.tag().equalsIgnoreCase("REXIT"))||(xblk.contents()==null))
				return unpackErr("Room","??"+xblk.tag()+" in room "+newRoom.roomID(),xblk);
			final int dir=xblk.getIntFromPieces("XDIRE");
			final String doorID=xblk.getValFromPieces("XDOOR");
			if((dir<0)||(dir>=Directions.NUM_DIRECTIONS()))
			{
				if((dir>255)&&(!(newRoom instanceof GridLocale)))
					return unpackErr("Room","Not GridLocale, tried "+dir+" exit for room '"+newRoom.roomID()+"'",xblk);
				else
				if(dir>255)
				{
					final String xdata=xblk.getValFromPieces("XDATA");
					final List<String> CEs=CMParms.parseSemicolons(xdata.trim(),true);
					for(int ces=0;ces<CEs.size();ces++)
					{
						final Vector<String> SCE=CMParms.parse(CEs.get(ces).trim());
						final GridLocale.CrossExit CE=new GridLocale.CrossExit();
						if(SCE.size()<3)
							continue;
						CE.x=CMath.s_int(SCE.elementAt(0));
						CE.y=CMath.s_int(SCE.elementAt(1));
						final int codeddir=CMath.s_int(SCE.elementAt(2));
						if(SCE.size()>=4)
							CE.destRoomID=doorID+SCE.elementAt(3);
						else
							CE.destRoomID=doorID;
						CE.out=(codeddir&256)==256;
						CE.dir=codeddir&255;
						((GridLocale)newRoom).addOuterExit(CE);
						Room linkRoom=null;
						if(forceArea!=null)
							linkRoom=forceArea.getRoom(doorID);
						if(linkRoom==null)
							linkRoom=CMLib.map().getRoom(doorID);
						if((!CE.out)&&(linkRoom!=null)&&(!(linkRoom instanceof GridLocale)))
						{
							linkRoom.rawDoors()[CE.dir]=newRoom;
							linkRoom.setRawExit(CE.dir,CMClass.getExit("Open"));
							if(andSave)
								CMLib.database().DBUpdateExits(linkRoom);
						}
					}
				}
				else
					return unpackErr("Room","Unknown direction: "+dir+" in room "+newRoom.roomID());
			}
			else
			{
				final List<XMLLibrary.XMLTag> xxV=xblk.getContentsFromPieces("XEXIT");
				if(xxV==null)
					return unpackErr("Room","null 'XEXIT' in room "+newRoom.roomID(),xblk.contents());
				Exit exit=null;
				if(xxV.size()>0)
				{
					String exitID=CMLib.xml().getValFromPieces(xxV,"EXID");
					exit=CMClass.getExit(exitID);
					if(exit==null)
						return unpackErr("Room","null EXID '"+exitID+"' in room "+newRoom.roomID());
					exit.setTemporaryDoorLink("{{#"+newRoom.roomID()+"#}}");
					exit.setMiscText(CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(xxV,"EXDAT")));
					newRoom.setRawExit(dir,exit);
				}
				else
					exit=CMClass.getExit("GenExit");
				exit.recoverPhyStats();
				if(doorID.length()>0)
				{
					Room linkRoom=null;
					if(forceArea!=null)
						linkRoom=forceArea.getRoom(doorID);
					if(linkRoom==null)
						linkRoom=CMLib.map().getRoom(doorID);
					if(linkRoom!=null)
						newRoom.rawDoors()[dir]=linkRoom;
					else
					{
						newRoom.setRawExit(dir,exit); // get will get the fake one too!
						exit.setTemporaryDoorLink(doorID);
					}
				}
			}
		}

		// find any mis-linked exits and fix them!
		try
		{
			if(forceArea == null)
			{
				for(final Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
				{
					Room R=r.nextElement();
					synchronized(("SYNC"+R.roomID()).intern())
					{
						R=CMLib.map().getRoom(R);
						fixFillingRoomUnlinkedExits(newRoom, R, andSave);
					}
				}
			}
			else
			{
				for(final Enumeration<Room> r=forceArea.getProperMap();r.hasMoreElements();)
				{
					Room R=r.nextElement();
					synchronized(("SYNC"+R.roomID()).intern())
					{
						R=CMLib.map().getRoom(R);
						fixFillingRoomUnlinkedExits(newRoom, R, andSave);
					}
				}
			}
		}
		catch(final NoSuchElementException e)
		{
		}
		if(andSave)
		{
			CMLib.database().DBUpdateRoom(newRoom);
			CMLib.database().DBUpdateExits(newRoom);
		}
		if(andContent)
		{
			final Map<String,Physical> identTable=new Hashtable<String,Physical>();

			final List<XMLLibrary.XMLTag> cV=CMLib.xml().getContentsFromPieces(xml,"ROOMCONTENT");
			if(cV==null)
				return unpackErr("Room","null 'ROOMCONTENT' in room "+newRoom.roomID(),xml);
			if(cV.size()>0)
			{
				final Map<MOB,String> mobRideTable=new Hashtable<MOB,String>();
				final List<XMLLibrary.XMLTag> mV=CMLib.xml().getContentsFromPieces(cV,"ROOMMOBS");
				if(mV!=null)
				{
					for(int m=0;m<mV.size();m++)
					{
						final XMLTag mblk=mV.get(m);
						if((!mblk.tag().equalsIgnoreCase("RMOB"))||(mblk.contents()==null))
							return unpackErr("Room","bad 'mblk' in room "+newRoom.roomID(),mblk);
						final String mClass=mblk.getValFromPieces("MCLAS");
						final MOB newMOB=CMClass.getMOB(mClass);
						if(newMOB==null)
							return unpackErr("Room","null 'mClass': "+mClass+" in room "+newRoom.roomID());
	
						// for rideables AND leaders now!
						final String iden=mblk.getValFromPieces("MIDEN");
						if((iden!=null)&&(iden.length()>0))
							identTable.put(iden,newMOB);
	
						newMOB.setMiscText(CMLib.xml().restoreAngleBrackets(mblk.getValFromPieces("MTEXT")));
						newMOB.basePhyStats().setLevel(mblk.getIntFromPieces("MLEVL"));
						newMOB.basePhyStats().setAbility(mblk.getIntFromPieces("MABLE"));
						newMOB.basePhyStats().setRejuv(mblk.getIntFromPieces("MREJV"));
						final String ride=mblk.getValFromPieces("MRIDE");
						if((ride!=null)&&(ride.length()>0))
							mobRideTable.put(newMOB,ride);
						newMOB.setStartRoom(newRoom);
						newMOB.setLocation(newRoom);
						newMOB.recoverCharStats();
						newMOB.recoverPhyStats();
						newMOB.recoverMaxState();
						newMOB.resetToMaxState();
						newMOB.bringToLife(newRoom,true);
					}
				}

				final Map<Item,String> itemLocTable=new Hashtable<Item,String>();
				final List<XMLLibrary.XMLTag> iV=CMLib.xml().getContentsFromPieces(cV,"ROOMITEMS");
				if(iV!=null)
				{
					for(int i=0;i<iV.size();i++)
					{
						final XMLTag iblk=iV.get(i);
						if((!iblk.tag().equalsIgnoreCase("RITEM"))||(iblk.contents()==null))
							return unpackErr("Room","bad 'iblk' in room "+newRoom.roomID(),iblk);
						final int itemCount = iblk.parms().containsKey("COUNT") ? CMath.s_int(iblk.getParmValue("COUNT")) : 1;  
						for(int inum=0;inum<itemCount;inum++)
						{
							final String iClass=iblk.getValFromPieces("ICLAS");
							final Item newItem=CMClass.getItem(iClass);
							if(newItem instanceof ArchonOnly)
								break;
							if(newItem==null)
								return unpackErr("Room","null 'iClass': "+iClass+" in room "+newRoom.roomID(),iblk);
							if((newItem instanceof Container)||(newItem instanceof Rideable))
							{
								final String iden=iblk.getValFromPieces("IIDEN");
								if((iden!=null)&&(iden.length()>0))
									identTable.put(iden,newItem);
							}
							final String iloc=iblk.getValFromPieces("ILOCA");
							if(iloc.length()>0)
								itemLocTable.put(newItem,iloc);
							newItem.basePhyStats().setLevel(iblk.getIntFromPieces("ILEVL"));
							newItem.basePhyStats().setAbility(iblk.getIntFromPieces("IABLE"));
							newItem.basePhyStats().setRejuv(iblk.getIntFromPieces("IREJV"));
							newItem.setUsesRemaining(iblk.getIntFromPieces("IUSES"));
							newItem.setOwner(newRoom); // temporary measure to take care of behaviors
							newItem.setMiscText(CMLib.xml().restoreAngleBrackets(iblk.getValFromPieces("ITEXT")));
							newItem.setContainer(null);
							newItem.recoverPhyStats();
							newRoom.addItem(newItem);
							newItem.recoverPhyStats();
						}
					}
				}
				for(final Item childI : itemLocTable.keySet())
				{
					final String loc=itemLocTable.get(childI);
					final Item parentI=(Item)identTable.get(loc);
					if(parentI!=null)
					{
						if(parentI instanceof Container)
							childI.setContainer((Container)parentI);
						childI.recoverPhyStats();
						parentI.recoverPhyStats();
					}
				}
				for(final MOB M : mobRideTable.keySet())
				{
					final String ride=mobRideTable.get(M);
					if((ride!=null)&&(ride.length()>0))
					{
						final Environmental E=identTable.get(ride);
						if(E instanceof Rideable)
							M.setRiding((Rideable)E);
						else
						if(E instanceof MOB)
							M.setFollowing((MOB)E);
					}
				}
			}
		}
		// equivalent to clear debriandrestart
		CMLib.threads().clearDebri(newRoom,0);
		if(andSave)
			CMLib.database().DBUpdateItems(newRoom);
		newRoom.startItemRejuv();
		if(andSave)
			CMLib.database().DBUpdateMOBs(newRoom);
		return "";
	}

	protected void fixFillingRoomUnlinkedExits(Room newRoom, Room R, boolean andSave)
	{
		boolean changed=false;
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			final Exit exit=R.getRawExit(d);
			if((exit!=null)&&(exit.temporaryDoorLink().equalsIgnoreCase(newRoom.roomID())))
			{
				exit.setTemporaryDoorLink("");
				R.rawDoors()[d]=newRoom;
				changed=true;
			}
			else
			if((R.rawDoors()[d]!=null)&&(R.rawDoors()[d].roomID().equals(newRoom.roomID())))
			{
				R.rawDoors()[d]=newRoom;
				changed=true;
			}
		}
		if(changed && andSave)
			CMLib.database().DBUpdateExits(R);
	}

	@Override
	public String fillAreaAndCustomVectorFromXML(String buf, List<XMLTag> area, List<CMObject> custom, Map<String,String> externalFiles)
	{
		final List<XMLLibrary.XMLTag> xml=CMLib.xml().parseAllXML(buf);
		if(xml==null)
			return unpackErr("Fill","null 'xml'");
		final String error=fillCustomVectorFromXML(xml,custom,externalFiles);
		if(error.length()>0)
			return error;
		final List<XMLLibrary.XMLTag> areaData=CMLib.xml().getContentsFromPieces(xml,"AREA");
		if(areaData==null)
			return unpackErr("Fill","null 'AREA'",xml);
		for(int a=0;a<areaData.size();a++)
			area.add(areaData.get(a));
		return "";
	}

	@Override
	public String fillCustomVectorFromXML(String xml, List<CMObject> custom, Map<String,String> externalFiles)
	{
		final List<XMLLibrary.XMLTag> xmlv=CMLib.xml().parseAllXML(xml);
		if(xmlv==null)
			return unpackErr("Custom","null 'xmlv'",xmlv);
		return fillCustomVectorFromXML(xmlv,custom,externalFiles);
	}

	@Override
	public String fillCustomVectorFromXML(List<XMLTag> xml, List<CMObject> custom, Map<String,String> externalFiles)
	{
		List<XMLLibrary.XMLTag> aV=CMLib.xml().getContentsFromPieces(xml,"CUSTOM");
		if((aV!=null)&&(custom!=null))
		{
			for(int r=0;r<aV.size();r++)
			{
				final XMLTag ablk=aV.get(r);
				if(ablk.tag().equalsIgnoreCase("RACE"))
				{
					Race R=CMClass.getRace("GenRace");
					if(R!=null)
					{
						R=(Race)R.copyOf();
						R.setRacialParms("<RACE>"+ablk.value()+"</RACE>");
						if(!R.ID().equals("GenRace"))
							custom.add(R);
					}
				}
				else
				if(ablk.tag().equalsIgnoreCase("CCLASS"))
				{
					CharClass C=CMClass.getCharClass("GenCharClass");
					if(C!=null)
					{
						C=(CharClass)C.copyOf();
						C.setClassParms("<CCLASS>"+ablk.value()+"</CCLASS>");
						if(!C.ID().equals("GenCharClass"))
							custom.add(C);
					}
				}
				else
				if(ablk.tag().equalsIgnoreCase("ABILITY"))
				{
					final String type=ablk.getParmValue( "TYPE");
					if(type!=null)
					{
						Ability A=CMClass.getAbility(type);
						if(A!=null)
						{
							A=(Ability)A.copyOf();
							final String ID=ablk.getParmValue( "ID");
							final boolean exists=CMClass.getAbility(ID)!=null;
							A.setStat("ALLXML", ablk.value());
							if(!exists)
								CMClass.delClass(CMObjectType.ABILITY, CMClass.getAbility(ID));
							custom.add(A);
						}
						else
							return unpackErr("Custom","?type?"+ablk.tag(),ablk);
					}
					else
						return unpackErr("Custom","?type?"+ablk.tag(),ablk);
				}
				else
				if(ablk.tag().equalsIgnoreCase("MANUFACTURER"))
				{
					final Manufacturer M=(Manufacturer)CMClass.getCommon("DefaultManufacturer");
					if(M!=null)
					{
						M.setXml(ablk.value());
						if(CMLib.tech().getManufacturer(M.name())==null)
							custom.add(M);
					}
					else
						return unpackErr("Custom","?type?"+ablk.tag(),ablk);
				}
				else
					return unpackErr("Custom","??"+ablk.tag(),ablk);
			}
		}
		aV=CMLib.xml().getContentsFromPieces(xml,"FILES");
		if((aV!=null)&&(externalFiles!=null))
		{
			for(int r=0;r<aV.size();r++)
			{
				final XMLTag ablk=aV.get(r);
				if(!ablk.tag().equalsIgnoreCase("FILE"))
					return unpackErr("Custom","Wrong tag in custome file! "+ablk.value(),ablk);
				final String filename=ablk.getParmValue("NAME");
				if((filename==null)||(filename.length()==0))
					return unpackErr("Custom","No custom file filename! "+ablk.value(),ablk);
				if(!externalFiles.containsKey(filename))
					externalFiles.put(filename,ablk.value());
			}
		}
		return "";
	}

	@Override
	public String fillAreasVectorFromXML(String buf, List<List<XMLTag>> areas, List<CMObject> custom, Map<String,String> externalFiles)
	{
		final List<XMLLibrary.XMLTag> xml=CMLib.xml().parseAllXML(buf);
		if(xml==null)
			return unpackErr("Areas","null 'xml'");
		fillCustomVectorFromXML(xml,custom,externalFiles);
		final List<XMLLibrary.XMLTag> aV=CMLib.xml().getContentsFromPieces(xml,"AREAS");
		if(aV==null)
			return unpackErr("Areas","null 'AREAS'",xml);
		for(int r=0;r<aV.size();r++)
		{
			final XMLTag ablk=aV.get(r);
			if((!ablk.tag().equalsIgnoreCase("AREA"))||(ablk.contents()==null))
				return unpackErr("Areas","??"+ablk.tag(),ablk);
			areas.add(ablk.contents());
		}
		return "";
	}

	@Override
	public void addAutoPropsToAreaIfNecessary(Area newArea)
	{
		if((newArea!=null)
		&&(newArea.ID().equals("StdArea")))
		{
			if(!CMProps.getVar(CMProps.Str.AUTOWEATHERPARMS).equalsIgnoreCase("no"))
			{
				Behavior B=newArea.fetchBehavior("WeatherAffects");
				if(B==null)
				{
					B=CMClass.getBehavior("WeatherAffects");
					B.setSavable(false);
					newArea.addBehavior(B);
				}
				if(!B.isSavable())
					B.setParms(CMProps.getVar(CMProps.Str.AUTOWEATHERPARMS));
			}
			if(CMProps.getVar(CMProps.Str.AUTOAREAPROPS).trim().length()>0)
			{
				final String props=CMProps.getVar(CMProps.Str.AUTOAREAPROPS).trim();
				final List<String> allProps=CMParms.parseSemicolons(props,true);
				String prop=null;
				String parms=null;
				Ability A=null;
				Behavior B=null;
				for(int v=0;v<allProps.size();v++)
				{
					prop=allProps.get(v);
					parms="";
					final int x=prop.indexOf('(');
					if(x>=0)
					{
						parms=prop.substring(x+1).trim();
						prop=prop.substring(0,x).trim();
						if(parms.endsWith(")")
							) parms=parms.substring(0,parms.length()-1);
					}
					B=CMClass.getBehavior(prop);
					if((B!=null)&&(newArea.fetchBehavior(B.ID())==null))
					{
						B.setSavable(false);
						newArea.addBehavior(B);
						B.setParms(parms);
					}
					else
					{
						A=CMClass.getAbility(prop);
						if((A!=null)&&(newArea.fetchEffect(A.ID())==null))
						{
							newArea.addNonUninvokableEffect(A);
							A.setSavable(false);
							A.setMiscText(parms);
						}
					}
				}
			}
		}
		if((newArea!=null)&&(!newArea.getParents().hasMoreElements()))
		{
			Area defaultParentArea=null;
			final String defaultParentAreaName=CMProps.getVar(CMProps.Str.DEFAULTPARENTAREA);
			if((defaultParentAreaName!=null)&&(defaultParentAreaName.trim().length()>0))
			{
				defaultParentArea=CMLib.map().getArea(defaultParentAreaName.trim());
				if(defaultParentArea==null)
					Log.errOut("RoomLoader","Default parent area from coffeemud.ini '"+defaultParentAreaName.trim()+"' is unknown.");
			}
			if(defaultParentArea!=null)
			{
				if((newArea!=defaultParentArea)&&(newArea.getTimeObj()==CMLib.time().globalClock()))
				{
					if(defaultParentArea.canChild(newArea) && (newArea.canParent(defaultParentArea)))
					{
						defaultParentArea.addChild(newArea);
						newArea.addParent(defaultParentArea);
					}
				}
			}
		}
	}

	@Override
	public String unpackAreaFromXML(List<XMLTag> aV, Session S, String overrideAreaType, boolean andRooms)
	{
		String areaClass=CMLib.xml().getValFromPieces(aV,"ACLAS");
		final String areaName=CMLib.xml().getValFromPieces(aV,"ANAME");

		if(CMLib.map().getArea(areaName)!=null)
			return "Area Exists: "+areaName;
		if(overrideAreaType!=null)
			areaClass=overrideAreaType;
		final Area newArea=CMClass.getAreaType(areaClass);
		if(newArea==null)
			return unpackErr("Area","No class: "+areaClass);
		newArea.setName(areaName);
		CMLib.map().addArea(newArea);
		CMLib.database().DBCreateArea(newArea);

		newArea.setDescription(CMLib.coffeeFilter().safetyFilter(CMLib.xml().getValFromPieces(aV,"ADESC")));
		newArea.setClimateType(CMLib.xml().getIntFromPieces(aV,"ACLIM"));
		newArea.setTheme(CMLib.xml().getIntFromPieces(aV,"ATECH"));
		newArea.setSubOpList(CMLib.xml().getValFromPieces(aV,"ASUBS"));
		newArea.setMiscText(CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(aV,"ADATA")));
		if(CMLib.flags().isSavable(newArea))
		CMLib.database().DBUpdateArea(newArea.Name(),newArea);
		if(andRooms)
		{
			final List<XMLLibrary.XMLTag> rV=CMLib.xml().getContentsFromPieces(aV,"AROOMS");
			if(rV==null)
				return unpackErr("Area","null 'AROOMS'",aV);
			for(int r=0;r<rV.size();r++)
			{
				final XMLTag ablk=rV.get(r);
				if((!ablk.tag().equalsIgnoreCase("AROOM"))||(ablk.contents()==null))
					return unpackErr("Area","??"+ablk.tag());
				//if(S!=null) S.rawPrint(".");
				final String err=unpackRoomFromXML(ablk.contents(),true);
				if(err.length()>0)
					return err;
			}
		}
		return "";
	}

	@Override
	public String unpackAreaFromXML(String buf, Session S, String overrideAreaType, boolean andRooms)
	{
		final List<XMLLibrary.XMLTag> xml=CMLib.xml().parseAllXML(buf);
		if(xml==null)
			return unpackErr("Area","null 'xml'");
		final List<XMLLibrary.XMLTag> aV=CMLib.xml().getContentsFromPieces(xml,"AREA");
		if(aV==null)
			return unpackErr("Area","null 'aV'",xml);
		return unpackAreaFromXML(aV,S,overrideAreaType,andRooms);
	}

	@Override
	public Area unpackAreaObjectFromXML(String xml) throws CMException
	{
		List<XMLLibrary.XMLTag> aV=CMLib.xml().parseAllXML(xml);
		if(aV==null)
			throw new CMException(unpackErr("Area","null 'xml'"));
		aV=CMLib.xml().getContentsFromPieces(aV,"AREA");
		if(aV==null)
			throw new CMException(unpackErr("Area","null 'AREA'",aV));

		final String areaClass=CMLib.xml().getValFromPieces(aV,"ACLAS");
		final String areaName=CMLib.xml().getValFromPieces(aV,"ANAME");

		final Area newArea=CMClass.getAreaType(areaClass);
		if(newArea==null)
			throw new CMException(unpackErr("Area","No class: "+areaClass));
		newArea.setName(areaName);

		newArea.setDescription(CMLib.coffeeFilter().safetyFilter(CMLib.xml().getValFromPieces(aV,"ADESC")));
		newArea.setClimateType(CMLib.xml().getIntFromPieces(aV,"ACLIM"));
		newArea.setTheme(CMLib.xml().getIntFromPieces(aV,"ATECH"));
		newArea.setSubOpList(CMLib.xml().getValFromPieces(aV,"ASUBS"));
		newArea.setMiscText(CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(aV,"ADATA")));
		final List<XMLLibrary.XMLTag> rV=CMLib.xml().getContentsFromPieces(aV,"AROOMS");
		if(rV==null)
			throw new CMException(unpackErr("Area","null 'AROOMS'",aV));
		for(int r=0;r<rV.size();r++)
		{
			final XMLTag ablk=rV.get(r);
			if((!ablk.tag().equalsIgnoreCase("AROOM"))||(ablk.contents()==null))
				throw new CMException(unpackErr("Area","??"+ablk.tag()));
			//if(S!=null) S.rawPrint(".");
			final String err=unpackRoomFromXML(newArea, ablk.contents(),true,false);
			if(err.length()>0)
				throw new CMException(err);
		}
		return newArea;
	}

	@Override
	public StringBuffer getAreaXML(Area area, Session S, Set<CMObject> custom, Set<String> files, boolean andRooms)
	{
		return getAreaXML(area, S, custom, files, andRooms, true);
	}

	@Override
	public StringBuffer getAreaObjectXML(Area area, Session S, Set<CMObject> custom, Set<String> files, boolean andRooms)
	{
		return getAreaXML(area, S, custom, files, andRooms, false);
	}

	protected StringBuffer getAreaXML(Area area, Session S, Set<CMObject> custom, Set<String> files, boolean andRooms, boolean isInDB)
	{
		final StringBuffer buf=new StringBuffer("");
		if(area==null)
			return buf;
		final Area.State oldFlag=area.getAreaState();
		area.setAreaState(Area.State.FROZEN);
		buf.append("<AREA>");
		buf.append(CMLib.xml().convertXMLtoTag("ACLAS",area.ID()));
		buf.append(CMLib.xml().convertXMLtoTag("ANAME",area.Name()));
		buf.append(CMLib.xml().convertXMLtoTag("ADESC",area.description()));
		buf.append(CMLib.xml().convertXMLtoTag("ACLIM",area.getClimateTypeCode()));
		buf.append(CMLib.xml().convertXMLtoTag("ASUBS",area.getSubOpList()));
		buf.append(CMLib.xml().convertXMLtoTag("ATECH",area.getThemeCode()));
		buf.append(CMLib.xml().convertXMLtoTag("ADATA",area.text()));
		if(andRooms)
		{
			final Enumeration<Room> r=area.getCompleteMap();
			if(!r.hasMoreElements())
				buf.append("<AROOMS />");
			else
			{
				buf.append("<AROOMS>");
				Room R=null;
				for(;r.hasMoreElements();)
				{
					R=r.nextElement();
					synchronized(("SYNC"+R.roomID()).intern())
					{
						R=CMLib.map().getRoom(R);
						//if(S!=null) S.rawPrint(".");
						if((R!=null)&&(R.roomID()!=null)&&(R.roomID().length()>0))
							buf.append(getRoomXML(R,custom,files,true,isInDB)+"\n\r");
					}
				}
				buf.append("</AROOMS>");
			}
		}
		fillFileSet(area,files);
		buf.append("</AREA>");
		area.setAreaState(oldFlag);
		return buf;
	}

	@Override
	public StringBuffer logTextDiff(String e1, String e2)
	{
		int start=0;
		int end=e1.length()-1;
		int end2=e2.length()-1;
		boolean stopStart=false;
		boolean stopEnd=false;
		while((!stopStart)||(!stopEnd))
		{
			if(!stopStart)
			{
				if((start>=end)
				 ||(start>=end2)
				 ||(e1.charAt(start)!=e2.charAt(start)))
					stopStart=true;
				else
					start++;
			}

			if(!stopEnd)
			{
				if((end<=start)
				||(end2<=start)
				||(e1.charAt(end)!=e2.charAt(end2)))
					stopEnd=true;
				else
				{
					end--;
					end2--;
				}
			}
		}
		final StringBuffer str=new StringBuffer("*1>");
		if(end<start)
			str.append("");
		else str.append(e1.substring(start,end));
		str.append("\n\r*2>");
		if(end2<start)
			str.append("");
		else str.append(e2.substring(start,end2));
		return str;
	}

	@Override
	public void logDiff(Environmental E1, Environmental E2)
	{
		final StringBuilder str=new StringBuilder("Unmatched - "+E1.Name()+"\n\r");
		if(E1 instanceof MOB)
		{
			final MOB mob=(MOB)E1;
			final MOB dup=(MOB)E2;
			if(!CMClass.classID(mob).equals(CMClass.classID(dup)))
				str.append(CMClass.classID(mob)+"!="+CMClass.classID(dup)+"\n\r");
			if(mob.basePhyStats().level()!=dup.basePhyStats().level())
				str.append("Level- "+mob.basePhyStats().level()+"!="+dup.basePhyStats().level()+"\n\r");
			if(mob.basePhyStats().ability()!=dup.basePhyStats().ability())
				str.append("Ability- "+mob.basePhyStats().ability()+"!="+dup.basePhyStats().ability()+"\n\r");
			if(!mob.text().equals(dup.text()))
				str.append(logTextDiff(mob.text(),dup.text()));
		}
		else
		if(E1 instanceof Item)
		{
			final Item item=(Item)E1;
			final Item dup=(Item)E2;
			if(!CMClass.classID(item).equals(CMClass.classID(dup)))
				str.append(CMClass.classID(item)+"!="+CMClass.classID(dup)+"\n\r");
			if(item.basePhyStats().level()!=dup.basePhyStats().level())
				str.append("Level- "+item.basePhyStats().level()+"!="+dup.basePhyStats().level()+"\n\r");
			if(item.basePhyStats().ability()!=dup.basePhyStats().ability())
				str.append("Ability- "+item.basePhyStats().ability()+"!="+dup.basePhyStats().ability()+"\n\r");
			if(item.usesRemaining()!=dup.usesRemaining())
				str.append("Uses- "+item.usesRemaining()+"!="+dup.usesRemaining()+"\n\r");
			if(!item.text().equals(dup.text()))
				str.append(logTextDiff(item.text(),dup.text()));
		}
		if(Log.debugChannelOn())
			Log.debugOut("CoffeeMaker",str.toString());
	}

	@Override
	public Room makeNewRoomContent(Room room, boolean makeLive)
	{
		if((room==null)||(room.roomID().length()==0))
			return null;
		room=CMLib.map().getRoom(room);
		Room R=CMLib.database().DBReadRoom(room.roomID(), false);
		if(R!=null)
			CMLib.database().DBReadContent(R.roomID(),R,makeLive);
		return R;
	}

	@Override
	public StringBuffer getMobXML(MOB mob)
	{
		final StringBuffer buf=new StringBuffer("");
		buf.append("<MOB>");
		buf.append(CMLib.xml().convertXMLtoTag("MCLAS",CMClass.classID(mob)));
		buf.append(CMLib.xml().convertXMLtoTag("MLEVL",mob.basePhyStats().level()));
		buf.append(CMLib.xml().convertXMLtoTag("MABLE",mob.basePhyStats().ability()));
		buf.append(CMLib.xml().convertXMLtoTag("MREJV",mob.basePhyStats().rejuv()));
		buf.append(CMLib.xml().convertXMLtoTag("MTEXT",CMLib.xml().parseOutAngleBrackets(mob.text())));
		buf.append("</MOB>\n\r");
		return buf;
	}

	@Override
	public StringBuffer getMobsXML(List<MOB> mobs,
								   Set<CMObject> custom,
								   Set<String> files,
								   Map<String,List<MOB>> found)
	{
		final StringBuffer buf=new StringBuffer("");
		for(final MOB mob : mobs)
		{
			if(mob.isSavable())
			{
				List<MOB> dups=found.get(mob.Name()+mob.displayText());
				if(dups==null)
				{
					dups=new Vector<MOB>();
					found.put(mob.Name()+mob.displayText(),dups);
					dups.add(mob);
				}
				else
				{
					boolean matched=false;
					for(int v=0;v<dups.size();v++)
					{
						final MOB dup=dups.get(v);
						final int oldHeight=mob.basePhyStats().height();
						final int oldWeight=mob.basePhyStats().weight();
						final int oldGender=mob.baseCharStats().getStat(CharStats.STAT_GENDER);
						dup.basePhyStats().setHeight(mob.basePhyStats().height());
						dup.basePhyStats().setWeight(mob.basePhyStats().weight());
						dup.baseCharStats().setStat(CharStats.STAT_GENDER,mob.baseCharStats().getStat(CharStats.STAT_GENDER));
						if(CMClass.classID(mob).equals(CMClass.classID(dup))
						&&(mob.basePhyStats().level()==dup.basePhyStats().level())
						&&(mob.basePhyStats().ability()==dup.basePhyStats().ability())
						&&(mob.text().equals(dup.text())))
							matched=true;
						dup.basePhyStats().setHeight(oldHeight);
						dup.basePhyStats().setWeight(oldWeight);
						dup.baseCharStats().setStat(CharStats.STAT_GENDER,oldGender);
						if(matched)
							break;
					}
					if(!matched)
					{
						for(int v=0;v<dups.size();v++)
						{
							final MOB dup=dups.get(v);
							final int oldHeight=mob.basePhyStats().height();
							final int oldWeight=mob.basePhyStats().weight();
							final int oldGender=mob.baseCharStats().getStat(CharStats.STAT_GENDER);
							dup.basePhyStats().setHeight(mob.basePhyStats().height());
							dup.basePhyStats().setWeight(mob.basePhyStats().weight());
							dup.baseCharStats().setStat(CharStats.STAT_GENDER,mob.baseCharStats().getStat(CharStats.STAT_GENDER));
							if(Log.debugChannelOn()&&CMSecurity.isDebugging(CMSecurity.DbgFlag.EXPORT))
								logDiff(mob,dup);
							dup.basePhyStats().setHeight(oldHeight);
							dup.basePhyStats().setWeight(oldWeight);
							dup.baseCharStats().setStat(CharStats.STAT_GENDER,oldGender);
						}
						dups.add(mob);
					}
					else
						continue;
				}
				buf.append(getMobXML(mob));
				possibleAddElectronicsManufacturers(mob, custom);
				possiblyAddCustomRace(mob, custom);
				possiblyAddCustomClass(mob, custom);
				fillFileSet(mob,files);
			}
		}
		return buf;
	}

	protected void possiblyAddCustomRace(final MOB mob, Set<CMObject> custom)
	{
		if(mob==null)
			return;
		final Race R=mob.baseCharStats().getMyRace();
		if((R==null)||(custom==null))
			return;
		if((R.isGeneric()) &&(!custom.contains(R)))
			custom.add(R);
		for(final Ability A : R.racialAbilities(null))
		{
			if(A.isGeneric() && !custom.contains(A))
				custom.add(A);
		}
		for(final Ability A : R.racialEffects(null))
		{
			if(A.isGeneric() && !custom.contains(A))
				custom.add(A);
		}
	}

	protected void possiblyAddCustomClass(final MOB mob, Set<CMObject> custom)
	{
		if(custom!=null)
		for(int c=0;c<mob.baseCharStats().numClasses();c++)
		{
			final CharClass C=mob.baseCharStats().getMyClass(c);
			if((C.isGeneric())&&(!custom.contains(C)))
				custom.add(C);
		}
	}

	@Override
	public StringBuffer getRoomMobs(Room room,
									Set<CMObject> custom,
									Set<String> files,
									Map<String,List<MOB>> found)
	{
		final StringBuffer buf=new StringBuffer("");
		room=makeNewRoomContent(room,false);
		if(room==null)
			return buf;
		final List<MOB> mobs=new Vector<MOB>();
		for(int i=0;i<room.numInhabitants();i++)
			mobs.add(room.fetchInhabitant(i));
		buf.append(getMobsXML(mobs,custom,files,found));
		room.destroy();
		return buf;
	}

	@Override
	public StringBuffer getUniqueItemXML(Item item,
										 CMObjectType type,
										 Map<String,List<Item>> found,
										 Set<String> files)
	{
		final StringBuffer buf=new StringBuffer("");
		if(type != null)
		{
			switch(type)
			{
			case WEAPON:
				if (!(item instanceof Weapon))
					return buf;
				break;
			case ARMOR:
				if (!(item instanceof Armor))
					return buf;
				break;
			case ITEM:
				break;
			case MISCMAGIC:
				if (!(item instanceof MiscMagic))
					return buf;
				break;
			case CLANITEM:
				if (!(item instanceof ClanItem))
					return buf;
				break;
			case TECH:
				if (!(item instanceof Technical))
					return buf;
				break;
			case COMPTECH:
				if (!(item instanceof TechComponent))
					return buf;
				break;
			case SOFTWARE:
				if (!(item instanceof Software))
					return buf;
				break;
			default:
				break;
			}
		}
		if(item.displayText().length()>0)
		{
			List<Item> dups=found.get(item.Name()+item.displayText());
			if(dups==null)
			{
				dups=new Vector<Item>();
				found.put(item.Name()+item.displayText(),dups);
				dups.add(item);
			}
			else
			{
				for(int v=0;v<dups.size();v++)
				{
					final Item dup=dups.get(v);
					final int oldHeight=item.basePhyStats().height();
					item.basePhyStats().setHeight(dup.basePhyStats().height());
					if(CMClass.classID(item).equals(CMClass.classID(dup))
					&&(item.basePhyStats().level()==dup.basePhyStats().level())
					&&(item.usesRemaining()==dup.usesRemaining())
					&&(item.basePhyStats().ability()==dup.basePhyStats().ability())
					&&(item.text().equals(dup.text())))
					{
						item.basePhyStats().setHeight(oldHeight);
						return buf;
					}
					item.basePhyStats().setHeight(oldHeight);
				}
				for(int v=0;v<dups.size();v++)
				{
					final Item dup=dups.get(v);
					final int oldHeight=item.basePhyStats().height();
					item.basePhyStats().setHeight(dup.basePhyStats().height());
					if(Log.debugChannelOn()&&CMSecurity.isDebugging(CMSecurity.DbgFlag.EXPORT))
						logDiff(item,dup);
					item.basePhyStats().setHeight(oldHeight);
				}
				dups.add(item);
			}
			buf.append(getItemXML(item));
			fillFileSet(item,files);
		}
		return buf;
	}

	@Override
	public StringBuffer getItemXML(Item item)
	{
		final StringBuffer buf=new StringBuffer("");
		buf.append("<ITEM>");
		buf.append(CMLib.xml().convertXMLtoTag("ICLAS",CMClass.classID(item)));
		buf.append(CMLib.xml().convertXMLtoTag("IUSES",item.usesRemaining()));
		buf.append(CMLib.xml().convertXMLtoTag("ILEVL",item.basePhyStats().level()));
		buf.append(CMLib.xml().convertXMLtoTag("IABLE",item.basePhyStats().ability()));
		buf.append(CMLib.xml().convertXMLtoTag("IREJV",item.basePhyStats().rejuv()));
		buf.append(CMLib.xml().convertXMLtoTag("ITEXT",CMLib.xml().parseOutAngleBrackets(item.text())));
		buf.append("</ITEM>\n\r");
		return buf;
	}

	@Override
	public Item getItemFromXML(String xmlBuffer)
	{
		final List<XMLLibrary.XMLTag> xml=CMLib.xml().parseAllXML(xmlBuffer);
		if((xml==null)||(xml.size()==0))
			return null;
		final XMLTag iblk=xml.get(0);
		if((!iblk.tag().equalsIgnoreCase("ITEM"))||(iblk.contents()==null))
			return null;
		final String itemClass=iblk.getValFromPieces("ICLAS");
		final Item newItem=CMClass.getItem(itemClass);
		if(newItem==null)
			return null;
		newItem.basePhyStats().setLevel(iblk.getIntFromPieces("ILEVL"));
		newItem.basePhyStats().setAbility(iblk.getIntFromPieces("IABLE"));
		newItem.basePhyStats().setRejuv(iblk.getIntFromPieces("IREJV"));
		newItem.setUsesRemaining(iblk.getIntFromPieces("IUSES"));
		newItem.setMiscText(CMLib.xml().restoreAngleBrackets(iblk.getValFromPieces("ITEXT")));
		newItem.setContainer(null);
		newItem.recoverPhyStats();
		return newItem;
	}

	@Override
	public String addItemsFromXML(List<XMLTag> xml, List<Item> addHere, Session S)
	{
		if(xml==null)
			return unpackErr("Items","null 'xml'");
		final List<XMLLibrary.XMLTag> iV=CMLib.xml().getContentsFromPieces(xml,"ITEMS");
		if(iV==null)
			return unpackErr("Items","null 'ITEMS' <ITEMS>",xml);
		for(int i=0;i<iV.size();i++)
		{
			final XMLTag iblk=iV.get(i);
			if((!iblk.tag().equalsIgnoreCase("ITEM"))||(iblk.contents()==null))
				return unpackErr("Items","??"+iblk.tag());
			//if(S!=null) S.rawPrint(".");
			final String itemClass=iblk.getValFromPieces("ICLAS");
			final Item newItem=CMClass.getItem(itemClass);
			if((newItem instanceof ArchonOnly)
			&&((S==null)||(S.mob()==null)||(!CMSecurity.isASysOp(S.mob()))))
				continue;
			if(newItem==null)
				return unpackErr("Items","null 'iClass': "+itemClass);
			newItem.basePhyStats().setLevel(iblk.getIntFromPieces("ILEVL"));
			newItem.basePhyStats().setAbility(iblk.getIntFromPieces("IABLE"));
			newItem.basePhyStats().setRejuv(iblk.getIntFromPieces("IREJV"));
			newItem.setUsesRemaining(iblk.getIntFromPieces("IUSES"));
			newItem.setMiscText(CMLib.xml().restoreAngleBrackets(iblk.getValFromPieces("ITEXT")));
			newItem.setContainer(null);
			newItem.recoverPhyStats();
			addHere.add(newItem);
		}
		return "";
	}
	
	@Override
	public String addMOBsFromXML(List<XMLTag> xml, List<MOB> addHere, Session S)
	{
		if(xml==null)
			return unpackErr("MOBs","null 'xml'");
		final List<XMLLibrary.XMLTag> mV=CMLib.xml().getContentsFromPieces(xml,"MOBS");
		if(mV==null)
			return unpackErr("MOBs","null 'MOBS'",xml);
		for(int m=0;m<mV.size();m++)
		{
			final XMLTag mblk=mV.get(m);
			if((!mblk.tag().equalsIgnoreCase("MOB"))||(mblk.contents()==null))
				return unpackErr("MOBs","bad 'mblk'");
			final String mClass=mblk.getValFromPieces("MCLAS");
			final MOB newMOB=CMClass.getMOB(mClass);
			if(newMOB==null)
				return unpackErr("MOBs","null 'mClass': "+mClass);
			final String text=CMLib.xml().restoreAngleBrackets(mblk.getValFromPieces("MTEXT"));
			newMOB.setMiscText(text);
			newMOB.basePhyStats().setLevel(mblk.getIntFromPieces("MLEVL"));
			newMOB.basePhyStats().setAbility(mblk.getIntFromPieces("MABLE"));
			newMOB.basePhyStats().setRejuv(mblk.getIntFromPieces("MREJV"));
			newMOB.recoverCharStats();
			newMOB.recoverPhyStats();
			newMOB.recoverMaxState();
			newMOB.resetToMaxState();
			addHere.add(newMOB);
		}
		return "";
	}
	
	@Override
	public String addItemsFromXML(String xmlBuffer, List<Item> addHere, Session S)
	{
		final List<XMLLibrary.XMLTag> xml=CMLib.xml().parseAllXML(xmlBuffer);
		return addItemsFromXML(xml, addHere, S);
	}

	@Override
	public MOB getMobFromXML(String xmlBuffer)
	{
		final List<XMLLibrary.XMLTag> xml=CMLib.xml().parseAllXML(xmlBuffer);
		if((xml==null)||(xml.size()==0))
			return null;
		final XMLTag mblk=xml.get(0);
		if((!mblk.tag().equalsIgnoreCase("MOB"))||(mblk.contents()==null))
			return null;
		final String mClass=mblk.getValFromPieces("MCLAS");
		final MOB newMOB=CMClass.getMOB(mClass);
		if(newMOB==null)
			return null;
		final String text=CMLib.xml().restoreAngleBrackets(mblk.getValFromPieces("MTEXT"));
		newMOB.setMiscText(text);
		newMOB.basePhyStats().setLevel(mblk.getIntFromPieces("MLEVL"));
		newMOB.basePhyStats().setAbility(mblk.getIntFromPieces("MABLE"));
		newMOB.basePhyStats().setRejuv(mblk.getIntFromPieces("MREJV"));
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		return newMOB;
	}

	@Override
	public String addMOBsFromXML(String xmlBuffer, List<MOB> addHere, Session S)
	{
		final List<XMLLibrary.XMLTag> xml=CMLib.xml().parseAllXML(xmlBuffer);
		return addMOBsFromXML(xml, addHere, S);
	}

	@Override
	public String addCataDataFromXML(String xmlBuffer, List<CataData> addHere, List<? extends Physical> nameMatchers, Session S)
	{
		final List<XMLLibrary.XMLTag> xml=CMLib.xml().parseAllXML(xmlBuffer);
		if(xml==null)
			return unpackErr("CataDats","null 'xml'");
		final List<Map<String,CataData>> sets = new ArrayList<Map<String,CataData>>();
		for(Iterator<XMLLibrary.XMLTag> t= xml.iterator();t.hasNext();)
		{
			XMLLibrary.XMLTag tag = t.next();
			if(tag.tag().equalsIgnoreCase("CATADATAS"))
			{
				final Map<String,CataData> set = new TreeMap<String,CataData>();
				sets.add(set);
				for(Iterator<XMLLibrary.XMLTag> t2= tag.contents().iterator();t2.hasNext();)
				{
					XMLLibrary.XMLTag cataDataTag = t2.next();
					if(cataDataTag.tag().equalsIgnoreCase("CATALOGDATA"))
					{
						CataData catDat = CMLib.catalog().sampleCataData(cataDataTag.toString());
						if(cataDataTag.parms().containsKey("NAME"))
							set.put(CMLib.xml().restoreAngleBrackets(cataDataTag.parms().get("NAME")), catDat);
						else
							return unpackErr("CataDats","null 'NAME'");
					}
				}
			}
		}
		if(nameMatchers == null)
		{
			for(Map<String,CataData> chk : sets)
			{
				for(CataData dat : chk.values())
					addHere.add(dat);
			}
		}
		else
		{
			int bestMatch = -1;
			Map<String,CataData> bestSet = null;
			for(Map<String,CataData> chk : sets)
			{
				int ct = 0;
				for(Physical P : nameMatchers)
				{
					if(chk.containsKey(P.Name()))
						ct++;
				}
				if((ct > bestMatch)&&(ct>0))
				{
					bestMatch=ct;
					bestSet=chk;
				}
			}
			if(bestSet != null)
			{
				for(Physical P : nameMatchers)
				{
					if(bestSet.containsKey(P.Name()))
						addHere.add(bestSet.get(P.Name()));
					else
					{
						addHere.clear();
						break;
					}
				}
			}
		}
		if(addHere.size() == 0)
			return unpackErr("CataDats","nothing found");
		return "";
	}

	@Override
	public StringBuffer getItemsXML(List<Item> items, Map<String,List<Item>> found, Set<String> files, CMObjectType type)
	{
		final StringBuffer buf=new StringBuffer("");
		for(final Item I : items)
			buf.append(getUniqueItemXML(I,type,found,files));
		return buf;
	}

	@Override
	public StringBuffer getRoomItems(Room room,
									 Map<String,List<Item>> found,
									 Set<String> files,
									 CMObjectType type) // 0=item, 1=weapon, 2=armor
	{
		final StringBuffer buf=new StringBuffer("");
		room=makeNewRoomContent(room,false);
		if(room==null)
			return buf;
		final List<Item> items=new Vector<Item>();
		for(int i=0;i<room.numItems();i++)
			items.add(room.getItem(i));
		final List<MOB> mobs=new Vector<MOB>();
		for(int i=0;i<room.numInhabitants();i++)
			mobs.add(room.fetchInhabitant(i));
		for(int i=0;i<items.size();i++)
		{
			final Item item=items.get(i);
			if(item.isSavable())
				buf.append(getUniqueItemXML(item,type,found,files));
		}
		for(int m=0;m<mobs.size();m++)
		{
			final MOB M=mobs.get(m);
			if((M!=null)&&(M.isSavable()))
			{
				for(int i=0;i<M.numItems();i++)
				{
					final Item item=M.getItem(i);
					buf.append(getUniqueItemXML(item,type,found,files));
				}
				if(CMLib.coffeeShops().getShopKeeper(M)!=null)
				{
					for(final Iterator<Environmental> i=CMLib.coffeeShops().getShopKeeper(M).getShop().getStoreInventory();i.hasNext();)
					{
						final Environmental E=i.next();
						if(E instanceof Item)
							buf.append(getUniqueItemXML((Item)E,type,found,files));
					}
				}
			}
		}
		room.destroy();
		return buf;
	}

	@Override
	public StringBuffer getRoomXML(Room room, Set<CMObject> custom, Set<String> files, boolean andContent)
	{
		return getRoomXML(room, custom, files, andContent, true);
	}

	protected StringBuffer getRoomXML(Room room, Set<CMObject> custom, Set<String> files, boolean andContent, boolean andIsInDB)
	{
		final StringBuffer buf=new StringBuffer("");
		if(room==null)
			return buf;
		// do this quick before a tick messes it up!
		final List<MOB> inhabs=new Vector<MOB>();
		final Room croom=andIsInDB?makeNewRoomContent(room,false):room;
		if(andContent)
		{
			for(int i=0;i<croom.numInhabitants();i++)
				inhabs.add(croom.fetchInhabitant(i));
		}
		final List<Item> items=new Vector<Item>();
		if(andContent)
		{
			for(int i=0;i<croom.numItems();i++)
				items.add(croom.getItem(i));
		}
		
		final Area area=room.getArea();
		final boolean isShip=(area instanceof BoardableShip);

		buf.append("<AROOM>");
		buf.append(CMLib.xml().convertXMLtoTag("ROOMID",room.roomID()));
		buf.append(CMLib.xml().convertXMLtoTag("RAREA",room.getArea().Name()));
		buf.append(CMLib.xml().convertXMLtoTag("RCLAS",CMClass.classID(room)));
		buf.append(CMLib.xml().convertXMLtoTag("RDISP",room.displayText()));
		buf.append(CMLib.xml().convertXMLtoTag("RDESC",room.description()));
		buf.append(CMLib.xml().convertXMLtoTag("RTEXT",CMLib.xml().parseOutAngleBrackets(room.text())));
		fillFileSet(room,files);
		buf.append("<ROOMEXITS>");
		Room door;
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			door=room.rawDoors()[d];
			final Exit exit=room.getRawExit(d);
			if((isShip)&&(exit!=null)&&(door!=null)&&(door.getArea() != area))
				door=null;
			if(((door!=null)&&(door.roomID().length()>0))
			||((door==null)&&(exit!=null)))
			{
				buf.append("<REXIT>");
				buf.append(CMLib.xml().convertXMLtoTag("XDIRE",d));
				if(door==null)
					buf.append("<XDOOR />");
				else
					buf.append(CMLib.xml().convertXMLtoTag("XDOOR",door.roomID()));
				if(exit==null)
					buf.append("<XEXIT />");
				else
				{
					buf.append("<XEXIT>");
					buf.append(CMLib.xml().convertXMLtoTag("EXID",exit.ID()));
					buf.append(CMLib.xml().convertXMLtoTag("EXDAT",CMLib.xml().parseOutAngleBrackets(exit.text())));
					buf.append("</XEXIT>");
				}
				fillFileSet(exit,files);
				buf.append("</REXIT>");
			}
		}
		if(room instanceof GridLocale)
		{
			final Set<String> done=new HashSet<String>();
			int ordinal=0;
			for(final Iterator<GridLocale.CrossExit> i=((GridLocale)room).outerExits();i.hasNext();)
			{
				final GridLocale.CrossExit CE=i.next();
				Room R=CMLib.map().getRoom(CE.destRoomID);
				if(R==null)
					continue;
				if(R.getGridParent()!=null)
					R=R.getGridParent();
				if((R!=null)&&(R.roomID().length()>0)&&(!done.contains(R.roomID())))
				{
					done.add(R.roomID());
					final Set<String> oldStrs=new HashSet<String>();
					for(final Iterator<GridLocale.CrossExit> i2=((GridLocale)room).outerExits();i2.hasNext();)
					{
						final GridLocale.CrossExit CE2=i2.next();
						if((CE2.destRoomID.equals(R.roomID())
						||(CE2.destRoomID.startsWith(R.roomID()+"#("))))
						{
							final String str=CE2.x+" "+CE2.y+" "+((CE2.out?256:512)|CE2.dir)+" "+CE2.destRoomID.substring(R.roomID().length())+";";
							if(!oldStrs.contains(str))
								oldStrs.add(str);
						}
					}
					final StringBuffer exitStr=new StringBuffer("");
					for (final String string : oldStrs)
						exitStr.append(string);
					buf.append("<REXIT>");
					buf.append(CMLib.xml().convertXMLtoTag("XDIRE",(256+(++ordinal))));
					buf.append(CMLib.xml().convertXMLtoTag("XDOOR",R.roomID()));
					buf.append(CMLib.xml().convertXMLtoTag("XDATA",exitStr.toString()));
					buf.append("</REXIT>");
				}
			}
		}
		buf.append("</ROOMEXITS>");
		if(andContent)
		{
			buf.append("<ROOMCONTENT>");
			if(inhabs.size()==0)
				buf.append("<ROOMMOBS />");
			else
			{
				buf.append("<ROOMMOBS>");
				for(int i=0;i<inhabs.size();i++)
				{
					final MOB mob=inhabs.get(i);
					if((mob.isMonster())&&((mob.amFollowing()==null)||(mob.amFollowing().isMonster())))
					{
						possiblyAddCustomRace(mob, custom);
						possiblyAddCustomClass(mob, custom);
						possibleAddElectronicsManufacturers(mob, custom);

						buf.append("<RMOB>");
						buf.append(CMLib.xml().convertXMLtoTag("MCLAS",CMClass.classID(mob)));
						if((((mob instanceof Rideable)&&(((Rideable)mob).numRiders()>0)))||(mob.numFollowers()>0))
							buf.append(CMLib.xml().convertXMLtoTag("MIDEN",""+mob));
						buf.append(CMLib.xml().convertXMLtoTag("MLEVL",mob.basePhyStats().level()));
						buf.append(CMLib.xml().convertXMLtoTag("MABLE",mob.basePhyStats().ability()));
						buf.append(CMLib.xml().convertXMLtoTag("MREJV",mob.basePhyStats().rejuv()));
						buf.append(CMLib.xml().convertXMLtoTag("MTEXT",CMLib.xml().parseOutAngleBrackets(mob.text())));
						if(mob.riding()!=null)
							buf.append(CMLib.xml().convertXMLtoTag("MRIDE",""+mob.riding()));
						else
						if(mob.amFollowing()!=null)
							buf.append(CMLib.xml().convertXMLtoTag("MRIDE",""+mob.amFollowing()));
						else
							buf.append("<MRIDE />");
						fillFileSet(mob,files);
						buf.append("</RMOB>");
					}
				}
				buf.append("</ROOMMOBS>");
			}
			if(items.size()==0)
				buf.append("<ROOMITEMS />");
			else
			{
				buf.append("<ROOMITEMS>");
				final List<Pair<String,int[]>> itemList=(items.size()>20?new LinkedList<Pair<String,int[]>>():null);
				for(int i=0;i<items.size();i++)
				{
					final Item item=items.get(i);
					if(item.isSavable() || (!andIsInDB))
					{
						StringBuilder ibuf=new StringBuilder();
						ibuf.append(CMLib.xml().convertXMLtoTag("ICLAS",CMClass.classID(item)));
						if(((item instanceof Container)&&(((Container)item).capacity()>0))
						||((item instanceof Rideable)&&(((Rideable)item).numRiders()>0)))
							ibuf.append(CMLib.xml().convertXMLtoTag("IIDEN",""+item));
						if(item.container()==null)
							ibuf.append("<ILOCA />");
						else
							ibuf.append(CMLib.xml().convertXMLtoTag("ILOCA",""+item.container()));
						ibuf.append(CMLib.xml().convertXMLtoTag("IREJV",item.basePhyStats().rejuv()));
						ibuf.append(CMLib.xml().convertXMLtoTag("IUSES",item.usesRemaining()));
						ibuf.append(CMLib.xml().convertXMLtoTag("ILEVL",item.basePhyStats().level()));
						ibuf.append(CMLib.xml().convertXMLtoTag("IABLE",item.basePhyStats().ability()));
						ibuf.append(CMLib.xml().convertXMLtoTag("ITEXT",CMLib.xml().parseOutAngleBrackets(item.text())));
						if(itemList != null)
						{
							String itemStr=ibuf.toString();
							boolean found=false;
							for(Pair<String,int[]> P : itemList)
							{
								if(itemStr.equals(P.first))
								{
									found=true;
									P.second[0]++;
								}
							}
							if(!found)
								itemList.add(new Pair<String,int[]>(itemStr,new int[]{1}));
						}
						else
						{
							buf.append("<RITEM>");
							buf.append(ibuf);
							buf.append("</RITEM>");
							ibuf = null;
						}
						possibleAddElectronicsManufacturers(item, custom);
						fillFileSet(item,files);
					}
				}
				if(itemList!=null)
				{
					for(Pair<String,int[]> P : itemList)
					{
						if(P.second[0]<=1)
							buf.append("<RITEM>");
						else
							buf.append("<RITEM COUNT="+P.second[0]+">");
						buf.append(P.first);
						buf.append("</RITEM>");
					}
				}
				buf.append("</ROOMITEMS>");
			}
			buf.append("</ROOMCONTENT>");
		}
		buf.append("</AROOM>");
		if(croom != room)
			croom.destroy();
		return buf;
	}

	@Override
	public void setPropertiesStr(Environmental E, String buf, boolean fromTop)
	{
		final List<XMLLibrary.XMLTag> V=CMLib.xml().parseAllXML(buf);
		if(V==null)
			Log.errOut("CoffeeMaker","setPropertiesStr: null 'V': "+((E==null)?"":E.Name()));
		else
			setPropertiesStr(E,V,fromTop);
	}

	protected void recoverPhysical(Physical P)
	{
		if(P==null)
			return;
		P.recoverPhyStats();
		if(P instanceof MOB)
		{
			((MOB)P).recoverCharStats();
			((MOB)P).recoverMaxState();
			((MOB)P).resetToMaxState();
		}
	}

	@Override
	public void setPropertiesStr(Environmental E, List<XMLTag> V, boolean fromTop)
	{
		if(E==null)
		{
			Log.errOut("CoffeeMaker","setPropertiesStr2: null 'E'");
			return;
		}
		if((!(E instanceof Physical))
		||(!handleCatalogItem((Physical)E, V, fromTop)))
		{
			if(E.isGeneric())
				setGenPropertiesStr(E,V);
			if(fromTop)
				setOrdPropertiesStr(E,V);
			if(E instanceof SpaceObject)
			{
				((SpaceObject)E).setRadius(CMLib.xml().getLongFromPieces(V,"SSRADIUS"));
				final long[] coords=CMParms.toLongArray(CMParms.parseCommas(CMLib.xml().getValFromPieces(V,"SSCOORDS"), true));
				if((coords!=null)&&(coords.length==3))
					((SpaceObject)E).setCoords(coords);
				((SpaceObject)E).setSpeed(CMLib.xml().getDoubleFromPieces(V,"SSSPEED"));
				final double[] dir=CMParms.toDoubleArray(CMParms.parseCommas(CMLib.xml().getValFromPieces(V,"SSDIR"), true));
				if((dir!=null)&&(dir.length==2))
					((SpaceObject)E).setDirection(dir);
			}
		}
		if(E instanceof Physical)
			recoverPhysical((Physical)E);
	}

	protected void setOrdPropertiesStr(Environmental E, List<XMLTag> V)
	{
		if(V==null)
		{
			Log.errOut("CoffeeMaker","null XML returned on "+identifier(E,null)+" parse. Load aborted.");
			return;
		}

		if(E instanceof Room)
		{
			setExtraEnvProperties(E,V);
			setGenScripts((Room)E,V,false);
			if(E instanceof GridLocale)
			{
				((GridLocale)E).setXGridSize(CMLib.xml().getIntFromPieces(V,"XGRID"));
				((GridLocale)E).setYGridSize(CMLib.xml().getIntFromPieces(V,"YGRID"));
			}
			if(E instanceof LocationRoom)
				((LocationRoom)E).setDirectionFromCore(CMParms.toDoubleArray(CMParms.parseCommas(CMLib.xml().getValFromPieces(V,"COREDIR"),true)));
			((Room)E).setClimateType(CMLib.xml().getIntFromPieces(V,"RCLIM",((Room)E).getClimateTypeCode()));
			((Room)E).setAtmosphere(CMLib.xml().getIntFromPieces(V,"RATMO",((Room)E).getAtmosphereCode()));
		}
		else
		if(E instanceof Area)
		{
			((Area)E).setArchivePath(CMLib.xml().getValFromPieces(V,"ARCHP"));
			if(E instanceof BoardableShip)
				((Area)E).setDisplayText(CMLib.xml().getValFromPieces(V,"DISP"));
			((Area)E).setAuthorID(CMLib.xml().getValFromPieces(V,"AUTHOR"));
			((Area)E).setCurrency(CMLib.xml().getValFromPieces(V,"CURRENCY"));
			((Area)E).setAtmosphere(CMLib.xml().getIntFromPieces(V,"AATMO",((Area)E).getAtmosphereCode()));
			final List<XMLLibrary.XMLTag> VP=CMLib.xml().getContentsFromPieces(V,"PARENTS");
			if(VP!=null)
			{
				for(int i=0;i<VP.size();i++)
				{
					final XMLTag ablk=VP.get(i);
					if((!ablk.tag().equalsIgnoreCase("PARENT"))||(ablk.contents()==null))
					{
						Log.errOut("CoffeeMaker","Error parsing 'PARENT' of "+identifier(E,null)+".  Load aborted");
						return;
					}
					final String aName=ablk.getValFromPieces("PARENTNAMED");
					final Area A=CMLib.map().getArea(aName);
					if(A==null)
						Log.warnOut("CoffeeMaker","Unknown parent area '"+aName+"' of "+identifier(E,null));
					else
					{
						((Area)E).addParent(A);
						A.addChild((Area)E);
					}
				}
			}
			final List<XMLLibrary.XMLTag> VC=CMLib.xml().getContentsFromPieces(V,"CHILDREN");
			if(VC!=null)
			{
				for(int i=0;i<VC.size();i++)
				{
					final XMLTag ablk=VC.get(i);
					if((!ablk.tag().equalsIgnoreCase("CHILD"))||(ablk.contents()==null))
					{
						Log.errOut("CoffeeMaker","Error parsing 'CHILD' of "+identifier(E,null)+".  Load aborted");
						return;
					}
					final String aName=ablk.getValFromPieces("CHILDNAMED");
					final Area A=CMLib.map().getArea(aName);
					if(A==null)
						Log.warnOut("CoffeeMaker","Unknown child area '"+aName+"' of "+identifier(E,null));
					else
					{
						((Area)E).addChild(A);
						A.addParent((Area)E);
					}
				}
			}
			for(final Enumeration<String> f=((Area)E).areaBlurbFlags();f.hasMoreElements();)
				((Area)E).delBlurbFlag(f.nextElement());
			final List<String> VB=CMLib.xml().parseXMLList(CMLib.xml().getValFromPieces(V,"BLURBS"));
			for(final String s : VB)
				((Area)E).addBlurbFlag(s);
			if(E instanceof GridZones)
			{
				((GridZones)E).setXGridSize(CMLib.xml().getIntFromPieces(V,"XGRID"));
				((GridZones)E).setYGridSize(CMLib.xml().getIntFromPieces(V,"YGRID"));
			}
			if(E instanceof AutoGenArea)
			{
				((AutoGenArea)E).setGeneratorXmlPath(CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(V,"AGXMLPATH")));
				((AutoGenArea)E).setAutoGenVariables(CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(V,"AGAUTOVAR")));
			}
			setExtraEnvProperties(E,V);
			setGenScripts((Area)E,V,false);
		}
		else
		if(E instanceof Ability)
			E.setMiscText(CMLib.xml().getValFromPieces(V,"AWRAP"));
		else
		if(E instanceof Item)
		{
			final Item I=(Item)E;
			I.setUsesRemaining(CMLib.xml().getIntFromPieces(V,"IUSES"));
			I.basePhyStats().setLevel(CMLib.xml().getIntFromPieces(V,"ILEVL"));
			I.basePhyStats().setAbility(CMLib.xml().getIntFromPieces(V,"IABLE"));
			if(!E.isGeneric())
				I.setMiscText(CMLib.xml().getValFromPieces(V,"ITEXT"));
			//item.wearAt(CMLib.xml().getIntFromPieces(V,"USES"));
		}
		else
		if(E instanceof MOB)
		{
			final MOB M=(MOB)E;
			M.basePhyStats().setLevel(CMLib.xml().getIntFromPieces(V,"MLEVL"));
			M.basePhyStats().setAbility(CMLib.xml().getIntFromPieces(V,"MABLE"));
			M.basePhyStats().setRejuv(CMLib.xml().getIntFromPieces(V,"MREJV"));
			if(!M.isGeneric())
				M.setMiscText(CMLib.xml().getValFromPieces(V,"MTEXT"));
		}
	}

	protected void setGenMobAbilities(MOB M, List<XMLLibrary.XMLTag> buf)
	{
		final List<XMLLibrary.XMLTag> V=CMLib.xml().getContentsFromPieces(buf,"ABLTYS");
		if(V==null)
		{
			Log.errOut("CoffeeMaker","Error parsing 'ABLTYS' of "+identifier(M,null)+".  Load aborted");
			return;
		}
		for(int i=0;i<V.size();i++)
		{
			final XMLTag ablk=V.get(i);
			if((!ablk.tag().equalsIgnoreCase("ABLTY"))||(ablk.contents()==null))
			{
				Log.errOut("CoffeeMaker","Error parsing 'ABLTY' of "+identifier(M,null)+".  Load aborted");
				return;
			}
			final Ability newOne=CMClass.getAbility(ablk.getValFromPieces("ACLASS"));
			if(newOne==null)
			{
				Log.errOut("CoffeeMaker","Unknown ability "+ablk.getValFromPieces("ACLASS")+" on "+identifier(M,null)+", skipping.");
				continue;
			}
			final List<XMLLibrary.XMLTag> adat=ablk.getContentsFromPieces("ADATA");
			if(adat==null)
			{
				Log.errOut("CoffeeMaker","Error parsing 'ABLTY DATA' of "+identifier(M,null)+".  Load aborted");
				return;
			}
			final String proff=ablk.getValFromPieces("APROF");
			if((proff!=null)&&(proff.length()>0))
				newOne.setProficiency(CMath.s_int(proff));
			else
				newOne.setProficiency(100);
			setPropertiesStr(newOne,adat,true);
			if(M.fetchAbility(newOne.ID())==null)
			{
				M.addAbility(newOne);
				newOne.autoInvocation(M, false);
			}
		}
	}

	@Override
	public void setGenScripts(PhysicalAgent E, List<XMLTag> buf, boolean restoreVars)
	{
		final List<XMLLibrary.XMLTag> V=CMLib.xml().getContentsFromPieces(buf,"SCRPTS");
		if(V==null)
			return;

		for(int i=0;i<V.size();i++)
		{
			final XMLTag sblk=V.get(i);
			if((!sblk.tag().equalsIgnoreCase("SCRPT"))||(sblk.contents()==null))
			{
				Log.errOut("CoffeeMaker","Error parsing 'SCRPT' of "+identifier(E,null)+".  Load aborted");
				return;
			}
			final ScriptingEngine S=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
			S.setSavable(true);
			final String script=sblk.getValFromPieces("SCRIPT");
			if(script==null)
			{
				Log.errOut("CoffeeMaker","Error parsing 'SCRIPT' of "+identifier(E,null)+".  Load aborted");
				continue;
			}
			S.setScript(CMLib.xml().restoreAngleBrackets(script));
			final String sq=sblk.getValFromPieces("SQN");
			if(sq.length()>0)
				S.registerDefaultQuest(sq);

			final String scope=sblk.getValFromPieces("SSCOP");
			if(scope.length()>0)
				S.setVarScope(scope);

			if(restoreVars)
			{
				final String svars=sblk.getValFromPieces("SSVAR");
				if((svars!=null)&&(svars.length()>0))
					S.setLocalVarXML(svars);
			}
			E.addScript(S);
		}
	}

	protected void setGenMobInventory(MOB M, List<XMLTag> buf)
	{
		final List<XMLLibrary.XMLTag> V=CMLib.xml().getContentsFromPieces(buf,"INVEN");
		boolean variableEq=false;
		if(V==null)
		{
			Log.errOut("CoffeeMaker","Error parsing 'INVEN' of "+identifier(M,null)+".  Load aborted");
			return;
		}
		final Hashtable<String,Container> IIDmap=new Hashtable<String,Container>();
		final Hashtable<Item,String> LOCmap=new Hashtable<Item,String>();
		for(int i=0;i<V.size();i++)
		{
			final XMLTag iblk=V.get(i);
			if((!iblk.tag().equalsIgnoreCase("ITEM"))||(iblk.contents()==null))
			{
				Log.errOut("CoffeeMaker","Error parsing 'ITEM' of "+identifier(M,null)+".  Load aborted");
				return;
			}
			final Item newOne=CMClass.getItem(iblk.getValFromPieces("ICLASS"));
			if(newOne instanceof ArchonOnly)
				continue;
			if(newOne==null)
			{
				Log.errOut("CoffeeMaker","Unknown item "+iblk.getValFromPieces("ICLASS")+" on "+identifier(M,null)+", skipping.");
				continue;
			}
			final List<XMLLibrary.XMLTag> idat=iblk.getContentsFromPieces("IDATA");
			if(idat==null)
			{
				Log.errOut("CoffeeMaker","Error parsing 'ITEM DATA' of "+identifier(M,null)+".  Load aborted");
				return;
			}
			final long wornCode=CMLib.xml().getLongFromPieces(idat,"IWORN");
			final String ILOC=CMLib.xml().getValFromPieces(idat,"ILOC");
			M.addItem(newOne);
			if(ILOC.length()>0)
				LOCmap.put(newOne,ILOC);
			setPropertiesStr(newOne,idat,true);
			if((newOne instanceof Container)
			&&(((Container)newOne).capacity()>0))
				IIDmap.put(CMLib.xml().getValFromPieces(idat,"IID"),(Container)newOne);
			if(newOne instanceof Electronics)
				variableEq=true;
			else
			if(newOne.basePhyStats().rejuv()>0&&newOne.basePhyStats().rejuv()!=PhyStats.NO_REJUV)
				variableEq=true;
			newOne.wearAt(wornCode);
		}
		for(int i=0;i<M.numItems();i++)
		{
			final Item item=M.getItem(i);
			if(item!=null)
			{
				final String ILOC=LOCmap.get(item);
				if(ILOC!=null)
					item.setContainer(IIDmap.get(ILOC));
				else
				if(item.amWearingAt(Wearable.WORN_HELD)
				&&(!item.rawLogicalAnd())
				&&((item.rawProperLocationBitmap()&Wearable.WORN_WIELD)>0)
				&&(M.fetchWornItems(Wearable.WORN_WIELD,(short)0,(short)0).size()==0))
					item.wearAt(Wearable.WORN_WIELD);
			}
		}
		if(variableEq || (M.getMoneyVariation() != 0))
			M.flagVariableEq();
	}

	@Override
	public void populateShops(final ShopKeeper shopKeep, final List<XMLTag> buf)
	{
		boolean variableEq=false;
		shopKeep.setWhatIsSoldMask(CMLib.xml().getLongFromPieces(buf,"SELLCD"));
		shopKeep.setWhatIsSoldZappermask(CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(buf, "SELLIMSK")));
		shopKeep.getShop().emptyAllShelves();
		final List<XMLLibrary.XMLTag> V=CMLib.xml().getContentsFromPieces(buf,"STORE");
		if(V==null)
		{
			Log.errOut("CoffeeMaker","Error parsing 'STORE' of "+identifier(shopKeep,null)+".  Load aborted");
			return;
		}
		final Hashtable<String,Container> IIDmap=new Hashtable<String,Container>();
		final Hashtable<Item,String> LOCmap=new Hashtable<Item,String>();
		for(int i=0;i<V.size();i++)
		{
			final XMLTag iblk=V.get(i);
			if((!iblk.tag().equalsIgnoreCase("SHITEM"))||(iblk.contents()==null))
			{
				Log.errOut("CoffeeMaker","Error parsing 'SHITEM' of "+identifier(shopKeep,null)+".  Load aborted");
				continue;
			}
			final String itemi=iblk.getValFromPieces("SICLASS");
			final XMLTag x=iblk.getPieceFromPieces("SITYPE");
			final CMClass.CMObjectType type=(x==null)?null:CMClass.getTypeByNameOrOrdinal(x.value());
			final int numStock=iblk.getIntFromPieces("SISTOCK");
			final String prc=iblk.getValFromPieces("SIPRICE");
			int stockPrice=-1;
			if((prc!=null)&&(prc.length()>0))
				stockPrice=CMath.s_int(prc);
			Environmental newOne=null;
			final List<XMLLibrary.XMLTag> idat=iblk.getContentsFromPieces("SIDATA");
			if(type!=null)
				newOne=(Environmental)CMClass.getByType(itemi, type);
			if((newOne==null)&&((iblk.value().indexOf("<ABLTY>")>=0)||(iblk.value().indexOf("&lt;ABLTY&gt;")>=0)))
				newOne=CMClass.getMOB(itemi);
			if(newOne==null)
				newOne=CMClass.getUnknown(itemi);
			if(newOne==null)
			{
				Log.errOut("CoffeeMaker","Unknown item "+itemi+" on "+identifier(shopKeep,null)+", skipping.");
				continue;
			}
			if(idat==null)
			{
				Log.errOut("CoffeeMaker","Error parsing 'SHOP DATA' of "+identifier(shopKeep,null)+".  Load aborted");
				continue;
			}
			if(newOne instanceof Item)
			{
				if(newOne instanceof Container)
					IIDmap.put(CMLib.xml().getValFromPieces(idat,"IID"),(Container)newOne);
				final String ILOC=CMLib.xml().getValFromPieces(idat,"ILOC");
				if(ILOC.length()>0)
					LOCmap.put((Item)newOne,ILOC);
			}
			setPropertiesStr(newOne,idat,true);
			if(newOne instanceof SpaceShip)
			{
				final String key=CMLib.tech().getElectronicsKey(((SpaceShip)newOne).getShipArea());
				if(key != null)
					CMLib.tech().unregisterAllElectronics(key);
			}
			if(newOne instanceof BoardableShip)
			{
				if(newOne instanceof LandTitle)
					((LandTitle)newOne).setOwnerName("");
			}
			if(newOne instanceof Electronics)
				variableEq=true;
			else 
			if((newOne instanceof Physical)
			&&(((Physical)newOne).basePhyStats().rejuv()>0)
			&&(((Physical)newOne).basePhyStats().rejuv()!=PhyStats.NO_REJUV))
				variableEq=true;
			shopKeep.getShop().addStoreInventory(newOne,numStock,stockPrice);
			newOne.destroy();
		}
		for(final Iterator<Environmental> i=shopKeep.getShop().getStoreInventory();i.hasNext();)
		{
			final Environmental stE=i.next();
			if(stE instanceof Item)
			{
				final Item item=(Item)stE;
				final String ILOC=LOCmap.get(item);
				if(ILOC!=null)
					item.setContainer(IIDmap.get(ILOC));
			}
		}
		if(variableEq)
			((MOB)shopKeep).flagVariableEq();
	}

	public boolean handleCatalogItem(Physical P, List<XMLTag> buf, boolean fromTop)
	{
		setPhyStats(P.basePhyStats(),CMLib.xml().getValFromPieces(buf,"PROP"));
		if((CMLib.flags().isCataloged(P))
		&&(P.isGeneric()))
		{
			P.setName(CMLib.xml().getValFromPieces(buf,"NAME"));
			final Physical cataP=CMLib.catalog().getCatalogObj(P);
			if(cataP!=null)
			{
				if(CMath.bset(cataP.basePhyStats().disposition(),PhyStats.IS_CATALOGED))
					Log.errOut("CoffeeMaker","Error with catalog object "+P.Name()+".");
				else
				if(cataP!=P)
				{
					if(fromTop)
						setOrdPropertiesStr(P,buf);
					setPropertiesStr(P, cataP.text(),false);
					CMLib.catalog().changeCatalogUsage(P, true);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public List<String> getAllGenStats(Physical P)
	{
		final STreeSet<String> set=new STreeSet<String>();
		set.addAll(Arrays.asList(P.getStatCodes()));
		set.addAll(Arrays.asList(P.basePhyStats().getStatCodes()));
		if(P instanceof MOB)
		{
			set.addAll(Arrays.asList(((MOB)P).baseCharStats().getStatCodes()));
			if(((MOB)P).playerStats()!=null)
				set.addAll(Arrays.asList(((MOB)P).playerStats().getStatCodes()));
			set.addAll(Arrays.asList(CMParms.toStringArray(GenericBuilder.GenMOBCode.values())));
		}
		else
		if(P instanceof Item)
			set.addAll(Arrays.asList(CMParms.toStringArray(GenericBuilder.GenItemCode.values())));
		return set.toVector();
	}

	@Override
	public String getFinalStatName(String stat)
	{
		final boolean current=stat.startsWith("CURRENT ")||stat.startsWith("CURRENT_");
		if(current)
			return stat.substring(8);
		else
		if(stat.startsWith("BASE ")||stat.startsWith("BASE_"))
			return stat.substring(5);
		return stat;
	}
	
	@Override
	public boolean isAnyGenStat(Physical P, String stat)
	{
		if(P.isStat(stat))
			return true;
		stat = getFinalStatName(stat);
		if(P.basePhyStats().isStat(stat))
			return true;
		if(P instanceof MOB)
		{
			if(((MOB)P).baseCharStats().isStat(stat))
				return true;
			if(((MOB)P).baseState().isStat(stat))
				return true;
			if((((MOB)P).playerStats()!=null)
			&&((MOB)P).playerStats().isStat(stat))
				return true;
			if((((MOB)P).playerStats()!=null)
			&&(((MOB)P).playerStats().getAccount()!=null)
			&&(((MOB)P).playerStats().getAccount().isStat(stat)))
				return true;
			if(getGenMobCodeNum(stat)>=0)
				return true;
			final GenMOBBonusFakeStats fakeStat = (GenMOBBonusFakeStats)CMath.s_valueOf(GenMOBBonusFakeStats.class, stat);
			if(fakeStat != null)
				return true;
		}
		else
		if(P instanceof Item)
		{
			if(getGenItemCodeNum(stat)>=0)
				return true;
		}
		return false;
	}

	@Override
	public String getAnyGenStat(Physical P, String stat)
	{
		if(P.isStat(stat))
			return P.getStat(stat);
		final boolean current=stat.startsWith("CURRENT ")||stat.startsWith("CURRENT_");
		stat = getFinalStatName(stat);
		if(P.basePhyStats().isStat(stat))
			return (current)?P.phyStats().getStat(stat):P.basePhyStats().getStat(stat);
		if(P instanceof MOB)
		{
			if(((MOB)P).baseCharStats().isStat(stat))
				return current?((MOB)P).charStats().getStat(stat):((MOB)P).baseCharStats().getStat(stat);
			if(((MOB)P).baseState().isStat(stat))
				return current?((MOB)P).curState().getStat(stat):((MOB)P).baseState().getStat(stat);
			if((((MOB)P).playerStats()!=null)
			&&(((MOB)P).playerStats().isStat(stat)))
				return ((MOB)P).playerStats().getStat(stat);
			if((((MOB)P).playerStats()!=null)
			&&(((MOB)P).playerStats().getAccount() != null)
			&&(((MOB)P).playerStats().getAccount().isStat(stat)))
				return ((MOB)P).playerStats().getAccount().getStat(stat);
			if(getGenMobCodeNum(stat)>=0)
				return getGenMobStat((MOB)P, stat);
			final GenMOBBonusFakeStats fakeStat = (GenMOBBonusFakeStats)CMath.s_valueOf(GenMOBBonusFakeStats.class, stat);
			if(fakeStat != null)
			{
				switch(fakeStat)
				{
				case QUESTPOINTS:
					return ""+((MOB)P).getQuestPoint();
				case FOLLOWERS:
					return ""+((MOB)P).numFollowers();
				case TRAINS:
					return ""+((MOB)P).getTrains();
				case PRACTICES:
					return ""+((MOB)P).getPractices();
				case STINK:
					if(((MOB)P).playerStats()!=null)
						return CMath.toPct(((MOB)P).playerStats().getHygiene()/PlayerStats.HYGIENE_DELIMIT);
					break;
				}
			}
		}
		else
		if(P instanceof Item)
		{
			if(getGenItemCodeNum(stat)>=0)
				return getGenItemStat((Item)P, stat);
		}
		return "";
	}

	@Override
	public void setAnyGenStat(Physical P, String stat, String value)
	{
		setAnyGenStat(P,stat,value,false);
	}

	@Override
	public void setAnyGenStat(Physical P, String stat, String value, boolean supportPlusMinusPrefix)
	{
		if(supportPlusMinusPrefix
		&&(value.trim().length()>0)
		&&("+-".indexOf(value.trim().charAt(0))>=0))
		{
			final char plusMinus=value.trim().charAt(0);
			final String oldVal=getAnyGenStat(P, stat);
			if((oldVal!=null)
			&&(CMath.isNumber(oldVal))
			&&(CMath.isNumber(value.trim().substring(1).trim())))
			{
				value=value.trim().substring(1).trim();
				if(CMath.isInteger(oldVal))
				{
					if(plusMinus=='+')
						value=Integer.toString(CMath.s_int(oldVal) + CMath.s_int(value));
					else
						value=Integer.toString(CMath.s_int(oldVal) - CMath.s_int(value));
				}
				else
				if(plusMinus=='+')
					value=Double.toString(CMath.s_double(oldVal) + CMath.s_double(value));
				else
					value=Double.toString(CMath.s_double(oldVal) - CMath.s_double(value));
			}
		}
		if(P.isStat(stat))
		{
			P.setStat(stat, value);
			return;
		}
		final boolean current=stat.startsWith("CURRENT ")||stat.startsWith("CURRENT_");
		stat = getFinalStatName(stat);
		if(P.basePhyStats().isStat(stat))
		{
			if(current)
				P.phyStats().setStat(stat, value);
			else
				P.basePhyStats().setStat(stat, value);
			return;
		}
		if(P instanceof MOB)
		{
			if(((MOB)P).baseCharStats().isStat(stat))
			{
				if(current)
					((MOB)P).charStats().setStat(stat, value);
				else
					((MOB)P).baseCharStats().setStat(stat, value);
				return;
			}
			if(((MOB)P).baseState().isStat(stat))
			{
				if(current)
					((MOB)P).curState().setStat(stat, value);
				else
					((MOB)P).baseState().setStat(stat, value);
				return;
			}
			if((((MOB)P).playerStats()!=null)
			&&(((MOB)P).playerStats().isStat(stat)))
			{
				((MOB)P).playerStats().setStat(stat, value);
				return;
			}
			if((((MOB)P).playerStats()!=null)
			&&(((MOB)P).playerStats().getAccount() != null)
			&&(((MOB)P).playerStats().getAccount().isStat(stat)))
			{
				((MOB)P).playerStats().getAccount().setStat(stat, value);
				return;
			}
			if(getGenMobCodeNum(stat)>=0)
			{
				setGenMobStat((MOB)P, stat, value);
				return;
			}
			final GenMOBBonusFakeStats fakeStat = (GenMOBBonusFakeStats)CMath.s_valueOf(GenMOBBonusFakeStats.class, stat);
			if(fakeStat != null)
			{
				switch(fakeStat)
				{
				case QUESTPOINTS:
					((MOB)P).setQuestPoint(CMath.parseIntExpression(value));
					return;
				case FOLLOWERS:
					return;
				case TRAINS:
					((MOB)P).setTrains(CMath.parseIntExpression(value));
					return;
				case PRACTICES:
					((MOB)P).setPractices(CMath.parseIntExpression(value));
					return;
				case STINK:
					((MOB)P).playerStats().setHygiene(Math.round(CMath.s_pct(value)*PlayerStats.HYGIENE_DELIMIT));
					return;
				}
			}
		}
		else
		if(P instanceof Item)
		{
			if(getGenItemCodeNum(stat)>=0)
			{
				setGenItemStat((Item)P, stat, value);
				return;
			}
		}
	}

	protected void setGenPropertiesStr(Environmental E, List<XMLTag> buf)
	{
		if(buf==null)
		{
			Log.errOut("CoffeeMaker","null XML returned on "+identifier(E,null)+" parse.  Load aborted.");
			return;
		}

		if((E instanceof MOB)&&(CMLib.xml().getValFromPieces(buf,"GENDER").length()==0))
		{
			Log.errOut("CoffeeMaker","MOB "+identifier(E,null)+" has malformed XML. Load aborted.");
			return;
		}

		if(E instanceof MOB)
		{
			((MOB)E).delAllAbilities();
			((MOB)E).delAllItems(true);
			if(E instanceof ShopKeeper)
			{
				for(final Iterator<Environmental> i=((ShopKeeper)E).getShop().getStoreInventory();i.hasNext();)
					((ShopKeeper)E).getShop().delAllStoreInventory(i.next());
			}
			if(E instanceof Deity)
			{
				while(((Deity)E).numBlessings()>0)
					((Deity)E).delBlessing(((Deity)E).fetchBlessing(0));
				while(((Deity)E).numCurses()>0)
					((Deity)E).delCurse(((Deity)E).fetchCurse(0));
				while(((Deity)E).numPowers()>0)
					((Deity)E).delPower(((Deity)E).fetchPower(0));
			}
		}

		if(E instanceof Physical)
		{
			final Physical P=(Physical)E;
			P.delAllEffects(false);
		}
		if(E instanceof PhysicalAgent)
		{
			final PhysicalAgent P=(PhysicalAgent)E;
			P.delAllBehaviors();
			P.delAllScripts();
		}

		if(E instanceof MOB)
		{
			final MOB mob=(MOB)E;
			mob.baseCharStats().setStat(CharStats.STAT_GENDER,CMLib.xml().getValFromPieces(buf,"GENDER").charAt(0));
			final List<XMLTag> clanPieces=CMLib.xml().getPiecesFromPieces(buf,"CLAN");
			for(final XMLTag p : clanPieces)
			{
				final String clanID=p.value();
				final Clan C=CMLib.clans().getClan(clanID);
				if(C!=null)
				{
					int roleID=C.getGovernment().getAcceptPos();
					if(p.parms().containsKey("ROLE"))
						roleID=CMath.s_int(p.parms().get("ROLE"));
					mob.setClan(C.clanID(), roleID);
				}
			}
			final String raceID=CMLib.xml().getValFromPieces(buf,"MRACE");
			final Race R=(raceID.length()>0)?CMClass.getRace(raceID):null;
			if(R!=null)
			{
				mob.baseCharStats().setMyRace(R);
				mob.setTrains(0);
				mob.setPractices(0);
				R.startRacing(mob,false);
			}
		}

		setEnvProperties(E,buf);
		final String deprecatedFlag=CMLib.xml().getValFromPieces(buf,"FLAG");
		if((deprecatedFlag!=null)&&(deprecatedFlag.length()>0))
			setEnvFlags(E,CMath.s_int(deprecatedFlag));

		if(E instanceof Exit)
		{
			final Exit exit=(Exit)E;
			final String closedText=CMLib.xml().getValFromPieces(buf,"CLOSTX");
			final String doorName=CMLib.xml().getValFromPieces(buf,"DOORNM");
			final String openName=CMLib.xml().getValFromPieces(buf,"OPENNM");
			final String closeName=CMLib.xml().getValFromPieces(buf,"CLOSNM");
			exit.setExitParams(doorName,closeName,openName,closedText);
			exit.setKeyName(CMLib.xml().getValFromPieces(buf,"KEYNM"));
			exit.setOpenDelayTicks(CMLib.xml().getIntFromPieces(buf,"OPENTK"));
		}

		if(E instanceof ClanItem)
		{
			((ClanItem)E).setClanID(CMLib.xml().getValFromPieces(buf,"CLANID"));
			((ClanItem)E).setClanItemType(ClanItem.ClanItemType.values()[CMLib.xml().getIntFromPieces(buf,"CITYPE")]);
		}

		if(E instanceof Item)
		{
			final Item item=(Item)E;
			item.setSecretIdentity(CMLib.xml().getValFromPieces(buf,"IDENT"));
			item.setBaseValue(CMLib.xml().getIntFromPieces(buf,"VALUE"));
			item.setMaterial(CMLib.xml().getIntFromPieces(buf,"MTRAL"));
			//item.setUsesRemaining(CMath.s_int(CMLib.xml().returnXMLValue(buf,"USES"))); // handled 'from top' & in db
			if(item instanceof Container)
			{
				((Container)item).setCapacity(CMLib.xml().getIntFromPieces(buf,"CAPA"));
				((Container)item).setContainTypes(CMLib.xml().getLongFromPieces(buf,"CONT"));
				final String openDelayStr=CMLib.xml().getValFromPieces(buf,"OPENTK");
				if((openDelayStr!=null)&&(openDelayStr.length()>0))
					((Container)item).setOpenDelayTicks(CMath.s_int(openDelayStr));

			}
			if(item instanceof AmmunitionWeapon)
				((AmmunitionWeapon)item).setAmmoCapacity(CMLib.xml().getIntFromPieces(buf,"ACAPA"));
			item.setRawLogicalAnd(CMLib.xml().getBoolFromPieces(buf,"WORNL"));
			item.setRawProperLocationBitmap(CMLib.xml().getLongFromPieces(buf,"WORNB"));
			item.setReadableText(CMLib.xml().getValFromPieces(buf,"READ"));
			if(item instanceof BoardableShip)
			{
				((BoardableShip)item).setShipArea(CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(buf,"SSAREA")));
				((BoardableShip)item).setHomePortID(CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(buf,"PORTID")));
			}
			if(item instanceof SpaceShip)
			{
				((SpaceShip)item).setOMLCoeff(CMLib.xml().getDoubleFromPieces(buf,"SSOML"));
				double[] facing=CMParms.toDoubleArray(CMParms.parseCommas(CMLib.xml().getValFromPieces(buf,"SSFACE"),true));
				if((facing!=null)&&(facing.length==2))
					((SpaceShip)item).setFacing(facing);
			}
		}

		if(E instanceof Book)
		{
			((Book)E).setMaxPages(CMLib.xml().getIntFromPieces(buf, "MAXPG"));
			((Book)E).setMaxCharsPerPage(CMLib.xml().getIntFromPieces(buf, "MAXCHPG"));
		}
		
		if(E instanceof Rideable)
		{
			((Rideable)E).setRideBasis(CMLib.xml().getIntFromPieces(buf,"RIDET"));
			((Rideable)E).setRiderCapacity(CMLib.xml().getIntFromPieces(buf,"RIDEC"));
			((Rideable)E).setPutString(CMLib.xml().getValFromPieces(buf, "PUTSTR", ""));
			((Rideable)E).setMountString(CMLib.xml().getValFromPieces(buf, "MOUNTSTR", ""));
			((Rideable)E).setDismountString(CMLib.xml().getValFromPieces(buf, "DISMOUNTSTR", ""));
			((Rideable)E).setRideString(CMLib.xml().getValFromPieces(buf, "RIDERSTR", ""));
			((Rideable)E).setStateString(CMLib.xml().getValFromPieces(buf, "STATESTR", ""));
			((Rideable)E).setStateStringSubject(CMLib.xml().getValFromPieces(buf, "STATESUBJSTR", ""));
		}
		if(E instanceof Electronics)
		{
			((Electronics)E).setPowerCapacity(CMLib.xml().getIntFromPieces(buf,"POWC"));
			((Electronics)E).setPowerRemaining(CMLib.xml().getIntFromPieces(buf,"POWR"));
			((Electronics)E).activate(CMLib.xml().getBoolFromPieces(buf, "EACT"));
			((Electronics)E).setManufacturerName(CMLib.xml().getValFromPieces(buf, "MANUFACT"));
		}
		if(E instanceof ElecPanel)
		{
			final String panelType=CMLib.xml().getValFromPieces(buf,"SSPANELT");
			final Technical.TechType type = (Technical.TechType)CMath.s_valueOf(Technical.TechType.class, panelType);
			if(type != null)
				((ElecPanel)E).setPanelType(type);
		}
		if(E instanceof TechComponent)
		{
			((TechComponent)E).setInstalledFactor((float)CMLib.xml().getDoubleFromPieces(buf,"INSTF"));
			((TechComponent)E).setRechargeRate((float)CMLib.xml().getDoubleFromPieces(buf,"RECHRATE",((TechComponent)E).getRechargeRate()));
		}
		if(E instanceof ShipEngine)
		{
			((ShipEngine)E).setMaxThrust(CMLib.xml().getIntFromPieces(buf,"SSTHRUST"));
			((ShipEngine)E).setSpecificImpulse(CMLib.xml().getIntFromPieces(buf,"SSIMPL"));
			((ShipEngine)E).setFuelEfficiency(CMLib.xml().getDoubleFromPieces(buf,"SSFEFF"));
			((ShipEngine)E).setMinThrust(CMLib.xml().getIntFromPieces(buf,"SSNTHRUST"));
			((ShipEngine)E).setConstantThruster(CMLib.xml().getBoolFromPieces(buf,"SSCONST",true));
			final String portsStr = CMLib.xml().getValFromPieces(buf, "SSAPORTS", "");
			if(portsStr.length()==0)
				((ShipEngine)E).setAvailPorts(TechComponent.ShipDir.values());
			else
				((ShipEngine)E).setAvailPorts(CMParms.parseEnumList(TechComponent.ShipDir.class, portsStr, ',').toArray(new TechComponent.ShipDir[0]));
		}
		if(E instanceof ShipWarComponent)
		{
			((ShipWarComponent)E).setPermittedNumDirections(CMLib.xml().getIntFromPieces(buf,"SSPDIRS"));
			((ShipWarComponent)E).setPermittedDirections(CMParms.parseEnumList(TechComponent.ShipDir.class, CMLib.xml().getValFromPieces(buf,"SSAPORTS"), ',').toArray(new TechComponent.ShipDir[0]));
			((ShipWarComponent)E).setDamageMsgTypes(CMParms.parseIntList(CMLib.xml().getValFromPieces(buf,"SSMTYPES"),','));
		}
		if(E instanceof PowerGenerator)
		{
			((PowerGenerator)E).setGeneratedAmountPerTick(CMLib.xml().getIntFromPieces(buf,"EGENAMT"));
		}
		if(E instanceof FuelConsumer)
		{
			final List<String> mats = CMParms.parseCommas(CMLib.xml().getValFromPieces(buf,"ECONSTYP"),true);
			final int[] newMats = new int[mats.size()];
			for(int x=0;x<mats.size();x++)
				newMats[x]=CMath.s_int(mats.get(x).trim());
			((FuelConsumer)E).setConsumedFuelType(newMats);
		}
		if(E instanceof Coins)
		{
			((Coins)E).setCurrency(CMLib.xml().getValFromPieces(buf,"CRNC"));
			((Coins)E).setDenomination(CMLib.xml().getDoubleFromPieces(buf,"DENOM"));
		}
		if(E instanceof Recipe)
		{
			((Recipe)E).setCommonSkillID(CMLib.xml().getValFromPieces(buf,"SKILLID"));
			int numSupported = CMLib.xml().getIntFromPieces(buf,"NUMRECIPES");
			if(numSupported<=0)
				numSupported=1;
			((Recipe)E).setTotalRecipePages(numSupported);
			final List<XMLTag> allRecipes = CMLib.xml().getPiecesFromPieces(buf, "RECIPE");
			final List<String> allRecipeStrings=new ArrayList<String>(allRecipes.size());
			for(final XMLTag piece : allRecipes)
				allRecipeStrings.add(piece.value());
			((Recipe)E).setRecipeCodeLines(allRecipeStrings.toArray(new String[0]));
		}
		if(E instanceof Light)
		{
			final String bo=CMLib.xml().getValFromPieces(buf,"BURNOUT");
			if((bo!=null)&&(bo.length()>0))
				((Light)E).setDestroyedWhenBurntOut(CMath.s_bool(bo));
		}

		if(E instanceof Wand)
		{
			final String bo=CMLib.xml().getValFromPieces(buf,"MAXUSE");
			if((bo!=null)&&(bo.length()>0))
				((Wand)E).setMaxUses(CMath.s_int(bo));
		}

		if(E instanceof LandTitle)
			((LandTitle)E).setLandPropertyID(CMLib.xml().getValFromPieces(buf,"LANDID"));
		else
		if(E instanceof PrivateProperty)
		{
			((PrivateProperty)E).setOwnerName(CMLib.xml().getValFromPieces(buf,"OWNERID"));
			((PrivateProperty)E).setPrice(CMLib.xml().getIntFromPieces(buf,"PRICE"));
		}

		if(E instanceof Perfume)
			((Perfume)E).setSmellList(CMLib.xml().getValFromPieces(buf,"SMELLLST"));

		if(E instanceof Food)
		{
			((Food)E).setNourishment(CMLib.xml().getIntFromPieces(buf,"CAPA2"));
			((Food)E).setBite(CMLib.xml().getIntFromPieces(buf,"BITE"));
		}

		if(E instanceof RawMaterial)
			((RawMaterial)E).setDomainSource(CMLib.xml().getValFromPieces(buf,"DOMN"));

		if(E instanceof Drink)
		{
			final int capacity=CMLib.xml().getIntFromPieces(buf,"CAPA2");
			((Drink)E).setLiquidHeld(capacity);
			final String remaining=CMLib.xml().getValFromPieces(buf,"REMAN");
			if(remaining.length()>0)
			{
				((Drink)E).setLiquidRemaining(CMath.s_int(remaining));
				((Drink)E).setLiquidType(CMLib.xml().getIntFromPieces(buf,"LTYPE"));
			}
			else
				((Drink)E).setLiquidRemaining(capacity);
			((Drink)E).setThirstQuenched(CMLib.xml().getIntFromPieces(buf,"DRINK"));
		}
		if(E instanceof Weapon)
		{
			((Weapon)E).setWeaponDamageType(CMLib.xml().getIntFromPieces(buf,"TYPE"));
			((Weapon)E).setWeaponClassification(CMLib.xml().getIntFromPieces(buf,"CLASS"));
			((Weapon)E).setRanges(CMLib.xml().getIntFromPieces(buf,"MINR"),CMLib.xml().getIntFromPieces(buf,"MAXR"));
		}
		if(E instanceof Armor)
		{
			((Armor)E).setClothingLayer(CMLib.xml().getShortFromPieces(buf,"LAYR"));
			((Armor)E).setLayerAttributes(CMLib.xml().getShortFromPieces(buf,"LAYA"));
		}
		if(E instanceof DeadBody)
		{
			if(((DeadBody)E).charStats()==null)
				((DeadBody)E).setCharStats((CharStats)CMClass.getCommon("DefaultCharStats"));
			try
			{
				((DeadBody)E).charStats().setStat(CharStats.STAT_GENDER,CMLib.xml().getValFromPieces(buf,"GENDER").charAt(0));
				((DeadBody)E).setIsPlayerCorpse(CMLib.xml().getBoolFromPieces(buf,"MPLAYR"));
				final String mobName=CMLib.xml().getValFromPieces(buf,"MDNAME");
				if(mobName.length()>0)
				{
					((DeadBody)E).setMobName(mobName);
					((DeadBody)E).setMobDescription(CMLib.xml().getValFromPieces(buf,"MDDESC"));
					((DeadBody)E).setTimeOfDeath(CMLib.xml().getLongFromPieces(buf,"MTOD"));
					((DeadBody)E).setKillerName(CMLib.xml().getValFromPieces(buf,"MKNAME"));
					((DeadBody)E).setIsKillerPlayer(CMLib.xml().getBoolFromPieces(buf,"MKPLAY"));
					((DeadBody)E).setMobHash(CMLib.xml().getIntFromPieces(buf,"MHASH"));
					((DeadBody)E).setMobPKFlag(CMLib.xml().getBoolFromPieces(buf,"MPKILL"));
					((DeadBody)E).setIsDestroyAfterLooting(CMLib.xml().getBoolFromPieces(buf,"MBREAL"));
					((DeadBody)E).setLastMessage(CMLib.xml().getValFromPieces(buf,"MDLMSG"));
					final String mobsXML=CMLib.xml().getValFromPieces(buf,"MOBS");
					if((mobsXML!=null)&&(mobsXML.length()>0))
					{
						final List<MOB> V=new Vector<MOB>();
						final String err=addMOBsFromXML("<MOBS>"+mobsXML+"</MOBS>",V,null);
						if((err.length()==0)&&(V.size()>0))
							((DeadBody)E).setSavedMOB(V.get(0), false);

					}
					final List<XMLLibrary.XMLTag> dblk=CMLib.xml().getContentsFromPieces(buf,"KLTOOL");
					if((dblk!=null)&&(dblk.size()>0))
					{
						final String itemi=CMLib.xml().getValFromPieces(dblk,"KLCLASS");
						final List<XMLLibrary.XMLTag> idat=CMLib.xml().getContentsFromPieces(dblk,"KLDATA");
						final Environmental newOne=CMClass.getUnknown(itemi);
						if(newOne==null)
							Log.errOut("CoffeeMaker","Unknown tool "+itemi+" of "+identifier(E,null)+".  Skipping.");
						else
						{
							setPropertiesStr(newOne,idat,true);
							((DeadBody)E).setKillerTool(newOne);
						}
					}
					else
						((DeadBody)E).setKillerTool(null);
				}
			}
			catch(final Exception e)
			{
			}
			final String raceID=CMLib.xml().getValFromPieces(buf,"MRACE");
			if((raceID.length()>0)&&(CMClass.getRace(raceID)!=null))
			{
				final Race R=CMClass.getRace(raceID);
				((DeadBody)E).charStats().setMyRace(R);
				((DeadBody)E).charStats().setWearableRestrictionsBitmap(((DeadBody)E).charStats().getWearableRestrictionsBitmap()|((DeadBody)E).charStats().getMyRace().forbiddenWornBits());
			}
		}
		if(E instanceof MOB)
		{
			final MOB mob=(MOB)E;
			final String alignStr=CMLib.xml().getValFromPieces(buf,"ALIG");
			if((alignStr.length()>0)&&(CMLib.factions().getFaction(CMLib.factions().AlignID())!=null))
				CMLib.factions().setAlignmentOldRange(mob,CMath.s_int(alignStr));
			CMLib.beanCounter().setMoney(mob,CMLib.xml().getIntFromPieces(buf,"MONEY"));
			mob.setMoneyVariation(CMLib.xml().getDoubleFromPieces(buf,"VARMONEY"));
			setGenMobInventory((MOB)E,buf);
			setGenMobAbilities((MOB)E,buf);
			setFactionFromXML((MOB)E,buf);

			if(E instanceof Banker)
			{
				((Banker)E).setBankChain(CMLib.xml().getValFromPieces(buf,"BANK"));
				((Banker)E).setCoinInterest(CMLib.xml().getDoubleFromPieces(buf,"COININT"));
				((Banker)E).setItemInterest(CMLib.xml().getDoubleFromPieces(buf,"ITEMINT"));
				final String loanInt=CMLib.xml().getValFromPieces(buf,"LOANINT");
				if(loanInt.length()>0)
					((Banker)E).setLoanInterest(CMath.s_double(loanInt));
			}

			if(E instanceof PostOffice)
			{
				((PostOffice)E).setPostalChain(CMLib.xml().getValFromPieces(buf,"POSTCHAIN"));
				((PostOffice)E).setMinimumPostage(CMLib.xml().getDoubleFromPieces(buf,"POSTMIN"));
				((PostOffice)E).setPostagePerPound(CMLib.xml().getDoubleFromPieces(buf,"POSTLBS"));
				((PostOffice)E).setHoldFeePerPound(CMLib.xml().getDoubleFromPieces(buf,"POSTHOLD"));
				((PostOffice)E).setFeeForNewBox(CMLib.xml().getDoubleFromPieces(buf,"POSTNEW"));
				((PostOffice)E).setMaxMudMonthsHeld(CMLib.xml().getIntFromPieces(buf,"POSTHELD"));
			}

			if(E instanceof Librarian)
			{
				((Librarian)E).setLibraryChain(CMLib.xml().getValFromPieces(buf,"LIBRCHAIN"));
				((Librarian)E).setOverdueCharge(CMLib.xml().getDoubleFromPieces(buf,"LIBROVERCHG"));
				((Librarian)E).setDailyOverdueCharge(CMLib.xml().getDoubleFromPieces(buf,"LIBRDAYCHG"));
				((Librarian)E).setOverdueChargePct(CMLib.xml().getDoubleFromPieces(buf,"LIBROVERPCT"));
				((Librarian)E).setDailyOverdueChargePct(CMLib.xml().getDoubleFromPieces(buf,"LIBDAYPCT"));
				((Librarian)E).setMinOverdueDays(CMLib.xml().getIntFromPieces(buf,"LIBMINDAYS"));
				((Librarian)E).setMaxOverdueDays(CMLib.xml().getIntFromPieces(buf,"LIBMAXDAYS"));
				((Librarian)E).setMaxBorrowed(CMLib.xml().getIntFromPieces(buf,"LIBMAXBORROW"));
				((Librarian)E).setContributorMask(CMLib.xml().getValFromPieces(buf,"LIBRCMASK"));
			}
			
			if(E instanceof Auctioneer)
			{
				((Auctioneer)E).setAuctionHouse(CMLib.xml().getValFromPieces(buf,"AUCHOUSE"));
				//((Auctioneer)E).setLiveListingPrice(CMLib.xml().getDoubleFromPieces(buf,"LIVEPRICE"));
				((Auctioneer)E).setTimedListingPrice(CMLib.xml().getDoubleFromPieces(buf,"TIMEPRICE"));
				((Auctioneer)E).setTimedListingPct(CMLib.xml().getDoubleFromPieces(buf,"TIMEPCT"));
				//((Auctioneer)E).setLiveFinalCutPct(CMLib.xml().getDoubleFromPieces(buf,"LIVECUT"));
				((Auctioneer)E).setTimedFinalCutPct(CMLib.xml().getDoubleFromPieces(buf,"TIMECUT"));
				((Auctioneer)E).setMaxTimedAuctionDays(CMLib.xml().getIntFromPieces(buf,"MAXADAYS"));
				((Auctioneer)E).setMinTimedAuctionDays(CMLib.xml().getIntFromPieces(buf,"MINADAYS"));
			}

			if(E instanceof Deity)
			{
				final Deity godmob=(Deity)E;
				godmob.setClericRequirements(CMLib.xml().getValFromPieces(buf,"CLEREQ"));
				godmob.setWorshipRequirements(CMLib.xml().getValFromPieces(buf,"WORREQ"));
				godmob.setClericRitual(CMLib.xml().getValFromPieces(buf,"CLERIT"));
				godmob.setWorshipRitual(CMLib.xml().getValFromPieces(buf,"WORRIT"));
				godmob.setClericSin(CMLib.xml().getValFromPieces(buf,"CLERSIT"));
				godmob.setWorshipSin(CMLib.xml().getValFromPieces(buf,"WORRSIT"));
				godmob.setClericPowerup(CMLib.xml().getValFromPieces(buf,"CLERPOW"));
				godmob.setServiceRitual(CMLib.xml().getValFromPieces(buf,"SVCRIT"));

				List<XMLLibrary.XMLTag> V=CMLib.xml().getContentsFromPieces(buf,"BLESSINGS");
				if(V==null)
				{
					Log.errOut("CoffeeMaker","Error parsing 'BLESSINGS' of "+identifier(E,null)+".  Load aborted");
					return;
				}
				for(int i=0;i<V.size();i++)
				{
					final XMLTag ablk=V.get(i);
					if((!ablk.tag().equalsIgnoreCase("BLESS"))||(ablk.contents()==null))
					{
						Log.errOut("CoffeeMaker","Error parsing 'BLESS' of "+identifier(E,null)+".  Load aborted");
						return;
					}
					final Ability newOne=CMClass.getAbility(ablk.getValFromPieces("BLCLASS"));
					if(newOne==null)
					{
						Log.errOut("CoffeeMaker","Unknown bless "+ablk.getValFromPieces("BLCLASS")+" on "+identifier(E,null)+", skipping.");
						continue;
					}
					final boolean clericsOnly=ablk.getBoolFromPieces("BLONLY");
					final List<XMLLibrary.XMLTag> adat=ablk.getContentsFromPieces("BLDATA");
					if(adat==null)
					{
						Log.errOut("CoffeeMaker","Error parsing 'BLESS DATA' of "+identifier(E,null)+".  Load aborted");
						return;
					}
					setPropertiesStr(newOne,adat,true);
					godmob.addBlessing(newOne,clericsOnly);
				}
				V=CMLib.xml().getContentsFromPieces(buf,"CURSES");
				if(V!=null)
				{
					for(int i=0;i<V.size();i++)
					{
						final XMLTag ablk=V.get(i);
						if((!ablk.tag().equalsIgnoreCase("CURSE"))||(ablk.contents()==null))
						{
							Log.errOut("CoffeeMaker","Error parsing 'CURSE' of "+identifier(E,null)+".  Load aborted");
							return;
						}
						final Ability newOne=CMClass.getAbility(ablk.getValFromPieces("CUCLASS"));
						if(newOne==null)
						{
							Log.errOut("CoffeeMaker","Unknown curse "+ablk.getValFromPieces("CUCLASS")+" on "+identifier(E,null)+", skipping.");
							continue;
						}
						final boolean clericsOnly=ablk.getBoolFromPieces("CUONLY");
						final List<XMLLibrary.XMLTag> adat=ablk.getContentsFromPieces("CUDATA");
						if(adat==null)
						{
							Log.errOut("CoffeeMaker","Error parsing 'CURSE DATA' of "+identifier(E,null)+".  Load aborted");
							return;
						}
						setPropertiesStr(newOne,adat,true);
						godmob.addCurse(newOne,clericsOnly);
					}
				}
				V=CMLib.xml().getContentsFromPieces(buf,"POWERS");
				if(V!=null)
				{
					for(int i=0;i<V.size();i++)
					{
						final XMLTag ablk=V.get(i);
						if((!ablk.tag().equalsIgnoreCase("POWER"))||(ablk.contents()==null))
						{
							Log.errOut("CoffeeMaker","Error parsing 'POWER' of "+identifier(E,null)+".  Load aborted");
							return;
						}
						final Ability newOne=CMClass.getAbility(ablk.getValFromPieces("POCLASS"));
						if(newOne==null)
						{
							Log.errOut("CoffeeMaker","Unknown power "+ablk.getValFromPieces("POCLASS")+" on "+identifier(E,null)+", skipping.");
							continue;
						}
						final List<XMLLibrary.XMLTag> adat=ablk.getContentsFromPieces("PODATA");
						if(adat==null)
						{
							Log.errOut("CoffeeMaker","Error parsing 'POWER DATA' of "+identifier(E,null)+".  Load aborted");
							return;
						}
						setPropertiesStr(newOne,adat,true);
						godmob.addPower(newOne);
					}
				}
			}
			List<String> V9=CMParms.parseSemicolons(CMLib.xml().getValFromPieces(buf,"TATTS"),true);
			for(final Enumeration<Tattoo> e=((MOB)E).tattoos();e.hasMoreElements();)
				((MOB)E).delTattoo(e.nextElement());
			for(final String tatt : V9)
				((MOB)E).addTattoo(((Tattoo)CMClass.getCommon("DefaultTattoo")).parse(tatt));

			V9=CMParms.parseSemicolons(CMLib.xml().getValFromPieces(buf,"EDUS"),true);
			((MOB)E).delAllExpertises();
			for(int v=0;v<V9.size();v++)
				((MOB)E).addExpertise(V9.get(v));

			if(E instanceof ShopKeeper)
				populateShops((ShopKeeper)E,buf);
		}
	}

	@Override
	public String getAccountXML(PlayerAccount account, Set<CMObject> custom, Set<String> files)
	{
		if(account==null)
			return "";
		if(account.getAccountName().length()==0)
			return "";
		final StringBuilder xml=new StringBuilder("");
		xml.append("<NAME>").append(account.getAccountName()).append("</NAME>");
		xml.append("<PASS>").append(account.getPasswordStr()).append("</PASS>");
		xml.append("<AXML>").append(account.getXML()).append("</AXML>");
		xml.append("<PLAYERS>");
		for(final Enumeration<MOB> m=account.getLoadPlayers(); m.hasMoreElements(); )
		{
			final MOB M=m.nextElement();
			xml.append("<PLAYER>").append(getPlayerXML(M,custom,files)).append("</PLAYER>");
		}
		xml.append("</PLAYERS>");
		return xml.toString();
	}

	@Override
	public String getPlayerXML(MOB mob, Set<CMObject> custom, Set<String> files)
	{
		if(mob==null)
			return "";
		if(mob.Name().length()==0)
			return "";
		final PlayerStats pstats=mob.playerStats();
		if(pstats==null)
			return "";

		final String strStartRoomID=(mob.getStartRoom()!=null)?CMLib.map().getExtendedRoomID(mob.getStartRoom()):"";
		final String strOtherRoomID=(mob.location()!=null)?CMLib.map().getExtendedRoomID(mob.location()):"";
		final StringBuilder pfxml=new StringBuilder(pstats.getXML());
		if(mob.tattoos().hasMoreElements())
		{
			pfxml.append("<TATTS>");
			Tattoo T = null;
			for(final Enumeration<Tattoo> e=mob.tattoos(); e.hasMoreElements();)
			{
				T=e.nextElement();
				if(T.getTattooName().startsWith("<TATTS>"))
					T.set(T.getTattooName().substring(7));
				pfxml.append(T.toString()+";");
			}
			pfxml.append("</TATTS>");
		}
		if(mob.expertises().hasMoreElements())
		{
			pfxml.append("<EDUS>");
			for(final Enumeration<String> x=mob.expertises();x.hasMoreElements();)
				pfxml.append(x.nextElement()).append(';');
			pfxml.append("</EDUS>");
		}
		pfxml.append(CMLib.xml().convertXMLtoTag("IMG",mob.rawImage()));

		final StringBuilder str=new StringBuilder("");
		str.append(CMLib.xml().convertXMLtoTag("CLASSID",mob.ID()));
		str.append(CMLib.xml().convertXMLtoTag("NAME",mob.Name()));
		str.append(CMLib.xml().convertXMLtoTag("PASS",pstats.getPasswordStr()));
		str.append(CMLib.xml().convertXMLtoTag("CLASS",mob.baseCharStats().getMyClassesStr()));
		str.append(CMLib.xml().convertXMLtoTag("RACE",mob.baseCharStats().getMyRace().ID()));
		str.append(CMLib.xml().convertXMLtoTag("GEND",""+((char)mob.baseCharStats().getStat(CharStats.STAT_GENDER))));
		for(final int i : CharStats.CODES.BASECODES())
			str.append(CMLib.xml().convertXMLtoTag(CMStrings.limit(CharStats.CODES.NAME(i),3),mob.baseCharStats().getStat(i)));
		str.append(CMLib.xml().convertXMLtoTag("HIT",mob.baseState().getHitPoints()));
		str.append(CMLib.xml().convertXMLtoTag("LVL",mob.baseCharStats().getMyLevelsStr()));
		str.append(CMLib.xml().convertXMLtoTag("MANA",mob.baseState().getMana()));
		str.append(CMLib.xml().convertXMLtoTag("MOVE",mob.baseState().getMovement()));
		str.append(CMLib.xml().convertXMLtoTag("EXP",mob.getExperience()));
		str.append(CMLib.xml().convertXMLtoTag("EXLV",mob.getExpNextLevel()));
		str.append(CMLib.xml().convertXMLtoTag("WORS",mob.getWorshipCharID()));
		str.append(CMLib.xml().convertXMLtoTag("PRAC",mob.getPractices()));
		str.append(CMLib.xml().convertXMLtoTag("TRAI",mob.getTrains()));
		str.append(CMLib.xml().convertXMLtoTag("AGEH",mob.getAgeMinutes()));
		str.append(CMLib.xml().convertXMLtoTag("GOLD",mob.getMoney()));
		str.append(CMLib.xml().convertXMLtoTag("WIMP",mob.getWimpHitPoint()));
		str.append(CMLib.xml().convertXMLtoTag("QUES",mob.getQuestPoint()));
		str.append(CMLib.xml().convertXMLtoTag("ROID",strStartRoomID+"||"+strOtherRoomID));
		str.append(CMLib.xml().convertXMLtoTag("DATE",pstats.getLastDateTime()));
		str.append(CMLib.xml().convertXMLtoTag("CHAN",pstats.getChannelMask()));
		str.append(CMLib.xml().convertXMLtoTag("ATTA",mob.basePhyStats().attackAdjustment()));
		str.append(CMLib.xml().convertXMLtoTag("AMOR",mob.basePhyStats().armor()));
		str.append(CMLib.xml().convertXMLtoTag("DAMG",mob.basePhyStats().damage()));
		str.append(CMLib.xml().convertXMLtoTag("BTMP",mob.getAttributesBitmap()));
		str.append(CMLib.xml().convertXMLtoTag("LEIG",mob.getLiegeID()));
		str.append(CMLib.xml().convertXMLtoTag("HEIT",mob.basePhyStats().height()));
		str.append(CMLib.xml().convertXMLtoTag("WEIT",mob.basePhyStats().weight()));
		str.append(CMLib.xml().convertXMLtoTag("PRPT",CMLib.xml().parseOutAngleBrackets(pstats.getPrompt())));
		str.append(CMLib.xml().convertXMLtoTag("COLR",pstats.getColorStr()));
		for(final Pair<Clan,Integer> p : mob.clans())
			str.append("<CLAN ROLE=").append(p.second.toString()).append(">").append(p.first.clanID()).append("</CLAN>");
		str.append(CMLib.xml().convertXMLtoTag("LSIP",pstats.getLastIP()));
		str.append(CMLib.xml().convertXMLtoTag("EMAL",pstats.getEmail()));
		str.append(CMLib.xml().convertXMLtoTag("PFIL",pfxml.toString()));
		str.append(CMLib.xml().convertXMLtoTag("SAVE",mob.baseCharStats().getNonBaseStatsAsString()));
		str.append(CMLib.xml().convertXMLtoTag("DESC",mob.description()));

		str.append(getExtraEnvPropertiesStr(mob));

		str.append(getGenMobAbilities(mob));

		str.append(getGenScripts(mob,true));

		possibleAddElectronicsManufacturers(mob, custom);

		str.append(getGenMobInventory(mob));
		
		str.append(getPlayerExtraInventory(mob));

		str.append(getFactionXML(mob));

		final StringBuilder fols=new StringBuilder("");
		for(int f=0;f<mob.numFollowers();f++)
		{
			final MOB thisMOB=mob.fetchFollower(f);
			possibleAddElectronicsManufacturers(thisMOB, custom);
			if((thisMOB!=null)&&(thisMOB.isMonster())&&(!thisMOB.isPossessing()))
			{
				fols.append("<FOLLOWER>");
				fols.append(CMLib.xml().convertXMLtoTag("FCLAS",CMClass.classID(thisMOB)));
				fols.append(CMLib.xml().convertXMLtoTag("FTEXT",thisMOB.text()));
				fols.append(CMLib.xml().convertXMLtoTag("FLEVL",thisMOB.basePhyStats().level()));
				fols.append(CMLib.xml().convertXMLtoTag("FABLE",thisMOB.basePhyStats().ability()));
				fols.append("</FOLLOWER>");
			}
		}
		str.append(CMLib.xml().convertXMLtoTag("FOLLOWERS",fols.toString()));

		possiblyAddCustomRace(mob, custom);
		possiblyAddCustomClass(mob, custom);

		fillFileSet(mob,files);
		return str.toString();
	}

	protected String addPlayersOnlyFromXML(List<XMLLibrary.XMLTag> mV, List<MOB> addMobs, Session S)
	{
		for(int m=0;m<mV.size();m++)
		{
			final XMLTag mblk=mV.get(m);
			if((!mblk.tag().equalsIgnoreCase("PLAYER"))||(mblk.contents()==null))
				return unpackErr("PLAYERs","bad 'mblk'");
			String classID=mblk.getValFromPieces( "CLASSID");
			if((classID==null)||(classID.length()==0))
				classID="StdMOB";
			final MOB mob=CMClass.getMOB(classID);
			mob.setPlayerStats((PlayerStats)CMClass.getCommon("DefaultPlayerStats"));
			mob.setName(mblk.getValFromPieces("NAME"));
			mob.playerStats().setPassword(mblk.getValFromPieces("PASS"));
			mob.baseCharStats().setMyClasses(mblk.getValFromPieces("CLASS"));
			mob.baseCharStats().setMyLevels(mblk.getValFromPieces("LVL"));
			int level=0;
			for(int i=0;i<mob.baseCharStats().numClasses();i++)
				level+=mob.baseCharStats().getClassLevel(mob.baseCharStats().getMyClass(i));
			mob.basePhyStats().setLevel(level);
			mob.baseCharStats().setMyRace(CMClass.getRace(mblk.getValFromPieces("RACE")));
			mob.baseCharStats().setStat(CharStats.STAT_GENDER,mblk.getValFromPieces("GEND").charAt(0));
			for(final int i : CharStats.CODES.BASECODES())
				mob.baseCharStats().setStat(i,mblk.getIntFromPieces(CMStrings.limit(CharStats.CODES.NAME(i),3)));
			mob.baseState().setHitPoints(mblk.getIntFromPieces("HIT"));
			mob.baseState().setMana(mblk.getIntFromPieces("MANA"));
			mob.baseState().setMovement(mblk.getIntFromPieces("MOVE"));
			final String alignStr=mblk.getValFromPieces("ALIG");
			if((alignStr.length()>0)&&(CMLib.factions().getFaction(CMLib.factions().AlignID())!=null))
				CMLib.factions().setAlignmentOldRange(mob,CMath.s_int(alignStr));
			mob.setExperience(mblk.getIntFromPieces("EXP"));
			//mob.setExpNextLevel(CMLib.xml().getIntFromPieces(mblk.contents,"EXLV"));
			mob.setWorshipCharID(mblk.getValFromPieces("WORS"));
			mob.setPractices(mblk.getIntFromPieces("PRAC"));
			mob.setTrains(mblk.getIntFromPieces("TRAI"));
			mob.setAgeMinutes(mblk.getIntFromPieces("AGEH"));
			mob.setWimpHitPoint(mblk.getIntFromPieces("WIMP"));
			mob.setQuestPoint(mblk.getIntFromPieces("QUES"));
			String roomID=mblk.getValFromPieces("ROID");
			if(roomID==null)
				roomID="";
			final int x=roomID.indexOf("||");
			if(x>=0)
			{
				mob.setLocation(CMLib.map().getRoom(roomID.substring(x+2)));
				roomID=roomID.substring(0,x);
			}
			mob.setStartRoom(CMLib.map().getRoom(roomID));
			mob.playerStats().setLastDateTime(mblk.getLongFromPieces("DATE"));
			mob.playerStats().setChannelMask(mblk.getIntFromPieces("CHAN"));
			mob.basePhyStats().setAttackAdjustment(mblk.getIntFromPieces("ATTA"));
			mob.basePhyStats().setArmor(mblk.getIntFromPieces("AMOR"));
			mob.basePhyStats().setDamage(mblk.getIntFromPieces("DAMG"));
			mob.setAttributesBitmap(mblk.getIntFromPieces("BTMP"));
			mob.setLiegeID(mblk.getValFromPieces("LEIG"));
			mob.basePhyStats().setHeight(mblk.getIntFromPieces("HEIT"));
			mob.basePhyStats().setWeight(mblk.getIntFromPieces("WEIT"));
			mob.playerStats().setPrompt(CMLib.xml().restoreAngleBrackets(mblk.getValFromPieces("PRPT")));
			final String colorStr=mblk.getValFromPieces("COLR");
			if((colorStr!=null)&&(colorStr.length()>0)&&(!colorStr.equalsIgnoreCase("NULL")))
				mob.playerStats().setColorStr(colorStr);
			final List<XMLTag> clanPieces=mblk.getPiecesFromPieces( "CLAN");
			final String oldRole=mblk.getValFromPieces("CLRO");
			if((clanPieces.size()==1)&&(oldRole!=null)&&(oldRole.length()>0))
				mob.setClan(clanPieces.get(0).value(), CMath.s_int(oldRole));
			else
			for(final XMLTag p : clanPieces)
				mob.setClan(p.value(), CMath.s_int(p.parms().get("ROLE")));
			mob.playerStats().setLastIP(mblk.getValFromPieces("LSIP"));
			mob.playerStats().setEmail(mblk.getValFromPieces("EMAL"));
			final String buf=mblk.getValFromPieces("PFIL");
			mob.playerStats().setXML(buf);
			List<String> V9=CMParms.parseSemicolons(CMLib.xml().returnXMLValue(buf,"TATTS"),true);
			for(final Enumeration<Tattoo> e=mob.tattoos();e.hasMoreElements();)
				mob.delTattoo(e.nextElement());
			for(String tatt : V9)
				mob.addTattoo(((Tattoo)CMClass.getCommon("DefaultTattoo")).parse(tatt));
			V9=CMParms.parseSemicolons(CMLib.xml().returnXMLValue(buf,"EDUS"),true);
			mob.delAllExpertises();
			for(int v=0;v<V9.size();v++) 
				mob.addExpertise(V9.get(v));
			mob.baseCharStats().setNonBaseStatsFromString(mblk.getValFromPieces("SAVE"));
			mob.setDescription(mblk.getValFromPieces("DESC"));
			mob.setImage(CMLib.xml().returnXMLValue(buf,"IMG"));

			setExtraEnvProperties(mob,mblk.contents());

			setGenMobAbilities(mob,mblk.contents());

			setGenScripts(mob,mblk.contents(),true);

			setGenMobInventory(mob,mblk.contents());

			setPlayerExtraInventory(mob,mblk.contents());

			setFactionFromXML(mob,mblk.contents());

			final List<XMLLibrary.XMLTag> iV=mblk.getContentsFromPieces("FOLLOWERS");
			if(iV==null)
				return unpackErr("PFols","null 'iV'",mblk);
			for(int i=0;i<iV.size();i++)
			{
				final XMLTag fblk=iV.get(i);
				if((!fblk.tag().equalsIgnoreCase("FOLLOWER"))||(fblk.contents()==null))
					return unpackErr("PFols","??"+fblk.tag());
				final String mobClass=fblk.getValFromPieces("FCLAS");
				final MOB newFollower=CMClass.getMOB(mobClass);
				if(newFollower==null)
					return unpackErr("PFols","null 'iClass': "+mobClass);
				newFollower.basePhyStats().setLevel(fblk.getIntFromPieces("FLEVL"));
				newFollower.basePhyStats().setAbility(fblk.getIntFromPieces("FABLE"));
				newFollower.setMiscText(fblk.getValFromPieces("FTEXT"));
				newFollower.recoverCharStats();
				newFollower.recoverPhyStats();
				newFollower.recoverMaxState();
				newFollower.resetToMaxState();
				mob.addFollower(newFollower,-1);
			}

			mob.recoverCharStats();
			mob.recoverPhyStats();
			mob.recoverMaxState();
			mob.resetToMaxState();
			addMobs.add(mob);
		}
		return "";
	}

	@Override
	public String addPlayersAndAccountsFromXML(String xmlBuffer, List<PlayerAccount> addAccounts, List<MOB> addMobs, Session S)
	{
		final List<XMLLibrary.XMLTag> xml=CMLib.xml().parseAllXML(xmlBuffer);
		if(xml==null)
			return unpackErr("PLAYERs","null 'xml'");
		List<XMLLibrary.XMLTag> mV=CMLib.xml().getContentsFromPieces(xml,"PLAYERS");
		if(mV!=null)
			return addPlayersOnlyFromXML(mV,addMobs,S);
		else
		{
			mV=CMLib.xml().getContentsFromPieces(xml,"ACCOUNTS");
			if(mV==null)
				return unpackErr("PLAYERs","null 'mV'",xml);
			for(int m=0;m<mV.size();m++)
			{
				final XMLTag mblk=mV.get(m);
				if((!mblk.tag().equalsIgnoreCase("ACCOUNT"))||(mblk.contents()==null))
					return unpackErr("ACCOUNTs","bad 'mblk'");
				PlayerAccount account = null;
				account = (PlayerAccount)CMClass.getCommon("DefaultPlayerAccount");
				final String name=mblk.getValFromPieces( "NAME");
				final String password=mblk.getValFromPieces( "PASS");
				final XMLTag xmlPiece=mblk.getPieceFromPieces( "AXML");
				final String accountXML=xmlPiece.value();
				final XMLTag playersPiece=mblk.getPieceFromPieces( "PLAYERS");
				final Vector<String> names = new Vector<String>();
				final List<MOB> accountMobs=new Vector<MOB>();
				final String err=addPlayersOnlyFromXML(playersPiece.contents(),accountMobs,S);
				if(err.length()>0)
					return err;
				addMobs.addAll(accountMobs);
				for(final MOB M : accountMobs)
				{
					M.playerStats().setAccount(account);
					names.add(M.Name());
				}
				account.setAccountName(CMStrings.capitalizeAndLower(name));
				account.setPassword(password);
				account.setPlayerNames(names);
				account.setXML(accountXML);
				addAccounts.add(account);
			}
		}
		return "";
	}

	@Override
	public String getExtraEnvPropertiesStr(Environmental E)
	{
		final StringBuilder text=new StringBuilder("");

		if(E instanceof Economics)
		{
			text.append(CMLib.xml().convertXMLtoTag("PREJFC",((Economics)E).prejudiceFactors()));
			text.append(CMLib.xml().convertXMLtoTag("IGNMSK",((Economics)E).ignoreMask()));
			text.append(CMLib.xml().convertXMLtoTag("BUDGET",((Economics)E).budget()));
			text.append(CMLib.xml().convertXMLtoTag("DEVALR",((Economics)E).devalueRate()));
			text.append(CMLib.xml().convertXMLtoTag("INVRER",((Economics)E).invResetRate()));
			final String[] prics=((Economics)E).itemPricingAdjustments();
			if(prics.length==0)
				text.append("<IPRICS />");
			else
			{
				text.append("<IPRICS>");
				for (final String pric : prics)
					text.append(CMLib.xml().convertXMLtoTag("IPRIC",CMLib.xml().parseOutAngleBrackets(pric)));
				text.append("</IPRICS>");
			}

		}

		text.append(CMLib.xml().convertXMLtoTag("IMG",E.rawImage()));

		if(E instanceof PhysicalAgent)
		{
			final PhysicalAgent P = (PhysicalAgent)E;
			final StringBuilder behaviorstr=new StringBuilder("");
			for(final Enumeration<Behavior> e=P.behaviors();e.hasMoreElements();)
			{
				final Behavior B=e.nextElement();
				if(B!=null)
				{
					behaviorstr.append("<BHAVE>");
					behaviorstr.append(CMLib.xml().convertXMLtoTag("BCLASS",CMClass.classID(B)));
					behaviorstr.append(CMLib.xml().convertXMLtoTag("BPARMS",CMLib.xml().parseOutAngleBrackets(B.getParms())));
					behaviorstr.append("</BHAVE>");
				}
			}
			text.append(CMLib.xml().convertXMLtoTag("BEHAVES",behaviorstr.toString()));
		}

		if(E instanceof Physical)
		{
			final Physical P = (Physical)E;
			final StringBuilder affectstr=new StringBuilder("");
			for(int a=0;a<P.numEffects();a++) // definitely personal
			{
				final Ability A=P.fetchEffect(a);
				if((A!=null)&&(A.isSavable()))
				{
					affectstr.append("<AFF>");
					affectstr.append(CMLib.xml().convertXMLtoTag("ACLASS",CMClass.classID(A)));
					affectstr.append(CMLib.xml().convertXMLtoTag("ATEXT",CMLib.xml().parseOutAngleBrackets(A.text())));
					affectstr.append("</AFF>");
				}
			}
			text.append(CMLib.xml().convertXMLtoTag("AFFECS",affectstr.toString()));
		}

		final String[] codes=E.getStatCodes();
		for(int i=E.getSaveStatIndex();i<codes.length;i++)
			text.append(CMLib.xml().convertXMLtoTag(codes[i].toUpperCase(),E.getStat(codes[i].toUpperCase())));
		return text.toString();
	}

	public void fillFileSet(List<String> V, Set<String> H)
	{
		if(H==null)
			return;
		if(V==null)
			return;
		for(int v=0;v<V.size();v++)
			if((!H.contains(V.get(v)))
			&&(V.get(v) != null))
				H.add(V.get(v));
	}

	@Override
	public void fillFileSet(Environmental E, Set<String> H)
	{
		if(E==null)
			return;
		if(E instanceof PhysicalAgent)
		{
			final PhysicalAgent P=(PhysicalAgent)E;
			for(final Enumeration<Behavior> e=P.behaviors();e.hasMoreElements();)
			{
				final Behavior B=e.nextElement();
				if(B!=null)
					fillFileSet(B.externalFiles(),H);
			}
			for(final Enumeration<ScriptingEngine> e=P.scripts();e.hasMoreElements();)
			{
				final ScriptingEngine SE=e.nextElement();
				if(SE!=null)
					fillFileSet(SE.externalFiles(),H);
			}
		}
		if(E instanceof Physical)
		{
			final Physical P=(Physical)E;
			for(int a=0;a<P.numEffects();a++)
			{
				final Ability A=P.fetchEffect(a);
				if((A!=null)&&(A.isSavable()))
					fillFileSet(A.externalFiles(),H);
			}
		}
		if(E instanceof MOB)
		{
			final MOB M=(MOB)E;
			for(int i=0;i<M.numItems();i++)
				fillFileSet(M.getItem(i),H);
		}
		if(E instanceof ShopKeeper)
		{
			for(final Iterator<Environmental> i=((ShopKeeper)E).getShop().getStoreInventory();i.hasNext();)
				fillFileSet(i.next(),H);
		}
	}

	protected void fillFileMap(Environmental E, List<String> V, Map<String,Set<Environmental>> H)
	{
		if(H==null)
			return;
		if(V==null)
			return;
		for(String path : V)
		{
			Set<Environmental> L;
			if(H.containsKey(path))
				L=H.get(path);
			else
			{
				L=new HashSet<Environmental>(1);
				H.put(path, L);
			}
			if(!L.contains(E))
				L.add(E);
		}
	}

	@Override
	public void fillFileMap(Environmental E, Map<String,Set<Environmental>> H)
	{
		if(E==null)
			return;
		if(E instanceof PhysicalAgent)
		{
			final PhysicalAgent P=(PhysicalAgent)E;
			for(final Enumeration<Behavior> e=P.behaviors();e.hasMoreElements();)
			{
				final Behavior B=e.nextElement();
				if(B!=null)
					fillFileMap(E, B.externalFiles(),H);
			}
			for(final Enumeration<ScriptingEngine> e=P.scripts();e.hasMoreElements();)
			{
				final ScriptingEngine SE=e.nextElement();
				if(SE!=null)
					fillFileMap(E, SE.externalFiles(),H);
			}
		}
		if(E instanceof Physical)
		{
			final Physical P=(Physical)E;
			for(int a=0;a<P.numEffects();a++)
			{
				final Ability A=P.fetchEffect(a);
				if((A!=null)&&(A.isSavable()))
					fillFileMap(E, A.externalFiles(),H);
			}
		}
		if(E instanceof MOB)
		{
			final MOB M=(MOB)E;
			for(int i=0;i<M.numItems();i++)
				fillFileMap(M.getItem(i),H);
		}
		if(E instanceof ShopKeeper)
		{
			for(final Iterator<Environmental> i=((ShopKeeper)E).getShop().getStoreInventory();i.hasNext();)
				fillFileMap(i.next(),H);
		}
		if(E instanceof Room)
		{
			for(final Enumeration<MOB> m=((Room)E).inhabitants();m.hasMoreElements();)
				fillFileMap(m.nextElement(),H);
			for(final Enumeration<Item> i=((Room)E).items();i.hasMoreElements();)
				fillFileMap(i.nextElement(),H);
		}
	}

	@Override
	public String getPhyStatsStr(PhyStats E)
	{
		return E.ability()+"|"+
				E.armor()+"|"+
				E.attackAdjustment()+"|"+
				E.damage()+"|"+
				E.disposition()+"|"+
				E.level()+"|"+
				E.rejuv()+"|"+
				E.speed()+"|"+
				E.weight()+"|"+
				E.height()+"|"+
				E.sensesMask()+"|";
	}

	@Override
	public String getCharStateStr(CharState E)
	{
		return E.getFatigue()+"|"+
				E.getHitPoints()+"|"+
				E.getHunger()+"|"+
				E.getMana()+"|"+
				E.getMovement()+"|"+
				E.getThirst()+"|";
	}

	@Override
	public String getCharStatsStr(CharStats E)
	{
		final StringBuilder str=new StringBuilder("");
		for(final int i : CharStats.CODES.ALLCODES())
			str.append(E.getStat(i)+"|");
		return str.toString();
	}

	@Override
	public String getEnvPropertiesStr(Environmental E)
	{
		final StringBuilder text=new StringBuilder("");
		text.append(CMLib.xml().convertXMLtoTag("NAME",E.Name()));
		text.append(CMLib.xml().convertXMLtoTag("DESC",E.description()));
		text.append(CMLib.xml().convertXMLtoTag("DISP",E.displayText()));
		if(E instanceof Physical)
			text.append(CMLib.xml().convertXMLtoTag("PROP",getPhyStatsStr(((Physical)E).basePhyStats())));
		text.append(getExtraEnvPropertiesStr(E));
		if(E instanceof PhysicalAgent)
			text.append(getGenScripts((PhysicalAgent)E,false));
		return text.toString();
	}

	@Override
	public void setCharStats(CharStats E, String props)
	{
		final String[] split=props.split("\\|");
		final int totalStats=CharStats.CODES.TOTAL();
		for(int x=0;x<split.length && (x<totalStats);x++)
			E.setStat(x,CMath.s_int(split[x]));
	}

	@Override
	public void setCharState(CharState E, String props)
	{
		final int[] nums=new int[6];
		final String[] split=props.split("\\|");
		final int totalStats=6;
		for(int x=0;x<split.length && (x<totalStats);x++)
			nums[x]=CMath.s_int(split[x]);
		E.setFatigue(nums[0]);
		E.setHitPoints(nums[1]);
		E.setHunger(nums[2]);
		E.setMana(nums[3]);
		E.setMovement(nums[4]);
		E.setThirst(nums[5]);
	}

	@Override
	public void setPhyStats(PhyStats E, String props)
	{
		if(props.length()==0)
			return;
		final double[] nums=new double[11];
		int x=0;
		int lastBar=0;
		for(int y=0;y<props.length();y++)
		{
			if(props.charAt(y)=='|')
			{
				try
				{
					nums[x] = Double.valueOf(props.substring(lastBar, y)).doubleValue();
				}
				catch (final Exception e)
				{
					nums[x] = CMath.s_int(props.substring(lastBar, y));
				}
				x++;
				lastBar=y+1;
			}
		}
		if(lastBar<props.length())
		{
			try
			{
				nums[x] = Double.valueOf(props.substring(lastBar)).doubleValue();
			}
			catch (final Exception e)
			{
				nums[x] = CMath.s_int(props.substring(lastBar));
			}
		}
		E.setAbility((int)Math.round(nums[0]));
		E.setArmor((int)Math.round(nums[1]));
		E.setAttackAdjustment((int)Math.round(nums[2]));
		E.setDamage((int)Math.round(nums[3]));
		E.setDisposition((int)Math.round(nums[4]));
		E.setLevel((int)Math.round(nums[5]));
		E.setRejuv((int)Math.round(nums[6]));
		E.setSpeed(nums[7]);
		E.setWeight((int)Math.round(nums[8]));
		E.setHeight((int)Math.round(nums[9]));
		E.setSensesMask((int)Math.round(nums[10]));
	}

	@Override
	public void setEnvProperties(Environmental E, List<XMLTag> buf)
	{
		E.setName(CMLib.xml().getValFromPieces(buf,"NAME"));
		E.setDescription(CMLib.xml().getValFromPieces(buf,"DESC"));
		E.setDisplayText(CMLib.xml().getValFromPieces(buf,"DISP"));
		if(E instanceof Physical)
			setPhyStats(((Physical)E).basePhyStats(),CMLib.xml().getValFromPieces(buf,"PROP"));
		setExtraEnvProperties(E,buf);
		if(E instanceof PhysicalAgent)
			setGenScripts((PhysicalAgent)E,buf,false);
	}

	protected String identifier(Environmental E, Environmental parent)
	{
		final StringBuilder str=new StringBuilder("");
		if((E instanceof MOB)&&(parent==null))
			parent=((MOB)E).location();
		if((E instanceof Item)&&(parent==null))
			parent=((Item)E).owner();
		if(E instanceof Area)
			return ((Area)E).Name()+" ("+((Area)E).ID()+")";
		if(E instanceof Room)
			str.append(((Room)E).roomID()+" ("+E.ID()+")");
		else
			str.append(E.Name()+" ("+E.ID()+")");
		if(parent!=null)
			return str.toString()+" of "+identifier(parent,null);
		return str.toString();
	}

	@Override
	public void setExtraEnvProperties(Environmental E, List<XMLTag> buf)
	{

		E.setImage(CMLib.xml().getValFromPieces(buf,"IMG"));
		if(E instanceof Economics)
		{
			((Economics)E).setPrejudiceFactors(CMLib.xml().getValFromPieces(buf,"PREJFC"));
			((Economics)E).setIgnoreMask(CMLib.xml().getValFromPieces(buf,"IGNMSK"));
			((Economics)E).setBudget(CMLib.xml().getValFromPieces(buf,"BUDGET"));
			((Economics)E).setDevalueRate(CMLib.xml().getValFromPieces(buf,"DEVALR"));
			((Economics)E).setInvResetRate(CMLib.xml().getIntFromPieces(buf,"INVRER"));
			final List<XMLLibrary.XMLTag> iV=CMLib.xml().getContentsFromPieces(buf,"IPRICS");
			if(iV!=null)
			{
				final String[] ipric=new String[iV.size()];
				for(int i=0;i<iV.size();i++)
				{
					final XMLTag iblk=iV.get(i);
					if((!iblk.tag().equalsIgnoreCase("IPRIC"))||(iblk.contents()==null))
					{
						Log.errOut("CoffeeMaker","Error parsing 'IPRICS' of "+identifier(E,null)+".  Load aborted");
						continue;
					}
					ipric[i]=CMLib.xml().restoreAngleBrackets(iblk.value());
				}
				((Economics)E).setItemPricingAdjustments(ipric);
			}
		}
		if(E instanceof PhysicalAgent)
		{
			final List<XMLLibrary.XMLTag> V=CMLib.xml().getContentsFromPieces(buf,"BEHAVES");
			if(V==null)
			{
				Log.errOut("CoffeeMaker","Error parsing 'BEHAVES' of "+identifier(E,null)+".  Load aborted");
				return;
			}
			for(int i=0;i<V.size();i++)
			{
				final XMLTag ablk=V.get(i);
				if((!ablk.tag().equalsIgnoreCase("BHAVE"))||(ablk.contents()==null))
				{
					Log.errOut("CoffeeMaker","Error parsing 'BHAVE' of "+identifier(E,null)+".  Load aborted");
					return;
				}
				final Behavior newOne=CMClass.getBehavior(ablk.getValFromPieces("BCLASS"));
				final String bparms=ablk.getValFromPieces("BPARMS");
				if(newOne==null)
				{
					Log.errOut("CoffeeMaker","Unknown behavior "+ablk.getValFromPieces("BCLASS")+" on "+identifier(E,null)+", skipping.");
					continue;
				}
				newOne.setParms(CMLib.xml().restoreAngleBrackets(bparms));
				((PhysicalAgent)E).addBehavior(newOne);
			}
		}
		if((E instanceof Area)&&(((Area)E).isSavable()))
			addAutoPropsToAreaIfNecessary((Area)E);

		if(E instanceof Physical)
		{
			final List<XMLLibrary.XMLTag> V=CMLib.xml().getContentsFromPieces(buf,"AFFECS");
			if(V==null)
			{
				Log.errOut("CoffeeMaker","Error parsing 'AFFECS' of "+identifier(E,null)+".  Load aborted");
				return;
			}
			for(int i=0;i<V.size();i++)
			{
				final XMLTag ablk=V.get(i);
				if((!ablk.tag().equalsIgnoreCase("AFF"))||(ablk.contents()==null))
				{
					Log.errOut("CoffeeMaker","Error parsing 'AFF' of "+identifier(E,null)+".  Load aborted");
					return;
				}
				final Ability newOne=CMClass.getAbility(ablk.getValFromPieces("ACLASS"));
				final String aparms=ablk.getValFromPieces("ATEXT");
				if(newOne==null)
				{
					Log.errOut("CoffeeMaker","Unknown affect "+ablk.getValFromPieces("ACLASS")+" on "+identifier(E,null)+", skipping.");
					continue;
				}
				newOne.setMiscText(CMLib.xml().restoreAngleBrackets(aparms));
				((Physical)E).addNonUninvokableEffect(newOne);
			}
		}
		final String[] codes=E.getStatCodes();
		for(int i=E.getSaveStatIndex();i<codes.length;i++)
		{
			String val=CMLib.xml().getValFromPieces(buf,codes[i].toUpperCase());
			if(val==null)
				val="";
			E.setStat(codes[i].toUpperCase(),val);
		}
	}

	@Override
	public Ammunition makeAmmunition(String ammunitionType, int number)
	{
		final Item neww=CMClass.getBasicItem("GenAmmunition");
		String ammo=ammunitionType;
		if(ammo.length()==0)
			return null;
		if(ammo.endsWith("s"))
			ammo=ammo.substring(0,ammo.length()-1);
		if(number>9)
		{
			neww.setName(L("a stack of @x1 @x2",""+number,CMLib.english().makePlural(ammo)));
			neww.setDisplayText(L("@x1 sit here.",neww.Name()));
		}
		else
		if(number>5)
		{
			neww.setName(L("a bunch of @x1",CMLib.english().makePlural(ammo)));
			neww.setDisplayText(L("@x1 sit here.",neww.Name()));
		}
		else
		if(number>1)
		{
			neww.setName(L("several @x1",CMLib.english().makePlural(ammo)));
			neww.setDisplayText(L("@x1 sit here.",neww.Name()));
		}
		else
		{
			neww.setName(CMLib.english().startWithAorAn(ammo));
			neww.setDisplayText(L("@x1 sits here.",neww.Name()));
		}
		((Ammunition)neww).setAmmunitionType(ammo);
		((Ammunition)neww).setAmmoRemaining(number);
		neww.setMaterial(RawMaterial.RESOURCE_OAK);
		neww.basePhyStats().setWeight(number);
		neww.setBaseValue(0);
		neww.recoverPhyStats();
		return (Ammunition)neww;
	}

	@Override
	public int getGenItemCodeNum(String code)
	{
		code=code.toUpperCase().trim();
		GenItemCode itemCode = (GenItemCode)CMath.s_valueOf(GenItemCode.class, code);
		if(itemCode != null)
			return itemCode.ordinal();
		for(GenItemCode c : GenItemCode.values())
		{
			if(code.startsWith(c.name()))
				return c.ordinal();
		}
		return -1;
	}

	@Override
	public String getGenItemStat(Item I, String code)
	{
		int codeNum = getGenItemCodeNum(code);
		if(codeNum < 0)
			return "";
		switch(GenItemCode.values()[codeNum])
		{
		case CLASS:
			return I.ID(); // class
		case USES:
			return "" + I.usesRemaining(); // uses
		case LEVEL:
			return "" + I.basePhyStats().level(); // level
		case ABILITY:
			return "" + I.basePhyStats().ability(); // ability
		case NAME:
			return I.Name(); // name
		case DISPLAY:
			return I.displayText(); // display
		case DESCRIPTION:
			return I.description(); // description
		case SECRET:
			return I.rawSecretIdentity(); // secret
		case PROPERWORN:
			return "" + I.rawProperLocationBitmap(); // properworn
		case WORNAND:
			return "" + I.rawLogicalAnd(); // wornand
		case BASEGOLD:
			return "" + I.baseGoldValue(); // basegold
		case ISGETTABLE:
			return "" + (!CMath.bset(I.basePhyStats().sensesMask(), PhyStats.SENSE_ITEMNOTGET)); // isgettable
		case ISREADABLE:
			return "" + (CMath.bset(I.basePhyStats().sensesMask(), PhyStats.SENSE_ITEMREADABLE)); // isreadable
		case ISDROPPABLE:
			return "" + (!CMath.bset(I.basePhyStats().sensesMask(), PhyStats.SENSE_ITEMNODROP)); // isdroppable
		case ISREMOVABLE:
			return "" + (!CMath.bset(I.basePhyStats().sensesMask(), PhyStats.SENSE_ITEMNOREMOVE)); // isremovable
		case MATERIAL:
			return "" + I.material(); // material
		case AFFBEHAV:
			return getExtraEnvPropertiesStr(I); // affbehav
		case DISPOSITION:
			return "" + I.basePhyStats().disposition(); // disposition
		case WEIGHT:
			return "" + I.basePhyStats().weight(); // weight
		case ARMOR:
			return "" + I.basePhyStats().armor(); // armor
		case DAMAGE:
			return "" + I.basePhyStats().damage(); // damage
		case ATTACK:
			return "" + I.basePhyStats().attackAdjustment(); // attack
		case READABLETEXT:
			return I.readableText(); // readabletext
		case IMG:
			return I.rawImage(); // img
			// case 23: return getGenScripts(I,false);
		}
		return "";
	}

	@Override
	public void setGenItemStat(Item I, String code, String val)
	{
		int codeNum = getGenItemCodeNum(code);
		if(codeNum < 0)
			return;
		switch(GenItemCode.values()[codeNum])
		{
		case CLASS:
			break; // class
		case USES:
			I.setUsesRemaining(CMath.s_parseIntExpression(val));
			break; // uses
		case LEVEL:
			I.basePhyStats().setLevel(CMath.s_parseIntExpression(val));
			break; // level
		case ABILITY:
			I.basePhyStats().setAbility(CMath.s_parseIntExpression(val));
			break; // ability
		case NAME:
			I.setName(val);
			break; // name
		case DISPLAY:
			I.setDisplayText(val);
			break; // display
		case DESCRIPTION:
			I.setDescription(val);
			break; // description
		case SECRET:
			I.setSecretIdentity(val);
			break; // secret
		case PROPERWORN:
		{
			if (CMath.isLong(val) || (val.trim().length() == 0)) // properworn
				I.setRawProperLocationBitmap(CMath.s_long(val));
			else
			{
				I.setRawProperLocationBitmap(0);
				final List<String> V = CMParms.parseCommas(val, true);
				final Wearable.CODES codes = Wearable.CODES.instance();
				for (final Iterator<String> e = V.iterator(); e.hasNext();)
				{
					val = e.next();
					final int wornIndex = codes.findDex_ignoreCase(val);
					if (wornIndex >= 0)
						I.setRawProperLocationBitmap(I.rawProperLocationBitmap() | codes.get(wornIndex));
				}
			}
			break;
		}
		case WORNAND:
			I.setRawLogicalAnd(CMath.s_bool(val));
			break; // wornand
		case BASEGOLD:
			I.setBaseValue(CMath.s_parseIntExpression(val));
			break; // basegold
		case ISGETTABLE:
			CMLib.flags().setGettable(I, CMath.s_bool(val));
			break; // isgettable
		case ISREADABLE:
			CMLib.flags().setReadable(I, CMath.s_bool(val));
			break; // isreadable
		case ISDROPPABLE:
			CMLib.flags().setDroppable(I, CMath.s_bool(val));
			break; // isdroppable
		case ISREMOVABLE:
			CMLib.flags().setRemovable(I, CMath.s_bool(val));
			break; // isremovable
		case MATERIAL:
			if (CMath.isInteger(val) || (val.trim().length() == 0)) // material
				I.setMaterial(CMath.s_int(val));
			else
			{
				final int rsc = RawMaterial.CODES.FIND_IgnoreCase(val);
				if (rsc >= 0)
					I.setMaterial(rsc);
			}
			break;
		case AFFBEHAV:
		{
			I.delAllEffects(true);
			I.delAllBehaviors();
			setExtraEnvProperties(I, CMLib.xml().parseAllXML(val)); // affbehav
			break;
		}
		case DISPOSITION:
		{
			if (CMath.isInteger(val) || (val.trim().length() == 0))
				I.basePhyStats().setDisposition(CMath.s_parseIntExpression(val)); // disposition
			else
			{
				I.basePhyStats().setDisposition(0);
				final List<String> V = CMParms.parseCommas(val, true);
				for (final Iterator<String> e = V.iterator(); e.hasNext();)
				{
					val = e.next();
					final int dispIndex = CMParms.indexOfIgnoreCase(PhyStats.IS_CODES, val);
					if (dispIndex >= 0)
						I.basePhyStats().setDisposition(I.basePhyStats().disposition() | (int) CMath.pow(2, dispIndex));
				}
			}
			break;
		}
		case WEIGHT:
			I.basePhyStats().setWeight(CMath.s_parseIntExpression(val));
			break; // weight
		case ARMOR:
			I.basePhyStats().setArmor(CMath.s_parseIntExpression(val));
			break; // armor
		case DAMAGE:
			I.basePhyStats().setDamage(CMath.s_parseIntExpression(val));
			break; // damage
		case ATTACK:
			I.basePhyStats().setAttackAdjustment(CMath.s_parseIntExpression(val));
			break; // attack
		case READABLETEXT:
			I.setReadableText(val);
			break; // readabletext
		case IMG:
			I.setImage(val);
			break; // img
		/*
		 * case 23: { while(I.numScripts()>0) { ScriptingEngine
		 * S=I.fetchScript(0); if(S!=null) I.delScript(S); }
		 * setGenScripts(I,CMLib.xml().parseAllXML(val),false); break; }
		 */
		}
	}

	@Override
	public int getGenMobCodeNum(String code)
	{
		code=code.toUpperCase().trim();
		GenMOBCode itemCode = (GenMOBCode)CMath.s_valueOf(GenMOBCode.class, code);
		if(itemCode != null)
			return itemCode.ordinal();
		for(GenMOBCode c : GenMOBCode.values())
		{
			if(code.startsWith(c.name()))
				return c.ordinal();
		}
		return -1;
	}

	@Override
	public String getGenMobStat(MOB M, String code)
	{
		int codeNum = getGenMobCodeNum(code);
		if(codeNum < 0)
			return "";
		switch(GenMOBCode.values()[codeNum])
		{
		case CLASS:
			return CMClass.classID(M); // class
		case RACE:
			return M.baseCharStats().getMyRace().ID(); // race
		case LEVEL:
			return "" + M.basePhyStats().level(); // level
		case ABILITY:
			return "" + M.basePhyStats().ability(); // ability
		case NAME:
			return M.Name(); // name
		case DISPLAY:
			return M.displayText(); // display
		case DESCRIPTION:
			return M.description(); // description
		case MONEY:
		{
			final String money = "" + CMLib.beanCounter().getMoney(M); // money
			// CMLib.beanCounter().clearZeroMoney(M,null); WHY THE HECK WAS THIS
			// EVER HERE?!?!
			return money;
		}
		case ALIGNMENT:
			return "" + M.fetchFaction(CMLib.factions().AlignID()); // alignment
		case DISPOSITION:
			return "" + M.basePhyStats().disposition(); // disposition
		case SENSES:
			return "" + M.basePhyStats().sensesMask(); // senses
		case ARMOR:
			return "" + M.basePhyStats().armor(); // armor
		case DAMAGE:
			return "" + M.basePhyStats().damage(); // damage
		case ATTACK:
			return "" + M.basePhyStats().attackAdjustment(); // attack
		case SPEED:
			return "" + M.basePhyStats().speed(); // speed
		case AFFBEHAV:
			return getExtraEnvPropertiesStr(M); // affbehav
		case ABLES:
			return getGenMobAbilities(M); // ables
		case INVENTORY:
		{
			final StringBuilder str = new StringBuilder(getGenMobInventory(M)); // inventory
			int x = str.indexOf("<IID>");
			while (x > 0)
			{
				final int y = str.indexOf("</IID>", x);
				if (y > x)
					str.delete(x, y + 6);
				else
					break;
				x = str.indexOf("<IID>");
			}
			x = str.indexOf("<ILOC>");
			while (x > 0)
			{
				final int y = str.indexOf("</ILOC>", x);
				if (y > x)
					str.delete(x, y + 7);
				else
					break;
				x = str.indexOf("<ILOC>");
			}
			return str.toString();
		}
		case TATTS:
		{
			final StringBuilder str = new StringBuilder(""); // tatts
			for (final Enumeration<Tattoo> e = M.tattoos(); e.hasMoreElements();)
				str.append(e.nextElement().toString() + ";");
			return str.toString();
		}
		case EXPS:
		{
			final StringBuilder str = new StringBuilder(""); // exps
			for (final Enumeration<String> x = M.expertises(); x.hasMoreElements();)
				str.append(x.nextElement()).append(';');
			return str.toString();
		}
		case IMG:
			return M.rawImage(); // img
		case FACTIONS:
			return M.getFactionListing(); // factions
		case VARMONEY:
			return "" + M.getMoneyVariation(); // varmoney
			// case 23: return getGenScripts(M,false);
		}
		return "";
	}

	@Override
	public void setGenMobStat(MOB M, String code, String val)
	{
		int codeNum = getGenMobCodeNum(code);
		if(codeNum < 0)
			return;
		switch(GenMOBCode.values()[codeNum])
		{
		case CLASS:
			break; // class
		case RACE:
			M.baseCharStats().setMyRace(CMClass.getRace(val));
			break; // race
		case LEVEL:
			M.basePhyStats().setLevel(CMath.s_parseIntExpression(val));
			break; // level
		case ABILITY:
			M.basePhyStats().setAbility(CMath.s_parseIntExpression(val));
			break; // ability
		case NAME:
			M.setName(val);
			break; // name
		case DISPLAY:
			M.setDisplayText(val);
			break; // display
		case DESCRIPTION:
			M.setDescription(val);
			break; // description
		case MONEY:
			CMLib.beanCounter().setMoney(M, CMath.s_parseIntExpression(val));
			break; // money
		case ALIGNMENT:
			if (CMath.s_int(val) == Integer.MAX_VALUE) // alignment
				M.removeFaction(CMLib.factions().AlignID());
			else
				M.addFaction(CMLib.factions().AlignID(), CMath.s_parseIntExpression(val));
			break;
		case DISPOSITION:
		{
			if (CMath.isInteger(val) || (val.trim().length() == 0)) // disposition
				M.basePhyStats().setDisposition(CMath.s_parseIntExpression(val));
			else
			{
				M.basePhyStats().setDisposition(0);
				final List<String> V = CMParms.parseCommas(val, true);
				for (final Iterator<String> e = V.iterator(); e.hasNext();)
				{
					val = e.next();
					final int dispIndex = CMParms.indexOfIgnoreCase(PhyStats.IS_CODES, val);
					if (dispIndex >= 0)
						M.basePhyStats().setDisposition(M.basePhyStats().disposition() | (int) CMath.pow(2, dispIndex));
				}
			}
			break;
		}
		case SENSES:
		{
			if (CMath.isInteger(val) || (val.trim().length() == 0)) // senses
				M.basePhyStats().setSensesMask(CMath.s_parseIntExpression(val));
			else
			{
				M.basePhyStats().setSensesMask(0);
				final List<String> V = CMParms.parseCommas(val, true);
				for (final Iterator<String> e = V.iterator(); e.hasNext();)
				{
					val = e.next();
					final int dispIndex = CMParms.indexOfIgnoreCase(PhyStats.CAN_SEE_CODES, val);
					if (dispIndex >= 0)
						M.basePhyStats().setSensesMask(M.basePhyStats().sensesMask() | (int) CMath.pow(2, dispIndex));
				}
			}
			break;
		}
		case ARMOR:
			M.basePhyStats().setArmor(CMath.s_parseIntExpression(val));
			break; // armor
		case DAMAGE:
			M.basePhyStats().setDamage(CMath.s_parseIntExpression(val));
			break; // damage
		case ATTACK:
			M.basePhyStats().setAttackAdjustment(CMath.s_parseIntExpression(val));
			break; // attack
		case SPEED:
			M.basePhyStats().setSpeed(CMath.s_parseMathExpression(val));
			break; // speed
		case AFFBEHAV:
		{
			M.delAllEffects(true);
			M.delAllBehaviors();
			setExtraEnvProperties(M, CMLib.xml().parseAllXML(val)); // affbehav
			break;
		}
		case ABLES:
		{
			final String extras = getExtraEnvPropertiesStr(M);
			M.delAllAbilities();
			setExtraEnvProperties(M, CMLib.xml().parseAllXML(extras)); // ables
			setGenMobAbilities(M, CMLib.xml().parseAllXML(val));
			break;
		}
		case INVENTORY:
		{
			M.delAllItems(true);
			setGenMobInventory(M, CMLib.xml().parseAllXML(val)); // inventory
			break;
		}
		case TATTS:
		{
			final List<String> V9 = CMParms.parseSemicolons(val, true);
			for (final Enumeration<Tattoo> e = M.tattoos(); e.hasMoreElements();) // tatts
				M.delTattoo(e.nextElement());
			for (final String tatt : V9)
				M.addTattoo(((Tattoo)CMClass.getCommon("DefaultTattoo")).parse(tatt));
			break;
		}
		case EXPS:
		{
			final List<String> V9 = CMParms.parseSemicolons(val, true); // exps
			M.delAllExpertises();
			for (int v = 0; v < V9.size(); v++)
				M.addExpertise(V9.get(v));
			break;
		}
		case IMG:
			M.setImage(val);
			break; // img
		case FACTIONS:
		{
			final List<String> V10 = CMParms.parseSemicolons(val, true); // factions
			for (int v = 0; v < V10.size(); v++)
			{
				final String s = V10.get(v);
				final int x = s.lastIndexOf('(');
				final int y = s.lastIndexOf(")");
				if ((x > 0) && (y > x))
					M.addFaction(s.substring(0, x), CMath.s_int(s.substring(x + 1, y)));
			}
			break;
		}
		case VARMONEY:
			M.setMoneyVariation(CMath.s_parseMathExpression(val));
			break; // varmoney
		/*
		 * case 23: { while(M.numScripts()>0) { ScriptingEngine
		 * S=M.fetchScript(0); if(S!=null) M.delScript(S); }
		 * setGenScripts(M,CMLib.xml().parseAllXML(val),false); break; }
		 */
		}
	}

	@Override
	public Area copyArea(Area A, String newName, boolean savable)
	{
		final Area newArea=(Area)A.copyOf();
		newArea.setName(newName);
		if(savable)
			CMLib.database().DBCreateArea(newArea);
		else
			CMLib.flags().setSavable(newArea, false);
		CMLib.map().addArea(newArea);
		final Map<String,String> altIDs=new Hashtable<String,String>();
		for(final Enumeration<Room> e=A.getCompleteMap();e.hasMoreElements();)
		{
			Room room=e.nextElement();
			synchronized(("SYNC"+room.roomID()).intern())
			{
				room=CMLib.map().getRoom(room);
				final Room newRoom=(Room)room.copyOf();
				newRoom.clearSky();
				if(newRoom instanceof GridLocale)
					((GridLocale)newRoom).clearGrid(null);
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					newRoom.rawDoors()[d]=null;
				newRoom.setRoomID(newArea.getNewRoomID(room,-1));
				newRoom.setArea(newArea);
				if(savable)
					CMLib.database().DBCreateRoom(newRoom);
				else
					CMLib.flags().setSavable(newRoom, false);
				altIDs.put(room.roomID(),newRoom.roomID());
				if(savable)
				{
					if(newRoom.numInhabitants()>0)
						CMLib.database().DBUpdateMOBs(newRoom);
					if(newRoom.numItems()>0)
						CMLib.database().DBUpdateItems(newRoom);
				}
			}
		}
		for(final Enumeration<Room> e=A.getCompleteMap();e.hasMoreElements();)
		{
			final Room room=e.nextElement();
			final String altID=altIDs.get(room.roomID());
			if(altID==null)
				continue;
			Room newRoom=CMLib.map().getRoom(altID);
			if(newRoom==null)
				continue;
			synchronized(("SYNC"+newRoom.roomID()).intern())
			{
				newRoom=CMLib.map().getRoom(newRoom);
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					final Room R=room.rawDoors()[d];
					String myRID=null;
					if(R!=null)
						myRID=altIDs.get(R.roomID());
					Room myR=null;
					if(myRID!=null)
						myR=CMLib.map().getRoom(myRID);
					newRoom.rawDoors()[d]=myR;
				}
				if(savable)
					CMLib.database().DBUpdateExits(newRoom);
				newRoom.getArea().fillInAreaRoom(newRoom);
			}
		}
		if(!savable)
			CMLib.map().delArea(newArea);
		return newArea;
	}

	@Override
	public String getFactionXML(MOB mob)
	{
		final StringBuilder facts=new StringBuilder();
		for(final Enumeration<String> e=mob.factions();e.hasMoreElements();)
		{
			final String name=e.nextElement();
			final int val=mob.fetchFaction(name);
			if(val!=Integer.MAX_VALUE)
				facts.append("<FCTN ID=\""+name+"\">"+val+"</FCTN>");
		}
		return CMLib.xml().convertXMLtoTag("FACTIONS",facts.toString());
	}

	@Override
	public void setFactionFromXML(MOB mob, List<XMLTag> xml)
	{
	   if(xml!=null)
	   {
		   final List<XMLLibrary.XMLTag> mV = CMLib.xml().getContentsFromPieces(xml,"FACTIONS");
		   if (mV!=null)
		   {
			   for (int m=0;m<mV.size();m++)
			   {
				   final XMLTag mblk=mV.get(m);
				   mob.addFaction(mblk.getParmValue("ID"),Integer.valueOf(mblk.value()).intValue());
			   }
		   }
	   }
	}
}
