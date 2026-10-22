# Avatrix (Fabric 1.21.4)

Client-side Fabric mod: click GUI (Right Shift), Target ESP (line of sight only), color settings, local configs.

## Get the .jar
1. Push this folder to a GitHub repo.
2. Open the Actions tab, run "build" (it also runs on every push).
3. Download the `avatrix-jar` artifact. Put the .jar in `.minecraft/mods` with Fabric Loader 0.16+ and Fabric API for 1.21.4.

## Build locally
Install Java 21 and Gradle 8.12+, then run `gradle build`. The jar ends up in `build/libs/`.

Configs are saved in `.minecraft/config/avatrix/configs/`.
