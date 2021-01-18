package msr.attend.teacher;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import msr.attend.teacher.Model.TeacherLoginModel;
import msr.attend.teacher.Model.UserPref;

public class Login extends Fragment {
    private UserPref userPref;
    private EditText phone, pass;
    private Button loginBtn;
    private FragmentInterface fragmentInterface;

    public Login() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        phone = view.findViewById(R.id.loginPhone);
        pass = view.findViewById(R.id.loginPass);
        loginBtn = view.findViewById(R.id.signInBtn);
        userPref = new UserPref(getContext());

        getActivity().setTitle("Login Teacher Account");

        fragmentInterface = (FragmentInterface) getActivity();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FirebaseDatabaseHelper().login(new TeacherLoginModel(phone.getText().toString(), pass.getText().toString()),
                        new FireMan.TeacherLogin() {
                    @Override
                    public void loginIsSuccess(String id, String department, String name) {
                        userPref.setTeacherId(id);
                        userPref.setDepartment(department);
                        userPref.setUserName(name);
                        fragmentInterface.gotoDashBoard();
                    }

                    @Override
                    public void loginIsFailed() {
                        Toast.makeText(getContext(), "Failed, Contact admin", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}