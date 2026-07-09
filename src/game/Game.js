import { AudioManager } from './audio.js';
import { rectsIntersect } from './collision.js';
import { BASE_SPEED } from './constants.js';
import { InputManager } from './input.js';
import { createState } from './state.js';
import { getCharacter, resetProgress as resetStoredProgress, saveProgress } from './storage.js';
import { formatNumber, circleCollision } from './utils.js';
import { Player } from '../entities/Player.js';
import { Particle } from '../entities/Particle.js';
import { Renderer } from '../systems/Renderer.js';
import { Spawner } from '../systems/Spawner.js';
import { getSpeed } from '../systems/DifficultySystem.js';
import { UIManager } from './ui.js';
import { CHARACTERS, SKILLS } from '../data/content.js';

export class Game {
  constructor() {
    this.state = createState();
    this.canvas = document.getElementById('gameCanvas');
    this.ui = new UIManager(this);
    this.audio = new AudioManager();
    this.input = new InputManager();
    this.player = new Player(getCharacter(this.state.save.selectedCharacter));
    this.renderer = new Renderer(this.canvas, this.ui);
    this.spawner = new Spawner();
    this.world = this.createWorld();
    this.lastTime = 0;
    this.loop = this.loop.bind(this);
  }

  init() {
    this.audio.setEnabled(this.state.save.soundEnabled);
    this.ui.bind();
    this.ui.updateTopHUD();
    this.ui.renderCharacters();
    this.ui.renderSkills();
    this.ui.updateRunHUD();
    this.input.init();
    this.renderer.resize();
    window.addEventListener('resize', () => this.renderer.resize());
    requestAnimationFrame(this.loop);
  }

  createWorld() {
    return {
      obstacles: [],
      collectibles: [],
      powerups: [],
      particles: [],
    };
  }

  resetWorld() {
    this.world = this.createWorld();
    this.spawner.reset();
    this.player.setCharacter(getCharacter(this.state.save.selectedCharacter));
    this.player.reset();
  }

  showScreen(screenId) {
    this.state.currentScreen = screenId;
    this.ui.showScreen(screenId);
    if (screenId !== 'gameScreen') {
      this.state.paused = false;
    }
    this.ui.updateTopHUD();
    this.ui.renderCharacters();
    this.ui.renderSkills();
  }

  startRun() {
    this.resetWorld();
    const extraHeart = this.state.save.skillLevels.heartStart || 0;
    this.state.run = {
      active: true,
      over: false,
      score: 0,
      distance: 0,
      coins: 0,
      gems: 0,
      lives: 3 + extraHeart,
      speed: BASE_SPEED,
      elapsed: 0,
      continued: false,
      effects: { magnet: 0, shield: 0, rocket: 0, lightning: 0, double: 0 },
    };
    this.showScreen('gameScreen');
    this.ui.updateRunHUD();
  }

  togglePause() {
    if (!this.state.run.active || this.state.run.over) return;
    this.state.paused = !this.state.paused;
  }

  toggleSound() {
    this.state.save.soundEnabled = !this.state.save.soundEnabled;
    this.audio.setEnabled(this.state.save.soundEnabled);
    this.persist();
    this.ui.updateTopHUD();
  }

  resetProgress() {
    this.state.save = resetStoredProgress();
    this.player.setCharacter(getCharacter(this.state.save.selectedCharacter));
    this.audio.setEnabled(this.state.save.soundEnabled);
    this.ui.updateTopHUD();
    this.ui.renderCharacters();
    this.ui.renderSkills();
  }

  continueRun() {
    if (this.state.run.continued || this.state.save.gems < 50) return;
    this.state.save.gems -= 50;
    this.state.run.continued = true;
    this.state.run.over = false;
    this.state.run.active = true;
    this.state.run.lives = 1;
    this.showScreen('gameScreen');
    this.persist();
    this.ui.updateRunHUD();
    this.ui.updateTopHUD();
  }

  handleCharacterAction(id) {
    const character = CHARACTERS.find(item => item.id === id);
    if (!character) return;
    const owned = this.state.save.ownedCharacters.includes(id) || character.type === 'free';
    if (owned) {
      this.state.save.selectedCharacter = id;
      this.player.setCharacter(character);
      this.persist();
      this.ui.renderCharacters();
      return;
    }

    if (character.type === 'coins' && this.state.save.coins >= character.price) {
      this.state.save.coins -= character.price;
    } else if (character.type === 'gems' && this.state.save.gems >= character.price) {
      this.state.save.gems -= character.price;
    } else {
      return;
    }

    this.state.save.ownedCharacters.push(id);
    this.state.save.selectedCharacter = id;
    this.player.setCharacter(character);
    this.persist();
    this.ui.updateTopHUD();
    this.ui.renderCharacters();
  }

  upgradeSkill(id) {
    const skill = SKILLS.find(item => item.id === id);
    if (!skill) return;
    const level = this.state.save.skillLevels[id] || 0;
    if (level >= skill.maxLevel) return;
    const cost = Math.floor(skill.baseCost * (skill.scale ** level));
    if (this.state.save.coins < cost) return;
    this.state.save.coins -= cost;
    this.state.save.skillLevels[id] = level + 1;
    this.persist();
    this.ui.updateTopHUD();
    this.ui.renderSkills();
  }

  persist() {
    saveProgress(this.state.save);
  }

  handleInput() {
    const actions = this.input.consume();
    for (const action of actions) {
      if (!this.state.run.active || this.state.run.over || this.state.paused) continue;
      if (action === 'left') this.player.moveLeft();
      if (action === 'right') this.player.moveRight();
      if (action === 'jump') { this.player.jump(); this.audio.jump(); }
      if (action === 'slide') this.player.slide();
    }
  }

  loop(timestamp) {
    const dt = Math.min(0.033, (timestamp - this.lastTime) / 1000 || 0);
    this.lastTime = timestamp;
    this.handleInput();

    if (this.state.run.active && !this.state.run.over && !this.state.paused) {
      this.update(dt);
    }

    this.renderer.render(this.state, this.player, this.world);
    requestAnimationFrame(this.loop);
  }

  update(dt) {
    const run = this.state.run;
    run.elapsed += dt;
    run.speed = getSpeed(run.elapsed, this.player.character.speed * (run.effects.lightning > 0 ? 1.18 : 1));
    run.distance += run.speed * dt;
    run.score += dt * 100 * (run.effects.double > 0 ? 2 : 1);

    Object.keys(run.effects).forEach(key => {
      run.effects[key] = Math.max(0, run.effects[key] - dt);
    });

    const spawned = this.spawner.update(dt, run.speed, this.player);
    this.world.obstacles.push(...spawned.obstacles);
    this.world.collectibles.push(...spawned.collectibles);
    this.world.powerups.push(...spawned.powerups);

    this.player.update(dt);

    const playerBounds = this.player.getBounds();
    const magnetRange = run.effects.magnet > 0 ? 82 + (this.state.save.skillLevels.magnetTime || 0) * 8 : 0;

    for (const obstacle of this.world.obstacles) {
      obstacle.update(run.speed, dt);
      if (!obstacle.hit && run.effects.rocket <= 0 && rectsIntersect(playerBounds, obstacle.getBounds())) {
        obstacle.hit = true;
        this.onHit();
      }
    }

    const coinBonus = 1 + (this.state.save.skillLevels.coinValue || 0);
    for (const collectible of this.world.collectibles) {
      const target = run.effects.magnet > 0 && Math.abs(collectible.x - this.player.x) < magnetRange ? this.player : null;
      collectible.update(run.speed, dt, target);
      if (!collectible.collected && circleCollision({ x: this.player.x, y: this.player.y - 40 }, collectible, 34)) {
        collectible.collected = true;
        if (collectible.type === 'coin') {
          run.coins += coinBonus;
          this.audio.coin();
          this.spawnParticles(collectible.x, collectible.y, '#ffd54b', 5);
        } else {
          run.gems += 1;
          this.audio.power();
          this.spawnParticles(collectible.x, collectible.y, '#61b6ff', 6);
        }
      }
    }

    for (const powerup of this.world.powerups) {
      powerup.update(run.speed, dt);
      if (!powerup.collected && circleCollision({ x: this.player.x, y: this.player.y - 40 }, powerup, 42)) {
        powerup.collected = true;
        this.applyPowerUp(powerup.config.id);
      }
    }

    for (const particle of this.world.particles) {
      particle.update(dt);
    }

    this.world.obstacles = this.world.obstacles.filter(item => !item.offscreen && !item.hit);
    this.world.collectibles = this.world.collectibles.filter(item => !item.offscreen && !item.collected);
    this.world.powerups = this.world.powerups.filter(item => !item.offscreen && !item.collected);
    this.world.particles = this.world.particles.filter(item => !item.dead);

    this.ui.updateRunHUD();
  }

  applyPowerUp(id) {
    const levels = this.state.save.skillLevels;
    if (id === 'magnet') this.state.run.effects.magnet = 5 + (levels.magnetTime || 0) * 0.8;
    if (id === 'shield') this.state.run.effects.shield = 4 + (levels.shieldTime || 0) * 1.1;
    if (id === 'rocket') this.state.run.effects.rocket = 3.4 + (levels.boostTime || 0) * 0.7;
    if (id === 'lightning') this.state.run.effects.lightning = 4.5;
    if (id === 'double') this.state.run.effects.double = 5.2;
    this.audio.power();
    this.spawnParticles(this.player.x, this.player.y - 60, '#ffffff', 9);
  }

  spawnParticles(x, y, color, count = 6) {
    for (let index = 0; index < count; index += 1) {
      this.world.particles.push(new Particle(x, y, color, 4 + Math.random() * 3));
    }
  }

  onHit() {
    if (this.state.run.effects.rocket > 0) return;
    if (this.state.run.effects.shield > 0) {
      this.state.run.effects.shield = 0;
      this.audio.power();
      this.spawnParticles(this.player.x, this.player.y - 48, '#78d8ff', 8);
      return;
    }

    this.state.run.lives -= 1;
    this.audio.hit();
    this.spawnParticles(this.player.x, this.player.y - 36, '#ff7f95', 10);
    if (this.state.run.lives <= 0) {
      this.endRun();
    }
  }

  endRun() {
    this.state.run.active = false;
    this.state.run.over = true;
    this.state.save.coins += this.state.run.coins;
    this.state.save.gems += this.state.run.gems;
    this.state.save.bestScore = Math.max(this.state.save.bestScore, Math.floor(this.state.run.score));
    this.persist();
    this.ui.updateTopHUD();
    this.ui.updateGameOver();
    this.audio.over();
    this.showScreen('gameOverScreen');
  }
}
