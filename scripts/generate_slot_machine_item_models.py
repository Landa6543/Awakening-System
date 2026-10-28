import json

def create_item_model(tex_name):
    # Centered in Java space [0..16]:
    # Width (X): 14 -> [1.0, 15.0]
    # Depth (Z): 15 -> [0.5, 15.5]
    # Height (Y): 23 -> [0.0, 23.0]
    # In item models, elements cannot have multiple rotations, so we keep the main cabinet boxes:
    # 1. Base: [1.0, 0.0, 0.5] to [15.0, 8.0, 15.5]
    # 2. Tray: [4.0, 2.0, 0.0] to [12.0, 6.0, 0.5]
    # 3. Middle Body: [1.0, 8.0, 3.5] to [15.0, 18.5, 15.5]
    # 4. Button: [2.0, 8.0, 1.5] to [3.5, 9.0, 3.0]
    # 5. Marquee: [1.0, 18.5, 1.0] to [15.0, 23.0, 11.0]
    # 6. Lever mount: [15.0, 6.0, 4.5] to [17.0, 10.5, 9.0]
    # 7. Lever arm: [16.0, 8.0, 6.0] to [17.0, 14.0, 7.0]
    # 8. Lever knob: [15.5, 13.5, 5.5] to [17.5, 15.5, 7.5]
    # 9. Reels / Screen glass: [2.5, 9.5, 3.0] to [13.5, 17.5, 3.5]
    
    elements = [
        {
            "name": "base",
            "from": [1.0, 0.0, 0.5],
            "to": [15.0, 8.0, 15.5],
            "faces": {
                "north": {"uv": [3.75, 5.25, 7.5, 7.25], "texture": "#texture"},
                "south": {"uv": [3.75, 5.25, 7.5, 7.25], "texture": "#texture"},
                "east":  {"uv": [7.5, 5.25, 11.0, 7.25], "texture": "#texture"},
                "west":  {"uv": [7.5, 5.25, 11.0, 7.25], "texture": "#texture"},
                "up":    {"uv": [3.75, 3.5, 7.5, 7.0], "texture": "#texture"},
                "down":  {"uv": [3.75, 3.5, 7.5, 7.0], "texture": "#texture"}
            }
        },
        {
            "name": "tray",
            "from": [4.0, 2.0, 0.0],
            "to": [12.0, 6.0, 0.5],
            "faces": {
                "north": {"uv": [13.25, 0.125, 15.25, 1.125], "texture": "#texture"},
                "up":    {"uv": [13.25, 0.0, 15.25, 0.125], "texture": "#texture"},
                "down":  {"uv": [13.25, 0.0, 15.25, 0.125], "texture": "#texture"}
            }
        },
        {
            "name": "middle_body",
            "from": [1.0, 8.0, 3.5],
            "to": [15.0, 18.5, 15.5],
            "faces": {
                "north": {"uv": [3.75, 2.625, 6.75, 5.25], "texture": "#texture"},
                "south": {"uv": [3.75, 2.625, 6.75, 5.25], "texture": "#texture"},
                "east":  {"uv": [3.75, 0.0, 7.25, 2.625], "texture": "#texture"},
                "west":  {"uv": [7.25, 0.0, 10.75, 2.625], "texture": "#texture"},
                "up":    {"uv": [3.75, 0.0, 7.25, 2.625], "texture": "#texture"}
            }
        },
        {
            "name": "screen",
            "from": [2.5, 9.5, 3.0],
            "to": [13.5, 17.5, 3.5],
            "faces": {
                "north": {"uv": [6.75, 3.625, 10.0, 5.125], "texture": "#texture"}
            }
        },
        {
            "name": "spin_button",
            "from": [2.0, 8.0, 1.5],
            "to": [3.5, 9.0, 3.0],
            "faces": {
                "north": {"uv": [13.75, 2.75, 14.25, 3.0], "texture": "#texture"},
                "up":    {"uv": [13.75, 2.75, 14.25, 3.0], "texture": "#texture"}
            }
        },
        {
            "name": "marquee",
            "from": [1.0, 18.5, 1.0],
            "to": [15.0, 23.0, 11.0],
            "faces": {
                "north": {"uv": [10.75, 0.0, 13.25, 1.125], "texture": "#texture"},
                "south": {"uv": [10.75, 0.0, 13.25, 1.125], "texture": "#texture"},
                "east":  {"uv": [10.75, 1.125, 14.25, 2.25], "texture": "#texture"},
                "west":  {"uv": [10.75, 1.125, 14.25, 2.25], "texture": "#texture"},
                "up":    {"uv": [2.5, 3.5, 0.0, 0.0], "texture": "#texture"}
            }
        },
        {
            "name": "lever_mount",
            "from": [15.0, 6.0, 4.5],
            "to": [17.0, 10.5, 9.0],
            "faces": {
                "east":  {"uv": [14.25, 1.625, 14.75, 2.75], "texture": "#texture"},
                "north": {"uv": [14.75, 1.625, 15.875, 2.75], "texture": "#texture"},
                "south": {"uv": [14.75, 1.625, 15.875, 2.75], "texture": "#texture"},
                "up":    {"uv": [14.25, 1.125, 15.375, 1.625], "texture": "#texture"}
            }
        },
        {
            "name": "lever_arm",
            "from": [16.0, 8.0, 6.0],
            "to": [17.0, 14.0, 7.0],
            "faces": {
                "north": {"uv": [2.625, 0.25, 2.875, 1.0], "texture": "#texture"},
                "south": {"uv": [2.625, 0.25, 2.875, 1.0], "texture": "#texture"},
                "east":  {"uv": [2.625, 0.25, 2.875, 1.0], "texture": "#texture"},
                "west":  {"uv": [2.625, 0.25, 2.875, 1.0], "texture": "#texture"},
                "up":    {"uv": [2.625, 0.25, 2.875, 1.0], "texture": "#texture"}
            }
        },
        {
            "name": "lever_knob",
            "from": [15.5, 13.5, 5.5],
            "to": [17.5, 15.5, 7.5],
            "faces": {
                "north": {"uv": [13.375, 2.625, 13.75, 3.0], "texture": "#texture"},
                "south": {"uv": [13.375, 2.625, 13.75, 3.0], "texture": "#texture"},
                "east":  {"uv": [13.375, 2.625, 13.75, 3.0], "texture": "#texture"},
                "west":  {"uv": [13.375, 2.625, 13.75, 3.0], "texture": "#texture"},
                "up":    {"uv": [13.375, 2.25, 13.75, 2.625], "texture": "#texture"},
                "down":  {"uv": [13.375, 2.25, 13.75, 2.625], "texture": "#texture"}
            }
        }
    ]

    display = {
        "gui": {
            "rotation": [30, -135, 0],
            "translation": [0, -3.0, 0],
            "scale": [0.48, 0.48, 0.48]
        },
        "ground": {
            "rotation": [0, 0, 0],
            "translation": [0, 2, 0],
            "scale": [0.35, 0.35, 0.35]
        },
        "thirdperson_righthand": {
            "rotation": [75, 45, 0],
            "translation": [0, 2.5, 0],
            "scale": [0.28, 0.28, 0.28]
        },
        "firstperson_righthand": {
            "rotation": [0, -135, 0],
            "translation": [0, 0, 0],
            "scale": [0.38, 0.38, 0.38]
        },
        "fixed": {
            "rotation": [0, 180, 0],
            "translation": [0, -2, 0],
            "scale": [0.45, 0.45, 0.45]
        }
    }

    return {
        "credit": "Awakening System - 3D Arcade Casino Slot Machine",
        "textures": {
            "texture": f"awakening_system:entity/{tex_name}",
            "particle": f"awakening_system:entity/{tex_name}"
        },
        "elements": elements,
        "display": display
    }

with open(r'src/main/resources/assets/awakening_system/models/item/slot_machine.json', 'w', encoding='utf-8') as f:
    json.dump(create_item_model('slot_machine'), f, indent=2)

with open(r'src/main/resources/assets/awakening_system/models/item/illegal_slot_machine.json', 'w', encoding='utf-8') as f:
    json.dump(create_item_model('illegal_slot_machine'), f, indent=2)

print('Updated slot_machine and illegal_slot_machine item models!')
