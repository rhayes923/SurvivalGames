# SurvivalGames
A Spigot minigame plugin that allows players to play the iconic Survival Games. 

A player can specify a world name when creating a new game. The world will load if it exists
in the server directory. When loading, the world will look for valid spawns, which are locations
that have signs on them that say 'spawn'. The amount of these signs in a 10x10 chunk area from the
spawn chunk will determine the amount of players that can play on the specified world. Once the 
queue is full, the game will start.

### Features
- Randomized loot in every chest
- Game events, including chest refills and a shrinking world border
- Custom scoreboard and end game results
- Spectating after death
- And more!

### Commands
- /sg start \<world> - Creates a new game using the specified world name
- /sg join - Joins the queue for an existing game
- /sg leave - Leaves the queue or leaves the active game
- /sg forcestart - Starts the game with current players in the queue

This plugin was made for fun.
