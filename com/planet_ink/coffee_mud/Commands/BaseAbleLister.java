package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class BaseAbleLister extends StdCommand
{
	public static int parseOutLevel(Vector commands)
	{
		if((commands.size()>1)
		&&(commands.lastElement() instanceof String)
		&&(Util.isNumber((String)commands.lastElement())))
		{
			int x=Util.s_int((String)commands.lastElement());
			commands.removeElementAt(commands.size()-1);
			return x;
		}
		return -1;
	}
	public static StringBuffer getAbilities(MOB able, 
											int ofType, 
											int ofDomain, 
											boolean addQualLine,
											int maxLevel)
	{
		Vector V=new Vector();
		int mask=Ability.ALL_CODES;
		if(ofDomain>=0)
		{
			mask=Ability.ALL_CODES|Ability.ALL_DOMAINS;
			ofType=ofType|ofDomain;
		}
		V.addElement(new Integer(ofType));
		return getAbilities(able,V,mask,addQualLine,maxLevel);
	}
	public static StringBuffer getAbilities(MOB able, 
											Vector ofTypes, 
											int mask, 
											boolean addQualLine,
											int maxLevel)
	{
		int highestLevel=0;
		int lowestLevel=able.envStats().level()+1;
		StringBuffer msg=new StringBuffer("");
		for(int a=0;a<able.numAbilities();a++)
		{
			Ability thisAbility=able.fetchAbility(a);
			int level=CMAble.qualifyingLevel(able,thisAbility);
			if(level<0) level=0;
			if((thisAbility!=null)
			&&(level>highestLevel)
			&&(level<lowestLevel)
			&&(ofTypes.contains(new Integer(thisAbility.classificationCode()&mask))))
				highestLevel=level;
		}
		if((maxLevel>=0)&&(maxLevel<highestLevel))
			highestLevel=maxLevel;
		for(int l=0;l<=highestLevel;l++)
		{
			StringBuffer thisLine=new StringBuffer("");
			int col=0;
			for(int a=0;a<able.numAbilities();a++)
			{
				Ability thisAbility=able.fetchAbility(a);
				int level=CMAble.qualifyingLevel(able,thisAbility);
				if(level<0) level=0;
				if((thisAbility!=null)
				&&(level==l)
				&&(ofTypes.contains(new Integer(thisAbility.classificationCode()&mask))))
				{
					if(thisLine.length()==0)
						thisLine.append("\n\rLevel ^!"+l+"^?:\n\r");
					if((++col)>3)
					{
						thisLine.append("\n\r");
						col=1;
					}
					thisLine.append("^N[^H"+Util.padRight(Integer.toString(thisAbility.profficiency()),3)+"%^?]^N "+Util.padRight(thisAbility.name(),(col==3)?18:19));
				}
			}
			if(thisLine.length()>0)
				msg.append(thisLine);
		}
		if(msg.length()==0)
			msg.append("^!None!^?");
		else
		if(addQualLine)
			msg.append("\n\r\n\rUse QUALIFY to see additional skills you can GAIN.");
		return msg;
	}
}
