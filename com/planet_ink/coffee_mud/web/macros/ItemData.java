package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class ItemData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=(String)httpReq.getRequestParameters().get("ROOM");
		if(last==null) return " @break@";
		String itemCode=(String)httpReq.getRequestParameters().get("ITEM");
		if(itemCode==null) return "@break@";
		int itemNum=Util.s_int(itemCode);
		
		String mobNum=(String)httpReq.getRequestParameters().get("MOB");
		
		Room R=CMMap.getRoom(last);
		if(R==null)
			return "No Room?!";
		ExternalPlay.resetRoom(R);
		
		Item I=null;
		MOB M=null;
		if(itemCode.length()==0)
			I=CMClass.getItem("GenItem");
		else
		if(mobNum!=null)
		{
			M=R.fetchInhabitant(Util.s_int(mobNum));
			if(M==null)
				return "No MOB?!";
			I=M.fetchInventory(Util.s_int(itemCode));
		}
		else
			I=R.fetchItem(Util.s_int(itemCode));
		
		if(I==null)
			return "No Item?!";
		
		boolean classChanged=(((String)httpReq.getRequestParameters().get("CLASSCHANGED")!=null)
							 &&(((String)httpReq.getRequestParameters().get("CLASSCHANGED")).equals("true")));
		
		// important generic<->non generic swap!
		String newClassID=(String)httpReq.getRequestParameters().get("CLASSES");
		Item I2=null;
		if(newClassID!=null) I2=CMClass.getItem(newClassID);
		if((I2!=null)&&(I.isGeneric()!=I2.isGeneric()))
			I=I2;

		if(I!=null)
		{
			StringBuffer str=new StringBuffer("");
			boolean resetIfNecessary=false;
			String[] okparms={"NAME","CLASSES","DISPLAYTEXT","DESCRIPTION",
							  "LEVEL","ABILITY","REJUV","MISCTEXT",
							  "MATERIALS","ISGENERIC","ISFOOD","NOURISHMENT",
							  "ISDRINK","LIQUIDHELD","QUENCHED","ISCONTAINER",
							  "CAPACITY","ISARMOR","ARMOR","WORNDATA","HEIGHT",
							  "ISWEAPON","WEAPONTYPE","WEAPONCLASS","ATTACK","DAMAGE","MINRANGE",
							  "MAXRANGE","SECRETIDENTITY","ISGETTABLE","ISREMOVABLE",
							  "ISDROPPABLE","ISTWOHANDED","ISTRAPPED","READABLESPELLS",
							  "ISWAND","USESREMAIN","VALUE","WEIGHT","ISMAP","MAPAREAS","ISREADABLE",
							  "ISPILL","ISSUPERPILL","ISPOTION","LIQUIDTYPES","AMMOTYPE",
							  "AMMOCAP","READABLESPELL","ISRIDEABLE","RIDEABLETYPE","MOBSHELD",
							  "HASALID","HASALOCK","KEYCODE","ISWALLPAPER","READABLETEXT"};
			for(int o=0;o<okparms.length;o++)
			if(parms.containsKey(okparms[o]))
			{
				String old=(String)httpReq.getRequestParameters().get(okparms[o]);
				String oldold=old;
				switch(o)
				{
				case 0: // name
					if((old==null)||(old.length()==0))
						old=I.name();
					str.append(old);
					break;
				case 1: // classes
					{
						if((old==null)||(old.length()==0))
							old=CMClass.className(I); 
						for(int r=0;r<CMClass.items.size();r++)
						{
							Exit cnam=(Exit)CMClass.items.elementAt(r);
							str.append("<OPTION VALUE=\""+CMClass.className(cnam)+"\"");
							if(old.equalsIgnoreCase(CMClass.className(cnam)))
								str.append(" SELECTED");
							str.append(">"+CMClass.className(cnam));
						}
						for(int r=0;r<CMClass.weapons.size();r++)
						{
							Exit cnam=(Exit)CMClass.weapons.elementAt(r);
							str.append("<OPTION VALUE=\""+CMClass.className(cnam)+"\"");
							if(old.equalsIgnoreCase(CMClass.className(cnam)))
								str.append(" SELECTED");
							str.append(">"+CMClass.className(cnam));
						}
						for(int r=0;r<CMClass.armor.size();r++)
						{
							Exit cnam=(Exit)CMClass.armor.elementAt(r);
							str.append("<OPTION VALUE=\""+CMClass.className(cnam)+"\"");
							if(old.equalsIgnoreCase(CMClass.className(cnam)))
								str.append(" SELECTED");
							str.append(">"+CMClass.className(cnam));
						}
						for(int r=0;r<CMClass.miscMagic.size();r++)
						{
							Exit cnam=(Exit)CMClass.miscMagic.elementAt(r);
							str.append("<OPTION VALUE=\""+CMClass.className(cnam)+"\"");
							if(old.equalsIgnoreCase(CMClass.className(cnam)))
								str.append(" SELECTED");
							str.append(">"+CMClass.className(cnam));
						}
					}
					break;
				case 2: // displaytext
					if((old==null)||(old.length()==0))
						old=I.displayText(); 
					str.append(old);
					break;
				case 3: // description
					if((old==null)||(old.length()==0))
						old=I.description(); 
					str.append(old);
					break;
				case 4: // level
					if((old==null)||(old.length()==0))
						old=""+I.baseEnvStats().level(); 
					str.append(old);
					break;
				case 5: // ability;
					if((old==null)||(old.length()==0))
						old=""+I.baseEnvStats().armor(); 
					str.append(old);
					break;
				case 6: // rejuv;
					if((old==null)||(old.length()==0))
						old=""+I.baseEnvStats().rejuv(); 
					str.append(old);
					break;
				case 7: // misctext
					if((old==null)||(old.length()==0))
						old=I.text(); 
					str.append(old);
					break;
				case 8: // materials
					if((old==null)||(old.length()==0))
						old=""+I.material();
					for(int r=0;r<EnvResource.RESOURCE_DESCS.length;r++)
					{
						str.append("<OPTION VALUE=\""+EnvResource.RESOURCE_DATA[r][0]+"\"");
						if(r==Util.s_int(old))
							str.append(" SELECTED");
						str.append(">"+EnvResource.RESOURCE_DESCS[r]);
					}
					break;
				case 9: // is generic
					if(I.isGeneric()) return "true";
					else return "false";
				case 10: // is food
					if(I instanceof Food) return "true";
					else return "false";
				case 11: // nourishment
					if(((old==null)||(old.length()==0))&&(I instanceof Food))
						old=""+((Food)I).nourishment();
					str.append(old);
					break;
				case 12: // is drink
					if(I instanceof Drink) return "true";
					else return "false";
				case 13: // liquid held
					if(((old==null)||(old.length()==0))&&(I instanceof Drink))
						old=""+((Drink)I).liquidHeld();
					str.append(old);
					break;
				case 14: // quenched
					if(((old==null)||(old.length()==0))&&(I instanceof Drink))
						old=""+((Drink)I).thirstQuenched();
					str.append(old);
					break;
				case 15: // is container
					if(I instanceof Container) return "true";
					else return "false";
				case 16: // capacity
					if(((old==null)||(old.length()==0))&&(I instanceof Container))
						old=""+((Container)I).capacity();
					str.append(old);
					break;
				case 17: // is armor
					if(I instanceof Armor) return "true";
					else return "false";
				case 18: // armor
					if((old==null)||(old.length()==0))
						old=""+I.baseEnvStats().armor(); 
					str.append(old);
					break;
				case 19: // worn data
					long climate=I.rawWornCode();
					if(httpReq.getRequestParameters().containsKey("WORNDATA"))
					{
						climate=Util.s_int((String)httpReq.getRequestParameters().get("WORNDATA"));
						for(int i=1;;i++)
							if(httpReq.getRequestParameters().containsKey("WORNDATA"+(new Integer(i).toString())))
								climate=climate|Util.s_int((String)httpReq.getRequestParameters().get("CLIMATE"+(new Integer(i).toString())));
							else
								break;
					}
					for(int i=1;i<Item.wornLocation.length;i++)
					{
						String climstr=Item.wornLocation[i];
						int mask=Util.pow(2,i-1);
						str.append("<OPTION VALUE="+mask);
						if((climate&mask)>0) str.append(" SELECTED");
						str.append(">"+climstr);
					}
					break;
				case 20: // height
					if((old==null)||(old.length()==0))
						old=""+I.baseEnvStats().height(); 
					str.append(old);
					break;
				case 21: // is weapon
					if(I instanceof Weapon) return "true";
					else return "false";
				case 22: // weapon type
					break;
				case 23: // weapon class
					break;
				case 24: // attack
					if((old==null)||(old.length()==0))
						old=""+I.baseEnvStats().attackAdjustment(); 
					str.append(old);
					break;
				case 25: // damage
					if((old==null)||(old.length()==0))
						old=""+I.baseEnvStats().damage(); 
					str.append(old);
					break;
				case 26: // min range
					if(((old==null)||(old.length()==0)))
						old=""+I.minRange();
					str.append(old);
					break;
				case 27: // max range
					if(((old==null)||(old.length()==0)))
						old=""+I.maxRange();
					str.append(old);
					break;
				case 28: // secret identity
					if((old==null)||(old.length()==0))
						old=I.rawSecretIdentity(); 
					str.append(old);
					break;
				case 29: // is gettable
					if(old==null)
						old=I.isGettable()?"checked":""; 
					else 
					if(old.equals("on")) 
						old="checked";
					str.append(old);
					break;
				case 30: // is removable
					if(old==null)
						old=I.isRemovable()?"checked":""; 
					else 
					if(old.equals("on")) 
						old="checked";
					str.append(old);
					break;
				case 31: // is droppable
					if(old==null)
						old=I.isDroppable()?"checked":""; 
					else 
					if(old.equals("on")) 
						old="checked";
					str.append(old);
					break;
				case 32: // is two handed
					if(old==null)
						old=I.rawLogicalAnd()?"checked":""; 
					else 
					if(old.equals("on")) 
						old="checked";
					str.append(old);
					break;
				case 33: // is trapped
					if(old==null)
						old=I.isTrapped()?"checked":""; 
					else 
					if(old.equals("on")) 
						old="checked";
					str.append(old);
					break;
				case 34: // readable spells
					if(I instanceof Potion)
						old=";"+((Potion)I).getSpellList();
					if(I instanceof Pill)
						old=";"+((Pill)I).getSpellList();
					if(httpReq.getRequestParameters().containsKey("READABLESPELLS"))
					{
						old=";"+(String)httpReq.getRequestParameters().get("READABLESPELLS");
						for(int i=1;;i++)
							if(httpReq.getRequestParameters().containsKey("READABLESPELLS"+(new Integer(i).toString())))
								old+=";"+(String)httpReq.getRequestParameters().get("READABLESPELLS"+(new Integer(i).toString()));
							else
								break;
					}
					old=old.toUpperCase()+";";
					for(int i=1;i<CMClass.abilities.size();i++)
					{
						Ability A2=(Ability)CMClass.abilities.elementAt(i);
						str.append("<OPTION VALUE=\""+A2.ID()+"\"");
						if(old.indexOf(";"+A2.ID().toUpperCase()+";")>=0) str.append(" SELECTED");
						str.append(">"+A2.name());
					}
					break;
				case 35: // is wand
					if(I instanceof Wand) return "true";
					else return "false";
				case 36: // uses
					if((old==null)||(old.length()==0))
						old=""+I.usesRemaining(); 
					str.append(old);
					break;
				case 37: // value
					if((old==null)||(old.length()==0))
						old=""+I.value(); 
					str.append(old);
					break;
				case 38: // weight
					if((old==null)||(old.length()==0))
						old=""+I.baseEnvStats().weight(); 
					str.append(old);
					break;
				case 39: // is map
					if(I instanceof com.planet_ink.coffee_mud.interfaces.Map) return "true";
					else return "false";
				case 40: // map areas
					String mask=";"+I.readableText();
					if(httpReq.getRequestParameters().containsKey("MAPAREAS"))
					{
						mask=";"+(String)httpReq.getRequestParameters().get("MAPAREAS");
						for(int i=1;;i++)
							if(httpReq.getRequestParameters().containsKey("MAPAREAS"+(new Integer(i).toString())))
								mask+=";"+(String)httpReq.getRequestParameters().get("MAPAREAS"+(new Integer(i).toString()));
							else
								break;
					}
					mask=mask.toUpperCase()+";";
					for(int i=1;i<CMMap.numAreas();i++)
					{
						Area A2=CMMap.getArea(i);
						str.append("<OPTION VALUE=\""+A2.name()+"\"");
						if(mask.indexOf(";"+A2.name().toUpperCase()+";")>=0) str.append(" SELECTED");
						str.append(">"+A2.name());
					}
					break;
				case 41: // is readable
					if(old==null)
						old=I.isReadable()?"checked":""; 
					else 
					if(old.equals("on")) 
						old="checked";
					str.append(old);
					break;
				case 42: // is pill
					if(I instanceof Pill) return "true";
					else return "false";
				case 43: // is super pill
					if((I instanceof Pill)&&(CMClass.className(I).indexOf("SuperPill")>0)) return "true";
					else return "false";
				case 44: // is potion
					if(I instanceof Potion) return "true";
					else return "false";
				case 45: // liquid types
					if(((old==null)||(old.length()==0))&&(I instanceof Drink))
						old=""+((Drink)I).liquidType();
					for(int r=0;r<EnvResource.RESOURCE_DESCS.length;r++)
					{
						if((EnvResource.RESOURCE_DATA[r][0]&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LIQUID)
						{
							str.append("<OPTION VALUE=\""+EnvResource.RESOURCE_DATA[r][0]+"\"");
							if(r==Util.s_int(old))
								str.append(" SELECTED");
							str.append(">"+EnvResource.RESOURCE_DESCS[r]);
						}
					}
					break;
				case 46: // ammo types
					if(((old==null)||(old.length()==0))&&(I instanceof Weapon))
						old=""+((Weapon)I).ammunitionType();
					str.append(old);
					break;
				case 47: // ammo capacity
					if(((old==null)||(old.length()==0))&&(I instanceof Weapon))
						old=""+((Weapon)I).ammunitionCapacity();
					str.append(old);
					break;
				case 48: // readable spell
					if(((old==null)||(old.length()==0))&&(I instanceof Wand))
						old=""+((((Wand)I).getSpell()!=null)?((Wand)I).getSpell().ID():"");
					for(int r=0;r<CMClass.abilities.size();r++)
					{
						Ability A=(Ability)CMClass.abilities.elementAt(r);
						str.append("<OPTION VALUE=\""+A.ID()+"\"");
						if(old.equals(A.ID()))
							str.append(" SELECTED");
						str.append(">"+A.name());
					}
					break;
				case 49: // is map
					if(I instanceof Rideable) return "true";
					else return "false";
				case 50: // rideable type
					if(((old==null)||(old.length()==0))&&(I instanceof Rideable))
						old=""+((Rideable)I).rideBasis();
					for(int r=0;r<Rideable.RIDEABLE_DESCS.length;r++)
					{
						str.append("<OPTION VALUE=\""+r+"\"");
						if(r==Util.s_int(old))
							str.append(" SELECTED");
						str.append(">"+Rideable.RIDEABLE_DESCS[r]);
					}
					break;
				case 51: // ammo capacity
					if(((old==null)||(old.length()==0))&&(I instanceof Rideable))
						old=""+((Rideable)I).mobCapacity();
					str.append(old);
					break;
				case 52: // has a lid
					if((old==null)&&(I instanceof Container))
						old=((Container)I).hasALid()?"checked":""; 
					else 
					if(old.equals("on")) 
						old="checked";
					str.append(old);
					break;
				case 53: // has a lock
					if((old==null)&&(I instanceof Container))
						old=((Container)I).hasALock()?"checked":""; 
					else 
					if(old.equals("on")) 
						old="checked";
					str.append(old);
					break;
				case 54: // key code
					if(((old==null)||(old.length()==0))&&(I instanceof Container))
						old=""+((Container)I).keyName();
					str.append(old);
					break;
				case 55: // is wallpaper
					if(CMClass.className(I).indexOf("Wallpaper")>0) return "true";
					else return "false";
				case 56: // readabletext
					if(((old==null)||(old.length()==0)))
						old=""+I.readableText();
					str.append(old);
					break;
				}
				if((oldold==null)&&(old!=null))
				{
					resetIfNecessary=true;
					httpReq.getRequestParameters().put(okparms[o],old.equals("checked")?"on":old);
				}
				
			}
			str.append(ExitData.dispositions(I,httpReq,parms));
			str.append(AreaData.affectsNBehaves(I,httpReq,parms));
			I.recoverEnvStats();
			I.text();
			ExternalPlay.DBUpdateExits(R);
			
			if(resetIfNecessary)
				httpReq.resetRequestEncodedParameters();
			
			String strstr=str.toString();
			if(strstr.endsWith(", "))
				strstr=strstr.substring(0,strstr.length()-2);
			return strstr;
		}
		return "";
	}
}
