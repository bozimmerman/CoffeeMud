package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

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

public class Merchant extends CommonSkill implements ShopKeeper
{
	public String ID() { return "Merchant"; }
	public String name(){ return "Marketeering";}
	private static final String[] triggerStrings = {"MARKET"};
	public String[] triggerStrings(){return triggerStrings;}
	public int overrideMana(){return 5;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	protected int trainsRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_SKILLTRAINCOST);}
	protected int practicesRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_SKILLPRACCOST);}

	protected Vector baseInventory=new Vector();
	protected Hashtable inventorySize=new Hashtable();
	protected Hashtable stockValues=new Hashtable();
	public Merchant()
	{
		super();
		displayText="";

		isAutoInvoked();
	}
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
				itemstr.append(XMLManager.convertXMLtoTag("IVAL",""+stockPrice(I)));
				itemstr.append(XMLManager.convertXMLtoTag("IDATA",CoffeeMaker.getPropertiesStr(I,true)));
				itemstr.append("</INV>");
			}
			text=itemstr.toString()+"</INVS>";
		}
		return text;
	}
	public String budget(){return "";}
	public void setBudget(String factors){}
	public String devalueRate(){return "";}
	public void setDevalueRate(String factors){}
	public int invResetRate(){return 0;}
	public void setInvResetRate(int ticks){}
	
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
				CoffeeMaker.setPropertiesStr(newOne,idat,true);
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
			A=(Merchant)M.fetchEffect(ID());
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

	public int stockPrice(Environmental E)
	{
		if(E==null) return 0;
		if(!(E instanceof Item)) return 0;
		Item I=(Item)E;
		Item I2=getBaseItem(I);
		Integer N=null;
		if(I2!=null) N=(Integer)stockValues.get(""+I2);
		return (N!=null)?N.intValue():I.baseGoldValue();
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
	public void addStoreInventory(Environmental thisThang)
	{ addStoreInventory(thisThang,1); }
	public void addStoreInventory(Environmental thisThang, int number)
	{
		if(thisThang instanceof Item)
			addStoreInventory(thisThang,number,stockPrice(thisThang));
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
		Environmental item=EnglishParser.fetchEnvironmental(V,name,true);
		if(item==null) item=EnglishParser.fetchEnvironmental(V,name,false);
		if(item==null) return null;
		return item;
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

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okMessage(myHost,msg);

		MOB mob=msg.source();
		MOB M=(MOB)affected;
		if(msg.amITarget(M))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_VALUE:
			case CMMsg.TYP_SELL:
				mob.tell("You'll have to talk to "+M.name()+" about that.");
				return false;
			case CMMsg.TYP_BUY:
			case CMMsg.TYP_VIEW:
			{
				if((msg.tool()!=null)
				&&(doIHaveThisInStock(msg.tool().Name(),mob)))
				{
					if((msg.targetMinor()==CMMsg.TYP_BUY)&&(msg.tool()!=null)&&(!msg.tool().okMessage(myHost,msg)))
					    return false;
					if((msg.targetMinor()!=CMMsg.TYP_VIEW)
					&&(yourValue(mob,msg.tool(),true)[0]>MoneyUtils.totalMoney(mob)))
					{
						CommonMsgs.say(M,mob,"You can't afford to buy "+msg.tool().name()+".",false,false);
						return false;
					}
					if(msg.tool() instanceof Item)
					{
						if(((Item)msg.tool()).envStats().level()>mob.envStats().level())
						{
							CommonMsgs.say(M,mob,"That's too advanced for you, I'm afraid.",true,false);
							return false;
						}
					}
					return super.okMessage(myHost,msg);
				}
				CommonMsgs.say(M,mob,"I don't have that in stock.  Ask for my LIST.",true,false);
				return false;
			}
			case CMMsg.TYP_LIST:
				return super.okMessage(myHost,msg);
			default:
				break;
			}
		}
		else
		if(msg.amISource(M)&&(msg.sourceMinor()==CMMsg.TYP_DEATH))
		{
			Item I=(Item)removeStock("all",M);
			while(I!=null)
			{
				M.addInventory(I);
				I=(Item)removeStock("all",M);
			}
			M.recoverEnvStats();
		}
		return super.okMessage(myHost,msg);
	}

	protected double getSalesTax()
	{
	    if(affected instanceof MOB)
	    {
	        MOB mob=(MOB)affected;
	        if(mob.location()!=null)
	        {
				Law theLaw=CoffeeUtensils.getTheLaw(mob.location(),mob);
				if(theLaw!=null)
				{
					String taxs=(String)theLaw.taxLaws().get("SALESTAX");
					if(taxs!=null)
						return Util.s_double(taxs);
				}
	        }
	    }
		return 0.0;
	    
	}
	
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
		{
			super.executeMsg(myHost,msg);
			return;
		}

		MOB M=(MOB)affected;
		if(msg.amITarget(M))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_VIEW:
				super.executeMsg(myHost,msg);
				if((msg.tool()!=null)&&(doIHaveThisInStock(msg.tool().Name(),mob)))
					CommonMsgs.say(M,msg.source(),"Interested in "+msg.tool().name()+"? Here is some information for you:\n\rLevel "+msg.tool().envStats().level()+"\n\rDescription: "+msg.tool().description(),true,false);
				break;
			case CMMsg.TYP_BUY:
				super.executeMsg(myHost,msg);
				MOB mobFor=msg.source();
				if((msg.targetMessage()!=null)&&(msg.targetMessage().length()>0)&&(mob.location()!=null))
				{
					Vector V=Util.parse(msg.targetMessage());
					if(((String)V.elementAt(V.size()-2)).equalsIgnoreCase("for"))
					{
						String s=(String)V.lastElement();
						if(s.endsWith(".")) s=s.substring(0,s.length()-1);
						MOB M2=mob.location().fetchInhabitant(s+"$");
						if(M2!=null) 
							mobFor=M2;
					}
				}
				if((msg.tool()!=null)
				&&(doIHaveThisInStock(msg.tool().Name(),mobFor)))
				{
					int price=yourValue(mob,msg.tool(),true)[0];
					Vector products=removeSellableProduct(msg.tool().Name(),mobFor);
					if(products.size()==0) break;
					Environmental product=(Environmental)products.firstElement();
					MoneyUtils.subtractMoney(M,mob,price);
					M.setMoney(M.getMoney()+price);
					mob.recoverEnvStats();
					if(product instanceof Item)
					{
						for(int p=0;p<products.size();p++)
						{
							Item I=(Item)products.elementAt(p);
							mob.location().addItemRefuse(I,Item.REFUSE_PLAYER_DROP);
						}
						FullMsg msg2=new FullMsg(mobFor,product,this,CMMsg.MSG_GET,null);
						if(M.location().okMessage(mobFor,msg2))
							M.location().send(mobFor,msg2);
						else
							return;
					}
					mob.location().recoverRoomStats();
				}
				break;
			case CMMsg.TYP_LIST:
				{
					super.executeMsg(myHost,msg);
					StringBuffer str=listInventory(mob);
					if(str.length()==0)
						CommonMsgs.say(M,mob,"I have nothing for sale.",false,false);
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

	public int[] yourValue(MOB mob, Environmental product, boolean sellTo)
	{
		int[] val=new int[3];
		if((product==null)||(!(product instanceof Item)))
			return val;
		val[0]=stockPrice(product);
		if((mob==null)||(mob==affected)) return val;

		// the price is 200% at 0 charisma, and 100% at 30
		val[0]=(int)Math.round(val[0]+val[0]-Util.mul(val[0],Util.div(mob.charStats().getStat(CharStats.CHARISMA),30.0)));
		if(val[0]<=0) val[0]=1;
		if(sellTo)
			val[0]+=((int)Util.mul(val[0],Util.div(getSalesTax(),100.0)));
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
				int val=yourValue(mob,E,true)[0];
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
				int val=yourValue(mob,E,true)[0];
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

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()==0)
		{
			commonTell(mob,"Market what? Enter \"market list\" for a list or \"market item value\" to sell something.");
			return false;
		}
		if(Util.combine(commands,0).equalsIgnoreCase("list"))
		{
			FullMsg msg=new FullMsg(mob,mob,CMMsg.MSG_LIST,"<S-NAME> review(s) <S-HIS-HER> inventory.");
			if(mob.location().okMessage(mob,msg))
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
			String iname=I.name();
			while(I!=null)
			{
				mob.addInventory(I);
				I=(Item)removeStock(itemName,mob);
			}
			delStoreInventory(I);
			mob.recoverCharStats();
			mob.recoverEnvStats();
			mob.recoverMaxState();
			mob.tell(iname+" has been removed from your inventory list.");
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
			commonTell(mob,"You don't seem to be carrying '"+itemName+"'.");
			return false;
		}

		if((numberInStock(target)<=0)&&(val<=0))
		{
			commonTell(mob,"You failed to specify a price for '"+itemName+"'.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(!profficiencyCheck(mob,0,auto))
		{
			commonTell(mob,target,null,"You fail to put <T-NAME> up for sale.");
			return false;
		}

		FullMsg msg=new FullMsg(mob,target,CMMsg.MSG_SELL,"<S-NAME> put(s) <T-NAME> up for sale.");
		if(mob.location().okMessage(mob,msg))
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
