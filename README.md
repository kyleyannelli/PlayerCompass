# PlayerCompass
### Get a compass HUD of the players in your server, without setting anything up.

A very simple plugin inspired by [CalebCompass](https://github.com/JamesThack/calebcompass).
Supports up to 306 players before marker overlaps. Default range is 1000 blocks. Your spawn point is shown as a red house icon;
There is NO config file for this plugin. You simply put it in your `plugins` folder, and it will work for everyone in the server.
You are told your assigned marker color upon joining the game. Characters and colors for markers are not persistent across restarts. Look at the commands section for further info.

## Commands

#### /pcc
Shows the current players, and their associated colors.

## Missing Behavior
###### I do not play Minecraft frequently. So, I won't be active on this plugin. These are mainly ideas for others to fix, if they'd like.

Currently, the plugin is quite dumb. Meaning, if locations overlap in the compass, it will show the one that came last in the game loop.
I will potentially make the behavior to show the closest one if overlapping, or flash between all players in the overlap.

## This Plugin Does One Thing

I really enjoyed the look and feel of CalebCompass.
So, I was going to fork it to add the ability to track other players. However, it contains things I will not use, and I thought the there would be no point for my use case given the extra depdencies.
CalebCompass, although stale, has the ability to track waypoints and integrate with other plugins. If you are looking for that, go check it out.
