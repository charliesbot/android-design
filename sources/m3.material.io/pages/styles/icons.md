# Icons
Source: https://m3.material.io/styles/icons

> Icons are small symbols to easily identify actions and categories

## Overview

- Get Material Symbols icons at fonts.google.com/icons. Recolor, resize, and copy and paste icons.
- Use the Material Symbols variable font to enable dynamic styling in product
- You can change the weight, fill, optical size, and grade of variable font icons

[VIDEO] Array of icons with various stylistic attributes.

#### Resources

|

Type |

Link |

Status
|

Design |

Icons catalog |

Available
|

Material Symbols Figma plugin |

Available
|

Icon keyline template (ZIP) |

Available

#### What's new

##### Copy & paste customized Material Symbols

You can now copy and paste icons from Google Fonts. Once you search for and select the desired icon, options will appear in the right-hand panel to resize, recolor, and copy the customized icon to clipboard.

[IMAGE] Panel showing options to size, recolor, and copy selected icon.

_Icons can now be copied with a single click_

##### Material Symbols

The new variable icon font set supports three styles: **outlined**, **rounded**, and **sharp**. All Material Symbols are newly drawn to be pixel-crisp and modernized.

[IMAGE] Twelve icons depicted in three styles: outlined, with rounded corners, and sharp.

_- Outlined
- Rounded
- Sharp_

##### Adjustable axes

Material Symbols have four adjustable stylistic variable font attributes called **axes**. An axis refers to an attribute of a symbol that can be altered to create visual variations. The attributes are: weight, fill, optical size, **grade**.

[VIDEO] Four icons shown with adjusted weight, fill, grade, and optical sizes.

_A range of symbols shown with the same weight, fill, grade, and optical sizes_

##### Material Symbols Figma plugin

Easily incorporate Material Symbols into your latest designs on Figma.

[IMAGE] Screenshot of Material Symbols plugin in Figma.

_Figma Symbols plugin_

## Designing icons

#### Design principles

Icons are an essential element of any interface, packing an informative punch into a small form factor. They’re designed to be simple, modern, friendly, and sometimes quirky. To ensure consistency and readability, their limited size means that each icon must strictly adhere to guidance while still expressing essential characteristics.

[IMAGE DO] Front view of boat icon.

**DO:** _Simplify icons for greater clarity and legibility_

[IMAGE DON'T] Boat image with sails, mast, and flag.

**DON'T:** _Don’t be overly literal. Avoid complex icons._

[IMAGE DO] Use geometric, consistent shapes.

**DO:** _Make icons graphic and bold_

[IMAGE DON'T] Detailed thumbs-up icon with contoured fingers in outline.

**DON'T:** _Don’t use delicate or loose organic shapes_

[IMAGE DO] Four icons with a consistent style.

**DO:** _Use and maintain a consistent visual style throughout one icon set_

[IMAGE DON'T] Four icons with a inconsistent styles.

**DON'T:** _Avoid mixing styles for one icon set_

#### Icon sizes and layout

##### Standard (Baseline) icon size

Standard icons are displayed as 24dp x 24dp. For pixel-perfect accuracy, create icons for viewing at 100% scale.

[IMAGE] Icon at 100% scale on a 24dp grid.

_24dp grid at 100% scale_

[IMAGE] Icon at 1000% scale on a 24dp grid.

_24dp grid at 1000% scale_

##### Additional optical icon sizes

Icons support additional sizes: 20dp, 40dp, and 48dp, with 20dp primarily for desktop, dense layouts, and small scale visuals, and 40dp and 48dp optimized for display or headline type, plus larger screen sizes.

[IMAGE] Four document icons shown at increasing scales.

_Supported icon sizes: 20dp, 24dp, 40dp, and 48dp_

##### Standard (Baseline) icon layout

Icon content should remain inside of the **live area**, which is the region of an image that is unlikely to be hidden from view (such as an area where sidebars appear upon scrolling).

If additional visual weight is needed, content may extend into the padding between the live area and the **trim area** (the complete size of a graphic). No parts of the icon should extend outside of the trim area.

[IMAGE] A 24dp-by-24dp icon grid with the 20dp-by-20dp live area highlighted.

_**Live area**

Icon content is limited to the 20dp x 20dp live area, with 2dp of padding around the perimeter_

[IMAGE] A 24dp-by-24dp icon grid with the inner 2dp padding highlighted.

_**Padding**

2dp of padding surrounds the live area_

[IMAGE DO] A 24dp-by-24dp icon grid with the inner 2dp padding highlighted.

**DO:** _Icon content is limited to the 20dp-x-20dp live area, with 2dp of padding around the perimeter_

[IMAGE CAUTION] Icon using live area and trim area.

**CAUTION:** _If additional visual weight is needed, content may extend into the padding between the live area and the trim area_

[IMAGE DON'T] Icon exceeding trim area.

**DON'T:** _No parts of the icon should extend outside of the trim area_

#### Grid and keyline shapes

##### Icon design template

If your design requires an icon that isn’t covered by the over 2,000 variations in Google Font’s icon library, you may want to create your own. Download this 24dp keyline template* (ZIP file) to design custom icons in Adobe Illustrator.

_*This template is available under __Apache 2.0__. By downloading this file, you agree to the __Google Terms of Service__. The __Google Privacy Policy__ describes how data is handled in this service._

##### Icon grid and keyline

The icon grid establishes clear rules for the consistent, but flexible, positioning of graphic elements.

Keyline shapes are the foundation of the grid. By using these core shapes as guidelines, you can maintain consistent visual proportions across system icons.

[IMAGE] A 24dp-by-24dp icon grid.

_Grid_

[IMAGE] A 24dp-by-24dp grid of foundational icon keylines: square, circle, vertical rectangle, horizontal rectangle.

_24dp grid at 1000% scale_

[IMAGE] A 24dp-by-24dp grid of foundational icon keylines with the square keyline highlighted.

_Square height and width, 18dp_

[IMAGE] Add chart icon on square keyline.

_Icon drawn using square keyline_

[IMAGE] A 24dp-by-24dp grid of foundational icon keylines with the circle keyline highlighted.

_Circle diameter, 20dp_

[IMAGE] Globe icon on circle keyline.

_Icon drawn using circle keyline_

[IMAGE] A 24dp-by-24dp grid of foundational icon keylines with the vertical rectangle keyline highlighted.

_Vertical rectangle height, 20dp, and width, 16dp_

[IMAGE] Document icon on vertical rectangle keyline.

_Icon drawn using vertical rectangle keyline_

[IMAGE] A 24dp-by-24dp grid of foundational icon keylines with the horizontal keyline highlighted.

_Horizontal rectangle height, 16dp, and width, 20dp_

[IMAGE] Envelope icon on horizontal rectangle keyline.

_Icon drawn using horizontal rectangle keyline_

[IMAGE DO] Icon grid including a folder icon aligning to the grid. X and Y placement coordinates are shown using integers.

**DO:** _Position icons “on pixel” within the icon grid_

[IMAGE DON'T] Icon grid including a folder icon misaligned to the grid with X and Y placement coordinates shown using decimals.

**DON'T:** _Don’t place the icon on a coordinate that isn’t “on pixel”_

#### Icon metrics

##### Anatomy

[IMAGE] Diagram of a calendar icon on a grid highlighting six different elements.

_- Corner
- Stroke terminal
- Counter stroke
- Stroke
- Counter area
- Bounding area_

##### Corners

Corner radii are 2dp by default. For the outlined style symbols, interior corners are square, not rounded. For shapes 2dp wide or less, stroke corners shouldn’t be rounded.

For the rounded style symbols, both exterior and interior corner radii are rounded and for the sharp style symbols, both exterior and interior corners radii reduce from 2dp to 0dp.

[IMAGE] Credit card symbol placed on grid with 2dp rounded exterior corners highlighted.

_Exterior corners with 2dp corner radii_

[IMAGE] Credit card symbol placed on grid with 2dp linear interior corners highlighted.

_Interior corners shouldn’t be rounded_

[IMAGE CAUTION] Document icon placed on grid with overly rounded corners highlighted.

**CAUTION:** _Overly round corners reduces the symbol’s legibility_

[IMAGE DON'T] ‘Add more’ icon placed on grid with inconsistent rounded corners.

**DON'T:** _Don’t use inconsistent corner radii_

##### Weight and stroke

The recommended stroke weight for icons is 2dp or the regular weight (400), which includes curves, angles, and both interior and exterior strokes. Material Symbols can provide a range of weights between thin (100) and bold (700).

[IMAGE] Regular stroke weight timer icon placed on a grid.

_Timer icon at the regular stroke weight (400)_

[IMAGE] Weight timer symbols ranging from 100 to 700 weight.

_Timer symbol shown across a 100–700 weight range_

[IMAGE] Arrow symbol placed on a grid with arrowhead terminals trimmed to 45 degrees highlighted.

_Stroke terminal on an icon_

[IMAGE] Add circle symbol placed on grid with linear 2dp inner stroke highlighted.

_Counter stroke on an icon_

[IMAGE] Add chart icon placed on grid with consistent stroke weights and squared stroke terminals.

_Use consistent stroke weights and squared stroke terminals_

[IMAGE] Add chart icon placed on grid showing inconsistent stroke weights and rounded stroke terminals.

_Don’t use inconsistent stroke weights or rounded stroke terminals_

##### Complex icon shapes

If an icon requires complex details, subtle adjustments can be made to improve its legibility. These adjustments are referred to as optical corrections. Any optical correction should use the geometric forms on which all other icons are based, without skewing or distorting those shapes.

[IMAGE] Paperclip icon on grid with adjusted 1.5dp stroke highlighted.

_The paperclip icon uses 1.5dp of the possible 2dp stroke area to fit multiple curves within the 24dp x 24dp icon space_

[IMAGE] Ramen bowl icon on grid with adjusted 1.5dp stroke highlighted.

_The ramen bowl icon uses 1.5dp stroke and 2dp stroke together within the 24 x 24dp icon space_

[IMAGE DO] Building icon using flat shapes.

**DO:** _Make icons face forward_

[IMAGE DON'T] Building icon in isometric perspective.

**DON'T:** _Don’t tilt, rotate, or make icons appear dimensional_

## Applying icons

#### Icon & Material Symbol styles

Material Symbols are the new default, and are available in three styles: **outlined, rounded, **and** sharp**. (The legacy Material Icons continue to be available, but don’t have the variable font capabilities of Material Symbols.)

##### Outlined style

Outlined symbols use stroke and fill attributes for a light, clean style that works well in dense UIs. The stroke weight of outlined icons can be adjusted to complement or contrast the weight of your typography.

[IMAGE] Examples of outlined symbols with stroke and fill attributes.

_Outlined style_

[IMAGE] Outlined icon set on grid.

_2dp outlined icons remain readable across sizes and applications_

[IMAGE] Four filled symbols showing full body human and proprietary icons.

_For optimal legibility and recognition, some symbols should remain filled, such as full body human icons or proprietary icons_

[IMAGE] Thin-lined outlined symbols correspond to app typography.

_The lighter stroke weight of these outlined symbols mirrors the thin lines of the app’s typography_

##### Rounded and sharp styles

Rounded symbols use a corner radius that pairs well with brands that use heavier typography, curved logos, or circular elements to express their style.

Sharp symbols display corners with straight edges, for a crisp style that remains legible even at smaller scales. These rectangular shapes can support brand styles that aren’t well-reflected by rounded shapes.

[IMAGE] Examples of rounded-style icons.

_Rounded-style icons_

[IMAGE] Examples of sharp-style icons.

_Sharp-style icons_

[IMAGE] Plus icon as a round icon.

_Corner radii for round icons_

[IMAGE] Plus icon as a sharp icon.

_Square corner radii for sharp icons_

[IMAGE] Travel app with rounded buttons and rounded icons.

_This app uses rounded buttons and round icons_

[IMAGE] Six icons implementing sharp style.

_The 0dp corner radius of the sharp icon set echoes this app’s rectangular design details_

#### Customizing Symbols

Material Symbols have four adjustable stylistic variable font attributes called **axes**. An axis is a typographic term referring to the attribute of a symbol that can be altered to create visual variations.

Each style symbol contains four axes: **weight, fill, grade,** and **optical size**.

##### Weight

Weight defines the symbol’s stroke weight, with a range of weights between thin (100) and bold (700). Weight can also affect the overall size of the symbol.

[VIDEO] Gradual increase of symbols from thin to bold.

_A symbol in a range of weights_

[IMAGE] 400 regular-weight icons used in standard navigation drawer and modal navigation drawer.

_400 regular-weight symbols_

[IMAGE DON'T] Photo gallery using 100 weight icons.

**DON'T:** _Don't use the lightest weight for standard-size (24dp) icons. The minimum weight for this size should be 200._

[IMAGE CAUTION] Three side-by-side 24p standard symbols.

**CAUTION:** _Be careful using excessive weight for standard 24dp symbols_

[IMAGE DO] Navigation rail with consistent symbol weights.

**DO:** _Apply weights consistently_

[IMAGE DON'T] Navigation rail with varying symbol weights.

**DON'T:** _Don’t mix different weights_

##### Fill

Fill gives you the ability to transition from a more outlined style to a reversed or more filled style.

A fill attribute can be used to convey a state of transition, such as unfilled and filled states. Values range from 0 to 1, with 1 being completely filled. Along with weight, fill is a primary attribute that impacts the overall look of a symbol.

[IMAGE] Unfilled icons.

_Unfilled symbols with fill set to 0_

[IMAGE] Set of filled icons.

_Filled symbols with fill set to 1_

[VIDEO] Four filled symbols in selected and unselected states set in bottom navigation.

_Bottom navigation with filled symbols in selected and unselected states_

##### Grade

Weight and grade affect a symbol’s thickness. Adjustments to grade are more granular than adjustments to weight and have a smaller impact on the size of the symbol.

Grade is also available in some text fonts. Grade levels between text and symbols can be matched for a harmonious visual effect. For example, if the text font has a -25 grade value, the symbols can match it with a suitable value of -25.

[IMAGE] Symbol thickness at grade 0 and at negative grade.

_- At grade 0, the thickness of the symbol does not change
- At negative grade, the thickness of the symbol appears lighter_

Grade can also compensate for** visual bleed**, which is when images can look bigger or smaller depending on the color contrast. To match the apparent icon size, the default grade for a dark icon on a light background is 0, and -25 for a light icon on a dark background.

[IMAGE] Button with icon and text in light UI.

_Icon button featuring a 0 default grade symbol in light UI_

[IMAGE] Button with icon and text in dark UI.

_Icon button featuring a negative grade symbol in dark UI_

To make strokes heavier and more emphasized, use positive value grade, such as when representing an active icon state.

[IMAGE] Photo icon in active state appearing bolder.

_An icon with active state using positive value grade for emphasis_

##### Optical sizes

Optical sizes range from 20dp to 48dp.

For the image to look the same at different sizes, the stroke weight (thickness) changes as the icon size scales. Optical size offers a way to automatically adjust the stroke weight when you increase or decrease the symbol size.

[IMAGE] Four icons gradually increasing in optical size.

_Four optical sizes, 20dp, 24dp, 40dp, 48dp_

Traditionally, icons are resized from a 24dp source vector, resulting in a large scaled icon that’s too heavy compared to the original. With the optical size axis, you can maintain the stroke weight (thickness) as the icon size grows.

[VIDEO] Side-by-side scaling view showing a Material icon and a Material Symbol.

_- Material icon
- Material Symbol_

[IMAGE] Desktop dropdown menu with icon in active state.

_Use 20dp optical size value for dense layouts on desktop_

[IMAGE] Forward and reverse symbols highlighted on device.

_Use larger size 40dp–48dp symbols when primary actions need to be highlighted_

#### Using Material Symbols with typography

Material Symbols are designed with similar considerations to typefaces, and often appear alongside text. Choosing the right icon set can tie the content of an interface together, enhancing the cohesive branded feel of your product.

[IMAGE] Selections of icons and typography examples in different contexts where weights and sizes are paired.

_Match the optical weight and size of text and icon to ensure consistency_

[IMAGE DO] Text and icon that are the same size.

**DO:** _Use the same size for your Material Symbols and text_

[IMAGE DON'T] A small icon mismatched with larger text.

**DON'T:** _Don’t mix the sizes of your symbol and text_

[IMAGE DO] An icon and text that are the same optical weight.

**DO:** _Use the same optical weight for your symbol and text_

[IMAGE DON'T] An icon and text that have mismatched optical weight.

**DON'T:** _Don’t use different optical weights for Material Symbols and text_

[IMAGE DO] Icon that has had its baseline shifted down 11.5%.

**DO:** _Shift down the baseline of symbols to approximately 11.5% of the text size_

[IMAGE DON'T] Icon and text that are using the same baseline.

**DON'T:** _Don’t use the same baseline for Material Symbols and text_

#### Accessibility

Learn more about making your icons more accessible.

##### Icons with a label text

Label text provides short, meaningful descriptions when symbols are more abstract. This can prove helpful in the case of navigation.

[IMAGE] Navigation bar showing four destinations, with 1 active destination featuring both icon and text label.

_Label text provides short descriptions, especially useful for navigation_

[IMAGE] Navigation bar with four destinations with only icons, no labels.

_Use caution if icons are displayed without labels. Icon meaning should always be unambiguous and accessible for all users. Text labels can be omitted in specific circumstances where reduced visual impact is necessary._

##### Small icons

Material Symbols can scale up or down in size without a loss of fidelity. Simple symbols, like stars for ratings, can be used on their own at any size, as long as they remain identifiable.

Other symbols should have an accompanying text label below 20dp to ensure their meaning is clear and to maintain accessibility. These symbols include:
- Complex icons, which are highly detailed or have multiple parts

- Icons with a key action, which are essential to using the product

##### Target size

Adequate space should surround icons to allow legibility and interaction.

Symbols of 24dp should have a target size of 48dp by default.

[IMAGE] 1. 24dp add symbol inside 48dp red square, 2. mobile UI with attach, add, and more symbols.

_- Measurements
- Placement_

When a mouse and keyboard are the primary input methods, measurements may be condensed to accommodate denser layouts.

A 20dp size symbol can use a target size of 40dp.

[IMAGE] 1. 24dp add symbol inside 48dp red square, 2. desktop UI with attach, add, and more symbols.

_- Measurements
- Placement_

#### Localizing icons

To make sure iconography translates effectively in local markets, test it across age groups, cultures, and languages, and follow these best practices:
- Use labels when icons and symbols are more abstract
- Remember that navigation items must have labels for clarity and accessibility
- Consider tech knowledge: people who use the internet a lot may have different understandings of icons than people who use the internet less

[IMAGE] Comparison of three UIs showing add to cart, add to bag, and add to basket.

_Translate icons for local markets. For example, different locales may prefer a cart, bag, or basket for checkout experiences._

##### Cultural influence of colors and symbols

Color carries cultural significance and can convey different emotions in different cultures. White is commonly associated with purity in western cultures but symbolizes mourning in some eastern cultures.

Consider cultural interpretations of symbols. In many western cultures, owls represent wisdom, while some eastern cultures view them as a negative omen. When using or creating symbols, be mindful that their meanings can vary significantly across cultures.

[IMAGE] Comparison of UIs where red and green are warning colors.

_Think about how color translates. Some locales use red as a warning color, while others use green._
