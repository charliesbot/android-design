# Looking for What’s Missing
Source: https://m3.material.io/blog/asset-people-3

> A guide to including world-building narratives in a designer’s toolkit

Published: 2022-03-31

When I visit the workshops of friends who are makers and builders, their project areas are surrounded by the tools and supplies they need to get their work done. Hammers, wrenches, and pliers are organized on a pegboard wall or in drawers ready for their moment to drive nails, tighten bolts, and bend wires. Like books in a library, the tools are cataloged and labeled not just for display, but for accessible and unambiguous use. If the tools are misplaced or impossible to find, then projects take longer to make and the experience is fraught with frustration.

The needs of physical-object makers are not wildly different from the needs of digital makers, like those who are building products with Material. Material is a collection of stuff to build things with. If that stuff isn’t set up within easy reach from the designer’s project space, then things are a lot harder and more frustrating to make.

Our team of designers discovered this for themselves in the process of illustrating different interface scenarios throughout our Material 3 guidelines. To help create thousands of images, I had already put together a library of backgrounds, device frames, annotation labels, and touch targets for the team to use as a way to manage consistency across guidelines. However, the designers were still finding themselves improvising on images, names, messages, media, and regional content. They had to insert what they knew from the same few parts of the world because it was much easier and faster than to look through a detached document of vetted content that wasn’t organized into the context of their workflow.

The realistic names and backstories that make up the collection of new profiles were missing from the easy-to-reach toolkit in the designer’s digital workshop. I needed to figure out a way to push the boundaries of Figma, the design software that our team uses, to deliver this content.

In my experience in the field of production design, these kinds of challenges are not rare. It’s part of the fun. I play keyboard hot keys like a piano, zipping through complex design software looking for the most clever ways to solve complex technical design problems. It’s a niche arena where I get to play with both the technical and the visual to make beautiful things robust, responsive, and alive.

## How to manage content with a design tool

Figma is a design tool that is set up to serve a visual-to-code process, but not a tool set up for content management. Since I was already using Figma’s library feature for the rest of the asset creation workflow, it was clear that was going to be the best place to insert this type of raw content—I just had to figure out how.

**Libraries** in Figma are sets of **components** and styles that are easily searchable. Components are elements connected to a main version that can be duplicated. Edits are made to the main version to then be dispersed to every connected duplicate, which is called an **instance**.

Including the profiles in a Figma library was an interesting puzzle to solve because it wasn’t only a matter of supplying licensed images to designers, I also needed to attach text that was tied to specific people, relationships, and scenarios independent from design elements.

I began by organizing the elements for the three profiles to observe patterns and outliers. All three had social circles, news articles, and audio media, but only Lee’s profile had a map showing live music venues while Sam’s profile included movie posters.

Then I took care of the features that Figma already intuitively supports by following these few steps:

1. Organize component **variants** (different expressions of one component) into categories. Design the categories to be descriptive and straightforward. Our library has a component called Social Circle, for example, which includes avatars for everyone the main person interacts with.

2. Use component **overrides** to bake in any available options, such as the name and relationship of the chosen social avatar. This will make something like a chat UI much easier to put together. Knowing the relationship between the interacting avatars will also inform the content and context of the scenario.

3. Group each collection of categories by profile so that they can be expanded or collapsed depending on who the designer chooses to use.

4. Color code the backgrounds of each asset person’s components as a supportive visual shorthand.

5. Provide any useful metadata to the components which will appear as hover-activated tooltips. The metadata can hold suggested uses as well as world-building context. For our Music components, which contain the licensed images of mock album covers, the tooltip suggests placing them in search results, media players, lists, and messages. It also includes a note about what kind of music the person prefers, like Sam listens to gentle classical and ambient music.

While metadata is helpful for context, a designer isn’t able to select, copy, and paste it. I wasn’t sure at first how I was going to provide easy access to the emails between siblings, message updates from a dog sitter, the tracklist for an album, or news headlines and articles tied to specific photographs.

My solution was to co-opt property overrides to allow the content to appear temporarily within the design space:

6. Provide raw text through property override toggles in the component variant which will place the text temporarily in the file so it can be copied and pasted into the design. Then the toggle can be turned off. This will keep designers in their workspace and not looking for a separate source document. Official edits to the text can still be made to the original component and it will disperse throughout all live instances.

7. Not everything will have extra raw content, so indicate which ones do to increase the ease of discoverability. A designer making an email interface will benefit from knowing who in the social circle has that kind of content available. I chose to indicate them with an asterisk (*) in the variant list and provided a note about the asterisk in the metadata tooltip.

8. Add any additional contextual information to the property panel. This is ideal for content that is important to know, but doesn’t need to be copied and pasted, like someone’s pronouns or how many episodes a podcast has.

The results are three different worlds organized into a concise, robust, customizable, and instructive group of components representing hundreds of unique pieces of content. Being concise is fundamental to usability. No one needs to use their time looking for a needle in a haystack when the needle can easily be threaded with licensed images, corresponding text, and all the metadata needed for designers to begin sewing together thoughtful and consistent designs.

[IMAGE] : An element converted into a component demonstrating how a change will automatically appear in connected instances in any file that has placed them from a shared library.

[IMAGE] Part of the analysis of various elements that make up Lee’s profile: Part of the analysis of various elements that make up Lee’s profile

[IMAGE] This social circle component contains nine image options with corresponding names, message text, relationship context, and other metadata.: This social circle component contains nine image options with corresponding names, message text, relationship context, and other metadata.

[IMAGE] Fully expanded library on the left and collapsed view on the right: Fully expanded library on the left and collapsed view on the right

[IMAGE] A tooltip showing metadata suggesting best uses for Ping’s media type: A tooltip showing metadata suggesting best uses for Ping’s media type

[IMAGE] The extra content toggle selected showing raw selectable text to accompany an album cover.: The extra content toggle selected showing raw selectable text to accompany an album cover

[IMAGE] Lee’s social circle variant with asterisk selected to indicate there is message text attached: Lee’s social circle variant with asterisk selected to indicate there is message text attached

[IMAGE] Podcast with ten episodes selected in the variant list supported with contextual information: Podcast with ten episodes selected in the variant list supported with contextual information

[IMAGE] An overview of the final library containing 28 components that represent 218 unique pieces of visual and written content: An overview of the final library containing 28 components that represent 218 unique pieces of visual and written content
