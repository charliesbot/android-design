# First Batch of Color Fonts Arrives on Google Fonts
Source: https://m3.material.io/blog/color-fonts-are-here

> Customize color palettes, add gradients, and more with COLRv1

Published: 2022-09-13

Even if you’ve never heard of “color fonts,” you probably use them everyday; emoji are color fonts. Color fonts enable color specification within the font file—and one glyph can have multiple colors. Non-color fonts (i.e., most existing fonts) only specify where color goes. A glyph can only be one color, and if you want your type to be something other than black, you have to manually change it.

Emoji is the most obvious use case for color fonts, but type designers are running with this technology to make highly expressive and customizable typefaces—especially in the newest format, COLRv1.

We’re so excited about color fonts, we even added a search filter for them on Google Fonts.

## What’s so special about the COLRv1 font format?

There are a few things. First, COLRv1 is a binary vector format (unlike the bitmap color fonts of yore), which means fonts can scale without becoming pixelated. And by layering glyphs and then reusing those layers in similar glyphs, file sizes stay compact.

COLRv1, which is already available in both Chrome, Android, and the Google Fonts API, adds support for:
- **Gradients**
All the common CSS gradients are possible, and multiple gradients can be included in a single glyph.

- **Multiple color palettes
**The typeface designer can choose to include multiple color palette options in the font for use in CSS (`base-palette`).

- **Color palette customization**
You can customize color palettes in CSS (`font-palette-values`).

- **OpenType Variations**
With the ability to add variable axes too, the possibilities for customizing a COLRv1 font are literally infinite!

## See COLRv1 in action with nine new typefaces, available now on Google Fonts

The ability to represent multiple colors within a font is an important advancement for expression and communication.

Traditionally, calligraphy was the highest form of Arabic art. And, as it has been used as much for beauty as communication since the earliest versions of the Quran, Arabic calligraphy is inherently colorful. But Arabic is also unique in its functional use of two-color glyphs to clarify pronunciation/add diacritics. Today, colors are still often used for this purpose to help people learn the language.

#### 1. **Reem Kufi Ink** and 2. **Reem Kufi Fun** (Arabic and Latin alphabets)
Khaled Hosny (with Latin components by Santiago Orozco)

Expanding on the Reem Kufi typeface, Reem Kufi Ink and Reem Kufi Fun are based on early Arabic Kufic calligraphy.

Reem Kufi Ink is inspired by designs of the late master of Arabic calligraphy Mohammed Abdul Qadir, who revived this art in the 20th century and formalized its rules. It uses color gradients as an artistic interpretation of what the ink flow of traditional calligraphy would look like when written with a nib (the pointed end of a pen) and ink.

Reem Kufi Fun is weight-variable and is also based on early Kufic (Mushafi) models, but is retrofitted to the Fatimid Kufic grid and borrows from its forms.

#### 3. **Aref Ruqaa Ink **(Arabic and Latin alphabets)
Abdullah Aref, Khaled Hosny, Hermann Zapf

Aref Ruqaa Ink is an Arabic typeface inspired by the classical Ruqaa calligraphic style, which is the the most common type of handwriting in the Arabic script. The use of gradients creates the look of simple, hand-drawn calligraphy.

#### 4. **Amiri Quran **(Arabic and Latin alphabets)
Khaled Hosny and Sebastian Kosch

The Amiri typeface family revives one of the few metal typefaces that were used in typesetting the Koran, a Naskh-style Arabic face. Amiri Quran adds color to reflect how the text appears in early 20th century printed versions of the scripture.

#### 5. **Cairo Play **(Arabic and Latin alphabets)
Mohamed Gaber, Accademia di Belle Arti di Urbino (Fine Arts Academy in Urbino, Italy)

Cairo Play is a new, weight-variable color font and member of the Cairo family. Cairo’s design is based on the Kufi calligraphy style. The Kufi style is the oldest style of Arabic calligraphy. Because it had to be drawn on parchment (before paper, which was much smoother, was widely available), its shapes are more angular than later, more cursive styles. This makes it perfect for balancing classic and contemporary tastes. Cairo Play’s Arabic component has a wide glyph set that supports the Arabic, Farsi and Urdu languages.

#### 6. **Blaka Ink **(Arabic and Latin alphabets)
Mohamed Gaber

Blaka Ink, part of the experimental Blaka family from Mohamed Gaber of Principle Design, is inspired by Kufic letterforms hand-drawn with a reed pen. Sharp edges, thick strokes, and gradients create a striking style and the appearance of overlapping ink strokes.

#### 7. **Noto Color Emoji**
Google

Last Spring we debuted Noto Emoji, a variable, monochrome version of Noto Color Emoji. And now the chromatic version is available on fonts.google, too. Coinciding with the new Unicode 15.0 Update, this launch includes 21 new characters, bringing us to a total of 3,664 emoji! Thanks to COLRv1, you can customize them all. Go ahead and give the duck a whirl.

#### 8. **Bungee Spice** (Latin alphabet)
David Jonathan Ross

This newest member of the urban signage-inspired Bungee family includes multiple gradients and color palettes for you to play with.

#### 9. **Nabla **(Latin alphabet)
Arthur Reinders Folmer, Just van Rossum

Nabla was inspired by isometric computer games (remember Q-Bert?). It makes use of COLRv1 capabilities to create perspective, with smooth gradients, sharp highlights, and blended shadows. It includes multiple color palettes and two font variation axes—one for the depth of the letters, and another for the thickness of the highlight.

Learn more about COLRv1 in “COLRv1 Color Gradient Vector Fonts in Chrome 98” on the Chrome Developers blog. Try and download all nine new color fonts on Google Fonts.

[IMAGE] Type samples with various gradient types applied.: 

[IMAGE] Type samples with six different gradient colors applied.: 

[IMAGE] Type sample comparing two different sets of gradient color values.: 

[IMAGE] Type sample showing extrusion depth and edge highlight variables at various values.: 

[IMAGE] Reem Kufi Ink specimen.: 

[IMAGE] Reem Kufi fun specimen.: 

[IMAGE] Aref Ruqaa Ink specimen.: 

[IMAGE] Amiri Quran specimen.: 

[IMAGE] Cairo Play specimen.: 

[IMAGE] Blaka Ink specimen.: 

[IMAGE] Noto Color Emoji specimen showing multicolored ducks.: 

[IMAGE] Bungee Spice specimen.: 

[IMAGE] Nabla specimen.:
