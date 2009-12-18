package com.planet_ink.coffee_mud.Common.interfaces;
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
import java.util.Vector;

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
/**
 * The core of event handling in CoffeeMud, a CMMsg represents an event that
 * occurs.  All events are caused by MOBs (even natural events).   CMMsg objects
 * are usually created by calling the CMClass classloader:
 * @see com.planet_ink.coffee_mud.core.CMClass#getMsg(MOB, int, String)
 *
 * Source
 * The source of any message must always be a valid reference to an instance of the 
 * MOB interface. In short, all events that occur in the system are a direct result 
 * of the activity of a MOB. This is on the theory that the universe is controlled 
 * and governed by sentience. In the extremely rare instances where a mob is not 
 * readily available to provide a message source, one should be instantiated -- 
 * even if it is just a blank, new StdMOB.
 *
 * Target
 * The target of a message may be null, or any valid reference to an instance of the 
 * Environmental interface, which includes Items, MOBs, Rooms, Exits, etc. The type 
 * and context of message you wish to generate will typically tell you intuitively 
 * whether the source is doing something to someone or something else, or is acting 
 * independently. This is usually another mob or an item, but you will find examples 
 * of all kinds of targets in the code.
 *
 * Tool
 * The tool of a message may be null, or any valid reference to an instance of the 
 * Environmental interface, which includes Items, Abilities, MOBs, Rooms, Exits, 
 * etc. The tool represents something which the source is utilizing to accomplish 
 * the task or generate the event. This is typically either an Ability object (like 
 * a Spell or Skill being used), or an Item object (like a weapon in an attack event).
 *
 * Source Code
 * This is an encoded integer which represents what the source MOB is actually doing. 
 * Codes are separable into major and minor codes, with the major code being a bitmask
 * and the minor code being a constant integer representing the event type.  See below.
 *
 * Source Message
 * This is the string which the source MOB will see should the event occur successfully.
 *
 * Target Code
 * This is an encoded integer which represents what is happening to the target. 
 * If there is no target, this number will typically have the value of 0 (CMMsg.NOEFFECT).
 * Codes are separable into major and minor codes, with the major code being a bitmask
 * and the minor code being a constant integer representing the event type.  See below.
 *
 * Target Message
 * This is the string which the target MOB (if it is a MOB) will see should the event 
 * occur successfully. If there is no target, this string is null.
 *
 * Others Code
 * This is an encoded integer which represents how any other objects (such as MOBs, 
 * Items, Rooms, Exits) other than the source and target, in the same room, perceive 
 * the event. If the event is completely imperceptible by anything other than the 
 * source, it may be 0 (CMMsg.NOEFFECT) Codes are separable into major and minor 
 * codes, with the major code being a bitmask and the minor code being a constant 
 * integer representing the event type.  See below.
 *
 * Others Message
 * This is the string which other MOBs in the same room as the source and target 
 * MOBs will see should the event occur successfully. If the event is completely 
 * imperceptible by other MOBs, it may be null.
 * 
 * 
 * ** * ** * ** * ** * ** * ** * ** * ** * ** * ** * ** * ** * ** * ** * ** * ** * ** * ** * ** 
 * You should also familiarize yourself with the interface that handles these
 * events
 * @see com.planet_ink.coffee_mud.core.interfaces.MsgListener#okMessage(Environmental, CMMsg)
 * @see com.planet_ink.coffee_mud.core.interfaces.MsgListener#executeMsg(Environmental, CMMsg)
 * @see com.planet_ink.coffee_mud.core.interfaces.MsgListener
 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room#send(MOB, CMMsg)
 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room#sendOthers(MOB, CMMsg)
 */
@SuppressWarnings("unchecked")
public interface CMMsg extends CMCommon
{
    /**
     * Returns high order bitmask for the target code
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetMinor()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MASK_MAGIC
     * @return high order bitmask for the target code
     */
	public int targetMajor();
	
    /**
     * Returns low order action type integer for the target code
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetMajor()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#TYP_CAST_SPELL
     * @return low order action type integer for the target code
     */
	public int targetMinor();
	
	/**
	 * A combination bitmask, action type integer that describes information
	 * about how this event affects or is perceived by the target of the
	 * action. 
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#target()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetMessage()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetMajor()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetMinor()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MASK_MAGIC
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#TYP_CAST_SPELL
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#setTargetCode(int)
	 * @return the combination bitmask/action type integer for target of event
	 */
	public int targetCode();
	
    /**
     * A combination bitmask, action type integer that describes information
     * about how this event affects or is perceived by the target of the
     * action. 
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#target()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetMessage()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetMajor()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetMinor()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MASK_MAGIC
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#TYP_CAST_SPELL
	 * @param code the new full bitmask/action type target event code
	 */
    public void setTargetCode(int code);
    
    /**
     * Returns the string seen by the target of the event, and only by
     * the target of the event.  
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#target()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#setTargetMessage(String)
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetCode()
     * @return a string to show to the target
     */
	public String targetMessage();
	
	/**
     * Sets the string seen by the target of the event, and only by
     * the target of the event.  
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#target()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetMessage()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetCode()
	 * @param str the string to show to the target, or NULL
	 */
    public void setTargetMessage(String str);
    
    /**
     * Returns whether the given code or mask is either the minor code
     * or a part of the major code of the targetCode
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetCode()
     * @param codeOrMask the target action code or target major mask to check
     * @return whether there is a match 
     */
    public boolean isTarget(int codeOrMask);
    
    /**
     * Returns whether the given code or mask string matches the minor code
     * or a part of the major code of the targetCode.
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#TYPE_DESCS
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MASK_DESCS
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetCode()
     * @param codeOrMaskDesc the target action code string, or mask string
     * @return whether there is a match 
     */
    public boolean isTarget(String codeOrMaskDesc);

    /**
     * Returns whether the given Environmental object is, in fact, the target
     * of this message.
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#target()
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental
     * @param E the Environmental to inspect, may not be null
     * @return whether the given E is the target of this message
     */
    public boolean isTarget(Environmental E);
    
    /**
     * Returns whether the given Environmental object is, in fact, the target
     * of this message.
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#target()
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental
     * @param thisOne the Environmental to inspect, may not be null
     * @return whether the given thisOne is the target of this message
     */
    public boolean amITarget(Environmental thisOne);
    
    /**
     * Returns high order bitmask for the source code
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceMinor()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MASK_MAGIC
     * @return high order bitmask for the source code
     */
	public int sourceMajor();
	
    /**
     * Returns low order action type integer for the target code
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetMajor()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#TYP_CAST_SPELL
     * @return low order action type integer for the target code
     */
	public int sourceMinor();
	
    /**
     * A combination bitmask, action type integer that describes information
     * about how this event affects or is perceived by the source of the
     * action. 
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#source()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceMessage()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceMajor()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceMinor()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MASK_MAGIC
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#TYP_CAST_SPELL
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#setSourceCode(int)
     * @return the combination bitmask/action type integer for source of event
     */
	public int sourceCode();
	
    /**
     * Returns whether the given code or mask is either the minor code
     * or a part of the major code of the sourceCode
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceCode()
     * @param codeOrMask the source action code or source major mask to check
     * @return whether there is a match 
     */
    public boolean isSource(int codeOrMask);
    
    /**
     * Returns whether the given code or mask string matches the minor code
     * or a part of the major code of the sourceCode.
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#TYPE_DESCS
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MASK_DESCS
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceCode()
     * @param codeOrMaskDesc the source action code string, or mask string
     * @return whether there is a match 
     */
    public boolean isSource(String codeOrMaskDesc);
    
    /**
     * Returns whether the given Environmental object is, in fact, the source
     * of this message.
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#source()
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental
     * @param E the Environmental to inspect, may not be null
     * @return whether the given E is the source of this message
     */
    public boolean isSource(Environmental E);
    
    /**
     * Returns whether the given Environmental object is, in fact, the source
     * of this message.
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#source()
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental
     * @param thisOne the Environmental to inspect, may not be null
     * @return whether the given thisOne is the source of this message
     */
    public boolean amISource(MOB thisOne);
    
    /**
     * A combination bitmask, action type integer that describes information
     * about how this event affects or is perceived by the source of the
     * action. 
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#source()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceMessage()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceMajor()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceMinor()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MASK_MAGIC
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#TYP_CAST_SPELL
     * @param code the new full bitmask/action type source event code
     */
    public void setSourceCode(int code);
    
    /**
     * Returns the string seen by the source of the event, and only by
     * the source of the event.  
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#source()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#setSourceMessage(String)
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceCode()
     * @return a string to show to the source
     */
	public String sourceMessage();
	
    /**
     * Sets the string seen by the source of the event, and only by
     * the source of the event.  
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#source()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceMessage()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceCode()
     * @param str the string to show to the source, or NULL
     */
    public void setSourceMessage(String str);

    /**
     * Returns high order bitmask for the others code
     * (Others are everyone not source, not target)
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersMinor()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MASK_MAGIC
     * @return high order bitmask for the others code
     */
	public int othersMajor();
	
    /**
     * Returns low order action type integer for the others code
     * (Others are everyone not source, not target)
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersMajor()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#TYP_CAST_SPELL
     * @return low order action type integer for the others code
     */
	public int othersMinor();
	
    /**
     * A combination bitmask, action type integer that describes information
     * about how this event affects or is perceived by the others of the
     * action.  (Others are everyone not source, not target)
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersMessage()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersMajor()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersMinor()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MASK_MAGIC
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#TYP_CAST_SPELL
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#setOthersCode(int)
     * @return the combination bitmask/action type integer for others of event
     */
	public int othersCode();
	
    /**
     * Returns whether the given code or mask is either the minor code
     * or a part of the major code of the othersCode
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersCode()
     * @param codeOrMask the others action code or others major mask to check
     * @return whether there is a match 
     */
    public boolean isOthers(int codeOrMask);
    
    /**
     * Returns whether the given code or mask string matches the minor code
     * or a part of the major code of the othersCode.  (Others are everyone
     * not source, not target)
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#TYPE_DESCS
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MASK_DESCS
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersCode()
     * @param codeOrMaskDesc the others action code string, or mask string
     * @return whether there is a match 
     */
    public boolean isOthers(String codeOrMaskDesc);
    
    /**
     * A combination bitmask, action type integer that describes information
     * about how this event affects or is perceived by the others of the
     * action.  (Others are everyone not source, not target)
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersMessage()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersMajor()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersMinor()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MASK_MAGIC
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#TYP_CAST_SPELL
     * @param code the new full bitmask/action type others event code
     */
    public void setOthersCode(int code);
    
    /**
     * Returns the string seen by the others of the event, and only by
     * the others of the event.  (Others are everyone not source, not target)
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#setOthersMessage(String)
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersCode()
     * @return a string to show to the others
     */
	public String othersMessage();
	
    /**
     * Sets the string seen by the others of the event, and only by
     * the others of the event. (Others are everyone not source, not target)
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersMessage()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersCode()
     * @param str the string to show to the others, or NULL
     */
    public void setOthersMessage(String str);

    /**
     * Returns whether the given Environmental object is neither the source
     * nor the target of this message.
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#source()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#target()
     * @param E the Environmental to inspect, may not be null
     * @return whether the given E is neither the source nor target of this message
     */
    public boolean isOthers(Environmental E);
    
    /**
     * Returns the target of this event, an Environmental object
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#setTarget(Environmental)
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental
     * @return the target of this event
     */
    public Environmental target();
    
    /**
     * Sets the target of this event, an Environmental object
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#target()
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental
     * @param E the new target of this event
     */
    public void setTarget(Environmental E);

    /**
     * Returns the means, item, portal, or otherwise tool that helps the source
     * affect the event upon the target.  
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#setTool(Environmental)
     * @return the tool of this event
     */
	public Environmental tool();
	
    /**
     * Sets the means, item, portal, or otherwise tool that helps the source
     * affect the event upon the target.  
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#tool()
	 * @param E the new tool of this event
	 */
    public void setTool(Environmental E);
    
    /**
     * Returns the source of this event, a MOB object
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#setSource(MOB)
     * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB
     * @return the source of this event
     */
	public MOB source();
	
    /**
     * Sets the source of this event, a MOB object
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#source()
     * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB
	 * @param mob the new source of this event
	 */
    public void setSource(MOB mob);

    /**
     * Returns the arbitrary value integer associated with this event.  Values tend to
     * be defined in a event-action code specific way, so that it will mean nothing
     * most of the time, and something different for each type of message.  For custom
     * messages therefore, it can be used for whatever one pleases, whereas for established
     * codebase message types, you should understand its meaning before doing anything with
     * it.
     * @return the integer value of this event
     */
	public int value();
	
    /**
     * Sets an arbitrary integer value associated with this event.  Values tend to
     * be defined in a event-action code specific way, so that it will mean nothing
     * most of the time, and something different for each type of message.  For custom
     * messages therefore, it can be used for whatever one pleases, whereas for established
     * codebase message types, you should understand its meaning before doing anything with
     * it.
     * @param amount the integer value of this event
     */
	public void setValue(int amount);

	/**
	 * Modifies one of more fields in this event.
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#source()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#target()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceMessage()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetMessage()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersMessage()
	 * @param source the source of the event
	 * @param target the target of the event
	 * @param newAllCode the source, target, and others code
	 * @param allMessage the source, target, and others message
	 */
    public void modify(MOB source, Environmental target, int newAllCode, String allMessage);
    
    /**
     * Modifies one of more fields in this event. Sets target and tool to NULL.
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#source()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#target()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceMessage()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetMessage()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersMessage()
     * @param source the new source of this event
     * @param newAllCode the new source, target, and others code of this event
     * @param allMessage the new source, target, and others message of this event
     */
    public void modify(MOB source, int newAllCode, String allMessage);
    
    /**
     * Modifies one of more fields in this event. Sets target and tool to NULL.
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#source()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#target()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceMessage()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetMessage()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersMessage()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#value()
     * @param source the new source of this event
     * @param newAllCode the new source, target, and others code of this event
     * @param allMessage the new source, target, and others message of this event
     * @param newValue the new value for this event
     */
    public void modify(MOB source, int newAllCode, String allMessage, int newValue);
    
    /**
     * Modifies one of more fields in this event.
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#source()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#target()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceMessage()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetMessage()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersMessage()
     * @param source the new source of this event
     * @param target the new target of this event
     * @param tool the new tool for this event
     * @param newAllCode the new source, target, and others code of this event
     * @param allMessage the new source, target, and others message of this event
     */
    public void modify(MOB source, Environmental target, Environmental tool, int newAllCode, String allMessage);
    
    /**
     * Modifies one of more fields in this event.
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#source()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#target()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceMessage()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetMessage()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersMessage()
     * @param source the new source of this event
     * @param target the new target of this event
     * @param tool the new tool for this event
     * @param newAllCode the new source, target, and others code of this event
     * @param sourceMessage the new source message for this event
     * @param targetMessage the new target message for this event
     * @param othersMessage the new others message for this event
     */
    public void modify(MOB source,
                       Environmental target,
                       Environmental tool,
                       int newAllCode,
                       String sourceMessage,
                       String targetMessage,
                       String othersMessage);
    
    /**
     * Modifies one of more fields in this event.
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#source()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#target()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceMessage()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetMessage()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersMessage()
     * @param source the new source of this event
     * @param target the new target of this event
     * @param tool the new tool for this event
     * @param newSourceCode the new source code for this event
     * @param sourceMessage the new source message for this event
     * @param newTargetCode the new target code for this event
     * @param targetMessage the new target message for this event
     * @param newOthersCode the new others code for this event
     * @param othersMessage the new others message for this event
     */
	public void modify(MOB source,
						Environmental target,
						Environmental tool,
						int newSourceCode,
						String sourceMessage,
						int newTargetCode,
						String targetMessage,
						int newOthersCode,
						String othersMessage);
	
    /**
     * Modifies one of more fields in this event.
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#source()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#target()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersCode()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceMessage()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetMessage()
     * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersMessage()
     * @param source the new source of this event
     * @param target the new target of this event
     * @param tool the new tool for this event
     * @param newSourceCode the new source code for this event
	 * @param newTargetCode the new target code for this event
	 * @param newOthersCode the new others code for this event
     * @param allMessage the new source, target, and others message of this event
	 */
    public void modify(MOB source,
                       Environmental target,
                       Environmental tool,
                       int newSourceCode,
                       int newTargetCode,
                       int newOthersCode,
                       String allMessage);

    /**
     * Returns a Vector of other CMMsg events which are slated to be confirmed
     * and executed AFTER this current message is handled.  This is implemented
     * by the Room object
     * @see com.planet_ink.coffee_mud.Locales.interfaces.Room#send(MOB, CMMsg)  
     * @return a Vector of CMMsg objects
     */
	public Vector trailerMsgs();
	
    /**
     * Appends to the Vector of other CMMsg events which are slated to be confirmed
     * and executed AFTER this current message is handled.  This is implemented
     * by the Room object
     * @see com.planet_ink.coffee_mud.Locales.interfaces.Room#send(MOB, CMMsg)  
	 * @param msg the CMMsg to append to this message.
	 */
	public void addTrailerMsg(CMMsg msg);

	// 0-1999 are message types
	// 2000-2047 are channels
	// flags are 2048, 4096, 8192, 16384,
	// flags are 2048, 4096, 8192, 16384, 32768, 65536,
	//131072, 262144, 524288, 1048576, and 2097152

	// helpful seperator masks
	/** Mask to remove the MAJOR_MASK from the source, target, or others code, leaving only TYPE */
	public static final int MINOR_MASK=2047;
    /** Mask to remove the TYPE CODE from the source, target, or others code, leaving only MASK bits */
	public static final int MAJOR_MASK=4192256;

	// masks for all messages
	/** MAJOR_MASK bit denoting a source, target, or others code does small hand movements */
	public static final int MASK_HANDS=2048;       // small hand movements
    /** MAJOR_MASK bit denoting a source, target, or others code does large body movements (travel) */
	public static final int MASK_MOVE=4096;        // large body movements (travel)
    /** MAJOR_MASK bit denoting a source, target, or others code does looking and seeing */
	public static final int MASK_EYES=8192;        // looking and seeing
    /** MAJOR_MASK bit denoting a source, target, or others code does speaking and eating */
	public static final int MASK_MOUTH=16384;      // speaking and eating
    /** MAJOR_MASK bit denoting a source, target, or others code does general body noises */
	public static final int MASK_SOUND=32768;      // general body noises
    /** MAJOR_MASK bit denoting a source, target, or others code is always confirmed (involuntary usually) */
	public static final int MASK_ALWAYS=65536;    // anything!
    /** MAJOR_MASK bit denoting a source, target, or others code is magical */
	public static final int MASK_MAGIC=131072;     // the magic mask!
    /** MAJOR_MASK bit denoting a source, target, or others code is thiefly, delicate */
	public static final int MASK_DELICATE=262144;  // for thief skills!
    /** MAJOR_MASK bit denoting a source, target, or others code is malicious, harmful to the target */
	public static final int MASK_MALICIOUS=524288; // for attacking
    /** MAJOR_MASK bit denoting a source, target, or others code is a channel message */
	public static final int MASK_CHANNEL=1048576;  // for channel messages
    /** MAJOR_MASK bit denoting an event that will be repeated, in order to optimize execution */
	public static final int MASK_OPTIMIZE=2097152; // to optimize a repeated msg

	// minor messages
	/** MINOR_MASK minor action code type, denoting a general area event */
	public static final int TYP_AREAAFFECT=1;
    /** MINOR_MASK minor action code type, denoting a push action*/
	public static final int TYP_PUSH=2;
    /** MINOR_MASK minor action code type, denoting a pull action*/
	public static final int TYP_PULL=3;
    /** MINOR_MASK minor action code type, denoting a recall action*/
	public static final int TYP_RECALL=4;
    /** MINOR_MASK minor action code type, denoting a open action*/
	public static final int TYP_OPEN=5;
    /** MINOR_MASK minor action code type, denoting a close action*/
	public static final int TYP_CLOSE=6;
    /** MINOR_MASK minor action code type, denoting a put action*/
	public static final int TYP_PUT=7;
    /** MINOR_MASK minor action code type, denoting a get action*/
	public static final int TYP_GET=8;
    /** MINOR_MASK minor action code type, denoting a unlock action*/
	public static final int TYP_UNLOCK=9;
    /** MINOR_MASK minor action code type, denoting a lock action*/
	public static final int TYP_LOCK=10;
    /** MINOR_MASK minor action code type, denoting a wield action*/
	public static final int TYP_WIELD=11;
    /** MINOR_MASK minor action code type, denoting a give action*/
	public static final int TYP_GIVE=12;
    /** MINOR_MASK minor action code type, denoting a buy action*/
	public static final int TYP_BUY=13;
    /** MINOR_MASK minor action code type, denoting a sell action*/
	public static final int TYP_SELL=14;
    /** MINOR_MASK minor action code type, denoting a drop action*/
	public static final int TYP_DROP=15;
    /** MINOR_MASK minor action code type, denoting a wear action*/
	public static final int TYP_WEAR=16;
    /** MINOR_MASK minor action code type, denoting a fill action*/
	public static final int TYP_FILL=17;
    /** MINOR_MASK minor action code type, denoting a delicate, thiefly action*/
	public static final int TYP_DELICATE_HANDS_ACT=18;
    /** MINOR_MASK minor action code type, denoting a shopkeeper value action*/
	public static final int TYP_VALUE=19;
    /** MINOR_MASK minor action code type, denoting a hold action*/
	public static final int TYP_HOLD=20;
    /** MINOR_MASK minor action code type, denoting a noisy, large movement action*/
	public static final int TYP_NOISYMOVEMENT=21;
    /** MINOR_MASK minor action code type, denoting a quiet, large movement action*/
	public static final int TYP_QUIETMOVEMENT=22;
    /** MINOR_MASK minor action code type, denoting a physical attack*/
	public static final int TYP_WEAPONATTACK=23;
    /** MINOR_MASK minor action code type, denoting a look action*/
	public static final int TYP_LOOK=24;
    /** MINOR_MASK minor action code type, denoting a read action*/
	public static final int TYP_READ=25;
    /** MINOR_MASK minor action code type, denoting a noisy, non-movement action*/
	public static final int TYP_NOISE=26;
    /** MINOR_MASK minor action code type, denoting a speaking action*/
	public static final int TYP_SPEAK=27;
    /** MINOR_MASK minor action code type, denoting a spell casting action*/
	public static final int TYP_CAST_SPELL=28;
    /** MINOR_MASK minor action code type, denoting a shopkeeper list action*/
	public static final int TYP_LIST=29;
    /** MINOR_MASK minor action code type, denoting a eat action*/
	public static final int TYP_EAT=30;
    /** MINOR_MASK minor action code type, denoting a entering action */
	public static final int TYP_ENTER=31;
    /** MINOR_MASK minor action code type, denoting a following action*/
	public static final int TYP_FOLLOW=32;
    /** MINOR_MASK minor action code type, denoting a leaving action*/
	public static final int TYP_LEAVE=33;
    /** MINOR_MASK minor action code type, denoting a sleeping action*/
	public static final int TYP_SLEEP=34;
    /** MINOR_MASK minor action code type, denoting a sitting action*/
	public static final int TYP_SIT=35;
    /** MINOR_MASK minor action code type, denoting a standing action*/
	public static final int TYP_STAND=36;
    /** MINOR_MASK minor action code type, denoting a fleeing action*/
	public static final int TYP_FLEE=37;
    /** MINOR_MASK minor action code type, denoting a nofollow action*/
	public static final int TYP_NOFOLLOW=38;
    /** MINOR_MASK minor action code type, denoting a writing action*/
	public static final int TYP_WRITE=39;
    /** MINOR_MASK minor action code type, denoting a flaming action*/
	public static final int TYP_FIRE=40;
    /** MINOR_MASK minor action code type, denoting a freezing action*/
	public static final int TYP_COLD=41;
    /** MINOR_MASK minor action code type, denoting a wet action*/
	public static final int TYP_WATER=42;
    /** MINOR_MASK minor action code type, denoting a gassing action*/
	public static final int TYP_GAS=43;
    /** MINOR_MASK minor action code type, denoting a mind-affecting action*/
	public static final int TYP_MIND=44;
    /** MINOR_MASK minor action code type, denoting a general unknown action*/
	public static final int TYP_GENERAL=45;
    /** MINOR_MASK minor action code type, denoting an embarrasing action*/
	public static final int TYP_JUSTICE=46;
    /** MINOR_MASK minor action code type, denoting a melting action*/
	public static final int TYP_ACID=47;
    /** MINOR_MASK minor action code type, denoting a shocking action*/
	public static final int TYP_ELECTRIC=48;
    /** MINOR_MASK minor action code type, denoting a poisoning action*/
	public static final int TYP_POISON=49;
    /** MINOR_MASK minor action code type, denoting a cold evil undead action*/
	public static final int TYP_UNDEAD=50;
    /** MINOR_MASK minor action code type, denoting a mount action*/
	public static final int TYP_MOUNT=51;
    /** MINOR_MASK minor action code type, denoting a dismount action*/
	public static final int TYP_DISMOUNT=52;
    /** MINOR_MASK minor action code type, denoting a general noisy movement action */
	public static final int TYP_OK_ACTION=53;
    /** MINOR_MASK minor action code type, denoting a general non-noisy movement action*/
	public static final int TYP_OK_VISUAL=54;
    /** MINOR_MASK minor action code type, denoting a drink action*/
	public static final int TYP_DRINK=55;
    /** MINOR_MASK minor action code type, denoting a small hands, non-movement action*/
	public static final int TYP_HANDS=56;
    /** MINOR_MASK minor action code type, denoting a paralyzing action*/
	public static final int TYP_PARALYZE=57;
    /** MINOR_MASK minor action code type, denoting a wand activation action*/
	public static final int TYP_WAND_USE=58;
    /** MINOR_MASK minor action code type, denoting a serve action*/
	public static final int TYP_SERVE=59;
    /** MINOR_MASK minor action code type, denoting a rebuke action*/
	public static final int TYP_REBUKE=60;
    /** MINOR_MASK minor action code type, denoting an advance on target action */
	public static final int TYP_ADVANCE=61;
    /** MINOR_MASK minor action code type, denoting a disease spreading action*/
	public static final int TYP_DISEASE=62;
    /** MINOR_MASK minor action code type, denoting a dying action*/
	public static final int TYP_DEATH=63;
    /** MINOR_MASK minor action code type, denoting a shopkeeper deposit action*/
	public static final int TYP_DEPOSIT=64;
    /** MINOR_MASK minor action code type, denoting a shopkeeper withdraw action*/
	public static final int TYP_WITHDRAW=65;
    /** MINOR_MASK minor action code type, denoting an emote action */
	public static final int TYP_EMOTE=66;
    /** MINOR_MASK minor action code type, denoting a game quitting action*/
	public static final int TYP_QUIT=67;
    /** MINOR_MASK minor action code type, denoting a game shutdown action*/
	public static final int TYP_SHUTDOWN=68;
    /** MINOR_MASK minor action code type, denoting a shopkeeper view action*/
	public static final int TYP_VIEW=69;
    /** MINOR_MASK minor action code type, denoting a player retire action*/
	public static final int TYP_RETIRE=70;
    /** MINOR_MASK minor action code type, denoting a target retreat from action */
	public static final int TYP_RETREAT=71;
    /** MINOR_MASK minor action code type, denoting a panic/near death action*/
	public static final int TYP_PANIC=72;
    /** MINOR_MASK minor action code type, denoting a throw action*/
	public static final int TYP_THROW=73;
    /** MINOR_MASK minor action code type, denoting a extinguishing action*/
	public static final int TYP_EXTINGUISH=74;
    /** MINOR_MASK minor action code type, denoting a tell action*/
	public static final int TYP_TELL=75;
    /** MINOR_MASK minor action code type, denoting a crawling/sit enter action*/
	public static final int TYP_SITMOVE=76;
    /** MINOR_MASK minor action code type, denoting a knock action*/
	public static final int TYP_KNOCK=77;
    /** MINOR_MASK minor action code type, denoting a practice action */
	public static final int TYP_PRACTICE=78;
    /** MINOR_MASK minor action code type, denoting a teach action*/
	public static final int TYP_TEACH=79;
    /** MINOR_MASK minor action code type, denoting a remove action*/
	public static final int TYP_REMOVE=80;
    /** MINOR_MASK minor action code type, denoting a xp gain/loss action*/
	public static final int TYP_EXPCHANGE=81;
    /** MINOR_MASK minor action code type, denoting a general damaging action*/
	public static final int TYP_DAMAGE=82;
    /** MINOR_MASK minor action code type, denoting a healing action*/
	public static final int TYP_HEALING=83;
    /** MINOR_MASK minor action code type, denoting a room resetting action*/
	public static final int TYP_ROOMRESET=84;
    /** MINOR_MASK minor action code type, denoting a missile weapon reload action*/
	public static final int TYP_RELOAD=85;
    /** MINOR_MASK minor action code type, denoting a sniff action*/
	public static final int TYP_SNIFF=86;
    /** MINOR_MASK minor action code type, denoting an activate action */
	public static final int TYP_ACTIVATE=87;
    /** MINOR_MASK minor action code type, denoting a deactivate action*/
	public static final int TYP_DEACTIVATE=88;
    /** MINOR_MASK minor action code type, denoting a gain or loss of faction*/
    public static final int TYP_FACTIONCHANGE=89;
    /** MINOR_MASK minor action code type, denoting a login action*/
    public static final int TYP_LOGIN=90;
    /** MINOR_MASK minor action code type, denoting a level gain action*/
    public static final int TYP_LEVEL=91;
    /** MINOR_MASK minor action code type, denoting a close look/examine action */
    public static final int TYP_EXAMINE=92;
    /** MINOR_MASK minor action code type, denoting a order target action*/
    public static final int TYP_ORDER=93;
    /** MINOR_MASK minor action code type, denoting an item/room expiration action */
    public static final int TYP_EXPIRE=94;
    /** MINOR_MASK minor action code type, denoting a banker borrow action*/
    public static final int TYP_BORROW=95;
    /** MINOR_MASK minor action code type, denoting an unknown command action */
    public static final int TYP_HUH=96;
    /** MINOR_MASK minor action code type, denoting a bring-to-life action*/
    public static final int TYP_LIFE=97;
    /** MINOR_MASK minor action code type, denoting a auctioneer bid action*/
    public static final int TYP_BID=98;
    /** MINOR_MASK minor action code type, denoting a auctioneer bid action*/
    public static final int TYP_CLANEVENT=99;

    /** MINOR_MASK minor action code type, denoting a channel action -- 2000-2047 are channels*/
	public static final int TYP_CHANNEL=2000; //(2000-2047 are channels)
	
	/** Addendum to TYPE_DESCS for codes above the last index (channels only at this point) */
    public static final Object[][] MISC_DESCS={
        {"CHANNEL",Integer.valueOf(2000),Integer.valueOf(2047)},
    };
    
    /** Index string descriptions of all the MINOR_MASK action code TYP_s */
    public static final String[] TYPE_DESCS={"NOTHING",
        "AREAAFFECT", "PUSH", "PULL", "RECALL", "OPEN", "CLOSE", "PUT", "GET", 
        "UNLOCK", "LOCK", "WIELD", "GIVE", "BUY", "SELL", "DROP", "WEAR", "FILL", 
        "DELICATE_HANDS_ACT", "VALUE", "HOLD", "NOISYMOVEMENT", "QUIETMOVEMENT", 
        "WEAPONATTACK", "LOOK", "READ", "NOISE", "SPEAK", "CAST_SPELL","LIST", 
        "EAT", "ENTER", "FOLLOW", "LEAVE", "SLEEP", "SIT", "STAND", "FLEE", 
        "NOFOLLOW", "WRITE", "FIRE", "COLD", "WATER", "GAS", "MIND", "GENERAL", 
        "JUSTICE", "ACID", "ELECTRIC", "POISON", "UNDEAD", "MOUNT", "DISMOUNT", 
        "OK_ACTION", "OK_VISUAL", "DRINK", "HANDS", "PARALYZE", "WAND_USE", "SERVE", 
        "REBUKE", "ADVANCE", "DISEASE", "DEATH", "DEPOSIT", "WITHDRAW", "EMOTE", 
        "QUIT", "SHUTDOWN", "VIEW", "RETIRE", "RETREAT","PANIC", "THROW", "EXTINGUISH", 
        "TELL", "SITMOVE", "KNOCK", "PRACTICE", "TEACH", "REMOVE", "EXPCHANGE", 
        "DAMAGE", "HEALING", "ROOMRESET", "RELOAD", "SNIFF", "ACTIVATE", "DEACTIVATE", 
        "FACTIONCHANGE", "LOGIN", "LEVEL", "EXAMINE", "ORDER","EXPIRE","BORROW","HUH",
        "LIFE","BID","CLANEVENT"
    };
    
    /** Index string descriptions of all the MAJOR_MASK code MAKS_s */
    public static final String[] MASK_DESCS={
        "TOUCH","MOVE","EYES","MOUTH","SOUND","GENERAL","MAGIC","DELICATE","MALICIOUS","CHANNEL","OPTIMIZE"
    };

	// helpful message groupings
    /** Useful MAJOR_MASK shortcut combining other MASK_ constants related to casting verbal magic */
	public static final int MSK_CAST_VERBAL=MASK_SOUND|MASK_MOUTH|MASK_MAGIC;
    /** Useful MAJOR_MASK shortcut combining other MASK_ constants related to casting verbal malicious magic */
	public static final int MSK_CAST_MALICIOUS_VERBAL=MASK_SOUND|MASK_MOUTH|MASK_MAGIC|MASK_MALICIOUS;
    /** Useful MAJOR_MASK shortcut combining other MASK_ constants related to casting somantic magic */
	public static final int MSK_CAST_SOMANTIC=MASK_HANDS|MASK_MAGIC;
    /** Useful MAJOR_MASK shortcut combining other MASK_ constants related to casting somantic malicious magic */
	public static final int MSK_CAST_MALICIOUS_SOMANTIC=MASK_HANDS|MASK_MAGIC|MASK_MALICIOUS;
    /** Useful MAJOR_MASK shortcut combining other MASK_ constants related to haggling over price */
	public static final int MSK_HAGGLE=MASK_HANDS|MASK_SOUND|MASK_MOUTH;
    /** Useful MAJOR_MASK shortcut combining other MASK_ constants related to casting both verbal and somantic */
	public static final int MSK_CAST=MSK_CAST_VERBAL|MSK_CAST_SOMANTIC;
    /** Useful MAJOR_MASK shortcut combining other MASK_ constants related to casting both verbal and somantic maliciously */
	public static final int MSK_CAST_MALICIOUS=MSK_CAST_MALICIOUS_VERBAL|MSK_CAST_MALICIOUS_SOMANTIC;
    /** Useful MAJOR_MASK shortcut combining other MASK_ constants related to malicious noisy movements */
	public static final int MSK_MALICIOUS_MOVE=MASK_MALICIOUS|MASK_MOVE|MASK_SOUND;

	// all major messages
	/** combined MAJOR and MINOR codes for useful event message type that does absolutely nothing */
	public static final int NO_EFFECT=0;
    /** combined MAJOR and MINOR codes for useful event message type for general area effects */
	public static final int MSG_AREAAFFECT=MASK_ALWAYS|TYP_AREAAFFECT;
    /** combined MAJOR and MINOR codes for useful event message type for a push event */
	public static final int MSG_PUSH=MASK_HANDS|TYP_PUSH;
    /** combined MAJOR and MINOR codes for useful event message type for a pull event*/
	public static final int MSG_PULL=MASK_HANDS|TYP_PULL;
    /** combined MAJOR and MINOR codes for useful event message type for a recall event*/
	public static final int MSG_RECALL=MASK_SOUND|TYP_RECALL; // speak precludes animals
    /** combined MAJOR and MINOR codes for useful event message type for a open event*/
	public static final int MSG_OPEN=MASK_HANDS|TYP_OPEN;
    /** combined MAJOR and MINOR codes for useful event message type for a close event*/
	public static final int MSG_CLOSE=MASK_HANDS|TYP_CLOSE;
    /** combined MAJOR and MINOR codes for useful event message type for a put event*/
	public static final int MSG_PUT=MASK_HANDS|TYP_PUT;
    /** combined MAJOR and MINOR codes for useful event message type for a get event*/
	public static final int MSG_GET=MASK_HANDS|TYP_GET;
    /** combined MAJOR and MINOR codes for useful event message type for a unlock event*/
	public static final int MSG_UNLOCK=MASK_HANDS|TYP_UNLOCK;
    /** combined MAJOR and MINOR codes for useful event message type for a lock event*/
	public static final int MSG_LOCK=MASK_HANDS|TYP_LOCK;
    /** combined MAJOR and MINOR codes for useful event message type for a wield event*/
	public static final int MSG_WIELD=MASK_HANDS|TYP_WIELD;
    /** combined MAJOR and MINOR codes for useful event message type for a give event*/
	public static final int MSG_GIVE=MASK_HANDS|TYP_GIVE;
    /** combined MAJOR and MINOR codes for useful event message type for a buy event*/
	public static final int MSG_BUY=MSK_HAGGLE|TYP_BUY;
    /** combined MAJOR and MINOR codes for useful event message type for a sell event*/
	public static final int MSG_SELL=MSK_HAGGLE|TYP_SELL;
    /** combined MAJOR and MINOR codes for useful event message type for a drop event*/
	public static final int MSG_DROP=MASK_HANDS|TYP_DROP;
    /** combined MAJOR and MINOR codes for useful event message type for a wear event*/
	public static final int MSG_WEAR=MASK_HANDS|TYP_WEAR;
    /** combined MAJOR and MINOR codes for useful event message type for a fill event*/
	public static final int MSG_FILL=MASK_HANDS|MASK_MOVE|MASK_SOUND|TYP_FILL;
    /** combined MAJOR and MINOR codes for useful event message type for a thiefly delicate quiet event*/
	public static final int MSG_DELICATE_SMALL_HANDS_ACT=MASK_HANDS|MASK_DELICATE|TYP_DELICATE_HANDS_ACT;
    /** combined MAJOR and MINOR codes for useful event message type for a thiefly large quiet event*/
	public static final int MSG_DELICATE_HANDS_ACT=MASK_HANDS|MASK_MOVE|MASK_DELICATE|TYP_DELICATE_HANDS_ACT;
    /** combined MAJOR and MINOR codes for useful event message type for a general thiefly embarrassing event */
	public static final int MSG_THIEF_ACT=MASK_HANDS|MASK_MOVE|MASK_DELICATE|TYP_JUSTICE;
    /** combined MAJOR and MINOR codes for useful event message type for a shopkeeper value event*/
	public static final int MSG_VALUE=MSK_HAGGLE|TYP_VALUE;
    /** combined MAJOR and MINOR codes for useful event message type for a hold event*/
	public static final int MSG_HOLD=MASK_HANDS|TYP_HOLD;
    /** combined MAJOR and MINOR codes for useful event message type for a large noisy movement event */
	public static final int MSG_NOISYMOVEMENT=MASK_HANDS|MASK_SOUND|MASK_MOVE|TYP_NOISYMOVEMENT;
    /** combined MAJOR and MINOR codes for useful event message type for a large quiet movement event */
	public static final int MSG_QUIETMOVEMENT=MASK_HANDS|MASK_MOVE|TYP_QUIETMOVEMENT;
    /** combined MAJOR and MINOR codes for useful event message type for a missile weapon reload event*/
	public static final int MSG_RELOAD=MASK_HANDS|TYP_QUIETMOVEMENT;
    /** combined MAJOR and MINOR codes for useful event message type for a physical attack event*/
	public static final int MSG_WEAPONATTACK=MASK_HANDS|MASK_MOVE|MASK_SOUND|MASK_MALICIOUS|TYP_WEAPONATTACK;
    /** combined MAJOR and MINOR codes for useful event message type for a look event*/
	public static final int MSG_LOOK=MASK_EYES|TYP_LOOK;
    /** combined MAJOR and MINOR codes for useful event message type for a read event*/
	public static final int MSG_READ=MASK_EYES|TYP_READ;
    /** combined MAJOR and MINOR codes for useful event message type for a noisy event*/
	public static final int MSG_NOISE=MASK_SOUND|TYP_NOISE;
    /** combined MAJOR and MINOR codes for useful event message type for a speak event*/
	public static final int MSG_SPEAK=MASK_SOUND|MASK_MOUTH|TYP_SPEAK;
    /** combined MAJOR and MINOR codes for useful event message type for a verbal spellcasting event*/
	public static final int MSG_CAST_VERBAL_SPELL=MSK_CAST_VERBAL|TYP_CAST_SPELL;
    /** combined MAJOR and MINOR codes for useful event message type for a shopkeeper list event*/
	public static final int MSG_LIST=MASK_SOUND|MASK_MOUTH|TYP_LIST;
    /** combined MAJOR and MINOR codes for useful event message type for a eat event*/
	public static final int MSG_EAT=MASK_HANDS|MASK_MOUTH|TYP_EAT;
    /** combined MAJOR and MINOR codes for useful event message type for a enter event */
	public static final int MSG_ENTER=MASK_MOVE|MASK_SOUND|TYP_ENTER;
    /** combined MAJOR and MINOR codes for useful event message type for a malicious verbal spellcasting event */
	public static final int MSG_CAST_ATTACK_VERBAL_SPELL=MSK_CAST_MALICIOUS_VERBAL|TYP_CAST_SPELL;
    /** combined MAJOR and MINOR codes for useful event message type for a leave event */
	public static final int MSG_LEAVE=MASK_MOVE|MASK_SOUND|TYP_LEAVE;
    /** combined MAJOR and MINOR codes for useful event message type for a sleep event*/
	public static final int MSG_SLEEP=MASK_MOVE|TYP_SLEEP;
    /** combined MAJOR and MINOR codes for useful event message type for a sit event */
	public static final int MSG_SIT=MASK_MOVE|TYP_SIT;
    /** combined MAJOR and MINOR codes for useful event message type for a stand event */
	public static final int MSG_STAND=MASK_MOVE|TYP_STAND;
    /** combined MAJOR and MINOR codes for useful event message type for a flee event */
	public static final int MSG_FLEE=MASK_MOVE|MASK_SOUND|TYP_FLEE;
    /** combined MAJOR and MINOR codes for useful event message type for a somantic spellcasting event */
	public static final int MSG_CAST_SOMANTIC_SPELL=MSK_CAST_SOMANTIC|TYP_CAST_SPELL;
    /** combined MAJOR and MINOR codes for useful event message type for a malicious somantic spellcasting event */
	public static final int MSG_CAST_ATTACK_SOMANTIC_SPELL=MSK_CAST_MALICIOUS_SOMANTIC|TYP_CAST_SPELL;
    /** combined MAJOR and MINOR codes for useful event message type for a spellcasting event */
	public static final int MSG_CAST=MSK_CAST|TYP_CAST_SPELL;
    /** combined MAJOR and MINOR codes for useful event message type for a malicious spellcasting event */
	public static final int MSG_CAST_MALICIOUS=MSK_CAST_MALICIOUS|TYP_CAST_SPELL;
    /** combined MAJOR and MINOR codes for useful event message type for a always confirmed noisy event */
	public static final int MSG_OK_ACTION=MASK_SOUND|MASK_ALWAYS|TYP_OK_ACTION;
    /** combined MAJOR and MINOR codes for useful event message type for a always confirmed quiet event */
	public static final int MSG_OK_VISUAL=MASK_ALWAYS|TYP_OK_VISUAL;
    /** combined MAJOR and MINOR codes for useful event message type for a drink event */
	public static final int MSG_DRINK=MASK_HANDS|MASK_MOUTH|TYP_DRINK;
    /** combined MAJOR and MINOR codes for useful event message type for a general hands event */
	public static final int MSG_HANDS=MASK_HANDS|TYP_HANDS;
    /** combined MAJOR and MINOR codes for useful event message type for a emoting event */
	public static final int MSG_EMOTE=MASK_SOUND|MASK_HANDS|TYP_EMOTE;
    /** combined MAJOR and MINOR codes for useful event message type for a follow event */
	public static final int MSG_FOLLOW=MASK_ALWAYS|TYP_FOLLOW;
    /** combined MAJOR and MINOR codes for useful event message type for a nofollow event */
	public static final int MSG_NOFOLLOW=MASK_ALWAYS|TYP_NOFOLLOW;
    /** combined MAJOR and MINOR codes for useful event message type for a write event */
	public static final int MSG_WRITE=MASK_HANDS|TYP_WRITE;
    /** combined MAJOR and MINOR codes for useful event message type for a mount event */
	public static final int MSG_MOUNT=MASK_MOVE|MASK_SOUND|TYP_MOUNT;
    /** combined MAJOR and MINOR codes for useful event message type for a dismount event */
	public static final int MSG_DISMOUNT=MASK_MOVE|MASK_SOUND|TYP_DISMOUNT;
    /** combined MAJOR and MINOR codes for useful event message type for a serve event */
	public static final int MSG_SERVE=MASK_MOUTH|MASK_SOUND|TYP_SERVE;
    /** combined MAJOR and MINOR codes for useful event message type for a rebuke event */
	public static final int MSG_REBUKE=MASK_MOUTH|MASK_SOUND|TYP_REBUKE;
    /** combined MAJOR and MINOR codes for useful event message type for a combat advance event */
	public static final int MSG_ADVANCE=MASK_MOVE|MASK_SOUND|MASK_MALICIOUS|TYP_ADVANCE;
    /** combined MAJOR and MINOR codes for useful event message type for a death event */
	public static final int MSG_DEATH=MASK_SOUND|MASK_ALWAYS|TYP_DEATH;
    /** combined MAJOR and MINOR codes for useful event message type for a bank withdraw event */
	public static final int MSG_WITHDRAW=MASK_HANDS|TYP_WITHDRAW;
    /** combined MAJOR and MINOR codes for useful event message type for a bank deposit event */
	public static final int MSG_DEPOSIT=MASK_HANDS|TYP_DEPOSIT;
    /** combined MAJOR and MINOR codes for useful event message type for a game quit event */
	public static final int MSG_QUIT=MASK_ALWAYS|TYP_QUIT;
    /** combined MAJOR and MINOR codes for useful event message type for a game shutdown event */
	public static final int MSG_SHUTDOWN=MASK_ALWAYS|TYP_SHUTDOWN;
    /** combined MAJOR and MINOR codes for useful event message type for a shop view event */
	public static final int MSG_VIEW=MASK_SOUND|MASK_MOUTH|TYP_VIEW;
    /** combined MAJOR and MINOR codes for useful event message type for a retire event */
	public static final int MSG_RETIRE=MASK_ALWAYS|TYP_RETIRE;
    /** combined MAJOR and MINOR codes for useful event message type for a combat retreat event */
	public static final int MSG_RETREAT=MASK_MOVE|MASK_SOUND|TYP_RETREAT;
    /** combined MAJOR and MINOR codes for useful event message type for a panic event */
	public static final int MSG_PANIC=MASK_MOVE|MASK_SOUND|TYP_PANIC;
    /** combined MAJOR and MINOR codes for useful event message type for a throw event */
	public static final int MSG_THROW=MASK_HANDS|MASK_SOUND|TYP_THROW;
    /** combined MAJOR and MINOR codes for useful event message type for a extinguish event */
	public static final int MSG_EXTINGUISH=MASK_HANDS|TYP_EXTINGUISH;
    /** combined MAJOR and MINOR codes for useful event message type for a tell event */
	public static final int MSG_TELL=MASK_ALWAYS|TYP_TELL;
    /** combined MAJOR and MINOR codes for useful event message type for a crawl event */
	public static final int MSG_SITMOVE=MASK_MOVE|TYP_SITMOVE;
    /** combined MAJOR and MINOR codes for useful event message type for a knock event */
	public static final int MSG_KNOCK=MASK_HANDS|MASK_SOUND|TYP_KNOCK;
    /** combined MAJOR and MINOR codes for useful event message type for a practice event */
	public static final int MSG_PRACTICE=MASK_HANDS|MASK_SOUND|MASK_MOVE|TYP_PRACTICE;
    /** combined MAJOR and MINOR codes for useful event message type for a teach event */
	public static final int MSG_TEACH=MASK_HANDS|MASK_SOUND|MASK_MOUTH|MASK_MOVE|TYP_TEACH;
    /** combined MAJOR and MINOR codes for useful event message type for a remove event */
	public static final int MSG_REMOVE=MASK_HANDS|TYP_REMOVE;
    /** combined MAJOR and MINOR codes for useful event message type for a damage event */
	public static final int MSG_DAMAGE=MASK_ALWAYS|TYP_DAMAGE;
    /** combined MAJOR and MINOR codes for useful event message type for a healing event */
	public static final int MSG_HEALING=MASK_ALWAYS|TYP_HEALING;
    /** combined MAJOR and MINOR codes for useful event message type for a room reset event */
	public static final int MSG_ROOMRESET=MASK_ALWAYS|TYP_ROOMRESET;
    /** combined MAJOR and MINOR codes for useful event message type for a sniff event */
	public static final int MSG_SNIFF=MASK_HANDS|TYP_SNIFF;
    /** combined MAJOR and MINOR codes for useful event message type for a activate event */
	public static final int MSG_ACTIVATE=MASK_HANDS|TYP_ACTIVATE;
    /** combined MAJOR and MINOR codes for useful event message type for a deactivate event */
	public static final int MSG_DEACTIVATE=MASK_HANDS|TYP_DEACTIVATE;
    /** combined MAJOR and MINOR codes for useful event message type for a login event */
    public static final int MSG_LOGIN=MASK_ALWAYS|TYP_LOGIN;
    /** combined MAJOR and MINOR codes for useful event message type for a level event */
    public static final int MSG_LEVEL=MASK_ALWAYS|TYP_LEVEL;
    /** combined MAJOR and MINOR codes for useful event message type for a examine event */
    public static final int MSG_EXAMINE=MASK_EYES|TYP_EXAMINE;
    /** combined MAJOR and MINOR codes for useful event message type for a target order event */
	public static final int MSG_ORDER=MASK_SOUND|MASK_MOUTH|TYP_ORDER;
    /** combined MAJOR and MINOR codes for useful event message type for a item expire event */
	public static final int MSG_EXPIRE=MASK_ALWAYS|TYP_EXPIRE;
    /** combined MAJOR and MINOR codes for useful event message type for a banker borrow event */
	public static final int MSG_BORROW=MASK_HANDS|TYP_BORROW;
    /** combined MAJOR and MINOR codes for useful event message type for a eat event */
	public static final int MSG_EAT_GROUND=MASK_MOUTH|TYP_EAT;
    /** combined MAJOR and MINOR codes for useful event message type for an unknown command event */
	public static final int MSG_HUH=MASK_ALWAYS|TYP_HUH;
    /** combined MAJOR and MINOR codes for useful event message type for a bring-to-life event */
    public static final int MSG_BRINGTOLIFE=MASK_ALWAYS|TYP_LIFE;
    /** combined MAJOR and MINOR codes for useful event message type for a auction bid event */
	public static final int MSG_BID=MSK_HAGGLE|TYP_BID;
    /** combined MAJOR and MINOR codes for useful event message type for a clan event */
	public static final int MSG_CLANEVENT=MASK_ALWAYS|TYP_CLANEVENT;
}