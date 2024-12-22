# Playerkits

## Description:

This project playerkits is aiming to set up and deliver rewards / kits to players in different categories.

---

## Installation:
- You need a Hibernate supported database (for example PostGreSQL) and the plugin on a paper server (1.21.1)
- You need Java 21 or higher, best is 21 lts
- optional is Vault for economy
---
## Setup:
After the plugin is enabled and the database connection works, you can create kits in game.
1. Check if you have sufficient permissions, if not, annoy your administrator to do so (Check out "Permissions" here)
2. Get your kit items in your inventory (if you don't know how, check out the title "Inventory")
3. Start with `/kit create <name>` and replace name with the wanted kit name.
When the setup starts, **don't** use slash commands, just write what is expected / what you want to be enabled. You can also correct mistakes with going back and forth in the setup
4. When you are done, check if everything fits with the `/kits` command or give yourself a kit with `/kit give <player> <kit>`
5. You can also delete kits, check out "Table of features"

Here is an **example**:
![img.png](assets/setup-example.png)
Check out "Kinds of kits" to know what you can do.
---
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
---
### Inventory
#### Kit space
To set rewards in a kit, you need to know how:
![assets/kit-preview.png](assets/kit-preview.png)

(Right-click a created kit to see a preview)
The hotbar and the inventory of the setup player is used as kit creation space. 
The row where the red dye is at, is **reserved** and can't be used.
---
#### Kits menu
![img.png](assets/kit-overview.png)

---
## Table of features

| Permission                    | Command                    | Usage                    |
|-------------------------------|----------------------------|--------------------------|
| playerkits.command.kit.create | `/kit create <name>`       | Create a new kit (setup) |
| playerkits.command.help       | `/kit help [query]`        | Shows the help menu      |
| playerkits.command.kits       | `/kits`                    | Open the kits overview   |
| playerkits.command.give       | `/kit give <player> <kit>` | Give a player a kit      |
| playerkits.command.delete     | `/kit delete <name>`       | Delete a Kit             |







