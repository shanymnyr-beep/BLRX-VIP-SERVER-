export class Particle {
  constructor(x, y, color, size = 6) {
    this.x = x;
    this.y = y;
    this.vx = (Math.random() - 0.5) * 120;
    this.vy = -Math.random() * 180;
    this.life = 0.7 + Math.random() * 0.4;
    this.maxLife = this.life;
    this.size = size;
    this.color = color;
  }

  update(dt) {
    this.x += this.vx * dt;
    this.y += this.vy * dt;
    this.vy += 320 * dt;
    this.life -= dt;
  }

  get dead() {
    return this.life <= 0;
  }
}
