"""
Remasterizador HD de Sonidos del Sistema v2 (Solo Leveling & Anime RPG).
Genera efectos de sonido cristalinos, elegantes, cinemáticos y suaves a 44.1 kHz Vorbis OGG.
Volumen calibrado a estándares profesionales (-8 dB a -12 dBFS) para evitar saturación y volumen excesivo.
"""

import os
import numpy as np
import soundfile as sf

SR = 44100
TARGET_DIR = r"src/main/resources/assets/awakening_system/sounds"

def smooth_env(t, attack=0.004, decay=0.25):
    """Envolvente AD ultra-suave sin pops ni clics (rampa coseno en el ataque)."""
    env = np.exp(-t / max(0.001, decay))
    att_samples = int(SR * attack)
    if att_samples > 0 and len(env) >= att_samples:
        ramp = 0.5 * (1.0 - np.cos(np.linspace(0, np.pi, att_samples)))
        env[:att_samples] *= ramp
    return env

def glass_tone(freq, dur, decay, fm_ratio=2.0, fm_depth=1.2, fm_decay=0.04):
    """Genera un tono cristalino tipo campana de vidrio / holograma de anime con síntesis FM."""
    t = np.linspace(0, dur, int(SR * dur), False)
    env = smooth_env(t, attack=0.003, decay=decay)
    
    # Modulación FM inicial que le da el brillo a cristal
    fm_env = np.exp(-t / max(0.001, fm_decay))
    mod = fm_depth * fm_env * np.sin(2 * np.pi * (freq * fm_ratio) * t)
    
    # Fundamental + armónicos suaves
    carrier = np.sin(2 * np.pi * freq * t + mod)
    octave = 0.22 * np.sin(2 * np.pi * (freq * 2.0) * t) * np.exp(-t / max(0.001, decay * 0.7))
    sub_fifth = 0.15 * np.sin(2 * np.pi * (freq * 1.4983) * t) * np.exp(-t / max(0.001, decay * 0.8))
    
    return (carrier + octave + sub_fifth) * env

def normalize(audio, peak=0.42):
    """Normaliza con headroom limpio para que no suene fuerte ni sature en auriculares."""
    max_val = np.max(np.abs(audio))
    if max_val > 1e-5:
        return (audio / max_val * peak).astype(np.float32)
    return audio.astype(np.float32)

def save_sound(name, audio, peak=0.42):
    path = os.path.join(TARGET_DIR, f"{name}.ogg")
    audio = normalize(audio, peak=peak)
    # Suave fade-out final de 5ms para garantizar silencio absoluto al terminar
    fade_len = int(SR * 0.005)
    if len(audio) > fade_len:
        fade = 0.5 * (1.0 + np.cos(np.linspace(0, np.pi, fade_len)))
        audio[-fade_len:] *= fade
    sf.write(path, audio, SR, format='OGG', subtype='VORBIS')
    print(f"[OK] Generado: {name}.ogg (peak: {peak}, dur: {len(audio)/SR:.2f}s)")

# -------------------------------------------------------------
# 1. UI HOLOGRÁFICA & NAVEGACIÓN
# -------------------------------------------------------------

def make_system_open():
    """Chime holográfico de cristal futurista estilo Solo Leveling."""
    dur = 0.55
    t = np.linspace(0, dur, int(SR * dur), False)
    
    # Acorde cristalino suave: E5 (659Hz) -> B5 (988Hz) -> F#6 (1480Hz)
    n1 = glass_tone(659.25, dur, 0.35, fm_ratio=2.0, fm_depth=0.8, fm_decay=0.03)
    
    t2 = np.maximum(0, t - 0.04)
    n2 = np.where(t >= 0.04, glass_tone(987.77, dur, 0.40, fm_ratio=2.0, fm_depth=1.0, fm_decay=0.03)[:len(t)], 0)
    
    t3 = np.maximum(0, t - 0.08)
    n3 = np.where(t >= 0.08, glass_tone(1479.98, dur, 0.45, fm_ratio=2.0, fm_depth=1.2, fm_decay=0.04)[:len(t)], 0)
    
    # Sutil cuerpo cálido inferior (220 Hz)
    body = 0.25 * np.sin(2 * np.pi * 220.0 * t) * smooth_env(t, attack=0.005, decay=0.22)
    
    return n1 * 0.6 + n2 * 0.75 + n3 * 0.9 + body

def make_system_close():
    """Disolución suave y elegante de ventana holográfica."""
    dur = 0.22
    t = np.linspace(0, dur, int(SR * dur), False)
    
    # Caída suave de cristal de alta frecuencia a media: 1200Hz -> 500Hz
    f_sweep = 1200.0 * np.exp(-14.0 * t) + 400.0
    phase = np.cumsum(2 * np.pi * f_sweep / SR)
    env = smooth_env(t, attack=0.002, decay=0.12)
    tone = np.sin(phase) * env
    
    # Tono cálido de cierre
    warm = 0.3 * np.sin(2 * np.pi * 330.0 * t) * smooth_env(t, attack=0.002, decay=0.08)
    return tone * 0.7 + warm

def make_system_click():
    """Clic táctil holográfico sutil, limpio y placentero (sin ruido molesto)."""
    dur = 0.035
    t = np.linspace(0, dur, int(SR * dur), False)
    
    # Transitorio de gota de cristal / pulso de luz táctil
    f_click = 1600.0 * np.exp(-70.0 * t) + 600.0
    phase = np.cumsum(2 * np.pi * f_click / SR)
    env = smooth_env(t, attack=0.001, decay=0.015)
    click = np.sin(phase) * env
    
    # Armónico de segundo plano suave
    tick = 0.3 * np.sin(2 * np.pi * 3200.0 * t) * np.exp(-120.0 * t)
    return click + tick

def make_system_stat_up():
    """Oleada armónica y gratificante al subir un punto de estadística."""
    dur = 0.45
    t = np.linspace(0, dur, int(SR * dur), False)
    
    # Dos tonos cristalinos ascendentes en quinta perfecta: C6 (1046 Hz) -> G6 (1568 Hz)
    t1 = glass_tone(1046.50, dur, 0.28, fm_ratio=2.0, fm_depth=0.9, fm_decay=0.02)
    t2 = np.where(t >= 0.06, glass_tone(1567.98, dur, 0.35, fm_ratio=2.0, fm_depth=1.1, fm_decay=0.03)[:len(t)], 0)
    
    # Pulso cálido de poder que respalda el stat (130 Hz)
    sub = 0.35 * np.sin(2 * np.pi * 130.81 * t) * smooth_env(t, attack=0.004, decay=0.20)
    
    return t1 * 0.7 + t2 * 0.85 + sub

def make_system_skill_unlock():
    """Arpegio etéreo celestial al desbloquear o subir de nivel una habilidad."""
    dur = 1.1
    t = np.linspace(0, dur, int(SR * dur), False)
    
    notes = [587.33, 880.00, 1174.66, 1760.00] # Re Mayor brillante (D5, A5, D6, A6)
    sig = np.zeros_like(t)
    for i, freq in enumerate(notes):
        delay = i * 0.055
        tone = glass_tone(freq, dur, 0.45 + i * 0.1, fm_ratio=2.0, fm_depth=0.9, fm_decay=0.03)
        padded = np.zeros_like(t)
        d_samples = int(delay * SR)
        padded[d_samples:] = tone[:len(t) - d_samples]
        sig += padded * (0.65 + i * 0.12)
        
    return sig

def make_system_error():
    """Doble blip digital suave y elegante de acción denegada (nada estridente)."""
    dur = 0.20
    t = np.linspace(0, dur, int(SR * dur), False)
    
    p1 = np.where(t < 0.08, np.sin(2 * np.pi * 261.63 * t) * smooth_env(t, attack=0.002, decay=0.04), 0)
    t2 = np.maximum(0, t - 0.09)
    p2 = np.where(t >= 0.09, np.sin(2 * np.pi * 220.00 * t2) * smooth_env(t2, attack=0.002, decay=0.05), 0)
    
    return p1 * 0.75 + p2 * 0.75

# -------------------------------------------------------------
# 2. PROGRESIÓN, SUBIDA DE NIVEL Y DESPERTAR
# -------------------------------------------------------------

def make_system_level_up():
    """El emblemático sonido de Level Up de Solo Leveling: fanfarria triunfal celestial limpia."""
    dur = 1.7
    t = np.linspace(0, dur, int(SR * dur), False)
    
    # 1. Base cálida de resonancia que no satura (110 Hz suave)
    sub = 0.3 * np.sin(2 * np.pi * 110.0 * t) * smooth_env(t, attack=0.005, decay=0.45)
    
    # 2. Campanas cristalinas ascendentes: F5 (698Hz) -> A5 (880Hz) -> C6 (1046Hz) -> F6 (1397Hz)
    chord_freqs = [698.46, 880.00, 1046.50, 1396.91]
    bells = np.zeros_like(t)
    for i, f in enumerate(chord_freqs):
        delay = i * 0.065
        tone = glass_tone(f, dur, 0.65 + i * 0.15, fm_ratio=2.0, fm_depth=1.0, fm_decay=0.04)
        padded = np.zeros_like(t)
        d_samples = int(delay * SR)
        padded[d_samples:] = tone[:len(t) - d_samples]
        bells += padded * (0.6 + i * 0.15)
        
    # 3. Resonancia final brillante (C6 sostenido sutil)
    shimmer = 0.25 * np.sin(2 * np.pi * 2093.0 * t) * smooth_env(t, attack=0.25, decay=0.8)
    
    return sub + bells * 0.9 + shimmer

def make_system_rank_up():
    """Ascenso de Rango de Cazador: 3 acordes majestuosos y campana dorada."""
    dur = 2.0
    t = np.linspace(0, dur, int(SR * dur), False)
    
    # 3 acordes ascendentes dorados:
    # 1: G Mayor (392, 493, 587) a 0.0s
    # 2: A Mayor (440, 554, 659) a 0.22s
    # 3: D Mayor heroico (587, 740, 880, 1174) a 0.46s
    chords = [
        (0.00, [392.00, 493.88, 587.33], 0.5),
        (0.20, [440.00, 554.37, 659.25], 0.6),
        (0.44, [587.33, 739.99, 880.00, 1174.66], 0.9)
    ]
    sig = np.zeros_like(t)
    for delay, notes, decay in chords:
        d_samples = int(delay * SR)
        for n in notes:
            tone = glass_tone(n, dur, decay, fm_ratio=2.0, fm_depth=0.8, fm_decay=0.03)
            padded = np.zeros_like(t)
            padded[d_samples:] = tone[:len(t) - d_samples]
            sig += padded * 0.35
            
    # Campana dorada final resonante
    t_bell = np.maximum(0, t - 0.44)
    bell = 0.35 * np.sin(2 * np.pi * 1760.0 * t_bell) * np.where(t >= 0.44, smooth_env(t_bell, attack=0.003, decay=1.0), 0)
    
    return sig + bell

def make_system_awakening():
    """Despertar de Cazador / Monarca: pulso profundo y destello dimensional cristalino."""
    dur = 2.6
    t = np.linspace(0, dur, int(SR * dur), False)
    
    # 1. Pulso cálido de poder místico (82 Hz -> 110 Hz)
    f_pulse = 82.0 + 28.0 * (1.0 - np.exp(-6.0 * t))
    pulse = 0.4 * np.sin(np.cumsum(2 * np.pi * f_pulse / SR)) * smooth_env(t, attack=0.05, decay=0.8)
    
    # 2. Destello de campanas astrales a los 0.4s
    t_astral = np.maximum(0, t - 0.35)
    astral_notes = [587.33, 880.00, 1174.66, 1760.00, 2349.32]
    astral = np.zeros_like(t)
    for i, n in enumerate(astral_notes):
        tone = glass_tone(n, dur, 1.1, fm_ratio=2.0, fm_depth=1.1, fm_decay=0.04)
        padded = np.zeros_like(t)
        d_samples = int((0.35 + i * 0.04) * SR)
        padded[d_samples:] = tone[:len(t) - d_samples]
        astral += padded * (0.35 - i * 0.04)
        
    return pulse + astral

def make_system_quest_alert():
    """El icónico 'Ping!' del Sistema de Solo Leveling: doble campanada cristalina limpia."""
    dur = 0.70
    t = np.linspace(0, dur, int(SR * dur), False)
    
    # Pulso 1: F#6 (1479.98 Hz) a t = 0
    p1 = glass_tone(1479.98, dur, 0.32, fm_ratio=2.0, fm_depth=1.3, fm_decay=0.025)
    
    # Pulso 2: B6 (1975.53 Hz) a t = 0.09s
    t2 = np.maximum(0, t - 0.09)
    p2 = np.where(t >= 0.09, glass_tone(1975.53, dur, 0.48, fm_ratio=2.0, fm_depth=1.5, fm_decay=0.03)[:len(t)], 0)
    
    # Sutil brillo etéreo de fondo
    sparkle = 0.18 * np.sin(2 * np.pi * 3951.0 * t) * smooth_env(t, attack=0.09, decay=0.25)
    
    return p1 * 0.75 + p2 * 0.95 + sparkle

def make_system_quest_complete():
    """Fanfarria celestial de Misión Completada con recompensas entregadas."""
    dur = 1.3
    t = np.linspace(0, dur, int(SR * dur), False)
    
    # Progresión triunfal de 4 notas: G5 (784Hz) -> B5 (988Hz) -> D6 (1175Hz) -> G6 (1568Hz)
    notes = [783.99, 987.77, 1174.66, 1567.98]
    sig = np.zeros_like(t)
    for i, n in enumerate(notes):
        delay = i * 0.065
        tone = glass_tone(n, dur, 0.55 + i * 0.12, fm_ratio=2.0, fm_depth=1.0, fm_decay=0.035)
        padded = np.zeros_like(t)
        d_samples = int(delay * SR)
        padded[d_samples:] = tone[:len(t) - d_samples]
        sig += padded * (0.65 + i * 0.15)
        
    return sig

def make_system_coins():
    """Tintineo metálico de monedas de oro suaves y nítidas (agradables al oído)."""
    dur = 0.45
    t = np.linspace(0, dur, int(SR * dur), False)
    
    # Moneda 1: 1864 Hz con armónico
    c1 = (np.sin(2 * np.pi * 1864.66 * t) + 0.3 * np.sin(2 * np.pi * 3729.31 * t)) * smooth_env(t, attack=0.001, decay=0.12)
    
    # Moneda 2 a t = 0.06
    t2 = np.maximum(0, t - 0.06)
    c2 = np.where(t >= 0.06, (np.sin(2 * np.pi * 2217.46 * t2) + 0.35 * np.sin(2 * np.pi * 4434.92 * t2)) * smooth_env(t2, attack=0.001, decay=0.14), 0)
    
    # Moneda 3 a t = 0.12
    t3 = np.maximum(0, t - 0.12)
    c3 = np.where(t >= 0.12, (np.sin(2 * np.pi * 1975.53 * t3) + 0.25 * np.sin(2 * np.pi * 3951.07 * t3)) * smooth_env(t3, attack=0.001, decay=0.16), 0)
    
    return c1 * 0.6 + c2 * 0.75 + c3 * 0.65

def main():
    os.makedirs(TARGET_DIR, exist_ok=True)
    print("Iniciando generación de efectos sonoros del Sistema v2 (Crystal Sci-Fi Anime)...")
    
    # Calibración de volumen:
    # UI común (click, close, stat): 0.30 - 0.35 peak
    # Chimes y alertas: 0.38 - 0.42 peak
    # Fanfarrias mayores (level up, rank up, awakening): 0.44 - 0.48 peak
    save_sound("system_open", make_system_open(), peak=0.38)
    save_sound("system_close", make_system_close(), peak=0.30)
    save_sound("system_click", make_system_click(), peak=0.28)
    save_sound("system_stat_up", make_system_stat_up(), peak=0.36)
    save_sound("system_skill_unlock", make_system_skill_unlock(), peak=0.40)
    save_sound("system_error", make_system_error(), peak=0.32)
    save_sound("system_level_up", make_system_level_up(), peak=0.44)
    save_sound("system_rank_up", make_system_rank_up(), peak=0.45)
    save_sound("system_awakening", make_system_awakening(), peak=0.46)
    save_sound("system_quest_alert", make_system_quest_alert(), peak=0.40)
    save_sound("system_quest_complete", make_system_quest_complete(), peak=0.42)
    save_sound("system_coins", make_system_coins(), peak=0.35)
    
    print("¡Generación completada con éxito!")

if __name__ == "__main__":
    main()
