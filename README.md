# BreakFree / تعافي

An Android recovery companion for overcoming compulsive pornography use: a live clean-streak
tracker, daily mood/urge check-ins, CBT-based SOS exercises, an AI counselor, and an anonymous
community — fully bilingual (English / العربية, with RTL).

## Requirements

- Android Studio (or a JDK 17+ toolchain) and Android SDK 36
- A Firebase project (Auth with Google Sign-In, Firestore, Firebase AI Logic, App Check)

## Run locally

1. Open the project in Android Studio and let Gradle sync.
2. **No API key is needed in `.env`.** Firebase AI Logic talks to Gemini through your Firebase
   project using App Check attestation. `.env.example` is intentionally empty.
3. `app/google-services.json` must contain a client whose `package_name` is exactly
   `com.aistudio.breakfree.recovr` (the module's `applicationId`). If it does not, Gradle fails with
   `No matching client found for package name`. Register the app once with:

   ```bash
   firebase apps:create ANDROID "BreakFree Android" \
     --package-name com.aistudio.breakfree.recovr --project <your-project-id>
   firebase apps:android:sha:create <appId> <debug-SHA-1> --project <your-project-id>
   firebase apps:sdkconfig ANDROID <appId> --project <your-project-id> -o app/google-services.json
   ```

   The debug SHA-1 is printed by
   `keytool -list -v -alias androiddebugkey -keystore ~/.android/debug.keystore -storepass android`.
   Add your upload-key SHA-1 the same way before shipping a release build, otherwise Google Sign-In
   fails with `DEVELOPER_ERROR 10`.

   `R.string.firestore_database_id` must name a database that exists in that project — `(default)`
   unless you created a named one. It must also match the `database` target in `firebase.json`,
   which is where the rules are deployed.
4. Deploy the security rules whenever you change `firestore.rules`:

   ```bash
   firebase deploy --only firestore:rules
   ```

5. Run the app on an emulator or a physical device.

From the command line (JDK 17+ required):

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

### Signing

`app/build.gradle.kts` only applies the AI Studio `release` / `debugConfig` signing configs when
the corresponding keystore actually exists. Both `my-upload-key.jks` and `debug.keystore` are
gitignored, so a fresh clone falls back to AGP's auto-generated debug keystore and builds fine.
For AI Studio builds, keep the keystores (or `KEYSTORE_PATH` / `STORE_PASSWORD` / `KEY_PASSWORD`)
available.

## Data model notes

- **Local-first.** Streaks, daily check-ins, and community posts are stored on-device
  (`SharedPreferences`) per account, so they survive restarts and work offline.
- **Cloud is a sync target.** When signed in, check-ins and the streak profile are mirrored to
  Firestore (`users/{uid}` and `users/{uid}/checkins/{yyyy-MM-dd}`) and merged back on sign-in.
- **Merge policy.** A relapse recorded on either side always wins over a longer streak; otherwise
  the longer streak wins (so a new device adopts the account's progress). Counters only move
  forward, and the local profile is only pushed when it is at least as advanced as the cloud copy.
- **Failed writes are retried.** A check-in that could not reach Firestore is flagged as pending
  locally, is never overwritten by a cloud snapshot, and is re-uploaded on the next sign-in.
- **No seeded demo data.** A new install starts at zero; nothing is fabricated.

## Privacy and consent

- **Nothing leaves the device unless you agree.** The first AI chat message opens an in-app
  disclosure; only chat text is sent to Gemini via Firebase AI Logic, and never your browser
  activity, mood logs, or the content monitor's findings.
- **The content monitor is opt-in.** The accessibility service is only opened after an explicit
  disclosure and consent. It matches visible text and the address bar of supported browsers in
  memory on-device; nothing is stored, logged, or uploaded.
- **Social apps are a separate, opt-in scope.** The Shield can also read the visible text of
  YouTube, X, Instagram, TikTok, Facebook and Reddit. Because that is wider than browser
  monitoring, it has its own disclosure: existing users keep browser-only coverage until they
  accept it, and new users grant both scopes with the current disclosure. Images, thumbnails and
  video frames are not analysed — accessibility can only see text (titles, captions, posts and
  descriptions).
- **Browser chrome is not content.** Search suggestions, the new-tab "most visited" tiles, and the
  browser's own internal pages are ignored, so a past search cannot raise a reminder while the
  user is doing nothing wrong. Page text is only matched once a real page is loaded.
- **Typing is not watching.** In an app, if any editable field has focus (a search box, comment box
  or message field) the window is not scanned, and suggestion lists and input fields are skipped.
  A past query or autocomplete row cannot raise a reminder on its own.
- **A timer never resets your streak.** When the 120-second step-back window expires, the app
  asks what happened. Confirming a slip resets the streak; saying you are still clean counts as a
  save.
- **Crisis messages are handled locally.** Self-harm or suicide messages are intercepted before any
  model call and answered with emergency resources in the user's language.
- **DNS lookups go to a third-party resolver, not to BreakFree.** The optional DNS Shield points
  Android's Private DNS at Cloudflare for Families. The resolver — not this app — sees the domain
  names being looked up. BreakFree never sees, stores or uploads them, and no page content is sent.
  That disclosure is shown before the shield can be enabled, separately from the accessibility
  consent, because it is a network data flow rather than an on-device check.
- **Backups are disabled** (`allowBackup="false"`) so recovery data is not copied off-device by the
  OS.

## DNS Shield (Private DNS)

The Shield has two independent layers: the **content monitor** (accessibility, on-device text) and
the **DNS Shield** (system-wide domain blocking). They can be enabled separately — the DNS layer keeps
working even when the content monitor is off.

### How it works

- The app sets Android's **Private DNS** (DNS over TLS) to `family.cloudflare-dns.com`, the
  **Cloudflare for Families** resolver. Cloudflare's family filter refuses adult and malware domains
  before any app can open a connection, so the block applies to browsers, social apps, and anything
  else that resolves a hostname.
- On phones that are **not** managed by BreakFree the shield runs as a **VPN that carries DNS
  queries only**, because a normal app cannot write the system's Private DNS setting. Android shows
  its own VPN indicator for as long as it runs, and no page, video or message traffic passes
  through it.
- It is **not a VPN** in the Device Owner case: no VPN slot is taken, no permanent foreground
  notification is shown, and it costs no measurable battery. In the non-Device-Owner case the VPN
  is the only honest mechanism available.
- Requires **Android 9 (API 28)** for Private DNS, and **Android 10 (API 29) + Device Owner** for
  the app to set the host itself (`DevicePolicyManager.setGlobalPrivateDnsModeSpecifiedHost`). The
  VPN fallback works from Android 7.

### Defaults

- The DNS Shield is **on by default** once its disclosure is accepted; the 90-day uninstall lock is
  the only opt-in piece. The accessibility monitor and the AI companion keep their own, separate
  disclosures.

### Device Owner vs. normal install

| | Device Owner | Normal install |
|---|---|---|
| Applies the resolver | One tap, automatically | On-device DNS filter (VPN), default on after consent |
| Locks Private DNS | Yes, for the 90-day commitment | No — the user can switch it off in system settings |
| Release | Lock is lifted automatically when the commitment ends | Nothing to release |

When BreakFree holds the Device Owner role, the DNS Shield is **locked during the 90-day
commitment**: the `DISALLOW_CONFIG_PRIVATE_DNS` user restriction greys out the Private DNS setting in
Android Settings, exactly like the uninstall lock greys out uninstall. The resolver is applied only
after the platform's DoT connectivity check succeeds, so the phone cannot be left unable to resolve
names. `UninstallLock.releaseIfDue` removes the restriction **before** releasing Device Owner — after
that point the app would no longer have the authority to change it. The resolver itself stays in
place and becomes user-editable again.

The platform call blocks while it performs a connectivity check, so it always runs on
`Dispatchers.IO`, never on the main thread.

### What it cannot do

- It blocks **domains**, not images, videos or page text. A site that is not on the resolver's list,
  or that serves adult content from a general-purpose domain, will still load.
- A **VPN** can bypass it (and a VPN that intercepts DNS prevents the resolver from being reachable at
  all).
- A browser or app with its **own encrypted DNS (DoH)** — for example Chrome's "Secure DNS" — can
  bypass the system resolver.
- It does not see browsing history and cannot report what was blocked.

In short: a strong speed bump for the impulse, not a content filter. The in-app disclosure says this
in both English and Arabic before the shield is enabled.

### Resolver details

- Hostname: `family.cloudflare-dns.com`
- Blocks: adult content, malware, and botnets (Cloudflare's "Families" 1.1.1.1 for Families policy)
- Privacy policy: Cloudflare's 1.1.1.1 resolver policy applies; BreakFree has no access to queries or
  logs

