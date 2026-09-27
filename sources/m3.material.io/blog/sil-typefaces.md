# New SIL Typefaces
Source: https://m3.material.io/blog/sil-typefaces

> Expanding type for legibility and lesser-served languages

Published: 2023-03-22

SIL International recently released three typefaces for lesser-served writing systems (Tai Viet, Yi, Lepcha) used in Asia. SIL has also created Andika, which is specially designed to maximize legibility for new readers.

## SIL and lesser-served languages

SIL International has a team of type designers who specialize in creating typefaces for lesser-served or non-dominant language communities. These are communities that exist alongside larger, more prominent language communities such as Chinese, English, or Arabic. These relatively smaller communities may have their own script, or they may have sounds in their language that are not represented in the script used by the majority language. Some non-dominant languages are endangered. According to UNESCO, about 40% of the estimated 7,000 languages are at risk of extinction.

Without typefaces, these language communities can’t survive online.

“SIL International’s vision is to see people flourishing using the languages they value most. A major roadblock has been a lack of computing support. We wanted to fill in the gaps and give attention to lesser-served writing systems. Ideally a digital font needs to provide a high-quality solution that adequately represents how the community wants to use the script,” explained Annie Olsen, SIL type designer.

Olsen is one of five type designers including Victor Gaultney, Peter Martin, Becca Spalinger, and Sharon Correll on the SIL Writing Systems Technology (WSTech) team that includes calligraphy teachers and software engineers, some with over 30 years of experience. They focus on:
- Indigenous scripts that don't yet have any digital fonts
- Established scripts lacking one or more characters needed to represent sounds in a different language written in that script
- Creating a significantly different style of font for an established script
- Converting fonts with outdated encodings to Unicode

SIL’s fonts support 30 scripts around the world that are used to write thousands of languages.

Linguists, language educators, and translators often reach out to SIL to request new or updated fonts for a language. SIL works with them to identify script experts to be their main resource for information about the script.

“Before we release a font, we seek feedback from script experts. During development we will often create pre-release versions that can be used for testing. We rely on people from the language community to give us accurate information about how the characters should look. When possible, they provide documentation such as printed items, scans of printed materials, or handwriting samples,” explained Olsen.

SIL has been making typefaces for over 50 years. With all of the accumulated knowledge about different languages and writing systems, SIL built ScriptSource to gather all the information they and others have learned and share it with the wider computing and design community.

The SIL Open Font License (OFL) is widely used by font designers worldwide, not only those who are working on SIL’s own fonts. The OFL is a libre software license and permits fonts to be used, modified, and distributed without restrictions or fees.

## SIL adds new and updated typefaces for lesser-served language and scripts

**1. Tai Heritage Pro (Tai Viet script)**

Approximate population of people who use the Tai Viet script: 1,255,700

Tai Viet (tô sư ꞌTáy) is a Southeast Asian script written from left-to-right. The script has been used since the 16th century in Vietnam, Laos, China, and Thailand for the Tai Dam, Tai Dón, Tai Daeng, Thai Song and Tày Tac languages. The script has 31 consonants and 14 vowels.

SIL's Tai Heritage Pro font for Tai Viet script has been adapted to several technological advances in font-making as well as in printing since its first release in the 1990s. Its characters are based on letters drawn by Faah Baccam, a Tai Dam language speaker and artist. The Tai Dam are an ethnic group predominantly from China, northwest Vietnam, Laos, and Thailand.

Read more on SIL’s homepage, ScriptSource, Unicode, Wikipedia, Wiktionary, and Richard Ishida’s r12a site.

**2. Nuosu (Yi script)**

Approximate population of people who use the Yi script: 3,271,500

Yi (Modern Yi) is an East Asian script written horizontally left-to-right (modern) or vertically right-to-left (traditional). It is used for Nuosu Yi and other languages in the Liangshan Yi region of China. Yi signs are made from five basic strokes; dot, horizontal line, vertical line, arch and circle.

Read more on ScriptSource, Unicode, Wikipedia, Wiktionary, Richard Ishida’s r12a site.

**3. Mingzat (Lepcha script)**

Approximate population of Lepcha speakers: 57,930

Lepcha (Róng) is written from left-to-right. The name "Mingzat" means "treasure of letters" in the Lepcha language. The Lepcha script is derived from Tibetan writing and has been used since the 18th century in India, Nepal, and Bhutan for the Tibeto-Burman Lepcha language.

SIL font developers worked with Jason Glavy, the original designer of this custom-encoded font, to first re-license it under SIL’s Open Font License (OFL), and then upgrade all of the characters to the Unicode Standard for Lepcha script. This led to further re-engineering to meet current industry standards, and it was finally released along with all source files.

Read more on SIL’s homepage, ScriptSource, Unicode, Wikipedia, Wiktionary, Richard Ishida’s r12a site.

## Smart font technology

SIL uses OpenType font shaping technology to address type complexities found in writing systems, such as:
- Alternate character forms
- How letters are joined together depending on their position in the word. For example, in Arabic, letters shift shapes depending on whether they are at the beginning, middle, or end of the word and depending on which letter comes before or after.
- Diacritic (accent mark) positioning: if there are two diacritics written above a certain letter, the font engineer writes OpenType code to position each diacritic properly.

Below is an example of how SIL uses OpenType technology to modify the position of the glyphs in the word "ngom" (meaning "to wrestle") in the Tai Dam language written using the Tai Viet script.

**Step 1**: The word without OpenType technology. Two letters collide.

**Step 2**: The OpenType code for this font first substitutes an alternate glyph (2) for the taller one (1). However, the tone mark (3) is still too close to the shorter glyph.

**Step 3**: Next the OpenType code moves the tone mark from its original position (3) a bit higher (4) to give adequate space between the strokes.

**Step 4**: The final rendering of the word after OpenType code was applied.

SIL also uses their own libre and open source Graphite font shaping technology, that was developed to handle complex scripts which OpenType can’t handle yet.

## Andika: a typeface for global literacy

Literacy specialists working with people learning to read the Latin script, used for hundreds of languages around the world, asked SIL to make a font specifically made for legibility. SIL designed the Andika typeface to maximize legibility, especially for new readers of all ages. The SIL team combined the wisdom of modern literacy specialists, typeface designers, and scientific researchers to accomplish that goal. Andika also supports all languages that use the Latin and Cyrillic scripts. Andika means "to write" in Swahili, a language widely used in eastern and southeastern Africa.

One of Andika’s techniques is to clearly distinguish similar letterforms and numbers. Andika has letterforms that are easier for new readers, such as the one-storey “a” and “g”.

See these examples of Andika’s legibility and letter recognition features:

- Andika’s ascenders allow enough room for diacritics.
- The visual separation of the strokes in the lowercase “m, n, p, and r,” may make it easier to distinguish the letterforms. The top triangle of white space between the stem and the curve is just a little larger than in other typefaces.
- The open counters in the lowercase “c” and “e” make letters appear larger than they are. Open counters help with recognizing any similarly shaped letters as there is a little more separation of black from white. The open counters help in recognizing turned letterforms, such as "ɔ" and "ə" in the language Anii of West Africa.

- The Latin uppercase "I,” lowercase “l,” and numeral “1” are designed to look distinct.
- The subtle emphasis added to the beginning and end strokes in lowercase “h,” “n,” “a,” and “u” is similar to handwritten forms and gives clearer definition to the letters. It's also a helpful model for students learning to write, as the hand tends to make similar subtle movements when pulling letter strokes from top to bottom and then moving to the right for the next stroke.
- The ascenders and descenders in the lowercase “f,” “y,” and” j” have clear curves.
- In the lowercase “r,” the curved stroke finishes with a downward movement to prevent the combination of the lowercase letters “rn” from being misread as “m”.

- The lowercase “o” and “a”: When using the simpler, single-story or primary form of a, it needs to be distinct from o.
- Lowercase “a,” “g,” “d,” “q,” “b” and “p”: The bowls are different from one another.

**Including both single-story and double-story options**

The simpler single-story lowercase “a” and “g” are preferred by literacy specialists for new readers because they are easier to imitate in handwriting. The double-story versions, also known as the typographic forms, are included in the font for literacy materials for more experienced readers.

Google Fonts has increasing support for non-dominant languages via the Noto project, and also by hosting fonts from type foundries like SIL who address underserved communities.

You can also find typefaces (Atkinson Hyperlegible, Lexend, and Readex Pro) designed for better legibility. To learn more about accessible text, read the Google Fonts Knowledge articles on how type influences readability and introducing accessibility in typography.

Other SIL fonts on Google Fonts:
- Abyssinica
- Alkalami
- Charis SIL
- Gentium Book Plus
- Gentium Plus
- Harmattan
- Lateef
- Padauk
- Scheherazade New

—

Annie Opitz Olsen is a graduate of the University of Nevada Reno. She worked as a printer and calligraphy teacher before joining SIL International in 1987. Since 2003 she has worked exclusively on SIL's Writing Systems Technology team, primarily as a font designer. She gave a type design workshop in Bangalore, and was a speaker at ATypI 2009 in Mexico City.

To learn more:
- Andika
- Lepcha
- Nuosu
- ScriptSource
- SIL International
- Tai Heritage Pro

[IMAGE] Five lines of text in Tai Heritage Pro: “Ải Mák Hút Sen Pau” (“Mr. Countless Warts”), Tai Dam folk tale

[IMAGE] Three columns of text: Epic Yi poem (Axmo Hnixsse "Mother’s Daughter")
1. Poem in Nuosu 2. Transliteration 3. Translation in English

[IMAGE] Black text on beige background: The word “tyâng” for “whole” in the Lepcha script.

[IMAGE] Letterforms in black and red: 

[IMAGE] Letterforms in black, green, and red with numbers in circles: 

[IMAGE] Letterforms in black, green, and red with numbers in circles: 

[IMAGE] Black letterforms: 

[IMAGE] Three lines with three sets of letters: 

[IMAGE] Two lines with four sets of letters: 

[IMAGE] Two lines with three sets of letters: 

[IMAGE] One line with two sets of letters: Lowercase “a” and “g”:
1. Single-story forms 
2. Double-story forms
