# Tic Tac Troll

*A game of mild contempt.* Tic-tac-toe for Android, played with trolls who look each other over, grumble "meh" and "bleh", and topple off their crates into the dirt, then pop, when they lose.

<p>
  <img src="docs/menu.png" width="240" alt="Menu screen with Gorp and Bramble">
  <img src="docs/game.png" width="240" alt="A game in progress on wooden crates">
  <img src="docs/win.png" width="240" alt="Gorp wins while Bramble's trolls fall into the dirt and pop">
</p>

## Features

- **Play the CPU or a friend.** Three CPU skill levels: Easy (random), Medium (wins or blocks when it can, mostly), and Troll (minimax; never loses in classic games). Two-player mode is pass-and-play.
- **Bored trolls mode.** Each player keeps at most three trolls. Play a fourth and your oldest gets bored and pops like a bubble, unless that fourth completes a row with it, which still wins. Games can't end in a draw. The troll about to leave is dimmed, and the setting is remembered.
- **Trolls with attitude.** They drop onto their crates with a squash, breathe, blink, look around at each other, startle when a neighbor lands, and grumble every few seconds. Block a winning line and the blocking troll says "bleh". The troll about to get bored yawns, and Bramble's trolls look up and ponder while the CPU thinks. Losers glance at the winning line before they topple into the dirt and pop, the three in the winning line hop while their teammates look down at them, and a draw gets a slow, disapproving blink, a collective stare and a "meh" from everyone.
- **Sound.** Background music, a thunk as each troll lands, and an ending sound for every outcome: a fanfare for a win, a sad trombone for losing to the CPU, and a brass shrug for a tie. A toggle on the menu mutes everything, and the setting is remembered.
- **Feel.** Haptic ticks as trolls land, a ghost preview while you hold a finger on a crate, and Bramble has opinions about the difficulty you pick.
- **Full screen, any screen.** The status and navigation bars are hidden (swipe in from an edge to see them briefly). Phones play in portrait; tablets and foldables can rotate, and wide windows lay the game out side by side.
- **Accessible.** Works with TalkBack: every crate is labeled, moves (including the CPU's) are announced, and the layout holds up at large font sizes.
- **Rematches alternate** who goes first; Restart keeps the same starter.

## Building

Requires Android Studio (or JDK 17+ with the Android SDK). The app runs on Android 11 (API 30) and up.

```sh
./gradlew installDebug   # build and install on a connected device or emulator
./gradlew test           # unit tests, including an exhaustive check that Troll mode never loses a classic game
./gradlew detekt         # static analysis
```

## Releasing

Release builds are shrunk with R8 and signed with the Play upload key, whose path and passwords come from `~/.gradle/gradle.properties` (`TICTACTROLL_UPLOAD_STORE_FILE`, `TICTACTROLL_UPLOAD_STORE_PASSWORD`, `TICTACTROLL_UPLOAD_KEY_ALIAS`, `TICTACTROLL_UPLOAD_KEY_PASSWORD`). Without them, release builds are unsigned.

```sh
./gradlew bundleRelease  # app/build/outputs/bundle/release/app-release.aab, for Play Console
```

Bump `versionCode` in `app/build.gradle.kts` for every upload. The store listing text, icon, feature graphic and screenshots live in `fastlane/metadata/android/en-US/`, and the privacy policy is [PRIVACY.md](PRIVACY.md), published at https://gist.github.com/nbradbury/71e17962b350c115c72e21c9878aeb85.

## Project layout

```
app/src/main/java/com/nbradbury/tictactroll/
  MainActivity.kt        entry point, full screen, orientation, saved settings, screen switching
  game/GameLogic.kt      board, rules (classic and bored trolls), win detection, CPU move selection
  game/GameViewModel.kt  game state, turns, and the timers behind glances, chatter, yawns and endings
  ui/MenuScreen.kt       title, settings panel (mode, difficulty, Bored trolls), sound toggle
  ui/GameScreen.kt       scores, board, falls and pops, result sheet
  ui/Troll.kt            the animated troll (landing, blinking, moods, reactions) and its speech bubble
  ui/TrollArt.kt         draws the troll art with movable irises and eyelids
  ui/Crate.kt            the wooden crate each cell is drawn as
  ui/Components.kt       shared pieces: backdrop, title, chunky button
  ui/Adaptive.kt         scales the layout to the window and picks side by side for wide windows
  ui/GameSounds.kt       sound effects and troll voices
  ui/GameHaptics.kt      haptic feedback for landings and endings
  ui/BackgroundMusic.kt  looping music tied to the app lifecycle
  ui/theme/              colors and fonts
```

## Credits

- Game design prototyped in Claude Design.
- Troll images created by Adobe Firefly.
- Background music: *Where the Trolls Tread* created by Google Gemini.
- Sound effects and troll voices are synthesized.
- Fonts: [Lilita One](https://fonts.google.com/specimen/Lilita+One), [Rubik Dirt](https://fonts.google.com/specimen/Rubik+Dirt), [DM Sans](https://fonts.google.com/specimen/DM+Sans) and [JetBrains Mono](https://fonts.google.com/specimen/JetBrains+Mono), all under the SIL Open Font License 1.1.

By Nick Bradbury.
