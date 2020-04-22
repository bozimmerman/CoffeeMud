package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ShopKeeper.ViewType;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;

import java.util.*;

/*
   Copyright 2005-2020 Bo Zimmerman

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
public class CoffeeShops extends StdLibrary implements ShoppingLibrary
{
	@Override
	public String ID()
	{
		return "CoffeeShops";
	}

	@Override
	public ShopKeeper getShopKeeper(final Environmental E)
	{
		if(E==null)
			return null;
		if(E instanceof ShopKeeper)
			return (ShopKeeper)E;
		if(E instanceof Physical)
		{
			final Physical P=(Physical)E;
			for(final Enumeration<Ability> a=P.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if(A instanceof ShopKeeper)
					return (ShopKeeper)A;
			}
		}
		if(E instanceof MOB)
		{
			Item I=null;
			final MOB mob=(MOB)E;
			for(int i=0;i<mob.numItems();i++)
			{
				I=mob.getItem(i);
				if(I instanceof ShopKeeper)
					return (ShopKeeper)I;
				if(I!=null)
				{
					for(final Enumeration<Ability> a=I.effects();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						if(A instanceof ShopKeeper)
							return (ShopKeeper)A;
					}
				}
			}
		}
		return null;
	}

	@Override
	public List<Environmental> getAllShopkeepers(final Room here, final MOB notMOB)
	{
		final Vector<Environmental> V=new Vector<Environmental>();
		if(here!=null)
		{
			if(getShopKeeper(here)!=null)
				V.addElement(here);
			final Area A=here.getArea();
			if(getShopKeeper(A)!=null)
				V.addElement(A);
			final List<Area> V2=A.getParentsRecurse();
			for(int v2=0;v2<V2.size();v2++)
			{
				if(getShopKeeper(V2.get(v2))!=null)
					V.addElement(V2.get(v2));
			}

			for(int i=0;i<here.numInhabitants();i++)
			{
				final MOB thisMOB=here.fetchInhabitant(i);
				if((thisMOB!=null)
				&&(thisMOB!=notMOB)
				&&(getShopKeeper(thisMOB)!=null)
				&&((notMOB==null)||(CMLib.flags().canBeSeenBy(thisMOB,notMOB))))
					V.addElement(thisMOB);
			}
			for(int i=0;i<here.numItems();i++)
			{
				final Item thisItem=here.getItem(i);
				if((thisItem!=null)
				&&(thisItem!=notMOB)
				&&(getShopKeeper(thisItem)!=null)
				&&(!CMLib.flags().isGettable(thisItem))
				&&(thisItem.container()==null)
				&&((notMOB==null)||(CMLib.flags().canBeSeenBy(thisItem,notMOB))))
					V.addElement(thisItem);
			}
		}
		return V;
	}

	protected String getSellableElectronicsName(final MOB viewerM, final Electronics E)
	{
		String baseName=E.name(viewerM);
		final String[] marks=CMProps.getListFileStringList(CMProps.ListFile.TECH_LEVEL_NAMES);
		if(baseName.indexOf(E.getFinalManufacturer().name())<0)
			baseName+= "("+E.getFinalManufacturer().name()+")";
		if(marks.length>0)
			baseName+=" "+marks[E.techLevel()%marks.length];
		return baseName;
	}

	protected int plusOrMinus(int range, final int hash, final boolean lieBool)
	{
		if(range == 0)
			range=1;
		final int hashedRange = CMath.abs(hash % range);
		return ((lieBool ? 1 : -1) * (hashedRange/2));
	}

	protected int plus(int range, final int hash)
	{
		if(range == 0)
			range=1;
		final int hashedRange = CMath.abs(hash % range);
		return hashedRange/2;
	}

	@Override
	public String getViewDescription(final MOB viewerM, final Environmental E, final Set<ViewType> flags)
	{
		final StringBuilder str=new StringBuilder("");
		if(E==null)
			return str.toString();
		final boolean lie = flags.contains(ViewType.FALSE);
		final int lieHash = E.name().hashCode();
		int level = 1;
		if(!flags.contains(ViewType.BASIC))
			str.append(L("It is '@x1'.",E.name()));
		else
		{
			if(E instanceof Ability)
			{
				final StringBuilder text;
				if(lie)
				{
					final int qualifyingLevel = CMLib.ableMapper().lowestQualifyingLevel(E.ID());
					Ability A=null;
					for(int i=0;i<100 && (A==null);i++)
					{
						A=CMClass.getAbility(lieHash+i);
						if((A==null)
						|| (!CMLib.ableMapper().qualifiesByAnyCharClass(A.ID()))
						|| (CMLib.ableMapper().lowestQualifyingLevel(A.ID())<=qualifyingLevel))
							A=null;
					}
					if(A==null)
						A=(Ability)E;
					text = CMLib.help().getHelpText(A.ID(), viewerM, false);
				}
				else
					text = CMLib.help().getHelpText(E.ID(), viewerM, false);
				if((text != null)
				&&(text.length()>0))
					str.append(text);
			}
			if(E instanceof Physical)
			{
				level=((Physical)E).phyStats().level() - (lie?plus(((Physical)E).phyStats().level()/2,lieHash):0);
				str.append("\n\rLevel      : "+level);
				str.append("\n\rType       : ");
				if(E instanceof LandTitle)
					str.append(L("Title Document"));
				else
				{
					if(E instanceof Electronics)
						str.append(L("Electronic "));
					if(E instanceof BoardableShip)
						str.append(L("Vessel"));
					else
					if(E instanceof ClanItem)
						str.append(L(((ClanItem)E).getClanItemType().getDisplayName()));
					else
					if(E instanceof Weapon)
						str.append(L("Weapon"));
					else
					if(E instanceof Armor)
						str.append(L("Armor"));
					else
					if(E instanceof Rideable)
						str.append(L("Rideable"));
					else
					if(E instanceof Container)
						str.append(L("Container"));
					else
						str.append(L("Item"));
				}
			}
			if(E instanceof LandTitle)
			{
				final LandTitle T=(LandTitle)E;
				str.append(L("\n\rSize       : ")+L("@x1 room(s)",""+(T.getAllTitledRooms().size()
												  + (lie?plus(T.getAllTitledRooms().size(),lieHash):0))));
				final StringBuilder features = new StringBuilder("");
				if(T.allowsExpansionConstruction() || (lie && (((lieHash >> 30) % 2) == 0)))
					features.append(L(" expandable"));
				if(T.rentalProperty()  || (lie && (((lieHash >> 29) % 2) == 0)))
					features.append(L(" rental"));
				// this space intentionally left with dynamic string
				str.append(L("\n\rFeatures   :"+features.toString()));
			}
			else
			if(E instanceof Item)
			{
				final Item I=(Item)E;
				final int material;
				if(lie)
				{
					final List<Integer> valueChoices = new ArrayList<Integer>();
					final int currVal = RawMaterial.CODES.VALUE(I.material());
					for(final int possCode : RawMaterial.CODES.COMPOSE_RESOURCES(I.material() & RawMaterial.MATERIAL_MASK))
					{
						if(RawMaterial.CODES.VALUE(possCode) >= currVal)
							valueChoices.add(Integer.valueOf(possCode));
					}
					material=(valueChoices.size()>0)?(valueChoices.get(CMath.abs(lieHash % valueChoices.size()))).intValue():I.material();
				}
				else
					material = I.material();
				str.append(L("\n\rMaterial   : @x1",L(CMStrings.capitalizeAndLower(RawMaterial.CODES.NAME(material).toLowerCase()))));
				str.append(L("\n\rWeight     : @x1 pounds",""+(I.phyStats().weight()
															 + (lie?-plus(I.phyStats().weight(),lieHash):0))));
				if(I instanceof Electronics)
				{
					str.append(L("\n\rMake       : @x1",""+((Electronics)I).getFinalManufacturer().name()));
					str.append(L("\n\rType       : @x1",""+((Electronics)I).getTechType().getDisplayName()));
				}
				if(I instanceof Technical)
				{
					str.append(L("\n\rModel Num. : @x1",""+(((Technical)I).techLevel()
															+ (lie?plus(4,lieHash):0))));
				}
				if(I instanceof BoardableShip)
				{
					final Area A=((BoardableShip)I).getShipArea();
					if(A!=null)
					{
						str.append(L("\n\rRooms      : @x1",""+(A.numberOfProperIDedRooms()
																+ (lie?plus(A.numberOfProperIDedRooms(),lieHash):0))));
						final List<String> miscItems=new ArrayList<String>();
						for(final Enumeration<Room> r= A.getProperMap(); r.hasMoreElements();)
						{
							final Room R=r.nextElement();
							if(R==null)
								continue;
							for(final Enumeration<Item> i = R.items();i.hasMoreElements();)
							{
								final Item I2=i.nextElement();
								if(I2.displayText().length()>0)
								{
									if(I2 instanceof TechComponent)
									{
										str.append(L("\n\r"+
												CMStrings.padRight(((TechComponent)I2).getTechType().getShortDisplayName(),11)
												 +": @x1",getSellableElectronicsName(viewerM,(Electronics)I2)));
									}
									else
										miscItems.add(I2.name(viewerM));
								}
							}
						}
						if(miscItems.size()>0)
						{
							str.append(L("\n\rMisc Items : "));
							str.append(CMParms.toListString(miscItems));
						}
					}
				}
				else
				if(I instanceof Weapon)
				{
					final String handedNess;
					if((I.rawLogicalAnd() && ((I.rawProperLocationBitmap()&(Item.WORN_HELD|Item.WORN_WIELD))==(Item.WORN_HELD|Item.WORN_WIELD)))
					&& (lie?((lieHash >> 29) % 2) == 0:true))
						handedNess = L(" (2 handed)");
					else
						handedNess = "";
					final int weaponType =  ((Weapon)I).weaponDamageType();
					str.append(L("\n\rWeap. Type : @x1",L(CMStrings.capitalizeAndLower(Weapon.TYPE_DESCS[weaponType]))));
					final int weaponClass = ((Weapon)I).weaponClassification();
					str.append(L("\n\rWeap. Class: @x1",L(CMStrings.capitalizeAndLower(Weapon.CLASS_DESCS[weaponClass])))).append(handedNess);
				}
				else
				if(I instanceof Armor)
				{
					str.append(L("\n\rWear Info  : Worn on "));
					final Wearable.CODES codes = Wearable.CODES.instance();
					final List<String> locs = new ArrayList<String>();
					final long rawLocationBitmap = I.rawProperLocationBitmap();
					for(final long wornCode : codes.all())
					{
						if(wornCode != Wearable.IN_INVENTORY)
						{
							if(codes.name(wornCode).length()>0)
							{
								if((rawLocationBitmap&wornCode)==wornCode)
									locs.add(CMStrings.capitalizeAndLower(codes.name(wornCode)));
							}
						}
					}
					final boolean rawLogicalAnd = I.rawLogicalAnd() && (lie?(((lieHash >> 29) % 2) == 0):true);
					str.append(CMParms.combineWith(locs, L(rawLogicalAnd ? " and " : " or ")));
					if(I.phyStats().height()>0)
					{
						final Armor.SizeDeviation deviation=((Armor) I).getSizingDeviation(viewerM);
						if((deviation != Armor.SizeDeviation.FITS) && (lie?(((lieHash >> 28) % 2) == 0):true))
							str.append(L("\n\rSize       : ") + I.phyStats().height() +" ("+L(deviation.toString().toLowerCase().replace('_',' ')+")"));
					}
				}
			}
			str.append(L("\n\rDescription: @x1",E.description()));
		}
		if((flags.contains(ViewType.IDENTIFY))
		&&(E instanceof Item))
		{
			if((((Item)E).secretIdentity()!=null)
			&& (((Item)E).secretIdentity().length()>0))
			{
				String secretIdentity = ((Item)E).secretIdentity();
				if(lie)
				{
					secretIdentity = CMStrings.replaceWhole(secretIdentity, " "+((Item)E).phyStats().level(), " "+level);
					secretIdentity = CMStrings.replaceWhole(secretIdentity, ""+((Item)E).phyStats().armor(),
							""+(((Item)E).phyStats().armor()+plus(((Item)E).phyStats().armor(),lieHash) ));
					final int timsLevel=CMLib.itemBuilder().timsLevelCalculator((Item)E);
					if(timsLevel != ((Item)E).phyStats().level())
						secretIdentity = CMStrings.replaceWhole(secretIdentity, ""+timsLevel, ""+(timsLevel+plus(timsLevel,lieHash) ));
					if(((Item)E).phyStats().ability()>0)
						secretIdentity = CMStrings.replaceWhole(secretIdentity, ""+((Item)E).phyStats().ability(),
								""+(((Item)E).phyStats().ability()+plus(((Item)E).phyStats().ability(),lieHash) ));
					if(((Item)E).phyStats().attackAdjustment()>0)
						secretIdentity = CMStrings.replaceWhole(secretIdentity, ""+((Item)E).phyStats().attackAdjustment(),
								""+(((Item)E).phyStats().attackAdjustment()+plus(((Item)E).phyStats().attackAdjustment(),lieHash) ));
					if(((Item)E).phyStats().damage()>0)
						secretIdentity = CMStrings.replaceWhole(secretIdentity, ""+((Item)E).phyStats().damage(),
								""+(((Item)E).phyStats().damage()+plus(((Item)E).phyStats().damage(),lieHash) ));
				}
				str.append(L("\n\rSpecial    : @x1",secretIdentity));
			}
			else
			if(lie)
			{
				final StringBuilder addOn = new StringBuilder("");
				if(lie)
				{
					if(E instanceof Weapon)
						addOn.append("Bonus for the weilder: ");
					else
					if(E instanceof Armor)
						addOn.append("Bonus for the wearer: ");
					else
						addOn.append("Bonus for the owner: ");
					final List<String> bonuses = new XVector<String>(new String[] {"Attack +@x1", "Damage +@x1", "Armor +@x1", "Casts @x2", "Attack +@x1", "Damage +@x1", "Armor +@x1", "Casts @x2"});
					bonuses.addAll(Arrays.asList(Arrays.copyOf(PhyStats.CAN_SEE_DESCS,8)));
					final String bonus = bonuses.get(CMath.abs(lieHash % bonuses.size()));
					if(bonus.indexOf("@x1")>=0)
						addOn.append(L(bonus,""+(CMath.abs(lieHash % ((Item)E).phyStats().level()/2))));
					else
					if(bonus.indexOf("@x2")>=0)
					{
						Ability A=null;
						for(int i=0;i<100 && (A==null);i++)
						{
							A=CMClass.getAbility(lieHash+i);
							if((A==null) || (!CMLib.ableMapper().qualifiesByAnyCharClass(A.ID())))
								A=null;
						}
						if(A!=null)
							addOn.append(L(bonus,A.name()));
					}
					else
						addOn.append(L(bonus));
				}
				str.append(L("\n\rSpecial    : @x1",addOn.toString()));
			}
		}
		return str.toString();
	}

	protected Ability getTrainableAbility(final MOB teacher, final Ability A)
	{
		if((teacher==null)||(A==null))
			return A;
		Ability teachableA=teacher.fetchAbility(A.ID());
		if(teachableA==null)
		{
			teachableA=(Ability)A.copyOf();
			teacher.addAbility(teachableA);
		}
		teachableA.setProficiency(100);
		return teachableA;
	}

	protected boolean shownInInventory(final MOB seller, final MOB buyer, final Environmental product, final ShopKeeper shopKeeper)
	{
		if(CMSecurity.isAllowed(buyer,buyer.location(),CMSecurity.SecFlag.CMDMOBS))
			return true;
		if(seller == buyer)
			return true;
		if(product instanceof Item)
		{
			if(((Item)product).container()!=null)
				return false;
			if(((Item)product).phyStats().level()>buyer.phyStats().level())
				return false;
			if(!CMLib.flags().canBeSeenBy(product,buyer))
				return false;
		}
		if(product instanceof MOB)
		{
			if(((MOB)product).phyStats().level()>buyer.phyStats().level())
				return false;
		}
		if(product instanceof Ability)
		{
			if(shopKeeper.isSold(ShopKeeper.DEAL_TRAINER))
			{
				if(!CMLib.ableMapper().qualifiesByLevel(buyer, (Ability)product))
					return false;
			}
		}
		return true;
	}

	public double rawSpecificGoldPrice(final Environmental product, final CoffeeShop shop)
	{
		double price=0.0;
		if(product instanceof Item)
			price=((Item)product).value();
		else
		if(product instanceof Ability)
		{
			if(shop.isSold(ShopKeeper.DEAL_TRAINER))
				price=CMLib.ableMapper().lowestQualifyingLevel(product.ID())*100;
			else
				price=CMLib.ableMapper().lowestQualifyingLevel(product.ID())*75;
		}
		else
		if(product instanceof MOB)
		{
			final MOB M=(MOB)product;
			final Ability A=M.fetchEffect("Prop_Retainable");
			if(A!=null)
			{
				if(A.text().indexOf(';')<0)
				{
					if(CMath.isDouble(A.text()))
						price=CMath.s_double(A.text());
					else
						price=CMath.s_int(A.text());
				}
				else
				{
					final String s2=A.text().substring(0,A.text().indexOf(';'));
					if(CMath.isDouble(s2))
						price=CMath.s_double(s2);
					else
						price=CMath.s_int(s2);
				}
			}
			if(price==0.0)
				price=(25.0+M.phyStats().level())*M.phyStats().level();
		}
		else
			price=CMLib.ableMapper().lowestQualifyingLevel(product.ID())*25;
		return price;
	}

	@Override
	public double prejudiceValueFromPart(final MOB customer, final boolean pawnTo, String part)
	{
		final int x=part.indexOf('=');
		if(x<0)
			return 0.0;
		final String sellorby=part.substring(0,x);
		part=part.substring(x+1);
		if(pawnTo&&(!sellorby.trim().equalsIgnoreCase("SELL")))
			return 0.0;
		if((!pawnTo)&&(!sellorby.trim().equalsIgnoreCase("BUY")))
			return 0.0;
		if(part.trim().indexOf(' ')<0)
			return CMath.s_double(part.trim());
		final Vector<String> V=CMParms.parse(part.trim());
		double d=0.0;
		boolean yes=false;
		final List<String> VF=customer.fetchFactionRanges();
		final String align=CMLib.flags().getAlignmentName(customer);
		final String sex=customer.charStats().genderName();
		final String className=customer.charStats().displayClassName();
		final String raceName=customer.charStats().raceName();
		final String raceCatName=customer.charStats().getMyRace().racialCategory();
		final String displayClassName=customer.charStats().getCurrentClass().name(customer.charStats().getCurrentClassLevel());
		for(int v=0;v<V.size();v++)
		{
			final String bit = V.elementAt(v);
			if (CMath.s_double(bit) != 0.0)
				d = CMath.s_double(bit);
			if ((bit.equalsIgnoreCase(className))
			||(bit.equalsIgnoreCase(displayClassName))
			||(bit.equalsIgnoreCase(sex))
			||(bit.equalsIgnoreCase(raceCatName))
			||(bit.equalsIgnoreCase(raceName))
			||(bit.equalsIgnoreCase(align)))
			{
				yes = true;
				break;
			}
			for (int vf = 0; vf < VF.size(); vf++)
			{
				if (bit.equalsIgnoreCase(VF.get(vf)))
				{
					yes = true;
					break;
				}
			}
		}
		if(yes)
			return d;
		return 0.0;

	}

	@Override
	public double prejudiceFactor(final MOB customer, String factors, final boolean pawnTo)
	{
		factors=factors.toUpperCase();
		if(factors.length()==0)
		{
			factors=CMProps.getVar(CMProps.Str.PREJUDICE).trim();
			if(factors.length()==0)
				return 1.0;
		}
		if(factors.indexOf('=')<0)
		{
			if(CMath.s_double(factors)!=0.0)
				return CMath.s_double(factors);
			return 1.0;
		}
		int x=factors.indexOf(';');
		while(x>=0)
		{
			final String part=factors.substring(0,x).trim();
			factors=factors.substring(x+1).trim();
			final double d=prejudiceValueFromPart(customer,pawnTo,part);
			if(d!=0.0)
				return d;
			x=factors.indexOf(';');
		}
		final double d=prejudiceValueFromPart(customer,pawnTo,factors.trim());
		if(d!=0.0)
			return d;
		return 1.0;
	}

	protected double itemPriceFactor(final Environmental product, final Room R, final String[] priceFactors, final boolean pawnTo)
	{
		if(priceFactors.length==0)
			return 1.0;
		double factor=1.0;
		int x=0;
		String factorMask=null;
		ItemPossessor oldOwner=null;
		if(product instanceof Item)
		{
			oldOwner=((Item)product).owner();
			if(R!=null)
				((Item)product).setOwner(R);
		}
		for (final String priceFactor : priceFactors)
		{
			factorMask=priceFactor.trim();
			x=factorMask.indexOf(' ');
			if(x<0)
				continue;
			if(CMLib.masking().maskCheck(factorMask.substring(x+1).trim(),product,false))
				factor*=CMath.s_double(factorMask.substring(0,x).trim());
		}
		if(product instanceof Item)
			((Item)product).setOwner(oldOwner);
		if(factor!=0.0)
			return factor;
		return 1.0;
	}

	@Override
	public ShopKeeper.ShopPrice sellingPrice(final MOB sellerShopM,
											 final MOB buyerCustM,
											 final Environmental product,
											 final ShopKeeper shopKeeper,
											 final CoffeeShop shop,
											 final boolean includeSalesTax)
	{
		final ShopKeeper.ShopPrice val=new ShopKeeper.ShopPrice();
		if(product==null)
			return val;
		final int stockPrice=shop.stockPrice(product);
		if(stockPrice<=-100)
		{
			if(stockPrice<=-1000)
				val.experiencePrice=(stockPrice*-1)-1000;
			else
				val.questPointPrice=(stockPrice*-1)-100;
			return val;
		}
		if(stockPrice>=0)
			val.absoluteGoldPrice=stockPrice;
		else
			val.absoluteGoldPrice=rawSpecificGoldPrice(product,shop);

		if(buyerCustM==null)
		{
			if(val.absoluteGoldPrice>0.0)
				val.absoluteGoldPrice=CMLib.beanCounter().abbreviatedRePrice(sellerShopM,val.absoluteGoldPrice);
			return val;
		}

		double prejudiceFactor=prejudiceFactor(buyerCustM,shopKeeper.finalPrejudiceFactors(),false);
		final Room loc=CMLib.map().roomLocation(shopKeeper);
		prejudiceFactor*=itemPriceFactor(product,loc,shopKeeper.finalItemPricingAdjustments(),false);
		val.absoluteGoldPrice=CMath.mul(prejudiceFactor,val.absoluteGoldPrice);

		// the price is 200% at 0 charisma, and 100% at 35
		if(sellerShopM.isMonster() && (!CMLib.flags().isGolem(sellerShopM)))
		{
			final double buyerCha=buyerCustM.charStats().getStat(CharStats.STAT_CHARISMA);
			final double buyerMinCha = (buyerCha < 1) ? 1 : buyerCha;
			final double sellerWis=sellerShopM.charStats().getStat(CharStats.STAT_WISDOM);
			final double sellerMinWis = (sellerWis < 3) ? 3 : sellerWis;
			final double denom = (buyerMinCha + sellerMinWis) * 0.8;
			val.absoluteGoldPrice=(val.absoluteGoldPrice*2)-(val.absoluteGoldPrice*((buyerMinCha-sellerMinWis)/denom));
		}

		if(includeSalesTax)
		{
			final double salesTax=getSalesTax(sellerShopM.getStartRoom(),sellerShopM);
			val.absoluteGoldPrice+=((salesTax>0.0)?(CMath.mul(val.absoluteGoldPrice,CMath.div(salesTax,100.0))):0.0);
		}
		if(val.absoluteGoldPrice<=0.0)
			val.absoluteGoldPrice=1.0;
		else
			val.absoluteGoldPrice=CMLib.beanCounter().abbreviatedRePrice(sellerShopM,val.absoluteGoldPrice);

		// the magical aura discount for miscmagic (potions, anything else.. MUST be basePhyStats tho!
		if((CMath.bset(buyerCustM.basePhyStats().disposition(),PhyStats.IS_BONUS))
		&&(product instanceof MiscMagic)
		&&(val.absoluteGoldPrice>2.0))
			val.absoluteGoldPrice/=2;

		return val;
	}

	private final static String[] emptyStringArray = new String[0];

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String[] parseItemPricingAdjustments(final String factors)
	{
		if((factors == null) || (factors.trim().length() == 0))
			return emptyStringArray;
		Map<String,String[]> hashedPriceAdjustments = (Map)Resources.getResource("SYSTEM_HASHED_PRICINGADJUSTMENTS");
		if(hashedPriceAdjustments == null)
		{
			hashedPriceAdjustments = new Hashtable<String,String[]>();
			Resources.submitResource("SYSTEM_HASHED_PRICINGADJUSTMENTS", hashedPriceAdjustments);
		}

		String[] pricingAdjustments = hashedPriceAdjustments.get(factors);
		if(pricingAdjustments != null)
			return pricingAdjustments;
		pricingAdjustments = CMParms.parseSemicolons(factors,true).toArray(new String[0]);
		hashedPriceAdjustments.put(factors, pricingAdjustments);
		return pricingAdjustments;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String[] parsePrejudiceFactors(final String factors)
	{
		if((factors == null) || (factors.trim().length() == 0))
			return emptyStringArray;
		Map<String,String[]> hashedPrejudiceFactors = (Map)Resources.getResource("SYSTEM_HASHED_PREJUDICEFACTORS");
		if(hashedPrejudiceFactors == null)
		{
			hashedPrejudiceFactors = new Hashtable<String,String[]>();
			Resources.submitResource("SYSTEM_HASHED_PREJUDICEFACTORS", hashedPrejudiceFactors);
		}

		String[] prejudiceFactors = hashedPrejudiceFactors.get(factors);
		if(prejudiceFactors != null)
			return prejudiceFactors;
		prejudiceFactors = CMParms.parseSemicolons(factors,true).toArray(new String[0]);
		hashedPrejudiceFactors.put(factors, prejudiceFactors);
		return prejudiceFactors;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Pair<Double,String>[] alternateParseItemPricingAdjustments(final String factors)
	{
		if((factors == null) || (factors.trim().length() == 0))
			return new Pair[0];
		Map<String,Pair<Double,String>[]> hashedPriceAdjustments = (Map)Resources.getResource("SYSTEM_HASHED_PRICINGADJUSTMENTS");
		if(hashedPriceAdjustments == null)
		{
			hashedPriceAdjustments = new Hashtable<String,Pair<Double,String>[]>();
			Resources.submitResource("SYSTEM_HASHED_PRICINGADJUSTMENTS", hashedPriceAdjustments);
		}

		Pair<Double,String>[] pricingAdjustments = hashedPriceAdjustments.get(factors);
		if(pricingAdjustments != null)
			return pricingAdjustments;
		final List<String> semiParsed = CMParms.parseSemicolons(factors,true);
		final List<Pair<Double,String>> almostParsed = new ArrayList<Pair<Double,String>>();
		for(final String s : semiParsed)
		{
			final int x=s.indexOf(' ');
			if(x<0)
				continue;
			almostParsed.add(new Pair<Double,String>(Double.valueOf(CMath.s_double(s.substring(0,x).trim())),s.substring(x+1).trim()));
		}
		pricingAdjustments = almostParsed.toArray(new Pair[0]);
		hashedPriceAdjustments.put(factors, pricingAdjustments);
		return pricingAdjustments;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Pair<Long,TimeClock.TimePeriod> parseBudget(final String budget)
	{
		if((budget == null) || (budget.trim().length() == 0))
			return null;
		else
		{
			Map<String,Pair<Long,TimeClock.TimePeriod>> hashedBudgets = (Map)Resources.getResource("SYSTEM_PARSED_BUDGETS");
			if(hashedBudgets == null)
			{
				hashedBudgets = new Hashtable<String,Pair<Long,TimeClock.TimePeriod>>();
				Resources.submitResource("SYSTEM_PARSED_BUDGETS", hashedBudgets);
			}
			Pair<Long,TimeClock.TimePeriod> budgetVals = hashedBudgets.get(budget);
			if(budgetVals != null)
				return budgetVals;
			budgetVals = new Pair<Long,TimeClock.TimePeriod>(Long.valueOf(Long.MAX_VALUE / 2), TimePeriod.HOUR);
			final Vector<String> V = CMParms.parse(budget.trim().toUpperCase());
			if (V.size() > 0)
			{
				if (V.firstElement().equals("0"))
					budgetVals.first = Long.valueOf(0);
				else
				{
					budgetVals.first = Long.valueOf(CMath.s_long(V.firstElement()));
					if (budgetVals.first.longValue() == 0)
						budgetVals.first = Long.valueOf(Long.MAX_VALUE / 2);
				}
				if (V.size() > 1)
				{
					final String s = V.lastElement().toUpperCase();
					budgetVals.second = (TimePeriod)CMath.s_valueOf(TimePeriod.class, s);
					if((budgetVals.second == null)&&(s.endsWith("S")))
						budgetVals.second = (TimePeriod)CMath.s_valueOf(TimePeriod.class, s.substring(0,s.length()-1));
					if(budgetVals.second == null)
						budgetVals.second = TimePeriod.HOUR;
				}
			}
			hashedBudgets.put(budget, budgetVals);
			return budgetVals;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public double[] parseDevalueRate(final String factors)
	{
		if((factors == null) || (factors.trim().length() == 0))
			return null;
		else
		{
			Map<String,double[]> hashedValueRates = (Map)Resources.getResource("SYSTEM_PARSED_DEVALUE_RATES");
			if(hashedValueRates == null)
			{
				hashedValueRates = new Hashtable<String,double[]>();
				Resources.submitResource("SYSTEM_PARSED_DEVALUE_RATES", hashedValueRates);
			}
			double[] devalueRate = hashedValueRates.get(factors);
			if(devalueRate != null)
				return devalueRate;

			final Vector<String> V=CMParms.parse(factors.trim());
			if(V.size()==1)
			{
				final double rate = CMath.div(CMath.s_double(V.firstElement()),100.0);
				devalueRate = new double[] { rate , rate };
			}
			else
			{
				devalueRate = new double[] { CMath.s_pct(V.firstElement()) , CMath.s_pct(V.lastElement()) };
			}
			hashedValueRates.put(factors, devalueRate);
			return devalueRate;
		}
	}

	protected double getStockSizeDevaluation(final ShopKeeper shop, final Environmental product, final double number)
	{
		int num=shop.getShop().numberInStock(product);
		num += (int)Math.round(Math.floor(number/2.0));
		if(num<=0)
			return 0.0;
		final double[] rates=shop.finalDevalueRate();
		if(rates == null)
			return 0.0;
		double rate=(product instanceof RawMaterial)?rates[1]:rates[0];
		if(rate<=0.0)
			return 0.0;
		rate=rate*num;
		if(rate>1.0)
			rate=1.0;
		if(rate<0.0)
			rate=0.0;
		return rate;
	}

	protected boolean isLotTooLarge(final ShopKeeper shop, final Environmental product)
	{
		final double number = this.getProductCount(product);
		if(number <= 1.0)
			return false;
		final double[] rates=shop.finalDevalueRate();
		if(rates == null)
			return false;
		final double rate=(product instanceof RawMaterial)?rates[1]:rates[0];
		if(rate<=0.0)
			return false;
		final int baseNum=shop.getShop().numberInStock(product);
		final int num = baseNum + (int)Math.round(Math.floor(number/2.0));
		if(num<=0)
			return false;
		final double baseRateAdj = rate * baseNum;
		if(baseRateAdj >= .95)
			return false;
		final double totalRateAdj = rate * num;
		if(totalRateAdj >= .95)
			return true;
		return false;
	}

	protected double getProductCount(final Environmental product)
	{
		double number=1.0;
		if(product instanceof PackagedItems)
			number=((PackagedItems)product).numberOfItemsInPackage();
		else
		if(product instanceof RawMaterial)
			number = ((RawMaterial)product).basePhyStats().weight();
		return number;
	}

	@Override
	public ShopKeeper.ShopPrice pawningPrice(final MOB buyerShopM,
											 final MOB sellerCustM,
											 Environmental product,
											 final ShopKeeper shopKeeper,
											 final CoffeeShop shop)
	{
		final double number=getProductCount(product);
		final ShopKeeper.ShopPrice val=new ShopKeeper.ShopPrice();
		try
		{
			if(product instanceof PackagedItems)
				product=((PackagedItems)product).peekFirstItem();
			else
			if(product instanceof RawMaterial)
			{
				product = (Environmental)product.copyOf();
				((RawMaterial) product).basePhyStats().setWeight(1);
				((RawMaterial) product).phyStats().setWeight(1);
				final int baseValue = ((RawMaterial) product).baseGoldValue();
				if(baseValue > number)
					((RawMaterial) product).setBaseValue( (int)Math.round(baseValue / number));
				else
				if(baseValue > 0)
					((RawMaterial) product).setBaseValue( 1);
			}
			if(product==null)
				return val;
			final int stockPrice=shop.stockPrice(product);
			if(stockPrice<=-100)
			{
				return val;
			}

			if(stockPrice>=0.0)
				val.absoluteGoldPrice=stockPrice;
			else
				val.absoluteGoldPrice=rawSpecificGoldPrice(product,shop);

			if(sellerCustM==null)
			{
				val.absoluteGoldPrice *= number;
				return val;
			}

			double prejudiceFactor=prejudiceFactor(sellerCustM,shopKeeper.finalPrejudiceFactors(),true);
			final Room loc=CMLib.map().roomLocation(shopKeeper);
			prejudiceFactor*=itemPriceFactor(product,loc,shopKeeper.finalItemPricingAdjustments(),true);
			val.absoluteGoldPrice=CMath.mul(prejudiceFactor,val.absoluteGoldPrice);

			double buyPrice=val.absoluteGoldPrice;
			if(buyerShopM.isMonster() && (!CMLib.flags().isGolem(buyerShopM)))
			{
				final double sellerCha=sellerCustM.charStats().getStat(CharStats.STAT_CHARISMA);
				final double sellerMinCha = (sellerCha < 1) ? 1 : sellerCha;
				final double buyerWis=buyerShopM.charStats().getStat(CharStats.STAT_WISDOM);
				final double buyerMinWis = (buyerWis < 3) ? 3 : buyerWis;
				final double denom = (sellerMinCha + buyerMinWis) * 0.8;
				buyPrice=(buyPrice/2)+((buyPrice/2)*((sellerMinCha-buyerMinWis)/denom));

			}
			if(!(product instanceof Ability))
				buyPrice=CMath.mul(buyPrice,1.0-getStockSizeDevaluation(shopKeeper,product,number));

			final double sellPrice=sellingPrice(buyerShopM,sellerCustM,product,shopKeeper,shop, false).absoluteGoldPrice;

			if(buyPrice>sellPrice)
				val.absoluteGoldPrice=sellPrice;
			else
				val.absoluteGoldPrice=buyPrice;

			val.absoluteGoldPrice *= number;

			if(val.absoluteGoldPrice<=0.0)
				val.absoluteGoldPrice=1.0;
		}
		finally
		{
			if((number > 1.0)&&(product!=null))
				product.destroy();
		}
		return val;
	}

	@Override
	public double getSalesTax(final Room homeRoom, final MOB seller)
	{
		if((seller==null)||(homeRoom==null))
			return 0.0;
		final Law theLaw=CMLib.law().getTheLaw(homeRoom,seller);
		if(theLaw!=null)
		{
			final String taxs=(String)theLaw.taxLaws().get("SALESTAX");
			if(taxs!=null)
				return CMath.s_double(taxs);
		}
		return 0.0;

	}

	@Override
	public boolean standardSellEvaluation(final MOB buyerShopM,
										  final MOB sellerCustM,
										  final Environmental product,
										  final ShopKeeper shop,
										  final double maxToPay,
										  final double maxEverPaid,
										  final boolean sellNotValue)
	{
		if((product!=null)
		&&(shop.doISellThis(product))
		&&(!(product instanceof Coins)))
		{
			final Room shopRoom=buyerShopM.location();
			if(shopRoom!=null)
			{
				int medianLevel=shopRoom.getArea().getPlayerLevel();
				if(medianLevel==0)
					medianLevel=shopRoom.getArea().getAreaIStats()[Area.Stats.MED_LEVEL.ordinal()];
				if(medianLevel>0)
				{
					final String range=CMParms.getParmStr(shop.finalPrejudiceFactors(),"RANGE","0");
					int rangeI=0;
					if((range.endsWith("%"))&&(CMath.isInteger(range.substring(0,range.length()-1))))
					{
						rangeI=CMath.s_int(range.substring(0,range.length()-1));
						rangeI=(int)Math.round(CMath.mul(medianLevel,CMath.div(rangeI,100.0)));
					}
					else
					if(CMath.isInteger(range))
						rangeI=CMath.s_int(range);
					if((rangeI>0)
					&&(product instanceof Physical)
					&&((((Physical)product).phyStats().level()>(medianLevel+rangeI))
						||(((Physical)product).phyStats().level()<(medianLevel-rangeI))))
					{
						CMLib.commands().postSay(buyerShopM,sellerCustM,L("I'm sorry, that's out of my level range."),true,false);
						return false;
					}
				}
			}
			if((product instanceof Item)
			&& (!CMLib.law().mayOwnThisItem(sellerCustM, (Item)product))
			&& ((!CMLib.flags().isEvil(buyerShopM))
				||(CMLib.flags().isLawful(buyerShopM))))
			{
				CMLib.commands().postSay(buyerShopM,sellerCustM,L("I don't buy stolen goods."),true,false);
				return false;
			}
			final double yourValue=pawningPrice(buyerShopM,sellerCustM,product,shop, shop.getShop()).absoluteGoldPrice;
			if(yourValue<2)
			{
				if(!isLotTooLarge(shop, product))
					CMLib.commands().postSay(buyerShopM,sellerCustM,L("I'm not interested."),true,false);
				else
					CMLib.commands().postSay(buyerShopM,sellerCustM,L("I'm not interested in the whole lot, but maybe a smaller count..."),true,false);
				return false;
			}
			if((product instanceof Physical)&&CMLib.flags().isEnspelled((Physical)product) || CMLib.flags().isOnFire((Physical)product))
			{
				CMLib.commands().postSay(buyerShopM, sellerCustM, L("I won't buy that in it's present state."), true, false);
				return false;
			}
			if((sellNotValue)&&(yourValue>maxToPay))
			{
				if(yourValue>maxEverPaid)
					CMLib.commands().postSay(buyerShopM,sellerCustM,L("That's way out of my price range! Try AUCTIONing it."),true,false);
				else
					CMLib.commands().postSay(buyerShopM,sellerCustM,L("Sorry, I can't afford that right now.  Try back later."),true,false);
				return false;
			}
			if(product instanceof Ability)
			{
				CMLib.commands().postSay(buyerShopM,sellerCustM,L("I'm not interested."),true,false);
				return false;
			}
			if((product instanceof Container)&&(((Container)product).hasALock()))
			{
				boolean found=false;
				final List<Item> V=((Container)product).getContents();
				for(int i=0;i<V.size();i++)
				{
					final Item I=V.get(i);
					if((I instanceof DoorKey)
					&&(((DoorKey)I).getKey().equals(((Container)product).keyName())))
						found=true;
					else
					if(CMLib.flags().isEnspelled(I) || CMLib.flags().isOnFire(I))
					{
						CMLib.commands().postSay(buyerShopM, sellerCustM, L("I won't buy the contents of that in it's present state."), true, false);
						return false;
					}
					else
					if((!CMLib.law().mayOwnThisItem(sellerCustM, I))
					&& (!CMLib.flags().isEvil(buyerShopM)))
					{
						CMLib.commands().postSay(buyerShopM,sellerCustM,L("I don't buy stolen goods."),true,false);
						return false;
					}
				}
				if(!found)
				{
					CMLib.commands().postSay(buyerShopM,sellerCustM,L("I won't buy that back unless you put the key in it."),true,false);
					return false;
				}
			}
			if((product instanceof Item)&&(sellerCustM.isMine(product)))
			{
				final CMMsg msg2=CMClass.getMsg(sellerCustM,product,CMMsg.MSG_DROP,null);
				if(!sellerCustM.location().okMessage(sellerCustM,msg2))
					return false;
			}
			return true;
		}
		CMLib.commands().postSay(buyerShopM,sellerCustM,L("I'm sorry, I'm not buying those."),true,false);
		return false;
	}

	@Override
	public boolean standardBuyEvaluation(final MOB sellerShopM,
										 final MOB buyerCustM,
										 final Environmental product,
										 final ShopKeeper shop,
										 final boolean buyNotView)
	{
		if((product!=null)
		&&(shop.getShop().doIHaveThisInStock("$"+product.Name()+"$",buyerCustM)))
		{
			if(buyNotView)
			{
				final ShopKeeper.ShopPrice price=sellingPrice(sellerShopM,buyerCustM,product,shop,shop.getShop(), true);
				if((price.experiencePrice>0)&&(price.experiencePrice>buyerCustM.getExperience()))
				{
					CMLib.commands().postSay(sellerShopM,buyerCustM,L("You aren't experienced enough to buy @x1.",product.name()),false,false);
					return false;
				}
				if((price.questPointPrice>0)&&(price.questPointPrice>buyerCustM.getQuestPoint()))
				{
					CMLib.commands().postSay(sellerShopM,buyerCustM,L("You don't have enough quest points to buy @x1.",product.name()),false,false);
					return false;
				}
				if((price.absoluteGoldPrice>0.0)
				&&(price.absoluteGoldPrice>CMLib.beanCounter().getTotalAbsoluteShopKeepersValue(buyerCustM,sellerShopM)))
				{
					CMLib.commands().postSay(sellerShopM,buyerCustM,L("You can't afford to buy @x1.",product.name()),false,false);
					return false;
				}
			}
			if(product instanceof Item)
			{
				if(((Item)product).phyStats().level()>buyerCustM.phyStats().level())
				{
					CMLib.commands().postSay(sellerShopM,buyerCustM,L("That's too advanced for you, I'm afraid."),true,false);
					return false;
				}
			}
			if((product instanceof PrivateProperty)
			&&((shop.isSold(ShopKeeper.DEAL_CLANDSELLER))||(shop.isSold(ShopKeeper.DEAL_CSHIPSELLER))))
			{
				final Pair<Clan,Integer> clanPair=CMLib.clans().findPrivilegedClan(buyerCustM, Clan.Function.PROPERTY_OWNER);
				if(clanPair==null)
				{
					if(!buyerCustM.clans().iterator().hasNext())
						CMLib.commands().postSay(sellerShopM,buyerCustM,L("I only sell to clans."),true,false);
					else
					if(!buyerCustM.isMonster())
						CMLib.commands().postSay(sellerShopM,buyerCustM,L("You are not authorized by your clan to handle property."),true,false);
					return false;
				}
			}
			if(product instanceof MOB)
			{
				if(buyerCustM.totalFollowers()>=buyerCustM.maxFollowers())
				{
					CMLib.commands().postSay(sellerShopM,buyerCustM,L("You can't accept any more followers."),true,false);
					return false;
				}
				if((CMProps.getIntVar(CMProps.Int.FOLLOWLEVELDIFF)>0)
				&&(!CMSecurity.isAllowed(sellerShopM,sellerShopM.location(),CMSecurity.SecFlag.ORDER))
				&&(!CMSecurity.isAllowed(buyerCustM,buyerCustM.location(),CMSecurity.SecFlag.ORDER)))
				{
					if(sellerShopM.phyStats().level() > (buyerCustM.phyStats().level() + CMProps.getIntVar(CMProps.Int.FOLLOWLEVELDIFF)))
					{
						buyerCustM.tell(L("@x1 is too advanced for you.",product.name()));
						return false;
					}
					if(sellerShopM.phyStats().level() < (buyerCustM.phyStats().level() - CMProps.getIntVar(CMProps.Int.FOLLOWLEVELDIFF)))
					{
						buyerCustM.tell(L("@x1 is too inexperienced for you.",product.name()));
						return false;
					}
				}
			}
			if(product instanceof Ability)
			{
				if(shop.isSold(ShopKeeper.DEAL_TRAINER))
				{
					final MOB teacher=CMClass.getMOB("Teacher");
					final Ability teachableA=getTrainableAbility(teacher, (Ability)product);
					if((teachableA==null)||(!teachableA.canBeLearnedBy(teacher,buyerCustM)))
					{
						teacher.destroy();
						return false;
					}
					teacher.destroy();
				}
				else
				if(buyNotView)
				{
					final Ability A=(Ability)product;
					if(A.canTarget(Ability.CAN_MOBS))
					{
					}
					else
					if(A.canTarget(Ability.CAN_ITEMS))
					{
						Item I=buyerCustM.fetchWieldedItem();
						if(I==null)
							I=buyerCustM.fetchHeldItem();
						if(I==null)
						{
							CMLib.commands().postSay(sellerShopM,buyerCustM,L("You need to be wielding or holding the item you want this cast on."),true,false);
							return false;
						}
					}
					else
					{
						CMLib.commands().postSay(sellerShopM,buyerCustM,L("I don't know how to sell that spell."),true,false);
						return false;
					}
				}
			}
			return true;
		}
		CMLib.commands().postSay(sellerShopM,buyerCustM,L("I don't have that in stock.  Ask for my LIST."),true,false);
		return false;
	}

	@Override
	public String getListInventory(final MOB seller,
								   final MOB buyer,
								   final List<? extends Environmental> rawInventory,
								   final int limit,
								   final ShopKeeper shop,
								   final String mask)
	{
		final StringBuilder str=new StringBuilder("");
		int csize=0;
		final List<Environmental> inventory=new ArrayList<Environmental>(rawInventory.size());
		Environmental E=null;
		for(int i=0;i<rawInventory.size();i++)
		{
			E=rawInventory.get(i);
			if(shownInInventory(seller,buyer,E,shop)
			&&((mask==null)||(mask.length()==0)||(CMLib.english().containsString(E.name(),mask))))
				inventory.add(E);
		}

		if(inventory.size()>0)
		{
			final int totalCols=((shop.isSold(ShopKeeper.DEAL_LANDSELLER))
							   ||(shop.isSold(ShopKeeper.DEAL_CLANDSELLER))
							   ||(shop.isSold(ShopKeeper.DEAL_SHIPSELLER))
							   ||(shop.isSold(ShopKeeper.DEAL_CSHIPSELLER)))?1:2;
			final int totalWidth=CMLib.lister().fixColWidth(60.0/totalCols,buyer);
			String showPrice=null;
			ShopKeeper.ShopPrice price=null;
			for(int i=0;i<inventory.size();i++)
			{
				E=inventory.get(i);
				price=sellingPrice(seller,buyer,E,shop,shop.getShop(), true);
				if((price.experiencePrice>0)&&(((""+price.experiencePrice).length()+2)>(4+csize)))
					csize=(""+price.experiencePrice).length()-2;
				else
				if((price.questPointPrice>0)&&(((""+price.questPointPrice).length()+2)>(4+csize)))
					csize=(""+price.questPointPrice).length()-2;
				else
				{
					showPrice=CMLib.beanCounter().abbreviatedPrice(seller,price.absoluteGoldPrice);
					if(showPrice.length()>(4+csize))
						csize=showPrice.length()-4;
				}
			}

			final String c;
			if(shop instanceof Librarian)
			{
				csize=-7;
				c="^x"+CMStrings.padRight(L("Item"),Math.max(totalWidth-csize,5));
			}
			else
				c="^x["+CMStrings.padRight(L("Cost"),4+csize)+"] "+CMStrings.padRight(L("Product"),Math.max(totalWidth-csize,5));
			str.append(c+((totalCols>1)?c:"")+"^.^N^<!ENTITY shopkeeper \""+CMStrings.removeColors(seller.name())+"\"^>^.^N\n\r");
			int colNum=0;
			int rowNum=0;
			String col=null;
			for(int i=0;i<inventory.size();i++)
			{
				E=inventory.get(i);
				price=sellingPrice(seller,buyer,E,shop,shop.getShop(), true);
				col=null;
				if(csize >= 0)
				{
					if(price.questPointPrice>0)
						col=CMStrings.padRight(L("[@x1qp",""+price.questPointPrice),(5+csize))+"] ";
					else
					if(price.experiencePrice>0)
						col=CMStrings.padRight(L("[@x1xp",""+price.experiencePrice),(5+csize))+"] ";
					else
						col=CMStrings.padRight("["+CMLib.beanCounter().abbreviatedPrice(seller,price.absoluteGoldPrice),5+csize)+"] ";
				}
				else
					col="";
				col += "^<SHOP^>"+CMStrings.padRight(E.name(),"^</SHOP^>",Math.max(totalWidth-csize,5));
				if((++colNum)>totalCols)
				{
					str.append("\n\r");
					rowNum++;
					if((limit>0)&&(rowNum>limit))
						break;
					colNum=1;
				}
				str.append(col);
			}
		}
		if(str.length()==0)
		{
			if(shop instanceof Librarian)
				return seller.name()+" has nothing left to loan.";
			else
			if((!shop.isSold(ShopKeeper.DEAL_BANKER))
			&&(!shop.isSold(ShopKeeper.DEAL_CLANBANKER))
			&&(!shop.isSold(ShopKeeper.DEAL_CLANPOSTMAN))
			&&(!shop.isSold(ShopKeeper.DEAL_AUCTIONEER))
			&&(!shop.isSold(ShopKeeper.DEAL_POSTMAN)))
				return seller.name()+" has nothing for sale.";
			return "";
		}
		final double salesTax=getSalesTax(seller.getStartRoom(),seller);
		return "\n\r"+str
				+((salesTax<=0.0)?"":"\n\r\n\rPrices above include a "+salesTax+"% sales tax.")
				+"^T";
	}

	@Override
	public String findInnRoom(final InnKey key, final String addThis, final Room R)
	{
		if(R==null)
			return null;
		final String keyNum=key.getKey();
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			if((R.getExitInDir(d)!=null)&&(R.getExitInDir(d).keyName().equals(keyNum)))
			{
				final String dirName=((R instanceof BoardableShip)||(R.getArea() instanceof BoardableShip))?
						CMLib.directions().getShipDirectionName(d):CMLib.directions().getDirectionName(d);
				if(addThis.length()>0)
					return addThis+" and to the "+dirName.toLowerCase();
				return "to the "+dirName.toLowerCase();
			}
		}
		return null;
	}

	@Override
	public MOB parseBuyingFor(final MOB buyer, final String message)
	{
		MOB mobFor=buyer;
		if((message!=null)
		&&(message.length()>0)
		&&(buyer.location()!=null))
		{
			final List<String> V=CMParms.parse(message);
			if((V.size()>2)
			&&(V.get(V.size()-2).equalsIgnoreCase("for")))
			{
				String s=V.get(V.size()-1);
				if(s.endsWith("."))
					s=s.substring(0,s.length()-1);
				final MOB M=buyer.location().fetchInhabitant("$"+s+"$");
				if(M!=null)
					mobFor=M;
			}
		}
		return mobFor;
	}

	@Override
	public double transactPawn(final MOB shopkeeper,
							   final MOB pawner,
							   final ShopKeeper shop,
							   final Environmental product)
	{
		final Environmental rawSoldItem=product;
		final Environmental coreSoldItem;
		final int number;
		if(product instanceof PackagedItems)
		{
			coreSoldItem=((PackagedItems)rawSoldItem).peekFirstItem();
			number=((PackagedItems)rawSoldItem).numberOfItemsInPackage();
		}
		else
		{
			coreSoldItem = product;
			number=1;
		}
		if((coreSoldItem!=null)&&(shop.doISellThis(coreSoldItem)))
		{
			final double val=pawningPrice(shopkeeper,pawner,rawSoldItem,shop, shop.getShop()).absoluteGoldPrice;
			final String currency=CMLib.beanCounter().getCurrency(shopkeeper);
			if(!(shopkeeper instanceof ShopKeeper))
				CMLib.beanCounter().subtractMoney(shopkeeper,currency,val);
			CMLib.beanCounter().giveSomeoneMoney(shopkeeper,pawner,currency,val);
			pawner.recoverPhyStats();
			pawner.tell(L("@x1 pays you @x2 for @x3.",shopkeeper.name(),CMLib.beanCounter().nameCurrencyShort(shopkeeper,val),rawSoldItem.name()));
			if(rawSoldItem instanceof Item)
			{
				List<Item> V=null;
				if(rawSoldItem instanceof Container)
					V=((Container)rawSoldItem).getDeepContents();
				((Item)rawSoldItem).unWear();
				((Item)rawSoldItem).removeFromOwnerContainer();
				if(V!=null)
				for(int v=0;v<V.size();v++)
					V.get(v).removeFromOwnerContainer();
				if(coreSoldItem instanceof Physical)
				{
					final Ability privateEffect=((Physical)coreSoldItem).fetchEffect("Prop_PrivateProperty");
					if(privateEffect != null)
						((Physical)coreSoldItem).delEffect(privateEffect);
				}
				shop.getShop().addStoreInventory(coreSoldItem,number,-1);
				if(V!=null)
				{
					for(int v=0;v<V.size();v++)
					{
						final Item item2=V.get(v);
						if(!shop.doISellThis(item2)||(item2 instanceof DoorKey))
							item2.destroy();
						else
						{
							final Ability privateEffect=item2.fetchEffect("Prop_PrivateProperty");
							if(privateEffect != null)
								item2.delEffect(privateEffect);
							shop.getShop().addStoreInventory(item2,1,-1);
						}
					}
				}
			}
			else
			if(product instanceof MOB)
			{
				final MOB newMOB=(MOB)product.copyOf();
				newMOB.setStartRoom(null);
				final Ability A=newMOB.fetchEffect("Skill_Enslave");
				if(A!=null)
					A.setMiscText("");
				newMOB.setLiegeID("");
				newMOB.setClan("", Integer.MIN_VALUE); // delete all sequence
				shop.getShop().addStoreInventory(newMOB);
				((MOB)product).setFollowing(null);
				if((((MOB)product).basePhyStats().rejuv()>0)
				&&(((MOB)product).basePhyStats().rejuv()!=PhyStats.NO_REJUV)
				&&(((MOB)product).getStartRoom()!=null))
					((MOB)product).killMeDead(false);
				else
					((MOB)product).destroy();
			}
			else
			if(product instanceof Ability)
			{

			}
			return val;
		}
		return Double.MIN_VALUE;
	}

	@Override
	public void transactMoneyOnly(final MOB seller,
								  final MOB buyer,
								  final ShopKeeper shop,
								  final Environmental product,
								  final boolean sellerGetsPaid)
	{
		if((seller==null)||(seller.location()==null)||(buyer==null)||(shop==null)||(product==null))
			return;
		final Room room=seller.location();
		final ShopKeeper.ShopPrice price=sellingPrice(seller,buyer,product,shop,shop.getShop(), true);
		if(price.absoluteGoldPrice>0.0)
		{
			CMLib.beanCounter().subtractMoney(buyer,CMLib.beanCounter().getCurrency(seller),price.absoluteGoldPrice);
			double totalFunds=price.absoluteGoldPrice;
			if(getSalesTax(seller.getStartRoom(),seller)!=0.0)
			{
				final Law theLaw=CMLib.law().getTheLaw(room,seller);
				final Area A2=CMLib.law().getLegalObject(room);
				if((theLaw!=null)&&(A2!=null))
				{
					final Law.TreasurySet treas=theLaw.getTreasuryNSafe(A2);
					final Room treasuryR=treas.room;
					final Container treasuryContainer=treas.container;
					if(treasuryR!=null)
					{
						final double taxAmount=totalFunds-sellingPrice(seller,buyer,product,shop,shop.getShop(), false).absoluteGoldPrice;
						totalFunds-=taxAmount;
						final Coins COIN=CMLib.beanCounter().makeBestCurrency(CMLib.beanCounter().getCurrency(seller),taxAmount,treasuryR,treasuryContainer);
						if(COIN!=null)
							COIN.putCoinsBack();
					}
				}
			}
			if(seller.isMonster())
			{
				final LandTitle T=CMLib.law().getLandTitle(seller.getStartRoom());
				if((T!=null)&&(T.getOwnerName().length()>0))
				{
					CMLib.beanCounter().modifyLocalBankGold(seller.getStartRoom().getArea(),
															T.getOwnerName(),
															CMLib.utensils().getFormattedDate(buyer)
																+": Deposit of "+CMLib.beanCounter().nameCurrencyShort(seller,totalFunds)
																+": Purchase: "+product.Name()+" from "+seller.Name(),
															totalFunds);
				}
			}
			if(sellerGetsPaid)
				CMLib.beanCounter().giveSomeoneMoney(seller,seller,CMLib.beanCounter().getCurrency(seller),totalFunds);
		}
		if(price.questPointPrice>0)
			buyer.setQuestPoint(buyer.getQuestPoint()-price.questPointPrice);
		if(price.experiencePrice>0)
			CMLib.leveler().postExperience(buyer,null,null,-price.experiencePrice,false);
		buyer.recoverPhyStats();
	}

	@Override
	public boolean purchaseItems(final Item baseProduct, final List<Environmental> products, final MOB seller, final MOB mobFor)
	{
		if((seller==null)||(seller.location()==null)||(mobFor==null))
			return false;
		final Room room=seller.location();
		for(int p=0;p<products.size();p++)
		{
			if(products.get(p) instanceof Item)
				room.addItem((Item)products.get(p),ItemPossessor.Expire.Player_Drop);
		}
		final CMMsg msg2=CMClass.getMsg(mobFor,baseProduct,seller,CMMsg.MSG_GET,null); // a shopkeeper get is distinguished by having the seller as the tool.
		if((baseProduct instanceof LandTitle)
		||(room.okMessage(mobFor,msg2)))
		{
			room.send(mobFor,msg2);
			if(baseProduct instanceof InnKey)
			{
				final InnKey item =(InnKey)baseProduct;
				String buf=findInnRoom(item, "", room);
				if(buf==null)
					buf=findInnRoom(item, "upstairs", room.getRoomInDir(Directions.UP));
				if(buf==null)
					buf=findInnRoom(item, "downstairs", room.getRoomInDir(Directions.DOWN));
				if(buf!=null)
					CMLib.commands().postSay(seller,mobFor,L("Your room is @x1.",buf),true,false);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean purchaseMOB(final MOB product, final MOB seller, final ShopKeeper shop, final MOB mobFor)
	{
		if((seller==null)||(seller.location()==null)||(product==null)||(shop==null)||(mobFor==null))
			return false;
		product.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		product.recoverPhyStats();
		product.setMiscText(product.text());
		Ability slaveA=null;
		if(shop.isSold(ShopKeeper.DEAL_SLAVES))
		{
			slaveA=product.fetchEffect("Skill_Enslave");
			if(slaveA!=null)
				slaveA.setMiscText("");
			else
			if(!CMLib.flags().isAnimalIntelligence(product))
			{
				slaveA=CMClass.getAbility("Skill_Enslave");
				if(slaveA!=null)
					product.addNonUninvokableEffect(slaveA);
			}
		}
		product.bringToLife(seller.location(),true);
		if(slaveA!=null)
		{
			slaveA=product.fetchEffect("Skill_Enslave");
			product.setLiegeID("");
			product.setClan("", Integer.MIN_VALUE); // delete all sequence
			product.setStartRoom(null);
			if(slaveA!=null)
				slaveA.setMiscText(mobFor.Name());
			product.text();
		}
		CMLib.commands().postFollow(product,mobFor,false);
		if(product.amFollowing()==null)
		{
			mobFor.tell(L("You cannot seem to accept this follower!"));
			return false;
		}
		return true;
	}

	@Override
	public void purchaseAbility(final Ability A,
								final MOB seller,
								final ShopKeeper shop,
								final MOB mobFor)
	{
		if((seller==null)||(seller.location()==null)||(A==null)||(shop==null)||(mobFor==null))
			return ;
		final Room room=seller.location();
		if(shop.isSold(ShopKeeper.DEAL_TRAINER))
		{
			final MOB teacher=CMClass.getMOB("Teacher");
			teacher.setName(seller.name());
			teacher.setBaseCharStats(seller.baseCharStats());
			teacher.setLocation(room);
			teacher.recoverCharStats();
			final Ability teachableA=getTrainableAbility(teacher,A);
			if(teachableA!=null)
				CMLib.expertises().postTeach(teacher,mobFor,teachableA);
			teacher.destroy();
		}
		else
		{
			if(seller.isMonster())
			{
				seller.curState().setMana(seller.maxState().getMana());
				seller.curState().setMovement(seller.maxState().getMovement());
			}
			final Object[][] victims=new Object[room.numInhabitants()][2];
			for(int x=0;x>victims.length;x++)
			{ // save victim status
				final MOB M=room.fetchInhabitant(x);
				if(M!=null)
				{
					victims[x][0]=M;
					victims[x][1]=M.getVictim();
				}
			}
			final List<String> V=new ArrayList<String>();
			if(A.canTarget(Ability.CAN_MOBS))
			{
				V.add("$"+mobFor.name()+"$");
				A.invoke(seller,V,mobFor,true,0);
			}
			else
			if(A.canTarget(Ability.CAN_ITEMS))
			{
				Item I=mobFor.fetchWieldedItem();
				if(I==null)
					I=mobFor.fetchHeldItem();
				if(I==null)
					I=mobFor.fetchItem(null,Wearable.FILTER_WORNONLY,"all");
				if(I==null)
					I=mobFor.fetchItem(null,Wearable.FILTER_UNWORNONLY,"all");
				if(I!=null)
				{
					V.add("$"+I.name()+"$");
					seller.addItem(I);
					A.invoke(seller,V,I,true,0);
					seller.delItem(I);
					if(!mobFor.isMine(I))
						mobFor.addItem(I);
				}
			}
			if(seller.isMonster())
			{
				seller.curState().setMana(seller.maxState().getMana());
				seller.curState().setMovement(seller.maxState().getMovement());
			}
			for(int x=0;x>victims.length;x++)
				((MOB)victims[x][0]).setVictim((MOB)victims[x][1]);
		}
	}

	private void addShipProperty(final MOB buyer, final List<Environmental> V, final ItemCollection extItems)
	{
		for(final Enumeration<Item> i=extItems.items();i.hasMoreElements();)
		{
			final Item I=i.nextElement();
			if((I instanceof PrivateProperty)
			&&(I instanceof BoardableShip)
			&&(!I.amDestroyed()))
			{
				final PrivateProperty P = (PrivateProperty)I;
				if(CMLib.law().doesOwnThisProperty(buyer,P))
				{
					final LandTitle titleI=(LandTitle)CMClass.getItem("GenTitle");
					titleI.setLandPropertyID(I.Name());
					if(!titleI.Name().endsWith(" (Copy)"))
						titleI.setName(L("@x1 (Copy)",titleI.Name()));
					titleI.text();
					((Item)titleI).recoverPhyStats();
					V.add(titleI);
				}
			}
		}
	}

	@Override
	public List<Environmental> addRealEstateTitles(final List<Environmental> productsV, final MOB buyer, final CoffeeShop shop, final Room myRoom)
	{
		if((myRoom==null)||(buyer==null))
			return productsV;
		final Area myArea=myRoom.getArea();
		String name=buyer.Name();
		Pair<Clan,Integer> buyerClanPair=null;
		if(shop.isSold(ShopKeeper.DEAL_CLANDSELLER)
		&&((buyerClanPair=CMLib.clans().findPrivilegedClan(buyer, Clan.Function.PROPERTY_OWNER))!=null))
			name=buyerClanPair.first.clanID();
		if((shop.isSold(ShopKeeper.DEAL_LANDSELLER)||(buyerClanPair!=null))
		&&(myArea!=null))
		{
			final Set<LandTitle> titles=new HashSet<LandTitle>();
			final Map<Room,LandTitle> roomTitleMap = new HashMap<Room, LandTitle>();
			final LegalLibrary law = CMLib.law();
			for(final Enumeration<Room> r=myArea.getProperMap();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				final LandTitle T=law.getLandTitle(R);
				if(T!=null)
				{
					if(!titles.contains(T))
						titles.add(T);
					roomTitleMap.put(R, T);
				}
			}

			for(final LandTitle T : titles)
			{
				if((T.getOwnerName().length()>0) // someone elses title, so never ever list it
				&&(!T.getOwnerName().equals(name))
				&&((!T.getOwnerName().equals(buyer.getLiegeID()))||(!buyer.isMarriedToLiege())))
					continue;

				boolean skipThisOne=false;
				final WorldMap map=CMLib.map();
				for(final Room R : T.getAllTitledRooms())
				{
					for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
					{
						final Room R2=R.getRoomInDir(d);
						if((R2 == null)||(map.getExtendedRoomID(R2).length()==0))
							continue;
						final LandTitle r2T=roomTitleMap.get(R2);
						if(r2T==null)
						{
							skipThisOne = false; // we are connected to unowned room (or another area) -- WIN!
							break;
						}
						if((r2T.getOwnerName().equals(name))
						||(r2T.getOwnerName().equals(buyer.getLiegeID())&&(buyer.isMarriedToLiege())))
						{
							skipThisOne = false; // we are connected to one of OUR rooms -- WIN!
							break;
						}
						if(r2T.getOwnerName().length()>0) // we are connected to someone else .. possibly boo
							skipThisOne=true;
					}
				}
				if(!skipThisOne)
				{
					final Item I=CMClass.getItem("GenTitle");
					((LandTitle)I).setLandPropertyID(T.landPropertyID());
					if((((LandTitle)I).getOwnerName().length()>0)
					&&(!I.Name().endsWith(" (Copy)")))
						I.setName(L("@x1 (Copy)",I.Name()));
					I.text();
					I.recoverPhyStats();
					if((T.getOwnerName().length()==0)
					&&(I.Name().endsWith(" (Copy)")))
						I.setName(I.Name().substring(0,I.Name().length()-7));
					productsV.add(I);
				}
			}
		}

		if(shop.isSold(ShopKeeper.DEAL_SHIPSELLER))
		{
			final PlayerStats pStats = buyer.playerStats();
			if((pStats != null)&&(pStats.getExtItems()!=null))
				this.addShipProperty(buyer, productsV, pStats.getExtItems());
		}
		if(shop.isSold(ShopKeeper.DEAL_CSHIPSELLER))
		{
			buyerClanPair=CMLib.clans().findPrivilegedClan(buyer, Clan.Function.PROPERTY_OWNER);
			if((buyerClanPair != null)&&(buyerClanPair.first!=null)&&(buyerClanPair.first.getExtItems()!=null))
				this.addShipProperty(buyer, productsV, buyerClanPair.first.getExtItems());
		}

		if(productsV.size()<2)
			return productsV;
		// this is actually returned
		final List<Environmental> finalTitleList=new Vector<Environmental>(productsV.size());
		LandTitle L=null;
		LandTitle L2=null;
		int x=-1;
		int x2=-1;
		while(productsV.size()>0)
		{
			if(((!(productsV.get(0) instanceof LandTitle)))
			||((x=(L=(LandTitle)productsV.get(0)).landPropertyID().lastIndexOf('#'))<0))
			{
				if(finalTitleList.size()==0)
					finalTitleList.add(productsV.remove(0));
				else
					finalTitleList.add(0,productsV.remove(0));
			}
			else
			{
				int lowest=CMath.s_int(L.landPropertyID().substring(x+1).trim());
				int chk=0;
				for(int v=1;v<productsV.size();v++)
				{
					if(productsV.get(v) instanceof LandTitle)
					{
						L2=(LandTitle)productsV.get(v);
						x2=L2.landPropertyID().lastIndexOf('#');
						if(x2>0)
						{
							chk=CMath.s_int(L2.landPropertyID().substring(x2+1).trim());
							if(chk<lowest)
							{
								lowest=chk;
								L=L2;
							}
						}
					}
				}
				productsV.remove(L);
				finalTitleList.add(L);
			}
		}
		return finalTitleList;
	}

	@Override
	public boolean ignoreIfNecessary(final MOB mob, final String ignoreMask, final MOB whoIgnores)
	{
		if((whoIgnores != null)
		&&(CMLib.flags().isSleeping(whoIgnores)))
		{
			mob.tell(whoIgnores,null,null,L("<S-NAME> appear(s) to be ignoring you."));
			return false;
		}
		if((ignoreMask.length()>0)
		&&(!CMLib.masking().maskCheck(ignoreMask,mob,false)))
		{
			mob.tell(whoIgnores,null,null,L("<S-NAME> appear(s) to be ignoring you."));
			return false;
		}
		return true;
	}

	@Override
	public String storeKeeperString(final CoffeeShop shop, final ShopKeeper keeper)
	{
		if(shop==null)
			return "";
		if(shop.isSold(ShopKeeper.DEAL_ANYTHING))
			return L("*Anything*");

		final Vector<String> V=new Vector<String>();
		for(int d=1;d<ShopKeeper.DEAL_DESCS.length;d++)
		{
			if(shop.isSold(d))
			{
				switch(d)
				{
				case ShopKeeper.DEAL_GENERAL:
					V.addElement(L("General items"));
					break;
				case ShopKeeper.DEAL_ARMOR:
					V.addElement(L("Armor"));
					break;
				case ShopKeeper.DEAL_MAGIC:
					V.addElement(L("Miscellaneous Magic Items"));
					break;
				case ShopKeeper.DEAL_WEAPONS:
					V.addElement(L("Weapons"));
					break;
				case ShopKeeper.DEAL_PETS:
					V.addElement(L("Pets and Animals"));
					break;
				case ShopKeeper.DEAL_LEATHER:
					V.addElement(L("Leather"));
					break;
				case ShopKeeper.DEAL_INVENTORYONLY:
					V.addElement(L("Only my Inventory"));
					break;
				case ShopKeeper.DEAL_TRAINER:
					V.addElement(L("Training in skills/spells/prayers/songs"));
					break;
				case ShopKeeper.DEAL_CASTER:
					V.addElement(L("Caster of spells/prayers"));
					break;
				case ShopKeeper.DEAL_ALCHEMIST:
					V.addElement(L("Potions"));
					break;
				case ShopKeeper.DEAL_INNKEEPER:
					V.addElement(L("My services as an Inn Keeper"));
					break;
				case ShopKeeper.DEAL_JEWELLER:
					V.addElement(L("Precious stones and jewellery"));
					break;
				case ShopKeeper.DEAL_BANKER:
					V.addElement(L("My services as a Banker"));
					break;
				case ShopKeeper.DEAL_CLANBANKER:
					V.addElement(L("My services as a Banker to Clans"));
					break;
				case ShopKeeper.DEAL_LANDSELLER:
					V.addElement(L("Real estate"));
					break;
				case ShopKeeper.DEAL_CLANDSELLER:
					V.addElement(L("Clan estates"));
					break;
				case ShopKeeper.DEAL_ANYTECHNOLOGY:
					V.addElement(L("Any technology"));
					break;
				case ShopKeeper.DEAL_BUTCHER:
					V.addElement(L("Meats"));
					break;
				case ShopKeeper.DEAL_FOODSELLER:
					V.addElement(L("Foodstuff"));
					break;
				case ShopKeeper.DEAL_GROWER:
					V.addElement(L("Vegetables"));
					break;
				case ShopKeeper.DEAL_HIDESELLER:
					V.addElement(L("Hides and Furs"));
					break;
				case ShopKeeper.DEAL_LUMBERER:
					V.addElement(L("Lumber"));
					break;
				case ShopKeeper.DEAL_METALSMITH:
					V.addElement(L("Metal ores"));
					break;
				case ShopKeeper.DEAL_STONEYARDER:
					V.addElement(L("Stone and rock"));
					break;
				case ShopKeeper.DEAL_SHIPSELLER:
					V.addElement(L("Ships"));
					break;
				case ShopKeeper.DEAL_CSHIPSELLER:
					V.addElement(L("Clan Ships"));
					break;
				case ShopKeeper.DEAL_SLAVES:
					V.addElement(L("Slaves"));
					break;
				case ShopKeeper.DEAL_POSTMAN:
					V.addElement(L("My services as a Postman"));
					break;
				case ShopKeeper.DEAL_CLANPOSTMAN:
					V.addElement(L("My services as a Postman for Clans"));
					break;
				case ShopKeeper.DEAL_AUCTIONEER:
					V.addElement(L("My services as an Auctioneer"));
					break;
				case ShopKeeper.DEAL_INSTRUMENTS:
					V.addElement(L("Musical instruments"));
					break;
				case ShopKeeper.DEAL_BOOKS:
					V.addElement(L("Books"));
					break;
				case ShopKeeper.DEAL_READABLES:
					V.addElement(L("Readables"));
					break;
				case ShopKeeper.DEAL_CLOTHSPINNER:
					V.addElement(L("Cloths"));
					break;
				default:
					V.addElement(L("... I have no idea WHAT I sell"));
					break;
				}
			}
		}
		if((keeper!=null)&&(keeper.getWhatIsSoldZappermask().length()>0))
			V.addElement(CMLib.masking().maskDesc(keeper.getWhatIsSoldZappermask()));
		return CMParms.toListString(V);
	}

	protected boolean shopKeeperItemTypeCheck(final Environmental E, final int dealCode, final ShopKeeper shopKeeper)
	{
		boolean chk;
		switch(dealCode)
		{
		case ShopKeeper.DEAL_ANYTHING:
			chk = !(E instanceof LandTitle);
			break;
		case ShopKeeper.DEAL_ARMOR:
			chk = (E instanceof Armor);
			break;
		case ShopKeeper.DEAL_MAGIC:
			chk = (E instanceof MiscMagic);
			break;
		case ShopKeeper.DEAL_WEAPONS:
			chk = (E instanceof Weapon)||(E instanceof Ammunition);
			break;
		case ShopKeeper.DEAL_GENERAL:
			chk = ((E instanceof Item)
					&&(!(E instanceof Armor))
					&&(!(E instanceof MiscMagic))
					&&(!(E instanceof ClanItem))
					&&(!(E instanceof Weapon))
					&&(!(E instanceof Ammunition))
					&&(!(E instanceof MOB))
					&&(!(E instanceof LandTitle))
					&&(!(E instanceof BoardableShip))
					&&(!(E instanceof RawMaterial))
					&&(!(E instanceof Ability)));
			break;
		case ShopKeeper.DEAL_LEATHER:
			chk = ((E instanceof Item)
					&&((((Item)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LEATHER)
					&&(!(E instanceof RawMaterial)));
			break;
		case ShopKeeper.DEAL_PETS:
			chk = ((E instanceof MOB)&&(CMLib.flags().isAnimalIntelligence((MOB)E)));
			break;
		case ShopKeeper.DEAL_SLAVES:
			chk = ((E instanceof MOB)&&(!CMLib.flags().isAnimalIntelligence((MOB)E)));
			break;
		case ShopKeeper.DEAL_INVENTORYONLY:
		{
			final CoffeeShop shop=(shopKeeper instanceof Librarian)?((Librarian)shopKeeper).getBaseLibrary():shopKeeper.getShop();
			chk = (shop.inEnumerableInventory(E));
			break;
		}
		case ShopKeeper.DEAL_INNKEEPER:
			chk = E instanceof InnKey;
			break;
		case ShopKeeper.DEAL_JEWELLER:
			chk = ((E instanceof Item)
					&&(!(E instanceof Weapon))
					&&(!(E instanceof MiscMagic))
					&&(!(E instanceof ClanItem))
					&&(((((Item)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_GLASS)
					||((((Item)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_PRECIOUS)
					||((Item)E).fitsOn(Wearable.WORN_EARS)
					||((Item)E).fitsOn(Wearable.WORN_NECK)
					||((Item)E).fitsOn(Wearable.WORN_RIGHT_FINGER)
					||((Item)E).fitsOn(Wearable.WORN_LEFT_FINGER)));
			break;
		case ShopKeeper.DEAL_ALCHEMIST:
			chk = (E instanceof Potion);
			break;
		case ShopKeeper.DEAL_LANDSELLER:
		case ShopKeeper.DEAL_CLANDSELLER:
			chk = ((E instanceof LandTitle)&&(CMLib.map().getShip(((LandTitle)E).landPropertyID())==null));
			break;
		case ShopKeeper.DEAL_SHIPSELLER:
		case ShopKeeper.DEAL_CSHIPSELLER:
			chk = ((E instanceof BoardableShip)
					||((E instanceof LandTitle)&&(CMLib.map().getShip(((LandTitle)E).landPropertyID())!=null)));
			break;
		case ShopKeeper.DEAL_ANYTECHNOLOGY:
			chk = (E instanceof Electronics);
			break;
		case ShopKeeper.DEAL_BUTCHER:
			chk = ((E instanceof RawMaterial)
				&&(((RawMaterial)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_FLESH);
			break;
		case ShopKeeper.DEAL_FOODSELLER:
			chk = (((E instanceof Food)||(E instanceof Drink))
					&&(!(E instanceof RawMaterial)));
			break;
		case ShopKeeper.DEAL_GROWER:
			chk = ((E instanceof RawMaterial)
				&&(((RawMaterial)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_VEGETATION);
			break;
		case ShopKeeper.DEAL_HIDESELLER:
			chk = ((E instanceof RawMaterial)
				&&((((RawMaterial)E).material()==RawMaterial.RESOURCE_HIDE)
				||(((RawMaterial)E).material()==RawMaterial.RESOURCE_FEATHERS)
				||(((RawMaterial)E).material()==RawMaterial.RESOURCE_LEATHER)
				||(((RawMaterial)E).material()==RawMaterial.RESOURCE_SCALES)
				||(((RawMaterial)E).material()==RawMaterial.RESOURCE_WOOL)
				||(((RawMaterial)E).material()==RawMaterial.RESOURCE_FUR)));
			break;
		case ShopKeeper.DEAL_LUMBERER:
			chk = ((E instanceof RawMaterial)
				&&((((RawMaterial)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN));
			break;
		case ShopKeeper.DEAL_CLOTHSPINNER:
			chk = ((E instanceof RawMaterial)
				&&((((RawMaterial)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_CLOTH));
			break;
		case ShopKeeper.DEAL_METALSMITH:
			chk = ((E instanceof RawMaterial)
				&&(((((RawMaterial)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
				||(((RawMaterial)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL));
			break;
		case ShopKeeper.DEAL_STONEYARDER:
			chk = ((E instanceof RawMaterial)
				&&(((((RawMaterial)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_ROCK)
					||((((RawMaterial)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_PRECIOUS)));
			break;
		case ShopKeeper.DEAL_INSTRUMENTS:
			chk = (E instanceof MusicalInstrument);
			break;
		case ShopKeeper.DEAL_BOOKS:
			chk = ((E instanceof Item)&&(E.ID().endsWith("Book"))&&(CMLib.flags().isReadable((Item)E)));
			break;
		case ShopKeeper.DEAL_READABLES:
			chk = ((E instanceof Item)&&(CMLib.flags().isReadable((Item)E)));
			break;
		default:
			chk=false;
			break;
		}
		if((shopKeeper!=null)&&(shopKeeper.getWhatIsSoldZappermask().length()>0))
			chk = chk && CMLib.masking().maskCheck(shopKeeper.getWhatIsSoldZappermask(), E, true);
		return chk;
	}

	@Override
	public boolean doISellThis(Environmental thisThang, final ShopKeeper shop)
	{
		if(thisThang instanceof PackagedItems)
			thisThang=((PackagedItems)thisThang).peekFirstItem();
		if(thisThang==null)
			return false;
		if((thisThang instanceof Coins)
		||(thisThang instanceof DeadBody)
		||(CMLib.flags().isChild(thisThang)))
			return false;
		boolean yesISell=false;
		if(shop.isSold(ShopKeeper.DEAL_ANYTHING))
		{
			yesISell = !(thisThang instanceof LandTitle);
		}
		else
		{
			for(int d=1;d<ShopKeeper.DEAL_DESCS.length;d++)
			{
				if(shop.isSold(d) && shopKeeperItemTypeCheck(thisThang,d,shop))
				{
					yesISell=true;
					break;
				}
			}
		}
		if(yesISell)
		{
			if((shop.getWhatIsSoldZappermask().length()>0)
			&&(!CMLib.masking().maskCheck(shop.getWhatIsSoldZappermask(),thisThang,true)))
				yesISell = false;
		}
		return yesISell;
	}

	@Override
	public void returnMoney(final MOB to, final String currency, final double amt)
	{
		if(amt>0)
			CMLib.beanCounter().giveSomeoneMoney(to, currency, amt);
		else
			CMLib.beanCounter().subtractMoney(to, currency,-amt);
		if(amt!=0)
		{
			if(!CMLib.flags().isInTheGame(to,true))
				CMLib.database().DBUpdatePlayerItems(to);
		}
	}

	@Override
	public String[] bid(final MOB mob, final double bid, final String bidCurrency, final AuctionData auctionData, final Item I, final List<String> auctionAnnounces)
	{
		String bidWords=CMLib.beanCounter().nameCurrencyShort(auctionData.getCurrency(),auctionData.getBid());
		final String currencyName=CMLib.beanCounter().getDenominationName(auctionData.getCurrency());
		if(bid==0.0)
			return new String[]{L("Up for auction: ^[@x1^].  The current bid is @x2.",I.name(),bidWords),null};

		if(!bidCurrency.equals(auctionData.getCurrency()))
			return new String[]{L("This auction is being bid in @x1 only.",currencyName),null};

		if(bid>CMLib.beanCounter().getTotalAbsoluteValue(mob,auctionData.getCurrency()))
			return new String[]{L("You don't have enough @x1 on hand to cover that bid.",currencyName),null};

		if((bid<auctionData.getBid())||(bid==0))
		{
			final String bwords=CMLib.beanCounter().nameCurrencyShort(bidCurrency, bid);
			return new String[]{L("Your bid of @x1 is insufficient.",bwords)
								+((auctionData.getBid()>0)?L(" The current high bid is @x1.",bidWords):""),null};
		}
		else
		if((bid>auctionData.getHighBid())||((bid>auctionData.getBid())&&(auctionData.getHighBid()==0)))
		{
			final MOB oldHighBider=auctionData.getHighBidderMob();
			if(auctionData.getHighBidderMob()!=null)
				returnMoney(auctionData.getHighBidderMob(),auctionData.getCurrency(),auctionData.getHighBid());
			auctionData.setHighBidderMob(mob);
			if(auctionData.getHighBid()<=0.0)
			{
				if(auctionData.getBid()>0)
					auctionData.setHighBid(auctionData.getBid());
				else
					auctionData.setHighBid(0.0);
			}
			auctionData.setBid(auctionData.getHighBid()+1.0);
			auctionData.setHighBid(bid);
			returnMoney(auctionData.getHighBidderMob(),auctionData.getCurrency(),-bid);
			bidWords=CMLib.beanCounter().nameCurrencyShort(auctionData.getCurrency(),auctionData.getBid());
			final String yourBidWords = CMLib.beanCounter().abbreviatedPrice(currencyName, auctionData.getHighBid());
			auctionAnnounces.add(L("A new bid has been entered for ^[@x1^]. The current high bid is @x2.",I.name(),bidWords));
			if((oldHighBider!=null)&&(oldHighBider==mob))
				return new String[]{L("You have submitted a new high bid of @x1 for ^[@x2^].",yourBidWords,I.name()),null};
			else
			if((oldHighBider!=null)&&(oldHighBider!=mob))
			{
				return new String[]{L("You have the new high reserve bid of @x1 for ^[@x2^]."
									+ " The current nominal high bid is @x3.",yourBidWords,I.name(),bidWords),
									L("You have been outbid for ^[@x1^].",I.name())
				};
			}
			else
				return new String[]{L("You have submitted a bid of @x1 for ^[@x2^].",yourBidWords,I.name()),null};
		}
		else
		if((bid==auctionData.getBid())&&(auctionData.getHighBidderMob()!=null))
		{
			return new String[]{L("You must bid higher than @x1 to have your bid accepted.",bidWords),null};
		}
		else
		if((bid==auctionData.getHighBid())&&(auctionData.getHighBidderMob()!=null))
		{
			if((auctionData.getHighBidderMob()!=null)&&(auctionData.getHighBidderMob()!=mob))
			{
				auctionData.setBid(bid);
				bidWords=CMLib.beanCounter().nameCurrencyShort(auctionData.getCurrency(),auctionData.getBid());
				auctionAnnounces.add(L("A new bid has been entered for ^[@x1^]. The current bid is @x2.",I.name(),bidWords));
				return new String[]{
						L("You have been outbid by proxy for ^[@x1^].",I.name()),
						L("Your high bid for ^[@x1^] has been reached.",I.name())};
			}
		}
		else
		{
			auctionData.setBid(bid);
			bidWords=CMLib.beanCounter().nameCurrencyShort(auctionData.getCurrency(),auctionData.getBid());
			auctionAnnounces.add(L("A new bid has been entered for ^[@x1^]. The current bid is @x2.",I.name(),bidWords));
			return new String[]{L("You have been outbid by proxy for ^[@x1^].",I.name()),null};
		}
		return null;
	}

	@Override
	public AuctionData getEnumeratedAuction(final String named, final String auctionHouse)
	{
		final List<AuctionData> V=getAuctions(null,auctionHouse);
		final List<Item> V2=new ArrayList<Item>();
		for(int v=0;v<V.size();v++)
			V2.add(V.get(v).getAuctionedItem());
		Environmental E=CMLib.english().fetchEnvironmental(V2,named,true);
		if(!(E instanceof Item))
			E=CMLib.english().fetchEnvironmental(V2,named,false);
		if(E!=null)
		{
			for(int v=0;v<V.size();v++)
			{
				if(V.get(v).getAuctionedItem()==E)
					return V.get(v);
			}
		}
		return null;
	}

	@Override
	public void saveAuction(final AuctionData data, final String auctionHouse, final boolean updateOnly)
	{
		if(data.getAuctionedItem() instanceof Container)
			((Container)data.getAuctionedItem()).emptyPlease(false);
		final StringBuilder xml=new StringBuilder("<AUCTION>");
		xml.append("<PRICE>"+data.getBid()+"</PRICE>");
		xml.append("<BUYOUT>"+data.getBuyOutPrice()+"</BUYOUT>");
		if(data.getHighBidderMob()!=null)
			xml.append("<BIDDER>"+data.getHighBidderMob().Name()+"</BIDDER>");
		else
			xml.append("<BIDDER />");
		xml.append("<MAXBID>"+data.getHighBid()+"</MAXBID>");
		xml.append("<AUCTIONITEM>");
		xml.append(CMLib.coffeeMaker().getItemXML(data.getAuctionedItem()).toString());
		xml.append("</AUCTIONITEM>");
		xml.append("</AUCTION>");
		if(!updateOnly)
		{
			CMLib.database().DBWriteJournal("SYSTEM_AUCTIONS_"+auctionHouse.toUpperCase().trim(),
											data.getAuctioningMob().Name(),
											""+data.getAuctionTickDown(),
											CMStrings.limit(data.getAuctionedItem().name(),38),
											xml.toString());
		}
		else
			CMLib.database().DBUpdateJournal(data.getAuctionDBKey(), data.getAuctionedItem().Name(),xml.toString(), 0);
	}

	@Override
	public List<AuctionData> getAuctions(final Object ofLike, final String auctionHouse)
	{
		final Vector<AuctionData> auctions=new Vector<AuctionData>();
		final String house="SYSTEM_AUCTIONS_"+auctionHouse.toUpperCase().trim();
		final List<JournalEntry> otherAuctions=CMLib.database().DBReadJournalMsgsByUpdateDate(house, true);
		for(int o=0;o<otherAuctions.size();o++)
		{
			final JournalEntry auctionData=otherAuctions.get(o);
			final String from=auctionData.from();
			final String to=auctionData.to();
			final String key=auctionData.key();
			if((ofLike instanceof MOB)&&(!((MOB)ofLike).Name().equals(to)))
				continue;
			if((ofLike instanceof String)&&(!((String)ofLike).equals(key)))
				continue;
			final AuctionData data=(AuctionData)CMClass.getCommon("DefaultAuction");
			data.setStartTime(auctionData.date());
			data.setAuctionTickDown(CMath.s_long(to));
			final String xml=auctionData.msg();
			List<XMLLibrary.XMLTag> xmlV=CMLib.xml().parseAllXML(xml);
			xmlV=CMLib.xml().getContentsFromPieces(xmlV,"AUCTION");
			final String bid=CMLib.xml().getValFromPieces(xmlV,"PRICE");
			final double oldBid=CMath.s_double(bid);
			data.setBid(oldBid);
			final String highBidder=CMLib.xml().getValFromPieces(xmlV,"BIDDER");
			if(highBidder.length()>0)
				data.setHighBidderMob(CMLib.players().getLoadPlayer(highBidder));
			final String maxBid=CMLib.xml().getValFromPieces(xmlV,"MAXBID");
			final double oldMaxBid=CMath.s_double(maxBid);
			data.setHighBid(oldMaxBid);
			data.setAuctionDBKey(key);
			final String buyOutPrice=CMLib.xml().getValFromPieces(xmlV,"BUYOUT");
			data.setBuyOutPrice(CMath.s_double(buyOutPrice));
			data.setAuctioningMob(CMLib.players().getLoadPlayer(from));
			data.setCurrency(CMLib.beanCounter().getCurrency(data.getAuctioningMob()));
			for(int v=0;v<xmlV.size();v++)
			{
				final XMLTag X=xmlV.get(v);
				if(X.tag().equalsIgnoreCase("AUCTIONITEM"))
				{
					data.setAuctionedItem(CMLib.coffeeMaker().getItemFromXML(X.value()));
					break;
				}
			}
			if((ofLike instanceof Item)&&(!((Item)ofLike).sameAs(data.getAuctionedItem())))
				continue;
			auctions.addElement(data);
		}
		return auctions;
	}

	@Override
	public String getListForMask(final String targetMessage)
	{
		if(targetMessage==null)
			return null;
		final int x=targetMessage.toUpperCase().lastIndexOf("FOR '");
		if(x>0)
		{
			final int y=targetMessage.lastIndexOf('\'');
			if(y>x)
				return targetMessage.substring(x+5,y);
		}
		return null;
	}

	@Override
	public String getAuctionInventory(final MOB seller, final MOB buyer, final Auctioneer auction, final String mask)
	{
		final StringBuilder str=new StringBuilder("");
		str.append("^x"+CMStrings.padRight(L("Lvl"),3)+" "+CMStrings.padRight(L("Item"),50)+" "+CMStrings.padRight(L("Days"),4)+" ["+CMStrings.padRight(L("Bid"),6)+"] Buy^.^N\n\r");
		final List<AuctionData> auctions=getAuctions(null,auction.auctionHouse());
		for(int v=0;v<auctions.size();v++)
		{
			final AuctionData data=auctions.get(v);
			if(shownInInventory(seller,buyer,data.getAuctionedItem(),auction))
			{
				if(((mask==null)||(mask.length()==0)||(CMLib.english().containsString(data.getAuctionedItem().name(),mask)))
				&&((data.getAuctionTickDown()>System.currentTimeMillis())||(data.getAuctioningMob()==buyer)||(data.getHighBidderMob()==buyer)))
				{
					Area area=CMLib.map().getStartArea(seller);
					if(area==null)
						area=CMLib.map().getStartArea(buyer);
					str.append(CMStrings.padRight(""+data.getAuctionedItem().phyStats().level(),3)+" ");
					str.append(CMStrings.padRight(data.getAuctionedItem().name(),50)+" ");
					if(data.getAuctionTickDown()>System.currentTimeMillis())
					{
						final long days=data.daysRemaining(buyer,seller);
						str.append(CMStrings.padRight(""+days,4)+" ");
					}
					else
					if(data.getAuctioningMob()==buyer)
						str.append("DONE ");
					else
						str.append("WON! ");
					str.append("["+CMStrings.padRight(CMLib.beanCounter().abbreviatedPrice(seller,data.getBid()),6)+"] ");
					if(data.getBuyOutPrice()<=0.0)
						str.append(CMStrings.padRight("-",6));
					else
						str.append(CMStrings.padRight(CMLib.beanCounter().abbreviatedPrice(seller,data.getBuyOutPrice()),6));
					str.append("\n\r");
				}
			}
		}
		return "\n\r"+str.toString();
	}

	@Override
	public void auctionNotify(final MOB M, final String resp, final String regardingItem)
	{
		try
		{
			if(CMLib.flags().isInTheGame(M,true))
				M.tell(resp);
			else
			if(M.playerStats()!=null)
			{
				CMLib.smtp().emailIfPossible(CMProps.getVar(CMProps.Str.SMTPSERVERNAME),
											"auction@"+CMProps.getVar(CMProps.Str.MUDDOMAIN).toLowerCase(),
											"noreply@"+CMProps.getVar(CMProps.Str.MUDDOMAIN).toLowerCase(),
											M.playerStats().getEmail(),
											"Auction Update for item: "+regardingItem,
											resp);
			}
		}
		catch (final Exception e)
		{
		}
	}

	@Override
	public void cancelAuction(final String auctionHouse, final AuctionData data)
	{
		final MOB mob = data.getAuctioningMob();
		final Item I=data.getAuctionedItem();
		if(I!=null)
		{
			if(mob!=null)
				mob.moveItemTo(I);
			final MOB M=data.getHighBidderMob();
			if(M!=null)
			{
				auctionNotify(M,"The auction for "+I.Name()+" was closed early.  You have been refunded your max bid.",I.Name());
				CMLib.coffeeShops().returnMoney(M,data.getCurrency(),data.getHighBid());
			}
		}
		CMLib.database().DBDeleteJournal(auctionHouse, data.getAuctionDBKey());
		if(mob!=null)
			mob.tell(L("Auction ended."));
	}
}
