#include "game.h"
#include "includes.h"

Game g_game;

int Game::rnd(int mod) {
    if (mod <= 0) return 0;
    rngState = rngState * 1103515245u + 12345u;
    return (int)((rngState >> 16) % (unsigned)mod);
}

float Game::coinSize() { return screenW * 0.06f; }

float Game::effH() { return playerState == 2 ? playerH * 0.5f : playerH; }
float Game::effY() { return playerState == 2 ? (groundY - playerH * 0.5f) : playerY; }

void Game::init(int w, int h) {
    screenW  = w;
    screenH  = h;
    groundY  = h * 0.80f;
    rngState = 22446688u;
    bestScore = 0;
    reset();
}

void Game::reset() {
    playerW = screenW * 0.14f;
    playerH = screenH * 0.15f;
    playerX = screenW * 0.18f;
    playerY = groundY - playerH;
    velY = 0.0f;
    playerState = 0;
    slideTimer = 0.0f;
    onGround = true;

    speed = screenW * 0.5f;
    spawnTimer = 1.0f;
    coinSpawnTimer = 0.6f;
    distanceAccum = 0.0f;

    score = 0;
    coinCount = 0;
    lives = 3;
    gameOver = false;
    frameTick = 0;

    for (int i = 0; i < MAX_OBS; ++i)   obs[i].active = false;
    for (int i = 0; i < MAX_COINS; ++i) coins[i].active = false;
}

void Game::spawnObstacle() {
    for (int i = 0; i < MAX_OBS; ++i) {
        if (!obs[i].active) {
            obs[i].active = true;
            obs[i].w = screenW * 0.11f;
            obs[i].h = screenH * 0.12f;
            obs[i].type = rnd(6);
            int flying = rnd(3); // ~1/3 chance: high obstacle you must slide under
            if (flying == 0) {
                obs[i].h = screenH * 0.10f;
                obs[i].y = groundY - playerH * 1.15f - obs[i].h;
            } else {
                obs[i].y = groundY - obs[i].h;
            }
            obs[i].x = screenW + obs[i].w;
            return;
        }
    }
}

void Game::spawnCoin() {
    for (int i = 0; i < MAX_COINS; ++i) {
        if (!coins[i].active) {
            coins[i].active = true;
            coins[i].x = screenW + screenW * 0.10f;
            int hi = rnd(2);
            coins[i].y = (hi == 0) ? (groundY - playerH * 0.6f)
                                   : (groundY - playerH * 1.6f);
            return;
        }
    }
}

void Game::update(float dt, int input) {
    if (gameOver) return;
    frameTick++;

    // Horizontal movement (left / right arrows)
    if (input == 3) playerX -= screenW * 0.6f * dt;
    if (input == 4) playerX += screenW * 0.6f * dt;
    if (playerX < screenW * 0.05f) playerX = screenW * 0.05f;
    if (playerX > screenW * 0.60f) playerX = screenW * 0.60f;

    // Jump (up arrow)
    if (input == 1 && onGround) {
        velY = -1.25f * screenH;
        onGround = false;
        playerState = 1;
    }
    // Slide / duck (down arrow)
    if (input == 2 && onGround && playerState != 2) {
        playerState = 2;
        slideTimer = 0.7f;
    }

    // Vertical physics
    if (!onGround) {
        velY += 2.2f * screenH * dt;
        playerY += velY * dt;
        float floorY = groundY - playerH;
        if (playerY >= floorY) {
            playerY = floorY;
            velY = 0.0f;
            onGround = true;
            playerState = 0;
        }
    }

    // Slide timing
    if (playerState == 2) {
        slideTimer -= dt;
        if (slideTimer <= 0.0f) playerState = 0;
    }

    // Effective (collision) box
    float ph = effH();
    float py = effY();

    // Difficulty ramp
    speed = screenW * 0.5f + score * (screenW * 0.00025f);

    // Obstacles
    spawnTimer -= dt;
    float interval = 1.3f - score * 0.0004f;
    if (interval < 0.65f) interval = 0.65f;
    if (spawnTimer <= 0.0f) { spawnObstacle(); spawnTimer = interval; }

    for (int i = 0; i < MAX_OBS; ++i) {
        if (!obs[i].active) continue;
        obs[i].x -= speed * dt;
        if (obs[i].x + obs[i].w < 0.0f) { obs[i].active = false; continue; }

        float shrink = playerW * 0.15f;
        if (playerX + shrink < obs[i].x + obs[i].w &&
            playerX + playerW - shrink > obs[i].x &&
            py < obs[i].y + obs[i].h &&
            py + ph > obs[i].y) {
            obs[i].active = false;
            lives--;
            if (lives <= 0) {
                gameOver = true;
                if (score > bestScore) bestScore = score;
            }
        }
    }

    // Coins
    coinSpawnTimer -= dt;
    if (coinSpawnTimer <= 0.0f) {
        spawnCoin();
        coinSpawnTimer = 0.5f + (rnd(100) / 100.0f);
    }
    float cs = coinSize();
    for (int i = 0; i < MAX_COINS; ++i) {
        if (!coins[i].active) continue;
        coins[i].x -= speed * dt;
        if (coins[i].x < -cs) { coins[i].active = false; continue; }
        if (playerX < coins[i].x + cs &&
            playerX + playerW > coins[i].x &&
            py < coins[i].y + cs &&
            py + ph > coins[i].y) {
            coins[i].active = false;
            coinCount++;
            score += 10;
        }
    }

    // Distance score
    distanceAccum += speed * dt * 0.02f;
    while (distanceAccum >= 1.0f) { score += 1; distanceAccum -= 1.0f; }
}
