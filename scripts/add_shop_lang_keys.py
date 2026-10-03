import json, os, glob

keys_en = {
    "item.awakening_system.crystal_chunk": "Crystal Chunk",
    "item.awakening_system.magical_crystal_chunk": "Magical Crystal Chunk",
    "item.awakening_system.starsilver_chunk": "Starsilver Chunk",
    "awakening_system.shop.item.create_wrench": "Wrench (Create)",
    "awakening_system.shop.item.create_goggles": "Engineer's Goggles (Create)",
    "awakening_system.shop.item.create_brass_ingot": "Brass Ingot (Create)",
    "awakening_system.shop.item.create_zinc_ingot": "Zinc Ingot (Create)",
    "awakening_system.shop.item.create_copper_casing": "Copper Casing (Create)",
    "awakening_system.shop.item.create_andesite_casing": "Andesite Casing (Create)",
    "awakening_system.shop.item.create_shaft": "Shaft (Create)",
    "awakening_system.shop.item.create_cogwheel": "Cogwheel (Create)",
    "awakening_system.shop.item.create_large_cogwheel": "Large Cogwheel (Create)",
    "awakening_system.shop.item.create_gearbox": "Gearbox (Create)",
    "awakening_system.shop.item.create_belt_connector": "Mechanical Belt (Create)",
    "awakening_system.shop.item.create_chute": "Chute (Create)",
    "awakening_system.shop.item.create_water_wheel": "Water Wheel (Create)",
    "awakening_system.shop.item.create_large_water_wheel": "Large Water Wheel (Create)",
    "awakening_system.shop.item.create_windmill_bearing": "Windmill Bearing (Create)",
    "awakening_system.shop.item.create_mechanical_press": "Mechanical Press (Create)",
    "awakening_system.shop.item.create_mechanical_mixer": "Mechanical Mixer (Create)",
    "awakening_system.shop.item.create_basin": "Basin (Create)",
    "awakening_system.shop.item.create_mechanical_drill": "Mechanical Drill (Create)",
    "awakening_system.shop.item.create_mechanical_saw": "Mechanical Saw (Create)",
    "awakening_system.shop.item.create_deployer": "Deployer (Create)",
    "awakening_system.shop.item.create_crushing_wheel": "Crushing Wheel (Create)",
    "awakening_system.shop.item.create_blaze_burner": "Blaze Burner (Create)",
    "awakening_system.shop.item.create_blaze_cake": "Blaze Cake (Create)",
    "awakening_system.shop.item.create_electron_tube": "Electron Tube (Create)",
    "awakening_system.shop.item.create_fluid_pipe": "Fluid Pipe (Create)",
    "awakening_system.shop.item.create_mechanical_pump": "Mechanical Pump (Create)",
    "awakening_system.shop.item.create_fluid_tank": "Fluid Tank (Create)",
    "awakening_system.shop.item.create_spout": "Spout (Create)",
    "awakening_system.shop.item.create_item_vault": "Item Vault (Create)",
    "awakening_system.shop.item.create_schematicannon": "Schematicannon (Create)"
}

keys_es = {
    "item.awakening_system.crystal_chunk": "Pedazo de Cristal",
    "item.awakening_system.magical_crystal_chunk": "Trozo de Cristal Mágico",
    "item.awakening_system.starsilver_chunk": "Trozo de Argénteo",
    "awakening_system.shop.item.create_wrench": "Llave Inglesa (Create)",
    "awakening_system.shop.item.create_goggles": "Gafas de Ingeniero (Create)",
    "awakening_system.shop.item.create_brass_ingot": "Lingote de Latón (Create)",
    "awakening_system.shop.item.create_zinc_ingot": "Lingote de Zinc (Create)",
    "awakening_system.shop.item.create_copper_casing": "Revestimiento de Cobre (Create)",
    "awakening_system.shop.item.create_andesite_casing": "Revestimiento de Andesita (Create)",
    "awakening_system.shop.item.create_shaft": "Eje Rotatorio (Create)",
    "awakening_system.shop.item.create_cogwheel": "Rueda Dentada (Create)",
    "awakening_system.shop.item.create_large_cogwheel": "Rueda Dentada Grande (Create)",
    "awakening_system.shop.item.create_gearbox": "Caja de Engranajes (Create)",
    "awakening_system.shop.item.create_belt_connector": "Correa de Transmisión (Create)",
    "awakening_system.shop.item.create_chute": "Conducto de Caída (Create)",
    "awakening_system.shop.item.create_water_wheel": "Rueda Hidráulica (Create)",
    "awakening_system.shop.item.create_large_water_wheel": "Rueda Hidráulica Grande (Create)",
    "awakening_system.shop.item.create_windmill_bearing": "Cojinete de Molino (Create)",
    "awakening_system.shop.item.create_mechanical_press": "Prensa Mecánica (Create)",
    "awakening_system.shop.item.create_mechanical_mixer": "Mezclador Mecánico (Create)",
    "awakening_system.shop.item.create_basin": "Cuenco Mecánico (Create)",
    "awakening_system.shop.item.create_mechanical_drill": "Taladro Mecánico (Create)",
    "awakening_system.shop.item.create_mechanical_saw": "Sierra Mecánica (Create)",
    "awakening_system.shop.item.create_deployer": "Desplegador Mecánico (Create)",
    "awakening_system.shop.item.create_crushing_wheel": "Rueda Trituradora (Create)",
    "awakening_system.shop.item.create_blaze_burner": "Quemador de Blaze (Create)",
    "awakening_system.shop.item.create_blaze_cake": "Pastel de Blaze (Create)",
    "awakening_system.shop.item.create_electron_tube": "Tubo de Electrones (Create)",
    "awakening_system.shop.item.create_fluid_pipe": "Tubería de Fluidos (Create)",
    "awakening_system.shop.item.create_mechanical_pump": "Bomba Mecánica (Create)",
    "awakening_system.shop.item.create_fluid_tank": "Tanque de Fluidos (Create)",
    "awakening_system.shop.item.create_spout": "Boquilla Dispensadora (Create)",
    "awakening_system.shop.item.create_item_vault": "Bóveda de Objetos (Create)",
    "awakening_system.shop.item.create_schematicannon": "Cañón de Esquemas (Create)"
}

# Update en_us.json
en_path = 'src/main/resources/assets/awakening_system/lang/en_us.json'
with open(en_path, 'r', encoding='utf-8') as f:
    en_data = json.load(f)
en_data.update(keys_en)
with open(en_path, 'w', encoding='utf-8') as f:
    json.dump(en_data, f, ensure_ascii=False, indent=2)

# Update all es_*.json files
for es_path in glob.glob('src/main/resources/assets/awakening_system/lang/es_*.json'):
    with open(es_path, 'r', encoding='utf-8') as f:
        es_data = json.load(f)
    es_data.update(keys_es)
    with open(es_path, 'w', encoding='utf-8') as f:
        json.dump(es_data, f, ensure_ascii=False, indent=2)

print("Updated en_us and all es_* lang files successfully.")
