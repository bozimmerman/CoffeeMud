package com.planet_ink.coffee_mud.commands.sysop;


import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.db.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Exits.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.*;
import java.io.*;

public class SysopSocials
{
	String Social_name;
	String You_see;
	String Third_party_sees;
	String Target_sees;
	String See_when_no_target;
	int sourceCode=Affect.GENERAL;
	int othersCode=Affect.GENERAL;
	int targetCode=Affect.GENERAL;
	public static void Create(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.isMonster())
			return;

		if(commands.size()<3)
		{
			mob.tell("but fail to specify the proper fields.\n\rThe format is CREATE SOCIAL [NAME]\n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		Social soc2=new Social();
		soc2.Social_name=((String)commands.elementAt(2)).toUpperCase();
		if(modifySocial(mob,soc2))
		{
			soc2.Social_name=soc2.Social_name.toUpperCase();
			if(MUD.allSocials.FetchSocial(soc2.Social_name)!=null)
			{
				mob.tell("That social already exists.  Try MODIFY!");
				return;
			}
			else
			{
				MUD.allSocials.soc.put(soc2.Social_name,soc2);
				MUD.allSocials.socialsList=null;
				MUD.allSocials.save();
			}
			Log.sysOut("SysopSocials",mob.ID()+" created social "+soc2.Social_name+".");
		}
	}

	public static boolean modifySocial(MOB mob, Social soc)
		throws IOException
	{
		String name=soc.Social_name;
		int x=name.toUpperCase().indexOf("<T-NAME>");
		boolean targeted=false;
		boolean self=false;
		if(x>=0)
		{
			targeted=true;
			name=name.substring(0,x).trim().toUpperCase();
		}
		else
		if(name.toUpperCase().endsWith("SELF"))
		{
			self=true;
			name=name.substring(0,name.length()-4).trim().toUpperCase();
		}


		mob.session().rawPrintln("\n\rSocial name '"+name+"' Enter new.");
		String newName=mob.session().prompt(": ","");
		if((newName!=null)&&(newName.length()>0))
			name=newName;
		else
			mob.session().println("(no change)");

		mob.session().rawPrintln("\n\rTarget="+(targeted?"TARGET":(self?"SELF":"NONE")));
		newName=mob.session().choose("Change (T/S/N)? ","TSN","");
		if((newName!=null)&&(newName.length()>0))
		{
			newName=newName.toUpperCase();
			switch(newName.charAt(0))
			{
				case 'T':
				targeted=true;
				self=false;
				break;
				case 'S':
				targeted=false;
				self=true;
				break;
				case 'N':
				targeted=false;
				self=false;
				break;
			}
		}
		else
			mob.session().println("(no change)");

		if(targeted)
			soc.Social_name=name+" <T-NAME>";
		else
		if(self)
			soc.Social_name=name+" SELF";
		else
			soc.Social_name=name;

		mob.session().rawPrintln("\n\rYou see '"+soc.You_see+"'.  Enter new.");
		newName=mob.session().prompt(": ","");
		if((newName!=null)&&(newName.length()>0))
			soc.You_see=newName;
		else
			mob.session().println("(no change)");


		if(soc.sourceCode==Affect.GENERAL)
			soc.sourceCode=Affect.HANDS_GENERAL;
		mob.session().rawPrintln("\n\rYour action type="+((soc.sourceCode==Affect.SOUND_WORDS)?"SPEAKING":((soc.sourceCode==Affect.HANDS_GENERAL)?"MOVEMENT":"MAKING NOISE")));
		newName=mob.session().choose("Change (W/M/N)? ","WMN","");
		if((newName!=null)&&(newName.length()>0))
		{
			newName=newName.toUpperCase();
			switch(newName.charAt(0))
			{
				case 'W':
				soc.sourceCode=Affect.SOUND_WORDS;
				break;
				case 'M':
				soc.sourceCode=Affect.HANDS_GENERAL;
				break;
				case 'N':
				soc.sourceCode=Affect.SOUND_NOISE;
				break;
			}
		}
		else
			mob.session().println("(no change)");

		mob.session().rawPrintln("\n\rOthers see '"+soc.Third_party_sees+"'.  Enter new.");
		newName=mob.session().prompt(": ","");
		if((newName!=null)&&(newName.length()>0))
			soc.Third_party_sees=newName;
		else
			mob.session().println("(no change)");

		if(soc.othersCode==Affect.GENERAL)
			soc.othersCode=Affect.HANDS_GENERAL;
		mob.session().rawPrintln("\n\rOthers affect type="+((soc.othersCode==Affect.SOUND_WORDS)?"HEARING WORDS":((soc.othersCode==Affect.VISUAL_WNOISE)?"SEEING MOVEMENT":"HEARING NOISE")));
		newName=mob.session().choose("Change (W/M/N)? ","WMN","");
		if((newName!=null)&&(newName.length()>0))
		{
			newName=newName.toUpperCase();
			switch(newName.charAt(0))
			{
				case 'W':
				soc.othersCode=Affect.SOUND_WORDS;
				soc.targetCode=Affect.SOUND_WORDS;
				break;
				case 'M':
				soc.othersCode=Affect.VISUAL_WNOISE;
				soc.targetCode=Affect.VISUAL_WNOISE;
				break;
				case 'N':
				soc.othersCode=Affect.SOUND_NOISE;
				soc.targetCode=Affect.SOUND_NOISE;
				break;
			}
		}
		else
			mob.session().println("(no change)");



		if(soc.Social_name.indexOf("<T-NAME>")>=0)
		{
			mob.session().rawPrintln("\n\rTarget sees '"+soc.Target_sees+"'.  Enter new.");
			newName=mob.session().prompt(": ","");
			if((newName!=null)&&(newName.length()>0))
				soc.Target_sees=newName;
			else
				mob.session().println("(no change)");


		if(soc.targetCode==Affect.GENERAL)
			soc.targetCode=Affect.HANDS_GENERAL;
		mob.session().rawPrintln("\n\rTarget affect type="+((soc.targetCode==Affect.SOUND_WORDS)?"HEARING WORDS":((soc.targetCode==Affect.HANDS_GENERAL)?"BEING TOUCHED":((soc.targetCode==Affect.VISUAL_WNOISE)?"SEEING MOVEMENT":"HEARING NOISE"))));
		newName=mob.session().choose("Change (W/T/M/N)? ","WMTN","");
		if((newName!=null)&&(newName.length()>0))
		{
			newName=newName.toUpperCase();
			switch(newName.charAt(0))
			{
				case 'W':
				soc.targetCode=Affect.SOUND_WORDS;
				break;
				case 'M':
				soc.targetCode=Affect.VISUAL_WNOISE;
				break;
				case 'T':
				soc.targetCode=Affect.HANDS_GENERAL;
				break;
				case 'N':
				soc.targetCode=Affect.SOUND_NOISE;
				break;
			}
		}
		else
			mob.session().println("(no change)");



			mob.session().rawPrintln("\n\rYou see when no target '"+soc.See_when_no_target+"'.  Enter new.");
			newName=mob.session().prompt(": ","");
			if((newName!=null)&&(newName.length()>0))
				soc.See_when_no_target=newName;
			else
				mob.session().println("(no change)");
		}
		return true;
	}

	public static void Modify(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.isMonster())
			return;

		if(commands.size()<3)
		{
			mob.session().rawPrintln("but fail to specify the proper fields.\n\rThe format is MODIFY SOCIAL [NAME] ([<T-NAME>], [SELF])\n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		else
		if(commands.size()>3)
		{
			String therest=CommandProcessor.combine(commands,3);
			if(!((therest.equalsIgnoreCase("<T-NAME>")||therest.equalsIgnoreCase("SELF"))))
			{
				mob.session().rawPrintln("but fail to specify the proper second parameter.\n\rThe format is MODIFY SOCIAL [NAME] ([<T-NAME>], [SELF])\n\r");
				mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a powerful spell.");
				return;
			}
		}

		String stuff=CommandProcessor.combine(commands,2).toUpperCase();
		Social soc2=MUD.allSocials.FetchSocial(stuff);
		if(soc2==null)
		{
			mob.tell("but fail to specify an EXISTING SOCIAL!\n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		if(modifySocial(mob,soc2))
		{
			soc2.Social_name=soc2.Social_name.toUpperCase();
			if(MUD.allSocials.FetchSocial(soc2.Social_name)!=soc2)
			{
				mob.session().rawPrintln("That social already exists in another form (<T-NAME>, or SELF).  Try deleting the other one first!");
				return;
			}
			else
			{
				MUD.allSocials.socialsList=null;
				MUD.allSocials.save();
			}
			Log.sysOut("SysopSocials",mob.ID()+" modified social "+soc2.Social_name+".");
		}
		mob.location().show(mob,null,Affect.VISUAL_WNOISE,"The happiness of all mankind has just increased!");
	}

	public static void Destroy(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.session().rawPrintln("but fail to specify the proper fields.\n\rThe format is DESTROY SOCIAL [NAME] ([<T-NAME>], [SELF])\n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		else
		if(commands.size()>3)
		{
			String therest=CommandProcessor.combine(commands,3);
			if(!((therest.equalsIgnoreCase("<T-NAME>")||therest.equalsIgnoreCase("SELF"))))
			{
				mob.session().rawPrintln("but fail to specify the proper second parameter.\n\rThe format is DESTROY SOCIAL [NAME] ([<T-NAME>], [SELF])\n\r");
				mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a powerful spell.");
				return;
			}
		}

		Social soc2=MUD.allSocials.FetchSocial(CommandProcessor.combine(commands,2).toUpperCase());
		if(soc2==null)
		{
			mob.tell("but fail to specify an EXISTING SOCIAL!\n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		else
		{
			if(mob.session().confirm("Are you sure you want to delete that social (y/N)? ","N"))
			{
				MUD.allSocials.soc.remove(soc2.Social_name);
				MUD.allSocials.socialsList=null;
				MUD.allSocials.save();
				mob.location().show(mob,null,Affect.VISUAL_WNOISE,"The happiness of all mankind has just decreased!");
			}
			else
				mob.location().show(mob,null,Affect.VISUAL_WNOISE,"The happiness of all mankind has just increased!");
			Log.sysOut("SysopSocials",mob.ID()+" destroyed social "+soc2.Social_name+".");
		}

	}
}
