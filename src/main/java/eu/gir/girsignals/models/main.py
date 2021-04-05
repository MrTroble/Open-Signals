import os
import time
start_time = time.time()

with open("""./GIRCustomModelLoader.java""") as java_file:
    for line in java_file:
        if "girsignals:blocks/" in line:
            content = line.split()
            print(content)

end_time = time.time()
print(f"Done, took {end_time - start_time} seconds.")