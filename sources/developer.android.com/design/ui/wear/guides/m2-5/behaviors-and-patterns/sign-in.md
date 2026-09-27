# Sign-in
Source: https://developer.android.com/design/ui/wear/guides/m2-5/behaviors-and-patterns/sign-in

Keep in mind the following best practices when designing sign-in experiences.

## Use Credential Manager

Each of Credential Manager's built-in authentication methods are fully standalone, requiring only the users watch and a data connection for authentication. No other authentication method can offer this benefit.

Use Credential Manager as the base of your authentication solution.

### Primary options: Credential Manager built-ins

Implement passkeys as the foremost option in Credential Manager to provide state-of-the-art security and simplicity to your users.

Implement the complete experience by adding passwords and Sign in With Google as well.

[IMAGE] A users passkey as the preferred authentication solution on Wear OS

**Fig. 1a: **Passkeys
[IMAGE] Passkeys, passwords, and Sign in With Google are all available for a user to authenticate with

**Fig. 1b: **Passkeys, passwords, & Sign in with Google

### Secondary options

Offer at least one secondary option to handle users dismissing Credential Manager. Provide at least two distinct sign-in options in total.

Apps without multiple sign-in methods create a difficult user experience. For example, an app that offers only a "Sign in on phone" option fails if the user's phone is not nearby.

[IMAGE] A user taps dismiss to navigate to backup authentication methods

**Fig. 2a: **Tap 'Dismiss' for backups
[IMAGE] Backup authentication options for Credential Manager

**Fig. 2b: **Backup authentication

**Special Case:** Automatic data layer authentication is the only secondary option which is acceptable to precede Credential Manager in your UX. This comes with several important caveats:
- You must offer at least one other authentication method beyond this because it works only on paired watches with a corresponding mobile app installed.
- You must fully automate the token exchange for the user, and you must not present any UI to the user beforehand. In other words, you should make the authentication process fully automatic from the user's perspective.
- If this method fails, either the user is not logged in on a paired phone, or there is no paired phone to begin with. Don't alert the user there was a failed attempt, instead navigate directly to Credential Manager.

### Prompt users correctly

**For apps requiring sign-in for all functionality:** Immediately present Credential Manager as the first screen to a signed-out user, without any preceding UI.

**For apps offering functionality without sign-in:** Delay presenting Credential Manager until necessary. Provide as many features as possible without requiring sign-in. When authentication becomes essential, display a 'sign in' button to launch Credential Manager. If sign-in fails, offer the option to skip authentication.
[IMAGE] Sign in immediately if needed for full functionality

## Authentication best practices

### Explain sign-in benefits

For apps that delay user sign-in until it becomes essential, clearly explain the benefits of signing in. Use the surrounding UI context to articulate the value to the user; don't assume they understand why signing in is beneficial.

Never refer to Credential Manager by name in your UI.

check_circle

###### Do
Provide context by asking for sign-in after the user initiates an action.

cancel

###### Don't
Display sign-in without explaining why sign-in is beneficial.

### Streamline

Streamline the authentication process by:
- Using Credential Manager
- Reducing the number of steps needed in secondary sign in options
- Keeping users signed in for as long as possible (within your privacy and security requirements)

check_circle

###### Do
Use Credential manager

check_circle

###### Do
Display all secondary options together.

check_circle

###### Do
Stay signed in

### Sign-in status and confirmations

When using secondary, non-Credential Manager options, display a message that alerts the user that they are being signed in the first time the app is opened, then display a confirmation message upon successful sign-in.

check_circle

###### Do
Tell user they're being signed in.

check_circle

###### Do
Show a confirmation.

## Other Resources

To learn how to implement Credential Manager on Wear, see our developer guide and our public sample.
