package msr.attend.teacher.Model;

import android.content.Context;
import android.content.SharedPreferences;

public class ClassPreferences {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public ClassPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences("Class_Pref",Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setHighestClass(String batch, int highestClass){
        editor.putInt(batch,highestClass);
        editor.commit();
    }

    public int getHighestClass(String batch){
        return sharedPreferences.getInt(batch, 0);
    }
}
