import { WORLD_HEIGHT, WORLD_WIDTH, LANES } from '../game/constants.js';

export class Obstacle {
  constructor(config, lane, y, variant = 'ground') {
    this.config = config;
    this.lane = lane;
    this.x = WORLD_WIDTH * LANES[lane];
    this.y = y;
    this.width = 64;
    this.height = variant === 'low' ? 44 : 64;
    this.variant = variant;
    this.hit = false;
  }

  update(speed, dt) {
    this.y += speed * dt;
  }

  getBounds() {
    return {
      x: this.x - this.width / 2,
      y: this.y - this.height,
      width: this.width,
      height: this.height,
    };
  }

  get offscreen() {
    return this.y - this.height > WORLD_HEIGHT + 80;
  }
}
