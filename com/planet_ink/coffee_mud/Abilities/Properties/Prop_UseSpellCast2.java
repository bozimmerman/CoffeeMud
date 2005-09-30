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
public class Prop_UseSpellCast2 extends Property
{
	private boolean processing=false;
	public String ID() { return "Prop_UseSpellCast2"; }
	public String name(){ return "Casting spells when used";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected Hashtable spellH=null;
	protected Vector spellV=null;
    private Vector mask=new Vector();
    
	public Vector getMySpellsV()
	{
		if(spellV!=null) return spellV;
		spellV=Prop_SpellAdder.getMySpellsV(this);
		return spellV;
	}
	public Hashtable getMySpellsH()
	{
		if(spellH!=null) return spellH;
		spellH=Prop_SpellAdder.getMySpellsH(this);
		return spellH;
	}

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		spellV=null;
		spellH=null;
        mask.clear();
        Prop_HaveAdjuster.buildMask(newText,mask);
	}


	public void addMeIfNeccessary(MOB sourceMOB, MOB newMOB)
	{
		Vector V=getMySpellsV();
		for(int v=0;v<V.size();v++)
		{
			Ability A=(Ability)V.elementAt(v);
			Ability EA=newMOB.fetchEffect(A.ID());
			if((EA==null)&&(Prop_SpellAdder.didHappen(100,this)))
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
                if((mask.size()==0)||(MUDZapper.zapperCheckReal(mask,sourceMOB)))
    				A.invoke(sourceMOB,V2,newMOB,true,(affected!=null)?affected.envStats().level():0);
			}
		}
	}

    public String accountForYourself()
    { return Prop_FightSpellCast.spellAccountingsWithMask(getMySpellsV(),"Casts "," when used.",text());}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if(processing) return;
		processing=true;

		if(affected==null) return;
		Item myItem=(Item)affected;
		if(myItem.owner()==null) return;
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_DRINK:
			if((myItem instanceof Drink)
			&&(msg.amITarget(myItem)))
				addMeIfNeccessary(msg.source(),msg.source());
			break;
		case CMMsg.TYP_EAT:
			if((myItem instanceof Food)
			&&(msg.amITarget(myItem)))
				addMeIfNeccessary(msg.source(),msg.source());
			break;
		case CMMsg.TYP_GET:
			if((!(myItem instanceof Drink))
			  &&(!(myItem instanceof Food))
			  &&(msg.amITarget(myItem)))
				addMeIfNeccessary(msg.source(),msg.source());
			break;
		}
		processing=false;
	}
}
