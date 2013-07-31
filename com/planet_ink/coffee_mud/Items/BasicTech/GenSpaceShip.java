package com.planet_ink.coffee_mud.Items.BasicTech;
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
import com.planet_ink.coffee_mud.Items.interfaces.ShipComponent.ShipEngine.ThrustPort;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechCommand;
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
	protected String 		readableText	= "";
	protected String 		owner 			= "";
	protected int 			price 			= 1000;
	protected Area 			area			= null;
	protected Manufacturer	cachedManufact  = null;
	protected String	 	manufacturer	= "RANDOM";
	public long[]   		coordinates 	= new long[3];
	public double[] 		direction   	= new double[2];
	public long 			speed			= 0;
	protected SpaceObject	spaceTarget 	= null;
	protected double[]		facing			= new double[2];

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
			((SpaceShip)area).setKnownSource(this);
			readableText=R.roomID();
		}
		return area;
	}
	
	public void setShipArea(String xml)
	{
		try {
			area=CMLib.coffeeMaker().unpackAreaObjectFromXML(xml);
			if(area instanceof SpaceShip)
			{
				area.setSavable(false);
				((SpaceShip)area).setKnownSource(this);
				for(Enumeration<Room> r=area.getCompleteMap();r.hasMoreElements();)
					CMLib.flags().setSavable(r.nextElement(), false);
			}
			else
			{
				Log.warnOut("Failed to unpack a space ship area for the space ship");
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
	
	public String text()
	{
		return CMLib.coffeeMaker().getPropertiesStr(this,false);
	}

	public void setMiscText(String newText)
	{
		miscText="";
		CMLib.coffeeMaker().setPropertiesStr(this,newText,false);
		recoverPhyStats();
	}

	public CMObject copyOf()
	{
		GenSpaceShip s=(GenSpaceShip)super.copyOf();
		s.destroyed=false;
		String xml=CMLib.coffeeMaker().getAreaObjectXML(getShipArea(), null, null, null, true).toString();
		s.setShipArea(xml);
		/*
		if(s.getShipArea().Name().startsWith("UNNAMED_"))
		{
			String num=Double.toString(Math.random());
			String oldName=s.Name();
			String oldDisplay=s.displayText();
			s.renameSpaceShip("UNNAMED_"+num.substring(num.indexOf('.')+1));
			s.setName(oldName);
			s.setDisplayText(oldDisplay);
		}
		*/
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
	
	@Override public long powerCapacity(){return 0;}
	@Override public void setPowerCapacity(long capacity){}
	@Override public long powerRemaining(){return 0;}
	@Override public int powerNeeds(){return 0;}
	@Override public void setPowerRemaining(long remaining){}
	@Override public void activate(boolean truefalse){}
	@Override public boolean activated(){return true;}
	@Override public int techLevel() { return phyStats().ability();}
	@Override public void setTechLevel(int lvl) { basePhyStats.setAbility(lvl); recoverPhyStats(); }
	@Override public String getManufacturerName() { return manufacturer; }
	@Override public void setManufacturerName(String name) { cachedManufact=null; if(name!=null) manufacturer=name; }
	
	@Override public int getMass(){ return (area instanceof SpaceShip)?((SpaceShip)area).getMass(): 1000; }

	@Override
	public Manufacturer getFinalManufacturer()
	{
		if(cachedManufact==null)
		{
			cachedManufact=CMLib.tech().getManufacturerOf(this,getManufacturerName().toUpperCase().trim());
			if(cachedManufact==null)
				cachedManufact=CMLib.tech().getDefaultManufacturer();
		}
		return cachedManufact;
	}
	
	@Override public long[] coordinates(){return coordinates;}
	@Override public double[] direction(){return direction;}
	@Override public double[] facing() { return facing; }
	@Override public void setFacing(double[] dir) { if(dir!=null) this.facing=dir; }
	@Override public SpaceObject knownTarget(){return spaceTarget;}
	@Override public void setKnownTarget(SpaceObject O){spaceTarget=O;}
	@Override public void setCoords(long[] coords)
	{
		if((coords!=null)&&(coords.length==3)) 
			CMLib.map().moveSpaceObject(this,coords);
	}
	@Override public void setDirection(double[] dir){if(dir!=null) direction=dir;}
	@Override public long speed(){return speed;}
	@Override public void setSpeed(long v){speed=v;}
	

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
	public long radius() {
		return (area instanceof SpaceObject)?((SpaceObject)area).radius():50;
	}

	@Override
	public void setRadius(long radius) {
		if (area instanceof SpaceObject)
			((SpaceObject)area).setRadius(radius);
	}

	@Override
	public double getOMLCoeff() {
		return (area instanceof SpaceShip)?((SpaceShip)area).getOMLCoeff()
				:SpaceObject.ATMOSPHERIC_DRAG_STREAMLINE + ((SpaceObject.ATMOSPHERIC_DRAG_BRICK-SpaceObject.ATMOSPHERIC_DRAG_STREAMLINE)/2.0);
	}

	@Override
	public void setOMLCoeff(double coeff) {
		if (area instanceof SpaceShip)
			((SpaceShip)area).setOMLCoeff(coeff);
	}
	
	@Override
	public void dockHere(LocationRoom R) 
	{
		if(!R.isContent(this))
		{
			if(owner()==null)
				R.addItem(this,Expire.Never);
			else
				R.moveItemTo(me, Expire.Never, Move.Followers);
		}
		setCoords(R.coordinates());
		CMLib.map().delObjectInSpace(getShipSpaceObject());
		if (area instanceof SpaceShip)
		{
			((SpaceShip)area).dockHere(R);
			for(Enumeration<Room> r=area.getProperMap();r.hasMoreElements();)
			{
				Room R2=r.nextElement();
				for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
					if((R2.getRawExit(d)!=null)
					&&((R2.rawDoors()[d]==null)||(R2.rawDoors()[d].getArea()!=area)))
						R2.rawDoors()[d]=R;
			}
		}
		setSpeed(0);
	}

	@Override
	public void unDock(boolean toSpace) 
	{
		LocationRoom R=getIsDocked();
		if(R!=null)
		{
			R.delItem(this);
			setOwner(null);
		}
		if (area instanceof SpaceShip)
			((SpaceShip)area).unDock(toSpace);
		if(R!=null)
		{
			setDirection(R.getDirectionFromCore());
			setFacing(R.getDirectionFromCore());
		}
		if(toSpace)
		{
			SpaceObject o = getShipSpaceObject();
			if((o != null)&&(R!=null))
				CMLib.map().addObjectToSpace(o,R.coordinates());
		}
	}
	
	@Override
	public SpaceObject getShipSpaceObject()
	{
		return this;
	}

	@Override 
	public LocationRoom getIsDocked()
	{
		if (area instanceof SpaceShip)
			return ((SpaceShip)area).getIsDocked();
		if(owner() instanceof LocationRoom)
			return ((LocationRoom)owner());
		return null;
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
	@Override public String getTitleID() { return this.toString(); }

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
		for(String word : new String[]{"NAME","NEWNAME","SHIPNAME","SHIP"})
		{
			for(String rubs : new String[]{"<>","[]","{}","()"})
			{
				if(Name().indexOf(rubs.charAt(0)+word+rubs.charAt(1))>=0)
					setName(CMStrings.replaceAll(Name(), rubs.charAt(0)+word+rubs.charAt(1), newName));
			}
			for(String rubs : new String[]{"<>","[]","{}","()"})
			{
				if(displayText().indexOf(rubs.charAt(0)+word+rubs.charAt(1))>=0)
					setDisplayText(CMStrings.replaceAll(displayText(), rubs.charAt(0)+word+rubs.charAt(1), newName));
			}
		}
	}
	
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(msg.amITarget(this))
		{
			if((msg.targetMinor()==CMMsg.TYP_OPEN)||(msg.targetMinor()==CMMsg.TYP_CLOSE)||(msg.targetMinor()==CMMsg.TYP_LOCK)||(msg.targetMinor()==CMMsg.TYP_UNLOCK))
			{
				msg.setOthersMessage(CMStrings.replaceAll(msg.othersMessage(), "<T-NAME>", "a hatch on <T-NAME>"));
				msg.setOthersMessage(CMStrings.replaceAll(msg.othersMessage(), "<T-NAMESELF>", "a hatch on <T-NAMESELF>"));
				msg.setSourceMessage(CMStrings.replaceAll(msg.othersMessage(), "<T-NAME>", "a hatch on <T-NAME>"));
				msg.setSourceMessage(CMStrings.replaceAll(msg.othersMessage(), "<T-NAMESELF>", "a hatch on <T-NAMESELF>"));
				msg.setTargetMessage(CMStrings.replaceAll(msg.othersMessage(), "<T-NAME>", "a hatch on <T-NAME>"));
				msg.setTargetMessage(CMStrings.replaceAll(msg.othersMessage(), "<T-NAMESELF>", "a hatch on <T-NAMESELF>"));
			}
		}
		return true;
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if(msg.amITarget(this))
		{
			if((msg.targetMinor()==CMMsg.TYP_GET)&&(msg.tool() instanceof ShopKeeper))
				transferOwnership(msg.source());
			else
			if((msg.targetMinor()==CMMsg.TYP_ACTIVATE)&&(CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
			{
				String[] parts=msg.targetMessage().split(" ");
				TechCommand command=TechCommand.findCommand(parts);
				if(command!=null)
				{
					Object[] parms=command.confirmAndTranslate(parts);
					if(parms!=null)
					{
						if(command==Technical.TechCommand.ACCELLLERATION)
						{
							ThrustPort dir=(ThrustPort)parms[0];
							int amount=((Integer)parms[1]).intValue();
							//long specificImpulse=((Long)parms[2]).longValue();
							switch(dir)
							{
							case STARBOARD: facing[0]-=amount; break;
							case PORT: facing[0]+=amount; break;
							case DORSEL: facing[1]-=amount; break;
							case VENTRAL: facing[1]+=amount; break;
							case FORWARD: break;
							case AFT: 
							{
								//TODO: apply inertial dampeners and OML
								//TODO: gravity is separate
								//TODO: inertial dampeners are separate?
								//force equation in air= A=((thrust / (m * inertial dampener <= 1 ))-1)*(1- OML))
								//force equation in space= A=(thrust / (m * inertial dampener <= 1 )
								
								// remember you are moving in a direction, but facing a different one.
								/*
								int mass=getMass();
								double ke1=0.5*(velocity*velocity); // *mass
								double ke2=0.5*(amount*amount); // *mass
								direction()[0]+=Math.sin(ke1/ke2);
								*/
								//TODO: how does the dir affect velocity and direction given thrust and facing?
								break;
							}
							}
							facing[0]=facing[0]%(2*Math.PI);
							facing[1]=facing[1]%(2*Math.PI);
						}
					}
				}
			}
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_SELL)
		&&(msg.tool()==this)
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
		&&(msg.target() instanceof MOB)
		&&(!(msg.target() instanceof Banker))
		&&(!(msg.target() instanceof Auctioneer))
		&&(!(msg.target() instanceof PostOffice)))
			transferOwnership((MOB)msg.target());
	}

	protected LocationRoom findNearestDocks(Room R)
	{
		List<LocationRoom> docks=new XVector<LocationRoom>();
		if(R!=null)
		{
			TrackingLibrary.TrackingFlags flags;
			flags = new TrackingLibrary.TrackingFlags()
					.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
					.plus(TrackingLibrary.TrackingFlag.NOAIR)
					.plus(TrackingLibrary.TrackingFlag.NOHOMES)
					.plus(TrackingLibrary.TrackingFlag.UNLOCKEDONLY)
					.plus(TrackingLibrary.TrackingFlag.NOWATER);
			List<Room> rooms=CMLib.tracking().getRadiantRooms(R, flags, 25);
			for(Room R2 : rooms)
				if((R2.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
				&&(R2 instanceof LocationRoom)
				&&(R.getArea().inMyMetroArea(R2.getArea())))
					docks.add((LocationRoom)R2);
			if(docks.size()==0)
				for(Enumeration<Room> r=R.getArea().getMetroMap();r.hasMoreElements();)
				{
					Room R2=r.nextElement();
					if((R2.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
					&&(R2 instanceof LocationRoom))
						docks.add((LocationRoom)R2);
				}
			if(docks.size()==0)
				for(Room R2 : rooms)
					if((R2.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
					&&(R2 instanceof LocationRoom))
						docks.add((LocationRoom)R2);
		}
		if(docks.size()==0)
			return null;
		return docks.get(CMLib.dice().roll(1, docks.size(), -1));
	}
	
	protected void transferOwnership(final MOB buyer)
	{
		if(CMLib.clans().checkClanPrivilege(buyer, getOwnerName(), Clan.Function.PROPERTY_OWNER))
		{
			Pair<Clan,Integer> targetClan=CMLib.clans().findPrivilegedClan(buyer, Clan.Function.PROPERTY_OWNER);
			if(targetClan!=null)
				setOwnerName(targetClan.first.clanID());
			else
				setOwnerName(buyer.Name());
		}
		else
			setOwnerName(buyer.Name());
		recoverPhyStats();
		//String registryNum=Double.toString(Math.random());
		//String randNum=CMStrings.limit(registryNum.substring(registryNum.indexOf('.')+1), 4);
		//renameSpaceShip("SS "+buyer.Name()+", Reg "+randNum);
		final Session session=buyer.session();
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
					||(!CMLib.login().isOkName(this.input.trim(),true))
					||(CMLib.tech().getMakeRegisteredKeys().contains(this.input.trim())))
					{
						session.println("^ZThat is not a permitted name.^N");
						session.prompt(namer[0].reset());
						return;
					}
					me.renameSpaceShip(this.input.trim());
					buyer.tell(name()+" is now signed over to "+getOwnerName()+".");
					LocationRoom finalR=findNearestDocks(R);
					if(finalR==null)
					{
						Log.errOut("Could not dock ship in area "+R.getArea().Name()+" due to lack of spaceport.");
						buyer.tell("Nowhere was found to dock your ship.  Please contact the administrators!.");
					}
					else
					{
						me.dockHere(finalR);
						buyer.tell("You'll find your ship docked at '"+finalR.displayText(buyer)+"'.");
					}
					if ((buyer.playerStats() != null) && (!buyer.playerStats().getExtItems().isContent(me)))
						buyer.playerStats().getExtItems().addItem(me);
				}
			};
			session.prompt(namer[0]);
		}
		else
		{
			buyer.tell(name()+" is now signed over to "+getOwnerName()+".");
			if ((buyer.playerStats() != null) && (!buyer.playerStats().getExtItems().isContent(this)))
				buyer.playerStats().getExtItems().addItem(this);
			LocationRoom finalR=findNearestDocks(R);
			if(finalR==null)
				Log.errOut("Could not dock ship in area "+R.getArea().Name()+" due to lack of spaceport.");
			else
				dockHere(finalR);
		}
	}

	@Override
	public BoundedCube getBounds() 
	{
		return new BoundedObject.BoundedCube(coordinates(),radius());
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
		case 6: return ""+powerCapacity();
		case 7: return ""+activated();
		case 8: return ""+powerRemaining();
		case 9: return getManufacturerName();
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
		case 6: setPowerCapacity(CMath.s_parseIntExpression(val)); break;
		case 7: activate(CMath.s_bool(val)); break;
		case 8: setPowerRemaining(CMath.s_parseLongExpression(val)); break;
		case 9: setManufacturerName(val); break;
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
