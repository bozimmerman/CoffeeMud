package com.planet_ink.coffee_mud.MOBS;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class StdShopKeeper extends StdMOB implements ShopKeeper
{
	public String ID(){return "StdShopKeeper";}
	protected int whatISell=0;
	protected Vector storeInventory=new Vector();
	protected Vector baseInventory=new Vector();
	protected Hashtable duplicateInventory=new Hashtable();
	protected Hashtable prices=new Hashtable();
	protected int invResetRate=0;
	protected int invResetTickDown=0;
	protected String budget="";
	protected long budgetRemaining=Long.MAX_VALUE/2;
	protected long budgetMax=Long.MAX_VALUE/2;
	protected int budgetTickDown=2;
	protected String devalueRate="";

	private final static Hashtable titleSets=new Hashtable();

	public StdShopKeeper()
	{
		super();
		Username="a shopkeeper";
		setDescription("He\\`s pleased to be of assistance.");
		setDisplayText("A shopkeeper is waiting to serve you.");
		setAlignment(1000);
		setMoney(0);
		baseEnvStats.setWeight(150);
		setWimpHitPoint(0);

		baseCharStats().setStat(CharStats.INTELLIGENCE,16);
		baseCharStats().setStat(CharStats.CHARISMA,25);

		baseEnvStats().setArmor(0);

		baseState.setHitPoints(1000);

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

	public int whatIsSold(){return whatISell;}
	public void setWhatIsSold(int newSellCode){whatISell=newSellCode;}

	protected void cloneFix(MOB E)
	{
		super.cloneFix(E);
		if(E instanceof StdShopKeeper)
		{
			storeInventory=new Vector();
			baseInventory=new Vector();
			duplicateInventory=new Hashtable();

			StdShopKeeper SK=(StdShopKeeper)E;
			for(int i=0;i<SK.storeInventory.size();i++)
			{
				Environmental I2=(Environmental)SK.storeInventory.elementAt(i);
				if(I2!=null)
					storeInventory.addElement(I2.copyOf());
			}
			for(int i=0;i<SK.baseInventory.size();i++)
			{
				Environmental I2=(Environmental)SK.baseInventory.elementAt(i);
				if(I2!=null)
					baseInventory.addElement(I2.copyOf());
			}
			for(Enumeration e=SK.duplicateInventory.keys();e.hasMoreElements();)
			{
				Environmental I2=(Environmental)e.nextElement();
				Integer I3=(Integer)SK.duplicateInventory.get(I2);
				if((I2!=null)&&(I3!=null))
					duplicateInventory.put(I2.copyOf(),I3);
			}
		}
	}
	protected boolean inBaseInventory(Environmental thisThang)
	{
		for(int x=0;x<baseInventory.size();x++)
		{
			Environmental E=(Environmental)baseInventory.elementAt(x);
			if((thisThang.isGeneric())&&(E.isGeneric()))
			{
				if(thisThang.Name().equals(E.Name()))
					return true;
			}
			else
			if(CMClass.className(thisThang).equals(CMClass.className(E)))
				return true;
		}
		return false;
	}

	public void addStoreInventory(Environmental thisThang)
	{
		addStoreInventory(thisThang,1,-1);
	}

	public int baseStockSize()
	{
		return baseInventory.size();
	}

	public int totalStockSize()
	{
		return storeInventory.size();
	}

	public void clearStoreInventory()
	{
		storeInventory.clear();
		baseInventory.clear();
		duplicateInventory.clear();
	}

	public Vector getUniqueStoreInventory()
	{
		Vector V=new Vector();
		Environmental lastE=null;
		for(int x=0;x<storeInventory.size();x++)
		{
			Environmental E=(Environmental)storeInventory.elementAt(x);
			boolean ok=true;

			if(lastE!=null)
			{
				if((lastE.isGeneric())&&(E.isGeneric()))
				{
					if(E.Name().equals(lastE.Name()))
						ok=false;
				}
				else
				if(lastE.ID().equals(E.ID()))
					ok=false;
			}

			if(ok)
			for(int v=0;v<V.size();v++)
			{
				Environmental EE=(Environmental)V.elementAt(v);
				if((EE.isGeneric())&&(E.isGeneric()))
				{
					if(E.Name().equals(EE.Name()))
					{
						ok=false;
						break;
					}
				}
				else
				if(EE.ID().equals(E.ID()))
					ok=false;
			}

			if(ok)
			{
				V.addElement(E);
				lastE=E;
			}
		}
		return V;
	}
	public Vector getBaseInventory()
	{
		return baseInventory;
	}

	public String storeKeeperString()
	{
		switch(whatISell)
		{
		case DEAL_ANYTHING:
			return "*Anything*";
		case DEAL_GENERAL:
			return "General items";
		case DEAL_ARMOR:
			return "Armor";
		case DEAL_MAGIC:
			return "Miscellaneous Magic Items";
		case DEAL_WEAPONS:
			return "Weapons";
		case DEAL_PETS:
			return "Pets and Animals";
		case DEAL_LEATHER:
			return "Leather";
		case DEAL_INVENTORYONLY:
			return "Only my Inventory";
		case DEAL_TRAINER:
			return "Training in skills/spells/prayers/songs";
		case DEAL_CASTER:
			return "Caster of spells/prayers";
		case DEAL_ALCHEMIST:
			return "Potions";
		case DEAL_INNKEEPER:
			return "My services as an Inn Keeper";
		case DEAL_JEWELLER:
			return "Precious stones and jewellery";
		case DEAL_BANKER:
			return "My services as a Banker";
		case DEAL_CLANBANKER:
			return "My services as a Banker to Clans";
		case DEAL_LANDSELLER:
			return "Real estate";
		case DEAL_CLANDSELLER:
			return "Clan estates";
		case DEAL_ANYTECHNOLOGY:
			return "Any technology";
		case DEAL_BUTCHER:
			return "Meats";
		case DEAL_FOODSELLER:
			return "Foodstuff";
		case DEAL_GROWER:
			return "Vegetables";
		case DEAL_HIDESELLER:
			return "Hides and Furs";
		case DEAL_LUMBERER:
			return "Lumber";
		case DEAL_METALSMITH:
			return "Metal ores";
		case DEAL_STONEYARDER:
			return "Stone and rock";
		case DEAL_SHIPSELLER:
			return "Ships";
		case DEAL_CSHIPSELLER:
			return "Clan Ships";
		case DEAL_SLAVES:
		    return "Slaves";
		default:
			return "... I have no idea WHAT I sell";
		}
	}

	public void addStoreInventory(Environmental thisThang, int number, int price)
	{
		if((whatISell==DEAL_INVENTORYONLY)&&(!inBaseInventory(thisThang)))
			baseInventory.addElement(thisThang.copyOf());
		if(prices.containsKey(thisThang.ID()+"/"+thisThang.name()))
			prices.remove(thisThang.ID()+"/"+thisThang.name());
		prices.put(thisThang.ID()+"/"+thisThang.name(),new Integer(price));
		if(thisThang instanceof InnKey)
		{
			for(int v=0;v<number;v++)
			{
				Environmental copy=thisThang.copyOf();
				((InnKey)copy).hangOnRack(this);
				storeInventory.addElement(copy);
			}
		}
		else
		{
			Environmental copy=thisThang.copyOf();
			storeInventory.addElement(copy);
			if(number>1)
				duplicateInventory.put(copy,new Integer(number));
		}
	}

	protected int processVariableEquipment()
	{
		int newLastTickedDateTime=super.processVariableEquipment();
		if(newLastTickedDateTime==0)
		{
			Vector rivals=new Vector();
			for(int v=0;v<baseInventory.size();v++)
			{
				Environmental E=(Environmental)baseInventory.elementAt(v);
				if((E.baseEnvStats().rejuv()>0)&&(E.baseEnvStats().rejuv()<Integer.MAX_VALUE))
					rivals.addElement(E);
			}
			for(int r=0;r<rivals.size();r++)
			{
				Environmental E=(Environmental)rivals.elementAt(r);
				if(Dice.rollPercentage()>E.baseEnvStats().rejuv())
					delStoreInventory(E);
				else
				{
					E.baseEnvStats().setRejuv(0);
					E.envStats().setRejuv(0);
				}
			}
		}
		return newLastTickedDateTime;
	}


	public void delStoreInventory(Environmental thisThang)
	{
		if((whatISell==DEAL_INVENTORYONLY)&&(inBaseInventory(thisThang)))
		{
			for(int v=baseInventory.size()-1;v>=0;v--)
			{
				Environmental E=(Environmental)baseInventory.elementAt(v);
				if((thisThang.isGeneric())&&(E.isGeneric()))
				{
					if(thisThang.Name().equals(E.Name()))
						baseInventory.removeElement(E);
				}
				else
				if(thisThang.ID().equals(E.ID()))
					baseInventory.removeElement(E);
			}
		}
		for(int v=storeInventory.size()-1;v>=0;v--)
		{
			Environmental E=(Environmental)storeInventory.elementAt(v);
			if((thisThang.isGeneric())&&(E.isGeneric()))
			{
				if(thisThang.Name().equals(E.Name()))
				{
					storeInventory.removeElement(E);
					duplicateInventory.remove(E);
				}
			}
			else
			if(thisThang.ID().equals(E.ID()))
			{
				storeInventory.removeElement(E);
				duplicateInventory.remove(E);
			}
		}
		prices.remove(thisThang.ID()+"/"+thisThang.name());
	}

	public boolean doISellThis(Environmental thisThang)
	{
		if(thisThang==null)
			return false;
		switch(whatISell)
		{
		case DEAL_ANYTHING:
			return true;
		case DEAL_ARMOR:
			return (thisThang instanceof Armor);
		case DEAL_MAGIC:
			return (thisThang instanceof MiscMagic);
		case DEAL_WEAPONS:
			return (thisThang instanceof Weapon)||(thisThang instanceof Ammunition);
		case DEAL_GENERAL:
			return ((thisThang instanceof Item)
					&&(!(thisThang instanceof Armor))
					&&(!(thisThang instanceof MiscMagic))
					&&(!(thisThang instanceof ClanItem))
					&&(!(thisThang instanceof Weapon))
					&&(!(thisThang instanceof Ammunition))
					&&(!(thisThang instanceof MOB))
					&&(!(thisThang instanceof EnvResource))
					&&(!(thisThang instanceof Ability)));
		case DEAL_LEATHER:
			return ((thisThang instanceof Item)
					&&((((Item)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LEATHER)
					&&(!(thisThang instanceof EnvResource)));
		case DEAL_PETS:
			return ((thisThang instanceof MOB)&&(Sense.isAnimalIntelligence((MOB)thisThang)));
		case DEAL_SLAVES:
			return ((thisThang instanceof MOB)&&(!Sense.isAnimalIntelligence((MOB)thisThang)));
		case DEAL_INVENTORYONLY:
			return (inBaseInventory(thisThang));
		case DEAL_INNKEEPER:
			return thisThang instanceof InnKey;
		case DEAL_JEWELLER:
			return ((thisThang instanceof Item)
					&&(!(thisThang instanceof Weapon))
					&&(!(thisThang instanceof MiscMagic))
					&&(!(thisThang instanceof ClanItem))
					&&(((((Item)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_GLASS)
					||((((Item)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_PRECIOUS)
					||((Item)thisThang).fitsOn(Item.ON_EARS)
					||((Item)thisThang).fitsOn(Item.ON_NECK)
					||((Item)thisThang).fitsOn(Item.ON_RIGHT_FINGER)
					||((Item)thisThang).fitsOn(Item.ON_LEFT_FINGER)));
		case DEAL_ALCHEMIST:
			return (thisThang instanceof Potion);
		case DEAL_LANDSELLER:
		case DEAL_CLANDSELLER:
		case DEAL_SHIPSELLER:
		case DEAL_CSHIPSELLER:
			return (thisThang instanceof LandTitle);
		case DEAL_ANYTECHNOLOGY:
			return (thisThang instanceof Electronics);
		case DEAL_BUTCHER:
			return ((thisThang instanceof EnvResource)
				&&(((EnvResource)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_FLESH);
		case DEAL_FOODSELLER:
			return (((thisThang instanceof Food)||(thisThang instanceof Drink))
					&&(!(thisThang instanceof EnvResource)));
		case DEAL_GROWER:
			return ((thisThang instanceof EnvResource)
				&&(((EnvResource)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_VEGETATION);
		case DEAL_HIDESELLER:
			return ((thisThang instanceof EnvResource)
				&&((((EnvResource)thisThang).material()==EnvResource.RESOURCE_HIDE)
				||(((EnvResource)thisThang).material()==EnvResource.RESOURCE_FEATHERS)
				||(((EnvResource)thisThang).material()==EnvResource.RESOURCE_LEATHER)
				||(((EnvResource)thisThang).material()==EnvResource.RESOURCE_SCALES)
				||(((EnvResource)thisThang).material()==EnvResource.RESOURCE_WOOL)
				||(((EnvResource)thisThang).material()==EnvResource.RESOURCE_FUR)));
		case DEAL_LUMBERER:
			return ((thisThang instanceof EnvResource)
				&&((((EnvResource)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN));
		case DEAL_METALSMITH:
			return ((thisThang instanceof EnvResource)
				&&(((((EnvResource)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL)
				||(((EnvResource)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_MITHRIL));
		case DEAL_STONEYARDER:
			return ((thisThang instanceof EnvResource)
				&&((((EnvResource)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_ROCK));
		}

		return false;
	}

	public boolean doIHaveThisInStock(String name, MOB mob)
	{
		Environmental item=EnglishParser.fetchEnvironmental(storeInventory,name,true);
		if(item==null)
			item=EnglishParser.fetchEnvironmental(storeInventory,name,false);
		if((item==null)
		   &&(mob!=null)
		   &&((whatISell==DEAL_LANDSELLER)||(whatISell==DEAL_CLANDSELLER)
			  ||(whatISell==DEAL_SHIPSELLER)||(whatISell==DEAL_CSHIPSELLER)))
		{
			item=EnglishParser.fetchEnvironmental(addRealEstate(new Vector(),mob),name,true);
			if(item==null)
				item=EnglishParser.fetchEnvironmental(addRealEstate(new Vector(),mob),name,false);
		}
		if(item!=null)
		   return true;
		return false;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==MudHost.TICK_MOB)&&(isGeneric()))
		{
			if(invResetTickDown==0)
			{
				invResetTickDown=invResetRate();
				if(invResetTickDown==0) invResetTickDown=Util.s_int(CommonStrings.getVar(CommonStrings.SYSTEM_INVRESETRATE));
				if(invResetTickDown==0) invResetTickDown=Integer.MAX_VALUE;
			}
			else
			if((--invResetTickDown)<=0)
			{
				if(storeInventory!=null)storeInventory.clear();
				if(baseInventory!=null)baseInventory.clear();
				if(duplicateInventory!=null) duplicateInventory.clear();
				if(prices!=null) prices.clear();
				if(titleSets!=null) titleSets.clear();
				invResetTickDown=invResetRate();
				if(invResetTickDown==0) invResetTickDown=Util.s_int(CommonStrings.getVar(CommonStrings.SYSTEM_INVRESETRATE));
				if(invResetTickDown==0) invResetTickDown=Integer.MAX_VALUE;
				if(miscText!=null)
				{
					String shoptext;
					if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MOBCOMPRESS))
						shoptext=CoffeeMaker.getGenMOBTextUnpacked(this,Util.decompressString(miscText));
					else
						shoptext=CoffeeMaker.getGenMOBTextUnpacked(this,new String(miscText));
					Vector xml=XMLManager.parseAllXML(shoptext);
					if(xml!=null)
					{
						CoffeeMaker.populateShops(this,xml);
						recoverEnvStats();
						recoverCharStats();
					}
				}
			}
			if((--budgetTickDown)<=0)
			{
				budgetTickDown=100;
				budgetRemaining=Long.MAX_VALUE/2;
				String s=budget();
				if(s.length()==0) s=CommonStrings.getVar(CommonStrings.SYSTEM_BUDGET);
				Vector V=Util.parse(s.trim().toUpperCase());
				if(V.size()>0)
				{
					if(((String)V.firstElement()).equals("0"))
						budgetRemaining=0;
					else
					{
						budgetRemaining=Util.s_long((String)V.firstElement());
						if(budgetRemaining==0)
							budgetRemaining=Long.MAX_VALUE/2;
					}
					s="DAY";
					if(V.size()>1) s=((String)V.lastElement()).toUpperCase();
					if(s.startsWith("DAY"))
						budgetTickDown=CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY);
					else
					if(location()!=null)
					{
						if(s.startsWith("HOUR"))
							budgetTickDown=CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)/location().getArea().getTimeObj().getHoursInDay();
						else
						if(s.startsWith("WEEK"))
							budgetTickDown=location().getArea().getTimeObj().getDaysInWeek()*CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY);
						else
						if(s.startsWith("MONTH"))
							budgetTickDown=location().getArea().getTimeObj().getDaysInMonth()*CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY);
						else
						if(s.startsWith("YEAR"))
							budgetTickDown=location().getArea().getTimeObj().getDaysInMonth()*location().getArea().getTimeObj().getMonthsInYear()*CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY);
					}
				}
				budgetMax=budgetRemaining;
			}
		}
		return true;
	}

	public int stockPrice(Environmental likeThis)
	{
		if(prices.containsKey(likeThis.ID()+"/"+likeThis.name()))
		   return ((Integer)prices.get(likeThis.ID()+"/"+likeThis.name())).intValue();
		return -1;
	}
	public int numberInStock(Environmental likeThis)
	{
		int num=0;
		for(int v=0;v<storeInventory.size();v++)
		{
			Environmental E=(Environmental)storeInventory.elementAt(v);
			if((likeThis.isGeneric())&&(E.isGeneric()))
			{
				if(E.Name().equals(likeThis.Name()))
				{
					Integer possNum=(Integer)duplicateInventory.get(E);
					if(possNum!=null)
						num+=possNum.intValue();
					else
						num++;
				}
			}
			else
			if(E.ID().equals(likeThis.ID()))
			{
				Integer possNum=(Integer)duplicateInventory.get(E);
				if(possNum!=null)
					num+=possNum.intValue();
				else
					num++;
			}
		}

		return num;
	}

	public Environmental getStock(String name, MOB mob)
	{
		Environmental item=EnglishParser.fetchEnvironmental(storeInventory,name,true);
		if(item==null)
			item=EnglishParser.fetchEnvironmental(storeInventory,name,false);
		if((item==null)
		&&((whatISell==DEAL_LANDSELLER)||(whatISell==DEAL_CLANDSELLER)
		   ||(whatISell==DEAL_SHIPSELLER)||(whatISell==DEAL_CSHIPSELLER))
		&&(mob!=null))
		{
			item=EnglishParser.fetchEnvironmental(addRealEstate(new Vector(),mob),name,true);
			if(item==null)
				item=EnglishParser.fetchEnvironmental(addRealEstate(new Vector(),mob),name,false);
		}
		return item;
	}


	public Environmental removeStock(String name, MOB mob)
	{
		Environmental item=getStock(name,mob);
		if(item!=null)
		{
			if(item instanceof Ability)
				return item;

			Integer possNum=(Integer)duplicateInventory.get(item);
			if(possNum!=null)
			{
				duplicateInventory.remove(item);
				int possValue=possNum.intValue();
				possValue--;
				if(possValue>1)
				{
					duplicateInventory.put(item,new Integer(possValue));
					item=item.copyOf();
				}
			}
			else
				storeInventory.removeElement(item);
			item.baseEnvStats().setRejuv(0);
			item.envStats().setRejuv(0);
		}
		return item;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		MOB mob=msg.source();
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_VALUE:
			case CMMsg.TYP_SELL:
			{
				if((msg.tool()!=null)
				&&(doISellThis(msg.tool()))
				&&(!(msg.tool() instanceof Coins)))
				{
				    double yourValue=yourValue(mob,msg.tool(),false,true).absoluteGoldPrice;
					if(yourValue<2)
					{
						CommonMsgs.say(this,mob,"I'm not interested.",true,false);
						return false;
					}
					if((msg.targetMinor()==CMMsg.TYP_SELL)&&(yourValue>budgetRemaining))
					{
						if(yourValue>budgetMax)
							CommonMsgs.say(this,mob,"That's way out of my price range! Try AUCTIONing it.",true,false);
						else
							CommonMsgs.say(this,mob,"Sorry, I can't afford that right now.  Try back later.",true,false);
						return false;
					}
					if(msg.tool() instanceof Ability)
					{
						CommonMsgs.say(this,mob,"I'm not interested.",true,false);
						return false;
					}
					if((msg.tool() instanceof Container)&&(((Container)msg.tool()).hasALock()))
					{
						for(int i=0;i<mob.inventorySize();i++)
						{
							Item I=mob.fetchInventory(i);
							if((I!=null)
							&&(I instanceof Key)
							&&(((Key)I).getKey().equals(((Container)msg.tool()).keyName()))&&(I.container()==msg.tool()))
								return true;
						}
						CommonMsgs.say(this,mob,"I won't buy that back unless you put the key in it.",true,false);
						return false;
					}
					if((msg.tool() instanceof Item)&&(msg.source().isMine(msg.tool())))
					{
						FullMsg msg2=new FullMsg(msg.source(),msg.tool(),CMMsg.MSG_DROP,null);
						if(!mob.location().okMessage(mob,msg2))
							return false;
					}
					return super.okMessage(myHost,msg);
				}
				CommonMsgs.say(this,mob,"I'm sorry, I'm not buying those.",true,false);
				return false;
			}
			case CMMsg.TYP_BUY:
			case CMMsg.TYP_VIEW:
			{
				if((msg.tool()!=null)
				&&(doIHaveThisInStock(msg.tool().Name()+"$",mob)))
				{
					if((msg.targetMinor()==CMMsg.TYP_BUY)&&(msg.tool()!=null)&&(!msg.tool().okMessage(myHost,msg)))
					    return false;
					if(msg.targetMinor()!=CMMsg.TYP_VIEW)
					{
						ShopPrice price=yourValue(mob,msg.tool(),true,true);
						if((price.experiencePrice>0)&&(price.experiencePrice>mob.getExperience()))
						{
							CommonMsgs.say(this,mob,"You aren't experienced enough to buy "+msg.tool().name()+".",false,false);
							return false;
						}
						if((price.questPointPrice>0)&&(price.questPointPrice>mob.getQuestPoint()))
						{
							CommonMsgs.say(this,mob,"You don't have enough quest points to buy "+msg.tool().name()+".",false,false);
							return false;
						}
						if((price.absoluteGoldPrice>0.0)
						&&(price.absoluteGoldPrice>BeanCounter.getTotalAbsoluteShopKeepersValue(mob,this)))
						{
							CommonMsgs.say(this,mob,"You can't afford to buy "+msg.tool().name()+".",false,false);
							return false;
						}
					}
					if(msg.tool() instanceof Item)
					{
						if(((Item)msg.tool()).envStats().level()>mob.envStats().level())
						{
							CommonMsgs.say(this,mob,"That's too advanced for you, I'm afraid.",true,false);
							return false;
						}
					}
					if((msg.tool() instanceof LandTitle)
					&&((whatISell==DEAL_CLANDSELLER)||(whatISell==DEAL_CSHIPSELLER)))
					{
						if(mob.getClanID().length()==0)
						{
							CommonMsgs.say(this,mob,"I only sell to clans.",true,false);
							return false;
						}
					}
					if(msg.tool() instanceof MOB)
					{
						if(msg.source().totalFollowers()>=msg.source().maxFollowers())
						{
							CommonMsgs.say(this,mob,"You can't accept any more followers.",true,false);
							return false;
						}
		                if((CommonStrings.getIntVar(CommonStrings.SYSTEMI_FOLLOWLEVELDIFF)>0)
        				&&(!CMSecurity.isAllowed(this,location(),"ORDER"))
        				&&(!CMSecurity.isAllowed(mob,mob.location(),"ORDER")))
                        {
        					if(envStats.level() > (mob.envStats().level() + CommonStrings.getIntVar(CommonStrings.SYSTEMI_FOLLOWLEVELDIFF)))
        					{
        						mob.tell(msg.tool().name() + " is too advanced for you.");
        						return false;
        					}
        					if(envStats.level() < (mob.envStats().level() - CommonStrings.getIntVar(CommonStrings.SYSTEMI_FOLLOWLEVELDIFF)))
        					{
        						mob.tell(msg.tool().name() + " is too inexperienced for you.");
        						return false;
        					}
                        }
					}
					if(msg.tool() instanceof Ability)
					{
						if((whatISell==DEAL_TRAINER)&&(!((Ability)msg.tool()).canBeLearnedBy(new Teacher(),mob)))
							return false;

						if((msg.targetMinor()==CMMsg.TYP_BUY)
						&&(whatISell!=DEAL_TRAINER))
						{
							Ability A=(Ability)msg.tool();
							if(A.canTarget(mob)){}
							else
							if(A.canTarget(CMClass.sampleItem()))
							{
								Item I=mob.fetchWieldedItem();
								if(I==null) I=mob.fetchFirstWornItem(Item.HELD);
								if(I==null)
								{
									CommonMsgs.say(this,mob,"You need to be wielding or holding the item you want this cast on.",true,false);
									return false;
								}
							}
							else
							{
								CommonMsgs.say(this,mob,"I don't know how to sell that spell.",true,false);
								return false;
							}
						}
					}
					return super.okMessage(myHost,msg);
				}
				CommonMsgs.say(this,mob,"I don't have that in stock.  Ask for my LIST.",true,false);
				return false;
			}
			case CMMsg.TYP_LIST:
				return super.okMessage(myHost,msg);
			default:
				break;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public String findInnRoom(InnKey key, String addThis, Room R)
	{
		if(R==null) return null;
		String keyNum=key.getKey();
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			if((R.getExitInDir(d)!=null)&&(R.getExitInDir(d).keyName().equals(keyNum)))
			{
				if(addThis.length()>0)
					return addThis+" and to the "+(Directions.getDirectionName(d).toLowerCase());
				else
					return "to the "+(Directions.getDirectionName(d).toLowerCase());
			}
		}
		return null;
	}
	
	protected double getSalesTax()
	{
		Law theLaw=CoffeeUtensils.getTheLaw(location(),this);
		if(theLaw!=null)
		{
			String taxs=(String)theLaw.taxLaws().get("SALESTAX");
			if(taxs!=null)
				return Util.s_double(taxs);
		}
		return 0.0;
	    
	}


	public Vector removeSellableProduct(String named, MOB mob)
	{
		Vector V=new Vector();
		Environmental product=removeStock(named,mob);
		if(product==null) return V;
		V.addElement(product);
		if(product instanceof Container)
		{
			int i=0;
			Key foundKey=null;
			Container C=((Container)product);
			while(i<storeInventory.size())
			{
				int a=storeInventory.size();
				Environmental I=(Environmental)storeInventory.elementAt(i);
				if((I instanceof Item)&&(((Item)I).container()==product))
				{
					if((I instanceof Key)&&(((Key)I).getKey().equals(C.keyName())))
						foundKey=(Key)I;
					((Item)I).unWear();
					V.addElement(I);
					storeInventory.removeElement(I);
					((Item)I).setContainer((Item)product);
				}
				if(a==storeInventory.size())
					i++;
			}
			if((C.isLocked())&&(foundKey==null))
			{
				String keyName=Double.toString(Math.random());
				C.setKeyName(keyName);
				C.setLidsNLocks(C.hasALid(),true,C.hasALock(),false);
				Key key=(Key)CMClass.getItem("StdKey");
				key.setKey(keyName);
				key.setContainer(C);
				V.addElement(key);
			}
		}
		return V;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
				if((msg.tool()!=null)
				&&((CMSecurity.isAllowed(msg.source(),location(),"ORDER")
					||(CMSecurity.isAllowed(msg.source(),location(),"CMDMOBS")&&(isMonster()))
					||(CMSecurity.isAllowed(msg.source(),location(),"CMDROOMS")&&(isMonster()))))
				&&((doISellThis(msg.tool()))||(whatISell==DEAL_INVENTORYONLY)))
				{
					Item item2=(Item)msg.tool().copyOf();
					storeInventory.addElement(item2);
					if(item2 instanceof InnKey)
						((InnKey)item2).hangOnRack(this);
					return;
				}
				super.executeMsg(myHost,msg);
				break;
			case CMMsg.TYP_VALUE:
				super.executeMsg(myHost,msg);
				CommonMsgs.say(this,mob,"I'll give you "+BeanCounter.nameCurrencyShort(this,yourValue(mob,msg.tool(),false,true).absoluteGoldPrice)+" for "+msg.tool().name()+".",true,false);
				break;
			case CMMsg.TYP_SELL:
				super.executeMsg(myHost,msg);
				if((msg.tool()!=null)&&(doISellThis(msg.tool())))
				{
					double val=yourValue(mob,msg.tool(),false,true).absoluteGoldPrice;
					String currency=BeanCounter.getCurrency(this);
					BeanCounter.giveSomeoneMoney(this,mob,currency,val);
					budgetRemaining=budgetRemaining-Math.round(val);
					mob.recoverEnvStats();
					mob.tell(name()+" pays you "+BeanCounter.nameCurrencyShort(this,val)+" for "+msg.tool().name()+".");
					if(msg.tool() instanceof LandTitle)
					{
						Object removeKey=null;
						for(Enumeration e=titleSets.keys();e.hasMoreElements();)
						{ Object O=e.nextElement();if(titleSets.get(O)==msg.tool()) removeKey=O; break;}
						if(removeKey!=null) titleSets.remove(removeKey);
					}
					if(msg.tool() instanceof Item)
					{
						Item item=(Item)msg.tool();
						Vector V=null;
						if(item instanceof Container)
							V=((Container)item).getContents();
						else
							V=new Vector();
						if(!V.contains(item)) V.addElement(item);
						for(int v=0;v<V.size();v++)
						{
							Item item2=(Item)V.elementAt(v);
							item2.unWear();
							mob.delInventory(item2);
							if(item!=item2)
							{
								item2.setContainer(item);
								storeInventory.addElement(item2);
							}
							else
								storeInventory.addElement(item2);
							if(item2 instanceof InnKey)
								((InnKey)item2).hangOnRack(this);
						}
						item.setContainer(null);
					}
					else
					if(msg.tool() instanceof MOB)
					{
					    MOB newMOB=(MOB)msg.tool().copyOf();
					    newMOB.setStartRoom(null);
					    Ability A=newMOB.fetchEffect("Skill_Enslave");
					    if(A!=null) A.setMiscText("");
					    newMOB.setLiegeID("");
					    newMOB.setClanID("");
						storeInventory.addElement(newMOB);
						((MOB)msg.tool()).setFollowing(null);
						if((((MOB)msg.tool()).baseEnvStats().rejuv()>0)
						&&(((MOB)msg.tool()).baseEnvStats().rejuv()<Integer.MAX_VALUE)
						&&(((MOB)msg.tool()).getStartRoom()!=null))
							((MOB)msg.tool()).killMeDead(false);
						else
							((MOB)msg.tool()).destroy();
					}
					else
					if(msg.tool() instanceof Ability)
					{

					}
					if(mySession!=null)
						mySession.stdPrintln(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
					if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
						mob.location().recoverRoomStats();
				}
				break;
			case CMMsg.TYP_VIEW:
				super.executeMsg(myHost,msg);
				if((msg.tool()!=null)&&(doIHaveThisInStock(msg.tool().Name()+"$",mob)))
				{
					StringBuffer str=new StringBuffer("");
					str.append("Interested in "+msg.tool().name()+"?");
					str.append(" Here is some information for you:");
					str.append("\n\rLevel      : "+msg.tool().envStats().level());
					if(msg.tool() instanceof Item)
					{
						Item I=(Item)msg.tool();
						str.append("\n\rMaterial   : "+Util.capitalize(EnvResource.RESOURCE_DESCS[I.material()&EnvResource.RESOURCE_MASK].toLowerCase()));
						str.append("\n\rWeight     : "+I.envStats().weight()+" pounds");
						if(I instanceof Weapon)
						{
							str.append("\n\rWeap. Type : "+Util.capitalize(Weapon.typeDescription[((Weapon)I).weaponType()]));
							str.append("\n\rWeap. Class: "+Util.capitalize(Weapon.classifictionDescription[((Weapon)I).weaponClassification()]));
						}
						else
						if(I instanceof Armor)
						{
							str.append("\n\rWear Info  : Worn on ");
							for(int l=0;l<20;l++)
							{
								int wornCode=1<<l;
								if(Sense.wornLocation(wornCode).length()>0)
								{
									if(((I.rawProperLocationBitmap()&wornCode)==wornCode))
									{
										str.append(Util.capitalize(Sense.wornLocation(wornCode))+" ");
										if(I.rawLogicalAnd())
											str.append("and ");
										else
											str.append("or ");
									}
								}
							}
							if(str.toString().endsWith(" and "))
								str.delete(str.length()-5,str.length());
							else
							if(str.toString().endsWith(" or "))
								str.delete(str.length()-4,str.length());
						}
					}
					str.append("\n\rDescription: "+msg.tool().description());
					CommonMsgs.say(this,msg.source(),str.toString(),true,false);
				}
				break;
			case CMMsg.TYP_BUY:
				super.executeMsg(myHost,msg);
				MOB mobFor=msg.source();
				if((msg.targetMessage()!=null)&&(msg.targetMessage().length()>0)&&(location()!=null))
				{
					Vector V=Util.parse(msg.targetMessage());
					if(((String)V.elementAt(V.size()-2)).equalsIgnoreCase("for"))
					{
						String s=(String)V.lastElement();
						if(s.endsWith(".")) s=s.substring(0,s.length()-1);
						MOB M=location().fetchInhabitant(s+"$");
						if(M!=null) 
							mobFor=M;
					}
				}
				if((msg.tool()!=null)
				&&(doIHaveThisInStock(msg.tool().Name()+"$",mobFor))
				&&(location()!=null))
				{
					Vector products=removeSellableProduct(msg.tool().Name()+"$",mobFor);
					if(products.size()==0) break;
					Environmental product=(Environmental)products.firstElement();
					ShopPrice price=yourValue(mob,product,true,true);
					if(price.absoluteGoldPrice>0.0) 
					{
					    BeanCounter.subtractMoney(mob,BeanCounter.getCurrency(this),price.absoluteGoldPrice);
					    if(getSalesTax()!=0.0)
					    {
							Law theLaw=CoffeeUtensils.getTheLaw(location(),this);
						    Area A2=CoffeeUtensils.getLegalObject(location());
							if((theLaw!=null)&&(A2!=null))
							{
								Environmental[] Treas=theLaw.getTreasuryNSafe(A2);
								Room treasuryR=(Room)Treas[0];
								Item treasuryItem=(Item)Treas[1];
					            if(treasuryR!=null)
					            {
					                Coins COIN=BeanCounter.makeBestCurrency(BeanCounter.getCurrency(this),price.absoluteGoldPrice-yourValue(mob,product,true,false).absoluteGoldPrice,treasuryR,treasuryItem);
				    				if(COIN!=null) COIN.putCoinsBack();
					            }
							}
					    }
					}
					if(price.questPointPrice>0) mob.setQuestPoint(mob.getQuestPoint()-price.questPointPrice);
					if(price.experiencePrice>0) MUDFight.postExperience(mob,null,null,-price.experiencePrice,false);
					mob.recoverEnvStats();
					if(product instanceof Item)
					{
						for(int p=0;p<products.size();p++)
							location().addItemRefuse((Item)products.elementAt(p),Item.REFUSE_PLAYER_DROP);
						FullMsg msg2=new FullMsg(mobFor,product,this,CMMsg.MSG_GET,null);
						if((product instanceof LandTitle)||(location().okMessage(mobFor,msg2)))
						{
							if(product instanceof LandTitle)
							{
								Object removeKey=null;
								for(Enumeration e=titleSets.keys();e.hasMoreElements();)
								{ Object O=e.nextElement();if(titleSets.get(O)==product) removeKey=O; break;}
								if(removeKey!=null) titleSets.remove(removeKey);
							}
							tell(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
							location().send(mobFor,msg2);
							if((msg.tool() instanceof InnKey)&&(location()!=null))
							{
								InnKey item =(InnKey)msg.tool();
								String buf=findInnRoom(item, "", location());
								if(buf==null) buf=findInnRoom(item, "upstairs", location().getRoomInDir(Directions.UP));
								if(buf==null) buf=findInnRoom(item, "downstairs", location().getRoomInDir(Directions.DOWN));
								if(buf!=null) CommonMsgs.say(this,mobFor,"Your room is "+buf+".",true,false);
							}
						}
						else
							return;
					}
					else
					if(product instanceof MOB)
					{
						MOB newMOB=(MOB)product;
						newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
						product.recoverEnvStats();
						product.setMiscText(product.text());
						Ability slaveA=null;
						if(whatISell==ShopKeeper.DEAL_SLAVES)
						{
						    slaveA=newMOB.fetchEffect("Skill_Enslave");
						    if(slaveA!=null) slaveA.setMiscText("");
						    else
						    if(!Sense.isAnimalIntelligence(newMOB))
						    {
						        slaveA=CMClass.getAbility("Skill_Enslave");
						        if(slaveA!=null)
						            newMOB.addNonUninvokableEffect(slaveA);
						    }
						}
						((MOB)product).bringToLife(location(),true);
					    if(slaveA!=null)
					    {
						    newMOB.setLiegeID("");
						    newMOB.setClanID("");
						    newMOB.setStartRoom(null);
					        slaveA.setMiscText(mobFor.Name());
				            newMOB.text();
					    }
						CommonMsgs.follow(newMOB,mobFor,false);
						if(newMOB.amFollowing()==null)
							mobFor.tell("You cannot accept seem to accept this follower!");
					}
					else
					if(product instanceof Ability)
					{
						Ability A=(Ability)product;
						if(whatISell==DEAL_TRAINER)
							A.teach(new Teacher(),mobFor);
						else
						{
							curState().setMana(maxState().getMana());
							curState().setMovement(maxState().getMovement());
							Vector V=new Vector();
							if(A.canTarget(mobFor))
							{
								V.addElement(mobFor.name()+"$");
								A.invoke(this,V,mobFor,true,0);
							}
							else
							if(A.canTarget(CMClass.sampleItem()))
							{
								Item I=mobFor.fetchWieldedItem();
								if(I==null) I=mobFor.fetchFirstWornItem(Item.HELD);
								if(I==null) I=mobFor.fetchWornItem("all");
								if(I==null) I=mobFor.fetchCarried(null,"all");
								if(I==null) return;
								V.addElement(I.name()+"$");
								addInventory(I);
								A.invoke(this,V,I,true,0);
								delInventory(I);
								if(!mobFor.isMine(I)) mobFor.addInventory(I);
							}
							curState().setMana(maxState().getMana());
							curState().setMovement(maxState().getMovement());
						}
					}

					if(mySession!=null)
						mySession.stdPrintln(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
					if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
						mob.location().recoverRoomStats();
				}
				break;
			case CMMsg.TYP_LIST:
				{
					super.executeMsg(myHost,msg);
					StringBuffer str=new StringBuffer("");
					int csize=0;
					Vector inventory=getUniqueStoreInventory();
					inventory=addRealEstate(inventory,mob);
					if(inventory.size()>0)
					{
						int totalCols=((whatISell==DEAL_LANDSELLER)
									   ||(whatISell==DEAL_CLANDSELLER)
									   ||(whatISell==DEAL_SHIPSELLER)
									   ||(whatISell==DEAL_CSHIPSELLER))?1:2;
						int totalWidth=60/totalCols;
						int limit=Util.getParmInt(prejudiceFactors(),"LIMIT",0);
						String showPrice=null;
						ShopPrice price=null;
						Environmental E=null;
						for(int i=0;i<inventory.size();i++)
						{
							E=(Environmental)inventory.elementAt(i);
							if(!((E instanceof Item)&&((((Item)E).container()!=null)||(!Sense.canBeSeenBy(E,mob)))))
							{
								price=yourValue(mob,E,true,true);
								if((price.experiencePrice>0)&&(((""+price.experiencePrice).length()+2)>(4+csize)))
									csize=(""+price.experiencePrice).length()-2;
								else
								if((price.questPointPrice>0)&&(((""+price.questPointPrice).length()+2)>(4+csize)))
									csize=(""+price.questPointPrice).length()-2;
								else
								{
								    showPrice=BeanCounter.abbreviatedPrice(this,price.absoluteGoldPrice);
									if(showPrice.length()>(4+csize))
										csize=showPrice.length()-4;
								}
							}
						}
	
						String c="^x["+Util.padRight("Cost",4+csize)+"] "+Util.padRight("Product",totalWidth-csize);
						str.append(c+((totalCols>1)?c:"")+"^.^N^<!ENTITY Shopkeeper \""+name()+"\"^>\n\r");
						int colNum=0;
						int rowNum=0;
						String col=null;
						for(int i=0;i<inventory.size();i++)
						{
							E=(Environmental)inventory.elementAt(i);
	
							if(!((E instanceof Item)&&((((Item)E).container()!=null)||(!Sense.canBeSeenBy(E,mob)))))
							{
								price=yourValue(mob,E,true,true);
								col=null;
								if(price.questPointPrice>0)
									col=Util.padRight("["+price.questPointPrice+"qp",5+csize)+"] "+"^<SHOP^>"+Util.padRight(E.name(),"^</SHOP^>",totalWidth-csize);
								else
								if(price.experiencePrice>0)
									col=Util.padRight("["+price.experiencePrice+"xp",5+csize)+"] "+"^<SHOP^>"+Util.padRight(E.name(),"^</SHOP^>",totalWidth-csize);
								else
									col=Util.padRight("["+BeanCounter.abbreviatedPrice(this,price.absoluteGoldPrice),5+csize)+"] "+"^<SHOP^>"+Util.padRight(E.name(),"^</SHOP^>",totalWidth-csize);
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
					}
					if(str.length()==0)
					{
						if((whatISell!=DEAL_BANKER)
						&&(whatISell!=DEAL_CLANBANKER))
							CommonMsgs.say(this,mob,"I have nothing for sale.",false,false);
					}
					else
					{
					    double salesTax=getSalesTax();
						mob.tell("\n\r"+str
						        +((salesTax<=0.0)?"":"\n\r\n\rPrices above include a "+salesTax+"% sales tax.")
						        +"^T");
					}
				}
				break;
			default:
				super.executeMsg(myHost,msg);
				break;
			}
		}
		else
			super.executeMsg(myHost,msg);
	}

	protected double prejudiceValueFromPart(MOB mob, boolean sellTo, String part)
	{
		int x=part.indexOf("=");
		if(x<0) return 0.0;
		String sellorby=part.substring(0,x);
		part=part.substring(x+1);
		if(sellTo&&(!sellorby.trim().equalsIgnoreCase("SELL")))
		   return 0.0;
		if((!sellTo)&&(!sellorby.trim().equalsIgnoreCase("BUY")))
		   return 0.0;
		if(part.trim().indexOf(" ")<0)
			return Util.s_double(part.trim());
		Vector V=Util.parse(part.trim());
		double d=0.0;
		boolean yes=false;
		for(int v=0;v<V.size();v++)
		{
			String bit=(String)V.elementAt(v);
			if(Util.s_double(bit)!=0.0)
				d=Util.s_double(bit);
            if(bit.equalsIgnoreCase(mob.charStats().getCurrentClass().name() ))
			{ yes=true; break;}
			if(bit.equalsIgnoreCase(mob.charStats().getMyRace().racialCategory()))
			{	yes=true; break;}
			if(bit.equalsIgnoreCase(CommonStrings.shortAlignmentStr(mob.getAlignment())))
			{ yes=true; break;}
		}
		if(yes) return d;
		return 0.0;

	}
	protected double prejudiceFactor(MOB mob, boolean sellTo)
	{
		String factors=prejudiceFactors().toUpperCase();
		if(factors.length()==0) 
		{
			factors=CommonStrings.getVar(CommonStrings.SYSTEM_PREJUDICE).trim();
			if(factors.length()==0)
				return 1.0;
		}
		if(factors.indexOf("=")<0)
		{
			if(Util.s_double(factors)!=0.0)
				return Util.s_double(factors);
			return 1.0;
		}
		int x=factors.indexOf(";");
		while(x>=0)
		{
			String part=factors.substring(0,x).trim();
			factors=factors.substring(x+1).trim();
			double d=prejudiceValueFromPart(mob,sellTo,part);
			if(d!=0.0) return d;
			x=factors.indexOf(";");
		}
		double d=prejudiceValueFromPart(mob,sellTo,factors.trim());
		if(d!=0.0) return d;
		return 1.0;
	}

	public double devalue(Environmental product)
	{
		int num=numberInStock(product);
		if(num<=0) return 0.0;
		double resourceRate=0.0;
		double itemRate=0.0;
		String s=devalueRate();
		if(s.length()==0) s=CommonStrings.getVar(CommonStrings.SYSTEM_DEVALUERATE);
		Vector V=Util.parse(s.trim());
		if(V.size()<=0)
			return 0.0;
		else
		if(V.size()==1)
		{
			resourceRate=Util.div(Util.s_double((String)V.firstElement()),100.0);
			itemRate=resourceRate;
		}
		else
		{
			itemRate=Util.div(Util.s_double((String)V.firstElement()),100.0);
			resourceRate=Util.div(Util.s_double((String)V.lastElement()),100.0);
		}
		double rate=(product instanceof EnvResource)?resourceRate:itemRate;
		rate=rate*num;
		if(rate>1.0) rate=1.0;
		if(rate<0.0) rate=0.0;
		return rate;
	}
	
	public ShopPrice yourValue(MOB mob, Environmental product, boolean sellTo, boolean includeTax)
	{
	    ShopPrice val=new ShopPrice();
		if(product==null) return val;
		Integer I=(Integer)prices.get(product.ID()+"/"+product.name());
		if((I!=null)&&(I.intValue()<=-100))
		{
			if(I.intValue()<=-1000)
				val.experiencePrice=(I.intValue()*-1)-1000;
			else
				val.questPointPrice=(I.intValue()*-1)-100;
			return val;
		}

		if(product instanceof Item)
		    val.absoluteGoldPrice=((Item)product).value();
		else
		if(product instanceof Ability)
		{
			if(whatISell==DEAL_TRAINER)
			    val.absoluteGoldPrice=CMAble.lowestQualifyingLevel(product.ID())*100;
			else
			    val.absoluteGoldPrice=CMAble.lowestQualifyingLevel(product.ID())*75;
		}
		else
		if(product instanceof MOB)
		{
			Ability A=product.fetchEffect("Prop_Retainable");
			if(A!=null)
			{
				if(A.text().indexOf(";")<0)
				{
				    if(Util.isDouble(A.text()))
				        val.absoluteGoldPrice=Util.s_double(A.text());
				    else
				        val.absoluteGoldPrice=new Integer(Util.s_int(A.text())).doubleValue();
				}
				else
				{
				    String s2=A.text().substring(0,A.text().indexOf(";"));
				    if(Util.isDouble(s2))
				        val.absoluteGoldPrice=Util.s_double(s2);
				    else
				        val.absoluteGoldPrice=new Integer(Util.s_int(s2)).doubleValue();
				}
			}
			if(val.absoluteGoldPrice==0.0)
			    val.absoluteGoldPrice=25.0*product.envStats().level();
		}
		else
		    val.absoluteGoldPrice=CMAble.lowestQualifyingLevel(product.ID())*25;
		if((I!=null)&&(I.intValue()>=0))
		    val.absoluteGoldPrice=I.intValue();

		if(mob==null) 
		{
		    if(sellTo&&(val.absoluteGoldPrice>0.0))
		        val.absoluteGoldPrice=BeanCounter.abbreviatedRePrice(this,val.absoluteGoldPrice);
		    return val;
		}

		double d=prejudiceFactor(mob,sellTo);
		val.absoluteGoldPrice=Util.mul(d,val.absoluteGoldPrice);

		//double halfPrice=Math.round(Util.div(val,2.0));
		// gets the shopkeeper a deal on junk.  Pays 5% at 3 charisma, and 50% at 30
		double buyPrice=Util.div(Util.mul(val.absoluteGoldPrice,mob.charStats().getStat(CharStats.CHARISMA)),60.0);
        if(!(product instanceof Ability))
			buyPrice=Util.mul(buyPrice,1.0-devalue(product));

		// the price is 200% at 0 charisma, and 100% at 30
		double sellPrice=val.absoluteGoldPrice
						+val.absoluteGoldPrice
						-Util.mul(val.absoluteGoldPrice,Util.div(mob.charStats().getStat(CharStats.CHARISMA),30.0));

		if(buyPrice>sellPrice)buyPrice=sellPrice;

		if(sellTo)
		    val.absoluteGoldPrice=sellPrice+(includeTax?(Util.mul(val.absoluteGoldPrice,Util.div(getSalesTax(),100.0))):0.0);
		else
		    val.absoluteGoldPrice=buyPrice;

		if(val.absoluteGoldPrice<=0.0) 
		    val.absoluteGoldPrice=1.0;
		else
		if(sellTo)
	        val.absoluteGoldPrice=BeanCounter.abbreviatedRePrice(this,val.absoluteGoldPrice);
		return val;
	}

	protected Vector addRealEstate(Vector V,MOB mob)
	{
		if(((whatISell==DEAL_LANDSELLER)
			||(whatISell==DEAL_SHIPSELLER)
			||((whatISell==DEAL_CSHIPSELLER)&&(mob.getClanID().length()>0))
			||((whatISell==DEAL_CLANDSELLER)&&(mob.getClanID().length()>0)))
		&&(getStartRoom()!=null)
		&&(getStartRoom().getArea()!=null))
		{
			String name=mob.Name();
			if((whatISell==DEAL_CLANDSELLER)||(whatISell==DEAL_CSHIPSELLER))
				name=mob.getClanID();
			HashSet roomsHandling=new HashSet();
			Hashtable titles=new Hashtable();
			if((whatISell==DEAL_CSHIPSELLER)||(whatISell==DEAL_SHIPSELLER))
			{
				Area myArea=getStartRoom().getArea();
				for(Enumeration r=CMMap.areas();r.hasMoreElements();)
				{
					Area A=(Area)r.nextElement();
					if((A instanceof SpaceShip)
					&&(Sense.isHidden(A)))
					{
						boolean related=myArea.isChild(A)||A.isParent(myArea);
						if(!related)
							for(int p=0;p<myArea.getNumParents();p++)
							{
								Area P=myArea.getParent(p);
								if((P!=null)&&(P!=myArea)&&((P==A)||(A.isParent(P))||(P.isChild(A))))
								{ related=true; break;}
							}
						if(related)
						{
							LandTitle LT=CoffeeUtensils.getLandTitle(A);
							if(LT!=null) titles.put(A,LT);
						}
					}
				}
			}
			else
				for(Enumeration r=getStartRoom().getArea().getProperMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					LandTitle A=CoffeeUtensils.getLandTitle(R);
					if((A!=null)&&(R.roomID().length()>0))
						titles.put(R,A);
				}

			for(Enumeration r=titles.keys();r.hasMoreElements();)
			{
				Environmental R=(Environmental)r.nextElement();
				LandTitle A=(LandTitle)titles.get(R);
				if(!roomsHandling.contains(R))
				{
					if(R instanceof Area)
						roomsHandling.add(R);
					else
					{
						Vector V2=A.getPropertyRooms();
						for(int v=0;v<V2.size();v++)
							roomsHandling.add(V2.elementAt(v));
					}
					Item I=(Item)titleSets.get(A);
					if(I==null)
					{
						I=CMClass.getItem("GenTitle");
						if(R instanceof Room)
							((LandTitle)I).setLandPropertyID(CMMap.getExtendedRoomID((Room)R));
						else
							((LandTitle)I).setLandPropertyID(R.Name());
						if((((LandTitle)I).landOwner().length()>0)
						&&(!I.Name().endsWith(" (Copy)")))
							I.setName(I.Name()+" (Copy)");
						I.text();
						I.recoverEnvStats();
						titleSets.put(A,I);
					}
					if((A.landOwner().length()>0)
					&&(!A.landOwner().equals(name))
					&&((!A.landOwner().equals(mob.getLiegeID()))||(!mob.isMarriedToLiege())))
						continue;
					else
					{
						boolean skipThisOne=false;
						if(R instanceof Room)
							for(int d=0;d<4;d++)
							{
								Room R2=((Room)R).getRoomInDir(d);
								LandTitle L2=null;
								if(R2!=null)
								{
									L2=(LandTitle)titles.get(R2);
									if(L2==null)
									{ skipThisOne=false; break;}
								}
								else
									continue;
								if((L2.landOwner().equals(name))
								||(L2.landOwner().equals(mob.getLiegeID())&&(mob.isMarriedToLiege())))
								{ skipThisOne=false; break;}
								if(L2.landOwner().length()>0)
									skipThisOne=true;
							}
						if(skipThisOne) continue;
					}
					if((A.landOwner().length()==0)
					&&(I.Name().endsWith(" (Copy)")))
						I.setName(I.Name().substring(0,I.Name().length()-7));

					V.addElement(I);
				}
			}
		}
		return V;
	}
	
	public String prejudiceFactors(){return Util.decompressString(miscText);}
	public void setPrejudiceFactors(String factors){miscText=Util.compressString(factors);}

	public String budget(){return budget;}
	public void setBudget(String factors){budget=factors; budgetTickDown=0;}
	
	public String devalueRate(){return devalueRate;}
	public void setDevalueRate(String factors){devalueRate=factors;}
	
	public int invResetRate(){return invResetRate;}
	public void setInvResetRate(int ticks){invResetRate=ticks; invResetTickDown=0;}
	
}
