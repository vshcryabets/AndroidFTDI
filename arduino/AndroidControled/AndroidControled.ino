/*
  Blink
  Turns on an LED on for one second, then off for one second, repeatedly.
 
  This example code is in the public domain.
 */
 
 byte data;

void setup() {  
  // start serial port at 9600 bps:
  Serial.begin(9600);  
  // initialize the digital pin as an output.
  // Pin 13 has an LED connected on most Arduino boards:
  pinMode(13, OUTPUT);     
  data = 100;
}

void loop() {
  digitalWrite(13, HIGH);   // set the LED on
  delay(data+30);              // wait for a second
  digitalWrite(13, LOW);    // set the LED off
  delay(80);              // wait for a second
    if (Serial.available() > 0) {
    // get incoming byte:
    data = Serial.read();
  }
}
