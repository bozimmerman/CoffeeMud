@echo off

set classpath_ = %classpath%
set classpath  = %classpath%;.;c:\prog\jdk130\lib\dt.jar;c:\prog\jdk130\lib\tools.jar

javac -d c:\mud com\planet_ink\coffee_mud\Abilities\*.java
javac -d c:\mud com\planet_ink\coffee_mud\Abilities\interfaces\*.java
javac -d c:\mud com\planet_ink\coffee_mud\application\*.java
javac -d c:\mud com\planet_ink\coffee_mud\Behaviors\*.java
javac -d c:\mud com\planet_ink\coffee_mud\CharClasses\*.java
javac -d c:\mud com\planet_ink\coffee_mud\commands\*.java
javac -d c:\mud com\planet_ink\coffee_mud\commands\sysop\*.java
javac -d c:\mud com\planet_ink\coffee_mud\db\*.java
javac -d c:\mud com\planet_ink\coffee_mud\deities\*.java
javac -d c:\mud com\planet_ink\coffee_mud\Exits\*.java
javac -d c:\mud com\planet_ink\coffee_mud\interfaces\*.java
javac -d c:\mud com\planet_ink\coffee_mud\Items\*.java
javac -d c:\mud com\planet_ink\coffee_mud\Items\Weapons\*.java
javac -d c:\mud com\planet_ink\coffee_mud\Items\MiscMagic\*.java
javac -d c:\mud com\planet_ink\coffee_mud\Items\Armor\*.java
javac -d c:\mud com\planet_ink\coffee_mud\Locales\*.java
javac -d c:\mud com\planet_ink\coffee_mud\MOBS\*.java
javac -d c:\mud com\planet_ink\coffee_mud\Races\*.java
javac -d c:\mud com\planet_ink\coffee_mud\service\*.java
javac -d c:\mud com\planet_ink\coffee_mud\StdAffects\*.java
javac -d c:\mud com\planet_ink\coffee_mud\telnet\*.java
javac -d c:\mud com\planet_ink\coffee_mud\utils\*.java

set classpath=%classpath_%