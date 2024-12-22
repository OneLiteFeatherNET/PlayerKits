This project playerkits is aiming to set up and deliver rewards / kits to players in different categories.

## Installation:
You need a Hibernate supported database and the plugin on a paper server, optional is Vault
for economy

## Setup:

### Kinds of kits
To set up a kit, you need to know there are several types of kits, for example:
- The one-time kit
  - This kit can only be claimed once for each player, if this feature is enabled / true
- The first-time kit
  - This kit can only be claimed when the player joins for the first time (get it before it is gone!)
- Kit with a price
  - This kit can only be claimed when an economy plugin (vault) is installed and the player mets the required amount
  - Every kit can have a price
- Kit with cooldown
  - This kit can only be claimed again when the cooldown is depleted / gone.
  - Every kit can have a cooldown
- (In)visible kit
  - If you enable invisibility of the kit, you can't claim it in the player menu and only give it manually to the player

### Inventory
To set rewards in a kit, you need to know how:


## Table of features

| Permission                    | Command                    | Usage                    |
|-------------------------------|----------------------------|--------------------------|
| playerkits.command.kit.create | `/kit create <name>`       | Create a new kit (setup) |
| playerkits.command.help       | `/kit help [query]`        | Shows the help menu      |
| playerkits.command.kits       | `/kits`                    | Open the kits overview   |
| playerkits.command.give       | `/kit give <player> <kit>` | Give a player a kit      |
| playerkits.command.delete     | `/kit delete <name>`       | Delete a Kit             |







