package com.planet_ink.coffee_mud.Libraries.interfaces;
import java.util.*;
/*
   Copyright 2005-2020 Bo Zimmerman

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
 * The Dice library governs random numbers in various ranges,
 * various hit point generation algorithms, and selecting objects
 * from various lists at random.
 *
 * @author Bo Zimmerman
 *
 */
public interface DiceLibrary extends CMLibrary
{
	/**
	 * Takes a score from 0-100, normalizes it to
	 * between 5 and 95, and then rolls a random
	 * number between 0 and 100.  If the random number
	 * is below the normalized score, it returns true,
	 * otherwise false
	 * @param score the number from 0-100
	 * @return true if you rolled under the score
	 */
	public boolean normalizeAndRollLess(int score);

	/**
	 * Takes a score from 0-100, normalizes it to
	 * between 5 and 95.
	 *
	 * @param score the number from 0-100
	 * @return the number from 5-95
	 */
	public int normalizeBy5(int score);

	/**
	 * Generates hit points for an NPC based on bizarre
	 * rules.
	 *
	 * If the code given is &gt; 32768, then the
	 * bits above 23 are number of roles, bits 15-23
	 * are the die type, and low bits added. The
	 * level is not used at all.
	 *
	 * If the code is &lt; 32768, then the level is
	 * used in the basic npc formula from the properties.
	 * The code is then the die-base for the formula.
	 *
	 * @see DiceLibrary#getHPCode(String)
	 * @see DiceLibrary#getHPCode(int, int, int)
	 * @see DiceLibrary#getHPBreakup(int, int)
	 *
	 * @param level the level of the npc
	 * @param code the die type, or a bitmap
	 * @return the hit points to give to the npc
	 */
	public int rollHP(int level, int code);

	/**
	 * This function takes a friendly-ish hit point
	 * die roll formula and generates a bitmap
	 * that can be given to the rollHP method.
	 * The bits above 23 are number of roles, bits 15-23
	 * are the die type, and low bits added.
	 *
	 * The dice roll formula type is, for example
	 * 4d6+2 which means to roll a six sided die
	 * 4 times and then add 2.
	 *
	 * @see DiceLibrary#getHPCode(int, int, int)
	 * @see DiceLibrary#rollHP(int, int)
	 * @see DiceLibrary#getHPBreakup(int, int)
	 *
	 * @param str the string to evaluate
	 * @return the encoded hit points bitmap
	 */
	public int getHPCode(String str);

	/**
	 * This function generates an encoded 32 bit
	 * bitmap to represent a die roll for a mob
	 * hitpoints.
	 *
	 * The bits above 23 are number of roles, bits 15-23
	 * are the die type, and low bits added.
	 *
	 * @see DiceLibrary#getHPCode(String)
	 * @see DiceLibrary#rollHP(int, int)
	 * @see DiceLibrary#getHPBreakup(int, int)
	 *
	 * @param roll the number of die rolls
	 * @param dice the sides on the die
	 * @param plus the amount to add to the result
	 * @return the encoded hit points bitmap
	 */
	public int getHPCode(int roll, int dice, int plus);

	/**
	 * Generates the die roll parts for an encoded hit
	 * point bitmap when the code is &gt; 32768,
	 * or according to another formula otherwise.
	 * The 3 parts of the result are:
	 * [0] the number of rolls
	 * [1] the sides of the die
	 * [2] an amount to add to the total
	 *
	 * If the code given is &gt; 32768, then the
	 * bits above 23 are number of roles, bits 15-23
	 * are the die type, and low bits added. The
	 * level is not used at all.
	 *
	 * If the code is &lt; 32768, then the level is
	 * the number of rolls, the code is the sides
	 * of the die, and the add is level * level * 0.85.
	 *
	 * @see DiceLibrary#getHPCode(String)
	 * @see DiceLibrary#getHPCode(int, int, int)
	 * @see DiceLibrary#rollHP(int, int)
	 *
	 * @param level the level of the npc
	 * @param code the die type, or a bitmap
	 * @return the hit points to give to the npc
	 */
	public int[] getHPBreakup(int level, int code);

	/**
	 * Selects and returns one of the objects from
	 * the set, except for the "not" one given
	 *
	 * @see DiceLibrary#pick(Object[])
	 * @see DiceLibrary#pick(int[])
	 * @see DiceLibrary#pick(List)
	 * @see DiceLibrary#pick(int[], int)
	 * @see DiceLibrary#doublePick(Object[][])
	 *
	 * @param set the set to choose from
	 * @param not null, or a member to not select
	 * @return an object from the set, except not
	 */
	public Object pick(Object[] set, Object not);

	/**
	 * Selects and returns one of the objects from
	 * the set.
	 *
	 * @see DiceLibrary#pick(Object[], Object)
	 * @see DiceLibrary#pick(int[])
	 * @see DiceLibrary#pick(List)
	 * @see DiceLibrary#pick(int[], int)
	 * @see DiceLibrary#doublePick(Object[][])
	 *
	 * @param set the set to choose from
	 * @return an object from the set
	 */
	public Object pick(Object[] set);

	/**
	 * Selects and returns one of the ints from
	 * the set, except for the "not" one given
	 *
	 * @see DiceLibrary#pick(Object[])
	 * @see DiceLibrary#pick(Object[], Object)
	 * @see DiceLibrary#pick(int[])
	 * @see DiceLibrary#pick(List)
	 * @see DiceLibrary#doublePick(Object[][])
	 *
	 * @param set the set to choose from
	 * @param not null, or a member to not select
	 * @return an int from the set, except not
	 */
	public int pick(int[] set, int not);

	/**
	 * Selects and returns one of the ints from
	 * the set.
	 *
	 * @see DiceLibrary#pick(Object[], Object)
	 * @see DiceLibrary#pick(Object[])
	 * @see DiceLibrary#pick(List)
	 * @see DiceLibrary#pick(int[], int)
	 * @see DiceLibrary#doublePick(Object[][])
	 *
	 * @param set the set to choose from
	 * @return an int from the set
	 */
	public int pick(int[] set);


	/**
	 * Selects and returns one of the objects from
	 * the list.
	 *
	 * @see DiceLibrary#pick(Object[], Object)
	 * @see DiceLibrary#pick(Object[])
	 * @see DiceLibrary#pick(int[])
	 * @see DiceLibrary#pick(int[], int)
	 * @see DiceLibrary#doublePick(Object[][])
	 *
	 * @param set the list to choose from
	 * @return an object from the list
	 */
	public Object pick(List<? extends Object> set);

	/**
	 * Selects and returns one of the objects from
	 * the one of the object lists in the set.
	 *
	 * @see DiceLibrary#pick(Object[], Object)
	 * @see DiceLibrary#pick(Object[])
	 * @see DiceLibrary#pick(int[])
	 * @see DiceLibrary#pick(List)
	 * @see DiceLibrary#pick(int[], int)
	 *
	 * @param set the sets to choose from
	 * @return an object from the sets
	 */
	public Object doublePick(Object[][] set);

	/**
	 * Returns a random number from 1-100
	 *
	 * @return the random percent number
	 */
	public int rollPercentage();

	/**
	 * The great workhorse that rolls dice.  It will
	 * roll a die-sided die number times, and then
	 * add modifier.
	 *
	 * @see DiceLibrary#rollNormalDistribution(int, int, int)
	 * @see DiceLibrary#rollLowBiased(int, int, int)
	 * @see DiceLibrary#rollInRange(long, long)
	 * @see DiceLibrary#rollInRange(int, int)
	 *
	 * @param number the number of times to roll
	 * @param die the sides of the die
	 * @param modifier the amount to add
	 * @return the randomly rolled result
	 */
	public int roll(int number, int die, int modifier);

	/**
	 * Returns a random number within the given
	 * min and max range.
	 *
	 * @see DiceLibrary#rollInRange(long, long)
	 * @see DiceLibrary#roll(int, int, int)
	 * @see DiceLibrary#rollLowBiased(int, int, int)
	 * @see DiceLibrary#rollNormalDistribution(int, int, int)
	 *
	 * @param min the minimum of the range
	 * @param max the maximum of the range
	 * @return a number in the range
	 */
	public int rollInRange(final int min, final int max);

	/**
	 * Returns a random number within the given
	 * min and max range.
	 *
	 * @see DiceLibrary#rollInRange(int, int)
	 * @see DiceLibrary#roll(int, int, int)
	 * @see DiceLibrary#rollLowBiased(int, int, int)
	 * @see DiceLibrary#rollNormalDistribution(int, int, int)
	 *
	 * @param min the minimum of the range
	 * @param max the maximum of the range
	 * @return a number in the range
	 */
	public long rollInRange(final long min, final long max);

	/**
	 * Rolls dice to generate a random number, but
	 * in a way that ensures a more balanced distribution.
	 *
	 * @see DiceLibrary#roll(int, int, int)
	 * @see DiceLibrary#rollLowBiased(int, int, int)
	 * @see DiceLibrary#rollInRange(int, int)
	 * @see DiceLibrary#rollInRange(long, long)
	 *
	 * @param number the number of times to roll
	 * @param die the sides of the die
	 * @param modifier the amount to add
	 * @return the randomly rolled result
	 */
	public int rollNormalDistribution(int number, int die, int modifier);


	/**
	 * Rolls dice to generate a random number, but
	 * in a way that biases the lower numbers.
	 *
	 * @see DiceLibrary#roll(int, int, int)
	 * @see DiceLibrary#rollNormalDistribution(int, int, int)
	 * @see DiceLibrary#rollInRange(int, int)
	 * @see DiceLibrary#rollInRange(long, long)
	 *
	 * @param number the number of times to roll
	 * @param die the sides of the die
	 * @param modifier the amount to add
	 * @return the randomly rolled result
	 */
	public int rollLowBiased(int number, int die, int modifier);

	/**
	 * Returns the seeded randomizer used by this lib.
	 *
	 * @return the randomizer
	 */
	public Random getRandomizer();

	/**
	 * Returns a long from -(range-1) to (range-1)
	 *
	 * @see DiceLibrary#plusOrMinus(int)
	 *
	 * @param range the range of the random long
	 * @return the random long
	 */
	public long plusOrMinus(final long range);

	/**
	 * Returns an int from -(range-1) to (range-1)
	 *
	 * @see DiceLibrary#plusOrMinus(long)
	 *
	 * @param range the range of the random int
	 * @return the random int
	 */
	public int plusOrMinus(final int range);

	/**
	 * Returns a double from -(range) to (range)
	 *
	 * @see DiceLibrary#plusOrMinus(long)
	 *
	 * @param range the range of the random double
	 * @return the random double
	 */
	public double plusOrMinus(final double range);

	/**
	 * Returns a float from -(range) to (range)
	 *
	 * @see DiceLibrary#plusOrMinus(long)
	 *
	 * @param range the range of the random float
	 * @return the random float
	 */
	public float plusOrMinus(final float range);

	/**
	 * Randomizes the contents of the list.
	 *
	 * @see DiceLibrary#scramble(int[])
	 *
	 * @param objs the list to scramble
	 */
	public void scramble(List<?> objs);

	/**
	 * Randomizes the contents of the set
	 *
	 * @see DiceLibrary#scramble(List)
	 *
	 * @param objs the set to randomize
	 */
	public void scramble(int[] objs);
}
