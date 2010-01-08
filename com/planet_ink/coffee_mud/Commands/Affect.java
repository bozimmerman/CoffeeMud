package com.planet_ink.coffee_mud.Commands;
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

/*
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Affect extends StdCommand
{
	private String[] access={"AFFECT","AFF","AF"};
	public String[] getAccessWords(){return access;}

	public String getAffects(Session S, Environmental E, boolean xtra)
	{
		StringBuffer msg=new StringBuffer("");
        int NUM_COLS=xtra?1:2;
        int COL_LEN=xtra?38:25;
		int colnum=NUM_COLS;
        MOB mob=(S!=null)?S.mob():null;
		for(int a=0;a<((E instanceof MOB)?((MOB)E).numAllEffects():E.numEffects());a++)
		{
			Ability thisAffect=E.fetchEffect(a);
			String disp=thisAffect.displayText();
			if((thisAffect!=null)&&(disp.length()>0))
			{
                if(xtra)
                {
                    disp+="^N: TR=";
                    long tr=thisAffect.expirationDate() / Tickable.TIME_TICK;
                    if(tr>=(Integer.MAX_VALUE/2))
                        disp+="~";
                    else
                        disp+=tr;
                    
                    disp+=", BY="+((thisAffect.invoker()==null)?"N/A":thisAffect.invoker().Name());
                }
                String[] disps={disp};
                if(disp.length()>(COL_LEN*NUM_COLS))
                {
                    String s=CMLib.coffeeFilter().fullOutFilter(S,mob,null,null,null,disp,true);
                    s=CMStrings.replaceAll(s,"\r","");
                    Vector V=CMParms.parseAny(s,"\n",true);
                    disps=new String[V.size()];
                    for(int d=0;d<V.size();d++)
                        disps[d]=(String)V.elementAt(d);
                    colnum=NUM_COLS;
                }
                for(int d=0;d<disps.length;d++) {
                    disp=disps[d];
    				if(((++colnum)>NUM_COLS)||(disp.length()>COL_LEN)){ msg.append("\n\r"); colnum=0;}
    			    msg.append("^S"+CMStrings.padRightPreserve("^<HELPNAME NAME='"+thisAffect.Name()+"'^>"+disp+"^</HELPNAME^>",COL_LEN));
    				if(disp.length()>COL_LEN) colnum=99;
                }
			}
		}
		msg.append("^N\n\r");
		return msg.toString();
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
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
				((StringBuffer)commands.firstElement()).append(getAffects(S,mob,false));
				return false;
			}
			else
			if(commands.firstElement() instanceof Vector)
			{
				((Vector)commands.firstElement()).addElement(getAffects(S,mob,false));
				return false;
			}
			else
			{
				commands.clear();
				commands.addElement(getAffects(S,mob,false));
				return false;
			}
		}

		if(S!=null)
		{
		    if(CMSecurity.isAllowed(mob, mob.location(),"CMDMOBS"))
		    {
	            String name=CMParms.combine(commands,1);
	            if(name.length()>0)
	            {
	                Environmental E=null;
	                if((name.equalsIgnoreCase("here")||(name.equalsIgnoreCase("room"))))
	                    E=CMLib.map().roomLocation(mob);
	                else
                    if((name.equalsIgnoreCase("area")||(name.equalsIgnoreCase("zone"))))
                        E=CMLib.map().areaLocation(mob);
                    else
    	                E=mob.location().fetchFromMOBRoomFavorsItems(mob,null,name,Wearable.FILTER_ANY);
	                if(E==null)
	                    S.colorOnlyPrint("You don't see "+name+" here.\n\r^N");
	                else
	                {
	                    if(S==mob.session())
	                        S.colorOnlyPrint(" \n\r^!"+E.name()+" is affected by: ^?");
	                    String msg=getAffects(S,E,true);
	                    if(msg.length()<5)
	                        S.colorOnlyPrintln("Nothing!\n\r^N");
	                    else
	                        S.colorOnlyPrintln(msg);
	                }
	                return false;
	            }
		        
		    }
			if(S==mob.session())
				S.colorOnlyPrint(" \n\r^!You are affected by: ^?");
            String msg=getAffects(S,mob,CMath.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS));
            if(msg.length()<5)
                S.colorOnlyPrintln("Nothing!\n\r^N");
            else
                S.colorOnlyPrintln(msg);
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
