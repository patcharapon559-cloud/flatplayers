# Flat Players (Forge 1.20.1, client-only)

- Join ANY server (vanilla or Forge) without the server having this mod.
  The mod registers no network channels and sets `IGNORESERVERONLY` / `displayTest`.
- Every other player is drawn as ONE flat 2D paper sheet (front of their skin,
  incl. hat/jacket/sleeve/pants layers) that always faces your camera.
  You are never changed (first person, F5 and inventory all stay 3D).
- Name tags are still shown.

## Settings
Config file: `config/flatplayers-client.toml`

| key | meaning |
|---|---|
| `enabled` | on/off |
| `flatDistance` | 0 = all other players flat. N>0 = players closer than N blocks stay 3D, farther ones become flat |
| `showNametag` | show name tags |

Settings screen (toggle, distance slider, name tags): press **K** in game
(rebindable in Controls), or Mods menu -> Flat Players -> Config.

In-game commands (client only, not sent to the server):
- `/flatplayers toggle`
- `/flatplayers distance <blocks>`

## Build
Needs JDK 17 and a Gradle wrapper (copy `gradlew`, `gradlew.bat` and `gradle/`
from the official Forge 1.20.1 MDK, or run `gradle wrapper --gradle-version 8.1.1`).

    ./gradlew build        # jar -> build/libs/flatplayers-1.0.0.jar
    ./gradlew runClient    # test in dev

Put the jar in `.minecraft/mods` with Forge 1.20.1 (47.x).
