import re
import os

builderMatch = re.compile(r"builder\s*([^;]+);")
seprops = re.compile(r"SEProperty\.of\s*\((\s*[^\)]*\s*)\)")
builderInternMatcher = re.compile(r"^\([^\.]*\.(?P<TOOL>[^\,]+)\s*,\s*\"(?P<NAME>[^\"]+)\"\s*\)")
bld = re.compile(r"\.(?P<KEY>[^\(]*)\s*\(\s*(?P<VALUE>[^\)]*)\s*\)")
params = re.compile(r"\s*(\(|,)?\s*\"?([^\",]+)\"?\s*(\);)?")
checker = re.compile(r"check\([^,]*\s*,\s*([^\)]*)\)?")

translationTable = {
    "height": "defaultHeight",
    "signHeight": "customNameRenderHeight"
}

def builderParser(build):
    settings = {}
    initialMatch = re.findall(builderInternMatcher, build)
    if len(initialMatch) == 0: return settings
    settings["placementToolName"] = initialMatch[0][0]
    settings["signalTypeName"] = initialMatch[0][1]
    settings["canLink"] = True
    next = re.findall(bld, "." + build.split(").", 1)[1])
    for key, value in next:
        key = key.strip()
        value = value.strip()
        if key == "noLink":
            settings["canLink"] = False
            continue
        if key == "config" or key == "build": continue
        if key in translationTable:
            key = translationTable[key]
        if value.endswith("f"):
            value = float(value[:-1])
        else:
            try:
                value = int(value)
            except:
                pass
        settings[key] = value
    return settings

def propParser(content):
    matches = re.findall(seprops, content)
    lop = []
    for proper in matches:
        tpl = re.findall(params, proper)
        prop = {}
        prop["name"] = tpl[0][1]
        splitNames = tpl[1][1].split(".")
        if len(splitNames) > 1:
            prop["enumClass"] = splitNames[0]
            prop["defaultState"] = splitNames[1]
        else:
            prop["defaultState"] = bool(tpl[1][1])
        if len(tpl) > 2:
            prop["changeableStage"] = tpl[2][1].split(".")[1]
        if len(tpl) > 3:
            prop["autoname"] = bool(tpl[3][1])
        checksFound = re.findall(checker, proper)
        if len(checksFound) > 0:
            prop["dependencies"] = "check(" + checksFound[0] + ")"
        lop.append(prop)

outputdir = "../../../../../resources/assets/girsignals/signalsystems"

for root, dirs, files in os.walk("."):
    for file in files:
        fpath = os.path.join(root, file)
        if not fpath.endswith(".java"): continue
        with open(fpath) as fp:
            content = fp.read()
            builder = re.findall(builderMatch, content)
            print(fpath)
            if len(builder) == 0:
                continue
            jsonOut = {}
            settings = builderParser(builder[0])
            jsonOut["systemProperties"] = settings
            jsonOut["seProperties"] = propParser(content)
            print(jsonOut)
            


