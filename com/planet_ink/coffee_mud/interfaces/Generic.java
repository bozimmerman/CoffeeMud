package com.planet_ink.coffee_mud.interfaces;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.Exits.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.Abilities.*;
import com.planet_ink.coffee_mud.application.*;
import java.util.Vector;

public class Generic
{
	
	public static boolean get(int x, int m)
	{
		return (x&m)==m;
	}
	
	public static int flags(Environmental E)
	{
		int f=0;
		if(E instanceof Item)
		{
			Item item=(Item)E;
			if(item.isDroppable())
				f=f|1;
			if(item.isGettable())
				f=f|2;
			if(item.isReadable())
				f=f|4;
			if(item.isRemovable())
				f=f|8;
			if(item.isTrapped())
				f=f|16;
		}
		if(E instanceof Container)
		{
			Container container=(Container)E;
			
			if(container.hasALid)
				f=f|32;
			if(container.hasALock)
				f=f|64;
			// defaultsclosed 128
			// defaultslocked 256
		}
		else
		if(E instanceof Exit)
		{
			Exit exit=(Exit)E;
			if(exit.isReadable())
				f=f|4;
			if(exit.isTrapped())
				f=f|16;
			if(exit.hasADoor())
				f=f|32;
			if(exit.hasALock())
				f=f|64;
			if(exit.defaultsClosed())
				f=f|128;
			if(exit.defaultsLocked())
				f=f|256;
			if(exit.levelRestricted())
				f=f|512;
			if(exit.classRestricted())
				f=f|1024;
		}
		return f;
	}
	
	public static void setFlags(Environmental E, int f)
	{
		if(E instanceof Item)
		{
			StdItem item=(StdItem)E;
			item.setDroppable(get(f,1));
			item.setGettable(get(f,2));
			item.setReadable(get(f,4));
			item.setRemovable(get(f,8));
			item.setTrapped(get(f,16));
		}
		if(E instanceof Container)
		{
			Container container=(Container)E;
			container.hasALid=get(f,32);
			container.hasALock=get(f,64);
			// defaultsclosed 128
			// defaultslocked 256
		}
		else
		if(E instanceof Exit)
		{
			Exit exit=(Exit)E;
			exit.setReadable(get(f,4));
			exit.setTrapped(get(f,16));
			exit.setHasDoor(get(f,32));
			exit.setHasLock(get(f,64));
			exit.setDefaultsClosed(get(f,128));
			exit.setDefaultsLocked(get(f,256));
			exit.setLevelRestricted(get(f,512));
			exit.setClassRestricted(get(f,1024));
			Thief_Trap.setTrapped(E,exit.isTrapped());
		}
		if(E instanceof Item)
			Thief_Trap.setTrapped(E,((Item)E).isTrapped());
	}
	
	public static String getPropertiesStr(Environmental E)
	{
		StringBuffer text=new StringBuffer("");
		
		text.append(getEnvPropertiesStr(E)
			+XMLManager.convertXMLtoTag("FLAG",flags(E)));
		
		if(E instanceof Exit)
		{
			Exit exit=(Exit)E;
			text.append(
			 XMLManager.convertXMLtoTag("CLOSTX",exit.closedText())
			+XMLManager.convertXMLtoTag("DOORNM",exit.doorName())
			+XMLManager.convertXMLtoTag("OPENNM",exit.openWord())
			+XMLManager.convertXMLtoTag("CLOSNM",exit.closeWord())
			+XMLManager.convertXMLtoTag("KEYNM",exit.keyName())
			+XMLManager.convertXMLtoTag("OPENTK",exit.openDelayTicks()));
		}
		
		if(E instanceof Item)
		{
			Item item=(Item)E;
			text.append(
			 XMLManager.convertXMLtoTag("IDENT",item.secretIdentity())
			+XMLManager.convertXMLtoTag("VALUE",item.value())
			+XMLManager.convertXMLtoTag("MTRAL",item.material())
			+XMLManager.convertXMLtoTag("USES",item.usesRemaining())
			+XMLManager.convertXMLtoTag("READ",item.readableText())
			+XMLManager.convertXMLtoTag("CAPA",item.capacity())
			+XMLManager.convertXMLtoTag("WORNL",item.rawLogicalAnd())
			+XMLManager.convertXMLtoTag("WORNB",item.rawProperLocationBitmap()));
		}
		
		if(E instanceof Food)
			text.append(XMLManager.convertXMLtoTag("CAPA2",((Food)E).amountOfNourishment));
		
		if(E instanceof Drink)
		{
			text.append(XMLManager.convertXMLtoTag("CAPA2",((Drink)E).amountOfLiquidHeld));
			text.append(XMLManager.convertXMLtoTag("DRINK",((Drink)E).amountOfThirstQuenched));
		}
		
		if(E instanceof Weapon)
		{
			text.append(XMLManager.convertXMLtoTag("TYPE",((Weapon)E).weaponType));
			text.append(XMLManager.convertXMLtoTag("CLASS",((Weapon)E).weaponClassification));
		}
		
		if(E instanceof MOB)
		{
			text.append(XMLManager.convertXMLtoTag("ALIG",((MOB)E).getAlignment()));
			text.append(XMLManager.convertXMLtoTag("MONEY",((MOB)E).getMoney()));
			text.append(XMLManager.convertXMLtoTag("GENDER",""+(char)((MOB)E).baseCharStats().getGender()));
			
			StringBuffer abilitystr=new StringBuffer("");
			for(int b=0;b<((MOB)E).numAbilities();b++)
				abilitystr.append(INI.className(((MOB)E).fetchAbility(b))+";");
		
			text.append(XMLManager.convertXMLtoTag("ABLE",abilitystr.toString()));
			
			StringBuffer itemstr=new StringBuffer("");
			for(int b=0;b<((MOB)E).inventorySize();b++)
				itemstr.append(INI.className(((MOB)E).fetchInventory(b))+"/"+((MOB)E).fetchInventory(b).rawWornCode()+";");
		
			text.append(XMLManager.convertXMLtoTag("ITEM",itemstr.toString()));
			
			if(E instanceof ShopKeeper)
			{
	
				Vector V=((ShopKeeper)E).getUniqueStoreInventory();
				itemstr=new StringBuffer("");
				for(int b=0;b<V.size();b++)
					itemstr.append(INI.className(V.elementAt(b))+"/"+((ShopKeeper)E).numberInStock(INI.className((Environmental)V.elementAt(b)))+";");
		
				text.append(
					XMLManager.convertXMLtoTag("SELLCD",((ShopKeeper)E).whatISell)
					+XMLManager.convertXMLtoTag("STORE",itemstr.toString()));
			}
		}
		
		return text.toString();
	}
	
	public static void setPropertiesStr(Environmental E, String buf)
	{
		setEnvProperties(E,buf);
		setFlags(E,Util.s_int(XMLManager.returnXMLValue(buf,"FLAG")));
		
		if(E instanceof Exit)
		{
			Exit exit=(Exit)E;
			String closedText=XMLManager.returnXMLValue(buf,"CLOSTX");
			String doorName=XMLManager.returnXMLValue(buf,"DOORNM");
			String openName=XMLManager.returnXMLValue(buf,"OPENNM");
			String closeName=XMLManager.returnXMLValue(buf,"CLOSNM");
			exit.setExitParams(doorName,closeName,openName,closedText);
			exit.setKeyName(XMLManager.returnXMLValue(buf,"KEYNM"));
			exit.setOpenDelayTicks(Util.s_int(XMLManager.returnXMLValue(buf,"OPENTK")));
		}
		
		if(E instanceof Item)
		{
			Item item=(Item)E;
			item.setSecretIdentity(XMLManager.returnXMLValue(buf,"IDENT"));
			item.setBaseValue(Util.s_int(XMLManager.returnXMLValue(buf,"VALUE")));
			item.setMaterial(Util.s_int(XMLManager.returnXMLValue(buf,"MTRAL")));
			item.setUsesRemaining(Util.s_int(XMLManager.returnXMLValue(buf,"USES")));
			item.setCapacity(Util.s_int(XMLManager.returnXMLValue(buf,"CAPA")));
			item.setRawLogicalAnd(XMLManager.returnXMLBoolean(buf,"WORNL"));
			item.setRawProperLocationBitmap(Util.s_int(XMLManager.returnXMLValue(buf,"WORNB")));
			item.setReadableText(XMLManager.returnXMLValue(buf,"READ"));

		}
		if(E instanceof Food)
			((Food)E).amountOfNourishment=Util.s_int(XMLManager.returnXMLValue(buf,"CAPA2"));
		
		if(E instanceof Drink)
		{
			((Drink)E).amountOfLiquidHeld=Util.s_int(XMLManager.returnXMLValue(buf,"CAPA2"));
			((Drink)E).amountOfThirstQuenched=Util.s_int(XMLManager.returnXMLValue(buf,"DRINK"));
		}
		if(E instanceof Weapon)
		{
			((Weapon)E).weaponType=Util.s_int(XMLManager.returnXMLValue(buf,"TYPE"));
			((Weapon)E).weaponClassification=Util.s_int(XMLManager.returnXMLValue(buf,"CLASS"));
		}
		if(E instanceof MOB)
		{
			((MOB)E).setAlignment(Util.s_int(XMLManager.returnXMLValue(buf,"ALIG")));
			((MOB)E).setMoney(Util.s_int(XMLManager.returnXMLValue(buf,"MONEY")));
			((MOB)E).baseCharStats().setGender((char)XMLManager.returnXMLValue(buf,"GENDER").charAt(0));
			
			
			String itemstr=XMLManager.returnXMLValue(buf,"ITEM");
			int y=itemstr.indexOf(";");
			while(((MOB)E).inventorySize()>0)
				((MOB)E).delInventory(((MOB)E).fetchInventory(0));
			while(y>=0)
			{
				String itemi=itemstr.substring(0,y);
				int wornCode=0;
				if(itemi.indexOf("/")>=0)
				{
					wornCode=Util.s_int(itemi.substring(itemi.indexOf("/")+1));
					itemi=itemi.substring(0,itemi.indexOf("/"));
				}
				Item newOne=MUD.getItem(itemi);
				if(newOne!=null)
				{
					newOne=(Item)newOne.newInstance();
					((MOB)E).addInventory(newOne);
					newOne.wear(wornCode);
				}
				itemstr=itemstr.substring(y+1);
				y=itemstr.indexOf(";");
			}
		
		
			String abilitystr=XMLManager.returnXMLValue(buf,"ABLE");
			y=abilitystr.indexOf(";");
			while(((MOB)E).numAbilities()>0)
				((MOB)E).delAbility(((MOB)E).fetchAbility(0));
			while(((MOB)E).numAffects()>0)
				((MOB)E).delAffect(((MOB)E).fetchAffect(0));
			while(y>=0)
			{
				String abilityi=abilitystr.substring(0,y);
				Ability newOne=MUD.getAbility(abilityi);
				if(newOne!=null)
				{
					newOne=(Ability)newOne.newInstance();
					if(((MOB)E).fetchAbility(newOne.ID())==null)
					{
						((MOB)E).addAbility(newOne);
						newOne.autoInvocation(((MOB)E));
					}
				}
				abilitystr=abilitystr.substring(y+1);
				y=abilitystr.indexOf(";");
			}
			
			if(E instanceof ShopKeeper)
			{
				((ShopKeeper)E).whatISell=Util.s_int(XMLManager.returnXMLValue(buf,"SELLCD"));
		
		
				Vector V=((ShopKeeper)E).getUniqueStoreInventory();
				for(int b=0;b<V.size();b++)
					((ShopKeeper)E).delStoreInventory(((Environmental)V.elementAt(b)));
		
				itemstr=XMLManager.returnXMLValue(buf,"STORE");
				y=itemstr.indexOf(";");
				while(y>=0)
				{
					String itemi=itemstr.substring(0,y);
					int number=1;
					if(itemi.indexOf("/")>=0)
					{
						number=Util.s_int(itemi.substring(itemi.indexOf("/")+1));
						itemi=itemi.substring(0,itemi.indexOf("/"));
					}
					Environmental newOne=MUD.getItem(itemi);
					if(newOne==null)
						newOne=MUD.getAbility(itemi);
					if(newOne==null)
						newOne=MUD.getMOB(itemi);
					if(newOne!=null)
					{
						newOne=newOne.newInstance();
						((ShopKeeper)E).addStoreInventory(newOne,number);
					}
					itemstr=itemstr.substring(y+1);
					y=itemstr.indexOf(";");
				}
			}
		}
	}
	
	public static String getEnvPropertiesStr(Environmental E)
	{
		StringBuffer text=new StringBuffer("");
		text.append(XMLManager.convertXMLtoTag("NAME",E.name()));
		text.append(XMLManager.convertXMLtoTag("DESC",E.description()));
		text.append(XMLManager.convertXMLtoTag("DISP",E.displayText()));
		text.append(XMLManager.convertXMLtoTag("PROP",
			E.baseEnvStats().ability()+";"+
			E.baseEnvStats().armor()+";"+
			E.baseEnvStats().attackAdjustment()+";"+
			E.baseEnvStats().damage()+";"+
			E.baseEnvStats().disposition()+";"+
			E.baseEnvStats().level()+";"+
			E.baseEnvStats().rejuv()+";"+
			E.baseEnvStats().speed()+";"+
			E.baseEnvStats().weight()+";"+
			E.baseEnvStats().sensesMask()+";"));
		
		StringBuffer behaviorstr=new StringBuffer("");
		for(int b=0;b<E.numBehaviors();b++)
			behaviorstr.append(INI.className(E.fetchBehavior(b))+";");
		
		text.append(XMLManager.convertXMLtoTag("BEHAV",behaviorstr.toString()));
		
		return text.toString();
	}
	public static void setEnvProperties(Environmental E, String buf)
	{
		E.setName(XMLManager.returnXMLValue(buf,"NAME"));
		E.setDescription(XMLManager.returnXMLValue(buf,"DESC"));
		E.setDisplayText(XMLManager.returnXMLValue(buf,"DISP"));
		String props=XMLManager.returnXMLValue(buf,"PROP");
		double[] nums=new double[11];
		int x=0;
		for(int y=props.indexOf(";");y>=0;y=props.indexOf(";"))
		{
			try
			{
				nums[x]=Double.parseDouble(props.substring(0,y));
			}
			catch(Exception e)
			{
				nums[x]=new Integer(Util.s_int(props.substring(0,y))).doubleValue();
			}
			x++;
			props=props.substring(y+1);
		}
		E.baseEnvStats().setAbility((int)Math.round(nums[0]));
		E.baseEnvStats().setArmor((int)Math.round(nums[1]));
		E.baseEnvStats().setAttackAdjustment((int)Math.round(nums[2]));
		E.baseEnvStats().setDamage((int)Math.round(nums[3]));
		E.baseEnvStats().setDisposition((int)Math.round(nums[4]));
		E.baseEnvStats().setLevel((int)Math.round(nums[5]));
		E.baseEnvStats().setRejuv((int)Math.round(nums[6]));
		E.baseEnvStats().setSpeed(nums[7]);
		E.baseEnvStats().setWeight((int)Math.round(nums[8]));
		E.baseEnvStats().setSensesMask((int)Math.round(nums[9]));
		
		
		String behaviorstr=XMLManager.returnXMLValue(buf,"BEHAV");
		
		x=behaviorstr.indexOf(";");
		while(E.numBehaviors()>0)
			E.delBehavior(E.fetchBehavior(0));
		while(x>=0)
		{
			String behaviori=behaviorstr.substring(0,x);
			Behavior newOne=(Behavior)MUD.getBehavior(behaviori);
			if(newOne!=null)
				E.addBehavior(newOne.newInstance());
			behaviorstr=behaviorstr.substring(x+1);
			x=behaviorstr.indexOf(";");
		}
		
	}
	
}
