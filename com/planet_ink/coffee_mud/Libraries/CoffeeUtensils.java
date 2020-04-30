package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/*
   Copyright 2001-2020 Bo Zimmerman

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
public class CoffeeUtensils extends StdLibrary implements CMMiscUtils
{
	@Override
	public String ID()
	{
		return "CoffeeUtensils";
	}

	private TriadVector<Integer,Integer,MaskingLibrary.CompiledZMask> lootPolicy = null;
	private final TriadVector<Integer,Integer,MaskingLibrary.CompiledZMask> noLootPolicy = new TriadVector<Integer,Integer,MaskingLibrary.CompiledZMask>();
	private int lastLootPolicyHash=0;

	public static final int LOOTFLAG_RUIN=1;
	public static final int LOOTFLAG_LOSS=2;
	public static final int LOOTFLAG_WORN=4;
	public static final int LOOTFLAG_UNWORN=8;

	@Override
	public String niceCommaList(final List<?> V, final boolean andTOrF)
	{
		String id="";
		for(int v=0;v<V.size();v++)
		{
			String s=null;
			if(V.get(v) instanceof Environmental)
				s=((Environmental)V.get(v)).name();
			else
			if(V.get(v) instanceof String)
				s=(String)V.get(v);
			else
				continue;
			if(V.size()==1)
				id+=s;
			else
			if(v==(V.size()-1))
				id+=((andTOrF)?"and ":"or ")+s;
			else
				id+=s+", ";
		}
		return id;
	}

	protected int getSimpleWeight(final Physical P)
	{
		if(P instanceof Item)
			return ((Item)P).recursiveWeight();
		else
			return P.phyStats().weight();
	}

	@Override
	public int getPullWeight(final Physical P)
	{
		if(P instanceof Rider)
		{
			Rider R=(Rider)P;
			int ct=99;
			while((R.riding() != null) &&(--ct>0))
				R=R.riding();
			if(R instanceof Rideable)
			{
				int totalWeight=0;
				final boolean smallWeight = (((Rideable)R).rideBasis()==Rideable.RIDEABLE_WAGON);
				final LinkedList<Rider> weightsToDo = new LinkedList<Rider>();
				weightsToDo.add(R);
				while((weightsToDo.size()>0)
				&&(totalWeight < Integer.MAX_VALUE/2))
				{
					R=weightsToDo.pop();
					totalWeight += getSimpleWeight(R);
					if(R instanceof Rideable)
					{
						for(final Enumeration<Rider> r=((Rideable)R).riders();r.hasMoreElements();)
							weightsToDo.addLast(r.nextElement());
					}
				}
				if(smallWeight)
					totalWeight /= 300;
				return totalWeight;
			}
			else
				return getSimpleWeight(P);
		}
		else
			return getSimpleWeight(P);
	}

	@Override
	public String getFormattedDate(final Environmental E)
	{
		String date=CMStrings.padRight(L("Unknown"),11);
		if(E!=null)
		{
			final Area A=CMLib.map().areaLocation(E);
			final TimeClock C;
			if(A != null)
				C=A.getTimeObj();
			else
				C=CMLib.time().globalClock();
			if(C!=null)
				date=CMStrings.padRight(C.getDayOfMonth()+"-"+C.getMonth()+"-"+C.getYear(),11);
		}
		return date;
	}

	@Override
	public void outfit(final MOB mob, final List<Item> items)
	{
		if((mob==null)||(items==null)||(items.size()==0))
			return;
		for(int i=0;i<items.size();i++)
		{
			Item I=items.get(i);
			if(mob.findItem("$"+I.name()+"$")==null)
			{
				I=(Item)I.copyOf();
				I.text();
				I.recoverPhyStats();
				mob.addItem(I);
				if(I.whereCantWear(mob)<=0)
				{
					I.wearIfPossible(mob);
					if(I.rawWornCode()!=0)
						mob.executeMsg(mob, CMClass.getMsg(mob, I,CMMsg.MSG_WIELD|CMMsg.MASK_ALWAYS, null));
				}
				if(((I instanceof Armor)||(I instanceof Weapon))
				&&(I.amWearingAt(Wearable.IN_INVENTORY)))
					I.destroy();
			}
		}
	}

	@Override
	public Trap makeADeprecatedTrap(final Physical unlockThis)
	{
		Trap theTrap=null;
		final int roll=(int)Math.round(Math.random()*100.0);
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
			if(((Container)unlockThis).hasADoor())
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

	@Override
	public void setTrapped(final Physical myThang)
	{
		final Trap t=makeADeprecatedTrap(myThang);
		t.setReset(50);
		setTrapped(myThang,t);
	}

	@Override
	public void setTrapped(final Physical myThang, final Trap theTrap)
	{
		for(int a=0;a<myThang.numEffects();a++)
		{
			final Ability A=myThang.fetchEffect(a);
			if((A!=null)&&(A instanceof Trap))
				A.unInvoke();
		}

		if(myThang.fetchEffect(theTrap.ID())==null)
			myThang.addEffect(theTrap);
	}

	@Override
	public Trap fetchMyTrap(final Physical myThang)
	{
		if(myThang==null)
			return null;
		for(final Enumeration<Ability> a=myThang.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A instanceof  Trap))
				return (Trap)A;
		}
		return null;
	}

	@Override
	public boolean reachableItem(final MOB mob, final Environmental E)
	{
		if((E==null)||(!(E instanceof Item)))
			return true;
		final Item I=(Item)E;
		final Rideable R=mob.riding();
		final ItemPossessor owner=I.owner();
		if((mob.isMine(I))
		||((mob.riding()!=null)&&((I==mob.riding())
								  ||(owner==R)
								  ||(I.ultimateContainer(R)==R)))
		||(owner==null)
		||((owner instanceof Room)&&(!((Room)owner).isContent(I))))
			return true;
		return false;
	}

	@Override
	public double memoryUse (Environmental E, final int number )
	{
		double s=-1.0;
		try
		{
			final int n = number;
			final Object[] objs = new Object[n] ;
			final Environmental cl = E;
			final Runtime rt = Runtime.getRuntime() ;
			final long m0 =rt.totalMemory() - rt.freeMemory() ;
			System.gc() ;
			Thread.sleep( 500 ) ;
			for (int i = 0 ; i < n ; ++i)
				objs[i] = E = (Environmental)cl.copyOf();
			System.gc() ;
			Thread.sleep( 1000 ) ;
			final long m1 =rt.totalMemory() - rt.freeMemory() ;
			final long dm = m1 - m0 ;
			s = (double)dm / (double)n ;
			if(s<0.0)
				return memoryUse(E,number);
		}
		catch (final Exception e)
		{
			return -1;
		}
		return s;
	}

	@Override
	public Language getLanguageSpoken(final Physical P)
	{
		Ability A=null;
		if(P instanceof MOB)
		{
			for(int i=0;i<((MOB)P).numAllEffects();i++)
			{
				A=P.fetchEffect(i);
				if((A instanceof Language)
				&& (((Language)A).beingSpoken(A.ID())))
					return (Language)A;
			}
		}
		else
		{
			for(int i=0;i<P.numEffects();i++)
			{
				A=P.fetchEffect(i);
				if((A instanceof Language) && (((Language)A).beingSpoken(A.ID())))
					return (Language)A;
			}
		}
		return null;
	}

	@Override
	public void extinguish(final MOB source, final Physical target, final boolean mundane)
	{
		if(target instanceof Room)
		{
			final Room R=(Room)target;
			for(int m=0;m<R.numInhabitants();m++)
			{
				final MOB M=R.fetchInhabitant(m);
				if(M!=null)
					extinguish(source,M,mundane);
			}
			for(int i=0;i<R.numItems();i++)
			{
				final Item I=R.getItem(i);
				if(I!=null)
					extinguish(source,I,mundane);
			}
			return;
		}
		for(int a=target.numEffects()-1;a>=0;a--) // personal effects
		{
			final Ability A=target.fetchEffect(a);
			if((A!=null)&&((!mundane)||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PROPERTY)))
			{
				if((CMath.bset(A.flags(),Ability.FLAG_HEATING)&&(!mundane))
				||(CMath.bset(A.flags(),Ability.FLAG_FIREBASED))
				||((A.ID().equalsIgnoreCase("Spell_SummonElemental")&&A.text().toUpperCase().indexOf("FIRE")>=0)))
					A.unInvoke();
			}
		}
		if((target instanceof MOB)&&(!mundane))
		{
			final MOB tmob=(MOB)target;
			if(tmob.charStats().getMyRace().ID().equals("FireElemental"))
				CMLib.combat().postDeath(source,(MOB)target,null);
			for(int i=0;i<tmob.numItems();i++)
			{
				final Item I=tmob.getItem(i);
				if(I!=null)
					extinguish(tmob,I,mundane);
			}
		}
		if((target instanceof Light)&&(((Light)target).isLit()))
		{
			((Light)target).tick(target,Tickable.TICKID_LIGHT_FLICKERS);
			((Light)target).light(false);
		}
	}

	@Override
	public void roomAffectFully(final CMMsg msg, final Room room, int dirCode)
	{
		room.send(msg.source(),msg);
		if((msg.target()==null)||(!(msg.target() instanceof Exit)))
			return;
		if(dirCode<0)
		{
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				if (room.getExitInDir(d) == msg.target())
				{
					dirCode = d;
					break;
				}
			}
		}
		if(dirCode<0)
			return;
		final Exit pair=room.getPairedExit(dirCode);
		if(pair!=null)
		{
			CMMsg altMsg=null;
			if((msg.targetMinor()==CMMsg.TYP_OPEN)&&(pair.isLocked()))
			{
				altMsg=CMClass.getMsg(msg.source(),pair,msg.tool(),CMMsg.MSG_UNLOCK,null,CMMsg.MSG_UNLOCK,null,CMMsg.MSG_UNLOCK,null);
				pair.executeMsg(msg.source(),altMsg);
			}
			altMsg=CMClass.getMsg(msg.source(),pair,msg.tool(),msg.sourceCode(),null,msg.targetCode(),null,msg.othersCode(),null);
			pair.executeMsg(msg.source(),altMsg);
		}
	}

	@Override
	public int disenchantItem(final Item target)
	{
		int level=target.basePhyStats().level();
		boolean doneSomething=false;
		if(target instanceof Wand)
		{
			final Ability A=((Wand)target).getSpell();
			if(A!=null)
				level=level-CMLib.ableMapper().lowestQualifyingLevel(A.ID())+2;
			((Wand)target).setSpell(null);
			((Wand)target).setUsesRemaining(0);
			doneSomething=true;
		}
		else
		if(target instanceof SpellHolder)
		{
			((SpellHolder)target).setSpellList("");
			doneSomething=true;
		}
		else
		if((target.phyStats().ability()>0)
		&&(!(target instanceof Coins)))
		{
			level=level-(target.basePhyStats().ability()*3);
			target.basePhyStats().setAbility(0);
			doneSomething=true;
		}

		final LinkedList<Ability> affects=new LinkedList<Ability>();
		for(final Enumeration<Ability> a=target.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&(!A.ID().equals("ExtraData")))
				affects.add(A);
		}
		for(final Ability A : affects)
		{
			A.unInvoke();
			level=level-1;
			target.delEffect(A);
			doneSomething=true;
		}
		if(target.amDestroyed())
			return 0;
		if(doneSomething)
			return level;
		return -999;
	}

	@Override
	public boolean disInvokeEffects(final Environmental E)
	{
		if(E==null)
			return false;
		if(E instanceof Affectable)
		{
			final Affectable aE=(Affectable)E;
			final LinkedList<Ability> affects=new LinkedList<Ability>();
			for(int a=aE.numEffects()-1;a>=0;a--) // personal affects
			{
				final Ability A=aE.fetchEffect(a);
				if(A!=null)
					affects.add(A);
			}
			for(final Ability A : affects)
			{
				if(A.canBeUninvoked() && (!A.isAutoInvoked()))
					A.unInvoke();
			}
		}
		return !E.amDestroyed();
	}

	private void fixElectronicalItem(final Item E)
	{
		if(E instanceof Electronics)
		{
			CMLib.tech().fixItemTechLevel((Electronics)E, -1);
			if(E instanceof BoardableShip)
			{
				final Area A=((BoardableShip)E).getShipArea();
				for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if(R!=null)
					{
						for(final Enumeration<Item> i=R.items();i.hasMoreElements();)
						{
							final Item I=i.nextElement();
							if(I instanceof Electronics)
								CMLib.tech().fixItemTechLevel((Electronics)I, ((Electronics)E).techLevel());
						}
					}
				}
			}
		}
	}

	@Override
	public int processVariableEquipment(final MOB mob, final boolean isRejuv)
	{
		int newLastTickedDateTime=0;

		if(mob!=null)
		{
			final Room R=mob.location();
			if(R!=null)
			{
				for(int i=0;i<R.numInhabitants();i++)
				{
					final MOB M=R.fetchInhabitant(i);
					if((M!=null)&&(!M.isMonster())&&(CMSecurity.isAllowed(M,R,CMSecurity.SecFlag.CMDMOBS)))
					{
						newLastTickedDateTime=-1;
						break;
					}
				}
				if(newLastTickedDateTime==0)
				{
					final List<List<Item>> rivals=new ArrayList<List<Item>>(mob.numItems());
					for(int i=0;i<mob.numItems();i++)
					{
						final Item I=mob.getItem(i);
						if(I!=null)
						{
							this.fixElectronicalItem(I);
							final int rejuv = I.basePhyStats().rejuv();
							if(((rejuv>0)&&(rejuv!=PhyStats.NO_REJUV))
							||(rejuv == PhyStats.ONE_JUV))
							{
								List<Item> V=null;
								for(int r=0;r<rivals.size();r++)
								{
									final List<Item> V2=rivals.get(r);
									final Item I2=V2.get(0);
									if(I2.rawWornCode()==I.rawWornCode())
									{
										V=V2;
										break;
									}
								}
								if(V==null)
								{
									V=new ArrayList<Item>(1);
									rivals.add(V);
								}
								V.add(I);
							}
						}
					}
					for(int i=0;i<rivals.size();i++)
					{
						final List<Item> V=rivals.get(i);
						if((V.size()==1)||(V.get(0).rawWornCode()==0))
						{
							for(int r=0;r<V.size();r++)
							{
								final Item I=V.get(r);
								final int rejuv=I.basePhyStats().rejuv();
								if((CMLib.dice().rollPercentage()<rejuv)
								||((rejuv<0)&&(isRejuv)))
								{
									mob.delItem(I);
									I.destroy();
								}
								else
								{
									I.basePhyStats().setRejuv(PhyStats.NO_REJUV);
									I.phyStats().setRejuv(PhyStats.NO_REJUV);
								}
							}
						}
						else
						{
							int totalChance=0;
							for(int r=0;r<V.size();r++)
							{
								final Item I=V.get(r);
								if(I.basePhyStats().rejuv()>0)
									totalChance+=I.basePhyStats().rejuv();
							}
							final int chosenChance=CMLib.dice().roll(1,totalChance,0);
							totalChance=0;
							Item chosenI=null;
							for(int r=0;r<V.size();r++)
							{
								final Item I=V.get(r);
								final int rejuv=I.basePhyStats().rejuv();
								if(rejuv>0)
								{
									if(chosenChance<=(totalChance+rejuv))
									{
										chosenI=I;
										break;
									}
									totalChance+=rejuv;
								}
							}
							for(int r=0;r<V.size();r++)
							{
								final Item I=V.get(r);
								if((chosenI!=I)
								||((I.basePhyStats().rejuv()==PhyStats.ONE_JUV)&&(isRejuv)))
								{
									mob.delItem(I);
									I.destroy();
								}
								else
								{
									I.basePhyStats().setRejuv(PhyStats.NO_REJUV);
									I.phyStats().setRejuv(PhyStats.NO_REJUV);
								}
							}
						}
					}
					if(mob instanceof ShopKeeper)
					{
						final List<Item> rivalItems=new Vector<Item>();
						final CoffeeShop shop=(mob instanceof Librarian)?((Librarian)mob).getBaseLibrary():((ShopKeeper)mob).getShop();
						for(final Iterator<Environmental> i=shop.getStoreInventory();i.hasNext();)
						{
							final Environmental E=i.next();
							if(E instanceof Item)
							{
								final Item I=(Item)E;
								fixElectronicalItem(I);
								final int rejuv=I.basePhyStats().rejuv();
								if(((rejuv>0)&&(rejuv!=PhyStats.NO_REJUV))
								||(rejuv == PhyStats.ONE_JUV))
									rivalItems.add((Item)E);
							}
						}
						for(final Item I : rivalItems)
						{
							final int rejuv=I.basePhyStats().rejuv();
							if(CMLib.dice().rollPercentage()>rejuv)
							{
								shop.delAllStoreInventory(I);
								I.destroy();
							}
							else
							{
								I.basePhyStats().setRejuv(PhyStats.NO_REJUV);
								I.phyStats().setRejuv(PhyStats.NO_REJUV);
							}
						}
					}
					CMLib.beanCounter().getTotalAbsoluteNativeValue(mob); // converts mob.get-Money();
					if(mob.getMoneyVariation()>0.0)
						CMLib.beanCounter().addMoney(mob, Math.random()*mob.getMoneyVariation());
					else
					if(mob.getMoneyVariation()<0.0)
						CMLib.beanCounter().subtractMoney(mob, -(Math.random()*mob.getMoneyVariation()));
					mob.recoverPhyStats();
					mob.recoverCharStats();
					mob.recoverMaxState();
				}
			}
		}
		return newLastTickedDateTime;
	}

	@Override
	public void recursiveDropMOB(final MOB mob, final Room room, final Item thisContainer, boolean bodyFlag)
	{
		// caller is responsible for recovering any env
		// stat changes!

		if(CMLib.flags().isHidden(thisContainer))
			thisContainer.basePhyStats().setDisposition(thisContainer.basePhyStats().disposition()&((int)PhyStats.ALLMASK-PhyStats.IS_HIDDEN));
		mob.delItem(thisContainer);
		thisContainer.unWear();
		if(!bodyFlag)
			bodyFlag=(thisContainer instanceof DeadBody);
		if(bodyFlag)
		{
			room.addItem(thisContainer);
			thisContainer.setExpirationDate(0);
		}
		else
			room.addItem(thisContainer,ItemPossessor.Expire.Player_Drop);
		thisContainer.recoverPhyStats();
		boolean nothingDone=true;
		do
		{
			nothingDone=true;
			for(int i=0;i<mob.numItems();i++)
			{
				final Item thisItem=mob.getItem(i);
				if((thisItem!=null)&&(thisItem.container()==thisContainer))
				{
					recursiveDropMOB(mob,room,thisItem,bodyFlag);
					nothingDone=false;
					break;
				}
			}
		}
		while(!nothingDone);
	}

	@Override
	public MOB getMobPossessingAnother(final MOB mob)
	{
		if(mob==null)
			return null;
		MOB M=null;
		for(final Session S : CMLib.sessions().localOnlineIterable())
		{
			M=S.mob();
			if((M!=null)&&(M.soulMate()==mob))
				return M;
		}
		return null;
	}

	@Override
	public boolean armorCheck(final MOB mob, final Item I, final int allowedArmorLevel)
	{
		if((((I instanceof Armor)||(I instanceof Shield)))
		&&(!(I instanceof FalseLimb))
		&&(!(I instanceof BodyToken))
		&&(I.rawProperLocationBitmap()&CharClass.ARMOR_WEARMASK)>0)
		{
			DoubleFilterer.Result filterResult = DoubleFilterer.Result.NOTAPPLICABLE;
			for(final DoubleFilterer<Item> F : mob.charStats().getItemProficiencies())
			{
				filterResult = F.getFilterResult(I);
				if(filterResult != DoubleFilterer.Result.NOTAPPLICABLE)
				{
					return (filterResult == DoubleFilterer.Result.ALLOWED) ? true : false;
				}
			}
			boolean ok=true;
			switch(I.material()&RawMaterial.MATERIAL_MASK)
			{
			case RawMaterial.MATERIAL_LEATHER:
				if((allowedArmorLevel==CharClass.ARMOR_CLOTH)
				||(allowedArmorLevel==CharClass.ARMOR_VEGAN)
				||(allowedArmorLevel==CharClass.ARMOR_OREONLY)
				||(allowedArmorLevel==CharClass.ARMOR_METALONLY))
					ok=false;
				break;
			case RawMaterial.MATERIAL_METAL:
			case RawMaterial.MATERIAL_MITHRIL:
				if((allowedArmorLevel==CharClass.ARMOR_CLOTH)
				||(allowedArmorLevel==CharClass.ARMOR_LEATHER)
				||(allowedArmorLevel==CharClass.ARMOR_NONMETAL))
					ok=false;
				break;
			case RawMaterial.MATERIAL_ENERGY:
				if((allowedArmorLevel==CharClass.ARMOR_METALONLY)
				||(allowedArmorLevel==CharClass.ARMOR_OREONLY)
				||(allowedArmorLevel==CharClass.ARMOR_VEGAN))
				   return false;
				break;
			case RawMaterial.MATERIAL_CLOTH:
				if((allowedArmorLevel==CharClass.ARMOR_METALONLY)
				||(allowedArmorLevel==CharClass.ARMOR_OREONLY)
				||((allowedArmorLevel==CharClass.ARMOR_VEGAN)
				   &&((I.material()==RawMaterial.RESOURCE_HIDE)
					  ||(I.material()==RawMaterial.RESOURCE_FUR)
					  ||(I.material()==RawMaterial.RESOURCE_FEATHERS)
					  ||(I.material()==RawMaterial.RESOURCE_WOOL))))
					ok=false;
				break;
			case RawMaterial.MATERIAL_SYNTHETIC:
			case RawMaterial.MATERIAL_WOODEN:
				if((allowedArmorLevel==CharClass.ARMOR_CLOTH)
				||(allowedArmorLevel==CharClass.ARMOR_OREONLY)
				||(allowedArmorLevel==CharClass.ARMOR_LEATHER)
				||(allowedArmorLevel==CharClass.ARMOR_METALONLY))
					ok=false;
				break;
			case RawMaterial.MATERIAL_ROCK:
			case RawMaterial.MATERIAL_GLASS:
				if((allowedArmorLevel==CharClass.ARMOR_CLOTH)
				||(allowedArmorLevel==CharClass.ARMOR_LEATHER)
				||(allowedArmorLevel==CharClass.ARMOR_METALONLY))
					ok=false;
				break;
			case RawMaterial.MATERIAL_FLESH:
				if((allowedArmorLevel==CharClass.ARMOR_METALONLY)
				||(allowedArmorLevel==CharClass.ARMOR_VEGAN)
				||(allowedArmorLevel==CharClass.ARMOR_CLOTH)
				||(allowedArmorLevel==CharClass.ARMOR_OREONLY))
					ok=false;
				break;
			case RawMaterial.MATERIAL_GAS:
				if((allowedArmorLevel==CharClass.ARMOR_METALONLY)
				||(allowedArmorLevel==CharClass.ARMOR_LEATHER)
				||(allowedArmorLevel==CharClass.ARMOR_CLOTH)
				||(allowedArmorLevel==CharClass.ARMOR_OREONLY))
					ok=false;
				break;
			default:
				if((allowedArmorLevel==CharClass.ARMOR_METALONLY)
				||(allowedArmorLevel==CharClass.ARMOR_OREONLY))
					ok=false;
				break;
			}
			return ok;
		}
		return true;
	}

	@Override
	public boolean armorCheck(final MOB mob, final int allowedArmorLevel)
	{
		if(allowedArmorLevel==CharClass.ARMOR_ANY)
			return true;

		for(int i=0;i<mob.numItems();i++)
		{
			final Item I=mob.getItem(i);
			if((I!=null)
			&&(!I.amWearingAt(Wearable.IN_INVENTORY)))
			{
				final boolean ok=armorCheck(mob,I,allowedArmorLevel);
				if((!ok)&&((I.rawWornCode()&CharClass.ARMOR_WEARMASK)>0))
					return false;
			}
		}
		return true;
	}

	@Override
	public List<DeadBody> getDeadBodies(final Environmental E)
	{
		if(E instanceof DeadBody)
			return new XVector<DeadBody>((DeadBody)E);
		if(E instanceof Container)
		{
			final Vector<DeadBody> Bs=new Vector<DeadBody>();
			final List<Item> V=((Container)E).getContents();
			for(int v=0;v<V.size();v++)
				Bs.addAll(getDeadBodies(V.get(v)));
			return Bs;
		}
		return new Vector<DeadBody>(1);
	}

	@Override
	public boolean canBePlayerDestroyed(final MOB mob, final Item I, final boolean ignoreBodies, final boolean ignoreWeight)
	{
		if((!ignoreBodies)
		&&(I instanceof DeadBody)
		&&(((DeadBody)I).isPlayerCorpse()))
			return false;
		if((I instanceof PrivateProperty)
		&&((!((PrivateProperty)I).getOwnerName().equals(mob.Name()))
			||(mob.getClanRole(((PrivateProperty)I).getOwnerName())==null)))
			return false;
		if((!CMLib.flags().isGettable(I))
		||((I instanceof ClanItem)&&(mob.getClanRole(((ClanItem)I).clanID())==null))
		||((!ignoreWeight)&&(I.basePhyStats().weight() > mob.maxCarry()))
		||(CMath.bset(I.phyStats().sensesMask(), PhyStats.SENSE_ITEMNOWISH)))
			return false;
		if(I instanceof Container)
		{
			for (final Item I2 : ((Container)I).getContents())
			{
				if(!canBePlayerDestroyed(mob, I2,ignoreBodies, ignoreWeight))
					return false;
			}
		}
		return true;
	}

	protected TriadVector<Integer,Integer,MaskingLibrary.CompiledZMask> parseLootPolicyFor(final MOB mob)
	{
		if((mob==null)||(!mob.isMonster()))
			return noLootPolicy;
		final String lootPolicyStr=CMProps.getVar(CMProps.Str.ITEMLOOTPOLICY);
		if((lootPolicy==null)||(lastLootPolicyHash!=lootPolicyStr.hashCode()))
		{
			final List<String> lootPolicies=(!mob.isMonster())?new Vector<String>():CMParms.parseCommas(CMProps.getVar(CMProps.Str.ITEMLOOTPOLICY),true);
			final TriadVector<Integer,Integer,MaskingLibrary.CompiledZMask> policies=new TriadVector<Integer,Integer,MaskingLibrary.CompiledZMask>();
			for(int p=0;p<lootPolicies.size();p++)
			{
				String s=lootPolicies.get(p).toUpperCase().trim();
				if(s.length()==0)
					continue;
				MaskingLibrary.CompiledZMask compiledMask=null;
				final int maskDex=s.indexOf("MASK=");
				if(maskDex>=0)
				{
					s=s.substring(0,maskDex).trim();
					compiledMask=CMLib.masking().maskCompile(lootPolicies.get(p).substring(maskDex+5).trim());
				}
				else
					compiledMask=CMLib.masking().createEmptyMask();
				final Vector<String> parsed=CMParms.parse(s);
				int pct=100;
				for(int x=0;x<parsed.size();x++)
				{
					if(CMath.isInteger(parsed.elementAt(x)))
						pct=CMath.s_int(parsed.elementAt(x));
					else
					if(CMath.isPct(parsed.elementAt(x)))
						pct=(int)Math.round(CMath.s_pct(parsed.elementAt(x))*100.0);
				}
				int flags=0;
				if(parsed.contains("RUIN"))
					flags|=LOOTFLAG_RUIN;
				else
				if(parsed.contains("LOSS"))
					flags|=LOOTFLAG_LOSS;
				if(flags==0)
					flags|=LOOTFLAG_LOSS;
				if(parsed.contains("WORN"))
					flags|=LOOTFLAG_WORN;
				else
				if(parsed.contains("UNWORN"))
					flags|=LOOTFLAG_UNWORN;
				policies.addElement(Integer.valueOf(pct),Integer.valueOf(flags),compiledMask);
			}
			lootPolicy=policies;
			lastLootPolicyHash=lootPolicyStr.hashCode();
		}
		return lootPolicy;
	}

	@Override
	public void confirmWearability(final MOB mob)
	{
		if(mob==null)
			return;
		final Race R=mob.charStats().getMyRace();
		final long mobUnwearableBitmap=mob.charStats().getWearableRestrictionsBitmap();
		final DVector reWearSet=new DVector(2);
		Item item=null;
		for(int i=0;i<mob.numItems();i++)
		{
			item=mob.getItem(i);
			if((item!=null)
			&&(!item.amWearingAt(Wearable.IN_INVENTORY)))
			{
				final Long oldCode=Long.valueOf(item.rawWornCode());
				item.unWear();
				if(reWearSet.size()==0)
					reWearSet.addElement(item,oldCode);
				else
				{
					final short layer=(item instanceof Armor)?((Armor)item).getClothingLayer():0;
					int d=0;
					for(;d<reWearSet.size();d++)
					{
						if(reWearSet.elementAt(d,1) instanceof Armor)
						{
							if(((Armor)reWearSet.elementAt(d,1)).getClothingLayer()>layer)
								break;
						}
						else
						if(0>layer)
							break;
					}
					if(d>=reWearSet.size())
						reWearSet.addElement(item,oldCode);
					else
						reWearSet.insertElementAt(d,item,oldCode);
				}

			}
		}
		final CMMsg wearMsg=CMClass.getMsg(mob,item,null,CMMsg.NO_EFFECT,null,0,null,CMMsg.NO_EFFECT,null);
		final CMMsg removeMsg=CMClass.getMsg(mob,item,null,CMMsg.NO_EFFECT,null,CMMsg.TYP_REMOVE|CMMsg.MASK_ALWAYS,null,CMMsg.NO_EFFECT,null);
		for(int r=0;r<reWearSet.size();r++)
		{
			item=(Item)reWearSet.elementAt(r,1);
			final long oldCode=((Long)reWearSet.elementAt(r,2)).longValue();
			int msgCode=CMMsg.MSG_WEAR;
			if((oldCode&Wearable.WORN_WIELD)>0)
				msgCode=CMMsg.MSG_WIELD;
			else
			if((oldCode&Wearable.WORN_HELD)>0)
				msgCode=CMMsg.MSG_HOLD;
			wearMsg.setTarget(item);
			wearMsg.setTargetCode(msgCode);
			if((R.okMessage(mob,wearMsg))
			&&(item.okMessage(item,wearMsg))
			&&((mobUnwearableBitmap&oldCode)==0)
			&&(item.canWear(mob,oldCode)))
				item.wearAt(oldCode);
			else
			{
				removeMsg.setTarget(item);
				mob.executeMsg(mob, removeMsg);
				if(removeMsg.trailerMsgs() != null)
				{
					for(final CMMsg msg : removeMsg.trailerMsgs())
						mob.executeMsg(mob, msg);
					removeMsg.trailerMsgs().clear();
				}
				if(removeMsg.trailerRunnables() != null)
				{
					for(final Runnable run : removeMsg.trailerRunnables())
						run.run();
					removeMsg.trailerRunnables().clear();
				}
			}
		}
		// why wasn't that here before?
		mob.recoverPhyStats();
		mob.recoverCharStats();
		mob.recoverMaxState();
	}

	@Override
	public Item ruinItem(final Item I)
	{
		if(I==null)
			return null;
		if((CMath.bset(I.phyStats().disposition(),PhyStats.IS_UNSAVABLE))
		||(CMath.bset(I.phyStats().sensesMask(), PhyStats.SENSE_ITEMNORUIN))
		||(I instanceof Coins))
			return I;
		if(I.ID().equals("GenRuinedItem"))
			return I;
		final Item I2=CMClass.getItem("GenRuinedItem");
		I2.basePhyStats().setWeight(I.basePhyStats().weight());
		I2.setName(I.Name());
		I2.setDisplayText(I.displayText());
		I2.setDescription(I2.description());
		I2.recoverPhyStats();
		I2.setRawLogicalAnd(I.rawLogicalAnd());
		I2.setRawProperLocationBitmap(I.rawProperLocationBitmap());
		I2.setMaterial(I.material());
		String ruinDescAdder=null;
		switch(I2.material()&RawMaterial.MATERIAL_MASK)
		{
			case RawMaterial.MATERIAL_LEATHER:
			case RawMaterial.MATERIAL_CLOTH:
			case RawMaterial.MATERIAL_VEGETATION:
			case RawMaterial.MATERIAL_FLESH:
			case RawMaterial.MATERIAL_PAPER:
				ruinDescAdder=L("@x1  is torn and ruined beyond repair.",CMStrings.capitalizeFirstLetter(I2.name()));
				break;
			case RawMaterial.MATERIAL_METAL:
			case RawMaterial.MATERIAL_MITHRIL:
			case RawMaterial.MATERIAL_WOODEN:
				ruinDescAdder=L("@x1 is battered and ruined beyond repair.",CMStrings.capitalizeFirstLetter(I2.name()));
				break;
			case RawMaterial.MATERIAL_GLASS:
				ruinDescAdder=L("@x1 is shattered and ruined beyond repair.",CMStrings.capitalizeFirstLetter(I2.name()));
				break;
			case RawMaterial.MATERIAL_ROCK:
			case RawMaterial.MATERIAL_PRECIOUS:
			case RawMaterial.MATERIAL_SYNTHETIC:
				ruinDescAdder=L("@x1 is cracked and ruined beyond repair.",CMStrings.capitalizeFirstLetter(I2.name()));
				break;
			case RawMaterial.MATERIAL_UNKNOWN:
			case RawMaterial.MATERIAL_ENERGY:
			case RawMaterial.MATERIAL_GAS:
			case RawMaterial.MATERIAL_LIQUID:
			default:
				ruinDescAdder=L("@x1 is ruined beyond repair.",CMStrings.capitalizeFirstLetter(I2.name()));
				break;
		}
		I2.setDescription(CMStrings.endWithAPeriod(I2.description())+" "+ruinDescAdder);
		final String oldName=I2.Name();
		I2.setName(CMLib.english().insertUnColoredAdjective(I2.Name(),L("ruined")));
		final int x=I2.displayText().toUpperCase().indexOf(oldName.toUpperCase());
		I2.setBaseValue(0);
		if(x>=0)
			I2.setDisplayText(I2.displayText().substring(0,x)+I2.Name()+I2.displayText().substring(x+oldName.length()));
		return I2;
	}

	@Override
	public Item isRuinedLoot(final MOB mob, final Item I)
	{
		if(I==null)
			return null;
		if((CMath.bset(I.phyStats().disposition(),PhyStats.IS_UNSAVABLE))
		||(CMath.bset(I.phyStats().sensesMask(), PhyStats.SENSE_ITEMNORUIN))
		||(I instanceof Coins))
			return I;
		if(I.ID().equals("GenRuinedItem"))
			return I;
		final TriadVector<Integer,Integer,MaskingLibrary.CompiledZMask> policies=parseLootPolicyFor(mob);
		for(int d=0;d<policies.size();d++)
		{
			if((policies.get(d).third.entries().length>0)
			&&(!CMLib.masking().maskCheck(policies.get(d).third,I,true)))
				continue;
			if(CMLib.dice().rollPercentage()>policies.get(d).first.intValue())
				continue;
			final int flags=policies.get(d).second.intValue();
			if(CMath.bset(flags,LOOTFLAG_WORN)&&I.amWearingAt(Wearable.IN_INVENTORY))
				continue;
			else
			if(CMath.bset(flags,LOOTFLAG_UNWORN)&&(!I.amWearingAt(Wearable.IN_INVENTORY)))
				continue;
			if(CMath.bset(flags,LOOTFLAG_LOSS))
				return null;
			return ruinItem(I);
		}
		return I;
	}

	@Override
	public void reloadCharClasses(final CharClass oldC)
	{
		for(final Enumeration<Room> e=CMLib.map().rooms();e.hasMoreElements();)
		{
			final Room room=e.nextElement();
			for(int i=0;i<room.numInhabitants();i++)
			{
				final MOB M=room.fetchInhabitant(i);
				if(M==null)
					continue;
				for(int c=0;c<M.baseCharStats().numClasses();c++)
				{
					if(M.baseCharStats().getMyClass(c)==oldC)
					{
						M.baseCharStats().setMyClasses(M.baseCharStats().getMyClassesStr());
						break;
					}
				}
				for(int c=0;c<M.charStats().numClasses();c++)
				{
					if(M.charStats().getMyClass(c)==oldC)
					{
						M.charStats().setMyClasses(M.charStats().getMyClassesStr());
						break;
					}
				}
			}
			for(final Enumeration<MOB> e2=CMLib.players().players();e2.hasMoreElements();)
			{
				final MOB M=e2.nextElement();
				for(int c=0;c<M.baseCharStats().numClasses();c++)
				{
					if(M.baseCharStats().getMyClass(c)==oldC)
					{
						M.baseCharStats().setMyClasses(M.baseCharStats().getMyClassesStr());
						break;
					}
				}
				for(int c=0;c<M.charStats().numClasses();c++)
				{
					if(M.charStats().getMyClass(c)==oldC)
					{
						M.charStats().setMyClasses(M.charStats().getMyClassesStr());
						break;
					}
				}
			}
		}
	}

	@Override
	public void swapRaces(final Race newR, final Race oldR)
	{
		for(final Enumeration<Room> e=CMLib.map().rooms();e.hasMoreElements();)
		{
			final Room room=e.nextElement();
			for(int i=0;i<room.numInhabitants();i++)
			{
				final MOB M=room.fetchInhabitant(i);
				if(M==null)
					continue;
				if(M.baseCharStats().getMyRace()==oldR)
					M.baseCharStats().setMyRace(newR);
				if(M.charStats().getMyRace()==oldR)
				{
					M.charStats().setMyRace(newR);
					M.charStats().setWearableRestrictionsBitmap(M.charStats().getWearableRestrictionsBitmap()|M.charStats().getMyRace().forbiddenWornBits());
				}
			}
			for(final Enumeration<MOB> e2=CMLib.players().players();e2.hasMoreElements();)
			{
				final MOB M=e2.nextElement();
				if(M.baseCharStats().getMyRace()==oldR)
					M.baseCharStats().setMyRace(newR);
				if(M.charStats().getMyRace()==oldR)
				{
					M.charStats().setMyRace(newR);
					M.charStats().setWearableRestrictionsBitmap(M.charStats().getWearableRestrictionsBitmap()|M.charStats().getMyRace().forbiddenWornBits());
				}
			}
		}
	}

	@Override
	public boolean resurrect(final MOB tellMob, final Room corpseRoom, final DeadBody body, final int XPLevel)
	{
		final MOB rejuvedMOB=CMLib.players().getPlayerAllHosts(body.getMobName());

		if(rejuvedMOB!=null) // doing this here is helpful -- it can trigger a socket error.
			rejuvedMOB.tell(L("You are being resurrected."));

		if((rejuvedMOB!=null)&&(rejuvedMOB.session()!=null)&&(!rejuvedMOB.session().isStopped()))
		{
			if(rejuvedMOB.location()!=corpseRoom)
			{
				rejuvedMOB.location().showOthers(rejuvedMOB,rejuvedMOB.location(),CMMsg.MSG_LEAVE|CMMsg.MASK_ALWAYS,L("<S-NAME> disappears!"));
				corpseRoom.bringMobHere(rejuvedMOB,false);
			}
			Ability A=rejuvedMOB.fetchAbility("Prop_AstralSpirit");
			if(A!=null)
				rejuvedMOB.delAbility(A);
			A=rejuvedMOB.fetchEffect("Prop_AstralSpirit");
			if(A!=null)
				rejuvedMOB.delEffect(A);

			int it=0;
			while(it<rejuvedMOB.location().numItems())
			{
				final Item item=rejuvedMOB.location().getItem(it);
				if((item!=null)&&(item.container()==body))
				{
					final CMMsg msg2=CMClass.getMsg(rejuvedMOB,body,item,CMMsg.MSG_GET,null);
					rejuvedMOB.location().send(rejuvedMOB,msg2);
					final CMMsg msg3=CMClass.getMsg(rejuvedMOB,item,null,CMMsg.MSG_GET,null);
					rejuvedMOB.location().send(rejuvedMOB,msg3);
					it=0;
				}
				else
					it++;
			}
			body.delEffect(body.fetchEffect("Age")); // so misskids doesn't record it
			body.destroy();
			rejuvedMOB.basePhyStats().setDisposition(CMath.unsetb(rejuvedMOB.basePhyStats().disposition(),PhyStats.IS_SITTING|PhyStats.IS_CUSTOM));
			rejuvedMOB.phyStats().setDisposition(CMath.unsetb(rejuvedMOB.basePhyStats().disposition(),PhyStats.IS_SITTING|PhyStats.IS_CUSTOM));
			rejuvedMOB.location().show(rejuvedMOB,null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> get(s) up!"));
			corpseRoom.recoverRoomStats();
			final List<String> whatsToDo=CMParms.parseCommas(CMProps.get(rejuvedMOB.session()).getStr(CMProps.Str.PLAYERDEATH),true);
			int rejuv=rejuvedMOB.phyStats().rejuv();
			if((rejuv==0)||(rejuv==Integer.MAX_VALUE))
				rejuv=rejuvedMOB.phyStats().level();
			if(((!rejuvedMOB.isMonster())&&(rejuvedMOB.soulMate()==null)))
				rejuv=1;
			final double[] vars=new double[] { rejuvedMOB.phyStats().level(), rejuvedMOB.phyStats().level(), rejuv };
			for(int w=0;w<whatsToDo.size();w++)
			{
				final String whatToDo=whatsToDo.get(w);
				if(whatToDo.startsWith("UNL"))
					CMLib.leveler().level(rejuvedMOB);
				else
				if(whatToDo.startsWith("ASTR"))
				{
				}
				else
				if(whatToDo.startsWith("RETA"))
				{
				}
				else
				if (whatToDo.startsWith("PUR"))
				{
				}
				else
				if((whatToDo.trim().equals("0"))||(CMath.s_int(whatToDo)>0))
				{
					if(XPLevel>=0)
					{
						int expLost=(CMath.s_int(whatToDo)+(2*XPLevel))/2;
						expLost=CMLib.leveler().postExperience(rejuvedMOB,null,null,expLost,false);
						rejuvedMOB.tell(L("^*You regain @x1 experience points.^?^.",""+expLost));
					}
				}
				else
				if(whatToDo.length()<3)
					continue;
				else
				if(CMath.s_parseIntExpression(whatToDo,vars)>0)
				{
					final int xp=CMath.s_parseIntExpression(whatToDo,vars);
					int expLost=(xp+(2*XPLevel))/2;
					expLost=CMLib.leveler().postExperience(rejuvedMOB,null,null,expLost,false);
					rejuvedMOB.tell(L("^*You regain @x1 experience points.^?^.",""+expLost));
				}
				else
				if(XPLevel>=0)
				{
					double lvl=body.phyStats().level();
					for(int l=body.phyStats().level();l<rejuvedMOB.phyStats().level();l++)
						lvl=lvl/2.0;
					int expRestored=(int)Math.round(((100.0+(2.0*(XPLevel)))*lvl)/2.0);
					expRestored=CMLib.leveler().postExperience(rejuvedMOB,null,null,expRestored,false);
					rejuvedMOB.tell(L("^*You regain @x1 experience points.^?^.",""+expRestored));
				}
			}
			return true;
		}
		else
			corpseRoom.show(tellMob,body,CMMsg.MSG_OK_VISUAL,L("<T-NAME> twitch(es) for a moment, but the spirit is too far gone."));
		return false;
	}

	@Override
	public long[][] compileConditionalRange(final List<String> condV, final int numDigits, final int startOfRange, final int endOfRange)
	{
		final long[][] finalSet = new long[endOfRange - startOfRange + 1][];
		for(String cond : condV)
		{
			final Vector<String> V=CMParms.parse(cond.trim());
			if(V.size()<2)
				continue;
			final long[] vals=new long[numDigits];
			for(int i=0;i<numDigits;i++)
			{
				if(i+1<V.size())
					vals[i]=CMath.s_long(V.elementAt(i+1));
			}
			cond=V.firstElement().trim();
			int start=startOfRange;
			int finish=endOfRange;
			if(cond.startsWith("<="))
				finish=CMath.s_int(cond.substring(2).trim());
			else
			if(cond.startsWith(">="))
				start=CMath.s_int(cond.substring(2).trim());
			else
			if(cond.startsWith("=="))
			{
				start=CMath.s_int(cond.substring(2).trim());
				finish=start;
			}
			else
			if(cond.startsWith("="))
			{
				start=CMath.s_int(cond.substring(1).trim());
				finish=start;
			}
			else
			if(cond.startsWith(">"))
				start=CMath.s_int(cond.substring(1).trim())+1;
			else
			if(cond.startsWith("<"))
				finish=CMath.s_int(cond.substring(1).trim())-1;

			if(finish > endOfRange)
				finish = endOfRange;
			if((start>=startOfRange)&&(start<=finish))
			{
				for(int s=start;s<=finish;s++)
				{
					if(finalSet[s-startOfRange]==null)
						finalSet[s-startOfRange] = vals;
				}
			}
		}
		return finalSet;
	}

	@Override
	public List<Item> deepCopyOf(final Item oldItem)
	{
		final List<Item> items=new ArrayList<Item>(1);
		if(oldItem == null)
			return items;
		final Item newItem = (Item)oldItem.copyOf();
		items.add(newItem);
		if(newItem instanceof Container)
		{
			final Container newContainer=(Container)newItem;
			for(final Item oldContentItem : ((Container)oldItem).getContents())
			{
				final Item newContentItem;
				if(oldContentItem instanceof Container)
				{
					final List<Item> newContents=deepCopyOf(oldContentItem);
					newContentItem = newContents.get(0);
					items.addAll(newContents);
				}
				else
				{
					newContentItem = (Item)oldContentItem.copyOf();
					items.add(newContentItem);
				}
				newContentItem.setContainer(newContainer);
			}
		}
		return items;
	}

	@Override
	public String buildPrompt(final MOB mob, final String prompt)
	{
		final StringBuffer buf=new StringBuffer("\n\r");
		String promptUp=null;
		int c=0;
		while(c<prompt.length())
		{
			if((prompt.charAt(c)=='%')&&(c<(prompt.length()-1)))
			{
				switch(prompt.charAt(++c))
				{
				case '-':
					if(c<(prompt.length()-2))
					{
						if(promptUp==null)
							promptUp=prompt.toUpperCase();
						final String promptSub=promptUp.substring(c+1);
						final Wearable.CODES wcodes = Wearable.CODES.instance();
						boolean isFound=false;
						for(final long code : wcodes.all())
						{
							if(promptSub.startsWith(wcodes.nameup(code)))
							{
								c+=1+wcodes.nameup(code).length();
								final Item I=mob.fetchFirstWornItem(code);
								if(I!=null)
									buf.append(I.name());
								isFound=true;
								break;
							}
						}
						if(!isFound)
						{
							final CharStats.CODES ccodes = CharStats.CODES.instance();
							for(final int code : ccodes.all())
							{
								if(promptSub.startsWith(ccodes.name(code)))
								{
									c+=1+ccodes.name(code).length();
									buf.append(mob.charStats().getStat(code));
									isFound=true;
									break;
								}
							}
							if(!isFound)
							{
								for(final int code : ccodes.all())
								{
									if(promptSub.startsWith("BASE "+ccodes.name(code)))
									{
										buf.append(mob.baseCharStats().getStat(code));
										c+=6+ccodes.name(code).length();
										isFound=true;
										break;
									}
								}
								if((!isFound)&&(promptSub.startsWith("STINK"))&&(mob.playerStats()!=null))
									buf.append(CMath.toPct(mob.playerStats().getHygiene()/PlayerStats.HYGIENE_DELIMIT));
							}
						}
						if(!isFound)
						{
							for(final String s : mob.phyStats().getStatCodes())
							{
								if(promptSub.startsWith(s))
								{
									c+=1+s.length();
									buf.append(mob.phyStats().getStat(s));
									isFound=true;
									break;
								}
							}
							if(!isFound)
							{
								for(final String s : mob.basePhyStats().getStatCodes())
								{
									if(promptSub.startsWith("BASE "+s))
									{
										c+=6+s.length();
										buf.append(mob.basePhyStats().getStat(s));
										isFound=true;
										break;
									}
								}
							}
						}
						if(!isFound)
						{
							for(final String s : mob.curState().getStatCodes())
							{
								if(promptSub.startsWith(s))
								{
									c+=1+s.length();
									buf.append(mob.curState().getStat(s));
									isFound=true;
									break;
								}
							}
							if(!isFound)
							{
								for(final String s : mob.maxState().getStatCodes())
								{
									if(promptSub.startsWith("MAX "+s))
									{
										c+=5+s.length();
										buf.append(mob.maxState().getStat(s));
										isFound=true;
										break;
									}
								}
							}
							if(!isFound)
							{
								for(final String s : mob.baseState().getStatCodes())
								{
									if(promptSub.startsWith("BASE "+s))
									{
										c+=6+s.length();
										buf.append(mob.baseState().getStat(s));
										isFound=true;
										break;
									}
								}
							}
						}
					}
					break;
				case 'a':
				{
					buf.append(CMLib.factions().getRangePercent(CMLib.factions().getAlignmentID(),mob.fetchFaction(CMLib.factions().getAlignmentID()))+"%");
					c++;
					break;
				}
				case 'A':
				{
					final Faction.FRange FR = CMLib.factions().getRange(CMLib.factions().getAlignmentID(), mob.fetchFaction(CMLib.factions().getAlignmentID()));
					buf.append((FR != null) ? FR.name() : "" + mob.fetchFaction(CMLib.factions().getAlignmentID()));
					c++;
					break;
				}
				case 'B':
				{
					buf.append("\n\r");
					c++;
					break;
				}
				case 'c':
				{
					buf.append(mob.numItems());
					c++;
					break;
				}
				case 'C':
				{
					buf.append(mob.maxItems());
					c++;
					break;
				}
				case 'd':
				{
					final MOB victim = mob.getVictim();
					if ((mob.isInCombat()) && (victim != null))
						buf.append("" + mob.rangeToTarget());
					c++;
					break;
				}
				case 'D':
				{
					final Item I = mob.fetchWieldedItem();
					if ((I instanceof AmmunitionWeapon) && (((AmmunitionWeapon) I).requiresAmmunition()))
						buf.append("" + ((AmmunitionWeapon) I).ammunitionRemaining());
					c++;
					break;
				}
				case 'e':
				{
					final MOB victim = mob.getVictim();
					if ((mob.isInCombat()) && (victim != null) && (CMLib.flags().canBeSeenBy(victim, mob)))
						buf.append(victim.name(mob));
					c++;
					break;
				}
				case 'E':
				{
					final MOB victim = mob.getVictim();
					if ((mob.isInCombat()) && (victim != null) && (!victim.amDead()) && (CMLib.flags().canBeSeenBy(victim, mob)))
						buf.append(victim.healthText(mob) + "\n\r");
					c++;
					break;
				}
				case 'f':
				{
					if(c<(prompt.length()-3))
					{
						if(promptUp==null)
							promptUp=prompt.toUpperCase();
						final String promptSub=promptUp.substring(c+1);
						for(final Enumeration<Faction> f=CMLib.factions().factions();f.hasMoreElements();)
						{
							final Faction F=f.nextElement();
							if(promptSub.startsWith(F.factionID()))
							{
								c+=1+F.factionID().length();
								buf.append(CMLib.factions().getRangePercent(F.factionID(),mob.fetchFaction(F.factionID()))+"%");
								break;
							}
							if(promptSub.startsWith(F.upperName()))
							{
								c+=1+F.name().length();
								buf.append(CMLib.factions().getRangePercent(F.factionID(),mob.fetchFaction(F.factionID()))+"%");
								break;
							}
						}
					}
					break;
				}
				case 'F':
				{
					if(c<(prompt.length()-3))
					{
						if(promptUp==null)
							promptUp=prompt.toUpperCase();
						final String promptSub=promptUp.substring(c+1);
						for(final Enumeration<Faction> f=CMLib.factions().factions();f.hasMoreElements();)
						{
							final Faction F=f.nextElement();
							if(promptSub.startsWith(F.factionID()))
							{
								c+=1+F.factionID().length();
								final Faction.FRange FR = CMLib.factions().getRange(F.factionID(), mob.fetchFaction(F.factionID()));
								buf.append((FR != null) ? FR.name() : "" + mob.fetchFaction(CMLib.factions().getAlignmentID()));
								break;
							}
							if(promptSub.startsWith(F.upperName()))
							{
								c+=1+F.name().length();
								final Faction.FRange FR = CMLib.factions().getRange(F.factionID(), mob.fetchFaction(F.factionID()));
								buf.append((FR != null) ? FR.name() : "" + mob.fetchFaction(CMLib.factions().getAlignmentID()));
								break;
							}
						}
					}
					break;
				}
				case 'g':
				{
					buf.append((int) Math.round(Math.floor(CMLib.beanCounter().getTotalAbsoluteNativeValue(mob)
												/ CMLib.beanCounter().getLowestDenomination(CMLib.beanCounter().getCurrency(mob)))));
					c++;
					break;
				}
				case 'G':
				{
					buf.append(CMLib.beanCounter().nameCurrencyShort(mob, CMLib.beanCounter().getTotalAbsoluteNativeValue(mob)));
					c++;
					break;
				}
				case 'h':
				{
					buf.append("^<Hp^>" + mob.curState().getHitPoints() + "^</Hp^>");
					c++;
					break;
				}
				case 'H':
				{
					buf.append("^<MaxHp^>" + mob.maxState().getHitPoints() + "^</MaxHp^>");
					c++;
					break;
				}
				case 'I':
				{
					if ((CMLib.flags().isCloaked(mob)) && (((mob.phyStats().disposition() & PhyStats.IS_NOT_SEEN) != 0)))
						buf.append(L("Wizinvisible"));
					else
					if (CMLib.flags().isCloaked(mob))
						buf.append("Cloaked");
					else
					if (!CMLib.flags().isSeeable(mob))
						buf.append(L("Undetectable"));
					else
					if (CMLib.flags().isInvisible(mob) && CMLib.flags().isHidden(mob))
						buf.append(L("Hidden/Invisible"));
					else
					if (CMLib.flags().isInvisible(mob))
						buf.append("Invisible");
					else
					if (CMLib.flags().isHidden(mob))
						buf.append("Hidden");
					c++;
					break;
				}
				case 'K':
				case 'k':
				{
					MOB tank = mob;
					if ((tank.getVictim() != null)
					&& (tank.getVictim().getVictim() != null)
					&& (tank.getVictim().getVictim() != mob))
						tank = tank.getVictim().getVictim();
					if (((c + 1) < prompt.length()) && (tank != null))
					{
						switch (prompt.charAt(c + 1))
						{
						case 'h':
						{
							buf.append(tank.curState().getHitPoints());
							c++;
							break;
						}
						case 'H':
						{
							buf.append(tank.maxState().getHitPoints());
							c++;
							break;
						}
						case 'm':
						{
							buf.append(tank.curState().getMana());
							c++;
							break;
						}
						case 'M':
						{
							buf.append(tank.maxState().getMana());
							c++;
							break;
						}
						case 'v':
						{
							buf.append(tank.curState().getMovement());
							c++;
							break;
						}
						case 'V':
						{
							buf.append(tank.maxState().getMovement());
							c++;
							break;
						}
						case 'e':
						{
							buf.append(tank.name(mob));
							c++;
							break;
						}
						case 'E':
						{
							if ((mob.isInCombat()) && (CMLib.flags().canBeSeenBy(tank, mob)))
								buf.append(tank.healthText(mob) + "\n\r");
							c++;
							break;
						}
						}
					}
					c++;
					break;
				}
				case 'm':
				{
					buf.append("^<Mana^>" + mob.curState().getMana() + "^</Mana^>");
					c++;
					break;
				}
				case 'M':
				{
					buf.append("^<MaxMana^>" + mob.maxState().getMana() + "^</MaxMana^>");
					c++;
					break;
				}
				case 'p':
				{
					buf.append("^<Point^>" + Math.round(Math.floor(mob.actions())) + "^</Point^>");
					c++;
					break;
				}
				case 'P':
				{
					buf.append("^<MaxPoint^>" + Math.round(Math.floor(mob.phyStats().speed())) + "^</MaxPoint^>");
					c++;
					break;
				}
				case 'r':
				{
					if (mob.location() != null)
						buf.append(mob.location().displayText(mob));
					c++;
					break;
				}
				case 'R':
				{
					if ((mob.location() != null) && CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.SYSMSGS))
						buf.append(mob.location().roomID());
					c++;
					break;
				}
				case 't':
				{
					if (mob.location() != null)
						buf.append(CMStrings.capitalizeAndLower(mob.location().getArea().getTimeObj().getTODCode().getDesc().toLowerCase()));
					c++;
					break;
				}
				case 'T':
				{
					if (mob.location() != null)
						buf.append(mob.location().getArea().getTimeObj().getHourOfDay());
					c++;
					break;
				}
				case 'u':
				{
					final List<String> spellsOnCooldown=new ArrayList<String>(1);
					for(final Enumeration<Ability> a=mob.abilities();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						if(A!=null)
						{
							final AbilityMapper.CompoundingRule rule = CMLib.ableMapper().getCompoundingRule(mob, A);
							if((rule!=null)
							&&(rule.compoundingTicks()>0))
							{
								final int[] consumed=A.usageCost(mob,false);
								if(consumed != null)
								{
									final int[] timeCache;
									final int nowLSW = (int)(System.currentTimeMillis()&0x7FFFFFFF);
									final int[][] abilityUsageCache=mob.getAbilityUsageCache(A.ID());
									if(abilityUsageCache[Ability.CACHEINDEX_LASTTIME] == null)
										continue;
									timeCache = abilityUsageCache[Ability.CACHEINDEX_LASTTIME];
									if(timeCache[Ability.USAGEINDEX_TIMELSW]>nowLSW)
										continue;
									final int numTicksSinceLastCast=(int)((nowLSW-timeCache[Ability.USAGEINDEX_TIMELSW]) / CMProps.getTickMillis());
									if(numTicksSinceLastCast >= rule.compoundingTicks())
										continue;
									if(((consumed[Ability.USAGEINDEX_MANA])>0)
									||((consumed[Ability.USAGEINDEX_MOVEMENT])>0)
									||((consumed[Ability.USAGEINDEX_HITPOINTS])>0))
										spellsOnCooldown.add(A.Name());
								}
							}
						}
					}
					if(spellsOnCooldown.size()>0)
						buf.append(CMParms.toListString(spellsOnCooldown));
					c++;
					break;
				}
				case 'v':
				{
					buf.append("^<Move^>" + mob.curState().getMovement() + "^</Move^>");
					c++;
					break;
				}
				case 'V':
				{
					buf.append("^<MaxMove^>" + mob.maxState().getMovement() + "^</MaxMove^>");
					c++;
					break;
				}
				case 'w':
				{
					buf.append(mob.phyStats().weight());
					c++;
					break;
				}
				case 'W':
				{
					buf.append(mob.maxCarry());
					c++;
					break;
				}
				case 'x':
				{
					if((!CMSecurity.isDisabled(CMSecurity.DisFlag.EXPERIENCE))
					&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.SHOWXP))
					&&!mob.charStats().getCurrentClass().expless()
					&&!mob.charStats().getMyRace().expless())
						buf.append(mob.getExperience());
					c++;
					break;
				}
				case 'X':
				{
					if((!CMSecurity.isDisabled(CMSecurity.DisFlag.EXPERIENCE))
					&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.SHOWXP))
					&&!mob.charStats().getCurrentClass().expless()
					&&!mob.charStats().getMyRace().expless())
					{
						if (mob.getExpNeededLevel() == Integer.MAX_VALUE)
							buf.append("N/A");
						else
							buf.append(mob.getExpNeededLevel());
					}
					c++;
					break;
				}
				case 'y':
				{
					final List<Ability> As = CMLib.flags().domainAffects(mob, Ability.ACODE_COMMON_SKILL);
					if(As != null)
					{
						for(final Ability A : As)
						{
							final String pct=A.getStat("PCTREMAIN");
							if(pct.length()>0)
							{
								buf.append(pct);
							}
						}
					}
					c++;
					break;
				}
				case 'Y':
				{
					final List<Ability> As = CMLib.flags().domainAffects(mob, Ability.ACODE_COMMON_SKILL);
					if(As != null)
					{
						for(final Ability A : As)
						{
							final String tickUpStr=A.getStat("TICKUP");
							if(tickUpStr.length()>0)
							{
								long tr=A.expirationDate();
								if(A.invoker()!=null)
									tr=tr-(System.currentTimeMillis()-A.invoker().lastTickedDateTime());
								if(tr<Ability.TICKS_ALMOST_FOREVER)
									buf.append(CMLib.time().date2EllapsedTime(tr, TimeUnit.SECONDS, true));
							}
						}
					}
					c++;
					break;
				}
				case 'z':
				{
					if (mob.location() != null)
						buf.append(mob.location().getArea().name());
					c++;
					break;
				}
				case 'Z':
				{
					final Room R=mob.location();
					final Area A=(R!=null)?R.getArea():null;
					if(((c + 1) < prompt.length())
					&&(A!=null))
					{
						switch (prompt.charAt(c + 1))
						{
						case 'o':
						case 'O':
						{
							final SpaceObject O=CMLib.map().getSpaceObject(R,true);
							if ( O != null)
								buf.append(O.name());
							c+=2;
							break;
						}
						case 'l':
							buf.append(A.getAreaIStats()[Area.Stats.AVG_LEVEL.ordinal()]);
							c+=2;
							break;
						case 'L':
							buf.append(A.getAreaIStats()[Area.Stats.MED_LEVEL.ordinal()]);
							c+=2;
							break;
						case 'c':
							buf.append(A.getCurrency().length()==0?"Default":A.getCurrency());
							c+=2;
							break;
						case 'a':
							buf.append(A.getAreaIStats()[Area.Stats.AVG_ALIGNMENT.ordinal()]);
							c+=2;
							break;
						case 'A':
							buf.append(A.getAreaIStats()[Area.Stats.MED_ALIGNMENT.ordinal()]);
							c+=2;
							break;
						case 'n':
						{
							final Faction.FRange FR = CMLib.factions().getRange(CMLib.factions().getAlignmentID(), A.getAreaIStats()[Area.Stats.AVG_ALIGNMENT.ordinal()]);
							buf.append((FR==null)?"":FR.name());
							c+=2;
							break;
						}
						case 'N':
							final Faction.FRange FR = CMLib.factions().getRange(CMLib.factions().getAlignmentID(), A.getAreaIStats()[Area.Stats.MED_ALIGNMENT.ordinal()]);
							buf.append((FR==null)?"":FR.name());
							c+=2;
							break;
						default:
							c+=1;
							break;
						}
					}
					break;
				}
				case '@':
				{
					if (mob.location() != null)
						buf.append(mob.location().getArea().getClimateObj().weatherDescription(mob.location()));
					c++;
					break;
				}
				default:
				{
					buf.append("%" + prompt.charAt(c));
					c++;
					break;
				}
				}
			}
			else
				buf.append(prompt.charAt(c++));
		}
		return buf.toString();
	}

	protected String raceMixRuleCheck(String rule, final String urace1,final  String urace2)
	{
		if(rule.toUpperCase().startsWith(urace1))
		{
			rule=rule.substring(urace1.length()).trim();
			if(rule.startsWith("+"))
			{
				rule=rule.substring(1).trim();
				if(rule.toUpperCase().startsWith(urace2))
				{
					rule=rule.substring(urace2.length()).trim();
					if(rule.startsWith("="))
					{
						return rule.substring(1).trim();
					}
				}
			}
		}
		return "";
	}

	@Override
	public String getUnsubscribeURL(final String name)
	{
		if((name == null)||(name.length()==0))
			return "N/A";
		final String passwordOfAnyKind;
		final PlayerAccount acct = CMLib.players().getLoadAccount(name);
		if((acct != null))
			passwordOfAnyKind = acct.getPasswordStr();
		else
		{
			MOB M=CMLib.players().getLoadPlayer(name);
			if((M == null)
			&&(CMLib.players().playerExistsAllHosts(name)))
				M=CMLib.players().getPlayerAllHosts(name);
			if((M != null) && (M.playerStats()!=null))
				passwordOfAnyKind = M.playerStats().getPasswordStr();
			else
			if(CMLib.players().accountExistsAllHosts(name))
			{
				final PlayerAccount acct2=CMLib.players().getAccountAllHosts(name);
				if(acct2 != null)
					passwordOfAnyKind = acct2.getPasswordStr();
				else
					return "N/A";
			}
			else
				return "N/A";
		}
		final String hostPart = CMLib.host().geWebHostUrl();
		final String b64repeatedHash = CMLib.encoder().makeRepeatableHashString(CMStrings.capitalizeAndLower(name)+"_"+passwordOfAnyKind);
		try
		{
			return hostPart+"Unsubscribe?USER="+CMStrings.capitalizeAndLower(name)+"&UNSUBKEY="+URLEncoder.encode(b64repeatedHash, "UTF-8");
		}
		catch (final UnsupportedEncodingException e)
		{
			Log.errOut(e);
			return "[ERR]";
		}
	}

	@Override
	public List<Race> getConstituantRaces(final String raceID)
	{
		final Vector<Race> racesToBaseFrom=new Vector<Race>();
		final Race human=CMClass.getRace("Human");
		final Race halfling=CMClass.getRace("Halfling");
		if((raceID.length()>1)&&(!raceID.endsWith("Race"))&&(Character.isUpperCase(raceID.charAt(0))))
		{
			int lastStart=0;
			int c=1;
			while(c<=raceID.length())
			{
				if((c==raceID.length())||(Character.isUpperCase(raceID.charAt(c))))
				{
					if((lastStart==0)&&(c==raceID.length())&&(!raceID.endsWith("ling"))&&(!raceID.startsWith("Half")))
						break;
					final String remainder = raceID.substring(lastStart);
					for(final Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
					{
						final Race R3=r.nextElement();
						if((!R3.ID().equals(raceID))
						&&(!R3.isGeneric())
						&&(remainder.startsWith(R3.ID()))
						&&(R3.ID().length()>(c-lastStart)))
							c=lastStart+R3.ID().length();
					}
					final String partial=raceID.substring(lastStart,c);
					if(partial.equals("Half")
					&&(!racesToBaseFrom.contains(human)))
					{
						racesToBaseFrom.add(human);
						lastStart=c;
					}
					else
					{
						Race R2=CMClass.getRace(partial);
						if((R2!=null)&&(!R2.ID().equals(raceID)))
						{
							racesToBaseFrom.add(R2);
							lastStart=c;
						}
						else
						if(partial.endsWith("ling"))
						{
							if(!racesToBaseFrom.contains(halfling))
								racesToBaseFrom.add(halfling);
							lastStart=c;
							R2=CMClass.getRace(partial.substring(0,partial.length()-4));
							if(R2!=null)
								racesToBaseFrom.add(R2);
						}
					}
					if(c==raceID.length())
						break;
				}
				c++;
			}
		}
		return racesToBaseFrom;
	}

	@Override
	public PairList<Item, Long> getSeenEquipment(final MOB mob, final long wornMask)
	{
		long wornCode=0;
		Item thisItem=null;
		final PairList<Item, Long> seenEQ = new PairVector<Item, Long>();
		final Wearable.CODES codes = Wearable.CODES.instance();
		for(int l=0;l<codes.all_ordered().length;l++)
		{
			wornCode=codes.all_ordered()[l];
			if((wornMask < 0)
			||(((wornMask & wornCode)==0)) && (wornMask != 0))
				continue;
			final List<Item> wornHere=mob.fetchWornItems(wornCode,(short)(Short.MIN_VALUE+1),(short)0);
			int numLocations=mob.getWearPositions(wornCode);
			if(numLocations==0)
				numLocations=1;
			if(wornHere.size()>0)
			{
				final List<List<Item>> sets=new Vector<List<Item>>(numLocations);
				for(int i=0;i<numLocations;i++)
					sets.add(new Vector<Item>());
				Item I=null;
				Item I2=null;
				short layer=Short.MAX_VALUE;
				short layerAtt=0;
				short layer2=Short.MAX_VALUE;
				short layerAtt2=0;
				List<Item> set=null;
				for(int i=0;i<wornHere.size();i++)
				{
					I=wornHere.get(i);
					if(I.container()!=null)
						continue;
					if(I instanceof Armor)
					{
						layer=((Armor)I).getClothingLayer();
						layerAtt=((Armor)I).getLayerAttributes();
					}
					else
					{
						layer=0;
						layerAtt=0;
					}
					for(int s=0;s<sets.size();s++)
					{
						set=sets.get(s);
						if(set.size()==0)
						{
							set.add(I);
							break;
						}
						for(int s2=0;s2<set.size();s2++)
						{
							I2=set.get(s2);
							if(I2 instanceof Armor)
							{
								layer2=((Armor)I2).getClothingLayer();
								layerAtt2=((Armor)I2).getLayerAttributes();
							}
							else
							{
								layer2=0;
								layerAtt2=0;
							}
							if(layer2==layer)
							{
								if(((layerAtt&Armor.LAYERMASK_MULTIWEAR)>0)
								&&((layerAtt2&Armor.LAYERMASK_MULTIWEAR)>0))
									set.add(s2,I);
								break;
							}
							if(layer2>layer)
							{
								set.add(s2,I);
								break;
							}
						}
						if(set.contains(I))
							break;
						if(layer2<layer)
						{
							set.add(I);
							break;
						}
					}
					wornHere.clear();
					for(int s=0;s<sets.size();s++)
					{
						set=sets.get(s);
						int s2=set.size()-1;
						for(;s2>=0;s2--)
						{
							I2=set.get(s2);
							wornHere.add(I2);
							if((!(I2 instanceof Armor))
							||(!CMath.bset(((Armor)I2).getLayerAttributes(),Armor.LAYERMASK_SEETHROUGH)))
							{
								break;
							}
						}
					}
				}
				for(int i=0;i<wornHere.size();i++)
				{
					thisItem=wornHere.get(i);
					if((thisItem.container()==null)&&(thisItem.amWearingAt(wornCode)))
					{
						if(CMLib.flags().isSeeable(thisItem))
							seenEQ.add(thisItem, Long.valueOf(thisItem.rawWornCode()));
					}
				}
			}
		}
		return seenEQ;
	}

	@Override
	public Race getMixedRace(final String race1, final String race2, final boolean ignoreRules)
	{
		if(race1.indexOf(race2)>=0)
			return CMClass.getRace(race1);
		else
		if(race2.indexOf(race1)>=0)
			return CMClass.getRace(race2);

		if(!ignoreRules)
		{
			final String raceMixRules = CMProps.getVar(CMProps.Str.RACEMIXING);
			if(raceMixRules.trim().length()>0)
			{
				final List<String> rules=CMParms.parseCommas(raceMixRules, true);
				final String urace1=race1.toUpperCase();
				final String urace2=race2.toUpperCase();
				for(String rule : rules)
				{
					rule=rule.trim();
					if(rule.equalsIgnoreCase("FATHER"))
					{
						Race R=CMClass.getRace(race2);
						if(R==null)
							R=CMClass.findRace(race2);
						if(R!=null)
							return R;
					}
					else
					if(rule.equalsIgnoreCase("MOTHER"))
					{
						Race R=CMClass.getRace(race1);
						if(R==null)
							R=CMClass.findRace(race1);
						if(R!=null)
							return R;
					}
					else
					{
						String chk=raceMixRuleCheck(rule,urace1,urace2);
						if((chk==null)||(chk.length()==0))
							chk=raceMixRuleCheck(rule,urace2,urace1);
						if((chk!=null)&&(chk.length()>0))
						{
							final String raceID=CMStrings.replaceAll(chk, " ", "_");
							Race R=CMClass.getRace(raceID);
							if(R==null)
								R=CMClass.findRace(raceID);
							if((R!=null)&&(R.isGeneric()))
							{
								if(CMLib.database().isRaceExpired(raceID))
								{
									CMLib.database().DBDeleteRace(raceID);
									CMClass.delRace(R);
									R=null;
								}
							}
							if(R!=null)
								return R;
							else
							{
								final Race FIRSTR=CMClass.getRace(race1);
								final Race SECONDR=CMClass.getRace(race2);
								R=FIRSTR.mixRace(SECONDR,raceID,chk);
								if(R.isGeneric() && (!R.ID().equals(race1))&& (!R.ID().equals(race2)))
								{
									CMClass.addRace(R);
									CMLib.database().DBCreateRace(R.ID(),R.racialParms());
								}
								return R;
							}
						}
					}
				}
			}
		}
		Race R=null;
		if(race1.equalsIgnoreCase("Human")||race2.equalsIgnoreCase("Human"))
		{
			String halfRace=(race1.equalsIgnoreCase("Human")?race2:race1);
			R=CMClass.getRace(halfRace);
			if((R!=null)&&(!R.ID().toUpperCase().startsWith("HALF")))
			{
				halfRace="Half"+CMStrings.capitalizeAndLower(R.ID().toLowerCase());
				Race testR=CMClass.getRace(halfRace);
				if((testR!=null)&&(testR.isGeneric()))
				{
					if(CMLib.database().isRaceExpired(halfRace))
					{
						CMLib.database().DBDeleteRace(halfRace);
						CMClass.delRace(testR);
						testR=null;
					}
				}
				if(testR!=null)
					R=testR;
				else
				{
					R=R.mixRace(CMClass.getRace("Human"),halfRace,"Half "+CMStrings.capitalizeAndLower(R.name()));
					if(R.isGeneric() && (!R.ID().equals(race1))&& (!R.ID().equals(race2)))
					{
						CMClass.addRace(R);
						CMLib.database().DBCreateRace(R.ID(),R.racialParms());
					}
				}
			}
		}
		else
		if(race1.equalsIgnoreCase("Halfling")||race2.equalsIgnoreCase("Halfling"))
		{
			String halfRace=(race1.equalsIgnoreCase("Halfling")?race2:race1);
			R=CMClass.getRace(halfRace);
			if((R!=null)&&(!R.ID().endsWith("ling")))
			{
				halfRace=R.ID()+"ling";
				Race testR=CMClass.getRace(halfRace);
				if((testR!=null)&&(testR.isGeneric()))
				{
					if(CMLib.database().isRaceExpired(halfRace))
					{
						CMLib.database().DBDeleteRace(halfRace);
						CMClass.delRace(testR);
						testR=null;
					}
				}
				if(testR!=null)
					R=testR;
				else
				{
					R=R.mixRace(CMClass.getRace("Halfling"),halfRace,CMStrings.capitalizeAndLower(R.name())+"ling");
					if(R.isGeneric() && (!R.ID().equals(race1))&& (!R.ID().equals(race2)))
					{
						CMClass.addRace(R);
						CMLib.database().DBCreateRace(R.ID(),R.racialParms());
					}
				}
			}
		}
		else
		{
			String first=null;
			if(race1.length()==race2.length())
				first=(race1.compareToIgnoreCase(race2)<0)?race1:race2;
			else
			if(race1.length()>race2.length())
				first=race1;
			else
				first=race2;
			final String second=(first.equals(race1)?race2:race1);
			final String halfRace=(race1.compareToIgnoreCase(race2)<0)?race1+race2:race2+race1;
			Race testR=CMClass.getRace(halfRace);
			final Race FIRSTR=CMClass.getRace(first);
			final Race SECONDR=CMClass.getRace(second);
			if((testR!=null)&&(testR.isGeneric()))
			{
				if(CMLib.database().isRaceExpired(halfRace))
				{
					CMLib.database().DBDeleteRace(halfRace);
					CMClass.delRace(testR);
					testR=null;
				}
			}
			if(testR!=null)
				R=testR;
			else
			{
				R=FIRSTR.mixRace(SECONDR,halfRace,FIRSTR.name()+"-"+SECONDR.name());
				if(R.isGeneric() && (!R.ID().equals(race1))&& (!R.ID().equals(race2)))
				{
					CMClass.addRace(R);
					CMLib.database().DBCreateRace(R.ID(),R.racialParms());
				}
			}
		}
		return R;
	}
}

