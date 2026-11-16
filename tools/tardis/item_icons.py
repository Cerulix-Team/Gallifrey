"""Draws the TARDIS exterior and interior door item icons (32x32) and the Stattenheim remote (16x16).

    python3 tools/tardis/item_icons.py
"""
import math
import os

from PIL import Image

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..')
OUT = os.path.join(ROOT, 'src/main/resources/assets/gallifrey/textures/item')

NAVY = (22, 40, 104)
NAVY_DARK = (10, 20, 62)
NAVY_LIGHT = (44, 70, 150)
OUTLINE = (6, 10, 34)
BLACK = (16, 16, 20)
WHITE = (236, 238, 230)
GLASS = (226, 234, 214)
LAMP = (255, 246, 200)


def canvas():
    return Image.new('RGBA', (32, 32), (0, 0, 0, 0))


def rect(img, x0, y0, x1, y1, c):
    for x in range(x0, x1 + 1):
        for y in range(y0, y1 + 1):
            img.putpixel((x, y), c + (255,))


def outline(img):
    """A dark 1px outline around everything drawn, so the icon reads on any background."""
    src = img.copy()
    for x in range(32):
        for y in range(32):
            if src.getpixel((x, y))[3]:
                continue
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = x + dx, y + dy
                if 0 <= nx < 32 and 0 <= ny < 32 and src.getpixel((nx, ny))[3]:
                    img.putpixel((x, y), OUTLINE + (255,))
                    break


def window(img, x, y):
    """A 4x4 police box window: six panes."""
    rect(img, x, y, x + 3, y + 3, GLASS)
    for yy in (y + 1, y + 3):
        for xx in range(x, x + 4):
            img.putpixel((xx, yy), NAVY_DARK + (255,))
    for yy in range(y, y + 4):
        img.putpixel((x + 2, yy), NAVY_DARK + (255,))


def exterior():
    img = canvas()
    rect(img, 15, 1, 16, 3, LAMP)              # lamp
    rect(img, 14, 1, 17, 1, NAVY_LIGHT)        # lamp cap
    rect(img, 13, 4, 18, 5, NAVY)              # roof stack
    rect(img, 10, 6, 21, 6, NAVY_LIGHT)
    rect(img, 8, 7, 23, 9, BLACK)              # POLICE BOX sign
    for x in range(10, 22, 2):
        img.putpixel((x, 8), WHITE + (255,))
    rect(img, 8, 10, 23, 29, NAVY)             # body
    rect(img, 8, 10, 9, 29, NAVY_LIGHT)        # corner posts
    rect(img, 22, 10, 23, 29, NAVY_DARK)
    rect(img, 15, 10, 16, 29, NAVY_DARK)       # door split
    window(img, 10, 11)
    window(img, 18, 11)
    for x0 in (10, 18):                        # recessed panels
        for y0 in (17, 23):
            rect(img, x0, y0, x0 + 3, y0 + 4, NAVY_DARK)
            rect(img, x0 + 1, y0 + 1, x0 + 2, y0 + 3, NAVY)
    rect(img, 11, 17, 12, 19, WHITE)           # notice on the left door
    img.putpixel((17, 21), (200, 160, 60, 255))  # handle
    rect(img, 7, 30, 24, 30, NAVY_DARK)        # plinth
    outline(img)
    return img


def interior_door():
    img = canvas()
    # roundel wall behind
    rect(img, 2, 2, 29, 29, (206, 204, 194))
    for cx, cy in ((5.5, 8), (25.5, 8), (5.5, 23), (25.5, 23)):
        for x in range(32):
            for y in range(32):
                d = math.hypot(x - cx, y - cy)
                if d <= 3.3 and 2 <= x <= 29 and 2 <= y <= 29:
                    img.putpixel((x, y), (168, 164, 150, 255) if d > 1.9 else (250, 238, 200, 255))
    # door frame
    rect(img, 9, 4, 22, 29, NAVY_DARK)
    # left door, closed: the back of the door (plain panels, windows lit from outside)
    rect(img, 10, 6, 15, 29, NAVY)
    rect(img, 11, 7, 14, 10, (200, 220, 236))
    for x in range(11, 15):
        img.putpixel((x, 9), NAVY_DARK + (255,))
    img.putpixel((13, 8), NAVY_DARK + (255,))
    img.putpixel((13, 10), NAVY_DARK + (255,))
    for y0 in (13, 20):
        rect(img, 11, y0, 14, y0 + 5, NAVY_DARK)
        rect(img, 12, y0 + 1, 13, y0 + 4, NAVY)
    # right door, ajar: light spilling in from outside
    rect(img, 16, 6, 21, 29, (255, 250, 222))
    rect(img, 17, 6, 21, 29, (232, 240, 255))
    rect(img, 19, 6, 21, 29, NAVY_LIGHT)       # the door edge, swung in
    rect(img, 20, 6, 21, 29, NAVY)
    rect(img, 9, 4, 22, 5, NAVY_LIGHT)         # top of the frame
    img.putpixel((15, 18), (200, 160, 60, 255))  # handle
    outline(img)
    return img


def stattenheim_remote():
    """16x16: a grey handset with a red summon button, a little screen and an antenna."""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    def r(x0, y0, x1, y1, c):
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                img.putpixel((x, y), c + (255,))
    r(10, 1, 10, 1, (255, 120, 90))          # antenna tip
    r(10, 2, 10, 4, (90, 90, 96))            # antenna
    r(4, 4, 11, 14, (150, 152, 156))         # body
    r(4, 4, 11, 4, (196, 198, 200))          # top edge highlight
    r(11, 5, 11, 14, (104, 106, 112))        # right side shadow
    r(5, 6, 9, 8, (20, 40, 34))              # screen
    r(6, 7, 7, 7, (110, 240, 170))
    r(6, 10, 8, 12, (150, 24, 20))           # summon button
    r(6, 10, 7, 11, (226, 52, 40))
    img.putpixel((10, 10), (60, 60, 66, 255))  # small buttons
    img.putpixel((10, 12), (60, 60, 66, 255))
    # outline
    src = img.copy()
    for x in range(16):
        for y in range(16):
            if src.getpixel((x, y))[3]:
                continue
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = x + dx, y + dy
                if 0 <= nx < 16 and 0 <= ny < 16 and src.getpixel((nx, ny))[3]:
                    img.putpixel((x, y), (30, 30, 36, 255))
                    break
    return img


def main():
    exterior().save(os.path.join(OUT, 'tardis_exterior.png'))
    interior_door().save(os.path.join(OUT, 'tardis_interior_door.png'))
    stattenheim_remote().save(os.path.join(OUT, 'stattenheim_remote.png'))
    print('saved')


if __name__ == '__main__':
    main()
