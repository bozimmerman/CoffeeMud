package com.planet_ink.coffee_mud.Common.interfaces;
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

import java.util.List;

/**
 * Although at one time a simple public variable object, the Social became an environmental
 * when it became import to be able to pass it as a tool of a message for various purposes.
 * It still retains much of its original awkwardness as can be seen in the method names.
 * Today the social is still mostly a variable storage unit, with a few methods to make use
 * of those variables.
 *
 * A social object exists not simply as one for each social command (smile, wiggy) but one
 * for each social variation (smile, smile self, smile bob).  The Name() field holds the
 * full unique name of each social object, with ID only having "DefaultSocial".
 *
 * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#Name()
 * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#setName(String)
 *
 * Managed by the SocialsList library.
 *
 * @see com.planet_ink.coffee_mud.Common.interfaces.Social
 * @see com.planet_ink.coffee_mud.Libraries.interfaces.SocialsList
 */

public interface Social extends Environmental, CMCommon
{

	/**
	 * Base name is the name of the social's command word.  It is NOT unique to a social
	 * object however, since a socials name usually includes its target extension.
	 *
	 * @return the base name
	 */
	public String baseName();

	/**
	 * You_see: what the player themself sees when they use this social.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Social#setYou_see(String)
	 * @return  what the player themself sees when they use this social.
	 */
	public String You_see();

	/**
	 * You_see: what the player themself sees when they use this social.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Social#You_see()
	 * @param str  what the player themself sees when they use this social.
	 */
	public void setYou_see(String str);

	/**
	 * Third_party_sees:  what everyone but the player and their target
	 * sees when a player uses this social.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Social#setThird_party_sees(String)
	 * @return the string everyone else sees
	 */
	public String Third_party_sees();

	/**
	 * Third_party_sees:  what everyone but the player and their target
	 * sees when a player uses this social.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Social#Third_party_sees()
	 * @param str the string everyone else sees
	 */
	public void setThird_party_sees(String str);

	/**
	 * Target_sees: what the target of this social sees when social is used on them.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Social#setTarget_sees(String)
	 * @return  what the target of this social sees when social is used on them
	 */
	public String Target_sees();

	/**
	 * Target_sees: what the target of this social sees when social is used on them.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Social#Target_sees()
	 * @param str what the target of this social sees when social is used on them
	 */
	public void setTarget_sees(String str);

	/**
	 * See_when_no_target: what the player sees when this social is used targeted, but
	 * the target is not available.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Social#setSee_when_no_target(String)
	 * @return the string seen by the player when the target has left
	 */
	public String See_when_no_target();

	/**
	 * See_when_no_target: what the player sees when this social is used targeted, but
	 * the target is not available.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Social#See_when_no_target()
	 * @param str the string seen by the player when the target has left
	 */
	public void setSee_when_no_target(String str);

	/**
	 * The official CoffeeMud message code used as the source code for this social,
	 * which reflects what the player is doing.  Only certain codes allowed.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Social#setSourceCode(int)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_SPEAK
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_HANDS
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_NOISE
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_NOISYMOVEMENT
	 * @return the CMMsg social code, fully formed
	 */
	public int sourceCode();

	/**
	 * Sets the official CoffeeMud message code used as the source code for this social,
	 * which reflects what the player is doing.  Only certain codes allowed.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Social#sourceCode()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_SPEAK
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_HANDS
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_NOISE
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_NOISYMOVEMENT
	 * @param code the CMMsg social code, fully formed
	 */
	public void setSourceCode(int code);

	/**
	 * The official CoffeeMud message code used as the others code for this social,
	 * which reflects what the player appears to be doing.  Only certain codes allowed.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Social#setOthersCode(int)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_SPEAK
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_HANDS
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_NOISE
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_NOISYMOVEMENT
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_OK_VISUAL
	 * @return the CMMsg social code, fully formed
	 */
	public int othersCode();

	/**
	 * Sets the official CoffeeMud message code used as the others code for this social,
	 * which reflects what the player appears to be doing.  Only certain codes allowed.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Social#othersCode()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_SPEAK
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_HANDS
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_NOISE
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_NOISYMOVEMENT
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_OK_VISUAL
	 *
	 * @param code the CMMsg social code, fully formed
	 */
	public void setOthersCode(int code);

	/**
	 * The official CoffeeMud message code used as the target code for this social,
	 * which reflects what is being done to the target.  Only certain codes allowed.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Social#setTargetCode(int)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_SPEAK
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_HANDS
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_NOISE
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_NOISYMOVEMENT
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_OK_VISUAL
	 * @return the CMMsg social code, fully formed
	 */
	public int targetCode();

	/**
	 * Sets the official CoffeeMud message code used as the target code for this social,
	 * which reflects what is being done to the target.  Only certain codes allowed.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Social#targetCode()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_SPEAK
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_HANDS
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_NOISE
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_NOISYMOVEMENT
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#MSG_OK_VISUAL
	 * @param code the CMMsg social code, fully formed
	 */
	public void setTargetCode(int code);

	/**
	 * Whether this social variation can be targeted.  Is determined by whether
	 * there is a target parameter in the name.
	 *
	 * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#Name()
	 * @param E The object to check to see if this is targetable against.
	 * @return true, if it can be targeted
	 */
	public boolean targetable(Environmental E);

	/**
	 * Returns the name of the MSP sound file associated with this social.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Social#setMSPfile(String)
	 * @return the msp sound file name
	 */
	public String MSPfile();

	/**
	 * Sets the name of the MSP sound file associated with this social.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Social#MSPfile()
	 * @param newFile the msp sound file name
	 */
	public void setMSPfile(String newFile);

	/**
	 * Executes this social by the given mob, using the target provided, with
	 * help from the provided command line strings in a vector,
	 * and with override message code flag.  It will generate a proper message
	 * and send it to the same room as the mob.
	 *
	 * @param mob the source of the social action
	 * @param commands the vector of strings
	 * @param target the target of the social
	 * @param auto true to override physical constraints of the source or target
	 *
	 * @return true, if successful
	 */
	public boolean invoke(MOB mob,
						  List<String> commands,
						  Physical target,
						  boolean auto);

	/**
	 * Manufactures a proper channel message that represents executing this
	 * social over a public chat channel.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Social#makeMessage(MOB, String, String, int, int, List, String, boolean)
	 *
	 * @param mob the mob doing the social
	 * @param channelInt the channel int representing which chat channel
	 * @param channelName the channel name of the chat channel
	 * @param commands the commands as a string vector entered by the user
	 * @param makeTarget true to create a fake target, or false to find a real one.
	 *
	 * @return the CMMsg that can now be sent to the world as a chat channel message
	 */
	public CMMsg makeChannelMsg(MOB mob,
								int channelInt,
								String channelName,
								List<String> commands,
								boolean makeTarget);

	/**
	 * Manufactures a proper message that represents executing this social as an action
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Social#makeChannelMsg(MOB, int, String, List, boolean)
	 *
	 * @param mob the mob doing the social
	 * @param str the str an optional "header" string for the msg, usually mxp related
	 * @param end the end an optional "trailer" string for the msg, usually mxp related
	 * @param srcMask the src mask to logically OR with the generated message source code
	 * @param fullCode the override target and others code from the one in this social
	 * @param commands the commands as a string vector entered by the user
	 * @param I3channelName the i3channel name or null if n/a
	 * @param makeTarget true to manufacture a target, or false to use a real one
	 *
	 * @return the CMMsg that can now be sent to the world as a chat channel message
	 */
	public CMMsg makeMessage(MOB mob,
							 String str,
							 String end,
							 int srcMask,
							 int fullCode,
							 List<String> commands,
							 String I3channelName,
							 boolean makeTarget);

	/**
	 * Returns the number of actions required to completely
	 * activate this social. A value of 0.0 means perform
	 * instantly.  This method only applies when the user
	 * is not in combat.
	 * @see Social#combatActionsCost(MOB, List)
	 * @see Social#checkedActionsCost(MOB, List)
	 * @param mob the mob executing the social, if any
	 * @param cmds the parameters to be passed to the social, if any
	 * @return the number of player free actions required to do this
	 */
	public double actionsCost(final MOB mob, final List<String> cmds);
	/**
	 * Returns the number of actions required to completely
	 * activate this social. A value of 0.0 means perform
	 * instantly.  This method only applies when the user
	 * is fighting in social.
	 * @see Social#actionsCost(MOB, List)
	 * @see Social#checkedActionsCost(MOB, List)
	 * @param mob the mob executing the social, if any
	 * @param cmds the parameters to be passed to the social, if any
	 * @return the number of player free actions required to do this
	 */
	public double combatActionsCost(final MOB mob, final List<String> cmds);
	/**
	 * Returns the number of actions required to completely
	 * activate this social. A value of 0.0 means perform
	 * instantly.  This method only should check whether the
	 * user is in combat and return a proper value.
	 * @see Social#combatActionsCost(MOB, List)
	 * @see Social#actionsCost(MOB, List)
	 * @param mob the mob executing the social, if any
	 * @param cmds the parameters to be passed to the social, if any
	 * @return the number of player free actions required to do this
	 */
	public double checkedActionsCost(final MOB mob, final List<String> cmds);
}
