# Phase 00 — Project setup

**Goal:** A NeoForge 1.21.4 project that builds, launches a dev client, and launches a dev dedicated server, with `elementalia` showing up in the mod list.

**Prerequisites:** None.

## Tasks

### Local prerequisites

- [ ] Install JDK 21 (Temurin or Microsoft build). Verify `java -version` reports 21.
- [ ] Install IntelliJ IDEA Community Edition.
- [ ] Install Git. Verify `git --version`.
- [ ] Confirm Minecraft Java Edition is installed on this machine (for visual sanity checks later).

### Repository

- [ ] `git init` in the project root.
- [ ] Create a `.gitignore` covering: `build/`, `.gradle/`, `run/`, `*.iml`, `.idea/`, `out/`, `bin/`, `eclipse/`, `*.launch`, `logs/`.
- [ ] Initial commit of the empty repo + this `plan/` folder.

### NeoForge MDK

- [ ] Download the NeoForge 1.21.4 MDK (Mod Development Kit) from the official NeoForged site.
- [ ] Extract the MDK contents into the project root (alongside `plan/`). Do not nest it in a subfolder.
- [ ] Verify presence of: `build.gradle`, `gradle.properties`, `settings.gradle`, `gradlew`, `gradlew.bat`, `src/main/`, `src/main/resources/META-INF/neoforge.mods.toml`.

### Configure mod metadata

Edit `gradle.properties` to use the decisions from `plan/README.md`:

- [ ] Set `mod_id=elementalia`
- [ ] Set `mod_name=Elementalia`
- [ ] Set `mod_group_id=com.example.elementalia`
- [ ] Set `mod_authors=` to your name.
- [ ] Set `mod_description=` to a one-line description.
- [ ] Confirm `minecraft_version=1.21.4`.
- [ ] Confirm `neo_version=` matches the latest stable NeoForge for 1.21.4 (read from the MDK's `gradle.properties` default — only change if you have a reason).

Edit `src/main/resources/META-INF/neoforge.mods.toml`:

- [ ] Replace `examplemod` references with `elementalia`.
- [ ] Set the display name, author, description fields to match.

Rename the example package:

- [ ] Rename `src/main/java/com/example/examplemod/` to `src/main/java/com/example/elementalia/`.
- [ ] Rename `ExampleMod.java` to `Elementalia.java` and update the class name + the `@Mod("examplemod")` annotation to `@Mod("elementalia")`.
- [ ] Update the `MODID` constant in `Elementalia.java`.

### First build and run

- [ ] From a terminal in the project root, run `./gradlew build`. Confirm it completes successfully. (First run downloads dependencies — can take several minutes.)
- [ ] Run `./gradlew runClient`. Confirm Minecraft launches, the title screen appears, and `Elementalia` appears in the **Mods** list.
- [ ] Close the client.
- [ ] Run `./gradlew runServer`. The first run will fail asking you to accept the EULA. Edit the generated `run/eula.txt` and set `eula=true`.
- [ ] Run `./gradlew runServer` again. Confirm the server starts, prints `Done (X.Xs)! For help, type "help"`, and lists `elementalia` when you type `/mods` or check the log.
- [ ] Stop the server with `stop`.

### IDE integration

- [ ] Open the project in IntelliJ IDEA. Import as a Gradle project.
- [ ] Confirm IntelliJ picks up the `runClient` and `runServer` run configurations (NeoForge MDK generates these automatically).
- [ ] Confirm you can launch `runClient` from inside IntelliJ.

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
