package com.planet_ink.coffee_mud.Commands.sysop;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Commands.base.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;
import java.io.*;

public class SysopSocials
{
	private SysopSocials(){}

	
	
	public static void create(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.isMonster())
			return;

		if(commands.size()<3)
		{
			mob.tell("but fail to specify the proper fields.\n\rThe format is CREATE SOCIAL [NAME]\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		Social soc2=new Social();
		soc2.setName(((String)commands.elementAt(2)).toUpperCase());
		if(modifySocial(mob,soc2))
		{
			soc2.setName(soc2.name().toUpperCase());
			if(Socials.FetchSocial(soc2.name())!=null)
			{
				mob.tell("That social already exists.  Try MODIFY!");
				return;
			}
			else
			{
				Socials.put(soc2.name(),soc2);
				Socials.resetSocialsList();
				Socials.save();
			}
			Log.sysOut("SysopSocials",mob.ID()+" created social "+soc2.name()+".");
		}
	}

	public static boolean modifySocial(MOB mob, Social soc)
		throws IOException
	{
		String name=soc.name();
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
		newName=mob.session().choose("Change T)arget, S)elf, N)one: ","TSN","");
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
			soc.setName(name+" <T-NAME>");
		else
		if(self)
			soc.setName(name+" SELF");
		else
			soc.setName(name);

		mob.session().rawPrintln("\n\rYou see '"+soc.You_see()+"'.  Enter new.");
		newName=mob.session().prompt(": ","");
		if((newName!=null)&&(newName.length()>0))
			soc.setYou_see(newName);
		else
			mob.session().println("(no change)");


		if(soc.sourceCode()==Affect.MSG_OK_ACTION)
			soc.setSourceCode(Affect.MSG_HANDS);
		mob.session().rawPrintln("\n\rYour action type="+((soc.sourceCode()==Affect.MSG_NOISYMOVEMENT)?"LARGE MOVEMENT":((soc.sourceCode()==Affect.MSG_SPEAK)?"SPEAKING":((soc.sourceCode()==Affect.MSG_HANDS)?"MOVEMENT":"MAKING NOISE"))));
		newName=mob.session().choose("Change W)ords, M)ovement (small), S)ound, L)arge Movement ","WMSL","");
		if((newName!=null)&&(newName.length()>0))
		{
			newName=newName.toUpperCase();
			switch(newName.charAt(0))
			{
				case 'W':
				soc.setSourceCode(Affect.MSG_SPEAK);
				break;
				case 'M':
				soc.setSourceCode(Affect.MSG_HANDS);
				break;
				case 'S':
				soc.setSourceCode(Affect.MSG_NOISE);
				break;
				case 'L':
				soc.setSourceCode(Affect.MSG_NOISYMOVEMENT);
				break;
			}
		}
		else
			mob.session().println("(no change)");

		mob.session().rawPrintln("\n\rOthers see '"+soc.Third_party_sees()+"'.  Enter new.");
		newName=mob.session().prompt(": ","");
		if((newName!=null)&&(newName.length()>0))
			soc.setThird_party_sees(newName);
		else
			mob.session().println("(no change)");

		if(soc.othersCode()==Affect.MSG_OK_ACTION)
			soc.setOthersCode(Affect.MSG_HANDS);
		mob.session().rawPrintln("\n\rOthers affect type="+((soc.othersCode()==Affect.MSG_HANDS)?"HANDS":((soc.sourceCode()==Affect.MSG_OK_VISUAL)?"VISUAL ONLY":((soc.othersCode()==Affect.MSG_SPEAK)?"HEARING WORDS":((soc.othersCode()==Affect.MSG_NOISYMOVEMENT)?"SEEING MOVEMENT":"HEARING NOISE")))));
		newName=mob.session().choose("Change W)ords, M)ovement (w/noise), S)ound, V)isual, H)ands: ","WMSVH","");
		if((newName!=null)&&(newName.length()>0))
		{
			newName=newName.toUpperCase();
			switch(newName.charAt(0))
			{
				case 'H':
				soc.setOthersCode(Affect.MSG_HANDS);
				soc.setTargetCode(Affect.MSG_HANDS);
				break;
				case 'W':
				soc.setOthersCode(Affect.MSG_SPEAK);
				soc.setTargetCode(Affect.MSG_SPEAK);
				break;
				case 'M':
				soc.setOthersCode(Affect.MSG_NOISYMOVEMENT);
				soc.setTargetCode(Affect.MSG_NOISYMOVEMENT);
				break;
				case 'S':
				soc.setOthersCode(Affect.MSG_NOISE);
				soc.setTargetCode(Affect.MSG_NOISE);
				break;
				case 'V':
				soc.setOthersCode(Affect.MSG_OK_VISUAL);
				soc.setTargetCode(Affect.MSG_OK_VISUAL);
				break;
			}
		}
		else
			mob.session().println("(no change)");



		if(soc.name().indexOf("<T-NAME>")>=0)
		{
			mob.session().rawPrintln("\n\rTarget sees '"+soc.Target_sees()+"'.  Enter new.");
			newName=mob.session().prompt(": ","");
			if((newName!=null)&&(newName.length()>0))
				soc.setTarget_sees(newName);
			else
				mob.session().println("(no change)");


		if(soc.targetCode()==Affect.MSG_OK_ACTION)
			soc.setTargetCode(Affect.MSG_HANDS);
		mob.session().rawPrintln("\n\rTarget affect type="+((soc.othersCode()==Affect.MSG_HANDS)?"HANDS":((soc.sourceCode()==Affect.MSG_OK_VISUAL)?"VISUAL ONLY":((soc.othersCode()==Affect.MSG_SPEAK)?"HEARING WORDS":((soc.othersCode()==Affect.MSG_NOISYMOVEMENT)?"SEEING MOVEMENT":"HEARING NOISE")))));
		newName=mob.session().choose("Change W)ords, M)ovement (w/noise), S)ound, V)isual, H)ands: ","WMSVH","");
		if((newName!=null)&&(newName.length()>0))
		{
			newName=newName.toUpperCase();
			switch(newName.charAt(0))
			{
				case 'W':
				soc.setTargetCode(Affect.MSG_SPEAK);
				break;
				case 'M':
				soc.setTargetCode(Affect.MSG_NOISYMOVEMENT);
				break;
				case 'H':
				soc.setTargetCode(Affect.MSG_HANDS);
				break;
				case 'S':
				soc.setTargetCode(Affect.MSG_NOISE);
				break;
				case 'V':
				soc.setTargetCode(Affect.MSG_OK_VISUAL);
				break;
			}
		}
		else
			mob.session().println("(no change)");



			mob.session().rawPrintln("\n\rYou see when no target '"+soc.See_when_no_target()+"'.  Enter new.");
			newName=mob.session().prompt(": ","");
			if((newName!=null)&&(newName.length()>0))
				soc.setSee_when_no_target(newName);
			else
				mob.session().println("(no change)");
		}
		return true;
	}

	public static void modify(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.isMonster())
			return;

		if(commands.size()<3)
		{
			mob.session().rawPrintln("but fail to specify the proper fields.\n\rThe format is MODIFY SOCIAL [NAME] ([<T-NAME>], [SELF])\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		else
		if(commands.size()>3)
		{
			String therest=Util.combine(commands,3);
			if(!((therest.equalsIgnoreCase("<T-NAME>")||therest.equalsIgnoreCase("SELF"))))
			{
				mob.session().rawPrintln("but fail to specify the proper second parameter.\n\rThe format is MODIFY SOCIAL [NAME] ([<T-NAME>], [SELF])\n\r");
				mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				return;
			}
		}

		String stuff=Util.combine(commands,2).toUpperCase();
		Social soc2=Socials.FetchSocial(stuff);
		if(soc2==null)
		{
			mob.tell("but fail to specify an EXISTING SOCIAL!\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		if(modifySocial(mob,soc2))
		{
			soc2.setName(soc2.name().toUpperCase());
			if(Socials.FetchSocial(soc2.name())!=soc2)
			{
				mob.session().rawPrintln("That social already exists in another form (<T-NAME>, or SELF).  Try deleting the other one first!");
				return;
			}
			else
			{
				Socials.resetSocialsList();
				Socials.save();
			}
			Log.sysOut("SysopSocials",mob.ID()+" modified social "+soc2.name()+".");
		}
		mob.location().showHappens(Affect.MSG_OK_ACTION,"The happiness of all mankind has just increased!");
	}

	public static void destroy(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.session().rawPrintln("but fail to specify the proper fields.\n\rThe format is DESTROY SOCIAL [NAME] ([<T-NAME>], [SELF])\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		else
		if(commands.size()>3)
		{
			String therest=Util.combine(commands,3);
			if(!((therest.equalsIgnoreCase("<T-NAME>")||therest.equalsIgnoreCase("SELF"))))
			{
				mob.session().rawPrintln("but fail to specify the proper second parameter.\n\rThe format is DESTROY SOCIAL [NAME] ([<T-NAME>], [SELF])\n\r");
				mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				return;
			}
		}

		Social soc2=Socials.FetchSocial(Util.combine(commands,2).toUpperCase());
		if(soc2==null)
		{
			mob.tell("but fail to specify an EXISTING SOCIAL!\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		else
		{
			if(mob.session().confirm("Are you sure you want to delete that social (y/N)? ","N"))
			{
				Socials.remove(soc2.name());
				Socials.resetSocialsList();
				Socials.save();
				mob.location().showHappens(Affect.MSG_OK_ACTION,"The happiness of all mankind has just decreased!");
			}
			else
				mob.location().showHappens(Affect.MSG_OK_ACTION,"The happiness of all mankind has just increased!");
			Log.sysOut("SysopSocials",mob.ID()+" destroyed social "+soc2.name()+".");
		}

	}
}
