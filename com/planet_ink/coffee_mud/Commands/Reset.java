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
				return false;
			}

			for(Enumeration r=rooms.elements();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				R.getArea().toggleMobility(false);
				CoffeeUtensils.resetRoom(R);
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
					CMClass.DBEngine().DBUpdateMOBs(R);
				}
				R.getArea().toggleMobility(true);
			}

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
		if(s.equalsIgnoreCase("conquestinstall"))
		{
			if(mob.session()==null) return false;
			mob.session().print("working...");
			for(Enumeration r=CMMap.areas();r.hasMoreElements();)
			{
				Area A=(Area)r.nextElement();
				Vector V=Sense.flaggedBehaviors(A,Behavior.FLAG_LEGALBEHAVIOR);
				if((V==null)||(V.size()==0))
				{
					Behavior B=CMClass.getBehavior("Conquerable");
					B.setParms("JOURNAL=\"The Conquest Chronicles\" LAW=TRUE");
					A.addBehavior(B);
					CMClass.DBEngine().DBUpdateArea(A.Name(),A);
					mob.session().print(".");
				}
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
		if(s.equalsIgnoreCase("smalleropendoors"))
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
					if((E!=null)
					&&(E.isGeneric())
					&&(!E.hasADoor())
					&&E.name().equalsIgnoreCase("the ground")
					&&(!E.isReadable())
					&&(E.temporaryDoorLink().length()==0)
					&&(E.displayText().equals(E.description())))
					{
						Exit E2=CMClass.getExit("OpenDescriptable");
						E2.setMiscText(E.displayText());
						R.rawExits()[d]=E2;
						changed=true;
					}
				}
				if(changed)
				{
					Log.sysOut("Reset","Fat doors in "+R.roomID()+" fixed.");
					CMClass.DBEngine().DBUpdateExits(R);
				}
				mob.session().print(".");
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
		if(s.equalsIgnoreCase("worlddescclear"))
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
							changedItems=changedItems||rightImportMat(mob,R.fetchItem(i),true)>=0;
						for(int m=0;m<R.numInhabitants();m++)
						{
							MOB M=R.fetchInhabitant(m);
							if(M==mob) continue;
							if(!M.isEligibleMonster()) continue;
							for(int i=0;i<M.inventorySize();i++)
								changedMOBS=changedMOBS||rightImportMat(mob,M.fetchInventory(i),true)>=0;
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
										didSomething=rightImportMat(mob,I,true)>=0;
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
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean arcCommand(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
