package com.planet_ink.coffee_mud.MOBS;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.Items.MiscMagic.*;
import com.planet_ink.coffee_mud.Items.Armor.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.db.*;
public class ShopKeeper extends StdMOB 
{
	public final static int ANYTHING=0;
	public final static int GENERAL=1;
	public final static int ARMOR=2;
	public final static int MAGIC=3;
	public final static int WEAPONS=4;
	public final static int PETS=5;
	public final static int LEATHER=6;
	public final static int ONLYBASEINVENTORY=7;
	public final static int TRAINER=8;
	public final static int CASTER=9;
	
	public int whatISell=0;
	private Vector storeInventory=new Vector();
	private int initializedSpecialContentInventory=2;
	protected Vector baseInventory=new Vector();
	
	public ShopKeeper()
	{
		super();
		Username="a shopkeeper";
		setDescription("He\\`s pleased to be of assistance.");
		setDisplayText("A shopkeeper is waiting to serve you.");
		setAlignment(1000);
		setMoney(0);
		baseEnvStats.setWeight(150);
		setWimpHitPoint(0);
		
		baseCharStats().setIntelligence(16);
		baseCharStats().setCharisma(25);
		
		baseEnvStats().setArmor(0);
		
		maxState.setHitPoints(1000);
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	
	public Environmental newInstance()
	{
		return new ShopKeeper();
	}
	
	private boolean inBaseInventory(Object thisThang)
	{
		for(int x=0;x<baseInventory.size();x++)
			if(INI.className(thisThang).equals(INI.className(baseInventory.elementAt(x))))
			   return true;
		return false;
	}
	
	public void addStoreInventory(Environmental thisThang)
	{
		addStoreInventory(thisThang,1);
	}
	
	public Vector getUniqueStoreInventory()
	{
		Vector V=new Vector();
		Environmental lastE=null;
		for(int x=0;x<storeInventory.size();x++)
		{
			Environmental E=(Environmental)storeInventory.elementAt(x);
			boolean ok=true;
			
			if((lastE!=null)&&(INI.className(lastE).equals(INI.className(E))))
				ok=false;
			
			if(ok)
			for(int v=0;v<V.size();v++)
				if(INI.className(V.elementAt(v)).equals(INI.className(E)))
				{
					ok=false;
					break;
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
		default:
			return "I have no idea WHAT I sell.";
		}
	}
	
	public void addStoreInventory(Environmental thisThang, int number)
	{
		if((whatISell==ONLYBASEINVENTORY)&&(!inBaseInventory(thisThang)))
			baseInventory.addElement(thisThang.copyOf());
		for(int v=0;v<number;v++)
		{
			Environmental copy=thisThang.copyOf();
			if(copy instanceof InnKey)
				((InnKey)copy).hangOnRack(this);
			storeInventory.addElement(copy);
		}
	}
	
	public void delStoreInventory(Environmental thisThang)
	{
		if((whatISell==ONLYBASEINVENTORY)&&(inBaseInventory(thisThang)))
		{
			for(int v=baseInventory.size()-1;v>=0;v--)
				if(INI.className(thisThang).equals(INI.className(baseInventory.elementAt(v))))
					baseInventory.removeElement(baseInventory.elementAt(v));
		}
		for(int v=storeInventory.size()-1;v>=0;v--)
			if(INI.className(thisThang).equals(INI.className(storeInventory.elementAt(v))))
				storeInventory.removeElement(storeInventory.elementAt(v));
	}
	
	public boolean doISellThis(Environmental thisThang)
	{
		if(thisThang==null)
			return false;
		if(whatISell==ANYTHING)
			return true;
		else
		if((whatISell==ARMOR)&&(thisThang instanceof Armor))
			return true;
		else
		if((whatISell==MAGIC)&&(thisThang instanceof MiscMagic))
			return true;
		else
		if((whatISell==WEAPONS)&&(thisThang instanceof Weapon))
			return true;
		else
		if((whatISell==GENERAL)&&(thisThang instanceof Item)&&(!(thisThang instanceof Armor))&&(!(thisThang instanceof MiscMagic))&&(!(thisThang instanceof Weapon))&&(!(thisThang instanceof MOB))&&(!(thisThang instanceof Ability)))
			return true;
		else
		if((whatISell==LEATHER)&&(thisThang instanceof Item)&&(((Item)thisThang).material()==Item.LEATHER))
			return true;
		else
		if((whatISell==PETS)&&(thisThang instanceof MOB))
			return true;
		else
		if((whatISell==ONLYBASEINVENTORY)&&(inBaseInventory(thisThang)))
			return true;
		
		return false;
	}
	
	public boolean doIHaveThisInStock(String name)
	{
		Environmental item=Util.fetchEnvironmental(storeInventory,name);
		if(item!=null)
		   return true;
		return false;
	}
	
	public int numberInStock(String name)
	{
		int num=0;
		for(int v=0;v<storeInventory.size();v++)
			if(INI.className(storeInventory.elementAt(v)).equals(name))
				num++;
		return num;
	}
	
	public Environmental getStock(String name)
	{
		Environmental item=Util.fetchEnvironmental(storeInventory,name);
		if(item!=null)
			return item;
		return null;
	}
	
	public Environmental removeStock(String name)
	{
		Environmental item=getStock(name);
		if(item!=null)
			storeInventory.removeElement(item);
		return item;
	}
	
	public boolean okAffect(Affect affect)
	{
		MOB mob=affect.source();
		if(affect.amITarget(this))
		{
			switch(affect.targetCode())
			{
			case Affect.HANDS_GIVE:
				if(!mob.isASysOp())
				{
					mob.tell("The Shopkeeper is not accepting charity.");
					return false;
				}
				else
					return true;
			case Affect.HANDS_SELL:
			{
				if((affect.tool()!=null)&&(doISellThis(affect.tool())))
				{
					if(yourValue(mob,affect.tool(),false)<2)
					{
						SocialProcessor.quickSay(this,mob,"I'm not interested.",true);
						return false;
					}
					if(affect.tool() instanceof Ability)
					{
						SocialProcessor.quickSay(this,mob,"I'm not interested.",true);
						return false;
					}
					if((affect.tool() instanceof Container)&&(((Container)affect.tool()).hasALock))
					{
						for(int i=0;i<mob.inventorySize();i++)
						{
							Item I=mob.fetchInventory(i);
							if((I instanceof StdKey)&&(I.text().equals(((Container)affect.tool()).keyName))&&(I.location()==affect.tool()))
								return true;
						}
						SocialProcessor.quickSay(this,mob,"I won't buy that back unless you put the key in it.",true);
						return false;
					}
					return true;
				}
				SocialProcessor.quickSay(this,mob,"I'm sorry, I'm not buying those.",true);
				return false;
			}
			case Affect.HANDS_BUY:
			{
				if((affect.tool()!=null)&&(doIHaveThisInStock(affect.tool().ID())))
				{
					if(yourValue(mob,affect.tool(),true)>mob.getMoney())
					{
						SocialProcessor.quickSay(this,mob,"You can't afford to buy "+affect.tool().name()+".",false);
						return false;
					}
					if(affect.tool() instanceof Ability)
					{
						if((whatISell==TRAINER)&&(!((Ability)affect.tool()).canBeLearnedBy(new Teacher(),mob)))
							return false;
					}
					return true;
				}
				SocialProcessor.quickSay(this,mob,"I don't have that in stock.  Ask for my LIST.",true);
				return false;
			}
			case Affect.SOUND_LIST:
			{
				if(storeInventory.size()==0)
				{
					SocialProcessor.quickSay(this,mob,"I'm fresh out of EVERYTHING.  Sorry!",false);
					return false;
				}
				else
					return true;
			}
			default:
				break;
			}
		}
		return super.okAffect(affect);
	}
	
	public boolean tick(int tickID)
	{
		if((tickID==ServiceEngine.MOB_TICK)
		&&(whatISell==ONLYBASEINVENTORY)
		&&(location()!=null)
		&&(initializedSpecialContentInventory>0))
		{
			initializedSpecialContentInventory--;
			if(initializedSpecialContentInventory<=0)
			{
				for(int c=location().numItems()-1;c>=0;c--)
				{
					Item I=location().fetchItem(c);
					
					int a=I.baseEnvStats().rejuv();
					if(a==Integer.MAX_VALUE) a=0;
					I.setLocation(null);
					while(a>=0)
					{
						a--;
						addStoreInventory(I.copyOf());
					}
					for(a=I.numAffects()-1;a>=0;a--)
						I.fetchAffect(a).unInvoke();
					I.destroyThis();
				}
				for(int c=location().numInhabitants()-1;c>=0;c--)
				{
					MOB I=location().fetchInhabitant(c);
					if((I!=this)&&(I.isMonster())&&(I.amFollowing()==null))
					{
						int a=I.baseEnvStats().rejuv();
						if(a==Integer.MAX_VALUE) a=0;
						while(a>=0)
						{
							a--;
							addStoreInventory(I.copyOf());
						}
						ServiceEngine.deleteTick(I,ServiceEngine.MOB_TICK);
						I.destroy();
					}
				}
			}
		}
		return super.tick(tickID);
	}
	
	public void affect(Affect affect)
	{
		super.affect(affect);
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetCode())
			{
			case Affect.HANDS_GIVE:
				if((affect.tool()!=null)&&(doISellThis(affect.tool())))
					storeInventory.addElement(affect.tool());
				break;
			case Affect.HANDS_SELL:
				if((affect.tool()!=null)&&(doISellThis(affect.tool())))
				{
					mob.setMoney(mob.getMoney()+yourValue(mob,affect.tool(),false));
					mob.tell(name()+" pays you "+yourValue(mob,affect.tool(),false)+" for "+affect.tool().name()+".");
					storeInventory.addElement(affect.tool());
					if(affect.tool() instanceof Item)
					{
						((Item)affect.tool()).setLocation(null);
						((Item)affect.tool()).remove();
						mob.delInventory((Item)affect.tool());
						if(affect.tool() instanceof Container)
						{
							int i=0;
							while(i<mob.inventorySize())
							{
								int a=mob.inventorySize();
								Item I=mob.fetchInventory(i);
								if(I.location()==affect.tool())
								{
									I.remove();
									storeInventory.addElement(I);
									mob.delInventory(I);
									((Item)I).setLocation((Item)affect.tool());
								}
								if(a==mob.inventorySize())
									i++;
							}
						}
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
			case Affect.HANDS_BUY:
				if((affect.tool()!=null)&&(doIHaveThisInStock(affect.tool().ID())))
				{
					Environmental product=removeStock(affect.tool().ID());
					mob.setMoney(mob.getMoney()-yourValue(mob,product,true));
					if(product instanceof Item)
					{
						mob.location().addItem((Item)product);
						if(product instanceof Container)
						{
							int i=0;
							StdKey foundKey=null;
							Container C=((Container)product);
							while(i<storeInventory.size())
							{
								int a=storeInventory.size();
								Environmental I=(Environmental)storeInventory.elementAt(i);
								if((I instanceof Item)&&(((Item)I).location()==product))
								{
									if((I instanceof StdKey)&&(((StdKey)I).text().equals(C.keyName)))
										foundKey=(StdKey)I;
									((Item)I).remove();
									mob.location().addItem((Item)I);
									storeInventory.removeElement(I);
									((Item)I).setLocation((Item)product);
								}
								if(a==storeInventory.size())
									i++;
							}
							if((C.isLocked)&&(foundKey==null))
							{
								String keyName=Double.toString(Math.random());
								C.keyName=keyName;
								C.isLocked=false;
								C.isOpen=true;
								StdKey key=new StdKey();
								key.setMiscText(keyName);
								key.setLocation(C);
								mob.location().addItem(key);
							}
						}
						FullMsg msg=new FullMsg(mob,product,this,Affect.HANDS_BUY,Affect.HANDS_GET,Affect.NO_EFFECT,null);
						if(location().okAffect(msg))
						{
							location().send(mob,msg);
							if(mySession!=null)
								mySession.stdPrintln(affect.source(),affect.target(),affect.targetMessage());
						}
						else
							return;
					}
					else
					if(product instanceof MOB)
					{
						((MOB)product).bringToLife(mob.location());
						((MOB)product).setFollowing(mob);
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
							V.addElement(mob.name());
							((Ability)product).invoke(this,V);
							curState().setMana(maxState().getMana());
						}
							
					}
						
					if(mySession!=null)
						mySession.stdPrintln(affect.source(),affect.target(),affect.targetMessage());
					mob.location().recoverRoomStats();
				}
				return;
			case Affect.SOUND_LIST:
				SocialProcessor.quickSay(this,mob,"\n\r"+listInventory(mob).toString(),true);
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
				val=product.baseEnvStats().level()*100;
			else
			if(whatISell==CASTER)
				val=product.baseEnvStats().level()*25;
		}
		else
			val=product.baseEnvStats().level()*25;
		if(mob==null) return val;
		
		int sellPrice=(int)Math.round(Util.mul(val,Util.div(10.0,mob.charStats().getCharisma())));
		int buyPrice=(int)Math.round(Util.div(Util.mul(val,Util.div(mob.charStats().getCharisma(),18.0)),2.0));
		if(buyPrice>sellPrice)buyPrice=sellPrice;
		
		if(sellTo)
			val=sellPrice;
		else
			val=buyPrice;
		
		if(val<=0) val=1;
		return val;
	}
	
	
	private StringBuffer listInventory(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		String c=Util.padRight("[Cost]",10)+Util.padRight("Product",29);
		msg.append(c+c+"\n\r");
		Hashtable stuff=new Hashtable();
		int colNum=0;
		for(int i=0;i<storeInventory.size();i++)
		{
			Environmental E=(Environmental)storeInventory.elementAt(i);
			boolean ok=(stuff.get(E.ID())==null);
			
			if((E instanceof Item)&&(((Item)E).location()!=null))
				ok=false;
			
			
			if(ok)
			{
				stuff.put(E.ID(),E.ID());
				String col=null;
				int val=yourValue(mob,E,true);
				col=Util.padRight("["+val+"]",10)+Util.padRight(E.name(),28);
				if((++colNum)>2)
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
