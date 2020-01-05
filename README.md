# WorldMaster
This is a spigot MC plugin with many usefull commands.

## Config
**Quotes are required for the text!**<br>
**welcome**: 'Text'<br>
If a player joins, the text "Welcome to 'Text'" will be shown.<br><br>
**disable_portals**: true or false<br>
If set to true, all portals will be disabled.<br>

## Commands
**Don't use quotes in the arguments that are in quotes!**<br><br>

**World**:<br>
*/world add "Name"*: Adds the world with the name "Name". **May produce lag!**<br>
*/world remove "Name"*: Removes the world with the name "Name".<br>
*/world renew "Name"*: Recreates the world with the name "Name" with the same seed.  **May produce lag!**<br>
*/world set_join_gamemode "Name" "Gamemode"*: Sets the join gamemode of the world with the name "Name" to "Gamemode".<br>
*/world set_permission "Name" enter "Permission"*: Sets the enter permission of the world with the name "Name" to "Permission". If "Permission" is "no_permission", the enter permission will be set to non permission.<br>
*/world set_permission "Name" build "Permission"*: Sets the build permission of the world with the name "Name" to "Permission". If "Permission" is "no_permission", the build permission will be set to non permission.<br>
*/world set_allowed_gamemodes "Name" "Gamemodes..."*: Sets the allowed gamemodes of the world with the name "Name". If "Gamemodes..." is "all_gamemodes", all gamemodes will be allowed.<br>
*/world set_time "Name" "Time"*: Sets the time of the world with the name "Name". "Time" can be a number or a preset string.<br>
*/world Set_difficulty "Name" "Difficulty"*: Sets the difficulty of the world with the name "Name".
*/world list*: Shows all available worlds.<br>
*/world tp "Name"*: Teleports the player who enterd the command to the world spawn point of the world with the name "Name".<br><br>

**Permission**:<br>
*/permission allow "Player" "Permission"*: Sets the permission "Permission" of the player "Player" to true. "Permission" can be custom [not shown in tab list].<br>
*/permission disallow "Player" "Permission"*: Sets the permission "Permission" of the player "Player" to flase. "Permission" can be custom [not shown in tab list].<br>
*/permission default "Player" "Permission"*: Sets the permission "Permission" of the player "Player" to the default value. "Permission" can be custom [not shown in tab list].<br><br>

**Elevator**:<br>
*/elevator make*: Gives the player who executed the command a selector. With this you can select blocks that should be part of the elevator.<br>
*/elevator create* "Name" "FloorHeight": Creates a elevator from the selected blocks with the name "Name". "FloorHeight" should be set to the acutal height of the elevator.<br>
*/elevator remove "Name"*: Removes the elevator with the name "Name".<br>
*/elevator add_floor "Name" "FloorName" "DoorPos" ["Permission"]*: Add the floor with the name "FloorName" to the elevator with the name "Name". "Permission" is optional and sets the permission for going on the floor. "Permission" can be custom [not shown in tab list].<br>
*/elevator remove_floor "Name" "FloorName"*: Removes the floor with the name "FloorName" from the elevator with the name "Name".<br>
*/elevator set_speed "Name" "speed"*: Sets the speed [in Blocks/s] of the elevator with the name "Name".<br>
*/elevator move "FloorName"*: Moves the nearest elevator to the command executer [max 5 blocks away] to the floor "FloorName" if the command executer has the permission for the floor.<br><br>

**Teleporter**:<br>
*/teleporter create "Name" "Icon"*: Creates a teleporter menu with the name "Name" and the icon "Icon".<br>
*/teleporter delete "Name"*: Removes the teleporter menu with the name "Name".<br>
*/teleporter give "Name"*: Gives the player who executed the command the teleporter menu with the name "Name".<br>
*/teleporter add "Name" "PositionName" "Slot"*: Adds the position with the name "PositionName" to the slot "Slot" of the teleporter menu with the name "Name".<br>
*/teleporter remove "Name" "Slot"*: Removes the position in the slot "Slot" of the teleporter menu with the name "Name".<br>
*/teleporter add_position "PositionName" "Icon" "WorldName" "Coords"*: Creates the position with the name "PositionName" and the icon "Icon". If someone clicks on the position in a teleporter menu, he will teleported to the coordinates "Coords" in the world with the name "World".<br>
*/teleporter remove_position "PositionName"*: Removes the position and all ocurents of the position with the name "PositionName".<br><br>

**Chunk loader**:<br>
*/chunk_loader*: Gives the player who executed the command a chunk loader block. If the player sets the block in a chunk it get force loaded, if he breaks the block it is no longer force loaded.<br><br>

**Inventory**:<br>
*/inventory list*: Lists all available inventory systems.<br>
*/inventory add "Name"*: Adds the inventory system with the name "Name".<br>
*/inventory remove "Name"*: Removes the inventory system with the name "Name".<br>
*/inventory list_world "Name"*: Lists all worlds where the inventory system with the name "Name" is used.<br>
*/inventory add_world "Name" "World"*: Adds the world with the name "World" to the inventory system with the name "Name".<br>
*/inventory remove_world "Name" "World"*: Removes the world with the name "World" form the inventory system with the name "Name".<br><br>

**Reload Config**:<br>
*/reload_config*: Reloads the config files of the plugin.<br>

## Permissions
*world*: Enables the use of /world "add", "remove", "renew" and "set_..."<br>
*permission*: Enables the whole command /permission<br>
*elevator*: Enables the use of /elevator "make", "create", "remove", "add_floor", "remove_floor" and "set_speed"<br>
*teleporter*: Enables the use of /teleporter "create", "delete", "add", "remove", "add_position" and "remove_position"<br>
*chunk_loader*: Enables the whole command /chunk_loader<br>
*reload_config*: Enables the whole command /reload_config<br>
*inventory*: Enables the whole command /inventory<br>
*build_spawn*: Emables the building in the spawn world<br>
*shop_owner*: Enables the creation of shops<br>

## Shop
1) Place normal single chest
2) Put the buy items in the first row [First 4 slots]
3) Put the sell items in the last row [First 4 slots]
4) Place a sign on the chest with the text [In 1 row]: [Shop]
5) But more items for selling in the chest
6) [Optional] Take all items for buying out of the chest

The shop owner sees all items in the chest and the buyer sees the shop page
