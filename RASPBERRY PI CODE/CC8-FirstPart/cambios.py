#### CONEXION FIREBASE ####
from pyrebase import pyrebase
#### GPIO ####
import RPi.GPIO as GPIO
#### TIME & THREADS ####
import time
import threading
import json
import datetime
import collections

GPIO.setmode(GPIO.BCM) #Modo de lectura pines raspberry pi

#### Pines de Lectura Sensor 1 ####
TRIG_SEN_1 = 6
ECHO_SEN_1 = 12
GPIO.setup(TRIG_SEN_1,GPIO.OUT)
GPIO.setup(ECHO_SEN_1,GPIO.IN)
#### Pines de Lectura Sensor 2 ####
TRIG_SEN_2 = 17
ECHO_SEN_2 = 18
GPIO.setup(TRIG_SEN_2,GPIO.OUT)
GPIO.setup(ECHO_SEN_2,GPIO.IN)
#### Pines de Lectura Sensor 1 ####
FORWARD_MOTOR_1 = 26 
BACKWARD_MOTOR_1 = 20
GPIO.setup(FORWARD_MOTOR_1,GPIO.OUT)
GPIO.setup(BACKWARD_MOTOR_1,GPIO.OUT)
#### Pines de Lectura Sensor 2 ####
FORWARD_MOTOR_2 = 19
BACKWARD_MOTOR_2 = 16
GPIO.setup(FORWARD_MOTOR_2,GPIO.OUT)
GPIO.setup(BACKWARD_MOTOR_2,GPIO.OUT)
#### CONFIGURACION INICIAL CONEXION FIREBASE ####
config = {
    "apiKey": "AIzaSyDKQPhib3hNN3TPYXQUxECi_XiucAK7AX4",
    "authDomain": "cc8-hardware.firebaseapp.com",
    "databaseURL": "https://cc8-hardware.firebaseio.com/",
    #"projectId": "cc8-hardware",
    "storageBucket": "cc8-hardware.appspot.com",
    #"messagingSenderId": "460720739788"
}
#### AUTENTICACION DEL USUARIO INICIO FIREBASE ####
firebase = pyrebase.initialize_app(config)
auth = firebase.auth()
usuario = 'cc8-user@gmail.com'
password = 12345678
user = auth.sign_in_with_email_and_password(usuario,password)
db = firebase.database()
#### INFORMACION A ENVIAR A FIREBASE ####
id = ""
url = ""
date = ""
sen1medicion = 0
sen2medicion = 0
sen1status = False
sen2status = False
sen1freq = 10000
sen2freq = 5000
sen1text = "Iniciando"
sen2text = "Iniciando"
motor1status = False
motor2status = False
motor1freq = 2000
motor2freq = 14000
motor1text = "Apagado"
motor2text = "Apagado"
resultskeysen1 = ""
resultskeysen2 = ""
resultskeymotor1 = ""
resultskeymotor2 = ""


lock = False

def infoformat():
    info = {"date":datetime.datetime.utcnow().isoformat(),
            "hardware":{
                "sen01": {
                    "tag": "Sensor adelante",
                    "type": "input"},
                "sen02": {
                    "tag": "Sensor atras",
                    "type": "input"},
                "motor01":{
                    "tag": "Motor izquierdo",
                    "type": "output"},
                "motor02":{
                    "tag": "Motor derecho",
                    "type": "output"}
                }
            }
    return info
def updateinfo(item):
    global sen1status
    global sen1freq
    global sen1text
    global sen2status
    global sen2freq
    global sen2text
    global motor1status
    global motor1freq
    global motor1text
    global motor2status
    global motor2freq
    global motor2text
    items = list(item)
    if(items[0]=="sen01"):
        info = str(items[1]).replace("{","").replace("}","").split(",")
        if(len(info)!=0):
            for x in range (0,len(info)):
                data = info[x].split(":")
                if(str(data[0].replace(" ","").replace("'",""))=="text"):
                    sen1text=str(data[1].replace(" ","").replace("'",""))
                elif(str(data[0].replace(" ","").replace("'",""))=="status"):
                    sen1status=str(data[1].replace(" ","").replace("'","").replace("f","F").replace("t","T"))
                elif(str(data[0].replace(" ","").replace("'",""))=="freq"):
                    sen1freq=(int)(data[1].replace(" ","").replace("'",""))
            db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("change").child("sen01").remove(user['idToken'])     
    elif(items[0]=="sen02"):
        info = str(items[1]).replace("{","").replace("}","").split(",")
        if(len(info)!=0):
            for x in range (0,len(info)):
                data = info[x].split(":")
                if(str(data[0].replace(" ","").replace("'",""))=="text"):
                    sen2text=str(data[1].replace(" ","").replace("'",""))
                elif(str(data[0].replace(" ","").replace("'",""))=="status"):
                    sen2status=str(data[1].replace(" ","").replace("'","").replace("f","F").replace("t","T"))
                elif(str(data[0].replace(" ","").replace("'",""))=="freq"):
                    sen2freq=(int)(data[1].replace(" ","").replace("'",""))
            db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("change").child("sen02").remove(user['idToken'])   
    elif(items[0]=="motor01"):
        info = str(items[1]).replace("{","").replace("}","").split(",")
        if(len(info)!=0):
            for x in range (0,len(info)):
                data = info[x].split(":")
                if(str(data[0].replace(" ","").replace("'",""))=="text"):
                    motor1text=str(data[1].replace(" ","").replace("'",""))
                elif(str(data[0].replace(" ","").replace("'",""))=="status"):
                    motor1status=str(data[1].replace(" ","").replace("'","").replace("f","F").replace("t","T"))
                elif(str(data[0].replace(" ","").replace("'",""))=="freq"):
                    motor1freq=(int)(data[1].replace(" ","").replace("'",""))
            #METODO CAMBIO MOTOR01
            motorinfo1()
            db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("change").child("motor01").remove(user['idToken'])   
    elif(items[0]=="motor02"):
        info = str(items[1]).replace("{","").replace("}","").split(",")
        if(len(info)!=0):
            for x in range (0,len(info)):
                data = info[x].split(":")
                if(str(data[0].replace(" ","").replace("'",""))=="text"):
                    motor2text=str(data[1].replace(" ","").replace("'",""))
                elif(str(data[0].replace(" ","").replace("'",""))=="status"):
                    motor2status=str(data[1].replace(" ","").replace("'","").replace("f","F").replace("t","T"))
                elif(str(data[0].replace(" ","").replace("'",""))=="freq"):
                    motor2freq=(int)(data[1].replace(" ","").replace("'",""))
            #METODO CAMBIO MOTOR02
            motorinfo2()
            db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("change").child("motor02").remove(user['idToken'])
        
def changeinfo():
    print ("change")
    while True:
        datadb = db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("change").get(user['idToken'])
        #db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("change").child("sen01").remove()
        #data = json.loads(sen1.val())
        datadb = datadb.val()
        items = list(datadb.items())
        if(len(items)>0):
            for x in range(0,len(items)):
                updateinfo(items[x])

def motor1infoformat():
    global motor1status
    global motor1freq
    global motor1text
    motor1info = {
        "date": datetime.datetime.utcnow().isoformat(),
        "status": motor1status,
        "freq": motor1freq,
        "text": motor1text
        }
    motor1info = motor1info
    return motor1info

def motor2infoformat():
    global motor2status
    global motor2freq
    global motor2text
    motor2info = {
        "date": datetime.datetime.utcnow().isoformat(),
        "status": motor2status,
        "freq": motor2freq,
        "text": motor2text
        }
    motor2info = motor2info
    return motor2info
    
    
def sen1infoformat():
    global sen1medicion
    global sen1status
    global sen1freq
    global sen1text
    sen1info = {
        "date": datetime.datetime.utcnow().isoformat(),
        "sensor": sen1medicion,
        "status": sen1status,
        "freq": sen1freq,
        "text": sen1text
        }
    print(sen1info)
    sen1info = sen1info
    return sen1info

def sen2infoformat():
    global sen2medicion
    global sen2status
    global sen2freq
    global sen2text
    sen2info = {
        "date": datetime.datetime.utcnow().isoformat(),
        "sensor": sen2medicion,
        "status": sen2status,
        "freq": sen2freq,
        "text": sen2text
        }
    print(sen2info)
    sen2info = sen2info
    return sen2info

def motor1freqfb():
    global motor1freq
    global lock
    while True:
        try:
            date = datetime.datetime.utcnow().isoformat()
            date = date.replace(".",":")
            #print(motor1freq)
            i = 0
            while(lock):
                i = 0        

            lock = True
            db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-motor01").child(date).set(motor1infoformat(),user['idToken'])
            lock = False
            time.sleep(motor1freq/1000)
        except KeyboardInterrupt:
            print("key")

def motor2freqfb():
    global motor2freq
    global lock
    while True:
        date = datetime.datetime.utcnow().isoformat()
        date = date.replace(".",":")
        #print(motor2freq)
        i = 0
        while(lock):
            i = 0

        lock = True
        db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-motor02").child(date).set(motor2infoformat(),user['idToken'])
        lock = False
        time.sleep(motor2freq/1000)

def motorinfo1():
    global motor1status
    global motor1text
    #while True:
    date = datetime.datetime.utcnow().isoformat()
    date = date.replace(".",":")
    if(motor1status=="False"):
        GPIO.output(FORWARD_MOTOR_1,GPIO.LOW)
        GPIO.output(BACKWARD_MOTOR_1,GPIO.LOW)
        motor1text="Detenido"
        db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-motor01").child(date).set(motor1infoformat(),user['idToken'])
    else:
        if(motor1text.upper()=="ADELANTE"):
            GPIO.output(FORWARD_MOTOR_1,GPIO.HIGH)
            db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-motor01").child(date).set(motor1infoformat(),user['idToken'])
        elif(motor1text.upper()=="ATRAS"):
            GPIO.output(BACKWARD_MOTOR_1,GPIO.HIGH)
            db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-motor01").child(date).set(motor1infoformat(),user['idToken'])
        else:
            db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-motor01").child(date).set(motor1infoformat(),user['idToken'])

def motorinfo2():
    global motor2status
    global motor2text
    #while True:
    date = datetime.datetime.utcnow().isoformat()
    date = date.replace(".",":")
    if(motor2status=="False"):
        GPIO.output(FORWARD_MOTOR_2,GPIO.LOW)
        GPIO.output(BACKWARD_MOTOR_2,GPIO.LOW)
        motor1text="Detenido"
        db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-motor02").child(date).set(motor2infoformat(),user['idToken'])
    else:
        if(motor2text.upper()=="ADELANTE"):
            GPIO.output(FORWARD_MOTOR_1,GPIO.HIGH)
            db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-motor02").child(date).set(motor2infoformat(),user['idToken'])
        elif(motor2text.upper()=="ATRAS"):
            GPIO.output(BACKWARD_MOTOR_1,GPIO.HIGH)
            db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-motor02").child(date).set(motor2infoformat(),user['idToken'])
        else:
            db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-motor02").child(date).set(motor2infoformat(),user['idToken'])

            
def motor1():
    global motor1status
    global motor1text
    while True:
        date = datetime.datetime.utcnow().isoformat()
        date = date.replace(".",":")
        if(motor1status=="False"):
            GPIO.output(FORWARD_MOTOR_1,GPIO.LOW)
            GPIO.output(BACKWARD_MOTOR_1,GPIO.LOW)
            motor1text="Detenido"
            db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-motor01").child(date).set(motor1infoformat(),user['idToken'])
        else:
            if(motor1text.upper()=="ADELANTE"):
                GPIO.output(FORWARD_MOTOR_1,GPIO.HIGH)
                db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-motor01").child(date).set(motor1infoformat(),user['idToken'])
            elif(motor1text.upper()=="ATRAS"):
                GPIO.output(BACKWARD_MOTOR_1,GPIO.HIGH)
                db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-motor01").child(date).set(motor1infoformat(),user['idToken'])
            else:
                db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-motor01").child(date).set(motor1infoformat(),user['idToken'])

def motor2():
    global motor2status
    global motor2text
    while True:
        date = datetime.datetime.utcnow().isoformat()
        date = date.replace(".",":")
        if(motor2status=="False"):
            GPIO.output(FORWARD_MOTOR_2,GPIO.LOW)
            GPIO.output(BACKWARD_MOTOR_2,GPIO.LOW)
            motor1text="Detenido"
            db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-motor02").child(date).set(motor2infoformat(),user['idToken'])
        else:
            if(motor2text.upper()=="ADELANTE"):
                GPIO.output(FORWARD_MOTOR_1,GPIO.HIGH)
                db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-motor02").child(date).set(motor2infoformat(),user['idToken'])
            elif(motor2text.upper()=="ATRAS"):
                GPIO.output(BACKWARD_MOTOR_1,GPIO.HIGH)
                db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-motor02").child(date).set(motor2infoformat(),user['idToken'])
            else:
                db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-motor02").child(date).set(motor2infoformat(),user['idToken'])

            
            
            

def sensor1(TRIG,ECHO):
    global sen1medicion
    global sen1freq
    global sen1status
    global sen1text
    while True:
        GPIO.output(TRIG,False)
        time.sleep(sen1freq/2000) #1
        GPIO.output(TRIG,True)
        time.sleep(sen1freq/2000)
        GPIO.output(TRIG,False)
        pulse_start = 0
        while GPIO.input(ECHO) == 0:
            pulse_start = time.time()
            pulse_end = 0
        while GPIO.input(ECHO) == 1:
            pulse_end = time.time()
            pulse_duration = pulse_end -pulse_start
        sen1medicion = pulse_duration * 17150
        sen1medicion = round(sen1medicion,0)
        print(sen1medicion)
        date = datetime.datetime.utcnow().isoformat()
        date = date.replace(".",":")
        if sen1medicion > 30:
            sen1status = False
            sen1text = "Lejos"
            db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-sen01").child(date).set(sen1infoformat(),user['idToken'])
        else:
            sen1status = True
            sen1text = "Peligro"
            db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-sen01").child(date).set(sen1infoformat(),user['idToken'])
        
def sensor2(TRIG,ECHO):
    global sen2medicion
    global sen2freq
    global sen2status
    global sen2text
    while True:
        GPIO.output(TRIG,False)
        time.sleep(sen2freq/2000) #1
        GPIO.output(TRIG,True)
        time.sleep(sen2freq/2000)
        GPIO.output(TRIG,False)
        pulse_start = 0
        while GPIO.input(ECHO) == 0:
            pulse_start = time.time()
            pulse_end = 0
        while GPIO.input(ECHO) == 1:
            pulse_end = time.time()
            pulse_duration = pulse_end -pulse_start
        sen2medicion = pulse_duration * 17150
        sen2medicion = round(sen2medicion,0)
        print(sen2medicion)
        date = datetime.datetime.utcnow().isoformat()
        date = date.replace(".",":")
        if sen2medicion > 30:
            sen2status = False
            sen2text = "Lejos"
            db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-sen02").child(date).set(sen1infoformat(),user['idToken'])
        else:
            sen2status = True
            sen2text = "Peligro"



db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-sen02").child(date).set(sen1infoformat(),user['idToken'])    
#print(infoformat())
results = db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("info").update(infoformat(),user['idToken'])
#print(results)
date = datetime.datetime.utcnow().isoformat()
date = date.replace(".",":")

resultskeysen1 = db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-sen01").child(date).set(sen1infoformat(),user['idToken'])
resultskeysen2 = db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-sen02").child(date).set(sen2infoformat(),user['idToken'])
resultskeysen1 = db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-motor01").child(date).set(motor1infoformat(),user['idToken'])
resultskeysen2 = db.child("Yn5fRPSQzPNtGYb7iEv7uIIWJkR2").child("data-motor02").child(date).set(motor2infoformat(),user['idToken'])

sen1thread = threading.Thread(target=sensor1(TRIG_SEN_1,ECHO_SEN_1))
sen2thread = threading.Thread(target=sensor2(TRIG_SEN_2,ECHO_SEN_2))
motor1thread = threading.Thread(target=motor1)
motor2thread = threading.Thread(target=motor2)
changethread = threading.Thread(target=changeinfo)
motor1fbthread = threading.Thread(target=motor1freqfb)
motor2fbthread = threading.Thread(target=motor2freqfb)
sen1thread.start()
sen2thread.start()
change.start()
motor1thread.start()
motor2thread.start()
motor1fbthread.start()
motor2fbthread.start()
while True:
    try:
        print ("Prueba")
        
        
        #print(resultskeysen1)
        
    except KeyboardInterrupt:
        break

GPIO.cleanup()
