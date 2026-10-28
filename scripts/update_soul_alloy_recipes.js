const fs = require('fs');

const recipeFiles = [
    'soul_alloy_sword.json',
    'soul_alloy_pickaxe.json',
    'soul_alloy_axe.json',
    'soul_alloy_shovel.json',
    'soul_alloy_hoe.json',
    'soul_alloy_helmet.json',
    'soul_alloy_chestplate.json',
    'soul_alloy_leggings.json',
    'soul_alloy_boots.json',
    'soul_alloy_furnace.json',
    'upgrade_abyssal_to_soul_alloy.json'
];

const dir = 'src/main/resources/data/awakening_system/recipe/';

recipeFiles.forEach(file => {
    const fullPath = dir + file;
    if (fs.existsSync(fullPath)) {
        let content = fs.readFileSync(fullPath, 'utf8');
        content = content.replace(/"awakening_system:soul_alloy_ingot"/g, '"awakening_system:ignited_soul_alloy_ingot"');
        fs.writeFileSync(fullPath, content, 'utf8');
        console.log(`Updated ${file} to use ignited_soul_alloy_ingot.`);
    } else {
        console.warn(`File not found: ${file}`);
    }
});
