export const CHARACTERS = [
  { id: 'boy', name: 'Street Runner', type: 'free', price: 0, icon: '🏃', speed: 1, jump: 1, color: '#4d8dff' },
  { id: 'girl', name: 'Girl Athlete', type: 'coins', price: 30000, icon: '🏃‍♀️', speed: 1.04, jump: 1.02, color: '#ff70cb' },
  { id: 'ninja', name: 'Shadow Ninja', type: 'coins', price: 45000, icon: '🥷', speed: 1.05, jump: 1.08, color: '#2a314b' },
  { id: 'robo', name: 'Robo-Runner', type: 'coins', price: 55000, icon: '🤖', speed: 1.08, jump: 1.01, color: '#6ee7ff' },
  { id: 'captain', name: 'Sea Captain', type: 'coins', price: 65000, icon: '🏴‍☠️', speed: 1.02, jump: 1.05, color: '#ffcb65' },
  { id: 'star', name: 'Star Navigator', type: 'gems', price: 200, icon: '👨‍🚀', speed: 1.09, jump: 1.08, color: '#d9ecff' },
  { id: 'dragon', name: 'Dragon Knight', type: 'gems', price: 250, icon: '🛡️', speed: 1.1, jump: 1.1, color: '#ff9468' },
];

export const SKILLS = [
  { id: 'magnetTime', name: 'Magnet Time', maxLevel: 5, baseCost: 500, scale: 1.8, description: 'Pull coins from nearby lanes.' },
  { id: 'shieldTime', name: 'Shield Time', maxLevel: 5, baseCost: 750, scale: 1.85, description: 'Take one hit without losing a heart.' },
  { id: 'boostTime', name: 'Rocket Boost', maxLevel: 5, baseCost: 900, scale: 1.9, description: 'Blast forward and ignore obstacles.' },
  { id: 'coinValue', name: 'Coin Bonus', maxLevel: 5, baseCost: 600, scale: 1.75, description: 'Collect more coins per pickup.' },
  { id: 'gemLuck', name: 'Gem Luck', maxLevel: 3, baseCost: 1200, scale: 2.1, description: 'Higher chance to spawn gems.' },
  { id: 'heartStart', name: 'Extra Heart', maxLevel: 2, baseCost: 2000, scale: 2.4, description: 'Start runs with more health.' },
];

export const POWERUPS = [
  { id: 'magnet', label: 'Magnet', color: '#6c63ff', rarity: 0.24 },
  { id: 'shield', label: 'Shield', color: '#78d8ff', rarity: 0.18 },
  { id: 'rocket', label: 'Rocket', color: '#ff7848', rarity: 0.12 },
  { id: 'lightning', label: 'Lightning', color: '#ffd237', rarity: 0.15 },
  { id: 'double', label: '2X', color: '#ff8af4', rarity: 0.13 },
];

export const OBSTACLES = [
  { id: 'spike', label: 'Spikes', color: '#868a94', damage: 1 },
  { id: 'crate', label: 'Crate', color: '#8f5b30', damage: 1 },
  { id: 'barrier', label: 'Barrier', color: '#ff9458', damage: 1 },
  { id: 'barrel', label: 'Barrel', color: '#76502c', damage: 1 },
  { id: 'cone', label: 'Cone', color: '#ff8b2f', damage: 1 },
  { id: 'fire', label: 'Fire', color: '#ff7a2e', damage: 1 },
];
