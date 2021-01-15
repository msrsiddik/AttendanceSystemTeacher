package msr.attend.teacher;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import msr.attend.teacher.Model.ClassAttendModel;
import msr.attend.teacher.Model.ClassModel;
import msr.attend.teacher.Model.CoordinatorModel;
import msr.attend.teacher.Model.NoticeModel;
import msr.attend.teacher.Model.StudentModel;
import msr.attend.teacher.Model.TeacherLoginModel;
import msr.attend.teacher.Model.TeacherModel;
import msr.attend.teacher.Notification.APIService;
import msr.attend.teacher.Notification.Client;
import msr.attend.teacher.Notification.Data;
import msr.attend.teacher.Notification.MyResponse;
import msr.attend.teacher.Notification.NotificationSender;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirebaseDatabaseHelper {
    private FirebaseDatabase database;
    private DatabaseReference teacherLogin;
    private DatabaseReference myBatchStudent;
    private DatabaseReference coordinatorRef;
    private DatabaseReference classInfoRef;
    private DatabaseReference studentRef;
    private DatabaseReference classAttendInfo;
    private DatabaseReference notification;
    private DatabaseReference universityEntry;

    private APIService apiService;

    public FirebaseDatabaseHelper() {
        database = FirebaseDatabase.getInstance();
        teacherLogin = database.getReference().child("Teachers");
        myBatchStudent = database.getReference().child("Students");
        coordinatorRef = database.getReference().child("Coordinators");
        classInfoRef = database.getReference().child("ClassInformation");
        studentRef = database.getReference().child("Students");
        classAttendInfo = database.getReference().child("ClassAttendInfo");
        notification = database.getReference().child("Notification");
        universityEntry = database.getReference().child("AttendInfoInUniversity");

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
    }

    public void getAttendanceBySubjectWiseSelectBatch(String batch, String subjectCode) {
        List<String> dates = new ArrayList<>();
        Map<String, Map<String, String>> studentAttendByDate = new HashMap<>();
        classAttendInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot date : snapshot.getChildren()) {
                    dates.add(date.getKey());
                    Map<String, String> attend = new HashMap<>();
                    for (DataSnapshot subject : date.getChildren()) {
                        for (DataSnapshot classAttend : subject.getChildren()) {
                            ClassAttendModel attendModel = classAttend.getValue(ClassAttendModel.class);
                            if (attendModel.getSubjectCode().equals(subjectCode)) {
                                attend.put(attendModel.getStuId(), attendModel.getPresent());
                            }
                        }
                    }
                    studentAttendByDate.put(date.getKey(), attend);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        studentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public interface UniversityEntry {
        void CurrentStatusListener(List<String> studentList);
    }

    public void getUniEntryCurrentStatus(String date, final UniversityEntry entry) {
        universityEntry.child("CurrentStatus").child(date).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> list = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    list.add(ds.getKey());
                    Log.e("Data", ds.getKey());
                }
                entry.CurrentStatusListener(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void sendNoticeByBatch(String title, String body, String batch, Context context) {
        notification.child("Tokens").child(batch).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String studentToken = ds.getValue(String.class);
                    Data data = new Data(title, body);
                    NotificationSender sender = new NotificationSender(data, studentToken);
                    apiService.sendNotifcation(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if (response.code() == 200) {
                                if (response.body().success != 1) {
                                    Toast.makeText(context, "Failed ", Toast.LENGTH_LONG);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void removeNotice(String teacherId, String noticeId) {
        notification.child("Notice").child(teacherId).child(noticeId).setValue(null);
    }

    public void getNotice(String teacherId, final FireMan.NoticeDataShort dataShort) {
        List<NoticeModel> noticeModels = new ArrayList<>();
        notification.child("Notice").child(teacherId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                noticeModels.clear();
                for (DataSnapshot d : snapshot.getChildren()) {
                    NoticeModel model = d.getValue(NoticeModel.class);
                    noticeModels.add(model);
                }
                Collections.reverse(noticeModels);
                dataShort.noticeLoadListener(noticeModels);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setNotice(NoticeModel notice) {
        String key = notification.push().getKey().substring(0, 10);
        notice.setNoticeId(key);
        notification.child("Notice").child(notice.getTeacherId()).child(key).setValue(notice);
    }

    public void getAllAttendanceInfoByBatchAndSubjectCode(String batch, String subjectCode, final FireMan.AttendDataShort dataShort) {
        classAttendInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ClassAttendModel> list = new ArrayList<>();
                for (DataSnapshot d : snapshot.getChildren()) {
                    //date path
                    for (DataSnapshot a : d.getChildren()) {
                        //subject code path
                        for (DataSnapshot b : a.getChildren()) {
                            ClassAttendModel attendModel = b.getValue(ClassAttendModel.class);
                            if (attendModel.getBatch().equals(batch) && attendModel.getSubjectCode().equals(subjectCode)) {
                                list.add(attendModel);
                            }
                        }
                    }
                }
                dataShort.classAttendListener(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getAllAttendanceInfoByStudentIdAndSubjectCode(String stuId, String subjectCode, final FireMan.AttendDataShort dataShort) {
        classAttendInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ClassAttendModel> list = new ArrayList<>();
                for (DataSnapshot d : snapshot.getChildren()) {
                    //date path
                    for (DataSnapshot a : d.getChildren()) {
                        //subject code path
                        for (DataSnapshot b : a.getChildren()) {
                            ClassAttendModel attendModel = b.getValue(ClassAttendModel.class);
                            if (attendModel.getStuId().equals(stuId) && attendModel.getSubjectCode().equals(subjectCode) && attendModel.getPresent().equals("true")) {
                                list.add(attendModel);
                            }
                        }
                    }
                }
                dataShort.classAttendListener(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getAllAttendanceInfoByStudentId(String stuId, final FireMan.AttendDataShort dataShort) {
        List<String> dates = new ArrayList<>();

//        Map<String, List<ClassAttendModel>> map = new HashMap<>();

        classAttendInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ClassAttendModel> list = new ArrayList<>();
                for (DataSnapshot d : snapshot.getChildren()) {
                    //date path
                    for (DataSnapshot a : d.getChildren()) {
                        //subject code path
                        for (DataSnapshot b : a.getChildren()) {
                            ClassAttendModel attendModel = b.getValue(ClassAttendModel.class);
                            if (attendModel.getStuId().equals(stuId)) {
                                list.add(attendModel);
                            }
                        }
//                        map.put(d.getKey(), list);
                    }
                }

                dataShort.classAttendListener(list);

//                for (Map.Entry<String, List<ClassAttendModel>> listMap : map.entrySet()){
//                    for (ClassAttendModel c : listMap.getValue()){
//                            Log.e("Student ",c.getStuId());
//                    }
//                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getAttendDataByDate(String date, String classCode, final FireMan.ClassAttendListener listener) {
        List<ClassAttendModel> list = new ArrayList<>();
        classAttendInfo.child(date).child(classCode)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            if (ds.exists()) {
                                ClassAttendModel attendModel = ds.getValue(ClassAttendModel.class);
                                list.add(attendModel);
                            }
                        }
                        listener.classIsLoaded(list);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    public void insertClassAttend(ClassAttendModel attendModel, String date, Context context) {

        classAttendInfo.child(date).child(attendModel.getSubjectCode()).child(attendModel.getStuId())
                .setValue(attendModel, (error, ref) -> {
                    Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
                });
    }

    public void editStudent(StudentModel model, final FireMan.StudentDataShort dataShort) {
        studentRef.child(model.getId()).setValue(model)
                .addOnSuccessListener(aVoid -> {
                    dataShort.studentIsEdited();
                });
    }

    public void deleteStudent(String id, final FireMan.StudentDataShort dataShort) {
        studentRef.child(id).setValue(null)
                .addOnSuccessListener(aVoid -> dataShort.studentIsDeleted());
    }

    public void getStudents(FireMan.StudentDataShort dataShort) {
        List<StudentModel> list = new ArrayList<>();
        studentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    StudentModel model = ds.getValue(StudentModel.class);
                    list.add(model);
                }
                dataShort.studentIsLoaded(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    interface StudentData{
        void studentListener(StudentModel student);
    }

    public void getStudentById(String studentId, final StudentData dataShort) {
        studentRef.orderByChild("id").equalTo(studentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot d : snapshot.getChildren()) {
                    StudentModel model = d.getValue(StudentModel.class);
                    dataShort.studentListener(model);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void insertStudent(StudentModel student, final FireMan.StudentDataShort dataShort) {
        String id = studentRef.push().getKey();
        student.setId(id);
        studentRef.child(student.getId()).setValue(student)
                .addOnSuccessListener(aVoid -> dataShort.studentIsInserted());
    }

    public void getClassInfo(String teacherId, FireMan.ClassInfoListener listener) {
        List<ClassModel> list = new ArrayList<>();
        classInfoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.exists()) {
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

    public void getCourseCoordinator(String id, final FireMan.CoordinatorListener listener) {
        coordinatorRef.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CoordinatorModel> coordinatorModels = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        CoordinatorModel model = ds.getValue(CoordinatorModel.class);
                        coordinatorModels.add(model);
                    }
                }
                listener.coordinatorIsLoaded(coordinatorModels);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getMyBatchStudent(String batch, FireMan.MyBatchStudentLoad load) {
        myBatchStudent.orderByChild("batch").equalTo(batch).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<StudentModel> list = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    list.add(ds.getValue(StudentModel.class));
                }
                load.studentIsLoaded(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void login(TeacherLoginModel model, final FireMan.TeacherLogin login) {
        Query query = teacherLogin.orderByChild("phone").equalTo(model.getPhone());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    TeacherModel teacher = ds.getValue(TeacherModel.class);
                    if (teacher.getPassword().equals(model.getPassword())) {
                        login.loginIsSuccess(teacher.getId(), teacher.getDepartment());
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

    public interface ProfileDataShot {
        void profileInfoListener(TeacherModel teacherModel);

        void profileEditListener();
    }

    public void getProfileInfo(String id, final ProfileDataShot dataShot) {
        teacherLogin.orderByChild("id").equalTo(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    TeacherModel model = ds.getValue(TeacherModel.class);
                    dataShot.profileInfoListener(model);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void editTeacher(TeacherModel teacher, final ProfileDataShot dataShort) {
        teacherLogin.child(teacher.getId()).setValue(teacher)
                .addOnSuccessListener(aVoid -> dataShort.profileEditListener());
    }

    public interface ClassModelDataShot {
        void classModelListener(List<ClassModel> models);
    }

    public void classModelByTeacherId(String id, final ClassModelDataShot classModelShot) {
        classInfoRef.orderByChild("teacherId").equalTo(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ClassModel> classModels = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ClassModel model = ds.getValue(ClassModel.class);
                    classModels.add(model);
                }
                classModelShot.classModelListener(classModels);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
