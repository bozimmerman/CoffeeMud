package com.planet_ink.coffee_mud.commands;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.io.*;
import java.util.*;
public class FrontDoor
{

	private Vector classQualifies(MOB mob)
	{
		Vector them=new Vector();
		for(int c=0;c<CMClass.charClasses.size();c++)
		{
			CharClass thisClass=(CharClass)CMClass.charClasses.elementAt(c);
			if((thisClass.playerSelectable())&&thisClass.qualifiesForThisClass(mob))
				them.addElement(thisClass);
		}
		return them;
	}

	private boolean isOkName(String login)
	{
		login=login.trim();

		if(login.indexOf(" ")>0) return false;
		if((" SHIT FUCK CUNT FAGGOT ASSHOLE ARSEHOLE PUSSY COCK SLUT BITCH DAMN CRAP ADMIN SYSOP ").indexOf(" "+login.toUpperCase()+" ")>=0)
			return false;
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
		return true;

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

		if(login.equalsIgnoreCase("You"))
			return false;
		ExternalPlay.DBUserSearch(mob,login);
		if(mob.ID().trim().length()>0)
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
							mob.session().mob().bringToLife(oldRoom);
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
				}
				else
				{
					ExternalPlay.DBRead(mob);
					mob.setUserInfo(mob.ID(),password,Calendar.getInstance());
					ExternalPlay.DBUpdate(mob);
					if(mob.baseCharStats()!=null)
						if(mob.baseCharStats().getMyClass()!=null)
							mob.baseCharStats().getMyClass().logon(mob);
				}
				mob.bringToLife(mob.location());
				ExternalPlay.DBReadFollowers(mob);
			}
			else
			{
				Log.sysOut("FrontDoor","Failed login: "+mob.name());
				mob.setUserInfo("","",Calendar.getInstance());
				mob.session().println("\n\rInvalid password.\n\r");
				return false;
			}
		}
		else
		{
			if(!isOkName(login))
			{
				mob.session().println("\n\rThat name is unrecognized.\n\rThat name is also not available for new users.\n\r  Choose another name (no spaces allowed)!\n\r");
				mob.setUserInfo("","",Calendar.getInstance());
			}
			else
			if(mob.session().confirm("\n\r'"+login+"' does not exist.\n\rIs this a new character you would like to create (y/N)?","N"))
			{

				mob.session().println(null,null,"\n\r\n\r"+Resources.getFileResource("newchar.txt").toString());

				String password="";
				while(password.length()==0)
				{
					password=mob.session().prompt("\n\rEnter a password: ","");
					if(password.length()==0)
						mob.session().println("\n\rYou must enter a password to continue.");
				}
				mob.setUserInfo(login,password,Calendar.getInstance());
				Log.sysOut("FrontDoor","Creating user: "+mob.name());
				
				mob.session().setTermID(0);
				if(mob.session().confirm("\n\rDo want ANSI colors (Y/n)?","Y"))
					mob.session().setTermID(1);

				mob.session().println(null,null,"\n\r\n\r"+Resources.getFileResource("races.txt").toString());

				mob.session().print("\n\r^BPlease choose from the following races:^N\n\r");

				StringBuffer tmpStrB=new StringBuffer("[");
				boolean tmpFirst = true;
				for(int r=0;r<CMClass.races.size();r++)
				{
					Race thisRace=(Race)CMClass.races.elementAt(r);
					if(thisRace.playerSelectable())
					{
						if (!tmpFirst)
							tmpStrB.append(", ");
						else
							tmpFirst = false;
						tmpStrB.append("^H"+thisRace.name()+"^?");
					}
				}
				tmpStrB.append("]");
				mob.session().print(tmpStrB.toString());
					
				Race newRace=null;
				while(newRace==null)
				{
					String raceStr=mob.session().prompt("\n\r: ","");
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
						if(!mob.session().confirm("^BIs ^H"+newRace.name()+"^? correct (Y/n)?^N","Y"))
							newRace=null;
				}
				
				String Gender="";
				while(Gender.length()==0)
					Gender=mob.session().choose("\n\r^BWhat is your gender (M/F)?^N","MF","");

				mob.baseCharStats().setGender(Gender.toUpperCase().charAt(0));

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

						mob.session().println("Your current stats are: \n\r"+mob.charStats().getStats(maxStat));
						mob.session().println("This would qualify you for ^H"+classes.toString()+"^N.");
							
						if(!mob.session().confirm("^BWould you like to re-roll (y/N)?^N","N"))
							mayCont=false;
					}
				}
				mob.baseCharStats().setMyRace(newRace);

				mob.session().println(null,null,"\n\r\n\r"+Resources.getFileResource("classes.txt").toString());

				mob.session().print("\n\r^BPlease choose from the following Classes:\n\r");
				mob.session().print("^H[" + listOfClasses.toString() + "]^N");
				CharClass newClass=null;
				while(newClass==null)
				{
					String ClassStr=mob.session().prompt("\n\r: ","");
					newClass=CMClass.getCharClass(ClassStr);
					if(newClass==null)
					for(int c=0;c<CMClass.charClasses.size();c++)
					{
						CharClass thisClass=(CharClass)CMClass.charClasses.elementAt(c);
						if((thisClass.playerSelectable())&&thisClass.qualifiesForThisClass(mob))
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
						if((thisClass.playerSelectable())&&thisClass.qualifiesForThisClass(mob))
							if(thisClass.name().toUpperCase().startsWith(ClassStr.toUpperCase()))
							{
								newClass=thisClass;
								break;
							}
					}
					if((newClass!=null)
					&&(newClass.playerSelectable())
					&&(newClass.qualifiesForThisClass(mob)))
					{
						if(!mob.session().confirm("Is "+newClass.name()+" correct (Y/n)?","Y"))
							newClass=null;
					}
					else
						newClass=null;
				}
				mob.baseCharStats().setMyClass(newClass);

				mob.baseEnvStats().setLevel(1);
				mob.baseEnvStats().setSensesMask(0);


				mob.baseState().setHitPoints(20);
				mob.baseState().setMovement(100);
				mob.baseState().setMana(100);

				mob.setStartRoom(CMMap.startRoom());
				Item r=(Item)CMClass.getItem("Ration");
				Item w=(Item)CMClass.getItem("Waterskin");
				Item t=(Item)CMClass.getItem("Torch");
				mob.addInventory(r);
				mob.addInventory(w);
				mob.addInventory(t);
				mob.setWimpHitPoint(5);

				mob.baseCharStats().getMyRace().newCharacter(mob);

				mob.recoverCharStats();
				mob.recoverEnvStats();
				mob.recoverMaxState();
				mob.resetToMaxState();

				mob.baseCharStats().getMyClass().newCharacter(mob,false);

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

				mob.bringToLife(mob.location());
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
}
