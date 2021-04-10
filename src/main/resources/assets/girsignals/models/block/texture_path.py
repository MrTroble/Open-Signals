import os
import json
import time

start_time = time.time()

path = input("Wo liegen die Dateien? [./] >")
if not path:
    path = "./"

for i in os.listdir(path):
    if not i.endswith(".json"):
        pass
    else:
        try:
            with open(path + """/""" + i, "r") as json_file:
                structure = json.load(json_file)
                newstructure = structure.copy()
        except json.JSONDecodeError:
                pass
        for key, value in structure["textures"].items():
            name = value.split("/")[1] + ".png"
            for root, dirs, files in os.walk("D:\\Nutzer\\Documents\\Spiele\\Minecraft\\programmieren\\GIRSignals\\src\\main\\resources\\assets\\girsignals\\textures\\blocks"):
                if name in files:
                    file_path = os.path.join(root, name).split("textures\\")
                    file_path[1] = file_path[1].replace("""\\""", """/""")
                    newstructure["textures"][key] = "girsignals:" + file_path[1].replace(".png", "")
        json.dump(newstructure, open(path + """/""" + i, "w"), indent=4)

end_time = time.time()
print(f"Done, took {end_time - start_time} seconds.")