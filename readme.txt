1. Go to your coffee mud directory and edit the makemud.bat, 
the first line will be something like:
SET JAVACPATH=C:\j2sdk1.4.1_02\bin\javac 
This might be different for you depending on where your java 
development package is installed. I have the latest one that 
Sun has and I installed it on C drive.

2. Save the bat file.

3. Run the bat file by double clicking on it. This will compile 
the mud, making all the .class files. You will prolly notice you 
only had the .java ones to begin with.

4. Go back to the mud directory and edit the 'mud.bat' and modify
this file to read what your mud is called, I called mine starbucks: 
java -Xms65535000 -Xmx115535000 com.planet_ink.coffee_mud.application.MUD "Starbucks"

5. Double click on the mud.bat and a DOS prompt will start running and
initialize everything for you.

6. Open up your favorite mud client, if you don't have one you can 
click on start>run and type 'telnet' in the prompt and press OK.

7. To connect to your local machine you would type in the host name or 
ip address section 'localhost' and connect to port 4444 by default. The 
port can be changed in the coffeemud.ini file with lots of other stuff.
