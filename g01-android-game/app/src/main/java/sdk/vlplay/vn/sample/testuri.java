package sdk.vlplay.vn.sample;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ThuyChi on 1/5/2017.
 */
public class testuri  implements Parcelable/*extends Activity */ {
    protected testuri(Parcel in) {
    }

    public static final Creator<testuri> CREATOR = new Creator<testuri>() {
        @Override
        public testuri createFromParcel(Parcel in) {
            return new testuri(in);
        }

        @Override
        public testuri[] newArray(int size) {
            return new testuri[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_main);
//        Uri data = this.getIntent().getData();
//        if (data != null && data.isHierarchical()) {
//            String uri = this.getIntent().getDataString();
//            Log.i("MyApp", "Deep link clicked " + uri);
//        }
//    }

//    public static void main(String... args) {
//        double a = 7d;
//        if (a > 7) {
//            System.out.println("a > 7");
//        } else if (a < 7) {
//            System.out.println("a < 7");
//        } else {
//            System.out.println("a = 7");
//        }
//    }

}
