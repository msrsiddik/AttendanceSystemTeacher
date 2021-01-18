package msr.attend.teacher;

import java.util.List;
import java.util.Set;

import msr.attend.teacher.Model.ClassAttendModel;
import msr.attend.teacher.Model.ClassModel;
import msr.attend.teacher.Model.ClassRepresentative;
import msr.attend.teacher.Model.CoordinatorModel;
import msr.attend.teacher.Model.NoticeModel;
import msr.attend.teacher.Model.StudentModel;

public class FireMan {
    public interface TeacherLogin{ void loginIsSuccess(String id, String department, String name); void loginIsFailed(); }
    public interface MyBatchStudentLoad{ void studentIsLoaded(List<StudentModel> list); }

    public interface CoordinatorListener {
        void coordinatorIsLoaded(List<CoordinatorModel> models);
    }

    public interface ClassInfoListener {
        void classInfoIsLoaded(List<ClassModel> list);
        void classInfoIsInserted();

    }

    public interface StudentDataShort{
        void studentIsLoaded(List<StudentModel> students);
        void studentIsInserted();
        void studentIsDeleted();
        void studentIsEdited();
    }

    public interface ClassAttendListener{ void classIsLoaded(List<ClassAttendModel> classAttendModels); }

    public interface AttendDataShort{
        void classAttendListener(List<ClassAttendModel> attendList);
    }

    public interface NoticeDataShort{
        void noticeLoadListener(List<NoticeModel> noticeModels);
    }

    public interface RunningBatchShot {
        void batchListener(Set<String> batchs);
    }

}
