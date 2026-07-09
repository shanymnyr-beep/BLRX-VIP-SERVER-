export const clamp = (value, min, max) => Math.max(min, Math.min(max, value));
export const lerp = (a, b, t) => a + (b - a) * t;
export const rand = (min, max) => Math.random() * (max - min) + min;
export const randInt = (min, max) => Math.floor(rand(min, max + 1));
export const pick = list => list[Math.floor(Math.random() * list.length)];
export function formatNumber(value) {
  return new Intl.NumberFormat('en-US').format(Math.floor(value));
}
export function circleCollision(a, b, radius = 32) {
  const dx = a.x - b.x;
  const dy = a.y - b.y;
  return dx * dx + dy * dy <= radius * radius;
}
