package com.planet_ink.coffee_mud.application;
import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.db.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.commands.*;

public class MUD 
{
	public final static int TICK_TIME=3000;
	
	public static DBConnections DBs=null;
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
	public static Vector map=new Vector();
	
	public static SaveThread saveThread=null;
	
	public static Socials allSocials=null;
													
	public static INI page=null;
	
	public static Vector allSessions=new Vector();
	
	public static boolean DBConfirmDeletions=false;
	
	public static void fatalStartupError(int type)
	{
		String str=null;
		switch(type)
		{
		case 1:
			str="Unable to read ini file. Exiting.";
			break;
		case 2:
			str="Map is empty?! Exiting.";
			break;
		default:
			str="Fatal error loading classes.  Make sure you start up coffeemud from the directory containing the class files.";
			break;
		}
		Log.errOut("MUD",str);
		System.out.println(str);
		System.exit(-1);
	}
	
	public static void main(String a[]) throws IOException
	{
		
		int q_len = 6;
		Socket sock;

		Log.startLogFiles();
		page=new INI("coffeemud.ini");
		if(!page.loaded)
			fatalStartupError(1);
		Log.Initialize(page.getStr("SYSMSGS"),page.getStr("ERRMSGS"),page.getStr("DBGMSGS"));
		DBs =new DBConnections(page.getStr("DBCLASS"),page.getStr("DBSERVICE"),page.getStr("DBUSER"),page.getStr("DBPASS"),page.getInt("DBCONNECTIONS"),true);
		DBConfirmDeletions=page.getBoolean("DBCONFIRMDELETIONS");
		
		Log.sysOut("MUD","Starting CoffeeMud...\n\r\n\r");
		String DBerrors=DBs.errorStatus().toString();
		if(DBerrors.length()==0)
			Log.sysOut("MUD","Database connection successful.");
		else
		{
			Log.errOut("MUD","Fatal database error: "+DBerrors);
			System.exit(-1);
		}
		if(DBConfirmDeletions)
			Log.sysOut("MUD","DB Deletions will be confirmed.");
		
		String prefix="com"+File.separatorChar+"planet_ink"+File.separatorChar+"coffee_mud"+File.separatorChar;
		
		races=page.loadVectorListToObj(prefix+"Races"+File.separatorChar);
		Log.sysOut("MUD","Races loaded      : "+races.size());
		if(races.size()==0) fatalStartupError(0);
		
		charClasses=page.loadVectorListToObj(prefix+"CharClasses"+File.separatorChar);
		Log.sysOut("MUD","Classes loaded    : "+charClasses.size());
		if(charClasses.size()==0) fatalStartupError(0);

		MOBs=page.loadVectorListToObj(prefix+"MOBS"+File.separatorChar);
		Log.sysOut("MUD","MOB Types loaded  : "+MOBs.size());
		if(MOBs.size()==0) fatalStartupError(0);
		
		exits=page.loadVectorListToObj(prefix+"Exits"+File.separatorChar);
		Log.sysOut("MUD","Exit Types loaded : "+exits.size());
		if(exits.size()==0) fatalStartupError(0);
		
		locales=page.loadVectorListToObj(prefix+"Locales"+File.separatorChar);
		Log.sysOut("MUD","Locales loaded    : "+locales.size());
		if(locales.size()==0) fatalStartupError(0);
		
		abilities=page.loadVectorListToObj(prefix+"Abilities"+File.separatorChar);
		Log.sysOut("MUD","Abilities loaded  : "+abilities.size());
		if(abilities.size()==0) fatalStartupError(0);
		
		items=page.loadVectorListToObj(prefix+"Items"+File.separatorChar);
		Log.sysOut("MUD","Items loaded      : "+items.size());
		if(items.size()==0) fatalStartupError(0);
		
		weapons=page.loadVectorListToObj(prefix+"Items"+File.separatorChar+"Weapons"+File.separatorChar);
		Log.sysOut("MUD","Weapons loaded    : "+weapons.size());
		if(weapons.size()==0) fatalStartupError(0);
		
		armor=page.loadVectorListToObj(prefix+"Items"+File.separatorChar+"Armor"+File.separatorChar);
		Log.sysOut("MUD","Armor loaded      : "+armor.size());
		if(armor.size()==0) fatalStartupError(0);
		
		miscMagic=page.loadVectorListToObj(prefix+"Items"+File.separatorChar+"MiscMagic"+File.separatorChar);
		Log.sysOut("MUD","Magic Items loaded: "+miscMagic.size());
		if(miscMagic.size()==0) fatalStartupError(0);
		
		behaviors=page.loadVectorListToObj(prefix+"Behaviors"+File.separatorChar);
		Log.sysOut("MUD","Behaviors loaded  : "+behaviors.size());
		if(behaviors.size()==0) fatalStartupError(0);
		
		CommandProcessor.commandSet.loadChannels(page.getStr("CHANNELS"));
		Log.sysOut("MUD","Channels loaded   : "+Channels.numChannelsLoaded);
		
		allSocials=new Socials("resources"+File.separatorChar+"socials.txt");
		if(!allSocials.loaded)
			Log.errOut("MUD","Unable to load socials from socials.txt!");
		else
			Log.sysOut("MUD","Socials loaded    : "+allSocials.num());
		
		RoomLoader.DBRead(map);
		Log.sysOut("MUD","Mapped rooms      : "+map.size());
		if(map.size()==0) fatalStartupError(2);
		
		CommandProcessor.commandSet.loadAbilities(abilities);
		
		ServerSocket servsock=new ServerSocket(page.getInt("PORT"), q_len);
		
		saveThread=new SaveThread();
		saveThread.start();
		Log.sysOut("MUD","Save thread started");
		

		Log.sysOut("MUD","Now listening on port "+page.getInt("PORT")+".");
		try
		{
			while(true)
			{
				sock=servsock.accept();
				Log.sysOut("MUD","Got a connection!");
				PrintWriter out=new PrintWriter(sock.getOutputStream());
				StringBuffer introText=Resources.getFileResource("intro.txt");
				if(introText!=null)
					out.println(introText.toString());
				BufferedReader in=new BufferedReader(new InputStreamReader(sock.getInputStream()));
				Session S=new Session(sock,in,out);
				S.start();
				allSessions.addElement(S);
			}
		}
		catch(Throwable t)
		{
			Log.sysOut("MUD","Shut down: "+t);
			System.exit(0);
		}
	}
	
	public static Environmental getEnv(Vector fromThese, String calledThis)
	{
		for(int i=0;i<fromThese.size();i++)
		{
			Environmental E=(Environmental)fromThese.elementAt(i);
			if(E.ID().equalsIgnoreCase(calledThis))
				return (Environmental)fromThese.elementAt(i);
		}
		return null;
	}
	
	public static Object getGlobal(Vector fromThese, String calledThis)
	{
		for(int i=0;i<fromThese.size();i++)
			if(Util.id(fromThese.elementAt(i)).equalsIgnoreCase(calledThis))
				return fromThese.elementAt(i);
		return null;
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
		return thisItem;
	}
	
	public static CharClass getCharClass(String calledThis)
	{
		return (CharClass)getGlobal(charClasses,calledThis);
	}
	public static Race getRace(String calledThis)
	{
		return (Race)getGlobal(races,calledThis);
	}
	public static Behavior getBehavior(String calledThis)
	{
		return (Behavior)getGlobal(behaviors,calledThis);
	}
	public static Room getLocale(String calledThis)
	{
		return (Room)getEnv(locales,calledThis);
	}
	public static Exit getExit(String calledThis)
	{
		return (Exit)getEnv(exits,calledThis);
	}
	public static Room getRoom(String calledThis)
	{
		return (Room)getEnv(map,calledThis);
	}
	public static MOB getMOB(String calledThis)
	{
		for(int i=0;i<MOBs.size();i++)
		{
			MOB mob=(MOB)MOBs.elementAt(i);
			
			if(INI.className(mob).equalsIgnoreCase(calledThis))
				return mob;
		}
		return null;
	}
	public static Ability getAbility(String calledThis)
	{
		return (Ability)getGlobal(abilities,calledThis);
	}

}
