import pygame
import Draw
import Point
pygame.init()


screenWidth = 1000
screenHeight = 500


selectedPoint = None

backgroundImg = pygame.image.load("Images/field2.png")
newScreenWidth = ((screenHeight / int(backgroundImg.get_height())) * backgroundImg.get_width())

fieldWidth = newScreenWidth
fieldHeight = screenHeight

backgroundImg = pygame.transform.scale(backgroundImg, (newScreenWidth, screenHeight))

font = pygame.font.SysFont("Corbel", 24)

screen = pygame.display.set_mode([screenWidth, screenHeight]) 
pygame.display.set_caption("PathGen")

running = True
while running:

    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            running = False
        if event.type == pygame.MOUSEBUTTONUP:
            selectedPoint = Point.clicked(Draw.getFieldPos(pygame.mouse.get_pos(), fieldWidth, fieldHeight), fieldWidth, fieldHeight, pygame, selectedPoint)
            
    ############

    Draw.drawField(backgroundImg, screen, pygame, screenWidth, screenHeight)
    Draw.drawPoints(pygame, screen, Point.getPoints())
    Draw.drawMouseCoords(pygame, screen, font, fieldWidth, fieldHeight)
    if selectedPoint != None:
        Draw.drawPointInfo(pygame, screen, font, selectedPoint)

    ############
    pygame.display.flip()

pygame.quit()