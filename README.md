Absolution
==========

Cool cursed mod that attempts to make the experience of playing Minecraft with an absolute pointing device (such as a graphics tablet, eye tracker, or wiimote) not entirely miserable.

Fabric 1.16.5, not finished atm.

Note that I kinda... accidentally stumbled my way into making an accessibility mod. I made this because i thought using tablet input would be fun and convenient (since i use pen input day-to-day on my computer a lot), but uh, maybe people actually need this, heh. I don't really know the state-of-the-art for pointing devices, especially accessible ones.

This mod makes no assumptions about what pointing device you're using, btw, i don't even know how to do that. It causes Minecraft to not "lock" your mouse to the screen, and works on anything able to move the cursor (including mice)

## To do list

* (important) **Fix the off-axis aiming math.** Right now aiming at places other than the center of the screen is not calculated correctly. Aiming exactly straight ahead is correct but the larger the angle the more wrong it gets
* Right now turn speed is not framerate-independent, that is a deal breaker.
* Make the circle look less obnoxious lol. I have some ideas.
* Click-to-select hotbar (would be so nice)
* Independent pitch/yaw sensitivity? Ellipsoid/square deadzones? Custom response curves? How deep does the rabbit hole go
* Config file & ingame config menu

## license lololol

Lgpl 3 or later, u know the drill by now