import { GRAVITY, JUMP_FORCE, LANES, PLAYER_HEIGHT, PLAYER_WIDTH, SLIDE_DURATION, WORLD_HEIGHT, WORLD_WIDTH } from '../game/constants.js';
import { clamp, lerp } from '../game/utils.js';

export class Player {
  constructor(character) {
    this.character = character;
    this.reset();
  }

  setCharacter(character) {
    this.character = character;
  }

  reset() {
    this.lane = 1;
    this.x = WORLD_WIDTH * LANES[this.lane];
    this.y = WORLD_HEIGHT - 190;
    this.baseY = this.y;
    this.width = PLAYER_WIDTH;
    this.height = PLAYER_HEIGHT;
    this.velocityY = 0;
    this.isJumping = false;
    this.isSliding = false;
    this.slideTimer = 0;
    this.anim = 0;
  }

  moveLeft() { this.lane = clamp(this.lane - 1, 0, 2); }
  moveRight() { this.lane = clamp(this.lane + 1, 0, 2); }

  jump() {
    if (this.isJumping) return;
    this.isJumping = true;
    this.velocityY = -(JUMP_FORCE * this.character.jump);
  }

  slide() {
    if (this.isJumping || this.isSliding) return;
    this.isSliding = true;
    this.slideTimer = SLIDE_DURATION;
  }

  update(dt) {
    const targetX = WORLD_WIDTH * LANES[this.lane];
    this.x = lerp(this.x, targetX, Math.min(1, dt * 12));

    if (this.isJumping) {
      this.velocityY += GRAVITY * dt;
      this.y += this.velocityY * dt;
      if (this.y >= this.baseY) {
        this.y = this.baseY;
        this.velocityY = 0;
        this.isJumping = false;
      }
    }

    if (this.isSliding) {
      this.slideTimer -= dt;
      if (this.slideTimer <= 0) {
        this.isSliding = false;
      }
    }

    this.anim += dt * 10;
  }

  getBounds() {
    const slideHeight = this.isSliding ? this.height * 0.55 : this.height;
    return {
      x: this.x - this.width / 2,
      y: this.y - slideHeight,
      width: this.width,
      height: slideHeight,
    };
  }
}
