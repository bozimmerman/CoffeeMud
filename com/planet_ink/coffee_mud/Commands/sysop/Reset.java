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
			if((R.getArea().name().equals(AreaID))
			&&(R.ID().startsWith(AreaID+"#")))
			{
				int newnum=Util.s_int(R.ID().substring(AreaID.length()+1));
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
				mob.tell(R.ID()+"/"+R.name()+"/"+R.displayText()+"--------------------");
				for(int i=0;i<R.numItems();i++)
				{
					Item I=R.fetchItem(i);
					if(I.ID().equalsIgnoreCase("GenWallpaper")) continue;
					String str=mob.session().prompt(" "+I.name()+"/"+EnvResource.RESOURCE_DESCS[I.material()&EnvResource.RESOURCE_MASK],"");
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
					String str=mob.session().prompt(" "+M.name()+"/"+M.charStats().getMyRace().ID(),"");
					if(str.length()>0)
					{
						Race R2=CMClass.getRace(str);
						if(R2==null)
							str=mob.session().prompt(" "+M.name()+"/"+M.charStats().getMyRace().ID(),"");
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
						str=mob.session().prompt("   "+I.name()+"/"+EnvResource.RESOURCE_DESCS[I.material()&EnvResource.RESOURCE_MASK],"");
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
					if(M instanceof ShopKeeper)
					{
						Vector V=((ShopKeeper)M).getUniqueStoreInventory();
						for(int i=V.size()-1;i>=0;i--)
						{
							Environmental E=(Environmental)V.elementAt(i);
							if(E instanceof Item)
							{
								Item I=(Item)E;
								str=mob.session().prompt(" - "+I.name()+"/"+EnvResource.RESOURCE_DESCS[I.material()&EnvResource.RESOURCE_MASK],"");
								if(str.equalsIgnoreCase("delete"))
								{
									((ShopKeeper)M).delStoreInventory(I);
									somethingDone=true;
									mob.tell(" - deleted");
								}
								else
								if(str.length()>0)
								for(int ii=0;ii<EnvResource.RESOURCE_DESCS.length;ii++)
								{
									if(str.equalsIgnoreCase(EnvResource.RESOURCE_DESCS[ii]))
									{
										int numInStock=((ShopKeeper)M).numberInStock(I);
										I.setMaterial(EnvResource.RESOURCE_DATA[ii][0]);
										somethingDone=true;
										mob.tell(" - Changed to "+EnvResource.RESOURCE_DESCS[I.material()&EnvResource.RESOURCE_MASK]);
										((ShopKeeper)M).delStoreInventory(I);
										((ShopKeeper)M).addStoreInventory(I,numInStock);
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
