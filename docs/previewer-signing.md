# Building and signing the previewer app on GitHub

The **Build previewer app** workflow (`.github/workflows/build-previewer.yml`) builds
`AndroidDesignPreview.dmg` on a Mac and `AndroidDesignPreview.msi` on Windows. It runs:

- on every pull request and every push to `main` that changes `app/` or the workflow files,
- when you start it by hand: **Actions** tab > **Build previewer app** > **Run workflow**,
- as the first step of **Release previewer app**, which runs when you push a `v*` tag.

Each run keeps both installers as downloads at the bottom of the run's page, under **Artifacts**.

## Signing is optional

With no secrets set, the builds are unsigned, as before. Add the secrets below to sign them. On
the Mac, signing plus notarization means people can open the app without the quarantine step. On
Windows, a signed installer shows your name instead of "Unknown publisher".

Add secrets in the repository under **Settings** > **Secrets and variables** > **Actions** >
**New repository secret**. Builds for pull requests from forks never see secrets, so they stay
unsigned.

| Secret                         | What it is                                                                        |
|--------------------------------|-----------------------------------------------------------------------------------|
| `MACOS_CERTIFICATE_P12`        | Your **Developer ID Application** certificate and its private key, as base64 text |
| `MACOS_CERTIFICATE_PASSWORD`   | The password you chose when you exported that certificate                         |
| `MACOS_NOTARIZATION_APPLE_ID`  | The email of the Apple developer account                                          |
| `MACOS_NOTARIZATION_PASSWORD`  | An app-specific password for that account (not the account password)              |
| `MACOS_NOTARIZATION_TEAM_ID`   | Your 10-character team ID                                                         |
| `WINDOWS_CERTIFICATE_PFX`      | Your code signing certificate and its private key, as base64 text                 |
| `WINDOWS_CERTIFICATE_PASSWORD` | The password of that `.pfx` file                                                  |

The Mac signs with just the first two. It also notarizes only when all three notarization
secrets are set.

## macOS

You need a paid Apple Developer account.

1. **Certificate.** In Keychain Access on your Mac, open **My Certificates** and find
   "Developer ID Application: *your name* (*team ID*)". If it is not there, create one at
   [developer.apple.com/account/resources/certificates](https://developer.apple.com/account/resources/certificates)
   and open the downloaded file. Right-click the certificate, choose **Export**, pick the
   `.p12` format and set a password. That password is `MACOS_CERTIFICATE_PASSWORD`.
2. **Turn it into text** for `MACOS_CERTIFICATE_P12`. This copies it to the clipboard:

   ```bash
   base64 -i Certificates.p12 | pbcopy
   ```

3. **App-specific password.** Sign in at [account.apple.com](https://account.apple.com), open
   **Sign-In and Security** > **App-Specific Passwords** and create one. That is
   `MACOS_NOTARIZATION_PASSWORD`.
4. **Team ID.** It is shown at
   [developer.apple.com/account](https://developer.apple.com/account) under **Membership
   details**, and in brackets in the certificate's name.

The workflow finds the certificate's name on its own. Apple's App Store Connect API keys are not
supported, because the Compose build only takes an Apple ID.

## Windows

You need a code signing certificate from a certificate authority, as a `.pfx` file with its
private key. If it is in your Windows certificate store, open **certmgr.msc**, right-click it under
**Personal** > **Certificates**, choose **All Tasks** > **Export**, include the private key and set
a password.

Turn the file into text for `WINDOWS_CERTIFICATE_PFX`. In PowerShell, this copies it to the
clipboard:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("$PWD\certificate.pfx")) | Set-Clipboard
```

The workflow signs the `.msi` installer and adds a timestamp, so the signature stays valid after
the certificate expires. Certificates that live only on a hardware key or in a cloud service
cannot be exported as a `.pfx` and are not supported by this setup.

## Check a build

Run the workflow by hand and download the artifacts. Then check the signatures:

```bash
xcrun stapler validate AndroidDesignPreview.dmg
```

```powershell
Get-AuthenticodeSignature AndroidDesignPreview.msi
```
