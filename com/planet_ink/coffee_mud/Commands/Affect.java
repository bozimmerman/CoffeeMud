package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Affect extends StdCommand
{
	private String[] access={"AFFECT","AFF","AF"};
	public String[] getAccessWords(){return access;}
	
	public String getAffects(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		int colnum=2;
		for(int a=0;a<mob.numAllEffects();a++)
		{
			Ability thisAffect=mob.fetchEffect(a);
			String disp=thisAffect.displayText();
			if((thisAffect!=null)&&(disp.length()>0))
			{
				if(((++colnum)>2)||(disp.length()>25)){ msg.append("\n\r"); colnum=0;}
				msg.append("^S"+Util.padRightPreserve(thisAffect.displayText(),25));
				if(disp.length()>25) colnum=99;
			}
		}
		if(msg.length()==0)
			msg.append("Nothing!");
		else
			msg.append("^?");
		msg.append("\n\r");
		return msg.toString();
	}
	
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		Session S=mob.session();
		if((commands!=null)&&(commands.size()>0)&&(!(commands.firstElement() instanceof String)))
		{
			if(commands.firstElement() instanceof MOB)
				S=((MOB)commands.firstElement()).session();
			else
			if(commands.firstElement() instanceof StringBuffer)
			{
				((StringBuffer)commands.firstElement()).append(getAffects(mob));
				return false;
			}
			else
			if(commands.firstElement() instanceof Vector)
			{
				((Vector)commands.firstElement()).addElement(getAffects(mob));
				return false;
			}
			else
			{
				commands.clear();
				commands.addElement(getAffects(mob));
				return false;
			}
		}

		if(S!=null)
		{
			if(S==mob.session())
				S.colorOnlyPrint("\n\r^!You are affected by:^? ");
			S.colorOnlyPrintln(getAffects(mob));
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
