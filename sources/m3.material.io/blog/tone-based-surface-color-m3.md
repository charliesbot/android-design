# Introducing Tone-based Surfaces in Material 3
Source: https://m3.material.io/blog/tone-based-surface-color-m3

> New surface color roles offer more flexibility for large screens and rich color features

Published: 2023-03-23

The previous way makers could achieve tinted surfaces, which are a hallmark of the M3 design language, was to assign the color role “surface” to a component, and increase its elevation to achieve the desired tinting which was applied via an opacity layer.

The update introduces dedicated surface color roles that are no longer tied to elevation. Makers will be able to choose the right surface roles based on the containment needs of their products, and now have more layout flexibility for larger screens.

The new **surface container roles** and tokens include five colors.

“Surface container” is the recommended default color role for a contained area against the “surface” color role. It provides good contrast and can be flexibly combined with all other surface container roles ranging from “surface container lowest” with the lowest emphasis against the “surface” role, to “surface container highest” with the highest emphasis against the “surface” role.

## Migrating to the new surface color roles

All Material Components will automatically update to use the new surface container color roles. For makers using the previous opacity-based surface model for custom color mappings, we recommend remapping these to the new roles.

- Surface Container Lowest is a new role
- Surface at elevation +1 becomes Surface Container Low
- Surface at elevation +2 becomes Surface Container
- Surface at elevation +3 becomes Surface Container High
- Surface at elevation +4 and +5 are being deprecated, it is recommended to use Surface Container Highest by default as a replacement. As an alternative Surface Container High, or Surface Dim can be used depending on the specific use case.
- Surface Variant becomes Surface Container Highest

Both methods will co-exist for an extended transition period, giving makers time to migrate to the new model.

This update also changes:
- The default light theme surface role from tone 99 to tone 98
- The chroma for the neutral palette is increased from 4 to 6
- Surface roles in dark theme are slightly darkened

To learn more about applying the new color roles in your products, visit the color system guidance.

[IMAGE] two color palettes: New color roles in light and dark theme

[IMAGE] diagram showing how color roles have been remapped: 

[IMAGE] example UI using new color roles: 

[VIDEO] Pre-update: A tinted overlay communicated the elevation of a component surface

[VIDEO]
