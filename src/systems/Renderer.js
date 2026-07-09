import { COLORS, WORLD_HEIGHT, WORLD_WIDTH } from '../game/constants.js';

export class Renderer {
  constructor(canvas, ui) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.ui = ui;
    this.roadScroll = 0;
  }

  resize() {
    const dpr = Math.min(window.devicePixelRatio || 1, 2);
    this.canvas.width = WORLD_WIDTH * dpr;
    this.canvas.height = WORLD_HEIGHT * dpr;
    this.ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
  }

  render(state, player, world) {
    const { ctx } = this;
    ctx.clearRect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
    this.drawBackground(ctx, state.run.elapsed);
    this.drawRoad(ctx, state.run.speed);
    this.drawCollectibles(ctx, world.collectibles);
    this.drawPowerUps(ctx, world.powerups);
    this.drawObstacles(ctx, world.obstacles);
    this.drawPlayer(ctx, player, state);
    this.drawParticles(ctx, world.particles);
    this.drawEffects(ctx, state);
  }

  drawBackground(ctx, elapsed) {
    const sky = ctx.createLinearGradient(0, 0, 0, WORLD_HEIGHT);
    sky.addColorStop(0, COLORS.skyTop);
    sky.addColorStop(0.35, COLORS.skyMid);
    sky.addColorStop(1, COLORS.skyBottom);
    ctx.fillStyle = sky;
    ctx.fillRect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);

    for (let i = 0; i < 9; i += 1) {
      const x = (i * 54 + (elapsed * 6)) % (WORLD_WIDTH + 80) - 40;
      const h = 120 + (i % 4) * 56;
      ctx.fillStyle = 'rgba(75, 55, 98, 0.42)';
      ctx.fillRect(x, 170 + (i % 3) * 18, 42, h);
    }

    ctx.fillStyle = 'rgba(255, 239, 210, 0.2)';
    ctx.beginPath();
    ctx.arc(WORLD_WIDTH / 2, 158, 84, 0, Math.PI * 2);
    ctx.fill();
  }

  drawRoad(ctx, speed) {
    this.roadScroll = (this.roadScroll + speed * 0.02) % 120;
    ctx.fillStyle = COLORS.road;
    ctx.beginPath();
    ctx.moveTo(85, WORLD_HEIGHT);
    ctx.lineTo(335, WORLD_HEIGHT);
    ctx.lineTo(265, 230);
    ctx.lineTo(155, 230);
    ctx.closePath();
    ctx.fill();

    ctx.strokeStyle = 'rgba(255,255,255,0.18)';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(155, 230);
    ctx.lineTo(85, WORLD_HEIGHT);
    ctx.moveTo(265, 230);
    ctx.lineTo(335, WORLD_HEIGHT);
    ctx.stroke();

    ctx.strokeStyle = COLORS.line;
    ctx.lineWidth = 7;
    for (let y = 250 - this.roadScroll; y < WORLD_HEIGHT; y += 120) {
      ctx.beginPath();
      ctx.moveTo(WORLD_WIDTH / 2, y);
      ctx.lineTo(WORLD_WIDTH / 2, y + 56);
      ctx.stroke();
    }
  }

  projectLaneX(laneX, y) {
    const perspective = (y - 230) / (WORLD_HEIGHT - 230);
    const left = 155 + (85 - 155) * perspective;
    const right = 265 + (335 - 265) * perspective;
    return left + (right - left) * laneX;
  }

  drawPlayer(ctx, player, state) {
    const bounce = Math.sin(player.anim * 1.8) * 4;
    const y = player.y + bounce;
    const x = this.projectLaneX(player.lane / 2, y);
    const slideScale = player.isSliding ? 0.65 : 1;

    ctx.save();
    ctx.translate(x, y);
    if (state.run.effects.rocket > 0) {
      ctx.fillStyle = 'rgba(255, 170, 60, 0.55)';
      ctx.beginPath();
      ctx.ellipse(0, 26, 26, 58, 0, 0, Math.PI * 2);
      ctx.fill();
    }

    ctx.fillStyle = player.character.color;
    ctx.beginPath();
    ctx.roundRect(-18, -72 * slideScale, 36, 54 * slideScale, 12);
    ctx.fill();

    ctx.fillStyle = '#ffd2b2';
    ctx.beginPath();
    ctx.arc(0, -84 * slideScale, 16, 0, Math.PI * 2);
    ctx.fill();

    ctx.strokeStyle = '#27153d';
    ctx.lineWidth = 6;
    ctx.lineCap = 'round';
    const swing = Math.sin(player.anim * 2.6) * 12;
    ctx.beginPath();
    ctx.moveTo(-8, -20 * slideScale);
    ctx.lineTo(-22, 8 + swing * 0.3);
    ctx.moveTo(8, -20 * slideScale);
    ctx.lineTo(22, 2 - swing * 0.3);
    ctx.moveTo(-6, -8 * slideScale);
    ctx.lineTo(-18, 30 + swing);
    ctx.moveTo(6, -8 * slideScale);
    ctx.lineTo(18, 32 - swing);
    ctx.stroke();

    if (state.run.effects.shield > 0) {
      ctx.strokeStyle = 'rgba(120, 231, 255, 0.8)';
      ctx.lineWidth = 4;
      ctx.beginPath();
      ctx.arc(0, -40, 44, 0, Math.PI * 2);
      ctx.stroke();
    }
    ctx.restore();
  }

  drawObstacles(ctx, obstacles) {
    for (const obstacle of obstacles) {
      const x = this.projectLaneX(obstacle.lane / 2, obstacle.y);
      ctx.save();
      ctx.translate(x, obstacle.y);
      if (obstacle.config.id === 'fire') {
        ctx.fillStyle = '#ff9e3d';
        ctx.beginPath();
        ctx.moveTo(0, -54); ctx.lineTo(-22, 0); ctx.lineTo(0, -18); ctx.lineTo(20, 0); ctx.closePath();
        ctx.fill();
      } else if (obstacle.config.id === 'cone') {
        ctx.fillStyle = '#ff8832';
        ctx.beginPath();
        ctx.moveTo(0, -46); ctx.lineTo(-22, 0); ctx.lineTo(22, 0); ctx.closePath();
        ctx.fill();
      } else if (obstacle.config.id === 'barrier') {
        ctx.fillStyle = '#ff8f5c';
        ctx.fillRect(-28, -44, 56, 38);
        ctx.fillStyle = '#fff5e5';
        ctx.fillRect(-28, -36, 56, 8);
      } else if (obstacle.config.id === 'spike') {
        ctx.fillStyle = '#828a98';
        for (let i = -24; i <= 24; i += 16) {
          ctx.beginPath();
          ctx.moveTo(i, 0); ctx.lineTo(i + 8, -34); ctx.lineTo(i + 16, 0); ctx.closePath();
          ctx.fill();
        }
      } else if (obstacle.config.id === 'barrel') {
        ctx.fillStyle = '#7b522a';
        ctx.beginPath();
        ctx.ellipse(0, -20, 24, 30, 0, 0, Math.PI * 2);
        ctx.fill();
      } else {
        ctx.fillStyle = '#8e6038';
        ctx.fillRect(-26, -44, 52, 44);
      }
      ctx.restore();
    }
  }

  drawCollectibles(ctx, collectibles) {
    for (const item of collectibles) {
      const x = this.projectLaneX(item.lane / 2, item.y);
      ctx.save();
      ctx.translate(x, item.y);
      if (item.type === 'gem') {
        ctx.fillStyle = '#53a0ff';
        ctx.beginPath();
        ctx.moveTo(0, -18); ctx.lineTo(18, -2); ctx.lineTo(8, 18); ctx.lineTo(-8, 18); ctx.lineTo(-18, -2); ctx.closePath();
        ctx.fill();
      } else {
        ctx.fillStyle = COLORS.coin;
        ctx.beginPath();
        ctx.arc(0, 0, 16, 0, Math.PI * 2);
        ctx.fill();
        ctx.fillStyle = 'rgba(255,255,255,0.34)';
        ctx.beginPath();
        ctx.arc(-4, -4, 7, 0, Math.PI * 2);
        ctx.fill();
      }
      ctx.restore();
    }
  }

  drawPowerUps(ctx, powerups) {
    for (const item of powerups) {
      const x = this.projectLaneX(item.lane / 2, item.y);
      ctx.save();
      ctx.translate(x, item.y);
      ctx.fillStyle = item.config.color;
      ctx.beginPath();
      ctx.arc(0, 0, 22, 0, Math.PI * 2);
      ctx.fill();
      ctx.fillStyle = '#fff9ff';
      ctx.font = '900 18px Nunito';
      ctx.textAlign = 'center';
      ctx.fillText(item.config.id === 'double' ? '2X' : item.config.id[0].toUpperCase(), 0, 6);
      ctx.restore();
    }
  }

  drawParticles(ctx, particles) {
    for (const particle of particles) {
      ctx.globalAlpha = Math.max(0, particle.life / particle.maxLife);
      ctx.fillStyle = particle.color;
      ctx.beginPath();
      ctx.arc(particle.x, particle.y, particle.size, 0, Math.PI * 2);
      ctx.fill();
    }
    ctx.globalAlpha = 1;
  }

  drawEffects(ctx, state) {
    const active = Object.entries(state.run.effects).filter(([, value]) => value > 0);
    if (!active.length) return;
    ctx.save();
    ctx.fillStyle = 'rgba(41, 21, 76, 0.35)';
    ctx.roundRect(18, WORLD_HEIGHT - 126, 214, 92, 20);
    ctx.fill();
    ctx.font = '900 16px Nunito';
    ctx.fillStyle = '#fff7fb';
    active.forEach(([name, value], index) => {
      ctx.fillText(`${name.toUpperCase()}: ${value.toFixed(1)}s`, 32, WORLD_HEIGHT - 92 + index * 20);
    });
    ctx.restore();
  }
}
