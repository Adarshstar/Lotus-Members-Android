# Lotus Members — Apple-level Design System

Generated with **Google Stitch** (project Lotus Members Premium iOS-grade).

## Design rules (do not violate)
- OLED black `#000000` background — never gray-washed
- Surfaces `#1C1C1E` / `#2C2C2E` only
- System blue `#0A84FF` for primary actions only
- SF Pro / sans-serif; Large Title 34, Body 17
- Continuous corners 12–16dp; button height 50dp
- **No neon, no gradients, no arcade chrome, no default AlertDialog**
- Custom Lotus dialogs only; spring animations 200–400ms
- Calm private club aesthetic (Apple HIG iOS 18)

## Screens (Stitch)
1. Login — invite code
2. Home — status + Join + Chat/Voice/Share
3. Chat — Messages-style
4. Calls — FaceTime-style voice
5. Profile

## Features
- Lightweight invite-code auth (Lambda free tier)
- Group chat (API Gateway WebSocket free tier)
- Voice call (WebRTC P2P — zero media server cost)
- Screen share (MediaProjection + WebRTC)
- Server status from existing Lotus-Wake Lambda

## Hosting cost target
**$0** on AWS free tier (Lambda + HTTP API + WebSocket + DynamoDB free tier)
