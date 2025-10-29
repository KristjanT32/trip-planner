package krisapps.tripplanner.misc.utils;

import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

public class AnimationUtils {

    public static void animateHBoxHorizontalScale(HBox hbox, double from, double to, Duration duration, Runnable onComplete) {
        ScaleTransition anim = new ScaleTransition(duration, hbox);
        anim.setFromX(from);
        anim.setToX(to);
        anim.setFromY(hbox.getScaleY());
        anim.setToY(hbox.getScaleY());
        anim.setAutoReverse(false);
        anim.setInterpolator(Interpolator.EASE_BOTH);
        anim.setOnFinished((e -> {
            onComplete.run();
        }));
        anim.play();
    }
}
