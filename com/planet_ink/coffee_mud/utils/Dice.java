package com.planet_ink.coffee_mud.utils;

import java.util.*;

public class Dice

{

    private static Random randomizer = null;

    public static void seed()
    {
        randomizer = new Random(System.currentTimeMillis());
    }

    public static int roll(int number, int die, int modifier)
    {
        if (randomizer == null)
            seed();

        if (die == 0)
            die = 6;

        int total = 0;
        for (int i = 0; i < number; i++)
        {
            total += (Math.abs(randomizer.nextInt() % die)) + 1;
        }
        total += modifier;
        return total;
    }

    public static int rollPercentage()
    {
        if (randomizer == null)
            seed();
        return (Math.abs(randomizer.nextInt() % 100)) + 1;
    }

}