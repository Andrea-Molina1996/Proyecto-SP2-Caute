import sys
import RPi.GPIO as GPIO
#### TIME & THREADS ####
import time


#### Pines de Lectura Sensor 1 ####
FORWARD_MOTOR_1 = 26 
BACKWARD_MOTOR_1 = 20
GPIO.setmode(GPIO.BCM)
#GPIO.cleanup()
GPIO.setup(FORWARD_MOTOR_1,GPIO.OUT)
GPIO.setup(BACKWARD_MOTOR_1,GPIO.OUT)

def forward(x):
    print("move front")
    GPIO.output(BACKWARD_MOTOR_1,GPIO.LOW)
    GPIO.output(FORWARD_MOTOR_1,GPIO.HIGH)
    
    time.sleep(x)
    GPIO.output(FORWARD_MOTOR_1,GPIO.LOW)

def backward(x):
    print("move back")
    GPIO.output(FORWARD_MOTOR_1,GPIO.LOW)
    GPIO.output(BACKWARD_MOTOR_1,GPIO.HIGH)
    
    time.sleep(x)
    GPIO.output(BACKWARD_MOTOR_1,GPIO.LOW)


while(1):
    try:
        forward(5)
        time.sleep(2)
        backward(5)
    except KeyboardInterrupt:
        break
GPIO.cleanup()
