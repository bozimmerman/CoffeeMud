package com.planet_ink.coffee_mud.Abilities.Properties;
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
public class Prop_CommonTwister extends Property
{
	public String ID() { return "Prop_CommonTwister"; }
	public String name(){ return "Common Twister";}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_ITEMS|Ability.CAN_MOBS;}
    protected DVector changes=new DVector(3);

	public String accountForYourself()
	{ return "Twists around what the gathering common skills gives you.";	}

	public void setMiscText(String text)
	{
		super.setMiscText(text);
		changes.clear();
		Vector V=CMParms.parseSemicolons(text,true);
		for(int v=0;v<V.size();v++)
		{
			String s=(String)V.elementAt(v);
			String skill=CMParms.getParmStr(s,"SKILL","");
			String mask=CMParms.getParmStr(s,"MASK","");
			if((skill.length()>0)&&(mask.length()>0))
				changes.addElement(skill,mask,s);
		}

	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		&&(msg.tool() instanceof Ability)
		&&(msg.target()!=null)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)
		&&((affected instanceof Room)||(affected instanceof Exit)||(affected instanceof Area)
		   ||((affected instanceof Item)&&(msg.source().isMine(affected)))
		   ||((affected instanceof MOB)&&(msg.source()==affected))))
		{
			Vector poss=new Vector();
			int randomResource=CMLib.dice().roll(1,RawMaterial.CODES.TOTAL()-1,0);
			if(text().length()==0)
			{
				Item I=CMLib.materials().makeItemResource(randomResource);
				msg.target().setName(I.Name());
				msg.target().setDisplayText(I.displayText());
				if(msg.target() instanceof Item)
					((Item)msg.target()).setMaterial(I.material());
			}
			else
			for(int v=0;v<changes.size();v++)
			{
				if(((String)changes.elementAt(v,1)).equals("*")
				||(((String)changes.elementAt(v,1)).equalsIgnoreCase(msg.tool().ID())))
				{
					String two=(String)changes.elementAt(v,2);
					if(two.equals("*")
					||(CMLib.english().containsString(msg.target().name(),two)))
						poss.addElement(changes.elementAt(v,3));
				}
			}
			if(poss.size()==0) return true;
			String var=(String)poss.elementAt(CMLib.dice().roll(1,poss.size(),-1));
			String newname=CMParms.getParmStr(var,"NAME","");
			String newdisp=CMParms.getParmStr(var,"DISPLAY","");
			String newmat=CMParms.getParmStr(var,"MATERIAL","");

			if(newname.length()>0)
			{
				if(newname.equals("*"))
				{
					Item I=CMLib.materials().makeItemResource(randomResource);
					msg.target().setName(I.Name());
				}
				else
					msg.target().setName(newname);
			}
			if(newdisp.length()>0)
			{
				if(newdisp.equals("*"))
				{
					Item I=CMLib.materials().makeItemResource(randomResource);
					msg.target().setDisplayText(I.displayText());
				}
				else
					msg.target().setDisplayText(newdisp);
			}
			if((newmat.length()>0)&&(msg.target() instanceof Item))
			{
				String oldMatName=RawMaterial.CODES.NAME(((Item)msg.target()).material()).toLowerCase();
				int newMatCode=-1;
				if(newmat.equals("*"))
					newMatCode=randomResource;
				else
				{
					newMatCode=CMLib.materials().getResourceCode(newmat,false);
					if(newMatCode<0)
					{
						newMatCode=CMLib.materials().getMaterialCode(newmat,false);
						if(newMatCode>0) newMatCode=CMLib.materials().getRandomResourceOfMaterial(newMatCode);
					}
					if(newMatCode>=0)
					{
						((Item)msg.target()).setMaterial(newMatCode);
						String newMatName=RawMaterial.CODES.NAME(newMatCode).toLowerCase();
						msg.target().setName(CMStrings.replaceAll(msg.target().name(),oldMatName,newMatName));
						msg.target().setDisplayText(CMStrings.replaceAll(msg.target().name(),oldMatName,newMatName));
						msg.target().setName(CMStrings.replaceAll(msg.target().name(),CMStrings.capitalizeAndLower(oldMatName),CMStrings.capitalizeAndLower(newMatName)));
						msg.target().setDisplayText(CMStrings.replaceAll(msg.target().name(),CMStrings.capitalizeAndLower(oldMatName),CMStrings.capitalizeAndLower(newMatName)));
						msg.target().setName(CMStrings.replaceAll(msg.target().name(),oldMatName.toUpperCase(),newMatName.toUpperCase()));
						msg.target().setDisplayText(CMStrings.replaceAll(msg.target().name(),oldMatName.toUpperCase(),newMatName.toUpperCase()));
					}
				}
			}
		}
		return super.okMessage(myHost,msg);
	}
}
