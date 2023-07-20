# rbhop-MC Mod
An implementation of bhop in Minecraft forge 1.20.1 based on the [roblox bhop](https://www.roblox.com/games/5315046213) physics engine.

# rbhop-MC Server
There will be an MC server made for this soon, possibly also featuring [Simple Voice Chat](https://beta.curseforge.com/minecraft/mc-mods/simple-voice-chat/files/4271644).


(note, the commands below are not yet implemented!)
## Available Server-Side Commands:
```yaml
User Commands:

  - name: /rtv
    description: Rocks the vote, casts a vote to change the map.
  
  - name: /revoke
    description: Revokes the vote to change a map.
  
  - name: /ping
    description: Shows your ping to the server.
  
  - name: /hide
    description: Hides other players.
  
  - name: /show
    description: Shows other players.
  
  - name: /diff
    description: Shows the difference between you and the world record bot.
  
  - name: /wr
    description: Shows you the world record on the map.
  
  - name: /r
    description: Restarts your timer and places you at the beginning of the map.


Mod Commands:

  - name: /bl:
    description: Blacklists a player, disallowing them from setting world records.

  - name: /removetime
    description: Removes a time manually from the database.

```

## Available Client-Side Commands:
```yaml
General Commands:

  - name: /gauge
    description: Shows how good your strafing is on a gauge

  - name: /showkeys
    description: Shows your keys.

  - name: /jhud
    description: Shows general information about your run.

  - name: /hit
    description: Shows where you are predicted to land based on your velocity.

  - name: /sync
    description: Shows how good sync is between keyboard and mouse movement.

  - name: /sens
    description: Changes your sensitivity.

  - name: /fov
    description: Changes your field of view. 


Singleplayer Commands:

  - name: /buildview
    description: enables you to see trigger blocks.
```
