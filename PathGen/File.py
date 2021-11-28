import math
import Convert
import Point
import glob
import json

def getSave(fileName, fieldWidth, fieldHeight):
    saves = glob.glob("json-paths/*.json*")
    for save in saves:
        if fileName in save:
            with open(save) as jsonSave:
                data = json.load(jsonSave)
                newPoints = []
                for x in range(len(data)):
                    newPoints.append(Point.Point(data[x]["x"], data[x]["y"], fieldWidth, fieldHeight, data[x]["angle"], data[x]["speed"], data[x]["time"], data[x]["color"]))
                for x in range(len(newPoints)):
                    newPoints[x].index = x
            Point.setPoints(newPoints)
            return save
    return None