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

    public void setIsLogin(boolean login){
        editor.putBoolean("isLogin",login);
        editor.commit();
    }

    public boolean getIsLogin(){
        return sharedPreferences.getBoolean("isLogin",false);
    }

    public void setDepartment(String department) {
        editor.putString("department",department);
        editor.commit();
    }

    public String getDepartment(){
        return sharedPreferences.getString("department",null);
    }

    public void setUserName(String name) {
        editor.putString("userName",name);
        editor.commit();
    }

    public String getUserName(){
        return sharedPreferences.getString("userName",null);
    }

    public void setSuperUser(boolean b) {
        editor.putBoolean("SU",b);
        editor.commit();
    }

    public boolean isSuperUser(){
        return sharedPreferences.getBoolean("SU", false);
    }

    public void clear(){
        editor.clear();
        editor.commit();
    }

}
