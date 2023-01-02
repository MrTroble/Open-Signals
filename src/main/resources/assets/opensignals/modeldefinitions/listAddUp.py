import json
import os

lst = []

for x in os.listdir("."):
    if not x.endswith(".extention.json") and not x == "list.json" and x.endswith(".json"):
        lst.append(x.replace(".json", ""))

with open("list.json", "w") as fp:
    json.dump(lst, fp)