package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Merchant extends CommonSkill implements ShopKeeper
{
	public String ID() { return "Merchant"; }
	public String name(){ return "Marketeering";}
	private static final String[] triggerStrings = {"MARKET"};
	public String[] triggerStrings(){return triggerStrings;}
	public int overrideMana(){return 5;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	private static boolean mapped=false;
	protected Vector baseInventory=new Vector();
	protected Hashtable inventorySize=new Hashtable();
	protected Hashtable stockValues=new Hashtable();
	public Merchant()
	{
		super();
		displayText="";

		isAutoInvoked();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",20,ID(),false);}
	}
	public Environmental newInstance(){	Merchant M=new Merchant(); M.setMiscText(text()); return M;}
	public String text()
	{
		String text="";
		Vector V=getBaseInventory();
		if((V!=null)&&(V.size()>0))
		{
			StringBuffer itemstr=new StringBuffer("<INVS>");
			for(int i=0;i<V.size();i++)
			{
				Item I=(Item)V.elementAt(i);
				itemstr.append("<INV>");
				itemstr.append(XMLManager.convertXMLtoTag("ICLASS",CMClass.className(I)));
				itemstr.append(XMLManager.convertXMLtoTag("INUM",""+numberInStock(I)));
				itemstr.append(XMLManager.convertXMLtoTag("IVAL",""+stockValue(I)));
				itemstr.append(XMLManager.convertXMLtoTag("IDATA",Generic.getPropertiesStr(I,true)));
				itemstr.append("</INV>");
			}
			text=itemstr.toString()+"</INVS>";
		}
		return text;
	}
	public void setMiscText(String text)
	{
		synchronized(this)
		{
			baseInventory=new Vector();
			Vector V=new Vector();
			inventorySize=new Hashtable();
			stockValues=new Hashtable();
			if(text.length()==0) return;
			
			Vector buf=XMLManager.parseAllXML(text);
			if(buf==null)
			{
				Log.errOut("Merchant","Error parsing data.");
				return;
			}
			Vector iV=XMLManager.getRealContentsFromPieces(buf,"INVS");
			if(iV==null)
			{
				Log.errOut("Merchant","Error parsing 'INVS'.");
				return;
			}
			for(int i=0;i<iV.size();i++)
			{
				XMLManager.XMLpiece iblk=(XMLManager.XMLpiece)iV.elementAt(i);
				if((!iblk.tag.equalsIgnoreCase("INV"))||(iblk.contents==null))
				{
					Log.errOut("Merchant","Error parsing 'INVS' data.");
					return;
				}
				String itemi=XMLManager.getValFromPieces(iblk.contents,"ICLASS");
				int itemnum=XMLManager.getIntFromPieces(iblk.contents,"INUM");
				int val=XMLManager.getIntFromPieces(iblk.contents,"IVAL");
				Environmental newOne=CMClass.getItem(itemi);
				Vector idat=XMLManager.getRealContentsFromPieces(iblk.contents,"IDATA");
				if((idat==null)||(newOne==null)||(!(newOne instanceof Item)))
				{
					Log.errOut("Merchant","Error parsing 'INV' data.");
					return;
				}
				Generic.setPropertiesStr(newOne,idat,true);
				Item I=(Item)newOne;
				I.recoverEnvStats();
				V.addElement(I);
				inventorySize.put(""+I,new Integer(itemnum));
				stockValues.put(""+I,new Integer(val));
			}
			baseInventory=V;
		}
	}
	
	public void affectEnvStats(Environmental E, EnvStats affectableStats)
	{
		if(E instanceof MOB)
		{
			Vector V=getBaseInventory();
			for(int i=0;i<V.size();i++)
			{
				Item I=(Item)V.elementAt(i);
				Integer N=(Integer)inventorySize.get(""+I);
				int num=(N!=null)?N.intValue():0;
				affectableStats.setWeight(affectableStats.weight()+(I.envStats().weight()*num));
			}
		}
	}
	
	public int whatIsSold(){return ShopKeeper.DEAL_INVENTORYONLY;}
	public void setWhatIsSold(int newSellCode){}
	private void updateBaseStoreInventory()
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB M=(MOB)affected;
			Merchant A=(Merchant)M.fetchAbility(ID());
			if((A!=null)&&(A!=this)) A.baseInventory=baseInventory;
			A=(Merchant)M.fetchAffect(ID());
			if((A!=null)&&(A!=this)) A.baseInventory=baseInventory;
		}
	}
	private Item getBaseItem(Environmental like)
	{
		Vector V=getBaseInventory();
		for(int x=0;x<V.size();x++)
		{
			Environmental E=(Environmental)V.elementAt(x);
			if((E.sameAs(like))&&(E instanceof Item))
				return (Item)E;
		}
		return null;
	}
	
	private int stockValue(Item I)
	{
		if(I==null) return 0;
		Item I2=getBaseItem(I);
		Integer N=null;
		if(I2!=null) N=(Integer)stockValues.get(""+I2);
		return (N!=null)?N.intValue():I.baseGoldValue();
	}
	private boolean inBaseInventory(Environmental thisThang)
	{
		Vector V=getUniqueStoreInventory();
		for(int x=0;x<V.size();x++)
			if(thisThang.sameAs((Item)baseInventory.elementAt(x))) 
				return true;
		return false;
	}
	public Vector getUniqueStoreInventory()
	{
		Vector V=new Vector();
		Environmental lastE=null;
		for(int x=0;x<baseInventory.size();x++)
		{
			Environmental E=(Environmental)baseInventory.elementAt(x);
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
	public void addStoreInventory(Environmental thisThang)
	{ addStoreInventory(thisThang,1); }
	public void addStoreInventory(Environmental thisThang, int number)
	{ 
		if(thisThang instanceof Item)
			addStoreInventory(thisThang,number,stockValue((Item)thisThang));
		else
			addStoreInventory(thisThang,number,1);
	}
	public void addStoreInventory(Environmental thisThang, int number, int val)
	{ 
		if(thisThang==null) return;
							
		Item I=getBaseItem(thisThang);
		if(I!=null)
		{
			Integer N=(Integer)inventorySize.get(""+I);
			if(N==null) N=new Integer(0);
			N=new Integer(N.intValue()+number);
			inventorySize.remove(""+I);
			inventorySize.put(""+I,N);
			stockValues.remove(""+I);
			stockValues.put(""+I,new Integer(val));
			return;
		}
		baseInventory.addElement(thisThang);
		inventorySize.put(""+thisThang,new Integer(number));
		stockValues.put(""+thisThang,new Integer(val));
		updateBaseStoreInventory();
	}
	public void delStoreInventory(Environmental thisThang)
	{
		if(thisThang==null) return;
		
		boolean doneSomething=false;
		Vector V=getBaseInventory();
		for(int i=0;i<V.size();i++)
		{
			Item I=(Item)V.elementAt(i);
			if(I.sameAs(thisThang))
			{
				baseInventory.removeElement(I);
				inventorySize.remove(""+I);
				stockValues.remove(""+I);
				doneSomething=true;
			}
		}
		if(doneSomething) updateBaseStoreInventory();
	}
	public boolean doISellThis(Environmental thisThang)
	{
		return (thisThang instanceof Item)&&(numberInStock(thisThang)>0);
	}
	public boolean doIHaveThisInStock(String name, MOB mob)
	{
		return numberInStock(getStock(name,mob))>0;
	}
	public int numberInStock(Environmental likeThis)
	{
		if(likeThis==null) return 0;
		Vector V=getBaseInventory();
		for(int x=0;x<V.size();x++)
		{
			Environmental E=(Environmental)V.elementAt(x);
			if(E.sameAs(likeThis))
			{
				Integer I=(Integer)inventorySize.get(""+E);
				if(I!=null) return I.intValue();
			}
		}
		return 0;
		
	}
	public Environmental getStock(String name, MOB mob)
	{
		Vector V=getUniqueStoreInventory();
		Environmental item=CoffeeUtensils.fetchEnvironmental(V,name,true);
		if(item==null) item=CoffeeUtensils.fetchEnvironmental(V,name,false);
		if(item==null) return null;
		return (Environmental)item;
	}
	public Environmental removeStock(String name, MOB mob)
	{
		Item I=(Item)getStock(name,mob);
		if(I==null) return null;
		Integer N=(Integer)inventorySize.get(""+I);
		if(N==null)
		{
			baseInventory.removeElement(I);
			stockValues.remove(""+I);
		}
		else
		{
			N=new Integer(N.intValue()-1);
			inventorySize.remove(""+I);
			if(N.intValue()<=0)
			{
				stockValues.remove(""+I);
				baseInventory.removeElement(I);
			}
			else
				inventorySize.put(""+I,N);
		}
		updateBaseStoreInventory();
		return (Item)I.copyOf();
	}
	public Vector removeSellableProduct(String named, MOB mob)
	{
		Vector V=new Vector();
		Environmental product=removeStock(named,mob);
		if(product!=null) V.addElement(product);
		return V;
	}
	public int baseStockSize()
	{
		return getBaseInventory().size();
	}
	public int totalStockSize()
	{
		return baseStockSize();
	}
	public Vector getBaseInventory()
	{
		return baseInventory;
	}
	
	public String storeKeeperString(){return "Only my Inventory";}
	public void clearStoreInventory(){setMiscText("");}
	public String prejudiceFactors(){return "";}
	public void setPrejudiceFactors(String factors){}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB))) 
			return super.okAffect(myHost,affect);
		
		MOB mob=affect.source();
		MOB M=(MOB)affected;
		if(affect.amITarget(M))
		{
			switch(affect.targetMinor())
			{
			case Affect.TYP_VALUE:
			case Affect.TYP_SELL:
				mob.tell("You'll have to talk to "+name()+" about that.");
				return false;
			case Affect.TYP_BUY:
			case Affect.TYP_VIEW:
			{
				if((affect.tool()!=null)
				&&(doIHaveThisInStock(affect.tool().name(),mob)))
				{
					if((affect.targetMinor()!=Affect.TYP_VIEW)
					&&(yourValue(mob,affect.tool(),true)>Money.totalMoney(mob)))
					{
						ExternalPlay.quickSay(M,mob,"You can't afford to buy "+affect.tool().name()+".",false,false);
						return false;
					}
					if(affect.tool() instanceof Item)
					{
						if(((Item)affect.tool()).envStats().level()>mob.envStats().level())
						{
							ExternalPlay.quickSay(M,mob,"That's too advanced for you, I'm afraid.",true,false);
							return false;
						}
					}
					return super.okAffect(myHost,affect);
				}
				ExternalPlay.quickSay(M,mob,"I don't have that in stock.  Ask for my LIST.",true,false);
				return false;
			}
			case Affect.TYP_LIST:
				return super.okAffect(myHost,affect);
			default:
				break;
			}
		}
		else
		if(affect.amISource(M)&&(affect.sourceMinor()==Affect.TYP_DEATH))
		{
			Item I=(Item)removeStock("all",M);
			while(I!=null)
			{
				M.addInventory(I);
				I=(Item)removeStock("all",M);
			}
			M.recoverEnvStats();
		}
		return super.okAffect(myHost,affect);
	}

	public void affect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB))) 
		{
			super.affect(myHost,affect);
			return;
		}
		
		MOB M=(MOB)affected;
		if(affect.amITarget(M))
		{
			MOB mob=affect.source();
			switch(affect.targetMinor())
			{
			case Affect.TYP_VIEW:
				super.affect(myHost,affect);
				if((affect.tool()!=null)&&(doIHaveThisInStock(affect.tool().name(),mob)))
					ExternalPlay.quickSay(M,affect.source(),"Interested in "+affect.tool().name()+"? Here is some information for you:\n\rLevel "+affect.tool().envStats().level()+"\n\rDescription: "+affect.tool().description(),true,false);
				break;
			case Affect.TYP_BUY:
				super.affect(myHost,affect);
				if((affect.tool()!=null)
				&&(doIHaveThisInStock(affect.tool().name(),mob)))
				{
					int price=yourValue(mob,affect.tool(),true);
					Vector products=removeSellableProduct(affect.tool().name(),mob);
					if(products.size()==0) break;
					Environmental product=(Environmental)products.firstElement();
					Money.setTotalMoney(M,mob,price);
					M.setMoney(M.getMoney()+price);
					mob.recoverEnvStats();
					if(product instanceof Item)
					{
						for(int p=0;p<products.size();p++)
						{
							Item I=(Item)products.elementAt(p);
							mob.location().addItemRefuse(I,Item.REFUSE_PLAYER_DROP);
						}
						FullMsg msg=new FullMsg(mob,product,this,Affect.MSG_GET,null);
						if(M.location().okAffect(mob,msg))
							M.location().send(mob,msg);
						else
							return;
					}
					mob.location().recoverRoomStats();
				}
				break;
			case Affect.TYP_LIST:
				{
					super.affect(myHost,affect);
					StringBuffer str=listInventory(mob);
					if(str.length()==0)
						ExternalPlay.quickSay(M,mob,"I have nothing for sale.",false,false);
					else
						ExternalPlay.quickSay(M,mob,"\n\r"+str+"^T",true,false);
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

	private int yourValue(MOB mob, Environmental product, boolean sellTo)
	{
		int val=0;
		if((product==null)||(!(product instanceof Item)))
			return val;
		val=stockValue((Item)product);
		if((mob==null)||(mob==affected)) return val;

		// the price is 200% at 0 charisma, and 100% at 30
		val=(int)Math.round(val+val-Util.mul(val,Util.div(mob.charStats().getStat(CharStats.CHARISMA),30.0)));
		if(val<=0) val=1;
		return val;
	}

	private StringBuffer listInventory(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		int csize=0;
		Vector inventory=getUniqueStoreInventory();
		if(inventory.size()==0) return msg;

		int totalCols=2;
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
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()==0)
		{
			commonTell(mob,"Market what? Enter \"market list\" for a list or \"market item value\" to sell something.");
			return false;
		}
		if(Util.combine(commands,0).equalsIgnoreCase("list"))
		{
			FullMsg msg=new FullMsg(mob,mob,Affect.MSG_LIST,null);
			if(mob.location().okAffect(mob,msg))
				mob.location().send(mob,msg);
			return true;
		}
		if(((String)commands.firstElement()).equalsIgnoreCase("remove")
		||((String)commands.firstElement()).equalsIgnoreCase("delete"))
		{
			if(commands.size()==1)
			{
				commonTell(mob,"Remove what item from the marketing list?");
				return false;
			}
			String itemName=Util.combine(commands,1);
			Item I=(Item)removeStock(itemName,mob);
			if(I==null)
			{
				commonTell(mob,"'"+itemName+"' is not on the list.");
				return false;
			}
			while(I!=null)
			{
				mob.addInventory(I);
				I=(Item)removeStock(itemName,mob);
			}
			delStoreInventory(I);
			mob.recoverCharStats();
			mob.recoverEnvStats();
			mob.recoverMaxState();
			return true;
		}
		
		Environmental target=null;
		int val=-1;
		if(commands.size()>1)
		{
			String s=(String)commands.lastElement();
			val=Util.s_int(s);
			if(val>0) commands.removeElement(s);
		}

		String itemName=Util.combine(commands,0);
		Vector V=new Vector();
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		if(itemName.toUpperCase().startsWith("ALL.")){ allFlag=true; itemName="ALL "+itemName.substring(4);}
		if(itemName.toUpperCase().endsWith(".ALL")){ allFlag=true; itemName="ALL "+itemName.substring(0,itemName.length()-4);}
		int addendum=1;
		String addendumStr="";
		do
		{
			Item I=mob.fetchCarried(null,itemName+addendumStr);
			if(I==null) break;
			if(target==null) 
				target=I;
			else 
			if(!target.sameAs(I)) 
				break;
			if(Sense.canBeSeenBy(I,mob))
				V.addElement(I);
			addendumStr="."+(++addendum);
		}
		while(allFlag);

		if(V.size()==0)
		{
			mob.tell("You don't seem to be carrying '"+itemName+"'.");
			return false;
		}
		
		if((numberInStock(target)<=0)&&(val<=0))
		{
			mob.tell("You failed to specify a price for '"+itemName+"'.");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		
		if(!profficiencyCheck(0,auto))
		{
			mob.tell(mob,target,null,"You fail to put <T-NAME> up for sale.");
			return false;
		}
		
		FullMsg msg=new FullMsg(mob,target,Affect.MSG_SELL,"<S-NAME> put(s) <T-NAME> up for sale.");
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			for(int i=0;i<V.size();i++)
			{
				Item I=(Item)V.elementAt(i);
				if(val<=0)
					addStoreInventory(I);
				else
					addStoreInventory(I,1,val);
				mob.delInventory(I);
			}
		}
		mob.location().recoverRoomStats();
		mob.recoverEnvStats();
		return true;
	}
}
