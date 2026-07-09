import { WORLD_HEIGHT, WORLD_WIDTH, LANES } from '../game/constants.js';

export class Collectible {
  constructor(type, lane, y, value = 1) {
    this.type = type;
    this.lane = lane;
    this.x = WORLD_WIDTH * LANES[lane];
    this.y = y;
    this.value = value;
    this.radius = type === 'gem' ? 18 : 16;
    this.collected = false;
  }

  update(speed, dt, magnetTarget = null) {
    this.y += speed * dt;
    if (magnetTarget) {
      this.x += (magnetTarget.x - this.x) * Math.min(1, dt * 7);
      this.y += (magnetTarget.y - this.y) * Math.min(1, dt * 4);
    }
  }

  get offscreen() {
    return this.y - this.radius > WORLD_HEIGHT + 80;
  }
}
