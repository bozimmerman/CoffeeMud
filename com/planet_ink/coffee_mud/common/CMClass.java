package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.Modifier;
import com.planet_ink.coffee_mud.utils.Log;
import com.planet_ink.coffee_mud.utils.INI;
import com.planet_ink.coffee_mud.utils.CoffeeUtensils;


public class CMClass extends ClassLoader
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
	public static Vector areaTypes=new Vector();
	public static Hashtable extraCmds=new Hashtable();

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
		//else
		//if((items.size()>0)&&(armor.size()>0)&&(weapons.size()>0)&&(miscMagic.size()>0))
		//	Log.sysOut("CMClass","Unknown Item '"+calledThis+"'.");
		return thisItem;
	}
	
	public static Command findExtraCommand(String word)
	{
		return (Command)extraCmds.get(word.trim().toUpperCase());
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
		//if((thisItem==null)&&(weapons.size()>0)&&(calledThis.length()>0))
		//	Log.sysOut("CMClass","Unknown Weapon '"+calledThis+"'.");
		return thisItem;
	}
	public static Item getMiscMagic(String calledThis)
	{
		Item thisItem=(Item)getEnv(miscMagic,calledThis);
		//if((thisItem==null)&&(miscMagic.size()>0)&&(calledThis.length()>0))
		//	Log.sysOut("CMClass","Unknown MiscMagic '"+calledThis+"'.");
		return thisItem;
	}
	public static Armor getArmor(String calledThis)
	{
		Armor thisItem=(Armor)getEnv(armor,calledThis);
		//if((thisItem==null)&&(armor.size()>0)&&(calledThis.length()>0))
		//	Log.sysOut("CMClass","Unknown Armor '"+calledThis+"'.");
		return thisItem;
	}
	public static Item getStdItem(String calledThis)
	{
		Item thisItem=(Item)getEnv(items,calledThis);
		//if((thisItem==null)&&(items.size()>0)&&(calledThis.length()>0))
		//	Log.sysOut("CMClass","Unknown StdItem '"+calledThis+"'.");
		return thisItem;
	}

	public static CharClass getCharClass(String calledThis)
	{
		CharClass thisItem= (CharClass)getGlobal(charClasses,calledThis);
		if((thisItem==null)&&(charClasses.size()>0))
		{
			for(int i=0;i<charClasses.size();i++)
			{
				CharClass C=(CharClass)charClasses.elementAt(i);
				if(C.name().equalsIgnoreCase(calledThis))
					return C;
			}
			//Log.sysOut("CMClass","Unknown CharClass '"+calledThis+"'.");
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
			//Log.sysOut("CMClass","Unknown Race '"+calledThis+"'.");
		}
		return thisItem;
	}
	public static Behavior getBehavior(String calledThis)
	{
		Behavior B=(Behavior)getGlobal(behaviors,calledThis);
		if(B!=null) 
			B=B.newInstance();
		//else
		//if((behaviors.size()>0)&&(calledThis.length()>0)&&(calledThis.length()>0))
		//	Log.sysOut("CMClass","Unknown Behavior '"+calledThis+"'.");
		return B;
	}
	public static Room getLocale(String calledThis)
	{
		Room thisItem= (Room)getEnv(locales,calledThis);
		//if((thisItem==null)&&(locales.size()>0)&&(calledThis.length()>0))
		//	Log.sysOut("CMClass","Unknown Locale '"+calledThis+"'.");
		return thisItem;
	}
	public static Area getAreaType(String calledThis)
	{
		Area thisItem= (Area)getEnv(areaTypes,calledThis);
		//if((thisItem==null)&&(areaTypes.size()>0)&&(calledThis.length()>0))
		//	Log.sysOut("CMClass","Unknown Area '"+calledThis+"'.");
		return thisItem;
	}
	public static Exit getExit(String calledThis)
	{
		Exit thisItem= (Exit)getEnv(exits,calledThis);
		//if((thisItem==null)&&(exits.size()>0)&&(calledThis.length()>0))
		//	Log.sysOut("CMClass","Unknown Exit '"+calledThis+"'.");
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
		//if((MOBs.size()>0)&&(calledThis.length()>0))
		//	Log.sysOut("CMClass","Unknown MOB '"+calledThis+"'.");
		return null;
	}
	public static Ability getAbility(String calledThis)
	{
		Ability A=(Ability)getGlobal(abilities,calledThis);
		if(A!=null)
			A=(Ability)A.newInstance();
		/*
		else
		if((abilities.size()>0)
		&&(calledThis.length()>0)
		&&(calledThis.indexOf("+")<0)
		&&(calledThis.indexOf("-")<0)
		&&(calledThis.indexOf("=")<0))
			Log.sysOut("CMClass","Unknown Ability '"+calledThis+"'.");
		*/
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
	public static Ability findAbility(String calledThis, CharStats charStats)
	{
		Vector ables=new Vector();
		for(int c=0;c<charStats.numClasses();c++)
		{
			CharClass C=charStats.getMyClass(c);
			for(int a=0;a<abilities.size();a++)
				if(CMAble.getQualifyingLevel(C.ID(),((Ability)abilities.elementAt(a)).ID())>=0)
					if(!ables.contains(abilities.elementAt(a)))
						ables.addElement(abilities.elementAt(a));
		}
		Ability A=(Ability)CoffeeUtensils.fetchEnvironmental(ables,calledThis,true);
		if(A==null)
			A=(Ability)CoffeeUtensils.fetchEnvironmental(ables,calledThis,false);
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

	public static boolean loadClasses(INI page)
	{
		String prefix="com"+File.separatorChar+"planet_ink"+File.separatorChar+"coffee_mud"+File.separatorChar;

		races=loadVectorListToObj(prefix+"Races"+File.separatorChar,page.getStr("RACES"),"com.planet_ink.coffee_mud.interfaces.Race");
		Log.sysOut("MUD","Races loaded      : "+races.size());
		if(races.size()==0) return false;
//-------------------------------------------------------

		charClasses=loadVectorListToObj(prefix+"CharClasses"+File.separatorChar,page.getStr("CHARCLASSES"),"com.planet_ink.coffee_mud.interfaces.CharClass");
		Log.sysOut("MUD","Classes loaded    : "+charClasses.size());
		if(charClasses.size()==0) return false;
//-------------------------------------------------------

		MOBs=loadVectorListToObj(prefix+"MOBS"+File.separatorChar,page.getStr("MOBS"),"com.planet_ink.coffee_mud.interfaces.MOB");
		Log.sysOut("MUD","MOB Types loaded  : "+MOBs.size());
		if(MOBs.size()==0) return false;
//-------------------------------------------------------

		exits=loadVectorListToObj(prefix+"Exits"+File.separatorChar,page.getStr("EXITS"),"com.planet_ink.coffee_mud.interfaces.Exit");
		Log.sysOut("MUD","Exit Types loaded : "+exits.size());
		if(exits.size()==0) return false;
//-------------------------------------------------------

		areaTypes=loadVectorListToObj(prefix+"Areas"+File.separatorChar,page.getStr("AREAS"),"com.planet_ink.coffee_mud.interfaces.Area");
		Log.sysOut("MUD","Area Types loaded : "+areaTypes.size());
		if(areaTypes.size()==0) return false;
//-------------------------------------------------------

		locales=loadVectorListToObj(prefix+"Locales"+File.separatorChar,page.getStr("LOCALES"),"com.planet_ink.coffee_mud.interfaces.Room");
		Log.sysOut("MUD","Locales loaded    : "+locales.size());
		if(locales.size()==0) return false;
//-------------------------------------------------------

		abilities=loadVectorListToObj(prefix+"Abilities"+File.separatorChar,page.getStr("ABILITIES"),"com.planet_ink.coffee_mud.interfaces.Ability");
		if(abilities.size()==0) return false;

		Vector tempV;
		int size=0;
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Fighter"+File.separatorChar,"","com.planet_ink.coffee_mud.interfaces.Ability");
		size=tempV.size();
		if(tempV.size()==0) return false; addV(tempV,abilities);
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Ranger"+File.separatorChar,"","com.planet_ink.coffee_mud.interfaces.Ability");
		size+=tempV.size();
		if(tempV.size()==0) return false; addV(tempV,abilities);
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Paladin"+File.separatorChar,"","com.planet_ink.coffee_mud.interfaces.Ability");
		size+=tempV.size();
		if(tempV.size()==0) return false; addV(tempV,abilities);
		Log.sysOut("MUD","Fighter Skills    : "+size);
//-------------------------------------------------------
		
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Druid"+File.separatorChar,"","com.planet_ink.coffee_mud.interfaces.Ability");
		Log.sysOut("MUD","Chants loaded     : "+tempV.size());
		if(tempV.size()==0) return false; addV(tempV,abilities);
//-------------------------------------------------------
		
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Languages"+File.separatorChar,"","com.planet_ink.coffee_mud.interfaces.Ability");
		Log.sysOut("MUD","Languages loaded  : "+tempV.size());
		if(tempV.size()==0)  return false; addV(tempV,abilities);
//-------------------------------------------------------
		
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Properties"+File.separatorChar,"","com.planet_ink.coffee_mud.interfaces.Ability");
		size=tempV.size();
		if(tempV.size()==0) return false; addV(tempV,abilities);
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Misc"+File.separatorChar,"","com.planet_ink.coffee_mud.interfaces.Ability");
		size+=tempV.size();
		if(tempV.size()==0) return false; addV(tempV,abilities);
		Log.sysOut("MUD","Properties loaded : "+size);
//-------------------------------------------------------
		
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Prayers"+File.separatorChar,"","com.planet_ink.coffee_mud.interfaces.Ability");
		Log.sysOut("MUD","Prayers loaded    : "+tempV.size());
		if(tempV.size()==0) return false; addV(tempV,abilities);
//-------------------------------------------------------
		
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Archon"+File.separatorChar,"","com.planet_ink.coffee_mud.interfaces.Ability");
		size+=tempV.size();
		if(tempV.size()==0) return false; addV(tempV,abilities);
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Skills"+File.separatorChar,"","com.planet_ink.coffee_mud.interfaces.Ability");
		size=tempV.size();
		if(tempV.size()==0) return false; addV(tempV,abilities);
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Thief"+File.separatorChar,"","com.planet_ink.coffee_mud.interfaces.Ability");
		size+=tempV.size();
		if(tempV.size()==0) return false; addV(tempV,abilities);
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Common"+File.separatorChar,"","com.planet_ink.coffee_mud.interfaces.Ability");
		size+=tempV.size();
		if(tempV.size()==0) return false; addV(tempV,abilities);
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Specializations"+File.separatorChar,"","com.planet_ink.coffee_mud.interfaces.Ability");
		size+=tempV.size();
		if(tempV.size()==0)  return false; addV(tempV,abilities);
		Log.sysOut("MUD","Skills loaded     : "+size);
//-------------------------------------------------------
		
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Songs"+File.separatorChar,"","com.planet_ink.coffee_mud.interfaces.Ability");
		Log.sysOut("MUD","Songs loaded      : "+tempV.size());
		if(tempV.size()==0) return false; addV(tempV,abilities);
//-------------------------------------------------------
		
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Spells"+File.separatorChar,"","com.planet_ink.coffee_mud.interfaces.Ability");
		Log.sysOut("MUD","Spells loaded     : "+tempV.size());
		if(tempV.size()==0)  return false; addV(tempV,abilities);
//-------------------------------------------------------
		tempV=loadVectorListToObj(prefix+"Abilities"+File.separatorChar+"Traps"+File.separatorChar,"","com.planet_ink.coffee_mud.interfaces.Ability");
		Log.sysOut("MUD","Traps loaded      : "+tempV.size());
		if(tempV.size()==0)  return false; addV(tempV,abilities);
//-------------------------------------------------------

		items=loadVectorListToObj(prefix+"Items"+File.separatorChar,page.getStr("ITEMS"),"com.planet_ink.coffee_mud.interfaces.Item");
		Log.sysOut("MUD","Items loaded      : "+items.size());
		if(items.size()==0) return false;
//-------------------------------------------------------

		weapons=loadVectorListToObj(prefix+"Items"+File.separatorChar+"Weapons"+File.separatorChar,"","com.planet_ink.coffee_mud.interfaces.Weapon");
		Log.sysOut("MUD","Weapons loaded    : "+weapons.size());
		if(weapons.size()==0) return false;
//-------------------------------------------------------

		armor=loadVectorListToObj(prefix+"Items"+File.separatorChar+"Armor"+File.separatorChar,"","com.planet_ink.coffee_mud.interfaces.Armor");
		Log.sysOut("MUD","Armor loaded      : "+armor.size());
		if(armor.size()==0) return false;
//-------------------------------------------------------

		miscMagic=loadVectorListToObj(prefix+"Items"+File.separatorChar+"MiscMagic"+File.separatorChar,"","com.planet_ink.coffee_mud.interfaces.MiscMagic");
		Log.sysOut("MUD","Magic Items loaded: "+miscMagic.size());
		if(miscMagic.size()==0) return false;
//-------------------------------------------------------

		behaviors=loadVectorListToObj(prefix+"Behaviors"+File.separatorChar,page.getStr("BEHAVIORS"),"com.planet_ink.coffee_mud.interfaces.Behavior");
		Log.sysOut("MUD","Behaviors loaded  : "+behaviors.size());
		if(behaviors.size()==0) return false;
//-------------------------------------------------------
		
		Vector cmds=loadVectorListToObj(prefix+"Commands"+File.separatorChar,page.getStr("COMMANDS"),"com.planet_ink.coffee_mud.interfaces.Command");
		if(cmds.size()>1)
		{
			Log.sysOut("MUD","XCommands loaded  : "+cmds.size());
			for(int c=0;c<cmds.size();c++)
			{
				Command C=(Command)cmds.elementAt(c);
				Vector wordList=C.getAccessWords();
				for(int w=0;w<wordList.size();w++)
				{
					String word=(String)wordList.elementAt(w);
					extraCmds.put(word.trim().toUpperCase(),C);
				}
			}
		}
		
		// misc startup stuff
		for(int c=0;c<charClasses.size();c++)
		{
			CharClass C=(CharClass)charClasses.elementAt(c);
			C.copyOf();
		}
		for(int r=0;r<races.size();r++)
		{
			Race R=(Race)races.elementAt(r);
			R.copyOf();
		}
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
		areaTypes=new Vector();
	}

	public static Hashtable loadHashListToObj(String filePath, String auxPath, String ancestor)
	{
		Hashtable h=new Hashtable();
		loadListToObj(h,filePath,false, ancestor);
		if(auxPath.length()>0)
			loadListToObj(h,auxPath,true, ancestor);
		return h;
	}
	public static Vector loadVectorListToObj(String filePath, String auxPath, String ancestor)
	{
		Vector v=new Vector();
		loadListToObj(v,filePath,false, ancestor);
		if(auxPath.length()>0)
			loadListToObj(v,auxPath,true, ancestor);
		return v;
	}
	public static void loadListToObj(Object toThis, String filePath, boolean aux, String ancestor)
	{
		StringBuffer objPathBuf=new StringBuffer(filePath);
		String objPath=objPathBuf.toString();
		
		Class ancestorCl=null;
		if (ancestor != null && ancestor.length() != 0)
		{
			try
			{
				ancestorCl = Class.forName(ancestor);
			}
			catch (ClassNotFoundException e)
			{
				Log.sysOut("CMClass","WARNING: Couldn't load ancestor class: "+ancestor);
				ancestorCl = null;
			}
		}
		
		int x=0;
		if(!aux)
		while((x=objPath.indexOf(File.separatorChar))>=0)
		{
			objPathBuf.setCharAt(x,'.');
			objPath=objPathBuf.toString();
		}
		File directory=new File(filePath);
		CMClass loader=new CMClass();
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
						String itemName=item.substring(0,item.length()-6);
						try
						{
							Object O=null;
							if(aux)
							{
								File f=new File(objPath+item);
								byte[] data=new byte[(int)f.length()];
								new FileInputStream(f).read(data,0,(int)f.length());
								Class C=loader.defineClass(itemName, data, 0, data.length);
								loader.resolveClass(C);
								if (!checkClass(C,ancestorCl))
									O=null;
								else
									O=(Object)C.newInstance();
							}
							else
							{
								Class C=Class.forName(objPath+itemName);
								if (!checkClass(C,ancestorCl))
									O=null;
								else
									O=(Object)C.newInstance();
							}
							if(O!=null)
							{
								if(toThis instanceof Hashtable)
									((Hashtable)toThis).put(itemName.trim(),O);
								else
								if(toThis instanceof Vector)
									((Vector)toThis).addElement(O);
							}
						}
						catch(Exception e)
						{
							Log.errOut("CMCLASS",e);
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
	
	private static boolean checkClass(Class cl, Class ancestorCl)
	{
		if (cl == null)
			return false;

		// if it's a primitive or an interface, we're not interested
		if (cl.isPrimitive() || cl.isInterface())
			return false;
		
		// if it's abstract or not public, we're not interested either
		// the Class class lacks shortcut methods for these, 
		// so we use java.lang.reflect.Modifier
		if ( Modifier.isAbstract( cl.getModifiers()) || !Modifier.isPublic( cl.getModifiers()) )
			return false;
		
		// if no ancestor was specified, we're done
		if (ancestorCl == null)
			return true;
		
		return (ancestorCl.isAssignableFrom(cl)) ;
	}


}
