package com.planet_ink.coffee_mud.Commands.base;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.io.*;
import java.util.*;
public class FrontDoor
{

	
	private boolean classOkForMe(MOB mob, CharClass thisClass)
	{
		if((thisClass.playerSelectable())
		   &&((CommonStrings.getVar(CommonStrings.SYSTEM_MULTICLASS).startsWith("NO"))
			  ||(CommonStrings.getVar(CommonStrings.SYSTEM_MULTICLASS).startsWith("MULTI"))
			  ||(thisClass.baseClass().equals(thisClass.ID())))
		   &&thisClass.qualifiesForThisClass(mob,true))
			return true;
		return false;
	}
	
	private Vector classQualifies(MOB mob)
	{
		Vector them=new Vector();
		for(int c=0;c<CMClass.charClasses.size();c++)
		{
			CharClass thisClass=(CharClass)CMClass.charClasses.elementAt(c);
			if(classOkForMe(mob,thisClass))
				them.addElement(thisClass);
		}
		return them;
	}

	private boolean isOkName(String login)
	{
		if(login.length()>20) return false;
		if(login.trim().indexOf(" ")>=0) return false;
		
		Vector V=Util.parse(login.toUpperCase().trim());
		for(int v=V.size()-1;v>=0;v--)
		{
			String str=(String)V.elementAt(v);
			if((" THE A AN ").indexOf(" "+str+" ")>=0)
				V.removeElementAt(v);
		}
		for(int v=0;v<V.size();v++)
		{
			String str=(String)V.elementAt(v);
			if((" YOU SHIT FUCK CUNT FAGGOT ASSHOLE ARSEHOLE PUSSY COCK SLUT BITCH DAMN CRAP ADMIN SYSOP ").indexOf(" "+str+" ")>=0)
				return false;
		}
		for(int c=0;c<login.length();c++)
		{
			char C=Character.toUpperCase(login.charAt(c));
			if(("ABCDEFGHIJKLMNOPQRSTUVWXYZ ").indexOf(C)<0)
				return false;
		}
		for(int m=0;m<CMClass.MOBs.size();m++)
		{
			MOB tm=(MOB)CMClass.MOBs.elementAt(m);
			if((CoffeeUtensils.containsString(tm.ID(),login))
			||(CoffeeUtensils.containsString(tm.name(),login)))
				return false;
		}
		for(Enumeration e=CMMap.MOBs.elements();e.hasMoreElements();)
		{
			MOB tm=(MOB)e.nextElement();
			if((CoffeeUtensils.containsString(tm.ID(),login))
			||(CoffeeUtensils.containsString(tm.name(),login)))
				return false;

		}
		for(int c=0;c<login.length();c++)
		{
			char C=Character.toUpperCase(login.charAt(c));
			if(("ABCDEFGHIJKLMNOPQRSTUVWXYZ ").indexOf(C)<0)
				return false;
		}
		return true;
	}

	public String nameFixer(String name)
	{
		return Character.toUpperCase(name.charAt(0))+name.substring(1).trim();
	}
	
	public boolean login(MOB mob)
		throws IOException
	{
		if(mob==null)
			return false;
		if(mob.session()==null)
			return false;

		String login=mob.session().prompt("name:");
		if(login==null) return false;
		login=login.trim();
		if(login.length()==0) return false;

		boolean found=ExternalPlay.DBUserSearch(mob,login);
		if(found)
		{
			mob.session().print("password:");
			String password=mob.session().blockingIn();
			if((mob.password().equalsIgnoreCase(password))&&(mob.name().trim().length()>0))
			{
				boolean swapMade=false;
				for(int s=0;s<Sessions.size();s++)
				{
					Session thisSession=(Session)Sessions.elementAt(s);
					if((thisSession.mob()!=null)&&(thisSession!=mob.session()))
					{
						if((thisSession.mob().ID().equals(mob.ID())))
						{
							swapMade=true;
							Room oldRoom=thisSession.mob().location();
							if(oldRoom!=null)
							while(oldRoom.isInhabitant(thisSession.mob()))
								oldRoom.delInhabitant(thisSession.mob());
							mob.session().setMob(thisSession.mob());
							thisSession.mob().setSession(mob.session());
							thisSession.setMob(null);
							thisSession.setKillFlag(true);
							Log.sysOut("FrontDoor","Session swap for "+mob.session().mob().name()+".");
							mob.session().mob().bringToLife(oldRoom,false);
							return true;
						}
					}
				}
				MOB oldMOB=mob;
				if(CMMap.MOBs.get(oldMOB.ID())!=null)
				{
					oldMOB.session().setMob((MOB)CMMap.MOBs.get(oldMOB.ID()));
					mob=oldMOB.session().mob();
					mob.setSession(oldMOB.session());
					if(mob!=oldMOB)
						oldMOB.setSession(null);
					showTheNews(mob);
					mob.bringToLife(mob.location(),false);
				}
				else
				{
					ExternalPlay.DBReadMOB(mob);
					mob.setUserInfo(mob.ID(),password);
					if(mob.baseCharStats()!=null)
					{
						mob.baseCharStats().getCurrentClass().startCharacter(mob,false,true);
						mob.baseCharStats().getMyRace().startRacing(mob,true);
					}
					showTheNews(mob);
					mob.bringToLife(mob.location(),true);
				}
				ExternalPlay.DBReadFollowers(mob);
			}
			else
			{
				Log.sysOut("FrontDoor","Failed login: "+mob.name());
				mob.setUserInfo("","");
				mob.session().println("\n\rInvalid password.\n\r");
				return false;
			}
		}
		else
		{
			if(!isOkName(login))
			{
				mob.session().println("\n\rThat name is unrecognized.\n\rThat name is also not available for new users.\n\r  Choose another name (no spaces allowed)!\n\r");
				mob.setUserInfo("","");
			}
			else
			if(mob.session().confirm("\n\r'"+nameFixer(login)+"' does not exist.\n\rIs this a new character you would like to create (y/N)?","N"))
			{
				login=nameFixer(login.trim());
				mob.session().println(null,null,"\n\r\n\r"+Resources.getFileResource("newchar.txt").toString());

				String password="";
				while(password.length()==0)
				{
					password=mob.session().prompt("\n\rEnter a password: ","");
					if(password.length()==0)
						mob.session().println("\n\rYou must enter a password to continue.");
				}
				mob.setUserInfo(login,password);
				Log.sysOut("FrontDoor","Creating user: "+mob.name());

				mob.session().setTermID(0);
				if(mob.session().confirm("\n\rDo want ANSI colors (Y/n)?","Y"))
					mob.session().setTermID(1);

				mob.session().println(null,null,Resources.getFileResource("races.txt").toString());

				StringBuffer listOfRaces=new StringBuffer("[");
				boolean tmpFirst = true;
				for(int r=0;r<CMClass.races.size();r++)
				{
					Race thisRace=(Race)CMClass.races.elementAt(r);
					if(thisRace.playerSelectable())
					{
						if (!tmpFirst)
							listOfRaces.append(", ");
						else
							tmpFirst = false;
						listOfRaces.append("^H"+thisRace.name()+"^N");
					}
				}
				listOfRaces.append("]");

				Race newRace=null;
				while(newRace==null)
				{
					mob.session().print("\n\r^!Please choose from the following races (?):^N\n\r");
					mob.session().print(listOfRaces.toString());
					String raceStr=mob.session().prompt("\n\r: ","");
					if(raceStr.trim().equalsIgnoreCase("?"))
						mob.session().println(null,null,"\n\r"+Resources.getFileResource("races.txt").toString());
					else
					{
						newRace=CMClass.getRace(raceStr);
						if((newRace!=null)&&(!newRace.playerSelectable()))
							newRace=null;
						if(newRace==null)
							for(int r=0;r<CMClass.races.size();r++)
							{
								Race R=(Race)CMClass.races.elementAt(r);
								if((R.name().equalsIgnoreCase(raceStr))
								&&(R.playerSelectable()))
								{
									newRace=(Race)CMClass.races.elementAt(r);
									break;
								}
							}
						if(newRace==null)
							for(int r=0;r<CMClass.races.size();r++)
							{
								Race R=(Race)CMClass.races.elementAt(r);
								if((R.name().toUpperCase().startsWith(raceStr.toUpperCase()))
								&&(R.playerSelectable()))
								{
									newRace=(Race)CMClass.races.elementAt(r);
									break;
								}
							}
						if(newRace!=null)
						{
							StringBuffer str=ExternalPlay.getHelpText(newRace.ID().toUpperCase());
							if(str!=null) mob.tell("\n\r^N"+str.toString()+"\n\r");
							if(!mob.session().confirm("^!Is ^H"+newRace.name()+"^N^! correct (Y/n)?^N","Y"))
								newRace=null;
						}
					}
				}
				mob.baseCharStats().setMyRace(newRace);

				String Gender="";
				while(Gender.length()==0)
					Gender=mob.session().choose("\n\r^!What is your gender (M/F)?^N","MF","");

				mob.baseCharStats().setStat(CharStats.GENDER,(int)Gender.toUpperCase().charAt(0));
				mob.baseCharStats().getMyRace().startRacing(mob,false);

				mob.session().println(null,null,"\n\r\n\r"+Resources.getFileResource("stats.txt").toString());

				boolean mayCont=true;
				int maxStat[]={18,18,18,18,18,18};
				StringBuffer listOfClasses=new StringBuffer("??? no classes ???");
				while(mayCont)
				{
					mob.baseCharStats().reRoll();
					mob.recoverCharStats();
					Vector V=classQualifies(mob);
					if(V.size()>1)
					{
						StringBuffer classes=new StringBuffer("");
						listOfClasses = new StringBuffer("");
						for(int v=0;v<V.size();v++)
							if(v==V.size()-1)
							{
								if (v != 0)
								{
									classes.append("^?and ^?");
									listOfClasses.append("^?or ^?");
								}
								classes.append(((CharClass)V.elementAt(v)).name());
								listOfClasses.append(((CharClass)V.elementAt(v)).name());
							}
							else
							{
								classes.append(((CharClass)V.elementAt(v)).name()+"^?, ^?");
								listOfClasses.append(((CharClass)V.elementAt(v)).name()+"^?, ^?");
							}

						mob.session().println("Your current stats are: \n\r"
											  +mob.charStats().getStats(maxStat)
											  +Util.padRight("TOTAL POINTS",15)+": "+CharStats.MAX_STATS+"/"+(18*5));
						
						mob.session().println("\n\rThis would qualify you for ^H"+classes.toString()+"^N.");

						if(!mob.session().confirm("^!Would you like to re-roll (y/N)?^N","N"))
							mayCont=false;
					}
				}
				mob.session().println(null,null,Resources.getFileResource("classes.txt").toString());

				CharClass newClass=null;
				while(newClass==null)
				{
					mob.session().print("\n\r^!Please choose from the following Classes:\n\r");
					mob.session().print("^H[" + listOfClasses.toString() + "]^N");
					String ClassStr=mob.session().prompt("\n\r: ","");
					if(ClassStr.trim().equalsIgnoreCase("?"))
						mob.session().println(null,null,"\n\r"+Resources.getFileResource("classes.txt").toString());
					else
					{
						newClass=CMClass.getCharClass(ClassStr);
						if(newClass==null)
						for(int c=0;c<CMClass.charClasses.size();c++)
						{
							CharClass thisClass=(CharClass)CMClass.charClasses.elementAt(c);
							if(classOkForMe(mob,thisClass))
								if(thisClass.name().equalsIgnoreCase(ClassStr))
								{
									newClass=thisClass;
									break;
								}
						}
						if(newClass==null)
						for(int c=0;c<CMClass.charClasses.size();c++)
						{
							CharClass thisClass=(CharClass)CMClass.charClasses.elementAt(c);
							if(classOkForMe(mob,thisClass))
								if(thisClass.name().toUpperCase().startsWith(ClassStr.toUpperCase()))
								{
									newClass=thisClass;
									break;
								}
						}
						if((newClass!=null)&&(classOkForMe(mob,newClass)))
						{
							StringBuffer str=ExternalPlay.getHelpText(newClass.ID().toUpperCase());
							if(str!=null) mob.tell("\n\r^N"+str.toString()+"\n\r");
							if(!mob.session().confirm("^NIs ^H"+newClass.name()+"^N correct (Y/n)?","Y"))
								newClass=null;
						}
						else
							newClass=null;
					}
				}
				mob.baseEnvStats().setLevel(1);
				mob.baseCharStats().setCurrentClass(newClass);
				mob.baseCharStats().setClassLevel(newClass,1);
				mob.baseEnvStats().setSensesMask(0);

				mob.baseState().setHitPoints(20);
				mob.baseState().setMovement(100);
				mob.baseState().setMana(100);

				Item r=(Item)CMClass.getItem("Ration");
				Item w=(Item)CMClass.getItem("Waterskin");
				Item t=(Item)CMClass.getItem("Torch");
				mob.addInventory(r);
				mob.addInventory(w);
				mob.addInventory(t);
				mob.setWimpHitPoint(5);

				mob.baseCharStats().getMyRace().startRacing(mob,false);
				mob.baseCharStats().getMyRace().outfit(mob);

				mob.recoverCharStats();
				mob.recoverEnvStats();
				mob.recoverMaxState();
				mob.resetToMaxState();

				mob.baseCharStats().getCurrentClass().startCharacter(mob,false,false);

				mob.session().println(null,null,"\n\r\n\r"+Resources.getFileResource("alignment.txt").toString());

				String alignment="";
				while(alignment.length()==0)
					alignment=mob.session().choose("Select a starting alignment:\n\r Good, Evil, or Neutral (G/N/E): ","GNE","");
				switch(alignment.charAt(0))
				{
				case 'G':
					mob.setAlignment(1000);
					break;
				case 'E':
					mob.setAlignment(0);
					break;
				case 'N':
				default:
					mob.setAlignment(500);
					break;
				}
				mob.baseCharStats().getCurrentClass().outfit(mob);
				mob.bringToLife(mob.getStartRoom(),true);
				ExternalPlay.DBCreateCharacter(mob);
				if(CMMap.MOBs.get(mob.ID())==null)
					CMMap.MOBs.put(mob.ID(),mob);

				Log.sysOut("FrontDoor","Created user: "+mob.name());
				return true;
			}
			return false;
		}
		mob.session().println("\n\r");
		return true;
	}
	
	public void showTheNews(MOB mob)
	{
		StringBuffer buf=new StringBuffer("");
		Vector journal=ExternalPlay.DBReadJournal("CoffeeMud News");
		for(int which=0;which<journal.size();which++)
		{
			Vector entry=(Vector)journal.elementAt(which);
			String from=(String)entry.elementAt(1);
			long last=Util.s_long((String)entry.elementAt(2));
			String to=(String)entry.elementAt(3);
			String subject=(String)entry.elementAt(4);
			String message=(String)entry.elementAt(5);
			boolean mineAble=to.equalsIgnoreCase(mob.name())||from.equalsIgnoreCase(mob.name());
			IQCalendar C=IQCalendar.getIQInstance();
			C.setTimeInMillis(last);
			if((C.after(mob.lastDateTime()))
			&&(to.equals("ALL")||mineAble))
			{
				buf.append("\n\r--------------------------------------\n\r");
				buf.append("\n\rNews: "+C.d2String()+"\n\r"+"FROM: "+Util.padRight(from,15)+"\n\rTO  : "+Util.padRight(to,15)+"\n\rSUBJ: "+subject+"\n\r"+message);
			}
		}
		if((!mob.isMonster())&&(buf.length()>0))
			mob.session().unfilteredPrintln(buf.toString()+"\n\r--------------------------------------\n\r");
	}
}
