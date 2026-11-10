# Fungus' Heart Indicator

![Fungus' Heart Indicator icon](src/main/resources/assets/fungus_heart_indicator/icon.png)

A client-side Fabric mod that puts vanilla hearts above players' heads.
It uses Minecraft's own sprites, so your resource pack works with it too.

- Half hearts, absorption, poison, wither, frozen and hardcore hearts.
- Damage flashes, healing flashes and regeneration animations.
- Distance and size sliders, with an Unlimited distance option.
- Optional hearts for your own player in third person.

## Install

Choose the JAR that matches your Minecraft version. Requires Fabric Loader
0.19.3 or newer. Fabric API isn't required by this mod.

| Minecraft | Java | Mod Menu for settings |
| --- | --- | --- |
| 26.3 | 25+ | 21.x |
| 26.2 | 25+ | 20.x |
| 26.1 | 25+ | 18.x |
| 1.21.11 | 21+ | 17.x |

Put the JAR in your `mods` folder. Mod Menu is optional; install its dependencies
if you use it. Changes save automatically. Press **Esc** to leave the settings.

Unlimited removes the mod's distance cap, but Minecraft still needs to render
and track the player. Servers that hide health data may show inaccurate values.

## Build

With JDK 25 installed, choose a target (defaults to 26.2):

```sh
./gradlew build -Pminecraft_version=26.3
```

Targets: `26.3`, `26.2`, `26.1`, `1.21.11`. The installable JAR is in
`build/<version>/libs/`. Don't install the `-sources.jar`.

Every target runs the headless tests. For the optional CPU benchmark, run
`./gradlew benchmarkHearts -Pminecraft_version=26.3`.
In-game OpenGL and Vulkan testing is still pending.
