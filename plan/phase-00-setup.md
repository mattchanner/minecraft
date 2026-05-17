# Phase 00 — Project setup

**Goal:** A NeoForge 1.21.4 project that builds, launches a dev client, and launches a dev dedicated server, with `elementalia` showing up in the mod list.

**Prerequisites:** None.

## Tasks

### Local prerequisites

- [x] Install JDK 21 (Temurin or Microsoft build). Verify `java -version` reports 21.
- [x] Install IntelliJ IDEA Community Edition.
- [x] Install Git. Verify `git --version`.
- [x] Confirm Minecraft Java Edition is installed on this machine (for visual sanity checks later).

### Repository

- [x] `git init` in the project root.
- [x] Create a `.gitignore` covering: `build/`, `.gradle/`, `run/`, `*.iml`, `.idea/`, `out/`, `bin/`, `eclipse/`, `*.launch`, `logs/`.
- [x] Initial commit of the empty repo + this `plan/` folder.

### NeoForge MDK

- [x] Download the NeoForge 1.21.4 MDK (Mod Development Kit) from the official NeoForged site.
- [x] Extract the MDK contents into the project root (alongside `plan/`). Do not nest it in a subfolder.
- [x] Verify presence of: `build.gradle`, `gradle.properties`, `settings.gradle`, `gradlew`, `gradlew.bat`, `src/main/`, `src/main/resources/META-INF/neoforge.mods.toml`.

### Configure mod metadata

Edit `gradle.properties` to use the decisions from `plan/README.md`:

- [x] Set `mod_id=elementalia`
- [x] Set `mod_name=Elementalia`
- [x] Set `mod_group_id=com.example.elementalia`
- [x] Set `mod_authors=` to your name.
- [x] Set `mod_description=` to a one-line description.
- [x] Confirm `minecraft_version=1.21.4`.
- [x] Confirm `neo_version=` matches the latest stable NeoForge for 1.21.4 (read from the MDK's `gradle.properties` default — only change if you have a reason).

Edit `src/main/resources/META-INF/neoforge.mods.toml`:

- [x] Replace `examplemod` references with `elementalia`.
- [x] Set the display name, author, description fields to match.

Rename the example package:

- [x] Rename `src/main/java/com/example/examplemod/` to `src/main/java/com/example/elementalia/`.
- [x] Rename `ExampleMod.java` to `Elementalia.java` and update the class name + the `@Mod("examplemod")` annotation to `@Mod("elementalia")`.
- [x] Update the `MODID` constant in `Elementalia.java`.

### First build and run

- [x] From a terminal in the project root, run `./gradlew build`. Confirm it completes successfully. (First run downloads dependencies — can take several minutes.)
- [x] Run `./gradlew runClient`. Confirm Minecraft launches, the title screen appears, and `Elementalia` appears in the **Mods** list.
- [x] Close the client.
- [x] Run `./gradlew runServer`. The first run will fail asking you to accept the EULA. Edit the generated `run/eula.txt` and set `eula=true`.
- [x] Run `./gradlew runServer` again. Confirm the server starts, prints `Done (X.Xs)! For help, type "help"`, and lists `elementalia` when you type `/mods` or check the log.
- [x] Stop the server with `stop`.

### IDE integration

- [x] Open the project in IntelliJ IDEA. Import as a Gradle project.
- [x] Confirm IntelliJ picks up the `runClient` and `runServer` run configurations (NeoForge MDK generates these automatically).
- [x] Confirm you can launch `runClient` from inside IntelliJ.

## Acceptance criteria

- `./gradlew build` succeeds with no errors.
- `./gradlew runClient` launches a client where `Elementalia` is listed in **Mods**.
- `./gradlew runServer` launches a dedicated server with the mod loaded.
- The project is a git repo with an initial commit.

## Notes / gotchas

- The first Gradle run downloads MC sources and decompiles them — be patient.
- If you see `Unsupported class file major version`, your JDK is wrong. Must be 21.
- On Windows, use `gradlew.bat` from `cmd` or `gradlew` from bash. The bash shell in this environment uses forward slashes.
- Do not commit the `run/` directory — it contains world saves and the EULA file, which churn constantly.
