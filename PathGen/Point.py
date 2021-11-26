import math
import Draw
import Convert

points = []

class Point:
    def __init__(self, x, y, fieldWidth, fieldHeight):
        self.x = x
        self.y = y
        self.pixelX = (x / ((1600 / fieldWidth) * (15.98295 / 1057)) ) + 218 * (fieldWidth / 1600)
        self.pixelY = -1 *(y / ((900 / fieldHeight) * (15.98295 / 1057)) ) + 759 * (fieldHeight / 900)
        self.angle = 0.0
        #angle is stored in radians
        self.speed = 0.0
        self.time = 0.0
        self.color = (255, 0, 0)
        self.index = len(points)

    def updatePixelPos(self, fieldWidth, fieldHeight):
        self.pixelX = (self.x / ((1600 / fieldWidth) * (15.98295 / 1057)) ) + 218 * (fieldWidth / 1600)
        self.pixelY = -1 *(self.y / ((900 / fieldHeight) * (15.98295 / 1057)) ) + 759 * (fieldHeight / 900)
    
    

def clicked(mousePos, fieldWidth, fieldHeight, pygame, selectedPoint, imgWidth):
    mousePixelPos = list(pygame.mouse.get_pos())
    mouseIsOnPoint = False
    for point in points:
        if Convert.getPixelDist(mousePixelPos[0], mousePixelPos[1], point.pixelX, point.pixelY) < 4:
            point.color = (0, 0, 255)
            if selectedPoint != None:
                if point.index != selectedPoint.index:
                    points[selectedPoint.index].color = (255, 0, 0)
            selectedPoint = point
            mouseIsOnPoint = True
    if not mouseIsOnPoint and pygame.mouse.get_pos()[0] < imgWidth:
        points.append( Point(mousePos[0], mousePos[1], fieldWidth, fieldHeight) )
    return selectedPoint

def saveSelectedPoint(selectedPoint, fieldWidth, fieldHeight):
    while selectedPoint.angle > 360:
        selectedPoint.angle -= 360
    points[selectedPoint.index] = selectedPoint
    points[selectedPoint.index].updatePixelPos(fieldWidth, fieldHeight)

def getPoints():
    return points

def deletePoint(index):
    del points[index]
    for x in range(len(points)):
        points[x].index = x