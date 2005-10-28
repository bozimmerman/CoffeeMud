package com.planet_ink.siplet.support;

public class MXPEntity implements Cloneable
{
    private String name="";
    private String definition="";
    
    public MXPEntity(String theName, String theDefinition)
    {
        super();
        name=theName;
        definition=theDefinition;
    }
    public String getName(){return name;}
    public String getDefinition(){return definition;}
    public void setDefinition(String newDefinition){definition=newDefinition;}
}
