package msr.attend.teacher;

import java.util.List;

import msr.attend.teacher.Model.ClassAttendModel;
import msr.attend.teacher.Model.ClassModel;
import msr.attend.teacher.Model.CoordinatorModel;
import msr.attend.teacher.Model.NoticeModel;
import msr.attend.teacher.Model.StudentModel;

public class FireMan {
    public interface TeacherLogin{ void loginIsSuccess(String id); void loginIsFailed(); }
    public interface MyBatchStudentLoad{ void studentIsLoaded(List<StudentModel> list); }

    public interface CoordinatorListener {
        void coordinatorIsLoad(CoordinatorModel model);
    }

    public interface ClassInfoListener {
        void classInfoIsLoaded(List<ClassModel> list);
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

}
