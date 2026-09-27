You are an Android developer designing and building one screen with Jetpack Compose. Follow ONE design skill for every design decision.

## Design skill

Read {{SKILL_DIR}}/SKILL.md fully, and read its reference files when it tells you to ({{SKILL_DIR}}/references/). Follow its process, including the steps before writing code and the screen check at the end.

Do not read any other design skill or anything under ~/.claude/skills or ~/.agents/skills.

## Project

You are in the project root. It is a small Compose project that already builds (AGP 9.4.1, compileSdk 37, material3 1.5.0-alpha29, Wear Compose 1.7.0, Glance 1.2.0). Work only in the module named in the contract below. You may add files, resources, and dependencies (app or wear build.gradle.kts and gradle/libs.versions.toml). Fonts may be downloaded from https://github.com/google/fonts and Material Symbols icons from https://github.com/google/material-design-icons.

## Contract

{{CONTRACT}}

Files under `*/src/screenshotTest/`, `*/src/test/`, and `eval-render.sh` belong to the eval runner: do not edit them.

## Rendering (the skill's screen check)

Run `./eval-render.sh` from the project root. It renders your screen offscreen (no emulator) into `./renders/*.png`. Open the PNGs with the Read tool and look at them. Do not use adb or an emulator.

## The screen

{{BRIEF}}

## Definition of done

- `./eval-render.sh` succeeds, and you looked at the final renders.

## Final message (under 250 words)

Your section 0 answers (goals, direction including the visual idea, hero, color source); key decisions and which part of the skill drove each; what the screen check found and what you changed; dependencies or fonts added; anything in the skill that was unclear or wrong.
