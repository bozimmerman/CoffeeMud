package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.*;
import java.io.*;
import com.planet_ink.coffee_mud.utils.Log;
import com.planet_ink.coffee_mud.utils.CoffeeUtensils;

public class CMClass
{
	public static Vector races=new Vector();
	public static Vector charClasses=new Vector();
	public static Vector MOBs=new Vector();
	public static Vector abilities=new Vector();
	public static Vector locales=new Vector();
	public static Vector exits=new Vector();
	public static Vector items=new Vector();
	public static Vector behaviors=new Vector();
	public static Vector weapons=new Vector();
	public static Vector armor=new Vector();
	public static Vector miscMagic=new Vector();

	public static Item getItem(String calledThis)
	{
		Item thisItem=(Item)getEnv(items,calledThis);
		if(thisItem==null)
			thisItem=(Item)getEnv(armor,calledThis);
		if(thisItem==null)
			thisItem=(Item)getEnv(weapons,calledThis);
		if(thisItem==null)
			thisItem=(Item)getEnv(miscMagic,calledThis);
		if(thisItem!=null)
			thisItem=(Item)thisItem.newInstance();
		else
		if((items.size()>0)&&(armor.size()>0)&&(weapons.size()>0)&&(miscMagic.size()>0))
			Log.sysOut("CMClass","Unknown Item '"+calledThis+"'.");
		return thisItem;
	}

	public static Environmental getUnknown(String calledThis)
	{
		Environmental thisItem=getEnv(items,calledThis);
		if(thisItem==null)
			thisItem=getEnv(armor,calledThis);
		if(thisItem==null)
			thisItem=getEnv(weapons,calledThis);
		if(thisItem==null)
			thisItem=getEnv(miscMagic,calledThis);
		if(thisItem==null)
		{
			for(int i=0;i<MOBs.size();i++)
			{
				MOB mob=(MOB)MOBs.elementAt(i);

				if(className(mob).equalsIgnoreCase(calledThis))
				{
					thisItem=mob;
					break;
				}
			}
		}
		if(thisItem==null)
			thisItem=(Environmental)getGlobal(abilities,calledThis);
		
		if(thisItem!=null)
			thisItem=thisItem.newInstance();
		
		if((thisItem==null)&&(charClasses.size()>0)&&(calledThis.length()>0))
			Log.sysOut("CMClass","Unknown Unknown '"+calledThis+"'.");
		
		return thisItem;
	}
	
	public static Weapon getWeapon(String calledThis)
	{
		Weapon thisItem=(Weapon)getEnv(weapons,calledThis);
		if((thisItem==null)&&(weapons.size()>0)&&(calledThis.length()>0))
			Log.sysOut("CMClass","Unknown Weapon '"+calledThis+"'.");
		return thisItem;
	}
	public static Item getMiscMagic(String calledThis)
	{
		Item thisItem=(Item)getEnv(miscMagic,calledThis);
		if((thisItem==null)&&(miscMagic.size()>0)&&(calledThis.length()>0))
			Log.sysOut("CMClass","Unknown MiscMagic '"+calledThis+"'.");
		return thisItem;
	}
	public static Armor getArmor(String calledThis)
	{
		Armor thisItem=(Armor)getEnv(armor,calledThis);
		if((thisItem==null)&&(armor.size()>0)&&(calledThis.length()>0))
			Log.sysOut("CMClass","Unknown Armor '"+calledThis+"'.");
		return thisItem;
	}
	public static Item getStdItem(String calledThis)
	{
		Item thisItem=(Item)getEnv(items,calledThis);
		if((thisItem==null)&&(items.size()>0)&&(calledThis.length()>0))
			Log.sysOut("CMClass","Unknown StdItem '"+calledThis+"'.");
		return thisItem;
	}

	public static CharClass getCharClass(String calledThis)
	{
		CharClass thisItem= (CharClass)getGlobal(charClasses,calledThis);
		if((thisItem==null)&&(charClasses.size()>0))
		{
			for(int i=0;i<charClasses.size();i++)
				if(((CharClass)(charClasses.elementAt(i))).name().equalsIgnoreCase(calledThis))
					return (CharClass)charClasses.elementAt(i);
			Log.sysOut("CMClass","Unknown CharClass '"+calledThis+"'.");
		}
		return thisItem;
	}
	public static Race getRace(String calledThis)
	{
		Race thisItem= (Race)getGlobal(races,calledThis);
		if((thisItem==null)&&(races.size()>0))
		{
			for(int i=0;i<races.size();i++)
				if(((Race)(races.elementAt(i))).name().equalsIgnoreCase(calledThis))
					return (Race)races.elementAt(i);
			Log.sysOut("CMClass","Unknown Race '"+calledThis+"'.");
		}
		return thisItem;
	}
	public static Behavior getBehavior(String calledThis)
	{
		Behavior B=(Behavior)getGlobal(behaviors,calledThis);
		if(B!=null) 
			B=B.newInstance();
		else
		if((behaviors.size()>0)&&(calledThis.length()>0)&&(calledThis.length()>0))
			Log.sysOut("CMClass","Unknown Behavior '"+calledThis+"'.");
		return B;
	}
	public static Room getLocale(String calledThis)
	{
		Room thisItem= (Room)getEnv(locales,calledThis);
		if((thisItem==null)&&(locales.size()>0)&&(calledThis.length()>0))
			Log.sysOut("CMClass","Unknown Locale '"+calledThis+"'.");
		return thisItem;
	}
	public static Exit getExit(String calledThis)
	{
		Exit thisItem= (Exit)getEnv(exits,calledThis);
		if((thisItem==null)&&(exits.size()>0)&&(calledThis.length()>0))
			Log.sysOut("CMClass","Unknown Exit '"+calledThis+"'.");
		return thisItem;
	}
	public static MOB getMOB(String calledThis)
	{
		for(int i=0;i<MOBs.size();i++)
		{
			MOB mob=(MOB)MOBs.elementAt(i);

			if(className(mob).equalsIgnoreCase(calledThis))
				return (MOB)mob.newInstance();
		}
		if((MOBs.size()>0)&&(calledThis.length()>0))
			Log.sysOut("CMClass","Unknown MOB '"+calledThis+"'.");
		return null;
	}
	public static Ability getAbility(String calledThis)
	{
		Ability A=(Ability)getGlobal(abilities,calledThis);
		if(A!=null)
			A=(Ability)A.newInstance();
		else
		if((abilities.size()>0)
		&&(calledThis.length()>0)
		&&(calledThis.indexOf("+")<0)
		&&(calledThis.indexOf("-")<0)
		&&(calledThis.indexOf("=")<0))
			Log.sysOut("CMClass","Unknown Ability '"+calledThis+"'.");
		return A;
	}
	public static Ability findAbility(String calledThis)
	{
		Ability A=(Ability)CoffeeUtensils.fetchEnvironmental(abilities,calledThis,true);
		if(A==null)
			A=(Ability)CoffeeUtensils.fetchEnvironmental(abilities,calledThis,false);
		if(A!=null)A=(Ability)A.newInstance();
		return A;
	}

	public static Environmental getEnv(Vector fromThese, String calledThis)
	{
		for(int i=0;i<fromThese.size();i++)
		{
			Environmental E=(Environmental)fromThese.elementAt(i);
			if(E.ID().equalsIgnoreCase(calledThis))
				return E.newInstance();
		}
		return null;
	}

	public static Object getGlobal(Vector fromThese, String calledThis)
	{
		for(int i=0;i<fromThese.size();i++)
			if(CoffeeUtensils.id(fromThese.elementAt(i)).equalsIgnoreCase(calledThis))
				return fromThese.elementAt(i);
		return null;
	}

	public static boolean loadClasses()
	{
		String prefix="com"+File.separatorChar+"planet_ink"+File.separatorChar+"coffee_mud"+File.separatorChar;

		races=loadVectorListToObj(prefix+"Races"+File.separatorChar);
		Log.sysOut("MUD","Races loaded      : "+races.size());
		if(races.size()==0) return false;

		charClasses=loadVectorListToObj(prefix+"CharClasses"+File.separatorChar);
		Log.sysOut("MUD","Classes loaded    : "+charClasses.size());
		if(charClasses.size()==0) return false;

		MOBs=loadVectorListToObj(prefix+"MOBS"+File.separatorChar);
		Log.sysOut("MUD","MOB Types loaded  : "+MOBs.size());
		if(MOBs.size()==0) return false;

		exits=loadVectorListToObj(prefix+"Exits"+File.separatorChar);
		Log.sysOut("MUD","Exit Types loaded : "+exits.size());
		if(exits.size()==0) return false;

		locales=loadVectorListToObj(prefix+"Locales"+File.separatorChar);
		Log.sysOut("MUD","Locales loaded    : "+locales.size());
		if(locales.size()==0) return false;

		abilities=loadVectorListToObj(prefix+"Abilities"+File.separatorChar);
		Log.sysOut("MUD","Abilities loaded  : "+abilities.size());
		if(abilities.size()==0) return false;

		Vector tempV;
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Archon"+File.separatorChar);
		Log.sysOut("MUD","           Archon : "+tempV.size());
		if(tempV.size()==0) return false; addV(tempV,abilities);
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Fighter"+File.separatorChar);
		Log.sysOut("MUD","          Fighter : "+tempV.size());
		if(tempV.size()==0) return false; addV(tempV,abilities);
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Misc"+File.separatorChar);
		Log.sysOut("MUD","             Misc : "+tempV.size());
		if(tempV.size()==0) return false; addV(tempV,abilities);
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Prayers"+File.separatorChar);
		Log.sysOut("MUD","          Prayers : "+tempV.size());
		if(tempV.size()==0) return false; addV(tempV,abilities);
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Properties"+File.separatorChar);
		Log.sysOut("MUD","       Properties : "+tempV.size());
		if(tempV.size()==0) return false; addV(tempV,abilities);
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Skills"+File.separatorChar);
		Log.sysOut("MUD","           Skills : "+tempV.size());
		if(tempV.size()==0) return false; addV(tempV,abilities);
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Songs"+File.separatorChar);
		Log.sysOut("MUD","            Songs : "+tempV.size());
		if(tempV.size()==0) return false; addV(tempV,abilities);
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Specializations"+File.separatorChar);
		Log.sysOut("MUD","  Specializations : "+tempV.size());
		if(tempV.size()==0) return false; addV(tempV,abilities);
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Spells"+File.separatorChar);
		Log.sysOut("MUD","           Spells : "+tempV.size());
		if(tempV.size()==0)  return false; addV(tempV,abilities);
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Thief"+File.separatorChar);
		Log.sysOut("MUD","            Thief : "+tempV.size());
		if(tempV.size()==0)  return false; addV(tempV,abilities);
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Traps"+File.separatorChar);
		Log.sysOut("MUD","            Traps : "+tempV.size());
		if(tempV.size()==0)  return false; addV(tempV,abilities);

		items=loadVectorListToObj(prefix+"Items"+File.separatorChar);
		Log.sysOut("MUD","Items loaded      : "+items.size());
		if(items.size()==0) return false;

		weapons=loadVectorListToObj(prefix+"Items"+File.separatorChar+"Weapons"+File.separatorChar);
		Log.sysOut("MUD","Weapons loaded    : "+weapons.size());
		if(weapons.size()==0) return false;

		armor=loadVectorListToObj(prefix+"Items"+File.separatorChar+"Armor"+File.separatorChar);
		Log.sysOut("MUD","Armor loaded      : "+armor.size());
		if(armor.size()==0) return false;

		miscMagic=loadVectorListToObj(prefix+"Items"+File.separatorChar+"MiscMagic"+File.separatorChar);
		Log.sysOut("MUD","Magic Items loaded: "+miscMagic.size());
		if(miscMagic.size()==0) return false;

		behaviors=loadVectorListToObj(prefix+"Behaviors"+File.separatorChar);
		Log.sysOut("MUD","Behaviors loaded  : "+behaviors.size());
		if(behaviors.size()==0) return false;
		return true;
	}

	public static int addV(Vector addMe, Vector toMe)
	{
		for(int v=0;v<addMe.size();v++)
			toMe.addElement(addMe.elementAt(v));
		return addMe.size();
	}

	public static void unload()
	{
		races=new Vector();
		charClasses=new Vector();
		MOBs=new Vector();
		abilities=new Vector();
		locales=new Vector();
		exits=new Vector();
		items=new Vector();
		behaviors=new Vector();
		weapons=new Vector();
		armor=new Vector();
		miscMagic=new Vector();
	}

	public static Hashtable loadHashListToObj(String filePath)
	{
		Hashtable h=new Hashtable();
		loadListToObj(h,filePath);
		return h;
	}
	public static Vector loadVectorListToObj(String filePath)
	{
		Vector v=new Vector();
		loadListToObj(v,filePath);
		return v;
	}
	public static void loadListToObj(Object toThis, String filePath)
	{
		StringBuffer objPathBuf=new StringBuffer(filePath);
		String objPath=objPathBuf.toString();
		int x=0;
		while((x=objPath.indexOf(File.separatorChar))>=0)
		{
			objPathBuf.setCharAt(x,'.');
			objPath=objPathBuf.toString();
		}
		File directory=new File(filePath);
		if((directory.canRead())&&(directory.isDirectory()))
		{
			String[] list=directory.list();
			for(int l=0;l<list.length;l++)
			{
				String item=list[l];
				if((item!=null)&&(item.length()>0))
				{
					if(item.toUpperCase().endsWith(".CLASS")&&(item.indexOf("$")<0))
					{
						item=item.substring(0,item.length()-6);
						try
						{
							Object O=(Object)Class.forName(objPath+item).newInstance();
							if(toThis instanceof Hashtable)
								((Hashtable)toThis).put(item.trim(),O);
							else
							if(toThis instanceof Vector)
								((Vector)toThis).addElement(O);
						}
						catch(Exception e)
						{
							if((!item.endsWith("Child"))&&(!item.endsWith("Room")))
								Log.sysOut("CMClass","Couldn't load: "+objPath+item);
						}
					}
				}
			}
		}
	}

	public static String className(Object O)
	{
		if(O==null) return "";
		String name=O.getClass().getName();
		int lastDot=name.lastIndexOf(".");
		if(lastDot>=0)
			return name.substring(lastDot+1);
		else
			return name;
	}
	
}
