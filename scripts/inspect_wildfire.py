import zipfile
import os

zip_path = r"C:\Users\soybe\Downloads\friends-and-foes-master.zip"
if not os.path.exists(zip_path):
    print("Zip file does not exist:", zip_path)
    exit(1)

with zipfile.ZipFile(zip_path, 'r') as z:
    wildfire_files = [f for f in z.namelist() if 'wildfire' in f.lower()]
    for f in sorted(wildfire_files):
        print(f)
