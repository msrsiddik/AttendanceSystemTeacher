package msr.attend.teacher;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import msr.attend.teacher.Model.StudentModel;
import msr.attend.teacher.Model.TeacherLoginModel;
import msr.attend.teacher.Model.TeacherModel;

public class FirebaseDatabaseHelper {
    private FirebaseDatabase database;
    private DatabaseReference teacherLogin;
    private DatabaseReference myBatchStudent;

    public FirebaseDatabaseHelper() {
        database = FirebaseDatabase.getInstance();
        teacherLogin = database.getReference().child("Teachers");
        myBatchStudent = database.getReference().child("Students");
    }

    public void getMyBatchStudent(FireMan.MyBatchStudentLoad load){
        myBatchStudent.orderByChild("batch").equalTo("42").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> list = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()){
                    list.add(ds.getValue(StudentModel.class).getName());
                }
                load.studentIsLoaded(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void login(TeacherLoginModel model, final FireMan.TeacherLogin login){
        Query query = teacherLogin.orderByChild("phone").equalTo(model.getPhone());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    TeacherModel teacher = ds.getValue(TeacherModel.class);
                    if (teacher.getPassword().equals(model.getPassword())){
                        login.loginIsSuccess();
                    } else {
                        login.loginIsFailed();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
