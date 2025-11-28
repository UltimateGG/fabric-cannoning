# Fabric Cannoning

Bringing factions cannoning to Fabric 1.20+.


### What it is
A Fabric mod that patches different aspects of the game to allow cannons to work (255 stackers, 320 stackers, rev nukes, etc.).

It is also what used to be called a "cannon jar" and contains optimizations to allow massive cannons to fire while
the server remains at a consistent 20 TPS. A lot of performance comes from the amazing work in the [Lithium mod](https://github.com/CaffeineMC/lithium).

Thanks to the awesome Fabric platform, the mod runs on the singleplayer server too, meaning you can
just boot up a singleplayer world and have a cannoning server.

### TODO/WIP
- Pistons used to push over 1 block (1 block + 2px)
- Ladders hitbox causing exposure differences (Same with webs)

### Config
See [Config.java](./src/main/java/com/evofaction/fc/Config.java) for docs and the latest config options.

Not every single patch is configurable. See list below for what this mod changes.

### Fixes
- Disable the initial randomness when TNT is ignited/dispensed 
- Disable water moving TNT (bubble columns still work)
- TNT fuse changed to be 1 tick longer to mimic how it was in 1.8
- Explosions are now created in the center of TNT (`0.98F / 2.0F`) instead of feet (this is how it was in Paper 1.8)
- TNT & falling blocks' exposure are now calculated from their center, not feet (this is how it was in Paper 1.8)
- Old floating point math - constants like gravity (Intended to be `-0.04D` in the old game got compiled to something like
`-0.03999999910593033D`). The mod patches TNT and falling blocks to use the old constants so even the most precise cannons should
take the exact same path, down to the millionth
- Fixes the default triangles order to always be YXZ (Plus toggleable e/w patch)
- Fixes carpet comps by making Block36 not block exposure
- Overrides exposure for ladders to use old shape
- Fixes old double piston extenders (e.g. used in sand comps). Allows two extended pistons retracting in the same tick to be pulled back
- Piston entity pullback fix - In 1.8 pistons only pushed, they did not *pull* entities. In 1.20, they do (Imagine making a hook shape with slime
blocks). This behavior is preserved but entities glitched in the moving block or piston head will not be pulled backwards. This
fixes things like sand comps so that it does not push and pull sand back and forth in the piston head.

### Additional Options
- Togglable East/west cannoning patch
  - Off by default because of 1. matching vanilla 1.8 behavior and 2. my philosophy 
- Water protected redstone
- Protection blocks (iron blocks) for testing cannons
- One push webs option
  - Sand is pushed out of webs in one go (vs multiple pulses). This is how it is in even newer versions like 1.21.10

### Optimizations
- Merged TNT (Preserves OOE)
- Exposure cache (Paper spigot's old optimize-explosions flag)
- Plus all of Lithium's optimizations. They have explosion and entity collision optimizations. 

### Compatibility Disclaimer
This project is **not** intended to create 100% parity with all 1.8 cannons/redstone.
Some things from 1.8 **will** be broken, and players will need to update their cannons.
You won't be able to just import any cannon and expect it to work.
That is part of the fun and skill of it. Also, this is the price to pay for all the new features and
innovation allowed by the new versions of the game. The most notable breaking changes
will be listed [here](./notes.md) for cannoners.

This is also **not** a complete factions mod. It is purely for fixing essential cannon functions.
I am planning to make a separate mod for a "factions core" that would contain things like preventing pearling through webs,
adding old sponges, etc.

I am taking a more vanilla approach to this, as that is what made factions so magical in
the first place. Letting the game just be what it is, using the mechanics we have and
pushing them to their limits.


### Suggested Additional Mods
- [CannonDebug](https://github.com/UltimateGG/CannonDebug-Fabric/releases) for your test server, which I have ported to 1.20.1
- [FerriteCore](https://modrinth.com/mod/ferrite-core) for memory optimization

#### Requires Lithium

<br/>

This project is licensed under the GNU General Public License v3.0
