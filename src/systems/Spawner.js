import { Collectible } from '../entities/Collectible.js';
import { Obstacle } from '../entities/Obstacle.js';
import { PowerUp } from '../entities/PowerUp.js';
import { OBSTACLES, POWERUPS } from '../data/content.js';
import { pick, randInt } from '../game/utils.js';

export class Spawner {
  constructor() {
    this.reset();
  }

  reset() {
    this.obstacleTimer = 0;
    this.coinTimer = 0;
    this.powerTimer = 0;
  }

  update(dt, speed, target) {
    const created = { obstacles: [], collectibles: [], powerups: [] };
    this.obstacleTimer -= dt;
    this.coinTimer -= dt;
    this.powerTimer -= dt;

    const obstacleDelay = Math.max(0.38, 1.05 - speed / 1100);
    const coinDelay = 0.35;
    const powerDelay = 5.5;

    if (this.obstacleTimer <= 0) {
      const lanes = [0, 1, 2];
      const lane = lanes[randInt(0, 2)];
      created.obstacles.push(new Obstacle(pick(OBSTACLES), lane, -60, Math.random() > 0.75 ? 'low' : 'ground'));
      if (Math.random() > 0.63) {
        const secondLane = lanes.filter(item => item !== lane)[randInt(0, 1)];
        created.obstacles.push(new Obstacle(pick(OBSTACLES), secondLane, -180, 'ground'));
      }
      this.obstacleTimer = obstacleDelay;
    }

    if (this.coinTimer <= 0) {
      const lane = randInt(0, 2);
      const lineLength = randInt(3, 6);
      for (let index = 0; index < lineLength; index += 1) {
        const isGem = Math.random() > 0.94;
        created.collectibles.push(new Collectible(isGem ? 'gem' : 'coin', lane, -80 - index * 70, 1));
      }
      this.coinTimer = coinDelay + Math.random() * 0.2;
    }

    if (this.powerTimer <= 0 && Math.random() > 0.52) {
      created.powerups.push(new PowerUp(pick(POWERUPS), randInt(0, 2), -120));
      this.powerTimer = powerDelay + Math.random() * 2;
    }

    return created;
  }
}
