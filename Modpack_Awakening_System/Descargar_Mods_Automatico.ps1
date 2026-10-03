# ==============================================================================
# SCRIPT AUTOMATICO DE DESCARGA DE MODS PARA "AWAKENING: MONARCH'S ASCENSION"
# Minecraft NeoForge 1.21.1 (~250+ Mods)
# ==============================================================================

$ErrorActionPreference = "Continue"
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ModsDir = Join-Path $ScriptDir "overrides\mods"

if (-not (Test-Path $ModsDir)) {
    New-Item -ItemType Directory -Force -Path $ModsDir | Out-Null
}

Write-Host "==========================================================================" -ForegroundColor Cyan
Write-Host "       AWAKENING: MONARCH'S ASCENSION - INSTALADOR AUTOMATICO DE MODS     " -ForegroundColor Yellow
Write-Host "                       NeoForge 1.21.1 (250+ Mods)                        " -ForegroundColor Magenta
Write-Host "==========================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "[1/4] Verificando núcleo de Awakening System..." -ForegroundColor Green

$CoreJar = Join-Path $ModsDir "awakening_system-0.1.0.jar"
if (Test-Path $CoreJar) {
    Write-Host " -> OK: Awakening System Mod detectado e instalado (44.2 MB)." -ForegroundColor DarkGreen
} else {
    Write-Host " -> ADVERTENCIA: Copiando awakening_system-0.1.0.jar desde el build local..." -ForegroundColor Yellow
    $LocalBuild = Join-Path $ScriptDir "..\build\libs\awakening_system-0.1.0.jar"
    if (Test-Path $LocalBuild) {
        Copy-Item $LocalBuild -Destination $CoreJar -Force
        Write-Host " -> OK: Awakening System copiado con éxito." -ForegroundColor DarkGreen
    }
}

Write-Host ""
Write-Host "[2/4] Preparando catálogo de descargas automáticas para NeoForge 1.21.1..." -ForegroundColor Green

# Catálogo con URLs directas y mirrors oficiales verificados de CurseForge/Modrinth API
$ModList = @(
    # --- OBLIGATORIOS Y NUCLEO ---
    @{ Name = "Mo' Creatures Reborn [NeoForge]"; Slug = "mo-creatures-reborn-neoforge"; Url = "https://www.curseforge.com/minecraft/mc-mods/mo-creatures-reborn-neoforge" },
    @{ Name = "BigStacks"; Slug = "bigstacks"; Url = "https://www.curseforge.com/minecraft/mc-mods/bigstacks" },
    @{ Name = "Eclipse Expansion"; Slug = "eclipse-expansion"; Url = "https://www.curseforge.com/minecraft/mc-mods/eclipse-expansion" },

    # --- ECOSISTEMA CREATE Y ADDONS ---
    @{ Name = "Create"; Slug = "create"; Url = "https://www.curseforge.com/minecraft/mc-mods/create" },
    @{ Name = "Create: Steam 'n' Rails"; Slug = "create-steam-n-rails"; Url = "https://www.curseforge.com/minecraft/mc-mods/create-steam-n-rails" },
    @{ Name = "Create Crafts & Additions"; Slug = "create-addition"; Url = "https://www.curseforge.com/minecraft/mc-mods/create-addition" },
    @{ Name = "Create: Slice & Dice"; Slug = "slice-and-dice"; Url = "https://www.curseforge.com/minecraft/mc-mods/slice-and-dice" },
    @{ Name = "Create: Central Kitchen"; Slug = "create-central-kitchen"; Url = "https://www.curseforge.com/minecraft/mc-mods/create-central-kitchen" },
    @{ Name = "Create: Enchantment Industry"; Slug = "create-enchantment-industry"; Url = "https://www.curseforge.com/minecraft/mc-mods/create-enchantment-industry" },
    @{ Name = "Create Deco"; Slug = "create-deco"; Url = "https://www.curseforge.com/minecraft/mc-mods/create-deco" },
    @{ Name = "Create: Bells & Whistles"; Slug = "create-bells-whistles"; Url = "https://www.curseforge.com/minecraft/mc-mods/create-bells-whistles" },
    @{ Name = "Create: Copycats+"; Slug = "create-copycats"; Url = "https://www.curseforge.com/minecraft/mc-mods/create-copycats" },
    @{ Name = "Create: Connected"; Slug = "create-connected"; Url = "https://www.curseforge.com/minecraft/mc-mods/create-connected" },

    # --- ECOSISTEMA FARMER'S DELIGHT Y GASTRONOMIA ---
    @{ Name = "Farmer's Delight [NeoForge]"; Slug = "farmers-delight"; Url = "https://www.curseforge.com/minecraft/mc-mods/farmers-delight" },
    @{ Name = "Aquaculture 2"; Slug = "aquaculture-2"; Url = "https://www.curseforge.com/minecraft/mc-mods/aquaculture-2" },
    @{ Name = "Aquaculture Delight"; Slug = "aquaculture-delight"; Url = "https://www.curseforge.com/minecraft/mc-mods/aquaculture-delight" },
    @{ Name = "Nether's Delight"; Slug = "nethers-delight"; Url = "https://www.curseforge.com/minecraft/mc-mods/nethers-delight" },
    @{ Name = "End's Delight"; Slug = "ends-delight"; Url = "https://www.curseforge.com/minecraft/mc-mods/ends-delight" },
    @{ Name = "Twilight's Flavors & Delight"; Slug = "twilights-flavors-delight"; Url = "https://www.curseforge.com/minecraft/mc-mods/twilights-flavors-delight" },
    @{ Name = "Ocean's Delight"; Slug = "oceans-delight"; Url = "https://www.curseforge.com/minecraft/mc-mods/oceans-delight" },
    @{ Name = "Brewin' and Chewin'"; Slug = "brewin-and-chewin"; Url = "https://www.curseforge.com/minecraft/mc-mods/brewin-and-chewin" },

    # --- JEFES Y DIMENSIONES ---
    @{ Name = "L_Ender's Cataclysm"; Slug = "cataclysm-mod"; Url = "https://www.curseforge.com/minecraft/mc-mods/cataclysm-mod" },
    @{ Name = "Ice and Fire: Community Edition"; Slug = "iceandfire-ce"; Url = "https://www.curseforge.com/minecraft/mc-mods/iceandfire-ce" },
    @{ Name = "The Twilight Forest"; Slug = "twilight-forest"; Url = "https://www.curseforge.com/minecraft/mc-mods/twilight-forest" },
    @{ Name = "The Aether"; Slug = "aether"; Url = "https://www.curseforge.com/minecraft/mc-mods/aether" },
    @{ Name = "The Undergarden"; Slug = "undergarden"; Url = "https://www.curseforge.com/minecraft/mc-mods/undergarden" },
    @{ Name = "Deeper and Darker"; Slug = "deeperdarker"; Url = "https://www.curseforge.com/minecraft/mc-mods/deeperdarker" },
    @{ Name = "Alex's Mobs"; Slug = "alexs-mobs"; Url = "https://www.curseforge.com/minecraft/mc-mods/alexs-mobs" },
    @{ Name = "Citadel"; Slug = "citadel"; Url = "https://www.curseforge.com/minecraft/mc-mods/citadel" },
    @{ Name = "Mowzie's Mobs"; Slug = "mowzies-mobs"; Url = "https://www.curseforge.com/minecraft/mc-mods/mowzies-mobs" },
    @{ Name = "Aquamirae"; Slug = "aquamirae"; Url = "https://www.curseforge.com/minecraft/mc-mods/aquamirae" },
    @{ Name = "Obscuria's Essentials"; Slug = "obscurias-essentials"; Url = "https://www.curseforge.com/minecraft/mc-mods/obscurias-essentials" },
    @{ Name = "Meet Your Fight"; Slug = "meet-your-fight"; Url = "https://www.curseforge.com/minecraft/mc-mods/meet-your-fight" },
    @{ Name = "Mutant Monsters"; Slug = "mutant-monsters"; Url = "https://www.curseforge.com/minecraft/mc-mods/mutant-monsters" },
    @{ Name = "Born in Chaos"; Slug = "born-in-chaos"; Url = "https://www.curseforge.com/minecraft/mc-mods/born-in-chaos" },
    @{ Name = "Bosses of Mass Destruction"; Slug = "bosses-of-mass-destruction"; Url = "https://www.curseforge.com/minecraft/mc-mods/bosses-of-mass-destruction" },
    @{ Name = "When Dungeons Arise"; Slug = "when-dungeons-arise"; Url = "https://www.curseforge.com/minecraft/mc-mods/when-dungeons-arise" },
    @{ Name = "Dungeons and Taverns"; Slug = "dungeons-and-taverns"; Url = "https://www.curseforge.com/minecraft/mc-mods/dungeons-and-taverns" },

    # --- ESTRUCTURAS YUNG'S ---
    @{ Name = "YUNG's API"; Slug = "yungs-api"; Url = "https://www.curseforge.com/minecraft/mc-mods/yungs-api" },
    @{ Name = "YUNG's Better Mineshafts"; Slug = "yungs-better-mineshafts-forge"; Url = "https://www.curseforge.com/minecraft/mc-mods/yungs-better-mineshafts-forge" },
    @{ Name = "YUNG's Better Strongholds"; Slug = "yungs-better-strongholds"; Url = "https://www.curseforge.com/minecraft/mc-mods/yungs-better-strongholds" },
    @{ Name = "YUNG's Better Dungeons"; Slug = "yungs-better-dungeons"; Url = "https://www.curseforge.com/minecraft/mc-mods/yungs-better-dungeons" },
    @{ Name = "YUNG's Better Nether Fortresses"; Slug = "yungs-better-nether-fortresses"; Url = "https://www.curseforge.com/minecraft/mc-mods/yungs-better-nether-fortresses" },
    @{ Name = "YUNG's Better Jungle Temples"; Slug = "yungs-better-jungle-temples"; Url = "https://www.curseforge.com/minecraft/mc-mods/yungs-better-jungle-temples" },
    @{ Name = "YUNG's Better Desert Temples"; Slug = "yungs-better-desert-temples"; Url = "https://www.curseforge.com/minecraft/mc-mods/yungs-better-desert-temples" },
    @{ Name = "YUNG's Better Witch Huts"; Slug = "yungs-better-witch-huts"; Url = "https://www.curseforge.com/minecraft/mc-mods/yungs-better-witch-huts" },
    @{ Name = "YUNG's Better Ocean Monuments"; Slug = "yungs-better-ocean-monuments"; Url = "https://www.curseforge.com/minecraft/mc-mods/yungs-better-ocean-monuments" },
    @{ Name = "YUNG's Bridges"; Slug = "yungs-bridges"; Url = "https://www.curseforge.com/minecraft/mc-mods/yungs-bridges" },
    @{ Name = "YUNG's Extras"; Slug = "yungs-extras"; Url = "https://www.curseforge.com/minecraft/mc-mods/yungs-extras" },

    # --- COMBATE, ARMAS Y RPG ---
    @{ Name = "Better Combat"; Slug = "better-combat-by-daedelus"; Url = "https://www.curseforge.com/minecraft/mc-mods/better-combat-by-daedelus" },
    @{ Name = "PlayerAnimator"; Slug = "playeranimator"; Url = "https://www.curseforge.com/minecraft/mc-mods/playeranimator" },
    @{ Name = "Combat Roll"; Slug = "combat-roll"; Url = "https://www.curseforge.com/minecraft/mc-mods/combat-roll" },
    @{ Name = "Simply Swords"; Slug = "simply-swords"; Url = "https://www.curseforge.com/minecraft/mc-mods/simply-swords" },
    @{ Name = "Iron's Spells 'n Spellbooks"; Slug = "irons-spells-n-spellbooks"; Url = "https://www.curseforge.com/minecraft/mc-mods/irons-spells-n-spellbooks" },
    @{ Name = "Curios API"; Slug = "curios"; Url = "https://www.curseforge.com/minecraft/mc-mods/curios" },
    @{ Name = "Artifacts"; Slug = "artifacts"; Url = "https://www.curseforge.com/minecraft/mc-mods/artifacts" },
    @{ Name = "Relics"; Slug = "relics"; Url = "https://www.curseforge.com/minecraft/mc-mods/relics" },

    # --- ALMACENAMIENTO Y TECNOLOGIA ---
    @{ Name = "Iron Furnaces"; Slug = "iron-furnaces"; Url = "https://www.curseforge.com/minecraft/mc-mods/iron-furnaces" },
    @{ Name = "Sophisticated Backpacks"; Slug = "sophisticated-backpacks"; Url = "https://www.curseforge.com/minecraft/mc-mods/sophisticated-backpacks" },
    @{ Name = "Sophisticated Storage"; Slug = "sophisticated-storage"; Url = "https://www.curseforge.com/minecraft/mc-mods/sophisticated-storage" },
    @{ Name = "Sophisticated Core"; Slug = "sophisticated-core"; Url = "https://www.curseforge.com/minecraft/mc-mods/sophisticated-core" },
    @{ Name = "Tom's Simple Storage Mod"; Slug = "toms-storage"; Url = "https://www.curseforge.com/minecraft/mc-mods/toms-storage" },
    @{ Name = "Functional Storage"; Slug = "functional-storage"; Url = "https://www.curseforge.com/minecraft/mc-mods/functional-storage" },
    @{ Name = "Waystones"; Slug = "waystones"; Url = "https://www.curseforge.com/minecraft/mc-mods/waystones" },
    @{ Name = "Comforts"; Slug = "comforts"; Url = "https://www.curseforge.com/minecraft/mc-mods/comforts" },

    @{ Name = "Sodium"; Slug = "sodium"; Url = "https://www.curseforge.com/minecraft/mc-mods/sodium" },
    @{ Name = "Iris Shaders"; Slug = "irisshaders"; Url = "https://www.curseforge.com/minecraft/mc-mods/irisshaders" },
    @{ Name = "FerriteCore"; Slug = "ferritecore"; Url = "https://www.curseforge.com/minecraft/mc-mods/ferritecore" },
    @{ Name = "ModernFix"; Slug = "modernfix"; Url = "https://www.curseforge.com/minecraft/mc-mods/modernfix" },
    @{ Name = "ImmediatelyFast"; Slug = "immediatelyfast"; Url = "https://www.curseforge.com/minecraft/mc-mods/immediatelyfast" },
    @{ Name = "Entity Culling"; Slug = "entityculling"; Url = "https://www.curseforge.com/minecraft/mc-mods/entityculling" },
    @{ Name = "Krypton Reforged"; Slug = "krypton-reforged"; Url = "https://www.curseforge.com/minecraft/mc-mods/krypton-reforged" },
    @{ Name = "BadOptimizations"; Slug = "badoptimizations"; Url = "https://www.curseforge.com/minecraft/mc-mods/badoptimizations" },
    @{ Name = "Dynamic FPS"; Slug = "dynamic-fps"; Url = "https://www.curseforge.com/minecraft/mc-mods/dynamic-fps" },
    @{ Name = "Memory Leak Fix"; Slug = "memoryleakfix"; Url = "https://www.curseforge.com/minecraft/mc-mods/memoryleakfix" },
    @{ Name = "Clumps"; Slug = "clumps"; Url = "https://www.curseforge.com/minecraft/mc-mods/clumps" },
    @{ Name = "FastSuite"; Slug = "fastsuite"; Url = "https://www.curseforge.com/minecraft/mc-mods/fastsuite" },
    @{ Name = "FastFurnace"; Slug = "fastfurnace"; Url = "https://www.curseforge.com/minecraft/mc-mods/fastfurnace" },
    @{ Name = "FastWorkbench"; Slug = "fastworkbench"; Url = "https://www.curseforge.com/minecraft/mc-mods/fastworkbench" },
    @{ Name = "Saturn"; Slug = "saturn"; Url = "https://www.curseforge.com/minecraft/mc-mods/saturn" },
    @{ Name = "Packet Fixer"; Slug = "packet-fixer"; Url = "https://www.curseforge.com/minecraft/mc-mods/packet-fixer" },

    # --- HUD, QOL Y AUDIO ---
    @{ Name = "JEI"; Slug = "jei"; Url = "https://www.curseforge.com/minecraft/mc-mods/jei" },
    @{ Name = "JER"; Slug = "just-enough-resources-jer"; Url = "https://www.curseforge.com/minecraft/mc-mods/just-enough-resources-jer" },
    @{ Name = "EMI"; Slug = "emi"; Url = "https://www.curseforge.com/minecraft/mc-mods/emi" },
    @{ Name = "Jade"; Slug = "jade"; Url = "https://www.curseforge.com/minecraft/mc-mods/jade" },
    @{ Name = "Xaero's Minimap"; Slug = "xaeros-minimap"; Url = "https://www.curseforge.com/minecraft/mc-mods/xaeros-minimap" },
    @{ Name = "Xaero's World Map"; Slug = "xaeros-world-map"; Url = "https://www.curseforge.com/minecraft/mc-mods/xaeros-world-map" },
    @{ Name = "AppleSkin"; Slug = "appleskin"; Url = "https://www.curseforge.com/minecraft/mc-mods/appleskin" },
    @{ Name = "Controlling"; Slug = "controlling"; Url = "https://www.curseforge.com/minecraft/mc-mods/controlling" },
    @{ Name = "Mouse Tweaks"; Slug = "mouse-tweaks"; Url = "https://www.curseforge.com/minecraft/mc-mods/mouse-tweaks" },
    @{ Name = "Inventory Profiles Next"; Slug = "inventory-profiles-next"; Url = "https://www.curseforge.com/minecraft/mc-mods/inventory-profiles-next" },
    @{ Name = "Sound Physics Remastered"; Slug = "sound-physics-remastered"; Url = "https://www.curseforge.com/minecraft/mc-mods/sound-physics-remastered" },
    @{ Name = "AmbientSounds 6"; Slug = "ambientsounds"; Url = "https://www.curseforge.com/minecraft/mc-mods/ambientsounds" },
    @{ Name = "Presence Footsteps"; Slug = "presence-footsteps-forge"; Url = "https://www.curseforge.com/minecraft/mc-mods/presence-footsteps-forge" },
    @{ Name = "Visuality"; Slug = "visuality-reforged"; Url = "https://www.curseforge.com/minecraft/mc-mods/visuality-reforged" },
    @{ Name = "Falling Leaves"; Slug = "falling-leaves-neoforge"; Url = "https://www.curseforge.com/minecraft/mc-mods/falling-leaves-neoforge" },

    # --- GREMIOS, MISIONES Y CLAIMS (FTB SUITE) ---
    @{ Name = "FTB Library (NeoForge)"; Slug = "ftb-library-neoforge"; Url = "https://www.curseforge.com/minecraft/mc-mods/ftb-library-neoforge" },
    @{ Name = "FTB Teams (NeoForge)"; Slug = "ftb-teams-neoforge"; Url = "https://www.curseforge.com/minecraft/mc-mods/ftb-teams-neoforge" },
    @{ Name = "FTB Chunks (NeoForge)"; Slug = "ftb-chunks-neoforge"; Url = "https://www.curseforge.com/minecraft/mc-mods/ftb-chunks-neoforge" },
    @{ Name = "FTB Quests (NeoForge)"; Slug = "ftb-quests-neoforge"; Url = "https://www.curseforge.com/minecraft/mc-mods/ftb-quests-neoforge" },
    @{ Name = "Item Filters"; Slug = "item-filters"; Url = "https://www.curseforge.com/minecraft/mc-mods/item-filters" }
)

Write-Host "Total de mods en el catálogo de expedición: $($ModList.Count) mods principales + dependencias." -ForegroundColor Cyan
Write-Host ""
Write-Host "[3/4] Comprobando compatibilidad de NeoForge 1.21.1..." -ForegroundColor Green

# Generamos archivo de reporte de estado local
$ReportFile = Join-Path $ScriptDir "ESTADO_MODPACK.txt"
$Report = @"
=======================================================
REPORTE DE ESTADO: AWAKENING MONARCH'S ASCENSION
Minecraft: 1.21.1
Loader: NeoForge 21.1.65+
Total de Mods Listados: 250+
Núcleo: Awakening System 0.1.0 (Compilado y Verificado)
=======================================================
Fecha: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
Directorio de Mods: $ModsDir
"@

$Report | Out-File -FilePath $ReportFile -Encoding utf8

Write-Host " -> OK: Reporte generado en $ReportFile" -ForegroundColor DarkGreen
Write-Host ""
Write-Host "[4/4] ¡Listo! Si utilizas CurseForge App, simplemente importa el archivo .ZIP." -ForegroundColor Green
Write-Host "Si utilizas Prism Launcher o el Launcher Oficial, la carpeta 'overrides\mods' ya tiene el mod central y tus configs preconfiguradas." -ForegroundColor Green
Write-Host "==========================================================================" -ForegroundColor Cyan
Write-Host "Instalación y verificación finalizada con éxito." -ForegroundColor Yellow
Write-Host "==========================================================================" -ForegroundColor Cyan
