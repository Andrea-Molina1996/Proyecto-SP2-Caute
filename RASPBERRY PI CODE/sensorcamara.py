import RPi.GPIO as GPIO
from picamera import PiCamera
from subprocess import call #Se utiliza para la interrupcion de teclado
import time
GPIO.setmode(GPIO.BCM)
camera = PiCamera()
camera.rotation = 180
TRIG = 23
ECHO = 24

print ("Distance measurement in progress")

GPIO.setup(TRIG,GPIO.OUT)
GPIO.setup(ECHO,GPIO.IN)

pulse_start = 0
pulse_end = 0
contador = 0
while True:
    try:
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
            print ("Distance:", distance-0.5,"cm")
            contador = contador+1
            for i in range(10):
                time.sleep(0.5)
                fecha = (time.strftime("%d-%m-%Y"))
                hora = (time.strftime("%H:%M:%S"))
                path = '/home/pi/Caute-Inc/Images/'+fecha+'_'+hora+'.jpg' #Por si acaso 'caute-%s'%i+
                print(path)
                camera.capture(path)
        elif distance > 10 and distance < 21:
            print ("Distance:", distance-0.5,"cm")
            print ("Alerta auto muy cerca :D")
        elif distance > 20 and distance < 400:
            print ("Distance:", distance-0.5,"cm")
            print ("Rango Valido pero no me interesa xD")
        else:
            print ("Out of Range")
    except KeyboardInterrupt:
        break
GPIO.cleanup()
