export class InputManager {
  constructor() {
    this.queue = [];
    this.boundKeyDown = this.onKeyDown.bind(this);
  }

  init() {
    window.addEventListener('keydown', this.boundKeyDown);
    document.querySelectorAll('[data-action]').forEach(button => {
      const action = button.dataset.action;
      button.addEventListener('pointerdown', event => {
        event.preventDefault();
        this.queue.push(action);
      });
    });
  }

  onKeyDown(event) {
    const map = {
      ArrowLeft: 'left',
      ArrowRight: 'right',
      ArrowUp: 'jump',
      ArrowDown: 'slide',
      a: 'left',
      d: 'right',
      w: 'jump',
      s: 'slide',
      ' ': 'jump',
    };
    const action = map[event.key];
    if (action) {
      event.preventDefault();
      this.queue.push(action);
    }
  }

  consume() {
    const actions = [...this.queue];
    this.queue.length = 0;
    return actions;
  }
}
