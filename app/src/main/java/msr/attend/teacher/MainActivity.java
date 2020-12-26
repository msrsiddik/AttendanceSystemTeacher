package msr.attend.teacher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;

import msr.attend.teacher.Model.ClassModel;
import msr.attend.teacher.Model.Utils;
import msr.attend.teacher.Service.DIUService;

public class MainActivity extends AppCompatActivity implements FragmentInterface{
    private FragmentManager fragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();

        startService(new Intent(this, DIUService.class));

        login();
    }

    @Override
    public void login() {
        fragmentManager.beginTransaction().add(R.id.fragContainer, new Login()).commit();
    }

    @Override
    public void gotoDashBoard() {
        fragmentManager.beginTransaction().replace(R.id.fragContainer, new DashBoard()).commit();
    }

    @Override
    public void gotoMyBatch() {
        fragmentManager.beginTransaction().replace(R.id.fragContainer, new MyBatch()).addToBackStack("myBatch").commit();
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
        fragmentManager.beginTransaction().replace(R.id.fragContainer, new NotificationSend()).addToBackStack(null).commit();
    }

    @Override
    public void gotoAttendViewByBatch(String batch) {
        Bundle bundle = new Bundle();
        bundle.putString("batch", batch);
        AttendanceViewByBatch viewByBatch = new AttendanceViewByBatch();
        viewByBatch.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.fragContainer, viewByBatch).addToBackStack(null).commit();
    }
}