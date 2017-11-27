#Importamos las librerias necesarias
import RPi.GPIO as GPIO 
from picamera import PiCamera
from subprocess import call #Se utiliza para la interrupcion de teclado
import time
import threading
import json 
#Definimos la numeracion a usar
GPIO.setmode(GPIO.BCM)
#Instanciamos camara
camera = PiCamera()
camera.rotation = 180
#Declaran pines sensor 1
TRIG1 = 23
ECHO1 = 24
#Declaran variables globales sensor 1
BANSEN1A = False
BANSEN1N = 0
BANSEN1V = False
#Declaran pines sensor 2
TRIG2 = 5
ECHO2 = 6
#Declaran variables globales sensor 2
BANSEN2A = False
BANSEN2N = 0
BANSEN2V = False
#Declaran variables globales para json
tipo = ''
rango = 0
ubicacion = 'NONE'
zona = 1
lado = ''
fechahora = ''
path_json = ''
print ("Distance measurement in progress")

#Se define la funcion de cada pin
GPIO.setup(TRIG1,GPIO.OUT)
GPIO.setup(ECHO1,GPIO.IN)
GPIO.setup(TRIG2,GPIO.OUT)
GPIO.setup(ECHO2,GPIO.IN)

#Funcion que maneja al sensor 1
def sensor1():
    global BANSEN1A
    global BANSEN1N
    global BANSEN1V
    pulse_start = 0
    pulse_end = 0
    GPIO.output(TRIG1,False)
    time.sleep(2)

    GPIO.output(TRIG1,True)
    time.sleep(0.00001)
    GPIO.output(TRIG1,False)
    while GPIO.input(ECHO1) == 0:
        pulse_start = time.time()
            
    while GPIO.input(ECHO1) == 1:
        pulse_end = time.time()

    pulse_duration = pulse_end -pulse_start

    distance = pulse_duration * 17150
    distance = round(distance,2)
    if distance > 1 and distance < 4:
        print ("Distance Sensor derecho Nivel 5:", distance-0.5,"cm")
        BANSEN1A=True
        BANSEN1V=True
    elif distance > 3 and distance < 8:
        print ("Distance: Sensor derecho Nivel 4:", distance-0.5,"cm")
        BANSEN1A=True
        BANSEN1N=4
        BANSEN1V=False
    elif distance > 7 and distance < 12:
        print ("Distance: Sensor derecho Nivel 3:", distance-0.5,"cm")
        BANSEN1A=True
        BANSEN1N=3
        BANSEN1V=False
    elif distance > 11 and distance < 16:
        print ("Distance: Sensor derecho Nivel 2:", distance-0.5,"cm")
        BANSEN1A=True
        BANSEN1N=2
        BANSEN1V=False
    elif distance > 15 and distance < 20:
        print ("Distance: Sensor derecho Nivel 1:", distance-0.5,"cm")
        BANSEN1A=True
        BANSEN1N=1
        BANSEN1V=False
    else:
        BANSEN1A=False
        BANSEN1N=0
        BANSEN1V=False

#Funcion que maneja el sensor 2
def sensor2():
    global BANSEN2A
    global BANSEN2N
    global BANSEN2V
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
    if distance > 1 and distance < 4:
        print ("Distance Sensor izquierdo Nivel 5:", distance-0.5,"cm")
        print ('\n')
        BANSEN2A=True
        BANSEN2V=True
    elif distance > 3 and distance < 8:
        print ("Distance: Sensor izquierdo Nivel 4:", distance-0.5,"cm")
        BANSEN2A=True
        BANSEN2N=4
        BANSEN2V=False
    elif distance > 7 and distance < 12:
        print ("Distance: Sensor izquierdo Nivel 3:", distance-0.5,"cm")
        BANSEN2A=True
        BANSEN2N=3
        BANSEN2V=False
    elif distance > 11 and distance < 16:
        print ("Distance: Sensor izquierdo Nivel 2:", distance-0.5,"cm")
        BANSEN2A=True
        BANSEN2N=2
        BANSEN2V=False
    elif distance > 15 and distance < 20:
        print ("Distance: Sensor izquierdo Nivel 1:", distance-0.5,"cm")
        BANSEN2A=True
        BANSEN2N=1
        BANSEN2V=False
    else:
        BANSEN2A=False
        BANSEN2N=0
        BANSEN2V=False

#Funcion que maneja la camara
def video():
    global BANSEN1A
    global BANSEN2A
    global BANSEN1V
    global BANSEN2V
    global fechahora
    global path_json
    BANSEN1A=False
    BANSEN2A=False
    if BANSEN1V == True or BANSEN2V == True:
        BANSEN1V =False
        BANSEN2V =False
        fecha = (time.strftime("%d-%m-%Y"))
        hora = (time.strftime("%H:%M:%S"))
        fechahora = fecha+hora
        path = '/home/pi/Caute-Inc/Videos/'+fecha+'_'+hora+'.h264' #Por si acaso 'caute-%s'%i+    
        path_json = path
        print(path)
        camera.start_recording(path)
        time.sleep(30)
        camera.stop_recording()
    
def camara():
    global BANSEN1A
    global BANSEN2A
    global BANSEN1N
    global BANSEN2N
    if BANSEN1A == True or BANSEN2A == True:
        BANSEN1A=False
        BANSEN2A=False
        if BANSEN2N > 0 or BANSEN1N > 0:
            if BANSEN1N == BANSEN2N:
                if BANSEN1N == 1 or BANSEN1N == 2:
                    print ("Imprimo Alerta")
                elif BANSEN1N == 3:
                    for i in range(3):
                        time.sleep(0.5)
                        fecha = (time.strftime("%d-%m-%Y"))
                        hora = (time.strftime("%H:%M:%S"))
                        path = '/home/pi/Caute-Inc/Images/'+fecha+'_'+hora+'.jpg' #Por si acaso 'caute-%s'%i+
                        print(path)
                        camera.capture(path)
                elif BANSEN1N == 4:
                    for i in range(5):
                        time.sleep(0.5)
                        fecha = (time.strftime("%d-%m-%Y"))
                        hora = (time.strftime("%H:%M:%S"))
                        path = '/home/pi/Caute-Inc/Images/'+fecha+'_'+hora+'.jpg' #Por si acaso 'caute-%s'%i+
                        print(path)
                        camera.capture(path)
            elif BANSEN1N > BANSEN2N:
                if BANSEN1N == 1 or BANSEN1N == 2:
                    print ("Imprimo Alerta")
                elif BANSEN1N == 3:
                    for i in range(3):
                        time.sleep(0.5)
                        fecha = (time.strftime("%d-%m-%Y"))
                        hora = (time.strftime("%H:%M:%S"))
                        path = '/home/pi/Caute-Inc/Images/'+fecha+'_'+hora+'.jpg' #Por si acaso 'caute-%s'%i+
                        print(path)
                        camera.capture(path)
                elif BANSEN1N == 4:
                    for i in range(5):
                        time.sleep(0.5)
                        fecha = (time.strftime("%d-%m-%Y"))
                        hora = (time.strftime("%H:%M:%S"))
                        path = '/home/pi/Caute-Inc/Images/'+fecha+'_'+hora+'.jpg' #Por si acaso 'caute-%s'%i+
                        print(path)
                        camera.capture(path)
            else:
                if BANSEN2N == 1 or BANSEN2N == 2:
                    print ("Imprimo Alerta")
                elif BANSEN2N == 3:
                    for i in range(3):
                        time.sleep(0.5)
                        fecha = (time.strftime("%d-%m-%Y"))
                        hora = (time.strftime("%H:%M:%S"))
                        path = '/home/pi/Caute-Inc/Images/'+fecha+'_'+hora+'.jpg' #Por si acaso 'caute-%s'%i+
                        print(path)
                        camera.capture(path)
                elif BANSEN2N == 4:
                    for i in range(5):
                        time.sleep(0.5)
                        fecha = (time.strftime("%d-%m-%Y"))
                        hora = (time.strftime("%H:%M:%S"))
                        path = '/home/pi/Caute-Inc/Images/'+fecha+'_'+hora+'.jpg' #Por si acaso 'caute-%s'%i+
                        print(path)
                        camera.capture(path)
        else:
            BANSEN2N = 0
            BANSEN1N = 0
            print ("No hay alerta")
    else:
        print ("Nada que capturar\n")

#Main    
while True:
    try:
        data = {"Caute":
                [{"Alarmas":
                  [{"Tipo":tipo,
                    "Rango":rango,
                    "Ubicacion":ubicacion,
                    "Zona":zona,
                    "Lado": lado,
                    "Fechahora":fechahora,
                    "Path":path_json}]}]}
        data_string=json.dumps(data)
        print (data)
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
        vid = threading.Thread(target=video)
        vid.setDaemon(True)
        vid.start()
        imagen.join()
        vid.join()
    except KeyboardInterrupt:
        break
#Se restablece los pines GPIO
GPIO.cleanup()
