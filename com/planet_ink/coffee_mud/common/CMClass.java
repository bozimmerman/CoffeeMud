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
	private static Vector races=new Vector();
	private static Vector charClasses=new Vector();
	private static Vector MOBs=new Vector();
	private static Hashtable abilities=new Hashtable();
	private static Vector locales=new Vector();
	private static Vector exits=new Vector();
	private static Vector items=new Vector();
	private static Vector behaviors=new Vector();
	private static Vector clantypes=new Vector();
	private static Vector weapons=new Vector();
	private static Vector armor=new Vector();
	private static Vector miscMagic=new Vector();
	private static Vector areaTypes=new Vector();
	private static Hashtable extraCmds=new Hashtable();

	public static Enumeration races(){return races.elements();}
	public static Race randomRace(){return (Race)races.elementAt((int)Math.round(Math.floor(Math.random()*new Integer(races.size()).doubleValue())));}
	public static Enumeration charClasses(){return charClasses.elements();}
	public static CharClass randomCharClass(){return (CharClass)charClasses.elementAt((int)Math.round(Math.floor(Math.random()*new Integer(charClasses.size()).doubleValue())));}
	public static Enumeration mobTypes(){return MOBs.elements();}
	public static Enumeration locales(){return locales.elements();}
	public static Enumeration exits(){return exits.elements();}
	public static Enumeration behaviors(){return behaviors.elements();}
	public static Enumeration clanTypes(){return clantypes.elements();}
	public static Enumeration items(){return items.elements();}
	public static Enumeration weapons(){return weapons.elements();}
	public static Enumeration armor(){return armor.elements();}
	public static Enumeration miscMagic(){return miscMagic.elements();}
	public static Enumeration areaTypes(){return areaTypes.elements();}
	public static Enumeration extraCmds(){return extraCmds.elements();}
	public static Enumeration abilities(){return abilities.elements();}
	public static Ability randomAbility()
	{
		Vector V=new Vector(abilities.entrySet());
		return (Ability)V.elementAt((int)Math.round(Math.floor(Math.random()*new Integer(V.size()).doubleValue())));
	}
	
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
		return (Weapon)getEnv(weapons,calledThis);
	}
	public static Item getMiscMagic(String calledThis)
	{
		return (Item)getEnv(miscMagic,calledThis);
	}
	public static Armor getArmor(String calledThis)
	{
		return (Armor)getEnv(armor,calledThis);
	}
	public static Item getStdItem(String calledThis)
	{
		return (Item)getEnv(items,calledThis);
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
		}
		return thisItem;
	}
	public static Behavior getBehavior(String calledThis)
	{
		Behavior B=(Behavior)getGlobal(behaviors,calledThis);
		if(B!=null) 
			B=B.newInstance();
		return B;
	}
	public static Clan getClan(String calledThis)
	{
		Clan C=(Clan)getGlobal(clantypes,calledThis);
		if(C!=null) 
			C=C.newInstance();
		return C;
	}
	public static Room getLocale(String calledThis)
	{
		return (Room)getEnv(locales,calledThis);
	}
	public static Area getAreaType(String calledThis)
	{
		return (Area)getEnv(areaTypes,calledThis);
	}
	public static Exit getExit(String calledThis)
	{
		return (Exit)getEnv(exits,calledThis);
	}
	public static MOB getMOB(String calledThis)
	{
		for(int i=0;i<MOBs.size();i++)
		{
			MOB mob=(MOB)MOBs.elementAt(i);

			if(className(mob).equalsIgnoreCase(calledThis))
				return (MOB)mob.newInstance();
		}
		return null;
	}
	public static Ability getAbility(String calledThis)
	{
		Ability A=(Ability)abilities.get(calledThis);
		if(A==null)
			A=(Ability)getGlobal(abilities,calledThis);
		if(A!=null)
			A=(Ability)A.newInstance();
		return A;
	}
	public static Ability findAbility(String calledThis)
	{
		Ability A=(Ability)abilities.get(calledThis);
		if(A==null)
			A=(Ability)CoffeeUtensils.fetchEnvironmental(abilities,calledThis,true);
		if(A==null)
			A=(Ability)CoffeeUtensils.fetchEnvironmental(abilities,calledThis,false);
		if(A!=null)A=(Ability)A.newInstance();
		return A;
	}
	public static Ability findAbility(String calledThis, CharStats charStats)
	{
		Ability A=findAbility(calledThis);
		if(A==null) 
			return null;
		for(int c=0;c<charStats.numClasses();c++)
		{
			CharClass C=charStats.getMyClass(c);
			if(CMAble.getQualifyingLevel(C.ID(),A.ID())>=0)
				return (Ability)A.newInstance();
		}
		return null;
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

	public static Object getGlobal(Hashtable fromThese, String calledThis)
	{
		for(Enumeration e=fromThese.elements();e.hasMoreElements();)
		{
			Object o=e.nextElement();
			if(CoffeeUtensils.id(o).equalsIgnoreCase(calledThis))
				return o;
		}
		return null;
	}

	public static boolean loadClasses(INI page)
	{
		String prefix="com"+File.separatorChar+"planet_ink"+File.separatorChar+"coffee_mud"+File.separatorChar;

		races=loadVectorListToObj(prefix+"Races"+File.separatorChar,page.getStr("RACES"),"com.planet_ink.coffee_mud.interfaces.Race");
		Log.sysOut("MUD","Races loaded      : "+races.size());
		if(races.size()==0) return false;

		charClasses=loadVectorListToObj(prefix+"CharClasses"+File.separatorChar,page.getStr("CHARCLASSES"),"com.planet_ink.coffee_mud.interfaces.CharClass");
		Log.sysOut("MUD","Classes loaded    : "+charClasses.size());
		if(charClasses.size()==0) return false;

		MOBs=loadVectorListToObj(prefix+"MOBS"+File.separatorChar,page.getStr("MOBS"),"com.planet_ink.coffee_mud.interfaces.MOB");
		Log.sysOut("MUD","MOB Types loaded  : "+MOBs.size());
		if(MOBs.size()==0) return false;

		exits=loadVectorListToObj(prefix+"Exits"+File.separatorChar,page.getStr("EXITS"),"com.planet_ink.coffee_mud.interfaces.Exit");
		Log.sysOut("MUD","Exit Types loaded : "+exits.size());
		if(exits.size()==0) return false;

		areaTypes=loadVectorListToObj(prefix+"Areas"+File.separatorChar,page.getStr("AREAS"),"com.planet_ink.coffee_mud.interfaces.Area");
		Log.sysOut("MUD","Area Types loaded : "+areaTypes.size());
		if(areaTypes.size()==0) return false;

		locales=loadVectorListToObj(prefix+"Locales"+File.separatorChar,page.getStr("LOCALES"),"com.planet_ink.coffee_mud.interfaces.Room");
		Log.sysOut("MUD","Locales loaded    : "+locales.size());
		if(locales.size()==0) return false;

		abilities=loadHashListToObj(prefix+"Abilities"+File.separatorChar,page.getStr("ABILITIES"),"com.planet_ink.coffee_mud.interfaces.Ability");
		if(abilities.size()==0) return false;

		if((page.getStr("ABILITIES")!=null)
		&&(page.getStr("ABILITIES").toUpperCase().indexOf("%DEFAULT%")>=0))
		{
			Hashtable tempH;
			int size=0;
			tempH=loadHashListToObj(prefix+"Abilities"+File.separatorChar+"Fighter"+File.separatorChar,"%DEFAULT%","com.planet_ink.coffee_mud.interfaces.Ability");
			size=tempH.size();
			addH(tempH,abilities);
			
			tempH=loadHashListToObj(prefix+"Abilities"+File.separatorChar+"Ranger"+File.separatorChar,"%DEFAULT%","com.planet_ink.coffee_mud.interfaces.Ability");
			size+=tempH.size();
			addH(tempH,abilities);
			
			tempH=loadHashListToObj(prefix+"Abilities"+File.separatorChar+"Paladin"+File.separatorChar,"%DEFAULT%","com.planet_ink.coffee_mud.interfaces.Ability");
			size+=tempH.size();
			addH(tempH,abilities);
			
			size+=tempH.size();
			Log.sysOut("MUD","Fighter Skills    : "+size);
			addH(tempH,abilities);
		
			tempH=loadHashListToObj(prefix+"Abilities"+File.separatorChar+"Druid"+File.separatorChar,"%DEFAULT%","com.planet_ink.coffee_mud.interfaces.Ability");
			Log.sysOut("MUD","Chants loaded     : "+tempH.size());
			addH(tempH,abilities);
		
			tempH=loadHashListToObj(prefix+"Abilities"+File.separatorChar+"Languages"+File.separatorChar,"%DEFAULT%","com.planet_ink.coffee_mud.interfaces.Ability");
			Log.sysOut("MUD","Languages loaded  : "+tempH.size());
			addH(tempH,abilities);
		
			tempH=loadHashListToObj(prefix+"Abilities"+File.separatorChar+"Properties"+File.separatorChar,"%DEFAULT%","com.planet_ink.coffee_mud.interfaces.Ability");
			size=tempH.size();
			addH(tempH,abilities);
			
			tempH=loadHashListToObj(prefix+"Abilities"+File.separatorChar+"Diseases"+File.separatorChar,"%DEFAULT%","com.planet_ink.coffee_mud.interfaces.Ability");
			size=tempH.size();
			addH(tempH,abilities);
			
			tempH=loadHashListToObj(prefix+"Abilities"+File.separatorChar+"Misc"+File.separatorChar,"%DEFAULT%","com.planet_ink.coffee_mud.interfaces.Ability");
			size+=tempH.size();
			Log.sysOut("MUD","Properties loaded : "+size);
			addH(tempH,abilities);
		
			tempH=loadHashListToObj(prefix+"Abilities"+File.separatorChar+"Prayers"+File.separatorChar,"%DEFAULT%","com.planet_ink.coffee_mud.interfaces.Ability");
			Log.sysOut("MUD","Prayers loaded    : "+tempH.size());
			addH(tempH,abilities);
		
			tempH=loadHashListToObj(prefix+"Abilities"+File.separatorChar+"Archon"+File.separatorChar,"%DEFAULT%","com.planet_ink.coffee_mud.interfaces.Ability");
			size+=tempH.size();
			addH(tempH,abilities);
			
			tempH=loadHashListToObj(prefix+"Abilities"+File.separatorChar+"Skills"+File.separatorChar,"%DEFAULT%","com.planet_ink.coffee_mud.interfaces.Ability");
			size=tempH.size();
			addH(tempH,abilities);
			
			tempH=loadHashListToObj(prefix+"Abilities"+File.separatorChar+"Thief"+File.separatorChar,"%DEFAULT%","com.planet_ink.coffee_mud.interfaces.Ability");
			size+=tempH.size();
			addH(tempH,abilities);
			
			tempH=loadHashListToObj(prefix+"Abilities"+File.separatorChar+"Common"+File.separatorChar,"%DEFAULT%","com.planet_ink.coffee_mud.interfaces.Ability");
			size+=tempH.size();
			addH(tempH,abilities);
			
			tempH=loadHashListToObj(prefix+"Abilities"+File.separatorChar+"Specializations"+File.separatorChar,"%DEFAULT%","com.planet_ink.coffee_mud.interfaces.Ability");
			size+=tempH.size();
			addH(tempH,abilities);
			Log.sysOut("MUD","Skills loaded     : "+size);
		
			tempH=loadHashListToObj(prefix+"Abilities"+File.separatorChar+"Songs"+File.separatorChar,"%DEFAULT%","com.planet_ink.coffee_mud.interfaces.Ability");
			Log.sysOut("MUD","Songs loaded      : "+tempH.size());
			addH(tempH,abilities);
		
			tempH=loadHashListToObj(prefix+"Abilities"+File.separatorChar+"Spells"+File.separatorChar,"%DEFAULT%","com.planet_ink.coffee_mud.interfaces.Ability");
			Log.sysOut("MUD","Spells loaded     : "+tempH.size());
			addH(tempH,abilities);
		
			tempH=loadHashListToObj(prefix+"Abilities"+File.separatorChar+"Traps"+File.separatorChar,"%DEFAULT%","com.planet_ink.coffee_mud.interfaces.Ability");
			Log.sysOut("MUD","Traps loaded      : "+tempH.size());
			addH(tempH,abilities);
		}

		items=loadVectorListToObj(prefix+"Items"+File.separatorChar,page.getStr("ITEMS"),"com.planet_ink.coffee_mud.interfaces.Item");
		Log.sysOut("MUD","Items loaded      : "+items.size());

		weapons=loadVectorListToObj(prefix+"Items"+File.separatorChar+"Weapons"+File.separatorChar,page.getStr("WEAPONS"),"com.planet_ink.coffee_mud.interfaces.Weapon");
		Log.sysOut("MUD","Weapons loaded    : "+weapons.size());

		armor=loadVectorListToObj(prefix+"Items"+File.separatorChar+"Armor"+File.separatorChar,page.getStr("ARMOR"),"com.planet_ink.coffee_mud.interfaces.Armor");
		Log.sysOut("MUD","Armor loaded      : "+armor.size());

		miscMagic=loadVectorListToObj(prefix+"Items"+File.separatorChar+"MiscMagic"+File.separatorChar,page.getStr("MISCMAGIC"),"com.planet_ink.coffee_mud.interfaces.MiscMagic");
		Log.sysOut("MUD","Magic Items loaded: "+miscMagic.size());
		
		if((items.size()+weapons.size()+armor.size()+miscMagic.size())==0) 
			return false;
		
		behaviors=loadVectorListToObj(prefix+"Behaviors"+File.separatorChar,page.getStr("BEHAVIORS"),"com.planet_ink.coffee_mud.interfaces.Behavior");
		Log.sysOut("MUD","Behaviors loaded  : "+behaviors.size());
		if(behaviors.size()==0) return false;
		
		clantypes=loadVectorListToObj(prefix+"Clans"+File.separatorChar,page.getStr("CLANTYPES"),"com.planet_ink.coffee_mud.interfaces.Clan");
		
		Vector cmds=loadVectorListToObj(prefix+"Commands"+File.separatorChar+"extra"+File.separatorChar,page.getStr("COMMANDS"),"com.planet_ink.coffee_mud.interfaces.Command");
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
	public static int addH(Hashtable addMe, Hashtable toMe)
	{
		for(Enumeration e=addMe.keys();e.hasMoreElements();)
		{
			Object key=(String)e.nextElement();
			Object O=addMe.get(key);
			if(!toMe.containsKey(key))
				toMe.put(key,O);
		}
		return addMe.size();
	}

	public static void unload()
	{
		races=new Vector();
		charClasses=new Vector();
		MOBs=new Vector();
		abilities=new Hashtable();
		locales=new Vector();
		exits=new Vector();
		items=new Vector();
		behaviors=new Vector();
		weapons=new Vector();
		armor=new Vector();
		miscMagic=new Vector();
		clantypes=new Vector();
		areaTypes=new Vector();
	}

	public static Hashtable loadHashListToObj(String filePath, String auxPath, String ancester)
	{
		Hashtable h=new Hashtable();
		int x=auxPath.indexOf(";");
		while(x>=0)
		{
			String path=auxPath.substring(0,x).trim();
			auxPath=auxPath.substring(x+1).trim();
			loadObjectListToObj(h,filePath,path,ancester);
			x=auxPath.indexOf(";");
		}
		loadObjectListToObj(h,filePath,auxPath,ancester);
		return h;
	}
	
	public static void loadObjectListToObj(Object o, String filePath, String path, String ancester)
	{
		if(path.length()>0)
		{
			if(path.equalsIgnoreCase("%default%"))
				loadListToObj(o,filePath,false, ancester);
			else
				loadListToObj(o,path,true, ancester);
		}
	}
	
	public static Vector loadVectorListToObj(String filePath, String auxPath, String ancester)
	{
		Vector v=new Vector();
		int x=auxPath.indexOf(";");
		while(x>=0)
		{
			String path=auxPath.substring(0,x).trim();
			auxPath=auxPath.substring(x+1).trim();
			loadObjectListToObj(v,filePath,path,ancester);
			x=auxPath.indexOf(";");
		}
		loadObjectListToObj(v,filePath,auxPath,ancester);
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
