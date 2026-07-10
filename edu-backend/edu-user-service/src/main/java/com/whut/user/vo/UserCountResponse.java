package com.whut.user.vo;

/**
 * 用户统计响应
 */
public class UserCountResponse {

    private long studentCount;
    private long teacherCount;

    public UserCountResponse() {}

    public UserCountResponse(long studentCount, long teacherCount) {
        this.studentCount = studentCount;
        this.teacherCount = teacherCount;
    }

    public long getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(long studentCount) {
        this.studentCount = studentCount;
    }

    public long getTeacherCount() {
        return teacherCount;
    }

    public void setTeacherCount(long teacherCount) {
        this.teacherCount = teacherCount;
    }
}
