# Critical Strike

A Minecraft mod that adds chance-based critical hit mechanics for melee and ranged combat, complete with flashy visual and audio feedback. Replicates Critical Hit mechanics from Minecraft Dungeons.

## Features

### Chance-Based Critical Strikes
- **RNG-based critical hit system** that replaces vanilla jump criticals
- **Melee critical strikes** - Weapons have a chance to deal bonus damage on hit
- **Ranged critical strikes** - Arrows and projectiles can critically hit targets
- **Configurable mechanics** - Toggle vanilla jump crits and customize critical chance/damage
- **Batching option** - Cache critical chance calculations per tick for performance

### Particle and Sound Effects
- **Fancy particle effect** are shown on critical strikes:
  - **Sparkle** - Burst of small sparkles radiating from impact
  - **Skull** - Floating skull particles that ascend and fade
  - **Circle** - Expanding animated ring at the center of impact
  -  with customizable colors
- **Dynamic sound effects** - Two critical hit sound variants with randomized pitch
- **Configurable volume** - Separate volume controls for melee and ranged crits

## Content

### Attributes
- **Critical Hit Chance** (`critical_strike:chance`) - Determines probability of landing a critical hit
  - Base innate bonus: +5%
  - Increased by enchantments and status effects
- **Critical Hit Damage** (`critical_strike:damage`) - Multiplier for critical hit damage
  - Base innate bonus: +50% (1.5x damage)
  - Increased by enchantments and status effects

### Enchantments
- **Critical Hit** (Max level 5) - Increases critical strike chance by +4% per level
- **Critical Impact** (Max level 5) - Increases critical damage by +10% per level
- Both enchantments work on swords, axes, bows, crossbows, and other weapons

### Status Effects
- **Critical Hit Effect** - Beneficial potion effect that boosts critical chance (+5% per level)
- **Critical Impact Effect** - Beneficial potion effect that boosts critical damage (+10% per level)
- Available as potions, splash potions, lingering potions, and tipped arrows