package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Prop_Smell extends Property
{
	public String ID() { return "Prop_Smell"; }
	public String name(){ return "A Smell";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS;}
	protected DVector smells=null;
	protected final static int FLAG_EMOTE=512;
	protected final static int FLAG_BROADCAST=1024;
	protected boolean lastWasBroadcast=false;
	
	public String accountForYourself(){ return "";	}
	public void setMiscText(String newStr){
	    if(newStr.startsWith("+"))
	    {
	        if(text().indexOf(newStr.substring(1).trim())>=0)
	            return;
	        super.setMiscText(text()+";"+newStr.substring(1).trim());
	        smells=null;
	    }
	    else
	    if(newStr.startsWith("-"))
	    {
	        int x=text().indexOf(newStr.substring(1).trim());
	        if(x>=0)
	        {
	            int len=newStr.substring(1).trim().length();
		        super.setMiscText(text().substring(0,x)+text().substring(x+len));
		        smells=null;
	        }
	        else
	            return;
	    }
	    else
	    {
		    super.setMiscText(newStr);
		    smells=null;
	    }
	}
	public DVector getSmells()
	{
	    if(smells!=null) return smells;
	    Vector allsmells=Util.parseSemicolons(text(),true);
	    smells=new DVector(3);
	    for(int i=0;i<allsmells.size();i++)
	    {
	        String smell=(String)allsmells.elementAt(i);
	        if(smell.length()>0)
	        {
	            int pct=100;
	            int ticks=-1;
	            Vector parsedSmell=Util.parse(smell);
	            for(int ii=parsedSmell.size()-1;ii>=0;ii--)
	            {
	                String s=((String)parsedSmell.elementAt(ii)).toUpperCase();
	                if(s.startsWith("TICKS="))
	                {
	                    ticks=Util.s_int(s.substring(6).trim());
	                    parsedSmell.removeElementAt(ii);
	                }
	                if(s.startsWith("CHANCE="))
	                {
	                    pct=(pct&(FLAG_BROADCAST+FLAG_EMOTE))+Util.s_int(s.substring(5).trim());
	                    parsedSmell.removeElementAt(ii);
	                }
	                if(s.equals("EMOTE"))
	                {
	                    pct=pct&FLAG_EMOTE;
	                    parsedSmell.removeElementAt(ii);
	                }
	                if(s.equals("BROADCAST"))
	                {
	                    pct=pct&FLAG_EMOTE;
	                    parsedSmell.removeElementAt(ii);
	                }
	            }
	            String finalSmell=Util.combine(parsedSmell,0).trim();
	            if(finalSmell.length()>0)
	                smells.addElement(finalSmell,new Integer(pct),new Integer(ticks));
	        }
	    }
	    return smells;
	}

	public String selectSmell(boolean emoteOnly)
	{
	    lastWasBroadcast=false;
	    getSmells();
	    if((smells!=null)&&(smells.size()>0))
	    {
	        int total=0;
	        for(int i=0;i<smells.size();i++)
	        {
	            int pct=((Integer)smells.elementAt(i,2)).intValue();
	            if((!emoteOnly)||(Util.bset(pct,FLAG_EMOTE)))
	                total+=pct&511;
	        }
	        if(total==0) return "";
	        int draw=Dice.roll(1,total,0);
	        for(int i=0;i<smells.size();i++)
	        {
	            int pct=((Integer)smells.elementAt(i,2)).intValue();
	            if((!emoteOnly)||(Util.bset(pct,FLAG_EMOTE)))
	            {
	                draw-=pct&511;
	                if(draw<=0)
	                {
	            	    lastWasBroadcast=Util.bset(pct,FLAG_BROADCAST);
	                    return (String)smells.elementAt(i,1);
	                }
	            }
	        }
	    }
	    return "";
	}
	
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.amITarget(affected))
		&&(msg.targetMinor()==CMMsg.TYP_SNIFF)
		&&(Sense.canSmell(msg.source())))
			msg.source().tell(msg.source(),affected,null,selectSmell(false));
	}
	
	public void emoteHere(Room room, MOB emoter, String str)
	{
		FullMsg msg=new FullMsg(emoter,null,CMMsg.MSG_EMOTE,str);
		if(room.okMessage(emoter,msg))
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB M=room.fetchInhabitant(i);
			if((M!=null)&&(!M.isMonster())&&(Sense.canSmell(M))) 
		        M.executeMsg(M,msg);
		}
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
	    if((affected instanceof MOB)&&(Dice.rollPercentage()<=20))
	    {
	        String emote=selectSmell(true);
	        if((emote!=null)&&(emote.length()>0))
	        {
		        Room room=CoffeeUtensils.roomLocation(affected);
		        if(room!=null)
		        {
		            emoteHere(room,(MOB)affected,emote);
		            if(lastWasBroadcast)
		            {
		                MOB emoter=CMClass.getMOB("StdMOB");
						for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
						{
							Room R=room.getRoomInDir(d);
							Exit E=room.getExitInDir(d);
							if((R!=null)&&(E!=null)&&(E.isOpen()))
							{
							    emoter.setLocation(R);
								emoter.setName("something "+Directions.getInDirectionName(Directions.getOpDirectionCode(d)));
								emoteHere(R,emoter,emote);
							}
						}
		            }
		        }
	        }
		    DVector sm=getSmells();
		    boolean redo=false;
		    for(int i=sm.size()-1;i>=0;i--)
		    {
		        if(((Integer)sm.elementAt(i,3)).intValue()>0)
		        {
		            Integer I=new Integer(((Integer)smells.elementAt(i,3)).intValue()-1);
		            if(I.intValue()>0)
		            {
		                String smell=(String)sm.elementAt(i,1);
		                Integer pct=(Integer)sm.elementAt(i,2);
		                sm.addElement(smell,pct,I);
		            }
	                sm.removeElementAt(i);
	                if(I.intValue()<=0) redo=true;
		        }
		    }
		    if(redo)
		    {
			    StringBuffer newText=new StringBuffer("");
			    for(int i=0;i<sm.size();i++)
			    {
		            String smell=(String)sm.elementAt(i,1);
			        Integer pct=(Integer)sm.elementAt(i,2);
		            Integer ticks=(Integer)sm.elementAt(i,3);
		            if(ticks.intValue()>0)
		                newText.append("TICKS="+ticks+" ");
		            if(Util.bset(pct.intValue(),FLAG_EMOTE))
				        newText.append("EMOTE ");
		            if(Util.bset(pct.intValue(),FLAG_BROADCAST))
				        newText.append("BROADCAST ");
		            if((pct.intValue()&511)!=100)
				        newText.append("CHANCE="+(pct.intValue()&511)+" ");
		            newText.append(smell+";");
			    }
			    if(newText.length()==0)
			        affected.delEffect(this);
			    else
			        setMiscText(newText.toString());
		    }
	    }
	    return super.tick(ticking,tickID);
	}
}

