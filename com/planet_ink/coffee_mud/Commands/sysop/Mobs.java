package com.planet_ink.coffee_mud.Commands.sysop;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;
import java.io.*;

public class Mobs
{
	private Mobs(){}

	private static void updateMOBS(MOB meMOB, Room thisOne)
	{
		thisOne.delInhabitant(meMOB);
		ExternalPlay.DBUpdateMOBs(thisOne);
		thisOne.addInhabitant(meMOB);
	}
	public static boolean destroyUser(MOB mob, Vector commands)
		throws Exception
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY USER [USER NAME]\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return false;
		}

		MOB deadMOB=(MOB)CMClass.getMOB("StdMOB");
		boolean found=ExternalPlay.DBUserSearch(deadMOB,Util.combine(commands,2));

		if(!found)
		{
			mob.tell("The user '"+Util.combine(commands,2)+"' does not exist!\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return false;
		}

		if(mob.session().confirm("This will complete OBLITERATE the user '"+deadMOB.Name()+"' forever.  Are you SURE?! (y/N)?","N"))
		{
			ExternalPlay.destroyUser(deadMOB);
			mob.tell("The user '"+Util.combine(commands,2)+"' is no more!\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"A horrible death cry can be heard throughout the land.");
			Log.sysOut("Mobs",mob.Name()+" destroyed user "+deadMOB.Name()+".");
			return true;
		}
		return true;
	}
	public static boolean destroy(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY MOB [MOB NAME]\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return false;
		}

		String mobID=Util.combine(commands,2);
		boolean allFlag=((String)commands.elementAt(2)).equalsIgnoreCase("all");
		if(mobID.toUpperCase().startsWith("ALL.")){ allFlag=true; mobID="ALL "+mobID.substring(4);}
		if(mobID.toUpperCase().endsWith(".ALL")){ allFlag=true; mobID="ALL "+mobID.substring(0,mobID.length()-4);}
		MOB deadMOB=(MOB)mob.location().fetchInhabitant(mobID);
		boolean doneSomething=false;
		while(deadMOB!=null)
		{
			if(!deadMOB.isMonster())
			{
				mob.tell(deadMOB.name()+" is a PLAYER!!\n\r");
				if(!doneSomething)
					mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				return false;
			}
			doneSomething=true;
			deadMOB.destroy();
			mob.location().showHappens(Affect.MSG_OK_VISUAL,deadMOB.name()+" vanishes in a puff of smoke.");
			Log.sysOut("Mobs",mob.Name()+" destroyed mob "+deadMOB.Name()+".");
			deadMOB=(MOB)mob.location().fetchInhabitant(mobID);
			if(!allFlag) break;
		}
		if(!doneSomething)
		{
			mob.tell("I don't see '"+mobID+" here.\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return false;
		}
		return true;
	}

	public static void modifyPlayer(MOB mob, Vector commands)
		throws IOException
	{

		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY USER [PLAYER NAME]\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		String mobID=Util.combine(commands,2);
		MOB M=CMMap.getPlayer(mobID);
		if(M==null)
			for(Enumeration p=CMMap.players();p.hasMoreElements();)
			{
				MOB mob2=(MOB)p.nextElement();
				if(mob2.Name().equalsIgnoreCase(mobID))
				{ M=mob2; break;}
			}
		MOB TM=CMClass.getMOB("StdMOB");
		if((M==null)&&(ExternalPlay.DBUserSearch(TM,mobID)))
		{
			M=CMClass.getMOB("StdMOB");
			M.setName(TM.Name());
			ExternalPlay.DBReadMOB(M);
			ExternalPlay.DBReadFollowers(M,false);
			if(M.playerStats()!=null)
				M.playerStats().setUpdated(M.playerStats().lastDateTime());
			M.recoverEnvStats();
			M.recoverCharStats();
		}
		if(M==null)
		{
			mob.tell("There is no such player as '"+mobID+"'!");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		Generic.modifyPlayer(mob,M);
		Log.sysOut("Mobs",mob.Name()+" modified player "+M.Name()+".");
	}
	public static void modify(MOB mob, Vector commands)
		throws IOException
	{

		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY MOB [MOB NAME] [LEVEL, ABILITY, REJUV, MISC] [NUMBER, TEXT]\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		String mobID=((String)commands.elementAt(2));
		String command=((String)commands.elementAt(3)).toUpperCase();
		String restStr="";
		if(commands.size()>4)
			restStr=Util.combine(commands,4);


		MOB modMOB=(MOB)mob.location().fetchInhabitant(mobID);
		if(modMOB==null)
		{
			mob.tell("I don't see '"+mobID+" here.\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(command.equals("LEVEL"))
		{
			int newLevel=Util.s_int(restStr);
			if(newLevel>=0)
			{
				modMOB.baseEnvStats().setLevel(newLevel);
				modMOB.recoverCharStats();
				modMOB.recoverEnvStats();
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,modMOB.name()+" shakes under the transforming power.");
			}
		}
		else
		if(command.equals("ABILITY"))
		{
			int newAbility=Util.s_int(restStr);
			modMOB.baseEnvStats().setAbility(newAbility);
			modMOB.recoverEnvStats();
			mob.location().show(mob,null,Affect.MSG_OK_ACTION,modMOB.name()+" shakes under the transforming power.");
		}
		else
		if(command.equals("REJUV"))
		{
			int newRejuv=Util.s_int(restStr);
			if(newRejuv>0)
			{
				modMOB.baseEnvStats().setRejuv(newRejuv);
				modMOB.recoverEnvStats();
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,modMOB.name()+" shakes under the transforming power.");
			}
			else
			{
				modMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
				modMOB.recoverEnvStats();
				mob.tell(modMOB.name()+" will now never rejuvinate.");
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,modMOB.name()+" shakes under the transforming power.");
			}
		}
		else
		if(command.equals("MISC"))
		{
			if(modMOB.isGeneric())
				Generic.genMiscSet(mob,modMOB);
			else
				modMOB.setMiscText(restStr);
			mob.location().show(mob,null,Affect.MSG_OK_ACTION,modMOB.name()+" shakes under the transforming power.");
		}
		else
		{
			mob.tell("...but failed to specify an aspect.  Try LEVEL, ABILITY, REJUV, or MISC.");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
		}
		Log.sysOut("Mobs",mob.Name()+" modified mob "+modMOB.Name()+".");
	}

	public static void create(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE MOB [MOB NAME]\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		String mobID=((String)commands.elementAt(2));
		MOB newMOB=(MOB)CMClass.getMOB(mobID);

		if(newMOB==null)
		{
			mob.tell("There's no such thing as a '"+mobID+"'.\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		newMOB.setStartRoom(mob.location());
		newMOB.setLocation(mob.location());
		newMOB.envStats().setRejuv(5000);
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(mob.location(),true);
		mob.location().show(mob,null,Affect.MSG_OK_ACTION,"Suddenly, "+newMOB.name()+" instantiates from the Java plain.");
		if(newMOB.isGeneric())
			Generic.genMiscSet(mob,newMOB);
		Log.sysOut("Mobs",mob.Name()+" created mob "+newMOB.Name()+".");
	}
}
