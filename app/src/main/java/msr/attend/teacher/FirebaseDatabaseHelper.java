package msr.attend.teacher;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import msr.attend.teacher.Model.ClassModel;
import msr.attend.teacher.Model.CoordinatorModel;
import msr.attend.teacher.Model.StudentModel;
import msr.attend.teacher.Model.TeacherLoginModel;
import msr.attend.teacher.Model.TeacherModel;

public class FirebaseDatabaseHelper {
    private FirebaseDatabase database;
    private DatabaseReference teacherLogin;
    private DatabaseReference myBatchStudent;
    private DatabaseReference coordinatorRef;
    private DatabaseReference classInfoRef;

    public FirebaseDatabaseHelper() {
        database = FirebaseDatabase.getInstance();
        teacherLogin = database.getReference().child("Teachers");
        myBatchStudent = database.getReference().child("Students");
        coordinatorRef = database.getReference().child("Coordinators");
        classInfoRef = database.getReference().child("ClassInformation");

    }

    public void getClassInfo(String teacherId, FireMan.ClassInfoListener listener){
        List<ClassModel> list = new ArrayList<>();
        classInfoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    if (ds.exists()){
                        ClassModel classModel = ds.getValue(ClassModel.class);
                        if (classModel.getTeacherId().equals(teacherId)) {
                            list.add(classModel);
                        }
                    }
                }
                listener.classInfoIsLoaded(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getCourseCoordinator(String id,FireMan.CoordinatorListener listener){
        coordinatorRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    CoordinatorModel model = snapshot.getValue(CoordinatorModel.class);
                    listener.coordinatorIsLoad(model);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getMyBatchStudent(String batch, FireMan.MyBatchStudentLoad load){
        myBatchStudent.orderByChild("batch").equalTo(batch).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<StudentModel> list = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()){
                    list.add(ds.getValue(StudentModel.class));
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
                        login.loginIsSuccess(teacher.getId());
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
