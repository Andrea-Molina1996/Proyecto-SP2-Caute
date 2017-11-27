from pyrebase import pyrebase
import time
import threading
import time
import cv2

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
usuario = 'mandre9612@gmail.com'
password = Mianmoba
user = auth.sign_in_with_email_and_password(usuario,password)
db = firebase.database()
##########################################################################
#################### Conexion Storage Firebase ###########################
##########################################################################
storage = firebase.storage()

def diffImg(t0, t1, t2):
  d1 = cv2.absdiff(t2, t1)
  d2 = cv2.absdiff(t1, t0)
  return cv2.bitwise_and(d1, d2)

cam = cv2.VideoCapture(0)

winName = "Movement Indicator"
cv2.namedWindow(winName, cv2.CV_WINDOW_AUTOSIZE)

# Read three images first:
t_minus = cv2.cvtColor(cam.read()[1], cv2.COLOR_RGB2GRAY)
t = cv2.cvtColor(cam.read()[1], cv2.COLOR_RGB2GRAY)
t_plus = cv2.cvtColor(cam.read()[1], cv2.COLOR_RGB2GRAY)

while True:
  cv2.imshow( winName, diffImg(t_minus, t, t_plus) )

  # Read next image
  t_minus = t
  print (t_minus)
  t = t_plus
  print (t)
  t_plus = cv2.cvtColor(cam.read()[1], cv2.COLOR_RGB2GRAY)
  print (t_plus)
  key = cv2.waitKey(10)
  if key == 27:
    cv2.destroyWindow(winName)
    break

print "Goodbye"
