package msr.attend.teacher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import msr.attend.teacher.Model.ClassModel;
import msr.attend.teacher.Model.UserPref;
import msr.attend.teacher.Model.Utils;
import msr.attend.teacher.Service.DIUService;

public class MainActivity extends AppCompatActivity implements FragmentInterface, BottomNavigationView.OnNavigationItemSelectedListener{
    private FragmentManager fragmentManager;
    private UserPref userPref;
    private BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();

        userPref = new UserPref(this);

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        if (userPref.getIsLogin()){
            gotoDashBoard();
        } else {
            login();
            navigation.setVisibility(View.INVISIBLE);
        }

        startService(new Intent(this, DIUService.class));

    }

    @Override
    public void login() {
        fragmentManager.beginTransaction().add(R.id.fragContainer, new Login()).commit();
    }

    @Override
    public void gotoDashBoard() {
        navigation.setVisibility(View.VISIBLE);
        fragmentManager.beginTransaction().replace(R.id.fragContainer, new DashBoard()).commit();
    }

    @Override
    public void gotoMyBatch(String selectBatch) {
        Bundle bundle = new Bundle();
        bundle.putString("batch", selectBatch);
        MyBatch myBatch = new MyBatch();
        myBatch.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.fragContainer, myBatch).addToBackStack("myBatch").commit();
    }

    @Override
    public void gotoMyClassAttend() {
        fragmentManager.beginTransaction().replace(R.id.fragContainer, new MyClass()).addToBackStack(null).commit();
    }

    @Override
    public void gotoAttendanceRegister(ClassModel classModel) {
        Bundle bundle = new Bundle();
        String model = Utils.getGsonParser().toJson(classModel);
        bundle.putString("classModel", model);
        Attendance_Register attendance_register = new Attendance_Register();
        attendance_register.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.fragContainer, attendance_register).addToBackStack(null).commit();
    }

    @Override
    public void addStudentForm() {
        fragmentManager.beginTransaction().replace(R.id.fragContainer, new AddStudent()).addToBackStack(null).commit();
    }

    @Override
    public void gotoMyNotification() {
        fragmentManager.beginTransaction().replace(R.id.fragContainer, new NoticeBoard()).addToBackStack(null).commit();
    }

    @Override
    public void gotoNoticeSet() {
        fragmentManager.beginTransaction().replace(R.id.fragContainer, new NotificationSend()).addToBackStack("NoticeSet").commit();
    }

    @Override
    public void gotoAttendViewByBatch(String batch) {
        Bundle bundle = new Bundle();
        bundle.putString("batch", batch);
        AttendanceViewByBatch viewByBatch = new AttendanceViewByBatch();
        viewByBatch.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.fragContainer, viewByBatch).addToBackStack(null).commit();
    }

    @Override
    public void gotoMyBatchChooser() {
        fragmentManager.beginTransaction().replace(R.id.fragContainer, new MyBatchChooser()).addToBackStack(null).commit();
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragContainer, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        switch (item.getItemId()){
            case R.id.dashboard:
                fragment = new DashBoard();
                break;

            case R.id.profile:
                fragment = new Profile();
                break;

//            case R.id.notification:
////                fragment = new UserNotification();
//                break;
        }
        return loadFragment(fragment);
    }
}