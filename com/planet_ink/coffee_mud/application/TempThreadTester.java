package com.planet_ink.coffee_mud.application;
import java.util.*;
import java.io.*;
import java.net.*;
import java.util.regex.*;
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
 * Work in progress.
 * @author Bo Zimmerman
 *
 */
public class TempThreadTester 
{
	static volatile com.planet_ink.coffee_mud.core.collections.SVector<Integer> V=new com.planet_ink.coffee_mud.core.collections.SVector<Integer>(); 
	static volatile long total=0;
	static java.util.Random rand=new java.util.Random(System.currentTimeMillis());
	
	public static void main(String[] args)
	{
		Runnable r0=new Runnable() {
			public void run() {
				while(true)
				{
					try{Thread.sleep(rand.nextInt(10));}catch(Exception e){}
					Integer I=Integer.valueOf(rand.nextInt(10));
					V.add(I);
					synchronized(rand) { total+=I.intValue();}
				}
			}
		};
		Runnable r1=new Runnable() {
			public void run() {
				while(true)
				{
					try{Thread.sleep(rand.nextInt(10));}catch(Exception e){}
					Integer I=V.firstElement();
					if(V.remove(I)){ synchronized(rand) { total-=I.intValue();}}
				}
			}
		};
		Runnable r2=new Runnable() {
			public void run() {
				while(true)
				{
					try{Thread.sleep(rand.nextInt(5));}catch(Exception e){}
					long tt=0;
					for(Integer I : V)
						tt+=I.intValue();
					System.out.println(tt+"/"+total);
				}
			}
		};
		new Thread(r0).start();
		new Thread(r2).start();
		new Thread(r2).start();
		new Thread(r1).start();
		try{Thread.sleep(30000);}catch(Exception e){}
	}
}
