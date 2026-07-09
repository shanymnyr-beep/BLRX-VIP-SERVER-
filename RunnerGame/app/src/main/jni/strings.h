#ifndef STRINGS_H
#define STRINGS_H

#include <string>

// Decrypts a game string stored (obfuscated) inside the native library.
std::string decryptString(int id);

enum {
    STR_TITLE = 0,   // BLRX RUNNER
    STR_PLAY,        // PLAY
    STR_SHOP,        // SHOP
    STR_SKILLS,      // SKILLS
    STR_SCORE,       // SCORE
    STR_COINS,       // COINS
    STR_GAMEOVER,    // GAME OVER
    STR_BEST,        // BEST
    STR_RETRY,       // RETRY
    STR_HOME,        // HOME
    STR_TAP_START,   // TAP TO START
    STR_COUNT
};

#endif
