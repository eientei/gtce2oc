[![Get on curse](https://cf.way2muchnoise.eu/versions/Get%20on%20curse%20for_gtce2oc_all.svg)](https://minecraft.curseforge.com/projects/gtce2oc)



GTCE2OC
=======

GregTechCE integration with OpenComputers

Provides:
- Electrical integration of OpenComputers blocks into GTCE network
- Lua API for read-only interactions with IEnergyContainers
- GTCE machines for OpenComputers config
- Recipeset for OpenComputers using GTCE items


Installation
============

Simply drop the jar (either from curseforge or local build) into mods directory of your 1.12.2 minecraft instance

It, being a coremod should instrument OpenComputers classes on each launch and on first one create a
`$MINECRAFT_HOME/config/opencomputers/gregtechce.recipes ` file which can be included in
`$MINECRAFT_HOME/config/opencomputers/user.recipes` using

    include file("gregtechce.recipes")
    
(preferably commenting out unneeded recipe sets)