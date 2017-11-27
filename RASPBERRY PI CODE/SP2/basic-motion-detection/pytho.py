# USAGE
# python motion_detector.py
# python motion_detector.py --video videos/example_01.mp4

# import the necessary packages
from pyrebase import pyrebase
import time
import random

##########################################################################
############## Configuracion Inicial Conexion Firebase ###################
##########################################################################
config = {
  "apiKey": "AIzaSyCQb5Tl1CsyQ9hgIQbmZskQyYVsJ1hVRqw",
  "authDomain": "caute-system.firebaseapp.com",
  "databaseURL": "https://caute-system.firebaseio.com",
  "storageBucket": "caute-system.appspot.com"
#projectId: "caute-a34d7",
#messagingSenderId: "486985772902"
}
##########################################################################
############ Autenticacion del Usuario Inicio Firebase ###################
##########################################################################
firebase = pyrebase.initialize_app(config)
auth = firebase.auth()
usuario = 'amolina96@hotmail.com'
password ="Mianmoba#96"
user = auth.sign_in_with_email_and_password(usuario,password)
db = firebase.database()
##########################################################################
#################### Conexion Storage Firebase ###########################
##########################################################################
storage = firebase.storage()


band = True
# loop over the frames of the video
while True:
        fecha = (time.strftime("%d-%m-%Y"))
        hora = (time.strftime("%H:%M:%S"))
        data = {"Valor":random.randint(0,250),
                "Seriedad":3,
                "Posicion_Camara":"Front",
                "ImgUrl":None,
                "VidUrl":None,
                "Probabilidad": "Person",
                "Fecha": fecha,
                "Hora":hora,
                "Notificada":False}
        results = db.child("G3bTAg0lqdba8g4Pr8aMmNflx2B3").child("Alerts").child("NOV").push(data,user['idToken'])
        time.sleep(50000)
# cleanup the camera and close any open windows
