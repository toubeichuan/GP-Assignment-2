# Stickman Badminton

---
Group member

| Name | Massey ID |
| ---- | --------- |
| Jianfeng Pang | 23010084  |
| Jinhao Guo | 23009971  |
| Yang Li | 23010055  |
| Jiale Wang | 23010109  |


---
**Stickman Badminton** is a Java-based local multiplayer and AI-powered single-player badminton game built on a lightweight `GameEngine`. Two stick-figure players face off on a flat court, using realistic physics, animations, and sound effects. First to 21 points wins; after a match you can choose to restart, return to the main menu, or quit.

---

## Table of Contents

1. [Features](#features)  
2. [Requirements](#requirements)
3. [Directory Structure](#directory-structure)  
4. [Game Modes](#game-modes)  
5. [Controls](#controls)  
6. [Scoring & Rules](#scoring--rules)  
7. [Assets](#assets)  
8. [Audio & Effects](#audio--effects)
9. [Extensibility](#extensibility)
10. [Resource Attributions](#resource-attributions)
---

## Features

- **Single-player Practice**: Rally against a simple AI that automatically tracks and returns the shuttlecock.  
- **Two-player Local**: Share one keyboard for head-to-head badminton.  
- **Smooth Animations**: Full serve, swing, jump and idle sprites for each stickman.  
- **Realistic Physics**: Gravity, bounce, air drag, edge-of-court wrapping.  
- **Sound & Music**: Continuous background music, serve, smash, clear-point and victory sounds.  
- **Menus & Instruction Screens**: Main menu, instruction screens, end-of-match restart/back options.  
- **Hit-box Visualization**: Debug mode to draw rectangles around rackets and shuttlecock.

---

## Requirements

- **Java Development Kit** (JDK) 8 or higher  
- **JavaSound** support for PCM-encoded WAV files (MP3 via SPI plugin if desired)  

---

> Ensure your working directory contains the `img/` and `Audio/` folders alongside the compiled classes.

---

## Directory Structure

```
project-root/
├─ src/
│  ├ Stickman_Badminton.java       # Main game class & menu logic
│  ├ GameEngine.java               # Simple drawing + input framework
│  ├ Player.java                   # Human player implementation
│  ├ Robot.java                    # AI opponent implementation
│  ├ Birdie.java                   # Shuttlecock physics & rendering
│  └ ...                           # Other support classes
├─ img/
│  ├ background.png
│  ├ menu.png
│  ├ inst1.png
│  ├ inst2.png
│  ├ scoreboard.png
│  ├ left-player/                   # Sprite sheets: standing/, forward/, swing/, serving/
│  ├ right-player/                  # Same structure
│  ├ shadow.png
│  └ ball.png
├─ Audio/
│  ├ audio_background.wav
│  ├ audio_serve.wav
│  ├ audio_smash.wav
│  ├ audio_sudden-turn.wav
│  ├ audio_clear.wav
│  └ audio_winning.wav
└─ README.md
```

---

## Game Modes

1. ** Single-player Mode **
   Train your serve and rally against an AI robot that automatically tracks the shuttle and hits it back.

2. **Two-player Mode (Local Multiplayer)**
   Two players share a keyboard:

    * **Left player** uses A/D to move, W to jump, S to serve/swing.
    * **Right player** uses ←/→ to move, ↑ to jump, ↓ to serve/swing.

3. **Quit**
   Exit the game.

After selecting a mode, a **1.5 s** instruction screen displays controls, then the match begins.

---

## Controls

| Action         | Left Player Keys       | Right Player Keys |
| -------------- | ---------------------- | -------------- |
| Move Forward   | **D**                  | **→**          |
| Move Backward  | **A**                  | **←**          |
| Jump           | **W**                  | **↑**          |
| Serve / Swing  | **S**                  | **↓**          |
| Menu Up/Down   | **↑**/**↓**, **Enter** |(menu only)     |

---

## Scoring & Rules

* **Rally** until one side fails to return (shuttle lands on ground).
* **First to 21 points** wins the match.
* After match end, an **End-screen** appears with options:

    * **Restart**: Play again in the same mode.
    * **Back to Menu**: Return to main menu.
    * **Quit**: Exit.

---

## Assets

* **Sprites**: 150×150 px frames for standing, forward, backward, swing (12 frames), serving (8 frames).
* **Shadow**: Separate PNG drawn beneath each player.
* **Shuttlecock**: 50×50 px, rotates in flight.
* **Scoreboard**: Static image shown behind score text.

---

## Audio & Effects

* **Background Music**: Looped WAV (`audio_background.wav`).
* **Serve**: Short serve sound (`audio_serve.wav`).
* **Smash**: Impact sound on successful hit (`audio_smash.wav`).
* **Turn Change**: Notification when serve changes (`audio_sudden-turn.wav`).
* **Clear Point**: Sound when shuttle hits ground (`audio_clear.wav`).
* **Victory**: Played once when match ends (`audio_winning.wav`).

All audio loaded via `AudioClip` class and JavaSound `Clip` API.

---

## Extensibility

* **Add New Modes**: Extend `GameState`, update menu and `update()` logic.
* **Improve AI**: Enhance `Robot.update()` with predictive movement and smarter swing timing.
* **Network Play**: Swap `Player` input with network messages for online matches.
* **Power-ups**: Introduce special items on court that modify shuttle physics.
* **Custom Skins**: Replace `img/left-player` or `right-player` folders with new artwork.

---


## Resource Attributions

Below is a list of all third-party assets and libraries used in **Stickman Badminton**, along with their original sources and licenses. 
### Art & Sprites

* **Stickman & Racket Sprites**
  Adapted from *Stickman Badminton* by Djinnworks
  (original mobile game sprites and animations)
  [https://play.google.com/store/apps/details?id=com.djinnworks.stickmanbadminton](https://play.google.com/store/apps/details?id=com.djinnworks.stickmanbadminton)
* **Background & UI Images**

    * `background.png`, `menu.png`, `inst1.png`, `inst2.png`, `scoreboard.png`
      Designed in-house by the development team
* **Shadow & Court Elements**
  Custom shapes and PNGs created for this project

### Audio

* **Background Music**
  “Sci-Fi” by Bensound (Creative Commons ― Attribution NoDerivatives 3.0)
  [https://www.bensound.com/royalty-free-music/track/sci-fi](https://www.bensound.com/royalty-free-music/track/sci-fi)
* **Sound Effects**

    * `audio_serve.wav`
    * `audio_smash.wav`
    * `audio_sudden-turn.wav`
    * `audio_clear.wav`
    * `audio_winning.wav`
      Sourced from [Freesound.org](https://freesound.org/) under CC0/public domain or CC-BY licenses (see individual files for attribution).

### Fonts

* **Arial**
  Standard system font; no external license required

---

Enjoy training and competing in **Stickman Badminton**! Pull, serve, and rally your way to victory. 


