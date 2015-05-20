StashYourXP
===========

This is a plugin for the 1.8.3 Bukkit API. It requires a CraftBukkit or Spigot server.
Spigot can be found at [spigotmc.org](http://spigotmc.org).

This plugin provides a way for players to store their XP in bottles (using the [Bottle o' Enchanting](http://minecraft.gamepedia.com/Bottle_o%27_Enchanting) item). Players can construct a simple Experience Bottler by
placing a gold pressure plate on top of a furnace. They will then (optionally) receive a book containing further instructions.

The Experience Bottler requires a configurable fuel item to run, at a (currently hardcoded) rate of 1 item per bottle.
By default, the fuel item is Glowstone Dust but other items can be used (I recommend Redstone Dust as a less expensive alternative).

By default the amount of XP taken from the player per bottle is about equal with the amount players will get back per bottle consumed.
A config option exists to introduce loss into this balance, so that there is an additional cost involved with bottling experience.

Compilation
-----------

This plugin has a Maven 3 pom.xml and uses Maven to compile. Dependencies are 
therefore managed by Maven. You should be able to build it with Maven by running

    mvn package

A jar will be generated in the target folder. For more information about the Maven
build system, see http://maven.apache.org/

Note that this assumes you have successfully run the Spigot BuildTools.jar to install the appropriate
dependencies into your local Maven repository. Instructions on how to acquire and run BuildTools
[can be found here](http://www.spigotmc.org/wiki/buildtools/).

Credits
-------

This plugin was initialized using [the BukkitPlugin-archetype](https://github.com/SagaciousZed/BukkitPlugin-archetype)
by SagaciousZed.
