import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

with open('src/main/java/com/soybe/awakeningsystem/SystemData.java', 'r', encoding='utf-8') as f:
    text = f.read()

ser_start = text.find('public CompoundTag serializeNBT')
deser_start = text.find('public void deserializeNBT')

ser_text = text[ser_start:deser_start]
deser_text = text[deser_start:]

ser_keys = set(re.findall(r'tag\.put\w*\(\s*"([^"]+)"', ser_text))
deser_keys = set(re.findall(r'tag\.get\w*\(\s*"([^"]+)"', deser_text))
deser_keys.update(re.findall(r'tag\.contains\(\s*"([^"]+)"', deser_text))

# Filter prefixes like equipped_active_ / equipped_passive_
ser_keys = set(k for k in ser_keys if not k.startswith("equipped_active_") and not k.startswith("equipped_passive_"))
deser_keys = set(k for k in deser_keys if not k.startswith("equipped_active_") and not k.startswith("equipped_passive_"))

print(f"Keys serialized: {len(ser_keys)}")
print(f"Keys deserialized: {len(deser_keys)}")

missing_in_deser = ser_keys - deser_keys
missing_in_ser = deser_keys - ser_keys

print(f"Missing in deserializeNBT ({len(missing_in_deser)}): {missing_in_deser}")
print(f"Missing in serializeNBT ({len(missing_in_ser)}): {missing_in_ser}")
