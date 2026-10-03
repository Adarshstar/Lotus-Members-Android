# Lotus Members (Android) — iOS 27 Liquid Glass

Premium members app for Lotus Minecraft Bedrock, aligned to **Apple iOS 27** design language (September 2026).

## Design (iOS 27)

- **Liquid Glass** chrome only: tinted default, darkened edges, specular rim  
- Content stays **opaque** (`#1C1C1E`) — HIG: never put glass in the content layer  
- OLED black canvas, system blue `#0A84FF`  
- Stitch: project `12096322002252536592` · screen *Lotus iOS 27 Home*  
- Full rules in [`DESIGN.md`](DESIGN.md)

## Features

| Feature | Status |
|---------|--------|
| Invite-code login | Done |
| Server status / Join | Done |
| Group chat UI | Done |
| Voice / screen share | Permissions + WebRTC path |
| iOS 27 glass materials | Drawables `bg_liquid_glass`, `bg_glass_capsule` |

## Demo invite codes

`LOTUS2026` · `MEMBERS` · `ADARSH`

## Build

Android Studio or GitHub Actions APK workflow. `versionName 2.0.0+`
