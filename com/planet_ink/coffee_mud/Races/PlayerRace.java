package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.Area;

public class PlayerRace extends Human
{
    public String ID(){ return "PlayerRace"; }
    public String name(){ return "PlayerRace"; }
    public String[] culturalAbilityNames(){return null;}
    public int[] culturalAbilityProfficiencies(){return null;}
    public int availabilityCode(){return 0;}
    
    public PlayerRace()
    {
        super();
    }

}
