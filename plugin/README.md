# android-design plugin

Material 3 Expressive design judgment (`android-design`) and live Jetpack Compose board explorations (`android-design-explore`) for Android phones, foldables, tablets, desktop, and Wear OS.

## Prerequisites

- **Node.js 18+** (runs `npx -y @charliesbot/android-design-preview@0.4.0`)
- **Java 17+** (system JDK or Android Studio's bundled JBR)

## Included skills and MCP tools

- `skills/android-design/`: design judgment, tokens, layout adaptation, and expressive components.
- `skills/android-design-explore/`: writes `design/<slug>.explore.kt` Compose boards and checks them via `android_design_preview` (`check_design`, `get_tree`) plus the localhost HTML/CSS/SVG board (`127.0.0.1`).
