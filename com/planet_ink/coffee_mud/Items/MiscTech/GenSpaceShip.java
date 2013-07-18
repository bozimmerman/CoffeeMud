package com.planet_ink.coffee_mud.Items.MiscTech;
import com.planet_ink.coffee_mud.Items.Basic.StdPortal;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Move;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
public class GenSpaceShip extends StdPortal implements Electronics, SpaceShip, PrivateProperty
{
	public String ID(){	return "GenSpaceShip";}
	protected String readableText="";
	protected String owner = "";
	protected int price = 1000;
	protected Manufacturer manufacturer = CMLib.tech().getDefaultManufacturer();
	protected Area area=null;

	public GenSpaceShip()
	{
		super();
		setName("a space ship");
		setDisplayText("a space ship is here.");
		setMaterial(RawMaterial.RESOURCE_STEEL);
		setDescription("");
		basePhyStats().setWeight(10000);
		recoverPhyStats();
		//CMLib.flags().setGettable(this, false);
		CMLib.flags().setSavable(this, false);
	}

	public boolean isGeneric(){return true;}

	public Area getShipArea()
	{
		if(destroyed)
			return null; 
		else
		if(area==null)
		{
			area=CMClass.getAreaType("StdSpaceShip");
			String num=Double.toString(Math.random());
			area.setName("UNNAMED_"+num.substring(num.indexOf('.')+1));
			area.setSavable(false);
			area.setTheme(Area.THEME_TECHNOLOGY);
			Room R=CMClass.getLocale("MetalRoom");
			R.setRoomID(area.Name()+"#0");
			R.setSavable(false);
			area.addProperRoom(R);
			readableText=R.roomID();
		}
		return area;
	}
	
	public void setShipArea(String xml)
	{
		try {
			area=CMLib.coffeeMaker().unpackAreaObjectFromXML(xml);
			if(area!=null)
			{
				area.setSavable(false);
				for(Enumeration<Room> r=area.getCompleteMap();r.hasMoreElements();)
					CMLib.flags().setSavable(r.nextElement(), false);
			}
			else
			{
				Log.warnOut("Failed to unpack an area for the space ship");
				getShipArea();
			}
		} catch (CMException e) {
			Log.warnOut("Unable to parse space ship xml for some reason.");
		}
	}
	
	public String keyName() { return readableText;}
	public void setKeyName(String newKeyName) { readableText=newKeyName;}
	
	public String readableText(){return readableText;}
	public void setReadableText(String text){readableText=text;}
	
	public CMObject copyOf()
	{
		GenSpaceShip s=(GenSpaceShip)super.copyOf();
		s.destroyed=false;
		String xml=CMLib.coffeeMaker().getAreaObjectXML(getShipArea(), null, null, null, true).toString();
		s.setShipArea(xml);
		if(s.getShipArea().Name().startsWith("UNNAMED_"))
		{
			String num=Double.toString(Math.random());
			s.renameSpaceShip("UNNAMED_"+num.substring(num.indexOf('.')+1));
		}
		return s;
	}
	
	public void stopTicking()
	{
		if(area!=null)
		{
			CMLib.threads().deleteAllTicks(area);
			String key=area.Name();
			String registryNum=area.getBlurbFlag("REGISTRY");
			if(registryNum!=null) 
				key+=registryNum;
			CMLib.tech().unregisterAllElectronics(key);
		}
		super.stopTicking();
		this.destroyed=false; // undo the weird thing
	}

	
	@Override
	protected Room getDestinationRoom()
	{
		getShipArea();
		Room R=null;
		List<String> V=CMParms.parseSemicolons(readableText(),true);
		if(V.size()>0)
			R=getShipArea().getRoom(V.get(CMLib.dice().roll(1,V.size(),-1)));
		return R;
	}
	
	public void destroy()
	{
		if(area!=null)
			CMLib.map().obliterateArea(area);
		super.destroy();
	}
	
	public int fuelType(){return -1;}
	public void setFuelType(int resource){}
	public long powerCapacity(){return 0;}
	public void setPowerCapacity(long capacity){}
	public long powerRemaining(){return 0;}
	public void setPowerNeeds(int amt){}
	public int powerNeeds(){return 0;}
	public void setPowerRemaining(long remaining){}
	public void activate(boolean truefalse){}
	public boolean activated(){return true;}
	public int techLevel() { return -1;}
	public void setTechLevel(int lvl) { }
	public void setManufacturer(Manufacturer manufacturer) { if(manufacturer!=null) this.manufacturer=manufacturer; }
	public Manufacturer getManufacturer() { return manufacturer; }
	
	@Override
	public long[] coordinates() 
	{
		return (area instanceof SpaceObject)?((SpaceObject)area).coordinates():new long[3];
	}

	@Override
	public void setCoords(long[] coords) 
	{
		if (area instanceof SpaceObject)
			((SpaceObject)area).setCoords(coords);
	}

	@Override
	public double[] direction() 
	{
		return (area instanceof SpaceObject)?((SpaceObject)area).direction():new double[2];
	}

	@Override
	public void setDirection(double[] dir) 
	{
		if (area instanceof SpaceObject)
			((SpaceObject)area).setDirection(dir);
	}

	@Override
	public long velocity() 
	{
		return (area instanceof SpaceObject)?((SpaceObject)area).velocity():0;
	}

	@Override
	public void setVelocity(long v) 
	{
		if (area instanceof SpaceObject)
			((SpaceObject)area).setVelocity(v);
	}

	@Override
	public long accelleration() 
	{
		return (area instanceof SpaceObject)?((SpaceObject)area).accelleration():0;
	}

	@Override
	public void setAccelleration(long v) 
	{
		if (area instanceof SpaceObject)
			((SpaceObject)area).setAccelleration(v);
	}

	@Override
	public SpaceObject knownTarget() 
	{
		return (area instanceof SpaceObject)?((SpaceObject)area).knownTarget():null;
	}

	@Override
	public void setKnownTarget(SpaceObject O) 
	{
		if (area instanceof SpaceObject)
			((SpaceObject)area).setKnownTarget(O);
	}

	@Override
	public SpaceObject knownSource() 
	{
		return (area instanceof SpaceObject)?((SpaceObject)area).knownSource():null;
	}

	@Override
	public void setKnownSource(SpaceObject O) 
	{
		if (area instanceof SpaceObject)
			((SpaceObject)area).setKnownSource(O);
	}

	@Override
	public SpaceObject orbiting() 
	{
		return (area instanceof SpaceObject)?((SpaceObject)area).orbiting():null;
	}

	@Override
	public void setOrbiting(SpaceObject O) 
	{
		if (area instanceof SpaceObject)
			((SpaceObject)area).setOrbiting(O);
	}

	@Override
	public void dockHere(Room R) 
	{
		if(!R.isContent(me))
			R.moveItemTo(me, Expire.Never, Move.Followers);
		CMLib.map().delObjectInSpace(this);
		if (area instanceof SpaceShip)
			((SpaceShip)area).dockHere(R);
	}

	@Override
	public void unDock(boolean toSpace) 
	{
		Room R=CMLib.map().roomLocation(this);
		R.delItem(this);
		if(toSpace)
			CMLib.map().addObjectToSpace(this);
		if (area instanceof SpaceShip)
			((SpaceShip)area).unDock(toSpace);
	}
	
	@Override public int getPrice() { return price; }
	@Override public void setPrice(int price) { this.price=price; }
	@Override public String getOwnerName() { return owner; }
	@Override public void setOwnerName(String owner) { this.owner=owner;}
	@Override
	public CMObject getOwnerObject()
	{
		String owner=getOwnerName();
		if(owner.length()==0) return null;
		Clan C=CMLib.clans().getClan(owner);
		if(C!=null) return C;
		return CMLib.players().getLoadPlayer(owner);
	}
	@Override
	public String getTitleID() { return this.toString(); }

	public void renameSpaceShip(String newName)
	{
		Area area=this.area;
		if(area instanceof SpaceShip)
		{
			final Room oldEntry=getDestinationRoom();
			String oldName=area.Name();
			String registryNum=area.getBlurbFlag("REGISTRY");
			if(registryNum==null) registryNum="";
			((SpaceShip)area).renameSpaceShip(newName);
			CMLib.tech().unregisterElectronics(null, oldName+registryNum);
			registryNum=Double.toString(Math.random());
			area.addBlurbFlag("REGISTRY Registry#"+registryNum.substring(registryNum.indexOf('.')+1));
			setReadableText(oldEntry.roomID());
			setShipArea(CMLib.coffeeMaker().getAreaObjectXML(area, null, null, null, true).toString());
		}
		if(Name().indexOf("[NAME]")>=0)
			setName(CMStrings.replaceAll(displayText(), "[NAME]", newName));
		else
			setName(newName);
		if(displayText().indexOf("[NAME]")>=0)
			setDisplayText(CMStrings.replaceAll(displayText(), "[NAME]", newName));
		else
			setDisplayText(newName+" is here.");
	}
	
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if((msg.targetMinor()==CMMsg.TYP_SELL)
		&&(msg.tool()==this)
		&&(msg.target()!=null)
		&&(msg.target() instanceof ShopKeeper))
		{
			setOwnerName("");
			recoverPhyStats();
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(msg.tool()==this)
		&&(msg.source()!=null)
		&&(getOwnerName().length()>0)
		&&((msg.source().Name().equals(getOwnerName()))
			||(msg.source().getLiegeID().equals(getOwnerName())&&msg.source().isMarriedToLiege())
			||(CMLib.clans().checkClanPrivilege(msg.source(), getOwnerName(), Clan.Function.PROPERTY_OWNER)))
		&&(msg.target()!=null)
		&&(msg.target() instanceof MOB)
		&&(!(msg.target() instanceof Banker))
		&&(!(msg.target() instanceof Auctioneer))
		&&(!(msg.target() instanceof PostOffice)))
		{
			if(CMLib.clans().checkClanPrivilege(msg.source(), getOwnerName(), Clan.Function.PROPERTY_OWNER))
			{
				Pair<Clan,Integer> targetClan=CMLib.clans().findPrivilegedClan((MOB)msg.target(), Clan.Function.PROPERTY_OWNER);
				if(targetClan!=null)
					setOwnerName(targetClan.first.clanID());
				else
					setOwnerName(msg.target().Name());
			}
			else
				setOwnerName(msg.target().Name());
			recoverPhyStats();
			String registryNum=Double.toString(Math.random());
			String randNum=CMStrings.limit(registryNum.substring(registryNum.indexOf('.')+1), 4);
			renameSpaceShip("SS "+msg.source().Name()+", Reg "+randNum);
			final Session session=msg.source().session();
			final Room R=CMLib.map().roomLocation(this);
			if(session!=null)
			{
				final GenSpaceShip me=this;
				final InputCallback[] namer=new InputCallback[1];
				namer[0]=new InputCallback(InputCallback.Type.PROMPT) {
					@Override public void showPrompt() { session.println("\n\rEnter a new name for your ship: "); }
					@Override public void timedOut() { }
					@Override public void callBack() {
						if((this.input.trim().length()==0)
						||(!CMLib.login().isOkName(this.input.trim()))
						||(CMLib.tech().getMakeRegisteredKeys().contains(this.input.trim())))
						{
							session.println("^XThat is not a permitted name.^N");
							session.prompt(namer[0]);
							return;
						}
						me.renameSpaceShip(this.input.trim());
						msg.source().tell(name()+" is now signed over to "+getOwnerName()+".");
						List<Room> docks=new XVector<Room>();
						if(R!=null)
							for(Enumeration<Room> r=R.getArea().getMetroMap();r.hasMoreElements();)
							{
								Room R2=r.nextElement();
								if(R2.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
									docks.add(R2);
							}
						if(docks.size()==0)
							docks.add(R);
						Room finalR=docks.get(CMLib.dice().roll(1, docks.size(), -1));
						me.dockHere(finalR);
						msg.source().tell("You'll find your ship docked at '"+finalR.roomTitle(msg.source())+"'.");
						if ((msg.source().playerStats() != null) && (!msg.source().playerStats().getExtItems().isContent(me)))
							msg.source().playerStats().getExtItems().addItem(me);
					}
				};
				session.prompt(namer[0]);
			}
			else
			{
				msg.source().tell(name()+" is now signed over to "+getOwnerName()+".");
				if ((msg.source().playerStats() != null) && (!msg.source().playerStats().getExtItems().isContent(this)))
					msg.source().playerStats().getExtItems().addItem(this);
				dockHere(R);
			}
		}
	}
	
	private final static String[] MYCODES={"HASLOCK","HASLID","CAPACITY",
							  "CONTAINTYPES","RIDEBASIS","MOBSHELD",
							  "FUELTYPE","POWERCAP","ACTIVATED","POWERREM",
							  "MANUFACTURER"};
	public String getStat(String code)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			return CMLib.coffeeMaker().getGenItemStat(this,code);
		switch(getCodeNum(code))
		{
		case 0: return ""+hasALock();
		case 1: return ""+hasALid();
		case 2: return ""+capacity();
		case 3: return ""+containTypes();
		case 4: return ""+rideBasis();
		case 5: return ""+riderCapacity();
		case 6: return ""+fuelType();
		case 7: return ""+powerCapacity();
		case 8: return ""+activated();
		case 9: return ""+powerRemaining();
		case 10: return getManufacturer().name();
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}
	public void setStat(String code, String val)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			CMLib.coffeeMaker().setGenItemStat(this,code,val);
		else
		switch(getCodeNum(code))
		{
		case 0: setLidsNLocks(hasALid(),isOpen(),CMath.s_bool(val),false); break;
		case 1: setLidsNLocks(CMath.s_bool(val),isOpen(),hasALock(),false); break;
		case 2: setCapacity(CMath.s_parseIntExpression(val)); break;
		case 3: setContainTypes(CMath.s_parseBitLongExpression(Container.CONTAIN_DESCS,val)); break;
		case 4: break;
		case 5: break;
		case 6:{
			int x=CMath.s_parseListIntExpression(RawMaterial.CODES.NAMES(), val);
			x=((x>=0)&&(x<RawMaterial.RESOURCE_MASK))?RawMaterial.CODES.GET(x):x;
			setFuelType(x); 
			break;
		   } 
		case 7: setPowerCapacity(CMath.s_parseIntExpression(val)); break;
		case 8: activate(CMath.s_bool(val)); break;
		case 9: setPowerRemaining(CMath.s_parseLongExpression(val)); break;
		case 10: setManufacturer(CMLib.tech().getManufacturer(val)); break;
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}
	protected int getCodeNum(String code){
		for(int i=0;i<MYCODES.length;i++)
			if(code.equalsIgnoreCase(MYCODES[i])) return i;
		return -1;
	}
	private static String[] codes=null;
	public String[] getStatCodes()
	{
		if(codes!=null) return codes;
		String[] MYCODES=CMProps.getStatCodesList(GenSpaceShip.MYCODES,this);
		String[] superCodes=GenericBuilder.GENITEMCODES;
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenSpaceShip)) return false;
		String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		return true;
	}
}
