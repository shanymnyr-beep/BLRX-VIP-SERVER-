import { CHARACTERS, SKILLS } from '../data/content.js';
import { formatNumber } from './utils.js';

export class UIManager {
  constructor(game) {
    this.game = game;
    this.elements = {
      coinsValue: document.getElementById('coinsValue'),
      gemsValue: document.getElementById('gemsValue'),
      runCoins: document.getElementById('runCoins'),
      runGems: document.getElementById('runGems'),
      livesBar: document.getElementById('livesBar'),
      finalScore: document.getElementById('finalScore'),
      finalCoins: document.getElementById('finalCoins'),
      bestScore: document.getElementById('bestScore'),
      toggleSound: document.getElementById('toggleSound'),
      characterGrid: document.getElementById('characterGrid'),
      skillsList: document.getElementById('skillsList'),
      continueBtn: document.getElementById('continueBtn'),
    };
  }

  bind() {
    document.getElementById('playBtn').addEventListener('click', () => this.game.startRun());
    document.getElementById('retryBtn').addEventListener('click', () => this.game.startRun());
    document.getElementById('pauseBtn').addEventListener('click', () => this.game.togglePause());
    document.getElementById('toggleSound').addEventListener('click', () => this.game.toggleSound());
    document.getElementById('resetProgress').addEventListener('click', () => this.game.resetProgress());
    document.getElementById('continueBtn').addEventListener('click', () => this.game.continueRun());

    document.querySelectorAll('[data-screen]').forEach(button => {
      button.addEventListener('click', () => this.game.showScreen(button.dataset.screen));
    });
  }

  updateTopHUD() {
    const { save } = this.game.state;
    this.elements.coinsValue.textContent = formatNumber(save.coins);
    this.elements.gemsValue.textContent = formatNumber(save.gems);
    this.elements.toggleSound.textContent = `Sound: ${save.soundEnabled ? 'On' : 'Off'}`;
  }

  updateRunHUD() {
    const { run } = this.game.state;
    this.elements.runCoins.textContent = formatNumber(run.coins);
    this.elements.runGems.textContent = formatNumber(run.gems);
    this.elements.livesBar.innerHTML = Array.from({ length: run.lives }, () => '<span class="heart">❤️</span>').join('');
  }

  updateGameOver() {
    const { run, save } = this.game.state;
    this.elements.finalScore.textContent = formatNumber(run.score);
    this.elements.finalCoins.textContent = formatNumber(run.coins);
    this.elements.bestScore.textContent = formatNumber(save.bestScore);
    this.elements.continueBtn.disabled = run.continued || save.gems < 50;
    this.elements.continueBtn.textContent = run.continued ? 'Already Continued' : 'Continue (50 Gems)';
  }

  renderCharacters() {
    const { save } = this.game.state;
    this.elements.characterGrid.innerHTML = CHARACTERS.map(character => {
      const owned = save.ownedCharacters.includes(character.id) || character.type === 'free';
      const selected = save.selectedCharacter === character.id;
      let label = 'Select';
      let className = 'character-action selected';
      if (!owned) {
        label = character.type === 'gems' ? `${character.price} Gems` : `${formatNumber(character.price)} Coins`;
        className = 'character-action';
      } else if (selected) {
        label = 'Selected';
      }
      return `
        <article class="character-card">
          <div class="character-thumb" style="background: linear-gradient(180deg, ${character.color}99, rgba(255,255,255,.18));">${character.icon}</div>
          <div class="character-title">${character.name}</div>
          <div class="character-price">${character.type === 'free' ? 'Starter' : label}</div>
          <button class="${selected ? 'character-action selected' : !owned ? 'character-action' : 'character-action'}" data-character="${character.id}">${selected ? 'Selected' : owned ? 'Use' : 'Buy'}</button>
        </article>`;
    }).join('');

    this.elements.characterGrid.querySelectorAll('[data-character]').forEach(button => {
      button.addEventListener('click', () => this.game.handleCharacterAction(button.dataset.character));
    });
  }

  renderSkills() {
    const { save } = this.game.state;
    this.elements.skillsList.innerHTML = SKILLS.map(skill => {
      const level = save.skillLevels[skill.id] || 0;
      const maxed = level >= skill.maxLevel;
      const cost = Math.floor(skill.baseCost * (skill.scale ** level));
      return `
        <article class="skill-card">
          <div class="character-title">${skill.name}</div>
          <div class="skill-level">Level ${level}/${skill.maxLevel}</div>
          <p>${skill.description}</p>
          <button class="upgrade-btn" data-skill="${skill.id}" ${maxed ? 'disabled' : ''}>${maxed ? 'Maxed' : `Upgrade: ${formatNumber(cost)} Coins`}</button>
        </article>`;
    }).join('');

    this.elements.skillsList.querySelectorAll('[data-skill]').forEach(button => {
      button.addEventListener('click', () => this.game.upgradeSkill(button.dataset.skill));
    });
  }

  showScreen(screenId) {
    document.querySelectorAll('.screen').forEach(screen => screen.classList.remove('active'));
    document.getElementById(screenId)?.classList.add('active');
  }
}
