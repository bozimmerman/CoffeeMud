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
public class Prop_CommonTwister extends Property
{
	public String ID() { return "Prop_CommonTwister"; }
	public String name(){ return "Common Twister";}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_ITEMS|Ability.CAN_MOBS;}
	private DVector changes=new DVector(3);

	public String accountForYourself()
	{ return "Twists around what the gathering common skills gives you.";	}

	public void setMiscText(String text)
	{
		super.setMiscText(text);
		changes.clear();
		Vector V=Util.parseSemicolons(text,true);
		for(int v=0;v<V.size();v++)
		{
			String s=(String)V.elementAt(v);
			String skill=Util.getParmStr(s,"SKILL","");
			String mask=Util.getParmStr(s,"MASK","");
			if((skill.length()>0)&&(mask.length()>0))
				changes.addElement(skill,mask,s);
		}

	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		&&(msg.tool() instanceof Ability)
		&&(msg.target()!=null)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES)==Ability.COMMON_SKILL)
		&&((affected instanceof Room)||(affected instanceof Exit)||(affected instanceof Area)
		   ||((affected instanceof Item)&&(msg.source().isMine(affected)))
		   ||((affected instanceof MOB)&&(msg.source()==affected))))
		{
			Vector poss=new Vector();
			int randomResource=Dice.roll(1,EnvResource.RESOURCE_DESCS.length-1,0);
			if(text().length()==0)
			{
				Item I=CoffeeUtensils.makeItemResource(randomResource);
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
					||(EnglishParser.containsString(msg.target().name(),two)))
						poss.addElement(changes.elementAt(v,3));
				}
			}
			if(poss.size()==0) return true;
			String var=(String)poss.elementAt(Dice.roll(1,poss.size(),-1));
			String newname=Util.getParmStr(var,"NAME","");
			String newdisp=Util.getParmStr(var,"DISPLAY","");
			String newmat=Util.getParmStr(var,"MATERIAL","");

			if(newname.length()>0)
			{
				if(newname.equals("*"))
				{
					Item I=CoffeeUtensils.makeItemResource(randomResource);
					msg.target().setName(I.Name());
				}
				else
					msg.target().setName(newname);
			}
			if(newdisp.length()>0)
			{
				if(newdisp.equals("*"))
				{
					Item I=CoffeeUtensils.makeItemResource(randomResource);
					msg.target().setDisplayText(I.displayText());
				}
				else
					msg.target().setDisplayText(newdisp);
			}
			if((newmat.length()>0)&&(msg.target() instanceof Item))
			{
				String oldMatName=EnvResource.RESOURCE_DESCS[((Item)msg.target()).material()&EnvResource.RESOURCE_MASK].toLowerCase();
				int newMatCode=-1;
				if(newmat.equals("*"))
					newMatCode=randomResource;
				else
				{
					newMatCode=CoffeeUtensils.getResourceCode(newmat);
					if(newMatCode<0)
					{
						newMatCode=CoffeeUtensils.getMaterialCode(newmat);
						if(newMatCode>0) newMatCode=CoffeeUtensils.getRandomResourceOfMaterial(newMatCode);
					}
					if(newMatCode>=0)
					{
						((Item)msg.target()).setMaterial(newMatCode);
						String newMatName=EnvResource.RESOURCE_DESCS[newMatCode&EnvResource.RESOURCE_MASK].toLowerCase();
						msg.target().setName(Util.replaceAll(msg.target().name(),oldMatName,newMatName));
						msg.target().setDisplayText(Util.replaceAll(msg.target().name(),oldMatName,newMatName));
						msg.target().setName(Util.replaceAll(msg.target().name(),Util.capitalizeAndLower(oldMatName),Util.capitalizeAndLower(newMatName)));
						msg.target().setDisplayText(Util.replaceAll(msg.target().name(),Util.capitalizeAndLower(oldMatName),Util.capitalizeAndLower(newMatName)));
						msg.target().setName(Util.replaceAll(msg.target().name(),oldMatName.toUpperCase(),newMatName.toUpperCase()));
						msg.target().setDisplayText(Util.replaceAll(msg.target().name(),oldMatName.toUpperCase(),newMatName.toUpperCase()));
					}
				}
			}
		}
		return super.okMessage(myHost,msg);
	}
}
