import os, re, sys

sys.stdout.reconfigure(encoding='utf-8')

boss_files = [
    'AncientCelestialDragonBossEntity.java',
    'BaranBossEntity.java',
    'BloodPatriarchBossEntity.java',
    'ElementalDemonKingBossEntity.java',
    'ElementalLordBossEntity.java',
    'IgrisBossEntity.java',
    'JadeGuardianBossEntity.java',
    'ShadowMonarchBossEntity.java',
    'YggdrasilTreantBossEntity.java'
]

dir_path = 'src/main/java/com/soybe/awakeningsystem/entity'

for bf in boss_files:
    p = os.path.join(dir_path, bf)
    if not os.path.exists(p):
        continue
    with open(p, 'r', encoding='utf-8', errors='ignore') as f:
        content = f.read()
    print(f'=== {bf} ===')
    for line in content.splitlines():
        if any(w in line for w in ['notify', 'displayClientMessage', 'sendSystemMessage', 'Component.literal', 'Component.translatable', 'broadcast']):
            print('  ' + line.strip())
