package com.planet_ink.coffee_mud.Commands.sysop;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
public class Reset
{
	private Reset(){}

	public static String getOpenRoomID(String AreaID)
	{
		int highest=0;
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if((R.getArea().Name().equals(AreaID))
			&&(R.roomID().startsWith(AreaID+"#")))
			{
				int newnum=Util.s_int(R.roomID().substring(AreaID.length()+1));
				if(newnum>=highest)
					highest=newnum+1;
			}
		}
		return AreaID+"#"+highest;
	}

	public static void resetSomething(MOB mob, Vector commands)
	{
		commands.removeElementAt(0);
		if(commands.size()<1)
		{
			mob.tell("Reset this Room, or the whole Area?");
			return;
		}
		String s=(String)commands.elementAt(0);
		if(s.equalsIgnoreCase("room"))
		{
			resetRoom(mob.location());
			mob.tell("Done.");
		}
		else
		if(s.equalsIgnoreCase("area"))
		{
			resetArea(mob.location().getArea());
			mob.tell("Done.");
		}
		else
		if(s.equalsIgnoreCase("mobstats"))
		{
			s="room";
			if(commands.size()>1) s=(String)commands.elementAt(1);
			Vector rooms=new Vector();
			if(s.toUpperCase().startsWith("ROOM"))
				rooms.addElement(mob.location());
			else
			if(s.toUpperCase().startsWith("AREA"))
				for(Enumeration e=mob.location().getArea().getMap();e.hasMoreElements();)
					rooms.addElement(((Room)e.nextElement()));
			else
			if(s.toUpperCase().startsWith("WORLD"))
				for(Enumeration e=CMMap.rooms();e.hasMoreElements();)
					rooms.addElement(((Room)e.nextElement()));
			else
			{
				mob.tell("Try ROOM, AREA, or WORLD.");
				return ;
			}
			
			for(Enumeration r=rooms.elements();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				R.getArea().toggleMobility(false);
				resetRoom(R);
				boolean somethingDone=false;
				for(int m=0;m<R.numInhabitants();m++)
				{
					MOB M=R.fetchInhabitant(m);
					if((M.isEligibleMonster())
					&&(M.getStartRoom()==R))
					{
						MOB M2=
						M.baseCharStats().getCurrentClass().buildMOB(null,
																	M.baseEnvStats().level(),
																	M.getAlignment(),
																	M.baseEnvStats().weight(),
																	M.getWimpHitPoint(),
																	(char)M.baseCharStats().getStat(CharStats.GENDER));
						M.baseEnvStats().setAttackAdjustment(M2.baseEnvStats().attackAdjustment());
						M.baseEnvStats().setArmor(M2.baseEnvStats().armor());
						M.baseEnvStats().setDamage(M2.baseEnvStats().damage());
						M.recoverEnvStats();
						somethingDone=true;
					}
				}
				if(somethingDone)
				{
					mob.tell("Room "+R.roomID()+" done.");
					ExternalPlay.DBUpdateMOBs(R);
				}
				R.getArea().toggleMobility(true);
			}
			
		}
		else
		if(s.toLowerCase().startsWith("golems"))
		{
			if(s.toLowerCase().endsWith("change"))
			{
				for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					R.getArea().toggleMobility(false);
					resetRoom(R);
					boolean somethingDone=false;
					mob.tell(R.roomID()+"/"+R.name()+"/"+R.displayText()+"--------------------");
					somethingDone=false;
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB M=R.fetchInhabitant(m);
						if(M==mob) continue;
						int d=M.baseEnvStats().disposition();
						if((d&EnvStats.IS_GOLEM)>0)
						{
							somethingDone=true;
							M.baseEnvStats().setDisposition(d-EnvStats.IS_GOLEM);
						}
					}
					if(somethingDone)
						ExternalPlay.DBUpdateMOBs(R);
					R.getArea().toggleMobility(true);
				}
			}
			StringBuffer is=new StringBuffer("");
			StringBuffer isnot=new StringBuffer("");
			Hashtable names=new Hashtable();
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				for(int i=0;i<R.numInhabitants();i++)
				{
					MOB M=R.fetchInhabitant(i);
					if(names.contains(M.Name())) continue;
					names.put(M.Name(),M.Name());
					if(Sense.isGolem(M))
						is.append(M.Name()+" ");
					else
						isnot.append(M.Name()+" ");
				}
			}
			mob.tell("IS-"+is.toString());
			Log.sysOut("GOLEMS","IS-"+is.toString());
			mob.tell("ISNOT-"+isnot.toString());
			Log.sysOut("GOLEMS","ISNOT-"+isnot.toString());
		}
		else
		if(s.toLowerCase().startsWith("norejuvers"))
		{
			StringBuffer is=new StringBuffer("");
			Hashtable names=new Hashtable();
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				for(int i=0;i<R.numInhabitants();i++)
				{
					MOB M=R.fetchInhabitant(i);
					if((M.baseEnvStats().rejuv()>0)&&(M.baseEnvStats().rejuv()<Integer.MAX_VALUE))
						continue;
					is.append(M.Name()+" ");
				}
			}
			mob.tell("IS-"+is.toString());
			Log.sysOut("REJUV","IS-"+is.toString());
		}
		else
		if(s.equalsIgnoreCase("areaoramamana"))
		{
			// this is just utility code and will change frequently
			Area A=mob.location().getArea();
			resetArea(A);
			A.toggleMobility(false);
			try{
			for(Enumeration r=A.getMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				resetRoom(R);
				boolean somethingDone=false;
				mob.tell(R.roomID()+"/"+R.name()+"/"+R.displayText()+"--------------------");
				for(int i=0;i<R.numItems();i++)
				{
					Item I=R.fetchItem(i);
					if(I.ID().equalsIgnoreCase("GenWallpaper")) continue;
					String str=mob.session().prompt(" "+I.Name()+"/"+EnvResource.RESOURCE_DESCS[I.material()&EnvResource.RESOURCE_MASK],"");
					if(str.length()>0)
					for(int ii=0;ii<EnvResource.RESOURCE_DESCS.length;ii++)
					{
						if(str.equalsIgnoreCase(EnvResource.RESOURCE_DESCS[ii]))
						{
							I.setMaterial(EnvResource.RESOURCE_DATA[ii][0]);
							somethingDone=true;
							mob.tell(" Changed to "+EnvResource.RESOURCE_DESCS[I.material()&EnvResource.RESOURCE_MASK]);
							break;
						}
					}
				}
				if(somethingDone)
					ExternalPlay.DBUpdateItems(R);
				somethingDone=false;
				for(int m=0;m<R.numInhabitants();m++)
				{
					MOB M=R.fetchInhabitant(m);
					if(M==mob) continue;
					String str=mob.session().prompt(" "+M.Name()+"/"+M.charStats().getMyRace().ID(),"");
					if(str.length()>0)
					{
						Race R2=CMClass.getRace(str);
						if(R2==null)
							str=mob.session().prompt(" "+M.Name()+"/"+M.charStats().getMyRace().ID(),"");
						else
						if(R2!=null)
						{
							mob.tell(" Changed to "+R2.ID());
							M.baseCharStats().setMyRace(R2);
							R2.setHeightWeight(M.baseEnvStats(),(char)M.baseCharStats().getStat(CharStats.GENDER));
							M.recoverCharStats();
							M.recoverEnvStats();
							somethingDone=true;
						}
					}
					for(int i=M.inventorySize()-1;i>=0;i--)
					{
						Item I=M.fetchInventory(i);
						str=mob.session().prompt("   "+I.Name()+"/"+EnvResource.RESOURCE_DESCS[I.material()&EnvResource.RESOURCE_MASK],"");
						if(str.equalsIgnoreCase("delete"))
						{
							M.delInventory(I);
							somethingDone=true;
							mob.tell("   deleted");
						}
						else
						if(str.length()>0)
						for(int ii=0;ii<EnvResource.RESOURCE_DESCS.length;ii++)
						{
							if(str.equalsIgnoreCase(EnvResource.RESOURCE_DESCS[ii]))
							{
								I.setMaterial(EnvResource.RESOURCE_DATA[ii][0]);
								somethingDone=true;
								mob.tell("   Changed to "+EnvResource.RESOURCE_DESCS[I.material()&EnvResource.RESOURCE_MASK]);
								break;
							}
						}
					}
					ShopKeeper SK=CoffeeUtensils.getShopKeeper(M);
					if(SK!=null)
					{
						Vector V=SK.getUniqueStoreInventory();
						for(int i=V.size()-1;i>=0;i--)
						{
							Environmental E=(Environmental)V.elementAt(i);
							if(E instanceof Item)
							{
								Item I=(Item)E;
								str=mob.session().prompt(" - "+I.Name()+"/"+EnvResource.RESOURCE_DESCS[I.material()&EnvResource.RESOURCE_MASK],"");
								if(str.equalsIgnoreCase("delete"))
								{
									SK.delStoreInventory(I);
									somethingDone=true;
									mob.tell(" - deleted");
								}
								else
								if(str.length()>0)
								for(int ii=0;ii<EnvResource.RESOURCE_DESCS.length;ii++)
								{
									if(str.equalsIgnoreCase(EnvResource.RESOURCE_DESCS[ii]))
									{
										int numInStock=SK.numberInStock(I);
										I.setMaterial(EnvResource.RESOURCE_DATA[ii][0]);
										somethingDone=true;
										mob.tell(" - Changed to "+EnvResource.RESOURCE_DESCS[I.material()&EnvResource.RESOURCE_MASK]);
										SK.delStoreInventory(I);
										SK.addStoreInventory(I,numInStock);
										break;
									}
								}
							}
						}
					}
					if(M.fetchAbility("Chopping")!=null)
					{
						somethingDone=true;
						M.delAbility(M.fetchAbility("Chopping"));
					}
					for(int i=0;i<M.numBehaviors();i++)
					{
						Behavior B=M.fetchBehavior(i);
						if((B.ID().equalsIgnoreCase("Mobile"))
						&&(B.getParms().trim().length()>0))
						{
							somethingDone=true;
							B.setParms("");
						}
					}
				}
				if(somethingDone)
					ExternalPlay.DBUpdateMOBs(R);
			}
			}
			catch(java.io.IOException e){}
			A.toggleMobility(true);
			mob.tell("Done.");
		}
	}
	public static void resetRoom(Room room)
	{
		if(room==null) return;
		boolean mobile=room.getMobility();
		room.toggleMobility(false);
		Rooms.clearTheRoom(room);
		ExternalPlay.DBReadContent(room,null);
		room.toggleMobility(mobile);
	}
	public static void resetArea(Area area)
	{
		boolean mobile=area.getMobility();
		area.toggleMobility(false);
		for(Enumeration r=area.getMap();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			resetRoom(R);
		}
		area.fillInAreaRooms();
		area.toggleMobility(mobile);
	}
}
