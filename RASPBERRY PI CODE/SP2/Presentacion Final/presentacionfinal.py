from clarifai.rest import ClarifaiApp
from clarifai.rest import Image as ClImage
import threading
import argparse
import datetime
import imutils
import time
import cv2



#Clarifai Config
app = ClarifaiApp()
model = app.models.get('general-v1.3')

#Image config
ap = argparse.ArgumentParser()
ap.add_argument("-a","--min-area", type=int, default=1500, help="El area minima")
ap.add_argument("-c","--cant-camara",type = int, default=2,help="Cantidad de camaras, maximo 4")
args = vars(ap.parse_args())
video = cv2.VideoWriter('video.jpeg',cv2.cv.CV_FOURCC('C','J','P','G'),1.0,(500,500))
cam0 = 0
cam1 = 1
cam2 = 2
cam3 = 3
fecha_y_hora_para_data_firebase = "hola.jpg"
seriedad = 1
distancia = 0
alerta = ""
imgUrl = ""

def camara(num):
    camara = cv2.VideoCapture(num)
    time.sleep(0.25)
    return camara

def confinit():
    global cam0
    global cam1
    global cam2
    global cam3
    if(args["cant_camara"]==2):
        print ("Camara Frontal y Trasera")
        try:
            cam0 = camara(0)
            cam1 = camara(1)
        except KeyboardInterrupt:
            print (key)
    elif(args["cant_camara"]==3):
        print ("Camara Frontal, Trasera derecha e izquierda")
        cam = camara(0)
        cam = camara(1)
        cam = camara(2)
    elif(args["cant_camara"]==4):
        print ("Camara Frontal derecha e izquierda, Trasera derecha e izquierda")
        cam = camara(0)
        cam = camara(1)
        cam = camara(2)
        cam = camara(3)

def clarifaim(imagen):
    #ruta = '/home/pi/Desktop/SP2/Presentacion Final/Images/'+image
    image = ClImage(file_obj=open(imagen,'rb'))
    response = model.predict([image])
    concepts = response['outputs'][0]['data']['concepts']
    thebest = ""
    for concept in concepts:
        if(concept['value']>0.96):
            print concept['name']
            thebest += concept['name']+","
    return thebest

firstFrame = None
def framesvideo(cam,num):
    global firstFrame
    global distancia
    global seriedad
    global fecha_y_hora_para_data_firebase
    global alerta
    (grabbed, frame)=cam.read()
    text = "Iniziando"
    
    #if not grabbed:
    #    break
    
    frame = imutils.resize(frame, width=500)
    gray = cv2.cvtColor(frame,cv2.COLOR_BGR2GRAY)
    gray = cv2.GaussianBlur(gray,(21,21),0)
    
    if firstFrame is None:
        firstFrame =  gray
        #continue

    frameDelta = cv2.absdiff(firstFrame,gray)
    #print frameDelta
    thresh = cv2.threshold(frameDelta, 25, 255, cv2.THRESH_BINARY)[1]
    #print "Thresh \n"
    #print thresh
    thresh = cv2.dilate(thresh, None, iterations=2)    
    #(cnts, _) = cv2.findContours(thresh.copy(),cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    (cnts, _) = cv2.findContours(thresh.copy(), cv2.RETR_EXTERNAL,
		cv2.CHAIN_APPROX_SIMPLE)
    for c in cnts:
        if cv2.contourArea(c)>args["min_area"]:
            
            ### Se desplega la alerta  AQUI
            (x,y,w,h) = cv2.boundingRect(c)
            cv2.rectangle(frame,(x,y),(x+w,y+h),(0,255,0),2)
            if(y+h>175 and y+h < 225):
                fecha = (time.strftime("%d-%m-%Y"))
                hora = (time.strftime("%H:%M:%S"))
                fecha_y_hora_para_data_firebase = fecha+"_"+hora+".jpg"
                # Getting the month id
                #now = datetime.datetime.now()
                #dateFormat = now.strftime('%d-%m-%Y')
                #timeFormat = now.strftime('%H:%M:%S')
                #nowMonth = time.strftime("%b")
                #Obteniendo la distancia
                distancia = 1.20
                seriedad = 3
                ruta = '/home/pi/Desktop/SP2/Presentacion Final/Images/'+fecha_y_hora_para_data_firebase
                cv2.imwrite(fecha_y_hora_para_data_firebase,frame)
                campos ="back"
                if(num==0):
                    campos="front"
                #print "clarifai"
                #time.sleep(1)
                objects = clarifaim(fecha_y_hora_para_data_firebase)
                print(objects)

                # before the 1 hour expiry:
                #user = auth.refresh(user['refreshToken'])
                # Almacenando la imagen
                # store an image as an admin account
                #image_name = +"_"+timeFormat+".jpg"
                #storage.child("images/"+image_name).put("example.jpg")
                #imgUrl = storage.child("images/example.jpg").get_url(token)
                alerta={
                    "cameraPosition":campos,
                    "date":fecha,
                    "distance":distancia,
                    "imgUrl": imgUrl,
                    "vidUrl": None,
                    "notified":False,
                    #"objects":objects,
                    "seriosness":seriedad,
                    "time":hora
                    }
                #print alerta
                # Pass the user's idToken to the push method
                #results = db.child(userID).child("Alerts").child(nowMonth).push(alerta, user['idToken'])
                #alertID = results['name']
                #print("user's id token> : %s" % results)
                #print("token value %s" % results['name'])

                #data to save for the gallery
                #dataGallery = {
                #    "imgName" : image_name,
                #    "imgUrl" : imgUrl,
                #    "alertID" : alertID
                #    }
                #results = db.child(userID).child("Gallery").child(nowMonth).push(dataGallery, user['idToken'])
                
            if(y+h>150 and y+h < 250):
                fecha = (time.strftime("%d-%m-%Y"))
                hora = (time.strftime("%H:%M:%S"))
                fecha_y_hora_para_data_firebase = fecha+"_"+hora+".jpg"
                distancia = 1
                seriedad = 4
                cv2.imwrite(fecha_y_hora_para_data_firebase,frame)
                print "clarifai"
                print clarifaim(fecha_y_hora_para_data_firebase)
            if(y+h>100 and y+h < 300):
                fecha = (time.strftime("%d-%m-%Y"))
                hora = (time.strftime("%H:%M:%S"))
                fecha_y_hora_para_data_firebase = fecha+" "+hora+".jpg"
                ruta = '/home/pi/Desktop/SP2/Presentacion Final/Images/'+fecha_y_hora_para_data_firebase
                distancia = 0.40
                seriedad = 5
                cv2.imwrite(fecha_y_hora_para_data_firebase,frame)
                print "clarifai"
                print clarifaim(fecha_y_hora_para_data_firebase)
                
                
            text = "Movimiento"

    name = "Webcam "+str(num)+":{}"
    cv2.putText(frame,name.format(text),(10,20),cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 255), 2)
    cv2.putText(frame, datetime.datetime.now().strftime("%A %d %B %Y %I:%M:%S%p"),(10, frame.shape[0] - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.35, (0, 0, 255), 1)
    namevista = "Vista"+str(num)
    cv2.imshow(namevista, frame)
    key = cv2.waitKey(1) & 0xFF


        
            
confinit()

while True:
    try:
        if(args["cant_camara"]==2):
            framesvideo(cam0,0)
            framesvideo(cam1,1)
        else:
            print ("En proceso")
    except KeyboardInterrupt:
        break
