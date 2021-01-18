package msr.attend.teacher;

import android.app.Dialog;
import android.content.Intent;
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

import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import msr.attend.teacher.Messenger.MessengerActivity;
import msr.attend.teacher.Model.ClassModel;
import msr.attend.teacher.Model.UserPref;

public class DashBoard extends Fragment {
    private UserPref userPref;
    private ImageButton myStudent, myBatch, attendance, notificationSet, messengerBtn;
    private FragmentInterface fragmentInterface;
    private FirebaseDatabaseHelper firebaseDatabaseHelper;

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
        messengerBtn = view.findViewById(R.id.messengerBtn);
        userPref = new UserPref(getContext());
        userPref.setIsLogin(true);

        getActivity().setTitle("Dashboard");

        firebaseDatabaseHelper = new FirebaseDatabaseHelper();

        int back = getFragmentManager().getBackStackEntryCount();
        if (back > 0){
            for (int i = 0; i < back; i++) {
                getFragmentManager().popBackStack();
            }
        }

        fragmentInterface = (FragmentInterface) getActivity();

        myStudent.setOnClickListener(v -> getBatchAndGoToListStudent());

        myBatch.setOnClickListener(v -> fragmentInterface.gotoMyBatchChooser());

        attendance.setOnClickListener(v -> fragmentInterface.gotoMyClassAttend());

        notificationSet.setOnClickListener(v -> fragmentInterface.gotoMyNotification());

        messengerBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MessengerActivity.class);
            getContext().startActivity(intent);
        });

        firebaseDatabaseHelper.getSuperSelectedTeacher(userPref.getTeacherId(), su -> {
            userPref.setSuperUser(su);
        });

        updateToken();
    }

    private void updateToken() {
        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        new FirebaseDatabaseHelper().setNotificationToken(refreshToken,userPref.getTeacherId());
    }

    private void getBatchAndGoToListStudent() {
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.my_student_dialog);

        Spinner spinner = dialog.findViewById(R.id.myBatch);

        firebaseDatabaseHelper.getClassInfo(userPref.getTeacherId(), new FireMan.ClassInfoListener() {
            @Override
            public void classInfoIsLoaded(List<ClassModel> list) {
                List<String> batchSubCode = new ArrayList<>();
                Collections.sort(list, (o1, o2) -> o1.getBatch().compareTo(o2.getBatch()));
                for (ClassModel model : list){
                    batchSubCode.add(model.getBatch() +" -> "+model.getSubCode());
                }
                ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, batchSubCode.toArray());
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
            }

            @Override
            public void classInfoIsInserted() {

            }
        });

        Button button = dialog.findViewById(R.id.myStudentBtn);
        button.setOnClickListener(v -> {
            String[] batchSubCode = spinner.getSelectedItem().toString().split(" -> ");
            fragmentInterface.gotoAttendViewByBatch(batchSubCode[0], batchSubCode[1]);
            dialog.dismiss();
        });

        dialog.show();
    }
}