package com.planet_ink.coffee_mud.Commands.base.sysop;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
public class Reset
{
	public String getOpenRoomID(String AreaID)
	{
		int highest=0;
		for(int m=0;m<CMMap.numRooms();m++)
		{
			Room thisRoom=CMMap.getRoom(m);
			if((thisRoom.getArea().name().equals(AreaID))
			&&(thisRoom.ID().startsWith(AreaID+"#")))
			{
				int newnum=Util.s_int(thisRoom.ID().substring(AreaID.length()+1));
				if(newnum>=highest)
					highest=newnum+1;
			}
		}
		return AreaID+"#"+highest;
	}

	public void resetSomething(MOB mob, Vector commands)
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
			room(mob.location());
			mob.tell("Done.");
		}
		else
		if(s.equalsIgnoreCase("area"))
		{
			area(mob.location().getArea());
			mob.tell("Done.");
		}
		else
		if(s.equalsIgnoreCase("areaoramamana"))
		{
			// this is just utility code and will change frequently
			Area A=mob.location().getArea();
			area(A);
			A.toggleMobility(false);
			Vector rooms=A.getMyMap();
			try{
			for(int r=0;r<rooms.size();r++)
			{
				Room room=(Room)rooms.elementAt(r);
				room(room);
				boolean somethingDone=false;
				mob.tell(room.ID()+"/"+room.name()+"/"+room.displayText()+"--------------------");
				for(int i=0;i<room.numItems();i++)
				{
					Item I=room.fetchItem(i);
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
					ExternalPlay.DBUpdateItems(room);
				somethingDone=false;
				for(int m=0;m<room.numInhabitants();m++)
				{
					MOB M=room.fetchInhabitant(m);
					if(M==mob) continue;
					String str=mob.session().prompt(" "+M.name()+"/"+M.charStats().getMyRace().ID(),"");
					if(str.length()>0)
					{
						Race R=CMClass.getRace(str);
						if(R==null)
							str=mob.session().prompt(" "+M.name()+"/"+M.charStats().getMyRace().ID(),"");
						else
						if(R!=null)
						{
							mob.tell(" Changed to "+R.ID());
							M.baseCharStats().setMyRace(R);
							R.setHeightWeight(M.baseEnvStats(),(char)M.baseCharStats().getStat(CharStats.GENDER));
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
						if((B.ID().equalsIgnoreCase("Mobile"))&&(B.getParms().trim().length()>0))
						{
							somethingDone=true;
							B.setParms("");
						}
					}
				}
				if(somethingDone)
					ExternalPlay.DBUpdateMOBs(room);
			}
			}
			catch(java.io.IOException e){}
			A.toggleMobility(true);
			mob.tell("Done.");
		}
	}
	public void room(Room room)
	{
		if(room==null) return;
		new Rooms().clearTheRoom(room);
		ExternalPlay.DBReadContent(room);
	}
	public void area(Area area)
	{
		Vector allRooms=area.getMyMap();
		for(int r=0;r<allRooms.size();r++)
		{
			Room room=(Room)allRooms.elementAt(r);
			room(room);
		}
	}
}
