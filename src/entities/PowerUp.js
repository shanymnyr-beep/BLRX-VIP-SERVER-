import { WORLD_HEIGHT, WORLD_WIDTH, LANES } from '../game/constants.js';

export class PowerUp {
  constructor(config, lane, y) {
    this.config = config;
    this.lane = lane;
    this.x = WORLD_WIDTH * LANES[lane];
    this.y = y;
    this.radius = 22;
    this.collected = false;
  }

  update(speed, dt) {
    this.y += speed * dt;
  }

  get offscreen() {
    return this.y - this.radius > WORLD_HEIGHT + 80;
  }
}
