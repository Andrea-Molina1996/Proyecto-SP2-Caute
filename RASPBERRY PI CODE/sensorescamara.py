#Importamos las librerias necesarias
import RPi.GPIO as GPIO 
from picamera import PiCamera
from subprocess import call #Se utiliza para la interrupcion de teclado
import time
import threading
#Definimos la numeracion a usar
GPIO.setmode(GPIO.BCM)
#Instanciamos camara
camera = PiCamera()
camera.rotation = 180
#Declaran pines sensor 1
TRIG = 23
ECHO = 24
#Declaran variables globales sensor 1
BANSEN1F = False
BANSEN1A = False
#Declaran pines sensor 2
TRIG2 = 5
ECHO2 = 6
#Declaran variables globales sensor 2
BANSEN2F = False
BANSEN2A = False
print ("Distance measurement in progress")

#Se define la funcion de cada pin
GPIO.setup(TRIG,GPIO.OUT)
GPIO.setup(ECHO,GPIO.IN)
GPIO.setup(TRIG2,GPIO.OUT)
GPIO.setup(ECHO2,GPIO.IN)

#Funcion que maneja al sensor 1
def sensor1():
    global BANSEN1A
    global BANSEN1F
    pulse_start = 0
    pulse_end = 0
    GPIO.output(TRIG,False)
    time.sleep(2)

    GPIO.output(TRIG,True)
    time.sleep(0.00001)
    GPIO.output(TRIG,False)
    while GPIO.input(ECHO) == 0:
        pulse_start = time.time()
            
    while GPIO.input(ECHO) == 1:
        pulse_end = time.time()

    pulse_duration = pulse_end -pulse_start

    distance = pulse_duration * 17150
    distance = round(distance,2)
    if distance > 2 and distance < 11:
        print ("Distance Sensor derecho:", distance-0.5,"cm")
        BANSEN1A=True
        BANSEN1F=True
    elif distance > 10 and distance < 21:
        print ("Distance: Sensor derecho", distance-0.5,"cm")
        BANSEN1A=True
        BANSEN1F=False
    else:
        BANSEN1A=False
        BANSEN1F=False

#Funcion que maneja el sensor 2
def sensor2():
    global BANSEN2A
    global BANSEN2F
    pulse_start = 0
    pulse_end = 0
    GPIO.output(TRIG2,False)
    time.sleep(2)

    GPIO.output(TRIG2,True)
    time.sleep(0.00001)
    GPIO.output(TRIG2,False)
    while GPIO.input(ECHO2) == 0:
        pulse_start = time.time()
            
    while GPIO.input(ECHO2) == 1:
        pulse_end = time.time()

    pulse_duration = pulse_end -pulse_start

    distance = pulse_duration * 17150
    distance = round(distance,2)
    if distance > 2 and distance < 11:
        print ("Distance sensor izquierdo:", distance-0.5,"cm")
        BANSEN2A=True
        BANSEN2F=True
    elif distance > 10 and distance < 21:
        print ("Distance sensor izquierdo:", distance-0.5,"cm")
        BANSEN2A=True
        BANSEN2F=False
    else:
        BANSEN2A=False
        BANSEN2F=False

#Funcion que maneja la camara    
def camara():
    global BANSEN1F
    global BANSEN1A
    global BANSEN2F
    global BANSEN2A
    if BANSEN1F == True or BANSEN2F == True:
        BANSEN1F=False
        BANSEN1A=False
        BANSEN2F=False
        BANSEN2A=False
        for i in range(5):
            time.sleep(0.5)
            fecha = (time.strftime("%d-%m-%Y"))
            hora = (time.strftime("%H:%M:%S"))
            path = '/home/pi/Caute-Inc/Images/'+fecha+'_'+hora+'.jpg' #Por si acaso 'caute-%s'%i+
            print(path)
            camera.capture(path)
    elif BANSEN1A == True or BANSEN2A == True:
        BANSEN1A=False
        BANSEN2A=False
        print ("Alerta auto muy cerca :D")

#Main    
while True:
    try:
        sen1 = threading.Thread(target=sensor1)
        sen1.setDaemon(True)
        sen1.start()
        sen2 = threading.Thread(target=sensor2)
        sen2.setDaemon(True)
        sen2.start()
        imagen = threading.Thread(target=camara)
        imagen.setDaemon(True)
        imagen.start()
        sen1.join()
        sen2.join()
        imagen.join()
    except KeyboardInterrupt:
        break
#Se restablece los pines GPIO
GPIO.cleanup()
