package com.planet_ink.coffee_mud.MOBS;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class StdShopKeeper extends StdMOB implements ShopKeeper
{
	protected int whatISell=0;
	private Vector storeInventory=new Vector();
	protected Vector baseInventory=new Vector();
	private Hashtable duplicateInventory=new Hashtable();
	protected int maximumDuplicatesBought=5;

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

	public Environmental newInstance()
	{
		return new StdShopKeeper();
	}

	public int whatIsSold(){return whatISell;}
	public void setWhatIsSold(int newSellCode){whatISell=newSellCode;}

	private boolean inBaseInventory(Environmental thisThang)
	{
		for(int x=0;x<baseInventory.size();x++)
		{
			Environmental E=(Environmental)baseInventory.elementAt(x);
			if((thisThang.isGeneric())&&(E.isGeneric()))
			{
				if(thisThang.name().equals(E.name()))
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
		addStoreInventory(thisThang,1);
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
					if(E.name().equals(lastE.name()))
						ok=false;
				}
				else
				if(CMClass.className(lastE).equals(CMClass.className(E)))
					ok=false;
			}

			if(ok)
			for(int v=0;v<V.size();v++)
			{
				Environmental EE=(Environmental)V.elementAt(v);
				if((EE.isGeneric())&&(E.isGeneric()))
				{
					if(E.name().equals(EE.name()))
					{
						ok=false;
						break;
					}
				}
				else
				if(CMClass.className(EE).equals(CMClass.className(E)))
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
			return "Pets";
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
		case DEAL_JEWELLER:
			return "Precious stones and jewellery";
		case DEAL_BANKER:
			return "My services as a Banker";
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
		default:
			return "... I have no idea WHAT I sell";
		}
	}

	public void addStoreInventory(Environmental thisThang, int number)
	{
		if((whatISell==DEAL_INVENTORYONLY)&&(!inBaseInventory(thisThang)))
			baseInventory.addElement(thisThang.copyOf());
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
			if(number>maximumDuplicatesBought)
				maximumDuplicatesBought=number;
			if(number>1)
				duplicateInventory.put(copy,new Integer(number));
		}
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
					if(thisThang.name().equals(E.name()))
						baseInventory.removeElement(E);
				}
				else
				if(CMClass.className(thisThang).equals(CMClass.className(E)))
					baseInventory.removeElement(E);
			}
		}
		for(int v=storeInventory.size()-1;v>=0;v--)
		{
			Environmental E=(Environmental)storeInventory.elementAt(v);
			if((thisThang.isGeneric())&&(E.isGeneric()))
			{
				if(thisThang.name().equals(E.name()))
				{
					storeInventory.removeElement(E);
					duplicateInventory.remove(E);
				}
			}
			else
			if(CMClass.className(thisThang).equals(CMClass.className(E)))
			{
				storeInventory.removeElement(E);
				duplicateInventory.remove(E);
			}
		}
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
			return (thisThang instanceof Weapon);
		case DEAL_GENERAL:
			return ((thisThang instanceof Item)
					&&(!(thisThang instanceof Armor))
					&&(!(thisThang instanceof MiscMagic))
					&&(!(thisThang instanceof Weapon))
					&&(!(thisThang instanceof MOB))
					&&(!(thisThang instanceof EnvResource))
					&&(!(thisThang instanceof Ability)));
		case DEAL_LEATHER:
			return ((thisThang instanceof Item)
					&&((((Item)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LEATHER)
					&&(!(thisThang instanceof EnvResource)));
		case DEAL_PETS:
			return (thisThang instanceof MOB);
		case DEAL_INVENTORYONLY:
			return (inBaseInventory(thisThang));
		case DEAL_JEWELLER:
			return ((thisThang instanceof Item)
					&&(!(thisThang instanceof Weapon))
					&&(!(thisThang instanceof MiscMagic))
					&&(((((Item)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_GLASS)
					||((((Item)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_PRECIOUS)
					||((Item)thisThang).canBeWornAt(Item.ON_EARS)
					||((Item)thisThang).canBeWornAt(Item.ON_NECK)
					||((Item)thisThang).canBeWornAt(Item.ON_RIGHT_FINGER)
					||((Item)thisThang).canBeWornAt(Item.ON_LEFT_FINGER)));
		case DEAL_ALCHEMIST:
			return (thisThang instanceof Potion);
		case DEAL_LANDSELLER:
		case DEAL_CLANDSELLER:
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
		Environmental item=CoffeeUtensils.fetchEnvironmental(storeInventory,name,true);
		if(item==null)
			item=CoffeeUtensils.fetchEnvironmental(storeInventory,name,false);
		if((item==null)
		   &&(mob!=null)
		   &&((whatISell==DEAL_LANDSELLER)||(whatISell==DEAL_CLANDSELLER)))
		{
			item=CoffeeUtensils.fetchEnvironmental(addRealEstate(new Vector(),mob),name,true);
			if(item==null)
				item=CoffeeUtensils.fetchEnvironmental(addRealEstate(new Vector(),mob),name,false);
		}
		if(item!=null)
		   return true;
		return false;
	}

	public int numberInStock(Environmental likeThis)
	{
		int num=0;
		for(int v=0;v<storeInventory.size();v++)
		{
			Environmental E=(Environmental)storeInventory.elementAt(v);
			if((likeThis.isGeneric())&&(E.isGeneric()))
			{
				if(E.name().equals(likeThis.name()))
				{
					Integer possNum=(Integer)duplicateInventory.get(E);
					if(possNum!=null)
						num+=possNum.intValue();
					else
						num++;
				}
			}
			else
			if(CMClass.className(E).equals(CMClass.className(likeThis)))
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
		Environmental item=CoffeeUtensils.fetchEnvironmental(storeInventory,name,true);
		if(item==null)
			item=CoffeeUtensils.fetchEnvironmental(storeInventory,name,false);
		if((item==null)
		   &&((whatISell==DEAL_LANDSELLER)||(whatISell==DEAL_CLANDSELLER))
		   &&(mob!=null))
		{
			item=CoffeeUtensils.fetchEnvironmental(addRealEstate(new Vector(),mob),name,true);
			if(item==null)
				item=CoffeeUtensils.fetchEnvironmental(addRealEstate(new Vector(),mob),name,false);
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
		}
		return item;
	}
	
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		MOB mob=affect.source();
		if(affect.amITarget(this))
		{
			switch(affect.targetMinor())
			{
			case Affect.TYP_VALUE:
			case Affect.TYP_SELL:
			{
				if((affect.tool()!=null)&&(doISellThis(affect.tool())))
				{
					if(yourValue(mob,affect.tool(),false)<2)
					{
						ExternalPlay.quickSay(this,mob,"I'm not interested.",true,false);
						return false;
					}
					if(affect.tool() instanceof Ability)
					{
						ExternalPlay.quickSay(this,mob,"I'm not interested.",true,false);
						return false;
					}
					if((numberInStock(affect.tool()))>=maximumDuplicatesBought)
					{
						ExternalPlay.quickSay(this,mob,"I'm sorry, I'm not buying any more of those.",true,false);
						return false;
					}
					if((affect.tool() instanceof Container)&&(((Container)affect.tool()).hasALock()))
					{
						for(int i=0;i<mob.inventorySize();i++)
						{
							Item I=mob.fetchInventory(i);
							if((I!=null)
							&&(I instanceof Key)
							&&(((Key)I).getKey().equals(((Container)affect.tool()).keyName()))&&(I.container()==affect.tool()))
								return true;
						}
						ExternalPlay.quickSay(this,mob,"I won't buy that back unless you put the key in it.",true,false);
						return false;
					}
					if((affect.tool() instanceof Item)&&(affect.source().isMine(affect.tool())))
					{
						FullMsg msg=new FullMsg(affect.source(),affect.tool(),Affect.MSG_DROP,null);
						if(!mob.location().okAffect(mob,msg))
							return false;
					}
					return super.okAffect(myHost,affect);
				}
				ExternalPlay.quickSay(this,mob,"I'm sorry, I'm not buying those.",true,false);
				return false;
			}
			case Affect.TYP_BUY:
			case Affect.TYP_VIEW:
			{
				if((affect.tool()!=null)
				&&(doIHaveThisInStock(affect.tool().name(),mob)))
				{
					if((affect.targetMinor()!=Affect.TYP_VIEW)
					&&(yourValue(mob,affect.tool(),true)>com.planet_ink.coffee_mud.utils.Money.totalMoney(mob)))
					{
						ExternalPlay.quickSay(this,mob,"You can't afford to buy "+affect.tool().name()+".",false,false);
						return false;
					}
					if(affect.tool() instanceof Item)
					{
						if(((Item)affect.tool()).envStats().level()>mob.envStats().level())
						{
							ExternalPlay.quickSay(this,mob,"That's too advanced for you, I'm afraid.",true,false);
							return false;
						}
					}
					if(affect.tool() instanceof MOB)
					{
						if(affect.source().numFollowers()>=affect.source().maxFollowers())
						{
							ExternalPlay.quickSay(this,mob,"You can't accept any more followers.",true,false);
							return false;
						}
					}
					if(affect.tool() instanceof Ability)
					{
						if((whatISell==DEAL_TRAINER)&&(!((Ability)affect.tool()).canBeLearnedBy(new Teacher(),mob)))
							return false;

						if(affect.targetMinor()==Affect.TYP_BUY)
						{
							Ability A=(Ability)affect.tool();
							if(A.canTarget(mob)){}
							else
							if(A.canTarget(CMClass.getItem("StdItem")))
							{
								Item I=mob.fetchWieldedItem();
								if(I==null) I=mob.fetchWornItem(Item.HELD);
								if(I==null)
								{
									ExternalPlay.quickSay(this,mob,"You need to be wielding or holding the item you want this cast on.",true,false);
									return false;
								}
							}
							else
							{
								ExternalPlay.quickSay(this,mob,"I don't know how to sell that spell.",true,false);
								return false;
							}
						}
					}
					return super.okAffect(myHost,affect);
				}
				ExternalPlay.quickSay(this,mob,"I don't have that in stock.  Ask for my LIST.",true,false);
				return false;
			}
			case Affect.TYP_LIST:
				return super.okAffect(myHost,affect);
			default:
				break;
			}
		}
		return super.okAffect(myHost,affect);
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
					((Item)I).remove();
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
	
	public void affect(Environmental myHost, Affect affect)
	{
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetMinor())
			{
			case Affect.TYP_GIVE:
				if((affect.tool()!=null)
				&&((doISellThis(affect.tool())))
				||((whatISell==DEAL_INVENTORYONLY)&&(mob.isASysOp(mob.location()))))
				{
					storeInventory.addElement(affect.tool());
					return;
				}
				super.affect(myHost,affect);
				break;
			case Affect.TYP_VALUE:
				super.affect(myHost,affect);
				ExternalPlay.quickSay(this,mob,"I'll give you "+yourValue(mob,affect.tool(),false)+" for "+affect.tool().name()+".",true,false);
				break;
			case Affect.TYP_SELL:
				super.affect(myHost,affect);
				if((affect.tool()!=null)&&(doISellThis(affect.tool())))
				{
					mob.setMoney(mob.getMoney()+yourValue(mob,affect.tool(),false));
					mob.recoverEnvStats();
					mob.tell(name()+" pays you "+yourValue(mob,affect.tool(),false)+" for "+affect.tool().name()+".");
					if(affect.tool() instanceof Item)
					{
						Item item=(Item)affect.tool();
						Vector V=null;
						if(item instanceof Container)
							V=((Container)item).getContents();
						else
							V=new Vector();
						if(!V.contains(item)) V.addElement(item);
						for(int v=0;v<V.size();v++)
						{
							Item item2=(Item)V.elementAt(v);
							item2.remove();
							mob.delInventory(item2);
							if(item!=item2)
							{
								item2.setContainer(item);
								storeInventory.addElement(item2);
							}
							else
								storeInventory.addElement(item2);
						}
						item.setContainer(null);
					}
					else
					if(affect.tool() instanceof MOB)
					{
						storeInventory.addElement(((MOB)affect.tool()).copyOf());
						((MOB)affect.tool()).setFollowing(null);
						if((((MOB)affect.tool()).baseEnvStats().rejuv()>0)
						&&(((MOB)affect.tool()).baseEnvStats().rejuv()<Integer.MAX_VALUE)
						&&(((MOB)affect.tool()).getStartRoom()!=null))
							((MOB)affect.tool()).killMeDead(false);
						else
							((MOB)affect.tool()).destroy();
					}
					else
					if(affect.tool() instanceof Ability)
					{

					}
					if(mySession!=null)
						mySession.stdPrintln(affect.source(),affect.target(),affect.tool(),affect.targetMessage());
					mob.location().recoverRoomStats();
				}
				break;
			case Affect.TYP_VIEW:
				super.affect(myHost,affect);
				if((affect.tool()!=null)&&(doIHaveThisInStock(affect.tool().name(),mob)))
					ExternalPlay.quickSay(this,affect.source(),"Interested in "+affect.tool().name()+"? Here is some information for you:\n\rLevel "+affect.tool().envStats().level()+"\n\rDescription: "+affect.tool().description(),true,false);
				break;
			case Affect.TYP_BUY:
				super.affect(myHost,affect);
				if((affect.tool()!=null)
				&&(doIHaveThisInStock(affect.tool().name(),mob)))
				{
					Vector products=removeSellableProduct(affect.tool().name(),mob);
					if(products.size()==0) break;
					Environmental product=(Environmental)products.firstElement();
					com.planet_ink.coffee_mud.utils.Money.subtractMoney(this,mob,yourValue(mob,product,true));
					mob.recoverEnvStats();
					if(product instanceof Item)
					{
						for(int p=0;p<products.size();p++)
						{
							Item I=(Item)products.elementAt(p);
							mob.location().addItemRefuse(I,Item.REFUSE_PLAYER_DROP);
						}
						FullMsg msg=new FullMsg(mob,product,this,Affect.MSG_GET,null);
						if(location().okAffect(mob,msg))
						{
							tell(affect.source(),affect.target(),affect.tool(),affect.targetMessage());
							location().send(mob,msg);
							if((affect.tool() instanceof InnKey)&&(location()!=null))
							{
								InnKey item =(InnKey)affect.tool();
								String buf=findInnRoom(item, "", location());
								if(buf==null) buf=findInnRoom(item, "upstairs", location().getRoomInDir(Directions.UP));
								if(buf==null) buf=findInnRoom(item, "downstairs", location().getRoomInDir(Directions.DOWN));
								if(buf!=null) ExternalPlay.quickSay(this,mob,"Your room is "+buf+".",true,false);
							}
						}
						else
							return;
					}
					else
					if(product instanceof MOB)
					{
						((MOB)product).baseEnvStats().setRejuv(Integer.MAX_VALUE);
						product.recoverEnvStats();
						product.setMiscText(product.text());
						((MOB)product).bringToLife(mob.location(),true);
						ExternalPlay.follow((MOB)product,mob,false);
						if(((MOB)product).amFollowing()==null)
							mob.tell("You cannot accept any more followers!");
					}
					else
					if(product instanceof Ability)
					{
						Ability A=(Ability)product;
						if(whatISell==DEAL_TRAINER)
							A.teach(new Teacher(),mob);
						else
						{
							curState().setMana(maxState().getMana());
							Vector V=new Vector();
							if(A.canTarget(mob))
							{
								V.addElement(mob.name()+"$");
								A.invoke(this,V,mob,true);
							}
							else
							if(A.canTarget(CMClass.getItem("StdItem")))
							{
								Item I=mob.fetchWieldedItem();
								if(I==null) I=mob.fetchWornItem(Item.HELD);
								if(I==null) return;
								V.addElement(I.name()+"$");
								addInventory(I);
								A.invoke(this,V,I,true);
								delInventory(I);
								if(!mob.isMine(I)) mob.addInventory(I);
							}
							curState().setMana(maxState().getMana());
						}
					}

					if(mySession!=null)
						mySession.stdPrintln(affect.source(),affect.target(),affect.tool(),affect.targetMessage());
					mob.location().recoverRoomStats();
				}
				break;
			case Affect.TYP_LIST:
				{
					super.affect(myHost,affect);
					StringBuffer str=listInventory(mob);
					if(str.length()==0)
					{
						if(whatISell!=DEAL_BANKER)
							ExternalPlay.quickSay(this,mob,"I have nothing for sale.",false,false);
					}
					else
						ExternalPlay.quickSay(this,mob,"\n\r"+str+"^T",true,false);
				}
				break;
			default:
				super.affect(myHost,affect);
				break;
			}
		}
		else
			super.affect(myHost,affect);
	}

	private double prejudiceValueFromPart(MOB mob, boolean sellTo, String part)
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
			if(bit.equalsIgnoreCase(mob.charStats().getMyRace().racialCategory()))
			{	yes=true; break;}
			if(bit.equalsIgnoreCase(CommonStrings.shortAlignmentStr(mob.getAlignment())))
			{ yes=true; break;}
		}
		if(yes) return d;
		return 0.0;

	}
	private double prejudiceFactor(MOB mob, boolean sellTo)
	{
		if(prejudiceFactors().length()==0) return 1.0;
		if(prejudiceFactors().indexOf("=")<0)
		{
			if(Util.s_double(prejudiceFactors())!=0.0)
				return Util.s_double(prejudiceFactors());
			return 1.0;
		}
		String factors=prejudiceFactors().toUpperCase();
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

	private int yourValue(MOB mob, Environmental product, boolean sellTo)
	{
		int val=0;
		if(product==null)
			return val;
		if(product instanceof Item)
			val=((Item)product).value();
		else
		if(product instanceof Ability)
		{
			if(whatISell==DEAL_TRAINER)
				val=CMAble.lowestQualifyingLevel(product.ID())*100;
			else
			if(whatISell==DEAL_CASTER)
				val=CMAble.lowestQualifyingLevel(product.ID())*25;
		}
		else
		if(product instanceof MOB)
		{
			Ability A=product.fetchAffect("Prop_Retainable");
			if(A!=null)
				val=Util.s_int(A.text());
			if(val==0)
				val=25*product.envStats().level();
		}
		else
			val=CMAble.lowestQualifyingLevel(product.ID())*25;
		if(mob==null) return val;

		double d=prejudiceFactor(mob,sellTo);
		val=(int)Math.round(Util.mul(d,val));

		//double halfPrice=Math.round(Util.div(val,2.0));
		double quarterPrice=Math.round(Util.div(val,4.0));

		// gets the shopkeeper a deal on junk.  Pays 25% at 0 charisma, and 50% at 30
		int buyPrice=(int)Math.round(quarterPrice+Util.mul(quarterPrice,Util.div(mob.charStats().getStat(CharStats.CHARISMA),30.0)));

        if((!(product instanceof Ability)&&(numberInStock(product)!=0)))
			buyPrice=(int)Math.round(Util.mul(buyPrice,Util.div((maximumDuplicatesBought-numberInStock(product)),maximumDuplicatesBought)));

		// the price is 200% at 0 charisma, and 100% at 30
		int sellPrice=(int)Math.round(val+val-Util.mul(val,Util.div(mob.charStats().getStat(CharStats.CHARISMA),30.0)));

		if(buyPrice>sellPrice)buyPrice=sellPrice;

		if(sellTo)
			val=sellPrice;
		else
			val=buyPrice;

		if(val<=0) val=1;
		return val;
	}

	private LandTitle getTitle(Room R)
	{
		for(int a=0;a<R.numAffects();a++)
			if(R.fetchAffect(a) instanceof LandTitle)
				return (LandTitle)R.fetchAffect(a);
		return null;
	}

	private Vector addRealEstate(Vector V,MOB mob)
	{
		if(((whatISell==DEAL_LANDSELLER)||((whatISell==DEAL_CLANDSELLER)&&(mob.getClanID().length()>0)))
		&&(getStartRoom()!=null)
		&&(getStartRoom().getArea()!=null))
		{
			String name=mob.name();
			if(whatISell==DEAL_CLANDSELLER)
				name=mob.getClanID();
			Vector roomsHandling=new Vector();
			for(Enumeration r=getStartRoom().getArea().getMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				LandTitle A=getTitle(R);
				if((A!=null)&&(!roomsHandling.contains(R)))
				{
					Vector V2=A.getRooms();
					for(int v=0;v<V2.size();v++)
						roomsHandling.addElement(V2.elementAt(v));
					Item I=CMClass.getItem("GenTitle");
					((LandTitle)I).setLandRoomID(R.ID());
					if(((LandTitle)I).landOwner().equals(name))
					{
						if(!I.name().endsWith(" (Copy)"))
							I.setName(I.name()+" (Copy)");
					}
					else
					if(((LandTitle)I).landOwner().length()>0)
						continue;
					else
					for(int d=0;d<4;d++)
					{
						Room R2=R.getRoomInDir(d);
						LandTitle A2=null;
						if(R2!=null)
							A2=getTitle(R2);
						if((A2!=null)&&(!A2.landOwner().equals(name)))
						   continue;
					}
					I.recoverEnvStats();
					V.addElement(I);
				}
			}
		}
		return V;
	}
	public String prejudiceFactors(){return Util.decompressString(miscText);}
	public void setPrejudiceFactors(String factors){miscText=Util.compressString(factors);}

	private StringBuffer listInventory(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		int csize=0;
		Vector inventory=getUniqueStoreInventory();
		inventory=addRealEstate(inventory,mob);
		if(inventory.size()==0) return msg;

		int totalCols=((whatISell==DEAL_LANDSELLER)||(whatISell==DEAL_CLANDSELLER))?1:2;
		int totalWidth=60/totalCols;

		for(int i=0;i<inventory.size();i++)
		{
			Environmental E=(Environmental)inventory.elementAt(i);

			if(!((E instanceof Item)&&((((Item)E).container()!=null)||(!Sense.canBeSeenBy(E,mob)))))
			{
				int val=yourValue(mob,E,true);
				if((""+val).length()>(4+csize))
					csize=(""+val).length()-4;
			}
		}

		String c="^x["+Util.padRight("Cost",4+csize)+"] "+Util.padRight("Product",totalWidth-csize);
		msg.append(c+((totalCols>1)?c:"")+"^.^N\n\r");
		int colNum=0;
		for(int i=0;i<inventory.size();i++)
		{
			Environmental E=(Environmental)inventory.elementAt(i);

			if(!((E instanceof Item)&&((((Item)E).container()!=null)||(!Sense.canBeSeenBy(E,mob)))))
			{
				String col=null;
				int val=yourValue(mob,E,true);
				col=Util.padRight("["+val,5+csize)+"] "+Util.padRight(E.name(),totalWidth-csize);
				if((++colNum)>totalCols)
				{
					msg.append("\n\r");
					colNum=1;
				}
				msg.append(col);
			}
		}
		return msg;
	}
}
