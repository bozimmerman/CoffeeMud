package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import java.util.*;

/**
 * <p>Title: False Realities Flavored CoffeeMUD</p>
 * <p>Description: The False Realities Version of CoffeeMUD</p>
 * <p>Copyright: Copyright (c) 2004 Jeremy Vyska</p>
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * <p>you may not use this file except in compliance with the License.
 * <p>You may obtain a copy of the License at
 *
 * <p>       http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * <p>distributed under the License is distributed on an "AS IS" BASIS,
 * <p>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>See the License for the specific language governing permissions and
 * <p>limitations under the License.
 * <p>Company: http://www.falserealities.com</p>
 * @author FR - Jeremy Vyska; CM - Bo Zimmerman
 * @version 1.0.0.0
 */
public class StdPowder extends StdItem implements MagicDust {
	public String ID(){	return "StdPowder";}

	public StdPowder()
	{
		super();

		setName("a pile of powder");
		baseEnvStats.setWeight(1);
		setDisplayText("A small pile of powder sits here.");
		setDescription("A small pile of powder.");
		secretIdentity="This is a pile of inert materials.";
		baseGoldValue=0;
		material=EnvResource.RESOURCE_ASH;
		recoverEnvStats();
	}
	
	public void spreadIfAble(MOB mob, Environmental target)
	{
        Vector spells = getSpells();
        if (spells.size() > 0)
            for (int i = 0; i < spells.size(); i++) 
			{
                Ability thisOne = (Ability) ( (Ability) spells.elementAt(i)).copyOf();
				if(thisOne.canTarget(target))
				{
					if((malicious(this))||(!(target instanceof MOB)))
						thisOne.invoke(mob, target, true, envStats().level());
					else
						thisOne.invoke((MOB)target,(MOB)target, true, envStats().level());
				}
            }
		destroy();
	}


// That which makes Powders work.  They're an item that when successfully dusted on a target, are 'cast' on the target
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
        if(msg.sourceMinor()==CMMsg.TYP_THROW ) 
		{
            if(msg.tool()==this) 
				spreadIfAble(msg.source(),msg.target());
            else
                super.executeMsg(myHost,msg);
        }
        else
            super.executeMsg(myHost,msg);
    }

	public String getSpellList()
	{ return miscText;}
	public void setSpellList(String list){miscText=list;}

    public boolean malicious(SpellHolder me) {
        Vector spells=getSpells();
        for(Enumeration e=spells.elements();e.hasMoreElements();) {
            Ability checking=(Ability)e.nextElement();
            if(checking.quality()==Ability.MALICIOUS)
                return true;
        }
        return false;
    }
	public Vector getSpells()
	{
		String names=getSpellList();

		Vector theSpells=new Vector();
		int del=names.indexOf(";");
		while(del>=0)
		{
			String thisOne=names.substring(0,del);
			if((thisOne.length()>0)&&(!thisOne.equals(";")))
			{
				Ability A=CMClass.getAbility(thisOne);
				if((A!=null)&&(!CMAble.classOnly("Archon",A.ID())))
				{
					A=(Ability)A.copyOf();
					theSpells.addElement(A);
				}
			}
			names=names.substring(del+1);
			del=names.indexOf(";");
		}
		if((names.length()>0)&&(!names.equals(";")))
		{
			Ability A=CMClass.getAbility(names);
			if(A!=null)
			{
				A=(Ability)A.copyOf();
				theSpells.addElement(A);
			}
		}
		recoverEnvStats();
		return theSpells;
	}

	public String secretIdentity()
	{
        return description()+"\n\r"+secretIdentity();
	}

}
