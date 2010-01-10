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
public class Prop_LangTranslator extends Property implements Language
{
	public String ID() { return "Prop_LangTranslator"; }
	public String name(){return "Language Translator";}
	public String writtenName(){return "Language Translator";}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	protected int canAffectCode(){return CAN_MOBS|CAN_ITEMS|CAN_ROOMS;}
    protected DVector langs=new DVector(2);

	public String accountForYourself()
	{ return "Translates spoken language";	}

	public void setMiscText(String text)
	{
		super.setMiscText(text);
		Vector V=CMParms.parse(text);
		langs.clear();
		int lastpct=100;
		for(int v=0;v<V.size();v++)
		{
			String s=(String)V.elementAt(v);
			if(s.endsWith("%")) s=s.substring(0,s.length()-1);
			if(CMath.isNumber(s))
				lastpct=CMath.s_int(s);
			else
			{
				Ability A=CMClass.getAbility(s);
				if(A!=null) langs.addElement(A.ID(),Integer.valueOf(lastpct));
			}
		}
	}

    public Vector languagesSupported() 
    {
        return langs.getDimensionVector(1);
    }
    public boolean translatesLanguage(String language)
    {
        return langs.containsIgnoreCase(language);
    }
    public int getProficiency(String language) {
        for(int i=0;i<langs.size();i++)
            if(((String)langs.elementAt(i,1)).equalsIgnoreCase(language))
                return ((Integer)langs.elementAt(i,2)).intValue();
        return 0;
    }
    public boolean beingSpoken(String language) { return true; }
    public void setBeingSpoken(String language, boolean beingSpoken) {}
    public Hashtable translationHash(String language) { return new Hashtable();}
    public Vector translationVector(String language) { return new Vector();}
    public String translate(String language, String word) { return word;}
    
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(msg.tool() instanceof Ability)
		{
			if(text().length()>0)
			{
				int t=langs.indexOf(msg.tool().ID());
				if(t<0) return;
				Integer I=(Integer)langs.elementAt(t,2);
				if(CMLib.dice().rollPercentage()>I.intValue())
					return;
			}
			if((msg.tool().ID().equals("Fighter_SmokeSignals"))
			&&(msg.sourceCode()==CMMsg.NO_EFFECT)
			&&(msg.targetCode()==CMMsg.NO_EFFECT)
			&&(msg.othersMessage()!=null))
				CMLib.commands().postSay(msg.source(),null,"The smoke signals seem to say '"+msg.othersMessage()+"'.",false,false);
			else
			if(((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
			   ||(msg.sourceMinor()==CMMsg.TYP_ORDER)
			   ||(CMath.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL)))
			&&(msg.sourceMessage()!=null)
			&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE))
			{
				String str=CMStrings.getSayFromMessage(msg.sourceMessage());
				if(str!=null)
				{
				    Environmental target=null;
				    String sourceName = affected.name();
				    if(msg.target() instanceof MOB)
				        target=(MOB)msg.target();
					if(CMath.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL))
						msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.MSG_NOISE|CMMsg.MASK_ALWAYS,sourceName+" say(s) '"+msg.source().name()+" said \""+str+"\" in "+msg.tool().name()+"'"));
					else
					if((target==null)&&(msg.targetMessage()!=null))
						msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.MSG_NOISE|CMMsg.MASK_ALWAYS,sourceName+" say(s) '"+msg.source().name()+" said \""+str+"\" in "+msg.tool().name()+"'"));
					else
					if(msg.othersMessage()!=null)
						msg.addTrailerMsg(CMClass.getMsg(msg.source(),target,null,CMMsg.MSG_NOISE|CMMsg.MASK_ALWAYS,sourceName+" say(s) '"+msg.source().name()+" said \""+str+"\" in "+msg.tool().name()+"'"));
				}
			}
		}
	}
}
