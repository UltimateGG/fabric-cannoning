# Differences
This document lists the major differences for 1.8 vs 1.20 cannoning and things that I found while making this mod.


### Exposure
1.8 used a different method of exposure calculations. Things like a standing sign would completely block exposure, even though you can walk through them.
Mostly, the block outline was the same hitbox as the explosion rays hit. For example, open fence gates you can walk through, but used to block all explosion rays.
Same for cobwebs. That is not the case anymore.

Basically, new MC uses the collision hitbox for exposure. It is more consistent. In general, whatever the player collides with,
your exposure rays will as well. This is a difference to note. Whatever the player can pass through, exposure now can as well, like cobwebs.


### Sand Comps
In 1.8, slab/carpet/fence comps were much more glitchy. Sand would slightly shift back and forth by fractions of a block (0.49-0.51)
every 3 ticks. It would also fall through the block it was in, which is why cannons required ladders under the slabs to suspend the sand.
In 1.20, the sand stays perfectly where it was dropped **(TODO: recheck after our fix)**, and sits ontop of the collision box of the block its on.
For example in 1.8, sand would fall through a slab comp and stay on the ladder at .0, but in 1.20 it will stay ontop of the slab, at 0.5.
Ladders are still needed under carpet to keep it from popping off. Ladders are also still needed under fence gates since they have no collision,
so the sand would fall through in that case.

### Redstone Update Order
Redstone update order has never been consistent between servers due to various optimizations.
It is different in 1.20, so some very complex cannons that implicitly rely on the old order may
behave differently. For most cannons, it should not matter.

#### Triggered Dispensers Bug 
The biggest thing this affects  is dispensers. It was and still is very buggy behavior for (mainly bud powered) dispensers. They get
stuck in a "triggered" state and won't dispense TNT after the first shot. This happens in both versions, but
the location and number of the dispensers that get bugged may be different from 1.8. Even in the old
game this behavior was inconsistent and buggy, and any cannon relying on it would have been glitchy as
well, therefore the old order will not be supported.
