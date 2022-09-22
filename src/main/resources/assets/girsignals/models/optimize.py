import os
import os.path
import json

credit = "Made with Blockbench by Mc_Jeronimo"


def checkTextures(jobj):
    lst = []
    if "elements" not in jobj:
        return jobj
    for element in jobj["elements"]:
        for na, face in element["faces"].items():
            texture = face["texture"]
            if not texture.startswith("#"):
                texture = face["texture"] = "#" + texture
            lst.append(texture[1:])
    nset = set(lst)
    texList = jobj["textures"]
    popList = []
    for tex in texList:
        if tex not in nset and tex != "particle":
            popList.append(tex)
    for tex in popList:
        texList.pop(tex)
    jobj["textures"] = texList
    for tex in nset:
        if tex not in texList:
            print("Warning texture " + tex + " not defined")
    return jobj
            

for root, dirs, files in os.walk("."):
    for name in files:
        pth = os.path.join(root, name)
        if not pth.endswith(".json"):
            continue
        with open(pth) as fp:
            jobj = json.load(fp)
            print(pth)
            jobj["credit"] = credit
            jobj = checkTextures(jobj)
            fp.close()
            fp = open(pth, "w")
            json.dump(jobj, fp, indent=None)
            fp.close()

