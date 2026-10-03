# Lotus Members — iOS 27 Liquid Glass Design System

Updated for **Apple iOS 27** (shipped September 2026). Generated with Google Stitch screen `Lotus iOS 27 Home`.

## What changed in iOS 27 (vs iOS 26 / older)

Apple refined **Liquid Glass** at WWDC 2026:

| iOS 27 change | How Lotus applies it |
|---------------|----------------------|
| Stronger diffusion behind glass | Glass chrome stays legible on busy content |
| **Darkened edge** on glass | 1dp dark stroke `#000000` @ 40–55% opacity around glass |
| Brighter specular rim | Soft top highlight `#FFFFFF` @ 12–18% |
| Transparency slider (clear → tinted) | Default = **moderately tinted** for OLED readability |
| No gyro motion shimmer | Static highlights only — no tilt sparkle |
| Glass for chrome only | Tab bar, status capsule, floating CTA — **not** content cards |
| Content layer = solid surfaces | Cards `#1C1C1E`, lists opaque |

## Color tokens (OLED dark)

```
background          #000000
surface             #1C1C1E   (content cards)
surface-2           #2C2C2E
separator           #38383A
label               #FFFFFF
secondary-label     #8E8E93
tertiary-label      #636366
primary (system blue) #0A84FF
success             #30D158
danger              #FF453A
glass-fill          #2C2C2E @ 72%   (tinted Liquid Glass default)
glass-edge          #000000 @ 50%
glass-specular      #FFFFFF @ 14%
```

## Materials hierarchy (HIG)

1. **Canvas** — pure black  
2. **Content** — opaque surface cards (never glass)  
3. **Chrome** — Liquid Glass (tab bar, capsules, FABs) with darkened edge + specular  
4. **Primary action** — solid system blue continuous button  

## Type

- Large Title 34 bold  
- Title 28 bold  
- Headline 17 semibold  
- Body 17 regular  
- Footnote 13  

## Motion (iOS 27 craft)

- Spring damping ~0.82, response ~0.35s  
- Fade 200–300ms  
- Nav bar may minimize on scroll (content first)  
- **No** excessive bounce or neon pulses  

## Hard rules

- No neon, no arcade chrome, no system AlertDialog  
- No glass inside content lists  
- Prefer legibility over maximum transparency  
- Match Stitch project: `projects/12096322002252536592`

## Screens

1. Login — invite code  
2. Home — iOS 27 Liquid Glass  
3. Chat — Messages-style  
4. Calls — FaceTime-style voice  
5. Profile  

## Hosting

$0 AWS free tier (Lambda + WebSocket + WebRTC P2P for voice/share)
