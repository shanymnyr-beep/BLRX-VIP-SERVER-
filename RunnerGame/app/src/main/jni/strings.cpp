#include "strings.h"

// All player-facing game strings are XOR-obfuscated at COMPILE TIME.
// The characters below are folded to ciphertext by the compiler, so the
// resulting .so does NOT contain readable text. They are only recovered
// (XOR again with the same key) at runtime. This keeps game text out of
// Java where it could be edited easily.

static const unsigned char K = 0x37;

#define X(c) ((char)((unsigned char)(c) ^ K))

static std::string dec(const char* enc, int n) {
    std::string s;
    s.reserve(n);
    for (int i = 0; i < n; ++i) {
        s += (char)((unsigned char)enc[i] ^ K);
    }
    return s;
}

std::string decryptString(int id) {
    switch (id) {
        case STR_TITLE: {
            const char e[] = { X('B'),X('L'),X('R'),X('X'),X(' '),X('R'),X('U'),X('N'),X('N'),X('E'),X('R') };
            return dec(e, sizeof(e));
        }
        case STR_PLAY: {
            const char e[] = { X('P'),X('L'),X('A'),X('Y') };
            return dec(e, sizeof(e));
        }
        case STR_SHOP: {
            const char e[] = { X('S'),X('H'),X('O'),X('P') };
            return dec(e, sizeof(e));
        }
        case STR_SKILLS: {
            const char e[] = { X('S'),X('K'),X('I'),X('L'),X('L'),X('S') };
            return dec(e, sizeof(e));
        }
        case STR_SCORE: {
            const char e[] = { X('S'),X('C'),X('O'),X('R'),X('E') };
            return dec(e, sizeof(e));
        }
        case STR_COINS: {
            const char e[] = { X('C'),X('O'),X('I'),X('N'),X('S') };
            return dec(e, sizeof(e));
        }
        case STR_GAMEOVER: {
            const char e[] = { X('G'),X('A'),X('M'),X('E'),X(' '),X('O'),X('V'),X('E'),X('R') };
            return dec(e, sizeof(e));
        }
        case STR_BEST: {
            const char e[] = { X('B'),X('E'),X('S'),X('T') };
            return dec(e, sizeof(e));
        }
        case STR_RETRY: {
            const char e[] = { X('R'),X('E'),X('T'),X('R'),X('Y') };
            return dec(e, sizeof(e));
        }
        case STR_HOME: {
            const char e[] = { X('H'),X('O'),X('M'),X('E') };
            return dec(e, sizeof(e));
        }
        case STR_TAP_START: {
            const char e[] = { X('T'),X('A'),X('P'),X(' '),X('T'),X('O'),X(' '),X('S'),X('T'),X('A'),X('R'),X('T') };
            return dec(e, sizeof(e));
        }
        default:
            return std::string("");
    }
}
