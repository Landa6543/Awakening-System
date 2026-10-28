# -*- coding: utf-8 -*-
"""
Generador de sprites pixel art HD (32x32) para Uma Musume en Awakening System:
- carat.png: Zanahoria Joya de Tracen (Carat)
- elite_carat.png: Zanahoria Prismática Arcoíris con corona dorada (Elite Carat)
- gacha_ticket.png: Boleto de Invocación Tracen verde menta y dorado
- golden_gacha_ticket.png: Boleto Dorado SSR con ribetes reales y gema rubí
- clover_spark.png: Estatua de la Diosa de Tracen (Goddess Statue / 女神像)
"""
import os
from PIL import Image, ImageDraw

OUTPUT_DIR = r"C:\Users\soybe\Desktop\Mods NeoForge\Sistema\src\main\resources\assets\awakening_system\textures\item"
os.makedirs(OUTPUT_DIR, exist_ok=True)

def create_carat():
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # 1. Hojas superiores (Stems / Leaves)
    stems = [
        # Tallo izquierdo
        (13, 3), (14, 4), (13, 5), (12, 4), (11, 3), (10, 4), (11, 5), (12, 6), (13, 7), (14, 8),
        # Tallo central alto
        (15, 1), (16, 1), (15, 2), (16, 2), (15, 3), (16, 3), (15, 4), (16, 5), (15, 6), (16, 7), (15, 8),
        # Tallo derecho
        (17, 3), (18, 2), (19, 3), (20, 4), (19, 5), (18, 5), (17, 6), (16, 6), (17, 7), (16, 8)
    ]
    for x, y in stems:
        img.putpixel((x, y), (46, 139, 87, 255)) # Verde base

    # Sombras y luces en hojas
    for x, y in [(15, 1), (16, 1), (11, 3), (18, 2), (15, 3), (17, 4)]:
        img.putpixel((x, y), (120, 240, 100, 255)) # Brillo lima
    for x, y in [(13, 7), (16, 7), (17, 8), (14, 8), (15, 8)]:
        img.putpixel((x, y), (20, 80, 40, 255)) # Sombra profunda

    # 2. Broche / Corona de sujeción (Dorado facetado)
    for x in range(12, 20):
        img.putpixel((x, 9), (218, 165, 32, 255))
        img.putpixel((x, 10), (255, 215, 0, 255))
    img.putpixel((11, 9), (139, 90, 0, 255))
    img.putpixel((20, 9), (139, 90, 0, 255))
    img.putpixel((14, 9), (255, 255, 200, 255))
    img.putpixel((15, 9), (255, 255, 200, 255))

    # 3. Cuerpo de Zanahoria Joya (Facetada)
    # Forma piramidal facetada desde Y=11 hasta Y=29
    widths = [
        (11, 9, 22), (12, 9, 22), (13, 10, 21), (14, 10, 21),
        (15, 11, 20), (16, 11, 20), (17, 12, 19), (18, 12, 19),
        (19, 13, 18), (20, 13, 18), (21, 13, 18), (22, 14, 17),
        (23, 14, 17), (24, 14, 17), (25, 14, 17), (26, 15, 16),
        (27, 15, 16), (28, 15, 16), (29, 15, 15)
    ]
    for y, x1, x2 in widths:
        for x in range(x1, x2 + 1):
            # Faceta izquierda (reflejo de luz dorado-ámbar)
            if x < 14:
                col = (255, 210, 80, 255)
            # Faceta central izquierda (brillo naranja cálido)
            elif x < 16:
                col = (255, 165, 30, 255)
            # Faceta central derecha (naranja puro)
            elif x < 18:
                col = (245, 120, 15, 255)
            # Faceta derecha (sombra profunda ámbar)
            else:
                col = (190, 70, 10, 255)
            img.putpixel((x, y), col)

    # 4. Bordes exteriores y biseles oscuros de joya
    for y, x1, x2 in widths:
        img.putpixel((x1, y), (130, 50, 10, 255))
        img.putpixel((x2, y), (110, 40, 5, 255))

    # 5. Facetas internas talladas (Líneas de corte de gema preciosa)
    cut_pixels = [
        (13, 12), (14, 13), (15, 14), (16, 15), (17, 16),
        (18, 17), (15, 18), (14, 19), (13, 20), (14, 21),
        (15, 22), (16, 23), (15, 25)
    ]
    for x, y in cut_pixels:
        if img.getpixel((x, y))[3] > 0:
            img.putpixel((x, y), (255, 230, 140, 255)) # Línea de reflejo brillante

    # Puntos de brillo especular de gema (Sparkle)
    sparkles = [(13, 11), (14, 12), (13, 16), (14, 17), (15, 24)]
    for x, y in sparkles:
        img.putpixel((x, y), (255, 255, 240, 255))

    # Estrella de brillo flotante exterior (Destello gacha)
    star_pixels = [
        (7, 10), (8, 9), (8, 10), (8, 11), (9, 10),
        (24, 21), (25, 20), (25, 21), (25, 22), (26, 21)
    ]
    for x, y in star_pixels:
        img.putpixel((x, y), (255, 250, 190, 255))
    img.putpixel((8, 10), (255, 255, 255, 255))
    img.putpixel((25, 21), (255, 255, 255, 255))

    return img


def create_elite_carat():
    # Elite Carat: Zanahoria joya arcoíris / prismática con corona de platino y oro
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))

    # 1. Tallos celestiales de color aurora boreal (Cian a Esmeralda brillante)
    stems = [
        (13, 3), (14, 4), (13, 5), (12, 4), (11, 3), (10, 4), (11, 5), (12, 6), (13, 7), (14, 8),
        (15, 1), (16, 1), (15, 2), (16, 2), (15, 3), (16, 3), (15, 4), (16, 5), (15, 6), (16, 7), (15, 8),
        (17, 3), (18, 2), (19, 3), (20, 4), (19, 5), (18, 5), (17, 6), (16, 6), (17, 7), (16, 8)
    ]
    for x, y in stems:
        img.putpixel((x, y), (0, 210, 190, 255))
    for x, y in [(15, 1), (16, 1), (11, 3), (18, 2), (15, 3), (17, 4)]:
        img.putpixel((x, y), (160, 255, 240, 255))
    for x, y in [(13, 7), (16, 7), (17, 8), (14, 8), (15, 8)]:
        img.putpixel((x, y), (0, 120, 140, 255))

    # 2. Corona Real Dorada ornamentada en la base de los tallos
    for x in range(11, 21):
        img.putpixel((x, 9), (255, 215, 0, 255))
        img.putpixel((x, 10), (218, 165, 32, 255))
    # Picos de corona
    for x in [11, 13, 15, 17, 19]:
        img.putpixel((x, 8), (255, 240, 120, 255))
    # Joya rubí en el centro de la corona
    img.putpixel((15, 9), (255, 40, 90, 255))
    img.putpixel((16, 9), (255, 40, 90, 255))

    # 3. Cuerpo de Zanahoria Prismática Arcoíris (Transiciones: Púrpura -> Rosa -> Dorado -> Cian)
    widths = [
        (11, 9, 22), (12, 9, 22), (13, 10, 21), (14, 10, 21),
        (15, 11, 20), (16, 11, 20), (17, 12, 19), (18, 12, 19),
        (19, 13, 18), (20, 13, 18), (21, 13, 18), (22, 14, 17),
        (23, 14, 17), (24, 14, 17), (25, 14, 17), (26, 15, 16),
        (27, 15, 16), (28, 15, 16), (29, 15, 15)
    ]
    for y, x1, x2 in widths:
        for x in range(x1, x2 + 1):
            prog_y = (y - 11) / 18.0
            prog_x = (x - x1) / max(1.0, float(x2 - x1))
            # Degradado iridiscente multicolor
            if prog_y < 0.25:
                # Magenta / Rosa brillante a Oro
                r = int(255 - prog_x * 40)
                g = int(120 + prog_x * 90)
                b = int(220 - prog_x * 120)
            elif prog_y < 0.55:
                # Dorado radiante a Naranja ámbar
                r = 255
                g = int(210 - prog_x * 80)
                b = int(80 - prog_x * 40)
            elif prog_y < 0.80:
                # Verde menta a Turquesa celestial
                r = int(100 - prog_x * 60)
                g = int(240 - prog_x * 40)
                b = int(200 + prog_x * 40)
            else:
                # Diamante zafiro / violeta místico
                r = int(150 + prog_x * 50)
                g = int(170 - prog_x * 50)
                b = 255
            img.putpixel((x, y), (r, g, b, 255))

    # Biseles y contorno de cristal
    for y, x1, x2 in widths:
        img.putpixel((x1, y), (255, 255, 255, 230)) # Brillo cristalino izquierdo
        img.putpixel((x2, y), (80, 50, 120, 240))    # Sombra violeta derecha

    # Destellos de diamante y estrellas estelares (Prisma SSR)
    glints = [(13, 13), (14, 14), (15, 17), (14, 21), (15, 25)]
    for x, y in glints:
        img.putpixel((x, y), (255, 255, 255, 255))
        img.putpixel((x + 1, y), (255, 255, 220, 220))
        img.putpixel((x, y + 1), (220, 255, 255, 220))

    # Aura de estrellas exteriores
    for cx, cy in [(6, 12), (25, 14), (7, 24), (24, 25)]:
        img.putpixel((cx, cy), (255, 255, 255, 255))
        img.putpixel((cx - 1, cy), (180, 230, 255, 200))
        img.putpixel((cx + 1, cy), (180, 230, 255, 200))
        img.putpixel((cx, cy - 1), (180, 230, 255, 200))
        img.putpixel((cx, cy + 1), (180, 230, 255, 200))

    return img


def create_gacha_ticket():
    # Boleto de Reclutamiento de Uma Musume (Tracen Recruitment Ticket)
    # Rectangular horizontal/ligeramente diagonal con perforaciones, verde menta, herradura y estrella
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))

    # Marco del boleto: X=3 a 28, Y=8 a 23
    x1, y1, x2, y2 = 3, 8, 28, 23

    # Fondo verde menta Tracen con textura
    for y in range(y1, y2 + 1):
        for x in range(x1, x2 + 1):
            # Degradado sutil menta a verde esmeralda suave
            val = int(160 + (x - x1) * 3)
            img.putpixel((x, y), (40, val, 145, 255))

    # Borde dorado exterior
    for x in range(x1, x2 + 1):
        img.putpixel((x, y1), (255, 215, 0, 255))
        img.putpixel((x, y2), (218, 165, 32, 255))
    for y in range(y1, y2 + 1):
        img.putpixel((x1, y), (255, 215, 0, 255))
        img.putpixel((x2, y), (218, 165, 32, 255))

    # Notches / Muescas de corte de ticket en los laterales
    notch_y = (y1 + y2) // 2
    for ny in [notch_y - 1, notch_y, notch_y + 1]:
        img.putpixel((x1, ny), (0, 0, 0, 0))
        img.putpixel((x1 + 1, ny), (255, 215, 0, 255))
        img.putpixel((x2, ny), (0, 0, 0, 0))
        img.putpixel((x2 - 1, ny), (218, 165, 32, 255))

    # Línea punteada de perforación para el talón del boleto en X=10
    for y in range(y1 + 1, y2):
        if y % 2 == 0:
            img.putpixel((10, y), (255, 255, 255, 180))
        else:
            img.putpixel((10, y), (30, 110, 95, 255))

    # Banda decorativa blanca / crema en el centro superior (Y=11 a 12)
    for x in range(12, 26):
        img.putpixel((x, 11), (250, 250, 240, 255))
        img.putpixel((x, 12), (230, 230, 210, 255))

    # Silueta de Herradura Dorada en el centro (X=16 a 22, Y=14 a 20)
    horseshoe = [
        (17, 14), (18, 14), (20, 14), (21, 14),
        (16, 15), (17, 15), (21, 15), (22, 15),
        (16, 16), (22, 16),
        (16, 17), (22, 17),
        (17, 18), (18, 19), (20, 19), (21, 18),
        (19, 19)
    ]
    for x, y in horseshoe:
        img.putpixel((x, y), (255, 215, 0, 255))
    # Clavos plateados de la herradura
    for x, y in [(16, 16), (22, 16), (17, 18), (21, 18)]:
        img.putpixel((x, y), (255, 255, 240, 255))

    # Estrella dorada en el centro de la herradura
    star_c = (19, 16)
    img.putpixel((19, 16), (255, 255, 255, 255))
    img.putpixel((19, 15), (255, 230, 100, 255))
    img.putpixel((19, 17), (255, 230, 100, 255))
    img.putpixel((18, 16), (255, 230, 100, 255))
    img.putpixel((20, 16), (255, 230, 100, 255))

    # Talón izquierdo (código de barras / líneas)
    for y in [10, 12, 14, 16, 18, 20]:
        img.putpixel((6, y), (255, 255, 220, 220))
        img.putpixel((7, y), (255, 255, 220, 220))

    # Brillo exterior
    img.putpixel((27, 7), (255, 255, 255, 220))
    img.putpixel((26, 8), (255, 255, 200, 200))

    return img


def create_golden_gacha_ticket():
    # Boleto Dorado SSR Tracen (Golden Gacha Ticket)
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))

    x1, y1, x2, y2 = 3, 7, 28, 24

    # Fondo dorado reluciente con franjas satinadas
    for y in range(y1, y2 + 1):
        for x in range(x1, x2 + 1):
            diag = (x + y) % 4
            if diag == 0:
                col = (255, 235, 140, 255) # Reflejo oro brillante
            elif diag == 1:
                col = (255, 215, 60, 255)  # Oro puro
            elif diag == 2:
                col = (235, 185, 20, 255)  # Oro cálido
            else:
                col = (200, 145, 10, 255)  # Oro ámbar sombra
            img.putpixel((x, y), col)

    # Marco doble exterior ornamentado de oro de alta pureza
    for x in range(x1, x2 + 1):
        img.putpixel((x, y1), (255, 250, 200, 255))
        img.putpixel((x, y2), (180, 120, 10, 255))
    for y in range(y1, y2 + 1):
        img.putpixel((x1, y), (255, 250, 200, 255))
        img.putpixel((x2, y), (180, 120, 10, 255))

    # Muescas perforadas en los costados
    notch_y = (y1 + y2) // 2
    for ny in [notch_y - 1, notch_y, notch_y + 1]:
        img.putpixel((x1, ny), (0, 0, 0, 0))
        img.putpixel((x1 + 1, ny), (255, 215, 0, 255))
        img.putpixel((x2, ny), (0, 0, 0, 0))
        img.putpixel((x2 - 1, ny), (180, 120, 10, 255))

    # Línea de perforación de corte
    for y in range(y1 + 1, y2):
        if y % 2 == 0:
            img.putpixel((10, y), (255, 255, 255, 240))
        else:
            img.putpixel((10, y), (160, 100, 10, 255))

    # Cinta heráldica central en carmesí real (Ruby banner)
    for x in range(12, 26):
        for y in [10, 11]:
            img.putpixel((x, y), (180, 20, 50, 255))
        img.putpixel((x, 10), (220, 50, 80, 255))

    # Herradura dorada central con gema incrustada
    horseshoe = [
        (17, 14), (18, 14), (20, 14), (21, 14),
        (16, 15), (17, 15), (21, 15), (22, 15),
        (16, 16), (22, 16),
        (16, 17), (22, 17),
        (16, 18), (22, 18),
        (17, 19), (18, 20), (20, 20), (21, 19),
        (19, 20)
    ]
    for x, y in horseshoe:
        img.putpixel((x, y), (255, 255, 210, 255))

    # Gema Rubí brillante en el corazón de la herradura (SSR)
    img.putpixel((19, 17), (255, 40, 80, 255))
    img.putpixel((19, 16), (255, 200, 220, 255))
    img.putpixel((18, 17), (220, 20, 60, 255))
    img.putpixel((20, 17), (220, 20, 60, 255))
    img.putpixel((19, 18), (170, 10, 40, 255))

    # Estrellas SSR y destellos de oro reluciente
    stars = [(5, 5), (28, 5), (29, 25), (4, 25), (14, 11), (23, 11)]
    for sx, sy in stars:
        img.putpixel((sx, sy), (255, 255, 255, 255))
        if 0 <= sx - 1 < 32: img.putpixel((sx - 1, sy), (255, 240, 150, 200))
        if 0 <= sx + 1 < 32: img.putpixel((sx + 1, sy), (255, 240, 150, 200))
        if 0 <= sy - 1 < 32: img.putpixel((sx, sy - 1), (255, 240, 150, 200))
        if 0 <= sy + 1 < 32: img.putpixel((sx, sy + 1), (255, 240, 150, 200))

    return img


def create_clover_spark():
    # Estatua de la Diosa (女神像 / Goddess Statue de Uma Musume Pretty Derby)
    # Figura sagrada alada dorada sosteniendo un trébol de cuatro hojas esmeralda radiante
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))

    # 1. Pedestal de mármol y oro en la base (Y=26 a 30)
    # Capa inferior amplia
    for x in range(10, 22):
        img.putpixel((x, 30), (160, 110, 30, 255))
        img.putpixel((x, 29), (218, 165, 32, 255))
    # Capa media
    for x in range(12, 20):
        img.putpixel((x, 28), (255, 215, 0, 255))
        img.putpixel((x, 27), (255, 235, 120, 255))
    # Capa superior soporte
    for x in range(13, 19):
        img.putpixel((x, 26), (218, 165, 32, 255))

    # 2. Alas celestiales desplegadas de la Diosa (Plumas doradas curvadas)
    # Ala izquierda
    left_wing = [
        (9, 6), (10, 6), (8, 7), (9, 7), (10, 7),
        (7, 8), (8, 8), (9, 8), (10, 8),
        (6, 9), (7, 9), (8, 9), (9, 9), (10, 9),
        (6, 10), (7, 10), (8, 10), (9, 10), (10, 10),
        (7, 11), (8, 11), (9, 11), (10, 11),
        (8, 12), (9, 12), (10, 12), (11, 12),
        (9, 13), (10, 13), (11, 13),
        (10, 14), (11, 14), (12, 14),
        (11, 15), (12, 15)
    ]
    for x, y in left_wing:
        img.putpixel((x, y), (255, 225, 90, 255))
    # Brillos en borde superior de ala izquierda
    for x, y in [(9, 6), (8, 7), (7, 8), (6, 9), (6, 10)]:
        img.putpixel((x, y), (255, 255, 220, 255))

    # Ala derecha (simétrica)
    right_wing = [
        (22, 6), (21, 6), (23, 7), (22, 7), (21, 7),
        (24, 8), (23, 8), (22, 8), (21, 8),
        (25, 9), (24, 9), (23, 9), (22, 9), (21, 9),
        (25, 10), (24, 10), (23, 10), (22, 10), (21, 10),
        (24, 11), (23, 11), (22, 11), (21, 11),
        (23, 12), (22, 12), (21, 12), (20, 12),
        (22, 13), (21, 13), (20, 13),
        (21, 14), (20, 14), (19, 14),
        (20, 15), (19, 15)
    ]
    for x, y in right_wing:
        img.putpixel((x, y), (240, 195, 40, 255))
    # Brillos en borde superior de ala derecha
    for x, y in [(22, 6), (23, 7), (24, 8), (25, 9), (25, 10)]:
        img.putpixel((x, y), (255, 250, 190, 255))

    # 3. Silueta del cuerpo de la Diosa (Túnica ondulada y busto)
    for y in range(16, 26):
        w = (y - 15) // 2
        for x in range(15 - w, 17 + w):
            shade = 255 - (x - 14) * 25
            img.putpixel((x, y), (min(255, shade), min(255, int(shade * 0.85)), 50, 255))

    # Cabeza y Halo divino
    # Halo circular dorado arriba (Y=2 a 4)
    for x in range(13, 19):
        img.putpixel((x, 2), (255, 255, 180, 255))
    img.putpixel((12, 3), (255, 235, 100, 255))
    img.putpixel((19, 3), (255, 235, 100, 255))
    img.putpixel((12, 4), (255, 215, 60, 255))
    img.putpixel((19, 4), (255, 215, 60, 255))

    # Rostro / Cabeza dorada estilizada
    for x in range(14, 18):
        for y in range(4, 8):
            img.putpixel((x, y), (255, 230, 130, 255))
    img.putpixel((15, 5), (255, 255, 220, 255)) # Brillo frente

    # Cuello y hombros
    for x in range(14, 18):
        img.putpixel((x, 8), (230, 180, 50, 255))
    for x in range(13, 19):
        img.putpixel((x, 9), (240, 190, 60, 255))

    # 4. Trébol de Cuatro Hojas Sagrado (Sostenido entre sus manos en el pecho, Y=10 a 14)
    clover_pixels = [
        # Hoja superior
        (15, 10), (16, 10), (15, 11), (16, 11),
        # Hoja izquierda
        (13, 12), (14, 12), (13, 13), (14, 13),
        # Hoja derecha
        (17, 12), (18, 12), (17, 13), (18, 13),
        # Hoja inferior
        (15, 14), (16, 14), (15, 15), (16, 15),
        # Centro
        (15, 12), (16, 12), (15, 13), (16, 13)
    ]
    for x, y in clover_pixels:
        img.putpixel((x, y), (46, 204, 113, 255)) # Verde esmeralda resplandeciente

    # Brillo central sagrado del trébol
    img.putpixel((15, 12), (200, 255, 220, 255))
    img.putpixel((16, 12), (255, 255, 255, 255))
    img.putpixel((15, 13), (120, 255, 180, 255))
    img.putpixel((16, 13), (120, 255, 180, 255))

    # Destellos de bendición divina flotantes
    sparks = [(4, 4), (27, 4), (5, 17), (26, 17), (16, 1)]
    for sx, sy in sparks:
        img.putpixel((sx, sy), (255, 255, 255, 255))
        if 0 <= sx - 1 < 32: img.putpixel((sx - 1, sy), (100, 255, 180, 180))
        if 0 <= sx + 1 < 32: img.putpixel((sx + 1, sy), (100, 255, 180, 180))
        if 0 <= sy - 1 < 32: img.putpixel((sx, sy - 1), (100, 255, 180, 180))
        if 0 <= sy + 1 < 32: img.putpixel((sx, sy + 1), (100, 255, 180, 180))

    return img


def main():
    items = {
        "carat.png": create_carat(),
        "elite_carat.png": create_elite_carat(),
        "gacha_ticket.png": create_gacha_ticket(),
        "golden_gacha_ticket.png": create_golden_gacha_ticket(),
        "clover_spark.png": create_clover_spark()
    }

    for name, img in items.items():
        out_path = os.path.join(OUTPUT_DIR, name)
        img.save(out_path, format="PNG")
        print(f"Sprite guardado exitosamente: {out_path} ({img.size[0]}x{img.size[1]})")

if __name__ == "__main__":
    main()
