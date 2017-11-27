##########################################################################

from pyrebase import pyrebase
import RPi.GPIO as GPIO 
from picamera import PiCamera
from subprocess import call 
import time
import threading
##########################################################################
GPIO.setmode(GPIO.BCM) #Modo de lectura pines raspberry pi
# #########################################################################
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
bandera_de_almacenamiento_base_de_datos_sensor_1 = False
bandera_de_alarma_sensor_1 = False
bandera_de_rango_de_peligro_sensor_1 = 1
bandera_de_validacion_para_toma_de_video_sensor_1 = False
##########################################################################
############# Variables Manejo Alertas Sensor 2 ##########################
##########################################################################
bandera_de_almacenamiento_base_de_datos_sensor_2 = False
bandera_de_alarma_sensor_2 = False
bandera_de_rango_de_peligro_sensor_2 = 0
bandera_de_validacion_para_toma_de_video_sensor_2 = False
##########################################################################
########### Variables Informacion A Mostrar En Alertas ###################
##########################################################################
tipo_para_data_firebase = ""
rango_para_data_firebase = 0
ubicacion_para_data_firebase = "Guatemala"
zona_para_data_firebase = 1
lado_para_data_firebase = ""
fecha_y_hora_para_data_firebase = ""
path_url_imagenes_o_video_para_data_firebase = ""
##########################################################################
############## Configuracion Inicial Conexion Firebase ###################
##########################################################################
config = {
  "apiKey": "AIzaSyBncJyndTtrsvbaFhYpj1tu6MC2YK8egqs",
  "authDomain": "caute-a34d7.firebaseapp.com",
  "databaseURL": "https://caute-a34d7.firebaseio.com/",
  "storageBucket": "caute-a34d7.appspot.com"
#projectId: "caute-a34d7",
#messagingSenderId: "486985772902"
}
##########################################################################
############ Autenticacion del Usuario Inicio Firebase ###################
##########################################################################
firebase = pyrebase.initialize_app(config)
auth = firebase.auth()
usuario = 'andrea.molina1996@galileo.edu'
password = 123456
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
#   INVARIANTE:                                                             #
#   No requiere de variables invariante                                     #
#                                                                           #
#   POSTCONDITION:                                                          #
#   1. Modifica la bandera_de_alarma_sensor_1 para conocer si se activo una #
#   alarma                                                                  #
#   2. Modifica la bandera_derango_de_peligro_sensor_1 para conocer la      #
#   seriedad del posible impacto                                            #
#   3. Modifica la bandera_de_validadcion_para_toma_de_video_sensor_1       #
#   al estar en el rango 5                                                  #
#   4. Modifica la bandera_de_almacenamiento_base_de_datos_sensor_1 si es   #
#   requerido almacenar una imagen o video                                  #
#   5. Modifica rango_para_data_firebase es el rango que se enviara junto   #
#   al mensaje que contendra la alarma.                                     #
#   6. Modifica tipo_para_data_firebase la cual indica la proximidad del    #
#   objeto reconocido por el sensor.                                        #
#   De lo contrario no coloca false y el rango_para_data_firebase = 0       #
#############################################################################
def sensor1():
    global bandera_de_alarma_sensor_1
    global bandera_de_rango_de_peligro_sensor_1
    global bandera_de_validacion_para_toma_de_video_sensor_1
    global bandera_de_almacenamiento_base_de_datos_sensor_1
    global rango_para_data_firebase
    global tipo_para_data_firebase
    GPIO.output(TRIG,False)
    time.sleep(0.00001) #1
    GPIO.output(TRIG,True)
    time.sleep(0.00001)
    GPIO.output(TRIG,False)
    pulse_start = 0
    while GPIO.input(ECHO) == 0:
        pulse_start = time.time()
    pulse_end = 0        
    while GPIO.input(ECHO) == 1:
        pulse_end = time.time()

    pulse_duration = pulse_end -pulse_start

    distance = pulse_duration * 17150
    distance = round(distance,2)
    if distance > 1 and distance < 4:
        print ("Distance Sensor derecho Nivel 5:", distance-0.5,"cm")
        bandera_de_alarma_sensor_1=True
        bandera_de_validacion_para_toma_de_video_sensor_1=True
        bandera_de_almacenamiento_base_de_datos_sensor_1=True
        rango_para_data_firebase = 5
        tipo_para_data_firebase = "Impacto"
    elif distance > 3 and distance < 8:
        print ("Distance: Sensor derecho Nivel 4:", distance-0.5,"cm")
        bandera_de_alarma_sensor_1=True
        bandera_de_almacenamiento_base_de_datos_sensor_1=True
        bandera_de_rango_de_peligro_sensor_1=4
        rango_para_data_firebase = 4
        tipo_para_data_firebase = "Muy Proximo"
    elif distance > 7 and distance < 12:
        print ("Distance: Sensor derecho Nivel 3:", distance-0.5,"cm")
        bandera_de_alarma_sensor_1=True
        bandera_de_almacenamiento_base_de_datos_sensor_1=True
        bandera_de_rango_de_peligro_sensor_1=3
        rango_para_data_firebase = 3
        tipo_para_data_firebase = "Proximo"
    elif distance > 11 and distance < 16:
        print ("Distance: Sensor derecho Nivel 2:", distance-0.5,"cm")
        bandera_de_alarma_sensor_1=True
        bandera_de_almacenamiento_base_de_datos_sensor_1=True
        bandera_de_rango_de_peligro_sensor_1=2
        rango_para_data_firebase = 2
        tipo_para_data_firebase = "Menos Proximo"
    elif distance > 15 and distance < 20:
        print ("Distance: Sensor derecho Nivel 1:", distance-0.5,"cm")
        bandera_de_alarma_sensor_1=True
        bandera_de_almacenamiento_base_de_datos_sensor_1=True
        bandera_de_rango_de_peligro_sensor_1=1
        rango_para_data_firebase = 1
        tipo_para_data_firebase = "Lejos"
    else:
        bandera_de_alarma_sensor_1=False
        bandera_de_rango_de_peligro_sensor_1=0

#############################################################################
#   INDENT:                                                                 #
#   Maneja la lectura de objetos que se aproximan por el lado               #
#   izquierdo del automovil. Devolviendo el nivel de gravedad y             #
#   proximidad de los objetos.                                              #
#                                                                           #
#   RETURNS:                                                                #
#   No retorna ningun valor                                                 #
#                                                                           #
#   PRECONDITION:                                                           #
#   GPIO.input(ECHO2)== 0                                                   #
#   GPIO.input(ECHO2)== 1                                                   #
#   distance > 1 and distance < 4                                           #
#   distance > 3 and distance < 8                                           #
#   distance > 7 and distance < 12                                          #
#   distance > 11 and distance < 16                                         #
#   distance > 15 and distance < 20                                         #
#                                                                           #
#   INVARIANTE:                                                             #
#   No requiere de variables invariante                                     #
#                                                                           #
#   POSTCONDITION:                                                          #
#   1. Modifica la bandera_de_alarma_sensor_2 para conocer si se activo una #
#   alarma                                                                  #
#   2. Modifica la bandera_derango_de_peligro_sensor_2 para conocer la      #
#   seriedad del posible impacto                                            #
#   3. Modifica la bandera_de_validadcion_para_toma_de_video_sensor_2       #
#   al estar en el rango 5                                                  #
#   4. Modifica la bandera_de_almacenamiento_base_de_datos_sensor_2 si es   #
#   requerido almacenar una imagen o video                                  #
#   5. Modifica rango_para_data_firebase es el rango que se enviara junto   #
#   al mensaje que contendra la alarma.                                     #
#   6. Modifica tipo_para_data_firebase la cual indica la proximidad del    #
#   objeto reconocido por el sensor.                                        #
#   De lo contrario no coloca false y el rango_para_data_firebase = 0       #
#############################################################################
def sensor2():
    global bandera_de_alarma_sensor_2
    global bandera_de_rango_de_peligro_sensor_2
    global bandera_de_validacion_para_toma_de_video_sensor_2
    global bandera_de_almacenamiento_base_de_datos_sensor_2
    global rango_para_data_firebase
    global tipo_para_data_firebase
    GPIO.output(TRIG2,False)
    time.sleep(0.00001) #1
    GPIO.output(TRIG2,True)
    time.sleep(0.00001)
    GPIO.output(TRIG2,False)
    pulse_start = 0
    while GPIO.input(ECHO2) == 0:
        pulse_start = time.time()
    pulse_end = 0        
    while GPIO.input(ECHO2) == 1:
        pulse_end = time.time()

    pulse_duration = pulse_end -pulse_start

    distance = pulse_duration * 17150
    distance = round(distance,2)
    if distance > 1 and distance < 4:
        print ("Distance Sensor izquierdo Nivel 5:", distance-0.5,"cm")
        print ('\n')
        bandera_de_alarma_sensor_2=True
        bandera_de_validacion_para_toma_de_video_sensor_2=True
        bandera_de_almacenamiento_base_de_datos_sensor_2=True
        rango_para_data_firebase = 5
        tipo_para_data_firebase = "Impacto"
    elif distance > 3 and distance < 8:
        print ("Distance: Sensor izquierdo Nivel 4:", distance-0.5,"cm")
        bandera_de_alarma_sensor_2=True
        bandera_de_almacenamiento_base_de_datos_sensor_2=True
        bandera_de_rango_de_peligro_sensor_2=4
        rango_para_data_firebase = 4
        tipo_para_data_firebase = "Muy Proximo"
    elif distance > 7 and distance < 12:
        print ("Distance: Sensor izquierdo Nivel 3:", distance-0.5,"cm")
        bandera_de_alarma_sensor_2=True
        bandera_de_almacenamiento_base_de_datos_sensor_2=True
        bandera_de_rango_de_peligro_sensor_2=3
        rango_para_data_firebase = 3
        tipo_para_data_firebase = "Proximo"
    elif distance > 11 and distance < 16:
        print ("Distance: Sensor izquierdo Nivel 2:", distance-0.5,"cm")
        bandera_de_alarma_sensor_2=True
        bandera_de_almacenamiento_base_de_datos_sensor_2=True
        bandera_de_rango_de_peligro_sensor_2=2
        rango_para_data_firebase = 2
        tipo_para_data_firebase = "Menos Proximo"
    elif distance > 15 and distance < 20:
        print ("Distance: Sensor izquierdo Nivel 1:", distance-0.5,"cm")
        bandera_de_alarma_sensor_2=True
        bandera_de_almacenamiento_base_de_datos_sensor_2=True
        bandera_de_rango_de_peligro_sensor_2=1
        rango_para_data_firebase = 1
        tipo_para_data_firebase = "Lejos"
    else:
        bandera_de_alarma_sensor_2=False
        bandera_de_rango_de_peligro_sensor_2=0

#############################################################################
#   INDENT:                                                                 #
#   Maneja la toma del video cuando existe un impacto es decir se           #
#   encuentra en el rango 5                                                 #
#                                                                           #
#   RETURNS:                                                                #
#   No retorna ningun valor                                                 #
#                                                                           #
#   PRECONDITION:                                                           #
#   1. bandera_de_alarma_sensor_1 == bandera_de_alarma_sensor_2             #
#   and bandera_de_alarma_sensor_1 == True                                  #
#   2. bandera_de_alarma_sensor_1 == True                                   #
#   3. bandera_de_alarma_sensor_2 == True                                   #
#   4. bandera_de_alarma_sensor_1 == True                                   #
#   or bandera_de_alarma_sensor_2 == True                                   #
#   5. bandera_de_validacion_para_toma_de_video_sensor_1 == True            #
#   or bandera_de_validacion_para_toma_de_video_sensor_2 == True            #
#                                                                           #
#   INVARIANTE:                                                             #
#   No requiere de variables invariante                                     #
#                                                                           #
#   POSTCONDITION:                                                          #
#                                                                           #
#############################################################################
#Funcion que maneja la camara
def video():
    global bandera_de_alarma_sensor_1
    global bandera_de_alarma_sensor_2
    global bandera_de_validacion_para_toma_de_video_sensor_1
    global bandera_de_validacion_para_toma_de_video_sensor_2
    global path_url_imagenes_o_video_para_data_firebase
    global fecha_y_hora_para_data_firebase
    global lado_para_data_firebase
    if bandera_de_alarma_sensor_1 == bandera_de_alarma_sensor_2 and bandera_de_alarma_sensor_1 == True:
        lado_para_data_firebase = "Ambos lados"
    elif bandera_de_alarma_sensor_1 == True:
        lado_para_data_firebase = "derecha"
    elif bandera_de_alarma_sensor_2 == True:
        lado_para_data_firebase = "izquierda"
    if bandera_de_alarma_sensor_1 == True or bandera_de_alarma_sensor_2 == True:
        if bandera_de_validacion_para_toma_de_video_sensor_1 == True or bandera_de_validacion_para_toma_de_video_sensor_2 == True:
            bandera_de_validacion_para_toma_de_video_sensor_1 =False
            bandera_de_validacion_para_toma_de_video_sensor_2 =False
            bandera_de_alarma_sensor_1=False
            bandera_de_alarma_sensor_2=False
            fecha = (time.strftime("%d-%m-%Y"))
            hora = (time.strftime("%H:%M:%S"))
            fecha_y_hora_para_data_firebase = fecha+" "+hora
            path = '/home/pi/Caute-Inc/Videos/'+fecha+'_'+hora+'.h264'   
            path_store = "Videos/"+fecha+'_'+hora+'.h264'
            name = "videos/"+fecha+"_"+hora+".h264"
            camera.start_recording(path)
            time.sleep(20)
            camera.stop_recording()
            storage.child(name).put(path_store,user['idToken'])
            path_url_imagenes_o_video_para_data_firebase= ""
            path_url_imagenes_o_video_para_data_firebase+=storage.child(name).get_url(user['idToken'])
            path_url_imagenes_o_video_para_data_firebase+= " "
#####################################################################################
#   INDENT:                                                                         #
#   Maneja la cantidad de imagenes y el lado del que el sensor marco                #
#   el contacto o la alarma.                                                        #
#                                                                                   #
#   RETURNS:                                                                        #
#   No retorna ningun valor                                                         #
#                                                                                   #
#   PRECONDITION:                                                                   #
#   1. bandera_de_alarma_sensor_1 == bandera_de_alarma_sensor_2                     #
#   and bandera_de_alarma_sensor_1 == True                                          #
#   2. bandera_de_rango_de_peligro_sensor_2 > 0                                     #
#   or bandera_de_rango_de_peligro_sensor_1 > 0                                     #
#   3. bandera_de_rango_de_peligro_sensor_1 == bandera_de_rango_de_peligro_sensor_2 #
#   4. bandera_de_rango_de_peligro_sensor_1 == 1                                    #
#   or bandera_de_rango_de_peligro_sensor_1 == 2                                    #
#   5. bandera_de_rango_de_peligro_sensor_1 == 3                                    #
#   6. bandera_de_rango_de_peligro_sensor_1 > bandera_de_rango_de_peligro_sensor_2  #
#   7. bandera_de_rango_de_peligro_sensor_1 < bandera_de_rango_de_peligro_sensor_2  #
#   8. bandera_de_rango_de_peligro_sensor_2 == 1                                    #
#   or bandera_de_rango_de_peligro_sensor_2 == 2                                    #
#   9. bandera_de_rango_de_peligro_sensor_2 == 3                                    #
#                                                                                   #
#   INVARIANTE:                                                                     #
#   No requiere de variables invariante                                             #
#                                                                                   #
#   POSTCONDITION:                                                                  #
#                                                                                   #
#####################################################################################
def camara():
    global bandera_de_alarma_sensor_1
    global bandera_de_alarma_sensor_2
    global bandera_de_rango_de_peligro_sensor_1
    global bandera_de_rango_de_peligro_sensor_2
    global fecha_y_hora_para_data_firebase
    global path_url_imagenes_o_video_para_data_firebase
    global lado_para_data_firebase
    if bandera_de_alarma_sensor_1 == True or bandera_de_alarma_sensor_2 == True:
        bandera_de_alarma_sensor_1=False
        bandera_de_alarma_sensor_2=False
        if bandera_de_rango_de_peligro_sensor_2 > 0 or bandera_de_rango_de_peligro_sensor_1 > 0:
            path_url_imagenes_o_video_para_data_firebase= ""
            if bandera_de_rango_de_peligro_sensor_1 == bandera_de_rango_de_peligro_sensor_2:
                lado_para_data_firebase = "Ambos lados"
                if bandera_de_rango_de_peligro_sensor_1 == 1 or bandera_de_rango_de_peligro_sensor_1 == 2:
                    print ("Imprimo Alerta")
                    fecha = (time.strftime("%d-%m-%Y"))
                    hora = (time.strftime("%H:%M:%S"))
                    fecha_y_hora_para_data_firebase = fecha+" "+hora
                elif bandera_de_rango_de_peligro_sensor_1 == 3:
                    for i in range(3):
                        time.sleep(0.05)
                        fecha = (time.strftime("%d-%m-%Y"))
                        hora = (time.strftime("%H:%M:%S"))
                        fecha_y_hora_para_data_firebase = fecha+" "+hora
                        path = '/home/pi/Caute-Inc/Images/'+fecha+'_'+hora+'.jpg' 
                        path_store = "Images/"+fecha+'_'+hora+'.jpg'
                        name = "images/"+fecha+"_"+hora+".jpg"
                        camera.capture(path)
                        storage.child(name).put(path_store,user['idToken'])
                        path_url_imagenes_o_video_para_data_firebase+=storage.child(name).get_url(user['idToken'])
                        path_url_imagenes_o_video_para_data_firebase+= " "
                else:
                    for i in range(5):
                        time.sleep(0.05)
                        fecha = (time.strftime("%d-%m-%Y"))
                        hora = (time.strftime("%H:%M:%S"))
                        fecha_y_hora_para_data_firebase = fecha+" "+hora
                        path = '/home/pi/Caute-Inc/Images/'+fecha+'_'+hora+'.jpg' 
                        path_store = "Images/"+fecha+'_'+hora+'.jpg'
                        name = "images/"+fecha+"_"+hora+".jpg"
                        camera.capture(path)
                        storage.child(name).put(path_store,user['idToken'])
                        path_url_imagenes_o_video_para_data_firebase+=storage.child(name).get_url(user['idToken'])
                        path_url_imagenes_o_video_para_data_firebase+= " "
            elif bandera_de_rango_de_peligro_sensor_1 > bandera_de_rango_de_peligro_sensor_2:
                lado_para_data_firebase = "derecho"
                if bandera_de_rango_de_peligro_sensor_1 == 1 or bandera_de_rango_de_peligro_sensor_1 == 2:
                    fecha = (time.strftime("%d-%m-%Y"))
                    hora = (time.strftime("%H:%M:%S"))
                    fecha_y_hora_para_data_firebase = fecha+" "+hora
                    print ("Imprimo Alerta")
                elif bandera_de_rango_de_peligro_sensor_1 == 3:
                    for i in range(3):
                        time.sleep(0.05)
                        fecha = (time.strftime("%d-%m-%Y"))
                        hora = (time.strftime("%H:%M:%S"))
                        fecha_y_hora_para_data_firebase = fecha+" "+hora
                        path = '/home/pi/Caute-Inc/Images/'+fecha+'_'+hora+'.jpg' 
                        path_store = "Images/"+fecha+'_'+hora+'.jpg'
                        name = "images/"+fecha+"_"+hora+".jpg"
                        camera.capture(path)
                        storage.child(name).put(path_store,user['idToken'])
                        path_url_imagenes_o_video_para_data_firebase+=storage.child(name).get_url(user['idToken'])
                        path_url_imagenes_o_video_para_data_firebase+= " "
                else:
                    for i in range(5):
                        time.sleep(0.05)
                        fecha = (time.strftime("%d-%m-%Y"))
                        hora = (time.strftime("%H:%M:%S"))
                        fecha_y_hora_para_data_firebase = fecha+" "+hora
                        path = '/home/pi/Caute-Inc/Images/'+fecha+'_'+hora+'.jpg' 
                        path_store = "Images/"+fecha+'_'+hora+'.jpg'
                        name = "images/"+fecha+"_"+hora+".jpg"
                        camera.capture(path)
                        storage.child(name).put(path_store,user['idToken'])
                        path_url_imagenes_o_video_para_data_firebase+=storage.child(name).get_url(user['idToken'])
                        path_url_imagenes_o_video_para_data_firebase+= " "
            else:
                lado_para_data_firebase ="izquierdo"
                if bandera_de_rango_de_peligro_sensor_2 == 1 or bandera_de_rango_de_peligro_sensor_2 == 2:
                    fecha = (time.strftime("%d-%m-%Y"))
                    hora = (time.strftime("%H:%M:%S"))
                    fecha_y_hora_para_data_firebase = fecha+" "+hora
                    print ("Imprimo Alerta")
                elif bandera_de_rango_de_peligro_sensor_2 == 3:
                    for i in range(3):
                        time.sleep(0.05)
                        fecha = (time.strftime("%d-%m-%Y"))
                        hora = (time.strftime("%H:%M:%S"))
                        fecha_y_hora_para_data_firebase = fecha+" "+hora
                        path = '/home/pi/Caute-Inc/Images/'+fecha+'_'+hora+'.jpg' 
                        path_store = "Images/"+fecha+'_'+hora+'.jpg'
                        name = "images/"+fecha+"_"+hora+".jpg"
                        camera.capture(path)
                        storage.child(name).put(path_store,user['idToken'])
                        path_url_imagenes_o_video_para_data_firebase+=storage.child(name).get_url(user['idToken'])
                        path_url_imagenes_o_video_para_data_firebase+= " "
                else:
                    for i in range(5):
                        time.sleep(0.05)
                        fecha = (time.strftime("%d-%m-%Y"))
                        hora = (time.strftime("%H:%M:%S"))
                        fecha_y_hora_para_data_firebase = fecha+" "+hora
                        path = '/home/pi/Caute-Inc/Images/'+fecha+'_'+hora+'.jpg' 
                        path_store = "Images/"+fecha+'_'+hora+'.jpg'
                        name = "images/"+fecha+"_"+hora+".jpg"
                        camera.capture(path)
                        storage.child(name).put(path_store,user['idToken'])
                        path_url_imagenes_o_video_para_data_firebase+=storage.child(name).get_url(user['idToken'])
                        path_url_imagenes_o_video_para_data_firebase+= " "
        else:
            bandera_de_rango_de_peligro_sensor_2 = 0
            bandera_de_rango_de_peligro_sensor_1 = 0
#####################################################################################
#   INDENT:                                                                         #
#   Maneja los threads de los sensores, camara y video; a su vez que maneja         #
#   la generacion de las alarmas.                                                   #
#                                                                                   #
#   RETURNS:                                                                        #
#   No retorna ningun valor                                                         #
#                                                                                   #
#   PRECONDITION:                                                                   #
#   1. bandera_de_almacenamiento_base_de_datos_sensor_2 == True                     #
#   or bandera_de_almacenamiento_base_de_datos_sensor_1 == True                     #
#   2. path_url_imagenes_o_video_para_data_firebase != ""                           #
#   and fecha_y_hora_para_data_firebase != ""                                       #
#                                                                                   #
#   INVARIANTE:                                                                     #
#   No requiere de variables invariante                                             #
#                                                                                   #
#   POSTCONDITION:                                                                  #
#                                                                                   #
#   ExCEPTION:                                                                      #
#   Cuando existe una interrupcion con el teclado o los pines del GPIO ya estan     #
#   en uso                                                                          #
#                                                                                   #
#####################################################################################      
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
        print("Objeto cerca")
        print("Lado "+lado_para_data_firebase)
        print("Seriedad %s"%rango_para_data_firebase)
        print("Proximidad "+tipo_para_data_firebase)
        if bandera_de_almacenamiento_base_de_datos_sensor_2 == True or bandera_de_almacenamiento_base_de_datos_sensor_1 == True:
            if path_url_imagenes_o_video_para_data_firebase != "" and fecha_y_hora_para_data_firebase != "":
                bandera_de_almacenamiento_base_de_datos_sensor_1=False
                bandera_de_almacenamiento_base_de_datos_sensor_2=False
                data = {"date":fecha_y_hora_para_data_firebase,
                        "notified":False,
                        "seriousness":rango_para_data_firebase,
                        "side":lado_para_data_firebase,
                        "type":tipo_para_data_firebase,
                        "ubication": ubicacion_para_data_firebase,
                        "zone": zona_para_data_firebase,
                        "urlImg":path_url_imagenes_o_video_para_data_firebase}
                path_url_imagenes_o_video_para_data_firebase=""
                rango_para_data_firebase = 0
                tipo_para_data_firebase=""
                lado_para_data_firebase=""
                results = db.child("URepm85HkpXKBWX8SYh5225Fpxc2").child("Alerts").push(data,user['idToken'])
    except KeyboardInterrupt:
        break
#Se restablece los pines GPIO
GPIO.cleanup()
