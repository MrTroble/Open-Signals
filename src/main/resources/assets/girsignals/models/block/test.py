import json

filename = input()
ids = input().split(",")
jstree = json.load(open(filename))
textures = jstree["textures"]
for element in jstree["elements"]:
    name = element["name"]
    for fname, face in element["faces"].items():
        idname = face['texture'][1:]
        if not idname in ids: continue
        ctext = textures[idname]
        nname = name + fname
        face['texture'] = nname
        textures[nname] = ctext

json.dump(jstree, open(filename, "w"), indent=2)