
  

  

# Visage

  

Advanced and fully automated cheat-protection to prevent combat cheating on the server

  

## VERSION COMPATIBILITY

  

- Visage is compatible with the **Spigot**, **Craftbukkit** and the **PaperSpigot** from the version **1.8** to the **1.16.X**

- Visage **does not** and will **never** support **1.7** or earlier versioning.

  

## VISAGE SUPPORT + REQUESTS

  

- Do not use a review to report bugs/errors. Those will be **ignored**!

- For feature requests and reporting erros, bugs and bypasses, utilize the Github issues task.

- For simple questions about Visage, ask in the resource discussion.

- For piracy and resource ownership related issues, PM SquareCode.

  

## WHAT IS VISAGE

Visage is an intelligent anti-cheat solution that protects your server from hackers. We are a complete community anticheat. Everyone is welcome to contribute his part. We have made the source code available on @SquareCode's gitserver. If you are interested in making changes to the anticheat you can register there and make a pull request.

  

## WHAT CAN VISAGE DO

Currently, Visage protects your server from the following hacks:

  
**Combat**
- Reach
- AttackRaytrace
- CombatAnalytics
- Heuristics
- KillAura
- NoSwing
- FastBow
- AimAssist

## FEATURES

  

- **CONFIGURABLE** - Visage has a large config file. In it the prefix, general messages, the individual permissions, all checks individually and other settings can be made.

- **AUTOMATED** - Visage has a fully automated punishment system. After Visage is certain a player is hacking (the lenience can be configured), the player is automatically kicked.

- **VERBOSE** - Visage can give detailed information about suspected players to staff. Staff with op are notified whenever a player is suspected of hacking.

- **MYSQL IMPLEMENTATION** - Visage has a full mysql implementation for log storaging, ban storaging, all time kicks and a global verbose mode.

- **GUI CONTROL** - Visage has a GUI to modify the individual checks and to view the global MySQL stats.

- **LOGSYSTEM** - Visage automatically creates a log file about each player who is automatically kicked.

- **ANTIVPN** - Visage automatically removes players from the game who use a VPN or proxy service. The automatic command and if the check should be on at all can be set in the config.yml.

- **OPENSOURCE** - This project is completely open source. The source code can be viewed and inspected on Github.

  

## DEPENDENCIES

  

- **Java** - To run Visage, you should be using a Spigot server with Java 8 (Java 7 not supported).

- **Server Version** - Visage 1.0+ supports Spigot versions 1.8-1.16.4

- **Packet handler** - Visage 1.0+ needs Atlas by @funkemunky

  

## PERMISSIONS
Use permission visage.command.* for all commands in one permission.
Use permission visage.admin.* to bypass the entire anticheat and receive admin notification in one permission

**visage.command.gui**:
- description: Permission for command visage gui
- children visage.command.*
- default: op

**visage.command.tps**:
- description: Permission for command visage tps
- children: visage.command.*
- default: op

**visage.command.enabled**:
- description: Permission for command visage enabled
- children: visage.command.*
- default: op

**visage.command.verbose**:
- description: Permission for command visage verbose
- children: visage.command.*
- default: false

**visage.command.notify**:
- description: Permission for command visage notify
- children: visage.command.*
- default: op

**visage.command.debug**:
- description: Permission for command visage debug
- children: visage.command.*
- default: op

**visage.command.kicks**:
- description: Permission for command visage kicks
- children: visage.command.*
- default: op

**visage.command.check**:
- description: Permission for command visage checks
- children: visage.command.*
- default: op

**visage.command.broadcast**:
- description: Permission for command visage broadcast
- children: visage.command.*
- default: op

**visage.command.logs**:
- description: Permission for command visage logs
- children: visage.command.*
- default: op

**visage.bypass**:
- description: Permission for bypass all checks of visage
- children: visage.admin.*
- default: false

**visage.notification**:
- description: Permission to receive notifications from visage
- children: visage.admin.*
- default: op


## COMMANDS


- /visage gui

- /visage tps>

- /visage enabled

- /visage verbose

- /visage notify [message]

- /visage debug [moduleName]

- /visage logs [playerName] [length]

- /visage kicks [playerName]

- /visage broadcast [message]

- /visage check/analyze [playerName]