package com.planet_ink.coffee_mud.utils;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;
import java.io.*;

public class CoffeeUtensils
{
	public static boolean hasASky(Room room)
	{
		if((room==null)
		||(room.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		||((room.domainType()&Room.INDOORS)>0))
			return false;
		return true;
	}

	public static ShopKeeper getShopKeeper(MOB mob)
	{
		if(mob==null) return null;
		if(mob instanceof ShopKeeper) return (ShopKeeper)mob;
		for(int a=0;a<mob.numEffects();a++)
		{
			Ability A=mob.fetchEffect(a);
			if((A!=null)&&(A instanceof ShopKeeper))
				return (ShopKeeper)A;
		}
		return null;
	}

	public static void outfit(MOB mob, Vector items)
	{
		if((mob==null)||(items==null)||(items.size()==0))
			return;
		for(int i=0;i<items.size();i++)
		{
			Item I=(Item)items.elementAt(i);
			if(mob.fetchInventory(I.name()+"$")==null)
			{
				I=(Item)I.copyOf();
				I.text();
				I.recoverEnvStats();
				mob.addInventory(I);
				if(I.whereCantWear(mob)<=0)
					I.wearIfPossible(mob);
			}
		}
	}

	public static Vector shopkeepers(Room here, MOB notMOB)
	{
		Vector V=new Vector();
		if(here!=null)
		for(int i=0;i<here.numInhabitants();i++)
		{
			MOB thisMOB=here.fetchInhabitant(i);
			if((thisMOB!=null)
			&&(thisMOB!=notMOB)
			&&(getShopKeeper(thisMOB)!=null)
			&&((notMOB==null)||(Sense.canBeSeenBy(thisMOB,notMOB))))
				V.addElement(thisMOB);
		}
		return V;
	}

	public static Trap getATrap(Environmental unlockThis)
	{
		Trap theTrap=null;
		int roll=(int)Math.round(Math.random()*100.0);
		if(unlockThis instanceof Exit)
		{
			if(((Exit)unlockThis).hasADoor())
			{
				if(((Exit)unlockThis).hasALock())
				{
					if(roll<20)
						theTrap=(Trap)CMClass.getAbility("Trap_Open");
					else
					if(roll<80)
						theTrap=(Trap)CMClass.getAbility("Trap_Unlock");
					else
						theTrap=(Trap)CMClass.getAbility("Trap_Enter");
				}
				else
				{
					if(roll<50)
						theTrap=(Trap)CMClass.getAbility("Trap_Open");
					else
						theTrap=(Trap)CMClass.getAbility("Trap_Enter");
				}
			}
			else
				theTrap=(Trap)CMClass.getAbility("Trap_Enter");
		}
		else
		if(unlockThis instanceof Container)
		{
			if(((Container)unlockThis).hasALid())
			{
				if(((Container)unlockThis).hasALock())
				{
					if(roll<20)
						theTrap=(Trap)CMClass.getAbility("Trap_Open");
					else
					if(roll<80)
						theTrap=(Trap)CMClass.getAbility("Trap_Unlock");
					else
						theTrap=(Trap)CMClass.getAbility("Trap_Get");
				}
				else
				{
					if(roll<50)
						theTrap=(Trap)CMClass.getAbility("Trap_Open");
					else
						theTrap=(Trap)CMClass.getAbility("Trap_Get");
				}
			}
			else
				theTrap=(Trap)CMClass.getAbility("Trap_Get");
		}
		else
		if(unlockThis instanceof Item)
			theTrap=(Trap)CMClass.getAbility("Trap_Get");
		return theTrap;
	}

	public static Trap fetchMyTrap(Environmental myThang)
	{
		if(myThang==null) return null;
		for(int a=0;a<myThang.numEffects();a++)
		{
			Ability A=myThang.fetchEffect(a);
			if((A!=null)&&(A instanceof  Trap))
				return (Trap)A;
		}
		return null;
	}

	public static void setTrapped(Environmental myThang, boolean isTrapped)
	{
		Trap t=getATrap(myThang);
		t.setReset(50);
		setTrapped(myThang,t,isTrapped);
	}
	public static void setTrapped(Environmental myThang, Trap theTrap, boolean isTrapped)
	{
		for(int a=0;a<myThang.numEffects();a++)
		{
			Ability A=myThang.fetchEffect(a);
			if((A!=null)&&(A instanceof Trap))
				A.unInvoke();
		}

		if((isTrapped)&&(myThang.fetchEffect(theTrap.ID())==null))
			myThang.addEffect(theTrap);
	}

	public static boolean reachableItem(MOB mob, Environmental I)
	{
		if((I==null)||(!(I instanceof Item)))
			return true;
		if((mob.isMine(I))
		||((mob.riding()!=null)&&((I==mob.riding())
								  ||(((Item)I).owner()==mob.riding())
								  ||(((Item)I).ultimateContainer()==mob.riding()))))
		   return true;
		return false;
	}

	public static Room roomLocation(Environmental E)
	{
		if(E==null) return null;
		if(E instanceof MOB)
			return ((MOB)E).location();
		else
		if((E instanceof Item)&&(((Item)E).owner() instanceof Room))
			return (Room)((Item)E).owner();
		else
		if((E instanceof Item)&&(((Item)E).owner() instanceof MOB))
		   return ((MOB)((Item)E).owner()).location();
		return null;
	}

    public static double memoryUse ( Environmental E, int number )
    {
		double s=-1.0;
		try
		{
            int n = number;
            Object[] objs = new Object[n] ;
            Environmental cl = E;
            Runtime rt = Runtime.getRuntime() ;
			long m0 =rt.totalMemory() - rt.freeMemory() ;
			System.gc() ;
			Thread.sleep( 500 ) ;
            for (int i = 0 ; i < n ; ++i) objs[i] =
                    E=cl.copyOf();
			System.gc() ;
			Thread.sleep( 1000 ) ;
			long m1 =rt.totalMemory() - rt.freeMemory() ;
            long dm = m1 - m0 ;
            s = (double)dm / (double)n ;
			if(s<0.0) return memoryUse(E,number);
		}
		catch(Exception e){return -1;}
		return s;
    }

	public static void extinguish(MOB source, Environmental target, boolean mundane)
	{
		if(target instanceof Room)
		{
			Room R=(Room)target;
			for(int m=0;m<R.numInhabitants();m++)
			{
				MOB M=R.fetchInhabitant(m);
				if(M!=null) extinguish(source,M,mundane);
			}
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if(I!=null) extinguish(source,I,mundane);
			}
			return;
		}
		for(int a=target.numEffects()-1;a>=0;a--)
		{
			Ability A=target.fetchEffect(a);
			if((A!=null)&&((!mundane)||(A.classificationCode()==Ability.PROPERTY)))
			{
				if((Util.bset(A.flags(),Ability.FLAG_HEATING)&&(!mundane))
				||(Util.bset(A.flags(),Ability.FLAG_BURNING))
				||((A.ID().equalsIgnoreCase("Spell_SummonElemental")&&A.text().toUpperCase().indexOf("FIRE")>=0)))
					A.unInvoke();
			}
		}
		if((target instanceof MOB)&&(!mundane))
		{
			MOB tmob=(MOB)target;
			if(tmob.charStats().getMyRace().ID().equals("FireElemental"))
				MUDFight.postDeath(source,(MOB)target,null);
			for(int i=0;i<tmob.inventorySize();i++)
			{
				Item I=tmob.fetchInventory(i);
				if(I!=null) extinguish(tmob,I,mundane);
			}
		}
		if((target instanceof Light)&&(((Light)target).isLit()))
		{
			((Light)target).tick(target,MudHost.TICK_LIGHT_FLICKERS);
			((Light)target).light(false);
		}
	}

	public static void obliterateRoom(Room deadRoom)
	{
		for(int a=deadRoom.numEffects()-1;a>=0;a--)
		{
			Ability A=deadRoom.fetchEffect(a);
			if(A!=null)
			{
				A.unInvoke();
				deadRoom.delEffect(A);
			}
		}
		CMMap.delRoom(deadRoom);
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			boolean changes=false;
			for(int dir=0;dir<Directions.NUM_DIRECTIONS;dir++)
			{
				Room thatRoom=R.rawDoors()[dir];
				if(thatRoom==deadRoom)
				{
					R.rawDoors()[dir]=null;
					changes=true;
					if((R.rawExits()[dir]!=null)&&(R.rawExits()[dir].isGeneric()))
					{
						Exit GE=(Exit)R.rawExits()[dir];
						GE.setTemporaryDoorLink(deadRoom.roomID());
					}
				}
			}
			if(changes)
				CMClass.DBEngine().DBUpdateExits(R);
		}
		for(int mb=deadRoom.numInhabitants()-1;mb>=0;mb--)
		{
			MOB mob2=deadRoom.fetchInhabitant(mb);
			if(mob2!=null)
			{
				if((mob2.getStartRoom()!=deadRoom)&&(mob2.getStartRoom()!=null)&&(CMMap.getRoom(mob2.getStartRoom().roomID())!=null))
					mob2.getStartRoom().bringMobHere(mob2,true);
				else
				{
					CMClass.ThreadEngine().deleteTick(mob2,-1);
					mob2.destroy();
				}
			}
		}
		for(int i=deadRoom.numItems()-1;i>=0;i--)
		{
			Item item2=deadRoom.fetchItem(i);
			if(item2!=null)
			{
				CMClass.ThreadEngine().deleteTick(item2,-1);
				item2.destroy();
			}
		}
		clearTheRoom(deadRoom);
		deadRoom.destroyRoom();
		if(deadRoom instanceof GridLocale)
			((GridLocale)deadRoom).clearGrid();
		CMClass.DBEngine().DBDeleteRoom(deadRoom);
	}

	public static void roomAffectFully(CMMsg msg, Room room, int dirCode)
	{
		room.send(msg.source(),msg);
		if((msg.target()==null)||(!(msg.target() instanceof Exit)))
			return;
		if(dirCode<0)
		{
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				if(room.getExitInDir(d)==msg.target()){ dirCode=d; break;}
		}
		if(dirCode<0) return;
		Exit pair=room.getPairedExit(dirCode);
		if(pair!=null)
		{
			FullMsg altMsg=null;
			if((msg.targetCode()==CMMsg.MSG_OPEN)&&(pair.isLocked()))
			{
				altMsg=new FullMsg(msg.source(),pair,msg.tool(),CMMsg.MSG_UNLOCK,null,CMMsg.MSG_UNLOCK,null,CMMsg.MSG_UNLOCK,null);
				pair.executeMsg(msg.source(),altMsg);
			}
			altMsg=new FullMsg(msg.source(),pair,msg.tool(),msg.sourceCode(),null,msg.targetCode(),null,msg.othersCode(),null);
			pair.executeMsg(msg.source(),altMsg);
		}
	}

	public static void obliteratePlayer(MOB deadMOB, boolean quiet)
	{
		if(CMMap.getPlayer(deadMOB.Name())!=null)
		{
		   deadMOB=(MOB)CMMap.getPlayer(deadMOB.Name());
		   CMMap.delPlayer(deadMOB);
		}
		for(int s=0;s<Sessions.size();s++)
		{
			Session S=(Session)Sessions.elementAt(s);
			if((!S.killFlag())&&(S.mob()!=null)&&(S.mob().Name().equals(deadMOB.Name())))
			   deadMOB=S.mob();
		}
		FullMsg msg=new FullMsg(deadMOB,null,CMMsg.MSG_RETIRE,(quiet)?null:"A horrible death cry is heard throughout the land.");
		if(deadMOB.location()!=null)
			deadMOB.location().send(deadMOB,msg);
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if((R!=null)&&(R!=deadMOB.location()))
			{
				if(R.okMessage(deadMOB,msg))
					R.sendOthers(deadMOB,msg);
				else
				{
					CMMap.addPlayer(deadMOB);
					return;
				}
			}
		}
		deadMOB.destroy();
		CMClass.DBEngine().DBDeleteMOB(deadMOB);
		if(deadMOB.session()!=null)
		{
			deadMOB.session().setKillFlag(true);
			deadMOB.session().setMob(null);
		}
		Log.sysOut("Scoring",deadMOB.name()+" has been deleted.");
	}

	protected static CMMsg resetMsg=null;
	public static void resetRoom(Room room)
	{
		if(room==null) return;
		boolean mobile=room.getMobility();
		room.toggleMobility(false);
		if(resetMsg==null) resetMsg=new FullMsg(CMClass.sampleMOB(),room,CMMsg.MSG_ROOMRESET,null);
		room.executeMsg(room,resetMsg);
		CoffeeUtensils.clearTheRoom(room);
		CMClass.DBEngine().DBReadContent(room,null);
		room.toggleMobility(mobile);
	}

	public static void clearTheRoom(Room room)
	{
		for(int m=room.numInhabitants()-1;m>=0;m--)
		{
			MOB mob2=room.fetchInhabitant(m);
			if((mob2!=null)&&(mob2.isEligibleMonster()))
			{
				if(mob2.getStartRoom()==room)
					mob2.destroy();
				else
				if(mob2.getStartRoom()!=null)
					mob2.getStartRoom().bringMobHere(mob2,true);
			}
		}
		while(room.numItems()>0)
		{
			Item I=room.fetchItem(0);
			I.destroy();
		}
		CMClass.ThreadEngine().clearDebri(room,0);
	}


	public static void clearDebriAndRestart(Room room, int taskCode)
	{
		CMClass.ThreadEngine().clearDebri(room,0);
		if(taskCode<2)
		{
			CMClass.DBEngine().DBUpdateItems(room);
			room.startItemRejuv();
		}
		if((taskCode==0)||(taskCode==2))
			CMClass.DBEngine().DBUpdateMOBs(room);
	}

	public static void obliterateArea(String areaName)
	{
		Room foundOne=CMMap.getFirstRoom();
		while(foundOne!=null)
		{
			foundOne=null;
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.getArea().Name().equalsIgnoreCase(areaName))
				{
					foundOne=R;
					break;
				}
			}
			if(foundOne!=null)
				obliterateRoom(foundOne);
		}

		Area A=CMMap.getArea(areaName);
		CMClass.DBEngine().DBDeleteArea(A);
		CMMap.delArea(A);
	}

	public static LandTitle getLandTitle(Area area)
	{
		if(area==null) return null;
		for(int a=0;a<area.numEffects();a++)
		{
			Ability A=area.fetchEffect(a);
			if((A!=null)&&(A instanceof LandTitle))
				return (LandTitle)A;
		}
		return null;
	}
	public static LandTitle getLandTitle(Room room)
	{
		if(room==null) return null;
		LandTitle title=getLandTitle(room.getArea());
		if(title!=null) return title;

		for(int a=0;a<room.numEffects();a++)
		{
			Ability A=room.fetchEffect(a);
			if((A!=null)&&(A instanceof LandTitle))
				return (LandTitle)A;
		}
		return null;
	}

	public static boolean doesOwnThisProperty(MOB mob, Room room)
	{
		LandTitle title=getLandTitle(room);
		if(title==null) return false;
		if(title.landOwner()==null) return false;
		if(title.landOwner().length()==0) return false;
		if(title.landOwner().equals(mob.Name())) return true;
		if((title.landOwner().equals(mob.getLiegeID())&&(mob.isMarriedToLiege())))
		   return true;
		if(title.landOwner().equals(mob.getClanID()))
		{
			Clan C=Clans.getClan(mob.getClanID());
			if((C!=null)&&(C.allowedToDoThis(mob,Clan.FUNC_CLANPROPERTYOWNER)>=0))
				return true;
		}
		return false;
	}

	public static boolean armorCheck(MOB mob, int allowedArmorLevel)
	{
		if(allowedArmorLevel==CharClass.ARMOR_ANY) return true;

		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if(I==null) break;
			if((!I.amWearingAt(Item.INVENTORY))
			&&((I instanceof Armor)||(I instanceof Shield)))
			{
				boolean ok=true;
				switch(I.material()&EnvResource.MATERIAL_MASK)
				{
				case EnvResource.MATERIAL_LEATHER:
					if((allowedArmorLevel==CharClass.ARMOR_CLOTH)
					||(allowedArmorLevel==CharClass.ARMOR_VEGAN)
					||(allowedArmorLevel==CharClass.ARMOR_METALONLY))
						ok=false;
					break;
				case EnvResource.MATERIAL_METAL:
				case EnvResource.MATERIAL_MITHRIL:
					if((allowedArmorLevel==CharClass.ARMOR_CLOTH)
					||(allowedArmorLevel==CharClass.ARMOR_LEATHER)
					||(allowedArmorLevel==CharClass.ARMOR_NONMETAL))
						ok=false;
					break;
				case EnvResource.MATERIAL_ENERGY:
					if((allowedArmorLevel==CharClass.ARMOR_METALONLY)
					||(allowedArmorLevel==CharClass.ARMOR_VEGAN))
					   return false;
					break;
				case EnvResource.MATERIAL_CLOTH:
					if((allowedArmorLevel==CharClass.ARMOR_METALONLY)
					||((allowedArmorLevel==CharClass.ARMOR_VEGAN)
					   &&((I.material()==EnvResource.RESOURCE_HIDE)
						  ||(I.material()==EnvResource.RESOURCE_FUR)
						  ||(I.material()==EnvResource.RESOURCE_FEATHERS)
						  ||(I.material()==EnvResource.RESOURCE_WOOL))))
						ok=false;
					break;
				case EnvResource.MATERIAL_PLASTIC:
				case EnvResource.MATERIAL_WOODEN:
				case EnvResource.MATERIAL_ROCK:
					if((allowedArmorLevel==CharClass.ARMOR_CLOTH)
					||(allowedArmorLevel==CharClass.ARMOR_VEGAN)
					||(allowedArmorLevel==CharClass.ARMOR_LEATHER)
					||(allowedArmorLevel==CharClass.ARMOR_METALONLY))
						ok=false;
					break;
				default:
					if(allowedArmorLevel==CharClass.ARMOR_METALONLY)
						ok=false;
					break;
				}
				if((!ok)&&((I.rawProperLocationBitmap()&CharClass.ARMOR_WEARMASK)>0))
					return false;
			}
		}
		return true;
	}
}

