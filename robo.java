import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import java.io.IOException;

public class UltrasonicSensor {

    private static final int DIST_THRESHOLD = 15;

    public static void main(String[] args) throws InterruptedException {
        final GpioController gpio = GpioFactory.getInstance();

        final GpioPinDigitalOutput leftTrig = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "LeftTrig", PinState.LOW);
        final GpioPinDigitalInput leftEcho = gpio.provisionDigitalInputPin(RaspiPin.GPIO_05, PinPullResistance.PULL_DOWN);

        final GpioPinDigitalOutput rightTrig = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "RightTrig", PinState.LOW);
        final GpioPinDigitalInput rightEcho = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, PinPullResistance.PULL_DOWN);

        while (true) {
            double leftDistance = measureDistance(leftTrig, leftEcho);
            double rightDistance = measureDistance(rightTrig, rightEcho);

            if (leftDistance <= DIST_THRESHOLD) {
                speakDistance(leftDistance);
            }

            if (rightDistance <= DIST_THRESHOLD) {
                speakDistance(rightDistance);
            }

            Thread.sleep(1000);
        }
    }

    private static double measureDistance(GpioPinDigitalOutput trig, GpioPinDigitalInput echo) throws InterruptedException {
        trig.low();
        Thread.sleep(500);

        trig.high();
        Thread.sleep(0, 10000);  // 10 microseconds
        trig.low();

        long start = System.nanoTime();
        while (echo.isLow()) {
            start = System.nanoTime();
        }

        long end = System.nanoTime();
        while (echo.isHigh()) {
            end = System.nanoTime();
        }

        double pulseDuration = (end - start) / 1e3; // in microseconds
        return pulseDuration / 58.2;  // convert to centimeters
    }

    private static void speakDistance(double distance) {
        try {
            String command = String.format("flite -t 'Obstacle detected at %.2f centimeters'", distance);
            Process proc = Runtime.getRuntime().exec(command);
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
