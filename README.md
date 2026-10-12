# Tide-of-Duskveil-Nightfall
Add a dynamic and configurable threat to your minecraft world, increasing the difficulty of the game

## Developer Information
### Event API
The mod exposes `TideNightLifecycle` Forge‑bus events:
- `TideNightLifecycle.StartingEvent`: Fires before Tide Night officially starts, cancellable. Cancelling prevents the event from activating.
- `TideNightLifecycle.StartedEvent`: Fires once Tide Night has fully begun, carries message and message‑type metadata.
- `TideNightLifecycle.StopEvent`: Fires on Tide‑Night termination.

Listen to these events from your own mod to add custom logic, override notifications or modify conditions.

### Per‑dimension state
Via `TideLevel` mixin‑injected interface on `ServerLevel`, every dimension maintains its own separate Tide‑Night trigger flag, active‑state and historical counter.

### Build
Requires JDK 17, Forge MDK matching target Minecraft version.

## Features
### Event & Trigger System
- Multiple independent trigger modes: full-moon trigger, fixed-day interval trigger, nightly weighted random chance, administrator manual command trigger.
- Configurable event start- and end-tick boundaries; you may adjust exact time window when Tide Night becomes active.
- `TideNightLifecycle` cancellable Forge-bus events (`StartingEvent`, `StartedEvent`, `StopEvent`), allowing other mods to intercept, cancel or react to event lifecycle.
- Multi-option notification system: random-pool selectable start / end messages, output target configurable: chat message, title, subtitle or action-bar.
- Execute arbitrary server-side commands automatically on event start and event end.
- Configurable in-event weather override: clear, rain or thunderstorm applied for the duration of Tide Night.
- Restrict `/time` and `/weather` commands during active Tide Night to prevent players from bypassing the crisis.
---
### Mob Spawning System
- Pure native entity instantiation, no `/summon` command workaround; entities are tagged with mod-specific markers for lifecycle tracking.
- Independent JSON-formatted mob spawn table (`duskveil-moblist.json`). Each mob entry defines valid event-count range, minimum-maximum spawn quantity per wave. Config files auto-repair missing or invalid JSON entries.
- Configurable dimension blacklist / whitelist. Separate spawning algorithms for surface-based and underground / nether-like dimensions.
- Spawn position validator: light-level check, collision validation, configurable retry attempts, optional force-spawn fallback.
- Progressive wave-size multiplier: mob quantity increases for each successive spawn-wave within single Tide Night.
- Multi-player mitigation: spawn count scaling factor to avoid overwhelming mob quantity when multiple players are nearby.
- Minimum / maximum spawn distance from player, evenly-distributed spawn mode option.
- Global mob-cap: stop further spawning once total mod-spawned mob count exceeds configured limit.
- Persistence flag toggle: control whether mod-generated mobs obey vanilla mob despawn rules.
- Experimental one-click entity purge: despawn all mod-spawned entities on Tide-Night end.
---
### Attributes & Behaviours
- Stat multipliers for max health, armor, armor toughness, melee-attack damage; separate speed multiplier for ground, flying and water-dwelling mobs.
- Configurable absorption shield value applied on spawn.
- Arbitrary mob effect list: apply configurable potion effects for remaining Tide-Night duration.
- Automatic equipment enchantment: configurable enchant power value; toggle treasure-enchantment eligibility.
- Toggle creeper charged-state on spawn.
- Toggle whether mod-spawned hostile mobs can attack each other.
- Built-in mob survival helpers: anti-suffocation block breaking, temporary fire-resistance when burning, slow-fall for newly-spawned flying mobs.
---
### Gameplay Restrictions
- Mixin-based sleep interception: prevent sleeping during Tide Night **without destroying bed blocks**. Intercepts both right-click bed and programmatic sleep-invocation.
- Proper gamemode filtering: sleep prevention only applies to survival / adventure players; creative mode remains unaffected.
---
### Administrator Commands
- Full suite of admin-level commands under root `/duskveil`.
- Every individual sub-command can be fully enabled / disabled inside common configuration file.
- Operations: schedule next-night trigger, instant start / instant stop event, adjust or query historical Tide-Night counter, purge all mod-spawned entities, manual gaze-aggro invocation.
---
### Client-Side Features
- Highly configurable real-time info overlay HUD:
    - Display game time & day count, player / camera coordinates, entity / hostile-mob / item count, real-time FPS.
    - Anchor to four screen corners: Top-Left, Bottom-Left, Top-Right, Bottom-Right.
    - Auto-hide when F1 (GUI hidden) or F3 (debug screen) is active.
    - Tunable line spacing, X/Y offset, text shadow toggle, decimal precision for coordinate output.
- Atmosphere rendering:
    - Smooth-transition sky & fog tint override during Tide Night, customizable hex-color value.
    - Optional gamma-lock to enforce darker visual ambiance for event nights.
---
### Configuration Architecture
- Configuration split into three scopes: `server`, `client`, `common`. Separation prevents client-only settings from leaking to server and vice-versa.
- Custom helper config framework supporting value range validation, enum option restriction, comment banner generation.
- Partial configuration supports live hot-reload; restart requirement clearly defined by config metadata.
- Mob-definition separated out into standalone JSON file, independent from toml config system.
---
## Commands
Commands require corresponding permission levels. Each sub-command may be disabled in the common configuration file.
- `/duskveil marker` : Schedule the subsequent night to become a Tide Night.
- `/duskveil start` : Immediately start an active Tide-Night event.
- `/duskveil stop` : Immediately terminate the currently-running Tide Night.
- `/duskveil count query` : Query total Tide-Night occurrence counter.
- `/duskveil count set <value>` : Manually set the event occurrence counter value.
- `/duskveil count add <value>` : Increment event occurrence counter.
- `/duskveil count decrease <value>` : Decrement event occurrence counter.
- `/duskveil clear` : Remove all entities spawned by TDN.
- `/duskveil gaze <player>` : Force nearby hostile / neutral entities to apply gaze aggro against target player.
- `/duskveil reload server/moblist` : Reload Server / Moblist Configuration.

## Configuration Notes
Four configuration artifacts will generate within `config/duskveil/`:
- `duskveil-server.toml`: Server-only settings. Contains event timing & triggers, spawner parameters, mob attribute multipliers, gaze-mechanic and mob-behaviour settings.
- `duskveil-client.toml`: Client-only settings. Info-overlay layout & content toggles, sky-fog tint color and gamma-lock behaviour.
- `duskveil-common.toml`: Shared cross-side settings. Command enable/disable toggles, JSON mob-list generation defaults.
- `duskveil-moblist.json`: Standalone JSON mob spawn table. Each entry defines mob id, start-history count, end-history count, min-max spawn amount per wave. Automatically repairs missing keys on server startup or config reload.

## Known Limitations
- Mob-spawning algorithm for non-surface dimensions remains experimental; terrain collision and position-finding logic may produce sub-optimal spawn positions.
- Neutral mob gaze behaviour depends on target mob type; some vanilla or modded neutral mob AI may override injected goal selectors.
- Custom goal selectors added by TDN can be overwritten by other mods which fully clear mob goal-selector entries.

## License
This project is released under the **MIT License**.