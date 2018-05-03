package com.planet_ink.coffee_mud.Abilities.Languages;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2010-2018 Bo Zimmerman

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

public class SignLanguage extends StdLanguage
{
	@Override
	public String ID()
	{
		return "SignLanguage";
	}

	private final static String localizedName = CMLib.lang().L("Sign Language");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String writtenName()
	{
		return "Braille";
	}

	public static List<String[]> wordLists=null;
	public SignLanguage()
	{
		super();
	}

	@Override
	public List<String[]> translationVector(String language)
	{
		return wordLists;
	}

	@Override
	protected boolean processSourceMessage(CMMsg msg, String str, int numToMess)
	{
		if(msg.sourceMessage()==null)
			return true;
		int wordStart=msg.sourceMessage().indexOf('\'');
		if(wordStart<0)
			return true;
		String wordsSaid=CMStrings.getSayFromMessage(msg.sourceMessage());
		if(wordsSaid == null)
			return true;
		if(numToMess>0)
			wordsSaid=messChars(ID(),wordsSaid,numToMess);
		final String fullMsgStr = CMStrings.substituteSayInMessage(msg.sourceMessage(),wordsSaid);
		wordStart=fullMsgStr.indexOf('\'');
		String startFullMsg=fullMsgStr.substring(0,wordStart);
		if(startFullMsg.indexOf("YELL(S)")>0)
		{
			msg.source().tell(L("You can't yell in sign language."));
			return false;
		}
		final String oldStartFullMsg = startFullMsg;
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
				   CMath.unsetb(msg.sourceCode(), CMMsg.MASK_SOUND|CMMsg.MASK_MOUTH) | CMMsg.MASK_MOVE,
				   startFullMsg + fullMsgStr.substring(wordStart),
				   msg.targetCode(),
				   msg.targetMessage(),
				   msg.othersCode(),
				   msg.othersMessage());
		return true;
	}

	@Override
	protected boolean processNonSourceMessages(CMMsg msg, String str, int numToMess)
	{
		final String fullOtherMsgStr=(msg.othersMessage()==null)?msg.targetMessage():msg.othersMessage();
		if(fullOtherMsgStr==null)
			return true;
		final int wordStart=fullOtherMsgStr.indexOf('\'');
		if(wordStart<0)
			return true;
		String startFullMsg=fullOtherMsgStr.substring(0,wordStart);
		String verb = "sign(s)";
		switch(CMLib.dice().roll(1, 20, 0))
		{
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
			verb = L("gesture(s)");
			break;
		case 6:
			verb = L("wave(s)");
			break;
		case 7:
		case 8:
			verb = L("gesticulate(s)");
			break;
		case 9:
			verb = L("wave(s) <S-HIS-HER> fingers");
			break;
		case 10:
			verb = L("wiggle(s) <S-HIS-HER> hands");
			break;
		case 11:
		case 12:
			verb = L("wave(s) <S-HIS-HER> hands");
			break;
		case 13:
			verb = L("wiggle(s) <S-HIS-HER> fingers");
			break;
		}
		final String oldStartFullMsg = startFullMsg;
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

	@Override
	protected boolean translateOthersMessage(CMMsg msg, String sourceWords)
	{
		if((msg.othersMessage()!=null)&&(sourceWords!=null))
		{
			String otherMes=msg.othersMessage();
			if((otherMes.lastIndexOf('\'')==otherMes.indexOf('\'')))
				otherMes=otherMes.replace('.', ' ')+'\''+sourceWords+'\'';
			if(msg.target()!=null)
				otherMes=CMLib.coffeeFilter().fullOutFilter(null,(MOB)affected,msg.source(),msg.target(),msg.tool(),otherMes,false);
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),affected,null,CMMsg.NO_EFFECT,null,msg.othersCode(),L("@x1 (translated from @x2)",CMStrings.substituteSayInMessage(otherMes,sourceWords),name()),CMMsg.NO_EFFECT,null));
			return true;
		}
		return false;
	}

	@Override
	protected boolean translateTargetMessage(CMMsg msg, String sourceWords)
	{
		if(msg.amITarget(affected)&&(msg.targetMessage()!=null))
		{
			String otherMes=msg.targetMessage();
			if((otherMes.lastIndexOf('\'')==otherMes.indexOf('\'')))
				otherMes=otherMes.replace('.', ' ')+'\''+sourceWords+'\'';
			if(msg.target()!=null)
				otherMes=CMLib.coffeeFilter().fullOutFilter(null,(MOB)affected,msg.source(),msg.target(),msg.tool(),otherMes,false);
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),affected,null,CMMsg.NO_EFFECT,null,msg.targetCode(),L("@x1 (translated from @x2)",CMStrings.substituteSayInMessage(otherMes,sourceWords),name()),CMMsg.NO_EFFECT,null));
			return true;
		}
		return false;
	}

	@Override
	protected boolean translateChannelMessage(CMMsg msg, String sourceWords)
	{
		if(CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL)&&(msg.othersMessage()!=null))
		{
			String otherMes=msg.othersMessage();
			if((otherMes.lastIndexOf('\'')==otherMes.indexOf('\'')))
				otherMes=otherMes.replace('.', ' ')+'\''+sourceWords+'\'';
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,msg.othersCode(),L("@x1 (translated from @x2)",CMStrings.substituteSayInMessage(otherMes,sourceWords),name())));
			return true;
		}
		return false;
	}

}
