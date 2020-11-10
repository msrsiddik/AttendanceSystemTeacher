package msr.attend.teacher;

import java.util.List;

import msr.attend.teacher.Model.CoordinatorModel;

public class FireMan {
    public interface TeacherLogin{ void loginIsSuccess(String id); void loginIsFailed(); }
    public interface MyBatchStudentLoad{ void studentIsLoaded(List<String> list); }

    public interface CoordinatorListener {
        void coordinatorIsLoad(CoordinatorModel model);
    }
}
