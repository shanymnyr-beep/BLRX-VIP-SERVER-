export class AudioManager {
  constructor() {
    this.enabled = true;
    this.ctx = null;
  }

  setEnabled(value) {
    this.enabled = value;
  }

  beep(frequency = 420, duration = 0.08, type = 'sine', gainValue = 0.02) {
    if (!this.enabled) return;
    const AudioContext = window.AudioContext || window.webkitAudioContext;
    if (!AudioContext) return;
    if (!this.ctx) this.ctx = new AudioContext();
    const osc = this.ctx.createOscillator();
    const gain = this.ctx.createGain();
    osc.type = type;
    osc.frequency.value = frequency;
    gain.gain.value = gainValue;
    osc.connect(gain);
    gain.connect(this.ctx.destination);
    osc.start();
    osc.stop(this.ctx.currentTime + duration);
  }

  coin() { this.beep(860, 0.05, 'triangle', 0.03); }
  jump() { this.beep(520, 0.06, 'sine', 0.025); }
  hit() { this.beep(160, 0.12, 'sawtooth', 0.035); }
  power() { this.beep(720, 0.14, 'square', 0.03); }
  over() { this.beep(140, 0.24, 'sawtooth', 0.04); }
}
