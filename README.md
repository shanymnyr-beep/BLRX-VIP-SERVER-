# Urban Runner

A mobile-first HTML5 endless runner game inspired by the provided visual assets.

## Tech
- HTML5 Canvas
- Vanilla JavaScript (ES modules)
- CSS3
- No build step required

## Structure

```text
BLRX-VIP-SERVER-/
├── index.html
├── styles/
│   └── main.css
├── src/
│   ├── main.js
│   ├── game/
│   │   ├── Game.js
│   │   ├── constants.js
│   │   ├── state.js
│   │   ├── input.js
│   │   ├── storage.js
│   │   ├── ui.js
│   │   ├── audio.js
│   │   ├── collision.js
│   │   └── utils.js
│   ├── entities/
│   │   ├── Player.js
│   │   ├── Obstacle.js
│   │   ├── Collectible.js
│   │   ├── PowerUp.js
│   │   └── Particle.js
│   ├── systems/
│   │   ├── Spawner.js
│   │   ├── Renderer.js
│   │   └── DifficultySystem.js
│   └── data/
│       └── content.js
└── assets/
    └── placeholders.md
```

## Run
Just open `index.html` in AIDE Pro preview or a browser.

## Notes
This version is built to be stable first: responsive UI, playable endless runner loop, shop, character selection, upgrades, save data, touch controls, keyboard controls, and polished canvas rendering using the supplied visual direction.
