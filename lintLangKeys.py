import os
import re
import sys

dirOfLanguageSets = {}

pathToLangs = "src/main/resources/assets/girsignals/lang"
for file in os.listdir(pathToLangs):
    languageSet = []
    with open(os.path.join(pathToLangs, file)) as fp:
        for line in fp:
            if len(line) > 2:
                languageSet.append(line.split("=")[0])
    dirOfLanguageSets[file] = languageSet

pattern = re.compile("I18n\.format\(\"([\w\s\.]+)\"\)")

foundAny = False

for root, dirs, files  in os.walk("src/main/java"):
    for file in files:
        with open(os.path.join(root, file)) as fp:
            content = str(fp.read())
            for key in re.findall(pattern, content):
                for name, set in dirOfLanguageSets.items():
                    if not key in set:
                        foundAny = True
                        print("Key", key, "not found in", name)

if foundAny:
    sys.exit("One or more keys not found!")
