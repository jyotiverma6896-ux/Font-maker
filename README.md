# Glyph Studio — Fontmaker reconstruction starter

This is an original Kotlin + Jetpack Compose starter inspired by the general concept of a font-making app. It does not copy proprietary source code. Current prototype features:

- Character strip for A–Z, a–z, 0–9 and common symbols
- Finger-drawn glyph canvas with guide lines
- Brush color and width controls
- Per-character stroke state while the app is open
- Undo last stroke and clear current glyph
- Export the current glyph as PNG to Pictures/GlyphStudio

Not implemented yet: persistent project files, real TTF/OTF font compilation, baseline/advance-width editing, import/export of complete font projects, and parity testing against the original app. PNG export is a prototype and needs device testing.

## Build in the cloud from an Android phone

1. Create a GitHub repository and upload this folder's contents.
2. Open the repository's **Actions** tab and enable Actions if prompted.
3. Run **Build Android APK** using **Run workflow**.
4. Download the `glyph-studio-debug-apk` artifact from the completed workflow.
5. Extract the ZIP and install `app-debug.apk`. Android may ask you to allow installs from your browser/files app.

The first build may need dependency downloads. This workflow builds a debug APK for testing, not a Play Store release.
