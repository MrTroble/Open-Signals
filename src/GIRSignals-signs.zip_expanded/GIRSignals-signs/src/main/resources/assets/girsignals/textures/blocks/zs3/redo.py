import os
import cv2

for file in os.listdir("."):
    if file.endswith(".png"):
        mat = cv2.imread(file, cv2.IMREAD_UNCHANGED)
        for x in mat:
            for y in x:
                if y[3] != 0 and y[0] < 40 and y[1] < 40 and y[2] < 40:
                    y[3] = 0
                if y[3] != 0:
                    print(y)

        print(file)
        cv2.imwrite("n" + file, mat)  
