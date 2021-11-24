import math
import Draw

points = []

class Point:
    def __init__(self, x, y, fieldWidth, fieldHeight):
        self.x = x
        self.y = y
        self.pixelX = (x / ((1600 / fieldWidth) * (52.4375 / 1057)) ) + 218 * (fieldWidth / 1600)
        self.pixelY = -1 *(y / ((900 / fieldHeight) * (52.4375 / 1057)) ) + 759 * (fieldHeight / 900)
        self.angle = 0.0
        #angle is stored in degree
        self.speed = 0.0
        self.time = 0.0
        self.color = (255, 0, 0)
        self.index = len(points)
    
    

def clicked(mousePos, fieldWidth, fieldHeight, pygame, selectedPoint):
    mousePixelPos = list(pygame.mouse.get_pos())
    mouseIsOnPoint = False
    for point in points:
        if Draw.getPixelDist(mousePixelPos[0], mousePixelPos[1], point.pixelX, point.pixelY) < 3:
            point.color = (0, 255, 0)
            if selectedPoint != None:
                if point.index != selectedPoint.index:
                    points[selectedPoint.index].color = (255, 0, 0)
            selectedPoint = point
            mouseIsOnPoint = True
    if not mouseIsOnPoint and mousePos[0] < 68.5:
        points.append( Point(mousePos[0], mousePos[1], fieldWidth, fieldHeight) )
    return selectedPoint

def getPoints():
    return points
