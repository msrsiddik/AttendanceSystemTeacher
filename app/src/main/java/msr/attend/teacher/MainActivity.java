package msr.attend.teacher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements FragmentInterface{
    private FragmentManager fragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();

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
        fragmentManager.beginTransaction().replace(R.id.fragContainer, new MyBatch()).commit();
    }
}