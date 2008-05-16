package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.WebMacros.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.lang.reflect.Modifier;
import org.mozilla.javascript.*;
import org.mozilla.javascript.optimizer.*;


/* 
   Copyright 2000-2008 Bo Zimmerman

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
public class CMClass extends ClassLoader
{
	protected static boolean debugging=false;
    protected static Hashtable classes=new Hashtable();
    public static CMClass instance(){ return new CMClass();}
    
    public static final int OBJECT_RACE=0;
    public static final int OBJECT_CHARCLASS=1;
    public static final int OBJECT_MOB=2;
    public static final int OBJECT_ABILITY=3;
    public static final int OBJECT_LOCALE=4;
    public static final int OBJECT_EXIT=5;
    public static final int OBJECT_ITEM=6;
    public static final int OBJECT_BEHAVIOR=7;
    public static final int OBJECT_CLAN=8;
    public static final int OBJECT_WEAPON=9;
    public static final int OBJECT_ARMOR=10;
    public static final int OBJECT_MISCMAGIC=11;
    public static final int OBJECT_AREA=12;
    public static final int OBJECT_COMMAND=13;
    public static final int OBJECT_CLANITEMS=14;
    public static final int OBJECT_MISCTECH=15;
    public static final int OBJECT_WEBMACROS=16;
    public static final int OBJECT_COMMON=17;
    public static final int OBJECT_LIBRARY=18;
    public static final int OBJECT_TOTAL=19;
    
    public static int longestWebMacro=-1;
    protected static TimeClock globalClock=null;
    protected static Hashtable common=new Hashtable();
    protected static Vector races=new Vector();
    protected static Vector charClasses=new Vector();
    protected static Vector MOBs=new Vector();
    protected static Vector abilities=new Vector();
    protected static Vector locales=new Vector();
    protected static Vector exits=new Vector();
    protected static Vector items=new Vector();
    protected static Vector behaviors=new Vector();
    protected static Vector weapons=new Vector();
    protected static Vector armor=new Vector();
    protected static Vector miscMagic=new Vector();
    protected static Vector miscTech=new Vector();
    protected static Vector clanItems=new Vector();
    protected static Vector areaTypes=new Vector();
    protected static Vector commands=new Vector();
    protected static Vector libraries=new Vector();
    protected static Hashtable webMacros=new Hashtable();
    protected static Hashtable CommandWords=new Hashtable();
    protected static final long[] OBJECT_CREATIONS=new long[OBJECT_TOTAL];
    protected static final long[] OBJECT_DESTRUCTIONS=new long[OBJECT_TOTAL];
    protected static final java.util.WeakHashMap[] OBJECT_CACHE=new java.util.WeakHashMap[OBJECT_TOTAL];
    protected static final java.util.WeakHashMap MSGS_IN_USE=new java.util.WeakHashMap();
    protected static final Vector MSGS_CACHE=new Vector();
    protected static final boolean KEEP_OBJECT_CACHE=false;
    static{ if(KEEP_OBJECT_CACHE) for(int i=0;i<OBJECT_TOTAL;i++)OBJECT_CACHE[i]=new java.util.WeakHashMap();}
    public static final String[] OBJECT_DESCS={
		"RACE","CHARCLASS","MOB","ABILITY","LOCALE","EXIT","ITEM","BEHAVIOR",
		"CLAN","WEAPON","ARMOR","MISCMAGIC","AREA","COMMAND","CLANITEMS",
		"MISCTECH","WEBMACROS","COMMON","LIBRARY"
	};
    protected static final String[] OBJECT_ANCESTORS={
		"com.planet_ink.coffee_mud.Races.interfaces.Race",
		"com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass",
		"com.planet_ink.coffee_mud.MOBS.interfaces.MOB",
		"com.planet_ink.coffee_mud.Abilities.interfaces.Ability",
		"com.planet_ink.coffee_mud.Locales.interfaces.Room",
		"com.planet_ink.coffee_mud.Exits.interfaces.Exit",
		"com.planet_ink.coffee_mud.Items.interfaces.Item",
		"com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior",
		"com.planet_ink.coffee_mud.core.interfaces.Clan",
		"com.planet_ink.coffee_mud.Items.interfaces.Weapon",
		"com.planet_ink.coffee_mud.Items.interfaces.Armor",
		"com.planet_ink.coffee_mud.Items.interfaces.MiscMagic",
		"com.planet_ink.coffee_mud.Areas.interfaces.Area",
		"com.planet_ink.coffee_mud.Commands.interfaces.Command",
		"com.planet_ink.coffee_mud.Items.interfaces.ClanItem",
		"com.planet_ink.coffee_mud.Items.interfaces.Electronics",
		"com.planet_ink.coffee_mud.WebMacros.interfaces.WebMacro",
        "com.planet_ink.coffee_mud.Common.interfaces.CMCommon",
        "com.planet_ink.coffee_mud.Libraries.interfaces.CMLibrary",
		};
    public static void bumpCounter(Object O, int which)
    {
        if(KEEP_OBJECT_CACHE)
        {
            if(OBJECT_CACHE[which].containsKey(O))
            {
                Log.errOut("Duplicate!",new Exception("Duplicate Found!"));
                return;
            }
            OBJECT_CACHE[which].put(O,OBJECT_CACHE);
        }
        OBJECT_CREATIONS[which]++;
    }
    public static void unbumpCounter(Object O, int which)
    {
        if(KEEP_OBJECT_CACHE)
        {
            if(OBJECT_CACHE[which].containsKey(O)) // yes, if its in there, its bad
            {
                OBJECT_CACHE[which].remove(O);  
                Log.errOut("bumped!",O.getClass().getName());
                return;
            }
        }
        OBJECT_DESTRUCTIONS[which]++;
    }
	public static Enumeration races(){return races.elements();}
    public static Enumeration commonObjects(){return common.elements();}
	public static Race randomRace(){return (Race)races.elementAt((int)Math.round(Math.floor(Math.random()*new Integer(races.size()).doubleValue())));}
	public static Enumeration charClasses(){return charClasses.elements();}
	public static CharClass randomCharClass(){return (CharClass)charClasses.elementAt((int)Math.round(Math.floor(Math.random()*new Integer(charClasses.size()).doubleValue())));}
	public static Enumeration mobTypes(){return MOBs.elements();}
    public static Enumeration libraries(){return libraries.elements();}
	public static Enumeration locales(){return locales.elements();}
	public static Enumeration exits(){return exits.elements();}
	public static Enumeration behaviors(){return behaviors.elements();}
	public static Enumeration basicItems(){return items.elements();}
	public static Enumeration weapons(){return weapons.elements();}
	public static Enumeration armor(){return armor.elements();}
	public static Enumeration miscMagic(){return miscMagic.elements();}
	public static Enumeration miscTech(){return miscTech.elements();}
	public static Enumeration clanItems(){return clanItems.elements();}
	public static Enumeration areaTypes(){return areaTypes.elements();}
	public static Enumeration commands(){return commands.elements();}
	public static Enumeration abilities(){return abilities.elements();}
	public static Enumeration webmacros(){return new Vector(webMacros.entrySet()).elements();}
	public static Ability randomAbility(){ return (Ability)abilities.elementAt((int)Math.round(Math.floor(Math.random()*new Integer(abilities.size()).doubleValue())));}
    public static Room getLocale(String calledThis){ return (Room)getNewGlobal(locales,calledThis); }
    public static CMObject getLibrary(String calledThis) { return (CMObject)getGlobal(libraries,calledThis); }
    public static Area anyOldArea(){return (Area)areaTypes.elementAt(0);}
    public static Area getAreaType(String calledThis) { return (Area)getNewGlobal(areaTypes,calledThis); }
    public static Exit getExit(String calledThis) { return (Exit)getNewGlobal(exits,calledThis);}
    public static MOB getMOB(String calledThis) { return (MOB)getNewGlobal(MOBs,calledThis); }
    public static Weapon getWeapon(String calledThis) { return (Weapon)getNewGlobal(weapons,calledThis); }
    public static Item getMiscMagic(String calledThis) { return (Item)getNewGlobal(miscMagic,calledThis); }
    public static Item getMiscTech(String calledThis) { return (Item)getNewGlobal(miscTech,calledThis);}    
    public static Armor getArmor(String calledThis) { return (Armor)getNewGlobal(armor,calledThis); }
    public static Item getBasicItem(String calledThis) { return (Item)getNewGlobal(items,calledThis); }
    public static Behavior getBehavior(String calledThis) { return (Behavior)getNewGlobal(behaviors,calledThis); }
    public static Ability getAbility(String calledThis) { return (Ability)getNewGlobal(abilities,calledThis); }
    public static CharClass getCharClass(String calledThis){ return (CharClass)getGlobal(charClasses,calledThis);}
    public static CMObject getCommon(String calledThis){return getNewGlobal(common,calledThis);}
    public static Command getCommand(String word){return (Command)getGlobal(commands,word);}
    public static WebMacro getWebMacro(String macroName){return (WebMacro)webMacros.get(macroName);}
    public static Race getRace(String calledThis){return (Race)getGlobal(races,calledThis);}


    public static String getCounterReport()
    {
        StringBuffer str=new StringBuffer("");
        for(int i=0;i<OBJECT_TOTAL;i++)
            if(OBJECT_CREATIONS[i]>0)
                str.append(CMStrings.padRight(OBJECT_DESCS[i],12)+": Created: "+OBJECT_CREATIONS[i]+", Destroyed: "+OBJECT_DESTRUCTIONS[i]+", Remaining: "+(OBJECT_CREATIONS[i]-OBJECT_DESTRUCTIONS[i])+"\n");
        return str.toString();
    }
    
	public static void addAllItemClassNames(Vector V, boolean NonArchon, boolean NonGeneric)
	{
		for(Enumeration i=basicItems();i.hasMoreElements();)
		{
			Item I=(Item)i.nextElement();
			if(((!NonArchon)||(!(I instanceof ArchonOnly)))
			&&((!NonGeneric)||(!I.isGeneric())))
				V.addElement(CMClass.classID(I));
		}
		for(Enumeration i=weapons();i.hasMoreElements();)
		{
			Item I=(Item)i.nextElement();
			if(((!NonArchon)||(!(I instanceof ArchonOnly)))
			&&((!NonGeneric)||(!I.isGeneric())))
				V.addElement(CMClass.classID(I));
		}
		for(Enumeration i=armor();i.hasMoreElements();)
		{
			Item I=(Item)i.nextElement();
			if(((!NonArchon)||(!(I instanceof ArchonOnly)))
			&&((!NonGeneric)||(!I.isGeneric())))
				V.addElement(CMClass.classID(I));
		}
		for(Enumeration i=miscMagic();i.hasMoreElements();)
		{
			Item I=(Item)i.nextElement();
			if(((!NonArchon)||(!(I instanceof ArchonOnly)))
			&&((!NonGeneric)||(!I.isGeneric())))
				V.addElement(CMClass.classID(I));
		}
		for(Enumeration i=miscTech();i.hasMoreElements();)
		{
			Item I=(Item)i.nextElement();
			if(((!NonArchon)||(!(I instanceof ArchonOnly)))
			&&((!NonGeneric)||(!I.isGeneric())))
				V.addElement(CMClass.classID(I));
		}
		for(Enumeration i=clanItems();i.hasMoreElements();)
		{
			Item I=(Item)i.nextElement();
			if(((!NonArchon)||(!(I instanceof ArchonOnly)))
			&&((!NonGeneric)||(!I.isGeneric())))
				V.addElement(CMClass.classID(I));
		}
	}
	
	public static Item getItem(String calledThis)
	{
		Item thisItem=(Item)getNewGlobal(items,calledThis);
		if(thisItem==null) thisItem=(Item)getNewGlobal(armor,calledThis);
		if(thisItem==null) thisItem=(Item)getNewGlobal(weapons,calledThis);
		if(thisItem==null) thisItem=(Item)getNewGlobal(miscMagic,calledThis);
		if(thisItem==null) thisItem=(Item)getNewGlobal(clanItems,calledThis);
		if(thisItem==null) thisItem=(Item)getNewGlobal(miscTech,calledThis);
		return thisItem;
	}

    protected static Item sampleItem=null;
	public static Item sampleItem(){
		if((sampleItem==null)&&(items.size()>0))
			sampleItem= (Item)((Item)items.firstElement()).copyOf();
		return sampleItem;
	}

    protected static MOB sampleMOB=null;
	public static MOB sampleMOB()
	{
		if((sampleMOB==null)&&(MOBs.size()>0))
		{
			sampleMOB=(MOB)((MOB)MOBs.firstElement()).copyOf();
			sampleMOB.baseEnvStats().setDisposition(EnvStats.IS_NOT_SEEN);
			sampleMOB.envStats().setDisposition(EnvStats.IS_NOT_SEEN);
		}
		if(sampleMOB.location()==null)
			sampleMOB.setLocation(CMLib.map().getRandomRoom());
		return sampleMOB;
	}

	public static Command findCommandByTrigger(String word,
											   boolean exactOnly)
	{
		Command C=(Command)CommandWords.get(word.trim().toUpperCase());
		if((exactOnly)||(C!=null)) return C;
		word=word.toUpperCase();
		for(Enumeration e=CommandWords.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			if(key.toUpperCase().startsWith(word))
				return (Command)CommandWords.get(key);
		}
		return null;
	}

    public static int totalClasses(){
        return races.size()+charClasses.size()+MOBs.size()+abilities.size()+locales.size()+exits.size()
              +items.size()+behaviors.size()+weapons.size()+armor.size()+miscMagic.size()+clanItems.size()
              +miscTech.size()+areaTypes.size()+common.size()+libraries.size()+commands.size()
              +webMacros.size();
    }
    
	public static boolean delClass(String type, CMObject O)
	{
        if(classes.containsKey(O.getClass().getName()))
            classes.remove(O.getClass().getName());
		Object set=getClassSet(type);
		if(set==null) return false;
		if(set instanceof Vector)
		{
			((Vector)set).remove(O);
            Vector newSet=new Vector(new TreeSet((Vector)set));
            ((Vector)set).clear();
            ((Vector)set).addAll(newSet);
            if(set==commands) reloadCommandWords();
            //if(set==libraries) CMLib.registerLibraries(libraries.elements());
		}
		else
		if(set instanceof Hashtable)
			((Hashtable)set).remove(O.ID().trim());
		else
		if(set instanceof HashSet)
			((HashSet)set).remove(O);
		else
			return false;
		return true;
	}
	
	protected static Object getClassSet(String type) { return getClassSet(classCode(type));}
	protected static Object getClassSet(int code)
	{
		switch(code)
		{
		case 0: return races;
		case 1: return charClasses; 
		case 2: return MOBs; 
		case 3: return abilities; 
		case 4: return locales; 
		case 5: return exits; 
		case 6: return items; 
		case 7: return behaviors; 
		case 8: return null;
		case 9: return weapons; 
		case 10: return armor; 
		case 11: return miscMagic; 
		case 12: return areaTypes; 
		case 13: return commands; 
		case 14: return clanItems; 
		case 15: return miscTech; 
		case 16: return webMacros; 
        case 17: return common; 
        case 18: return libraries; 
		}
		return null;
	}

	public static boolean addClass(String type, CMObject O)
	{
		Object set=getClassSet(type);
		if(set==null) return false;
		if(set instanceof Vector)
		{
			((Vector)set).addElement(O);
            Vector newSet=new Vector(new TreeSet((Vector)set));
            ((Vector)set).clear();
            ((Vector)set).addAll(newSet);
            if(set==commands) reloadCommandWords();
            if(set==libraries) CMLib.registerLibraries(libraries.elements());
}
		else
		if(set instanceof Hashtable)
			((Hashtable)set).put(O.ID().trim().toUpperCase(), O);
		else
		if(set instanceof HashSet)
			((HashSet)set).add(O);
		else
			return false;
		return true;
	}

	public static int classCode(String name)
	{
		for(int i=0;i<OBJECT_DESCS.length;i++)
		{
			if(OBJECT_DESCS[i].toUpperCase().startsWith(name.toUpperCase()))
				return i;
		}
		return -1;
	}
	
	public static int classCode(Object O)
	{
		for(int i=CMClass.OBJECT_ANCESTORS.length-1;i>=0;i--)
		{
			try{
				Class ancestorCl = instance().loadClass(CMClass.OBJECT_ANCESTORS[i]);
				if(CMClass.checkAncestry(O.getClass(),ancestorCl))
					return i;
			}catch(Exception e){}
		}
		return -1;
	}
	
	public static boolean loadClass(String classType, String path, boolean quiet)
	{
        debugging=CMSecurity.isDebugging("CLASSLOADER");
		Object set=getClassSet(classType);
		if(set==null) return false;

		if(!loadListToObj(set,path,OBJECT_ANCESTORS[classCode(classType)],quiet))
            return false;

        if(set instanceof Vector)
        {
            Vector newSet=new Vector(new TreeSet((Vector)set));
            ((Vector)set).clear();
            ((Vector)set).addAll(newSet);
            if(set==commands) reloadCommandWords();
            if(set==libraries) CMLib.registerLibraries(libraries.elements());
        }
		return true;
	}

	public static Object unsortedLoadClass(String classType, String path, boolean quiet)
	{
		if((path==null)||(path.length()==0))
			return null;
		try{
			String pathLess=path;
			if(pathLess.toUpperCase().endsWith(".CLASS"))
				pathLess=pathLess.substring(0,pathLess.length()-6);
			else
			if(pathLess.toUpperCase().endsWith(".JS"))
				pathLess=pathLess.substring(0,pathLess.length()-3);
			pathLess=pathLess.replace('/','.');
			pathLess=pathLess.replace('\\','.');
			if(classes.contains(pathLess)) 
				return ((Class)classes.get(pathLess)).newInstance();
		}catch(Exception e){}
		Vector V=new Vector();
		if(classCode(classType)<0)
			return null;
		if((!path.toUpperCase().endsWith(".CLASS"))
		&&(!path.toUpperCase().endsWith(".JS")))
		{
			path=path.replace('.','/');
			path+=".class";
		}
		if(!loadListToObj(V,path,OBJECT_ANCESTORS[classCode(classType)],quiet))
			return null;
		if(V.size()==0) return null;
		return (Object)V.firstElement();
	}

	public static boolean checkForCMClass(String classType, String path)
	{ 
		return unsortedLoadClass(classType,path,true)!=null;
	}

    public static String ancestor(String code)
    {
        int num=classCode(code);
        if((num>=0)&&(num<OBJECT_ANCESTORS.length))
            return OBJECT_ANCESTORS[num];
        return "";
    }
	public static Object getClass(String calledThis)
	{
		String shortThis=calledThis;
		int x=shortThis.lastIndexOf('.');
		if(x>0) shortThis=shortThis.substring(x+1);
		Object set=null;
		Object thisItem=null;
		for(int i=0;i<CMClass.OBJECT_DESCS.length;i++)
		{
			set=getClassSet(i);
			if(set==null) continue;
			if(set instanceof Vector)
				thisItem=getGlobal((Vector)set,shortThis);
			else
			if(set instanceof Hashtable)
				thisItem=getGlobal((Hashtable)set,shortThis);
			if(thisItem!=null) return thisItem;
		}
        try{	return ((Class)classes.get(calledThis)).newInstance();}catch(Exception e){}
		return thisItem;
	}

	public static Environmental getUnknown(String calledThis)
	{
		Environmental thisItem=(Environmental)getNewGlobal(items,calledThis);
		if(thisItem==null) thisItem=(Environmental)getNewGlobal(armor,calledThis);
		if(thisItem==null) thisItem=(Environmental)getNewGlobal(weapons,calledThis);
		if(thisItem==null) thisItem=(Environmental)getNewGlobal(miscMagic,calledThis);
		if(thisItem==null) thisItem=(Environmental)getNewGlobal(miscTech,calledThis);
		if(thisItem==null) thisItem=(Environmental)getNewGlobal(MOBs,calledThis);
		if(thisItem==null) thisItem=(Environmental)getNewGlobal(abilities,calledThis);
		if(thisItem==null) thisItem=(Environmental)getNewGlobal(clanItems,calledThis);
		if((thisItem==null)&&(charClasses.size()>0)&&(calledThis.length()>0))
			Log.sysOut("CMClass","Unknown Unknown '"+calledThis+"'.");
		return thisItem;
	}

    public static Race findRace(String calledThis)
    {
        Race thisItem=getRace(calledThis);
        if(thisItem!=null) return thisItem;
        for(int i=0;i<races.size();i++)
        {
            Race R=(Race)races.elementAt(i);
            if(R.name().equalsIgnoreCase(calledThis))
                return R;
        }
        return null;
    }
    public static CharClass findCharClass(String calledThis)
    {
        CharClass thisItem=getCharClass(calledThis);
        if(thisItem!=null) return thisItem;
        for(int i=0;i<charClasses.size();i++)
        {
            CharClass C=(CharClass)charClasses.elementAt(i);
            for(int n=0;n<C.nameSet().length;n++)
            if(C.nameSet()[n].equalsIgnoreCase(calledThis))
                return C;
        }
        return null;
    }
    public static CMObject getNewGlobal(Vector list, String ID)
    {
        CMObject O=(CMObject)getGlobal(list,ID);
        if(O!=null) return O.newInstance();
        return null;
    }
	public static Object getGlobal(Vector list, String ID)
	{
		if(list.size()==0) return null;
		int start=0;
		int end=list.size()-1;
		while(start<=end)
		{
			int mid=(end+start)/2;
			int comp=classID(list.elementAt(mid)).compareToIgnoreCase(ID);
			if(comp==0)
				return list.elementAt(mid);
			else
			if(comp>0)
				end=mid-1;
			else
				start=mid+1;

		}
		return null;
	}
	public static Ability findAbility(String calledThis)
	{
		Ability A=(Ability)getGlobal(abilities,calledThis);
		if(A==null) A=(Ability)CMLib.english().fetchEnvironmental(abilities,calledThis,true);
		if(A==null) A=(Ability)CMLib.english().fetchEnvironmental(abilities,calledThis,false);
		if(A!=null)A=(Ability)A.newInstance();
		return A;
	}
	public static Ability findAbility(String calledThis, CharStats charStats)
	{
		Ability A=null;
		Vector As=new Vector();
		for(Enumeration e=abilities();e.hasMoreElements();)
		{
			A=(Ability)e.nextElement();
			for(int c=0;c<charStats.numClasses();c++)
			{
				CharClass C=charStats.getMyClass(c);
				if(CMLib.ableMapper().getQualifyingLevel(C.ID(),true,A.ID())>=0)
				{	As.addElement(A); break;}
			}
		}
		A=(Ability)CMLib.english().fetchEnvironmental(As,calledThis,true);
		if(A==null) A=(Ability)CMLib.english().fetchEnvironmental(As,calledThis,false);
        if(A==null) A=(Ability)getGlobal(abilities,calledThis);
		if(A!=null)A=(Ability)A.newInstance();
		return A;
	}

	public static Ability findAbility(String calledThis, MOB mob)
	{
		Vector As=new Vector();
        Ability A=null;
		for(int a=0;a<mob.numAbilities();a++)
		{
		    Ability B=mob.fetchAbility(a);
		    if(B!=null) As.addElement(B);
		}
		A=(Ability)CMLib.english().fetchEnvironmental(As,calledThis,true);
		if(A==null)
			A=(Ability)CMLib.english().fetchEnvironmental(As,calledThis,false);
        if(A==null)
            A=(Ability)getGlobal(abilities,calledThis);
		if(A!=null)A=(Ability)A.newInstance();
		return A;
	}
	
    public static CMObject getNewGlobal(Hashtable list, String ID)
    {
        CMObject O=(CMObject)getGlobal(list,ID);
        if(O!=null) return O.newInstance();
        return null;
    }
    
	public static Object getGlobal(Hashtable fromThese, String calledThis)
	{
		Object o=fromThese.get(calledThis);
		if(o==null)
		{
			for(Enumeration e=fromThese.elements();e.hasMoreElements();)
			{
				o=e.nextElement();
				if(classID(o).equalsIgnoreCase(calledThis))
					return o;
			}
			return null;
		}
		return o;
	}

	public static void addRace(Race GR)
	{
		for(int i=0;i<races.size();i++)
		{
			Race R=(Race)races.elementAt(i);
			if(R.ID().compareToIgnoreCase(GR.ID())>=0)
			{
                if(R.ID().compareToIgnoreCase(GR.ID())==0)
    				races.setElementAt(GR,i);
                else
                    races.insertElementAt(GR,i);
				return;
			}
		}
		races.addElement(GR);
	}
	public static void addCharClass(CharClass CR)
	{
		for(int i=0;i<charClasses.size();i++)
		{
			CharClass C=(CharClass)charClasses.elementAt(i);
			if(C.ID().compareToIgnoreCase(CR.ID())>=0)
			{
                if(C.ID().compareToIgnoreCase(CR.ID())==0)
                    charClasses.setElementAt(CR,i);
                else
    				charClasses.insertElementAt(CR,i);
				return;
			}
		}
		charClasses.addElement(CR);
	}
	public static void delCharClass(CharClass C)
	{
        charClasses.removeElement(C);
	}
	public static void delRace(Race R)
	{
		races.removeElement(R);
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

    public static boolean returnMsg(CMMsg msg)
    {
        synchronized(CMClass.MSGS_CACHE)
        {
            if(MSGS_CACHE.size()<10000)
            {
                MSGS_CACHE.addElement(msg);
                return true;
            }
        }
        return false;
    }
    
    public static Vector sortEnvironmentalsByID(Vector V) {
        Hashtable hashed=new Hashtable();
        for(Enumeration e=V.elements();e.hasMoreElements();)
        {
            Environmental E=(Environmental)e.nextElement();
            hashed.put(E.ID().toUpperCase(),E);
        }
        Vector idSet=new Vector(new TreeSet(hashed.keySet()));
        Vector V2=new Vector(V.size());
        for(Enumeration e=idSet.elements();e.hasMoreElements();)
            V2.addElement(hashed.get((String)e.nextElement()));
        return V2;
    }

    public static Vector sortEnvironmentalsByName(Vector V) {
        Hashtable nameHash=new Hashtable();
        Vector V3;
        for(Enumeration e=V.elements();e.hasMoreElements();)
        {
            Environmental E=(Environmental)e.nextElement();
            if(nameHash.containsKey(E.Name().toUpperCase()))
            {
                V3=CMParms.makeVector((Object[])nameHash.get(E.Name().toUpperCase()));
                V3.addElement(E);
                nameHash.put(E.Name().toUpperCase(),V3.toArray());
            }
            else
                nameHash.put(E.Name().toUpperCase(),new Object[]{E});
        }
        Vector idSet=new Vector(new TreeSet(nameHash.keySet()));
        Vector V2=new Vector(V.size());
        for(Enumeration e=idSet.elements();e.hasMoreElements();)
            V2.addAll(CMParms.makeVector((Object[])nameHash.get((String)e.nextElement())));
        return V2;
    }

    public static CMMsg MsgFactory()
    {
        CMMsg msg=null;
        synchronized(CMClass.MSGS_CACHE)
        {
            if(MSGS_CACHE.size()==0)
            {
                if(MSGS_CACHE.size()==0)
                {
                    msg=(CMMsg)getCommon("DefaultMessage");
                    MSGS_CACHE.addElement(msg);
                }
            }
            msg=(CMMsg)MSGS_CACHE.firstElement();
            MSGS_CACHE.removeElementAt(0);
        }
        return msg;
    }
    
    public static CMMsg getMsg(MOB source, int newAllCode, String allMessage)
    { CMMsg M=MsgFactory(); M.modify(source,newAllCode,allMessage); return M;}
    public static CMMsg getMsg(MOB source, int newAllCode, String allMessage, int newValue)
    { CMMsg M=MsgFactory(); M.modify(source,newAllCode,allMessage,newValue); return M;}
    public static CMMsg getMsg(MOB source, Environmental target, int newAllCode, String allMessage)
    { CMMsg M=MsgFactory(); M.modify(source,target,newAllCode,allMessage); return M;}
    public static CMMsg getMsg(MOB source, Environmental target, Environmental tool, int newAllCode, String allMessage)
    { CMMsg M=MsgFactory(); M.modify(source,target,tool,newAllCode,allMessage); return M;}
    public static CMMsg getMsg(MOB source, Environmental target, Environmental tool, int newSourceCode, int newTargetCode, int newOthersCode, String Message)
    { CMMsg M=MsgFactory(); M.modify(source,target,tool,newSourceCode,newTargetCode,newOthersCode,Message); return M;}
    public static CMMsg getMsg(MOB source, Environmental target, Environmental tool, int newSourceCode, String sourceMessage, String targetMessage, String othersMessage)
    { CMMsg M=MsgFactory(); M.modify(source,target,tool,newSourceCode,sourceMessage,newSourceCode,targetMessage,newSourceCode,othersMessage); return M;}
    public static CMMsg getMsg(MOB source, Environmental target, Environmental tool, int newSourceCode, String sourceMessage, int newTargetCode, String targetMessage, int newOthersCode, String othersMessage)
    { CMMsg M=MsgFactory(); M.modify(source,target,tool,newSourceCode,sourceMessage,newTargetCode,targetMessage,newOthersCode,othersMessage); return M;}
    

	public static void unload()
	{
        common=new Hashtable();
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
		miscTech=new Vector();
		areaTypes=new Vector();
		clanItems=new Vector();
		commands=new Vector();
		webMacros=new Hashtable();
		CommandWords=new Hashtable();
	}
    private static void initializeClassGroup(Vector V){ for(int v=0;v<V.size();v++) ((CMObject)V.elementAt(v)).initializeClass();}
    private static void initializeClassGroup(Hashtable H){ 
        for(Enumeration e=H.elements();e.hasMoreElements();) ((CMObject)e.nextElement()).initializeClass();}
    public static void intializeClasses()
    {
        initializeClassGroup(common);
        initializeClassGroup(races);
        initializeClassGroup(charClasses);
        initializeClassGroup(MOBs);
        initializeClassGroup(abilities);
        initializeClassGroup(locales);
        initializeClassGroup(exits);
        initializeClassGroup(items);
        initializeClassGroup(behaviors);
        initializeClassGroup(weapons);
        initializeClassGroup(armor);
        initializeClassGroup(miscMagic);
        initializeClassGroup(miscTech);
        initializeClassGroup(areaTypes);
        initializeClassGroup(clanItems);
        initializeClassGroup(commands);
        initializeClassGroup(webMacros);
        initializeClassGroup(CommandWords);
        initializeClassGroup(libraries);
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

	public static boolean loadObjectListToObj(Object o, String filePath, String path, String ancester)
	{
		if(path.length()>0)
		{
            boolean success=false;
			if(path.equalsIgnoreCase("%default%"))
				success=loadListToObj(o,filePath, ancester, false);
			else
				success=loadListToObj(o,path,ancester, false);
            return success;
		}
        return false;
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
		return new Vector(new TreeSet(v));
	}

	public static boolean loadListToObj(Object toThis, String filePath, String ancestor, boolean quiet)
	{ 
        Class ancestorCl=null;
        CMClass loader=new CMClass();
        if (ancestor != null && ancestor.length() != 0)
        {
            try
            {
                ancestorCl = loader.loadClass(ancestor);
            }
            catch (ClassNotFoundException e)
            {
                if(!quiet)
                    Log.sysOut("CMClass","WARNING: Couldn't load ancestor class: "+ancestor);
            }
        }

        CMFile file=new CMFile(filePath,null,true);
        Vector fileList=new Vector();
        if(file.canRead())
        {
            if(file.isDirectory())
            {
                CMFile[] list=file.listFiles();
                for(int l=0;l<list.length;l++)
                    if((list[l].getName().indexOf("$")<0)&&(list[l].getName().toUpperCase().endsWith(".CLASS")))
                        fileList.addElement(list[l].getVFSPathAndName());
                for(int l=0;l<list.length;l++)
                    if(list[l].getName().toUpperCase().endsWith(".JS"))
                        fileList.addElement(list[l].getVFSPathAndName());
            }
            else
            {
                fileList.addElement(file.getVFSPathAndName());
            }
        }
        else
        {
            if(!quiet)
                Log.errOut("CMClass","Unable to access path "+file.getVFSPathAndName());
            return false;
        }
        for(int l=0;l<fileList.size();l++)
        {
            String item=(String)fileList.elementAt(l);
            if(item.startsWith("/")) item=item.substring(1);
            try
            {
                Object O=null;
                String packageName=item.replace('/','.');
                if(packageName.toUpperCase().endsWith(".CLASS"))
                    packageName=packageName.substring(0,packageName.length()-6);
                Class C=loader.loadClass(packageName,true);
                if(C!=null)
                {
                    if(!checkAncestry(C,ancestorCl))
                    {
                        if(!quiet)
                            Log.sysOut("CMClass","WARNING: class failed ancestral check: "+packageName);
                    }
                    O=C.newInstance();
                }
                if(O==null)
                {
                    if(!quiet)
                        Log.sysOut("CMClass","Unable to create class '"+packageName+"'");
                }
                else
                {
                    String itemName=O.getClass().getName();
                    int x=itemName.lastIndexOf(".");
                    if(x>=0) itemName=itemName.substring(x+1);
                    if(toThis instanceof Hashtable)
                    {
                        Hashtable H=(Hashtable)toThis;
                        if(H.containsKey(itemName.trim().toUpperCase()))
                            H.remove(itemName.trim().toUpperCase());
                        H.put(itemName.trim().toUpperCase(),O);
                    }
                    else
                    if(toThis instanceof Vector)
                    {
                        Vector V=(Vector)toThis;
                        boolean doNotAdd=false;
                        for(int v=0;v<V.size();v++)
                            if(rawClassName(V.elementAt(v)).equals(itemName))
                            {
                                V.setElementAt(O,v);
                                doNotAdd=true;
                                break;
                            }
                        if(!doNotAdd)
                            V.addElement(O);
                    }
                }
            }
            catch(Throwable e)
            {
                if(!quiet)
                    Log.errOut("CMClass",e);
                return false;
            }
        }
        return true;
    }

	public static String rawClassName(Object O)
	{
		if(O==null) return "";
		return rawClassName(O.getClass());
	}

	public static String rawClassName(Class C)
	{
		if(C==null) return "";
		String name=C.getName();
		int lastDot=name.lastIndexOf(".");
		if(lastDot>=0)
			return name.substring(lastDot+1);
		return name;
	}

    protected static boolean checkAncestry(Class cl, Class ancestorCl)
	{
		if (cl == null) return false;
		if (cl.isPrimitive() || cl.isInterface()) return false;
		if ( Modifier.isAbstract( cl.getModifiers()) || !Modifier.isPublic( cl.getModifiers()) ) return false;
		if (ancestorCl == null) return true;
		return (ancestorCl.isAssignableFrom(cl)) ;
	}

    public static String classPtrStr(Object e)
    {
        String ptr=""+e;
        int x=ptr.lastIndexOf("@");
        if(x>0)return ptr.substring(x+1);
        return ptr;
    }
    
	public static String classID(Object e)
	{
		if(e!=null)
		{
			if(e instanceof CMObject)
				return ((CMObject)e).ID();
			else
			if(e instanceof Command)
				return rawClassName(e);
			else
				return rawClassName(e);
		}
		return "";
	}
    
    public static final TimeClock globalClock()
    {
        if(globalClock==null) 
        {
            globalClock=(TimeClock)getCommon("DefaultTimeClock");
            if(globalClock!=null) globalClock.setLoadName("GLOBAL");
        }
        return globalClock;
    }
    
    /**
     * This is a simple version for external clients since they
     * will always want the class resolved before it is returned
     * to them.
     */
    public Class loadClass(String className) throws ClassNotFoundException {
        return (loadClass(className, true));
    }

    public Class finishDefineClass(String className, byte[] classData, String overPackage, boolean resolveIt)
        throws ClassFormatError 
    {
        Class result=null;
        if(overPackage!=null)
        {
            int x=className.lastIndexOf(".");
            if(x>=0)
                className=overPackage+className.substring(x);
            else
                className=overPackage+"."+className;
        }
        try{result=defineClass(className, classData, 0, classData.length);}
        catch(NoClassDefFoundError e)
        {
            if(e.getMessage().toLowerCase().indexOf("(wrong name:")>=0)
            {
                int x=className.lastIndexOf(".");
                if(x>=0)
                {
                    String notherName=className.substring(x+1);
                    result=defineClass(notherName, classData, 0, classData.length);
                }
                else
                    throw e;
            }
            else
                throw e;
        }
        if (result==null){throw new ClassFormatError();}
        if (resolveIt){resolveClass(result);}
        if(debugging) Log.debugOut("CMClass","Loaded: "+result.getName());
        classes.put(className, result);
        return result;
    }

    /**
     * This is the required version of loadClass which is called
     * both from loadClass above and from the internal function
     * FindClassFromClass.
     */
    public synchronized Class loadClass(String className, boolean resolveIt)
        throws ClassNotFoundException 
    {
        String pathName=null;
        if(className.endsWith(".class")) className=className.substring(0,className.length()-6);
        if(className.toUpperCase().endsWith(".JS"))
        {
            pathName=className.substring(0,className.length()-3).replace('.','/')+className.substring(className.length()-3);
            className=className.substring(0,className.length()-3);
        }
        else
            pathName=className.replace('.','/')+".class";
        Class result = (Class)classes.get(className);
        if (result!=null)
        {
            if(debugging) Log.debugOut("CMClass","Loaded: "+result.getName());
        	return result;
        }
        if((super.findLoadedClass(className)!=null)
        ||(className.indexOf("com.planet_ink.coffee_mud.")<0)
        ||(className.startsWith("com.planet_ink.coffee_mud.core."))
        ||(className.startsWith("com.planet_ink.coffee_mud.application."))
        ||(className.indexOf(".interfaces.")>=0))
        {
	        try{
	        	result=super.findSystemClass(className); 
	        	if(result!=null)
	        	{
	                if(debugging) Log.debugOut("CMClass","Loaded: "+result.getName());
	        		return result;
	        	}
	        } catch(Throwable t){}
        }
        /* Try to load it from our repository */
        CMFile CF=new CMFile(pathName,null,false);
        byte[] classData=CF.raw();
        if((classData==null)||(classData.length==0))
        {
            throw new ClassNotFoundException("File "+pathName+" not readable!");
        }
        if(CF.getName().toUpperCase().endsWith(".JS"))
        {
            String name=CF.getName().substring(0,CF.getName().length()-3);
            StringBuffer str=CF.textVersion(classData);
            if((str==null)||(str.length()==0))
                new ClassNotFoundException("JavaScript file "+pathName+" not readable!");
            Vector V=Resources.getFileLineVector(str);
            Class extendsClass=null;
            Vector implementsClasses=new Vector();
            String overPackage=null;
            for(int v=0;v<V.size();v++)
            {
                if((extendsClass==null)&&((String)V.elementAt(v)).trim().toUpperCase().startsWith("//EXTENDS "))
                {
                    String extendName=((String)V.elementAt(v)).trim().substring(10).trim();
                    try{extendsClass=loadClass(extendName);}
                    catch(ClassNotFoundException e)
                    {
                        Log.errOut("CMClass","Could not load "+CF.getName()+" from "+className+" because "+extendName+" is an invalid extension.");
                        throw e;
                    }
                }
                if((overPackage==null)&&((String)V.elementAt(v)).trim().toUpperCase().startsWith("//PACKAGE "))
                    overPackage=((String)V.elementAt(v)).trim().substring(10).trim();
                if(((String)V.elementAt(v)).toUpperCase().startsWith("//IMPLEMENTS "))
                {
                    String extendName=((String)V.elementAt(v)).substring(13).trim();
                    Class C=null;
                    try{C=loadClass(extendName);}catch(ClassNotFoundException e){continue;}
                    implementsClasses.addElement(C);
                }
            }
            Context X=Context.enter();
            JScriptLib jlib=new JScriptLib();
            X.initStandardObjects(jlib);
            jlib.defineFunctionProperties(JScriptLib.functions, JScriptLib.class, ScriptableObject.DONTENUM);
            CompilerEnvirons ce = new CompilerEnvirons(); 
            ce.initFromContext(X); 
            ClassCompiler cc = new ClassCompiler(ce); 
            if(extendsClass==null)
                Log.errOut("CMClass","Warning: "+CF.getVFSPathAndName()+" does not extend any class!");
            else
                cc.setTargetExtends(extendsClass);
            Class mainClass=null;
            if(implementsClasses.size()>0)
            {
                Class[] CS=new Class[implementsClasses.size()];
                for(int i=0;i<implementsClasses.size();i++) CS[i]=(Class)implementsClasses.elementAt(i);
                cc.setTargetImplements(CS);
            }
            Object[] objs = cc.compileToClassFiles(str.toString(), "script", 1, name);
            for (int i=0;i<objs.length;i+=2)
            {
                Class C=finishDefineClass((String)objs[i],(byte[])objs[i+1],overPackage,resolveIt);
                if(mainClass==null) mainClass=C;
            }
            Context.exit();
            if(debugging) Log.debugOut("CMClass","Loaded: "+mainClass.getName());
            return mainClass;
        }
	    result=finishDefineClass(className,classData,null,resolveIt);
		return result;
    }
    
    protected static void reloadCommandWords()
    {
    	CommandWords.clear();
        for(int c=0;c<commands.size();c++)
        {
            Command C=(Command)commands.elementAt(c);
            String[] wordList=C.getAccessWords();
            if(wordList!=null)
                for(int w=0;w<wordList.length;w++)
                    CommandWords.put(wordList[w].trim().toUpperCase(),C);
        }

    }
    
    public static boolean loadClasses(CMProps page)
    {
        try
        {
            String prefix="com/planet_ink/coffee_mud/";
            debugging=CMSecurity.isDebugging("CLASSLOADER");
            libraries=loadVectorListToObj(prefix+"Libraries/",page.getStr("LIBRARY"),ancestor("LIBRARY"));
            if(libraries.size()==0) return false;
            CMLib.registerLibraries(libraries.elements());
            if(CMLib.unregistered().length()>0)
            {
                Log.errOut("CMClass","Fatal Error: libraries are unregistered: "+CMLib.unregistered().substring(0,CMLib.unregistered().length()-2));
                return false;
            }
            
            common=loadHashListToObj(prefix+"Common/",page.getStr("COMMON"),ancestor("COMMON"));
            if(common.size()==0) return false;
            
            webMacros=CMClass.loadHashListToObj(prefix+"WebMacros/", "%DEFAULT%",ancestor("WEBMACROS"));
            Log.sysOut(Thread.currentThread().getName(),"WebMacros loaded  : "+webMacros.size());
            for(Enumeration e=webMacros.keys();e.hasMoreElements();)
            {
                String key=(String)e.nextElement();
                if(key.length()>longestWebMacro) 
                    longestWebMacro=key.length();
            }
            
            races=loadVectorListToObj(prefix+"Races/",page.getStr("RACES"),ancestor("RACE"));
            Log.sysOut(Thread.currentThread().getName(),"Races loaded      : "+races.size());
            if(races.size()==0) return false;
    
            charClasses=loadVectorListToObj(prefix+"CharClasses/",page.getStr("CHARCLASSES"),ancestor("CHARCLASS"));
            Log.sysOut(Thread.currentThread().getName(),"Classes loaded    : "+charClasses.size());
            if(charClasses.size()==0) return false;
    
            MOBs=loadVectorListToObj(prefix+"MOBS/",page.getStr("MOBS"),ancestor("MOB"));
            Log.sysOut(Thread.currentThread().getName(),"MOB Types loaded  : "+MOBs.size());
            if(MOBs.size()==0) return false;
    
            exits=loadVectorListToObj(prefix+"Exits/",page.getStr("EXITS"),ancestor("EXIT"));
            Log.sysOut(Thread.currentThread().getName(),"Exit Types loaded : "+exits.size());
            if(exits.size()==0) return false;
    
            areaTypes=loadVectorListToObj(prefix+"Areas/",page.getStr("AREAS"),ancestor("AREA"));
            Log.sysOut(Thread.currentThread().getName(),"Area Types loaded : "+areaTypes.size());
            if(areaTypes.size()==0) return false;
    
            locales=loadVectorListToObj(prefix+"Locales/",page.getStr("LOCALES"),ancestor("LOCALE"));
            Log.sysOut(Thread.currentThread().getName(),"Locales loaded    : "+locales.size());
            if(locales.size()==0) return false;
    
            abilities=loadVectorListToObj(prefix+"Abilities/",page.getStr("ABILITIES"),ancestor("ABILITY"));
            if(abilities.size()==0) return false;
    
            if((page.getStr("ABILITIES")!=null)
            &&(page.getStr("ABILITIES").toUpperCase().indexOf("%DEFAULT%")>=0))
            {
                Vector tempV;
                int size=0;
                tempV=loadVectorListToObj(prefix+"Abilities/Fighter/","%DEFAULT%",ancestor("ABILITY"));
                size=tempV.size();
                addV(tempV,abilities);
    
                tempV=loadVectorListToObj(prefix+"Abilities/Ranger/","%DEFAULT%",ancestor("ABILITY"));
                size+=tempV.size();
                addV(tempV,abilities);
    
                tempV=loadVectorListToObj(prefix+"Abilities/Paladin/","%DEFAULT%",ancestor("ABILITY"));
                size+=tempV.size();
                addV(tempV,abilities);
    
                size+=tempV.size();
                if(size>0) Log.sysOut(Thread.currentThread().getName(),"Fighter Skills    : "+size);
                addV(tempV,abilities);
    
                tempV=loadVectorListToObj(prefix+"Abilities/Druid/","%DEFAULT%",ancestor("ABILITY"));
                if(tempV.size()>0) Log.sysOut(Thread.currentThread().getName(),"Chants loaded     : "+tempV.size());
                addV(tempV,abilities);
    
                tempV=loadVectorListToObj(prefix+"Abilities/Languages/","%DEFAULT%",ancestor("ABILITY"));
                if(tempV.size()>0) Log.sysOut(Thread.currentThread().getName(),"Languages loaded  : "+tempV.size());
                addV(tempV,abilities);
    
                tempV=loadVectorListToObj(prefix+"Abilities/Properties/","%DEFAULT%",ancestor("ABILITY"));
                size=tempV.size();
                addV(tempV,abilities);
    
                tempV=loadVectorListToObj(prefix+"Abilities/Diseases/","%DEFAULT%",ancestor("ABILITY"));
                size+=tempV.size();
                addV(tempV,abilities);
    
                tempV=loadVectorListToObj(prefix+"Abilities/Poisons/","%DEFAULT%",ancestor("ABILITY"));
                size+=tempV.size();
                addV(tempV,abilities);
    
                tempV=loadVectorListToObj(prefix+"Abilities/Misc/","%DEFAULT%",ancestor("ABILITY"));
                size+=tempV.size();
                Log.sysOut(Thread.currentThread().getName(),"Properties loaded : "+size);
                addV(tempV,abilities);
    
                tempV=loadVectorListToObj(prefix+"Abilities/Prayers/","%DEFAULT%",ancestor("ABILITY"));
                Log.sysOut(Thread.currentThread().getName(),"Prayers loaded    : "+tempV.size());
                addV(tempV,abilities);
    
                tempV=loadVectorListToObj(prefix+"Abilities/Archon/","%DEFAULT%",ancestor("ABILITY"));
                size+=tempV.size();
                addV(tempV,abilities);
    
                tempV=loadVectorListToObj(prefix+"Abilities/Skills/","%DEFAULT%",ancestor("ABILITY"));
                size=tempV.size();
                addV(tempV,abilities);
    
                tempV=loadVectorListToObj(prefix+"Abilities/Thief/","%DEFAULT%",ancestor("ABILITY"));
                size+=tempV.size();
                addV(tempV,abilities);
    
                tempV=loadVectorListToObj(prefix+"Abilities/Common/","%DEFAULT%",ancestor("ABILITY"));
                size+=tempV.size();
                addV(tempV,abilities);
    
                tempV=loadVectorListToObj(prefix+"Abilities/Specializations/","%DEFAULT%",ancestor("ABILITY"));
                size+=tempV.size();
                addV(tempV,abilities);
                if(size>0) Log.sysOut(Thread.currentThread().getName(),"Skills loaded     : "+size);
    
                tempV=loadVectorListToObj(prefix+"Abilities/Songs/","%DEFAULT%",ancestor("ABILITY"));
                if(tempV.size()>0) Log.sysOut(Thread.currentThread().getName(),"Songs loaded      : "+tempV.size());
                addV(tempV,abilities);
    
                tempV=loadVectorListToObj(prefix+"Abilities/Spells/","%DEFAULT%",ancestor("ABILITY"));
                if(tempV.size()>0) Log.sysOut(Thread.currentThread().getName(),"Spells loaded     : "+tempV.size());
                addV(tempV,abilities);
    
                tempV=loadVectorListToObj(prefix+"Abilities/SuperPowers/","%DEFAULT%",ancestor("ABILITY"));
                size=tempV.size();
                addV(tempV,abilities);
                if(size>0) Log.sysOut(Thread.currentThread().getName(),"Heroics loaded    : "+size);
                
                tempV=loadVectorListToObj(prefix+"Abilities/Traps/","%DEFAULT%",ancestor("ABILITY"));
                if(tempV.size()>0) Log.sysOut(Thread.currentThread().getName(),"Traps loaded      : "+tempV.size());
                addV(tempV,abilities);
                abilities=new Vector(new TreeSet(abilities));
                
                CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: reading genAbilities");
                Vector genAbilities=CMLib.database().DBReadAbilities();
                if(genAbilities.size()>0)
                {
                    int loaded=0;
                    for(int r=0;r<genAbilities.size();r++)
                    {
                        Ability A=(Ability)(CMClass.getAbility("GenAbility").copyOf());
                        A.setStat("ALLXML",(String)((Vector)genAbilities.elementAt(r)).lastElement());
                        if(!A.ID().equals("GenAbility"))
                        {
                            abilities.addElement(A);
                            loaded++;
                        }
                    }
                    if(loaded>0)
                    {
                        Log.sysOut(Thread.currentThread().getName(),"GenAbles loaded   : "+loaded);
                        abilities=new Vector(new TreeSet(abilities));
                    }
                }
                
            }
    
            items=loadVectorListToObj(prefix+"Items/Basic/",page.getStr("ITEMS"),ancestor("ITEM"));
            if(items.size()>0) Log.sysOut(Thread.currentThread().getName(),"Basic Items loaded: "+items.size());
    
            weapons=loadVectorListToObj(prefix+"Items/Weapons/",page.getStr("WEAPONS"),ancestor("WEAPON"));
            if(weapons.size()>0) Log.sysOut(Thread.currentThread().getName(),"Weapons loaded    : "+weapons.size());
    
            armor=loadVectorListToObj(prefix+"Items/Armor/",page.getStr("ARMOR"),ancestor("ARMOR"));
            if(armor.size()>0) Log.sysOut(Thread.currentThread().getName(),"Armor loaded      : "+armor.size());
    
            miscMagic=loadVectorListToObj(prefix+"Items/MiscMagic/",page.getStr("MISCMAGIC"),ancestor("MISCMAGIC"));
            if(miscMagic.size()>0) Log.sysOut(Thread.currentThread().getName(),"Magic Items loaded: "+miscMagic.size());
    
            clanItems=loadVectorListToObj(prefix+"Items/ClanItems/",page.getStr("CLANITEMS"),ancestor("CLANITEMS"));
            if(clanItems.size()>0) Log.sysOut(Thread.currentThread().getName(),"Clan Items loaded : "+clanItems.size());
    
            miscTech=loadVectorListToObj(prefix+"Items/MiscTech/",page.getStr("MISCTECH"),ancestor("MISCTECH"));
            if(miscTech.size()>0) Log.sysOut(Thread.currentThread().getName(),"Electronics loaded: "+miscTech.size());
            Vector tempV=loadVectorListToObj(prefix+"Items/Software/",page.getStr("SOFTWARE"),"com.planet_ink.coffee_mud.Items.interfaces.Software");
            if(tempV.size()>0) addV(tempV,miscTech);
            miscTech=new Vector(new TreeSet(miscTech));
    
            if((items.size()+weapons.size()+armor.size()+miscTech.size()+miscMagic.size()+clanItems.size())==0)
                return false;
    
            behaviors=loadVectorListToObj(prefix+"Behaviors/",page.getStr("BEHAVIORS"),ancestor("BEHAVIOR"));
            Log.sysOut(Thread.currentThread().getName(),"Behaviors loaded  : "+behaviors.size());
            if(behaviors.size()==0) return false;
    
            commands=loadVectorListToObj(prefix+"Commands/",page.getStr("COMMANDS"),ancestor("COMMAND"));
            Log.sysOut(Thread.currentThread().getName(),"Commands loaded   : "+commands.size());
            if(commands.size()==0) return false;
        }
        catch(Throwable t)
        {
            t.printStackTrace();
            return false;
        }

        reloadCommandWords();
        
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
        CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: reading genRaces");
        Vector genRaces=CMLib.database().DBReadRaces();
        if(genRaces.size()>0)
        {
            int loaded=0;
            for(int r=0;r<genRaces.size();r++)
            {
                Race GR=(Race)getRace("GenRace").copyOf();
                GR.setRacialParms((String)((Vector)genRaces.elementAt(r)).elementAt(1));
                if(!GR.ID().equals("GenRace"))
                {
                    addRace(GR);
                    loaded++;
                }
            }
            if(loaded>0)
                Log.sysOut(Thread.currentThread().getName(),"GenRaces loaded   : "+loaded);
        }
        CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: reading genClasses");
        Vector genClasses=CMLib.database().DBReadClasses();
        if(genClasses.size()>0)
        {
            int loaded=0;
            for(int r=0;r<genClasses.size();r++)
            {
                CharClass CR=(CharClass)(CMClass.getCharClass("GenCharClass").copyOf());
                CR.setClassParms((String)((Vector)genClasses.elementAt(r)).elementAt(1));
                if(!CR.ID().equals("GenCharClass"))
                {
                    addCharClass(CR);
                    loaded++;
                }
            }
            if(loaded>0)
                Log.sysOut(Thread.currentThread().getName(),"GenClasses loaded : "+loaded);
        }
        CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: initializing classes");
        intializeClasses();
        CMLib.expertises().recompileExpertises();
        Log.sysOut(Thread.currentThread().getName(),"Expertises defined: "+CMLib.expertises().numExpertises());
        return true;
    }

    protected static class JScriptLib extends ScriptableObject
    {
        public String getClassName(){ return "JScriptLib";}
        static final long serialVersionUID=47;
        public static String[] functions = {"toJavaString"};
        public String toJavaString(Object O){return Context.toString(O);}
    }
}
