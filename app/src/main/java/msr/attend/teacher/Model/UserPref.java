package msr.attend.teacher.Model;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPref {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public UserPref(Context context) {
        sharedPreferences = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setTeacherId(String id){
        editor.putString("id",id);
        editor.commit();
    }

    public String getTeacherId(){
        return sharedPreferences.getString("id","");
    }

    public void setMyBatch(String batch) {
        editor.putString("batch",batch);
        editor.commit();
    }

    public String getMyBatch(){
        return sharedPreferences.getString("batch","");
    }
}