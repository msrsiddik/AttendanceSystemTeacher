package msr.attend.teacher;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import msr.attend.teacher.Model.ClassModel;
import msr.attend.teacher.Model.UserPref;

public class DashBoard extends Fragment {
    private UserPref userPref;
    private ImageButton myStudent, myBatch, attendance, notificationSet;
    private FragmentInterface fragmentInterface;

    public DashBoard() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dash_board, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        myStudent = view.findViewById(R.id.myStudent);
        myBatch = view.findViewById(R.id.myBatch);
        attendance = view.findViewById(R.id.attendance);
        notificationSet = view.findViewById(R.id.notificationSet);
        userPref = new UserPref(getContext());

        new FirebaseDatabaseHelper().getCourseCoordinator(userPref.getTeacherId(), model -> {
            userPref.setMyBatch(model.getBatch());
        });
        fragmentInterface = (FragmentInterface) getActivity();

        myStudent.setOnClickListener(v -> getBatchAndGoToListStudent());

        myBatch.setOnClickListener(v -> fragmentInterface.gotoMyBatch());

        attendance.setOnClickListener(v -> fragmentInterface.gotoMyClassAttend());

        notificationSet.setOnClickListener(v -> fragmentInterface.gotoMyNotification());
    }

    private void getBatchAndGoToListStudent() {
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.my_student_dialog);

        Spinner spinner = dialog.findViewById(R.id.myBatch);
        new FirebaseDatabaseHelper().getClassInfo(userPref.getTeacherId(), list -> {
            Set<String> batch = new HashSet<>();
            for (ClassModel model : list){
                batch.add(model.getBatch());
            }
            ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, batch.toArray());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        });

        Button button = dialog.findViewById(R.id.myStudentBtn);
        button.setOnClickListener(v -> {
            dialog.dismiss();
            fragmentInterface.gotoAttendViewByBatch(spinner.getSelectedItem().toString());
        });

        dialog.show();
    }
}