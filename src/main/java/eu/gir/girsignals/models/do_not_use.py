import os
import time
import re

java_path = input("Wo liegt die Java-Datei (mit Dateiendung)[./GIRCustomModelLoader.java]>")
#Setting standard path
if not java_path:
    java_path = "./GIRCustomModelLoader.java"
new_models = input("Wo liegen die neuen Models? (benötigt)>")

start_time = time.time()

with open(java_path, "r+") as java_file:
    #Filtering image names
    java_content = java_file.read()
    images = list(dict.fromkeys(re.findall("\"girsignals:blocks/(\w*)", java_content)))
    newpaths = {}
    #Searching file
    for file_name in images:
        modelname = file_name + ".png"
        for root, dirs, files in os.walk(new_models):
            if modelname in files:
                #Cutting at textures to get the "blocks/..."
                file_path = os.path.join(root, file_name).split("blocks\\")
                #fixing the slashes
                file_path[1] = file_path[1].replace("""\\""", """/""")
                #Adding <Old image name> : <New image path (blocks/...)> to dict
                newpaths[file_name] = file_path[1]
    for key, value in newpaths.items():
        java_content = java_content.replace(key, value)
    java_file.seek(0)
    java_file.write(java_content)

print(f"Done, took {time.time() - start_time} seconds.")