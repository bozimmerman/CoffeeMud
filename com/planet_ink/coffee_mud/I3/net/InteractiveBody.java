package com.planet_ink.coffee_mud.i3.net;

public interface InteractiveBody {
    public abstract void executeCommand(String cmd);
    public abstract void loseLink();
}
