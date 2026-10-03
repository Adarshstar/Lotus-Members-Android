# Lotus Members (Android)

Premium **Apple-level** members app for the Lotus Minecraft Bedrock server.

## Design
- Design system from **Google Stitch** (`DESIGN.md`)
- OLED black, system blue `#0A84FF`, SF-style type
- Screens: Login · Home · Chat · Calls (voice/WebRTC next)
- **No system AlertDialog** — custom UI only

## Features
| Feature | Status |
|---------|--------|
| Invite-code login | Done (SHA-256 session) |
| Server status / Join | Done (Lotus-Wake Lambda) |
| Group chat UI | Done (local-first; WebSocket next) |
| Voice call | UI/permissions ready — WebRTC P2P |
| Screen share | Permission ready — MediaProjection + WebRTC |
| Passkey | Placeholder |

## Hosting ($0 target)
- Status: existing free **AWS Lambda**
- Auth/chat: Lambda + API Gateway HTTP/WebSocket free tier
- Voice/media: **peer WebRTC** (no server media cost)

## Build
Open in Android Studio or use the GitHub Actions APK workflow.

### Demo invite codes
`LOTUS2026` · `MEMBERS` · `ADARSH`

## Stitch project
`projects/12096322002252536592` — Lotus Members Premium iOS-grade
