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
		case ANYTHING:
			return "*Anything*";
		case GENERAL:
			return "General items";
		case ARMOR:
			return "Armor";
		case MAGIC:
			return "Miscellaneous Magic Items";
		case WEAPONS:
			return "Weapons";
		case PETS:
			return "Pets";
		case LEATHER:
			return "Leather";
		case ONLYBASEINVENTORY:
			return "Only my Inventory";
		case TRAINER:
			return "Training in skills/spells/prayers/songs";
		case CASTER:
			return "Caster of spells/prayers";
		case ALCHEMIST:
			return "Potions";
		case JEWELLER:
			return "Precious stones and jewellery";
		case BANKER:
			return "My services as a Banker";
		case LANDSELLER:
			return "Real estate";
		default:
			return "... I have no idea WHAT I sell";
		}
	}

	public void addStoreInventory(Environmental thisThang, int number)
	{
		if((whatISell==ONLYBASEINVENTORY)&&(!inBaseInventory(thisThang)))
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
			if(number>1)
				duplicateInventory.put(copy,new Integer(number));
		}
	}

	public void delStoreInventory(Environmental thisThang)
	{
		if((whatISell==ONLYBASEINVENTORY)&&(inBaseInventory(thisThang)))
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
		case ANYTHING:
			return true;
		case ARMOR:
			return (thisThang instanceof Armor);
		case MAGIC:
			return (thisThang instanceof MiscMagic);
		case WEAPONS:
			return (thisThang instanceof Weapon);
		case GENERAL:
			return ((thisThang instanceof Item)
					&&(!(thisThang instanceof Armor))
					&&(!(thisThang instanceof MiscMagic))
					&&(!(thisThang instanceof Weapon))
					&&(!(thisThang instanceof MOB))
					&&(!(thisThang instanceof Ability)));
		case LEATHER:
			return ((thisThang instanceof Item)
					&&((((Item)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LEATHER));
		case PETS:
			return (thisThang instanceof MOB);
		case ONLYBASEINVENTORY:
			return (inBaseInventory(thisThang));
		case JEWELLER:
			return ((thisThang instanceof Item)
					&&(((((Item)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_GLASS)
					||((((Item)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_PRECIOUS)
					||((Item)thisThang).canBeWornAt(Item.ON_EARS)
					||((Item)thisThang).canBeWornAt(Item.ON_NECK)
					||((Item)thisThang).canBeWornAt(Item.ON_RIGHT_FINGER)
					||((Item)thisThang).canBeWornAt(Item.ON_LEFT_FINGER)));
		case ALCHEMIST:
			return (thisThang instanceof Potion);
		case LANDSELLER:
			return (thisThang instanceof LandTitle);
		}

		return false;
	}

	public boolean doIHaveThisInStock(String name, MOB mob)
	{
		Environmental item=CoffeeUtensils.fetchEnvironmental(storeInventory,name,true);
		if(item==null)
			item=CoffeeUtensils.fetchEnvironmental(storeInventory,name,false);
		if((item==null)&&(whatISell==LANDSELLER)&&(mob!=null))
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
		if((item==null)&&(whatISell==LANDSELLER)&&(mob!=null))
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

	public boolean okAffect(Affect affect)
	{
		MOB mob=affect.source();
		if(affect.amITarget(this))
		{
			switch(affect.targetMinor())
			{
			case Affect.TYP_GIVE:
				if(!mob.isASysOp(mob.location()))
				{
					mob.tell(mob.charStats().HeShe()+" is not accepting charity.");
					return false;
				}
				else
					return true;
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
					FullMsg msg=new FullMsg(affect.source(),affect.tool(),Affect.MSG_DROP,null);
					if(!mob.location().okAffect(msg))
						return false;
					return true;
				}
				ExternalPlay.quickSay(this,mob,"I'm sorry, I'm not buying those.",true,false);
				return false;
			}
			case Affect.TYP_BUY:
			{
				if((affect.tool()!=null)&&(doIHaveThisInStock(affect.tool().name(),mob)))
				{
					if(yourValue(mob,affect.tool(),true)>mob.getMoney())
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
					if(affect.tool() instanceof Ability)
					{
						if((whatISell==TRAINER)&&(!((Ability)affect.tool()).canBeLearnedBy(new Teacher(),mob)))
							return false;
					}
					return true;
				}
				ExternalPlay.quickSay(this,mob,"I don't have that in stock.  Ask for my LIST.",true,false);
				return false;
			}
			case Affect.TYP_LIST:
				return true;
			default:
				break;
			}
		}
		return super.okAffect(affect);
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
	
	
	public void affect(Affect affect)
	{
		super.affect(affect);
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetMinor())
			{
			case Affect.TYP_GIVE:
				if((affect.tool()!=null)
				   &&((doISellThis(affect.tool())))
				   ||((whatISell==ShopKeeper.ONLYBASEINVENTORY)&&(mob.isASysOp(mob.location()))))
					storeInventory.addElement(affect.tool());
				break;
			case Affect.TYP_VALUE:
				ExternalPlay.quickSay(this,mob,"I'll give you "+yourValue(mob,affect.tool(),false)+" for "+affect.tool().name()+".",true,false);
				break;
			case Affect.TYP_SELL:
				if((affect.tool()!=null)&&(doISellThis(affect.tool())))
				{
					mob.setMoney(mob.getMoney()+yourValue(mob,affect.tool(),false));
					mob.tell(name()+" pays you "+yourValue(mob,affect.tool(),false)+" for "+affect.tool().name()+".");
					if(affect.tool() instanceof LandTitle)
						storeInventory.addElement(affect.tool());
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
						}
						item.setContainer(null);
					}
					else
					if(affect.tool() instanceof MOB)
					{
						((MOB)affect.tool()).setFollowing(null);
						((MOB)affect.tool()).destroy();
					}
					else
					if(affect.tool() instanceof Ability)
					{

					}
					if(mySession!=null)
						mySession.stdPrintln(affect.source(),affect.target(),affect.targetMessage());
					mob.location().recoverRoomStats();
				}
				return;
			case Affect.TYP_BUY:
				if((affect.tool()!=null)&&(doIHaveThisInStock(affect.tool().name(),mob)))
				{
					Environmental product=removeStock(affect.tool().name(),mob);
					mob.setMoney(mob.getMoney()-yourValue(mob,product,true));
					if(product instanceof Item)
					{
						mob.location().addItemRefuse((Item)product,Item.REFUSE_PLAYER_DROP);
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
									mob.location().addItemRefuse((Item)I,Item.REFUSE_PLAYER_DROP);
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
								mob.location().addItemRefuse(key,Item.REFUSE_PLAYER_DROP);
							}
						}
						FullMsg msg=new FullMsg(mob,product,this,Affect.MSG_GET,null);
						if(location().okAffect(msg))
						{
							tell(affect.source(),affect.target(),affect.targetMessage());
							location().send(mob,msg);
							if((affect.tool() instanceof InnKey)&&(location()!=null))
							{
								InnKey item =(InnKey)affect.tool();
								String buf=findInnRoom(item, "", location());
								if(buf==null) buf=findInnRoom(item, "upstairs", location().getRoomInDir(Directions.UP));
								if(buf==null) buf=findInnRoom(item, "downstairs", location().getRoomInDir(Directions.DOWN));
								if(buf!=null) ExternalPlay.quickSay(this,mob,"Your room is "+buf+".",false,false);
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
						if(whatISell==TRAINER)
							((Ability)product).teach(new Teacher(),mob);
						else
						{
							curState().setMana(maxState().getMana());
							Vector V=new Vector();
							V.addElement(mob.name()+"$");
							((Ability)product).invoke(this,V,mob,true);
							curState().setMana(maxState().getMana());
						}

					}

					if(mySession!=null)
						mySession.stdPrintln(affect.source(),affect.target(),affect.targetMessage());
					mob.location().recoverRoomStats();
				}
				return;
			case Affect.TYP_LIST:
				{
					StringBuffer str=listInventory(mob);
					if(str.length()==0)
					{
						if(whatISell!=ShopKeeper.BANKER)
							ExternalPlay.quickSay(this,mob,"I have nothing for sale.",false,false);
					}
					else
						ExternalPlay.quickSay(this,mob,"\n\r"+str+"^T",true,false);
				}
				return;
			default:
				break;
			}
		}
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
			if(whatISell==TRAINER)
				val=CMAble.lowestQualifyingLevel(product.ID())*100;
			else
			if(whatISell==CASTER)
				val=CMAble.lowestQualifyingLevel(product.ID())*25;
		}
		else
			val=CMAble.lowestQualifyingLevel(product.ID())*25;
		if(mob==null) return val;

		//double halfPrice=Math.round(Util.div(val,2.0));
		double quarterPrice=Math.round(Util.div(val,4.0));
		
		// gets the shopkeeper a deal on junk.  Pays 25% at 0 charisma, and 50% at 30
		int buyPrice=(int)Math.round(quarterPrice+Util.mul(quarterPrice,Util.div(mob.charStats().getStat(CharStats.CHARISMA),30.0)));
		
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

	
	private Vector addRealEstate(Vector V,MOB mob)
	{
		if((whatISell==ShopKeeper.LANDSELLER)
		&&(getStartRoom()!=null)
		&&(getStartRoom().getArea()!=null))
		{
			Vector rooms=getStartRoom().getArea().getMyMap();
			for(int r=0;r<rooms.size();r++)
			{
				Room R=(Room)rooms.elementAt(r);
				Ability A=null;
				for(int a=0;a<R.numAffects();a++)
					if(R.fetchAffect(a) instanceof LandTitle)
					{ A=R.fetchAffect(a); break;}
				if(A!=null)
				{
					Item I=CMClass.getItem("StdTitle");
					((LandTitle)I).setLandRoomID(R.ID());
					if(((LandTitle)I).landOwner().equals(mob.name()))
						I.baseEnvStats().setWeight(1);
					else
					if(((LandTitle)I).landOwner().length()>0)
						continue;
					I.recoverEnvStats();
					V.addElement(I);
				}
			}
		}
		return V;
	}

	private StringBuffer listInventory(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		int csize=0;
		Vector inventory=getUniqueStoreInventory();
		inventory=addRealEstate(inventory,mob);
		if(inventory.size()==0) return msg;
		
		int totalCols=(whatISell==ShopKeeper.LANDSELLER)?1:2;
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
		msg.append(c+((totalCols>1)?c:"")+"^^^N\n\r");
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
