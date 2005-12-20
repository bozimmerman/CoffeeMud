//Lemming.js
//a sample MOB class to demonstrate Javascript class creation.
//to load this class from the command line, enter:
//LOAD MOB /resources/jscript/Lemming.js
//then enter:
//CREATE MOB Lemming
//
//extends com.planet_ink.coffee_mud.MOBS.StdMOB
//do not change the above line -- it's actual code!

// just like in all other cm classes, an ID method must be defined
// and the value returned must be the class name.
function ID(){return "Lemming";}

// since we don't have constructor access to set our initial fields,
// we must use the newInstance() method.
function newInstance()
{
	var lemm=this.super$newInstance();
	lemm.setName("a lemming");
	lemm.setDisplayText("a lemming is waiting to commit suicide");
	return lemm;
}

// Define a shortcut to our libraries
var lib=Packages.com.planet_ink.coffee_mud.core.CMLib;
// and define a countdown variable to kill off our lemming.
var countdown=10;


// here is our tick method.  Here we can wait for our countdown to
// complete and then kill off our poor lemming by calling
// the appropriate library method.
function tick(host,tickID)
{
	if(!this.amDead())
	{
		countdown--;
		if(countdown<=0)
		{
			lib.combat().postDeath(null,this,null);
			countdown=10;
		}
	}
    return this.super$tick(host,tickID);
}

// now we get creative.  Let's capture the death message posted in
// the tick method, and change the text around a bit.
function okMessage(host,msg)
{
	if((msg.source()==this)&&(msg.othersMessage()!=null))
	{
		var x=msg.othersMessage().indexOf('is DEAD!!!');
		if(x>=0)
		{
			var newmsg=msg.othersMessage().substring(0,x);
			newmsg+="jumps off a cliff!!!";
			newmsg+=msg.othersMessage().substring(x+10);
			msg.setOthersMessage(newmsg);
		}
	}
	return this.super$okMessage(host,msg);
}