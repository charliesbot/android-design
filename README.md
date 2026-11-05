# android-design

Agent skills for designing Android UI with Material 3 Expressive in Jetpack Compose, for phone, tablet, and desktop apps. Every rule is distilled from Google's official guidance.

- **`android-design`** gives an agent design judgment for building, refining, and reviewing real Compose: hierarchy, color, type, shape, motion, and component choice.
- **`android-design-explore`** lets an agent explore directions as live proposals in a native previewer app, then port the approved one to Compose.

## Install the skills

```bash
npx skills add charliesbot/android-design
```

Or copy the folders in `skills/` into your agent's skills directory (for Claude Code, `~/.claude/skills/`).

## Install the previewer app

Only `android-design-explore` needs it. It runs on Apple Silicon Macs.

1. Download `AndroidDesignPreview.dmg` from the [latest release](https://github.com/charliesbot/android-design/releases/latest) and drag the app into Applications.
2. Clear the quarantine flag. The app is not signed, and a quarantined copy started by an agent never responds.

   ```bash
   xattr -dr com.apple.quarantine /Applications/AndroidDesignPreview.app
   ```

3. Register its binary as a stdio MCP server named `android_design_preview`. For Claude Code:

   ```bash
   claude mcp add --scope user android_design_preview -- /Applications/AndroidDesignPreview.app/Contents/MacOS/AndroidDesignPreview
   ```

Other agents take the same binary path as a stdio MCP command. All agents share one window, one tab per session. You can also open the app yourself and load a proposal with File > Open Proposal.

To build from source instead (JDK 17), run `./gradlew :app:installApp` in `app/`. It installs to `~/Applications`, needs no quarantine step, and the binary path changes to match.

### Windows

Windows 10 (1803 or later) and 11, x64.

1. Download `AndroidDesignPreview.msi` from the [latest release](https://github.com/charliesbot/android-design/releases/latest) and run it. It installs for your user only, with no admin prompt. The installer is not signed, so SmartScreen may ask you to confirm (More info > Run anyway).
2. Register the binary. For Claude Code:

   ```powershell
   claude mcp add --scope user android_design_preview -- "$env:LOCALAPPDATA\AndroidDesignPreview\AndroidDesignPreview.exe"
   ```

To build from source, run `.\gradlew.bat :app:installApp` in `app\`. It installs to the same folder, so the path above stays the same. Quit a running instance first, because Windows locks its files.
