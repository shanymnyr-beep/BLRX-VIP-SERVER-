import { BASE_SPEED, MAX_SPEED } from '../game/constants.js';

export function getSpeed(elapsed, multiplier = 1) {
  const ramp = Math.min(1, elapsed / 80);
  return (BASE_SPEED + (MAX_SPEED - BASE_SPEED) * ramp) * multiplier;
}
