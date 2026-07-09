import { loadSave } from './storage.js';

export function createState() {
  const save = loadSave();
  return {
    currentScreen: 'menuScreen',
    paused: false,
    run: {
      active: false,
      over: false,
      score: 0,
      distance: 0,
      coins: 0,
      gems: 0,
      lives: 3,
      speed: 0,
      elapsed: 0,
      continued: false,
      effects: {
        magnet: 0,
        shield: 0,
        rocket: 0,
        lightning: 0,
        double: 0,
      },
    },
    save,
  };
}
