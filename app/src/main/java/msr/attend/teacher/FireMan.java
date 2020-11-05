package msr.attend.teacher;

import java.util.List;

public class FireMan {
    public interface TeacherLogin{ void loginIsSuccess(); void loginIsFailed(); }
    public interface MyBatchStudentLoad{ void studentIsLoaded(List<String> list); }
}
