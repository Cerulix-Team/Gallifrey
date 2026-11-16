"""Generates the TARDIS control panels: textures, models, blockstates, item models, loot tables, and the
Java table of button hit-boxes (TardisPanelLayout.java), so what you see and what you click always match.

    python3 tools/tardis/control_assets.py

Dalek Mod (1.16.5 / 1.20.1) style: each control is a flat, classic-series grey panel you can put on a floor,
wall or ceiling. Some panels hold several buttons, each used by clicking it:

  flight lever ...... big U-handled lever: pull to take off, pull again once you've arrived to land
  coordinates ....... X / Y / Z  - and +, an increment dial, a rotation dial and a find-ground switch
  dimension selector  previous / show / next buttons and a screen
  fast return ....... small lever: sets the destination back to where you last were
  lock panel ........ door button and a key: open/close the doors, lock/unlock them
  handbrake ......... (Gallifrey extra) must be off to take off or land
  refueller ......... (Gallifrey extra) fuel tank and lever: refuel while landed
  randomiser ........ (Gallifrey extra) one big button: pick a random destination nearby
  light alternator .. (Gallifrey extra) knife switch: main power on/off (off = lights out, no flight)

Everything here is designed in "panel view" coordinates, as you see the panel lying on the floor in front
of you: u = left to right (0-16), w = near to far (0-16), h = height. The model's near edge is north, so
u maps to model x = 16 - u and w to model z = w (the blockstate turns it to face whoever placed it).
"""
import json
import os

from PIL import Image

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..')
RES = os.path.join(ROOT, 'src/main/resources')
A = os.path.join(RES, 'assets/gallifrey')
JAVA = os.path.join(ROOT, 'src/main/java/com/cerulixteam/gallifrey/tardis/TardisPanelLayout.java')
NS = 'gallifrey'
TEX = NS + ':block/tardis_panels/'
MAX_BUTTONS = 9  # the "button" blockstate property runs 0 (none pressed) .. MAX_BUTTONS

# ----------------------------------------------------------------------------- colours

GREY = (196, 196, 190)
GREY_LIGHT = (222, 222, 216)
GREY_DARK = (150, 150, 146)
GREY_EDGE = (118, 118, 116)
RED = (214, 34, 30)
RED_DARK = (120, 16, 16)
BLACK = (22, 24, 30)
WHITE = (240, 240, 236)
GOLD = (214, 170, 60)
GOLD_DARK = (150, 110, 30)
AXIS_COLOURS = [(220, 40, 34), (60, 190, 70), (50, 110, 230)]  # X, Y, Z


def rgba(c):
    return tuple(c) + (255,) if len(c) == 3 else tuple(c)


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3])


# ----------------------------------------------------------------------------- texture helpers

class Tex:
    """A 16x16 texture painted in panel-view coordinates (u left->right, w near->far)."""

    def __init__(self, base=GREY, speckle=True):
        self.img = Image.new('RGBA', (16, 16), rgba(base))
        if speckle:
            for x in range(16):
                for y in range(16):
                    n = (x * 7 + y * 13 + x * y) % 17
                    if n == 0:
                        self.img.putpixel((x, y), rgba(shade(base, 0.9)))
                    elif n == 5:
                        self.img.putpixel((x, y), rgba(shade(base, 1.06)))

    def px(self, u, w, c):
        if 0 <= u < 16 and 0 <= w < 16:
            self.img.putpixel((15 - u, w), rgba(c))

    def rect(self, u0, w0, u1, w1, c):
        for u in range(u0, u1):
            for w in range(w0, w1):
                self.px(u, w, c)

    def border(self, c=GREY_EDGE, inner=GREY_LIGHT):
        for i in range(16):
            for (u, w) in ((i, 0), (i, 15), (0, i), (15, i)):
                self.px(u, w, c)
        for i in range(1, 15):
            self.px(i, 1, inner)   # near edge catches the light
            self.px(1, i, inner)

    def glyph(self, u0, w0, rows, c):
        """rows[0] is the top of the glyph as you read it, i.e. the far side of the panel."""
        h = len(rows)
        for r, row in enumerate(rows):
            for k, ch in enumerate(row):
                if ch == '#':
                    self.px(u0 + k, w0 + h - 1 - r, c)

    def save(self, name):
        self.img.save(os.path.join(TEXDIR, name + '.png'))


def flat(c, speckle=True, edge=None):
    t = Tex(c, speckle)
    if edge:
        for i in range(16):
            for (x, y) in ((i, 0), (i, 15), (0, i), (15, i)):
                t.img.putpixel((x, y), rgba(edge))
    return t


def side_tex():
    """Sides of the panels: grey with a darker lower band."""
    t = Tex(GREY_DARK)
    for x in range(16):
        for y in range(16):
            if y >= 14:
                t.img.putpixel((x, y), rgba(GREY_EDGE))
            elif y in (0, 1) or y == 13:
                t.img.putpixel((x, y), rgba(shade(GREY_DARK, 1.1)))
    return t


def lights_tex():
    """A row of little red indicator lights on grey."""
    t = Tex(GREY)
    for x in range(16):
        for y in range(16):
            if y % 4 in (1, 2) and x % 3 == 1:
                t.img.putpixel((x, y), rgba(RED if (x + y) % 2 else RED_DARK))
    return t


def screen_tex():
    """Dimension selector screen: dark glass, stars, and a planet."""
    t = Tex(BLACK, speckle=False)
    for (x, y) in ((2, 3), (12, 2), (5, 12), (13, 11), (9, 5), (3, 8), (14, 6)):
        t.img.putpixel((x, y), rgba(WHITE))
    for x in range(16):
        for y in range(16):
            d = ((x - 8.5) ** 2 + (y - 8) ** 2) ** 0.5
            if d < 3.6:
                c = (70, 120, 220) if (x + y) % 3 else (90, 180, 110)
                if x - y > 2:
                    c = shade(c, 0.6)
                t.img.putpixel((x, y), rgba(c))
            elif 4.6 <= d < 5.2 and abs(y - 8) < 2:
                t.img.putpixel((x, y), rgba((200, 170, 230)))
    for x in range(16):
        t.img.putpixel((x, 0), rgba(GREY_EDGE))
        t.img.putpixel((x, 15), rgba(GREY_EDGE))
        t.img.putpixel((0, x), rgba(GREY_EDGE))
        t.img.putpixel((15, x), rgba(GREY_EDGE))
    return t


def glow_tex(c):
    t = Tex(c, speckle=False)
    for x in range(16):
        for y in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
            t.img.putpixel((x, y), rgba(shade(c, 1.3 if d < 3 else (1.05 if d < 6 else 0.8))))
    return t


def tank_tex():
    t = Tex((50, 36, 20), speckle=False)
    for x in range(16):
        for y in range(16):
            c = (110, 130, 140) if y < 5 else ((255, 150, 40) if (x + y) % 4 else (255, 205, 90))
            if x in (0, 15):
                c = shade(c, 0.6)
            if y in (0, 15):
                c = GREY_EDGE
            t.img.putpixel((x, y), rgba(c))
    return t


# ----------------------------------------------------------------------------- the panels

def box(u0, h0, w0, u1, h1, w1, tex, faces=None, rot=None):
    """An element in panel-view coordinates. rot = (axis, angle, (u, h, w) origin)."""
    e = {'from': [16 - u1, h0, w0], 'to': [16 - u0, h1, w1],
         'faces': {d: {'texture': '#' + tex} for d in (faces or ('north', 'south', 'east', 'west', 'up', 'down'))}}
    if rot and rot[1]:
        axis, angle, (ou, oh, ow) = rot
        # Seen from the panel's near edge, the x axis runs right-to-left, so an x tilt keeps its sign.
        e['rotation'] = {'axis': axis, 'angle': angle, 'origin': [16 - ou, oh, ow]}
    return e


def slab(top):
    """The flat 2-pixel panel every control sits on."""
    return [box(0, 0, 0, 16, 2, 16, 'side', ('north', 'south', 'east', 'west', 'down')),
            box(0, 0, 0, 16, 2, 16, top, ('up',))]


def button(u0, w0, u1, w1, tex, pressed, height=1.0):
    top = 2 + (0.35 if pressed else height)
    return box(u0, 2, w0, u1, top, w1, tex)


def lever_handle(u_mid, w_mid, base_h, height, width, grip, pulled, posts=2):
    """A lever on a pivot at (u_mid, base_h, w_mid). pulled = leaning away from you."""
    r = ('x', 22.5 if pulled else -22.5, (u_mid, base_h, w_mid))
    els = []
    if posts == 2:
        for pu in (u_mid - width / 2, u_mid + width / 2 - 1):
            els.append(box(pu, base_h, w_mid - 0.5, pu + 1, base_h + height, w_mid + 0.5, 'metal', rot=r))
        els.append(box(u_mid - width / 2 - 0.5, base_h + height - 1, w_mid - 1, u_mid + width / 2 + 0.5,
                       base_h + height + 1, w_mid + 1, grip, rot=r))
    else:
        els.append(box(u_mid - 0.5, base_h, w_mid - 0.5, u_mid + 0.5, base_h + height, w_mid + 0.5, 'metal', rot=r))
        els.append(box(u_mid - width / 2, base_h + height - 1, w_mid - 1, u_mid + width / 2, base_h + height + 1,
                       w_mid + 1, grip, rot=r))
    return els


# Each panel: id, the buttons (id, action, (u0, h0, w0, u1, h1, w1) hit-box), and a function
# model(on, pressed_index) -> elements. Index 0 = no button pressed, i = buttons[i - 1] pressed.

def flight_lever_model(on, pressed):
    return ([box(2, 0, 3, 14, 1, 13, 'side'),
             box(3, 1, 5, 13, 4, 11, 'side', ('north', 'south', 'east', 'west', 'down')),
             box(3, 1, 5, 13, 4, 11, 'lights', ('up',))]
            + lever_handle(8, 8, 4, 8, 8, 'red', on))


def handbrake_model(on, pressed):
    return ([box(4, 0, 3, 12, 1, 13, 'side'),
             box(5, 1, 5, 11, 3, 11, 'side', ('north', 'south', 'east', 'west', 'down')),
             box(5, 1, 5, 11, 3, 11, 'base_handbrake', ('up',))]
            + lever_handle(8, 8, 3, 7, 6, 'red', on, posts=1))


def fast_return_model(on, pressed):
    pulled = pressed == 1
    return ([box(4, 0, 1, 12, 1.5, 15, 'side', ('north', 'south', 'east', 'west', 'down')),
             box(4, 0, 1, 12, 1.5, 15, 'base_fast_return', ('up',))]
            + lever_handle(8, 9, 1.5, 5, 6, 'gold', pulled))


def coords_model(on, pressed):
    p = lambda i: pressed == i
    els = slab('base_coordinates')
    rows = [(1, 4), (5, 8), (9, 12)]  # X, Y, Z rows (near to far): red, green, blue
    for k, ((w0, w1), axis) in enumerate(zip(rows, 'xyz')):
        els.append(button(1, w0, 4, w1, axis + '_minus', p(1 + 2 * k)))     # minus (left, darker)
        els.append(button(12, w0, 15, w1, axis + '_plus', p(2 + 2 * k)))    # plus (right, brighter)
    # increment and rotation dials: a cross-shaped knob that turns when used
    for i, (u0, u1) in ((7, (1, 6)), (8, (10, 15))):
        r = ('y', 22.5 if p(i) else 0, ((u0 + u1) / 2, 3, 14))
        els.append(box(u0 + 1, 2, 12.5, u1 - 1, 3.5, 15.5, 'black', rot=r))
        els.append(box(u0, 2, 13.5, u1, 3.5, 14.5, 'black', rot=r))
        els.append(box(u0 + 2, 3.5, 13.5, u1 - 2, 4, 14.5, 'white', rot=r))
    # find-ground switch: a little toggle that flips with the setting
    els.append(box(7, 2, 12.5, 9, 2.5, 15.5, 'metal'))
    els.append(box(7.5, 2.5, 13.5, 8.5, 5, 14.5, 'gold', rot=('x', 22.5 if on else -22.5, (8, 2.5, 14))))
    return els


COORD_BUTTONS = [
    ('x_down', 'X_DOWN', (1, 2, 1, 4, 3, 4)), ('x_up', 'X_UP', (12, 2, 1, 15, 3, 4)),
    ('y_down', 'Y_DOWN', (1, 2, 5, 4, 3, 8)), ('y_up', 'Y_UP', (12, 2, 5, 15, 3, 8)),
    ('z_down', 'Z_DOWN', (1, 2, 9, 4, 3, 12)), ('z_up', 'Z_UP', (12, 2, 9, 15, 3, 12)),
    ('increment', 'INCREMENT', (1, 2, 12.5, 6, 4, 15.5)), ('rotation', 'ROTATION', (10, 2, 12.5, 15, 4, 15.5)),
    ('find_ground', 'FIND_GROUND', (6.5, 2, 12.5, 9.5, 5, 15.5)),
]


def dimension_model(on, pressed):
    p = lambda i: pressed == i
    return (slab('base_dimension')
            + [box(3, 2, 7, 13, 2.5, 15, 'side', ('north', 'south', 'east', 'west')),
               box(3, 2, 7, 13, 2.5, 15, 'screen', ('up',)),
               button(1.5, 1.5, 5, 5, 'red_dark', p(1)),
               button(6.5, 1.5, 9.5, 5, 'white', p(2)),
               button(11, 1.5, 14.5, 5, 'red', p(3))])


DIM_BUTTONS = [('dim_prev', 'DIM_PREV', (1.5, 2, 1.5, 5, 3, 5)),
               ('dim_show', 'DIM_SHOW', (6.5, 2, 1.5, 9.5, 3, 5)),
               ('dim_next', 'DIM_NEXT', (11, 2, 1.5, 14.5, 3, 5))]


def lock_model(on, pressed):
    key_rot = ('y', 45 if on else 0, (4.5, 3, 8))
    return (slab('base_lock')
            + [button(9, 2, 14, 13, 'red', pressed == 1),
               box(2, 2, 4, 7, 2.75, 12, 'gold'),
               box(3.5, 2.75, 7, 5.5, 3, 9, 'black'),
               box(4, 2.75, 7.75, 5, 6, 8.25, 'gold', rot=key_rot),
               box(3.25, 5, 7.75, 5.75, 7, 8.25, 'gold', rot=key_rot)])


LOCK_BUTTONS = [('doors', 'DOORS', (9, 2, 2, 14, 3, 13)), ('lock', 'LOCK', (2, 2, 4, 7, 7, 12))]


def refueler_model(on, pressed):
    return (slab('base_refueler')
            + [box(2, 2, 4, 7, 9, 12, 'tank'),
               box(1.5, 9, 3.5, 7.5, 10, 12.5, 'metal'),
               box(9, 2, 6, 14, 3.5, 10, 'side')]
            + lever_handle(11.5, 8, 3.5, 5, 3, 'gold', on, posts=1))


def randomiser_model(on, pressed):
    down = pressed == 1
    top = 3.2 if down else 4.5
    return (slab('base_randomiser')
            + [box(3.5, 2, 3.5, 12.5, 2.75, 12.5, 'black'),
               box(5, 2, 5, 11, top, 11, 'cyan'),
               box(6, top, 6, 10, top + 0.5, 10, 'cyan')])


def light_alternator_model(on, pressed):
    """Main power: a knife switch (thrown back = on) and a bulb that lights with the power."""
    return (slab('base_light_alternator')
            + [box(3, 2, 2.5, 13, 3, 13.5, 'black'),
               box(4, 3, 10, 6, 5, 12, 'gold'), box(10, 3, 10, 12, 5, 12, 'gold'),      # contact clips
               box(1.5, 2, 1.5, 5, 6.5, 5, 'bulb_on' if on else 'bulb_off'),          # the bulb
               box(1.25, 6.5, 1.25, 5.25, 7.25, 5.25, 'metal')]
            + lever_handle(8, 4.5, 3, 7, 7, 'black', on))


PANELS = [
    # id, is_lever (whole block is one button), buttons, model, has an on/off state
    ('flight_lever', True, [('flight_lever', 'FLIGHT', None)], flight_lever_model, True),
    ('coordinate_panel', False, COORD_BUTTONS, coords_model, True),
    ('dimension_selector', False, DIM_BUTTONS, dimension_model, False),
    ('fast_return', True, [('fast_return', 'FAST_RETURN', None)], fast_return_model, False),
    ('lock_panel', False, LOCK_BUTTONS, lock_model, True),
    ('handbrake', True, [('handbrake', 'HANDBRAKE', None)], handbrake_model, True),
    ('refueler', True, [('refueler', 'REFUEL', None)], refueler_model, True),
    ('randomiser', False, [('randomise', 'RANDOMISE', (3.5, 2, 3.5, 12.5, 4.5, 12.5))], randomiser_model, False),
    ('light_alternator', True, [('light_alternator', 'POWER', None)], light_alternator_model, True),
]


# ----------------------------------------------------------------------------- base textures (panel tops)

def base_tex(kind):
    t = Tex(GREY)
    t.border()
    if kind == 'coordinates':
        # between each row's - and + buttons: a dark strip with lamps in that axis's colour
        for (w0, w1), c in zip([(1, 4), (5, 8), (9, 12)], AXIS_COLOURS):
            t.rect(5, w0, 11, w1, BLACK)
            for u in (6, 8, 10):
                t.px(u - (1 if u == 10 else 0), w0 + 1, c)
            t.px(7, w0 + 1, shade(c, 0.5))
            t.px(8, w0 + 1, c)
    elif kind == 'dimension':
        t.rect(2, 6, 14, 16, GREY_EDGE)
    elif kind == 'lock':
        t.rect(8, 1, 15, 14, GREY_EDGE)
    elif kind == 'randomiser':
        for u in range(16):
            for w in range(16):
                d = ((u - 7.5) ** 2 + (w - 7.5) ** 2) ** 0.5
                if 5.6 <= d < 6.4:
                    t.px(u, w, GREY_EDGE)
    elif kind == 'handbrake':
        t = Tex(GREY)
        t.border()
        t.rect(7, 2, 9, 14, BLACK)          # the slot the lever runs in
    elif kind == 'fast_return':
        t = Tex(GREY)
        t.border()
        for w in range(2, 14):
            t.px(7, w, (235, 190, 30) if (w // 2) % 2 else BLACK)
            t.px(8, w, (235, 190, 30) if (w // 2) % 2 == 0 else BLACK)
    elif kind == 'light_alternator':
        bolt = ["..##", ".##.", "####", ".##.", "##.."]
        t.glyph(11, 2, bolt, GOLD)
    elif kind == 'refueler':
        t.rect(8, 5, 15, 11, GREY_EDGE)
    return t


# ----------------------------------------------------------------------------- blockstate rotations

# (x, y) model rotation for each face/facing, the same as vanilla buttons and levers. Java uses the same
# table (TardisPanelLayout.ROTATIONS) to turn a click back into panel coordinates.
FACINGS = ['north', 'east', 'south', 'west']
ROTATIONS = {
    'floor': {'north': (0, 0), 'east': (0, 90), 'south': (0, 180), 'west': (0, 270)},
    'wall': {'north': (90, 0), 'east': (90, 90), 'south': (90, 180), 'west': (90, 270)},
    'ceiling': {'north': (180, 180), 'east': (180, 270), 'south': (180, 0), 'west': (180, 90)},
}


# ----------------------------------------------------------------------------- the monitor

def monitor_front_tex():
    """Front of the monitor as you look at it: grey casing, dark bezel, the screen, knobs underneath."""
    img = Image.new('RGBA', (16, 16), rgba(GREY))
    put = lambda x, y, c: img.putpixel((x, y), rgba(c))
    for x in range(16):
        for y in range(16):
            if (x * 7 + y * 13 + x * y) % 17 == 0:
                put(x, y, shade(GREY, 0.92))
    for x in range(2, 14):                       # bezel
        for y in range(2, 11):
            put(x, y, GREY_EDGE)
    screen_bg = (8, 30, 24)
    glow = (110, 240, 170)
    dim = (40, 120, 90)
    for x in range(3, 13):                       # screen
        for y in range(3, 10):
            put(x, y, screen_bg)
    for x in range(3, 13):
        for y in range(3, 10):
            d = ((x - 5.5) ** 2 + (y - 6.0) ** 2) ** 0.5
            if 1.6 <= d <= 2.4:
                put(x, y, glow)                   # Gallifreyan circle
    put(5, 6, glow)
    put(7, 4, dim)
    for (x0, x1, y) in ((9, 12, 4), (9, 11, 6), (9, 12, 8)):
        for x in range(x0, x1 + 1):
            put(x, y, dim if (x + y) % 3 == 0 else glow)   # readout lines
    for x in range(2, 14):                       # top highlight / bottom shadow of the bezel
        put(x, 2, shade(GREY_EDGE, 1.2))
        put(x, 10, shade(GREY_EDGE, 0.8))
    put(4, 12, RED)                              # knobs and lights
    put(5, 12, RED_DARK)
    put(7, 12, (230, 190, 40))
    for x in (10, 12):
        put(x, 12, WHITE)
        put(x, 13, GREY_EDGE)
    return img


def casing_tex():
    """Monitor sides and top: grey with cooling vents."""
    t = Tex(GREY)
    for x in range(16):
        for y in range(16):
            if y in (0, 15) or x in (0, 15):
                t.img.putpixel((x, y), rgba(GREY_EDGE))
    for y in (4, 6, 8, 10):
        for x in range(4, 12):
            t.img.putpixel((x, y), rgba(shade(GREY_EDGE, 0.8)))
    return t


def monitor_model():
    front_uv = None
    return [
        box(3, 0, 4, 13, 1, 13, 'side'),                                         # base
        box(6.5, 1, 7.5, 9.5, 3, 10.5, 'metal'),                                  # neck
        box(1.5, 3, 4, 14.5, 14, 12, 'casing', ('south', 'east', 'west', 'up', 'down')),
        box(1.5, 3, 4, 14.5, 14, 12, 'monitor_front', ('north',)),                # casing front
        box(3, 4.5, 12, 13, 12.5, 15, 'casing'),                                  # CRT back
        box(7.5, 14, 7.5, 8.5, 15, 8.5, 'red'),                                   # lamp on top
    ]


def write_monitor():
    monitor_front_tex().save(os.path.join(TEXDIR, 'monitor_front.png'))
    casing_tex().save('casing')
    textures = {'particle': TEX + 'casing', 'side': TEX + 'side', 'metal': TEX + 'metal', 'red': TEX + 'red',
                'casing': TEX + 'casing', 'monitor_front': TEX + 'monitor_front'}
    elements = monitor_model()
    # the front texture is painted as you see it, so map the whole 16x16 onto the casing's front face
    for e in elements:
        if 'north' in e['faces'] and e['faces']['north']['texture'] == '#monitor_front':
            e['faces']['north']['uv'] = [0, 0, 16, 16]
    write(os.path.join(A, 'models/block/tardis_monitor.json'),
          {'parent': 'minecraft:block/block', 'textures': textures, 'elements': elements})
    write(os.path.join(A, 'models/item/tardis_monitor.json'), {'parent': NS + ':block/tardis_monitor'})


def write(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', newline='\r\n') as f:
        json.dump(obj, f, indent=2)
        f.write('\n')


def element_bounds(e):
    """Panel-view bounds of an element (ignoring its own small rotation, padded a little if rotated)."""
    (x0, y0, z0), (x1, y1, z1) = e['from'], e['to']
    pad = 1.5 if 'rotation' in e else 0
    return (16 - x1 - pad, y0, z0 - pad, 16 - x0 + pad, y1 + pad * 0.5, z1 + pad)


def clamp_box(b):
    u0, h0, w0, u1, h1, w1 = b
    return (max(0, u0), max(0, h0), max(0, w0), min(16, u1), min(16, h1), min(16, w1))


def main():
    global TEXDIR
    TEXDIR = os.path.join(A, 'textures/block/tardis_panels')
    os.makedirs(TEXDIR, exist_ok=True)
    for old in os.listdir(TEXDIR):
        os.remove(os.path.join(TEXDIR, old))

    side_tex().save('side')
    lights_tex().save('lights')
    screen_tex().save('screen')
    tank_tex().save('tank')
    flat(GREY_DARK, edge=GREY_EDGE).save('metal')
    flat(RED, edge=RED_DARK).save('red')
    flat(RED_DARK, edge=shade(RED_DARK, 0.7)).save('red_dark')
    flat(BLACK, speckle=False).save('black')
    flat(WHITE, edge=GREY_DARK).save('white')
    flat(GOLD, edge=GOLD_DARK).save('gold')
    glow_tex((80, 220, 230)).save('cyan')
    glow_tex((255, 200, 90)).save('bulb_on')
    flat((80, 70, 50), edge=(50, 44, 32)).save('bulb_off')
    for axis, c in zip('xyz', AXIS_COLOURS):
        flat(shade(c, 0.55), edge=shade(c, 0.35)).save(axis + '_minus')
        flat(c, edge=shade(c, 0.7)).save(axis + '_plus')
    for kind in ('coordinates', 'dimension', 'lock', 'randomiser', 'handbrake', 'fast_return', 'refueler',
                 'light_alternator'):
        base_tex(kind).save('base_' + kind)

    textures = {n: TEX + n for n in ('side', 'lights', 'screen', 'tank', 'metal', 'red', 'red_dark', 'black',
                                     'white', 'gold', 'cyan', 'base_coordinates', 'base_dimension', 'base_lock',
                                     'base_randomiser', 'base_handbrake', 'base_fast_return', 'base_refueler',
                                     'x_minus', 'x_plus', 'y_minus', 'y_plus', 'z_minus', 'z_plus',
                                     'base_light_alternator', 'bulb_on', 'bulb_off')}
    textures['particle'] = TEX + 'side'

    java_panels = []
    for pid, is_lever, buttons, model, has_on in PANELS:
        name = 'tardis_' + pid
        presses = len(buttons) if not is_lever or pid == 'fast_return' else 0
        written = {}
        for on in ((False, True) if has_on else (False,)):
            for pressed in range(presses + 1):
                elements = model(on, pressed)
                key = json.dumps(elements, sort_keys=True)
                if key in written:
                    continue
                suffix = ('_on' if on else '') + ('_p%d' % pressed if pressed else '')
                written[key] = name + suffix
                write(os.path.join(A, 'models/block', name + suffix + '.json'),
                      {'parent': 'minecraft:block/block', 'textures': textures, 'elements': elements})

        def model_for(on, pressed):
            on = on and has_on
            pressed = pressed if pressed <= presses else 0
            return written[json.dumps(model(on, pressed), sort_keys=True)]

        variants = {}
        for face, rots in ROTATIONS.items():
            for facing in FACINGS:
                x, y = rots[facing]
                for on in (False, True):
                    for pressed in range(MAX_BUTTONS + 1):
                        v = {'model': '%s:block/%s' % (NS, model_for(on, pressed))}
                        if x:
                            v['x'] = x
                        if y:
                            v['y'] = y
                        variants['button=%d,face=%s,facing=%s,on=%s' % (pressed, face, facing, str(on).lower())] = v
        write(os.path.join(A, 'blockstates', name + '.json'), {'variants': variants})
        write(os.path.join(A, 'models/item', name + '.json'), {'parent': '%s:block/%s' % (NS, model_for(False, 0))})
        write(os.path.join(RES, 'data/gallifrey/loot_tables/blocks', name + '.json'), {
            'type': 'minecraft:block',
            'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': '%s:%s' % (NS, name)}],
                       'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})

        # outline: the slab or housing plus every raised part, so clicks land on the part you aim at
        shape = [clamp_box(element_bounds(e)) for e in model(False, 0)]
        shape = sorted(set(tuple(round(v, 3) for v in b) for b in shape))
        btns = []
        for bid, action, hb in buttons:
            if hb is None:
                hb = (0, 0, 0, 16, 16, 16)
            btns.append((bid, action, hb))
        java_panels.append((pid, is_lever, btns, shape))

    write_java(java_panels)
    write_monitor()
    print('generated', len(PANELS), 'panels')


def jnum(v):
    return ('%sF' % repr(float(v)))


def write_java(panels):
    lines = []
    lines.append('package com.cerulixteam.gallifrey.tardis;')
    lines.append('')
    lines.append('import java.util.List;')
    lines.append('')
    lines.append('/**')
    lines.append(' * GENERATED by tools/tardis/control_assets.py - do not edit by hand.')
    lines.append(' *')
    lines.append(' * Button hit-boxes and outline boxes for the TARDIS control panels, in panel-view pixels:')
    lines.append(' * u = left to right, h = height, w = near to far, as the panel lies on the floor in front of you.')
    lines.append(' */')
    lines.append('public final class TardisPanelLayout {')
    lines.append('    private TardisPanelLayout() {}')
    lines.append('')
    lines.append('    /** Highest value of the panels\' "button" blockstate property (0 = nothing pressed). */')
    lines.append('    public static final int MAX_BUTTONS = %d;' % MAX_BUTTONS)
    lines.append('')
    lines.append('    public record Button(String id, TardisPanelAction action, float u0, float h0, float w0, float u1, float h1, float w1) {}')
    lines.append('')
    lines.append('    public record Layout(boolean lever, List<Button> buttons, List<float[]> shape) {}')
    lines.append('')
    lines.append('    public static Layout of(TardisPanel panel) {')
    lines.append('        return switch (panel) {')
    for pid, is_lever, btns, shape in panels:
        lines.append('            case %s -> new Layout(%s,' % (pid.upper(), 'true' if is_lever else 'false'))
        lines.append('                    List.of(')
        bl = []
        for bid, action, hb in btns:
            bl.append('                            new Button("%s", TardisPanelAction.%s, %s)' % (
                bid, action, ', '.join(jnum(v) for v in hb)))
        lines.append(',\n'.join(bl) + '),')
        lines.append('                    List.of(')
        sl = []
        for b in shape:
            sl.append('                            new float[]{%s}' % ', '.join(jnum(v) for v in b))
        lines.append(',\n'.join(sl) + '));')
    lines.append('        };')
    lines.append('    }')
    lines.append('}')
    with open(JAVA, 'w', newline='\r\n') as f:
        f.write('\n'.join(lines) + '\n')


if __name__ == '__main__':
    main()
