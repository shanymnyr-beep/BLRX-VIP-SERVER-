import { STORAGE_KEY } from './constants.js';
import { CHARACTERS, SKILLS } from '../data/content.js';

export function createDefaultSave() {
  const skillLevels = Object.fromEntries(SKILLS.map(skill => [skill.id, 0]));
  return {
    coins: 105432,
    gems: 1450,
    bestScore: 105432,
    selectedCharacter: 'captain',
    ownedCharacters: ['boy', 'captain'],
    soundEnabled: true,
    skillLevels,
  };
}

export function loadSave() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return createDefaultSave();
    const parsed = JSON.parse(raw);
    return {
      ...createDefaultSave(),
      ...parsed,
      ownedCharacters: Array.from(new Set(parsed.ownedCharacters || ['boy'])),
    };
  } catch {
    return createDefaultSave();
  }
}

export function saveProgress(data) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
}

export function resetProgress() {
  const fresh = createDefaultSave();
  saveProgress(fresh);
  return fresh;
}

export function getCharacter(id) {
  return CHARACTERS.find(character => character.id === id) || CHARACTERS[0];
}
