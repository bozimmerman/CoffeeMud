package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

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

			if(container.hasALid())
				f=f|32;
			if(container.hasALock())
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
			if(exit.alignmentRestricted())
				f=f|2048;
		}
		return f;
	}

	public static void setFlags(Environmental E, int f)
	{
		if(E instanceof Item)
		{
			Item item=(Item)E;
			item.setDroppable(get(f,1));
			item.setGettable(get(f,2));
			item.setReadable(get(f,4));
			item.setRemovable(get(f,8));
			item.setTrapped(get(f,16));
		}
		if(E instanceof Container)
		{
			Container container=(Container)E;
			container.setLidsNLocks(get(f,32),!get(f,32),get(f,64),get(f,64));
		}
		else
		if(E instanceof Exit)
		{
			Exit exit=(Exit)E;
			exit.setReadable(get(f,4));
			exit.setTrapped(get(f,16));
			boolean HasDoor=get(f,32);
			boolean HasLock=get(f,64);
			boolean DefaultsClosed=get(f,128);
			boolean DefaultsLocked=get(f,256);
			exit.setLevelRestricted(get(f,512));
			exit.setClassRestricted(get(f,1024));
			exit.setAlignmentRestricted(get(f,2048));
			exit.setDoorsNLocks(HasDoor,!DefaultsClosed,DefaultsClosed,HasLock,DefaultsLocked,DefaultsLocked);
			((Trap)CMClass.getAbility("Trap_Trap")).setTrapped(E,exit.isTrapped());
		}
		if(E instanceof Item)
			((Trap)CMClass.getAbility("Trap_Trap")).setTrapped(E,((Item)E).isTrapped());
	}

	public static String getPropertiesStr(Environmental E, boolean fromTop)
	{
		if(E==null)
		{
			Log.errOut("Generic","getPropertiesStr: null 'E'");
			return "";
		}
		else
			return (E.isGeneric()?getGenPropertiesStr(E):"") + (fromTop?getOrdPropertiesStr(E):"");
	}

	private static String getOrdPropertiesStr(Environmental E)
	{
		if(E instanceof Room)
			return getExtraEnvPropertiesStr(E);
		else
		if(E instanceof Ability)
			return XMLManager.convertXMLtoTag("AWRAP",E.text());
		else
		if(E instanceof Item)
		{
			Item item=(Item)E;
			String xml=
				 XMLManager.convertXMLtoTag("IID",""+E)
				+XMLManager.convertXMLtoTag("IWORN",""+item.rawWornCode())
				+XMLManager.convertXMLtoTag("ILOC",""+((item.location()!=null)?(""+item.location()):""))
				+XMLManager.convertXMLtoTag("IUSES",""+item.usesRemaining())
				+XMLManager.convertXMLtoTag("ILEVL",""+E.baseEnvStats().level())
				+XMLManager.convertXMLtoTag("IABLE",""+E.baseEnvStats().ability())
				+((E.isGeneric()?"":XMLManager.convertXMLtoTag("ITEXT",""+E.text())));
			return xml;
		}
		else
		if(E instanceof MOB)
		{
			String xml=
				 XMLManager.convertXMLtoTag("MLEVL",""+E.baseEnvStats().level())
				+XMLManager.convertXMLtoTag("MABLE",""+E.baseEnvStats().ability())
				+XMLManager.convertXMLtoTag("MREJUV",""+E.baseEnvStats().rejuv())
				+((E.isGeneric()?"":XMLManager.convertXMLtoTag("ITEXT",""+E.text())));
			return xml;
		}
		return "";
	}

	public static String getGenPropertiesStr(Environmental E)
	{
		StringBuffer text=new StringBuffer("");
		text.append(getEnvPropertiesStr(E));

		text.append(XMLManager.convertXMLtoTag("FLAG",flags(E)));

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
			 XMLManager.convertXMLtoTag("IDENT",item.rawSecretIdentity())
			+XMLManager.convertXMLtoTag("VALUE",item.value())
			//+XMLManager.convertXMLtoTag("USES",item.usesRemaining()) // handled 'from top' & in db
			+XMLManager.convertXMLtoTag("MTRAL",item.material())
			+XMLManager.convertXMLtoTag("READ",item.readableText())
			+XMLManager.convertXMLtoTag("CAPA",item.capacity())
			+XMLManager.convertXMLtoTag("WORNL",item.rawLogicalAnd())
			+XMLManager.convertXMLtoTag("WORNB",item.rawProperLocationBitmap()));
		}

		if(E instanceof Food)
			text.append(XMLManager.convertXMLtoTag("CAPA2",((Food)E).nourishment()));

		if(E instanceof Drink)
		{
			text.append(XMLManager.convertXMLtoTag("CAPA2",((Drink)E).liquidHeld()));
			text.append(XMLManager.convertXMLtoTag("DRINK",((Drink)E).thirstQuenched()));
		}

		if(E instanceof Weapon)
		{
			text.append(XMLManager.convertXMLtoTag("TYPE",((Weapon)E).weaponType()));
			text.append(XMLManager.convertXMLtoTag("CLASS",((Weapon)E).weaponClassification()));
		}

		if(E instanceof MOB)
		{
			text.append(XMLManager.convertXMLtoTag("ALIG",((MOB)E).getAlignment()));
			text.append(XMLManager.convertXMLtoTag("MONEY",((MOB)E).getMoney()));
			text.append(XMLManager.convertXMLtoTag("GENDER",""+(char)((MOB)E).baseCharStats().getGender()));
			text.append(XMLManager.convertXMLtoTag("MRACE",""+((MOB)E).baseCharStats().getMyRace().ID()));

			StringBuffer itemstr=new StringBuffer("");
			for(int b=0;b<((MOB)E).inventorySize();b++)
			{
				Item I=((MOB)E).fetchInventory(b);
				itemstr.append("<ITEM>");
				itemstr.append(XMLManager.convertXMLtoTag("ICLASS",CMClass.className(I)));
				itemstr.append(XMLManager.convertXMLtoTag("IDATA",getPropertiesStr(I,true)));
				itemstr.append("</ITEM>");
			}
			text.append(XMLManager.convertXMLtoTag("INVEN",itemstr.toString()));

			StringBuffer abilitystr=new StringBuffer("");
			for(int b=0;b<((MOB)E).numAbilities();b++)
			{
				Ability A=((MOB)E).fetchAbility(b);
				if(!A.isBorrowed(E))
				{
					abilitystr.append("<ABLTY>");
					abilitystr.append(XMLManager.convertXMLtoTag("ACLASS",CMClass.className(A)));
					abilitystr.append(XMLManager.convertXMLtoTag("ADATA",getPropertiesStr(A,true)));
					abilitystr.append("</ABLTY>");
				}
			}
			text.append(XMLManager.convertXMLtoTag("ABLTYS",abilitystr.toString()));

			if(E instanceof ShopKeeper)
			{
				text.append(XMLManager.convertXMLtoTag("SELLCD",((ShopKeeper)E).whatIsSold()));

				Vector V=((ShopKeeper)E).getUniqueStoreInventory();
				itemstr=new StringBuffer("");
				for(int b=0;b<V.size();b++)
				{
					Environmental Env=(Environmental)V.elementAt(b);
					itemstr.append("<SHITEM>");
					itemstr.append(XMLManager.convertXMLtoTag("SICLASS",CMClass.className(Env)));
					itemstr.append(XMLManager.convertXMLtoTag("SISTOCK",((ShopKeeper)E).numberInStock(Env)));
					itemstr.append(XMLManager.convertXMLtoTag("SIDATA",getPropertiesStr(Env,true)));
					itemstr.append("</SHITEM>");
				}
				text.append(XMLManager.convertXMLtoTag("STORE",itemstr.toString()));
			}
		}
		return text.toString();
	}

	public static void setPropertiesStr(Environmental E, String buf, boolean fromTop)
	{
		Vector V=XMLManager.parseAllXML(buf);
		if(V==null)
			Log.errOut("Generic","setPropertiesStr: null 'V'");
		else
		if(E==null)
			Log.errOut("Generic","setPropertiesStr: null 'E'");
		else
		{
			if(E.isGeneric())
				setGenPropertiesStr(E,V);
			if(fromTop)
				setOrdPropertiesStr(E,V);
			recoverEnvironmental(E);
		}
	}

	private static void recoverEnvironmental(Environmental E)
	{
		if(E==null) return;
		E.recoverEnvStats();
		if(E instanceof MOB)
		{
			((MOB)E).recoverCharStats();
			((MOB)E).recoverMaxState();
			((MOB)E).resetToMaxState();
		}
	}

	private static void setPropertiesStr(Environmental E, Vector V, boolean fromTop)
	{
		if(E==null)
			Log.errOut("Generic","setPropertiesStr2: null 'E'");
		else
		{
			if(E.isGeneric())
				setGenPropertiesStr(E,V);
			if(fromTop)
				setOrdPropertiesStr(E,V);
			recoverEnvironmental(E);
		}
	}

	private static void setOrdPropertiesStr(Environmental E, Vector V)
	{
		if(V==null)
		{
			Log.errOut("Generic","null XML returned on "+E.ID()+" parse. Load aborted.");
			return;
		}


		if(E instanceof Room)
			setExtraEnvProperties(E,V);
		else
		if(E instanceof Ability)
			E.setMiscText(XMLManager.getValFromPieces(V,"AWRAP"));
		else
		if(E instanceof Item)
		{
			Item item=(Item)E;
			item.setUsesRemaining(XMLManager.getIntFromPieces(V,"IUSES"));
			item.baseEnvStats().setLevel(XMLManager.getIntFromPieces(V,"ILEVL"));
			item.baseEnvStats().setAbility(XMLManager.getIntFromPieces(V,"IABLE"));
			if(!E.isGeneric())
				item.setMiscText(XMLManager.getValFromPieces(V,"ITEXT"));
			item.wearAt(XMLManager.getIntFromPieces(V,"USES"));
		}
		else
		if(E instanceof MOB)
		{
			E.baseEnvStats().setLevel(XMLManager.getIntFromPieces(V,"MLEVL"));
			E.baseEnvStats().setAbility(XMLManager.getIntFromPieces(V,"MABLE"));
			E.baseEnvStats().setAbility(XMLManager.getIntFromPieces(V,"MREJUV"));
			if(!E.isGeneric())
				E.setMiscText(XMLManager.getValFromPieces(V,"MTEXT"));
		}
	}

	private static void setGenPropertiesStr(Environmental E, Vector buf)
	{
		if(buf==null)
		{
			Log.errOut("Generic","null XML returned on "+E.ID()+" parse.  Load aborted.");
			return;
		}

		if(E instanceof MOB)
		{
			while(((MOB)E).numAbilities()>0)
				((MOB)E).delAbility(((MOB)E).fetchAbility(0));
			while(((MOB)E).inventorySize()>0)
				((MOB)E).delInventory(((MOB)E).fetchInventory(0));
			if(E instanceof ShopKeeper)
			{
				Vector V=((ShopKeeper)E).getUniqueStoreInventory();
				for(int b=0;b<V.size();b++)
					((ShopKeeper)E).delStoreInventory(((Environmental)V.elementAt(b)));
			}
		}
		while(E.numAffects()>0)
			E.delAffect(E.fetchAffect(0));
		while(E.numBehaviors()>0)
			E.delBehavior(E.fetchBehavior(0));

		setEnvProperties(E,buf);

		setFlags(E,Util.s_int(XMLManager.getValFromPieces(buf,"FLAG")));

		if(E instanceof Exit)
		{
			Exit exit=(Exit)E;
			String closedText=XMLManager.getValFromPieces(buf,"CLOSTX");
			String doorName=XMLManager.getValFromPieces(buf,"DOORNM");
			String openName=XMLManager.getValFromPieces(buf,"OPENNM");
			String closeName=XMLManager.getValFromPieces(buf,"CLOSNM");
			exit.setExitParams(doorName,closeName,openName,closedText);
			exit.setKeyName(XMLManager.getValFromPieces(buf,"KEYNM"));
			exit.setOpenDelayTicks(XMLManager.getIntFromPieces(buf,"OPENTK"));
		}

		if(E instanceof Item)
		{
			Item item=(Item)E;
			item.setSecretIdentity(XMLManager.getValFromPieces(buf,"IDENT"));
			item.setBaseValue(XMLManager.getIntFromPieces(buf,"VALUE"));
			item.setMaterial(XMLManager.getIntFromPieces(buf,"MTRAL"));
			//item.setUsesRemaining(Util.s_int(XMLManager.returnXMLValue(buf,"USES")));
			item.setCapacity(XMLManager.getIntFromPieces(buf,"CAPA"));
			item.setRawLogicalAnd(XMLManager.getBoolFromPieces(buf,"WORNL"));
			item.setRawProperLocationBitmap(XMLManager.getIntFromPieces(buf,"WORNB"));
			item.setReadableText(XMLManager.getValFromPieces(buf,"READ"));

		}
		if(E instanceof Food)
			((Food)E).setNourishment(XMLManager.getIntFromPieces(buf,"CAPA2"));

		if(E instanceof Drink)
		{
			((Drink)E).setLiquidHeld(XMLManager.getIntFromPieces(buf,"CAPA2"));
			((Drink)E).setLiquidRemaining(XMLManager.getIntFromPieces(buf,"CAPA2"));
			((Drink)E).setThirstQuenched(XMLManager.getIntFromPieces(buf,"DRINK"));
		}
		if(E instanceof Weapon)
		{
			((Weapon)E).setWeaponType(XMLManager.getIntFromPieces(buf,"TYPE"));
			((Weapon)E).setWeaponClassification(XMLManager.getIntFromPieces(buf,"CLASS"));
		}
		if(E instanceof MOB)
		{
			MOB mob=(MOB)E;
			mob.setAlignment(XMLManager.getIntFromPieces(buf,"ALIG"));
			mob.setMoney(XMLManager.getIntFromPieces(buf,"MONEY"));
			mob.baseCharStats().setGender((char)XMLManager.getValFromPieces(buf,"GENDER").charAt(0));
			
			String raceID=XMLManager.getValFromPieces(buf,"MRACE");
			if((raceID.length()>0)&&(CMClass.getRace(raceID)!=null))
				mob.baseCharStats().setMyRace(CMClass.getRace(raceID));
			
			Vector V=XMLManager.getRealContentsFromPieces(buf,"INVEN");
			if(V==null)
			{
				Log.errOut("Generic","Error parsing 'INVEN' of "+E.ID()+".  Load aborted");
				return;
			}
			else
			{
				Hashtable IIDmap=new Hashtable();
				Hashtable LOCmap=new Hashtable();
				for(int i=0;i<V.size();i++)
				{
					XMLManager.XMLpiece iblk=(XMLManager.XMLpiece)V.elementAt(i);
					if((!iblk.tag.equalsIgnoreCase("ITEM"))||(iblk.contents==null))
					{
						Log.errOut("Generic","Error parsing 'ITEM' of "+E.ID()+".  Load aborted");
						return;
					}
					Item newOne=CMClass.getItem(XMLManager.getValFromPieces(iblk.contents,"ICLASS"));
					Vector idat=XMLManager.getRealContentsFromPieces(iblk.contents,"IDATA");
					if((idat==null)||(newOne==null))
					{
						Log.errOut("Generic","Error parsing 'ITEM DATA' of "+CMClass.className(newOne)+".  Load aborted");
						return;
					}
					int wornCode=XMLManager.getIntFromPieces(idat,"IWORN");
					String IID=XMLManager.getValFromPieces(idat,"IID");
					String ILOC=XMLManager.getValFromPieces(idat,"ILOC");
					newOne=(Item)newOne.newInstance();
					mob.addInventory(newOne);
					IIDmap.put(IID,newOne);
					if(ILOC.length()>0)
						LOCmap.put(newOne,ILOC);
					setPropertiesStr(newOne,idat,true);
					newOne.wearAt(wornCode);
				}
				for(int i=0;i<mob.inventorySize();i++)
				{
					Item item=mob.fetchInventory(i);
					String ILOC=(String)LOCmap.get(item);
					if(ILOC!=null)
						item.setLocation((Item)IIDmap.get(ILOC));
				}
			}


			V=XMLManager.getRealContentsFromPieces(buf,"ABLTYS");
			if(V==null)
			{
				Log.errOut("Generic","Error parsing 'ABLTYS' of "+E.ID()+".  Load aborted");
				return;
			}
			else
			{
				for(int i=0;i<V.size();i++)
				{
					XMLManager.XMLpiece ablk=(XMLManager.XMLpiece)V.elementAt(i);
					if((!ablk.tag.equalsIgnoreCase("ABLTY"))||(ablk.contents==null))
					{
						Log.errOut("Generic","Error parsing 'ABLTY' of "+E.ID()+".  Load aborted");
						return;
					}
					Ability newOne=CMClass.getAbility(XMLManager.getValFromPieces(ablk.contents,"ACLASS"));
					Vector adat=XMLManager.getRealContentsFromPieces(ablk.contents,"ADATA");
					if((adat==null)||(newOne==null))
					{
						Log.errOut("Generic","Error parsing 'ABLTY DATA' of "+CMClass.className(newOne)+".  Load aborted");
						return;
					}
					newOne=(Ability)newOne.newInstance();
					newOne.setProfficiency(100);
					setPropertiesStr(newOne,adat,true);
					if(((MOB)E).fetchAbility(newOne.ID())==null)
					{
						((MOB)E).addAbility(newOne);
						newOne.autoInvocation(((MOB)E));
					}
				}
			}

			if(E instanceof ShopKeeper)
			{
				ShopKeeper shopmob=(ShopKeeper)E;
				shopmob.setWhatIsSold(XMLManager.getIntFromPieces(buf,"SELLCD"));

				V=XMLManager.getRealContentsFromPieces(buf,"STORE");
				if(V==null)
				{
					Log.errOut("Generic","Error parsing 'STORE' of "+E.ID()+".  Load aborted");
					return;
				}
				else
				{
					Hashtable IIDmap=new Hashtable();
					Hashtable LOCmap=new Hashtable();
					for(int i=0;i<V.size();i++)
					{
						XMLManager.XMLpiece iblk=(XMLManager.XMLpiece)V.elementAt(i);
						if((!iblk.tag.equalsIgnoreCase("SHITEM"))||(iblk.contents==null))
						{
							Log.errOut("Generic","Error parsing 'SHITEM' of "+E.ID()+".  Load aborted");
							return;
						}
						String itemi=XMLManager.getValFromPieces(iblk.contents,"SICLASS");
						int numStock=XMLManager.getIntFromPieces(iblk.contents,"SISTOCK");
						Environmental newOne=CMClass.getUnknown(itemi);
						Vector idat=XMLManager.getRealContentsFromPieces(iblk.contents,"SIDATA");
						if((idat==null)||(newOne==null))
						{
							Log.errOut("Generic","Error parsing 'SHOP DATA' of "+CMClass.className(newOne)+".  Load aborted");
							return;
						}
						newOne=newOne.newInstance();
						if(newOne instanceof Item)
						{
							String IID=XMLManager.getValFromPieces(idat,"IID");
							String ILOC=XMLManager.getValFromPieces(idat,"ILOC");
							IIDmap.put(newOne,IID);
							if(ILOC.length()>0)
								LOCmap.put(ILOC,newOne);
						}
						setPropertiesStr(newOne,idat,true);
						shopmob.addStoreInventory(newOne,numStock);
					}
					for(int i=0;i<shopmob.getUniqueStoreInventory().size();i++)
					{
						Environmental stE=(Environmental)shopmob.getUniqueStoreInventory().elementAt(i);
						if(stE instanceof Item)
						{
							Item item=(Item)stE;
							String ILOC=(String)LOCmap.get(item);
							if(ILOC!=null)
								item.setLocation((Item)IIDmap.get(ILOC));
						}
					}
				}
			}
		}
	}

	private static String getExtraEnvPropertiesStr(Environmental E)
	{
		StringBuffer text=new StringBuffer("");
		StringBuffer behaviorstr=new StringBuffer("");
		for(int b=0;b<E.numBehaviors();b++)
		{
			Behavior B=E.fetchBehavior(b);

			behaviorstr.append("<BHAVE>");
			behaviorstr.append(XMLManager.convertXMLtoTag("BCLASS",CMClass.className(B)));
			behaviorstr.append(XMLManager.convertXMLtoTag("BPARMS",B.getParms()));
			behaviorstr.append("</BHAVE>");
		}
		text.append(XMLManager.convertXMLtoTag("BEHAVES",behaviorstr.toString()));

		StringBuffer affectstr=new StringBuffer("");
		for(int a=0;a<E.numAffects();a++)
		{
			Ability A=E.fetchAffect(a);
			if(!A.isBorrowed(E))
			{
				affectstr.append("<AFF>");
				affectstr.append(XMLManager.convertXMLtoTag("ACLASS",CMClass.className(A)));
				affectstr.append(XMLManager.convertXMLtoTag("ATEXT",A.text()));
				affectstr.append("</AFF>");
			}
		}
		text.append(XMLManager.convertXMLtoTag("AFFECS",affectstr.toString()));
		return text.toString();
	}

	private static String getEnvPropertiesStr(Environmental E)
	{
		StringBuffer text=new StringBuffer("");
		text.append(XMLManager.convertXMLtoTag("NAME",E.name()));
		text.append(XMLManager.convertXMLtoTag("DESC",E.description()));
		text.append(XMLManager.convertXMLtoTag("DISP",E.displayText()));
		text.append(XMLManager.convertXMLtoTag("PROP",
			E.baseEnvStats().ability()+"|"+
			E.baseEnvStats().armor()+"|"+
			E.baseEnvStats().attackAdjustment()+"|"+
			E.baseEnvStats().damage()+"|"+
			E.baseEnvStats().disposition()+"|"+
			E.baseEnvStats().level()+"|"+
			E.baseEnvStats().rejuv()+"|"+
			E.baseEnvStats().speed()+"|"+
			E.baseEnvStats().weight()+"|"+
			E.baseEnvStats().sensesMask()+"|"));

		text.append(getExtraEnvPropertiesStr(E));
		return text.toString();
	}

	private static void setEnvProperties(Environmental E, Vector buf)
	{
		E.setName(XMLManager.getValFromPieces(buf,"NAME"));
		E.setDescription(XMLManager.getValFromPieces(buf,"DESC"));
		E.setDisplayText(XMLManager.getValFromPieces(buf,"DISP"));
		String props=XMLManager.getValFromPieces(buf,"PROP");
		double[] nums=new double[11];
		int x=0;
		for(int y=props.indexOf("|");y>=0;y=props.indexOf("|"))
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

		setExtraEnvProperties(E,buf);
	}

	private static void setExtraEnvProperties(Environmental E, Vector buf)
	{

		Vector V=XMLManager.getRealContentsFromPieces(buf,"BEHAVES");
		if(V==null)
		{
			Log.errOut("Generic","Error parsing 'BEHAVES' of "+E.ID()+".  Load aborted");
			return;
		}
		else
		{
			for(int i=0;i<V.size();i++)
			{
				XMLManager.XMLpiece ablk=(XMLManager.XMLpiece)V.elementAt(i);
				if((!ablk.tag.equalsIgnoreCase("BHAVE"))||(ablk.contents==null))
				{
					Log.errOut("Generic","Error parsing 'BHAVE' of "+E.ID()+".  Load aborted");
					return;
				}
				Behavior newOne=CMClass.getBehavior(XMLManager.getValFromPieces(ablk.contents,"BCLASS"));
				String bparms=XMLManager.getValFromPieces(ablk.contents,"BPARMS");
				if(newOne==null)
				{
					Log.errOut("Generic","Error parsing 'BHAVE DATA' of "+CMClass.className(newOne)+".  Load aborted");
					return;
				}
				newOne=(Behavior)newOne.newInstance();
				newOne.setParms(bparms);
				E.addBehavior(newOne);
			}
		}

		V=XMLManager.getRealContentsFromPieces(buf,"AFFECS");
		if(V==null)
		{
			Log.errOut("Generic","Error parsing 'AFFECS' of "+E.ID()+".  Load aborted");
			return;
		}
		else
		{
			for(int i=0;i<V.size();i++)
			{
				XMLManager.XMLpiece ablk=(XMLManager.XMLpiece)V.elementAt(i);
				if((!ablk.tag.equalsIgnoreCase("AFF"))||(ablk.contents==null))
				{
					Log.errOut("Generic","Error parsing 'AFF' of "+E.ID()+".  Load aborted");
					return;
				}
				Ability newOne=CMClass.getAbility(XMLManager.getValFromPieces(ablk.contents,"ACLASS"));
				String aparms=XMLManager.getValFromPieces(ablk.contents,"ATEXT");
				if(newOne==null)
				{
					Log.errOut("Generic","Error parsing 'AFF DATA' of "+CMClass.className(newOne)+".  Load aborted");
					return;
				}
				newOne=(Ability)newOne.newInstance();
				newOne.setMiscText(aparms);
				E.addNonUninvokableAffect(newOne);
			}
		}
	}

}
