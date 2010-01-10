package com.planet_ink.coffee_mud.Abilities.Languages;
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
public class SignLanguage extends StdLanguage
{
	public String ID() { return "SignLanguage"; }
	public String name(){ return "Sign Language";}
	public String writtenName() { return "Braille";}
	public static Vector wordLists=null;
	private static boolean mapped=false;
	public SignLanguage()
	{
		super();
		if(!mapped){mapped=true;
					CMLib.ableMapper().addCharAbilityMapping("All",1,ID(),false);}
	}

	public Vector translationVector(String language)
	{
		return wordLists;
	}

    protected boolean processSourceMessage(CMMsg msg, String str, int numToMess)
    {
    	if(msg.sourceMessage()==null) return true;
    	int wordStart=msg.sourceMessage().indexOf('\'');
    	if(wordStart<0) return true;
        String wordsSaid=CMStrings.getSayFromMessage(msg.sourceMessage());
        if(numToMess>0) wordsSaid=messChars(ID(),wordsSaid,numToMess);
        String fullMsgStr = CMStrings.substituteSayInMessage(msg.sourceMessage(),wordsSaid);
    	wordStart=fullMsgStr.indexOf('\'');
    	String startFullMsg=fullMsgStr.substring(0,wordStart);
    	if(startFullMsg.indexOf("YELL(S)")>0)
    	{
    		msg.source().tell("You can't yell in sign language.");
    		return false;
    	}
    	String oldStartFullMsg = startFullMsg;
    	startFullMsg = CMStrings.replaceFirstWord(startFullMsg, "say(s)", "sign(s)");
    	startFullMsg = CMStrings.replaceFirstWord(startFullMsg, "ask(s)", "sign(s) askingly");
    	startFullMsg = CMStrings.replaceFirstWord(startFullMsg, "exclaim(s)", "sign(s) excitedly");
    	if(oldStartFullMsg.equals(startFullMsg))
    	{
    		int x=startFullMsg.toLowerCase().lastIndexOf("(s)");
    		if(x<0) 
    			x=startFullMsg.trim().length();
    		else
    			x+=3;
    		startFullMsg = startFullMsg.substring(0,x)+" in sign" +startFullMsg.substring(x);
    	}
    	
		msg.modify(msg.source(),
				   msg.target(),
				   this,
				   CMath.unsetb(msg.sourceCode(), CMMsg.MASK_SOUND) | CMMsg.MASK_MOVE,
				   startFullMsg + fullMsgStr.substring(wordStart),
				   msg.targetCode(),
                   msg.targetMessage(),
				   msg.othersCode(),
                   msg.othersMessage());
		return true;
    }
    
    protected boolean processNonSourceMessages(CMMsg msg, String str, int numToMess)
    {
        String fullOtherMsgStr=(msg.othersMessage()==null)?msg.targetMessage():msg.othersMessage();
        if(fullOtherMsgStr==null) return true;
    	int wordStart=fullOtherMsgStr.indexOf('\'');
    	if(wordStart<0) return true;
    	String startFullMsg=fullOtherMsgStr.substring(0,wordStart);
    	String verb = "sign(s)";
    	switch(CMLib.dice().roll(1, 20, 0))
    	{
    	case 1: case 2: case 3: case 4: case 5: verb="gesture(s)"; break;
    	case 6: verb="wave(s)"; break;
    	case 7: case 8: verb="gesticulate(s)"; break;
    	case 9: verb="wave(s) <S-HIS-HER> fingers"; break;
    	case 10: verb="wiggle(s) <S-HIS-HER> hands"; break;
    	case 11: case 12: verb="wave(s) <S-HIS-HER> hands"; break;
    	case 13: verb="wiggle(s) <S-HIS-HER> fingers"; break;
    	}
    	String oldStartFullMsg = startFullMsg;
    	startFullMsg = CMStrings.replaceFirstWord(startFullMsg, "tell(s)", verb);
    	startFullMsg = CMStrings.replaceFirstWord(startFullMsg, "say(s)", verb);
    	startFullMsg = CMStrings.replaceFirstWord(startFullMsg, "ask(s)", verb+" askingly");
    	startFullMsg = CMStrings.replaceFirstWord(startFullMsg, "exclaim(s)", verb+" excitedly");
    	if(oldStartFullMsg.equals(startFullMsg))
    	{
    		int x=startFullMsg.toLowerCase().lastIndexOf("(s)");
    		if(x<0) 
    			x=startFullMsg.trim().length();
    		else
    			x+=3;
    		startFullMsg = startFullMsg.substring(0,x)+" in "+verb+startFullMsg.substring(x);
    	}
		msg.modify(msg.source(),
				   msg.target(),
				   this,
				   msg.sourceCode(),
				   msg.sourceMessage(),
				   CMath.unsetb(msg.targetCode(), CMMsg.MASK_SOUND) | CMMsg.MASK_MOVE,
				   startFullMsg.trim() + ".",
				   CMath.unsetb(msg.othersCode(), CMMsg.MASK_SOUND) | CMMsg.MASK_MOVE,
				   startFullMsg.trim() + ".");
		return true;
    }
    
	protected boolean translateOthersMessage(CMMsg msg, String sourceWords)
	{
		if((msg.othersMessage()!=null)&&(sourceWords!=null)&&(sourceWords!=null))
		{
			String otherMes=msg.othersMessage();
			if((otherMes.lastIndexOf('\'')==otherMes.indexOf('\'')))
				otherMes=otherMes.replace('.', ' ')+'\''+sourceWords+'\'';
			if(msg.target()!=null)
				otherMes=CMLib.coffeeFilter().fullOutFilter(null,(MOB)affected,msg.source(),msg.target(),msg.tool(),otherMes,false);
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),affected,null,CMMsg.NO_EFFECT,null,msg.othersCode(),CMStrings.substituteSayInMessage(otherMes,sourceWords)+" (translated from "+name()+")",CMMsg.NO_EFFECT,null));
			return true;
		}
		return false;
	}
	
	protected boolean translateTargetMessage(CMMsg msg, String sourceWords)
	{
		if(msg.amITarget(affected)&&(msg.targetMessage()!=null))
		{
			String otherMes=msg.targetMessage();
			if((otherMes.lastIndexOf('\'')==otherMes.indexOf('\'')))
				otherMes=otherMes.replace('.', ' ')+'\''+sourceWords+'\'';
			if(msg.target()!=null)
				otherMes=CMLib.coffeeFilter().fullOutFilter(null,(MOB)affected,msg.source(),msg.target(),msg.tool(),otherMes,false);
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),affected,null,CMMsg.NO_EFFECT,null,msg.targetCode(),CMStrings.substituteSayInMessage(otherMes,sourceWords)+" (translated from "+name()+")",CMMsg.NO_EFFECT,null));
			return true;
		}
		return false;
	}

	protected boolean translateChannelMessage(CMMsg msg, String sourceWords)
	{
		if(CMath.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL)&&(msg.othersMessage()!=null))
		{
			String otherMes=msg.othersMessage();
			if((otherMes.lastIndexOf('\'')==otherMes.indexOf('\'')))
				otherMes=otherMes.replace('.', ' ')+'\''+sourceWords+'\'';
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,msg.othersCode(),CMStrings.substituteSayInMessage(otherMes,sourceWords)+" (translated from "+name()+")"));
			return true;
		}
		return false;
	}
	
}
