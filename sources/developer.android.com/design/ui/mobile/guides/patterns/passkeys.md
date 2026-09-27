# User authentication with passkeys
Source: https://developer.android.com/design/ui/mobile/guides/patterns/passkeys

Passkeys are a safer and more convenient replacement for passwords. With passkeys, users can sign in to apps and websites using their device's built-in authentication features, like fingerprint, face, PIN, or pattern. This provides a seamless sign-in experience, freeing your users from having to remember passwords.

## Key points

Keep in mind the following considerations when using passkeys:
- Use the Credential Manager API to support passkeys in your product. Credential Manager consolidates all your sign-in methods, including passkeys, passwords, and federated sign-in solutions, so there's no need to list all sign-in options up front.
- Support passkeys as the default sign-in method.
- Introduce passkeys at the right moment—during account creation, during account recovery, after signing in with a password, and within account settings—to encourage user engagement and adoption
- Provide a dedicated section within your product settings menu for managing passkeys, including an option to delete them
- Keep information about passkeys consistent throughout the product to help users learn about this new sign-in method
- Use the passkey icon provided by Google to create a consistent sign-in experience on Android

## Get started

For a simpler and more secure sign-in experience, use passkeys in your product. Passkeys reduce the need for passwords, offer a streamlined user experience, and are more resistant to security vulnerabilities such as phishing or compromised passwords.

Here are some typical user journeys where your product can introduce passkeys. You can find more details in the following sections in this document:
- Create and save a passkey for new and existing users
- Sign in with a saved credential
- Manage passkeys in your app or website settings

Keep in mind the following UX principles when designing your sign-in experience with passkeys:
- Prioritize simplicity: Minimize the number of steps required to sign up or sign in and keep the requirements for doing so straightforward
- Be transparent: Clearly communicate what data you collect and why
- Give users control: Allow users to fill optional details later

Use the Credential Manager API to support passkeys in your product. Explore the implementation guide to learn how to integrate this API.

## Create and save a passkey

We recommend promoting passkey creation in the following ways:
- New users: Promote passkey creation during account creation
- Existing users: Promote passkey creation during account recovery, after signing in with a password, and within account settings, where users are already in the account management mindset
- Consistent messaging: Maintain consistent wording and design in passkey creation prompts across your product

### The account creation moment

When users are creating a new account, they are already thinking about how they will sign in to that account in the future. This is a good opportunity to introduce passkeys and explain how they're easier and safer for sign-in.

Android 15 streamlines passkey creation, reducing it to a single-tap flow.

[IMAGE] Three screenshots demonstrating single-tap passkey creation on Android 15. The
first screen shows a signup form. The second screen offers to create a passkey
with a single tap. The third screen confirms account
creation.
_Single-tap passkey creation on Android 15_

Users on Android 14 and earlier will continue to use the two-tap flow.

[IMAGE] Four screenshots demonstrating single-tap passkey creation on Android 14 and
lower. The first screen shows a signup form. The second screen offers to create
a passkey. the third screen prompts the user confirm their screen lock with a
fingerprint. The fourth screen confirms account
creation.
_Two-tap passkey creation on Android 14 and earlier_

#### Make passkeys the default way for users to sign up

When users create a new account, prioritize passkeys as the default option for account creation. However, ensure users can use alternative sign-in options if they prefer.

check_circle

###### Do
Make passkeys the primary way for users to sign up.

check_circle

###### Do
Provide users with a way to sign up without creating a passkey. On the "Other options" page, continuously display the passkey option.

cancel

###### Don't
Don't list all sign-up options together as the default.

check_circle

###### Do
Make the prompt content concise to give key benefits of passkeys. Provide a button or link that users can use to learn more.

### The account recovery moment

Users who tried to recover their accounts using their old password and failed may be more likely to adopt passkeys.

#### Prompt users to create a passkey when they reset their password

Encourage users to create a passkey when they try to reset their password. At this point, users are experiencing the frustration and inconvenience of forgotten passwords, making them more receptive to the security and convenience that passkeys offer. Consider prompting users to create a passkey for other account recovery use cases.

[IMAGE] Screenshot
_Prompt users to create a passkey when they reset their password_

[IMAGE] Screenshot
_Prompt users to create a passkey at the end of password reset as users may find it time-consuming to create and save a new password._

#### Guide users how to fix a problem with their passkey

When users request help to fix a problem with their passkey, add troubleshooting steps or link to our help center article.

[IMAGE] Screenshot

### The moment immediately after signing in with a password

Users can forget their passwords, leading to frustration during sign-in. To prevent this, encourage them to create a passkey immediately after they sign in with a password or other method.

[IMAGE] Screenshot
_Display a passkey prompt immediately after signing with a password or other method._

### The account management moment

For existing accounts with passwords or other sign-in methods, display a passkey prompt in the account settings to help users upgrade to a passkey. Ensure that the user is not creating duplicate passkeys for the same username in the same password manager.

[IMAGE] Screenshot

### Content design guide: Introducing your users to passkeys

By prioritizing clarity, simplicity, and user benefits, you can encourage adoption of passkeys. Follow these guidelines to create a more seamless user experience when introducing passkeys in your product.

#### Highlight key benefits of passkeys before passkey creation

Since passkeys are a new sign-in method, it's crucial to clearly and concisely explain their benefits to users before prompting them to create one. Focusing on the benefits, and incorporating visuals like the passkey icon, fingerprint, or face will increase understanding and reduce abandonment.

Our research shows that users value speed, simplicity, and security. Refine your messaging to emphasize what your users care most about when signing in and using your product.

#### Use familiar language and simple explanations, and don't over explain the technology

Passkeys are advanced tech behind familiar experiences. When introducing "passkeys", let users know they allow them to securely sign in by simply using their screen lock–the same way they unlock their device, whether that's with a fingerprint, face, PIN, or other method.

Calling out these familiar security experiences decreases anxiety in adoption, and disambiguating what the experience will be for the user reduces uncertainty. Users who need more information can dig in deeper.

#### Include the term "passkey", but don't make it the central focus of your content design when introducing them

Using the term "passkey" enables user self-education and builds recognition. The industry has aligned on using this term in implementations across platforms, and many organizations are publishing deeper explanations of the technology, reducing the need for individual products to do so. Since it's a new term, avoid overwhelming users by putting it front and center when first introducing passkeys.

check_circle

###### Do
Display key benefits of passkeys before passkey creation.

cancel

###### Don't
Don't initiate the passkey creation flow without first presenting the benefits of passkeys to the user.

check_circle

###### Do
Lead with the benefits of passkeys in the header, then explain what they are and how they're used in the body.

cancel

###### Don't
Avoid emphasizing the term "passkey" in the header to avoid confusion for users unfamiliar with passkeys.

#### Prompt users with "Create a passkey"

When inviting users to start using passkeys, we recommend saying, "Create a passkey." This is consistent with the API screens owned by the platforms. Only one passkey is created at a time. Similar to passwords, passkeys are not proper nouns. They are not capitalized and generally come with an article such as "a." When referring to more than one passkey, the word can be made plural.

check_circle

###### Do
Use "Create a passkey" for the button label.

cancel

###### Don't
Don't use other button labels except "Create a passkey" to maintain consistency with Credential Manager API screens.

#### Use "Save" when conveying where passkeys are stored

Users can choose where to save their passkey, such as in a password manager. When selecting a specific password manager for their passkey storage, we recommend saying "Save a passkey to [password_manager_name]."

#### Communicate to users the status of passkey creation

Keep users informed about what's going on during account creation. This increases user confidence in passkeys and the product as a whole.

check_circle

###### Do
Display a confirmation message to let users know their passkey creation have been successful.

cancel

###### Don't
Don't skip a confirmation message after passkey creation.

## Sign in with a saved credential

The Credential Manager API displays its account selector screen that shows users a list of available accounts when they sign in to your product.
- If they have only one account, users can sign in immediately with their screen lock on Android 15; on Android 14 and earlier, they'll first confirm their account information before using their screen lock to sign in.
- If the user has multiple accounts, they first select the preferred account and then use their screen lock to sign in with a passkey.

If users can't find their account on the sign-in screen, they can tap "Sign-in options" or "More options" for alternatives, or they can dismiss the screen to manually enter a password or use other traditional methods.

[IMAGE] Screenshot
_One account on Android 15_

[IMAGE] Screenshot
_One account on Android 14 and earlier_

[IMAGE] Screenshot
_Multiple accounts on all Android versions_
(_Branding on the biometric screen is only available for Android 15_)

#### Unified sign-in

Credential Manager brings together passkeys with traditional sign-in methods like passwords and federated sign-ins such as _Sign in with Google_. Here's how it works:
- Android 13 or earlier: Passkeys and passwords are saved to and retrieved from Google Password Manager. Users can restore their passkeys and passwords on another Android-powered device by signing in with their Google Account.
- Android 14 and higher: Credential Manager works with all enabled password managers on the user's device, including Google Password Manager. The Android system gathers credentials from these services and displays them in a convenient list.

[IMAGE] Screenshot

- _One account from Google Password Manager_
- _Multiple accounts from multiple password managers_

check_circle

###### Do
Use Credential Manager to bring all sign-in methods (passkeys, passwords, federated sign-ins).

cancel

###### Don't
Don't display a separate button or link to trigger each of the supported sign-in methods.

#### Simple experience with consolidated sign-in methods

Credential Manager simplifies sign-in by consolidating the sign-in methods for each account and surfacing the safest and simplest authentication method. For example, if the user has both a password and a passkey for their account, the system will propose using the passkey.

[IMAGE] Screenshot

Users might use multiple password managers to sign in to apps and websites, which means they might have multiple saved passkeys with the same username for the same app or website. To manage this, Credential Manager organizes them by last-used time and displays one. However, if the user prefers to use a different method, they can tap "More options" or "Sign-in options" to choose an alternative.

[IMAGE] Screenshot

#### Content style guide for sign-in

When prompting users to access their account through the Credential Manager's sign-in screen, use the phrase "Sign in", or specify the credential type with "Sign in with a passkey" or "Sign in with a password".

"Sign in" (no hyphen) is the verb form. "Sign-in" (hyphen) can be used as a noun to identify the authentication experience or as a pronoun.

Although "Sign in" and "Log in" are often used interchangeably, we recommend using "Sign in" to align with the terminology used in platform-specific API screens. This promotes a more unified experience.

## Manage passkeys in your app or website settings

Provide your user with access points to manage their passkeys in accessible, helpful locations.

#### Invite users to manage their credentials at key points in the settings

[IMAGE] Screenshot

- _Before creating a passkey_
- _After creating passkeys_
- _After tapping on "Manage" button_

#### Make passkey information easy to scan and understand

Unlike passwords, which are tangible combinations of letters, numbers, and symbols, passkeys are largely invisible to the users. To ensure clarity within your settings UI, it's crucial to display helpful information about each passkey. This includes at least the following details:
- Name and icon of the password manager that syncs the passkey
- Timestamp of when the passkey was created and used the last time
- An option to delete the passkey from the account

A user may need to delete the passkey on your product and then also from their password manager to fully delete it, but once it's deleted in either location, it will no longer work.

check_circle

###### Do
Display the name and icon of the password manager that syncs the passkey.

cancel

###### Don't
Don't highlight the device name where the passkey was created with a password manager. Users might think the passkey is saved on that specific device.

check_circle

###### Do
Offer an ability to delete a passkey.

cancel

###### Don't
Don't use other terms except "delete" to maintain consistency with Credential Manager API screens.

check_circle

###### Do
Provide an option to delete a passkey.

check_circle

###### Do
Consider displaying a passkey prompt again if they delete all of their passkeys.

## Use the Google passkey icon

Use the Google passkey icon to create a unified user experience for passkeys on Android. This icon makes it easier for users to recognize the new sign-in method and helps to increase adoption. For consistency and optimal readability, we recommend using only the filled version of the icon. Refer to the Material icon guidelines for how to use the icon in your products.
|   | Light theme  | Dark theme
| Asset  |
[IMAGE] icon in light theme
|
[IMAGE] icon in dark theme

| Resource  | Passkey icon  |
