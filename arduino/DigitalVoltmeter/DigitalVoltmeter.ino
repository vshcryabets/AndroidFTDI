//electronicsblog.net 
//http://www.electronicsblog.net/digital-voltmeter-arduino-ant-pc-visual-c-comunication-via-serial-port/
//Arduino communication with PC via serial port.

int voltage=0;
int channel =0;
unsigned char incomingByte = 0;
boolean measure=false;

void setup() {

  Serial.begin(9600);
}

void loop() { 

  if (measure) {

    voltage=analogRead(channel);
    Serial.write(0xAB);
    Serial.write(voltage>>8);
    Serial.write(voltage & 0xFF);
    delay(50);

  }

  if (Serial.available() > 0) {
    delay(10);

    if(Serial.read()=='a') {
      incomingByte =Serial.read();

      switch (incomingByte) {

      case 'a':
        measure=true;
        channel=0;
        break;

      case 'b':
        measure=true;
        channel=1;
        break;

      case 'c':
        measure=true;
        channel=2;
        break;

      case 'd':
        measure=false;
        break;

      }

    }

  }                

};
