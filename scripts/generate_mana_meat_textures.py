from PIL import Image
import os

def hex_to_rgba(h, a=255):
    h = h.lstrip('#')
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)

_ = (0, 0, 0, 0)

# ==============================================================================
# 1. RAW MANA BEEF (mana_beef.png)
# Classic T-bone / Ribeye steak with glowing cyan mana marbling
# ==============================================================================
b_out = hex_to_rgba("#350711")
b_m1  = hex_to_rgba("#6E1222") # shadow
b_m2  = hex_to_rgba("#9E1C32") # core red
b_m3  = hex_to_rgba("#C82A45") # mid red
b_m4  = hex_to_rgba("#EE4463") # highlight red
b_v1  = hex_to_rgba("#0284C7") # deep cyan
b_v2  = hex_to_rgba("#38BDF8") # bright cyan
b_v3  = hex_to_rgba("#E0F2FE") # glowing cyan white
b_f1  = hex_to_rgba("#7DD3FC") # fat shade
b_f2  = hex_to_rgba("#F0F9FF") # fat bright
b_bn  = hex_to_rgba("#FFFFFF") # bone
b_bns = hex_to_rgba("#94A3B8") # bone shade

mana_beef_pixels = [
    [ _,     _,     _,     _,    b_out, b_out, b_out, b_out, b_out,  _,     _,     _,     _,     _,     _,     _ ],
    [ _,     _,    b_out, b_out, b_f2,  b_f2,  b_f2,  b_f1,  b_f2, b_out, b_out,  _,     _,     _,     _,     _ ],
    [ _,    b_out, b_f2,  b_f2,  b_m4,  b_m3,  b_v3,  b_v2,  b_m3, b_f2,  b_f1, b_out,  _,     _,     _,     _ ],
    [ _,    b_out, b_f2,  b_m4,  b_m3,  b_v3,  b_v2,  b_m2,  b_m3, b_m3,  b_f2,  b_out,  _,     _,     _,     _ ],
    [b_out, b_f2,  b_m3,  b_v3,  b_v2,  b_m2,  b_m2,  b_m2,  b_m1, b_m2,  b_f1, b_out,  _,     _,     _,     _ ],
    [b_out, b_f1,  b_m3,  b_v2,  b_m2,  b_bn,  b_bns, b_m1,  b_m2, b_m3,  b_m2, b_out,  _,     _,     _,     _ ],
    [b_out, b_f1,  b_m2,  b_m2,  b_bn,  b_bn,  b_bns, b_m1,  b_v3, b_v2,  b_m2, b_out,  _,     _,     _,     _ ],
    [b_out, b_m2,  b_m2,  b_m1,  b_bns, b_bns, b_m1,  b_v3,  b_v2, b_m2,  b_m3, b_out,  _,     _,     _,     _ ],
    [b_out, b_m2,  b_m1,  b_m1,  b_m1,  b_m1,  b_v3,  b_v2,  b_m2, b_m3,  b_m3, b_out,  _,     _,     _,     _ ],
    [b_out, b_m1,  b_m1,  b_v3,  b_v2,  b_m1,  b_v2,  b_m2,  b_m3, b_m4,  b_m2, b_out,  _,     _,     _,     _ ],
    [ _,    b_out, b_m1,  b_v2,  b_m2,  b_m2,  b_m2,  b_m3,  b_m3, b_m2,  b_out, b_out,  _,     _,     _,     _ ],
    [ _,    b_out, b_f1,  b_m2,  b_m2,  b_m3,  b_m2,  b_m2,  b_m1, b_m1, b_out,  _,     _,     _,     _,     _ ],
    [ _,     _,    b_out, b_f2,  b_f1,  b_m2,  b_m1,  b_m1, b_out, b_out,  _,     _,     _,     _,     _,     _ ],
    [ _,     _,     _,    b_out, b_out, b_f1, b_out, b_out,  _,     _,     _,     _,     _,     _,     _,     _ ],
    [ _,     _,     _,     _,     _,    b_out,  _,     _,     _,     _,     _,     _,     _,     _,     _,     _ ],
    [ _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _ ]
]

# ==============================================================================
# 2. COOKED MANA BEEF (cooked_mana_beef.png)
# Juicy grilled steak, diagonal grill char lines, shimmering mana juice
# ==============================================================================
cb_out = hex_to_rgba("#1F0A04")
cb_c1  = hex_to_rgba("#3E1508") # charred sear
cb_c2  = hex_to_rgba("#66250D") # deep roasted
cb_c3  = hex_to_rgba("#943B16") # caramelized crust
cb_c4  = hex_to_rgba("#C25422") # golden brown
cb_c5  = hex_to_rgba("#F59E0B") # sear highlight
cb_m1  = hex_to_rgba("#7A1818") # juicy center
cb_m2  = hex_to_rgba("#A62626") # tender pink
cb_v1  = hex_to_rgba("#0284C7")
cb_v2  = hex_to_rgba("#38BDF8")
cb_v3  = hex_to_rgba("#E0F2FE")

cooked_mana_beef_pixels = [
    [ _,      _,      _,      _,     cb_out, cb_out, cb_out, cb_out, cb_out,  _,      _,      _,      _,      _,      _,      _ ],
    [ _,      _,     cb_out, cb_out, cb_c4,  cb_c5,  cb_c4,  cb_c3,  cb_c4,  cb_out, cb_out,  _,      _,      _,      _,      _ ],
    [ _,     cb_out, cb_c4,  cb_c3,  cb_c1,  cb_c1,  cb_v3,  cb_v2,  cb_c3,  cb_c4,  cb_c3,  cb_out,  _,      _,      _,      _ ],
    [ _,     cb_out, cb_c4,  cb_c1,  cb_c1,  cb_m2,  cb_v2,  cb_c2,  cb_c1,  cb_c1,  cb_c4,  cb_out,  _,      _,      _,      _ ],
    [cb_out, cb_c4,  cb_c3,  cb_m2,  cb_v3,  cb_v2,  cb_c2,  cb_c1,  cb_c1,  cb_c2,  cb_c3,  cb_out,  _,      _,      _,      _ ],
    [cb_out, cb_c3,  cb_c1,  cb_c1,  cb_m2,  cb_c2,  cb_c1,  cb_c1,  cb_m2,  cb_c3,  cb_c2,  cb_out,  _,      _,      _,      _ ],
    [cb_out, cb_c3,  cb_c1,  cb_m1,  cb_c2,  cb_c1,  cb_c1,  cb_m1,  cb_v3,  cb_v2,  cb_c2,  cb_out,  _,      _,      _,      _ ],
    [cb_out, cb_c2,  cb_m1,  cb_m1,  cb_c1,  cb_c1,  cb_m1,  cb_v3,  cb_v2,  cb_c2,  cb_c3,  cb_out,  _,      _,      _,      _ ],
    [cb_out, cb_c2,  cb_c1,  cb_c1,  cb_c1,  cb_c1,  cb_v3,  cb_v2,  cb_c2,  cb_c3,  cb_c3,  cb_out,  _,      _,      _,      _ ],
    [cb_out, cb_c1,  cb_c1,  cb_v3,  cb_v2,  cb_c1,  cb_v2,  cb_c2,  cb_c3,  cb_c4,  cb_c2,  cb_out,  _,      _,      _,      _ ],
    [ _,     cb_out, cb_c1,  cb_v2,  cb_c2,  cb_c2,  cb_c2,  cb_c3,  cb_c3,  cb_c2,  cb_out, cb_out,  _,      _,      _,      _ ],
    [ _,     cb_out, cb_c3,  cb_c2,  cb_c2,  cb_c3,  cb_c2,  cb_c2,  cb_c1,  cb_c1,  cb_out,  _,      _,      _,      _,      _ ],
    [ _,      _,     cb_out, cb_c4,  cb_c3,  cb_c2,  cb_c1,  cb_c1,  cb_out, cb_out,  _,      _,      _,      _,      _,      _ ],
    [ _,      _,      _,     cb_out, cb_out, cb_c3,  cb_out, cb_out,  _,      _,      _,      _,      _,      _,      _,      _ ],
    [ _,      _,      _,      _,      _,     cb_out,  _,      _,      _,      _,      _,      _,      _,      _,      _,      _ ],
    [ _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _ ]
]

# ==============================================================================
# 3. RAW MANA PORKCHOP (mana_porkchop.png)
# Curved bone at top, tender pink cutlet with ethereal mana rind
# ==============================================================================
p_out = hex_to_rgba("#380B20")
p_f1  = hex_to_rgba("#7DD3FC")
p_f2  = hex_to_rgba("#BAE6FD")
p_f3  = hex_to_rgba("#F0F9FF")
p_m1  = hex_to_rgba("#831843") # deep pink
p_m2  = hex_to_rgba("#BE185D") # core rosy
p_m3  = hex_to_rgba("#EC4899") # bright rosy
p_m4  = hex_to_rgba("#F472B6") # highlight pink
p_v1  = hex_to_rgba("#0284C7")
p_v2  = hex_to_rgba("#38BDF8")
p_v3  = hex_to_rgba("#E0F2FE")
p_bn  = hex_to_rgba("#FFFFFF")
p_bns = hex_to_rgba("#CBD5E1")

mana_porkchop_pixels = [
    [ _,     _,     _,     _,    p_out, p_out, p_out,  _,     _,     _,     _,     _,     _,     _,     _,     _ ],
    [ _,     _,    p_out, p_out, p_bn,  p_bns, p_out, p_out,  _,     _,     _,     _,     _,     _,     _,     _ ],
    [ _,    p_out, p_f3,  p_bn,  p_bns, p_f3,  p_f2,  p_f1,  p_out,  _,     _,     _,     _,     _,     _,     _ ],
    [ _,    p_out, p_f3,  p_f2,  p_m4,  p_m3,  p_v3,  p_v2,  p_f2,  p_out,  _,     _,     _,     _,     _,     _ ],
    [p_out, p_f2,  p_m4,  p_m3,  p_v3,  p_v2,  p_m2,  p_m3,  p_f1,  p_out,  _,     _,     _,     _,     _,     _ ],
    [p_out, p_f1,  p_m3,  p_v3,  p_v2,  p_m2,  p_m2,  p_m2,  p_m2,  p_out,  _,     _,     _,     _,     _,     _ ],
    [p_out, p_m3,  p_m3,  p_v2,  p_m2,  p_m2,  p_m1,  p_m2,  p_m1,  p_out,  _,     _,     _,     _,     _,     _ ],
    [p_out, p_m2,  p_m2,  p_m2,  p_m1,  p_v3,  p_v2,  p_m1,  p_m1,  p_out,  _,     _,     _,     _,     _,     _ ],
    [p_out, p_m2,  p_m2,  p_m1,  p_v3,  p_v2,  p_m2,  p_m2,  p_m1,  p_out,  _,     _,     _,     _,     _,     _ ],
    [p_out, p_m1,  p_m1,  p_v3,  p_v2,  p_m2,  p_m3,  p_m2,  p_m1,  p_out,  _,     _,     _,     _,     _,     _ ],
    [ _,    p_out, p_m1,  p_v2,  p_m2,  p_m3,  p_m3,  p_m2,  p_m1,  p_out,  _,     _,     _,     _,     _,     _ ],
    [ _,    p_out, p_m1,  p_m1,  p_m2,  p_m3,  p_m2,  p_m1,  p_out,  _,     _,     _,     _,     _,     _,     _ ],
    [ _,     _,    p_out, p_m1,  p_m1,  p_m2,  p_m1,  p_out,  _,     _,     _,     _,     _,     _,     _,     _ ],
    [ _,     _,     _,    p_out, p_out, p_m1,  p_out,  _,     _,     _,     _,     _,     _,     _,     _,     _ ],
    [ _,     _,     _,     _,     _,    p_out,  _,     _,     _,     _,     _,     _,     _,     _,     _,     _ ],
    [ _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _ ]
]

# ==============================================================================
# 4. COOKED MANA PORKCHOP (cooked_mana_porkchop.png)
# Golden caramelized roasted porkchop with honey glaze and mana shimmer
# ==============================================================================
cp_out = hex_to_rgba("#291002")
cp_c1  = hex_to_rgba("#522105")
cp_c2  = hex_to_rgba("#7C3409")
cp_c3  = hex_to_rgba("#A8490E")
cp_c4  = hex_to_rgba("#D96918")
cp_c5  = hex_to_rgba("#F59E0B")
cp_c6  = hex_to_rgba("#FDE68A")
cp_m1  = hex_to_rgba("#6B2C0B")
cp_m2  = hex_to_rgba("#943E10")
cp_v1  = hex_to_rgba("#0284C7")
cp_v2  = hex_to_rgba("#38BDF8")
cp_v3  = hex_to_rgba("#E0F2FE")
cp_bn  = hex_to_rgba("#E2E8F0")
cp_bns = hex_to_rgba("#94A3B8")

cooked_mana_porkchop_pixels = [
    [ _,      _,      _,      _,     cp_out, cp_out, cp_out,  _,      _,      _,      _,      _,      _,      _,      _,      _ ],
    [ _,      _,     cp_out, cp_out, cp_bn,  cp_bns, cp_out, cp_out,  _,      _,      _,      _,      _,      _,      _,      _ ],
    [ _,     cp_out, cp_c6,  cp_bn,  cp_bns, cp_c6,  cp_c5,  cp_c4,  cp_out,  _,      _,      _,      _,      _,      _,      _ ],
    [ _,     cp_out, cp_c6,  cp_c5,  cp_c4,  cp_c3,  cp_v3,  cp_v2,  cp_c4,  cp_out,  _,      _,      _,      _,      _,      _ ],
    [cp_out, cp_c5,  cp_c4,  cp_c3,  cp_v3,  cp_v2,  cp_m2,  cp_c3,  cp_c3,  cp_out,  _,      _,      _,      _,      _,      _ ],
    [cp_out, cp_c4,  cp_c3,  cp_v3,  cp_v2,  cp_m2,  cp_m2,  cp_m1,  cp_c2,  cp_out,  _,      _,      _,      _,      _,      _ ],
    [cp_out, cp_c3,  cp_c3,  cp_v2,  cp_m2,  cp_m1,  cp_c1,  cp_m1,  cp_c1,  cp_out,  _,      _,      _,      _,      _,      _ ],
    [cp_out, cp_c2,  cp_m2,  cp_m1,  cp_c1,  cp_v3,  cp_v2,  cp_c1,  cp_c1,  cp_out,  _,      _,      _,      _,      _,      _ ],
    [cp_out, cp_c2,  cp_m1,  cp_c1,  cp_v3,  cp_v2,  cp_m2,  cp_c2,  cp_c1,  cp_out,  _,      _,      _,      _,      _,      _ ],
    [cp_out, cp_c1,  cp_c1,  cp_v3,  cp_v2,  cp_m2,  cp_c3,  cp_c2,  cp_c1,  cp_out,  _,      _,      _,      _,      _,      _ ],
    [ _,     cp_out, cp_c1,  cp_v2,  cp_m2,  cp_c3,  cp_c3,  cp_c2,  cp_c1,  cp_out,  _,      _,      _,      _,      _,      _ ],
    [ _,     cp_out, cp_c1,  cp_c1,  cp_m2,  cp_c3,  cp_c2,  cp_c1,  cp_out,  _,      _,      _,      _,      _,      _,      _ ],
    [ _,      _,     cp_out, cp_c1,  cp_c1,  cp_c2,  cp_c1,  cp_out,  _,      _,      _,      _,      _,      _,      _,      _ ],
    [ _,      _,      _,     cp_out, cp_out, cp_c1,  cp_out,  _,      _,      _,      _,      _,      _,      _,      _,      _ ],
    [ _,      _,      _,      _,      _,     cp_out,  _,      _,      _,      _,      _,      _,      _,      _,      _,      _ ],
    [ _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _ ]
]

# ==============================================================================
# 5. RAW MANA MUTTON (mana_mutton.png)
# Distinct Lamb chop with long slender rib bone extending to the top-right
# ==============================================================================
m_out = hex_to_rgba("#350606")
m_m1  = hex_to_rgba("#5F0D0D")
m_m2  = hex_to_rgba("#8E1414")
m_m3  = hex_to_rgba("#BD1E1E")
m_m4  = hex_to_rgba("#E53232")
m_v1  = hex_to_rgba("#0284C7")
m_v2  = hex_to_rgba("#38BDF8")
m_v3  = hex_to_rgba("#E0F2FE")
m_f1  = hex_to_rgba("#7DD3FC")
m_f2  = hex_to_rgba("#F0F9FF")
m_bn  = hex_to_rgba("#FFFFFF")
m_bns = hex_to_rgba("#CBD5E1")

mana_mutton_pixels = [
    [ _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _,    m_out, m_out,  _,     _ ],
    [ _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _,    m_out, m_bn,  m_out,  _,     _ ],
    [ _,     _,     _,     _,     _,     _,     _,     _,     _,     _,    m_out, m_bn,  m_bns, m_out,  _,     _ ],
    [ _,     _,     _,     _,     _,     _,     _,     _,     _,    m_out, m_bn,  m_bns, m_out,  _,     _,     _ ],
    [ _,     _,     _,     _,     _,     _,     _,     _,    m_out, m_bn,  m_bns, m_out,  _,     _,     _,     _ ],
    [ _,     _,     _,     _,     _,    m_out, m_out, m_out, m_bn,  m_bns, m_out,  _,     _,     _,     _,     _ ],
    [ _,     _,     _,     _,    m_out, m_f2,  m_f2,  m_f1,  m_bns, m_out,  _,     _,     _,     _,     _,     _ ],
    [ _,     _,     _,    m_out, m_f2,  m_m4,  m_m3,  m_v3,  m_v2,  m_out,  _,     _,     _,     _,     _,     _ ],
    [ _,     _,    m_out, m_f2,  m_m3,  m_v3,  m_v2,  m_m2,  m_m1,  m_out,  _,     _,     _,     _,     _,     _ ],
    [ _,    m_out, m_f1,  m_m3,  m_v2,  m_m2,  m_m2,  m_v3,  m_v2,  m_out,  _,     _,     _,     _,     _,     _ ],
    [ _,    m_out, m_m3,  m_m2,  m_m2,  m_v3,  m_v2,  m_m2,  m_m1,  m_out,  _,     _,     _,     _,     _,     _ ],
    [m_out, m_m2,  m_m2,  m_v3,  m_v2,  m_m2,  m_m1,  m_m1,  m_out,  _,     _,     _,     _,     _,     _,     _ ],
    [m_out, m_m2,  m_m1,  m_v2,  m_m1,  m_m1,  m_m1,  m_out,  _,     _,     _,     _,     _,     _,     _,     _ ],
    [m_out, m_m1,  m_m1,  m_m1,  m_m1,  m_out, m_out,  _,     _,     _,     _,     _,     _,     _,     _,     _ ],
    [ _,    m_out, m_out, m_out, m_out,  _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _ ],
    [ _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _ ]
]

# ==============================================================================
# 6. COOKED MANA MUTTON (cooked_mana_mutton.png)
# Roasted lamb chop with herb rub and rib bone
# ==============================================================================
cm_out = hex_to_rgba("#1F0A03")
cm_c1  = hex_to_rgba("#431505")
cm_c2  = hex_to_rgba("#6E270A")
cm_c3  = hex_to_rgba("#9E3C11")
cm_c4  = hex_to_rgba("#C7541B")
cm_h1  = hex_to_rgba("#16A34A") # herb green
cm_h2  = hex_to_rgba("#4ADE80") # bright herb
cm_v1  = hex_to_rgba("#0284C7")
cm_v2  = hex_to_rgba("#38BDF8")
cm_v3  = hex_to_rgba("#E0F2FE")
cm_bn  = hex_to_rgba("#E2E8F0")
cm_bns = hex_to_rgba("#94A3B8")

cooked_mana_mutton_pixels = [
    [ _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _,     cm_out, cm_out,  _,      _ ],
    [ _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _,     cm_out, cm_bn,  cm_out,  _,      _ ],
    [ _,      _,      _,      _,      _,      _,      _,      _,      _,      _,     cm_out, cm_bn,  cm_bns, cm_out,  _,      _ ],
    [ _,      _,      _,      _,      _,      _,      _,      _,      _,     cm_out, cm_bn,  cm_bns, cm_out,  _,      _,      _ ],
    [ _,      _,      _,      _,      _,      _,      _,      _,     cm_out, cm_bn,  cm_bns, cm_out,  _,      _,      _,      _ ],
    [ _,      _,      _,      _,      _,     cm_out, cm_out, cm_out, cm_bn,  cm_bns, cm_out,  _,      _,      _,      _,      _ ],
    [ _,      _,      _,      _,     cm_out, cm_c4,  cm_c4,  cm_c3,  cm_bns, cm_out,  _,      _,      _,      _,      _,      _ ],
    [ _,      _,      _,     cm_out, cm_c4,  cm_c3,  cm_h2,  cm_v3,  cm_v2,  cm_out,  _,      _,      _,      _,      _,      _ ],
    [ _,      _,     cm_out, cm_c4,  cm_c3,  cm_v3,  cm_v2,  cm_h1,  cm_c2,  cm_out,  _,      _,      _,      _,      _,      _ ],
    [ _,     cm_out, cm_c3,  cm_c3,  cm_v2,  cm_c2,  cm_c2,  cm_v3,  cm_v2,  cm_out,  _,      _,      _,      _,      _,      _ ],
    [ _,     cm_out, cm_c3,  cm_c2,  cm_c2,  cm_v3,  cm_v2,  cm_c2,  cm_c1,  cm_out,  _,      _,      _,      _,      _,      _ ],
    [cm_out, cm_c2,  cm_c2,  cm_v3,  cm_v2,  cm_c2,  cm_c1,  cm_c1,  cm_out,  _,      _,      _,      _,      _,      _,      _ ],
    [cm_out, cm_c2,  cm_c1,  cm_v2,  cm_c1,  cm_c1,  cm_c1,  cm_out,  _,      _,      _,      _,      _,      _,      _,      _ ],
    [cm_out, cm_c1,  cm_c1,  cm_c1,  cm_c1,  cm_out, cm_out,  _,      _,      _,      _,      _,      _,      _,      _,      _ ],
    [ _,     cm_out, cm_out, cm_out, cm_out,  _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _ ],
    [ _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _ ]
]

# ==============================================================================
# 7. RAW MANA CHICKEN (mana_chicken_meat.png)
# Classic drumstick with double-ball joint bone at top, plump rounded bell at bottom
# ==============================================================================
k_out = hex_to_rgba("#380916")
k_m1  = hex_to_rgba("#7A142D")
k_m2  = hex_to_rgba("#A82245")
k_m3  = hex_to_rgba("#D43862")
k_m4  = hex_to_rgba("#F4638B")
k_m5  = hex_to_rgba("#FDA4BA")
k_v1  = hex_to_rgba("#0284C7")
k_v2  = hex_to_rgba("#38BDF8")
k_v3  = hex_to_rgba("#E0F2FE")
k_bn  = hex_to_rgba("#FFFFFF")
k_bns = hex_to_rgba("#CBD5E1")

mana_chicken_pixels = [
    [ _,     _,     _,     _,     _,     _,     _,     _,     _,    k_out, k_out,  _,    k_out, k_out,  _,     _ ],
    [ _,     _,     _,     _,     _,     _,     _,     _,    k_out, k_bn,  k_out, k_out, k_bn,  k_out,  _,     _ ],
    [ _,     _,     _,     _,     _,     _,     _,     _,    k_out, k_bn,  k_bns, k_bn,  k_bns, k_out,  _,     _ ],
    [ _,     _,     _,     _,     _,     _,     _,     _,     _,    k_out, k_bn,  k_bns, k_out,  _,     _,     _ ],
    [ _,     _,     _,     _,     _,     _,     _,     _,    k_out, k_bn,  k_bns, k_out,  _,     _,     _,     _ ],
    [ _,     _,     _,     _,     _,     _,    k_out, k_out, k_bn,  k_bns, k_out,  _,     _,     _,     _,     _ ],
    [ _,     _,     _,     _,     _,    k_out, k_m5,  k_m5,  k_m4,  k_out,  _,     _,     _,     _,     _,     _ ],
    [ _,     _,     _,     _,    k_out, k_m5,  k_m4,  k_m3,  k_v3,  k_v2,  k_out,  _,     _,     _,     _,     _ ],
    [ _,     _,     _,    k_out, k_m5,  k_m4,  k_v3,  k_v2,  k_m3,  k_m2,  k_out,  _,     _,     _,     _,     _ ],
    [ _,     _,    k_out, k_m4,  k_v3,  k_v2,  k_m3,  k_m2,  k_m2,  k_m1,  k_out,  _,     _,     _,     _,     _ ],
    [ _,    k_out, k_m4,  k_v2,  k_m3,  k_m2,  k_m2,  k_m1,  k_m1,  k_out,  _,     _,     _,     _,     _,     _ ],
    [ _,    k_out, k_m3,  k_m3,  k_m2,  k_m1,  k_m1,  k_m1,  k_out,  _,     _,     _,     _,     _,     _,     _ ],
    [ _,    k_out, k_m3,  k_m2,  k_m1,  k_m1,  k_m1,  k_out,  _,     _,     _,     _,     _,     _,     _,     _ ],
    [ _,     _,    k_out, k_m2,  k_m1,  k_m1,  k_out,  _,     _,     _,     _,     _,     _,     _,     _,     _ ],
    [ _,     _,     _,    k_out, k_out, k_out,  _,     _,     _,     _,     _,     _,     _,     _,     _,     _ ],
    [ _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _,     _ ]
]

# ==============================================================================
# 8. COOKED MANA CHICKEN (cooked_mana_chicken_meat.png)
# Golden-crispy roasted drumstick with appetizing skin and double-knob bone
# ==============================================================================
ck_out = hex_to_rgba("#2B0E03")
ck_c1  = hex_to_rgba("#541B07")
ck_c2  = hex_to_rgba("#852F0C")
ck_c3  = hex_to_rgba("#B84612")
ck_c4  = hex_to_rgba("#E3641B")
ck_c5  = hex_to_rgba("#FB923C")
ck_c6  = hex_to_rgba("#FEF08A") # crispy fat glint
ck_v1  = hex_to_rgba("#0284C7")
ck_v2  = hex_to_rgba("#38BDF8")
ck_v3  = hex_to_rgba("#E0F2FE")
ck_bn  = hex_to_rgba("#E2E8F0")
ck_bns = hex_to_rgba("#94A3B8")

cooked_mana_chicken_pixels = [
    [ _,      _,      _,      _,      _,      _,      _,      _,      _,     ck_out, ck_out,  _,    ck_out, ck_out,  _,      _ ],
    [ _,      _,      _,      _,      _,      _,      _,      _,     ck_out, ck_bn,  ck_out, ck_out, ck_bn,  ck_out,  _,      _ ],
    [ _,      _,      _,      _,      _,      _,      _,      _,     ck_out, ck_bn,  ck_bns, ck_bn,  ck_bns, ck_out,  _,      _ ],
    [ _,      _,      _,      _,      _,      _,      _,      _,      _,     ck_out, ck_bn,  ck_bns, ck_out,  _,      _,      _ ],
    [ _,      _,      _,      _,      _,      _,      _,      _,     ck_out, ck_bn,  ck_bns, ck_out,  _,      _,      _,      _ ],
    [ _,      _,      _,      _,      _,      _,     ck_out, ck_out, ck_bn,  ck_bns, ck_out,  _,      _,      _,      _,      _ ],
    [ _,      _,      _,      _,      _,     ck_out, ck_c5,  ck_c6,  ck_c5,  ck_out,  _,      _,      _,      _,      _,      _ ],
    [ _,      _,      _,      _,     ck_out, ck_c5,  ck_c4,  ck_c3,  ck_v3,  ck_v2,  ck_out,  _,      _,      _,      _,      _ ],
    [ _,      _,      _,     ck_out, ck_c5,  ck_c4,  ck_v3,  ck_v2,  ck_c3,  ck_c2,  ck_out,  _,      _,      _,      _,      _ ],
    [ _,      _,     ck_out, ck_c4,  ck_v3,  ck_v2,  ck_c3,  ck_c2,  ck_c2,  ck_c1,  ck_out,  _,      _,      _,      _,      _ ],
    [ _,     ck_out, ck_c4,  ck_v2,  ck_c3,  ck_c2,  ck_c2,  ck_c1,  ck_c1,  ck_out,  _,      _,      _,      _,      _,      _ ],
    [ _,     ck_out, ck_c3,  ck_c3,  ck_c2,  ck_c1,  ck_c1,  ck_c1,  ck_out,  _,      _,      _,      _,      _,      _,      _ ],
    [ _,     ck_out, ck_c3,  ck_c2,  ck_c1,  ck_c1,  ck_c1,  ck_out,  _,      _,      _,      _,      _,      _,      _,      _ ],
    [ _,      _,     ck_out, ck_c2,  ck_c1,  ck_c1,  ck_out,  _,      _,      _,      _,      _,      _,      _,      _,      _ ],
    [ _,      _,      _,     ck_out, ck_out, ck_out,  _,      _,      _,      _,      _,      _,      _,      _,      _,      _ ],
    [ _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _,      _ ]
]

def save_texture(pixels, path):
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), pixels[y][x])
    im.save(path)
    print(f"Saved {path} ({im.size})")

base_dir = "src/main/resources/assets/awakening_system/textures/item"
save_texture(mana_beef_pixels, os.path.join(base_dir, "mana_beef.png"))
save_texture(cooked_mana_beef_pixels, os.path.join(base_dir, "cooked_mana_beef.png"))
save_texture(mana_porkchop_pixels, os.path.join(base_dir, "mana_porkchop.png"))
save_texture(cooked_mana_porkchop_pixels, os.path.join(base_dir, "cooked_mana_porkchop.png"))
save_texture(mana_mutton_pixels, os.path.join(base_dir, "mana_mutton.png"))
save_texture(cooked_mana_mutton_pixels, os.path.join(base_dir, "cooked_mana_mutton.png"))
save_texture(mana_chicken_pixels, os.path.join(base_dir, "mana_chicken_meat.png"))
save_texture(cooked_mana_chicken_pixels, os.path.join(base_dir, "cooked_mana_chicken_meat.png"))
