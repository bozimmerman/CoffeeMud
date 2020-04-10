package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AccountStats.Agent;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Achievement;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.AchievementLoadFlag;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Award;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.ExpertiseDefinition;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_web.interfaces.*;

import java.util.*;

/*
   Copyright 2015-2020 Bo Zimmerman

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
 * The achievement library forms the basis of the achievement system,
 * which accepts player events as input, as well as the file based achievement
 * definition files, and tracks player and account completion of achievements
 * through trackers.
 * @see AchievementLibrary.Event
 * @see AchievementLibrary.Tracker
 * @see AchievementLibrary.Achievement
 * @author Bo Zimmerman
 *
 */
public interface AchievementLibrary extends CMLibrary
{
	/**
	 * The list of arguments/parameters common to all achievement event types
	 */
	public final String[] BASE_ACHIEVEMENT_PARAMETERS = new String[] { "EVENT", "DISPLAY", "TITLE", "REWARDS", "VISIBLEMASK" };

	/**
	 * Events define the type of achievement, describing specific arguments that
	 * the achievement of each event type needs. It is also used to specify the
	 * type of player event that has occurred, allowing the appropriate achievement
	 * to be determined and tracked.
	 * @author Bo Zimmerman
	 *
	 */
	public enum Event
	{
		KILLS("Number of Kills",new String[]{"NUM","ZAPPERMASK","PLAYERMASK"}),
		STATVALUE("A Stat value",new String[]{"VALUE","ABOVEBELOW","STAT"}),
		FACTION("A Faction level",new String[]{"VALUE","ABOVEBELOW","ID"}),
		EXPLORE("Exploration",new String[]{"PERCENT","AREA"}),
		CRAFTING("Crafting",new String[]{"NUM","ABILITYID"}),
		MENDER("Mending",new String[]{"NUM","ABILITYID"}),
		SKILLUSE("Using Skills",new String[]{"NUM","ABILITYID"}),
		SOCIALUSE("Using Socials",new String[]{"NUM","SOCIALID"}),
		QUESTOR("Completing Quests",new String[]{"NUM","PLAYERMASK","QUESTMASK"}),
		ACHIEVER("Completing Achievements",new String[]{"ACHIEVEMENTLIST"}),
		ROOMENTER("Entering a Room",new String[]{"ROOMID"}),
		LEVELSGAINED("Gaining Levels",new String[]{"NUM","PLAYERMASK"}),
		CLASSLEVELSGAINED("Gaining Class Levels",new String[]{"NUM","CLASS","PLAYERMASK"}),
		TIMEPLAYED("Time Played",new String[]{"SECONDS","PLAYERMASK"}),
		JUSTBE("Character State",new String[]{"PLAYERMASK"}),
		DEATHS("Dieing",new String[]{"NUM","ZAPPERMASK","PLAYERMASK"}),
		RETIRE("Retiring",new String[]{"NUM","PLAYERMASK"}),
		REMORT("Remorting",new String[]{"NUM","PLAYERMASK"}),
		GOTITEM("Got an item",new String[]{"NUM","ITEMMASK","PLAYERMASK"}),
		FACTIONS("A group of factions",new String[]{"VALUE","ABOVEBELOW","IDMASK","NUM"}),
		BIRTHS("Births",new String[]{"NUM","ZAPPERMASK","PLAYERMASK"}),
		RACEBIRTH("Race Creation",new String[]{"NUM","ZAPPERMASK","PLAYERMASK"}),
		PLAYERBORN("Being a Player Born",new String[]{"PLAYERMASK"}),
		PLAYERBORNPARENT("Being a Player Parent",new String[]{"NUM","ZAPPERMASK","PLAYERMASK"}),
		CLANPROPERTY("Purchased property",new String[]{"NUM","AREAMASK"}),
		CONQUEREDAREAS("Conquered an area",new String[]{"NUM","AREAMASK"}),
		CLANKILLS("Killed a rival clan member",new String[]{"NUM","ZAPPERMASK","PLAYERMASK"}),
		CLANDECLARE("Declared a new relationship",new String[]{"NUM","RELATION"}),
		CHARACTERS("Account size",new String[]{"NUM", "PLAYERMASK"}),
		CLANMEMBERS("Membership size",new String[]{"NUM", "PLAYERMASK"}),
		CONQUESTPOINTS("Conquest points",new String[]{"NUM","AREAMASK"}),
		CLANLEVELSGAINED("Clan levels",new String[]{"NUM"}),
		SHIPSSUNK("Ships sunk",new String[] {"NUM", "PLAYERMASK", "SHIPMASK"}),
		CMDUSE("Using Commands",new String[]{"NUM","COMMANDID"}),
		AREAVISIT("Visit an Area/Plane",new String[]{"AREAMASK","NUM", "PLAYERMASK","TIME_MINS"}),
		;
		private final String[] parameters;
		private final String displayName;

		private Event(final String displayName, final String[] extraParameters)
		{
			this.displayName = displayName;
			parameters = CMParms.combine(BASE_ACHIEVEMENT_PARAMETERS, extraParameters);
		}

		/**
		 * Returns the friendly display name of this event.
		 * @return the friendly display name of this event.
		 */
		public String displayName()
		{
			return this.displayName;
		}

		/**
		 * Returns all arguments, required and optional, to this achievement event type
		 * @return all arguments to this achievement event type
		 */
		public String[] getParameters()
		{
			return parameters;
		}

		/**
		 * Returns a string list of all event types.
		 * @return a string list of all event types.
		 */
		public static String[] getEventChoices()
		{
			final List<String> choices=new ArrayList<String>(Event.values().length);
			for(final Event E : Event.values())
				choices.add(E.name());
			return choices.toArray(new String[0]);
		}
	}

	/**
	 * The achievement interface provides basic information about the specific achievement,
	 * as defined in the achievements.ini definition file.  It also allows a tracker to
	 * be created for situation where progress in the event requires tracking each
	 * player event.
	 * @author Bo Zimmerman
	 *
	 */
	public interface Achievement
	{
		/**
		 * Returns whether this is a player or account achievement.
		 * @see com.planet_ink.coffee_mud.Common.interfaces.AccountStats.Agent
		 * @return the agent type
		 */
		public AccountStats.Agent getAgent();

		/**
		 * Returns the event that defines the general type of this achievement.
		 * @see Event
		 * @return the event that defines the general type of this achievement.
		 */
		public Event getEvent();

		/**
		 * Gets the tattoo that is added to players or accounts to designate
		 * that this achievement has been completed.  It is also the unique
		 * key for all achievements.
		 * @return the tattoo that is added to players or accounts
		 */
		public String getTattoo();

		/**
		 * Returns whether this achievement can be seen in lists
		 * by the given mob, and whether, if it is achieved, an
		 * announcement is given to the player.
		 *
		 * @param mob the mob to check for
		 * @return true if it can be seen, false otherwise
		 */
		public boolean canBeSeenBy(final MOB mob);

		/**
		 * Returns whether this achievement can be applied to the
		 * given agent type.  Some are account or clan only, and
		 * some account or player, etc...
		 *
		 * @param agent the agent type
		 * @return true if the achievement applies, false otherwise
		 */
		public boolean canApplyTo(Agent agent);

		/**
		 * Creates a new tracker object with the given progress count as
		 * the default/starting value.
		 * @see Achievement#getTargetCount()
		 * @param oldCount the initial value for progress, if applicable
		 * @return a new tracker object with the given progress count
		 */
		public Tracker getTracker(int oldCount);

		/**
		 * Parses the parameters defined by the event type of this achievement
		 * to produce the unique achievement that this is. The parameters are
		 * in key=value pairs, space delimited.
		 * @see Event#getParameters()
		 * @see Achievement#getRawParmVal(String)
		 * @param parms the parameter values to parse
		 * @return "" if all went well, or an error message
		 */
		public String parseParms(String parms);

		/**
		 * Returns the friendly display name of this achievement.
		 * @return the friendly display name of this achievement.
		 */
		public String getDisplayStr();

		/**
		 * Returns the list of coded awards given to players who
		 * complete this achievement.  The format is a string with
		 * a number followed by a string of the type of thing
		 * to award, such as QP, XP, or a currency.
		 * @see AchievementLibrary.Award
		 * @return the list of decoded awards given to players
		 */
		public Award[] getRewards();

		/**
		 * For achievements that require tracking progress,
		 * this returns the target count that the tracker
		 * must report.
		 * @return the target count that the tracker
		 */
		public int getTargetCount();

		/**
		 * Returns true if this achievement is completed
		 * when the tracker count is &gt; than the target count,
		 * and false if the opposite is true.
		 * @return true for a &gt; completion
		 */
		public boolean isTargetFloor();

		/**
		 * Returns true if this achievement requires progress
		 * tracking, which means it must be saved with the player
		 * and/or account record to keep track of.
		 * @return true if this achievement requires progress
		 */
		public boolean isSavableTracker();

		/**
		 * Allows access to the parameters passed into this
		 * achievement to create it.
		 * @see Event#getParameters()
		 * @see Achievement#parseParms(String)
		 * @param str the name of the parameter to query
		 * @return the value of the parameter
		 */
		public String getRawParmVal(String str);
	}

	/**
	 * The award type is an enumeration of the different types
	 * of awards that can be granted for completing an achievement.
	 * @author Bo Zimmerman
	 *
	 */
	public enum AwardType
	{
		QP,
		XP,
		CURRENCY,
		TITLE,
		ABILITY,
		EXPERTISE,
		STAT,
		NOPURGE,
		CLANXP,
		CLANCURRENCY,
		;
	}

	/**
	 * Flags to denote how awards are given when achievements
	 * are granted, especially on character creation and/or
	 * remort.
	 * @author Bo Zimmerman
	 *
	 */
	public enum AchievementLoadFlag
	{
		REMORT_PRELOAD,
		REMORT_POSTLOAD,
		CHARCR_PRELOAD,
		CHARCR_POSTLOAD,
		NORMAL
	}

	/**
	 * The award interface provides pre-parsed award information for those who
	 * complete the achievement.
	 * @author Bo Zimmerman
	 *
	 */
	public interface Award
	{
		/**
		 * The type of award
		 * @return type of award
		 */
		public AwardType getType();

		/**
		 * Returns a description of the award
		 * @return a description of the award
		 */
		public String getDescription();

		/**
		 * Returns true if the award is given before the character
		 * is fully created.
		 * @return true if it is pre-awarded, false if always after
		 */
		public boolean isPreAwarded();

		/**
		 * Returns true if the award is given only to new characters or
		 * at achievement-time
		 * @return true if it is only awarded for new chars and at ach-time
		 */
		public boolean isNotAwardedOnRemort();
	}

	/**
	 * The AbilityAward interface provides pre-parsed award information for those who
	 * complete the achievement.
	 * @author Bo Zimmerman
	 *
	 */
	public interface AbilityAward extends Award
	{
		/**
		 * The ability mapping granted by this award
		 * @return the ability mapping granted by this award
		 */
		public AbilityMapping getAbilityMapping();
	}

	/**
	 * The award interface provides pre-parsed award information for those who
	 * complete the achievement.
	 * @author Bo Zimmerman
	 *
	 */
	public interface AmountAward extends Award
	{
		/**
		 * The amount awarded by the award
		 * @return amount awarded by the award
		 */
		public int getAmount();
	}

	/**
	 * The award interface provides pre-parsed award information for those who
	 * complete the achievement.
	 * @author Bo Zimmerman
	 *
	 */
	public interface CurrencyAward extends AmountAward
	{
		/**
		 * The currency name awarded by this award
		 * @return currency name awarded by this award
		 */
		public String getCurrency();
	}

	/**
	 * The award interface provides pre-parsed award information for those who
	 * complete the achievement.
	 * @author Bo Zimmerman
	 *
	 */
	public interface StatAward extends AmountAward
	{
		/**
		 * The stat name awarded by this award
		 * @return stat name awarded by this award
		 */
		public String getStat();
	}

	/**
	 * The award interface provides pre-parsed award information for those who
	 * complete the achievement.
	 * Gets the title that is awarded to players who complete this
	 * achievement.  It is in normal title format, where * is
	 * substituted for the players name.
	 * @author Bo Zimmerman
	 *
	 */
	public interface TitleAward extends Award
	{
		/**
		 * The title name awarded by this award
		 * @return title name awarded by this award
		 */
		public String getTitle();
	}

	/**
	 * The ExpertiseAward interface provides pre-parsed award information for those who
	 * complete the achievement.
	 * @author Bo Zimmerman
	 *
	 */
	public interface ExpertiseAward extends Award
	{
		/**
		 * The level at which the player is granted, or qualified for,
		 * this expertise.
		 * @return the level at which the player is granted
		 */
		public int getLevel();

		/**
		 * The expertise granted by this award.
		 * @return expertise granted by this award.
		 */
		public ExpertiseDefinition getExpertise();
	}

	/**
	 * A tracker object assigned to a particular player or account
	 * for a particular achievement, allowing the achievement to
	 * track progress if it needs to, or just providing a way
	 * to quickly query completion otherwise.
	 * @author Bo Zimmerman
	 *
	 */
	public interface Tracker extends Cloneable
	{
		/**
		 * The achievement to which this tracker belongs.
		 * @return achievement to which this tracker belongs.
		 */
		public Achievement getAchievement();

		/**
		 * Returns true if the given mob has completed this
		 * achievement, even if the tattoo has not yet been
		 * assigned.
		 * @param tracked the player being checked
		 * @return true if completion has happened, false otherwise
		 */
		public boolean isAchieved(Tattooable tracked);

		/**
		 * For events which require tracked progress, this method is
		 * called to give this tracker a potential bump, after testing
		 * the given mob and the given arguments to see if the
		 * achievement deserves a bump in progress.
		 * @param mob the player who did something trackable
		 * @param tracked the object that can get credit/tattooed
		 * @param bumpNum the amount to bump the progress by
		 * @param parms optional arguments unique to the Event
		 * @return true if a bump occurred, false otherwise
		 */
		public boolean testBump(MOB mob, Tattooable tracked, int bumpNum, Object... parms);

		/**
		 * Returns the count/score to show for the given mob.  If the
		 * achievement of this tracker is Savable, then the mob may be
		 * null, since the count would then be internally stored.
		 * @param tracked the mob to get a count for -- required ONLY for unsavable
		 * @return the score for this achievement and this mob
		 */
		public int getCount(Tattooable tracked);

		/**
		 * Returns a copy of this tracker, unattached to the
		 * tracker it is a copy of.
		 * @return a copy of this tracker
		 */
		public Tracker copyOf();
	}

	/**
	 * This method is how an achievement definition row is evaluated and added
	 * to the permanent list of achievements.  It accepts the coded row with
	 * key=value pairs for all the achievement arguments.  If all is well, ""
	 * is returned, otherwise an error message is returned.
	 * @param agent whether this is a player or account
	 * @param row the coded key=value pairs row.
	 * @param addIfPossible true if, on success, the new achievment is added, false otherwise
	 * @return the error message, or "" for success
	 */
	public String evaluateAchievement(AccountStats.Agent agent, String row, boolean addIfPossible);

	/**
	 * Forces all achievements to be reloaded from the definition file.
	 */
	public void reloadAchievements();

	/**
	 * Iterates through all the player achievements to see if the given mob has completed
	 * any new ones, granting them and the awards if they have.
	 * @param mob the player to evaluate
	 * @return true if any achievements were newly completed, false otherwise
	 */
	public boolean evaluatePlayerAchievements(MOB mob);

	/**
	 * Iterates through all the player account achievements to see if the given
	 * mob has completed any new ones, granting them and the awards if they have.
	 * @param mob the player whose account to evaluate
	 * @return true if any achievements were newly completed, false otherwise
	 */
	public boolean evaluateAccountAchievements(final MOB mob);

	/**
	 * Iterates through all the clan achievements to see if any clans
	 * have completed any new ones, granting them and the awards if they have.
	 * @return true if any achievements were newly completed, false otherwise
	 */
	public boolean evaluateClanAchievements();

	/**
	 * Allows iterating through all the achievements of the given agent group.
	 * If the agent is null, then ALL achievements from all groups are iterated
	 * through.
	 * @param agent the player, or account, or null for all
	 * @return the enumeration of all achievements that apply
	 */
	public Enumeration<Achievement> achievements(AccountStats.Agent agent);

	/**
	 * Returns the achievement with the given tattoo key.
	 * @param tattoo the tattoo key to find the achievement for
	 * @return the achievement object
	 */
	public Achievement getAchievement(String tattoo);

	/**
	 * Finds and deleted the achievement with teh given tattoo key.
	 * @param tattoo the tattoo key to find the achievement for
	 * @return the achievement object deleted, or null if not found
	 */
	public Achievement deleteAchievement(String tattoo);

	/**
	 * Forces any changed or deleted achievements to re-saved to
	 * the definition file.
	 * @param modifyTattoo the tattoo modified or deleted
	 */
	public void resaveAchievements(final String modifyTattoo);

	/**
	 * Allows a new achievement to be added or removed, with a user interface
	 * editor presented for the given mob.
	 * @param mob the mob adding or editing the achievement
	 * @param agent whether player or account achievement
	 * @param tattoo the tattoo of the new or old achievement
	 * @param A the achievement to modify, or null for new
	 * @return true if the achievement was added or modified, false otherwise
	 */
	public boolean addModifyAchievement(final MOB mob, AccountStats.Agent agent, final String tattoo, Achievement A);

	/**
	 * When an event occurs that might possible cause a player to have one of their achievements bumped,
	 * this method is called with event specific parameters which might possibly cause the achievement
	 * to be bumped in the tracker, which might cause it to be completed as well.
	 * @param mob the player whose achievement needs to be checked
	 * @param E the event that occurred
	 * @param bumpNum the amount to bump the achievement by
	 * @param parms any event-specific argument that help determine whether a bump is warranted.
	 */
	public void possiblyBumpAchievement(final MOB mob, final Event E, int bumpNum, Object... parms);

	/**
	 * When an event occurs that might possible cause a player to have one of their achievements bumped,
	 * this method is called with event specific parameters which might possibly cause the achievement
	 * to be bumped in the tracker, which might cause it to be completed as well.  This method does
	 * not actually affect the players trackers, but only pretends to, and then returns the list of
	 * Achievements that would be gained by the effort.
	 * @param mob the player whose achievement needs to be checked
	 * @param E the event that occurred
	 * @param bumpNum the amount to bump the achievement by
	 * @param parms any event-specific argument that help determine whether a bump is warranted.
	 * @return the list of achievements that would be earned by the bump, if any. Empty list otherwise.
	 */
	public List<Achievement> fakeBumpAchievement(final MOB mob, final Event E, int bumpNum, Object... parms);

	/**
	 * Returns all the comment/help entries from the achievement definition file
	 * The map is of the form event ID, then parameter-&gt;help map.
	 * @return all the comment/help entries from the achievement definition file
	 */
	public Map<String,Map<String,String>> getAchievementsHelpMap();

	/**
	 * Given the comments/help entried from the achievement definition file, and an event,
	 * and the name of the parameter inside the event, this returns the help entry for
	 * that parameter
	 * @param helpMap the help map
	 * @param E the event to get help for
	 * @param parmName the parameter of that event to get help for
	 * @return the help text.
	 */
	public String getAchievementsHelpFromMap(Map<String,Map<String,String>> helpMap, Event E, String parmName);

	/**
	 * Converts a parsed awards list back into an unparsed parameter/value string.
	 * @param awards a parsed awards list
	 * @return an unparsed parameter/value string
	 */
	public String getAwardString(final Award[] awards);

	/**
	 * Typically called when a mob gains a level, to allow the achievements on the mob to
	 * assign any new skills or expertises.  Can also be called just to populate a mob
	 * with achievement skills, so it should also confirm any lower level skills also.
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#addAbility(Ability)
	 * @param mob the mob to give abilities to.
	 */
	public void grantAbilitiesAndExpertises(MOB mob);

	/**
	 * When a player is loaded, this method inspects their tattoos for any past
	 * achievements and, if found, loads the playerstats with trackable
	 * skill and expertises mappings, allowing them to receive those awards when
	 * the time is right.
	 * @see AchievementLibrary#grantAbilitiesAndExpertises(MOB)
	 * @param mob the tattooable mob to check tattoos on
	 * @param stats the playerstats to load with prizes
	 */
	public void loadPlayerSkillAwards(Tattooable mob, PlayerStats stats);

	/**
	 * When a new player is created, this method inspects their account tattoos
	 * for any that need to be passed down to this new player.  If any are passed
	 * down, then the awards are granted, including skill awards if any.
	 * @see AchievementLibrary#loadPlayerSkillAwards(Tattooable, PlayerStats)
	 * @param mob the new character to load up.
	 * @param flag the circumstances under which achievements are being loaded
	 */
	public void loadAccountAchievements(final MOB mob, final AchievementLoadFlag flag);

	/**
	 * When a new player added to a clan, this method inspects their clan tattoos
	 * for any that need to be passed down to this new member.  If any are passed
	 * down, then the awards are granted, including skill awards if any.
	 * @see AchievementLibrary#loadPlayerSkillAwards(Tattooable, PlayerStats)
	 * @param mob the new character to load up.
	 * @param flag the circumstances under which achievements are being loaded
	 */
	public void loadClanAchievements(final MOB mob, final AchievementLoadFlag flag);

	/**
	 * When a player remorts, they keep their player achievements, but the rewards
	 * are removed.  This method is called to re-reward all player achievement rewards.
	 * @param mob the mob to award
	 * @param flag this is happening before or after stat selection
	 */
	public void reloadPlayerAwards(MOB mob, AchievementLoadFlag flag);

	/**
	 * Searches for an Achievement of the given tattoo name or display name,
	 * and returns a help entry for the achievement.
	 * @param ID the tattoo name or display name
	 * @param exact true for exact matches only, false for startswith
	 * @return the help entry, or ""
	 */
	public String getAchievementsHelp(String ID, boolean exact);

	/**
	 * Returns a friendly description of the award, adjusting for the recipient, and
	 * who the award was actually awarded to.
	 * @param A the achievement the award came from
	 * @param award the award
	 * @param forM the recipient of the award
	 * @param forT the earner of the award (mob, clan, etc...)
	 * @return the friendliest award description possible
	 */
	public String fixAwardDescription(final Achievement A, final Award award, final MOB forM, final Tattooable forT);

	/**
	 * When a clan member is removed from his clan for any reason, this
	 * method is called to remove any inhereted benefits, such as titles,
	 * or abilities or expertises, etc.
	 * @param mob the evicted mob
	 * @param clan the clan evicted from
	 * @return the message about what is removed
	 */
	public String removeClanAchievementAwards(final MOB mob, final Clan clan);


}
