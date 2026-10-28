Add-Type -AssemblyName System.Drawing

$themes = @(
    @{ Name = "locked_chest_overworld"; Tex = "locked_chest_overworld" },
    @{ Name = "locked_chest_nether";    Tex = "locked_chest_nether" },
    @{ Name = "locked_chest_end";       Tex = "locked_chest_end" },
    @{ Name = "locked_chest_teyvat";    Tex = "locked_chest_teyvat" },
    @{ Name = "locked_chest_isekai";    Tex = "locked_chest_isekai" },
    @{ Name = "locked_chest_murim";     Tex = "locked_chest_murim" },
    @{ Name = "locked_treasure_chest";  Tex = "locked_treasure_chest" }
)

$baseDir = "src/main/resources/assets/awakening_system"

foreach ($t in $themes) {
    $name = $t.Name
    $texName = $t.Tex
    $entPath = "$baseDir/textures/entity/chest/$texName.png"

    if (-not (Test-Path $entPath)) {
        Write-Warning "Entity texture not found: $entPath"
        continue
    }

    $entBmp = [System.Drawing.Bitmap]::FromFile((Resolve-Path $entPath).Path)

    # 1. Generate 16x16 Front composite: Lid (14x5 at y=1..5, x=1..14), Base (14x9 at y=6..14, x=1..14)
    # Plus latch (x=7..8, y=5..8) and padlock (x=6..9, y=7..11)
    $frontBmp = New-Object System.Drawing.Bitmap 16, 16
    $sideBmp  = New-Object System.Drawing.Bitmap 16, 16
    $topBmp   = New-Object System.Drawing.Bitmap 16, 16
    $botBmp   = New-Object System.Drawing.Bitmap 16, 16

    # TOP: x=14..27, y=0..13 from entity -> place at x=1..14, y=1..14
    for ($y = 0; $y -lt 14; $y++) {
        for ($x = 0; $x -lt 14; $x++) {
            $c = $entBmp.GetPixel(14 + $x, $y)
            $topBmp.SetPixel(1 + $x, 1 + $y, $c)
        }
    }
    # Pad top outer border with corner pixels
    for ($i = 0; $i -lt 16; $i++) {
        $topBmp.SetPixel($i, 0, $topBmp.GetPixel($i, 1))
        $topBmp.SetPixel($i, 15, $topBmp.GetPixel($i, 14))
        $topBmp.SetPixel(0, $i, $topBmp.GetPixel(1, $i))
        $topBmp.SetPixel(15, $i, $topBmp.GetPixel(14, $i))
    }

    # BOTTOM: x=28..41, y=19..32 -> place at x=1..14, y=1..14
    for ($y = 0; $y -lt 14; $y++) {
        for ($x = 0; $x -lt 14; $x++) {
            $c = $entBmp.GetPixel(28 + $x, 19 + $y)
            $botBmp.SetPixel(1 + $x, 1 + $y, $c)
        }
    }
    for ($i = 0; $i -lt 16; $i++) {
        $botBmp.SetPixel($i, 0, $botBmp.GetPixel($i, 1))
        $botBmp.SetPixel($i, 15, $botBmp.GetPixel($i, 14))
        $botBmp.SetPixel(0, $i, $botBmp.GetPixel(1, $i))
        $botBmp.SetPixel(15, $i, $botBmp.GetPixel(14, $i))
    }

    # FRONT: Lid front (14x5 at entity x=14..27, y=14..18) -> place at x=1..14, y=1..5
    for ($y = 0; $y -lt 5; $y++) {
        for ($x = 0; $x -lt 14; $x++) {
            $c = $entBmp.GetPixel(14 + $x, 14 + $y)
            $frontBmp.SetPixel(1 + $x, 1 + $y, $c)
        }
    }
    # Base front (14x9 at entity x=14..27, y=33..41) -> place at x=1..14, y=6..14
    for ($y = 0; $y -lt 9; $y++) {
        for ($x = 0; $x -lt 14; $x++) {
            $c = $entBmp.GetPixel(14 + $x, 33 + $y)
            $frontBmp.SetPixel(1 + $x, 6 + $y, $c)
        }
    }
    # Latch (2x4 at entity x=1..2, y=1..4) -> place at x=7..8, y=4..7
    for ($y = 0; $y -lt 4; $y++) {
        for ($x = 0; $x -lt 2; $x++) {
            $c = $entBmp.GetPixel(1 + $x, 1 + $y)
            $frontBmp.SetPixel(7 + $x, 4 + $y, $c)
        }
    }
    # Padlock (4x4 at entity x=41..44, y=45..48) -> place at x=6..9, y=8..11
    for ($y = 0; $y -lt 4; $y++) {
        for ($x = 0; $x -lt 4; $x++) {
            $c = $entBmp.GetPixel(41 + $x, 45 + $y)
            $frontBmp.SetPixel(6 + $x, 8 + $y, $c)
        }
    }
    # Padlock shackle (2x2 at entity x=41..42, y=54..55) -> place at x=7..8, y=6..7
    for ($y = 0; $y -lt 2; $y++) {
        for ($x = 0; $x -lt 2; $x++) {
            $c = $entBmp.GetPixel(41 + $x, 54 + $y)
            $frontBmp.SetPixel(7 + $x, 6 + $y, $c)
        }
    }
    for ($i = 0; $i -lt 16; $i++) {
        $frontBmp.SetPixel($i, 0, $frontBmp.GetPixel($i, 1))
        $frontBmp.SetPixel($i, 15, $frontBmp.GetPixel($i, 14))
        $frontBmp.SetPixel(0, $i, $frontBmp.GetPixel(1, $i))
        $frontBmp.SetPixel(15, $i, $frontBmp.GetPixel(14, $i))
    }

    # SIDE: Lid side (14x5 at entity x=0..13, y=14..18) -> place at x=1..14, y=1..5
    for ($y = 0; $y -lt 5; $y++) {
        for ($x = 0; $x -lt 14; $x++) {
            $c = $entBmp.GetPixel($x, 14 + $y)
            $sideBmp.SetPixel(1 + $x, 1 + $y, $c)
        }
    }
    # Base side (14x9 at entity x=0..13, y=33..41) -> place at x=1..14, y=6..14
    for ($y = 0; $y -lt 9; $y++) {
        for ($x = 0; $x -lt 14; $x++) {
            $c = $entBmp.GetPixel($x, 33 + $y)
            $sideBmp.SetPixel(1 + $x, 6 + $y, $c)
        }
    }
    for ($i = 0; $i -lt 16; $i++) {
        $sideBmp.SetPixel($i, 0, $sideBmp.GetPixel($i, 1))
        $sideBmp.SetPixel($i, 15, $sideBmp.GetPixel($i, 14))
        $sideBmp.SetPixel(0, $i, $sideBmp.GetPixel(1, $i))
        $sideBmp.SetPixel(15, $i, $sideBmp.GetPixel(14, $i))
    }

    # Save face textures
    $prefix = if ($name -eq "locked_treasure_chest") { "locked_chest" } else { $name }
    $frontBmp.Save("$baseDir/textures/block/${prefix}_front.png", [System.Drawing.Imaging.ImageFormat]::Png)
    $sideBmp.Save("$baseDir/textures/block/${prefix}_side.png", [System.Drawing.Imaging.ImageFormat]::Png)
    $topBmp.Save("$baseDir/textures/block/${prefix}_top.png", [System.Drawing.Imaging.ImageFormat]::Png)
    $botBmp.Save("$baseDir/textures/block/${prefix}_bottom.png", [System.Drawing.Imaging.ImageFormat]::Png)

    if ($name -eq "locked_treasure_chest") {
        # Also save with locked_treasure_chest prefix if referenced
        $frontBmp.Save("$baseDir/textures/block/locked_treasure_chest_front.png", [System.Drawing.Imaging.ImageFormat]::Png)
        $sideBmp.Save("$baseDir/textures/block/locked_treasure_chest_side.png", [System.Drawing.Imaging.ImageFormat]::Png)
        $topBmp.Save("$baseDir/textures/block/locked_treasure_chest_top.png", [System.Drawing.Imaging.ImageFormat]::Png)
        $botBmp.Save("$baseDir/textures/block/locked_treasure_chest_bottom.png", [System.Drawing.Imaging.ImageFormat]::Png)
    }

    $frontBmp.Dispose(); $sideBmp.Dispose(); $topBmp.Dispose(); $botBmp.Dispose()
    $entBmp.Dispose()
    Write-Host "Generated block textures for $name from entity texture!"
}
