import math
import Point

def drawField(field, screen, pygame, screenWidth, screenHeight):
    pygame.draw.rect(screen, (0, 0, 0), (0, 0, screenWidth, screenHeight))
    screen.blit(field, (0, 0))

def drawPoints(pygame, screen, points):
    mousePosPixels = list(pygame.mouse.get_pos())

    if len(points) > 0:
        prevX = points[0].pixelX
        prevY = points[0].pixelY

    for point in points:
        radius = 3
        if getPixelDist(mousePosPixels[0], mousePosPixels[1], point.pixelX, point.pixelY) < 3 or point.color == (0, 255, 0):
            radius = 5
        pygame.draw.line(screen, (255, 0, 0), (prevX, prevY), (point.pixelX, point.pixelY))
        prevX = point.pixelX
        prevY = point.pixelY
        pygame.draw.circle(screen, point.color, (point.pixelX, point.pixelY), radius, 0)

def drawMouseCoords(pygame, screen, font, fieldWidth, fieldHeight):
    mouseCoords = font.render("#Points: " + str(len(Point.getPoints())) + "  Pos: " + str( getFieldPos(pygame.mouse.get_pos(), fieldWidth, fieldHeight)), True, (255, 255, 255) )
    screen.blit(mouseCoords, (20, 20))

def drawPointInfo(pygame, screen, font, point):
    pointAngle = font.render("Angle: " + str(point.angle), True, (255, 255, 255))
    pointSpeed = font.render("Speed: " + str(point.speed), True, (255, 255, 255))
    pointTime = font.render("Time: " + str(point.time), True, (255, 255, 255))
    pointIndex = font.render("Index: " + str(point.index), True, (255, 255, 255))
    screen.blit(pointAngle, (890, 10))
    screen.blit(pointSpeed, (890, 110))
    screen.blit(pointTime, (890, 150))
    screen.blit(pointIndex, (890, 190))

    pygame.draw.circle(screen, (255, 255, 0), (945, 70), 35, 0)
    pygame.draw.line(screen, (255, 0, 0), (945, 70), (980, 70))
    pygame.draw.circle(screen, (0, 0, 0), (945, 70), 5, 0)

    angleInRadians = point.angle * (3.14159265 / 180)

    pygame.draw.line(screen, (0, 255, 0), (945, 70), (35 * math.cos(angleInRadians) + 945, 35 * math.sin(angleInRadians) + 70))

def getFieldPos(mousePos, fieldWidth, fieldHeight):
    mousePos = list(mousePos)

    mousePos[0] -= 218 * (fieldWidth / 1600)
    mousePos[1] = -1 * (mousePos[1] - (759 * (fieldHeight / 900)))

    mousePos[0] = math.floor((mousePos[0] * (1600 / fieldWidth)) * (52.4375 / 1057) * 100) / 100
    mousePos[1] = math.floor((mousePos[1] * (900 / fieldHeight)) * (52.4375 / 1057) * 100) / 100

    return mousePos

def getPixelDist(x1, y1, x2, y2):
    return math.sqrt( ( (x1 - x2) * (x1 - x2) ) + ( (y1 - y2) * (y1 - y2) ) )