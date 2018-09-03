const int pin_A = 7;  
const int pin_B = 8;  
unsigned char encoder_A;
unsigned char encoder_B;
unsigned char encoder_A_prev = 1; //HIGH; not-rotating

unsigned long currentTime;
unsigned long loopTime;

int counter = 0; 

void setup()  {
  pinMode(pin_A, INPUT);
  pinMode(pin_B, INPUT);
  currentTime = millis();
  loopTime = currentTime; 
  //software solution for debouncing is to enforce a delay b/w the checks on signal changes
  Serial.begin(9600);
} 

void loop()  {
    currentTime = millis();
    //this lets the loop wait until the debouncing is done, we don't want to minimize the delay so much
    if(currentTime >= (loopTime + 5)){// 5ms since last check of encoder = 200Hz
      // Read encoder pins
      encoder_A = digitalRead(pin_A);    
      encoder_B = digitalRead(pin_B);   
      if((!encoder_A) && (encoder_A_prev)){
        // A has gone from high to low; A went from {not rotating} to {rotating}
        if(encoder_B) { 
          // B is high: NOT ROTATING, clockwise (A got low before B)
//          counter++;
//          Serial.println(counter);
          Serial.println('A');               
        } else {
          // B is low; ROTATING, counter-clockwise (B got low before A. We get to this condition after B starts rotating in anti-clockwise direction
          //b/c we check on the state of A in the outer condition, which is 90 degrees out of phase & goes from high to low after B, if we are rotating in the counter-clockwise direction
//          counter--;      
//          Serial.println(counter);
          Serial.println('D');               
        }   
      }
      encoder_A_prev = encoder_A;     // Store value of A for next time    
      loopTime = currentTime; // Updates loopTime
    }
}
