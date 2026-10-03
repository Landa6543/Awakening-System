# coding: utf-8
import os, sys, math, random
from PIL import Image, ImageDraw

boxes = [
    ('body', 12, 12, 20),
    ('belly_plates', 10, 3, 18),
    ('back_spines', 2, 3, 16),
    ('neck1_cube', 8, 8, 8),
    ('neck1_spines', 1, 2, 6),
    ('neck1_plates', 6, 2, 6),
    ('neck2_cube', 7, 7, 8),
    ('neck2_spines', 1, 2, 6),
    ('neck2_plates', 5, 2, 6),
    ('neck3_cube', 6, 6, 7),
    ('neck3_spines', 1, 2, 5),
    ('neck3_plates', 4, 2, 5),
    ('skull', 7, 6, 7),
    ('snout', 5, 4, 7),
    ('brow_ridges', 6, 2, 5),
    ('upper_fangs', 4, 2, 6),
    ('crest_spikes', 1, 3, 5),
    ('lower_jaw', 5, 3, 8),
    ('lower_fangs', 4, 2, 7),
    ('tongue', 3, 1, 5),
    ('left_horn_base', 2, 3, 8),
    ('left_horn_tip', 2, 2, 7),
    ('right_horn_base', 2, 3, 8),
    ('right_horn_tip', 2, 2, 7),
    ('left_cheek_frill', 4, 3, 3),
    ('right_cheek_frill', 4, 3, 3),
    ('left_wing_arm', 16, 4, 4),
    ('left_wing_forearm', 18, 3, 3),
    ('left_thumb_claw', 2, 4, 2),
    ('left_strut1', 22, 1, 1),
    ('left_strut2', 20, 1, 1),
    ('left_strut3', 18, 1, 1),
    ('left_membrane', 24, 0, 16),
    ('right_wing_arm', 16, 4, 4),
    ('right_wing_forearm', 18, 3, 3),
    ('right_thumb_claw', 2, 4, 2),
    ('right_strut1', 22, 1, 1),
    ('right_strut2', 20, 1, 1),
    ('right_strut3', 18, 1, 1),
    ('right_membrane', 24, 0, 16),
    ('front_left_leg', 4, 7, 5),
    ('front_left_shin', 3, 6, 4),
    ('front_left_foot', 4, 2, 5),
    ('front_right_leg', 4, 7, 5),
    ('front_right_shin', 3, 6, 4),
    ('front_right_foot', 4, 2, 5),
    ('back_left_leg', 5, 8, 7),
    ('back_left_shin', 4, 7, 4),
    ('back_left_foot', 5, 2, 6),
    ('back_right_leg', 5, 8, 7),
    ('back_right_shin', 4, 7, 4),
    ('back_right_foot', 5, 2, 6),
    ('tail1', 7, 7, 9),
    ('tail1_spines', 1, 2, 7),
    ('tail2', 6, 6, 9),
    ('tail2_spines', 1, 2, 7),
    ('tail3', 4, 4, 9),
    ('tail3_spines', 1, 2, 7),
    ('tail4', 3, 3, 9),
    ('tail4_spines', 1, 1, 7),
    ('tail_spade_core', 2, 2, 8),
    ('tail_spade_blade', 10, 1, 7),
    ('tail_spade_fin', 1, 5, 5),
]

W, H = 256, 256
grid = [[False]*W for _ in range(H)]

def can_place(x, y, bw, bh):
    if x + bw > W or y + bh > H: return False
    for r in range(y, y + bh):
        for c in range(x, x + bw):
            if grid[r][c]: return False
    return True

def place(x, y, bw, bh):
    for r in range(y, y + bh):
        for c in range(x, x + bw):
            grid[r][c] = True

items = []
for name, dx, dy, dz in boxes:
    w = 2 * (dz + dx)
    h = dz + dy
    items.append({'name': name, 'w': w, 'h': h, 'dx': dx, 'dy': dy, 'dz': dz})

sorted_items = sorted(items, key=lambda it: (it['h'], it['w']), reverse=True)
placements = {}
for it in sorted_items:
    placed = False
    bw, bh = it['w'], it['h']
    for y in range(H - bh + 1):
        for x in range(W - bw + 1):
            if can_place(x, y, bw, bh):
                place(x, y, bw, bh)
                placements[it['name']] = (x, y, bw, bh)
                placed = True
                break
        if placed: break
    if not placed:
        raise Exception('Failed to place ' + it['name'])

def u(name): return placements[name][0]
def v(name): return placements[name][1]

print('Placements generated cleanly.')
