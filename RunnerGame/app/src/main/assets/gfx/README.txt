Place the game images here (assets/gfx/) using EXACTLY these filenames.
The engine loads them by name. If a file is missing, the game still runs
and draws a colored fallback shape instead (so the build never breaks).

Required by the engine:
  bg.png            -> stage background (side-scroll)
  player_run.png    -> run sprite sheet (8 frames in one horizontal row)
  player_jump.png   -> jump sprite sheet (4 frames)
  player_slide.png  -> slide/duck sprite sheet (4 frames)
  coin.png          -> single coin image
  obstacles.png     -> obstacles sheet (6 obstacles in one horizontal row)
  arrows.png        -> control arrows sheet (4 frames: UP, DOWN, LEFT, RIGHT)

Optional (wired in next step):
  menu_bg.png, gameover.png, shop_bg.png, characters.png, powerups.png, gem.png
