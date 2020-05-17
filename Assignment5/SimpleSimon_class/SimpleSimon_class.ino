/////////////////////////////////////////////////////
// Circuit Playground Simple Simon
/////////////////////////////////////////////////////
//CLASS ACTIVITY LECTURE 6. May 7th 2020
////////////////////////////////////////

#include <Adafruit_CircuitPlayground.h>
//Game configuration variables
#define MAX_SEQUENCE    31 //The maximun allowed sequence lenght
#define GUESS_TIMEOUT   3000
//////////////////////////////////////


//Implementation parameters
#define NO_BUTTON       -1
#define FAILURE_TONE    100
#define SEQUENCE_DELAY  800
#define DEBOUNCE        250
#define CAP_THRESHOLD   10 //Sensitivity for the user board press

uint8_t simonSequence[MAX_SEQUENCE];
uint8_t sequenceLength;
uint8_t currentStep;
unsigned long startGuessTime;
int8_t guess;
bool game_won = false;
bool game_lost = false;


//Parameters to detect the board shake
float threshold = 6;
unsigned long shakeInterval = 3000;
float startAccelMag = 0;
unsigned long lastShakeTime = 0;


struct button {
  uint8_t capPad[2];
  uint8_t pixel[3];
  uint32_t color;
  uint16_t freq;
} simonButton[] = {
  { {3, 2},    {0, 1, 2},  0x00FF00,   415 }, // GREEN
  { {0, 1},    {2, 3, 4},  0xFFFF00,   252 }, // YELLOW
  { {12, 6},  {5, 6, 7},  0x0000FF,   209 }, // BLUE
  { {9, 10},  {7, 8, 9},  0xFF0000,   310 }, // RED
};

///////////////////////////////////////////////////////////////////////////////

//  ***** TO DO:  GET THE MAGNITUDE OF THE ACCELERATION VECTOR *************
float getAccelMagnitude() {
 
}

///////////////////////////////////////////////////////////////////////////////


//  ***** TO DO:  DETECT SHAKE EVENT *************
boolean isShake() {
  float currentAccelMag = getAccelMagnitude();
 
}

///////////////////////////////////////////////////////////////////////////////
void newGame() {
  
  CircuitPlayground.clearPixels();
  CircuitPlayground.setPixelColor(0, 0xFFFFFF);
  
  //Get the acceleration magnitude
  startAccelMag = getAccelMagnitude();

 //  ***** TO DO:  DETECT SHAKE EVENT *************
  //Sit here until right button press OR board shaked
  //while ( NOT button press OR NOT board shaked) { }
  while(true){} //CHANGE THE "TRUE" CONDITION TO THE PREVIOUS CONDITION
  
  startGame();
  
}

///////////////////////////////////////////////////////////////////////////////
void startGame(){
  randomSeed(millis());
  sequenceLength = 8;
  // Populate the game sequence
  for (int i = 0; i < sequenceLength; i++) {
    simonSequence[i] = random(4);
  }

  // We start with the first step in the sequence
  currentStep = 1;
}
///////////////////////////////////////////////////////////////////////////////
void blink_new_grame(int pixel, int ntimes) {
  for (int i = 0; i < ntimes; i++) {
    CircuitPlayground.setPixelColor(pixel, 0xFFFFFF);
    delay(500);
    CircuitPlayground.setPixelColor(pixel, 0x000000);
    delay(500);
  }
}

///////////////////////////////////////////////////////////////////////////////
void indicateButton(uint8_t b, uint16_t duration) {
  CircuitPlayground.clearPixels();
  for (int p = 0; p < 3; p++) {
    CircuitPlayground.setPixelColor(simonButton[b].pixel[p], simonButton[b].color);
  }
  CircuitPlayground.playTone(simonButton[b].freq, duration);
  CircuitPlayground.clearPixels();
}


///////////////////////////////////////////////////////////////////////////////
void showSequence() {
  uint16_t toneDuration = 420;

  // Play back sequence up to current step
  for (int i = 0; i < currentStep; i++) {
    delay(50);
    indicateButton(simonSequence[i], toneDuration);
  }
}

///////////////////////////////////////////////////////////////////////////////
uint8_t getUserTurn() {
  for (int b = 0; b < 4; b++) {
    for (int p = 0; p < 2; p++) {
      if (CircuitPlayground.readCap(simonButton[b].capPad[p]) > CAP_THRESHOLD) {
        indicateButton(b, DEBOUNCE);
        return b;
      }
    }
  }
  return NO_BUTTON;
}

///////////////////////////////////////////////////////////////////////////////

//  ***** TO DO:  visual indication that game was LOST *************
void gameLost() {
  //visual indication that game was lost
  
  newGame();

}

///////////////////////////////////////////////////////////////////////////////
//  ***** TO DO:  visual indication that game was WON *************
void gameWon() {

    //visual indication that game was lost
  // ***** TO DO *************
  newGame();

}


///////////////////////////////////////////////////////////////////////////////
void setup() {
  // Initialize the Circuit Playground
  CircuitPlayground.begin();
    
  CircuitPlayground.setAccelRange(LIS3DH_RANGE_8_G);
  CircuitPlayground.clearPixels();
  CircuitPlayground.setPixelColor(0, 0xFFFFFF);

  Serial.begin(9600);

  startGame(); // this will start the game automatically 
              //change this to newGame() once you detect the right button press and the shake event
              //to start the game
  
}

///////////////////////////////////////////////////////////////////////////////
void loop() {
  game_lost = false;
  game_won = false;
  // Show sequence up to current step
  showSequence();


  // Read player button presses
  for (int s = 0; s < currentStep; s++) {
    startGuessTime = millis();
    guess = NO_BUTTON;
    while ((millis() - startGuessTime < GUESS_TIMEOUT) && (guess == NO_BUTTON)) {
      guess = getUserTurn();

    }

    if (guess != simonSequence[s]) {
      game_lost = true;
      Serial.println("game lost");
      gameLost();
    }
  }

  if (!game_lost) {
    currentStep++;
    if (currentStep > sequenceLength) {
      delay(SEQUENCE_DELAY);
      Serial.println("game won");
      game_won = true;
      gameWon();

    }
    if (!game_won && !game_lost) {
      delay(SEQUENCE_DELAY);
    }
  }
}
