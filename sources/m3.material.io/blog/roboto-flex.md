# Roboto … But Make It Flex
Source: https://m3.material.io/blog/roboto-flex

> Google’s most popular font gets customizable with the launch of Roboto Flex

Published: 2022-05-05

Get it on Google Fonts.

There’s no perfect typeface that works for every size, every device, every application, every style, and every mood. But as the default for Android, with over 2.5 billion active users spanning over 190 countries, Roboto needs to be as flexible as possible.

Roboto is Google Fonts’ most popular download (you’re reading this blog post in Roboto). It was first released all the way back in 2011, but it’s been updated over the years to improve its language support and aesthetic qualities. The Roboto superfamily has grown from the initial sans and condensed to span Roboto Slab, Roboto Mono, and—most recently—Roboto Serif.

Now there’s another Roboto joining the lineup: Introducing Roboto Flex—a major upgrade to Roboto and Google Fonts’ biggest project to date. With a huge range of weights and widths across optical sizes, plus additional capabilities for fine-tuning, Roboto Flex was designed by Font Bureau to be super scalable, adaptable, customizable, and optimizable.

## A variable font with 12(!) axes

Like its siblings, Roboto Flex is a variable font. Using a relatively new version of the OpenType file format, variable fonts allow one file to contain multiple stylistic variations. The benefits of this are often summed up in the rubric “compress, express, finesse:”

“Compress” refers to their file compression abilities, while “express” and “finesse” are about giving people a larger palette (“express”) and more control (“finesse”) over their designs. The express and finesse benefits are made possible by a variable font’s “axes,” which give people the ability to choose and combine any value(s) along them, instead of being stuck with the limited options defined by type designers.

On first glance, Roboto Flex probably looks familiar. In fact, beta versions have been in use on this blog for headlines since last year. And if you study the glyph set at size 14 (its default), you might have a difficult time finding any differences between it and the original sans Roboto (though there are a few small ones, shown below). But Flex is designed to take full advantage of what variable fonts technology can do; it’s a reimagining of Roboto for the countless and ever-changing ways people read today.

“Everyone is switching between devices, resizing browsers, and spreading their viewport across multiple screens,” said David Berlow from Font Bureau when asked about the intent of Flex. "Fortunately, scaling, adaptability, and responsiveness are all questions variable typography can answer.”

## Weights, widths, and optical sizes

Optical sizes are different versions of a typeface optimized for use at specific sizes or size ranges, and are common in “pro” or “elite” typefaces. But rarely do you see a variable font like Flex, which has a wide range of optically sized weights and widths; most have a range of weights and widths, but all for one optical size … so you get a lot of flexibility stylewise, but you’re restricted to one size. Or, the fonts have a wide range of optical size designs, but the range of weights and widths doesn’t change much … so you get flexibility in the sizes you can use, but your expressive style choices are limited.

Roboto Flex offers both: a huge range of weights and widths across a complete stack of optical sizes. And this gives designers new creative opportunities with Roboto. “The extremely light and narrow, or bold and wide styles that are now available in Flex’s largest optical sizes are so extreme, they are astonishing to me,” said Dave Crossland, Lead UX Programs and Operations Manager for Google Fonts. “And you don’t have to worry about them being illegible at small sizes, because the 1,000 weight at 144pt is not as extreme at 10pt. Everything in between is smoothly balanced too, for a very even typographic color, and this really simplifies producing high-quality typography.”

## User axes

Most variable fonts today include “user axes,” which offer expressive options to customize type. Type designers can create axes for almost anything. For example, axes have been created for “Casual,” “Softness,” “Cursive,” and even “Wonky.” But the most common user axes are the staples of typography: Weight, Width, Italic, Slant, Grade, and Optical Size. The only one of these six that Roboto Flex doesn’t contain is Italic—and that’s because it has an equivalent Slant axis, so it isn’t necessary.

## Parametric Axes

But what if you want even more control? What if you want to make granular tweaks, like increasing just the stroke weight of your characters, or decreasing the depth of your lowercase descenders? What if you want to harmonize the heights of two different fonts? That’s where parametric axes come in.

Unless you’ve tried out Amstelvar Alpha (also from Font Bureau), the first experiment in creating a variable font enabling parametric design, you may be unfamiliar with the concept of parametric axes. Parametric axes are used to fine-tune a design by giving designers the ability to alter minute details of a font.

Flex has seven parametric axes that can be used individually or together. Here’s an overview of them:
|

**1. Parametric Counter Width** (XTRA in CSS)

The Parametric Counter Width axis alters the transparent forms inside and around glyphs in the X dimension. One application is in fine-tuning justification, as it changes the number of characters per line.   | 
|

**2. Parametric Thin Stroke** (YOPQ in CSS)

The Parametric Thin Strokes axis alters stroke widths at their thinnest parts, typically in the Y dimension for the Latin script. One use-case is to improve legibility in very small sized text, because it can lower stroke contrast.   | 
|

**3. Parametric Lowercase Height** (YTLC in CSS)

The Parametric Lowercase Height axis alters the volume of all space within the lowercase's vertical alignment zone: the vertical space inside counters of glyphs and the space in their sidebearings. (This adds more space vertically instead of changing the side bearings like tracking does).

This is not truly an “x-height axis," since it only adjusts space vertically, and x-height proportions are formed by the relationships of uppercase, lowercase, ascenders and descenders, as well as horizontal forms. There are parametric axes for all of these.   | 
|

**4. Parametric Uppercase Height** (YTUC in CSS)

Similarly, the Parametric Uppercase Height axis alters the volume of all space within the uppercase's vertical alignment.   | 
|

**5. Parametric Ascender Height** (YTAS in CSS)

The Parametric Ascender Height axis alters the volume of all space within the ascenders’ vertical alignment zone.   | 
|

**6. Parametric Descender Depth** (YTDE in CSS)

The Parametric Descender Depth axis alters the depth of the space within the descenders’ vertical alignment zone, which has a negative value being below the baseline.   | 
|

**7. Parametric Figure Height** (YTFI in CSS)

The Parametric Figure Height axis alters the vertical space of figures. It can be used for aligning figures to other vertical zones.   | 

Together with Flex’s wide range of weights and widths across optical sizes, it’s this level of precision that makes it so adept at performing on—and switching between—the biggest wallscreen and the smallest handheld device.

“With this many ways to gain more control over your design, Roboto Flex can help you solve just about any typographical challenge you may have,” said Berlow.

On the other hand, Flex is also designed to work out of the box, at any size, and what really excites Berlow is being able to help save people from the tedious parts of typography: “If someone has been using another font where they constantly had to change a lot of things to make it work … and then with Roboto Flex and automatic optical sizing they don’t have to adjust tracking or anything else; they can just leave the weight at 400 for all the styles … That will mean they can go home an hour earlier, or spend an hour playing outside. That little change will make me happy.”

Roboto Flex is open source and available now from Google Fonts in Latin, Greek, and Cyrillic.

●●●

## About the type designers

**David Berlow**, the president of Font Bureau, is a 44-year veteran of the type industry. Beginning his career in 1978 at Mergenthaler-Linotype, Stempel, and Haas, he moved on to one of the first digital type foundries, Bitstream, in 1982. Berlow began consulting and sub-contracting to Apple Computer in 1989, leading development of the era-defining fonts for macOS 7 that introduced the TrueType format, and later the first variable font, Skia. His fonts are distributed by Google, Apple, Microsoft, Adobe, Monotype, Morisawa and Type Network. He has been the recipient of lifetime achievement awards from both the Type Directors Club and the Society of Type Aficionados. fontbureau.typenetwork.com

**Santiago Orozco** is a type designer and engineer, based in Monterrey, Nuevo León, México. With a background in computer science at the University of Monterrey (UDEM), he found himself at the intersection between design and technology. In 2009, he Founded Typemade Foundry, and now specializes in type design, font production, and type technology. typemade.com

**Irene Vlachou** is a typeface designer based in Athens, Greece. She holdsan MA in Typeface Design from the University of Reading. Irene has collaborated with international type foundries and corporations, working as a typeface designer and a consultant for Greek typefaces. From 2013 to 2019, she was a senior designer and variable font expert at Type-Together. She currently works full time as a freelancer typeface designer specializing in OEM/System fonts. Since 2017 she is a mentor on the GSOC program on behalf of GFOSS, on expansion of Greek litre fonts. For the spring semester of 2022, Irene is an artist-in-residency at La Becque and a visiting professor at the Master program of Typeface design at ECAL, Lausanne. ivtype.com

**Ilya Ruderman** is a type and graphic designer and teacher, who lives and works in Barcelona. He is a graduate of the Moscow State University of the Printing Arts (2002), where his graduation project was done under the supervision of Alexander Tarbeev. He has a MA degree in type design from the Type & Media programme at the Royal Academy of Art in the Hague (2005). After completing the programme, he returned to Moscow where he has collaborated with a number of media organizations: Kommersant, Afisha, Moskovskiye Novosti, Bolshoi Gorod and Men’s Health Russia. In 2005–2007 he was art director for Afisha’s city guidebooks, and 2007–2015 he supervised the curriculum in type and typography at the British Higher School of Art and Design in Moscow. He has been sought out as an expert consultant on Cyrillic type design by international foundries since 2008. In 2014 he founded the foundry CSTM Fonts with Yury Ostromentsky, and in 2016 the type.today font store with a focus on Cyrillic typefaces. type.today/en/designer/ilya-ruderman

**Yury Ostromentsky** is a graphic and type designer, and co-founder of type.today store. Graduate of the Moscow State University of Printing Arts (Department of Arts and Technical Design of Printed Materials). Yury worked as a designer and art director for publishers and design studios. From 2004 to 2012, he was art director with Bolshoy Gorod (Big City) magazine. In 2004, he and lya Ruderman, Dmitry Yakovlev, and Daria Yarzhambek launched the DailyType webpage. Later in 2014, Yury and Ilya Ruderman founded CSTM Fonts type design studio which released Pilar, Big City Grotesque, Kazimir, Navigo, Normalidad, RIA Typeface, Lurk, Loos, Maregraph typefaces and CSTM Xprmntl series, as well as Cyrillic versions of Druk, Graphik, Spectral, Stratos and Apoc. The works by Ostromentsky and CSTM Fonts were awarded by European Design Award, Granshan and Modern Cyrillic Competition. studio.type.today/en/designer/yuriy-ostromentskiy

**Mikhail Strukov** is a type designer, graduate of Ilya Ruderman’s course at BHSAD (Moscow) and Frank Blokland's program at Plantin Institute of Typography (Antwerp), where he developed a deep interest in history of type and evolution of type production. Collaborates with CSTM Fonts and Samarskaya & Partners, working mostly on Cyrillic versions for both custom and retail typefaces. When not busy with designing Cyrillic, he reviews, consults and writes about Cyrillic. Smalltown resident, road cycling fan, local communities builder.

## About the type foundry

Formed in 1989, Font Bureau has been active in the design of typefaces, the development of tools for both type design and typography, and as a consultant in the development of font technology for both operating systems and applications.

Font Bureau’s custom fonts are seen in hundreds of publications world-wide. Its retail font library is popular among designers of a wide range of projects, and its pioneering work in variable font technology has set examples for both the finessing of typography for better composition, and new levels of expression that type can bring to reinforce user interest in any design. fontbureau.typenetwork.com

#### Art direction by Zak Klauck, Animations by Q Choi

[IMAGE] 3.01% in Roboto Flex overlaid by an outlined version in the original Roboto: Font Bureau’s house standards for quality show up in a few subtle updates to Roboto’s neo-grotesque sans design, including switching the default numeral style from tabular lining to proportional; and making the zeros in the percent symbol not deviate from the regular numeral zero design.

[IMAGE] Six words set in different styles of Roboto Flex: Samples of Roboto Flex’s huge range of weights and widths at size 144

[IMAGE] Five animated letter “o” characters: Demo of Roboto Flex’s five user axes: (1) Weight (2) Width (3) Optical Size (4) Grade (5) Slant

[IMAGE] Animated letter “O” character changing counter width using the Parametric Counter Width axis in Roboto Flex: 

[IMAGE] Animated letter “O” character changing the stroke width where it is thinner, using the Parametric Thin Stroke axis in Roboto Flex: 

[IMAGE] Animated letter “o” character changing in height using the Parametric Lowercase Height axis in Roboto Flex: 

[IMAGE] Animated letter “O” character changing in height using the Parametric Uppercase Counter Height axis in Roboto Flex: 

[IMAGE] Animated letter “d” character changing the height of its ascender using the Parametric Ascender Height axis in Roboto Flex: 

[IMAGE] Animated letter “p” character changing the depth of its descender using the Parametric Descender Depth axis in Roboto Flex: 

[IMAGE] Animated number “8” character changing its height using the Parametric Figure Height axis in Roboto Flex: 

[IMAGE] Decreasing sizes of text set in Roboto in a left column and Roboto Flex in a right column: Left: The original Material Design type ramp using Roboto. Right: The same ramp made simple and smoother with Roboto Flex’s regular weight with automatic optical size variations.
