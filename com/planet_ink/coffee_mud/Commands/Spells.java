package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spells extends BaseAbleLister
{
	public Spells(){}

	private String[] access={"SPELLS","SP"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String qual=Util.combine(commands,1).toUpperCase();
		int domain=-1;
		String domainName="Arcane";
		if(qual.length()>0)
		for(int i=1;i<Ability.DOMAIN_DESCS.length;i++)
			if(Ability.DOMAIN_DESCS[i].startsWith(qual))
			{ domain=i<<5; break;}
			else
			if((Ability.DOMAIN_DESCS[i].indexOf("/")>=0)
			&&(Ability.DOMAIN_DESCS[i].substring(Ability.DOMAIN_DESCS[i].indexOf("/")+1).startsWith(qual)))
			{ domain=i<<5; break;}
		if(domain>0)
			domainName=Ability.DOMAIN_DESCS[domain>>5].toLowerCase();
		StringBuffer spells=new StringBuffer("");
		if((domain<0)&&(qual.length()>0))
		{
			spells.append("\n\rValid schools are: ");
			for(int i=1;i<Ability.DOMAIN_DESCS.length;i++)
				spells.append(Ability.DOMAIN_DESCS[i]+" ");

		}
		else
			spells.append("\n\r^HYour "+domainName+" spells:^? "+getAbilities(mob,Ability.SPELL,domain,true));
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(spells.toString()+"\n\r");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
