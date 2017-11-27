##########################################################################
from pyrebase import pyrebase
import RPi.GPIO as GPIO 
from picamera import PiCamera
from subprocess import call 
import time
import threading
##########################################################################
GPIO.setmode(GPIO.BCM) #Modo de lectura pines raspberry pi
##########################################################################
############## Instanciando la funcion Camara ############################
##########################################################################
camera = PiCamera()
camera.rotation = 180
##########################################################################
################# Pines de Lectura Sensor 1 ##############################
##########################################################################
TRIG = 23
ECHO = 24
##########################################################################
################# Pines de Lectura Sensor 2 ##############################
##########################################################################
TRIG2 = 5
ECHO2 = 6
##########################################################################
############# Variables Manejo Alertas Sensor 1 ##########################
##########################################################################
BANSEN1B = False
BANSEN1A = False
BANSEN1N = 1
BANSEN1V = False
##########################################################################
############# Variables Manejo Alertas Sensor 2 ##########################
##########################################################################
BANSEN2B = False
BANSEN2A = False
BANSEN2N = 0
BANSEN2V = False
##########################################################################
########### Variables Informacion A Mostrar En Alertas ###################
##########################################################################
tipo = ""
rango = 0
ubicacion = "Guatemala"
zona = 1
lado = ""
fechahora = ""
path_json = ""
##########################################################################
############## Configuracion Inicial Conexion Firebase ###################
##########################################################################
config = {
  "apiKey": "AIzaSyA2rArrbLYU759h-_sHwMZSU5l9nW-H8Z8",
  "authDomain": "caute-ec4ff.firebaseapp.com",
  "databaseURL": "https://caute-ec4ff.firebaseio.com",
  "storageBucket": "caute-ec4ff.appspot.com"
}
##########################################################################
############ Autenticacion del Usuario Inicio Firebase ###################
##########################################################################
firebase = pyrebase.initialize_app(config)
auth = firebase.auth()
usuario = 'andrea.molina1996@galileo.edu'
password = 123456789
user = auth.sign_in_with_email_and_password(usuario,password)
db = firebase.database()
##########################################################################
#################### Conexion Storage Firebase ###########################
##########################################################################
storage = firebase.storage()
##########################################################################
############### Definicion Pines De Entrada O Salida #####################
##########################################################################
GPIO.setup(TRIG,GPIO.OUT)
GPIO.setup(ECHO,GPIO.IN)
GPIO.setup(TRIG2,GPIO.OUT)
GPIO.setup(ECHO2,GPIO.IN)

print ("Toma de informacion sistema caute en progreso")

#############################################################################
#   INDENT:                                                                 #
#   Maneja la lectura de objetos que se aproximan por el lado               #
#   derecho del automovil. Devolviendo el nivel de gravedad y               #
#   proximidad de los objetos.                                              #
#                                                                           #
#   RETURNS:                                                                #
#   No retorna ningun valor                                                 #
#                                                                           #
#   PRECONDITION:                                                           #
#   GPIO.input(ECHO)== 0                                                    #
#   GPIO.input(ECHO)== 1                                                    #
#   distance > 1 and distance < 4                                           #
#   distance > 3 and distance < 8                                           #
#   distance > 7 and distance < 12                                          #
#   distance > 11 and distance < 16                                         #
#   distance > 15 and distance < 20                                         #
#                                                                           #
#                                                                           #
#                                                                           #
#                                                                           #
def sensor1():
    global BANSEN1A
    global BANSEN1N
    global BANSEN1V
    global BANSEN1B
    global rango
    global lado
    global tipo
    pulse_start = 0
    pulse_end = 0
    lado = "Derecho"
    GPIO.output(TRIG,False)
    time.sleep(1)

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
    if distance > 1 and distance < 4:
        print ("Distance Sensor derecho Nivel 5:", distance-0.5,"cm")
        BANSEN1A=True
        BANSEN1V=True
        BANSEN1B=True
        rango = 5
        tipo = "Impacto"
    elif distance > 3 and distance < 8:
        print ("Distance: Sensor derecho Nivel 4:", distance-0.5,"cm")
        BANSEN1A=True
        BANSEN1B=True
        BANSEN1N=4
        rango = 4
        tipo = "Muy Proximo"
    elif distance > 7 and distance < 12:
        print ("Distance: Sensor derecho Nivel 3:", distance-0.5,"cm")
        BANSEN1A=True
        BANSEN1B=True
        BANSEN1N=3
        rango = 3
        tipo = "Proximo"
    elif distance > 11 and distance < 16:
        print ("Distance: Sensor derecho Nivel 2:", distance-0.5,"cm")
        BANSEN1A=True
        BANSEN1B=True
        BANSEN1N=2
        rango = 2
        tipo = "Menos Proximo"
    elif distance > 15 and distance < 20:
        print ("Distance: Sensor derecho Nivel 1:", distance-0.5,"cm")
        BANSEN1A=True
        BANSEN1B=True
        BANSEN1N=1
        rango = 1
        tipo = "Lejos"
    else:
        BANSEN1A=False
        BANSEN1N=0

#Funcion que maneja el sensor 2
def sensor2():
    global BANSEN2A
    global BANSEN2N
    global BANSEN2V
    global BANSEN2B
    global rango
    global lado
    global tipo
    pulse_start = 0
    pulse_end = 0
    lado = "Izquierdo"
    GPIO.output(TRIG2,False)
    time.sleep(1)

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
        BANSEN2B=True
        rango = 5
        tipo = "Impacto"
    elif distance > 3 and distance < 8:
        print ("Distance: Sensor izquierdo Nivel 4:", distance-0.5,"cm")
        BANSEN2A=True
        BANSEN2B=True
        BANSEN2N=4
        rango = 4
        tipo = "Muy Proximo"
    elif distance > 7 and distance < 12:
        print ("Distance: Sensor izquierdo Nivel 3:", distance-0.5,"cm")
        BANSEN2A=True
        BANSEN2B=True
        BANSEN2N=3
        rango = 3
        tipo = "Proximo"
    elif distance > 11 and distance < 16:
        print ("Distance: Sensor izquierdo Nivel 2:", distance-0.5,"cm")
        BANSEN2A=True
        BANSEN2B=True
        BANSEN2N=2
        rango = 2
        tipo = "Menos Proximo"
    elif distance > 15 and distance < 20:
        print ("Distance: Sensor izquierdo Nivel 1:", distance-0.5,"cm")
        BANSEN2A=True
        BANSEN2B=True
        BANSEN2N=1
        rango = 1
        tipo = "Lejos"
    else:
        BANSEN2A=False
        BANSEN2N=0

#Funcion que maneja la camara
def video():
    global BANSEN1A
    global BANSEN2A
    global BANSEN1V
    global BANSEN2V
    global path_json
    global fechahora
    if BANSEN1A == True or BANSEN2A == True:
        print ("Esta entrando al primer if de video")
        if BANSEN1V == True or BANSEN2V == True:
            print ("Esta entrando al segundo if de video")
            BANSEN1V =False
            BANSEN2V =False
            BANSEN1A=False
            BANSEN2A=False
            fecha = (time.strftime("%d-%m-%Y"))
            hora = (time.strftime("%H:%M:%S"))
            fechahora = fecha+" "+hora
            path = '/home/pi/Caute-Inc/Videos/'+fecha+'_'+hora+'.h264' #Por si acaso 'caute-%s'%i+    
            path_store = "Videos/"+fecha+'_'+hora+'.h264'
            name = "videos/"+fecha+"_"+hora+".h264"
            print(path)
            camera.start_recording(path)
            camara.wait_recording(20)
            camera.stop_recording()
            storage.child(name).put(path_store,user['idToken'])
            path_json= ""
            path_json+=storage.child(name).get_url(user['idToken'])
            path_json+= " "
        else:
            print ("Esta entrando al else del segundo if video")
    else:
        print ("Esta entrandoo al else del primer if video")
    
def camara():
    global BANSEN1A
    global BANSEN2A
    global BANSEN1N
    global BANSEN2N
    global fechahora
    global path_json
    if BANSEN1A == True or BANSEN2A == True:
        BANSEN1A=False
        BANSEN2A=False
        if BANSEN2N > 0 or BANSEN1N > 0:
            path_json= ""
            if BANSEN1N == BANSEN2N:
                if BANSEN1N == 1 or BANSEN1N == 2:
                    print ("Imprimo Alerta")
                    fecha = (time.strftime("%d-%m-%Y"))
                    hora = (time.strftime("%H:%M:%S"))
                    fechahora = fecha+" "+hora
                elif BANSEN1N == 3:
                    print ("Tercer rango camara")
                    for i in range(3):
                        time.sleep(0.5)
                        fecha = (time.strftime("%d-%m-%Y"))
                        hora = (time.strftime("%H:%M:%S"))
                        fechahora = fecha+" "+hora
                        path = '/home/pi/Caute-Inc/Images/'+fecha+'_'+hora+'.jpg' #Por si acaso 'caute-%s'%i+
                        path_store = "Images/"+fecha+'_'+hora+'.jpg'
                        name = "images/"+fecha+"_"+hora+".jpg"
                        print(path)
                        camera.capture(path)
                        storage.child(name).put(path_store,user['idToken'])
                        path_json+=storage.child(name).get_url(user['idToken'])
                        path_json+= " "
                else:
                    print ("Cuarto rango camara")
                    for i in range(5):
                        time.sleep(0.5)
                        fecha = (time.strftime("%d-%m-%Y"))
                        hora = (time.strftime("%H:%M:%S"))
                        fechahora = fecha+" "+hora
                        path = '/home/pi/Caute-Inc/Images/'+fecha+'_'+hora+'.jpg' #Por si acaso 'caute-%s'%i+
                        path_store = "Images/"+fecha+'_'+hora+'.jpg'
                        name = "images/"+fecha+"_"+hora+".jpg"
                        print(path)
                        camera.capture(path)
                        storage.child(name).put(path_store,user['idToken'])
                        path_json+=storage.child(name).get_url(user['idToken'])
                        path_json+= " "
            elif BANSEN1N > BANSEN2N:
                if BANSEN1N == 1 or BANSEN1N == 2:
                    print ("Imprimo Alerta")
                elif BANSEN1N == 3:
                    for i in range(3):
                        time.sleep(0.5)
                        fecha = (time.strftime("%d-%m-%Y"))
                        hora = (time.strftime("%H:%M:%S"))
                        fechahora = fecha+" "+hora
                        path = '/home/pi/Caute-Inc/Images/'+fecha+'_'+hora+'.jpg' #Por si acaso 'caute-%s'%i+
                        path_store = "Images/"+fecha+'_'+hora+'.jpg'
                        name = "images/"+fecha+"_"+hora+".jpg"
                        print(path)
                        camera.capture(path)
                        storage.child(name).put(path_store,user['idToken'])
                        path_json+=storage.child(name).get_url(user['idToken'])
                        path_json+= " "
                else:
                    for i in range(5):
                        time.sleep(0.5)
                        fecha = (time.strftime("%d-%m-%Y"))
                        hora = (time.strftime("%H:%M:%S"))
                        fechahora = fecha+" "+hora
                        path = '/home/pi/Caute-Inc/Images/'+fecha+'_'+hora+'.jpg' #Por si acaso 'caute-%s'%i+
                        path_store = "Images/"+fecha+'_'+hora+'.jpg'
                        name = "images/"+fecha+"_"+hora+".jpg"
                        print(path)
                        camera.capture(path)
                        storage.child(name).put(path_store,user['idToken'])
                        path_json+=storage.child(name).get_url(user['idToken'])
                        path_json+= " "
            else:
                if BANSEN2N == 1 or BANSEN2N == 2:
                    print ("Imprimo Alerta")
                elif BANSEN2N == 3:
                    for i in range(3):
                        time.sleep(0.5)
                        fecha = (time.strftime("%d-%m-%Y"))
                        hora = (time.strftime("%H:%M:%S"))
                        fechahora = fecha+" "+hora
                        path = '/home/pi/Caute-Inc/Images/'+fecha+'_'+hora+'.jpg' #Por si acaso 'caute-%s'%i+
                        path_store = "Images/"+fecha+'_'+hora+'.jpg'
                        name = "images/"+fecha+"_"+hora+".jpg"
                        print(path)
                        camera.capture(path)
                        storage.child(name).put(path_store,user['idToken'])
                        path_json+=storage.child(name).get_url(user['idToken'])
                        path_json+= " "
                else:
                    for i in range(5):
                        time.sleep(0.5)
                        fecha = (time.strftime("%d-%m-%Y"))
                        hora = (time.strftime("%H:%M:%S"))
                        fechahora = fecha+" "+hora
                        path = '/home/pi/Caute-Inc/Images/'+fecha+'_'+hora+'.jpg' #Por si acaso 'caute-%s'%i+
                        path_store = "Images/"+fecha+'_'+hora+'.jpg'
                        name = "images/"+fecha+"_"+hora+".jpg"
                        print(path)
                        camera.capture(path)
                        storage.child(name).put(path_store,user['idToken'])
                        path_json+=storage.child(name).get_url(user['idToken'])
                        path_json+= " "
        else:
            BANSEN2N = 0
            BANSEN1N = 0
            print ("No hay alerta camara")
    else:
        print ("Nada que capturar en camara")
               
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
        if BANSEN2B == True or BANSEN1B == True:
            if path_json != "" and fechahora != "":
                BANSEN1B=False
                BANSEN2B=False
                data = {"date":fechahora,
                        "notified":False,
                        "seriousness":rango,
                        "side":lado,
                        "type":tipo,
                        "ubication": ubicacion,
                        "zone": zona,
                        "urlImg":path_json}
                path_json=""
                rango = 0
                tipo=""
                results = db.child("users").child("5j7CLayx3uPCo2opG1CENlINLhr2").child("Alerts").push(data,user['idToken'])
    except KeyboardInterrupt:
        break
#Se restablece los pines GPIO
GPIO.cleanup()
