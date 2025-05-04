package pedroaba.java.race.utils;

import pedroaba.java.race.constants.Config;
import pedroaba.java.race.entities.Car;
import pedroaba.java.race.powers.Banana;
import pedroaba.java.race.powers.Boost;
import pedroaba.java.race.powers.RedShell;

import java.util.Arrays;
import java.util.Random;

public class ApplyPowerTo {
    public static void apply(Car targetToReceivePower, Car target) {
        double[] probabilities = {5.0, 10.0, 2.0};

        double total = Arrays.stream(probabilities).sum();

        int indexOfPower = -1;
        double randomValue = Math.random() * total;
        for (int i = 0; i < probabilities.length; i++) {
            if (randomValue < probabilities[i]) {
                indexOfPower = i;
                break;
            }
            randomValue -= probabilities[i];
        }

        Random random = new Random();
        switch (indexOfPower) {
            case 0:
                target.setPower(
                    new Banana(target, Config.TIME_DURATION_OF_BANANA_POWER)
                );
                break;
            case 1:
                target.setPower(
                    new Boost(targetToReceivePower, Config.SPEED_BOOST_AMOUNT)
                );
                break;
            case 2:
                target.setPower(
                    new RedShell(target, random.nextDouble(Config.START_RANGE_OF_POWER_RANGE, Config.END_RANGE_OF_POWER_RANGE), Config.TIME_DURATION_OF_RED_SHELL_POWER)
                );
                break;
            default:
                break;
        }
    }

    public static void applyBoost(Car targetToReceivePower) {
        Random random = new Random();
        targetToReceivePower.setPower(
            new Boost(targetToReceivePower, Config.SPEED_BOOST_AMOUNT)
        );
    }
}
