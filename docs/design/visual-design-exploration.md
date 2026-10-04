# Visual design exploration

**Date:** 2026-10-03

## TL;DR

Make design exploration easier to compare, refine, and resume through a local gallery of real Compose proposals. The workflow stays simple, preserves exploration history inside the requesting app's repository, and converges on one explicitly approved design that can transfer into the app. Approval ends exploration without starting implementation.

## Design

### A review surface outside chat

Chat is useful for discussing a design but poor for comparing and inspecting several UI proposals. Requests for multiple designs or for exploration should open a local mini website that becomes the visual review surface. Ordinary implementation requests do not start an exploration.

The prompt determines the scope. Exploring how to add a feature preserves the app's visual language and surrounding design unless the request or the feature calls for structural changes. A redesign can explore broader changes. Alternatives should represent different design approaches, not merely different colors.

Proposals use real Compose code and components, informed by the requesting app's theme, assets, representative content, and constraints. Image-generated UI mockups are not a substitute. The installed skill and its template are not working directories, and exploration does not modify production app code.

### Compare and inspect

The gallery presents proposals side by side, with phone views first. Opening an option expands it for closer inspection across phone, tablet, desktop, and wide, passport-shaped foldable viewports. Every proposal covers all four sizes rather than waiting for a shortlist. These are layout views of the proposal, not scaled copies of a phone image.

Each option has a name, one sentence explaining its main design choice, and one notes textbox. Important behavior that the preview cannot demonstrate can be explained briefly beside it. Keep the gallery focused on comparison rather than turning it into a design report or annotation editor. There are no separate device-level notes or positional annotations in this design.

The device presets are settled: Pixel 11, Pixel Tablet, a representative Googlebook, and the wide main display of the non-Ultra Galaxy Z Fold8. The implementing agent resolves their logical viewport dimensions, density, and insets from verified device profiles. That is implementation work, not another user design decision. Physical display resolution alone does not establish the Compose layout size.

### Refine through visual grilling

The user can select several options during a round and record what to retain or change in their notes. Selection means interest in a direction, not final approval or permission to implement. Feedback such as "B's layout with C's navigation" can draw from multiple proposals.

The user requests the next round in chat. The agent reads the gallery selections and notes, applies clear feedback directly, and asks focused questions when a meaningful design decision remains. When a choice is best understood visually, show alternatives rather than requiring the user to resolve it through prose alone. The gallery supports this conversation; it is not another chat client.

Produce one refinement by default. Generate additional alternatives when the user asks to explore again, not automatically after every round. Preserve earlier rounds and their notes, show the latest round by default, and make prior versions available for comparison. Do not force another question or iteration when the user is ready to approve.

### Preserve the exploration with the requesting project

Save each exploration under `design-explorations/` inside the repository of the app requesting designs, not inside the android-design skill repository or its installed copy. Keep these artifacts local and Git-ignored by default. Proposal code, previews, selections, notes, and round history stay together as ordinary project files so reopening restores the exploration without reconstructing the chat. This does not require a database or a separate storage framework.

Persisted exploration artifacts are separate from production code. Saving them in the app repository does not authorize changes to the app's source or build configuration. Use representative data and necessary non-sensitive assets, not credentials, local databases, or real personal data. History remains available until explicitly deleted; approval does not trigger cleanup.

This differs from the current exploration workflow, which uses disposable copies under temporary storage and presents rendered PNGs. The proposed gallery introduces durable project-local artifacts and a visual review loop. This document records that proposed behavior; it does not change the current skill instructions or workspace helper.

### Approve one concrete design

An explicit **Approve design** action finishes exploration by marking one exact proposal revision approved. Multiple shortlisted ideas must first become one consolidated, reviewed proposal. "B's layout with C's navigation" is not a final design until the combined result has been shown.

Approval does not implement the design, alter production code, or imply that app integration has already been verified. The agent reports that the selected design is ready for implementation and stops. A later explicit implementation request follows the target project's approval and development workflow.

### Android rendering and AI visual review

The value of the existing example project is that ideas are expressed using real components rather than images that invent UI or ignore its rules. Fidelity is confidence in transferring that design into a real app. It is not a demand for identical antialiasing or crispness across rendering environments, nor a claim that isolated proposal code can be copied into every app without adaptation.

For v1, keep an Android Compose exploration project and use **Compose Preview CLI** to render its proposals to PNG. The AI opens and inspects those actual PNGs before presenting them, using the skill's visual screen check. After a revision, it renders and inspects the updated output rather than inferring appearance from code or reusing stale images.

The local website displays the same PNGs for comparison, expanded inspection, device switching, selections, and notes. It does not recreate the Android UI in HTML or run the proposals as a web app. Compose Preview CLI owns rendering; the gallery owns the review workflow. No Compose Multiplatform web runtime or Playwright capture layer is needed for this proposal-rendering path.

The template provides one adaptive sample screen and four named, device-specific `@Preview` wrappers. Each proposal follows that structure, sharing its screen implementation, sample content, and theme across the four previews. Four images do not require four independently implemented layouts. Enlarging an image is only for closer inspection; it does not demonstrate layout adaptation. Screenshots also do not establish interaction or motion quality.

Compose Multiplatform was considered for live interaction and continuous resizing. It remains technically plausible, but adapting the template, aligning dependencies, hosting browser proposals, and adding browser capture introduces complexity beyond the core comparison-and-refinement goal. V1 chooses the existing Android rendering path for simplicity, not because web fidelity was disproven or because a speed benchmark established a winner. There is no dual-renderer system.

Neither design approval nor a subsequent implementation request authorizes the skill to upgrade or downgrade library versions in the requesting app to match the template. Adapt the selected proposal to the app's existing dependencies, not the app's dependencies to the proposal. If the design cannot be preserved with those versions, surface the incompatibility for a decision rather than silently changing versions or substituting a different design. Inspect the target app's dependency constraints during exploration so known incompatibilities are visible before approval.

Wear OS is outside this v1. Using an Android screenshot workflow does not widen the skill's validated scope.

## Links

- [Compose Multiplatform fidelity research](../research/compose-multiplatform-proposal-fidelity.md): evidence for the alternative considered, not the selected v1 renderer.
- [Current exploration workflow](../../skills/android-design/references/exploration.md): temporary Compose workspaces, rendered proposals, and the explicit transition to implementation.
- [Current exploration checks](../../evals/exploration.md): existing workspace isolation and refinement checks, not evidence that the proposed gallery exists.
- [Android design skill](../../skills/android-design/SKILL.md): design judgment, component guidance, and current platform scope.
- [Compose AI Tools](https://github.com/yschimke/compose-ai-tools): the Compose Preview CLI used to render actual Android previews to PNG.
