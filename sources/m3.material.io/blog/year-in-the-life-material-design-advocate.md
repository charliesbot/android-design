# A Year in the Life of a Material Design Advocate
Source: https://m3.material.io/blog/year-in-the-life-material-design-advocate

> What it’s like to be a Design Advocate at Google

Published: 2021-04-22

##### _Illustration by Mariah Barnaby-Norris, article originally published in Design Systems For Figma._

My name is Yasmine Evjen and I lead the Material Design Advocacy team at Google. We’re a team of designers and developers helping others build beautiful, human-centered experiences with Material Design. Advocacy (also known as Design or Developer Relations) is a role that requires us to be present and responsive in our communities, responding to their needs as they show up.

My team helps designers and developers build with our guidelines, code components, and tools through education, resources, and hands-on partner engagements. Some days are spent writing blog posts, creating a talk, or filming for our YouTube channel. Other days are spent creating resources, such as interactive examples and tools to help make it easier to build beautiful UI on Android, the Web, or Flutter. We also work directly with external partners and internal teams across Google to help them implement and gain actionable feedback on how we can improve the Design System. And we get paid to tweet.

I tried really hard to do a “day in the life” post and even explored doing a month-overview, but the reality is that the format could never capture the full story. I decided to do a “year in the life,” focusing on Google I/O. In Developer Relations (DevRel) at Google, the first quarter of the year is all centered around getting ready for Google I/O, our annual developer conference that usually takes place in May. There are products to finalize, narratives to develop, physical sandboxes to plan, demos to build, and content to create.

## Google I/O 2018

At Google I/O 2018, we introduced Material Theming, a system capability to customize Material Design to reflect your unique style. With a few changes to your global typography, color, and shape styles, you can create a custom theme that’s consistent across your entire product. With the 2018 release, we updated our guidelines, code components, and launched tools like the Material Theme Editor sketch plugin. We worked with the Material team to create codelabs, design labs, videos, and work with partners to support the launch.

While there was a lot of excitement with the announcement of Material Theming in May 2018, we saw that a lot of app developers struggled with how to incorporate it into their existing apps. We had these beautiful Material Studies, but didn’t have great resources for how to break down the theming process into easy-to-replicate steps. There was still a big disconnect between what designers were creating with our Theme Editor plugin and how developers could implement it in code. In the Fall of 2018, I really began to dig into this; I asked, _“How might we show the robust capabilities of theming in our code components?”_

While I had spent my most recent years as a UX designer, early in my career I did front-end web development. I wanted to understand how a developer would start to theme our components, I teamed up with Lynn Jepsen, who was the Material Design Components for Web engineering lead. She set up my dev environment and helped me when I got stuck, like the time I accidentally broke the terminal. I still don’t know how I broke the terminal, but Lynn came over to my desk and said, “That was a big whoopsie no-no” and fixed it. Thanks, Lynn. We worked on some demos together, like Adopt a Pup which showed how to customize an app’s theme by changing a few variables. We used that demo to develop a talk, showing how to get started with Material Design Components and Theming on the web. Lynn and I were known to be pretty loud in the office, and we started a big enough commotion that others from the engineering and design team started to get involved. Team members like Bonnie Zhou started building templates and starter kits. Material Designer, David Allin Reese, was inspired by how easy it was to theme with code, he built a series of Material themed buttons to show off how to bring a brand style to life with our components.

In parallel, I was talking to Michelle Alvarez, a lead designer on the Material Design team who worked on Material Theming. We spent hours trying to break down and teach the visual design principles found in Material Theming into digestible concepts. During this time, Jina Anne from Clarity reached out and invited us to speak at the design systems community conference in December 2018. I knew this would be a perfect opportunity to share how we developed Material Theming and go into details for all the subsystem categories. I just needed to convince Michelle to give the talk with me. I had been bugging Michelle for a while to speak or create a video, but anytime I brought it up she would quickly say, “No, I’m not a public speaker!” But if you know anything about advocates, we are incredibly persistent. I’m not sure what finally convinced her, but I reassured her that I would coach her through each step and we would co-develop and co-present the content, she finally agreed.

In December 2018, we flew to New York City and gave our talk Material Theming: Building an Expressive Design System. In the talk, we shared how Material was broken down into various subsystems that could be customized (Type, Shape, and Color). A system of systems so to speak. We shared the tools and resources that you could use to help you customize your theme. The talk was well-received:

##### @materialdesign saw a lot of criticism at #clarity2018, but @YasmineEvjen and @michalva show us how Google is using theming and extensibility as a means to empower its users to design unique apps through variations on type, shape, and color. pic.twitter.com/Xd6uxKWu5i

##### — Asia Hoe (@asiahoe) December 12, 2018

It was a great way to wrap up the year.

## Google I/O 2019

Early in 2019, I received a message from Michelle, “Rachel [Been, Material Design’s Creative Director] wants us to give our Material Theming talk at I/O 2019.” Although I had been in DevRel for 2 years, I hadn’t given a talk at an event as big as I/O. I had a moment of panic then said, “Let’s do this!”

We wanted to make this version of the talk more developer-friendly, I had the ambition to showcase code changes in a live demo. There was also another problem that we had to creatively solve around. In our original talk, we relied on the Theme Editor plugin to showcase how to apply theming to a set of components. And while it was a great tool for starting new projects in Sketch, it didn’t easily integrate with existing source files and didn’t serve the needs of designers and developers on other tools. There were discussions of retiring the Theme Editor, and while no official decisions had been made, we didn’t want to showcase a tool whose future was _sketchy_.

Our program manager, Dylin Martin, was leading the creation of the Material Design I/O sandbox experience. We were brainstorming ways to showcase how customizable Material was and she had this brilliant idea, would it be possible to create and print actual sticker sheets of our components? A personalized Material Theme that people could walk away with. I said, you know we could easily build this with our code components. All we need to do is recreate our “sticker sheet,” adjust the theming variables and boom people would have their own sticker sheet AND actual code they can begin developing their apps with. Dylin began organizing the team, she recruited David Allin Reese to be the lead designer, Bonnie Zhou and Hunter Stich to be the lead developers, and I functioned as the product and DevRel lead.

We started developing this for the web and Android, using Glitch would allow people to duplicate our “Build a Material Theme” project and have their very own customized set of components available to begin building with right away. We were so excited, we were going to use these tools in our talk to showcase how a few theming values can shape the look and feel of a design system. Attendees would be able to get a customized theme within the sandbox, and viewers at home would be able to create with it regardless of location.

Once we got the app into a functional demo, I began writing the documentation, and worked with content strategist Barbara Eldredge on a communication plan and how we would showcase it on the site. I reached out to Glitch to give them a head’s up and they wanted to partner with us. Their team helped us fix issues we ran into, and at launch they showcased the project on their homepage and wrote an article on their blog.

At Google I/O 2019, Michelle and I delivered our talk _Material Theming: Build Expressively with Material Components_. We launched the “Build a Material Theme” interactive project for web and Android. The physical sandbox drew attendees and the Material Design team created custom themes for them onsite. I even got a selfie with Sundar!

This was an incredibly special moment for me. In 2014 I was on my couch watching Matias Duarte announce Material Design at Google I/O, that day I told myself that I wanted to work at Google. A year later in 2015, I attended my first Google I/O and watched the Material Design Awards. In 2017 I joined Google to help support the 2018 launch of Material Theming. And in 2019, 5 years from its initial announcement, I was on stage giving a talk about Material Design on a product I helped build. Shortly after, I became the technical lead and manager for the Material Design Advocacy team.

Whenever someone asks me, “what does a typical day look like for a Material Design Advocate,” I have to start by saying that’s an incredibly hard question to answer because there isn’t such a thing as a typical day, which I absolutely love about my job.

[IMAGE] Photo of Yasmine and Liam in front of a large presentation screen: On stage with Liam at Google Developer Days

[IMAGE] Animation showing various components changing between themes: Material studies showcase the flexibility of Material Theming and components to create expressive and unique apps

[IMAGE] Animation showing code changes reflecting in a sample app screen: Changing theming variables to show Material Theming in the Adopt a Pup sample app

[IMAGE] Animation of apps changing from similar designs to very distinctive designs: Material Design apps being transformed by Material Theming

[IMAGE] Animation of apps with different color, shape, and type values: Material studies showcasing the customizable Material Theming categories

[IMAGE] Yasmine coding on stage while Liam looks up at the screen: Demoing the Build a Material Theme project for Android on stage

[IMAGE] Animation of code manipulation in the Build A Material Theme tool: The Build a Material Theme project on Glitch

[IMAGE] Photo of the Material sandbox at Google IO 2019: David Allin Reese and Bonnie Zhou in the Material Design sandbox at Google I/O 2019

[IMAGE] Yasmine and Michelle in front of a large audience, with a presentation screen on either side: On stage with Michelle Alvarez at Google I/O 2019

[IMAGE] Grid of photos showing people from the team waving their hands: Members of the Material Design team
