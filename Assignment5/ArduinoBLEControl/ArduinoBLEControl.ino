/*********************************************************************
  * Laura Arjona. UW EE P 523. SPRING 2020
  * Example of simple interaction beteween Adafruit Circuit Playground
  * and Android App. Communication with BLE - uart
*********************************************************************/
#include <Arduino.h>
#include <SPI.h>
#include "Adafruit_BLE.h"
#include "Adafruit_BluefruitLE_SPI.h"
#include "Adafruit_BluefruitLE_UART.h"
#include <Adafruit_CircuitPlayground.h>

#include "BluefruitConfig.h"

#if SOFTWARE_SERIAL_AVAILABLE
  #include <SoftwareSerial.h>
#endif


// Strings to compare incoming BLE messages
String start = "start";
String red = "red";
String readtemp = "readtemp";
String stp = "stop";
String cancelNotice = "cancelnotice";


int  sensorTemp = 0;

unsigned long startMoveTime;
unsigned long now;
String curData = "";


//Parameters to detect the board move
float threshold = 0.2;
unsigned long moveInterval = 3000;
float startAccelMag = 0;
unsigned long lastMoveTime = 0;
unsigned long rightButtonTime = 0;
unsigned long leftButtonTime = 0;
boolean right = false;
boolean left = false;



/*=========================================================================
    APPLICATION SETTINGS
    -----------------------------------------------------------------------*/
    #define FACTORYRESET_ENABLE         0
    #define MINIMUM_FIRMWARE_VERSION    "0.6.6"
    #define MODE_LED_BEHAVIOUR          "MODE"
/*=========================================================================*/

// Create the bluefruit object, either software serial...uncomment these lines

Adafruit_BluefruitLE_UART ble(BLUEFRUIT_HWSERIAL_NAME, BLUEFRUIT_UART_MODE_PIN);

/* ...hardware SPI, using SCK/MOSI/MISO hardware SPI pins and then user selected CS/IRQ/RST */
// Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_CS, BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);

/* ...software SPI, using SCK/MOSI/MISO user-defined SPI pins and then user selected CS/IRQ/RST */
//Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_SCK, BLUEFRUIT_SPI_MISO,
//                             BLUEFRUIT_SPI_MOSI, BLUEFRUIT_SPI_CS,
//                             BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);


// A small helper to show errors on the serial monitor
void error(const __FlashStringHelper*err) {
  Serial.println(err);
  while (1);
}



void moveDetector() {
   
   //Get the acceleration magnitude
  startAccelMag = getAccelMagnitude();

  //Sit here until right button press or board shaked
  while (true) { // may need to delete
    if (isMove()) {
      //Blink the neopixel pixel
      //blink_new_grame(0, 3);

      startMoveTime = millis();
      
      if (curData != "detected") {
          char output[8];
          String data = "detected";
          curData = data;
          Serial.println(data);
          data.toCharArray(output,8);
          ble.print(data);   
          delay(500);       
      }
      break;
    } else {
//      delay(20);
      //delay(DEBOUNCE); //??
    }
  }

  
}



  float getAccelMagnitude() {
  float X = 0;
  float Y = 0;
  float Z = 0;

  float dX = 0;
  float dY = 0;
  float dZ = 0;


  for (int i = 0; i < 10; ++i) {
    dX = CircuitPlayground.motionX();
    dY = CircuitPlayground.motionY();
    dZ = CircuitPlayground.motionZ();

    X += dX;
    Y += dY;
    Z += dZ;
  }

  X /= 10;
  Y /= 10;
  Z /= 10;

  return sqrt(X * X + Y * Y + Z * Z);
}




boolean isMove() {
  float currentAccelMag = getAccelMagnitude();
  if ((currentAccelMag - startAccelMag) < threshold) {
    return false;
  }

  now = millis();
  if ((now - lastMoveTime) < moveInterval) {
    // don't give shake gestures too frequently
    return false;
  }

  Serial.println("Move detected");
  lastMoveTime = now;
  startAccelMag = currentAccelMag;
  
  return true;
}





void setup(void)
{
  CircuitPlayground.begin();
 
  
  

  Serial.begin(115200);

  /* Initialise the module */
  Serial.print(F("Initialising the Bluefruit LE module: "));

  if ( !ble.begin(VERBOSE_MODE) )
  {
    error(F("Couldn't find Bluefruit, make sure it's in CoMmanD mode & check wiring?"));
  }
  Serial.println( F("OK!") );

  if ( FACTORYRESET_ENABLE )
  {
    /* Perform a factory reset to make sure everything is in a known state */
    Serial.println(F("Performing a factory reset: "));
    if ( ! ble.factoryReset() ){
      error(F("Couldn't factory reset"));
    }
  }

  /* Disable command echo from Bluefruit */
  ble.echo(false);

  Serial.println("Requesting Bluefruit info:");
  /* Print Bluefruit information */
  ble.info();

  Serial.println(F("Please use Adafruit Bluefruit LE app to connect in UART mode"));
  Serial.println(F("Then Enter characters to send to Bluefruit"));
  Serial.println();

  ble.verbose(false);  // debug info is a little annoying after this point!

  /* Wait for connection */
  while (! ble.isConnected()) {
      delay(500);
  }
Serial.println("CONECTED:");
  Serial.println(F("******************************"));

  // LED Activity command is only supported from 0.6.6
  if ( ble.isVersionAtLeast(MINIMUM_FIRMWARE_VERSION) )
  {
    // Change Mode LED Activity
    Serial.println(F("Change LED activity to " MODE_LED_BEHAVIOUR));
    ble.sendCommandCheckOK("AT+HWModeLED=" MODE_LED_BEHAVIOUR);
  }

  // Set module to DATA mode
  Serial.println( F("Switching to DATA mode!") );
  ble.setMode(BLUEFRUIT_MODE_DATA);

  Serial.println(F("******************************"));

  CircuitPlayground.setAccelRange(LIS3DH_RANGE_8_G);


  for(int i = 0; i < 11; i++){
      CircuitPlayground.setPixelColor(i,0, 255, 0);
    }
      moveDetector();
 
  CircuitPlayground.setPixelColor(20,20,200,20);
 
  delay(1000);
}
/**************************************************************************/
/*!
   Constantly poll for new command or response data
*/
/**************************************************************************/
void loop(void)
{
  // Save received data to string
  String received = "";
  while ( ble.available() )
  {
    int c = ble.read();
    Serial.print((char)c);
    received += (char)c;
        delay(50);
  }


  float X = 0;
  float Y = 0;
  float Z = 0;

  float dX = 0;
  float dY = 0;
  float dZ = 0;
  float res = 0;

  for (int i = 0; i < 10; ++i) {
    dX = CircuitPlayground.motionX();
    dY = CircuitPlayground.motionY();
    dZ = CircuitPlayground.motionZ();

    X += dX;
    Y += dY;
    Z += dZ;
  }

  X /= 10;
  Y /= 10;
  Z /= 10;

  res = sqrt(X * X  + Z * Z);
  Serial.println(res);


     

//  wiating info from android to cancel
//

  if (curData == "detected") {
        //CircuitPlayground.playTone(500,100);

        
        if (cancelNotice == received) {
      //CircuitPlayground.clearPixels();
//      change color to green
//      detect shake again!
            for(int i = 0; i < 11; i++){
              CircuitPlayground.setPixelColor(i,0, 255, 0);
              }
             curData = "";
            moveDetector();
        } else {
            now = millis();  
            if (now - startMoveTime > 8000) {
               //sent notice to Android make a call
                 //sent notice to android
                Serial.println("Read time expired"); 
                delay(10);
      
                //Send data to Android Device
                  
                char output[8];
                String data = "expired";
                curData = data;
                Serial.println(data);
                data.toCharArray(output,8);
                ble.print(data);
                delay(500);
            } else {
                  if (CircuitPlayground.rightButton()) {
                    right = true;
                    
                   }
                           
                  if (right && CircuitPlayground.leftButton()) {
                    char output[8];
                    String data = "dismiss";
                    Serial.println(data);
                    data.toCharArray(output,8);
                    ble.print(data);
                    delay(50);
  
                    for(int i = 0; i < 11; i++){
                      CircuitPlayground.setPixelColor(i,0, 255, 0);
                      }
                     curData = "";
                     right = false;
                     //left = false;
                     delay(1000);
                     moveDetector();
                    }

                              //blink yellow color
                  for(int i = 0; i < 11; i++){
                      CircuitPlayground.setPixelColor(i,255, 255, 0);
                  }
                  delay(50);
                  for (int i = 0; i < 11; i++) {
                    CircuitPlayground.setPixelColor(i,0, 0, 0);
                    }
                   delay(50);
                                           
                }
        }
                
              
              
    } else if (curData = "expired") {
        CircuitPlayground.playTone(1500,100);
                           //change light color to red
        for(int i = 0; i < 11; i++){
           CircuitPlayground.setPixelColor(i,255, 0, 0);
           }
        delay(100);
          for (int i = 0; i < 11; i++) {
            CircuitPlayground.setPixelColor(i,0, 0, 0);
            }
        delay(100);
      } 
         
        
//  if (isMove) {
//      if (cancelNotice == received) {
//      //CircuitPlayground.clearPixels();
////      change color to green
////      detect shake again!
//      for(int i = 0; i < 11; i++){
//          CircuitPlayground.setPixelColor(i,0, 255, 0);
//        }
//       isMove = false;
//       moveDetector();
//       
//      } else {
//      
//      //detect time
//        now = millis();
//          if (now - startMoveTime > 8000) {
//       
//            CircuitPlayground.playTone(1500,100);
//
//  
//            //sent notice to Android make a call
//                 //sent notice to android
//            Serial.println("Read time expired"); 
//            delay(10);
//      
//             //Send data to Android Device
//             if (curData != "timeExpired") {
//                 //change light color to red
//                for(int i = 0; i < 11; i++){
//                   CircuitPlayground.setPixelColor(i,255, 0, 0);
//                  }
//                  
//                char output[8];
//                String data = "timeExpired";
//                curData = data;
//                Serial.println(data);
//                data.toCharArray(output,8);
//                ble.print(data);
//                delay(500);
//                
//              }
//
//              
//            } else {
//            
//                CircuitPlayground.playTone(500,100);
//                 //sent notice to android
//                     Serial.println("Read isMoving"); 
//                      delay(10);
//                
//                   //Send data to Android Device
//                   if (curData != "moveDetected") {
//                          //change light color to yellow
//                      for(int i = 0; i < 11; i++){
//                        CircuitPlayground.setPixelColor(i,255, 255, 0);
//                        }
//                      char output[8];
//                      String data = "moveDetected";
//                      curData = data;
//                      Serial.println(data);
//                      data.toCharArray(output,8);
//                      ble.print(data);   
//                      delay(500);                 
//                    }
//
//      
//             }
//       }
//    }

  if(red == received){
    Serial.println("RECEIVED RED!!!!"); 
       for(int i = 0; i < 11; i++){
      CircuitPlayground.setPixelColor(i,221, 44, 44);
    }
    delay(50);
  }
 
  else if(readtemp == received){
       
    sensorTemp = CircuitPlayground.temperature(); // returns a floating point number in Centigrade
    Serial.println("Read temperature sensor"); 
    delay(10);

   //Send data to Android Device
    char output[8];
    String data = "";
    data += sensorTemp;
    Serial.println(data);
    data.toCharArray(output,8);
    ble.print(data);
  }
 
  else if (stp == received){
      CircuitPlayground.clearPixels();

    }
    
  }

 
