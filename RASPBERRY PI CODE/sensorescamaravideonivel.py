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
BANSEN1N = 1
BANSEN1V = False
#Declaran pines sensor 2
TRIG2 = 5
ECHO2 = 6
#Declaran variables globales sensor 2
BANSEN2F = False
BANSEN2A = False
BANSEN2N = 0
BANSEN2V = False
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
    global BANSEN1N
    global BANSEN1V
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
    if distance > 25 and distance < 60:
        print ("Distance Sensor derecho Nivel 5:", distance-0.5,"cm")
        BANSEN1A=True
        BANSEN1F=True
        BANSEN1V=True
    elif distance > 59 and distance < 100:
        print ("Distance: Sensor derecho Nivel 4:", distance-0.5,"cm")
        BANSEN1A=True
        BANSEN1F=False
        BANSEN1N=4
    elif distance > 99 and distance < 150:
        print ("Distance: Sensor derecho Nivel 3:", distance-0.5,"cm")
        BANSEN1A=True
        BANSEN1F=False
        BANSEN1N=3
    elif distance > 149 and distance < 200:
        print ("Distance: Sensor derecho Nivel 2:", distance-0.5,"cm")
        BANSEN1A=True
        BANSEN1F=False
        BANSEN1N=2
    elif distance > 199 and distance < 250:
        print ("Distance: Sensor derecho Nivel 1:", distance-0.5,"cm")
        BANSEN1A=True
        BANSEN1F=False
        BANSEN1N=1
    else:
        BANSEN1A=False
        BANSEN1F=False
        BANSEN1N=0

#Funcion que maneja el sensor 2
def sensor2():
    global BANSEN2A
    global BANSEN2F
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
        BANSEN2F=True
        BANSEN2V=True
    elif distance > 3 and distance < 8:
        print ("Distance: Sensor izquierdo Nivel 4:", distance-0.5,"cm")
        BANSEN2A=True
        BANSEN2F=False
        BANSEN2N=4
    elif distance > 7 and distance < 12:
        print ("Distance: Sensor izquierdo Nivel 3:", distance-0.5,"cm")
        BANSEN2A=True
        BANSEN2F=False
        BANSEN2N=3
    elif distance > 11 and distance < 16:
        print ("Distance: Sensor izquierdo Nivel 2:", distance-0.5,"cm")
        BANSEN2A=True
        BANSEN2F=False
        BANSEN2N=2
    elif distance > 15 and distance < 20:
        print ("Distance: Sensor izquierdo Nivel 1:", distance-0.5,"cm")
        BANSEN2A=True
        BANSEN2F=False
        BANSEN2N=1
    else:
        BANSEN2A=False
        BANSEN2F=False
        BANSEN2N=0

#Funcion que maneja la camara
def video():
    global BANSEN1F
    global BANSEN1A
    global BANSEN2F
    global BANSEN2A
    global BANSEN1V
    global BANSEN2V
    if BANSEN1F == True or BANSEN2F == True:
        BANSEN1F=False
        BANSEN1A=False
        BANSEN2F=False
        BANSEN2A=False
        if BANSEN1V == True or BANSEN2V == True:
            BANSEN1V =False
            BANSEN2V =False
            fecha = (time.strftime("%d-%m-%Y"))
            hora = (time.strftime("%H:%M:%S"))
            path = '/home/pi/Caute-Inc/Videos/'+fecha+'_'+hora+'.h264' #Por si acaso 'caute-%s'%i+    
            print(path)
            camera.start_recording(path)
            time.sleep(30)
            camera.stop_recording()
        else:
            print ("Nada que grabar segundo IF")
    else:
        print ("Nada que grabar primer IF")
    
def camara():
    global BANSEN1F
    global BANSEN1A
    global BANSEN2F
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
                else:
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
                else:
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
                else:
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
