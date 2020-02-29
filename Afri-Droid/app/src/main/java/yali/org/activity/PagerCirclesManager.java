package yali.org.activity;

import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import yali.org.R;


public class PagerCirclesManager {

    public static void dotStatusManage(int position, AppCompatActivity activity){
        ImageView dotOne = (ImageView) activity.findViewById(R.id.circle_page1);
        ImageView dotTwo = (ImageView) activity.findViewById(R.id.circle_page2);
        ImageView dotThird = (ImageView) activity.findViewById(R.id.circle_page3);
        ImageView dotFourth = (ImageView) activity.findViewById(R.id.circle_page4);
        switch(position){
            case 0:
                setActiveDot(dotOne);
                setInactiveDot(dotTwo);
                setInactiveDot(dotThird);
                setInactiveDot(dotFourth);
                break;
            case 1:
                setInactiveDot(dotOne);
                setActiveDot(dotTwo);
                setInactiveDot(dotThird);
                setInactiveDot(dotFourth);
                break;
            case 2:
                setInactiveDot(dotOne);
                setInactiveDot(dotTwo);
                setActiveDot(dotThird);
                setInactiveDot(dotFourth);
                break;
            case 3:
                setInactiveDot(dotOne);
                setInactiveDot(dotTwo);
                setInactiveDot(dotThird);
                setActiveDot(dotFourth);
                break;
        }
    }

    public static void setActiveDot(ImageView dot){
        if(dot != null) dot.setImageResource(R.drawable.circle_shape_active);
    }

    public static void setInactiveDot(ImageView dot){
        if(dot != null) dot.setImageResource(R.drawable.circle_shape_inactive);
    }

}
