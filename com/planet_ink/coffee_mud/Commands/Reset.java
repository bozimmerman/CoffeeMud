package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Reset extends StdCommand
{
	public Reset(){}

	private String[] access={"RESET"};
	public String[] getAccessWords(){return access;}

	public int resetAreaOramaManaI(MOB mob, Item I, Hashtable rememberI, String lead)
		throws java.io.IOException
	{
		int nochange=0;
		if(I instanceof Weapon)
		{
			Weapon W=(Weapon)I;
			if((W.requiresAmmunition())&&(W.ammunitionCapacity()>0))
			{
				String str=mob.session().prompt(lead+I.Name()+" requires ("+W.ammunitionType()+"): ");
				if(str.length()>0)
				{
					if((str.trim().length()==0)||(str.equalsIgnoreCase("no")))
					{
						W.setAmmunitionType("");
						W.setAmmoCapacity(0);
						W.setUsesRemaining(100);
						str=mob.session().prompt(lead+I.Name()+" new weapon type: ");
						W.setWeaponType(Util.s_int(str));
					}
					else
						W.setAmmunitionType(str.trim());
					nochange=1;
				}
			}
		}
		Integer IT=(Integer)rememberI.get(I.Name());
		if(IT!=null)
		{
			if(IT.intValue()==I.material())
			{
				mob.tell(lead+I.Name()+" still "+EnvResource.RESOURCE_DESCS[I.material()&EnvResource.RESOURCE_MASK]);
				return nochange;
			}
			I.setMaterial(IT.intValue());
			mob.tell(lead+I.Name()+" Changed to "+EnvResource.RESOURCE_DESCS[I.material()&EnvResource.RESOURCE_MASK]);
			return 1;
		}
		while(true)
		{
			String str=mob.session().prompt(lead+I.Name()+"/"+EnvResource.RESOURCE_DESCS[I.material()&EnvResource.RESOURCE_MASK],"");
			if(str.equalsIgnoreCase("delete"))
				return -1;
			else
			if(str.length()==0)
			{
				rememberI.put(I.Name(),new Integer(I.material()));
				return nochange;
			}
			if(str.equals("?"))
				mob.tell(I.Name()+"/"+I.displayText()+"/"+I.description());
			else
			{
				String poss="";
				for(int ii=0;ii<EnvResource.RESOURCE_DESCS.length;ii++)
				{
					if(EnvResource.RESOURCE_DESCS[ii].startsWith(str.toUpperCase()))
					   poss=EnvResource.RESOURCE_DESCS[ii];
					if(str.equalsIgnoreCase(EnvResource.RESOURCE_DESCS[ii]))
					{
						I.setMaterial(EnvResource.RESOURCE_DATA[ii][0]);
						mob.tell(lead+"Changed to "+EnvResource.RESOURCE_DESCS[I.material()&EnvResource.RESOURCE_MASK]);
						rememberI.put(I.Name(),new Integer(I.material()));
						return 1;
					}
				}
				if(poss.length()==0)
				{
					for(int ii=0;ii<EnvResource.RESOURCE_DESCS.length;ii++)
					{
						if(EnvResource.RESOURCE_DESCS[ii].indexOf(str.toUpperCase())>=0)
						   poss=EnvResource.RESOURCE_DESCS[ii];
					}
				}
				mob.tell(lead+"'"+str+"' does not exist.  Try '"+poss+"'.");
			}
		}
	}

	private int rightImportMat(MOB mob, Item I, boolean openOnly)
		throws java.io.IOException
	{
		if((I!=null)&&(I.description().trim().length()>0))
		{
			int x=I.description().trim().indexOf(" ");
			int y=I.description().trim().lastIndexOf(" ");
			if((x<0)||((x>0)&&(y==x)))
			{
				String s=I.description().trim().toLowerCase();
				if((mob!=null)&&(mob.session()!=null)&&(openOnly))
				{
					if(mob.session().confirm("Clear "+I.name()+"/"+I.displayText()+"/"+I.description()+" (Y/n)?","Y"))
					{
						I.setDescription("");
						return I.material();
					}
					return -1;
				}
				int rightMat=-1;
				for(int i=0;i<Import.objDescs.length;i++)
				{
					if(Import.objDescs[i][0].equals(s))
					{
						rightMat=Util.s_int(Import.objDescs[i][1]);
						break;
					}
				}
				s=I.description().trim().toUpperCase();
				if(rightMat<0)
				{
					Log.sysOut("Reset","Unconventional material: "+I.description());
					for(int i=0;i<EnvResource.RESOURCE_DESCS.length;i++)
					{
						if(EnvResource.RESOURCE_DESCS[i].equals(s))
						{
							rightMat=EnvResource.RESOURCE_DATA[i][0];
							break;
						}
					}
				}
				if(rightMat<0)
					Log.sysOut("Reset","Unknown material: "+I.description());
				else
				if(I.material()!=rightMat)
				{
					if(mob!=null)
					{
						if(mob.session().confirm("Change "+I.name()+"/"+I.displayText()+" material to "+EnvResource.RESOURCE_DESCS[rightMat&EnvResource.RESOURCE_MASK]+" (y/N)?","N"))
						{
							I.setMaterial(rightMat);
							I.setDescription("");
							return rightMat;
						}
					}
					else
					{
						Log.sysOut("Reset","Changed "+I.name()+"/"+I.displayText()+" material to "+EnvResource.RESOURCE_DESCS[rightMat&EnvResource.RESOURCE_MASK]+"!");
						I.setMaterial(rightMat);
						I.setDescription("");
						return rightMat;
					}
				}
				else
				{
					I.setDescription("");
					return rightMat;
				}
			}
		}
		return -1;
	}

	public void resetArea(Area area)
	{
		boolean mobile=area.getMobility();
		area.toggleMobility(false);
		for(Enumeration r=area.getMap();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			CoffeeUtensils.resetRoom(R);
		}
		area.fillInAreaRooms();
		area.toggleMobility(mobile);
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isASysOp(mob.location()))
		{
			mob.tell("You are not powerful enough to do that.");
			return false;
		}
		commands.removeElementAt(0);
		if(commands.size()<1)
		{
			mob.tell("Reset this Room, or the whole Area?");
			return false;
		}
		String s=(String)commands.elementAt(0);
		if(s.equalsIgnoreCase("room"))
		{
			CoffeeUtensils.resetRoom(mob.location());
			mob.tell("Done.");
		}
		else
		if(s.equalsIgnoreCase("area"))
		{
			resetArea(mob.location().getArea());
			mob.tell("Done.");
		}
		else
		if(s.equalsIgnoreCase("bankdata"))
		{
			String bank=Util.combine(commands,1);
			if(bank.length()==0){
				mob.tell("Which bank?");
				return false;
			}
			Vector V=CMClass.DBEngine().DBReadJournal(bank);
			for(int v=0;v<V.size();v++)
			{
				Vector V2=(Vector)V.elementAt(v);
				String name=(String)V2.elementAt(1);
				String ID=(String)V2.elementAt(4);
				String classID=((String)V2.elementAt(3));
				String data=((String)V2.elementAt(5));
				if(ID.equalsIgnoreCase("COINS")) classID="COINS";
				Item I=(Item)CMClass.getItem("GenItem").copyOf();
				CMClass.DBEngine().DBCreateData(name,bank,""+I,classID+";"+data);
			}
			CMClass.DBEngine().DBDeleteJournal(bank,Integer.MAX_VALUE);
			mob.tell(V.size()+" records done.");
		}
		else
		if(s.equalsIgnoreCase("arearoomids"))
		{
			Area A=mob.location().getArea();
			boolean somethingDone=false;
			for(Enumeration e=A.getMap();e.hasMoreElements();)
			{
				Room R=(Room)e.nextElement();
				if((R.roomID().length()>0)
				&&(R.roomID().indexOf("#")>0)
				&&(!R.roomID().startsWith(A.Name())))
				{
					String oldID=R.roomID();
					R.setRoomID(CMMap.getOpenRoomID(A.Name()));
					CMClass.DBEngine().DBReCreate(R,oldID);
					for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
					{
						Room R2=(Room)r.nextElement();
						if(R2!=R)
						for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
							if(R2.rawDoors()[d]==R)
							{
								CMClass.DBEngine().DBUpdateExits(R2);
								break;
							}
					}
					if(R instanceof GridLocale)
						R.getArea().fillInAreaRoom(R);
					somethingDone=true;
					mob.tell("Room "+oldID+" changed to "+R.roomID()+".");
				}
			}
			if(!somethingDone)
				mob.tell("No rooms were found which needed renaming.");
			else
				mob.tell("Done renumbering rooms.");
		}
		else
		if(s.equalsIgnoreCase("groundlydoors"))
		{
			if(mob.session()==null) return false;
			mob.session().print("working...");
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				boolean changed=false;
				if(R.roomID().length()>0)
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					Exit E=R.rawExits()[d];
					if((E!=null)&&E.hasADoor()&&E.name().equalsIgnoreCase("the ground"))
					{
						E.setName("a door");
						E.setExitParams("door","close","open","a door, closed.");
						changed=true;
					}
				}
				if(changed)
				{
					Log.sysOut("Reset","Groundly doors in "+R.roomID()+" fixed.");
					CMClass.DBEngine().DBUpdateExits(R);
				}
				mob.session().print(".");
			}
			mob.session().println("done!");
		}
		else
		if(s.equalsIgnoreCase("allmobarmorfix"))
		{
			if(mob.session()==null) return false;
			mob.session().print("working...");
			for(Enumeration a=CMMap.areas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				A.toggleMobility(false);
				for(Enumeration r=A.getMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if(R.roomID().length()==0) continue;
					CoffeeUtensils.resetRoom(R);
					boolean didSomething=false;
					for(int i=0;i<R.numInhabitants();i++)
					{
						MOB M=(MOB)R.fetchInhabitant(i);
						if((M.isMonster())
						&&(M.getStartRoom()==R)
						&&(M.baseEnvStats().armor()==((100-(M.baseEnvStats().level()*7)))))
						{
							int oldArmor=M.baseEnvStats().armor();
							M.baseEnvStats().setArmor(M.baseCharStats().getCurrentClass().getLevelArmor(M));
							M.recoverEnvStats();
							Log.sysOut("Reset","Updated "+M.name()+" in room "+R.roomID()+" from "+oldArmor+" to "+M.baseEnvStats().armor()+".");
							didSomething=true;
						}
						else
							Log.sysOut("Reset","Skipped "+M.name()+" in room "+R.roomID());
					}
					mob.session().print(".");
					if(didSomething)
						CMClass.DBEngine().DBUpdateMOBs(R);
				}
				A.toggleMobility(true);
			}
			mob.session().println("done!");
		}
		else
		if(s.equalsIgnoreCase("goldceilingfixer"))
		{
			if(mob.session()==null) return false;
			mob.session().print("working...");
			for(Enumeration a=CMMap.areas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				A.toggleMobility(false);
				for(Enumeration r=A.getMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if(R.roomID().length()==0) continue;
					CoffeeUtensils.resetRoom(R);
					boolean didSomething=false;
					for(int i=0;i<R.numInhabitants();i++)
					{
						MOB M=(MOB)R.fetchInhabitant(i);
						if((M.isMonster())
						&&(M.getStartRoom()==R)
						&&(M.getMoney()>(M.baseEnvStats().level()*20)))
						{
							M.setMoney(M.baseEnvStats().level()*20);
							Log.sysOut("Reset","Updated "+M.name()+" in room "+R.roomID()+".");
							didSomething=true;
						}
					}
					mob.session().print(".");
					if(didSomething)
						CMClass.DBEngine().DBUpdateMOBs(R);
				}
				A.toggleMobility(true);
			}
			mob.session().println("done!");
		}
		else
		if(s.equalsIgnoreCase("worldmatconfirm"))
		{
			if(mob.session()==null) return false;
			mob.session().print("working...");
			for(Enumeration a=CMMap.areas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				A.toggleMobility(false);
				for(Enumeration r=A.getMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if(R.roomID().length()>0)
					{
						CoffeeUtensils.resetRoom(R);
						boolean changedMOBS=false;
						boolean changedItems=false;
						for(int i=0;i<R.numItems();i++)
							changedItems=changedItems||(rightImportMat(null,R.fetchItem(i),false)>=0);
						for(int m=0;m<R.numInhabitants();m++)
						{
							MOB M=R.fetchInhabitant(m);
							if(M==mob) continue;
							if(!M.isEligibleMonster()) continue;
							for(int i=0;i<M.inventorySize();i++)
								changedMOBS=changedMOBS||(rightImportMat(null,M.fetchInventory(i),false)>=0);
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
										boolean didSomething=false;
										didSomething=rightImportMat(null,I,false)>=0;
										changedMOBS=changedMOBS||didSomething;
										if(didSomething)
										{
											int numInStock=SK.numberInStock(I);
											int stockPrice=SK.stockPrice(I);
											SK.delStoreInventory(I);
											SK.addStoreInventory(I,numInStock,stockPrice);
										}
									}
								}
							}
						}
						if(changedItems)
							CMClass.DBEngine().DBUpdateItems(R);
						if(changedMOBS)
							CMClass.DBEngine().DBUpdateMOBs(R);
						mob.session().print(".");
					}
				}
				A.toggleMobility(true);
			}
			mob.session().println("done!");
		}
		else
		if(s.equalsIgnoreCase("worlditemfixer"))
		{
			if(mob.session()==null) return false;
			mob.session().print("working...");
			for(Enumeration a=CMMap.areas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				A.toggleMobility(false);
				for(Enumeration r=A.getMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if(R.roomID().length()>0)
					{
						CoffeeUtensils.resetRoom(R);
						boolean changedMOBS=false;
						boolean changedItems=false;
						for(int i=0;i<R.numItems();i++)
						{
							Item I=R.fetchItem(i);
							if(itemFix(I))
								changedItems=true;
						}
						for(int m=0;m<R.numInhabitants();m++)
						{
							MOB M=R.fetchInhabitant(m);
							if(M==mob) continue;
							if(!M.isEligibleMonster()) continue;
							for(int i=0;i<M.inventorySize();i++)
							{
								Item I=M.fetchInventory(i);
								if(itemFix(I))
									changedMOBS=true;
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
										boolean didSomething=false;
										didSomething=itemFix(I);
										changedMOBS=changedMOBS||didSomething;
										if(didSomething)
										{
											int numInStock=SK.numberInStock(I);
											int stockPrice=SK.stockPrice(I);
											SK.delStoreInventory(I);
											SK.addStoreInventory(I,numInStock,stockPrice);
										}
									}
								}
							}
						}
						if(changedItems)
							CMClass.DBEngine().DBUpdateItems(R);
						if(changedMOBS)
							CMClass.DBEngine().DBUpdateMOBs(R);
						mob.session().print(".");
					}
				}
				A.toggleMobility(true);
			}
			mob.session().println("done!");
		}
		else
		if(s.equalsIgnoreCase("bedfixer"))
		{
			if(mob.session()==null) return false;
			mob.session().print("working...");
			for(Enumeration a=CMMap.areas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				A.toggleMobility(false);
				for(Enumeration r=A.getMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if(R.roomID().length()>0)
					{
						CoffeeUtensils.resetRoom(R);
						boolean changedItems=false;
						for(int i=0;i<R.numItems();i++)
						{

						}
						if(changedItems)
							CMClass.DBEngine().DBUpdateItems(R);
						mob.session().print(".");
					}
				}
				A.toggleMobility(true);
			}
			mob.session().println("done!");
		}
		else
		if(s.equalsIgnoreCase("clantick"))
			Clans.tickAllClans();
		else
		if(s.equalsIgnoreCase("arearacemat"))
		{
			// this is just utility code and will change frequently
			Area A=mob.location().getArea();
			resetArea(A);
			A.toggleMobility(false);
			Hashtable rememberI=new Hashtable();
			Hashtable rememberM=new Hashtable();
			try{
			for(Enumeration r=A.getMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				CoffeeUtensils.resetRoom(R);
				boolean somethingDone=false;
				mob.tell(R.roomID()+"/"+R.name()+"/"+R.displayText()+"--------------------");
				for(int i=R.numItems()-1;i>=0;i--)
				{
					Item I=R.fetchItem(i);
					if(I.ID().equalsIgnoreCase("GenWallpaper")) continue;
					int returned=resetAreaOramaManaI(mob,I,rememberI," ");
					if(returned<0)
					{
						R.delItem(I);
						somethingDone=true;
						mob.tell(" deleted");
					}
					else
					if(returned>0)
						somethingDone=true;
				}
				if(somethingDone)
					CMClass.DBEngine().DBUpdateItems(R);
				somethingDone=false;
				for(int m=0;m<R.numInhabitants();m++)
				{
					MOB M=R.fetchInhabitant(m);
					if(M==mob) continue;
					if(!M.isEligibleMonster()) continue;
					Race R2=(Race)rememberM.get(M.Name());
					if(R2!=null)
					{
						if(M.charStats().getMyRace()==R2)
							mob.tell(" "+M.Name()+" still "+R2.name());
						else
						{
							M.baseCharStats().setMyRace(R2);
							R2.setHeightWeight(M.baseEnvStats(),(char)M.baseCharStats().getStat(CharStats.GENDER));
							M.recoverCharStats();
							M.recoverEnvStats();
							mob.tell(" "+M.Name()+" Changed to "+R2.ID());
							somethingDone=true;
						}
					}
					else
					while(true)
					{
						String str=mob.session().prompt(" "+M.Name()+"/"+M.charStats().getMyRace().ID(),"");
						if(str.length()==0)
						{
							rememberM.put(M.name(),M.baseCharStats().getMyRace());
							break;
						}
						if(str.equals("?"))
							mob.tell(M.Name()+"/"+M.displayText()+"/"+M.description());
						else
						{
							R2=CMClass.getRace(str);
							if(R2==null)
							{
								String poss="";
								if(poss.length()==0)
								for(Enumeration e=CMClass.races();e.hasMoreElements();)
								{
									Race R3=(Race)e.nextElement();
									if(R3.ID().toUpperCase().startsWith(str.toUpperCase()))
									   poss=R3.name();
								}
								if(poss.length()==0)
								for(Enumeration e=CMClass.races();e.hasMoreElements();)
								{
									Race R3=(Race)e.nextElement();
									if(R3.ID().toUpperCase().indexOf(str.toUpperCase())>=0)
									   poss=R3.name();
								}
								if(poss.length()==0)
								for(Enumeration e=CMClass.races();e.hasMoreElements();)
								{
									Race R3=(Race)e.nextElement();
									if(R3.name().toUpperCase().startsWith(str.toUpperCase()))
									   poss=R3.name();
								}
								if(poss.length()==0)
								for(Enumeration e=CMClass.races();e.hasMoreElements();)
								{
									Race R3=(Race)e.nextElement();
									if(R3.name().toUpperCase().indexOf(str.toUpperCase())>=0)
									   poss=R3.name();
								}
								mob.tell(" '"+str+"' is not a valid race.  Try '"+poss+"'.");
								continue;
							}
							mob.tell(" Changed to "+R2.ID());
							M.baseCharStats().setMyRace(R2);
							R2.setHeightWeight(M.baseEnvStats(),(char)M.baseCharStats().getStat(CharStats.GENDER));
							M.recoverCharStats();
							M.recoverEnvStats();
							rememberM.put(M.name(),M.baseCharStats().getMyRace());
							somethingDone=true;
							break;
						}
					}
					for(int i=M.inventorySize()-1;i>=0;i--)
					{
						Item I=M.fetchInventory(i);
						int returned=resetAreaOramaManaI(mob,I,rememberI,"   ");
						if(returned<0)
						{
							M.delInventory(I);
							somethingDone=true;
							mob.tell("   deleted");
						}
						else
						if(returned>0)
							somethingDone=true;
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
								int returned=resetAreaOramaManaI(mob,I,rememberI," - ");
								if(returned<0)
								{
									SK.delStoreInventory(I);
									somethingDone=true;
									mob.tell("   deleted");
								}
								else
								if(returned>0)
								{
									somethingDone=true;
									int numInStock=SK.numberInStock(I);
									int stockPrice=SK.stockPrice(I);
									SK.delStoreInventory(I);
									SK.addStoreInventory(I,numInStock,stockPrice);
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
					CMClass.DBEngine().DBUpdateMOBs(R);
			}
			}
			catch(java.io.IOException e){}
			A.toggleMobility(true);
			mob.tell("Done.");
		}
		else
			mob.tell("'"+s+"' is an unknown reset.  Try ROOM, AREA, AREARACEMAT *, MOBSTATS ROOM *, MOBSTATS AREA *, MOBSTATS WORLD *, AREAROOMIDS *.\n\r * = Reset functions which may take a long time to complete.");
		return false;
	}

	public boolean fixRejuvItem(Item I)
	{
		Ability A=I.fetchEffect("ItemRejuv");
		if(A!=null)
		{
			A=I.fetchEffect("ItemRejuv");
			if(!A.isBorrowed(I))
				return false;
			A.setBorrowed(I,true);
			return true;
		}
		return false;
	}
	
	public boolean itemFix(Item I)
	{
		if((I instanceof Weapon)||(I instanceof Armor))
		{
			int lvl=I.baseEnvStats().level();
			Ability ADJ=I.fetchEffect("Prop_WearAdjuster");
			if(ADJ==null) ADJ=I.fetchEffect("Prop_HaveAdjuster");
			if(ADJ==null) ADJ=I.fetchEffect("Prop_RideAdjuster");
			Ability RES=I.fetchEffect("Prop_WearResister");
			if(RES==null) RES=I.fetchEffect("Prop_HaveResister");
			Ability CAST=I.fetchEffect("Prop_WearSpellCast");
			int castMul=1;
			if(CAST==null) CAST=I.fetchEffect("Prop_UseSpellCast");
			if(CAST==null) CAST=I.fetchEffect("Prop_UseSpellCast2");
			if(CAST==null) CAST=I.fetchEffect("Prop_HaveSpellCast");
			if(CAST==null){ CAST=I.fetchEffect("Prop_FightSpellCast"); castMul=-1;}
			int[] LVLS=getItemLevels(I,ADJ,RES,CAST);
			int TLVL=totalLevels(LVLS);
			if(lvl<0)
			{
				if(TLVL<=0)
					lvl=1;
				else
					lvl=TLVL;
				I.baseEnvStats().setLevel(lvl);
				I.recoverEnvStats();
				fixRejuvItem(I);
				return true;
			}
			if(TLVL<=0) return fixRejuvItem(I);
			if(TLVL<=(lvl+25)) return fixRejuvItem(I);
			int FTLVL=TLVL;
			int lastLvl=-1;
			Vector illegalNums=new Vector();
			Log.sysOut("Reset",I.name()+"("+I.baseEnvStats().level()+") "+TLVL+", "+I.baseEnvStats().armor()+"/"+I.baseEnvStats().attackAdjustment()+"/"+I.baseEnvStats().damage()+"/"+((ADJ!=null)?ADJ.text():"null"));
			while((TLVL>(lvl+15))&&(illegalNums.size()<4))
			{
				lastLvl=TLVL;
				int highIndex=-1;
				for(int i=0;i<LVLS.length;i++)
					if(((highIndex<0)||(LVLS[i]>LVLS[highIndex]))
					&&(!illegalNums.contains(new Integer(i))))
						highIndex=i;
				if(highIndex<0) break;
				switch(highIndex)
				{
				case 0:
					if(I instanceof Weapon)
					{
						String s=(ADJ!=null)?ADJ.text():"";
						int oldAtt=I.baseEnvStats().attackAdjustment();
						int oldDam=I.baseEnvStats().damage();
						toneDownWeapon((Weapon)I,ADJ);
						if((I.baseEnvStats().attackAdjustment()==oldAtt)
						&&(I.baseEnvStats().damage()==oldDam)
						&&((ADJ==null)||(ADJ.text().equals(s))))
							illegalNums.addElement(new Integer(0));
					}
					else
					{
						String s=(ADJ!=null)?ADJ.text():"";
						int oldArm=I.baseEnvStats().armor();
						toneDownArmor((Armor)I,ADJ);
						if((I.baseEnvStats().armor()==oldArm)
						&&((ADJ==null)||(ADJ.text().equals(s))))
							illegalNums.addElement(new Integer(0));
					}
					break;
				case 1:
					if(I.baseEnvStats().ability()>0)
						I.baseEnvStats().setAbility(I.baseEnvStats().ability()-1);
					else
						illegalNums.addElement(new Integer(1));
					break;
				case 2:
					illegalNums.addElement(new Integer(2));
					// nothing I can do!;
					break;
				case 3:
					if(ADJ==null)
						illegalNums.addElement(new Integer(3));
					else
					{
						String oldTxt=ADJ.text();
						toneDownAdjuster(I,ADJ);
						if(ADJ.text().equals(oldTxt))
							illegalNums.addElement(new Integer(3));
					}
					break;
				}
				LVLS=getItemLevels(I,ADJ,RES,CAST);
				TLVL=totalLevels(LVLS);
			}
			Log.sysOut("Reset",I.name()+"("+I.baseEnvStats().level()+") "+FTLVL+"->"+TLVL+", "+I.baseEnvStats().armor()+"/"+I.baseEnvStats().attackAdjustment()+"/"+I.baseEnvStats().damage()+"/"+((ADJ!=null)?ADJ.text():"null"));
			fixRejuvItem(I);
			return true;
		}
		return fixRejuvItem(I);
	}
	
	public void toneDownWeapon(Weapon W, Ability ADJ)
	{
		boolean fixdam=true;
		boolean fixatt=true;
		if((ADJ!=null)&&(ADJ.text().toUpperCase().indexOf("DAMAGE+")>=0))
		{
			int a=ADJ.text().toUpperCase().indexOf("DAMAGE+");
			int a2=ADJ.text().toUpperCase().indexOf(" ",a+4);
			if(a2<0) a2=ADJ.text().length();
			int num=Util.s_int(ADJ.text().substring(a+7,a2));
			if(num>W.baseEnvStats().damage())
			{
				fixdam=false;
				ADJ.setMiscText(ADJ.text().substring(0,a+7)+(num/2)+ADJ.text().substring(a2));
			}
		}
		if((ADJ!=null)&&(ADJ.text().toUpperCase().indexOf("ATTACK+")>=0))
		{
			int a=ADJ.text().toUpperCase().indexOf("ATTACK+");
			int a2=ADJ.text().toUpperCase().indexOf(" ",a+4);
			if(a2<0) a2=ADJ.text().length();
			int num=Util.s_int(ADJ.text().substring(a+7,a2));
			if(num>W.baseEnvStats().attackAdjustment())
			{
				fixatt=false;
				ADJ.setMiscText(ADJ.text().substring(0,a+7)+(num/2)+ADJ.text().substring(a2));
			}
		}
		if(fixdam&&(W.baseEnvStats().damage()>=2))
			W.baseEnvStats().setDamage(W.baseEnvStats().damage()/2);
		if(fixatt&&(W.baseEnvStats().attackAdjustment()>=2))
			W.baseEnvStats().setAttackAdjustment(W.baseEnvStats().attackAdjustment()/2);
		W.recoverEnvStats();
	}
	public void toneDownArmor(Armor A, Ability ADJ)
	{
		boolean fixit=true;
		if((ADJ!=null)&&(ADJ.text().toUpperCase().indexOf("ARMOR-")>=0))
		{
			int a=ADJ.text().toUpperCase().indexOf("ARMOR-");
			int a2=ADJ.text().toUpperCase().indexOf(" ",a+4);
			if(a2<0) a2=ADJ.text().length();
			int num=Util.s_int(ADJ.text().substring(a+6,a2));
			if(num>A.baseEnvStats().armor())
			{
				fixit=false;
				ADJ.setMiscText(ADJ.text().substring(0,a+6)+(num/2)+ADJ.text().substring(a2));
			}
		}
		if(fixit&&(A.baseEnvStats().armor()>=2))
		{
			A.baseEnvStats().setArmor(A.baseEnvStats().armor()/2);
			A.recoverEnvStats();
		}
	}
	
	public void toneDownAdjuster(Item I, Ability ADJ)
	{
		String s=ADJ.text();
		int plusminus=s.indexOf("+");
		int minus=s.indexOf("-");
		if((minus>=0)&&((plusminus<0)||(minus<plusminus)))
			plusminus=minus;
		while(plusminus>=0)
		{
			int spaceafter=s.indexOf(" ",plusminus+1);
			if(spaceafter<0) spaceafter=s.length();
			if(spaceafter>plusminus)
			{
				String number=s.substring(plusminus+1,spaceafter).trim();
				if(Util.isNumber(number))
				{
					int num=Util.s_int(number);
					int spacebefore=s.lastIndexOf(" ",plusminus);
					if(spacebefore<0) spacebefore=0;
					if(spacebefore<plusminus)
					{
						boolean proceed=true;
						String wd=s.substring(spacebefore,plusminus).trim().toUpperCase();
						if(wd.startsWith("DIS")) 
							proceed=false;
						else
						if(wd.startsWith("SEN")) 
							proceed=false;
						else
						if(wd.startsWith("ARM")&&(I instanceof Armor))
							proceed=false;
						else
						if(wd.startsWith("ATT")&&(I instanceof Weapon))
						   proceed=false;
						else
						if(wd.startsWith("DAM")&&(I instanceof Weapon))
						   proceed=false;
						else
						if(wd.startsWith("ARM")&&(s.charAt(plusminus)=='+'))
							proceed=false;
						else
						if((!wd.startsWith("ARM"))&&(s.charAt(plusminus)=='-'))
							proceed=false;
						if(proceed)
						{
							if((num!=1)&&(num!=-1))
								s=s.substring(0,plusminus+1)+(num/2)+s.substring(spaceafter);
						}
					}
				}
			}
			minus=s.indexOf("-",plusminus+1);
			plusminus=s.indexOf("+",plusminus+1);
			if((minus>=0)&&((plusminus<0)||(minus<plusminus)))
				plusminus=minus;
		}
		ADJ.setMiscText(s);
	}
	
	public int[] getItemLevels(Item I, Ability ADJ, Ability RES, Ability CAST)
	{
		int[] LVLS=new int[4];
		LVLS[0]=timsBaseLevel(I,ADJ);
		LVLS[1]=levelsFromAbility(I);
		LVLS[2]=levelsFromCaster(I,CAST);
		LVLS[3]=levelsFromAdjuster(I,ADJ);
		return LVLS;
	}
	
	public int totalLevels(int[] levels)
	{ 
		int lvl=levels[0]; 
		for(int i=1;i<levels.length;i++) 
		    lvl+=levels[i]; 
		return lvl;
	}
	
	public static int timsBaseLevel(Item I, Ability ADJ)
	{
		int level=0;
		int otherDam=0;
		int otherAtt=0;
		int otherArm=0;
		if(ADJ!=null)
		{
			otherArm=Util.getParmPlus(ADJ.text(),"arm")*-1;
			otherAtt=Util.getParmPlus(ADJ.text(),"att");
			otherDam=Util.getParmPlus(ADJ.text(),"dam");
		}
		int curArmor=I.baseEnvStats().armor()+otherArm;
		double curAttack=new Integer(I.baseEnvStats().attackAdjustment()+otherAtt).doubleValue();
		double curDamage=new Integer(I.baseEnvStats().damage()+otherDam).doubleValue();
		if(I instanceof Weapon)
		{
			double weight=new Integer(I.baseEnvStats().weight()).doubleValue();
			if(weight<1.0) weight=1.0;
			double range=new Integer(I.maxRange()).doubleValue();
			level=(int)Math.round(Math.floor((2.0*curDamage/(2.0*(I.rawLogicalAnd()?2.0:1.0)+1.0)+(curAttack-weight)/5.0+range)*(range/weight+2.0)))+1;
		}
		else
		{
			long worndata=I.rawProperLocationBitmap();
			double weightpts=0;
			for(int i=0;i<Item.wornWeights.length-1;i++)
			{
				if(Util.isSet(worndata,i))
				{
					weightpts+=Item.wornWeights[i+1];
					if(!I.rawLogicalAnd()) break;
				}
			}
			int[] leatherPoints={ 0, 0, 1, 5,10,16,23,31,40,49,58,67,76,85,94};
			int[] clothPoints=  { 0, 3, 7,12,18,25,33,42,52,62,72,82,92,102};
			int[] metalPoints=  { 0, 0, 0, 0, 1, 3, 5, 8,12,17,23,30,38,46,54,62,70,78,86,94};
			int materialCode=I.material()&EnvResource.MATERIAL_MASK;
			int[] useArray=null;
			switch(materialCode)
			{
			case EnvResource.MATERIAL_METAL:
			case EnvResource.MATERIAL_MITHRIL:
			case EnvResource.MATERIAL_PRECIOUS:
			case EnvResource.MATERIAL_ENERGY:
				useArray=metalPoints;
				break;
			case EnvResource.MATERIAL_PLASTIC:
			case EnvResource.MATERIAL_LEATHER:
			case EnvResource.MATERIAL_GLASS:
			case EnvResource.MATERIAL_ROCK:
			case EnvResource.MATERIAL_WOODEN:
				useArray=leatherPoints;
				break;
			default:
				useArray=clothPoints;
				break;
			}
			int which=(int)Math.round(Util.div(curArmor,weightpts)+1);
			if(which<0) which=0;
			if(which>=useArray.length)
				which=useArray.length-1;
			level=useArray[which];
		}
		return level;
	}

	public int levelsFromAbility(Item savedI)
	{ return savedI.baseEnvStats().ability()*5;}
	
	public int levelsFromCaster(Item savedI, Ability CAST)
	{
		int level=0;
		if(CAST!=null)
		{
			String ID=CAST.ID().toUpperCase();
			Vector theSpells=new Vector();
			String names=CAST.text();
			int del=names.indexOf(";");
			while(del>=0)
			{
				String thisOne=names.substring(0,del);
				Ability A=(Ability)CMClass.getAbility(thisOne);
				if(A!=null)	theSpells.addElement(A);
				names=names.substring(del+1);
				del=names.indexOf(";");
			}
			Ability A=(Ability)CMClass.getAbility(names);
			if(A!=null) theSpells.addElement(A);
			for(int v=0;v<theSpells.size();v++)
			{
				A=(Ability)theSpells.elementAt(v);
				int mul=1;
				if(A.quality()==Ability.MALICIOUS) mul=-1;
				if(ID.indexOf("HAVE")>=0)
					level+=(mul*CMAble.lowestQualifyingLevel(A.ID()));
				else
					level+=(mul*CMAble.lowestQualifyingLevel(A.ID())/2);
			}
		}
		return level;
	}
	public int levelsFromAdjuster(Item savedI, Ability ADJ)
	{
		int level=0;
		if(ADJ!=null)
		{
			String newText=ADJ.text();
			int ab=Util.getParmPlus(newText,"abi");
			int arm=Util.getParmPlus(newText,"arm")*-1;
			int att=Util.getParmPlus(newText,"att");
			int dam=Util.getParmPlus(newText,"dam");
			if(savedI instanceof Weapon)
				level+=(arm*2);
			else
			if(savedI instanceof Armor)
			{
				level+=(att/2);
				level+=(dam*3);
			}
			level+=ab*5;
			
			
			int dis=Util.getParmPlus(newText,"dis");
			if(dis!=0) level+=5;
			int sen=Util.getParmPlus(newText,"sen");
			if(sen!=0) level+=5;
			level+=(int)Math.round(5.0*Util.getParmDoublePlus(newText,"spe"));
			for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
			{
				int stat=Util.getParmPlus(newText,CharStats.TRAITS[i].substring(0,3).toLowerCase());
				int max=Util.getParmPlus(newText,("max"+(CharStats.TRAITS[i].substring(0,3).toLowerCase())));
				level+=(stat*5);
				level+=(max*5);
			}

			int hit=Util.getParmPlus(newText,"hit");
			int man=Util.getParmPlus(newText,"man");
			int mv=Util.getParmPlus(newText,"mov");
			level+=(hit/5);
			level+=(man/5);
			level+=(mv/5);
		}
		return level;
	}
}
