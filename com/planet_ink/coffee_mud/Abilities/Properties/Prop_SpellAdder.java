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
public class Prop_SpellAdder extends Property
{
	public String ID() { return "Prop_SpellAdder"; }
	public String name(){ return "Casting spells on oneself";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_MOBS;}
    protected MOB trickMOB=null;
    protected Environmental lastMOB=null;
    protected boolean processing=false;
    protected String maskString="";
    protected String parmString="";
	protected Hashtable spellH=null;
	protected Vector spellV=null;
    protected Vector mask=new Vector();
    
 	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		spellV=null;
		spellH=null;
        mask=new Vector();
        lastMOB=null;
        int maskindex=newText.toUpperCase().indexOf("MASK=");
        parmString=newText;
        if(maskindex>0)
        {
            maskString=newText.substring(maskindex+5).trim();
            if(maskString.length()>0)
                Util.addToVector(MUDZapper.zapperCompile(maskString),mask);
            parmString=newText.substring(0,maskindex).trim();
        }
	}

	public Vector getMySpellsV()
	{
        if(spellV!=null) return spellV;
		spellV=new Vector();
		String names=parmString;
		int del=names.indexOf(";");
		while(del>=0)
		{
			String thisOne=names.substring(0,del).trim();
			String parm="";
			if(thisOne.endsWith(")"))
			{
				int x=thisOne.indexOf("(");
				if(x>0)
				{
					parm=thisOne.substring(x+1,thisOne.length()-1);
					thisOne=thisOne.substring(0,x).trim();
				}
			}

			Ability A=CMClass.getAbility(thisOne);
			if((A!=null)&&(!CMAble.classOnly("Archon",A.ID())))
			{
				A=(Ability)A.copyOf();
				A.setMiscText(parm);
                spellV.addElement(A);
			}
			names=names.substring(del+1);
			del=names.indexOf(";");
		}
		String parm="";
		if(names.endsWith(")"))
		{
			int x=names.indexOf("(");
			if(x>0)
			{
				parm=names.substring(x+1,names.length()-1);
				names=names.substring(0,x).trim();
			}
		}
		Ability A=CMClass.getAbility(names);
		if((A!=null)&&(!CMAble.classOnly("Archon",A.ID())))
		{
			A=(Ability)A.copyOf();
			A.setMiscText(parm);
            spellV.addElement(A);
		}
		return spellV;
	}

	public boolean didHappen(int defaultPct)
	{
		int x=parmString.indexOf("%");
		if(x<0)
		{
			if(Dice.rollPercentage()<=defaultPct)
				return true;
			return false;
		}
		int mul=1;
		int tot=0;
		while((--x)>=0)
		{
			if(Character.isDigit(parmString.charAt(x)))
				tot+=Util.s_int(""+parmString.charAt(x))*mul;
			else
				x=-1;
			mul=mul*10;
		}
		if(Dice.rollPercentage()<=tot)
			return true;
		return false;
	}
    
	public Hashtable getMySpellsH()
	{
        if(spellH!=null) return spellH;
        spellH=new Hashtable();
		Vector V=getMySpellsV();
		for(int v=0;v<V.size();v++)
            spellH.put(((Ability)V.elementAt(v)).ID(),((Ability)V.elementAt(v)).ID());
		return spellH;
	}


	public MOB qualifiedMOB(Environmental target)
	{
		if(target instanceof MOB)
			return (MOB)target;

		if((target instanceof Item)&&(((Item)target).owner()!=null)&&(((Item)target).owner() instanceof MOB))
			return (MOB)((Item)target).owner();
        if(trickMOB==null)
            trickMOB=CMClass.getMOB("StdMOB");
        Room R=CoffeeUtensils.roomLocation(target);
        trickMOB.setLocation((R==null)?CMClass.getLocale("StdRoom"):R);
		return trickMOB;
	}

	public boolean addMeIfNeccessary(Environmental source, Environmental target)
	{
		Vector V=getMySpellsV();
        if((target==null)
        ||(V.size()==0)
        ||((mask.size()>0)
            &&(!MUDZapper.zapperCheckReal(mask,qualifiedMOB(source)))))
            return false;
        
		for(int v=0;v<V.size();v++)
		{
			Ability A=(Ability)V.elementAt(v);
			Ability EA=target.fetchEffect(A.ID());
			if((EA==null)&&(didHappen(100)))
            {
				String t=A.text();
				A=(Ability)A.copyOf();
				Vector V2=new Vector();
				if(t.length()>0)
				{
					int x=t.indexOf("/");
					if(x<0)
					{
						V2=Util.parse(t);
						A.setMiscText("");
					}
					else
					{
						V2=Util.parse(t.substring(0,x));
						A.setMiscText(t.substring(x+1));
					}
				}
				A.invoke(qualifiedMOB(source),V2,target,true,(affected!=null)?affected.envStats().level():0);
				EA=target.fetchEffect(A.ID());
                lastMOB=target;
			}
			if(EA!=null)
				EA.makeLongLasting();
		}
        return true;
	}

    public String accountForYourself()
    { return spellAccountingsWithMask("Casts "," on the first one who enters.");}
    
    public void removeMyAffectsFromLastMOB()
    {
        removeMyAffectsFrom(lastMOB);
        lastMOB=null;
    }
    
	public void removeMyAffectsFrom(Environmental E)
	{
        if(E==null)return;
        
		Hashtable h=getMySpellsH();
		int x=0;
		while(x<E.numEffects())
		{
			Ability thisAffect=E.fetchEffect(x);
			if(thisAffect!=null)
			{
				String ID=(String)h.get(thisAffect.ID());
				if((ID!=null)
                &&(thisAffect.invoker()==qualifiedMOB(E)))
				{
					thisAffect.unInvoke();
					x=-1;
				}
			}
			x++;
		}
	}

	public void executeMsg(Environmental host, CMMsg msg)
	{
		if((affected instanceof Room)||(affected instanceof Area))
		{
			if((msg.targetMinor()==CMMsg.TYP_LEAVE)
			||(msg.sourceMinor()==CMMsg.TYP_RECALL))
				removeMyAffectsFrom(msg.source());
			if(msg.targetMinor()==CMMsg.TYP_ENTER)
				addMeIfNeccessary(msg.source(),msg.source());
		}
	}

	public void affectEnvStats(Environmental host, EnvStats affectableStats)
	{
		if(processing) return;
		if((affected instanceof MOB)
		   ||(affected instanceof Item))
		{
			processing=true;
			if((lastMOB!=null)
			 &&(host!=lastMOB))
				removeMyAffectsFrom(lastMOB);

			if((lastMOB==null)&&(host!=null))
				addMeIfNeccessary(host,host);
			processing=false;
		}
	}
    
    public String spellAccountingsWithMask(String pre, String post)
    {
        Vector spellList=getMySpellsV();
        String id="";
        for(int v=0;v<spellList.size();v++)
        {
            Ability A=(Ability)spellList.elementAt(v);
            if(spellList.size()==1)
                id+=A.name();
            else
            if(v==(spellList.size()-1))
                id+="and "+A.name();
            else
                id+=A.name()+", ";
        }
        if(spellList.size()>0)
            id=pre+id+post;
        if(maskString.length()>0)
            id+="  Restrictions: "+MUDZapper.zapperDesc(maskString);
        return id;
    }
}
