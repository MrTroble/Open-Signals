import json

with open("list.json") as fp:
    listOfTypes = json.load(fp)
    for type in listOfTypes:
        jelement = {}
        jelement["loader"] = "opensignals:signalloader"
        jelement["model"] = type
        with open(type + ".json", "w") as fp:
            json.dump(jelement, fp)