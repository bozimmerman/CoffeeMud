package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class BribeGateGuard extends StdBehavior
{
	private Hashtable partials=new Hashtable();

	public String ID(){return "BribeGateGuard";}
	public Behavior newInstance(){	return new BribeGateGuard();}

	private int price()
	{
		return getVal(getParms(),"price",5);
	}

  private String gates()
  {
    return ID()+getVal(getParms(),"gates","General");
  }

	int tickTock=0;
  Vector paidPlayers=new Vector();
	
	private int findGate(MOB mob)
	{
		if(mob.location()==null) return -1;
		if(!mob.location().isInhabitant(mob))
			return -1;
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			if(mob.location().getRoomInDir(d)!=null)
			{
				Exit e=mob.location().getExitInDir(d);
				if(e.hasADoor())
					return d;
			}
		}
		return -1;
	}
	
	private Key getMyKeyTo(MOB mob, Exit e)
	{
		Key key=null;
		String keyCode=e.keyName();
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item item=mob.fetchInventory(i);
			if((item instanceof Key)&&(((Key)item).getKey().equals(keyCode)))
			{
				key=(Key)item;
				break;
			}
		}
		if(key==null)
		{
			key=(Key)CMClass.getItem("StdKey");
			key.setKey(keyCode);
			mob.addInventory(key);
		}
		return key;
	}

  private void payment(Coins given, MOB mob)
  {
    // make a note in the journal
    Coins item=(Coins)CMClass.getItem("StdCoins");
    int newNum=given.numberOfCoins();
    newNum+=getBalance(mob);
    item.setNumberOfCoins(newNum);
    delBalance(mob);
    writeBalance(item, mob);
  }

  private boolean checkBalance(int charge, MOB mob)
  {
    // Does this MOB have the cash for the charge?
    if(getBalance(mob)>charge)
      return true;
    return false;
  }

  private int getBalance(MOB mob)
  {
    // return the balance in int form
		Vector V=ExternalPlay.DBReadJournal(gates());
		Vector mine=new Vector();
    int balance=0;
		for(int v=0;v<V.size();v++)
		{
			Vector V2=(Vector)V.elementAt(v);
			if(((String)V2.elementAt(1)).equalsIgnoreCase(mob.name()))
				mine.addElement(V2);
		}
		for(int v=0;v<mine.size();v++)
		{
      Vector V2=(Vector)mine.elementAt(v);
      String fullName=((String)V2.elementAt(4));
      if(fullName.equals("COINS"))
      {
        Coins item=(Coins)CMClass.getItem("StdCoins");
				if(item!=null)
				{
					Generic.setPropertiesStr(item,((String)V2.elementAt(5)),true);
					item.recoverEnvStats();
					item.text();
        }
        balance+=item.numberOfCoins();
      }
    }
    return balance;
  }
  
  private void charge(int charge, MOB mob)
  {
    // update the balance in the journal
    Coins item=(Coins)CMClass.getItem("StdCoins");
    int newNum=getBalance(mob);
    newNum-=charge;
    if(newNum>0)
    {
      item.setNumberOfCoins(newNum);
      delBalance(mob);
      writeBalance(item, mob);
    }
    else
    {
      delBalance(mob);
    }
  }

  private void delBalance(MOB mob)
  {
    // kill the journal entries for that mob
		Vector V=ExternalPlay.DBReadJournal(gates());
		Vector mine=new Vector();
    int balance=0;
		for(int v=0;v<V.size();v++)
		{
			Vector V2=(Vector)V.elementAt(v);
			if(((String)V2.elementAt(1)).equalsIgnoreCase(mob.name()))
				mine.addElement(V2);
		}
		for(int v=0;v<mine.size();v++)
		{
      Vector V2=(Vector)mine.elementAt(v);
      String fullName=((String)V2.elementAt(4));
      if(fullName.equals("COINS"))
      {
        ExternalPlay.DBDeleteJournal(((String)V2.elementAt(0)),Integer.MAX_VALUE);
      }
    }
  }
  
  private void writeBalance(Coins balance, MOB mob)
  {
    // write an entry for that mob
    ExternalPlay.DBWriteJournal(gates(),mob.name(),CMClass.className(balance),"COINS",Generic.getPropertiesStr(balance,true),-1);
  }

	public static int getVal(String text, String key, int defaultValue)
	{
		text=text.toUpperCase();
		key=key.toUpperCase();
		int x=text.indexOf(key);
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='=')&&(!Character.isDigit(text.charAt(x))))
					x++;
				if((x<text.length())&&(text.charAt(x)=='='))
				{
					while((x<text.length())&&(!Character.isDigit(text.charAt(x))))
						x++;
					if(x<text.length())
					{
						text=text.substring(x);
						x=0;
						while((x<text.length())&&(Character.isDigit(text.charAt(x))))
							x++;
						return Util.s_int(text.substring(0,x));
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return defaultValue;
	}

	public static String getVal(String text, String key, String defaultValue)
	{
		text=text.toUpperCase();
		key=key.toUpperCase();
		int x=text.indexOf(key);
		while(x>=0)
		{
      int y=text.indexOf("=", x);
      int z=text.indexOf(" ", y);
      if(z<0)
        return text.substring(y+1);
      if((y>0)&&(z>y+1))
        return text.substring(y+1,z-1);
      return defaultValue;
		}
		return defaultValue;
	}

	public boolean okAffect(Environmental oking, Affect affect)
	{
		if(!super.okAffect(oking,affect)) return false;
		MOB mob=affect.source();
		if(!canFreelyBehaveNormal(oking)) return true;
		MOB monster=(MOB)oking;
		if(affect.target()==null) return true;
		if(!Sense.canBeSeenBy(affect.source(),oking))
			return true;
		if(mob.location()==monster.location())
		{
			if(affect.target() instanceof Exit)
			{
				if((affect.targetMinor()!=Affect.TYP_CLOSE)&&(!affect.source().isMonster()))
        {
          if(checkBalance(price(), mob))
          {
            return true;
          }
          else
          {
            FullMsg msgs=new FullMsg(monster,mob,Affect.MSG_NOISYMOVEMENT,"<S-NAME> won't let <T-NAME> through there.");
            if(monster.location().okAffect(msgs))
            {
              monster.location().send(monster,msgs);
              ExternalPlay.quickSay(monster,mob,"I'll let you through here if you pay the fee of "+price()+".",true,false);
              return false;
            }
          }
        }
        return true;
			}

    }
    return true;
  }

	public void affect(Environmental affecting, Affect affect)
	{
		super.affect(affecting,affect);
		MOB source=affect.source();
		if(!canActAtAll(affecting)) return;
		
		MOB observer=(MOB)affecting;
		if((affect.sourceMinor()==Affect.TYP_ENTER)
		&&(!affect.amISource(observer))
		&&(Sense.canSenseMoving(affect.source(),affecting))
		&&(!affect.source().isMonster()))
		{
			// check if the affect.source() has paid enough.  if so, add them to the paid list
      if(checkBalance(price(), source))
      {
        paidPlayers.addElement(source);
      }
		}
		else
		if((affect.sourceMinor()==Affect.TYP_LEAVE)
		&&(!affect.amISource(observer))
		&&(!affect.source().isMonster()))
		{
      if(paidPlayers.contains(source))     // the player that the guard acknowledged as paid has now left
      {
        paidPlayers.remove(source);
        if((affect.tool()!=null)&&(affect.tool() instanceof Exit))
        {
          Exit exit=(Exit)affect.tool();
          int dir=findGate(observer);
          Exit e=observer.location().getExitInDir(dir);
          if(exit.name()==e.name()) // the player is walking through the gate.  NOW we charge their balance
          {
            charge(price(),source);
          }
        }
      }
		}
		else
		if(affect.amITarget(observer)
		   &&(!affect.amISource(observer))
		   &&(affect.targetMinor()==Affect.TYP_GIVE)
		   &&(affect.tool()!=null)
		   &&(affect.tool() instanceof Coins))
		{
			payment((Coins)affect.tool(),affect.source());
      ExternalPlay.quickSay(observer,source,"Thank you very much.",true,false);
      if(getBalance(source)>0)
        ExternalPlay.quickSay(observer,source,"I'll hang on to the remaining "+getBalance(source)+" for you",true,false);
		}
	}

	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Host.MOB_TICK) return;
		if(!canFreelyBehaveNormal(ticking)) return;
		MOB mob=(MOB)ticking;
		int dir=findGate(mob);
		if(dir<0) 
    {
      ExternalPlay.quickSay(mob,null,"I'd shut the gate, but there isn't one...",false,false);
      return;
    }
		Exit e=mob.location().getExitInDir(dir);
    tickTock++;
		if(tickTock>2)
		{
			tickTock=0;
      boolean nightTime=(mob.location().getArea().getTODCode()==Area.TIME_NIGHT);
      if(nightTime)
      {
        if((!e.isLocked())&&(e.hasALock()))
        {
          if(getMyKeyTo(mob,e)!=null)
          {
            FullMsg msg=new FullMsg(mob,e,Affect.MSG_LOCK,"<S-NAME> lock(s) <T-NAME>.");
            if(mob.location().okAffect(msg))
              ExternalPlay.roomAffectFully(msg,mob.location(),dir);
          }
        }
      }
      else
      if(e.isLocked())
      {
        if(getMyKeyTo(mob,e)!=null)
        {
          FullMsg msg=new FullMsg(mob,e,Affect.MSG_UNLOCK,"<S-NAME> unlock(s) <T-NAME>.");
          if(mob.location().okAffect(msg))
            ExternalPlay.roomAffectFully(msg,mob.location(),dir);
        }
      }
      if((e.isOpen())&&(paidPlayers.isEmpty()))
      {
        FullMsg msg=new FullMsg(mob,e,Affect.MSG_CLOSE,"<S-NAME> close(s) <T-NAME>.");
        if(mob.location().okAffect(msg))
          ExternalPlay.roomAffectFully(msg,mob.location(),dir);
      }
      if((!e.isOpen())&&(!paidPlayers.isEmpty()))
      {
        FullMsg msg=new FullMsg(mob,e,Affect.MSG_OPEN,"<S-NAME> open(s) <T-NAME>.");
        for(int i=0;i<paidPlayers.size();i++)
        {
          ExternalPlay.quickSay(mob,(MOB)paidPlayers.elementAt(i),"I still have that "+getBalance((MOB)paidPlayers.elementAt(i))+" from before if you're heading through",true,false);
        }
        if(mob.location().okAffect(msg))
          ExternalPlay.roomAffectFully(msg,mob.location(),dir);
      }

    }
  }

}