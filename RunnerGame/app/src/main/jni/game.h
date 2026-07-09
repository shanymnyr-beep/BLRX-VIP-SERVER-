#ifndef GAME_H
#define GAME_H

struct Obstacle { float x, y, w, h; int type; bool active; };
struct Coin     { float x, y; bool active; };

class Game {
public:
    static const int MAX_OBS   = 8;
    static const int MAX_COINS = 12;

    int   screenW, screenH;
    float groundY;

    float playerX, playerY, playerW, playerH;
    float velY;
    int   playerState;   // 0 run, 1 jump, 2 slide
    float slideTimer;
    bool  onGround;

    Obstacle obs[MAX_OBS];
    Coin     coins[MAX_COINS];

    float speed;
    float spawnTimer;
    float coinSpawnTimer;
    float distanceAccum;

    int  score;
    int  coinCount;
    int  lives;
    bool gameOver;
    int  bestScore;

    unsigned int rngState;
    int frameTick;

    void init(int w, int h);
    void reset();
    void update(float dt, int input);
    void spawnObstacle();
    void spawnCoin();
    int  rnd(int mod);
    float coinSize();
    float effH();
    float effY();
};

extern Game g_game;

#endif
